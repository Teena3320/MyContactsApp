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
    
