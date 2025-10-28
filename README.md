# 🚨 Respondr — AI Emergency Assistant

A student-built Android application that serves as an AI-powered emergency assistant using Google's Gemini API.

## ✨ Features

- 🎤 **Voice Input** - Speak to describe emergencies
- ⌨️ **Text Input** - Type emergency descriptions
- 🤖 **AI Analysis** - Gemini AI analyzes and responds to emergency situations
- 🎨 **Modern UI** - Beautiful Material Design interface

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd Respondr
```

### 2. Get Your Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Create a new API key (it's free!)
3. Copy your API key

### 3. Configure API Key Securely

Open `local.properties` (already gitignored) and add:

```properties
GEMINI_API_KEY=your_api_key_here
```

**✅ Your API key is now secure!** It's stored locally and won't be committed to Git.

### 4. Build and Run

```bash
# Windows
.\gradlew.bat installDebug

# Or open in Android Studio and click Run
```

## 🛠 Tech Stack

- **Language:** Java
- **UI:** Material Design 3, ConstraintLayout, CardView
- **AI:** Google Gemini 2.0 Flash (Free tier)
- **Networking:** OkHttp + Gson
- **Speech:** Android SpeechRecognizer
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 36

## 🔒 API Key Security

✅ **What we implemented:**

- API key stored in `local.properties` (gitignored)
- Injected via BuildConfig at build time
- No hardcoded secrets in source code
- Safe to commit to GitHub!

❌ **Never commit:**

- `local.properties` file
- Any files with API keys
- Keystore files (_.jks, _.keystore)

## 📊 Free Tier Limits (Gemini API)

- **60** requests per minute
- **1 million** tokens per minute
- **1,500** requests per day

Perfect for student projects and testing!

## ❓ Troubleshooting

**Build error about GEMINI_API_KEY:**

- Make sure you added your key to `local.properties`
- Rebuild the project

**Network errors:**

- Check internet connection
- Verify API key at [Google AI Studio](https://aistudio.google.com)

**Speech recognition not working:**

- Grant microphone permission when prompted
- Ensure device has Google Play Services

---

Built with ❤️ for emergency response education
