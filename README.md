# 🎓 Smart Student Grade Tracker & Academic Curriculum Engine

An enterprise-ready, full-stack Academic Performance Tracking & Grade Management System developed for the **CodeAlpha Java Programming Internship**.

Features an automated **Multi-Curriculum Subject Engine** supporting School (State, CBSE, ICSE), Pre-University / PUC (Science PCMB/PCMC, Commerce, Arts), and Degree / Engineering (VTU & Autonomous), alongside **Automated VTU Marksheet PDF Extraction**, **Bulk ZIP Archive Processing**, and **Official Printable Student Scorecards**.

---

## 🌟 Key Features

### 1. 📚 Multi-Curriculum & Board Template Engine
- **Class 1 to 10th (Schooling):**
  - **Karnataka State Board (KSEEB / KSEAB):** 6 core subjects (First Language 125M, Second Language 100M, Third Language 100M, Mathematics 100M, Science 100M, Social Science 100M — Total 625 Marks).
  - **CBSE (Central Board):** 5 subjects (English, Hindi/Regional, Standard Math, Science, Social Science — Total 500 Marks).
  - **ICSE (Council for CISCE):** 6 subject groups (English, Second Language, HCG History/Civics/Geography, Science Physics/Chemistry/Biology, Mathematics, Computer Applications — Total 600 Marks).
- **PUC / Pre-University (11th & 12th):**
  - **Science (PCMB):** Physics, Chemistry, Mathematics, Biology, English, Kannada/Hindi (Total 600 Marks).
  - **Science (PCMC):** Physics, Chemistry, Mathematics, Computer Science, English, Kannada/Hindi (Total 600 Marks).
  - **Commerce:** Accountancy, Business Studies, Economics, Statistics/Computer Science, English, Second Language (Total 600 Marks).
  - **Arts:** History, Economics, Political Science, Sociology/Psychology, English, Second Language (Total 600 Marks).
- **Degree / Higher Education:**
  - **VTU Engineering:** Core engineering subjects, labs, mathematics, with 100-mark scales (40% minimum passing criteria per subject).
  - **Autonomous Colleges & Universities:** Flexible credit/mark templates.
  - **Custom Template:** Dynamic `+ Add Subject` row generator for any elective, course code, or exam board.
- **⚡ Quick Mode:** Single-click toggle for fast direct aggregate entry (0 - 100%) without entering individual subject breakdown.

### 2. 🖨️ Official Printable Student Marks Card
- Generates a formal, printable academic scorecard for each student.
- Itemized table with Course Name, Maximum Marks, Passing Marks, Marks Obtained, and Subject Status (`PASS` / `FAIL`).
- Board-specific failure handling: If a student falls below passing threshold in any core subject, the scorecard automatically flags the overall result as `FAILED`.
- Direct `🖨️ Print Card` integration with clean print-optimized CSS layout.

### 3. 📄 Automated PDF & Bulk ZIP Marksheet Ingestion
- **VTU 92-Student Result Sheet Parser:** Built-in PDF parser extracting all 92 students directly from institutional examination result sheets using Apache PDFBox regex parsing.
- **Individual Student Marksheet Support:** Automatically detects single student cards (e.g. USN `1SG21CS045`, Name, Total Marks, SGPA/Percentage).
- **Bulk ZIP Archive Ingestion:** Upload `.zip` files containing dozens of individual student result cards; the system unzips in-memory, parses every PDF simultaneously, and aggregates the batch into a single preview.
- **Multi-File Batch Upload:** Select or drag & drop multiple PDF, Excel, and CSV files together in a single operation.
- **Pre-Commit Interactive Preview:** Review all parsed records, inspect validation errors, preview duplicates, and confirm import with a single click.

### 4. 📊 Analytics, Reports & Data Export
- Real-time statistics: Total students, class average, pass percentage, top scorer(s), and grade distribution chart.
- Multi-dimensional filters: Search by name or USN/ID, filter by Grade (A+, A, B, C, D, F) or Status (PASSED, FAILED).
- Export in multiple industry formats: **PDF Report**, **Excel (.xlsx)**, and **CSV**.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 25 / 17+, Spring Boot 3.2.4, Spring Data JPA, Hibernate, Bean Validation |
| **Database** | In-Memory H2 Database (with H2 Web Console), JPA Entities |
| **PDF & Office Processing** | Apache PDFBox 2.0.30, Apache POI 5.2.5 (ooxml), iText PDF |
| **Frontend** | HTML5, Vanilla CSS3 (Custom Glassmorphism Design System, Micro-animations), Modern JavaScript (ES6+) |
| **CLI Fallback** | Java Console Application (`Main.java`, `StudentManager.java`) |

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher (JDK 25 supported)
- Apache Maven 3.8+

### 1. Clone the Repository
```bash
git clone https://github.com/shreyahurakadli2912-oss/StudentGradeTracker.git
cd StudentGradeTracker
```

### 2. Run the Full-Stack Web Application
```bash
mvn spring-boot:run
```
Once started, open your browser and navigate to:
```text
http://localhost:8080
```

H2 Database Console is available at:
```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:gradetracker
Username: SA
Password: (blank)
```

### 3. Run the Classic Console CLI
```bash
javac src/*.java
java src.Main
```

### 4. Run the Test Suites
```bash
# Automated JUnit / Spring Boot tests:
mvn test

# Classic CLI test suite:
java src.GradeTrackerTest
```

---

## 📡 REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/students` | Retrieve students (supports `search`, `grade`, `status`, `sortBy`, `sortOrder`) |
| `GET` | `/api/students/{studentId}` | Retrieve a specific student by Student ID / USN |
| `POST` | `/api/students` | Create student record (supports JSON `subjectBreakdown`) |
| `PUT` | `/api/students/{studentId}` | Update student details and subject breakdown |
| `DELETE` | `/api/students/{studentId}` | Delete a student by ID |
| `DELETE` | `/api/students/all` | Clear all student records |
| `GET` | `/api/students/stats` | Class performance analytics and metrics |
| `POST` | `/api/students/import/preview` | Multipart file upload preview (single PDF, multi-file, or ZIP archive) |
| `POST` | `/api/students/import/confirm` | Confirm batch import to database |
| `GET` | `/api/students/export/pdf` | Download PDF grade report |
| `GET` | `/api/students/export/excel` | Download Excel (.xlsx) report |
| `GET` | `/api/students/export/csv` | Download CSV grade export |

---

## 📁 Repository Structure

```
StudentGradeTracker/
├── src/
│   ├── main/java/com/codealpha/gradetracker/
│   │   ├── StudentGradeTrackerApplication.java  # Spring Boot entry point
│   │   ├── controller/                         # REST API Controllers
│   │   ├── dto/                                # Request & Response DTOs
│   │   ├── model/                              # JPA Student Entities
│   │   ├── repository/                         # Spring Data JPA Repositories
│   │   └── service/                            # Business Logic & PDF/ZIP Parsers
│   └── (classic CLI classes: Main.java, Student.java, StudentManager.java, InputValidator.java)
├── web/                                        # Modern Glassmorphic Web App
│   ├── index.html                              # Web Dashboard & Modals
│   ├── styles.css                              # Design System, Themes & Print Styles
│   └── app.js                                  # Interactive State & Real-Time Calc Engine
├── pom.xml                                     # Maven Dependencies & Build Configuration
└── README.md                                   # Comprehensive Documentation
```

---

## ⚖️ License
This project is open-source and developed under the **CodeAlpha Java Programming Internship**.
