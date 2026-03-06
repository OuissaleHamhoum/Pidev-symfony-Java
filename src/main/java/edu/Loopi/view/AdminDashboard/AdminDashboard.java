package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.Notification;
import edu.Loopi.entities.User;
import edu.Loopi.services.NotificationService;
import edu.Loopi.services.UserService;
import edu.Loopi.tools.SessionManager;
import edu.Loopi.view.LoginView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboard {
    private User currentUser;
    private UserService userService;
    private NotificationService notificationService;
    private Stage primaryStage;
    private BorderPane root;
    private StackPane mainContentArea;
    private String currentView = "dashboard";
    private boolean sidebarCollapsed = false;

    // Vues
    private DashboardView dashboardView;
    private UserManagementView userManagementView;
    private UserProfileView userProfileView;
    private ProductManagementView productManagementView;
    private EventManagementView eventManagementView;
    private EventMapView eventMapView;
    private CollectionDashboardView collectionDashboardView;
    private SettingsView settingsView;

    // Composants d'en-tête
    private StackPane headerProfileContainer;
    private VBox headerProfileInfo;
    private Label headerProfileName;
    private Label headerProfileRole;
    private VBox sidebar;
    private Map<String, Button> sidebarButtons = new HashMap<>();

    // Gestion des notifications
    private Button notificationsBtn;
    private Label notificationBadge;
    private PauseTransition notificationRefreshTimer;

    private boolean isDarkMode = false;
    private Button themeToggleBtn;

    // Couleurs professionnelles
    private static final String LIGHT_BG = "#F3F4F6";
    private static final String LIGHT_SIDEBAR = "#FFFFFF";
    private static final String LIGHT_HEADER = "#FFFFFF";
    private static final String LIGHT_TEXT = "#1F2937";
    private static final String LIGHT_TEXT_SECONDARY = "#4B5563";
    private static final String LIGHT_TEXT_MUTED = "#6B7280";
    private static final String LIGHT_BORDER = "#E5E7EB";
    private static final String LIGHT_CARD = "#FFFFFF";
    private static final String LIGHT_HOVER = "#F9FAFB";

    private static final String DARK_BG = "#0B1120";
    private static final String DARK_SIDEBAR = "#1A202C";
    private static final String DARK_HEADER = "#1A202C";
    private static final String DARK_TEXT = "#F7FAFC";
    private static final String DARK_TEXT_SECONDARY = "#E2E8F0";
    private static final String DARK_TEXT_MUTED = "#A0AEC0";
    private static final String DARK_BORDER = "#2D3748";
    private static final String DARK_CARD = "#1A202C";
    private static final String DARK_HOVER = "#2D3748";

    private static final String ACCENT_COLOR = "#3182CE";
    private static final String SUCCESS_COLOR = "#38A169";
    private static final String WARNING_COLOR = "#DD6B20";
    private static final String DANGER_COLOR = "#E53E3E";

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.notificationService = new NotificationService();
        SessionManager.login(user);

        // Initialisation des vues
        this.dashboardView = new DashboardView(currentUser, userService, this);
        this.userManagementView = new UserManagementView(currentUser, userService, this);
        this.userProfileView = new UserProfileView(currentUser, userService, this);
        this.productManagementView = new ProductManagementView(currentUser, userService, this);
        this.eventManagementView = new EventManagementView(currentUser, userService, this);
        this.eventMapView = new EventMapView(currentUser, userService, this);
        this.collectionDashboardView = new CollectionDashboardView(currentUser, userService, this);
        this.settingsView = new SettingsView(currentUser, userService, this);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("LOOPI - Administration");

        try {
            InputStream logoStream = getClass().getResourceAsStream("/images/logo/logo.png");
            if (logoStream != null) {
                Image icon = new Image(logoStream);
                primaryStage.getIcons().add(icon);
                System.out.println("✅ Logo chargé depuis: /images/logo/logo.png");
            } else {
                File logoFile = new File("src/main/resources/images/logo/logo.png");
                if (logoFile.exists()) {
                    Image icon = new Image(new FileInputStream(logoFile));
                    primaryStage.getIcons().add(icon);
                    System.out.println("✅ Logo chargé depuis: " + logoFile.getAbsolutePath());
                } else {
                    System.out.println("⚠️ Logo non trouvé, utilisation de l'icône par défaut");
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur chargement logo: " + e.getMessage());
        }

        root = new BorderPane();

        VBox header = createHeader();
        sidebar = createSidebar();

        ScrollPane sidebarScrollPane = new ScrollPane(sidebar);
        sidebarScrollPane.setFitToWidth(true);
        sidebarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sidebarScrollPane.setPrefWidth(260);

        root.setLeft(sidebarScrollPane);
        root.setTop(header);

        mainContentArea = new StackPane();
        mainContentArea.setPadding(new Insets(24));
        root.setCenter(mainContentArea);

        showDashboard();

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        applyTheme();
        startNotificationRefresh();
    }

    private void startNotificationRefresh() {
        notificationRefreshTimer = new PauseTransition(Duration.seconds(5));
        notificationRefreshTimer.setOnFinished(e -> {
            updateNotificationBadge();
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
            } else {
                notificationBadge.setVisible(false);
            }
        });
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setPadding(new Insets(12, 24, 12, 24));
        header.setStyle(getHeaderStyle());

        HBox headerContent = new HBox(16);
        headerContent.setAlignment(Pos.CENTER_LEFT);

        Button menuToggle = new Button("☰");
        menuToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getTextColor() +
                "; -fx-font-size: 22px; -fx-cursor: hand; -fx-padding: 8; -fx-min-width: 40;");
        menuToggle.setOnAction(e -> toggleSidebar());

        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        try {
            InputStream logoStream = getClass().getResourceAsStream("/images/logo/logo.png");
            Image logoImage = null;

            if (logoStream != null) {
                logoImage = new Image(logoStream, 48, 48, true, true);
            } else {
                File logoFile = new File("src/main/resources/images/logo/logo.png");
                if (logoFile.exists()) {
                    logoImage = new Image(new FileInputStream(logoFile), 48, 48, true, true);
                }
            }

            if (logoImage != null && !logoImage.isError()) {
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitWidth(48);
                logoView.setFitHeight(48);
                logoView.setPreserveRatio(true);
                logoBox.getChildren().add(logoView);
            } else {
                Label logoIcon = new Label("♻️");
                logoIcon.setFont(Font.font("System", 40));
                logoBox.getChildren().add(logoIcon);
            }
        } catch (Exception e) {
            Label logoIcon = new Label("♻️");
            logoIcon.setFont(Font.font("System", 40));
            logoBox.getChildren().add(logoIcon);
        }

        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: " + (isDarkMode ? DARK_CARD : "#F9FAFB") +
                "; -fx-background-radius: 8; -fx-border-color: " + getBorderColor() + "; -fx-border-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-text-fill: " + getTextColor() + "; -fx-prompt-text-fill: " + getTextColorMuted() + ";");
        searchField.setPrefWidth(280);
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                performGlobalSearch(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getTextColorMuted() +
                "; -fx-cursor: hand; -fx-padding: 8 16; -fx-font-size: 16px;");
        searchBtn.setOnAction(e -> performGlobalSearch(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        themeToggleBtn = new Button(isDarkMode ? "☀️" : "🌙");
        themeToggleBtn.setStyle("-fx-background-color: " + (isDarkMode ? DARK_CARD : "#F9FAFB") +
                "; -fx-text-fill: " + getTextColor() + "; -fx-font-size: 18px; -fx-padding: 8 14; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: " + getBorderColor() + "; -fx-border-radius: 8;");
        themeToggleBtn.setOnAction(e -> toggleTheme());

        // Bouton notification avec badge
        StackPane notificationPane = new StackPane();

        notificationsBtn = new Button("🔔");
        notificationsBtn.setStyle("-fx-background-color: " + (isDarkMode ? DARK_CARD : "#F9FAFB") +
                "; -fx-text-fill: " + getTextColor() + "; -fx-font-size: 18px; -fx-padding: 8 14; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: " + getBorderColor() + "; -fx-border-radius: 8;");
        notificationsBtn.setTooltip(new Tooltip("Notifications"));
        notificationsBtn.setOnAction(e -> showNotifications());

        notificationBadge = new Label();
        notificationBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-min-width: 18px; -fx-min-height: 18px; -fx-background-radius: 9; -fx-alignment: center;");
        notificationBadge.setTranslateX(10);
        notificationBadge.setTranslateY(-10);
        notificationBadge.setVisible(false);

        notificationPane.getChildren().addAll(notificationsBtn, notificationBadge);

        HBox profileBox = new HBox(10);
        profileBox.setAlignment(Pos.CENTER_RIGHT);

        headerProfileInfo = new VBox(2);
        headerProfileInfo.setAlignment(Pos.CENTER_RIGHT);
        headerProfileName = new Label(currentUser.getNomComplet());
        headerProfileName.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        headerProfileName.setTextFill(Color.web(getTextColor()));

        headerProfileRole = new Label(getRoleInFrench(currentUser.getRole()));
        headerProfileRole.setFont(Font.font("System", FontWeight.NORMAL, 12));
        headerProfileRole.setTextFill(Color.web(getTextColorMuted()));

        headerProfileInfo.getChildren().addAll(headerProfileName, headerProfileRole);

        headerProfileContainer = new StackPane();
        headerProfileContainer.setStyle("-fx-cursor: hand;");
        headerProfileContainer.setOnMouseClicked(e -> showUserProfileInMain());

        Circle profileCircle = new Circle(18);
        profileCircle.setFill(Color.web(ACCENT_COLOR));

        ImageView profileImageView = loadProfileImage(currentUser, 36);
        if (profileImageView != null) {
            headerProfileContainer.getChildren().add(profileImageView);
        } else {
            String initials = getInitials(currentUser);
            Label profileText = new Label(initials);
            profileText.setFont(Font.font("System", FontWeight.BOLD, 14));
            profileText.setTextFill(Color.WHITE);
            headerProfileContainer.getChildren().addAll(profileCircle, profileText);
        }

        profileBox.getChildren().addAll(headerProfileInfo, headerProfileContainer);

        headerContent.getChildren().addAll(menuToggle, logoBox, searchBox, spacer, themeToggleBtn, notificationPane, profileBox);
        header.getChildren().add(headerContent);

        return header;
    }

    private void showNotifications() {
        Stage notifStage = new Stage();
        notifStage.setTitle("Notifications");
        notifStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        notifStage.initOwner(primaryStage);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(700);
        content.setPrefHeight(600);
        content.setStyle("-fx-background-color: " + getCardBg() + ";");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: " + getBorderColor() + "; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label("🔔");
        iconLabel.setFont(Font.font("System", 32));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label("Notifications");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(getTextColor()));

        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());
        Label countLabel = new Label(nonLues + " notification(s) non lue(s)");
        countLabel.setFont(Font.font("System", 14));
        countLabel.setTextFill(Color.web(getTextColorMuted()));

        headerText.getChildren().addAll(titleLabel, countLabel);
        header.getChildren().addAll(iconLabel, headerText);

        ListView<Notification> notifList = new ListView<>();
        notifList.setPrefHeight(400);
        notifList.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                } else {
                    VBox cell = new VBox(8);
                    cell.setPadding(new Insets(12));
                    cell.setStyle("-fx-background-color: " + (n.isRead() ? "#f8fafc" : "#eff6ff") +
                            "; -fx-background-radius: 8; -fx-border-color: " + (n.isRead() ? getBorderColor() : "#3b82f6") +
                            "; -fx-border-radius: 8;");

                    HBox titleRow = new HBox(10);
                    titleRow.setAlignment(Pos.CENTER_LEFT);

                    String typeIcon = n.getType().contains("APPROUVE") ? "✅" :
                            n.getType().contains("REFUSE") ? "❌" :
                                    n.getType().contains("PUBLIE") ? "📢" :
                                            n.getType().contains("NOUVEAU") ? "👤" :
                                                    n.getType().contains("ANNULE") ? "🚫" : "📝";

                    Label typeIconLabel = new Label(typeIcon);
                    typeIconLabel.setFont(Font.font("System", 16));

                    Label titreNotifLabel = new Label(n.getTitre());
                    titreNotifLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    titreNotifLabel.setTextFill(Color.web(getTextColor()));

                    titleRow.getChildren().addAll(typeIconLabel, titreNotifLabel);

                    if (!n.isRead()) {
                        Label newBadge = new Label("NOUVEAU");
                        newBadge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                                "-fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 10; -fx-font-weight: bold;");
                        titleRow.getChildren().add(newBadge);
                    }

                    Label messageLabel = new Label(n.getMessage());
                    messageLabel.setWrapText(true);
                    messageLabel.setFont(Font.font("System", 12));
                    messageLabel.setTextFill(Color.web(getTextColorMuted()));

                    VBox detailsBox = new VBox(5);
                    if (n.getNomOrganisateur() != null && !n.getNomOrganisateur().isEmpty()) {
                        HBox orgRow = new HBox(10);
                        orgRow.setAlignment(Pos.CENTER_LEFT);
                        Label orgIcon = new Label("👤");
                        orgIcon.setFont(Font.font("System", 12));
                        Label orgText = new Label("Organisateur: " + n.getNomOrganisateur());
                        orgText.setFont(Font.font("System", 11));
                        orgText.setTextFill(Color.web("#3b82f6"));
                        orgRow.getChildren().addAll(orgIcon, orgText);
                        detailsBox.getChildren().add(orgRow);
                    }

                    if (n.getNomParticipant() != null && !n.getNomParticipant().isEmpty()) {
                        HBox partRow = new HBox(10);
                        partRow.setAlignment(Pos.CENTER_LEFT);
                        Label partIcon = new Label("👤");
                        partIcon.setFont(Font.font("System", 12));
                        Label partText = new Label("Participant: " + n.getNomParticipant());
                        partText.setFont(Font.font("System", 11));
                        partText.setTextFill(Color.web("#10b981"));
                        partRow.getChildren().addAll(partIcon, partText);
                        detailsBox.getChildren().add(partRow);
                    }

                    if (n.getCommentaire() != null && !n.getCommentaire().isEmpty()) {
                        HBox commentRow = new HBox(10);
                        commentRow.setAlignment(Pos.CENTER_LEFT);
                        Label commentIcon = new Label("💬");
                        commentIcon.setFont(Font.font("System", 12));
                        Label commentText = new Label("Commentaire: " + n.getCommentaire());
                        commentText.setWrapText(true);
                        commentText.setFont(Font.font("System", 11));
                        commentText.setTextFill(Color.web("#f59e0b"));
                        commentRow.getChildren().addAll(commentIcon, commentText);
                        detailsBox.getChildren().add(commentRow);
                    }

                    Label dateLabel = new Label(n.getFormattedDate());
                    dateLabel.setFont(Font.font("System", 10));
                    dateLabel.setTextFill(Color.web(getTextColorMuted()));

                    cell.getChildren().addAll(titleRow, messageLabel);
                    if (!detailsBox.getChildren().isEmpty()) {
                        cell.getChildren().add(detailsBox);
                    }
                    cell.getChildren().add(dateLabel);

                    setGraphic(cell);
                }
            }
        });

        List<Notification> notifications = notificationService.getNotificationsForAdmin(currentUser.getId());
        notifList.setItems(FXCollections.observableArrayList(notifications));

        notifList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Notification selected = notifList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showNotificationDetails(selected);
                    notificationService.marquerCommeLue(selected.getId());
                    updateNotificationBadge();
                }
            }
        });

        Button markAllReadBtn = new Button("✓ Tout marquer comme lu");
        markAllReadBtn.setStyle("-fx-background-color: " + getAccentColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");
        markAllReadBtn.setOnAction(e -> {
            notificationService.marquerToutesCommeLues(currentUser.getId());
            updateNotificationBadge();
            showNotifications();
        });

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: " + getDangerColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> notifStage.close());

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(markAllReadBtn, closeBtn);

        content.getChildren().addAll(header, notifList, buttonBox);
        VBox.setVgrow(notifList, Priority.ALWAYS);

        Scene scene = new Scene(content);
        notifStage.setScene(scene);
        notifStage.show();
    }

    private void showNotificationDetails(Notification notification) {
        Stage dialog = new Stage();
        dialog.setTitle("Détails de la notification");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(600);
        content.setStyle("-fx-background-color: " + getCardBg() + ";");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: " + getBorderColor() + "; -fx-border-width: 0 0 1 0;");

        String icon = notification.getType().contains("APPROUVE") ? "✅" :
                notification.getType().contains("REFUSE") ? "❌" :
                        notification.getType().contains("PUBLIE") ? "📢" :
                                notification.getType().contains("NOUVEAU") ? "👤" :
                                        notification.getType().contains("ANNULE") ? "🚫" : "📝";

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 32));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label(notification.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(getTextColor()));
        titleLabel.setWrapText(true);

        Label dateLabel = new Label(notification.getFormattedDate());
        dateLabel.setFont(Font.font("System", 13));
        dateLabel.setTextFill(Color.web(getTextColorMuted()));

        headerText.getChildren().addAll(titleLabel, dateLabel);
        header.getChildren().addAll(iconLabel, headerText);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(12);
        infoGrid.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;

        infoGrid.add(new Label("Message:"), 0, row);
        TextArea messageArea = new TextArea(notification.getMessage());
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(3);
        infoGrid.add(messageArea, 1, row);
        row++;

        String source = "";
        if (notification.getNomOrganisateur() != null) {
            source = "Organisateur: " + notification.getNomOrganisateur();
            if (notification.getEmailOrganisateur() != null) {
                source += " (" + notification.getEmailOrganisateur() + ")";
            }
        } else if (notification.getNomParticipant() != null) {
            source = "Participant: " + notification.getNomParticipant();
            if (notification.getEmailParticipant() != null) {
                source += " (" + notification.getEmailParticipant() + ")";
            }
        } else if (notification.getNomAdmin() != null) {
            source = "Admin: " + notification.getNomAdmin();
            if (notification.getEmailAdmin() != null) {
                source += " (" + notification.getEmailAdmin() + ")";
            }
        } else {
            source = "Système";
        }

        infoGrid.add(new Label("Source:"), 0, row);
        Label sourceValue = new Label(source);
        sourceValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        sourceValue.setTextFill(Color.web(getAccentColor()));
        infoGrid.add(sourceValue, 1, row);
        row++;

        if (notification.getCommentaire() != null && !notification.getCommentaire().isEmpty()) {
            infoGrid.add(new Label("Commentaire:"), 0, row);
            TextArea commentArea = new TextArea(notification.getCommentaire());
            commentArea.setWrapText(true);
            commentArea.setEditable(false);
            commentArea.setPrefRowCount(3);
            infoGrid.add(commentArea, 1, row);
            row++;
        }

        if (notification.getEventTitre() != null && !notification.getEventTitre().isEmpty()) {
            infoGrid.add(new Label("Événement:"), 0, row);
            Label eventValue = new Label(notification.getEventTitre());
            eventValue.setFont(Font.font("System", FontWeight.BOLD, 13));
            eventValue.setTextFill(Color.web(getSuccessColor()));
            infoGrid.add(eventValue, 1, row);
            row++;
        }

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(450);
        infoGrid.getColumnConstraints().addAll(col1, col2);

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: " + getAccentColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(header, infoGrid, closeBtn);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(260);
        sidebar.setStyle(getSidebarStyle());
        sidebar.setPadding(new Insets(20, 12, 20, 12));

        VBox navMenu = new VBox(4);
        navMenu.setPadding(new Insets(0, 0, 20, 0));

        // Section PRINCIPAL
        Label mainSection = new Label("  PRINCIPAL");
        mainSection.setFont(Font.font("System", FontWeight.BOLD, 12));
        mainSection.setTextFill(Color.web(getTextColorMuted()));
        mainSection.setPadding(new Insets(0, 0, 5, 12));
        navMenu.getChildren().add(mainSection);

        Button dashboardBtn = createSidebarButton("📊", "Tableau de bord", "dashboard", true);
        sidebarButtons.put("dashboard", dashboardBtn);
        navMenu.getChildren().add(dashboardBtn);

        // Section UTILISATEURS
        Label usersSection = new Label("  UTILISATEURS");
        usersSection.setFont(Font.font("System", FontWeight.BOLD, 12));
        usersSection.setTextFill(Color.web(getTextColorMuted()));
        usersSection.setPadding(new Insets(15, 0, 5, 12));
        navMenu.getChildren().add(usersSection);

        Button usersBtn = createSidebarButton("👥", "Gestion utilisateurs", "users", false);
        sidebarButtons.put("users", usersBtn);
        navMenu.getChildren().add(usersBtn);

        // Section PRODUITS
        Label productsSection = new Label("  PRODUITS");
        productsSection.setFont(Font.font("System", FontWeight.BOLD, 12));
        productsSection.setTextFill(Color.web(getTextColorMuted()));
        productsSection.setPadding(new Insets(15, 0, 5, 12));
        navMenu.getChildren().add(productsSection);

        Button productsBtn = createSidebarButton("🖼️", "Gestion galerie", "products", false);
        sidebarButtons.put("products", productsBtn);
        navMenu.getChildren().add(productsBtn);

        // Section ÉVÉNEMENTS
        Label eventsSection = new Label("  ÉVÉNEMENTS");
        eventsSection.setFont(Font.font("System", FontWeight.BOLD, 12));
        eventsSection.setTextFill(Color.web(getTextColorMuted()));
        eventsSection.setPadding(new Insets(15, 0, 5, 12));
        navMenu.getChildren().add(eventsSection);

        Button eventsBtn = createSidebarButton("📅", "Gestion événements", "events", false);
        sidebarButtons.put("events", eventsBtn);
        navMenu.getChildren().add(eventsBtn);

        // Section COLLECTIONS
        Label collectionSection = new Label("  COLLECTIONS");
        collectionSection.setFont(Font.font("System", FontWeight.BOLD, 12));
        collectionSection.setTextFill(Color.web(getTextColorMuted()));
        collectionSection.setPadding(new Insets(15, 0, 5, 12));
        navMenu.getChildren().add(collectionSection);

        Button collectionsBtn = createSidebarButton("📦", "Tableau Collection", "collectionDashboard", false);
        sidebarButtons.put("collectionDashboard", collectionsBtn);
        navMenu.getChildren().add(collectionsBtn);

        // Section CARTE
        Label mapSection = new Label("  CARTE");
        mapSection.setFont(Font.font("System", FontWeight.BOLD, 12));
        mapSection.setTextFill(Color.web(getTextColorMuted()));
        mapSection.setPadding(new Insets(15, 0, 5, 12));
        navMenu.getChildren().add(mapSection);

        Button mapBtn = createSidebarButton("🗺️", "Carte des événements", "map", false);
        sidebarButtons.put("map", mapBtn);
        navMenu.getChildren().add(mapBtn);

        Separator separator = new Separator();
        separator.setPadding(new Insets(12, 0, 12, 0));
        separator.setStyle("-fx-background-color: " + getBorderColor() + ";");
        navMenu.getChildren().add(separator);

        // Section PROFIL
        Label profileSection = new Label("  PROFIL");
        profileSection.setFont(Font.font("System", FontWeight.BOLD, 12));
        profileSection.setTextFill(Color.web(getTextColorMuted()));
        profileSection.setPadding(new Insets(0, 0, 5, 12));
        navMenu.getChildren().add(profileSection);

        Button profileBtn = createSidebarButton("👤", "Mon profil", "profile", false);
        sidebarButtons.put("profile", profileBtn);
        navMenu.getChildren().add(profileBtn);

        Button settingsBtn = createSidebarButton("⚙️", "Paramètres", "settings", false);
        sidebarButtons.put("settings", settingsBtn);
        navMenu.getChildren().add(settingsBtn);

        Button helpBtn = createSidebarButton("❓", "Aide", "help", false);
        sidebarButtons.put("help", helpBtn);
        navMenu.getChildren().add(helpBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox footer = new VBox(12);
        footer.setPadding(new Insets(12, 0, 0, 0));
        footer.setStyle("-fx-border-color: " + getBorderColor() + "; -fx-border-width: 1 0 0 0;");

        HBox userFooter = new HBox(10);
        userFooter.setAlignment(Pos.CENTER_LEFT);
        userFooter.setPadding(new Insets(12, 0, 0, 0));

        StackPane footerAvatarContainer = new StackPane();
        Circle footerAvatar = new Circle(18);
        footerAvatar.setFill(Color.web(ACCENT_COLOR));

        ImageView footerImageView = loadProfileImage(currentUser, 36);
        if (footerImageView != null) {
            footerAvatarContainer.getChildren().add(footerImageView);
        } else {
            Label footerInitials = new Label(getInitials(currentUser));
            footerInitials.setFont(Font.font("System", FontWeight.BOLD, 12));
            footerInitials.setTextFill(Color.WHITE);
            footerAvatarContainer.getChildren().addAll(footerAvatar, footerInitials);
        }

        VBox userInfo = new VBox(2);
        Label footerName = new Label(currentUser.getNomComplet());
        footerName.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        footerName.setTextFill(Color.web(getTextColor()));

        Label footerEmail = new Label(currentUser.getEmail());
        footerEmail.setFont(Font.font("System", FontWeight.NORMAL, 11));
        footerEmail.setTextFill(Color.web(getTextColorMuted()));

        userInfo.getChildren().addAll(footerName, footerEmail);
        userFooter.getChildren().addAll(footerAvatarContainer, userInfo);

        Button logoutBtn = new Button("🚪 Déconnexion");
        logoutBtn.setPrefWidth(236);
        logoutBtn.setPrefHeight(38);
        logoutBtn.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: 600; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        logoutBtn.setOnAction(e -> logout());

        footer.getChildren().addAll(userFooter, logoutBtn);

        sidebar.getChildren().addAll(navMenu, spacer, footer);
        return sidebar;
    }

    private Button createSidebarButton(String icon, String text, String view, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(236);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 12));
        btn.setFont(Font.font("System", 13));
        btn.setGraphicTextGap(10);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 16));
        iconLabel.setTextFill(Color.LIMEGREEN);
        btn.setGraphic(iconLabel);

        if (active) {
            btn.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                    "-fx-font-weight: 600; -fx-background-radius: 6;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getTextColor() +
                    "; -fx-font-weight: 500; -fx-background-radius: 6;");
        }

        btn.setOnAction(e -> switchView(view));

        btn.setOnMouseEntered(e -> {
            if (!currentView.equals(view)) {
                btn.setStyle("-fx-background-color: " + (isDarkMode ? DARK_HOVER : LIGHT_HOVER) +
                        "; -fx-text-fill: " + getTextColor() + "; -fx-font-weight: 500; -fx-background-radius: 6;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!currentView.equals(view)) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getTextColor() +
                        "; -fx-font-weight: 500; -fx-background-radius: 6;");
            }
        });

        return btn;
    }

    private void updateSidebarButtons(String activeView) {
        sidebarButtons.forEach((view, btn) -> {
            if (view.equals(activeView)) {
                btn.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-background-radius: 6;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getTextColor() +
                        "; -fx-font-weight: 500; -fx-background-radius: 6;");
            }
        });
    }

    private void switchView(String view) {
        currentView = view;
        updateSidebarButtons(view);

        switch (view) {
            case "dashboard":
                showDashboard();
                break;
            case "users":
                showUserManagementView();
                break;
            case "products":
                showProductManagementView();
                break;
            case "events":
                showEventManagementView();
                break;
            case "collectionDashboard":
                showCollectionDashboardView();
                break;
            case "map":
                showEventMapView();
                break;
            case "profile":
                showUserProfileInMain();
                break;
            case "settings":
                showSettingsView();
                break;
            case "help":
                showHelpCenter();
                break;
        }
    }

    public void showDashboard() {
        dashboardView.showDashboard(mainContentArea, isDarkMode);
    }

    public void showUserManagementView() {
        userManagementView.showUserManagementView(mainContentArea, isDarkMode);
    }

    public void showProductManagementView() {
        productManagementView.showProductManagementView(mainContentArea, isDarkMode);
    }

    public void showEventManagementView() {
        eventManagementView.showEventManagementView(mainContentArea, isDarkMode);
    }

    public void showCollectionDashboardView() {
        collectionDashboardView.showCollectionDashboardView(mainContentArea, isDarkMode);
    }

    public void showEventMapView() {
        eventMapView.showEventMapView(mainContentArea, isDarkMode);
        if (eventMapView.getWebEngine() != null) {
            setupJavaBridge(eventMapView.getWebEngine(), eventMapView);
        }
    }

    public void showUserProfileInMain() {
        userProfileView.showUserProfileView(mainContentArea, isDarkMode);
    }

    public void showSettingsView() {
        settingsView.showSettingsView(mainContentArea, isDarkMode);
    }

    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        themeToggleBtn.setText(isDarkMode ? "☀️" : "🌙");
        themeToggleBtn.setStyle("-fx-background-color: " + (isDarkMode ? DARK_CARD : "#F9FAFB") +
                "; -fx-text-fill: " + getTextColor() + "; -fx-font-size: 18px; -fx-padding: 8 14; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: " + getBorderColor() + "; -fx-border-radius: 8;");
        updateHeaderProfileImage();
        refreshAllViews();
    }

    private void applyTheme() {
        root.setStyle("-fx-background-color: " + getBgColor() + ";");
        sidebar.setStyle(getSidebarStyle());
        VBox header = (VBox) root.getTop();
        header.setStyle(getHeaderStyle());

        if (headerProfileName != null) headerProfileName.setTextFill(Color.web(getTextColor()));
        if (headerProfileRole != null) headerProfileRole.setTextFill(Color.web(getTextColorMuted()));
        updateSidebarTextColors();
    }

    private void updateSidebarTextColors() {
        sidebarButtons.forEach((view, btn) -> {
            if (!currentView.equals(view)) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getTextColor() +
                        "; -fx-font-weight: 500; -fx-background-radius: 6;");
            }
        });

        if (sidebar != null) {
            VBox navMenu = (VBox) sidebar.getChildren().get(0);
            for (int i = 0; i < navMenu.getChildren().size(); i++) {
                Node node = navMenu.getChildren().get(i);
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getText().matches("\\s*[A-Z]+")) {
                        label.setTextFill(Color.web(getTextColorMuted()));
                    }
                }
            }
        }
    }

    private void refreshAllViews() {
        switch (currentView) {
            case "dashboard": showDashboard(); break;
            case "users": showUserManagementView(); break;
            case "products": showProductManagementView(); break;
            case "events": showEventManagementView(); break;
            case "collectionDashboard": showCollectionDashboardView(); break;
            case "map": showEventMapView(); break;
            case "profile": showUserProfileInMain(); break;
            case "settings": showSettingsView(); break;
        }
    }

    public String getBgColor() { return isDarkMode ? DARK_BG : LIGHT_BG; }
    public String getTextColor() { return isDarkMode ? DARK_TEXT : LIGHT_TEXT; }
    public String getTextColorMuted() { return isDarkMode ? DARK_TEXT_MUTED : LIGHT_TEXT_MUTED; }
    public String getBorderColor() { return isDarkMode ? DARK_BORDER : LIGHT_BORDER; }
    public String getCardBg() { return isDarkMode ? DARK_CARD : LIGHT_CARD; }
    public String getAccentColor() { return ACCENT_COLOR; }
    public String getSuccessColor() { return SUCCESS_COLOR; }
    public String getWarningColor() { return WARNING_COLOR; }
    public String getDangerColor() { return DANGER_COLOR; }

    private String getSidebarStyle() {
        return "-fx-background-color: " + (isDarkMode ? DARK_SIDEBAR : LIGHT_SIDEBAR) +
                "; -fx-border-color: " + getBorderColor() + "; -fx-border-width: 0 1 0 0;";
    }

    private String getHeaderStyle() {
        return "-fx-background-color: " + (isDarkMode ? DARK_HEADER : LIGHT_HEADER) +
                "; -fx-border-color: " + getBorderColor() + "; -fx-border-width: 0 0 1 0;";
    }

    public String getRoleInFrench(String role) {
        if (role == null) return "";
        switch (role.toLowerCase()) {
            case "admin": return "Administrateur";
            case "organisateur": return "Organisateur";
            case "participant": return "Participant";
            default: return role;
        }
    }

    public void updateHeaderProfileImage() {
        currentUser = userService.getUserById(currentUser.getId());

        headerProfileContainer.getChildren().clear();

        ImageView profileImageView = loadProfileImage(currentUser, 36);
        if (profileImageView != null) {
            headerProfileContainer.getChildren().add(profileImageView);
        } else {
            Circle profileCircle = new Circle(18);
            profileCircle.setFill(Color.web(ACCENT_COLOR));
            String initials = getInitials(currentUser);
            Label profileText = new Label(initials);
            profileText.setFont(Font.font("System", FontWeight.BOLD, 12));
            profileText.setTextFill(Color.WHITE);
            headerProfileContainer.getChildren().addAll(profileCircle, profileText);
        }

        if (headerProfileName != null) headerProfileName.setText(currentUser.getNomComplet());
        if (headerProfileRole != null) headerProfileRole.setText(getRoleInFrench(currentUser.getRole()));

        updateSidebarFooter();
    }

    private void updateSidebarFooter() {
        VBox footer = (VBox) sidebar.getChildren().get(2);
        HBox userFooter = (HBox) footer.getChildren().get(0);
        StackPane avatarContainer = (StackPane) userFooter.getChildren().get(0);
        VBox userInfo = (VBox) userFooter.getChildren().get(1);

        avatarContainer.getChildren().clear();
        ImageView footerImageView = loadProfileImage(currentUser, 36);
        if (footerImageView != null) {
            avatarContainer.getChildren().add(footerImageView);
        } else {
            Circle footerAvatar = new Circle(18);
            footerAvatar.setFill(Color.web(ACCENT_COLOR));
            Label footerInitials = new Label(getInitials(currentUser));
            footerInitials.setFont(Font.font("System", FontWeight.BOLD, 12));
            footerInitials.setTextFill(Color.WHITE);
            avatarContainer.getChildren().addAll(footerAvatar, footerInitials);
        }

        ((Label)userInfo.getChildren().get(0)).setText(currentUser.getNomComplet());
        ((Label)userInfo.getChildren().get(1)).setText(currentUser.getEmail());
    }

    public String getInitials(User user) {
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            initials += String.valueOf(user.getPrenom().charAt(0)).toUpperCase();
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            initials += String.valueOf(user.getNom().charAt(0)).toUpperCase();
        }
        return initials.isEmpty() ? "U" : initials;
    }

    public ImageView loadProfileImage(User user, double size) {
        if (user == null) return null;

        String photoPath = user.getPhoto();
        if (photoPath == null || photoPath.isEmpty() || photoPath.equals("default.jpg")) {
            return null;
        }

        try {
            File imageFile = null;

            if (photoPath.startsWith("profiles/")) {
                imageFile = new File("src/main/resources/" + photoPath);
                if (!imageFile.exists()) {
                    imageFile = new File(photoPath);
                }
            } else {
                imageFile = new File("src/main/resources/profiles/" + photoPath);
                if (!imageFile.exists()) {
                    imageFile = new File("profiles/" + photoPath);
                }
            }

            if (imageFile.exists() && imageFile.isFile()) {
                Image avatarImage = new Image(new FileInputStream(imageFile), size, size, true, true);
                ImageView avatarImageView = new ImageView(avatarImage);
                avatarImageView.setFitWidth(size);
                avatarImageView.setFitHeight(size);
                avatarImageView.setPreserveRatio(true);

                Circle clip = new Circle(size/2, size/2, size/2);
                avatarImageView.setClip(clip);

                return avatarImageView;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur chargement image: " + e.getMessage());
        }
        return null;
    }

    private void toggleSidebar() {
        ScrollPane sidebarScrollPane = (ScrollPane) root.getLeft();

        if (!sidebarCollapsed) {
            sidebarScrollPane.setPrefWidth(70);
            sidebarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            sidebarButtons.forEach((view, btn) -> {
                btn.setText("");
                btn.setPrefWidth(46);
                btn.setPadding(new Insets(0, 0, 0, 15));
            });

            VBox navMenu = (VBox) sidebar.getChildren().get(0);
            for (int i = 0; i < navMenu.getChildren().size(); i++) {
                Node node = navMenu.getChildren().get(i);
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getText().matches("\\s*[A-Z]+")) {
                        label.setVisible(false);
                        label.setManaged(false);
                    }
                }
            }

            VBox footer = (VBox) sidebar.getChildren().get(2);
            HBox userFooter = (HBox) footer.getChildren().get(0);
            VBox userInfo = (VBox) userFooter.getChildren().get(1);
            userInfo.setVisible(false);
            userInfo.setManaged(false);

            Button logoutBtn = (Button) footer.getChildren().get(1);
            logoutBtn.setText("");
            logoutBtn.setPrefWidth(46);
            logoutBtn.setPadding(new Insets(8, 0, 8, 15));

        } else {
            sidebarScrollPane.setPrefWidth(260);
            sidebarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            sidebarButtons.forEach((view, btn) -> {
                btn.setText(getButtonText(view));
                btn.setPrefWidth(236);
                btn.setPadding(new Insets(0, 0, 0, 12));
            });

            VBox navMenu = (VBox) sidebar.getChildren().get(0);
            for (int i = 0; i < navMenu.getChildren().size(); i++) {
                Node node = navMenu.getChildren().get(i);
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getText().matches("\\s*[A-Z]+")) {
                        label.setVisible(true);
                        label.setManaged(true);
                    }
                }
            }

            VBox footer = (VBox) sidebar.getChildren().get(2);
            HBox userFooter = (HBox) footer.getChildren().get(0);
            VBox userInfo = (VBox) userFooter.getChildren().get(1);
            userInfo.setVisible(true);
            userInfo.setManaged(true);

            Button logoutBtn = (Button) footer.getChildren().get(1);
            logoutBtn.setText("🚪 Déconnexion");
            logoutBtn.setPrefWidth(236);
            logoutBtn.setPadding(new Insets(8, 20, 8, 20));
        }

        sidebarCollapsed = !sidebarCollapsed;
    }

    private String getButtonText(String view) {
        switch (view) {
            case "dashboard": return "Tableau de bord";
            case "users": return "Gestion utilisateurs";
            case "products": return "Gestion galerie";
            case "events": return "Gestion événements";
            case "collectionDashboard": return "Tableau Collection";
            case "map": return "Carte des événements";
            case "profile": return "Mon profil";
            case "settings": return "Paramètres";
            case "help": return "Aide";
            default: return "";
        }
    }

    private void performGlobalSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        switchView("users");
        userManagementView.searchUsers(keyword);
    }

    private void showHelpCenter() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Centre d'aide");
        alert.setHeaderText(null);
        alert.setContentText("Pour assistance, contactez: support@loopi.tn\n\nDocumentation: https://docs.loopi.tn");
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public Stage getPrimaryStage() { return primaryStage; }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public UserService getUserService() { return userService; }
    public StackPane getMainContentArea() { return mainContentArea; }
    public boolean isDarkMode() { return isDarkMode; }

    private void logout() {
        if (notificationRefreshTimer != null) {
            notificationRefreshTimer.stop();
        }
        SessionManager.logout();
        primaryStage.close();

        try {
            LoginView loginView = new LoginView();
            Stage loginStage = new Stage();
            loginView.start(loginStage);
        } catch (Exception e) {
            System.out.println("Erreur retour login: " + e.getMessage());
        }
    }

    public void setupJavaBridge(WebEngine webEngine, EventMapView eventMapView) {
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                EventMapView.JavaConnector connector = eventMapView.new JavaConnector();
                window.setMember("javaApp", connector);
                System.out.println("✅ Pont Java-JavaScript établi avec succès");
            }
        });
    }
}