package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import edu.Loopi.entities.Participation;
import edu.Loopi.services.EventService;
import edu.Loopi.services.UserService;
import edu.Loopi.services.NotificationService;
import edu.Loopi.services.ParticipationService;
import edu.Loopi.services.AddressSuggestionService;
import edu.Loopi.services.AIImageGenerationService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class EventManagementView {
    private User currentUser;
    private UserService userService;
    private EventService eventService;
    private NotificationService notificationService;
    private ParticipationService participationService;
    private AddressSuggestionService addressService;
    private AdminDashboard adminDashboard;
    private String selectedImagePath = ""; // Pour stocker le chemin de l'image sélectionnée

    // Composants UI
    private TableView<Event> eventsTable;
    private ObservableList<Event> masterData;
    private FilteredList<Event> filteredData;

    // Statistiques
    private Label totalEventsLabel;
    private Label pendingEventsLabel;
    private Label approvedEventsLabel;
    private Label rejectedEventsLabel;
    private Label publishedEventsLabel;
    private Label totalParticipantsLabel;
    private Label pastEventsLabel;

    // Filtres
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> validationFilter;
    private ComboBox<String> publicationFilter;
    private ComboBox<String> organisateurFilter;

    // Timer pour le rafraîchissement automatique
    private PauseTransition autoRefreshTimer;

    // Constantes pour les couleurs
    private static final String AI_COLOR = "#9b59b6";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String SUCCESS_COLOR = "#2ecc71";
    private static final String WARNING_COLOR = "#f39c12";
    private static final String BORDER_COLOR = "#e9ecef";

    // Constantes pour les chemins d'images - MÊMES CHEMINS QUE DANS EVENTVIEW
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

    // Formatteurs de date
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public EventManagementView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
        this.eventService = new EventService();
        this.notificationService = new NotificationService();
        this.participationService = new ParticipationService();
        this.addressService = new AddressSuggestionService();
        createUploadDirectories();
    }

    /**
     * Crée les dossiers d'upload s'ils n'existent pas (MÊME QUE DANS EVENTVIEW)
     */
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

            // Vérifier les permissions
            System.out.println("   - Dossier events accessible en écriture: " + eventsDir.canWrite());
            System.out.println("   - Dossier AI accessible en écriture: " + aiDir.canWrite());
        } catch (Exception e) {
            System.err.println("❌ Erreur création dossiers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Copie une image dans le stockage (MÊME QUE DANS EVENTVIEW)
     */
    private String copyImageToStorage(File sourceFile) {
        try {
            File directory = new File(FULL_EVENTS_PATH);
            if (!directory.exists()) directory.mkdirs();

            if (!directory.canWrite()) {
                System.err.println("❌ Le dossier n'est pas accessible en écriture: " + FULL_EVENTS_PATH);
                return null;
            }

            String extension = "";
            String fileName = sourceFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            extension = (dotIndex > 0) ? fileName.substring(dotIndex) : ".jpg";

            if (!extension.matches("\\.(jpg|jpeg|png|gif|bmp)$")) {
                System.err.println("❌ Extension non supportée: " + extension);
                return null;
            }

            String uniqueFileName = "event_" + UUID.randomUUID().toString() + extension;
            String fullPath = FULL_EVENTS_PATH + uniqueFileName;

            Files.copy(sourceFile.toPath(), Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);

            File savedFile = new File(fullPath);
            if (savedFile.exists() && savedFile.length() > 0) {
                System.out.println("✅ Image copiée: " + fullPath + " (" + savedFile.length() + " bytes)");
                return DB_EVENTS_PATH + uniqueFileName;
            }
            return null;

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la copie: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Charge une image depuis le stockage (MÊME QUE DANS EVENTVIEW)
     */
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
                if (imgFile.exists() && imgFile.isFile()) {
                    Image image = new Image(imgFile.toURI().toString(), true);
                    if (!image.isError()) {
                        return image;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Exception lors du chargement: " + e.getMessage());
        }
        return null;
    }

    public void showEventManagementView(StackPane mainContentArea, boolean isDarkMode) {
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: " + adminDashboard.getBgColor() + ";");

        VBox topSection = createTopSection(isDarkMode);
        mainContainer.setTop(topSection);

        VBox centerSection = createCenterSection(isDarkMode);
        mainContainer.setCenter(centerSection);

        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(mainContainer);

        loadEvents();
        setupAutoRefresh();
    }

    private void setupAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }

        autoRefreshTimer = new PauseTransition(Duration.seconds(10));
        autoRefreshTimer.setOnFinished(e -> {
            refreshData();
            autoRefreshTimer.play();
        });
        autoRefreshTimer.play();
    }

    private VBox createTopSection(boolean isDarkMode) {
        VBox topSection = new VBox(20);
        topSection.setPadding(new Insets(20, 24, 10, 24));
        topSection.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";" +
                "-fx-border-color: " + adminDashboard.getBorderColor() + ";" +
                "-fx-border-width: 0 0 1 0;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("📅 Gestion des Événements");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Validez, modifiez et gérez tous les événements de la plateforme");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        header.getChildren().add(headerText);

        HBox statsBar = createStatsBar(isDarkMode);
        HBox filterBar = createFilterBar(isDarkMode);

        topSection.getChildren().addAll(headerText, statsBar, filterBar);
        return topSection;
    }

    private HBox createStatsBar(boolean isDarkMode) {
        HBox statsBar = new HBox(15);
        statsBar.setPadding(new Insets(15, 0, 5, 0));
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setStyle("-fx-background-color: transparent;");

        totalEventsLabel = new Label("0");
        pendingEventsLabel = new Label("0");
        approvedEventsLabel = new Label("0");
        rejectedEventsLabel = new Label("0");
        publishedEventsLabel = new Label("0");
        pastEventsLabel = new Label("0");
        totalParticipantsLabel = new Label("0");

        statsBar.getChildren().addAll(
                createStatCard("📊", "Total", totalEventsLabel, adminDashboard.getAccentColor()),
                createStatCard("⏳", "En attente", pendingEventsLabel, "#f39c12"),
                createStatCard("✅", "Approuvés", approvedEventsLabel, "#2ecc71"),
                createStatCard("❌", "Refusés", rejectedEventsLabel, "#e74c3c"),
                createStatCard("📢", "Publiés", publishedEventsLabel, "#9b59b6"),
                createStatCard("⌛", "Passés", pastEventsLabel, "#6c757d"),
                createStatCard("👥", "Participants", totalParticipantsLabel, "#1abc9c")
        );

        return statsBar;
    }

    private VBox createStatCard(String icon, String label, Label valueLabel, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 4;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(120);
        card.setMinWidth(120);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 20));

        Label titleLabel = new Label(label);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        header.getChildren().addAll(iconLabel, titleLabel);

        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    private HBox createFilterBar(boolean isDarkMode) {
        HBox filterBar = new HBox(15);
        filterBar.setPadding(new Insets(15, 0, 10, 0));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-background-color: transparent;");

        // Bouton d'actualisation avec style amélioré
        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-size: 18px; -fx-min-width: 40px; -fx-min-height: 40px; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        refreshBtn.setTooltip(new Tooltip("Actualiser les données"));
        refreshBtn.setOnAction(e -> refreshData());

        // Barre de recherche améliorée
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 25; -fx-padding: 5 15; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 25; -fx-border-width: 1;");
        searchBox.setPrefWidth(300);
        searchBox.setMinWidth(250);

        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("System", 16));
        searchIcon.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
        searchIcon.setMinWidth(20);
        searchIcon.setAlignment(Pos.CENTER);

        searchField = new TextField();
        searchField.setPromptText("Rechercher un événement...");
        searchField.setStyle("-fx-background-color: transparent; -fx-pref-width: 220px; -fx-font-size: 13px; " +
                "-fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() + ";");
        searchField.textProperty().addListener((obs, old, nv) -> applyFilters());

        // Bouton pour effacer la recherche
        Button clearSearchBtn = new Button("✕");
        clearSearchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColorMuted() +
                "; -fx-font-size: 14px; -fx-padding: 0 5; -fx-cursor: hand; -fx-min-width: 20px;");
        clearSearchBtn.setManaged(false);
        clearSearchBtn.setVisible(false);
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            clearSearchBtn.setManaged(false);
            clearSearchBtn.setVisible(false);
        });

        searchField.textProperty().addListener((obs, old, nv) -> {
            if (nv != null && !nv.isEmpty()) {
                clearSearchBtn.setManaged(true);
                clearSearchBtn.setVisible(true);
            } else {
                clearSearchBtn.setManaged(false);
                clearSearchBtn.setVisible(false);
            }
        });

        searchBox.getChildren().addAll(searchIcon, searchField, clearSearchBtn);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");
        sep1.setPrefHeight(30);

        VBox statusBox = new VBox(2);
        Label statusLabel = new Label("Statut (date)");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-font-weight: bold;");

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "À venir", "En cours", "Passés");
        statusFilter.setValue("Tous");
        statusFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 20; -fx-padding: 6 15; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 20; -fx-border-width: 1;");
        statusFilter.setPrefWidth(120);
        statusFilter.setMinWidth(100);
        statusFilter.setOnAction(e -> applyFilters());

        statusBox.getChildren().addAll(statusLabel, statusFilter);

        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");
        sep2.setPrefHeight(30);

        VBox validationBox = new VBox(2);
        Label validationLabel = new Label("Validation");
        validationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-font-weight: bold;");

        validationFilter = new ComboBox<>();
        validationFilter.getItems().addAll("Tous", "En attente", "Approuvés", "Refusés");
        validationFilter.setValue("Tous");
        validationFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 20; -fx-padding: 6 15; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 20; -fx-border-width: 1;");
        validationFilter.setPrefWidth(120);
        validationFilter.setMinWidth(100);
        validationFilter.setOnAction(e -> applyFilters());

        validationBox.getChildren().addAll(validationLabel, validationFilter);

        Separator sep3 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep3.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");
        sep3.setPrefHeight(30);

        VBox publicationBox = new VBox(2);
        Label publicationLabel = new Label("Publication");
        publicationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-font-weight: bold;");

        publicationFilter = new ComboBox<>();
        publicationFilter.getItems().addAll("Tous", "Brouillon", "Publiés");
        publicationFilter.setValue("Tous");
        publicationFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 20; -fx-padding: 6 15; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 20; -fx-border-width: 1;");
        publicationFilter.setPrefWidth(120);
        publicationFilter.setMinWidth(100);
        publicationFilter.setOnAction(e -> applyFilters());

        publicationBox.getChildren().addAll(publicationLabel, publicationFilter);

        Separator sep4 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep4.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");
        sep4.setPrefHeight(30);

        VBox orgBox = new VBox(2);
        Label orgLabel = new Label("Organisateur");
        orgLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-font-weight: bold;");

        organisateurFilter = new ComboBox<>();
        organisateurFilter.getItems().add("Tous");

        List<User> organisateurs = userService.getUsersByRole("organisateur");
        for (User org : organisateurs) {
            organisateurFilter.getItems().add(org.getPrenom() + " " + org.getNom());
        }

        organisateurFilter.setValue("Tous");
        organisateurFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 20; -fx-padding: 6 15; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 20; -fx-border-width: 1;");
        organisateurFilter.setPrefWidth(150);
        organisateurFilter.setMinWidth(120);
        organisateurFilter.setOnAction(e -> applyFilters());

        orgBox.getChildren().addAll(orgLabel, organisateurFilter);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("📥 Exporter");
        exportBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        exportBtn.setTooltip(new Tooltip("Exporter en CSV"));
        exportBtn.setOnAction(e -> exportEvents());

        Button resetBtn = new Button("✕ Réinitialiser");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getDangerColor() +
                "; -fx-border-color: " + adminDashboard.getDangerColor() + "; -fx-border-radius: 20; -fx-padding: 8 20; " +
                "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px; -fx-border-width: 1.5;");
        resetBtn.setTooltip(new Tooltip("Réinitialiser tous les filtres"));
        resetBtn.setOnAction(e -> resetFilters());

        filterBar.getChildren().addAll(refreshBtn, searchBox, sep1, statusBox, sep2, validationBox, sep3, publicationBox, sep4, orgBox, spacer, exportBtn, resetBtn);

        return filterBar;
    }

    private VBox createCenterSection(boolean isDarkMode) {
        VBox centerSection = new VBox(15);
        centerSection.setPadding(new Insets(0, 24, 24, 24));
        centerSection.setStyle("-fx-background-color: transparent;");

        VBox tableContainer = new VBox(15);
        tableContainer.setPadding(new Insets(20));
        tableContainer.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        Label tableTitle = new Label("📋 Liste des événements");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web(adminDashboard.getTextColor()));

        createEventsTable(isDarkMode);

        ScrollPane tableScrollPane = new ScrollPane(eventsTable);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setFitToHeight(true);
        tableScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tableScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tableScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        tableScrollPane.setPrefHeight(600);

        VBox.setVgrow(tableScrollPane, Priority.ALWAYS);
        tableContainer.getChildren().addAll(tableTitle, tableScrollPane);
        centerSection.getChildren().add(tableContainer);

        return centerSection;
    }

    @SuppressWarnings("unchecked")
    private void createEventsTable(boolean isDarkMode) {
        eventsTable = new TableView<>();
        eventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        eventsTable.setPrefHeight(600);
        eventsTable.setMinHeight(500);
        eventsTable.setStyle("-fx-background-color: transparent; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 8;");

        // Colonne Image
        TableColumn<Event, String> imageCol = new TableColumn<>("Image");
        imageCol.setPrefWidth(80);
        imageCol.setMinWidth(80);
        imageCol.setMaxWidth(80);
        imageCol.setCellFactory(col -> new TableCell<Event, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
                Rectangle clip = new Rectangle(60, 50);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                imageView.setClip(clip);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    try {
                        if (event.getImage_evenement() != null && !event.getImage_evenement().isEmpty()) {
                            Image img = loadImageFromStorage(event.getImage_evenement());
                            if (img != null) {
                                imageView.setImage(img);
                            } else {
                                imageView.setImage(new Image("https://via.placeholder.com/60x50/3182ce/ffffff?text=📅"));
                            }
                        } else {
                            imageView.setImage(new Image("https://via.placeholder.com/60x50/3182ce/ffffff?text=📅"));
                        }
                    } catch (Exception e) {
                        imageView.setImage(new Image("https://via.placeholder.com/60x50/3182ce/ffffff?text=📅"));
                    }
                    setGraphic(imageView);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne ID
        TableColumn<Event, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id_evenement"));
        idCol.setPrefWidth(50);
        idCol.setMinWidth(50);
        idCol.setMaxWidth(50);
        idCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // Colonne Titre
        TableColumn<Event, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        titreCol.setPrefWidth(180);
        titreCol.setMinWidth(150);
        titreCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        // Colonne Organisateur
        TableColumn<Event, String> organisateurCol = new TableColumn<>("Organisateur");
        organisateurCol.setCellValueFactory(cellData -> {
            User org = userService.getUserById(cellData.getValue().getId_organisateur());
            String nomComplet = org != null ? org.getPrenom() + " " + org.getNom() : "Inconnu";
            return new javafx.beans.property.SimpleStringProperty(nomComplet);
        });
        organisateurCol.setPrefWidth(140);
        organisateurCol.setMinWidth(120);
        organisateurCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        // Colonne Date
        TableColumn<Event, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFormattedDate()
                ));
        dateCol.setPrefWidth(130);
        dateCol.setMinWidth(120);
        dateCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Lieu
        TableColumn<Event, String> lieuCol = new TableColumn<>("Lieu");
        lieuCol.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        lieuCol.setPrefWidth(130);
        lieuCol.setMinWidth(120);
        lieuCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.length() > 20 ? item.substring(0, 17) + "..." : item);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        // Colonne Participants
        TableColumn<Event, Integer> participantsCol = new TableColumn<>("Participants");
        participantsCol.setCellValueFactory(new PropertyValueFactory<>("participantsCount"));
        participantsCol.setPrefWidth(85);
        participantsCol.setMinWidth(80);
        participantsCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Capacité
        TableColumn<Event, String> capaciteCol = new TableColumn<>("Capacité");
        capaciteCol.setCellValueFactory(cellData -> {
            Integer cap = cellData.getValue().getCapacite_max();
            return new javafx.beans.property.SimpleStringProperty(cap != null ? cap.toString() : "Illimité");
        });
        capaciteCol.setPrefWidth(70);
        capaciteCol.setMinWidth(70);
        capaciteCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Statut (date)
        TableColumn<Event, String> statutDateCol = new TableColumn<>("Statut");
        statutDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut()));
        statutDateCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    String color;
                    switch (item.toLowerCase()) {
                        case "à venir":
                            color = "#f39c12";
                            break;
                        case "en cours":
                            color = "#9b59b6";
                            break;
                        case "passé":
                            color = "#6c757d";
                            break;
                        default:
                            color = "#6c757d";
                    }
                    Label badge = new Label(item);
                    badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                    badge.setTextFill(Color.WHITE);
                    badge.setPadding(new Insets(4, 10, 4, 10));
                    badge.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15;");
                    badge.setTooltip(new Tooltip("Statut: " + item));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statutDateCol.setPrefWidth(75);
        statutDateCol.setMinWidth(70);

        // Colonne Validation
        TableColumn<Event, String> validationCol = new TableColumn<>("Validation");
        validationCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatutValidationFr()));
        validationCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    Label badge = new Label(item);
                    badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                    badge.setTextFill(Color.WHITE);
                    badge.setPadding(new Insets(4, 10, 4, 10));
                    badge.setStyle("-fx-background-color: " + event.getStatutValidationColor() + "; -fx-background-radius: 15;");
                    badge.setTooltip(new Tooltip("Validation: " + item));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        validationCol.setPrefWidth(85);
        validationCol.setMinWidth(80);

        // Colonne Publication
        TableColumn<Event, String> publicationCol = new TableColumn<>("Publication");
        publicationCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatutPublicationFr()));
        publicationCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    Label badge = new Label(item);
                    badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                    badge.setTextFill(Color.WHITE);
                    badge.setPadding(new Insets(4, 10, 4, 10));
                    badge.setStyle("-fx-background-color: " +
                            (event.isEstPublie() ? "#2ecc71" : "#95a5a6") + "; -fx-background-radius: 15;");
                    badge.setTooltip(new Tooltip("Publication: " + item));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        publicationCol.setPrefWidth(85);
        publicationCol.setMinWidth(80);

        // Colonne Actions
        TableColumn<Event, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(450);
        actionsCol.setMinWidth(400);
        actionsCol.setCellFactory(col -> new TableCell<Event, Void>() {
            private final Button viewBtn = createActionButton("👁️", "Voir détails", adminDashboard.getAccentColor());
            private final Button editBtn = createActionButton("✏️", "Modifier", adminDashboard.getWarningColor());
            private final Button approveBtn = createActionButton("✅", "Approuver", adminDashboard.getSuccessColor());
            private final Button rejectBtn = createActionButton("❌", "Refuser", adminDashboard.getDangerColor());
            private final Button participantsBtn = createActionButton("👥", "Participants", "#9b59b6");
            private final Button publishBtn = createActionButton("📢", "Publier", "#2ecc71");
            private final Button unpublishBtn = createActionButton("🔒", "Dépublier", "#95a5a6");
            private final Button deleteBtn = createActionButton("🗑️", "Supprimer", "#6c757d");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, approveBtn, rejectBtn, participantsBtn, publishBtn, unpublishBtn, deleteBtn);

            {
                buttons.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showEventDetails(event);
                });

                editBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showEditEventDialog(event);
                });

                approveBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showApproveDialog(event);
                });

                rejectBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showRejectDialog(event);
                });

                participantsBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showEventParticipants(event);
                });

                publishBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    confirmPublish(event);
                });

                unpublishBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    confirmUnpublish(event);
                });

                deleteBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    confirmDelete(event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());

                    // Activer/désactiver les boutons selon le statut
                    if ("approuve".equals(event.getStatutValidation())) {
                        approveBtn.setDisable(true);
                        approveBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");

                        if (event.isEstPublie()) {
                            publishBtn.setDisable(true);
                            publishBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                            unpublishBtn.setDisable(false);
                            unpublishBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                        } else {
                            publishBtn.setDisable(false);
                            publishBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                            unpublishBtn.setDisable(true);
                            unpublishBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                        }
                    } else {
                        approveBtn.setDisable(false);
                        approveBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                        publishBtn.setDisable(true);
                        publishBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                        unpublishBtn.setDisable(true);
                        unpublishBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                    }

                    if ("refuse".equals(event.getStatutValidation())) {
                        rejectBtn.setDisable(true);
                        rejectBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                    } else {
                        rejectBtn.setDisable(false);
                        rejectBtn.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                    }

                    setGraphic(buttons);
                }
            }
        });

        eventsTable.getColumns().addAll(imageCol, idCol, titreCol, organisateurCol, dateCol, lieuCol,
                participantsCol, capaciteCol, statutDateCol, validationCol, publicationCol, actionsCol);

        eventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private Button createActionButton(String icon, String tooltip, String color) {
        Button btn = new Button(icon);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
        btn.setTooltip(new Tooltip(tooltip));
        btn.setMinWidth(40);
        btn.setPrefWidth(40);

        // Effet hover
        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
        });

        return btn;
    }

    private String darkenColor(String color) {
        switch (color) {
            case "#3182CE": return "#2c5282";
            case "#f59e0b": return "#b45309";
            case "#2ecc71": return "#27ae60";
            case "#ef4444": return "#dc2626";
            case "#9b59b6": return "#7e3a8b";
            case "#95a5a6": return "#7f8c8d";
            case "#6c757d": return "#4b5563";
            default: return color;
        }
    }

    private void loadEvents() {
        List<Event> events = eventService.refreshEvents();
        masterData = FXCollections.observableArrayList(events);
        filteredData = new FilteredList<>(masterData, p -> true);
        eventsTable.setItems(filteredData);

        updateStats();
    }

    private void refreshData() {
        // Forcer le rafraîchissement depuis la base de données
        eventService.refreshEvents();

        // Recharger les événements
        loadEvents();

        // Réappliquer les filtres
        applyFilters();

        System.out.println("✅ Données actualisées");
    }

    private void updateStats() {
        List<Event> allEvents = masterData;

        int total = allEvents.size();
        int enAttente = (int) allEvents.stream().filter(e -> "en_attente".equals(e.getStatutValidation())).count();
        int approuves = (int) allEvents.stream().filter(e -> "approuve".equals(e.getStatutValidation())).count();
        int refuses = (int) allEvents.stream().filter(e -> "refuse".equals(e.getStatutValidation())).count();
        int publies = (int) allEvents.stream().filter(e -> e.isEstPublie()).count();
        int passes = (int) allEvents.stream().filter(e -> e.isDatePassee()).count();
        int totalParticipants = allEvents.stream().mapToInt(Event::getParticipantsCount).sum();

        totalEventsLabel.setText(String.valueOf(total));
        pendingEventsLabel.setText(String.valueOf(enAttente));
        approvedEventsLabel.setText(String.valueOf(approuves));
        rejectedEventsLabel.setText(String.valueOf(refuses));
        publishedEventsLabel.setText(String.valueOf(publies));
        pastEventsLabel.setText(String.valueOf(passes));
        totalParticipantsLabel.setText(String.valueOf(totalParticipants));
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = statusFilter.getValue();
        String selectedValidation = validationFilter.getValue();
        String selectedPublication = publicationFilter.getValue();
        String selectedOrganisateur = organisateurFilter.getValue();

        filteredData.setPredicate(event -> {
            if (!searchText.isEmpty()) {
                boolean matches = event.getTitre().toLowerCase().contains(searchText) ||
                        (event.getLieu() != null && event.getLieu().toLowerCase().contains(searchText)) ||
                        (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchText));
                if (!matches) return false;
            }

            if (selectedStatus != null && !"Tous".equals(selectedStatus)) {
                String eventStatut = event.getStatut();
                if (!eventStatut.equalsIgnoreCase(selectedStatus)) {
                    return false;
                }
            }

            if (selectedValidation != null && !"Tous".equals(selectedValidation)) {
                String validationValue = "";
                switch (selectedValidation) {
                    case "En attente": validationValue = "en_attente"; break;
                    case "Approuvés": validationValue = "approuve"; break;
                    case "Refusés": validationValue = "refuse"; break;
                }
                if (!validationValue.equals(event.getStatutValidation())) {
                    return false;
                }
            }

            if (selectedPublication != null && !"Tous".equals(selectedPublication)) {
                boolean estPublie = event.isEstPublie();
                if (selectedPublication.equals("Brouillon") && estPublie) return false;
                if (selectedPublication.equals("Publiés") && !estPublie) return false;
            }

            if (selectedOrganisateur != null && !"Tous".equals(selectedOrganisateur)) {
                User org = userService.getUserById(event.getId_organisateur());
                String orgName = org != null ? org.getPrenom() + " " + org.getNom() : "";
                if (!orgName.equals(selectedOrganisateur)) {
                    return false;
                }
            }

            return true;
        });

        eventsTable.setItems(filteredData);
    }

    private void resetFilters() {
        searchField.clear();
        statusFilter.setValue("Tous");
        validationFilter.setValue("Tous");
        publicationFilter.setValue("Tous");
        organisateurFilter.setValue("Tous");
        applyFilters();
    }

    private void showEventParticipants(Event event) {
        Stage stage = new Stage();
        stage.setTitle("Participants - " + event.getTitre());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(adminDashboard.getPrimaryStage());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(1000);
        content.setPrefHeight(700);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("👥");
        iconLabel.setFont(Font.font("System", 48));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label(event.getTitre() + " - Participants");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label countLabel = new Label(event.getParticipantsCount() + " participant(s)");
        countLabel.setFont(Font.font("System", 16));
        countLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(titleLabel, countLabel);
        header.getChildren().addAll(iconLabel, headerText);

        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#F3F4F6") +
                "; -fx-background-radius: 12;");
        statsBox.setAlignment(Pos.CENTER);

        VBox inscritsBox = createParticipantStat("📝 Inscrits", String.valueOf(event.getParticipantsInscrits()), "#3b82f6");
        VBox presentsBox = createParticipantStat("✅ Présents", String.valueOf(event.getParticipantsPresents()), "#10b981");
        VBox absentsBox = createParticipantStat("❌ Absents", String.valueOf(event.getParticipantsAbsents()), "#ef4444");

        statsBox.getChildren().addAll(inscritsBox, presentsBox, absentsBox);

        TableView<Participation> participantsTable = new TableView<>();
        participantsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        participantsTable.setPrefHeight(400);
        participantsTable.setStyle("-fx-background-color: transparent; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 8;");

        TableColumn<Participation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Participation, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cellData -> {
            User user = userService.getUserById(cellData.getValue().getIdUser());
            String nomComplet = user != null ? user.getPrenom() + " " + user.getNom() : "Inconnu";
            return new javafx.beans.property.SimpleStringProperty(nomComplet);
        });
        nomCol.setPrefWidth(180);

        TableColumn<Participation, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> {
            User user = userService.getUserById(cellData.getValue().getIdUser());
            return new javafx.beans.property.SimpleStringProperty(user != null ? user.getEmail() : "Inconnu");
        });
        emailCol.setPrefWidth(200);

        TableColumn<Participation, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setPrefWidth(150);

        TableColumn<Participation, String> ageCol = new TableColumn<>("Âge");
        ageCol.setCellValueFactory(cellData -> {
            Integer age = cellData.getValue().getAge();
            return new javafx.beans.property.SimpleStringProperty(age != null ? age.toString() : "-");
        });
        ageCol.setPrefWidth(60);
        ageCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Participation, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutCol.setPrefWidth(100);
        statutCol.setCellFactory(col -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(statut.toUpperCase());
                    badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                    badge.setTextFill(Color.WHITE);
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    String color = statut.equals("inscrit") ? "#3b82f6" :
                            statut.equals("present") ? "#10b981" : "#ef4444";
                    badge.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Participation, String> dateCol = new TableColumn<>("Date d'inscription");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Participation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        actionsCol.setCellFactory(col -> new TableCell<Participation, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: " + adminDashboard.getWarningColor() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                editBtn.setTooltip(new Tooltip("Modifier statut"));

                deleteBtn.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                deleteBtn.setTooltip(new Tooltip("Supprimer"));

                editBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    showEditParticipationDialog(p, event);
                });

                deleteBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    confirmDeleteParticipation(p, event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        participantsTable.getColumns().addAll(idCol, nomCol, emailCol, contactCol, ageCol, statutCol, dateCol, actionsCol);

        List<Participation> participants = participationService.getParticipationsByEvent(event.getId_evenement());
        participantsTable.setItems(FXCollections.observableArrayList(participants));

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> stage.close());

        content.getChildren().addAll(header, statsBox, participantsTable, closeBtn);
        VBox.setVgrow(participantsTable, Priority.ALWAYS);

        Scene scene = new Scene(content);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createParticipantStat(String label, String value, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 30, 15, 30));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: " + color + "; -fx-border-width: 0 0 4 0;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));

        Label labelLabel = new Label(label);
        labelLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        labelLabel.setTextFill(Color.web("#6c757d"));

        box.getChildren().addAll(valueLabel, labelLabel);
        return box;
    }

    private void showEditParticipationDialog(Participation participation, Event event) {
        Stage dialog = new Stage();
        dialog.setTitle("Modifier la participation");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(adminDashboard.getPrimaryStage());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Label title = new Label("Modifier le statut du participant");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        User participant = userService.getUserById(participation.getIdUser());
        Label participantName = new Label("Participant: " + (participant != null ? participant.getNomComplet() : "Inconnu"));
        participantName.setFont(Font.font("System", 14));
        participantName.setTextFill(Color.web(adminDashboard.getTextColor()));

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("inscrit", "present", "absent");
        statutCombo.setValue(participation.getStatut());
        statutCombo.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        statutCombo.setPrefWidth(Double.MAX_VALUE);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-padding: 10 25; -fx-cursor: hand; -fx-font-size: 14px;");

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String newStatut = statutCombo.getValue();
            if (newStatut != null && !newStatut.equals(participation.getStatut())) {
                boolean updated = participationService.updateStatut(
                        participation.getIdEvenement(),
                        participation.getIdUser(),
                        newStatut
                );
                if (updated) {
                    adminDashboard.showAlert("Succès", "Statut mis à jour avec succès");
                    dialog.close();
                    showEventParticipants(event);
                } else {
                    adminDashboard.showError("Erreur", "Impossible de mettre à jour le statut");
                }
            } else {
                dialog.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        content.getChildren().addAll(title, participantName, statutCombo, buttonBox);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void confirmDeleteParticipation(Participation participation, Event event) {
        User participant = userService.getUserById(participation.getIdUser());
        String participantName = participant != null ? participant.getNomComplet() : "l'utilisateur";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la participation ?");
        alert.setContentText("Voulez-vous vraiment supprimer la participation de " + participantName + " ?\nCette action est irréversible.");
        alert.initOwner(adminDashboard.getPrimaryStage());

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Supprimer");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = participationService.annulerParticipation(
                    participation.getIdEvenement(),
                    participation.getIdUser()
            );
            if (deleted) {
                adminDashboard.showAlert("Succès", "Participation supprimée avec succès");
                showEventParticipants(event);
            } else {
                adminDashboard.showError("Erreur", "Impossible de supprimer la participation");
            }
        }
    }

    private void confirmPublish(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de publication");
        alert.setHeaderText("Publier l'événement");
        alert.setContentText("Voulez-vous publier \"" + event.getTitre() + "\" ?\nL'événement sera visible par tous les participants.");
        alert.initOwner(adminDashboard.getPrimaryStage());

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Publier");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventService.publierEvenement(event.getId_evenement())) {
                adminDashboard.showAlert("Succès", "✅ Événement publié avec succès !");
                refreshData();
            } else {
                adminDashboard.showError("Erreur", "❌ Impossible de publier l'événement");
            }
        }
    }

    private void confirmUnpublish(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de dépublication");
        alert.setHeaderText("Dépublier l'événement");
        alert.setContentText("Voulez-vous dépublier \"" + event.getTitre() + "\" ?\nL'événement ne sera plus visible par les participants.");
        alert.initOwner(adminDashboard.getPrimaryStage());

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Dépublier");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getWarningColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventService.depublierEvenement(event.getId_evenement())) {
                adminDashboard.showAlert("Succès", "✅ Événement dépublié avec succès !");
                refreshData();
            } else {
                adminDashboard.showError("Erreur", "❌ Impossible de dépublier l'événement");
            }
        }
    }

    private void showApproveDialog(Event event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Approuver l'événement");
        dialog.setHeaderText("Approuver \"" + event.getTitre() + "\"");
        dialog.initOwner(adminDashboard.getPrimaryStage());

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Label infoLabel = new Label("Vous êtes sur le point d'approuver cet événement. " +
                "Il pourra ensuite être publié par l'organisateur pour être visible par les participants.");
        infoLabel.setWrapText(true);
        infoLabel.setFont(Font.font("System", 13));
        infoLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextArea commentaireArea = new TextArea();
        commentaireArea.setPromptText("Commentaire pour l'organisateur (optionnel)");
        commentaireArea.setPrefRowCount(4);
        commentaireArea.setWrapText(true);
        commentaireArea.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6;");

        content.getChildren().addAll(infoLabel, new Label("Commentaire:"), commentaireArea);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Approuver");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return commentaireArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(commentaire -> {
            if (eventService.approuverEvenement(event.getId_evenement(), commentaire, currentUser.getId())) {
                adminDashboard.showAlert("Succès", "✅ Événement approuvé avec succès !");
                refreshData();
            } else {
                adminDashboard.showError("Erreur", "❌ Impossible d'approuver l'événement");
            }
        });
    }

    private void showRejectDialog(Event event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Refuser l'événement");
        dialog.setHeaderText("Refuser \"" + event.getTitre() + "\"");
        dialog.initOwner(adminDashboard.getPrimaryStage());

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Label infoLabel = new Label("Veuillez indiquer le motif du refus. " +
                "Ce message sera envoyé à l'organisateur.");
        infoLabel.setWrapText(true);
        infoLabel.setFont(Font.font("System", 13));
        infoLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextArea motifArea = new TextArea();
        motifArea.setPromptText("Motif du refus (obligatoire)");
        motifArea.setPrefRowCount(5);
        motifArea.setWrapText(true);
        motifArea.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6;");

        content.getChildren().addAll(infoLabel, new Label("Motif du refus *"), motifArea);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Refuser");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        final Button ok = okButton;
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, actionEvent -> {
            if (motifArea.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Validation");
                alert.setHeaderText(null);
                alert.setContentText("Le motif du refus est obligatoire !");
                alert.initOwner(adminDashboard.getPrimaryStage());
                alert.showAndWait();
                actionEvent.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return motifArea.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motif -> {
            if (eventService.refuserEvenement(event.getId_evenement(), motif, currentUser.getId())) {
                adminDashboard.showAlert("Succès", "✅ Événement refusé avec succès !");
                refreshData();
            } else {
                adminDashboard.showError("Erreur", "❌ Impossible de refuser l'événement");
            }
        });
    }

    /**
     * Crée un TextField avec autocomplétion d'adresses
     */
    private TextField createAddressTextField(String initialValue, Stage dialog) {
        TextField textField = new TextField(initialValue);
        textField.setPromptText("Saisissez une adresse...");
        textField.setStyle("-fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        // Créer un ContextMenu pour les suggestions
        ContextMenu suggestionsMenu = new ContextMenu();

        // Timer pour le debounce (éviter trop de requêtes)
        PauseTransition debounceTimer = new PauseTransition(Duration.millis(500));

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().length() < 3) {
                suggestionsMenu.hide();
                return;
            }

            // Réinitialiser le timer
            debounceTimer.setOnFinished(event -> {
                String query = newVal.trim();

                // Exécuter la recherche dans un thread séparé
                new Thread(() -> {
                    List<String> suggestions = addressService.getAddressSuggestions(query);

                    // Mettre à jour l'interface JavaFX
                    Platform.runLater(() -> {
                        suggestionsMenu.getItems().clear();

                        if (suggestions.isEmpty()) {
                            MenuItem noResultItem = new MenuItem("Aucune suggestion");
                            noResultItem.setDisable(true);
                            suggestionsMenu.getItems().add(noResultItem);
                        } else {
                            for (String suggestion : suggestions) {
                                MenuItem item = new MenuItem(suggestion);
                                item.setOnAction(e -> {
                                    textField.setText(suggestion);
                                    suggestionsMenu.hide();
                                });
                                suggestionsMenu.getItems().add(item);
                            }
                        }

                        // Afficher le menu sous le TextField
                        if (!suggestionsMenu.getItems().isEmpty()) {
                            suggestionsMenu.show(textField, javafx.geometry.Side.BOTTOM, 0, 0);
                        } else {
                            suggestionsMenu.hide();
                        }
                    });
                }).start();
            });

            debounceTimer.playFromStart();
        });

        // Cacher le menu quand le champ perd le focus
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                suggestionsMenu.hide();
            }
        });

        return textField;
    }

    /**
     * Affiche la boîte de dialogue de modification d'événement
     * AVEC GÉNÉRATION IA ET AUTOCOMPLÉTION DU LIEU
     */
    private void showEditEventDialog(Event event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("✏️ Modifier l'événement");
        dialog.initOwner(adminDashboard.getPrimaryStage());
        dialog.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        // Header avec dégradé
        HBox header = new HBox(15);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-background-radius: 12 12 0 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label headerIcon = new Label("✏️");
        headerIcon.setFont(Font.font("System", 32));
        headerIcon.setTextFill(Color.WHITE);

        VBox headerText = new VBox(3);
        Label headerTitle = new Label("Modifier l'événement");
        headerTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        headerTitle.setTextFill(Color.WHITE);

        Label headerSubtitle = new Label("ID: " + event.getId_evenement() + " • " + event.getStatutValidationFr());
        headerSubtitle.setFont(Font.font("System", 12));
        headerSubtitle.setTextFill(Color.web("#e0e0e0"));

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        header.getChildren().addAll(headerIcon, headerText);

        // Formulaire
        VBox form = new VBox(20);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        // ========== TITRE ==========
        VBox titleBox = new VBox(3);
        Label titleLabel = new Label("📝 Titre *");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextField titreField = new TextField(event.getTitre());
        titreField.setPromptText("Ex: Nettoyage de la plage");
        titreField.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-padding: 10 12; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        titreField.setPrefWidth(500);

        Label titreError = new Label();
        titreError.setFont(Font.font("System", 11));
        titreError.setTextFill(Color.web(adminDashboard.getDangerColor()));
        titreError.setVisible(false);
        titreError.setWrapText(true);

        titleBox.getChildren().addAll(titleLabel, titreField, titreError);

        // ========== DESCRIPTION ==========
        VBox descBox = new VBox(3);
        Label descLabel = new Label("📄 Description *");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        descLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextArea descArea = new TextArea(event.getDescription());
        descArea.setPromptText("Décrivez votre événement...");
        descArea.setPrefRowCount(4);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-padding: 10 12; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");

        Label descError = new Label();
        descError.setFont(Font.font("System", 11));
        descError.setTextFill(Color.web(adminDashboard.getDangerColor()));
        descError.setVisible(false);
        descError.setWrapText(true);

        descBox.getChildren().addAll(descLabel, descArea, descError);

        // ========== DATE ET HEURE ==========
        VBox dateBox = new VBox(3);
        Label dateLabel = new Label("📅 Date & Heure *");
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        dateLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        HBox dateTimeBox = new HBox(10);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker(event.getDate_evenement().toLocalDate());
        datePicker.setPrefWidth(150);
        datePicker.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-padding: 8 12; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, event.getDate_evenement().getHour());
        hourSpinner.setPrefWidth(80);
        hourSpinner.setEditable(true);
        hourSpinner.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-text-fill: " + adminDashboard.getTextColor() + ";");

        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, event.getDate_evenement().getMinute());
        minuteSpinner.setPrefWidth(80);
        minuteSpinner.setEditable(true);
        minuteSpinner.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-text-fill: " + adminDashboard.getTextColor() + ";");

        Label separatorLabel = new Label("h");
        separatorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        separatorLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        dateTimeBox.getChildren().addAll(datePicker, hourSpinner, separatorLabel, minuteSpinner);

        Label dateError = new Label();
        dateError.setFont(Font.font("System", 11));
        dateError.setTextFill(Color.web(adminDashboard.getDangerColor()));
        dateError.setVisible(false);
        dateError.setWrapText(true);

        dateBox.getChildren().addAll(dateLabel, dateTimeBox, dateError);

        // ========== LIEU AVEC AUTOCOMPLÉTION ==========
        VBox lieuBox = new VBox(3);
        Label lieuLabel = new Label("📍 Lieu *");
        lieuLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        lieuLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextField lieuField = createAddressTextField(event.getLieu(), dialog);

        Label lieuError = new Label();
        lieuError.setFont(Font.font("System", 11));
        lieuError.setTextFill(Color.web(adminDashboard.getDangerColor()));
        lieuError.setVisible(false);
        lieuError.setWrapText(true);

        lieuBox.getChildren().addAll(lieuLabel, lieuField, lieuError);

        // ========== CAPACITÉ ==========
        VBox capaciteBox = new VBox(3);
        Label capaciteLabel = new Label("👥 Capacité max");
        capaciteLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        capaciteLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        HBox capaciteControl = new HBox(10);
        capaciteControl.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> capaciteSpinner = new Spinner<>(1, 1000,
                event.getCapacite_max() != null ? event.getCapacite_max() : 50);
        capaciteSpinner.setEditable(true);
        capaciteSpinner.setPrefWidth(100);
        capaciteSpinner.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-text-fill: " + adminDashboard.getTextColor() + ";");

        CheckBox capaciteIllimitee = new CheckBox("Illimitée");
        capaciteIllimitee.setTextFill(Color.web(adminDashboard.getTextColor()));
        if (event.getCapacite_max() == null) {
            capaciteIllimitee.setSelected(true);
            capaciteSpinner.setDisable(true);
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

        // ========== SECTION IMAGE ==========
        VBox imageSection = new VBox(10);
        imageSection.setPadding(new Insets(15));
        imageSection.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#F3F4F6") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 12;");

        Label imageTitle = new Label("🖼️ Image");
        imageTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        imageTitle.setTextFill(Color.web(adminDashboard.getTextColor()));

        // Conteneur pour l'aperçu de l'image
        ImageView previewImageView = new ImageView();
        previewImageView.setFitWidth(200);
        previewImageView.setFitHeight(120);
        previewImageView.setPreserveRatio(true);

        Rectangle previewClip = new Rectangle(200, 120);
        previewClip.setArcWidth(12);
        previewClip.setArcHeight(12);
        previewImageView.setClip(previewClip);

        // Charger l'image existante si disponible
        String currentImagePath = event.getImage_evenement();
        selectedImagePath = currentImagePath != null ? currentImagePath : "";

        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            System.out.println("📸 Chargement de l'image existante: " + currentImagePath);
            Image existingImage = loadImageFromStorage(currentImagePath);
            if (existingImage != null) {
                previewImageView.setImage(existingImage);
                System.out.println("✅ Image chargée avec succès");
            } else {
                System.out.println("❌ Échec du chargement de l'image existante, utilisation du placeholder");
                previewImageView.setImage(new Image("https://via.placeholder.com/200x120/3182ce/ffffff?text=Image"));
            }
        } else {
            System.out.println("🖼️ Aucune image existante, utilisation du placeholder");
            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/3182ce/ffffff?text=Image"));
        }

        VBox previewBox = new VBox(5, previewImageView);
        previewBox.setAlignment(Pos.CENTER);

        // Boutons pour les actions sur l'image
        FlowPane imageButtons = new FlowPane();
        imageButtons.setAlignment(Pos.CENTER);
        imageButtons.setHgap(10);
        imageButtons.setVgap(10);
        imageButtons.setPadding(new Insets(5, 0, 5, 0));

        Button browseBtn = new Button("📁 Choisir");
        browseBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px; -fx-cursor: hand;");

        Button clearImageBtn = new Button("🗑️ Effacer");
        clearImageBtn.setStyle("-fx-background-color: " + DANGER_COLOR +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px; -fx-cursor: hand;");
        clearImageBtn.setVisible(currentImagePath != null && !currentImagePath.isEmpty());

        Button aiGenerateBtn = new Button("✨ Générer avec IA");
        aiGenerateBtn.setStyle("-fx-background-color: " + AI_COLOR +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                "-fx-font-size: 12px; -fx-cursor: hand;");
        aiGenerateBtn.setTooltip(new Tooltip("Générer une image avec Stability AI"));

        ProgressIndicator aiProgressIndicator = new ProgressIndicator();
        aiProgressIndicator.setVisible(false);
        aiProgressIndicator.setPrefSize(25, 25);

        imageButtons.getChildren().addAll(browseBtn, clearImageBtn, aiGenerateBtn, aiProgressIndicator);
        imageSection.getChildren().addAll(imageTitle, previewBox, imageButtons);

        // Action pour choisir une image
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
                        adminDashboard.showAlert("Succès", "Image chargée avec succès !");
                    } else {
                        adminDashboard.showError("Erreur", "❌ Échec du chargement de l'image");
                    }
                } else {
                    adminDashboard.showError("Erreur", "❌ Échec de la copie de l'image");
                }
            }
        });

        // Action pour effacer l'image
        clearImageBtn.setOnAction(e -> {
            selectedImagePath = "";
            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/3182ce/ffffff?text=Image"));
            clearImageBtn.setVisible(false);
        });

        // Action pour générer une image avec l'IA
        AIImageGenerationService aiImageService = new AIImageGenerationService();

        if (aiImageService.isConfigured()) {
            aiGenerateBtn.setText("✨ Générer avec " + aiImageService.getModelName());
        }

        aiGenerateBtn.setOnAction(e -> {
            String titre = titreField.getText().trim();
            String description = descArea.getText().trim();

            if (titre.isEmpty()) {
                adminDashboard.showError("Information manquante", "Veuillez d'abord saisir un titre");
                return;
            }

            if (description.isEmpty()) {
                adminDashboard.showError("Information manquante", "Veuillez d'abord saisir une description");
                return;
            }

            // Désactiver les boutons pendant la génération
            aiGenerateBtn.setDisable(true);
            browseBtn.setDisable(true);
            aiProgressIndicator.setVisible(true);
            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/9b59b6/ffffff?text=Génération..."));

            new Thread(() -> {
                try {
                    String generatedPath = aiImageService.generateEventImage(titre, description);

                    Platform.runLater(() -> {
                        aiProgressIndicator.setVisible(false);
                        aiGenerateBtn.setDisable(false);
                        browseBtn.setDisable(false);

                        if (generatedPath != null) {
                            selectedImagePath = generatedPath;
                            Image newImage = loadImageFromStorage(generatedPath);
                            if (newImage != null) {
                                previewImageView.setImage(newImage);
                                clearImageBtn.setVisible(true);
                                adminDashboard.showAlert("Succès", "✅ Image générée avec succès !");
                            } else {
                                previewImageView.setImage(new Image("https://via.placeholder.com/200x120/3182ce/ffffff?text=Image"));
                                adminDashboard.showError("Erreur", "❌ Échec du chargement de l'image");
                            }
                        } else {
                            previewImageView.setImage(new Image("https://via.placeholder.com/200x120/3182ce/ffffff?text=Image"));
                            adminDashboard.showError("Erreur", "❌ Échec de la génération");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        aiProgressIndicator.setVisible(false);
                        aiGenerateBtn.setDisable(false);
                        browseBtn.setDisable(false);
                        previewImageView.setImage(new Image("https://via.placeholder.com/200x120/3182ce/ffffff?text=Image"));
                        adminDashboard.showError("Erreur", "❌ Erreur: " + ex.getMessage());
                    });
                }
            }).start();
        });

        // Assemblage du formulaire
        form.getChildren().addAll(titleBox, descBox, dateBox, lieuBox, capaciteBox, imageSection);

        // ScrollPane pour le formulaire
        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(450);

        // ========== BOUTONS ==========
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(15, 25, 25, 25));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-padding: 10 25; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");
        cancelBtn.setOnAction(e -> {
            selectedImagePath = "";
            dialog.close();
        });

        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        // ========== ACTION DU BOUTON ENREGISTRER ==========
        saveBtn.setOnAction(e -> {
            // Réinitialiser les messages d'erreur
            titreError.setVisible(false);
            descError.setVisible(false);
            dateError.setVisible(false);
            lieuError.setVisible(false);

            boolean isValid = true;

            // Validation du titre
            String titre = titreField.getText().trim();
            if (titre.isEmpty()) {
                titreError.setText("🔴 Le titre est obligatoire");
                titreError.setVisible(true);
                isValid = false;
            } else if (titre.length() < 3) {
                titreError.setText("🔴 Minimum 3 caractères");
                titreError.setVisible(true);
                isValid = false;
            }

            // Validation de la description
            String description = descArea.getText().trim();
            if (description.isEmpty()) {
                descError.setText("🔴 La description est obligatoire");
                descError.setVisible(true);
                isValid = false;
            } else if (description.length() < 10) {
                descError.setText("🔴 Minimum 10 caractères");
                descError.setVisible(true);
                isValid = false;
            }

            // Validation de la date
            LocalDate date = datePicker.getValue();
            if (date == null) {
                dateError.setText("🔴 La date est obligatoire");
                dateError.setVisible(true);
                isValid = false;
            } else {
                LocalDateTime eventDateTime = LocalDateTime.of(date,
                        LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));
                if (eventDateTime.isBefore(LocalDateTime.now())) {
                    dateError.setText("🔴 Date future requise");
                    dateError.setVisible(true);
                    isValid = false;
                }
            }

            // Validation du lieu
            String lieu = lieuField.getText().trim();
            if (lieu.isEmpty()) {
                lieuError.setText("🔴 Le lieu est obligatoire");
                lieuError.setVisible(true);
                isValid = false;
            } else if (lieu.length() < 3) {
                lieuError.setText("🔴 Minimum 3 caractères");
                lieuError.setVisible(true);
                isValid = false;
            }

            if (!isValid) return;

            // Mise à jour de l'événement
            event.setTitre(titre);
            event.setDescription(description);
            event.setDate_evenement(LocalDateTime.of(datePicker.getValue(),
                    LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())));
            event.setLieu(lieu);

            if (!capaciteIllimitee.isSelected()) {
                event.setCapacite_max(capaciteSpinner.getValue());
            } else {
                event.setCapacite_max(null);
            }

            // Mettre à jour l'image si elle a changé
            if (!selectedImagePath.isEmpty() && !selectedImagePath.equals(event.getImage_evenement())) {
                System.out.println("📸 Sauvegarde du nouveau chemin d'image: " + selectedImagePath);
                event.setImage_evenement(selectedImagePath);
            }
            // Si selectedImagePath est vide, on garde l'image existante

            // DEBUG - Afficher le contenu de l'événement avant enregistrement
            System.out.println("\n🔍 DEBUG - Contenu de l'événement avant sauvegarde:");
            System.out.println("   ID: " + event.getId_evenement());
            System.out.println("   Titre: '" + event.getTitre() + "'");
            System.out.println("   Description: '" + event.getDescription() + "'");
            System.out.println("   Date: " + event.getDate_evenement());
            System.out.println("   Lieu: '" + event.getLieu() + "'");
            System.out.println("   Capacité: " + event.getCapacite_max());
            System.out.println("   Image: '" + event.getImage_evenement() + "'");

            System.out.println("🔄 Tentative de mise à jour de l'événement ID: " + event.getId_evenement());
            boolean updated = eventService.updateEvent(event);

            if (updated) {
                adminDashboard.showAlert("Succès", "✅ Événement modifié avec succès !");

                // Forcer le rafraîchissement des données
                refreshData();

                dialog.close();
            } else {
                adminDashboard.showError("Erreur", "❌ Impossible de modifier l'événement");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        // Assemblage final
        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 650, 750);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showEventDetails(Event event) {
        Stage dialog = new Stage();
        dialog.setTitle("Détails de l'événement");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(adminDashboard.getPrimaryStage());
        dialog.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        // Header avec dégradé
        HBox header = new HBox(15);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-background-radius: 12 12 0 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label headerIcon = new Label("📅");
        headerIcon.setFont(Font.font("System", 32));
        headerIcon.setTextFill(Color.WHITE);

        VBox headerText = new VBox(3);
        Label headerTitle = new Label(event.getTitre());
        headerTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        headerTitle.setTextFill(Color.WHITE);

        Label headerSubtitle = new Label("ID: " + event.getId_evenement() + " • " + event.getStatutValidationFr());
        headerSubtitle.setFont(Font.font("System", 12));
        headerSubtitle.setTextFill(Color.web("#e0e0e0"));

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        header.getChildren().addAll(headerIcon, headerText);

        // Contenu principal
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#F3F4F6") +
                "; -fx-background-radius: 10;");
        imageContainer.setPrefHeight(180);
        imageContainer.setMinHeight(180);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(550);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);

        try {
            if (event.getImage_evenement() != null && !event.getImage_evenement().isEmpty()) {
                Image img = loadImageFromStorage(event.getImage_evenement());
                if (img != null) {
                    imageView.setImage(img);
                } else {
                    imageView.setImage(new Image("https://via.placeholder.com/550x160/3182ce/ffffff?text=" + event.getTitre()));
                }
            } else {
                imageView.setImage(new Image("https://via.placeholder.com/550x160/3182ce/ffffff?text=" + event.getTitre()));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/550x160/3182ce/ffffff?text=" + event.getTitre()));
        }

        Rectangle clip = new Rectangle(550, 160);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        imageView.setClip(clip);
        imageContainer.getChildren().add(imageView);

        // Description
        VBox descBox = new VBox(8);
        Label descLabel = new Label("📝 Description");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        descLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextArea descArea = new TextArea(event.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#F3F4F6") +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 6; -fx-padding: 10;");

        descBox.getChildren().addAll(descLabel, descArea);

        // Grille d'informations
        VBox infoBox = new VBox(15);
        infoBox.setPadding(new Insets(10, 0, 10, 0));

        // Ligne 1: Date
        HBox dateRow = new HBox(10);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");
        dateIcon.setFont(Font.font("System", 16));
        Label dateLabelTitle = new Label("Date :");
        dateLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        dateLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
        Label dateValue = new Label(event.getFormattedDate());
        dateValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        dateValue.setTextFill(Color.web(adminDashboard.getAccentColor()));
        dateRow.getChildren().addAll(dateIcon, dateLabelTitle, dateValue);

        // Ligne 2: Lieu
        HBox lieuRow = new HBox(10);
        lieuRow.setAlignment(Pos.CENTER_LEFT);
        Label lieuIcon = new Label("📍");
        lieuIcon.setFont(Font.font("System", 16));
        Label lieuLabelTitle = new Label("Lieu :");
        lieuLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        lieuLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
        Label lieuValue = new Label(event.getLieu());
        lieuValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        lieuValue.setTextFill(Color.web(adminDashboard.getTextColor()));
        lieuValue.setWrapText(true);
        lieuRow.getChildren().addAll(lieuIcon, lieuLabelTitle, lieuValue);

        // Ligne 3: Organisateur
        User org = userService.getUserById(event.getId_organisateur());
        HBox orgRow = new HBox(10);
        orgRow.setAlignment(Pos.CENTER_LEFT);
        Label orgIcon = new Label("👤");
        orgIcon.setFont(Font.font("System", 16));
        Label orgLabelTitle = new Label("Organisateur :");
        orgLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        orgLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
        Label orgValue = new Label(org != null ? org.getNomComplet() : "Inconnu");
        orgValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        orgValue.setTextFill(Color.web(adminDashboard.getSuccessColor()));
        orgRow.getChildren().addAll(orgIcon, orgLabelTitle, orgValue);

        // Ligne 4: Email
        HBox emailRow = new HBox(10);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        Label emailIcon = new Label("📧");
        emailIcon.setFont(Font.font("System", 16));
        Label emailLabelTitle = new Label("Email :");
        emailLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        emailLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
        Label emailValue = new Label(org != null ? org.getEmail() : "Inconnu");
        emailValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        emailValue.setTextFill(Color.web(adminDashboard.getTextColor()));
        emailRow.getChildren().addAll(emailIcon, emailLabelTitle, emailValue);

        // Ligne 5: Capacité
        HBox capRow = new HBox(10);
        capRow.setAlignment(Pos.CENTER_LEFT);
        Label capIcon = new Label("👥");
        capIcon.setFont(Font.font("System", 16));
        Label capLabelTitle = new Label("Capacité :");
        capLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        capLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
        String capacite = event.getCapacite_max() != null ? event.getCapacite_max() + " places" : "Illimitée";
        Label capValue = new Label(capacite + " (" + event.getParticipantsCount() + " inscrits)");
        capValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        capValue.setTextFill(Color.web(adminDashboard.getWarningColor()));
        capRow.getChildren().addAll(capIcon, capLabelTitle, capValue);

        // Ligne 6: Statut validation
        HBox statutRow = new HBox(10);
        statutRow.setAlignment(Pos.CENTER_LEFT);
        Label statutIcon = new Label("✅");
        statutIcon.setFont(Font.font("System", 16));
        Label statutLabelTitle = new Label("Validation :");
        statutLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        statutLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
        Label statutValue = new Label(event.getStatutValidationFr());
        statutValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        statutValue.setStyle("-fx-background-color: " + event.getStatutValidationColor() +
                "; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;");
        statutRow.getChildren().addAll(statutIcon, statutLabelTitle, statutValue);

        // Ligne 7: Publication
        HBox pubRow = new HBox(10);
        pubRow.setAlignment(Pos.CENTER_LEFT);
        Label pubIcon = new Label("📢");
        pubIcon.setFont(Font.font("System", 16));
        Label pubLabelTitle = new Label("Publication :");
        pubLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        pubLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
        Label pubValue = new Label(event.getStatutPublicationFr());
        pubValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        pubValue.setStyle("-fx-background-color: " +
                (event.isEstPublie() ? "#2ecc71" : "#95a5a6") + "; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;");
        pubRow.getChildren().addAll(pubIcon, pubLabelTitle, pubValue);

        infoBox.getChildren().addAll(dateRow, lieuRow, orgRow, emailRow, capRow, statutRow, pubRow);

        // Commentaire de validation (si existe)
        if (event.getCommentaireValidation() != null && !event.getCommentaireValidation().isEmpty()) {
            Separator sep = new Separator();
            sep.setPadding(new Insets(10, 0, 10, 0));

            HBox commentRow = new HBox(10);
            commentRow.setAlignment(Pos.TOP_LEFT);
            Label commentIcon = new Label("💬");
            commentIcon.setFont(Font.font("System", 16));
            Label commentLabelTitle = new Label("Commentaire :");
            commentLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
            commentLabelTitle.setTextFill(Color.web(adminDashboard.getTextColor()));
            Label commentValue = new Label(event.getCommentaireValidation());
            commentValue.setWrapText(true);
            commentValue.setFont(Font.font("System", 13));
            commentValue.setTextFill(Color.web(adminDashboard.getTextColor()));
            commentRow.getChildren().addAll(commentIcon, commentLabelTitle, commentValue);

            infoBox.getChildren().addAll(sep, commentRow);
        }

        // Historique
        if (event.getDateSoumission() != null || event.getDateValidation() != null || event.getDatePublication() != null) {
            Separator histoSep = new Separator();
            histoSep.setPadding(new Insets(15, 0, 10, 0));

            HBox histoTitle = new HBox(10);
            histoTitle.setAlignment(Pos.CENTER_LEFT);
            Label histoIcon = new Label("📋");
            histoIcon.setFont(Font.font("System", 16));
            Label histoLabel = new Label("Historique");
            histoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            histoLabel.setTextFill(Color.web(adminDashboard.getTextColor()));
            histoTitle.getChildren().addAll(histoIcon, histoLabel);

            VBox histoBox = new VBox(8);
            histoBox.setPadding(new Insets(10, 0, 0, 25));

            if (event.getDateSoumission() != null) {
                Label soumissionLabel = new Label("• Créé le: " + event.getDateSoumission().toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                soumissionLabel.setFont(Font.font("System", 12));
                soumissionLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
                histoBox.getChildren().add(soumissionLabel);
            }
            if (event.getDateValidation() != null) {
                String action = "approuve".equals(event.getStatutValidation()) ? "Approuvé" : "Refusé";
                Label validationLabel = new Label("• " + action + " le: " + event.getDateValidation().toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                validationLabel.setFont(Font.font("System", 12));
                validationLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
                histoBox.getChildren().add(validationLabel);
            }
            if (event.getDatePublication() != null) {
                Label publicationLabel = new Label("• Publié le: " + event.getDatePublication().toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                publicationLabel.setFont(Font.font("System", 12));
                publicationLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
                histoBox.getChildren().add(publicationLabel);
            }

            infoBox.getChildren().addAll(histoSep, histoTitle, histoBox);
        }

        // Assemblage du contenu
        VBox mainContent = new VBox(15);
        mainContent.getChildren().addAll(imageContainer, descBox, infoBox);

        content.getChildren().add(mainContent);

        // ScrollPane pour le contenu
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(500);

        // Bouton Fermer
        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(15, 25, 25, 25));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-width: 1 0 0 0;");

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 40; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().add(closeBtn);

        // Assemblage final
        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 650, 750);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void confirmDelete(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'événement ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement \"" + event.getTitre() + "\" ?\nCette action est irréversible et supprimera toutes les participations associées.");
        alert.initOwner(adminDashboard.getPrimaryStage());

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Supprimer");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventService.deleteEvent(event.getId_evenement())) {
                adminDashboard.showAlert("Succès", "✅ Événement supprimé avec succès !");
                refreshData();
            } else {
                adminDashboard.showError("Erreur", "❌ Impossible de supprimer l'événement");
            }
        }
    }

    private void exportEvents() {
        if (filteredData == null || filteredData.isEmpty()) {
            adminDashboard.showError("Erreur", "Aucun événement à exporter");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les événements");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        fileChooser.setInitialFileName("evenements_" + LocalDateTime.now().format(EXPORT_DATE_FORMAT) + ".csv");

        File file = fileChooser.showSaveDialog(adminDashboard.getPrimaryStage());
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("ID;Titre;Description;Date;Lieu;Organisateur;Email Organisateur;Capacité;Participants;Statut;Validation;Publication;Commentaire");

            for (Event event : filteredData) {
                User org = userService.getUserById(event.getId_organisateur());
                String orgName = org != null ? org.getNomComplet() : "Inconnu";
                String orgEmail = org != null ? org.getEmail() : "";

                writer.println(String.format("%d;%s;%s;%s;%s;%s;%s;%s;%d;%s;%s;%s;%s",
                        event.getId_evenement(),
                        escapeCSV(event.getTitre()),
                        escapeCSV(event.getDescription()),
                        event.getFormattedDate(),
                        escapeCSV(event.getLieu()),
                        escapeCSV(orgName),
                        escapeCSV(orgEmail),
                        event.getCapacite_max() != null ? event.getCapacite_max() : "Illimité",
                        event.getParticipantsCount(),
                        event.getStatut(),
                        event.getStatutValidationFr(),
                        event.getStatutPublicationFr(),
                        escapeCSV(event.getCommentaireValidation() != null ? event.getCommentaireValidation() : "")
                ));
            }

            adminDashboard.showAlert("Succès", "✅ Export réussi : " + filteredData.size() + " événements exportés");

        } catch (Exception e) {
            adminDashboard.showError("Erreur", "❌ Erreur lors de l'export : " + e.getMessage());
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}