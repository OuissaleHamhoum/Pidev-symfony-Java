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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.borders.Border;

import java.io.File;
import java.io.FileOutputStream;

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


    // Détecteur de mots inappropriés
    private BadWordDetector badWordDetector = new BadWordDetector();


    // Composants UI
    private TableView<Produit> productTable;
    private TableView<Feedback> feedbackTable;
    private ComboBox<User> organisateurFilter;
    private ComboBox<String> categoryFilter;
    private TextField searchField;
    private Label totalProductsValue;
    private Label totalFeedbacksValue;
    private Label avgRatingValue;

    private Label flaggedFeedbacksValue;

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

        // Séparateur vertical
        Separator sep3 = new Separator(Orientation.VERTICAL);
        sep3.setStyle("-fx-background-color: " + dashboard.getBorderColor() + ";");

        // Avis signalés (bad words)
        VBox flaggedFeedbacksBox = new VBox(2);
        flaggedFeedbacksBox.setAlignment(Pos.CENTER_LEFT);

        Label flaggedFeedbacksTitle = new Label("⚠️ AVIS SIGNALÉS");
        flaggedFeedbacksTitle.setFont(Font.font("System", FontWeight.BOLD, 10));
        flaggedFeedbacksTitle.setTextFill(Color.web(dashboard.getTextColorMuted()));

        flaggedFeedbacksValue = new Label("0");
        flaggedFeedbacksValue.setFont(Font.font("System", FontWeight.BOLD, 18));
        flaggedFeedbacksValue.setTextFill(Color.web(dashboard.getWarningColor()));

        flaggedFeedbacksBox.getChildren().addAll(flaggedFeedbacksTitle, flaggedFeedbacksValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statsRow.getChildren().addAll(totalProductsBox, sep1, totalFeedbacksBox, sep2, avgRatingBox, sep3, flaggedFeedbacksBox, spacer);

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

        VBox orgFilterBox = new VBox(5);
        Label orgLabel = new Label("Organisateur");
        orgLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        organisateurFilter = new ComboBox<>();
        organisateurFilter.setPromptText("Tous les organisateurs");
        organisateurFilter.setPrefWidth(200);
        organisateurFilter.setStyle("-fx-background-radius: 5; -fx-background-color: white;");
        organisateurFilter.setOnAction(e -> loadProducts());

        // Filtre catégorie

        VBox catFilterBox = new VBox(5);
        Label catLabel = new Label("Catégorie");
        catLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        catLabel.setTextFill(Color.web(dashboard.getTextColor()));

        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Toutes", "Objets décoratifs", "Art mural",
                "Mobilier artistique", "Installations artistiques");
        categoryFilter.setValue("Toutes");
        categoryFilter.setPrefWidth(180);
        categoryFilter.setStyle("-fx-background-radius: 5; -fx-background-color: white;");
        categoryFilter.setOnAction(e -> loadProducts());

        catFilterBox.getChildren().addAll(catLabel, categoryFilter);

        // Bouton pour afficher uniquement les avis signalés
        ToggleButton showFlaggedOnlyBtn = new ToggleButton("🚩 Avis signalés");
        showFlaggedOnlyBtn.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#F3F4F6") + ";" +
                "-fx-text-fill: " + dashboard.getTextColor() + ";" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 5 12;" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;");
        showFlaggedOnlyBtn.setSelected(false);
        showFlaggedOnlyBtn.setOnAction(e -> {
            if (showFlaggedOnlyBtn.isSelected()) {
                showFlaggedOnlyBtn.setStyle("-fx-background-color: " + dashboard.getWarningColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 5 12;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;");
                loadFlaggedFeedbacks();
            } else {
                showFlaggedOnlyBtn.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#F3F4F6") + ";" +
                        "-fx-text-fill: " + dashboard.getTextColor() + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 5 12;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;");
                loadFeedbacks();
            }
        });


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


        filtersRow.getChildren().addAll(searchBox, organisateurFilter, categoryFilter, showFlaggedOnlyBtn, spacer, refreshBtn);
        return filtersRow;
    }

    // ==================== MÉTHODE createProductsTab ====================


    private VBox createProductsTab(boolean isDarkMode) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15, 0, 0, 0));


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

        content.getChildren().add(productTable);


        return content;
    }

    private VBox createFeedbacksTab(boolean isDarkMode) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15, 0, 0, 0));


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

        commentCol.setCellFactory(col -> new TableCell<Feedback, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Détection des mots inappropriés
                    Feedback feedback = getTableView().getItems().get(getIndex());
                    if (badWordDetector.containsBadWords(item)) {
                        setStyle("-fx-background-color: " + dashboard.getWarningColor() + "20; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        setTooltip(new Tooltip("⚠️ Ce commentaire contient des mots inappropriés"));
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        commentCol.setPrefWidth(300);

        TableColumn<Feedback, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDate(cellData.getValue().getDateCommentaire())));
        dateCol.setPrefWidth(120);


        TableColumn<Feedback, String> flaggedCol = new TableColumn<>("Signalé");
        flaggedCol.setCellValueFactory(cellData -> {
            boolean hasBadWords = badWordDetector.containsBadWords(cellData.getValue().getCommentaire());
            return new SimpleStringProperty(hasBadWords ? "⚠️ Oui" : "✓ Non");
        });
        flaggedCol.setCellFactory(col -> new TableCell<Feedback, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.contains("⚠️")) {
                        setStyle("-fx-text-fill: " + dashboard.getWarningColor() + "; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: " + dashboard.getSuccessColor() + ";");
                    }
                }
            }
        });
        flaggedCol.setPrefWidth(80);

        TableColumn<Feedback, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Feedback, Void>() {
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            private final Button reviewBtn = new Button("🔍 Examiner");
            private final Button reportBtn = new Button("📄 Rapport PDF");
            private final HBox pane = new HBox(5, reviewBtn, reportBtn, deleteBtn);


            {
                deleteBtn.setStyle("-fx-background-color: " + dashboard.getDangerColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 3;" +
                        "-fx-cursor: hand;");


                reviewBtn.setStyle("-fx-background-color: " + dashboard.getWarningColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 3;" +
                        "-fx-cursor: hand;");

                reportBtn.setStyle("-fx-background-color: " + dashboard.getAccentColor() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 3;" +
                        "-fx-cursor: hand;");


                deleteBtn.setOnAction(e -> {
                    Feedback f = getTableView().getItems().get(getIndex());
                    confirmDeleteFeedback(f);
                });


                reviewBtn.setOnAction(e -> {
                    Feedback f = getTableView().getItems().get(getIndex());
                    showFeedbackReviewDialog(f);
                });

                reportBtn.setOnAction(e -> {
                    Feedback f = getTableView().getItems().get(getIndex());
                    generateFeedbackReport(f);
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                setGraphic(empty ? null : pane);
            }
        });
        actionsCol.setPrefWidth(200);

        feedbackTable.getColumns().addAll(userCol, productCol, ratingCol, commentCol, dateCol, flaggedCol, actionsCol);

        VBox.setVgrow(feedbackTable, Priority.ALWAYS);
        content.getChildren().add(feedbackTable);


        return content;
    }

    // ==================== MÉTHODE DE GÉNÉRATION DE RAPPORT PDF ====================

    /**
     * Génère un rapport PDF avec toutes les informations du feedback et du produit
     */
    private void generateFeedbackReport(Feedback feedback) {
        try {
            // Récupérer toutes les informations nécessaires
            Produit produit = produitService.getAll().stream()
                    .filter(p -> p.getId() == feedback.getIdProduit())
                    .findFirst().orElse(null);

            if (produit == null) {
                dashboard.showError("Erreur", "Produit non trouvé");
                return;
            }

            User participant = userService.getUserById(feedback.getIdUser());
            User organisateur = userService.getUserById(produit.getIdUser());

            // Boîte de dialogue pour ajouter un avertissement
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ajouter un avertissement");
            dialog.setHeaderText("Ajouter un avertissement au participant");
            dialog.setContentText("Avertissement (optionnel):");

            Optional<String> warningResult = dialog.showAndWait();
            String warning = warningResult.orElse("");

            // Ouvrir une boîte de dialogue de confirmation avec résumé
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Générer rapport PDF");
            confirmAlert.setHeaderText("Confirmer la génération du rapport");

            String content = "Les informations suivantes seront incluses dans le rapport PDF :\n\n" +
                    "📋 INFORMATIONS PARTICIPANT :\n" +
                    "• Nom : " + (participant != null ? participant.getNomComplet() : "Inconnu") + "\n" +
                    "• Email : " + (participant != null ? participant.getEmail() : "Inconnu") + "\n\n" +
                    "💬 INFORMATIONS COMMENTAIRE :\n" +
                    "• Date : " + formatDate(feedback.getDateCommentaire()) + "\n" +
                    "• Note : " + feedback.getNote() + " étoiles\n" +
                    "• Commentaire : " + feedback.getCommentaire() + "\n" +
                    "• Mots inappropriés détectés : " + String.join(", ", badWordDetector.findBadWords(feedback.getCommentaire())) + "\n\n" +
                    "📦 INFORMATIONS PRODUIT :\n" +
                    "• Nom : " + produit.getNom() + "\n" +
                    "• Catégorie : " + categoryNames.getOrDefault(produit.getIdCategorie(), "Inconnue") + "\n" +
                    "• Description : " + produit.getDescription() + "\n" +
                    "• Organisateur : " + (organisateur != null ? organisateur.getNomComplet() : "Inconnu") + "\n\n" +
                    "⚠️ AVERTISSEMENT AJOUTÉ :\n" + (warning.isEmpty() ? "Aucun avertissement" : warning);

            confirmAlert.setContentText(content);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Générer le PDF
                generatePDF(produit, feedback, participant, organisateur, warning);
            }

        } catch (Exception e) {
            e.printStackTrace();
            dashboard.showError("Erreur", "Erreur lors de la génération du rapport : " + e.getMessage());
        }
    }

    /**
     * Génère le fichier PDF avec toutes les informations (sans image)
     */
    private void generatePDF(Produit produit, Feedback feedback, User participant, User organisateur, String warning) {
        try {
            // Créer un FileChooser pour choisir l'emplacement de sauvegarde
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
            );

            String fileName = "rapport_commentaire_" +
                    (participant != null ? participant.getNomComplet().replace(" ", "_") : "inconnu") +
                    "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".pdf";
            fileChooser.setInitialFileName(fileName);

            File file = fileChooser.showSaveDialog(dashboard.getPrimaryStage());
            if (file == null) return;

            // Initialiser le document PDF
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Titre du document
            Paragraph title = new Paragraph("RAPPORT D'AVIS SIGNALÉ")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Informations du rapport
            Paragraph rapportInfo = new Paragraph(
                    "Rapport généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n" +
                            "Généré par : " + currentUser.getNomComplet() + " (" + currentUser.getEmail() + ")")
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(rapportInfo);

            // Section Participant
            Paragraph participantTitle = new Paragraph("1. INFORMATIONS DU PARTICIPANT")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setMarginTop(15);
            document.add(participantTitle);

            Table participantTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            addPdfTableRow(participantTable, "Nom complet:", participant != null ? participant.getNomComplet() : "Inconnu");
            addPdfTableRow(participantTable, "Email:", participant != null ? participant.getEmail() : "Inconnu");
            addPdfTableRow(participantTable, "Rôle:", participant != null ? participant.getRole() : "Inconnu");
            addPdfTableRow(participantTable, "ID Utilisateur:", participant != null ? String.valueOf(participant.getId()) : "Inconnu");

            document.add(participantTable);

            // Section Commentaire
            Paragraph commentTitle = new Paragraph("2. INFORMATIONS DU COMMENTAIRE")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setMarginTop(15);
            document.add(commentTitle);

            Table commentTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            addPdfTableRow(commentTable, "Date du commentaire:", formatDate(feedback.getDateCommentaire()));
            addPdfTableRow(commentTable, "Note attribuée:", feedback.getNote() + " / 5");
            addPdfTableRow(commentTable, "Commentaire:", feedback.getCommentaire());

            List<String> badWords = badWordDetector.findBadWords(feedback.getCommentaire());
            addPdfTableRow(commentTable, "Mots inappropriés détectés:",
                    badWords.isEmpty() ? "Aucun" : String.join(", ", badWords));

            document.add(commentTable);

            // Section Produit
            Paragraph productTitle = new Paragraph("3. INFORMATIONS DU PRODUIT")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setMarginTop(15);
            document.add(productTitle);

            Table productTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            addPdfTableRow(productTable, "Nom du produit:", produit.getNom());
            addPdfTableRow(productTable, "Catégorie:", categoryNames.getOrDefault(produit.getIdCategorie(), "Inconnue"));
            addPdfTableRow(productTable, "Description:", produit.getDescription());
            addPdfTableRow(productTable, "ID Produit:", String.valueOf(produit.getId()));

            document.add(productTable);

            // Section Organisateur
            Paragraph organisateurTitle = new Paragraph("4. INFORMATIONS DE L'ORGANISATEUR")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setMarginTop(15);
            document.add(organisateurTitle);

            Table organisateurTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            addPdfTableRow(organisateurTable, "Nom:", organisateur != null ? organisateur.getNomComplet() : "Inconnu");
            addPdfTableRow(organisateurTable, "Email:", organisateur != null ? organisateur.getEmail() : "Inconnu");

            document.add(organisateurTable);

            // Section Avertissement
            Paragraph warningTitle = new Paragraph("5. AVERTISSEMENT")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.RED)
                    .setMarginTop(15);
            document.add(warningTitle);

            Paragraph warningText = new Paragraph(warning.isEmpty() ? "Aucun avertissement ajouté" : warning)
                    .setFontSize(12)
                    .setBackgroundColor(ColorConstants.YELLOW)
                    .setPadding(10)
                    .setMarginBottom(20);
            document.add(warningText);

            // Pied de page
            Paragraph footer = new Paragraph(
                    "Ce rapport a été généré automatiquement dans le cadre de la modération des commentaires.\n" +
                            "Document confidentiel - À usage interne uniquement.")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            // Fermer le document
            document.close();

            dashboard.showAlert("Succès", "Rapport PDF généré avec succès : " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            dashboard.showError("Erreur", "Erreur lors de la création du PDF : " + e.getMessage());
        }
    }

    /**
     * Helper pour ajouter une ligne au tableau PDF
     */
    private void addPdfTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label).setBold());
        labelCell.setBorder(Border.NO_BORDER);
        table.addCell(labelCell);

        Cell valueCell = new Cell().add(new Paragraph(value != null ? value : ""));
        valueCell.setBorder(Border.NO_BORDER);
        table.addCell(valueCell);
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

    private void loadFlaggedFeedbacks() {
        List<Feedback> allFeedbacks = new ArrayList<>();
        List<Produit> allProducts = produitService.getAll();

        for (Produit p : allProducts) {
            allFeedbacks.addAll(feedbackService.getFeedbacksByProduct(p.getId()));
        }

        List<Feedback> flaggedFeedbacks = allFeedbacks.stream()
                .filter(f -> badWordDetector.containsBadWords(f.getCommentaire()))
                .collect(Collectors.toList());

        feedbackTable.setItems(FXCollections.observableArrayList(flaggedFeedbacks));
    }


    private void updateStats() {
        List<Produit> allProducts = produitService.getAll();
        List<Feedback> allFeedbacks = new ArrayList<>();

        for (Produit p : allProducts) {
            allFeedbacks.addAll(feedbackService.getFeedbacksByProduct(p.getId()));
        }

        long flaggedCount = allFeedbacks.stream()
                .filter(f -> badWordDetector.containsBadWords(f.getCommentaire()))
                .count();


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

        if (flaggedFeedbacksValue != null) {
            flaggedFeedbacksValue.setText(String.valueOf(flaggedCount));
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

    private void showFeedbackReviewDialog(Feedback f) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Examen de l'avis");
        dialog.setHeaderText("Avis signalé - " + f.getUserName());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        // Informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);

        Label userLabel = new Label("Utilisateur:");
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label userValue = new Label(f.getUserName());

        Label productLabel = new Label("Produit:");
        productLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Produit p = produitService.getAll().stream()
                .filter(prod -> prod.getId() == f.getIdProduit())
                .findFirst().orElse(null);
        Label productValue = new Label(p != null ? p.getNom() : "Produit supprimé");

        Label ratingLabel = new Label("Note:");
        ratingLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label ratingValue = new Label("⭐".repeat(f.getNote()));

        Label dateLabel = new Label("Date:");
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label dateValue = new Label(formatDate(f.getDateCommentaire()));

        infoGrid.add(userLabel, 0, 0);
        infoGrid.add(userValue, 1, 0);
        infoGrid.add(productLabel, 0, 1);
        infoGrid.add(productValue, 1, 1);
        infoGrid.add(ratingLabel, 0, 2);
        infoGrid.add(ratingValue, 1, 2);
        infoGrid.add(dateLabel, 0, 3);
        infoGrid.add(dateValue, 1, 3);

        // Commentaire original
        Label commentLabel = new Label("Commentaire original:");
        commentLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextArea originalComment = new TextArea(f.getCommentaire());
        originalComment.setWrapText(true);
        originalComment.setEditable(false);
        originalComment.setPrefRowCount(4);
        originalComment.setStyle("-fx-control-inner-background: #fef9e7; -fx-text-fill: #2c3e50;");

        // Mots détectés
        List<String> badWordsFound = badWordDetector.findBadWords(f.getCommentaire());
        Label detectionLabel = new Label("Mots inappropriés détectés: " + String.join(", ", badWordsFound));
        detectionLabel.setStyle("-fx-text-fill: " + dashboard.getWarningColor() + "; -fx-font-weight: bold;");

        // Actions
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        Button generateReportBtn = new Button("📄 Générer Rapport");
        generateReportBtn.setStyle("-fx-background-color: " + dashboard.getAccentColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;");
        generateReportBtn.setOnAction(e -> {
            generateFeedbackReport(f);
            dialog.close();
        });

        Button deleteBtn = new Button("🗑️ Supprimer l'avis");
        deleteBtn.setStyle("-fx-background-color: " + dashboard.getDangerColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            feedbackService.deleteFeedback(f.getIdFeedback());
            loadFeedbacks();
            updateStats();
            dialog.setResult(ButtonType.CANCEL);
            dialog.close();
            dashboard.showAlert("Succès", "Avis supprimé avec succès !");
        });

        actionButtons.getChildren().addAll(generateReportBtn, deleteBtn);

        content.getChildren().addAll(infoGrid, commentLabel, originalComment, detectionLabel, actionButtons);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
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

    // ==================== CLASSE INTERNE DÉTECTEUR DE BAD WORDS ====================

    /**
     * Détecteur de mots inappropriés dans les commentaires
     */
    private class BadWordDetector {
        // Liste de mots inappropriés (à personnaliser selon vos besoins)
        private final Set<String> badWords = new HashSet<>(Arrays.asList(
                // Insultes en français
                "merde", "putain", "connard", "connasse", "salope", "enculé", "enculée",
                "bâtard", "bâtarde", "fils de pute", "fdp", "ntm", "pd", "pédé",
                "salaud", "salopard", "ordure", "charogne", "raciste", "nazi",
                // Mots grossiers en anglais
                "shit", "fuck", "asshole", "bitch", "bastard", "damn", "hell",
                "cunt", "dick", "pussy", "motherfucker", "mf", "wtf", "stfu",
                // Termes racistes/discriminatoires
                "nègre", "bougnoule", "sale arabe", "sale juif", "sale noir",
                "sale blanc", "sale chinois", "sale rom", "sale gitan",
                // Termes violents/menaces
                "tuer", "crever", "massacre", "assassiner", "violer", "brûler",
                "pendre", "décapiter", "exploser", "bombe", "attentat",
                // Insultes courantes en français
                "abruti", "abrutie", "imbécile", "crétin", "crétine", "débile",
                "idiot", "idiote", "stupide", "con", "conne", "couillon",
                "gros con", "grosse conne", "pauvre type", "trou du cul", "trouduc"
        ));

        // Mots autorisés qui pourraient être mal interprétés
        private final Set<String> allowedWords = new HashSet<>(Arrays.asList(
                "confiture", "confit", "conference", "connaissance", "connu",
                "connue", "connus", "connues", "connection", "connexion"
        ));

        /**
         * Vérifie si un texte contient des mots inappropriés
         */
        public boolean containsBadWords(String text) {
            if (text == null || text.isEmpty()) return false;

            String lowerText = text.toLowerCase();

            // Vérifier chaque mot inapproprié
            for (String badWord : badWords) {
                // Vérifier si le mot est présent en tant que mot entier ou partie de mot
                if (lowerText.contains(badWord)) {
                    // Vérifier que ce n'est pas un mot autorisé
                    boolean isAllowed = false;
                    for (String allowed : allowedWords) {
                        if (lowerText.contains(allowed) && allowed.contains(badWord)) {
                            isAllowed = true;
                            break;
                        }
                    }
                    if (!isAllowed) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Retourne la liste des mots inappropriés trouvés dans le texte
         */
        public List<String> findBadWords(String text) {
            List<String> found = new ArrayList<>();
            if (text == null || text.isEmpty()) return found;

            String lowerText = text.toLowerCase();

            for (String badWord : badWords) {
                if (lowerText.contains(badWord)) {
                    // Vérifier que ce n'est pas un mot autorisé
                    boolean isAllowed = false;
                    for (String allowed : allowedWords) {
                        if (lowerText.contains(allowed) && allowed.contains(badWord)) {
                            isAllowed = true;
                            break;
                        }
                    }
                    if (!isAllowed) {
                        found.add(badWord);
                    }
                }
            }
            return found;
        }

        /**
         * Ajoute un mot à la liste des mots inappropriés
         */
        public void addBadWord(String word) {
            if (word != null && !word.isEmpty()) {
                badWords.add(word.toLowerCase());
            }
        }

        /**
         * Ajoute un mot à la liste des mots autorisés
         */
        public void addAllowedWord(String word) {
            if (word != null && !word.isEmpty()) {
                allowedWords.add(word.toLowerCase());
            }
        }
    }

}