# GymHub SA API Reference

Backend: Spring Boot 3.5.16, Java 21, base package `za.co.gymhubsa.api`.
This document is derived strictly from the controller, DTO, enum, and security-configuration source
code in this repository, so that a separate Angular frontend (`gymhub-sa-web`) can be implemented
against it without reading the Java source. Real field names and types are used throughout; anything
that could not be determined unambiguously from source is flagged as
`(verify in source: <path>)` rather than guessed.

## Table of Contents

1. [Auth](#auth)
2. [Member](#member)
3. [Trainer](#trainer)
4. [Membership](#membership)
5. [Attendance](#attendance)
6. [Payment](#payment)
7. [Workout](#workout)
8. [Diet](#diet)
9. [Loyalty](#loyalty)
10. [Notification](#notification)
11. [Dashboard](#dashboard)
12. [Reporting](#reporting)
13. [Files](#files)
14. [Enums Reference](#enums-reference)
15. [Conventions](#conventions)

---

## Auth

Source: `za.co.gymhubsa.api.auth.controller.AuthController`, `za.co.gymhubsa.api.auth.dto.*`.
Base path: `/api/v1/auth`. All endpoints in this controller are **public** (listed in
`SecurityConfig.PUBLIC_ENDPOINTS` as `/api/v1/auth/**`) — no `Authorization` header required.

### POST /api/v1/auth/login
Authenticate with email and password, returning an access + refresh token pair.

**Auth:** none (public)

**Request body — `LoginRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `email` | String | Yes (`@NotBlank`) | `@Email` |
| `password` | String | Yes (`@NotBlank`) | — |

**Response data — `LoginResponse`**

| Field | Type | Notes |
|---|---|---|
| `accessToken` | String | JWT access token |
| `refreshToken` | String | JWT refresh token |
| `tokenType` | String | Always `"Bearer"` |
| `expiresIn` | long | Access token lifetime **in seconds** (`accessTokenExpirationMs / 1000`) |
| `user` | `UserSummaryResponse` | See below |

**`UserSummaryResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `firstName` | String |
| `lastName` | String |
| `email` | String |
| `role` | `Role` enum (`ADMIN`, `TRAINER`, `MEMBER`) |

### POST /api/v1/auth/refresh
Exchange a valid refresh token for a new access + refresh token pair.

**Auth:** none (public) — but requires a valid, non-expired **refresh** token (the filter/service
rejects a token that is not a refresh token)

**Request body — `RefreshTokenRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `refreshToken` | String | Yes (`@NotBlank`) | — |

**Response data:** `LoginResponse` (same shape as login)

### POST /api/v1/auth/logout
Clears the server-side `SecurityContextHolder` context. Tokens are stateless (JWT), so there is no
server-side token blacklist/invalidation — **clients must discard the access/refresh tokens
themselves** (e.g. clear local storage) for logout to take effect.

**Auth:** none (public, no request body)

**Response:** `ApiResponse<Void>` with `message: "Logged out successfully"`, `data: null`

### POST /api/v1/auth/forgot-password
Request a password reset token (intended to be emailed/SMSed to the user once notification channels
are wired to it).

**Auth:** none (public)

**Request body — `ForgotPasswordRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `email` | String | Yes (`@NotBlank`) | `@Email` |

**Response:** `ApiResponse<Void>` with a generic message regardless of whether the email exists
(`"If an account with that email exists, a password reset link has been sent"`) — this is intentional
to avoid user enumeration.

### POST /api/v1/auth/reset-password
Reset a password using a valid reset token (see `PasswordResetToken` entity,
`za.co.gymhubsa.api.auth.entity.PasswordResetToken`).

**Auth:** none (public)

**Request body — `ResetPasswordRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `token` | String | Yes (`@NotBlank`) | — |
| `newPassword` | String | Yes (`@NotBlank`) | `@Size(min = 8)` |

**Response:** `ApiResponse<Void>` with `message: "Password reset successfully"`

---

## Member

Source: `za.co.gymhubsa.api.member.controller.MemberController`, `za.co.gymhubsa.api.member.dto.*`.
Base path: `/api/v1/members`.

### POST /api/v1/members
Register a new member — creates both the login `User` account and the `Member` profile in one call.

**Auth:** `ADMIN`

**Request body — `MemberRegistrationRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `firstName` | String | Yes (`@NotBlank`) | — |
| `lastName` | String | Yes (`@NotBlank`) | — |
| `email` | String | Yes (`@NotBlank`) | `@Email` |
| `password` | String | Yes (`@NotBlank`) | `@Size(min = 8)` |
| `phoneNumber` | String | No | — |
| `dateOfBirth` | LocalDate | No | — |
| `gender` | `Gender` enum | No | — |
| `address` | String | No | — |
| `emergencyContactName` | String | No | — |
| `emergencyContactPhone` | String | No | — |

**Response data — `MemberResponse`** (see field table under GET /{id} below)

### GET /api/v1/members
Search/list members with optional keyword and status filters, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Query params:** `keyword` (String, optional), `status` (`MemberStatus` enum, optional),
plus standard `Pageable` params (`page`, `size`, `sort`; default `size=20`)

**Response data:** `Page<MemberResponse>` (see [Conventions → Pagination](#pagination))

### GET /api/v1/members/me
Get the signed-in member's own profile (member resolved by authenticated email).

**Auth:** `MEMBER`

**Response data:** `MemberResponse`

### GET /api/v1/members/{id}
View a member's profile by id.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `id` (Long)

**Response data — `MemberResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `userId` | Long |
| `firstName` | String |
| `lastName` | String |
| `email` | String |
| `phoneNumber` | String |
| `dateOfBirth` | LocalDate |
| `gender` | `Gender` enum |
| `address` | String |
| `emergencyContactName` | String |
| `emergencyContactPhone` | String |
| `profilePhotoUrl` | String |
| `qrCode` | String |
| `status` | `MemberStatus` enum |
| `joinDate` | LocalDate |

### PUT /api/v1/members/{id}
Update a member's profile details (does not change email/password).

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request body — `MemberUpdateRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `firstName` | String | Yes (`@NotBlank`) | — |
| `lastName` | String | Yes (`@NotBlank`) | — |
| `phoneNumber` | String | No | — |
| `dateOfBirth` | LocalDate | No | — |
| `gender` | `Gender` enum | No | — |
| `address` | String | No | — |
| `emergencyContactName` | String | No | — |
| `emergencyContactPhone` | String | No | — |

**Response data:** `MemberResponse`

### PATCH /api/v1/members/{id}/status
Change a member's status (active/inactive/suspended).

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request body — `MemberStatusUpdateRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `status` | `MemberStatus` enum | Yes (`@NotNull`) | — |

**Response data:** `MemberResponse`

### DELETE /api/v1/members/{id}
Deactivate a member (soft delete: disables login, preserves history — does not hard-delete the row).

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Response:** `ApiResponse<Void>` with `message: "Member deactivated successfully"`

### POST /api/v1/members/{id}/photo
Upload/replace a member's profile photo.

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request:** `multipart/form-data` with part name `file` (`MultipartFile`)

**Response data:** `MemberResponse` (with updated `profilePhotoUrl`)

---

## Trainer

Source: `za.co.gymhubsa.api.trainer.controller.*`, `za.co.gymhubsa.api.trainer.dto.*`.
Covers three controllers: `TrainerController` (`/api/v1/trainers`),
`TrainerAssignmentController` (`/api/v1/trainer-assignments`), and
`TrainerScheduleController` (`/api/v1/trainers/{trainerId}/schedule` and `/api/v1/trainers/me/schedule`).

### Trainer Profiles (`/api/v1/trainers`)

#### POST /api/v1/trainers
Register a new trainer (creates both login account and trainer profile).

**Auth:** `ADMIN`

**Request body — `TrainerRegistrationRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `firstName` | String | Yes (`@NotBlank`) | — |
| `lastName` | String | Yes (`@NotBlank`) | — |
| `email` | String | Yes (`@NotBlank`) | `@Email` |
| `password` | String | Yes (`@NotBlank`) | `@Size(min = 8)` |
| `phoneNumber` | String | No | — |
| `specialization` | String | No | — |
| `bio` | String | No | — |
| `yearsOfExperience` | Integer | No | — |

**Response data:** `TrainerResponse` (see field table below)

#### GET /api/v1/trainers
Search/list trainers with optional keyword and status filters, paginated.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER` (any authenticated role)

**Query params:** `keyword` (String, optional), `status` (`TrainerStatus` enum, optional),
plus standard `Pageable` params (default `size=20`)

**Response data:** `Page<TrainerResponse>`

#### GET /api/v1/trainers/me
Get the signed-in trainer's own profile.

**Auth:** `TRAINER`

**Response data:** `TrainerResponse`

#### GET /api/v1/trainers/{id}
View a trainer's profile by id.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Path variables:** `id` (Long)

**Response data — `TrainerResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `userId` | Long |
| `firstName` | String |
| `lastName` | String |
| `email` | String |
| `phoneNumber` | String |
| `specialization` | String |
| `bio` | String |
| `yearsOfExperience` | Integer |
| `profilePhotoUrl` | String |
| `status` | `TrainerStatus` enum |
| `hireDate` | LocalDate |

#### PUT /api/v1/trainers/{id}
Update a trainer's profile details.

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request body — `TrainerUpdateRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `firstName` | String | Yes (`@NotBlank`) | — |
| `lastName` | String | Yes (`@NotBlank`) | — |
| `phoneNumber` | String | No | — |
| `specialization` | String | No | — |
| `bio` | String | No | — |
| `yearsOfExperience` | Integer | No | — |

**Response data:** `TrainerResponse`

#### PATCH /api/v1/trainers/{id}/status
Change a trainer's status (active/inactive).

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request body — `TrainerStatusUpdateRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `status` | `TrainerStatus` enum | Yes (`@NotNull`) | — |

**Response data:** `TrainerResponse`

#### POST /api/v1/trainers/{id}/photo
Upload/replace a trainer's profile photo.

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request:** `multipart/form-data`, part name `file`

**Response data:** `TrainerResponse`

#### GET /api/v1/trainers/{id}/performance
Get a trainer's performance summary (active/total assigned members).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `id` (Long)

**Response data — `TrainerPerformanceResponse`**

| Field | Type |
|---|---|
| `trainerId` | Long |
| `trainerFullName` | String |
| `yearsOfExperience` | Integer |
| `activeMemberCount` | long |
| `totalAssignedEver` | long |

### Trainer Assignments (`/api/v1/trainer-assignments`)

#### POST /api/v1/trainer-assignments/assign
Assign a trainer to a member. Ends any existing active assignment for that member first.

**Auth:** `ADMIN`

**Request body — `AssignTrainerRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `trainerId` | Long | Yes (`@NotNull`) | — |

**Response data — `MemberTrainerAssignmentResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `memberFullName` | String |
| `trainerId` | Long |
| `trainerFullName` | String |
| `assignedDate` | LocalDate |
| `endDate` | LocalDate (nullable) |
| `active` | boolean |

#### GET /api/v1/trainer-assignments/member/{memberId}/current
Get a member's currently assigned trainer.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data:** `MemberTrainerAssignmentResponse`

#### GET /api/v1/trainer-assignments/trainer/{trainerId}/members
List members currently assigned to a trainer (admin/trainer view of an arbitrary trainer id).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `trainerId` (Long)

**Response data:** `List<MemberTrainerAssignmentResponse>`

#### GET /api/v1/trainer-assignments/me/members
Self-service variant: get the signed-in trainer's own currently assigned members
(resolves the trainer id from the authenticated principal, then delegates to the same lookup as
`/trainer/{trainerId}/members`).

**Auth:** `TRAINER`

**Response data:** `List<MemberTrainerAssignmentResponse>`

### Trainer Schedules (`/api/v1/trainers/{trainerId}/schedule`, `/api/v1/trainers/me/schedule`)

#### POST /api/v1/trainers/{trainerId}/schedule
Add a weekly availability slot for a trainer.

**Auth:** `ADMIN`

**Path variables:** `trainerId` (Long)

**Request body — `TrainerScheduleRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `dayOfWeek` | `java.time.DayOfWeek` enum (`MONDAY`…`SUNDAY`) | Yes (`@NotNull`) | — |
| `startTime` | LocalTime | Yes (`@NotNull`) | — |
| `endTime` | LocalTime | Yes (`@NotNull`) | — |

**Response data — `TrainerScheduleResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `trainerId` | Long |
| `dayOfWeek` | `DayOfWeek` |
| `startTime` | LocalTime |
| `endTime` | LocalTime |

#### GET /api/v1/trainers/{trainerId}/schedule
Get a trainer's full weekly availability schedule.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Path variables:** `trainerId` (Long)

**Response data:** `List<TrainerScheduleResponse>`

#### DELETE /api/v1/trainers/{trainerId}/schedule/{scheduleId}
Remove an availability slot from a trainer's schedule.

**Auth:** `ADMIN`

**Path variables:** `trainerId` (Long), `scheduleId` (Long)

**Response:** `ApiResponse<Void>` with `message: "Schedule slot removed successfully"`

#### POST /api/v1/trainers/me/schedule — self-service variant
Add a slot to the signed-in trainer's own schedule (trainer id resolved from principal).

**Auth:** `TRAINER`

**Request body:** `TrainerScheduleRequest` (same as above)

**Response data:** `TrainerScheduleResponse`

#### DELETE /api/v1/trainers/me/schedule/{scheduleId} — self-service variant
Remove a slot from the signed-in trainer's own schedule.

**Auth:** `TRAINER`

**Path variables:** `scheduleId` (Long)

**Response:** `ApiResponse<Void>` with `message: "Schedule slot removed successfully"`

---

## Membership

Source: `za.co.gymhubsa.api.membership.controller.*`, `za.co.gymhubsa.api.membership.dto.*`.
Covers `MembershipPlanController` (`/api/v1/membership-plans`, the plan catalog) and
`MemberMembershipController` (`/api/v1/memberships`, a member's assigned membership instances).

### Membership Plans (`/api/v1/membership-plans`)

#### POST /api/v1/membership-plans
Create a new membership plan.

**Auth:** `ADMIN`

**Request body — `MembershipPlanRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes (`@NotBlank`) | unique at the DB level (`verify in source: za.co.gymhubsa.api.membership.entity.MembershipPlan` — `@Column(unique = true)`) |
| `description` | String | No | — |
| `durationDays` | Integer | Yes (`@NotNull`) | `@Positive` |
| `price` | BigDecimal | Yes (`@NotNull`) | `@DecimalMin("0.0", inclusive = true)` |

**Response data:** `MembershipPlanResponse` (see field table below)

#### GET /api/v1/membership-plans
List membership plans (active-only by default).

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Query params:** `includeInactive` (boolean, default `false`)

**Response data — `List<MembershipPlanResponse>`**

| Field | Type |
|---|---|
| `id` | Long |
| `name` | String |
| `description` | String |
| `durationDays` | Integer |
| `price` | BigDecimal |
| `active` | boolean |

#### GET /api/v1/membership-plans/{id}
Get a membership plan by id.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Path variables:** `id` (Long)

**Response data:** `MembershipPlanResponse`

#### PUT /api/v1/membership-plans/{id}
Update a membership plan.

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request body:** `MembershipPlanRequest` (same as create)

**Response data:** `MembershipPlanResponse`

#### DELETE /api/v1/membership-plans/{id}
Deactivate a membership plan (soft delete — `active=false`; preserves history for any
`MemberMembership` referencing it).

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Response:** `ApiResponse<Void>` with `message: "Membership plan deactivated successfully"`

### Member Memberships (`/api/v1/memberships`)

#### POST /api/v1/memberships/assign
Assign a membership plan to a member. The member must not already have an active membership.

**Auth:** `ADMIN`

**Request body — `AssignMembershipRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `planId` | Long | Yes (`@NotNull`) | — |

**Response data:** `MemberMembershipResponse` (see field table below)

#### POST /api/v1/memberships/{memberId}/renew
Renew a member's membership (extends from the current end date, or starts fresh if already expired).

**Auth:** `ADMIN`

**Path variables:** `memberId` (Long)

**Response data:** `MemberMembershipResponse`

#### POST /api/v1/memberships/{memberId}/change-plan
Upgrade or downgrade a member's active membership to a different plan.

**Auth:** `ADMIN`

**Path variables:** `memberId` (Long)

**Request body — `ChangePlanRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `newPlanId` | Long | Yes (`@NotNull`) | — |

**Response data:** `MemberMembershipResponse`

#### GET /api/v1/memberships/{memberId}/current
Get a member's current active membership.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data — `MemberMembershipResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `memberFullName` | String |
| `planId` | Long |
| `planName` | String |
| `startDate` | LocalDate |
| `endDate` | LocalDate |
| `pricePaid` | BigDecimal |
| `status` | `MembershipStatus` enum |

#### GET /api/v1/memberships/{memberId}/history
Get a member's full membership history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<MemberMembershipResponse>`

#### GET /api/v1/memberships/me/current — self-service variant
Get the signed-in member's own current active membership.

**Auth:** `MEMBER`

**Response data:** `MemberMembershipResponse`

#### GET /api/v1/memberships/me/history — self-service variant
Get the signed-in member's own membership history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<MemberMembershipResponse>`

#### GET /api/v1/memberships/expiring-soon
List active memberships expiring within the given number of days.

**Auth:** `ADMIN`

**Query params:** `days` (int, default `7`)

**Response data:** `List<MemberMembershipResponse>`

---

## Attendance

Source: `za.co.gymhubsa.api.attendance.controller.AttendanceController`,
`za.co.gymhubsa.api.attendance.dto.*`. Base path: `/api/v1/attendance`.

### POST /api/v1/attendance/check-in
Manually check in a member (front desk staff action). Internally recorded with
`method = MANUAL`.

**Auth:** `ADMIN`, `TRAINER`

**Request body — `CheckInRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |

**Response data:** `AttendanceResponse` (see field table below)

### POST /api/v1/attendance/check-in/qr
Check in a member by scanning their QR membership card (the member's `qrCode`, see
`MemberResponse.qrCode`). Internally recorded with `method = QR_CODE`.

**Auth:** `ADMIN`, `TRAINER`

**Request body — `QrCheckInRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `qrCode` | String | Yes (`@NotBlank`) | — |

**Response data:** `AttendanceResponse`

### POST /api/v1/attendance/check-out
Check out a member (closes their currently open check-in).

**Auth:** `ADMIN`, `TRAINER`

**Request body — `CheckOutRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |

**Response data:** `AttendanceResponse`

### GET /api/v1/attendance/member/{memberId}
Get a member's attendance history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data — `Page<AttendanceResponse>`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `memberFullName` | String |
| `checkInTime` | Instant |
| `checkOutTime` | Instant (nullable — null while still checked in) |
| `method` | `CheckInMethod` enum |
| `durationMinutes` | Long (nullable; computed only once checked out) |

### GET /api/v1/attendance/me — self-service variant
Get the signed-in member's own attendance history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<AttendanceResponse>`

### GET /api/v1/attendance/daily-report
Get the daily attendance report (defaults to today if `date` is omitted).

**Auth:** `ADMIN`, `TRAINER`

**Query params:** `date` (LocalDate, ISO `yyyy-MM-dd`, optional — defaults to current date)

**Response data — `DailyAttendanceReportResponse`**

| Field | Type |
|---|---|
| `date` | LocalDate |
| `totalCheckIns` | long |
| `records` | `List<AttendanceResponse>` |

---

## Payment

Source: `za.co.gymhubsa.api.payment.controller.*`, `za.co.gymhubsa.api.payment.dto.*`.
Covers `PaymentController` (`/api/v1/payments`, authenticated) and
`PaymentWebhookController` (`/api/v1/payments/webhook`, shared-secret authenticated — public per
`SecurityConfig`).

### POST /api/v1/payments
Record a manual payment (cash/card/EFT) taken at the front desk.

**Auth:** `ADMIN`

**Request body — `RecordPaymentRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `membershipId` | Long | No | — |
| `amount` | BigDecimal | Yes (`@NotNull`) | `@DecimalMin("0.01")` |
| `method` | `PaymentMethod` enum | Yes (`@NotNull`) | — |
| `notes` | String | No | — |

**Response data:** `PaymentResponse` (see field table below)

### POST /api/v1/payments/online/initiate
Initiate an online payment for the signed-in member; returns a redirect URL to the (mocked) payment
gateway. See `za.co.gymhubsa.api.payment.gateway.MockPaymentGatewayService`.

**Auth:** `MEMBER`

**Request body — `InitiateOnlinePaymentRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `membershipId` | Long | No | — |
| `amount` | BigDecimal | Yes (`@NotNull`) | `@DecimalMin("0.01")` |

**Response data — `OnlinePaymentInitiationResponse`**

| Field | Type |
|---|---|
| `paymentId` | Long |
| `gatewayReference` | String |
| `redirectUrl` | String |

### GET /api/v1/payments/{id}/receipt
Get a payment's receipt (only meaningful once the payment is `COMPLETED`).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `id` (Long — the payment id)

**Response data — `ReceiptResponse`**

| Field | Type |
|---|---|
| `receiptNumber` | String |
| `memberId` | Long |
| `memberFullName` | String |
| `memberEmail` | String |
| `planName` | String (nullable — null if payment is not tied to a membership) |
| `amount` | BigDecimal |
| `method` | `PaymentMethod` enum |
| `paidAt` | Instant |

### GET /api/v1/payments/member/{memberId}
Get a member's payment history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data — `Page<PaymentResponse>`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `memberFullName` | String |
| `membershipId` | Long (nullable) |
| `amount` | BigDecimal |
| `method` | `PaymentMethod` enum |
| `status` | `PaymentStatus` enum |
| `receiptNumber` | String |
| `gatewayReference` | String (nullable — only for online payments) |
| `paidAt` | Instant (nullable until completed) |
| `createdAt` | Instant |

### GET /api/v1/payments/me — self-service variant
Get the signed-in member's own payment history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<PaymentResponse>`

### GET /api/v1/payments/member/{memberId}/outstanding-balance
Get a member's outstanding balance against their active membership.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data — `OutstandingBalanceResponse`**

| Field | Type |
|---|---|
| `memberId` | Long |
| `memberFullName` | String |
| `expectedAmount` | BigDecimal |
| `paidAmount` | BigDecimal |
| `outstandingAmount` | BigDecimal |

### GET /api/v1/payments/outstanding
List all members with an outstanding balance.

**Auth:** `ADMIN`

**Response data:** `List<OutstandingBalanceResponse>`

### GET /api/v1/payments/revenue
Get total revenue and payment count for a date range (defaults to the current calendar month).

**Auth:** `ADMIN`

**Query params:** `from` (LocalDate, ISO, optional — defaults to the 1st of the current month),
`to` (LocalDate, ISO, optional — defaults to today)

**Response data — `RevenueReportResponse`**

| Field | Type |
|---|---|
| `from` | LocalDate |
| `to` | LocalDate |
| `totalRevenue` | BigDecimal |
| `paymentCount` | long |

### POST /api/v1/payments/webhook/confirm
Simulated payment-gateway server-to-server callback that confirms or fails a previously initiated
online payment. **Not JWT-authenticated** — authenticated instead via a shared-secret header.

**Auth:** none (public per `SecurityConfig`); requires header `X-Webhook-Secret` equal to
`app.payment-gateway.webhook-secret` (env `PAYMENT_WEBHOOK_SECRET`), otherwise the service throws
`UnauthorizedException` → HTTP 401 `"Invalid credentials"`.

**Request headers:** `X-Webhook-Secret: <shared secret>`

**Request body — `WebhookConfirmRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `gatewayReference` | String | Yes (`@NotBlank`) | — |
| `success` | Boolean | Yes (`@NotNull`) | — |

**Response data:** `PaymentResponse`

---

## Workout

Source: `za.co.gymhubsa.api.workout.controller.*`, `za.co.gymhubsa.api.workout.dto.*`.
Covers `WorkoutPlanController` (`/api/v1/workout-plans`, the plan template catalog),
`WorkoutAssignmentController` (`/api/v1/workouts`, assigning a plan to a member), and
`WorkoutProgressController` (`/api/v1/workouts`, logging progress against an assignment).

### Workout Plan Templates (`/api/v1/workout-plans`)

#### POST /api/v1/workout-plans
Create a workout plan template with its exercises.

**Auth:** `ADMIN`, `TRAINER`

**Request body — `WorkoutPlanRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes (`@NotBlank`) | — |
| `description` | String | No | — |
| `difficultyLevel` | `DifficultyLevel` enum | No | — |
| `durationWeeks` | Integer | No | — |
| `exercises` | `List<WorkoutExerciseRequest>` | Yes (`@NotEmpty`) | each element validated (`@Valid`) |

**`WorkoutExerciseRequest`** (nested, per exercise)

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes (`@NotBlank`) | — |
| `sets` | Integer | Yes (`@NotNull`) | `@Positive` |
| `reps` | Integer | Yes (`@NotNull`) | `@Positive` |
| `restSeconds` | Integer | No | — |
| `sortOrder` | Integer | Yes (`@NotNull`) | — |
| `notes` | String | No | — |

**Response data:** `WorkoutPlanResponse` (full variant, with exercises — see below)

#### GET /api/v1/workout-plans
List workout plan templates (active-only by default). Uses the **summary** variant
(`exercises: []`) for list views.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Query params:** `includeInactive` (boolean, default `false`)

**Response data:** `List<WorkoutPlanResponse>` (summary variant)

#### GET /api/v1/workout-plans/{id}
Get a workout plan template with its full exercise list.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Path variables:** `id` (Long)

**Response data — `WorkoutPlanResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `name` | String |
| `description` | String |
| `difficultyLevel` | `DifficultyLevel` enum |
| `durationWeeks` | Integer |
| `active` | boolean |
| `exercises` | `List<WorkoutExerciseResponse>` (`[]` for the list/summary variant) |

**`WorkoutExerciseResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `name` | String |
| `sets` | Integer |
| `reps` | Integer |
| `restSeconds` | Integer |
| `sortOrder` | Integer |
| `notes` | String |

#### PUT /api/v1/workout-plans/{id}
Update a workout plan template and **replace its entire exercise list**.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `id` (Long)

**Request body:** `WorkoutPlanRequest` (same as create)

**Response data:** `WorkoutPlanResponse` (full variant)

#### DELETE /api/v1/workout-plans/{id}
Deactivate a workout plan template (soft delete; preserves assignment history).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `id` (Long)

**Response:** `ApiResponse<Void>` with `message: "Workout plan deactivated successfully"`

### Workout Assignments (`/api/v1/workouts`)

#### POST /api/v1/workouts/assign
Assign a workout plan to a member (replaces/cancels their current active plan, if any).

**Auth:** `ADMIN`, `TRAINER`

**Request body — `AssignWorkoutPlanRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `workoutPlanId` | Long | Yes (`@NotNull`) | — |

**Response data:** `MemberWorkoutPlanResponse` (see field table below)

#### PATCH /api/v1/workouts/{memberWorkoutPlanId}/complete
Mark a member's workout plan assignment as completed.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberWorkoutPlanId` (Long)

**Response data:** `MemberWorkoutPlanResponse`

#### GET /api/v1/workouts/member/{memberId}/current
Get a member's currently active workout plan.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data — `MemberWorkoutPlanResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `memberFullName` | String |
| `workoutPlanId` | Long |
| `workoutPlanName` | String |
| `assignedDate` | LocalDate |
| `status` | `WorkoutAssignmentStatus` enum |

#### GET /api/v1/workouts/member/{memberId}/history
Get a member's workout plan assignment history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<MemberWorkoutPlanResponse>`

#### GET /api/v1/workouts/me/current — self-service variant
Get the signed-in member's own currently active workout plan.

**Auth:** `MEMBER`

**Response data:** `MemberWorkoutPlanResponse`

#### GET /api/v1/workouts/me/history — self-service variant
Get the signed-in member's own workout plan history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<MemberWorkoutPlanResponse>`

### Workout Progress (`/api/v1/workouts`)

#### POST /api/v1/workouts/{memberWorkoutPlanId}/progress
Log progress for a member's workout plan assignment (e.g. a trainer logging a session on the
member's behalf).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberWorkoutPlanId` (Long)

**Request body — `WorkoutProgressLogRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `logDate` | LocalDate | No | — |
| `durationMinutes` | Integer | No | — |
| `rating` | Integer | No | — |
| `notes` | String | No | — |

**Response data — `WorkoutProgressLogResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberWorkoutPlanId` | Long |
| `logDate` | LocalDate |
| `durationMinutes` | Integer |
| `rating` | Integer |
| `notes` | String |

#### GET /api/v1/workouts/{memberWorkoutPlanId}/progress
Get progress logs for a member's workout plan assignment, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberWorkoutPlanId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<WorkoutProgressLogResponse>`

#### POST /api/v1/workouts/me/progress — self-service variant
Log progress against the signed-in member's own active workout plan.

**Auth:** `MEMBER`

**Request body:** `WorkoutProgressLogRequest` (same as above)

**Response data:** `WorkoutProgressLogResponse`

#### GET /api/v1/workouts/me/progress — self-service variant
Get the signed-in member's own workout progress logs, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<WorkoutProgressLogResponse>`

---

## Diet

Source: `za.co.gymhubsa.api.diet.controller.*`, `za.co.gymhubsa.api.diet.dto.*`.
Covers four controllers: `DietPlanController` (`/api/v1/diet-plans`, the plan template catalog),
`DietAssignmentController` (`/api/v1/diets`, assigning a plan to a member),
`NutritionGoalController` (`/api/v1/nutrition-goals`), and
`WeightProgressController` (`/api/v1/weight-progress`).

### Diet Plan Templates (`/api/v1/diet-plans`)

#### POST /api/v1/diet-plans
Create a diet plan template with its meals.

**Auth:** `ADMIN`, `TRAINER`

**Request body — `DietPlanRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes (`@NotBlank`) | — |
| `description` | String | No | — |
| `dailyCalorieTarget` | Integer | No | — |
| `proteinGrams` | Integer | No | — |
| `carbsGrams` | Integer | No | — |
| `fatGrams` | Integer | No | — |
| `meals` | `List<DietMealRequest>` | Yes (`@NotEmpty`) | each element validated (`@Valid`) |

**`DietMealRequest`** (nested, per meal)

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes (`@NotBlank`) | — |
| `description` | String | No | — |
| `calories` | Integer | No | — |
| `sortOrder` | Integer | Yes (`@NotNull`) | — |

**Response data:** `DietPlanResponse` (full variant, with meals — see below)

#### GET /api/v1/diet-plans
List diet plan templates (active-only by default). Uses the **summary** variant
(`meals: []`) for list views.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Query params:** `includeInactive` (boolean, default `false`)

**Response data:** `List<DietPlanResponse>` (summary variant)

#### GET /api/v1/diet-plans/{id}
Get a diet plan template with its full meal list.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Path variables:** `id` (Long)

**Response data — `DietPlanResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `name` | String |
| `description` | String |
| `dailyCalorieTarget` | Integer |
| `proteinGrams` | Integer |
| `carbsGrams` | Integer |
| `fatGrams` | Integer |
| `active` | boolean |
| `meals` | `List<DietMealResponse>` (`[]` for the list/summary variant) |

**`DietMealResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `name` | String |
| `description` | String |
| `calories` | Integer |
| `sortOrder` | Integer |

#### PUT /api/v1/diet-plans/{id}
Update a diet plan template and **replace its entire meal list**.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `id` (Long)

**Request body:** `DietPlanRequest` (same as create)

**Response data:** `DietPlanResponse` (full variant)

#### DELETE /api/v1/diet-plans/{id}
Deactivate a diet plan template (soft delete; preserves assignment history).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `id` (Long)

**Response:** `ApiResponse<Void>` with `message: "Diet plan deactivated successfully"`

### Diet Assignments (`/api/v1/diets`)

#### POST /api/v1/diets/assign
Assign a diet plan to a member (replaces/cancels their current active plan, if any).

**Auth:** `ADMIN`, `TRAINER`

**Request body — `AssignDietPlanRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `dietPlanId` | Long | Yes (`@NotNull`) | — |

**Response data:** `MemberDietPlanResponse` (see field table below)

#### GET /api/v1/diets/member/{memberId}/current
Get a member's currently active diet plan.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data — `MemberDietPlanResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `memberFullName` | String |
| `dietPlanId` | Long |
| `dietPlanName` | String |
| `assignedDate` | LocalDate |
| `status` | `DietAssignmentStatus` enum |

#### GET /api/v1/diets/member/{memberId}/history
Get a member's diet plan assignment history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<MemberDietPlanResponse>`

#### GET /api/v1/diets/me/current — self-service variant
Get the signed-in member's own currently active diet plan.

**Auth:** `MEMBER`

**Response data:** `MemberDietPlanResponse`

#### GET /api/v1/diets/me/history — self-service variant
Get the signed-in member's own diet plan history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<MemberDietPlanResponse>`

### Nutrition Goals (`/api/v1/nutrition-goals`)

#### POST /api/v1/nutrition-goals/member/{memberId}
Set a nutrition goal for a member (supersedes their current active goal, if any).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Request body — `NutritionGoalRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `goalType` | `GoalType` enum | Yes (`@NotNull`) | — |
| `startWeightKg` | BigDecimal | No | — |
| `targetWeightKg` | BigDecimal | No | — |
| `targetDate` | LocalDate | No | — |

**Response data:** `NutritionGoalResponse` (see field table below)

#### PATCH /api/v1/nutrition-goals/{goalId}/achieved
Mark a nutrition goal as achieved.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `goalId` (Long)

**Response data:** `NutritionGoalResponse`

#### GET /api/v1/nutrition-goals/member/{memberId}/current
Get a member's current active nutrition goal.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data — `NutritionGoalResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `goalType` | `GoalType` enum |
| `startWeightKg` | BigDecimal |
| `targetWeightKg` | BigDecimal |
| `targetDate` | LocalDate |
| `status` | `NutritionGoalStatus` enum |

#### GET /api/v1/nutrition-goals/member/{memberId}/history
Get a member's nutrition goal history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<NutritionGoalResponse>`

#### GET /api/v1/nutrition-goals/me/current — self-service variant
Get the signed-in member's own current nutrition goal.

**Auth:** `MEMBER`

**Response data:** `NutritionGoalResponse`

#### GET /api/v1/nutrition-goals/me/history — self-service variant
Get the signed-in member's own nutrition goal history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<NutritionGoalResponse>`

### Weight Progress (`/api/v1/weight-progress`)

#### POST /api/v1/weight-progress/member/{memberId}
Log a weight entry for a member (e.g. a trainer recording a weigh-in).

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Request body — `WeightProgressLogRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `logDate` | LocalDate | No | — |
| `weightKg` | BigDecimal | Yes (`@NotNull`) | — |
| `notes` | String | No | — |

**Response data — `WeightProgressLogResponse`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `logDate` | LocalDate |
| `weightKg` | BigDecimal |
| `notes` | String |

#### GET /api/v1/weight-progress/member/{memberId}/history
Get a member's weight history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<WeightProgressLogResponse>`

#### GET /api/v1/weight-progress/member/{memberId}/latest
Get a member's most recently logged weight entry.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data:** `WeightProgressLogResponse`

#### POST /api/v1/weight-progress/me — self-service variant
Log a weight entry for the signed-in member.

**Auth:** `MEMBER`

**Request body:** `WeightProgressLogRequest` (same as above)

**Response data:** `WeightProgressLogResponse`

#### GET /api/v1/weight-progress/me/history — self-service variant
Get the signed-in member's own weight history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<WeightProgressLogResponse>`

#### GET /api/v1/weight-progress/me/latest — self-service variant
Get the signed-in member's own latest logged weight entry.

**Auth:** `MEMBER`

**Response data:** `WeightProgressLogResponse`

---

## Loyalty

Source: `za.co.gymhubsa.api.loyalty.controller.*`, `za.co.gymhubsa.api.loyalty.dto.*`.
Covers `LoyaltyController` (`/api/v1/loyalty`, points earning/redeeming/balance) and
`RewardController` (`/api/v1/rewards`, the redeemable reward catalog).

### Loyalty Points (`/api/v1/loyalty`)

#### POST /api/v1/loyalty/earn
Award loyalty points to a member (e.g. attendance streak, promotion).

**Auth:** `ADMIN`, `TRAINER`

**Request body — `EarnPointsRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `points` | Integer | Yes (`@NotNull`) | `@Positive` |
| `reason` | String | Yes (`@NotBlank`) | — |

**Response data:** `LoyaltyTransactionResponse` (see field table below)

#### POST /api/v1/loyalty/redeem
Redeem a member's points for a reward (e.g. front desk fulfilling a redemption on the member's
behalf).

**Auth:** `ADMIN`, `TRAINER`

**Request body — `RedeemPointsRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `rewardId` | Long | Yes (`@NotNull`) | — |

**Response data:** `LoyaltyTransactionResponse`

#### GET /api/v1/loyalty/member/{memberId}/balance
Get a member's loyalty points balance.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Response data — `LoyaltyAccountResponse`**

| Field | Type |
|---|---|
| `memberId` | Long |
| `pointsBalance` | Integer |

#### GET /api/v1/loyalty/member/{memberId}/history
Get a member's loyalty points transaction history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data — `Page<LoyaltyTransactionResponse>`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long |
| `type` | `LoyaltyTransactionType` enum |
| `points` | Integer |
| `reason` | String |
| `balanceAfter` | Integer |
| `rewardId` | Long (nullable — only set for `REDEEMED` transactions) |
| `rewardName` | String (nullable) |
| `createdAt` | Instant |

#### GET /api/v1/loyalty/me/balance — self-service variant
Get the signed-in member's own points balance.

**Auth:** `MEMBER`

**Response data:** `LoyaltyAccountResponse`

#### GET /api/v1/loyalty/me/history — self-service variant
Get the signed-in member's own points transaction history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<LoyaltyTransactionResponse>`

#### POST /api/v1/loyalty/me/redeem — self-service variant
Redeem the signed-in member's own points for a reward.

**Auth:** `MEMBER`

**Request body — `MemberRedeemRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `rewardId` | Long | Yes (`@NotNull`) | — |

**Response data:** `LoyaltyTransactionResponse`

### Reward Catalog (`/api/v1/rewards`)

#### POST /api/v1/rewards
Create a new reward.

**Auth:** `ADMIN`

**Request body — `RewardRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes (`@NotBlank`) | — |
| `description` | String | No | — |
| `pointsCost` | Integer | Yes (`@NotNull`) | `@Positive` |
| `stockQuantity` | Integer | No | `@PositiveOrZero` |

**Response data:** `RewardResponse` (see field table below)

#### GET /api/v1/rewards
List rewards (active-only by default).

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Query params:** `includeInactive` (boolean, default `false`)

**Response data — `List<RewardResponse>`**

| Field | Type |
|---|---|
| `id` | Long |
| `name` | String |
| `description` | String |
| `pointsCost` | Integer |
| `stockQuantity` | Integer |
| `active` | boolean |

#### GET /api/v1/rewards/{id}
Get a reward by id.

**Auth:** `ADMIN`, `TRAINER`, `MEMBER`

**Path variables:** `id` (Long)

**Response data:** `RewardResponse`

#### PUT /api/v1/rewards/{id}
Update a reward.

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Request body:** `RewardRequest` (same as create)

**Response data:** `RewardResponse`

#### DELETE /api/v1/rewards/{id}
Deactivate a reward (soft delete; preserves redemption history).

**Auth:** `ADMIN`

**Path variables:** `id` (Long)

**Response:** `ApiResponse<Void>` with `message: "Reward deactivated successfully"`

---

## Notification

Source: `za.co.gymhubsa.api.notification.controller.NotificationController`,
`za.co.gymhubsa.api.notification.dto.*`. Base path: `/api/v1/notifications`.

### POST /api/v1/notifications/send
Send a notification to a member (membership expiry, payment reminder, appointment reminder,
promotional, password reset, or general).

**Auth:** `ADMIN`, `TRAINER`

**Request body — `SendNotificationRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `memberId` | Long | Yes (`@NotNull`) | — |
| `channel` | `NotificationChannel` enum | Yes (`@NotNull`) | — |
| `type` | `NotificationType` enum | Yes (`@NotNull`) | — |
| `subject` | String | No | — |
| `message` | String | Yes (`@NotBlank`) | — |

**Response data:** `NotificationResponse` (see field table below)

### POST /api/v1/notifications/promotional/broadcast
Broadcast a promotional notification to specific members, or to all active members if
`memberIds` is omitted/empty.

**Auth:** `ADMIN`

**Request body — `PromotionalBroadcastRequest`**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `channel` | `NotificationChannel` enum | Yes (`@NotNull`) | — |
| `subject` | String | No | — |
| `message` | String | Yes (`@NotBlank`) | — |
| `memberIds` | `List<Long>` | No | if null/empty, broadcasts to all active members |

**Response data:** `List<NotificationResponse>`

### GET /api/v1/notifications/member/{memberId}/history
Get a member's notification history, paginated.

**Auth:** `ADMIN`, `TRAINER`

**Path variables:** `memberId` (Long)

**Query params:** standard `Pageable` (default `size=20`)

**Response data — `Page<NotificationResponse>`**

| Field | Type |
|---|---|
| `id` | Long |
| `memberId` | Long (nullable — null for non-member-targeted notifications) |
| `recipient` | String |
| `channel` | `NotificationChannel` enum |
| `type` | `NotificationType` enum |
| `subject` | String |
| `message` | String |
| `status` | `NotificationStatus` enum |
| `sentAt` | Instant (nullable until sent) |
| `errorMessage` | String (nullable — populated if `status = FAILED`) |

### GET /api/v1/notifications/me/history — self-service variant
Get the signed-in member's own notification history, paginated.

**Auth:** `MEMBER`

**Query params:** standard `Pageable` (default `size=20`)

**Response data:** `Page<NotificationResponse>`

---

## Dashboard

Source: `za.co.gymhubsa.api.dashboard.controller.DashboardController`,
`za.co.gymhubsa.api.dashboard.dto.DashboardSummaryResponse`. Base path: `/api/v1/dashboard`.

### GET /api/v1/dashboard/summary
Aggregated admin dashboard: totals, today's attendance, monthly revenue, memberships expiring soon,
recent payments, and recent notifications/alerts.

**Auth:** `ADMIN`

**Response data — `DashboardSummaryResponse`**

| Field | Type |
|---|---|
| `totalMembers` | long |
| `activeMemberships` | long |
| `todaysAttendanceCount` | long |
| `monthlyRevenue` | BigDecimal |
| `monthlyPaymentCount` | long |
| `membershipsExpiringSoon` | `List<MemberMembershipResponse>` (see [Membership](#membership)) |
| `recentPayments` | `List<PaymentResponse>` (see [Payment](#payment)) |
| `recentNotifications` | `List<NotificationResponse>` (see [Notification](#notification)) |

---

## Reporting

Source: `za.co.gymhubsa.api.reporting.controller.ReportingController`,
`za.co.gymhubsa.api.reporting.dto.*`. Base path: `/api/v1/reports`.
**The entire controller is class-level `@PreAuthorize("hasRole('ADMIN')")`** — every endpoint below
requires the `ADMIN` role.

### GET /api/v1/reports/members
Member report: status breakdown plus a searchable/paginated member list.

**Auth:** `ADMIN`

**Query params:** `keyword` (String, optional), `status` (`MemberStatus` enum, optional),
standard `Pageable` (default `size=20`)

**Response data — `MemberReportResponse`**

| Field | Type |
|---|---|
| `totalMembers` | long |
| `activeCount` | long |
| `inactiveCount` | long |
| `suspendedCount` | long |
| `members` | `Page<MemberResponse>` (see [Member](#member)) |

### GET /api/v1/reports/memberships
Membership report: status breakdown plus active-member distribution per plan.

**Auth:** `ADMIN`

**Response data — `MembershipReportResponse`**

| Field | Type |
|---|---|
| `activeCount` | long |
| `expiredCount` | long |
| `cancelledCount` | long |
| `planDistribution` | `List<PlanDistributionEntry>` |

**`PlanDistributionEntry`**

| Field | Type |
|---|---|
| `planId` | Long |
| `planName` | String |
| `activeMemberCount` | long |

### GET /api/v1/reports/attendance
Attendance report for a given date (defaults to today). Returns the same shape as
`/api/v1/attendance/daily-report`.

**Auth:** `ADMIN`

**Query params:** `date` (LocalDate, ISO, optional)

**Response data:** `DailyAttendanceReportResponse` (see [Attendance](#attendance))

### GET /api/v1/reports/payments
Payment report for a date range: totals plus breakdown by payment method.

**Auth:** `ADMIN`

**Query params:** `from` (LocalDate, ISO, **required**), `to` (LocalDate, ISO, **required**)

**Response data — `PaymentReportResponse`**

| Field | Type |
|---|---|
| `from` | LocalDate |
| `to` | LocalDate |
| `totalAmount` | BigDecimal |
| `totalCount` | long |
| `byMethod` | `List<PaymentMethodBreakdownEntry>` |

**`PaymentMethodBreakdownEntry`**

| Field | Type |
|---|---|
| `method` | `PaymentMethod` enum |
| `totalAmount` | BigDecimal |
| `count` | long |

### GET /api/v1/reports/revenue
Revenue report for a date range (defaults to current month). Returns the same shape as
`/api/v1/payments/revenue`.

**Auth:** `ADMIN`

**Query params:** `from` (LocalDate, ISO, optional), `to` (LocalDate, ISO, optional)

**Response data:** `RevenueReportResponse` (see [Payment](#payment))

### GET /api/v1/reports/trainers
Trainer report: performance summary for every active trainer.

**Auth:** `ADMIN`

**Response data — `TrainerReportResponse`**

| Field | Type |
|---|---|
| `trainers` | `List<TrainerPerformanceResponse>` (see [Trainer](#trainer)) |

### GET /api/v1/reports/membership-expiry
Membership expiry report: active memberships expiring within N days.

**Auth:** `ADMIN`

**Query params:** `days` (Integer, optional — service-level default applies when omitted;
`verify in source: za.co.gymhubsa.api.reporting.service.ReportingService#getMembershipExpiryReport`,
controller Javadoc states default 30)

**Response data:** `List<MemberMembershipResponse>` (see [Membership](#membership))

### GET /api/v1/reports/loyalty
Loyalty report: total points earned/redeemed and top point earners.

**Auth:** `ADMIN`

**Response data — `LoyaltyReportResponse`**

| Field | Type |
|---|---|
| `totalPointsEarned` | long |
| `totalPointsRedeemed` | long |
| `topEarners` | `List<LoyaltyTopEarnerEntry>` |

**`LoyaltyTopEarnerEntry`**

| Field | Type |
|---|---|
| `memberId` | Long |
| `memberFullName` | String |
| `pointsBalance` | Integer |

---

## Files

Source: `za.co.gymhubsa.api.common.storage.FileController`. Base path: `/api/v1/files`.
Serves previously uploaded files (profile photos, etc.) by the relative path returned from
`FileStorageService.store(...)` (e.g. the `profilePhotoUrl` on `MemberResponse`/`TrainerResponse`).

### GET /api/v1/files/{subdirectory}/{filename}
Download a previously uploaded file.

**Auth:** any authenticated user (no `@PreAuthorize` on this controller, but the endpoint is not in
`SecurityConfig.PUBLIC_ENDPOINTS`, so Spring Security's default `anyRequest().authenticated()` rule
applies — a valid JWT is required)

**Path variables:** `subdirectory` (String, e.g. `"members"`, `"trainers"`), `filename` (String)

**Response:** raw file bytes (`Resource`), not wrapped in `ApiResponse`

Local storage root is configured via `app.file-storage.base-dir` (env `FILE_STORAGE_BASE_DIR`,
default `uploads`) — see `za.co.gymhubsa.api.common.storage.FileStorageProperties` and
`LocalFileStorageService`.

---

## Enums Reference

Every enum type referenced by any request/response DTO in this API, with exact constant names
(JSON values match the Java constant names verbatim, e.g. `"ADMIN"`, `"ACTIVE"`).

| Enum | Package | Values |
|---|---|---|
| `Role` | `za.co.gymhubsa.api.auth.entity` | `ADMIN`, `TRAINER`, `MEMBER` |
| `Gender` | `za.co.gymhubsa.api.member.entity` | `MALE`, `FEMALE`, `OTHER` |
| `MemberStatus` | `za.co.gymhubsa.api.member.entity` | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `TrainerStatus` | `za.co.gymhubsa.api.trainer.entity` | `ACTIVE`, `INACTIVE` |
| `MembershipStatus` | `za.co.gymhubsa.api.membership.entity` | `ACTIVE`, `EXPIRED`, `CANCELLED` |
| `CheckInMethod` | `za.co.gymhubsa.api.attendance.entity` | `MANUAL`, `QR_CODE` |
| `PaymentMethod` | `za.co.gymhubsa.api.payment.entity` | `CASH`, `CARD`, `EFT`, `ONLINE` |
| `PaymentStatus` | `za.co.gymhubsa.api.payment.entity` | `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED` |
| `DifficultyLevel` | `za.co.gymhubsa.api.workout.entity` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` |
| `WorkoutAssignmentStatus` | `za.co.gymhubsa.api.workout.entity` | `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `DietAssignmentStatus` | `za.co.gymhubsa.api.diet.entity` | `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `GoalType` | `za.co.gymhubsa.api.diet.entity` | `WEIGHT_LOSS`, `WEIGHT_GAIN`, `MUSCLE_GAIN`, `MAINTENANCE` |
| `NutritionGoalStatus` | `za.co.gymhubsa.api.diet.entity` | `ACTIVE`, `ACHIEVED`, `CANCELLED` |
| `LoyaltyTransactionType` | `za.co.gymhubsa.api.loyalty.entity` | `EARNED`, `REDEEMED`, `ADJUSTED` |
| `NotificationChannel` | `za.co.gymhubsa.api.notification.entity` | `EMAIL`, `SMS`, `WHATSAPP`, `PUSH` |
| `NotificationStatus` | `za.co.gymhubsa.api.notification.entity` | `PENDING`, `SENT`, `FAILED` |
| `NotificationType` | `za.co.gymhubsa.api.notification.entity` | `MEMBERSHIP_EXPIRY`, `PAYMENT_REMINDER`, `APPOINTMENT_REMINDER`, `PROMOTIONAL`, `PASSWORD_RESET`, `GENERAL` |
| `DayOfWeek` | `java.time` (JDK standard enum, used by `TrainerScheduleRequest`/`Response`) | `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY` |

---

## Conventions

### Base URL & Environments

- **API prefix:** all endpoints are under `/api/v1` (see each `@RequestMapping` above).
- **Local dev port:** `server.port` defaults to **`8081`** (`application.yml`: `server.port:
  ${SERVER_PORT:8081}`), overridable via the `SERVER_PORT` environment variable. This differs from
  the commonly-assumed Spring Boot default of 8080 — confirm the actual port your running instance
  is bound to before pointing the Angular app at it.
- **Swagger / OpenAPI:** configured via springdoc (`application.yml`):
  - Swagger UI: `/swagger-ui.html` (`springdoc.swagger-ui.path`), which redirects to the standard
    springdoc resource path `/swagger-ui/index.html`.
  - OpenAPI JSON: `/v3/api-docs` (`springdoc.api-docs.path`).
  - Both paths are listed in `SecurityConfig.PUBLIC_ENDPOINTS` (`/swagger-ui/**`,
    `/swagger-ui.html`, `/v3/api-docs/**`) so they are reachable without a token. The
    "Authorize" button in Swagger UI uses the `bearerAuth` HTTP-bearer scheme configured in
    `OpenApiConfig`.
- **Actuator:** `/actuator/health` is public; only `health` and `info` are exposed
  (`management.endpoints.web.exposure.include`).
- **H2 console (dev profile only):** `/h2-console/**` is public, path `/h2-console`.
- **CORS:** configured in `za.co.gymhubsa.api.config.CorsProperties`
  (`@ConfigurationProperties(prefix = "app.cors")`) and applied in `SecurityConfig`. The property is
  `app.cors.allowed-origins`, bound from env var **`CORS_ALLOWED_ORIGINS`**, default
  `http://localhost:4200` in both the base and `dev` profiles. **Add the Angular dev server's
  origin (e.g. `http://localhost:4200`) to this list — it is a comma-separated list if multiple
  origins are needed** (`List<String>` binding). Allowed methods: `GET, POST, PUT, PATCH, DELETE,
  OPTIONS`; allowed headers: `*`; credentials: allowed.

### Authentication

- **Scheme:** JWT bearer token. Send `Authorization: Bearer <accessToken>` on every request to a
  protected endpoint (`za.co.gymhubsa.api.security.jwt.JwtAuthenticationFilter`, header name
  `Authorization`, prefix `"Bearer "` — case-sensitive, with a trailing space).
  - The filter only accepts tokens for which `jwtService.isAccessToken(token)` is true — a refresh
    token will **not** work as a bearer token on normal endpoints; it may only be POSTed to
    `/api/v1/auth/refresh`.
  - On an invalid/expired token the filter silently clears the security context and lets the
    request fall through unauthenticated, which then triggers `JwtAuthenticationEntryPoint` → HTTP
    401 for protected endpoints (see [Error Responses](#error-responses)).
- **Token lifetimes** (`za.co.gymhubsa.api.security.jwt.JwtProperties`, prefix `app.jwt`):
  - `app.jwt.access-token-expiration-ms` — env `JWT_ACCESS_EXPIRATION_MS`, default `900000` ms
    (**15 minutes**).
  - `app.jwt.refresh-token-expiration-ms` — env `JWT_REFRESH_EXPIRATION_MS`, default `604800000` ms
    (**7 days**).
  - `app.jwt.secret` — env `JWT_SECRET`; the `dev` profile supplies a fixed non-production default
    so the app runs out of the box.
  - `LoginResponse.expiresIn` reports the access-token lifetime **in seconds**
    (`accessTokenExpirationMs / 1000`), not milliseconds.
- **Public (unauthenticated) endpoints** — exact matcher list from
  `SecurityConfig.PUBLIC_ENDPOINTS`:
  `/api/v1/auth/**`, `/api/v1/payments/webhook/**`, `/swagger-ui/**`, `/swagger-ui.html`,
  `/v3/api-docs/**`, `/actuator/health`, `/h2-console/**`. Every other request requires
  `anyRequest().authenticated()`, with fine-grained role checks applied per-method via
  `@PreAuthorize` (method security is enabled via `@EnableMethodSecurity`).
- **Roles (`Role` enum):** `ADMIN`, `TRAINER`, `MEMBER` — a `User` has exactly one role. Role checks
  throughout this document reflect each endpoint's actual `@PreAuthorize("hasRole('...')")` /
  `hasAnyRole('...', '...')` annotation; for the `/api/v1/reports/**` group the role check is
  applied once, at the controller class level.
- **Dev-seeded admin account** (dev profile only — `za.co.gymhubsa.api.config.DevDataSeeder`):
  email `admin@gymhubsa.co.za`, password `Admin@12345`. Useful for local testing; do not assume
  this exists in non-dev environments.

### Standard Success Response Envelope

Every successful response (HTTP 2xx) is wrapped by `za.co.gymhubsa.api.common.dto.ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Optional human-readable message, or omitted",
  "data": { },
  "timestamp": "2026-01-15T10:32:00.123Z"
}
```

| Field | Type | Notes |
|---|---|---|
| `success` | boolean | always `true` on this envelope |
| `message` | String | nullable; **omitted from the JSON entirely when null** (`@JsonInclude(NON_NULL)`) |
| `data` | `T` (generic) | the endpoint's payload; nullable (e.g. `Void` endpoints always omit it) |
| `timestamp` | Instant | server time the response was built, ISO-8601 / RFC-3339 with `Z` suffix |

Helper factories used by controllers: `ApiResponse.success(data)` (no message),
`ApiResponse.success(message, data)`, and `ApiResponse.message(message)` (for `Void` endpoints —
`data` is omitted).

### Error Response Envelope & Status Codes

Every error response is built by `za.co.gymhubsa.api.common.exception.GlobalExceptionHandler` using
`za.co.gymhubsa.api.common.dto.ErrorResponse`:

```json
{
  "success": false,
  "message": "Human-readable error message",
  "status": 404,
  "path": "/api/v1/members/9999",
  "timestamp": "2026-01-15T10:32:00.123Z",
  "errors": ["Email is required", "Password must be at least 8 characters"]
}
```

| Field | Type | Notes |
|---|---|---|
| `success` | boolean | always `false` |
| `message` | String | summary message |
| `status` | int | HTTP status code, duplicated in the body |
| `path` | String | the request URI that failed |
| `timestamp` | Instant | ISO-8601 |
| `errors` | `List<String>` | nullable; **only populated for bean-validation failures** (per-field default messages); omitted otherwise |

**Exception → HTTP status mapping** (exhaustive, from `GlobalExceptionHandler`):

| Exception | HTTP Status | `message` |
|---|---|---|
| `ResourceNotFoundException` | 404 Not Found | the exception's own message (e.g. `"Member not found with identifier: 9999"`) |
| `BadRequestException` | 400 Bad Request | the exception's own message |
| `MethodArgumentNotValidException` (bean validation, `@Valid` failures) | 400 Bad Request | `"Validation failed"`, with `errors` populated from each field's validation message |
| `UnauthorizedException`, `BadCredentialsException`, Spring Security `AuthenticationException` | 401 Unauthorized | always `"Invalid credentials"` (message is not exception-specific) |
| `org.springframework.security.access.AccessDeniedException` (role/`@PreAuthorize` failure) | 403 Forbidden | always `"You do not have permission to perform this action"` |
| any other `Exception` (unhandled) | 500 Internal Server Error | always `"An unexpected error occurred"` |

**Note:** there is **no dedicated HTTP 409 Conflict mapping** in this API. State-conflict scenarios
(e.g. "member already has an active membership", "duplicate plan name") are raised as
`BadRequestException` by the relevant service and therefore surface as **400 Bad Request**, not 409
— do not design frontend error handling around a 409 status for these cases
(`verify in source: the individual *Service classes, e.g.
za.co.gymhubsa.api.membership.service.MemberMembershipService`).

### Pagination

Any endpoint whose controller method takes a `org.springframework.data.domain.Pageable` parameter
(annotated `@PageableDefault(size = 20)` throughout this API — every paginated endpoint in this
backend defaults to a page size of **20**) supports the standard Spring Data web query parameters:

| Query param | Type | Notes |
|---|---|---|
| `page` | int | 0-based page index, default `0` |
| `size` | int | page size, default `20` in this API (every `@PageableDefault` found in the codebase uses `size = 20`) |
| `sort` | String, repeatable | format `property,direction` (e.g. `sort=lastName,asc`); repeat the param for multi-field sort (e.g. `sort=lastName,asc&sort=firstName,asc`); omit for unsorted/default DB order |

The resulting `org.springframework.data.domain.Page<T>` is placed directly in the `ApiResponse.data`
field (there is no custom pagination DTO in this codebase — it is Spring Data's own `Page`
serialization). The standard Jackson field names Spring Data produces for a `Page<T>` are:

```json
{
  "success": true,
  "data": {
    "content": [ ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": { "sorted": false, "unsorted": true, "empty": true },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 3,
    "size": 20,
    "number": 0,
    "sort": { "sorted": false, "unsorted": true, "empty": true },
    "first": true,
    "numberOfElements": 3,
    "empty": false
  },
  "message": null,
  "timestamp": "2026-01-15T10:32:00.123Z"
}
```

| Field (inside `data`) | Type | Notes |
|---|---|---|
| `content` | `T[]` | the actual page of items — this is what the frontend lists/tables will render |
| `totalElements` | long | total matching rows across all pages |
| `totalPages` | int | total number of pages |
| `number` | int | current page index (0-based) |
| `size` | int | page size used |
| `numberOfElements` | int | number of elements on this specific page (may be `< size` on the last page) |
| `first` / `last` | boolean | whether this is the first/last page |
| `empty` | boolean | whether `content` is empty |
| `pageable` / `sort` | object | Spring Data's internal pageable/sort metadata; generally not needed by the frontend beyond `sort.sorted` |

`(verify in source: confirm this exact Jackson shape by inspecting a live response from any
paginated endpoint — this is standard `org.springframework.data.domain.PageImpl` serialization
behavior for this Spring Boot/Spring Data version, since the project has no custom `Page`→DTO
mapping and does not depend on `spring-hateoas`, but it is a framework default rather than
application code.)`

### `/me/...` Self-Service vs Admin/Trainer Variants

Several modules expose a `/me/...` (or `/me` path segment) variant alongside an id-based admin/
trainer variant for the same underlying data. These are documented separately under each module
above, but as a quick summary, modules with both variants: **Member** (`GET /members/me` vs
`GET /members/{id}`), **Trainer** (`GET /trainers/me` vs `GET /trainers/{id}`,
`GET /trainer-assignments/me/members` vs `.../trainer/{trainerId}/members`,
`/trainers/me/schedule` vs `/trainers/{trainerId}/schedule`), **Membership**
(`/memberships/me/current`, `/memberships/me/history`), **Attendance** (`/attendance/me`),
**Payment** (`/payments/me`), **Workout** (`/workouts/me/current`, `/workouts/me/history`,
`/workouts/me/progress`), **Diet** (`/diets/me/current`, `/diets/me/history`), **Nutrition Goals**
(`/nutrition-goals/me/current`, `/nutrition-goals/me/history`), **Weight Progress**
(`/weight-progress/me`, `/me/history`, `/me/latest`), **Loyalty**
(`/loyalty/me/balance`, `/me/history`, `/me/redeem`), **Notification** (`/notifications/me/history`).
The `/me/...` variants always require the `MEMBER` (or `TRAINER`, for trainer-specific `/me`
endpoints) role and resolve the acting member/trainer id from the authenticated principal's email
(`Authentication.getName()`), never from a client-supplied id — the admin/trainer id-based variants
require `ADMIN`/`TRAINER` and accept any id.

### Entity Base Fields

All JPA entities extend `za.co.gymhubsa.api.common.entity.BaseEntity`, which contributes:
`id` (Long, auto-generated identity), `createdAt` (Instant, set once on insert), `updatedAt`
(Instant, updated on every save) — managed automatically via JPA auditing. These are not always
surfaced on every response DTO (each DTO's field table above is authoritative for what that specific
endpoint actually returns).
