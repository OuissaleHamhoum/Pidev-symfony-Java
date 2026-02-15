package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.tools.MyConnection;
import edu.Loopi.tools.SessionManager;
import edu.Loopi.view.AdminDashboard.AdminDashboard;
import edu.Loopi.services.AuthService;
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
    private Label errorLabel;
    private Button loginBtn;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Test de connexion BD
        if (!MyConnection.testConnection()) {
            showAlert("Erreur", "Impossible de se connecter à la base de données.\nVérifiez que MySQL est démarré.");
            System.exit(1);
        }

        primaryStage.setTitle("Loopi - Connexion");
        primaryStage.setResizable(false);

        // Layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #4CAF50;");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Formulaire
        VBox form = createLoginForm();
        root.setCenter(form);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 380, 520);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Focus sur le champ email
        emailField.requestFocus();
    }

    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(40, 20, 20, 20));
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Loopi Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        header.getChildren().add(title);
        return header;
    }

    private VBox createLoginForm() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 30, 0, 30));

        VBox card = new VBox(15);
        card.setMaxWidth(300);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white;");

        // Titre
        Label connectLabel = new Label("Connectez-vous à votre compte");
        connectLabel.setFont(Font.font("Arial", 14));
        connectLabel.setTextFill(Color.GRAY);

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        emailField = new TextField();
        emailField.setPromptText("Entrez votre email");
        emailField.setPrefHeight(35);
        emailField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");

        // Mot de passe
        Label passwordLabel = new Label("Mot de passe");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");
        passwordField.setPrefHeight(35);
        passwordField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");

        // Ligne de séparation
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Bouton
        loginBtn = new Button("Se connecter");
        loginBtn.setPrefHeight(38);
        loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        // Lien inscription
        VBox registerBox = new VBox(5);
        registerBox.setAlignment(Pos.CENTER);

        Label newLabel = new Label("Nouveau sur Loopi?");
        newLabel.setFont(Font.font("Arial", 12));
        newLabel.setTextFill(Color.GRAY);

        Hyperlink registerLink = new Hyperlink("Créer un compte");
        registerLink.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        registerLink.setTextFill(Color.web("#4CAF50"));
        registerLink.setOnAction(e -> openRegister());

        registerBox.getChildren().addAll(newLabel, registerLink);

        // Erreur
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Actions clavier
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());

        card.getChildren().addAll(
                connectLabel,
                emailLabel, emailField,
                passwordLabel, passwordField,
                separator,
                loginBtn,
                registerBox,
                errorLabel
        );

        container.getChildren().add(card);
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);

        Label footerText = new Label("© 2024 Loopi - Plateforme Écologique");
        footerText.setFont(Font.font("Arial", 10));
        footerText.setTextFill(Color.WHITE);

        footer.getChildren().add(footerText);
        return footer;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        // Désactiver le bouton pendant la tentative
        loginBtn.setDisable(true);
        loginBtn.setText("Connexion...");

        new Thread(() -> {
            try {
                Thread.sleep(500);
                javafx.application.Platform.runLater(() -> {
                    try {
                        User user = authService.login(email, password);

                        if (user != null) {
                            System.out.println("✅ Connexion réussie: " + user.getEmail() + " (" + user.getRole() + ")");
                            SessionManager.login(user);
                            openDashboard(user);
                        } else {
                            errorLabel.setText("Email ou mot de passe incorrect");
                            errorLabel.setVisible(true);
                            loginBtn.setDisable(false);
                            loginBtn.setText("Se connecter");
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Erreur de connexion: " + ex.getMessage());
                        errorLabel.setVisible(true);
                        loginBtn.setDisable(false);
                        loginBtn.setText("Se connecter");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void openDashboard(User user) {
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
                    showAlert("Erreur", "Rôle non reconnu: " + role);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openRegister() {
        RegisterView registerView = new RegisterView();
        registerView.show(primaryStage);
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