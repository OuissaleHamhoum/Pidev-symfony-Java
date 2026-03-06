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

    /**
     * Convertit une adresse en coordonnées GPS
     * @param address L'adresse à géocoder
     * @return Un tableau [latitude, longitude] ou null si non trouvé
     */
    public double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            System.out.println("⚠️ Adresse vide");
            return null;
        }

        try {
            // Nettoyer l'adresse
            address = address.trim();

            // Ajouter "Tunisie" si l'adresse ne contient pas de pays
            if (!address.toLowerCase().contains("tunisie") &&
                    !address.toLowerCase().contains("tunisia") &&
                    !address.toLowerCase().contains("تونس")) {
                address = address + ", Tunisie";
            }

            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + "?q=" + encodedAddress + "&format=json&limit=1&countrycodes=tn";

            System.out.println("📍 Géocodage de: " + address);

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
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonArray results = JsonParser.parseString(response.toString()).getAsJsonArray();
                if (results.size() > 0) {
                    JsonObject first = results.get(0).getAsJsonObject();
                    double lat = first.get("lat").getAsDouble();
                    double lon = first.get("lon").getAsDouble();

                    System.out.println("✅ Géocodage réussi: " + address + " -> " + lat + ", " + lon);

                    // Attendre un peu pour respecter la limite de 1 requête/seconde
                    Thread.sleep(1000);

                    return new double[]{lat, lon};
                } else {
                    System.out.println("⚠️ Aucun résultat pour: " + address);
                }
            } else {
                System.err.println("❌ Erreur HTTP " + responseCode + " pour: " + address);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur géocodage: " + e.getMessage());
        }

        // Retourner des coordonnées par défaut (centre de la Tunisie)
        return new double[]{36.8065, 10.1815};
    }

    /**
     * Met à jour les coordonnées d'un événement dans la base de données
     */
    public boolean updateEventCoordinates(int eventId, String lieu) {
        double[] coords = geocodeAddress(lieu);
        if (coords != null) {
            String sql = "UPDATE evenement SET latitude = ?, longitude = ? WHERE id_evenement = ?";
            try (java.sql.Connection conn = MyConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setDouble(1, coords[0]);
                pstmt.setDouble(2, coords[1]);
                pstmt.setInt(3, eventId);

                int updated = pstmt.executeUpdate();
                if (updated > 0) {
                    System.out.println("✅ Coordonnées mises à jour pour événement ID " + eventId);
                    return true;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur mise à jour BDD: " + e.getMessage());
            }
        }
        return false;
    }
}