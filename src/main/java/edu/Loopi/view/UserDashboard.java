package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.ParticipationService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class UserDashboard {
    private User currentUser;
    private BorderPane root;
    private ParticipationService participationService;
    private EventViewParticipant eventView;

    public UserDashboard(User user) {
        this.currentUser = user;
        this.participationService = new ParticipationService();
        SessionManager.login(user);
    }

    public void start(Stage stage) {
        try {
            stage.setTitle("LOOPI - Espace Participant");

            root = new BorderPane();
            root.setStyle("-fx-background-color: #f5f5f5;");

            HBox header = createHeader();
            root.setTop(header);

            VBox sidebar = createSidebar(stage);
            root.setLeft(sidebar);

            showEvents();

            Scene scene = new Scene(root, 1300, 800);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            SessionManager.printSessionInfo();
            System.out.println("✅ Dashboard participant affiché avec succès");

        } catch (Exception e) {
            System.err.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            showFallbackUI(stage);
        }
    }

    private void showFallbackUI(Stage stage) {
        BorderPane fallbackRoot = new BorderPane();
        fallbackRoot.setStyle("-fx-background-color: #f5f5f5;");

        VBox fallbackContent = new VBox(20);
        fallbackContent.setAlignment(Pos.CENTER);
        fallbackContent.setPadding(new Insets(40));

        Label errorTitle = new Label("⚠️ Erreur d'affichage");
        errorTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        errorTitle.setTextFill(Color.RED);

        Label errorMsg = new Label("L'interface n'a pas pu être chargée correctement.\n" +
                "Cause: Veuillez vérifier les fichiers de vue.");
        errorMsg.setFont(Font.font("Arial", 14));
        errorMsg.setTextFill(Color.web("#666"));
        errorMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        errorMsg.setWrapText(true);

        Button retryBtn = new Button("🔄 Réessayer");
        retryBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;");
        retryBtn.setOnAction(ev -> {
            try {
                start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        fallbackContent.getChildren().addAll(errorTitle, errorMsg, retryBtn);
        fallbackRoot.setCenter(fallbackContent);

        Scene fallbackScene = new Scene(fallbackRoot, 1200, 700);
        stage.setScene(fallbackScene);
        stage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #4CAF50; -fx-padding: 15 30;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("LOOPI PARTICIPANT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label welcome = new Label(currentUser.getNomComplet());
        welcome.setTextFill(Color.WHITE);
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label role = new Label(currentUser.getRole().toUpperCase());
        role.setTextFill(Color.web("#e0e0e0"));
        role.setFont(Font.font("Arial", 11));

        userInfo.getChildren().addAll(welcome, role);

        header.getChildren().addAll(title, spacer, userInfo);
        return header;
    }

    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(5);
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        HBox profileBox = new HBox(15);
        profileBox.setPadding(new Insets(0, 15, 20, 15));
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 0 0 1 0;");

        Label avatar = new Label("👤");
        avatar.setFont(Font.font("Arial", 32));
        avatar.setTextFill(Color.WHITE);

        VBox profileInfo = new VBox(2);
        Label profileName = new Label(currentUser.getPrenom());
        profileName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        profileName.setTextFill(Color.WHITE);

        Label profileEmail = new Label(currentUser.getEmail());
        profileEmail.setFont(Font.font("Arial", 11));
        profileEmail.setTextFill(Color.web("#bdc3c7"));

        profileInfo.getChildren().addAll(profileName, profileEmail);
        profileBox.getChildren().addAll(avatar, profileInfo);

        VBox menuItems = new VBox(5);
        menuItems.setPadding(new Insets(10, 10, 10, 10));

        Label eventsSection = new Label("  ÉVÉNEMENTS");
        eventsSection.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        eventsSection.setTextFill(Color.web("#bdc3c7"));
        eventsSection.setPadding(new Insets(10, 0, 5, 10));

        Button eventsBtn = createMenuButton("📅 Tous les événements");
        eventsBtn.setOnAction(e -> showEvents());

        Button myParticipationsBtn = createMenuButton("👥 Mes participations");
        myParticipationsBtn.setOnAction(e -> showMyParticipations());

        Label shopSection = new Label("  BOUTIQUE");
        shopSection.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        shopSection.setTextFill(Color.web("#bdc3c7"));
        shopSection.setPadding(new Insets(20, 0, 5, 10));

        Button browseBtn = createMenuButton("🛒 Explorer");
        browseBtn.setOnAction(e -> showProducts());

        Button ordersBtn = createMenuButton("📦 Mes commandes");
        ordersBtn.setOnAction(e -> showOrders());

        Label donationsSection = new Label("  COLLECTES");
        donationsSection.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        donationsSection.setTextFill(Color.web("#bdc3c7"));
        donationsSection.setPadding(new Insets(20, 0, 5, 10));

        Button campaignsBtn = createMenuButton("💰 Campagnes");
        campaignsBtn.setOnAction(e -> showCampaigns());

        Button myDonationsBtn = createMenuButton("❤️ Mes dons");
        myDonationsBtn.setOnAction(e -> showDonations());

        Button myCouponsBtn = createMenuButton("🎫 Mes coupons");
        myCouponsBtn.setOnAction(e -> showCoupons());

        Label profileSection = new Label("  PROFIL");
        profileSection.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        profileSection.setTextFill(Color.web("#bdc3c7"));
        profileSection.setPadding(new Insets(20, 0, 5, 10));

        Button profileBtn = createMenuButton("👤 Mon profil");
        profileBtn.setOnAction(e -> showProfile());

        Button settingsBtn = createMenuButton("⚙️ Paramètres");
        settingsBtn.setOnAction(e -> showSettings());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createMenuButton("🚪 Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout(stage));

        menuItems.getChildren().addAll(
                eventsSection, eventsBtn, myParticipationsBtn,
                shopSection, browseBtn, ordersBtn,
                donationsSection, campaignsBtn, myDonationsBtn, myCouponsBtn,
                profileSection, profileBtn, settingsBtn
        );

        sidebar.getChildren().addAll(profileBox, menuItems, spacer, logoutBtn);
        return sidebar;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(250);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");

        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;"));

        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;"));

        return btn;
    }

    // ============ MÉTHODES DE NAVIGATION ============

    private void showEvents() {
        try {
            if (eventView == null) {
                eventView = new EventViewParticipant(currentUser);
            }
            root.setCenter(eventView.getView());
            System.out.println("✅ EventViewParticipant chargée");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement EventViewParticipant: " + e.getMessage());
            e.printStackTrace();
            showComingSoon("Événements", "📅");
        }
    }

    private void showMyParticipations() {
        try {
            if (eventView != null) {
                eventView.showMyParticipations();
            } else {
                eventView = new EventViewParticipant(currentUser);
                eventView.showMyParticipations();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur affichage participations: " + e.getMessage());
            e.printStackTrace();
            showComingSoon("Mes participations", "👥");
        }
    }

    private void showProducts() {
        showComingSoon("Boutique", "🛒");
    }

    private void showOrders() {
        showComingSoon("Mes commandes", "📦");
    }

    private void showCampaigns() {
        showComingSoon("Campagnes", "💰");
    }

    private void showDonations() {
        showComingSoon("Mes dons", "❤️");
    }

    private void showCoupons() {
        showComingSoon("Mes coupons", "🎫");
    }

    private void showProfile() {
        showComingSoon("Mon profil", "👤");
    }

    private void showSettings() {
        showComingSoon("Paramètres", "⚙️");
    }

    private void showComingSoon(String title, String icon) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label comingSoon = new Label("Module en cours de développement...");
        comingSoon.setFont(Font.font("Arial", 14));
        comingSoon.setTextFill(Color.web("#7f8c8d"));

        VBox wrapper = new VBox(20);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMaxWidth(600);
        wrapper.getChildren().addAll(iconLabel, titleLabel, comingSoon);

        content.getChildren().add(wrapper);
        root.setCenter(content);
    }

    private void logout(Stage stage) {
        SessionManager.logout();
        stage.close();
        try {
            LoginView loginView = new LoginView();
            Stage loginStage = new Stage();
            loginView.start(loginStage);
        } catch (Exception e) {
            System.err.println("❌ Erreur retour login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}