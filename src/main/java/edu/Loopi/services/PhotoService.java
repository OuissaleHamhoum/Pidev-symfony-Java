package edu.Loopi.services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PhotoService {

    private static final String PROFILES_DIR = "src/main/resources/profiles/";
    private static final String TEMP_DIR = "src/main/resources/temp/";

    public PhotoService() {
        // Créer les dossiers s'ils n'existent pas
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(PROFILES_DIR));
            Files.createDirectories(Paths.get(TEMP_DIR));
            System.out.println("✅ Dossiers de photos créés");
        } catch (IOException e) {
            System.err.println("❌ Erreur création dossiers: " + e.getMessage());
        }
    }

    /**
     * Choisir une photo depuis l'ordinateur
     */
    public File choosePhotoFromComputer(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println("✅ Photo sélectionnée: " + selectedFile.getName());
        }
        return selectedFile;
    }

    /**
     * Sauvegarder une photo de profil
     */
    public String saveProfilePhoto(File sourceFile, int userId) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = getFileExtension(sourceFile.getName());
            String fileName = "profile_" + userId + "_" + timestamp + extension;
            Path destination = Paths.get(PROFILES_DIR + fileName);

            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Photo sauvegardée: " + fileName);

            return "profiles/" + fileName;
        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde photo: " + e.getMessage());
            return "default.jpg";
        }
    }

    /**
     * Sauvegarder une photo depuis la caméra
     */
    public String saveCameraPhoto(BufferedImage image, int userId) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "profile_" + userId + "_" + timestamp + ".jpg";
            File outputFile = new File(PROFILES_DIR + fileName);

            ImageIO.write(image, "jpg", outputFile);
            System.out.println("✅ Photo caméra sauvegardée: " + fileName);

            return "profiles/" + fileName;
        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde photo caméra: " + e.getMessage());
            return "default.jpg";
        }
    }

    /**
     * Convertir JavaFX Image en BufferedImage
     */
    public BufferedImage fxImageToBufferedImage(Image image) {
        return SwingFXUtils.fromFXImage(image, null);
    }

    /**
     * Charger une image depuis le dossier profiles
     */
    public Image loadProfileImage(String photoPath, double width, double height) {
        try {
            if (photoPath == null || photoPath.isEmpty() || photoPath.equals("default.jpg")) {
                return null;
            }

            File file = new File(photoPath);
            if (!file.exists()) {
                file = new File(PROFILES_DIR + photoPath.replace("profiles/", ""));
            }

            if (file.exists()) {
                return new Image(file.toURI().toString(), width, height, true, true);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement image: " + e.getMessage());
        }
        return null;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? ".jpg" : fileName.substring(dotIndex);
    }
}