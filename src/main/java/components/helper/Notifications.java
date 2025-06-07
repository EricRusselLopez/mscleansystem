package src.main.java.components.helper;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import src.server.java.ServerURL;

public class Notifications extends ServerURL {
        private String lastTime = "1970-01-01 00:00:00";

    public void loadNotificationsRequests(DefaultTableModel model, JTable table) {
        try {
            String params = "action=get&client_fetch_token_request=;;mslaundryshop2025;;"
                    + "&since=" + URLEncoder.encode(lastTime, StandardCharsets.UTF_8);

            HttpURLConnection conn = createPostConnection("get_notifications.php", params);
            JSONArray arr = parseJsonArrayResponse(conn);

            List<Object[]> newRows = new ArrayList<>();
            boolean newDataAdded = false;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int id             = obj.getInt("id");
                String rawTime     = obj.getString("time");
                String formatted   = rawTime.length() >= 16 ? rawTime.substring(0, 16) : rawTime;
                String message     = obj.getString("message");

                boolean alreadyExists = false;
                for (int j = 0; j < model.getRowCount(); j++) {
                    if ((int) model.getValueAt(j, 0) == id) {
                        alreadyExists = true;
                        break;
                    }
                }

                if (!alreadyExists) {
                    newRows.add(new Object[]{ id, formatted, message });
                    newDataAdded = true;
                }

                if (rawTime.compareTo(lastTime) > 0) {
                    lastTime = rawTime;
                }
            }

            for (Object[] row : newRows) {
                model.insertRow(0, row);
            }

            if (newDataAdded && model.getRowCount() > 0) {
                Toolkit.getDefaultToolkit().beep();
                table.scrollRectToVisible(table.getCellRect(0, 0, true));
            }
        } catch (Exception e) {}
    }
    
    public void deleteNotification(JTable table, DefaultTableModel model) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(null,
                "No notification selected.",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this item?", "Remove Item", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            String id  = model.getValueAt(row, 0).toString();
                        try {
                String params = "action=delete"
                    + "&client_fetch_token_request=;;mslaundryshop2025;;"
                    + "&id=" + URLEncoder.encode(id, "UTF-8");

                HttpURLConnection conn = createPostConnection("get_notifications.php", params);
                JSONObject resp = parseJsonResponse(conn);

                if (resp.getBoolean("response")) {
                    model.removeRow(row);
                    loadNotificationsRequests(model, table);
                    JOptionPane.showMessageDialog(null,
                            "Notification removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(null,
                        resp.optString("message", "Remove failed."),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "An error occurred while deleteting.",
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

