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
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboard {
    private User currentUser;
    private UserService userService;
    private Stage primaryStage;
    private BorderPane root;
    private StackPane mainContentArea;
    private String currentView = "dashboard";

    private DashboardView dashboardView;
    private UserManagementView userManagementView;
    private UserProfileView userProfileView;
    private AnalyticsView analyticsView;
    private SettingsView settingsView;

    private StackPane headerProfileContainer;
    private VBox headerProfileInfo;
    private Label headerProfileName;
    private Label headerProfileRole;

    private VBox sidebar;
    private Map<String, Button> sidebarButtons = new HashMap<>();
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
        SessionManager.login(user);

        this.dashboardView = new DashboardView(currentUser, userService, this);
        this.userManagementView = new UserManagementView(currentUser, userService, this);
        this.userProfileView = new UserProfileView(currentUser, userService, this);
        this.analyticsView = new AnalyticsView(currentUser, userService, this);
        this.settingsView = new SettingsView(currentUser, userService, this);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("LOOPI - Administration");

        try {
            File logoFile = new File("logo.png");
            if (logoFile.exists()) {
                Image icon = new Image(new FileInputStream(logoFile));
                primaryStage.getIcons().add(icon);
            }
        } catch (Exception e) {
            System.out.println("Logo non trouvé");
        }

        root = new BorderPane();

        sidebar = createSidebar();
        VBox header = createHeader();

        root.setLeft(sidebar);
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
            File logoFile = new File("logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(new FileInputStream(logoFile), 32, 32, true, true);
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitWidth(32);
                logoView.setFitHeight(32);
                logoBox.getChildren().add(logoView);
            } else {
                Label logoIcon = new Label("♻️");
                logoIcon.setFont(Font.font("System", 28));
                logoBox.getChildren().add(logoIcon);
            }
        } catch (Exception e) {
            Label logoIcon = new Label("♻️");
            logoIcon.setFont(Font.font("System", 28));
            logoBox.getChildren().add(logoIcon);
        }

        Label logoText = new Label("LOOPI");
        logoText.setFont(Font.font("System", FontWeight.BOLD, 18));
        logoText.setTextFill(Color.web(getTextColor()));
        logoBox.getChildren().add(logoText);

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
            if (e.getCode() == KeyCode.ENTER) {
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

        Button notificationsBtn = new Button("🔔");
        notificationsBtn.setStyle("-fx-background-color: " + (isDarkMode ? DARK_CARD : "#F9FAFB") +
                "; -fx-text-fill: " + getTextColor() + "; -fx-font-size: 18px; -fx-padding: 8 14; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: " + getBorderColor() + "; -fx-border-radius: 8;");

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

        headerContent.getChildren().addAll(menuToggle, logoBox, searchBox, spacer, themeToggleBtn, notificationsBtn, profileBox);
        header.getChildren().add(headerContent);

        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(260);
        sidebar.setStyle(getSidebarStyle());
        sidebar.setPadding(new Insets(20, 12, 20, 12));

        VBox navMenu = new VBox(4);
        navMenu.setPadding(new Insets(0, 0, 20, 0));

        Button dashboardBtn = createSidebarButton("📊", "Tableau de bord", "dashboard", true);
        sidebarButtons.put("dashboard", dashboardBtn);

        Button usersBtn = createSidebarButton("👥", "Utilisateurs", "users", false);
        sidebarButtons.put("users", usersBtn);

        Button analyticsBtn = createSidebarButton("📈", "Statistiques", "analytics", false);
        sidebarButtons.put("analytics", analyticsBtn);

        Separator separator = new Separator();
        separator.setPadding(new Insets(12, 0, 12, 0));
        separator.setStyle("-fx-background-color: " + getBorderColor() + ";");

        Button profileBtn = createSidebarButton("👤", "Mon profil", "profile", false);
        sidebarButtons.put("profile", profileBtn);

        Button settingsBtn = createSidebarButton("⚙️", "Paramètres", "settings", false);
        sidebarButtons.put("settings", settingsBtn);

        Button helpBtn = createSidebarButton("❓", "Aide", "help", false);
        sidebarButtons.put("help", helpBtn);

        navMenu.getChildren().addAll(dashboardBtn, usersBtn, analyticsBtn, separator, profileBtn, settingsBtn, helpBtn);

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
    }

    private void refreshAllViews() {
        switch (currentView) {
            case "dashboard": showDashboard(); break;
            case "users": showUserManagementViewInCenter(); break;
            case "profile": showUserProfileInMain(); break;
            case "analytics": showAnalyticsView(); break;
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

    private void switchView(String view) {
        currentView = view;
        updateSidebarButtons(view);

        switch (view) {
            case "dashboard": showDashboard(); break;
            case "users": showUserManagementViewInCenter(); break;
            case "analytics": showAnalyticsView(); break;
            case "settings": showSettingsView(); break;
            case "profile": showUserProfileInMain(); break;
            case "help": showHelpCenter(); break;
        }
    }

    public void showDashboard() {
        dashboardView.showDashboard(mainContentArea, isDarkMode);
    }

    public void showUserManagementViewInCenter() {
        userManagementView.showUserManagementView(mainContentArea, isDarkMode);
    }

    public void showUserProfileInMain() {
        userProfileView.showUserProfileView(mainContentArea, isDarkMode);
    }

    public void showAnalyticsView() {
        analyticsView.showAnalyticsView(mainContentArea, isDarkMode);
    }

    public void showSettingsView() {
        settingsView.showSettingsView(mainContentArea, isDarkMode);
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
        if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.jpg")) {
            try {
                String photoPath = user.getPhoto();
                File imageFile = new File(photoPath);

                if (!imageFile.exists() && photoPath.contains("profiles")) {
                    imageFile = new File("profiles/" + photoPath.replace("profiles/", "").replace("profiles\\", ""));
                }

                if (imageFile.exists()) {
                    Image avatarImage = new Image(new FileInputStream(imageFile), size, size, true, true);
                    ImageView avatarImageView = new ImageView(avatarImage);
                    avatarImageView.setFitWidth(size);
                    avatarImageView.setFitHeight(size);
                    avatarImageView.setPreserveRatio(true);
                    avatarImageView.setStyle("-fx-background-radius: 50%;");
                    return avatarImageView;
                }
            } catch (Exception e) {
                System.out.println("Erreur chargement image: " + e.getMessage());
            }
        }
        return null;
    }

    private void toggleSidebar() {
        if (sidebar.getPrefWidth() > 100) {
            sidebar.setPrefWidth(70);
        } else {
            sidebar.setPrefWidth(260);
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
}