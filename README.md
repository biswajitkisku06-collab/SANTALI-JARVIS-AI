# SANTALI JARVIS AI

Android-first personal AI assistant starter project.

## What is included
- Android app: Kotlin + Jetpack Compose
- Node.js backend: Gemini API integration
- GitHub Actions workflow to build an APK
- A clear separation between Android client and backend

This is a working foundation, not a finished ChatGPT-level product. Voice, long-term memory, web search, image understanding, and Android actions are planned modules and must be implemented and tested in later phases.

## Requirements
- GitHub account
- Android phone for testing
- A Gemini API key for AI replies
- A Node.js hosting service for the backend

## Build Android APK with GitHub Actions
1. Create a private GitHub repository named `SANTALI-JARVIS-AI`.
2. Upload the **contents** of this project ZIP into the repository root (not the ZIP file itself).
3. Open the repository's **Actions** tab.
4. Enable Actions if GitHub asks.
5. Run `Build SANTALI JARVIS APK` using **Run workflow**.
6. When it succeeds, open the workflow run and download the `Santali-Jarvis-debug-apk` artifact.

## Backend setup
1. Deploy the `backend` folder to a Node.js hosting service.
2. Set environment variables:
   - `GEMINI_API_KEY` = your Gemini API key
   - `PORT` = `3000` (many hosts set this automatically)
3. Start command: `npm start`
4. Copy the deployed HTTPS URL.
5. In the Android app, open Settings and enter that backend URL.

Never put your Gemini API key inside the Android app or upload it to GitHub.

## Current app behavior
The Android app provides a chat screen and backend URL setting. It sends chat messages to:
`POST /api/chat`
with JSON:
`{"message":"Hello","history":[]}`

The backend returns:
`{"reply":"..."}`

## Planned modules
- Multi-model router (Gemini first; other providers only after adding their APIs)
- Conversation memory (PostgreSQL)
- Voice input/output (Android speech APIs + ElevenLabs)
- Web search with citations
- Image understanding
- Permission-based Android actions
- Security, rate limiting, and audit logs
