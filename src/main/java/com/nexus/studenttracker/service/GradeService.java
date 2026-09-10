package com.nexus.studenttracker.service;

import com.nexus.studenttracker.model.Student;
import com.nexus.studenttracker.repository.StudentRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GradeService {
    private final StudentRepository repository;
    private final List<Student> students;

    public GradeService(StudentRepository repository) {
        this.repository = repository;
        this.students = new ArrayList<>(repository.load());
    }

    public List<Student> getStudents() {
        return students;
    }

    public void addStudent(Student student) {
        students.add(student);
        save();
    }

    public void updateStudent(Student oldStudent, Student replacement) {
        int index = students.indexOf(oldStudent);
        if (index >= 0) {
            students.set(index, replacement);
            save();
        }
    }

    public void deleteStudent(Student student) {
        students.remove(student);
        save();
    }

    public void save() {
        repository.save(students);
    }

    public double getOverallAverage() {
        return students.stream().mapToDouble(Student::getAverage).average().orElse(0);
    }

    public Student getTopStudent() {
        return students.stream()
                .max(Comparator.comparingDouble(Student::getAverage))
                .orElse(null);
    }

    public Student getLowestStudent() {
        return students.stream()
                .min(Comparator.comparingDouble(Student::getAverage))
                .orElse(null);
    }

    public long getPassingCount() {
        return students.stream().filter(s -> s.getAverage() >= 50).count();
    }

    public long getAtRiskCount() {
        return students.size() - getPassingCount();
    }
}
