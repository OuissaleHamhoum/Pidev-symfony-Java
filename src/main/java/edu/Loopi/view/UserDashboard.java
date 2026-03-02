package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.ParticipationService;
import edu.Loopi.services.UserService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;

// Imports des vues existantes
import edu.Loopi.view.ProductGalleryView;
import edu.Loopi.view.ParticipantCampaignView;
import edu.Loopi.view.DonationHistoryView;
import edu.Loopi.view.FavorisView;
import edu.Loopi.view.RecommendationView;

public class UserDashboard {
    private User currentUser;
    private BorderPane root;
    private ParticipationService participationService;
    private UserService userService;
    private EventViewParticipant eventView;
    private ProductGalleryView productGalleryView;
    private ParticipantCampaignView campaignView;
    private DonationHistoryView donationHistoryView;
    private UserProfileView userProfileView;

    public UserDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.participationService = new ParticipationService();
        SessionManager.login(user);

        // Initialisation des vues existantes
        this.eventView = new EventViewParticipant(currentUser);
        this.productGalleryView = new ProductGalleryView();
        this.campaignView = new ParticipantCampaignView(currentUser);
        this.donationHistoryView = new DonationHistoryView(currentUser);
        this.userProfileView = new UserProfileView(user, userService, this);
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

            // Afficher la vue par défaut (événements)
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
        header.setStyle("-fx-background-color: #059669; -fx-padding: 15 30;");
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
        sidebar.setStyle("-fx-background-color: #064e3b;");
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        // PROFIL BOX AVEC IMAGE
        HBox profileBox = new HBox(15);
        profileBox.setPadding(new Insets(0, 15, 20, 15));
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setStyle("-fx-border-color: #065f46; -fx-border-width: 0 0 1 0;");

        // Conteneur pour l'avatar avec image ou initiales
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(50, 50);

        // Cercle de fond
        Circle avatarCircle = new Circle(25);
        avatarCircle.setFill(Color.web("#059669"));

        // Charger l'image de profil si elle existe
        ImageView profileImageView = loadProfileImageForSidebar(currentUser, 46);
        if (profileImageView != null) {
            avatarContainer.getChildren().add(profileImageView);
        } else {
            // Sinon afficher les initiales
            String initials = getInitials(currentUser);
            Label avatarText = new Label(initials);
            avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            avatarText.setTextFill(Color.WHITE);
            avatarContainer.getChildren().addAll(avatarCircle, avatarText);
        }

        VBox profileInfo = new VBox(2);
        Label profileName = new Label(currentUser.getPrenom());
        profileName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        profileName.setTextFill(Color.WHITE);

        Label profileEmail = new Label(currentUser.getEmail());
        profileEmail.setFont(Font.font("Arial", 11));
        profileEmail.setTextFill(Color.web("#bdc3c7"));

        profileInfo.getChildren().addAll(profileName, profileEmail);
        profileBox.getChildren().addAll(avatarContainer, profileInfo);

        // Ajouter un effet de clic pour ouvrir le profil
        profileBox.setCursor(javafx.scene.Cursor.HAND);
        profileBox.setOnMouseClicked(e -> showProfile());
        profileBox.setOnMouseEntered(e ->
                profileBox.setStyle("-fx-border-color: #065f46; -fx-border-width: 0 0 1 0; -fx-background-color: #065f46; -fx-background-radius: 8; -fx-padding: 0 15 20 15;"));
        profileBox.setOnMouseExited(e ->
                profileBox.setStyle("-fx-border-color: #065f46; -fx-border-width: 0 0 1 0; -fx-background-color: transparent; -fx-padding: 0 15 20 15;"));

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

        Button browseBtn = createMenuButton("🛒 Galerie");
        browseBtn.setOnAction(e -> showProducts());

        Button recBtn = createMenuButton("🎯 Recommandations");
        recBtn.setOnAction(e -> showRecommendations());

        Button favorisBtn = createMenuButton("❤️ Mes favoris");
        favorisBtn.setOnAction(e -> showFavoris());



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
        logoutBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout(stage));

        menuItems.getChildren().addAll(
                eventsSection, eventsBtn, myParticipationsBtn,
                shopSection, browseBtn, recBtn, favorisBtn,
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
                btn.setStyle("-fx-background-color: #065f46; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;"));

        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;"));

        return btn;
    }

    // ============ MÉTHODES DE NAVIGATION ============

    private void showRecommendations() {
        try {
            RecommendationView recView = new RecommendationView();
            root.setCenter(recView.getView());
            System.out.println("✅ Recommandations affichées");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement recommandations: " + e.getMessage());
            showComingSoon("Recommandations", "🎯");
        }
    }

    private void showEvents() {
        try {
            if (eventView == null) {
                eventView = new EventViewParticipant(currentUser);
            }
            root.setCenter(eventView.getView());
            System.out.println("✅ Événements affichés");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement événements: " + e.getMessage());
            showComingSoon("Événements", "📅");
        }
    }

    private void showMyParticipations() {
        try {
            if (eventView == null) {
                eventView = new EventViewParticipant(currentUser);
            }
            eventView.showMyParticipations();
            root.setCenter(eventView.getView());
            System.out.println("✅ Mes participations affichées");
        } catch (Exception e) {
            System.err.println("❌ Erreur affichage participations: " + e.getMessage());
            showComingSoon("Mes participations", "👥");
        }
    }

    private void showProducts() {
        try {
            if (productGalleryView == null) {
                productGalleryView = new ProductGalleryView();
            }
            root.setCenter(productGalleryView.getView());
            System.out.println("✅ Galerie produits affichée");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement galerie: " + e.getMessage());
            showComingSoon("Galerie", "🛒");
        }
    }

    private void showFavoris() {
        try {
            FavorisView favorisView = new FavorisView();
            root.setCenter(favorisView.getView());
            System.out.println("✅ Page des favoris affichée");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement favoris: " + e.getMessage());
            showComingSoon("Mes favoris", "❤️");
        }
    }



    private void showCampaigns() {
        try {
            if (campaignView == null) {
                campaignView = new ParticipantCampaignView(currentUser);
            }
            root.setCenter(campaignView.getView());
            System.out.println("✅ Campagnes affichées");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement campagnes: " + e.getMessage());
            showComingSoon("Campagnes", "💰");
        }
    }

    private void showDonations() {
        try {
            if (donationHistoryView == null) {
                donationHistoryView = new DonationHistoryView(currentUser);
            }
            root.setCenter(donationHistoryView.getView());
            System.out.println("✅ Historique des dons affiché");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement dons: " + e.getMessage());
            showComingSoon("Mes dons", "❤️");
        }
    }

    private void showCoupons() {
        try {
            VBox couponsView = createCouponsView();
            root.setCenter(couponsView);
            System.out.println("✅ Coupons affichés");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement coupons: " + e.getMessage());
            showComingSoon("Mes coupons", "🎫");
        }
    }

    private void showProfile() {
        try {
            VBox profileView = userProfileView.createUserProfileView();
            root.setCenter(profileView);
            System.out.println("✅ Profil affiché");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement profil: " + e.getMessage());
            e.printStackTrace();
            showComingSoon("Mon profil", "👤");
        }
    }

    private void showSettings() {
        try {
            VBox settingsView = createSettingsView();
            root.setCenter(settingsView);
            System.out.println("✅ Paramètres affichés");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement paramètres: " + e.getMessage());
            showComingSoon("Paramètres", "⚙️");
        }
    }



    // Vue pour les coupons
    private VBox createCouponsView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label title = new Label("🎫 Mes Coupons");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#064e3b"));

        GridPane couponsGrid = new GridPane();
        couponsGrid.setHgap(20);
        couponsGrid.setVgap(20);
        couponsGrid.setPadding(new Insets(20));
        couponsGrid.setAlignment(Pos.CENTER);

        VBox coupon1 = createCouponCard("ECO10", "10% de réduction", "Valable jusqu'au 30/03/2024", "#10b981");
        VBox coupon2 = createCouponCard("LOYALTY20", "20% sur votre prochain achat", "Valable jusqu'au 15/04/2024", "#3b82f6");
        VBox coupon3 = createCouponCard("FREESHIP", "Livraison gratuite", "Valable jusqu'au 31/03/2024", "#8b5cf6");

        couponsGrid.add(coupon1, 0, 0);
        couponsGrid.add(coupon2, 1, 0);
        couponsGrid.add(coupon3, 2, 0);

        content.getChildren().addAll(title, couponsGrid);
        return content;
    }

    private VBox createCouponCard(String code, String description, String validity, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);

        Label codeLabel = new Label(code);
        codeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        codeLabel.setTextFill(Color.WHITE);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        Label validLabel = new Label(validity);
        validLabel.setFont(Font.font("Arial", 10));
        validLabel.setTextFill(Color.web("#f0f0f0"));

        Button copyBtn = new Button("Copier le code");
        copyBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + color + "; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        copyBtn.setOnAction(e -> {
            showAlert("Code copié !", "Le code " + code + " a été copié dans le presse-papier.");
        });

        card.getChildren().addAll(codeLabel, descLabel, validLabel, copyBtn);
        return card;
    }

    // Vue pour les paramètres
    private VBox createSettingsView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label title = new Label("⚙️ Paramètres");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#064e3b"));

        VBox notificationSettings = new VBox(10);
        notificationSettings.setPadding(new Insets(20));
        notificationSettings.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");

        Label notifTitle = new Label("Notifications");
        notifTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        CheckBox emailNotif = new CheckBox("Recevoir des notifications par email");
        emailNotif.setSelected(true);

        CheckBox smsNotif = new CheckBox("Recevoir des notifications par SMS");
        smsNotif.setSelected(false);

        CheckBox promoNotif = new CheckBox("Recevoir des offres promotionnelles");
        promoNotif.setSelected(true);

        notificationSettings.getChildren().addAll(notifTitle, emailNotif, smsNotif, promoNotif);

        VBox privacySettings = new VBox(10);
        privacySettings.setPadding(new Insets(20));
        privacySettings.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");

        Label privacyTitle = new Label("Confidentialité");
        privacyTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        CheckBox shareData = new CheckBox("Partager mes données avec les partenaires");
        shareData.setSelected(false);

        CheckBox publicProfile = new CheckBox("Profil public");
        publicProfile.setSelected(true);

        privacySettings.getChildren().addAll(privacyTitle, shareData, publicProfile);

        Button saveBtn = new Button("💾 Enregistrer les paramètres");
        saveBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;");
        saveBtn.setOnAction(e -> showAlert("Succès", "Paramètres enregistrés !"));

        content.getChildren().addAll(title, notificationSettings, privacySettings, saveBtn);
        return content;
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
        titleLabel.setTextFill(Color.web("#064e3b"));

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

    // ============ MÉTHODES POUR L'AVATAR ============

    private String getInitials(User user) {
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            initials += String.valueOf(user.getPrenom().charAt(0)).toUpperCase();
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            initials += String.valueOf(user.getNom().charAt(0)).toUpperCase();
        }
        return initials.isEmpty() ? "U" : initials;
    }

    private ImageView loadProfileImageForSidebar(User user, double size) {
        if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.jpg")) {
            try {
                File imageFile = new File(user.getPhoto());
                if (!imageFile.exists() && user.getPhoto().contains("profiles")) {
                    imageFile = new File("profiles/" + user.getPhoto().replace("profiles/", "").replace("profiles\\", ""));
                }
                if (imageFile.exists()) {
                    Image image = new Image(new FileInputStream(imageFile), size, size, true, true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(size);
                    imageView.setFitHeight(size);
                    imageView.setPreserveRatio(true);

                    // Créer un cercle de clipping pour arrondir l'image
                    Circle clip = new Circle(size/2, size/2, size/2);
                    imageView.setClip(clip);

                    return imageView;
                }
            } catch (Exception e) {
                System.out.println("Erreur chargement image sidebar: " + e.getMessage());
            }
        }
        return null;
    }

    // ============ MÉTHODES POUR LE PROFIL ============

    public void showMainContent() {
        try {
            showEvents(); // Retour à la vue par défaut des événements
        } catch (Exception e) {
            System.err.println("❌ Erreur affichage contenu principal: " + e.getMessage());
        }
    }

    public void updateUserInfo(User updatedUser) {
        this.currentUser = updatedUser;
        this.userProfileView = new UserProfileView(updatedUser, userService, this);

        // Mettre à jour l'affichage dans le header
        HBox header = (HBox) root.getTop();
        if (header != null && header.getChildren().size() >= 3) {
            VBox userInfo = (VBox) header.getChildren().get(2);
            if (userInfo != null && userInfo.getChildren().size() >= 2) {
                ((Label) userInfo.getChildren().get(0)).setText(updatedUser.getNomComplet());
            }
        }

        // Mettre à jour le profil dans la sidebar avec l'image
        VBox sidebar = (VBox) root.getLeft();
        if (sidebar != null && sidebar.getChildren().size() > 0) {
            HBox profileBox = (HBox) sidebar.getChildren().get(0);
            if (profileBox != null) {
                // Mettre à jour l'avatar
                StackPane avatarContainer = (StackPane) profileBox.getChildren().get(0);
                avatarContainer.getChildren().clear();

                ImageView profileImageView = loadProfileImageForSidebar(updatedUser, 46);
                if (profileImageView != null) {
                    avatarContainer.getChildren().add(profileImageView);
                } else {
                    Circle avatarCircle = new Circle(23);
                    avatarCircle.setFill(Color.web("#059669"));
                    String initials = getInitials(updatedUser);
                    Label avatarText = new Label(initials);
                    avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    avatarText.setTextFill(Color.WHITE);
                    avatarContainer.getChildren().addAll(avatarCircle, avatarText);
                }

                // Mettre à jour les informations
                if (profileBox.getChildren().size() >= 2) {
                    VBox profileInfo = (VBox) profileBox.getChildren().get(1);
                    if (profileInfo != null && profileInfo.getChildren().size() >= 2) {
                        ((Label) profileInfo.getChildren().get(0)).setText(updatedUser.getPrenom());
                        ((Label) profileInfo.getChildren().get(1)).setText(updatedUser.getEmail());
                    }
                }
            }
        }
    }
}