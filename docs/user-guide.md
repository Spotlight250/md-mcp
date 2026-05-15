# User Guide: AI Agent Bridge for Moneydance

This guide explains how to install and use the **AI Agent Bridge** to let local AI assistants (like Claude Desktop or Antigravity) "read" your Moneydance financial data securely.

## Prerequisites

1.  **Moneydance**: Ensure you have Moneydance installed.
2.  **Node.js**: You need Node.js (version 18 or higher) to run the bridge proxy. Download it from [nodejs.org](https://nodejs.org/).

---

## 1. Install the Moneydance Extension

1.  Download the latest `mcpserver.mxt` from the [GitHub Releases](https://github.com/Spotlight250/md-mcp/releases).
2.  Open Moneydance.
3.  Go to **Extensions** -> **Manage Extensions**.
4.  Click **Add From File...** and select the `mcpserver.mxt` you downloaded.
5.  Click **Yes** when warned about the signature (this is a self-signed open-source tool).
6.  The server starts automatically whenever your data file is open.

---

## 2. Set Up the Proxy

AI agents communicate using a protocol called "stdio," but the Moneydance plugin uses "HTTP." We use a small script to bridge the two.

1.  Download the `mcp-proxy.mjs` script from the same [GitHub Release](https://github.com/Spotlight250/md-mcp/releases).
2.  Place it in a permanent folder on your computer (e.g., `C:\Tools\md-mcp\mcp-proxy.mjs`).

---

## 3. Configure Your AI Agent

You now need to tell your AI agent where the proxy script is.

### For Claude Desktop
1.  Open your Claude Desktop configuration file:
    - **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
    - **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
2.  Add the `moneydance` server to the `mcpServers` list:

```json
{
  "mcpServers": {
    "moneydance": {
      "command": "node",
      "args": [
        "C:/Absolute/Path/To/mcp-proxy.mjs"
      ]
    }
  }
}
```
*(Replace the path with the actual path to your `mcp-proxy.mjs` file.)*

3.  **Restart Claude Desktop.**

---

## 4. Usage

Once configured, you can ask your AI assistant questions like:
- *"What is my current net worth?"*
- *"Show me my 5 most recent transactions in my 'Groceries' category."*
- *"Analyze my investment performance since January 1st."*

## 🚀 AI Automation Skills

Beyond simple questions, you can use pre-defined **Skills** to perform deep analysis. To use a skill, simply copy the requirement into your chat or ask the agent to perform the specific audit.

### Available Skills:
- **[Subscription Finder](../skills/subscription_finder.md)**: Automatically scans your ledger to identify recurring payments, price increases, and hidden subscriptions.

> **Tip**: You can tell your agent: *"I've provided a subscription finder skill template in my files, please use it to audit my last 6 months of spending."*

---

## 🛠 Troubleshooting
- **Server Not Running**: In Moneydance, check **Help** -> **Console Window**. You should see `[MCP Server] MCP server started`.
- **Manual Start**: If the server didn't start automatically, go to **Extensions** -> **AI Agent Bridge (MCP)** to toggle it on.
- **Node Errors**: Ensure `node --version` works in your terminal.
