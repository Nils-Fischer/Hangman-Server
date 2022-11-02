import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import static spark.Spark.*;

public class Server {

    private static final Map<String, Hangman> games = new HashMap<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {


        post("/new-game", (request, response) -> {
            String word = request.headers("word");

            if (word == null) {
                response.status(401);
                return gson.toJson(new StandardResponse("fail", "no word was entered"));
            }

            Pattern p = Pattern.compile("([a-zA-Z]+[-\\s]?[a-zA-Z]+)+");
            if (!p.matcher(word).matches()) {
                response.status(401);
                return gson.toJson(new StandardResponse("fail", "invalid word"));
            }

            String gameCode = Integer.toString(new Random().nextInt(10000));
            games.put(gameCode, new Hangman(word));

            JsonObject json = new JsonObject();
            json.addProperty("gameCode", gameCode);

            response.status(201);
            return gson.toJsonTree(new StandardResponse("success", json));
        });

        get("/status", (request, response) -> {
            String gameCode = request.headers("gameCode");

            if (!games.containsKey(gameCode)) {
                response.status(401);
                return gson.toJsonTree(new StandardResponse("fail", "Game doesn't exist, or has already concluded"));
            }

            return gson.toJsonTree(new StandardResponse("success", gson.toJsonTree(games.get(gameCode))));
        });

        post("/guess", (request, response) -> {
            String gameCode = request.headers("gameCode");
            String guess = request.headers("guess");
            String turn = request.headers("turn");

            if (!games.containsKey(gameCode)) {
                response.status(401);
                return gson.toJsonTree(new StandardResponse("fail", "Game doesn't exist"));
            }
            Hangman game = games.get(gameCode);

            if (turn == null || !turn.equals(Integer.toString(game.getTurn()))) {
                response.status(401);
                return gson.toJsonTree(new StandardResponse("fail", "Your game status is outdated"));
            }

            Pattern p1 = Pattern.compile("[a-zA-Z]");
            Pattern p2 = Pattern.compile("([a-zA-Z]+[-\\s]?[a-zA-Z]+)+");

            if (!p1.matcher(guess).matches() && !p2.matcher(guess).matches()) {
                response.status(401);
                return gson.toJsonTree(new StandardResponse("fail", "invalid guess"));
            }

            String message = game.guess(guess);
            if (message.equals("Game Over") || message.equals("solved")) games.remove(gameCode);
            if (message.equals("Game Over")) message +=
                    "\n__________\n|/       |\n|        |\n|        o\n|       \\|/\n/\\      / \\";
            return gson.toJsonTree(new StandardResponse("success", message, gson.toJsonTree(game)));
        });

    }
}

