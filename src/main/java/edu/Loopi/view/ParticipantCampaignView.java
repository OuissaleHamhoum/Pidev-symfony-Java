package edu.Loopi.view;

import edu.Loopi.entities.Collection;
import edu.Loopi.entities.Donation;
import edu.Loopi.entities.User;
import edu.Loopi.services.DonationService;
import edu.Loopi.services.CollectionService;
import edu.Loopi.services.AIService;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ParticipantCampaignView {
    private VBox container;
    private FlowPane flowPane;
    private CollectionService service = new CollectionService();
    private DonationService donationService = new DonationService();
    private AIService aiService = new AIService();
    private User currentUser;

    // --- NEW FIELDS FOR INTERACTION ---
    private List<Collection> allData;
    private TextField searchField;
    private ComboBox<String> materialFilterCombo;
    private ComboBox<String> sortComboBox;

    public ParticipantCampaignView(User user) {
        this.currentUser = user;
        this.container = new VBox(20);
        this.container.setPadding(new Insets(30));
        this.container.setStyle("-fx-background-color: #f1f5f9;"); // Cool background

        createView();
        loadData();
    }

    private void createView() {
        // --- FRIENDLY HEADER ---
        VBox headerBox = new VBox(5);
        Label title = new Label("🌿 Tableau de Recyclage");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#064e3b"));

        Label subtitle = new Label("Trouvez une campagne et faites un don pour la planète.");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web("#475569"));
        headerBox.getChildren().addAll(title, subtitle);

        // --- MODERN CONTROL BAR (SEARCH, FILTER, SORT) ---
        HBox controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER_LEFT);
        controlBar.setPadding(new Insets(15));
        controlBar.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher une campagne...");
        searchField.setPrefHeight(40);
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        searchField.textProperty().addListener((obs, old, nv) -> processAndDisplayData());

        materialFilterCombo = new ComboBox<>();
        materialFilterCombo.setItems(FXCollections.observableArrayList("Tous", "Plastique", "Papier", "Verre", "Métal", "Carton"));
        materialFilterCombo.setValue("Tous");
        materialFilterCombo.setPromptText("Matériau");
        materialFilterCombo.setPrefHeight(40);
        materialFilterCombo.setStyle("-fx-background-radius: 10;");
        materialFilterCombo.setOnAction(e -> processAndDisplayData());

        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll(
                "Titre (A-Z)",
                "Titre (Z-A)",
                "Montant Actuel (⬇️)",
                "Montant Actuel (⬆️)",
                "Objectif (⬇️)",
                "Objectif (⬆️)"
        );
        sortComboBox.setPromptText("Trier par...");
        sortComboBox.setPrefHeight(40);
        sortComboBox.setStyle("-fx-background-radius: 10;");
        sortComboBox.setOnAction(e -> processAndDisplayData());

        controlBar.getChildren().addAll(searchField, materialFilterCombo, sortComboBox);

        // --- FLOWPANE FOR CARDS ---
        flowPane = new FlowPane();
        flowPane.setHgap(25);
        flowPane.setVgap(25);
        flowPane.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        container.getChildren().addAll(headerBox, controlBar, scroll);
    }

    private void loadData() {
        allData = service.getAllCollections();
        processAndDisplayData();
    }

    // --- COMBINED FILTER, SORT, AND STATUS LOGIC ---
    private void processAndDisplayData() {
        if (allData == null) return;
        String query = searchField.getText().toLowerCase();
        String material = materialFilterCombo.getValue();
        String sortOption = sortComboBox.getValue();

        List<Collection> processedList = allData.stream()
                // 1. Filter by Name/Description
                .filter(c -> c.getTitle().toLowerCase().contains(query) ||
                        c.getMaterial_type().toLowerCase().contains(query))
                // 2. Filter by Material
                .filter(c -> material == null || material.equals("Tous") || c.getMaterial_type().equalsIgnoreCase(material))
                .collect(Collectors.toList());

        // 3. Sort
        if (sortOption != null) {
            switch (sortOption) {
                case "Titre (A-Z)":
                    processedList.sort(Comparator.comparing(Collection::getTitle, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "Titre (Z-A)":
                    processedList.sort(Comparator.comparing(Collection::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                    break;
                case "Montant Actuel (⬇️)":
                    processedList.sort(Comparator.comparingDouble(Collection::getCurrent_amount));
                    break;
                case "Montant Actuel (⬆️)":
                    processedList.sort(Comparator.comparingDouble(Collection::getCurrent_amount).reversed());
                    break;
                case "Objectif (⬇️)":
                    processedList.sort(Comparator.comparingDouble(Collection::getGoal_amount));
                    break;
                case "Objectif (⬆️)":
                    processedList.sort(Comparator.comparingDouble(Collection::getGoal_amount).reversed());
                    break;
            }
        }

        // 4. Move finished to bottom
        processedList.sort((c1, c2) -> {
            boolean c1Completed = c1.getCurrent_amount() >= c1.getGoal_amount();
            boolean c2Completed = c2.getCurrent_amount() >= c2.getGoal_amount();
            if (c1Completed && !c2Completed) return 1;
            if (!c1Completed && c2Completed) return -1;
            return 0;
        });

        // 5. Display
        flowPane.getChildren().clear();
        for (Collection c : processedList) {
            flowPane.getChildren().add(createCampaignCard(c));
        }
    }

    private VBox createCampaignCard(Collection c) {
        VBox card = new VBox(10);
        card.setPrefSize(290, 430);
        String normalStyle = "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);";
        card.setStyle(normalStyle);

        // --- HOVER EXPAND EFFECT ---
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(5,150,105,0.2), 15, 0, 0, 6);");
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            card.setStyle(normalStyle);
        });

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(c.getImage_collection() == null || c.getImage_collection().isEmpty() ? "https://via.placeholder.com/290x140" : "file:" + c.getImage_collection()));
        } catch (Exception e) { img.setImage(new Image("https://via.placeholder.com/290x140")); }
        img.setFitWidth(290); img.setFitHeight(140);
        Rectangle clip = new Rectangle(290, 140); clip.setArcWidth(30); clip.setArcHeight(30);
        img.setClip(clip);

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        Label type = new Label(c.getMaterial_type().toUpperCase());
        type.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 10;");

        // Title is now displayed
        Label title = new Label(c.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#1e293b"));
        title.setWrapText(true); title.setMinHeight(45);

        Label organizer = new Label("👤 Organisé par: " + (c.getUserName() != null ? c.getUserName() : "Inconnu"));
        organizer.setTextFill(Color.web("#475569")); organizer.setFont(Font.font("System", FontWeight.BOLD, 11));

        Label goalLabel = new Label(String.format("Objectif: %.1f %s", c.getGoal_amount(), c.getUnit()));
        goalLabel.setFont(Font.font(11)); goalLabel.setTextFill(Color.GRAY);

        double progress = (c.getGoal_amount() > 0) ? (c.getCurrent_amount() / c.getGoal_amount()) : 0;
        boolean isFinished = c.getCurrent_amount() >= c.getGoal_amount();

        ProgressBar pb = new ProgressBar(Math.min(progress, 1.0));
        pb.setPrefWidth(260); pb.setStyle("-fx-accent: #10b981;");

        Label stats = new Label(String.format("Collecté: %.1f %s", c.getCurrent_amount(), c.getUnit()));
        stats.setFont(Font.font("System", FontWeight.BOLD, 12));
        stats.setTextFill(Color.web("#059669"));

        Button donateBtn = new Button();
        if (isFinished) {
            donateBtn.setText("✅ Objectif Atteint");
            donateBtn.setDisable(true);
            donateBtn.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        } else {
            donateBtn.setText("❤️ Faire un don");
            donateBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
            donateBtn.setOnAction(e -> openDonationDialog(c));
        }

        content.getChildren().addAll(type, title, organizer, goalLabel, pb, stats, donateBtn);
        card.getChildren().addAll(img, content);
        return card;
    }

    private void openDonationDialog(Collection c) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Faire un don");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: white;");

        HBox campaignHeader = new HBox(20);
        ImageView img = new ImageView();
        try {
            img.setImage(new Image(c.getImage_collection() == null || c.getImage_collection().isEmpty() ? "https://via.placeholder.com/150x100" : "file:" + c.getImage_collection()));
        } catch (Exception e) { img.setImage(new Image("https://via.placeholder.com/150x100")); }
        img.setFitWidth(150); img.setPreserveRatio(true);

        VBox campaignDetails = new VBox(5);
        Label title = new Label(c.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        ProgressBar pb = new ProgressBar(Math.min(c.getCurrent_amount() / c.getGoal_amount(), 1.0));
        pb.setPrefWidth(200); pb.setStyle("-fx-accent: #059669;");

        Label stats = new Label(String.format("%.1f / %.1f %s", c.getCurrent_amount(), c.getGoal_amount(), c.getUnit()));
        campaignDetails.getChildren().addAll(title, pb, stats);
        campaignHeader.getChildren().addAll(img, campaignDetails);

        double remaining = Math.max(0, c.getGoal_amount() - c.getCurrent_amount());
        VBox sliderBox = new VBox(10);
        Label qteLabel = new Label("Quantité à donner (Max: " + String.format("%.1f", remaining) + ") :");
        Slider slider = new Slider(0, remaining, 0);
        slider.setShowTickLabels(true);

        Label valLabel = new Label("0.0 " + c.getUnit());
        valLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        valLabel.setTextFill(Color.web("#059669"));

        VBox aiBox = new VBox(5);
        aiBox.setPadding(new Insets(10));
        aiBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 8; -fx-border-color: #dcfce7;");
        Label aiHeader = new Label("✨ Impact Environnemental (IA):");
        aiHeader.setFont(Font.font("System", FontWeight.BOLD, 12));
        aiHeader.setTextFill(Color.web("#166534"));
        Label aiResult = new Label("Bougez le curseur pour voir l'impact de votre don...");
        aiResult.setFont(Font.font("System", 11));
        aiResult.setWrapText(true);
        aiBox.getChildren().addAll(aiHeader, aiResult);

        // --- DEBOUNCE LOGIC ---
        PauseTransition debounceTimer = new PauseTransition(Duration.millis(600));

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double roundedValue = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            valLabel.setText(roundedValue + " " + c.getUnit());
            aiResult.setText("L'IA prépare son analyse... ✨");

            debounceTimer.setOnFinished(event -> {
                Thread aiThread = new Thread(() -> {
                    try {
                        String impact = aiService.getRecyclingImpact(c.getMaterial_type(), roundedValue, c.getUnit());
                        Platform.runLater(() -> aiResult.setText(impact));
                    } catch (Exception ex) {
                        Platform.runLater(() -> aiResult.setText("Impact non disponible."));
                    }
                });
                aiThread.setDaemon(true);
                aiThread.start();
            });
            debounceTimer.playFromStart();
        });

        Button confirmBtn = new Button("Confirmer le Don ❤️");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        confirmBtn.setOnAction(e -> {
            double roundedValue = Math.round(slider.getValue() * 10.0) / 10.0;
            if(roundedValue > 0) processDonation(c, roundedValue, dialog);
        });
        sliderBox.getChildren().addAll(qteLabel, slider, valLabel, aiBox, confirmBtn);

        VBox historyBox = new VBox(10);
        Label historyTitle = new Label("📜 Donations récentes pour cette collecte :");
        historyTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        historyTitle.setTextFill(Color.web("#1e293b"));

        VBox donationList = new VBox(5);
        List<Donation> campaignDons = donationService.getDonationsByCollection(c.getId_collection());

        if (campaignDons.isEmpty()) {
            Label empty = new Label("Aucun don pour le moment.");
            empty.setTextFill(Color.GRAY);
            donationList.getChildren().add(empty);
        } else {
            for (Donation d : campaignDons) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 8; -fx-background-color: #f1f5f9; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
                Label donor = new Label("👤 " + (d.getUserName() != null ? d.getUserName() : "Donateur"));
                donor.setFont(Font.font("System", FontWeight.BOLD, 13));
                donor.setTextFill(Color.web("#0f172a"));
                Label amtTxt = new Label(" a donné ");
                amtTxt.setTextFill(Color.web("#475569"));
                Label val = new Label(String.format("+%.1f %s", d.getAmount(), c.getUnit()));
                val.setFont(Font.font("System", FontWeight.BOLD, 13));
                val.setTextFill(Color.web("#059669"));
                row.getChildren().addAll(donor, amtTxt, val);
                donationList.getChildren().add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(donationList);
        scroll.setFitToWidth(true); scroll.setPrefHeight(150);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 5;");

        historyBox.getChildren().addAll(historyTitle, scroll);

        layout.getChildren().addAll(campaignHeader, new Separator(), sliderBox, new Separator(), historyBox);
        dialog.setScene(new Scene(layout, 480, 720));
        dialog.show();
    }

    private void processDonation(Collection c, double amount, Stage stage) {
        c.setCurrent_amount(c.getCurrent_amount() + amount);
        service.updateEntity(c);

        Donation donation = new Donation();
        donation.setId_user(currentUser.getId());
        donation.setId_collection(c.getId_collection());
        donation.setAmount(amount);
        donation.setStatus("confirmé");

        // Call service and capture returned badge name
        String unlockedBadge = donationService.addDonation(donation);

        stage.close();
        loadData();

        // Standard Alert
        Alert confirmAlert = new Alert(Alert.AlertType.INFORMATION);
        confirmAlert.setTitle("Don Enregistré");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(String.format("Merci ! Votre don de %.1f %s a été enregistré.", amount, c.getUnit()));
        confirmAlert.showAndWait();

        // --- FIX: Declare imageFile here ---
        if (unlockedBadge != null) {
            String imageFile = "";
            // Map badge name to image file
            switch (unlockedBadge) {
                case "First Timer": imageFile = "first_timer.png"; break;
                case "Plastic Pioneer": imageFile = "plastic_pioneer.png"; break;
                case "Paper Warrior": imageFile = "paper_warrior.png"; break;
                case "Glass Master": imageFile = "glass_master.png"; break;
                case "Metal Titan": imageFile = "metal_titan.png"; break;
                case "Cardboard King": imageFile = "cardboard_king.png"; break;
                default: imageFile = "default_badge.png"; // Fallback image
            }
            // Now imageFile is resolved!
            showBadgeUnlockedAlert(unlockedBadge, imageFile);
        }
    }

    // 4. ADD THIS METHOD TO THE CLASS
    private void showBadgeUnlockedAlert(String badgeName, String imageFileName) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        // Transparent style removes the OS window borders
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #059669; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");

        Label congrats = new Label("🎉 Félicitations !");
        congrats.setFont(Font.font("System", FontWeight.BOLD, 24));
        congrats.setTextFill(Color.web("#065f46"));

        ImageView imageView = new ImageView();
        try {
            java.net.URL imageUrl = getClass().getResource("/images/" + imageFileName);
            if (imageUrl != null) {
                imageView.setImage(new Image(imageUrl.toExternalForm()));
                imageView.setFitWidth(120);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load image: " + imageFileName);
        }

        Label message = new Label("Vous avez débloqué le badge :");
        message.setFont(Font.font("System", 14));
        message.setTextFill(Color.web("#475569"));

        Label badgeLabel = new Label(badgeName);
        badgeLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        badgeLabel.setTextFill(Color.web("#059669"));

        Button okBtn = new Button("Génial !");
        okBtn.setStyle("-fx-background-color: #059669; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14; " +
                "-fx-padding: 10 30; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand;");
        okBtn.setOnAction(e -> dialog.close());

        card.getChildren().addAll(congrats, imageView, message, badgeLabel, okBtn);

        // --- ANIMATION: POP-IN EFFECT ---
        card.setScaleX(0);
        card.setScaleY(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), card);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();

        Scene scene = new Scene(card);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        dialog.initOwner(container.getScene().getWindow());

        dialog.showAndWait();
    }
    public VBox getView() { return container; }
}