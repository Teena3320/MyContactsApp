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
