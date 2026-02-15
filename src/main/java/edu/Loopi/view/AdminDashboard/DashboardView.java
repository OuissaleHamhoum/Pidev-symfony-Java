package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;

    private ComboBox<String> registrationPeriodFilter;
    private ComboBox<String> registrationTypeFilter;
    private ComboBox<String> activityPeriodFilter;
    private ComboBox<String> activityTypeFilter;

    private LineChart<String, Number> registrationChart;
    private BarChart<String, Number> activityChart;

    public DashboardView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    @SuppressWarnings("unchecked")
    public void showDashboard(StackPane mainContentArea, boolean isDarkMode) {
        ScrollPane scrollPane = createDashboardView(isDarkMode);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(scrollPane);
    }

    @SuppressWarnings("unchecked")
    private ScrollPane createDashboardView(boolean isDarkMode) {
        VBox container = new VBox(24);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + adminDashboard.getBgColor() + ";");

        HBox header = createWelcomeHeader(isDarkMode);

        // Cartes de statistiques
        HBox statsCards = createStatsCards(isDarkMode);

        HBox chartsRow1 = new HBox(20);
        chartsRow1.setAlignment(Pos.CENTER);

        VBox registrationChartCard = createRegistrationChart(isDarkMode);
        registrationChartCard.setPrefWidth(700);

        VBox distributionCard = createDistributionCard(isDarkMode);
        distributionCard.setPrefWidth(500);

        chartsRow1.getChildren().addAll(registrationChartCard, distributionCard);

        HBox chartsRow2 = new HBox(20);
        chartsRow2.setAlignment(Pos.CENTER);
        chartsRow2.setPadding(new Insets(20, 0, 0, 0));

        VBox activityChartCard = createActivityChart(isDarkMode);
        activityChartCard.setPrefWidth(600);

        VBox genderChartCard = createGenderChart(isDarkMode);
        genderChartCard.setPrefWidth(600);

        chartsRow2.getChildren().addAll(activityChartCard, genderChartCard);

        VBox recentUsers = createRecentUsersTable(isDarkMode);

        container.getChildren().addAll(header, statsCards, chartsRow1, chartsRow2, recentUsers);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createWelcomeHeader(boolean isDarkMode) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 8, 0));

        VBox headerText = new VBox(4);

        LocalDateTime now = LocalDateTime.now();
        String greeting = getGreeting(now.getHour()) + ", " + currentUser.getPrenom();

        Label welcomeLabel = new Label(greeting);
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
        welcomeLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label dateLabel = new Label(now.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")));
        dateLabel.setFont(Font.font("System", 14));
        dateLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(welcomeLabel, dateLabel);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        header.getChildren().addAll(headerText);
        return header;
    }

    private String getGreeting(int hour) {
        if (hour < 12) return "Bonjour";
        if (hour < 18) return "Bon après-midi";
        return "Bonsoir";
    }

    private HBox createStatsCards(boolean isDarkMode) {
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER);
        cards.setPadding(new Insets(20, 0, 20, 0));

        int totalUsers = userService.countUsers();
        int[] roleStats = userService.getUserStatistics();
        int activeToday = getActiveUsersToday();
        int newThisMonth = getNewUsersThisMonth();

        VBox totalCard = createStatCard("👥", "Total", String.format("%,d", totalUsers),
                "+" + newThisMonth + " ce mois", isDarkMode);
        VBox activeCard = createStatCard("✅", "Actifs ajd", String.format("%,d", activeToday),
                String.format("%.1f%%", (activeToday * 100.0 / totalUsers)), isDarkMode);
        VBox adminsCard = createStatCard("👑", "Admins", String.format("%,d", roleStats[0]),
                String.format("%.1f%%", (roleStats[0] * 100.0 / totalUsers)), isDarkMode);
        VBox organizersCard = createStatCard("🎯", "Organisateurs", String.format("%,d", roleStats[1]),
                String.format("%.1f%%", (roleStats[1] * 100.0 / totalUsers)), isDarkMode);
        VBox participantsCard = createStatCard("😊", "Participants", String.format("%,d", roleStats[2]),
                String.format("%.1f%%", (roleStats[2] * 100.0 / totalUsers)), isDarkMode);

        cards.getChildren().addAll(totalCard, activeCard, adminsCard, organizersCard, participantsCard);
        return cards;
    }

    private VBox createStatCard(String icon, String title, String value, String subtitle, boolean isDarkMode) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1); " +
                "-fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 12;");
        card.setPrefWidth(200);
        card.setPrefHeight(120);

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 28));
        iconLabel.setMinWidth(40);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "20; -fx-background-radius: 8; -fx-padding: 6;");

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        subtitleLabel.setTextFill(Color.web(adminDashboard.getSuccessColor()));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        topRow.getChildren().addAll(iconLabel, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        card.getChildren().addAll(topRow, valueLabel);
        return card;
    }

    private int getActiveUsersToday() {
        List<User> users = userService.getAllUsers();
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        return (int) users.stream()
                .filter(u -> u.getUpdatedAt() != null && u.getUpdatedAt().isAfter(today))
                .count();
    }

    private int getNewUsersThisMonth() {
        List<User> users = userService.getAllUsers();
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        return (int) users.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(firstDayOfMonth))
                .count();
    }

    @SuppressWarnings("unchecked")
    private VBox createRegistrationChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        VBox headerText = new VBox(2);
        Label title = new Label("Croissance des inscriptions");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Nombre d'inscriptions par période");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Filtres pour le graphique de croissance
        registrationPeriodFilter = new ComboBox<>();
        registrationPeriodFilter.getItems().addAll("7 jours", "30 jours", "90 jours", "6 mois", "12 mois");
        registrationPeriodFilter.setValue("30 jours");
        registrationPeriodFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        registrationPeriodFilter.setPrefWidth(100);
        registrationPeriodFilter.setOnAction(e -> updateRegistrationChart());

        registrationTypeFilter = new ComboBox<>();
        registrationTypeFilter.getItems().addAll("Par jour", "Par semaine", "Par mois");
        registrationTypeFilter.setValue("Par jour");
        registrationTypeFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        registrationTypeFilter.setPrefWidth(100);
        registrationTypeFilter.setOnAction(e -> updateRegistrationChart());

        HBox filtersBox = new HBox(10);
        filtersBox.getChildren().addAll(registrationPeriodFilter, registrationTypeFilter);

        header.getChildren().addAll(headerText, filtersBox);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Période");
        xAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));
        xAxis.setTickLabelFont(Font.font("System", 10));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'inscriptions");
        yAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));
        yAxis.setTickLabelFont(Font.font("System", 10));

        registrationChart = new LineChart<>(xAxis, yAxis);
        registrationChart.setTitle("");
        registrationChart.setLegendVisible(false);
        registrationChart.setAnimated(false);
        registrationChart.setPrefHeight(250);
        registrationChart.setStyle("-fx-background-color: transparent;");
        registrationChart.setCreateSymbols(true);
        registrationChart.setVerticalGridLinesVisible(false);

        updateRegistrationChart();

        card.getChildren().addAll(header, registrationChart);
        return card;
    }

    @SuppressWarnings("unchecked")
    private void updateRegistrationChart() {
        registrationChart.getData().clear();

        String period = registrationPeriodFilter.getValue();
        String type = registrationTypeFilter.getValue();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions");

        Map<String, Integer> registrationData = getRegistrationData(period, type);

        registrationData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    XYChart.Data<String, Number> chartData = new XYChart.Data<>(entry.getKey(), entry.getValue());
                    series.getData().add(chartData);
                });

        registrationChart.getData().add(series);

        // Styliser la ligne et les points
        series.getNode().setStyle("-fx-stroke: " + adminDashboard.getAccentColor() + "; -fx-stroke-width: 2;");

        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                        "; -fx-background-radius: 4px; -fx-padding: 4px;");
            }
        }
    }

    private Map<String, Integer> getRegistrationData(String period, String type) {
        Map<String, Integer> data = new LinkedHashMap<>();
        List<User> users = userService.getAllUsers();
        LocalDateTime now = LocalDateTime.now();

        int days = getDaysFromPeriod(period);
        DateTimeFormatter formatter;

        switch (type) {
            case "Par jour":
                formatter = DateTimeFormatter.ofPattern("dd/MM");
                for (int i = days - 1; i >= 0; i--) {
                    LocalDateTime date = now.minusDays(i);
                    String key = date.format(formatter);
                    data.put(key, 0);
                }
                break;
            case "Par semaine":
                formatter = DateTimeFormatter.ofPattern("'Semaine' w");
                int weeks = days / 7;
                for (int i = weeks - 1; i >= 0; i--) {
                    LocalDateTime date = now.minusWeeks(i);
                    String key = "S" + (weeks - i);
                    data.put(key, 0);
                }
                break;
            case "Par mois":
                formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                int months = days / 30;
                for (int i = months - 1; i >= 0; i--) {
                    LocalDateTime date = now.minusMonths(i);
                    String key = date.format(formatter);
                    data.put(key, 0);
                }
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("dd/MM");
                for (int i = days - 1; i >= 0; i--) {
                    LocalDateTime date = now.minusDays(i);
                    String key = date.format(formatter);
                    data.put(key, 0);
                }
        }

        for (User user : users) {
            if (user.getCreatedAt() != null && user.getCreatedAt().isAfter(now.minusDays(days))) {
                String key;
                switch (type) {
                    case "Par jour":
                        key = user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
                        break;
                    case "Par semaine":
                        key = "S" + (int) Math.ceil((double) (days - user.getCreatedAt().until(now, java.time.temporal.ChronoUnit.DAYS)) / 7);
                        break;
                    case "Par mois":
                        key = user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                        break;
                    default:
                        key = user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
                }
                data.put(key, data.getOrDefault(key, 0) + 1);
            }
        }

        return data;
    }

    private int getDaysFromPeriod(String period) {
        switch (period) {
            case "7 jours": return 7;
            case "30 jours": return 30;
            case "90 jours": return 90;
            case "6 mois": return 180;
            case "12 mois": return 365;
            default: return 30;
        }
    }

    @SuppressWarnings("unchecked")
    private VBox createDistributionCard(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label title = new Label("Répartition par rôle");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        int[] roleStats = userService.getUserStatistics();
        int total = roleStats[0] + roleStats[1] + roleStats[2];

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(200, 200);
        pieChart.setStyle("-fx-background-color: transparent;");

        if (total > 0) {
            PieChart.Data adminSlice = new PieChart.Data("Admins", roleStats[0]);
            PieChart.Data orgSlice = new PieChart.Data("Organisateurs", roleStats[1]);
            PieChart.Data partSlice = new PieChart.Data("Participants", roleStats[2]);
            pieChart.getData().addAll(adminSlice, orgSlice, partSlice);
        }

        VBox legendBox = new VBox(8);
        legendBox.setPadding(new Insets(0, 0, 0, 16));
        legendBox.setPrefWidth(180);

        addLegendItem(legendBox, adminDashboard.getAccentColor(), "Administrateurs", roleStats[0], total);
        addLegendItem(legendBox, adminDashboard.getSuccessColor(), "Organisateurs", roleStats[1], total);
        addLegendItem(legendBox, adminDashboard.getWarningColor(), "Participants", roleStats[2], total);

        HBox content = new HBox(16);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(pieChart, legendBox);

        card.getChildren().addAll(title, content);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox createActivityChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        VBox headerText = new VBox(2);
        Label title = new Label("Activité");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Connexions par période");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Filtres pour le graphique d'activité
        activityPeriodFilter = new ComboBox<>();
        activityPeriodFilter.getItems().addAll("7 jours", "30 jours", "90 jours", "6 mois", "12 mois");
        activityPeriodFilter.setValue("30 jours");
        activityPeriodFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        activityPeriodFilter.setPrefWidth(100);
        activityPeriodFilter.setOnAction(e -> updateActivityChart());

        activityTypeFilter = new ComboBox<>();
        activityTypeFilter.getItems().addAll("Par jour", "Par semaine", "Par mois");
        activityTypeFilter.setValue("Par jour");
        activityTypeFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        activityTypeFilter.setPrefWidth(100);
        activityTypeFilter.setOnAction(e -> updateActivityChart());

        HBox filtersBox = new HBox(10);
        filtersBox.getChildren().addAll(activityPeriodFilter, activityTypeFilter);

        header.getChildren().addAll(headerText, filtersBox);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Période");
        xAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));
        xAxis.setTickLabelFont(Font.font("System", 11));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre de connexions");
        yAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));
        yAxis.setTickLabelFont(Font.font("System", 11));

        activityChart = new BarChart<>(xAxis, yAxis);
        activityChart.setTitle("");
        activityChart.setLegendVisible(false);
        activityChart.setAnimated(false);
        activityChart.setPrefHeight(220);
        activityChart.setStyle("-fx-background-color: transparent;");
        activityChart.setCategoryGap(15);
        activityChart.setBarGap(5);

        updateActivityChart();

        card.getChildren().addAll(header, activityChart);
        return card;
    }

    @SuppressWarnings("unchecked")
    private void updateActivityChart() {
        activityChart.getData().clear();

        String period = activityPeriodFilter.getValue();
        String type = activityTypeFilter.getValue();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> activityData = getActivityData(period, type);

        activityData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        activityChart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + adminDashboard.getAccentColor() + ";");
            }
        }
    }

    private Map<String, Integer> getActivityData(String period, String type) {
        Map<String, Integer> activity = new LinkedHashMap<>();
        List<User> users = userService.getAllUsers();
        LocalDateTime now = LocalDateTime.now();

        int days = getDaysFromPeriod(period);

        // Initialiser les données
        switch (type) {
            case "Par jour":
                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");
                for (int i = days - 1; i >= 0; i--) {
                    LocalDateTime date = now.minusDays(i);
                    String key = date.format(dayFormatter);
                    activity.put(key, 0);
                }
                break;
            case "Par semaine":
                int weeks = days / 7;
                for (int i = weeks - 1; i >= 0; i--) {
                    String key = "S" + (weeks - i);
                    activity.put(key, 0);
                }
                break;
            case "Par mois":
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
                int months = days / 30;
                for (int i = months - 1; i >= 0; i--) {
                    LocalDateTime date = now.minusMonths(i);
                    String key = date.format(monthFormatter);
                    activity.put(key, 0);
                }
                break;
        }

        // Remplir les données
        for (User user : users) {
            if (user.getUpdatedAt() != null && user.getUpdatedAt().isAfter(now.minusDays(days))) {
                String key;
                switch (type) {
                    case "Par jour":
                        key = user.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
                        break;
                    case "Par semaine":
                        int weekNum = (int) Math.ceil((double) (days - user.getUpdatedAt().until(now, java.time.temporal.ChronoUnit.DAYS)) / 7);
                        key = "S" + weekNum;
                        break;
                    case "Par mois":
                        key = user.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                        break;
                    default:
                        key = user.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
                }
                activity.put(key, activity.getOrDefault(key, 0) + 1);
            }
        }

        return activity;
    }

    @SuppressWarnings("unchecked")
    private VBox createGenderChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(2);
        Label title = new Label("Répartition par genre");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Distribution (tous les utilisateurs)");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        header.getChildren().add(headerText);

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(200, 200);
        pieChart.setStyle("-fx-background-color: transparent;");

        VBox legendBox = new VBox(8);
        legendBox.setPadding(new Insets(0, 0, 0, 16));
        legendBox.setPrefWidth(180);

        HBox content = new HBox(16);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(pieChart, legendBox);

        updateGenderChart(pieChart, legendBox);

        card.getChildren().addAll(header, content);
        return card;
    }

    private void updateGenderChart(PieChart pieChart, VBox legendBox) {
        List<User> users = userService.getAllUsers();
        int hommes = 0, femmes = 0, autres = 0;

        for (User user : users) {
            if (user.getIdGenre() == 1) hommes++;
            else if (user.getIdGenre() == 2) femmes++;
            else autres++;
        }
        int total = hommes + femmes + autres;

        pieChart.getData().clear();
        legendBox.getChildren().clear();

        if (total > 0) {
            if (hommes > 0) {
                pieChart.getData().add(new PieChart.Data("Hommes", hommes));
                addGenderLegendItem(legendBox, "#3B82F6", "Hommes", hommes, total);
            }
            if (femmes > 0) {
                pieChart.getData().add(new PieChart.Data("Femmes", femmes));
                addGenderLegendItem(legendBox, "#EC4899", "Femmes", femmes, total);
            }
            if (autres > 0) {
                pieChart.getData().add(new PieChart.Data("Autres", autres));
                addGenderLegendItem(legendBox, "#8B5CF6", "Autres", autres, total);
            }
        }
    }

    private void addGenderLegendItem(VBox legendBox, String color, String label, int count, int total) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(6);
        dot.setFill(Color.web(color));

        VBox textBox = new VBox(2);
        Label roleLabel = new Label(label);
        roleLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        roleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        double percentage = total > 0 ? (count * 100.0 / total) : 0;
        Label countLabel = new Label(String.format("%d (%.1f%%)", count, percentage));
        countLabel.setFont(Font.font("System", 11));
        countLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        textBox.getChildren().addAll(roleLabel, countLabel);
        item.getChildren().addAll(dot, textBox);
        legendBox.getChildren().add(item);
    }

    private void addLegendItem(VBox legendBox, String color, String label, int count, int total) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(6);
        dot.setFill(Color.web(color));

        VBox textBox = new VBox(2);
        Label roleLabel = new Label(label);
        roleLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        roleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        double percentage = total > 0 ? (count * 100.0 / total) : 0;
        Label countLabel = new Label(String.format("%d (%.1f%%)", count, percentage));
        countLabel.setFont(Font.font("System", 11));
        countLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        textBox.getChildren().addAll(roleLabel, countLabel);
        item.getChildren().addAll(dot, textBox);
        legendBox.getChildren().add(item);
    }

    @SuppressWarnings("unchecked")
    private VBox createRecentUsersTable(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(2);
        Label title = new Label("Derniers utilisateurs");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Inscriptions récentes");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button viewAllBtn = new Button("Voir tout →");
        viewAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getAccentColor() +
                "; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-size: 13px;");
        viewAllBtn.setOnAction(e -> adminDashboard.showUserManagementViewInCenter());

        header.getChildren().addAll(headerText, viewAllBtn);

        TableView<User> userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPrefHeight(260);
        userTable.setStyle("-fx-background-color: transparent;");

        TableColumn<User, String> avatarCol = new TableColumn<>("");
        avatarCol.setPrefWidth(50);
        avatarCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    StackPane avatarContainer = new StackPane();
                    Circle avatarCircle = new Circle(16);
                    avatarCircle.setFill(Color.web(adminDashboard.getAccentColor()));

                    ImageView img = adminDashboard.loadProfileImage(user, 32);
                    if (img != null) {
                        avatarContainer.getChildren().add(img);
                    } else {
                        Label initials = new Label(adminDashboard.getInitials(user));
                        initials.setFont(Font.font("System", FontWeight.BOLD, 11));
                        initials.setTextFill(Color.WHITE);
                        avatarContainer.getChildren().addAll(avatarCircle, initials);
                    }
                    setGraphic(avatarContainer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<User, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        nameCol.setPrefWidth(160);
        nameCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    Label nameLabel = new Label(user.getNomComplet());
                    nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
                    nameLabel.setTextFill(Color.web(adminDashboard.getTextColor()));
                    setGraphic(nameLabel);
                }
            }
        });

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        emailCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Label emailLabel = new Label(item);
                    emailLabel.setFont(Font.font("System", 12));
                    emailLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
                    setGraphic(emailLabel);
                }
            }
        });

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);
        roleCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                } else {
                    Label roleLabel = new Label(getRoleInFrench(role));
                    roleLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                    roleLabel.setTextFill(Color.WHITE);
                    roleLabel.setPadding(new Insets(4, 10, 4, 10));

                    String bgColor = role.equalsIgnoreCase("admin") ? adminDashboard.getAccentColor() :
                            role.equalsIgnoreCase("organisateur") ? adminDashboard.getSuccessColor() :
                                    adminDashboard.getWarningColor();

                    roleLabel.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12;");
                    setGraphic(roleLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<User, String> dateCol = new TableColumn<>("Inscription");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        dateCol.setPrefWidth(100);
        dateCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Label dateLabel = new Label(item);
                    dateLabel.setFont(Font.font("System", 12));
                    dateLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
                    setGraphic(dateLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        userTable.getColumns().addAll(avatarCol, nameCol, emailCol, roleCol, dateCol);

        List<User> allUsers = userService.getAllUsers();
        List<User> recentUsers = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null)
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        userTable.setItems(FXCollections.observableArrayList(recentUsers));
        userTable.setPlaceholder(new Label("Aucun utilisateur"));

        card.getChildren().addAll(header, userTable);
        return card;
    }


    private String getRoleInFrench(String role) {
        if (role == null) return "";
        switch (role.toLowerCase()) {
            case "admin": return "Administrateur";
            case "organisateur": return "Organisateur";
            case "participant": return "Participant";
            default: return role;
        }
    }
}