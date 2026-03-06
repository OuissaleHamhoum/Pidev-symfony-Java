package edu.Loopi.view;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import edu.Loopi.entities.Participation;
import edu.Loopi.services.EventService;
import edu.Loopi.services.ParticipationService;
import edu.Loopi.services.NotificationService;
import edu.Loopi.services.UserService;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EventViewParticipant {
    private User currentUser;
    private VBox mainLayout;
    private FlowPane cardsContainer;
    private EventService eventService = new EventService();
    private ParticipationService participationService = new ParticipationService();
    private NotificationService notificationService = new NotificationService();
    private UserService userService = new UserService();
    private List<Event> allEvents;

    // Composants pour les filtres
    private TextField searchField = new TextField();
    private ComboBox<String> statusFilter = new ComboBox<>();
    private ComboBox<String> sortFilter = new ComboBox<>();
    private CheckBox showPastEventsCheckBox = new CheckBox("Afficher les événements passés");
    private HBox statsBar = new HBox(20);
    private Label messageInfoLabel;

    // Constantes pour les images
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String IMAGE_STORAGE_DIR = "src" + File.separator + "main" + File.separator +
            "resources" + File.separator + "uploads" + File.separator +
            "events" + File.separator;
    private static final String FULL_IMAGE_PATH = PROJECT_ROOT + File.separator + IMAGE_STORAGE_DIR;

    public EventViewParticipant(User user) {
        this.currentUser = user;
        this.mainLayout = new VBox(25);
        createView();
        loadData();
    }

    private void createView() {
        mainLayout.getChildren().clear();
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #f8f9fa;");

        VBox heroSection = new VBox(8);
        heroSection.setPadding(new Insets(0, 0, 15, 0));

        Label bigTitle = new Label("📅 Événements Écologiques");
        bigTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        bigTitle.setTextFill(Color.web("#1e293b"));

        Label description = new Label("Participez à des événements engagés pour l'environnement");
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        description.setTextFill(Color.web("#475569"));
        description.setWrapText(true);

        heroSection.getChildren().addAll(bigTitle, description);

        messageInfoLabel = new Label("");
        messageInfoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        messageInfoLabel.setTextFill(Color.web("#059669"));
        messageInfoLabel.setPadding(new Insets(5, 0, 0, 0));
        messageInfoLabel.setVisible(false);
        heroSection.getChildren().add(messageInfoLabel);

        statsBar.setAlignment(Pos.CENTER);
        statsBar.setPadding(new Insets(15));
        statsBar.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15));
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 15; -fx-cursor: hand;");
        refreshBtn.setTooltip(new Tooltip("Actualiser"));
        refreshBtn.setOnAction(e -> refreshData());

        Label statusLabel = new Label("Statut:");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web("#1e293b"));

        statusFilter.getItems().addAll("Tous", "À venir", "En cours", "Passés");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(120);
        statusFilter.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 12; " +
                "-fx-background-color: white; -fx-border-color: #cbd5e1;");
        statusFilter.setOnAction(e -> applyFilters());

        Label sortLabel = new Label("Trier par:");
        sortLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sortLabel.setTextFill(Color.web("#1e293b"));

        sortFilter.getItems().addAll("Plus récent", "Moins récent", "Places disponibles", "Plus de participants");
        sortFilter.setValue("Plus récent");
        sortFilter.setPrefWidth(150);
        sortFilter.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 12; " +
                "-fx-background-color: white; -fx-border-color: #cbd5e1;");
        sortFilter.setOnAction(e -> applyFilters());

        Label searchLabel = new Label("Recherche:");
        searchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        searchLabel.setTextFill(Color.web("#1e293b"));

        searchField.setPromptText("🔍 Titre, lieu...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 12; " +
                "-fx-background-color: white; -fx-border-color: #cbd5e1;");
        searchField.textProperty().addListener((obs, old, nv) -> applyFilters());

        showPastEventsCheckBox.setFont(Font.font("Arial", 12));
        showPastEventsCheckBox.setTextFill(Color.web("#1e293b"));
        showPastEventsCheckBox.setSelected(false);
        showPastEventsCheckBox.setOnAction(e -> applyFilters());

        Button myParticipationsBtn = new Button("👥 Mes participations");
        myParticipationsBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 30; " +
                "-fx-font-size: 13px; -fx-cursor: hand;");
        myParticipationsBtn.setOnAction(e -> showMyParticipations());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(
                refreshBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                statusLabel, statusFilter,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                sortLabel, sortFilter,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                searchLabel, searchField,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                showPastEventsCheckBox,
                spacer,
                myParticipationsBtn
        );

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
        List<Event> tousEvents = eventService.getAllEvents();
        allEvents = tousEvents.stream()
                .filter(e -> "approuve".equals(e.getStatutValidation()))
                .filter(e -> e.isEstPublie())
                .collect(Collectors.toList());

        if (allEvents.isEmpty()) {
            messageInfoLabel.setText("🔔 Aucun événement disponible pour le moment. Revenez plus tard !");
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

    private void updateStats() {
        statsBar.getChildren().clear();

        List<Event> upcomingEvents = allEvents.stream()
                .filter(e -> !e.isDatePassee())
                .collect(Collectors.toList());

        int total = allEvents.size();
        int aVenir = (int) allEvents.stream().filter(e -> "à venir".equals(e.getStatut()) && !e.isDatePassee()).count();
        int enCours = (int) allEvents.stream().filter(e -> "en cours".equals(e.getStatut()) && !e.isDatePassee()).count();
        int passes = (int) allEvents.stream().filter(Event::isDatePassee).count();

        int mesParticipations = participationService.countParticipationsByUser(currentUser.getId());

        statsBar.getChildren().addAll(
                createStatCard("📊", String.valueOf(total), "Total", "#1e293b"),
                createStatCard("⏳", String.valueOf(aVenir), "À venir", "#3b82f6"),
                createStatCard("🔄", String.valueOf(enCours), "En cours", "#f59e0b"),
                createStatCard("✅", String.valueOf(passes), "Passés", "#64748b"),
                createStatCard("👥", String.valueOf(mesParticipations), "Mes participations", "#10b981")
        );
    }

    private VBox createStatCard(String icon, String value, String label, String color) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        HBox iconBox = new HBox(5);
        iconBox.setAlignment(Pos.CENTER);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 14px;");
        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #475569;");
        iconBox.getChildren().addAll(iconLbl, labelLbl);

        card.getChildren().addAll(valLbl, iconBox);
        return card;
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = statusFilter.getValue();
        String selectedSort = sortFilter.getValue();
        boolean showPast = showPastEventsCheckBox.isSelected();

        List<Event> filtered = allEvents.stream()
                .filter(event -> {
                    if (searchText.isEmpty()) return true;
                    return event.getTitre().toLowerCase().contains(searchText) ||
                            (event.getLieu() != null && event.getLieu().toLowerCase().contains(searchText));
                })
                .filter(event -> {
                    if (!showPast && event.isDatePassee()) return false;
                    return true;
                })
                .filter(event -> {
                    if (selectedStatus == null || "Tous".equals(selectedStatus)) return true;
                    String eventStatut = event.getStatut().toLowerCase();
                    String filterStatut = selectedStatus.toLowerCase();
                    if (filterStatut.equals("à venir") && eventStatut.equals("à venir") && !event.isDatePassee()) return true;
                    if (filterStatut.equals("en cours") && eventStatut.equals("en cours") && !event.isDatePassee()) return true;
                    if (filterStatut.equals("passés") && event.isDatePassee()) return true;
                    return false;
                })
                .collect(Collectors.toList());

        switch (selectedSort) {
            case "Plus récent":
                filtered.sort((e1, e2) -> e2.getDate_evenement().compareTo(e1.getDate_evenement()));
                break;
            case "Moins récent":
                filtered.sort((e1, e2) -> e1.getDate_evenement().compareTo(e2.getDate_evenement()));
                break;
            case "Places disponibles":
                filtered.sort((e1, e2) -> {
                    int places1 = e1.getPlacesRestantes();
                    int places2 = e2.getPlacesRestantes();
                    return Integer.compare(places2, places1);
                });
                break;
            case "Plus de participants":
                filtered.sort((e1, e2) -> Integer.compare(
                        e2.getParticipantsCount(), e1.getParticipantsCount()));
                break;
        }

        displayCards(filtered);
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

            Label emptySubtext = new Label("Revenez plus tard pour découvrir de nouveaux événements !");
            emptySubtext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            emptySubtext.setTextFill(Color.web("#64748b"));

            emptyState.getChildren().addAll(emptyIcon, emptyText, emptySubtext);
            cardsContainer.getChildren().add(emptyState);
        } else {
            for (Event event : events) {
                cardsContainer.getChildren().add(createEventCard(event));
            }
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(0);
        card.setPrefSize(320, 520);
        boolean isPastEvent = event.isDatePassee();

        String cardStyle = isPastEvent ?
                "-fx-background-color: #f1f5f9; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 5);" :
                "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 5);";

        card.setStyle(cardStyle);

        if (!isPastEvent) {
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
        }

        StackPane imgContainer = new StackPane();
        ImageView imgView = new ImageView();
        imgView.setFitWidth(320);
        imgView.setFitHeight(160);
        imgView.setPreserveRatio(false);

        if (event.getImage_evenement() != null && !event.getImage_evenement().isEmpty()) {
            try {
                String fileName = event.getImage_evenement().substring(event.getImage_evenement().lastIndexOf('/') + 1);
                File imgFile = new File(FULL_IMAGE_PATH + fileName);
                if (imgFile.exists()) {
                    imgView.setImage(new Image(imgFile.toURI().toString()));
                } else {
                    imgView.setImage(new Image("https://via.placeholder.com/320x160/e2e8f0/1e293b?text=LOOPI"));
                }
            } catch (Exception e) {
                imgView.setImage(new Image("https://via.placeholder.com/320x160/e2e8f0/1e293b?text=LOOPI"));
            }
        } else {
            imgView.setImage(new Image("https://via.placeholder.com/320x160/e2e8f0/1e293b?text=LOOPI"));
        }

        Rectangle clip = new Rectangle(320, 160);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imgView.setClip(clip);
        imgContainer.getChildren().add(imgView);

        String statut = event.getStatut();
        Label statusBadge = new Label(statut.substring(0, 1).toUpperCase() + statut.substring(1));
        statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        statusBadge.setTextFill(Color.WHITE);
        statusBadge.setPadding(new Insets(4, 12, 4, 12));

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

        Label approvedBadge = new Label("✓ Approuvé");
        approvedBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        approvedBadge.setTextFill(Color.WHITE);
        approvedBadge.setPadding(new Insets(3, 10, 3, 10));
        approvedBadge.setStyle("-fx-background-color: #10b981; -fx-background-radius: 20;");
        StackPane.setAlignment(approvedBadge, Pos.TOP_LEFT);
        StackPane.setMargin(approvedBadge, new Insets(10, 0, 0, 10));
        imgContainer.getChildren().add(approvedBadge);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        HBox dateLieu = new HBox(10);
        dateLieu.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅 " + event.getFormattedDate().substring(0, 10));
        dateLabel.setFont(Font.font(11));
        dateLabel.setTextFill(Color.web("#64748b"));

        Label lieuLabel = new Label("📍 " + (event.getLieu() != null && event.getLieu().length() > 20 ?
                event.getLieu().substring(0, 17) + "..." : event.getLieu()));
        lieuLabel.setFont(Font.font(11));
        lieuLabel.setTextFill(Color.web("#64748b"));

        dateLieu.getChildren().addAll(dateLabel, lieuLabel);

        Label titreLabel = new Label(event.getTitre());
        titreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titreLabel.setTextFill(isPastEvent ? Color.web("#64748b") : Color.web("#0f172a"));
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
        descLabel.setTextFill(Color.web("#475569"));
        descLabel.setMaxHeight(40);

        Label organisateurLabel = new Label("👤 Organisé par: " +
                (event.getOrganisateurNom() != null ? event.getOrganisateurNom() : "Inconnu"));
        organisateurLabel.setFont(Font.font(10));
        organisateurLabel.setTextFill(Color.web("#64748b"));

        HBox progressInfo = new HBox(15);
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        progressInfo.setPadding(new Insets(5, 0, 5, 0));

        VBox participantsBox = new VBox(2);
        participantsBox.setAlignment(Pos.CENTER);

        Label participantsCount = new Label(String.valueOf(event.getParticipantsCount()));
        participantsCount.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        participantsCount.setTextFill(isPastEvent ? Color.web("#94a3b8") : Color.web("#10b981"));

        Label participantsLabel = new Label("participants");
        participantsLabel.setFont(Font.font(10));
        participantsLabel.setTextFill(Color.web("#64748b"));

        participantsBox.getChildren().addAll(participantsCount, participantsLabel);
        progressInfo.getChildren().add(participantsBox);

        if (event.getCapacite_max() != null) {
            VBox capaciteBox = new VBox(2);
            capaciteBox.setAlignment(Pos.CENTER);

            Label capaciteCount = new Label(String.valueOf(event.getCapacite_max()));
            capaciteCount.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            capaciteCount.setTextFill(isPastEvent ? Color.web("#94a3b8") : Color.web("#3b82f6"));

            Label capaciteLabel = new Label("places max");
            capaciteLabel.setFont(Font.font(10));
            capaciteLabel.setTextFill(Color.web("#64748b"));

            capaciteBox.getChildren().addAll(capaciteCount, capaciteLabel);
            progressInfo.getChildren().add(capaciteBox);

            int placesRestantes = event.getPlacesRestantes();
            VBox restantesBox = new VBox(2);
            restantesBox.setAlignment(Pos.CENTER);

            Label restantesCount = new Label(String.valueOf(placesRestantes));
            restantesCount.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            restantesCount.setTextFill(isPastEvent ? Color.web("#94a3b8") :
                    (placesRestantes > 0 ? Color.web("#f59e0b") : Color.web("#ef4444")));

            Label restantesLabel = new Label("places restantes");
            restantesLabel.setFont(Font.font(10));
            restantesLabel.setTextFill(Color.web("#64748b"));

            restantesBox.getChildren().addAll(restantesCount, restantesLabel);
            progressInfo.getChildren().add(restantesBox);
        }

        boolean isParticipant = participationService.isParticipant(event.getId_evenement(), currentUser.getId());
        Button actionBtn = new Button();
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setPrefHeight(40);
        actionBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        actionBtn.setCursor(javafx.scene.Cursor.HAND);

        if (isPastEvent) {
            actionBtn.setText("👁️ Voir détails");
            actionBtn.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; " +
                    "-fx-background-radius: 8; -fx-opacity: 0.8;");
            actionBtn.setOnAction(e -> showEventDetails(event));
        } else if (event.isComplet()) {
            actionBtn.setText("⚠️ Complet");
            actionBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                    "-fx-background-radius: 8;");
            actionBtn.setDisable(true);
        } else if (isParticipant) {
            actionBtn.setText("✅ Gérer ma participation");
            actionBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                    "-fx-background-radius: 8; -fx-font-weight: bold;");
            actionBtn.setOnAction(e -> showParticipationManagement(event));
        } else {
            actionBtn.setText("🎟️ Participer");
            actionBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-background-radius: 8; -fx-font-weight: bold;");
            actionBtn.setOnAction(e -> openParticipationDialog(event));
        }

        content.getChildren().addAll(dateLieu, titreLabel, descLabel, organisateurLabel, progressInfo, actionBtn);
        card.getChildren().addAll(imgContainer, content);

        return card;
    }

    private void showEventDetails(Event event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détails de l'événement");
        dialog.setResizable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        content.setPrefWidth(600);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label("📅");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label(event.getTitre());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#0f172a"));
        titleLabel.setWrapText(true);

        Label dateLabel = new Label(event.getFormattedDate());
        dateLabel.setFont(Font.font("Segoe UI", 14));
        dateLabel.setTextFill(Color.web("#475569"));

        headerText.getChildren().addAll(titleLabel, dateLabel);
        header.getChildren().addAll(iconLabel, headerText);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(15);
        infoGrid.setPadding(new Insets(15, 0, 15, 0));

        // Ligne 1: Lieu
        Label lieuIcon = new Label("📍");
        lieuIcon.setFont(Font.font("Arial", 16));
        infoGrid.add(lieuIcon, 0, 0);

        VBox lieuBox = new VBox(2);
        Label lieuLabel = new Label("Lieu");
        lieuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lieuLabel.setTextFill(Color.web("#64748b"));
        Label lieuValue = new Label(event.getLieu() != null ? event.getLieu() : "Non spécifié");
        lieuValue.setFont(Font.font("Arial", 14));
        lieuValue.setTextFill(Color.web("#0f172a"));
        lieuValue.setWrapText(true);
        lieuBox.getChildren().addAll(lieuLabel, lieuValue);
        infoGrid.add(lieuBox, 1, 0);

        // Ligne 2: Organisateur
        Label orgIcon = new Label("👤");
        orgIcon.setFont(Font.font("Arial", 16));
        infoGrid.add(orgIcon, 0, 1);

        VBox orgBox = new VBox(2);
        Label orgLabel = new Label("Organisateur");
        orgLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        orgLabel.setTextFill(Color.web("#64748b"));
        Label orgValue = new Label(event.getOrganisateurNom() != null ? event.getOrganisateurNom() : "Inconnu");
        orgValue.setFont(Font.font("Arial", 14));
        orgValue.setTextFill(Color.web("#0f172a"));
        orgValue.setWrapText(true);
        orgBox.getChildren().addAll(orgLabel, orgValue);
        infoGrid.add(orgBox, 1, 1);

        // Ligne 3: Participants
        Label partIcon = new Label("👥");
        partIcon.setFont(Font.font("Arial", 16));
        infoGrid.add(partIcon, 0, 2);

        VBox partBox = new VBox(2);
        Label partLabel = new Label("Participants");
        partLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        partLabel.setTextFill(Color.web("#64748b"));
        Label partValue = new Label(event.getParticipantsCount() + " participant(s)");
        partValue.setFont(Font.font("Arial", 14));
        partValue.setTextFill(Color.web("#0f172a"));
        partBox.getChildren().addAll(partLabel, partValue);
        infoGrid.add(partBox, 1, 2);

        // Ligne 4: Capacité (si disponible)
        if (event.getCapacite_max() != null) {
            Label capIcon = new Label("📊");
            capIcon.setFont(Font.font("Arial", 16));
            infoGrid.add(capIcon, 0, 3);

            VBox capBox = new VBox(2);
            Label capLabel = new Label("Capacité");
            capLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            capLabel.setTextFill(Color.web("#64748b"));
            Label capValue = new Label(event.getCapacite_max() + " places (" + event.getPlacesRestantes() + " restantes)");
            capValue.setFont(Font.font("Arial", 14));
            capValue.setTextFill(Color.web("#0f172a"));
            capBox.getChildren().addAll(capLabel, capValue);
            infoGrid.add(capBox, 1, 3);
        }

        // Ligne 5: Statut
        Label statutIcon = new Label("📌");
        statutIcon.setFont(Font.font("Arial", 16));
        infoGrid.add(statutIcon, 0, 4);

        VBox statutBox = new VBox(2);
        Label statutLabelTitle = new Label("Statut");
        statutLabelTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statutLabelTitle.setTextFill(Color.web("#64748b"));

        String statut = event.getStatut();
        Label statutValue = new Label(statut.substring(0, 1).toUpperCase() + statut.substring(1));
        statutValue.setFont(Font.font("Arial", 14));
        statutValue.setTextFill(Color.web(statut.equals("passé") ? "#64748b" :
                (statut.equals("à venir") ? "#3b82f6" : "#f59e0b")));
        statutBox.getChildren().addAll(statutLabelTitle, statutValue);
        infoGrid.add(statutBox, 1, 4);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(490);
        infoGrid.getColumnConstraints().addAll(col1, col2);

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        Label descriptionTitle = new Label("📝 Description");
        descriptionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        descriptionTitle.setTextFill(Color.web("#0f172a"));

        TextArea descArea = new TextArea(event.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(5);
        descArea.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-font-size: 13px;");

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(header, infoGrid, separator, descriptionTitle, descArea, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 650, 750);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openParticipationDialog(Event event) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Participation à l'événement");
        dialog.setResizable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        content.setPrefWidth(500);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label("🎟️");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label("Confirmer votre participation");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#0f172a"));

        Label eventTitle = new Label(event.getTitre());
        eventTitle.setFont(Font.font("Segoe UI", 14));
        eventTitle.setTextFill(Color.web("#475569"));
        eventTitle.setWrapText(true);

        headerText.getChildren().addAll(titleLabel, eventTitle);
        header.getChildren().addAll(iconLabel, headerText);

        VBox infoBox = new VBox(15);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12;");

        int placesRestantes = event.getPlacesRestantes();
        Label placesLabel = new Label("📊 Places restantes: " + placesRestantes);
        placesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        placesLabel.setTextFill(placesRestantes > 0 ? Color.web("#059669") : Color.web("#ef4444"));

        HBox dateInfo = new HBox(10);
        dateInfo.setAlignment(Pos.CENTER_LEFT);
        dateInfo.getChildren().addAll(
                new Label("📅"),
                new Label("Date: " + event.getFormattedDate())
        );
        ((Label) dateInfo.getChildren().get(1)).setFont(Font.font("Arial", 13));

        HBox lieuInfo = new HBox(10);
        lieuInfo.setAlignment(Pos.CENTER_LEFT);
        lieuInfo.getChildren().addAll(
                new Label("📍"),
                new Label("Lieu: " + event.getLieu())
        );
        ((Label) lieuInfo.getChildren().get(1)).setFont(Font.font("Arial", 13));

        infoBox.getChildren().addAll(placesLabel, dateInfo, lieuInfo);

        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(10, 0, 10, 0));

        Label formLabel = new Label("Vos informations");
        formLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        formLabel.setTextFill(Color.web("#0f172a"));

        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Email de contact *");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        emailLabel.setTextFill(Color.web("#475569"));

        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("votre@email.com");
        emailField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-font-size: 13px;");
        emailBox.getChildren().addAll(emailLabel, emailField);

        VBox dateNaissanceBox = new VBox(5);
        Label dateNaissanceLabel = new Label("Date de naissance (optionnel)");
        dateNaissanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        dateNaissanceLabel.setTextFill(Color.web("#475569"));

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("jj/mm/aaaa");
        datePicker.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-font-size: 13px;");
        datePicker.setPrefWidth(Double.MAX_VALUE);
        dateNaissanceBox.getChildren().addAll(dateNaissanceLabel, datePicker);

        formBox.getChildren().addAll(formLabel, emailBox, dateNaissanceBox);

        Label confirmationMsg = new Label();
        confirmationMsg.setFont(Font.font("Arial", 12));
        confirmationMsg.setWrapText(true);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("Confirmer");
        confirmBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;");
        confirmBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                confirmationMsg.setText("⚠️ L'email de contact est obligatoire");
                confirmationMsg.setTextFill(Color.web("#ef4444"));
                return;
            }

            if (!isValidEmail(email)) {
                confirmationMsg.setText("⚠️ Format d'email invalide");
                confirmationMsg.setTextFill(Color.web("#ef4444"));
                return;
            }

            Integer age = null;
            if (datePicker.getValue() != null) {
                LocalDate birthDate = datePicker.getValue();
                int calculatedAge = LocalDate.now().getYear() - birthDate.getYear();
                if (calculatedAge < 1 || calculatedAge > 120) {
                    confirmationMsg.setText("⚠️ Âge invalide (1-120 ans)");
                    confirmationMsg.setTextFill(Color.web("#ef4444"));
                    return;
                }
                age = calculatedAge;
            }

            boolean success = participationService.participer(
                    event.getId_evenement(),
                    currentUser.getId(),
                    email,
                    age
            );

            if (success) {
                refreshData();
                dialog.close();

                // Notification à l'organisateur
                User organisateur = userService.getUserById(event.getId_organisateur());
                if (organisateur != null) {
                    notificationService.creerNotificationNouveauParticipant(
                            organisateur.getId(),
                            event.getId_evenement(),
                            event.getTitre(),
                            currentUser.getPrenom() + " " + currentUser.getNom(),
                            currentUser.getEmail()
                    );
                }

                // Notification à l'admin
                List<User> admins = userService.getUsersByRole("admin");
                for (User admin : admins) {
                    notificationService.creerNotificationParticipation(
                            admin.getId(),
                            event.getId_evenement(),
                            event.getTitre() + " - Nouveau participant: " + currentUser.getPrenom() + " " + currentUser.getNom()
                    );
                }

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✅ Vous êtes inscrit à l'événement !");
                successAlert.showAndWait();
            } else {
                if (participationService.isEventComplet(event.getId_evenement())) {
                    confirmationMsg.setText("❌ Événement complet");
                } else {
                    confirmationMsg.setText("❌ Erreur lors de l'inscription");
                }
                confirmationMsg.setTextFill(Color.web("#ef4444"));
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, confirmBtn);

        content.getChildren().addAll(header, infoBox, formBox, confirmationMsg, buttonBox);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showParticipationManagement(Event event) {
        Stage manageStage = new Stage();
        manageStage.setTitle("Gérer ma participation");
        manageStage.initModality(Modality.APPLICATION_MODAL);
        manageStage.setResizable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        content.setPrefWidth(500);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label("🎟️");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label("Ma participation");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#0f172a"));

        Label eventTitle = new Label(event.getTitre());
        eventTitle.setFont(Font.font("Segoe UI", 13));
        eventTitle.setTextFill(Color.web("#475569"));
        eventTitle.setWrapText(true);

        headerText.getChildren().addAll(titleLabel, eventTitle);
        header.getChildren().addAll(iconLabel, headerText);

        Participation participation = participationService.getParticipation(event.getId_evenement(), currentUser.getId());

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(12));
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;");

        Label infoTitle = new Label("📋 Informations actuelles");
        infoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        infoTitle.setTextFill(Color.web("#0f172a"));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);

        infoGrid.add(new Label("Email de contact:"), 0, 0);
        Label contactValue = new Label(participation.getContact());
        contactValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
        infoGrid.add(contactValue, 1, 0);

        if (participation.getAge() != null) {
            infoGrid.add(new Label("Âge:"), 0, 1);
            Label ageValue = new Label(participation.getAge() + " ans");
            ageValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
            infoGrid.add(ageValue, 1, 1);
        }

        infoGrid.add(new Label("Statut:"), 0, 2);
        Label statutLabel = new Label(participation.getStatut().toUpperCase());
        statutLabel.setStyle(getStatutStyle(participation.getStatut()));
        infoGrid.add(statutLabel, 1, 2);

        infoBox.getChildren().addAll(infoTitle, infoGrid);

        VBox modifyBox = new VBox(12);
        modifyBox.setPadding(new Insets(12));
        modifyBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        Label modifyTitle = new Label("✏️ Modifier mes informations");
        modifyTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        modifyTitle.setTextFill(Color.web("#0f172a"));

        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Email de contact");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web("#475569"));

        TextField emailField = new TextField(participation.getContact());
        emailField.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 8; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-font-size: 13px;");

        VBox dateNaissanceBox = new VBox(5);
        Label dateNaissanceLabel = new Label("Date de naissance");
        dateNaissanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dateNaissanceLabel.setTextFill(Color.web("#475569"));

        DatePicker datePicker = new DatePicker();
        if (participation.getAge() != null) {
            int birthYear = LocalDate.now().getYear() - participation.getAge();
            datePicker.setValue(LocalDate.of(birthYear, 1, 1));
        }
        datePicker.setPromptText("jj/mm/aaaa");
        datePicker.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 8; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-font-size: 13px;");
        datePicker.setPrefWidth(Double.MAX_VALUE);

        Button updateBtn = new Button("💾 Mettre à jour");
        updateBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showAlert(manageStage, "Erreur", "L'email ne peut pas être vide");
                return;
            }
            if (!isValidEmail(email)) {
                showAlert(manageStage, "Erreur", "Format d'email invalide");
                return;
            }

            Integer age = null;
            if (datePicker.getValue() != null) {
                int calculatedAge = LocalDate.now().getYear() - datePicker.getValue().getYear();
                if (calculatedAge < 1 || calculatedAge > 120) {
                    showAlert(manageStage, "Erreur", "Âge invalide (1-120)");
                    return;
                }
                age = calculatedAge;
            }

            boolean success = participationService.modifierParticipation(
                    event.getId_evenement(),
                    currentUser.getId(),
                    email,
                    age
            );

            if (success) {
                showAlert(manageStage, "Succès", "✅ Vos informations ont été mises à jour");
                manageStage.close();
                refreshData();
            }
        });

        emailBox.getChildren().addAll(emailLabel, emailField);
        dateNaissanceBox.getChildren().addAll(dateNaissanceLabel, datePicker);
        modifyBox.getChildren().addAll(modifyTitle, emailBox, dateNaissanceBox, updateBtn);

        VBox cancelBox = new VBox(12);
        cancelBox.setPadding(new Insets(12));
        cancelBox.setStyle("-fx-background-color: #fff5f5; -fx-background-radius: 8; " +
                "-fx-border-color: #fed7d7; -fx-border-radius: 8;");

        Label cancelTitle = new Label("⚠️ Annuler ma participation");
        cancelTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        cancelTitle.setTextFill(Color.web("#c53030"));

        Label cancelWarning = new Label("Cette action est irréversible. Vous ne pourrez plus participer à cet événement.");
        cancelWarning.setWrapText(true);
        cancelWarning.setFont(Font.font("Arial", 12));
        cancelWarning.setTextFill(Color.web("#718096"));

        Button cancelBtn = new Button("❌ Annuler ma participation");
        cancelBtn.setStyle("-fx-background-color: #c53030; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Annuler votre participation ?");
            confirm.setContentText("Êtes-vous sûr de vouloir annuler votre participation à cet événement ?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = participationService.annulerParticipation(
                            event.getId_evenement(),
                            currentUser.getId()
                    );

                    if (success) {
                        // Notification à l'organisateur
                        User organisateur = userService.getUserById(event.getId_organisateur());
                        if (organisateur != null) {
                            notificationService.creerNotificationParticipantAnnule(
                                    organisateur.getId(),
                                    event.getId_evenement(),
                                    event.getTitre(),
                                    currentUser.getPrenom() + " " + currentUser.getNom(),
                                    currentUser.getEmail()
                            );
                        }

                        // Notification à l'admin
                        List<User> admins = userService.getUsersByRole("admin");
                        for (User admin : admins) {
                            notificationService.creerNotificationAnnulation(
                                    admin.getId(),
                                    event.getId_evenement(),
                                    event.getTitre() + " - Participant: " + currentUser.getPrenom() + " " + currentUser.getNom()
                            );
                        }

                        showAlert(manageStage, "Succès", "✅ Participation annulée avec succès");
                        manageStage.close();
                        refreshData();
                    }
                }
            });
        });

        cancelBox.getChildren().addAll(cancelTitle, cancelWarning, cancelBtn);

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        closeBtn.setOnAction(e -> manageStage.close());

        VBox buttonBox = new VBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        content.getChildren().addAll(header, infoBox, modifyBox, cancelBox, buttonBox);

        Scene scene = new Scene(content);
        manageStage.setScene(scene);
        manageStage.show();
    }

    private String getStatutStyle(String statut) {
        switch (statut.toLowerCase()) {
            case "inscrit": return "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "present": return "-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "absent": return "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 12px;";
            default: return "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 12px;";
        }
    }

    private void showAlert(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showMyParticipations() {
        Stage participationsStage = new Stage();
        participationsStage.setTitle("Mes participations");
        participationsStage.setResizable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        content.setPrefWidth(650);
        content.setPrefHeight(500);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label("👥");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label("Mes participations");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#0f172a"));

        Label subtitleLabel = new Label("Liste des événements auxquels vous participez");
        subtitleLabel.setFont(Font.font("Segoe UI", 12));
        subtitleLabel.setTextFill(Color.web("#475569"));

        headerText.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconLabel, headerText);

        List<Participation> participations = participationService.getParticipationsByUser(currentUser.getId());

        if (participations.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));

            Label emptyIcon = new Label("📅");
            emptyIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));

            Label emptyText = new Label("Vous n'avez pas encore de participations");
            emptyText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            emptyText.setTextFill(Color.web("#1e293b"));

            Label emptySub = new Label("Explorez les événements et inscrivez-vous !");
            emptySub.setFont(Font.font("Segoe UI", 14));
            emptySub.setTextFill(Color.web("#64748b"));

            emptyBox.getChildren().addAll(emptyIcon, emptyText, emptySub);
            content.getChildren().addAll(header, emptyBox);
        } else {
            ListView<Participation> participationList = new ListView<>();
            participationList.setPrefHeight(350);
            participationList.setCellFactory(lv -> new ListCell<Participation>() {
                @Override
                protected void updateItem(Participation p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox cell = new VBox(8);
                        cell.setPadding(new Insets(12));
                        cell.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; " +
                                "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");

                        HBox headerCell = new HBox(10);
                        headerCell.setAlignment(Pos.CENTER_LEFT);

                        Label eventName = new Label(p.getEventTitre() != null ? p.getEventTitre() : "Événement");
                        eventName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                        eventName.setTextFill(Color.web("#0f172a"));

                        String statusColor = p.getStatut().equals("inscrit") ? "#3b82f6" :
                                p.getStatut().equals("present") ? "#10b981" : "#ef4444";
                        Label statusLabel = new Label(p.getStatut().toUpperCase());
                        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                        statusLabel.setTextFill(Color.WHITE);
                        statusLabel.setPadding(new Insets(4, 10, 4, 10));
                        statusLabel.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 15;");

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        headerCell.getChildren().addAll(eventName, spacer, statusLabel);

                        HBox infoCell = new HBox(20);
                        infoCell.setAlignment(Pos.CENTER_LEFT);

                        Label dateLabel = new Label("📅 " + p.getFormattedDate());
                        dateLabel.setFont(Font.font(11));
                        dateLabel.setTextFill(Color.web("#64748b"));

                        Label lieuLabel = new Label("📍 " + (p.getEventLieu() != null ? p.getEventLieu() : "Non spécifié"));
                        lieuLabel.setFont(Font.font(11));
                        lieuLabel.setTextFill(Color.web("#64748b"));

                        infoCell.getChildren().addAll(dateLabel, lieuLabel);

                        Button gererBtn = new Button("Gérer");
                        gererBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
                        gererBtn.setOnAction(e -> {
                            Event event = eventService.getEventById(p.getIdEvenement());
                            participationsStage.close();
                            showParticipationManagement(event);
                        });

                        HBox actionCell = new HBox(10);
                        actionCell.setAlignment(Pos.CENTER_RIGHT);
                        actionCell.getChildren().add(gererBtn);

                        cell.getChildren().addAll(headerCell, infoCell, actionCell);
                        setGraphic(cell);
                    }
                }
            });

            participationList.getItems().addAll(participations);
            content.getChildren().addAll(header, participationList);
        }

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;");
        closeBtn.setOnAction(e -> participationsStage.close());

        VBox buttonBox = new VBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        content.getChildren().add(buttonBox);

        Scene scene = new Scene(content);
        participationsStage.setScene(scene);
        participationsStage.initOwner(mainLayout.getScene().getWindow());
        participationsStage.show();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
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