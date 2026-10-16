# Himeji Excel Export - Dev Setup (Windows)

The simplest ways to set up and develop this project on Windows are directly through **IntelliJ IDEA** or via **GitHub CLI (`gh`)**.

---

## Option 1: Setup Directly in IntelliJ IDEA

This workflow keeps the cloning, project import, SDK provisioning, and running/testing inside IntelliJ IDEA.

### 1. Install IntelliJ IDEA & Git
Install via `winget` (Windows Package Manager):
```powershell
winget install JetBrains.IntelliJIDEA.Community
```
*(Or download the installer from [jetbrains.com/idea](https://www.jetbrains.com/idea/download/) and [Git for Windows](https://git-scm.com/install/windows)).*

### 2. Clone and Open Project
1. Open IntelliJ IDEA.
2. On the Welcome screen, click **Get from VCS** (or navigate to **File > New > Project from Version Control...**).
3. Log in with your **GitHub** account or enter the repository URL:
   - `https://github.com/sprend/refactor-excel-export.git`
4. Choose a local directory and click **Clone**.

### 3. Automatic JDK & Gradle Provisioning
- IntelliJ IDEA automatically detects Gradle and uses the bundled Gradle Wrapper (`gradlew.bat`).
- **Java 17** is configured via Gradle Toolchains (`jvmToolchain(17)`). IntelliJ / Gradle will download and configure JDK 17 automatically if needed.

### 4. Run & Test via Toolbar
Pre-configured run configurations are available directly in the top run toolbar:
- **Run Tests**: Select **All JUnit tests** from the toolbar and click **Run** (or press `Ctrl+Shift+F10`).
- **Generate Sample Export**: Select **Run Main** from the toolbar to generate `sample-export.xlsx` in the project root.

---

## Option 2: Quick CLI Setup with GitHub CLI (`gh`)

Using GitHub CLI (`gh`) is the simplest terminal path because `gh auth login` handles GitHub authentication via browser OAuth, eliminating the need to manually generate, configure, and copy SSH keys.

### 1. Install GitHub CLI & IntelliJ IDEA
Install via `winget`:
```powershell
winget install --id GitHub.cli
winget install JetBrains.IntelliJIDEA.Community
```
*(Alternatively, install via Scoop: `scoop install gh`, Chocolatey: `choco install gh`, or download the installer from [cli.github.com](https://cli.github.com/)).*

### 2. Authenticate & Clone
In PowerShell or Windows Terminal:
```powershell
gh auth login
gh repo clone sprend/refactor-excel-export
cd refactor-excel-export
```

### 3. Open in IntelliJ IDEA
Open the project directory in IntelliJ IDEA:
```powershell
idea .
```
*(Or launch IntelliJ IDEA and open the cloned folder).*

### 4. Build, Test, and Run
IntelliJ and the Gradle Wrapper will automatically manage dependencies and JDK 17 via Gradle Toolchains.

From PowerShell / Command Prompt:
- **Run all tests**:
  ```powershell
  .\gradlew.bat test
  ```
- **Generate sample export**:
  ```powershell
  .\gradlew.bat run
  ```
