package com.example.hangman;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.IOException;

import static com.example.hangman.Main.primaryStage;

public class EndPageController {

    @FXML
    private Label resultLabel, solution;

    @FXML
    public void setResultLabel(boolean win) {
        resultLabel.setText(win ? "YOU WON!" : "YOU LOST!");
    }

    @FXML
    public void setSolution(String sol) {
        solution.setText("Solution: " + sol);
    }

    @FXML
    public void restartButtonClick() throws IOException {
        FXMLLoader fxmlLoader =  new FXMLLoader(Main.class.getResource("intro.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800 , 550);

        primaryStage.setTitle("Medialab Hangman");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    private void exitButtonClick () {
        GLOBALS.exit();
    }

}
