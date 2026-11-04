package za.co.gymhubsa.api.trainer.service;

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
import za.co.gymhubsa.api.trainer.dto.TrainerPerformanceResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerRegistrationRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerStatusUpdateRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerUpdateRequest;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;
import za.co.gymhubsa.api.trainer.repository.MemberTrainerAssignmentRepository;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private static final String PHOTO_SUBDIRECTORY = "trainers";

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final MemberTrainerAssignmentRepository memberTrainerAssignmentRepository;

    @Transactional
    public TrainerResponse register(TrainerRegistrationRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("A user with this email already exists");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .role(Role.TRAINER)
                .enabled(true)
                .build();
        userRepository.save(user);

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(request.specialization())
                .bio(request.bio())
                .yearsOfExperience(request.yearsOfExperience())
                .status(TrainerStatus.ACTIVE)
                .hireDate(LocalDate.now())
                .build();
        trainerRepository.save(trainer);

        return TrainerResponse.from(trainer);
    }

    @Transactional(readOnly = true)
    public TrainerResponse getById(Long id) {
        return TrainerResponse.from(findTrainerOrThrow(id));
    }

    @Transactional(readOnly = true)
    public TrainerResponse getByEmail(String email) {
        return trainerRepository.findByUser_EmailIgnoreCase(email)
                .map(TrainerResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
    }

    @Transactional(readOnly = true)
    public Page<TrainerResponse> search(String keyword, TrainerStatus status, Pageable pageable) {
        return trainerRepository.search(blankToNull(keyword), status, pageable)
                .map(TrainerResponse::from);
    }

    @Transactional
    public TrainerResponse update(Long id, TrainerUpdateRequest request) {
        Trainer trainer = findTrainerOrThrow(id);
        User user = trainer.getUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());

        trainer.setSpecialization(request.specialization());
        trainer.setBio(request.bio());
        trainer.setYearsOfExperience(request.yearsOfExperience());

        userRepository.save(user);
        trainerRepository.save(trainer);

        return TrainerResponse.from(trainer);
    }

    @Transactional
    public TrainerResponse updateStatus(Long id, TrainerStatusUpdateRequest request) {
        Trainer trainer = findTrainerOrThrow(id);
        trainer.setStatus(request.status());
        trainerRepository.save(trainer);
        return TrainerResponse.from(trainer);
    }

    @Transactional
    public TrainerResponse uploadProfilePhoto(Long id, MultipartFile file) {
        Trainer trainer = findTrainerOrThrow(id);

        if (trainer.getProfilePhotoUrl() != null) {
            fileStorageService.delete(trainer.getProfilePhotoUrl());
        }

        String storedPath = fileStorageService.store(file, PHOTO_SUBDIRECTORY);
        trainer.setProfilePhotoUrl(storedPath);
        trainerRepository.save(trainer);

        return TrainerResponse.from(trainer);
    }

    @Transactional(readOnly = true)
    public TrainerPerformanceResponse getPerformance(Long id) {
        Trainer trainer = findTrainerOrThrow(id);
        long activeMemberCount = memberTrainerAssignmentRepository.countByTrainerIdAndActiveTrue(id);
        long totalAssignedEver = memberTrainerAssignmentRepository.countByTrainerId(id);

        return new TrainerPerformanceResponse(
                trainer.getId(),
                trainer.getUser().getFirstName() + " " + trainer.getUser().getLastName(),
                trainer.getYearsOfExperience(),
                activeMemberCount,
                totalAssignedEver
        );
    }

    private Trainer findTrainerOrThrow(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Trainer", id));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
