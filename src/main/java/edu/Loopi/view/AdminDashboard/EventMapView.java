package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import edu.Loopi.services.EventService;
import edu.Loopi.services.GeocodingService;
import edu.Loopi.services.UserService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class EventMapView {
    private User currentUser;
    private UserService userService;
    private EventService eventService;
    private AdminDashboard adminDashboard;
    private GeocodingService geocodingService;

    // Composants UI
    private WebView webView;
    private WebEngine webEngine;
    private StackPane mapContainer;
    private StackPane storyPreviewPane;
    private VBox storyCard;
    private Label storyTitleLabel;
    private Label storyDateLabel;
    private Label storyLocationLabel;
    private Label storyParticipantsLabel;
    private Label storyOrganizerLabel;
    private ImageView storyImageView;
    private ProgressIndicator storyLoadingIndicator;
    private Map<Integer, Event> eventCache = new HashMap<>();
    private Event currentHoverEvent = null;
    private ComboBox<String> mapStyleSelector;
    private Slider zoomSlider;
    private Label coordinatesLabel;
    private Label totalEventsLabel;
    private ToggleGroup filterGroup;
    private boolean mapReady = false;
    private ProgressIndicator loadingIndicator;
    private List<Event> allEventsWithCoords = new ArrayList<>();

    // Timer pour le rafraîchissement automatique
    private PauseTransition autoRefreshTimer;

    // Constantes
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String IMAGE_STORAGE_DIR = "src" + File.separator + "main" + File.separator +
            "resources" + File.separator + "uploads" + File.separator +
            "events" + File.separator;
    private static final String FULL_IMAGE_PATH = PROJECT_ROOT + File.separator + IMAGE_STORAGE_DIR;

    public EventMapView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
        this.eventService = new EventService();
        this.geocodingService = new GeocodingService();
    }

    public void showEventMapView(StackPane mainContentArea, boolean isDarkMode) {
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: " + adminDashboard.getBgColor() + ";");

        VBox topSection = createTopBar(isDarkMode);
        mainContainer.setTop(topSection);

        StackPane centerStack = createMapView(isDarkMode);
        mainContainer.setCenter(centerStack);

        HBox bottomSection = createBottomInfoBar(isDarkMode);
        mainContainer.setBottom(bottomSection);

        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(mainContainer);

        loadMap();

        // Démarrer le rafraîchissement automatique
        setupAutoRefresh();
    }

    private void setupAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }

        autoRefreshTimer = new PauseTransition(Duration.seconds(10));
        autoRefreshTimer.setOnFinished(e -> {
            if (mapReady) {
                refreshMap();
            }
            autoRefreshTimer.play();
        });
        autoRefreshTimer.play();
    }

    private void refreshMap() {
        Platform.runLater(() -> {
            // Nettoyer les anciens marqueurs
            try {
                if (mapReady) {
                    webEngine.executeScript(
                            "if (allMarkers) { allMarkers.forEach(function(m) { if (map.hasLayer(m)) map.removeLayer(m); }); } " +
                                    "allMarkers = []; markers = {};"
                    );
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Erreur nettoyage marqueurs: " + ex.getMessage());
            }

            // Recharger les événements
            loadEvents();
        });
    }

    private VBox createTopBar(boolean isDarkMode) {
        VBox topBar = new VBox(10);
        topBar.setPadding(new Insets(15, 24, 15, 24));
        topBar.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "white") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-width: 0 0 1 0;");

        HBox firstRow = new HBox();
        firstRow.setAlignment(Pos.CENTER_LEFT);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label mapIcon = new Label("🗺️");
        mapIcon.setFont(Font.font("System", 28));

        Label title = new Label("Carte des Événements");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        totalEventsLabel = new Label("(0 événements)");
        totalEventsLabel.setFont(Font.font("System", 14));
        totalEventsLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        titleBox.getChildren().addAll(mapIcon, title, totalEventsLabel);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Contrôles de zoom
        HBox zoomControls = new HBox(10);
        zoomControls.setAlignment(Pos.CENTER_RIGHT);

        Button zoomInBtn = new Button("+");
        zoomInBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-size: 16px; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 18; -fx-cursor: hand;");
        zoomInBtn.setOnAction(e -> {
            if (mapReady) webEngine.executeScript("if(window.map) map.zoomIn()");
        });

        zoomSlider = new Slider(1, 18, 8);
        zoomSlider.setPrefWidth(150);
        zoomSlider.valueProperty().addListener((obs, old, val) -> {
            if (mapReady) webEngine.executeScript("if(window.map) map.setZoom(" + val.intValue() + ")");
        });

        Button zoomOutBtn = new Button("-");
        zoomOutBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-size: 16px; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 18; -fx-cursor: hand;");
        zoomOutBtn.setOnAction(e -> {
            if (mapReady) webEngine.executeScript("if(window.map) map.zoomOut()");
        });

        zoomControls.getChildren().addAll(zoomInBtn, zoomSlider, zoomOutBtn);

        firstRow.getChildren().addAll(titleBox, spacer1, zoomControls);

        // Deuxième ligne
        HBox secondRow = new HBox(15);
        secondRow.setAlignment(Pos.CENTER_LEFT);
        secondRow.setPadding(new Insets(10, 0, 0, 0));

        Label styleLabel = new Label("Style:");
        styleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        styleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        mapStyleSelector = new ComboBox<>();
        mapStyleSelector.getItems().addAll("Standard", "Sombre", "Satellite");
        mapStyleSelector.setValue("Standard");
        mapStyleSelector.setOnAction(e -> {
            if (mapReady) changeMapStyle(mapStyleSelector.getValue());
        });

        Label filterLabel = new Label("Filtrer:");
        filterLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        filterLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        filterGroup = new ToggleGroup();

        ToggleButton allBtn = createFilterToggle("Tous", true);
        ToggleButton upcomingBtn = createFilterToggle("À venir", false);
        ToggleButton ongoingBtn = createFilterToggle("En cours", false);
        ToggleButton pastBtn = createFilterToggle("Passés", false);

        allBtn.setToggleGroup(filterGroup);
        upcomingBtn.setToggleGroup(filterGroup);
        ongoingBtn.setToggleGroup(filterGroup);
        pastBtn.setToggleGroup(filterGroup);

        allBtn.setOnAction(e -> filterEvents("all"));
        upcomingBtn.setOnAction(e -> filterEvents("upcoming"));
        ongoingBtn.setOnAction(e -> filterEvents("ongoing"));
        pastBtn.setOnAction(e -> filterEvents("past"));

        HBox filterButtons = new HBox(5);
        filterButtons.getChildren().addAll(allBtn, upcomingBtn, ongoingBtn, pastBtn);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Bouton de rafraîchissement manuel
        Button refreshMapBtn = new Button("🔄");
        refreshMapBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-size: 16px; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 18; -fx-cursor: hand;");
        refreshMapBtn.setTooltip(new Tooltip("Rafraîchir la carte"));
        refreshMapBtn.setOnAction(e -> {
            if (mapReady) {
                refreshMap();
            }
        });

        Button fitBoundsBtn = new Button("Ajuster la vue");
        fitBoundsBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-cursor: hand;");
        fitBoundsBtn.setOnAction(e -> {
            if (mapReady) webEngine.executeScript("if(typeof fitMapToEvents === 'function') fitMapToEvents()");
        });

        secondRow.getChildren().addAll(
                styleLabel, mapStyleSelector,
                filterLabel, filterButtons,
                spacer2, refreshMapBtn, fitBoundsBtn
        );

        topBar.getChildren().addAll(firstRow, secondRow);
        return topBar;
    }

    private ToggleButton createFilterToggle(String text, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.setSelected(selected);
        btn.setStyle("-fx-background-color: " + (selected ? "#3182CE" : "transparent") +
                "; -fx-text-fill: " + (selected ? "white" : adminDashboard.getTextColor()) +
                "; -fx-border-color: #3182CE; -fx-border-radius: 20; -fx-background-radius: 20; " +
                "-fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");

        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            btn.setStyle("-fx-background-color: " + (newVal ? "#3182CE" : "transparent") +
                    "; -fx-text-fill: " + (newVal ? "white" : adminDashboard.getTextColor()) +
                    "; -fx-border-color: #3182CE; -fx-border-radius: 20; -fx-background-radius: 20; " +
                    "-fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        });

        return btn;
    }

    private HBox createBottomInfoBar(boolean isDarkMode) {
        HBox bottomBar = new HBox(20);
        bottomBar.setPadding(new Insets(10, 24, 15, 24));
        bottomBar.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "white"));
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        coordinatesLabel = new Label("Position: --");
        coordinatesLabel.setFont(Font.font("System", 12));
        coordinatesLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER_RIGHT);

        VBox legendItem1 = createLegendItem("●", "À venir", "#F39C12");
        VBox legendItem2 = createLegendItem("●", "En cours", "#9B59B6");
        VBox legendItem3 = createLegendItem("●", "Passés", "#6C757D");

        legend.getChildren().addAll(legendItem1, legendItem2, legendItem3);

        bottomBar.getChildren().addAll(coordinatesLabel, spacer, legend);
        return bottomBar;
    }

    private VBox createLegendItem(String icon, String text, String color) {
        VBox item = new VBox(2);
        item.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 16));
        iconLabel.setTextFill(Color.web(color));

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("System", 10));
        textLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        item.getChildren().addAll(iconLabel, textLabel);
        return item;
    }

    private StackPane createMapView(boolean isDarkMode) {
        StackPane stack = new StackPane();
        stack.setPadding(new Insets(20, 24, 10, 24));

        mapContainer = new StackPane();
        mapContainer.setStyle("-fx-background-color: " + adminDashboard.getCardBg() +
                "; -fx-background-radius: 16; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 16;");

        webView = new WebView();
        webView.setPrefHeight(600);
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(100, 100);

        mapContainer.getChildren().addAll(webView, loadingIndicator);
        StackPane.setAlignment(loadingIndicator, Pos.CENTER);

        storyPreviewPane = new StackPane();
        storyPreviewPane.setVisible(false);
        storyPreviewPane.setManaged(false);
        storyPreviewPane.setPadding(new Insets(20));

        storyCard = createStoryCard(isDarkMode);
        storyPreviewPane.getChildren().add(storyCard);

        StackPane.setAlignment(storyPreviewPane, Pos.BOTTOM_LEFT);
        StackPane.setMargin(storyPreviewPane, new Insets(0, 0, 30, 30));

        stack.getChildren().addAll(mapContainer, storyPreviewPane);
        return stack;
    }

    private VBox createStoryCard(boolean isDarkMode) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "white") +
                "; -fx-background-radius: 20; -fx-border-color: " + adminDashboard.getAccentColor() +
                "; -fx-border-width: 2; -fx-border-radius: 20;");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: " + (isDarkMode ? "#1A202C" : "#f0f0f0") +
                "; -fx-background-radius: 15;");

        storyImageView = new ImageView();
        storyImageView.setFitWidth(280);
        storyImageView.setFitHeight(180);
        storyImageView.setPreserveRatio(true);

        // Appliquer un cercle de clipping pour l'image
        Circle imageClip = new Circle(90, 90, 90);
        storyImageView.setClip(imageClip);

        storyImageView.setStyle("-fx-background-radius: 15;");

        storyLoadingIndicator = new ProgressIndicator();
        storyLoadingIndicator.setVisible(false);
        storyLoadingIndicator.setPrefSize(40, 40);

        imageContainer.getChildren().addAll(storyImageView, storyLoadingIndicator);

        VBox textContent = new VBox(8);

        storyTitleLabel = new Label();
        storyTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        storyTitleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));
        storyTitleLabel.setWrapText(true);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);

        storyDateLabel = new Label();
        storyDateLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
        storyDateLabel.setWrapText(true);
        infoGrid.add(new Label("📅"), 0, 0);
        infoGrid.add(storyDateLabel, 1, 0);

        storyLocationLabel = new Label();
        storyLocationLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
        storyLocationLabel.setWrapText(true);
        infoGrid.add(new Label("📍"), 0, 1);
        infoGrid.add(storyLocationLabel, 1, 1);

        storyParticipantsLabel = new Label();
        storyParticipantsLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
        infoGrid.add(new Label("👥"), 0, 2);
        infoGrid.add(storyParticipantsLabel, 1, 2);

        storyOrganizerLabel = new Label();
        storyOrganizerLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
        storyOrganizerLabel.setWrapText(true);
        infoGrid.add(new Label("👤"), 0, 3);
        infoGrid.add(storyOrganizerLabel, 1, 3);

        Button viewStoryBtn = new Button("👁️ Voir les détails");
        viewStoryBtn.setMaxWidth(Double.MAX_VALUE);
        viewStoryBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand;");
        viewStoryBtn.setOnAction(e -> {
            if (currentHoverEvent != null) {
                showEventDetails(currentHoverEvent);
            }
        });

        textContent.getChildren().addAll(storyTitleLabel, infoGrid, viewStoryBtn);
        card.getChildren().addAll(imageContainer, textContent);

        FadeTransition ft = new FadeTransition(Duration.millis(300), card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        ScaleTransition st = new ScaleTransition(Duration.millis(300), card);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1);
        st.setToY(1);

        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.play();

        return card;
    }

    private void loadMap() {
        loadingIndicator.setVisible(true);

        String mapHtml = generateMapHtml();
        webEngine.loadContent(mapHtml);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadingIndicator.setVisible(false);
                mapReady = true;
                System.out.println("✅ Carte chargée");

                setupJavaBridge();

                Platform.runLater(() -> {
                    loadEvents();
                });
            }
        });
    }

    private void loadEvents() {
        // Utiliser refreshEvents pour forcer le rechargement depuis la base
        List<Event> allEvents = eventService.refreshEvents();
        allEventsWithCoords.clear();
        eventCache.clear();

        int geocoded = 0;

        for (Event event : allEvents) {
            if ("approuve".equals(event.getStatutValidation())) {
                // Si l'événement n'a pas de coordonnées, essayer de les géocoder
                if (!event.hasCoordinates()) {
                    System.out.println("📍 Géocodage de: " + event.getLieu() + " pour " + event.getTitre());
                    double[] coords = geocodingService.geocodeAddress(event.getLieu());
                    if (coords != null) {
                        event.setLatitude(coords[0]);
                        event.setLongitude(coords[1]);
                        // Sauvegarder en base de données
                        eventService.updateEvent(event);
                        geocoded++;
                        System.out.println("✅ Coordonnées trouvées: " + coords[0] + ", " + coords[1]);
                    }
                }

                if (event.hasCoordinates()) {
                    allEventsWithCoords.add(event);
                    eventCache.put(event.getId_evenement(), event);
                }
            }
        }

        int eventCount = allEventsWithCoords.size();
        System.out.println("📊 " + eventCount + " événements avec coordonnées trouvés");
        if (geocoded > 0) {
            System.out.println("🆕 " + geocoded + " nouveaux événements géocodés");
        }

        // Nettoyer les anciens marqueurs avant d'ajouter les nouveaux
        if (mapReady) {
            try {
                webEngine.executeScript(
                        "if (allMarkers) { allMarkers.forEach(function(m) { if (map.hasLayer(m)) map.removeLayer(m); }); } " +
                                "allMarkers = []; markers = {};"
                );
            } catch (Exception e) {
                System.err.println("⚠️ Erreur nettoyage marqueurs: " + e.getMessage());
            }
        }

        // Ajouter les événements à la carte
        for (Event event : allEventsWithCoords) {
            addEventMarker(event);
        }

        Platform.runLater(() -> {
            totalEventsLabel.setText("(" + eventCount + " événements)");
        });
    }

    private String sanitizeForJS(String text) {
        if (text == null) return "";
        // Remplacer les caractères problématiques
        return text.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ä", "a")
                .replace("ï", "i")
                .replace("î", "i")
                .replace("ô", "o")
                .replace("ö", "o")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ü", "u")
                .replace("ç", "c")
                .replace("ÿ", "y")
                .replace("œ", "oe")
                .replace("æ", "ae");
    }

    private String encodeForUrl(String text) {
        if (text == null) return "";
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return text.replace(" ", "+");
        }
    }

    private void addEventMarker(Event event) {
        if (!mapReady || !event.hasCoordinates()) return;

        double lat = event.getLatitude();
        double lng = event.getLongitude();

        String imageUrl = getEventImageUrl(event);
        String statut = event.getStatut() != null ? event.getStatut() : "Inconnu";

        // Nettoyer le statut pour éviter les caractères spéciaux
        if (statut.equals("À venir")) statut = "A venir";

        String description = event.getDescription() != null ? event.getDescription() : "";
        if (description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }

        String safeTitle = sanitizeForJS(event.getTitre() != null ? event.getTitre() : "Sans titre");
        String safeDate = sanitizeForJS(event.getFormattedDate() != null ? event.getFormattedDate() : "Date inconnue");
        String safeLieu = sanitizeForJS(event.getLieu() != null ? event.getLieu() : "Lieu inconnu");
        String safeDesc = sanitizeForJS(description);
        String safeImageUrl = imageUrl.replace("'", "\\'");

        String script = String.format(Locale.US,
                "try { addEventMarker(%d, '%s', %f, %f, '%s', '%s', '%s', '%s', %d, '%s'); } catch(e) { console.log('Erreur: '+e); }",
                event.getId_evenement(),
                safeTitle,
                lat,
                lng,
                safeDate,
                safeLieu,
                safeDesc,
                statut,
                event.getParticipantsCount(),
                safeImageUrl
        );

        try {
            webEngine.executeScript(script);
            System.out.println("📍 Marqueur ajouté: " + event.getTitre() + " (" + lat + ", " + lng + ")");
        } catch (Exception e) {
            System.err.println("⚠️ Erreur ajout marqueur: " + e.getMessage());
        }
    }

    private String getEventImageUrl(Event event) {
        // Vérifier si l'image existe localement
        if (event.getImage_evenement() != null && !event.getImage_evenement().isEmpty()) {
            String fileName = event.getImage_evenement().substring(event.getImage_evenement().lastIndexOf('/') + 1);
            File imgFile = new File(FULL_IMAGE_PATH + fileName);
            if (imgFile.exists()) {
                return "file:///" + imgFile.getAbsolutePath().replace("\\", "/");
            }
        }

        // Image par défaut avec le titre de l'événement encodé proprement
        String title = event.getTitre() != null ? event.getTitre() : "Event";
        String encodedTitle = encodeForUrl(title);
        return "https://via.placeholder.com/300x150/3182ce/ffffff?text=" + encodedTitle;
    }

    private void changeMapStyle(String style) {
        String url = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
        if ("Sombre".equals(style)) {
            url = "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png";
        } else if ("Satellite".equals(style)) {
            url = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}";
        }
        final String finalUrl = url;
        webEngine.executeScript("if (typeof changeMapStyle === 'function') changeMapStyle('" + finalUrl + "')");
    }

    private void filterEvents(String type) {
        final String finalType = type;
        webEngine.executeScript("if (typeof filterEvents === 'function') filterEvents('" + finalType + "')");
    }

    private String generateMapHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    body { margin:0; padding:0; }
                    #map { width:100%; height:600px; background:#1a1a1a; }
                    .event-marker {
                        transition: transform 0.2s;
                        cursor: pointer;
                        z-index: 10;
                    }
                    .event-marker:hover {
                        transform: scale(1.2);
                        z-index: 1000;
                    }
                    .marker-pulse {
                        animation: pulse 2s infinite;
                    }
                    @keyframes pulse {
                        0% { transform: scale(1); }
                        50% { transform: scale(1.1); }
                        100% { transform: scale(1); }
                    }
                    .custom-popup .leaflet-popup-content-wrapper {
                        border-radius: 12px;
                        overflow: hidden;
                        box-shadow: 0 4px 15px rgba(0,0,0,0.2);
                    }
                    .custom-popup .leaflet-popup-content {
                        margin: 0;
                        width: 300px;
                    }
                    .popup-image {
                        width: 100%;
                        height: 150px;
                        object-fit: cover;
                        background: #f0f0f0;
                        border-radius: 12px 12px 0 0;
                    }
                    .popup-content {
                        padding: 15px;
                    }
                    .popup-title {
                        font-size: 18px;
                        font-weight: bold;
                        margin-bottom: 8px;
                        color: #333;
                    }
                    .popup-info {
                        margin: 5px 0;
                        color: #666;
                        font-size: 13px;
                        display: flex;
                        align-items: center;
                        gap: 5px;
                    }
                    .popup-badge {
                        display: inline-block;
                        padding: 4px 12px;
                        border-radius: 20px;
                        font-size: 11px;
                        font-weight: bold;
                        color: white;
                        margin-top: 8px;
                    }
                    .popup-footer {
                        margin-top: 12px;
                        text-align: center;
                    }
                    .view-details-btn {
                        background: #3182CE;
                        color: white;
                        border: none;
                        padding: 10px;
                        border-radius: 6px;
                        width: 100%;
                        cursor: pointer;
                        font-weight: bold;
                        transition: background 0.2s;
                    }
                    .view-details-btn:hover {
                        background: #2c5282;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([36.8065, 10.1815], 8);
                    
                    var currentLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap'
                    }).addTo(map);
                    
                    var markers = {};
                    var allMarkers = [];
                    
                    function changeMapStyle(url) {
                        if (currentLayer) map.removeLayer(currentLayer);
                        currentLayer = L.tileLayer(url, {attribution: '© OpenStreetMap'}).addTo(map);
                    }
                    
                    function createIcon(color, isHovered) {
                        var size = isHovered ? 50 : 40;
                        return L.divIcon({
                            className: 'event-marker' + (isHovered ? '' : ' marker-pulse'),
                            html: '<div style="background:'+color+'; width:'+size+'px; height:'+size+'px; border-radius:50%; border:3px solid white; display:flex; align-items:center; justify-content:center; box-shadow:0 2px 10px rgba(0,0,0,0.3);">📍</div>',
                            iconSize: [size, size],
                            iconAnchor: [size/2, size/2]
                        });
                    }
                    
                    function addEventMarker(id, title, lat, lng, date, lieu, description, statut, participants, imageUrl) {
                        try {
                            var color = statut === 'A venir' ? '#f39c12' : 
                                       statut === 'En cours' ? '#9b59b6' : '#6c757d';
                            
                            var marker = L.marker([lat, lng], {
                                icon: createIcon(color, false),
                                eventData: {
                                    id: id,
                                    title: title,
                                    date: date,
                                    lieu: lieu,
                                    participants: participants,
                                    imageUrl: imageUrl,
                                    statut: statut
                                }
                            }).addTo(map);
                            
                            allMarkers.push(marker);
                            
                            var popupContent = '<div>' +
                                '<img src="'+imageUrl+'" class="popup-image" onerror="this.src=\\'https://via.placeholder.com/300x150/3182ce/ffffff?text=LOOPI\\'">' +
                                '<div class="popup-content">' +
                                    '<div class="popup-title">'+title+'</div>' +
                                    '<div class="popup-info">📅 '+date+'</div>' +
                                    '<div class="popup-info">📍 '+lieu+'</div>' +
                                    '<div class="popup-info">👥 '+participants+' participants</div>' +
                                    '<span class="popup-badge" style="background:'+color+'">'+statut+'</span>' +
                                    '<div class="popup-footer">' +
                                        '<button class="view-details-btn" onclick="viewDetails('+id+')">👁️ Voir détails</button>' +
                                    '</div>' +
                                '</div>' +
                            '</div>';
                            
                            marker.bindPopup(popupContent, {
                                className: 'custom-popup',
                                minWidth: 300,
                                maxWidth: 300,
                                autoPan: true,
                                autoPanPadding: [50, 50]
                            });
                            
                            marker.on('mouseover', function() {
                                this.setIcon(createIcon(color, true));
                                var d = this.options.eventData;
                                if (window.javaApp) {
                                    window.javaApp.showPreview(
                                        d.id, d.title, d.date, d.lieu, d.participants, d.imageUrl
                                    );
                                }
                            });
                            
                            marker.on('mouseout', function() {
                                this.setIcon(createIcon(color, false));
                                if (window.javaApp) window.javaApp.hidePreview();
                            });
                            
                            marker.on('click', function() {
                                this.openPopup();
                            });
                            
                            markers[id] = marker;
                            
                            console.log('Marqueur ajouté: ' + title + ' à ' + lat + ',' + lng);
                        } catch(e) {
                            console.log('Erreur création marqueur: '+e);
                        }
                    }
                    
                    function filterEvents(type) {
                        allMarkers.forEach(function(m) {
                            var data = m.options.eventData;
                            var show = type === 'all' ? true :
                                      type === 'upcoming' ? data.statut === 'A venir' :
                                      type === 'ongoing' ? data.statut === 'En cours' :
                                      type === 'past' ? data.statut === 'Passé' : true;
                            if (show) {
                                if (!map.hasLayer(m)) map.addLayer(m);
                            } else {
                                if (map.hasLayer(m)) map.removeLayer(m);
                            }
                        });
                    }
                    
                    function fitMapToEvents() {
                        if (allMarkers.length > 0) {
                            var group = L.featureGroup(allMarkers);
                            map.fitBounds(group.getBounds(), { padding: [50, 50] });
                            console.log('Vue ajustée à ' + allMarkers.length + ' événements');
                        } else {
                            map.setView([36.8065, 10.1815], 8);
                        }
                    }
                    
                    function viewDetails(id) {
                        if (window.javaApp) window.javaApp.viewDetails(id);
                    }
                    
                    map.on('moveend', function() {
                        var c = map.getCenter();
                        if (window.javaApp) window.javaApp.updateCoords(c.lat, c.lng);
                    });
                    
                    setTimeout(function() {
                        if (window.javaApp) window.javaApp.onMapReady();
                        console.log('✅ Carte prête');
                    }, 500);
                </script>
            </body>
            </html>
            """;
    }

    private void setupJavaBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaApp", new JavaConnector());
            System.out.println("✅ Pont Java-JavaScript établi");
        } catch (Exception e) {
            System.err.println("⚠️ Erreur pont: " + e.getMessage());
        }
    }

    public class JavaConnector {
        public void onMapReady() {
            Platform.runLater(() -> System.out.println("✅ Carte prête côté JavaScript"));
        }

        public void showPreview(int id, String title, String date, String lieu,
                                int participants, String imageUrl) {
            Platform.runLater(() -> {
                Event event = eventCache.get(id);
                if (event == null) {
                    event = eventService.getEventById(id);
                    if (event != null) eventCache.put(id, event);
                }

                if (event != null) {
                    currentHoverEvent = event;
                    storyTitleLabel.setText(title);
                    storyDateLabel.setText(date);
                    storyLocationLabel.setText(lieu);
                    storyParticipantsLabel.setText(participants + " participants");

                    User org = userService.getUserById(event.getId_organisateur());
                    storyOrganizerLabel.setText(org != null ? org.getNomComplet() : "Inconnu");

                    storyLoadingIndicator.setVisible(true);
                    storyImageView.setVisible(false);

                    try {
                        final String finalImageUrl = imageUrl;
                        Image image = new Image(finalImageUrl, true);
                        image.progressProperty().addListener((obs, prog, newProg) -> {
                            if (newProg.doubleValue() >= 1.0) {
                                storyLoadingIndicator.setVisible(false);
                                storyImageView.setVisible(true);
                                storyImageView.setImage(image);
                            }
                        });

                        image.errorProperty().addListener((obs, oldErr, newErr) -> {
                            storyLoadingIndicator.setVisible(false);
                            storyImageView.setVisible(true);
                        });
                    } catch (Exception e) {
                        storyLoadingIndicator.setVisible(false);
                        storyImageView.setVisible(true);
                    }

                    storyPreviewPane.setVisible(true);
                }
            });
        }

        public void hidePreview() {
            Platform.runLater(() -> {
                storyPreviewPane.setVisible(false);
                currentHoverEvent = null;
            });
        }

        public void viewDetails(int id) {
            Platform.runLater(() -> {
                Event event = eventCache.get(id);
                if (event == null) {
                    event = eventService.getEventById(id);
                }
                if (event != null) showEventDetails(event);
            });
        }

        public void updateCoords(double lat, double lng) {
            Platform.runLater(() -> {
                String dirLat = lat >= 0 ? "N" : "S";
                String dirLng = lng >= 0 ? "E" : "W";
                coordinatesLabel.setText(String.format("%.4f° %s, %.4f° %s",
                        Math.abs(lat), dirLat, Math.abs(lng), dirLng));
            });
        }
    }

    private void showEventDetails(Event event) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(adminDashboard.getPrimaryStage());
        stage.setTitle("Détails - " + event.getTitre());

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + "; -fx-background-radius: 12;");

        // Image avec cercle
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(200, 200);
        imageContainer.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "20; -fx-background-radius: 100;");
        imageContainer.setAlignment(Pos.CENTER);

        ImageView imgView = new ImageView();
        imgView.setFitWidth(180);
        imgView.setFitHeight(180);
        imgView.setPreserveRatio(true);

        // Cercle de clipping pour l'image
        Circle clipCircle = new Circle(90, 90, 90);
        imgView.setClip(clipCircle);

        String imgUrl = getEventImageUrl(event);
        if (imgUrl != null) {
            try {
                Image img = new Image(imgUrl, true);
                imgView.setImage(img);
            } catch (Exception e) {
                // Image par défaut si erreur
                imgView.setImage(new Image("https://via.placeholder.com/180x180/3182ce/ffffff?text=" +
                        encodeForUrl(event.getTitre())));
            }
        } else {
            imgView.setImage(new Image("https://via.placeholder.com/180x180/3182ce/ffffff?text=" +
                    encodeForUrl(event.getTitre())));
        }

        imageContainer.getChildren().add(imgView);

        // Grille d'informations
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        // Ligne 0: Titre
        Label titleLabel = new Label(event.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        grid.add(titleLabel, 0, 0, 2, 1);
        GridPane.setHalignment(titleLabel, javafx.geometry.HPos.CENTER);

        // Ligne 1: Date
        Label dateIcon = new Label("📅");
        dateIcon.setFont(Font.font("System", 16));
        Label dateValue = new Label(event.getFormattedDate() != null ? event.getFormattedDate() : "Non définie");
        dateValue.setWrapText(true);
        dateValue.setTextFill(Color.web(adminDashboard.getTextColor()));
        grid.add(dateIcon, 0, 1);
        grid.add(dateValue, 1, 1);

        // Ligne 2: Lieu
        Label lieuIcon = new Label("📍");
        lieuIcon.setFont(Font.font("System", 16));
        Label lieuValue = new Label(event.getLieu() != null ? event.getLieu() : "Non défini");
        lieuValue.setWrapText(true);
        lieuValue.setTextFill(Color.web(adminDashboard.getTextColor()));
        grid.add(lieuIcon, 0, 2);
        grid.add(lieuValue, 1, 2);

        // Ligne 3: Coordonnées
        if (event.hasCoordinates()) {
            Label coordIcon = new Label("🌍");
            coordIcon.setFont(Font.font("System", 16));
            Label coordValue = new Label(String.format("%.4f, %.4f", event.getLatitude(), event.getLongitude()));
            coordValue.setTextFill(Color.web(adminDashboard.getTextColor()));
            grid.add(coordIcon, 0, 3);
            grid.add(coordValue, 1, 3);
        }

        // Ligne 4: Participants
        Label partIcon = new Label("👥");
        partIcon.setFont(Font.font("System", 16));
        Label partValue = new Label(event.getParticipantsCount() + " inscrits");
        partValue.setTextFill(Color.web(adminDashboard.getTextColor()));
        grid.add(partIcon, 0, 4);
        grid.add(partValue, 1, 4);

        // Ligne 5: Capacité
        if (event.getCapacite_max() != null) {
            Label capIcon = new Label("📊");
            capIcon.setFont(Font.font("System", 16));
            Label capValue = new Label(event.getCapacite_max() + " places");
            capValue.setTextFill(Color.web(adminDashboard.getTextColor()));
            grid.add(capIcon, 0, 5);
            grid.add(capValue, 1, 5);
        }

        // Ligne 6: Organisateur
        Label orgIcon = new Label("👤");
        orgIcon.setFont(Font.font("System", 16));
        User org = userService.getUserById(event.getId_organisateur());
        Label orgValue = new Label(org != null ? org.getNomComplet() : "Inconnu");
        orgValue.setWrapText(true);
        orgValue.setTextFill(Color.web(adminDashboard.getTextColor()));
        grid.add(orgIcon, 0, 6);
        grid.add(orgValue, 1, 6);

        // Ligne 7: Statut
        Label statutIcon = new Label("📌");
        statutIcon.setFont(Font.font("System", 16));
        String statut = event.getStatut() != null ? event.getStatut() : "Inconnu";
        Label statutLabel = new Label(statut);
        String statutColor = statut.equals("À venir") ? "#f39c12" :
                statut.equals("En cours") ? "#9b59b6" : "#6c757d";
        statutLabel.setStyle("-fx-background-color: " + statutColor +
                "; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 15;");
        grid.add(statutIcon, 0, 7);
        grid.add(statutLabel, 1, 7);

        // Ligne 8: Description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Label descIcon = new Label("📝");
            descIcon.setFont(Font.font("System", 16));
            Label descLabel = new Label(event.getDescription());
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(350);
            descLabel.setTextFill(Color.web(adminDashboard.getTextColor()));
            grid.add(descIcon, 0, 8);
            grid.add(descLabel, 1, 8);
        }

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(50);
        col1.setHalignment(javafx.geometry.HPos.CENTER);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(380);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // Bouton Fermer
        Button closeBtn = new Button("Fermer");
        closeBtn.setMaxWidth(200);
        closeBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        VBox buttonBox = new VBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        content.getChildren().addAll(imageContainer, grid, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Scene scene = new Scene(scrollPane, 550, 700);
        stage.setScene(scene);
        stage.show();
    }

    private String escapeJS(String text) {
        if (text == null) return "";
        return text.replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }
}