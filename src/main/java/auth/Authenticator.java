package src.main.java.auth;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;

import src.main.java.auth.session.java.FetchUser;
import src.main.java.components.helper.Accounts;
import src.server.java.ServerURL;
import src.utils.CustomBorder;

public class Authenticator extends ServerURL implements CustomBorder {

    CookieManager cookieManager = new CookieManager();

    public Authenticator() {
        CookieHandler.setDefault(cookieManager);
    }


    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    protected String[] loadedUserSession = readUserSessionFromFile("./src/main/java/auth/session/user.txt");

    public String login(String email, String password, Component[] component) {
        Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        JComponent emailField = (JComponent) component[0];
        JComponent passwordField = (JComponent) component[1];
        JComboBox<?> branchField = (JComboBox<?>) component[2];
        Component loginButton = component[3];

        disableComponent(loginButton);

        if (email.isEmpty()) {
            setInputBorderColor(emailField, "Email address", Color.RED);
            enableComponent(loginButton);
            return "";
        }

        if (!emailMatcher.matches()) {
            setInputBorderColor(emailField, "Email address", Color.RED);
            showError("Please enter your email address correctly.", "Invalid email");
            enableComponent(loginButton);
            return "";
        }

        if (password.isEmpty()) {
            setInputBorderColor(emailField, "Email address", Color.BLACK);
            setInputBorderColor(passwordField, "Password", Color.RED);
            enableComponent(loginButton);
            return "";
        }
        if (branchField.getSelectedItem().toString().equalsIgnoreCase("select branch")) {
            showError("Please select your branch.", "Invalid branch");
            enableComponent(loginButton);
            return "";
        }

        setInputBorderColor(emailField, "Email address", Color.BLACK);
        setInputBorderColor(passwordField, "Password", Color.BLACK);

        try {
            String postDataLogin = "email=" + URLEncoder.encode(email, "UTF-8")
                                + "&password=" + URLEncoder.encode(password, "UTF-8")
                                + "&client_fetch_token_request=;;mslaundryshop2025;;&branch=" + branchField.getSelectedItem().toString();
            HttpURLConnection connLogin = createPostConnection("login.php", postDataLogin);
            JSONObject respLogin = parseJsonResponse(connLogin);

            if (respLogin == null || !respLogin.getBoolean("response")) {
                String msg = respLogin != null && respLogin.has("message")
                        ? respLogin.getString("message")
                        : "Incorrect email or password.";
                setInputBorderColor(passwordField, "Password", Color.RED);
                showError(msg, "Login Failed");
                ((javax.swing.text.JTextComponent) passwordField).setText("");
                enableComponent(loginButton);
                return "";
            }

            // Good login: stash user data
            FetchUser.setUserData(respLogin);
            String role   = respLogin.getString("role");
            String branch = respLogin.optString("branch", "");
            String clearPass = password;

            // 2) If owner, do PIN check
            if ("owner".equals(role)) {
                JPasswordField pinField = new JPasswordField();
                pinField.setEchoChar('•');
                int option = JOptionPane.showConfirmDialog(
                    null,
                    pinField,
                    "Please enter your security PIN:",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );

                if (option != JOptionPane.OK_OPTION) {
                    enableComponent(loginButton);
                    return "";
                }

                char[] pinChars = pinField.getPassword();
                String pin = new String(pinChars);
                Arrays.fill(pinChars, '0');

                String postDataPin = "securityRequest=" + URLEncoder.encode(pin, "UTF-8")
                                + "&client_fetch_token_request=;;mslaundryshop2025;;";
                HttpURLConnection connPin = createPostConnection("login.php", postDataPin);
                JSONObject respPin = parseJsonResponse(connPin);
                if (respPin == null || !respPin.getBoolean("response")) {
                    String msg = respPin != null && respPin.has("message")
                            ? respPin.getString("message")
                            : "Incorrect security PIN.";
                    showError(msg, "PIN Failed");
                    enableComponent(loginButton);
                    return "";
                }
            }

            saveUserSessionToFile(
                respLogin.getString("email"),
                clearPass,
                role,
                branch
            );
            enableComponent(loginButton);
            return role;

        } catch (Exception e) {
            showError("Something went wrong. Try again later.", "Error");
            enableComponent(loginButton);
            return "";
        }
    }



    public void requestApproval(String firstname, String lastname, String email, String password, String cPassword, String branch, String gender, Component[] component) {
        Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        Pattern namePattern = Pattern.compile("[^a-zA-Z ]");
        JComponent fnameField = (JComponent) component[0];
        JComponent lnameField = (JComponent) component[1];
        JComponent emailField = (JComponent) component[2];
        JComponent passwordField = (JComponent) component[3];
        JComponent cPasswordField = (JComponent) component[4];
        JComponent branchField = (JComponent) component[5];
        JComponent genderField = (JComponent) component[6];
        Component requestBtn = component[7];
        Component loginBtn = component[8];
        disableComponent(requestBtn);

        setInputBorderColor(fnameField, "Firstname", Color.BLACK);
        setInputBorderColor(lnameField, "Lastname", Color.BLACK);
        setInputBorderColor(emailField, "Email address (Contact)", Color.BLACK);
        setInputBorderColor(passwordField, "Password", Color.BLACK);
        setInputBorderColor(cPasswordField, "Confirm password", Color.BLACK);
        setInputBorderColor(branchField, "Branch", Color.BLACK);
        setInputBorderColor(genderField, "Gender", Color.BLACK);


        if (firstname.isEmpty() || firstname.length() < 3) {
            setInputBorderColor(fnameField, "Firstname", Color.RED);
            enableComponent(requestBtn);
            return;
        }
        if (lastname.isEmpty() || lastname.length() < 3) {
            setInputBorderColor(lnameField, "Lastname", Color.RED);
            enableComponent(requestBtn);
            return;
        }
        if (namePattern.matcher(firstname).find() || namePattern.matcher(lastname).find()) {
            setInputBorderColor(fnameField, "Firstname", Color.RED);
            setInputBorderColor(lnameField, "Lastname", Color.RED);
            showError("Special characters and numbers are not allowed in username.", "Invalid name");
            enableComponent(requestBtn);
            return;
        }
        if (email.isEmpty()) {
            setInputBorderColor(emailField, "Email address (Contact)", Color.RED);
            showError("Please enter your email address.", "Invalid email");
            enableComponent(requestBtn);
            return;
        }
        if (!emailMatcher.matches()) {
            setInputBorderColor(emailField, "Email address (Contact)", Color.RED);
            showError("Please enter your email address correctly.", "Invalid email");
            enableComponent(requestBtn);
            return;
        }
        if (password.isEmpty()) {
            setInputBorderColor(passwordField, "Password", Color.RED);
            enableComponent(requestBtn);
            return;
        }
        if (password.length() <= 7) {
            setInputBorderColor(passwordField, "Password", Color.RED);
            showError("Your password must be at least 8 Characters long.", "Invalid password");
            enableComponent(requestBtn);
            return;
        }
        if (cPassword.isEmpty()) {
            setInputBorderColor(cPasswordField, "Confirm password", Color.RED);
            showError("Please confirm your password.", "Missing field");
            enableComponent(requestBtn);
            return;
        }
        if (!password.equals(cPassword)) {
            setInputBorderColor(cPasswordField, "Confirm password", Color.RED);
            showError("Passwords do not match. Try again.", "Mismatch");
            enableComponent(requestBtn);
            return;
        }
        if (branch.equals("Select branch")) {
            setInputBorderColor(branchField, "Select branch", Color.RED);
            showError("Please select your branch.", "Missing branch");
            enableComponent(requestBtn);
            return;
        }
        if (gender.equals("Select your gender")) {
            setInputBorderColor(genderField, "Select gender", Color.RED);
            showError("Please select your gender.", "Missing gender");
            enableComponent(requestBtn);
            return;
        }

        disableInputs(new Component[] {
            fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn
        });

        String postData = "&action=send&client_fetch_token_request=;;mslaundryshop2025;;&fname=" + firstname + "&lname=" + lastname + "&email=" + email + "&gender=" + gender + "&branch=" + branch;
        HttpURLConnection conn = createPostConnection("email_verification.php", postData);
        JSONObject response = parseJsonResponse(conn);

        if (response != null && response.optBoolean("response")) {
            while (true) {
                String code = JOptionPane.showInputDialog(null, "Enter the verification code sent to your email:", "Email Verification", JOptionPane.QUESTION_MESSAGE);

                if (code == null) {
                    showInfo("Verification canceled.");
                    resetFormFields(fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, genderField);
                    enableInputs(new Component[] {
                        fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn
                    });
                    return;
                }

                String verifyData = "&action=verify&client_fetch_token_request=;;mslaundryshop2025;;&code=" + code + "&fname=" + firstname + "&lname=" + lastname + "&email=" + email + "&password=" + password + "&role=employee&branch=" + branch + "&gender=" + gender;
                HttpURLConnection verifyConn = createPostConnection("email_verification.php", verifyData);
                JSONObject verifyResponse = parseJsonResponse(verifyConn);

                if (verifyResponse != null && verifyResponse.optBoolean("response")) {
                    JOptionPane.showMessageDialog(
                        null,
                        "✅ Your account request has been submitted successfully!\n" +
                        "Please wait for the owner’s approval. You’ll receive an email notification\n" +
                        "as soon as your account is activated.",
                        "Request Submitted",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    resetFormFields(fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, genderField);
                    enableInputs(new Component[] {
                        fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn
                    });
                    return;
                } else {
                    showError("Incorrect code. Try again.", "Error");
                }
            }
        } else if (response != null && response.optBoolean("exist")) {
            setInputBorderColor(emailField, "Email address (Contact)", Color.RED);
            showError(response.optString("error", "Email already exists."), "Invalid email");
        } else {
            showError("Something went wrong. Try again later.", "Error");
        }

        enableInputs(new Component[] {
            fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn
        });
    }


    public void approveUser(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "No user selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);

        String currentStatus = (String) model.getValueAt(modelRow, 5);
        String branch = (String) model.getValueAt(modelRow, 3);
        if ("approved".equals(currentStatus)) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                "You already approved this user.",
                "User is already approved",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = (String) model.getValueAt(modelRow, 2);
        int confirm = JOptionPane.showConfirmDialog(null,
            "Are you sure you want to approve this user?",
            "Approve user",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String verifyData = "&action=approved&client_fetch_token_request=;;mslaundryshop2025;;&email=" + email + "&branch=" + branch;
            HttpURLConnection verifyConn = createPostConnection("get_approvals.php", verifyData);
            JSONObject verifyResponse = parseJsonResponse(verifyConn);

            if (verifyResponse != null && verifyResponse.optBoolean("response")) {
                JOptionPane.showMessageDialog(null,
                    "Approved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                    model.setRowCount(0);
                    new Accounts().loadAccountsRequests(model);
            }
        }
    }


    public void rejectUser(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "No user selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);

        String currentStatus = (String) model.getValueAt(modelRow, 5);
        if ("approved".equals(currentStatus)) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                "You already approved this user.",
                "User is already approved",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fname = (String) model.getValueAt(modelRow, 0);
        String lname = (String) model.getValueAt(modelRow, 1);
        String email = (String) model.getValueAt(modelRow, 2);
        String branch = (String) model.getValueAt(modelRow, 3);
        int confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to reject this user?\n\nNote: The user will still be able to submit another request in the future.",
            "Confirm Rejection",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );


        if (confirm == JOptionPane.YES_OPTION) {
            String verifyData = "&action=reject&client_fetch_token_request=;;mslaundryshop2025;;&email=" + email + "&branch=" + branch;
            HttpURLConnection verifyConn = createPostConnection("get_approvals.php", verifyData);
            JSONObject verifyResponse = parseJsonResponse(verifyConn);

            if (verifyResponse != null && verifyResponse.optBoolean("response")) {
                JOptionPane.showMessageDialog(null,
                    "User rejected successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                    model.setRowCount(0);
                    new Accounts().loadAccountsRequests(model);
            }
        }
    }


       public void removeUser(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "No user selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);

        String currentStatus = (String) model.getValueAt(modelRow, 5);
        if ("requested".equals(currentStatus)) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
             "This user is still pending approval.\n" +
             "Please use the “Reject” button to deny this request.",
            "Cannot Remove Pending User",
            JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = (String) model.getValueAt(modelRow, 2);
        String branch = (String) model.getValueAt(modelRow, 3);
        int confirm = JOptionPane.showConfirmDialog(
            null,
            "This user has already been approved in branch: " + branch + ".\n" +
            "Removing them will delete their account permanently.\n\n" +
            "Are you sure you want to remove \"" + email + "\" from branch " + branch + "?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );


        if (confirm == JOptionPane.YES_OPTION) {
            String verifyData = "&action=remove&client_fetch_token_request=;;mslaundryshop2025;;&email=" + email + "&branch=" + branch;
            HttpURLConnection verifyConn = createPostConnection("get_approvals.php", verifyData);
            JSONObject verifyResponse = parseJsonResponse(verifyConn);

            if (verifyResponse != null && verifyResponse.optBoolean("response")) {
                JOptionPane.showMessageDialog(null,
                    "User removed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                    model.setRowCount(0);
                    new Accounts().loadAccountsRequests(model);
            }
        }
    }

 

    private void resetFormFields(JComponent fname, JComponent lname, JComponent email, JComponent pass, JComponent cpass, JComponent branch, JComponent gender) {
        ((JTextField) fname).setText("");
        ((JTextField) lname).setText("");
        ((JTextField) email).setText("");
        ((JPasswordField) pass).setText("");
        ((JPasswordField) cpass).setText("");
        ((JComboBox<?>) branch).setSelectedIndex(0);
        ((JComboBox<?>) gender).setSelectedIndex(0);
    }

    private void enableInputs(Component[] components) {
        for (Component c : components) enableComponent(c);
    }

    private void disableInputs(Component[] components) {
        for (Component c : components) disableComponent(c);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(null, message);
    }




    public final String isAuthenticated() {
        if (loadedUserSession != null && loadedUserSession.length >= 4) {
            try {
                String postData = "email=" + loadedUserSession[0] +
                                "&password=" + loadedUserSession[1] +
                                "&role=" + loadedUserSession[2] +
                                "&branch=" + loadedUserSession[3] +
                                "&client_fetch_token_request=;;mslaundryshop2025;;";

                HttpURLConnection conn = createPostConnection("session_login.php", postData);
                JSONObject response = parseJsonResponse(conn);

                if (response != null && response.has("response") && response.getBoolean("response") == true) {
                    FetchUser.setUserData(response);
                    return loadedUserSession[2];

                } else {
                    return null;
                }
            } catch (Exception e) {
    
                showError("Something went wrong. Try again later.", "Error");
            }
        }
        return null;
    }


    public final void logout() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/main/java/auth/session/user.txt", false))) {
            writer.write("");
        } catch (IOException e) {

        }
    }



    /* ------------------ Helpers ------------------ */

    private static void saveUserSessionToFile(String email, String password, String role, String branch) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/main/java/auth/session/user.txt", false))) {
            writer.write(email + "," + password + "," + role + "," + branch);
        } catch (IOException e) {

        }
    }

    private String[] readUserSessionFromFile(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            return (line != null) ? line.split(",") : null;
        } catch (IOException e) {
            return null;
        }
    }

    private JSONObject parseJsonResponse(HttpURLConnection conn) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        StringBuilder responseBuilder = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
        }

        return new JSONObject(responseBuilder.toString());

    } catch (Exception e) {
        return null;
    }
}


    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void enableComponent(Component c) {
        c.setEnabled(true);
    }

    private void disableComponent(Component c) {
        c.setEnabled(false);
    }
}
