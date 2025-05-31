package src.main.java.auth;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONObject;

import src.main.java.App;
import src.server.java.ServerURL;

public class Login extends javax.swing.JPanel {

    private App app;
    Authenticator authenticator = new Authenticator();

    // Components
    private javax.swing.JButton loginBtn;
    private javax.swing.JButton signupBtn;
    private javax.swing.JButton forgotpasswordBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField emailField;
    private javax.swing.JComboBox<String> branchField;

    public Login(App app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        branchField = new javax.swing.JComboBox<>();
        forgotpasswordBtn = new javax.swing.JButton();
        loginBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        signupBtn = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Login Or Signup");

        emailField.setToolTipText("Your registered email");
        emailField.setBorder(javax.swing.BorderFactory.createTitledBorder("Email address"));
        emailField.setOpaque(false);

        passwordField.setBorder(javax.swing.BorderFactory.createTitledBorder("Password"));
        passwordField.setOpaque(false);

        String postData = "action=get&client_fetch_token_request=;;mslaundryshop2025;;";
        HttpURLConnection conn = new ServerURL().createPostConnection("get_branches.php", postData);
        JSONArray response = parseJsonArrayResponse(conn);
        if (response != null) {
                    String[] comboItems = new String[response.length() + 1];
                    comboItems[0] = "Select branch";

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.getJSONObject(i);
                        comboItems[i + 1] = obj.getString("branchid") + " (" + obj.getString("name") + ")";
                    }

        branchField.setModel(new DefaultComboBoxModel<>(comboItems));
        branchField.setOpaque(false);

        }

        // Forgot Password Button
        forgotpasswordBtn.setFont(new java.awt.Font("Segoe UI", 2, 14));
        forgotpasswordBtn.setForeground(new java.awt.Color(51, 102, 255));
        forgotpasswordBtn.setText("Forgot password?");
        forgotpasswordBtn.setBorderPainted(false);
        forgotpasswordBtn.setContentAreaFilled(false);
        forgotpasswordBtn.setFocusPainted(false);
        forgotpasswordBtn.setMargin(new java.awt.Insets(2, 14, 3, 5));
        forgotpasswordBtn.setOpaque(false);
        forgotpasswordBtn.addActionListener(e -> new ForgotPassword().requestForgotPassword());

        loginBtn.setBackground(new java.awt.Color(153, 255, 255));
        loginBtn.setFont(new java.awt.Font("Segoe UI", 1, 14));
        loginBtn.setForeground(new java.awt.Color(0, 0, 0));
        loginBtn.setText("Login");
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener((ActionEvent e) -> {
            String role = authenticator.login(
                emailField.getText(),
                new String(passwordField.getPassword()),
                new Component[]{emailField, passwordField, branchField, loginBtn}
            );

            switch (role) {
                case "employee" -> app.showEmployee();
                case "owner" -> app.showOwner();
                default -> {
                }
            }
        });

        // Sign Up Button
        signupBtn.setText("Don't have an account? Signup here!");
        signupBtn.setFocusPainted(false);
        signupBtn.addActionListener(e -> app.showRegister());

        // Label "Or"
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Or");

        // Layout
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(emailField)
                .addComponent(passwordField)
                .addComponent(branchField)
                .addComponent(loginBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(forgotpasswordBtn))
                .addComponent(signupBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(22, 22, 22)
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(branchField, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(forgotpasswordBtn)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(loginBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(signupBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }


        private JSONArray parseJsonArrayResponse(HttpURLConnection conn) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder responseBuilder = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }

            return new JSONArray(responseBuilder.toString());

        } catch (Exception e) {
            return null;
        }
    }
}
