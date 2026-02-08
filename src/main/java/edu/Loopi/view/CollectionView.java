package edu.Loopi.view;

import edu.Loopi.entities.Collection;
import edu.Loopi.entities.User;
import edu.Loopi.services.CollectionService;
import javafx.animation.ScaleTransition;
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
import java.util.stream.Collectors;

public class CollectionView {
    private User currentUser;
    private VBox mainLayout;
    private FlowPane cardsContainer;
    private CollectionService service = new CollectionService();
    private List<Collection> allData;
    private String selectedImagePath = "";

    public CollectionView(User user) {
        this.currentUser = user;
        this.mainLayout = new VBox(25);
        createView();
        loadData();
    }

    private void createView() {
        mainLayout.getChildren().clear();
        mainLayout.setPadding(new Insets(40));
        mainLayout.setStyle("-fx-background-color: #f8f9fa;");

        // --- 1. HEADER SECTION ---
        VBox heroSection = new VBox(8);
        Label bigTitle = new Label("Mes Campagnes");
        bigTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        bigTitle.setTextFill(Color.web("#1e293b"));

        Label description = new Label("Gérez et suivez l'avancement de vos collectes.");
        description.setFont(Font.font("Segoe UI", 15));
        description.setTextFill(Color.web("#64748b"));
        heroSection.getChildren().addAll(bigTitle, description);

        // --- 2. SEARCH & ACTION BAR ---
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-background-radius: 10; -fx-padding: 10; -fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        searchField.textProperty().addListener((obs, old, nv) -> filterData(nv));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Nouveau");
        applyButtonEffect(addBtn, "#10b981", "#059669");
        addBtn.setOnAction(e -> openDialog(null));

        actionBar.getChildren().addAll(searchField, spacer, addBtn);

        // --- 3. CARDS AREA ---
        cardsContainer = new FlowPane();
        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainLayout.getChildren().addAll(heroSection, actionBar, scrollPane);
    }

    private void loadData() {
        allData = service.getCollectionsByUser(currentUser.getId());
        displayCards(allData);
    }

    private void filterData(String query) {
        List<Collection> filtered = allData.stream()
                .filter(c -> c.getTitle().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        displayCards(filtered);
    }

    private void displayCards(List<Collection> list) {
        cardsContainer.getChildren().clear();
        if (list != null) {
            for (Collection c : list) {
                cardsContainer.getChildren().add(createStylishCard(c));
            }
        }
    }

    private VBox createStylishCard(Collection c) {
        VBox card = new VBox(0);
        card.setPrefSize(260, 370); // Slightly taller to accommodate the title
        String normalStyle = "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 5);";
        card.setStyle(normalStyle);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 15, 0, 0, 8);");
            scaleNode(card, 1.03);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(normalStyle);
            scaleNode(card, 1.0);
        });

        // Image Section
        StackPane imgContainer = new StackPane();
        ImageView imgView = new ImageView();
        try {
            String path = c.getImage_collection();
            imgView.setImage(new Image((path == null || path.isEmpty()) ? "https://via.placeholder.com/260x140" : "file:" + path));
        } catch (Exception e) {
            imgView.setImage(new Image("https://via.placeholder.com/260x140"));
        }
        imgView.setFitWidth(260); imgView.setFitHeight(140);
        Rectangle clip = new Rectangle(260, 140); clip.setArcWidth(30); clip.setArcHeight(30);
        imgView.setClip(clip);
        imgContainer.getChildren().add(imgView);

        // Content Section
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label lblDate = new Label("📅 " + (c.getCreated_at() != null ? new SimpleDateFormat("dd/MM/yyyy").format(c.getCreated_at()) : "08/02/2026"));
        lblDate.setFont(Font.font(10)); lblDate.setTextFill(Color.web("#94a3b8"));

        // FIXED: Campaign Title
        Label lblTitle = new Label(c.getTitle() != null ? c.getTitle() : "Sans Titre");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setTextFill(Color.web("#1e293b"));
        lblTitle.setWrapText(true);
        lblTitle.setMinHeight(25); // Prevents disappearing

        // Material Pill
        Label lblType = new Label(c.getMaterial_type().toUpperCase());
        lblType.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 9;");

        // Progress
        double progress = (c.getGoal_amount() > 0) ? (c.getCurrent_amount() / c.getGoal_amount()) : 0;
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(230); pb.setPrefHeight(8); pb.setStyle("-fx-accent: #10b981;");

        // Actions
        HBox actions = new HBox(8);
        Button btnEdit = new Button("Modifier");
        applyButtonEffect(btnEdit, "#f59e0b", "#d97706");
        btnEdit.setOnAction(e -> openDialog(c));

        Button btnDelete = new Button("Supprimer");
        applyButtonEffect(btnDelete, "#ef4444", "#dc2626");
        btnDelete.setOnAction(e -> {
            service.deleteEntity(c.getId_collection());
            loadData();
        });

        actions.getChildren().addAll(btnEdit, btnDelete);
        content.getChildren().addAll(lblDate, lblTitle, lblType, pb, actions);
        card.getChildren().addAll(imgContainer, content);

        return card;
    }

    private void openDialog(Collection existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Nouveau" : "Modifier");

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        TextField fTitle = new TextField(existing != null ? existing.getTitle() : "");
        TextField fMat = new TextField(existing != null ? existing.getMaterial_type() : "");
        TextField fGoal = new TextField(existing != null ? String.valueOf(existing.getGoal_amount()) : "");

        Label pathLabel = new Label(existing != null ? existing.getImage_collection() : "Pas d'image");
        pathLabel.setFont(Font.font(10));
        selectedImagePath = (existing != null) ? existing.getImage_collection() : "";

        Button browse = new Button("📁 Image");
        browse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                selectedImagePath = f.getAbsolutePath();
                pathLabel.setText(f.getName());
            }
        });

        Button save = new Button("Confirmer");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");

        save.setOnAction(e -> {
            try {
                Collection c = (existing == null) ? new Collection() : existing;
                c.setTitle(fTitle.getText());
                c.setMaterial_type(fMat.getText());
                c.setGoal_amount(Double.parseDouble(fGoal.getText()));
                c.setImage_collection(selectedImagePath);
                c.setUnit("kg");
                c.setId_user(currentUser.getId());
                c.setStatus("active");

                if (existing == null) service.addEntity(c);
                else service.updateEntity(c);

                loadData();
                dialog.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        form.getChildren().addAll(new Label("Titre"), fTitle, new Label("Matériau"), fMat,
                new Label("Objectif"), fGoal, browse, pathLabel, save);

        dialog.setScene(new Scene(form, 350, 520));
        dialog.showAndWait();
    }

    private void applyButtonEffect(Button btn, String color, String hoverColor) {
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11; -fx-padding: 6 12;";
        String hov = "-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11; -fx-padding: 6 12;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> { btn.setStyle(hov); scaleNode(btn, 1.1); });
        btn.setOnMouseExited(e -> { btn.setStyle(base); scaleNode(btn, 1.0); });
    }

    private void scaleNode(javafx.scene.Node node, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setToX(scale); st.setToY(scale);
        st.play();
    }

    public VBox getView() { return mainLayout; }
}