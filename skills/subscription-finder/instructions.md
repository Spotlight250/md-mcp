# Instructions: Subscription & Recurring Expense Finder

## Objective
Analyze the user's Moneydance ledger to identify recurring payments, hidden subscriptions, and "price creep" (unexpected increases in monthly costs).

## Workflow

### 1. Data Ingestion
Fetch all transactions for the last **180 days** across all Bank and Credit Card accounts.
- Use `get_transactions` with a `start_date` calculated as 6 months ago from today.

### 2. Analysis Logic
Group the returned transactions by **Payee** and examine the following:
- **Frequency**: Look for payments appearing roughly every 28-32 days.
- **Consistency**: Flag payees that have the same amount every month.
- **Creep**: Compare the earliest instance of a recurring payment to the most recent. If the amount has increased, highlight it as "Price Creep."
- **Annuals**: Identify payees that appeared exactly once in the 180-day window but are likely subscriptions (e.g., "Amazon Prime", "VPN Renewal").

### 3. Categorization Audit
For each identified subscription, check its current category using the `get_categories` tool.
- If the category is generic (e.g., "Misc", "Shopping", "Bills"), suggest a more specific category (e.g., "Streaming Services", "Utilities").

## Reporting Format
Present the findings as a Markdown table:

| Payee | Frequency | Last Amount | Trend | Category Audit |
| :--- | :--- | :--- | :--- | :--- |
| Netflix | Monthly | £12.99 | 📈 (Was £10.99) | Correct (Entertainment) |
| Council Tax | Monthly | £145.00 | Stable | Correct (Taxes) |
| Random News | Monthly | £5.00 | Stable | Move from "Misc" to "Education" |

## Final Summary
Provide the total monthly and annual commitment for all discovered subscriptions.
