package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.Feedback;
import edu.Loopi.entities.Produit;
import edu.Loopi.entities.User;
import edu.Loopi.services.FeedbackService;
import edu.Loopi.services.ProduitService;
import edu.Loopi.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.StringConverter;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ProductManagementView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard dashboard;
    private ProduitService produitService = new ProduitService();
    private FeedbackService feedbackService = new FeedbackService();

    // Composants UI
    private TableView<Produit> productTable;
    private TableView<Feedback> feedbackTable;
    private ComboBox<User> organisateurFilter;
    private ComboBox<String> categoryFilter;
    private TextField searchField;
    private Label totalProductsValue;
    private Label totalFeedbacksValue;
    private Label avgRatingValue;
    private VBox statsBox;
    private TabPane tabPane;

    private final Map<Integer, String> categoryNames = new HashMap<>() {{
        put(1, "Objets décoratifs");
        put(2, "Art mural");
        put(3, "Mobilier artistique");
        put(4, "Installations artistiques");
    }};

    public ProductManagementView(User currentUser, UserService userService, AdminDashboard dashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.dashboard = dashboard;
    }

    public void showProductManagementView(StackPane mainContentArea, boolean isDarkMode) {
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: " + dashboard.getBgColor() + ";");

        // Top section avec titre, statistiques compactes et filtres
        VBox topSection = createTopSection(isDarkMode);
        mainContainer.setTop(topSection);

        // Onglets (prennent tout l'espace restant)
        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent; -fx-tab-border-color: transparent;");

        Tab productsTab = new Tab("📦 Produits");
        productsTab.setContent(createProductsTab(isDarkMode));
        productsTab.setClosable(false);

        Tab feedbacksTab = new Tab("💬 Avis & Commentaires");
        feedbacksTab.setContent(createFeedbacksTab(isDarkMode));
        feedbacksTab.setClosable(false);

        tabPane.getTabs().addAll(productsTab, feedbacksTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        mainContainer.setCenter(tabPane);
        BorderPane.setMargin(tabPane, new Insets(0, 24, 24, 24));

        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(mainContainer);

        // Charger les données initiales
        loadOrganisateurs();
        loadProducts();
        loadFeedbacks();
        updateStats();
    }

    private VBox createTopSection(boolean isDarkMode) {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(20, 24, 10, 24));
        topSection.setStyle("-fx-background-color: " + dashboard.getCardBg() + ";" +
                "-fx-border-color: " + dashboard.getBorderColor() + ";" +
                "-fx-border-width: 0 0 1 0;");

        // Header
        Label title = new Label("📸 Gestion de la Galerie");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(dashboard.getTextColor()));

        Label subtitle = new Label("Consultez et gérez tous les produits et avis des organisateurs");
        subtitle.setFont(Font.font("System", 12));
        subtitle.setTextFill(Color.web(dashboard.getTextColorMuted()));

        // Statistiques compactes horizontales
        HBox statsRow = createCompactStatsRow(isDarkMode);

        // Filtres compacts
        HBox filtersRow = createCompactFiltersRow(isDarkMode);

        topSection.getChildren().addAll(title, subtitle, statsRow, filtersRow);
        return topSection;
    }

    private HBox createCompactStatsRow(boolean isDarkMode) {
        HBox statsRow = new HBox(15);
        statsRow.setPadding(new Insets(10, 0, 5, 0));
        statsRow.setAlignment(Pos.CENTER_LEFT);

        // Total Produits
        VBox totalProductsBox = new VBox(2);
        totalProductsBox.setAlignment(Pos.CENTER_LEFT);

        Label totalProductsTitle = new Label("📦 PRODUITS");
        totalProductsTitle.setFont(Font.font("System", FontWeight.BOLD, 10));
        totalProductsTitle.setTextFill(Color.web(dashboard.getTextColorMuted()));

        totalProductsValue = new Label("0");
        totalProductsValue.setFont(Font.font("System", FontWeight.BOLD, 18));
        totalProductsValue.setTextFill(Color.web(dashboard.getAccentColor()));

        totalProductsBox.getChildren().addAll(totalProductsTitle, totalProductsValue);

        // Séparateur vertical
        Separator sep1 = new Separator(Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: " + dashboard.getBorderColor() + ";");

        // Total Avis
        VBox totalFeedbacksBox = new VBox(2);
        totalFeedbacksBox.setAlignment(Pos.CENTER_LEFT);

        Label totalFeedbacksTitle = new Label("💬 AVIS");
        totalFeedbacksTitle.setFont(Font.font("System", FontWeight.BOLD, 10));
        totalFeedbacksTitle.setTextFill(Color.web(dashboard.getTextColorMuted()));

        totalFeedbacksValue = new Label("0");
        totalFeedbacksValue.setFont(Font.font("System", FontWeight.BOLD, 18));
        totalFeedbacksValue.setTextFill(Color.web(dashboard.getSuccessColor()));

        totalFeedbacksBox.getChildren().addAll(totalFeedbacksTitle, totalFeedbacksValue);

        // Séparateur vertical
        Separator sep2 = new Separator(Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: " + dashboard.getBorderColor() + ";");

        // Note Moyenne
        VBox avgRatingBox = new VBox(2);
        avgRatingBox.setAlignment(Pos.CENTER_LEFT);

        Label avgRatingTitle = new Label("⭐ NOTE MOYENNE");
        avgRatingTitle.setFont(Font.font("System", FontWeight.BOLD, 10));
        avgRatingTitle.setTextFill(Color.web(dashboard.getTextColorMuted()));

        avgRatingValue = new Label("0.0");
        avgRatingValue.setFont(Font.font("System", FontWeight.BOLD, 18));
        avgRatingValue.setTextFill(Color.web("#f1c40f"));

        avgRatingBox.getChildren().addAll(avgRatingTitle, avgRatingValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statsRow.getChildren().addAll(totalProductsBox, sep1, totalFeedbacksBox, sep2, avgRatingBox, spacer);
        return statsRow;
    }

    private HBox createCompactFiltersRow(boolean isDarkMode) {
        HBox filtersRow = new HBox(10);
        filtersRow.setPadding(new Insets(5, 0, 10, 0));
        filtersRow.setAlignment(Pos.CENTER_LEFT);

        // Recherche
        HBox searchBox = new HBox(5);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#F3F4F6") + ";" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 12;");
        searchBox.setPrefWidth(200);

        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("System", 12));

        searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setStyle("-fx-background-color: transparent; -fx-pref-width: 150px; -fx-font-size: 12px;");
        searchField.setOnKeyReleased(e -> loadProducts());

        searchBox.getChildren().addAll(searchIcon, searchField);

        // Filtre organisateur
        organisateurFilter = new ComboBox<>();
        organisateurFilter.setPromptText("Organisateur");
        organisateurFilter.setPrefWidth(140);
        organisateurFilter.setStyle("-fx-background-radius: 20; -fx-font-size: 12px;");
        organisateurFilter.setOnAction(e -> loadProducts());

        // Filtre catégorie
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Toutes", "Objets décoratifs", "Art mural",
                "Mobilier artistique", "Installations artistiques");
        categoryFilter.setValue("Toutes");
        categoryFilter.setPrefWidth(140);
        categoryFilter.setStyle("-fx-background-radius: 20; -fx-font-size: 12px;");
        categoryFilter.setOnAction(e -> loadProducts());

        // Bouton Exporter CSV
        MenuButton exportMenu = new MenuButton("📥 Export");
        exportMenu.setStyle("-fx-background-color: " + dashboard.getAccentColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 5 15;" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;");

        MenuItem exportProductsItem = new MenuItem("Exporter les produits");
        exportProductsItem.setOnAction(e -> exportProductsToCSV());

        MenuItem exportFeedbacksItem = new MenuItem("Exporter les avis");
        exportFeedbacksItem.setOnAction(e -> exportFeedbacksToCSV());

        MenuItem exportAllItem = new MenuItem("Exporter les deux");
        exportAllItem.setOnAction(e -> exportAllToCSV());

        exportMenu.getItems().addAll(exportProductsItem, exportFeedbacksItem, new SeparatorMenuItem(), exportAllItem);

        // Bouton Rafraîchir compact
        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: " + dashboard.getAccentColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;");
        refreshBtn.setTooltip(new Tooltip("Rafraîchir"));
        refreshBtn.setOnAction(e -> {
            loadProducts();
            loadFeedbacks();
            updateStats();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filtersRow.getChildren().addAll(searchBox, organisateurFilter, categoryFilter, spacer, exportMenu, refreshBtn);
        return filtersRow;
    }

    private VBox createStatsBox(boolean isDarkMode) {
        // Cette méthode n'est plus utilisée mais conservée pour compatibilité
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: " + dashboard.getCardBg() + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + dashboard.getBorderColor() + ";" +
                "-fx-border-radius: 10;");
        return box;
    }

    private VBox createStatCard(String title, String value, boolean isDarkMode) {
        // Cette méthode n'est plus utilisée mais conservée pour compatibilité
        VBox card = new VBox(5);
        card.setPadding(new Insets(15, 25, 15, 25));
        card.setStyle("-fx-background-color: " + dashboard.getCardBg() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + dashboard.getBorderColor() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(180);
        return card;
    }

    private VBox createFiltersBox(boolean isDarkMode) {
        // Cette méthode n'est plus utilisée mais conservée pour compatibilité
        VBox filtersBox = new VBox(10);
        filtersBox.setPadding(new Insets(15));
        filtersBox.setStyle("-fx-background-color: " + dashboard.getCardBg() + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + dashboard.getBorderColor() + ";" +
                "-fx-border-radius: 10;");
        return filtersBox;
    }

    private VBox createProductsTab(boolean isDarkMode) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15, 0, 0, 0));

        // Barre d'actions pour l'onglet Produits
        HBox tabActions = new HBox(10);
        tabActions.setAlignment(Pos.CENTER_RIGHT);

        Button exportProductsBtn = new Button("📥 Exporter les produits");
        exportProductsBtn.setStyle("-fx-background-color: " + dashboard.getAccentColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 5 12;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;");
        exportProductsBtn.setOnAction(e -> exportProductsToCSV());

        tabActions.getChildren().add(exportProductsBtn);

        // Tableau des produits (agrandi)
        productTable = new TableView<>();
        productTable.setStyle("-fx-background-color: transparent; -fx-border-color: " + dashboard.getBorderColor() + "; -fx-border-radius: 5;");
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Colonnes
        TableColumn<Produit, String> imageCol = new TableColumn<>("Image");
        imageCol.setCellFactory(col -> new TableCell<Produit, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Produit p = getTableView().getItems().get(getIndex());
                    try {
                        if (p.getImage() != null && !p.getImage().isEmpty()) {
                            File file = new File(p.getImage());
                            if (file.exists()) {
                                imageView.setImage(new Image(file.toURI().toString()));
                            } else {
                                imageView.setImage(null);
                            }
                        } else {
                            imageView.setImage(null);
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                    setGraphic(imageView);
                }
            }
        });
        imageCol.setPrefWidth(80);

        TableColumn<Produit, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom()));
        nameCol.setPrefWidth(150);

        TableColumn<Produit, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));
        descriptionCol.setPrefWidth(250);

        TableColumn<Produit, String> categoryCol = new TableColumn<>("Catégorie");
        categoryCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(categoryNames.getOrDefault(
                        cellData.getValue().getIdCategorie(), "Inconnue")));
        categoryCol.setPrefWidth(120);

        TableColumn<Produit, String> organisateurCol = new TableColumn<>("Organisateur");
        organisateurCol.setCellValueFactory(cellData -> {
            User org = userService.getUserById(cellData.getValue().getIdUser());
            return new SimpleStringProperty(org != null ? org.getNomComplet() : "Inconnu");
        });
        organisateurCol.setPrefWidth(150);

        TableColumn<Produit, Integer> feedbacksCol = new TableColumn<>("Avis");
        feedbacksCol.setCellValueFactory(cellData -> {
            int count = feedbackService.getFeedbacksByProduct(cellData.getValue().getId()).size();
            return new SimpleIntegerProperty(count).asObject();
        });
        feedbacksCol.setPrefWidth(70);

        TableColumn<Produit, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Produit, Void>() {
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            private final Button viewBtn = new Button("👁️ Voir");
            private final HBox pane = new HBox(5, viewBtn, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: " + dashboard.getDangerColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 3;" +
                        "-fx-cursor: hand;");

                viewBtn.setStyle("-fx-background-color: " + dashboard.getAccentColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 3;" +
                        "-fx-cursor: hand;");

                deleteBtn.setOnAction(e -> {
                    Produit p = getTableView().getItems().get(getIndex());
                    confirmDeleteProduct(p);
                });

                viewBtn.setOnAction(e -> {
                    Produit p = getTableView().getItems().get(getIndex());
                    showProductDetails(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        actionsCol.setPrefWidth(150);

        productTable.getColumns().addAll(imageCol, nameCol, descriptionCol, categoryCol,
                organisateurCol, feedbacksCol, actionsCol);

        VBox.setVgrow(productTable, Priority.ALWAYS);
        content.getChildren().addAll(tabActions, productTable);

        return content;
    }

    private VBox createFeedbacksTab(boolean isDarkMode) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15, 0, 0, 0));

        // Barre d'actions pour l'onglet Avis
        HBox tabActions = new HBox(10);
        tabActions.setAlignment(Pos.CENTER_RIGHT);

        Button exportFeedbacksBtn = new Button("📥 Exporter les avis");
        exportFeedbacksBtn.setStyle("-fx-background-color: " + dashboard.getAccentColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 5 12;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;");
        exportFeedbacksBtn.setOnAction(e -> exportFeedbacksToCSV());

        tabActions.getChildren().add(exportFeedbacksBtn);

        // Tableau des feedbacks (agrandi)
        feedbackTable = new TableView<>();
        feedbackTable.setStyle("-fx-background-color: transparent; -fx-border-color: " + dashboard.getBorderColor() + "; -fx-border-radius: 5;");
        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Feedback, String> userCol = new TableColumn<>("Utilisateur");
        userCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserName()));
        userCol.setPrefWidth(150);

        TableColumn<Feedback, String> productCol = new TableColumn<>("Produit");
        productCol.setCellValueFactory(cellData -> {
            Produit p = produitService.getAll().stream()
                    .filter(prod -> prod.getId() == cellData.getValue().getIdProduit())
                    .findFirst().orElse(null);
            return new SimpleStringProperty(p != null ? p.getNom() : "Produit supprimé");
        });
        productCol.setPrefWidth(180);

        TableColumn<Feedback, Integer> ratingCol = new TableColumn<>("Note");
        ratingCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNote()).asObject());
        ratingCol.setCellFactory(col -> new TableCell<Feedback, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("⭐".repeat(item));
                }
            }
        });
        ratingCol.setPrefWidth(100);

        TableColumn<Feedback, String> commentCol = new TableColumn<>("Commentaire");
        commentCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCommentaire()));
        commentCol.setPrefWidth(300);

        TableColumn<Feedback, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDate(cellData.getValue().getDateCommentaire())));
        dateCol.setPrefWidth(120);

        TableColumn<Feedback, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Feedback, Void>() {
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            {
                deleteBtn.setStyle("-fx-background-color: " + dashboard.getDangerColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 3;" +
                        "-fx-cursor: hand;");

                deleteBtn.setOnAction(e -> {
                    Feedback f = getTableView().getItems().get(getIndex());
                    confirmDeleteFeedback(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        actionsCol.setPrefWidth(80);

        feedbackTable.getColumns().addAll(userCol, productCol, ratingCol, commentCol, dateCol, actionsCol);

        VBox.setVgrow(feedbackTable, Priority.ALWAYS);
        content.getChildren().addAll(tabActions, feedbackTable);

        return content;
    }

    private void loadOrganisateurs() {
        List<User> organisateurs = userService.getUsersByRole("organisateur");

        ObservableList<User> organisateurList = FXCollections.observableArrayList(organisateurs);

        // Ajouter une option "Tous"
        organisateurList.add(0, null);

        organisateurFilter.setItems(organisateurList);

        organisateurFilter.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "Tous" : user.getNomComplet();
            }

            @Override
            public User fromString(String string) {
                if (string.equals("Tous")) return null;
                return organisateurList.stream()
                        .filter(u -> u != null && u.getNomComplet().equals(string))
                        .findFirst().orElse(null);
            }
        });

        organisateurFilter.setValue(null);
    }

    private void loadProducts() {
        List<Produit> allProducts = produitService.getAll();
        String searchText = searchField.getText().toLowerCase().trim();
        User selectedOrg = organisateurFilter.getValue();
        String selectedCategory = categoryFilter.getValue();

        List<Produit> filteredProducts = allProducts.stream()
                .filter(p -> {
                    if (selectedOrg != null) {
                        return p.getIdUser() == selectedOrg.getId();
                    }
                    return true;
                })
                .filter(p -> {
                    if (selectedCategory != null && !"Toutes".equals(selectedCategory)) {
                        int catId = categoryNames.entrySet().stream()
                                .filter(e -> e.getValue().equals(selectedCategory))
                                .map(Map.Entry::getKey)
                                .findFirst().orElse(0);
                        return p.getIdCategorie() == catId;
                    }
                    return true;
                })
                .filter(p -> {
                    if (searchText.isEmpty()) return true;
                    return p.getNom().toLowerCase().contains(searchText) ||
                            p.getDescription().toLowerCase().contains(searchText);
                })
                .collect(Collectors.toList());

        productTable.setItems(FXCollections.observableArrayList(filteredProducts));
    }

    private void loadFeedbacks() {
        List<Feedback> allFeedbacks = new ArrayList<>();
        List<Produit> allProducts = produitService.getAll();

        for (Produit p : allProducts) {
            allFeedbacks.addAll(feedbackService.getFeedbacksByProduct(p.getId()));
        }

        feedbackTable.setItems(FXCollections.observableArrayList(allFeedbacks));
    }

    private void updateStats() {
        List<Produit> allProducts = produitService.getAll();
        List<Feedback> allFeedbacks = new ArrayList<>();

        for (Produit p : allProducts) {
            allFeedbacks.addAll(feedbackService.getFeedbacksByProduct(p.getId()));
        }

        if (totalProductsValue != null) {
            totalProductsValue.setText(String.valueOf(allProducts.size()));
        }

        if (totalFeedbacksValue != null) {
            totalFeedbacksValue.setText(String.valueOf(allFeedbacks.size()));
        }

        double avgRating = allFeedbacks.stream()
                .mapToInt(Feedback::getNote)
                .average()
                .orElse(0.0);

        if (avgRatingValue != null) {
            avgRatingValue.setText(String.format("%.1f", avgRating));
        }
    }

    private void confirmDeleteProduct(Produit p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le produit");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer \"" + p.getNom() + "\" ?\nCette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            produitService.supprimerProduit(p.getId());
            loadProducts();
            loadFeedbacks();
            updateStats();
            dashboard.showAlert("Succès", "Produit supprimé avec succès !");
        }
    }

    private void confirmDeleteFeedback(Feedback f) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'avis");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet avis ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            feedbackService.deleteFeedback(f.getIdFeedback());
            loadFeedbacks();
            updateStats();
            dashboard.showAlert("Succès", "Avis supprimé avec succès !");
        }
    }

    private void showProductDetails(Produit p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du produit");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        imageContainer.setPrefHeight(200);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            // Ignorer
        }
        imageContainer.getChildren().add(imageView);

        // Informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));

        Label nameLabel = new Label("Nom:");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label nameValue = new Label(p.getNom());

        Label catLabel = new Label("Catégorie:");
        catLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label catValue = new Label(categoryNames.getOrDefault(p.getIdCategorie(), "Inconnue"));

        Label orgLabel = new Label("Organisateur:");
        orgLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        User org = userService.getUserById(p.getIdUser());
        Label orgValue = new Label(org != null ? org.getNomComplet() : "Inconnu");

        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        TextArea descArea = new TextArea(p.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(4);
        descArea.setMaxWidth(380);

        infoGrid.add(nameLabel, 0, 0);
        infoGrid.add(nameValue, 1, 0);
        infoGrid.add(catLabel, 0, 1);
        infoGrid.add(catValue, 1, 1);
        infoGrid.add(orgLabel, 0, 2);
        infoGrid.add(orgValue, 1, 2);
        infoGrid.add(descLabel, 0, 3);
        infoGrid.add(descArea, 1, 3);

        GridPane.setColumnSpan(descArea, 1);

        content.getChildren().addAll(imageContainer, infoGrid);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null) return "Date inconnue";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }

    // ==================== FONCTIONS D'EXPORT CSV ====================

    private void exportProductsToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les produits");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv")
        );
        fileChooser.setInitialFileName("produits_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(dashboard.getPrimaryStage());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // En-tête CSV
                writer.println("ID;Nom;Description;Catégorie;Organisateur;Email Organisateur;Nombre d'avis");

                // Données
                for (Produit p : productTable.getItems()) {
                    User org = userService.getUserById(p.getIdUser());
                    String orgName = org != null ? org.getNomComplet() : "Inconnu";
                    String orgEmail = org != null ? org.getEmail() : "";
                    int feedbackCount = feedbackService.getFeedbacksByProduct(p.getId()).size();

                    writer.println(String.format("%d;%s;%s;%s;%s;%s;%d",
                            p.getId(),
                            escapeCSV(p.getNom()),
                            escapeCSV(p.getDescription()),
                            categoryNames.getOrDefault(p.getIdCategorie(), "Inconnue"),
                            escapeCSV(orgName),
                            escapeCSV(orgEmail),
                            feedbackCount
                    ));
                }

                dashboard.showAlert("Succès", "Export réussi : " + file.getName());
            } catch (Exception e) {
                dashboard.showError("Erreur", "Erreur lors de l'export : " + e.getMessage());
            }
        }
    }

    private void exportFeedbacksToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les avis");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv")
        );
        fileChooser.setInitialFileName("avis_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(dashboard.getPrimaryStage());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // En-tête CSV
                writer.println("ID;Utilisateur;Email;Produit;Note;Commentaire;Date");

                // Données
                for (Feedback f : feedbackTable.getItems()) {
                    Produit p = produitService.getAll().stream()
                            .filter(prod -> prod.getId() == f.getIdProduit())
                            .findFirst().orElse(null);
                    String productName = p != null ? p.getNom() : "Produit supprimé";

                    writer.println(String.format("%d;%s;%s;%s;%d;%s;%s",
                            f.getIdFeedback(),
                            escapeCSV(f.getUserName()),
                            "", // Email (à ajouter si disponible)
                            escapeCSV(productName),
                            f.getNote(),
                            escapeCSV(f.getCommentaire()),
                            formatDate(f.getDateCommentaire())
                    ));
                }

                dashboard.showAlert("Succès", "Export réussi : " + file.getName());
            } catch (Exception e) {
                dashboard.showError("Erreur", "Erreur lors de l'export : " + e.getMessage());
            }
        }
    }

    private void exportAllToCSV() {
        exportProductsToCSV();
        exportFeedbacksToCSV();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        // Échapper les guillemets et entourer de guillemets si nécessaire
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}