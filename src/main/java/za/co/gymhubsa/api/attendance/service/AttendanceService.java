package za.co.gymhubsa.api.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.attendance.dto.AttendanceResponse;
import za.co.gymhubsa.api.attendance.dto.DailyAttendanceReportResponse;
import za.co.gymhubsa.api.attendance.entity.Attendance;
import za.co.gymhubsa.api.attendance.entity.CheckInMethod;
import za.co.gymhubsa.api.attendance.repository.AttendanceRepository;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    /** Gym operates in South Africa; daily report boundaries use this zone regardless of server clock. */
    private static final ZoneId GYM_ZONE = ZoneId.of("Africa/Johannesburg");

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AttendanceResponse checkIn(Long memberId, CheckInMethod method) {
        Member member = findMemberOrThrow(memberId);

        attendanceRepository.findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(memberId)
                .ifPresent(open -> {
                    throw new BadRequestException("Member is already checked in; check out first");
                });

        Attendance attendance = Attendance.builder()
                .member(member)
                .checkInTime(Instant.now())
                .method(method)
                .build();
        attendanceRepository.save(attendance);

        return AttendanceResponse.from(attendance);
    }

    @Transactional
    public AttendanceResponse checkInByQrCode(String qrCode) {
        Member member = memberRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new BadRequestException("No member found for this QR code"));
        return checkIn(member.getId(), CheckInMethod.QR_CODE);
    }

    @Transactional
    public AttendanceResponse checkOut(Long memberId) {
        Attendance attendance = attendanceRepository
                .findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(memberId)
                .orElseThrow(() -> new BadRequestException("Member has no open check-in to check out of"));

        attendance.setCheckOutTime(Instant.now());
        attendanceRepository.save(attendance);

        return AttendanceResponse.from(attendance);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getHistory(Long memberId, Pageable pageable) {
        return attendanceRepository.findByMemberIdOrderByCheckInTimeDesc(memberId, pageable)
                .map(AttendanceResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getMyHistory(String email, Pageable pageable) {
        Member member = memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        return getHistory(member.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public DailyAttendanceReportResponse getDailyReport(LocalDate date) {
        Instant startOfDay = date.atStartOfDay(GYM_ZONE).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(GYM_ZONE).toInstant();

        var records = attendanceRepository.findByCheckInTimeBetweenOrderByCheckInTimeAsc(startOfDay, endOfDay)
                .stream()
                .map(AttendanceResponse::from)
                .toList();

        return new DailyAttendanceReportResponse(date, records.size(), records);
    }

    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", memberId));
    }
}
