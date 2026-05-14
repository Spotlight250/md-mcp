# Moneydance MCP Server Roadmap & Capabilities

To make the Moneydance MCP server genuinely useful for an AI agent, it needs to provide a comprehensive view of the user's financial state, allow deep-dives into historical data, and eventually support intelligent automation (like categorization).

The capabilities are broken down into logical phases.

## Phase 1: Core Read-Only Access (The Foundation)
These tools are essential for the agent to answer basic questions like *"What is my net worth?"*, *"How much did I spend on groceries last month?"*, or *"Do I have enough cash in my checking account?"*

- [ ] `get_net_worth`
  - **Description:** Returns a high-level summary of total assets, liabilities, and net worth in the base currency.
- [ ] `get_accounts`
  - **Description:** Lists all accounts (bank, credit card, investment, loan, asset).
  - **Data:** Account ID, name, type, current balance, cleared balance, and currency.
- [ ] `get_categories`
  - **Description:** Lists all income and expense categories (the chart of accounts).
  - **Data:** Category ID, name, parent category, and type (income/expense).
- [ ] `get_transactions`
  - **Description:** Queries historical transactions across all accounts.
  - **Parameters:** `account_id` (optional), `start_date`, `end_date`, `category_id` (optional), `payee_match` (optional).
  - **Data:** Date, payee, amount, category, memo, tags, and status (cleared/reconciled).

## Phase 2: Advanced Read-Only (Investments & Planning)
These tools allow the agent to answer questions like *"How is my retirement portfolio performing?"* or *"What bills are due next week?"*

- [ ] `get_investments`
  - **Description:** Lists all securities held within investment accounts.
  - **Data:** Ticker symbol, security name, quantity held, current price, and total value.
- [ ] `get_currencies`
  - **Description:** Lists available currencies and their current exchange rates relative to the base currency.
- [ ] `get_reminders`
  - **Description:** Lists upcoming scheduled transactions (reminders).
  - **Parameters:** `days_ahead` (e.g., next 30 days).
  - **Data:** Next date, payee, amount, and frequency.
- [ ] `get_budgets`
  - **Description:** Retrieves the current budget limits and actual spending against them.

## Phase 3: AI Automation (Write Operations)
> **Security Note:** Write operations must be strictly opt-in via plugin settings, as the agent could mutate financial records.

These tools allow the agent to actively help manage finances, like *"Categorize my recent Amazon purchases based on my email receipts."*

- [ ] `update_transaction`
  - **Description:** Modifies an existing transaction.
  - **Use Case:** The agent identifies an uncategorized transaction and assigns the correct category or tags.
- [ ] `add_transaction`
  - **Description:** Inserts a new transaction into an account.
  - **Use Case:** The agent parses a receipt provided by the user and automatically records the expense.

---

## Technical Debt & Infrastructure
- [ ] **JSON Parser Hardening:** Replace or harden the manual JSON string builder to safely handle all edge cases (escaping quotes, newlines, nulls) when data models get complex.
- [ ] **Dynamic Tool Registration:** Refactor `McpProtocolHandler.java` to use a dynamic registry pattern for tools, making it easy to plug in new capabilities without massive `switch` statements.
- [ ] **Error Handling:** Standardize JSON-RPC error responses for when Moneydance data is inaccessible or queries fail.
