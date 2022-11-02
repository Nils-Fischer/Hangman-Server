import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Client {

    private static String gameCode;
    private static String turn;
    private static final Gson gson = new Gson();
    private static boolean inGame = false;
    private static boolean active = true;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        while (active) {
            cli(sc.nextLine().strip());
        }
        sc.close();
    }

    public static void cli(String input) throws URISyntaxException, IOException, InterruptedException {
        if (input == null) {
            System.out.println("invalid input");
            return;
        }
        String[] command = input.split(" ", 2);
        switch (command[0]) {
            case "newGame" -> {
                if (command.length == 1 || command[1].equals("")) {
                    System.out.println("no Parameter was given\n" +
                            "type help for more information");
                    return;
                }
                newGame(command[1]);
            }
            case "joinGame" -> {
                if (command.length == 1 || command[1].equals("")) {
                    System.out.println("no Parameter was given\n" +
                            "type help for more information");
                    return;
                }
                joinGame(command[1]);
            }
            case "guess" -> {
                if (command.length == 1 || command[1].equals("")) {
                    System.out.println("no Parameter was given\n" +
                            "type help for more information");
                    return;
                }
                guess(command[1]);
            }
            case "showBoard" -> {
                if (gameCode == null) {
                    System.out.println("You have not joined a Game");
                    return;
                }
                if (command.length > 1) {
                    System.out.println("showBoard requires no parameter\n" +
                            "type help for more information");
                    return;
                }
                showBoard();
            }
            case "help" -> System.out.println("""
                    newGame [word]:             start a new game with the specified word
                    joinGame [gameCode]:        join an already existing game
                    guess [letter/solution]:    make a guess for a letter or the solution
                    shoBoard:                   show the state of the board
                    exit:                       exit the game""");
            case "exit" -> active = false;
            default -> System.out.println(command[0] + " is undefined\n" +
                    "type help for more information");
        }
    }

    public static void newGame(String word) throws URISyntaxException, IOException, InterruptedException {
        URI url = new URI("http://localhost:4567/new-game");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("word", word)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map jMap = gson.fromJson(response.body(), Map.class);
        String status = (String) jMap.get("status");
        turn = "0";

        StringBuilder sb = new StringBuilder();
        sb.append("status: ").append(status);
        if (jMap.containsKey("message"))
            sb.append("\nmessage: ").append((String) jMap.get("message")); //falls message übergeben wurde
        if (jMap.containsKey("data"))
            gameCode = (String) ((Map) jMap.get("data")).get("gameCode");
        sb.append("\ngameCode: ").append(gameCode);

        inGame = true;
        System.out.println(sb);
        update();
    }

    public static void joinGame(String gc) throws URISyntaxException, IOException, InterruptedException {
        gameCode = gc;
        inGame = true;
        showBoard();
        update();
    }

    public static void guess(String guess) throws URISyntaxException, IOException, InterruptedException {
        if (gameCode == null) {
            System.out.println("you have not joined a Game");
            return;
        }
        URI url = new URI("http://localhost:4567/guess");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("gameCode", gameCode)
                .header("guess", guess)
                .header("turn", turn)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map jMap = gson.fromJson(response.body(), Map.class);
        String message = (String) jMap.get("message");

        System.out.println("" + "message: " + message);
        showBoard();
    }

    public static void showBoard() throws URISyntaxException, IOException, InterruptedException {
        if (gameCode == null) {
            System.out.println("you have not joined a Game");
            return;
        }
        URI url = new URI("http://localhost:4567/status");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("gameCode", gameCode)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map jMap = gson.fromJson(response.body(), Map.class);

        StringBuilder sb = new StringBuilder();

        if (jMap.containsKey("data")) {
            sb.append("-----status-----\n");
            sb.append("word: ").append((String) ((Map) jMap.get("data")).get("uWord")).append("\n");
            sb.append((String) ((Map) jMap.get("data")).get("drawing"));
            String guesses = String.join(", ", (List) ((Map) jMap.get("data")).get("wrongGuesses"));
            if (!guesses.equals("")) sb.append("\nwrong guesses: ").append(guesses).append("\n");
            turn = Integer.toString(((Double) ((Map) jMap.get("data")).get("turn")).intValue());
            System.out.println(sb);
        }
    }

    private static void poll() throws URISyntaxException, IOException, InterruptedException {
        if (gameCode == null) return;

        URI url = new URI("http://localhost:4567/status");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("gameCode", gameCode)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map jMap = gson.fromJson(response.body(), Map.class);
        if (!jMap.containsKey("data")) {
            inGame = false;
            return;
        }

        String remoteTurn = Integer.toString(((Double) ((Map) jMap.get("data")).get("turn")).intValue());
        if (remoteTurn.equals(turn)) return;
        showBoard();
    }

    private static void update() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!inGame) cancel();
                try {
                    poll();
                } catch (URISyntaxException | InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 2000, 2000);
    }
}

