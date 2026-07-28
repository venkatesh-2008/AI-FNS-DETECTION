import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main extends JFrame {

    // Replace with your NewsAPI Key
    static String API_KEY = "YOUR_API_KEY";

    static String[] trustedSources = {
            "bbc-news",
            "reuters",
            "associated-press",
            "cnn",
            "the-hindu",
            "the-times-of-india"
    };

    JTextField headlineField;
    JButton verifyButton;
    JTextArea outputArea;

    public Main() {

        setTitle("AI News Verifier");
        setSize(700, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new BorderLayout(10,10));

        JLabel label = new JLabel("Enter News Headline:");

        headlineField = new JTextField();

        verifyButton = new JButton("Verify News");

        topPanel.add(label, BorderLayout.NORTH);
        topPanel.add(headlineField, BorderLayout.CENTER);
        topPanel.add(verifyButton, BorderLayout.EAST);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        verifyButton.addActionListener(e -> {

            String headline = headlineField.getText().trim();

            if(headline.isEmpty()){
                JOptionPane.showMessageDialog(this,
                        "Please enter a news headline.");
                return;
            }

            outputArea.setText("Checking...\n");

            new Thread(() -> {

                try {
                    verify(headline);
                } catch (Exception ex) {

                    SwingUtilities.invokeLater(() ->
                            outputArea.setText(
                                    "Error:\n" + ex.getMessage()));

                }

            }).start();

        });

    }

    public void verify(String headline) throws Exception {

        String url =
                "https://newsapi.org/v2/everything?q="
                        + URLEncoder.encode(headline,
                        StandardCharsets.UTF_8)
                        + "&language=en&pageSize=10&apiKey="
                        + API_KEY;

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
                JsonParser.parseString(response.body())
                        .getAsJsonObject();

        JsonArray articles = json.getAsJsonArray("articles");

        StringBuilder result = new StringBuilder();

        boolean trusted = false;

        result.append("Trusted Sources Found\n");
        result.append("=========================\n\n");

        if (articles != null) {

            for (int i = 0; i < articles.size(); i++) {

                JsonObject article =
                        articles.get(i).getAsJsonObject();

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

                        result.append("---------------------------------\n");
                        result.append("Source : ")
                                .append(sourceName)
                                .append("\n");

                        result.append("Title  : ")
                                .append(title)
                                .append("\n\n");

                    }

                }

            }

        }

        result.append("\n===========================\n");

        if (trusted)
            result.append("RESULT : TRUE NEWS");
        else
            result.append("RESULT : UNVERIFIED");

        SwingUtilities.invokeLater(() ->
                outputArea.setText(result.toString()));

    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new Main().setVisible(true);

        });

    }

}
