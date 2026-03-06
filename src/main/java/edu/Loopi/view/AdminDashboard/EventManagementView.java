package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.Notification;
import edu.Loopi.entities.User;
import edu.Loopi.entities.Participation;
import edu.Loopi.services.EventService;
import edu.Loopi.services.UserService;
import edu.Loopi.services.NotificationService;
import edu.Loopi.services.ParticipationService;
import edu.Loopi.services.AddressSuggestionService;
import javafx.animation.PauseTransition;
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
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class EventManagementView {
    private User currentUser;
    private UserService userService;
    private EventService eventService;
    private NotificationService notificationService;
    private ParticipationService participationService;
    private AddressSuggestionService addressService;
    private AdminDashboard adminDashboard;

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

    // Constantes pour les chemins d'images
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String IMAGE_STORAGE_DIR = "src" + File.separator + "main" + File.separator +
            "resources" + File.separator + "uploads" + File.separator +
            "events" + File.separator;
    private static final String FULL_IMAGE_PATH = PROJECT_ROOT + File.separator + IMAGE_STORAGE_DIR;

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

        totalEventsLabel = new Label("0");
        pendingEventsLabel = new Label("0");
        approvedEventsLabel = new Label("0");
        rejectedEventsLabel = new Label("0");
        publishedEventsLabel = new Label("0");
        pastEventsLabel = new Label("0");
        totalParticipantsLabel = new Label("0");

        statsBar.getChildren().addAll(
                createStatCard("📊", "Total événements", totalEventsLabel, adminDashboard.getAccentColor()),
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
        card.setPrefWidth(130);

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

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 8 12; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setTooltip(new Tooltip("Actualiser"));
        refreshBtn.setOnAction(e -> refreshData());

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#F3F4F6") +
                "; -fx-background-radius: 20; -fx-padding: 5 15;");
        searchBox.setPrefWidth(250);

        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("System", 14));

        searchField = new TextField();
        searchField.setPromptText("Rechercher un événement...");
        searchField.setStyle("-fx-background-color: transparent; -fx-pref-width: 200px; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        searchField.textProperty().addListener((obs, old, nv) -> applyFilters());

        searchBox.getChildren().addAll(searchIcon, searchField);

        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");

        VBox statusBox = new VBox(2);
        Label statusLabel = new Label("Statut (date)");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-font-weight: bold;");

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "À venir", "En cours", "Passés");
        statusFilter.setValue("Tous");
        statusFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> applyFilters());

        statusBox.getChildren().addAll(statusLabel, statusFilter);

        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");

        VBox validationBox = new VBox(2);
        Label validationLabel = new Label("Validation");
        validationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-font-weight: bold;");

        validationFilter = new ComboBox<>();
        validationFilter.getItems().addAll("Tous", "En attente", "Approuvés", "Refusés");
        validationFilter.setValue("Tous");
        validationFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        validationFilter.setPrefWidth(120);
        validationFilter.setOnAction(e -> applyFilters());

        validationBox.getChildren().addAll(validationLabel, validationFilter);

        Separator sep3 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep3.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");

        VBox publicationBox = new VBox(2);
        Label publicationLabel = new Label("Publication");
        publicationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-font-weight: bold;");

        publicationFilter = new ComboBox<>();
        publicationFilter.getItems().addAll("Tous", "Brouillon", "Publiés");
        publicationFilter.setValue("Tous");
        publicationFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        publicationFilter.setPrefWidth(120);
        publicationFilter.setOnAction(e -> applyFilters());

        publicationBox.getChildren().addAll(publicationLabel, publicationFilter);

        Separator sep4 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep4.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");

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
                "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        organisateurFilter.setPrefWidth(150);
        organisateurFilter.setOnAction(e -> applyFilters());

        orgBox.getChildren().addAll(orgLabel, organisateurFilter);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("📥 Exporter CSV");
        exportBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-cursor: hand;");
        exportBtn.setOnAction(e -> exportEvents());

        Button resetBtn = new Button("✕ Réinitialiser");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getDangerColor() +
                "; -fx-border-color: " + adminDashboard.getDangerColor() + "; -fx-border-radius: 20; -fx-padding: 8 16; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");
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

        VBox.setVgrow(eventsTable, Priority.ALWAYS);
        tableContainer.getChildren().addAll(tableTitle, eventsTable);
        centerSection.getChildren().add(tableContainer);

        return centerSection;
    }

    @SuppressWarnings("unchecked")
    private void createEventsTable(boolean isDarkMode) {
        eventsTable = new TableView<>();
        eventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        eventsTable.setPrefHeight(550);
        eventsTable.setStyle("-fx-background-color: transparent; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 8;");

        TableColumn<Event, String> imageCol = new TableColumn<>("Image");
        imageCol.setPrefWidth(80);
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
                            String fileName = event.getImage_evenement().substring(event.getImage_evenement().lastIndexOf('/') + 1);
                            File imgFile = new File(FULL_IMAGE_PATH + fileName);
                            if (imgFile.exists()) {
                                imageView.setImage(new Image(imgFile.toURI().toString()));
                            } else {
                                imageView.setImage(new Image("https://via.placeholder.com/60x50/" +
                                        adminDashboard.getAccentColor().substring(1) + "/ffffff?text=📅"));
                            }
                        } else {
                            imageView.setImage(new Image("https://via.placeholder.com/60x50/" +
                                    adminDashboard.getAccentColor().substring(1) + "/ffffff?text=📅"));
                        }
                    } catch (Exception e) {
                        imageView.setImage(new Image("https://via.placeholder.com/60x50/" +
                                adminDashboard.getAccentColor().substring(1) + "/ffffff?text=📅"));
                    }
                    setGraphic(imageView);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Event, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id_evenement"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Event, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        titreCol.setPrefWidth(200);
        titreCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        TableColumn<Event, String> organisateurCol = new TableColumn<>("Organisateur");
        organisateurCol.setCellValueFactory(cellData -> {
            User org = userService.getUserById(cellData.getValue().getId_organisateur());
            return new javafx.beans.property.SimpleStringProperty(
                    org != null ? org.getPrenom() + " " + org.getNom() : "Inconnu"
            );
        });
        organisateurCol.setPrefWidth(150);
        organisateurCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        TableColumn<Event, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFormattedDate()
                ));
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");
        dateCol.setCellFactory(col -> new TableCell<Event, String>() {
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

        TableColumn<Event, String> lieuCol = new TableColumn<>("Lieu");
        lieuCol.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        lieuCol.setPrefWidth(150);
        lieuCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        TableColumn<Event, Integer> participantsCol = new TableColumn<>("Participants");
        participantsCol.setCellValueFactory(new PropertyValueFactory<>("participantsCount"));
        participantsCol.setPrefWidth(90);
        participantsCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Event, String> capaciteCol = new TableColumn<>("Capacité");
        capaciteCol.setCellValueFactory(cellData -> {
            Integer cap = cellData.getValue().getCapacite_max();
            return new javafx.beans.property.SimpleStringProperty(cap != null ? cap.toString() : "Illimité");
        });
        capaciteCol.setPrefWidth(80);
        capaciteCol.setStyle("-fx-alignment: CENTER;");

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
                        case "à venir": color = "#f39c12"; break;
                        case "en cours": color = "#9b59b6"; break;
                        case "passé": color = "#6c757d"; break;
                        default: color = "#6c757d";
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
        statutDateCol.setPrefWidth(80);

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
        validationCol.setPrefWidth(90);

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
        publicationCol.setPrefWidth(90);

        TableColumn<Event, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(400);
        actionsCol.setCellFactory(col -> new TableCell<Event, Void>() {
            private final Button viewBtn = createActionButton("👁️", "Voir détails", adminDashboard.getAccentColor());
            private final Button editBtn = createActionButton("✏️", "Modifier", adminDashboard.getWarningColor());
            private final Button approveBtn = createActionButton("✅", "Approuver", adminDashboard.getSuccessColor());
            private final Button rejectBtn = createActionButton("❌", "Refuser", adminDashboard.getDangerColor());
            private final Button participantsBtn = createActionButton("👥", "Participants", "#9b59b6");
            private final Button deleteBtn = createActionButton("🗑️", "Supprimer", "#6c757d");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, approveBtn, rejectBtn, participantsBtn, deleteBtn);
            {
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

                    if ("approuve".equals(event.getStatutValidation())) {
                        approveBtn.setDisable(true);
                        approveBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                    } else {
                        approveBtn.setDisable(false);
                        approveBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
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
    }

    private Button createActionButton(String icon, String tooltip, String color) {
        Button btn = new Button(icon);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
        btn.setTooltip(new Tooltip(tooltip));
        btn.setMinWidth(35);
        btn.setPrefWidth(35);
        return btn;
    }

    private void loadEvents() {
        List<Event> events = eventService.refreshEvents();
        masterData = FXCollections.observableArrayList(events);
        filteredData = new FilteredList<>(masterData, p -> true);
        eventsTable.setItems(filteredData);

        updateStats();
    }

    private void refreshData() {
        eventService.refreshEvents();
        loadEvents();
        applyFilters();
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
        content.setPrefWidth(900);
        content.setPrefHeight(650);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("👥");
        iconLabel.setFont(Font.font("System", 32));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label(event.getTitre() + " - Participants");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label countLabel = new Label(event.getParticipantsCount() + " participant(s)");
        countLabel.setFont(Font.font("System", 14));
        countLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(titleLabel, countLabel);
        header.getChildren().addAll(iconLabel, headerText);

        HBox statsBox = new HBox(15);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#F3F4F6") +
                "; -fx-background-radius: 12;");

        VBox inscritsBox = createParticipantStat("📝 Inscrits", String.valueOf(event.getParticipantsInscrits()), "#3b82f6");
        VBox presentsBox = createParticipantStat("✅ Présents", String.valueOf(event.getParticipantsPresents()), "#10b981");
        VBox absentsBox = createParticipantStat("❌ Absents", String.valueOf(event.getParticipantsAbsents()), "#ef4444");

        statsBox.getChildren().addAll(inscritsBox, presentsBox, absentsBox);

        TableView<Participation> participantsTable = new TableView<>();
        participantsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        participantsTable.setPrefHeight(350);
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
        nomCol.setPrefWidth(150);
        nomCol.setCellFactory(col -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });

        TableColumn<Participation, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> {
            User user = userService.getUserById(cellData.getValue().getIdUser());
            return new javafx.beans.property.SimpleStringProperty(user != null ? user.getEmail() : "Inconnu");
        });
        emailCol.setPrefWidth(180);
        emailCol.setCellFactory(col -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });

        TableColumn<Participation, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setPrefWidth(120);

        TableColumn<Participation, String> ageCol = new TableColumn<>("Âge");
        ageCol.setCellValueFactory(cellData -> {
            Integer age = cellData.getValue().getAge();
            return new javafx.beans.property.SimpleStringProperty(age != null ? age.toString() : "-");
        });
        ageCol.setPrefWidth(60);
        ageCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Participation, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
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
                    badge.setPadding(new Insets(4, 10, 4, 10));
                    String color = statut.equals("inscrit") ? "#3b82f6" :
                            statut.equals("present") ? "#10b981" : "#ef4444";
                    badge.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statutCol.setPrefWidth(80);

        TableColumn<Participation, String> dateCol = new TableColumn<>("Date inscription");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        dateCol.setPrefWidth(130);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Participation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(col -> new TableCell<Participation, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: " + adminDashboard.getWarningColor() + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                editBtn.setTooltip(new Tooltip("Modifier statut"));

                deleteBtn.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
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
        closeBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");
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
        box.setPadding(new Insets(10, 20, 10, 20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));

        Label labelLabel = new Label(label);
        labelLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
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
        content.setPrefWidth(450);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Label title = new Label("Modifier le statut");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        User participant = userService.getUserById(participation.getIdUser());
        Label participantName = new Label("Participant: " + (participant != null ? participant.getNomComplet() : "Inconnu"));
        participantName.setFont(Font.font("System", 13));
        participantName.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("inscrit", "present", "absent");
        statutCombo.setValue(participation.getStatut());
        statutCombo.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 8; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        statutCombo.setPrefWidth(Double.MAX_VALUE);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

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
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la participation ?");
        alert.setContentText("Voulez-vous vraiment supprimer la participation de " + participantName + " ?");

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

    private void showEventDetails(Event event) {
        Stage dialog = new Stage();
        dialog.setTitle("Détails de l'événement");
        dialog.initOwner(adminDashboard.getPrimaryStage());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(800);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📅");
        iconLabel.setFont(Font.font("System", 32));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label(event.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label idLabel = new Label("ID: " + event.getId_evenement());
        idLabel.setFont(Font.font("System", 12));
        idLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(titleLabel, idLabel);
        header.getChildren().addAll(iconLabel, headerText);

        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#F3F4F6") +
                "; -fx-background-radius: 10;");
        imageContainer.setPrefHeight(200);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(750);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        try {
            if (event.getImage_evenement() != null && !event.getImage_evenement().isEmpty()) {
                String fileName = event.getImage_evenement().substring(event.getImage_evenement().lastIndexOf('/') + 1);
                File imgFile = new File(FULL_IMAGE_PATH + fileName);
                if (imgFile.exists()) {
                    imageView.setImage(new Image(imgFile.toURI().toString()));
                }
            }
        } catch (Exception e) {
            // Ignorer
        }
        imageContainer.getChildren().add(imageView);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(15);
        infoGrid.setPadding(new Insets(10, 0, 10, 0));

        infoGrid.add(new Label("Description:"), 0, 0);
        TextArea descArea = new TextArea(event.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(600);
        infoGrid.add(descArea, 1, 0);

        infoGrid.add(new Label("Date:"), 0, 1);
        Label dateValue = new Label(event.getFormattedDate());
        dateValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        infoGrid.add(dateValue, 1, 1);

        infoGrid.add(new Label("Lieu:"), 0, 2);
        Label lieuValue = new Label(event.getLieu());
        lieuValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        infoGrid.add(lieuValue, 1, 2);

        infoGrid.add(new Label("Organisateur:"), 0, 3);
        User org = userService.getUserById(event.getId_organisateur());
        Label orgValue = new Label(org != null ? org.getNomComplet() : "Inconnu");
        orgValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        infoGrid.add(orgValue, 1, 3);

        infoGrid.add(new Label("Email:"), 0, 4);
        Label emailValue = new Label(org != null ? org.getEmail() : "Inconnu");
        emailValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        infoGrid.add(emailValue, 1, 4);

        infoGrid.add(new Label("Capacité:"), 0, 5);
        String capacite = event.getCapacite_max() != null ? event.getCapacite_max() + " places" : "Illimitée";
        Label capaciteValue = new Label(capacite + " (" + event.getParticipantsCount() + " inscrits)");
        capaciteValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        infoGrid.add(capaciteValue, 1, 5);

        infoGrid.add(new Label("Statut validation:"), 0, 6);
        Label statutValue = new Label(event.getStatutValidationFr());
        statutValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        statutValue.setStyle("-fx-background-color: " + event.getStatutValidationColor() +
                "; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;");
        infoGrid.add(statutValue, 1, 6);

        infoGrid.add(new Label("Publication:"), 0, 7);
        Label pubValue = new Label(event.getStatutPublicationFr());
        pubValue.setFont(Font.font("System", FontWeight.BOLD, 13));
        pubValue.setStyle("-fx-background-color: " +
                (event.isEstPublie() ? "#2ecc71" : "#95a5a6") + "; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;");
        infoGrid.add(pubValue, 1, 7);

        if (event.getCommentaireValidation() != null && !event.getCommentaireValidation().isEmpty()) {
            infoGrid.add(new Label("Commentaire:"), 0, 8);
            Label commentaireValue = new Label(event.getCommentaireValidation());
            commentaireValue.setWrapText(true);
            infoGrid.add(commentaireValue, 1, 8);
        }

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(600);
        infoGrid.getColumnConstraints().addAll(col1, col2);

        Label histoTitre = new Label("📋 Historique des actions");
        histoTitre.setFont(Font.font("System", FontWeight.BOLD, 14));
        histoTitre.setTextFill(Color.web(adminDashboard.getTextColor()));

        VBox histoBox = new VBox(5);
        if (event.getDateSoumission() != null) {
            histoBox.getChildren().add(new Label("• Créé le: " + event.getDateSoumission().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
        if (event.getDateValidation() != null) {
            String action = "approuve".equals(event.getStatutValidation()) ? "Approuvé" : "Refusé";
            histoBox.getChildren().add(new Label("• " + action + " le: " + event.getDateValidation().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
        if (event.getDatePublication() != null) {
            histoBox.getChildren().add(new Label("• Publié le: " + event.getDatePublication().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }

        content.getChildren().addAll(header, imageContainer, infoGrid);

        if (!histoBox.getChildren().isEmpty()) {
            content.getChildren().addAll(histoTitre, histoBox);
        }

        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        Scene scene = new Scene(dialogPane, 850, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showEditEventDialog(Event event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("✏️ Modifier l'événement");
        dialog.initOwner(adminDashboard.getPrimaryStage());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(600);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Label title = new Label("Modifier l'événement");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        grid.add(new Label("Titre:"), 0, 0);
        TextField titreField = new TextField(event.getTitre());
        titreField.setPrefWidth(400);
        grid.add(titreField, 1, 0);

        grid.add(new Label("Description:"), 0, 1);
        TextArea descArea = new TextArea(event.getDescription());
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(400);
        descArea.setWrapText(true);
        grid.add(descArea, 1, 1);

        grid.add(new Label("Date:"), 0, 2);
        DatePicker datePicker = new DatePicker(event.getDate_evenement().toLocalDate());
        datePicker.setPrefWidth(150);
        grid.add(datePicker, 1, 2);

        grid.add(new Label("Heure:"), 0, 3);
        HBox timeBox = new HBox(10);
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, event.getDate_evenement().getHour());
        hourSpinner.setPrefWidth(80);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, event.getDate_evenement().getMinute());
        minuteSpinner.setPrefWidth(80);
        timeBox.getChildren().addAll(hourSpinner, new Label("h"), minuteSpinner);
        grid.add(timeBox, 1, 3);

        grid.add(new Label("Lieu:"), 0, 4);
        TextField lieuField = new TextField(event.getLieu());
        lieuField.setPrefWidth(400);
        grid.add(lieuField, 1, 4);

        grid.add(new Label("Capacité max:"), 0, 5);
        HBox capaciteBox = new HBox(10);
        Spinner<Integer> capaciteSpinner = new Spinner<>(1, 1000,
                event.getCapacite_max() != null ? event.getCapacite_max() : 50);
        capaciteSpinner.setEditable(true);
        capaciteSpinner.setPrefWidth(100);

        CheckBox capaciteIllimitee = new CheckBox("Illimitée");
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

        capaciteBox.getChildren().addAll(capaciteSpinner, capaciteIllimitee);
        grid.add(capaciteBox, 1, 5);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(450);
        grid.getColumnConstraints().addAll(col1, col2);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            event.setTitre(titreField.getText());
            event.setDescription(descArea.getText());
            event.setDate_evenement(LocalDateTime.of(datePicker.getValue(),
                    LocalDateTime.of(0, 1, 1, hourSpinner.getValue(), minuteSpinner.getValue()).toLocalTime()));
            event.setLieu(lieuField.getText());

            if (!capaciteIllimitee.isSelected()) {
                event.setCapacite_max(capaciteSpinner.getValue());
            } else {
                event.setCapacite_max(null);
            }

            if (eventService.updateEvent(event)) {
                adminDashboard.showAlert("Succès", "✅ Événement modifié avec succès !");
                refreshData();
                dialog.close();
            } else {
                adminDashboard.showError("Erreur", "❌ Impossible de modifier l'événement");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        VBox mainContent = new VBox(10, grid, buttonBox);
        content.getChildren().addAll(title, mainContent);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showApproveDialog(Event event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Approuver l'événement");
        dialog.setHeaderText("Approuver \"" + event.getTitre() + "\"");
        dialog.initOwner(adminDashboard.getPrimaryStage());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        Label infoLabel = new Label("Vous êtes sur le point d'approuver cet événement. " +
                "Il pourra ensuite être publié par l'organisateur pour être visible par les participants.");
        infoLabel.setWrapText(true);

        TextArea commentaireArea = new TextArea();
        commentaireArea.setPromptText("Commentaire pour l'organisateur (optionnel)");
        commentaireArea.setPrefRowCount(3);

        content.getChildren().addAll(infoLabel, new Label("Commentaire:"), commentaireArea);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Approuver");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

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
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        Label infoLabel = new Label("Veuillez indiquer le motif du refus. " +
                "Ce message sera envoyé à l'organisateur.");
        infoLabel.setWrapText(true);

        TextArea motifArea = new TextArea();
        motifArea.setPromptText("Motif du refus (obligatoire)");
        motifArea.setPrefRowCount(4);

        content.getChildren().addAll(infoLabel, new Label("Motif du refus *"), motifArea);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Refuser");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        final Button ok = okButton;
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, actionEvent -> {
            if (motifArea.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Validation");
                alert.setHeaderText(null);
                alert.setContentText("Le motif du refus est obligatoire !");
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

    private void confirmDelete(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'événement ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement \"" + event.getTitre() + "\" ?\nCette action est irréversible.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + ";");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Supprimer");
        okButton.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

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