# Agents - Coding Rules & Guidelines

Rules and conventions for AI-assisted coding in this project.

## Architecture & Code Structure

Follow [domain-model.md](domain-model.md) for all Java backend code:

- **Layering:** Service (public API) -> Component (private logic) -> Repository (persistence)
- **Transactions:** Service starts (`@Transactional(timeout=10_000)`), Component participates (`MANDATORY`)
- **Packages:** Service at root, components in `component/`, entities in `model/`, REST in `api/`
- **Naming:** `*Service`, `*Component`, `*Repository`, `*Entity`, `*Resource`, `*Timer`
- **Components:** Single `call()` method, extracted when >10 lines / reused / testable
- **Conversion:** Service returns Entity, Resource converts to API model
- **Testing:** One test class per production class, `subject` variable, GIVEN/WHEN/THEN pattern, random data
