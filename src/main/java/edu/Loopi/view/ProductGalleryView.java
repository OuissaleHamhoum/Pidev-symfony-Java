package edu.Loopi.view;

import edu.Loopi.entities.Produit;
import edu.Loopi.services.ProduitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ProductGalleryView {
    private ProduitService ps = new ProduitService();
    private FlowPane flowPane = new FlowPane(25, 25);
    private TextField searchField = new TextField();
    private ComboBox<String> categoryFilter = new ComboBox<>();
    private ComboBox<String> sortCombo = new ComboBox<>();
    private List<Produit> allProducts = new ArrayList<>();

    private final Map<Integer, String> categoryNames = new HashMap<>() {{
        put(1, "Objets décoratifs");
        put(2, "Art mural");
        put(3, "Mobilier artistique");
        put(4, "Installations artistiques");
    }};

    // Map inverse pour le filtrage (nom catégorie -> id)
    private final Map<String, Integer> categoryFilterMap = new HashMap<>() {{
        put("Tous", 0);
        put("Objets décoratifs", 1);
        put("Art mural", 2);
        put("Mobilier artistique", 3);
        put("Installations artistiques", 4);
    }};

    /**
     * This method is called by UserDashboard to display the gallery
     * inside the main window's center area.
     */
    public Parent getView() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #f4f7f6;");

        // --- Header avec titre et barre de recherche/filtres ---
        VBox header = createHeader();
        layout.setTop(header);

        // Charger tous les produits
        allProducts = ps.getAll();

        // --- Gallery Grid ---
        flowPane.setPadding(new Insets(30));
        flowPane.setAlignment(Pos.TOP_CENTER);

        // Afficher les produits filtrés
        applyFilters();

        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        layout.setCenter(scroll);

        return layout;
    }

    /**
     * Crée l'en-tête avec le titre et la barre de recherche/filtres
     */
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(25, 25, 15, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #27ae60, #2ecc71);");

        // Titre
        Label title = new Label("DÉCOUVREZ NOS TRÉSORS");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setAlignment(Pos.CENTER);

        // Sous-titre
        Label subtitle = new Label("Des créations uniques par nos artistes talentueux");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.9);");
        subtitle.setAlignment(Pos.CENTER);

        // Barre de recherche et filtres
        HBox filterBar = createFilterBar();

        header.getChildren().addAll(title, subtitle, filterBar);
        return header;
    }

    /**
     * Crée la barre de recherche et les filtres
     */
    private HBox createFilterBar() {
        HBox filterBar = new HBox(20);
        filterBar.setAlignment(Pos.CENTER);
        filterBar.setPadding(new Insets(15, 0, 5, 0));
        filterBar.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 50; -fx-padding: 10 20;");

        // Icône de recherche
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        // Champ de recherche
        searchField.setPromptText("Rechercher par titre, description...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 20; -fx-background-color: white; -fx-padding: 8 15;");
        searchField.setOnKeyReleased(e -> applyFilters());

        // Séparateur vertical
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: white;");

        // Label Catégorie
        Label filterLabel = new Label("Catégorie:");
        filterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Filtre par catégorie
        categoryFilter.getItems().addAll(categoryFilterMap.keySet());
        categoryFilter.setValue("Tous");
        categoryFilter.setStyle("-fx-background-radius: 20; -fx-background-color: white; -fx-padding: 5 10;");
        categoryFilter.setPrefWidth(180);
        categoryFilter.setOnAction(e -> applyFilters());

        // Séparateur vertical
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: white;");

        // Label Tri
        Label sortLabel = new Label("Trier par:");
        sortLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Options de tri
        sortCombo.getItems().addAll(
                "Plus récent",
                "Moins récent",
                "Nom (A-Z)",
                "Nom (Z-A)"
        );
        sortCombo.setValue("Plus récent");
        sortCombo.setStyle("-fx-background-radius: 20; -fx-background-color: white; -fx-padding: 5 10;");
        sortCombo.setPrefWidth(150);
        sortCombo.setOnAction(e -> applyFilters());

        // Bouton pour réinitialiser les filtres
        Button resetBtn = new Button("✕ Réinitialiser");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
        resetBtn.setOnAction(e -> resetFilters());

        filterBar.getChildren().addAll(
                searchIcon, searchField, sep1,
                filterLabel, categoryFilter, sep2,
                sortLabel, sortCombo, resetBtn
        );

        return filterBar;
    }

    /**
     * Applique les filtres et le tri
     */
    private void applyFilters() {
        flowPane.getChildren().clear();

        String searchText = searchField.getText().toLowerCase().trim();
        String selectedCategory = categoryFilter.getValue();
        String selectedSort = sortCombo.getValue();

        // Filtrage
        List<Produit> filteredList = allProducts.stream()
                .filter(p -> {
                    // Filtre par recherche (titre ou description)
                    if (searchText.isEmpty()) return true;
                    return p.getNom().toLowerCase().contains(searchText) ||
                            p.getDescription().toLowerCase().contains(searchText);
                })
                .filter(p -> {
                    // Filtre par catégorie
                    if (selectedCategory == null || selectedCategory.equals("Tous")) return true;
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

        // Afficher les résultats
        if (filteredList.isEmpty()) {
            showNoResults();
        } else {
            for (Produit p : filteredList) {
                flowPane.getChildren().add(createEnhancedCard(p));
            }
        }
    }

    /**
     * Réinitialise tous les filtres
     */
    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("Tous");
        sortCombo.setValue("Plus récent");
        applyFilters();
    }

    /**
     * Affiche un message quand aucun produit ne correspond
     */
    private void showNoResults() {
        VBox noResults = new VBox(15);
        noResults.setAlignment(Pos.CENTER);
        noResults.setPadding(new Insets(50));
        noResults.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        noResults.setMaxWidth(400);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 48px;");

        Label message = new Label("Aucun produit trouvé");
        message.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label suggestion = new Label("Essayez de modifier vos critères de recherche");
        suggestion.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Button resetBtn = new Button("Voir tous les produits");
        resetBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        resetBtn.setOnAction(e -> resetFilters());

        noResults.getChildren().addAll(icon, message, suggestion, resetBtn);
        flowPane.getChildren().add(noResults);
    }

    /**
     * Keeps the original functionality to open in a standalone window if needed.
     */
    public void show() {
        Stage stage = new Stage();
        stage.setTitle("LOOPI - Boutique Participant");
        Scene scene = new Scene((Region) getView(), 1300, 800);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private VBox createEnhancedCard(Produit p) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-cursor: hand;");

        StackPane imgContainer = new StackPane();
        imgContainer.setPrefSize(260, 180);

        ImageView iv = new ImageView();
        iv.setFitWidth(260);
        iv.setFitHeight(180);

        Rectangle clip = new Rectangle(260, 180);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        iv.setClip(clip);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    iv.setImage(new Image(file.toURI().toString()));
                } else {
                    iv.setImage(new Image("https://via.placeholder.com/260x180/e74c3c/ffffff?text=Image+Non+Trouvée"));
                }
            }
        } catch (Exception e) {
            iv.setImage(new Image("https://via.placeholder.com/260x180/27ae60/ffffff?text=LOOPI"));
        }
        imgContainer.getChildren().add(iv);

        VBox info = new VBox(8);
        info.setPadding(new Insets(15));
        info.setAlignment(Pos.CENTER_LEFT);

        // Nom du produit
        Label name = new Label(p.getNom().toUpperCase());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        name.setWrapText(true);

        // Catégorie avec badge
        String catName = categoryNames.getOrDefault(p.getIdCategorie(), "Autre");
        Label category = new Label(catName);
        category.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #27ae60; -fx-font-size: 11px; " +
                "-fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12;");

        // Description courte
        String shortDesc = p.getDescription().length() > 60
                ? p.getDescription().substring(0, 60) + "..."
                : p.getDescription();
        Label desc = new Label(shortDesc);
        desc.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        desc.setWrapText(true);
        desc.setMaxHeight(40);

        // Bouton Voir détails
        Button viewBtn = new Button("Voir détails");
        viewBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 12px; -fx-padding: 8 0; -fx-background-radius: 8; -fx-cursor: hand;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> new ProductDetailView(p).show());

        info.getChildren().addAll(name, category, desc, viewBtn);
        card.getChildren().addAll(imgContainer, info);

        // Effets de survol
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "-fx-scale-x: 1.02; -fx-scale-y: 1.02;");
            card.setEffect(new javafx.scene.effect.DropShadow(20, javafx.scene.paint.Color.rgb(0,0,0,0.2)));
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("-fx-scale-x: 1.02; -fx-scale-y: 1.02;", ""));
            card.setEffect(null);
        });

        // Clic sur la carte
        card.setOnMouseClicked(e -> new ProductDetailView(p).show());

        return card;
    }

}