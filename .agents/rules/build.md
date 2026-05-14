---
trigger: "build\\.gradle|gradlew|.*\\.mxt$"
description: Rules for building, signing, and deploying the Moneydance plugin using the devkit.
---
# Build & Deployment

- **Moneydance DevKit Constraints:** The Moneydance DevKit (`extadmin.jar`, `moneydance-dev.jar`) is proprietary. It is strictly loaded from `plugin/lib/`. 
- **No Maven Central:** Do not attempt to resolve Moneydance dependencies from Maven Central or other remote repositories.
- **Gradle:** Use the included Gradle wrapper (`.\gradlew.bat`).
- **Task:** The primary build task is `signExt`, which compiles, packages, and signs the `.mxt` file.
- **Signing:** Key generation and signing use the `extadmin.jar`. When prompted for a passphrase during builds, the dev standard is `devkey123`.
- **Manual Install:** Extensions are self-signed and must be manually loaded into Moneydance by the user.
