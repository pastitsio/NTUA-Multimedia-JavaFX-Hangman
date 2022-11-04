package com.example.hangman;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


public class IntroController {
    private Dict dict;

    @FXML
    private Label dictLabel1, dictLabel2;
    @FXML
    private Button centerButton;

    final FileChooser fc = new FileChooser();


    /* Menubar */

    @FXML
    public void menuStart() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game.fxml"));
        Parent HangmanGameLayout = loader.load();

        GameController gameController = loader.getController();
        Main.primaryStage.setTitle("Game in Progress");
        Main.primaryStage.setScene(new Scene(HangmanGameLayout));

        // sets up new game (dict, activeWord and lives)
        // and assigns it to gameController.game;
        if (dict == null) {
            dict = new Dict().loadRandomFromFolder();
        }
        GameRound game = new GameRound(dict);
        GLOBALS.LOG("Open Library book ID: " + dict.getID());

        gameController.startGame(game);
    }

    @FXML
    public void menuLoad() {
        fc.setTitle("Choose Dictionary.");

        File file = fc.showOpenDialog(null);
        String filename = file.getName();
        dict = new Dict().loadFromFile(filename);
        dictLabel2.setText("ID = " + dict.getID());
        centerButton.setText("Start!");

    }

    @FXML
    public void menuCreate() {
        TextInputDialog tiDialog = new TextInputDialog();
        tiDialog.setTitle("Enter Book ID");
        tiDialog.setHeaderText("Format: OLXXXXXXW");
        tiDialog.setContentText("Enter: ");


        Optional<String> result = tiDialog.showAndWait();
        if (result.isPresent()) {
            String bookID = result.get();
            dictLabel1.setText("Checking validity with API, ");
            dictLabel2.setText("ID = " + bookID);

            try {
                dict = new Dict().loadDictFromID(bookID);
                dictLabel1.setText("Dictionary created: ");
                dictLabel2.setText("ID = " + bookID);
                centerButton.setText("Start!");
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("ERROR OCCURED! RETRY!");
                alert.setContentText(e.getMessage());
                alert.show();
                dictLabel1.setText("Dictionary Selected: ");
                dictLabel2.setText("Random");
            }

        }
    }

    @FXML
    public void menuExit() {
        GLOBALS.exit();
    }


    @FXML
    public void menuDictionary() {
        if (this.dict != null) {
            GLOBALS.activeDictionaryInfo(this.dict.getWordList());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("No Dictionary selected!");
            alert.show();
        }
    }

    @FXML
    public void menuRounds() {
        GLOBALS.showRounds();
    }


}
