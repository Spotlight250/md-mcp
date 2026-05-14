# Moneydance MCP Server Plugin

This repository provides a complete integration bridge between [Moneydance](https://infinitekind.com/moneydance) (a desktop personal finance application) and AI Agents via the [Model Context Protocol (MCP)](https://modelcontextprotocol.io).

It allows AI assistants (like Claude, Antigravity, or any MCP-compatible agent) to query your live, encrypted financial data securely, without the data ever leaving your local machine.

## How It Works

Moneydance uses a proprietary, encrypted data format. To access this data safely, this project provides a **Java Plugin** that runs directly inside the Moneydance application. The plugin hosts a lightweight, zero-dependency HTTP server that speaks the MCP JSON-RPC protocol over a local socket.

Because most AI agents expect MCP servers to communicate over Standard I/O (`stdio`), a tiny **Node.js Proxy** is provided to translate between the agent and the Moneydance plugin.

## Documentation

The documentation has been modularized for clarity:

1. **[Architecture Overview](docs/architecture.md)**
   - Detailed rationale for the plugin approach, system flow diagrams, and the security model.
2. **[Building & Installing](docs/build-and-install.md)**
   - Prerequisites (Java, MD DevKit), how to compile the `.mxt` extension, and how to install it into Moneydance.
3. **[MCP Client Integration](docs/mcp-client.md)**
   - How to configure an AI agent to use the Node.js proxy, and how to run the standalone test client.

## Quick Start (For End Users)

If you already have a compiled `mcpserver.mxt` file and just want to connect your agent:

1. **Install the Plugin:** In Moneydance, go to *Extensions* -> *Manage Extensions* -> *Add From File...* and select `mcpserver.mxt`.
2. **Start the Server:** Open your data file. (Check *Help* -> *Console Window* for the `[MCP Server] MCP server started` message).
3. **Configure Your Agent:** Add the proxy script to your agent's MCP configuration (e.g., `mcp_config.json`):
   ```json
   {
     "mcpServers": {
       "moneydance": {
         "command": "node",
         "args": ["/absolute/path/to/md-mcp/client/src/mcp-proxy.mjs"]
       }
     }
   }
   ```
4. Ask your agent to list your tools!

## Current Capabilities

The server currently implements a "Hello World" skeleton to prove the end-to-end transport layer:
- `ping`: Returns server status, the Moneydance build number, and the name of the currently open data file.

*(Future implementation phases will add tools to query account balances, transactions, and budgets.)*
