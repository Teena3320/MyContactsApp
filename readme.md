# Use Case 1: User Registration
 
  This console class handles:
  - Reading user input (email, name, password)
  - Early validation for blank/invalid email
  - Triggering the registration process via UserService
 
 Demonstrates:
 - Basic input handling
 - Exception handling
 - Simple OOP layering (UI → Service → Repository)
   
# Use Case 2: User Authentication (Login)
 
  This console class handles:
  - Reading user input (email, password)
  - Early validation for blank/invalid email (abort attempt)
  - Verifying credentials via UserService (hash & compare)
 
  Demonstrates:
  - Basic input handling
  - Exception handling (InvalidCredentialsException)
  - Simple OOP layering (UI → Service → Repository)

# Use Case 3: User Profile Management
 
  This console class enables:
  - Updating profile name
  - Changing password (with current-password verification)
 
  Demonstrates:
  - Encapsulation (User mutators with validation)
  - Exception handling (Validation & IncorrectPassword)
  - Simple in-memory state (current logged-in user)

 # Use Case 4: Create Contact

 This module enables:
 - Creating Person or Organization contacts
 - Adding multiple phone numbers and emails with validation

 Demonstrates:
 - Inheritance (Contact → Person/Organization)
 - Composition (Contact has PhoneNumber, Email)
 - Exception handling (Validation, Duplicate contact)


# Use Case 5: View Contact Details

This module enables:
 - Listing all contacts for the logged‑in user
 - Selecting a specific contact to view full details
 - Displaying formatted information (type, name, phones, emails, timestamps)
 - Optional enhancements:
 - Uppercase contact name
 -  Masked email addresses
  
Demonstrates:
 - Read‑only view rendering
 - Clean separation of display logic using ContactRenderer
 - Optional formatting flags (uppercase, mask emails)
 - Safe access to stored contact data
 - Polymorphic behavior (PersonContact / OrganizationContact share display logic)

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

#  Use Case 7: Delete Contact
  
  Supports two deletion modes:
  - Soft Delete
  - Marks contact as “deleted”
  Hidden from:
  - View names
  - View details
  
  Data preserved internally (recoverable if needed)
  Hard Delete
  Permanently removes the contact from the repository
  
  UI flow:
  - Choose a contact
  Choose delete type:
  1) Soft Delete
  2) Hard Delete
  3) Cancel
  
  Demonstrates:
  - Entity lifecycle management
  - Soft vs hard deletion
  - Repository update logic
  - Exception handling for invalid deletions

# Use Case 8: Contact Groups

 This module enables:
 - Creating groups for the logged‑in user
 - Adding or removing contacts as group members
 - Renaming existing groups
 - Viewing group lists (names only)
 - Viewing detailed group info (member names)
 - Bulk deleting all group members (soft or hard delete)
 
 Input behavior:
 - Group names must be non blank
 - Uses validated numeric selection for members
 - Rejects whitespace-only inputs
 - Only non deleted contacts can be added as members
 
 Safe update flow:
 - Prevents duplicate group names per user
 - Keeps membership consistent if contacts are deleted
 - Bulk operations use ContactService for soft/hard delete
 - Group stores only contact IDs to avoid stale references
 
 Demonstrates:
 - Encapsulation in ContactGroup
 - Exception handling (ValidationException, DuplicateGroupException)
 - Clean OOP workflow for create/rename/add/remove/bulk actions
 - Consistent validation rules matching previous use cases

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

# Use Case 10: Basic Filtering
 
 This module enables:
  - Filtering contacts by tag, date added, or frequency of contact
  - Displaying a refined list of contacts based on chosen criteria
  - Supporting sorted results (alphabetical, by creation date) for easier navigation
  
  Input behavior:
  - User selects a filter type from a menu
  - Rejects invalid filter choices
  - Date filters must use non‑blank input (if applicable)
  - Filters apply only to non‑deleted contacts
  
  Safe filtering flow:
  - Ensures filters are applied consistently and do not modify contact data
  - Uses comparison logic safely (dates, strings, tags)
  - Sorting performed using Java’s built‑in comparators
  - Prevents errors by ignoring contacts without required metadata (e.g., missing tags)
  
  Demonstrates:
  - Use of simple filter interfaces or dedicated filter methods
  - Clean separation between filtering logic and UI display
  - Encapsulation of comparison logic (alphabetical, date-based)
  - Java collection utilities:
  	Comparator
  	Collections.sort()
   Loop-based conditional filtering
   
   Consistent user experience aligned with previous use cases      

   # Use Case 11: Create and Manage Tags
   
  This module enables:
  - Creating custom tags (e.g., Family, Work, Friends)
  - Ensuring each tag name is unique for the logged in user
  - Renaming existing tags
  - Viewing all available tags
  - Deleting tags safely without breaking contact data
  - Keeping tag lists consistent across contacts
  
  Input behavior:
  - Tag names must be non blank
  - Rejects whitespace only names
  - Renaming a tag requires a unique new name
  - Deleting a tag removes it from contacts that use it
  
  Safe update flow:
  - Prevents duplicate tag names per user
  - Ensures tag removal from contacts is clean and consistent
  - Snapshot of tag list updated atomically after rename/delete
  - Stores tags in a dedicated Tag structure for stability and identity
  
  Demonstrates:
  - Encapsulation in a Tag class (identity, equality, managed naming)
  - Exception handling (ValidationException, custom DuplicateTagException)
  - Clear OOP relationships between Tag and Contact (many to many association)
  - Clean workflow for create/rename/delete operations
  - Consistent input rules matching previous use cases (non blank, duplicate safe)

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
 
    
