package edu.Loopi.view;

import edu.Loopi.entities.Feedback;
import edu.Loopi.entities.Produit;
import edu.Loopi.entities.User;
import edu.Loopi.services.FeedbackService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProductDetailView {
    private Produit produit;
    private User currentUser;
    private FeedbackService feedbackService = new FeedbackService();
    private int selectedRating = 0;
    private Stage stage;

    private final Map<Integer, String> categoryNames = new HashMap<>() {{
        put(1, "Objets décoratifs");
        put(2, "Art mural");
        put(3, "Mobilier artistique");
        put(4, "Installations artistiques");
    }};

    public ProductDetailView(Produit produit) {
        this.produit = produit;
        this.currentUser = SessionManager.getCurrentUser();
    }

    public void show() {
        this.stage = new Stage();
        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #ffffff;");

        Button backBtn = new Button("← Retour à la boutique");
        backBtn.setStyle("-fx-background-color: #f1f1f1; -fx-text-fill: #333; -fx-cursor: hand; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> stage.close());

        HBox topSection = new HBox(40);
        topSection.setAlignment(Pos.TOP_CENTER);

        VBox leftBox = new VBox();
        leftBox.setPrefWidth(400);

        StackPane imageFrame = new StackPane();
        imageFrame.setStyle("-fx-border-color: #eee; -fx-border-width: 1; -fx-background-color: #fafafa; -fx-background-radius: 10; -fx-border-radius: 10;");
        imageFrame.setPrefSize(400, 400);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(380);
        imageView.setFitHeight(380);
        imageView.setPreserveRatio(true);

        try {
            if (produit.getImage() != null) {
                File file = new File(produit.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur image: " + e.getMessage());
        }
        imageFrame.getChildren().add(imageView);
        leftBox.getChildren().add(imageFrame);

        VBox rightBox = new VBox(20);
        rightBox.setPrefWidth(500);

        Label title = new Label(produit.getNom().toUpperCase());
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#2c3e50"));

        String catLabelName = categoryNames.getOrDefault(produit.getIdCategorie(), "Catégorie inconnue");
        Label category = new Label("🏷️ " + catLabelName);
        category.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #f0fdf4; -fx-padding: 5 10; -fx-background-radius: 5;");

        Label descTitle = new Label("Description :");
        descTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label description = new Label(produit.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-line-spacing: 5;");
        description.setMaxWidth(480);

        VBox feedbackInput = new VBox(15);
        feedbackInput.setPadding(new Insets(20));
        feedbackInput.setStyle("-fx-background-color: #fcfcfc; -fx-border-color: #f1f1f1; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label rateLabel = new Label("Votre avis compte !");
        rateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox starBox = new HBox(8);
        for (int i = 1; i <= 5; i++) {
            int rating = i;
            Button star = new Button("☆");
            star.setStyle("-fx-font-size: 26px; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
            star.setOnAction(e -> {
                selectedRating = rating;
                updateStars(starBox);
            });
            starBox.getChildren().add(star);
        }

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Partagez votre expérience avec ce produit...");
        commentArea.setPrefHeight(100);
        commentArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        Button submitBtn = new Button("PUBLIER L'AVIS");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;");

        // --- FIXED SUBMIT LOGIC WITH TEXT VALIDATION ---
        submitBtn.setOnAction(e -> {
            if (selectedRating == 0) {
                showAlert("Note manquante", "S'il vous plaît, donnez une note en cliquant sur les étoiles.");
                return;
            }
            if (commentArea.getText().trim().isEmpty()) {
                showAlert("Commentaire vide", "S'il vous plaît, écrivez un petit message avant de publier.");
                return;
            }
            Feedback f = new Feedback(currentUser.getId(), produit.getId(), selectedRating, commentArea.getText());
            feedbackService.addFeedback(f);
            refresh();
        });

        feedbackInput.getChildren().addAll(rateLabel, starBox, commentArea, submitBtn);
        rightBox.getChildren().addAll(title, category, descTitle, description, new Separator(), feedbackInput);
        topSection.getChildren().addAll(leftBox, rightBox);

        VBox bottomSection = new VBox(20);
        Label reviewsTitle = new Label("COMMENTAIRES DE LA COMMUNAUTÉ");
        reviewsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        reviewsTitle.setStyle("-fx-border-color: #27ae60; -fx-border-width: 0 0 3 0; -fx-padding: 0 0 5 0;");

        VBox reviewsContainer = new VBox(15);
        List<Feedback> feedbacks = feedbackService.getFeedbacksByProduct(produit.getId());

        if (feedbacks.isEmpty()) {
            Label empty = new Label("Aucun avis pour le moment. Soyez le premier à commenter !");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
            reviewsContainer.getChildren().add(empty);
        } else {
            for (Feedback f : feedbacks) {
                reviewsContainer.getChildren().add(createFeedbackCard(f, starBox, commentArea, submitBtn));
            }
        }

        bottomSection.getChildren().addAll(reviewsTitle, reviewsContainer);
        VBox finalLayout = new VBox(40, backBtn, topSection, bottomSection);
        ScrollPane mainScroll = new ScrollPane(finalLayout);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background-color: transparent; -fx-background: white;");

        Scene scene = new Scene(mainScroll, 1050, 850);
        stage.setScene(scene);
        stage.setTitle("Détails du Produit - " + produit.getNom());
        stage.show();
    }

    private VBox createFeedbackCard(Feedback f, HBox starBox, TextArea commentArea, Button submitBtn) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #eee; -fx-border-radius: 10; -fx-background-radius: 10;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label user = new Label(f.getUserName());
        user.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #34495e;");

        Label stars = new Label("★".repeat(f.getNote()) + "☆".repeat(5 - f.getNote()));
        stars.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 16px;");

        Label dateLabel = new Label();
        if (f.getDateCommentaire() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            dateLabel.setText(f.getDateCommentaire().format(formatter));
        } else {
            dateLabel.setText("Récemment");
        }
        dateLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(user, stars, spacer, dateLabel);

        Label content = new Label(f.getCommentaire());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #555;");

        card.getChildren().addAll(header, content);

        if (f.getIdUser() == currentUser.getId()) {
            HBox actions = new HBox(15);
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button edit = new Button("Modifier ✎");
            edit.setStyle("-fx-text-fill: #3498db; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold;");

            edit.setOnAction(e -> {
                selectedRating = f.getNote();
                updateStars(starBox);
                commentArea.setText(f.getCommentaire());
                submitBtn.setText("CONFIRMER LA MODIFICATION");
                submitBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5;");

                submitBtn.setOnAction(ev -> {
                    // ALSO VALIDATE ON UPDATE
                    if (commentArea.getText().trim().isEmpty()) {
                        showAlert("Erreur", "Le commentaire ne peut pas être vide.");
                        return;
                    }
                    f.setNote(selectedRating);
                    f.setCommentaire(commentArea.getText());
                    feedbackService.updateFeedback(f);
                    refresh();
                });
            });

            Button delete = new Button("Supprimer 🗑");
            delete.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold;");

            delete.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Suppression");
                alert.setHeaderText("Supprimer mon commentaire");
                alert.setContentText("Voulez-vous vraiment supprimer cet avis ?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    feedbackService.deleteFeedback(f.getIdFeedback());
                    refresh();
                }
            });

            actions.getChildren().addAll(edit, delete);
            card.getChildren().add(actions);
        }

        return card;
    }

    private void updateStars(HBox starBox) {
        for (int i = 0; i < 5; i++) {
            Button b = (Button) starBox.getChildren().get(i);
            b.setText(i < selectedRating ? "★" : "☆");
            b.setStyle(i < selectedRating ?
                    "-fx-font-size: 26px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-padding: 0;" :
                    "-fx-font-size: 26px; -fx-background-color: transparent; -fx-text-fill: #333; -fx-padding: 0;");
        }
    }

    private void refresh() {
        stage.close();
        this.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}