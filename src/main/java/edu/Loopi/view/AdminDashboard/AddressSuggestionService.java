package edu.Loopi.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class AddressSuggestionService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";
    private static final String USER_AGENT = "LoopiApp/1.0";

    public List<String> getAddressSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();

        if (query == null || query.trim().length() < 3) {
            return suggestions;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = NOMINATIM_URL + encodedQuery + "&limit=5";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

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
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des suggestions d'adresses: " + e.getMessage());
            // En cas d'erreur, retourner une liste vide
        }

        return suggestions;
    }

    public double[] geocodeAddress(String address) {
        double[] coords = null;

        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + encodedAddress + "&limit=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject obj = jsonArray.getJSONObject(0);
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    coords = new double[]{lat, lon};
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du géocodage de l'adresse: " + e.getMessage());
        }

        return coords;
    }
}