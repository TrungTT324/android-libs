# 📦 Core Library (`libs:core`)

Thư viện nòng cốt dùng chung cho toàn bộ hệ sinh thái ứng dụng Android Native Kotlin (`app`, `appMic`, `appMouse`, `appQa`).

---

## 🛠️ Danh sách các Module Tiện ích (Utils)

### 1. `NetworkUtils` (`hdisoft.app.core.utils.NetworkUtils`)
Hỗ trợ xử lý mạng cục bộ (LAN), IP private và tự động bật/tắt Wi-Fi.

```kotlin
// Lấy IP private nội bộ trong mạng Wi-Fi/Ethernet (vd: "192.168.1.135")
val ip = NetworkUtils.getLocalIpAddress(context)

// Lấy dải Subnet cục bộ (vd: "192.168.1.")
val subnet = NetworkUtils.getLocalSubnet(context)

// Kiểm tra Wi-Fi có đang bật không
val isWifiOn = NetworkUtils.isWifiEnabled(context)

// Tự động kiểm tra và bật Wi-Fi nếu bị tắt
NetworkUtils.ensureWifiEnabled(context)
```

---

### 2. `ClipboardUtils` (`hdisoft.app.core.utils.ClipboardUtils`)
Sao chép và đọc dữ liệu từ bộ nhớ tạm (Clipboard) kèm thông báo Toast tự động.

```kotlin
// Sao chép văn bản vào Clipboard (tự động hiện Toast "Đã sao chép...")
ClipboardUtils.copyToClipboard(context, label = "IP", text = "192.168.1.135")

// Sao chép với thông báo tùy chỉnh
ClipboardUtils.copyToClipboard(context, "IP", "192.168.1.135", toastMessage = "Đã lưu địa chỉ IP!")

// Đọc văn bản đang lưu trong Clipboard
val text = ClipboardUtils.readFromClipboard(context)
```

---

### 3. `ToastUtils` (`hdisoft.app.core.utils.ToastUtils`)
Các Kotlin Extension Functions giúp hiển thị thông báo Toast nhanh gọn.

```kotlin
import hdisoft.app.core.utils.showToast
import hdisoft.app.core.utils.showLongToast

// Hiển thị Toast ngắn
context.showToast("Kết nối thành công!")
context.showToast(R.string.success_msg)

// Hiển thị Toast dài
context.showLongToast("Không tìm thấy thiết bị trong mạng LAN")
```

---

### 4. `FormatUtils` (`hdisoft.app.core.utils.FormatUtils`)
Định dạng kích thước tệp, thời lượng phát và mã BuildNo.

```kotlin
// Định dạng dung lượng tệp (B, KB, MB, GB) -> "12.5 MB"
val fileSizeStr = FormatUtils.formatFileSize(13107200L)

// Định dạng thời lượng mili giây -> "03:45" hoặc "01:15:30"
val timeStr = FormatUtils.formatDurationMs(225000L)

// Định dạng BuildNo 12 chữ số (202607251140) -> "25 @ 11:40"
val buildStr = FormatUtils.formatBuildNo(202607251140L)
```

---

### 5. `PermissionUtils` (`hdisoft.app.core.utils.PermissionUtils`)
Kiểm tra quyền truy cập Runtime Permissions trên Android 6.0+.

```kotlin
// Kiểm tra 1 quyền bất kỳ
val hasMic = PermissionUtils.hasPermission(context, Manifest.permission.RECORD_AUDIO)

// Kiểm tra danh sách nhiều quyền
val hasAll = PermissionUtils.hasPermissions(
    context,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

// Kiểm tra nhanh quyền Record Audio
val micGranted = PermissionUtils.hasAudioPermission(context)
```

---

### 6. `DeviceUtils` (`hdisoft.app.core.utils.DeviceUtils`)
Nhận diện thiết bị, phát hiện giả lập (Emulator) và kiểm tra quyền Root (`su`).

```kotlin
// Lấy Android Device ID
val deviceId = DeviceUtils.getDeviceId(context)

// Kiểm tra thiết bị có phải Giả lập Android (Emulator) không
if (DeviceUtils.isEmulator()) {
    // Cảnh báo hạn chế NAT giả lập
}

// Kiểm tra thiết bị đã Root và có quyền su usable hay không
if (DeviceUtils.isDeviceRooted()) {
    // Thực thi lệnh bằng quyền Root
}
```

---

### 7. `AppTool` (`hdisoft.app.core.utils.AppTool`)
Tìm kiếm và mở ứng dụng đã cài đặt trên máy theo tên hoặc package name.

```kotlin
// Mở ứng dụng theo tên (vd: "YouTube", "Chrome") hoặc package name ("com.google.android.youtube")
val launched = AppTool.openApp(context, "youtube")
```

---

## 🌐 Mạng Sockets TCP (`net`)

- **`TcpServer`** (`hdisoft.app.core.net.TcpServer`): Máy chủ TCP Socket lắng nghe kết nối truyền nhận âm thanh / dữ liệu theo thời gian thực.
- **`TcpClient`** (`hdisoft.app.core.net.TcpClient`): Máy khách TCP kết nối trực tiếp tới Server qua địa chỉ IP & Port.

---

## 💾 Lưu trữ Cấu hình (`prefs`)

- **`AppPreferences`** (`hdisoft.app.core.prefs.AppPreferences`): Trình quản lý `SharedPreferences` lưu trữ cấu hình Host IP, cổng kết nối và các cài đặt ứng dụng.
