# MCP Client Integration

Because Moneydance is a standalone desktop application, the plugin runs an embedded HTTP server to expose the Model Context Protocol (MCP). However, most AI agents expect MCP servers to be command-line executables that communicate over Standard Input/Output (`stdio`).

To bridge this gap, this repository provides a Node.js proxy script.

## The Stdio Proxy (`client/src/mcp-proxy.mjs`)

The proxy acts as a translation layer:
1. It runs as an MCP `stdio` server, attaching directly to the AI agent.
2. It receives JSON-RPC requests from the agent over `stdin`.
3. It forwards those requests as HTTP POST payloads to the Moneydance plugin (`http://127.0.0.1:38867/mcp`).
4. It relays the HTTP responses back to the agent over `stdout`.

### Proxy Configuration

To configure an AI agent (like Antigravity or Claude Desktop) to use the Moneydance MCP server, add the proxy script to the agent's MCP configuration file (e.g., `mcp_config.json`):

```json
{
  "mcpServers": {
    "moneydance": {
      "command": "node",
      "args": [
        "c:/Absolute/Path/To/md-mcp/client/src/mcp-proxy.mjs"
      ]
    }
  }
}
```

*Note: Ensure the absolute path points correctly to where you cloned this repository.*

## The Test Client (`client/src/test-client.mjs`)

If you are developing new tools in the Java plugin, you can use the standalone test client to verify the raw HTTP endpoints without needing a full AI agent attached.

The test client performs a full MCP handshake over HTTP and calls the `ping` tool.

1. Navigate to the `client/` directory.
2. Run the tests:
   ```bash
   npm run test
   ```
3. The output will show the raw JSON-RPC requests and responses.
