# AI Agent Rules for md-mcp

You are working on `md-mcp`, a Moneydance plugin that exposes a Model Context Protocol (MCP) server. 
**You must strictly adhere to the following rules when working in this repository.**

## 1. Test-Driven Development (TDD) Mandate
- **Test First:** You MUST write failing unit tests before writing any production code.
- **Isolate Logic:** Because testing code inside the Moneydance JVM is difficult, extract business logic, JSON parsing, and MCP protocol formatting into plain Java classes that can be tested independently of the `FeatureModule` context.
- **Prove Failure:** Run the tests to prove they fail (Red), then write the minimal code to pass (Green), then refactor.

## 2. Zero External Dependencies (The Golden Rule)
- **No External Libraries:** Do NOT add dependencies like Jackson, Gson, Apache Commons, Spring, or heavy HTTP servers (like Tomcat/Jetty).
- **Why?** Moneydance plugins run in a highly customized, shared JVM classloader. External libraries frequently cause conflicts.
- **Implementation:** Use raw `java.net.ServerSocket` for HTTP transport. Use the custom, manual JSON parser inside `McpProtocolHandler.java` for all JSON manipulation. 

## 3. Moneydance DevKit Constraints
- **Local DevKit Only:** The Moneydance DevKit (`extadmin.jar`, `moneydance-dev.jar`) is proprietary. It is strictly loaded from `plugin/lib/`. 
- **No Maven Central:** Do not attempt to resolve Moneydance dependencies from Maven Central or other remote repositories.
- **Java Version:** Target Java 17. Do not use Kotlin to avoid version mismatch issues with the host app.

## 4. MCP Protocol & Proxy Architecture
- **Transport:** The Java plugin speaks HTTP POST (JSON-RPC) on `127.0.0.1:38867`. It does *not* speak standard MCP `stdio`.
- **The Proxy:** AI agents interface with the `client/src/mcp-proxy.mjs` Node script via `stdio`. The proxy translates this to HTTP.
- **Protocol Adherence:** Ensure all JSON-RPC responses strictly match the Model Context Protocol specification (e.g., proper JSON strings inside the `content[text]` field).

## 5. Security & Safety
- **Read-Only Default:** Assume all operations are read-only. Never implement a write/mutate operation on the financial data without explicit user permission and extensive safeguards.
- **Localhost Only:** Never bind the server to `0.0.0.0`. It must always be restricted to the loopback interface (`127.0.0.1`).
- **No Secrets in Output:** Never include raw passwords, sync keys, or encryption material in MCP responses or logs.

## 6. Build & Deployment
- **Gradle:** Use the included Gradle wrapper (`.\gradlew.bat`).
- **Task:** The primary build task is `signExt`, which compiles, packages, and signs the `.mxt` file.
- **Signing:** Key generation and signing use the `extadmin.jar`. When prompted for a passphrase during builds, the dev standard is `devkey123`.
- **Manual Install:** Extensions are self-signed and must be manually loaded into Moneydance by the user.
