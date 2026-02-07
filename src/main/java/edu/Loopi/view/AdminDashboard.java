package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
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
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class AdminDashboard {
    private User currentUser;
    private UserService userService;
    private Stage primaryStage;
    private BorderPane root;

    // Tableau des utilisateurs
    private TableView<User> userTable;
    private ObservableList<User> userList;

    // Données de démonstration pour les graphiques
    private double[] monthlyRevenue = {45000, 52000, 48000, 61000, 72000, 68000, 85000, 79000, 92000, 88000, 95000, 105000};
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private String[] categories = {"Mobilier", "Décorations", "Accessoires", "Jouets", "Vêtements"};
    private double[] categorySales = {45000, 32000, 28000, 19000, 15000};
    private String[] topProducts = {"Table en palette", "Lampes bouteilles", "Sac pneus", "Jouets bois", "Veste recyclée"};
    private int[] productSales = {245, 198, 156, 132, 108};
    private String[] regions = {"Tunis", "Sousse", "Sfax", "Bizerte", "Nabeul"};
    private int[] regionUsers = {850, 620, 480, 320, 280};

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        SessionManager.login(user);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("LOOPI - Tableau de Bord Administrateur");

        // Layout principal
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        HBox header = createModernHeader();
        root.setTop(header);

        // Sidebar
        VBox sidebar = createModernSidebar();
        root.setLeft(sidebar);

        // Contenu par défaut (dashboard)
        ScrollPane dashboardContent = createDashboardView();
        root.setCenter(dashboardContent);

        Scene scene = new Scene(root, 1400, 800);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS non chargé, utilisation des styles inline");
        }

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

        // Icône de menu
        Button menuToggle = new Button("☰");
        menuToggle.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: #4f46e5;");

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("LOOPI ADMIN");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        mainTitle.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Tableau de bord analytique");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setTextFill(Color.web("#64748b"));

        titleBox.getChildren().addAll(mainTitle, subtitle);

        logoBox.getChildren().addAll(menuToggle, titleBox);

        // Barre de recherche et infos utilisateur
        HBox rightSection = new HBox(20);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        // Barre de recherche
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20; -fx-padding: 5 15;");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 14px;");
        searchField.setPrefWidth(200);

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b;");

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Notifications avec badge
        StackPane notificationsContainer = new StackPane();
        Button notificationsBtn = new Button("🔔");
        notificationsBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: #64748b;");
        notificationsBtn.setTooltip(new Tooltip("Notifications"));

        // Badge de notification
        StackPane notificationBadge = new StackPane();
        notificationBadge.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 10;");
        notificationBadge.setPrefSize(20, 20);
        notificationBadge.setTranslateX(8);
        notificationBadge.setTranslateY(-8);

        Label badgeText = new Label("3");
        badgeText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        badgeText.setTextFill(Color.WHITE);

        notificationBadge.getChildren().add(badgeText);
        notificationBadge.setVisible(true);

        notificationsContainer.getChildren().addAll(notificationsBtn, notificationBadge);

        // Profil utilisateur
        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_RIGHT);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label userName = new Label(currentUser.getNomComplet());
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        userName.setTextFill(Color.web("#1e293b"));

        Label userRole = new Label(currentUser.getRole().toUpperCase());
        userRole.setFont(Font.font("Arial", 11));
        userRole.setTextFill(Color.web("#64748b"));

        userInfo.getChildren().addAll(userName, userRole);

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(40, 40);
        avatar.setStyle("-fx-background-color: #4f46e5; -fx-background-radius: 20; -fx-cursor: hand;");

        Label avatarText = new Label(String.valueOf(currentUser.getPrenom().charAt(0)));
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        avatarText.setTextFill(Color.WHITE);

        avatar.getChildren().add(avatarText);

        userProfile.getChildren().addAll(userInfo, avatar);

        HBox.setHgrow(rightSection, Priority.ALWAYS);
        rightSection.getChildren().addAll(searchBox, notificationsContainer, userProfile);

        header.getChildren().addAll(logoBox, rightSection);

        return header;
    }

    private VBox createModernSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(30, 0, 0, 0));

        // Navigation principale
        VBox navSection = new VBox(5);
        navSection.setPadding(new Insets(0, 20, 30, 20));

        Label navTitle = new Label("NAVIGATION");
        navTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        navTitle.setTextFill(Color.web("#94a3b8"));
        navTitle.setPadding(new Insets(0, 0, 10, 0));

        // Boutons de navigation
        Button dashboardBtn = createNavButton("📊 Dashboard", true);
        dashboardBtn.setOnAction(e -> showDashboard());

        Button usersBtn = createNavButton("👥 Utilisateurs", false);
        usersBtn.setOnAction(e -> showUserManagementViewInCenter());

        Button productsBtn = createNavButton("🛒 Produits", false);
        productsBtn.setOnAction(e -> showProducts());

        Button ordersBtn = createNavButton("📦 Commandes", false);
        ordersBtn.setOnAction(e -> showOrders());

        Button eventsBtn = createNavButton("📅 Événements", false);
        eventsBtn.setOnAction(e -> showEvents());

        Button donationsBtn = createNavButton("💰 Dons", false);
        donationsBtn.setOnAction(e -> showDonations());

        Button analyticsBtn = createNavButton("📈 Analytics", false);
        analyticsBtn.setOnAction(e -> showAnalytics());

        navSection.getChildren().addAll(navTitle, dashboardBtn, usersBtn, productsBtn, ordersBtn, eventsBtn, donationsBtn, analyticsBtn);

        // Section paramètres
        VBox settingsSection = new VBox(5);
        settingsSection.setPadding(new Insets(20, 20, 0, 20));

        Label settingsTitle = new Label("PARAMÈTRES");
        settingsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        settingsTitle.setTextFill(Color.web("#94a3b8"));
        settingsTitle.setPadding(new Insets(0, 0, 10, 0));

        Button settingsBtn = createNavButton("⚙️ Paramètres", false);
        settingsBtn.setOnAction(e -> showSettings());

        Button profileBtn = createNavButton("👤 Mon Profil", false);
        profileBtn.setOnAction(e -> showProfile());

        Button logoutBtn = createNavButton("🚪 Déconnexion", false);
        logoutBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-border-color: #fecaca;");
        logoutBtn.setOnAction(e -> logout());

        settingsSection.getChildren().addAll(settingsTitle, settingsBtn, profileBtn, logoutBtn);

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

        Label date = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.setFont(Font.font("Arial", 10));
        date.setTextFill(Color.web("#94a3b8"));

        footer.getChildren().addAll(version, date);

        sidebar.getChildren().addAll(navSection, spacer, settingsSection, footer);

        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(240);
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 15));

        if (active) {
            btn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #c7d2fe; " +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 0;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                    "-fx-font-size: 14px; -fx-border-color: transparent;");
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
        container.setStyle("-fx-background-color: #f8fafc;");

        // En-tête dashboard
        HBox dashboardHeader = createDashboardHeader();

        // Cartes de statistiques principales
        HBox statsCards = createStatsCards();

        // Première ligne de graphiques
        HBox chartRow1 = createChartRow1();

        // Deuxième ligne de graphiques
        HBox chartRow2 = createChartRow2();

        // Troisième ligne de graphiques
        HBox chartRow3 = createChartRow3();

        // Tableau des dernières activités
        VBox activityTable = createActivityTable();

        container.getChildren().addAll(dashboardHeader, statsCards, chartRow1, chartRow2, chartRow3, activityTable);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private HBox createDashboardHeader() {
        HBox dashboardHeader = new HBox();
        dashboardHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Tableau de bord analytique");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Aperçu des performances et statistiques globales");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);

        HBox.setHgrow(headerText, Priority.ALWAYS);
        dashboardHeader.getChildren().add(headerText);

        // Filtres date
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_RIGHT);

        ComboBox<String> periodFilter = new ComboBox<>();
        periodFilter.getItems().addAll("Aujourd'hui", "Cette semaine", "Ce mois", "Cette année", "Personnalisé");
        periodFilter.setValue("Ce mois");
        periodFilter.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16;");
        refreshBtn.setOnAction(e -> refreshDashboard());

        filters.getChildren().addAll(periodFilter, refreshBtn);

        HBox.setHgrow(filters, Priority.ALWAYS);
        dashboardHeader.getChildren().add(filters);

        return dashboardHeader;
    }

    private HBox createStatsCards() {
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);

        VBox revenueCard = createStatCard("💰", "Revenu total", "€105,248", "+12.5%", "#10b981", "#ecfdf5");
        VBox usersCard = createStatCard("👥", "Utilisateurs totaux", "2,845", "+8.2%", "#3b82f6", "#eff6ff");
        VBox ordersCard = createStatCard("📦", "Commandes", "1,247", "+5.7%", "#8b5cf6", "#f5f3ff");
        VBox conversionCard = createStatCard("📊", "Taux conversion", "3.2%", "-0.5%", "#f59e0b", "#fffbeb");

        statsCards.getChildren().addAll(revenueCard, usersCard, ordersCard, conversionCard);
        return statsCards;
    }

    private VBox createStatCard(String icon, String title, String value, String change, String color, String bgColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setPrefWidth(240);

        HBox iconRow = new HBox();
        iconRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(40, 40);
        iconContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 18));

        iconContainer.getChildren().add(iconLabel);

        HBox.setHgrow(iconRow, Priority.ALWAYS);
        iconRow.getChildren().add(iconContainer);

        VBox content = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.web("#64748b"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web("#1e293b"));

        HBox changeRow = new HBox(5);
        changeRow.setAlignment(Pos.CENTER_LEFT);

        Label changeLabel = new Label(change);
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        if (change.startsWith("+")) {
            changeLabel.setTextFill(Color.web("#10b981"));
        } else {
            changeLabel.setTextFill(Color.web("#ef4444"));
        }

        Label periodLabel = new Label("vs mois dernier");
        periodLabel.setFont(Font.font("Arial", 10));
        periodLabel.setTextFill(Color.web("#94a3b8"));

        changeRow.getChildren().addAll(changeLabel, periodLabel);

        content.getChildren().addAll(titleLabel, valueLabel, changeRow);

        card.getChildren().addAll(iconRow, content);

        return card;
    }

    private HBox createChartRow1() {
        HBox chartRow1 = new HBox(20);
        chartRow1.setAlignment(Pos.CENTER);

        // Graphique de revenu
        VBox revenueChartContainer = new VBox(10);
        revenueChartContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        revenueChartContainer.setPrefWidth(500);

        HBox revenueHeader = new HBox();
        revenueHeader.setAlignment(Pos.CENTER_LEFT);

        VBox revenueText = new VBox(2);
        Label revenueTitle = new Label("Revenu mensuel");
        revenueTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        revenueTitle.setTextFill(Color.web("#1e293b"));

        Label revenueSubtitle = new Label("Tendance des revenus sur 12 mois");
        revenueSubtitle.setFont(Font.font("Arial", 12));
        revenueSubtitle.setTextFill(Color.web("#64748b"));

        revenueText.getChildren().addAll(revenueTitle, revenueSubtitle);
        HBox.setHgrow(revenueText, Priority.ALWAYS);

        Label revenueValue = new Label("€105,248");
        revenueValue.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        revenueValue.setTextFill(Color.web("#10b981"));

        revenueHeader.getChildren().addAll(revenueText, revenueValue);

        // Line Chart
        NumberAxis xAxis = new NumberAxis(1, 12, 1);
        NumberAxis yAxis = new NumberAxis(0, 120000, 20000);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "€", ""));

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(true);
        lineChart.setPrefHeight(250);
        lineChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < months.length; i++) {
            series.getData().add(new XYChart.Data<>(i+1, monthlyRevenue[i]));
        }
        lineChart.getData().add(series);

        revenueChartContainer.getChildren().addAll(revenueHeader, lineChart);

        // Pie Chart
        VBox pieChartContainer = new VBox(10);
        pieChartContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        pieChartContainer.setPrefWidth(500);

        Label pieTitle = new Label("Ventes par catégorie");
        pieTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        pieTitle.setTextFill(Color.web("#1e293b"));

        PieChart pieChart = new PieChart();
        for (int i = 0; i < categories.length; i++) {
            PieChart.Data slice = new PieChart.Data(categories[i] + " (" + (int)categorySales[i]/1000 + "K)", categorySales[i]);
            pieChart.getData().add(slice);
        }
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setPrefHeight(250);

        pieChartContainer.getChildren().addAll(pieTitle, pieChart);

        chartRow1.getChildren().addAll(revenueChartContainer, pieChartContainer);
        return chartRow1;
    }

    private HBox createChartRow2() {
        HBox chartRow2 = new HBox(20);
        chartRow2.setAlignment(Pos.CENTER);

        // Bar Chart
        VBox barChartContainer = new VBox(10);
        barChartContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        barChartContainer.setPrefWidth(500);

        Label barTitle = new Label("Top produits vendus");
        barTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        barTitle.setTextFill(Color.web("#1e293b"));

        CategoryAxis xBarAxis = new CategoryAxis();
        NumberAxis yBarAxis = new NumberAxis(0, 300, 50);

        BarChart<String, Number> barChart = new BarChart<>(xBarAxis, yBarAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(20);
        barChart.setPrefHeight(250);
        barChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        for (int i = 0; i < topProducts.length; i++) {
            barSeries.getData().add(new XYChart.Data<>(topProducts[i], productSales[i]));
        }
        barChart.getData().add(barSeries);

        // La méthode setCreateSymbols n'existe pas pour BarChart, on la supprime
        // barChart.setCreateSymbols(false); // Cette ligne a été supprimée

        barChartContainer.getChildren().addAll(barTitle, barChart);

        // Performance Card
        VBox performanceCard = new VBox(15);
        performanceCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        performanceCard.setPrefWidth(500);

        Label perfTitle = new Label("Performance hebdomadaire");
        perfTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        perfTitle.setTextFill(Color.web("#1e293b"));

        // KPI Items
        VBox kpis = new VBox(15);
        kpis.getChildren().addAll(
                createKPIItem("Taux de satisfaction", "92%", "#10b981"),
                createKPIItem("Temps réponse moyen", "2.4h", "#3b82f6"),
                createKPIItem("Taux de rétention", "78%", "#8b5cf6"),
                createKPIItem("Panier moyen", "€84.50", "#f59e0b")
        );

        // Area Chart
        CategoryAxis xAreaAxis = new CategoryAxis();
        xAreaAxis.getCategories().addAll("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim");
        NumberAxis yAreaAxis = new NumberAxis(0, 100, 20);

        AreaChart<String, Number> areaChart = new AreaChart<>(xAreaAxis, yAreaAxis);
        areaChart.setTitle("");
        areaChart.setLegendVisible(false);
        areaChart.setCreateSymbols(false);
        areaChart.setPrefHeight(150);
        areaChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> areaSeries = new XYChart.Series<>();
        areaSeries.getData().addAll(
                new XYChart.Data<>("Lun", 45),
                new XYChart.Data<>("Mar", 52),
                new XYChart.Data<>("Mer", 48),
                new XYChart.Data<>("Jeu", 61),
                new XYChart.Data<>("Ven", 72),
                new XYChart.Data<>("Sam", 68),
                new XYChart.Data<>("Dim", 85)
        );
        areaChart.getData().add(areaSeries);

        performanceCard.getChildren().addAll(perfTitle, kpis, areaChart);

        chartRow2.getChildren().addAll(barChartContainer, performanceCard);
        return chartRow2;
    }

    private HBox createKPIItem(String label, String value, String color) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setSpacing(10);

        StackPane indicator = new StackPane();
        indicator.setPrefSize(8, 8);
        indicator.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        Label labelText = new Label(label);
        labelText.setFont(Font.font("Arial", 12));
        labelText.setTextFill(Color.web("#64748b"));

        HBox.setHgrow(labelText, Priority.ALWAYS);

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        valueText.setTextFill(Color.web("#1e293b"));

        item.getChildren().addAll(indicator, labelText, valueText);

        return item;
    }

    private HBox createChartRow3() {
        HBox chartRow3 = new HBox(20);
        chartRow3.setAlignment(Pos.CENTER);

        // Stacked Bar Chart
        VBox stackedBarContainer = new VBox(10);
        stackedBarContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        stackedBarContainer.setPrefWidth(500);

        Label stackedTitle = new Label("Utilisateurs par région");
        stackedTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        stackedTitle.setTextFill(Color.web("#1e293b"));

        CategoryAxis xRegionAxis = new CategoryAxis();
        xRegionAxis.setLabel("Régions");
        NumberAxis yRegionAxis = new NumberAxis(0, 1000, 200);

        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xRegionAxis, yRegionAxis);
        stackedBarChart.setTitle("");
        stackedBarChart.setLegendVisible(false);
        stackedBarChart.setPrefHeight(250);
        stackedBarChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> regionSeries = new XYChart.Series<>();
        for (int i = 0; i < regions.length; i++) {
            regionSeries.getData().add(new XYChart.Data<>(regions[i], regionUsers[i]));
        }
        stackedBarChart.getData().add(regionSeries);

        stackedBarContainer.getChildren().addAll(stackedTitle, stackedBarChart);

        // Funnel Chart
        VBox funnelContainer = new VBox(10);
        funnelContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        funnelContainer.setPrefWidth(500);

        Label funnelTitle = new Label("Tunnel de conversion");
        funnelTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        funnelTitle.setTextFill(Color.web("#1e293b"));

        VBox funnelVisual = new VBox(10);
        funnelVisual.setAlignment(Pos.CENTER);
        funnelVisual.setPadding(new Insets(20));

        String[] funnelSteps = {"Visiteurs (10,000)", "Inscrits (1,500)", "Première commande (450)", "Clients fidèles (180)"};
        int[] funnelValues = {10000, 1500, 450, 180};
        String[] funnelColors = {"#4f46e5", "#3b82f6", "#10b981", "#059669"};

        for (int i = 0; i < funnelSteps.length; i++) {
            HBox step = new HBox(15);
            step.setAlignment(Pos.CENTER_LEFT);
            step.setPadding(new Insets(5, 0, 5, 0));

            // Barre horizontale
            StackPane barContainer = new StackPane();
            barContainer.setPrefHeight(30);
            barContainer.setPrefWidth(400 * (funnelValues[i] / 10000.0));
            barContainer.setStyle("-fx-background-color: " + funnelColors[i] + "; -fx-background-radius: 5;");

            Label stepLabel = new Label(funnelSteps[i]);
            stepLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            stepLabel.setTextFill(Color.web("#1e293b"));
            stepLabel.setPadding(new Insets(0, 0, 0, 10));

            StackPane.setAlignment(stepLabel, Pos.CENTER_LEFT);
            barContainer.getChildren().add(stepLabel);

            step.getChildren().add(barContainer);
            funnelVisual.getChildren().add(step);
        }

        funnelContainer.getChildren().addAll(funnelTitle, funnelVisual);

        chartRow3.getChildren().addAll(stackedBarContainer, funnelContainer);
        return chartRow3;
    }

    private VBox createActivityTable() {
        VBox activityTable = new VBox(15);
        activityTable.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        activityTable.setPrefWidth(1020);

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label activityTitle = new Label("Activités récentes");
        activityTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        activityTitle.setTextFill(Color.web("#1e293b"));

        HBox.setHgrow(tableHeader, Priority.ALWAYS);
        tableHeader.getChildren().add(activityTitle);

        @SuppressWarnings("unchecked")
        TableView<String[]> activityTableView = new TableView<>();
        activityTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activityTableView.setPrefHeight(200);
        activityTableView.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        // Colonnes
        TableColumn<String[], String> userCol = new TableColumn<>("Utilisateur");
        userCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        userCol.setPrefWidth(150);

        TableColumn<String[], String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        actionCol.setPrefWidth(300);

        TableColumn<String[], String> timeCol = new TableColumn<>("Heure");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        timeCol.setPrefWidth(100);

        TableColumn<String[], String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        statusCol.setPrefWidth(100);

        activityTableView.getColumns().addAll(userCol, actionCol, timeCol, statusCol);

        ObservableList<String[]> activityData = FXCollections.observableArrayList(
                new String[]{"Marie Dupont", "A ajouté un nouveau produit", "Il y a 5 min", "✅"},
                new String[]{"Admin System", "A modifié les paramètres système", "Il y a 15 min", "⚠️"},
                new String[]{"Pierre Martin", "A passé une commande de €245", "Il y a 30 min", "✅"},
                new String[]{"Organisateur Eco", "A créé un nouvel événement", "Il y a 1h", "✅"},
                new String[]{"Ben Ali", "A fait un don de €100", "Il y a 2h", "✅"}
        );

        activityTableView.setItems(activityData);

        activityTable.getChildren().addAll(tableHeader, activityTableView);
        return activityTable;
    }

    // NOUVELLE MÉTHODE pour afficher la vue gestion utilisateurs
    private void showUserManagementViewInCenter() {
        ScrollPane content = createEnhancedUserManagementView();
        root.setCenter(content);
        updateSidebarButton("users");
    }

    private ScrollPane createEnhancedUserManagementView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        // Titre
        Label title = new Label("Gestion des Utilisateurs");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        // Statistiques rapides améliorées
        HBox statsBox = createEnhancedQuickStats();

        // Barre d'outils améliorée
        HBox toolbar = createEnhancedToolbar();

        // Tableau des utilisateurs
        VBox tableContainer = new VBox(15);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3);");

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Liste des utilisateurs");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#1e293b"));

        HBox.setHgrow(tableHeader, Priority.ALWAYS);
        tableHeader.getChildren().add(tableTitle);

        int userCount = userService.countUsers();
        Label userCountLabel = new Label("Total: " + userCount + " utilisateurs");
        userCountLabel.setFont(Font.font("Arial", 12));
        userCountLabel.setTextFill(Color.web("#64748b"));

        tableHeader.getChildren().add(userCountLabel);

        userTable = new TableView<>();
        setupEnhancedUserTable();

        tableContainer.getChildren().addAll(tableHeader, userTable);

        container.getChildren().addAll(title, statsBox, toolbar, tableContainer);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private HBox createEnhancedQuickStats() {
        HBox statsBox = new HBox(20);

        int[] stats = userService.getUserStatistics();
        int totalUsers = stats[0] + stats[1] + stats[2];
        Random rand = new Random();

        VBox totalBox = createEnhancedStatCard("👥", "Total Utilisateurs", String.valueOf(totalUsers),
                "+" + (5 + rand.nextInt(10)) + "%", "#4f46e5");
        VBox adminBox = createEnhancedStatCard("👑", "Administrateurs", String.valueOf(stats[0]),
                "+" + (2 + rand.nextInt(5)) + "%", "#10b981");
        VBox orgBox = createEnhancedStatCard("🎯", "Organisateurs", String.valueOf(stats[1]),
                "+" + (10 + rand.nextInt(15)) + "%", "#3b82f6");
        VBox partBox = createEnhancedStatCard("😊", "Participants", String.valueOf(stats[2]),
                "+" + (3 + rand.nextInt(8)) + "%", "#f59e0b");
        VBox activeBox = createEnhancedStatCard("✅", "Actifs ce mois", String.valueOf(totalUsers - rand.nextInt(10)),
                "+" + (1 + rand.nextInt(5)) + "%", "#8b5cf6");

        statsBox.getChildren().addAll(totalBox, adminBox, orgBox, partBox, activeBox);
        return statsBox;
    }

    private VBox createEnhancedStatCard(String icon, String title, String value, String change, String color) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setPrefWidth(180);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(50, 50);
        iconContainer.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 12;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 20));
        iconLabel.setTextFill(Color.web(color));

        iconContainer.getChildren().add(iconLabel);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web("#1e293b"));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web("#64748b"));

        HBox changeRow = new HBox(5);
        changeRow.setAlignment(Pos.CENTER);

        Label changeLabel = new Label(change);
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        if (change.startsWith("+")) {
            changeLabel.setTextFill(Color.web("#10b981"));
        } else {
            changeLabel.setTextFill(Color.web("#ef4444"));
        }

        Label changeText = new Label("vs mois dernier");
        changeText.setFont(Font.font("Arial", 10));
        changeText.setTextFill(Color.web("#94a3b8"));

        changeRow.getChildren().addAll(changeLabel, changeText);

        card.getChildren().addAll(iconContainer, valueLabel, titleLabel, changeRow);
        return card;
    }

    private HBox createEnhancedToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button addBtn = createEnhancedToolbarButton("➕ Ajouter utilisateur", "#4f46e5");
        addBtn.setOnAction(e -> showAddUserDialog());

        Button editBtn = createEnhancedToolbarButton("✏️ Modifier", "#3b82f6");
        editBtn.setOnAction(e -> editSelectedUser());

        Button deleteBtn = createEnhancedToolbarButton("🗑️ Supprimer", "#ef4444");
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        Button exportBtn = createEnhancedToolbarButton("📤 Exporter", "#10b981");
        exportBtn.setOnAction(e -> exportUsers());

        Button refreshBtn = createEnhancedToolbarButton("🔄 Actualiser", "#64748b");
        refreshBtn.setOnAction(e -> refreshUserTable());

        // Champ de recherche amélioré
        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un utilisateur...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-padding: 12 15;");

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                "-fx-padding: 12 15; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> searchUsers(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        HBox.setHgrow(searchBox, Priority.ALWAYS);
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, exportBtn, refreshBtn, searchBox);

        return toolbar;
    }

    private Button createEnhancedToolbarButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; " +
                "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    @SuppressWarnings("unchecked")
    private void setupEnhancedUserTable() {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPlaceholder(new Label("Aucun utilisateur trouvé"));
        userTable.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        userTable.setPrefHeight(400);

        // Colonnes améliorées
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(120);

        TableColumn<User, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        prenomCol.setPrefWidth(120);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);

        TableColumn<User, String> sexeCol = new TableColumn<>("Genre");
        sexeCol.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        sexeCol.setPrefWidth(80);

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

        TableColumn<User, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData -> {
            Random random = new Random(cellData.getValue().getId());
            String[] statuses = {"Actif", "Inactif", "Suspendu"};
            return new javafx.beans.property.SimpleStringProperty(statuses[random.nextInt(statuses.length)]);
        });
        statusCol.setPrefWidth(80);

        userTable.getColumns().addAll(idCol, nomCol, prenomCol, emailCol, roleCol, sexeCol, dateCol, statusCol);

        // Style amélioré pour les cellules de rôle
        roleCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);

                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(5, 10, 5, 10));

                    switch (role.toLowerCase()) {
                        case "admin":
                            setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                    "-fx-padding: 5 12;");
                            break;
                        case "organisateur":
                            setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                    "-fx-padding: 5 12;");
                            break;
                        case "participant":
                            setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                    "-fx-padding: 5 12;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Style pour les cellules de statut
        statusCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(5, 10, 5, 10));

                    switch (status.toLowerCase()) {
                        case "actif":
                            setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                    "-fx-padding: 5 12;");
                            break;
                        case "inactif":
                            setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                    "-fx-padding: 5 12;");
                            break;
                        case "suspendu":
                            setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                    "-fx-padding: 5 12;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        refreshUserTable();
    }

    private void refreshUserTable() {
        List<User> users = userService.getAllUsers();
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    private void searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshUserTable();
            return;
        }

        List<User> users = userService.searchUsers(keyword);
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    private void exportUsers() {
        showAlert("Export", "Exportation des utilisateurs - Fonctionnalité en développement");
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Remplissez les informations du nouvel utilisateur");
        dialog.initOwner(primaryStage);

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        nomField.setPrefHeight(40);
        styleTextField(nomField);

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        prenomField.setPrefHeight(40);
        styleTextField(prenomField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(40);
        styleTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setPrefHeight(40);
        styleTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue("participant");
        roleCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue("Non spécifié");
        genreCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Mot de passe:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Genre:"), 0, 5);
        grid.add(genreCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                User newUser = new User();
                newUser.setNom(nomField.getText());
                newUser.setPrenom(prenomField.getText());
                newUser.setEmail(emailField.getText());
                newUser.setPassword(passwordField.getText());
                newUser.setRole(roleCombo.getValue());
                newUser.setPhoto("default.jpg");

                String genre = genreCombo.getValue();
                int idGenre = 3; // Par défaut "Non spécifié"
                if ("Homme".equals(genre)) idGenre = 1;
                else if ("Femme".equals(genre)) idGenre = 2;
                newUser.setIdGenre(idGenre);

                return newUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (userService.emailExists(user.getEmail())) {
                showAlert("Erreur", "Cet email est déjà utilisé!");
                return;
            }

            if (userService.addUser(user)) {
                showAlert("Succès", "Utilisateur ajouté avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout de l'utilisateur");
            }
        });
    }

    private void editSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur à modifier");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modifier les informations de " + selectedUser.getNomComplet());
        dialog.initOwner(primaryStage);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField nomField = new TextField(selectedUser.getNom());
        nomField.setPrefHeight(40);
        styleTextField(nomField);

        TextField prenomField = new TextField(selectedUser.getPrenom());
        prenomField.setPrefHeight(40);
        styleTextField(prenomField);

        TextField emailField = new TextField(selectedUser.getEmail());
        emailField.setPrefHeight(40);
        styleTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe (laisser vide pour garder l'actuel)");
        passwordField.setPrefHeight(40);
        styleTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue(selectedUser.getRole());
        roleCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue(selectedUser.getSexe() != null ? selectedUser.getSexe() : "Non spécifié");
        genreCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Nouveau mot de passe:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Genre:"), 0, 5);
        grid.add(genreCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selectedUser.setNom(nomField.getText());
                selectedUser.setPrenom(prenomField.getText());
                selectedUser.setEmail(emailField.getText());
                selectedUser.setRole(roleCombo.getValue());

                String genre = genreCombo.getValue();
                int idGenre = 3;
                if ("Homme".equals(genre)) idGenre = 1;
                else if ("Femme".equals(genre)) idGenre = 2;
                selectedUser.setIdGenre(idGenre);

                if (!passwordField.getText().isEmpty()) {
                    selectedUser.setPassword(passwordField.getText());
                }

                return selectedUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (userService.updateUser(user)) {
                showAlert("Succès", "Utilisateur modifié avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de la modification de l'utilisateur");
            }
        });
    }

    private void deleteSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur à supprimer");
            return;
        }

        if (selectedUser.getId() == currentUser.getId()) {
            showAlert("Erreur", "Vous ne pouvez pas supprimer votre propre compte!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " +
                selectedUser.getNomComplet() + " ?");
        confirmAlert.initOwner(primaryStage);

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (userService.deleteUser(selectedUser.getId())) {
                showAlert("Succès", "Utilisateur supprimé avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de la suppression de l'utilisateur");
            }
        }
    }

    // Méthodes pour les autres vues
    private void showDashboard() {
        ScrollPane content = createDashboardView();
        root.setCenter(content);
        updateSidebarButton("dashboard");
    }

    private void showProducts() {
        VBox productsView = new VBox(30);
        productsView.setPadding(new Insets(40));
        productsView.setAlignment(Pos.CENTER);

        Label title = new Label("Gestion des Produits");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1e293b"));

        Label message = new Label("Cette section est en cours de développement.\n\n" +
                "Fonctionnalités à venir :\n" +
                "• Gestion complète des produits\n" +
                "• Catégories et sous-catégories\n" +
                "• Gestion des stocks\n" +
                "• Analyse des ventes par produit\n" +
                "• Génération de rapports");
        message.setFont(Font.font("Arial", 16));
        message.setTextFill(Color.web("#64748b"));
        message.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        message.setWrapText(true);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setMaxWidth(600);

        card.getChildren().addAll(title, message);
        productsView.getChildren().add(card);

        ScrollPane scrollPane = new ScrollPane(productsView);
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        updateSidebarButton("products");
    }

    private void showOrders() {
        VBox ordersView = new VBox(30);
        ordersView.setPadding(new Insets(40));
        ordersView.setAlignment(Pos.CENTER);

        Label title = new Label("Gestion des Commandes");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1e293b"));

        // Tableau de commandes simulé
        VBox tableContainer = new VBox(15);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25;");
        tableContainer.setMaxWidth(1000);

        Label tableTitle = new Label("Commandes récentes");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#1e293b"));

        @SuppressWarnings("unchecked")
        TableView<String[]> ordersTable = new TableView<>();
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersTable.setPrefHeight(300);
        ordersTable.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        TableColumn<String[], String> orderIdCol = new TableColumn<>("ID Commande");
        orderIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        orderIdCol.setPrefWidth(100);

        TableColumn<String[], String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        clientCol.setPrefWidth(150);

        TableColumn<String[], String> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        amountCol.setPrefWidth(100);

        TableColumn<String[], String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        dateCol.setPrefWidth(100);

        TableColumn<String[], String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[4]));
        statusCol.setPrefWidth(100);

        ordersTable.getColumns().addAll(orderIdCol, clientCol, amountCol, dateCol, statusCol);

        ObservableList<String[]> ordersData = FXCollections.observableArrayList(
                new String[]{"#001245", "Marie Dupont", "€245.50", "15/03/2024", "Livrée"},
                new String[]{"#001244", "Pierre Martin", "€120.00", "14/03/2024", "Expédiée"},
                new String[]{"#001243", "Ahmed Ben", "€89.99", "14/03/2024", "En préparation"},
                new String[]{"#001242", "Sophie Leroy", "€320.75", "13/03/2024", "Confirmée"},
                new String[]{"#001241", "Karim Said", "€45.99", "12/03/2024", "En attente"}
        );

        ordersTable.setItems(ordersData);

        tableContainer.getChildren().addAll(tableTitle, ordersTable);
        ordersView.getChildren().addAll(title, tableContainer);

        ScrollPane scrollPane = new ScrollPane(ordersView);
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        updateSidebarButton("orders");
    }

    private void showEvents() {
        VBox eventsView = new VBox(30);
        eventsView.setPadding(new Insets(40));
        eventsView.setAlignment(Pos.CENTER);

        Label title = new Label("Gestion des Événements");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1e293b"));

        // Calendrier simplifié
        VBox calendarContainer = new VBox(15);
        calendarContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25;");
        calendarContainer.setMaxWidth(800);

        Label calendarTitle = new Label("Calendrier des événements");
        calendarTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        calendarTitle.setTextFill(Color.web("#1e293b"));

        GridPane calendar = new GridPane();
        calendar.setHgap(10);
        calendar.setVgap(10);
        calendar.setPadding(new Insets(20, 0, 0, 0));

        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            dayLabel.setTextFill(Color.web("#64748b"));
            dayLabel.setAlignment(Pos.CENTER);
            calendar.add(dayLabel, i, 0);
        }

        int day = 1;
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (day <= 31) {
                    StackPane dayCell = new StackPane();
                    dayCell.setPrefSize(40, 40);
                    dayCell.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;");

                    Label dayNumber = new Label(String.valueOf(day));
                    dayNumber.setFont(Font.font("Arial", 12));

                    if (day == 15) {
                        dayCell.setStyle("-fx-background-color: #eef2ff; -fx-border-color: #4f46e5; -fx-border-width: 2; -fx-background-radius: 8;");
                        dayNumber.setTextFill(Color.web("#4f46e5"));
                        dayNumber.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    }

                    dayCell.getChildren().add(dayNumber);
                    calendar.add(dayCell, col, row);
                    day++;
                }
            }
        }

        calendarContainer.getChildren().addAll(calendarTitle, calendar);
        eventsView.getChildren().addAll(title, calendarContainer);

        ScrollPane scrollPane = new ScrollPane(eventsView);
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        updateSidebarButton("events");
    }

    private void showDonations() {
        VBox donationsView = new VBox(30);
        donationsView.setPadding(new Insets(40));
        donationsView.setAlignment(Pos.CENTER);

        Label title = new Label("Gestion des Dons");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1e293b"));

        // Statistiques des dons
        HBox donationStats = new HBox(20);
        donationStats.setAlignment(Pos.CENTER);

        VBox totalDonations = createDonationStatCard("💰", "Total collecté", "€2,845", "+18%");
        VBox donorsCount = createDonationStatCard("👥", "Donateurs", "142", "+12%");
        VBox avgDonation = createDonationStatCard("📊", "Don moyen", "€20.50", "+5%");
        VBox campaigns = createDonationStatCard("🎯", "Campagnes actives", "8", "+3%");

        donationStats.getChildren().addAll(totalDonations, donorsCount, avgDonation, campaigns);

        donationsView.getChildren().addAll(title, donationStats);

        ScrollPane scrollPane = new ScrollPane(donationsView);
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        updateSidebarButton("donations");
    }

    private VBox createDonationStatCard(String icon, String title, String value, String change) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setPrefWidth(200);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 24));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.web("#1e293b"));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.web("#64748b"));

        Label changeLabel = new Label(change);
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        changeLabel.setTextFill(change.startsWith("+") ? Color.web("#10b981") : Color.web("#ef4444"));

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel, changeLabel);
        return card;
    }

    private void showAnalytics() {
        VBox analyticsView = new VBox(20);
        analyticsView.setPadding(new Insets(30));
        analyticsView.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Analytics Avancées");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        // Graphique combiné
        VBox combinedChartContainer = new VBox(15);
        combinedChartContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25;");
        combinedChartContainer.setPrefWidth(1000);

        Label chartTitle = new Label("Analyse comparative des performances");
        chartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        chartTitle.setTextFill(Color.web("#1e293b"));

        // Créer un graphique combiné (Line + Bar)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.getCategories().addAll(months);
        xAxis.setLabel("Mois");
        NumberAxis yAxis = new NumberAxis(0, 120000, 20000);
        yAxis.setLabel("Revenu (€)");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "€", ""));

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setLegendVisible(true);
        lineChart.setCreateSymbols(true);
        lineChart.setPrefHeight(400);
        lineChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenu");
        for (int i = 0; i < months.length; i++) {
            revenueSeries.getData().add(new XYChart.Data<>(months[i], monthlyRevenue[i]));
        }

        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("Nouveaux utilisateurs");
        for (int i = 0; i < months.length; i++) {
            userSeries.getData().add(new XYChart.Data<>(months[i], monthlyRevenue[i] / 300));
        }

        lineChart.getData().addAll(revenueSeries, userSeries);

        combinedChartContainer.getChildren().addAll(chartTitle, lineChart);
        analyticsView.getChildren().addAll(title, combinedChartContainer);

        ScrollPane scrollPane = new ScrollPane(analyticsView);
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        updateSidebarButton("analytics");
    }

    private void showSettings() {
        VBox settingsView = new VBox(30);
        settingsView.setPadding(new Insets(40));
        settingsView.setAlignment(Pos.CENTER);

        Label title = new Label("Paramètres du système");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1e293b"));

        // Cartes de paramètres
        VBox settingsCards = new VBox(15);
        settingsCards.setMaxWidth(600);

        String[][] settingsGroups = {
                {"Général", "Paramètres généraux de l'application"},
                {"Sécurité", "Configuration de la sécurité et des accès"},
                {"Notifications", "Gestion des notifications et alertes"},
                {"Intégrations", "Services et API tiers"},
                {"Apparence", "Thème et personnalisation"}
        };

        for (String[] group : settingsGroups) {
            HBox settingCard = new HBox(15);
            settingCard.setAlignment(Pos.CENTER_LEFT);
            settingCard.setPadding(new Insets(20));
            settingCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: #e2e8f0; -fx-border-width: 1;");
            settingCard.setPrefWidth(600);

            VBox textContent = new VBox(5);
            Label groupTitle = new Label(group[0]);
            groupTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            groupTitle.setTextFill(Color.web("#1e293b"));

            Label groupDesc = new Label(group[1]);
            groupDesc.setFont(Font.font("Arial", 12));
            groupDesc.setTextFill(Color.web("#64748b"));

            textContent.getChildren().addAll(groupTitle, groupDesc);

            HBox.setHgrow(textContent, Priority.ALWAYS);
            settingCard.getChildren().add(textContent);

            Button configureBtn = new Button("Configurer");
            configureBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;");
            settingCard.getChildren().add(configureBtn);

            settingsCards.getChildren().add(settingCard);
        }

        settingsView.getChildren().addAll(title, settingsCards);

        ScrollPane scrollPane = new ScrollPane(settingsView);
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        updateSidebarButton("settings");
    }

    private void showProfile() {
        VBox profileView = new VBox(30);
        profileView.setPadding(new Insets(40));
        profileView.setAlignment(Pos.CENTER);

        Label title = new Label("Mon Profil");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1e293b"));

        VBox profileCard = new VBox(20);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPadding(new Insets(40));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1;");
        profileCard.setMaxWidth(500);

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(100, 100);
        avatar.setStyle("-fx-background-color: #4f46e5; -fx-background-radius: 50;");

        Label avatarText = new Label(String.valueOf(currentUser.getPrenom().charAt(0)) +
                String.valueOf(currentUser.getNom().charAt(0)));
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        avatarText.setTextFill(Color.WHITE);

        avatar.getChildren().add(avatarText);

        // Informations utilisateur
        VBox userInfo = new VBox(10);
        userInfo.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(currentUser.getNomComplet());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web("#1e293b"));

        Label emailLabel = new Label(currentUser.getEmail());
        emailLabel.setFont(Font.font("Arial", 16));
        emailLabel.setTextFill(Color.web("#64748b"));

        HBox roleBox = new HBox(5);
        roleBox.setAlignment(Pos.CENTER);

        Label roleLabel = new Label(currentUser.getRole().toUpperCase());
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        roleLabel.setTextFill(Color.WHITE);
        roleLabel.setPadding(new Insets(5, 15, 5, 15));
        roleLabel.setStyle("-fx-background-color: #4f46e5; -fx-background-radius: 20;");

        roleBox.getChildren().add(roleLabel);

        userInfo.getChildren().addAll(nameLabel, emailLabel, roleBox);

        // Statistiques personnelles
        VBox personalStats = new VBox(15);
        personalStats.setPadding(new Insets(20, 0, 0, 0));
        personalStats.setAlignment(Pos.CENTER);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);

        String[][] statsData = {
                {"Date d'inscription", currentUser.getCreatedAt() != null ?
                        currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"},
                {"Dernière connexion", "Aujourd'hui"},
                {"Sessions actives", "1"},
                {"Activité totale", "42h 15m"}
        };

        for (int i = 0; i < statsData.length; i++) {
            Label statLabel = new Label(statsData[i][0] + ":");
            statLabel.setFont(Font.font("Arial", 12));
            statLabel.setTextFill(Color.web("#64748b"));

            Label statValue = new Label(statsData[i][1]);
            statValue.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            statValue.setTextFill(Color.web("#1e293b"));

            statsGrid.add(statLabel, 0, i);
            statsGrid.add(statValue, 1, i);
        }

        personalStats.getChildren().add(statsGrid);

        profileCard.getChildren().addAll(avatar, userInfo, personalStats);
        profileView.getChildren().add(profileCard);

        ScrollPane scrollPane = new ScrollPane(profileView);
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        updateSidebarButton("profile");
    }

    private void logout() {
        SessionManager.logout();
        primaryStage.close();
        new LoginView().start(new Stage());
    }

    private void refreshDashboard() {
        showDashboard();
        showAlert("Actualisation", "Dashboard actualisé avec les dernières données");
    }

    private void toggleSidebar() {
        VBox sidebar = (VBox) root.getLeft();
        if (sidebar.getPrefWidth() == 280) {
            sidebar.setPrefWidth(80);
            // Cacher les textes, ne garder que les icônes
        } else {
            sidebar.setPrefWidth(280);
            // Réafficher les textes
        }
    }

    private void updateSidebarButton(String activeButton) {
        VBox sidebar = (VBox) root.getLeft();
        VBox navSection = (VBox) sidebar.getChildren().get(0);

        // Réinitialiser tous les boutons
        for (int i = 1; i < navSection.getChildren().size(); i++) {
            if (navSection.getChildren().get(i) instanceof Button) {
                Button btn = (Button) navSection.getChildren().get(i);
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        }

        // Activer le bon bouton
        String[] buttonNames = {"dashboard", "users", "products", "orders", "events", "donations", "analytics"};
        int buttonIndex = -1;
        for (int i = 0; i < buttonNames.length; i++) {
            if (buttonNames[i].equals(activeButton)) {
                buttonIndex = i + 1; // +1 car le premier enfant est le label
                break;
            }
        }

        if (buttonIndex != -1 && buttonIndex < navSection.getChildren().size()) {
            Button activeBtn = (Button) navSection.getChildren().get(buttonIndex);
            activeBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #c7d2fe; " +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 0;");
        }
    }

    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        field.setPrefWidth(300);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);

        // Style personnalisé pour l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

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
}