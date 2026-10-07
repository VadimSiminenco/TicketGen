# TicketGen development rules

- Lab 2 is developed on `lab2-ui-tickets`; keep Lab 1 on `main`.
- Use Java 17, Maven, Spring Boot, Apache POI and Selenium.
- Keep HTTP/UI in controllers, assignment rules in services, file parsing and persistence in repositories, and data in domain models. Follow SOLID without unnecessary layers.
- Data files: `students.xlsx`, `tickets.docx`, `results.xlsx`; the data directory is configurable.
- Never use real user Excel/Word files in tests. Generate temporary fixtures.
- Run `mvn clean test` and `mvn clean verify` before committing.
- Append one result row per successful generation; never alter earlier rows. A repeat uses the first historical ticket for group + trimmed last name + trimmed first name.
- A locked `results.xlsx` must show a retry/return dialog and no success result. Retry must add exactly one row.
- ESC closes the ticket result dialog only; it does not terminate the server or browser.
