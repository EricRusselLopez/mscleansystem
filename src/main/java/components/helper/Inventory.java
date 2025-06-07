package src.main.java.components.helper;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import src.main.java.auth.session.java.FetchUser;
import src.server.java.ServerURL;
import src.utils.CustomBorder;

public class Inventory extends ServerURL implements CustomBorder {
    private String lastTime = "0000-00-00 00:00:00";
    JSONObject user = FetchUser.getUserData();


private final Set<String> outOfStockNotified = new HashSet<>();
    
    public void loadInventoryRequests(DefaultTableModel model) {
        String params = "action=get&client_fetch_token_request=;;mslaundryshop2025;;"
                    + "&since=0000-00-00 00:00:00";
        HttpURLConnection conn = createPostConnection("get_inventory.php", params);
        JSONArray arr = parseJsonArrayResponse(conn);

        Set<String> incomingKeys    = new HashSet<>();
        Set<String> currentOutOfStock = new HashSet<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String branch = obj.getString("branch");
            String status = obj.getString("status").trim().toLowerCase();
            String item   = obj.getString("item_name");

            // skip irrelevant branches
            if (!branch.equals(user.getString("branch")) 
             && !"all".equals(user.getString("branch"))) {
                continue;
            }

            String key = generateKey(item, branch);
            incomingKeys.add(key);

            // if this one is out of stock, note it
            if ("out of stock".equals(status)) {
                currentOutOfStock.add(key);
                // only beep the very first time we see *this* key go out of stock
                if (!outOfStockNotified.contains(key)) {
                    Toolkit.getDefaultToolkit().beep();
                    outOfStockNotified.add(key);
                }
            }

            // … now update or add the row as before …
            boolean found = false;
            for (int j = 0; j < model.getRowCount(); j++) {
                String existingKey = generateKey(
                    model.getValueAt(j, 0).toString(),
                    model.getValueAt(j, 3).toString()
                );
                if (existingKey.equals(key)) {
                    model.setValueAt(obj.getInt("quantity"),  j, 1);
                    model.setValueAt(obj.getInt("threshold"), j, 2);
                    model.setValueAt(status,                   j, 4);
                    model.setValueAt(obj.getString("last_restock"), j, 5);
                    found = true;
                    break;
                }
            }
            if (!found) {
                model.addRow(new Object[]{
                    item,
                    obj.getInt("quantity"),
                    obj.getInt("threshold"),
                    branch,
                    status,
                    obj.getString("last_restock")
                });
            }

            // update lastTime as you had it …
            String time = obj.getString("time");
            if (time.compareTo(lastTime) > 0) {
                lastTime = time;
            }
        }

        // remove rows no longer present
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            String existingKey = generateKey(
                model.getValueAt(i, 0).toString(),
                model.getValueAt(i, 3).toString()
            );
            if (!incomingKeys.contains(existingKey)) {
                model.removeRow(i);
            }
        }

        // *finally* drop notifications for items that have since been restocked
        outOfStockNotified.retainAll(currentOutOfStock);
    }

    private String generateKey(String item, String branch) {
        return item.trim().toLowerCase() + "::" + branch.trim().toLowerCase();
    }

    public void newInventory(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) null, "Add New Inventory", true);
        dialog.setSize(450, 430);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(null);

        JTextField itemNameField = new JTextField();
        setInputBorderColor(itemNameField, "Item Name", Color.BLACK);

        JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
        thresholdSpinner.setOpaque(false);
        thresholdSpinner.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        JComponent thresholdEditor = thresholdSpinner.getEditor();
        if (thresholdEditor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) thresholdEditor).getTextField();
            textField.setOpaque(false);
            textField.setBackground(new Color(0, 0, 0, 0)); // Transparent text field
            textField.setBorder(null); // Optional: remove border if needed
        }
        setInputBorderColor(thresholdSpinner, "Threshold", Color.BLACK);

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 1, 1));
        quantitySpinner.setOpaque(false);
        quantitySpinner.setBackground(new Color(0, 0, 0, 0));
        JComponent quantityEditor = quantitySpinner.getEditor();
        if (quantityEditor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) quantityEditor).getTextField();
            textField.setOpaque(false);
            textField.setBackground(new Color(0, 0, 0, 0));
            textField.setBorder(null);
        }
        setInputBorderColor(quantitySpinner, "Quantity", Color.BLACK);


        SpinnerNumberModel quantityModel = (SpinnerNumberModel) quantitySpinner.getModel();

        thresholdSpinner.addChangeListener(e -> {
            int newThreshold = (int) thresholdSpinner.getValue();
            int currentQty = (int) quantitySpinner.getValue();

            quantityModel.setMaximum(newThreshold);

            if (currentQty > newThreshold) {
                quantitySpinner.setValue(newThreshold);
            }
        });



        JComboBox<String> branchComboBox = new JComboBox<>();
        String postDataBranch = "action=get&client_fetch_token_request=;;mslaundryshop2025;;";
                HttpURLConnection connBranch = new ServerURL().createPostConnection("get_branches.php", postDataBranch);
                JSONArray responseBranch = parseJsonArrayResponse(connBranch);
                if (responseBranch != null) {
                            String[] comboItems = new String[responseBranch.length() + 1];
                            comboItems[0] = "Select branch";

                            for (int i = 0; i < responseBranch.length(); i++) {
                                JSONObject obj = responseBranch.getJSONObject(i);
                                comboItems[i + 1] = obj.getString("branchid") + " (" + obj.getString("name") + ")";
                            }

                branchComboBox.setModel(new DefaultComboBoxModel<>(comboItems));
            }

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(Color.CYAN);
        saveButton.setFocusPainted(false);
        saveButton.setEnabled(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(itemNameField);
        panel.add(branchComboBox);
        panel.add(quantitySpinner);
        panel.add(thresholdSpinner);
        panel.add(saveButton);
        panel.add(cancelButton);
        dialog.add(panel);

        itemNameField.getDocument().addDocumentListener(new DocumentListener() {
            void check() {
                saveButton.setEnabled(!itemNameField.getText().trim().isEmpty());
            }
            public void insertUpdate(DocumentEvent e) { check(); }
            public void removeUpdate(DocumentEvent e) { check(); }
            public void changedUpdate(DocumentEvent e) { check(); }
        });

        saveButton.addActionListener(e -> {
            String itemName = itemNameField.getText().trim();
            int quantity = (Integer) quantitySpinner.getValue();
            int threshold = (Integer) thresholdSpinner.getValue();
            String branch = branchComboBox.getSelectedItem().toString();

            if (!itemName.matches("^[A-Za-z ]+$")) {
                JOptionPane.showMessageDialog(dialog, "Item name must only contain letters and spaces.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (branch.equals("Select branch")) {
                JOptionPane.showMessageDialog(dialog, "Please select a branch.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if(quantity <= 0 || threshold <= 0) {
                JOptionPane.showMessageDialog(dialog, "Please enter an Quantity and Threshold", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }


            try {
                HttpURLConnection conn = createPostConnection(
                    "get_inventory.php",
                    "action=add" +
                    "&client_fetch_token_request=;;mslaundryshop2025;;" +
                    "&item_name=" + URLEncoder.encode(itemName, StandardCharsets.UTF_8) +
                    "&quantity=" + quantity +
                    "&threshold=" + threshold +
                    "&branch=" + URLEncoder.encode(branch, StandardCharsets.UTF_8)
                );

                JSONObject response = parseJsonResponse(conn);
                if (response.getBoolean("response")) {
                    loadInventoryRequests(tableModel);
                    JOptionPane.showMessageDialog(null, "Item added successfully.");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(
                        null, 
                        "An item with the same name and branch already exists.\nPlease avoid adding duplicates to prevent confusion.", 
                        "Duplicate Item Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "An error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }




    public void updateInventory(JTable table, DefaultTableModel model) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(null,
                "Please select an item to update.",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
            String itemName  = model.getValueAt(row, 0).toString();
            int currQty      = Integer.parseInt(model.getValueAt(row, 1).toString());
            int currThr      = Integer.parseInt(model.getValueAt(row, 2).toString());
            String branch    = model.getValueAt(row, 3).toString();

            SpinnerNumberModel thrModel = new SpinnerNumberModel(currThr, 0, 9999, 1);
            SpinnerNumberModel qtyModel = new SpinnerNumberModel(currQty, 0, currThr, 1);

            JSpinner thrSpinner = new JSpinner(thrModel);
            JSpinner qtySpinner = new JSpinner(qtyModel);

            thrSpinner.addChangeListener(e -> {
                int newThreshold = (int) thrSpinner.getValue();
                int currentQty = (int) qtySpinner.getValue();

                qtyModel.setMaximum(newThreshold);

                if (currentQty > newThreshold) {
                    qtySpinner.setValue(newThreshold);
                }
            });

        Object[] inputs = {
            "Item: "      + itemName,
            "Branch: "    + branch,
            "Quantity:",  qtySpinner,
            "Threshold:", thrSpinner
        };

        int choice = JOptionPane.showConfirmDialog(null, inputs,
            "Restock / Update Inventory", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        int newQty = (Integer) qtySpinner.getValue();
        int newThr = (Integer) thrSpinner.getValue();

        try {
            String params = "action=update"
                + "&client_fetch_token_request=;;mslaundryshop2025;;"
                + "&item_name="  + URLEncoder.encode(itemName, "UTF-8")
                + "&quantity="   + newQty
                + "&threshold="  + newThr
                + "&branch="     + URLEncoder.encode(branch,   "UTF-8");

            HttpURLConnection conn = createPostConnection("get_inventory.php", params);
            JSONObject resp = parseJsonResponse(conn);

            if (resp.getBoolean("response")) {
                loadInventoryRequests(model);
                JOptionPane.showMessageDialog(null,
                    "Inventory updated successfully.");
                    table.clearSelection();
            } else {
                JOptionPane.showMessageDialog(null,
                    resp.optString("message", "Update failed."),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "An error occurred while updating.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void useInventory(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null,
                "Please select an item to use.",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemName = model.getValueAt(row, 0).toString();
        int    currQty  = Integer.parseInt(model.getValueAt(row, 1).toString());
        String branch   = model.getValueAt(row, 3).toString();

        if (currQty == 0) {
            JOptionPane.showMessageDialog(null,
                "This item is out of stock.",
                "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JSpinner useSpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, currQty, 1)
        );
        Object[] inputs = {
            "Using Item: "     + itemName,
            "Branch: "         + branch,
            "Quantity to Use:", useSpinner
        };

        int choice = JOptionPane.showConfirmDialog(null, inputs,
            "Use Inventory Item", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        int useQty = (Integer) useSpinner.getValue();
        if (useQty <= 0 || useQty > currQty) {
            JOptionPane.showMessageDialog(null,
                "Invalid usage quantity.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int newQty = currQty - useQty;

        try {
            String params = "action=use"
                + "&client_fetch_token_request=;;mslaundryshop2025;;"
                + "&item_name=" + URLEncoder.encode(itemName, "UTF-8")
                + "&quantity="  + newQty
                + "&branch="    + URLEncoder.encode(branch,   "UTF-8");

            HttpURLConnection conn = createPostConnection("get_inventory.php", params);
            JSONObject resp = parseJsonResponse(conn);

            if (resp.getBoolean("response")) {
                loadInventoryRequests(model);
                    JOptionPane.showMessageDialog(null,
                        "Item used successfully.");
                        table.clearSelection();
            } else {
                JOptionPane.showMessageDialog(null,
                    resp.optString("message", "Use failed."),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "An error occurred while using item.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteInventory(JTable table, DefaultTableModel model) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(null,
                "Please select an item to remove.",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this item?", "Remove Item", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            String itemName  = model.getValueAt(row, 0).toString();
            String branch    = model.getValueAt(row, 3).toString();


            try {
                String params = "action=remove"
                    + "&client_fetch_token_request=;;mslaundryshop2025;;"
                    + "&item_name=" + URLEncoder.encode(itemName, "UTF-8")
                    + "&branch="    + URLEncoder.encode(branch,   "UTF-8");

                HttpURLConnection conn = createPostConnection("get_inventory.php", params);
                JSONObject resp = parseJsonResponse(conn);

                if (resp.getBoolean("response")) {
                    model.removeRow(row);
                    loadInventoryRequests(model);
                    JOptionPane.showMessageDialog(null,
                            "Item removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(null,
                        resp.optString("message", "Use failed."),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "An error occurred while using item.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
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
