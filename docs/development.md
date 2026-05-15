# Development & Build Guide

This guide explains how to compile the Moneydance MCP server plugin from source and set up your local development environment.

## Prerequisites

1.  **Java Development Kit (JDK) 17+**
2.  **Moneydance DevKit 5.1**
    - Download the devkit from [The Infinite Kind Developer Site](https://infinitekind.com/developer).
    - Extract the `.tar.gz` archive.
    - Copy `extadmin.jar` and `moneydance-dev.jar` from the extracted `lib/` directory into this project's `plugin/lib/` directory.

## Build Process

The project uses a standard Gradle wrapper to compile the Java source code and package it into a Moneydance Extension (`.mxt`) file.

1.  Open a terminal in the `plugin/` directory.
2.  Run the build task:
    ```bash
    .\gradlew.bat signExt
    ```
3.  During the build process, you will be prompted to enter a **passphrase**. 
    - This unlocks the private key used to sign the extension. 
    - For local development, enter the default passphrase: `devkey123`.

If the build is successful, the signed extension file will be generated at:
`plugin/dist/mcpserver.mxt`

## Installation for Developers

If you have built the `.mxt` yourself, follow the same installation steps as an end-user, but select your locally built file.

👉 **[See User Installation Guide](user-guide.md)**

## Testing

We use JUnit 5 for unit testing. Logic is isolated into plain Java classes to allow testing without the Moneydance JVM.

```bash
cd plugin
.\gradlew.bat test
```

## Protocol Implementation Notes

- **HTTP Endpoint**: The Java plugin listens on `127.0.0.1:38867`.
- **JSON Format**: We use a custom, zero-dependency JSON builder and parser to avoid classloader conflicts.
- **MCP Version**: Target version `2024-11-05`.
