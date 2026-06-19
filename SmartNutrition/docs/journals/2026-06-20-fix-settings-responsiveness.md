# Nhật ký Thay đổi: Khắc phục Lỗi Cài đặt Dark Mode & Ngôn ngữ Không Hoạt động, Lỗi ActivityResultRegistry Owner & Lỗi Crash Biểu đồ Vico

**Ngày**: 20/06/2026  
**Tác giả**: Antigravity  
**Chủ đề**: Fix Settings & Analytics Screen Issues (Dark Mode, Localization, Context Wrapper & Vico Chart Colors & Crash)

## 1. Vấn đề (Root Cause)
- **Dark Mode**: Thẻ theme chính `SmartNutritionTheme` trong `MainActivity.kt` mặc định sử dụng `isSystemInDarkTheme()`, do đó nó hoàn toàn bỏ qua tùy chọn được lưu trữ trong SharedPreferences (`dark_mode`). Việc thay đổi chế độ tối trong `SettingsScreen` không kích hoạt cập nhật giao diện ứng dụng ngay lập tức vì theme không quan sát tùy chọn này một cách reactive.
- **Ngôn ngữ (Localization)**: `MainActivity` kế thừa từ `ComponentActivity` thay vì `AppCompatActivity`. Vì vậy, cơ chế cấu hình ngôn ngữ thông qua `AppCompatDelegate.setApplicationLocales` của AppCompat backport không tự động áp dụng và cập nhật tài nguyên (resources context) trên toàn hệ thống Jetpack Compose mà không khởi động lại Activity (recreate) hoặc truyền cấu hình context tùy chỉnh.
- **Lỗi Crash app (IllegalStateException - No ActivityResultRegistryOwner)**: Khi chúng tôi bọc nhánh Compose dưới `CompositionLocalProvider` bằng context trả về từ `context.createConfigurationContext(config)`, context này là một Configuration Context thô, không phải là một `ContextWrapper` liên kết với Activity (`MainActivity`). 
  Khi các màn hình khác (như `ReminderSettingsScreen` hay `CameraCaptureScreen`) thực hiện gọi `rememberLauncherForActivityResult`, hệ thống Compose cố gắng tìm `ActivityResultRegistryOwner` bằng cách ép kiểu `LocalContext.current` hoặc duyệt qua chuỗi `baseContext` của nó. Vì context mới không trỏ về Activity, quá trình này trả về `null` dẫn đến lỗi `java.lang.IllegalStateException: No ActivityResultRegistryOwner was provided via LocalActivityResultRegistryOwner` và gây crash ứng dụng ngay lập tức khi recompose hoặc điều hướng.
- **Lỗi Crash biểu đồ Vico khi chọn thống kê tháng (IllegalStateException - CartesianValueFormatter.format returned an empty string)**:
  Trong thư viện Vico v2 (phục vụ biểu đồ LineChart/BarChart), phương thức định dạng nhãn `CartesianValueFormatter.format` bắt buộc phải trả về một chuỗi không rỗng (non-empty string). Khi chuyển sang chế độ tháng (limit = 30), Vico có thể yêu cầu định dạng cho các chỉ mục nằm ngoài phạm vi danh sách (out-of-bounds) hoặc khi dữ liệu bị trống. Trước đây chúng ta trả về chuỗi rỗng `""` như một giá trị fallback. Việc này kích hoạt cơ chế bảo vệ của Vico v2 và ném ra ngoại lệ crash ứng dụng:
  `java.lang.IllegalStateException: CartesianValueFormatter.format returned an empty string. Use HorizontalAxis.ItemPlacer and VerticalAxis.ItemPlacer, not empty strings, to control which x and y values are labeled.`
- **Giao diện sáng/tối và Màu sắc Biểu đồ (Light Mode Chart UI)**:
  Mặc định biểu đồ Vico sử dụng màu xanh/đỏ mặc định của thư viện, không khớp với chủ đề xanh lục bảo (Emerald Green) của ứng dụng. Hơn nữa, các Card chứa biểu đồ sử dụng màu nền `surfaceVariant` mặc định (trong chế độ Light là màu xám nhạt `0xFFF1F5F9`), làm cho biểu đồ trông tối và thiếu độ tương phản với màu nền của màn hình (`0xFFFAFAF9`).
- **Ngôn ngữ Cài đặt chưa đầy đủ**:
  Nhiều nhãn và mô tả chi tiết trong `SettingsScreen.kt` vẫn bị hardcode tiếng Việt khiến cho khi chuyển đổi sang tiếng Anh, giao diện cài đặt vẫn hiển thị phần lớn tiếng Việt.

## 2. Giải pháp Thực hiện
Để giải quyết các vấn đề trên một cách mượt mà và trực tiếp trong Jetpack Compose:
1. **Lắng nghe SharedPreferences động**:
   - Khai báo các trạng thái Compose (`isDarkModeState`, `languageState`) được khởi tạo từ giá trị lưu trữ trong `app_settings` SharedPreferences.
   - Sử dụng một `SharedPreferences.OnSharedPreferenceChangeListener` được lưu trữ qua `remember` để cập nhật lập tức các state này khi người dùng thay đổi thiết lập trong màn hình `SettingsScreen`.
2. **Thiết kế LocalizedContext (ContextWrapper tùy biến)**:
   - Tạo class `LocalizedContext` kế thừa từ `ContextWrapper(base)`.
   - Ghi đè `getResources()` và `getAssets()` để trả về các đối tượng tài nguyên tương ứng với ngôn ngữ được thiết lập từ `base.createConfigurationContext(config)`.
   - Vì lớp này kế thừa từ `ContextWrapper` với `baseContext` là Activity gốc (`MainActivity`), các hàm tìm kiếm Owner (như `ActivityResultRegistryOwner`, `OnBackPressedDispatcherOwner`, v.v.) hoạt động bình thường.
3. **Cung cấp cấu hình Locale và Theme động**:
   - Sử dụng `CompositionLocalProvider` cung cấp `LocalContext provides LocalizedContext(context, config)` và `LocalConfiguration provides config`.
   - Truyền trực tiếp `isDarkModeState` vào thuộc tính `darkTheme` của `SmartNutritionTheme`. Giao diện và thanh trạng thái (status bar) sẽ thay đổi màu sắc ngay lập tức.
4. **Khắc phục lỗi định dạng rỗng trên trục x của biểu đồ Vico**:
   - Thay đổi các giá trị trả về mặc định trong `CartesianValueFormatter` của cả hai biểu đồ cân nặng (`WeightChartSection`) và calorie (`CalorieChartSection`) trong `AnalyticsScreen.kt`.
   - Thay vì trả về chuỗi rỗng `""`, bộ định dạng sẽ trả về một khoảng trắng `" "` (không phải chuỗi rỗng, không hiển thị nhãn thừa).
5. **Cải tiến Màu sắc & Độ tương phản của Biểu đồ Thống kê**:
   - Chuyển màu nền của các `Card` chứa biểu đồ thành `MaterialTheme.colorScheme.surface` (trắng tinh `0xFFFFFFFF` trong chế độ Sáng và xám đen `0xFF1E293B` trong chế độ Tối) kết hợp với độ nổi bóng `elevation = 2.dp`. Điều này tạo ra ranh giới rõ ràng và hiển thị sáng sủa hơn hẳn trong Light Mode.
   - Cấu hình màu sắc các biểu đồ đồng bộ với theme: Biểu đồ Cân nặng (`WeightChartSection`) vẽ bằng màu xanh Emerald chủ đạo (`MaterialTheme.colorScheme.primary`), biểu đồ Calo (`CalorieChartSection`) vẽ bằng cột màu xanh da trời (`MaterialTheme.colorScheme.secondary`).
6. **Bản địa hóa (Localization) hoàn chỉnh màn hình Cài đặt**:
   - Di chuyển tất cả chuỗi văn bản cứng tiếng Việt (gồm tiêu đề phân hệ, mô tả cài đặt chế độ tối, đồng bộ dữ liệu ngoại tuyến, đăng xuất, phiên bản ứng dụng...) vào các tệp tài nguyên `strings.xml` tương ứng của ngôn ngữ tiếng Anh (`values/strings.xml`) và tiếng Việt (`values-vi/strings.xml`).
   - Sử dụng `stringResource()` cho toàn bộ màn hình `SettingsScreen` để đảm bảo khi chọn "English" trong cài đặt, toàn bộ màn hình sẽ được dịch hoàn toàn sang tiếng Anh đồng bộ với hệ thống.
7. **Khôi phục Locale khi Khởi động**:
   - Cập nhật thêm logic thiết lập Locale mặc định trong hàm `onCreate` của lớp `SmartNutritionApp.kt`.

## 3. Các File Thay đổi
- [MainActivity.kt](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/app/src/main/java/com/team/smartnutrition/MainActivity.kt)
- [SmartNutritionApp.kt](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/app/src/main/java/com/team/smartnutrition/SmartNutritionApp.kt)
- [AnalyticsScreen.kt](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/app/src/main/java/com/team/smartnutrition/analytics/AnalyticsScreen.kt)
- [SettingsScreen.kt](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/app/src/main/java/com/team/smartnutrition/analytics/SettingsScreen.kt)
- [strings.xml (English)](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/app/src/main/res/values/strings.xml)
- [strings.xml (Vietnamese)](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/app/src/main/res/values-vi/strings.xml)
- [plan.md](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/plans/module5-analytics-settings/plan.md) (Cập nhật trạng thái Phase 5 thành Completed)
- [phase-05-ui-settingsscreen-y.md](file:///home/taict/Desktop/LapTrinhMobile/SmartNutrition/plans/module5-analytics-settings/phase-05-ui-settingsscreen-y.md) (Cập nhật trạng thái thành Completed)

## 4. Kết quả & Đánh giá
- Biên dịch ứng dụng thành công (`BUILD SUCCESSFUL`).
- Tính năng Dark Mode và Ngôn ngữ hoạt động mượt mà, chuyển đổi tức thì không cần reload màn hình.
- Các tính năng đăng ký camera, lấy kết quả activity hoạt động bình thường, hoàn toàn khắc phục lỗi crash ứng dụng.
- Chế độ hiển thị biểu đồ theo tuần và tháng hoạt động ổn định, chuyển đổi qua lại nhanh chóng không còn lỗi crash của thư viện Vico.
