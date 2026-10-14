import java.util.Scanner;

/**
 * Main application class for the Student Grade Tracker system.
 * Handles menu navigation and user interaction loop.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StudentManager manager = new StudentManager();

        System.out.println("=======================================================");
        System.out.println("     WELCOME TO STUDENT GRADE TRACKER SYSTEM           ");
        System.out.println("=======================================================");

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = InputValidator.readMenuChoice(scanner, "Enter your choice (1-8): ", 1, 8);

            switch (choice) {
                case 1:
                    handleAddStudent(scanner, manager);
                    break;
                case 2:
                    handleViewAllStudents(manager);
                    break;
                case 3:
                    handleSearchStudent(scanner, manager);
                    break;
                case 4:
                    handleUpdateStudent(scanner, manager);
                    break;
                case 5:
                    handleDeleteStudent(scanner, manager);
                    break;
                case 6:
                    handleCalculateStatistics(manager);
                    break;
                case 7:
                    handleViewGradeReport(manager);
                    break;
                case 8:
                    running = false;
                    System.out.println("\nThank you for using Student Grade Tracker. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    /**
     * Displays the main menu options to the user.
     */
    private static void displayMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Student by ID");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Calculate Class Statistics");
        System.out.println("7. View Student Grade Report");
        System.out.println("8. Exit");
        System.out.println("-------------------------------------------");
    }

    /**
     * Option 1: Add a new student record.
     */
    private static void handleAddStudent(Scanner scanner, StudentManager manager) {
        System.out.println("\n--- ADD NEW STUDENT ---");
        String id;
        while (true) {
            id = InputValidator.readStudentId(scanner, "Enter Student ID: ");
            if (manager.isIdExists(id)) {
                System.out.printf("Error: A student with ID '%s' already exists. Please use a unique ID.%n", id);
            } else {
                break;
            }
        }

        String name = InputValidator.readNonEmptyString(scanner, "Enter Student Name: ");
        double marks = InputValidator.readValidMarks(scanner, "Enter Marks (0 - 100): ");

        Student student = new Student(id, name, marks);
        if (manager.addStudent(student)) {
            System.out.printf("Success: Student '%s' (ID: %s) added successfully with Grade '%s'.%n",
                    name, id, student.getGrade());
        } else {
            System.out.println("Error: Failed to add student.");
        }
    }

    /**
     * Option 2: View all registered students.
     */
    private static void handleViewAllStudents(StudentManager manager) {
        System.out.println("\n--- REGISTERED STUDENTS LIST ---");
        manager.displayAllStudents();
    }

    /**
     * Option 3: Search for a student by ID.
     */
    private static void handleSearchStudent(Scanner scanner, StudentManager manager) {
        System.out.println("\n--- SEARCH STUDENT BY ID ---");
        String id = InputValidator.readStudentId(scanner, "Enter Student ID to search: ");
        Student student = manager.searchStudentById(id);

        if (student != null) {
            System.out.println("\nStudent Found:");
            System.out.println("+--------------+------------------------+----------+--------------+");
            System.out.println("| Student ID   | Student Name           | Marks    | Letter Grade |");
            System.out.println("+--------------+------------------------+----------+--------------+");
            System.out.println(student);
            System.out.println("+--------------+------------------------+----------+--------------+");
        } else {
            System.out.printf("Student with ID '%s' not found.%n", id);
        }
    }

    /**
     * Option 4: Update an existing student's record.
     */
    private static void handleUpdateStudent(Scanner scanner, StudentManager manager) {
        System.out.println("\n--- UPDATE STUDENT RECORD ---");
        String id = InputValidator.readStudentId(scanner, "Enter Student ID to update: ");
        Student student = manager.searchStudentById(id);

        if (student == null) {
            System.out.printf("Student with ID '%s' not found.%n", id);
            return;
        }

        System.out.printf("Current Record -> Name: %s, Marks: %.2f, Grade: %s%n",
                student.getName(), student.getMarks(), student.getGrade());

        String newName = InputValidator.readNonEmptyString(scanner, "Enter New Name: ");
        double newMarks = InputValidator.readValidMarks(scanner, "Enter New Marks (0 - 100): ");

        if (manager.updateStudent(id, newName, newMarks)) {
            System.out.printf("Success: Student record for ID '%s' updated successfully! New Grade: '%s'.%n",
                    id, student.getGrade());
        } else {
            System.out.println("Error: Failed to update student record.");
        }
    }

    /**
     * Option 5: Delete a student record with confirmation.
     */
    private static void handleDeleteStudent(Scanner scanner, StudentManager manager) {
        System.out.println("\n--- DELETE STUDENT RECORD ---");
        String id = InputValidator.readStudentId(scanner, "Enter Student ID to delete: ");
        Student student = manager.searchStudentById(id);

        if (student == null) {
            System.out.printf("Student with ID '%s' not found.%n", id);
            return;
        }

        System.out.printf("Student Found -> ID: %s | Name: %s | Marks: %.2f | Grade: %s%n",
                student.getId(), student.getName(), student.getMarks(), student.getGrade());

        boolean confirm = InputValidator.readConfirmation(scanner,
                "Are you sure you want to delete this student?");

        if (confirm) {
            if (manager.deleteStudent(id)) {
                System.out.printf("Success: Student record for ID '%s' deleted successfully.%n", id);
            } else {
                System.out.println("Error: Could not delete student.");
            }
        } else {
            System.out.println("Deletion cancelled. No changes were made.");
        }
    }

    /**
     * Option 6: Calculate class statistics.
     */
    private static void handleCalculateStatistics(StudentManager manager) {
        manager.displayClassStatistics();
    }

    /**
     * Option 7: View Student Grade Report.
     */
    private static void handleViewGradeReport(StudentManager manager) {
        manager.displayGradeReport();
    }
}
