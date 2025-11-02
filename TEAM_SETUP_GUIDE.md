# Respondr - Team Setup Guide 🚀

Welcome to the Respondr project! This guide will help you set up your development environment and start contributing.

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Git Workflow](#git-workflow)
4. [Running the Project](#running-the-project)
5. [Project Structure](#project-structure)
6. [Common Issues & Solutions](#common-issues--solutions)
7. [Contributing Guidelines](#contributing-guidelines)

---

## Prerequisites

Before you start, make sure you have:

- ✅ **Git** installed ([Download Git](https://git-scm.com/downloads))
- ✅ **Android Studio** (Latest version recommended) ([Download](https://developer.android.com/studio))
- ✅ **JDK 11 or higher** (Usually comes with Android Studio)
- ✅ **GitHub account**
- ✅ Access to this repository

---

## Initial Setup

### 1. Clone the Repository

Open your terminal/command prompt and run:

```bash
# Clone the repository
git clone https://github.com/eduardodfran/Respondr.git

# Navigate into the project directory
cd Respondr
```

### 2. Set Up Gemini API Key

⚠️ **IMPORTANT**: Never commit your API key to Git!

1. Open the project in Android Studio
2. Create a `local.properties` file in the root directory (if it doesn't exist)
3. Add your Gemini API key:

```properties
# local.properties
GEMINI_API_KEY=YOUR_API_KEY_HERE
```

**How to get a Gemini API key:**

- Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
- Sign in with your Google account
- Click "Create API Key"
- Copy the key and paste it in `local.properties`

### 3. Open Project in Android Studio

1. Open Android Studio
2. Select **"Open an existing project"**
3. Navigate to the `Respondr` folder and click **OK**
4. Wait for Gradle sync to complete (this may take a few minutes)

---

## Git Workflow

### Understanding Branches

- **`main`** - Production-ready code (protected)
- **`dev`** - Development branch (all features merge here first)
- **`feature/your-feature-name`** - Your personal feature branches

### Daily Workflow

#### Step 1: Always Start with Latest Code

```bash
# Make sure you're on the main branch
git checkout main

# Pull the latest changes
git pull origin main
```

#### Step 2: Create a Feature Branch

```bash
# Create and switch to a new branch
git checkout -b feature/your-feature-name

# Example:
git checkout -b feature/add-location-tracking
git checkout -b feature/fix-chat-ui
git checkout -b feature/firebase-integration
```

**Naming conventions:**

- `feature/description` - For new features
- `fix/description` - For bug fixes
- `docs/description` - For documentation updates
- `refactor/description` - For code refactoring

#### Step 3: Make Your Changes

1. Write your code
2. Test your changes
3. Make sure everything works

#### Step 4: Stage and Commit Your Changes

```bash
# See what files you changed
git status

# Add specific files
git add app/src/main/java/com/example/respondr/YourFile.java

# Or add all changed files
git add .

# Commit with a clear message
git commit -m "Add location tracking feature"
```

**Good commit messages:**

- ✅ `Add voice recognition for emergency input`
- ✅ `Fix chat bubble alignment issue`
- ✅ `Update README with setup instructions`

**Bad commit messages:**

- ❌ `Updated stuff`
- ❌ `Fixed bug`
- ❌ `asdfasdf`

#### Step 5: Push Your Branch

```bash
# Push your feature branch to GitHub
git push origin feature/your-feature-name
```

#### Step 6: Create a Pull Request

1. Go to [GitHub Repository](https://github.com/eduardodfran/Respondr)
2. You'll see a **"Compare & pull request"** button - click it
3. Fill in the PR details:
   - **Title**: Brief description of your changes
   - **Description**:
     - What did you change?
     - Why did you change it?
     - How to test it?
     - Screenshots (if UI changes)
4. Click **"Create Pull Request"**
5. Wait for code review and approval
6. Once approved, your code will be merged!

### Keeping Your Branch Up to Date

If `main` branch gets updated while you're working:

```bash
# Save your current work
git add .
git commit -m "WIP: Work in progress"

# Switch to main and pull latest
git checkout main
git pull origin main

# Go back to your branch
git checkout feature/your-feature-name

# Merge main into your branch
git merge main

# If there are conflicts, resolve them in Android Studio
# Then continue:
git add .
git commit -m "Merge main into feature branch"
git push origin feature/your-feature-name
```

---

## Running the Project

### Build and Run

1. Connect an Android device or start an emulator
2. Click the **Run** button (green triangle) in Android Studio
3. Or use command line:

```bash
# Build the project
./gradlew build

# On Windows:
.\gradlew.bat build
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "com.example.respondr.ExampleUnitTest"
```

---

## Project Structure

```
Respondr/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/respondr/
│   │   │   │   ├── MainActivity.java         # Main activity with chat UI
│   │   │   │   ├── GeminiClient.java         # Gemini API client
│   │   │   │   ├── ChatAdapter.java          # RecyclerView adapter for chat
│   │   │   │   └── ChatMessage.java          # Chat message model
│   │   │   ├── res/
│   │   │   │   ├── layout/                   # UI layouts
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── item_chat_user.xml    # User message bubble
│   │   │   │   │   └── item_chat_ai.xml      # AI message bubble
│   │   │   │   ├── values/                   # Colors, strings, themes
│   │   │   │   └── drawable/                 # Images and drawables
│   │   │   └── AndroidManifest.xml           # App configuration
│   │   └── test/                             # Unit tests
│   └── build.gradle.kts                      # App dependencies
├── gradle/                                   # Gradle wrapper files
├── local.properties                          # API keys (gitignored!)
├── .gitignore                               # Files to ignore in Git
├── README.md                                # Project documentation
└── TEAM_SETUP_GUIDE.md                      # This file!
```

### Key Files to Know:

- **MainActivity.java** - Main screen with chat and emergency buttons
- **GeminiClient.java** - Handles API calls to Gemini AI
- **activity_main.xml** - Main screen layout
- **build.gradle.kts** - Dependencies and build configuration

---

## Common Issues & Solutions

### Issue: "JAVA_HOME is not set"

**Solution:**

```bash
# Windows - Set JAVA_HOME
setx JAVA_HOME "C:\Program Files\Android\Android Studio\jbr"

# macOS/Linux - Add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home)
```

### Issue: "API Key not found" / "BuildConfig.GEMINI_API_KEY"

**Solution:**

1. Make sure `local.properties` exists in project root
2. Add your API key: `GEMINI_API_KEY=YOUR_KEY_HERE`
3. Sync Gradle: **File → Sync Project with Gradle Files**

### Issue: Git merge conflicts

**Solution:**

1. Open Android Studio
2. Go to **VCS → Git → Resolve Conflicts**
3. Choose which version to keep (yours, theirs, or merge manually)
4. After resolving:

```bash
git add .
git commit -m "Resolve merge conflicts"
git push
```

### Issue: Can't push to main branch

**Solution:**

- Never push directly to `main`! Always create a feature branch
- Main branch is protected - use Pull Requests

### Issue: Gradle sync failed

**Solution:**

1. **File → Invalidate Caches / Restart**
2. Delete `.gradle` and `.idea` folders
3. Restart Android Studio
4. Let it re-sync

---

## Contributing Guidelines

### Code Style

- ✅ Use meaningful variable names
- ✅ Add comments for complex logic
- ✅ Follow Java naming conventions (camelCase for variables, PascalCase for classes)
- ✅ Keep methods short and focused (one responsibility)
- ✅ Format code before committing: **Ctrl+Alt+L** (Windows/Linux) or **Cmd+Option+L** (Mac)

### Before Committing

- [ ] Test your changes on an emulator or device
- [ ] Make sure the app builds without errors
- [ ] Remove any debug `Log` statements
- [ ] Check you're not committing `local.properties` or API keys
- [ ] Write a clear commit message

### Pull Request Checklist

- [ ] Code builds successfully
- [ ] No merge conflicts with `main`
- [ ] Clear description of what changed and why
- [ ] Screenshots included (if UI changes)
- [ ] Tested on Android device/emulator

---

## Quick Reference Commands

```bash
# Clone repository
git clone https://github.com/eduardodfran/Respondr.git

# Create new branch
git checkout -b feature/my-feature

# Check status
git status

# Stage changes
git add .

# Commit
git commit -m "Description of changes"

# Push branch
git push origin feature/my-feature

# Pull latest changes
git pull origin main

# Switch branches
git checkout branch-name

# List all branches
git branch -a

# Delete local branch
git branch -d feature/my-feature

# See commit history
git log --oneline
```

---

## Getting Help

### Stuck? Need help?

1. **Check existing issues**: [GitHub Issues](https://github.com/eduardodfran/Respondr/issues)
2. **Create a new issue**: Describe the problem with screenshots
3. **Ask the team**: Use your team chat (Discord/Slack/etc.)
4. **Documentation**:
   - [README.md](README.md) - Project overview
   - [FOLLOW_UP_QUESTIONS_FEATURE.md](FOLLOW_UP_QUESTIONS_FEATURE.md) - Follow-up questions system
   - [BEAUTIFUL_CHAT_UPDATE.md](BEAUTIFUL_CHAT_UPDATE.md) - Chat UI documentation

### Useful Resources

- [Android Developer Docs](https://developer.android.com/docs)
- [Git Cheat Sheet](https://education.github.com/git-cheat-sheet-education.pdf)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Material Design Guidelines](https://m3.material.io/)

---

## Ready to Start? 🎉

1. ✅ Clone the repository
2. ✅ Set up your API key in `local.properties`
3. ✅ Open in Android Studio
4. ✅ Create a feature branch
5. ✅ Start coding!

**Remember**: Ask questions if you're stuck. We're a team! 💪

---

_Last updated: November 2, 2025_
