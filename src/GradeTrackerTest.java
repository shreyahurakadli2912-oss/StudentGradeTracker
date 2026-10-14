import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Automated test suite to verify all requirements of the Student Grade Tracker application.
 */
public class GradeTrackerTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println("     RUNNING AUTOMATED TEST SUITE FOR GRADE TRACKER    ");
        System.out.println("=======================================================\n");

        testGradeBoundaryCalculations();
        testStudentManagerAddAndDuplicate();
        testStudentManagerSearch();
        testStudentManagerUpdate();
        testStudentManagerDelete();
        testClassStatisticsNoStudents();
        testClassStatisticsSingleStudent();
        testClassStatisticsMultipleStudentsAndTies();
        testEndToEndConsoleMenuWorkflow();

        System.out.println("\n=======================================================");
        System.out.printf("TEST SUMMARY: %d / %d Tests Passed.%n", passedTests, totalTests);
        System.out.println("=======================================================");

        if (passedTests != totalTests) {
            System.err.println("Some tests failed!");
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY!");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName);
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        totalTests++;
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.printf("[FAIL] %s - Expected: '%s', Actual: '%s'%n", testName, expected, actual);
        }
    }

    private static void assertEquals(String testName, double expected, double actual, double delta) {
        totalTests++;
        if (Math.abs(expected - actual) <= delta) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.printf("[FAIL] %s - Expected: '%f', Actual: '%f'%n", testName, expected, actual);
        }
    }

    /**
     * Test 1: Verify all 5 grade ranges including boundary values (0, 59, 60, 69, 70, 79, 80, 89, 90, 100).
     */
    private static void testGradeBoundaryCalculations() {
        System.out.println("--- Test Group 1: Grade Boundaries ---");
        assertEquals("Grade for 100", "A", new Student("S1", "Alice", 100.0).getGrade());
        assertEquals("Grade for 90", "A", new Student("S2", "Bob", 90.0).getGrade());
        assertEquals("Grade for 89.9", "B", new Student("S3", "Charlie", 89.9).getGrade());
        assertEquals("Grade for 80", "B", new Student("S4", "David", 80.0).getGrade());
        assertEquals("Grade for 79.9", "C", new Student("S5", "Eve", 79.9).getGrade());
        assertEquals("Grade for 70", "C", new Student("S6", "Frank", 70.0).getGrade());
        assertEquals("Grade for 69.9", "D", new Student("S7", "Grace", 69.9).getGrade());
        assertEquals("Grade for 60", "D", new Student("S8", "Heidi", 60.0).getGrade());
        assertEquals("Grade for 59.9", "F", new Student("S9", "Ivan", 59.9).getGrade());
        assertEquals("Grade for 0", "F", new Student("S10", "Judy", 0.0).getGrade());
    }

    /**
     * Test 2: Adding students and duplicate ID prevention.
     */
    private static void testStudentManagerAddAndDuplicate() {
        System.out.println("\n--- Test Group 2: Add Student & Duplicate ID ---");
        StudentManager manager = new StudentManager();

        boolean added1 = manager.addStudent(new Student("S101", "Alice Smith", 85.0));
        assertTrue("Add Student S101", added1);
        assertEquals("Student count is 1", 1, manager.getStudentCount());

        boolean addedDuplicate = manager.addStudent(new Student("S101", "Duplicate Alice", 95.0));
        assertTrue("Reject duplicate ID S101", !addedDuplicate);
        assertEquals("Student count remains 1", 1, manager.getStudentCount());

        boolean addedCaseInsensitiveDuplicate = manager.addStudent(new Student("s101", "Case Duplicate", 92.0));
        assertTrue("Reject case-insensitive duplicate ID s101", !addedCaseInsensitiveDuplicate);
        assertEquals("Student count remains 1", 1, manager.getStudentCount());
    }

    /**
     * Test 3: Search functionality.
     */
    private static void testStudentManagerSearch() {
        System.out.println("\n--- Test Group 3: Search Student ---");
        StudentManager manager = new StudentManager();
        manager.addStudent(new Student("S101", "Alice", 92.0));

        Student found = manager.searchStudentById("S101");
        assertTrue("Search existing ID S101", found != null);
        if (found != null) {
            assertEquals("Found student name", "Alice", found.getName());
        }

        Student foundCase = manager.searchStudentById("s101");
        assertTrue("Search case-insensitive ID s101", foundCase != null);

        Student notFound = manager.searchStudentById("999");
        assertTrue("Search non-existent ID 999 returns null", notFound == null);

        Student invalidSearch = manager.searchStudentById("");
        assertTrue("Search empty string ID returns null", invalidSearch == null);
    }

    /**
     * Test 4: Update functionality.
     */
    private static void testStudentManagerUpdate() {
        System.out.println("\n--- Test Group 4: Update Student ---");
        StudentManager manager = new StudentManager();
        manager.addStudent(new Student("101", "Bob", 65.0));

        assertEquals("Initial grade before update", "D", manager.searchStudentById("101").getGrade());

        boolean updated = manager.updateStudent("101", "Bob Marley", 95.0);
        assertTrue("Update existing student returns true", updated);

        Student updatedStudent = manager.searchStudentById("101");
        assertEquals("Updated student name", "Bob Marley", updatedStudent.getName());
        assertEquals("Updated student marks", 95.0, updatedStudent.getMarks(), 0.001);
        assertEquals("Grade recalculated automatically to A", "A", updatedStudent.getGrade());

        boolean updateNonExistent = manager.updateStudent("999", "Ghost", 50.0);
        assertTrue("Update non-existent student returns false", !updateNonExistent);
    }

    /**
     * Test 5: Delete functionality.
     */
    private static void testStudentManagerDelete() {
        System.out.println("\n--- Test Group 5: Delete Student ---");
        StudentManager manager = new StudentManager();
        manager.addStudent(new Student("101", "Charlie", 78.0));
        manager.addStudent(new Student("102", "David", 88.0));

        assertEquals("Initial count is 2", 2, manager.getStudentCount());

        boolean deleted = manager.deleteStudent("101");
        assertTrue("Delete student 101 returns true", deleted);
        assertEquals("Count after deletion is 1", 1, manager.getStudentCount());

        Student searchDeleted = manager.searchStudentById("101");
        assertTrue("Deleted student no longer searchable", searchDeleted == null);

        boolean deleteNonExistent = manager.deleteStudent("999");
        assertTrue("Delete non-existent student returns false", !deleteNonExistent);
    }

    /**
     * Test 6: Class statistics with no students.
     */
    private static void testClassStatisticsNoStudents() {
        System.out.println("\n--- Test Group 6: Statistics with No Students ---");
        StudentManager manager = new StudentManager();
        // Redirect stdout to verify message without crashing or division by zero
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            manager.displayClassStatistics();
            String output = outContent.toString();
            assertTrue("Safe message displayed for empty list", output.contains("No students found. Cannot calculate statistics."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 7: Class statistics with single student.
     */
    private static void testClassStatisticsSingleStudent() {
        System.out.println("\n--- Test Group 7: Statistics with Single Student ---");
        StudentManager manager = new StudentManager();
        manager.addStudent(new Student("101", "Solo Student", 85.5));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            manager.displayClassStatistics();
            String output = outContent.toString();
            assertTrue("Total count 1 shown", output.contains("Total Number of Students : 1"));
            assertTrue("Average marks 85.50 shown", output.contains("Average Marks            : 85.50"));
            assertTrue("Highest marks 85.50 shown", output.contains("Highest Marks            : 85.50"));
            assertTrue("Lowest marks 85.50 shown", output.contains("Lowest Marks             : 85.50"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 8: Class statistics with multiple students and tied scores.
     */
    private static void testClassStatisticsMultipleStudentsAndTies() {
        System.out.println("\n--- Test Group 8: Statistics with Multiple Students & Tied Scores ---");
        StudentManager manager = new StudentManager();
        manager.addStudent(new Student("101", "Alice", 95.0)); // Top tie
        manager.addStudent(new Student("102", "Bob", 95.0));   // Top tie
        manager.addStudent(new Student("103", "Charlie", 50.0)); // Bottom tie
        manager.addStudent(new Student("104", "David", 50.0));   // Bottom tie

        // Total: 290. Average: 290 / 4 = 72.50
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            manager.displayClassStatistics();
            String output = outContent.toString();
            assertTrue("Total count 4 shown", output.contains("Total Number of Students : 4"));
            assertTrue("Average 72.50 shown", output.contains("Average Marks            : 72.50"));
            assertTrue("Highest 95.00 shown", output.contains("Highest Marks            : 95.00"));
            assertTrue("Both top achievers listed", output.contains("Alice (ID: 101), Bob (ID: 102)"));
            assertTrue("Lowest 50.00 shown", output.contains("Lowest Marks             : 50.00"));
            assertTrue("Both lowest achievers listed", output.contains("Charlie (ID: 103), David (ID: 104)"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 9: End-to-end CLI workflow testing menu navigation, invalid inputs, non-numeric strings, etc.
     */
    private static void testEndToEndConsoleMenuWorkflow() {
        System.out.println("\n--- Test Group 9: End-To-End Console Menu Workflow ---");

        // Simulated inputs:
        // invalid menu choice -> 99
        // menu 1 (Add Student) -> ID: 101, Name: John, Marks: -50 (invalid) -> Marks: 150 (invalid) -> Marks: abc (invalid) -> Marks: 85.5
        // menu 1 (Add Student with Duplicate ID) -> ID: 101 (duplicate error) -> ID: 102, Name: Jane, Marks: 92.0
        // menu 2 (View All)
        // menu 3 (Search) -> ID: 101
        // menu 3 (Search non-existent) -> ID: 999
        // menu 4 (Update) -> ID: 101, New Name: John Doe, New Marks: 90.0
        // menu 6 (Stats)
        // menu 7 (Report)
        // menu 5 (Delete) -> ID: 101 -> Confirm: N (cancel)
        // menu 5 (Delete) -> ID: 101 -> Confirm: Y (delete)
        // menu 2 (View All)
        // menu 8 (Exit)

        String simulatedInput = String.join("\n",
                "99",               // Invalid option
                "1",                // Add student
                "101", "John",
                "-50", "150", "abc", "85.5", // invalid marks then valid
                "1",                // Add student
                "101",              // Duplicate ID
                "102", "Jane", "92.0",
                "2",                // View All
                "3", "101",         // Search 101
                "3", "999",         // Search 999
                "4", "101", "John Doe", "90.0", // Update 101
                "6",                // Stats
                "7",                // Report
                "5", "101", "N",    // Delete 101, cancel
                "5", "101", "Y",    // Delete 101, confirm
                "2",                // View All
                "8"                 // Exit
        ) + "\n";

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream testIn = new ByteArrayInputStream(simulatedInput.getBytes());
        ByteArrayOutputStream testOut = new ByteArrayOutputStream();

        System.setIn(testIn);
        System.setOut(new PrintStream(testOut));

        try {
            Main.main(new String[]{});
            String output = testOut.toString();

            assertTrue("Handled invalid menu choice 99", output.contains("Please select a valid option between 1 and 8"));
            assertTrue("Handled negative marks -50", output.contains("Marks must be between 0 and 100 inclusive"));
            assertTrue("Handled over 100 marks 150", output.contains("Marks must be between 0 and 100 inclusive"));
            assertTrue("Handled nonnumeric marks abc", output.contains("Invalid numeric format"));
            assertTrue("Handled duplicate ID error", output.contains("already exists"));
            assertTrue("John Doe updated successfully", output.contains("John Doe"));
            assertTrue("Deletion cancelled message printed", output.contains("Deletion cancelled"));
            assertTrue("Deletion success message printed", output.contains("deleted successfully"));
            assertTrue("Graceful exit message printed", output.contains("Goodbye!"));

        } catch (Exception e) {
            System.err.println("Exception during End-To-End test: " + e.getMessage());
            e.printStackTrace();
            assertTrue("End-to-End menu execution completed without throwing exception", false);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}
