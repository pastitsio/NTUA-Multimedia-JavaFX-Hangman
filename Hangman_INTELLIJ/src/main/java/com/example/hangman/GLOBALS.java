package com.example.hangman;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class GLOBALS {
    public final static String folder = "./medialab";
    public final static int SPACING = 2;
    public static void LOG(String s) { System.out.println(s); }
    public final static String winLog = "./rounds.csv";

    public static void showRounds() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rounds");
        alert.setHeaderText("Last 5 Rounds");
        try {
            Scanner sc = new Scanner(new File(GLOBALS.winLog));
            String[] output = new String[5];

            int count = 0;
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(",");
                output[(count++)%5] = "Word: " + line[0] + ", Tries: " + line[1] + ", Winner: " + line[2];
            }
            sc.close();

            StringBuilder out = new StringBuilder();
            for (String round: output)
                if (round != null)
                    out.append(round).append("\n");

            alert.setContentText(String.valueOf(out));
        } catch (FileNotFoundException e) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("No Games Played!");
        }

        alert.show();


    }

    public static void activeDictionaryInfo(ArrayList<String> wordList) {
        int six = 0, tenAndMore = 0, rest = 0;

        for (String word : wordList) {
            if (word.length() == 6) ++six;
            else if (word.length() >= 10) ++tenAndMore;
            else ++rest;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(six + " words with length = 6.\n"
                + rest + " words with length between 7-9.\n"
                + tenAndMore + " words with length over 10.\n");

        alert.show();
    }

    public static void exit() {
        Platform.exit();
        System.exit(0);
    }
}