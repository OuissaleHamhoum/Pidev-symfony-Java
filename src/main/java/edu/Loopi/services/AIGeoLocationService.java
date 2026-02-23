package edu.Loopi.services.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.Loopi.entities.Event;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class AIGeoLocationService {
    private static final String CONFIG_FILE = "src/main/resources/config/api.properties";
    private String apiKey;
    private String apiUrl;
    private boolean useDemoMode = false;

    public AIGeoLocationService() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);

        try {
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    properties.load(input);
                }

                this.apiKey = properties.getProperty("stability.api.key");
                this.apiUrl = properties.getProperty("stability.api.url");

                if (apiKey == null || apiKey.isEmpty() || apiKey.equals("votre_cle_api_stability_ai")) {
                    System.out.println("⚠️ Mode démo - Clé API Stability AI non configurée");
                    this.useDemoMode = true;
                } else {
                    System.out.println("✅ API Stability AI configurée avec la clé: " + apiKey.substring(0, 10) + "...");
                }
            } else {
                System.err.println("❌ Fichier de configuration non trouvé: " + CONFIG_FILE);
                this.useDemoMode = true;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement configuration: " + e.getMessage());
            this.useDemoMode = true;
        }
    }

    public CompletableFuture<String> generateEnhancedEventImage(Event event) {
        return CompletableFuture.supplyAsync(() -> {
            if (useDemoMode) {
                return generateDemoImage(event);
            }

            try {
                String prompt = buildImagePrompt(event);
                System.out.println("🎨 Génération d'image avec Stability AI pour: " + event.getTitre());

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(90000);
                connection.setDoOutput(true);

                JsonObject requestBody = new JsonObject();
                JsonObject textPrompt = new JsonObject();
                textPrompt.addProperty("text", prompt);

                JsonArray textPrompts = new JsonArray();
                textPrompts.add(textPrompt);

                requestBody.add("text_prompts", textPrompts);
                requestBody.addProperty("cfg_scale", 7);
                requestBody.addProperty("height", 1024);
                requestBody.addProperty("width", 1024);
                requestBody.addProperty("samples", 1);
                requestBody.addProperty("steps", 50);

                String jsonBody = requestBody.toString();

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        return extractImageFromResponse(response.toString());
                    }
                } else {
                    System.err.println("❌ Erreur API: " + responseCode);
                    return generateDemoImage(event);
                }

            } catch (Exception e) {
                System.err.println("❌ Erreur génération image: " + e.getMessage());
                return generateDemoImage(event);
            }
        });
    }

    private String buildImagePrompt(Event event) {
        return String.format("Professional event photography, %s at %s, atmospheric lighting, high quality, 4k, detailed",
                event.getTitre(),
                event.getLieu());
    }

    private String generateDemoImage(Event event) {
        // Images de démo de haute qualité
        String[] demoImages = {
                "https://images.unsplash.com/photo-1531058020387-3be344556be6?w=600&auto=format",
                "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600&auto=format",
                "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600&auto=format",
                "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=600&auto=format",
                "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format"
        };

        int index = Math.abs(event.hashCode()) % demoImages.length;
        return demoImages[index];
    }

    private String extractImageFromResponse(String jsonResponse) {
        try {
            JsonObject response = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray artifacts = response.getAsJsonArray("artifacts");

            if (artifacts != null && artifacts.size() > 0) {
                String base64Image = artifacts.get(0).getAsJsonObject().get("base64").getAsString();
                return "data:image/png;base64," + base64Image;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur extraction image: " + e.getMessage());
        }
        return null;
    }

    public boolean isConfigured() {
        return !useDemoMode;
    }
}