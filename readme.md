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
