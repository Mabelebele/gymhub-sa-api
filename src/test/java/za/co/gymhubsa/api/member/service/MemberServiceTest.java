package za.co.gymhubsa.api.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;

    private MemberService memberService;

    private User testUser;
    private Member testMember;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, userRepository, passwordEncoder, fileStorageService);

        testUser = User.builder()
                .firstName("Lerato")
                .lastName("Mokoena")
                .email("lerato@example.com")
                .password("hashed")
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        testUser.setId(1L);

        testMember = Member.builder()
                .user(testUser)
                .status(MemberStatus.ACTIVE)
                .joinDate(LocalDate.now())
                .build();
        testMember.setId(1L);
    }

    @Test
    void register_withNewEmail_createsUserAndMember() {
        MemberRegistrationRequest request = new MemberRegistrationRequest("Lerato", "Mokoena",
                "lerato@example.com", "Password123!", "0821234567", LocalDate.of(1995, 5, 1),
                null, "123 Main Rd", "Thabo Mokoena", "0839876543");

        when(userRepository.existsByEmailIgnoreCase("lerato@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");

        MemberResponse response = memberService.register(request);

        assertThat(response.firstName()).isEqualTo("Lerato");
        assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE);
        verify(userRepository).save(any(User.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void register_withExistingEmail_throwsBadRequest() {
        MemberRegistrationRequest request = new MemberRegistrationRequest("Lerato", "Mokoena",
                "lerato@example.com", "Password123!", null, null, null, null, null, null);

        when(userRepository.existsByEmailIgnoreCase("lerato@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_modifiesUserAndMemberFields() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        MemberUpdateRequest request = new MemberUpdateRequest("Lerato", "Sithole", "0821110000",
                LocalDate.of(1995, 5, 1), null, "456 New St", "Thabo Mokoena", "0839876543");

        MemberResponse response = memberService.update(1L, request);

        assertThat(response.lastName()).isEqualTo("Sithole");
        assertThat(testUser.getPhoneNumber()).isEqualTo("0821110000");
    }

    @Test
    void updateStatus_changesStatus() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        MemberResponse response = memberService.updateStatus(1L, new MemberStatusUpdateRequest(MemberStatus.SUSPENDED));

        assertThat(response.status()).isEqualTo(MemberStatus.SUSPENDED);
    }

    @Test
    void delete_softDeletesMemberAndDisablesUser() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        memberService.delete(1L);

        assertThat(testMember.getStatus()).isEqualTo(MemberStatus.INACTIVE);
        assertThat(testUser.isEnabled()).isFalse();
    }

    @Test
    void uploadProfilePhoto_storesFileAndUpdatesUrl() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(fileStorageService.store(eq(file), anyString())).thenReturn("members/abc123.jpg");

        MemberResponse response = memberService.uploadProfilePhoto(1L, file);

        assertThat(response.profilePhotoUrl()).isEqualTo("members/abc123.jpg");
    }
}
