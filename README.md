# 🛒 Bring! - A Multiplatform Shopping List App

[![Platforms](https://img.shields.io/badge/web-WebAssembly-blue)](https://bring.procyk.in)
[![Platforms](https://img.shields.io/badge/mobile-Android%20%7C%20iOS-blue)](https://github.com/avan1235/bring/releases/latest)
[![Platforms](https://img.shields.io/badge/desktop-Windows%20%7C%20macOS%20%7C%20Linux-blue)](https://github.com/avan1235/bring/releases/latest)

[![Build](https://img.shields.io/github/actions/workflow/status/avan1235/bring/release.yml?label=Build&color=green)](https://github.com/avan1235/bring/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/avan1235/bring?label=Release&color=green)](https://github.com/avan1235/bring/releases/latest)
[![Google Play](https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Din.procyk.bring%26l%3DGoogle%2520Play%26m%3D%24version)](https://play.google.com/store/apps/details?id=in.procyk.bring)
[![Docker](https://img.shields.io/docker/v/avan1235/bring?label=Docker%20Hub&color=green)](https://hub.docker.com/repository/docker/avan1235/bring/tags?ordering=last_updated)

[![License: MIT](https://img.shields.io/badge/License-MIT-red.svg)](./LICENSE.md)
[![GitHub Repo stars](https://img.shields.io/github/stars/avan1235/bring?style=social)](https://github.com/avan1235/bring/stargazers)
[![Fork Mini Games](https://img.shields.io/github/forks/avan1235/bring?logo=github&style=social)](https://github.com/avan1235/bring/fork)

## 📱 Overview

Bring! is a modern, feature-rich shopping list application built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP). It allows users to create, manage, and share shopping lists across multiple platforms with a seamless, native-like experience.

Web version is publicly available at https://bring.procyk.in

<div style="display: flex; justify-content: center; flex-wrap: nowrap;">
  <img src="composeApp/screenshots/edit-list-screen-options.png" style="width: 20%;" />
  <img src="composeApp/screenshots/create-list-screen-custom-name.png" style="width: 20%;" />
  <img src="composeApp/screenshots/create-list-screen-join-list.png" style="width: 20%;" />
</div>
<div style="display: flex; justify-content: center; flex-wrap: nowrap;">
  <img src="composeApp/screenshots/favorites-screen-collections.png" style="width: 20%;" />
  <img src="composeApp/screenshots/settings-screen-options.png" style="width: 20%;" />
</div>


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