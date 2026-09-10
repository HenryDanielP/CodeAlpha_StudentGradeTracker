package com.nexus.studenttracker.repository;

import com.nexus.studenttracker.model.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentRepository {
    private final Path dataFile = Path.of("data", "students.csv");

    public StudentRepository() {
        try {
            Files.createDirectories(dataFile.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory.", e);
        }
    }

    public List<Student> load() {
        List<Student> students = new ArrayList<>();
        if (!Files.exists(dataFile)) return students;

        try {
            for (String line : Files.readAllLines(dataFile)) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 1) continue;

                Map<String, Double> grades = new LinkedHashMap<>();
                for (int i = 1; i + 1 < parts.length; i += 2) {
                    if (!parts[i].isBlank()) {
                        grades.put(parts[i], Double.parseDouble(parts[i + 1]));
                    }
                }
                students.add(new Student(parts[0], grades));
            }
        } catch (Exception e) {
            System.err.println("Could not load saved students: " + e.getMessage());
        }
        return students;
    }

    public void save(List<Student> students) {
        List<String> lines = new ArrayList<>();
        lines.add("# NEXUS Student Grade Tracker data");
        for (Student student : students) {
            StringBuilder line = new StringBuilder(student.getName().replace(",", " "));
            for (Map.Entry<String, Double> entry : student.getGrades().entrySet()) {
                line.append(",").append(entry.getKey())
                    .append(",").append(entry.getValue());
            }
            lines.add(line.toString());
        }

        try {
            Files.write(
                dataFile,
                lines,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Could not save student data.", e);
        }
    }
}
