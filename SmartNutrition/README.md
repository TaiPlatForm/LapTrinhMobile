# 🥗 Smart Nutrition

**Ứng dụng Quản lý Dinh dưỡng Cá nhân với AI**

Android Native (Kotlin + Jetpack Compose) + Firebase

---

## 🚀 HƯỚNG DẪN CLONE VÀ CHẠY

### Bước 1: Clone project
```bash
git clone <URL_REPO>
```

### Bước 2: Mở trong Android Studio
1. Mở Android Studio → **File > Open** → chọn thư mục `SmartNutrition/`
2. Android Studio sẽ tự động sync Gradle (chờ 2-3 phút lần đầu)
3. Nếu báo lỗi Gradle Wrapper → **File > Invalidate Caches > Restart**

### Bước 3: Thêm Firebase config
1. Nhận file `google-services.json` từ nhóm trưởng
2. Copy vào thư mục `app/` (cùng cấp với `build.gradle.kts`)

### Bước 4: Thêm Gemini API Key (chỉ TV2, TV3 cần)
1. Mở file `local.properties` (ở thư mục gốc project)
2. Thêm dòng: `GEMINI_API_KEY=your_api_key_here`
3. **KHÔNG push file này lên GitHub**

### Bước 5: Chạy app
1. Kết nối điện thoại Android hoặc mở Emulator
2. Nhấn ▶️ Run

---

## 📦 CẤU TRÚC PROJECT

```
app/src/main/java/com/team/smartnutrition/
├── MainActivity.kt              ← Entry point (KHÔNG SỬA)
├── SmartNutritionApp.kt         ← Application class (KHÔNG SỬA)
│
├── navigation/                  ← Điều hướng (KHÔNG SỬA trừ khi thêm route)
│   ├── Screen.kt                ← Định nghĩa routes
│   ├── BottomNavBar.kt          ← Bottom Navigation Bar
│   └── SmartNutritionNavHost.kt ← Navigation graph
│
├── ui/theme/                    ← Design System (KHÔNG SỬA)
│   ├── Color.kt                 ← Bảng màu Emerald Green
│   ├── Type.kt                  ← Typography
│   └── Theme.kt                 ← Material 3 Theme
│
├── common/components/           ← UI components dùng chung
│   └── CommonComponents.kt      ← TopBar, Loading, Error, Button...
│
├── auth/          ← 🔑 TV1 code ở đây
├── pantry/        ← 📸 TV2 code ở đây
├── meal/          ← 🍽️ TV3 code ở đây
├── habit/         ← ⏰ TV4 code ở đây
└── analytics/     ← 📊 TV5 code ở đây
```

---

## 👥 PHÂN CÔNG MODULE

| TV  | Module             | Package    | Độ khó |
|-----|--------------------|------------|--------|
| TV1 | Auth & Profile     | `auth/`    | ⭐⭐     |
| TV2 | Pantry Scanner     | `pantry/`  | ⭐⭐⭐⭐   |
| TV3 | AI Meal Planner    | `meal/`    | ⭐⭐⭐    |
| TV4 | Habit & Reminder   | `habit/`   | ⭐⭐⭐    |
| TV5 | Analytics & Settings | `analytics/` | ⭐⭐ |

---

## ⚠️ QUY TẮC LÀM VIỆC

### 1. CHỈ SỬA FILE TRONG PACKAGE CỦA MÌNH
- TV1 chỉ sửa files trong `auth/`
- TV2 chỉ sửa files trong `pantry/`
- v.v.
- **KHÔNG sửa file trong `navigation/`, `ui/theme/`, `common/`** trừ khi thông báo cả nhóm

### 2. GIT WORKFLOW
```bash
# Lần đầu sau khi clone:
git checkout -b feature/module-X    # X = số module của bạn

# Mỗi khi code xong 1 phần:
git add .
git commit -m "feat(module-X): mô tả ngắn"
git push origin feature/module-X

# Khi muốn merge vào develop:
# Tạo Pull Request trên GitHub
```

### 3. DÙNG THEME, KHÔNG HARDCODE MÀU
```kotlin
// ❌ SAI - Không làm thế này:
Text(color = Color(0xFF10B981))

// ✅ ĐÚNG - Dùng theme:
Text(color = MaterialTheme.colorScheme.primary)
```

### 4. DÙNG STRINGS.XML, KHÔNG HARDCODE TEXT
```kotlin
// ❌ SAI:
Text("Đăng nhập")

// ✅ ĐÚNG:
Text(stringResource(R.string.login_button))
```

### 5. NAVIGATE BẰNG SCREEN OBJECT
```kotlin
// ❌ SAI:
navController.navigate("home")

// ✅ ĐÚNG:
navController.navigate(Screen.Home.route)
```

---

## 🔗 FIRESTORE SCHEMA

```
users/{uid}
├── email, displayName, gender, birthYear
├── heightCm, weightKg, activityLevel, goal
├── bmi, bmr, tdee, proteinTarget, carbTarget, fatTarget, calorieTarget
│
├── weightLog/{date}        ← TV1 ghi, TV5 đọc
│   └── weightKg, bmi, loggedAt
│
├── pantry/{autoId}         ← TV2 ghi, TV3+TV5 đọc
│   └── name, caloriesPer100g, proteinPer100g, quantityGrams, expiryDate, status
│
├── mealPlans/{weekId}      ← TV3 ghi, TV5 đọc
│   └── days[7] → meals{breakfast, lunch, dinner}
│
└── habits/{date}           ← TV4 ghi, TV5 đọc
    └── waterCups, waterGoal, sleepHours, vitaminTaken
```

**QUY TẮC: Mỗi module CHỈ GHI vào collection của mình. Module 5 CHỈ ĐỌC.**

---

## 📅 TIMELINE (3 tuần)

| Tuần | TV1 | TV2 | TV3 | TV4 | TV5 |
|------|-----|-----|-----|-----|-----|
| 1 | ✅ Auth xong | Camera + Barcode setup | Meal UI + mock data | Habit UI | Hỗ trợ team |
| 2 | Weight Log | Pantry hoàn thiện | Gemini API | AlarmManager + Notif | Charts UI |
| 3 | Test + fix | Test + fix | Tích hợp pantry data | Test + fix | PDF + Settings |

---

## 🛠️ TECH STACK

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Navigation**: Compose Navigation
- **Database**: Cloud Firestore (+ Offline Persistence)
- **Auth**: Firebase Auth + Google Credential Manager
- **AI**: Gemini AI SDK (Vision + Text)
- **Camera**: CameraX
- **Barcode**: ML Kit Barcode Scanning
- **Charts**: Vico (Compose-native) hoặc MPAndroidChart
- **Image Loading**: Coil
- **JSON**: Gson
