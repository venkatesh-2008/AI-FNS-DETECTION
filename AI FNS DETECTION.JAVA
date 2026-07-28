import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main extends JFrame {

    // Replace with your Gemini API Key
    private static final String API_KEY = "AQ.Ab8RN6JhYy9v5lyYSu-EPkSOvAajNvItiOd9DalYcevvxK_oAQ";

    private JTextField headlineField;
    private JTextArea outputArea;

    public Main() {

        setTitle("AI Fake News Detector (Gemini)");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel label = new JLabel("Enter News:");

        headlineField = new JTextField();

        JButton verifyButton = new JButton("Verify");

        JPanel top = new JPanel(new BorderLayout(10,10));
        top.add(label, BorderLayout.NORTH);
        top.add(headlineField, BorderLayout.CENTER);
        top.add(verifyButton, BorderLayout.EAST);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 15));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        verifyButton.addActionListener(e -> {

            String news = headlineField.getText().trim();

            if(news.isEmpty()){
                JOptionPane.showMessageDialog(this,"Enter news.");
                return;
            }

            outputArea.setText("Checking with Gemini...");

            new Thread(() -> {

                try{
                    String result = verifyNews(news);

                    SwingUtilities.invokeLater(() ->
                            outputArea.setText(result));

                }catch(Exception ex){

                    SwingUtilities.invokeLater(() ->
                            outputArea.setText(ex.getMessage()));

                }

            }).start();

        });

    }

    public String verifyNews(String news) throws Exception {

        String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                        + API_KEY;

        String prompt =
                "You are a fake news detector.\n"
                + "Analyze the following news.\n"
                + "Reply ONLY in this format:\n\n"
                + "Result: TRUE or FAKE\n"
                + "Reason: one short sentence\n\n"
                + "News:\n"
                + news;

        JsonObject body = new JsonObject();

        JsonArray contents = new JsonArray();

        JsonObject content = new JsonObject();

        JsonArray parts = new JsonArray();

        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);

        parts.add(part);

        content.add("parts", parts);

        contents.add(content);

        body.add("contents", contents);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type","application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response =
                client.send(request,
                        HttpResponse.BodyHandlers.ofString());

        JsonObject json =
                JsonParser.parseString(response.body()).getAsJsonObject();

        JsonArray candidates =
                json.getAsJsonArray("candidates");

        if(candidates==null)
            return response.body();

        JsonObject first =
                candidates.get(0).getAsJsonObject();

        JsonObject contentObj =
                first.getAsJsonObject("content");

        JsonArray partsArray =
                contentObj.getAsJsonArray("parts");

        return partsArray.get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString();

    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new Main().setVisible(true);

        });

    }

}
