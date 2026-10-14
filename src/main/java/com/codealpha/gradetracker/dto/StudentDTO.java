package com.codealpha.gradetracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StudentDTO {

    private Long id;

    @NotBlank(message = "Student ID cannot be empty")
    private String studentId;

    @NotBlank(message = "Student Name cannot be empty")
    private String name;

    private String className;

    @NotNull(message = "Marks cannot be empty")
    @Min(value = 0, message = "Marks must be between 0 and 100")
    @Max(value = 100, message = "Marks must be between 0 and 100")
    private Double marks;

    private String grade;
    private String status;
    private String subjectBreakdown;

    public StudentDTO() {
    }

    public StudentDTO(String studentId, String name, String className, Double marks) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.marks = marks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubjectBreakdown() {
        return subjectBreakdown;
    }

    public void setSubjectBreakdown(String subjectBreakdown) {
        this.subjectBreakdown = subjectBreakdown;
    }
}
