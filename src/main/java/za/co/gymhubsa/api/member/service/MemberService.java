package za.co.gymhubsa.api.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.common.storage.FileStorageService;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.dto.MemberStatusUpdateRequest;
import za.co.gymhubsa.api.member.dto.MemberUpdateRequest;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String PHOTO_SUBDIRECTORY = "members";

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public MemberResponse register(MemberRegistrationRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("A user with this email already exists");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(user);

        Member member = Member.builder()
                .user(user)
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .address(request.address())
                .emergencyContactName(request.emergencyContactName())
                .emergencyContactPhone(request.emergencyContactPhone())
                .status(MemberStatus.ACTIVE)
                .joinDate(LocalDate.now())
                .qrCode(UUID.randomUUID().toString())
                .build();
        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse getById(Long id) {
        return MemberResponse.from(findMemberOrThrow(id));
    }

    @Transactional(readOnly = true)
    public MemberResponse getByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Member member = memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> search(String keyword, MemberStatus status, Pageable pageable) {
        return memberRepository.search(blankToNull(keyword), status, pageable)
                .map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse update(Long id, MemberUpdateRequest request) {
        Member member = findMemberOrThrow(id);
        User user = member.getUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());

        member.setDateOfBirth(request.dateOfBirth());
        member.setGender(request.gender());
        member.setAddress(request.address());
        member.setEmergencyContactName(request.emergencyContactName());
        member.setEmergencyContactPhone(request.emergencyContactPhone());

        userRepository.save(user);
        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateStatus(Long id, MemberStatusUpdateRequest request) {
        Member member = findMemberOrThrow(id);
        member.setStatus(request.status());
        memberRepository.save(member);
        return MemberResponse.from(member);
    }

    /**
     * Soft-deletes a member: marks them inactive and disables login, rather
     * than removing the row, so attendance/payment/membership history
     * (built in later modules) stays intact.
     */
    @Transactional
    public void delete(Long id) {
        Member member = findMemberOrThrow(id);
        member.setStatus(MemberStatus.INACTIVE);
        member.getUser().setEnabled(false);
        memberRepository.save(member);
    }

    @Transactional
    public MemberResponse uploadProfilePhoto(Long id, MultipartFile file) {
        Member member = findMemberOrThrow(id);

        if (member.getProfilePhotoUrl() != null) {
            fileStorageService.delete(member.getProfilePhotoUrl());
        }

        String storedPath = fileStorageService.store(file, PHOTO_SUBDIRECTORY);
        member.setProfilePhotoUrl(storedPath);
        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    private Member findMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", id));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
