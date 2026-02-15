package edu.Loopi.view;

import edu.Loopi.entities.Donation;
import edu.Loopi.entities.User;
import edu.Loopi.services.DonationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.List;

public class DonationHistoryView {
    private VBox container;
    private DonationService service = new DonationService();
    private User currentUser;

    public DonationHistoryView(User user) {
        this.currentUser = user;
        this.container = new VBox(25);
        this.container.setPadding(new Insets(30));
        // Soft light background to make the white cards pop
        this.container.setStyle("-fx-background-color: #f1f5f9;");

        Label title = new Label("📋 Mon Historique de Dons");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#064e3b"));

        VBox historyList = new VBox(15);
        historyList.setPadding(new Insets(10));

        loadHistoryCards(historyList);

        ScrollPane scrollPane = new ScrollPane(historyList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(title, scrollPane);
    }

    private void loadHistoryCards(VBox listContainer) {
        List<Donation> donations = service.getHistoryByUser(currentUser.getId());

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
        // White card background
        row.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");

        // THE PICTURE/ICON ON THE LEFT (Eco-Badge)
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(55, 55);
        iconBox.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 12;");
        Label badgeIcon = new Label("♻️"); // Changed to recycling symbol
        badgeIcon.setFont(Font.font(26));
        iconBox.getChildren().add(badgeIcon);

        // Text details
        VBox details = new VBox(5);

        // FIX: Ensuring the collection name is dark and visible
        Label campaignName = new Label(d.getCollectionTitle());
        campaignName.setFont(Font.font("System", FontWeight.BOLD, 17));
        campaignName.setTextFill(Color.web("#1e293b")); // Dark Navy/Charcoal

        String dateStr = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm").format(d.getDonation_date());
        Label dateLabel = new Label(dateStr);
        dateLabel.setTextFill(Color.web("#64748b")); // Clearly visible slate grey
        dateLabel.setFont(Font.font(13));

        details.getChildren().addAll(campaignName, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status Badge (Confirmé)
        Label statusBadge = new Label(d.getStatus().toUpperCase());
        statusBadge.setStyle("-fx-background-color: #ecfdf5; " +
                "-fx-text-fill: #065f46; " +
                "-fx-padding: 6 14; " +
                "-fx-background-radius: 20; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 10;");

        // Amount Section
        VBox amountBox = new VBox(2);
        amountBox.setAlignment(Pos.CENTER_RIGHT);
        Label amountLabel = new Label("+" + d.getAmount() + " kg");
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        amountLabel.setTextFill(Color.web("#10b981")); // Bright Emerald Green

        Label unitText = new Label("Matière recyclée");
        unitText.setFont(Font.font(11));
        unitText.setTextFill(Color.web("#94a3b8"));

        amountBox.getChildren().addAll(amountLabel, unitText);

        row.getChildren().addAll(iconBox, details, spacer, statusBadge, amountBox);

        // Smooth Hover Effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"));

        return row;
    }

    public VBox getView() { return container; }
}