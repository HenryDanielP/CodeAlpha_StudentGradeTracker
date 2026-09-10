package com.nexus.studenttracker;

import com.nexus.studenttracker.repository.StudentRepository;
import com.nexus.studenttracker.service.GradeService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NexusApplication extends Application {
    @Override
    public void start(Stage stage) {
        StudentRepository repository = new StudentRepository();
        GradeService service = new GradeService(repository);

        NexusView view = new NexusView(service);

        Scene scene = new Scene(view, 1280, 800);
        scene.getStylesheets().add(
                getClass().getResource("/css/nexus.css").toExternalForm()
        );

        stage.setTitle("NEXUS — Student Grade Tracker");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
