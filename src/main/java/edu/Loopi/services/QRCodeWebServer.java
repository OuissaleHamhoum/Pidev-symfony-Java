package edu.Loopi.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.Loopi.entities.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class QRCodeWebServer {

    private static final int PORT = 8081;
    private HttpServer server;
    private QRLoginService qrLoginService;
    private Gson gson = new Gson();
    private boolean isRunning = false;
    private QRLoginCallback callback;

    public interface QRLoginCallback {
        void onLoginSuccess(User user);
    }

    public QRCodeWebServer(QRLoginService qrLoginService, QRLoginCallback callback) {
        this.qrLoginService = qrLoginService;
        this.callback = callback;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Handlers simplifiés
            server.createContext("/", new SimpleHandler("Bienvenue sur Loopi QR"));
            server.createContext("/login", new LoginPageHandler());
            server.createContext("/api/login", new LoginApiHandler());
            server.createContext("/test", new SimpleHandler("✅ Serveur OK - Connexion établie"));
            server.createContext("/health", new SimpleHandler("✅ Serveur fonctionne normalement"));

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            isRunning = true;

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ SERVEUR QR CODE DÉMARRÉ");
            System.out.println("=".repeat(60));
            System.out.println("\n📱 URL POUR LE TÉLÉPHONE:");
            System.out.println("   ⭐ http://10.21.92.26:" + PORT + "/test");
            System.out.println("   ⭐ http://10.21.92.26:" + PORT + "/login");
            System.out.println("\n🔍 TEST LOCAL:");
            System.out.println("   http://localhost:" + PORT + "/test");
            System.out.println("=".repeat(60) + "\n");

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null && isRunning) {
            server.stop(0);
            isRunning = false;
            System.out.println("✅ Serveur arrêté");
        }
    }

    public String getServerUrl() {
        return "http://10.21.92.26:" + PORT;
    }

    // Handler simple pour les tests
    class SimpleHandler implements HttpHandler {
        private String message;

        public SimpleHandler(String message) {
            this.message = message;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = message + "\nHeure: " + new java.util.Date();
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }

            System.out.println("📱 " + exchange.getRequestURI() + " appelé depuis " + exchange.getRemoteAddress());
        }
    }

    // Handler pour la page de connexion
    class LoginPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            String sessionId = params.get("session");
            String token = params.get("token");

            System.out.println("📱 Page login demandée depuis: " + exchange.getRemoteAddress());

            String html = generateLoginPage(sessionId, token);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }

            System.out.println("   ✅ Page envoyée (" + bytes.length + " octets)");
        }

        private String generateLoginPage(String sessionId, String token) {
            String safeSessionId = sessionId != null ? sessionId : "";
            String safeToken = token != null ? token : "";

            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Loopi - Connexion</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial; background: #0f172a; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; padding: 16px; }\n" +
                    "        .card { background: white; border-radius: 20px; padding: 32px; max-width: 350px; width: 100%; box-shadow: 0 10px 30px rgba(0,0,0,0.3); }\n" +
                    "        h1 { text-align: center; color: #0f172a; margin-bottom: 24px; }\n" +
                    "        .badge { background: #dcfce7; color: #166534; padding: 12px; border-radius: 10px; margin-bottom: 24px; text-align: center; font-weight: bold; }\n" +
                    "        .form-group { margin-bottom: 20px; }\n" +
                    "        label { display: block; margin-bottom: 8px; color: #1e293b; font-weight: 600; }\n" +
                    "        input { width: 100%; padding: 12px; border: 2px solid #e2e8f0; border-radius: 10px; font-size: 16px; box-sizing: border-box; }\n" +
                    "        button { width: 100%; padding: 14px; background: #3b82f6; color: white; border: none; border-radius: 10px; font-size: 16px; font-weight: bold; cursor: pointer; }\n" +
                    "        button:hover { background: #2563eb; }\n" +
                    "        button:disabled { opacity: 0.7; }\n" +
                    "        .message { margin-top: 20px; padding: 12px; border-radius: 8px; text-align: center; display: none; }\n" +
                    "        .success { background: #f0fdf4; color: #166534; display: block; }\n" +
                    "        .error { background: #fef2f2; color: #991b1b; display: block; }\n" +
                    "        .loading { display: none; text-align: center; margin-top: 20px; }\n" +
                    "        .loading.active { display: block; }\n" +
                    "        .spinner { border: 3px solid #f3f3f3; border-top: 3px solid #3b82f6; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto; }\n" +
                    "        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"card\">\n" +
                    "        <h1>🔐 Loopi</h1>\n" +
                    "        <div class=\"badge\">✅ QR code scanné</div>\n" +
                    "        \n" +
                    "        <form id=\"loginForm\">\n" +
                    "            <div class=\"form-group\">\n" +
                    "                <label>📧 Email</label>\n" +
                    "                <input type=\"email\" id=\"email\" placeholder=\"votre@email.com\" required>\n" +
                    "            </div>\n" +
                    "            <div class=\"form-group\">\n" +
                    "                <label>🔐 Mot de passe</label>\n" +
                    "                <input type=\"password\" id=\"password\" placeholder=\"••••••••\" required>\n" +
                    "            </div>\n" +
                    "            <button type=\"submit\" id=\"submitBtn\">Se connecter</button>\n" +
                    "        </form>\n" +
                    "        \n" +
                    "        <div class=\"loading\" id=\"loading\">\n" +
                    "            <div class=\"spinner\"></div>\n" +
                    "            <p style=\"margin-top: 10px;\">Connexion...</p>\n" +
                    "        </div>\n" +
                    "        <div class=\"message\" id=\"message\"></div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <script>\n" +
                    "        const sessionId = \"" + safeSessionId + "\";\n" +
                    "        const token = \"" + safeToken + "\";\n" +
                    "\n" +
                    "        document.getElementById('loginForm').addEventListener('submit', async (e) => {\n" +
                    "            e.preventDefault();\n" +
                    "            \n" +
                    "            const email = document.getElementById('email').value.trim();\n" +
                    "            const password = document.getElementById('password').value;\n" +
                    "            const btn = document.getElementById('submitBtn');\n" +
                    "            const loading = document.getElementById('loading');\n" +
                    "            const msg = document.getElementById('message');\n" +
                    "            \n" +
                    "            if (!email || !password) {\n" +
                    "                msg.textContent = 'Tous les champs sont requis';\n" +
                    "                msg.className = 'message error';\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            btn.disabled = true;\n" +
                    "            loading.classList.add('active');\n" +
                    "            msg.style.display = 'none';\n" +
                    "            \n" +
                    "            try {\n" +
                    "                const response = await fetch('/api/login', {\n" +
                    "                    method: 'POST',\n" +
                    "                    headers: { 'Content-Type': 'application/json' },\n" +
                    "                    body: JSON.stringify({ sessionId, token, email, password })\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                \n" +
                    "                if (result.success) {\n" +
                    "                    msg.textContent = '✅ ' + result.message;\n" +
                    "                    msg.className = 'message success';\n" +
                    "                    setTimeout(() => window.close(), 2000);\n" +
                    "                } else {\n" +
                    "                    msg.textContent = '❌ ' + result.message;\n" +
                    "                    msg.className = 'message error';\n" +
                    "                    btn.disabled = false;\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                msg.textContent = '❌ Erreur de connexion';\n" +
                    "                msg.className = 'message error';\n" +
                    "                btn.disabled = false;\n" +
                    "            } finally {\n" +
                    "                loading.classList.remove('active');\n" +
                    "            }\n" +
                    "        });\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }
    }

    class LoginApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, false, "Méthode non autorisée", 405);
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = gson.fromJson(body, Map.class);

                String sessionId = data.get("sessionId");
                String token = data.get("token");
                String email = data.get("email");
                String password = data.get("password");

                QRLoginService.QRValidationResult result = qrLoginService.validateLogin(
                        sessionId, token, email, password
                );

                if (result.success && callback != null) {
                    callback.onLoginSuccess(result.user);
                }

                sendJson(exchange, result.success, result.message, result.success ? 200 : 400);

            } catch (Exception e) {
                sendJson(exchange, false, "Erreur: " + e.getMessage(), 500);
            }
        }

        private void sendJson(HttpExchange exchange, boolean success, String message, int code) throws IOException {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", message);
            String json = gson.toJson(response);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(code, json.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes());
            }
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) result.put(kv[0], kv[1]);
            }
        }
        return result;
    }
}