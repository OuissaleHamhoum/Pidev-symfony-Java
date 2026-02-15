package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RegisterView {
    private Stage registerStage;

    private TextField prenomField;
    private TextField nomField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<String> roleCombo;
    private ComboBox<String> genreCombo;
    private Label errorLabel;
    private Label emailSuggestionLabel;
    private Button registerBtn;

    private UserService userService;

    public RegisterView() {
        this.userService = new UserService();
    }

    public void show(Stage parentStage) {
        registerStage = new Stage();
        registerStage.setTitle("Loopi - Créer un compte");
        registerStage.initModality(Modality.WINDOW_MODAL);
        registerStage.initOwner(parentStage);
        registerStage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #4CAF50;");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Formulaire
        ScrollPane scrollPane = createRegisterForm();
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 400, 680);
        registerStage.setScene(scene);
        registerStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox(2);
        header.setPadding(new Insets(30, 20, 15, 20));
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Loopi");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Créer un compte");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.WHITE);

        Label subsubtitle = new Label("Rejoignez la communauté Loopi");
        subsubtitle.setFont(Font.font("Arial", 12));
        subsubtitle.setTextFill(Color.WHITE);

        header.getChildren().addAll(title, subtitle, subsubtitle);
        return header;
    }

    private ScrollPane createRegisterForm() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10, 30, 20, 30));

        VBox card = new VBox(12);
        card.setMaxWidth(320);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white;");

        // Prénom avec contrôle
        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        prenomField = new TextField();
        prenomField.setPromptText("Votre prénom");
        prenomField.setPrefHeight(35);
        prenomField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");
        prenomField.textProperty().addListener((obs, old, newVal) -> generateEmailSuggestion());

        // Nom avec contrôle
        Label nomLabel = new Label("Nom *");
        nomLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nomField = new TextField();
        nomField.setPromptText("Votre nom");
        nomField.setPrefHeight(35);
        nomField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");
        nomField.textProperty().addListener((obs, old, newVal) -> generateEmailSuggestion());

        // Email avec suggestion
        Label emailLabel = new Label("Email *");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox emailBox = new HBox(5);
        emailBox.setAlignment(Pos.CENTER_LEFT);

        emailField = new TextField();
        emailField.setPromptText("votre.email@exemple.com");
        emailField.setPrefHeight(35);
        emailField.setPrefWidth(220);
        emailField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");

        Button suggestBtn = new Button("Suggérer");
        suggestBtn.setStyle("-fx-background-color: #E0E0E0; -fx-cursor: hand;");
        suggestBtn.setPrefHeight(35);
        suggestBtn.setOnAction(e -> applyEmailSuggestion());

        emailBox.getChildren().addAll(emailField, suggestBtn);

        // Label de suggestion
        emailSuggestionLabel = new Label();
        emailSuggestionLabel.setFont(Font.font("Arial", 10));
        emailSuggestionLabel.setTextFill(Color.GREEN);
        emailSuggestionLabel.setVisible(false);

        // Mot de passe
        Label passwordLabel = new Label("Mot de passe *");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 6 caractères");
        passwordField.setPrefHeight(35);
        passwordField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");

        // Confirmer
        Label confirmLabel = new Label("Confirmer *");
        confirmLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Retapez votre mot de passe");
        confirmPasswordField.setPrefHeight(35);
        confirmPasswordField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");

        // Force du mot de passe
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefHeight(5);
        strengthBar.setMaxWidth(Double.MAX_VALUE);
        strengthBar.setVisible(false);

        Label strengthLabel = new Label();
        strengthLabel.setFont(Font.font("Arial", 10));

        passwordField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty()) {
                strengthBar.setVisible(true);
                int strength = calculatePasswordStrength(newVal);
                strengthBar.setProgress(strength / 100.0);

                if (strength < 30) {
                    strengthBar.setStyle("-fx-accent: #F44336;");
                    strengthLabel.setText("Faible");
                    strengthLabel.setTextFill(Color.RED);
                } else if (strength < 60) {
                    strengthBar.setStyle("-fx-accent: #FF9800;");
                    strengthLabel.setText("Moyen");
                    strengthLabel.setTextFill(Color.ORANGE);
                } else {
                    strengthBar.setStyle("-fx-accent: #4CAF50;");
                    strengthLabel.setText("Fort");
                    strengthLabel.setTextFill(Color.GREEN);
                }
            } else {
                strengthBar.setVisible(false);
                strengthLabel.setText("");
            }
        });

        // Rôle
        Label roleLabel = new Label("Rôle");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox roleBox = new HBox(5);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Button prevRoleBtn = new Button("◀");
        prevRoleBtn.setPrefSize(30, 35);
        prevRoleBtn.setStyle("-fx-background-color: #E0E0E0; -fx-cursor: hand;");

        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Participant", "Organisateur", "Admin");
        roleCombo.setValue("Participant");
        roleCombo.setPrefHeight(35);
        roleCombo.setPrefWidth(180);
        roleCombo.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");

        Button nextRoleBtn = new Button("▶");
        nextRoleBtn.setPrefSize(30, 35);
        nextRoleBtn.setStyle("-fx-background-color: #E0E0E0; -fx-cursor: hand;");

        prevRoleBtn.setOnAction(e -> {
            int idx = roleCombo.getSelectionModel().getSelectedIndex();
            if (idx > 0) roleCombo.getSelectionModel().select(idx - 1);
        });

        nextRoleBtn.setOnAction(e -> {
            int idx = roleCombo.getSelectionModel().getSelectedIndex();
            if (idx < roleCombo.getItems().size() - 1) roleCombo.getSelectionModel().select(idx + 1);
        });

        roleBox.getChildren().addAll(prevRoleBtn, roleCombo, nextRoleBtn);

        // Genre (ComboBox depuis la BD)
        Label genreLabel = new Label("Genre");
        genreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue("Non spécifié");
        genreCombo.setPrefHeight(35);
        genreCombo.setMaxWidth(Double.MAX_VALUE);
        genreCombo.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");

        // Bouton
        registerBtn = new Button("Créer mon compte");
        registerBtn.setPrefHeight(40);
        registerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> handleRegister());

        // Lien connexion
        HBox loginLink = new HBox(5);
        loginLink.setAlignment(Pos.CENTER);

        Label haveAccountLabel = new Label("Déjà un compte?");
        haveAccountLabel.setFont(Font.font("Arial", 12));
        haveAccountLabel.setTextFill(Color.GRAY);

        Hyperlink loginHyperlink = new Hyperlink("Se connecter");
        loginHyperlink.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        loginHyperlink.setTextFill(Color.web("#4CAF50"));
        loginHyperlink.setOnAction(e -> registerStage.close());

        loginLink.getChildren().addAll(haveAccountLabel, loginHyperlink);

        // Erreur
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(
                prenomLabel, prenomField,
                nomLabel, nomField,
                emailLabel, emailBox, emailSuggestionLabel,
                passwordLabel, passwordField, strengthBar, strengthLabel,
                confirmLabel, confirmPasswordField,
                roleLabel, roleBox,
                genreLabel, genreCombo,
                registerBtn,
                loginLink,
                errorLabel
        );

        container.getChildren().add(card);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    // Générer une suggestion d'email basée sur prénom.nom@loopi.tn
    private void generateEmailSuggestion() {
        String prenom = prenomField.getText().trim().toLowerCase();
        String nom = nomField.getText().trim().toLowerCase();

        if (!prenom.isEmpty() && !nom.isEmpty()) {
            // Nettoyer les caractères spéciaux
            prenom = prenom.replaceAll("[^a-z]", "");
            nom = nom.replaceAll("[^a-z]", "");

            if (!prenom.isEmpty() && !nom.isEmpty()) {
                String suggestion = prenom + "." + nom + "@loopi.tn";
                emailSuggestionLabel.setText("Suggestion: " + suggestion);
                emailSuggestionLabel.setVisible(true);
            } else {
                emailSuggestionLabel.setVisible(false);
            }
        } else {
            emailSuggestionLabel.setVisible(false);
        }
    }

    // Appliquer la suggestion d'email
    private void applyEmailSuggestion() {
        String prenom = prenomField.getText().trim().toLowerCase();
        String nom = nomField.getText().trim().toLowerCase();

        if (!prenom.isEmpty() && !nom.isEmpty()) {
            prenom = prenom.replaceAll("[^a-z]", "");
            nom = nom.replaceAll("[^a-z]", "");

            if (!prenom.isEmpty() && !nom.isEmpty()) {
                String suggestion = prenom + "." + nom + "@loopi.tn";
                emailField.setText(suggestion);
            }
        }
    }

    // Calculer la force du mot de passe
    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 6) strength += 20;
        if (password.length() >= 8) strength += 15;
        if (password.matches(".*[A-Z].*")) strength += 20;
        if (password.matches(".*[0-9].*")) strength += 20;
        if (password.matches(".*[!@#$%^&*].*")) strength += 25;
        return Math.min(strength, 100);
    }

    private void handleRegister() {
        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleCombo.getValue().toLowerCase();
        String genre = genreCombo.getValue();

        // Map genre vers id_genre
        int idGenre = 3; // Non spécifié par défaut
        if ("Homme".equals(genre)) idGenre = 1;
        else if ("Femme".equals(genre)) idGenre = 2;

        // Validation
        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Tous les champs marqués * sont obligatoires");
            return;
        }

        // Validation prénom (lettres uniquement)
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s-]{2,}")) {
            showError("Prénom invalide (minimum 2 lettres)");
            return;
        }

        // Validation nom (lettres uniquement)
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s-]{2,}")) {
            showError("Nom invalide (minimum 2 lettres)");
            return;
        }

        // Validation email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showError("Format d'email invalide");
            return;
        }

        // Vérifier si l'email existe déjà
        if (userService.emailExists(email)) {
            showError("Cet email est déjà utilisé");
            return;
        }

        // Validation mot de passe
        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        // Créer l'utilisateur
        User newUser = new User(nom, prenom, email, password, role);
        newUser.setIdGenre(idGenre);
        newUser.setPhoto("default.jpg");

        // Désactiver le bouton
        registerBtn.setDisable(true);
        registerBtn.setText("Création en cours...");

        new Thread(() -> {
            try {
                Thread.sleep(800);
                javafx.application.Platform.runLater(() -> {
                    boolean success = userService.addUser(newUser);

                    if (success) {
                        showSuccess("Compte créé avec succès !\nBienvenue " + prenom + " !\nVous pouvez maintenant vous connecter.");
                        registerStage.close();
                    } else {
                        showError("Erreur lors de la création du compte");
                        registerBtn.setDisable(false);
                        registerBtn.setText("Créer mon compte");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}