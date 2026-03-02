package edu.Loopi.view;

import edu.Loopi.entities.Notification;
import edu.Loopi.entities.User;
import edu.Loopi.services.EventService;
import edu.Loopi.services.NotificationService;
import edu.Loopi.services.ProduitService;
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
import java.util.List;

public class OrganizerDashboard {
    private User currentUser;
    private BorderPane root;
    private EventService eventService;
    private ProduitService produitService;
    private NotificationService notificationService;
    private UserService userService;
    private NotificationsContentView notificationsContentView;
    private OrganizerProfileView organizerProfileView;

    public OrganizerDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.eventService = new EventService();
        this.produitService = new ProduitService();
        this.notificationService = new NotificationService();
        this.notificationsContentView = new NotificationsContentView(user, notificationService);
        this.organizerProfileView = new OrganizerProfileView(user, userService, this);
        SessionManager.login(user);
        System.out.println("✅ OrganizerDashboard initialisé pour: " + user.getEmail());
    }

    public void start(Stage stage) {
        try {
            stage.setTitle("LOOPI - Espace Organisateur");

            root = new BorderPane();
            root.setStyle("-fx-background-color: #f5f5f5;");

            // Header
            HBox header = createHeader();
            root.setTop(header);

            // Menu latéral
            VBox sidebar = createSidebar(stage);
            root.setLeft(sidebar);

            // Contenu principal (Accueil par défaut)
            VBox content = createMainContent();
            root.setCenter(content);

            Scene scene = new Scene(root, 1300, 800);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            SessionManager.printSessionInfo();
            System.out.println("✅ Dashboard organisateur affiché avec succès");

        } catch (Exception e) {
            System.err.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();

            // Interface de secours
            showFallbackUI(stage, e.getMessage());
        }
    }

    private void showFallbackUI(Stage stage, String errorMessage) {
        BorderPane fallbackRoot = new BorderPane();
        fallbackRoot.setStyle("-fx-background-color: #f5f5f5;");

        VBox fallbackContent = new VBox(20);
        fallbackContent.setAlignment(Pos.CENTER);
        fallbackContent.setPadding(new Insets(40));

        Label errorTitle = new Label("⚠️ Erreur d'affichage");
        errorTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        errorTitle.setTextFill(Color.RED);

        Label errorMsg = new Label("L'interface n'a pas pu être chargée correctement.\n" +
                "Cause: " + errorMessage);
        errorMsg.setFont(Font.font("Arial", 14));
        errorMsg.setTextFill(Color.web("#666"));
        errorMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        errorMsg.setWrapText(true);

        Button retryBtn = new Button("🔄 Réessayer");
        retryBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
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
        header.setStyle("-fx-background-color: #2196F3; -fx-padding: 15 30;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("LOOPI ORGANISATEUR");
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
        sidebar.setPrefWidth(260);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        // PROFIL BOX AVEC IMAGE
        HBox profileBox = new HBox(15);
        profileBox.setPadding(new Insets(0, 15, 20, 15));
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 0 0 1 0;");

        // Conteneur pour l'avatar avec image ou initiales
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(50, 50);

        // Cercle de fond
        Circle avatarCircle = new Circle(25);
        avatarCircle.setFill(Color.web("#2196F3"));

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
        Label profileName = new Label(currentUser.getPrenom() + " " + currentUser.getNom());
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
                profileBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 0 0 1 0; -fx-background-color: #34495e; -fx-background-radius: 8; -fx-padding: 0 15 20 15;"));
        profileBox.setOnMouseExited(e ->
                profileBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 0 0 1 0; -fx-background-color: transparent; -fx-padding: 0 15 20 15;"));

        VBox menuItems = new VBox(5);
        menuItems.setPadding(new Insets(10, 10, 10, 10));

        // Section PRODUITS
        Label produitsLabel = new Label("  PRODUITS");
        produitsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        produitsLabel.setTextFill(Color.web("#bdc3c7"));
        produitsLabel.setPadding(new Insets(10, 0, 5, 15));

        Button productsBtn = createMenuButton("📦 Mes produits");
        productsBtn.setOnAction(e -> showMyProducts());

        // Section ÉVÉNEMENTS
        Label eventsLabel = new Label("  ÉVÉNEMENTS");
        eventsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        eventsLabel.setTextFill(Color.web("#bdc3c7"));
        eventsLabel.setPadding(new Insets(20, 0, 5, 15));

        Button eventsBtn = createMenuButton("📅 Mes événements");
        eventsBtn.setOnAction(e -> showMyEvents());

        // Section PARTICIPANTS
        Label participantsLabel = new Label("  PARTICIPANTS");
        participantsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        participantsLabel.setTextFill(Color.web("#bdc3c7"));
        participantsLabel.setPadding(new Insets(20, 0, 5, 15));

        Button participantsBtn = createMenuButton("👥 Gestion participants");
        participantsBtn.setOnAction(e -> showParticipantsManagement());

        // Section COLLECTES
        Label collectesLabel = new Label("  COLLECTES");
        collectesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        collectesLabel.setTextFill(Color.web("#bdc3c7"));
        collectesLabel.setPadding(new Insets(20, 0, 5, 15));

        Button donationsBtn = createMenuButton("💰 Campagnes");
        donationsBtn.setOnAction(e -> showCollections());

        // Section STATISTIQUES
        Label statsLabel = new Label("  STATISTIQUES");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statsLabel.setTextFill(Color.web("#bdc3c7"));
        statsLabel.setPadding(new Insets(20, 0, 5, 15));

        Button statsBtn = createMenuButton("📊 Tableau de bord");
        statsBtn.setOnAction(e -> showStatistics());

        // Section NOTIFICATIONS
        Label notifLabel = new Label("  NOTIFICATIONS");
        notifLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        notifLabel.setTextFill(Color.web("#bdc3c7"));
        notifLabel.setPadding(new Insets(20, 0, 5, 15));

        Button notificationsBtn = createMenuButton("🔔 Notifications");
        updateNotificationBadge(notificationsBtn);
        notificationsBtn.setOnAction(e -> showNotificationsInContent());

        // Section PROFIL
        Label profilLabel = new Label("  PROFIL");
        profilLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        profilLabel.setTextFill(Color.web("#bdc3c7"));
        profilLabel.setPadding(new Insets(20, 0, 5, 15));

        Button profileBtn = createMenuButton("👤 Mon profil");
        profileBtn.setOnAction(e -> showProfile());

        // Espaceur
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Déconnexion
        Button logoutBtn = createMenuButton("🚪 Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout(stage));

        menuItems.getChildren().addAll(
                produitsLabel, productsBtn,
                eventsLabel, eventsBtn,
                participantsLabel, participantsBtn,
                collectesLabel, donationsBtn,
                statsLabel, statsBtn,
                notifLabel, notificationsBtn,
                profilLabel, profileBtn
        );

        sidebar.getChildren().addAll(profileBox, menuItems, spacer, logoutBtn);
        return sidebar;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
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

    private void updateNotificationBadge(Button btn) {
        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());

        if (nonLues > 0) {
            btn.setText("🔔 Notifications (" + nonLues + ")");
            btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        } else {
            btn.setText("🔔 Notifications");
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        }
    }

    private void refreshDashboard() {
        VBox sidebar = (VBox) root.getLeft();
        for (var node : sidebar.getChildren()) {
            if (node instanceof VBox) {
                VBox menuItems = (VBox) node;
                for (var item : menuItems.getChildren()) {
                    if (item instanceof Button) {
                        Button btn = (Button) item;
                        if (btn.getText().contains("🔔")) {
                            updateNotificationBadge(btn);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void showNotificationsInContent() {
        VBox content = notificationsContentView.getView();
        content.setOnMouseClicked(e -> refreshDashboard());
        root.setCenter(content);
        refreshDashboard();
    }

    public VBox createMainContent() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);

        Label welcome = new Label("Bienvenue dans votre espace Organisateur!");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcome.setTextFill(Color.web("#2196F3"));

        Label subtitle = new Label("Gérez vos produits recyclés, organisez des événements écologiques\n" +
                "et participez à l'économie circulaire");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#666"));
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        subtitle.setWrapText(true);

        // Statistiques
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(30, 0, 30, 0));

        int nbProduits = 0;
        int nbEvenements = 0;
        int nbNotifications = notificationService.countNotificationsNonLues(currentUser.getId());

        try {
            nbProduits = produitService.getProduitsParOrganisateur(currentUser.getId()).size();
        } catch (Exception e) {
            System.err.println("Erreur chargement produits: " + e.getMessage());
        }

        try {
            nbEvenements = eventService.countEventsByOrganisateur(currentUser.getId());
        } catch (Exception e) {
            System.err.println("Erreur chargement événements: " + e.getMessage());
        }

        VBox productsCard = createInfoCard("📦", "Produits", nbProduits + " produits actifs");
        VBox eventsCard = createInfoCard("📅", "Événements", nbEvenements + " événements");
        VBox notificationsCard = createInfoCard("🔔", "Notifications", nbNotifications + " non lues");

        statsBox.getChildren().clear();
        statsBox.getChildren().addAll(productsCard, eventsCard, notificationsCard);

        // Actions rapides
        VBox quickActions = new VBox(10);
        quickActions.setAlignment(Pos.CENTER);

        Label actionsTitle = new Label("Actions rapides");
        actionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        actionsTitle.setTextFill(Color.web("#333"));

        FlowPane actionsBox = new FlowPane();
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setHgap(15);
        actionsBox.setVgap(15);
        actionsBox.setPadding(new Insets(10, 0, 10, 0));

        Button addProductQuickBtn = createQuickActionButton("➕ Ajouter un produit");
        addProductQuickBtn.setOnAction(e -> showMyProducts());

        Button createEventQuickBtn = createQuickActionButton("📅 Créer un événement");
        createEventQuickBtn.setOnAction(e -> showMyEvents());

        Button viewParticipantsBtn = createQuickActionButton("👥 Voir participants");
        viewParticipantsBtn.setOnAction(e -> showParticipantsManagement());

        Button viewNotificationsBtn = createQuickActionButton("🔔 Voir notifications");
        viewNotificationsBtn.setOnAction(e -> showNotificationsInContent());

        actionsBox.getChildren().clear();
        actionsBox.getChildren().addAll(
                addProductQuickBtn,
                createEventQuickBtn,
                viewParticipantsBtn,
                viewNotificationsBtn
        );

        quickActions.getChildren().clear();
        quickActions.getChildren().addAll(actionsTitle, actionsBox);

        content.getChildren().clear();
        content.getChildren().addAll(welcome, subtitle, statsBox, quickActions);

        return content;
    }

    private VBox createInfoCard(String icon, String title, String value) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefSize(200, 150);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 36));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#333"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", 14));
        valueLabel.setTextFill(Color.web("#666"));

        card.getChildren().clear();
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);

        return card;
    }

    private Button createQuickActionButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");

        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));

        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));

        return btn;
    }

    // ============ MÉTHODES DE NAVIGATION ============

    private void showMyProducts() {
        try {
            Class.forName("edu.Loopi.view.GalerieView");
            GalerieView galerieView = new GalerieView();
            root.setCenter(galerieView.getView());
            System.out.println("✅ GalerieView chargée avec succès");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Classe GalerieView non trouvée");
            showComingSoon("Produits", "📦");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement GalerieView: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des produits: " + e.getMessage());
        }
    }

    private void showMyEvents() {
        try {
            Class.forName("edu.Loopi.view.EventView");
            EventView eventView = new EventView(currentUser);
            root.setCenter(eventView.getView());
            System.out.println("✅ EventView chargée avec succès");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Classe EventView non trouvée");
            showComingSoon("Événements", "📅");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement EventView: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des événements: " + e.getMessage());
        }
    }

    private void showParticipantsManagement() {
        try {
            Class.forName("edu.Loopi.view.ParticipantsView");
            ParticipantsView participantsView = new ParticipantsView(currentUser);
            root.setCenter(participantsView.getView());
            System.out.println("✅ ParticipantsView chargée avec succès");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Classe ParticipantsView non trouvée");
            showAlert("Information", "Le module de gestion des participants sera bientôt disponible.");
            showComingSoon("Gestion Participants", "👥");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement ParticipantsView: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la gestion des participants: " + e.getMessage());
        }
    }

    private void showCollections() {
        try {
            Class.forName("edu.Loopi.view.CollectionView");
            CollectionView collectionView = new CollectionView(currentUser);
            root.setCenter(collectionView.getView());
            System.out.println("✅ CollectionView chargée avec succès");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Classe CollectionView non trouvée");
            showComingSoon("Campagnes", "💰");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement CollectionView: " + e.getMessage());
            showComingSoon("Campagnes", "💰");
        }
    }

    private void showStatistics() {
        showComingSoon("Statistiques", "📊");
    }

    private void showProfile() {
        VBox profileView = organizerProfileView.createOrganizerProfileView();
        root.setCenter(profileView);
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
        VBox content = createMainContent();
        root.setCenter(content);
    }

    public void updateUserInfo(User updatedUser) {
        this.currentUser = updatedUser;
        this.organizerProfileView = new OrganizerProfileView(updatedUser, userService, this);

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
                    avatarCircle.setFill(Color.web("#2196F3"));
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
                        ((Label) profileInfo.getChildren().get(0)).setText(updatedUser.getPrenom() + " " + updatedUser.getNom());
                        ((Label) profileInfo.getChildren().get(1)).setText(updatedUser.getEmail());
                    }
                }
            }
        }
    }
}