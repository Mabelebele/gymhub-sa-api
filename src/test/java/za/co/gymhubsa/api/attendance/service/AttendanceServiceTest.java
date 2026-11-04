package za.co.gymhubsa.api.attendance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.attendance.dto.AttendanceResponse;
import za.co.gymhubsa.api.attendance.dto.DailyAttendanceReportResponse;
import za.co.gymhubsa.api.attendance.entity.Attendance;
import za.co.gymhubsa.api.attendance.entity.CheckInMethod;
import za.co.gymhubsa.api.attendance.repository.AttendanceRepository;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private MemberRepository memberRepository;

    private AttendanceService attendanceService;

    private Member member;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(attendanceRepository, memberRepository);

        User user = User.builder().firstName("Bongani").lastName("Zondo").email("bongani@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);

        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now())
                .qrCode("qr-token-123").build();
        member.setId(1L);
    }

    @Test
    void checkIn_withNoOpenSession_createsAttendance() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(1L))
                .thenReturn(Optional.empty());

        AttendanceResponse response = attendanceService.checkIn(1L, CheckInMethod.MANUAL);

        assertThat(response.checkOutTime()).isNull();
        assertThat(response.method()).isEqualTo(CheckInMethod.MANUAL);
    }

    @Test
    void checkIn_withExistingOpenSession_throwsBadRequest() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        Attendance open = Attendance.builder().member(member).checkInTime(Instant.now())
                .method(CheckInMethod.MANUAL).build();
        when(attendanceRepository.findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(1L))
                .thenReturn(Optional.of(open));

        assertThatThrownBy(() -> attendanceService.checkIn(1L, CheckInMethod.MANUAL))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void checkInByQrCode_resolvesMemberAndChecksIn() {
        when(memberRepository.findByQrCode("qr-token-123")).thenReturn(Optional.of(member));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(1L))
                .thenReturn(Optional.empty());

        AttendanceResponse response = attendanceService.checkInByQrCode("qr-token-123");

        assertThat(response.method()).isEqualTo(CheckInMethod.QR_CODE);
    }

    @Test
    void checkInByQrCode_withUnknownCode_throwsBadRequest() {
        when(memberRepository.findByQrCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkInByQrCode("unknown"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void checkOut_withOpenSession_setsCheckOutTime() {
        Attendance open = Attendance.builder().member(member).checkInTime(Instant.now())
                .method(CheckInMethod.MANUAL).build();
        when(attendanceRepository.findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(1L))
                .thenReturn(Optional.of(open));

        AttendanceResponse response = attendanceService.checkOut(1L);

        assertThat(response.checkOutTime()).isNotNull();
        assertThat(response.durationMinutes()).isNotNull();
    }

    @Test
    void checkOut_withNoOpenSession_throwsBadRequest() {
        when(attendanceRepository.findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut(1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getMyHistory_whenMemberProfileMissing_throwsNotFound() {
        when(memberRepository.findByUser_EmailIgnoreCase("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.getMyHistory("ghost@example.com",
                org.springframework.data.domain.PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDailyReport_countsRecordsWithinDay() {
        Attendance record1 = Attendance.builder().member(member).checkInTime(Instant.now())
                .method(CheckInMethod.MANUAL).build();
        when(attendanceRepository.findByCheckInTimeBetweenOrderByCheckInTimeAsc(any(), any()))
                .thenReturn(List.of(record1));

        DailyAttendanceReportResponse report = attendanceService.getDailyReport(LocalDate.now());

        assertThat(report.totalCheckIns()).isEqualTo(1);
    }
}
