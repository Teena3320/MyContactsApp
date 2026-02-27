# Use Case 2: User Authentication (Login)
 
  This console class handles:
  - Reading user input (email, password)
  - Early validation for blank/invalid email (abort attempt)
  - Verifying credentials via UserService (hash & compare)
 
  Demonstrates:
  - Basic input handling
  - Exception handling (InvalidCredentialsException)
  - Simple OOP layering (UI → Service → Repository)
