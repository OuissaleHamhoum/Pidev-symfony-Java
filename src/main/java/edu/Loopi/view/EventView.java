package edu.Loopi.view;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.Notification;
import edu.Loopi.entities.User;
import edu.Loopi.services.AIImageGenerationService;
import edu.Loopi.services.EventService;
import edu.Loopi.services.NotificationService;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class EventView {
    private User currentUser;
    private VBox mainLayout;
    private FlowPane cardsContainer;
    private EventService eventService = new EventService();
    private NotificationService notificationService = new NotificationService();
    private List<Event> allEvents;
    private String selectedImagePath = "";

    // Constantes de couleurs
    private static final String PRIMARY_COLOR = "#4361ee";
    private static final String SUCCESS_COLOR = "#2ecc71";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String WARNING_COLOR = "#f39c12";
    private static final String DARK_COLOR = "#2c3e50";
    private static final String LIGHT_GRAY = "#f8f9fa";
    private static final String BORDER_COLOR = "#e9ecef";
    private static final String AI_COLOR = "#9b59b6";

    // Chemins des dossiers d'upload
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String RESOURCES_DIR = "src" + File.separator + "main" + File.separator +
            "resources" + File.separator;
    private static final String UPLOADS_DIR = RESOURCES_DIR + "uploads" + File.separator;
    private static final String EVENTS_DIR = UPLOADS_DIR + "events" + File.separator;
    private static final String AI_DIR = UPLOADS_DIR + "ai_generated" + File.separator;

    private static final String FULL_EVENTS_PATH = PROJECT_ROOT + File.separator + EVENTS_DIR;
    private static final String FULL_AI_PATH = PROJECT_ROOT + File.separator + AI_DIR;

    private static final String DB_EVENTS_PATH = "uploads/events/";
    private static final String DB_AI_PATH = "uploads/ai_generated/";

    // Composants pour les filtres
    private TextField searchField = new TextField();
    private ComboBox<String> statusFilter = new ComboBox<>();
    private ComboBox<String> validationFilter = new ComboBox<>();
    private ComboBox<String> publicationFilter = new ComboBox<>();
    private ComboBox<String> sortCombo = new ComboBox<>();
    private HBox statsBar = new HBox(15);
    private Label messageInfoLabel;

    public EventView(User user) {
        this.currentUser = user;
        this.mainLayout = new VBox(20);
        createUploadDirectories();
        createView();
        loadData();
    }

    private void createUploadDirectories() {
        try {
            File eventsDir = new File(FULL_EVENTS_PATH);
            File aiDir = new File(FULL_AI_PATH);

            if (!eventsDir.exists()) {
                boolean created = eventsDir.mkdirs();
                System.out.println("📁 Dossier events " + (created ? "créé" : "existe déjà") + ": " + FULL_EVENTS_PATH);
            }

            if (!aiDir.exists()) {
                boolean created = aiDir.mkdirs();
                System.out.println("📁 Dossier AI " + (created ? "créé" : "existe déjà") + ": " + FULL_AI_PATH);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur création dossiers: " + e.getMessage());
        }
    }

    private String copyImageToStorage(File sourceFile) {
        try {
            File directory = new File(FULL_EVENTS_PATH);
            if (!directory.exists()) directory.mkdirs();

            String extension = "";
            String fileName = sourceFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            extension = (dotIndex > 0) ? fileName.substring(dotIndex) : ".jpg";

            String uniqueFileName = "event_" + UUID.randomUUID().toString() + extension;
            String fullPath = FULL_EVENTS_PATH + uniqueFileName;

            Files.copy(sourceFile.toPath(), Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);

            File savedFile = new File(fullPath);
            if (savedFile.exists()) {
                System.out.println("✅ Image copiée: " + fullPath);
                return DB_EVENTS_PATH + uniqueFileName;
            }
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Image loadImageFromStorage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            String fileName = imagePath.substring(imagePath.lastIndexOf('/') + 1);

            String[] possiblePaths = {
                    FULL_EVENTS_PATH + fileName,
                    FULL_AI_PATH + fileName,
                    PROJECT_ROOT + File.separator + "src" + File.separator + "main" + File.separator +
                            "resources" + File.separator + imagePath.replace('/', File.separatorChar)
            };

            for (String path : possiblePaths) {
                File imgFile = new File(path);
                if (imgFile.exists()) {
                    Image image = new Image(imgFile.toURI().toString(), true);
                    return image;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Exception lors du chargement: " + e.getMessage());
        }
        return null;
    }

    private void createView() {
        mainLayout.getChildren().clear();
        mainLayout.setPadding(new Insets(0));
        mainLayout.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        VBox header = createHeader();

        // Message d'information
        messageInfoLabel = new Label("");
        messageInfoLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        messageInfoLabel.setTextFill(Color.web("#059669"));
        messageInfoLabel.setPadding(new Insets(5, 40, 0, 40));
        messageInfoLabel.setVisible(false);

        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(20, 40, 10, 40));
        statsBar.setStyle("-fx-background-color: " + LIGHT_GRAY + ";");

        HBox filterBar = createFilterBar();

        cardsContainer = new FlowPane();
        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setAlignment(Pos.TOP_LEFT);
        cardsContainer.setPadding(new Insets(20, 40, 30, 40));

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: null;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainLayout.getChildren().addAll(header, messageInfoLabel, statsBar, filterBar, scrollPane);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(25, 40, 15, 40));
        header.setStyle("-fx-background: linear-gradient(to right, " + PRIMARY_COLOR + ", #3a0ca3);");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📅 Mes Événements");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Nouvel événement");
        addBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + PRIMARY_COLOR + "; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 20; " +
                "-fx-background-radius: 25; -fx-cursor: hand;");
        addBtn.setOnAction(e -> openEventDialog(null));

        topRow.getChildren().addAll(title, spacer, addBtn);

        Label subtitle = new Label("Gérez vos événements écologiques et suivez leur validation");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.rgb(255, 255, 255, 0.9));

        header.getChildren().addAll(topRow, subtitle);
        return header;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15, 40, 15, 40));
        filterBar.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1 0 0 0;");

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 8 12; -fx-background-radius: 10; -fx-cursor: hand;");
        refreshBtn.setTooltip(new Tooltip("Actualiser"));
        refreshBtn.setOnAction(e -> refreshData());

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 20; -fx-padding: 5 12;");
        searchBox.setPrefWidth(250);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 14px;");

        searchField.setPromptText("Rechercher...");
        searchField.setStyle("-fx-background-color: transparent; -fx-pref-width: 200px; -fx-font-size: 13px;");
        searchField.setOnKeyReleased(e -> applyFilters());

        searchBox.getChildren().addAll(searchIcon, searchField);

        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: " + BORDER_COLOR + ";");

        // Filtre Statut (date)
        VBox statusBox = new VBox(2);
        Label statusLabel = new Label("Statut");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");

        statusFilter.getItems().addAll("Tous", "À venir", "En cours", "Passés");
        statusFilter.setValue("Tous");
        statusFilter.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px;");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> applyFilters());

        statusBox.getChildren().addAll(statusLabel, statusFilter);

        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: " + BORDER_COLOR + ";");

        // Filtre Validation
        VBox validationBox = new VBox(2);
        Label validationLabel = new Label("Validation");
        validationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");

        validationFilter.getItems().addAll("Tous", "En attente", "Approuvés", "Refusés");
        validationFilter.setValue("Tous");
        validationFilter.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px;");
        validationFilter.setPrefWidth(120);
        validationFilter.setOnAction(e -> applyFilters());

        validationBox.getChildren().addAll(validationLabel, validationFilter);

        Separator sep3 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep3.setStyle("-fx-background-color: " + BORDER_COLOR + ";");

        // Filtre Publication
        VBox publicationBox = new VBox(2);
        Label publicationLabel = new Label("Publication");
        publicationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");

        publicationFilter.getItems().addAll("Tous", "Brouillon", "Publiés");
        publicationFilter.setValue("Tous");
        publicationFilter.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px;");
        publicationFilter.setPrefWidth(120);
        publicationFilter.setOnAction(e -> applyFilters());

        publicationBox.getChildren().addAll(publicationLabel, publicationFilter);

        Separator sep4 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep4.setStyle("-fx-background-color: " + BORDER_COLOR + ";");

        // Tri
        VBox sortBox = new VBox(2);
        Label sortLabel = new Label("Trier par");
        sortLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");

        sortCombo.getItems().addAll(
                "📅 Plus récent",
                "📅 Moins récent",
                "👥 Plus de participants"
        );
        sortCombo.setValue("📅 Plus récent");
        sortCombo.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px;");
        sortCombo.setPrefWidth(130);
        sortCombo.setOnAction(e -> applyFilters());

        sortBox.getChildren().addAll(sortLabel, sortCombo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button resetBtn = new Button("✕ Réinitialiser");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + DANGER_COLOR + "; " +
                "-fx-border-color: " + DANGER_COLOR + "; -fx-border-radius: 15; -fx-padding: 6 15; " +
                "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;");
        resetBtn.setOnAction(e -> resetFilters());

        filterBar.getChildren().addAll(refreshBtn, searchBox, sep1, statusBox, sep2, validationBox, sep3, publicationBox, sep4, sortBox, spacer, resetBtn);
        return filterBar;
    }

    private void loadData() {
        allEvents = eventService.getEventsByOrganisateur(currentUser.getId());

        // Message si aucun événement
        if (allEvents.isEmpty()) {
            messageInfoLabel.setText("🔔 Vous n'avez pas encore créé d'événement. Cliquez sur 'Nouvel événement' pour commencer !");
            messageInfoLabel.setVisible(true);
        } else {
            messageInfoLabel.setVisible(false);
        }

        updateStats();
        applyFilters();
    }

    private void refreshData() {
        loadData();
    }

    private void resetFilters() {
        searchField.clear();
        statusFilter.setValue("Tous");
        validationFilter.setValue("Tous");
        publicationFilter.setValue("Tous");
        sortCombo.setValue("📅 Plus récent");
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = statusFilter.getValue();
        String selectedValidation = validationFilter.getValue();
        String selectedPublication = publicationFilter.getValue();
        String selectedSort = sortCombo.getValue();

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
                .filter(event -> {
                    if (selectedValidation == null || "Tous".equals(selectedValidation)) return true;
                    String eventValidation = event.getStatutValidation();
                    String filterValidation = selectedValidation.toLowerCase();

                    if (filterValidation.equals("en attente") && "en_attente".equals(eventValidation)) return true;
                    if (filterValidation.equals("approuvés") && "approuve".equals(eventValidation)) return true;
                    if (filterValidation.equals("refusés") && "refuse".equals(eventValidation)) return true;
                    return false;
                })
                .filter(event -> {
                    if (selectedPublication == null || "Tous".equals(selectedPublication)) return true;
                    boolean estPublie = event.isEstPublie();
                    if (selectedPublication.equals("Brouillon") && !estPublie) return true;
                    if (selectedPublication.equals("Publiés") && estPublie) return true;
                    return false;
                })
                .collect(Collectors.toList());

        if (selectedSort != null) {
            switch (selectedSort) {
                case "📅 Plus récent":
                    filtered.sort((e1, e2) -> e2.getDate_evenement().compareTo(e1.getDate_evenement()));
                    break;
                case "📅 Moins récent":
                    filtered.sort((e1, e2) -> e1.getDate_evenement().compareTo(e2.getDate_evenement()));
                    break;
                case "👥 Plus de participants":
                    filtered.sort((e1, e2) -> Integer.compare(e2.getParticipantsCount(), e1.getParticipantsCount()));
                    break;
            }
        }

        displayCards(filtered);
    }

    private void updateStats() {
        statsBar.getChildren().clear();

        int total = allEvents.size();
        int aVenir = (int) allEvents.stream().filter(e -> "à venir".equals(e.getStatut())).count();
        int enCours = (int) allEvents.stream().filter(e -> "en cours".equals(e.getStatut())).count();
        int passes = (int) allEvents.stream().filter(e -> "passé".equals(e.getStatut())).count();
        int enAttente = (int) allEvents.stream().filter(e -> "en_attente".equals(e.getStatutValidation())).count();
        int approuves = (int) allEvents.stream().filter(e -> "approuve".equals(e.getStatutValidation())).count();
        int refuses = (int) allEvents.stream().filter(e -> "refuse".equals(e.getStatutValidation())).count();
        int publies = (int) allEvents.stream().filter(e -> e.isEstPublie()).count();
        int brouillons = (int) allEvents.stream().filter(e -> !e.isEstPublie()).count();
        int totalParticipants = allEvents.stream().mapToInt(Event::getParticipantsCount).sum();

        statsBar.getChildren().addAll(
                createStatCard("📊", String.valueOf(total), "Total", PRIMARY_COLOR),
                createStatCard("⏳", String.valueOf(aVenir), "À venir", WARNING_COLOR),
                createStatCard("🔄", String.valueOf(enCours), "En cours", "#9b59b6"),
                createStatCard("✅", String.valueOf(passes), "Passés", "#6c757d"),
                createStatCard("⏰", String.valueOf(enAttente), "En attente", "#f39c12"),
                createStatCard("✓", String.valueOf(approuves), "Approuvés", SUCCESS_COLOR),
                createStatCard("❌", String.valueOf(refuses), "Refusés", DANGER_COLOR),
                createStatCard("📢", String.valueOf(publies), "Publiés", "#2ecc71"),
                createStatCard("📝", String.valueOf(brouillons), "Brouillons", "#95a5a6"),
                createStatCard("👥", String.valueOf(totalParticipants), "Participants", "#9b59b6")
        );
    }

    private VBox createStatCard(String icon, String value, String label, String color) {
        VBox card = new VBox(3);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 15, 8, 15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 0 3;");
        card.setPrefWidth(130);

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        HBox iconBox = new HBox(5);
        iconBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 12px;");
        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        iconBox.getChildren().addAll(iconLbl, labelLbl);

        card.getChildren().addAll(valLbl, iconBox);
        return card;
    }

    private void displayCards(List<Event> events) {
        cardsContainer.getChildren().clear();

        if (events.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(60));
            emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
            emptyState.setMaxWidth(500);

            Label emptyIcon = new Label("📅");
            emptyIcon.setFont(Font.font("System", FontWeight.BOLD, 48));

            String message = searchField.getText().isEmpty() && "Tous".equals(statusFilter.getValue()) ?
                    "Vous n'avez pas encore créé d'événement" :
                    "Aucun événement ne correspond à vos critères";

            Label emptyText = new Label(message);
            emptyText.setFont(Font.font("System", FontWeight.BOLD, 18));
            emptyText.setTextFill(Color.web(DARK_COLOR));

            Label emptySubtext = new Label("Créez votre premier événement en cliquant sur le bouton");
            emptySubtext.setFont(Font.font("System", 14));
            emptySubtext.setTextFill(Color.web("#6c757d"));

            Button createFirstBtn = new Button("➕ Créer un événement");
            createFirstBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 30; -fx-cursor: hand;");
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
        card.setPrefSize(340, 520);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3); -fx-cursor: hand;");

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 15, 0, 0, 5); -fx-scale-x: 1.01; -fx-scale-y: 1.01;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");
        });

        StackPane imgContainer = new StackPane();
        imgContainer.setPrefSize(340, 160);
        imgContainer.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 16 16 0 0;");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(340);
        imgView.setFitHeight(160);
        imgView.setPreserveRatio(false);

        Image image = loadImageFromStorage(event.getImage_evenement());
        if (image != null) {
            imgView.setImage(image);
        } else {
            imgView.setImage(new Image("https://via.placeholder.com/340x160/" + PRIMARY_COLOR.substring(1) + "/ffffff?text=ÉVÉNEMENT"));
        }

        Rectangle clip = new Rectangle(340, 160);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imgView.setClip(clip);
        imgContainer.getChildren().add(imgView);

        // Badge statut événement (date)
        String statut = event.getStatut();
        Label statusBadge = new Label(statut.substring(0, 1).toUpperCase() + statut.substring(1));
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 11));
        statusBadge.setTextFill(Color.WHITE);
        statusBadge.setPadding(new Insets(4, 12, 4, 12));

        String statusColor;
        switch (statut.toLowerCase()) {
            case "à venir":
                statusColor = WARNING_COLOR;
                break;
            case "en cours":
                statusColor = "#9b59b6";
                break;
            case "passé":
                statusColor = "#6c757d";
                break;
            default:
                statusColor = "#6c757d";
        }
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 20;");
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(8, 8, 0, 0));
        imgContainer.getChildren().add(statusBadge);

        // Badge validation
        Label validationBadge = new Label(event.getStatutValidationFr());
        validationBadge.setFont(Font.font("System", FontWeight.BOLD, 11));
        validationBadge.setTextFill(Color.WHITE);
        validationBadge.setPadding(new Insets(4, 12, 4, 12));
        validationBadge.setStyle("-fx-background-color: " + event.getStatutValidationColor() + "; -fx-background-radius: 20;");
        StackPane.setAlignment(validationBadge, Pos.TOP_LEFT);
        StackPane.setMargin(validationBadge, new Insets(8, 0, 0, 8));
        imgContainer.getChildren().add(validationBadge);

        // Badge publication
        Label publishBadge = new Label(event.isEstPublie() ? "📢 Publié" : "📝 Brouillon");
        publishBadge.setFont(Font.font("System", FontWeight.BOLD, 11));
        publishBadge.setTextFill(Color.WHITE);
        publishBadge.setPadding(new Insets(4, 12, 4, 12));
        publishBadge.setStyle("-fx-background-color: " +
                (event.isEstPublie() ? "#2ecc71" : "#95a5a6") + "; -fx-background-radius: 20;");
        StackPane.setAlignment(publishBadge, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(publishBadge, new Insets(0, 8, 8, 0));
        imgContainer.getChildren().add(publishBadge);

        // Badge commentaire (si existe)
        if (event.getCommentaireValidation() != null && !event.getCommentaireValidation().isEmpty()) {
            Label commentBadge = new Label("💬 Commentaire");
            commentBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
            commentBadge.setTextFill(Color.WHITE);
            commentBadge.setPadding(new Insets(3, 10, 3, 10));
            commentBadge.setStyle("-fx-background-color: #3498db; -fx-background-radius: 20;");
            StackPane.setAlignment(commentBadge, Pos.BOTTOM_LEFT);
            StackPane.setMargin(commentBadge, new Insets(0, 0, 8, 8));
            imgContainer.getChildren().add(commentBadge);

            Tooltip.install(commentBadge, new Tooltip("Commentaire: " + event.getCommentaireValidation()));
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(12, 15, 15, 15));

        HBox dateLieu = new HBox(8);
        dateLieu.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅 " + event.getFormattedDate().substring(0, 10));
        dateLabel.setFont(Font.font(11));
        dateLabel.setTextFill(Color.web("#6c757d"));

        Label lieuLabel = new Label("📍 " + (event.getLieu() != null && event.getLieu().length() > 15 ?
                event.getLieu().substring(0, 12) + "..." : event.getLieu()));
        lieuLabel.setFont(Font.font(11));
        lieuLabel.setTextFill(Color.web("#6c757d"));

        dateLieu.getChildren().addAll(dateLabel, lieuLabel);

        Label titreLabel = new Label(event.getTitre());
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titreLabel.setTextFill(Color.web(DARK_COLOR));
        titreLabel.setWrapText(true);
        titreLabel.setMaxHeight(44);

        Label descLabel = new Label();
        String desc = event.getDescription();
        if (desc != null && desc.length() > 70) {
            desc = desc.substring(0, 67) + "...";
        }
        descLabel.setText(desc != null ? desc : "");
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font(11));
        descLabel.setTextFill(Color.web("#6c757d"));
        descLabel.setMaxHeight(40);

        // Informations de capacité
        HBox capacityInfo = new HBox(15);
        capacityInfo.setAlignment(Pos.CENTER_LEFT);
        capacityInfo.setPadding(new Insets(5, 0, 5, 0));

        VBox countBox = new VBox(2);
        Label participantsCount = new Label(String.valueOf(event.getParticipantsCount()));
        participantsCount.setFont(Font.font("System", FontWeight.BOLD, 18));
        participantsCount.setTextFill(Color.web(PRIMARY_COLOR));

        Label participantsLabel = new Label("participants");
        participantsLabel.setFont(Font.font(10));
        participantsLabel.setTextFill(Color.web("#6c757d"));

        countBox.getChildren().addAll(participantsCount, participantsLabel);

        if (event.getCapacite_max() != null) {
            VBox capaciteBox = new VBox(2);
            Label capaciteCount = new Label(String.valueOf(event.getCapacite_max()));
            capaciteCount.setFont(Font.font("System", FontWeight.BOLD, 18));
            capaciteCount.setTextFill(Color.web("#9b59b6"));

            Label capaciteLabel = new Label("places max");
            capaciteLabel.setFont(Font.font(10));
            capaciteLabel.setTextFill(Color.web("#6c757d"));

            capaciteBox.getChildren().addAll(capaciteCount, capaciteLabel);
            capacityInfo.getChildren().add(capaciteBox);
        }

        capacityInfo.getChildren().add(countBox);

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(5, 0, 0, 0));

        // Bouton Publier/Dépublier (uniquement si approuvé)
        if ("approuve".equals(event.getStatutValidation())) {
            Button publishBtn = new Button(event.isEstPublie() ? "🔒 Dépublier" : "📢 Publier");
            publishBtn.setStyle("-fx-background-color: " +
                    (event.isEstPublie() ? "#e67e22" : "#2ecc71") + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
            publishBtn.setOnAction(e -> {
                if (event.isEstPublie()) {
                    // Dépublier
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Dépublier l'événement");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Voulez-vous dépublier cet événement ? Il ne sera plus visible par les participants.");

                    if (confirm.showAndWait().get() == ButtonType.OK) {
                        eventService.depublierEvenement(event.getId_evenement());
                        refreshData();
                        showAlert("Succès", "Événement dépublié avec succès");
                    }
                } else {
                    // Publier
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Publier l'événement");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Voulez-vous publier cet événement ? Il sera visible par tous les participants.");

                    if (confirm.showAndWait().get() == ButtonType.OK) {
                        eventService.publierEvenement(event.getId_evenement());
                        refreshData();
                        showAlert("Succès", "Événement publié avec succès");
                    }
                }
            });
            actions.getChildren().add(publishBtn);
        }

        Button btnEdit = new Button("✏️ Modifier");
        btnEdit.setStyle("-fx-background-color: " + WARNING_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> openEventDialog(event));

        Button btnParticipants = new Button("👥 " + event.getParticipantsCount());
        btnParticipants.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnParticipants.setOnAction(e -> showParticipantsDialog(event));

        Button btnDelete = new Button("🗑️");
        btnDelete.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> deleteEvent(event));

        Button btnNotif = new Button("🔔 Notif");
        btnNotif.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnNotif.setOnAction(e -> showEventNotifications(event));

        actions.getChildren().addAll(btnEdit, btnParticipants, btnDelete, btnNotif);

        content.getChildren().addAll(dateLieu, titreLabel, descLabel, capacityInfo, actions);
        card.getChildren().addAll(imgContainer, content);

        return card;
    }

    private void showParticipantsDialog(Event event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Participants - " + event.getTitre());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(700);
        content.setPrefHeight(500);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label("👥");
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 28));

        VBox headerText = new VBox(5);
        Label headerTitle = new Label(event.getTitre());
        headerTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        headerTitle.setTextFill(Color.web(DARK_COLOR));

        Label headerSubtitle = new Label(event.getFormattedDate() + " - " + event.getLieu());
        headerSubtitle.setFont(Font.font("System", 12));
        headerSubtitle.setTextFill(Color.web("#6c757d"));

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        header.getChildren().addAll(iconLabel, headerText);

        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 12;");

        int placesRestantes = event.getPlacesRestantes();
        statsBox.getChildren().addAll(
                createStatSmall("Inscrits", String.valueOf(event.getParticipantsInscrits()), WARNING_COLOR),
                createStatSmall("Présents", String.valueOf(event.getParticipantsPresents()), SUCCESS_COLOR),
                createStatSmall("Absents", String.valueOf(event.getParticipantsAbsents()), DANGER_COLOR),
                createStatSmall("Places restantes", placesRestantes >= 0 ? String.valueOf(placesRestantes) : "Illimité", "#9b59b6")
        );

        TableView<User> participantTable = new TableView<>();
        participantTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        participantTable.setStyle("-fx-background-color: transparent;");

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
                    setText(statut.substring(0, 1).toUpperCase() + statut.substring(1));
                    setAlignment(Pos.CENTER);
                    setFont(Font.font("System", FontWeight.BOLD, 12));

                    switch (statut.toLowerCase()) {
                        case "present":
                            setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
                            break;
                        case "absent":
                            setStyle("-fx-text-fill: " + DANGER_COLOR + ";");
                            break;
                        default:
                            setStyle("-fx-text-fill: " + WARNING_COLOR + ";");
                    }
                }
            }
        });

        participantTable.getColumns().addAll(nomCol, emailCol, statutCol);

        List<User> participants = eventService.getParticipantsByEvent(event.getId_evenement());
        participantTable.setItems(FXCollections.observableArrayList(participants));

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: " + BORDER_COLOR + "; -fx-text-fill: " + DARK_COLOR + "; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;");
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
        titleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");

        card.getChildren().addAll(valLbl, titleLbl);
        return card;
    }

    private void deleteEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'événement ?");
        alert.setContentText("Voulez-vous vraiment supprimer \"" + event.getTitre() + "\" ?\nCette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventService.deleteEvent(event.getId_evenement())) {
                refreshData();
                showAlert("Succès", "✅ Événement supprimé avec succès !");
            }
        }
    }

    private void showEventNotifications(Event event) {
        Stage notifStage = new Stage();
        notifStage.initModality(Modality.APPLICATION_MODAL);
        notifStage.initOwner(mainLayout.getScene().getWindow());
        notifStage.setTitle("Notifications - " + event.getTitre());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(600);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("🔔");
        iconLabel.setFont(Font.font("System", 32));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label("Historique des notifications");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(DARK_COLOR));

        Label eventLabel = new Label(event.getTitre());
        eventLabel.setFont(Font.font("System", 14));
        eventLabel.setTextFill(Color.web("#6c757d"));

        headerText.getChildren().addAll(titleLabel, eventLabel);
        header.getChildren().addAll(iconLabel, headerText);

        ListView<Notification> notifList = new ListView<>();
        notifList.setPrefHeight(400);
        notifList.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null || n.getIdEvenement() != event.getId_evenement()) {
                    setGraphic(null);
                } else {
                    VBox cell = new VBox(8);
                    cell.setPadding(new Insets(12));
                    cell.setStyle("-fx-background-color: " + (n.isRead() ? "#f8fafc" : "#eff6ff") +
                            "; -fx-background-radius: 8; -fx-border-color: " + (n.isRead() ? "#e2e8f0" : "#3b82f6") +
                            "; -fx-border-radius: 8;");

                    HBox titleRow = new HBox(10);
                    titleRow.setAlignment(Pos.CENTER_LEFT);

                    String icon = "";
                    if (n.getType().contains("APPROUVE")) {
                        icon = "✅";
                    } else if (n.getType().contains("REFUSE")) {
                        icon = "❌";
                    } else if (n.getType().contains("PUBLIE")) {
                        icon = "📢";
                    } else if (n.getType().contains("NOUVEAU_PARTICIPANT")) {
                        icon = "👤";
                    } else if (n.getType().contains("PARTICIPANT_ANNULE")) {
                        icon = "🚫";
                    } else {
                        icon = "📝";
                    }

                    Label iconLabel = new Label(icon);
                    iconLabel.setFont(Font.font("System", 16));

                    Label titreNotif = new Label(n.getTitre());
                    titreNotif.setFont(Font.font("System", FontWeight.BOLD, 14));
                    titreNotif.setTextFill(Color.web("#0f172a"));

                    if (!n.isRead()) {
                        Label newBadge = new Label("NOUVEAU");
                        newBadge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                                "-fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 10; -fx-font-weight: bold;");
                        titleRow.getChildren().addAll(iconLabel, titreNotif, newBadge);
                    } else {
                        titleRow.getChildren().addAll(iconLabel, titreNotif);
                    }

                    Label messageLabel = new Label(n.getMessage());
                    messageLabel.setWrapText(true);
                    messageLabel.setFont(Font.font("System", 12));
                    messageLabel.setTextFill(Color.web("#475569"));

                    // Détails supplémentaires
                    VBox detailsBox = new VBox(5);
                    if (n.getNomAdmin() != null && !n.getNomAdmin().isEmpty()) {
                        Label adminLabel = new Label("👤 Admin: " + n.getNomAdmin() +
                                (n.getEmailAdmin() != null ? " (" + n.getEmailAdmin() + ")" : ""));
                        adminLabel.setFont(Font.font("System", 11));
                        adminLabel.setTextFill(Color.web("#64748b"));
                        detailsBox.getChildren().add(adminLabel);
                    }

                    if (n.getCommentaire() != null && !n.getCommentaire().isEmpty()) {
                        Label commentLabel = new Label("💬 Commentaire: " + n.getCommentaire());
                        commentLabel.setWrapText(true);
                        commentLabel.setFont(Font.font("System", 11));
                        commentLabel.setTextFill(Color.web("#64748b"));
                        detailsBox.getChildren().add(commentLabel);
                    }

                    Label dateLabel = new Label(n.getFormattedDate());
                    dateLabel.setFont(Font.font("System", 10));
                    dateLabel.setTextFill(Color.web("#64748b"));

                    cell.getChildren().addAll(titleRow, messageLabel);
                    if (!detailsBox.getChildren().isEmpty()) {
                        cell.getChildren().add(detailsBox);
                    }
                    cell.getChildren().add(dateLabel);

                    setGraphic(cell);
                }
            }
        });

        // Filtrer les notifications pour cet événement
        ObservableList<Notification> eventNotifs = FXCollections.observableArrayList();
        List<Notification> allNotifs = notificationService.getNotificationsForOrganisateur(currentUser.getId());
        for (Notification n : allNotifs) {
            if (n.getIdEvenement() == event.getId_evenement()) {
                eventNotifs.add(n);
            }
        }
        notifList.setItems(eventNotifs);

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> notifStage.close());

        content.getChildren().addAll(header, notifList, closeBtn);

        Scene scene = new Scene(content);
        notifStage.setScene(scene);
        notifStage.show();
    }

    @SuppressWarnings("unchecked")
    private void openEventDialog(Event existingEvent) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existingEvent == null ? "🎯 Créer un événement" : "✏️ Modifier l'événement");
        dialog.setResizable(false);
        dialog.setWidth(700);
        dialog.setHeight(800);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + LIGHT_GRAY + ";");

        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");
        header.setAlignment(Pos.CENTER_LEFT);

        Label headerIcon = new Label(existingEvent == null ? "🎯" : "✏️");
        headerIcon.setFont(Font.font("System", 28));
        headerIcon.setTextFill(Color.WHITE);

        VBox headerText = new VBox(3);
        Label headerTitle = new Label(existingEvent == null ? "Créer un événement" : "Modifier l'événement");
        headerTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        headerTitle.setTextFill(Color.WHITE);

        Label headerSubtitle = new Label(existingEvent == null
                ? "Remplissez les informations ci-dessous"
                : "Modifiez les informations");
        headerSubtitle.setFont(Font.font("System", FontWeight.NORMAL, 12));
        headerSubtitle.setTextFill(Color.web("#e0e0e0"));

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        header.getChildren().addAll(headerIcon, headerText);

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        // TITRE
        VBox titleBox = new VBox(3);
        Label titleLabel = new Label("📝 Titre *");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextField titreField = new TextField(existingEvent != null ? existingEvent.getTitre() : "");
        titreField.setPromptText("Ex: Nettoyage de la plage");
        titreField.setStyle("-fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        Label titreError = new Label();
        titreError.setFont(Font.font("System", FontWeight.NORMAL, 11));
        titreError.setTextFill(Color.web(DANGER_COLOR));
        titreError.setVisible(false);

        titleBox.getChildren().addAll(titleLabel, titreField, titreError);

        // DESCRIPTION
        VBox descBox = new VBox(3);
        Label descLabel = new Label("📄 Description *");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextArea descArea = new TextArea(existingEvent != null ? existingEvent.getDescription() : "");
        descArea.setPromptText("Décrivez votre événement...");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        Label descError = new Label();
        descError.setFont(Font.font("System", FontWeight.NORMAL, 11));
        descError.setTextFill(Color.web(DANGER_COLOR));
        descError.setVisible(false);

        descBox.getChildren().addAll(descLabel, descArea, descError);

        // DATE ET HEURE
        VBox dateBox = new VBox(3);
        Label dateLabel = new Label("📅 Date & Heure *");
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox dateTimeBox = new HBox(10);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(180);
        datePicker.setStyle("-fx-background-radius: 8; -fx-padding: 8; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 9);
        hourSpinner.setPrefWidth(70);
        hourSpinner.setStyle("-fx-background-radius: 8; -fx-padding: 6; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        Label hourLabel = new Label("h");
        hourLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.setPrefWidth(70);
        minuteSpinner.setStyle("-fx-background-radius: 8; -fx-padding: 6; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        if (existingEvent != null && existingEvent.getDate_evenement() != null) {
            datePicker.setValue(existingEvent.getDate_evenement().toLocalDate());
            hourSpinner.getValueFactory().setValue(existingEvent.getDate_evenement().getHour());
            minuteSpinner.getValueFactory().setValue(existingEvent.getDate_evenement().getMinute());
        } else {
            datePicker.setValue(LocalDate.now().plusDays(7));
        }

        dateTimeBox.getChildren().addAll(datePicker, hourSpinner, hourLabel, minuteSpinner);

        Label dateError = new Label();
        dateError.setFont(Font.font("System", FontWeight.NORMAL, 11));
        dateError.setTextFill(Color.web(DANGER_COLOR));
        dateError.setVisible(false);

        dateBox.getChildren().addAll(dateLabel, dateTimeBox, dateError);

        // LIEU
        VBox lieuBox = new VBox(3);
        Label lieuLabel = new Label("📍 Lieu *");
        lieuLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextField lieuField = new TextField(existingEvent != null ? existingEvent.getLieu() : "");
        lieuField.setPromptText("Ex: Plage de Sousse");
        lieuField.setStyle("-fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        Label lieuError = new Label();
        lieuError.setFont(Font.font("System", FontWeight.NORMAL, 11));
        lieuError.setTextFill(Color.web(DANGER_COLOR));
        lieuError.setVisible(false);

        lieuBox.getChildren().addAll(lieuLabel, lieuField, lieuError);

        // CAPACITÉ
        VBox capaciteBox = new VBox(3);
        Label capaciteLabel = new Label("👥 Capacité max");
        capaciteLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox capaciteControl = new HBox(10);
        capaciteControl.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> capaciteSpinner = new Spinner<>(1, 1000, 50);
        capaciteSpinner.setEditable(true);
        capaciteSpinner.setPrefWidth(100);
        capaciteSpinner.setStyle("-fx-background-radius: 8; -fx-padding: 6; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        CheckBox capaciteIllimitee = new CheckBox("Illimitée");
        capaciteIllimitee.setFont(Font.font("System", FontWeight.NORMAL, 12));

        if (existingEvent != null && existingEvent.getCapacite_max() != null) {
            capaciteSpinner.getValueFactory().setValue(existingEvent.getCapacite_max());
        } else {
            capaciteSpinner.getValueFactory().setValue(50);
        }

        capaciteIllimitee.setOnAction(e -> {
            if (capaciteIllimitee.isSelected()) {
                capaciteSpinner.setDisable(true);
                capaciteSpinner.getValueFactory().setValue(null);
            } else {
                capaciteSpinner.setDisable(false);
                capaciteSpinner.getValueFactory().setValue(50);
            }
        });

        capaciteControl.getChildren().addAll(capaciteSpinner, capaciteIllimitee);
        capaciteBox.getChildren().addAll(capaciteLabel, capaciteControl);

        // SECTION IMAGE
        VBox imageSection = new VBox(10);
        imageSection.setPadding(new Insets(15));
        imageSection.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 12;");

        Label imageTitle = new Label("🖼️ Image");
        imageTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        ImageView previewImageView = new ImageView();
        previewImageView.setFitWidth(200);
        previewImageView.setFitHeight(120);
        previewImageView.setPreserveRatio(true);

        Rectangle previewClip = new Rectangle(200, 120);
        previewClip.setArcWidth(12);
        previewClip.setArcHeight(12);
        previewImageView.setClip(previewClip);

        selectedImagePath = "";
        previewImageView.setImage(new Image("https://via.placeholder.com/200x120/" + PRIMARY_COLOR.substring(1) + "/ffffff?text=Image"));

        if (existingEvent != null && existingEvent.getImage_evenement() != null && !existingEvent.getImage_evenement().isEmpty()) {
            Image existingImage = loadImageFromStorage(existingEvent.getImage_evenement());
            if (existingImage != null) {
                previewImageView.setImage(existingImage);
                selectedImagePath = existingEvent.getImage_evenement();
            }
        }

        VBox previewBox = new VBox(5, previewImageView);
        previewBox.setAlignment(Pos.CENTER);

        FlowPane imageButtons = new FlowPane();
        imageButtons.setAlignment(Pos.CENTER);
        imageButtons.setHgap(10);
        imageButtons.setVgap(10);
        imageButtons.setPadding(new Insets(5, 0, 5, 0));

        Button browseBtn = new Button("📁 Choisir");
        browseBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px; -fx-cursor: hand;");

        Button clearImageBtn = new Button("🗑️ Effacer");
        clearImageBtn.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px; -fx-cursor: hand;");
        clearImageBtn.setVisible(!selectedImagePath.isEmpty());

        Button aiGenerateBtn = new Button("✨ Générer avec IA");
        aiGenerateBtn.setStyle("-fx-background-color: " + AI_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px; -fx-cursor: hand;");
        aiGenerateBtn.setTooltip(new Tooltip("Générer une image avec Stability AI"));

        ProgressIndicator aiProgressIndicator = new ProgressIndicator();
        aiProgressIndicator.setVisible(false);
        aiProgressIndicator.setPrefSize(25, 25);

        imageButtons.getChildren().addAll(browseBtn, clearImageBtn, aiGenerateBtn, aiProgressIndicator);
        imageSection.getChildren().addAll(imageTitle, previewBox, imageButtons);

        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            File file = fc.showOpenDialog(dialog);
            if (file != null) {
                String storedPath = copyImageToStorage(file);
                if (storedPath != null) {
                    selectedImagePath = storedPath;
                    Image newImage = loadImageFromStorage(storedPath);
                    if (newImage != null) {
                        previewImageView.setImage(newImage);
                        clearImageBtn.setVisible(true);
                    }
                }
            }
        });

        clearImageBtn.setOnAction(e -> {
            selectedImagePath = "";
            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/" + PRIMARY_COLOR.substring(1) + "/ffffff?text=Image"));
            clearImageBtn.setVisible(false);
        });

        AIImageGenerationService aiImageService = new AIImageGenerationService();

        if (aiImageService.isConfigured()) {
            aiGenerateBtn.setText("✨ Générer avec " + aiImageService.getModelName());
        }

        aiGenerateBtn.setOnAction(e -> {
            String titre = titreField.getText().trim();
            String description = descArea.getText().trim();

            if (titre.isEmpty()) {
                showAlert("Information manquante", "Veuillez d'abord saisir un titre");
                return;
            }

            if (description.isEmpty()) {
                showAlert("Information manquante", "Veuillez d'abord saisir une description");
                return;
            }

            aiGenerateBtn.setDisable(true);
            aiProgressIndicator.setVisible(true);
            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/9b59b6/ffffff?text=Génération..."));

            new Thread(() -> {
                try {
                    String generatedPath = aiImageService.generateEventImage(titre, description);

                    Platform.runLater(() -> {
                        aiProgressIndicator.setVisible(false);
                        aiGenerateBtn.setDisable(false);

                        if (generatedPath != null) {
                            selectedImagePath = generatedPath;
                            Image newImage = loadImageFromStorage(generatedPath);
                            if (newImage != null) {
                                previewImageView.setImage(newImage);
                                clearImageBtn.setVisible(true);
                                showAlert("Succès", "✅ Image générée avec succès !");
                            } else {
                                previewImageView.setImage(new Image("https://via.placeholder.com/200x120/" + PRIMARY_COLOR.substring(1) + "/ffffff?text=Image"));
                                showAlert("Erreur", "❌ Échec du chargement de l'image");
                            }
                        } else {
                            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/" + PRIMARY_COLOR.substring(1) + "/ffffff?text=Image"));
                            showAlert("Erreur", "❌ Échec de la génération");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        aiProgressIndicator.setVisible(false);
                        aiGenerateBtn.setDisable(false);
                        previewImageView.setImage(new Image("https://via.placeholder.com/200x120/" + PRIMARY_COLOR.substring(1) + "/ffffff?text=Image"));
                        showAlert("Erreur", "❌ Erreur: " + ex.getMessage());
                    });
                }
            }).start();
        });

        form.getChildren().addAll(titleBox, descBox, dateBox, lieuBox, capaciteBox, imageSection);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(500);

        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(15, 20, 20, 20));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: " + BORDER_COLOR + "; -fx-text-fill: " + DARK_COLOR + "; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            selectedImagePath = "";
            dialog.close();
        });

        Button saveBtn = new Button(existingEvent == null ? "Soumettre pour validation" : "Enregistrer");
        saveBtn.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            titreError.setVisible(false);
            descError.setVisible(false);
            dateError.setVisible(false);
            lieuError.setVisible(false);

            String titre = titreField.getText().trim();
            String description = descArea.getText().trim();
            LocalDate date = datePicker.getValue();
            String lieu = lieuField.getText().trim();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();

            boolean isValid = true;

            if (titre.isEmpty()) {
                titreError.setText("🔴 Titre obligatoire");
                titreError.setVisible(true);
                isValid = false;
            } else if (titre.length() < 3) {
                titreError.setText("🔴 Minimum 3 caractères");
                titreError.setVisible(true);
                isValid = false;
            }

            if (description.isEmpty()) {
                descError.setText("🔴 Description obligatoire");
                descError.setVisible(true);
                isValid = false;
            } else if (description.length() < 10) {
                descError.setText("🔴 Minimum 10 caractères");
                descError.setVisible(true);
                isValid = false;
            }

            if (date == null) {
                dateError.setText("🔴 Date obligatoire");
                dateError.setVisible(true);
                isValid = false;
            } else {
                LocalDateTime eventDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
                if (eventDateTime.isBefore(LocalDateTime.now())) {
                    dateError.setText("🔴 Date future requise");
                    dateError.setVisible(true);
                    isValid = false;
                }
            }

            if (lieu.isEmpty()) {
                lieuError.setText("🔴 Lieu obligatoire");
                lieuError.setVisible(true);
                isValid = false;
            } else if (lieu.length() < 3) {
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

            if (!capaciteIllimitee.isSelected()) {
                event.setCapacite_max(capaciteSpinner.getValue());
            } else {
                event.setCapacite_max(null);
            }

            if (!selectedImagePath.isEmpty()) {
                event.setImage_evenement(selectedImagePath);
            }

            boolean success;
            if (existingEvent == null) {
                success = eventService.addEvent(event);
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Événement soumis");
                    alert.setHeaderText(null);
                    alert.setContentText("Votre événement a été soumis pour validation par l'administrateur. Vous recevrez une notification lorsqu'il sera approuvé.");
                    alert.showAndWait();
                }
            } else {
                success = eventService.updateEvent(event);
            }

            if (success) {
                refreshData();
                dialog.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 700, 800);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return mainLayout;
    }
}