package com.codealpha.gradetracker;

import com.codealpha.gradetracker.dto.ClassStatisticsDTO;
import com.codealpha.gradetracker.dto.StudentDTO;
import com.codealpha.gradetracker.service.GradeCalculator;
import com.codealpha.gradetracker.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class StudentGradeTrackerApplicationTests {

    @Autowired
    private StudentService studentService;

    @Autowired
    private GradeCalculator gradeCalculator;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(studentService);
        assertNotNull(gradeCalculator);
    }

    @Test
    @DisplayName("Verify Grade Boundary Rules")
    void testGradeBoundaries() {
        assertEquals("A", gradeCalculator.calculateGrade(100.0));
        assertEquals("A", gradeCalculator.calculateGrade(90.0));
        assertEquals("B", gradeCalculator.calculateGrade(89.9));
        assertEquals("B", gradeCalculator.calculateGrade(80.0));
        assertEquals("C", gradeCalculator.calculateGrade(79.9));
        assertEquals("C", gradeCalculator.calculateGrade(70.0));
        assertEquals("D", gradeCalculator.calculateGrade(69.9));
        assertEquals("D", gradeCalculator.calculateGrade(60.0));
        assertEquals("F", gradeCalculator.calculateGrade(59.9));
        assertEquals("F", gradeCalculator.calculateGrade(0.0));
    }

    @Test
    @DisplayName("Verify Invalid Marks Range Rejection")
    void testInvalidMarksRejection() {
        assertFalse(gradeCalculator.isValidMarks(-1.0));
        assertFalse(gradeCalculator.isValidMarks(101.0));
        assertTrue(gradeCalculator.isValidMarks(0.0));
        assertTrue(gradeCalculator.isValidMarks(100.0));
    }

    @Test
    @DisplayName("Create Student and Prevent Duplicate ID")
    void testCreateAndDuplicateRejection() {
        StudentDTO dto = new StudentDTO("TST101", "Test Student", "10A", 88.0);
        StudentDTO created = studentService.createStudent(dto);

        assertNotNull(created.getId());
        assertEquals("TST101", created.getStudentId());
        assertEquals("B", created.getGrade());
        assertEquals("PASSED", created.getStatus());

        // Attempt duplicate creation
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.createStudent(new StudentDTO("TST101", "Duplicate Student", "10A", 95.0));
        });
    }

    @Test
    @DisplayName("Update Student Details & Recalculate Grade")
    void testUpdateStudent() {
        StudentDTO created = studentService.createStudent(new StudentDTO("TST102", "Initial Name", "10A", 55.0));
        assertEquals("F", created.getGrade());

        StudentDTO updatedDTO = new StudentDTO("TST102", "Updated Name", "10B", 92.0);
        StudentDTO updated = studentService.updateStudent("TST102", updatedDTO);

        assertEquals("Updated Name", updated.getName());
        assertEquals("10B", updated.getClassName());
        assertEquals(92.0, updated.getMarks());
        assertEquals("A", updated.getGrade());
        assertEquals("PASSED", updated.getStatus());
    }

    @Test
    @DisplayName("Calculate Class Statistics Accuracy")
    void testClassStatistics() {
        studentService.createStudent(new StudentDTO("T1", "Student One", "10A", 100.0));
        studentService.createStudent(new StudentDTO("T2", "Student Two", "10A", 50.0));

        ClassStatisticsDTO stats = studentService.getStatistics();
        assertTrue(stats.getTotalStudents() >= 2);
        assertTrue(stats.getAverageMarks() > 0);
        assertEquals(100.0, stats.getHighestMarks());
        assertTrue(stats.getHighestScorers().contains("Student One"));
    }
}
