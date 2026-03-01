package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.AuthService;
import edu.Loopi.services.UserService;
import edu.Loopi.services.RealtimeValidationService;
import edu.Loopi.services.PhotoService;
import edu.Loopi.services.CameraService;
import edu.Loopi.tools.MyConnection;

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
import java.util.regex.Pattern;

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

    // Labels d'erreur pour chaque champ
    private Label prenomErrorLabel;
    private Label nomErrorLabel;
    private Label emailErrorLabel;
    private Label genreErrorLabel;
    private Label passwordErrorLabel;
    private Label confirmPasswordErrorLabel;
    private Label roleErrorLabel;
    private Label registerErrorLabel;

    private Button registerBtn;
    private Button backToLoginBtn;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Photo de profil
    private ImageView profileImageView;
    private File selectedPhotoFile;
    private BufferedImage capturedPhoto;
    private boolean isUsingCamera = false;
    private Label photoStatusLabel;
    private StackPane imageContainer;

    // Rôle
    private ToggleGroup roleGroup;
    private RadioButton adminRadio;
    private RadioButton organisateurRadio;
    private RadioButton participantRadio;

    // Toggle buttons
    private Button loginToggleBtn;
    private Button registerToggleBtn;
    private HBox toggleBox;

    // Indicateur de force du mot de passe
    private ProgressBar passwordStrengthBar;
    private Label strengthTextLabel;

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

        // Conteneur principal avec les deux colonnes
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");

        // Colonne gauche avec animation
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

        prenomField.requestFocus();
        setupValidations();
    }

    private VBox createLeftColumn() {
        VBox leftColumn = new VBox(15);
        leftColumn.setStyle("-fx-background-color: linear-gradient(to bottom, #0f172a, #1e293b);");
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setPadding(new Insets(25, 15, 15, 15));

        Label title = new Label("Loopi");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Rejoignez la communauté");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#94a3b8"));
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setMaxWidth(280);

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background: #94a3b8;");
        separator.setMaxWidth(200);

        // Points animés
        HBox dotsBox = new HBox(8);
        dotsBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Circle dot = new Circle(4);
            dot.setFill(Color.rgb(5, 150, 105, 0.7));

            FadeTransition fade = new FadeTransition(Duration.millis(1000), dot);
            fade.setFromValue(0.3);
            fade.setToValue(1.0);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setAutoReverse(true);
            fade.setDelay(Duration.millis(i * 200));
            fade.play();

            dotsBox.getChildren().add(dot);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox partnersBox = createPartnersLogo();
        partnersBox.setAlignment(Pos.CENTER);
        partnersBox.setPadding(new Insets(10, 0, 5, 0));

        leftColumn.getChildren().addAll(title, subtitle, separator, dotsBox, spacer, partnersBox);

        return leftColumn;
    }

    private HBox createPartnersLogo() {
        HBox partnersBox = new HBox(20);
        partnersBox.setAlignment(Pos.CENTER);
        partnersBox.setPadding(new Insets(10, 0, 10, 0));
        partnersBox.setMinHeight(60);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(5, 150, 105, 0.5));
        glow.setRadius(8);

        // Logo ESPRIT
        try {
            URL espritUrl = getClass().getResource("/images/logo/esprit.png");
            if (espritUrl != null) {
                Image logo = new Image(espritUrl.toExternalForm());
                ImageView espritLogo = new ImageView(logo);
                espritLogo.setFitWidth(90);
                espritLogo.setFitHeight(45);
                espritLogo.setPreserveRatio(true);
                espritLogo.setSmooth(true);
                espritLogo.setCache(true);
                espritLogo.setEffect(glow);
                partnersBox.getChildren().add(espritLogo);
            } else {
                partnersBox.getChildren().add(createFallbackLogo("ESPRIT"));
            }
        } catch (Exception e) {
            partnersBox.getChildren().add(createFallbackLogo("ESPRIT"));
        }

        // Logo Loopi
        try {
            URL loopiUrl = getClass().getResource("/images/logo/logo.png");
            if (loopiUrl != null) {
                Image logo = new Image(loopiUrl.toExternalForm());
                ImageView loopiLogo = new ImageView(logo);
                loopiLogo.setFitWidth(90);
                loopiLogo.setFitHeight(45);
                loopiLogo.setPreserveRatio(true);
                loopiLogo.setSmooth(true);
                loopiLogo.setCache(true);
                loopiLogo.setEffect(glow);
                partnersBox.getChildren().add(loopiLogo);
            } else {
                partnersBox.getChildren().add(createFallbackLogo("Loopi"));
            }
        } catch (Exception e) {
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
        VBox rightColumn = new VBox(8);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPadding(new Insets(15, 20, 10, 20));
        rightColumn.setStyle("-fx-background-color: white;");

        // Toggle Buttons
        toggleBox = new HBox();
        toggleBox.setAlignment(Pos.CENTER);
        toggleBox.setPadding(new Insets(0, 0, 10, 0));
        toggleBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 30; -fx-padding: 4;");

        loginToggleBtn = new Button("Se connecter");
        loginToggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 20; " +
                "-fx-background-radius: 25; -fx-cursor: hand; -fx-border: none;");
        loginToggleBtn.setOnAction(e -> goToLogin());

        registerToggleBtn = new Button("S'inscrire");
        registerToggleBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 20; " +
                "-fx-background-radius: 25; -fx-cursor: hand; -fx-border: none;");

        toggleBox.getChildren().addAll(loginToggleBtn, registerToggleBtn);

        Label mainTitle = new Label("Créer un compte");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        mainTitle.setTextFill(Color.web("#1e293b"));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(420);

        VBox formContainer = new VBox(10);
        formContainer.setPadding(new Insets(5, 5, 5, 5));

        // ========== PHOTO DE PROFIL ==========
        VBox photoBox = new VBox(8);
        photoBox.setAlignment(Pos.CENTER);
        photoBox.setPadding(new Insets(5, 0, 10, 0));

        // Conteneur pour l'image
        imageContainer = new StackPane();
        imageContainer.setPrefSize(120, 120);
        imageContainer.setMinSize(120, 120);
        imageContainer.setMaxSize(120, 120);
        imageContainer.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 60; -fx-border-color: #059669; -fx-border-radius: 60; -fx-border-width: 3;");

        profileImageView = new ImageView();
        profileImageView.setFitWidth(114);
        profileImageView.setFitHeight(114);
        profileImageView.setPreserveRatio(true);

        // Cercle de découpe parfait
        Circle clipCircle = new Circle(57);
        clipCircle.setCenterX(57);
        clipCircle.setCenterY(57);
        profileImageView.setClip(clipCircle);

        // Image par défaut
        try {
            Image defaultImage = new Image("https://ui-avatars.com/api/?name=User&size=114&background=059669&color=fff&bold=true");
            profileImageView.setImage(defaultImage);
            imageContainer.getChildren().add(profileImageView);
        } catch (Exception e) {
            Label initialLabel = new Label("👤");
            initialLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
            initialLabel.setTextFill(Color.WHITE);
            imageContainer.getChildren().add(initialLabel);
        }

        HBox photoButtons = new HBox(15);
        photoButtons.setAlignment(Pos.CENTER);
        photoButtons.setPadding(new Insets(5, 0, 0, 0));

        Button choosePhotoBtn = new Button("Choisir");
        choosePhotoBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 6 18; -fx-background-radius: 20; -fx-cursor: hand; " +
                "-fx-font-weight: bold;");
        choosePhotoBtn.setOnAction(e -> chooseProfilePhoto());

        Button takePhotoBtn = new Button("Prendre");
        takePhotoBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 6 18; -fx-background-radius: 20; -fx-cursor: hand; " +
                "-fx-font-weight: bold;");
        takePhotoBtn.setOnAction(e -> openCamera());

        photoStatusLabel = new Label();
        photoStatusLabel.setFont(Font.font("Segoe UI", 10));
        photoStatusLabel.setTextFill(Color.GREEN);
        photoStatusLabel.setVisible(false);

        photoButtons.getChildren().addAll(choosePhotoBtn, takePhotoBtn);
        photoBox.getChildren().addAll(imageContainer, photoButtons, photoStatusLabel);

        // ========== PRÉNOM AVEC VALIDATION ==========
        VBox prenomBox = new VBox(2);
        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        prenomLabel.setTextFill(Color.web("#1e293b"));

        prenomField = new TextField();
        prenomField.setPromptText("Votre prénom");
        prenomField.setPrefHeight(38);
        prenomField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");

        prenomErrorLabel = new Label();
        prenomErrorLabel.setFont(Font.font("Segoe UI", 9));
        prenomErrorLabel.setTextFill(Color.web("#ef4444"));
        prenomErrorLabel.setManaged(false);
        prenomErrorLabel.setVisible(false);
        prenomErrorLabel.setPadding(new Insets(2, 0, 0, 5));

        prenomBox.getChildren().addAll(prenomLabel, prenomField, prenomErrorLabel);

        // ========== NOM AVEC VALIDATION ==========
        VBox nomBox = new VBox(2);
        Label nomLabel = new Label("Nom *");
        nomLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        nomLabel.setTextFill(Color.web("#1e293b"));

        nomField = new TextField();
        nomField.setPromptText("Votre nom");
        nomField.setPrefHeight(38);
        nomField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");

        nomErrorLabel = new Label();
        nomErrorLabel.setFont(Font.font("Segoe UI", 9));
        nomErrorLabel.setTextFill(Color.web("#ef4444"));
        nomErrorLabel.setManaged(false);
        nomErrorLabel.setVisible(false);
        nomErrorLabel.setPadding(new Insets(2, 0, 0, 5));

        nomBox.getChildren().addAll(nomLabel, nomField, nomErrorLabel);

        // ========== EMAIL AVEC VALIDATION ==========
        VBox emailBox = new VBox(2);
        Label emailLabel = new Label("Email *");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web("#1e293b"));

        emailField = new TextField();
        emailField.setPromptText("votre@email.com");
        emailField.setPrefHeight(38);
        emailField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");

        emailErrorLabel = new Label();
        emailErrorLabel.setFont(Font.font("Segoe UI", 9));
        emailErrorLabel.setTextFill(Color.web("#ef4444"));
        emailErrorLabel.setManaged(false);
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setPadding(new Insets(2, 0, 0, 5));

        emailBox.getChildren().addAll(emailLabel, emailField, emailErrorLabel);

        // ========== GENRE AVEC VALIDATION ==========
        VBox genreBox = new VBox(2);
        Label genreLabel = new Label("Genre *");
        genreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        genreLabel.setTextFill(Color.web("#1e293b"));

        genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreComboBox.setValue("Non spécifié");
        genreComboBox.setPrefHeight(38);
        genreComboBox.setMaxWidth(Double.MAX_VALUE);
        genreComboBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-background-color: #f8fafc; -fx-font-size: 12px;");

        genreErrorLabel = new Label();
        genreErrorLabel.setFont(Font.font("Segoe UI", 9));
        genreErrorLabel.setTextFill(Color.web("#ef4444"));
        genreErrorLabel.setManaged(false);
        genreErrorLabel.setVisible(false);
        genreErrorLabel.setPadding(new Insets(2, 0, 0, 5));

        genreBox.getChildren().addAll(genreLabel, genreComboBox, genreErrorLabel);

        // ========== RÔLE AVEC VALIDATION ==========
        VBox roleSection = new VBox(5);
        roleSection.setPadding(new Insets(5, 0, 5, 0));
        roleSection.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 10; -fx-padding: 12;");

        Label roleLabel = new Label("Choisissez votre rôle *");
        roleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        roleLabel.setTextFill(Color.web("#059669"));

        HBox roleButtons = new HBox(25);
        roleButtons.setAlignment(Pos.CENTER);
        roleButtons.setPadding(new Insets(5, 0, 5, 0));

        roleGroup = new ToggleGroup();

        // Admin
        VBox adminBox = new VBox(3);
        adminBox.setAlignment(Pos.CENTER);
        adminRadio = new RadioButton();
        adminRadio.setToggleGroup(roleGroup);
        adminRadio.setUserData("admin");

        Label adminLabel = new Label("👑 Admin");
        adminLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        adminLabel.setTextFill(Color.web("#1e293b"));
        adminBox.getChildren().addAll(adminRadio, adminLabel);

        // Organisateur
        VBox organisateurBox = new VBox(3);
        organisateurBox.setAlignment(Pos.CENTER);
        organisateurRadio = new RadioButton();
        organisateurRadio.setToggleGroup(roleGroup);
        organisateurRadio.setUserData("organisateur");

        Label organisateurLabel = new Label("📅 Organisateur");
        organisateurLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        organisateurLabel.setTextFill(Color.web("#1e293b"));
        organisateurBox.getChildren().addAll(organisateurRadio, organisateurLabel);

        // Participant
        VBox participantBox = new VBox(3);
        participantBox.setAlignment(Pos.CENTER);
        participantRadio = new RadioButton();
        participantRadio.setToggleGroup(roleGroup);
        participantRadio.setSelected(true);
        participantRadio.setUserData("participant");

        Label participantLabel = new Label("👤 Participant");
        participantLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        participantLabel.setTextFill(Color.web("#1e293b"));
        participantBox.getChildren().addAll(participantRadio, participantLabel);

        roleButtons.getChildren().addAll(adminBox, organisateurBox, participantBox);

        roleErrorLabel = new Label();
        roleErrorLabel.setFont(Font.font("Segoe UI", 9));
        roleErrorLabel.setTextFill(Color.web("#ef4444"));
        roleErrorLabel.setManaged(false);
        roleErrorLabel.setVisible(false);
        roleErrorLabel.setPadding(new Insets(2, 0, 0, 5));
        roleErrorLabel.setAlignment(Pos.CENTER);

        roleSection.getChildren().addAll(roleLabel, roleButtons, roleErrorLabel);

        // ========== MOT DE PASSE AVEC VALIDATION ==========
        VBox passwordBox = new VBox(2);
        Label passwordLabel = new Label("Mot de passe * (min. 8 caractères)");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        passwordLabel.setTextFill(Color.web("#1e293b"));

        StackPane passwordStack = new StackPane();

        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setPrefHeight(38);
        passwordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
            validatePassword(newVal);
        });

        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("••••••••");
        visiblePasswordField.setPrefHeight(38);
        visiblePasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
            validatePassword(newVal);
        });

        showPasswordCheckBox = new CheckBox("👁️");
        showPasswordCheckBox.setStyle("-fx-font-size: 14px; -fx-cursor: hand; -fx-background-color: transparent;");
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

        HBox strengthBox = new HBox(10);
        strengthBox.setAlignment(Pos.CENTER_LEFT);
        strengthBox.setPadding(new Insets(2, 0, 0, 0));

        passwordStrengthBar = new ProgressBar(0);
        passwordStrengthBar.setPrefWidth(120);
        passwordStrengthBar.setPrefHeight(8);

        strengthTextLabel = new Label("Faible");
        strengthTextLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        strengthBox.getChildren().addAll(passwordStrengthBar, strengthTextLabel);

        passwordErrorLabel = new Label();
        passwordErrorLabel.setFont(Font.font("Segoe UI", 9));
        passwordErrorLabel.setTextFill(Color.web("#ef4444"));
        passwordErrorLabel.setManaged(false);
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setPadding(new Insets(2, 0, 0, 5));

        passwordBox.getChildren().addAll(passwordLabel, passwordStack, strengthBox, passwordErrorLabel);

        // ========== CONFIRMATION MOT DE PASSE AVEC VALIDATION ==========
        VBox confirmBox = new VBox(2);
        Label confirmLabel = new Label("Confirmer le mot de passe *");
        confirmLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        confirmLabel.setTextFill(Color.web("#1e293b"));

        StackPane confirmStack = new StackPane();

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("••••••••");
        confirmPasswordField.setPrefHeight(38);
        confirmPasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateConfirmPassword());

        visibleConfirmPasswordField = new TextField();
        visibleConfirmPasswordField.setPromptText("••••••••");
        visibleConfirmPasswordField.setPrefHeight(38);
        visibleConfirmPasswordField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
        visibleConfirmPasswordField.setManaged(false);
        visibleConfirmPasswordField.setVisible(false);
        visibleConfirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateConfirmPassword());

        showConfirmPasswordCheckBox = new CheckBox("👁️");
        showConfirmPasswordCheckBox.setStyle("-fx-font-size: 14px; -fx-cursor: hand; -fx-background-color: transparent;");
        showConfirmPasswordCheckBox.setPadding(new Insets(0, 8, 0, 0));
        showConfirmPasswordCheckBox.setAlignment(Pos.CENTER_RIGHT);
        showConfirmPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isConfirmPasswordVisible = newVal;
            toggleConfirmPasswordVisibility();
        });

        HBox confirmWrapper = new HBox();
        confirmWrapper.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(confirmPasswordField, Priority.ALWAYS);
        HBox.setHgrow(visibleConfirmPasswordField, Priority.ALWAYS);
        confirmWrapper.getChildren().addAll(confirmPasswordField, visibleConfirmPasswordField);

        confirmStack.getChildren().addAll(confirmWrapper, showConfirmPasswordCheckBox);
        StackPane.setAlignment(showConfirmPasswordCheckBox, Pos.CENTER_RIGHT);

        confirmPasswordErrorLabel = new Label();
        confirmPasswordErrorLabel.setFont(Font.font("Segoe UI", 9));
        confirmPasswordErrorLabel.setTextFill(Color.web("#ef4444"));
        confirmPasswordErrorLabel.setManaged(false);
        confirmPasswordErrorLabel.setVisible(false);
        confirmPasswordErrorLabel.setPadding(new Insets(2, 0, 0, 5));

        confirmBox.getChildren().addAll(confirmLabel, confirmStack, confirmPasswordErrorLabel);

        // Bouton inscription
        registerBtn = new Button("Créer mon compte");
        registerBtn.setPrefHeight(45);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 22; -fx-cursor: hand;");
        registerBtn.setOnAction(e -> handleRegister());

        // Lien connexion
        HBox loginBox = new HBox(8);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(8, 0, 5, 0));

        Label alreadyLabel = new Label("Déjà un compte ?");
        alreadyLabel.setFont(Font.font("Segoe UI", 11));
        alreadyLabel.setTextFill(Color.web("#64748b"));

        backToLoginBtn = new Button("Se connecter");
        backToLoginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #059669; " +
                "-fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-border-color: transparent; " +
                "-fx-underline: true;");
        backToLoginBtn.setOnAction(e -> goToLogin());

        loginBox.getChildren().addAll(alreadyLabel, backToLoginBtn);

        // Label d'erreur général
        registerErrorLabel = new Label();
        registerErrorLabel.setFont(Font.font("Segoe UI", 10));
        registerErrorLabel.setTextFill(Color.web("#ef4444"));
        registerErrorLabel.setWrapText(true);
        registerErrorLabel.setAlignment(Pos.CENTER);
        registerErrorLabel.setMaxWidth(Double.MAX_VALUE);
        registerErrorLabel.setVisible(false);

        formContainer.getChildren().addAll(
                photoBox,
                prenomBox,
                nomBox,
                emailBox,
                genreBox,
                roleSection,
                passwordBox,
                confirmBox,
                registerBtn,
                loginBox,
                registerErrorLabel
        );

        scrollPane.setContent(formContainer);

        rightColumn.getChildren().addAll(toggleBox, mainTitle, scrollPane);

        return rightColumn;
    }

    private void setupValidations() {
        // Validation du prénom
        prenomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(prenomErrorLabel, "⚠ Prénom requis");
                prenomField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            } else {
                hideFieldError(prenomErrorLabel);
                prenomField.setStyle("-fx-border-color: #10b981; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            }
        });

        // Validation du nom
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(nomErrorLabel, "⚠ Nom requis");
                nomField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            } else {
                hideFieldError(nomErrorLabel);
                nomField.setStyle("-fx-border-color: #10b981; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            }
        });

        // Validation de l'email
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            String email = newVal.trim();
            if (email.isEmpty()) {
                showFieldError(emailErrorLabel, "⚠ Email requis");
                emailField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showFieldError(emailErrorLabel, "❌ Format d'email invalide");
                emailField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            } else {
                hideFieldError(emailErrorLabel);
                emailField.setStyle("-fx-border-color: #10b981; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            }
        });

        // Validation du genre
        genreComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                showFieldError(genreErrorLabel, "⚠ Genre requis");
            } else {
                hideFieldError(genreErrorLabel);
            }
        });

        // Validation du rôle
        roleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                showFieldError(roleErrorLabel, "⚠ Rôle requis");
            } else {
                hideFieldError(roleErrorLabel);
            }
        });
    }

    private void validatePassword(String password) {
        if (password.isEmpty()) {
            showFieldError(passwordErrorLabel, "⚠ Mot de passe requis");
        } else if (password.length() < 8) {
            showFieldError(passwordErrorLabel, "⚠ Minimum 8 caractères");
        } else {
            hideFieldError(passwordErrorLabel);
        }
    }

    private void validateConfirmPassword() {
        String password = isPasswordVisible ? visiblePasswordField.getText() : passwordField.getText();
        String confirmPassword = isConfirmPasswordVisible ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();

        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordErrorLabel, "⚠ Confirmation requise");
        } else if (!confirmPassword.equals(password)) {
            showFieldError(confirmPasswordErrorLabel, "❌ Les mots de passe ne correspondent pas");
        } else {
            hideFieldError(confirmPasswordErrorLabel);
        }
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideFieldError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthBar.setStyle("-fx-accent: #ef4444;");
            strengthTextLabel.setText("Faible");
            strengthTextLabel.setTextFill(Color.web("#ef4444"));
            return;
        }

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 10) score++;
        if (Pattern.compile("[0-9]").matcher(password).find()) score++;
        if (Pattern.compile("[a-z]").matcher(password).find()) score++;
        if (Pattern.compile("[A-Z]").matcher(password).find()) score++;
        if (Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) score++;

        double progress = Math.min(score / 6.0, 1.0);
        passwordStrengthBar.setProgress(progress);

        if (score <= 2) {
            passwordStrengthBar.setStyle("-fx-accent: #ef4444;");
            strengthTextLabel.setText("Faible");
            strengthTextLabel.setTextFill(Color.web("#ef4444"));
        } else if (score <= 4) {
            passwordStrengthBar.setStyle("-fx-accent: #f59e0b;");
            strengthTextLabel.setText("Moyen");
            strengthTextLabel.setTextFill(Color.web("#f59e0b"));
        } else {
            passwordStrengthBar.setStyle("-fx-accent: #10b981;");
            strengthTextLabel.setText("Fort");
            strengthTextLabel.setTextFill(Color.web("#10b981"));
        }
    }

    private void chooseProfilePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString(), 114, 114, true, true);
                profileImageView.setImage(image);
                profileImageView.setFitWidth(114);
                profileImageView.setFitHeight(114);

                Circle clipCircle = new Circle(57);
                clipCircle.setCenterX(57);
                clipCircle.setCenterY(57);
                profileImageView.setClip(clipCircle);

                imageContainer.getChildren().clear();
                imageContainer.getChildren().add(profileImageView);

                selectedPhotoFile = file;
                isUsingCamera = false;
                showPhotoStatus("✅ Photo chargée", Color.GREEN);
            } catch (Exception e) {
                showRegisterError("❌ Erreur lors du chargement de la photo");
                e.printStackTrace();
            }
        }
    }

    private void openCamera() {
        if (cameraService == null) {
            cameraService = new CameraService();
        }

        Stage cameraStage = new Stage();
        cameraStage.setTitle("Prendre une photo");
        cameraStage.initModality(Modality.APPLICATION_MODAL);
        cameraStage.initOwner(primaryStage);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e293b;");
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("📷 Prendre une photo");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

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

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button startBtn = new Button("▶ Démarrer");
        startBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");

        Button captureBtn = new Button("📸 Capturer");
        captureBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
        captureBtn.setDisable(true);

        Button acceptBtn = new Button("✅ Accepter");
        acceptBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 25; -fx-cursor: hand;");
        acceptBtn.setVisible(false);

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            stopCamera();
            cameraStage.close();
        });

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.WHITE);

        final boolean[] isCameraRunning = {false};
        final BufferedImage[] capturedImage = new BufferedImage[1];

        startBtn.setOnAction(e -> {
            new Thread(() -> {
                boolean started = cameraService.startCamera();
                Platform.runLater(() -> {
                    if (started) {
                        isCameraRunning[0] = true;
                        startBtn.setDisable(true);
                        captureBtn.setDisable(false);
                        statusLabel.setText("✅ Caméra prête");
                        cameraPane.getChildren().clear();
                        cameraFeed.setVisible(true);
                        cameraPane.getChildren().add(cameraFeed);

                        new Thread(() -> {
                            while (isCameraRunning[0]) {
                                BufferedImage frame = cameraService.captureImage();
                                if (frame != null) {
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

        captureBtn.setOnAction(e -> {
            statusLabel.setText("🔄 Capture...");

            new Thread(() -> {
                BufferedImage captured = cameraService.captureImage();
                Platform.runLater(() -> {
                    if (captured != null) {
                        capturedImage[0] = captured;

                        Image fxImage = SwingFXUtils.toFXImage(captured, null);
                        cameraFeed.setImage(fxImage);

                        captureBtn.setVisible(false);
                        acceptBtn.setVisible(true);
                        statusLabel.setText("✅ Photo capturée - Cliquez sur Accepter");
                    } else {
                        statusLabel.setText("❌ Erreur capture");
                    }
                });
            }).start();
        });

        acceptBtn.setOnAction(e -> {
            if (capturedImage[0] != null) {
                try {
                    BufferedImage original = capturedImage[0];

                    java.awt.Image awtImage = original.getScaledInstance(114, 114, java.awt.Image.SCALE_SMOOTH);
                    BufferedImage resizedImage = new BufferedImage(114, 114, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(awtImage, 0, 0, null);
                    g.dispose();

                    Image fxImage = SwingFXUtils.toFXImage(resizedImage, null);

                    profileImageView.setImage(fxImage);
                    profileImageView.setFitWidth(114);
                    profileImageView.setFitHeight(114);

                    Circle clipCircle = new Circle(57);
                    clipCircle.setCenterX(57);
                    clipCircle.setCenterY(57);
                    profileImageView.setClip(clipCircle);

                    imageContainer.getChildren().clear();
                    imageContainer.getChildren().add(profileImageView);

                    this.capturedPhoto = original;
                    isUsingCamera = true;

                    showPhotoStatus("✅ Photo prise avec succès", Color.GREEN);

                    stopCamera();
                    cameraStage.close();
                } catch (Exception ex) {
                    statusLabel.setText("❌ Erreur lors du traitement de l'image");
                    ex.printStackTrace();
                }
            }
        });

        buttons.getChildren().addAll(startBtn, captureBtn, acceptBtn, cancelBtn);
        layout.getChildren().addAll(title, cameraPane, buttons, statusLabel);

        Scene scene = new Scene(layout, 500, 500);
        cameraStage.setScene(scene);
        cameraStage.setOnCloseRequest(e -> stopCamera());
        cameraStage.showAndWait();
    }

    private void stopCamera() {
        if (cameraService != null) {
            cameraService.stopCamera();
        }
    }

    private void showPhotoStatus(String message, Color color) {
        photoStatusLabel.setText(message);
        photoStatusLabel.setTextFill(color);
        photoStatusLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> photoStatusLabel.setVisible(false));
        pause.play();
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

    private boolean validateAllFields() {
        boolean isValid = true;

        // Validation prénom
        if (prenomField.getText().trim().isEmpty()) {
            showFieldError(prenomErrorLabel, "⚠ Prénom requis");
            prenomField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            isValid = false;
        }

        // Validation nom
        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomErrorLabel, "⚠ Nom requis");
            nomField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            isValid = false;
        }

        // Validation email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showFieldError(emailErrorLabel, "⚠ Email requis");
            emailField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showFieldError(emailErrorLabel, "❌ Format d'email invalide");
            emailField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #f8fafc;");
            isValid = false;
        }

        // Validation genre
        if (genreComboBox.getValue() == null || genreComboBox.getValue().isEmpty()) {
            showFieldError(genreErrorLabel, "⚠ Genre requis");
            isValid = false;
        }

        // Validation rôle
        if (roleGroup.getSelectedToggle() == null) {
            showFieldError(roleErrorLabel, "⚠ Rôle requis");
            isValid = false;
        }

        // Validation mot de passe
        String password = isPasswordVisible ? visiblePasswordField.getText() : passwordField.getText();
        if (password.isEmpty()) {
            showFieldError(passwordErrorLabel, "⚠ Mot de passe requis");
            isValid = false;
        } else if (password.length() < 8) {
            showFieldError(passwordErrorLabel, "⚠ Minimum 8 caractères");
            isValid = false;
        }

        // Validation confirmation
        String confirmPassword = isConfirmPasswordVisible ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordErrorLabel, "⚠ Confirmation requise");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            showFieldError(confirmPasswordErrorLabel, "❌ Les mots de passe ne correspondent pas");
            isValid = false;
        }

        return isValid;
    }

    private void handleRegister() {
        if (!validateAllFields()) {
            showRegisterError("❌ Veuillez corriger les erreurs");
            return;
        }

        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String genre = genreComboBox.getValue();

        String role = "participant";
        if (roleGroup.getSelectedToggle() != null) {
            role = (String) roleGroup.getSelectedToggle().getUserData();
        }

        String password = isPasswordVisible ?
                visiblePasswordField.getText().trim() : passwordField.getText().trim();

        setButtonsDisabled(true);
        registerBtn.setText("Inscription en cours...");

        final String finalPrenom = prenom;
        final String finalNom = nom;
        final String finalEmail = email;
        final String finalRole = role;
        final int finalGenreId = getGenreId(genre);
        final String finalPassword = password;
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

                        boolean registered = authService.register(newUser, finalPassword);

                        if (registered) {
                            User created = authService.getUserByEmail(finalEmail);

                            if (created != null) {
                                if (finalIsUsingCamera && finalCapturedPhoto != null) {
                                    String photoPath = photoService.saveCameraPhoto(finalCapturedPhoto, created.getId());
                                    created.setPhoto(photoPath);
                                    userService.updateUser(created);
                                } else if (finalSelectedPhotoFile != null) {
                                    String photoPath = photoService.saveProfilePhoto(finalSelectedPhotoFile, created.getId());
                                    created.setPhoto(photoPath);
                                    userService.updateUser(created);
                                }
                            }

                            String roleDisplay = "";
                            switch(finalRole) {
                                case "admin": roleDisplay = "👑 Admin"; break;
                                case "organisateur": roleDisplay = "📅 Organisateur"; break;
                                case "participant": roleDisplay = "👤 Participant"; break;
                            }

                            showSuccessAlert("✅ Inscription réussie",
                                    "Bienvenue " + finalPrenom + " " + finalNom + " !\n\n" +
                                            "Votre compte a été créé avec succès.\n" +
                                            "Rôle: " + roleDisplay + "\n\n" +
                                            "Vous pouvez maintenant vous connecter.");
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
                        ex.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private int getGenreId(String genre) {
        if (genre == null) return 3;
        switch (genre) {
            case "Homme": return 1;
            case "Femme": return 2;
            default: return 3;
        }
    }

    private void goToLogin() {
        stopCamera();
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
        translate.setToX(5);
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
        adminRadio.setDisable(disabled);
        organisateurRadio.setDisable(disabled);
        participantRadio.setDisable(disabled);
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