package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Node;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

@SuppressWarnings("unchecked")
public class DashboardView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;

    private ComboBox<String> timeFilterCombo;
    private LineChart<String, Number> registrationLineChart;
    private PieChart rolePieChart;

    public DashboardView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    public void showDashboard(StackPane mainContentArea) {
        ScrollPane content = createDashboardView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(content);
    }

    private ScrollPane createDashboardView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #E6F8F6;");

        // En-tête du dashboard
        HBox dashboardHeader = new HBox();
        dashboardHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Welcome back, " + currentUser.getPrenom() + "! Here's what's happening with your platform.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#03414D"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Bouton refresh
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-border-color: #03414D; -fx-border-radius: 8; " +
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
        trendsChartSection.setPrefWidth(900);

        // Graphique de distribution (25% de l'espace)
        VBox distributionChartSection = createEnhancedDistributionChart();
        distributionChartSection.setPrefWidth(450);

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
                String.valueOf(totalUsers), "+" + growthRate + "%", "#72DFD0", true);

        VBox adminCard = createStatCard("👑", "Admins",
                String.valueOf(roleStats[0]), "System Managers", "#72DFD0", false);

        VBox orgCard = createStatCard("🎯", "Organizers",
                String.valueOf(roleStats[1]), "Event Managers", "#A0F6D2", false);

        VBox partCard = createStatCard("😊", "Participants",
                String.valueOf(roleStats[2]), "Active Users", "#A0F6D2", false);

        statsCards.getChildren().addAll(totalCard, adminCard, orgCard, partCard);
        return statsCards;
    }

    private VBox createStatCard(String icon, String title, String value, String subtitle, String color, boolean isMain) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                "-fx-border-color: #72DFD0; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(isMain ? 300 : 250);
        card.setPrefHeight(150);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        Circle iconCircle = new Circle(25);
        iconCircle.setFill(Color.web(color));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));
        iconLabel.setTextFill(Color.web("#03414D"));

        iconContainer.getChildren().addAll(iconCircle, iconLabel);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#03414D"));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 12));
        subtitleLabel.setTextFill(Color.web("#03414D"));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconContainer, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web("#03414D"));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    private VBox createEnhancedTrendsChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Registration Trends");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("User registration over time");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#03414D"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        // Contrôles de filtre temporel
        HBox filterControls = new HBox(10);
        filterControls.setAlignment(Pos.CENTER_RIGHT);

        Label periodLabel = new Label("Period:");
        periodLabel.setFont(Font.font("Arial", 12));
        periodLabel.setTextFill(Color.web("#03414D"));

        timeFilterCombo = new ComboBox<>();
        timeFilterCombo.getItems().addAll(
                "Last 7 Days",
                "Last 30 Days",
                "Last 90 Days",
                "This Year",
                "All Time"
        );
        timeFilterCombo.setValue("Last 30 Days");
        timeFilterCombo.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        timeFilterCombo.setPrefWidth(150);
        timeFilterCombo.setOnAction(e -> updateRegistrationChart());

        filterControls.getChildren().addAll(periodLabel, timeFilterCombo);
        header.getChildren().addAll(textContent, filterControls);

        // Area Chart pour les tendances
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelFill(Color.web("#03414D"));
        xAxis.setTickLabelFont(Font.font("Arial", 10));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Registrations");
        yAxis.setTickLabelFill(Color.web("#03414D"));
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

    private void applyChartSeriesStyles() {
        for (XYChart.Series<String, Number> series : registrationLineChart.getData()) {
            String seriesName = series.getName();
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    switch (seriesName) {
                        case "Total":
                            node.setStyle("-fx-stroke: #72DFD0; -fx-stroke-width: 3;");
                            break;
                        case "Admins":
                            node.setStyle("-fx-stroke: #03414D; -fx-stroke-width: 2;");
                            break;
                        case "Organizers":
                            node.setStyle("-fx-stroke: #A0F6D2; -fx-stroke-width: 2;");
                            break;
                        case "Participants":
                            node.setStyle("-fx-stroke: #72DFD0; -fx-stroke-width: 2;");
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
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Role Distribution");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("User distribution by role");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#03414D"));

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
            adminSlice.getNode().setStyle("-fx-pie-color: #72DFD0;");
            orgSlice.getNode().setStyle("-fx-pie-color: #A0F6D2;");
            partSlice.getNode().setStyle("-fx-pie-color: #03414D;");
        }
    }

    private VBox createEnhancedSummaryTable() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label tableTitle = new Label("Recent Users");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#03414D"));

        Label tableSubtitle = new Label("Latest registered users");
        tableSubtitle.setFont(Font.font("Arial", 12));
        tableSubtitle.setTextFill(Color.web("#03414D"));

        textContent.getChildren().addAll(tableTitle, tableSubtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Tableau des utilisateurs récents avec avatars
        TableView<User> recentTable = new TableView<>();
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recentTable.setPrefHeight(250);
        recentTable.setStyle("-fx-background-color: transparent;");

        // Colonne Avatar
        TableColumn<User, String> avatarCol = new TableColumn<>("");
        avatarCol.setPrefWidth(60);
        avatarCol.setCellFactory(column -> new TableCell<User, String>() {
            private final StackPane avatarContainer = new StackPane();
            private final Circle avatarCircle = new Circle(18);

            {
                avatarCircle.setFill(Color.web("#72DFD0"));
                avatarContainer.getChildren().add(avatarCircle);
                avatarContainer.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());

                    // Charger l'image de profil
                    ImageView avatarImageView = loadProfileImage(user, 36);
                    if (avatarImageView != null) {
                        avatarContainer.getChildren().clear();
                        avatarContainer.getChildren().add(avatarImageView);
                    } else {
                        // Utiliser les initiales
                        avatarContainer.getChildren().clear();
                        avatarCircle.setFill(Color.web("#72DFD0"));
                        String initials = getInitials(user);
                        Label avatarText = new Label(initials);
                        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                        avatarText.setTextFill(Color.WHITE);
                        avatarContainer.getChildren().addAll(avatarCircle, avatarText);
                    }

                    setGraphic(avatarContainer);
                }
            }
        });

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
                            setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; -fx-background-radius: 15;");
                            break;
                        case "organisateur":
                            setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; -fx-background-radius: 15;");
                            break;
                        case "participant":
                            setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; -fx-background-radius: 15;");
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

        recentTable.getColumns().addAll(avatarCol, nameCol, emailCol, roleCol, dateCol);

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

    // NOUVELLE MÉTHODE pour charger les images de profil
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
    private double calculateGrowthRate() {
        int totalUsers = userService.countUsers();
        if (totalUsers <= 10) {
            return 25.0;
        } else {
            return 5.0;
        }
    }

    private int getActiveUsersThisMonth() {
        int totalUsers = userService.countUsers();
        return (int)(totalUsers * 0.75);
    }

    private void refreshDashboard() {
        adminDashboard.showAlert("Refreshed", "Dashboard data has been refreshed");
        // Pour rafraîchir complètement, on pourrait recréer la vue
    }
}