package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import edu.Loopi.view.CollectionView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class OrganizerDashboard {
    private User currentUser;
    private BorderPane root; // Added as a field to allow content swapping

    public OrganizerDashboard(User user) {
        this.currentUser = user;
        SessionManager.login(user);
    }

    public void start(Stage stage) {
        stage.setTitle("LOOPI - Espace Organisateur");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Menu latéral
        VBox sidebar = createSidebar(stage);
        root.setLeft(sidebar);

        // Contenu principal
        VBox content = createMainContent();
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        SessionManager.printSessionInfo();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #2196F3; -fx-padding: 15 30;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("LOOPI ORGANISATEUR");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        Label welcome = new Label("Bienvenue, " + currentUser.getPrenom());
        welcome.setTextFill(Color.WHITE);
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        userBox.getChildren().add(welcome);
        HBox.setHgrow(userBox, Priority.ALWAYS);

        header.getChildren().addAll(title, userBox);
        return header;
    }

    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(5);
        sidebar.setStyle("-fx-background-color: #333;");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        Button productsBtn = createMenuButton("📦 Mes produits");
        productsBtn.setOnAction(e -> showMyProducts());

        Button addProductBtn = createMenuButton("➕ Ajouter produit");
        addProductBtn.setOnAction(e -> addProduct());

        Button eventsBtn = createMenuButton("📅 Mes événements");
        eventsBtn.setOnAction(e -> showMyEvents());

        Button addEventBtn = createMenuButton("➕ Créer événement");
        addEventBtn.setOnAction(e -> createEvent());

        // UPDATED: Now swaps the center content to the CollectionView
        Button donationsBtn = createMenuButton("💰 Campagnes");
        donationsBtn.setOnAction(e -> {
            CollectionView collectionView = new CollectionView(currentUser);
            root.setCenter(collectionView.getView());
        });

        Button statsBtn = createMenuButton("📊 Statistiques");
        statsBtn.setOnAction(e -> showStatistics());

        Button profileBtn = createMenuButton("👤 Mon profil");
        profileBtn.setOnAction(e -> showProfile());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createMenuButton("🚪 Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #d32f2f;");
        logoutBtn.setOnAction(e -> logout(stage));

        sidebar.getChildren().addAll(productsBtn, addProductBtn, eventsBtn, addEventBtn,
                donationsBtn, statsBtn, profileBtn, spacer, logoutBtn);
        return sidebar;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #444; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20;"));
        return btn;
    }

    private VBox createMainContent() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);

        Label welcome = new Label("Bienvenue dans votre espace Organisateur!");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcome.setTextFill(Color.web("#2196F3"));

        Label subtitle = new Label("Gérez vos produits recyclés, organisez des événements écologiques\n" +
                "et participez à l'économie circulaire");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#666"));
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Cartes de statistiques
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox productsCard = createInfoCard("📦", "Produits", "0 produits actifs");
        VBox salesCard = createInfoCard("💰", "Ventes", "0.00 DT de CA");
        VBox eventsCard = createInfoCard("📅", "Événements", "0 événements organisés");
        VBox donationsCard = createInfoCard("❤️", "Dons", "0 campagnes actives");

        statsBox.getChildren().addAll(productsCard, salesCard, eventsCard, donationsCard);

        // Actions rapides
        VBox quickActions = new VBox(10);
        quickActions.setAlignment(Pos.CENTER);

        Label actionsTitle = new Label("Actions rapides");
        actionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        actionsTitle.setTextFill(Color.web("#333"));

        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER);

        Button addProductBtn = createQuickActionButton("➕ Ajouter un produit");
        Button createEventBtn = createQuickActionButton("📅 Créer un événement");
        Button viewProductsBtn = createQuickActionButton("📦 Voir mes produits");

        actionsBox.getChildren().addAll(addProductBtn, createEventBtn, viewProductsBtn);
        quickActions.getChildren().addAll(actionsTitle, actionsBox);

        content.getChildren().addAll(welcome, subtitle, statsBox, quickActions);
        return content;
    }

    private VBox createInfoCard(String icon, String title, String value) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefSize(200, 150);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 36));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#333"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", 14));
        valueLabel.setTextFill(Color.web("#666"));

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    private Button createQuickActionButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;"));
        return btn;
    }

    private void showMyProducts() { showAlert("Info", "Mes produits - En développement"); }
    private void addProduct() { showAlert("Info", "Ajouter produit - En développement"); }
    private void showMyEvents() { showAlert("Info", "Mes événements - En développement"); }
    private void createEvent() { showAlert("Info", "Créer événement - En développement"); }
    private void showStatistics() { showAlert("Info", "Statistiques - En développement"); }
    private void showProfile() { showAlert("Info", "Mon profil - En développement"); }

    private void logout(Stage stage) {
        SessionManager.logout();
        stage.close();
        new LoginView().start(new Stage());
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}