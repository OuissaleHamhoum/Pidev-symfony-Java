package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.AuthService;
import edu.Loopi.tools.MyConnection;
import edu.Loopi.view.AdminDashboard.AdminDashboard;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginView extends Application {
    private AuthService authService = new AuthService();
    private Stage primaryStage;

    // Composants UI
    private TextField emailField;
    private PasswordField passwordField;
    private CheckBox rememberMeCheck;
    private Label errorLabel;
    private Button loginButton;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Vérifier la connexion à la base de données
        if (!MyConnection.testConnection()) {
            showAlert("Erreur de connexion",
                    "Impossible de se connecter à la base de données.\n" +
                            "Vérifiez que MySQL est en cours d'exécution.");
            System.exit(1);
        }

        primaryStage.setTitle("LOOPI - Connexion");
        primaryStage.setResizable(false);

        // Créer le layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2E7D32, #4CAF50);");

        // Header avec logo
        HBox headerBox = createHeader();
        root.setTop(headerBox);

        // Formulaire de connexion au centre
        VBox loginForm = createLoginForm();
        StackPane centerPane = new StackPane(loginForm);
        centerPane.setPadding(new Insets(20));
        root.setCenter(centerPane);

        // Footer
        HBox footerBox = createFooter();
        root.setBottom(footerBox);

        Scene scene = new Scene(root, 900, 700);

        // Charger le CSS si disponible
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS non trouvé, utilisation des styles inline");
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        // Vérifier si l'utilisateur a déjà des identifiants enregistrés
        checkRememberedCredentials();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(40, 20, 20, 20));
        header.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(10);
        titleBox.setAlignment(Pos.CENTER);

        Label mainTitle = new Label("LOOPI");
        mainTitle.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
        mainTitle.setTextFill(Color.WHITE);
        mainTitle.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        Label subTitle = new Label("Le carrefour de l'économie circulaire");
        subTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subTitle.setTextFill(Color.WHITE);

        titleBox.getChildren().addAll(mainTitle, subTitle);
        header.getChildren().add(titleBox);

        return header;
    }

    private VBox createLoginForm() {
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(450);

        // Card container
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 30, 30, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: rgba(0,0,0,0.1); -fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 5);");

        // Title
        Label titleLabel = new Label("CONNEXION");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        // Subtitle
        Label subtitleLabel = new Label("Connectez-vous à votre compte LOOPI");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.web("#666"));
        subtitleLabel.setWrapText(true);
        subtitleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Form fields
        VBox formFields = new VBox(15);

        // Email field
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Adresse Email");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web("#555"));

        emailField = new TextField();
        emailField.setPromptText("exemple@email.com");
        emailField.setPrefHeight(45);
        styleTextField(emailField);

        // Add email icon
        HBox emailContainer = new HBox();
        emailContainer.setAlignment(Pos.CENTER_LEFT);
        emailContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 10;");

        Label emailIcon = new Label("✉️");
        emailIcon.setPadding(new Insets(0, 15, 0, 15));

        emailContainer.getChildren().addAll(emailIcon, emailField);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        emailBox.getChildren().addAll(emailLabel, emailContainer);

        // Password field
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Mot de passe");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        passwordLabel.setTextFill(Color.web("#555"));

        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setPrefHeight(45);
        styleTextField(passwordField);

        HBox passwordContainer = new HBox();
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        passwordContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 10;");

        Label passwordIcon = new Label("🔒");
        passwordIcon.setPadding(new Insets(0, 15, 0, 15));

        // Show password toggle
        Button showPasswordBtn = new Button("👁");
        showPasswordBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-cursor: hand; -fx-font-size: 16px;");
        showPasswordBtn.setTooltip(new Tooltip("Afficher le mot de passe"));
        showPasswordBtn.setPadding(new Insets(0, 15, 0, 0));

        // Champ texte pour afficher le mot de passe en clair
        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("••••••••");
        visiblePasswordField.setPrefHeight(45);
        visiblePasswordField.setVisible(false);
        styleTextField(visiblePasswordField);

        // Synchroniser les champs de mot de passe
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        showPasswordBtn.setOnAction(e -> {
            boolean show = !visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(show);
            passwordField.setVisible(!show);
            showPasswordBtn.setText(show ? "👁‍🗨" : "👁");
        });

        passwordContainer.getChildren().addAll(passwordIcon, passwordField, visiblePasswordField, showPasswordBtn);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(visiblePasswordField, Priority.ALWAYS);

        passwordBox.getChildren().addAll(passwordLabel, passwordContainer);

        // Options row
        HBox optionsRow = new HBox();
        optionsRow.setAlignment(Pos.CENTER_LEFT);
        optionsRow.setSpacing(10);

        rememberMeCheck = new CheckBox("Se souvenir de moi");
        rememberMeCheck.setFont(Font.font("Arial", 12));
        rememberMeCheck.setTextFill(Color.web("#666"));

        Hyperlink forgotPasswordLink = new Hyperlink("Mot de passe oublié ?");
        forgotPasswordLink.setFont(Font.font("Arial", 12));
        forgotPasswordLink.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        forgotPasswordLink.setOnAction(e -> openForgotPassword());

        HBox.setHgrow(forgotPasswordLink, Priority.ALWAYS);
        optionsRow.getChildren().addAll(rememberMeCheck, forgotPasswordLink);

        // Error label
        errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        // Login button
        loginButton = new Button("SE CONNECTER");
        loginButton.setPrefHeight(50);
        loginButton.setPrefWidth(350);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; " +
                "-fx-cursor: hand;");
        loginButton.setOnAction(e -> handleLogin());

        // Hover effect pour le bouton
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        });

        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; " +
                    "-fx-cursor: hand;");
        });

        // Add enter key support
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        visiblePasswordField.setOnAction(e -> handleLogin());

        // Divider
        HBox divider = new HBox();
        divider.setPrefHeight(20);

        Label orLabel = new Label("OU");
        orLabel.setFont(Font.font("Arial", 12));
        orLabel.setTextFill(Color.web("#999"));

        HBox orContainer = new HBox();
        orContainer.setAlignment(Pos.CENTER);
        orContainer.setSpacing(10);

        Region line1 = new Region();
        line1.setPrefHeight(1);
        line1.setStyle("-fx-background-color: #e0e0e0;");
        HBox.setHgrow(line1, Priority.ALWAYS);

        Region line2 = new Region();
        line2.setPrefHeight(1);
        line2.setStyle("-fx-background-color: #e0e0e0;");
        HBox.setHgrow(line2, Priority.ALWAYS);

        orContainer.getChildren().addAll(line1, orLabel, line2);

        // Social login options (simplifié)
        VBox socialLoginBox = new VBox(10);
        socialLoginBox.setAlignment(Pos.CENTER);

        Label socialLabel = new Label("Connectez-vous rapidement avec");
        socialLabel.setFont(Font.font("Arial", 12));
        socialLabel.setTextFill(Color.web("#666"));

        HBox socialButtons = new HBox(15);
        socialButtons.setAlignment(Pos.CENTER);

        // Créer des boutons sociaux simplifiés
        Button googleButton = createSocialButton("G", "#DB4437");
        Button facebookButton = createSocialButton("f", "#4267B2");
        Button twitterButton = createSocialButton("𝕏", "#1DA1F2");

        socialButtons.getChildren().addAll(googleButton, facebookButton, twitterButton);
        socialLoginBox.getChildren().addAll(socialLabel, socialButtons);

        // Register section
        VBox registerSection = new VBox(10);
        registerSection.setAlignment(Pos.CENTER);

        Label noAccountLabel = new Label("Vous n'avez pas de compte ?");
        noAccountLabel.setFont(Font.font("Arial", 12));
        noAccountLabel.setTextFill(Color.web("#666"));

        // Utiliser une méthode simple pour créer le bouton d'inscription
        Button registerButton = new Button("S'INSCRIRE");
        registerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; " +
                "-fx-font-weight: bold; -fx-border-color: #4CAF50; -fx-border-radius: 8; " +
                "-fx-padding: 10 25; -fx-cursor: hand;");
        registerButton.setOnAction(e -> openRegister());

        registerButton.setOnMouseEntered(e -> registerButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-border-color: #4CAF50; -fx-border-radius: 8; " +
                        "-fx-padding: 10 25; -fx-cursor: hand;"
        ));

        registerButton.setOnMouseExited(e -> registerButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4CAF50; " +
                        "-fx-font-weight: bold; -fx-border-color: #4CAF50; -fx-border-radius: 8; " +
                        "-fx-padding: 10 25; -fx-cursor: hand;"
        ));

        registerSection.getChildren().addAll(noAccountLabel, registerButton);

        // Assemble form
        formFields.getChildren().addAll(
                emailBox, passwordBox, optionsRow, errorLabel, loginButton
        );

        card.getChildren().addAll(
                titleLabel, subtitleLabel, formFields,
                orContainer, socialLoginBox, divider, registerSection
        );

        formContainer.getChildren().add(card);
        return formContainer;
    }

    private Button createSocialButton(String icon, String color) {
        Button button = new Button(icon);
        button.setPrefSize(45, 45);
        button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 50%%; -fx-font-size: 16px; -fx-cursor: hand;",
                color
        ));

        button.setOnMouseEntered(e -> button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 50%%; -fx-font-size: 16px; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);",
                color
        )));

        button.setOnMouseExited(e -> button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 50%%; -fx-font-size: 16px; -fx-cursor: hand;",
                color
        )));

        button.setOnAction(e -> {
            showAlert("Connexion sociale",
                    "La connexion avec les réseaux sociaux sera disponible prochainement.");
        });

        return button;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);

        Label footerText = new Label("© 2024 LOOPI - Plateforme d'économie circulaire");
        footerText.setFont(Font.font("Arial", 10));
        footerText.setTextFill(Color.WHITE);

        footer.getChildren().add(footerText);
        return footer;
    }

    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-padding: 0 10;");
        field.setPrefWidth(300);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Adresse email invalide");
            return;
        }

        // Désactiver le bouton pendant la vérification
        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours...");

        // Simuler un délai réseau (pour l'UI)
        new Thread(() -> {
            try {
                Thread.sleep(800); // Simulation de délai réseau

                javafx.application.Platform.runLater(() -> {
                    User user = authService.login(email, password);

                    if (user != null) {
                        System.out.println("✅ Connexion réussie pour: " + user.getEmail());

                        // Sauvegarder les identifiants si "Se souvenir de moi" est coché
                        if (rememberMeCheck.isSelected()) {
                            saveCredentials(email, password);
                        }

                        // Ouvrir le dashboard approprié
                        openDashboard(user);
                    } else {
                        showError("Email ou mot de passe incorrect");
                        loginButton.setDisable(false);
                        loginButton.setText("SE CONNECTER");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur lors de la connexion");
                    loginButton.setDisable(false);
                    loginButton.setText("SE CONNECTER");
                });
            }
        }).start();
    }

    private boolean isValidEmail(String email) {
        // Validation simple d'email
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Auto-hide error after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setVisible(false);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void saveCredentials(String email, String password) {
        // Note: En production, utilisez un stockage sécurisé
        // Pour la démo, on utilise simplement les préférences
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LoginView.class);
            prefs.put("remembered_email", email);
            // Attention: Ne jamais stocker les mots de passe en clair en production!
            prefs.put("remembered_password", password);
        } catch (Exception e) {
            System.out.println("⚠️ Impossible de sauvegarder les identifiants");
        }
    }

    private void checkRememberedCredentials() {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LoginView.class);
            String savedEmail = prefs.get("remembered_email", "");
            String savedPassword = prefs.get("remembered_password", "");

            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                emailField.setText(savedEmail);
                passwordField.setText(savedPassword);
                rememberMeCheck.setSelected(true);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Impossible de charger les identifiants sauvegardés");
        }
    }

    private void openDashboard(User user) {
        // Fermer la fenêtre de login
        primaryStage.close();

        // Ouvrir la fenêtre de dashboard
        Stage dashboardStage = new Stage();

        try {
            String role = user.getRole().toLowerCase();

            switch (role) {
                case "admin":
                    AdminDashboard adminDashboard = new AdminDashboard(user);
                    adminDashboard.start(dashboardStage);
                    break;

                case "organisateur":
                    // OrganizerDashboard organizerDashboard = new OrganizerDashboard(user);
                    // organizerDashboard.start(dashboardStage);
                    showAlert("Dashboard Organisateur", "Le dashboard organisateur n'est pas encore implémenté.");
                    new LoginView().start(new Stage());
                    break;

                case "participant":
                    // UserDashboard userDashboard = new UserDashboard(user);
                    // userDashboard.start(dashboardStage);
                    showAlert("Dashboard Participant", "Le dashboard participant n'est pas encore implémenté.");
                    new LoginView().start(new Stage());
                    break;

                default:
                    showAlert("Erreur", "Rôle non reconnu: " + role);
                    // Revenir au login
                    new LoginView().start(new Stage());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ouverture du dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le dashboard");
            new LoginView().start(new Stage());
        }
    }

    private void openForgotPassword() {
        // Cette méthode peut être implémentée plus tard
        showAlert("Mot de passe oublié",
                "Veuillez contacter l'administrateur pour réinitialiser votre mot de passe.");
    }

    private void openRegister() {
        // Créer une fenêtre d'inscription simplifiée directement
        Stage registerStage = new Stage();
        registerStage.setTitle("LOOPI - Inscription");

        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2E7D32, #4CAF50);");

        // Card
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        card.setMaxWidth(400);

        Label title = new Label("Inscription");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2E7D32"));

        Label message = new Label("L'inscription complète sera disponible bientôt.\n\n" +
                "Pour le moment, vous pouvez utiliser ces comptes de test:\n\n" +
                "👑 Admin: admin@loopi.tn / admin123\n" +
                "🎯 Organisateur: organisateur@loopi.tn / org123\n" +
                "😊 Participant: participant@loopi.tn / part123");
        message.setFont(Font.font("Arial", 14));
        message.setTextFill(Color.web("#666"));
        message.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        message.setWrapText(true);

        Button backButton = new Button("Retour à la connexion");
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        backButton.setOnAction(e -> {
            registerStage.close();
        });

        card.getChildren().addAll(title, message, backButton);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 500, 450);
        registerStage.setScene(scene);
        registerStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}