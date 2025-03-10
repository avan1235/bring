# 🛒 Bring! - A Multiplatform Shopping List App

## 📱 Overview

Bring! is a modern, feature-rich shopping list application built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP). It allows users to create, manage, and share shopping lists across multiple platforms with a seamless, native-like experience.

## 🚀 Features

- ✅ Create and manage shopping lists
- ✅ Import shopping lists data from external websites
- ✅ Mark items as favorites for quick access
- ✅ Real-time synchronization across devices
- ✅ Customizable settings
- ✅ Beautiful, responsive UI with Material Design

## 🛠️ Technology Stack

### Client
- **Kotlin Multiplatform** - Share code across platforms
- **Compose Multiplatform** - UI framework for all platforms
- **Ktor Client** - HTTP client for API communication
- **KStore** - Cross-platform storage solution
- **Arrow** - Functional programming library
- **Kotlinx Serialization** - JSON/CBOR serialization
- **Kotlinx Coroutines** - Asynchronous programming
- **Kotlinx DateTime** - Cross-platform date/time handling
- **Kotlinx RPC** - Type-safe client-server communication

### Server
- **Ktor Server** - Asynchronous web framework
- **Exposed** - SQL framework for database access
- **PostgreSQL** - Relational database
- **GraalVM** - Native compilation for improved performance
- **Koin** - Dependency injection
- **Kotlinx RPC** - Type-safe client-server communication

## 💻 Supported Platforms

The client application supports:
- 📱 Android
- 🍎 iOS (requires macOS for building)
- 🖥️ Desktop (Windows, macOS, Linux)
- 🌐 Web (via WebAssembly)

## 🏗️ Project Structure

```
bring/
├── composeApp/           # Client application code
│   └── src/
│       ├── androidMain/   # Android-specific code
│       ├── commonMain/    # Shared client code
│       ├── iosMain/       # iOS-specific code
│       ├── jvmMain/       # Desktop-specific code
│       └── wasmJsMain/    # Web-specific code
├── server/               # Server application code
│   └── src/
│       └── main/         # Server implementation
├── shared/               # Shared code between client and server
│   └── src/
│       └── commonMain/   # Data models, API definitions
└── shared-client/        # Client-specific shared code
    └── src/
        └── commonMain/   # API clients, network communication
```

## 🚀 Getting Started

### Prerequisites
- JDK 11 or higher
- Docker and Docker Compose (for running the server)
- Android Studio or IntelliJ IDEA (for development)
- Xcode (for iOS development, macOS only)

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author
Maciej Procyk