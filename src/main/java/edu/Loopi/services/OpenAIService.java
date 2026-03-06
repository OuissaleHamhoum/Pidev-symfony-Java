package edu.Loopi.services;

import com.google.gson.*;
import edu.Loopi.config.OpenAIConfig;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAIService {
    private static final Gson gson = new Gson();

    private String callOpenAI(String prompt, int maxTokens, double temperature) {
        try {
            URL url = new URL(OpenAIConfig.API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + OpenAIConfig.API_KEY);
            conn.setDoOutput(true);

            // Création du body JSON
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", OpenAIConfig.MODEL);
            requestBody.addProperty("max_tokens", maxTokens);
            requestBody.addProperty("temperature", temperature);

            JsonArray messages = new JsonArray();

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "Vous êtes un assistant spécialisé dans l'art recyclé et l'éco-design.");
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);

            requestBody.add("messages", messages);

            // Envoi de la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lecture de la réponse
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
            } else {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder error = new StringBuilder();
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    error.append(errorLine.trim());
                }
                System.err.println("❌ Erreur API OpenAI: " + error.toString());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Exception OpenAI: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ==================== FONCTIONNALITÉS PRODUIT ====================

    /**
     * API 1: Conseils pour le choix d'image
     */
    public String getImageAdvice(String productName, String category, String description) {
        String prompt = String.format(
                "En tant qu'expert en design et art recyclé, donnez des conseils pour choisir une image " +
                        "pour un produit avec les détails suivants:\n" +
                        "Nom du produit: %s\n" +
                        "Catégorie: %s\n" +
                        "Description: %s\n\n" +
                        "Fournissez 3-5 conseils spécifiques sur le type d'image à utiliser, les couleurs, " +
                        "le style, et comment mettre en valeur ce produit. Répondez en français de manière concise.",
                productName, category, description
        );
        return callOpenAI(prompt, 300, 0.7);
    }

    /**
     * API 2: Suggestion de description automatique
     */
    public String generateProductDescription(String productName, String category, String keywords) {
        String prompt = String.format(
                "Générez une description attrayante et professionnelle pour un produit d'art recyclé.\n" +
                        "Nom du produit: %s\n" +
                        "Catégorie: %s\n" +
                        "Mots-clés/contexte: %s\n\n" +
                        "La description doit être en français, faire 3-4 phrases, mettre en avant les aspects " +
                        "artistiques et écologiques, et être adaptée pour une boutique en ligne.",
                productName, category, keywords.isEmpty() ? "Aucun mot-clé fourni" : keywords
        );
        return callOpenAI(prompt, 250, 0.8);
    }

    // ==================== FONCTIONNALITÉS FEEDBACK ====================

    /**
     * API 3: Analyse de sentiment des commentaires
     */
    public SentimentAnalysis analyzeSentiment(String comment) {
        String prompt = String.format(
                "Analysez le sentiment du commentaire suivant concernant un produit d'art recyclé.\n" +
                        "Commentaire: \"%s\"\n\n" +
                        "Répondez UNIQUEMENT au format JSON avec les champs:\n" +
                        "- sentiment: (positif, neutre, negatif)\n" +
                        "- score: (nombre entre 0 et 1)\n" +
                        "- mots_cles: (liste des mots clés qui ont influencé l'analyse)\n" +
                        "- explication: (courte explication en français)",
                comment
        );

        String response = callOpenAI(prompt, 200, 0.3);
        if (response != null) {
            try {
                // Essayer d'extraire le JSON de la réponse
                String jsonStr = extractJsonFromResponse(response);
                JsonObject json = gson.fromJson(jsonStr, JsonObject.class);

                SentimentAnalysis analysis = new SentimentAnalysis();
                analysis.setSentiment(json.get("sentiment").getAsString());
                analysis.setScore(json.get("score").getAsDouble());

                if (json.has("mots_cles")) {
                    JsonArray motsCles = json.getAsJsonArray("mots_cles");
                    for (int i = 0; i < motsCles.size(); i++) {
                        analysis.addMotCle(motsCles.get(i).getAsString());
                    }
                }

                analysis.setExplanation(json.get("explication").getAsString());
                return analysis;
            } catch (Exception e) {
                // Fallback: analyse simple basée sur des mots-clés
                return fallbackSentimentAnalysis(comment);
            }
        }
        return fallbackSentimentAnalysis(comment);
    }

    /**
     * API 4: Chatbot sur les avantages du produit
     */
    public String askProductBenefits(String productName, String category, String userQuestion) {
        String prompt = String.format(
                "Vous êtes un assistant virtuel spécialisé dans les produits d'art recyclé de LOOPI. " +
                        "Un client vous pose une question sur le produit suivant:\n" +
                        "Produit: %s\n" +
                        "Catégorie: %s\n" +
                        "Question du client: %s\n\n" +
                        "Répondez de façon chaleureuse et informative en français, en mettant en avant " +
                        "les avantages écologiques et artistiques. Restez concis (maximum 4 phrases).",
                productName, category, userQuestion
        );
        return callOpenAI(prompt, 200, 0.7);
    }

    // ==================== UTILITAIRES ====================

    private String extractJsonFromResponse(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}') + 1;
        if (start >= 0 && end > start) {
            return response.substring(start, end);
        }
        return "{\"sentiment\":\"neutre\",\"score\":0.5,\"mots_cles\":[],\"explication\":\"Analyse automatique\"}";
    }

    private SentimentAnalysis fallbackSentimentAnalysis(String comment) {
        SentimentAnalysis analysis = new SentimentAnalysis();
        comment = comment.toLowerCase();

        // Mots-clés positifs
        String[] positif = {"beau", "magnifique", "super", "excellent", "génial", "parfait",
                "adoré", "aime", "incroyable", "superbe", "merveilleux", "bravo"};

        // Mots-clés négatifs
        String[] negatif = {"mauvais", "déçu", "décevant", "horrible", "terrible", "problème",
                "cassé", "abîmé", "cher", "délai", "lent", "dommage"};

        int positifCount = 0;
        int negatifCount = 0;

        for (String mot : positif) {
            if (comment.contains(mot)) positifCount++;
        }

        for (String mot : negatif) {
            if (comment.contains(mot)) negatifCount++;
        }

        if (positifCount > negatifCount) {
            analysis.setSentiment("positif");
            analysis.setScore(0.5 + (positifCount * 0.1));
        } else if (negatifCount > positifCount) {
            analysis.setSentiment("negatif");
            analysis.setScore(0.5 - (negatifCount * 0.1));
        } else {
            analysis.setSentiment("neutre");
            analysis.setScore(0.5);
        }

        analysis.setExplanation("Analyse basée sur les mots-clés détectés");
        return analysis;
    }

    // Classe interne pour l'analyse de sentiment
    public static class SentimentAnalysis {
        private String sentiment;
        private double score;
        private java.util.List<String> motsCles = new java.util.ArrayList<>();
        private String explanation;

        // Getters et setters
        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public java.util.List<String> getMotsCles() { return motsCles; }
        public void addMotCle(String mot) { this.motsCles.add(mot); }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }

        public String getSentimentEmoji() {
            switch (sentiment) {
                case "positif": return "😊";
                case "negatif": return "😞";
                default: return "😐";
            }
        }
    }

    // Ajoutez cette méthode de test dans OpenAIService.java
    public boolean testApiConnection() {
        try {
            String testPrompt = "Réponds simplement 'OK' si tu reçois ce message.";
            String response = callOpenAI(testPrompt, 10, 0.1);
            System.out.println("✅ Test API réussi: " + response);
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            System.err.println("❌ Test API échoué: " + e.getMessage());
            return false;
        }
    }

}