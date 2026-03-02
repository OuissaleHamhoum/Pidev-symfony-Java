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

// Imports des vues existantes
import edu.Loopi.view.ProductGalleryView;
import edu.Loopi.view.ParticipantCampaignView;
import edu.Loopi.view.DonationHistoryView;
import edu.Loopi.view.FavorisView;

public class UserDashboard {
    private User currentUser;
    private BorderPane root;
    private ParticipationService participationService;
    private EventViewParticipant eventView;
    private ProductGalleryView productGalleryView;
    private ParticipantCampaignView campaignView;
    private DonationHistoryView donationHistoryView;

    public UserDashboard(User user) {
        this.currentUser = user;
        this.participationService = new ParticipationService();
        SessionManager.login(user);

        // Initialisation des vues existantes
        this.eventView = new EventViewParticipant(currentUser);
        this.productGalleryView = new ProductGalleryView();
        this.campaignView = new ParticipantCampaignView(currentUser);
        this.donationHistoryView = new DonationHistoryView(currentUser);
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

        HBox profileBox = new HBox(15);
        profileBox.setPadding(new Insets(0, 15, 20, 15));
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setStyle("-fx-border-color: #065f46; -fx-border-width: 0 0 1 0;");

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

        Button browseBtn = createMenuButton("🛒 Galerie");
        browseBtn.setOnAction(e -> showProducts());

        // NOUVEAU BOUTON RECOMMANDATIONS
        Button recBtn = createMenuButton("🎯 Recommandations");
        recBtn.setOnAction(e -> showRecommendations());

        // NOUVEAU BOUTON FAVORIS
        Button favorisBtn = createMenuButton("❤️ Mes favoris");
        favorisBtn.setOnAction(e -> showFavoris());

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
        logoutBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout(stage));

        menuItems.getChildren().addAll(
                eventsSection, eventsBtn, myParticipationsBtn,
                shopSection, browseBtn, recBtn, favorisBtn, ordersBtn, // recBtn ajouté ici
                donationsSection, campaignsBtn, myDonationsBtn, myCouponsBtn,
                profileSection, profileBtn, settingsBtn
        );

        sidebar.getChildren().addAll(profileBox, menuItems, spacer, logoutBtn);
        return sidebar;
    }

    // Ajouter la méthode :
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
// Dans UserDashboard.java, assurez-vous que les méthodes sont correctes :

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

    // NOUVELLE MÉTHODE POUR LES FAVORIS
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

    private void showOrders() {
        try {
            VBox ordersView = createOrdersView();
            root.setCenter(ordersView);
            System.out.println("✅ Commandes affichées");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement commandes: " + e.getMessage());
            showComingSoon("Mes commandes", "📦");
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

    // ... inside UserDashboard class ...

    private void showBadgeUnlockedAlert(String badgeName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Nouvelle Réussite !");
        alert.setHeaderText("Félicitations, " + currentUser.getPrenom() + " !");
        alert.setContentText("Vous venez de débloquer le badge : " + badgeName + "\n" +
                "Allez dans 'Mes dons' pour voir votre collection !");

        // Add a custom style if you want
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #ecfdf5;");

        alert.showAndWait();
    }

    private void showDonations() {
        try {
            // 1. RE-FETCH FRESH USER DATA
            // This ensures we have the latest 'total_metal', 'total_cardboard', etc.
            edu.Loopi.services.UserService userService = new edu.Loopi.services.UserService();
            this.currentUser = userService.getUserById(currentUser.getId());

            // 2. CREATE A FRESH VIEW INSTANCE
            // This triggers the constructor of DonationHistoryView which calls loadBadges()
            this.donationHistoryView = new DonationHistoryView(currentUser);

            // 3. UPDATE THE UI
            root.setCenter(donationHistoryView.getView());

            System.out.println("✅ Historique des dons rafraîchi et affiché");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement dons: " + e.getMessage());
            showComingSoon("Mes dons", "❤️");
        }
    }

// ... rest of the class ...

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
            VBox profileView = createProfileView();
            root.setCenter(profileView);
            System.out.println("✅ Profil affiché");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement profil: " + e.getMessage());
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

    // Vue pour les commandes
    private VBox createOrdersView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label title = new Label("📦 Mes Commandes");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#064e3b"));

        VBox ordersList = new VBox(10);
        ordersList.setPadding(new Insets(20));
        ordersList.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");

        Label order1 = new Label("✅ Commande #12345 - 15/02/2024 - 3 articles - 45.99€ (Livrée)");
        Label order2 = new Label("🔄 Commande #12344 - 10/02/2024 - 1 article - 12.50€ (En cours)");
        Label order3 = new Label("✅ Commande #12343 - 05/02/2024 - 2 articles - 28.75€ (Livrée)");
        Label order4 = new Label("⏳ Commande #12342 - 28/01/2024 - 4 articles - 67.30€ (En préparation)");

        ordersList.getChildren().addAll(order1, order2, order3, order4);

        content.getChildren().addAll(title, ordersList);
        return content;
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

    // Vue pour le profil
    private VBox createProfileView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label title = new Label("👤 Mon Profil");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#064e3b"));

        GridPane profileInfo = new GridPane();
        profileInfo.setHgap(20);
        profileInfo.setVgap(15);
        profileInfo.setPadding(new Insets(30));
        profileInfo.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");
        profileInfo.setAlignment(Pos.CENTER);

        profileInfo.add(new Label("Nom complet:"), 0, 0);
        profileInfo.add(new Label(currentUser.getNomComplet()), 1, 0);

        profileInfo.add(new Label("Email:"), 0, 1);
        profileInfo.add(new Label(currentUser.getEmail()), 1, 1);

        profileInfo.add(new Label("Rôle:"), 0, 2);
        profileInfo.add(new Label(currentUser.getRole()), 1, 2);

        profileInfo.add(new Label("Téléphone:"), 0, 3);
        profileInfo.add(new Label("+216 XX XXX XXX"), 1, 3);

        profileInfo.add(new Label("Adresse:"), 0, 4);
        profileInfo.add(new Label("Tunis, Tunisie"), 1, 4);

        Button editBtn = new Button("✏️ Modifier le profil");
        editBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;");
        editBtn.setOnAction(e -> showAlert("Modification", "Fonctionnalité de modification à venir..."));

        content.getChildren().addAll(title, profileInfo, editBtn);
        return content;
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
}