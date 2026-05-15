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
- *"Show me my account balances as of January 1st."*
- *"Show me my 5 most recent transactions in my 'Groceries' category."*
- *"Analyze my investment performance since January 1st."*

## 🚀 AI Automation Skills

"Skills" are advanced prompts or workflows that help the AI perform complex audits (like identifying subscriptions or prepping for taxes) by chaining multiple MCP tool calls together.

### How to use a Skill:

1. Go to your AI agent's **Skills**, **Plugins**, or **Tools** menu.
2. Click **Create Skill**, **Upload**, or **Add Plugin**.
3. Select the `.zip` file you downloaded from the [GitHub Releases](https://github.com/Spotlight250/md-mcp/releases).
4. Your agent uses the pre-defined logic and prompts for advanced financial audits.

### Available Skills

- **[Subscription Finder](../skills/subscription-finder/)**: Scans your ledger to identify recurring payments, detect price increases, and highlight "zombie" subscriptions.
    - *Prompt Example*: "Use the subscription finder skill logic to audit my last 6 months of spending and show me everything that looks like a recurring payment."

> **Tip**: Skills work best when you provide a specific timeframe (e.g., "last 90 days") to minimize data processing.

---

## 🔒 Privacy & Security

The AI Agent Bridge is built with a **"local-first"** philosophy, but it is important to understand how your data moves:

1.  **Local Processing**: The bridge and the data fetch happen entirely on your computer. Your Moneydance data is never "streamed" to a central server.
2.  **On-Demand Sharing**: Your financial data is **only** sent to your AI provider (e.g., Anthropic, Google, or OpenAI) when you ask a question that requires it. 
3.  **Selective Context**: The bridge only shares the specific data needed to answer your question (e.g., "last 90 days of transactions"). It never sends your entire ledger unless you explicitly ask for a full audit.
4.  **No Training**: Most "Pro" or "Enterprise" AI tiers do not use your conversation data to train their models, but we recommend checking your specific provider's privacy policy.

> [!IMPORTANT]
> If you require 100% air-gapped privacy, you can use this bridge with **local AI models** (like Llama 3 via Goose or LM Studio).

---

## 🛠 Troubleshooting

- **Server Not Running**: In Moneydance, check **Help** -> **Console Window**. You should see `[MCP Server] MCP server started`.
- **Manual Start**: If the server didn't start automatically, go to **Extensions** -> **AI Agent Bridge (MCP)** to toggle it on.
- **Node Errors**: Ensure `node --version` works in your terminal.
