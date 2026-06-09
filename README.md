# 🌍 IndiaConnect Native Language Translator

Visual-first, high-performance Android translation suite built to bridge regional and global communication gaps using modern declarative UI and on-device machine learning engines.

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2F%20Clean-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

🚀 **[Download the Release APK (v1.0.0)](https://github.com/JAKOB-010/IndiaConnect-Native-language-Translator/releases/download/v1.0.0/IndiaConnect_v1.0.apk)**

---

## 📖 Project Description

**IndiaConnect Native Language Translator** is a production-ready application engineered with **Jetpack Compose** and **Clean MVVM Architecture**. It provides a unified language state engine supporting 23 native Indian scripts, regional dialects, and major global languages. 

Unlike standard translation tooling, this application emphasizes privacy and runtime speed by executing critical text parsing and optical recognition directly on the device hardware, combined with isolated API endpoints for live data routing.

---

## 🗺️ Table of Contents
1. [📱 Features & Screenshots](#-features--screenshots)
2. [🏗️ Technical Architecture](#%EF%B8%8F-technical-architecture)
3. [⚙️ Installation & Setup](#%EF%B8%8F-installation--setup)
4. [🛠️ How to Use the Project](#%EF%B8%8F-how-to-use-the-project)
5. [📜 License](#-license)
6. [🤝 Credits & Acknowledgments](#-credits--acknowledgments)

---
## 📱 Features & Screenshots

| 💬 Text Translation | 🎙️ Live Conversation | 📷 Offline Scan (OCR) | 📖 Offline Phrasebook |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/879eae81-c999-4eb7-b08a-79ab51b73fae" width="200" alt="Text Translation"/> | <img src="https://github.com/user-attachments/assets/d35b9b30-4e77-44f6-b0bb-a6b8e4bfa53d" width="200" alt="live sample"/> | <img src="https://github.com/user-attachments/assets/bdf2e000-3f70-4e4d-ab11-359c1193be50" width="200" alt="scan sample"/> | <img src="https://github.com/user-attachments/assets/e23192fb-7a2c-4b29-ac1a-6990b91cdb7a" width="200" alt="offline sample"/> |

---

### 🚀 Core Feature Deep-Dive

* **💬 Text & Voice Translation Suite**
  A sleek, bidirectional interface supporting precise typing and real-time voice inputs. Features instantaneous character counts, immediate one-click clipboard copying, and sharp Text-to-Speech playback metrics.
  
  <table>
    <tr>
      <td align="center"><b>🎙️ Voice Interface</b><br/> <img src="https://github.com/user-attachments/assets/58fe9e09-1f1b-48d1-8a28-418d35b8142b" width="200" alt="voice sample"/></td>
      <td align="center"><b>🌐 Dynamic Language Selector</b><br/> <img src="https://github.com/user-attachments/assets/84b07d53-ba7a-4c01-974a-4b6fbc1d529e" width="200" alt="language selector"/></td>
    </tr>
  </table>

<br/>

* **🎙️ Live Conversation Mode**
  Engineered specifically for real-time, side-by-side interaction. Split-screen viewport controls isolate discrete user audio streams, making two-way native cross-language dialogues flow organically.

<br/>

* **📷 Offline Camera Scan (OCR)**
  Utilizes on-device hardware parsing powered by Google ML Kit. Instantly processes characters from targeted camera viewports or existing local gallery files completely isolated from network tracking.

<br/>

* **📖 Native Travel Phrasebook Matrix**
  A localized structural data repository map tracking key conversational modules across multiple script ecosystems. Allows localized module pre-loading so context navigation remains highly responsive offline.
  
  <table>
    <tr>
      <td align="center"><b>🇮🇳 Hindi Regional Script Dataset Sample</b><br/> <img src="https://github.com/user-attachments/assets/887def3f-21b7-47f6-8dca-ac759e96f910" width="200" alt="offline hindi"/></td>
    </tr>
  </table>

---
## 🏗️ Technical Architecture

<p align="center">
 <img width="4143" height="3795" alt="Jetpack Compose UI State-2026-06-09-112118" src="https://github.com/user-attachments/assets/aacc5986-93ab-4607-a684-f07439798bcd" />
</p>

The architecture is designed strictly around modern Android development standards, prioritizing modularity, off-the-main-thread processing, and full offline-first functionality. 

### 🔄 Architectural Pattern: MVVM + UDF
The application utilizes the **Model-View-ViewModel (MVVM)** pattern tightly coupled with **Unidirectional Data Flow (UDF)** to ensure UI state consistency:

* **UI Layer (View):** Built entirely with **Jetpack Compose**. Composables remain functional and stateless, rendering strictly based on incoming state flows and updating actions back up to the business layer.
* **State Management:** Retains application business logic and operational triggers safely across configuration shifts, exposing UI states cleanly to reactive interfaces.
* **Data & Service Layer (Repository):** Acts as a centralized mediator orchestrating on-device hardware engines, local memory collections, and ML processing routines.

---

### 🛠️ Core Technological Stack

| System Component | Engine / Framework | Purpose & Implementation |
| :--- | :--- | :--- |
| **UI Framework** | Jetpack Compose | Native Declarative UI implementing custom Material 3 Dark Themes. |
| **Concurrency** | Kotlin Coroutines & Flow | Asynchronous background processing for instant offline lookups and hardware audio processing. |
| **Machine Learning** | Google ML Kit (On-Device) | Specialized optical scanning engine supporting offline text parsing (Latin & Devanagari scripts). |
| **Speech-to-Text** | Android `SpeechRecognizer` | High-fidelity hardware microphone streaming for instant voice feature input processing. |
| **Text-to-Speech** | Android `TextToSpeech` (TTS) | Localized multi-accent synthesizer generating immediate vocal audio feedback. |
| **Camera Framework** | Jetpack CameraX | Highly optimized, lifecycle-aware hardware camera viewport layout for real-time OCR framing. |

---

### 📁 Project Directory Structure

```text
app/src/main/java/com/example/indiaconnect/
├── ui/                         # Core application package managing view state and data logic
│   ├── theme/                  # Unified token color palettes, styling rules, and typography
│   ├── LiveConversationView.kt # Split-screen real-time two-way voice chat interface
│   ├── Localization.kt         # Translation configuration matrix and locale setup maps
│   ├── ModuleViews.kt          # Reusable feature subcomponents and layout blocks
│   ├── OfflinePhrasebookView.kt# Native phrasebook categorization menu dashboard
│   ├── PhrasebookRepository.kt # Structural offline database map tracking travel modules
│   ├── ScanTranslationView.kt  # Optical scanner surface bound to local device lens modules
│   ├── Screens.kt              # View architecture routers and navigation anchors
│   ├── TextTranslationView.kt  # Core text input typing area with character count logic
│   ├── TranslationShared.kt    # Inter-feature shared translation utilities and data states
│   └── VoiceTranslationView.kt # Mic-driven interface supporting direct voice recordings
└── MainActivity.kt             # Primary single-activity application entry point

```
## 🚀 Installation & Setup

### 📋 Prerequisites
To build and run this project, ensure your development environment meets the following requirements:
* **Android Studio:** Ladybug / Koala or newer recommended.
* **Minimum SDK:** API Level 24 (Android 7.0 Nougat) or higher.
* **Testing Device:** A physical Android device is highly recommended to properly test the CameraX viewport (OCR) and native SpeechRecognizer/Microphone hardware features.

### 🛠️ Getting Started

**1. Clone the repository** Open your terminal and run the following command:
```bash
git clone [https://github.com/JAKOB-010/indiaconnect.git](https://github.com/YOUR_GITHUB_USERNAME/indiaconnect.git)
```
**2. Open in Android Studio**
    Launch Android Studio, click on **Open**, and navigate to the directory where you cloned the repository.

**3. Sync Gradle Dependencies**
    Once the project loads, Android Studio will prompt you to sync Gradle. Allow the build system to fetch all required Jetpack Compose and Google ML Kit dependencies.

**4. Build and Run**
    Connect your physical device (ensure USB Debugging is enabled in Developer Options) or launch an Android Emulator. Click the **Run** ▶️ button in the top toolbar.

    ---
> ⚠️ **Important Note on Offline Functionality:**
> This application is engineered to be an offline-first tool. However, the Google ML Kit OCR and Android Text-to-Speech (TTS) engines may require a brief, one-time cellular or Wi-Fi connection during their very first initialization on a new device to download the necessary native language models. Once these lightweight models are cached by the OS, the app operates 100% locally with zero network calls.

 ## 📖 How to Use

Once the application is installed and running on your device, you can navigate through the core modules to test its features. 

### 💬 1. Text & Voice Translation
* **Manual Typing:** Tap the input text field, select your source and target languages from the dynamic dropdowns, and begin typing. The translation will generate in real time.
* **Voice Input:** Tap the **Microphone (🎙️)** button. Speak naturally into your device, and the app will instantly transcribe and translate your voice.
* **Audio Playback:** Tap the **Speaker (🔊)** icon next to any translation block to hear the localized pronunciation via the native Text-to-Speech engine.

---

### 🎙️ 2. Live Conversation Mode
* **Setup:** Navigate to the Live Chat tab. The screen dynamically splits into two distinct viewports designed for two users sitting across from each other.
* **Interaction:** * User A taps their microphone and speaks in their selected language.
  * The app instantly translates and displays the text on User B's side of the screen.
  * User B then taps their microphone to reply, enabling a seamless, zero-latency two-way dialogue.

---

### 📷 3. Offline Camera Scan (OCR)
* **Live Camera Stream:** Open the Camera tab. Point your device's camera at any supported regional text (like menus, documents, or street signs). The ML Kit engine will recognize and translate the text on the fly.
* **Gallery Import:** Tap the **Gallery** icon to upload an existing photo or screenshot from your device. The app will extract and translate the strings directly from the image pixels.

---

 ### 📖 4. Native Travel Phrasebook
* **Browsing Modules:** Open the Phrasebook tab to explore pre-categorized situational datasets (e.g., *Greetings, Dining, Travel, Emergency*).
* **Learning & Audio:** Tap on any category to view essential daily phrases. Tap a specific phrase card to hear its native pronunciation without needing a network connection.


## 📄 License

This project is licensed under the **MIT License**. 

You are free to use, modify, and distribute this software as per the terms of the license. See the [LICENSE](LICENSE) file in the root directory for full details and legal conditions.

---
**Disclaimer:** *The Google ML Kit and Android SDK components used in this application are subject to their own respective Google API Terms of Service.*
## 🤝 Credits & Acknowledgments

This project was made possible thanks to several incredible open-source technologies, frameworks, and developer tools. A special thank you to:

* **[Google ML Kit](https://developers.google.com/ml-kit):** For providing the powerful, on-device machine learning models that drive the offline text recognition (OCR) and translation features.
* **[Jetpack Compose](https://developer.android.com/jetpack/compose):** For the modern, declarative UI toolkit that made building the dynamic, responsive interfaces a breeze.
* **[Android CameraX](https://developer.android.com/training/camerax):** For simplifying the complex hardware camera integration needed for the live scanning module.
* **[Material Design 3](https://m3.material.io/):** For the intuitive design system, accessible color palettes, and standardized iconography used throughout the application.
* **[Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html):** For enabling seamless, non-blocking background threads and reactive state management.

Finally, a huge thank you to the global open-source community and the extensive Android documentation that helped bring this offline-first translation tool to life!
