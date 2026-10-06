# Stonic Phone AI — MVP

Android starter project for a Stonic-style personal assistant.

## Included
- Hindi voice input via Android SpeechRecognizer
- Hindi text-to-speech response
- Local commands for Settings, Wi-Fi, Bluetooth
- Launch shortcuts for YouTube, Chrome, WhatsApp, Instagram and Camera
- Dark assistant UI
- Clean place to add Gemini backend/agent tools

## Important
This MVP intentionally does NOT embed a Gemini API key. Add a secure backend/proxy in the next stage. Google's current Gemini docs recommend the Interactions API for new projects.

## Build
Open the folder in Android Studio, let Gradle sync, then Build > Build APK(s).

## Next stage
1. Gemini backend with tool/function calling
2. App discovery instead of hard-coded package names
3. Accessibility service for user-approved UI automation
4. Floating overlay assistant
5. Local memory and settings
6. Web search/tool use
