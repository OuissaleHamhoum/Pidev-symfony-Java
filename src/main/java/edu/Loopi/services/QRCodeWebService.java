package edu.Loopi.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.Loopi.entities.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class QRCodeWebService {

    private QRLoginService qrLoginService;
    private HttpServer server;
    private int port = 8080;
    private Gson gson = new Gson();
    private String localIp;
    private boolean isRunning = false;

    public QRCodeWebService() {
        this.qrLoginService = new QRLoginService();
        this.localIp = getLocalIpAddress();
    }

    public void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/mobile/login", new MobileLoginPageHandler());
            server.createContext("/api/mobile/login", new MobileLoginHandler());
            server.createContext("/test", new TestHandler());

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            isRunning = true;

            System.out.println("\n✅ Serveur web démarré sur le port " + port);
            System.out.println("📱 URL pour le téléphone: http://" + localIp + ":" + port + "/mobile/login");
            System.out.println("🔍 Test local: http://localhost:" + port + "/test");

        } catch (IOException e) {
            System.err.println("❌ Erreur démarrage serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (server != null && isRunning) {
            server.stop(0);
            isRunning = false;
            System.out.println("✅ Serveur web arrêté");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getServerUrl() {
        return "http://" + localIp + ":" + port;
    }

    // UNE SEULE méthode getLocalIpAddress (pas de doublon)
    private String getLocalIpAddress() {
        try {
            // Essayer de trouver une IP non loopback
            java.util.Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                if (netint.isUp() && !netint.isLoopback()) {
                    java.util.Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
            // Fallback à l'IP par défaut
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "192.168.1." + (int)(Math.random() * 100 + 100);
        }
    }

    // Handler de test
    class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "✅ Serveur Loopi fonctionne!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            sendResponse(exchange, response, 200);
        }
    }

    class MobileLoginPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            String sessionId = params.get("session");
            System.out.println("📱 Téléphone connecté - Session: " + sessionId);

            String html = generateMobileLoginPage(sessionId);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            sendResponse(exchange, html, 200);
        }

        private String generateMobileLoginPage(String sessionId) {
            String safeSessionId = sessionId != null ? sessionId : "";

            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Loopi - Connexion Mobile</title>\n" +
                    "    <style>\n" +
                    "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                    "        body {\n" +
                    "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            min-height: 100vh;\n" +
                    "            display: flex;\n" +
                    "            justify-content: center;\n" +
                    "            align-items: center;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            background: white;\n" +
                    "            border-radius: 20px;\n" +
                    "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
                    "            padding: 40px;\n" +
                    "            max-width: 400px;\n" +
                    "            width: 100%;\n" +
                    "            animation: slideUp 0.5s ease;\n" +
                    "        }\n" +
                    "        @keyframes slideUp {\n" +
                    "            from { opacity: 0; transform: translateY(20px); }\n" +
                    "            to { opacity: 1; transform: translateY(0); }\n" +
                    "        }\n" +
                    "        .header {\n" +
                    "            text-align: center;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        .header h1 {\n" +
                    "            color: #333;\n" +
                    "            font-size: 28px;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        .header p {\n" +
                    "            color: #666;\n" +
                    "            font-size: 14px;\n" +
                    "        }\n" +
                    "        .badge {\n" +
                    "            background: #d4edda;\n" +
                    "            color: #155724;\n" +
                    "            padding: 15px;\n" +
                    "            border-radius: 10px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            text-align: center;\n" +
                    "            font-weight: bold;\n" +
                    "        }\n" +
                    "        .form-group {\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .form-group label {\n" +
                    "            display: block;\n" +
                    "            margin-bottom: 8px;\n" +
                    "            color: #555;\n" +
                    "            font-weight: 600;\n" +
                    "            font-size: 14px;\n" +
                    "        }\n" +
                    "        .form-group input {\n" +
                    "            width: 100%;\n" +
                    "            padding: 12px 15px;\n" +
                    "            border: 2px solid #e1e1e1;\n" +
                    "            border-radius: 10px;\n" +
                    "            font-size: 16px;\n" +
                    "            transition: border-color 0.3s;\n" +
                    "        }\n" +
                    "        .form-group input:focus {\n" +
                    "            outline: none;\n" +
                    "            border-color: #667eea;\n" +
                    "        }\n" +
                    "        button {\n" +
                    "            width: 100%;\n" +
                    "            padding: 14px;\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 10px;\n" +
                    "            font-size: 16px;\n" +
                    "            font-weight: 600;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: transform 0.2s, box-shadow 0.2s;\n" +
                    "        }\n" +
                    "        button:hover {\n" +
                    "            transform: translateY(-2px);\n" +
                    "            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);\n" +
                    "        }\n" +
                    "        .message {\n" +
                    "            margin-top: 20px;\n" +
                    "            padding: 10px;\n" +
                    "            border-radius: 8px;\n" +
                    "            text-align: center;\n" +
                    "            font-size: 14px;\n" +
                    "            display: none;\n" +
                    "        }\n" +
                    "        .message.success {\n" +
                    "            background: #d4edda;\n" +
                    "            color: #155724;\n" +
                    "            display: block;\n" +
                    "        }\n" +
                    "        .message.error {\n" +
                    "            background: #f8d7da;\n" +
                    "            color: #721c24;\n" +
                    "            display: block;\n" +
                    "        }\n" +
                    "        .loading {\n" +
                    "            display: none;\n" +
                    "            text-align: center;\n" +
                    "            margin-top: 20px;\n" +
                    "        }\n" +
                    "        .loading.active {\n" +
                    "            display: block;\n" +
                    "        }\n" +
                    "        .spinner {\n" +
                    "            border: 3px solid #f3f3f3;\n" +
                    "            border-top: 3px solid #667eea;\n" +
                    "            border-radius: 50%;\n" +
                    "            width: 40px;\n" +
                    "            height: 40px;\n" +
                    "            animation: spin 1s linear infinite;\n" +
                    "            margin: 0 auto;\n" +
                    "        }\n" +
                    "        @keyframes spin {\n" +
                    "            0% { transform: rotate(0deg); }\n" +
                    "            100% { transform: rotate(360deg); }\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <div class=\"header\">\n" +
                    "            <h1>🔐 Loopi</h1>\n" +
                    "            <p>Connexion mobile sécurisée</p>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"badge\">\n" +
                    "            ✅ QR code scanné avec succès\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <form id=\"loginForm\" onsubmit=\"handleSubmit(event)\">\n" +
                    "            <div class=\"form-group\">\n" +
                    "                <label for=\"email\">📧 Email</label>\n" +
                    "                <input type=\"email\" id=\"email\" name=\"email\" \n" +
                    "                       placeholder=\"votre@email.com\" required \n" +
                    "                       autocomplete=\"email\" autofocus>\n" +
                    "            </div>\n" +
                    "            \n" +
                    "            <div class=\"form-group\">\n" +
                    "                <label for=\"password\">🔐 Mot de passe</label>\n" +
                    "                <input type=\"password\" id=\"password\" name=\"password\" \n" +
                    "                       placeholder=\"••••••••\" required \n" +
                    "                       autocomplete=\"current-password\">\n" +
                    "            </div>\n" +
                    "            \n" +
                    "            <button type=\"submit\" id=\"submitBtn\">\n" +
                    "                Se connecter\n" +
                    "            </button>\n" +
                    "        </form>\n" +
                    "        \n" +
                    "        <div class=\"loading\" id=\"loading\">\n" +
                    "            <div class=\"spinner\"></div>\n" +
                    "            <p>Connexion en cours...</p>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"message\" id=\"message\"></div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <script>\n" +
                    "        const sessionId = \"" + safeSessionId + "\";\n" +
                    "\n" +
                    "        async function handleSubmit(event) {\n" +
                    "            event.preventDefault();\n" +
                    "            \n" +
                    "            const email = document.getElementById('email').value;\n" +
                    "            const password = document.getElementById('password').value;\n" +
                    "            const submitBtn = document.getElementById('submitBtn');\n" +
                    "            const loading = document.getElementById('loading');\n" +
                    "            const message = document.getElementById('message');\n" +
                    "            \n" +
                    "            if (!email || !password) {\n" +
                    "                showMessage('Veuillez remplir tous les champs', 'error');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            if (!isValidEmail(email)) {\n" +
                    "                showMessage('Email invalide', 'error');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            submitBtn.disabled = true;\n" +
                    "            loading.classList.add('active');\n" +
                    "            \n" +
                    "            try {\n" +
                    "                const response = await fetch('/api/mobile/login', {\n" +
                    "                    method: 'POST',\n" +
                    "                    headers: {\n" +
                    "                        'Content-Type': 'application/json',\n" +
                    "                    },\n" +
                    "                    body: JSON.stringify({\n" +
                    "                        sessionId: sessionId,\n" +
                    "                        email: email,\n" +
                    "                        password: password\n" +
                    "                    })\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                \n" +
                    "                if (result.success) {\n" +
                    "                    showMessage('✅ ' + result.message, 'success');\n" +
                    "                    setTimeout(() => window.close(), 2000);\n" +
                    "                } else {\n" +
                    "                    showMessage('❌ ' + result.message, 'error');\n" +
                    "                    submitBtn.disabled = false;\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                showMessage('❌ Erreur de connexion', 'error');\n" +
                    "                submitBtn.disabled = false;\n" +
                    "            } finally {\n" +
                    "                loading.classList.remove('active');\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function isValidEmail(email) {\n" +
                    "            return /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/.test(email);\n" +
                    "        }\n" +
                    "        \n" +
                    "        function showMessage(text, type) {\n" +
                    "            const message = document.getElementById('message');\n" +
                    "            message.textContent = text;\n" +
                    "            message.className = 'message ' + type;\n" +
                    "        }\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }
    }

    class MobileLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, false, "Méthode non autorisée", 405);
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();

                String sessionId = jsonObject.has("sessionId") ? jsonObject.get("sessionId").getAsString() : "";
                String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : "";
                String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : "";

                QRLoginService.QRValidationResult result = qrLoginService.validateMobileLogin(sessionId, email, password);

                sendJsonResponse(exchange, result.isSuccess(), result.getMessage(), result.isSuccess() ? 200 : 400);

            } catch (Exception e) {
                sendJsonResponse(exchange, false, "Erreur: " + e.getMessage(), 500);
            }
        }

        private void sendJsonResponse(HttpExchange exchange, boolean success, String message, int statusCode) throws IOException {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", message);
            sendResponse(exchange, gson.toJson(response), statusCode);
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    result.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return result;
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}