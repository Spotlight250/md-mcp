---
trigger: always_on
description: Strict rules enforcing Test-Driven Development (TDD) before implementing any features.
---
# Test-Driven Development (TDD) Mandate

- **Test First:** You MUST write failing unit tests before writing any production code.
- **Isolate Logic:** Because testing code inside the Moneydance JVM is difficult, extract business logic, JSON parsing, and MCP protocol formatting into plain Java classes that can be tested independently of the `FeatureModule` context.
- **Prove Failure:** Run the tests to prove they fail (Red), then write the minimal code to pass (Green), then refactor.
