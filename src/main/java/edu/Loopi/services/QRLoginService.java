package edu.Loopi.services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import edu.Loopi.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QRLoginService {

    private static final int QR_SIZE = 350;
    private static final long QR_EXPIRY_TIME = 120000; // 2 minutes
    private static final String SECRET_KEY = "votreCléSecrèteTrèsLonguePourJWT2026LoopiApp";

    private static Map<String, QRLoginSession> activeSessions = new ConcurrentHashMap<>();
    private AuthService authService;
    private UserService userService;

    public QRLoginService() {
        this.authService = new AuthService();
        this.userService = new UserService();
        startCleanupThread();
    }

    /**
     * Génère un QR code avec token JWT
     */
    public QRCodeResult generateQRCode(String serverUrl) {
        try {
            String sessionId = UUID.randomUUID().toString();
            String token = generateJWTToken(sessionId);

            QRLoginSession session = new QRLoginSession(sessionId, token, System.currentTimeMillis());
            activeSessions.put(sessionId, session);

            String loginUrl = serverUrl + "/login?session=" + sessionId + "&token=" + token;
            BufferedImage qrImage = generateQRCodeImage(loginUrl);

            System.out.println("📱 QR Code généré pour session: " + sessionId);
            System.out.println("🔗 URL: " + loginUrl);

            return new QRCodeResult(qrImage, sessionId, loginUrl);

        } catch (Exception e) {
            System.err.println("❌ Erreur génération QR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String generateJWTToken(String sessionId) {
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.builder()
                .setSubject(sessionId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + QR_EXPIRY_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private BufferedImage generateQRCodeImage(String data) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Valide la connexion depuis le téléphone
     */
    public QRValidationResult validateLogin(String sessionId, String token, String email, String password) {
        System.out.println("🔍 Validation login - Session: " + sessionId + ", Email: " + email);

        try {
            QRLoginSession session = activeSessions.get(sessionId);

            if (session == null) {
                System.out.println("❌ Session non trouvée: " + sessionId);
                return new QRValidationResult(false, "Session invalide ou expirée");
            }

            if (session.isUsed()) {
                System.out.println("❌ Session déjà utilisée: " + sessionId);
                return new QRValidationResult(false, "QR code déjà utilisé");
            }

            if (!session.getToken().equals(token)) {
                System.out.println("❌ Token invalide");
                return new QRValidationResult(false, "Token de sécurité invalide");
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - session.getTimestamp() > QR_EXPIRY_TIME) {
                System.out.println("❌ Session expirée: " + sessionId);
                activeSessions.remove(sessionId);
                return new QRValidationResult(false, "QR code expiré");
            }

            User user = authService.login(email, password);
            if (user == null) {
                System.out.println("❌ Identifiants incorrects pour: " + email);
                return new QRValidationResult(false, "Email ou mot de passe incorrect");
            }

            session.setUsed(true);
            session.setUserId(user.getId());

            System.out.println("✅ Connexion réussie pour: " + user.getEmail());
            return new QRValidationResult(true, "Connexion réussie", user);

        } catch (Exception e) {
            System.err.println("❌ Erreur validation: " + e.getMessage());
            e.printStackTrace();
            return new QRValidationResult(false, "Erreur: " + e.getMessage());
        }
    }

    /**
     * Vérifie le statut d'une session
     */
    public QRValidationResult checkSessionStatus(String sessionId) {
        QRLoginSession session = activeSessions.get(sessionId);

        if (session == null) {
            return new QRValidationResult(false, "Session expirée");
        }

        if (session.isUsed() && session.getUserId() > 0) {
            User user = userService.getUserById(session.getUserId());
            if (user != null) {
                return new QRValidationResult(true, "Connexion réussie", user);
            }
        }

        return new QRValidationResult(false, "En attente de validation");
    }

    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // Nettoyage toutes les 30 secondes
                    long currentTime = System.currentTimeMillis();
                    activeSessions.entrySet().removeIf(entry ->
                            currentTime - entry.getValue().getTimestamp() > QR_EXPIRY_TIME);

                    if (!activeSessions.isEmpty()) {
                        System.out.println("🧹 Nettoyage: " + activeSessions.size() + " sessions actives");
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    // Classes de résultat
    public static class QRCodeResult {
        public final BufferedImage image;
        public final String sessionId;
        public final String loginUrl;

        public QRCodeResult(BufferedImage image, String sessionId, String loginUrl) {
            this.image = image;
            this.sessionId = sessionId;
            this.loginUrl = loginUrl;
        }

        public Image getFXImage() {
            return SwingFXUtils.toFXImage(image, null);
        }
    }

    public static class QRValidationResult {
        public final boolean success;
        public final String message;
        public final User user;

        public QRValidationResult(boolean success, String message) {
            this(success, message, null);
        }

        public QRValidationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }

    private static class QRLoginSession {
        private final String sessionId;
        private final String token;
        private final long timestamp;
        private boolean used;
        private int userId;

        public QRLoginSession(String sessionId, String token, long timestamp) {
            this.sessionId = sessionId;
            this.token = token;
            this.timestamp = timestamp;
            this.used = false;
            this.userId = -1;
        }

        public String getSessionId() { return sessionId; }
        public String getToken() { return token; }
        public long getTimestamp() { return timestamp; }
        public boolean isUsed() { return used; }
        public int getUserId() { return userId; }
        public void setUsed(boolean used) { this.used = used; }
        public void setUserId(int userId) { this.userId = userId; }
    }
}