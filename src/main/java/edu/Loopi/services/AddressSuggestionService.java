package edu.Loopi.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AddressSuggestionService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "LoopiApp/1.0 (contact@loopi.tn)";

    /**
     * Obtient des suggestions d'adresses
     */
    public List<String> getAddressSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();

        if (query == null || query.trim().length() < 3) {
            return suggestions;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String urlStr = NOMINATIM_URL + "?format=json&q=" + encodedQuery +
                    "&limit=5&addressdetails=1&accept-language=fr";

            // Ajouter le filtre pays seulement si pas de pays dans la requête
            if (!query.toLowerCase().contains("tunisie") &&
                    !query.toLowerCase().contains("tunisia") &&
                    !query.toLowerCase().contains("france")) {
                urlStr += "&countrycodes=tn";
            }

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "fr,fr-FR;q=0.9");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String displayName = obj.getString("display_name");
                    suggestions.add(displayName);
                }

                System.out.println("📋 " + suggestions.size() + " suggestions trouvées pour: " + query);
            }

            // Respecter la limite de requêtes
            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println("❌ Erreur suggestions: " + e.getMessage());
        }
        return suggestions;
    }

    /**
     * Géocode une adresse complète (même méthode que GeocodingService)
     */
    public double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String urlStr = NOMINATIM_URL + "?format=json&q=" + encodedAddress + "&limit=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject obj = jsonArray.getJSONObject(0);
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    System.out.println("✅ Géocodage réussi: " + lat + ", " + lon + " pour " + address);
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur géocodage: " + e.getMessage());
        }
        return null;
    }
}