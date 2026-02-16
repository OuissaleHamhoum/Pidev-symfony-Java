package edu.Loopi.view;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import edu.Loopi.services.EventService;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EventView {
    private User currentUser;
    private VBox mainLayout;
    private FlowPane cardsContainer;
    private EventService eventService = new EventService();
    private List<Event> allEvents;
    private String selectedImagePath = "";

    // 📁 DOSSIER DANS resources/
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String IMAGE_STORAGE_DIR = "src" + File.separator + "main" + File.separator +
            "resources" + File.separator + "uploads" + File.separator +
            "events" + File.separator;
    private static final String FULL_IMAGE_PATH = PROJECT_ROOT + File.separator + IMAGE_STORAGE_DIR;

    // 📁 Chemin pour la BDD
    private static final String DB_IMAGE_PATH = "uploads/events/";

    // 🔑 INSÉREZ VOTRE CLÉ API OPENAI ICI (optionnel)
    private static final String OPENAI_API_KEY = "sOPENAI_API_KEY";

    // 📚 Bibliothèque d'images gratuites (toutes fonctionnelles)
    private static final String[] FREE_IMAGES = {
            "https://images.pexels.com/photos/976866/pexels-photo-976866.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/1190298/pexels-photo-1190298.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/1482473/pexels-photo-1482473.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/2608517/pexels-photo-2608517.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/2774556/pexels-photo-2774556.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/3178786/pexels-photo-3178786.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/3184418/pexels-photo-3184418.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/3194523/pexels-photo-3194523.jpeg?auto=compress&cs=tinysrgb&w=600"
    };

    // Composants pour les filtres
    private TextField searchField = new TextField();
    private ComboBox<String> statusFilter = new ComboBox<>();
    private HBox statsBar = new HBox(20);

    public EventView(User user) {
        this.currentUser = user;
        this.mainLayout = new VBox(25);

        createUploadDirectory();
        createView();
        loadData();
    }

    // ============ GESTION DU DOSSIER D'IMAGES ============

    private void createUploadDirectory() {
        File directory = new File(FULL_IMAGE_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("✅ Dossier créé: " + FULL_IMAGE_PATH);
        }
    }

    private String copyImageToStorage(File sourceFile) {
        try {
            File directory = new File(FULL_IMAGE_PATH);
            if (!directory.exists()) directory.mkdirs();

            String extension = "";
            String fileName = sourceFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            extension = (dotIndex > 0) ? fileName.substring(dotIndex) : ".jpg";

            String uniqueFileName = "event_" + UUID.randomUUID().toString() + extension;
            String fullPath = FULL_IMAGE_PATH + uniqueFileName;

            Files.copy(sourceFile.toPath(), Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);

            return DB_IMAGE_PATH + uniqueFileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Image loadImageFromStorage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;

        try {
            String fileName = imagePath.substring(imagePath.lastIndexOf('/') + 1);
            File imgFile = new File(FULL_IMAGE_PATH + fileName);
            if (imgFile.exists()) {
                return new Image(imgFile.toURI().toString());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement image: " + e.getMessage());
        }
        return null;
    }

    // ============ GÉNÉRATION D'IMAGE AVEC FALLBACK - CORRIGÉ ============

    private String generateImageWithAI(String prompt) {
        // Vérifier si la clé API est configurée
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty() ||
                OPENAI_API_KEY.equals("OPENAI_API_KEY") ||
                !OPENAI_API_KEY.startsWith("sk-")) {

            System.out.println("ℹ️ Pas de clé API valide, utilisation d'images gratuites");
            return downloadFreeImage(prompt);
        }

        HttpURLConnection connection = null;
        try {
            System.out.println("🎨 Tentative de génération avec DALL-E 3...");

            URL url = new URL("https://api.openai.com/v1/images/generations");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            String enhancedPrompt = "Create a beautiful, colorful, professional event poster for: " + prompt +
                    ". Style: digital art, vibrant colors, eco-friendly, environmental, " +
                    "high quality, 4k, detailed, no text, no words, just visual.";

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "dall-e-3");
            requestBody.addProperty("prompt", enhancedPrompt);
            requestBody.addProperty("n", 1);
            requestBody.addProperty("size", "1024x1024");
            requestBody.addProperty("quality", "standard");
            requestBody.addProperty("style", "vivid");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("📡 Code réponse API: " + responseCode);

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    String imageUrl = jsonResponse.getAsJsonArray("data")
                            .get(0).getAsJsonObject()
                            .get("url").getAsString();

                    System.out.println("✅ Image générée avec DALL-E 3, téléchargement...");
                    return downloadImageFromUrl(imageUrl);
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    String errorMsg = response.toString();
                    System.err.println("❌ Erreur API: " + errorMsg);

                    if (errorMsg.contains("billing_hard_limit_reached") ||
                            errorMsg.contains("insufficient_quota") ||
                            errorMsg.contains("rate_limit")) {
                        System.out.println("ℹ️ Problème de billing ou quota, utilisation d'images gratuites");
                        return downloadFreeImage(prompt);
                    }
                }
                return downloadFreeImage(prompt);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur génération IA: " + e.getMessage());
            return downloadFreeImage(prompt);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String downloadFreeImage(String prompt) {
        try {
            System.out.println("🖼️ Téléchargement d'une image gratuite depuis Pexels...");

            File directory = new File(FULL_IMAGE_PATH);
            if (!directory.exists()) directory.mkdirs();

            // Sélectionner une image basée sur le prompt
            int hash = Math.abs(prompt.hashCode());
            int imageIndex = hash % FREE_IMAGES.length;
            String imageUrl = FREE_IMAGES[imageIndex];

            String uniqueFileName = "event_free_" + UUID.randomUUID().toString() + ".jpg";
            String fullPath = FULL_IMAGE_PATH + uniqueFileName;

            // Télécharger l'image
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Image gratuite téléchargée: " + fullPath);
            return DB_IMAGE_PATH + uniqueFileName;

        } catch (Exception e) {
            System.err.println("❌ Erreur téléchargement image gratuite: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String downloadImageFromUrl(String imageUrl) {
        try {
            File directory = new File(FULL_IMAGE_PATH);
            if (!directory.exists()) directory.mkdirs();

            String uniqueFileName = "event_ai_" + UUID.randomUUID().toString() + ".png";
            String fullPath = FULL_IMAGE_PATH + uniqueFileName;

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Image téléchargée: " + fullPath);
            return DB_IMAGE_PATH + uniqueFileName;

        } catch (Exception e) {
            System.err.println("❌ Erreur téléchargement: " + e.getMessage());
            return null;
        }
    }

    private void createView() {
        mainLayout.getChildren().clear();
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #f8f9fa;");

        // HEADER
        VBox heroSection = new VBox(8);
        heroSection.setPadding(new Insets(0, 0, 15, 0));

        Label bigTitle = new Label("📅 Mes Événements");
        bigTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        bigTitle.setTextFill(Color.web("#1e293b"));

        Label description = new Label("Gérez vos événements écologiques et suivez les inscriptions.");
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        description.setTextFill(Color.web("#475569"));
        description.setWrapText(true);

        heroSection.getChildren().addAll(bigTitle, description);

        // STATISTIQUES
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setPadding(new Insets(15));
        statsBar.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        // BARRE DE FILTRES
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15));
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 15;");
        refreshBtn.setTooltip(new Tooltip("Actualiser"));
        refreshBtn.setOnAction(e -> refreshData());

        // FILTRE STATUT
        Label statusLabel = new Label("Statut:");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web("#1e293b"));

        statusFilter.getItems().addAll("Tous", "À venir", "En cours", "Passés");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(120);
        statusFilter.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 12; " +
                "-fx-background-color: white; -fx-border-color: #cbd5e1;");
        statusFilter.setOnAction(e -> applyFilters());

        // RECHERCHE
        Label searchLabel = new Label("Recherche:");
        searchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        searchLabel.setTextFill(Color.web("#1e293b"));

        searchField.setPromptText("🔍 Titre ou lieu...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 12; " +
                "-fx-background-color: white; -fx-border-color: #cbd5e1;");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        Button addBtn = new Button("➕ Nouvel Événement");
        addBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 30; " +
                "-fx-font-size: 13px; -fx-cursor: hand;");
        addBtn.setOnAction(e -> openEventDialog(null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(
                refreshBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                statusLabel, statusFilter,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                searchLabel, searchField,
                spacer,
                addBtn
        );

        // ZONE DES CARTES
        cardsContainer = new FlowPane();
        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setAlignment(Pos.TOP_LEFT);
        cardsContainer.setPadding(new Insets(15, 0, 15, 0));

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainLayout.getChildren().addAll(heroSection, statsBar, filterBar, scrollPane);
    }

    private void loadData() {
        allEvents = eventService.getEventsByOrganisateur(currentUser.getId());
        updateStats();
        applyFilters();
    }

    private void refreshData() {
        loadData();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = statusFilter.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(event -> {
                    if (searchText.isEmpty()) return true;
                    return event.getTitre().toLowerCase().contains(searchText) ||
                            (event.getLieu() != null && event.getLieu().toLowerCase().contains(searchText));
                })
                .filter(event -> {
                    if (selectedStatus == null || "Tous".equals(selectedStatus)) return true;
                    String eventStatut = event.getStatut().toLowerCase();
                    String filterStatut = selectedStatus.toLowerCase();

                    if (filterStatut.equals("à venir") && eventStatut.equals("à venir")) return true;
                    if (filterStatut.equals("en cours") && eventStatut.equals("en cours")) return true;
                    if (filterStatut.equals("passés") && eventStatut.equals("passé")) return true;
                    return false;
                })
                .collect(Collectors.toList());

        displayCards(filtered);
    }

    private void updateStats() {
        statsBar.getChildren().clear();

        int total = allEvents.size();
        int aVenir = (int) allEvents.stream().filter(e -> "à venir".equals(e.getStatut())).count();
        int enCours = (int) allEvents.stream().filter(e -> "en cours".equals(e.getStatut())).count();
        int passes = (int) allEvents.stream().filter(e -> "passé".equals(e.getStatut())).count();
        int totalParticipants = allEvents.stream().mapToInt(Event::getParticipantsCount).sum();

        statsBar.getChildren().addAll(
                createStatCard("📊", String.valueOf(total), "#1e293b"),
                createStatCard("⏳", String.valueOf(aVenir), "#3b82f6"),
                createStatCard("🔄", String.valueOf(enCours), "#f59e0b"),
                createStatCard("✅", String.valueOf(passes), "#64748b"),
                createStatCard("👥", String.valueOf(totalParticipants), "#10b981")
        );
    }

    private VBox createStatCard(String icon, String value, String color) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 16px;");

        card.getChildren().addAll(valLbl, iconLbl);
        return card;
    }

    private void displayCards(List<Event> events) {
        cardsContainer.getChildren().clear();

        if (events.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40));
            emptyState.setPrefWidth(600);

            Label emptyIcon = new Label("📅");
            emptyIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));

            String message = "Aucun événement trouvé";
            if (!searchField.getText().isEmpty() || !"Tous".equals(statusFilter.getValue())) {
                message = "Aucun résultat pour les filtres";
            }

            Label emptyText = new Label(message);
            emptyText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            emptyText.setTextFill(Color.web("#1e293b"));

            Label emptySubtext = new Label("Créez votre premier événement");
            emptySubtext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            emptySubtext.setTextFill(Color.web("#64748b"));

            Button createFirstBtn = new Button("➕ Créer un événement");
            createFirstBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 30;");
            createFirstBtn.setOnAction(e -> openEventDialog(null));

            emptyState.getChildren().addAll(emptyIcon, emptyText, emptySubtext, createFirstBtn);
            cardsContainer.getChildren().add(emptyState);
        } else {
            for (Event event : events) {
                cardsContainer.getChildren().add(createEventCard(event));
            }
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(0);
        card.setPrefSize(280, 380);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 5);");

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8);");
            scaleNode(card, 1.02);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 5);");
            scaleNode(card, 1.0);
        });

        // IMAGE
        StackPane imgContainer = new StackPane();
        ImageView imgView = new ImageView();

        Image image = loadImageFromStorage(event.getImage_evenement());
        if (image != null) {
            imgView.setImage(image);
        } else {
            imgView.setImage(new Image("https://via.placeholder.com/280x140/e2e8f0/1e293b?text=LOOPI"));
        }

        imgView.setFitWidth(280);
        imgView.setFitHeight(140);
        imgView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(280, 140);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imgView.setClip(clip);
        imgContainer.getChildren().add(imgView);

        // BADGE STATUT
        String statut = event.getStatut();
        Label statusBadge = new Label(statut.substring(0, 1).toUpperCase());
        statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        statusBadge.setTextFill(Color.WHITE);
        statusBadge.setPadding(new Insets(4, 10, 4, 10));

        String statusColor;
        switch (statut.toLowerCase()) {
            case "à venir": statusColor = "#3b82f6"; break;
            case "en cours": statusColor = "#f59e0b"; break;
            case "passé": statusColor = "#64748b"; break;
            default: statusColor = "#94a3b8";
        }
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 20;");
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(10, 10, 0, 0));
        imgContainer.getChildren().add(statusBadge);

        // CONTENU
        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        HBox dateLieu = new HBox(10);
        dateLieu.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅 " + event.getFormattedDate().substring(0, 10));
        dateLabel.setFont(Font.font(11));
        dateLabel.setTextFill(Color.web("#64748b"));

        Label lieuLabel = new Label("📍 " + (event.getLieu() != null && event.getLieu().length() > 15 ?
                event.getLieu().substring(0, 12) + "..." : event.getLieu()));
        lieuLabel.setFont(Font.font(11));
        lieuLabel.setTextFill(Color.web("#64748b"));

        dateLieu.getChildren().addAll(dateLabel, lieuLabel);

        Label titreLabel = new Label(event.getTitre());
        titreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titreLabel.setTextFill(Color.web("#0f172a"));
        titreLabel.setWrapText(true);
        titreLabel.setMaxHeight(40);

        Label descLabel = new Label();
        String desc = event.getDescription();
        if (desc != null && desc.length() > 50) {
            desc = desc.substring(0, 47) + "...";
        }
        descLabel.setText(desc != null ? desc : "");
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font(11));
        descLabel.setTextFill(Color.web("#475569"));
        descLabel.setMaxHeight(40);

        HBox capacityBox = new HBox(15);
        capacityBox.setAlignment(Pos.CENTER_LEFT);
        capacityBox.setPadding(new Insets(5, 0, 5, 0));

        VBox participantsBox = new VBox(2);
        Label participantsCount = new Label(String.valueOf(event.getParticipantsCount()));
        participantsCount.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        participantsCount.setTextFill(Color.web("#10b981"));
        Label participantsLabel = new Label("participants");
        participantsLabel.setFont(Font.font(10));
        participantsLabel.setTextFill(Color.web("#64748b"));
        participantsBox.getChildren().addAll(participantsCount, participantsLabel);

        capacityBox.getChildren().add(participantsBox);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(5, 0, 0, 0));

        Button btnEdit = new Button("✏️");
        btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12;");
        btnEdit.setTooltip(new Tooltip("Modifier"));
        btnEdit.setOnAction(e -> openEventDialog(event));

        Button btnDelete = new Button("🗑️");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12;");
        btnDelete.setTooltip(new Tooltip("Supprimer"));
        btnDelete.setOnAction(e -> deleteEvent(event));

        Button btnParticipants = new Button("👥 " + event.getParticipantsCount());
        btnParticipants.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12;");
        btnParticipants.setTooltip(new Tooltip("Voir les participants"));
        btnParticipants.setOnAction(e -> showParticipantsDialog(event));

        actions.getChildren().addAll(btnEdit, btnParticipants, btnDelete);

        content.getChildren().addAll(dateLieu, titreLabel, descLabel, capacityBox, actions);
        card.getChildren().addAll(imgContainer, content);

        return card;
    }

    // ============ DIALOGUE DE CRÉATION D'ÉVÉNEMENT - CORRIGÉ avec Platform.runLater ============

    private void openEventDialog(Event existingEvent) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existingEvent == null ? "🎯 Créer un événement" : "✏️ Modifier l'événement");
        dialog.setResizable(false);
        dialog.setWidth(700);
        dialog.setHeight(750);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // --- HEADER ---
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #0f172a;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label headerIcon = new Label(existingEvent == null ? "🎯" : "✏️");
        headerIcon.setFont(Font.font("Segoe UI", 28));
        headerIcon.setTextFill(Color.WHITE);

        VBox headerText = new VBox(3);
        Label headerTitle = new Label(existingEvent == null ? "Créer un événement" : "Modifier");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        headerTitle.setTextFill(Color.WHITE);

        Label headerSubtitle = new Label(existingEvent == null
                ? "Remplissez les informations ci-dessous"
                : "Modifiez les informations");
        headerSubtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        headerSubtitle.setTextFill(Color.web("#cbd5e1"));

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        header.getChildren().addAll(headerIcon, headerText);

        // --- FORMULAIRE ---
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        // TITRE
        VBox titleBox = new VBox(3);
        Label titleLabel = new Label("📝 Titre *");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        TextField titreField = new TextField(existingEvent != null ? existingEvent.getTitre() : "");
        titreField.setPromptText("Ex: Nettoyage de la plage");
        titreField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        Label titreError = new Label();
        titreError.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        titreError.setTextFill(Color.web("#ef4444"));
        titreError.setVisible(false);

        titleBox.getChildren().addAll(titleLabel, titreField, titreError);

        // DESCRIPTION
        VBox descBox = new VBox(3);
        Label descLabel = new Label("📄 Description *");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        TextArea descArea = new TextArea(existingEvent != null ? existingEvent.getDescription() : "");
        descArea.setPromptText("Décrivez votre événement...");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        Label descError = new Label();
        descError.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        descError.setTextFill(Color.web("#ef4444"));
        descError.setVisible(false);

        descBox.getChildren().addAll(descLabel, descArea, descError);

        // DATE ET HEURE
        VBox dateBox = new VBox(3);
        Label dateLabel = new Label("📅 Date & Heure *");
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        HBox dateTimeBox = new HBox(10);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(180);
        datePicker.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 9);
        hourSpinner.setPrefWidth(70);
        hourSpinner.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        Label hourLabel = new Label("h");
        hourLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.setPrefWidth(70);
        minuteSpinner.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        if (existingEvent != null && existingEvent.getDate_evenement() != null) {
            datePicker.setValue(existingEvent.getDate_evenement().toLocalDate());
            hourSpinner.getValueFactory().setValue(existingEvent.getDate_evenement().getHour());
            minuteSpinner.getValueFactory().setValue(existingEvent.getDate_evenement().getMinute());
        } else {
            datePicker.setValue(LocalDate.now().plusDays(7));
        }

        dateTimeBox.getChildren().addAll(datePicker, hourSpinner, hourLabel, minuteSpinner);

        Label dateError = new Label();
        dateError.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        dateError.setTextFill(Color.web("#ef4444"));
        dateError.setVisible(false);

        dateBox.getChildren().addAll(dateLabel, dateTimeBox, dateError);

        // LIEU
        VBox lieuBox = new VBox(3);
        Label lieuLabel = new Label("📍 Lieu *");
        lieuLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        TextField lieuField = new TextField(existingEvent != null ? existingEvent.getLieu() : "");
        lieuField.setPromptText("Ex: Plage de Sousse");
        lieuField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        Label lieuError = new Label();
        lieuError.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        lieuError.setTextFill(Color.web("#ef4444"));
        lieuError.setVisible(false);

        lieuBox.getChildren().addAll(lieuLabel, lieuField, lieuError);

        // CAPACITÉ
        VBox capaciteBox = new VBox(3);
        Label capaciteLabel = new Label("👥 Capacité max");
        capaciteLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        HBox capaciteControl = new HBox(10);
        capaciteControl.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> capaciteSpinner = new Spinner<>(1, 1000, 50);
        capaciteSpinner.setEditable(true);
        capaciteSpinner.setPrefWidth(100);
        capaciteSpinner.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        Label capaciteHelp = new Label("(optionnel)");
        capaciteHelp.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        capaciteHelp.setTextFill(Color.web("#64748b"));

        if (existingEvent != null && existingEvent.getCapacite_max() != null) {
            capaciteSpinner.getValueFactory().setValue(existingEvent.getCapacite_max());
        }

        capaciteControl.getChildren().addAll(capaciteSpinner, capaciteHelp);
        capaciteBox.getChildren().addAll(capaciteLabel, capaciteControl);

        // --- SECTION IMAGE AVEC Platform.runLater CORRIGÉ ---
        VBox imageSection = new VBox(10);
        imageSection.setPadding(new Insets(15));
        imageSection.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label imageTitle = new Label("🖼️ Image");
        imageTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        // Aperçu
        ImageView previewImageView = new ImageView();
        previewImageView.setFitWidth(200);
        previewImageView.setFitHeight(120);
        previewImageView.setPreserveRatio(true);

        Rectangle previewClip = new Rectangle(200, 120);
        previewClip.setArcWidth(12);
        previewClip.setArcHeight(12);
        previewImageView.setClip(previewClip);

        selectedImagePath = "";
        previewImageView.setImage(new Image("https://via.placeholder.com/200x120/e2e8f0/1e293b?text=Image"));

        if (existingEvent != null && existingEvent.getImage_evenement() != null && !existingEvent.getImage_evenement().isEmpty()) {
            Image existingImage = loadImageFromStorage(existingEvent.getImage_evenement());
            if (existingImage != null) {
                previewImageView.setImage(existingImage);
                selectedImagePath = existingEvent.getImage_evenement();
            }
        }

        VBox previewBox = new VBox(5, previewImageView);
        previewBox.setAlignment(Pos.CENTER);

        // Boutons
        HBox imageButtons = new HBox(10);
        imageButtons.setAlignment(Pos.CENTER);

        Button browseBtn = new Button("📁 Choisir");
        browseBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px;");

        Button generateAIBtn = new Button("🎨 IA");
        generateAIBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px;");
        generateAIBtn.setTooltip(new Tooltip("Générer une image gratuite"));

        Button clearImageBtn = new Button("🗑️");
        clearImageBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6;");
        clearImageBtn.setVisible(selectedImagePath != null && !selectedImagePath.isEmpty());

        ProgressIndicator aiProgress = new ProgressIndicator();
        aiProgress.setPrefSize(25, 25);
        aiProgress.setVisible(false);

        imageButtons.getChildren().addAll(browseBtn, generateAIBtn, clearImageBtn, aiProgress);

        imageSection.getChildren().addAll(imageTitle, previewBox, imageButtons);

        // Assemblage
        form.getChildren().addAll(titleBox, descBox, dateBox, lieuBox, capaciteBox, imageSection);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(500);

        // --- BOUTONS ---
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(15, 20, 20, 20));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8;");
        cancelBtn.setOnAction(e -> {
            selectedImagePath = "";
            dialog.close();
        });

        Button saveBtn = new Button(existingEvent == null ? "Créer" : "Enregistrer");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8;");

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(buttonBox);

        // --- ACTION UPLOAD ---
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

            File file = fc.showOpenDialog(dialog);
            if (file != null) {
                String storedPath = copyImageToStorage(file);
                if (storedPath != null) {
                    selectedImagePath = storedPath;
                    Image newImage = loadImageFromStorage(storedPath);
                    if (newImage != null) {
                        previewImageView.setImage(newImage);
                    }
                    clearImageBtn.setVisible(true);
                }
            }
        });

        // --- ACTION GÉNÉRATION IA - CORRIGÉ avec Platform.runLater ---
        generateAIBtn.setOnAction(e -> {
            String description = descArea.getText().trim();
            String titre = titreField.getText().trim();

            if (description.isEmpty() && titre.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Information");
                alert.setHeaderText("Description ou titre requis");
                alert.setContentText("Veuillez saisir une description pour générer une image.");
                alert.showAndWait();
                return;
            }

            String prompt = description.isEmpty() ? titre : description;

            generateAIBtn.setDisable(true);
            browseBtn.setDisable(true);
            aiProgress.setVisible(true);

            Task<String> generateTask = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    return generateImageWithAI(prompt);
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    String result = getValue();

                    // ✅ CORRECTION : Platform.runLater pour l'interface JavaFX
                    Platform.runLater(() -> {
                        if (result != null) {
                            selectedImagePath = result;
                            Image newImage = loadImageFromStorage(result);
                            if (newImage != null) {
                                previewImageView.setImage(newImage);
                            }
                            clearImageBtn.setVisible(true);

                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Succès");
                            success.setHeaderText("✅ Image ajoutée");
                            success.setContentText("L'image a été téléchargée avec succès.");
                            success.showAndWait();
                        }

                        generateAIBtn.setDisable(false);
                        browseBtn.setDisable(false);
                        aiProgress.setVisible(false);
                    });
                }

                @Override
                protected void failed() {
                    super.failed();

                    // ✅ CORRECTION : Platform.runLater pour l'interface JavaFX
                    Platform.runLater(() -> {
                        generateAIBtn.setDisable(false);
                        browseBtn.setDisable(false);
                        aiProgress.setVisible(false);

                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur");
                        error.setHeaderText("❌ Échec du téléchargement");
                        error.setContentText("Impossible de télécharger l'image. Veuillez réessayer.");
                        error.showAndWait();
                    });
                }
            };

            new Thread(generateTask).start();
        });

        // --- ACTION EFFACER ---
        clearImageBtn.setOnAction(e -> {
            selectedImagePath = "";
            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/e2e8f0/1e293b?text=Image"));
            clearImageBtn.setVisible(false);
        });

        // --- VALIDATION ET SAUVEGARDE ---
        saveBtn.setOnAction(e -> {
            titreError.setVisible(false);
            descError.setVisible(false);
            dateError.setVisible(false);
            lieuError.setVisible(false);

            titreField.setStyle("-fx-border-color: #cbd5e1;");
            descArea.setStyle("-fx-border-color: #cbd5e1;");
            datePicker.setStyle("-fx-border-color: #cbd5e1;");
            lieuField.setStyle("-fx-border-color: #cbd5e1;");

            String titre = titreField.getText().trim();
            String description = descArea.getText().trim();
            LocalDate date = datePicker.getValue();
            String lieu = lieuField.getText().trim();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();

            boolean isValid = true;

            if (titre.isEmpty()) {
                titreField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                titreError.setText("🔴 Titre obligatoire");
                titreError.setVisible(true);
                isValid = false;
            } else if (titre.length() < 3) {
                titreField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                titreError.setText("🔴 Minimum 3 caractères");
                titreError.setVisible(true);
                isValid = false;
            }

            if (description.isEmpty()) {
                descArea.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                descError.setText("🔴 Description obligatoire");
                descError.setVisible(true);
                isValid = false;
            } else if (description.length() < 10) {
                descArea.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                descError.setText("🔴 Minimum 10 caractères");
                descError.setVisible(true);
                isValid = false;
            }

            if (date == null) {
                datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                dateError.setText("🔴 Date obligatoire");
                dateError.setVisible(true);
                isValid = false;
            } else {
                LocalDateTime eventDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
                if (eventDateTime.isBefore(LocalDateTime.now())) {
                    datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                    dateError.setText("🔴 Date future requise");
                    dateError.setVisible(true);
                    isValid = false;
                }
            }

            if (lieu.isEmpty()) {
                lieuField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                lieuError.setText("🔴 Lieu obligatoire");
                lieuError.setVisible(true);
                isValid = false;
            } else if (lieu.length() < 3) {
                lieuField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                lieuError.setText("🔴 Minimum 3 caractères");
                lieuError.setVisible(true);
                isValid = false;
            }

            if (!isValid) return;

            LocalDateTime eventDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

            Event event = existingEvent != null ? existingEvent : new Event();
            event.setTitre(titre);
            event.setDescription(description);
            event.setDate_evenement(eventDateTime);
            event.setLieu(lieu);
            event.setId_organisateur(currentUser.getId());
            event.setCapacite_max(capaciteSpinner.getValue());

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                event.setImage_evenement(selectedImagePath);
            }

            boolean success;
            if (existingEvent == null) {
                success = eventService.addEvent(event);
            } else {
                success = eventService.updateEvent(event);
            }

            if (success) {
                refreshData();
                dialog.close();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✅ Événement " + (existingEvent == null ? "créé" : "modifié") + " !");
                successAlert.showAndWait();
            }
        });

        Scene scene = new Scene(root, 700, 750);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ============ GESTION DES PARTICIPANTS ============

    @SuppressWarnings("unchecked")
    private void showParticipantsDialog(Event event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Participants");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(600);
        content.setPrefHeight(400);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label("👥");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        VBox headerText = new VBox(3);
        Label headerTitle = new Label("Participants");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label headerSubtitle = new Label(event.getTitre().length() > 30 ?
                event.getTitre().substring(0, 27) + "..." : event.getTitre());
        headerSubtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        headerSubtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        header.getChildren().addAll(iconLabel, headerText);

        HBox statsBox = new HBox(15);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12;");

        statsBox.getChildren().addAll(
                createStatSmall("Inscrits", String.valueOf(event.getParticipantsInscrits()), "#3b82f6"),
                createStatSmall("Présents", String.valueOf(event.getParticipantsPresents()), "#10b981"),
                createStatSmall("Absents", String.valueOf(event.getParticipantsAbsents()), "#ef4444")
        );

        TableView<User> participantTable = new TableView<>();
        participantTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getPrenom() + " " + cellData.getValue().getNom().charAt(0) + "."
                ));
        nomCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData -> {
            String fullInfo = cellData.getValue().getSexe();
            if (fullInfo != null && fullInfo.contains("Statut:")) {
                String statut = fullInfo.split("Statut:")[1].trim();
                return new javafx.beans.property.SimpleStringProperty(statut);
            }
            return new javafx.beans.property.SimpleStringProperty("inscrit");
        });
        statutCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                } else {
                    setText(statut.substring(0, 1).toUpperCase());
                    setAlignment(Pos.CENTER);
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));

                    switch (statut.toLowerCase()) {
                        case "present": setStyle("-fx-text-fill: #10b981;"); break;
                        case "absent": setStyle("-fx-text-fill: #ef4444;"); break;
                        default: setStyle("-fx-text-fill: #3b82f6;");
                    }
                }
            }
        });

        participantTable.getColumns().addAll(nomCol, emailCol, statutCol);

        List<User> participants = eventService.getParticipantsByEvent(event.getId_evenement());
        participantTable.setItems(javafx.collections.FXCollections.observableArrayList(participants));

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6;");
        closeBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(header, statsBox, participantTable, closeBtn);
        VBox.setVgrow(participantTable, Priority.ALWAYS);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.show();
    }

    private VBox createStatSmall(String label, String value, String color) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(8, 15, 8, 15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 2 0;");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label titleLbl = new Label(label);
        titleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #475569;");

        card.getChildren().addAll(valLbl, titleLbl);
        return card;
    }

    private void deleteEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ?");
        alert.setContentText("Supprimer " + event.getTitre() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventService.deleteEvent(event.getId_evenement())) {
                refreshData();
            }
        }
    }

    private void scaleNode(javafx.scene.Node node, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }

    public VBox getView() {
        return mainLayout;
    }
}