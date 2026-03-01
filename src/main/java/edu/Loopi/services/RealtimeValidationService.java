package edu.Loopi.services;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class RealtimeValidationService {

    public void setupEmailValidation(TextField emailField, Label errorLabel) {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            String email = newValue.trim();
            if (email.isEmpty()) {
                errorLabel.setText("⚠ Email requis");
                errorLabel.setStyle("-fx-text-fill: #f59e0b;");
                errorLabel.setVisible(true);
            } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errorLabel.setText("❌ Format d'email invalide");
                errorLabel.setStyle("-fx-text-fill: #ef4444;");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        });
    }

    public void setupPasswordValidation(PasswordField passwordField, Label strengthLabel) {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            String password = newValue;
            if (password.isEmpty()) {
                strengthLabel.setVisible(false);
            } else {
                strengthLabel.setVisible(true);
                if (password.length() < 6) {
                    strengthLabel.setText("🔴 Faible (min 6 caractères)");
                    strengthLabel.setStyle("-fx-text-fill: #ef4444;");
                } else if (password.length() < 10) {
                    strengthLabel.setText("🟡 Moyen");
                    strengthLabel.setStyle("-fx-text-fill: #f59e0b;");
                } else {
                    strengthLabel.setText("🟢 Fort");
                    strengthLabel.setStyle("-fx-text-fill: #10b981;");
                }
            }
        });
    }

    public void setupPasswordValidation(TextField textField, Label strengthLabel) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String password = newValue;
            if (password.isEmpty()) {
                strengthLabel.setVisible(false);
            } else {
                strengthLabel.setVisible(true);
                if (password.length() < 6) {
                    strengthLabel.setText("🔴 Faible (min 6 caractères)");
                    strengthLabel.setStyle("-fx-text-fill: #ef4444;");
                } else if (password.length() < 10) {
                    strengthLabel.setText("🟡 Moyen");
                    strengthLabel.setStyle("-fx-text-fill: #f59e0b;");
                } else {
                    strengthLabel.setText("🟢 Fort");
                    strengthLabel.setStyle("-fx-text-fill: #10b981;");
                }
            }
        });
    }

    // NOUVELLE MÉTHODE AJOUTÉE
    public void setupPasswordConfirmation(
            PasswordField passwordField,
            TextField visiblePasswordField,
            PasswordField confirmPasswordField,
            TextField visibleConfirmPasswordField,
            Label errorLabel) {

        // Listener pour le champ mot de passe (caché)
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordMatch(passwordField, visiblePasswordField, confirmPasswordField, visibleConfirmPasswordField, errorLabel);
        });

        // Listener pour le champ mot de passe (visible)
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordMatch(passwordField, visiblePasswordField, confirmPasswordField, visibleConfirmPasswordField, errorLabel);
        });

        // Listener pour le champ confirmation (caché)
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordMatch(passwordField, visiblePasswordField, confirmPasswordField, visibleConfirmPasswordField, errorLabel);
        });

        // Listener pour le champ confirmation (visible)
        visibleConfirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordMatch(passwordField, visiblePasswordField, confirmPasswordField, visibleConfirmPasswordField, errorLabel);
        });
    }

    private void checkPasswordMatch(
            PasswordField passwordField,
            TextField visiblePasswordField,
            PasswordField confirmPasswordField,
            TextField visibleConfirmPasswordField,
            Label errorLabel) {

        // Récupérer les mots de passe des champs actifs
        String password = passwordField.isVisible() ?
                passwordField.getText() : visiblePasswordField.getText();
        String confirmPassword = confirmPasswordField.isVisible() ?
                confirmPasswordField.getText() : visibleConfirmPasswordField.getText();

        if (confirmPassword.isEmpty()) {
            errorLabel.setVisible(false);
        } else if (!password.equals(confirmPassword)) {
            errorLabel.setText("❌ Les mots de passe ne correspondent pas");
            errorLabel.setStyle("-fx-text-fill: #ef4444;");
            errorLabel.setVisible(true);
        } else {
            errorLabel.setVisible(false);
        }
    }
}