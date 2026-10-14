package com.codealpha.gradetracker.service;

import org.springframework.stereotype.Component;

/**
 * Reusable utility service for calculating student letter grades and pass/fail statuses.
 */
@Component
public class GradeCalculator {

    /**
     * Calculates letter grade based on marks out of 100.
     * Scale:
     * 90.0 to 100.0 : A
     * 80.0 to < 90.0: B
     * 70.0 to < 80.0: C
     * 60.0 to < 70.0: D
     * 0.0 to < 60.0 : F
     *
     * @param marks Marks obtained (0.0 to 100.0)
     * @return Grade letter as String ("A", "B", "C", "D", or "F")
     */
    public String calculateGrade(double marks) {
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

    /**
     * Determines pass or fail status based on grade/marks.
     *
     * @param marks Marks obtained
     * @return "PASSED" if marks >= 60.0, "FAILED" otherwise
     */
    public String calculateStatus(double marks) {
        return marks >= 60.0 ? "PASSED" : "FAILED";
    }

    /**
     * Validates that marks are within the valid numeric range [0.0, 100.0].
     *
     * @param marks Marks to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMarks(Double marks) {
        return marks != null && marks >= 0.0 && marks <= 100.0;
    }
}
