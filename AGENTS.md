# Project Development Guidelines

- Follow SOLID principles.
- Separate business logic, input/output, and storage concerns.
- Do not place all application logic in `Program` or `Main`.
- Use small classes with a single responsibility.
- Pass dependencies through abstractions where justified.
- Avoid code duplication.
- Do not overcomplicate the architecture without need.
- Keep the code clear and appropriate for an educational laboratory project.
- Add new functionality gradually without breaking working behavior.
- Analyze the existing project structure before making changes.
- Do not remove working code without necessity.
- Keep the design flexible enough to replace Excel storage with another storage implementation later.
- Keep ticket-number generation separate from Excel access and console interaction.
