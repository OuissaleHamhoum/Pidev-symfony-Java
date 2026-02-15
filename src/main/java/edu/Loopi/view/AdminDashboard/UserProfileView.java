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
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
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

    public void showUserProfileView(StackPane mainContentArea, boolean isDarkMode) {
        ScrollPane scrollPane = createUserProfileView(isDarkMode);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(scrollPane);
    }

    private String getRoleInFrench(String role) {
        if (role == null) return "";
        switch (role.toLowerCase()) {
            case "admin": return "Administrateur";
            case "organisateur": return "Organisateur";
            case "participant": return "Participant";
            default: return role;
        }
    }

    private ScrollPane createUserProfileView(boolean isDarkMode) {
        VBox container = new VBox(24);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + "; -fx-background-radius: 12; -fx-padding: 24;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("Mon profil");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Gérez vos informations personnelles");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getAccentColor() +
                "; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-size: 14px;");
        backBtn.setOnAction(e -> adminDashboard.showDashboard());

        header.getChildren().addAll(headerText, backBtn);

        VBox profileContent = new VBox(24);
        profileContent.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#F9FAFB") +
                "; -fx-background-radius: 12; -fx-padding: 24; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        HBox topSection = new HBox(32);
        topSection.setAlignment(Pos.CENTER_LEFT);

        VBox avatarBox = new VBox(16);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPrefWidth(200);

        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(60);
        avatarCircle.setFill(Color.web(adminDashboard.getAccentColor()));

        ImageView avatarImageView = adminDashboard.loadProfileImage(currentUser, 120);
        if (avatarImageView != null) {
            avatarContainer.getChildren().add(avatarImageView);
        } else {
            String initials = adminDashboard.getInitials(currentUser);
            Label avatarText = new Label(initials);
            avatarText.setFont(Font.font("System", FontWeight.BOLD, 32));
            avatarText.setTextFill(Color.WHITE);
            avatarContainer.getChildren().addAll(avatarCircle, avatarText);
        }

        Button changeAvatarBtn = new Button("📷 Changer photo");
        changeAvatarBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getAccentColor() +
                "; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-font-size: 13px;");
        changeAvatarBtn.setOnAction(e -> changeProfilePicture());

        avatarBox.getChildren().addAll(avatarContainer, changeAvatarBtn);

        VBox basicInfo = new VBox(16);
        basicInfo.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(currentUser.getNomComplet());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        nameLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        HBox roleBox = new HBox(8);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Label roleLabel = new Label(getRoleInFrench(currentUser.getRole()));
        roleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        roleLabel.setTextFill(Color.WHITE);
        roleLabel.setPadding(new Insets(4, 12, 4, 12));

        String roleColor = currentUser.getRole().equalsIgnoreCase("admin") ? adminDashboard.getAccentColor() :
                currentUser.getRole().equalsIgnoreCase("organisateur") ? adminDashboard.getSuccessColor() :
                        adminDashboard.getWarningColor();
        roleLabel.setStyle("-fx-background-color: " + roleColor + "; -fx-background-radius: 16;");

        Label emailLabel = new Label(currentUser.getEmail());
        emailLabel.setFont(Font.font("System", 16));
        emailLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label memberSinceLabel = new Label("Membre depuis: " +
                (currentUser.getCreatedAt() != null ?
                        currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) :
                        "Date inconnue"));
        memberSinceLabel.setFont(Font.font("System", 13));
        memberSinceLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        roleBox.getChildren().add(roleLabel);
        basicInfo.getChildren().addAll(nameLabel, roleBox, emailLabel, memberSinceLabel);

        topSection.getChildren().addAll(avatarBox, basicInfo);

        Separator separator = new Separator();
        separator.setPadding(new Insets(16, 0, 16, 0));
        separator.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");

        VBox formSection = new VBox(16);
        formSection.setStyle("-fx-background-color: " + (isDarkMode ? adminDashboard.getCardBg() : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-padding: 24; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label formTitle = new Label("Modifier mes informations");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web(adminDashboard.getTextColor()));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(24);
        formGrid.setVgap(16);
        formGrid.setPadding(new Insets(16, 0, 16, 0));

        Label nomLabel = new Label("Nom");
        nomLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        nomLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label prenomLabel = new Label("Prénom");
        prenomLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        prenomLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label emailLabel2 = new Label("Email");
        emailLabel2.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        emailLabel2.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label genreLabel = new Label("Genre");
        genreLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        genreLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label currentPassLabel = new Label("Mot de passe actuel");
        currentPassLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        currentPassLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label newPassLabel = new Label("Nouveau mot de passe");
        newPassLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        newPassLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label confirmPassLabel = new Label("Confirmation");
        confirmPassLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        confirmPassLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextField nomField = new TextField(currentUser.getNom());
        styleFormTextField(nomField, isDarkMode);

        TextField prenomField = new TextField(currentUser.getPrenom());
        styleFormTextField(prenomField, isDarkMode);

        TextField emailField = new TextField(currentUser.getEmail());
        styleFormTextField(emailField, isDarkMode);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Autre");
        if (currentUser.getIdGenre() == 1) genreCombo.setValue("Homme");
        else if (currentUser.getIdGenre() == 2) genreCombo.setValue("Femme");
        else genreCombo.setValue("Autre");
        styleFormComboBox(genreCombo, isDarkMode);

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Mot de passe actuel");
        styleFormTextField(currentPasswordField, isDarkMode);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe (optionnel)");
        styleFormTextField(newPasswordField, isDarkMode);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le nouveau mot de passe");
        styleFormTextField(confirmPasswordField, isDarkMode);

        formGrid.add(nomLabel, 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(prenomLabel, 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(emailLabel2, 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(genreLabel, 0, 3);
        formGrid.add(genreCombo, 1, 3);
        formGrid.add(currentPassLabel, 0, 4);
        formGrid.add(currentPasswordField, 1, 4);
        formGrid.add(newPassLabel, 0, 5);
        formGrid.add(newPasswordField, 1, 5);
        formGrid.add(confirmPassLabel, 0, 6);
        formGrid.add(confirmPasswordField, 1, 6);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(300);
        formGrid.getColumnConstraints().addAll(col1, col2);

        formSection.getChildren().addAll(formTitle, formGrid);

        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(16, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-font-weight: 500; -fx-padding: 8 24; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-font-size: 13px;");
        cancelBtn.setOnAction(e -> adminDashboard.showDashboard());

        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: 600; -fx-padding: 8 24; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        saveBtn.setOnAction(e -> {
            if (saveProfileChanges(nomField, prenomField, emailField, genreCombo,
                    currentPasswordField, newPasswordField, confirmPasswordField)) {
                adminDashboard.showAlert("Succès", "Profil mis à jour avec succès");
                currentUser = userService.getUserById(currentUser.getId());
                SessionManager.setCurrentUser(currentUser);
                adminDashboard.setCurrentUser(currentUser);
                adminDashboard.updateHeaderProfileImage();
                showUserProfileView(adminDashboard.getMainContentArea(), isDarkMode);
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        profileContent.getChildren().addAll(topSection, separator, formSection, buttonBox);
        container.getChildren().addAll(header, profileContent);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private boolean saveProfileChanges(TextField nomField, TextField prenomField, TextField emailField,
                                       ComboBox<String> genreCombo, PasswordField currentPasswordField,
                                       PasswordField newPasswordField, PasswordField confirmPasswordField) {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            adminDashboard.showError("Erreur", "Tous les champs sont requis");
            return false;
        }

        if (!isValidEmail(emailField.getText())) {
            adminDashboard.showError("Erreur", "Format d'email invalide");
            return false;
        }

        if (!emailField.getText().equals(currentUser.getEmail()) && userService.emailExists(emailField.getText())) {
            adminDashboard.showError("Erreur", "Cet email est déjà utilisé");
            return false;
        }

        if (!newPasswordField.getText().isEmpty()) {
            if (currentPasswordField.getText().isEmpty()) {
                adminDashboard.showError("Erreur", "Mot de passe actuel requis");
                return false;
            }

            if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
                adminDashboard.showError("Erreur", "Mot de passe actuel incorrect");
                return false;
            }

            if (newPasswordField.getText().length() < 8) {
                adminDashboard.showError("Erreur", "Le mot de passe doit contenir 8+ caractères");
                return false;
            }

            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                adminDashboard.showError("Erreur", "Les mots de passe ne correspondent pas");
                return false;
            }
        }

        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setEmail(emailField.getText());

        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) currentUser.setIdGenre(1);
        else if ("Femme".equals(genre)) currentUser.setIdGenre(2);
        else currentUser.setIdGenre(3);

        if (!newPasswordField.getText().isEmpty()) {
            currentUser.setPassword(newPasswordField.getText());
        }

        return userService.updateUser(currentUser);
    }

    private void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(adminDashboard.getPrimaryStage());
        if (selectedFile != null) {
            try {
                File profileDir = new File("profiles");
                if (!profileDir.exists()) profileDir.mkdir();

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String extension = getFileExtension(selectedFile.getName());
                String newFileName = "profile_" + currentUser.getId() + "_" + timestamp + extension;
                File destFile = new File("profiles/" + newFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                currentUser.setPhoto("profiles/" + newFileName);

                if (userService.updateUser(currentUser)) {
                    adminDashboard.showAlert("Succès", "Photo mise à jour");
                    SessionManager.setCurrentUser(currentUser);
                    adminDashboard.setCurrentUser(currentUser);
                    adminDashboard.updateHeaderProfileImage();
                    showUserProfileView(adminDashboard.getMainContentArea(), adminDashboard.isDarkMode());
                }
            } catch (Exception e) {
                adminDashboard.showError("Erreur", "Échec du téléchargement");
            }
        }
    }

    private void styleFormTextField(TextField field, boolean isDarkMode) {
        field.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-padding: 8 12; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() + ";");
        field.setPrefWidth(300);
    }

    private void styleFormTextField(PasswordField field, boolean isDarkMode) {
        field.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-padding: 8 12; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() + ";");
        field.setPrefWidth(300);
    }

    private void styleFormComboBox(ComboBox<String> comboBox, boolean isDarkMode) {
        comboBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        comboBox.setPrefWidth(300);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}