package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import edu.Loopi.tools.SessionManager;
import edu.Loopi.view.LoginView;
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
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class AdminDashboard {
    private User currentUser;
    private UserService userService;
    private Stage primaryStage;
    private BorderPane root;
    private StackPane mainContentArea;
    private boolean sidebarCollapsed = false;
    private String currentView = "dashboard";

    // Références aux vues
    private DashboardView dashboardView;
    private UserManagementView userManagementView;
    private UserProfileView userProfileView;
    private AnalyticsView analyticsView;
    private SettingsView settingsView;

    // Composants du header qui seront mis à jour
    private StackPane headerProfileContainer;
    private VBox headerProfileInfo;
    private StackPane footerAvatarContainer;

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        SessionManager.login(user);

        // Initialiser les vues
        this.dashboardView = new DashboardView(currentUser, userService, this);
        this.userManagementView = new UserManagementView(currentUser, userService, this);
        this.userProfileView = new UserProfileView(currentUser, userService, this);
        this.analyticsView = new AnalyticsView(currentUser, userService, this);
        this.settingsView = new SettingsView(currentUser, userService, this);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("LOOPI - Tableau de Bord Administrateur");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #E6F8F6;");

        // Créer le header modernisé
        VBox header = createModernHeader();
        root.setTop(header);

        // Créer le sidebar avec le style modernisé
        VBox sidebar = createModernSidebar();
        root.setLeft(sidebar);

        // Zone de contenu principale avec padding
        mainContentArea = new StackPane();
        mainContentArea.setPadding(new Insets(0));
        mainContentArea.setStyle("-fx-background-color: transparent;");

        // Charger le dashboard par défaut
        showDashboard();
        root.setCenter(mainContentArea);

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        SessionManager.printSessionInfo();
    }

    // ============ HEADER MODERNISÉ ============
    private VBox createModernHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: linear-gradient(to right, #03414D, #03414D);");

        // Barre supérieure
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: transparent;");

        // Bouton menu toggle
        Button menuToggle = new Button("☰");
        menuToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 20px; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
        menuToggle.setOnAction(e -> toggleSidebar());

        // Logo et titre
        HBox logoBox = new HBox(15);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Circle logoCircle = new Circle(20);
        logoCircle.setFill(Color.web("#FFFFFF"));

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("LOOPI");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        mainTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("Admin Dashboard");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setTextFill(Color.web("#E6F8F6"));

        titleBox.getChildren().addAll(mainTitle, subtitle);
        logoBox.getChildren().addAll(logoCircle, titleBox);

        // Espaceur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Barre de recherche
        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(5, 15, 5, 15));

        TextField searchField = new TextField();
        searchField.setPromptText("Search users, stats...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-text-fill: white;");
        searchField.setPrefWidth(250);
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performGlobalSearch(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> performGlobalSearch(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Notifications et profil
        HBox rightControls = new HBox(15);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        // Bouton notifications
        Button notificationsBtn = new Button("🔔");
        notificationsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-cursor: hand;");
        notificationsBtn.setTooltip(new Tooltip("Notifications"));

        // Profil utilisateur - STOCKER LA RÉFÉRENCE
        headerProfileContainer = new StackPane();
        headerProfileContainer.setOnMouseClicked(e -> showUserProfileInMain());

        Circle profileCircle = new Circle(22);
        profileCircle.setFill(Color.web("#FFFFFF"));

        // Charger l'image de profil
        ImageView profileImageView = loadProfileImage(currentUser, 44);
        if (profileImageView != null) {
            headerProfileContainer.getChildren().add(profileImageView);
        } else {
            String initials = getInitials(currentUser);
            Label profileText = new Label(initials);
            profileText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            profileText.setTextFill(Color.web("#03414D"));
            headerProfileContainer.getChildren().addAll(profileCircle, profileText);
        }

        headerProfileInfo = new VBox(2);
        headerProfileInfo.setAlignment(Pos.CENTER_LEFT);

        Label profileName = new Label(currentUser.getNomComplet());
        profileName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        profileName.setTextFill(Color.WHITE);

        Label profileRole = new Label(currentUser.getRole().toUpperCase());
        profileRole.setFont(Font.font("Arial", 10));
        profileRole.setTextFill(Color.web("#E6F8F6"));

        headerProfileInfo.getChildren().addAll(profileName, profileRole);
        headerProfileContainer.setStyle("-fx-cursor: hand;");

        HBox profileBox = new HBox(10);
        profileBox.setAlignment(Pos.CENTER_RIGHT);
        profileBox.getChildren().addAll(headerProfileInfo, headerProfileContainer);

        rightControls.getChildren().addAll(searchBox, notificationsBtn, profileBox);
        topBar.getChildren().addAll(menuToggle, logoBox, spacer, rightControls);

        // Barre des onglets
        HBox tabBar = createTabBar();

        header.getChildren().addAll(topBar, tabBar);
        return header;
    }

    // Méthode pour mettre à jour l'image de profil dans le header
    public void updateHeaderProfileImage() {
        // Rafraîchir les données utilisateur
        currentUser = userService.getUserById(currentUser.getId());

        // Mettre à jour le conteneur de profil dans le header
        headerProfileContainer.getChildren().clear();

        ImageView profileImageView = loadProfileImage(currentUser, 44);
        if (profileImageView != null) {
            headerProfileContainer.getChildren().add(profileImageView);
        } else {
            Circle profileCircle = new Circle(22);
            profileCircle.setFill(Color.web("#FFFFFF"));
            String initials = getInitials(currentUser);
            Label profileText = new Label(initials);
            profileText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            profileText.setTextFill(Color.web("#03414D"));
            headerProfileContainer.getChildren().addAll(profileCircle, profileText);
        }

        // Mettre à jour les informations du profil
        if (headerProfileInfo != null && headerProfileInfo.getChildren().size() >= 2) {
            ((Label)headerProfileInfo.getChildren().get(0)).setText(currentUser.getNomComplet());
            ((Label)headerProfileInfo.getChildren().get(1)).setText(currentUser.getRole().toUpperCase());
        }

        // Mettre à jour le footer du sidebar
        updateSidebarFooter();
    }

    // Méthode pour mettre à jour le footer du sidebar
    private void updateSidebarFooter() {
        VBox sidebar = (VBox) root.getLeft();
        VBox footer = (VBox) sidebar.getChildren().get(3);

        // Mettre à jour l'avatar du footer
        if (footerAvatarContainer != null) {
            footerAvatarContainer.getChildren().clear();
            Circle footerAvatar = new Circle(20);
            footerAvatar.setFill(Color.web("#03414D"));

            ImageView footerImageView = loadProfileImage(currentUser, 40);
            if (footerImageView != null) {
                footerAvatarContainer.getChildren().add(footerImageView);
            } else {
                String initials = getInitials(currentUser);
                Label footerInitials = new Label(initials);
                footerInitials.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                footerInitials.setTextFill(Color.WHITE);
                footerAvatarContainer.getChildren().addAll(footerAvatar, footerInitials);
            }
        }

        // Mettre à jour les informations utilisateur dans le footer
        if (footer.getChildren().size() > 0) {
            HBox userFooter = (HBox) footer.getChildren().get(0);
            if (userFooter.getChildren().size() > 1) {
                VBox userInfo = (VBox) userFooter.getChildren().get(1);
                if (userInfo.getChildren().size() >= 2) {
                    ((Label)userInfo.getChildren().get(0)).setText(currentUser.getNomComplet());
                    ((Label)userInfo.getChildren().get(1)).setText(currentUser.getEmail());
                }
            }
        }
    }

    private HBox createTabBar() {
        HBox tabBar = new HBox();
        tabBar.setPadding(new Insets(0, 30, 0, 30));
        tabBar.setStyle("-fx-background-color: #72DFD0;");
        tabBar.setPrefHeight(40);

        HBox tabsContainer = new HBox(0);
        tabsContainer.setAlignment(Pos.CENTER_LEFT);

        Button dashboardTab = createTabButton("Dashboard", "dashboard", true);
        Button usersTab = createTabButton("Users Management", "users", false);
        Button analyticsTab = createTabButton("Analytics", "analytics", false);
        Button settingsTab = createTabButton("Settings", "settings", false);

        tabsContainer.getChildren().addAll(dashboardTab, usersTab, analyticsTab, settingsTab);
        tabBar.getChildren().add(tabsContainer);

        return tabBar;
    }

    private Button createTabButton(String text, String viewName, boolean active) {
        Button tab = new Button(text);
        tab.setPadding(new Insets(10, 20, 10, 20));
        tab.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        if (active) {
            tab.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                    "-fx-border-color: transparent; -fx-cursor: hand;");
        } else {
            tab.setStyle("-fx-background-color: transparent; -fx-text-fill: #000000; " +
                    "-fx-border-color: transparent; -fx-cursor: hand;");
        }

        tab.setOnAction(e -> switchView(viewName));

        tab.setOnMouseEntered(e -> {
            if (!currentView.equals(viewName)) {
                tab.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #03414D; " +
                        "-fx-border-color: transparent; -fx-cursor: hand;");
            }
        });

        tab.setOnMouseExited(e -> {
            if (!currentView.equals(viewName)) {
                tab.setStyle("-fx-background-color: transparent; -fx-text-fill: #000000; " +
                        "-fx-border-color: transparent; -fx-cursor: hand;");
            }
        });

        return tab;
    }

    // ============ SIDEBAR MODERNISÉ ============
    private VBox createModernSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #72DFD0; -fx-border-color: #03414D; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(25, 0, 20, 0));

        // Logo sidebar
        HBox sidebarLogo = new HBox(15);
        sidebarLogo.setPadding(new Insets(0, 0, 25, 25));
        sidebarLogo.setAlignment(Pos.CENTER_LEFT);

        Label sidebarIcon = new Label("📊");
        sidebarIcon.setFont(Font.font("Arial", 24));

        VBox logoText = new VBox(2);
        Label logoTitle = new Label("LOOPI Admin");
        logoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        logoTitle.setTextFill(Color.web("#03414D"));

        Label logoSubtitle = new Label("Analytics");
        logoSubtitle.setFont(Font.font("Arial", 11));
        logoSubtitle.setTextFill(Color.web("#03414D"));

        logoText.getChildren().addAll(logoTitle, logoSubtitle);
        sidebarLogo.getChildren().addAll(sidebarIcon, logoText);

        // Navigation
        VBox navSection = new VBox(5);
        navSection.setPadding(new Insets(10, 15, 20, 15));

        // Dashboard Section
        Label dashboardLabel = new Label("DASHBOARD");
        dashboardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        dashboardLabel.setTextFill(Color.web("#03414D"));
        dashboardLabel.setPadding(new Insets(15, 0, 10, 10));

        Button dashboardBtn = createSidebarButton("📊", "Dashboard", "dashboard", true);
        dashboardBtn.setOnAction(e -> switchView("dashboard"));

        Button usersBtn = createSidebarButton("👥", "Users", "users", false);
        usersBtn.setOnAction(e -> switchView("users"));

        Button analyticsBtn = createSidebarButton("📈", "Analytics", "analytics", false);
        analyticsBtn.setOnAction(e -> switchView("analytics"));

        // Applications Section
        Label appsLabel = new Label("APPLICATIONS");
        appsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        appsLabel.setTextFill(Color.web("#03414D"));
        appsLabel.setPadding(new Insets(20, 0, 10, 10));

        Button profileBtn = createSidebarButton("👤", "My Profile", "profile", false);
        profileBtn.setOnAction(e -> showUserProfileInMain());

        Button settingsBtn = createSidebarButton("⚙️", "Settings", "settings", false);
        settingsBtn.setOnAction(e -> showSettingsView());

        // Support Section
        Label supportLabel = new Label("SUPPORT");
        supportLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        supportLabel.setTextFill(Color.web("#03414D"));
        supportLabel.setPadding(new Insets(20, 0, 10, 10));

        Button helpBtn = createSidebarButton("❓", "Help Center", "help", false);
        helpBtn.setOnAction(e -> showHelpCenter());

        Button supportBtn = createSidebarButton("🆘", "Contact Support", "contact", false);
        supportBtn.setOnAction(e -> showContactSupport());

        // Espaceur
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer sidebar
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(20, 20, 20, 20));
        footer.setStyle("-fx-background-color: #A0F6D2; -fx-border-color: #03414D; -fx-border-width: 1 0 0 0;");

        // User info in footer - STOCKER LA RÉFÉRENCE
        HBox userFooter = new HBox(10);
        userFooter.setAlignment(Pos.CENTER_LEFT);

        footerAvatarContainer = new StackPane();
        Circle footerAvatar = new Circle(20);
        footerAvatar.setFill(Color.web("#03414D"));

        ImageView footerImageView = loadProfileImage(currentUser, 40);
        if (footerImageView != null) {
            footerAvatarContainer.getChildren().add(footerImageView);
        } else {
            Label footerInitials = new Label(getInitials(currentUser));
            footerInitials.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            footerInitials.setTextFill(Color.WHITE);
            footerAvatarContainer.getChildren().addAll(footerAvatar, footerInitials);
        }

        VBox userInfo = new VBox(2);
        Label footerName = new Label(currentUser.getNomComplet());
        footerName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        footerName.setTextFill(Color.web("#03414D"));

        Label footerEmail = new Label(currentUser.getEmail());
        footerEmail.setFont(Font.font("Arial", 10));
        footerEmail.setTextFill(Color.web("#03414D"));

        userInfo.getChildren().addAll(footerName, footerEmail);
        userFooter.getChildren().addAll(footerAvatarContainer, userInfo);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setPrefWidth(220);
        logoutBtn.setPrefHeight(40);
        logoutBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout());

        footer.getChildren().addAll(userFooter, logoutBtn);

        navSection.getChildren().addAll(
                dashboardLabel, dashboardBtn, usersBtn, analyticsBtn,
                appsLabel, profileBtn, settingsBtn,
                supportLabel, helpBtn, supportBtn
        );

        sidebar.getChildren().addAll(sidebarLogo, navSection, spacer, footer);
        return sidebar;
    }

    private Button createSidebarButton(String icon, String text, String view, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(230);
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 15));
        btn.setFont(Font.font("Arial", 14));
        btn.setGraphicTextGap(15);

        // Ajouter l'icône
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));
        btn.setGraphic(iconLabel);

        if (active) {
            btn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                    "-fx-font-weight: bold; -fx-border-color: transparent; " +
                    "-fx-border-radius: 8;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #03414D; " +
                    "-fx-border-color: transparent; -fx-border-radius: 8;");
        }

        btn.setOnMouseEntered(e -> {
            if (!currentView.equals(view)) {
                btn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent; " +
                        "-fx-border-radius: 8;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!currentView.equals(view)) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #03414D; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent; " +
                        "-fx-border-radius: 8;");
            }
        });

        return btn;
    }

    // ============ MÉTHODES DE NAVIGATION ============
    private void switchView(String view) {
        currentView = view;
        updateSidebarButtons(view);

        switch (view) {
            case "dashboard":
                showDashboard();
                break;
            case "users":
                showUserManagementViewInCenter();
                break;
            case "analytics":
                showAnalyticsView();
                break;
            case "settings":
                showSettingsView();
                break;
            case "profile":
                showUserProfileInMain();
                break;
        }
    }

    private void updateSidebarButtons(String activeView) {
        VBox sidebar = (VBox) root.getLeft();
        VBox navSection = (VBox) sidebar.getChildren().get(1);

        // Réinitialiser tous les boutons
        for (var node : navSection.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String btnView = getButtonView(btn.getText());

                if (btnView != null) {
                    if (btnView.equals(activeView)) {
                        btn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                                "-fx-font-weight: bold; -fx-border-color: transparent; " +
                                "-fx-border-radius: 8;");
                    } else {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #03414D; " +
                                "-fx-border-color: transparent; -fx-border-radius: 8;");
                    }
                }
            }
        }
    }

    private String getButtonView(String buttonText) {
        switch (buttonText) {
            case "Dashboard": return "dashboard";
            case "Users": return "users";
            case "Analytics": return "analytics";
            case "My Profile": return "profile";
            case "Settings": return "settings";
            default: return null;
        }
    }

    // ============ MÉTHODES D'AFFICHAGE ============
    public void showDashboard() {
        dashboardView.showDashboard(mainContentArea);
        currentView = "dashboard";
        updateSidebarButtons("dashboard");
    }

    public void showUserManagementViewInCenter() {
        userManagementView.showUserManagementView(mainContentArea);
        currentView = "users";
        updateSidebarButtons("users");
    }

    public void showUserProfileInMain() {
        userProfileView.showUserProfileView(mainContentArea);
        currentView = "profile";
        updateSidebarButtons("profile");
    }

    public void showAnalyticsView() {
        analyticsView.showAnalyticsView(mainContentArea);
        currentView = "analytics";
        updateSidebarButtons("analytics");
    }

    public void showSettingsView() {
        settingsView.showSettingsView(mainContentArea);
        currentView = "settings";
        updateSidebarButtons("settings");
    }

    // ============ MÉTHODES UTILITAIRES ============
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

    // Méthode pour charger les images de profil
    private ImageView loadProfileImage(User user, double size) {
        if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.jpg")) {
            try {
                String photoPath = user.getPhoto();
                File imageFile;

                // Vérifier différents formats de chemin
                if (photoPath.startsWith("profiles/")) {
                    imageFile = new File(photoPath);
                } else if (photoPath.startsWith("profiles\\")) {
                    imageFile = new File(photoPath);
                } else if (photoPath.contains("profile_")) {
                    // Essayer avec le dossier profiles
                    imageFile = new File("profiles/" + photoPath);
                    if (!imageFile.exists()) {
                        imageFile = new File(photoPath);
                    }
                } else {
                    imageFile = new File(photoPath);
                }

                if (imageFile.exists()) {
                    Image avatarImage = new Image("file:" + imageFile.getAbsolutePath(), size, size, true, true, true);
                    ImageView avatarImageView = new ImageView(avatarImage);
                    avatarImageView.setFitWidth(size);
                    avatarImageView.setFitHeight(size);
                    avatarImageView.setPreserveRatio(true);
                    avatarImageView.setStyle("-fx-background-radius: 50%;");
                    return avatarImageView;
                }
            } catch (Exception e) {
                System.out.println("Error loading profile image for user " + user.getId() + ": " + e.getMessage());
            }
        }

        return null;
    }

    private void toggleSidebar() {
        VBox sidebar = (VBox) root.getLeft();
        if (sidebarCollapsed) {
            sidebar.setPrefWidth(260);
            sidebarCollapsed = false;
        } else {
            sidebar.setPrefWidth(80);
            sidebarCollapsed = true;
        }
    }

    private void performGlobalSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        switchView("users");
        userManagementView.searchUsers(keyword);
    }

    private void showHelpCenter() {
        showAlert("Help Center", "For assistance, please contact: support@loopi.tn\n\n" +
                "Documentation: https://docs.loopi.tn\n" +
                "FAQ: https://loopi.tn/faq");
    }

    private void showContactSupport() {
        showAlert("Contact Support",
                "Email: support@loopi.tn\n" +
                        "Phone: +216 XX XXX XXX\n" +
                        "Office Hours: Mon-Fri 9:00-17:00\n" +
                        "Address: Tunis, Tunisia");
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    // Méthodes d'accès pour les vues
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public UserService getUserService() {
        return userService;
    }

    public StackPane getMainContentArea() {
        return mainContentArea;
    }

    private void logout() {
        SessionManager.logout();
        primaryStage.close();

        // Retour à la page de login
        try {
            LoginView loginView = new LoginView();
            Stage loginStage = new Stage();
            loginView.start(loginStage);
        } catch (Exception e) {
            System.out.println("Error returning to login: " + e.getMessage());
        }
    }
}