package edu.Loopi.view;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.Participation;
import edu.Loopi.entities.User;
import edu.Loopi.services.EventService;
import edu.Loopi.services.ParticipationService;
import edu.Loopi.services.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ParticipantsView {
    private User currentUser;
    private EventService eventService = new EventService();
    private ParticipationService participationService = new ParticipationService();
    private UserService userService = new UserService();

    private ComboBox<Event> eventSelector;
    private TableView<Participation> participantsTable;
    private Label totalParticipantsLabel;
    private Label inscritsLabel;
    private Label presentsLabel;
    private Label absentsLabel;
    private Label eventNameLabel;
    private Label eventDateLabel;
    private Label eventLocationLabel;
    private Label eventCapacityLabel;
    private VBox eventDetailsBox;
    private VBox participantDetailsBox;

    public ParticipantsView(User user) {
        this.currentUser = user;
    }

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8fafc;");

        // HEADER
        HBox header = createHeader();

        // STATISTIQUES GLOBALES
        HBox globalStatsBox = createGlobalStatistics();

        // SÉLECTEUR D'ÉVÉNEMENT
        VBox selectorBox = createEventSelector();

        // DÉTAILS DE L'ÉVÉNEMENT SÉLECTIONNÉ
        eventDetailsBox = createEventDetails();
        eventDetailsBox.setVisible(false);
        eventDetailsBox.setManaged(false);

        // STATISTIQUES DE L'ÉVÉNEMENT
        HBox eventStatsBox = createEventStatistics();
        eventStatsBox.setVisible(false);
        eventStatsBox.setManaged(false);

        // TABLEAU DES PARTICIPANTS
        VBox tableBox = createParticipantsTable();

        // DÉTAILS DU PARTICIPANT SÉLECTIONNÉ
        participantDetailsBox = createParticipantDetails();
        participantDetailsBox.setVisible(false);
        participantDetailsBox.setManaged(false);

        container.getChildren().addAll(
                header,
                globalStatsBox,
                selectorBox,
                eventDetailsBox,
                eventStatsBox,
                tableBox,
                participantDetailsBox
        );

        // Charger les événements de l'organisateur
        loadEvents();

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;");

        VBox headerText = new VBox(5);

        Label title = new Label("👥 Gestion Complète des Participants");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#0f172a"));

        Label subtitle = new Label("Visualisez, gérez et analysez tous les participants à vos événements");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#475569"));

        headerText.getChildren().addAll(title, subtitle);

        // Badge du nombre total d'événements
        int totalEvents = eventService.countEventsByOrganisateur(currentUser.getId());
        Label eventsCount = new Label(totalEvents + " Événement(s)");
        eventsCount.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventsCount.setTextFill(Color.web("#2196F3"));
        eventsCount.setPadding(new Insets(10, 25, 10, 25));
        eventsCount.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 30;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(headerText, spacer, eventsCount);

        return header;
    }

    private HBox createGlobalStatistics() {
        HBox box = new HBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        box.setAlignment(Pos.CENTER);

        totalParticipantsLabel = new Label("0");
        VBox totalCard = createStatCard("👥 Total Participants", totalParticipantsLabel, "#2196F3",
                "Tous événements confondus");

        inscritsLabel = new Label("0");
        VBox inscritsCard = createStatCard("📝 Inscrits", inscritsLabel, "#3b82f6",
                "En attente de confirmation");

        presentsLabel = new Label("0");
        VBox presentsCard = createStatCard("✅ Présents", presentsLabel, "#10b981",
                "Ont participé à l'événement");

        absentsLabel = new Label("0");
        VBox absentsCard = createStatCard("❌ Absents", absentsLabel, "#ef4444",
                "Ne se sont pas présentés");

        box.getChildren().addAll(totalCard, inscritsCard, presentsCard, absentsCard);

        return box;
    }

    private VBox createStatCard(String title, Label valueLabel, String color, String description) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 4 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(220);

        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web("#0f172a"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 11));
        descLabel.setTextFill(Color.web("#64748b"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(valueLabel, titleLabel, descLabel);
        return card;
    }

    private VBox createEventSelector() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label selectorTitle = new Label("📋 Sélectionner un événement");
        selectorTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        selectorTitle.setTextFill(Color.web("#0f172a"));

        HBox selectorContent = new HBox(20);
        selectorContent.setAlignment(Pos.CENTER_LEFT);

        Label selectorLabel = new Label("Événement :");
        selectorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        selectorLabel.setTextFill(Color.web("#0f172a"));

        eventSelector = new ComboBox<>();
        eventSelector.setPrefWidth(500);
        eventSelector.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 15; " +
                "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-font-size: 14px;");
        eventSelector.setPromptText("Choisissez un événement...");
        eventSelector.setOnAction(e -> {
            Event selected = eventSelector.getValue();
            if (selected != null) {
                loadEventDetails(selected);
                loadParticipantsForEvent(selected);
                eventDetailsBox.setVisible(true);
                eventDetailsBox.setManaged(true);
            }
        });

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            loadEvents();
            if (eventSelector.getValue() != null) {
                loadEventDetails(eventSelector.getValue());
                loadParticipantsForEvent(eventSelector.getValue());
            }
        });

        Button clearBtn = new Button("✕ Effacer");
        clearBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> {
            eventSelector.setValue(null);
            participantsTable.setItems(FXCollections.observableArrayList());
            eventDetailsBox.setVisible(false);
            eventDetailsBox.setManaged(false);
            participantDetailsBox.setVisible(false);
            participantDetailsBox.setManaged(false);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        selectorContent.getChildren().addAll(selectorLabel, eventSelector, spacer, refreshBtn, clearBtn);
        box.getChildren().addAll(selectorTitle, selectorContent);

        return box;
    }

    private VBox createEventDetails() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("📌 Détails de l'événement");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#0f172a"));

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(10, 0, 5, 0));

        // Ligne 0: Titre de l'événement
        eventNameLabel = new Label();
        eventNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        eventNameLabel.setTextFill(Color.web("#0f172a"));
        detailsGrid.add(eventNameLabel, 0, 0, 2, 1);

        // Ligne 1: Date
        detailsGrid.add(new Label("📅 Date :"), 0, 1);
        eventDateLabel = new Label();
        eventDateLabel.setFont(Font.font("Arial", 14));
        detailsGrid.add(eventDateLabel, 1, 1);

        // Ligne 2: Lieu
        detailsGrid.add(new Label("📍 Lieu :"), 0, 2);
        eventLocationLabel = new Label();
        eventLocationLabel.setFont(Font.font("Arial", 14));
        detailsGrid.add(eventLocationLabel, 1, 2);

        // Ligne 3: Capacité
        detailsGrid.add(new Label("👥 Capacité :"), 0, 3);
        eventCapacityLabel = new Label();
        eventCapacityLabel.setFont(Font.font("Arial", 14));
        detailsGrid.add(eventCapacityLabel, 1, 3);

        box.getChildren().addAll(title, detailsGrid);

        return box;
    }

    private HBox createEventStatistics() {
        HBox box = new HBox(20);
        box.setPadding(new Insets(15, 20, 15, 20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        box.setAlignment(Pos.CENTER_LEFT);

        Label statsTitle = new Label("Statistiques de l'événement :");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statsTitle.setTextFill(Color.web("#0f172a"));

        // Ces labels seront mis à jour dynamiquement
        Label eventInscrits = new Label("0");
        eventInscrits.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventInscrits.setTextFill(Color.web("#3b82f6"));

        Label eventPresents = new Label("0");
        eventPresents.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventPresents.setTextFill(Color.web("#10b981"));

        Label eventAbsents = new Label("0");
        eventAbsents.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        eventAbsents.setTextFill(Color.web("#ef4444"));

        HBox inscritsStat = new HBox(5, new Label("📝 Inscrits:"), eventInscrits);
        inscritsStat.setAlignment(Pos.CENTER_LEFT);

        HBox presentsStat = new HBox(5, new Label("✅ Présents:"), eventPresents);
        presentsStat.setAlignment(Pos.CENTER_LEFT);

        HBox absentsStat = new HBox(5, new Label("❌ Absents:"), eventAbsents);
        absentsStat.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(statsTitle, inscritsStat, presentsStat, absentsStat, spacer);

        // Stocker les labels pour mise à jour
        box.getProperties().put("eventInscrits", eventInscrits);
        box.getProperties().put("eventPresents", eventPresents);
        box.getProperties().put("eventAbsents", eventAbsents);

        return box;
    }

    @SuppressWarnings("unchecked")
    private VBox createParticipantsTable() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("📋 Liste détaillée des participants");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#0f172a"));

        Label participantCount = new Label();
        participantCount.setFont(Font.font("Arial", 12));
        participantCount.setTextFill(Color.web("#64748b"));
        participantCount.setPadding(new Insets(0, 0, 0, 15));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Boutons d'actions
        Button exportCSVBtn = new Button("📥 Exporter CSV");
        exportCSVBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        exportCSVBtn.setOnAction(e -> exportParticipantsToCSV());

        Button exportAllBtn = new Button("📊 Exporter tous");
        exportAllBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        exportAllBtn.setOnAction(e -> exportAllParticipants());

        header.getChildren().addAll(tableTitle, participantCount, spacer, exportCSVBtn, exportAllBtn);
        box.getProperties().put("participantCount", participantCount);

        // Création du tableau
        participantsTable = new TableView<>();
        participantsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        participantsTable.setPrefHeight(450);
        participantsTable.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        // Colonne ID
        TableColumn<Participation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Participant
        TableColumn<Participation, String> participantCol = new TableColumn<>("Participant");
        participantCol.setCellValueFactory(cellData -> {
            User user = userService.getUserById(cellData.getValue().getIdUser());
            String fullName = user != null ? user.getPrenom() + " " + user.getNom() : "Utilisateur inconnu";
            return new SimpleStringProperty(fullName);
        });
        participantCol.setPrefWidth(180);

        // Colonne Email
        TableColumn<Participation, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> {
            User user = userService.getUserById(cellData.getValue().getIdUser());
            return new SimpleStringProperty(user != null ? user.getEmail() : "N/A");
        });
        emailCol.setPrefWidth(200);

        // Colonne Contact
        TableColumn<Participation, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setPrefWidth(180);

        // Colonne Âge
        TableColumn<Participation, Integer> ageCol = new TableColumn<>("Âge");
        ageCol.setCellValueFactory(cellData -> {
            Integer age = cellData.getValue().getAge();
            return new javafx.beans.property.SimpleObjectProperty<>(age);
        });
        ageCol.setCellFactory(column -> new TableCell<Participation, Integer>() {
            @Override
            protected void updateItem(Integer age, boolean empty) {
                super.updateItem(age, empty);
                if (empty || age == null) {
                    setText("-");
                } else {
                    setText(age + " ans");
                }
                setAlignment(Pos.CENTER);
            }
        });
        ageCol.setPrefWidth(80);

        // Colonne Statut
        TableColumn<Participation, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutCol.setCellFactory(column -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut.toUpperCase());
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(4, 12, 4, 12));
                    setFont(Font.font("Arial", FontWeight.BOLD, 11));

                    switch (statut.toLowerCase()) {
                        case "inscrit":
                            setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 15;");
                            break;
                        case "present":
                            setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 15;");
                            break;
                        case "absent":
                            setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 15;");
                            break;
                    }
                }
            }
        });
        statutCol.setPrefWidth(100);

        // Colonne Date d'inscription
        TableColumn<Participation, String> dateCol = new TableColumn<>("Date d'inscription");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Actions
        TableColumn<Participation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(column -> new TableCell<Participation, Void>() {
            private final Button viewBtn = new Button("👤 Détails");
            private final Button editBtn = new Button("📝 Modifier");
            private final Button presentBtn = new Button("✅ Présent");
            private final Button absentBtn = new Button("❌ Absent");
            private final HBox buttons = new HBox(5);

            {
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                presentBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                absentBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    showParticipantFullDetails(p);
                });

                editBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    showUpdateStatutDialog(p);
                });

                presentBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    updateParticipantStatus(p, "present");
                });

                absentBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    updateParticipantStatus(p, "absent");
                });

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Participation p = getTableView().getItems().get(getIndex());
                    buttons.getChildren().clear();

                    if ("inscrit".equals(p.getStatut())) {
                        buttons.getChildren().addAll(viewBtn, editBtn, presentBtn, absentBtn);
                    } else {
                        buttons.getChildren().addAll(viewBtn, editBtn);
                    }
                    setGraphic(buttons);
                }
            }
        });

        participantsTable.getColumns().addAll(
                idCol, participantCol, emailCol, contactCol, ageCol, statutCol, dateCol, actionsCol
        );

        // Style des lignes
        participantsTable.setRowFactory(tv -> new TableRow<Participation>() {
            @Override
            protected void updateItem(Participation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f8fafc;");
                    } else {
                        setStyle("-fx-background-color: white;");
                    }

                    setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            showParticipantFullDetails(item);
                        }
                    });
                }
            }
        });

        box.getChildren().addAll(header, participantsTable);

        return box;
    }

    private VBox createParticipantDetails() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(25));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("👤");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));

        VBox headerText = new VBox(5);
        Label title = new Label("Fiche détaillée du participant");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#0f172a"));

        Label participantName = new Label();
        participantName.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        participantName.setTextFill(Color.web("#2196F3"));

        headerText.getChildren().addAll(title, participantName);
        header.getChildren().addAll(iconLabel, headerText);

        // Grille d'informations
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(15, 0, 15, 0));

        // Section 1: Informations personnelles
        Label personalTitle = new Label("📋 INFORMATIONS PERSONNELLES");
        personalTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        personalTitle.setTextFill(Color.web("#0f172a"));
        detailsGrid.add(personalTitle, 0, 0, 2, 1);

        detailsGrid.add(new Label("Nom complet:"), 0, 1);
        Label fullNameDetail = new Label();
        fullNameDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(fullNameDetail, 1, 1);

        detailsGrid.add(new Label("Email:"), 0, 2);
        Label emailDetail = new Label();
        emailDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(emailDetail, 1, 2);

        detailsGrid.add(new Label("Âge:"), 0, 3);
        Label ageDetail = new Label();
        ageDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(ageDetail, 1, 3);

        detailsGrid.add(new Label("Genre:"), 0, 4);
        Label genderDetail = new Label();
        genderDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(genderDetail, 1, 4);

        // Section 2: Informations de participation
        Label participationTitle = new Label("🎟️ INFORMATIONS DE PARTICIPATION");
        participationTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        participationTitle.setTextFill(Color.web("#0f172a"));
        detailsGrid.add(participationTitle, 0, 5, 2, 1);

        detailsGrid.add(new Label("Email de contact:"), 0, 6);
        Label contactDetail = new Label();
        contactDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(contactDetail, 1, 6);

        detailsGrid.add(new Label("Statut:"), 0, 7);
        Label statutDetail = new Label();
        statutDetail.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        detailsGrid.add(statutDetail, 1, 7);

        detailsGrid.add(new Label("Date d'inscription:"), 0, 8);
        Label dateDetail = new Label();
        dateDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(dateDetail, 1, 8);

        // Section 3: Informations sur l'événement
        Label eventTitle = new Label("📅 INFORMATIONS SUR L'ÉVÉNEMENT");
        eventTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        eventTitle.setTextFill(Color.web("#0f172a"));
        detailsGrid.add(eventTitle, 0, 9, 2, 1);

        detailsGrid.add(new Label("Événement:"), 0, 10);
        Label eventDetail = new Label();
        eventDetail.setFont(Font.font("Arial", 14));
        eventDetail.setWrapText(true);
        detailsGrid.add(eventDetail, 1, 10);

        detailsGrid.add(new Label("Date:"), 0, 11);
        Label eventDateDetail = new Label();
        eventDateDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(eventDateDetail, 1, 11);

        detailsGrid.add(new Label("Lieu:"), 0, 12);
        Label eventLocationDetail = new Label();
        eventLocationDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(eventLocationDetail, 1, 12);

        detailsGrid.add(new Label("Organisateur:"), 0, 13);
        Label organizerDetail = new Label();
        organizerDetail.setFont(Font.font("Arial", 14));
        detailsGrid.add(organizerDetail, 1, 13);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            box.setVisible(false);
            box.setManaged(false);
        });

        Button editStatusBtn = new Button("📝 Modifier le statut");
        editStatusBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");

        buttonBox.getChildren().addAll(closeBtn, editStatusBtn);

        box.getChildren().addAll(header, detailsGrid, buttonBox);

        // Stocker tous les labels dans les propriétés
        box.getProperties().put("participantName", participantName);
        box.getProperties().put("fullNameDetail", fullNameDetail);
        box.getProperties().put("emailDetail", emailDetail);
        box.getProperties().put("ageDetail", ageDetail);
        box.getProperties().put("genderDetail", genderDetail);
        box.getProperties().put("contactDetail", contactDetail);
        box.getProperties().put("statutDetail", statutDetail);
        box.getProperties().put("dateDetail", dateDetail);
        box.getProperties().put("eventDetail", eventDetail);
        box.getProperties().put("eventDateDetail", eventDateDetail);
        box.getProperties().put("eventLocationDetail", eventLocationDetail);
        box.getProperties().put("organizerDetail", organizerDetail);
        box.getProperties().put("editStatusBtn", editStatusBtn);

        return box;
    }

    private void loadEvents() {
        List<Event> events = eventService.getEventsByOrganisateur(currentUser.getId());
        ObservableList<Event> eventList = FXCollections.observableArrayList(events);
        eventSelector.setItems(eventList);

        // Configuration de l'affichage des événements
        eventSelector.setCellFactory(lv -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.getTitre() + " - " + event.getFormattedDate() + " - " +
                            event.getParticipantsCount() + " participants");
                }
            }
        });

        eventSelector.setButtonCell(new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.getTitre() + " - " + event.getFormattedDate());
                }
            }
        });

        // Mettre à jour les statistiques globales
        updateGlobalStats(events);
    }

    private void updateGlobalStats(List<Event> events) {
        int totalParticipants = 0;
        int totalInscrits = 0;
        int totalPresents = 0;
        int totalAbsents = 0;

        for (Event event : events) {
            List<Participation> participants = participationService.getParticipationsByEvent(event.getId_evenement());
            totalParticipants += participants.size();

            for (Participation p : participants) {
                switch (p.getStatut().toLowerCase()) {
                    case "inscrit": totalInscrits++; break;
                    case "present": totalPresents++; break;
                    case "absent": totalAbsents++; break;
                }
            }
        }

        totalParticipantsLabel.setText(String.valueOf(totalParticipants));
        inscritsLabel.setText(String.valueOf(totalInscrits));
        presentsLabel.setText(String.valueOf(totalPresents));
        absentsLabel.setText(String.valueOf(totalAbsents));
    }

    private void loadEventDetails(Event event) {
        eventNameLabel.setText(event.getTitre());
        eventDateLabel.setText(event.getFormattedDate());
        eventLocationLabel.setText(event.getLieu() != null ? event.getLieu() : "Non spécifié");

        String capacite = event.getCapacite_max() != null ?
                event.getCapacite_max() + " places" : "Illimitée";
        eventCapacityLabel.setText(capacite);
    }

    private void loadParticipantsForEvent(Event event) {
        List<Participation> participants = participationService.getParticipationsByEvent(event.getId_evenement());
        ObservableList<Participation> participantList = FXCollections.observableArrayList(participants);
        participantsTable.setItems(participantList);

        // Mettre à jour le compteur
        Label countLabel = (Label) participantsTable.getParent().getProperties().get("participantCount");
        if (countLabel != null) {
            countLabel.setText("(" + participants.size() + " participant(s))");
        }

        // Mettre à jour les statistiques de l'événement
        updateEventStats(event, participants);
    }

    private void updateEventStats(Event event, List<Participation> participants) {
        int inscrits = 0;
        int presents = 0;
        int absents = 0;

        for (Participation p : participants) {
            switch (p.getStatut().toLowerCase()) {
                case "inscrit": inscrits++; break;
                case "present": presents++; break;
                case "absent": absents++; break;
            }
        }

        // Afficher les statistiques
        HBox eventStatsBox = (HBox) eventDetailsBox.getParent().getChildrenUnmodifiable().get(4);
        if (eventStatsBox != null) {
            Label eventInscrits = (Label) eventStatsBox.getProperties().get("eventInscrits");
            Label eventPresents = (Label) eventStatsBox.getProperties().get("eventPresents");
            Label eventAbsents = (Label) eventStatsBox.getProperties().get("eventAbsents");

            if (eventInscrits != null) eventInscrits.setText(String.valueOf(inscrits));
            if (eventPresents != null) eventPresents.setText(String.valueOf(presents));
            if (eventAbsents != null) eventAbsents.setText(String.valueOf(absents));

            eventStatsBox.setVisible(true);
            eventStatsBox.setManaged(true);
        }
    }

    private void showParticipantFullDetails(Participation participation) {
        User participant = userService.getUserById(participation.getIdUser());
        Event event = eventService.getEventById(participation.getIdEvenement());
        User organizer = userService.getUserById(event.getId_organisateur());

        if (participant == null || event == null) return;

        // Mettre à jour tous les labels
        Label participantName = (Label) participantDetailsBox.getProperties().get("participantName");
        Label fullNameDetail = (Label) participantDetailsBox.getProperties().get("fullNameDetail");
        Label emailDetail = (Label) participantDetailsBox.getProperties().get("emailDetail");
        Label ageDetail = (Label) participantDetailsBox.getProperties().get("ageDetail");
        Label genderDetail = (Label) participantDetailsBox.getProperties().get("genderDetail");
        Label contactDetail = (Label) participantDetailsBox.getProperties().get("contactDetail");
        Label statutDetail = (Label) participantDetailsBox.getProperties().get("statutDetail");
        Label dateDetail = (Label) participantDetailsBox.getProperties().get("dateDetail");
        Label eventDetail = (Label) participantDetailsBox.getProperties().get("eventDetail");
        Label eventDateDetail = (Label) participantDetailsBox.getProperties().get("eventDateDetail");
        Label eventLocationDetail = (Label) participantDetailsBox.getProperties().get("eventLocationDetail");
        Label organizerDetail = (Label) participantDetailsBox.getProperties().get("organizerDetail");
        Button editStatusBtn = (Button) participantDetailsBox.getProperties().get("editStatusBtn");

        String fullName = participant.getPrenom() + " " + participant.getNom();
        participantName.setText(fullName);
        fullNameDetail.setText(fullName);
        emailDetail.setText(participant.getEmail());
        ageDetail.setText(participation.getAge() != null ? participation.getAge() + " ans" : "Non spécifié");

        String gender = "";
        if (participant.getIdGenre() == 1) gender = "Homme";
        else if (participant.getIdGenre() == 2) gender = "Femme";
        else gender = "Non spécifié";
        genderDetail.setText(gender);

        contactDetail.setText(participation.getContact());

        String statut = participation.getStatut().toUpperCase();
        statutDetail.setText(statut);
        statutDetail.setStyle(getStatutStyle(participation.getStatut()));

        dateDetail.setText(participation.getFormattedDate());
        eventDetail.setText(event.getTitre());
        eventDateDetail.setText(event.getFormattedDate());
        eventLocationDetail.setText(event.getLieu() != null ? event.getLieu() : "Non spécifié");
        organizerDetail.setText(organizer != null ? organizer.getPrenom() + " " + organizer.getNom() : "N/A");

        editStatusBtn.setOnAction(e -> showUpdateStatutDialog(participation));

        participantDetailsBox.setVisible(true);
        participantDetailsBox.setManaged(true);
    }

    private String getStatutStyle(String statut) {
        switch (statut.toLowerCase()) {
            case "inscrit":
                return "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;";
            case "present":
                return "-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;";
            case "absent":
                return "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;";
            default:
                return "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;";
        }
    }

    private void updateParticipantStatus(Participation participation, String newStatus) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Changer le statut");
        confirm.setContentText("Voulez-vous marquer ce participant comme '" + newStatus + "' ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = participationService.updateStatut(
                    participation.getIdEvenement(),
                    participation.getIdUser(),
                    newStatus
            );

            if (success) {
                // Recharger les données
                Event selectedEvent = eventSelector.getValue();
                if (selectedEvent != null) {
                    loadParticipantsForEvent(selectedEvent);
                }

                // Mettre à jour les statistiques globales
                loadEvents();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Statut mis à jour avec succès !");
                successAlert.showAndWait();
            }
        }
    }

    private void showUpdateStatutDialog(Participation participation) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifier le statut");
        dialog.setResizable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(400);

        Label title = new Label("📝 Changer le statut du participant");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#0f172a"));

        User participant = userService.getUserById(participation.getIdUser());
        Label participantName = new Label(participant.getPrenom() + " " + participant.getNom());
        participantName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        participantName.setTextFill(Color.web("#2196F3"));
        participantName.setPadding(new Insets(5, 0, 10, 0));

        VBox radioBox = new VBox(12);
        radioBox.setPadding(new Insets(10, 0, 10, 0));
        radioBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 8;");

        ToggleGroup group = new ToggleGroup();

        RadioButton inscritRadio = new RadioButton("Inscrit - En attente de confirmation");
        inscritRadio.setToggleGroup(group);
        inscritRadio.setSelected(participation.getStatut().equals("inscrit"));
        inscritRadio.setFont(Font.font("Arial", 13));

        RadioButton presentRadio = new RadioButton("Présent - A participé à l'événement");
        presentRadio.setToggleGroup(group);
        presentRadio.setSelected(participation.getStatut().equals("present"));
        presentRadio.setFont(Font.font("Arial", 13));

        RadioButton absentRadio = new RadioButton("Absent - Ne s'est pas présenté");
        absentRadio.setToggleGroup(group);
        absentRadio.setSelected(participation.getStatut().equals("absent"));
        absentRadio.setFont(Font.font("Arial", 13));

        radioBox.getChildren().addAll(inscritRadio, presentRadio, absentRadio);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String newStatut = "";
            if (inscritRadio.isSelected()) newStatut = "inscrit";
            else if (presentRadio.isSelected()) newStatut = "present";
            else if (absentRadio.isSelected()) newStatut = "absent";

            if (!newStatut.isEmpty() && !newStatut.equals(participation.getStatut())) {
                boolean success = participationService.updateStatut(
                        participation.getIdEvenement(),
                        participation.getIdUser(),
                        newStatut
                );

                if (success) {
                    Event selectedEvent = eventSelector.getValue();
                    if (selectedEvent != null) {
                        loadParticipantsForEvent(selectedEvent);
                    }
                    loadEvents();
                    dialog.close();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("Statut mis à jour avec succès !");
                    alert.showAndWait();
                }
            } else {
                dialog.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        content.getChildren().addAll(title, participantName, radioBox, buttonBox);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.show();
    }

    private void exportParticipantsToCSV() {
        Event selectedEvent = eventSelector.getValue();
        if (selectedEvent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement d'abord.");
            alert.showAndWait();
            return;
        }

        List<Participation> participants = participantsTable.getItems();

        try {
            String fileName = "participants_" + selectedEvent.getTitre().replace(" ", "_") +
                    "_" + LocalDate.now() + ".csv";

            try (FileWriter writer = new FileWriter(fileName)) {
                // En-tête CSV
                writer.write("ID,Prénom,Nom,Email,Email de contact,Âge,Statut,Date d'inscription\n");

                for (Participation p : participants) {
                    User user = userService.getUserById(p.getIdUser());
                    if (user != null) {
                        writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                                p.getId(),
                                escapeCsv(user.getPrenom()),
                                escapeCsv(user.getNom()),
                                escapeCsv(user.getEmail()),
                                escapeCsv(p.getContact()),
                                p.getAge() != null ? p.getAge() : "-",
                                p.getStatut(),
                                p.getFormattedDate()
                        ));
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("✅ Export réussi !\nFichier: " + fileName);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("❌ Erreur lors de l'export: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void exportAllParticipants() {
        List<Event> events = eventService.getEventsByOrganisateur(currentUser.getId());

        try {
            String fileName = "tous_participants_" + LocalDate.now() + ".csv";

            try (FileWriter writer = new FileWriter(fileName)) {
                // En-tête CSV
                writer.write("ID,Prénom,Nom,Email,Email de contact,Âge,Statut,Événement,Date événement,Date inscription\n");

                for (Event event : events) {
                    List<Participation> participants = participationService.getParticipationsByEvent(event.getId_evenement());

                    for (Participation p : participants) {
                        User user = userService.getUserById(p.getIdUser());
                        if (user != null) {
                            writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                    p.getId(),
                                    escapeCsv(user.getPrenom()),
                                    escapeCsv(user.getNom()),
                                    escapeCsv(user.getEmail()),
                                    escapeCsv(p.getContact()),
                                    p.getAge() != null ? p.getAge() : "-",
                                    p.getStatut(),
                                    escapeCsv(event.getTitre()),
                                    event.getFormattedDate(),
                                    p.getFormattedDate()
                            ));
                        }
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("✅ Export de tous les participants réussi !\nFichier: " + fileName);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("❌ Erreur lors de l'export: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}