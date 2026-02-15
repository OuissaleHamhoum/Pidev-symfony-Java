package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SettingsView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;

    public SettingsView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    public void showSettingsView(StackPane mainContentArea, boolean isDarkMode) {
        ScrollPane scrollPane = createSettingsView(isDarkMode);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(scrollPane);
    }

    private ScrollPane createSettingsView(boolean isDarkMode) {
        VBox container = new VBox(24);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + adminDashboard.getCardBg() + "; -fx-background-radius: 12; -fx-padding: 24;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("Paramètres");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(adminDashboard.getTextColor()));

        Label subtitle = new Label("Configuration du système");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web(adminDashboard.getTextColorMuted()));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        header.getChildren().add(headerText);

        VBox settingsContent = new VBox(16);
        settingsContent.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#F9FAFB") +
                "; -fx-background-radius: 12; -fx-padding: 24; -fx-border-color: " + adminDashboard.getBorderColor() +
                "; -fx-border-radius: 12;");

        settingsContent.getChildren().addAll(
                createGeneralSettings(isDarkMode),
                createSecuritySettings(isDarkMode),
                createAppearanceSettings(isDarkMode),
                createNotificationSettings(isDarkMode),
                createSystemSettings(isDarkMode)
        );

        container.getChildren().addAll(header, settingsContent);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private VBox createGeneralSettings(boolean isDarkMode) {
        VBox section = createSection("Général");

        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Français", "English", "العربية");
        languageCombo.setValue("Français");
        styleComboBox(languageCombo, isDarkMode);

        ComboBox<String> timezoneCombo = new ComboBox<>();
        timezoneCombo.getItems().addAll("UTC", "GMT+1 (Paris)", "GMT+2", "GMT+3");
        timezoneCombo.setValue("GMT+1 (Paris)");
        styleComboBox(timezoneCombo, isDarkMode);

        CheckBox autoSaveCheck = new CheckBox("Sauvegarde automatique");
        autoSaveCheck.setSelected(true);
        styleCheckBox(autoSaveCheck, isDarkMode);

        section.getChildren().addAll(
                createSettingRow("Langue", languageCombo),
                createSettingRow("Fuseau horaire", timezoneCombo),
                createSettingRow("", autoSaveCheck)
        );

        return section;
    }

    private VBox createSecuritySettings(boolean isDarkMode) {
        VBox section = createSection("Sécurité");

        CheckBox twoFactorCheck = new CheckBox("Authentification à deux facteurs");
        twoFactorCheck.setSelected(false);
        styleCheckBox(twoFactorCheck, isDarkMode);

        ComboBox<String> sessionCombo = new ComboBox<>();
        sessionCombo.getItems().addAll("30 minutes", "1 heure", "2 heures", "4 heures", "8 heures");
        sessionCombo.setValue("2 heures");
        styleComboBox(sessionCombo, isDarkMode);

        CheckBox loginAlertCheck = new CheckBox("Alerte en cas de nouvelle connexion");
        loginAlertCheck.setSelected(true);
        styleCheckBox(loginAlertCheck, isDarkMode);

        section.getChildren().addAll(
                createSettingRow("", twoFactorCheck),
                createSettingRow("Expiration session", sessionCombo),
                createSettingRow("", loginAlertCheck)
        );

        return section;
    }

    private VBox createAppearanceSettings(boolean isDarkMode) {
        VBox section = createSection("Apparence");

        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Clair", "Sombre", "Système");
        themeCombo.setValue(adminDashboard.isDarkMode() ? "Sombre" : "Clair");
        styleComboBox(themeCombo, isDarkMode);
        themeCombo.setOnAction(e -> {
            String selected = themeCombo.getValue();
            if (selected.equals("Sombre") && !adminDashboard.isDarkMode()) {
                adminDashboard.toggleTheme();
            } else if (selected.equals("Clair") && adminDashboard.isDarkMode()) {
                adminDashboard.toggleTheme();
            }
        });

        ComboBox<String> fontSizeCombo = new ComboBox<>();
        fontSizeCombo.getItems().addAll("Petit", "Moyen", "Grand");
        fontSizeCombo.setValue("Moyen");
        styleComboBox(fontSizeCombo, isDarkMode);

        CheckBox compactViewCheck = new CheckBox("Vue compacte");
        compactViewCheck.setSelected(false);
        styleCheckBox(compactViewCheck, isDarkMode);

        section.getChildren().addAll(
                createSettingRow("Thème", themeCombo),
                createSettingRow("Taille police", fontSizeCombo),
                createSettingRow("", compactViewCheck)
        );

        return section;
    }

    private VBox createNotificationSettings(boolean isDarkMode) {
        VBox section = createSection("Notifications");

        CheckBox emailNotifCheck = new CheckBox("Notifications par email");
        emailNotifCheck.setSelected(true);
        styleCheckBox(emailNotifCheck, isDarkMode);

        CheckBox pushNotifCheck = new CheckBox("Notifications push");
        pushNotifCheck.setSelected(true);
        styleCheckBox(pushNotifCheck, isDarkMode);

        CheckBox digestCheck = new CheckBox("Résumé hebdomadaire");
        digestCheck.setSelected(false);
        styleCheckBox(digestCheck, isDarkMode);

        section.getChildren().addAll(
                createSettingRow("", emailNotifCheck),
                createSettingRow("", pushNotifCheck),
                createSettingRow("", digestCheck)
        );

        return section;
    }

    private VBox createSystemSettings(boolean isDarkMode) {
        VBox section = createSection("Système");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button backupBtn = new Button("📦 Sauvegarder");
        backupBtn.setStyle("-fx-background-color: " + adminDashboard.getAccentColor() +
                "; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        backupBtn.setOnAction(e -> adminDashboard.showAlert("Sauvegarde", "Sauvegarde effectuée avec succès"));

        Button restoreBtn = new Button("🔄 Restaurer");
        restoreBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + adminDashboard.getTextColor() +
                "; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: " +
                adminDashboard.getBorderColor() + "; -fx-border-radius: 6; -fx-font-size: 13px;");
        restoreBtn.setOnAction(e -> adminDashboard.showAlert("Restauration", "Fonctionnalité à venir"));

        Button clearCacheBtn = new Button("🗑️ Vider cache");
        clearCacheBtn.setStyle("-fx-background-color: " + adminDashboard.getDangerColor() +
                "; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        clearCacheBtn.setOnAction(e -> adminDashboard.showAlert("Cache", "Cache vidé avec succès"));

        buttonBox.getChildren().addAll(backupBtn, restoreBtn, clearCacheBtn);

        section.getChildren().add(buttonBox);
        return section;
    }

    private VBox createSection(String title) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(0, 0, 16, 0));

        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(adminDashboard.getTextColor()));

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + adminDashboard.getBorderColor() + ";");

        section.getChildren().addAll(sectionTitle, separator);
        return section;
    }

    private HBox createSettingRow(String label, Node control) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));

        if (!label.isEmpty()) {
            Label labelNode = new Label(label);
            labelNode.setFont(Font.font("System", FontWeight.NORMAL, 13));
            labelNode.setTextFill(Color.web(adminDashboard.getTextColor()));
            labelNode.setPrefWidth(120);
            row.getChildren().add(labelNode);
        }

        row.getChildren().add(control);
        return row;
    }

    private void styleComboBox(ComboBox<String> comboBox, boolean isDarkMode) {
        comboBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2D3748" : "#FFFFFF") +
                "; -fx-border-color: " + adminDashboard.getBorderColor() + "; -fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 13px; -fx-text-fill: " + adminDashboard.getTextColor() + ";");
        comboBox.setPrefWidth(200);
    }

    private void styleCheckBox(CheckBox checkBox, boolean isDarkMode) {
        checkBox.setStyle("-fx-text-fill: " + adminDashboard.getTextColor() + "; -fx-font-size: 13px;");
    }
}