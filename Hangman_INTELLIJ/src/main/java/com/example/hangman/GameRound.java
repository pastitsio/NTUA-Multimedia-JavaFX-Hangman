package com.example.hangman;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Main class of the actual game. Stores info about round's lives,
 * successful attempts, points. Every new round, a new such object is
 * created.
 */
public class GameRound {
    private final Dict dict;

    private int lives;
    private int points;
    private int success;

    private final char[] activeWord;  // updated with each choice.
    private final String gameWord;    // immutable.

    private final ArrayList<String> activeWordList;
    private HashMap<Integer, HashMap<Character, Double>> probabilitiesPerPos;

    /**
     * Inits fields.
     *
     * @param inDict Contains wordList and bookID. gameWord is randomly selected from
     *               inDict.
     */
    public GameRound(Dict inDict) {
        this.dict = inDict;

        this.lives = 6;
        this.points = 0;
        this.success = 0;

        final ArrayList<String> wordList = this.dict.getWordList();

        // choose random word.
        Random rand = new Random();
        gameWord = wordList.get(rand.nextInt(wordList.size()));
        GLOBALS.LOG("Round's word: "  + gameWord + ", Length: " + gameWord.length());

        // build activeWordList.
        this.activeWordList = new ArrayList<>();
        for (String word : wordList)
            if (word.length() == this.gameWord.length())
                activeWordList.add(word);

        this.activeWord = new char[gameWord.length()];
        for (int i = 0; i < gameWord.length(); ++i)
            this.activeWord[i] = '_';

        this.calcProbabilities();
    }

    /**
     * Getter method for dict.
     * @return Round's selected dict.
     */
    public Dict getDict()  { return dict; }

    /**
     * Getter method for active word-list as a string.
     * @return activeWordList.toString().
     */
    public String getActiveWordListString() { return "ActiveWordList = " + activeWordList + "\n"; }

    /**
     * Getter method for activeWordList's size.
     * @return Active word-list's size.
     */
    public int getActiveWordListCount() { return activeWordList.size(); }

    /**
     * Getter method for activeWord.
     * @return Round's word updated with choices.
     */
    public char[] getActiveWord() { return activeWord; }

    /**
     * Getter method for lives.
     * @return Round's lives.
     */
    public int getLives() { return lives; }

    /**
     * Getter method for points.
     * @return Round's points.
     */
    public int getPoints() { return points; }

    /**
     * Getter method for successful attempts.
     * @return successful attempts
     */
    public int getSuccess() { return success; }

    /**
     * Getter method for gameWord.
     * @return chosen word for round.
     */
    public String getGameWord() { return gameWord; }

    /**
     * Getter method for probabilities per position. Updated every round.
     * @return letter probabilites per position, sorted descending.
     */
    public HashMap<Integer, HashMap<Character, Double>> getProbabilitiesPerPos() {
        return this.probabilitiesPerPos;
    }

    /**
     * Getter method for active dictionary's word-list. Immutable in round's context.
     * @return Chosen dictionary's word-list.
     */
    public ArrayList<String> getWordList() { return dict.getWordList(); }

    private void updateActiveWordList(boolean correct, char letter, int pos) {
        // updates based on letter choice.
        Iterator<String> itr = activeWordList.iterator();

        while (itr.hasNext()) {
            String word = itr.next();
            if (correct) {
                if (word.charAt(pos) == letter) continue;
            } else {
                if (word.charAt(pos) != letter) continue;
            }
            itr.remove();
        }

        GLOBALS.LOG(activeWordList.toString());
    }

    private void calcProbabilities() {
        this.probabilitiesPerPos = new HashMap<>();
        for (int pos = 0; pos < gameWord.length(); ++pos) {

            HashMap<Character, Double> letterProbs = new HashMap<>();
            if (this.activeWord[pos] == '_') {
                // if letter not found in current pos,
                // construct hash (k: letter, v: frequency/len) to compute probs.
                Double baseProb = 1.0 / activeWordList.size();

                for (String word : activeWordList) {
                    Character key = word.charAt(pos);
                    letterProbs.put(key, letterProbs.getOrDefault(key, 0.0) + baseProb);
                }

                // sort by value descending.
                letterProbs = letterProbs.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(
                                toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                        LinkedHashMap::new)
                        );


            } else {
                // activeWord[pos] already guessed correctly.
                letterProbs.put(this.activeWord[pos], 1.0);
            }
            this.probabilitiesPerPos.put(pos, letterProbs);
        }
//        GLOBALS.LOG(this.probabilitiesPerPos.toString());
    }

    /**
     * Player inputs letter and position. Lives, points, successful attempts,
     * active word list and letter probabilities per position are updated.
     *
     * @param letter player's letter choice.
     * @param pos player's position choice.
     */
    public void play(char letter, int pos) {
        GLOBALS.LOG(letter + String.valueOf(pos));

        if (gameWord.charAt(pos) == letter) {
            this.points += 5; // at least 5 points
            double prob = probabilitiesPerPos.get(pos).get(letter);
            if (prob < .25) this.points += 25;
            else if (prob < .40) this.points += 10;
            else if (prob < .60) this.points +=  5;

            this.activeWord[pos] = letter; // update word with letter choice

            updateActiveWordList(true, letter, pos);
            ++this.success;
        } else {
            updateActiveWordList(false, letter, pos);
            --this.lives;
        }
        this.calcProbabilities();
    }

    /**
     * @return game is over and won.
     */
    public boolean isWon() {
        return (gameWord.equals(String.valueOf(activeWord)));
    }

    /**
     * @return game is over and lost.
     */
    public boolean isOver() {
        return lives == 0;
    }
}
