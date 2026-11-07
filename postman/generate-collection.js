// One-off generator for the Postman collection. Not part of the build;
// re-run with `node postman/generate-collection.js` after adding endpoints.
const fs = require('fs');
const path = require('path');

const AUTH = { ADMIN: 'admin', TRAINER: 'trainer', MEMBER: 'member', NONE: 'none', WEBHOOK: 'webhook' };

function authHeader(auth) {
    if (auth === AUTH.NONE || auth === AUTH.WEBHOOK) return [];
    const tokenVar = `${auth}Token`;
    return [{ key: 'Authorization', value: `Bearer {{${tokenVar}}}`, type: 'text' }];
}

function req(name, method, urlPath, { auth = AUTH.ADMIN, body = null, query = null, test = null, extraHeaders = [] } = {}) {
    const headers = [...authHeader(auth), ...extraHeaders];
    if (body) headers.push({ key: 'Content-Type', value: 'application/json', type: 'text' });

    const url = {
        raw: `{{baseUrl}}${urlPath}` + (query ? '?' + Object.entries(query).map(([k, v]) => `${k}=${v}`).join('&') : ''),
        host: ['{{baseUrl}}'],
        path: urlPath.replace(/^\//, '').split('/'),
    };
    if (query) url.query = Object.entries(query).map(([k, v]) => ({ key: k, value: String(v) }));

    const item = {
        name,
        request: {
            method,
            header: headers,
            url,
        },
    };
    if (body) {
        item.request.body = { mode: 'raw', raw: JSON.stringify(body, null, 2), options: { raw: { language: 'json' } } };
    }
    if (test) {
        const fullScript = ['const json = pm.response.json();', ...test.split('\n')].join('\n');
        item.event = [{ listen: 'test', script: { type: 'text/javascript', exec: fullScript.split('\n') } }];
    }
    return item;
}

const saveVar = (varName, jsonPath) => `pm.collectionVariables.set('${varName}', ${jsonPath});`;

const folders = [
    {
        name: '00 - Auth',
        item: [
            req('Login as Admin (seeded dev account)', 'POST', '/api/v1/auth/login', {
                auth: AUTH.NONE,
                body: { email: 'admin@gymhubsa.co.za', password: 'Admin@12345' },
                test: saveVar('adminToken', 'json.data.accessToken'),
            }),
            req('Refresh Token', 'POST', '/api/v1/auth/refresh', { auth: AUTH.NONE, body: { refreshToken: '{{adminRefreshToken}}' } }),
            req('Logout', 'POST', '/api/v1/auth/logout', { auth: AUTH.ADMIN }),
            req('Forgot Password', 'POST', '/api/v1/auth/forgot-password', { auth: AUTH.NONE, body: { email: 'admin@gymhubsa.co.za' } }),
            req('Reset Password', 'POST', '/api/v1/auth/reset-password', { auth: AUTH.NONE, body: { token: '{{resetToken}}', newPassword: 'NewPassword123!' } }),
        ],
    },
    {
        name: '01 - Members',
        item: [
            req('Register Member', 'POST', '/api/v1/members', {
                body: { firstName: 'Palesa', lastName: 'Demo', email: 'palesa.demo.{{$timestamp}}@gymhubsa.co.za', password: 'Password123!', phoneNumber: '0821234567' },
                test: saveVar('memberId', 'json.data.id') + "\n" + saveVar('memberEmail', 'json.data.email') + "\n" + saveVar('memberQrCode', 'json.data.qrCode'),
            }),
            req('Search / List Members', 'GET', '/api/v1/members', { query: { keyword: '', status: '' } }),
            req('Get My Profile (Member)', 'GET', '/api/v1/members/me', { auth: AUTH.MEMBER }),
            req('Get Member By Id', 'GET', '/api/v1/members/{{memberId}}'),
            req('Update Member', 'PUT', '/api/v1/members/{{memberId}}', {
                body: { firstName: 'Palesa', lastName: 'Demo-Updated', phoneNumber: '0821234567' },
            }),
            req('Update Member Status', 'PATCH', '/api/v1/members/{{memberId}}/status', { body: { status: 'ACTIVE' } }),
            req('Upload Profile Photo', 'POST', '/api/v1/members/{{memberId}}/photo'),
            req('Deactivate Member', 'DELETE', '/api/v1/members/{{memberId}}'),
        ],
    },
    {
        name: '02 - Membership Plans',
        item: [
            req('Create Plan', 'POST', '/api/v1/membership-plans', {
                body: { name: 'Monthly Basic {{$timestamp}}', description: 'Full gym access', durationDays: 30, price: 499.0 },
                test: saveVar('planId', 'json.data.id'),
            }),
            req('List Plans', 'GET', '/api/v1/membership-plans', { auth: AUTH.MEMBER, query: { includeInactive: 'false' } }),
            req('Get Plan By Id', 'GET', '/api/v1/membership-plans/{{planId}}', { auth: AUTH.MEMBER }),
            req('Update Plan', 'PUT', '/api/v1/membership-plans/{{planId}}', {
                body: { name: 'Monthly Basic', description: 'Full gym access, updated', durationDays: 30, price: 549.0 },
            }),
            req('Deactivate Plan', 'DELETE', '/api/v1/membership-plans/{{planId}}'),
        ],
    },
    {
        name: '03 - Memberships',
        item: [
            req('Assign Plan to Member', 'POST', '/api/v1/memberships/assign', {
                body: { memberId: '{{memberId}}', planId: '{{planId}}' },
                test: saveVar('membershipId', 'json.data.id'),
            }),
            req('Renew Membership', 'POST', '/api/v1/memberships/{{memberId}}/renew'),
            req('Change Plan (Upgrade/Downgrade)', 'POST', '/api/v1/memberships/{{memberId}}/change-plan', { body: { newPlanId: '{{planId}}' } }),
            req('Get Current Membership (Admin)', 'GET', '/api/v1/memberships/{{memberId}}/current'),
            req('Get Membership History (Admin)', 'GET', '/api/v1/memberships/{{memberId}}/history'),
            req('Get My Current Membership (Member)', 'GET', '/api/v1/memberships/me/current', { auth: AUTH.MEMBER }),
            req('Get My Membership History (Member)', 'GET', '/api/v1/memberships/me/history', { auth: AUTH.MEMBER }),
            req('List Expiring Soon', 'GET', '/api/v1/memberships/expiring-soon', { query: { days: 7 } }),
        ],
    },
    {
        name: '04 - Attendance',
        item: [
            req('Check In (Manual)', 'POST', '/api/v1/attendance/check-in', { body: { memberId: '{{memberId}}' } }),
            req('Check In (QR Code)', 'POST', '/api/v1/attendance/check-in/qr', { body: { qrCode: '{{memberQrCode}}' } }),
            req('Check Out', 'POST', '/api/v1/attendance/check-out', { body: { memberId: '{{memberId}}' } }),
            req('Get Member Attendance History (Admin)', 'GET', '/api/v1/attendance/member/{{memberId}}'),
            req('Get My Attendance History (Member)', 'GET', '/api/v1/attendance/me', { auth: AUTH.MEMBER }),
            req('Daily Attendance Report', 'GET', '/api/v1/attendance/daily-report'),
        ],
    },
    {
        name: '05 - Payments',
        item: [
            req('Record Manual Payment', 'POST', '/api/v1/payments', {
                body: { memberId: '{{memberId}}', membershipId: '{{membershipId}}', amount: 200.0, method: 'CASH', notes: 'Front desk deposit' },
                test: saveVar('paymentId', 'json.data.id'),
            }),
            req('Initiate Online Payment (Member)', 'POST', '/api/v1/payments/online/initiate', { auth: AUTH.MEMBER, body: { membershipId: '{{membershipId}}', amount: 299.0 } }),
            req('Get Receipt', 'GET', '/api/v1/payments/{{paymentId}}/receipt'),
            req('Get Member Payment History (Admin)', 'GET', '/api/v1/payments/member/{{memberId}}'),
            req('Get My Payment History (Member)', 'GET', '/api/v1/payments/me', { auth: AUTH.MEMBER }),
            req('Get Outstanding Balance for Member', 'GET', '/api/v1/payments/member/{{memberId}}/outstanding-balance'),
            req('List All Outstanding Balances', 'GET', '/api/v1/payments/outstanding'),
            req('Revenue Report', 'GET', '/api/v1/payments/revenue'),
            req('Webhook: Confirm Online Payment', 'POST', '/api/v1/payments/webhook/confirm', {
                auth: AUTH.WEBHOOK,
                body: { gatewayReference: '{{gatewayReference}}', success: true },
                extraHeaders: [{ key: 'X-Webhook-Secret', value: '{{webhookSecret}}', type: 'text' }],
            }),
        ],
    },
    {
        name: '06 - Trainers',
        item: [
            req('Register Trainer', 'POST', '/api/v1/trainers', {
                body: { firstName: 'Thabo', lastName: 'Coach', email: 'thabo.coach.{{$timestamp}}@gymhubsa.co.za', password: 'Password123!', specialization: 'Strength', bio: '10 years experience', yearsOfExperience: 10 },
                test: saveVar('trainerId', 'json.data.id') + "\n" + saveVar('trainerEmail', 'json.data.email'),
            }),
            req('Search / List Trainers', 'GET', '/api/v1/trainers', { auth: AUTH.MEMBER, query: { keyword: '', status: '' } }),
            req('Get My Profile (Trainer)', 'GET', '/api/v1/trainers/me', { auth: AUTH.TRAINER }),
            req('Get Trainer By Id', 'GET', '/api/v1/trainers/{{trainerId}}', { auth: AUTH.MEMBER }),
            req('Update Trainer', 'PUT', '/api/v1/trainers/{{trainerId}}', {
                body: { firstName: 'Thabo', lastName: 'Coach-Updated', phoneNumber: '0821234567', specialization: 'Strength & Conditioning', bio: 'Updated bio', yearsOfExperience: 11 },
            }),
            req('Update Trainer Status', 'PATCH', '/api/v1/trainers/{{trainerId}}/status', { body: { status: 'ACTIVE' } }),
            req('Upload Trainer Photo', 'POST', '/api/v1/trainers/{{trainerId}}/photo'),
            req('Get Trainer Performance', 'GET', '/api/v1/trainers/{{trainerId}}/performance'),
        ],
    },
    {
        name: '07 - Trainer Schedule',
        item: [
            req('Add Schedule Slot (Admin)', 'POST', '/api/v1/trainers/{{trainerId}}/schedule', { body: { dayOfWeek: 'MONDAY', startTime: '06:00:00', endTime: '09:00:00' } }),
            req('Get Trainer Schedule', 'GET', '/api/v1/trainers/{{trainerId}}/schedule', { auth: AUTH.MEMBER }),
            req('Remove Schedule Slot (Admin)', 'DELETE', '/api/v1/trainers/{{trainerId}}/schedule/{{scheduleId}}'),
            req('Add My Schedule Slot (Trainer)', 'POST', '/api/v1/trainers/me/schedule', { auth: AUTH.TRAINER, body: { dayOfWeek: 'WEDNESDAY', startTime: '17:00:00', endTime: '19:00:00' } }),
            req('Remove My Schedule Slot (Trainer)', 'DELETE', '/api/v1/trainers/me/schedule/{{scheduleId}}', { auth: AUTH.TRAINER }),
        ],
    },
    {
        name: '08 - Trainer Assignments',
        item: [
            req('Assign Trainer to Member', 'POST', '/api/v1/trainer-assignments/assign', { body: { memberId: '{{memberId}}', trainerId: '{{trainerId}}' } }),
            req('Get Member\'s Current Trainer', 'GET', '/api/v1/trainer-assignments/member/{{memberId}}/current'),
            req('Get Trainer\'s Assigned Members', 'GET', '/api/v1/trainer-assignments/trainer/{{trainerId}}/members'),
            req('Get My Assigned Members (Trainer)', 'GET', '/api/v1/trainer-assignments/me/members', { auth: AUTH.TRAINER }),
        ],
    },
    {
        name: '09 - Workout Plans',
        item: [
            req('Create Workout Plan', 'POST', '/api/v1/workout-plans', {
                auth: AUTH.TRAINER,
                body: {
                    name: 'Beginner Full Body', description: '3x/week full body', difficultyLevel: 'BEGINNER', durationWeeks: 4,
                    exercises: [
                        { name: 'Squat', sets: 3, reps: 10, restSeconds: 60, sortOrder: 1 },
                        { name: 'Push-up', sets: 3, reps: 12, restSeconds: 45, sortOrder: 2 },
                    ],
                },
                test: saveVar('workoutPlanId', 'json.data.id'),
            }),
            req('List Workout Plans', 'GET', '/api/v1/workout-plans', { auth: AUTH.MEMBER, query: { includeInactive: 'false' } }),
            req('Get Workout Plan By Id', 'GET', '/api/v1/workout-plans/{{workoutPlanId}}', { auth: AUTH.MEMBER }),
            req('Update Workout Plan', 'PUT', '/api/v1/workout-plans/{{workoutPlanId}}', {
                auth: AUTH.TRAINER,
                body: {
                    name: 'Beginner Full Body v2', description: 'Updated', difficultyLevel: 'BEGINNER', durationWeeks: 6,
                    exercises: [{ name: 'Deadlift', sets: 4, reps: 8, restSeconds: 90, sortOrder: 1 }],
                },
            }),
            req('Deactivate Workout Plan', 'DELETE', '/api/v1/workout-plans/{{workoutPlanId}}', { auth: AUTH.TRAINER }),
        ],
    },
    {
        name: '10 - Workout Assignments & Progress',
        item: [
            req('Assign Workout Plan', 'POST', '/api/v1/workouts/assign', { auth: AUTH.TRAINER, body: { memberId: '{{memberId}}', workoutPlanId: '{{workoutPlanId}}' },
                test: saveVar('memberWorkoutPlanId', 'json.data.id') },
            ),
            req('Mark Assignment Completed', 'PATCH', '/api/v1/workouts/{{memberWorkoutPlanId}}/complete', { auth: AUTH.TRAINER }),
            req('Get Member\'s Current Workout Plan', 'GET', '/api/v1/workouts/member/{{memberId}}/current'),
            req('Get Member\'s Workout History', 'GET', '/api/v1/workouts/member/{{memberId}}/history'),
            req('Get My Current Workout Plan (Member)', 'GET', '/api/v1/workouts/me/current', { auth: AUTH.MEMBER }),
            req('Get My Workout History (Member)', 'GET', '/api/v1/workouts/me/history', { auth: AUTH.MEMBER }),
            req('Log Progress for Assignment (Trainer)', 'POST', '/api/v1/workouts/{{memberWorkoutPlanId}}/progress', { auth: AUTH.TRAINER, body: { durationMinutes: 45, rating: 4, notes: 'Good session' } }),
            req('Get Progress Logs (Admin/Trainer)', 'GET', '/api/v1/workouts/{{memberWorkoutPlanId}}/progress'),
            req('Log My Progress (Member)', 'POST', '/api/v1/workouts/me/progress', { auth: AUTH.MEMBER, body: { durationMinutes: 40, rating: 3, notes: 'Felt good' } }),
            req('Get My Progress Logs (Member)', 'GET', '/api/v1/workouts/me/progress', { auth: AUTH.MEMBER }),
        ],
    },
    {
        name: '11 - Diet Plans',
        item: [
            req('Create Diet Plan', 'POST', '/api/v1/diet-plans', {
                auth: AUTH.TRAINER,
                body: {
                    name: 'High-Protein Cutting', description: 'For fat loss', dailyCalorieTarget: 1800, proteinGrams: 160, carbsGrams: 150, fatGrams: 50,
                    meals: [{ name: 'Breakfast', description: 'Oats + eggs', calories: 450, sortOrder: 1 }],
                },
                test: saveVar('dietPlanId', 'json.data.id'),
            }),
            req('List Diet Plans', 'GET', '/api/v1/diet-plans', { auth: AUTH.MEMBER, query: { includeInactive: 'false' } }),
            req('Get Diet Plan By Id', 'GET', '/api/v1/diet-plans/{{dietPlanId}}', { auth: AUTH.MEMBER }),
            req('Update Diet Plan', 'PUT', '/api/v1/diet-plans/{{dietPlanId}}', {
                auth: AUTH.TRAINER,
                body: { name: 'High-Protein Cutting v2', description: 'Updated', dailyCalorieTarget: 1900, proteinGrams: 170, carbsGrams: 150, fatGrams: 50,
                    meals: [{ name: 'Lunch', description: 'Chicken + rice', calories: 600, sortOrder: 1 }] },
            }),
            req('Deactivate Diet Plan', 'DELETE', '/api/v1/diet-plans/{{dietPlanId}}', { auth: AUTH.TRAINER }),
        ],
    },
    {
        name: '12 - Diet Assignments',
        item: [
            req('Assign Diet Plan', 'POST', '/api/v1/diets/assign', { body: { memberId: '{{memberId}}', dietPlanId: '{{dietPlanId}}' } }),
            req('Get Member\'s Current Diet Plan', 'GET', '/api/v1/diets/member/{{memberId}}/current'),
            req('Get Member\'s Diet History', 'GET', '/api/v1/diets/member/{{memberId}}/history'),
            req('Get My Current Diet Plan (Member)', 'GET', '/api/v1/diets/me/current', { auth: AUTH.MEMBER }),
            req('Get My Diet History (Member)', 'GET', '/api/v1/diets/me/history', { auth: AUTH.MEMBER }),
        ],
    },
    {
        name: '13 - Nutrition Goals',
        item: [
            req('Set Nutrition Goal', 'POST', '/api/v1/nutrition-goals/member/{{memberId}}', {
                body: { goalType: 'WEIGHT_LOSS', startWeightKg: 85.0, targetWeightKg: 78.0, targetDate: '2026-12-31' },
                test: saveVar('nutritionGoalId', 'json.data.id'),
            }),
            req('Mark Goal Achieved', 'PATCH', '/api/v1/nutrition-goals/{{nutritionGoalId}}/achieved'),
            req('Get Member\'s Current Goal', 'GET', '/api/v1/nutrition-goals/member/{{memberId}}/current'),
            req('Get Member\'s Goal History', 'GET', '/api/v1/nutrition-goals/member/{{memberId}}/history'),
            req('Get My Current Goal (Member)', 'GET', '/api/v1/nutrition-goals/me/current', { auth: AUTH.MEMBER }),
            req('Get My Goal History (Member)', 'GET', '/api/v1/nutrition-goals/me/history', { auth: AUTH.MEMBER }),
        ],
    },
    {
        name: '14 - Weight Progress',
        item: [
            req('Log Weight for Member (Admin/Trainer)', 'POST', '/api/v1/weight-progress/member/{{memberId}}', { body: { weightKg: 84.2, notes: 'Week 1' } }),
            req('Get Member\'s Weight History', 'GET', '/api/v1/weight-progress/member/{{memberId}}/history'),
            req('Get Member\'s Latest Weight', 'GET', '/api/v1/weight-progress/member/{{memberId}}/latest'),
            req('Log My Weight (Member)', 'POST', '/api/v1/weight-progress/me', { auth: AUTH.MEMBER, body: { weightKg: 83.5, notes: 'Week 2' } }),
            req('Get My Weight History (Member)', 'GET', '/api/v1/weight-progress/me/history', { auth: AUTH.MEMBER }),
            req('Get My Latest Weight (Member)', 'GET', '/api/v1/weight-progress/me/latest', { auth: AUTH.MEMBER }),
        ],
    },
    {
        name: '15 - Loyalty',
        item: [
            req('Earn Points', 'POST', '/api/v1/loyalty/earn', { body: { memberId: '{{memberId}}', points: 100, reason: 'Attendance streak' } }),
            req('Redeem Points (Admin, on behalf)', 'POST', '/api/v1/loyalty/redeem', { body: { memberId: '{{memberId}}', rewardId: '{{rewardId}}' } }),
            req('Get Member\'s Balance', 'GET', '/api/v1/loyalty/member/{{memberId}}/balance'),
            req('Get Member\'s Points History', 'GET', '/api/v1/loyalty/member/{{memberId}}/history'),
            req('Get My Balance (Member)', 'GET', '/api/v1/loyalty/me/balance', { auth: AUTH.MEMBER }),
            req('Get My Points History (Member)', 'GET', '/api/v1/loyalty/me/history', { auth: AUTH.MEMBER }),
            req('Redeem My Points (Member)', 'POST', '/api/v1/loyalty/me/redeem', { auth: AUTH.MEMBER, body: { rewardId: '{{rewardId}}' } }),
        ],
    },
    {
        name: '16 - Rewards',
        item: [
            req('Create Reward', 'POST', '/api/v1/rewards', { body: { name: 'Free Protein Shake', description: 'On the house', pointsCost: 50, stockQuantity: 20 },
                test: saveVar('rewardId', 'json.data.id') },
            ),
            req('List Rewards', 'GET', '/api/v1/rewards', { auth: AUTH.MEMBER, query: { includeInactive: 'false' } }),
            req('Get Reward By Id', 'GET', '/api/v1/rewards/{{rewardId}}', { auth: AUTH.MEMBER }),
            req('Update Reward', 'PUT', '/api/v1/rewards/{{rewardId}}', { body: { name: 'Free Protein Shake', description: 'Updated', pointsCost: 60, stockQuantity: 15 } }),
            req('Deactivate Reward', 'DELETE', '/api/v1/rewards/{{rewardId}}'),
        ],
    },
    {
        name: '17 - Notifications',
        item: [
            req('Send Notification to Member', 'POST', '/api/v1/notifications/send', { body: { memberId: '{{memberId}}', channel: 'EMAIL', type: 'GENERAL', subject: 'Welcome', message: 'Welcome to GymHub SA!' } }),
            req('Broadcast Promotional Notification', 'POST', '/api/v1/notifications/promotional/broadcast', { body: { channel: 'EMAIL', subject: 'Sale!', message: '20% off this week', memberIds: null } }),
            req('Get Member\'s Notification History (Admin)', 'GET', '/api/v1/notifications/member/{{memberId}}/history'),
            req('Get My Notification History (Member)', 'GET', '/api/v1/notifications/me/history', { auth: AUTH.MEMBER }),
        ],
    },
    {
        name: '18 - Dashboard',
        item: [req('Get Dashboard Summary', 'GET', '/api/v1/dashboard/summary')],
    },
    {
        name: '19 - Reports',
        item: [
            req('Member Report', 'GET', '/api/v1/reports/members'),
            req('Membership Report', 'GET', '/api/v1/reports/memberships'),
            req('Attendance Report', 'GET', '/api/v1/reports/attendance'),
            req('Payment Report', 'GET', '/api/v1/reports/payments', { query: { from: '2026-01-01', to: '2026-12-31' } }),
            req('Revenue Report', 'GET', '/api/v1/reports/revenue'),
            req('Trainer Report', 'GET', '/api/v1/reports/trainers'),
            req('Membership Expiry Report', 'GET', '/api/v1/reports/membership-expiry', { query: { days: 30 } }),
            req('Loyalty Report', 'GET', '/api/v1/reports/loyalty'),
        ],
    },
    {
        name: '20 - Files',
        item: [req('Download Uploaded File', 'GET', '/api/v1/files/{{fileSubdirectory}}/{{fileName}}', { auth: AUTH.MEMBER })],
    },
];

// Login requests for trainer/member tokens, inserted right after member/trainer creation for a smooth "Run folder" flow.
folders[1].item.splice(1, 0, req('Login as New Member', 'POST', '/api/v1/auth/login', {
    auth: AUTH.NONE,
    body: { email: '{{memberEmail}}', password: 'Password123!' },
    test: saveVar('memberToken', 'json.data.accessToken'),
}));
folders[5].item.splice(1, 0, req('Login as New Trainer', 'POST', '/api/v1/auth/login', {
    auth: AUTH.NONE,
    body: { email: '{{trainerEmail}}', password: 'Password123!' },
    test: saveVar('trainerToken', 'json.data.accessToken'),
}));

const collection = {
    info: {
        name: 'GymHub SA API',
        description: 'End-to-end collection for the GymHub SA backend. Run "00 - Auth" first, then folders in order — '
            + 'IDs and tokens are captured into collection variables automatically via each request\'s test script.',
        schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
    },
    variable: [
        { key: 'baseUrl', value: 'http://localhost:8081' },
        { key: 'adminToken', value: '' },
        { key: 'trainerToken', value: '' },
        { key: 'memberToken', value: '' },
        { key: 'memberId', value: '' },
        { key: 'memberEmail', value: '' },
        { key: 'memberQrCode', value: '' },
        { key: 'trainerId', value: '' },
        { key: 'trainerEmail', value: '' },
        { key: 'planId', value: '' },
        { key: 'membershipId', value: '' },
        { key: 'paymentId', value: '' },
        { key: 'gatewayReference', value: '' },
        { key: 'webhookSecret', value: 'dev-only-webhook-secret-change-me' },
        { key: 'scheduleId', value: '' },
        { key: 'workoutPlanId', value: '' },
        { key: 'memberWorkoutPlanId', value: '' },
        { key: 'dietPlanId', value: '' },
        { key: 'nutritionGoalId', value: '' },
        { key: 'rewardId', value: '' },
        { key: 'fileSubdirectory', value: 'members' },
        { key: 'fileName', value: '' },
    ],
    item: folders.map((f) => ({ name: f.name, item: f.item })),
};

const outPath = path.join(__dirname, 'GymHub-SA-API.postman_collection.json');
fs.writeFileSync(outPath, JSON.stringify(collection, null, 2) + '\n');
console.log('Wrote', outPath, '-', folders.reduce((n, f) => n + f.item.length, 0), 'requests in', folders.length, 'folders');
