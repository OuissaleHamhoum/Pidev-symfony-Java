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

    // Pour le filtre
    private ComboBox<String> currentRoleFilter;
    private ComboBox<String> timeFilterCombo;

    // Données pour les graphiques
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
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createModernHeader();
        root.setTop(header);

        VBox sidebar = createSimplifiedSidebar();
        root.setLeft(sidebar);

        // Zone de contenu principale
        mainContentArea = new StackPane();
        ScrollPane dashboardContent = createDashboardView();
        mainContentArea.getChildren().add(dashboardContent);
        root.setCenter(mainContentArea);

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        SessionManager.printSessionInfo();
    }

    private HBox createModernHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo/Titre
        HBox logoBox = new HBox(15);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Button menuToggle = new Button("☰");
        menuToggle.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 16px; -fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 8;");
        menuToggle.setOnAction(e -> toggleSidebar());

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("LOOPI ADMIN");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        mainTitle.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Tableau de bord analytique");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setTextFill(Color.web("#64748b"));

        titleBox.getChildren().addAll(mainTitle, subtitle);
        logoBox.getChildren().addAll(menuToggle, titleBox);

        // Espaceur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Barre de recherche
        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(5, 15, 5, 15));

        TextField searchField = new TextField();
        searchField.setPromptText("Type to search...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 14px;");
        searchField.setPrefWidth(250);
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performGlobalSearch(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b;");

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Profil utilisateur
        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_RIGHT);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label userName = new Label(currentUser.getNomComplet());
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        userName.setTextFill(Color.web("#1e293b"));

        Label userRole = new Label(currentUser.getRole().toUpperCase());
        userRole.setFont(Font.font("Arial", 10));
        userRole.setTextFill(Color.web("#4f46e5"));

        userInfo.getChildren().addAll(userName, userRole);

        // Avatar
        StackPane avatarContainer = new StackPane();
        avatarContainer.setOnMouseClicked(e -> showUserProfileInMain());

        Circle avatarCircle = new Circle(20);
        avatarCircle.setFill(Color.web("#4f46e5"));

        String initials = getInitials(currentUser);
        Label avatarText = new Label(initials);
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        avatarText.setTextFill(Color.WHITE);

        avatarContainer.getChildren().addAll(avatarCircle, avatarText);
        avatarContainer.setStyle("-fx-cursor: hand;");

        userProfile.getChildren().addAll(searchBox, userInfo, avatarContainer);
        header.getChildren().addAll(logoBox, spacer, userProfile);

        return header;
    }

    private VBox createSimplifiedSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(30, 0, 0, 0));

        // Navigation principale simplifiée
        VBox navSection = new VBox(5);
        navSection.setPadding(new Insets(0, 20, 30, 20));

        Label navTitle = new Label("MENU PRINCIPAL");
        navTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        navTitle.setTextFill(Color.web("#94a3b8"));
        navTitle.setPadding(new Insets(0, 0, 15, 0));

        // Boutons de navigation simplifiés
        Button dashboardBtn = createNavButton("📊 Tableau de bord", true);
        dashboardBtn.setOnAction(e -> showDashboard());

        Button profileBtn = createNavButton("👤 Mon Profil", false);
        profileBtn.setOnAction(e -> showUserProfileInMain());

        Button tablesBtn = createNavButton("📋 Gestion Utilisateurs", false);
        tablesBtn.setOnAction(e -> showUserManagementViewInCenter());

        Button supportBtn = createNavButton("🆘 Support", false);
        supportBtn.setOnAction(e -> showAlert("Information", "Support technique: contact@loopi.tn"));

        // Bouton Déconnexion
        Button logoutBtn = createNavButton("🚪 Déconnexion", false);
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                "-fx-font-size: 14px; -fx-border-color: transparent;");
        logoutBtn.setOnAction(e -> logout());

        navSection.getChildren().addAll(navTitle, dashboardBtn, profileBtn, tablesBtn, supportBtn, logoutBtn);

        // Espaceur
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer sidebar
        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));
        footer.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Label version = new Label("LOOPI v2.0");
        version.setFont(Font.font("Arial", 10));
        version.setTextFill(Color.web("#94a3b8"));

        Label status = new Label("✓ Connecté");
        status.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        status.setTextFill(Color.web("#10b981"));

        footer.getChildren().addAll(version, status);
        sidebar.getChildren().addAll(navSection, spacer, footer);

        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(210);
        btn.setPrefHeight(40);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 15));
        btn.setFont(Font.font("Arial", 14));

        if (active) {
            btn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                    "-fx-font-weight: bold; -fx-border-color: #c7d2fe; " +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 0;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                    "-fx-border-color: transparent;");
        }

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#4f46e5")) {
                btn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #475569; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#4f46e5")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        });

        return btn;
    }

    private ScrollPane createDashboardView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        // Statistiques basées sur les données réelles
        HBox statsCards = createRealStatsCards();

        // Contrôles de filtre temporel
        HBox filterControls = createTimeFilterControls();

        // Section graphiques côte à côte
        HBox chartsSection = new HBox(25);
        chartsSection.setAlignment(Pos.CENTER);

        // Graphique d'inscriptions à gauche
        VBox leftChartSection = createEnhancedRegistrationTrendChart();
        leftChartSection.setPrefWidth(750);

        // Graphique de distribution à droite
        VBox rightChartSection = createEnhancedRoleDistributionChart();
        rightChartSection.setPrefWidth(750);

        chartsSection.getChildren().addAll(leftChartSection, rightChartSection);

        // Tableau récapitulatif
        VBox summaryTable = createUserSummaryTable();

        container.getChildren().addAll(statsCards, filterControls, chartsSection, summaryTable);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createTimeFilterControls() {
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_RIGHT);
        filterBox.setPadding(new Insets(0, 0, 10, 0));

        Label filterLabel = new Label("Période:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        filterLabel.setTextFill(Color.web("#64748b"));

        timeFilterCombo = new ComboBox<>();
        timeFilterCombo.getItems().addAll(
                "Dernier jour",
                "2 derniers jours",
                "3 derniers jours",
                "7 derniers jours",
                "Ce mois",
                "Cette année",
                "Tout le temps"
        );
        timeFilterCombo.setValue("Tout le temps");
        timeFilterCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        timeFilterCombo.setPrefWidth(150);
        timeFilterCombo.setOnAction(e -> updateRegistrationChart());

        filterBox.getChildren().addAll(filterLabel, timeFilterCombo);
        return filterBox;
    }

    private HBox createRealStatsCards() {
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);

        int totalUsers = userService.countUsers();
        int[] roleStats = userService.getUserStatistics();
        int activeUsers = getActiveUsersThisMonth();

        // Calculer les pourcentages
        double adminPercent = totalUsers > 0 ? (roleStats[0] * 100.0 / totalUsers) : 0;
        double orgPercent = totalUsers > 0 ? (roleStats[1] * 100.0 / totalUsers) : 0;
        double partPercent = totalUsers > 0 ? (roleStats[2] * 100.0 / totalUsers) : 0;

        VBox card1 = createStatCardWithPercentage("👥 Total Utilisateurs",
                String.valueOf(totalUsers),
                "Utilisateurs inscrits",
                "#4f46e5",
                "100%");

        VBox card2 = createStatCardWithPercentage("👑 Administrateurs",
                String.valueOf(roleStats[0]),
                "Gestion système",
                "#10b981",
                String.format("%.1f%%", adminPercent));

        VBox card3 = createStatCardWithPercentage("🎯 Organisateurs",
                String.valueOf(roleStats[1]),
                "Gestion événements",
                "#3b82f6",
                String.format("%.1f%%", orgPercent));

        VBox card4 = createStatCardWithPercentage("😊 Participants",
                String.valueOf(roleStats[2]),
                String.format("%.1f%% actifs", getActiveRate()),
                "#f59e0b",
                String.format("%.1f%%", partPercent));

        statsCards.getChildren().addAll(card1, card2, card3, card4);
        return statsCards;
    }

    private VBox createStatCardWithPercentage(String title, String value, String subtitle, String color, String percentage) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(280);
        card.setPrefHeight(150);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Icône
        Circle iconCircle = new Circle(15);
        iconCircle.setFill(Color.web(color + "20"));

        Label iconLabel = new Label(title.substring(0, 2));
        iconLabel.setFont(Font.font("Arial", 12));
        iconLabel.setTextFill(Color.web(color));

        StackPane iconContainer = new StackPane(iconCircle, iconLabel);
        iconContainer.setAlignment(Pos.CENTER);

        VBox titleContent = new VBox(2);
        Label titleLabel = new Label(title.substring(2));
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#64748b"));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 10));
        subtitleLabel.setTextFill(Color.web("#94a3b8"));

        titleContent.getChildren().addAll(titleLabel, subtitleLabel);
        titleRow.getChildren().addAll(iconContainer, titleContent);

        // Pourcentage
        Label percentageLabel = new Label(percentage);
        percentageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        percentageLabel.setTextFill(Color.web(color));
        percentageLabel.setPadding(new Insets(0, 0, 5, 0));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web("#1e293b"));

        card.getChildren().addAll(titleRow, percentageLabel, valueLabel);
        return card;
    }

    private VBox createEnhancedRoleDistributionChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Distribution des Rôles");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Répartition des utilisateurs par rôle");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Container pour graphique et légende
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER);

        // Pie Chart pour les rôles
        rolePieChart = new PieChart();
        rolePieChart.setLabelsVisible(true);
        rolePieChart.setLegendVisible(false);
        rolePieChart.setPrefSize(350, 350);
        rolePieChart.setStyle("-fx-background-color: transparent;");

        updateRoleChart();

        // Légende détaillée avec pourcentages
        legendBox = createDetailedLegend();

        contentBox.getChildren().addAll(rolePieChart, legendBox);
        container.getChildren().addAll(header, contentBox);
        return container;
    }

    private void updateRoleChart() {
        int[] roleStats = userService.getUserStatistics();
        int total = roleStats[0] + roleStats[1] + roleStats[2];

        rolePieChart.getData().clear();

        if (total > 0) {
            // Ajouter les données avec pourcentages dans le label
            PieChart.Data adminSlice = new PieChart.Data(
                    String.format("Admins (%.1f%%)", (roleStats[0] * 100.0 / total)),
                    roleStats[0]
            );
            PieChart.Data orgSlice = new PieChart.Data(
                    String.format("Organisateurs (%.1f%%)", (roleStats[1] * 100.0 / total)),
                    roleStats[1]
            );
            PieChart.Data partSlice = new PieChart.Data(
                    String.format("Participants (%.1f%%)", (roleStats[2] * 100.0 / total)),
                    roleStats[2]
            );

            rolePieChart.getData().addAll(adminSlice, orgSlice, partSlice);

            // Personnaliser les couleurs
            adminSlice.getNode().setStyle("-fx-pie-color: #4f46e5;");
            orgSlice.getNode().setStyle("-fx-pie-color: #3b82f6;");
            partSlice.getNode().setStyle("-fx-pie-color: #10b981;");
        }
    }

    private VBox createDetailedLegend() {
        VBox legendBox = new VBox(15);
        legendBox.setPrefWidth(250);
        legendBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-padding: 20;");

        Label legendTitle = new Label("Détails par rôle");
        legendTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        legendTitle.setTextFill(Color.web("#1e293b"));

        int[] roleStats = userService.getUserStatistics();
        int total = roleStats[0] + roleStats[1] + roleStats[2];

        if (total > 0) {
            // Admin
            HBox adminRow = createLegendRow("Admin", roleStats[0], total, "#4f46e5");
            // Organisateur
            HBox orgRow = createLegendRow("Organisateur", roleStats[1], total, "#3b82f6");
            // Participant
            HBox partRow = createLegendRow("Participant", roleStats[2], total, "#10b981");

            // Totaux
            Separator separator = new Separator();
            separator.setPrefWidth(200);

            HBox totalRow = new HBox();
            totalRow.setAlignment(Pos.CENTER_LEFT);
            totalRow.setSpacing(10);

            Label totalLabel = new Label("Total:");
            totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            totalLabel.setTextFill(Color.web("#1e293b"));

            Label totalValue = new Label(String.valueOf(total));
            totalValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            totalValue.setTextFill(Color.web("#4f46e5"));

            totalRow.getChildren().addAll(totalLabel, totalValue);

            legendBox.getChildren().addAll(legendTitle, adminRow, orgRow, partRow, separator, totalRow);
        } else {
            Label noDataLabel = new Label("Aucune donnée disponible");
            noDataLabel.setFont(Font.font("Arial", 12));
            noDataLabel.setTextFill(Color.web("#94a3b8"));
            noDataLabel.setAlignment(Pos.CENTER);
            legendBox.getChildren().addAll(legendTitle, noDataLabel);
        }

        return legendBox;
    }

    private HBox createLegendRow(String role, int count, int total, String color) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // Point de couleur
        Circle dot = new Circle(5);
        dot.setFill(Color.web(color));

        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("Arial", 12));
        roleLabel.setTextFill(Color.web("#475569"));
        roleLabel.setPrefWidth(90);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        countLabel.setTextFill(Color.web("#1e293b"));

        double percentage = total > 0 ? (count * 100.0 / total) : 0;
        Label percentLabel = new Label(String.format("(%.1f%%)", percentage));
        percentLabel.setFont(Font.font("Arial", 10));
        percentLabel.setTextFill(Color.web("#94a3b8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(dot, roleLabel, countLabel, percentLabel, spacer);
        return row;
    }

    private VBox createEnhancedRegistrationTrendChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Tendances d'Inscription");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Évolution des inscriptions par rôle");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Line Chart pour les tendances
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Période");
        xAxis.setTickLabelFill(Color.web("#64748b"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'inscriptions");
        yAxis.setTickLabelFill(Color.web("#64748b"));

        registrationLineChart = new LineChart<>(xAxis, yAxis);
        registrationLineChart.setTitle("");
        registrationLineChart.setLegendVisible(true);
        registrationLineChart.setCreateSymbols(true);
        registrationLineChart.setPrefHeight(400);
        registrationLineChart.setStyle("-fx-background-color: transparent;");

        // Initialiser avec toutes les données
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
        orgSeries.setName("Organisateurs");

        XYChart.Series<String, Number> partSeries = new XYChart.Series<>();
        partSeries.setName("Participants");

        // Récupérer les périodes (clés du map)
        List<String> periods = new ArrayList<>(registrationData.keySet());
        Collections.sort(periods);

        for (String periodKey : periods) {
            Map<String, Integer> roleData = registrationData.get(periodKey);

            int adminCount = roleData.getOrDefault("admin", 0);
            int orgCount = roleData.getOrDefault("organisateur", 0);
            int partCount = roleData.getOrDefault("participant", 0);

            adminSeries.getData().add(new XYChart.Data<>(periodKey, adminCount));
            orgSeries.getData().add(new XYChart.Data<>(periodKey, orgCount));
            partSeries.getData().add(new XYChart.Data<>(periodKey, partCount));
        }

        registrationLineChart.getData().addAll(adminSeries, orgSeries, partSeries);

        // Personnaliser les couleurs des lignes
        for (XYChart.Series<String, Number> series : registrationLineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle(getSeriesStyle(series.getName()));
            }
        }
    }

    private Map<String, Map<String, Integer>> getRegistrationDataByPeriod(String period) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        List<User> allUsers = userService.getAllUsers();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period) {
            case "Dernier jour":
                startDate = now.minusDays(1);
                break;
            case "2 derniers jours":
                startDate = now.minusDays(2);
                break;
            case "3 derniers jours":
                startDate = now.minusDays(3);
                break;
            case "7 derniers jours":
                startDate = now.minusDays(7);
                break;
            case "Ce mois":
                startDate = now.withDayOfMonth(1);
                break;
            case "Cette année":
                startDate = now.withDayOfYear(1);
                break;
            default: // "Tout le temps"
                startDate = LocalDateTime.MIN;
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
            String periodKey = getPeriodKey(user.getCreatedAt(), period);
            String role = user.getRole().toLowerCase();

            result.putIfAbsent(periodKey, new HashMap<>());
            Map<String, Integer> roleMap = result.get(periodKey);
            roleMap.put(role, roleMap.getOrDefault(role, 0) + 1);
        }

        // S'assurer que toutes les périodes sont présentes
        ensureAllPeriods(result, period);

        return result;
    }

    private String getPeriodKey(LocalDateTime date, String period) {
        switch (period) {
            case "Dernier jour":
                return date.format(DateTimeFormatter.ofPattern("HH:00"));
            case "2 derniers jours":
            case "3 derniers jours":
                return date.format(DateTimeFormatter.ofPattern("dd/MM HH:00"));
            case "7 derniers jours":
                return date.format(DateTimeFormatter.ofPattern("E dd"));
            case "Ce mois":
                return "Semaine " + ((date.getDayOfMonth() - 1) / 7 + 1);
            case "Cette année":
                return date.format(DateTimeFormatter.ofPattern("MMM"));
            default: // "Tout le temps"
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }

    private void ensureAllPeriods(Map<String, Map<String, Integer>> data, String period) {
        // Ajouter les périodes manquantes avec des valeurs à 0
        List<String> expectedPeriods = getExpectedPeriods(period);
        for (String periodKey : expectedPeriods) {
            if (!data.containsKey(periodKey)) {
                Map<String, Integer> zeroMap = new HashMap<>();
                zeroMap.put("admin", 0);
                zeroMap.put("organisateur", 0);
                zeroMap.put("participant", 0);
                data.put(periodKey, zeroMap);
            }
        }

        // Trier par ordre chronologique
        List<String> sortedKeys = new ArrayList<>(data.keySet());
        sortedKeys.sort(Comparator.naturalOrder());

        Map<String, Map<String, Integer>> sortedData = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            sortedData.put(key, data.get(key));
        }

        data.clear();
        data.putAll(sortedData);
    }

    private List<String> getExpectedPeriods(String period) {
        List<String> periods = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        switch (period) {
            case "Dernier jour":
                for (int i = 0; i < 24; i++) {
                    periods.add(now.minusHours(23 - i).format(DateTimeFormatter.ofPattern("HH:00")));
                }
                break;
            case "2 derniers jours":
                for (int i = 0; i < 48; i++) {
                    periods.add(now.minusHours(47 - i).format(DateTimeFormatter.ofPattern("dd/MM HH:00")));
                }
                break;
            case "3 derniers jours":
                for (int i = 0; i < 72; i++) {
                    periods.add(now.minusHours(71 - i).format(DateTimeFormatter.ofPattern("dd/MM HH:00")));
                }
                break;
            case "7 derniers jours":
                for (int i = 0; i < 7; i++) {
                    periods.add(now.minusDays(6 - i).format(DateTimeFormatter.ofPattern("E dd")));
                }
                break;
            case "Ce mois":
                int weeksInMonth = 4; // Simplification
                for (int i = 1; i <= weeksInMonth; i++) {
                    periods.add("Semaine " + i);
                }
                break;
            case "Cette année":
                for (int i = 1; i <= 12; i++) {
                    periods.add(LocalDateTime.of(now.getYear(), i, 1, 0, 0)
                            .format(DateTimeFormatter.ofPattern("MMM")));
                }
                break;
            default: // "Tout le temps"
                // Pour la démonstration, utiliser les 12 derniers mois
                for (int i = 0; i < 12; i++) {
                    periods.add(now.minusMonths(11 - i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
                }
                break;
        }

        return periods;
    }

    private String getSeriesStyle(String seriesName) {
        switch (seriesName) {
            case "Admins":
                return "-fx-stroke: #4f46e5; -fx-stroke-width: 3;";
            case "Organisateurs":
                return "-fx-stroke: #3b82f6; -fx-stroke-width: 3;";
            case "Participants":
                return "-fx-stroke: #10b981; -fx-stroke-width: 3;";
            default:
                return "-fx-stroke: #64748b; -fx-stroke-width: 2;";
        }
    }

    private VBox createUserSummaryTable() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Derniers Utilisateurs Inscrits");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Les 10 derniers utilisateurs ajoutés au système");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Tableau simple
        TableView<User> summaryTable = new TableView<>();
        summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        summaryTable.setPrefHeight(250);
        summaryTable.setStyle("-fx-background-color: transparent;");

        // Colonnes
        TableColumn<User, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        nameCol.setPrefWidth(200);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
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

        TableColumn<User, String> dateCol = new TableColumn<>("Inscription");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setPrefWidth(150);

        summaryTable.getColumns().addAll(nameCol, emailCol, roleCol, dateCol);

        // Charger les 10 derniers utilisateurs (triés par date décroissante)
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

        summaryTable.setItems(FXCollections.observableArrayList(recentUsers));

        container.getChildren().addAll(header, summaryTable);
        return container;
    }

    // ============ PROFIL UTILISATEUR DANS LA ZONE PRINCIPALE ============

    private void showUserProfileInMain() {
        VBox profileView = createUserProfileView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(profileView);
        updateSidebarButton("profile");
    }

    private VBox createUserProfileView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Mon Profil");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Gérez vos informations personnelles");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Bouton pour retourner au dashboard
        Button backBtn = new Button("← Retour au dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; " +
                "-fx-font-weight: bold; -fx-border-color: transparent;");
        backBtn.setOnAction(e -> showDashboard());

        header.getChildren().addAll(headerText, backBtn);

        // Contenu du profil
        VBox profileContent = new VBox(30);
        profileContent.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // Section Avatar et Informations de base
        HBox topSection = new HBox(30);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Avatar avec option de modification
        VBox avatarBox = new VBox(20);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPrefWidth(200);

        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(70);

        // Charger l'image de profil si disponible
        ImageView avatarImageView = null;
        if (currentUser.getPhoto() != null && !currentUser.getPhoto().equals("default.jpg")) {
            try {
                Image avatarImage = new Image("file:" + currentUser.getPhoto());
                avatarImageView = new ImageView(avatarImage);
                avatarImageView.setFitWidth(140);
                avatarImageView.setFitHeight(140);
                avatarImageView.setPreserveRatio(true);
                avatarCircle.setFill(Color.TRANSPARENT);
                avatarContainer.getChildren().add(avatarImageView);
            } catch (Exception e) {
                // Si l'image ne peut pas être chargée, utiliser l'avatar par défaut
                avatarCircle.setFill(Color.web("#4f46e5"));
                String initials = getInitials(currentUser);
                Label avatarText = new Label(initials);
                avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
                avatarText.setTextFill(Color.WHITE);
                avatarContainer.getChildren().addAll(avatarCircle, avatarText);
            }
        } else {
            avatarCircle.setFill(Color.web("#4f46e5"));
            String initials = getInitials(currentUser);
            Label avatarText = new Label(initials);
            avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
            avatarText.setTextFill(Color.WHITE);
            avatarContainer.getChildren().addAll(avatarCircle, avatarText);
        }

        Button changeAvatarBtn = new Button("📷 Changer la photo");
        changeAvatarBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
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

        Label memberSinceLabel = new Label("Membre depuis: " +
                (currentUser.getCreatedAt() != null ?
                        currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) :
                        "Date inconnue"));
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

        Label formTitle = new Label("Modifier les informations");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        formTitle.setTextFill(Color.web("#1e293b"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(30);
        formGrid.setVgap(20);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        // Champs de formulaire avec données actuelles
        TextField nomField = new TextField(currentUser.getNom());
        styleFormTextField(nomField);

        TextField prenomField = new TextField(currentUser.getPrenom());
        styleFormTextField(prenomField);

        TextField emailField = new TextField(currentUser.getEmail());
        styleFormTextField(emailField);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        // Définir la valeur actuelle basée sur l'idGenre
        if (currentUser.getIdGenre() == 1) {
            genreCombo.setValue("Homme");
        } else if (currentUser.getIdGenre() == 2) {
            genreCombo.setValue("Femme");
        } else {
            genreCombo.setValue("Non spécifié");
        }
        styleFormComboBox(genreCombo);

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Mot de passe actuel (pour confirmation)");
        styleFormTextField(currentPasswordField);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe (optionnel)");
        styleFormTextField(newPasswordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le nouveau mot de passe");
        styleFormTextField(confirmPasswordField);

        // Ajout des champs au formulaire
        formGrid.add(new Label("Nom:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("Prénom:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Genre:"), 0, 3);
        formGrid.add(genreCombo, 1, 3);
        formGrid.add(new Label("Mot de passe actuel:"), 0, 4);
        formGrid.add(currentPasswordField, 1, 4);
        formGrid.add(new Label("Nouveau mot de passe:"), 0, 5);
        formGrid.add(newPasswordField, 1, 5);
        formGrid.add(new Label("Confirmation:"), 0, 6);
        formGrid.add(confirmPasswordField, 1, 6);

        formSection.getChildren().addAll(formTitle, formGrid);

        // Boutons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8;");
        cancelBtn.setOnAction(e -> showDashboard());

        Button saveBtn = new Button("💾 Enregistrer les modifications");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8;");
        saveBtn.setOnAction(e -> {
            if (saveProfileChanges(nomField, prenomField, emailField, genreCombo,
                    currentPasswordField, newPasswordField, confirmPasswordField)) {
                showAlert("Succès", "Votre profil a été mis à jour avec succès!");
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

    // ============ GESTION DES UTILISATEURS ============

    private void showUserManagementViewInCenter() {
        ScrollPane content = createEnhancedUserManagementView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(content);
        updateSidebarButton("tables");
    }

    private ScrollPane createEnhancedUserManagementView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Gestion des Utilisateurs");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Tables - Gestion des utilisateurs LOOPI");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Bouton pour retourner au dashboard
        Button backBtn = new Button("← Retour au dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; " +
                "-fx-font-weight: bold; -fx-border-color: transparent;");
        backBtn.setOnAction(e -> showDashboard());

        header.getChildren().addAll(headerText, backBtn);

        // Statistiques rapides
        HBox quickStats = createUserManagementStats();

        // Barre d'outils
        HBox toolbar = createUserManagementToolbar();

        // Tableau des utilisateurs
        VBox tableContainer = createUserTableContainer();

        container.getChildren().addAll(header, quickStats, toolbar, tableContainer);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createUserManagementStats() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        int[] stats = userService.getUserStatistics();
        int totalUsers = stats[0] + stats[1] + stats[2];
        int activeUsers = getActiveUsersThisMonth();

        VBox totalCard = createUserStatCard("👥", "Total", String.valueOf(totalUsers),
                "Utilisateurs", "#4f46e5");

        VBox adminCard = createUserStatCard("👑", "Admins", String.valueOf(stats[0]),
                "Administrateurs", "#10b981");

        VBox orgCard = createUserStatCard("🎯", "Organisateurs", String.valueOf(stats[1]),
                "Organisateurs", "#3b82f6");

        VBox partCard = createUserStatCard("😊", "Participants", String.valueOf(stats[2]),
                "Participants", "#f59e0b");

        statsBox.getChildren().addAll(totalCard, adminCard, orgCard, partCard);
        return statsBox;
    }

    private VBox createUserStatCard(String icon, String title, String value, String subtitle, String color) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(200);
        card.setPrefHeight(120);

        HBox iconRow = new HBox();
        iconRow.setAlignment(Pos.CENTER);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(40, 40);
        iconContainer.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 10;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));
        iconLabel.setTextFill(Color.web(color));
        iconContainer.getChildren().add(iconLabel);

        iconRow.getChildren().add(iconContainer);

        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.web("#1e293b"));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web(color));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 10));
        subtitleLabel.setTextFill(Color.web("#94a3b8"));

        textContent.getChildren().addAll(valueLabel, titleLabel, subtitleLabel);
        card.getChildren().addAll(iconRow, textContent);

        return card;
    }

    private HBox createUserManagementToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Bouton Ajouter
        Button addBtn = createToolbarButton("➕ Ajouter", "#10b981");
        addBtn.setOnAction(e -> showAddUserDialog());

        // Bouton Modifier
        Button editBtn = createToolbarButton("✏️ Modifier", "#3b82f6");
        editBtn.setOnAction(e -> editSelectedUser());

        // Bouton Supprimer
        Button deleteBtn = createToolbarButton("🗑️ Supprimer", "#ef4444");
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        // Bouton Exporter
        Button exportBtn = createToolbarButton("📤 Exporter", "#8b5cf6");
        exportBtn.setOnAction(e -> exportUsers());

        // Bouton Actualiser
        Button refreshBtn = createToolbarButton("🔄 Actualiser", "#64748b");
        refreshBtn.setOnAction(e -> refreshUserTable());

        // Filtre par rôle
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filtrer:");
        filterLabel.setFont(Font.font("Arial", 12));
        filterLabel.setTextFill(Color.web("#64748b"));

        currentRoleFilter = new ComboBox<>();
        currentRoleFilter.getItems().addAll("Tous les rôles", "Admin", "Organisateur", "Participant");
        currentRoleFilter.setValue("Tous les rôles");
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
        searchField.setPromptText("Rechercher un utilisateur...");
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

        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, exportBtn, refreshBtn, filterBox, searchBox);
        return toolbar;
    }

    private Button createToolbarButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-font-size: 14px;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                        "-fx-cursor: hand; -fx-font-size: 14px;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                        "-fx-cursor: hand; -fx-font-size: 14px;"
        ));

        return btn;
    }

    private VBox createUserTableContainer() {
        VBox tableContainer = new VBox(15);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(2);
        Label tableTitle = new Label("Liste des utilisateurs");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#1e293b"));

        Label tableSubtitle = new Label("Sélectionnez un utilisateur pour effectuer des actions");
        tableSubtitle.setFont(Font.font("Arial", 12));
        tableSubtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(tableTitle, tableSubtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        int userCount = userService.countUsers();
        Label userCountLabel = new Label("Total: " + userCount + " utilisateurs");
        userCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        userCountLabel.setTextFill(Color.web("#4f46e5"));
        userCountLabel.setPadding(new Insets(5, 15, 5, 15));
        userCountLabel.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 12;");

        tableHeader.getChildren().addAll(headerText, userCountLabel);

        userTable = new TableView<>();
        setupEnhancedUserTable();

        tableContainer.getChildren().addAll(tableHeader, userTable);
        return tableContainer;
    }

    @SuppressWarnings("unchecked")
    private void setupEnhancedUserTable() {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPlaceholder(new Label("Aucun utilisateur trouvé"));
        userTable.setStyle("-fx-background-color: transparent;");
        userTable.setPrefHeight(500);

        // Colonne ID
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setSortable(true);

        // Colonne Avatar
        TableColumn<User, String> avatarCol = new TableColumn<>("");
        avatarCol.setPrefWidth(50);
        avatarCol.setCellFactory(column -> new TableCell<User, String>() {
            private final StackPane avatarContainer = new StackPane();
            private final Circle avatarCircle = new Circle(15);
            private final Label avatarText = new Label();

            {
                avatarCircle.setFill(Color.web("#4f46e5"));
                avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
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
                }
            }
        });

        // Colonne Nom complet
        TableColumn<User, String> nameCol = new TableColumn<>("Nom complet");
        nameCol.setCellValueFactory(cellData -> {
            String fullName = cellData.getValue().getPrenom() + " " + cellData.getValue().getNom();
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });
        nameCol.setPrefWidth(180);

        // Colonne Email
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        // Colonne Rôle
        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
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
                        default:
                            setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 15;");
                    }
                }
            }
        });

        // Colonne Genre
        TableColumn<User, String> sexeCol = new TableColumn<>("Genre");
        sexeCol.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        sexeCol.setPrefWidth(80);
        sexeCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Date d'inscription
        TableColumn<User, String> dateCol = new TableColumn<>("Inscription");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setPrefWidth(100);
        dateCol.setStyle("-fx-alignment: CENTER;");
        dateCol.setSortable(true);

        // Colonne Statut
        TableColumn<User, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(90);
        statusCol.setCellFactory(column -> new TableCell<User, String>() {
            private final Circle statusDot = new Circle(4);
            private final Label statusText = new Label();
            private final HBox container = new HBox(5, statusDot, statusText);

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
                        statusText.setText("Actif");
                        statusText.setTextFill(Color.web("#10b981"));
                    } else {
                        statusDot.setFill(Color.web("#94a3b8"));
                        statusText.setText("Inactif");
                        statusText.setTextFill(Color.web("#94a3b8"));
                    }
                    setGraphic(container);
                }
            }
        });

        userTable.getColumns().addAll(idCol, avatarCol, nameCol, emailCol, roleCol, sexeCol, dateCol, statusCol);

        // Alternance de couleurs de ligne
        userTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f8fafc;");
                    } else {
                        setStyle("-fx-background-color: white;");
                    }

                    // Surligner la ligne au survol
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
                }
            }
        });

        refreshUserTable();
    }

    private void refreshUserTable() {
        List<User> users = userService.getAllUsers();
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);

        // Mettre à jour le compteur
        if (currentRoleFilter != null && !currentRoleFilter.getValue().equals("Tous les rôles")) {
            filterUsersByRole(currentRoleFilter.getValue());
        }
    }

    private void filterUsersByRole(String roleFilter) {
        if (roleFilter.equals("Tous les rôles")) {
            refreshUserTable();
            return;
        }

        String role = roleFilter.toLowerCase();
        List<User> filteredUsers = userService.getUsersByRole(role);
        userList = FXCollections.observableArrayList(filteredUsers);
        userTable.setItems(userList);

        // Mettre à jour le placeholder si vide
        if (filteredUsers.isEmpty()) {
            userTable.setPlaceholder(new Label("Aucun utilisateur trouvé pour le rôle: " + roleFilter));
        }
    }

    private void searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (currentRoleFilter != null && !currentRoleFilter.getValue().equals("Tous les rôles")) {
                filterUsersByRole(currentRoleFilter.getValue());
            } else {
                refreshUserTable();
            }
            return;
        }

        List<User> users = userService.searchUsers(keyword);
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);

        if (users.isEmpty()) {
            userTable.setPlaceholder(new Label("Aucun utilisateur trouvé pour: \"" + keyword + "\""));
        }
    }

    // ============ FONCTIONNALITÉS DES BOUTONS ============

    private void showAddUserDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Ajouter un nouvel utilisateur");
        dialogStage.initOwner(primaryStage);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: white;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Ajouter un nouvel utilisateur");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Remplissez les informations du nouvel utilisateur");
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
        nomField.setPromptText("Nom");
        styleFormTextField(nomField);

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        styleFormTextField(prenomField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe (min. 8 caractères)");
        styleFormTextField(passwordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");
        styleFormTextField(confirmPasswordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue("participant");
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue("Non spécifié");
        styleFormComboBox(genreCombo);

        // Ajout des labels et champs au grid
        formGrid.add(new Label("Nom *:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("Prénom *:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email *:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Mot de passe *:"), 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(new Label("Confirmation *:"), 0, 4);
        formGrid.add(confirmPasswordField, 1, 4);
        formGrid.add(new Label("Rôle *:"), 0, 5);
        formGrid.add(roleCombo, 1, 5);
        formGrid.add(new Label("Genre:"), 0, 6);
        formGrid.add(genreCombo, 1, 6);

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button addBtn = new Button("Ajouter l'utilisateur");
        addBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        addBtn.setOnAction(e -> {
            // Validation
            if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                    emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires (*)");
                return;
            }

            if (!isValidEmail(emailField.getText())) {
                showAlert("Erreur", "Veuillez entrer une adresse email valide");
                return;
            }

            if (passwordField.getText().length() < 8) {
                showAlert("Erreur", "Le mot de passe doit contenir au moins 8 caractères");
                return;
            }

            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas");
                return;
            }

            if (userService.emailExists(emailField.getText())) {
                showAlert("Erreur", "Cet email est déjà utilisé par un autre utilisateur");
                return;
            }

            // Création de l'utilisateur
            User newUser = new User();
            newUser.setNom(nomField.getText());
            newUser.setPrenom(prenomField.getText());
            newUser.setEmail(emailField.getText());
            newUser.setPassword(passwordField.getText());
            newUser.setRole(roleCombo.getValue());
            newUser.setPhoto("default.jpg");

            // Définir l'idGenre en fonction du choix
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
                showAlert("Succès", "Utilisateur ajouté avec succès!");
                refreshUserTable();
                dialogStage.close();
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout de l'utilisateur");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, addBtn);
        mainLayout.getChildren().addAll(header, formGrid, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void editSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur à modifier");
            return;
        }

        // Empêcher la modification de son propre compte (sauf pour admin)
        if (selectedUser.getId() == currentUser.getId() && !currentUser.isAdmin()) {
            showAlert("Avertissement", "Vous ne pouvez pas modifier votre propre compte depuis cette interface");
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Modifier l'utilisateur");
        dialogStage.initOwner(primaryStage);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: white;");

        // En-tête avec avatar
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(30);
        avatarCircle.setFill(Color.web("#4f46e5"));

        String initials = getInitials(selectedUser);
        Label avatarText = new Label(initials);
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        avatarText.setTextFill(Color.WHITE);

        avatarContainer.getChildren().addAll(avatarCircle, avatarText);

        VBox headerText = new VBox(5);
        Label title = new Label("Modifier l'utilisateur");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label(selectedUser.getNomComplet());
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

        TextField nomField = new TextField(selectedUser.getNom());
        styleFormTextField(nomField);

        TextField prenomField = new TextField(selectedUser.getPrenom());
        styleFormTextField(prenomField);

        TextField emailField = new TextField(selectedUser.getEmail());
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe (laisser vide pour garder)");
        styleFormTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue(selectedUser.getRole());
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        // Définir la valeur basée sur l'idGenre
        if (selectedUser.getIdGenre() == 1) {
            genreCombo.setValue("Homme");
        } else if (selectedUser.getIdGenre() == 2) {
            genreCombo.setValue("Femme");
        } else {
            genreCombo.setValue("Non spécifié");
        }
        styleFormComboBox(genreCombo);

        formGrid.add(new Label("Nom *:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("Prénom *:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email *:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Nouveau mot de passe:"), 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(new Label("Rôle *:"), 0, 4);
        formGrid.add(roleCombo, 1, 4);
        formGrid.add(new Label("Genre:"), 0, 5);
        formGrid.add(genreCombo, 1, 5);

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button saveBtn = new Button("Enregistrer les modifications");
        saveBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        saveBtn.setOnAction(e -> {
            // Validation
            if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                    emailField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires (*)");
                return;
            }

            if (!isValidEmail(emailField.getText())) {
                showAlert("Erreur", "Veuillez entrer une adresse email valide");
                return;
            }

            // Vérifier si l'email a changé et s'il existe déjà
            if (!emailField.getText().equals(selectedUser.getEmail()) &&
                    userService.emailExists(emailField.getText())) {
                showAlert("Erreur", "Cet email est déjà utilisé par un autre utilisateur");
                return;
            }

            // Mettre à jour les informations
            selectedUser.setNom(nomField.getText());
            selectedUser.setPrenom(prenomField.getText());
            selectedUser.setEmail(emailField.getText());
            selectedUser.setRole(roleCombo.getValue());

            // Mettre à jour le mot de passe si fourni
            if (!passwordField.getText().isEmpty()) {
                if (passwordField.getText().length() < 8) {
                    showAlert("Erreur", "Le mot de passe doit contenir au moins 8 caractères");
                    return;
                }
                selectedUser.setPassword(passwordField.getText());
            }

            // Mettre à jour l'idGenre en fonction du choix
            String genre = genreCombo.getValue();
            if ("Homme".equals(genre)) {
                selectedUser.setIdGenre(1);
            } else if ("Femme".equals(genre)) {
                selectedUser.setIdGenre(2);
            } else {
                selectedUser.setIdGenre(3);
            }

            // Mettre à jour dans la base de données
            if (userService.updateUser(selectedUser)) {
                showAlert("Succès", "Utilisateur modifié avec succès!");
                refreshUserTable();
                dialogStage.close();
            } else {
                showAlert("Erreur", "Erreur lors de la modification de l'utilisateur");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        mainLayout.getChildren().addAll(header, formGrid, buttonBox);

        Scene scene = new Scene(mainLayout, 500, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void deleteSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur à supprimer");
            return;
        }

        // Empêcher la suppression de son propre compte
        if (selectedUser.getId() == currentUser.getId()) {
            showAlert("Erreur", "Vous ne pouvez pas supprimer votre propre compte!");
            return;
        }

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement l'utilisateur :\n\n" +
                "Nom: " + selectedUser.getNomComplet() + "\n" +
                "Email: " + selectedUser.getEmail() + "\n" +
                "Rôle: " + selectedUser.getRole() + "\n\n" +
                "Cette action est irréversible.");
        confirmAlert.initOwner(primaryStage);

        // Personnaliser les boutons
        ButtonType yesButton = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Non, annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            if (userService.deleteUser(selectedUser.getId())) {
                showAlert("Succès", "Utilisateur supprimé avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de la suppression de l'utilisateur");
            }
        }
    }

    private void exportUsers() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les utilisateurs");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );

        fileChooser.setInitialFileName("utilisateurs_loopi_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                List<User> users = userTable.getItems();
                exportToCSV(users, file);

                showAlert("Export réussi",
                        users.size() + " utilisateurs exportés avec succès vers :\n" +
                                file.getAbsolutePath());

            } catch (Exception e) {
                showAlert("Erreur d'export", "Erreur lors de l'export : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToCSV(List<User> users, File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            // En-tête
            writer.write("ID;Nom;Prénom;Email;Rôle;Genre;Date d'inscription;Statut\n");

            // Données
            for (User user : users) {
                String line = String.format("%d;%s;%s;%s;%s;%s;%s;%s\n",
                        user.getId(),
                        user.getNom(),
                        user.getPrenom(),
                        user.getEmail(),
                        user.getRole(),
                        user.getSexe() != null ? user.getSexe() : "",
                        user.getCreatedAt() != null ?
                                user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "",
                        "Actif" // Statut simulé
                );
                writer.write(line);
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

    private void performGlobalSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        showUserManagementViewInCenter();
        searchUsers(keyword);
        showAlert("Recherche", "Résultats pour: " + keyword);
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
            System.out.println("Erreur lors du retour au login: " + e.getMessage());
        }
    }

    private void showDashboard() {
        ScrollPane content = createDashboardView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(content);
        updateSidebarButton("dashboard");
    }

    private void toggleSidebar() {
        VBox sidebar = (VBox) root.getLeft();
        if (sidebar.getPrefWidth() == 250) {
            sidebar.setPrefWidth(80);
            // Cacher les textes longs, ne montrer que les icônes
            for (var node : sidebar.getChildren()) {
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    for (var child : vbox.getChildren()) {
                        if (child instanceof Button) {
                            Button btn = (Button) child;
                            String text = btn.getText();
                            // Garder seulement l'emoji
                            if (text.length() > 2) {
                                btn.setText(text.substring(0, 2));
                            }
                        }
                    }
                }
            }
        } else {
            sidebar.setPrefWidth(250);
            // Restaurer les textes complets
            updateSidebarButton("dashboard");
        }
    }

    private void updateSidebarButton(String activeButton) {
        VBox sidebar = (VBox) root.getLeft();
        VBox navSection = (VBox) sidebar.getChildren().get(0);

        // Réinitialiser tous les boutons
        for (int i = 1; i < navSection.getChildren().size(); i++) {
            if (navSection.getChildren().get(i) instanceof Button) {
                Button btn = (Button) navSection.getChildren().get(i);
                String text = btn.getText();
                // Restaurer le texte complet
                switch (text) {
                    case "📊": btn.setText("📊 Tableau de bord"); break;
                    case "👤": btn.setText("👤 Mon Profil"); break;
                    case "📋": btn.setText("📋 Gestion Utilisateurs"); break;
                    case "🆘": btn.setText("🆘 Support"); break;
                    case "🚪": btn.setText("🚪 Déconnexion"); break;
                }

                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        }

        // Activer le bon bouton
        Button activeBtn = null;
        switch (activeButton) {
            case "dashboard":
                if (navSection.getChildren().size() > 1) {
                    activeBtn = (Button) navSection.getChildren().get(1);
                }
                break;
            case "tables":
                if (navSection.getChildren().size() > 3) {
                    activeBtn = (Button) navSection.getChildren().get(3);
                }
                break;
            case "profile":
                if (navSection.getChildren().size() > 2) {
                    activeBtn = (Button) navSection.getChildren().get(2);
                }
                break;
        }

        if (activeBtn != null) {
            activeBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                    "-fx-font-weight: bold; -fx-border-color: #c7d2fe; " +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 0;");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private String darkenColor(String hex) {
        try {
            if (hex.startsWith("#") && hex.length() == 7) {
                int r = Integer.parseInt(hex.substring(1, 3), 16);
                int g = Integer.parseInt(hex.substring(3, 5), 16);
                int b = Integer.parseInt(hex.substring(5, 7), 16);

                r = Math.max(0, r - 30);
                g = Math.max(0, g - 30);
                b = Math.max(0, b - 30);

                return String.format("#%02x%02x%02x", r, g, b);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'assombrissement de la couleur: " + e.getMessage());
        }
        return hex;
    }

    // ============ MÉTHODES DE GESTION DU PROFIL ============

    private boolean saveProfileChanges(TextField nomField, TextField prenomField, TextField emailField,
                                       ComboBox<String> genreCombo, PasswordField currentPasswordField,
                                       PasswordField newPasswordField, PasswordField confirmPasswordField) {
        // Validation des champs obligatoires
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Erreur", "Les champs Nom, Prénom et Email sont obligatoires");
            return false;
        }

        // Validation de l'email
        if (!isValidEmail(emailField.getText())) {
            showAlert("Erreur", "Veuillez entrer une adresse email valide");
            return false;
        }

        // Vérifier si l'email a changé
        if (!emailField.getText().equals(currentUser.getEmail())) {
            if (userService.emailExists(emailField.getText())) {
                showAlert("Erreur", "Cet email est déjà utilisé par un autre utilisateur");
                return false;
            }
        }

        // Vérification du mot de passe actuel si changement de mot de passe demandé
        if (!newPasswordField.getText().isEmpty()) {
            if (currentPasswordField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez entrer votre mot de passe actuel pour changer de mot de passe");
                return false;
            }

            if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
                showAlert("Erreur", "Le mot de passe actuel est incorrect");
                return false;
            }

            if (newPasswordField.getText().length() < 8) {
                showAlert("Erreur", "Le nouveau mot de passe doit contenir au moins 8 caractères");
                return false;
            }

            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Erreur", "Les nouveaux mots de passe ne correspondent pas");
                return false;
            }
        }

        // Mettre à jour l'utilisateur
        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setEmail(emailField.getText());

        // Mettre à jour l'idGenre en fonction du choix
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
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour du profil");
            return false;
        }
    }

    private void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
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
                    showAlert("Succès", "Photo de profil mise à jour avec succès!");
                    // Rafraîchir l'affichage
                    showUserProfileInMain();
                } else {
                    showAlert("Erreur", "Erreur lors de la mise à jour de la photo");
                }

            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
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

    // ============ MÉTHODES DE STATISTIQUES ============

    private double getActiveRate() {
        // Simuler un taux d'activité
        return 78.3;
    }

    private int getActiveUsersThisMonth() {
        // Simuler 75% des utilisateurs actifs ce mois
        return (int)(userService.countUsers() * 0.75);
    }
}