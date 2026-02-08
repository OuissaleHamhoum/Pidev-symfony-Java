package edu.Loopi.view.AdminDashboard;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AnalyticsView {
    private User currentUser;
    private UserService userService;
    private AdminDashboard adminDashboard;

    public AnalyticsView(User currentUser, UserService userService, AdminDashboard adminDashboard) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.adminDashboard = adminDashboard;
    }

    public void showAnalyticsView(StackPane mainContentArea) {
        VBox analyticsView = createAnalyticsView();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(analyticsView);
    }

    private VBox createAnalyticsView() {
        VBox analyticsView = new VBox(20);
        analyticsView.setPadding(new Insets(30));
        analyticsView.setStyle("-fx-background-color: #E6F8F6;");

        Label title = new Label("Analytics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Detailed analytics and reports");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#03414D"));

        // Contenu de l'analytics (à développer)
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label comingSoon = new Label("Analytics features coming soon...");
        comingSoon.setFont(Font.font("Arial", 16));
        comingSoon.setTextFill(Color.web("#03414D"));

        content.getChildren().add(comingSoon);
        analyticsView.getChildren().addAll(title, subtitle, content);

        return analyticsView;
    }
}