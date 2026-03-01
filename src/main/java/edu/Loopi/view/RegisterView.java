package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.AuthService;
import edu.Loopi.services.UserService;
import edu.Loopi.services.RealtimeValidationService;
import edu.Loopi.services.PhotoService;
import edu.Loopi.services.CameraService;
import edu.Loopi.tools.MyConnection;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.File;

public class RegisterView extends Application {

    private AuthService authService = new AuthService();
    private UserService userService = new UserService();
    private RealtimeValidationService validationService = new RealtimeValidationService();
    private PhotoService photoService = new PhotoService();
    private CameraService cameraService;

    private Stage primaryStage;

    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField visiblePasswordField;
    private PasswordField confirmPasswordField;
    private TextField visibleConfirmPasswordField;
    private ComboBox<String> genreComboBox;
    private CheckBox showPasswordCheckBox;
    private CheckBox showConfirmPasswordCheckBox;
    private Label emailErrorLabel;
    private Label passwordStrengthLabel;
    private Label confirmPasswordErrorLabel;
    private Label registerErrorLabel;
    private Button registerBtn;
    private Button backToLoginBtn;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Nouveaux éléments pour la photo
    private ImageView profileImageView;
    private File selectedPhotoFile;
    private BufferedImage capturedPhoto;
    private String profilePhotoPath = "default.jpg";
    private boolean isUsingCamera = false;

    // Nouveaux éléments pour le rôle
    private ToggleGroup roleGroup;
    private RadioButton participantRadio;
    private RadioButton organisateurRadio;
    private RadioButton adminRadio; // AJOUT: bouton pour admin

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.cameraService = new CameraService();

        if (!MyConnection.testConnection()) {
            showErrorAlert("Erreur de connexion",
                    "Impossible de se connecter à la base de données.\nVérifiez que MySQL est démarré.");
            System.exit(1);
        }

        primaryStage.setTitle("Loopi - Inscription");
        primaryStage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #059669, #047857);");

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        root.setEffect(dropShadow);

        VBox header = createHeader();
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(createRegisterForm());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setBorder(null);
        root.setCenter(scrollPane);

        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 550, 900); // Augmenté la hauteur
        primaryStage.setScene(scene);
        primaryStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        prenomField.requestFocus();
        setupValidations();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(30, 20, 10, 20));
        header.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("♻️");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        iconLabel.setTextFill(Color.WHITE);

        Label title = new Label("Rejoindre Loopi");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Créez votre compte en quelques secondes");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#e0e0e0"));
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        header.getChildren().addAll(iconLabel, title, subtitle);
        return header;
    }

    private VBox createRegisterForm() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10, 30, 20, 30));

        VBox card = new VBox(15);
        card.setMaxWidth(500);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Label registerLabel = new Label("Créer un compte");
        registerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        registerLabel.setTextFill(Color.web("#1e293b"));
        registerLabel.setAlignment(Pos.CENTER);
        registerLabel.setMaxWidth(Double.MAX_VALUE);

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(5, 0, 10, 0));

        // SECTION PHOTO DE PROFIL
        VBox photoBox = new VBox(10);
        photoBox.setAlignment(Pos.CENTER);
        photoBox.setPadding(new Insets(10, 0, 15, 0));

        Label photoLabel = new Label("📸 Photo de profil");
        photoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        photoLabel.setTextFill(Color.web("#1e293b"));

        // Image par défaut
        profileImageView = new ImageView();
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profileImageView.setPreserveRatio(true);

        // Créer un cercle de recadrage pour l'image
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(50);
        clip.setArcHeight(50);
        profileImageView.setClip(clip);

        // Image par défaut (avatar avec initiales)
        try {
            Image defaultImage = new Image("https://ui-avatars.com/api/?name=User&size=100&background=059669&color=fff");
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            // Ignorer
        }

        HBox photoButtons = new HBox(10);
        photoButtons.setAlignment(Pos.CENTER);

        Button choosePhotoBtn = new Button("📁 Choisir photo");
        choosePhotoBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
        choosePhotoBtn.setOnAction(e -> chooseProfilePhoto());

        Button takePhotoBtn = new Button("📷 Prendre photo");
        takePhotoBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
        takePhotoBtn.setOnAction(e -> openCamera());

        photoButtons.getChildren().addAll(choosePhotoBtn, takePhotoBtn);
        photoBox.getChildren().addAll(photoLabel, profileImageView, photoButtons);

        // Prénom
        VBox prenomBox = new VBox(5);
        Label prenomLabel = new Label("👤 Prénom *");
        prenomLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        prenomLabel.setTextFill(Color.web("#1e293b"));
        prenomField = new TextField();
        prenomField.setPromptText("Votre prénom");
        prenomField.setPrefHeight(40);
        prenomField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");
        prenomBox.getChildren().addAll(prenomLabel, prenomField);

        // Nom
        VBox nomBox = new VBox(5);
        Label nomLabel = new Label("📝 Nom *");
        nomLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nomLabel.setTextFill(Color.web("#1e293b"));
        nomField = new TextField();
        nomField.setPromptText("Votre nom");
        nomField.setPrefHeight(40);
        nomField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");
        nomBox.getChildren().addAll(nomLabel, nomField);

        // Email
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("📧 Email *");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        emailLabel.setTextFill(Color.web("#1e293b"));
        emailField = new TextField();
        emailField.setPromptText("votre@email.com");
        emailField.setPrefHeight(40);
        emailField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");
        emailErrorLabel = new Label();
        emailErrorLabel.setFont(Font.font("Segoe UI", 11));
        emailErrorLabel.setManaged(false);
        emailErrorLabel.setVisible(false);
        emailBox.getChildren().addAll(emailLabel, emailField, emailErrorLabel);

        // Genre
        VBox genreBox = new VBox(5);
        Label genreLabel = new Label("⚥ Genre");
        genreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        genreLabel.setTextFill(Color.web("#1e293b"));
        genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreComboBox.setValue("Non spécifié");
        genreComboBox.setPrefHeight(40);
        genreComboBox.setMaxWidth(Double.MAX_VALUE);
        genreComboBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8;");
        genreBox.getChildren().addAll(genreLabel, genreComboBox);

        // SECTION RÔLE - MODIFIÉ (avec ADMIN)
        VBox roleBox = new VBox(5);
        Label roleLabel = new Label("👑 Choisir un rôle *");
        roleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        roleLabel.setTextFill(Color.web("#1e293b"));

        HBox roleButtons = new HBox(20);
        roleButtons.setAlignment(Pos.CENTER_LEFT);

        roleGroup = new ToggleGroup();

        participantRadio = new RadioButton("Participant");
        participantRadio.setToggleGroup(roleGroup);
        participantRadio.setSelected(true);
        participantRadio.setUserData("participant");
        participantRadio.setStyle("-fx-font-size: 13px;");

        organisateurRadio = new RadioButton("Organisateur");
        organisateurRadio.setToggleGroup(roleGroup);
        organisateurRadio.setUserData("organisateur");
        organisateurRadio.setStyle("-fx-font-size: 13px;");

        // AJOUT: bouton Admin
        adminRadio = new RadioButton("Administrateur");
        adminRadio.setToggleGroup(roleGroup);
        adminRadio.setUserData("admin");
        adminRadio.setStyle("-fx-font-size: 13px; -fx-text-fill: #8b5cf6;");

        Label roleInfo = new Label("ℹ️ Les administrateurs ont accès à toutes les fonctionnalités");
        roleInfo.setFont(Font.font("Segoe UI", 11));
        roleInfo.setTextFill(Color.web("#64748b"));

        roleButtons.getChildren().addAll(participantRadio, organisateurRadio, adminRadio);
        roleBox.getChildren().addAll(roleLabel, roleButtons, roleInfo);

        // Mot de passe
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("🔐 Mot de passe *");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        passwordLabel.setTextFill(Color.web("#1e293b"));

        StackPane passwordStack = new StackPane();
        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");

        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("••••••••");
        visiblePasswordField.setPrefHeight(40);
        visiblePasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");
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

        passwordBox.getChildren().addAll(passwordLabel, passwordStack, passwordStrengthLabel);

        // Confirmer mot de passe
        VBox confirmPasswordBox = new VBox(5);
        Label confirmPasswordLabel = new Label("🔐 Confirmer le mot de passe *");
        confirmPasswordLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        confirmPasswordLabel.setTextFill(Color.web("#1e293b"));

        StackPane confirmPasswordStack = new StackPane();
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("••••••••");
        confirmPasswordField.setPrefHeight(40);
        confirmPasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");

        visibleConfirmPasswordField = new TextField();
        visibleConfirmPasswordField.setPromptText("••••••••");
        visibleConfirmPasswordField.setPrefHeight(40);
        visibleConfirmPasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");
        visibleConfirmPasswordField.setManaged(false);
        visibleConfirmPasswordField.setVisible(false);

        showConfirmPasswordCheckBox = new CheckBox("👁️");
        showConfirmPasswordCheckBox.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
        showConfirmPasswordCheckBox.setPadding(new Insets(0, 10, 0, 0));
        showConfirmPasswordCheckBox.setAlignment(Pos.CENTER_RIGHT);
        showConfirmPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isConfirmPasswordVisible = newVal;
            toggleConfirmPasswordVisibility();
        });

        HBox confirmPasswordWrapper = new HBox();
        confirmPasswordWrapper.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(confirmPasswordField, Priority.ALWAYS);
        HBox.setHgrow(visibleConfirmPasswordField, Priority.ALWAYS);
        confirmPasswordWrapper.getChildren().addAll(confirmPasswordField, visibleConfirmPasswordField);

        confirmPasswordStack.getChildren().addAll(confirmPasswordWrapper, showConfirmPasswordCheckBox);
        StackPane.setAlignment(showConfirmPasswordCheckBox, Pos.CENTER_RIGHT);

        confirmPasswordErrorLabel = new Label();
        confirmPasswordErrorLabel.setFont(Font.font("Segoe UI", 11));
        confirmPasswordErrorLabel.setManaged(false);
        confirmPasswordErrorLabel.setVisible(false);
        confirmPasswordErrorLabel.setPadding(new Insets(2, 0, 0, 0));

        confirmPasswordBox.getChildren().addAll(confirmPasswordLabel, confirmPasswordStack, confirmPasswordErrorLabel);

        // Bouton d'inscription
        registerBtn = new Button("Créer mon compte");
        registerBtn.setPrefHeight(45);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 25; -fx-cursor: hand;");
        registerBtn.setOnAction(e -> handleRegister());

        registerBtn.setOnMouseEntered(e ->
                registerBtn.setStyle("-fx-background-color: #047857; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14px; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"));
        registerBtn.setOnMouseExited(e ->
                registerBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14px; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"));

        // Lien retour à la connexion
        VBox loginBox = new VBox(10);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(10, 0, 0, 0));

        Label alreadyLabel = new Label("Déjà un compte ?");
        alreadyLabel.setFont(Font.font("Segoe UI", 12));
        alreadyLabel.setTextFill(Color.web("#64748b"));

        backToLoginBtn = new Button("Se connecter");
        backToLoginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #059669; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: transparent;");
        backToLoginBtn.setOnAction(e -> goToLogin());

        loginBox.getChildren().addAll(alreadyLabel, backToLoginBtn);

        // Label d'erreur
        registerErrorLabel = new Label();
        registerErrorLabel.setFont(Font.font("Segoe UI", 12));
        registerErrorLabel.setTextFill(Color.web("#ef4444"));
        registerErrorLabel.setWrapText(true);
        registerErrorLabel.setAlignment(Pos.CENTER);
        registerErrorLabel.setMaxWidth(Double.MAX_VALUE);
        registerErrorLabel.setVisible(false);

        // Ajouter tous les composants à la carte
        card.getChildren().addAll(
                registerLabel, sep1,
                photoBox,
                prenomBox, nomBox, emailBox, genreBox,
                roleBox,
                passwordBox, confirmPasswordBox,
                registerBtn, loginBox, registerErrorLabel
        );

        container.getChildren().add(card);
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);

        Label footerText = new Label("© 2026 Loopi - Plateforme Écologique");
        footerText.setFont(Font.font("Segoe UI", 11));
        footerText.setTextFill(Color.web("#e0e0e0"));

        footer.getChildren().add(footerText);
        return footer;
    }

    private void setupValidations() {
        validationService.setupEmailValidation(emailField, emailErrorLabel);
        validationService.setupPasswordValidation(passwordField, passwordStrengthLabel);
        validationService.setupPasswordValidation(visiblePasswordField, passwordStrengthLabel);
        validationService.setupPasswordConfirmation(
                passwordField, visiblePasswordField,
                confirmPasswordField, visibleConfirmPasswordField,
                confirmPasswordErrorLabel);
    }

    private void chooseProfilePhoto() {
        File selectedFile = photoService.choosePhotoFromComputer(primaryStage);
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString(), 100, 100, true, true);
                profileImageView.setImage(image);
                selectedPhotoFile = selectedFile;
                isUsingCamera = false;
                System.out.println("✅ Photo choisie: " + selectedFile.getName());
            } catch (Exception e) {
                showRegisterError("❌ Erreur chargement photo");
            }
        }
    }

    private void openCamera() {
        Stage cameraStage = new Stage();
        cameraStage.setTitle("Prendre une photo");
        cameraStage.initModality(Modality.APPLICATION_MODAL);
        cameraStage.initOwner(primaryStage);

        VBox cameraLayout = new VBox(20);
        cameraLayout.setPadding(new Insets(20));
        cameraLayout.setStyle("-fx-background-color: #1e293b;");
        cameraLayout.setAlignment(Pos.CENTER);

        Label title = new Label("📷 Prendre une photo");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        // Zone de la caméra
        StackPane cameraPane = new StackPane();
        cameraPane.setPrefSize(320, 240);
        cameraPane.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 10; " +
                "-fx-border-color: #059669; -fx-border-radius: 10; -fx-border-width: 2;");

        Label cameraPlaceholder = new Label("📷 CAMÉRA");
        cameraPlaceholder.setFont(Font.font(20));
        cameraPlaceholder.setTextFill(Color.web("#64748b"));
        cameraPane.getChildren().add(cameraPlaceholder);

        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(300);
        previewImage.setFitHeight(200);
        previewImage.setPreserveRatio(true);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button startBtn = new Button("▶ Démarrer");
        startBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");

        Button captureBtn = new Button("📸 Capturer");
        captureBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
        captureBtn.setDisable(true);

        Button acceptBtn = new Button("✅ Accepter");
        acceptBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        acceptBtn.setVisible(false);

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            cameraService.stopCamera();
            cameraStage.close();
        });

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.WHITE);

        // Créer une copie finale de cameraService pour l'utiliser dans les lambdas
        CameraService finalCameraService = this.cameraService;

        // Variables pour stocker la photo capturée de manière finale
        final BufferedImage[] capturedPhotoRef = new BufferedImage[1];
        capturedPhotoRef[0] = null;

        startBtn.setOnAction(e -> {
            new Thread(() -> {
                boolean started = finalCameraService.startCamera();
                Platform.runLater(() -> {
                    if (started) {
                        startBtn.setDisable(true);
                        captureBtn.setDisable(false);
                        statusLabel.setText("✅ Caméra prête");
                        cameraPane.getChildren().clear();

                        Label cameraActive = new Label("📷 FLUX ACTIF");
                        cameraActive.setFont(Font.font(16));
                        cameraActive.setTextFill(Color.GREEN);
                        cameraPane.getChildren().add(cameraActive);
                    } else {
                        statusLabel.setText("❌ Erreur: Aucune caméra trouvée");
                    }
                });
            }).start();
        });

        captureBtn.setOnAction(e -> {
            statusLabel.setText("🔄 Capture...");

            new Thread(() -> {
                BufferedImage captured = finalCameraService.captureImage();
                Platform.runLater(() -> {
                    if (captured != null) {
                        Image fxImage = SwingFXUtils.toFXImage(captured, null);
                        previewImage.setImage(fxImage);
                        capturedPhotoRef[0] = captured;

                        cameraPane.getChildren().clear();
                        cameraPane.getChildren().add(previewImage);

                        captureBtn.setVisible(false);
                        acceptBtn.setVisible(true);
                        statusLabel.setText("✅ Photo capturée");
                    } else {
                        statusLabel.setText("❌ Erreur capture");
                    }
                });
            }).start();
        });

        acceptBtn.setOnAction(e -> {
            if (capturedPhotoRef[0] != null) {
                Image fxImage = SwingFXUtils.toFXImage(capturedPhotoRef[0], null);
                profileImageView.setImage(fxImage);
                this.capturedPhoto = capturedPhotoRef[0]; // Assigner à la variable d'instance
                isUsingCamera = true;
                finalCameraService.stopCamera();
                cameraStage.close();
            }
        });

        buttons.getChildren().addAll(startBtn, captureBtn, acceptBtn, cancelBtn);
        cameraLayout.getChildren().addAll(title, cameraPane, buttons, statusLabel);

        Scene scene = new Scene(cameraLayout, 400, 450);
        cameraStage.setScene(scene);
        cameraStage.setOnCloseRequest(e -> finalCameraService.stopCamera());
        cameraStage.showAndWait();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setManaged(true);
            visiblePasswordField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
        }
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            visibleConfirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(false);
            confirmPasswordField.setVisible(false);
        } else {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            confirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(false);
            visibleConfirmPasswordField.setVisible(false);
        }
    }

    private void handleRegister() {
        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String genre = genreComboBox.getValue();

        // Récupérer le rôle sélectionné
        String role = "participant";
        if (roleGroup.getSelectedToggle() != null) {
            role = (String) roleGroup.getSelectedToggle().getUserData();
        }

        String password = isPasswordVisible ?
                visiblePasswordField.getText().trim() : passwordField.getText().trim();
        String confirmPassword = isConfirmPasswordVisible ?
                visibleConfirmPasswordField.getText().trim() : confirmPasswordField.getText().trim();

        // Validations
        if (prenom.isEmpty()) {
            showRegisterError("⚠ Prénom requis");
            return;
        }

        if (nom.isEmpty()) {
            showRegisterError("⚠ Nom requis");
            return;
        }

        if (email.isEmpty()) {
            showRegisterError("⚠ Email requis");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showRegisterError("❌ Format d'email invalide");
            return;
        }

        if (password.isEmpty()) {
            showRegisterError("⚠ Mot de passe requis");
            return;
        }

        if (password.length() < 6) {
            showRegisterError("⚠ Mot de passe trop court (min 6 caractères)");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showRegisterError("❌ Les mots de passe ne correspondent pas");
            return;
        }

        if (authService.emailExists(email)) {
            showRegisterError("❌ Cet email est déjà utilisé");
            return;
        }

        setButtonsDisabled(true);
        registerBtn.setText("Création en cours...");

        // Créer des copies finales des variables utilisées dans le thread
        final String finalPrenom = prenom;
        final String finalNom = nom;
        final String finalEmail = email;
        final String finalRole = role;
        final int finalGenreId = getGenreIdFromString(genre);
        final boolean finalIsUsingCamera = isUsingCamera;
        final BufferedImage finalCapturedPhoto = capturedPhoto;
        final File finalSelectedPhotoFile = selectedPhotoFile;

        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> {
                    try {
                        User newUser = new User();
                        newUser.setPrenom(finalPrenom);
                        newUser.setNom(finalNom);
                        newUser.setEmail(finalEmail);
                        newUser.setRole(finalRole);

                        newUser.setIdGenre(finalGenreId);
                        newUser.setPhoto("default.jpg");

                        // Inscription sans photo d'abord
                        boolean registered = authService.register(newUser, password);

                        if (registered) {
                            // Récupérer l'utilisateur créé pour avoir l'ID
                            User created = authService.getUserByEmail(finalEmail);

                            if (created != null) {
                                // Sauvegarder la photo si elle existe
                                if (finalIsUsingCamera && finalCapturedPhoto != null) {
                                    String photoPath = photoService.saveCameraPhoto(finalCapturedPhoto, created.getId());
                                    created.setPhoto(photoPath);
                                    userService.updateUser(created);

                                    // Ajouter le visage pour la reconnaissance faciale
                                    if (cameraService != null) {
                                        cameraService.addFaceForTraining(finalCapturedPhoto, created.getId());
                                    }

                                } else if (finalSelectedPhotoFile != null) {
                                    String photoPath = photoService.saveProfilePhoto(finalSelectedPhotoFile, created.getId());
                                    created.setPhoto(photoPath);
                                    userService.updateUser(created);
                                }
                            }

                            System.out.println("✅ Inscription réussie: " + finalEmail + " (rôle: " + finalRole + ")");
                            showSuccessAlert("Inscription réussie",
                                    "Votre compte a été créé avec succès !\nVous pouvez maintenant vous connecter.");
                            goToLogin();
                        } else {
                            showRegisterError("❌ Erreur lors de l'inscription");
                            setButtonsDisabled(false);
                            registerBtn.setText("Créer mon compte");
                        }
                    } catch (Exception ex) {
                        showRegisterError("❌ Erreur: " + ex.getMessage());
                        setButtonsDisabled(false);
                        registerBtn.setText("Créer mon compte");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private int getGenreIdFromString(String genre) {
        if (genre == null) return 3;
        switch (genre.toLowerCase()) {
            case "homme": return 1;
            case "femme": return 2;
            case "non spécifié":
            default: return 3;
        }
    }

    private void goToLogin() {
        if (cameraService != null) {
            cameraService.stopCamera();
        }

        LoginView loginView = new LoginView();
        try {
            loginView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRegisterError(String message) {
        registerErrorLabel.setText(message);
        registerErrorLabel.setVisible(true);

        TranslateTransition translate = new TranslateTransition(Duration.millis(50), registerErrorLabel);
        translate.setFromX(0);
        translate.setToX(10);
        translate.setAutoReverse(true);
        translate.setCycleCount(4);
        translate.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> registerErrorLabel.setVisible(false));
        pause.play();
    }

    private void setButtonsDisabled(boolean disabled) {
        registerBtn.setDisable(disabled);
        backToLoginBtn.setDisable(disabled);
        nomField.setDisable(disabled);
        prenomField.setDisable(disabled);
        emailField.setDisable(disabled);
        passwordField.setDisable(disabled);
        visiblePasswordField.setDisable(disabled);
        confirmPasswordField.setDisable(disabled);
        visibleConfirmPasswordField.setDisable(disabled);
        genreComboBox.setDisable(disabled);
        showPasswordCheckBox.setDisable(disabled);
        showConfirmPasswordCheckBox.setDisable(disabled);
        participantRadio.setDisable(disabled);
        organisateurRadio.setDisable(disabled);
        adminRadio.setDisable(disabled); // AJOUT
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show(Stage parentStage) {
        try {
            start(parentStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}