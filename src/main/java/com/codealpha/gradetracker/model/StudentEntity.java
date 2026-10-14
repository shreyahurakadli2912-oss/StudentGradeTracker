package com.codealpha.gradetracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_id", columnList = "student_id", unique = true)
})
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Student ID cannot be blank")
    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @NotBlank(message = "Student name cannot be blank")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "class_name")
    private String className;

    @NotNull(message = "Marks cannot be null")
    @Min(value = 0, message = "Marks cannot be less than 0")
    @Max(value = 100, message = "Marks cannot be greater than 100")
    @Column(name = "marks", nullable = false)
    private Double marks;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "subject_breakdown", length = 4000)
    private String subjectBreakdown;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public StudentEntity() {
    }

    public StudentEntity(String studentId, String name, String className, Double marks) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.marks = marks;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.className == null || this.className.trim().isEmpty()) {
            this.className = "General";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSubjectBreakdown() {
        return subjectBreakdown;
    }

    public void setSubjectBreakdown(String subjectBreakdown) {
        this.subjectBreakdown = subjectBreakdown;
    }
}
