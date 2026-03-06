package edu.Loopi.view;

import edu.Loopi.entities.Notification;
import edu.Loopi.entities.User;
import edu.Loopi.services.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsContentView {
    private User currentUser;
    private NotificationService notificationService;
    private ListView<Notification> notificationList;
    private Label unreadCountLabel;
    private ComboBox<String> typeFilter;

    public NotificationsContentView(User currentUser, NotificationService notificationService) {
        this.currentUser = currentUser;
        this.notificationService = notificationService;
    }

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();
        HBox statsBox = createStatsBox();
        HBox filterBar = createFilterBar();

        notificationList = new ListView<>();
        notificationList.setPrefHeight(500);
        notificationList.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createNotificationCell(n));

                    setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            showNotificationDetails(n);
                        }
                    });
                }
            }
        });

        loadNotifications();

        Button markAllReadBtn = new Button("✓ Tout marquer comme lu");
        markAllReadBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        markAllReadBtn.setMaxWidth(Double.MAX_VALUE);
        markAllReadBtn.setOnAction(e -> {
            notificationService.marquerToutesCommeLues(currentUser.getId());
            loadNotifications();
            updateStats();
        });

        container.getChildren().addAll(header, statsBox, filterBar, notificationList, markAllReadBtn);
        VBox.setVgrow(notificationList, Priority.ALWAYS);

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;");

        Label iconLabel = new Label("🔔");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));

        VBox headerText = new VBox(5);
        Label title = new Label("Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0f172a"));

        unreadCountLabel = new Label();
        unreadCountLabel.setFont(Font.font("Segoe UI", 14));
        unreadCountLabel.setTextFill(Color.web("#475569"));

        headerText.getChildren().addAll(title, unreadCountLabel);
        header.getChildren().addAll(iconLabel, headerText);

        return header;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        statsBox.setAlignment(Pos.CENTER);

        List<Notification> allNotifs = notificationService.getNotificationsForOrganisateur(currentUser.getId());
        int total = allNotifs.size();
        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());

        VBox totalCard = createStatCard("📊 Total", String.valueOf(total), "#3b82f6");
        VBox unreadCard = createStatCard("🔔 Non lues", String.valueOf(nonLues), "#f97316");
        VBox readCard = createStatCard("✅ Lues", String.valueOf(total - nonLues), "#10b981");

        statsBox.getChildren().addAll(totalCard, unreadCard, readCard);
        return statsBox;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 30, 15, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.web("#64748b"));

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setPadding(new Insets(10, 0, 10, 0));
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filtrer par type:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        filterLabel.setTextFill(Color.web("#0f172a"));

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Toutes", "Approbations", "Refus", "Participants", "Publications", "Annulations");
        typeFilter.setValue("Toutes");
        typeFilter.setStyle("-fx-background-radius: 8; -fx-padding: 8 15; -fx-background-color: white; -fx-border-color: #e2e8f0;");
        typeFilter.setOnAction(e -> filterNotifications(typeFilter.getValue()));

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            loadNotifications();
            updateStats();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(filterLabel, typeFilter, spacer, refreshBtn);
        return filterBar;
    }

    private void filterNotifications(String filterType) {
        List<Notification> allNotifs = notificationService.getNotificationsForOrganisateur(currentUser.getId());

        if ("Toutes".equals(filterType)) {
            notificationList.getItems().setAll(allNotifs);
        } else {
            List<Notification> filtered;
            switch (filterType) {
                case "Approbations":
                    filtered = allNotifs.stream()
                            .filter(n -> n.getType().contains("APPROUVE"))
                            .collect(Collectors.toList());
                    break;
                case "Refus":
                    filtered = allNotifs.stream()
                            .filter(n -> n.getType().contains("REFUSE"))
                            .collect(Collectors.toList());
                    break;
                case "Participants":
                    filtered = allNotifs.stream()
                            .filter(n -> n.getType().contains("PARTICIPANT"))
                            .collect(Collectors.toList());
                    break;
                case "Publications":
                    filtered = allNotifs.stream()
                            .filter(n -> n.getType().contains("PUBLIE"))
                            .collect(Collectors.toList());
                    break;
                case "Annulations":
                    filtered = allNotifs.stream()
                            .filter(n -> n.getType().contains("ANNULE"))
                            .collect(Collectors.toList());
                    break;
                default:
                    filtered = allNotifs;
            }
            notificationList.getItems().setAll(filtered);
        }
    }

    private void loadNotifications() {
        List<Notification> notifications = notificationService.getNotificationsForOrganisateur(currentUser.getId());
        notificationList.getItems().setAll(notifications);
        updateStats();
    }

    private void updateStats() {
        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());
        unreadCountLabel.setText(nonLues + " notification(s) non lue(s)");
    }

    private VBox createNotificationCell(Notification n) {
        VBox cell = new VBox(12);
        cell.setPadding(new Insets(15));
        cell.setStyle("-fx-background-color: " + (n.isRead() ? "#f8fafc" : "#eff6ff") + "; " +
                "-fx-background-radius: 8; -fx-border-color: " + (n.isRead() ? "#e2e8f0" : "#3b82f6") + "; " +
                "-fx-border-radius: 8; -fx-border-width: " + (n.isRead() ? "1" : "2") + ";");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = "";
        String color = "";
        String sourceInfo = "";

        switch (n.getType()) {
            case "EVENEMENT_APPROUVE":
                icon = "✅";
                color = "#10b981";
                sourceInfo = "Admin: " + (n.getNomAdmin() != null ? n.getNomAdmin() : "Administrateur");
                break;
            case "EVENEMENT_REFUSE":
                icon = "❌";
                color = "#ef4444";
                sourceInfo = "Admin: " + (n.getNomAdmin() != null ? n.getNomAdmin() : "Administrateur");
                break;
            case "NOUVEAU_PARTICIPANT":
                icon = "👤";
                color = "#3b82f6";
                sourceInfo = "Participant: " + (n.getNomParticipant() != null ? n.getNomParticipant() : "Inconnu");
                break;
            case "PARTICIPANT_ANNULE":
                icon = "🚫";
                color = "#f97316";
                sourceInfo = "Participant: " + (n.getNomParticipant() != null ? n.getNomParticipant() : "Inconnu");
                break;
            case "EVENEMENT_PUBLIE":
                icon = "📢";
                color = "#8b5cf6";
                sourceInfo = "Système";
                break;
            case "EVENEMENT_MODIFIE":
                icon = "✏️";
                color = "#f59e0b";
                sourceInfo = "Système";
                break;
            case "PARTICIPATION":
                icon = "✅";
                color = "#10b981";
                sourceInfo = "Participant: " + (n.getNomParticipant() != null ? n.getNomParticipant() : "Inconnu");
                break;
            case "ANNULATION":
                icon = "❌";
                color = "#ef4444";
                sourceInfo = "Participant: " + (n.getNomParticipant() != null ? n.getNomParticipant() : "Inconnu");
                break;
            default:
                icon = "🔔";
                color = "#6b7280";
                sourceInfo = "Système";
        }

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        VBox textContent = new VBox(5);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(n.getTitre());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0f172a"));

        if (!n.isRead()) {
            Label newBadge = new Label("NOUVEAU");
            newBadge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 10; -fx-font-weight: bold;");
            titleRow.getChildren().addAll(titleLabel, newBadge);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        Label messageLabel = new Label(n.getMessage());
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setTextFill(Color.web("#475569"));
        messageLabel.setWrapText(true);

        VBox detailsBox = new VBox(5);
        detailsBox.setPadding(new Insets(10, 0, 0, 20));

        Label sourceLabel = new Label("📨 " + sourceInfo);
        sourceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        sourceLabel.setTextFill(Color.web(color));
        detailsBox.getChildren().add(sourceLabel);

        if (n.getNomAdmin() != null && !n.getNomAdmin().isEmpty() && !sourceInfo.contains("Admin")) {
            HBox adminRow = new HBox(10);
            adminRow.setAlignment(Pos.CENTER_LEFT);
            Label adminIcon = new Label("👤 Admin:");
            adminIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            adminIcon.setTextFill(Color.web("#475569"));
            Label adminName = new Label(n.getNomAdmin());
            adminName.setFont(Font.font("Segoe UI", 12));
            adminName.setTextFill(Color.web("#0f172a"));
            if (n.getEmailAdmin() != null && !n.getEmailAdmin().isEmpty()) {
                Label adminEmail = new Label("(" + n.getEmailAdmin() + ")");
                adminEmail.setFont(Font.font("Segoe UI", 10));
                adminEmail.setTextFill(Color.web("#64748b"));
                adminRow.getChildren().addAll(adminIcon, adminName, adminEmail);
            } else {
                adminRow.getChildren().addAll(adminIcon, adminName);
            }
            detailsBox.getChildren().add(adminRow);
        }

        if (n.getCommentaire() != null && !n.getCommentaire().isEmpty()) {
            HBox commentRow = new HBox(10);
            commentRow.setAlignment(Pos.CENTER_LEFT);
            Label commentIcon = new Label("💬 Commentaire:");
            commentIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            commentIcon.setTextFill(Color.web("#475569"));
            Label commentText = new Label(n.getCommentaire());
            commentText.setFont(Font.font("Segoe UI", 12));
            commentText.setTextFill(Color.web("#0f172a"));
            commentText.setWrapText(true);
            commentRow.getChildren().addAll(commentIcon, commentText);
            detailsBox.getChildren().add(commentRow);
        }

        if (n.getNomParticipant() != null && !n.getNomParticipant().isEmpty() && !sourceInfo.contains("Participant")) {
            HBox participantRow = new HBox(10);
            participantRow.setAlignment(Pos.CENTER_LEFT);
            Label partIcon = new Label("👤 Participant:");
            partIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            partIcon.setTextFill(Color.web("#475569"));
            Label partName = new Label(n.getNomParticipant());
            partName.setFont(Font.font("Segoe UI", 12));
            partName.setTextFill(Color.web("#0f172a"));
            if (n.getEmailParticipant() != null && !n.getEmailParticipant().isEmpty()) {
                Label partEmail = new Label("(" + n.getEmailParticipant() + ")");
                partEmail.setFont(Font.font("Segoe UI", 10));
                partEmail.setTextFill(Color.web("#64748b"));
                participantRow.getChildren().addAll(partIcon, partName, partEmail);
            } else {
                participantRow.getChildren().addAll(partIcon, partName);
            }
            detailsBox.getChildren().add(participantRow);
        }

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(n.getFormattedDate());
        dateLabel.setFont(Font.font(11));
        dateLabel.setTextFill(Color.web("#64748b"));

        if (n.getEventTitre() != null && !n.getEventTitre().isEmpty()) {
            Label eventLabel = new Label("📅 " + n.getEventTitre());
            eventLabel.setFont(Font.font(11));
            eventLabel.setTextFill(Color.web("#64748b"));
            footer.getChildren().add(eventLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markReadBtn = new Button("✓ Marquer comme lu");
        markReadBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8;");
        markReadBtn.setVisible(!n.isRead());
        markReadBtn.setOnAction(e -> {
            notificationService.marquerCommeLue(n.getId());
            loadNotifications();
            updateStats();
        });

        textContent.getChildren().addAll(titleRow, messageLabel);
        if (!detailsBox.getChildren().isEmpty()) {
            textContent.getChildren().add(detailsBox);
        }
        textContent.getChildren().add(footer);

        header.getChildren().addAll(iconLabel, textContent, spacer, markReadBtn);

        cell.getChildren().add(header);

        cell.setOnMouseClicked(e -> {
            if (!n.isRead()) {
                notificationService.marquerCommeLue(n.getId());
                loadNotifications();
                updateStats();
            }
        });

        return cell;
    }

    private void showNotificationDetails(Notification notification) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(notificationList.getScene().getWindow());
        dialog.setTitle("Détails de la notification");

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(600);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = "";
        String color = "";
        String sourceInfo = "";

        switch (notification.getType()) {
            case "EVENEMENT_APPROUVE":
                icon = "✅";
                color = "#10b981";
                sourceInfo = "Admin: " + (notification.getNomAdmin() != null ? notification.getNomAdmin() : "Administrateur");
                break;
            case "EVENEMENT_REFUSE":
                icon = "❌";
                color = "#ef4444";
                sourceInfo = "Admin: " + (notification.getNomAdmin() != null ? notification.getNomAdmin() : "Administrateur");
                break;
            case "NOUVEAU_PARTICIPANT":
                icon = "👤";
                color = "#3b82f6";
                sourceInfo = "Participant: " + (notification.getNomParticipant() != null ? notification.getNomParticipant() : "Inconnu");
                break;
            case "PARTICIPANT_ANNULE":
                icon = "🚫";
                color = "#f97316";
                sourceInfo = "Participant: " + (notification.getNomParticipant() != null ? notification.getNomParticipant() : "Inconnu");
                break;
            case "EVENEMENT_PUBLIE":
                icon = "📢";
                color = "#8b5cf6";
                sourceInfo = "Système";
                break;
            case "EVENEMENT_MODIFIE":
                icon = "✏️";
                color = "#f59e0b";
                sourceInfo = "Système";
                break;
            case "PARTICIPATION":
                icon = "✅";
                color = "#10b981";
                sourceInfo = "Participant: " + (notification.getNomParticipant() != null ? notification.getNomParticipant() : "Inconnu");
                break;
            case "ANNULATION":
                icon = "❌";
                color = "#ef4444";
                sourceInfo = "Participant: " + (notification.getNomParticipant() != null ? notification.getNomParticipant() : "Inconnu");
                break;
            default:
                icon = "🔔";
                color = "#6b7280";
                sourceInfo = "Système";
        }

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));

        VBox headerText = new VBox(5);
        Label titleLabel = new Label(notification.getTitre());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(color));
        titleLabel.setWrapText(true);

        Label dateLabel = new Label(notification.getFormattedDate());
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(titleLabel, dateLabel);
        header.getChildren().addAll(iconLabel, headerText);

        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));

        VBox detailsBox = new VBox(15);
        detailsBox.setPadding(new Insets(10, 0, 10, 0));

        HBox sourceBox = new HBox(10);
        sourceBox.setAlignment(Pos.CENTER_LEFT);
        Label sourceIcon = new Label("📨");
        sourceIcon.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label sourceText = new Label("Source: " + sourceInfo);
        sourceText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sourceText.setTextFill(Color.web(color));
        sourceBox.getChildren().addAll(sourceIcon, sourceText);
        detailsBox.getChildren().add(sourceBox);

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setTextFill(Color.web("#1e293b"));
        messageLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0;");

        detailsBox.getChildren().add(messageLabel);

        if (notification.getNomAdmin() != null && !notification.getNomAdmin().isEmpty()) {
            VBox adminBox = new VBox(8);
            adminBox.setPadding(new Insets(15));
            adminBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

            Label adminTitle = new Label("👤 Informations de l'administrateur");
            adminTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            adminTitle.setTextFill(Color.web("#2196F3"));

            GridPane adminGrid = new GridPane();
            adminGrid.setHgap(15);
            adminGrid.setVgap(8);

            adminGrid.add(new Label("Nom:"), 0, 0);
            Label adminName = new Label(notification.getNomAdmin());
            adminName.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            adminName.setTextFill(Color.web("#0f172a"));
            adminGrid.add(adminName, 1, 0);

            if (notification.getEmailAdmin() != null && !notification.getEmailAdmin().isEmpty()) {
                adminGrid.add(new Label("Email:"), 0, 1);
                Label adminEmail = new Label(notification.getEmailAdmin());
                adminEmail.setFont(Font.font("Arial", 13));
                adminEmail.setTextFill(Color.web("#2563eb"));
                adminGrid.add(adminEmail, 1, 1);
            }

            adminBox.getChildren().addAll(adminTitle, adminGrid);
            detailsBox.getChildren().add(adminBox);
        }

        if (notification.getCommentaire() != null && !notification.getCommentaire().isEmpty()) {
            VBox commentBox = new VBox(8);
            commentBox.setPadding(new Insets(15));
            commentBox.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 8; -fx-border-color: #fdba74; -fx-border-radius: 8;");

            Label commentTitle = new Label("💬 Commentaire");
            commentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            commentTitle.setTextFill(Color.web("#c2410c"));

            TextArea commentArea = new TextArea(notification.getCommentaire());
            commentArea.setWrapText(true);
            commentArea.setEditable(false);
            commentArea.setPrefRowCount(4);
            commentArea.setStyle("-fx-control-inner-background: #fff7ed; -fx-text-fill: #0f172a;");

            commentBox.getChildren().addAll(commentTitle, commentArea);
            detailsBox.getChildren().add(commentBox);
        }

        if (notification.getNomParticipant() != null && !notification.getNomParticipant().isEmpty()) {
            VBox participantBox = new VBox(8);
            participantBox.setPadding(new Insets(15));
            participantBox.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 8; -fx-border-color: #7dd3fc; -fx-border-radius: 8;");

            Label participantTitle = new Label("👤 Informations du participant");
            participantTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            participantTitle.setTextFill(Color.web("#0369a1"));

            GridPane participantGrid = new GridPane();
            participantGrid.setHgap(15);
            participantGrid.setVgap(8);

            participantGrid.add(new Label("Nom:"), 0, 0);
            Label partName = new Label(notification.getNomParticipant());
            partName.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            partName.setTextFill(Color.web("#0f172a"));
            participantGrid.add(partName, 1, 0);

            if (notification.getEmailParticipant() != null && !notification.getEmailParticipant().isEmpty()) {
                participantGrid.add(new Label("Email:"), 0, 1);
                Label partEmail = new Label(notification.getEmailParticipant());
                partEmail.setFont(Font.font("Arial", 13));
                partEmail.setTextFill(Color.web("#0369a1"));
                participantGrid.add(partEmail, 1, 1);
            }

            participantBox.getChildren().addAll(participantTitle, participantGrid);
            detailsBox.getChildren().add(participantBox);
        }

        if (notification.getEventTitre() != null && !notification.getEventTitre().isEmpty()) {
            HBox eventBox = new HBox(15);
            eventBox.setAlignment(Pos.CENTER_LEFT);
            eventBox.setPadding(new Insets(10));
            eventBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");

            Label eventIcon = new Label("📅");
            eventIcon.setFont(Font.font("Arial", FontWeight.BOLD, 18));

            Label eventLabel = new Label("Événement concerné:");
            eventLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            eventLabel.setTextFill(Color.web("#475569"));

            Label eventName = new Label(notification.getEventTitre());
            eventName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            eventName.setTextFill(Color.web("#2196F3"));

            eventBox.getChildren().addAll(eventIcon, eventLabel, eventName);
            detailsBox.getChildren().add(eventBox);
        }

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(header, sep, detailsBox, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 650, 700);
        dialog.setScene(scene);
        dialog.show();
    }
}