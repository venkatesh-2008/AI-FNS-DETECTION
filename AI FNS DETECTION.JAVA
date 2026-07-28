import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {

    static String API_KEY = "fac691bdd2644c7d8a70275c615329b4";

    static String[] trustedSources = {
            "bbc-news",
            "reuters",
            "associated-press",
            "cnn",
            "the-hindu",
            "the-times-of-india"
    };

    public static void main(String[] args) throws Exception {

        java.util.Scanner sc = new java.util.Scanner(System.in);

        System.out.println("========== AI NEWS VERIFIER ==========");

        System.out.print("Enter News Headline: ");

        String headline = sc.nextLine();

        verify(headline);

        sc.close();
    }

    public static void verify(String headline) throws Exception {

        String url =
                "https://newsapi.org/v2/everything?q="
                        + URLEncoder.encode(headline, StandardCharsets.UTF_8)
                        + "&language=en&pageSize=10&apiKey=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

        HttpResponse<String> response =
                client.send(request,
                        HttpResponse.BodyHandlers.ofString());

        JsonObject json =
                JsonParser.parseString(response.body()).getAsJsonObject();

        JsonArray articles = json.getAsJsonArray("articles");

        boolean trusted = false;

        System.out.println("\nTrusted Sources Found:");

        for (int i = 0; i < articles.size(); i++) {

            JsonObject article = articles.get(i).getAsJsonObject();

            JsonObject source =
                    article.getAsJsonObject("source");

            String sourceId =
                    source.get("id").isJsonNull()
                            ? ""
                            : source.get("id").getAsString();

            String sourceName =
                    source.get("name").getAsString();

            String title =
                    article.get("title").getAsString();

            for (String t : trustedSources) {

                if (t.equalsIgnoreCase(sourceId)) {

                    trusted = true;

                    System.out.println("--------------------------------");
                    System.out.println(sourceName);
                    System.out.println(title);
                }
            }

        }

        System.out.println("\n==============================");

        if (trusted)
            System.out.println("RESULT : TRUE NEWS");
        else
            System.out.println("RESULT : UNVERIFIED");

    }
}