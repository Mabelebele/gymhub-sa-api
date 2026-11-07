# Sample Request Payloads

Standalone example JSON bodies for every request DTO in the API, organized by module
(mirrors `src/main/java/za/co/gymhubsa/api/<module>/dto`). These replace the hardcoded
`@Schema(example = ...)` annotations that used to live on the DTOs — use these files
instead when testing endpoints manually (curl, Postman "raw body from file", etc.) or
as a quick reference for request shapes.

| Module | Files |
|---|---|
| `auth/` | login-request, refresh-token-request, forgot-password-request, reset-password-request |
| `member/` | member-registration-request, member-update-request, member-status-update-request |
| `trainer/` | trainer-registration-request, trainer-update-request, trainer-status-update-request, trainer-schedule-request, assign-trainer-request |
| `membership/` | membership-plan-request, assign-membership-request, change-plan-request |
| `attendance/` | check-in-request, check-out-request, qr-check-in-request |
| `payment/` | record-payment-request, initiate-online-payment-request, webhook-confirm-request |
| `workout/` | workout-plan-request, workout-exercise-request (nested item), assign-workout-plan-request, workout-progress-log-request |
| `diet/` | diet-plan-request, diet-meal-request (nested item), assign-diet-plan-request, nutrition-goal-request, weight-progress-log-request |
| `loyalty/` | earn-points-request, redeem-points-request, member-redeem-request, reward-request |
| `notification/` | send-notification-request, promotional-broadcast-request |

Values like `refreshToken` and `token` are placeholders — substitute real values
returned by `/api/v1/auth/login` or `/api/v1/auth/forgot-password` respectively.
IDs (`memberId`, `trainerId`, etc.) assume a freshly-seeded H2 dev database; adjust
to match whatever records actually exist in your database.
