# Oratio 🕊️

**Oratio** é um aplicativo Android desenvolvido em **Kotlin** e **Jetpack Compose** projetado para reunir e disponibilizar orações e rezas em múltiplos idiomas (como Português, Latim, Inglês e Espanhol) com funcionamento **100% offline**.

---

## ✨ Funcionalidades Principais

- 🌐 **Suporte Multilíngue:** Orações disponíveis em **Latim** (`la`), **Português** (`pt`), **Inglês** (`en`) e **Espanhol** (`es`).
- 📖 **Modo Bilíngue Paralelo:** Permite visualizar o texto original (ex: Latim) e a tradução (ex: Português) lado a lado em tempo real.
- 💾 **Funcionamento Offline (Offline-First):** Armazenamento em banco de dados SQLite local gerenciado via **Room Database**, populado automaticamente no primeiro acesso através de um arquivo de semente em JSON.
- 🔍 **Busca Rápida:** Pesquisa textual instantânea por títulos, categorias ou palavras-chave dentro do conteúdo das orações.
- ⭐ **Favoritos:** Permite salvar orações favoritas para consulta rápida.
- 🎨 **Interface Moderna:** Construída com **Jetpack Compose** e diretrizes de design do **Material Design 3**.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Descrição |
| :--- | :--- |
| **Kotlin** | Linguagem principal do projeto (v`2.2.10`) |
| **Jetpack Compose** | Toolkit moderno para construção de interfaces declarativas |
| **Material 3** | Componentes e temas visuais atualizados |
| **Room Database** | Biblioteca oficial do Android para persistência SQLite (v`2.7.2`) |
| **KSP** | Kotlin Symbol Processing para geração de código do Room (v`2.2.10-2.0.2`) |
| **KotlinX Serialization** | Parsing e serialização de dados JSON (v`1.8.0`) |

---

## 📂 Estrutura do Projeto

```text
app/src/main/java/cnc/oratio/
├── data/
│   ├── local/
│   │   ├── dao/                 # Data Access Objects (PrayerDao)
│   │   ├── database/            # OratioDatabase e DatabaseInitializer
│   │   ├── entity/              # Entidades Room (Prayer, Translation, Category, Language)
│   │   └── model/               # Modelos relacionais e de serialização JSON
│   └── repository/              # PrayerRepository abstraindo a camada de dados
└── ui/
    ├── theme/                   # Tema visual, cores e tipografia Material 3
    ├── PrayerScreen.kt          # Interface principal com navegação de idiomas e modo bilíngue
    └── MainActivity.kt          # Activity principal e inicialização do repositório
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Android Studio** (versão Ladybug ou superior recomendada)
- **JDK 17** ou superior
- **Android SDK 37**

### Passos para Executar

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/carllosnc/oratio.git
   cd oratio
   ```

2. **Compilar a aplicação via linha de comando:**
   - **Linux/macOS:**
     ```bash
     ./gradlew assembleDebug
     ```
   - **Windows (PowerShell/CMD):**
     ```cmd
     .\gradlew.bat assembleDebug
     ```

3. **Executar no Emulador ou Dispositivo Físico:**
   Abra a pasta do projeto no Android Studio e clique em **Run (Shift + F10)**.

---

## 📝 Adicionando Novas Orações

O projeto utiliza o arquivo semente localizado em `app/src/main/assets/prayers_seed.json`. Para adicionar novas orações ou traduções, basta incluir um novo objeto no JSON seguindo a estrutura:

```json
{
  "id": "nome_da_oracao",
  "categoryId": "basic",
  "defaultTitle": "Título Padrão",
  "translations": [
    {
      "languageCode": "la",
      "title": "Título em Latim",
      "subtitle": "Subtítulo",
      "content": "Texto da oração em Latim...",
      "notes": "Notas ou contexto histórico"
    },
    {
      "languageCode": "pt",
      "title": "Título em Português",
      "subtitle": "Subtítulo",
      "content": "Texto da oração em Português...",
      "notes": "Notas em Português"
    }
  ]
}
```

---

## 📄 Licença

Este projeto está sob a licença **MIT**. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
