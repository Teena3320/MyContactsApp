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
