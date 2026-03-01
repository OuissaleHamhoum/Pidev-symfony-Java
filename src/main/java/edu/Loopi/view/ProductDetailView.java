package edu.Loopi.view;

import edu.Loopi.entities.Feedback;
import edu.Loopi.entities.Produit;
import edu.Loopi.entities.User;
import edu.Loopi.services.FeedbackService;
import edu.Loopi.services.ProduitService;
import edu.Loopi.services.FavorisService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;

import javafx.geometry.Orientation;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;


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
<<<<<<< HEAD
     * Crée la section de partage sur les réseaux sociaux et QR code
=======
     * Crée la section de partage sur les réseaux sociaux
>>>>>>> 1e615f748ff84fe73d51f4e96046ed760d677f91
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

        // Séparateur vertical
        Separator sep1 = new Separator(Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: #e0e0e0;");

        // BOUTON QR CODE (NOIR)
        Button qrButton = createQRCodeButton();


        // Bouton Copier le lien
        Button copyBtn = createSocialButton("Copier le lien", "#6c757d", "📋");
        copyBtn.setOnAction(e -> copyLinkToClipboard());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        socialSection.getChildren().addAll(shareLabel, facebookBtn, twitterBtn, whatsappBtn, sep1, qrButton, spacer, copyBtn);

        return socialSection;
    }

    /**
<<<<<<< HEAD
     * Crée un bouton noir avec QR code intégré
     */
    private Button createQRCodeButton() {
        Button btn = new Button();
        btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 15; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");

        // Contenu du bouton avec QR code et texte
        HBox content = new HBox(8);
        content.setAlignment(Pos.CENTER);

        // Petit QR code
        ImageView qrIcon = generateQRCode(getProductUrl(), 25, 25);
        qrIcon.setFitWidth(20);
        qrIcon.setFitHeight(20);

        Label text = new Label("QR Code");
        text.setTextFill(javafx.scene.paint.Color.WHITE);
        text.setStyle("-fx-font-weight: bold;");

        content.getChildren().addAll(qrIcon, text);
        btn.setGraphic(content);

        // Tooltip
        Tooltip.install(btn, new Tooltip("Afficher le QR code"));

        // Animation au survol
        ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
        st.setToX(1.05);
        st.setToY(1.05);

        btn.setOnMouseEntered(e -> {
            st.setRate(1);
            st.play();
            btn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 15; " +
                    "-fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, #00000080, 10, 0, 0, 2);");
        });

        btn.setOnMouseExited(e -> {
            st.setRate(-1);
            st.play();
            btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 15; " +
                    "-fx-background-radius: 20; -fx-cursor: hand;");
        });

        // Action : afficher le grand QR code
        btn.setOnAction(e -> showLargeQRCode());

        return btn;
    }

    /**
     * Génère un QR code à partir d'un texte
     */
    private ImageView generateQRCode(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            WritableImage image = new WritableImage(width, height);
            PixelWriter pixelWriter = image.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (WriterException e) {
            e.printStackTrace();
            // En cas d'erreur, retourner une image vide
            ImageView fallback = new ImageView();
            fallback.setFitWidth(width);
            fallback.setFitHeight(height);
            return fallback;
        }
    }

    /**
     * Affiche une version agrandie du QR code
     */
    private void showLargeQRCode() {
        Stage qrStage = new Stage();
        qrStage.setTitle("QR Code - " + produit.getNom());

        VBox qrContainer = new VBox(20);
        qrContainer.setAlignment(Pos.CENTER);
        qrContainer.setPadding(new Insets(30));
        qrContainer.setStyle("-fx-background-color: white;");

        Label title = new Label(produit.getNom());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ImageView largeQR = generateQRCode(getProductUrl(), 300, 300);

        Label urlLabel = new Label(getProductUrl());
        urlLabel.setWrapText(true);
        urlLabel.setMaxWidth(400);
        urlLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-text-alignment: center;");

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> qrStage.close());

        // Bouton pour télécharger le QR code
        Button downloadBtn = new Button("📥 Télécharger");
        downloadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
        downloadBtn.setOnAction(e -> downloadQRCode());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(downloadBtn, closeBtn);

        qrContainer.getChildren().addAll(title, largeQR, urlLabel, buttonBox);

        Scene scene = new Scene(qrContainer, 500, 550);
        qrStage.setScene(scene);
        qrStage.show();
    }

    /**
     * Télécharge le QR code en tant que fichier image
     */
    private void downloadQRCode() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer le QR Code");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Fichier PNG", "*.png")
            );
            fileChooser.setInitialFileName("qrcode_" + produit.getNom().replace(" ", "_") + ".png");

            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                // Générer le QR code en mémoire
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(getProductUrl(), BarcodeFormat.QR_CODE, 300, 300);

                // Convertir en BufferedImage pour sauvegarde
                java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(300, 300, java.awt.image.BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < 300; x++) {
                    for (int y = 0; y < 300; y++) {
                        bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                    }
                }

                // Sauvegarder
                javax.imageio.ImageIO.write(bufferedImage, "PNG", file);
                showAlert("Succès", "QR Code téléchargé avec succès !");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de télécharger le QR code: " + e.getMessage());
        }
    }

    /**
=======
>>>>>>> 1e615f748ff84fe73d51f4e96046ed760d677f91
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
<<<<<<< HEAD
     * Génère une URL pour le produit
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
                // Retirer des favoris
                favorisService.supprimerFavoris(currentUser.getId(), produit.getId());
                estFavoris = false;
                showAlert("Succès", "Produit retiré des favoris ❌");
            } else {
                // Ajouter aux favoris
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

            btn.setText("❤");
            btn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
        } else {
            btn.setText("❤");
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

        // Formulaire d'avis
        VBox formBox = new VBox(10);
        formBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");

        Label rateLabel = new Label("Donnez votre avis :");

        rateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");


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

        feedbackSection.getChildren().addAll(ratingHeader, feedbackTitle, formBox, commentsList);
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

        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");


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

