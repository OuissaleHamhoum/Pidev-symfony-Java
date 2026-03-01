package edu.Loopi.services;

import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CameraService {

    private Webcam webcam;
    private boolean isRunning = false;
    private BufferedImage lastCapturedImage;

    // Dossier pour stocker les photos
    private static final String PHOTOS_DIR = "src/main/resources/profiles/";

    public CameraService() {
        // Créer le dossier s'il n'existe pas
        try {
            Files.createDirectories(Paths.get(PHOTOS_DIR));
        } catch (IOException e) {
            System.err.println("❌ Erreur création dossier photos: " + e.getMessage());
        }
    }

    /**
     * Démarrer la caméra
     */
    public boolean startCamera() {
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                System.err.println("❌ Aucune caméra trouvée");
                return false;
            }

            webcam.open();
            isRunning = true;
            System.out.println("✅ Caméra démarrée");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur démarrage caméra: " + e.getMessage());
            return false;
        }
    }

    /**
     * Arrêter la caméra
     */
    public void stopCamera() {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            isRunning = false;
            System.out.println("✅ Caméra arrêtée");
        }
    }

    /**
     * Capturer une image depuis la caméra
     */
    public BufferedImage captureImage() {
        if (webcam != null && webcam.isOpen()) {
            lastCapturedImage = webcam.getImage();
            return lastCapturedImage;
        }
        return null;
    }

    /**
     * Convertir BufferedImage en Image JavaFX
     */
    public Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Sauvegarder une photo
     */
    public String savePhoto(BufferedImage image, int userId) {
        try {
            String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(PHOTOS_DIR + fileName);
            ImageIO.write(image, "jpg", outputFile);
            System.out.println("✅ Photo sauvegardée: " + fileName);
            return "profiles/" + fileName;
        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde photo: " + e.getMessage());
            return "default.jpg";
        }
    }

    /**
     * Vérifier si l'image contient un visage (version simplifiée)
     * Dans une vraie application, utilisez OpenCV ou une API de reconnaissance faciale
     */
    public boolean hasFace(BufferedImage image) {
        // Version simplifiée - toujours retourner true
        // Pour une vraie reconnaissance, intégrez OpenCV ou une API comme Amazon Rekognition
        return true;
    }

    /**
     * Authentifier par reconnaissance faciale (version simplifiée)
     */
    public int authenticateByFace(BufferedImage image) {
        // Version simplifiée - retourne -1 (non reconnu)
        // Dans une vraie application, implémentez la reconnaissance faciale
        System.out.println("⚠️ Reconnaissance faciale non implémentée - utilisation mode démo");
        return -1;
    }

    /**
     * Ajouter un visage à l'entraînement (version simplifiée)
     */
    public void addFaceForTraining(BufferedImage image, int userId) {
        // Sauvegarder simplement la photo sans reconnaissance
        savePhoto(image, userId);
        System.out.println("✅ Photo sauvegardée pour l'utilisateur " + userId);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public BufferedImage getLastCapturedImage() {
        return lastCapturedImage;
    }
}