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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import src.main.java.auth.session.java.FetchUser;
import src.server.java.ServerURL;
import src.utils.CustomBorder;

public class GenerateReports extends ServerURL implements CustomBorder {
    private String lastTime = "0000-00-00 00:00:00";

    JSONObject user = FetchUser.getUserData();

    public void loadReportsRequests(DefaultTableModel model) {

        String params = "action=get&client_fetch_token_request=;;mslaundryshop2025;;"
                    + "&since=" + URLEncoder.encode(lastTime, StandardCharsets.UTF_8);

        HttpURLConnection conn = createPostConnection("get_reports.php", params);
        JSONArray arr = parseJsonArrayResponse(conn);

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String time         = obj.getString("time");
            String transaction  = obj.getString("transaction_id");
            String customer     = obj.getString("customer_name");
            String serviceType     = obj.getString("servicetype");
            String amount       = obj.getString("total_amount");
            String branch       = obj.getString("branch");
            String status       = obj.getString("status");


            if (!branch.equals(user.getString("branch")) && !user.getString("branch").equals("all")) {
                continue;
            }

                
                boolean found = false;

                for (int j = 0; j < model.getRowCount(); j++) {
                    String existingTransaction = (String) model.getValueAt(j, 0);
                    if (existingTransaction.equals(transaction)) {
                        model.setValueAt(transaction, j, 0);
                        model.setValueAt(time, j, 1);
                        model.setValueAt(customer, j, 2);
                        model.setValueAt(serviceType, j, 3);
                        model.setValueAt(amount, j, 4);
                        model.setValueAt(branch, j, 5);
                        model.setValueAt(status, j, 6);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    model.addRow(new Object[]{ transaction, time, customer, serviceType, amount, branch, status });
                }


            if (time.compareTo(lastTime) > 0) {
                lastTime = time;
            }
        }
    }


        public void newReport(DefaultTableModel tableModel) {

            JDialog dialog = new JDialog((Frame) null, "Add new Report", true);
            dialog.setSize(450, 380);
            dialog.setResizable(false);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(null);

            JTextField customerNameField = new JTextField();
            JTextField serviceTypeField = new JTextField();
            JTextField amoutField = new JTextField();
            JComboBox jComboBox1 = new JComboBox();
            jComboBox1.setBackground(new java.awt.Color(255, 255, 255));
            jComboBox1.setFont(new java.awt.Font("Segoe UI", 1, 12));
            jComboBox1.setForeground(new java.awt.Color(51, 51, 51));
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pending", "Canceled", "Pickup", "Delivered", "Paid" }));

            setInputBorderColor(customerNameField, "Customer Name", Color.BLACK);
            setInputBorderColor(serviceTypeField, "Servive Type", Color.BLACK);
            setInputBorderColor(amoutField, "Total Amount", Color.BLACK);

            JButton saveButton = new JButton("Save Changes");
            saveButton.setBackground(Color.CYAN);
            saveButton.setFocusPainted(false);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFocusPainted(false);

            JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(customerNameField);
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
            serviceTypeField.getDocument().addDocumentListener(inputListener);
            amoutField.getDocument().addDocumentListener(inputListener);

            saveButton.addActionListener(e -> {

                    String customer = customerNameField.getText().trim();
                    String serviceType = serviceTypeField.getText().trim();
                    String amountText = amoutField.getText().trim();
                    String status = jComboBox1.getSelectedItem().toString();

                    if (!customer.matches("^[A-Za-z ]+$")) {
                        JOptionPane.showMessageDialog(dialog, "Customer name must only contain letters and spaces.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (serviceType.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Servive Type field cannot be empty.", "Missing Input", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!amountText.matches("^\\d+(\\.\\d{1,2})?$")) {
                        JOptionPane.showMessageDialog(dialog, "Amount must be a valid number (e.g., 150 or 150.75).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    HttpURLConnection conn = createPostConnection(
                    "get_reports.php",
                    "action=add&client_fetch_token_request=;;mslaundryshop2025;;&customer=" + customer + "&servicetype=" + serviceType + "&amount=" + amountText + "&status=" + status + "&branch=" + user.getString("branch")  
                );

                JSONObject responseArray = parseJsonResponse(conn);
                if(responseArray.getBoolean("response") == true) {
                        loadReportsRequests(tableModel);
                        JOptionPane.showMessageDialog(null, "Report added successfully");
                        dialog.dispose();
                    }
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
                HttpURLConnection conn = createPostConnection(
                "get_reports.php",
                "action=update&client_fetch_token_request=;;mslaundryshop2025;;&newstatus=" + jComboBox2.getSelectedItem().toString() + "&ti=" + ti
            );
            JSONObject responseArray = parseJsonResponse(conn);
            if(responseArray.getBoolean("response") == true) {
                    loadReportsRequests(tableModel);
                    JOptionPane.showMessageDialog(null, "Report updated successfully");
                    dialog.dispose();
                }
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
        e.printStackTrace();
        return new JSONArray();
    }
}
}
