package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AnalyticsView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;

    // Filtres pour les graphiques
    private ComboBox<String> growthPeriodFilter;
    private ComboBox<String> growthTypeFilter;
    private ComboBox<String> activityPeriodFilter;
    private ComboBox<String> activityTypeFilter;

    private BarChart<String, Number> growthChart;
    private LineChart<String, Number> activityLineChart;

    public AnalyticsView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    @SuppressWarnings("unchecked")
    public void showAnalyticsView(StackPane mainContentArea, boolean isDarkMode) {
        ScrollPane scrollPane = createAnalyticsView(isDarkMode);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(scrollPane);
    }

    @SuppressWarnings("unchecked")
    private ScrollPane createAnalyticsView(boolean isDarkMode) {
        VBox container = new VBox(24);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + "; -fx-background-radius: 12; -fx-padding: 24;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("Statistiques détaillées");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Analyses et rapports de la plateforme");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Label dateLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setFont(Font.font("System", 14));
        dateLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        header.getChildren().addAll(headerText, dateLabel);

        HBox statsCards = createStatsCards(isDarkMode);

        HBox chartsRow1 = new HBox(20);
        chartsRow1.setAlignment(Pos.CENTER);
        chartsRow1.setPadding(new Insets(20, 0, 0, 0));

        VBox userGrowthCard = createUserGrowthChart(isDarkMode);
        userGrowthCard.setPrefWidth(700);

        VBox roleDistributionCard = createRoleDistributionChart(isDarkMode);
        roleDistributionCard.setPrefWidth(500);

        chartsRow1.getChildren().addAll(userGrowthCard, roleDistributionCard);

        HBox chartsRow2 = new HBox(20);
        chartsRow2.setAlignment(Pos.CENTER);
        chartsRow2.setPadding(new Insets(20, 0, 0, 0));

        VBox activityCard = createActivityChart(isDarkMode);
        activityCard.setPrefWidth(600);

        VBox genderCard = createGenderChart(isDarkMode);
        genderCard.setPrefWidth(600);

        chartsRow2.getChildren().addAll(activityCard, genderCard);

        container.getChildren().addAll(header, statsCards, chartsRow1, chartsRow2);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private HBox createStatsCards(boolean isDarkMode) {
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER);
        cards.setPadding(new Insets(20, 0, 0, 0));

        int totalUsers = userService.countUsers();
        int[] roleStats = userService.getUserStatistics();
        int activeUsers = calculateActiveUsers();

        VBox totalCard = createStatCard("👥", "Total", String.format("%,d", totalUsers), isDarkMode);
        VBox activeCard = createStatCard("✅", "Actifs (30j)", String.format("%,d", activeUsers), isDarkMode);
        VBox adminsCard = createStatCard("👑", "Admins", String.format("%,d", roleStats[0]), isDarkMode);
        VBox organizersCard = createStatCard("🎯", "Organisateurs", String.format("%,d", roleStats[1]), isDarkMode);
        VBox participantsCard = createStatCard("😊", "Participants", String.format("%,d", roleStats[2]), isDarkMode);

        cards.getChildren().addAll(totalCard, activeCard, adminsCard, organizersCard, participantsCard);
        return cards;
    }

    private VBox createStatCard(String icon, String title, String value, boolean isDarkMode) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
        card.setPrefWidth(200);
        card.setPrefHeight(110);

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 28));
        iconLabel.setMinWidth(40);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "20; -fx-background-radius: 8; -fx-padding: 6;");

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        titleBox.getChildren().add(titleLabel);
        topRow.getChildren().addAll(iconLabel, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        card.getChildren().addAll(topRow, valueLabel);
        return card;
    }

    private int calculateActiveUsers() {
        List<User> users = userService.getAllUsers();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        return (int) users.stream()
                .filter(u -> u.getUpdatedAt() != null && u.getUpdatedAt().isAfter(thirtyDaysAgo))
                .count();
    }

    @SuppressWarnings("unchecked")
    private VBox createUserGrowthChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
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

        // Filtres pour la croissance
        growthPeriodFilter = new ComboBox<>();
        growthPeriodFilter.getItems().addAll("30 jours", "90 jours", "6 mois", "12 mois", "24 mois");
        growthPeriodFilter.setValue("12 mois");
        growthPeriodFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        growthPeriodFilter.setPrefWidth(100);
        growthPeriodFilter.setOnAction(e -> updateGrowthChart());

        growthTypeFilter = new ComboBox<>();
        growthTypeFilter.getItems().addAll("Par semaine", "Par mois", "Par trimestre");
        growthTypeFilter.setValue("Par mois");
        growthTypeFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        growthTypeFilter.setPrefWidth(100);
        growthTypeFilter.setOnAction(e -> updateGrowthChart());

        HBox filtersBox = new HBox(10);
        filtersBox.getChildren().addAll(growthPeriodFilter, growthTypeFilter);

        header.getChildren().addAll(headerText, filtersBox);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Période");
        xAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));
        xAxis.setTickLabelFont(Font.font("System", 10));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'inscriptions");
        yAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));
        yAxis.setTickLabelFont(Font.font("System", 10));

        growthChart = new BarChart<>(xAxis, yAxis);
        growthChart.setTitle("");
        growthChart.setLegendVisible(false);
        growthChart.setAnimated(false);
        growthChart.setPrefHeight(250);
        growthChart.setStyle("-fx-background-color: transparent;");
        growthChart.setCategoryGap(10);

        updateGrowthChart();

        card.getChildren().addAll(header, growthChart);
        return card;
    }

    private void updateGrowthChart() {
        growthChart.getData().clear();

        String period = growthPeriodFilter.getValue();
        String type = growthTypeFilter.getValue();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<String, Integer> monthlyData = getFilteredRegistrations(period, type);

        monthlyData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
                    series.getData().add(data);
                });

        growthChart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + adminDashboard.getAccentColor() + ";");
            }
        }
    }

    private Map<String, Integer> getFilteredRegistrations(String period, String type) {
        Map<String, Integer> data = new LinkedHashMap<>();
        List<User> users = userService.getAllUsers();
        LocalDateTime now = LocalDateTime.now();

        int months = getMonthsFromPeriod(period);
        LocalDateTime cutoff = now.minusMonths(months);

        DateTimeFormatter formatter;
        switch (type) {
            case "Par semaine":
                formatter = DateTimeFormatter.ofPattern("'S'ww yyyy");
                break;
            case "Par mois":
                formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                break;
            case "Par trimestre":
                formatter = DateTimeFormatter.ofPattern("'T'Q yyyy");
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        }

        for (User user : users) {
            if (user.getCreatedAt() != null && user.getCreatedAt().isAfter(cutoff)) {
                String key;
                if (type.equals("Par trimestre")) {
                    int month = user.getCreatedAt().getMonthValue();
                    int quarter = (month - 1) / 3 + 1;
                    key = "T" + quarter + " " + user.getCreatedAt().getYear();
                } else {
                    key = user.getCreatedAt().format(formatter);
                }
                data.put(key, data.getOrDefault(key, 0) + 1);
            }
        }

        return data;
    }

    private int getMonthsFromPeriod(String period) {
        switch (period) {
            case "30 jours": return 1;
            case "90 jours": return 3;
            case "6 mois": return 6;
            case "12 mois": return 12;
            case "24 mois": return 24;
            default: return 12;
        }
    }

    @SuppressWarnings("unchecked")
    private VBox createRoleDistributionChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label title = new Label("Répartition par rôle");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        int[] roleStats = userService.getUserStatistics();

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(280, 280);
        pieChart.setStyle("-fx-background-color: transparent;");

        if (roleStats[0] + roleStats[1] + roleStats[2] > 0) {
            pieChart.getData().add(new PieChart.Data("Administrateurs (" + roleStats[0] + ")", roleStats[0]));
            pieChart.getData().add(new PieChart.Data("Organisateurs (" + roleStats[1] + ")", roleStats[1]));
            pieChart.getData().add(new PieChart.Data("Participants (" + roleStats[2] + ")", roleStats[2]));
        }

        card.getChildren().addAll(title, pieChart);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox createActivityChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
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

        // Filtres pour l'activité
        activityPeriodFilter = new ComboBox<>();
        activityPeriodFilter.getItems().addAll("30 jours", "90 jours", "6 mois", "12 mois", "24 mois");
        activityPeriodFilter.setValue("90 jours");
        activityPeriodFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        activityPeriodFilter.setPrefWidth(100);
        activityPeriodFilter.setOnAction(e -> updateActivityLineChart());

        activityTypeFilter = new ComboBox<>();
        activityTypeFilter.getItems().addAll("Par semaine", "Par mois", "Par trimestre");
        activityTypeFilter.setValue("Par mois");
        activityTypeFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        activityTypeFilter.setPrefWidth(100);
        activityTypeFilter.setOnAction(e -> updateActivityLineChart());

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

        activityLineChart = new LineChart<>(xAxis, yAxis);
        activityLineChart.setTitle("");
        activityLineChart.setLegendVisible(false);
        activityLineChart.setAnimated(false);
        activityLineChart.setPrefHeight(220);
        activityLineChart.setStyle("-fx-background-color: transparent;");
        activityLineChart.setCreateSymbols(true);
        activityLineChart.setVerticalGridLinesVisible(false);

        updateActivityLineChart();

        card.getChildren().addAll(header, activityLineChart);
        return card;
    }

    private void updateActivityLineChart() {
        activityLineChart.getData().clear();

        String period = activityPeriodFilter.getValue();
        String type = activityTypeFilter.getValue();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<String, Integer> activityData = getFilteredActivity(period, type);

        activityData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        activityLineChart.getData().add(series);

        series.getNode().setStyle("-fx-stroke: " + adminDashboard.getAccentColor() + "; -fx-stroke-width: 2;");

        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                        "; -fx-background-radius: 4px; -fx-padding: 3px;");
            }
        }
    }

    private Map<String, Integer> getFilteredActivity(String period, String type) {
        Map<String, Integer> activity = new LinkedHashMap<>();
        List<User> users = userService.getAllUsers();
        LocalDateTime now = LocalDateTime.now();

        int months = getMonthsFromPeriod(period);
        LocalDateTime cutoff = now.minusMonths(months);

        DateTimeFormatter formatter;
        switch (type) {
            case "Par semaine":
                formatter = DateTimeFormatter.ofPattern("'S'ww yyyy");
                break;
            case "Par mois":
                formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                break;
            case "Par trimestre":
                formatter = DateTimeFormatter.ofPattern("'T'Q yyyy");
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        }

        for (User user : users) {
            if (user.getUpdatedAt() != null && user.getUpdatedAt().isAfter(cutoff)) {
                String key;
                if (type.equals("Par trimestre")) {
                    int month = user.getUpdatedAt().getMonthValue();
                    int quarter = (month - 1) / 3 + 1;
                    key = "T" + quarter + " " + user.getUpdatedAt().getYear();
                } else {
                    key = user.getUpdatedAt().format(formatter);
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
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
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
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(280, 280);
        pieChart.setStyle("-fx-background-color: transparent;");

        updateGenderChart(pieChart);

        card.getChildren().addAll(header, pieChart);
        return card;
    }

    private void updateGenderChart(PieChart pieChart) {
        List<User> users = userService.getAllUsers();
        int hommes = 0, femmes = 0, autres = 0;

        for (User user : users) {
            if (user.getIdGenre() == 1) hommes++;
            else if (user.getIdGenre() == 2) femmes++;
            else autres++;
        }

        pieChart.getData().clear();


        if (hommes + femmes + autres > 0) {
            if (hommes > 0) pieChart.getData().add(new PieChart.Data("Hommes (" + hommes + ")", hommes));
            if (femmes > 0) pieChart.getData().add(new PieChart.Data("Femmes (" + femmes + ")", femmes));
            if (autres > 0) pieChart.getData().add(new PieChart.Data("Autres (" + autres + ")", autres));
        }
    }
}