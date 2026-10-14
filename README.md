# Student Grade Tracker

A beginner-friendly, menu-driven Java console application built for managing student records, calculating grade letters, generating student reports, and computing class-wide statistics.

Developed for the **CodeAlpha Java Programming Internship**.

---

## 📌 Project Overview

The **Student Grade Tracker** allows educators and instructors to easily manage student information. The system stores student details in memory using Java's `ArrayList` data structure and provides features to add, search, update, delete, display records, calculate letter grades, and view comprehensive class performance statistics.

---

## ✨ Features

1. **Interactive Main Menu:** Easy-to-use menu that continues running until explicitly exited.
2. **Add Student Record:** Accepts unique Student ID, Student Name, and Marks (0 - 100). Includes validation against empty values, duplicate IDs, and invalid numerical inputs.
3. **View All Students:** Displays registered student records in a clean tabular format showing ID, Name, Marks, and Letter Grade.
4. **Search Student by ID:** Case-insensitive search returning full details for a student. Handles non-existent or invalid queries gracefully.
5. **Update Student Details:** Enables updating name and marks while preserving the original Student ID. Recalculates letter grades automatically.
6. **Delete Student Record:** Removes student records with a confirmation prompt (Y/N) to prevent accidental deletions.
7. **Calculate Class Statistics:**
   - Total number of registered students
   - Total marks sum
   - Class average marks (formatted to 2 decimal places)
   - Highest score & top performer(s), with support for ties
   - Lowest score & lowest performer(s), with support for ties
   - Empty list protection to prevent division-by-zero errors
8. **Student Grade Report:** Dedicated report listing all students alongside their letter grades.
9. **Robust Input Validation & Error Handling:** Prevents crashes caused by non-numeric input, empty inputs, negative marks, marks over 100, or invalid menu choices. Uses `Scanner.nextLine()` with safe parsing to prevent buffer issues.

---

## 🛠️ Grade Scale Reference

| Marks Range | Letter Grade |
| ----------- | ------------ |
| 90 – 100    | **A**        |
| 80 – < 90   | **B**        |
| 70 – < 80   | **C**        |
| 60 – < 70   | **D**        |
| 0 – < 60    | **F**        |

---

## 💻 Technologies Used

- **Programming Language:** Java (JDK 8+ / JDK 25 recommended)
- **Data Structures:** `java.util.ArrayList`, `java.util.List`
- **Standard Libraries:** `java.util.Scanner`
- **Architecture:** Object-Oriented Design (Model, Manager, Utility, Main)

---

## 📋 System Requirements

- **Operating System:** Windows 10/11 (or Linux/macOS)
- **Java Development Kit:** JDK installed (`javac` and `java` commands accessible in PATH).

---

## 🚀 How to Compile and Run (Windows)

Open Command Prompt (`cmd`) or PowerShell in the root directory of the project (`StudentGradeTracker`):

### 1. Compile the Project
```cmd
javac src/*.java
```

### 2. Run the Main Application
```cmd
java src.Main
```

### 3. Run the Automated Test Suite
```cmd
java src.GradeTrackerTest
```

---

## 🖥️ Example Interaction & Sample Output

```text
=======================================================
     WELCOME TO STUDENT GRADE TRACKER SYSTEM           
=======================================================

---------------- MAIN MENU ----------------
1. Add Student
2. View All Students
3. Search Student by ID
4. Update Student
5. Delete Student
6. Calculate Class Statistics
7. View Student Grade Report
8. Exit
-------------------------------------------
Enter your choice (1-8): 1

--- ADD NEW STUDENT ---
Enter Student ID: S101
Enter Student Name: Alice Johnson
Enter Marks (0 - 100): 94.5
Success: Student 'Alice Johnson' (ID: S101) added successfully with Grade 'A'.

---------------- MAIN MENU ----------------
1. Add Student
2. View All Students
...
Enter your choice (1-8): 2

--- REGISTERED STUDENTS LIST ---
+--------------+------------------------+----------+--------------+
| Student ID   | Student Name           | Marks    | Letter Grade |
+--------------+------------------------+----------+--------------+
| S101         | Alice Johnson          | 94.50    | A            |
+--------------+------------------------+----------+--------------+

---------------- MAIN MENU ----------------
1. Add Student
2. View All Students
...
Enter your choice (1-8): 6

=======================================================
               CLASS STATISTICS SUMMARY                 
=======================================================
Total Number of Students : 1
Total Marks Accumulated  : 94.50
Average Marks            : 94.50
-------------------------------------------------------
Highest Marks            : 94.50
Highest Achiever(s)      : Alice Johnson (ID: S101)
-------------------------------------------------------
Lowest Marks             : 94.50
Lowest Achiever(s)       : Alice Johnson (ID: S101)
=======================================================
```

---

## ⚠️ Known Limitations

- **In-Memory Data Storage:** Records are stored in memory using an `ArrayList`. Closing the application resets the data. Persistent storage (file/database) is omitted as per project constraints.
- **Console Interface:** Interactive user interface is text-based command-line only.

---

## 📁 Project Structure

```
StudentGradeTracker/
├── src/
│   ├── Main.java               # Main entry point and menu control loop
│   ├── Student.java            # Student model class with grade logic
│   ├── StudentManager.java     # Operations: add, search, update, delete, stats
│   ├── InputValidator.java     # Safe input reading and validation utilities
│   └── GradeTrackerTest.java   # Automated unit and integration test suite
└── README.md                   # Comprehensive project documentation
```
