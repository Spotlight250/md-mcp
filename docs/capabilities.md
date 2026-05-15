# Capabilities & Integration Guide

The Moneydance MCP Server exposes your financial data through three primary building blocks: **Tools**, **Resources**, and **Infrastructure**.

## 1. Tools (Executable Actions)

Tools are active functions that the AI Agent can invoke to query specific data points or perform analysis.

### Core Finance
| Tool | Description | Key Parameters |
| :--- | :--- | :--- |
| `get_net_worth` | Returns total assets, liabilities, and net worth. | `as_of_date`, `account_ids` |
| `get_accounts` | Lists bank, credit card, investment, and loan accounts. Supports historical dates and balance types. | `as_of_date`, `cleared_only`, `include_inactive`, `include_hidden` |
| `get_categories` | Lists all income and expense categories. | None |
| `get_transactions` | Search the ledger for specific activity. | `start_date`, `end_date`, `payee`, `category` |

### Investments & Performance
| Tool | Description | Key Parameters |
| :--- | :--- | :--- |
| `get_investments` | Lists all held securities with shares and current value. | None |
| `get_security_prices` | Returns historical price snapshots for a security. | `ticker`, `start_date`, `end_date` |
| `get_security_performance` | Provides ROI, gain/loss, and trade history. | `ticker`, `account_id` |
| `get_currencies` | Current exchange rates relative to base currency. | None |

---

## 2. Resources (Contextual Data)

Resources provide the agent with a "map" of your data structure without requiring manual tool calls. These are available at connection time.

*   **`mcp://moneydance/accounts/hierarchy`** (JSON)
    *   **Purpose:** Explains the relationship between parent and child accounts.
    *   **Agent Benefit:** Allows the agent to understand that "Checking Account A" belongs to "Bank X" instantly.
*   **`mcp://moneydance/categories/full`** (JSON)
    *   **Purpose:** The complete chart of accounts for income and expenses.
    *   **Agent Benefit:** Essential for the agent to suggest accurate categorization for new transactions.
*   **`mcp://moneydance/securities/master`** (JSON)
    *   **Purpose:** A master list of every ticker and security name in the file.
    *   **Agent Benefit:** Helps the agent resolve ambiguous references to companies or assets.

---

## 3. Security & Infrastructure

Built for privacy-conscious financial management.

*   **Zero-Cloud Path:** Data is read directly from the Moneydance JVM. It never touches a third-party server or cloud bridge before reaching your local AI agent.
*   **Loopback Security:** The embedded server binds exclusively to `127.0.0.1`. It is inaccessible from your local network or the internet.
*   **Recursive Descent JSON Parser:** To maintain a zero-dependency footprint (critical for Moneydance plugin stability), we implemented a custom, high-performance JSON parser that safely handles nested financial data and escaping.
*   **Test-Driven Reliability:** Every tool and formatter is backed by unit tests, ensuring that ROI calculations and currency conversions are bit-for-bit accurate.

## 4. Example Queries

The agent can now answer complex questions such as:
- *"Show me my top 5 investment holdings by current value."*
- *"What is my current net worth?"*
- *"Show me my account balances as of January 1st."*
- *"Show me my 5 most recent transactions in my 'Groceries' category."*
- *"Analyze my investment performance since January 1st."*
- *"Does my net worth include the cash in my brokerage account?"*
