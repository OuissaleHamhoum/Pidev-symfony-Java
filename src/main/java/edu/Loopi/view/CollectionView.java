package edu.Loopi.view;

import edu.Loopi.entities.Collection;
import edu.Loopi.entities.User;
import edu.Loopi.services.CollectionService;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class CollectionView {
    private User currentUser;
    private VBox mainLayout;
    private FlowPane cardsContainer;
    private HBox statsBar;
    private CollectionService service = new CollectionService();
    private List<Collection> allData;
    private String selectedImagePath = "";

    public CollectionView(User user) {
        this.currentUser = user;
        this.mainLayout = new VBox(20);
        this.statsBar = new HBox(15);
        createView();
        loadData();
    }

    private void createView() {
        mainLayout.getChildren().clear();
        mainLayout.setPadding(new Insets(30, 40, 30, 40));
        mainLayout.setStyle("-fx-background-color: #f7fee7;");

        VBox heroSection = new VBox(2);
        Label bigTitle = new Label("Tableau de Recyclage");
        bigTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        bigTitle.setTextFill(Color.web("#064e3b"));

        Label subTitle = new Label("Suivez l'impact de vos collectes en temps réel.");
        subTitle.setTextFill(Color.web("#059669"));
        heroSection.getChildren().addAll(bigTitle, subTitle);

        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(0, 0, 10, 0));

        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher une collecte...");
        searchField.setPrefHeight(40);
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-background-radius: 10; -fx-background-color: white; -fx-border-color: #dcfce7; -fx-border-radius: 10;");
        searchField.textProperty().addListener((obs, old, nv) -> filterData(nv));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Nouvelle Collecte");
        applyButtonEffect(addBtn, "#059669", "#047857");
        addBtn.setPrefHeight(40);
        addBtn.setOnAction(e -> openDialog(null));

        actionBar.getChildren().addAll(searchField, spacer, addBtn);

        cardsContainer = new FlowPane();
        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainLayout.getChildren().addAll(heroSection, statsBar, actionBar, scrollPane);
    }

    private void loadData() {
        allData = service.getCollectionsByUser(currentUser.getId());
        updateStats(allData);
        displayCards(allData);
    }

    private void updateStats(List<Collection> visibleData) {
        statsBar.getChildren().clear();
        if (visibleData == null || visibleData.isEmpty()) return;
        double totalGoal = visibleData.stream().mapToDouble(Collection::getGoal_amount).sum();
        double totalCurrent = visibleData.stream().mapToDouble(Collection::getCurrent_amount).sum();

        statsBar.getChildren().addAll(
                createStatCard("Unités", String.valueOf(visibleData.size()), "#10b981"),
                createStatCard("Objectif", String.format("%.0f kg", totalGoal), "#059669"),
                createStatCard("Collecté", String.format("%.1f kg", totalCurrent), "#047857")
        );
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setMinWidth(150);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2); -fx-border-color: " + color + "; -fx-border-width: 0 0 0 4;");
        Label lblT = new Label(title.toUpperCase());
        lblT.setFont(Font.font("System", FontWeight.BOLD, 9)); lblT.setTextFill(Color.web("#6b7280"));
        Label lblV = new Label(value);
        lblV.setFont(Font.font("System", FontWeight.BOLD, 16)); lblV.setTextFill(Color.web(color));
        card.getChildren().addAll(lblT, lblV);
        return card;
    }

    private void filterData(String query) {
        List<Collection> filtered = allData.stream()
                .filter(c -> c.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        c.getMaterial_type().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        displayCards(filtered);
        updateStats(filtered);
    }

    private void displayCards(List<Collection> list) {
        cardsContainer.getChildren().clear();
        if (list != null) {
            for (Collection c : list) cardsContainer.getChildren().add(createStylishCard(c));
        }
    }

    private VBox createStylishCard(Collection c) {
        VBox card = new VBox(0);
        card.setPrefSize(250, 360);
        String normalStyle = "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);";
        card.setStyle(normalStyle);

        card.setOnMouseEntered(e -> {
            scaleNode(card, 1.02);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(5,150,105,0.15), 15, 0, 0, 6);");
        });
        card.setOnMouseExited(e -> {
            scaleNode(card, 1.0);
            card.setStyle(normalStyle);
        });

        StackPane imgContainer = new StackPane();
        ImageView imgView = new ImageView();
        try {
            String path = c.getImage_collection();
            imgView.setImage(new Image((path == null || path.isEmpty()) ? "https://via.placeholder.com/250x120" : "file:" + path));
        } catch (Exception e) { imgView.setImage(new Image("https://via.placeholder.com/250x120")); }
        imgView.setFitWidth(250); imgView.setFitHeight(120);
        Rectangle clip = new Rectangle(250, 120); clip.setArcWidth(30); clip.setArcHeight(30);
        imgView.setClip(clip);
        imgContainer.getChildren().add(imgView);

        VBox content = new VBox(8);
        content.setPadding(new Insets(12));

        Label lblType = new Label(c.getMaterial_type().toUpperCase());
        lblType.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 8;");

        Label lblTitle = new Label(c.getTitle());
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitle.setTextFill(Color.web("#1e293b"));
        lblTitle.setWrapText(true); lblTitle.setMinHeight(35);

        HBox progressInfo = new HBox(4);
        Label currentVal = new Label(String.format("%.1f", c.getCurrent_amount()));
        currentVal.setFont(Font.font("System", FontWeight.BOLD, 12)); currentVal.setTextFill(Color.web("#059669"));
        Label goalVal = new Label("/ " + String.format("%.1f kg", c.getGoal_amount()));
        goalVal.setFont(Font.font("System", 12)); goalVal.setTextFill(Color.web("#94a3b8"));
        progressInfo.getChildren().addAll(currentVal, goalVal);

        double progress = (c.getGoal_amount() > 0) ? (c.getCurrent_amount() / c.getGoal_amount()) : 0;
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(225); pb.setPrefHeight(8);
        pb.setStyle("-fx-accent: #10b981;");

        HBox actions = new HBox(8);
        actions.setPadding(new Insets(5, 0, 0, 0));
        Button btnEdit = new Button("Modifier"); applyButtonEffect(btnEdit, "#f59e0b", "#d97706");
        Button btnDelete = new Button("Supprimer"); applyButtonEffect(btnDelete, "#ef4444", "#dc2626");

        btnEdit.setOnAction(e -> openDialog(c));
        btnDelete.setOnAction(e -> { service.deleteEntity(c.getId_collection()); loadData(); });

        actions.getChildren().addAll(btnEdit, btnDelete);
        content.getChildren().addAll(lblType, lblTitle, progressInfo, pb, actions);
        card.getChildren().addAll(imgContainer, content);
        return card;
    }

    private void openDialog(Collection existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Ajouter" : "Editer");

        VBox form = new VBox(12);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: #f0fdf4;");

        Label header = new Label(existing == null ? "Nouvelle Entrée" : "Modifier Entrée");
        header.setFont(Font.font("System", FontWeight.BOLD, 20));
        header.setTextFill(Color.web("#064e3b"));

        ImageView preview = new ImageView();
        preview.setFitWidth(300); preview.setFitHeight(130);
        Rectangle previewClip = new Rectangle(300, 130); previewClip.setArcWidth(20); previewClip.setArcHeight(20);
        preview.setClip(previewClip);

        if (existing != null && existing.getImage_collection() != null) {
            try { preview.setImage(new Image("file:" + existing.getImage_collection())); } catch (Exception e) {
                preview.setImage(new Image("https://via.placeholder.com/300x130"));
            }
        } else { preview.setImage(new Image("https://via.placeholder.com/300x130")); }

        Button browse = new Button("📸 Photo");
        browse.setStyle("-fx-background-color: white; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: #059669; -fx-cursor: hand;");
        browse.setOnAction(e -> {
            File f = new FileChooser().showOpenDialog(dialog);
            if (f != null) {
                selectedImagePath = f.getAbsolutePath();
                preview.setImage(new Image("file:" + selectedImagePath));
            }
        });

        TextField fTitle = new TextField(existing != null ? existing.getTitle() : "");
        fTitle.setPromptText("Titre...");
        fTitle.setStyle("-fx-background-radius: 8;");

        ComboBox<String> fMat = new ComboBox<>(FXCollections.observableArrayList("Plastique", "Papier", "Verre", "Métal", "Carton"));
        fMat.setMaxWidth(Double.MAX_VALUE);
        if (existing != null) fMat.setValue(existing.getMaterial_type());

        TextField fGoal = new TextField(existing != null ? String.valueOf(existing.getGoal_amount()) : "");
        UnaryOperator<TextFormatter.Change> filter = change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null;
        fGoal.setTextFormatter(new TextFormatter<>(filter));

        selectedImagePath = (existing != null) ? existing.getImage_collection() : "";

        Button save = new Button("Enregistrer");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand;");

        // --- VALIDATION LOGIC RESTORED ---
        save.setOnAction(e -> {
            if (fTitle.getText().trim().isEmpty() || fMat.getValue() == null || fGoal.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs manquants");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez remplir tous les champs avant de confirmer !");
                alert.showAndWait();
                return;
            }
            try {
                Collection c = (existing == null) ? new Collection() : existing;
                c.setTitle(fTitle.getText());
                c.setMaterial_type(fMat.getValue());
                c.setGoal_amount(Double.parseDouble(fGoal.getText()));
                c.setImage_collection(selectedImagePath);
                c.setId_user(currentUser.getId());
                c.setUnit("kg");
                c.setStatus("active");
                if (existing == null) service.addEntity(c); else service.updateEntity(c);
                loadData();
                dialog.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        form.getChildren().addAll(header, preview, browse, new Label("Nom"), fTitle, new Label("Matériau"), fMat, new Label("Objectif (kg)"), fGoal, save);
        dialog.setScene(new Scene(form, 350, 650));
        dialog.showAndWait();
    }

    private void applyButtonEffect(Button btn, String color, String hoverColor) {
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 11;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> { btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 11;"); scaleNode(btn, 1.05); });
        btn.setOnMouseExited(e -> { btn.setStyle(base); scaleNode(btn, 1.0); });
    }

    private void scaleNode(javafx.scene.Node node, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setToX(scale); st.setToY(scale); st.play();
    }

    public VBox getView() { return mainLayout; }
}