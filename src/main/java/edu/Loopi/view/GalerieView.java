package edu.Loopi.view;

import edu.Loopi.entities.Produit;
import edu.Loopi.services.ProduitService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.shape.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GalerieView {
    private ProduitService ps = new ProduitService();
    private FlowPane flowPane = new FlowPane();
    private String selectedImagePath = "";

    private final Map<String, Integer> categories = new HashMap<>() {{
        put("Mobilier recyclé", 1);
        put("Décorations écologiques", 2);
        put("Accessoires durables", 3);
        put("Jouets éducatifs", 4);
    }};

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("Ma Galerie de Produits");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button addBtn = new Button("➕ Ajouter un Produit");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-background-radius: 20; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showProductForm(null));

        HBox header = new HBox(title, new Region(), addBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        flowPane.setHgap(25);
        flowPane.setVgap(25);
        refreshData();

        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        container.getChildren().addAll(header, scroll);
        return container;
    }

    private void refreshData() {
        flowPane.getChildren().clear();
        if (SessionManager.getCurrentUser() == null) return;

        int userId = SessionManager.getCurrentUser().getId();
        for (Produit p : ps.getProduitsParOrganisateur(userId)) {
            flowPane.getChildren().add(createProductCard(p));
        }
    }

    private VBox createProductCard(Produit p) {
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setMaxWidth(240);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(210, 140);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(210);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(210, 140);
        clip.setArcWidth(20); clip.setArcHeight(20);
        imageView.setClip(clip);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) imageView.setImage(new Image(file.toURI().toString()));
                else imageView.setImage(new Image("https://via.placeholder.com/210x140?text=Fichier+Perdu"));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/210x140?text=Erreur"));
        }
        imageContainer.getChildren().add(imageView);

        Label name = new Label(p.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #34495e;");

        String catName = "Inconnue";
        for (Map.Entry<String, Integer> entry : categories.entrySet()) {
            if (entry.getValue() == p.getIdCategorie()) { catName = entry.getKey(); break; }
        }
        Label categoryTag = new Label(catName);
        categoryTag.setStyle("-fx-background-color: #ebf5fb; -fx-text-fill: #3498db; -fx-font-size: 10px; " +
                "-fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold;");

        Label description = new Label(p.getDescription());
        description.setWrapText(true);
        description.setMinHeight(40);
        description.setMaxHeight(60);
        description.setAlignment(Pos.TOP_LEFT);
        description.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-font-style: italic;");
        VBox.setMargin(description, new Insets(5, 0, 5, 0));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        Button edit = new Button("✏️");
        edit.setOnAction(e -> showProductForm(p));

        Button del = new Button("🗑️");
        del.setStyle("-fx-text-fill: #e74c3c;");
        del.setOnAction(e -> {
            // --- CONFIRMATION DE SUPPRESSION ---
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le produit ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + p.getNom() + "' ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ps.supprimerProduit(p.getId());
                refreshData();
            }
        });

        actions.getChildren().addAll(edit, del);
        card.getChildren().addAll(imageContainer, name, categoryTag, description, actions);
        return card;
    }

    private void showProductForm(Produit existingProduct) {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle(existingProduct == null ? "Nouveau Produit" : "Modifier Produit");
        ButtonType saveBtnType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nomF = new TextField(existingProduct != null ? existingProduct.getNom() : "");
        TextArea descF = new TextArea(existingProduct != null ? existingProduct.getDescription() : "");
        descF.setPrefRowCount(3);

        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll(categories.keySet());
        if (existingProduct != null) {
            categories.forEach((name, id) -> { if(id == existingProduct.getIdCategorie()) catCombo.setValue(name); });
        } else {
            catCombo.getSelectionModel().selectFirst();
        }

        Button fileBtn = new Button("📂 Image");
        Label pathLabel = new Label(existingProduct != null ? "Image déjà liée" : "Aucune...");

        fileBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File f = fc.showOpenDialog(null);
            if (f != null) {
                selectedImagePath = f.getAbsolutePath();
                pathLabel.setText(f.getName());
            }
        });

        grid.add(new Label("Nom:"), 0, 0); grid.add(nomF, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descF, 1, 1);
        grid.add(new Label("Catégorie:"), 0, 2); grid.add(catCombo, 1, 2);
        grid.add(new Label("Photo:"), 0, 3); grid.add(fileBtn, 1, 3);
        grid.add(pathLabel, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // --- AJOUT DES CONTRÔLES DE SAISIE ---
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String nom = nomF.getText().trim();
            String desc = descF.getText().trim();
            StringBuilder errorMsg = new StringBuilder();

            // Validation Nom
            if (nom.isEmpty()) {
                errorMsg.append("- Le nom ne doit pas être vide.\n");
            } else if (nom.length() < 2 || nom.length() > 30) {
                errorMsg.append("- Le nom doit contenir entre 2 et 30 caractères.\n");
            } else if (!nom.matches("^[a-zA-Z0-9À-ÿ]+( [a-zA-Z0-9À-ÿ]+)*$")) {
                errorMsg.append("- Le nom ne doit pas contenir de symboles spéciaux ni d'espaces multiples.\n");
            }

            // Validation Description
            if (desc.isEmpty()) {
                errorMsg.append("- La description est obligatoire.\n");
            } else if (desc.length() < 10 || desc.length() > 255) {
                errorMsg.append("- La description doit contenir entre 10 et 255 caractères.\n");
            }

            if (errorMsg.length() > 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de validation");
                alert.setHeaderText("Veuillez corriger les champs suivants :");
                alert.setContentText(errorMsg.toString());
                alert.showAndWait();
                event.consume(); // Empêche la fermeture du dialogue
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                int userId = SessionManager.getCurrentUser().getId();
                int catId = categories.get(catCombo.getValue());
                String img = (selectedImagePath.isEmpty() && existingProduct != null) ? existingProduct.getImage() : selectedImagePath;

                if (existingProduct == null) {
                    return new Produit(0, nomF.getText().trim(), descF.getText().trim(), img, catId, userId);
                } else {
                    existingProduct.setNom(nomF.getText().trim());
                    existingProduct.setDescription(descF.getText().trim());
                    existingProduct.setImage(img);
                    existingProduct.setIdCategorie(catId);
                    return existingProduct;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            if (existingProduct == null) ps.ajouterProduit(p);
            else ps.modifierProduit(p);
            selectedImagePath = "";
            refreshData();
        });
    }
}