package edu.Loopi.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AIService {
    // Replace with your Groq key (gsk_...)
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public String getRecyclingImpact(String material, double amount, String unit) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String prompt = String.format("Donne moi une seule phrase courte et encourageante sur l'impact écologique de recycler %.1f %s de %s.", amount, unit, material);

            // UPDATED MODEL: Using llama-3.3-70b-versatile (Active in 2026)
            String jsonBody = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            if (body.contains("\"content\":\"")) {
                int start = body.indexOf("\"content\":\"") + 11;
                int end = body.indexOf("\"", start);
                String result = body.substring(start, end);

                // Cleaning up common JSON encoded French characters
                return result.replace("\\n", " ")
                        .replace("\\u2019", "'")
                        .replace("\\u00e9", "é")
                        .replace("\\u00e8", "è")
                        .replace("\\u00e0", "à")
                        .replace("\\u00f4", "ô");
            } else {
                System.out.println("Error from Groq: " + body);
                return "L'IA est en pause café... Réessayez !";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de connexion.";
        }
    }
}