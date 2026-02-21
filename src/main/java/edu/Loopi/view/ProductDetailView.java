package edu.Loopi.view;

import edu.Loopi.entities.Feedback;
import edu.Loopi.entities.Produit;
import edu.Loopi.entities.User;
import edu.Loopi.services.FeedbackService;
import edu.Loopi.services.ProduitService;
import edu.Loopi.services.FavorisService;
import edu.Loopi.services.OpenAIService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ProductDetailView {
    private Produit produit;
    private User currentUser;
    private FeedbackService feedbackService = new FeedbackService();
    private ProduitService produitService = new ProduitService();
    private FavorisService favorisService = new FavorisService();
    private OpenAIService openAIService = new OpenAIService();
    private int selectedRating = 0;
    private Stage stage;
    private boolean estFavoris = false;

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

        // Vérifier si le produit est déjà en favoris
        estFavoris = favorisService.estDansFavoris(currentUser.getId(), produit.getId());

        // Conteneur principal avec padding
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Barre du haut
        HBox topBar = createTopBar();

        // === SECTION PRODUIT ===
        VBox productSection = createProductSection();

        // === SECTION RESEAUX SOCIAUX ===
        HBox socialShareSection = createSocialShareSection();

        // === SECTION FEEDBACK ===
        VBox feedbackSection = createFeedbackSection();

        // === PRODUITS SIMILAIRES ===
        VBox similarSection = createSimilarProductsSection();

        // Ajout de toutes les sections au conteneur principal
        mainContainer.getChildren().addAll(topBar, productSection, socialShareSection, feedbackSection, similarSection);

        // ScrollPane pour permettre le défilement
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Scene scene = new Scene(scrollPane, 900, 800);
        stage.setScene(scene);
        stage.setTitle("Détails du Produit - " + produit.getNom());
        stage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        backBtn.setOnAction(e -> stage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, spacer);
        return topBar;
    }

    private VBox createProductSection() {
        VBox productSection = new VBox(20);
        productSection.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Image du produit
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #fafafa; -fx-background-radius: 10;");
        imageContainer.setPrefHeight(300);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(280);
        imageView.setPreserveRatio(true);

        try {
            if (produit.getImage() != null) {
                File file = new File(produit.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    imageView.setImage(new Image("https://via.placeholder.com/400x280?text=Image+non+disponible"));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/400x280?text=Erreur+chargement"));
        }
        imageContainer.getChildren().add(imageView);

        // Titre et bouton favoris
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(produit.getNom());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button favBtn = createFavorisButton();

        titleBox.getChildren().addAll(title, favBtn);
        HBox.setHgrow(title, Priority.ALWAYS);

        String catName = categoryNames.getOrDefault(produit.getIdCategorie(), "Catégorie inconnue");
        Label category = new Label("Catégorie: " + catName);
        category.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label description = new Label(produit.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        productSection.getChildren().addAll(imageContainer, titleBox, category, description);
        return productSection;
    }

    /**
     * Crée la section de partage sur les réseaux sociaux
     */
    private HBox createSocialShareSection() {
        HBox socialSection = new HBox(20);
        socialSection.setAlignment(Pos.CENTER_LEFT);
        socialSection.setPadding(new Insets(15, 20, 15, 20));
        socialSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label shareLabel = new Label("Partager sur :");
        shareLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // Bouton Facebook
        Button facebookBtn = createSocialButton("Facebook", "#1877f2", "F");
        facebookBtn.setOnAction(e -> shareOnFacebook());

        // Bouton Twitter
        Button twitterBtn = createSocialButton("Twitter", "#1da1f2", "🐦");
        twitterBtn.setOnAction(e -> shareOnTwitter());

        // Bouton WhatsApp
        Button whatsappBtn = createSocialButton("WhatsApp", "#25d366", "📱");
        whatsappBtn.setOnAction(e -> shareOnWhatsApp());

        // Bouton LinkedIn
        Button linkedinBtn = createSocialButton("LinkedIn", "#0077b5", "in");
        linkedinBtn.setOnAction(e -> shareOnLinkedIn());

        // Bouton Copier le lien
        Button copyBtn = createSocialButton("Copier le lien", "#6c757d", "📋");
        copyBtn.setOnAction(e -> copyLinkToClipboard());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        socialSection.getChildren().addAll(shareLabel, facebookBtn, twitterBtn, whatsappBtn, linkedinBtn, spacer, copyBtn);
        return socialSection;
    }

    /**
     * Crée un bouton de réseau social stylisé
     */
    private Button createSocialButton(String text, String color, String icon) {
        Button btn = new Button(icon + " " + text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");

        // Animation au survol
        ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
        st.setToX(1.05);
        st.setToY(1.05);

        btn.setOnMouseEntered(e -> {
            st.setRate(1);
            st.play();
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; " +
                    "-fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, " + color + "80, 10, 0, 0, 2);");
        });

        btn.setOnMouseExited(e -> {
            st.setRate(-1);
            st.play();
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; " +
                    "-fx-background-radius: 20; -fx-cursor: hand;");
        });

        return btn;
    }

    /**
     * Partage sur Facebook
     */
    private void shareOnFacebook() {
        try {
            String url = "https://www.facebook.com/sharer/sharer.php?u=" +
                    URLEncoder.encode(getProductUrl(), StandardCharsets.UTF_8);
            openBrowser(url);
            showAlert("Partage Facebook", "Redirection vers Facebook pour partager ce produit !");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir Facebook: " + e.getMessage());
        }
    }

    /**
     * Partage sur Twitter
     */
    private void shareOnTwitter() {
        try {
            String text = "Découvrez " + produit.getNom() + " sur LOOPI !";
            String url = "https://twitter.com/intent/tweet?text=" +
                    URLEncoder.encode(text, StandardCharsets.UTF_8) +
                    "&url=" + URLEncoder.encode(getProductUrl(), StandardCharsets.UTF_8);
            openBrowser(url);
            showAlert("Partage Twitter", "Redirection vers Twitter pour partager ce produit !");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir Twitter: " + e.getMessage());
        }
    }

    /**
     * Partage sur WhatsApp
     */
    private void shareOnWhatsApp() {
        try {
            String text = "Découvrez " + produit.getNom() + " sur LOOPI !\n" + getProductUrl();
            String url = "https://api.whatsapp.com/send?text=" +
                    URLEncoder.encode(text, StandardCharsets.UTF_8);
            openBrowser(url);
            showAlert("Partage WhatsApp", "Redirection vers WhatsApp pour partager ce produit !");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir WhatsApp: " + e.getMessage());
        }
    }

    /**
     * Partage sur LinkedIn
     */
    private void shareOnLinkedIn() {
        try {
            String url = "https://www.linkedin.com/sharing/share-offsite/?url=" +
                    URLEncoder.encode(getProductUrl(), StandardCharsets.UTF_8);
            openBrowser(url);
            showAlert("Partage LinkedIn", "Redirection vers LinkedIn pour partager ce produit !");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir LinkedIn: " + e.getMessage());
        }
    }

    /**
     * Copie le lien du produit dans le presse-papiers
     */
    private void copyLinkToClipboard() {
        try {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(getProductUrl());
            clipboard.setContent(content);
            showAlert("Lien copié !", "Le lien du produit a été copié dans le presse-papiers.");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de copier le lien: " + e.getMessage());
        }
    }

    /**
     * Génère une URL pour le produit (simulée)
     */
    private String getProductUrl() {
        return "https://www.loopi.tn/produit/" + produit.getId() + "/" +
                produit.getNom().toLowerCase().replace(" ", "-");
    }

    /**
     * Ouvre le navigateur par défaut avec l'URL donnée
     */
    private void openBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le navigateur: " + e.getMessage());
        }
    }

    private Button createFavorisButton() {
        Button favBtn = new Button();
        favBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-cursor: hand;");

        // Mettre à jour le texte selon l'état
        updateFavorisButton(favBtn);

        // Animation au survol
        ScaleTransition st = new ScaleTransition(Duration.millis(200), favBtn);
        st.setToX(1.2);
        st.setToY(1.2);

        favBtn.setOnMouseEntered(e -> {
            st.setRate(1);
            st.play();
        });

        favBtn.setOnMouseExited(e -> {
            st.setRate(-1);
            st.play();
        });

        // Action du bouton
        favBtn.setOnAction(e -> {
            if (estFavoris) {
                favorisService.supprimerFavoris(currentUser.getId(), produit.getId());
                estFavoris = false;
                showAlert("Succès", "Produit retiré des favoris ❌");
            } else {
                favorisService.ajouterFavoris(currentUser.getId(), produit.getId());
                estFavoris = true;
                showAlert("Succès", "Produit ajouté aux favoris ❤️");
            }
            updateFavorisButton(favBtn);
        });

        return favBtn;
    }

    private void updateFavorisButton(Button btn) {
        if (estFavoris) {
            btn.setText("❤️");
            btn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
        } else {
            btn.setText("❤️");
            btn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-text-fill: #95a5a6; -fx-cursor: hand;");
        }
    }

    private VBox createFeedbackSection() {
        VBox feedbackSection = new VBox(20);
        feedbackSection.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // En-tête avec note moyenne
        HBox ratingHeader = createRatingHeader();

        Label feedbackTitle = new Label("Avis des clients");
        feedbackTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre d'outils IA
        HBox aiTools = new HBox(10);
        aiTools.setAlignment(Pos.CENTER_RIGHT);
        aiTools.setPadding(new Insets(0, 0, 10, 0));

        Button analyzeBtn = new Button("📊 Analyser les avis");
        analyzeBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        analyzeBtn.setOnAction(e -> showSentimentAnalysis());

        Button chatbotBtn = new Button("🤖 Assistant virtuel");
        chatbotBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        chatbotBtn.setOnAction(e -> showChatbot());

        aiTools.getChildren().addAll(analyzeBtn, chatbotBtn);

        // Formulaire d'avis
        VBox formBox = new VBox(10);
        formBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");

        Label rateLabel = new Label("Donnez votre avis :");
        rateLabel.setStyle("-fx-font-weight: bold;");

        // Étoiles
        HBox starBox = new HBox(5);
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button star = new Button("☆");
            star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            star.setOnAction(e -> {
                selectedRating = rating;
                updateStars(starBox);
            });
            starBox.getChildren().add(star);
        }

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Partagez votre expérience avec ce produit...");
        commentArea.setPrefRowCount(3);

        Button submitBtn = new Button("Publier mon avis");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> handleSubmit(commentArea, starBox));

        formBox.getChildren().addAll(rateLabel, starBox, commentArea, submitBtn);

        // Liste des commentaires
        VBox commentsList = new VBox(10);

        List<Feedback> feedbacks = feedbackService.getFeedbacksByProduct(produit.getId());

        if (feedbacks.isEmpty()) {
            Label noComments = new Label("Aucun avis pour le moment. Soyez le premier à commenter !");
            noComments.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
            commentsList.getChildren().add(noComments);
        } else {
            for (Feedback f : feedbacks) {
                commentsList.getChildren().add(createCommentCard(f));
            }
        }

        feedbackSection.getChildren().addAll(ratingHeader, feedbackTitle, aiTools, formBox, commentsList);
        return feedbackSection;
    }

    private HBox createRatingHeader() {
        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 15 0;");

        List<Feedback> feedbacks = feedbackService.getFeedbacksByProduct(produit.getId());

        if (feedbacks.isEmpty()) {
            Label noRating = new Label("Aucun avis pour le moment");
            noRating.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
            header.getChildren().add(noRating);
            return header;
        }

        double avgRating = feedbacks.stream()
                .mapToInt(Feedback::getNote)
                .average()
                .orElse(0.0);

        Label avgLabel = new Label(String.format("%.1f", avgRating));
        avgLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        avgLabel.setPrefWidth(70);

        VBox starsBox = new VBox(5);

        HBox stars = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label();
            if (i <= Math.round(avgRating)) {
                star.setText("★");
                star.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 20px;");
            } else {
                star.setText("☆");
                star.setStyle("-fx-text-fill: #ccc; -fx-font-size: 20px;");
            }
            stars.getChildren().add(star);
        }

        Label reviewCount = new Label("(" + feedbacks.size() + " avis)");
        reviewCount.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        starsBox.getChildren().addAll(stars, reviewCount);

        header.getChildren().addAll(avgLabel, starsBox);
        return header;
    }

    private void updateStars(HBox starBox) {
        for (int i = 0; i < starBox.getChildren().size(); i++) {
            Button star = (Button) starBox.getChildren().get(i);
            if (i < selectedRating) {
                star.setText("★");
                star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            } else {
                star.setText("☆");
                star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            }
        }
    }

    private void handleSubmit(TextArea commentArea, HBox starBox) {
        if (selectedRating == 0) {
            showAlert("Erreur", "Veuillez sélectionner une note");
            return;
        }
        if (commentArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez écrire un commentaire");
            return;
        }

        Feedback feedback = new Feedback(
                currentUser.getId(),
                produit.getId(),
                selectedRating,
                commentArea.getText()
        );

        feedbackService.addFeedback(feedback);

        // Reset
        selectedRating = 0;
        commentArea.clear();
        updateStars(starBox);

        // Refresh
        refresh();
    }

    private VBox createCommentCard(Feedback f) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label(f.getUserName());
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox stars = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= f.getNote() ? "★" : "☆");
            star.setStyle(i <= f.getNote() ? "-fx-text-fill: #f1c40f;" : "-fx-text-fill: #ccc;");
            stars.getChildren().add(star);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(formatDate(f.getDateCommentaire()));
        dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        header.getChildren().addAll(userLabel, stars, spacer, dateLabel);

        Label comment = new Label(f.getCommentaire());
        comment.setWrapText(true);
        comment.setStyle("-fx-text-fill: #555;");

        card.getChildren().addAll(header, comment);

        // Boutons pour ses propres commentaires
        if (f.getIdUser() == currentUser.getId()) {
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button editBtn = new Button("Modifier");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-radius: 3; -fx-padding: 3 10; -fx-cursor: hand;");

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-radius: 3; -fx-padding: 3 10; -fx-cursor: hand;");

            final Feedback currentFeedback = f;

            editBtn.setOnAction(e -> showEditDialog(currentFeedback));
            deleteBtn.setOnAction(e -> confirmDelete(currentFeedback));

            actions.getChildren().addAll(editBtn, deleteBtn);
            card.getChildren().add(actions);
        }

        return card;
    }

    private void showEditDialog(Feedback f) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier votre avis");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox starBox = new HBox(5);
        int currentRating = f.getNote();

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button star = new Button(i <= currentRating ? "★" : "☆");
            star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f;");
            star.setOnAction(e -> {
                f.setNote(rating);
                updateDialogStars(starBox, rating);
            });
            starBox.getChildren().add(star);
        }

        TextArea commentArea = new TextArea(f.getCommentaire());
        commentArea.setPrefRowCount(3);

        content.getChildren().addAll(new Label("Note:"), starBox, new Label("Commentaire:"), commentArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (!commentArea.getText().trim().isEmpty()) {
                f.setCommentaire(commentArea.getText());
                feedbackService.updateFeedback(f);
                refresh();
            }
        }
    }

    private void updateDialogStars(HBox starBox, int rating) {
        for (int i = 0; i < starBox.getChildren().size(); i++) {
            Button star = (Button) starBox.getChildren().get(i);
            star.setText(i < rating ? "★" : "☆");
        }
    }

    private void confirmDelete(Feedback f) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer votre avis ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            feedbackService.deleteFeedback(f.getIdFeedback());
            refresh();
        }
    }

    private VBox createSimilarProductsSection() {
        VBox similarSection = new VBox(15);
        similarSection.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label similarTitle = new Label("Produits similaires");
        similarTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox similarProducts = new HBox(15);
        similarProducts.setAlignment(Pos.CENTER_LEFT);

        List<Produit> relatedProducts = produitService.getAll().stream()
                .filter(p -> p.getIdCategorie() == produit.getIdCategorie() && p.getId() != produit.getId())
                .limit(4)
                .collect(Collectors.toList());

        if (relatedProducts.isEmpty()) {
            Label noSimilar = new Label("Aucun autre produit dans cette catégorie");
            noSimilar.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
            similarProducts.getChildren().add(noSimilar);
        } else {
            for (Produit p : relatedProducts) {
                similarProducts.getChildren().add(createSimilarProductCard(p));
            }
        }

        similarSection.getChildren().addAll(similarTitle, similarProducts);
        return similarSection;
    }

    private VBox createSimilarProductCard(Produit p) {
        VBox card = new VBox(5);
        card.setPrefWidth(150);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(130);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        try {
            if (p.getImage() != null) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            // Ignorer
        }

        Label name = new Label(p.getNom());
        name.setWrapText(true);
        name.setMaxWidth(100);
        name.setAlignment(Pos.CENTER);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        card.getChildren().addAll(imageView, name);

        final Produit currentProduit = p;
        final Stage currentStage = this.stage;

        card.setOnMouseClicked(e -> {
            ProductDetailView detailView = new ProductDetailView(currentProduit);
            detailView.show();
            currentStage.close();
        });

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;"));

        return card;
    }

    /**
     * Analyse des sentiments des avis
     */
    private void showSentimentAnalysis() {
        List<Feedback> feedbacks = feedbackService.getFeedbacksByProduct(produit.getId());

        if (feedbacks.isEmpty()) {
            showAlert("Information", "Aucun avis à analyser pour ce produit.");
            return;
        }

        Task<OpenAIService.SentimentAnalysis> task = new Task<OpenAIService.SentimentAnalysis>() {
            @Override
            protected OpenAIService.SentimentAnalysis call() throws Exception {
                // Analyser le premier commentaire comme exemple
                return openAIService.analyzeSentiment(feedbacks.get(0).getCommentaire());
            }
        };

        task.setOnSucceeded(e -> {
            OpenAIService.SentimentAnalysis analysis = task.getValue();
            showSentimentResult(analysis, feedbacks.size());
        });

        task.setOnFailed(e -> {
            showAlert("Erreur", "Échec de l'analyse des sentiments");
        });

        showLoadingDialog("Analyse des avis en cours...", task);
        new Thread(task).start();
    }

    private void showSentimentResult(OpenAIService.SentimentAnalysis analysis, int totalAvis) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📊 Analyse des sentiments");
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // En-tête avec émotion dominante
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label emotionIcon = new Label(analysis.getSentimentEmoji());
        emotionIcon.setStyle("-fx-font-size: 48px;");

        VBox sentimentBox = new VBox(5);
        Label sentimentLabel = new Label("Sentiment: " + analysis.getSentiment());
        sentimentLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " +
                getSentimentColor(analysis.getSentiment()) + ";");

        Label scoreLabel = new Label(String.format("Score de confiance: %.0f%%", analysis.getScore() * 100));
        scoreLabel.setStyle("-fx-font-size: 14px;");

        sentimentBox.getChildren().addAll(sentimentLabel, scoreLabel);
        headerBox.getChildren().addAll(emotionIcon, sentimentBox);

        // Barre de progression
        ProgressBar sentimentBar = new ProgressBar(analysis.getScore());
        sentimentBar.setPrefWidth(400);
        sentimentBar.setStyle("-fx-accent: " + getSentimentColor(analysis.getSentiment()) + ";");

        // Explication
        Label explicationLabel = new Label(analysis.getExplanation());
        explicationLabel.setWrapText(true);
        explicationLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Mots-clés détectés
        if (!analysis.getMotsCles().isEmpty()) {
            VBox keywordsBox = new VBox(10);
            keywordsBox.setPadding(new Insets(10, 0, 0, 0));

            Label keywordsTitle = new Label("🔑 Mots-clés détectés:");
            keywordsTitle.setStyle("-fx-font-weight: bold;");

            FlowPane keywordsFlow = new FlowPane(10, 10);
            keywordsFlow.setPrefWrapLength(400);

            for (String mot : analysis.getMotsCles()) {
                Label tag = new Label(mot);
                tag.setStyle("-fx-background-color: #e2e8f0; -fx-padding: 5 12; -fx-background-radius: 15; -fx-font-size: 12px;");
                keywordsFlow.getChildren().add(tag);
            }

            keywordsBox.getChildren().addAll(keywordsTitle, keywordsFlow);
            content.getChildren().add(keywordsBox);
        }

        // Statistiques
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(15, 0, 0, 0));
        statsBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-padding: 15 0 0 0;");

        VBox totalBox = new VBox(5);
        totalBox.setAlignment(Pos.CENTER);
        Label totalValue = new Label(String.valueOf(totalAvis));
        totalValue.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label totalLabel = new Label("Total avis");
        totalBox.getChildren().addAll(totalValue, totalLabel);

        statsBox.getChildren().add(totalBox);

        content.getChildren().addAll(headerBox, sentimentBar, explicationLabel, statsBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String getSentimentColor(String sentiment) {
        switch (sentiment) {
            case "positif": return "#10b981";
            case "negatif": return "#ef4444";
            default: return "#f59e0b";
        }
    }

    /**
     * Chatbot pour les questions sur le produit
     */
    private void showChatbot() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🤖 Assistant LOOPI");
        dialog.setHeaderText("Posez vos questions sur " + produit.getNom());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // Zone des messages
        VBox messagesBox = new VBox(10);
        messagesBox.setPrefHeight(300);
        messagesBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 8;");

        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Message de bienvenue
        HBox welcomeMsg = createChatMessage("Bonjour ! Je suis l'assistant LOOPI. " +
                "Posez-moi des questions sur les avantages de " + produit.getNom() +
                " (qualités artistiques, écologiques, etc.)", false);
        messagesBox.getChildren().add(welcomeMsg);

        // Zone de saisie
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        TextField questionField = new TextField();
        questionField.setPromptText("Votre question...");
        questionField.setPrefWidth(300);

        Button sendBtn = new Button("Envoyer");
        sendBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

        inputBox.getChildren().addAll(questionField, sendBtn);

        content.getChildren().addAll(scrollPane, inputBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Gestion de l'envoi
        sendBtn.setOnAction(e -> {
            String question = questionField.getText().trim();
            if (!question.isEmpty()) {
                // Ajouter la question de l'utilisateur
                HBox userMsg = createChatMessage(question, true);
                messagesBox.getChildren().add(userMsg);

                // Effacer le champ
                questionField.clear();

                // Faire défiler vers le bas
                scrollPane.setVvalue(1.0);

                // Obtenir la réponse de l'IA
                Task<String> responseTask = new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        String category = categoryNames.getOrDefault(produit.getIdCategorie(), "Art recyclé");
                        return openAIService.askProductBenefits(produit.getNom(), category, question);
                    }
                };

                responseTask.setOnSucceeded(ev -> {
                    String response = responseTask.getValue();
                    if (response != null && !response.isEmpty()) {
                        HBox botMsg = createChatMessage(response, false);
                        messagesBox.getChildren().add(botMsg);
                        scrollPane.setVvalue(1.0);
                    }
                });

                new Thread(responseTask).start();
            }
        });

        // Permettre l'envoi avec Entrée
        questionField.setOnAction(e -> sendBtn.fire());

        dialog.showAndWait();
    }

    private HBox createChatMessage(String message, boolean isUser) {
        HBox box = new HBox(10);
        box.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        box.setPadding(new Insets(5));

        Label avatar = new Label(isUser ? "👤" : "🤖");
        avatar.setStyle("-fx-font-size: 20px;");

        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(300);
        msgLabel.setStyle(isUser ?
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 15 15 5 15;" :
                "-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; -fx-padding: 10; -fx-background-radius: 15 15 15 5;");

        if (isUser) {
            box.getChildren().addAll(msgLabel, avatar);
        } else {
            box.getChildren().addAll(avatar, msgLabel);
        }

        return box;
    }

    private void showLoadingDialog(String message, Task<?> task) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🤖 Traitement IA");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setPrefWidth(300);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(40, 40);
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(progress, msgLabel);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        task.setOnSucceeded(e -> dialog.close());
        task.setOnFailed(e -> dialog.close());

        dialog.show();
    }

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null) return "Récemment";
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    private void refresh() {
        stage.close();
        this.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}