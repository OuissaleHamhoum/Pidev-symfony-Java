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
import java.util.HashMap;
import java.util.Map;

public class CameraService {

    private Webcam webcam;
    private boolean isRunning = false;
    private BufferedImage lastCapturedImage;

    // Dossier pour stocker les photos
    private static final String PHOTOS_DIR = "src/main/resources/profiles/";

    // Simulateur de reconnaissance faciale (à remplacer par OpenCV)
    private Map<Integer, String> userFaceData = new HashMap<>();

    public CameraService() {
        // Créer le dossier s'il n'existe pas
        try {
            Files.createDirectories(Paths.get(PHOTOS_DIR));
            System.out.println("✅ Dossier photos créé: " + PHOTOS_DIR);
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
     * Sauvegarder une photo de profil
     */
    public String saveProfilePhoto(BufferedImage image, int userId) {
        try {
            String fileName = "profile_" + userId + ".jpg";
            File outputFile = new File(PHOTOS_DIR + fileName);

            // Redimensionner l'image pour uniformité
            BufferedImage resizedImage = resizeImage(image, 200, 200);

            ImageIO.write(resizedImage, "jpg", outputFile);
            System.out.println("✅ Photo sauvegardée: " + fileName);

            // Stocker le chemin relatif
            String relativePath = "profiles/" + fileName;

            // Enregistrer pour la reconnaissance faciale
            userFaceData.put(userId, relativePath);

            return relativePath;
        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde photo: " + e.getMessage());
            return "profiles/default.jpg";
        }
    }

    /**
     * Redimensionner une image
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    /**
     * Authentifier par reconnaissance faciale (version améliorée)
     * Compare l'image capturée avec les photos stockées
     */
    public int authenticateByFace(BufferedImage capturedImage, UserService userService) {
        try {
            if (capturedImage == null) {
                System.out.println("❌ Image capturée null");
                return -1;
            }

            // Redimensionner l'image capturée pour comparaison
            BufferedImage resizedCaptured = resizeImage(capturedImage, 100, 100);

            File profilesDir = new File(PHOTOS_DIR);
            File[] profileFiles = profilesDir.listFiles((dir, name) ->
                    name.startsWith("profile_") && (name.endsWith(".jpg") || name.endsWith(".png"))
            );

            if (profileFiles == null || profileFiles.length == 0) {
                System.out.println("❌ Aucune photo de profil trouvée");
                return -1;
            }

            int bestMatchUserId = -1;
            double bestMatchScore = 0.7; // Seuil minimum de confiance

            System.out.println("🔍 Recherche faciale parmi " + profileFiles.length + " photos...");

            for (File profileFile : profileFiles) {
                try {
                    // Extraire l'ID du nom de fichier
                    String fileName = profileFile.getName();
                    int userId = extractUserIdFromFilename(fileName);

                    if (userId == -1) continue;

                    BufferedImage storedImage = ImageIO.read(profileFile);
                    if (storedImage == null) continue;

                    BufferedImage resizedStored = resizeImage(storedImage, 100, 100);

                    // Calculer la similarité (méthode simplifiée)
                    double similarity = compareImages(resizedCaptured, resizedStored);

                    System.out.println("   Comparaison avec user " + userId + ": " + (similarity * 100) + "%");

                    if (similarity > bestMatchScore) {
                        bestMatchScore = similarity;
                        bestMatchUserId = userId;
                    }

                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lecture fichier: " + profileFile.getName());
                }
            }

            if (bestMatchUserId != -1) {
                System.out.println("✅ Correspondance trouvée: User ID " + bestMatchUserId +
                        " avec score " + (bestMatchScore * 100) + "%");
                return bestMatchUserId;
            } else {
                System.out.println("❌ Aucune correspondance trouvée");
                return -1;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur reconnaissance faciale: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Méthode simplifiée de comparaison d'images
     * Compare les histogrammes de couleurs
     */
    private double compareImages(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();

        if (width != img2.getWidth() || height != img2.getHeight()) {
            return 0;
        }

        long diff = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;

                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;

                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
            }
        }

        double maxDiff = 3L * 255 * width * height;
        double similarity = 1.0 - (diff / maxDiff);

        return similarity;
    }

    /**
     * Extraire l'ID utilisateur du nom de fichier
     */
    private int extractUserIdFromFilename(String filename) {
        try {
            // Format: profile_123.jpg
            String[] parts = filename.split("_");
            if (parts.length >= 2) {
                String idPart = parts[1].split("\\.")[0];
                return Integer.parseInt(idPart);
            }
        } catch (Exception e) {
            // Ignorer
        }
        return -1;
    }

    /**
     * Vérifier si l'image contient un visage (simulé)
     */
    public boolean hasFace(BufferedImage image) {
        // Version simplifiée - toujours retourner true
        return true;
    }

    /**
     * Ajouter un visage pour l'entraînement
     */
    public void addFaceForTraining(BufferedImage image, int userId) {
        saveProfilePhoto(image, userId);
        System.out.println("✅ Visage enregistré pour l'utilisateur " + userId);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public BufferedImage getLastCapturedImage() {
        return lastCapturedImage;
    }
}