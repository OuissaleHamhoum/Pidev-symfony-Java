package edu.Loopi.services;

import edu.Loopi.entities.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.Scene;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class GoogleAuthService {

    private static final String CONFIG_FILE = "/config/google-oauth.properties";
    private AuthService authService;

    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public GoogleAuthService(AuthService authService) {
        this.authService = authService;
        loadConfig();
    }

    private void loadConfig() {
        Properties config = new Properties();
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("❌ Fichier de configuration non trouvé: " + CONFIG_FILE);
                return;
            }
            config.load(input);

            clientId = config.getProperty("google.client.id");
            clientSecret = config.getProperty("google.client.secret");
            redirectUri = config.getProperty("google.redirect.uri");

            // Nettoyer les valeurs
            if (clientId != null) clientId = clientId.trim();
            if (clientSecret != null) clientSecret = clientSecret.trim();
            if (redirectUri != null) redirectUri = redirectUri.trim();

            System.out.println("✅ Google OAuth configuré");
            System.out.println("   Client ID: " + maskString(clientId));
            System.out.println("   Redirect URI: " + redirectUri);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void openGoogleLogin(Stage parentStage, GoogleLoginCallback callback) {
        System.out.println("\n🔐 DÉMARRAGE CONNEXION GOOGLE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            System.err.println("❌ Configuration Google incomplète");
            Platform.runLater(() -> callback.onError("Configuration Google incomplète"));
            return;
        }

        Stage loginStage = new Stage();
        loginStage.setTitle("Connexion avec Google - Loopi");
        loginStage.initOwner(parentStage);
        loginStage.initModality(Modality.WINDOW_MODAL);
        loginStage.setResizable(false);
        loginStage.setWidth(480);
        loginStage.setHeight(640);

        WebView webView = new WebView();
        webView.setPrefSize(480, 640);
        WebEngine webEngine = webView.getEngine();

        // IMPORTANT: Utiliser un thread séparé pour le callback
        webEngine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc != null && newLoc.startsWith(redirectUri)) {
                System.out.println("📍 Callback détecté: " + newLoc);
                // Traiter dans un thread séparé pour ne pas bloquer JavaFX
                new Thread(() -> handleCallback(newLoc, loginStage, callback)).start();
            }
        });

        String authUrl = buildAuthUrl();
        System.out.println("🔐 URL d'authentification: " + authUrl);
        webEngine.load(authUrl);

        Scene scene = new Scene(webView);
        loginStage.setScene(scene);
        loginStage.showAndWait();
    }

    private String buildAuthUrl() {
        try {
            return "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()) + "&" +
                    "redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()) + "&" +
                    "response_type=code&" +
                    "scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8.name()) + "&" +
                    "access_type=offline&" +
                    "prompt=consent";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void handleCallback(String url, Stage loginStage, GoogleLoginCallback callback) {
        try {
            System.out.println("\n📥 TRAITEMENT DU CALLBACK");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Extraire le code avec décodage URL
            String code = extractCode(url);

            // Fermer la fenêtre de login sur le thread JavaFX
            Platform.runLater(loginStage::close);

            if (code != null) {
                System.out.println("✅ Code d'autorisation reçu: " + code);
                System.out.println("🔄 Échange du code contre token...");

                String accessToken = exchangeCodeForToken(code);

                if (accessToken != null) {
                    System.out.println("✅ Token d'accès obtenu");
                    System.out.println("🔄 Récupération des infos utilisateur...");

                    JsonObject userInfo = getUserInfo(accessToken);

                    if (userInfo != null) {
                        processUserInfo(userInfo, callback);
                    } else {
                        System.err.println("❌ Erreur infos utilisateur");
                        Platform.runLater(() -> callback.onError("Erreur récupération infos"));
                    }
                } else {
                    System.err.println("❌ Échec obtention token");
                    Platform.runLater(() -> callback.onError("Échec obtention token"));
                }
            } else {
                System.err.println("❌ Code non trouvé dans l'URL");
                Platform.runLater(() -> callback.onError("Code d'autorisation non trouvé"));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> callback.onError("Erreur: " + e.getMessage()));
        }
    }

    /**
     * Extrait le code de l'URL avec décodage URL complet
     */
    private String extractCode(String url) throws Exception {
        // Parser l'URL correctement
        URL parsedUrl = new URL(url);
        String query = parsedUrl.getQuery();

        if (query == null) {
            return null;
        }

        // Séparer les paramètres
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && "code".equals(keyValue[0])) {
                // Décoder le code (important pour les caractères comme %2F)
                return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
            }
        }
        return null;
    }

    /**
     * Échange le code contre un token d'accès
     */
    private String exchangeCodeForToken(String code) throws Exception {
        // Construire les paramètres avec encodage correct
        String params = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8.name()) + "&" +
                "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()) + "&" +
                "client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8.name()) + "&" +
                "redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()) + "&" +
                "grant_type=authorization_code";

        System.out.println("📤 Envoi requête token à Google...");
        System.out.println("   Client ID: " + maskString(clientId));
        System.out.println("   Code length: " + code.length());

        HttpURLConnection conn = (HttpURLConnection)
                new URL("https://oauth2.googleapis.com/token").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        // Envoyer la requête
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("📥 Code réponse token: " + responseCode);

        if (responseCode == 200) {
            // Succès - lire la réponse
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                return json.get("access_token").getAsString();
            }
        } else {
            // Erreur - lire le message d'erreur
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {

                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
                System.err.println("❌ Erreur détaillée token: " + error.toString());
            }
            return null;
        }
    }

    /**
     * Récupère les informations de l'utilisateur avec le token
     */
    private JsonObject getUserInfo(String accessToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection)
                new URL("https://www.googleapis.com/oauth2/v3/userinfo").openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                return JsonParser.parseString(response.toString()).getAsJsonObject();
            }
        }
        return null;
    }

    /**
     * Traite les informations utilisateur et crée/connecte l'utilisateur
     */
    private void processUserInfo(JsonObject userInfo, GoogleLoginCallback callback) {
        try {
            String email = userInfo.get("email").getAsString();
            String givenName = userInfo.has("given_name") ? userInfo.get("given_name").getAsString() : "";
            String familyName = userInfo.has("family_name") ? userInfo.get("family_name").getAsString() : "";
            String name = userInfo.has("name") ? userInfo.get("name").getAsString() : email;

            System.out.println("\n👤 INFORMATIONS GOOGLE:");
            System.out.println("   Email: " + email);
            System.out.println("   Prénom: " + givenName);
            System.out.println("   Nom: " + familyName);

            // Vérifier si l'utilisateur existe déjà
            User existingUser = authService.getUserByEmail(email);

            if (existingUser != null) {
                System.out.println("✅ Utilisateur existe déjà (ID: " + existingUser.getId() + ")");
                User finalUser = existingUser;
                Platform.runLater(() -> callback.onSuccess(finalUser));
            } else {
                System.out.println("📝 Création nouveau compte...");

                User newUser = new User();
                newUser.setEmail(email);
                newUser.setNom(familyName.isEmpty() ? name : familyName);
                newUser.setPrenom(givenName.isEmpty() ? name : givenName);
                newUser.setPassword("GOOGLE_" + System.currentTimeMillis() + "_AUTH");
                newUser.setRole("participant");
                newUser.setIdGenre(3); // Non spécifié
                newUser.setPhoto("default.jpg");

                boolean registered = authService.registerWithGoogle(newUser);

                if (registered) {
                    System.out.println("✅ Compte créé avec succès");
                    User created = authService.getUserByEmail(email);
                    if (created != null) {
                        User finalUser = created;
                        Platform.runLater(() -> callback.onSuccess(finalUser));
                    } else {
                        System.err.println("❌ Erreur récupération du compte créé");
                        Platform.runLater(() -> callback.onError("Erreur récupération compte"));
                    }
                } else {
                    System.err.println("❌ Échec création du compte");
                    Platform.runLater(() -> callback.onError("Erreur création compte"));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur traitement userInfo: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> callback.onError("Erreur traitement: " + e.getMessage()));
        }
    }

    private String maskString(String str) {
        if (str == null || str.length() < 10) return "***";
        return str.substring(0, 8) + "..." + str.substring(str.length() - 4);
    }

    public interface GoogleLoginCallback {
        void onSuccess(User user);
        void onError(String error);
    }
}