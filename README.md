# Oratio 🕊️

**Oratio** is an Android application built with **Kotlin** and **Jetpack Compose**, designed to organize and present prayers and devotions in multiple languages (such as Latin, Portuguese, English, and Spanish) with **100% offline-first capability**.

---

## ✨ Main Features

- 🌐 **Multilingual Support:** Prayers available in **Latin** (`la`), **Portuguese** (`pt`), **English** (`en`), and **Spanish** (`es`).
- 📖 **Parallel Bilingual Mode:** View the original text (e.g., Latin) alongside its translation (e.g., English or Portuguese) side by side in real time.
- 💾 **Offline-First Storage:** Local SQLite database managed via **Room Database**, automatically pre-populated on first launch using a JSON seed file.
- 🔍 **Fast Search:** Instant full-text search across titles, categories, or keywords within prayer contents.
- ⭐ **Favorites:** Bookmark favorite prayers for quick access.
- 🎨 **Modern Design:** Built with **Jetpack Compose** following **Material Design 3** guidelines.

---

## 🛠️ Tech Stack

| Technology | Description |
| :--- | :--- |
| **Kotlin** | Primary programming language (v`2.2.10`) |
| **Jetpack Compose** | Modern declarative UI toolkit |
| **Material 3** | Latest design system components and themes |
| **Room Database** | Official Android library for SQLite persistence (v`2.7.2`) |
| **KSP** | Kotlin Symbol Processing for Room code generation (v`2.2.10-2.0.2`) |
| **KotlinX Serialization** | JSON parsing and data serialization (v`1.8.0`) |

---

## 📂 Project Structure

```text
app/src/main/java/cnc/oratio/
├── data/
│   ├── local/
│   │   ├── dao/                 # Data Access Objects (PrayerDao)
│   │   ├── database/            # OratioDatabase and DatabaseInitializer
│   │   ├── entity/              # Room Entities (Prayer, Translation, Category, Language)
│   │   └── model/               # Relational models and JSON serialization DTOs
│   └── repository/              # PrayerRepository abstraction layer
└── ui/
    ├── theme/                   # Material Design 3 theme, colors, and typography
    ├── PrayerScreen.kt          # Main screen with language navigation and bilingual view
    └── MainActivity.kt          # Main entry Activity and repository initialization
```

---

## 🚀 How to Run the Project

### Prerequisites
- **Android Studio** (Ladybug release or newer recommended)
- **JDK 17** or higher
- **Android SDK 37**

### Steps to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/carllosnc/oratio.git
   cd oratio
   ```

2. **Build the application via CLI:**
   - **Linux/macOS:**
     ```bash
     ./gradlew assembleDebug
     ```
   - **Windows (PowerShell/CMD):**
     ```cmd
     .\gradlew.bat assembleDebug
     ```

3. **Run on Emulator or Physical Device:**
   Open the project folder in Android Studio and click **Run (Shift + F10)**.

---

## 📝 Adding New Prayers

The application uses a seed file located at `app/src/main/assets/prayers_seed.json`. To add new prayers or translations, add a new JSON object following this structure:

```json
{
  "id": "prayer_unique_id",
  "categoryId": "basic",
  "defaultTitle": "Default Title",
  "translations": [
    {
      "languageCode": "la",
      "title": "Title in Latin",
      "subtitle": "Subtitle",
      "content": "Prayer text in Latin...",
      "notes": "Historical context or liturgical notes"
    },
    {
      "languageCode": "en",
      "title": "Title in English",
      "subtitle": "Subtitle",
      "content": "Prayer text in English...",
      "notes": "Notes in English"
    }
  ]
}
```

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
