package edu.Loopi.view;

import edu.Loopi.entities.Produit;
import edu.Loopi.services.FavorisService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.effect.DropShadow;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FavorisView {
    private FavorisService favorisService = new FavorisService();
    private Stage stage;
    private FlowPane productsGrid;
    private TextField searchField = new TextField();
    private ComboBox<String> categoryFilter = new ComboBox<>();
    private ComboBox<String> sortCombo = new ComboBox<>();
    private List<Produit> allFavoris = new ArrayList<>();

    private static final String PRIMARY_COLOR = "#4361ee";
    private static final String SECONDARY_COLOR = "#3f37c9";
    private static final String ACCENT_COLOR = "#f72585";
    private static final String BACKGROUND_COLOR = "#f8f9fa";

    private final Map<Integer, String> categoryNames = new HashMap<>() {{
        put(1, "Objets décoratifs");
        put(2, "Art mural");
        put(3, "Mobilier artistique");
        put(4, "Installations artistiques");
    }};

    // Map inverse pour le filtrage (nom catégorie -> id)
    private final Map<String, Integer> categoryFilterMap = new HashMap<>() {{
        put("Toutes les catégories", 0);
        put("Objets décoratifs", 1);
        put("Art mural", 2);
        put("Mobilier artistique", 3);
        put("Installations artistiques", 4);
    }};

    public void show() {
        this.stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Top Bar with filters
        mainContainer.setTop(createTopBar());

        // Center Content
        mainContainer.setCenter(createContent());

        Scene scene = new Scene(mainContainer, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Mes Favoris");
        stage.show();
    }

    public Parent getView() {
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Charger tous les favoris
        loadAllFavoris();

        // Header with title and filters
        VBox header = createHeader();
        mainContainer.setTop(header);

        // Center Content
        mainContainer.setCenter(createContent());

        return mainContainer;
    }

    /**
     * Crée l'en-tête avec le titre et la barre de recherche/filtres
     */
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(25, 30, 15, 30));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");

        // Titre seulement (sans compteur)
        Label title = new Label("❤ Mes Favoris");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_COLOR + ";");

        // Barre de recherche et filtres
        HBox filterBar = createFilterBar();

        header.getChildren().addAll(title, filterBar);
        return header;
    }

    private HBox createTopBar() {
        VBox topBar = new VBox(15);
        topBar.setPadding(new Insets(20, 30, 15, 30));
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");

        // Ligne du haut avec bouton retour et titre
        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY_COLOR + "; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 8 16; " +
                "-fx-border-color: " + PRIMARY_COLOR + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        backBtn.setOnAction(e -> stage.close());

        Label title = new Label("❤️ Mes Favoris");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_COLOR + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(backBtn, title, spacer);

        // Barre de recherche et filtres
        HBox filterBar = createFilterBar();

        topBar.getChildren().addAll(topRow, filterBar);
        return new HBox(topBar); // Wrapper pour compatibilité avec le code existant
    }

    /**
     * Crée la barre de recherche et les filtres
     */
    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(10, 0, 5, 0));

        // Icône de recherche
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");

        // Champ de recherche
        searchField.setPromptText("Rechercher dans vos favoris...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 25; -fx-background-color: #f1f3f5; -fx-padding: 10 15; -fx-border-color: #e9ecef; -fx-border-radius: 25;");
        searchField.setOnKeyReleased(e -> applyFilters());

        // Séparateur vertical
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: #e9ecef;");

        // Label Catégorie
        Label filterLabel = new Label("Catégorie:");
        filterLabel.setStyle("-fx-text-fill: #495057; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Filtre par catégorie
        categoryFilter.getItems().addAll(categoryFilterMap.keySet());
        categoryFilter.setValue("Toutes les catégories");
        categoryFilter.setStyle("-fx-background-radius: 25; -fx-background-color: #f1f3f5; -fx-padding: 8 15; -fx-border-color: #e9ecef; -fx-border-radius: 25;");
        categoryFilter.setPrefWidth(180);
        categoryFilter.setOnAction(e -> applyFilters());

        // Séparateur vertical
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: #e9ecef;");

        // Label Tri
        Label sortLabel = new Label("Trier par:");
        sortLabel.setStyle("-fx-text-fill: #495057; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Options de tri
        sortCombo.getItems().addAll(
                "Plus récent",
                "Moins récent",
                "Nom (A-Z)",
                "Nom (Z-A)"
        );
        sortCombo.setValue("Plus récent");
        sortCombo.setStyle("-fx-background-radius: 25; -fx-background-color: #f1f3f5; -fx-padding: 8 15; -fx-border-color: #e9ecef; -fx-border-radius: 25;");
        sortCombo.setPrefWidth(150);
        sortCombo.setOnAction(e -> applyFilters());

        // Bouton pour réinitialiser les filtres
        Button resetBtn = new Button("✕ Réinitialiser");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_COLOR + "; -fx-border-color: " + ACCENT_COLOR + "; -fx-border-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        resetBtn.setOnAction(e -> resetFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(
                searchIcon, searchField, sep1,
                filterLabel, categoryFilter, sep2,
                sortLabel, sortCombo, spacer, resetBtn
        );

        return filterBar;
    }

    private Parent createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 30, 30, 30));
        content.setAlignment(Pos.TOP_CENTER);

        productsGrid = new FlowPane(25, 25);
        productsGrid.setPadding(new Insets(10));
        productsGrid.setAlignment(Pos.TOP_CENTER);

        // Charger tous les favoris
        loadAllFavoris();

        // Appliquer les filtres
        applyFilters();

        ScrollPane scrollPane = new ScrollPane(productsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: null;");
        scrollPane.setPadding(new Insets(0));

        content.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return content;
    }

    /**
     * Charge tous les favoris depuis le service
     */
    private void loadAllFavoris() {
        allFavoris = favorisService.getFavorisByUser(SessionManager.getCurrentUser().getId());
    }

    /**
     * Applique les filtres et le tri
     */
    private void applyFilters() {
        productsGrid.getChildren().clear();

        String searchText = searchField.getText().toLowerCase().trim();
        String selectedCategory = categoryFilter.getValue();
        String selectedSort = sortCombo.getValue();

        // Filtrage
        List<Produit> filteredList = allFavoris.stream()
                .filter(p -> {
                    // Filtre par recherche (titre ou description)
                    if (searchText.isEmpty()) return true;
                    return p.getNom().toLowerCase().contains(searchText) ||
                            p.getDescription().toLowerCase().contains(searchText);
                })
                .filter(p -> {
                    // Filtre par catégorie
                    if (selectedCategory == null || selectedCategory.equals("Toutes les catégories")) return true;
                    Integer catId = categoryFilterMap.get(selectedCategory);
                    return catId != null && p.getIdCategorie() == catId;
                })
                .collect(Collectors.toList());

        // Tri
        if (selectedSort != null) {
            switch (selectedSort) {
                case "Plus récent":
                    filteredList.sort(Comparator.comparingInt(Produit::getId).reversed());
                    break;
                case "Moins récent":
                    filteredList.sort(Comparator.comparingInt(Produit::getId));
                    break;
                case "Nom (A-Z)":
                    filteredList.sort(Comparator.comparing(p -> p.getNom().toLowerCase()));
                    break;
                case "Nom (Z-A)":
                    filteredList.sort(Comparator.comparing((Produit p) -> p.getNom().toLowerCase()).reversed());
                    break;
            }
        }

        // Afficher les résultats sans compteur
        if (filteredList.isEmpty()) {
            showNoResults();
        } else {
            for (Produit p : filteredList) {
                productsGrid.getChildren().add(createFavorisCard(p));
            }
        }
    }

    /**
     * Réinitialise tous les filtres
     */
    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("Toutes les catégories");
        sortCombo.setValue("Plus récent");
        applyFilters();
    }

    /**
     * Affiche un message quand aucun favori ne correspond
     */
    private void showNoResults() {
        VBox noResults = new VBox(20);
        noResults.setAlignment(Pos.CENTER);
        noResults.setPadding(new Insets(50));
        noResults.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 20, 0, 0, 5);");
        noResults.setMaxWidth(500);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 64px;");

        Label message = new Label("Aucun favori trouvé");
        message.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label suggestion = new Label("Aucun produit ne correspond à vos critères de recherche");
        suggestion.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        suggestion.setWrapText(true);
        suggestion.setTextAlignment(TextAlignment.CENTER);

        Button resetBtn = new Button("Voir tous mes favoris");
        resetBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12 25; -fx-background-radius: 30; " +
                "-fx-cursor: hand;");
        resetBtn.setOnAction(e -> resetFilters());

        noResults.getChildren().addAll(icon, message, suggestion, resetBtn);
        productsGrid.getChildren().add(noResults);
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(25);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));
        emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 20, 0, 0, 5);");
        emptyBox.setMaxWidth(500);

        Label iconLabel = new Label("❤️");
        iconLabel.setFont(Font.font("Segoe UI", 80));
        iconLabel.setTextFill(Color.web(ACCENT_COLOR));

        Label emptyLabel = new Label("Votre liste de favoris est vide");
        emptyLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subLabel = new Label("Explorez nos produits et ajoutez-les à vos favoris en cliquant sur le cœur");
        subLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        subLabel.setWrapText(true);
        subLabel.setTextAlignment(TextAlignment.CENTER);

        Button exploreBtn = new Button("🛒 Explorer la galerie");
        exploreBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 14 30; -fx-background-radius: 30; " +
                "-fx-cursor: hand;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(ACCENT_COLOR, 0.3));
        shadow.setRadius(10);
        exploreBtn.setEffect(shadow);

        exploreBtn.setOnAction(e -> {
            if (stage != null) stage.close();
            ProductGalleryView galleryView = new ProductGalleryView();
            galleryView.show();
        });

        emptyBox.getChildren().addAll(iconLabel, emptyLabel, subLabel, exploreBtn);
        return emptyBox;
    }

    private VBox createFavorisCard(Produit p) {
        VBox card = new VBox(15);
        card.setPrefWidth(260);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); -fx-cursor: hand;");

        // Image Container
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(260, 160);
        imageContainer.setStyle("-fx-background-color: #f1f3f5; -fx-background-radius: 16 16 0 0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    imageView.setImage(img);
                } else {
                    imageView.setImage(new Image("https://via.placeholder.com/260x160/4361ee/ffffff?text=Image"));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/260x160/4361ee/ffffff?text=LOOPI"));
        }
        imageContainer.getChildren().add(imageView);

        // Content Container
        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16, 16, 20, 16));
        contentBox.setAlignment(Pos.CENTER_LEFT);

        // Category Badge
        String catName = getCategoryName(p.getIdCategorie());
        Label category = new Label(catName);
        category.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: " + PRIMARY_COLOR + "; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 20;");

        // Product Name
        Label name = new Label(p.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #212529;");
        name.setWrapText(true);
        name.setMaxWidth(220);

        // Description (shortened)
        String shortDesc = p.getDescription().length() > 60
                ? p.getDescription().substring(0, 60) + "..."
                : p.getDescription();
        Label description = new Label(shortDesc);
        description.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");
        description.setWrapText(true);
        description.setMaxHeight(40);

        // Actions avec les nouveaux styles
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(5, 0, 0, 0));

        // Bouton Voir - Rose clair (#FFB6C1)
        Button viewBtn = new Button("Voir");
        viewBtn.setStyle("-fx-background-color: #FFB6C1; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                "-fx-cursor: hand;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(viewBtn, Priority.ALWAYS);

        // Effet de survol pour le bouton Voir (rose plus foncé)
        viewBtn.setOnMouseEntered(e -> {
            viewBtn.setStyle("-fx-background-color: #FFA0B0; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,182,193,0.5), 10, 0, 0, 2);");
        });
        viewBtn.setOnMouseExited(e -> {
            viewBtn.setStyle("-fx-background-color: #FFB6C1; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand;");
        });

        // Bouton Supprimer - Rouge foncé (#8B0000)
        Button removeBtn = new Button("Supprimer");
        removeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                "-fx-cursor: hand;");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(removeBtn, Priority.ALWAYS);

        // Effet de survol pour le bouton Supprimer (rouge plus clair mais toujours foncé)
        removeBtn.setOnMouseEntered(e -> {
            removeBtn.setStyle("-fx-background-color: #A52A2A; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(139,0,0,0.5), 10, 0, 0, 2);");
        });
        removeBtn.setOnMouseExited(e -> {
            removeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand;");
        });

        viewBtn.setOnAction(e -> {
            ProductDetailView detailView = new ProductDetailView(p);
            detailView.show();
        });

        removeBtn.setOnAction(e -> confirmRemove(p));

        actions.getChildren().addAll(viewBtn, removeBtn);

        contentBox.getChildren().addAll(category, name, description, actions);
        card.getChildren().addAll(imageContainer, contentBox);

        // Hover Effects sur la carte
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(20);

        card.setOnMouseEntered(e -> {
            card.setEffect(shadow);
            card.setStyle(card.getStyle() + "-fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });

        card.setOnMouseExited(e -> {
            card.setEffect(null);
            card.setStyle(card.getStyle().replace("-fx-scale-x: 1.02; -fx-scale-y: 1.02;", ""));
        });

        return card;
    }

    private void confirmRemove(Produit p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous retirer \"" + p.getNom() + "\" de vos favoris ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        dialogPane.getButtonTypes().stream()
                .map(buttonType -> dialogPane.lookupButton(buttonType))
                .forEach(button -> button.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            favorisService.supprimerFavoris(SessionManager.getCurrentUser().getId(), p.getId());
            loadAllFavoris();
            applyFilters();
            showAlert("Succès", "Produit retiré des favoris !");
        }
    }

    private String getCategoryName(int idCat) {
        switch(idCat) {
            case 1: return "Objets décoratifs";
            case 2: return "Art mural";
            case 3: return "Mobilier artistique";
            case 4: return "Installations artistiques";
            default: return "Autre";
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

        alert.showAndWait();
    }
}