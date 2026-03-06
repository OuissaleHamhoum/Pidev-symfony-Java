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
    private static final String USER_AGENT = "LoopiApp/1.0 (contact@loopi.tn)";

    public List<String> getAddressSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();

        if (query == null || query.trim().length() < 3) {
            return suggestions;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = NOMINATIM_URL + encodedQuery + "&limit=5&countrycodes=tn";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    suggestions.add(obj.getString("display_name"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur suggestions: " + e.getMessage());
        }
        return suggestions;
    }

    public double[] geocodeAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + encodedAddress + "&limit=1";

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
                    return new double[]{obj.getDouble("lat"), obj.getDouble("lon")};
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur géocodage: " + e.getMessage());
        }
        return null;
    }
}