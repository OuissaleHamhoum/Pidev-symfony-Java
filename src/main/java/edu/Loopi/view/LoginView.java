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

    private TextField emailField;
    private PasswordField passwordField;
    private CheckBox rememberMeCheck;
    private Label errorLabel;
    private Button loginButton;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Test de connexion à la base de données
        if (!MyConnection.testConnection()) {
            showAlert("Erreur de connexion",
                    "Impossible de se connecter à la base de données.\n" +
                            "Vérifiez que MySQL est en cours d'exécution.\n\n" +
                            "URL: jdbc:mysql://localhost:3306/loopi_db\n" +
                            "User: root\n" +
                            "Password: (vide)");
            System.exit(1);
        }

        primaryStage.setTitle("LOOPI - Connexion");
        primaryStage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2E7D32, #4CAF50);");

        HBox headerBox = createHeader();
        root.setTop(headerBox);

        VBox loginForm = createLoginForm();
        StackPane centerPane = new StackPane(loginForm);
        centerPane.setPadding(new Insets(20));
        root.setCenter(centerPane);

        HBox footerBox = createFooter();
        root.setBottom(footerBox);

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Charger les identifiants sauvegardés
        checkRememberedCredentials();

        System.out.println("✅ LoginView démarré avec succès");
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

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 30, 30, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: rgba(0,0,0,0.1); -fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 5);");

        Label titleLabel = new Label("CONNEXION");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        Label subtitleLabel = new Label("Connectez-vous à votre compte LOOPI");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.web("#666"));
        subtitleLabel.setWrapText(true);
        subtitleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox formFields = new VBox(15);

        // Champ Email
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Adresse Email");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web("#555"));

        emailField = new TextField();
        emailField.setPromptText("exemple@email.com");
        emailField.setPrefHeight(45);
        styleTextField(emailField);

        HBox emailContainer = new HBox();
        emailContainer.setAlignment(Pos.CENTER_LEFT);
        emailContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 10;");

        Label emailIcon = new Label("✉️");
        emailIcon.setPadding(new Insets(0, 15, 0, 15));

        emailContainer.getChildren().addAll(emailIcon, emailField);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        emailBox.getChildren().addAll(emailLabel, emailContainer);

        // Champ Mot de passe
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

        Button showPasswordBtn = new Button("👁");
        showPasswordBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-cursor: hand; -fx-font-size: 16px;");
        showPasswordBtn.setTooltip(new Tooltip("Afficher le mot de passe"));
        showPasswordBtn.setPadding(new Insets(0, 15, 0, 0));

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("••••••••");
        visiblePasswordField.setPrefHeight(45);
        visiblePasswordField.setVisible(false);
        styleTextField(visiblePasswordField);

        // Lier les deux champs de mot de passe
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        showPasswordBtn.setOnAction(e -> {
            boolean show = !visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(show);
            passwordField.setVisible(!show);
            showPasswordBtn.setText(show ? "👁‍🗨" : "👁");

            // Transférer le focus
            if (show) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        });

        passwordContainer.getChildren().addAll(passwordIcon, passwordField, visiblePasswordField, showPasswordBtn);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(visiblePasswordField, Priority.ALWAYS);

        passwordBox.getChildren().addAll(passwordLabel, passwordContainer);

        // Options (Se souvenir de moi + Mot de passe oublié)
        HBox optionsRow = new HBox();
        optionsRow.setAlignment(Pos.CENTER_LEFT);
        optionsRow.setSpacing(10);

        rememberMeCheck = new CheckBox("Se souvenir de moi");
        rememberMeCheck.setFont(Font.font("Arial", 12));
        rememberMeCheck.setTextFill(Color.web("#666"));

        Hyperlink forgotPasswordLink = new Hyperlink("Mot de passe oublié ?");
        forgotPasswordLink.setFont(Font.font("Arial", 12));
        forgotPasswordLink.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-underline: true;");
        forgotPasswordLink.setOnAction(e -> openForgotPassword());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        optionsRow.getChildren().addAll(rememberMeCheck, spacer, forgotPasswordLink);

        // Label d'erreur
        errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setMaxWidth(350);

        // Bouton de connexion
        loginButton = new Button("SE CONNECTER");
        loginButton.setPrefHeight(50);
        loginButton.setPrefWidth(350);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; " +
                "-fx-cursor: hand;");
        loginButton.setOnAction(e -> handleLogin());

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

        // Actions des touches Entrée
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        visiblePasswordField.setOnAction(e -> handleLogin());

        // Séparateur "OU"
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

        // Connexion sociale
        VBox socialLoginBox = new VBox(10);
        socialLoginBox.setAlignment(Pos.CENTER);

        Label socialLabel = new Label("Connectez-vous rapidement avec");
        socialLabel.setFont(Font.font("Arial", 12));
        socialLabel.setTextFill(Color.web("#666"));

        HBox socialButtons = new HBox(15);
        socialButtons.setAlignment(Pos.CENTER);

        Button googleButton = createSocialButton("G", "#DB4437");
        Button facebookButton = createSocialButton("f", "#4267B2");
        Button twitterButton = createSocialButton("𝕏", "#1DA1F2");

        socialButtons.getChildren().addAll(googleButton, facebookButton, twitterButton);
        socialLoginBox.getChildren().addAll(socialLabel, socialButtons);

        // Section inscription
        VBox registerSection = new VBox(10);
        registerSection.setAlignment(Pos.CENTER);

        Label noAccountLabel = new Label("Vous n'avez pas de compte ?");
        noAccountLabel.setFont(Font.font("Arial", 12));
        noAccountLabel.setTextFill(Color.web("#666"));

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

        // Assemblage du formulaire
        formFields.getChildren().addAll(
                emailBox,
                passwordBox,
                optionsRow,
                errorLabel,
                loginButton
        );

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                formFields,
                orContainer,
                socialLoginBox,
                divider,
                registerSection
        );

        formContainer.getChildren().add(card);
        return formContainer;
    }

    private Button createSocialButton(String icon, String color) {
        Button button = new Button(icon);
        button.setPrefSize(45, 45);
        button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 50%%; -fx-font-size: 18px; -fx-cursor: hand;",
                color
        ));

        button.setOnMouseEntered(e -> button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 50%%; -fx-font-size: 18px; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);",
                color
        )));

        button.setOnMouseExited(e -> button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 50%%; -fx-font-size: 18px; -fx-cursor: hand;",
                color
        )));

        button.setOnAction(e -> {
            showAlert("Connexion sociale",
                    "La connexion avec les réseaux sociaux sera disponible prochainement.\n\n" +
                            "Pour le moment, utilisez les comptes de test:\n" +
                            "• Admin: admin@loopi.tn / admin123\n" +
                            "• Organisateur: organisateur@loopi.tn / org123\n" +
                            "• Participant: participant@loopi.tn / part123");
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
                "-fx-font-size: 14px; -fx-padding: 0 10; -fx-text-fill: #333;");
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

        // Désactiver le bouton pendant la connexion
        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours...");

        // Simuler un délai de chargement (optionnel)
        new Thread(() -> {
            try {
                Thread.sleep(500);

                javafx.application.Platform.runLater(() -> {
                    try {
                        User user = authService.login(email, password);

                        if (user != null) {
                            System.out.println("✅ Connexion réussie pour: " + user.getEmail());
                            System.out.println("   Rôle: " + user.getRole());
                            System.out.println("   ID: " + user.getId());

                            // Sauvegarder les identifiants si "Se souvenir de moi" est coché
                            if (rememberMeCheck.isSelected()) {
                                saveCredentials(email, password);
                            } else {
                                clearSavedCredentials();
                            }

                            // Ouvrir le dashboard approprié
                            openDashboard(user);

                        } else {
                            showError("Email ou mot de passe incorrect");
                            resetLoginButton();
                        }
                    } catch (Exception ex) {
                        System.err.println("❌ Erreur lors de la connexion: " + ex.getMessage());
                        ex.printStackTrace();
                        showError("Erreur de connexion: " + ex.getMessage());
                        resetLoginButton();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur lors de la connexion");
                    resetLoginButton();
                });
            }
        }).start();
    }

    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("SE CONNECTER");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Animation pour attirer l'attention
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        // Masquer automatiquement après 5 secondes
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
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LoginView.class);
            prefs.put("remembered_email", email);
            prefs.put("remembered_password", password);
            System.out.println("💾 Identifiants sauvegardés");
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de sauvegarder les identifiants: " + e.getMessage());
        }
    }

    private void clearSavedCredentials() {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LoginView.class);
            prefs.remove("remembered_email");
            prefs.remove("remembered_password");
            System.out.println("🗑️ Identifiants effacés");
        } catch (Exception e) {
            System.err.println("⚠️ Impossible d'effacer les identifiants: " + e.getMessage());
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
                System.out.println("🔑 Identifiants sauvegardés chargés pour: " + savedEmail);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de charger les identifiants sauvegardés: " + e.getMessage());
        }
    }

    private void openDashboard(User user) {
        primaryStage.close();

        Stage dashboardStage = new Stage();

        try {
            String role = user.getRole().toLowerCase();

            switch (role) {
                case "admin":
                    try {
                        // Vérifier que la classe AdminDashboard existe
                        Class.forName("edu.Loopi.view.AdminDashboard.AdminDashboard");
                        AdminDashboard adminDashboard = new AdminDashboard(user);
                        adminDashboard.start(dashboardStage);
                        System.out.println("✅ Dashboard Admin ouvert");
                    } catch (ClassNotFoundException e) {
                        System.err.println("❌ Classe AdminDashboard non trouvée!");
                        showAlert("Erreur", "Le module administrateur n'est pas installé correctement.");
                        returnToLogin();
                    }
                    break;

                case "organisateur":
                    try {
                        // Vérifier que la classe OrganizerDashboard existe
                        Class.forName("edu.Loopi.view.OrganizerDashboard");
                        OrganizerDashboard organizerDashboard = new OrganizerDashboard(user);
                        organizerDashboard.start(dashboardStage);
                        System.out.println("✅ Dashboard Organisateur ouvert");
                    } catch (ClassNotFoundException e) {
                        System.err.println("❌ Classe OrganizerDashboard non trouvée!");
                        System.err.println("   Assurez-vous que le fichier OrganizerDashboard.java existe dans edu.Loopi.view");

                        // Solution de secours: essayer avec le nom alternatif
                        try {
                            Class.forName("edu.Loopi.view.OrganizerDashboard");
                            // Si on arrive ici, c'est que la classe existe
                            OrganizerDashboard organizerDashboard = new OrganizerDashboard(user);
                            organizerDashboard.start(dashboardStage);
                        } catch (ClassNotFoundException ex2) {
                            showAlert("Erreur",
                                    "Le module organisateur n'est pas installé correctement.\n\n" +
                                            "Veuillez créer la classe OrganizerDashboard.java dans le package edu.Loopi.view\n" +
                                            "ou contacter le développeur.");
                            returnToLogin();
                        } catch (Exception ex2) {
                            showAlert("Erreur", "Erreur lors de l'ouverture du dashboard: " + ex2.getMessage());
                            returnToLogin();
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors de l'ouverture du dashboard organisateur:");
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible d'ouvrir le dashboard organisateur:\n" + e.getMessage());
                        returnToLogin();
                    }
                    break;

                // Dans la méthode openDashboard(User user), remplacer le bloc case "participant":

                case "participant":
                    try {
                        Class.forName("edu.Loopi.view.UserDashboard");
                        UserDashboard userDashboard = new UserDashboard(user);
                        userDashboard.start(dashboardStage);
                        System.out.println("✅ Dashboard Participant ouvert avec gestion des événements");
                    } catch (ClassNotFoundException e) {
                        System.err.println("❌ Classe UserDashboard non trouvée!");
                        showAlert("Erreur", "Le module participant n'est pas installé correctement.");
                        returnToLogin();
                    }
                    break;

                default:
                    showAlert("Erreur", "Rôle non reconnu: " + role);
                    returnToLogin();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur générale lors de l'ouverture du dashboard:");
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le dashboard:\n" + e.getMessage());
            returnToLogin();
        }
    }

    private void returnToLogin() {
        try {
            new LoginView().start(new Stage());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du retour à la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openForgotPassword() {
        try {
            Class.forName("edu.Loopi.view.ForgotPasswordView");
            ForgotPasswordView forgotView = new ForgotPasswordView();
            forgotView.start(primaryStage);
        } catch (ClassNotFoundException e) {
            showAlert("Mot de passe oublié",
                    "Fonctionnalité en cours de développement.\n\n" +
                            "Pour réinitialiser votre mot de passe, veuillez contacter l'administrateur:\n" +
                            "admin@loopi.tn");
        }
    }

    private void openRegister() {
        Stage registerStage = new Stage();
        registerStage.setTitle("LOOPI - Inscription");

        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2E7D32, #4CAF50);");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 5);");
        card.setMaxWidth(450);

        Label title = new Label("Inscription");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2E7D32"));

        Label message = new Label(
                "L'inscription complète sera disponible prochainement.\n\n" +
                        "📋 Pour le moment, vous pouvez utiliser ces comptes de test:\n\n" +
                        "👑 ADMIN\n" +
                        "   Email: admin@loopi.tn\n" +
                        "   Mot de passe: admin123\n\n" +
                        "🎯 ORGANISATEUR\n" +
                        "   Email: organisateur@loopi.tn\n" +
                        "   Mot de passe: org123\n\n" +
                        "😊 PARTICIPANT\n" +
                        "   Email: participant@loopi.tn\n" +
                        "   Mot de passe: part123"
        );
        message.setFont(Font.font("Arial", 14));
        message.setTextFill(Color.web("#333"));
        message.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        message.setWrapText(true);

        Button backButton = new Button("Retour à la connexion");
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        backButton.setPrefWidth(250);

        backButton.setOnMouseEntered(e -> backButton.setStyle(
                "-fx-background-color: #388E3C; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));

        backButton.setOnMouseExited(e -> backButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));

        backButton.setOnAction(e -> registerStage.close());

        card.getChildren().addAll(title, message, backButton);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 550, 550);
        registerStage.setScene(scene);
        registerStage.initOwner(primaryStage);
        registerStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}