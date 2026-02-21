package com.expensetracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private TextField securityField;
    @FXML private VBox securityBox;
    @FXML private Label statusLabel;
    @FXML private Button mainAuthBtn;
    @FXML private Text authTitle;
    @FXML private Hyperlink toggleLink;

    private boolean isLoginMode = true;

    @FXML
    public void initialize() {

        Database.initialize();
    }
    @FXML
    private void handleUserExport() {
        Database.exportUsersToCSV();
    }

    @FXML
    private void handlePrimaryAction() {
        if (isLoginMode) {
            loginUser();
        } else {
            registerUser();
        }
    }

    private void loginUser() {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userField.getText());
            pstmt.setString(2, passField.getText());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                navigateToDashboard();
            } else {
                statusLabel.setText("Invalid username or password.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void registerUser() {
        String sql = "INSERT INTO users(username, password, security_answer) VALUES(?,?,?)";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userField.getText().isEmpty() || passField.getText().isEmpty() || securityField.getText().isEmpty()) {
                statusLabel.setText("All fields are required for registration.");
                return;
            }

            pstmt.setString(1, userField.getText());
            pstmt.setString(2, passField.getText());
            pstmt.setString(3, securityField.getText().toLowerCase());
            pstmt.executeUpdate();

            statusLabel.setText("Registration successful! Please sign in.");
            toggleAuthMode(); // Switch back to login
        } catch (SQLException e) {
            statusLabel.setText("Username already exists.");
        }
    }

    @FXML
    private void handleForgot() {
        if (userField.getText().isEmpty()) {
            statusLabel.setText("Enter username first to recover password.");
            return;
        }

        String sql = "SELECT password FROM users WHERE username = ? AND security_answer = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userField.getText());
            pstmt.setString(2, securityField.getText().toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Password Recovery");
                alert.setHeaderText("Account Verified");
                alert.setContentText("Your password is: " + rs.getString("password"));
                alert.showAndWait();
            } else {
                securityBox.setVisible(true);
                securityBox.setManaged(true);
                statusLabel.setText("Please enter your security answer.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleAuthMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            authTitle.setText("Login to your account");
            mainAuthBtn.setText("SIGN IN");
            toggleLink.setText("Create an account");
            securityBox.setVisible(false);
            securityBox.setManaged(false);
        } else {
            authTitle.setText("Create a new account");
            mainAuthBtn.setText("REGISTER");
            toggleLink.setText("Already have an account?");
            securityBox.setVisible(true);
            securityBox.setManaged(true);
        }
    }

    private void navigateToDashboard() throws IOException {
        // Loading the main expense_tracker.fml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/expense_tracker.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) userField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("CoinCare Dashboard");
        stage.show();
    }
}