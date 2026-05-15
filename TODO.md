# Moneydance MCP Server Roadmap & Capabilities

This document tracks the evolution of the Moneydance MCP integration.

## [x] Phase 1: Core Read-Only Access

These tools allow the agent to answer basic financial questions.

- [x] `get_net_worth`: Total assets, liabilities, and net worth.
- [x] `get_accounts`: Lists bank, credit card, investment, and loan accounts.
- [x] `get_categories`: Lists income and expense categories.
- [x] `get_transactions`: Multi-criteria search across the ledger.

## [x] Phase 2: Investments & Analytics

- [x] **Hardened Infrastructure**: Custom recursive descent `JsonParser` and structured builders.
- [x] **Portfolio Tools**:
  - [x] `get_investments`: Security holdings and current values.
  - [x] `get_currencies`: FX rate discovery.
- [x] **Performance Tools**:
  - [x] `get_security_prices`: Historical price snapshots.
  - [x] `get_security_performance`: ROI analysis and transaction history.

## [x] Phase 2.5: Contextual Resources

- [x] **Resource Primitives**:
  - [x] `mcp://moneydance/accounts/hierarchy`: Full account tree (JSON).
  - [x] `mcp://moneydance/categories/full`: Comprehensive category map (JSON).
  - [x] `mcp://moneydance/securities/master`: Security metadata master list (JSON).

## [ ] Future Roadmap
>
> **Security Note:** Write operations require explicit user approval and are architected as "Human-in-the-loop."

- [ ] **Security Price Updates**: Tool to push historical/current prices for any security.
- [ ] **Transaction Tagging**: AI-driven categorization for "Uncategorized" items.
- [ ] **Memo Cleanup**: Automate the renaming of messy bank descriptions.
- [ ] **Bill Reminders**: `get_reminders` for upcoming payment tracking.
- [ ] **Budget Analysis**: `get_budgets` to compare actual spending against limits.
- [ ] **Prompts Library**: Pre-built AI prompts for "Portfolio Audits" and "Tax Prep."
- [/] **AI Skills Library**: [Subscription Finder](skills/subscription-finder/) (Standard Format Complete).
- [ ] **Safety Layer**: Implement `resources://pending-changes` to review AI edits before commitment.
- [ ] **Batch Processing**: Support for multi-transaction categorization in a single approval step.

---

## Technical Foundation

- **Zero-Dependency Java**: Runs in any Moneydance environment (Java 17+).
- **Formatter-Isolate Pattern**: Business logic is separated from formatting for 100% unit testability.
- **Hardened JSON Stack**: Custom recursive-descent parser for classloader safety.
- **JSON-RPC 2.0**: Strict adherence to the MCP specification.
