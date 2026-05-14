---
trigger: always_on
description: Security constraints regarding localhost binding, read-only defaults, and protecting sensitive financial data.
---
# Security & Safety

- **Read-Only Default:** Assume all operations are read-only. Never implement a write/mutate operation on the financial data without explicit user permission and extensive safeguards.
- **Localhost Only:** Never bind the server to `0.0.0.0`. It must always be restricted to the loopback interface (`127.0.0.1`).
- **No Secrets in Output:** Never include raw passwords, sync keys, or encryption material in MCP responses or logs.
