# 🚨 Respondr — AI Emergency Assistant (Android)

Respondr is a student-built Android app that helps users quickly describe an emergency by voice or text, then uses AI to classify the situation (for example: **Medical**, **Fire**, or **Police**) and generate a structured alert.

> ⚠️ **Important:** This is an educational prototype and is **not** a replacement for 911 or professional emergency services.

---

## Why this project exists

In real emergencies, people may struggle to explain what is happening clearly and quickly.  
Respondr explores how AI can help transform raw user input into a cleaner, categorized incident report.

---

## What it does

- 🎤 Accepts **voice input** (speech-to-text)
- ⌨️ Accepts **text input**
- 🤖 Uses **Google Gemini API** to analyze and classify emergency descriptions
- 📍 Captures location context (when enabled in app flow)
- 🧾 Produces structured emergency data for responder-style workflows
- 📱 Runs as a native Android app with modern UI patterns

---

## Demo / Screenshots

Add screenshots or a short demo video/GIF here.

Example:
- `docs/screenshots/home.png`
- `docs/screenshots/analysis.png`

---

## Tech stack

- **Language:** Java
- **Platform:** Android
- **AI:** Google Gemini API
- **Networking/Data:** REST APIs, JSON, OkHttp, Gson
- **Speech:** Android `SpeechRecognizer`
- **(If used in your branch):** Firebase for realtime/state synchronization

---

## Getting started

### 1) Clone the repo

```bash
git clone https://github.com/eduardodfran/Respondr.git
cd Respondr
```

### 2) Add your Gemini API key

Create or edit `local.properties` (this file is gitignored):

```properties
GEMINI_API_KEY=your_api_key_here
```

Get a key from: https://aistudio.google.com/app/apikey

### 3) Build and run

```bash
# Windows
.\gradlew.bat installDebug
```

Or open the project in **Android Studio** and click **Run**.

---

## Project status

- ✅ Active student project
- ✅ Core AI-assisted emergency classification flow implemented
- 🔄 Ongoing improvements to UX, reliability, and alert formatting

---

## Current limitations

- Prototype only (not production emergency infrastructure)
- AI responses may be inaccurate or incomplete
- Requires internet access for AI analysis
- Should be used for educational/testing purposes only

---

## Security notes

- API keys are loaded from `local.properties` at build time
- Do **not** commit secrets or keystore files
- Review your `.gitignore` before pushing changes

---

## Resume-ready summary

If you want to reference this project:

**Respondr | AI Emergency Assistant**  
Built an Android app that captures voice/text emergency reports, uses Gemini AI to classify incident type, and outputs structured, geotagged alert data for dispatcher-style workflows.

---

## License

Add your license here (for example, MIT) once selected.
