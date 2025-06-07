package src.main.java.components.helper;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.midi.Track;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import src.main.java.App;
import src.main.java.auth.session.java.FetchUser;
import src.server.java.ServerURL;
import src.utils.CustomBorder;

public class GenerateReports extends ServerURL implements CustomBorder {
    private String lastTime = "0000-00-00 00:00:00";
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    JSONObject user = FetchUser.getUserData();
    private App app;

    public GenerateReports() {}
    public GenerateReports(App app) {
        this.app = app;
    }

public void loadReportsRequests(DefaultTableModel model, boolean limitToLatest8) {
    try {
        String params = "action=get&client_fetch_token_request=;;mslaundryshop2025;;"
                + "&since=" + URLEncoder.encode(lastTime, StandardCharsets.UTF_8);

        HttpURLConnection conn = createPostConnection("get_reports.php", params);
        JSONArray arr = parseJsonArrayResponse(conn);

        List<Object[]> allRows = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String rawTime      = obj.getString("time");
            String formattedTime = rawTime.length() >= 16 ? rawTime.substring(0, 16) : rawTime;
            String transaction  = obj.getString("transaction_id");
            String customer     = obj.getString("customer_name");
            String email        = obj.getString("contact");
            String serviceType  = obj.getString("servicetype");
            String amount       = obj.getString("total_amount");
            String branch       = obj.getString("branch");
            String status       = obj.getString("status");

            if (!branch.equals(user.getString("branch")) && !user.getString("branch").equals("all")) {
                continue;
            }

            allRows.add(new Object[]{ transaction, formattedTime, email, customer, serviceType, amount, branch, status });

            if (rawTime.compareTo(lastTime) > 0) {
                lastTime = rawTime;
            }
        }

        allRows.sort((a, b) -> ((String) b[1]).compareTo((String) a[1]));

        if (limitToLatest8 && allRows.size() > 8) {
            allRows = allRows.subList(0, 8);
        }

        if (model.getRowCount() == 0) {
            for (Object[] row : allRows) {
                model.addRow(row);
            }
        } else {
            for (Object[] row : allRows) {
                boolean found = false;
                for (int j = 0; j < model.getRowCount(); j++) {
                    if (model.getValueAt(j, 0).equals(row[0])) {
                        for (int k = 0; k < row.length; k++) {
                            model.setValueAt(row[k], j, k);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    model.insertRow(0, row);
                }
            }
            if (limitToLatest8) {
                while (model.getRowCount() > 8) {
                    model.removeRow(model.getRowCount() - 1);
                }
            }
        }

    } catch (Exception e) {}
}



public void newReport(DefaultTableModel tableModel, JTable table) {

    JDialog dialog = new JDialog((Frame) null, "Add new Report", true);
    dialog.setSize(490, 480);
    dialog.setResizable(false);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(null);

    JTextField customerNameField = new JTextField();
    JTextField contactField = new JTextField();
    JTextField serviceTypeField = new JTextField();
    JTextField amoutField = new JTextField();
    JComboBox jComboBox1 = new JComboBox();
    jComboBox1.setBackground(new java.awt.Color(255, 255, 255));
    jComboBox1.setFont(new java.awt.Font("Segoe UI", 1, 12));
    jComboBox1.setForeground(new java.awt.Color(51, 51, 51));
    jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pending", "Canceled", "Pickup", "Delivered", "Paid" }));

    setInputBorderColor(customerNameField, "Customer Name", Color.BLACK);
    setInputBorderColor(contactField, "Customer Email or (Phone Number)", Color.BLACK);
    setInputBorderColor(serviceTypeField, "Servive Type", Color.BLACK);
    setInputBorderColor(amoutField, "Total Amount", Color.BLACK);

    JButton saveButton = new JButton("Save Changes");
    saveButton.setBackground(Color.CYAN);
    saveButton.setFocusPainted(false);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.setFocusPainted(false);

    JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(customerNameField);
    panel.add(contactField);
    panel.add(serviceTypeField);
    panel.add(amoutField);
    panel.add(jComboBox1);
    panel.add(saveButton);
    panel.add(cancelButton);
    dialog.add(panel);

    saveButton.setEnabled(false);

    DocumentListener inputListener = new DocumentListener() {
        void checkChanges() {
            boolean changed = !customerNameField.getText().equals("")
                    && !contactField.getText().equals("")
                    && !serviceTypeField.getText().equals("")
                    && !amoutField.getText().equals("");
            saveButton.setEnabled(changed);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkChanges();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkChanges();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkChanges();
        }
    };

    customerNameField.getDocument().addDocumentListener(inputListener);
    contactField.getDocument().addDocumentListener(inputListener);
    serviceTypeField.getDocument().addDocumentListener(inputListener);
    amoutField.getDocument().addDocumentListener(inputListener);

    saveButton.addActionListener(e -> {

        String customer = customerNameField.getText().trim();
        String contact = contactField.getText().trim();
        Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(contact);
        String serviceType = serviceTypeField.getText().trim();
        String amountText = amoutField.getText().trim();
        String status = jComboBox1.getSelectedItem().toString();

        if (!customer.matches("^[A-Za-z ]+$")) {
            JOptionPane.showMessageDialog(dialog, "Customer name must only contain letters and spaces.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isEmail = emailMatcher.matches();
        boolean isPhone = contact.matches("^\\d{7,15}$");
        boolean isManual = contact.equalsIgnoreCase("n/a") || contact.equalsIgnoreCase("no contact");

        if (!isEmail && !isPhone && !isManual) {
            setInputBorderColor(contactField, "Customer Email or (Phone Number)", Color.RED);
            JOptionPane.showMessageDialog(dialog, "Please enter a valid email, phone number (digits only), or type 'N/A' if not available.", "Invalid Contact Info", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isManual) {
            JOptionPane.showMessageDialog(dialog, "Customer has no email or phone number. A physical claim slip will be required.", "Manual Notification Required", JOptionPane.INFORMATION_MESSAGE);
        }

        if (serviceType.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Servive Type field cannot be empty.", "Missing Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!amountText.matches("^\\d+(\\.\\d{1,2})?$")) {
            JOptionPane.showMessageDialog(dialog, "Amount must be a valid number (e.g., 150 or 150.75).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (status.equals("Pickup")) {
            int choice = JOptionPane.showConfirmDialog(
                dialog,
                "By setting the status to “Ready for Pickup”, the customer will be notified that their order is available for collection.\n" +
                "They’ll expect to come in and pick up their laundry—are you sure you want to proceed?",
                "Confirm Pickup Status",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (status.equals("Paid")) {
        int choice = JOptionPane.showConfirmDialog(
            dialog,
            "Marking this order as 'Paid' means the transaction is complete and can no longer be modified.\n" +
            "Are you absolutely sure you want to mark this as Paid?",
            "Confirm Payment Completion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
    }


        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> app.overlayLoading(true));
                dialog.setVisible(false);
                Thread.sleep(2000);

                HttpURLConnection conn = createPostConnection(
                    "get_reports.php",
                    "action=add&client_fetch_token_request=;;mslaundryshop2025;;" +
                    "&customer=" + URLEncoder.encode(customer, StandardCharsets.UTF_8) +
                    "&contact=" + URLEncoder.encode(contact, StandardCharsets.UTF_8) +
                    "&servicetype=" + URLEncoder.encode(serviceType, StandardCharsets.UTF_8) +
                    "&amount=" + URLEncoder.encode(amountText, StandardCharsets.UTF_8) +
                    "&status=" + URLEncoder.encode(status, StandardCharsets.UTF_8) +
                    "&branch=" + URLEncoder.encode(user.getString("branch"), StandardCharsets.UTF_8)
                );

                JSONObject responseArray = parseJsonResponse(conn);

                if (responseArray.getBoolean("response")) {
                    String transactionId = responseArray.getString("ti");

                    if (isPhone && status.equalsIgnoreCase("Pickup")) {
                        String manualSms = "Hello " + customer + ", your laundry is ready for pickup.\n" +
                                "Transaction ID: " + transactionId + "\n" +
                                "Service: " + serviceType + "\n" +
                                "Amount: ₱" + amountText + "\n" +
                                "From branch: " + user.getString("branch");

                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(dialog,
                                "Please manually SMS the customer:\n\n" + manualSms + "\n\nContact: " + contact,
                                "Manual SMS Required",
                                JOptionPane.INFORMATION_MESSAGE);
                        });
                    }

                    SwingUtilities.invokeLater(() -> {
                        loadReportsRequests(tableModel, false);
                        JOptionPane.showMessageDialog(null, "Report added successfully");
                        dialog.dispose();
                        table.clearSelection();
                    });
                }

                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> app.overlayLoading(false));
            }
        }.execute();

    });

    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
}



    public void updateReport(JTable userTable, DefaultTableModel tableModel) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "No report selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);

        String ti = (String) tableModel.getValueAt(modelRow, 0);
        String customerName = (String) tableModel.getValueAt(modelRow, 2);
        String contact = (String) tableModel.getValueAt(modelRow, 3);
        String service = (String) tableModel.getValueAt(modelRow, 4);
        String amount = (String) tableModel.getValueAt(modelRow, 5);
        String branch = (String) tableModel.getValueAt(modelRow, 6);
        String oldStatus = (String) tableModel.getValueAt(modelRow, 7);


        JDialog dialog = new JDialog((Frame) null, "Update report", true);
        dialog.setSize(450, 380);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(null);

            JComboBox jComboBox2 = new JComboBox();
            jComboBox2.setBackground(new java.awt.Color(255, 255, 255));
            jComboBox2.setFont(new java.awt.Font("Segoe UI", 1, 12));
            jComboBox2.setForeground(new java.awt.Color(51, 51, 51));
            jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {"Pending", "Canceled", "Pickup", "Delivered", "Paid" }));

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(Color.CYAN);
        saveButton.setFocusPainted(false);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(jComboBox2);
        panel.add(saveButton);
        panel.add(cancelButton);
        dialog.add(panel);


        saveButton.addActionListener(e -> {

            boolean isPhone = contact.matches("^\\d{7,15}$");

            String status = jComboBox2.getSelectedItem().toString();
            if (oldStatus.equals("Pickup") && status.equals("Pickup")) {
                    JOptionPane.showMessageDialog(
                        dialog,
                        "Customer has already been notified that their laundry is ready for pickup!",
                        "Already Notified",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }
                new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SwingUtilities.invokeLater(() -> app.overlayLoading(true));
                    dialog.setVisible(false);
                    Thread.sleep(2000);
                    if (status.equals("Pickup") && !oldStatus.equals("Pickup")) {
                        int choice = JOptionPane.showConfirmDialog(
                            dialog,
                            "By setting the status to “Ready for Pickup”, the customer will be notified that their order is available for collection.\n" +
                            "They’ll expect to come in and pick up their laundry—are you sure you want to proceed?",
                            "Confirm Pickup Status",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (choice != JOptionPane.YES_OPTION) return null;
                    }

                    if (status.equals("Paid")) {
                        int choice = JOptionPane.showConfirmDialog(
                            dialog,
                            "Marking this order as 'Paid' means the transaction is complete and can no longer be modified.\n" +
                            "Are you absolutely sure you want to mark this as Paid?",
                            "Confirm Payment Completion",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (choice != JOptionPane.YES_OPTION) return null;
                    }

                    HttpURLConnection conn = createPostConnection(
                        "get_reports.php",
                        "action=update&client_fetch_token_request=;;mslaundryshop2025;;" +
                        "&newstatus=" + URLEncoder.encode(status, StandardCharsets.UTF_8) +
                        "&ti=" + URLEncoder.encode(ti, StandardCharsets.UTF_8) +
                        "&contact=" + URLEncoder.encode(contact, StandardCharsets.UTF_8) +
                        "&amount=" + URLEncoder.encode(amount, StandardCharsets.UTF_8) +
                        "&branch=" + URLEncoder.encode(branch, StandardCharsets.UTF_8) +
                        "&customer=" + URLEncoder.encode(customerName, StandardCharsets.UTF_8)
                    );

                    JSONObject responseArray = parseJsonResponse(conn);

                    if (responseArray.getBoolean("response")) {
                        String transactionId = responseArray.getString("ti");

                        if (isPhone && status.equalsIgnoreCase("Pickup")) {
                            String manualSms = "Hello " + customerName + ", your laundry is ready for pickup.\n" +
                                    "Transaction ID: " + transactionId + "\n" +
                                    "Service: " + service + "\n" +
                                    "Amount: ₱" + amount + "\n" +
                                    "From branch: " + branch;

                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(dialog,
                                    "Please manually SMS the customer:\n\n" + manualSms + "\n\nContact: " + contact,
                                    "Manual SMS Required",
                                    JOptionPane.INFORMATION_MESSAGE);
                            });
                        }

                        SwingUtilities.invokeLater(() -> {
                            loadReportsRequests(tableModel, false);
                            JOptionPane.showMessageDialog(null, "Report updated successfully");
                            dialog.dispose();
                            userTable.clearSelection();
                        });
                    }

                    return null;
                }

                @Override
                protected void done() {
                    SwingUtilities.invokeLater(() -> app.overlayLoading(false));
                }
            }.execute();

            });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
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

        private JSONArray parseJsonArrayResponse(HttpURLConnection conn) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        StringBuilder responseBuilder = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
        }

        return new JSONArray(responseBuilder.toString());

    } catch (Exception e) {
        return new JSONArray();
    }
}
}
