package edu.Loopi.view;

import edu.Loopi.entities.Collection;
import edu.Loopi.entities.Donation;
import edu.Loopi.entities.User;
import edu.Loopi.services.CollectionService;
import edu.Loopi.services.DonationService;
import edu.Loopi.services.AIService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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

import java.text.SimpleDateFormat;
import java.util.List;

public class ParticipantCampaignView {
    private VBox container;
    private FlowPane flowPane;
    private CollectionService service = new CollectionService();
    private DonationService donationService = new DonationService();
    private AIService aiService = new AIService();
    private User currentUser;

    public ParticipantCampaignView(User user) {
        this.currentUser = user;
        this.container = new VBox(25);
        this.container.setPadding(new Insets(30));
        this.container.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("🌿 Campagnes de Collecte Actives");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#064e3b"));

        flowPane = new FlowPane();
        flowPane.setHgap(20);
        flowPane.setVgap(25);

        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        container.getChildren().addAll(title, scroll);
        loadData();
    }

    private void loadData() {
        flowPane.getChildren().clear();
        List<Collection> allCampaigns = service.getAllCollections();
        for (Collection c : allCampaigns) {
            flowPane.getChildren().add(createCampaignCard(c));
        }
    }

    private VBox createCampaignCard(Collection c) {
        VBox card = new VBox(10);
        card.setPrefSize(280, 420);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(c.getImage_collection() == null || c.getImage_collection().isEmpty() ? "https://via.placeholder.com/280x140" : "file:" + c.getImage_collection()));
        } catch (Exception e) { img.setImage(new Image("https://via.placeholder.com/280x140")); }
        img.setFitWidth(280); img.setFitHeight(140);
        Rectangle clip = new Rectangle(280, 140); clip.setArcWidth(30); clip.setArcHeight(30);
        img.setClip(clip);

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        Label type = new Label(c.getMaterial_type().toUpperCase());
        type.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 10;");

        Label title = new Label(c.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setWrapText(true); title.setMinHeight(45);

        Label organizer = new Label("👤 Organisé par: " + (c.getUserName() != null ? c.getUserName() : "Inconnu"));
        organizer.setTextFill(Color.web("#475569")); organizer.setFont(Font.font("System", FontWeight.BOLD, 11));

        Label goalLabel = new Label(String.format("Objectif: %.1f %s", c.getGoal_amount(), c.getUnit()));
        goalLabel.setFont(Font.font(11)); goalLabel.setTextFill(Color.GRAY);

        double progress = (c.getGoal_amount() > 0) ? (c.getCurrent_amount() / c.getGoal_amount()) : 0;
        boolean isFinished = c.getCurrent_amount() >= c.getGoal_amount();

        ProgressBar pb = new ProgressBar(Math.min(progress, 1.0));
        pb.setPrefWidth(250); pb.setStyle("-fx-accent: #10b981;");

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

        // --- NEW PROFESSIONAL DEBOUNCE LOGIC ---
        PauseTransition debounceTimer = new PauseTransition(Duration.millis(600));

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double roundedValue = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            valLabel.setText(roundedValue + " " + c.getUnit());
            aiResult.setText("L'IA prépare son analyse... ✨");

            // Reset the timer every time the slider is moved
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
        donationService.addDonation(donation);
        stage.close();
        loadData();
        new Alert(Alert.AlertType.INFORMATION, String.format("Merci ! Votre don de %.1f %s a été enregistré.", amount, c.getUnit())).show();
    }

    public VBox getView() { return container; }
}