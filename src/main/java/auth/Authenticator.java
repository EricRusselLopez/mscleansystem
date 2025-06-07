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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;

import src.main.java.App;
import src.main.java.auth.session.java.FetchUser;
import src.main.java.components.helper.Accounts;
import src.server.java.ServerURL;
import src.utils.CustomBorder;

public class Authenticator extends ServerURL implements CustomBorder {

    CookieManager cookieManager = new CookieManager();
    private App app;
    String roleReturn;

    public Authenticator() {}
    public Authenticator(App app) {
        this.app = app;
        CookieHandler.setDefault(cookieManager);
    }


    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    protected String[] loadedUserSession = readUserSessionFromFile("./src/main/java/auth/session/user.txt");

public void login(String email, String password, Component[] component, Consumer<String> callback) {
    Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
    JComponent emailField = (JComponent) component[0];
    JComponent passwordField = (JComponent) component[1];
    JComboBox<?> branchField = (JComboBox<?>) component[2];
    Component loginButton = component[3];

    disableComponent(loginButton);

    if (email.isEmpty()) {
        setInputBorderColor(emailField, "Email address", Color.RED);
        enableComponent(loginButton);
        callback.accept("");
        return;
    }

    if (!emailMatcher.matches()) {
        setInputBorderColor(emailField, "Email address", Color.RED);
        showError("Please enter your email address correctly.", "Invalid email");
        enableComponent(loginButton);
        callback.accept("");
        return;
    }

    if (password.isEmpty()) {
        setInputBorderColor(emailField, "Email address", Color.BLACK);
        setInputBorderColor(passwordField, "Password", Color.RED);
        enableComponent(loginButton);
        callback.accept("");
        return;
    }
    if (branchField.getSelectedItem().toString().equalsIgnoreCase("select branch")) {
        showError("Please select your branch.", "Invalid branch");
        enableComponent(loginButton);
        callback.accept("");
        return;
    }

    setInputBorderColor(emailField, "Email address", Color.BLACK);
    setInputBorderColor(passwordField, "Password", Color.BLACK);

    new SwingWorker<String, Void>() {
        @Override
        protected String doInBackground() throws Exception {
            SwingUtilities.invokeLater(() -> app.overlayLoading(true));
            Thread.sleep(500);

            try {
                String postDataLogin = "email=" + URLEncoder.encode(email, "UTF-8")
                        + "&password=" + URLEncoder.encode(password, "UTF-8")
                        + "&client_fetch_token_request=;;mslaundryshop2025;;&branch=" + branchField.getSelectedItem().toString();
                HttpURLConnection connLogin = createPostConnection("login.php", postDataLogin);
                JSONObject respLogin = parseJsonResponse(connLogin);

                if (respLogin == null || !respLogin.getBoolean("response")) {
                    return "LOGIN_FAILED:" + (respLogin != null && respLogin.has("message") ? respLogin.getString("message") : "Incorrect email or password.");
                }

                FetchUser.setUserData(respLogin);
                String role = respLogin.getString("role");
                String branch = respLogin.optString("branch", "");
                String clearPass = password;

                if ("owner".equals(role)) {
                    final String[] pinResult = new String[1];
                    final boolean[] pinCanceled = new boolean[1];

                    SwingUtilities.invokeAndWait(() -> {
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
                            pinCanceled[0] = true;
                        } else {
                            char[] pinChars = pinField.getPassword();
                            pinResult[0] = new String(pinChars);
                            Arrays.fill(pinChars, '0');
                        }
                    });

                    if (pinCanceled[0]) {
                        return "PIN_CANCELLED";
                    }

                    String postDataPin = "securityRequest=" + URLEncoder.encode(pinResult[0], "UTF-8")
                            + "&client_fetch_token_request=;;mslaundryshop2025;;";
                    HttpURLConnection connPin = createPostConnection("login.php", postDataPin);
                    JSONObject respPin = parseJsonResponse(connPin);

                    if (respPin == null || !respPin.getBoolean("response")) {
                        return "PIN_FAILED:" + (respPin != null && respPin.has("message") ? respPin.getString("message") : "Incorrect security PIN.");
                    }
                }

                saveUserSessionToFile(
                        respLogin.getString("email"),
                        clearPass,
                        role,
                        branch
                );

                return role;

            } catch (Exception e) {
                return "ERROR:Something went wrong. Try again later.";
            }
        }

        @Override
        protected void done() {
            SwingUtilities.invokeLater(() -> {
                try {
                    app.overlayLoading(false);

                    String result = get();

                    if (result.startsWith("LOGIN_FAILED:")) {
                        String msg = result.substring("LOGIN_FAILED:".length());
                        setInputBorderColor(passwordField, "Password", Color.RED);
                        showError(msg, "Login Failed");
                        ((javax.swing.text.JTextComponent) passwordField).setText("");
                        enableComponent(loginButton);
                        callback.accept("");
                        return;
                    }

                    if (result.equals("PIN_CANCELLED")) {
                        enableComponent(loginButton);
                        callback.accept("");
                        return;
                    }

                    if (result.startsWith("PIN_FAILED:")) {
                        String msg = result.substring("PIN_FAILED:".length());
                        showError(msg, "PIN Failed");
                        enableComponent(loginButton);
                        callback.accept("");
                        return;
                    }

                    if (result.equals("ERROR:Something went wrong. Try again later.")) {
                        showError("Something went wrong. Try again later.", "Error");
                        enableComponent(loginButton);
                        callback.accept("");
                        return;
                    }

                    enableComponent(loginButton);
                    callback.accept(result);

                } catch (Exception ex) {
                    showError("Unexpected error occurred.", "Error");
                    enableComponent(loginButton);
                    callback.accept("");
                }
            });
        }
    }.execute();
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

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> app.overlayLoading(true));
                Thread.sleep(500);

                String postData = "&action=send&client_fetch_token_request=;;mslaundryshop2025;;&fname=" + firstname
                        + "&lname=" + lastname
                        + "&email=" + email
                        + "&gender=" + gender
                        + "&branch=" + branch;
                HttpURLConnection conn = createPostConnection("email_verification.php", postData);
                JSONObject response = parseJsonResponse(conn);

                if (response == null) return "ERROR:Network error";

                if (!response.optBoolean("response", false)) {
                    if (response.optBoolean("exist", false)) {
                        return "EXIST:" + response.optString("error", "Email already exists.");
                    } else {
                        return "ERROR:" + response.optString("error", "Something went wrong. Try again later.");
                    }
                }

                while (true) {
                    final String[] codeInput = new String[1];

                    SwingUtilities.invokeAndWait(() -> {
                        codeInput[0] = JOptionPane.showInputDialog(null,
                                "Enter the verification code sent to your email:",
                                "Email Verification",
                                JOptionPane.QUESTION_MESSAGE);
                    });

                    if (codeInput[0] == null) {
                        return "CANCELLED";
                    }

                    String verifyData = "&action=verify&client_fetch_token_request=;;mslaundryshop2025;;&code=" + codeInput[0]
                            + "&fname=" + firstname
                            + "&lname=" + lastname
                            + "&email=" + email
                            + "&password=" + password
                            + "&role=employee"
                            + "&branch=" + branch
                            + "&gender=" + gender;

                    HttpURLConnection verifyConn = createPostConnection("email_verification.php", verifyData);
                    JSONObject verifyResponse = parseJsonResponse(verifyConn);

                    if (verifyResponse != null && verifyResponse.optBoolean("response", false)) {
                        return "SUCCESS";
                    } else {
                        SwingUtilities.invokeAndWait(() -> showError("Incorrect code. Try again.", "Error"));
                    }
                }
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    app.overlayLoading(false);

                    try {
                        String result = get();

                        if (result.equals("SUCCESS")) {
                            JOptionPane.showMessageDialog(null,
                                    "✅ Your account request has been submitted successfully!\n" +
                                            "Please wait for the owner’s approval. You’ll receive an email notification\n" +
                                            "as soon as your account is activated.",
                                    "Request Submitted",
                                    JOptionPane.INFORMATION_MESSAGE);
                            resetFormFields(fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, genderField);
                            enableInputs(new Component[]{fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn});
                        } else if (result.equals("CANCELLED")) {
                            showInfo("Verification canceled.");
                            resetFormFields(fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, genderField);
                            enableInputs(new Component[]{fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn});
                        } else if (result.startsWith("EXIST:")) {
                            setInputBorderColor(emailField, "Email address (Contact)", Color.RED);
                            showError(result.substring(6), "Invalid email");
                            enableInputs(new Component[]{fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn});
                        } else if (result.startsWith("ERROR:")) {
                            showError(result.substring(6), "Error");
                            enableInputs(new Component[]{fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn});
                        } else {
                            showError("Unexpected error occurred.", "Error");
                            enableInputs(new Component[]{fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn});
                        }
                    } catch (Exception ex) {
                        showError("Unexpected error occurred.", "Error");
                        enableInputs(new Component[]{fnameField, lnameField, emailField, passwordField, cPasswordField, branchField, loginBtn, requestBtn});
                    }
                });
            }
        }.execute();

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
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    SwingUtilities.invokeLater(() -> app.overlayLoading(true));

                    String verifyData = "&action=approved&client_fetch_token_request=;;mslaundryshop2025;;&email=" + email + "&branch=" + branch;
                    HttpURLConnection verifyConn = createPostConnection("get_approvals.php", verifyData);
                    JSONObject verifyResponse = parseJsonResponse(verifyConn);

                    return verifyResponse != null && verifyResponse.optBoolean("response");
                }

                @Override
                protected void done() {
                    SwingUtilities.invokeLater(() -> app.overlayLoading(false));
                    try {
                        boolean success = get();

                        if (success) {
                            JOptionPane.showMessageDialog(null,
                                "Approved successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                            model.setRowCount(0);
                            new Accounts().loadAccountsRequests(model);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                "Approval failed. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null,
                            "An unexpected error occurred.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
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
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    SwingUtilities.invokeLater(() -> app.overlayLoading(true));

                    String verifyData = "&action=reject&client_fetch_token_request=;;mslaundryshop2025;;&email=" + email + "&branch=" + branch;
                    HttpURLConnection verifyConn = createPostConnection("get_approvals.php", verifyData);
                    JSONObject verifyResponse = parseJsonResponse(verifyConn);

                    return verifyResponse != null && verifyResponse.optBoolean("response");
                }

                @Override
                protected void done() {
                    SwingUtilities.invokeLater(() -> app.overlayLoading(false));
                    try {
                        boolean success = get();

                        if (success) {
                            JOptionPane.showMessageDialog(null,
                                "User rejected successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                            model.setRowCount(0);
                            new Accounts().loadAccountsRequests(model);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                "Rejection failed. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null,
                            "An unexpected error occurred.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();

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
            new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> app.overlayLoading(true));

                String verifyData = "&action=remove&client_fetch_token_request=;;mslaundryshop2025;;&email=" + email + "&branch=" + branch;
                HttpURLConnection verifyConn = createPostConnection("get_approvals.php", verifyData);
                JSONObject verifyResponse = parseJsonResponse(verifyConn);

                return verifyResponse != null && verifyResponse.optBoolean("response");
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> app.overlayLoading(false));
                try {
                    boolean success = get();

                    if (success) {
                        JOptionPane.showMessageDialog(null,
                            "User removed successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                        model.setRowCount(0);
                        new Accounts().loadAccountsRequests(model);
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "Failed to remove user. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        "An unexpected error occurred.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();

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
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> app.overlayLoading(true));
                Thread.sleep(2000);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/main/java/auth/session/user.txt", false))) {
                    writer.write("");
                } catch (IOException e) {}
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    app.overlayLoading(false);
                    app.showLogin();
                });
            }
        }.execute();
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
