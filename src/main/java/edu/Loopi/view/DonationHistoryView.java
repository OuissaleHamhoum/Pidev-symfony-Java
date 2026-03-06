package edu.Loopi.view;

import edu.Loopi.entities.Donation;
import edu.Loopi.entities.User;
import edu.Loopi.services.DonationService;
import edu.Loopi.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ProgressBar; // For the Progress Bar
import javafx.scene.control.Label;       // You likely already have this, but just in case
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.List;

public class DonationHistoryView {
    private VBox container;
    private DonationService donationService = new DonationService();
    private UserService userService = new UserService();
    private User currentUser;
    private HBox badgesContainer;

    public DonationHistoryView(User user) {
        this.currentUser = user;
        // Fetch fresh user data to get updated badge counts
        this.currentUser = userService.getUserById(user.getId());

        this.container = new VBox(25);
        this.container.setPadding(new Insets(30));
        this.container.setStyle("-fx-background-color: #f1f5f9;");

        Label title = new Label("📋 Mon Historique de Dons");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#064e3b"));

        // --- SECTION 1: BADGES ---
        Label badgesTitle = new Label("🏆 Mes Badges");
        badgesTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        badgesContainer = new HBox(15);
        badgesContainer.setAlignment(Pos.CENTER_LEFT);
        loadBadges(); // Method to fill badgesContainer

        // --- SECTION 2: HISTORY ---
        Label historyTitle = new Label("📜 Historique");
        historyTitle.setFont(Font.font("System", FontWeight.BOLD, 20));

        VBox historyList = new VBox(15);
        historyList.setPadding(new Insets(10));
        loadHistoryCards(historyList);

        ScrollPane scrollPane = new ScrollPane(historyList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(title, badgesTitle, badgesContainer, historyTitle, scrollPane);
    }

    private void loadBadges() {
        badgesContainer.getChildren().clear();

        // Create a temporary list to hold the UI components
        List<VBox> badgeBoxes = new ArrayList<>();

        // Define all badges in a helper method to keep this clean
        badgeBoxes.add(createBadgeBox("First Timer", "first_timer.png", currentUser.isHasDonatedFirstTime() ? 1.0 : 0.0, 1.0, "#dcfce7", "#065f46"));
        badgeBoxes.add(createBadgeBox("Plastic Pioneer", "plastic_pioneer.png", currentUser.getTotalPlastic(), 50.0, "#dbeafe", "#1e40af"));
        badgeBoxes.add(createBadgeBox("Paper Warrior", "paper_warrior.png", currentUser.getTotalPaper(), 30.0, "#fef3c7", "#92400e"));
        badgeBoxes.add(createBadgeBox("Glass Master", "glass_master.png", currentUser.getTotalGlass(), 20.0, "#d1fae5", "#065f46"));
        badgeBoxes.add(createBadgeBox("Metal Titan", "metal_titan.png", currentUser.getTotalMetal(), 15.0, "#e5e7eb", "#374151"));
        badgeBoxes.add(createBadgeBox("Cardboard King", "cardboard_king.png", currentUser.getTotalCardboard(), 25.0, "#f3e8ff", "#6b21a8"));

        // Sort: Unlocked (isUnlocked == true) comes first, then locked
        badgeBoxes.sort((b1, b2) -> {
            boolean u1 = b1.getUserData() != null && (boolean)b1.getUserData();
            boolean u2 = b2.getUserData() != null && (boolean)b2.getUserData();
            return Boolean.compare(u2, u1); // Sort descending (true first)
        });

        badgesContainer.getChildren().addAll(badgeBoxes);
    }

    // Helper method to create the VBox AND return it
    private VBox createBadgeBox(String name, String imageFileName, double current, double goal, String bgColor, String textColor) {
        boolean isUnlocked = current >= goal;
        VBox badgeBox = new VBox(8);
        badgeBox.setUserData(isUnlocked); // Store status here for sorting!

        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.setPrefSize(130, 150);
        badgeBox.setStyle("-fx-background-color: " + (isUnlocked ? bgColor : "#f1f5f9") + "; " +
                "-fx-background-radius: 15; -fx-padding: 10; " +
                "-fx-border-color: " + (isUnlocked ? "transparent" : "#cbd5e1") + "; " +
                "-fx-border-radius: 15;");

        try {
            java.net.URL imageUrl = getClass().getResource("/images/" + imageFileName);
            if (imageUrl != null) {
                ImageView imageView = new ImageView(new Image(imageUrl.toExternalForm()));
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);

                // --- GRAYSCALE FOR LOCKED ---
                if (!isUnlocked) {
                    javafx.scene.effect.ColorAdjust grayscale = new javafx.scene.effect.ColorAdjust();
                    grayscale.setSaturation(-1.0); // Full black and white
                    imageView.setEffect(grayscale);
                    imageView.setOpacity(0.4);
                }
                badgeBox.getChildren().add(imageView);
            } else {
                System.err.println("❌ Image not found: /images/" + imageFileName);
                badgeBox.getChildren().add(new Label("❓"));
            }

            Label nameLabel = new Label(name);
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            nameLabel.setTextFill(Color.web(isUnlocked ? textColor : "#64748b"));

            // --- PROGRESS BAR ---
            VBox progressSection = new VBox(3);
            progressSection.setAlignment(Pos.CENTER);

            ProgressBar pb = new ProgressBar(Math.min(current / goal, 1.0));
            pb.setPrefWidth(90);
            pb.setStyle(isUnlocked ? "-fx-accent: #10b981;" : "-fx-accent: #94a3b8;");

            Label progressLabel = new Label(String.format("%.1f / %.0f kg", current, goal));
            progressLabel.setFont(Font.font(9));
            progressLabel.setTextFill(Color.web("#94a3b8"));

            progressSection.getChildren().addAll(pb, progressLabel);
            badgeBox.getChildren().addAll(nameLabel, progressSection);

        } catch (Exception e) {
            System.err.println("❌ Error creating badge box for " + name + ": " + e.getMessage());
            e.printStackTrace(); // This is crucial to find the error!
        }

        return badgeBox;
    }

    // --- UPDATED METHOD: Robust Image Loading ---
    private void addBadgeToUI(String name, String imageFileName, double current, double goal, String bgColor, String textColor) {
        boolean isUnlocked = current >= goal;

        VBox badgeBox = new VBox(8);
        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.setPrefSize(130, 150);
        badgeBox.setStyle("-fx-background-color: " + (isUnlocked ? bgColor : "#f1f5f9") + "; " +
                "-fx-background-radius: 15; -fx-padding: 10; " +
                "-fx-border-color: " + (isUnlocked ? "transparent" : "#cbd5e1") + "; " +
                "-fx-border-radius: 15;");

        try {
            java.net.URL imageUrl = getClass().getResource("/images/" + imageFileName);
            if (imageUrl != null) {
                ImageView imageView = new ImageView(new Image(imageUrl.toExternalForm()));
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);

                // --- FEATURE 1: GRAYSCALE FOR LOCKED ---
                if (!isUnlocked) {
                    javafx.scene.effect.ColorAdjust grayscale = new javafx.scene.effect.ColorAdjust();
                    grayscale.setSaturation(-1.0); // Full black and white
                    imageView.setEffect(grayscale);
                    imageView.setOpacity(0.4);
                }
                badgeBox.getChildren().add(imageView);
            }

            Label nameLabel = new Label(name);
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            nameLabel.setTextFill(Color.web(isUnlocked ? textColor : "#64748b"));

            // --- FEATURE 2: PROGRESS BAR ---
            VBox progressSection = new VBox(3);
            progressSection.setAlignment(Pos.CENTER);

            ProgressBar pb = new ProgressBar(Math.min(current / goal, 1.0));
            pb.setPrefWidth(90);
            pb.setStyle(isUnlocked ? "-fx-accent: #10b981;" : "-fx-accent: #94a3b8;");

            Label progressLabel = new Label(String.format("%.1f / %.0f kg", current, goal));
            progressLabel.setFont(Font.font(9));
            progressLabel.setTextFill(Color.web("#94a3b8"));

            progressSection.getChildren().addAll(pb, progressLabel);
            badgeBox.getChildren().addAll(nameLabel, progressSection);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        badgesContainer.getChildren().add(badgeBox);
    }

    private void loadHistoryCards(VBox listContainer) {
        List<Donation> donations = donationService.getHistoryByUser(currentUser.getId());

        if (donations.isEmpty()) {
            Label emptyLabel = new Label("Aucun don trouvé. Allez dans 'Les Campagnes' pour commencer !");
            emptyLabel.setTextFill(Color.web("#64748b"));
            listContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Donation d : donations) {
            listContainer.getChildren().add(createDonationRecord(d));
        }
    }

    private HBox createDonationRecord(Donation d) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(20));
        row.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");

        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(55, 55);
        iconBox.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 12;");
        Label badgeIcon = new Label("♻️");
        badgeIcon.setFont(Font.font(26));
        iconBox.getChildren().add(badgeIcon);

        VBox details = new VBox(5);
        Label campaignName = new Label(d.getCollectionTitle());
        campaignName.setFont(Font.font("System", FontWeight.BOLD, 17));
        campaignName.setTextFill(Color.web("#1e293b"));

        String dateStr = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm").format(d.getDonation_date());
        Label dateLabel = new Label(dateStr);
        dateLabel.setTextFill(Color.web("#64748b"));
        dateLabel.setFont(Font.font(13));

        details.getChildren().addAll(campaignName, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(d.getStatus().toUpperCase());
        statusBadge.setStyle("-fx-background-color: #ecfdf5; " +
                "-fx-text-fill: #065f46; " +
                "-fx-padding: 6 14; " +
                "-fx-background-radius: 20; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 10;");

        VBox amountBox = new VBox(2);
        amountBox.setAlignment(Pos.CENTER_RIGHT);
        Label amountLabel = new Label("+" + d.getAmount() + " kg");
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        amountLabel.setTextFill(Color.web("#10b981"));

        Label unitText = new Label("Matière recyclée");
        unitText.setFont(Font.font(11));
        unitText.setTextFill(Color.web("#94a3b8"));

        amountBox.getChildren().addAll(amountLabel, unitText);

        row.getChildren().addAll(iconBox, details, spacer, statusBadge, amountBox);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"));

        return row;
    }

    public VBox getView() { return container; }
}