package edu.Loopi.view;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.Participation;
import edu.Loopi.entities.User;
import edu.Loopi.services.EventService;
import edu.Loopi.services.ParticipationService;
import edu.Loopi.services.UserService;
import edu.Loopi.services.NotificationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParticipantsView {
    private User currentUser;
    private EventService eventService = new EventService();
    private ParticipationService participationService = new ParticipationService();
    private UserService userService = new UserService();
    private NotificationService notificationService = new NotificationService();

    // Constantes de couleurs
    private static final String PRIMARY_COLOR = "#2196F3";
    private static final String SUCCESS_COLOR = "#10b981";
    private static final String WARNING_COLOR = "#f59e0b";
    private static final String DANGER_COLOR = "#ef4444";
    private static final String DARK_COLOR = "#1e293b";
    private static final String LIGHT_GRAY = "#f8fafc";
    private static final String BORDER_COLOR = "#e2e8f0";

    // Formatteurs de date pour l'export
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EXPORT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter EXPORT_FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Composants principaux
    private VBox mainContainer;
    private TableView<Participation> participantsTable;
    private ComboBox<Event> eventSelector;
    private ComboBox<String> statusFilter;
    private TextField searchField;

    // Labels pour les statistiques globales
    private Label totalParticipantsLabel;
    private Label totalInscritsLabel;
    private Label totalPresentsLabel;
    private Label totalAbsentsLabel;
    private Label eventParticipantsCount;

    // Labels pour les statistiques de l'événement sélectionné
    private Label eventInscritsValue;
    private Label eventPresentsValue;
    private Label eventAbsentsValue;
    private Label eventCompletionValue;
    private Label eventTauxPresenceValue;

    // Labels pour les détails de l'événement
    private Label eventTitreValue;
    private Label eventDateValue;
    private Label eventLieuValue;
    private Label eventCapaciteValue;
    private Label eventStatutValue;

    // Données observables
    private ObservableList<Event> eventsData;
    private ObservableList<Participation> allParticipations;
    private FilteredList<Participation> filteredParticipations;
    private SortedList<Participation> sortedParticipations;

    // Panel d'information de l'événement
    private VBox eventInfoPanel;
    private Event currentSelectedEvent;

    public ParticipantsView(User user) {
        this.currentUser = user;
        this.eventsData = FXCollections.observableArrayList();
        this.allParticipations = FXCollections.observableArrayList();
    }

    @SuppressWarnings("unchecked")
    public VBox getView() {
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: #f1f5f9;");

        VBox header = createHeader();
        HBox topToolbar = createTopToolbar();
        HBox globalStats = createGlobalStatistics();
        VBox eventSelectorBox = createEventSelector();
        eventInfoPanel = createEventInfoPanel();
        VBox filterBox = createFilterBox();
        VBox tableBox = createParticipantsTable();
        HBox bottomToolbar = createBottomToolbar();

        mainContainer.getChildren().addAll(
                header,
                topToolbar,
                globalStats,
                eventSelectorBox,
                eventInfoPanel,
                filterBox,
                tableBox,
                bottomToolbar
        );

        loadData();

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox root = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 2 0;");

        HBox titleRow = new HBox(20);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("👥");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        iconLabel.setMinWidth(70);
        iconLabel.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(5);
        Label mainTitle = new Label("Gestion des Participants");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        mainTitle.setTextFill(Color.web(DARK_COLOR));

        Label subtitle = new Label("Gérez tous les participants à vos événements écologiques");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#64748b"));
        subtitle.setWrapText(true);

        titleBox.getChildren().addAll(mainTitle, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateBadge = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        dateBadge.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        dateBadge.setTextFill(Color.web(PRIMARY_COLOR));
        dateBadge.setPadding(new Insets(8, 20, 8, 20));
        dateBadge.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 30;");

        titleRow.getChildren().addAll(iconLabel, titleBox, spacer, dateBadge);
        header.getChildren().add(titleRow);

        return header;
    }

    private HBox createTopToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(15));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setTooltip(new Tooltip("Actualiser les données"));
        refreshBtn.setOnAction(e -> refreshData());

        Button exportAllBtn = new Button("📥 Exporter");
        exportAllBtn.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        exportAllBtn.setTooltip(new Tooltip("Exporter tous les participants"));
        exportAllBtn.setOnAction(e -> exportAllParticipants());

        Button statsBtn = new Button("📊 Rapport statistique");
        statsBtn.setStyle("-fx-background-color: " + WARNING_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        statsBtn.setTooltip(new Tooltip("Générer un rapport statistique détaillé"));
        statsBtn.setOnAction(e -> showDetailedStats());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        eventParticipantsCount = new Label("0 participant(s)");
        eventParticipantsCount.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        eventParticipantsCount.setTextFill(Color.web(PRIMARY_COLOR));

        toolbar.getChildren().addAll(refreshBtn, exportAllBtn, statsBtn, spacer, eventParticipantsCount);

        return toolbar;
    }

    private HBox createGlobalStatistics() {
        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        statsBox.setAlignment(Pos.CENTER);

        totalParticipantsLabel = new Label("0");
        VBox totalCard = createStatCard("👥 Total participants", totalParticipantsLabel, DARK_COLOR, "Tous les participants");

        totalInscritsLabel = new Label("0");
        VBox inscritsCard = createStatCard("📝 Inscrits", totalInscritsLabel, "#3b82f6", "En attente de confirmation");

        totalPresentsLabel = new Label("0");
        VBox presentsCard = createStatCard("✅ Présents", totalPresentsLabel, SUCCESS_COLOR, "Ont participé à l'événement");

        totalAbsentsLabel = new Label("0");
        VBox absentsCard = createStatCard("❌ Absents", totalAbsentsLabel, DANGER_COLOR, "Ne se sont pas présentés");

        statsBox.getChildren().addAll(totalCard, inscritsCard, presentsCard, absentsCard);
        return statsBox;
    }

    private VBox createStatCard(String title, Label valueLabel, String color, String description) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 30, 15, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 4 0;");
        card.setPrefWidth(200);

        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web(DARK_COLOR));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 11));
        descLabel.setTextFill(Color.web("#64748b"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(valueLabel, titleLabel, descLabel);
        return card;
    }

    private VBox createEventSelector() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label selectorTitle = new Label("📋 Sélectionner un événement");
        selectorTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        selectorTitle.setTextFill(Color.web(DARK_COLOR));
        selectorTitle.setWrapText(true);

        HBox selectorContent = new HBox(15);
        selectorContent.setAlignment(Pos.CENTER_LEFT);

        Label selectorLabel = new Label("Événement :");
        selectorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        selectorLabel.setMinWidth(80);
        selectorLabel.setWrapText(true);

        eventSelector = new ComboBox<>();
        eventSelector.setPrefWidth(500);
        eventSelector.setStyle("-fx-background-radius: 8; -fx-padding: 8 12; " +
                "-fx-background-color: white; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        eventSelector.setCellFactory(lv -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else if (event.getId_evenement() == -1) {
                    setText("📋 Tous les participants (tous les événements)");
                } else {
                    int count = participationService.countParticipantsByEvent(event.getId_evenement());
                    setText(event.getTitre() + " - " + event.getFormattedDate() +
                            " (" + count + " participant" + (count > 1 ? "s" : "") + ")");
                }
                setWrapText(true);
            }
        });

        eventSelector.setButtonCell(new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else if (event.getId_evenement() == -1) {
                    setText("📋 Tous les participants");
                } else {
                    setText(event.getTitre() + " - " + event.getFormattedDate());
                }
                setWrapText(true);
            }
        });

        eventSelector.setOnAction(e -> {
            Event selected = eventSelector.getValue();
            if (selected != null) {
                if (selected.getId_evenement() == -1) {
                    clearEventSelection();
                } else {
                    selectEvent(selected);
                }
            }
        });

        Button clearSelectionBtn = new Button("✕ Effacer la sélection");
        clearSelectionBtn.setStyle("-fx-background-color: " + BORDER_COLOR + "; -fx-text-fill: " + DARK_COLOR + "; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        clearSelectionBtn.setTooltip(new Tooltip("Afficher tous les participants"));
        clearSelectionBtn.setOnAction(e -> clearEventSelection());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        selectorContent.getChildren().addAll(selectorLabel, eventSelector, spacer, clearSelectionBtn);
        box.getChildren().addAll(selectorTitle, selectorContent);

        return box;
    }

    private VBox createEventInfoPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 2; -fx-border-radius: 16;");
        panel.setVisible(false);
        panel.setManaged(false);

        Label panelTitle = new Label("📊 Statistiques de l'événement sélectionné");
        panelTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        panelTitle.setTextFill(Color.web(PRIMARY_COLOR));
        panelTitle.setWrapText(true);

        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(30);
        mainGrid.setVgap(15);

        VBox leftColumn = new VBox(15);
        leftColumn.setPadding(new Insets(10));
        leftColumn.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 12; -fx-padding: 15;");

        Label infoTitle = new Label("📌 Informations générales");
        infoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        infoTitle.setTextFill(Color.web(DARK_COLOR));
        infoTitle.setWrapText(true);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(12);

        Label titreLabel = new Label("📝 Titre :");
        titreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        titreLabel.setWrapText(true);
        eventTitreValue = new Label();
        eventTitreValue.setStyle("-fx-font-weight: bold; -fx-text-fill: " + PRIMARY_COLOR + ";");
        eventTitreValue.setWrapText(true);
        infoGrid.add(titreLabel, 0, 0);
        infoGrid.add(eventTitreValue, 1, 0);

        Label dateLabel = new Label("📅 Date :");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        dateLabel.setWrapText(true);
        eventDateValue = new Label();
        eventDateValue.setWrapText(true);
        infoGrid.add(dateLabel, 0, 1);
        infoGrid.add(eventDateValue, 1, 1);

        Label lieuLabel = new Label("📍 Lieu :");
        lieuLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        lieuLabel.setWrapText(true);
        eventLieuValue = new Label();
        eventLieuValue.setWrapText(true);
        infoGrid.add(lieuLabel, 0, 2);
        infoGrid.add(eventLieuValue, 1, 2);

        Label capaciteLabel = new Label("👥 Capacité :");
        capaciteLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        capaciteLabel.setWrapText(true);
        eventCapaciteValue = new Label();
        eventCapaciteValue.setWrapText(true);
        infoGrid.add(capaciteLabel, 0, 3);
        infoGrid.add(eventCapaciteValue, 1, 3);

        Label statutLabel = new Label("📊 Statut :");
        statutLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        statutLabel.setWrapText(true);
        eventStatutValue = new Label();
        eventStatutValue.setWrapText(true);
        infoGrid.add(statutLabel, 0, 4);
        infoGrid.add(eventStatutValue, 1, 4);

        leftColumn.getChildren().addAll(infoTitle, infoGrid);

        VBox rightColumn = new VBox(15);
        rightColumn.setPadding(new Insets(10));
        rightColumn.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 12; -fx-padding: 15;");

        Label statsTitle = new Label("📈 Statistiques de participation");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statsTitle.setTextFill(Color.web(DARK_COLOR));
        statsTitle.setWrapText(true);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(12);

        Label inscritLabel = new Label("📝 Inscrits :");
        inscritLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        inscritLabel.setWrapText(true);
        eventInscritsValue = new Label("0");
        eventInscritsValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventInscritsValue.setTextFill(Color.web("#3b82f6"));
        eventInscritsValue.setWrapText(true);
        statsGrid.add(inscritLabel, 0, 0);
        statsGrid.add(eventInscritsValue, 1, 0);

        Label presentLabel = new Label("✅ Présents :");
        presentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        presentLabel.setWrapText(true);
        eventPresentsValue = new Label("0");
        eventPresentsValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventPresentsValue.setTextFill(Color.web(SUCCESS_COLOR));
        eventPresentsValue.setWrapText(true);
        statsGrid.add(presentLabel, 0, 1);
        statsGrid.add(eventPresentsValue, 1, 1);

        Label absentLabel = new Label("❌ Absents :");
        absentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        absentLabel.setWrapText(true);
        eventAbsentsValue = new Label("0");
        eventAbsentsValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventAbsentsValue.setTextFill(Color.web(DANGER_COLOR));
        eventAbsentsValue.setWrapText(true);
        statsGrid.add(absentLabel, 0, 2);
        statsGrid.add(eventAbsentsValue, 1, 2);

        Label completionLabel = new Label("📊 Taux remplissage :");
        completionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        completionLabel.setWrapText(true);
        eventCompletionValue = new Label("0%");
        eventCompletionValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventCompletionValue.setTextFill(Color.web(WARNING_COLOR));
        eventCompletionValue.setWrapText(true);
        statsGrid.add(completionLabel, 0, 3);
        statsGrid.add(eventCompletionValue, 1, 3);

        Label presenceLabel = new Label("✅ Taux présence :");
        presenceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        presenceLabel.setWrapText(true);
        eventTauxPresenceValue = new Label("0%");
        eventTauxPresenceValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventTauxPresenceValue.setTextFill(Color.web(SUCCESS_COLOR));
        eventTauxPresenceValue.setWrapText(true);
        statsGrid.add(presenceLabel, 0, 4);
        statsGrid.add(eventTauxPresenceValue, 1, 4);

        rightColumn.getChildren().addAll(statsTitle, statsGrid);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");

        mainGrid.add(leftColumn, 0, 0);
        mainGrid.add(rightColumn, 1, 0);

        panel.getChildren().addAll(panelTitle, mainGrid, progressBar);
        return panel;
    }

    private VBox createFilterBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15, 20, 15, 20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label filterTitle = new Label("🔍 Filtrer les participants");
        filterTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        filterTitle.setTextFill(Color.web(DARK_COLOR));
        filterTitle.setWrapText(true);

        HBox filterContent = new HBox(15);
        filterContent.setAlignment(Pos.CENTER_LEFT);

        Label searchLabel = new Label("Rechercher :");
        searchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        searchLabel.setWrapText(true);

        searchField = new TextField();
        searchField.setPromptText("Nom, prénom, email ou contact...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 8; -fx-padding: 8; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");
        searchField.textProperty().addListener((obs, old, nv) -> applyFilters());

        Label statusLabel = new Label("Statut :");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statusLabel.setWrapText(true);

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Inscrits", "Présents", "Absents");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(120);
        statusFilter.setStyle("-fx-background-radius: 8; -fx-padding: 8; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");
        statusFilter.setOnAction(e -> applyFilters());

        Button clearFiltersBtn = new Button("✕ Réinitialiser les filtres");
        clearFiltersBtn.setStyle("-fx-background-color: " + BORDER_COLOR + "; -fx-text-fill: " + DARK_COLOR + "; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        clearFiltersBtn.setTooltip(new Tooltip("Effacer tous les filtres"));
        clearFiltersBtn.setOnAction(e -> clearFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterContent.getChildren().addAll(searchLabel, searchField, statusLabel, statusFilter, spacer, clearFiltersBtn);
        box.getChildren().addAll(filterTitle, filterContent);

        return box;
    }

    @SuppressWarnings("unchecked")
    private VBox createParticipantsTable() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label tableTitle = new Label("📋 Liste des participants");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web(DARK_COLOR));
        tableTitle.setWrapText(true);

        participantsTable = new TableView<>();
        participantsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        participantsTable.setPrefHeight(400);
        participantsTable.setMinHeight(300);
        participantsTable.setStyle("-fx-background-color: transparent; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        TableColumn<Participation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Participation, String> participantCol = new TableColumn<>("Participant");
        participantCol.setCellValueFactory(cellData -> {
            User user = userService.getUserById(cellData.getValue().getIdUser());
            String fullName = user != null ? user.getPrenom() + " " + user.getNom() : "Utilisateur inconnu";
            return new SimpleStringProperty(fullName);
        });
        participantCol.setPrefWidth(180);

        TableColumn<Participation, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> {
            User user = userService.getUserById(cellData.getValue().getIdUser());
            return new SimpleStringProperty(user != null ? user.getEmail() : "N/A");
        });
        emailCol.setPrefWidth(180);

        TableColumn<Participation, String> contactCol = new TableColumn<>("📞 Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setPrefWidth(140);

        TableColumn<Participation, String> ageCol = new TableColumn<>("Âge");
        ageCol.setCellValueFactory(cellData -> {
            Integer age = cellData.getValue().getAge();
            return new SimpleStringProperty(age != null ? age + " ans" : "-");
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
                    badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                    badge.setTextFill(Color.WHITE);
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle("-fx-background-color: " + getStatusColor(statut) + "; -fx-background-radius: 15;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Participation, String> dateCol = new TableColumn<>("📅 Date d'inscription");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        dateCol.setPrefWidth(130);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Participation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<Participation, Void>() {
            private final Button viewBtn = createIconButton("👤", "#3b82f6", "Voir détails");
            private final Button presentBtn = createIconButton("✅", SUCCESS_COLOR, "Marquer présent");
            private final Button absentBtn = createIconButton("❌", DANGER_COLOR, "Marquer absent");
            private final Button deleteBtn = createIconButton("🗑️", "#6b7280", "Supprimer");
            private final HBox buttons = new HBox(5, viewBtn, presentBtn, absentBtn, deleteBtn);

            {
                viewBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    showParticipantDetails(p);
                });

                presentBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    updateStatus(p, "present");
                });

                absentBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    updateStatus(p, "absent");
                });

                deleteBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    deleteParticipation(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        participantsTable.getColumns().addAll(
                idCol, participantCol, emailCol, contactCol, ageCol,
                statutCol, dateCol, actionsCol
        );

        participantsTable.setTooltip(new Tooltip("Double-cliquez sur un participant pour voir ses détails"));

        participantsTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && participantsTable.getSelectionModel().getSelectedItem() != null) {
                showParticipantDetails(participantsTable.getSelectionModel().getSelectedItem());
            }
        });

        VBox.setVgrow(participantsTable, Priority.ALWAYS);
        box.getChildren().addAll(header, participantsTable);

        return box;
    }

    private Button createIconButton(String icon, String color, String tooltip) {
        Button btn = new Button(icon);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-min-width: 35px; -fx-min-height: 35px; " +
                "-fx-background-radius: 4; -fx-cursor: hand;");
        btn.setTooltip(new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-min-width: 35px; -fx-min-height: 35px; " +
                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-min-width: 35px; -fx-min-height: 35px; " +
                    "-fx-background-radius: 4; -fx-cursor: hand;");
        });

        return btn;
    }

    private String darkenColor(String color) {
        switch (color) {
            case "#3b82f6": return "#2563eb";
            case SUCCESS_COLOR: return "#059669";
            case DANGER_COLOR: return "#dc2626";
            case "#6b7280": return "#4b5563";
            default: return color;
        }
    }

    private HBox createBottomToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(15));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        Button markAllPresentBtn = new Button("✅ Tout marquer présent");
        markAllPresentBtn.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        markAllPresentBtn.setTooltip(new Tooltip("Marquer tous les participants affichés comme présents"));
        markAllPresentBtn.setOnAction(e -> markAllAsPresent());

        Button markAllAbsentBtn = new Button("❌ Tout marquer absent");
        markAllAbsentBtn.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        markAllAbsentBtn.setTooltip(new Tooltip("Marquer tous les participants affichés comme absents"));
        markAllAbsentBtn.setOnAction(e -> markAllAsAbsent());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(markAllPresentBtn, markAllAbsentBtn, spacer);

        return toolbar;
    }

    private void loadData() {
        List<Event> events = eventService.getEventsByOrganisateur(currentUser.getId());
        eventsData.setAll(events);

        Event allEventsOption = new Event();
        allEventsOption.setId_evenement(-1);
        allEventsOption.setTitre("Tous les participants");

        ObservableList<Event> comboItems = FXCollections.observableArrayList();
        comboItems.add(allEventsOption);
        comboItems.addAll(eventsData);
        eventSelector.setItems(comboItems);
        eventSelector.setValue(allEventsOption);

        allParticipations.clear();
        for (Event event : events) {
            List<Participation> participants = participationService.getParticipationsByEvent(event.getId_evenement());
            allParticipations.addAll(participants);
        }

        filteredParticipations = new FilteredList<>(allParticipations, p -> true);
        sortedParticipations = new SortedList<>(filteredParticipations);
        sortedParticipations.comparatorProperty().bind(participantsTable.comparatorProperty());
        participantsTable.setItems(sortedParticipations);

        updateGlobalStats();
        updateParticipantCount();
    }

    private void refreshData() {
        loadData();
        clearFilters();
        if (currentSelectedEvent != null) {
            selectEvent(currentSelectedEvent);
        } else {
            clearEventSelection();
        }
    }

    private void applyFilters() {
        if (filteredParticipations == null) return;

        String searchText = searchField.getText().toLowerCase().trim();
        String statusValue = statusFilter.getValue();

        filteredParticipations.setPredicate(participation -> {
            if (statusValue != null && !"Tous".equals(statusValue)) {
                String expectedStatus = statusValue.equals("Inscrits") ? "inscrit" :
                        statusValue.equals("Présents") ? "present" : "absent";
                if (!participation.getStatut().equalsIgnoreCase(expectedStatus)) {
                    return false;
                }
            }

            if (!searchText.isEmpty()) {
                User user = userService.getUserById(participation.getIdUser());
                if (user == null) return false;

                boolean matches = user.getPrenom().toLowerCase().contains(searchText) ||
                        user.getNom().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText) ||
                        (participation.getContact() != null && participation.getContact().toLowerCase().contains(searchText));

                if (!matches) return false;
            }

            return true;
        });

        updateParticipantCount();
    }

    private void clearFilters() {
        searchField.clear();
        statusFilter.setValue("Tous");
        applyFilters();
    }

    private void selectEvent(Event event) {
        currentSelectedEvent = event;

        List<Participation> eventParticipations = allParticipations.stream()
                .filter(p -> p.getIdEvenement() == event.getId_evenement())
                .collect(Collectors.toList());

        filteredParticipations = new FilteredList<>(FXCollections.observableArrayList(eventParticipations), p -> true);
        sortedParticipations = new SortedList<>(filteredParticipations);
        sortedParticipations.comparatorProperty().bind(participantsTable.comparatorProperty());
        participantsTable.setItems(sortedParticipations);

        eventInfoPanel.setVisible(true);
        eventInfoPanel.setManaged(true);

        updateEventInfo(event);
        updateEventStats(eventParticipations);
        updateParticipantCount();

        applyFilters();
    }

    private void clearEventSelection() {
        currentSelectedEvent = null;

        filteredParticipations = new FilteredList<>(allParticipations, p -> true);
        sortedParticipations = new SortedList<>(filteredParticipations);
        sortedParticipations.comparatorProperty().bind(participantsTable.comparatorProperty());
        participantsTable.setItems(sortedParticipations);

        eventInfoPanel.setVisible(false);
        eventInfoPanel.setManaged(false);
        updateParticipantCount();
        applyFilters();
    }

    private void updateEventInfo(Event event) {
        eventTitreValue.setText(event.getTitre());
        eventDateValue.setText(event.getFormattedDate());
        eventLieuValue.setText(event.getLieu() != null ? event.getLieu() : "Non spécifié");
        eventCapaciteValue.setText(event.getCapacite_max() != null ?
                event.getCapacite_max() + " places" : "Illimitée");

        String statut = event.getStatut();
        eventStatutValue.setText(statut.substring(0, 1).toUpperCase() + statut.substring(1));

        String statutColor;
        switch (statut.toLowerCase()) {
            case "à venir": statutColor = WARNING_COLOR; break;
            case "en cours": statutColor = "#9b59b6"; break;
            case "passé": statutColor = "#6c757d"; break;
            default: statutColor = "#6c757d";
        }
        eventStatutValue.setTextFill(Color.web(statutColor));
        eventStatutValue.setStyle("-fx-font-weight: bold;");
    }

    private void updateEventStats(List<Participation> participants) {
        long inscrits = participants.stream().filter(p -> "inscrit".equals(p.getStatut())).count();
        long presents = participants.stream().filter(p -> "present".equals(p.getStatut())).count();
        long absents = participants.stream().filter(p -> "absent".equals(p.getStatut())).count();
        long total = participants.size();

        eventInscritsValue.setText(String.valueOf(inscrits));
        eventPresentsValue.setText(String.valueOf(presents));
        eventAbsentsValue.setText(String.valueOf(absents));

        if (total > 0) {
            double tauxPresence = (presents * 100.0) / total;
            eventTauxPresenceValue.setText(String.format("%.1f%%", tauxPresence));
        } else {
            eventTauxPresenceValue.setText("0%");
        }

        if (currentSelectedEvent != null && currentSelectedEvent.getCapacite_max() != null &&
                currentSelectedEvent.getCapacite_max() > 0) {
            double tauxRemplissage = (total * 100.0) / currentSelectedEvent.getCapacite_max();
            eventCompletionValue.setText(String.format("%.1f%%", tauxRemplissage));

            ProgressBar progressBar = (ProgressBar) eventInfoPanel.getChildren().get(2);
            progressBar.setProgress(Math.min(tauxRemplissage / 100, 1.0));
        } else {
            eventCompletionValue.setText("-");
            ProgressBar progressBar = (ProgressBar) eventInfoPanel.getChildren().get(2);
            progressBar.setProgress(0);
        }
    }

    private void updateGlobalStats() {
        long total = allParticipations.size();
        long inscrits = allParticipations.stream().filter(p -> "inscrit".equals(p.getStatut())).count();
        long presents = allParticipations.stream().filter(p -> "present".equals(p.getStatut())).count();
        long absents = allParticipations.stream().filter(p -> "absent".equals(p.getStatut())).count();

        totalParticipantsLabel.setText(String.valueOf(total));
        totalInscritsLabel.setText(String.valueOf(inscrits));
        totalPresentsLabel.setText(String.valueOf(presents));
        totalAbsentsLabel.setText(String.valueOf(absents));
    }

    private void updateParticipantCount() {
        if (sortedParticipations != null) {
            int count = sortedParticipations.size();
            eventParticipantsCount.setText(count + " participant" + (count > 1 ? "s" : ""));
        }
    }

    private void updateStatus(Participation participation, String newStatus) {
        String statusText = newStatus.equals("present") ? "PRÉSENT" : "ABSENT";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Changement de statut");
        confirm.setContentText("Voulez-vous marquer ce participant comme " + statusText + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = participationService.updateStatut(
                    participation.getIdEvenement(),
                    participation.getIdUser(),
                    newStatus
            );

            if (success) {
                // Notification au participant
                notificationService.creerNotificationModification(
                        participation.getIdUser(),
                        participation.getIdEvenement(),
                        eventService.getEventById(participation.getIdEvenement()).getTitre(),
                        "Votre statut a été mis à jour: " + statusText
                );
                refreshData();
                showSuccess("Statut mis à jour avec succès !");
            } else {
                showError("Échec de la mise à jour du statut");
            }
        }
    }

    private void deleteParticipation(Participation participation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la participation");
        confirm.setContentText("Voulez-vous vraiment supprimer cette participation ?\nCette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Event event = eventService.getEventById(participation.getIdEvenement());
            User participant = userService.getUserById(participation.getIdUser());

            boolean success = participationService.annulerParticipation(
                    participation.getIdEvenement(),
                    participation.getIdUser()
            );

            if (success) {
                // Notification au participant
                notificationService.creerNotificationAnnulation(
                        participation.getIdUser(),
                        participation.getIdEvenement(),
                        event.getTitre()
                );

                // Notification à l'admin
                List<User> admins = userService.getUsersByRole("admin");
                for (User admin : admins) {
                    notificationService.creerNotificationAnnulation(
                            admin.getId(),
                            participation.getIdEvenement(),
                            event.getTitre() + " - Participant supprimé: " + participant.getPrenom() + " " + participant.getNom()
                    );
                }

                refreshData();
                showSuccess("Participation supprimée avec succès !");
            } else {
                showError("Échec de la suppression");
            }
        }
    }

    private void markAllAsPresent() {
        if (filteredParticipations == null || filteredParticipations.isEmpty()) {
            showWarning("Aucun participant à marquer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Action groupée");
        confirm.setContentText("Voulez-vous marquer tous les participants affichés comme PRÉSENTS ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int success = 0;
            int total = 0;

            for (Participation p : filteredParticipations) {
                if (!"present".equals(p.getStatut())) {
                    total++;
                    if (participationService.updateStatut(p.getIdEvenement(), p.getIdUser(), "present")) {
                        success++;
                    }
                }
            }

            if (success > 0) {
                refreshData();
                showSuccess(success + " participant(s) marqué(s) comme présent(s) sur " + total);
            }
        }
    }

    private void markAllAsAbsent() {
        if (filteredParticipations == null || filteredParticipations.isEmpty()) {
            showWarning("Aucun participant à marquer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Action groupée");
        confirm.setContentText("Voulez-vous marquer tous les participants affichés comme ABSENTS ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int success = 0;
            int total = 0;

            for (Participation p : filteredParticipations) {
                if (!"absent".equals(p.getStatut())) {
                    total++;
                    if (participationService.updateStatut(p.getIdEvenement(), p.getIdUser(), "absent")) {
                        success++;
                    }
                }
            }

            if (success > 0) {
                refreshData();
                showSuccess(success + " participant(s) marqué(s) comme absent(s) sur " + total);
            }
        }
    }

    private void showParticipantDetails(Participation participation) {
        User participant = userService.getUserById(participation.getIdUser());
        Event event = eventService.getEventById(participation.getIdEvenement());

        if (participant == null || event == null) {
            showError("Impossible de charger les détails du participant");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détails du participant - " + participant.getPrenom() + " " + participant.getNom());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(550);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");

        Label avatarIcon = new Label("👤");
        avatarIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label("Fiche détaillée du participant");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));
        titleLabel.setWrapText(true);

        Label nameLabel = new Label(participant.getPrenom() + " " + participant.getNom());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web(DARK_COLOR));
        nameLabel.setWrapText(true);

        headerText.getChildren().addAll(titleLabel, nameLabel);
        header.getChildren().addAll(avatarIcon, headerText);

        VBox personalSection = createDetailSection("📋 Informations personnelles");
        GridPane personalGrid = createDetailGrid();
        addDetailRow(personalGrid, "📧 Email :", participant.getEmail(), 0);
        addDetailRow(personalGrid, "🎂 Âge :", participation.getAge() != null ? participation.getAge() + " ans" : "Non spécifié", 1);
        addDetailRow(personalGrid, "⚥ Genre :", getGenderString(participant), 2);
        personalSection.getChildren().add(personalGrid);

        VBox participationSection = createDetailSection("🎟️ Détails de participation");
        GridPane participationGrid = createDetailGrid();
        addDetailRow(participationGrid, "📞 Contact :", participation.getContact(), 0);

        Label statutLabel = new Label("📊 Statut :");
        statutLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        statutLabel.setWrapText(true);
        HBox statutBox = new HBox(10);
        Label statutBadge = new Label(participation.getStatut().toUpperCase());
        statutBadge.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statutBadge.setTextFill(Color.WHITE);
        statutBadge.setPadding(new Insets(4, 15, 4, 15));
        statutBadge.setStyle("-fx-background-color: " + getStatusColor(participation.getStatut()) + "; -fx-background-radius: 15;");
        statutBox.getChildren().add(statutBadge);

        participationGrid.add(statutLabel, 0, 1);
        participationGrid.add(statutBox, 1, 1);

        addDetailRow(participationGrid, "📅 Date d'inscription :", participation.getFormattedDate(), 2);
        participationSection.getChildren().add(participationGrid);

        VBox eventSection = createDetailSection("📅 Événement associé");
        GridPane eventGrid = createDetailGrid();
        addDetailRow(eventGrid, "🎯 Titre :", event.getTitre(), 0);
        addDetailRow(eventGrid, "📆 Date :", event.getFormattedDate(), 1);
        addDetailRow(eventGrid, "📍 Lieu :", event.getLieu() != null ? event.getLieu() : "Non spécifié", 2);

        User organizer = userService.getUserById(event.getId_organisateur());
        String organizerName = organizer != null ? organizer.getPrenom() + " " + organizer.getNom() : "Inconnu";
        addDetailRow(eventGrid, "👤 Organisateur :", organizerName, 3);

        eventSection.getChildren().add(eventGrid);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));

        Button presentBtn = createDetailButton("✅ Marquer présent", SUCCESS_COLOR);
        presentBtn.setOnAction(e -> {
            updateStatus(participation, "present");
            dialog.close();
        });

        Button absentBtn = createDetailButton("❌ Marquer absent", DANGER_COLOR);
        absentBtn.setOnAction(e -> {
            updateStatus(participation, "absent");
            dialog.close();
        });

        Button exportBtn = createDetailButton("📥 Exporter", "#3b82f6");
        exportBtn.setOnAction(e -> {
            exportSingleParticipant(participation);
            dialog.close();
        });

        Button closeBtn = createDetailButton("Fermer", BORDER_COLOR);
        closeBtn.setTextFill(Color.web(DARK_COLOR));
        closeBtn.setOnAction(e -> dialog.close());

        buttons.getChildren().addAll(presentBtn, absentBtn, exportBtn, closeBtn);

        content.getChildren().addAll(header, personalSection, participationSection, eventSection, buttons);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Scene scene = new Scene(scrollPane, 600, 750);
        dialog.setScene(scene);
        dialog.show();
    }

    private VBox createDetailSection(String title) {
        VBox section = new VBox(8);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 8; " +
                "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(DARK_COLOR));
        titleLabel.setWrapText(true);

        section.getChildren().add(titleLabel);
        return section;
    }

    private GridPane createDetailGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 5, 0));
        return grid;
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label labelField = new Label(label);
        labelField.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_COLOR + ";");
        labelField.setWrapText(true);
        Label valueField = new Label(value);
        valueField.setStyle("-fx-text-fill: " + DARK_COLOR + ";");
        valueField.setWrapText(true);
        grid.add(labelField, 0, row);
        grid.add(valueField, 1, row);
    }

    private Button createDetailButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        return btn;
    }

    private void showDetailedStats() {
        Stage statsStage = new Stage();
        statsStage.initModality(Modality.APPLICATION_MODAL);
        statsStage.setTitle("Rapport statistique - Participants");

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(750);
        content.setMaxWidth(800);

        Label title = new Label("📊 RAPPORT STATISTIQUE DÉTAILLÉ");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);

        Label dateLabel = new Label("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setTextFill(Color.web(DARK_COLOR));
        dateLabel.setWrapText(true);
        dateLabel.setAlignment(Pos.CENTER);

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(10, 0, 10, 0));

        VBox globalSection = new VBox(15);
        globalSection.setPadding(new Insets(15));
        globalSection.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12;");

        Label globalTitle = new Label("📈 STATISTIQUES GLOBALES");
        globalTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        globalTitle.setTextFill(Color.web(PRIMARY_COLOR));
        globalTitle.setWrapText(true);

        GridPane globalGrid = new GridPane();
        globalGrid.setHgap(30);
        globalGrid.setVgap(15);
        globalGrid.setPadding(new Insets(10, 0, 5, 0));

        Label totalLabel = new Label("👥 Total participants :");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        totalLabel.setTextFill(Color.web(DARK_COLOR));
        totalLabel.setWrapText(true);
        Label totalValue = new Label(totalParticipantsLabel.getText());
        totalValue.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        totalValue.setTextFill(Color.web(PRIMARY_COLOR));
        totalValue.setWrapText(true);
        globalGrid.add(totalLabel, 0, 0);
        globalGrid.add(totalValue, 1, 0);

        Label inscritTotalLabel = new Label("📝 Inscrits :");
        inscritTotalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        inscritTotalLabel.setTextFill(Color.web(DARK_COLOR));
        inscritTotalLabel.setWrapText(true);
        Label inscritTotalValue = new Label(totalInscritsLabel.getText());
        inscritTotalValue.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        inscritTotalValue.setTextFill(Color.web("#3b82f6"));
        inscritTotalValue.setWrapText(true);
        globalGrid.add(inscritTotalLabel, 0, 1);
        globalGrid.add(inscritTotalValue, 1, 1);

        Label presentTotalLabel = new Label("✅ Présents :");
        presentTotalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        presentTotalLabel.setTextFill(Color.web(DARK_COLOR));
        presentTotalLabel.setWrapText(true);
        Label presentTotalValue = new Label(totalPresentsLabel.getText());
        presentTotalValue.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        presentTotalValue.setTextFill(Color.web(SUCCESS_COLOR));
        presentTotalValue.setWrapText(true);
        globalGrid.add(presentTotalLabel, 0, 2);
        globalGrid.add(presentTotalValue, 1, 2);

        Label absentTotalLabel = new Label("❌ Absents :");
        absentTotalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        absentTotalLabel.setTextFill(Color.web(DARK_COLOR));
        absentTotalLabel.setWrapText(true);
        Label absentTotalValue = new Label(totalAbsentsLabel.getText());
        absentTotalValue.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        absentTotalValue.setTextFill(Color.web(DANGER_COLOR));
        absentTotalValue.setWrapText(true);
        globalGrid.add(absentTotalLabel, 0, 3);
        globalGrid.add(absentTotalValue, 1, 3);

        int totalGlobal = Integer.parseInt(totalParticipantsLabel.getText());
        int presentsGlobal = Integer.parseInt(totalPresentsLabel.getText());
        double tauxPresenceGlobal = totalGlobal > 0 ? (presentsGlobal * 100.0 / totalGlobal) : 0;

        Label tauxGlobalLabel = new Label("📊 Taux de présence global :");
        tauxGlobalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tauxGlobalLabel.setTextFill(Color.web(DARK_COLOR));
        tauxGlobalLabel.setWrapText(true);
        Label tauxGlobalValue = new Label(String.format("%.1f%%", tauxPresenceGlobal));
        tauxGlobalValue.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tauxGlobalValue.setTextFill(Color.web(WARNING_COLOR));
        tauxGlobalValue.setWrapText(true);
        globalGrid.add(tauxGlobalLabel, 0, 4);
        globalGrid.add(tauxGlobalValue, 1, 4);

        globalSection.getChildren().addAll(globalTitle, globalGrid);

        VBox eventSection = new VBox(15);
        eventSection.setPadding(new Insets(15));
        eventSection.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12;");

        Label eventSectionTitle = new Label("📋 STATISTIQUES PAR ÉVÉNEMENT");
        eventSectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        eventSectionTitle.setTextFill(Color.web(SUCCESS_COLOR));
        eventSectionTitle.setWrapText(true);

        VBox eventsContainer = new VBox(15);
        eventsContainer.setPadding(new Insets(10, 0, 5, 0));

        if (eventsData.isEmpty()) {
            Label noEventLabel = new Label("Aucun événement trouvé");
            noEventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            noEventLabel.setTextFill(Color.web(DARK_COLOR));
            noEventLabel.setWrapText(true);
            eventsContainer.getChildren().add(noEventLabel);
        } else {
            for (Event event : eventsData) {
                List<Participation> eventParticipants = participationService.getParticipationsByEvent(event.getId_evenement());
                long inscrits = eventParticipants.stream().filter(p -> "inscrit".equals(p.getStatut())).count();
                long presents = eventParticipants.stream().filter(p -> "present".equals(p.getStatut())).count();
                long absents = eventParticipants.stream().filter(p -> "absent".equals(p.getStatut())).count();
                long total = eventParticipants.size();

                VBox eventCard = new VBox(10);
                eventCard.setPadding(new Insets(15));
                eventCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8;");

                Label eventName = new Label("🎯 " + event.getTitre());
                eventName.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                eventName.setTextFill(Color.web(PRIMARY_COLOR));
                eventName.setWrapText(true);

                Label eventInfo = new Label(event.getFormattedDate() + " - " + (event.getLieu() != null ? event.getLieu() : "Lieu non spécifié"));
                eventInfo.setFont(Font.font("Arial", 13));
                eventInfo.setTextFill(Color.web("#64748b"));
                eventInfo.setWrapText(true);

                GridPane eventStatGrid = new GridPane();
                eventStatGrid.setHgap(25);
                eventStatGrid.setVgap(10);
                eventStatGrid.setPadding(new Insets(10, 0, 5, 0));

                Label totalEventLabel = new Label("Total :");
                totalEventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                totalEventLabel.setTextFill(Color.web(DARK_COLOR));
                totalEventLabel.setWrapText(true);
                Label totalEventValue = new Label(String.valueOf(total));
                totalEventValue.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                totalEventValue.setTextFill(Color.web(PRIMARY_COLOR));
                totalEventValue.setWrapText(true);
                eventStatGrid.add(totalEventLabel, 0, 0);
                eventStatGrid.add(totalEventValue, 1, 0);

                Label inscritEventLabel = new Label("Inscrits :");
                inscritEventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                inscritEventLabel.setTextFill(Color.web(DARK_COLOR));
                inscritEventLabel.setWrapText(true);
                Label inscritEventValue = new Label(String.valueOf(inscrits));
                inscritEventValue.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                inscritEventValue.setTextFill(Color.web("#3b82f6"));
                inscritEventValue.setWrapText(true);
                eventStatGrid.add(inscritEventLabel, 2, 0);
                eventStatGrid.add(inscritEventValue, 3, 0);

                Label presentEventLabel = new Label("Présents :");
                presentEventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                presentEventLabel.setTextFill(Color.web(DARK_COLOR));
                presentEventLabel.setWrapText(true);
                Label presentEventValue = new Label(String.valueOf(presents));
                presentEventValue.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                presentEventValue.setTextFill(Color.web(SUCCESS_COLOR));
                presentEventValue.setWrapText(true);
                eventStatGrid.add(presentEventLabel, 0, 1);
                eventStatGrid.add(presentEventValue, 1, 1);

                Label absentEventLabel = new Label("Absents :");
                absentEventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                absentEventLabel.setTextFill(Color.web(DARK_COLOR));
                absentEventLabel.setWrapText(true);
                Label absentEventValue = new Label(String.valueOf(absents));
                absentEventValue.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                absentEventValue.setTextFill(Color.web(DANGER_COLOR));
                absentEventValue.setWrapText(true);
                eventStatGrid.add(absentEventLabel, 2, 1);
                eventStatGrid.add(absentEventValue, 3, 1);

                double tauxPresenceEvent = total > 0 ? (presents * 100.0 / total) : 0;
                Label tauxEventLabel = new Label("Taux présence :");
                tauxEventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                tauxEventLabel.setTextFill(Color.web(DARK_COLOR));
                tauxEventLabel.setWrapText(true);
                Label tauxEventValue = new Label(String.format("%.1f%%", tauxPresenceEvent));
                tauxEventValue.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                tauxEventValue.setTextFill(Color.web(WARNING_COLOR));
                tauxEventValue.setWrapText(true);
                eventStatGrid.add(tauxEventLabel, 0, 2);
                eventStatGrid.add(tauxEventValue, 1, 2);

                eventCard.getChildren().addAll(eventName, eventInfo, eventStatGrid);
                eventsContainer.getChildren().add(eventCard);
            }
        }

        eventSection.getChildren().addAll(eventSectionTitle, eventsContainer);

        VBox repartitionSection = new VBox(15);
        repartitionSection.setPadding(new Insets(15));
        repartitionSection.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12;");

        Label repartitionTitle = new Label("🔄 RÉPARTITION PAR STATUT");
        repartitionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        repartitionTitle.setTextFill(Color.web(WARNING_COLOR));
        repartitionTitle.setWrapText(true);

        GridPane repartitionGrid = new GridPane();
        repartitionGrid.setHgap(30);
        repartitionGrid.setVgap(15);
        repartitionGrid.setPadding(new Insets(10, 0, 5, 0));

        int total = Integer.parseInt(totalParticipantsLabel.getText());
        int inscrits = Integer.parseInt(totalInscritsLabel.getText());
        int presents = Integer.parseInt(totalPresentsLabel.getText());
        int absents = Integer.parseInt(totalAbsentsLabel.getText());

        double pInscrits = total > 0 ? (inscrits * 100.0 / total) : 0;
        double pPresents = total > 0 ? (presents * 100.0 / total) : 0;
        double pAbsents = total > 0 ? (absents * 100.0 / total) : 0;

        Label repInscritLabel = new Label("📝 Inscrits :");
        repInscritLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        repInscritLabel.setTextFill(Color.web(DARK_COLOR));
        repInscritLabel.setWrapText(true);
        Label repInscritValue = new Label(inscrits + " (" + String.format("%.1f", pInscrits) + "%)");
        repInscritValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        repInscritValue.setTextFill(Color.web("#3b82f6"));
        repInscritValue.setWrapText(true);
        repartitionGrid.add(repInscritLabel, 0, 0);
        repartitionGrid.add(repInscritValue, 1, 0);

        Label repPresentLabel = new Label("✅ Présents :");
        repPresentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        repPresentLabel.setTextFill(Color.web(DARK_COLOR));
        repPresentLabel.setWrapText(true);
        Label repPresentValue = new Label(presents + " (" + String.format("%.1f", pPresents) + "%)");
        repPresentValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        repPresentValue.setTextFill(Color.web(SUCCESS_COLOR));
        repPresentValue.setWrapText(true);
        repartitionGrid.add(repPresentLabel, 0, 1);
        repartitionGrid.add(repPresentValue, 1, 1);

        Label repAbsentLabel = new Label("❌ Absents :");
        repAbsentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        repAbsentLabel.setTextFill(Color.web(DARK_COLOR));
        repAbsentLabel.setWrapText(true);
        Label repAbsentValue = new Label(absents + " (" + String.format("%.1f", pAbsents) + "%)");
        repAbsentValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        repAbsentValue.setTextFill(Color.web(DANGER_COLOR));
        repAbsentValue.setWrapText(true);
        repartitionGrid.add(repAbsentLabel, 0, 2);
        repartitionGrid.add(repAbsentValue, 1, 2);

        repartitionSection.getChildren().addAll(repartitionTitle, repartitionGrid);

        Button closeBtn = new Button("Fermer le rapport");
        closeBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> statsStage.close());

        VBox buttonBox = new VBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        content.getChildren().addAll(
                title,
                dateLabel,
                sep1,
                globalSection,
                eventSection,
                repartitionSection,
                buttonBox
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 800, 800);
        statsStage.setScene(scene);
        statsStage.show();
    }

    private void exportAllParticipants() {
        if (allParticipations.isEmpty()) {
            showWarning("Aucun participant à exporter");
            return;
        }

        String timestamp = LocalDateTime.now().format(EXPORT_FILE_DATE_FORMAT);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter tous les participants");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        fileChooser.setInitialFileName("participants_complet_" + timestamp + ".csv");

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {

            writer.println("#RAPPORT DES PARTICIPANTS");
            writer.println("#Date d'export: " + LocalDateTime.now().format(EXPORT_DATETIME_FORMAT));
            writer.println("#Exporté par: " + currentUser.getPrenom() + " " + currentUser.getNom());
            writer.println("#Total participants: " + allParticipations.size());
            writer.println("#");
            writer.println("ID_Participant;Nom;Prénom;Email;Contact;Âge;Statut;Nom_Événement;Date_Événement;Lieu_Événement;Date_Inscription");

            for (Participation p : allParticipations) {
                User user = userService.getUserById(p.getIdUser());
                Event event = eventService.getEventById(p.getIdEvenement());
                if (user == null || event == null) continue;

                writer.println(String.format("%d;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s",
                        p.getId(),
                        escapeCSV(user.getNom()),
                        escapeCSV(user.getPrenom()),
                        escapeCSV(user.getEmail()),
                        escapeCSV(p.getContact() != null ? p.getContact() : ""),
                        p.getAge() != null ? p.getAge() : "",
                        p.getStatut(),
                        escapeCSV(event.getTitre()),
                        event.getDate_evenement() != null ? event.getDate_evenement().format(EXPORT_DATE_FORMAT) : "",
                        escapeCSV(event.getLieu() != null ? event.getLieu() : ""),
                        p.getDateInscription() != null ? p.getDateInscription().toLocalDateTime().format(EXPORT_DATETIME_FORMAT) : ""
                ));
            }

            showSuccess("Export réussi : " + file.getName() + "\n" +
                    allParticipations.size() + " participants exportés");

        } catch (Exception e) {
            showError("Échec de l'export: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportSingleParticipant(Participation p) {
        User user = userService.getUserById(p.getIdUser());
        Event event = eventService.getEventById(p.getIdEvenement());
        if (user == null || event == null) return;

        String timestamp = LocalDateTime.now().format(EXPORT_FILE_DATE_FORMAT);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter participant");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        fileChooser.setInitialFileName("participant_" + user.getNom() + "_" + user.getPrenom() + "_" + timestamp + ".csv");

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {

            writer.println("#FICHE INDIVIDUELLE DU PARTICIPANT");
            writer.println("#Date d'export: " + LocalDateTime.now().format(EXPORT_DATETIME_FORMAT));
            writer.println("#Exporté par: " + currentUser.getPrenom() + " " + currentUser.getNom());
            writer.println("#");
            writer.println("Catégorie;Information");
            writer.println("ID Participant;" + p.getId());
            writer.println("Nom;" + escapeCSV(user.getNom()));
            writer.println("Prénom;" + escapeCSV(user.getPrenom()));
            writer.println("Nom complet;" + escapeCSV(user.getPrenom() + " " + user.getNom()));
            writer.println("Email;" + escapeCSV(user.getEmail()));
            writer.println("Âge;" + (p.getAge() != null ? p.getAge() : ""));
            writer.println("Genre;" + getGenderString(user));
            writer.println("");
            writer.println("Contact;" + escapeCSV(p.getContact() != null ? p.getContact() : ""));
            writer.println("Statut;" + p.getStatut());
            writer.println("Date d'inscription;" + (p.getDateInscription() != null ?
                    p.getDateInscription().toLocalDateTime().format(EXPORT_DATETIME_FORMAT) : ""));
            writer.println("");
            writer.println("Événement;" + escapeCSV(event.getTitre()));
            writer.println("Date événement;" + (event.getDate_evenement() != null ?
                    event.getDate_evenement().format(EXPORT_DATETIME_FORMAT) : ""));
            writer.println("Lieu;" + escapeCSV(event.getLieu() != null ? event.getLieu() : ""));

            User organizer = userService.getUserById(event.getId_organisateur());
            if (organizer != null) {
                writer.println("Organisateur;" + escapeCSV(organizer.getPrenom() + " " + organizer.getNom()));
                writer.println("Email organisateur;" + escapeCSV(organizer.getEmail()));
            }

            showSuccess("Export réussi : " + file.getName());

        } catch (Exception e) {
            showError("Échec de l'export: " + e.getMessage());
        }
    }

    private String escapeCSV(String value) {
        if (value == null || value.isEmpty()) return "";

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(";") || escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String getGenderString(User user) {
        if (user.getIdGenre() == 1) return "Homme";
        if (user.getIdGenre() == 2) return "Femme";
        return "Non spécifié";
    }

    private String getStatusColor(String statut) {
        switch (statut.toLowerCase()) {
            case "inscrit": return "#3b82f6";
            case "present": return SUCCESS_COLOR;
            case "absent": return DANGER_COLOR;
            default: return "#94a3b8";
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("✅ " + message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText("❌ " + message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText("⚠️ " + message);
        alert.showAndWait();
    }
}