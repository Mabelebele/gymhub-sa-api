package za.co.gymhubsa.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.co.gymhubsa.api.attendance.entity.CheckInMethod;
import za.co.gymhubsa.api.attendance.service.AttendanceService;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.diet.dto.AssignDietPlanRequest;
import za.co.gymhubsa.api.diet.dto.DietMealRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanRequest;
import za.co.gymhubsa.api.diet.dto.NutritionGoalRequest;
import za.co.gymhubsa.api.diet.dto.WeightProgressLogRequest;
import za.co.gymhubsa.api.diet.entity.GoalType;
import za.co.gymhubsa.api.diet.service.DietAssignmentService;
import za.co.gymhubsa.api.diet.service.DietPlanService;
import za.co.gymhubsa.api.diet.service.NutritionGoalService;
import za.co.gymhubsa.api.diet.service.WeightProgressService;
import za.co.gymhubsa.api.loyalty.dto.EarnPointsRequest;
import za.co.gymhubsa.api.loyalty.dto.RewardRequest;
import za.co.gymhubsa.api.loyalty.service.LoyaltyService;
import za.co.gymhubsa.api.loyalty.service.RewardService;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.service.MemberService;
import za.co.gymhubsa.api.membership.dto.AssignMembershipRequest;
import za.co.gymhubsa.api.membership.dto.MembershipPlanRequest;
import za.co.gymhubsa.api.membership.dto.MembershipPlanResponse;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;
import za.co.gymhubsa.api.membership.service.MembershipPlanService;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationType;
import za.co.gymhubsa.api.notification.service.NotificationService;
import za.co.gymhubsa.api.payment.dto.RecordPaymentRequest;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;
import za.co.gymhubsa.api.payment.service.PaymentService;
import za.co.gymhubsa.api.trainer.dto.AssignTrainerRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerRegistrationRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerResponse;
import za.co.gymhubsa.api.trainer.service.TrainerAssignmentService;
import za.co.gymhubsa.api.trainer.service.TrainerService;
import za.co.gymhubsa.api.workout.dto.AssignWorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutExerciseRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogRequest;
import za.co.gymhubsa.api.workout.entity.DifficultyLevel;
import za.co.gymhubsa.api.workout.service.WorkoutAssignmentService;
import za.co.gymhubsa.api.workout.service.WorkoutPlanService;
import za.co.gymhubsa.api.workout.service.WorkoutProgressService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a realistic, connected dataset in the {@code dev} profile only, so
 * every table has example rows to explore (H2 console) and the API is
 * immediately usable (login/Swagger "Authorize") without manual setup.
 * Never runs against the {@code prod} profile.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@gymhubsa.co.za";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@12345";
    private static final String DEFAULT_PASSWORD = "Password123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final TrainerAssignmentService trainerAssignmentService;
    private final MembershipPlanService membershipPlanService;
    private final MemberMembershipService memberMembershipService;
    private final PaymentService paymentService;
    private final AttendanceService attendanceService;
    private final WorkoutPlanService workoutPlanService;
    private final WorkoutAssignmentService workoutAssignmentService;
    private final WorkoutProgressService workoutProgressService;
    private final DietPlanService dietPlanService;
    private final DietAssignmentService dietAssignmentService;
    private final NutritionGoalService nutritionGoalService;
    private final WeightProgressService weightProgressService;
    private final LoyaltyService loyaltyService;
    private final RewardService rewardService;
    private final NotificationService notificationService;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL)) {
            return;
        }

        seedAdmin();

        List<TrainerResponse> trainers = seedTrainers();
        TrainerResponse shaheed = trainers.get(0);
        TrainerResponse stanley = trainers.get(1);

        List<MemberResponse> members = seedMembers();
        MemberResponse khanyisile = members.get(0);
        MemberResponse bridget = members.get(1);
        MemberResponse tshilidzi = members.get(2);
        MemberResponse tshepiso = members.get(3);

        List<MembershipPlanResponse> plans = seedMembershipPlans();
        MembershipPlanResponse monthlyBasic = plans.get(0);
        MembershipPlanResponse annualPremium = plans.get(1);
        MembershipPlanResponse weeklyTrial = plans.get(2);

        seedMembershipsAndPayments(khanyisile, bridget, tshilidzi, tshepiso, monthlyBasic, annualPremium, weeklyTrial);
        seedTrainerAssignments(khanyisile, bridget, tshepiso, shaheed, stanley);
        seedAttendance(khanyisile, bridget, tshepiso);
        seedWorkout(khanyisile);
        seedDiet(bridget);
        seedLoyalty(khanyisile, tshepiso);
        seedNotifications(khanyisile, bridget);

        log.info("Dev data seeded: admin + {} trainers + {} members + {} membership plans", trainers.size(), members.size(), plans.size());
    }

    private void seedAdmin() {
        User admin = User.builder()
                .firstName("Gym")
                .lastName("Admin")
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.info("Seeded default dev admin -> email: {}, password: {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }

    private List<TrainerResponse> seedTrainers() {
        TrainerResponse shaheed = trainerService.register(new TrainerRegistrationRequest(
                "Shaheed Anover", "Mahomed", "shaheed.mahomed@gymhubsa.co.za", DEFAULT_PASSWORD,
                "0821110001", "Strength & Conditioning", "Certified strength coach with a passion for powerlifting.", 8));

        TrainerResponse stanley = trainerService.register(new TrainerRegistrationRequest(
                "Stanley Kibi", "Mabelebele", "stanley.mabelebele@gymhubsa.co.za", DEFAULT_PASSWORD,
                "0821110002", "Functional Training", "Specialist in functional movement and mobility training.", 6));

        log.info("Seeded trainers -> password for all: {}", DEFAULT_PASSWORD);
        return List.of(shaheed, stanley);
    }

    private List<MemberResponse> seedMembers() {
        MemberResponse khanyisile = memberService.register(new MemberRegistrationRequest(
                "Khanyisile Goodness", "Kunene", "khanyisile.kunene@gymhubsa.co.za", DEFAULT_PASSWORD,
                "0821230001", LocalDate.of(1996, 4, 12), null, "12 Jacaranda St, Pretoria",
                "Thandiwe Kunene", "0831230001"));

        MemberResponse bridget = memberService.register(new MemberRegistrationRequest(
                "Bridget", "Maoko", "bridget.maoko@gymhubsa.co.za", DEFAULT_PASSWORD,
                "0821230002", LocalDate.of(1990, 9, 3), null, "45 Church St, Polokwane",
                "Sipho Maoko", "0831230002"));

        MemberResponse tshilidzi = memberService.register(new MemberRegistrationRequest(
                "Tshilidzi", "Molea", "tshilidzi.molea@gymhubsa.co.za", DEFAULT_PASSWORD,
                "0821230003", LocalDate.of(1988, 1, 22), null, "7 Voortrekker Rd, Bloemfontein",
                "Azwindini Molea", "0831230003"));

        MemberResponse tshepiso = memberService.register(new MemberRegistrationRequest(
                "Tshepiso Makwarela", "Nkgwang", "tshepiso.nkgwang@gymhubsa.co.za", DEFAULT_PASSWORD,
                "0821230004", LocalDate.of(1999, 11, 30), null, "88 Nelson Mandela Dr, Polokwane",
                "Lesego Nkgwang", "0831230004"));

        log.info("Seeded members -> password for all: {}", DEFAULT_PASSWORD);
        return List.of(khanyisile, bridget, tshilidzi, tshepiso);
    }

    private List<MembershipPlanResponse> seedMembershipPlans() {
        MembershipPlanResponse monthlyBasic = membershipPlanService.create(
                new MembershipPlanRequest("Monthly Basic", "Full gym access, billed monthly", 30, new BigDecimal("499.00")));
        MembershipPlanResponse annualPremium = membershipPlanService.create(
                new MembershipPlanRequest("Annual Premium", "Full gym access plus classes, billed annually", 365, new BigDecimal("4999.00")));
        MembershipPlanResponse weeklyTrial = membershipPlanService.create(
                new MembershipPlanRequest("Weekly Trial", "Short trial pass", 6, new BigDecimal("150.00")));

        return List.of(monthlyBasic, annualPremium, weeklyTrial);
    }

    private void seedMembershipsAndPayments(MemberResponse khanyisile, MemberResponse bridget, MemberResponse tshilidzi,
                                             MemberResponse tshepiso, MembershipPlanResponse monthlyBasic,
                                             MembershipPlanResponse annualPremium, MembershipPlanResponse weeklyTrial) {
        var khanyisileMembership = memberMembershipService.assign(new AssignMembershipRequest(khanyisile.id(), monthlyBasic.id()));
        paymentService.recordManualPayment(new RecordPaymentRequest(khanyisile.id(), khanyisileMembership.id(),
                new BigDecimal("499.00"), PaymentMethod.CASH, "Paid in full at front desk"));

        var bridgetMembership = memberMembershipService.assign(new AssignMembershipRequest(bridget.id(), annualPremium.id()));
        // Partial payment, deliberately less than the plan price, so the outstanding-balance report has an example.
        paymentService.recordManualPayment(new RecordPaymentRequest(bridget.id(), bridgetMembership.id(),
                new BigDecimal("2000.00"), PaymentMethod.CARD, "First installment"));

        // Short trial plan so this membership shows up in the expiring-soon / membership-expiry reports immediately.
        memberMembershipService.assign(new AssignMembershipRequest(tshilidzi.id(), weeklyTrial.id()));

        var tshepisoMembership = memberMembershipService.assign(new AssignMembershipRequest(tshepiso.id(), monthlyBasic.id()));
        paymentService.recordManualPayment(new RecordPaymentRequest(tshepiso.id(), tshepisoMembership.id(),
                new BigDecimal("499.00"), PaymentMethod.EFT, "Paid in full via EFT"));
    }

    private void seedTrainerAssignments(MemberResponse khanyisile, MemberResponse bridget, MemberResponse tshepiso,
                                         TrainerResponse shaheed, TrainerResponse stanley) {
        trainerAssignmentService.assign(new AssignTrainerRequest(khanyisile.id(), shaheed.id()));
        trainerAssignmentService.assign(new AssignTrainerRequest(bridget.id(), stanley.id()));
        trainerAssignmentService.assign(new AssignTrainerRequest(tshepiso.id(), shaheed.id()));
    }

    private void seedAttendance(MemberResponse khanyisile, MemberResponse bridget, MemberResponse tshepiso) {
        attendanceService.checkIn(khanyisile.id(), CheckInMethod.MANUAL); // left checked in, to show in "today's attendance"

        attendanceService.checkIn(bridget.id(), CheckInMethod.QR_CODE);
        attendanceService.checkOut(bridget.id());

        attendanceService.checkIn(tshepiso.id(), CheckInMethod.MANUAL);
        attendanceService.checkOut(tshepiso.id());
    }

    private void seedWorkout(MemberResponse khanyisile) {
        var plan = workoutPlanService.create(new WorkoutPlanRequest(
                "Beginner Full Body", "3x/week full-body routine", DifficultyLevel.BEGINNER, 4,
                List.of(
                        new WorkoutExerciseRequest("Squat", 3, 10, 60, 1, null),
                        new WorkoutExerciseRequest("Push-up", 3, 12, 45, 2, null),
                        new WorkoutExerciseRequest("Plank", 3, 1, 30, 3, "Hold for 30 seconds")
                )));

        var assignment = workoutAssignmentService.assign(new AssignWorkoutPlanRequest(khanyisile.id(), plan.id()));
        workoutProgressService.addLog(assignment.id(), new WorkoutProgressLogRequest(LocalDate.now(), 45, 4, "Felt strong"));
    }

    private void seedDiet(MemberResponse bridget) {
        var plan = dietPlanService.create(new DietPlanRequest(
                "High-Protein Cutting", "For fat loss while preserving muscle", 1800, 160, 150, 50,
                List.of(
                        new DietMealRequest("Breakfast", "Oats with egg whites", 450, 1),
                        new DietMealRequest("Lunch", "Grilled chicken with rice and veg", 600, 2),
                        new DietMealRequest("Dinner", "Fish with sweet potato", 550, 3)
                )));

        dietAssignmentService.assign(new AssignDietPlanRequest(bridget.id(), plan.id()));
        nutritionGoalService.setGoal(bridget.id(), new NutritionGoalRequest(
                GoalType.WEIGHT_LOSS, new BigDecimal("78.00"), new BigDecimal("70.00"), LocalDate.now().plusDays(90)));

        weightProgressService.logWeight(bridget.id(), new WeightProgressLogRequest(LocalDate.now().minusDays(14), new BigDecimal("78.00"), "Starting weight"));
        weightProgressService.logWeight(bridget.id(), new WeightProgressLogRequest(LocalDate.now(), new BigDecimal("76.50"), "Week 2 check-in"));
    }

    private void seedLoyalty(MemberResponse khanyisile, MemberResponse tshepiso) {
        loyaltyService.earn(new EarnPointsRequest(khanyisile.id(), 100, "Attendance streak bonus"));
        loyaltyService.earn(new EarnPointsRequest(tshepiso.id(), 50, "Sign-up bonus"));

        var reward = rewardService.create(new RewardRequest("Free Protein Shake", "Redeemable at the front desk", 50, 20));
        loyaltyService.redeem(tshepiso.id(), reward.id());
    }

    private void seedNotifications(MemberResponse khanyisile, MemberResponse bridget) {
        notificationService.sendToMember(khanyisile.id(), NotificationChannel.EMAIL, NotificationType.GENERAL,
                "Welcome to GymHub SA", "Hi Khanyisile, welcome to GymHub SA! We're glad to have you.");
        notificationService.sendToMember(bridget.id(), NotificationChannel.EMAIL, NotificationType.GENERAL,
                "Welcome to GymHub SA", "Hi Bridget, welcome to GymHub SA! We're glad to have you.");
    }
}
