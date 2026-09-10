package com.nexus.studenttracker.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Student {
    private String name;
    private final Map<String, Double> grades = new LinkedHashMap<>();

    public Student(String name) {
        this.name = name;
    }

    public Student(String name, Map<String, Double> grades) {
        this.name = name;
        this.grades.putAll(grades);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Double> getGrades() {
        return grades;
    }

    public void setGrade(String subject, double score) {
        grades.put(subject, score);
    }

    public double getAverage() {
        if (grades.isEmpty()) return 0;
        return grades.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public double getHighest() {
        return grades.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    public double getLowest() {
        return grades.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    public String getLetterGrade() {
        double avg = getAverage();
        if (avg >= 90) return "A+";
        if (avg >= 80) return "A";
        if (avg >= 70) return "B";
        if (avg >= 60) return "C";
        if (avg >= 50) return "D";
        return "F";
    }

    public String getStatus() {
        return getAverage() >= 50 ? "PASS" : "AT RISK";
    }
}
