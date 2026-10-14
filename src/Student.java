/**
 * Represents an individual student with ID, Name, and Marks out of 100.
 * Provides grade calculation according to standard grade boundaries.
 */
public class Student {
    private String id;
    private String name;
    private double marks;

    /**
     * Constructor for Student.
     *
     * @param id    Unique Student ID
     * @param name  Student Full Name
     * @param marks Marks obtained (0 to 100 inclusive)
     */
    public Student(String id, String name, double marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    /**
     * Calculates the letter grade based on marks.
     * Grade scale:
     * 90 to 100      : A
     * 80 to < 90     : B
     * 70 to < 80     : C
     * 60 to < 70     : D
     * 0 to < 60      : F
     *
     * @return Letter grade as String ("A", "B", "C", "D", or "F")
     */
    public String getGrade() {
        if (marks >= 90.0 && marks <= 100.0) {
            return "A";
        } else if (marks >= 80.0) {
            return "B";
        } else if (marks >= 70.0) {
            return "C";
        } else if (marks >= 60.0) {
            return "D";
        } else {
            return "F";
        }
    }

    @Override
    public String toString() {
        return String.format("| %-12s | %-22s | %-8.2f | %-12s |", id, name, marks, getGrade());
    }
}
