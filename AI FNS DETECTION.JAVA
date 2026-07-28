import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeNewsDetector extends JFrame {

    private JTextArea newsArea;
    private JLabel scoreLabel;
    private JLabel predictionLabel;

    private static final Set<String> FAKE_WORDS = Set.of(
            "shocking", "miracle", "secret", "viral",
            "unbelievable", "click here", "100% true",
            "share immediately", "rumor", "fake", "exposed"
    );

    private static final Set<String> TRUSTED_SOURCES = Set.of(
            "bbc", "reuters", "ap", "the hindu",
            "times of india", "ndtv"
    );

    static class DetectionResult {
        int score;
        String status;

        DetectionResult(int score, String status) {
            this.score = score;
            this.status = status;
        }
    }

    public FakeNewsDetector() {

        setTitle("Fake News Detector");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font = new Font("Arial", Font.PLAIN, 16);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel heading = new JLabel("Fake News Detector", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(heading, BorderLayout.NORTH);

        newsArea = new JTextArea();
        newsArea.setFont(font);
        newsArea.setLineWrap(true);
        newsArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(newsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        JButton detectButton = new JButton("Detect News");
        JButton clearButton = new JButton("Clear");

        detectButton.setFont(font);
        clearButton.setFont(font);

        scoreLabel = new JLabel("Confidence Score : ");
        predictionLabel = new JLabel("Prediction : ");

        scoreLabel.setFont(font);
        predictionLabel.setFont(font);

        detectButton.addActionListener(e -> detectNews());

        clearButton.addActionListener(e -> {
            newsArea.setText("");
            scoreLabel.setText("Confidence Score : ");
            predictionLabel.setText("Prediction : ");
            predictionLabel.setForeground(Color.BLACK);
        });

        bottomPanel.add(detectButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(scoreLabel);
        bottomPanel.add(predictionLabel);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void detectNews() {

        String news = newsArea.getText().trim();

        if (news.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter some news.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DetectionResult result = analyze(news);

        scoreLabel.setText("Confidence Score : " + result.score + "%");
        predictionLabel.setText("Prediction : " + result.status);

        if (result.score >= 70) {
            predictionLabel.setForeground(Color.RED);
        } else if (result.score >= 40) {
            predictionLabel.setForeground(Color.ORANGE);
        } else {
            predictionLabel.setForeground(new Color(0, 128, 0));
        }
    }

    public static DetectionResult analyze(String news) {

        String text = news.toLowerCase();
        int score = 0;

        for (String word : FAKE_WORDS) {
            if (text.contains(word)) {
                score += 10;
            }
        }

        int capitals = 0;
        for (char c : news.toCharArray()) {
            if (Character.isUpperCase(c)) {
                capitals++;
            }
        }

        if ((double) capitals / news.length() > 0.30) {
            score += 15;
        }

        long exclamationCount = news.chars()
                .filter(ch -> ch == '!')
                .count();

        if (exclamationCount >= 3) {
            score += 15;
        }

        Pattern pattern = Pattern.compile("(http|https)://\\S+");
        Matcher matcher = pattern.matcher(news);

        if (matcher.find()) {
            score += 10;
        }

        for (String source : TRUSTED_SOURCES) {
            if (text.contains(source)) {
                score -= 20;
            }
        }

        score = Math.max(0, Math.min(score, 100));

        String status;

        if (score >= 70) {
            status = "HIGH RISK (Likely Fake)";
        } else if (score >= 40) {
            status = "MEDIUM RISK";
        } else {
            status = "LOW RISK (Likely Real)";
        }

        return new DetectionResult(score, status);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new FakeNewsDetector().setVisible(true);
        });

    }
}
