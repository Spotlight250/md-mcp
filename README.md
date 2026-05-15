# Moneydance MCP Server Plugin

This repository provides a high-performance, secure bridge between [Moneydance](https://infinitekind.com/moneydance) and AI Agents via the [Model Context Protocol (MCP)](https://modelcontextprotocol.io).

When the Moneydance application is open and the extension is enabled, it allows local AI assistants to "read" your live financial data—including net worth, investment performance, and transaction history—directly from the source with zero cloud dependencies.

## 🚀 Current Status: Phase 2.5 Complete

The server is fully functional for **Read-Only** operations. It features a hardened JSON infrastructure and a comprehensive toolset for personal finance analysis.

### Core Capabilities

- **Financial Tools**: Query accounts, categories, net worth, and detailed transaction history.
- **Investment Analytics**: ROI analysis, historical price tracking, and portfolio valuation.
- **Structural Resources**: Instant discovery of account and category hierarchies.
- **Security First**: 127.0.0.1 loopback only, zero external dependencies, and TDD-verified accuracy.

👉 **[View Detailed Capabilities & Example Queries](docs/capabilities.md)**

---

## 🛠 Documentation Index

1. **[Architecture Overview](docs/architecture.md)**
    - System flow, security model, and the Node.js Proxy rationale.
2. **[Building & Installing](docs/build-and-install.md)**
    - Compiling the `.mxt` extension and loading it into Moneydance.
3. **[MCP Client Integration](docs/mcp-client.md)**
    - Configuring Claude, Desktop apps, or the standalone test client.
4. **[Roadmap](TODO.md)**
    - Future phases including Budgets, Reminders, and AI-driven Categorization.

---

## ⚡ Quick Start

1. **Build the Plugin:** Run `.\gradlew.bat signExt` in the `plugin/` directory (Passphrase: `devkey123`).
2. **Install in Moneydance:** Go to *Extensions* -> *Manage Extensions* -> *Add From File...* and select `plugin/dist/mcpserver.mxt`.
3. **Start the Proxy:** Configure your MCP host to use the proxy script:

    ```json
    "moneydance": {
      "command": "node",
      "args": ["/path/to/md-mcp/client/src/mcp-proxy.mjs"]
    }
    ```

4. **Ask your Agent:** *"What is my current net worth across all accounts?"*

---

## 🏗 Technical Excellence

- **Custom JSON Infrastructure**: A ground-up recursive descent parser ensures stability within the Moneydance classloader.
- **Isolated Formatting**: Separates data retrieval from JSON building for 100% test coverage of financial logic.
- **Zero Dependencies**: Pure Java implementation for maximum compatibility and security.
