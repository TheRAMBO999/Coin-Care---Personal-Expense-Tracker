# Coin Care 
Project Development Steps: CoinCare Expense Tracker

### Step 1: Environment Setup
- JDK Installation: Installed Java 17 (or higher) to handle modern JavaFX and OCR libraries.

IDE Configuration: Set up IntelliJ IDEA with the Maven build system to manage project dependencies.

OCR Engine: Installed Tesseract OCR on the system and configured the tessdata path so the app can "read" receipts.

### Step 2: Dependency Management (pom.xml)
Defined the project structure and added libraries for JavaFX (UI), SQLite (Database), and Tess4J (OCR wrapper).

Configured Maven plugins to compile the code and prepare it for packaging into an executable.

### Step 3: Database Architecture
Created a Database.java class using JDBC to connect to a local aura_tracker.db file.

Designed two main tables:

Users Table: Stores usernames, passwords, and security answers for authentication.

Expenses Table: Stores categories, amounts (like the 7.35 scan), descriptions, and dates.

### Step 4: User Interface Design (FXML & CSS)
Login/Register Screen (auth.fxml): Designed a secure entry point with a dark-themed aesthetic.

Main Dashboard (expense_tracker.fxml): Created a layout using StackPane and VBox to include a navigation bar, history table, and floating action buttons.

Styling: Applied a custom styles.css to handle the "GitHub Dark" look and button hover effects.

### Step 5: Core Feature Implementation
Authentication Logic: Wrote the AuthController to verify users and allow for password recovery using a security question.

OCR Scanning: Implemented the "Scan Receipt" feature which uses Tesseract to automatically extract the total price from images.

Expense Management: Added functionality to Save, Edit, and Delete transactions, with a real-time History table.

### Step 6: Data Export and Portability
CSV Export: Built methods to export both the expense history and the user list into .csv files for external viewing in Excel.

Scene Switching: Configured the app to transition smoothly from Login to Dashboard and back (Logout).

### Step 7: Packaging and Deployment
Used the jpackage tool to bundle the Java code and the runtime into a standalone Windows .exe installer.
