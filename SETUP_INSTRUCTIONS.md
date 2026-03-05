# Lyra Saver - Complete Setup Instructions

Complete guide to build, deploy, and run the Lyra Saver application on your Android device.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Building the APK](#building-the-apk)
4. [Deploying to Your Phone](#deploying-to-your-phone)
5. [App Configuration & Permissions](#app-configuration--permissions)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before you begin, ensure you have:

- **Android Studio** (latest version recommended) - [Download here](https://developer.android.com/studio)
- **Java 17 or later** - Required for Gradle and Kotlin compilation
- **An Android device** with Android 7.0 (API 24) or higher
- **WhatsApp installed** on the target device (WhatsApp or WhatsApp Business)
- **USB cable** to connect your device to computer
- **~500MB free space** on your device for the app and media files

### Verify Java Installation
Open a terminal/command prompt and run:
```bash
java -version
```
You should see Java 17+ output. If not, install Java 17 from [here](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).

---

## Environment Setup

### Step 1: Clone or Extract the Project

If you have the project as a ZIP file:
```bash
# Extract to a location without spaces in the path
unzip lyra-saver-main.zip
cd lyra-saver-main
```

If you're cloning from GitHub:
```bash
git clone https://github.com/yourusername/lyra-saver.git
cd lyra-saver
```

### Step 2: Open Project in Android Studio

1. Launch Android Studio
2. Click **File** → **Open**
3. Navigate to the `lyra-saver` project folder
4. Click **OK**

Android Studio will automatically:
- Download Gradle dependencies
- Sync the Kotlin project
- Configure the build system

**Wait for the sync to complete** (check the bottom status bar).

### Step 3: Verify IDE Configuration

1. **Check SDK Version**: Go to `File` → `Project Structure` → `Project`
   - Verify `Gradle JDK` is set to JDK 17+
   - Verify `SDK Location` points to Android SDK (usually `C:\Users\{user}\AppData\Local\Android\Sdk` on Windows)

2. **Install SDK Components** (if needed): Go to `Tools` → `SDK Manager`
   - Ensure `Android SDK 35` is installed
   - Ensure `Build Tools 35.x.x` is installed
   - Ensure `Android SDK Platform 24` (API 24) is installed for minSdk compatibility

3. **Verify Gradle Sync**: Click `Tools` → `Kotlin` → `Configure Kotlin Plugin Updates` → dismiss any prompts

---

## Building the APK

### Method 1: Build Debug APK (Recommended for Testing)

This creates an APK suitable for development and testing on your phone.

#### Using Android Studio UI:
1. At the top of Android Studio, find the **Build** menu
2. Click **Build** → **Build Bundle(s)/APK(s)** → **Build APK(s)**
3. Wait for the build to complete (you'll see a Gradle console output)
4. When finished, a notification appears: **Build successful** with a link to locate the APK

#### APK Location:
```
app/build/outputs/apk/debug/app-debug.apk
```

**Note**: If you see compilation errors, run:
```bash
./gradlew clean build
```
(On Windows Command Prompt, use `gradlew clean build`)

#### Expected Build Time:
- First build: 2-5 minutes (downloading dependencies)
- Subsequent builds: 30-60 seconds

### Method 2: Build Release APK (For Production/Google Play)

This creates an optimized APK for distribution.

#### Prerequisites:
You need a **keystore file** to sign the APK. The project includes a self-signed test keystore.

#### Steps:
1. Go to **Build** → **Build Bundle(s)/APK(s)** → **Build APK(s)**
2. Android Studio will ask for signing configuration:
   - **Keystore path**: Navigate to `keystore/release.jks`
   - **Keystore password**: `lyrasaver` (default test keystore)
   - **Key alias**: `lyrasaver`
   - **Key password**: `lyrasaver`
3. Proceed with the build

#### Release APK Location:
```
app/build/outputs/apk/release/app-release.apk
```

### Method 3: Using GitHub Actions (Automatic CI/CD)

The project includes automatic APK generation:

1. Push your code to GitHub
2. Navigate to **Actions** tab in your GitHub repository
3. Click the latest workflow run
4. Download the artifact `app-debug.apk` or `app-release.apk`

---

## Deploying to Your Phone

### Step 1: Enable Developer Mode on Your Android Device

1. Open **Settings** on your phone
2. Go to **About Phone**
3. Scroll to **Build Number**
4. Tap **Build Number** 7 times rapidly (you'll see a "You are a developer now" message)
5. Go back to **Settings** → **Developer Options** (should now be visible)
6. **Enable** the following:
   - USB Debugging
   - (Optional) Disable absolute volume in Developer Options

### Step 2: Connect Your Phone via USB

1. Connect your phone to your computer with a USB cable
2. On your phone, a prompt appears asking to allow USB debugging
3. Check **Always allow from this computer** and tap **Allow**
4. In Android Studio, verify your device appears in the device list (top toolbar)

### Step 3: Deploy Debug APK Using Android Studio

#### Option A: Install and Run
1. Click the **Run** button (green play icon) in Android Studio
2. Select your connected device from the dialog
3. Click **OK**

Android Studio will:
- Build the APK
- Install on your device
- Launch the app automatically

#### Option B: Manual ADB Install
Open a terminal in the project directory:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

The app appears on your device's home screen or app drawer.

### Step 4: Deploy Release APK

If you built a release APK, transfer it to your phone:

#### Option A: Direct File Transfer
1. Copy `app-release.apk` to your phone via USB
2. On your phone, enable **Install from Unknown Sources** (Settings → Apps → Special app access)
3. Open your file manager and tap the APK file
4. Confirm installation

#### Option B: Using ADB
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

---

## App Configuration & Permissions

### First Launch

When you first launch **Lyra Saver**, the app requests several permissions:

#### 1. **Notification Listener Access** (Required for Deleted Message Recovery)
- The app will prompt: "Lyra Saver wants to access your notifications"
- You'll be redirected to Settings → Notifications → Notification Access
- **Enable** "Lyra Saver" in the list
- This allows the app to detect deleted messages in WhatsApp

#### 2. **File Access Permissions**
- The app will prompt for "Files and Media" or "Photos and Videos"
- Grant these permissions to allow the app to access WhatsApp Status folder

#### 3. **Folder Selection (Storage Access Framework)**
- When you first tap **"Select WhatsApp Folder"**, you'll see a file browser
- Navigate to: `Android/media/com.whatsapp/WhatsApp/Media/.Statuses` (or similar for WhatsApp Business)
- Tap the folder and select **"Use this folder"**
- This grants permanent access to the Status folder

### Manual Configuration Steps

#### Enable Notification Access:
1. Open **Settings** on your phone
2. Go to **Apps** → **Notification Access** (or **Notifications** → **Notification Access**)
3. Find **Lyra Saver** in the list
4. Enable the toggle

#### Configure Auto-Save (Optional):
1. Open Lyra Saver
2. Go to **Settings** tab
3. Enable **Auto-Save Status** toggle
4. The app will automatically check and save new statuses every 5 minutes

#### Configure Auto-Delete (Optional):
1. In **Settings**, enable **Auto-Delete Old Messages**
2. Set the retention period (e.g., keep messages for 30 days)
3. The app handles deletion automatically in the background

### Permissions Summary

| Permission | Purpose | Required |
|-----------|---------|----------|
| **Notification Access** | Detect deleted messages | Yes |
| **Read External Storage** | Access WhatsApp Status folder | Yes |
| **Write External Storage** | Save statuses to device | Yes |
| **Post Notifications** | Show app notifications | No (Android 13+) |
| **Request Ignore Battery Optimization** | Keep auto-save working | No |

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: "Failed to Build" or Gradle Sync Error
**Solution**:
```bash
# Clean the project
./gradlew clean

# Rebuild
./gradlew build

# If that fails, check Java version
java -version
# Ensure it's Java 17+
```

#### Issue 2: "Device Not Found" in Android Studio
**Solution**:
1. Ensure USB Debugging is enabled on your phone (Settings → Developer Options)
2. Disconnect and reconnect the USB cable
3. Accept the USB Debugging prompt on your phone
4. In Android Studio: Click **Tools** → **Device Manager** → refresh the device list

#### Issue 3: "Unable to Install APK" Error
**Solution**:
- The app is already installed: Run `adb uninstall com.lyrasaver` first
- Different ABI (architecture): Use Android Emulator instead
- Storage full: Free up space on your device

#### Issue 4: "WhatsApp Folder Not Found"
**Solution**:
1. Verify WhatsApp is installed and has created the Status folder:
   - Open WhatsApp, go to a status
   - Send a test status yourself
   - Wait 5 seconds for it to save
2. Navigate manually using Storage Access Framework:
   - Tap **"Select WhatsApp Folder"** in the app
   - Browse to `Internal Storage` → `Android` → `media` → `com.whatsapp` → `WhatsApp` → `Media` → `.Statuses`
   - Select the folder

#### Issue 5: "Notification Access Not Working"
**Solution**:
1. Go to **Settings** → **Apps** → **Notification Access**
2. Find & enable **Lyra Saver**
3. Force stop and restart Lyra Saver:
   - Settings → Apps → Lyra Saver → Force Stop
   - Relaunch from app drawer

#### Issue 6: "App Crashes on Launch"
**Solution**:
1. Check logcat in Android Studio (View → Tool Windows → Logcat)
2. Look for red error lines
3. Common causes:
   - Missing permissions (grant them manually in Settings)
   - Database corruption: Uninstall, clear app data, reinstall
   - Incompatible Android version: Update Android or use compatible device

#### Issue 7: "Cannot Write to Downloads Folder"
**Solution**:
- On Android 11+, the app is restricted to its app-specific directories
- Saved statuses are stored in: `Android/data/com.lyrasaver/files`
- To access: Use **File Manager** → **Android** → **data** → **com.lyrasaver**
- Or use Storage Access Framework from the app

---

## Advanced Topics

### Building a AAB (Android App Bundle) for Google Play

```bash
./gradlew bundleRelease
```

Output location: `app/build/outputs/bundle/release/app-release.aab`

Upload this to Google Play Console for app distribution.

### Signing Your APK for Google Play

The app uses a test keystore. For production:

1. Create a new production keystore:
```bash
keytool -genkey -v -keystore my-key.jks -keyalg RSA -keysize 2048 -validity 10000
```

2. Update `gradle.properties` with your keystore path and credentials
3. Rebuild the APK

### Installing on Android Emulator

If you don't have a physical device:

1. In Android Studio: Click **Device Manager** (left toolbar)
2. Create a new virtual device (e.g., Pixel 5 with API 35)
3. Click the play button to launch the emulator
4. Run the app using the same steps as physical device

### Enabling Logging for Debugging

Edit `android/build.gradle.kts` to enable verbose logging:

```gradle
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-verbose"
    }
}
```

---

## Next Steps

After successful installation:

1. **Grant Permissions**: When the app first launches, grant all requested permissions
2. **Select WhatsApp Folder**: Tap "Select WhatsApp Folder" to enable status scanning
3. **Enable Notifications**: Go to Settings app → Notifications → Notification Access → Enable Lyra Saver
4. **Enable Auto-Save**: In the app's Settings tab, toggle "Auto-Save Status"
5. **Test Features**:
   - Take a WhatsApp status and verify it appears in the app
   - Delete a message and check if it appears in "Deleted Messages"
   - Try splitting a video

---

## Support

If you encounter issues:

1. Check the **Logcat** in Android Studio for error messages
2. Verify all permissions are granted in Settings
3. Ensure WhatsApp is installed and recently used
4. Try clearing app data: Settings → Apps → Lyra Saver → Storage → Clear Cache/Clear Data
5. Reinstall the app if all else fails

---

## Summary

You now have all the steps to build and run **Lyra Saver** on your Android device. The process is:

1. ✅ Setup environment (Android Studio, Java 17)
2. ✅ Open project in Android Studio
3. ✅ Build APK (Debug or Release)
4. ✅ Deploy to device (USB or file transfer)
5. ✅ Grant permissions
6. ✅ Select WhatsApp folder
7. ✅ Enable features in Settings
8. ✅ Start using the app!

Happy saving! 📱✨
