# Moneydance MCP Bridge 🚀

This repository provides a high-performance, secure bridge between [Moneydance](https://infinitekind.com/moneydance) and AI Agents via the [Model Context Protocol (MCP)](https://modelcontextprotocol.io).

When the Moneydance application is open and the extension is enabled, it allows local AI assistants to "read" your live financial data—including net worth, investment performance, and transaction history—directly from the source with zero cloud dependencies.

---

## 📥 Getting Started

### **I just want to use it**
If you are a Moneydance user who wants to connect to an AI agent (like Claude Desktop):
👉 **[Read the User Installation Guide](docs/user-guide.md)**

### **I want to contribute or build from source**
If you are a developer looking to extend the plugin or build the `.mxt` yourself:
👉 **[Read the Development & Build Guide](docs/development.md)**

---

## 🛠 Features

- **Financial Tools**: Query accounts, categories, net worth, and detailed transaction history with **high-fidelity metadata** (splits, tags, clearing status).
- **Investment Analytics**: Native gain/loss, cost basis, and ROI tracking using the internal Moneydance reporting engine.
- **Structural Resources**: Instant discovery of account and category hierarchies.
- **AI Automation Skills**: Pre-built templates for complex tasks like subscription auditing and tax prep.
- **Security First**: Hardened JSON infrastructure with strictly read-only operations and localhost-only binding.

👉 **[View Detailed Capabilities & Examples](docs/capabilities.md)**

---

## 🏗 Project Architecture

The project consists of two main components:
1.  **Java Plugin**: Runs inside Moneydance, providing a secure HTTP JSON-RPC endpoint.
2.  **Node.js Proxy**: Bridges the Java HTTP server to the standard MCP `stdio` transport.

👉 **[Architecture Deep Dive](docs/architecture.md)**

---

## 🏗 Technical Excellence

- **Hardened JSON Infrastructure**: A ground-up recursive descent parser ensures stability within the Moneydance classloader.
- **Zero-Dependency Proxy**: A standalone Node.js bridge that requires no installation or `npm install`.
- **Zero-Friction Deployment**: Automated bridge script extraction and configuration dashboard.
- **Isolated Formatting**: Separates data retrieval from JSON building for 100% test coverage of financial logic.
- **Zero Dependencies (Java)**: Pure Java implementation for maximum compatibility and security.

## 📜 Release History
Track all changes and project milestones in the **[Changelog](CHANGELOG.md)**.

---

## ⚖️ License
This project is licensed under the MIT License.
