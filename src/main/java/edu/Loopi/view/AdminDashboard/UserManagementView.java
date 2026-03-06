package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import edu.Loopi.services.PhotoService;
import edu.Loopi.services.CameraService;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javafx.application.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.awt.image.BufferedImage;

import javafx.scene.input.KeyCode;

public class UserManagementView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;
    private PhotoService photoService;
    private CameraService cameraService;

    private TableView<User> userTable;
    private ObservableList<User> masterData;
    private FilteredList<User> filteredData;
    private ComboBox<String> roleFilter;
    private TextField searchField;

    public UserManagementView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
        this.photoService = new PhotoService();
        this.cameraService = new CameraService();
    }

    @SuppressWarnings("unchecked")
    public void showUserManagementView(StackPane mainContentArea, boolean isDarkMode) {
        VBox content = createUserManagementView(isDarkMode);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(content);
    }

    @SuppressWarnings("unchecked")
    private VBox createUserManagementView(boolean isDarkMode) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + "; -fx-background-radius: 12; -fx-padding: 24;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("Gestion des utilisateurs");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        int userCount = userService.countUsers();
        Label subtitle = new Label(userCount + " utilisateurs inscrits");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button addUserBtn = new Button("+ Ajouter");
        addUserBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        addUserBtn.setOnAction(e -> showAddUserDialog());

        header.getChildren().addAll(headerText, addUserBtn);

        HBox toolbar = createToolbar(isDarkMode);

        VBox tableContainer = new VBox(16);
        tableContainer.setStyle("-fx-background-color: " + (isDarkMode ? adminDashboard.getCardBg() : "#FFFFFF") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12; -fx-padding: 20;");

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableTitleBox = new VBox(2);
        Label tableTitle = new Label("Liste des utilisateurs");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label tableSubtitle = new Label(userCount + " utilisateurs");
        tableSubtitle.setFont(Font.font("System", 13));
        tableSubtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        tableTitleBox.getChildren().addAll(tableTitle, tableSubtitle);
        HBox.setHgrow(tableTitleBox, Priority.ALWAYS);

        Button exportCSVBtn = new Button("📥 Exporter CSV");
        exportCSVBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getAccentColor() +
                "; -fx-font-weight: 500; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-font-size: 13px;");
        exportCSVBtn.setOnAction(e -> exportUsersToCSV());

        tableHeader.getChildren().addAll(tableTitleBox, exportCSVBtn);

        initializeUserTable(isDarkMode);
        tableContainer.getChildren().addAll(tableHeader, userTable);

        container.getChildren().addAll(header, toolbar, tableContainer);
        return container;
    }

    private HBox createToolbar(boolean isDarkMode) {
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(16, 0, 16, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#F3F4F6") +
                "; -fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-weight: 500; -fx-padding: 6 14; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 6; -fx-font-size: 13px;");
        refreshBtn.setOnAction(e -> refreshUserTable());

        Label filterLabel = new Label("Filtre:");
        filterLabel.setFont(Font.font("System", 13));
        filterLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("Tous", "Administrateurs", "Organisateurs", "Participants");
        roleFilter.setValue("Tous");
        roleFilter.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        roleFilter.setPrefWidth(150);
        roleFilter.setOnAction(e -> applyFilter());

        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-background-radius: 6; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6;");

        searchField = new TextField();
        searchField.setPromptText("Rechercher par nom, prénom ou email...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() + "; -fx-padding: 8 12;");
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) applyFilter();
        });
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColorMuted() +
                "; -fx-cursor: hand; -fx-padding: 8 12; -fx-font-size: 14px;");
        searchBtn.setOnAction(e -> applyFilter());

        searchBox.getChildren().addAll(searchField, searchBtn);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        toolbar.getChildren().addAll(refreshBtn, filterLabel, roleFilter, searchBox);
        return toolbar;
    }

    private void applyFilter() {
        if (filteredData == null) return;

        String selectedRole = roleFilter.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        Predicate<User> rolePredicate = user -> {
            if (selectedRole.equals("Tous")) return true;
            String role = selectedRole.equals("Administrateurs") ? "admin" :
                    selectedRole.equals("Organisateurs") ? "organisateur" : "participant";
            return user.getRole().equalsIgnoreCase(role);
        };

        Predicate<User> searchPredicate = user -> {
            if (searchText.isEmpty()) return true;
            return user.getNom().toLowerCase().contains(searchText) ||
                    user.getPrenom().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText) ||
                    user.getNomComplet().toLowerCase().contains(searchText);
        };

        filteredData.setPredicate(rolePredicate.and(searchPredicate));
    }

    @SuppressWarnings("unchecked")
    private void initializeUserTable(boolean isDarkMode) {
        userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPrefHeight(450);
        userTable.setStyle("-fx-background-color: transparent;");

        // Charger les données
        refreshUserTable();

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: " + adminDashboard.getTextColor() + ";");

        TableColumn<User, String> avatarCol = new TableColumn<>("Photo");
        avatarCol.setPrefWidth(80);
        avatarCol.setCellFactory(col -> new TableCell<User, String>() {
            private final ImageView imageView = new ImageView();
            private final Circle clip = new Circle(20, 20, 20);

            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
                imageView.setClip(clip);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    ImageView img = loadUserAvatar(user, 40);
                    if (img != null) {
                        setGraphic(img);
                    } else {
                        // Afficher les initiales si pas de photo
                        StackPane avatarContainer = new StackPane();
                        Circle avatarCircle = new Circle(20);
                        avatarCircle.setFill(Color.web(adminDashboard.getAccentColor()));
                        Label initials = new Label(getInitials(user));
                        initials.setFont(Font.font("System", FontWeight.BOLD, 12));
                        initials.setTextFill(Color.WHITE);
                        avatarContainer.getChildren().addAll(avatarCircle, initials);
                        setGraphic(avatarContainer);
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<User, String> nameCol = new TableColumn<>("Nom complet");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        nameCol.setPrefWidth(180);
        nameCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    VBox vbox = new VBox(2);
                    Label nameLabel = new Label(user.getNomComplet());
                    nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
                    nameLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

                    Label emailLabel = new Label(user.getEmail());
                    emailLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
                    emailLabel.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

                    vbox.getChildren().addAll(nameLabel, emailLabel);
                    setGraphic(vbox);
                }
            }
        });

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                } else {
                    Label roleLabel = new Label(getRoleInFrench(role));
                    roleLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                    roleLabel.setTextFill(Color.WHITE);
                    roleLabel.setPadding(new Insets(4, 12, 4, 12));

                    String bgColor = role.equalsIgnoreCase("admin") ? adminDashboard.getAccentColor() :
                            role.equalsIgnoreCase("organisateur") ? adminDashboard.getSuccessColor() :
                                    adminDashboard.getWarningColor();

                    roleLabel.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12;");
                    setGraphic(roleLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<User, String> genderCol = new TableColumn<>("Genre");
        genderCol.setCellValueFactory(cellData -> {
            String gender = "";
            if (cellData.getValue().getIdGenre() == 1) gender = "Homme";
            else if (cellData.getValue().getIdGenre() == 2) gender = "Femme";
            else gender = "Autre";
            return new javafx.beans.property.SimpleStringProperty(gender);
        });
        genderCol.setPrefWidth(80);
        genderCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: " + adminDashboard.getTextColor() + ";");

        TableColumn<User, String> dateCol = new TableColumn<>("Inscription");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        dateCol.setPrefWidth(100);
        dateCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: " + adminDashboard.getTextColor() + ";");

        TableColumn<User, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getAccentColor() +
                        "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getDangerColor() +
                        "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4;");

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    editSelectedUser(user);
                });

                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteSelectedUser(user);
                });

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        userTable.getColumns().addAll(idCol, avatarCol, nameCol, roleCol, genderCol, dateCol, actionCol);

        // Lier la table aux données filtrées
        userTable.setItems(filteredData);
    }

    private ImageView loadUserAvatar(User user, double size) {
        if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.jpg")) {
            try {
                File imageFile = null;
                String photoPath = user.getPhoto();

                // Essayer différents chemins possibles
                if (photoPath.startsWith("profiles/")) {
                    imageFile = new File("src/main/resources/" + photoPath);
                    if (!imageFile.exists()) {
                        imageFile = new File(photoPath);
                    }
                } else {
                    imageFile = new File("src/main/resources/profiles/" + photoPath);
                    if (!imageFile.exists()) {
                        imageFile = new File("profiles/" + photoPath);
                    }
                }

                if (imageFile.exists() && imageFile.isFile()) {
                    Image avatarImage = new Image(new FileInputStream(imageFile), size, size, true, true);
                    ImageView avatarImageView = new ImageView(avatarImage);
                    avatarImageView.setFitWidth(size);
                    avatarImageView.setFitHeight(size);
                    avatarImageView.setPreserveRatio(true);

                    // Forme circulaire
                    Circle clip = new Circle(size/2, size/2, size/2);
                    avatarImageView.setClip(clip);

                    return avatarImageView;
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur chargement avatar: " + e.getMessage());
            }
        }
        return null;
    }

    private String getInitials(User user) {
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            initials += String.valueOf(user.getPrenom().charAt(0)).toUpperCase();
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            initials += String.valueOf(user.getNom().charAt(0)).toUpperCase();
        }
        return initials.isEmpty() ? "U" : initials;
    }

    private String getRoleInFrench(String role) {
        if (role == null) return "";
        switch (role.toLowerCase()) {
            case "admin": return "Administrateur";
            case "organisateur": return "Organisateur";
            case "participant": return "Participant";
            default: return role;
        }
    }

    public void refreshUserTable() {
        List<User> users = userService.getAllUsers();
        masterData = FXCollections.observableArrayList(users);
        filteredData = new FilteredList<>(masterData, p -> true);
        if (userTable != null) {
            userTable.setItems(filteredData);
        }
    }

    public void filterUsersByRole(String roleFilter) {
        applyFilter();
    }

    public void searchUsers(String keyword) {
        searchField.setText(keyword);
        applyFilter();
    }

    private void showAddUserDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Ajouter un utilisateur");
        dialogStage.initOwner(adminDashboard.getPrimaryStage());
        dialogStage.initModality(Modality.WINDOW_MODAL);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(24));
        mainLayout.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? adminDashboard.getCardBg() : "#FFFFFF") + ";");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("Nouvel utilisateur");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Remplissez les informations ci-dessous");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColorMuted() +
                "; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 4 8;");
        closeBtn.setOnAction(e -> {
            stopCamera();
            dialogStage.close();
        });

        header.getChildren().addAll(headerText, closeBtn);

        // ========== SECTION PHOTO ==========
        VBox photoSection = new VBox(15);
        photoSection.setPadding(new Insets(10));
        photoSection.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#F3F4F6") +
                "; -fx-background-radius: 12; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 12;");

        Label photoTitle = new Label("📷 Photo de profil");
        photoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        photoTitle.setTextFill(Color.web(adminDashboard.getTextColor()));

        HBox photoContainer = new HBox(30);
        photoContainer.setAlignment(Pos.CENTER_LEFT);

        // Conteneur pour l'aperçu de la photo
        StackPane avatarPreviewContainer = new StackPane();
        avatarPreviewContainer.setPrefSize(120, 120);
        avatarPreviewContainer.setMinSize(120, 120);
        avatarPreviewContainer.setMaxSize(120, 120);
        avatarPreviewContainer.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "20; -fx-background-radius: 60; -fx-border-color: " + adminDashboard.getAccentColor() + "; -fx-border-radius: 60; -fx-border-width: 2;");

        ImageView avatarPreview = new ImageView();
        avatarPreview.setFitWidth(114);
        avatarPreview.setFitHeight(114);
        avatarPreview.setPreserveRatio(true);

        // Cercle de clipping pour l'aperçu
        Circle previewClip = new Circle(57, 57, 57);
        avatarPreview.setClip(previewClip);

        // Image par défaut
        try {
            Image defaultImage = new Image("https://ui-avatars.com/api/?name=User&size=114&background=" +
                    adminDashboard.getAccentColor().substring(1) + "&color=fff&bold=true");
            avatarPreview.setImage(defaultImage);
        } catch (Exception e) {
            Label placeholder = new Label("👤");
            placeholder.setFont(Font.font("System", FontWeight.BOLD, 48));
            placeholder.setTextFill(Color.web(adminDashboard.getAccentColor()));
            avatarPreviewContainer.getChildren().add(placeholder);
        }
        avatarPreviewContainer.getChildren().add(avatarPreview);

        // Variables pour stocker la photo
        final File[] selectedPhotoFile = {null};
        final BufferedImage[] capturedPhoto = {null};
        final boolean[] isUsingCamera = {false};

        VBox photoButtons = new VBox(10);
        photoButtons.setAlignment(Pos.CENTER_LEFT);

        Label photoStatus = new Label("Aucune photo sélectionnée");
        photoStatus.setFont(Font.font("System", 11));
        photoStatus.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button choosePhotoBtn = new Button("📁 Choisir un fichier");
        choosePhotoBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");

        Button takePhotoBtn = new Button("📷 Prendre une photo");
        takePhotoBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");

        Button clearPhotoBtn = new Button("🗑️ Effacer");
        clearPhotoBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getDangerColor() +
                "; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getDangerColor() + "; -fx-border-radius: 6; -fx-font-size: 12px;");
        clearPhotoBtn.setVisible(false);

        buttonRow.getChildren().addAll(choosePhotoBtn, takePhotoBtn, clearPhotoBtn);
        photoButtons.getChildren().addAll(buttonRow, photoStatus);

        photoContainer.getChildren().addAll(avatarPreviewContainer, photoButtons);
        photoSection.getChildren().addAll(photoTitle, photoContainer);

        // ========== FORMULAIRE ==========
        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        // Labels
        Label nomLabel = new Label("Nom *");
        nomLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        nomLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        prenomLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label emailLabel = new Label("Email *");
        emailLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        emailLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label passwordLabel = new Label("Mot de passe *");
        passwordLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        passwordLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label confirmLabel = new Label("Confirmation *");
        confirmLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        confirmLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label roleLabel = new Label("Rôle *");
        roleLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        roleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label genreLabel = new Label("Genre");
        genreLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        genreLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        // Champs
        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        styleFormTextField(nomField);

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        styleFormTextField(prenomField);

        TextField emailField = new TextField();
        emailField.setPromptText("exemple@email.com");
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 8 caractères");
        styleFormTextField(passwordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");
        styleFormTextField(confirmPasswordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue("participant");
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Autre");
        genreCombo.setValue("Autre");
        styleFormComboBox(genreCombo);

        formGrid.add(nomLabel, 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(prenomLabel, 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(emailLabel, 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(passwordLabel, 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(confirmLabel, 0, 4);
        formGrid.add(confirmPasswordField, 1, 4);
        formGrid.add(roleLabel, 0, 5);
        formGrid.add(roleCombo, 1, 5);
        formGrid.add(genreLabel, 0, 6);
        formGrid.add(genreCombo, 1, 6);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(300);
        formGrid.getColumnConstraints().addAll(col1, col2);

        // Label d'erreur
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setTextFill(Color.web(adminDashboard.getDangerColor()));
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // Boutons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(16, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-font-weight: 500; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-font-size: 13px;");
        cancelBtn.setOnAction(e -> {
            stopCamera();
            dialogStage.close();
        });

        Button addBtn = new Button("Ajouter");
        addBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: 600; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");

        // ========== GESTIONNAIRES D'ÉVÉNEMENTS POUR LA PHOTO ==========

        // Choisir un fichier
        choosePhotoBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une photo de profil");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File file = fileChooser.showOpenDialog(dialogStage);
            if (file != null) {
                try {
                    Image image = new Image(file.toURI().toString(), 114, 114, true, true);
                    avatarPreview.setImage(image);
                    selectedPhotoFile[0] = file;
                    isUsingCamera[0] = false;
                    capturedPhoto[0] = null;
                    photoStatus.setText("✅ Photo chargée: " + file.getName());
                    photoStatus.setTextFill(Color.GREEN);
                    clearPhotoBtn.setVisible(true);
                } catch (Exception ex) {
                    photoStatus.setText("❌ Erreur lors du chargement");
                    photoStatus.setTextFill(Color.web(adminDashboard.getDangerColor()));
                }
            }
        });

        // Prendre une photo avec la caméra
        takePhotoBtn.setOnAction(e -> openCameraDialog(dialogStage, avatarPreview, capturedPhoto, isUsingCamera, photoStatus, clearPhotoBtn));

        // Effacer la photo
        clearPhotoBtn.setOnAction(e -> {
            try {
                Image defaultImage = new Image("https://ui-avatars.com/api/?name=User&size=114&background=" +
                        adminDashboard.getAccentColor().substring(1) + "&color=fff&bold=true");
                avatarPreview.setImage(defaultImage);
            } catch (Exception ex) {
                avatarPreview.setImage(null);
            }
            selectedPhotoFile[0] = null;
            capturedPhoto[0] = null;
            isUsingCamera[0] = false;
            photoStatus.setText("Aucune photo sélectionnée");
            photoStatus.setTextFill(Color.web(adminDashboard.getTextColorMuted()));
            clearPhotoBtn.setVisible(false);
        });

        // Ajouter l'utilisateur
        addBtn.setOnAction(e -> {
            if (validateAndAddUser(nomField, prenomField, emailField, passwordField,
                    confirmPasswordField, roleCombo, genreCombo, selectedPhotoFile[0],
                    capturedPhoto[0], isUsingCamera[0], errorLabel)) {
                refreshUserTable();
                adminDashboard.showAlert("Succès", "Utilisateur ajouté avec succès");
                stopCamera();
                dialogStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, addBtn);

        mainLayout.getChildren().addAll(header, photoSection, formGrid, errorLabel, buttonBox);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        Scene scene = new Scene(scrollPane, 600, 800);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void openCameraDialog(Stage parentStage, ImageView previewImage,
                                  BufferedImage[] capturedPhoto, boolean[] isUsingCamera,
                                  Label statusLabel, Button clearBtn) {
        if (cameraService == null) {
            cameraService = new CameraService();
        }

        Stage cameraStage = new Stage();
        cameraStage.setTitle("Prendre une photo");
        cameraStage.initModality(Modality.APPLICATION_MODAL);
        cameraStage.initOwner(parentStage);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e293b;");
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("📷 Prendre une photo");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        StackPane cameraPane = new StackPane();
        cameraPane.setPrefSize(400, 300);
        cameraPane.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 10; " +
                "-fx-border-color: " + adminDashboard.getAccentColor() + "; -fx-border-radius: 10; -fx-border-width: 2;");

        Label cameraPlaceholder = new Label("📷 CAMÉRA");
        cameraPlaceholder.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        cameraPlaceholder.setTextFill(Color.web("#64748b"));
        cameraPane.getChildren().add(cameraPlaceholder);

        ImageView cameraFeed = new ImageView();
        cameraFeed.setFitWidth(380);
        cameraFeed.setFitHeight(280);
        cameraFeed.setPreserveRatio(true);
        cameraFeed.setVisible(false);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button startBtn = new Button("▶ Démarrer");
        startBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");

        Button captureBtn = new Button("📸 Capturer");
        captureBtn.setStyle("-fx-background-color: " + adminDashboard.getSuccessColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
        captureBtn.setDisable(true);

        Button acceptBtn = new Button("✅ Accepter");
        acceptBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 25; -fx-cursor: hand;");
        acceptBtn.setVisible(false);

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            stopCamera();
            cameraStage.close();
        });

        Label camStatusLabel = new Label();
        camStatusLabel.setFont(Font.font("Segoe UI", 12));
        camStatusLabel.setTextFill(Color.WHITE);

        final boolean[] isCameraRunning = {false};
        final BufferedImage[] lastFrame = new BufferedImage[1];

        startBtn.setOnAction(ev -> {
            new Thread(() -> {
                boolean started = cameraService.startCamera();
                Platform.runLater(() -> {
                    if (started) {
                        isCameraRunning[0] = true;
                        startBtn.setDisable(true);
                        captureBtn.setDisable(false);
                        camStatusLabel.setText("✅ Caméra prête");
                        cameraPane.getChildren().clear();
                        cameraFeed.setVisible(true);
                        cameraPane.getChildren().add(cameraFeed);

                        new Thread(() -> {
                            while (isCameraRunning[0]) {
                                BufferedImage frame = cameraService.captureImage();
                                if (frame != null) {
                                    lastFrame[0] = frame;
                                    Image fxImage = SwingFXUtils.toFXImage(frame, null);
                                    Platform.runLater(() -> cameraFeed.setImage(fxImage));
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    break;
                                }
                            }
                        }).start();
                    } else {
                        camStatusLabel.setText("❌ Erreur: Aucune caméra trouvée");
                    }
                });
            }).start();
        });

        captureBtn.setOnAction(ev -> {
            camStatusLabel.setText("🔄 Capture...");

            new Thread(() -> {
                BufferedImage captured = cameraService.captureImage();
                Platform.runLater(() -> {
                    if (captured != null) {
                        lastFrame[0] = captured;

                        Image fxImage = SwingFXUtils.toFXImage(captured, null);
                        cameraFeed.setImage(fxImage);

                        captureBtn.setVisible(false);
                        acceptBtn.setVisible(true);
                        camStatusLabel.setText("✅ Photo capturée - Cliquez sur Accepter");
                    } else {
                        camStatusLabel.setText("❌ Erreur capture");
                    }
                });
            }).start();
        });

        acceptBtn.setOnAction(ev -> {
            if (lastFrame[0] != null) {
                try {
                    // Redimensionner l'image pour l'aperçu
                    java.awt.Image awtImage = lastFrame[0].getScaledInstance(114, 114, java.awt.Image.SCALE_SMOOTH);
                    BufferedImage resizedImage = new BufferedImage(114, 114, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(awtImage, 0, 0, null);
                    g.dispose();

                    Image fxImage = SwingFXUtils.toFXImage(resizedImage, null);
                    previewImage.setImage(fxImage);

                    capturedPhoto[0] = lastFrame[0];
                    isUsingCamera[0] = true;

                    statusLabel.setText("✅ Photo prise avec succès");
                    statusLabel.setTextFill(Color.GREEN);
                    clearBtn.setVisible(true);

                    stopCamera();
                    cameraStage.close();
                } catch (Exception ex) {
                    camStatusLabel.setText("❌ Erreur traitement image");
                }
            }
        });

        buttons.getChildren().addAll(startBtn, captureBtn, acceptBtn, cancelBtn);
        layout.getChildren().addAll(title, cameraPane, buttons, camStatusLabel);

        Scene scene = new Scene(layout, 500, 500);
        cameraStage.setScene(scene);
        cameraStage.setOnCloseRequest(e -> stopCamera());
        cameraStage.showAndWait();
    }

    private void stopCamera() {
        if (cameraService != null) {
            cameraService.stopCamera();
        }
    }

    private boolean validateAndAddUser(TextField nomField, TextField prenomField, TextField emailField,
                                       PasswordField passwordField, PasswordField confirmPasswordField,
                                       ComboBox<String> roleCombo, ComboBox<String> genreCombo,
                                       File selectedPhotoFile, BufferedImage capturedPhoto, boolean isUsingCamera,
                                       Label errorLabel) {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showError(errorLabel, "Tous les champs obligatoires doivent être remplis");
            return false;
        }

        if (!isValidEmail(emailField.getText())) {
            showError(errorLabel, "Format d'email invalide");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            showError(errorLabel, "Le mot de passe doit contenir au moins 8 caractères");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError(errorLabel, "Les mots de passe ne correspondent pas");
            return false;
        }

        if (userService.emailExists(emailField.getText())) {
            showError(errorLabel, "Cet email est déjà utilisé");
            return false;
        }

        User newUser = new User();
        newUser.setNom(nomField.getText());
        newUser.setPrenom(prenomField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(passwordField.getText());
        newUser.setRole(roleCombo.getValue());
        newUser.setPhoto("default.jpg");

        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) newUser.setIdGenre(1);
        else if ("Femme".equals(genre)) newUser.setIdGenre(2);
        else newUser.setIdGenre(3);

        boolean userAdded = userService.addUser(newUser);

        if (userAdded) {
            // Récupérer l'utilisateur fraîchement créé pour obtenir son ID
            User createdUser = userService.getUserByEmail(emailField.getText());

            if (createdUser != null) {
                // Sauvegarder la photo si elle existe
                if (isUsingCamera && capturedPhoto != null) {
                    String photoPath = cameraService.saveProfilePhoto(capturedPhoto, createdUser.getId());
                    createdUser.setPhoto(photoPath);
                    userService.updateUser(createdUser);
                    System.out.println("✅ Photo caméra sauvegardée: " + photoPath);
                } else if (selectedPhotoFile != null) {
                    String photoPath = photoService.saveProfilePhoto(selectedPhotoFile, createdUser.getId());
                    createdUser.setPhoto(photoPath);
                    userService.updateUser(createdUser);
                    System.out.println("✅ Photo fichier sauvegardée: " + photoPath);
                }
            }
            return true;
        }

        return false;
    }

    private void editSelectedUser(User user) {
        if (user.getId() == currentUser.getId() && !currentUser.getRole().equalsIgnoreCase("admin")) {
            adminDashboard.showError("Attention", "Vous ne pouvez pas modifier votre propre compte");
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Modifier l'utilisateur");
        dialogStage.initOwner(adminDashboard.getPrimaryStage());

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(24));
        mainLayout.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? adminDashboard.getCardBg() : "#FFFFFF") + ";");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(24);
        avatarCircle.setFill(Color.web(adminDashboard.getAccentColor()));

        ImageView avatarImageView = adminDashboard.loadProfileImage(user, 48);
        if (avatarImageView != null) {
            avatarContainer.getChildren().add(avatarImageView);
        } else {
            Label avatarText = new Label(getInitials(user));
            avatarText.setFont(Font.font("System", FontWeight.BOLD, 16));
            avatarText.setTextFill(Color.WHITE);
            avatarContainer.getChildren().addAll(avatarCircle, avatarText);
        }

        VBox headerText = new VBox(4);
        Label title = new Label("Modifier l'utilisateur");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label(user.getEmail());
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColorMuted() +
                "; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 4 8;");
        closeBtn.setOnAction(e -> dialogStage.close());

        header.getChildren().addAll(avatarContainer, headerText, closeBtn);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        Label nomLabel = new Label("Nom *");
        nomLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        nomLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        prenomLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label emailLabel = new Label("Email *");
        emailLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        emailLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label passwordLabel = new Label("Nouveau mot de passe");
        passwordLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        passwordLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label roleLabel = new Label("Rôle *");
        roleLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        roleLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label genreLabel = new Label("Genre");
        genreLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        genreLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label photoLabel = new Label("Photo");
        photoLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        photoLabel.setTextFill(Color.web(adminDashboard.getTextColor()));

        TextField nomField = new TextField(user.getNom());
        styleFormTextField(nomField);

        TextField prenomField = new TextField(user.getPrenom());
        styleFormTextField(prenomField);

        TextField emailField = new TextField(user.getEmail());
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Laisser vide pour conserver");
        styleFormTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue(user.getRole());
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Autre");
        if (user.getIdGenre() == 1) genreCombo.setValue("Homme");
        else if (user.getIdGenre() == 2) genreCombo.setValue("Femme");
        else genreCombo.setValue("Autre");
        styleFormComboBox(genreCombo);

        HBox photoBox = new HBox(8);
        photoBox.setAlignment(Pos.CENTER_LEFT);

        Button changePhotoBtn = new Button("📷 Changer photo");
        changePhotoBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getAccentColor() +
                "; -fx-font-weight: 500; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-font-size: 12px;");
        changePhotoBtn.setOnAction(e -> changeUserProfilePicture(user, dialogStage));

        Label photoInfo = new Label(user.getPhoto() != null ? user.getPhoto() : "default.jpg");
        photoInfo.setFont(Font.font("System", 11));
        photoInfo.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        photoBox.getChildren().addAll(changePhotoBtn, photoInfo);

        formGrid.add(nomLabel, 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(prenomLabel, 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(emailLabel, 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(passwordLabel, 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(roleLabel, 0, 4);
        formGrid.add(roleCombo, 1, 4);
        formGrid.add(genreLabel, 0, 5);
        formGrid.add(genreCombo, 1, 5);
        formGrid.add(photoLabel, 0, 6);
        formGrid.add(photoBox, 1, 6);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(300);
        formGrid.getColumnConstraints().addAll(col1, col2);

        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setTextFill(Color.web(adminDashboard.getDangerColor()));
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(16, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-font-weight: 500; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-font-size: 13px;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() + "; -fx-text-fill: white; " +
                "-fx-font-weight: 600; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        saveBtn.setOnAction(e -> {
            if (validateAndUpdateUser(user, nomField, prenomField, emailField, passwordField,
                    roleCombo, genreCombo, errorLabel)) {
                refreshUserTable();
                adminDashboard.showAlert("Succès", "Utilisateur modifié avec succès");
                dialogStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        mainLayout.getChildren().addAll(header, formGrid, errorLabel, buttonBox);

        Scene scene = new Scene(mainLayout, 600, 650);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private boolean validateAndUpdateUser(User user, TextField nomField, TextField prenomField,
                                          TextField emailField, PasswordField passwordField,
                                          ComboBox<String> roleCombo, ComboBox<String> genreCombo,
                                          Label errorLabel) {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showError(errorLabel, "Tous les champs obligatoires doivent être remplis");
            return false;
        }

        if (!isValidEmail(emailField.getText())) {
            showError(errorLabel, "Format d'email invalide");
            return false;
        }

        if (!emailField.getText().equals(user.getEmail()) && userService.emailExists(emailField.getText())) {
            showError(errorLabel, "Cet email est déjà utilisé");
            return false;
        }

        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setEmail(emailField.getText());
        user.setRole(roleCombo.getValue());

        if (!passwordField.getText().isEmpty()) {
            if (passwordField.getText().length() < 8) {
                showError(errorLabel, "Le mot de passe doit contenir au moins 8 caractères");
                return false;
            }
            user.setPassword(passwordField.getText());
        }

        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) user.setIdGenre(1);
        else if ("Femme".equals(genre)) user.setIdGenre(2);
        else user.setIdGenre(3);

        return userService.updateUser(user);
    }

    private void changeUserProfilePicture(User user, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(parentStage);
        if (selectedFile != null) {
            try {
                File profileDir = new File("profiles");
                if (!profileDir.exists()) profileDir.mkdir();

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileExtension = getFileExtension(selectedFile.getName());
                String newFileName = "profile_" + user.getId() + "_" + timestamp + fileExtension;
                File destFile = new File("profiles/" + newFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                user.setPhoto("profiles/" + newFileName);

                if (userService.updateUser(user)) {
                    adminDashboard.showAlert("Succès", "Photo mise à jour");
                    refreshUserTable();
                }
            } catch (Exception e) {
                adminDashboard.showError("Erreur", "Erreur lors du téléchargement");
            }
        }
    }

    private void deleteSelectedUser(User user) {
        if (user.getId() == currentUser.getId()) {
            adminDashboard.showError("Erreur", "Vous ne pouvez pas supprimer votre propre compte");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Voulez-vous vraiment supprimer " + user.getNomComplet() + " ?");
        confirmAlert.initOwner(adminDashboard.getPrimaryStage());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userService.deleteUser(user.getId())) {
                adminDashboard.showAlert("Succès", "Utilisateur supprimé");
                refreshUserTable();
            }
        }
    }

    private void exportUsersToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les utilisateurs");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fileChooser.setInitialFileName("utilisateurs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(adminDashboard.getPrimaryStage());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("ID,Nom,Prénom,Email,Rôle,Genre,Date inscription\n");

                for (User user : filteredData) {
                    String genre = user.getIdGenre() == 1 ? "Homme" : (user.getIdGenre() == 2 ? "Femme" : "Autre");
                    String regDate = user.getCreatedAt() != null ?
                            user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                            user.getId(), user.getNom(), user.getPrenom(), user.getEmail(),
                            user.getRole(), genre, regDate));
                }
                adminDashboard.showAlert("Succès", filteredData.size() + " utilisateurs exportés");
            } catch (Exception e) {
                adminDashboard.showError("Erreur", "Échec de l'export");
            }
        }
    }

    private void styleFormTextField(TextField field) {
        field.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-padding: 8 12; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() + ";");
        field.setPrefWidth(300);
    }

    private void styleFormTextField(PasswordField field) {
        field.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-border-radius: 6; " +
                "-fx-padding: 8 12; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-prompt-text-fill: " + adminDashboard.getTextColorMuted() + ";");
        field.setPrefWidth(300);
    }

    private void styleFormComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-background-color: " + (adminDashboard.isDarkMode() ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        comboBox.setPrefWidth(300);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}