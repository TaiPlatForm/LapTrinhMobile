# 📖 PHÂN TÍCH CHI TIẾT MODULE 1–3
## 📱 Dự án: Ứng dụng Quản lý Dinh dưỡng Cá nhân với AI (Smart Nutrition)

> Tài liệu này giải thích **cực chi tiết** từng module: mục đích, màn hình UI, luồng dữ liệu, cấu trúc Firestore, thư viện cần dùng, và code mẫu minh họa. Dùng cho **nhóm dev** đọc hiểu và **AI** sinh code.

---

## MODULE 1: 🔑 ĐĂNG NHẬP & THIẾT LẬP THỂ TRẠNG

### 1.1 Mục đích
Cho phép người dùng tạo tài khoản, đăng nhập, và thiết lập hồ sơ sức khỏe cá nhân. Hệ thống sẽ tự động tính toán các chỉ số dinh dưỡng (BMI, BMR, TDEE, Macros) dựa trên thông tin cơ thể để làm nền tảng cho toàn bộ app.

### 1.2 Danh sách màn hình (Screens)

| # | Tên màn hình | Mô tả |
|:-:|:------------|:------|
| 1 | `LoginActivity` | Form đăng nhập Email/Password + Nút "Đăng nhập bằng Google" + Link "Chưa có tài khoản?" |
| 2 | `RegisterActivity` | Form đăng ký (Email, Password, Confirm Password) |
| 3 | `ProfileSetupActivity` | Form nhập thể trạng ban đầu (chỉ hiện 1 lần sau đăng ký lần đầu) |
| 4 | `ProfileViewActivity` | Xem thông tin cá nhân + Các chỉ số đã tính + Nút chỉnh sửa |
| 5 | `WeightLogActivity` | Nhập cân nặng hôm nay + Danh sách lịch sử cân nặng |

### 1.3 Cấu trúc Firestore Database

```
users (collection)
└── {uid} (document)
    ├── email: "user@gmail.com"
    ├── displayName: "Nguyễn Văn A"
    ├── gender: "male"           // "male" | "female"
    ├── birthYear: 2003
    ├── heightCm: 170            // Chiều cao (cm)
    ├── weightKg: 65.5           // Cân nặng hiện tại (kg)
    ├── activityLevel: 1.55      // Hệ số vận động (1.2 → 1.9)
    ├── goal: "lose_weight"      // "lose_weight" | "maintain" | "gain_muscle"
    ├── bmi: 22.66
    ├── bmr: 1632.5
    ├── tdee: 2530.4
    ├── proteinTarget: 130       // gram/ngày
    ├── carbTarget: 280          // gram/ngày
    ├── fatTarget: 70            // gram/ngày
    ├── calorieTarget: 2024      // kcal/ngày (TDEE đã điều chỉnh theo goal)
    ├── createdAt: Timestamp
    ├── updatedAt: Timestamp
    │
    └── weightLog (sub-collection)
        └── {date} (document)    // VD: "2026-05-21"
            ├── weightKg: 65.2
            ├── bmi: 22.56       // Tính lại BMI theo cân nặng mới
            └── loggedAt: Timestamp
```

### 1.4 Thuật toán tính chỉ số sức khỏe (chạy LOCAL trên app)

```java
// === BMI ===
double bmi = weightKg / Math.pow(heightCm / 100.0, 2);
// Phân loại: < 18.5 = Thiếu cân, 18.5-24.9 = Bình thường, 25-29.9 = Thừa cân, >= 30 = Béo phì

// === BMR (Harris-Benedict) ===
double bmr;
if (gender.equals("male")) {
    bmr = 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age);
} else {
    bmr = 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age);
}

// === TDEE ===
// activityLevel: 1.2 (ít vận động), 1.375 (nhẹ), 1.55 (vừa), 1.725 (nhiều), 1.9 (rất nhiều)
double tdee = bmr * activityLevel;

// === Calorie Target (điều chỉnh theo mục tiêu) ===
double calorieTarget;
if (goal.equals("lose_weight")) {
    calorieTarget = tdee - 500;     // Giảm 500 kcal/ngày → giảm ~0.5kg/tuần
} else if (goal.equals("gain_muscle")) {
    calorieTarget = tdee + 300;     // Tăng 300 kcal/ngày
} else {
    calorieTarget = tdee;           // Giữ nguyên
}

// === Macros Target (tỷ lệ P/C/F) ===
double proteinGrams, carbGrams, fatGrams;
if (goal.equals("gain_muscle")) {
    // Tăng cơ: 30% Protein, 45% Carb, 25% Fat
    proteinGrams = (calorieTarget * 0.30) / 4;  // 1g protein = 4 kcal
    carbGrams    = (calorieTarget * 0.45) / 4;   // 1g carb = 4 kcal
    fatGrams     = (calorieTarget * 0.25) / 9;   // 1g fat = 9 kcal
} else if (goal.equals("lose_weight")) {
    // Giảm cân: 35% Protein, 35% Carb, 30% Fat
    proteinGrams = (calorieTarget * 0.35) / 4;
    carbGrams    = (calorieTarget * 0.35) / 4;
    fatGrams     = (calorieTarget * 0.30) / 9;
} else {
    // Duy trì: 25% Protein, 50% Carb, 25% Fat
    proteinGrams = (calorieTarget * 0.25) / 4;
    carbGrams    = (calorieTarget * 0.50) / 4;
    fatGrams     = (calorieTarget * 0.25) / 9;
}
```

### 1.5 Luồng hoạt động (Flow)

```
[Mở App] → Kiểm tra FirebaseAuth.currentUser
    ├── null → LoginActivity
    │   ├── Nhập Email/Pass → signInWithEmailAndPassword()
    │   ├── Click Google → CredentialManager → signInWithCredential()
    │   └── Click "Đăng ký" → RegisterActivity → createUserWithEmailAndPassword()
    │       └── Thành công → ProfileSetupActivity
    │
    └── != null → Kiểm tra Firestore users/{uid} có tồn tại?
        ├── Chưa có → ProfileSetupActivity (nhập thể trạng lần đầu)
        └── Có rồi → MainActivity (Dashboard)
```

### 1.6 Dependencies (build.gradle)

```groovy
// Firebase Auth
implementation 'com.google.firebase:firebase-auth'
// Google Credential Manager (thay thế Google Sign-In cũ)
implementation 'androidx.credentials:credentials:1.3.0'
implementation 'androidx.credentials:credentials-play-services-auth:1.3.0'
implementation 'com.google.android.libraries.identity.googleid:googleid:1.1.1'
// Firestore
implementation 'com.google.firebase:firebase-firestore'
```

### 1.7 Lưu ý quan trọng cho dev
- **Google Sign-In**: Cần lấy SHA-1 fingerprint từ Android Studio → paste vào Firebase Console → Download lại `google-services.json`. Sai bước này = không login được.
- **Weight Logger**: Dùng document ID = ngày (format `yyyy-MM-dd`) để mỗi ngày chỉ có 1 record, user log lại thì update chứ không tạo mới.
- **Tính lại chỉ số**: Mỗi khi user sửa profile (đổi cân nặng, chiều cao) → phải tính lại BMI/BMR/TDEE/Macros và update cùng lúc.

---

## MODULE 2: 📸 QUÉT & QUẢN LÝ KHO THỰC PHẨM

### 2.1 Mục đích
Giúp người dùng số hóa kho thực phẩm tại nhà bằng 2 cách: **chụp ảnh** (AI nhận diện tự động) hoặc **quét barcode** (tra cứu sản phẩm đóng hộp). Quản lý tồn kho với cảnh báo hạn sử dụng.

### 2.2 Danh sách màn hình

| # | Tên màn hình | Mô tả |
|:-:|:------------|:------|
| 1 | `PantryListActivity` | Danh sách thực phẩm trong kho (RecyclerView) + FAB button "Thêm" + Bộ lọc (Tất cả / Sắp hết hạn / Đã hết hạn) |
| 2 | `CameraCaptureActivity` | Preview camera toàn màn hình + Nút chụp + Nút chuyển sang quét barcode |
| 3 | `FoodResultActivity` | Hiển thị kết quả AI nhận diện (Tên, Calo, Protein) + Cho user chỉnh sửa + Chọn số lượng & ngày hết hạn + Nút "Lưu vào kho" |
| 4 | `BarcodeScanActivity` | Camera quét barcode real-time + Hiển thị kết quả tra cứu |
| 5 | `FoodDetailActivity` | Chi tiết 1 thực phẩm (sửa số lượng, xóa, xem ngày thêm) |

### 2.3 Cấu trúc Firestore Database

```
users/{uid}/pantry (sub-collection)
└── {autoId} (document)
    ├── name: "Ức gà"
    ├── caloriesPer100g: 165         // kcal trên 100g
    ├── proteinPer100g: 31           // gram trên 100g
    ├── quantityGrams: 500           // Tổng gram đang có
    ├── unit: "gram"                 // "gram" | "piece" | "ml"
    ├── source: "camera"             // "camera" | "barcode" | "manual"
    ├── imageUrl: ""                 // (optional) URL ảnh đã chụp
    ├── expiryDate: Timestamp        // Ngày hết hạn
    ├── addedAt: Timestamp
    ├── status: "fresh"              // "fresh" | "expiring" | "expired"
    └── barcode: ""                  // Mã vạch nếu có
```

### 2.4 Luồng hoạt động: Chụp ảnh → AI nhận diện

```
[User mở Camera] → CameraX Preview hiển thị
    → User nhấn nút Chụp
    → CameraX capture ảnh → lưu Bitmap vào bộ nhớ tạm
    → Convert Bitmap → byte[] → Base64 String
    → Gửi đến Gemini API với prompt:

    ┌──────────────────────────────────────────────────┐
    │ PROMPT GỬI GEMINI:                               │
    │ "Bạn là chuyên gia dinh dưỡng. Nhận diện thực   │
    │ phẩm trong ảnh này. Trả về ĐÚNG 1 JSON object:  │
    │ {                                                │
    │   "name": "tên tiếng Việt",                      │
    │   "calories": số kcal trên 100g,                 │
    │   "protein": số gram protein trên 100g           │
    │ }                                                │
    │ Chỉ trả JSON, không giải thích thêm."            │
    └──────────────────────────────────────────────────┘

    → Gemini trả JSON → Parse bằng Gson
    → Hiển thị kết quả trên FoodResultActivity
    → User xác nhận / chỉnh sửa → Lưu vào Firestore
```

### 2.5 Luồng hoạt động: Quét Barcode

```
[User chọn "Quét mã vạch"] → Mở BarcodeScanActivity
    → ML Kit BarcodeScannerOptions (FORMAT_EAN_13, FORMAT_EAN_8, FORMAT_UPC_A)
    → Camera quét real-time → Phát hiện barcode → Lấy rawValue (chuỗi số)
    → Tra cứu chuỗi số:
        ├── Option A: Tra bảng local (HashMap hardcode các SP phổ biến VN)
        └── Option B: Gọi Open Food Facts API: https://world.openfoodfacts.org/api/v2/product/{barcode}
    → Hiển thị kết quả → User xác nhận → Lưu Firestore
```

### 2.6 Logic cảnh báo hạn sử dụng

```java
long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);

if (daysUntilExpiry > 3) {
    // 🟢 Xanh lá — Còn tươi
    holder.statusBadge.setBackgroundColor(Color.parseColor("#4CAF50"));
    holder.statusText.setText("Còn " + daysUntilExpiry + " ngày");
} else if (daysUntilExpiry >= 1) {
    // 🟡 Vàng — Sắp hết hạn
    holder.statusBadge.setBackgroundColor(Color.parseColor("#FFC107"));
    holder.statusText.setText("Sắp hết hạn!");
} else {
    // 🔴 Đỏ — Đã hết hạn
    holder.statusBadge.setBackgroundColor(Color.parseColor("#F44336"));
    holder.statusText.setText("ĐÃ HẾT HẠN");
}
```

### 2.7 Dependencies

```groovy
// CameraX
implementation 'androidx.camera:camera-core:1.3.4'
implementation 'androidx.camera:camera-camera2:1.3.4'
implementation 'androidx.camera:camera-lifecycle:1.3.4'
implementation 'androidx.camera:camera-view:1.3.4'
// ML Kit Barcode
implementation 'com.google.mlkit:barcode-scanning:17.3.0'
// Gemini AI
implementation 'com.google.ai.client.generativeai:generativeai:0.9.0'
// JSON parsing
implementation 'com.google.code.gson:gson:2.11.0'
```

### 2.8 Lưu ý quan trọng cho dev
- **Camera Permission**: Phải xin quyền `CAMERA` runtime. Nếu user từ chối → hiển thị dialog giải thích + mở Settings.
- **Gemini API Key**: KHÔNG hardcode trong code. Dùng `local.properties` + `BuildConfig.GEMINI_API_KEY`.
- **Ảnh quá lớn**: Resize Bitmap xuống max 1024x1024 trước khi encode Base64, tránh request quá 4MB.
- **Lỗi JSON từ Gemini**: Gemini đôi khi trả markdown (```json ... ```) thay vì pure JSON → cần strip markdown wrapper trước khi parse.

---

## MODULE 3: 🍽️ GỢI Ý THỰC ĐƠN THÔNG MINH (AI MEAL PLANNER)

### 3.1 Mục đích
Sử dụng AI để tự động lên thực đơn 7 ngày (mỗi ngày 3 bữa) dựa trên nguyên liệu đang có trong kho + mục tiêu sức khỏe của user. Ưu tiên nguyên liệu sắp hết hạn.

### 3.2 Danh sách màn hình

| # | Tên màn hình | Mô tả |
|:-:|:------------|:------|
| 1 | `MealPlanWeekActivity` | Lịch 7 ngày (Thứ 2→CN), mỗi ngày hiện 3 card (Sáng/Trưa/Tối) với tên món + kcal tổng. Nút "🤖 Lên thực đơn bằng AI" ở trên cùng |
| 2 | `MealDetailActivity` | Chi tiết 1 bữa ăn: tên món, danh sách nguyên liệu + định lượng, công thức nấu step-by-step, tổng calo/protein |

### 3.3 Cấu trúc Firestore Database

```
users/{uid}/mealPlans (sub-collection)
└── {weekId} (document)     // VD: "2026-W21" (tuần 21 năm 2026)
    ├── createdAt: Timestamp
    ├── startDate: "2026-05-19"
    ├── endDate: "2026-05-25"
    └── days: [              // Array of 7 days
        {
            "date": "2026-05-19",
            "dayOfWeek": "Thứ Hai",
            "meals": {
                "breakfast": {
                    "name": "Cháo gà rau củ",
                    "totalCalories": 350,
                    "totalProtein": 25,
                    "ingredients": [
                        {"name": "Ức gà", "amount": "100g"},
                        {"name": "Gạo", "amount": "50g"},
                        {"name": "Cà rốt", "amount": "30g"}
                    ],
                    "recipe": "1. Nấu gạo với 500ml nước...\n2. Xé ức gà nhỏ...\n3. ..."
                },
                "lunch": { ... },
                "dinner": { ... }
            }
        },
        // ... 6 ngày còn lại
    ]
```

### 3.4 Luồng hoạt động: Lên thực đơn AI

```
[User nhấn "Lên thực đơn bằng AI"]
    → App hiện Loading Dialog "AI đang suy nghĩ..."
    → Query Firestore: users/{uid}/pantry (WHERE status != "expired")
    → Sort theo expiryDate ASC (sắp hết hạn lên trước)
    → Query Firestore: users/{uid} (lấy calorieTarget, goal)
    → Ghép thành prompt gửi Gemini:

    ┌──────────────────────────────────────────────────┐
    │ PROMPT GỬI GEMINI:                               │
    │ "Bạn là đầu bếp dinh dưỡng chuyên nghiệp.       │
    │                                                  │
    │ Nguyên liệu hiện có (ưu tiên dùng trước):       │
    │ - Ức gà: 500g (hết hạn 2 ngày nữa) ⚠️           │
    │ - Gạo: 1000g                                     │
    │ - Cà rốt: 200g                                   │
    │ - Trứng gà: 10 quả                               │
    │                                                  │
    │ Mục tiêu sức khỏe: Giảm cân                     │
    │ Calo mục tiêu mỗi ngày: 2024 kcal               │
    │                                                  │
    │ Hãy lên thực đơn 7 ngày, mỗi ngày 3 bữa         │
    │ (Sáng/Trưa/Tối). Ưu tiên sử dụng nguyên liệu   │
    │ có dấu ⚠️ trước.                                 │
    │                                                  │
    │ Trả về ĐÚNG JSON format: { days: [...] }         │
    │ (format chi tiết ở system prompt)"               │
    └──────────────────────────────────────────────────┘

    → Gemini trả JSON → Parse → Lưu vào Firestore mealPlans/{weekId}
    → Hiển thị lên Calendar UI
    → Dismiss Loading Dialog
```

### 3.5 Thiết kế Calendar UI

```
┌─────────────────────────────────────────┐
│  ← Tuần 19/05 - 25/05/2026 →           │  (ViewPager swipe tuần)
├─────────────────────────────────────────┤
│  [🤖 Lên thực đơn bằng AI]             │  (Button nổi bật)
├──────┬──────┬──────┬──────┬──────┬──────┤
│ T.Hai│ T.Ba │ T.Tư │ T.Năm│ T.Sáu│ T.Bảy│ CN   (Tab hoặc HorizontalScroll)
├──────┴──────┴──────┴──────┴──────┴──────┤
│ 🌅 Sáng: Cháo gà rau củ    350 kcal    │  (Card clickable → MealDetail)
│ 🌞 Trưa: Cơm gà xào rau   580 kcal    │
│ 🌙 Tối:  Salad ức gà       420 kcal    │
│ ────────────────────────────            │
│ Tổng ngày: 1350 / 2024 kcal            │  (ProgressBar)
└─────────────────────────────────────────┘
```

### 3.6 Dependencies

```groovy
// Gemini AI (dùng chung với Module 2)
implementation 'com.google.ai.client.generativeai:generativeai:0.9.0'
// Firestore (dùng chung)
implementation 'com.google.firebase:firebase-firestore'
// Gson (dùng chung)
implementation 'com.google.code.gson:gson:2.11.0'
```

### 3.7 Lưu ý quan trọng cho dev
- **Prompt Engineering là chìa khóa**: Nếu prompt không rõ ràng, Gemini sẽ trả JSON sai format → crash. Luôn dùng `try-catch` khi parse + có UI fallback "Không thể tạo thực đơn, thử lại".
- **Quota Gemini Free Tier**: 15 RPM (requests/minute), 1M tokens/day. Đủ dùng cho dev nhưng nếu test spam sẽ bị rate limit.
- **Phối hợp Module 2**: Module 3 ĐỌC dữ liệu từ collection `pantry` của Module 2. Hai dev phải thống nhất schema từ đầu.
- **Cache thực đơn**: Không gọi AI mỗi lần mở app. Chỉ gọi khi user bấm nút. Thực đơn đã tạo lưu Firestore và load lại bình thường.
# 📖 PHÂN TÍCH CHI TIẾT MODULE 4–5

---

## MODULE 4: ⏰ THEO DÕI SINH HỌC & NHẮC NHỞ

### 4.1 Mục đích
Giúp người dùng duy trì thói quen sức khỏe hàng ngày thông qua hệ thống nhắc nhở tự động (uống nước, uống vitamin) và dashboard ghi chép thói quen (số cốc nước, giờ ngủ).

### 4.2 Danh sách màn hình

| # | Tên màn hình | Mô tả |
|:-:|:------------|:------|
| 1 | `HabitDashboardActivity` | Dashboard chính: CircularProgress uống nước (VD: 5/8 cốc) + Giờ ngủ hôm nay + Nút "+1 cốc" |
| 2 | `ReminderSettingsActivity` | Cài đặt: Bật/Tắt nhắc nước (chọn interval: mỗi 1h/2h/3h) + Bật/Tắt nhắc vitamin (chọn giờ cố định) + Chọn giờ bắt đầu/kết thúc |

### 4.3 Cấu trúc Firestore Database

```
users/{uid}/habits (sub-collection)
└── {date} (document)           // VD: "2026-05-21"
    ├── waterCups: 5             // Số cốc nước đã uống
    ├── waterGoal: 8             // Mục tiêu cốc nước/ngày
    ├── sleepHours: 7.5          // Số giờ ngủ (user tự nhập)
    ├── vitaminTaken: true       // Đã uống vitamin chưa
    └── updatedAt: Timestamp
```

**Cài đặt nhắc nhở (lưu local bằng SharedPreferences, KHÔNG lưu Firestore)**:
```
KEY: "water_reminder_enabled" → true/false
KEY: "water_interval_hours"   → 2 (mỗi 2 tiếng)
KEY: "water_start_hour"       → 8 (bắt đầu từ 8:00)
KEY: "water_end_hour"         → 22 (kết thúc lúc 22:00)
KEY: "vitamin_reminder_enabled" → true/false
KEY: "vitamin_hour"           → 7 (nhắc lúc 7:00 sáng)
```

### 4.4 Luồng hoạt động: Nhắc nhở (AlarmManager)

```
[User bật nhắc nhở uống nước, interval = 2h, từ 8:00 → 22:00]
    → App tạo các Alarm:
        Alarm 1: 08:00
        Alarm 2: 10:00
        Alarm 3: 12:00
        Alarm 4: 14:00
        Alarm 5: 16:00
        Alarm 6: 18:00
        Alarm 7: 20:00
        Alarm 8: 22:00
    → Mỗi Alarm:
        AlarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            PendingIntent → WaterReminderReceiver.class
        )

[Đến giờ hẹn] → Android kích hoạt WaterReminderReceiver (extends BroadcastReceiver)
    → onReceive():
        1. Tạo NotificationChannel (nếu Android 8+)
        2. Build Notification:
            - Title: "💧 Đã đến giờ uống nước!"
            - Body: "Bạn đã uống 5/8 cốc hôm nay. Tiếp tục nhé!"
            - Action button: "Đã uống" → update Firestore waterCups += 1
        3. NotificationManager.notify(notificationId, notification)
```

### 4.5 Code mẫu: Đặt Alarm

```java
// Đặt 1 alarm nhắc nước
private void setWaterAlarm(Context context, int hour, int minute, int requestCode) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    Intent intent = new Intent(context, WaterReminderReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    // Set thời gian trigger
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);

    // Nếu thời gian đã qua → đặt cho ngày mai
    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_YEAR, 1);
    }

    // setExactAndAllowWhileIdle: hoạt động cả khi Doze mode
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.getTimeInMillis(),
        pendingIntent
    );
}
```

### 4.6 Code mẫu: BroadcastReceiver + Notification

```java
public class WaterReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Tạo Notification Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "water_channel",
                "Nhắc uống nước",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhắc nhở uống nước định kỳ");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // 2. Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "water_channel")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("💧 Đã đến giờ uống nước!")
            .setContentText("Uống 1 cốc nước để giữ sức khỏe nhé!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        // 3. Hiển thị
        NotificationManagerCompat.from(context).notify(1001, builder.build());

        // 4. Đặt lại alarm cho lần tiếp theo (repeating thủ công)
        // ... gọi lại setWaterAlarm() cho giờ tiếp theo
    }
}
```

### 4.7 AndroidManifest.xml cần thêm

```xml
<!-- Quyền đặt alarm chính xác (Android 12+) -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<!-- Quyền hiển thị notification (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<!-- Nhận lại alarm sau khi khởi động lại máy -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<receiver android:name=".receivers.WaterReminderReceiver" android:exported="false" />
<receiver android:name=".receivers.BootReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

### 4.8 Dependencies

```groovy
// Chỉ cần Firebase Firestore (dùng chung) — không cần thêm thư viện đặc biệt
implementation 'com.google.firebase:firebase-firestore'
```

### 4.9 Lưu ý quan trọng cho dev
- **Không dùng `setRepeating()`**: Từ Android 4.4+, `setRepeating()` không chính xác. Dùng `setExactAndAllowWhileIdle()` rồi tự đặt lại alarm tiếp theo trong `onReceive()`.
- **Boot Receiver**: Khi restart điện thoại, mọi alarm bị hủy. Cần `BootReceiver` để đặt lại tất cả alarm khi máy khởi động.
- **Android 13+ cần xin quyền Notification**: `POST_NOTIFICATIONS` phải xin runtime, không tự có.

---

## MODULE 5: 📊 THỐNG KÊ THỂ TRẠNG & CÀI ĐẶT

### 5.1 Mục đích
Tổng hợp dữ liệu từ các module khác để hiển thị biểu đồ trực quan (cân nặng theo thời gian, calo nạp vào mỗi ngày). Cho phép xuất báo cáo PDF, thay đổi ngôn ngữ và theme.

### 5.2 Danh sách màn hình

| # | Tên màn hình | Mô tả |
|:-:|:------------|:------|
| 1 | `AnalyticsActivity` | 2 biểu đồ: LineChart (cân nặng) + BarChart (calo). Toggle tuần/tháng. Nút "Xuất PDF" |
| 2 | `SettingsActivity` | Dropdown ngôn ngữ (Tiếng Việt / English) + Switch Dark/Light Mode + Toggle Offline Sync + Nút Đăng xuất + Phiên bản app |

### 5.3 Nguồn dữ liệu (ĐỌC từ Firestore của module khác)

```
Biểu đồ cân nặng ← users/{uid}/weightLog/{date}    (Module 1 ghi)
Biểu đồ calo    ← users/{uid}/mealPlans/{weekId}    (Module 3 ghi)
                   + users/{uid}/pantry/{itemId}     (Module 2 ghi)
```

### 5.4 Vẽ biểu đồ với MPAndroidChart

```java
// === LineChart: Cân nặng theo tuần ===
private void drawWeightChart(List<WeightEntry> entries) {
    List<Entry> chartEntries = new ArrayList<>();
    List<String> labels = new ArrayList<>();

    for (int i = 0; i < entries.size(); i++) {
        chartEntries.add(new Entry(i, entries.get(i).getWeightKg()));
        labels.add(entries.get(i).getDate()); // "21/05"
    }

    LineDataSet dataSet = new LineDataSet(chartEntries, "Cân nặng (kg)");
    dataSet.setColor(Color.parseColor("#4CAF50"));
    dataSet.setCircleColor(Color.parseColor("#388E3C"));
    dataSet.setLineWidth(2f);
    dataSet.setCircleRadius(4f);
    dataSet.setValueTextSize(10f);
    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Đường cong mượt

    LineData lineData = new LineData(dataSet);
    lineChart.setData(lineData);

    // Format trục X thành ngày tháng
    XAxis xAxis = lineChart.getXAxis();
    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setGranularity(1f);

    lineChart.getDescription().setEnabled(false);
    lineChart.animateX(1000); // Animation 1 giây
    lineChart.invalidate();
}

// === BarChart: Calo mỗi ngày ===
private void drawCalorieChart(List<DailyCalorie> data, float targetCalorie) {
    List<BarEntry> barEntries = new ArrayList<>();
    for (int i = 0; i < data.size(); i++) {
        barEntries.add(new BarEntry(i, data.get(i).getTotalCalories()));
    }

    BarDataSet dataSet = new BarDataSet(barEntries, "Calo nạp vào (kcal)");
    dataSet.setColor(Color.parseColor("#FF9800"));

    BarData barData = new BarData(dataSet);
    barChart.setData(barData);

    // Vẽ đường mục tiêu calo (LimitLine)
    LimitLine target = new LimitLine(targetCalorie, "Mục tiêu: " + targetCalorie + " kcal");
    target.setLineColor(Color.RED);
    target.setLineWidth(2f);
    barChart.getAxisLeft().addLimitLine(target);

    barChart.invalidate();
}
```

### 5.5 Xuất báo cáo PDF

```java
private void exportPdf(List<WeightEntry> weights, List<DailyCalorie> calories) {
    // 1. Tạo PDF Document
    PdfDocument document = new PdfDocument();
    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
    PdfDocument.Page page = document.startPage(pageInfo);
    Canvas canvas = page.getCanvas();

    // 2. Vẽ nội dung
    Paint titlePaint = new Paint();
    titlePaint.setTextSize(24);
    titlePaint.setFakeBoldText(true);
    canvas.drawText("BÁO CÁO DINH DƯỠNG TUẦN", 50, 60, titlePaint);

    Paint bodyPaint = new Paint();
    bodyPaint.setTextSize(14);

    int y = 120;
    canvas.drawText("LỊCH SỬ CÂN NẶNG:", 50, y, titlePaint);
    y += 30;
    for (WeightEntry w : weights) {
        canvas.drawText(w.getDate() + ": " + w.getWeightKg() + " kg", 70, y, bodyPaint);
        y += 25;
    }

    // ... vẽ thêm bảng calo, mục tiêu, v.v.

    document.finishPage(page);

    // 3. Lưu file
    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File pdfFile = new File(downloadsDir, "BaoCao_" + LocalDate.now() + ".pdf");
    document.writeTo(new FileOutputStream(pdfFile));
    document.close();

    // 4. Mở file bằng FileProvider
    Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(uri, "application/pdf");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    startActivity(intent);
}
```

### 5.6 Đa ngôn ngữ (Localization)

```
Cấu trúc thư mục:
res/
├── values/
│   └── strings.xml          ← Tiếng Anh (mặc định)
└── values-vi/
    └── strings.xml          ← Tiếng Việt
```

```xml
<!-- values/strings.xml (English) -->
<string name="app_name">AI Wellness</string>
<string name="water_reminder_title">Time to drink water!</string>
<string name="weight_label">Weight</string>

<!-- values-vi/strings.xml (Vietnamese) -->
<string name="app_name">AI Wellness</string>
<string name="water_reminder_title">Đã đến giờ uống nước!</string>
<string name="weight_label">Cân nặng</string>
```

```java
// Chuyển đổi ngôn ngữ runtime
private void setLocale(String langCode) { // "vi" hoặc "en"
    Locale locale = new Locale(langCode);
    Locale.setDefault(locale);

    Configuration config = new Configuration();
    config.setLocale(locale);
    getResources().updateConfiguration(config, getResources().getDisplayMetrics());

    // Lưu lựa chọn
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.edit().putString("language", langCode).apply();

    // Restart activity để áp dụng
    recreate();
}
```

### 5.7 Theme Sáng/Tối

```java
// Chuyển Dark/Light Mode
private void setTheme(boolean isDark) {
    if (isDark) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Lưu preference
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.edit().putBoolean("dark_mode", isDark).apply();
}

// Gọi trong Application.onCreate() để khôi phục theme khi mở app
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
```

### 5.8 Offline Sync (Firestore Persistence)

```java
// Trong Application.onCreate() — CHỈ CẦN 1 DÒNG
FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)    // ← Dòng quan trọng nhất
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build();
FirebaseFirestore.getInstance().setFirestoreSettings(settings);

// Sau khi bật:
// - Mất mạng → user vẫn thêm thực phẩm, log cân nặng bình thường (lưu local SQLite)
// - Có mạng lại → Firebase SDK tự đồng bộ lên Cloud, KHÔNG cần viết code sync thủ công
```

### 5.9 Dependencies

```groovy
// Biểu đồ
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
// Firestore (dùng chung)
implementation 'com.google.firebase:firebase-firestore'
// SharedPreferences mặc định (có sẵn trong Android SDK, không cần thêm)
```

### 5.10 Lưu ý quan trọng cho dev
- **MPAndroidChart**: Thêm `maven { url 'https://jitpack.io' }` vào `settings.gradle` > `repositories`. Nếu thiếu → Gradle không tìm thấy thư viện.
- **Đa ngôn ngữ cần phối hợp 5 người**: MỌI text hiển thị trong app phải dùng `@string/key_name`, KHÔNG hardcode tiếng Việt. Nếu module khác hardcode → đa ngôn ngữ sẽ bị thiếu sót.
- **PDF Canvas**: Tọa độ `(0,0)` ở góc trái trên. Phải tự tính `y` cho từng dòng. Nếu quá nhiều data → cần tạo page thứ 2.
- **FileProvider**: Cần khai báo trong `AndroidManifest.xml` và tạo file `res/xml/file_paths.xml`. Quên bước này = crash khi mở PDF.

---

## 🔗 MA TRẬN PHỤ THUỘC GIỮA CÁC MODULE

```
Module 1 (Auth)  ──writes──→  users/{uid}           ←──reads──  Module 3, 5
                 ──writes──→  users/{uid}/weightLog  ←──reads──  Module 5

Module 2 (Pantry) ──writes──→  users/{uid}/pantry   ←──reads──  Module 3, 5

Module 3 (Meal)  ──writes──→  users/{uid}/mealPlans ←──reads──  Module 5

Module 4 (Habit) ──writes──→  users/{uid}/habits    ←──reads──  Module 5

Module 5 (Stats) ──reads all── (không ghi dữ liệu mới, chỉ đọc + tổng hợp + xuất PDF)
```

> **Quy tắc vàng**: Mỗi module **CHỈ GHI vào collection của mình**. Module 5 là module "read-only" — chỉ đọc và tổng hợp dữ liệu từ 4 module khác.

### Thứ tự phát triển khuyến nghị

```
Tuần 1: Module 1 (Auth) → phải xong trước vì tất cả module khác cần UID
Tuần 2: Module 2 (Pantry) + Module 4 (Habit) → song song, không phụ thuộc nhau
Tuần 3: Module 3 (Meal AI) → cần data từ Module 2
Tuần 4: Module 5 (Stats) → cần data từ Module 1,2,3,4 → làm cuối cùng
```
