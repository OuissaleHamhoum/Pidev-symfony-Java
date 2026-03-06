package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.Feedback;
import edu.Loopi.entities.Produit;
import edu.Loopi.entities.User;
import edu.Loopi.services.FeedbackService;
import edu.Loopi.services.ProduitService;
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
import java.util.*;
import java.util.stream.Collectors;

public class GalleryAnalyticsView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;
    private ProduitService produitService = new ProduitService();
    private FeedbackService feedbackService = new FeedbackService();

    // Composants pour les filtres
    private ComboBox<String> periodFilter;
    private ComboBox<String> dateRangeFilter;
    private Label lastUpdateLabel;

    // Maps pour les données
    private final Map<Integer, String> categoryNames = new HashMap<>() {{
        put(1, "Objets décoratifs");
        put(2, "Art mural");
        put(3, "Mobilier artistique");
        put(4, "Installations artistiques");
    }};

    public GalleryAnalyticsView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    public void showGalleryAnalyticsView(StackPane mainContentArea, boolean isDarkMode) {
        ScrollPane scrollPane = createGalleryAnalyticsView(isDarkMode);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(scrollPane);
    }

    private ScrollPane createGalleryAnalyticsView(boolean isDarkMode) {
        VBox container = new VBox(24);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + "; -fx-background-radius: 12; -fx-padding: 24;");

        // Header
        HBox header = createHeader();

        // Filtres globaux
        HBox globalFilters = createGlobalFilters(isDarkMode);

        // Section Produits
        Label productsSectionTitle = new Label("📊 ANALYSE DES PRODUITS");
        productsSectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        productsSectionTitle.setTextFill(Color.web(adminDashboard.getAccentColor()));
        productsSectionTitle.setPadding(new Insets(20, 0, 10, 0));

        // Statistiques produits
        HBox productStats = createProductStats(isDarkMode);

        // Graphiques produits
        HBox productChartsRow1 = new HBox(20);
        productChartsRow1.setAlignment(Pos.CENTER);
        productChartsRow1.setPadding(new Insets(20, 0, 0, 0));

        VBox categoryChartCard = createCategoryChart(isDarkMode);
        categoryChartCard.setPrefWidth(700);

        VBox organizerGenderChartCard = createOrganizerGenderChart(isDarkMode);
        organizerGenderChartCard.setPrefWidth(500);

        productChartsRow1.getChildren().addAll(categoryChartCard, organizerGenderChartCard);

        // Section Avis
        Label feedbacksSectionTitle = new Label("💬 ANALYSE DES AVIS");
        feedbacksSectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        feedbacksSectionTitle.setTextFill(Color.web(adminDashboard.getAccentColor()));
        feedbacksSectionTitle.setPadding(new Insets(30, 0, 10, 0));

        // Statistiques avis
        HBox feedbackStats = createFeedbackStats(isDarkMode);

        // Graphiques avis
        HBox feedbackChartsRow1 = new HBox(20);
        feedbackChartsRow1.setAlignment(Pos.CENTER);
        feedbackChartsRow1.setPadding(new Insets(20, 0, 20, 0));

        VBox participantActivityChartCard = createParticipantActivityChart(isDarkMode);
        participantActivityChartCard.setPrefWidth(700);

        VBox feedbackStatusChartCard = createFeedbackStatusChart(isDarkMode);
        feedbackStatusChartCard.setPrefWidth(500);

        feedbackChartsRow1.getChildren().addAll(participantActivityChartCard, feedbackStatusChartCard);

        // Ajouter tous les éléments au container (sans le graphique de progression)
        container.getChildren().addAll(
                header,
                globalFilters,
                productsSectionTitle,
                productStats,
                productChartsRow1,
                feedbacksSectionTitle,
                feedbackStats,
                feedbackChartsRow1
        );

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("📈 Analyse de la Galerie");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Statistiques détaillées des produits et avis");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        lastUpdateLabel = new Label("Dernière mise à jour: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lastUpdateLabel.setFont(Font.font("System", 12));
        lastUpdateLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        header.getChildren().addAll(headerText, lastUpdateLabel);
        return header;
    }

    private HBox createGlobalFilters(boolean isDarkMode) {
        HBox filtersRow = new HBox(15);
        filtersRow.setPadding(new Insets(15, 0, 0, 0));
        filtersRow.setAlignment(Pos.CENTER_LEFT);

        // Filtre de période
        VBox periodBox = new VBox(5);
        Label periodLabel = new Label("Période d'analyse");
        periodLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        periodLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        periodFilter = new ComboBox<>();
        periodFilter.getItems().addAll("30 derniers jours", "3 derniers mois", "6 derniers mois", "12 derniers mois", "Tout");
        periodFilter.setValue("12 derniers mois");
        periodFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px;");
        periodFilter.setPrefWidth(150);
        periodFilter.setOnAction(e -> refreshAllCharts());

        periodBox.getChildren().addAll(periodLabel, periodFilter);

        // Filtre de plage de dates personnalisée
        VBox dateRangeBox = new VBox(5);
        Label dateRangeLabel = new Label("Plage personnalisée");
        dateRangeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        dateRangeLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        dateRangeFilter = new ComboBox<>();
        dateRangeFilter.getItems().addAll("Toutes les dates", "Ce mois", "Ce trimestre", "Cette année");
        dateRangeFilter.setValue("Toutes les dates");
        dateRangeFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#F3F4F6") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 12px;");
        dateRangeFilter.setPrefWidth(150);
        dateRangeFilter.setOnAction(e -> refreshAllCharts());

        dateRangeBox.getChildren().addAll(dateRangeLabel, dateRangeFilter);

        // Bouton Rafraîchir
        Button refreshBtn = new Button("🔄 Rafraîchir");
        refreshBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            refreshAllCharts();
            lastUpdateLabel.setText("Dernière mise à jour: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filtersRow.getChildren().addAll(periodBox, dateRangeBox, spacer, refreshBtn);
        return filtersRow;
    }

    private HBox createProductStats(boolean isDarkMode) {
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(10, 0, 0, 0));
        statsRow.setAlignment(Pos.CENTER_LEFT);

        List<Produit> allProducts = produitService.getAll();

        // Total produits
        VBox totalBox = createStatBox("📦 Total Produits",
                String.valueOf(allProducts.size()),
                adminDashboard.getAccentColor(),
                isDarkMode);



        // Top organisateur
        Map<Integer, Long> productsByOrganizer = allProducts.stream()
                .collect(Collectors.groupingBy(Produit::getIdUser, Collectors.counting()));

        Integer topOrgId = productsByOrganizer.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String topOrganizer = "N/A";
        if (topOrgId != null) {
            User org = userService.getUserById(topOrgId);
            topOrganizer = org != null ? org.getNomComplet() : "Inconnu";
        }

        VBox topOrgBox = createStatBox("👑 Top Organisateur",
                topOrganizer,
                "#9b59b6",
                isDarkMode);

        statsRow.getChildren().addAll(totalBox, topOrgBox);
        return statsRow;
    }

    private HBox createFeedbackStats(boolean isDarkMode) {
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(10, 0, 0, 0));
        statsRow.setAlignment(Pos.CENTER_LEFT);

        List<Feedback> allFeedbacks = getAllFeedbacks();
        List<Feedback> filteredFeedbacks = filterFeedbacksByDate(allFeedbacks);

        // Total avis
        VBox totalBox = createStatBox("💬 Total Avis",
                String.valueOf(filteredFeedbacks.size()),
                "#2ecc71",
                isDarkMode);

        // Note moyenne
        double avgRating = filteredFeedbacks.stream()
                .mapToInt(Feedback::getNote)
                .average()
                .orElse(0);

        VBox avgBox = createStatBox("⭐ Note Moyenne",
                String.format("%.1f/5", avgRating),
                "#f1c40f",
                isDarkMode);

        // Avis signalés
        long flaggedCount = filteredFeedbacks.stream()
                .filter(f -> containsBadWords(f.getCommentaire()))
                .count();

        double flaggedPercentage = filteredFeedbacks.isEmpty() ? 0 :
                (flaggedCount * 100.0 / filteredFeedbacks.size());

        VBox flaggedBox = createStatBox("⚠️ Avis Signalés",
                String.format("%d (%.1f%%)", flaggedCount, flaggedPercentage),
                adminDashboard.getWarningColor(),
                isDarkMode);

        statsRow.getChildren().addAll(totalBox, avgBox, flaggedBox);
        return statsRow;
    }

    private VBox createStatBox(String title, String value, String color, boolean isDarkMode) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 10; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 10;");
        box.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    @SuppressWarnings("unchecked")
    private VBox createCategoryChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label title = new Label("Répartition des produits par catégorie");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Catégorie");
        xAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre de produits");
        yAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(250);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<Produit> allProducts = produitService.getAll();

        Map<Integer, Long> categoryCounts = allProducts.stream()
                .collect(Collectors.groupingBy(Produit::getIdCategorie, Collectors.counting()));

        long totalProducts = allProducts.size();

        for (Map.Entry<Integer, Long> entry : categoryCounts.entrySet()) {
            String categoryName = categoryNames.getOrDefault(entry.getKey(), "Inconnue");
            series.getData().add(new XYChart.Data<>(categoryName, entry.getValue()));
        }

        chart.getData().add(series);

        // Styliser les barres et ajouter les pourcentages
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + adminDashboard.getAccentColor() + ";");

                // Ajouter un tooltip avec le pourcentage
                double percentage = (data.getYValue().doubleValue() * 100.0) / totalProducts;
                Tooltip tooltip = new Tooltip(String.format("%.1f%%", percentage));
                Tooltip.install(node, tooltip);
            }
        }

        card.getChildren().addAll(title, chart);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox createOrganizerGenderChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label title = new Label("Genre des organisateurs");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(250, 250);
        pieChart.setStyle("-fx-background-color: transparent;");

        List<Produit> allProducts = produitService.getAll();

        Set<Integer> organizerIds = allProducts.stream()
                .map(Produit::getIdUser)
                .collect(Collectors.toSet());

        int hommes = 0, femmes = 0, autres = 0;

        for (Integer orgId : organizerIds) {
            User org = userService.getUserById(orgId);
            if (org != null) {
                if (org.getIdGenre() == 1) hommes++;
                else if (org.getIdGenre() == 2) femmes++;
                else autres++;
            }
        }

        int totalOrganisateurs = hommes + femmes + autres;

        if (totalOrganisateurs > 0) {
            if (hommes > 0) {
                double pourcentageHommes = (hommes * 100.0) / totalOrganisateurs;
                PieChart.Data data = new PieChart.Data(String.format("Hommes (%d - %.1f%%)", hommes, pourcentageHommes), hommes);
                pieChart.getData().add(data);
            }
            if (femmes > 0) {
                double pourcentageFemmes = (femmes * 100.0) / totalOrganisateurs;
                PieChart.Data data = new PieChart.Data(String.format("Femmes (%d - %.1f%%)", femmes, pourcentageFemmes), femmes);
                pieChart.getData().add(data);
            }
            if (autres > 0) {
                double pourcentageAutres = (autres * 100.0) / totalOrganisateurs;
                PieChart.Data data = new PieChart.Data(String.format("Autres (%d - %.1f%%)", autres, pourcentageAutres), autres);
                pieChart.getData().add(data);
            }
        }

        card.getChildren().addAll(title, pieChart);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox createParticipantActivityChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label title = new Label("Activité des participants (nombre d'avis)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Participant");
        xAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));
        xAxis.setTickLabelFont(Font.font("System", 10));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'avis");
        yAxis.setTickLabelFill(Color.web(adminDashboard.getTextColorMuted()));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(250);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<Feedback> allFeedbacks = getAllFeedbacks();
        List<Feedback> filteredFeedbacks = filterFeedbacksByDate(allFeedbacks);

        Map<Integer, Long> feedbacksByUser = filteredFeedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getIdUser, Collectors.counting()));

        long totalFeedbacks = filteredFeedbacks.size();

        // Trier et prendre les 10 participants les plus actifs
        feedbacksByUser.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    User user = userService.getUserById(entry.getKey());
                    String userName = user != null ? user.getPrenom() + " " + user.getNom().charAt(0) + "." : "Inconnu";
                    XYChart.Data<String, Number> data = new XYChart.Data<>(userName, entry.getValue());
                    series.getData().add(data);
                });

        chart.getData().add(series);

        // Styliser les barres et ajouter les pourcentages
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + "#3498db" + ";");

                // Ajouter un tooltip avec le pourcentage
                double percentage = (data.getYValue().doubleValue() * 100.0) / totalFeedbacks;
                Tooltip tooltip = new Tooltip(String.format("%.1f%% des avis", percentage));
                Tooltip.install(node, tooltip);
            }
        }

        card.getChildren().addAll(title, chart);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox createFeedbackStatusChart(boolean isDarkMode) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label title = new Label("Avis normaux vs signalés");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(250, 250);
        pieChart.setStyle("-fx-background-color: transparent;");

        List<Feedback> allFeedbacks = getAllFeedbacks();
        List<Feedback> filteredFeedbacks = filterFeedbacksByDate(allFeedbacks);

        long normalCount = filteredFeedbacks.stream()
                .filter(f -> !containsBadWords(f.getCommentaire()))
                .count();
        long flaggedCount = filteredFeedbacks.stream()
                .filter(f -> containsBadWords(f.getCommentaire()))
                .count();

        int total = filteredFeedbacks.size();

        if (total > 0) {
            if (normalCount > 0) {
                double pourcentageNormal = (normalCount * 100.0) / total;
                PieChart.Data data = new PieChart.Data(String.format("Normaux (%d - %.1f%%)", normalCount, pourcentageNormal), normalCount);
                pieChart.getData().add(data);
            }
            if (flaggedCount > 0) {
                double pourcentageFlagged = (flaggedCount * 100.0) / total;
                PieChart.Data data = new PieChart.Data(String.format("Signalés (%d - %.1f%%)", flaggedCount, pourcentageFlagged), flaggedCount);
                pieChart.getData().add(data);
            }
        }

        card.getChildren().addAll(title, pieChart);
        return card;
    }

    // ============ MÉTHODES UTILITAIRES ============

    private List<Feedback> getAllFeedbacks() {
        List<Feedback> allFeedbacks = new ArrayList<>();
        List<Produit> allProducts = produitService.getAll();
        for (Produit p : allProducts) {
            allFeedbacks.addAll(feedbackService.getFeedbacksByProduct(p.getId()));
        }
        return allFeedbacks;
    }

    private List<Feedback> filterFeedbacksByDate(List<Feedback> feedbacks) {
        String selectedPeriod = periodFilter != null ? periodFilter.getValue() : "12 derniers mois";
        LocalDateTime cutoff = getCutoffDate(selectedPeriod);

        if (cutoff == null) return feedbacks;

        return feedbacks.stream()
                .filter(f -> f.getDateCommentaire() != null && f.getDateCommentaire().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    private LocalDateTime getCutoffDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "30 derniers jours": return now.minusDays(30);
            case "3 derniers mois": return now.minusMonths(3);
            case "6 derniers mois": return now.minusMonths(6);
            case "12 derniers mois": return now.minusMonths(12);
            case "Tout": return null;
            default: return now.minusMonths(12);
        }
    }

    private void refreshAllCharts() {
        showGalleryAnalyticsView(adminDashboard.getMainContentArea(), adminDashboard.isDarkMode());
    }

    // Détecteur de mots inappropriés
    private boolean containsBadWords(String text) {
        if (text == null || text.isEmpty()) return false;

        Set<String> badWords = new HashSet<>(Arrays.asList(
                "merde", "putain", "connard", "connasse", "salope", "enculé", "enculée",
                "bâtard", "bâtarde", "fils de pute", "fdp", "ntm", "pd", "pédé",
                "shit", "fuck", "asshole", "bitch", "bastard", "damn", "hell",
                "cunt", "dick", "pussy", "motherfucker", "mf", "wtf", "stfu"
        ));

        String lowerText = text.toLowerCase();
        for (String badWord : badWords) {
            if (lowerText.contains(badWord)) {
                return true;
            }
        }
        return false;
    }
}