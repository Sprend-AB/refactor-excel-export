# Himeji Excel Export

A standalone Kotlin/Java utility library and generator for exporting Himeji parcel data to styled Excel spreadsheets (`.xlsx`).

## The task

The task is to refactor the [ParcelExcelWriter](src/main/kotlin/com/sprend/himeji/original/ParcelExcelWriter.kt) for readability and maintainability. And you are lucky: there are tests in [ParcelExcelWriterTest](src/test/kotlin/com/sprend/himeji/ParcelExcelWriterTest.kt) to confirm that your changes don't break the functionality. Feel free to change the current code, or to completely rewrite it.

## Features

- **Excel Spreadsheet Generation**: Creates formatted `.xlsx` workbooks containing parcel data with custom styling and number formats.
- **Multilingual Support**: Supports Swedish and English localized headers and messages via XML properties resources.
- **Standalone CLI Generator**: Simple entry point ([com.sprend.himeji.Main](src/main/kotlin/com/sprend/himeji/Main.kt)) to generate sample exports for validation.

## Prerequisites

- **IntelliJ IDEA** (Community or Ultimate edition)
- (Or command line JDK 17 + Git)

## Quick Start in IntelliJ IDEA

1. In IntelliJ IDEA, select **Get from VCS** (or **File > New > Project from Version Control...**).
2. Enter the URL of this repository (or sign in with your GitHub account and select it) and click **Clone**.
3. IntelliJ IDEA automatically imports the Gradle project and provisions **JDK 17** via Gradle Toolchains.
4. Run tests or generate exports using the pre-configured toolbar configurations:
   - **All JUnit tests**: Runs the test suite.
   - **Run Main**: Generates `sample-export.xlsx` in the project root.

## Command Line Quick Start

### Build and Run Tests

```bash
./gradlew test
```

### Generate Sample Export

```bash
./gradlew run
```

## Detailed Development Setup

Platform-specific guides:

- [macOS Dev Setup Guide](docs/himeji-dev-setup-macos.md)
- [Windows Dev Setup Guide](docs/himeji-dev-setup-windows.md)
