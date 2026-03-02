# Use Case 12: Apply Tags to Contacts
  
  This module enables:
  - Assigning one or multiple tags to a contact
  - Removing tags from any contact
  - Viewing which tags a contact currently has
  - Keeping tag selections consistent even when tags or contacts change
  
  Input behavior:
  - User selects a tag from their tag list
  - User selects a contact from their contact list
  - Rejects whitespace-only input for tag selection
  - Rejects invalid tag/contact indices
  - Ensures tags belong to the same owner as the contact
  
  Safe update flow:
  - Prevents adding a tag that is already applied
  - Cleanly removes tag references using the contact’s internal tag set
  - Enforces many‑to‑one and many‑to‑many relationships safely
  - Updates are applied only to non‑deleted contacts
  - When a tag is deleted (UC 11), it is removed from all associated contacts automatically
  
  Demonstrates:
  - Encapsulation of tag management inside Contact and TagService
  - Exception handling (ValidationException) for invalid or mismatched tag assignments
  - Clean OOP relationship handling (Contact ↔ Tag)
  - Consistent validation rules matching previous use cases (numeric selection, non‑blank inputs, owner checks)
 
