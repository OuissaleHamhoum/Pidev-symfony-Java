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
import java.nio.charset.StandardCharsets;

public class GeocodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "LoopiApp/1.0 (contact@loopi.tn)";

    /**
     * Géocode une adresse et retourne les coordonnées [latitude, longitude]
     */
    public double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            System.err.println("❌ Adresse vide ou nulle");
            return null;
        }

        try {
            // Nettoyer l'adresse
            address = address.trim();

            // Ajouter "Tunisie" si pas déjà présent
            if (!address.toLowerCase().contains("tunisie") &&
                    !address.toLowerCase().contains("tunisia")) {
                address = address + ", Tunisie";
            }

            System.out.println("📍 Géocodage de l'adresse: " + address);

            // Encoder l'adresse pour l'URL
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());

            // Construire l'URL avec des paramètres optimisés
            String urlStr = NOMINATIM_URL + "?q=" + encodedAddress +
                    "&format=json" +
                    "&limit=1" +
                    "&addressdetails=1" +
                    "&accept-language=fr" +
                    "&countrycodes=tn";

            System.out.println("📡 URL: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "fr,fr-FR;q=0.9");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonArray results = JsonParser.parseString(response.toString()).getAsJsonArray();
                if (results.size() > 0) {
                    JsonObject first = results.get(0).getAsJsonObject();

                    if (first.has("lat") && first.has("lon")) {
                        double lat = first.get("lat").getAsDouble();
                        double lon = first.get("lon").getAsDouble();

                        System.out.println("✅ Coordonnées trouvées: " + lat + ", " + lon);

                        // Respecter la limite de requêtes
                        Thread.sleep(1000);

                        return new double[]{lat, lon};
                    }
                } else {
                    System.out.println("⚠️ Aucun résultat trouvé pour: " + address);
                }
            } else {
                System.err.println("❌ Erreur HTTP: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur géocodage: " + e.getMessage());
            e.printStackTrace();
        }

        // Retourner Tunis par défaut en cas d'échec
        System.out.println("⚠️ Utilisation des coordonnées par défaut (Tunis)");
        return new double[]{36.8065, 10.1815};
    }

    public boolean updateEventCoordinates(int eventId, String lieu) {
        double[] coords = geocodeAddress(lieu);
        if (coords != null && coords.length == 2) {
            String sql = "UPDATE evenement SET latitude = ?, longitude = ? WHERE id_evenement = ?";
            try (java.sql.Connection conn = MyConnection.getInstance().getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, coords[0]);
                pstmt.setDouble(2, coords[1]);
                pstmt.setInt(3, eventId);
                int result = pstmt.executeUpdate();
                System.out.println("✅ Coordonnées mises à jour en BDD pour l'événement " + eventId);
                return result > 0;
            } catch (Exception e) {
                System.err.println("❌ Erreur mise à jour BDD: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }
}