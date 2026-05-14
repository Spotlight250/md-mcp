---
trigger: ".*\\.mjs$|.*\\.java$"
description: Documentation on the Node proxy architecture and MCP protocol integration specifics.
---
# MCP Protocol & Proxy Architecture

- **Transport:** The Java plugin speaks HTTP POST (JSON-RPC) on `127.0.0.1:38867`. It does *not* speak standard MCP `stdio`.
- **The Proxy:** AI agents interface with the `client/src/mcp-proxy.mjs` Node script via `stdio`. The proxy translates this to HTTP.
- **Protocol Adherence:** Ensure all JSON-RPC responses strictly match the Model Context Protocol specification (e.g., proper JSON strings inside the `content[text]` field).
