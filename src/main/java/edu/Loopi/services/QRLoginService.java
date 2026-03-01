package edu.Loopi.services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import edu.Loopi.entities.User;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QRLoginService {

    private static final int QR_SIZE = 350;
    private static final long QR_EXPIRY_TIME = 120000; // 2 minutes
    private static final String QR_CODES_DIR = "src/main/resources/qrcodes/";

    private static Map<String, QRLoginSession> activeSessions = new ConcurrentHashMap<>();

    private AuthService authService;
    private UserService userService;

    public QRLoginService() {
        this.authService = new AuthService();
        this.userService = new UserService();

        try {
            Files.createDirectories(Paths.get(QR_CODES_DIR));
        } catch (IOException e) {
            System.err.println("❌ Erreur création dossier QR: " + e.getMessage());
        }

        startCleanupThread();
    }

    /**
     * Générer un QR Code avec une URL directe
     */
    public QRCodeResult generateLoginQRCode(String loginUrl, String sessionId) {
        try {
            System.out.println("📱 Génération QR code pour URL: " + loginUrl);

            QRLoginSession session = new QRLoginSession(sessionId, System.currentTimeMillis());
            activeSessions.put(sessionId, session);

            BufferedImage qrImage = generateQRCode(loginUrl);
            return new QRCodeResult(qrImage, sessionId, loginUrl);

        } catch (Exception e) {
            System.err.println("❌ Erreur génération QR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }    private BufferedImage generateQRCode(String data) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Valider la connexion depuis le téléphone
     */
    public QRValidationResult validateMobileLogin(String sessionId, String email, String password) {
        try {
            QRLoginSession session = activeSessions.get(sessionId);

            if (session == null) {
                return new QRValidationResult(false, "Session invalide ou expirée", null);
            }

            if (session.isUsed()) {
                return new QRValidationResult(false, "QR code déjà utilisé", null);
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - session.getTimestamp() > QR_EXPIRY_TIME) {
                activeSessions.remove(sessionId);
                return new QRValidationResult(false, "QR code expiré", null);
            }

            User user = authService.login(email, password);
            if (user == null) {
                return new QRValidationResult(false, "Email ou mot de passe incorrect", null);
            }

            session.setUsed(true);
            session.setUserId(user.getId());

            return new QRValidationResult(true, "Connexion réussie", user);

        } catch (Exception e) {
            return new QRValidationResult(false, "Erreur: " + e.getMessage(), null);
        }
    }

    /**
     * Vérifier si une session est validée
     */
    public QRValidationResult checkSessionStatus(String sessionId) {
        QRLoginSession session = activeSessions.get(sessionId);

        if (session == null) {
            return new QRValidationResult(false, "Session expirée", null);
        }

        if (session.isUsed() && session.getUserId() > 0) {
            User user = userService.getUserById(session.getUserId());
            if (user != null) {
                return new QRValidationResult(true, "Connexion réussie", user);
            }
        }

        return new QRValidationResult(false, "En attente de validation", null);
    }

    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    long currentTime = System.currentTimeMillis();
                    activeSessions.entrySet().removeIf(entry ->
                            currentTime - entry.getValue().getTimestamp() > QR_EXPIRY_TIME
                    );
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
        private BufferedImage image;
        private String sessionId;
        private String loginUrl;

        public QRCodeResult(BufferedImage image, String sessionId, String loginUrl) {
            this.image = image;
            this.sessionId = sessionId;
            this.loginUrl = loginUrl;
        }

        public BufferedImage getImage() { return image; }
        public String getSessionId() { return sessionId; }
        public String getLoginUrl() { return loginUrl; }
        public Image getFXImage() { return SwingFXUtils.toFXImage(image, null); }
    }

    public static class QRValidationResult {
        private boolean success;
        private String message;
        private User user;

        public QRValidationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    private static class QRLoginSession {
        private String sessionId;
        private long timestamp;
        private boolean used;
        private int userId;

        public QRLoginSession(String sessionId, long timestamp) {
            this.sessionId = sessionId;
            this.timestamp = timestamp;
            this.used = false;
            this.userId = -1;
        }

        public String getSessionId() { return sessionId; }
        public long getTimestamp() { return timestamp; }
        public boolean isUsed() { return used; }
        public int getUserId() { return userId; }
        public void setUsed(boolean used) { this.used = used; }
        public void setUserId(int userId) { this.userId = userId; }
    }
}