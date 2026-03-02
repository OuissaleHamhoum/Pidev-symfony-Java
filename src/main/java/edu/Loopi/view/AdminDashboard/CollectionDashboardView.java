package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.Collection;
import edu.Loopi.entities.User;
import edu.Loopi.services.CollectionService;
import edu.Loopi.services.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

public class CollectionDashboardView extends VBox {
    private TableView<Collection> table;
    private CollectionService collectionService;
    private UserService userService;
    private AdminDashboard dashboard;
    private TextField searchField;

    // FIX: Using labels to update text, not the container
    private Label totalCollectionsValueLabel;
    private Label totalGoalValueLabel;

    // 1. The master list holds all data
    private ObservableList<Collection> masterData = FXCollections.observableArrayList();
    // 2. The filtered list is what the table actually displays
    private FilteredList<Collection> filteredData;
    // 3. The sorted list to sort completed items to the bottom
    private SortedList<Collection> sortedData;

    public CollectionDashboardView(User currentUser, UserService userService, AdminDashboard dashboard) {
        this.dashboard = dashboard;
        this.collectionService = new CollectionService();
        this.userService = userService;

        this.setSpacing(15);
        this.setPadding(new Insets(25));
        this.setAlignment(Pos.TOP_LEFT);
        this.setStyle("-fx-background-color: #F3F4F6;");

        // --- Header Section ---
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("📦 Gestion des Collections");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.web("#1F2937"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search Bar Container
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 5 15; -fx-border-color: #D1D5DB; -fx-border-radius: 20;");
        Label searchIcon = new Label("🔍");
        searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setStyle("-fx-background-color: transparent; -fx-pref-width: 200px;");
        searchContainer.getChildren().addAll(searchIcon, searchField);

        Button exportBtn = new Button("📥 Export CSV");
        exportBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 15; -fx-font-weight: bold;");
        exportBtn.setOnAction(e -> exportToCSV());

        headerBox.getChildren().addAll(titleLabel, spacer, searchContainer, exportBtn);

        // --- Stats Section ---
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10, 0, 10, 0));

        // FIX: Create cards using VBox
        statsBox.getChildren().addAll(
                createStatsCardVBox("Total Collections", "0"),
                createStatsCardVBox("Objectif Total (kg)", "0.00")
        );

        // --- Table Setup ---
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-background-radius: 10;");

        // --- ROW FACTORY ---
        table.setRowFactory(tv -> new TableRow<Collection>() {
            @Override
            protected void updateItem(Collection item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getCurrent_amount() >= item.getGoal_amount()) {
                    setStyle("-fx-background-color: #d1fae5;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<Collection, String> colTitle = new TableColumn<>("Titre");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(150);

        TableColumn<Collection, String> colCreator = new TableColumn<>("Créateur");
        colCreator.setCellValueFactory(cellData -> {
            Collection c = cellData.getValue();
            User user = userService.getUserById(c.getId_user());
            return new SimpleStringProperty(user != null ? user.getNomComplet() : "Inconnu");
        });
        colCreator.setPrefWidth(120);

        TableColumn<Collection, String> colMaterial = new TableColumn<>("Matériau");
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("material_type"));
        colMaterial.setPrefWidth(100);

        TableColumn<Collection, Double> colCurrent = new TableColumn<>("Actuel (kg)");
        colCurrent.setCellValueFactory(new PropertyValueFactory<>("current_amount"));
        colCurrent.setPrefWidth(100);

        TableColumn<Collection, Double> colGoal = new TableColumn<>("Objectif (kg)");
        colGoal.setCellValueFactory(new PropertyValueFactory<>("goal_amount"));
        colGoal.setPrefWidth(100);

        // --- STATUS COLUMN ---
        TableColumn<Collection, String> colStatus = new TableColumn<>("Statut");
        colStatus.setCellValueFactory(cellData -> {
            Collection c = cellData.getValue();
            if (c.getCurrent_amount() >= c.getGoal_amount()) {
                return new SimpleStringProperty("✅ Terminé");
            } else {
                return new SimpleStringProperty(c.getStatus());
            }
        });
        colStatus.setPrefWidth(100);

        // --- Actions Column ---
        TableColumn<Collection, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                String btnStyle = "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10;";
                editBtn.setStyle(btnStyle + "-fx-background-color: #3B82F6; -fx-text-fill: white;");
                deleteBtn.setStyle(btnStyle + "-fx-background-color: #EF4444; -fx-text-fill: white;");
                pane.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> openEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> confirmDeleteCollection(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colTitle, colCreator, colMaterial, colCurrent, colGoal, colStatus, actionsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // --- FILTERED AND SORTED LOGIC ---
        // 1. Initialize filter and sort based on empty masterData
        filteredData = new FilteredList<>(masterData, p -> true);

        sortedData = new SortedList<>(filteredData, (c1, c2) -> {
            boolean c1Completed = c1.getCurrent_amount() >= c1.getGoal_amount();
            boolean c2Completed = c2.getCurrent_amount() >= c2.getGoal_amount();

            if (c1Completed && !c2Completed) return 1;
            if (!c1Completed && c2Completed) return -1;
            return 0;
        });

        table.setItems(sortedData);

        // 2. Populate masterData
        loadData();

        // --- SEARCH LISTENER ---
        searchField.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(col -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                User creator = userService.getUserById(col.getId_user());
                String creatorName = creator != null ? creator.getNomComplet().toLowerCase() : "";

                return col.getTitle().toLowerCase().contains(lower) ||
                        col.getMaterial_type().toLowerCase().contains(lower) ||
                        creatorName.contains(lower);
            });
            // Update stats when filter changes
            updateStats();
        });

        this.getChildren().addAll(headerBox, statsBox, table);
    }

    // FIX: Changed from Label to VBox to guarantee wrapping
    private VBox createStatsCardVBox(String title, String initialValue) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");

        Label valueLabel = new Label(initialValue);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #1F2937; -fx-font-weight: bold;");

        // Keep references to update the values later
        if (title.contains("Collections")) {
            this.totalCollectionsValueLabel = valueLabel;
        } else {
            this.totalGoalValueLabel = valueLabel;
        }

        VBox card = new VBox(5, titleLabel, valueLabel);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #E5E7EB; -fx-border-radius: 10;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200); // Set a reasonable width

        return card;
    }

    // --- UPDATED STATS METHOD (Safely uses filteredData) ---
    private void updateStats() {
        if (totalCollectionsValueLabel == null || totalGoalValueLabel == null || filteredData == null) {
            return;
        }

        // Calculate based on current items in the table (filteredData)
        int count = filteredData.size();
        double totalGoal = filteredData.stream()
                .mapToDouble(Collection::getGoal_amount)
                .sum();

        // FIX: Update the specific value labels
        totalCollectionsValueLabel.setText(String.valueOf(count));
        totalGoalValueLabel.setText(String.format("%.2f", totalGoal));
    }

    private void loadData() {
        // Fetch data
        List<Collection> data = collectionService.getAllCollections();
        // Populate masterData
        masterData.setAll(data);
        // FIX: Ensure stats are updated ONLY AFTER data is loaded
        updateStats();
    }

    private void openEditDialog(Collection c) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Collection");
        dialog.setHeaderText("Mise à jour : " + c.getTitle());
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(c.getTitle());
        ComboBox<String> matField = new ComboBox<>();
        matField.getItems().addAll("Plastique", "Papier", "Verre", "Métal", "Carton");
        matField.setValue(c.getMaterial_type());
        matField.setEditable(true);

        TextField goalField = new TextField(String.valueOf(c.getGoal_amount()));
        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("Active", "Archivée", "Brouillon");
        statusField.setValue(c.getStatus());

        grid.add(new Label("Titre:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Matériau:"), 0, 1); grid.add(matField, 1, 1);
        grid.add(new Label("Objectif:"), 0, 2); grid.add(goalField, 1, 2);
        grid.add(new Label("Statut:"), 0, 3); grid.add(statusField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveBtn) {
            c.setTitle(titleField.getText());
            c.setMaterial_type(matField.getValue());
            try {
                c.setGoal_amount(Double.parseDouble(goalField.getText()));
            } catch (NumberFormatException e) {
                // Ignore update if number format is invalid
            }
            c.setStatus(statusField.getValue());
            collectionService.updateEntity(c);
            table.refresh();
            updateStats();
            dashboard.showAlert("Succès", "Collection modifiée !");
        }
    }

    private void confirmDeleteCollection(Collection c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer");
        alert.setHeaderText("Confirmation");

        String contentText = "Supprimer " + c.getTitle() + " ?";
        if (c.getCurrent_amount() >= c.getGoal_amount()) {
            contentText = "Cette collection est terminée (" + c.getCurrent_amount() + "/" + c.getGoal_amount() + " kg).\n\n" + contentText;
        }

        alert.setContentText(contentText);
        if (alert.showAndWait().get() == ButtonType.OK) {
            collectionService.deleteEntity(c.getId_collection());
            masterData.remove(c);
            updateStats();
            dashboard.showAlert("Succès", "Collection supprimée.");
        }
    }

    public void showCollectionDashboardView(StackPane mainContentArea, boolean isDarkMode) {
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(this);
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Titre,Createur,Materiau,Actuel(kg),Objectif(kg),Statut");
                for (Collection c : masterData) {
                    User creator = userService.getUserById(c.getId_user());
                    String creatorName = creator != null ? creator.getNomComplet() : "Inconnu";
                    pw.println(c.getTitle() + "," + creatorName + "," + c.getMaterial_type() + "," + c.getCurrent_amount() + "," + c.getGoal_amount() + "," + c.getStatus());
                }
                dashboard.showAlert("Succès", "Export réussi !");
            } catch (Exception e) {
                dashboard.showAlert("Erreur", "Échec de l'export.");
            }
        }
    }

    public Node getView() { return this; }
}