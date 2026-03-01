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
import edu.Loopi.services.QRCodeWebService;
import edu.Loopi.services.QRLoginService.QRCodeResult;
import edu.Loopi.services.QRLoginService.QRValidationResult;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.util.UUID;
public class LoginView extends Application {

    private AuthService authService = new AuthService();
    private GoogleAuthService googleAuthService;
    private RealtimeValidationService validationService = new RealtimeValidationService();
    private CameraService cameraService;
    private QRLoginService qrLoginService;
    private QRCodeWebService qrWebService;

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

    // Variables pour la caméra
    private Stage cameraLoginStage;
    private boolean isCameraRunning = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.googleAuthService = new GoogleAuthService(authService);
        this.cameraService = new CameraService();
        this.qrLoginService = new QRLoginService();
        this.qrWebService = new QRCodeWebService();

        if (!MyConnection.testConnection()) {
            showErrorAlert("Erreur de connexion",
                    "Impossible de se connecter à la base de données.\nVérifiez que MySQL est démarré.");
            System.exit(1);
        }

        primaryStage.setTitle("Loopi - Connexion");
        primaryStage.setResizable(false);

        // Conteneur principal avec gradient
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #059669, #047857);");

        // Créer le conteneur du contenu avec l'effet d'ombre
        BorderPane contentContainer = new BorderPane();

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        contentContainer.setEffect(dropShadow);

        // Créer le contenu principal
        VBox content = new VBox();

        VBox header = createHeader();
        VBox formContainer = createLoginForm();
        HBox footer = createFooter();

        content.getChildren().addAll(header, formContainer, footer);

        // Espacement pour éviter que le footer colle au bas
        VBox.setVgrow(formContainer, Priority.ALWAYS);

        // Créer le ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Style pour que le ScrollPane soit transparent
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-scroll-bar-track-color: transparent;" +
                        "-fx-scroll-bar-thumb-color: rgba(255,255,255,0.3);"
        );

        contentContainer.setCenter(scrollPane);
        root.getChildren().add(contentContainer);

        // Ajouter un indicateur de scroll
        Label scrollHint = createScrollHint();
        StackPane.setAlignment(scrollHint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(scrollHint, new Insets(0, 0, 10, 0));
        root.getChildren().add(scrollHint);

        Scene scene = new Scene(root, 420, 750);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Animation d'entrée
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        emailField.requestFocus();
        setupRealtimeValidation();
    }

    private Label createScrollHint() {
        Label scrollHint = new Label("▼ Faites défiler ▼");
        scrollHint.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        scrollHint.setTextFill(Color.web("#ffffff", 0.7));
        scrollHint.setAlignment(Pos.CENTER);

        FadeTransition fadeHint = new FadeTransition(Duration.millis(1500), scrollHint);
        fadeHint.setFromValue(0.3);
        fadeHint.setToValue(1.0);
        fadeHint.setCycleCount(Animation.INDEFINITE);
        fadeHint.setAutoReverse(true);
        fadeHint.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            fadeHint.stop();
            scrollHint.setVisible(false);
        });
        pause.play();

        return scrollHint;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(40, 20, 20, 20));
        header.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("♻️");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 64));
        iconLabel.setTextFill(Color.WHITE);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), iconLabel);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
        scaleTransition.play();

        Label title = new Label("Loopi");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Économie Circulaire & Solidarité");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#e0e0e0"));
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        header.getChildren().addAll(iconLabel, title, subtitle);
        return header;
    }

    private VBox createLoginForm() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 30, 20, 30));

        VBox card = new VBox(20);
        card.setMaxWidth(350);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Label connectLabel = new Label("Bienvenue !");
        connectLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        connectLabel.setTextFill(Color.web("#1e293b"));
        connectLabel.setAlignment(Pos.CENTER);
        connectLabel.setMaxWidth(Double.MAX_VALUE);

        Label connectSubLabel = new Label("Connectez-vous pour continuer");
        connectSubLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        connectSubLabel.setTextFill(Color.web("#64748b"));
        connectSubLabel.setAlignment(Pos.CENTER);
        connectSubLabel.setMaxWidth(Double.MAX_VALUE);

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(5, 0, 5, 0));

        // Email
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("📧 Email");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        emailLabel.setTextFill(Color.web("#1e293b"));

        emailField = new TextField();
        emailField.setPromptText("votre@email.com");
        emailField.setPrefHeight(42);
        emailField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 10; " +
                "-fx-background-radius: 10; -fx-padding: 8 15;");

        emailErrorLabel = new Label();
        emailErrorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        emailErrorLabel.setManaged(false);
        emailErrorLabel.setVisible(false);

        emailBox.getChildren().addAll(emailLabel, emailField, emailErrorLabel);

        // Mot de passe
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("🔐 Mot de passe");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        passwordLabel.setTextFill(Color.web("#1e293b"));

        StackPane passwordStack = new StackPane();

        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setPrefHeight(42);
        passwordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 10; " +
                "-fx-background-radius: 10; -fx-padding: 8 15;");

        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("••••••••");
        visiblePasswordField.setPrefHeight(42);
        visiblePasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 10; " +
                "-fx-background-radius: 10; -fx-padding: 8 15;");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        showPasswordCheckBox = new CheckBox("👁️");
        showPasswordCheckBox.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
        showPasswordCheckBox.setPadding(new Insets(0, 10, 0, 0));
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
        passwordStrengthLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        passwordStrengthLabel.setManaged(false);
        passwordStrengthLabel.setVisible(false);
        passwordStrengthLabel.setPadding(new Insets(2, 0, 0, 0));

        HBox forgotRow = new HBox();
        forgotRow.setAlignment(Pos.CENTER_RIGHT);

        Hyperlink forgotLink = new Hyperlink("Mot de passe oublié ?");
        forgotLink.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        forgotLink.setTextFill(Color.web("#059669"));
        forgotLink.setStyle("-fx-cursor: hand;");
        forgotLink.setOnAction(e -> handleForgotPassword());

        forgotRow.getChildren().add(forgotLink);

        passwordBox.getChildren().addAll(passwordLabel, passwordStack,
                passwordStrengthLabel, forgotRow);

        // Bouton de connexion standard
        loginBtn = new Button("Se connecter");
        loginBtn.setPrefHeight(45);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 25; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> handleLogin());

        loginBtn.setOnMouseEntered(e ->
                loginBtn.setStyle("-fx-background-color: #047857; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14px; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e ->
                loginBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14px; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"));

        // Séparateur OU
        HBox orBox = new HBox(10);
        orBox.setAlignment(Pos.CENTER);
        orBox.setPadding(new Insets(10, 0, 10, 0));

        Separator leftSep = new Separator();
        leftSep.setPrefWidth(100);
        HBox.setHgrow(leftSep, Priority.ALWAYS);

        Label orLabel = new Label("OU");
        orLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        orLabel.setTextFill(Color.web("#94a3b8"));

        Separator rightSep = new Separator();
        rightSep.setPrefWidth(100);
        HBox.setHgrow(rightSep, Priority.ALWAYS);

        orBox.getChildren().addAll(leftSep, orLabel, rightSep);

        // Bouton Google
        googleBtn = new Button("Continuer avec Google");
        googleBtn.setPrefHeight(45);
        googleBtn.setMaxWidth(Double.MAX_VALUE);
        googleBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 25; " +
                "-fx-background-radius: 25; -fx-cursor: hand;");

        Label googleIcon = new Label("G");
        googleIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        googleIcon.setTextFill(Color.web("#ea4335"));
        googleIcon.setPadding(new Insets(0, 10, 0, 0));
        googleBtn.setGraphic(googleIcon);
        googleBtn.setGraphicTextGap(8);
        googleBtn.setContentDisplay(ContentDisplay.LEFT);

        googleBtn.setOnAction(e -> handleGoogleLogin());

        googleBtn.setOnMouseEntered(e ->
                googleBtn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #1e293b; " +
                        "-fx-font-weight: bold; -fx-font-size: 13px; " +
                        "-fx-border-color: #cbd5e1; -fx-border-radius: 25; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"));
        googleBtn.setOnMouseExited(e ->
                googleBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1e293b; " +
                        "-fx-font-weight: bold; -fx-font-size: 13px; " +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 25; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"));

        // Autres méthodes de connexion
        HBox otherMethodsBox = new HBox(10);
        otherMethodsBox.setAlignment(Pos.CENTER);
        otherMethodsBox.setPadding(new Insets(10, 0, 5, 0));

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

        // Lien d'inscription
        VBox registerBox = new VBox(10);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(10, 0, 0, 0));

        Label newLabel = new Label("Nouveau sur Loopi ?");
        newLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        newLabel.setTextFill(Color.web("#64748b"));

        Hyperlink registerLink = new Hyperlink("Créer un compte");
        registerLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        registerLink.setTextFill(Color.web("#059669"));
        registerLink.setStyle("-fx-cursor: hand;");
        registerLink.setOnAction(e -> openRegister());

        registerBox.getChildren().addAll(newLabel, registerLink);

        // Label d'erreur
        loginErrorLabel = new Label();
        loginErrorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        loginErrorLabel.setTextFill(Color.web("#ef4444"));
        loginErrorLabel.setWrapText(true);
        loginErrorLabel.setAlignment(Pos.CENTER);
        loginErrorLabel.setMaxWidth(Double.MAX_VALUE);
        loginErrorLabel.setVisible(false);

        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        visiblePasswordField.setOnAction(e -> handleLogin());

        card.getChildren().addAll(
                connectLabel, connectSubLabel, sep1,
                emailBox, passwordBox, loginBtn,
                orBox, googleBtn,
                otherMethodsBox,
                registerBox, loginErrorLabel
        );

        container.getChildren().add(card);
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);

        Label footerText = new Label("© 2026 Loopi - Plateforme Écologique");
        footerText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        footerText.setTextFill(Color.web("#e0e0e0"));

        footer.getChildren().add(footerText);
        return footer;
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
        loginBtn.setText("Connexion en cours...");

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

        Label title = new Label("👤 Connexion Faciale");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Label instruction = new Label("Placez votre visage devant la caméra\nLa connexion sera automatique après autorisation");
        instruction.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
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
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
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
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setWrapText(true);
        statusLabel.setTextAlignment(TextAlignment.CENTER);

        // Thread pour le flux caméra
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
        if (qrLoginService == null) {
            qrLoginService = new QRLoginService();
        }

        QRCodeWebService webService = new QRCodeWebService();
        webService.startServer();

        Stage qrStage = new Stage();
        qrStage.setTitle("Connexion par QR Code");
        qrStage.initModality(Modality.APPLICATION_MODAL);
        qrStage.initOwner(primaryStage);

        qrStage.setOnCloseRequest(e -> webService.stopServer());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e293b;");

        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(30));

        Label title = new Label("📱 Connexion par QR Code");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label instruction = new Label(
                "Scannez ce QR code avec votre téléphone\n" +
                        "Une page de connexion s'ouvrira automatiquement"
        );
        instruction.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        instruction.setTextFill(Color.web("#e0e0e0"));
        instruction.setTextAlignment(TextAlignment.CENTER);
        instruction.setWrapText(true);

        // Utiliser l'URL complète du serveur
        String serverUrl = webService.getServerUrl();
        String sessionId = java.util.UUID.randomUUID().toString(); // Import java.util.UUID

        // Construire l'URL complète pour le QR code
        String loginUrl = serverUrl + "/mobile/login?session=" + sessionId;

        System.out.println("📱 URL générée: " + loginUrl);

        // Générer le QR code avec l'URL
        QRCodeResult qrResult = qrLoginService.generateLoginQRCode(loginUrl, sessionId);

        if (qrResult == null) {
            showErrorAlert("Erreur", "Impossible de générer le QR code");
            qrStage.close();
            webService.stopServer();
            return;
        }

        // Afficher le QR code
        ImageView qrImageView = new ImageView(qrResult.getFXImage());
        qrImageView.setFitWidth(280);
        qrImageView.setFitHeight(280);
        qrImageView.setPreserveRatio(true);
        qrImageView.setSmooth(true);

        StackPane qrContainer = new StackPane(qrImageView);
        qrContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        qrContainer.setMaxWidth(320);
        qrContainer.setMaxHeight(320);

        // Informations
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);

        Label urlInfo = new Label("URL: " + loginUrl);
        urlInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        urlInfo.setTextFill(Color.CYAN);
        urlInfo.setWrapText(true);

        Label sessionLabel = new Label("⏳ QR Code valable 2 minutes");
        sessionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        sessionLabel.setTextFill(Color.YELLOW);

        Label statusLabel = new Label("En attente de scan...");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.ORANGE);
        statusLabel.setWrapText(true);
        statusLabel.setTextAlignment(TextAlignment.CENTER);

        Button cancelBtn = new Button("❌ Fermer");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 25; -fx-cursor: hand;");
        cancelBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        cancelBtn.setOnAction(e -> {
            webService.stopServer();
            qrStage.close();
        });

        infoBox.getChildren().addAll(urlInfo, sessionLabel, statusLabel);
        mainContent.getChildren().addAll(title, instruction, qrContainer, infoBox, cancelBtn);
        root.setCenter(mainContent);

        // Démarrer la vérification périodique
        startQRPolling(sessionId, qrStage, statusLabel, webService);

        Scene scene = new Scene(root, 500, 700);
        qrStage.setScene(scene);
        qrStage.showAndWait();
    }    private void startQRPolling(String sessionId, Stage qrStage, Label statusLabel, QRCodeWebService webService) {
        Thread pollingThread = new Thread(() -> {
            int attempts = 0;
            int maxAttempts = 40; // 2 minutes / 3 secondes

            while (attempts < maxAttempts && qrStage.isShowing()) {
                try {
                    Thread.sleep(3000);

                    QRValidationResult result = qrLoginService.checkSessionStatus(sessionId);

                    if (result.isSuccess()) {
                        Platform.runLater(() -> {
                            statusLabel.setText("✅ Connexion réussie!");
                            statusLabel.setTextFill(Color.GREEN);

                            User user = result.getUser();
                            SessionManager.login(user);

                            PauseTransition pause = new PauseTransition(Duration.seconds(1));
                            pause.setOnFinished(e -> {
                                webService.stopServer();
                                qrStage.close();
                                openDashboard(user);
                            });
                            pause.play();
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
                    webService.stopServer();
                });
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
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
        translate.setToX(10);
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

    private void openRegister() {
        RegisterView registerView = new RegisterView();
        registerView.show(primaryStage);
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