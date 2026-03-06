package edu.Loopi.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.Loopi.entities.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class QRCodeWebServer {

    private static final int PORT = 8081;
    private HttpServer server;
    private QRLoginService qrLoginService;
    private Gson gson = new Gson();
    private boolean isRunning = false;
    private QRLoginCallback callback;
    private String serverIp = null;
    private List<String> allIps = new ArrayList<>();

    public interface QRLoginCallback {
        void onLoginSuccess(User user);
    }

    public QRCodeWebServer(QRLoginService qrLoginService, QRLoginCallback callback) {
        this.qrLoginService = qrLoginService;
        this.callback = callback;
        detectAllIpAddresses();
    }

    private void detectAllIpAddresses() {
        allIps.clear();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // Ignorer les interfaces loopback et désactivées
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // Ne garder que les IPv4
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        allIps.add(ip);

                        // Afficher le nom de l'interface pour identification
                        String displayName = iface.getDisplayName().toLowerCase();
                        String interfaceType = "Inconnu";

                        if (displayName.contains("wi-fi") || displayName.contains("wlan") || displayName.contains("wireless")) {
                            interfaceType = "WiFi";
                        } else if (displayName.contains("ethernet") || displayName.contains("lan")) {
                            interfaceType = "Ethernet";
                        } else if (displayName.contains("bluetooth")) {
                            interfaceType = "Bluetooth";
                        } else if (displayName.contains("vmware") || displayName.contains("virtual") || displayName.contains("virtualbox")) {
                            interfaceType = "Virtuelle";
                        }

                        System.out.println("📡 Interface " + interfaceType + ": " + iface.getName() + " -> " + ip);
                    }
                }
            }

            // Choisir la meilleure IP pour le serveur
            selectBestServerIp();

        } catch (SocketException e) {
            System.err.println("❌ Erreur détection interfaces: " + e.getMessage());
            serverIp = "127.0.0.1";
            allIps.add(serverIp);
        }
    }

    private void selectBestServerIp() {
        // Priorité 1: IPs commençant par 10. (réseau local)
        for (String ip : allIps) {
            if (ip.startsWith("10.")) {
                serverIp = ip;
                System.out.println("✅ IP LAN (10.x.x.x) sélectionnée: " + ip);
                return;
            }
        }

        // Priorité 2: IPs de type 192.168.x.x (réseau local)
        for (String ip : allIps) {
            if (ip.startsWith("192.168.")) {
                serverIp = ip;
                System.out.println("✅ IP LAN sélectionnée: " + ip);
                return;
            }
        }

        // Priorité 3: IPs de type 172.16.x.x à 172.31.x.x (réseau local)
        for (String ip : allIps) {
            if (ip.startsWith("172.")) {
                String[] parts = ip.split("\\.");
                if (parts.length > 1) {
                    try {
                        int second = Integer.parseInt(parts[1]);
                        if (second >= 16 && second <= 31) {
                            serverIp = ip;
                            System.out.println("✅ IP LAN sélectionnée: " + ip);
                            return;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                }
            }
        }

        // Si aucune IP locale trouvée, prendre la première non-loopback
        for (String ip : allIps) {
            if (!ip.equals("127.0.0.1") && !ip.equals("0.0.0.0")) {
                serverIp = ip;
                System.out.println("⚠️ IP sélectionnée (peut ne pas être accessible): " + ip);
                return;
            }
        }

        // Fallback
        serverIp = "127.0.0.1";
        System.out.println("⚠️ Aucune IP réseau trouvée, utilisation de localhost");
    }

    public void start() {
        try {
            // Écouter sur toutes les interfaces (0.0.0.0) pour être accessible depuis le réseau
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/", new SimpleHandler("Bienvenue sur Loopi QR"));
            server.createContext("/login", new LoginPageHandler());
            server.createContext("/api/login", new LoginApiHandler());
            server.createContext("/test", new SimpleHandler("✅ Serveur OK - Connexion établie"));
            server.createContext("/health", new SimpleHandler("✅ Serveur fonctionne normalement"));
            server.createContext("/ips", new IPsHandler());
            server.createContext("/qr-code", new QRCodeHandler()); // Page qui montre le QR code

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            isRunning = true;

            printServerInfo();

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void printServerInfo() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ SERVEUR QR CODE DÉMARRÉ AVEC SUCCÈS");
        System.out.println("=".repeat(70));
        System.out.println("\n📱 INSTRUCTIONS POUR LES UTILISATEURS:");
        System.out.println("   1. Connectez-vous au WiFi: " + getCurrentWifiName());
        System.out.println("   2. Scannez le QR code ou tapez l'URL:");
        System.out.println("      ⭐ http://" + serverIp + ":" + PORT + "/login");
        System.out.println("\n🔍 PAGES DE TEST:");
        System.out.println("   • http://" + serverIp + ":" + PORT + "/test");
        System.out.println("   • http://" + serverIp + ":" + PORT + "/health");
        System.out.println("   • http://" + serverIp + ":" + PORT + "/ips");
        System.out.println("   • http://" + serverIp + ":" + PORT + "/qr-code");
        System.out.println("\n📡 TOUTES LES IPS DISPONIBLES:");
        for (String ip : allIps) {
            String type = ip.equals(serverIp) ? " (utilisée)" : "";
            System.out.println("   • http://" + ip + ":" + PORT + "/login" + type);
        }
        System.out.println("\n⚠️ SI LE QR CODE NE FONCTIONNE PAS:");
        System.out.println("   • Vérifiez que le téléphone est sur le MÊME réseau WiFi");
        System.out.println("   • Essayez chaque IP listée ci-dessus");
        System.out.println("   • Désactivez le pare-feu Windows temporairement");
        System.out.println("   • Utilisez l'adresse: http://" + serverIp + ":" + PORT + "/qr-code");
        System.out.println("\n⏱️  Le QR code est valable 2 minutes");
        System.out.println("👥 Plusieurs personnes peuvent se connecter avec leurs propres comptes");
        System.out.println("=".repeat(70) + "\n");
    }

    public void stop() {
        if (server != null && isRunning) {
            server.stop(0);
            isRunning = false;
            System.out.println("✅ Serveur arrêté");
        }
    }

    public String getServerUrl() {
        return "http://" + serverIp + ":" + PORT;
    }

    private String getCurrentWifiName() {
        try {
            Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("SSID") && !line.contains("BSSID")) {
                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }
        } catch (Exception e) {}
        return "le même réseau que l'ordinateur";
    }

    // Handler pour afficher le QR code
    class QRCodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Loopi - QR Code</title>
                    <style>
                        body { font-family: Arial; background: #0f172a; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; padding: 20px; }
                        .container { max-width: 600px; width: 100%; }
                        .card { background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0,0,0,0.3); margin-bottom: 20px; }
                        h1 { color: #0f172a; text-align: center; margin-bottom: 20px; }
                        .ip-list { background: #f8fafc; border-radius: 10px; padding: 20px; margin: 20px 0; }
                        .ip-item { padding: 10px; border-bottom: 1px solid #e2e8f0; }
                        .ip-item:last-child { border-bottom: none; }
                        .ip-address { font-family: monospace; font-size: 18px; color: #059669; font-weight: bold; }
                        .instructions { background: #dcfce7; border-radius: 10px; padding: 15px; color: #166534; margin: 20px 0; }
                        .warning { background: #fee2e2; border-radius: 10px; padding: 15px; color: #991b1b; margin: 20px 0; }
                        .btn { background: #3b82f6; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-size: 16px; cursor: pointer; text-decoration: none; display: inline-block; margin: 5px; }
                        .btn:hover { background: #2563eb; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="card">
                            <h1>📱 Connexion par QR Code</h1>
                            
                            <div class="instructions">
                                <strong>✅ Instructions :</strong><br>
                                1. Connectez votre téléphone au même WiFi que l'ordinateur<br>
                                2. Scannez le QR code affiché dans l'application<br>
                                3. Ou utilisez l'une des adresses ci-dessous
                            </div>
                            
                            <div class="ip-list">
                                <h3>📡 Adresses disponibles :</h3>
                """;

            for (String ip : allIps) {
                String type = ip.equals(serverIp) ? " (recommandée)" : "";
                html += "<div class='ip-item'>🔗 <span class='ip-address'>http://" + ip + ":" + PORT + "/login</span>" + type + "</div>";
            }

            html += """
                            </div>
                            
                            <div class="warning">
                                <strong>⚠️ Dépannage :</strong><br>
                                • Si aucune adresse ne fonctionne, vérifiez que :<br>
                                &nbsp;&nbsp;- Le téléphone est sur le MÊME réseau WiFi<br>
                                &nbsp;&nbsp;- Le pare-feu Windows n'est pas activé<br>
                                &nbsp;&nbsp;- Le port 8081 n'est pas bloqué
                            </div>
                            
                            <div style="text-align: center;">
                                <a href="/login" class="btn">🔐 Aller à la connexion</a>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """;

            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }
        }
    }

    // Handler simple pour les tests
    class SimpleHandler implements HttpHandler {
        private String message;

        public SimpleHandler(String message) {
            this.message = message;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            String response = message + "\nHeure: " + new java.util.Date() +
                    "\nServeur: " + serverIp + ":" + PORT +
                    "\nClient: " + clientIp;
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }

            System.out.println("📱 " + exchange.getRequestURI() + " appelé depuis " + clientIp);
        }
    }

    // Handler pour afficher les IPs
    class IPsHandler implements HttpHandler {
        @Override
        @SuppressWarnings("unchecked")
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder response = new StringBuilder();
            response.append("🌐 SERVEUR LOOPI - INFORMATIONS RÉSEAU\n\n");
            response.append("IP du serveur utilisée: ").append(serverIp).append("\n");
            response.append("Port: ").append(PORT).append("\n\n");

            response.append("📡 TOUTES LES IPS DISPONIBLES:\n");
            for (String ip : allIps) {
                String type = ip.equals(serverIp) ? " (utilisée)" : "";
                response.append("   • http://").append(ip).append(":").append(PORT).append("/login").append(type).append("\n");
            }

            response.append("\n📱 Pour vous connecter:\n");
            response.append("1. Scannez le QR code\n");
            response.append("2. Ou tapez l'URL ci-dessus\n");
            response.append("3. Entrez vos identifiants\n");
            response.append("4. La connexion sera automatique sur l'ordinateur\n");

            byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
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
        @SuppressWarnings("unchecked")
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
                System.out.println("📥 API login - Requête de " + exchange.getRemoteAddress());

                Type mapType = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> data = gson.fromJson(body, mapType);

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
                System.err.println("❌ Erreur API: " + e.getMessage());
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