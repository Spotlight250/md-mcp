---
trigger: always_on
description: Java environment constraints including zero-dependency requirements and JDK 17 targeting.
---

# Java & Dependencies Constraints

- **Zero External Dependencies:** Do NOT add dependencies like Jackson, Gson, Apache Commons, Spring, or heavy HTTP servers (like Tomcat/Jetty).
- **Why?** Moneydance plugins run in a highly customized, shared JVM classloader. External libraries frequently cause conflicts.
- **Implementation:** Use raw `java.net.ServerSocket` for HTTP transport. Use the custom, manual JSON parser inside `McpProtocolHandler.java` for all JSON manipulation.
- **Java Version:** Target Java 17. Do not use Kotlin to avoid version mismatch issues with the host app.
