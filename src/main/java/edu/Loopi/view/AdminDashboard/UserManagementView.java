package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.scene.input.KeyCode;

public class UserManagementView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;

    private TableView<User> userTable;
    private ObservableList<User> userList;
    private ComboBox<String> currentRoleFilter;

    public UserManagementView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    public void showUserManagementView(StackPane mainContentArea) {
        ScrollPane content = createEnhancedUserManagementView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(content);
    }

    private ScrollPane createEnhancedUserManagementView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #E6F8F6;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Users Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Manage all users in the LOOPI platform");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#03414D"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button addUserBtn = new Button("➕ Add User");
        addUserBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");
        addUserBtn.setOnAction(e -> showAddUserDialog());

        header.getChildren().addAll(headerText, addUserBtn);

        // Barre d'outils
        HBox toolbar = createEnhancedUserToolbar();

        // Tableau des utilisateurs
        VBox tableContainer = new VBox(15);
        tableContainer.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableTitleBox = new VBox(2);
        Label tableTitle = new Label("All Users");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#03414D"));

        int userCount = userService.countUsers();
        Label tableSubtitle = new Label(userCount + " users found");
        tableSubtitle.setFont(Font.font("Arial", 12));
        tableSubtitle.setTextFill(Color.web("#03414D"));

        tableTitleBox.getChildren().addAll(tableTitle, tableSubtitle);
        HBox.setHgrow(tableTitleBox, Priority.ALWAYS);

        // Boutons d'export
        HBox exportButtons = new HBox(10);
        exportButtons.setAlignment(Pos.CENTER_RIGHT);

        Button exportCSVBtn = new Button("📥 CSV");
        exportCSVBtn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        exportCSVBtn.setOnAction(e -> exportUsersToCSV());

        Button exportPDFBtn = new Button("📥 PDF");
        exportPDFBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        exportPDFBtn.setOnAction(e -> adminDashboard.showAlert("Info", "PDF export coming soon"));

        exportButtons.getChildren().addAll(exportCSVBtn, exportPDFBtn);
        tableHeader.getChildren().addAll(tableTitleBox, exportButtons);

        // Initialiser et configurer le tableau
        initializeUserTable();

        tableContainer.getChildren().addAll(tableHeader, userTable);
        container.getChildren().addAll(header, toolbar, tableContainer);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createEnhancedUserToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Bouton Actualiser
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshUserTable());

        // Filtre par rôle
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter:");
        filterLabel.setFont(Font.font("Arial", 12));
        filterLabel.setTextFill(Color.web("#03414D"));

        currentRoleFilter = new ComboBox<>();
        currentRoleFilter.getItems().addAll("All Roles", "Admin", "Organizer", "Participant");
        currentRoleFilter.setValue("All Roles");
        currentRoleFilter.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
        currentRoleFilter.setPrefWidth(150);
        currentRoleFilter.setOnAction(e -> filterUsersByRole(currentRoleFilter.getValue()));

        filterBox.getChildren().addAll(filterLabel, currentRoleFilter);

        // Champ de recherche
        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; " +
                "-fx-border-color: #72DFD0; -fx-border-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search users...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-padding: 10 15;");
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                searchUsers(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #03414D; " +
                "-fx-padding: 10 15; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> searchUsers(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        toolbar.getChildren().addAll(refreshBtn, filterBox, searchBox);
        return toolbar;
    }

    @SuppressWarnings("unchecked")
    private void initializeUserTable() {
        userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPrefHeight(500);
        userTable.setStyle("-fx-background-color: transparent; -fx-border-color: #72DFD0; -fx-border-radius: 8;");

        // Colonne ID
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);
        idCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #000000;");
        idCol.setSortable(true);

        // Colonne Avatar
        TableColumn<User, String> avatarCol = new TableColumn<>("");
        avatarCol.setPrefWidth(60);
        avatarCol.setCellFactory(column -> new TableCell<User, String>() {
            private final StackPane avatarContainer = new StackPane();
            private final Circle avatarCircle = new Circle(20);

            {
                avatarCircle.setFill(Color.web("#72DFD0"));
                avatarContainer.getChildren().add(avatarCircle);
                avatarContainer.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());

                    // Charger l'image de profil si disponible
                    ImageView avatarImageView = loadProfileImage(user, 40);
                    if (avatarImageView != null) {
                        avatarContainer.getChildren().clear();
                        avatarContainer.getChildren().add(avatarImageView);
                    } else {
                        // Utiliser les initiales
                        avatarContainer.getChildren().clear();
                        avatarCircle.setFill(Color.web("#72DFD0"));
                        String initials = adminDashboard.getInitials(user);
                        Label avatarText = new Label(initials);
                        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                        avatarText.setTextFill(Color.WHITE);
                        avatarContainer.getChildren().addAll(avatarCircle, avatarText);
                    }

                    setGraphic(avatarContainer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Nom complet
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> {
            String fullName = cellData.getValue().getPrenom() + " " + cellData.getValue().getNom();
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });
        nameCol.setPrefWidth(200);
        nameCol.setStyle("-fx-text-fill: #000000;");

        // Colonne Email
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);
        emailCol.setStyle("-fx-text-fill: #000000;");

        // Colonne Rôle
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role.toUpperCase());
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(4, 12, 4, 12));
                    setFont(Font.font("Arial", FontWeight.BOLD, 11));

                    switch (role.toLowerCase()) {
                        case "admin":
                            setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; -fx-background-radius: 15;");
                            break;
                        case "organisateur":
                            setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; -fx-background-radius: 15;");
                            break;
                        case "participant":
                            setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; -fx-background-radius: 15;");
                            break;
                    }
                }
            }
        });

        // Colonne Genre
        TableColumn<User, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(cellData -> {
            String gender = "";
            if (cellData.getValue().getIdGenre() == 1) {
                gender = "Homme";
            } else if (cellData.getValue().getIdGenre() == 2) {
                gender = "Femme";
            } else {
                gender = "Non spécifié";
            }
            return new javafx.beans.property.SimpleStringProperty(gender);
        });
        genderCol.setPrefWidth(100);
        genderCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #000000;");

        // Colonne Date d'inscription
        TableColumn<User, String> dateCol = new TableColumn<>("Registration Date");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #000000;");

        // Colonne Statut
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<User, String>() {
            private final Circle statusDot = new Circle(5);
            private final Label statusText = new Label();
            private final HBox container = new HBox(8, statusDot, statusText);

            {
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Déterminer le statut en fonction de la dernière activité
                    User user = getTableView().getItems().get(getIndex());
                    boolean isActive = isUserActive(user);

                    if (isActive) {
                        statusDot.setFill(Color.web("#72DFD0"));
                        statusText.setText("Active");
                        statusText.setTextFill(Color.web("#03414D"));
                    } else {
                        statusDot.setFill(Color.web("#A0F6D2"));
                        statusText.setText("Inactive");
                        statusText.setTextFill(Color.web("#03414D"));
                    }
                    setGraphic(container);
                }
            }
        });

        // Colonne Actions
        TableColumn<User, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                        "-fx-background-radius: 5; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editSelectedUser(user);
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteSelectedUser(user);
                });

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        userTable.getColumns().addAll(idCol, avatarCol, nameCol, emailCol, roleCol, genderCol, dateCol, statusCol, actionCol);

        // Ajouter la sélection de ligne avec affichage des détails
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showUserDetails(newSelection);
            }
        });

        // Style des lignes
        userTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setStyle("");
                } else {
                    // Alternance de couleurs
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #E6F8F6;");
                    } else {
                        setStyle("-fx-background-color: #FFFFFF;");
                    }

                    // Surlignage au survol
                    setOnMouseEntered(e -> {
                        if (!isEmpty()) {
                            setStyle("-fx-background-color: #A0F6D2;");
                        }
                    });

                    setOnMouseExited(e -> {
                        if (!isEmpty()) {
                            if (getIndex() % 2 == 0) {
                                setStyle("-fx-background-color: #E6F8F6;");
                            } else {
                                setStyle("-fx-background-color: #FFFFFF;");
                            }
                        }
                    });

                    // Sélection
                    selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        if (isNowSelected) {
                            setStyle("-fx-background-color: #72DFD0; -fx-font-weight: bold;");
                        } else {
                            if (getIndex() % 2 == 0) {
                                setStyle("-fx-background-color: #E6F8F6;");
                            } else {
                                setStyle("-fx-background-color: #FFFFFF;");
                            }
                        }
                    });
                }
            }
        });

        // Charger les données
        refreshUserTable();
    }

    private void showUserDetails(User user) {
        // Créer un popup ou un panneau latéral pour afficher les détails
        Stage detailsStage = new Stage();
        detailsStage.setTitle("User Details");
        detailsStage.initOwner(adminDashboard.getPrimaryStage());

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #FFFFFF;");

        // En-tête avec avatar
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(40);
        avatarCircle.setFill(Color.web("#72DFD0"));

        // Charger l'image de profil
        ImageView avatarImageView = loadProfileImage(user, 80);
        if (avatarImageView != null) {
            avatarContainer.getChildren().add(avatarImageView);
        } else {
            String initials = adminDashboard.getInitials(user);
            Label avatarText = new Label(initials);
            avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            avatarText.setTextFill(Color.WHITE);
            avatarContainer.getChildren().addAll(avatarCircle, avatarText);
        }

        VBox headerText = new VBox(5);
        Label title = new Label(user.getNomComplet());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#03414D"));

        Label roleLabel = new Label(user.getRole().toUpperCase());
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        roleLabel.setTextFill(Color.WHITE);
        roleLabel.setPadding(new Insets(5, 15, 5, 15));
        roleLabel.setStyle("-fx-background-color: #72DFD0; -fx-background-radius: 15;");

        headerText.getChildren().addAll(title, roleLabel);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        header.getChildren().addAll(avatarContainer, headerText);

        // Détails
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(20, 0, 20, 0));

        detailsGrid.add(new Label("Email:"), 0, 0);
        detailsGrid.add(new Label(user.getEmail()), 1, 0);

        detailsGrid.add(new Label("Gender:"), 0, 1);
        String gender = user.getIdGenre() == 1 ? "Homme" : (user.getIdGenre() == 2 ? "Femme" : "Non spécifié");
        detailsGrid.add(new Label(gender), 1, 1);

        detailsGrid.add(new Label("Registration Date:"), 0, 2);
        String regDate = user.getCreatedAt() != null ?
                user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Unknown";
        detailsGrid.add(new Label(regDate), 1, 2);

        detailsGrid.add(new Label("Status:"), 0, 3);
        detailsGrid.add(new Label(isUserActive(user) ? "Active" : "Inactive"), 1, 3);

        // Bouton Fermer
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> detailsStage.close());

        mainLayout.getChildren().addAll(header, detailsGrid, closeBtn);

        Scene scene = new Scene(mainLayout, 400, 350);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    private boolean isUserActive(User user) {
        // Logique pour déterminer si un utilisateur est actif
        // Pour l'instant, on retourne toujours vrai (à améliorer)
        return true;
    }

    public void refreshUserTable() {
        List<User> users = userService.getAllUsers();
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
        userTable.refresh();
    }

    public void filterUsersByRole(String roleFilter) {
        if (roleFilter.equals("All Roles")) {
            refreshUserTable();
            return;
        }

        String role = "";
        switch (roleFilter) {
            case "Admin": role = "admin"; break;
            case "Organizer": role = "organisateur"; break;
            case "Participant": role = "participant"; break;
        }

        List<User> filteredUsers = userService.getUsersByRole(role);
        userList = FXCollections.observableArrayList(filteredUsers);
        userTable.setItems(userList);
    }

    public void searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (currentRoleFilter != null && !currentRoleFilter.getValue().equals("All Roles")) {
                filterUsersByRole(currentRoleFilter.getValue());
            } else {
                refreshUserTable();
            }
            return;
        }

        List<User> users = userService.searchUsers(keyword);
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    // ============ DIALOGUES UTILISATEUR ============
    private void showAddUserDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add New User");
        dialogStage.initOwner(adminDashboard.getPrimaryStage());

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #FFFFFF;");

        // En-tête
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Add New User");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Fill in the new user's information");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#03414D"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        header.getChildren().add(headerText);

        // Formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        // Champs de saisie
        TextField nomField = new TextField();
        nomField.setPromptText("Last Name");
        styleFormTextField(nomField);

        TextField prenomField = new TextField();
        prenomField.setPromptText("First Name");
        styleFormTextField(prenomField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min. 8 characters)");
        styleFormTextField(passwordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        styleFormTextField(confirmPasswordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue("participant");
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue("Non spécifié");
        styleFormComboBox(genreCombo);

        // Ajout des labels et champs - CORRECTION ICI : Ajout du padding en bas du formulaire
        formGrid.add(new Label("Last Name *:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("First Name *:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email *:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Password *:"), 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(new Label("Confirm *:"), 0, 4);
        formGrid.add(confirmPasswordField, 1, 4);
        formGrid.add(new Label("Role *:"), 0, 5);
        formGrid.add(roleCombo, 1, 5);
        formGrid.add(new Label("Gender:"), 0, 6);
        formGrid.add(genreCombo, 1, 6);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setMinHeight(30);

        // Boutons - CORRECTION ICI : S'assurer que les boutons sont visibles
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0)); // Ajout de padding
        buttonBox.setMinHeight(60);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-min-width: 120; -fx-min-height: 40;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button addBtn = new Button("Add User");
        addBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-min-width: 120; -fx-min-height: 40;");
        addBtn.setOnAction(e -> {
            if (validateAndAddUser(nomField, prenomField, emailField, passwordField,
                    confirmPasswordField, roleCombo, genreCombo, errorLabel)) {
                refreshUserTable();
                adminDashboard.showAlert("Success", "User added successfully!");
                dialogStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, addBtn);

        // Ajouter tous les composants au layout principal
        mainLayout.getChildren().addAll(header, formGrid, errorLabel, buttonBox);

        // Créer un ScrollPane pour s'assurer que tout est visible
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        Scene scene = new Scene(scrollPane, 500, 650); // Augmenté la hauteur
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private boolean validateAndAddUser(TextField nomField, TextField prenomField, TextField emailField,
                                       PasswordField passwordField, PasswordField confirmPasswordField,
                                       ComboBox<String> roleCombo, ComboBox<String> genreCombo, Label errorLabel) {
        // Validation
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showError(errorLabel, "Please fill all required fields (*)");
            return false;
        }

        if (!isValidEmail(emailField.getText())) {
            showError(errorLabel, "Please enter a valid email address");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            showError(errorLabel, "Password must be at least 8 characters long");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError(errorLabel, "Passwords do not match");
            return false;
        }

        if (userService.emailExists(emailField.getText())) {
            showError(errorLabel, "This email is already used by another user");
            return false;
        }

        // Création de l'utilisateur
        User newUser = new User();
        newUser.setNom(nomField.getText());
        newUser.setPrenom(prenomField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(passwordField.getText());
        newUser.setRole(roleCombo.getValue());
        newUser.setPhoto("default.jpg");

        // Définir l'idGenre
        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) {
            newUser.setIdGenre(1);
        } else if ("Femme".equals(genre)) {
            newUser.setIdGenre(2);
        } else {
            newUser.setIdGenre(3);
        }

        // Ajout dans la base de données
        if (userService.addUser(newUser)) {
            return true;
        } else {
            showError(errorLabel, "Error adding user");
            return false;
        }
    }

    private void editSelectedUser(User user) {
        if (user.getId() == currentUser.getId() && !currentUser.getRole().equalsIgnoreCase("admin")) {
            adminDashboard.showAlert("Warning", "You cannot edit your own account from this interface");
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit User");
        dialogStage.initOwner(adminDashboard.getPrimaryStage());

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #FFFFFF;");

        // En-tête avec avatar
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(25);
        avatarCircle.setFill(Color.web("#72DFD0"));

        // Charger l'image de profil
        ImageView avatarImageView = loadProfileImage(user, 50);
        if (avatarImageView != null) {
            avatarContainer.getChildren().add(avatarImageView);
        } else {
            String initials = adminDashboard.getInitials(user);
            Label avatarText = new Label(initials);
            avatarText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            avatarText.setTextFill(Color.WHITE);
            avatarContainer.getChildren().addAll(avatarCircle, avatarText);
        }

        VBox headerText = new VBox(5);
        Label title = new Label("Edit User");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label(user.getNomComplet());
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#03414D"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        header.getChildren().addAll(avatarContainer, headerText);

        // Formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20, 0, 20, 0));

        TextField nomField = new TextField(user.getNom());
        styleFormTextField(nomField);

        TextField prenomField = new TextField(user.getPrenom());
        styleFormTextField(prenomField);

        TextField emailField = new TextField(user.getEmail());
        styleFormTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New password (leave empty to keep)");
        styleFormTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue(user.getRole());
        styleFormComboBox(roleCombo);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        if (user.getIdGenre() == 1) {
            genreCombo.setValue("Homme");
        } else if (user.getIdGenre() == 2) {
            genreCombo.setValue("Femme");
        } else {
            genreCombo.setValue("Non spécifié");
        }
        styleFormComboBox(genreCombo);

        // Champ pour télécharger une nouvelle photo
        HBox photoBox = new HBox(10);
        photoBox.setAlignment(Pos.CENTER_LEFT);

        Button changePhotoBtn = new Button("📷 Change Photo");
        changePhotoBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");
        changePhotoBtn.setOnAction(e -> changeUserProfilePicture(user, dialogStage));

        Label photoInfo = new Label("Current: " + (user.getPhoto() != null ? user.getPhoto() : "default.jpg"));
        photoInfo.setFont(Font.font("Arial", 11));
        photoInfo.setTextFill(Color.web("#03414D"));

        photoBox.getChildren().addAll(changePhotoBtn, photoInfo);

        formGrid.add(new Label("Last Name *:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("First Name *:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email *:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("New Password:"), 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(new Label("Role *:"), 0, 4);
        formGrid.add(roleCombo, 1, 4);
        formGrid.add(new Label("Gender:"), 0, 5);
        formGrid.add(genreCombo, 1, 5);
        formGrid.add(new Label("Profile Photo:"), 0, 6);
        formGrid.add(photoBox, 1, 6);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setMinHeight(30);

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        buttonBox.setMinHeight(60);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-min-width: 120; -fx-min-height: 40;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #72DFD0; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-min-width: 120; -fx-min-height: 40;");
        saveBtn.setOnAction(e -> {
            if (validateAndUpdateUser(user, nomField, prenomField, emailField, passwordField,
                    roleCombo, genreCombo, errorLabel)) {
                refreshUserTable();
                adminDashboard.showAlert("Success", "User updated successfully!");
                dialogStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        mainLayout.getChildren().addAll(header, formGrid, errorLabel, buttonBox);

        Scene scene = new Scene(mainLayout, 550, 700);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private ImageView loadProfileImage(User user, double size) {
        if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.jpg")) {
            try {
                String photoPath = user.getPhoto();
                File imageFile;

                // Vérifier différents formats de chemin
                if (photoPath.startsWith("profiles/")) {
                    imageFile = new File(photoPath);
                } else if (photoPath.startsWith("profiles\\")) {
                    imageFile = new File(photoPath);
                } else if (photoPath.contains("profile_")) {
                    // Essayer avec le dossier profiles
                    imageFile = new File("profiles/" + photoPath);
                    if (!imageFile.exists()) {
                        imageFile = new File(photoPath);
                    }
                } else {
                    imageFile = new File(photoPath);
                }

                if (imageFile.exists()) {
                    Image avatarImage = new Image("file:" + imageFile.getAbsolutePath(), size, size, true, true, true);
                    ImageView avatarImageView = new ImageView(avatarImage);
                    avatarImageView.setFitWidth(size);
                    avatarImageView.setFitHeight(size);
                    avatarImageView.setPreserveRatio(true);
                    avatarImageView.setStyle("-fx-background-radius: 50%;");
                    return avatarImageView;
                }
            } catch (Exception e) {
                System.out.println("Error loading profile image for user " + user.getId() + ": " + e.getMessage());
                System.out.println("Photo path attempted: " + user.getPhoto());
            }
        }

        // Retourner null si pas d'image, le tableau affichera les initiales
        return null;
    }

    private void changeUserProfilePicture(User user, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(parentStage);
        if (selectedFile != null) {
            try {
                // Créer un dossier pour les photos de profil si nécessaire
                File profileDir = new File("profiles");
                if (!profileDir.exists()) {
                    profileDir.mkdir();
                }

                // Générer un nom de fichier unique
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileExtension = getFileExtension(selectedFile.getName());
                String newFileName = "profile_" + user.getId() + "_" + timestamp + fileExtension;
                File destFile = new File("profiles/" + newFileName);
                Path destPath = destFile.toPath();

                // Copier le fichier
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                // Mettre à jour le chemin de la photo dans l'utilisateur
                user.setPhoto("profiles/" + newFileName);

                // Mettre à jour dans la base de données
                if (userService.updateUser(user)) {
                    adminDashboard.showAlert("Success", "Profile picture updated successfully!");
                    // Rafraîchir le tableau pour afficher la nouvelle image
                    refreshUserTable();
                } else {
                    adminDashboard.showAlert("Error", "Error updating profile picture");
                }

            } catch (Exception e) {
                adminDashboard.showAlert("Error", "Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateAndUpdateUser(User user, TextField nomField, TextField prenomField,
                                          TextField emailField, PasswordField passwordField,
                                          ComboBox<String> roleCombo, ComboBox<String> genreCombo,
                                          Label errorLabel) {
        // Validation
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty()) {
            showError(errorLabel, "Please fill all required fields (*)");
            return false;
        }

        if (!isValidEmail(emailField.getText())) {
            showError(errorLabel, "Please enter a valid email address");
            return false;
        }

        // Vérifier si l'email a changé
        if (!emailField.getText().equals(user.getEmail()) &&
                userService.emailExists(emailField.getText())) {
            showError(errorLabel, "This email is already used by another user");
            return false;
        }

        // Mettre à jour les informations
        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setEmail(emailField.getText());
        user.setRole(roleCombo.getValue());

        // Mettre à jour le mot de passe si fourni
        if (!passwordField.getText().isEmpty()) {
            if (passwordField.getText().length() < 8) {
                showError(errorLabel, "Password must be at least 8 characters long");
                return false;
            }
            user.setPassword(passwordField.getText());
        }

        // Mettre à jour l'idGenre
        String genre = genreCombo.getValue();
        if ("Homme".equals(genre)) {
            user.setIdGenre(1);
        } else if ("Femme".equals(genre)) {
            user.setIdGenre(2);
        } else {
            user.setIdGenre(3);
        }

        // Mettre à jour dans la base de données
        if (userService.updateUser(user)) {
            return true;
        } else {
            showError(errorLabel, "Error updating user");
            return false;
        }
    }

    private void deleteSelectedUser(User user) {
        // Empêcher la suppression de son propre compte
        if (user.getId() == currentUser.getId()) {
            adminDashboard.showAlert("Error", "You cannot delete your own account!");
            return;
        }

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete the user:\n\n" +
                "Name: " + user.getNomComplet() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "Role: " + user.getRole() + "\n\n" +
                "This action cannot be undone.");
        confirmAlert.initOwner(adminDashboard.getPrimaryStage());

        ButtonType yesButton = new ButtonType("Yes, Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            if (userService.deleteUser(user.getId())) {
                adminDashboard.showAlert("Success", "User deleted successfully!");
                refreshUserTable();
            } else {
                adminDashboard.showAlert("Error", "Error deleting user");
            }
        }
    }

    // ============ MÉTHODES UTILITAIRES ============
    private void styleFormTextField(TextField field) {
        field.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px; -fx-text-fill: #000000;");
        field.setPrefWidth(300);
    }

    private void styleFormTextField(PasswordField field) {
        field.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px; -fx-text-fill: #000000;");
        field.setPrefWidth(300);
    }

    private void styleFormComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #72DFD0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px;");
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

    // ============ EXPORT UTILISATEURS ============
    private void exportUsersToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Users");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        fileChooser.setInitialFileName("loopi_users_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(adminDashboard.getPrimaryStage());
        if (file != null) {
            try {
                List<User> users = userTable.getItems();
                exportToCSV(users, file);

                adminDashboard.showAlert("Export Successful",
                        users.size() + " users exported successfully to:\n" +
                                file.getAbsolutePath());

            } catch (Exception e) {
                adminDashboard.showAlert("Export Error", "Error during export: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToCSV(List<User> users, File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            // En-tête
            writer.write("ID,Last Name,First Name,Email,Role,Gender,Registration Date,Status\n");

            // Données
            for (User user : users) {
                String gender = user.getIdGenre() == 1 ? "Homme" : (user.getIdGenre() == 2 ? "Femme" : "Non spécifié");
                String regDate = user.getCreatedAt() != null ?
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";

                String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                        user.getId(),
                        escapeCsv(user.getNom()),
                        escapeCsv(user.getPrenom()),
                        escapeCsv(user.getEmail()),
                        escapeCsv(user.getRole()),
                        escapeCsv(gender),
                        escapeCsv(regDate),
                        "Active"
                );
                writer.write(line);
            }
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