package com.example.hangman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        Main.primaryStage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("intro.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 800 , 550);

        primaryStage.setTitle("Medialab Hangman");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}