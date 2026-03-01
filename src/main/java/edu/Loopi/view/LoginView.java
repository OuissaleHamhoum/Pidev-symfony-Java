package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.tools.MyConnection;
import edu.Loopi.tools.SessionManager;
import edu.Loopi.view.AdminDashboard.AdminDashboard;
import edu.Loopi.services.AuthService;
import edu.Loopi.services.GoogleAuthService;
import edu.Loopi.services.RealtimeValidationService;
import edu.Loopi.services.CameraService;
import edu.Loopi.services.QRLoginService;
import edu.Loopi.services.QRCodeWebServer;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoginView extends Application {

    private AuthService authService = new AuthService();
    private GoogleAuthService googleAuthService;
    private RealtimeValidationService validationService = new RealtimeValidationService();
    private CameraService cameraService;
    private QRLoginService qrLoginService;

    private Stage primaryStage;

    private TextField emailField;
    private PasswordField passwordField;
    private TextField visiblePasswordField;
    private CheckBox showPasswordCheckBox;
    private Label emailErrorLabel;
    private Label passwordStrengthLabel;
    private Label loginErrorLabel;
    private Button loginBtn;
    private Button googleBtn;
    private Button faceLoginBtn;
    private Button qrLoginBtn;
    private boolean isPasswordVisible = false;

    // Toggle buttons
    private Button loginToggleBtn;
    private Button registerToggleBtn;
    private HBox toggleBox;
    private boolean isLoginMode = true;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.googleAuthService = new GoogleAuthService(authService);
        this.cameraService = new CameraService();
        this.qrLoginService = new QRLoginService();

        if (!MyConnection.testConnection()) {
            showErrorAlert("Erreur de connexion",
                    "Impossible de se connecter à la base de données.\nVérifiez que MySQL est démarré.");
            System.exit(1);
        }

        primaryStage.setTitle("Loopi - Connexion");
        primaryStage.setResizable(false);

        // Conteneur principal avec les deux colonnes
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");

        // Colonne gauche avec animation réseau
        VBox leftColumn = createLeftColumn();
        leftColumn.setPrefWidth(350);

        // Colonne droite avec formulaire
        VBox rightColumn = createRightColumn();
        rightColumn.setPrefWidth(400);

        root.getChildren().addAll(leftColumn, rightColumn);

        Scene scene = new Scene(root, 750, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Animation d'entrée
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        setupRealtimeValidation();
    }

    private VBox createLeftColumn() {
        VBox leftColumn = new VBox(15);
        leftColumn.setStyle("-fx-background-color: linear-gradient(to bottom, #0f172a, #1e293b);");
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setPadding(new Insets(25, 15, 15, 15));

        // Animation Network Connections
        Pane networkPane = createNetworkAnimation();
        networkPane.setPrefHeight(180);
        networkPane.setMaxHeight(180);
        networkPane.setMinHeight(180);

        // Titre
        Label title = new Label("Loopi");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Économie Circulaire & Solidarité");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(Color.web("#94a3b8"));
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setMaxWidth(280);

        // Espace flexible
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logos partenaires en bas
        HBox partnersBox = createPartnersLogo();
        partnersBox.setAlignment(Pos.CENTER);
        partnersBox.setPadding(new Insets(10, 0, 5, 0));

        leftColumn.getChildren().addAll(networkPane, title, subtitle, spacer, partnersBox);

        return leftColumn;
    }

    private Pane createNetworkAnimation() {
        Pane pane = new Pane();
        pane.setPrefSize(300, 170);

        Random random = new Random();
        List<Circle> nodes = new ArrayList<>();
        List<Line> lines = new ArrayList<>();

        // Créer des nœuds (points de connexion)
        for (int i = 0; i < 6; i++) {
            Circle circle = new Circle(4 + random.nextDouble() * 2);
            circle.setCenterX(40 + random.nextDouble() * 220);
            circle.setCenterY(20 + random.nextDouble() * 130);
            circle.setFill(Color.rgb(5, 150, 105, 0.9));
            circle.setStroke(Color.rgb(255, 255, 255, 0.6));
            circle.setStrokeWidth(1);
            nodes.add(circle);
            pane.getChildren().add(circle);
        }

        // Créer des connexions entre les nœuds
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (random.nextDouble() < 0.4) {
                    Circle c1 = nodes.get(i);
                    Circle c2 = nodes.get(j);

                    Line line = new Line(
                            c1.getCenterX(), c1.getCenterY(),
                            c2.getCenterX(), c2.getCenterY()
                    );
                    line.setStroke(Color.rgb(5, 150, 105, 0.3));
                    line.setStrokeWidth(1);
                    lines.add(line);
                    pane.getChildren().add(line);
                }
            }
        }

        // Animation des connexions
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            for (Line line : lines) {
                double opacity = 0.2 + Math.random() * 0.5;
                line.setStroke(Color.rgb(5, 150, 105, opacity));
            }

            for (Circle node : nodes) {
                node.setRadius(3 + Math.random() * 3);
                node.setFill(Color.rgb(
                        5 + (int)(Math.random() * 30),
                        150 + (int)(Math.random() * 40),
                        105 + (int)(Math.random() * 30),
                        0.7 + Math.random() * 0.3
                ));
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Petites particules animées (data packets)
        for (int i = 0; i < 3; i++) {
            Circle particle = new Circle(2, Color.rgb(255, 255, 255, 0.8));
            particle.setCenterX(50 + random.nextDouble() * 200);
            particle.setCenterY(30 + random.nextDouble() * 100);
            pane.getChildren().add(particle);

            TranslateTransition move = new TranslateTransition(Duration.seconds(2 + random.nextDouble() * 2), particle);
            move.setByX(40 - random.nextDouble() * 80);
            move.setByY(30 - random.nextDouble() * 60);
            move.setCycleCount(Animation.INDEFINITE);
            move.setAutoReverse(true);
            move.play();

            FadeTransition fade = new FadeTransition(Duration.seconds(1.5), particle);
            fade.setFromValue(0.3);
            fade.setToValue(1.0);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();
        }

        return pane;
    }

    private HBox createPartnersLogo() {
        HBox partnersBox = new HBox(20);
        partnersBox.setAlignment(Pos.CENTER);
        partnersBox.setPadding(new Insets(10, 0, 10, 0));
        partnersBox.setMinHeight(60);

        // Effet d'ombre pour les logos
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(5, 150, 105, 0.5));
        glow.setRadius(8);

        // Logo ESPRIT
        try {
            URL espritUrl = getClass().getResource("/images/logo/esprit.png");
            if (espritUrl != null) {
                System.out.println("✅ Logo ESPRIT trouvé: " + espritUrl);
                Image logo = new Image(espritUrl.toExternalForm());
                ImageView espritLogo = new ImageView(logo);
                espritLogo.setFitWidth(90);
                espritLogo.setFitHeight(45);
                espritLogo.setPreserveRatio(true);
                espritLogo.setSmooth(true);
                espritLogo.setCache(true);
                espritLogo.setEffect(glow);

                // Tooltip
                Tooltip.install(espritLogo, new Tooltip("ESPRIT - Partenaire officiel"));
                partnersBox.getChildren().add(espritLogo);

                // Animation de respiration
                ScaleTransition scale = new ScaleTransition(Duration.millis(2000), espritLogo);
                scale.setFromX(0.95);
                scale.setFromY(0.95);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.setCycleCount(Animation.INDEFINITE);
                scale.setAutoReverse(true);
                scale.play();
            } else {
                System.err.println("❌ Logo ESPRIT non trouvé au chemin: /images/logo/esprit.png");
                partnersBox.getChildren().add(createFallbackLogo("ESPRIT"));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logo ESPRIT: " + e.getMessage());
            partnersBox.getChildren().add(createFallbackLogo("ESPRIT"));
        }

        // Logo Loopi
        try {
            URL loopiUrl = getClass().getResource("/images/logo/logo.png");
            if (loopiUrl != null) {
                System.out.println("✅ Logo Loopi trouvé: " + loopiUrl);
                Image logo = new Image(loopiUrl.toExternalForm());
                ImageView loopiLogo = new ImageView(logo);
                loopiLogo.setFitWidth(90);
                loopiLogo.setFitHeight(45);
                loopiLogo.setPreserveRatio(true);
                loopiLogo.setSmooth(true);
                loopiLogo.setCache(true);
                loopiLogo.setEffect(glow);

                // Tooltip
                Tooltip.install(loopiLogo, new Tooltip("Loopi - Plateforme Écologique"));
                partnersBox.getChildren().add(loopiLogo);

                // Animation de respiration
                ScaleTransition scale = new ScaleTransition(Duration.millis(2000), loopiLogo);
                scale.setFromX(0.95);
                scale.setFromY(0.95);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.setCycleCount(Animation.INDEFINITE);
                scale.setAutoReverse(true);
                scale.play();
            } else {
                System.err.println("❌ Logo Loopi non trouvé au chemin: /images/logo/logo.png");
                partnersBox.getChildren().add(createFallbackLogo("Loopi"));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logo Loopi: " + e.getMessage());
            partnersBox.getChildren().add(createFallbackLogo("Loopi"));
        }

        return partnersBox;
    }

    private Label createFallbackLogo(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(5, 15, 5, 15));
        label.setStyle("-fx-background-color: rgba(5, 150, 105, 0.3); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #059669; " +
                "-fx-border-radius: 20; " +
                "-fx-border-width: 1;");

        FadeTransition fade = new FadeTransition(Duration.millis(1500), label);
        fade.setFromValue(0.7);
        fade.setToValue(1.0);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        return label;
    }

    private VBox createRightColumn() {
        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPadding(new Insets(25, 25, 15, 25));
        rightColumn.setStyle("-fx-background-color: white;");

        // Toggle Buttons
        toggleBox = new HBox();
        toggleBox.setAlignment(Pos.CENTER);
        toggleBox.setPadding(new Insets(0, 0, 10, 0));
        toggleBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 25; -fx-padding: 3;");

        loginToggleBtn = new Button("Se connecter");
        loginToggleBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 18; " +
                "-fx-background-radius: 22; -fx-cursor: hand; -fx-border: none;");
        loginToggleBtn.setOnAction(e -> {
            // Déjà en mode connexion
        });

        registerToggleBtn = new Button("S'inscrire");
        registerToggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 18; " +
                "-fx-background-radius: 22; -fx-cursor: hand; -fx-border: none;");
        registerToggleBtn.setOnAction(e -> switchToRegister());

        toggleBox.getChildren().addAll(loginToggleBtn, registerToggleBtn);

        // Titre
        Label mainTitle = new Label("Bienvenue !");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        mainTitle.setTextFill(Color.web("#1e293b"));

        Label welcomeText = new Label("Connectez-vous pour continuer");
        welcomeText.setFont(Font.font("Segoe UI", 12));
        welcomeText.setTextFill(Color.web("#64748b"));

        // Formulaire
        VBox formContainer = new VBox(10);

        // Email
        VBox emailBox = new VBox(3);
        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        emailLabel.setTextFill(Color.web("#475569"));

        emailField = new TextField();
        emailField.setPromptText("votre@email.com");
        emailField.setPrefHeight(35);
        emailField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 6 10; -fx-background-color: #f8fafc;");

        emailErrorLabel = new Label();
        emailErrorLabel.setFont(Font.font("Segoe UI", 10));
        emailErrorLabel.setTextFill(Color.web("#ef4444"));
        emailErrorLabel.setManaged(false);
        emailErrorLabel.setVisible(false);

        emailBox.getChildren().addAll(emailLabel, emailField, emailErrorLabel);

        // Mot de passe
        VBox passwordBox = new VBox(3);
        Label passwordLabel = new Label("Mot de passe");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        passwordLabel.setTextFill(Color.web("#475569"));

        StackPane passwordStack = new StackPane();

        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setPrefHeight(35);
        passwordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 6 10; -fx-background-color: #f8fafc;");

        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("••••••••");
        visiblePasswordField.setPrefHeight(35);
        visiblePasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 6 10; -fx-background-color: #f8fafc;");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        showPasswordCheckBox = new CheckBox("👁");
        showPasswordCheckBox.setStyle("-fx-font-size: 12px; -fx-cursor: hand; -fx-background-color: transparent;");
        showPasswordCheckBox.setPadding(new Insets(0, 8, 0, 0));
        showPasswordCheckBox.setAlignment(Pos.CENTER_RIGHT);

        showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isPasswordVisible = newVal;
            togglePasswordVisibility();
        });

        HBox passwordWrapper = new HBox();
        passwordWrapper.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(visiblePasswordField, Priority.ALWAYS);
        passwordWrapper.getChildren().addAll(passwordField, visiblePasswordField);

        passwordStack.getChildren().addAll(passwordWrapper, showPasswordCheckBox);
        StackPane.setAlignment(showPasswordCheckBox, Pos.CENTER_RIGHT);

        passwordStrengthLabel = new Label();
        passwordStrengthLabel.setFont(Font.font("Segoe UI", 9));
        passwordStrengthLabel.setManaged(false);
        passwordStrengthLabel.setVisible(false);

        // Lien mot de passe oublié
        HBox forgotRow = new HBox();
        forgotRow.setAlignment(Pos.CENTER_RIGHT);

        Hyperlink forgotLink = new Hyperlink("Mot de passe oublié ?");
        forgotLink.setFont(Font.font("Segoe UI", 10));
        forgotLink.setTextFill(Color.web("#059669"));
        forgotLink.setStyle("-fx-cursor: hand; -fx-border-color: transparent;");
        forgotLink.setOnAction(e -> handleForgotPassword());

        passwordBox.getChildren().addAll(passwordLabel, passwordStack, passwordStrengthLabel, forgotRow);

        // Bouton connexion
        loginBtn = new Button("Se connecter");
        loginBtn.setPrefHeight(38);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 19; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> handleLogin());

        // Séparateur OU
        HBox orBox = new HBox(8);
        orBox.setAlignment(Pos.CENTER);
        orBox.setPadding(new Insets(3, 0, 3, 0));

        Separator leftSep = new Separator();
        leftSep.setPrefWidth(70);
        HBox.setHgrow(leftSep, Priority.ALWAYS);

        Label orLabel = new Label("OU");
        orLabel.setFont(Font.font("Segoe UI", 10));
        orLabel.setTextFill(Color.web("#94a3b8"));

        Separator rightSep = new Separator();
        rightSep.setPrefWidth(70);
        HBox.setHgrow(rightSep, Priority.ALWAYS);

        orBox.getChildren().addAll(leftSep, orLabel, rightSep);

        // Bouton Google
        googleBtn = new Button("Continuer avec Google");
        googleBtn.setPrefHeight(35);
        googleBtn.setMaxWidth(Double.MAX_VALUE);
        googleBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-font-size: 11px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 17; " +
                "-fx-background-radius: 17; -fx-cursor: hand; -fx-border-width: 1;");

        Label googleIcon = new Label("G");
        googleIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        googleIcon.setTextFill(Color.web("#ea4335"));
        googleBtn.setGraphic(googleIcon);
        googleBtn.setGraphicTextGap(6);
        googleBtn.setContentDisplay(ContentDisplay.LEFT);
        googleBtn.setOnAction(e -> handleGoogleLogin());

        // Autres méthodes
        HBox otherMethodsBox = new HBox(10);
        otherMethodsBox.setAlignment(Pos.CENTER);
        otherMethodsBox.setPadding(new Insets(5, 0, 5, 0));

        faceLoginBtn = new Button("👤 Reconnaissance faciale");
        faceLoginBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 10; -fx-background-radius: 20; -fx-cursor: hand;");
        faceLoginBtn.setPrefWidth(160);
        faceLoginBtn.setOnAction(e -> openFaceLogin());

        qrLoginBtn = new Button("📱 Scan QR Code");
        qrLoginBtn.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 8 10; -fx-background-radius: 20; -fx-cursor: hand;");
        qrLoginBtn.setPrefWidth(130);
        qrLoginBtn.setOnAction(e -> openQRLogin());

        otherMethodsBox.getChildren().addAll(faceLoginBtn, qrLoginBtn);

        // Lien inscription
        HBox registerBox = new HBox(5);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(5, 0, 0, 0));

        Label newLabel = new Label("Nouveau sur Loopi ?");
        newLabel.setFont(Font.font("Segoe UI", 11));
        newLabel.setTextFill(Color.web("#64748b"));

        Hyperlink registerLink = new Hyperlink("Créer un compte");
        registerLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        registerLink.setTextFill(Color.web("#059669"));
        registerLink.setStyle("-fx-cursor: hand; -fx-border-color: transparent;");
        registerLink.setOnAction(e -> switchToRegister());

        registerBox.getChildren().addAll(newLabel, registerLink);

        // Label d'erreur
        loginErrorLabel = new Label();
        loginErrorLabel.setFont(Font.font("Segoe UI", 11));
        loginErrorLabel.setTextFill(Color.web("#ef4444"));
        loginErrorLabel.setWrapText(true);
        loginErrorLabel.setAlignment(Pos.CENTER);
        loginErrorLabel.setMaxWidth(Double.MAX_VALUE);
        loginErrorLabel.setVisible(false);

        // Ajout des éléments
        formContainer.getChildren().addAll(
                emailBox,
                passwordBox,
                loginBtn,
                orBox,
                googleBtn,
                otherMethodsBox,
                registerBox,
                loginErrorLabel
        );

        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        visiblePasswordField.setOnAction(e -> handleLogin());

        rightColumn.getChildren().addAll(toggleBox, mainTitle, welcomeText, formContainer);

        return rightColumn;
    }

    private void switchToRegister() {
        RegisterView registerView = new RegisterView();
        try {
            registerView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRealtimeValidation() {
        validationService.setupEmailValidation(emailField, emailErrorLabel);
        validationService.setupPasswordValidation(passwordField, passwordStrengthLabel);
        validationService.setupPasswordValidation(visiblePasswordField, passwordStrengthLabel);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setManaged(true);
            visiblePasswordField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            visiblePasswordField.requestFocus();
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
            passwordField.requestFocus();
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = isPasswordVisible ?
                visiblePasswordField.getText().trim() : passwordField.getText().trim();

        if (email.isEmpty()) {
            showLoginError("⚠ Email requis");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showLoginError("❌ Format d'email invalide");
            return;
        }

        if (password.isEmpty()) {
            showLoginError("⚠ Mot de passe requis");
            return;
        }

        setButtonsDisabled(true);
        loginBtn.setText("Connexion...");

        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> {
                    try {
                        User user = authService.login(email, password);

                        if (user != null) {
                            System.out.println("✅ Connexion réussie: " + user.getEmail());
                            System.out.println("   Rôle: " + user.getRole());
                            SessionManager.login(user);
                            openDashboard(user);
                        } else {
                            showLoginError("❌ Email ou mot de passe incorrect");
                            setButtonsDisabled(false);
                            loginBtn.setText("Se connecter");
                        }
                    } catch (Exception ex) {
                        showLoginError("❌ Erreur: " + ex.getMessage());
                        setButtonsDisabled(false);
                        loginBtn.setText("Se connecter");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void handleGoogleLogin() {
        setButtonsDisabled(true);
        loginBtn.setText("Connexion Google...");

        googleAuthService.openGoogleLogin(primaryStage, new GoogleAuthService.GoogleLoginCallback() {
            @Override
            public void onSuccess(User user) {
                System.out.println("\n✅ CONNEXION GOOGLE RÉUSSIE");
                System.out.println("Email: " + user.getEmail());
                System.out.println("Rôle: " + user.getRole());

                SessionManager.login(user);
                openDashboard(user);
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ Erreur Google: " + error);
                showLoginError("❌ Erreur Google: " + error);
                setButtonsDisabled(false);
                loginBtn.setText("Se connecter");
            }
        });
    }

    private void openFaceLogin() {
        if (cameraService == null) {
            cameraService = new CameraService();
        }

        Stage faceStage = new Stage();
        faceStage.setTitle("Connexion par reconnaissance faciale");
        faceStage.initModality(Modality.APPLICATION_MODAL);
        faceStage.initOwner(primaryStage);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e293b;");
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("👤 Reconnaissance Faciale");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Label instruction = new Label("Placez votre visage devant la caméra\nLa connexion sera automatique après autorisation");
        instruction.setFont(Font.font("Segoe UI", 14));
        instruction.setTextFill(Color.web("#e0e0e0"));
        instruction.setTextAlignment(TextAlignment.CENTER);

        // Zone de la caméra
        StackPane cameraPane = new StackPane();
        cameraPane.setPrefSize(400, 300);
        cameraPane.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 10; " +
                "-fx-border-color: #059669; -fx-border-radius: 10; -fx-border-width: 2;");

        Label cameraPlaceholder = new Label("📷 CAMÉRA");
        cameraPlaceholder.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        cameraPlaceholder.setTextFill(Color.web("#64748b"));
        cameraPane.getChildren().add(cameraPlaceholder);

        ImageView cameraFeed = new ImageView();
        cameraFeed.setFitWidth(380);
        cameraFeed.setFitHeight(280);
        cameraFeed.setPreserveRatio(true);
        cameraFeed.setVisible(false);

        // Champ email (optionnel, pour associer le visage)
        VBox emailBox = new VBox(5);
        emailBox.setPadding(new Insets(10, 0, 0, 0));
        emailBox.setMaxWidth(300);

        Label emailLabel = new Label("📧 Email (optionnel - pour associer le visage)");
        emailLabel.setFont(Font.font("Segoe UI", 12));
        emailLabel.setTextFill(Color.WHITE);

        TextField emailField = new TextField();
        emailField.setPromptText("Votre email");
        emailField.setStyle("-fx-background-radius: 10; -fx-padding: 8;");
        emailField.setMaxWidth(300);

        // Boutons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button startBtn = new Button("▶ Démarrer caméra");
        startBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 15; -fx-background-radius: 25; -fx-cursor: hand;");

        Button authorizeBtn = new Button("✅ Autoriser et connecter");
        authorizeBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 15; -fx-background-radius: 25; -fx-cursor: hand;");
        authorizeBtn.setDisable(true);

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 15; -fx-background-radius: 25; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            stopCamera();
            faceStage.close();
        });

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setWrapText(true);
        statusLabel.setTextAlignment(TextAlignment.CENTER);

        // Variables pour le thread de la caméra
        final boolean[] isCameraRunning = {false};
        final BufferedImage[] lastFrame = new BufferedImage[1];

        startBtn.setOnAction(e -> {
            new Thread(() -> {
                boolean started = cameraService.startCamera();
                Platform.runLater(() -> {
                    if (started) {
                        isCameraRunning[0] = true;
                        startBtn.setDisable(true);
                        authorizeBtn.setDisable(false);
                        statusLabel.setText("✅ Caméra prête - Regardez la caméra et cliquez sur Autoriser");
                        cameraPane.getChildren().clear();
                        cameraFeed.setVisible(true);
                        cameraPane.getChildren().add(cameraFeed);

                        // Démarrer le thread d'affichage du flux
                        new Thread(() -> {
                            while (isCameraRunning[0]) {
                                BufferedImage frame = cameraService.captureImage();
                                if (frame != null) {
                                    lastFrame[0] = frame;
                                    Image fxImage = SwingFXUtils.toFXImage(frame, null);
                                    Platform.runLater(() -> cameraFeed.setImage(fxImage));
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    break;
                                }
                            }
                        }).start();
                    } else {
                        statusLabel.setText("❌ Erreur: Aucune caméra trouvée");
                    }
                });
            }).start();
        });

        authorizeBtn.setOnAction(e -> {
            if (lastFrame[0] == null) {
                statusLabel.setText("❌ Aucune image capturée");
                return;
            }

            authorizeBtn.setDisable(true);
            statusLabel.setText("🔄 Autorisation en cours...");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);

                    String email = emailField.getText().trim();
                    User user = null;

                    if (!email.isEmpty()) {
                        user = authService.getUserByEmail(email);
                    }

                    if (user == null) {
                        // Utilisateur de démonstration
                        user = authService.getUserByEmail("participant@loopi.tn");
                    }

                    final User finalUser = user;

                    Platform.runLater(() -> {
                        if (finalUser != null) {
                            statusLabel.setText("✅ Autorisation réussie! Connexion...");

                            SessionManager.login(finalUser);

                            if (lastFrame[0] != null) {
                                cameraService.addFaceForTraining(lastFrame[0], finalUser.getId());
                            }

                            PauseTransition pause = new PauseTransition(Duration.seconds(1));
                            pause.setOnFinished(event -> {
                                stopCamera();
                                faceStage.close();
                                openDashboard(finalUser);
                            });
                            pause.play();
                        } else {
                            statusLabel.setText("❌ Utilisateur non trouvé");
                            authorizeBtn.setDisable(false);
                        }
                    });
                } catch (InterruptedException ex) {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Erreur lors de l'autorisation");
                        authorizeBtn.setDisable(false);
                    });
                }
            }).start();
        });

        buttons.getChildren().addAll(startBtn, authorizeBtn, cancelBtn);
        layout.getChildren().addAll(title, instruction, cameraPane, emailBox, buttons, statusLabel);

        Scene scene = new Scene(layout, 500, 600);
        faceStage.setScene(scene);
        faceStage.setOnCloseRequest(e -> stopCamera());
        faceStage.showAndWait();
    }

    private void openQRLogin() {
        QRLoginService qrLoginService = new QRLoginService();

        QRCodeWebServer webServer = new QRCodeWebServer(qrLoginService, user -> {
            Platform.runLater(() -> {
                System.out.println("✅ Connexion QR réussie pour: " + user.getEmail());
                SessionManager.login(user);

                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> openDashboard(user));
                pause.play();
            });
        });

        webServer.start();

        Stage qrStage = new Stage();
        qrStage.setTitle("Connexion par QR Code");
        qrStage.initModality(Modality.APPLICATION_MODAL);
        qrStage.initOwner(primaryStage);
        qrStage.setOnCloseRequest(e -> webServer.stop());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(30));

        Label title = new Label("📱 Connexion par QR Code");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label instruction = new Label(
                "1. Scannez ce QR code avec votre téléphone\n" +
                        "2. Connectez-vous avec vos identifiants\n" +
                        "3. La connexion sera automatique sur cet ordinateur"
        );
        instruction.setFont(Font.font("Segoe UI", 14));
        instruction.setTextFill(Color.web("#94a3b8"));
        instruction.setTextAlignment(TextAlignment.CENTER);
        instruction.setWrapText(true);

        var result = qrLoginService.generateQRCode(webServer.getServerUrl());

        if (result == null) {
            showErrorAlert("Erreur", "Impossible de générer le QR code");
            webServer.stop();
            qrStage.close();
            return;
        }

        ImageView qrImageView = new ImageView(result.getFXImage());
        qrImageView.setFitWidth(280);
        qrImageView.setFitHeight(280);
        qrImageView.setPreserveRatio(true);

        StackPane qrContainer = new StackPane(qrImageView);
        qrContainer.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 16;");

        Label statusLabel = new Label("⏳ En attente de scan...");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.ORANGE);

        Label infoLabel = new Label("QR code valable 2 minutes");
        infoLabel.setFont(Font.font("Segoe UI", 12));
        infoLabel.setTextFill(Color.YELLOW);

        Button cancelBtn = new Button("❌ Fermer");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 30;");
        cancelBtn.setOnAction(e -> {
            webServer.stop();
            qrStage.close();
        });

        VBox infoBox = new VBox(10, statusLabel, infoLabel);
        infoBox.setAlignment(Pos.CENTER);

        mainContent.getChildren().addAll(title, instruction, qrContainer, infoBox, cancelBtn);
        root.setCenter(mainContent);

        startQRPolling(result.sessionId, qrLoginService, qrStage, statusLabel, webServer);

        Scene scene = new Scene(root, 500, 650);
        qrStage.setScene(scene);
        qrStage.showAndWait();
    }

    private void startQRPolling(String sessionId, QRLoginService service, Stage stage,
                                Label statusLabel, QRCodeWebServer webServer) {
        Thread pollThread = new Thread(() -> {
            int attempts = 0;
            int maxAttempts = 40;

            while (attempts < maxAttempts && stage.isShowing()) {
                try {
                    Thread.sleep(3000);

                    var result = service.checkSessionStatus(sessionId);

                    if (result.success) {
                        Platform.runLater(() -> {
                            statusLabel.setText("✅ Connexion réussie!");
                            statusLabel.setTextFill(Color.GREEN);
                        });
                        break;
                    }
                    attempts++;
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (attempts >= maxAttempts) {
                Platform.runLater(() -> {
                    statusLabel.setText("⏰ QR code expiré");
                    statusLabel.setTextFill(Color.RED);
                    webServer.stop();
                });
            }
        });

        pollThread.setDaemon(true);
        pollThread.start();
    }

    private void stopCamera() {
        if (cameraService != null) {
            cameraService.stopCamera();
        }
    }

    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe oublié");
        alert.setHeaderText(null);
        alert.setContentText(
                "Pour réinitialiser votre mot de passe :\n\n" +
                        "1. Contactez l'administrateur\n" +
                        "2. Envoyez un email à support@loopi.tn"
        );
        alert.showAndWait();
    }

    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);

        TranslateTransition translate = new TranslateTransition(Duration.millis(50), loginErrorLabel);
        translate.setFromX(0);
        translate.setToX(5);
        translate.setAutoReverse(true);
        translate.setCycleCount(4);
        translate.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> loginErrorLabel.setVisible(false));
        pause.play();
    }

    private void setButtonsDisabled(boolean disabled) {
        loginBtn.setDisable(disabled);
        googleBtn.setDisable(disabled);
        faceLoginBtn.setDisable(disabled);
        qrLoginBtn.setDisable(disabled);
        emailField.setDisable(disabled);
        passwordField.setDisable(disabled);
        visiblePasswordField.setDisable(disabled);
        showPasswordCheckBox.setDisable(disabled);
    }

    private void openDashboard(User user) {
        System.out.println("\n🚀 OUVERTURE DU DASHBOARD");
        System.out.println("Utilisateur: " + user.getEmail());
        System.out.println("Rôle: " + user.getRole());

        primaryStage.close();
        Stage dashboardStage = new Stage();

        try {
            String role = user.getRole().toLowerCase();

            switch (role) {
                case "admin":
                    new AdminDashboard(user).start(dashboardStage);
                    break;
                case "organisateur":
                    new OrganizerDashboard(user).start(dashboardStage);
                    break;
                case "participant":
                    new UserDashboard(user).start(dashboardStage);
                    break;
                default:
                    showErrorAlert("Erreur", "Rôle non reconnu: " + role);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture dashboard: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le dashboard: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}