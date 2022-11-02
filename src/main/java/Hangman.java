import java.util.ArrayList;
import java.util.List;

public class Hangman {
    private int turn;
    private final String word;
    private String uWord;
    private String drawing;
    private int stage;
    private final List<String> wrongGuesses;

    public int getTurn() {
        return turn;
    }

    public Hangman(String word) {
        this.turn = 0;
        this.stage = 12;
        this.word = word.toUpperCase();
        this.uWord = word.replaceAll("[a-zA-Z]", "_"); // create a new word with only underscores: "____"
        this.drawing = "";
        this.wrongGuesses = new ArrayList<>();
    }

    public String guess(String guess) {
        guess = guess.toUpperCase();
        turn++;
        if (guess.length() != 1) {
            if (guess.equals(word)) {
                uWord = guess;
                return "solved";
            }
            advanceHangman();
            if (stage == 0) return "game Over";
            return "false guess";
        }
        char g = guess.charAt(0);
        if (word.contains(guess)) {
            StringBuilder sb = new StringBuilder(this.uWord);

            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == g) sb.setCharAt(i, g);
            }

            this.uWord = sb.toString();
            if (uWord.equals(word)) return "solved";
            return "correct guess";
        }
        if (!wrongGuesses.contains(guess)) wrongGuesses.add(guess);
        advanceHangman();
        if (stage == 0) return "Game Over";
        return "false guess";
    }

    public void advanceHangman() {
        switch (--stage) {
            case 11 -> drawing = "\n/\\";
            case 10 -> drawing = "\n|\n|\n|\n|\n/\\";
            case 9 -> drawing = "\n__________\n|\n|\n|\n|\n/\\";
            case 8 -> drawing = "\n__________\n|/\n|\n|\n|\n/\\";
            case 6 -> drawing = "\n__________\n|/       |\n|        |\n|\n|\n/\\";
            case 5 -> drawing = "\n__________\n|/       |\n|        |\n|        o\n|\n/\\";
            case 4 -> drawing = "\n__________\n|/       |\n|        |\n|        o\n|        |\n/\\";
            case 3 -> drawing = "\n__________\n|/       |\n|        |\n|        o\n|       \\|\n/\\";
            case 2 -> drawing = "\n__________\n|/       |\n|        |\n|        o\n|       \\|/\n/\\";
            case 1 -> drawing = "\n__________\n|/       |\n|        |\n|        o\n|       \\|/\n/\\      /";
            case 0 -> drawing = "\n__________\n|/       |\n|        |\n|        o\n|       \\|/\n/\\      / \\";
        }
    }
}
