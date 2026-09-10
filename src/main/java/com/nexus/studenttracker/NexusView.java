package com.nexus.studenttracker;

import com.nexus.studenttracker.model.Student;
import com.nexus.studenttracker.service.GradeService;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

public class NexusView extends BorderPane {
    private final GradeService service;
    private final StackPane content = new StackPane();
    private final Label pageTitle = new Label("DASHBOARD");
    private final Label toast = new Label();

    private final String[] subjects = {"Mathematics", "Science", "English", "Java"};

    public NexusView(GradeService service) {
        this.service = service;
        setLeft(buildSidebar());
        setTop(buildTopBar());
        setCenter(content);
        showDashboard();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(24, 16, 20, 16));
        sidebar.setPrefWidth(230);

        Label logo = new Label("NEXUS");
        logo.getStyleClass().add("logo");

        Label subtitle = new Label("STUDENT INTELLIGENCE");
        subtitle.getStyleClass().add("sidebar-subtitle");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button dashboard = navButton("⌂", "Dashboard");
        Button students = navButton("▣", "Students");
        Button analytics = navButton("◈", "Analytics");

        dashboard.setOnAction(e -> showDashboard());
        students.setOnAction(e -> showStudents());
        analytics.setOnAction(e -> showAnalytics());

        Label task = new Label("CODEALPHA • TASK 1");
        task.getStyleClass().add("sidebar-footer");

        sidebar.getChildren().addAll(logo, subtitle, new Separator(),
                dashboard, students, analytics, spacer, task);
        return sidebar;
    }

    private Button navButton(String icon, String text) {
        Button b = new Button(icon + "   " + text);
        b.getStyleClass().add("nav-button");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(15);
        bar.getStyleClass().add("topbar");
        bar.setPadding(new Insets(18, 28, 18, 28));
        bar.setAlignment(Pos.CENTER_LEFT);

        pageTitle.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label live = new Label("●  LIVE DATA");
        live.getStyleClass().add("live-pill");

        Button add = new Button("+  ADD STUDENT");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> showStudentDialog(null));

        bar.getChildren().addAll(pageTitle, spacer, live, add);
        return bar;
    }

    private VBox page(String title, String description) {
        pageTitle.setText(title.toUpperCase());
        VBox root = new VBox(22);
        root.setPadding(new Insets(26, 30, 30, 30));
        Label desc = new Label(description);
        desc.getStyleClass().add("page-description");
        root.getChildren().add(desc);
        return root;
    }

    private void showDashboard() {
        VBox root = page("Dashboard", "Real-time overview of student performance and grade health.");

        GridPane cards = new GridPane();
        cards.setHgap(16);
        cards.setVgap(16);
        cards.setMaxWidth(Double.MAX_VALUE);

        Student top = service.getTopStudent();
        Student low = service.getLowestStudent();

        cards.add(statCard("TOTAL STUDENTS", String.valueOf(service.getStudents().size()), "Enrolled records"), 0, 0);
        cards.add(statCard("CLASS AVERAGE", format(service.getOverallAverage()) + "%", "Across all subjects"), 1, 0);
        cards.add(statCard("HIGHEST AVERAGE", top == null ? "—" : format(top.getAverage()) + "%", top == null ? "No data" : top.getName()), 2, 0);
        cards.add(statCard("LOWEST AVERAGE", low == null ? "—" : format(low.getAverage()) + "%", low == null ? "No data" : low.getName()), 3, 0);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(25);
        cards.getColumnConstraints().addAll(cc, new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints());

        HBox lower = new HBox(18);
        lower.setFillHeight(true);
        VBox.setVgrow(lower, Priority.ALWAYS);

        VBox performance = panel("PERFORMANCE SNAPSHOT");
        Label pass = new Label(service.getPassingCount() + " students passing");
        pass.getStyleClass().add("big-number");
        Label risk = new Label(service.getAtRiskCount() + " students at risk");
        risk.getStyleClass().add("risk-text");
        ProgressBar progress = new ProgressBar(service.getStudents().isEmpty() ? 0 :
                (double) service.getPassingCount() / service.getStudents().size());
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.getStyleClass().add("progress");
        performance.getChildren().addAll(pass, risk, progress);

        VBox recent = panel("TOP PERFORMERS");
        if (service.getStudents().isEmpty()) {
            recent.getChildren().add(emptyState("No students yet", "Add your first student to populate NEXUS."));
        } else {
            service.getStudents().stream()
                    .sorted((a,b) -> Double.compare(b.getAverage(), a.getAverage()))
                    .limit(5)
                    .forEach(s -> recent.getChildren().add(studentRow(s)));
        }

        HBox.setHgrow(performance, Priority.ALWAYS);
        HBox.setHgrow(recent, Priority.ALWAYS);
        lower.getChildren().addAll(performance, recent);

        VBox quick = panel("QUICK ACTIONS");
        HBox quickActions = new HBox(10);
        Button addQuick = new Button("+ ADD STUDENT");
        addQuick.getStyleClass().add("primary-button");
        addQuick.setOnAction(e -> showStudentDialog(null));
        Button viewStudents = new Button("VIEW DIRECTORY");
        viewStudents.getStyleClass().add("small-button");
        viewStudents.setOnAction(e -> showStudents());
        Button viewAnalytics = new Button("OPEN ANALYTICS");
        viewAnalytics.getStyleClass().add("small-button");
        viewAnalytics.setOnAction(e -> showAnalytics());
        quickActions.getChildren().addAll(addQuick, viewStudents, viewAnalytics);
        quick.getChildren().add(quickActions);

        root.getChildren().addAll(cards, lower, quick);
        show(root);
    }

    private VBox statCard(String label, String value, String detail) {
        VBox box = new VBox(8);
        box.getStyleClass().add("stat-card");
        Label l = new Label(label);
        l.getStyleClass().add("card-label");
        Label v = new Label(value);
        v.getStyleClass().add("card-value");
        Label d = new Label(detail);
        d.getStyleClass().add("card-detail");
        box.getChildren().addAll(l, v, d);
        return box;
    }

    private VBox panel(String title) {
        VBox box = new VBox(14);
        box.getStyleClass().add("panel");
        box.setPadding(new Insets(20));
        Label t = new Label(title);
        t.getStyleClass().add("panel-title");
        box.getChildren().add(t);
        return box;
    }

    private HBox studentRow(Student s) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("student-row");

        Label name = new Label(s.getName());
        name.getStyleClass().add("row-name");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label grade = new Label(s.getLetterGrade());
        grade.getStyleClass().add("grade-badge");
        Label avg = new Label(format(s.getAverage()) + "%");
        avg.getStyleClass().add("row-average");

        row.getChildren().addAll(name, spacer, grade, avg);
        return row;
    }

    private void showStudents() {
        VBox root = page("Students", "Manage student records, grades and individual performance.");

        HBox tools = new HBox(12);
        TextField search = new TextField();
        search.setPromptText("Search students...");
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        Label count = new Label(service.getStudents().size() + " RECORDS");
        count.getStyleClass().add("record-count");

        tools.getChildren().addAll(search, count);

        VBox list = new VBox(8);
        list.getStyleClass().add("student-list");

        Runnable refresh = () -> {
            list.getChildren().clear();
            String query = search.getText().trim().toLowerCase();
            service.getStudents().stream()
                    .filter(s -> s.getName().toLowerCase().contains(query))
                    .forEach(s -> list.getChildren().add(fullStudentRow(s)));
            count.setText(list.getChildren().size() + " RECORDS");
        };
        search.textProperty().addListener((obs, old, now) -> refresh.run());
        refresh.run();

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("clean-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(tools, scroll);
        show(root);
    }

    private HBox fullStudentRow(Student s) {
        HBox row = studentRow(s);
        row.getStyleClass().add("interactive-row");

        Label subjects = new Label(s.getGrades().size() + " subjects");
        subjects.getStyleClass().add("row-detail");

        ProgressBar scoreBar = new ProgressBar(s.getAverage() / 100.0);
        scoreBar.setPrefWidth(110);
        scoreBar.getStyleClass().add("mini-progress");

        Button edit = new Button("EDIT");
        edit.getStyleClass().add("small-button");
        edit.setOnAction(e -> showStudentDialog(s));

        Button delete = new Button("DELETE");
        delete.getStyleClass().add("danger-button");
        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + s.getName() + "?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Remove student record");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    service.deleteStudent(s);
                    showStudents();
                    showToast("Student deleted");
                }
            });
        });

        row.getChildren().addAll(subjects, scoreBar, edit, delete);
        return row;
    }

    private void showAnalytics() {
        VBox root = page("Analytics", "Visualize grade distribution and subject performance.");

        HBox charts = new HBox(18);
        VBox distribution = panel("GRADE DISTRIBUTION");
        PieChart pie = new PieChart();
        pie.setLegendVisible(true);
        pie.setLabelsVisible(true);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String grade : new String[]{"A+", "A", "B", "C", "D", "F"}) counts.put(grade, 0);
        for (Student s : service.getStudents()) counts.computeIfPresent(s.getLetterGrade(), (k,v) -> v + 1);
        counts.forEach((k,v) -> { if (v > 0) pie.getData().add(new PieChart.Data(k, v)); });
        distribution.getChildren().add(pie);

        VBox subjectsPanel = panel("SUBJECT AVERAGES");
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis(0, 100, 10);
        BarChart<String, Number> bar = new BarChart<>(x, y);
        bar.setLegendVisible(false);
        bar.setAnimated(false);
        for (String subject : subjects) {
            double avg = service.getStudents().stream()
                    .filter(s -> s.getGrades().containsKey(subject))
                    .mapToDouble(s -> s.getGrades().get(subject))
                    .average().orElse(0);
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(subject, avg));
            bar.getData().add(series);
        }
        subjectsPanel.getChildren().add(bar);

        HBox.setHgrow(distribution, Priority.ALWAYS);
        HBox.setHgrow(subjectsPanel, Priority.ALWAYS);
        charts.getChildren().addAll(distribution, subjectsPanel);

        VBox insight = panel("ACADEMIC INSIGHTS");
        Student top = service.getTopStudent();
        String topText = top == null ? "No student data yet." :
                "Top performer: " + top.getName() + " • " + format(top.getAverage()) + "%";
        Label insightText = new Label(topText + "\n" +
                "Pass rate: " + (service.getStudents().isEmpty() ? "0" :
                format(service.getPassingCount() * 100.0 / service.getStudents().size())) + "%");
        insightText.getStyleClass().add("insight-text");
        insight.getChildren().add(insightText);

        root.getChildren().addAll(charts, insight);
        VBox.setVgrow(charts, Priority.ALWAYS);
        show(root);
    }

    private void showStudentDialog(Student existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Student" : "Edit Student");
        dialog.setHeaderText(existing == null ? "Create a new student record" : "Update student grades");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField name = new TextField(existing == null ? "" : existing.getName());
        name.setPromptText("Student name");

        grid.add(new Label("Student Name"), 0, 0);
        grid.add(name, 1, 0);

        Map<String, TextField> fields = new LinkedHashMap<>();
        for (int i = 0; i < subjects.length; i++) {
            TextField field = new TextField();
            field.setPromptText("0 - 100");
            if (existing != null && existing.getGrades().containsKey(subjects[i])) {
                field.setText(String.valueOf(existing.getGrades().get(subjects[i])));
            }
            fields.put(subjects[i], field);
            grid.add(new Label(subjects[i]), 0, i + 1);
            grid.add(field, 1, i + 1);
        }

        dialog.getDialogPane().setContent(grid);
        ButtonType save = new ButtonType("SAVE RECORD", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        Node saveButton = dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (name.getText().trim().isEmpty()) throw new IllegalArgumentException("Enter a student name.");
                Student result = new Student(name.getText().trim());
                for (Map.Entry<String, TextField> entry : fields.entrySet()) {
                    double score = Double.parseDouble(entry.getValue().getText().trim());
                    if (score < 0 || score > 100) throw new IllegalArgumentException("Scores must be between 0 and 100.");
                    result.setGrade(entry.getKey(), score);
                }

                if (existing == null) service.addStudent(result);
                else service.updateStudent(existing, result);
                showStudents();
                showToast(existing == null ? "Student added successfully" : "Student updated successfully");
            } catch (Exception ex) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                alert.setHeaderText("Invalid student data");
                alert.showAndWait();
            }
        });

        dialog.showAndWait();
    }

    private Label emptyState(String title, String message) {
        Label label = new Label(title + "\n" + message);
        label.getStyleClass().add("empty-state");
        return label;
    }

    private void show(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        content.getChildren().setAll(node);
        fade.play();
    }

    private void showToast(String message) {
        toast.setText(message);
        toast.getStyleClass().setAll("toast");
        if (!content.getChildren().contains(toast)) {
            getChildren().add(toast);
            StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(toast, new Insets(0, 30, 25, 0));
        }
        toast.setOpacity(1);
        FadeTransition fade = new FadeTransition(Duration.seconds(2.2), toast);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setDelay(Duration.seconds(1));
        fade.play();
    }

    private String format(double value) {
        return String.format("%.1f", value);
    }
}
