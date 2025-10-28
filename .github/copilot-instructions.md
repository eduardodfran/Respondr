# Respondr – AI Emergency Assistant (Student Project)

Respondr is a student-built Android application that serves as an AI-powered emergency assistant. It listens to the user’s voice or text input, detects the type of emergency using the Gemini Free API, and sends an alert with the user’s location to Firebase.

## Project Goal

Create a functional mobile prototype that uses AI to understand emergencies and respond intelligently. The focus is on functionality, not full deployment, so we will use free tools and student-friendly APIs.

## Tech Stack

Frontend: Java + Jetpack Compose  
AI: Gemini Free API (Google AI Studio key)  
Database: Firebase Firestore or Realtime Database  
Speech recognition: Android SpeechRecognizer  
Maps and location: Google Maps SDK  
Optional: Firebase Cloud Messaging for notifications  
Optional: Simple dashboard using HTML and Firebase Hosting

## App Flow

1. The user taps the Emergency button.  
2. The app listens to voice input or allows typing.  
3. The input text is sent to Gemini Free API.  
4. Gemini responds with the detected emergency type such as Fire, Medical, or Police.  
5. The app gets the current GPS location using FusedLocationProviderClient.  
6. The emergency type, description, and coordinates are sent to Firebase.  
7. Optional: A web dashboard or another mobile device connected to Firebase receives and displays the alert.

## Gemini API Example

Use your free Gemini API key from Google AI Studio.  
