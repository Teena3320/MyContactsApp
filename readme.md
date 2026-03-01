# Use Case 8: Contact Groups

 This module enables:
 - Creating groups for the logged in user
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
 - Clean OOP workflow for create/rename/add/remove/bulk operations
 - Consistent validation rules matching previous use cases
