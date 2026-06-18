# 🎨 PROMPT THIẾT KẾ GIAO DIỆN (UI/UX DESIGN PROMPT)
## 📱 Dự án: Quản lý Dinh dưỡng Cá nhân với AI (Smart Nutrition)

Bạn có thể copy nội dung dưới đây để gửi cho các AI tạo code UI/UX (như **v0.dev**, **Stitch**, **bolt.new**, hoặc **Claude**) để tạo nhanh bản mockup giao diện HTML/TailwindCSS hoặc Jetpack Compose nhằm đánh giá trực quan.

---

```text
Chủ đề: Thiết kế giao diện ứng dụng di động (Mobile App) "Quản lý Dinh dưỡng Cá nhân với AI (Smart Nutrition)"
Ngôn ngữ thiết kế: Hiện đại, sạch sẽ, chuẩn Y tế/Dinh dưỡng chủ động (Preventative Health), phong cách cao cấp (Premium Glassmorphism).
Bảng màu chính: 
- Màu nền: Slate/Dark Mode (hoặc màu kem sữa nhẹ dịu nếu là Light Mode)
- Màu nhấn (Accent): Emerald Green (Xanh ngọc lục bảo - tạo cảm giác tươi mát, sức khỏe)
- Các màu trạng thái: Cảnh báo hết hạn (Đỏ san hô, Vàng hổ phách), An toàn (Xanh lá nhạt).
Typography: Sử dụng font chữ Sans-serif hiện đại (Outfit hoặc Inter), hiển thị thông số rõ ràng.

YÊU CẦU: Hãy tạo giao diện Mockup cho ứng dụng di động bao gồm Thanh điều hướng dưới cùng (Bottom Navigation Bar) có 5 Tabs và thiết kế chi tiết 5 Màn hình sau:

---

### MÀN HÌNH 1: AUTH & PROFILE SETUP (Thiết lập thể trạng)
- Gồm 2 phần:
  1. Form Login/Register tối giản (Email, Password, Button Đăng nhập bằng Google).
  2. Màn hình Khảo sát chỉ số (Onboarding):
     - Các ô nhập dữ liệu trực quan: Tuổi (slider/number picker), Chiều cao (cm), Cân nặng (kg).
     - Lựa chọn giới tính (dạng Card lựa chọn có icon Nam/Nữ).
     - Mục tiêu sức khỏe: 3 thẻ card lớn có thể click chọn: "Tăng cơ (Bulking)", "Giảm mỡ (Shredding)", "Duy trì sức khỏe (Healthy)".

---

### MÀN HÌNH 2: PANTRY & INVENTORY (Kho thực phẩm ảo)
- Phần đầu trang: 
  - Ô tìm kiếm thực phẩm.
  - 2 nút bấm nổi bật có icon: [Chụp ảnh nhận diện AI] và [Quét mã vạch Barcode].
- Phần danh sách kho thực phẩm (Dùng Card Grid hoặc List):
  - Mỗi thẻ thực phẩm gồm: Ảnh minh họa nhỏ, Tên thực phẩm, Số lượng (Ví dụ: "3 quả", "500g"), Lượng Calo ước tính.
  - Huy hiệu (Badge) cảnh báo hạn sử dụng:
    - Màu Đỏ: "Hết hạn hôm nay!"
    - Màu Vàng: "Còn 2 ngày"
    - Màu Xanh: "Tươi ngon"

---

### MÀN HÌNH 3: MEAL PLANNER AI (Lịch ăn uống thông minh)
- Phần đầu trang:
  - Thanh chọn ngày trong tuần nằm ngang (Thứ 2 -> Chủ Nhật), ngày hiện tại được highlight màu Emerald Green.
- Phần thân trang:
  - Nút bấm lớn hiệu ứng gradient: "Lên thực đơn bằng AI" (kèm icon Sparkles lấp lánh).
  - Danh sách thực đơn trong ngày chia làm 3 thẻ lớn:
    - Bữa Sáng (Breakfast): Tên món ăn gợi ý, Lượng Calo, thời gian chuẩn bị.
    - Bữa Trưa (Lunch): Tên món ăn, danh sách nguyên liệu dùng từ Pantry.
    - Bữa Tối (Dinner): Tên món ăn, nút bấm "Xem công thức".
  - Khi click vào món ăn -> Hiển thị Modal/Popup chi tiết các bước nấu ăn do AI gợi ý.

---

### MÀN HÌNH 4: HABIT TRACKER & REMINDER (Theo dõi thói quen)
- Dashboard theo dõi trong ngày:
  - Vòng tròn tiến trình (Progress Circle) lượng nước uống: Ví dụ "1.2L / 2.0L", kèm nút "+" nhanh để thêm 250ml nước.
  - Thẻ theo dõi Giấc ngủ: hiển thị thanh tiến trình giờ ngủ đêm qua.
- Phần cài đặt nhắc nhở:
  - Danh sách các khung giờ nhắc nhở uống nước/uống vitamin dưới dạng Switch Button (gạt bật/tắt).
  - Nút thêm lịch nhắc nhở mới.

---

### MÀN HÌNH 5: ANALYTICS & SETTINGS (Biểu đồ & Cài đặt)
- Phần biểu đồ (Thực tế & Trực quan):
  - 1 biểu đồ đường (Line Chart) thể hiện xu hướng cân nặng giảm/tăng dần theo tuần.
  - 1 biểu đồ cột (Bar Chart) so sánh Calo nạp vào thực tế (từ các bữa ăn đã lưu) so với Calo tiêu thụ mục tiêu.
- Phần cài đặt:
  - Switch bật/tắt chế độ ngoại tuyến (Offline Sync).
  - Đổi đơn vị (kg/lbs, L/oz).
```
