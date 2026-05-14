# Moneydance MCP Server Roadmap & Capabilities

To make the Moneydance MCP server genuinely useful for an AI agent, it needs to provide a comprehensive view of the user's financial state, allow deep-dives into historical data, and eventually support intelligent automation (like categorization).

The capabilities are broken down into logical phases.

## Phase 1: Core Read-Only Access (The Foundation)
These tools are essential for the agent to answer basic questions like *"What is my net worth?"*, *"How much did I spend on groceries last month?"*, or *"Do I have enough cash in my checking account?"*

- [ ] `get_net_worth`
  - **Description:** Returns total assets, liabilities, and net worth in the base currency. Can be queried for specific accounts or across the entire file.
  - **Parameters:** `as_of_date` (optional, defaults to today), `account_ids` (optional, defaults to all accounts).
  - **Note:** This tool also serves the purpose of checking historical account balances at any given date "X".
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

## [x] Phase 2: Investments & Analytics (Completed)
- [x] **Hardened JSON Parser & Builders**
- [x] **Portfolio Tools**
    - [x] `get_investments`: Lists all security holdings.
    - [x] `get_currencies`: Lists exchange rates to base currency.
- [x] **Performance Tools**
    - [x] `get_security_prices`: Historical price retrieval.
    - [x] `get_security_performance`: ROI analysis data (prices + transactions).

## Potential Future Enhancements
> **Security Note:** Write operations must be strictly opt-in via plugin settings, as the agent could mutate financial records. It is currently undecided if write operations will ever be implemented.

These tools allow the agent to actively help manage or plan finances.

- [ ] `get_reminders`
  - **Description:** Lists upcoming scheduled transactions (reminders).
  - **Parameters:** `days_ahead` (e.g., next 30 days).
  - **Data:** Next date, payee, amount, and frequency.
- [ ] `get_budgets`
  - **Description:** Retrieves the current budget limits and actual spending against them.
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
