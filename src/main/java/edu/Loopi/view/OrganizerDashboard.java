package edu.Loopi.view;

import edu.Loopi.entities.Notification;
import edu.Loopi.entities.User;
import edu.Loopi.services.NotificationService;
import edu.Loopi.services.ProduitService;
import edu.Loopi.services.EventService;
import edu.Loopi.services.UserService;
import edu.Loopi.tools.SessionManager;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("unchecked")
public class OrganizerDashboard {
    private User currentUser;
    private BorderPane root;
    private EventService eventService;
    private ProduitService produitService;
    private NotificationService notificationService;
    private UserService userService;
    private NotificationsContentView notificationsContentView;
    private OrganizerProfileView organizerProfileView;

    private Button notificationsBtn;
    private Label notificationBadge;
    private PauseTransition notificationRefreshTimer;

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

            HBox header = createHeader();
            root.setTop(header);

            VBox sidebar = createSidebar(stage);
            root.setLeft(sidebar);

            VBox content = createMainContent();
            root.setCenter(content);

            Scene scene = new Scene(root, 1300, 800);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            SessionManager.printSessionInfo();
            System.out.println("✅ Dashboard organisateur affiché avec succès");

            startNotificationRefresh();

        } catch (Exception e) {
            System.err.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            showFallbackUI(stage, e.getMessage());
        }
    }

    private void startNotificationRefresh() {
        notificationRefreshTimer = new PauseTransition(Duration.seconds(5));
        notificationRefreshTimer.setOnFinished(e -> {
            updateNotificationBadge();
            updateNotificationMenuBadge();
            notificationRefreshTimer.play();
        });
        notificationRefreshTimer.play();
    }

    private void updateNotificationBadge() {
        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());
        Platform.runLater(() -> {
            if (nonLues > 0) {
                notificationBadge.setText(String.valueOf(nonLues));
                notificationBadge.setVisible(true);

                ScaleTransition st = new ScaleTransition(Duration.millis(200), notificationBadge);
                st.setFromX(0.5);
                st.setFromY(0.5);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            } else {
                notificationBadge.setVisible(false);
            }
        });
    }

    private void updateNotificationMenuBadge() {
        VBox sidebar = (VBox) root.getLeft();
        if (sidebar != null) {
            for (var node : sidebar.getChildren()) {
                if (node instanceof VBox) {
                    VBox menuItems = (VBox) node;
                    for (var item : menuItems.getChildren()) {
                        if (item instanceof Button) {
                            Button btn = (Button) item;
                            if (btn.getText().contains("🔔")) {
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
                                break;
                            }
                        }
                    }
                }
            }
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

        StackPane notificationPane = new StackPane();

        notificationsBtn = new Button("🔔");
        notificationsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-cursor: hand;");
        notificationsBtn.setOnAction(e -> showNotificationsInContent());

        notificationBadge = new Label();
        notificationBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-min-width: 18px; -fx-min-height: 18px; -fx-background-radius: 9; -fx-alignment: center;");
        notificationBadge.setTranslateX(10);
        notificationBadge.setTranslateY(-10);
        notificationBadge.setVisible(false);

        notificationPane.getChildren().addAll(notificationsBtn, notificationBadge);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label welcome = new Label(currentUser.getNomComplet());
        welcome.setTextFill(Color.WHITE);
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label role = new Label(currentUser.getRole().toUpperCase());
        role.setTextFill(Color.web("#e0e0e0"));
        role.setFont(Font.font("Arial", 11));

        userInfo.getChildren().addAll(welcome, role);

        header.getChildren().addAll(title, spacer, notificationPane, userInfo);

        updateNotificationBadge();

        return header;
    }

    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(5);
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(260);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        HBox profileBox = new HBox(15);
        profileBox.setPadding(new Insets(0, 15, 20, 15));
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 0 0 1 0;");

        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(50, 50);

        Circle avatarCircle = new Circle(25);
        avatarCircle.setFill(Color.web("#2196F3"));

        ImageView profileImageView = loadProfileImageForSidebar(currentUser, 46);
        if (profileImageView != null) {
            avatarContainer.getChildren().add(profileImageView);
        } else {
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

        profileBox.setCursor(javafx.scene.Cursor.HAND);
        profileBox.setOnMouseClicked(e -> showProfile());
        profileBox.setOnMouseEntered(e ->
                profileBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 0 0 1 0; -fx-background-color: #34495e; -fx-background-radius: 8; -fx-padding: 0 15 20 15;"));
        profileBox.setOnMouseExited(e ->
                profileBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 0 0 1 0; -fx-background-color: transparent; -fx-padding: 0 15 20 15;"));

        VBox menuItems = new VBox(5);
        menuItems.setPadding(new Insets(10, 10, 10, 10));

        Label produitsLabel = new Label("  PRODUITS");
        produitsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        produitsLabel.setTextFill(Color.web("#bdc3c7"));
        produitsLabel.setPadding(new Insets(10, 0, 5, 15));

        Button productsBtn = createMenuButton("📦 Mes produits");
        productsBtn.setOnAction(e -> showMyProducts());

        Label eventsLabel = new Label("  ÉVÉNEMENTS");
        eventsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        eventsLabel.setTextFill(Color.web("#bdc3c7"));
        eventsLabel.setPadding(new Insets(20, 0, 5, 15));

        Button eventsBtn = createMenuButton("📅 Mes événements");
        eventsBtn.setOnAction(e -> showMyEvents());

        Label participantsLabel = new Label("  PARTICIPANTS");
        participantsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        participantsLabel.setTextFill(Color.web("#bdc3c7"));
        participantsLabel.setPadding(new Insets(20, 0, 5, 15));

        Button participantsBtn = createMenuButton("👥 Gestion participants");
        participantsBtn.setOnAction(e -> showParticipantsManagement());

        Label collectesLabel = new Label("  COLLECTES");
        collectesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        collectesLabel.setTextFill(Color.web("#bdc3c7"));
        collectesLabel.setPadding(new Insets(20, 0, 5, 15));

        Button donationsBtn = createMenuButton("💰 Campagnes");
        donationsBtn.setOnAction(e -> showCollections());

        Label statsLabel = new Label("  STATISTIQUES");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statsLabel.setTextFill(Color.web("#bdc3c7"));
        statsLabel.setPadding(new Insets(20, 0, 5, 15));

        Button statsBtn = createMenuButton("📊 Tableau de bord");
        statsBtn.setOnAction(e -> showStatistics());

        Label notifLabel = new Label("  NOTIFICATIONS");
        notifLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        notifLabel.setTextFill(Color.web("#bdc3c7"));
        notifLabel.setPadding(new Insets(20, 0, 5, 15));

        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());
        String notifText = nonLues > 0 ? "🔔 Notifications (" + nonLues + ")" : "🔔 Notifications";
        Button notificationsMenuBtn = createMenuButton(notifText);
        if (nonLues > 0) {
            notificationsMenuBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        }
        notificationsMenuBtn.setOnAction(e -> showNotificationsInContent());

        Label profilLabel = new Label("  PROFIL");
        profilLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        profilLabel.setTextFill(Color.web("#bdc3c7"));
        profilLabel.setPadding(new Insets(20, 0, 5, 15));

        Button profileBtn = createMenuButton("👤 Mon profil");
        profileBtn.setOnAction(e -> showProfile());

        menuItems.getChildren().addAll(
                produitsLabel, productsBtn,
                eventsLabel, eventsBtn,
                participantsLabel, participantsBtn,
                collectesLabel, donationsBtn,
                statsLabel, statsBtn,
                notifLabel, notificationsMenuBtn,
                profilLabel, profileBtn
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createMenuButton("🚪 Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout(stage));

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

    private void showNotificationsInContent() {
        VBox content = notificationsContentView.getView();

        for (var node : content.getChildren()) {
            if (node instanceof ListView) {
                ListView<Notification> notifList = (ListView<Notification>) node;

                notifList.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        Notification selected = notifList.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            showNotificationDetails(selected);
                            notificationService.marquerCommeLue(selected.getId());
                            updateNotificationBadge();
                            updateNotificationMenuBadge();

                            Platform.runLater(() -> {
                                List<Notification> updated = notificationService.getNotificationsForOrganisateur(currentUser.getId());
                                notifList.setItems(FXCollections.observableArrayList(updated));
                            });
                        }
                    }
                });
                break;
            }
        }

        root.setCenter(content);
        updateNotificationMenuBadge();
    }

    private void showNotificationDetails(Notification notification) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(root.getScene().getWindow());
        dialog.setTitle("Détails de la notification");

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(650);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = "";
        String color = "";
        String sourceInfo = "";

        switch (notification.getType()) {
            case "EVENEMENT_APPROUVE":
                icon = "✅";
                color = "#10b981";
                sourceInfo = "Admin: " + (notification.getNomAdmin() != null ? notification.getNomAdmin() : "Administrateur");
                break;
            case "EVENEMENT_REFUSE":
                icon = "❌";
                color = "#ef4444";
                sourceInfo = "Admin: " + (notification.getNomAdmin() != null ? notification.getNomAdmin() : "Administrateur");
                break;
            case "NOUVEAU_PARTICIPANT":
                icon = "👤";
                color = "#3b82f6";
                sourceInfo = "Participant: " + (notification.getNomParticipant() != null ? notification.getNomParticipant() : "Inconnu");
                break;
            case "PARTICIPANT_ANNULE":
                icon = "🚫";
                color = "#f97316";
                sourceInfo = "Participant: " + (notification.getNomParticipant() != null ? notification.getNomParticipant() : "Inconnu");
                break;
            case "EVENEMENT_PUBLIE":
                icon = "📢";
                color = "#8b5cf6";
                sourceInfo = "Système";
                break;
            case "EVENEMENT_MODIFIE":
                icon = "✏️";
                color = "#f59e0b";
                sourceInfo = "Système";
                break;
            default:
                icon = "🔔";
                color = "#6b7280";
                sourceInfo = "Système";
        }

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label(notification.getTitre());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(color));
        titleLabel.setWrapText(true);

        Label dateLabel = new Label(notification.getFormattedDate());
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(titleLabel, dateLabel);
        header.getChildren().addAll(iconLabel, headerText);

        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));

        VBox detailsBox = new VBox(15);
        detailsBox.setPadding(new Insets(10, 0, 10, 0));

        HBox sourceBox = new HBox(10);
        sourceBox.setAlignment(Pos.CENTER_LEFT);
        Label sourceIcon = new Label("📨");
        sourceIcon.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label sourceText = new Label("Source: " + sourceInfo);
        sourceText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sourceText.setTextFill(Color.web(color));
        sourceBox.getChildren().addAll(sourceIcon, sourceText);
        detailsBox.getChildren().add(sourceBox);

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setTextFill(Color.web("#1e293b"));
        detailsBox.getChildren().add(messageLabel);

        if (notification.getNomAdmin() != null && !notification.getNomAdmin().isEmpty() && !sourceInfo.contains("Admin")) {
            VBox adminBox = new VBox(8);
            adminBox.setPadding(new Insets(15));
            adminBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

            Label adminTitle = new Label("👤 Informations de l'administrateur");
            adminTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            adminTitle.setTextFill(Color.web("#2196F3"));

            GridPane adminGrid = new GridPane();
            adminGrid.setHgap(15);
            adminGrid.setVgap(8);

            adminGrid.add(new Label("Nom:"), 0, 0);
            Label adminName = new Label(notification.getNomAdmin());
            adminName.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            adminName.setTextFill(Color.web("#0f172a"));
            adminGrid.add(adminName, 1, 0);

            if (notification.getEmailAdmin() != null && !notification.getEmailAdmin().isEmpty()) {
                adminGrid.add(new Label("Email:"), 0, 1);
                Label adminEmail = new Label(notification.getEmailAdmin());
                adminEmail.setFont(Font.font("Arial", 13));
                adminEmail.setTextFill(Color.web("#2563eb"));
                adminGrid.add(adminEmail, 1, 1);
            }

            adminBox.getChildren().addAll(adminTitle, adminGrid);
            detailsBox.getChildren().add(adminBox);
        }

        if (notification.getCommentaire() != null && !notification.getCommentaire().isEmpty()) {
            VBox commentBox = new VBox(8);
            commentBox.setPadding(new Insets(15));
            commentBox.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 8; -fx-border-color: #fdba74; -fx-border-radius: 8;");

            Label commentTitle = new Label("💬 Commentaire");
            commentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            commentTitle.setTextFill(Color.web("#c2410c"));

            TextArea commentArea = new TextArea(notification.getCommentaire());
            commentArea.setWrapText(true);
            commentArea.setEditable(false);
            commentArea.setPrefRowCount(4);
            commentArea.setStyle("-fx-control-inner-background: #fff7ed; -fx-text-fill: #0f172a;");

            commentBox.getChildren().addAll(commentTitle, commentArea);
            detailsBox.getChildren().add(commentBox);
        }

        if (notification.getNomParticipant() != null && !notification.getNomParticipant().isEmpty() && !sourceInfo.contains("Participant")) {
            VBox participantBox = new VBox(8);
            participantBox.setPadding(new Insets(15));
            participantBox.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 8; -fx-border-color: #7dd3fc; -fx-border-radius: 8;");

            Label participantTitle = new Label("👤 Informations du participant");
            participantTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            participantTitle.setTextFill(Color.web("#0369a1"));

            GridPane participantGrid = new GridPane();
            participantGrid.setHgap(15);
            participantGrid.setVgap(8);

            participantGrid.add(new Label("Nom:"), 0, 0);
            Label partName = new Label(notification.getNomParticipant());
            partName.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            partName.setTextFill(Color.web("#0f172a"));
            participantGrid.add(partName, 1, 0);

            if (notification.getEmailParticipant() != null && !notification.getEmailParticipant().isEmpty()) {
                participantGrid.add(new Label("Email:"), 0, 1);
                Label partEmail = new Label(notification.getEmailParticipant());
                partEmail.setFont(Font.font("Arial", 13));
                partEmail.setTextFill(Color.web("#0369a1"));
                participantGrid.add(partEmail, 1, 1);
            }

            participantBox.getChildren().addAll(participantTitle, participantGrid);
            detailsBox.getChildren().add(participantBox);
        }

        if (notification.getEventTitre() != null && !notification.getEventTitre().isEmpty()) {
            HBox eventBox = new HBox(15);
            eventBox.setAlignment(Pos.CENTER_LEFT);
            eventBox.setPadding(new Insets(10));
            eventBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");

            Label eventIcon = new Label("📅");
            eventIcon.setFont(Font.font("Arial", FontWeight.BOLD, 18));

            Label eventLabel = new Label("Événement concerné:");
            eventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            eventLabel.setTextFill(Color.web("#475569"));

            Label eventName = new Label(notification.getEventTitre());
            eventName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            eventName.setTextFill(Color.web("#2196F3"));

            eventBox.getChildren().addAll(eventIcon, eventLabel, eventName);
            detailsBox.getChildren().add(eventBox);
        }

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(header, sep, detailsBox, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 700, 750);
        dialog.setScene(scene);
        dialog.show();
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

        statsBox.getChildren().addAll(productsCard, eventsCard, notificationsCard);

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

        actionsBox.getChildren().addAll(
                addProductQuickBtn,
                createEventQuickBtn,
                viewParticipantsBtn,
                viewNotificationsBtn
        );

        quickActions.getChildren().addAll(actionsTitle, actionsBox);

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

    private void showMyProducts() {
        try {
            Class.forName("edu.Loopi.view.GalerieView");
            GalerieView galerieView = new GalerieView();
            root.setCenter(galerieView.getView());
            System.out.println("✅ GalerieView chargée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement GalerieView: " + e.getMessage());
            showComingSoon("Produits", "📦");
        }
    }

    private void showMyEvents() {
        try {
            Class.forName("edu.Loopi.view.EventView");
            EventView eventView = new EventView(currentUser);
            root.setCenter(eventView.getView());
            System.out.println("✅ EventView chargée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement EventView: " + e.getMessage());
            showComingSoon("Événements", "📅");
        }
    }

    private void showParticipantsManagement() {
        try {
            Class.forName("edu.Loopi.view.ParticipantsView");
            ParticipantsView participantsView = new ParticipantsView(currentUser);
            root.setCenter(participantsView.getView());
            System.out.println("✅ ParticipantsView chargée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement ParticipantsView: " + e.getMessage());
            showComingSoon("Gestion Participants", "👥");
        }
    }

    private void showCollections() {
        try {
            Class.forName("edu.Loopi.view.CollectionView");
            User freshUser = userService.getUserById(currentUser.getId());
            this.currentUser = freshUser;
            CollectionView collectionView = new CollectionView(freshUser);
            root.setCenter(collectionView.getView());
            System.out.println("✅ CollectionView chargée avec succès");
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
        if (notificationRefreshTimer != null) {
            notificationRefreshTimer.stop();
        }
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

    public void showMainContent() {
        VBox content = createMainContent();
        root.setCenter(content);
    }

    public void updateUserInfo(User updatedUser) {
        this.currentUser = updatedUser;
        this.organizerProfileView = new OrganizerProfileView(updatedUser, userService, this);

        HBox header = (HBox) root.getTop();
        if (header != null && header.getChildren().size() >= 4) {
            VBox userInfo = (VBox) header.getChildren().get(3);
            if (userInfo != null && userInfo.getChildren().size() >= 2) {
                ((Label) userInfo.getChildren().get(0)).setText(updatedUser.getNomComplet());
            }
        }

        VBox sidebar = (VBox) root.getLeft();
        if (sidebar != null && sidebar.getChildren().size() > 0) {
            HBox profileBox = (HBox) sidebar.getChildren().get(0);
            if (profileBox != null) {
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

                if (profileBox.getChildren().size() >= 2) {
                    VBox profileInfo = (VBox) profileBox.getChildren().get(1);
                    if (profileInfo != null && profileInfo.getChildren().size() >= 2) {
                        ((Label) profileInfo.getChildren().get(0)).setText(updatedUser.getPrenom() + " " + updatedUser.getNom());
                        ((Label) profileInfo.getChildren().get(1)).setText(updatedUser.getEmail());
                    }
                }
            }
        }
        updateNotificationBadge();
        updateNotificationMenuBadge();
    }
}