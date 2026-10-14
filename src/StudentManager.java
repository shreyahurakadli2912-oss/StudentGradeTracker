import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of students in memory using an ArrayList.
 * Handles adding, searching, updating, deleting, and statistics calculation.
 */
public class StudentManager {
    private final List<Student> students;

    /**
     * Initializes an empty StudentManager.
     */
    public StudentManager() {
        this.students = new ArrayList<>();
    }

    /**
     * Checks if a student ID already exists in the system (case-insensitive).
     *
     * @param id Student ID to check
     * @return true if ID exists, false otherwise
     */
    public boolean isIdExists(String id) {
        if (id == null) return false;
        for (Student student : students) {
            if (student.getId().equalsIgnoreCase(id.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new student to the system.
     *
     * @param student Student object to add
     * @return true if added successfully, false if ID already exists
     */
    public boolean addStudent(Student student) {
        if (student == null || isIdExists(student.getId())) {
            return false;
        }
        students.add(student);
        return true;
    }

    /**
     * Returns a copy of the list of all students.
     *
     * @return List of all registered students
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    /**
     * Searches for a student by ID (case-insensitive).
     *
     * @param id Student ID to search for
     * @return Student object if found, null otherwise
     */
    public Student searchStudentById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        for (Student student : students) {
            if (student.getId().equalsIgnoreCase(id.trim())) {
                return student;
            }
        }
        return null;
    }

    /**
     * Updates an existing student's details.
     *
     * @param id       Student ID to locate
     * @param newName  New name for the student
     * @param newMarks New marks for the student
     * @return true if updated successfully, false if student not found
     */
    public boolean updateStudent(String id, String newName, double newMarks) {
        Student student = searchStudentById(id);
        if (student == null) {
            return false;
        }
        student.setName(newName.trim());
        student.setMarks(newMarks);
        return true;
    }

    /**
     * Deletes a student by ID.
     *
     * @param id Student ID to delete
     * @return true if deleted successfully, false if student not found
     */
    public boolean deleteStudent(String id) {
        Student student = searchStudentById(id);
        if (student == null) {
            return false;
        }
        return students.remove(student);
    }

    /**
     * Returns total number of students.
     *
     * @return Student count
     */
    public int getStudentCount() {
        return students.size();
    }

    /**
     * Displays all registered students in a formatted table.
     */
    public void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }

        printTableSeparator();
        System.out.println("| Student ID   | Student Name           | Marks    | Letter Grade |");
        printTableSeparator();
        for (Student student : students) {
            System.out.println(student);
        }
        printTableSeparator();
    }

    /**
     * Displays a dedicated student grade report.
     */
    public void displayGradeReport() {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }

        System.out.println("\n=======================================================");
        System.out.println("              STUDENT GRADE REPORT                      ");
        System.out.println("=======================================================");
        printTableSeparator();
        System.out.println("| Student ID   | Student Name           | Marks    | Letter Grade |");
        printTableSeparator();
        for (Student student : students) {
            System.out.println(student);
        }
        printTableSeparator();
    }

    /**
     * Calculates and displays class performance statistics:
     * - Total students
     * - Total marks
     * - Average marks (formatted to 2 decimal places)
     * - Highest marks & top scorer(s)
     * - Lowest marks & lowest scorer(s)
     */
    public void displayClassStatistics() {
        if (students.isEmpty()) {
            System.out.println("No students found. Cannot calculate statistics.");
            return;
        }

        double totalMarks = 0;
        double highestMarks = Double.NEGATIVE_INFINITY;
        double lowestMarks = Double.POSITIVE_INFINITY;

        for (Student s : students) {
            double marks = s.getMarks();
            totalMarks += marks;
            if (marks > highestMarks) {
                highestMarks = marks;
            }
            if (marks < lowestMarks) {
                lowestMarks = marks;
            }
        }

        double averageMarks = totalMarks / students.size();

        // Identify all students sharing highest marks
        List<String> highestScorers = new ArrayList<>();
        // Identify all students sharing lowest marks
        List<String> lowestScorers = new ArrayList<>();

        for (Student s : students) {
            if (Double.compare(s.getMarks(), highestMarks) == 0) {
                highestScorers.add(s.getName() + " (ID: " + s.getId() + ")");
            }
            if (Double.compare(s.getMarks(), lowestMarks) == 0) {
                lowestScorers.add(s.getName() + " (ID: " + s.getId() + ")");
            }
        }

        int passedCount = 0;
        int failedCount = 0;
        int countA = 0, countB = 0, countC = 0, countD = 0, countF = 0;

        for (Student s : students) {
            if (s.getMarks() >= 60.0) {
                passedCount++;
            } else {
                failedCount++;
            }
            switch (s.getGrade()) {
                case "A": countA++; break;
                case "B": countB++; break;
                case "C": countC++; break;
                case "D": countD++; break;
                default: countF++; break;
            }
        }

        System.out.println("\n=======================================================");
        System.out.println("               CLASS STATISTICS SUMMARY                 ");
        System.out.println("=======================================================");
        System.out.printf("Total Number of Students : %d%n", students.size());
        System.out.printf("Total Marks Accumulated  : %.2f%n", totalMarks);
        System.out.printf("Average Marks            : %.2f%n", averageMarks);
        System.out.println("-------------------------------------------------------");
        System.out.printf("Highest Marks            : %.2f%n", highestMarks);
        System.out.println("Highest Achiever(s)      : " + String.join(", ", highestScorers));
        System.out.println("-------------------------------------------------------");
        System.out.printf("Lowest Marks             : %.2f%n", lowestMarks);
        System.out.println("Lowest Achiever(s)       : " + String.join(", ", lowestScorers));
        System.out.println("-------------------------------------------------------");
        System.out.printf("Passing Students Count   : %d (%.1f%%)%n", passedCount, (passedCount * 100.0 / students.size()));
        System.out.printf("Failing Students Count   : %d (%.1f%%)%n", failedCount, (failedCount * 100.0 / students.size()));
        System.out.printf("Grade Distribution       : A: %d | B: %d | C: %d | D: %d | F: %d%n", countA, countB, countC, countD, countF);
        System.out.println("=======================================================\n");
    }

    /**
     * Pre-populates the manager with sample students for quick testing.
     */
    public void loadSampleData() {
        addStudent(new Student("S101", "Alice Johnson", 94.5));
        addStudent(new Student("S102", "Bob Smith", 82.0));
        addStudent(new Student("S103", "Charlie Brown", 74.5));
        addStudent(new Student("S104", "David Miller", 65.0));
        addStudent(new Student("S105", "Eva Green", 52.0));
    }

    private void printTableSeparator() {
        System.out.println("+--------------+------------------------+----------+--------------+");
    }
}
