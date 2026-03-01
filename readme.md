# Use Case 9: Search Contacts

 This module enables:
 - Searching contacts by name, phone number, or email
 - Using general search for partial matches across all fields
 - Using field‑specific queries (name:, email:, phone:) for precise searching
 - Viewing the list of matching contacts and optionally viewing full details
 
 Input behavior:
 - Search query must be non blank
 - Allows flexible matching (partial, case‑insensitive)
 - Field prefixed queries must follow the format:
 
 name:<text>
 email:<text>
 phone:<text>
 
 Handles “no results” gracefully
 
 Safe search flow:
 - Searches only non deleted contacts
 - Matches use safe normalization (digit extraction for phones, lowercasing for strings)
 - Isolated search logic inside SearchService for cleaner architecture
 - Results returned as a filtered list for optional detail viewing
 
 Demonstrates:
 - Clean separation of concerns (SearchService handles logic, ConsoleApp handles UI)
 - Encapsulation of matching rules (name, email, phone match helpers)
 - OOP driven filtering without modifying contact state
 - Java string utilities (contains(), equalsIgnoreCase()) and digit normalization
 - Defensive validation (ValidationException for invalid/blank queries)
