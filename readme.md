# Use Case 6:  Edit Contact

 This module enables:
	- Selecting an existing Person or Organization contact to edit
	Updating core fields:
	- Name (full name or organization name)
	- Phone numbers (add, replace, remove with immediate validation)
	- Emails (add, replace, remove with immediate validation)

	Input behavior aligned with creation:
	- Per entry validation for phone and email
	- Reject whitespace-only inputs
	- After each valid phone/email, prompt: “Add another …? (Y/n)”

	Safe update flow:
	- Only applies changes if inputs pass validation
	- Prevents duplicate contact names per user
	- Keeps timestamps consistent (updates updatedAt)

	Demonstrates:
	- Encapsulation & validation in domain (setter methods + value objects)
	- Exception handling (ValidationException, DuplicateContactException)
	- Plain OOP editing workflow (no Command/Memento; straightforward update logic)
	- Defensive updates: work with in-memory entities, fail fast on invalid input
	- Consistency with UC‑04 input rules (same validators for Email, PhoneNumber)
