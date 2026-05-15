# User Guide: AI Agent Bridge for Moneydance

This guide explains how to install and use the **AI Agent Bridge** to let local AI assistants (like Claude Desktop or Antigravity) "read" your Moneydance financial data securely.

## Prerequisites

1. **Moneydance**: Ensure you have Moneydance installed.
2. **Node.js**: You need Node.js (version 18 or higher) to run the bridge proxy. Download it from [nodejs.org](https://nodejs.org/).

---

## 1. Install the Moneydance Extension

1. Download the latest `mcpserver.mxt` from the [GitHub Releases](https://github.com/Spotlight250/md-mcp/releases).
2. Open Moneydance.
3. Go to **Extensions** -> **Manage Extensions**.
4. Click **Add From File...** and select the `mcpserver.mxt` you downloaded.
5. Click **Yes** when warned about the signature (this is a self-signed open-source tool).
6. The server starts automatically whenever your data file is open.

---

## 2. Connect Your AI Agent

The extension includes a built-in dashboard that generates your MCP configuration for you.

1. In Moneydance, go to **Extensions** -> **AI Agent Bridge (MCP)**.
2. The dashboard will open, showing a configuration block.
3. Click **Copy Configuration**.
4. Paste this configuration into your AI agent's MCP settings:
    - **Claude Desktop**: Open `claude_desktop_config.json` and paste into the `mcpServers` block.
    - **Cursor**: Go to Settings -> Features -> MCP and add a new server.
    - **Goose**: Add a new server in the Goose configuration.
5. **Restart your AI agent.**

> [!NOTE]
> The bridge script is automatically deployed to your `.moneydance/mcp/` folder the first time you run the extension. No manual downloads are required!

---

## 4. Usage

Once configured, you can ask your AI assistant questions like:

- *"What is my current net worth?"*
- *"Show me my 5 most recent transactions in my 'Groceries' category."*
- *"Analyze my investment performance since January 1st."*

## 🚀 AI Automation Skills

Beyond simple questions, you can use pre-defined **Skills** to perform deep analysis. To use a skill, simply copy the requirement into your chat or ask the agent to perform the specific audit.

### Available Skills

- **[Subscription Finder](../skills/subscription-finder/)**: Automatically scans your ledger to identify recurring payments, price increases, and hidden subscriptions.

> **Tip**: You can tell your agent: *"I've provided a subscription finder skill template in my files, please use it to audit my last 6 months of spending."*

---

## 🛠 Troubleshooting

- **Server Not Running**: In Moneydance, check **Help** -> **Console Window**. You should see `[MCP Server] MCP server started`.
- **Manual Start**: If the server didn't start automatically, go to **Extensions** -> **AI Agent Bridge (MCP)** to toggle it on.
- **Node Errors**: Ensure `node --version` works in your terminal.
