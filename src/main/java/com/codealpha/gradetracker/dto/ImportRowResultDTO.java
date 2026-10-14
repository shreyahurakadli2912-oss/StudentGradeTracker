package com.codealpha.gradetracker.dto;

public class ImportRowResultDTO {

    private int rowNumber;
    private String studentId;
    private String name;
    private String className;
    private Double marks;
    private String grade;
    private String status;
    private boolean valid;
    private String errorReason;

    public ImportRowResultDTO() {
    }

    public ImportRowResultDTO(int rowNumber, String studentId, String name, String className, Double marks, boolean valid, String errorReason) {
        this.rowNumber = rowNumber;
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.marks = marks;
        this.valid = valid;
        this.errorReason = errorReason;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }
}
