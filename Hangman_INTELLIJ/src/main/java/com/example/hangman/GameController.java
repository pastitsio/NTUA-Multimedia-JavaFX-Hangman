package com.example.hangman;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.io.*;
import java.util.*;

public class GameController {
    private GameRound gcGame;

    @FXML
    private Text displayWord, possibleLetters, displayActiveWordCount, displayPoints, displaySuccess;

    // Image
    @FXML
    private Circle head;
    @FXML
    private Text eyes;
    @FXML
    private CubicCurve leftArm;
    @FXML
    private QuadCurve body;
    @FXML
    private Line rightArm, leftLeg;

    @FXML
    private Slider inputPosition;
    @FXML
    private TextField inputLetter;

    private final Alert alert = new Alert(Alert.AlertType.NONE);

    private void updateDisplayTopBar() {
        displayActiveWordCount.setText("Word Count = " + gcGame.getActiveWordListCount());
        displayPoints.setText("Points = " + gcGame.getPoints());
        displaySuccess.setText("Success = " + gcGame.getSuccess());
    }

    private void updateDisplayWord () {
        // pretty-prints active word to screen,
        // with spacing between characters.

        String _activeWord = new String(this.gcGame.getActiveWord());
        char[] _word = new char[this.gcGame.getActiveWord().length * GLOBALS.SPACING];
        for (int i = 0; i < _activeWord.length(); i++) {
            _word[GLOBALS.SPACING * i] = _activeWord.charAt(i);
        }
        String toDisplay = new String(_word).replace("\0", " ");
        displayWord.setText("Word:  " + toDisplay);
    }

    private void updateDisplayImage() {
        int lives = gcGame.getLives();
       // if lives == 6 do nothing;
        if (lives == 5){
            head.setVisible(true);
        }
        if (lives == 4){
            eyes.setVisible(true);
        }
        if (lives == 3){
            leftArm.setVisible(true);
        }
        if (lives == 2){
            rightArm.setVisible(true);
        }
        if (lives == 1){
            body.setVisible(true);
        }
        if (lives == 0){
            leftLeg.setVisible(true);
        }

    }

    private void updateDisplayProbabilities() {
        HashMap<Integer, HashMap<Character, Double>> probsPerPos = gcGame.getProbabilitiesPerPos();

        StringBuilder toReturn = new StringBuilder(400);
        for (int pos = 0; pos < gcGame.getGameWord().length(); ++pos) {
            toReturn.append("\npos ").append(pos + 1).append(":\n").append("[");

            HashMap<Character, Double> currentPos = probsPerPos.get(pos);
            Character[] keys = new Character[currentPos.keySet().size()];
            currentPos.keySet().toArray(keys);
            double temp = currentPos.get(keys[0]);

            for (Character key: keys) {
                if (currentPos.get(key) == temp) {
                    toReturn.append(key).append(", ");
                } else {
                    temp = currentPos.  get(key);
                    toReturn.append("], [").append(key).append(", ");
                }
            }
            toReturn.append("]\n\n");

        }

        possibleLetters.setText(toReturn.toString().replaceAll(", ]", "]"));
    }

    private void updateDisplayInterface(){
        updateDisplayTopBar();
        updateDisplayImage();
        updateDisplayWord();
        updateDisplayProbabilities();
    }


    // Only called once.
    private void setupSlider() {
        this.inputPosition.setMin(1.0);
        this.inputPosition.setMax(this.gcGame.getGameWord().length());
        this.inputPosition.setValue((int)(this.gcGame.getGameWord().length()/2.0));

        this.inputPosition.setBlockIncrement(1.0);
        this.inputPosition.setMajorTickUnit(1.0);
        this.inputPosition.setShowTickMarks(false);
        this.inputPosition.setSnapToTicks(true);
    }

    public void startGame(GameRound game) {
        this.gcGame = game;
        setupSlider();
        updateDisplayInterface();

        GLOBALS.LOG(this.gcGame.getActiveWordListString());
    }

    private void storeRoundResult(boolean result) throws IOException {
        File winLog = new File(GLOBALS.winLog);
        winLog.createNewFile();
        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(winLog, true));

        String winner = result ? "Player" : "CPU";
        int tries = result ? gcGame.getSuccess() : 6 - gcGame.getLives();
        ArrayList<String> row = new ArrayList<>(
                Arrays.asList(gcGame.getGameWord(), String.valueOf(tries), winner));
        csvWriter.append(String.join(",", row));
        csvWriter.newLine();

        GLOBALS.LOG(String.join(",", row));

        csvWriter.flush();
        csvWriter.close();
    }

    // store result and load end page.
    private void gameOver(boolean win) throws IOException {
        this.storeRoundResult(win);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("endPage.fxml"));
        Parent EndPageLostLayout = loader.load();
        EndPageController EndPageController = loader.getController();

        EndPageController.setResultLabel(win);
        EndPageController.setSolution(gcGame.getGameWord());

        Main.primaryStage.setTitle("Game Over");
        Main.primaryStage.setScene(new Scene(EndPageLostLayout));
    }


    @FXML
    public void inputButtonClick() throws IOException {
        // On button press, we get user input. If letter input is invalid or
        // not contained in prob list, prompt to retry.
        char letter = Character.toUpperCase(
                inputLetter.getText().charAt(0)
        );
        int pos = (int) inputPosition.getValue() - 1;

        HashMap<Integer, HashMap<Character, Double>> probsPerPos = gcGame.getProbabilitiesPerPos();
        Set<Character> validLetters = probsPerPos.get(pos).keySet();

        alert.setAlertType(Alert.AlertType.ERROR);
        if (!Character.isLetter(letter)) {
            alert.setContentText("Input must be letter!");
            alert.show(); return;
        } else if (!validLetters.contains(letter)) {
            alert.setContentText("Choose a letter from " + validLetters);
            alert.show(); return;
        } else if (Character.isLetter(gcGame.getActiveWord()[pos])){
            alert.setContentText("Position already filled!");
            alert.show(); return;
        }

        gcGame.play(letter, pos);

        if (!gcGame.isOver()) {
            if (gcGame.isWon()) {
                gameOver(true); // WIN.
            }
            updateDisplayInterface();
        } else {
            gameOver(false);   // LOSS.
        }


    }

    // setting visibility for body parts.
    @FXML
    public void initialize(){
        head.setVisible(false);
        eyes.setVisible(false);
        leftArm.setVisible(false);
        rightArm.setVisible(false);
        body.setVisible(false);
        leftLeg.setVisible(false);
    }


    /* Menubar */
    @FXML
    public void startButtonClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("New Game started!");
        alert.show();

        this.startGame(new GameRound(this.gcGame.getDict()));
    }

    @FXML
    private void exitButtonClick () {
        GLOBALS.exit();
    }

    @FXML
    public void menuDictionary() {
        GLOBALS.activeDictionaryInfo(this.gcGame.getWordList());
    }

    @FXML
    public void menuRounds() {
        GLOBALS.showRounds();
    }

    @FXML
    void menuSolution() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Solution");
        alert.setHeaderText("Show Solution and store result as LOSS?");
        alert.setContentText("Choose your option.");

        ButtonType buttonTypeOne = new ButtonType("Yes");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setContentText("Solution: " + gcGame.getGameWord());
            alert1.showAndWait();
            gameOver(false);
        }
    }


}
