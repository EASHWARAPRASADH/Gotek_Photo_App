# Student ID Photo Capture App

A comprehensive Android application for efficiently capturing student photos for ID cards without requiring professional photographers. Teachers can use this app to take photos that are automatically linked to students with structured naming and cloud storage.

## Features

### 🔐 Authentication System
- Login for teachers/admin
- Role-based access control (Teacher, Admin)
- Default credentials: `admin` / `admin123`

### 📋 Student Selection Flow
- Select School → Class → Section → Student
- Fetch student data from local database
- Mock data generation for demonstration

### 📷 Camera Capture Module
- Device camera integration (mobile-friendly)
- Face alignment overlay guide
- Real-time instructions (center face, good lighting)
- Direct in-app capture (no external camera app)

### 📝 Auto Photo Naming System
- Automatic filename generation: `[SCHOOLCODE]-[CLASS]-[ROLLNUMBER].jpg`
- Example: `SCH01-10A-023.jpg`
- Metadata storage:
  - student_id
  - student_name
  - class
  - section
  - timestamp

### ✅ Photo Quality Validation
- Face detection using ML Kit
- Blur detection algorithm
- Lighting quality warnings
- Retake option for poor quality photos

### 👁️ Preview & Confirm
- Show captured image
- Buttons: Retake / Confirm Save
- Review before final save

### ☁️ Cloud Upload with Offline Support
- Upload to backend (API integration ready)
- Structured folder storage: `/school/class/section/`
- Offline photo storage
- Auto-sync when internet is available

### 📊 Admin Dashboard
- View uploaded photos
- Filter by class/section
- Progress tracker (e.g., 20/45 students completed)
- Track missing students

## Tech Stack

### Frontend
- **Kotlin** - Primary language
- **CameraX** - Camera integration
- **ML Kit** - Face detection
- **Room Database** - Local storage
- **Coroutines** - Async operations
- **ViewModel & LiveData** - Architecture components
- **Navigation Component** - Screen navigation
- **Work Manager** - Background tasks

### Storage
- **Room Database** - Local data persistence
- **File System** - Local photo storage
- **API Ready** - Retrofit integration for cloud upload

## Requirements

- Android SDK 24 (Android 7.0) or higher
- Camera permission
- Internet permission (for cloud upload)
- Storage permission (for local photo save)

## Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd app
   ```

2. **Open in Android Studio**
   - Open the project in Android Studio
   - Wait for Gradle sync to complete
   - Ensure Android SDK is installed

3. **Configure SDK path** (if needed)
   - Edit `local.properties`:
     ```
     sdk.dir=/Users/your_username/Library/Android/sdk
     ```

4. **Build and Run**
   - Connect an Android device or start emulator
   - Click Run in Android Studio
   - Grant camera permission when prompted

## Usage

### For Teachers

1. **Login**
   - Enter username and password
   - Default: `admin` / `admin123`

2. **Select Student**
   - Choose School from dropdown
   - Choose Class from dropdown
   - Choose Section from dropdown
   - Tap on student name from list

3. **Capture Photo**
   - Position student's face within the white guide overlay
   - Ensure good lighting
   - Tap capture button
   - Wait for face detection and validation

4. **Preview & Save**
   - Review the captured photo
   - Tap "Retake" if unsatisfied
   - Tap "Save" to confirm

5. **Photo is automatically**
   - Named: `SCH01-10A-023.jpg`
   - Saved locally
   - Queued for cloud upload
   - Student status updated to "CAPTURED"

### For Admins

1. **Access Dashboard**
   - View total students count
   - View completed students count
   - View pending students count
   - Progress bar showing completion percentage

2. **Filter Students**
   - Filter by Class
   - Filter by Section
   - View student list with photo status

3. **Track Progress**
   - See which students have photos
   - Identify missing photos
   - Monitor completion status

## Photo Specifications

- **Dimensions**: 35mm x 45mm (ID card standard)
- **Aspect Ratio**: 7:9
- **Output Resolution**: 350x450 pixels
- **Format**: JPEG (quality 100%)
- **Naming**: `[SCHOOLCODE]-[CLASS]-[ROLLNUMBER].jpg`

## Project Structure

```
app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/studentidphotocapture/app/
│   │       │   ├── data/
│   │       │   │   ├── dao/          # Database access objects
│   │       │   │   ├── database/     # Room database
│   │       │   │   ├── model/        # Data models
│   │       │   │   └── repository/   # Repository pattern
│   │       │   ├── ui/
│   │       │   │   ├── camera/       # Camera capture
│   │       │   │   ├── dashboard/    # Admin dashboard
│   │       │   │   ├── login/        # Authentication
│   │       │   │   ├── preview/      # Photo preview
│   │       │   │   └── studentselection/  # Student selection
│   │       │   ├── util/             # Utilities
│   │       │   └── workmanager/      # Background tasks
│   │       └── res/
│   │           ├── drawable/         # Drawables
│   │           ├── layout/           # UI layouts
│   │           ├── values/           # Resources
│   │           └── xml/              # XML configs
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Key Components

### Data Models
- **User**: Authentication and role management
- **Student**: Student information and photo status
- **School**: School structure with classes and sections
- **PhotoMetadata**: Photo metadata and upload status

### Repositories
- **AuthRepository**: User authentication
- **StudentRepository**: Student data management
- **PhotoRepository**: Photo storage and upload

### Activities
- **LoginActivity**: User authentication
- **StudentSelectionActivity**: Student selection flow
- **CameraActivity**: Photo capture with face detection
- **PreviewActivity**: Photo review and confirmation
- **DashboardActivity**: Admin dashboard and progress tracking

### Background Services
- **PhotoUploadWorker**: Background photo upload
- **PhotoUploadService**: Upload service trigger
- **NetworkUtil**: Network connectivity check

## Customization

### Backend API Integration

To integrate with your backend API:

1. **Create API Service**
   ```kotlin
   interface PhotoUploadApi {
       @Multipart
       @POST("upload")
       suspend fun uploadPhoto(
           @Part photo: MultipartBody.Part,
           @Query("schoolCode") schoolCode: String,
           @Query("classGrade") classGrade: String,
           @Query("section") section: String
       ): Response<UploadResponse>
   }
   ```

2. **Update PhotoUploadWorker**
   - Replace simulated upload with actual API call
   - Handle API responses and errors
   - Update upload status accordingly

### Custom Photo Dimensions

Modify in `CameraActivity.kt`:
```kotlin
companion object {
    private const val PHOTO_WIDTH = 350  // Change as needed
    private const val PHOTO_HEIGHT = 450 // Change as needed
}
```

### Add More Schools/Classes

Update in `StudentSelectionViewModel.kt`:
```kotlin
private fun loadMockData() {
    _uiState.value = _uiState.value.copy(
        schools = listOf("SCH01", "SCH02", "SCH03"), // Add more
        classes = listOf("10A", "10B", "11A"),       // Add more
        sections = listOf("A", "B", "C")              // Add more
    )
}
```

## Troubleshooting

### Camera not working
- Ensure camera permission is granted
- Check if device has a camera
- Try restarting the app

### Photos not uploading
- Check internet connection
- Verify backend API is running
- Check PhotoUploadWorker logs

### Face detection not working
- Ensure good lighting
- Position face clearly in guide
- Check ML Kit dependencies

## Future Enhancements

- [ ] QR code scanning to auto-select student
- [ ] Bulk import student list (CSV)
- [ ] Advanced photo editing (crop, rotate, brightness)
- [ ] Multiple photo capture per student
- [ ] PDF generation for ID cards
- [ ] Email notification for completed batches
- [ ] Cloud backup and restore
- [ ] Multi-language support

## License

This project is proprietary software for ID card companies.

## Support

For support and inquiries, please contact the development team.
