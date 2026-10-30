# 💊 MediScan – Smart Medicine & Health Scanner

MediScan is an AI-powered Android application designed to help users identify medicines and understand important medicine-related information. It combines OCR technology, AI-powered explanations, and medicine search functionality to make medicine information more accessible.

## 📱 About the Project

MediScan is developed using **Kotlin** and native Android technologies. Users can scan medicine packaging using their phone camera or search for medicines manually. The application extracts text using ML Kit and uses AI to generate easy-to-understand medicine explanations.

## ✨ Features

- **📷 Smart Medicine Scanner:** Scan medicine packaging using the device camera.
- **🔍 OCR Text Recognition:** Extract medicine names and text using Google ML Kit.
- **🤖 AI-Powered Explanations:** Generate medicine information using Groq AI (LLaMA 3.3 70B).
- **🔎 Manual Medicine Search:** Search medicines with debounced suggestions.
- **📚 Scan History:** View previously scanned medicines.
- **❤️ Favorites:** Save frequently accessed medicines.
- **🌐 Multi-language Support:** English and Hindi language support.
- **📡 Offline Support:** Store medicine data locally using SharedPreferences and Gson.
- **🔐 Firebase Authentication:** Register, login, and maintain user sessions.
- **🔑 Password Reset:** Recover access to an account.
- **🗑️ Account Management:** Manage account settings and account deletion.
- **💡 Daily Health Tips:** Display useful health-related tips.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| XML | User interface design |
| Android Studio | Development environment |
| CameraX | Camera integration |
| Google ML Kit | Text recognition (OCR) |
| Groq API | AI-powered medicine explanations |
| LLaMA 3.3 70B | AI language model |
| Firebase Authentication | User authentication |
| Retrofit | API communication |
| Room / SharedPreferences | Local data storage |
| Gson | JSON serialization |

## 🏗️ Application Architecture

```text
                 MediScan Android App
                          |
          +---------------+---------------+
          |               |               |
       Home Screen     Scanner        User Profile
                          |
                       CameraX
                          |
                    ML Kit OCR
                          |
                    Text Extraction
                          |
                 Text Processing
                          |
                 Medicine Detection
                          |
                    Groq AI API
                          |
                 Medicine Explanation
                          |
                  Result Screen
                          |
               History / Favorites
```

## 📸 Application Screenshots

Add your application screenshots here:

| Home Screen | Login Screen |
|---|---|
| Add Home screenshot | Add Login screenshot |

| Medicine Scanner | Medicine Result |
|---|---|
| Add Scanner screenshot | Add Result screenshot |

| Scan History | User Profile |
|---|---|
| Add History screenshot | Add Profile screenshot |

## 🚀 Getting Started

### Prerequisites

- Android Studio
- JDK compatible with your Android Gradle Plugin
- Android SDK
- Android device or emulator
- Firebase project
- Groq API key (for AI-powered functionality)

### Installation

1. Clone the repository:

```bash
git clone https://github.com/anish-thakur-code/MediScan.git
```

2. Open the project in Android Studio.

3. Allow Gradle to sync and download the required dependencies.

4. Configure Firebase by adding your own `google-services.json` file inside the `app` directory.

5. Configure your Groq API integration securely. Do not commit API keys or other private credentials to GitHub. For production, use a backend API to protect the key.

6. Build and run the application on an Android device or emulator.

## 🔐 Security & Privacy

- Firebase Authentication is used for user authentication.
- Sensitive credentials should not be stored in the public repository.
- API keys should be managed securely through a backend.
- Users can manage their account through the application settings.

## 🎯 Project Objectives

- Make medicine information easier to access.
- Reduce manual effort in identifying medicine names.
- Use OCR technology to extract text from medicine packaging.
- Provide AI-assisted explanations in a user-friendly format.
- Support medicine information access even with limited connectivity.

## 🔮 Future Enhancements

- Medicine interaction warnings.
- Medicine reminder notifications.
- Prescription scanning.
- Expanded medicine database.
- More language support.
- Improved medicine identification accuracy.

## 👨‍💻 Developer

**Anish Thakur**

GitHub: [anish-thakur-code](https://github.com/anish-thakur-code)

Project Repository: [MediScan](https://github.com/anish-thakur-code/MediScan)

---

⭐ If you find this project interesting, consider giving the repository a star!

**Disclaimer:** MediScan is an informational application and is not a substitute for professional medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional before making decisions about medication.
