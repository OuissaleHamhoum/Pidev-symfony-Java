package edu.Loopi.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.Loopi.tools.MyConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.PreparedStatement;

public class GeocodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "LoopiApp/1.0 (contact@loopi.tn)";

    public double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            address = address.trim();
            if (!address.toLowerCase().contains("tunisie") &&
                    !address.toLowerCase().contains("tunisia")) {
                address = address + ", Tunisie";
            }

            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + "?q=" + encodedAddress + "&format=json&limit=1&countrycodes=tn";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonArray results = JsonParser.parseString(response.toString()).getAsJsonArray();
                if (results.size() > 0) {
                    JsonObject first = results.get(0).getAsJsonObject();
                    Thread.sleep(1000);
                    return new double[]{first.get("lat").getAsDouble(), first.get("lon").getAsDouble()};
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur géocodage: " + e.getMessage());
        }
        return new double[]{36.8065, 10.1815};
    }

    public boolean updateEventCoordinates(int eventId, String lieu) {
        double[] coords = geocodeAddress(lieu);
        if (coords != null) {
            String sql = "UPDATE evenement SET latitude = ?, longitude = ? WHERE id_evenement = ?";
            try (java.sql.Connection conn = MyConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, coords[0]);
                pstmt.setDouble(2, coords[1]);
                pstmt.setInt(3, eventId);
                return pstmt.executeUpdate() > 0;
            } catch (Exception e) {
                System.err.println("❌ Erreur mise à jour: " + e.getMessage());
            }
        }
        return false;
    }
}