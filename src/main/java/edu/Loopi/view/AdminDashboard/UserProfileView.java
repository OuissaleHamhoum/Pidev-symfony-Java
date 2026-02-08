package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserProfileView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;

    public UserProfileView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    public void showUserProfileView(StackPane mainContentArea) {
        VBox profileView = createEnhancedUserProfileView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(profileView);
    }

    private VBox createEnhancedUserProfileView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #E6F8F6;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Manage your personal information and settings");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#03414D"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-border-color: transparent; -fx-cursor: hand;");
        backBtn.setOnAction(e -> adminDashboard.showDashboard());

        header.getChildren().addAll(headerText, backBtn);

        // Contenu du profil
        VBox profileContent = new VBox(30);
        profileContent.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; " +
                "-fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Section Avatar et Informations
        HBox topSection = new HBox(40);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Avatar avec option de modification
        VBox avatarBox = new VBox(20);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPrefWidth(250);

        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(80);
        avatarCircle.setFill(Color.web("#72DFD0"));

        // Charger l'image de profil si disponible
        ImageView avatarImageView = loadProfileImage(currentUser, 160);
        if (avatarImageView != null) {
            avatarContainer.getChildren().add(avatarImageView);
        } else {
            // Si l'image n'existe pas, utiliser l'avatar par défaut
            createDefaultAvatar(avatarContainer, avatarCircle);
        }

        Button changeAvatarBtn = new Button("📷 Change Photo");
        changeAvatarBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        changeAvatarBtn.setOnAction(e -> changeProfilePicture());

        avatarBox.getChildren().addAll(avatarContainer, changeAvatarBtn);

        // Informations de base
        VBox basicInfo = new VBox(20);
        basicInfo.setStyle("-fx-padding: 20 0 0 0;");

        Label nameLabel = new Label(currentUser.getNomComplet());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        nameLabel.setTextFill(Color.web("#03414D"));

        HBox roleBox = new HBox(10);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Label roleLabel = new Label(currentUser.getRole().toUpperCase());
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        roleLabel.setTextFill(Color.WHITE);
        roleLabel.setPadding(new Insets(8, 20, 8, 20));
        roleLabel.setStyle("-fx-background-color: #72DFD0; -fx-background-radius: 20;");

        Label emailLabel = new Label(currentUser.getEmail());
        emailLabel.setFont(Font.font("Arial", 16));
        emailLabel.setTextFill(Color.web("#03414D"));

        Label memberSinceLabel = new Label("Member since: " +
                (currentUser.getCreatedAt() != null ?
                        currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) :
                        "Unknown date"));
        memberSinceLabel.setFont(Font.font("Arial", 12));
        memberSinceLabel.setTextFill(Color.web("#03414D"));

        roleBox.getChildren().addAll(roleLabel);
        basicInfo.getChildren().addAll(nameLabel, roleBox, emailLabel, memberSinceLabel);

        topSection.getChildren().addAll(avatarBox, basicInfo);

        // Séparateur
        Separator separator = new Separator();
        separator.setPadding(new Insets(20, 0, 20, 0));

        // Formulaire de modification
        VBox formSection = new VBox(20);
        formSection.setStyle("-fx-background-color: #E6F8F6; -fx-background-radius: 12; -fx-padding: 30;");

        Label formTitle = new Label("Edit Information");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        formTitle.setTextFill(Color.web("#03414D"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(30);
        formGrid.setVgap(20);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        // Champs de formulaire
        TextField nomField = new TextField(currentUser.getNom());
        styleFormTextField(nomField);

        TextField prenomField = new TextField(currentUser.getPrenom());
        styleFormTextField(prenomField);

        TextField emailField = new TextField(currentUser.getEmail());
        styleFormTextField(emailField);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        if (currentUser.getIdGenre() == 1) {
            genreCombo.setValue("Homme");
        } else if (currentUser.getIdGenre() == 2) {
            genreCombo.setValue("Femme");
        } else {
            genreCombo.setValue("Non spécifié");
        }
        styleFormComboBox(genreCombo);

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current password");
        styleFormTextField(currentPasswordField);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password (optional)");
        styleFormTextField(newPasswordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        styleFormTextField(confirmPasswordField);

        // Ajouter les champs au formulaire
        formGrid.add(new Label("Last Name:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("First Name:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Gender:"), 0, 3);
        formGrid.add(genreCombo, 1, 3);
        formGrid.add(new Label("Current Password:"), 0, 4);
        formGrid.add(currentPasswordField, 1, 4);
        formGrid.add(new Label("New Password:"), 0, 5);
        formGrid.add(newPasswordField, 1, 5);
        formGrid.add(new Label("Confirm Password:"), 0, 6);
        formGrid.add(confirmPasswordField, 1, 6);

        formSection.getChildren().addAll(formTitle, formGrid);

        // Boutons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-min-width: 120; -fx-min-height: 40;");
        cancelBtn.setOnAction(e -> adminDashboard.showDashboard());

        Button saveBtn = new Button("💾 Save Changes");
        saveBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-min-width: 120; -fx-min-height: 40;");
        saveBtn.setOnAction(e -> {
            if (saveProfileChanges(nomField, prenomField, emailField, genreCombo,
                    currentPasswordField, newPasswordField, confirmPasswordField)) {
                adminDashboard.showAlert("Success", "Your profile has been updated successfully!");
                // Mettre à jour l'utilisateur courant
                currentUser = userService.getUserById(currentUser.getId());
                SessionManager.setCurrentUser(currentUser);
                // Mettre à jour l'utilisateur dans l'AdminDashboard
                adminDashboard.setCurrentUser(currentUser);
                // Mettre à jour l'image de profil dans le header
                adminDashboard.updateHeaderProfileImage();
                // Rafraîchir l'affichage
                showUserProfileView(adminDashboard.getMainContentArea());
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        profileContent.getChildren().addAll(topSection, separator, formSection, buttonBox);
        container.getChildren().addAll(header, profileContent);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return new VBox(scrollPane);
    }

    private ImageView loadProfileImage(User user, double size) {
        if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.jpg")) {
            try {
                String photoPath = user.getPhoto();
                File imageFile;

                // Vérifier différents formats de chemin
                if (photoPath.startsWith("profiles/")) {
                    imageFile = new File(photoPath);
                } else if (photoPath.startsWith("profiles\\")) {
                    imageFile = new File(photoPath);
                } else if (photoPath.contains("profile_")) {
                    // Essayer avec le dossier profiles
                    imageFile = new File("profiles/" + photoPath);
                    if (!imageFile.exists()) {
                        imageFile = new File(photoPath);
                    }
                } else {
                    imageFile = new File(photoPath);
                }

                if (imageFile.exists()) {
                    Image avatarImage = new Image("file:" + imageFile.getAbsolutePath(), size, size, true, true, true);
                    ImageView avatarImageView = new ImageView(avatarImage);
                    avatarImageView.setFitWidth(size);
                    avatarImageView.setFitHeight(size);
                    avatarImageView.setPreserveRatio(true);
                    avatarImageView.setStyle("-fx-background-radius: 50%;");
                    return avatarImageView;
                }
            } catch (Exception e) {
                System.out.println("Error loading profile image for user " + user.getId() + ": " + e.getMessage());
            }
        }

        return null;
    }

    private void createDefaultAvatar(StackPane container, Circle circle) {
        String initials = adminDashboard.getInitials(currentUser);
        Label avatarText = new Label(initials);
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        avatarText.setTextFill(Color.WHITE);
        container.getChildren().addAll(circle, avatarText);
    }

    // ============ GESTION DU PROFIL ============
    private boolean saveProfileChanges(TextField nomField, TextField prenomField, TextField emailField,
                                       ComboBox<String> genreCombo, PasswordField currentPasswordField,
                                       PasswordField newPasswordField, PasswordField confirmPasswordField) {
        // Validation des champs obligatoires
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            adminDashboard.showAlert("Error", "Last Name, First Name and Email are required");
            return false;
        }

        // Validation de l'email
        if (!isValidEmail(emailField.getText())) {
            adminDashboard.showAlert("Error", "Please enter a valid email address");
            return false;
        }

        // Vérifier si l'email a changé
        if (!emailField.getText().equals(currentUser.getEmail())) {
            if (userService.emailExists(emailField.getText())) {
                adminDashboard.showAlert("Error", "This email is already used by another user");
                return false;
            }
        }

        // Vérification du mot de passe actuel si changement de mot de passe demandé
        if (!newPasswordField.getText().isEmpty()) {
            if (currentPasswordField.getText().isEmpty()) {
                adminDashboard.showAlert("Error", "Please enter your current password to change password");
                return false;
            }

            // Dans un système réel, on vérifierait le mot de passe hashé
            if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
                adminDashboard.showAlert("Error", "Current password is incorrect");
                return false;
            }

            if (newPasswordField.getText().length() < 8) {
                adminDashboard.showAlert("Error", "New password must be at least 8 characters long");
                return false;
            }

            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                adminDashboard.showAlert("Error", "New passwords do not match");
                return false;
            }
        }

        // Mettre à jour l'utilisateur
        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setEmail(emailField.getText());

        // Mettre à jour l'idGenre
        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) {
            currentUser.setIdGenre(1);
        } else if ("Femme".equals(genre)) {
            currentUser.setIdGenre(2);
        } else {
            currentUser.setIdGenre(3);
        }

        // Mettre à jour le mot de passe si nécessaire
        if (!newPasswordField.getText().isEmpty()) {
            currentUser.setPassword(newPasswordField.getText());
        }

        // Sauvegarder dans la base de données
        if (userService.updateUser(currentUser)) {
            // Mettre à jour l'utilisateur dans la session
            SessionManager.setCurrentUser(currentUser);
            return true;
        } else {
            adminDashboard.showAlert("Error", "An error occurred while updating the profile");
            return false;
        }
    }

    private void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(adminDashboard.getPrimaryStage());
        if (selectedFile != null) {
            try {
                // Créer un dossier pour les photos de profil si nécessaire
                File profileDir = new File("profiles");
                if (!profileDir.exists()) {
                    profileDir.mkdir();
                }

                // Générer un nom de fichier unique
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileExtension = getFileExtension(selectedFile.getName());
                String newFileName = "profile_" + currentUser.getId() + "_" + timestamp + fileExtension;
                File destFile = new File("profiles/" + newFileName);
                Path destPath = destFile.toPath();

                // Copier le fichier
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                // Mettre à jour le chemin de la photo dans l'utilisateur
                currentUser.setPhoto("profiles/" + newFileName);

                // Mettre à jour dans la base de données
                if (userService.updateUser(currentUser)) {
                    adminDashboard.showAlert("Success", "Profile picture updated successfully!");

                    // Mettre à jour l'utilisateur dans la session
                    SessionManager.setCurrentUser(currentUser);

                    // Mettre à jour l'utilisateur dans l'AdminDashboard
                    adminDashboard.setCurrentUser(currentUser);

                    // Mettre à jour l'image de profil dans le header
                    adminDashboard.updateHeaderProfileImage();

                    // Rafraîchir l'affichage pour montrer la nouvelle image
                    showUserProfileView(adminDashboard.getMainContentArea());
                } else {
                    adminDashboard.showAlert("Error", "Error updating profile picture");
                }

            } catch (Exception e) {
                adminDashboard.showAlert("Error", "Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private void styleFormTextField(TextField field) {
        field.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px; -fx-text-fill: #000000;");
        field.setPrefWidth(300);
    }

    private void styleFormTextField(PasswordField field) {
        field.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px; -fx-text-fill: #000000;");
        field.setPrefWidth(300);
    }

    private void styleFormComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        comboBox.setPrefWidth(300);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}