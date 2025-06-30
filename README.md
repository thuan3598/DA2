# Ứng dụng Sổ tay điện tử theo dõi mực nước hồ

Đây là ứng dụng di động Android được phát triển để quản lý và ghi nhận dữ liệu mực nước hồ. Ứng dụng giúp người dùng dễ dàng theo dõi, thêm mới, cập nhật và xuất báo cáo.

## Mục lục

1.  [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống)
2.  [Thiết lập môi trường phát triển](#2-thiết-lập-môi-trường-phát-triển)
    * [Cài đặt Android Studio](#cài-đặt-android-studio)
    * [Cấu hình Android SDK](#cấu-hình-android-sdk)
    * [Thiết lập Firebase Project](#thiết-lập-firebase-project)
    * [Thiết lập Google Maps API Key](#thiết-lập-google-maps-api-key)
3.  [Cài đặt thư viện và phụ thuộc](#3-cài-đặt-thư-viện-và-phụ-thuộc)
4.  [Biên dịch và chạy chương trình](#4-biên-dịch-và-chạy-chương-trình)
    * [Sử dụng Android Studio](#sử-dụng-android-studio)
    * [Biên dịch bằng Gradle Command Line](#biên-dịch-bằng-gradle-command-line)
5.  [Kiểm thử chương trình](#5-kiểm-thử-chương-trình)
    * [Kiểm thử thủ công (Manual Testing)](#kiểm-thử-thủ-công-manual-testing)
    * [Test Cases](#test-cases)
6.  [Xử lý sự cố (Troubleshooting)](#6-xử-lý-sự-cố-troubleshooting)

---

### 1. Yêu cầu hệ thống

Để phát triển và chạy ứng dụng này, bạn cần có các công cụ và môi trường sau:

* **Hệ điều hành:** Windows 10/11 (64-bit), macOS 10.14 trở lên, hoặc Linux (64-bit).
* **Bộ nhớ RAM:** Tối thiểu 8GB (khuyến nghị 16GB trở lên).
* **Ổ đĩa cứng:** Tối thiểu 8GB dung lượng trống để cài đặt Android Studio, SDK và các tệp dự án.
* **CPU:** Khuyến nghị Intel Core i5 thế hệ thứ 8 trở lên hoặc tương đương.
* **Java Development Kit (JDK):** JDK 11 hoặc phiên bản mới hơn. Android Studio thường đi kèm với bản JDK tích hợp.
* **Kết nối Internet:** Cần thiết để tải Android Studio, SDK, thư viện Gradle và tương tác với Firebase/Google Maps.

### 2. Thiết lập môi trường phát triển

#### Cài đặt Android Studio

1.  **Tải xuống Android Studio:** Truy cập trang web chính thức của Android Studio: <https://developer.android.com/studio>
2.  **Cài đặt:** Chạy tệp cài đặt đã tải xuống và làm theo hướng dẫn trên màn hình. Đảm bảo bạn chọn cài đặt tất cả các thành phần mặc định, bao gồm Android SDK, Android SDK Platform-Tools và Android Emulator.

#### Cấu hình Android SDK

Android Studio sẽ tự động cài đặt phiên bản SDK khuyến nghị. Nếu cần, bạn có thể kiểm tra hoặc cài đặt thêm các phiên bản SDK khác:

1.  Mở Android Studio.
2.  Đi tới `File > Settings` (trên Windows/Linux) hoặc `Android Studio > Preferences` (trên macOS).
3.  Chọn `Appearance & Behavior > System Settings > Android SDK`.
4.  Tại tab "SDK Platforms", chọn các phiên bản Android bạn muốn hỗ trợ (ví dụ: Android 11, Android 12, Android 13).
5.  Tại tab "SDK Tools", đảm bảo "Android SDK Build-Tools", "Android SDK Platform-Tools", "Android Emulator" và "Google Play services" được chọn.
6.  Nhấp `Apply` và `OK` để cài đặt các thành phần đã chọn.

#### Thiết lập Firebase Project

Ứng dụng này sử dụng Firebase Realtime Database và Firebase Authentication. Bạn có thể tích hợp Firebase vào ứng dụng Android một cách thuận tiện bằng công cụ Firebase Assistant trong Android Studio:

1.  **Mở Firebase Assistant:** Trong Android Studio, mở dự án của bạn và truy cập menu: `Tools > Firebase`.
2.  **Kết nối đến Firebase:**
    * Tại bảng điều khiển Firebase Assistant hiện ra ở bên phải màn hình, chọn dịch vụ `Authentication`.
    * Chọn `Email and password authentication` rồi nhấn `Connect to Firebase`.
    * Android Studio sẽ mở một cửa sổ kết nối Firebase. Tại đây, bạn có thể chọn một Firebase Project đã có sẵn trên Firebase Console hoặc tạo một Project mới.
    * Sau khi chọn/tạo project, nhấn `Register App` để liên kết ứng dụng Android của bạn với Firebase Project.
    * Khi thành công, Android Studio sẽ tự động tạo project trên Firebase Console (nếu tạo mới), tải tệp `google-services.json` và đặt nó vào thư mục `app/` của dự án của bạn.
3.  **Thêm Firebase Authentication SDK:**
    * Vẫn trong Firebase Assistant, dưới mục `Authentication`, chọn bước tiếp theo là `Add the Firebase Authentication SDK to your app`.
    * Nhấn nút `Add SDK`. Android Studio sẽ tự động thêm các dependency cần thiết vào tệp `build.gradle (Module: app)` và thực hiện đồng bộ hóa dự án.
4.  **Bật phương thức đăng nhập Email/Password:**
    * Truy cập Firebase Console: <https://console.firebase.google.com/>
    * Điều hướng đến `Build > Authentication > tab Sign-in method`.
    * Bật (`Enable`) phương thức `Email/Password` và nhấn `Save`.
5.  **Thêm Firebase Realtime Database SDK:**
    * Quay lại Firebase Assistant trong Android Studio, chọn `Realtime Database`.
    * Chọn `Save and retrieve data`, sau đó nhấn `Add Realtime Database SDK` để thêm các thư viện cần thiết vào `build.gradle`.
6.  **Tạo Realtime Database:**
    * Mở Firebase Console và điều hướng đến `Build > Realtime Database`.
    * Nhấn `Create Database`.
    * Chọn vị trí gần bạn nhất và khởi tạo ở chế độ `Start in test mode` để tiện phát triển (lưu ý: cần cập nhật Security Rules sau này cho môi trường production).

#### Thiết lập Google Maps API Key

Ứng dụng này sử dụng Google Maps để hiển thị vị trí. Bạn cần tạo một API Key và thêm vào dự án của mình:

1.  **Tạo API Key:**
    * Truy cập Google Cloud Console - Google Maps Platform: <https://cloud.google.com/maps-platform/>
    * Đăng nhập bằng tài khoản Google của bạn.
    * Tạo một Project mới hoặc chọn Project hiện có.
    * Kích hoạt (`Enable`) các API sau trong mục "APIs & Services > Library":
        * `Maps SDK for Android`
        * `Places API` (nếu có sử dụng tìm kiếm địa điểm)
    * Điều hướng đến `APIs & Services > Credentials`.
    * Nhấp vào `Create credentials > API key`.
    * Sao chép API Key vừa tạo. **Lưu ý:** Để bảo mật, bạn nên hạn chế API Key này chỉ cho ứng dụng Android của mình bằng cách thêm "Android apps" restriction và cung cấp SHA-1 fingerprint của ứng dụng.
2.  **Thêm API Key vào dự án:**
    * Mở tệp `AndroidManifest.xml` trong thư mục `app/src/main/`.
    * Thêm thẻ `<meta-data>` bên trong thẻ `<application>` như sau:

    ```xml
    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApp"
        tools:targetApi="31">

        <!-- Thêm Google Maps API Key tại đây -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_GOOGLE_MAPS_API_KEY" />
        <!-- Thay thế YOUR_GOOGLE_MAPS_API_KEY bằng API Key của bạn -->

        <!-- Các Activity khác của bạn -->
        <activity android:name=".ui.login.MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <!-- ... -->

    </application>
    ```

### 3. Cài đặt thư viện và phụ thuộc

Các thư viện cần thiết được quản lý thông qua Gradle. Khi bạn mở dự án trong Android Studio, Gradle sẽ tự động tải các thư viện này. Các phụ thuộc được định nghĩa trong tệp `build.gradle (Module: app)` và có thể sử dụng Gradle Version Catalogs (ví dụ: `libs.appcompat` trỏ đến phiên bản trong `gradle/libs.versions.toml`).

**Trong `build.gradle (Project: YourProjectName)` (root level):**

```gradle
plugins {
    id 'com.android.application' version '8.2.0' apply false // Hoặc phiên bản mới nhất
    id 'com.android.library' version '8.2.0' apply false // Hoặc phiên bản mới nhất
    id 'org.jetbrains.kotlin.android' version '1.9.0' apply false // Hoặc phiên bản mới nhất
    id 'com.google.gms.google-services' version '4.4.1' apply false // Firebase Google Services Plugin
}
```
**Trong `build.gradle (Module: app):`**
```gradle
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services' // Thêm dòng này để tích hợp Firebase
}

android {
    namespace 'com.thuan.myapp' // Đảm bảo namespace đúng
    compileSdk 34 // Hoặc phiên bản SDK cao nhất bạn muốn biên dịch

    defaultConfig {
        applicationId "com.thuan.myapp"
        minSdk 24 // Phiên bản Android tối thiểu hỗ trợ
        targetSdk 34 // Phiên bản Android mục tiêu
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding true
    }
    // Cấu hình cho TensorFlow Lite
    aaptOptions {
        noCompress "tflite"
    }
}

dependencies {
    // AndroidX UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // TensorFlow Lite (cho xử lý ảnh/nhận diện)
    implementation ("org.tensorflow:tensorflow-lite:2.14.0")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation ("org.tensorflow:tensorflow-lite-metadata:0.4.4")

    // Biểu đồ (MPAndroidChart)
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Google Maps và Location Services
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    // Quản lý quyền (Dexter)
    implementation ("com.karumi:dexter:6.2.2")

    // iText for PDF generation
    implementation ("com.itextpdf:itextpdf:5.5.13")

    // OCR (Tess-two)
    implementation ("com.rmtheis:tess-two:9.1.0")

    // OpenCV (cho xử lý ảnh)
    implementation ("com.quickbirdstudios:opencv:4.5.3.0")

    // CircleImageView (nếu sử dụng ảnh profile tròn)
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // RecyclerView (cho danh sách)
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Google Mobile Vision (cho TextRecognizer)
    implementation("com.google.android.gms:play-services-vision:20.1.3")

    // Gson (cho xử lý JSON)
    implementation ("com.google.code.gson:gson:2.10.1")
}
```
Sau khi thêm hoặc chỉnh sửa các phụ thuộc, bạn cần `Sync Project with Gradle Files` (biểu tượng hình con voi xanh hoặc thông báo "Sync Now" trên Android Studio).
### 4. Biên dịch và chạy chương trình
#### Sử dụng Android Studio
1. **Mở dự án:** Khởi động Android Studio và chọn `Open an existing Android Studio project`. Duyệt đến thư mục gốc của dự án này và nhấp `OK`.

2. **Đồng bộ Gradle:** Đợi Android Studio đồng bộ hóa dự án với Gradle. Nếu có bất kỳ lỗi nào, hãy kiểm tra kết nối internet và cấu hình Gradle.

3. **Chọn thiết bị:**
   * Thiết bị ảo (Emulator):* Nhấp vào biểu tượng AVD Manager (Android Virtual Device Manager) trên thanh công cụ, sau đó tạo một thiết bị ảo mới hoặc chọn một thiết bị ảo đã có.
   * Thiết bị vật lý:* Kết nối thiết bị Android của bạn với máy tính qua cáp USB. Đảm bảo bạn đã bật chế độ "USB debugging" trong "Developer options" trên thiết bị của mình.
4. **Chạy ứng dụng:** Nhấp vào biểu tượng `Run 'app'` (mũi tên màu xanh lá cây) trên thanh công cụ của Android Studio. Android Studio sẽ biên dịch ứng dụng và triển khai nó lên thiết bị hoặc emulator đã chọn.
#### Biên dịch bằng Gradle Command Line
Bạn có thể biên dịch và cài đặt ứng dụng bằng các lệnh Gradle trực tiếp từ Terminal hoặc Command Prompt tại thư mục gốc của dự án.
1. **Biên dịch debug APK:**
   ```Bash
   ./gradlew assembleDebug
   ```
   (Trên Windows, dùng `gradlew.bat assembleDebug`)
APK sẽ được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.
2. **Cài đặt lên thiết bị đã kết nối:**
   ```Bash
   ./gradlew installDebug
   ```
   Lệnh này sẽ biên dịch nếu chưa có và cài đặt lên thiết bị Android đang kết nối qua ADB.
### 5. Kiểm thử chương trình
1. **Chuẩn bị môi trường:**
   * Đảm bảo thiết bị kiểm thử (ảo hoặc thật) đã được kết nối và hoạt động.
   * Kiểm tra kết nối mạng (Wi-Fi, Dữ liệu di động) theo yêu cầu của từng Test Case (online/offline).
   * Thiết lập dữ liệu ban đầu trên Firebase Console nếu Test Case yêu cầu điều kiện dữ liệu cụ thể.
2. **Thực hiện Test Cases:**
   * Tham khảo bộ Test Cases chi tiết cho ứng dụng trong file test_case.xlsx
   * Đối với mỗi Test Case, đọc kỹ "Điều kiện tiên quyết", "Các bước thực hiện" và "Kết quả mong đợi".
   * Thực hiện từng bước trên ứng dụng.
   * Ghi lại "Kết quả quan sát được" trong bảng Test Case.
   * So sánh "Kết quả quan sát được" với "Kết quả mong đợi" để xác định "Trạng thái" (Pass/Fail/Blocked/N/A).
   * Nếu là "Fail" hoặc "Blocked", ghi chú chi tiết lỗi, các bước để tái tạo lỗi và (nếu có thể) ảnh chụp màn hình hoặc logcat.
3. **Đánh giá:** Sau khi hoàn thành tất cả các Test Case, tổng hợp kết quả để đánh giá tổng quan về chất lượng và độ ổn định của ứng dụng.
### 6. Xử lý sự cố (Troubleshooting)
* **Lỗi Gradle Sync Failed:**
  * Kiểm tra kết nối internet.
  * Đảm bảo phiên bản Gradle Plugin và thư viện AndroidX/Firebase trong build.gradle là tương thích.
  * Thử `File > Invalidate Caches / Restart...` trong Android Studio.
* **Ứng dụng bị Crash khi chạy:**
  * Kiểm tra Logcat trong Android Studio để tìm thông báo lỗi chi tiết (ví dụ: `NullPointerException`, `IllegalStateException`).
  * Đảm bảo bạn đã thêm `file google-services.json` đúng cách vào thư mục `app/`.
  * Kiểm tra Firebase Security Rules để chắc chắn ứng dụng có quyền truy cập vào Realtime Database.
* **Không thể kết nối Firebase:**
  * Kiểm tra google-services.json đã được tải và đặt đúng chỗ.
  * Đảm bảo bạn đã thêm com.google.gms.google-services plugin vào cả build.gradle cấp project và cấp module.
  * Kiểm tra Security Rules trên Firebase Console.
* **Lỗi liên quan đến Google Maps:**
  * Đảm bảo bạn đã thêm Google Maps API Key vào AndroidManifest.xml và API Key đó đã được kích hoạt đúng các dịch vụ Maps trên Google Cloud Console.
  * Kiểm tra quyền truy cập vị trí trong `AndroidManifest.xml` và đảm bảo đã yêu cầu quyền runtime.
* **Lỗi "Cannot resolve method 'getAbsolutePath' in 'InputStream'" khi tạo PDF:**
  * Lỗi này xảy ra khi cố gắng lấy đường dẫn tuyệt đối từ `InputStream` của file font. Hãy đảm bảo bạn chỉ truyền đường dẫn tương đối của file font trong thư mục `assets` (ví dụ: `"font/times.ttf"`) cho `BaseFont.createFont()`.
* **Lỗi liên quan đến TensorFlow Lite / OpenCV / Tess-two:**
  * Đảm bảo các file model (`.tflite`) hoặc các tài nguyên cần thiết cho các thư thư viện này được đặt đúng vị trí (ví dụ: thư mục `assets`).
  * Kiểm tra cấu hình `aaptOptions { noCompress "tflite" }` trong `build.gradle` để đảm bảo các file model không bị nén.



