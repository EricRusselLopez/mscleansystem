package src.main.java.components.helper;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import src.server.java.ServerURL;

public class Notifications extends ServerURL {
        private String lastTime = "1970-01-01 00:00:00";

          public void loadNotificationsRequests(DefaultTableModel model) {
            String params = "action=get&client_fetch_token_request=;;mslaundryshop2025;;"
                        + "&since=" + URLEncoder.encode(lastTime, StandardCharsets.UTF_8);
            HttpURLConnection conn = createPostConnection("get_notifications.php", params);
            JSONArray arr = parseJsonArrayResponse(conn);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int id =         obj.getInt("id");
                String time    = obj.getString("time");
                String message = obj.getString("message");

                    // Toolkit.getDefaultToolkit().beep();
                    model.addRow(new Object[]{id, time, message });

                if (time.compareTo(lastTime) > 0) {
                    lastTime = time;
                }
            }
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
                    loadNotificationsRequests(model);
                    JOptionPane.showMessageDialog(null,
                            "Notification removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(null,
                        resp.optString("message", "Remove failed."),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
                e.printStackTrace();
                return new JSONArray();
            }
        }
}

