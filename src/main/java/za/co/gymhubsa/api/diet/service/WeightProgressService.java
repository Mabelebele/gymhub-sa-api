package za.co.gymhubsa.api.diet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.diet.dto.WeightProgressLogRequest;
import za.co.gymhubsa.api.diet.dto.WeightProgressLogResponse;
import za.co.gymhubsa.api.diet.entity.WeightProgressLog;
import za.co.gymhubsa.api.diet.repository.WeightProgressLogRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WeightProgressService {

    private final WeightProgressLogRepository weightProgressLogRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public WeightProgressLogResponse logWeight(Long memberId, WeightProgressLogRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", memberId));

        WeightProgressLog log = WeightProgressLog.builder()
                .member(member)
                .logDate(request.logDate() != null ? request.logDate() : LocalDate.now())
                .weightKg(request.weightKg())
                .notes(request.notes())
                .build();
        weightProgressLogRepository.save(log);

        return WeightProgressLogResponse.from(log);
    }

    @Transactional(readOnly = true)
    public Page<WeightProgressLogResponse> getHistory(Long memberId, Pageable pageable) {
        return weightProgressLogRepository.findByMemberIdOrderByLogDateDesc(memberId, pageable)
                .map(WeightProgressLogResponse::from);
    }

    @Transactional(readOnly = true)
    public WeightProgressLogResponse getLatest(Long memberId) {
        return weightProgressLogRepository.findFirstByMemberIdOrderByLogDateDesc(memberId)
                .map(WeightProgressLogResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No weight entries logged for this member"));
    }

    @Transactional
    public WeightProgressLogResponse logMyWeight(String email, WeightProgressLogRequest request) {
        return logWeight(resolveMemberIdByEmail(email), request);
    }

    @Transactional(readOnly = true)
    public Page<WeightProgressLogResponse> getMyHistory(String email, Pageable pageable) {
        return getHistory(resolveMemberIdByEmail(email), pageable);
    }

    @Transactional(readOnly = true)
    public WeightProgressLogResponse getMyLatest(String email) {
        return getLatest(resolveMemberIdByEmail(email));
    }

    private Long resolveMemberIdByEmail(String email) {
        return memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"))
                .getId();
    }
}
