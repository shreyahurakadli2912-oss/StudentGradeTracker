package com.codealpha.gradetracker.dto;

import java.util.Map;

public class ClassStatisticsDTO {

    private long totalStudents;
    private double totalMarks;
    private double averageMarks;
    private double passPercentage;
    private long passedCount;
    private long failedCount;
    private double highestMarks;
    private String highestScorers;
    private double lowestMarks;
    private String lowestScorers;
    private Map<String, Long> gradeDistribution;

    public ClassStatisticsDTO() {
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getAverageMarks() {
        return averageMarks;
    }

    public void setAverageMarks(double averageMarks) {
        this.averageMarks = averageMarks;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public long getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(long passedCount) {
        this.passedCount = passedCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }

    public double getHighestMarks() {
        return highestMarks;
    }

    public void setHighestMarks(double highestMarks) {
        this.highestMarks = highestMarks;
    }

    public String getHighestScorers() {
        return highestScorers;
    }

    public void setHighestScorers(String highestScorers) {
        this.highestScorers = highestScorers;
    }

    public double getLowestMarks() {
        return lowestMarks;
    }

    public void setLowestMarks(double lowestMarks) {
        this.lowestMarks = lowestMarks;
    }

    public String getLowestScorers() {
        return lowestScorers;
    }

    public void setLowestScorers(String lowestScorers) {
        this.lowestScorers = lowestScorers;
    }

    public Map<String, Long> getGradeDistribution() {
        return gradeDistribution;
    }

    public void setGradeDistribution(Map<String, Long> gradeDistribution) {
        this.gradeDistribution = gradeDistribution;
    }
}
