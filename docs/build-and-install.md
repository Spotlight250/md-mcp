# Building & Installing the Plugin

This guide explains how to compile the Moneydance MCP server plugin from source and install it into your local Moneydance application.

## Prerequisites

1. **Java Development Kit (JDK) 17+**
2. **Moneydance DevKit 5.1**
   - Download the devkit from [The Infinite Kind Developer Site](https://infinitekind.com/developer).
   - Extract the `.tar.gz` archive.
   - Copy `extadmin.jar` and `moneydance-dev.jar` from the extracted `lib/` directory into this project's `plugin/lib/` directory.

## Build Process

The project uses a standard Gradle wrapper to compile the Java source code and package it into a Moneydance Extension (`.mxt`) file.

1. Open a terminal in the `plugin/` directory.
2. Run the build task:
   ```bash
   .\gradlew.bat signExt
   ```
3. During the build process, you will be prompted to enter a **passphrase**. 
   - This unlocks the private key used to sign the extension. 
   - For local development, enter the default passphrase: `devkey123`.

If the build is successful, the signed extension file will be generated at:
`plugin/dist/mcpserver.mxt`

## Installation in Moneydance

Because this plugin is self-signed (not officially signed by The Infinite Kind), you must install it manually.

1. Open Moneydance.
2. In the top menu bar, click **Extensions** -> **Manage Extensions**.
3. Click the **Add From File...** button.
4. Navigate to and select the `plugin/dist/mcpserver.mxt` file you just built.
5. You will see a warning stating: *"The signature on this extension is either invalid or missing."* This is expected for locally built extensions. Click **Yes** to continue loading the extension.

## Verifying the Server

The plugin binds its lifecycle to your data file.

1. Ensure a Moneydance data file is open.
2. Go to **Help** -> **Console Window**.
3. Look for the following log entry:
   `[MCP Server] MCP server started on http://127.0.0.1:38867/mcp`
4. If you don't see it, you can manually trigger the server by clicking **Extensions** -> **MCP Server**.

When you close your data file, the server will gracefully shut down.
