// RecommendationView.java
package edu.Loopi.view;

import edu.Loopi.entities.Produit;
import edu.Loopi.entities.User;
import edu.Loopi.services.RecommendationService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.List;

public class RecommendationView {
    private RecommendationService recommendationService = new RecommendationService();
    private User currentUser;
    private FlowPane recommendationsGrid;
    private ComboBox<String> typeSelector;
    private Label descriptionLabel;

    public RecommendationView() {
        this.currentUser = SessionManager.getCurrentUser();
    }

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8fafc;");

        VBox header = createHeader();
        VBox selectorBox = createSelector();

        recommendationsGrid = new FlowPane(25, 25);
        recommendationsGrid.setPadding(new Insets(20, 0, 20, 0));
        recommendationsGrid.setAlignment(Pos.TOP_CENTER);

        loadRecommendations("pourvous");

        ScrollPane scrollPane = new ScrollPane(recommendationsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: null;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(header, selectorBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return container;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;");

        Label title = new Label("🎯 Recommandations");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0f172a"));

        descriptionLabel = new Label();
        descriptionLabel.setFont(Font.font("Segoe UI", 14));
        descriptionLabel.setTextFill(Color.web("#475569"));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(800);

        header.getChildren().addAll(title, descriptionLabel);
        return header;
    }

    private VBox createSelector() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        HBox selectorRow = new HBox(20);
        selectorRow.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Type de recommandations :");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        typeSelector = new ComboBox<>();
        typeSelector.getItems().addAll(
                "🎯 Pour vous (Basé sur vos favoris)",
                "🔥 Tendance (Nouveautés notées de la semaine)",
                "⭐ Les mieux notés (Tous les temps)",
                "🆕 Nouveautés (Produits de cette semaine)"
        );
        typeSelector.setValue("🎯 Pour vous (Basé sur vos favoris)");
        typeSelector.setStyle("-fx-background-radius: 8; -fx-padding: 8 15; -fx-font-size: 14px;");
        typeSelector.setPrefWidth(400);
        typeSelector.setOnAction(e -> {
            String selected = typeSelector.getValue();
            if (selected.contains("Pour vous")) {
                loadRecommendations("pourvous");
                descriptionLabel.setText("Basé sur l'analyse de vos favoris : catégories favorites et mots-clés similaires");
            } else if (selected.contains("Tendance")) {
                loadRecommendations("tendance");
                descriptionLabel.setText("Les nouveaux produits de la semaine avec leurs notes ⭐");
            } else if (selected.contains("mieux notés")) {
                loadRecommendations("mieuxnotes");
                descriptionLabel.setText("Les produits avec les meilleures notes de tous les temps");
            } else if (selected.contains("Nouveautés")) {
                loadRecommendations("nouveautes");
                descriptionLabel.setText("Tous les produits ajoutés cette semaine ✨");
            }
        });

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            String selected = typeSelector.getValue();
            if (selected.contains("Pour vous")) {
                loadRecommendations("pourvous");
            } else if (selected.contains("Tendance")) {
                loadRecommendations("tendance");
            } else if (selected.contains("mieux notés")) {
                loadRecommendations("mieuxnotes");
            } else if (selected.contains("Nouveautés")) {
                loadRecommendations("nouveautes");
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        selectorRow.getChildren().addAll(label, typeSelector, spacer, refreshBtn);

        HBox infoBox = new HBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(10, 0, 0, 0));

        if (currentUser == null) {
            Label loginWarning = new Label("⚠️ Connectez-vous pour des recommandations personnalisées");
            loginWarning.setStyle("-fx-text-fill: #f97316; -fx-font-weight: bold;");
            infoBox.getChildren().add(loginWarning);
        } else {
            Label statsLabel = new Label("✨ Basé sur votre historique d'activité");
            statsLabel.setStyle("-fx-text-fill: #10b981;");
            infoBox.getChildren().add(statsLabel);
        }

        box.getChildren().addAll(selectorRow, infoBox);
        return box;
    }

    private void loadRecommendations(String type) {
        recommendationsGrid.getChildren().clear();

        List<Produit> recommendations = null;
        int limit = 12;

        try {
            if ("pourvous".equals(type)) {
                if (currentUser != null) {
                    recommendations = recommendationService.getRecommandationsPourVous(currentUser.getId(), limit);
                } else {
                    recommendations = recommendationService.getRecommandationsPopulaires(limit);
                }
            } else if ("tendance".equals(type)) {
                recommendations = recommendationService.getRecommandationsTendance(limit);
            } else if ("mieuxnotes".equals(type)) {
                recommendations = recommendationService.getRecommandationsMieuxNotes(limit);
            } else if ("nouveautes".equals(type)) {
                recommendations = recommendationService.getRecommandationsNouveautes(limit);
            } else {
                recommendations = recommendationService.getRecommandationsPopulaires(limit);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement recommandations: " + e.getMessage());
            e.printStackTrace();
            recommendations = new java.util.ArrayList<>();
        }

        if (recommendations == null || recommendations.isEmpty()) {
            showEmptyState(type);
        } else {
            for (Produit p : recommendations) {
                recommendationsGrid.getChildren().add(createRecommendationCard(p, type));
            }
        }
    }

    private VBox createRecommendationCard(Produit p, String type) {
        VBox card = new VBox(12);
        card.setPrefWidth(260);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3); -fx-cursor: hand;");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(260, 160);
        imageContainer.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 16 16 0 0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(260, 160);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    imageView.setImage(new Image("https://via.placeholder.com/260x160/3b82f6/ffffff?text=LOOPI"));
                }
            } else {
                imageView.setImage(new Image("https://via.placeholder.com/260x160/3b82f6/ffffff?text=LOOPI"));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/260x160/3b82f6/ffffff?text=LOOPI"));
        }
        imageContainer.getChildren().add(imageView);

        String badgeText = "";
        String badgeColor = "";

        if ("pourvous".equals(type)) {
            badgeText = "🎯 Pour vous";
            badgeColor = "#8b5cf6";
        } else if ("tendance".equals(type)) {
            badgeText = "🔥 Tendance";
            badgeColor = "#f97316";
        } else if ("mieuxnotes".equals(type)) {
            badgeText = "⭐ Mieux noté";
            badgeColor = "#f59e0b";
        } else if ("nouveautes".equals(type)) {
            badgeText = "🆕 Nouveauté";
            badgeColor = "#10b981";
        }

        if (!badgeText.isEmpty()) {
            Label recBadge = new Label(badgeText);
            recBadge.setStyle("-fx-background-color: 1" + badgeColor + "; -fx-text-fill: white; " +
                    "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12;");
            StackPane.setAlignment(recBadge, Pos.TOP_LEFT);
            StackPane.setMargin(recBadge, new Insets(8, 0, 0, 8));
            imageContainer.getChildren().add(recBadge);
        }

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(16));
        contentBox.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(p.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0f172a;");
        name.setWrapText(true);

        String catName = getCategoryName(p.getIdCategorie());
        Label category = new Label(catName);
        category.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 12;");

        String shortDesc = p.getDescription().length() > 60
                ? p.getDescription().substring(0, 60) + "..."
                : p.getDescription();
        Label description = new Label(shortDesc);
        description.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        description.setWrapText(true);
        description.setMaxHeight(40);

        Button viewBtn = new Button("Voir détails");
        viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 0; -fx-background-radius: 8; -fx-cursor: hand;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> new ProductDetailView(p).show());

        contentBox.getChildren().addAll(name, category, description, viewBtn);
        card.getChildren().addAll(imageContainer, contentBox);

        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "-fx-scale-x: 1.02; -fx-scale-y: 1.02;");
            card.setEffect(new javafx.scene.effect.DropShadow(20, Color.rgb(59, 130, 246, 0.3)));
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("-fx-scale-x: 1.02; -fx-scale-y: 1.02;", ""));
            card.setEffect(null);
        });

        card.setOnMouseClicked(e -> new ProductDetailView(p).show());

        return card;
    }

    private void showEmptyState(String type) {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));
        emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        emptyBox.setMaxWidth(500);

        String icon = "🎯";
        String message = "Aucune recommandation disponible";
        String suggestion = "";

        if ("pourvous".equals(type)) {
            icon = "💭";
            message = "Pas encore de recommandations personnalisées";
            suggestion = "Ajoutez des produits à vos favoris pour obtenir des suggestions !";
        } else if ("tendance".equals(type)) {
            icon = "🔥";
            message = "Aucune tendance cette semaine";
            suggestion = "Soyez le premier à noter les nouveaux produits !";
        } else if ("mieuxnotes".equals(type)) {
            icon = "⭐";
            message = "Aucun produit mieux noté";
            suggestion = "Soyez le premier à noter des produits !";
        } else if ("nouveautes".equals(type)) {
            icon = "🆕";
            message = "Aucune nouveauté cette semaine";
            suggestion = "Revenez la semaine prochaine pour découvrir les nouveaux produits";
        }

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 64px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label suggestionLabel = new Label(suggestion);
        suggestionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        suggestionLabel.setWrapText(true);
        suggestionLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        emptyBox.getChildren().addAll(iconLabel, messageLabel, suggestionLabel);
        recommendationsGrid.getChildren().add(emptyBox);
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
}