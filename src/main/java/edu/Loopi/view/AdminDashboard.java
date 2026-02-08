package edu.Loopi.view;

import edu.Loopi.entities.*;
import edu.Loopi.services.*;
import edu.Loopi.tools.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboard {
    private User currentUser;
    private UserService userService;
    private Stage primaryStage;
    private BorderPane root;
    private TableView<User> userTable;
    private ObservableList<User> userList;
    private StackPane mainContentArea;

    private boolean sidebarCollapsed = false;
    private String currentView = "dashboard";
    private Map<String, List<User>> cachedUserData = new HashMap<>();

    private ComboBox<String> currentRoleFilter;
    private ComboBox<String> timeFilterCombo;
    private PieChart rolePieChart;
    private LineChart<String, Number> registrationLineChart;
    private VBox legendBox;

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        SessionManager.login(user);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("LOOPI - Tableau de Bord Administrateur");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

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
        header.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #6366f1);");

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
        logoCircle.setFill(Color.web("#ffffff"));

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("LOOPI");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        mainTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("Admin Dashboard");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setTextFill(Color.web("#e2e8f0"));

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

        // Profil utilisateur
        StackPane profileContainer = new StackPane();
        profileContainer.setOnMouseClicked(e -> showUserProfileInMain());

        Circle profileCircle = new Circle(22);
        profileCircle.setFill(Color.web("#ffffff"));

        String initials = getInitials(currentUser);
        Label profileText = new Label(initials);
        profileText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        profileText.setTextFill(Color.web("#4f46e5"));

        VBox profileInfo = new VBox(2);
        profileInfo.setAlignment(Pos.CENTER_LEFT);

        Label profileName = new Label(currentUser.getNomComplet());
        profileName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        profileName.setTextFill(Color.WHITE);

        Label profileRole = new Label(currentUser.getRole().toUpperCase());
        profileRole.setFont(Font.font("Arial", 10));
        profileRole.setTextFill(Color.web("#c7d2fe"));

        profileInfo.getChildren().addAll(profileName, profileRole);

        profileContainer.getChildren().addAll(profileCircle, profileText);
        profileContainer.setStyle("-fx-cursor: hand;");

        HBox profileBox = new HBox(10);
        profileBox.setAlignment(Pos.CENTER_RIGHT);
        profileBox.getChildren().addAll(profileInfo, profileContainer);

        rightControls.getChildren().addAll(searchBox, notificationsBtn, profileBox);
        topBar.getChildren().addAll(menuToggle, logoBox, spacer, rightControls);

        // Barre des onglets
        HBox tabBar = createTabBar();

        header.getChildren().addAll(topBar, tabBar);
        return header;
    }

    private HBox createTabBar() {
        HBox tabBar = new HBox();
        tabBar.setPadding(new Insets(0, 30, 0, 30));
        tabBar.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
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
            tab.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; " +
                    "-fx-border-color: transparent; -fx-cursor: hand;");
        } else {
            tab.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); " +
                    "-fx-border-color: transparent; -fx-cursor: hand;");
        }

        tab.setOnAction(e -> switchView(viewName));

        tab.setOnMouseEntered(e -> {
            if (!currentView.equals(viewName)) {
                tab.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                        "-fx-border-color: transparent; -fx-cursor: hand;");
            }
        });

        tab.setOnMouseExited(e -> {
            if (!currentView.equals(viewName)) {
                tab.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); " +
                        "-fx-border-color: transparent; -fx-cursor: hand;");
            }
        });

        return tab;
    }

    // ============ SIDEBAR MODERNISÉ ============
    private VBox createModernSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
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
        logoTitle.setTextFill(Color.web("#1e293b"));

        Label logoSubtitle = new Label("Analytics");
        logoSubtitle.setFont(Font.font("Arial", 11));
        logoSubtitle.setTextFill(Color.web("#94a3b8"));

        logoText.getChildren().addAll(logoTitle, logoSubtitle);
        sidebarLogo.getChildren().addAll(sidebarIcon, logoText);

        // Navigation
        VBox navSection = new VBox(5);
        navSection.setPadding(new Insets(10, 15, 20, 15));

        // Dashboard Section
        Label dashboardLabel = new Label("DASHBOARD");
        dashboardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        dashboardLabel.setTextFill(Color.web("#94a3b8"));
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
        appsLabel.setTextFill(Color.web("#94a3b8"));
        appsLabel.setPadding(new Insets(20, 0, 10, 10));

        Button profileBtn = createSidebarButton("👤", "My Profile", "profile", false);
        profileBtn.setOnAction(e -> showUserProfileInMain());

        Button settingsBtn = createSidebarButton("⚙️", "Settings", "settings", false);
        settingsBtn.setOnAction(e -> showSettingsView());

        // Support Section
        Label supportLabel = new Label("SUPPORT");
        supportLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        supportLabel.setTextFill(Color.web("#94a3b8"));
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
        footer.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        // User info in footer
        HBox userFooter = new HBox(10);
        userFooter.setAlignment(Pos.CENTER_LEFT);

        Circle footerAvatar = new Circle(20);
        footerAvatar.setFill(Color.web("#4f46e5"));

        Label footerInitials = new Label(getInitials(currentUser));
        footerInitials.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        footerInitials.setTextFill(Color.WHITE);

        StackPane avatarContainer = new StackPane(footerAvatar, footerInitials);

        VBox userInfo = new VBox(2);
        Label footerName = new Label(currentUser.getNomComplet());
        footerName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        footerName.setTextFill(Color.web("#1e293b"));

        Label footerEmail = new Label(currentUser.getEmail());
        footerEmail.setFont(Font.font("Arial", 10));
        footerEmail.setTextFill(Color.web("#64748b"));

        userInfo.getChildren().addAll(footerName, footerEmail);
        userFooter.getChildren().addAll(avatarContainer, userInfo);

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
            btn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                    "-fx-font-weight: bold; -fx-border-color: transparent; " +
                    "-fx-border-radius: 8;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                    "-fx-border-color: transparent; -fx-border-radius: 8;");
        }

        btn.setOnMouseEntered(e -> {
            if (!currentView.equals(view)) {
                btn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #475569; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent; " +
                        "-fx-border-radius: 8;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!currentView.equals(view)) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
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
                        btn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                                "-fx-font-weight: bold; -fx-border-color: transparent; " +
                                "-fx-border-radius: 8;");
                    } else {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
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

    // ============ DASHBOARD VIEW ============
    private void showDashboard() {
        ScrollPane content = createDashboardView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(content);
        currentView = "dashboard";
        updateSidebarButtons("dashboard");
    }

    private ScrollPane createDashboardView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8f9fa;");

        // En-tête du dashboard
        HBox dashboardHeader = new HBox();
        dashboardHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Welcome back, " + currentUser.getPrenom() + "! Here's what's happening with your platform.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Bouton refresh
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; " +
                "-fx-font-weight: bold; -fx-border-color: #e2e8f0; -fx-border-radius: 8; " +
                "-fx-padding: 10 20; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshDashboard());

        dashboardHeader.getChildren().addAll(headerText, refreshBtn);

        // Cartes de statistiques
        HBox statsCards = createEnhancedStatsCards();

        // Section principale avec graphiques
        HBox chartsSection = new HBox(25);
        chartsSection.setAlignment(Pos.CENTER);

        // Graphique des tendances d'inscription (75% de l'espace)
        VBox trendsChartSection = createEnhancedTrendsChart();
        trendsChartSection.setPrefWidth(900); // Plus large

        // Graphique de distribution (25% de l'espace)
        VBox distributionChartSection = createEnhancedDistributionChart();
        distributionChartSection.setPrefWidth(450); // Plus petit

        chartsSection.getChildren().addAll(trendsChartSection, distributionChartSection);

        // Tableau récapitulatif
        VBox summarySection = createEnhancedSummaryTable();

        container.getChildren().addAll(dashboardHeader, statsCards, chartsSection, summarySection);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    private HBox createEnhancedStatsCards() {
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);

        int totalUsers = userService.countUsers();
        int[] roleStats = userService.getUserStatistics();
        int activeUsers = getActiveUsersThisMonth();
        double growthRate = calculateGrowthRate();

        VBox totalCard = createStatCard("👥", "Total Users",
                String.valueOf(totalUsers), "+" + growthRate + "%", "#4f46e5", true);

        VBox adminCard = createStatCard("👑", "Admins",
                String.valueOf(roleStats[0]), "System Managers", "#10b981", false);

        VBox orgCard = createStatCard("🎯", "Organizers",
                String.valueOf(roleStats[1]), "Event Managers", "#3b82f6", false);

        VBox partCard = createStatCard("😊", "Participants",
                String.valueOf(roleStats[2]), "Active Users", "#f59e0b", false);

        statsCards.getChildren().addAll(totalCard, adminCard, orgCard, partCard);
        return statsCards;
    }

    private VBox createStatCard(String icon, String title, String value, String subtitle, String color, boolean isMain) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(isMain ? 300 : 250);
        card.setPrefHeight(150);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        Circle iconCircle = new Circle(25);
        iconCircle.setFill(Color.web(color + "20"));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));
        iconLabel.setTextFill(Color.web(color));

        iconContainer.getChildren().addAll(iconCircle, iconLabel);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#64748b"));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 12));
        subtitleLabel.setTextFill(Color.web("#94a3b8"));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconContainer, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web("#1e293b"));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    private VBox createEnhancedTrendsChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Registration Trends");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("User registration over time");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        // Contrôles de filtre temporel
        HBox filterControls = new HBox(10);
        filterControls.setAlignment(Pos.CENTER_RIGHT);

        Label periodLabel = new Label("Period:");
        periodLabel.setFont(Font.font("Arial", 12));
        periodLabel.setTextFill(Color.web("#64748b"));

        timeFilterCombo = new ComboBox<>();
        timeFilterCombo.getItems().addAll(
                "Last 7 Days",
                "Last 30 Days",
                "Last 90 Days",
                "This Year",
                "All Time"
        );
        timeFilterCombo.setValue("Last 30 Days");
        timeFilterCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        timeFilterCombo.setPrefWidth(150);
        timeFilterCombo.setOnAction(e -> updateRegistrationChart());

        filterControls.getChildren().addAll(periodLabel, timeFilterCombo);
        header.getChildren().addAll(textContent, filterControls);

        // Area Chart pour les tendances
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelFill(Color.web("#64748b"));
        xAxis.setTickLabelFont(Font.font("Arial", 10));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Registrations");
        yAxis.setTickLabelFill(Color.web("#64748b"));
        yAxis.setTickLabelFont(Font.font("Arial", 10));

        registrationLineChart = new LineChart<>(xAxis, yAxis);
        registrationLineChart.setTitle("");
        registrationLineChart.setLegendVisible(true);
        registrationLineChart.setCreateSymbols(true);
        registrationLineChart.setPrefHeight(350);
        registrationLineChart.setStyle("-fx-background-color: transparent;");

        // Charger les données initiales
        updateRegistrationChart();

        container.getChildren().addAll(header, registrationLineChart);
        return container;
    }

    private void updateRegistrationChart() {
        String period = timeFilterCombo.getValue();
        Map<String, Map<String, Integer>> registrationData = getRegistrationDataByPeriod(period);

        registrationLineChart.getData().clear();

        // Créer une série pour chaque rôle
        XYChart.Series<String, Number> adminSeries = new XYChart.Series<>();
        adminSeries.setName("Admins");

        XYChart.Series<String, Number> orgSeries = new XYChart.Series<>();
        orgSeries.setName("Organizers");

        XYChart.Series<String, Number> partSeries = new XYChart.Series<>();
        partSeries.setName("Participants");

        XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
        totalSeries.setName("Total");

        // Récupérer les périodes (clés du map) et les trier
        List<String> periods = new ArrayList<>(registrationData.keySet());
        periods.sort(Comparator.naturalOrder());

        for (String periodKey : periods) {
            Map<String, Integer> roleData = registrationData.get(periodKey);

            int adminCount = roleData.getOrDefault("admin", 0);
            int orgCount = roleData.getOrDefault("organisateur", 0);
            int partCount = roleData.getOrDefault("participant", 0);
            int totalCount = adminCount + orgCount + partCount;

            adminSeries.getData().add(new XYChart.Data<>(periodKey, adminCount));
            orgSeries.getData().add(new XYChart.Data<>(periodKey, orgCount));
            partSeries.getData().add(new XYChart.Data<>(periodKey, partCount));
            totalSeries.getData().add(new XYChart.Data<>(periodKey, totalCount));
        }

        registrationLineChart.getData().addAll(totalSeries, adminSeries, orgSeries, partSeries);

        // Appliquer les styles aux séries
        applyChartSeriesStyles();
    }

    @SuppressWarnings("unchecked")
    private void applyChartSeriesStyles() {
        for (XYChart.Series<String, Number> series : registrationLineChart.getData()) {
            String seriesName = series.getName();
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    switch (seriesName) {
                        case "Total":
                            node.setStyle("-fx-stroke: #4f46e5; -fx-stroke-width: 3;");
                            break;
                        case "Admins":
                            node.setStyle("-fx-stroke: #10b981; -fx-stroke-width: 2;");
                            break;
                        case "Organizers":
                            node.setStyle("-fx-stroke: #3b82f6; -fx-stroke-width: 2;");
                            break;
                        case "Participants":
                            node.setStyle("-fx-stroke: #f59e0b; -fx-stroke-width: 2;");
                            break;
                    }
                }
            }
        }
    }

    private Map<String, Map<String, Integer>> getRegistrationDataByPeriod(String period) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        List<User> allUsers = userService.getAllUsers();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        DateTimeFormatter formatter;

        switch (period) {
            case "Last 7 Days":
                startDate = now.minusDays(7);
                formatter = DateTimeFormatter.ofPattern("EEE");
                break;
            case "Last 30 Days":
                startDate = now.minusDays(30);
                formatter = DateTimeFormatter.ofPattern("dd/MM");
                break;
            case "Last 90 Days":
                startDate = now.minusDays(90);
                formatter = DateTimeFormatter.ofPattern("MM/dd");
                break;
            case "This Year":
                startDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0);
                formatter = DateTimeFormatter.ofPattern("MMM");
                break;
            default: // "All Time"
                startDate = LocalDateTime.MIN;
                formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                break;
        }

        // Filtrer les utilisateurs par date
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getCreatedAt() != null &&
                        (startDate.equals(LocalDateTime.MIN) ||
                                user.getCreatedAt().isAfter(startDate)))
                .collect(Collectors.toList());

        // Grouper par période selon l'intervalle
        for (User user : filteredUsers) {
            String periodKey = user.getCreatedAt().format(formatter);
            String role = user.getRole().toLowerCase();

            result.putIfAbsent(periodKey, new HashMap<>());
            Map<String, Integer> roleMap = result.get(periodKey);
            roleMap.put(role, roleMap.getOrDefault(role, 0) + 1);

            // Ajouter aussi au total
            roleMap.put("total", roleMap.getOrDefault("total", 0) + 1);
        }

        // S'assurer que toutes les périodes sont présentes
        ensureAllPeriods(result, period, formatter);

        // Trier par ordre chronologique
        return sortByChronologicalOrder(result, period);
    }

    private void ensureAllPeriods(Map<String, Map<String, Integer>> data, String period, DateTimeFormatter formatter) {
        LocalDateTime now = LocalDateTime.now();
        List<String> expectedPeriods = new ArrayList<>();

        switch (period) {
            case "Last 7 Days":
                for (int i = 6; i >= 0; i--) {
                    expectedPeriods.add(now.minusDays(i).format(formatter));
                }
                break;
            case "Last 30 Days":
                for (int i = 29; i >= 0; i--) {
                    expectedPeriods.add(now.minusDays(i).format(formatter));
                }
                break;
            case "Last 90 Days":
                for (int i = 89; i >= 0; i--) {
                    expectedPeriods.add(now.minusDays(i).format(formatter));
                }
                break;
            case "This Year":
                for (int i = 1; i <= 12; i++) {
                    expectedPeriods.add(LocalDateTime.of(now.getYear(), i, 1, 0, 0).format(formatter));
                }
                break;
        }

        for (String periodKey : expectedPeriods) {
            if (!data.containsKey(periodKey)) {
                Map<String, Integer> zeroMap = new HashMap<>();
                zeroMap.put("admin", 0);
                zeroMap.put("organisateur", 0);
                zeroMap.put("participant", 0);
                zeroMap.put("total", 0);
                data.put(periodKey, zeroMap);
            }
        }
    }

    private Map<String, Map<String, Integer>> sortByChronologicalOrder(Map<String, Map<String, Integer>> data, String period) {
        Map<String, Map<String, Integer>> sortedData = new LinkedHashMap<>();

        List<String> sortedKeys = new ArrayList<>(data.keySet());
        sortedKeys.sort(Comparator.naturalOrder());

        for (String key : sortedKeys) {
            sortedData.put(key, data.get(key));
        }

        return sortedData;
    }

    private VBox createEnhancedDistributionChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Role Distribution");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("User distribution by role");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Pie Chart pour les rôles
        rolePieChart = new PieChart();
        rolePieChart.setLabelsVisible(true);
        rolePieChart.setLegendVisible(false);
        rolePieChart.setPrefSize(300, 300);
        rolePieChart.setStyle("-fx-background-color: transparent;");

        updateRoleChart();

        container.getChildren().addAll(header, rolePieChart);
        return container;
    }

    private void updateRoleChart() {
        int[] roleStats = userService.getUserStatistics();
        int total = roleStats[0] + roleStats[1] + roleStats[2];

        rolePieChart.getData().clear();

        if (total > 0) {
            PieChart.Data adminSlice = new PieChart.Data(
                    String.format("Admins\n%d (%.1f%%)", roleStats[0], (roleStats[0] * 100.0 / total)),
                    roleStats[0]
            );
            PieChart.Data orgSlice = new PieChart.Data(
                    String.format("Organizers\n%d (%.1f%%)", roleStats[1], (roleStats[1] * 100.0 / total)),
                    roleStats[1]
            );
            PieChart.Data partSlice = new PieChart.Data(
                    String.format("Participants\n%d (%.1f%%)", roleStats[2], (roleStats[2] * 100.0 / total)),
                    roleStats[2]
            );

            rolePieChart.getData().addAll(adminSlice, orgSlice, partSlice);

            // Appliquer les couleurs
            adminSlice.getNode().setStyle("-fx-pie-color: #4f46e5;");
            orgSlice.getNode().setStyle("-fx-pie-color: #3b82f6;");
            partSlice.getNode().setStyle("-fx-pie-color: #10b981;");
        }
    }

    private VBox createEnhancedSummaryTable() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Recent Users");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Latest registered users");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Tableau des utilisateurs récents
        TableView<User> recentTable = new TableView<>();
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recentTable.setPrefHeight(250);
        recentTable.setStyle("-fx-background-color: transparent;");

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        nameCol.setPrefWidth(200);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role.toUpperCase());
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(4, 12, 4, 12));
                    setFont(Font.font("Arial", FontWeight.BOLD, 11));

                    switch (role.toLowerCase()) {
                        case "admin":
                            setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; -fx-background-radius: 15;");
                            break;
                        case "organisateur":
                            setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-background-radius: 15;");
                            break;
                        case "participant":
                            setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; -fx-background-radius: 15;");
                            break;
                    }
                }
            }
        });
        roleCol.setPrefWidth(120);

        TableColumn<User, String> dateCol = new TableColumn<>("Registration Date");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setPrefWidth(120);

        recentTable.getColumns().addAll(nameCol, emailCol, roleCol, dateCol);

        // Charger les 10 derniers utilisateurs
        List<User> allUsers = userService.getAllUsers();
        List<User> recentUsers = allUsers.stream()
                .sorted((u1, u2) -> {
                    if (u1.getCreatedAt() == null && u2.getCreatedAt() == null) return 0;
                    if (u1.getCreatedAt() == null) return 1;
                    if (u2.getCreatedAt() == null) return -1;
                    return u2.getCreatedAt().compareTo(u1.getCreatedAt());
                })
                .limit(10)
                .collect(Collectors.toList());

        recentTable.setItems(FXCollections.observableArrayList(recentUsers));

        container.getChildren().addAll(header, recentTable);
        return container;
    }

    // ============ PROFIL UTILISATEUR ============
    private void showUserProfileInMain() {
        VBox profileView = createEnhancedUserProfileView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(profileView);
        currentView = "profile";
        updateSidebarButtons("profile");
    }

    private VBox createEnhancedUserProfileView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8f9fa;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Manage your personal information and settings");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; " +
                "-fx-font-weight: bold; -fx-border-color: transparent; -fx-cursor: hand;");
        backBtn.setOnAction(e -> showDashboard());

        header.getChildren().addAll(headerText, backBtn);

        // Contenu du profil
        VBox profileContent = new VBox(30);
        profileContent.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Section Avatar et Informations
        HBox topSection = new HBox(40);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Avatar avec option de modification
        VBox avatarBox = new VBox(20);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPrefWidth(250);

        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(80);

        // Charger l'image de profil si disponible
        ImageView avatarImageView = null;
        if (currentUser.getPhoto() != null && !currentUser.getPhoto().equals("default.jpg")) {
            try {
                File imageFile = new File(currentUser.getPhoto());
                if (imageFile.exists()) {
                    Image avatarImage = new Image("file:" + currentUser.getPhoto());
                    avatarImageView = new ImageView(avatarImage);
                    avatarImageView.setFitWidth(160);
                    avatarImageView.setFitHeight(160);
                    avatarImageView.setPreserveRatio(true);
                    avatarCircle.setFill(Color.TRANSPARENT);
                    avatarContainer.getChildren().add(avatarImageView);
                } else {
                    // Si l'image n'existe pas, utiliser l'avatar par défaut
                    createDefaultAvatar(avatarContainer, avatarCircle);
                }
            } catch (Exception e) {
                createDefaultAvatar(avatarContainer, avatarCircle);
            }
        } else {
            createDefaultAvatar(avatarContainer, avatarCircle);
        }

        Button changeAvatarBtn = new Button("📷 Change Photo");
        changeAvatarBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        changeAvatarBtn.setOnAction(e -> changeProfilePicture());

        avatarBox.getChildren().addAll(avatarContainer, changeAvatarBtn);

        // Informations de base
        VBox basicInfo = new VBox(20);
        basicInfo.setStyle("-fx-padding: 20 0 0 0;");

        Label nameLabel = new Label(currentUser.getNomComplet());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        nameLabel.setTextFill(Color.web("#1e293b"));

        HBox roleBox = new HBox(10);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Label roleLabel = new Label(currentUser.getRole().toUpperCase());
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        roleLabel.setTextFill(Color.WHITE);
        roleLabel.setPadding(new Insets(8, 20, 8, 20));
        roleLabel.setStyle("-fx-background-color: #4f46e5; -fx-background-radius: 20;");

        Label emailLabel = new Label(currentUser.getEmail());
        emailLabel.setFont(Font.font("Arial", 16));
        emailLabel.setTextFill(Color.web("#64748b"));

        Label memberSinceLabel = new Label("Member since: " +
                (currentUser.getCreatedAt() != null ?
                        currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) :
                        "Unknown date"));
        memberSinceLabel.setFont(Font.font("Arial", 12));
        memberSinceLabel.setTextFill(Color.web("#94a3b8"));

        roleBox.getChildren().addAll(roleLabel);
        basicInfo.getChildren().addAll(nameLabel, roleBox, emailLabel, memberSinceLabel);

        topSection.getChildren().addAll(avatarBox, basicInfo);

        // Séparateur
        Separator separator = new Separator();
        separator.setPadding(new Insets(20, 0, 20, 0));

        // Formulaire de modification
        VBox formSection = new VBox(20);
        formSection.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-padding: 30;");

        Label formTitle = new Label("Edit Information");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        formTitle.setTextFill(Color.web("#1e293b"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(30);
        formGrid.setVgap(20);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        // Champs de formulaire
        TextField nomField = new TextField(currentUser.getNom());
        styleFormTextField(nomField);

        TextField prenomField = new TextField(currentUser.getPrenom());
        styleFormTextField(prenomField);

        TextField emailField = new TextField(currentUser.getEmail());
        styleFormTextField(emailField);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        if (currentUser.getIdGenre() == 1) {
            genreCombo.setValue("Homme");
        } else if (currentUser.getIdGenre() == 2) {
            genreCombo.setValue("Femme");
        } else {
            genreCombo.setValue("Non spécifié");
        }
        styleFormComboBox(genreCombo);

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current password");
        styleFormTextField(currentPasswordField);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password (optional)");
        styleFormTextField(newPasswordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        styleFormTextField(confirmPasswordField);

        // Ajouter les champs au formulaire
        formGrid.add(new Label("Last Name:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("First Name:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Gender:"), 0, 3);
        formGrid.add(genreCombo, 1, 3);
        formGrid.add(new Label("Current Password:"), 0, 4);
        formGrid.add(currentPasswordField, 1, 4);
        formGrid.add(new Label("New Password:"), 0, 5);
        formGrid.add(newPasswordField, 1, 5);
        formGrid.add(new Label("Confirm Password:"), 0, 6);
        formGrid.add(confirmPasswordField, 1, 6);

        formSection.getChildren().addAll(formTitle, formGrid);

        // Boutons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> showDashboard());

        Button saveBtn = new Button("💾 Save Changes");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            if (saveProfileChanges(nomField, prenomField, emailField, genreCombo,
                    currentPasswordField, newPasswordField, confirmPasswordField)) {
                showAlert("Success", "Your profile has been updated successfully!");
                // Mettre à jour l'utilisateur courant
                currentUser = userService.getUserById(currentUser.getId());
                SessionManager.setCurrentUser(currentUser);
                // Rafraîchir l'affichage
                showUserProfileInMain();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        profileContent.getChildren().addAll(topSection, separator, formSection, buttonBox);
        container.getChildren().addAll(header, profileContent);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return new VBox(scrollPane);
    }

    private void createDefaultAvatar(StackPane container, Circle circle) {
        circle.setFill(Color.web("#4f46e5"));
        String initials = getInitials(currentUser);
        Label avatarText = new Label(initials);
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        avatarText.setTextFill(Color.WHITE);
        container.getChildren().addAll(circle, avatarText);
    }

    // ============ GESTION DES UTILISATEURS ============
    private void showUserManagementViewInCenter() {
        ScrollPane content = createEnhancedUserManagementView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(content);
        currentView = "users";
        updateSidebarButtons("users");
    }

    private ScrollPane createEnhancedUserManagementView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8f9fa;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Users Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Manage all users in the LOOPI platform");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button addUserBtn = new Button("➕ Add User");
        addUserBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");
        addUserBtn.setOnAction(e -> showAddUserDialog());

        header.getChildren().addAll(headerText, addUserBtn);

        // Barre d'outils
        HBox toolbar = createEnhancedUserToolbar();

        // Tableau des utilisateurs
        VBox tableContainer = new VBox(15);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableTitleBox = new VBox(2);
        Label tableTitle = new Label("All Users");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#1e293b"));

        int userCount = userService.countUsers();
        Label tableSubtitle = new Label(userCount + " users found");
        tableSubtitle.setFont(Font.font("Arial", 12));
        tableSubtitle.setTextFill(Color.web("#64748b"));

        tableTitleBox.getChildren().addAll(tableTitle, tableSubtitle);
        HBox.setHgrow(tableTitleBox, Priority.ALWAYS);

        // Boutons d'export
        HBox exportButtons = new HBox(10);
        exportButtons.setAlignment(Pos.CENTER_RIGHT);

        Button exportCSVBtn = new Button("📥 CSV");
        exportCSVBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        exportCSVBtn.setOnAction(e -> exportUsersToCSV());

        Button exportPDFBtn = new Button("📥 PDF");
        exportPDFBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        exportPDFBtn.setOnAction(e -> showAlert("Info", "PDF export coming soon"));

        exportButtons.getChildren().addAll(exportCSVBtn, exportPDFBtn);
        tableHeader.getChildren().addAll(tableTitleBox, exportButtons);

        // Initialiser et configurer le tableau
        initializeUserTable();

        tableContainer.getChildren().addAll(tableHeader, userTable);
        container.getChildren().addAll(header, toolbar, tableContainer);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createEnhancedUserToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Bouton Actualiser
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshUserTable());

        // Filtre par rôle
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter:");
        filterLabel.setFont(Font.font("Arial", 12));
        filterLabel.setTextFill(Color.web("#64748b"));

        currentRoleFilter = new ComboBox<>();
        currentRoleFilter.getItems().addAll("All Roles", "Admin", "Organizer", "Participant");
        currentRoleFilter.setValue("All Roles");
        currentRoleFilter.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        currentRoleFilter.setPrefWidth(150);
        currentRoleFilter.setOnAction(e -> filterUsersByRole(currentRoleFilter.getValue()));

        filterBox.getChildren().addAll(filterLabel, currentRoleFilter);

        // Champ de recherche
        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search users...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-padding: 10 15;");
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                searchUsers(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                "-fx-padding: 10 15; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> searchUsers(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        toolbar.getChildren().addAll(refreshBtn, filterBox, searchBox);
        return toolbar;
    }

    @SuppressWarnings("unchecked")
    private void initializeUserTable() {
        userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPrefHeight(500);
        userTable.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        // Colonne ID
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setSortable(true);

        // Colonne Avatar
        TableColumn<User, String> avatarCol = new TableColumn<>("");
        avatarCol.setPrefWidth(60);
        avatarCol.setCellFactory(column -> new TableCell<User, String>() {
            private final StackPane avatarContainer = new StackPane();
            private final Circle avatarCircle = new Circle(20);
            private final Label avatarText = new Label();

            {
                avatarCircle.setFill(Color.web("#4f46e5"));
                avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                avatarText.setTextFill(Color.WHITE);
                avatarContainer.getChildren().addAll(avatarCircle, avatarText);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    String initials = getInitials(user);
                    avatarText.setText(initials);
                    setGraphic(avatarContainer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Nom complet
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> {
            String fullName = cellData.getValue().getPrenom() + " " + cellData.getValue().getNom();
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });
        nameCol.setPrefWidth(200);

        // Colonne Email
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        // Colonne Rôle
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role.toUpperCase());
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(4, 12, 4, 12));
                    setFont(Font.font("Arial", FontWeight.BOLD, 11));

                    switch (role.toLowerCase()) {
                        case "admin":
                            setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; -fx-background-radius: 15;");
                            break;
                        case "organisateur":
                            setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-background-radius: 15;");
                            break;
                        case "participant":
                            setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; -fx-background-radius: 15;");
                            break;
                    }
                }
            }
        });

        // Colonne Genre
        TableColumn<User, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        genderCol.setPrefWidth(100);
        genderCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Date d'inscription
        TableColumn<User, String> dateCol = new TableColumn<>("Registration Date");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Statut
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<User, String>() {
            private final Circle statusDot = new Circle(5);
            private final Label statusText = new Label();
            private final HBox container = new HBox(8, statusDot, statusText);

            {
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Simuler un statut (actif/inactif)
                    boolean isActive = Math.random() > 0.3; // 70% de chance d'être actif
                    if (isActive) {
                        statusDot.setFill(Color.web("#10b981"));
                        statusText.setText("Active");
                        statusText.setTextFill(Color.web("#10b981"));
                    } else {
                        statusDot.setFill(Color.web("#94a3b8"));
                        statusText.setText("Inactive");
                        statusText.setTextFill(Color.web("#94a3b8"));
                    }
                    setGraphic(container);
                }
            }
        });

        // Colonne Actions
        TableColumn<User, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editSelectedUser(user);
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteSelectedUser(user);
                });

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        userTable.getColumns().addAll(idCol, avatarCol, nameCol, emailCol, roleCol, genderCol, dateCol, statusCol, actionCol);

        // Style des lignes
        userTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setStyle("");
                } else {
                    // Alternance de couleurs
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f8fafc;");
                    } else {
                        setStyle("-fx-background-color: white;");
                    }

                    // Surlignage au survol
                    setOnMouseEntered(e -> {
                        if (!isEmpty()) {
                            setStyle("-fx-background-color: #eef2ff;");
                        }
                    });

                    setOnMouseExited(e -> {
                        if (!isEmpty()) {
                            if (getIndex() % 2 == 0) {
                                setStyle("-fx-background-color: #f8fafc;");
                            } else {
                                setStyle("-fx-background-color: white;");
                            }
                        }
                    });

                    // Sélection
                    selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        if (isNowSelected) {
                            setStyle("-fx-background-color: #e0e7ff; -fx-font-weight: bold;");
                        } else {
                            if (getIndex() % 2 == 0) {
                                setStyle("-fx-background-color: #f8fafc;");
                            } else {
                                setStyle("-fx-background-color: white;");
                            }
                        }
                    });
                }
            }
        });

        // Charger les données
        refreshUserTable();
    }

    private void refreshUserTable() {
        List<User> users = userService.getAllUsers();
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
        userTable.refresh();
    }

    private void filterUsersByRole(String roleFilter) {
        if (roleFilter.equals("All Roles")) {
            refreshUserTable();
            return;
        }

        String role = "";
        switch (roleFilter) {
            case "Admin": role = "admin"; break;
            case "Organizer": role = "organisateur"; break;
            case "Participant": role = "participant"; break;
        }

        List<User> filteredUsers = userService.getUsersByRole(role);
        userList = FXCollections.observableArrayList(filteredUsers);
        userTable.setItems(userList);
    }

    private void searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (currentRoleFilter != null && !currentRoleFilter.getValue().equals("All Roles")) {
                filterUsersByRole(currentRoleFilter.getValue());
            } else {
                refreshUserTable();
            }
            return;
        }

        List<User> users = userService.searchUsers(keyword);
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    // ============ DIALOGUES UTILISATEUR ============
    private void showAddUserDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add New User");
        dialogStage.initOwner(primaryStage);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: white;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Add New User");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Fill in the new user's information");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        header.getChildren().add(headerText);

        // Formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        // Champs de saisie
        TextField nomField = new TextField();
        nomField.setPromptText("Last Name");
        styleFormTextField(nomField);

        TextField prenomField = new TextField();
        prenomField.setPromptText("First Name");
        styleFormTextField(prenomField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min. 8 characters)");
        styleFormTextField(passwordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        styleFormTextField(confirmPasswordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue("participant");
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue("Non spécifié");
        styleFormComboBox(genreCombo);

        // Ajout des labels et champs
        formGrid.add(new Label("Last Name *:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("First Name *:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email *:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Password *:"), 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(new Label("Confirm *:"), 0, 4);
        formGrid.add(confirmPasswordField, 1, 4);
        formGrid.add(new Label("Role *:"), 0, 5);
        formGrid.add(roleCombo, 1, 5);
        formGrid.add(new Label("Gender:"), 0, 6);
        formGrid.add(genreCombo, 1, 6);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button addBtn = new Button("Add User");
        addBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            if (validateAndAddUser(nomField, prenomField, emailField, passwordField,
                    confirmPasswordField, roleCombo, genreCombo, errorLabel)) {
                refreshUserTable();
                dialogStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, addBtn);
        mainLayout.getChildren().addAll(header, formGrid, errorLabel, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private boolean validateAndAddUser(TextField nomField, TextField prenomField, TextField emailField,
                                       PasswordField passwordField, PasswordField confirmPasswordField,
                                       ComboBox<String> roleCombo, ComboBox<String> genreCombo, Label errorLabel) {
        // Validation
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showError(errorLabel, "Please fill all required fields (*)");
            return false;
        }

        if (!isValidEmail(emailField.getText())) {
            showError(errorLabel, "Please enter a valid email address");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            showError(errorLabel, "Password must be at least 8 characters long");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError(errorLabel, "Passwords do not match");
            return false;
        }

        if (userService.emailExists(emailField.getText())) {
            showError(errorLabel, "This email is already used by another user");
            return false;
        }

        // Création de l'utilisateur
        User newUser = new User();
        newUser.setNom(nomField.getText());
        newUser.setPrenom(prenomField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(passwordField.getText());
        newUser.setRole(roleCombo.getValue());
        newUser.setPhoto("default.jpg");

        // Définir l'idGenre
        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) {
            newUser.setIdGenre(1);
        } else if ("Femme".equals(genre)) {
            newUser.setIdGenre(2);
        } else {
            newUser.setIdGenre(3);
        }

        // Ajout dans la base de données
        if (userService.addUser(newUser)) {
            showAlert("Success", "User added successfully!");
            return true;
        } else {
            showError(errorLabel, "Error adding user");
            return false;
        }
    }

    private void editSelectedUser(User user) {
        if (user.getId() == currentUser.getId() && !currentUser.isAdmin()) {
            showAlert("Warning", "You cannot edit your own account from this interface");
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit User");
        dialogStage.initOwner(primaryStage);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: white;");

        // En-tête avec avatar
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(25);
        avatarCircle.setFill(Color.web("#4f46e5"));

        String initials = getInitials(user);
        Label avatarText = new Label(initials);
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        avatarText.setTextFill(Color.WHITE);

        avatarContainer.getChildren().addAll(avatarCircle, avatarText);

        VBox headerText = new VBox(5);
        Label title = new Label("Edit User");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label(user.getNomComplet());
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        header.getChildren().addAll(avatarContainer, headerText);

        // Formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        TextField nomField = new TextField(user.getNom());
        styleFormTextField(nomField);

        TextField prenomField = new TextField(user.getPrenom());
        styleFormTextField(prenomField);

        TextField emailField = new TextField(user.getEmail());
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New password (leave empty to keep)");
        styleFormTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue(user.getRole());
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        if (user.getIdGenre() == 1) {
            genreCombo.setValue("Homme");
        } else if (user.getIdGenre() == 2) {
            genreCombo.setValue("Femme");
        } else {
            genreCombo.setValue("Non spécifié");
        }
        styleFormComboBox(genreCombo);

        formGrid.add(new Label("Last Name *:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("First Name *:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email *:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("New Password:"), 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(new Label("Role *:"), 0, 4);
        formGrid.add(roleCombo, 1, 4);
        formGrid.add(new Label("Gender:"), 0, 5);
        formGrid.add(genreCombo, 1, 5);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            if (validateAndUpdateUser(user, nomField, prenomField, emailField, passwordField,
                    roleCombo, genreCombo, errorLabel)) {
                refreshUserTable();
                dialogStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        mainLayout.getChildren().addAll(header, formGrid, errorLabel, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private boolean validateAndUpdateUser(User user, TextField nomField, TextField prenomField,
                                          TextField emailField, PasswordField passwordField,
                                          ComboBox<String> roleCombo, ComboBox<String> genreCombo,
                                          Label errorLabel) {
        // Validation
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty()) {
            showError(errorLabel, "Please fill all required fields (*)");
            return false;
        }

        if (!isValidEmail(emailField.getText())) {
            showError(errorLabel, "Please enter a valid email address");
            return false;
        }

        // Vérifier si l'email a changé
        if (!emailField.getText().equals(user.getEmail()) &&
                userService.emailExists(emailField.getText())) {
            showError(errorLabel, "This email is already used by another user");
            return false;
        }

        // Mettre à jour les informations
        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setEmail(emailField.getText());
        user.setRole(roleCombo.getValue());

        // Mettre à jour le mot de passe si fourni
        if (!passwordField.getText().isEmpty()) {
            if (passwordField.getText().length() < 8) {
                showError(errorLabel, "Password must be at least 8 characters long");
                return false;
            }
            user.setPassword(passwordField.getText());
        }

        // Mettre à jour l'idGenre
        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) {
            user.setIdGenre(1);
        } else if ("Femme".equals(genre)) {
            user.setIdGenre(2);
        } else {
            user.setIdGenre(3);
        }

        // Mettre à jour dans la base de données
        if (userService.updateUser(user)) {
            showAlert("Success", "User updated successfully!");
            return true;
        } else {
            showError(errorLabel, "Error updating user");
            return false;
        }
    }

    private void deleteSelectedUser(User user) {
        // Empêcher la suppression de son propre compte
        if (user.getId() == currentUser.getId()) {
            showAlert("Error", "You cannot delete your own account!");
            return;
        }

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete the user:\n\n" +
                "Name: " + user.getNomComplet() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "Role: " + user.getRole() + "\n\n" +
                "This action cannot be undone.");
        confirmAlert.initOwner(primaryStage);

        ButtonType yesButton = new ButtonType("Yes, Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            if (userService.deleteUser(user.getId())) {
                showAlert("Success", "User deleted successfully!");
                refreshUserTable();
            } else {
                showAlert("Error", "Error deleting user");
            }
        }
    }

    // ============ MÉTHODES UTILITAIRES ============
    private void styleFormTextField(TextField field) {
        field.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        field.setPrefWidth(300);
    }

    private void styleFormComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        comboBox.setPrefWidth(300);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
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
        searchUsers(keyword);
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

    private void refreshDashboard() {
        showDashboard();
        showAlert("Refreshed", "Dashboard data has been refreshed");
    }

    private void showAnalyticsView() {
        showAlert("Info", "Analytics view coming soon");
    }

    private void showSettingsView() {
        showAlert("Info", "Settings view coming soon");
    }

    private void showHelpCenter() {
        showAlert("Help Center", "For assistance, please contact: support@loopi.tn");
    }

    private void showContactSupport() {
        showAlert("Contact Support", "Email: support@loopi.tn\nPhone: +216 XX XXX XXX");
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    // ============ GESTION DU PROFIL ============
    private boolean saveProfileChanges(TextField nomField, TextField prenomField, TextField emailField,
                                       ComboBox<String> genreCombo, PasswordField currentPasswordField,
                                       PasswordField newPasswordField, PasswordField confirmPasswordField) {
        // Validation des champs obligatoires
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "Last Name, First Name and Email are required");
            return false;
        }

        // Validation de l'email
        if (!isValidEmail(emailField.getText())) {
            showAlert("Error", "Please enter a valid email address");
            return false;
        }

        // Vérifier si l'email a changé
        if (!emailField.getText().equals(currentUser.getEmail())) {
            if (userService.emailExists(emailField.getText())) {
                showAlert("Error", "This email is already used by another user");
                return false;
            }
        }

        // Vérification du mot de passe actuel si changement de mot de passe demandé
        if (!newPasswordField.getText().isEmpty()) {
            if (currentPasswordField.getText().isEmpty()) {
                showAlert("Error", "Please enter your current password to change password");
                return false;
            }

            if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
                showAlert("Error", "Current password is incorrect");
                return false;
            }

            if (newPasswordField.getText().length() < 8) {
                showAlert("Error", "New password must be at least 8 characters long");
                return false;
            }

            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Error", "New passwords do not match");
                return false;
            }
        }

        // Mettre à jour l'utilisateur
        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setEmail(emailField.getText());

        // Mettre à jour l'idGenre
        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) {
            currentUser.setIdGenre(1);
        } else if ("Femme".equals(genre)) {
            currentUser.setIdGenre(2);
        } else {
            currentUser.setIdGenre(3);
        }

        // Mettre à jour le mot de passe si nécessaire
        if (!newPasswordField.getText().isEmpty()) {
            currentUser.setPassword(newPasswordField.getText());
        }

        // Sauvegarder dans la base de données
        if (userService.updateUser(currentUser)) {
            return true;
        } else {
            showAlert("Error", "An error occurred while updating the profile");
            return false;
        }
    }

    private void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                // Créer un dossier pour les photos de profil si nécessaire
                File profileDir = new File("profiles");
                if (!profileDir.exists()) {
                    profileDir.mkdir();
                }

                // Copier le fichier dans le dossier profiles
                String newFileName = "profile_" + currentUser.getId() + "_" +
                        System.currentTimeMillis() + getFileExtension(selectedFile.getName());
                File destFile = new File("profiles/" + newFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Mettre à jour le chemin de la photo dans l'utilisateur
                currentUser.setPhoto(destFile.getPath());

                // Mettre à jour dans la base de données
                if (userService.updateUser(currentUser)) {
                    showAlert("Success", "Profile picture updated successfully!");
                    // Rafraîchir l'affichage pour montrer la nouvelle image
                    showUserProfileInMain();
                } else {
                    showAlert("Error", "Error updating profile picture");
                }

            } catch (Exception e) {
                showAlert("Error", "Error loading image: " + e.getMessage());
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    // ============ MÉTHODES DE STATISTIQUES ============
    private double calculateGrowthRate() {
        // Simuler un taux de croissance de 5%
        return 5.0;
    }

    private int getActiveUsersThisMonth() {
        // Simuler 75% des utilisateurs actifs ce mois
        return (int)(userService.countUsers() * 0.75);
    }

    // ============ EXPORT UTILISATEURS ============
    private void exportUsersToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Users");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        fileChooser.setInitialFileName("loopi_users_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                List<User> users = userTable.getItems();
                exportToCSV(users, file);

                showAlert("Export Successful",
                        users.size() + " users exported successfully to:\n" +
                                file.getAbsolutePath());

            } catch (Exception e) {
                showAlert("Export Error", "Error during export: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToCSV(List<User> users, File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            // En-tête
            writer.write("ID,Last Name,First Name,Email,Role,Gender,Registration Date,Status\n");

            // Données
            for (User user : users) {
                String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                        user.getId(),
                        user.getNom(),
                        user.getPrenom(),
                        user.getEmail(),
                        user.getRole(),
                        user.getSexe() != null ? user.getSexe() : "",
                        user.getCreatedAt() != null ?
                                user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "",
                        "Active"
                );
                writer.write(line);
            }
        }
    }
}