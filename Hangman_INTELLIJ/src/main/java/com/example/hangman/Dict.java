package com.example.hangman;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.*;
import java.io.*;



public class Dict {
    private ArrayList<String> wordList;
    private String ID;

    public static class UndersizedException extends Exception {
        public UndersizedException(String errorMessage) {
            super(errorMessage);
        }
    }
    public static class UnbalancedException extends Exception {
        public UnbalancedException(String errorMessage) {
            super(errorMessage);
        }
    }
    public static class NoDescriptionException extends Exception {
        public NoDescriptionException(String errorMessage) {
            super(errorMessage);
        }

    }

    private ArrayList<String> fetchNewDict(String OLBookID) throws Exception     {

        String desc;
        GLOBALS.LOG("\n\nAPI-CALL for book with ID = " + OLBookID);

        try {
            URL url = new URL(String.format("https://openlibrary.org/works/%s.json", OLBookID));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            // Getting the response code
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                StringBuilder inline = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                // Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }

                // Close the scanner
                scanner.close();

                // Using the JSON simple library parse the string into a json object
                JSONObject json_results = (JSONObject) new JSONParser().parse(inline.toString());

                // Get the description
                desc = json_results.get("description").toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }

        if (desc == null) {
            throw new NoDescriptionException("'description' key not in JSON!\n");
        }
        desc = desc.toUpperCase();

        // Remove grammatical marks, trailing whitespaces and split to String []
        String[] splitDesc = Arrays.stream(
                desc.replaceAll("[^A-Z]+", " ").split(" ")).map(String::trim)
                .toArray(String[]::new);

        Set<String> set = new HashSet<>(Arrays.asList(splitDesc));

        // Remove length(word) < 6
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().length() < 6) {
                iterator.remove();
            }
        }

        // Size over 20 check
        if (set.size() < 20) {
            throw new UndersizedException("Contains less than 20!\n");
        }

        // 20% over 9 check
        int over9 = 0;
        iterator = set.iterator();
        while (iterator.hasNext()) {
         if (iterator.next().length() >= 9) {
             over9++;
         }
        }
        if ((float)over9/set.size() < 0.2) {
         throw new UnbalancedException("20% NOT over 9 letters!\n");
        }

        return new ArrayList<>(set);
    }

    private String getRandomWorkID() {

        Random rand = new Random();
        int ID = rand.nextInt(1000000 - 10000) + 10000 ;
        return "OL" + ID + "W";
    }
    // try with ID = "OL31390631M";

    private void saveToFolder() {
        saveToFolder(GLOBALS.folder);
    }
    private void saveToFolder(String folderName) {
        if (this.wordList != null) {
            File dir = new File(folderName);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

            try {
                File myObj = new File(folderName + "/hangman_" + this.ID + ".txt");
                if (myObj.createNewFile()) {
                    GLOBALS.LOG("File created: " + myObj.getName());
                } else {
                    GLOBALS.LOG("File already exists.");
                }
                FileWriter myWriter = new FileWriter(myObj);
                for (String word:this.wordList){
                    myWriter.write(word + "\n");
                }
                myWriter.close();
            } catch (IOException e) {
                GLOBALS.LOG("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> getNewRandomID() throws Exception {
        // Fetches new Dict from API, until all requirements are met.

        this.ID = getRandomWorkID();
        return fetchNewDict(this.ID);
    }

    public Dict() {
        this.wordList = null;
        this.ID = null;
    }
    // ONLY USE THESE.
    public Dict loadDictFromID(String bID) throws Exception {
        // Tries to fetch given ID, might have to retry.
        this.wordList = fetchNewDict(bID);
        this.ID = bID;
        this.saveToFolder();
        return this;
    }
    public Dict loadFromFile(String filename) {
        String path = GLOBALS.folder +"/"+ filename;
        File file = new File(path);
        GLOBALS.LOG("File selected: " + path);

        this.wordList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                this.wordList.add(line);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.ID = filename.split("_|\\.")[1];

        return this;
    }
    public Dict loadRandomFromFolder() {
        File dir = new File(GLOBALS.folder);
        File[] files = dir.listFiles();
        assert files != null;
        Random rand = new Random();
        File file = files[rand.nextInt(files.length)];
        return loadFromFile(file.getName());
    }

    public ArrayList<String> getWordList(){
        return this.wordList;
    }
    public String getID() { return this.ID; }

}

