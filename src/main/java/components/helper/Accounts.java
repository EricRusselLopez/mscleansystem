package src.main.java.components.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import src.server.java.ServerURL;

public class Accounts extends ServerURL {
    
private String lastTime = "1970-01-01 00:00:00";

public void loadAccountsRequests(DefaultTableModel model) {
    try {
        String params = "action=get&client_fetch_token_request=;;mslaundryshop2025;;"
                      + "&since=" + URLEncoder.encode(lastTime, StandardCharsets.UTF_8);
        HttpURLConnection conn = createPostConnection("get_approvals.php", params);

        JSONArray employees = parseJsonArrayResponse(conn);

        for (int i = 0; i < employees.length(); i++) {
            JSONObject emp = employees.getJSONObject(i);
            final String firstname = emp.getString("firstname");
            final String lastname  = emp.getString("lastname");
            final String email     = emp.getString("email");
            final String branch    = emp.getString("branch");
            final String gender    = emp.getString("gender");
            final String status    = emp.getString("status");
            final String time      = emp.getString("time");

            SwingUtilities.invokeLater(() -> {
                model.addRow(new Object[] {
                    firstname, lastname, email, branch, gender, status
                });
            });

            if (time.compareTo(lastTime) > 0) {
                lastTime = time;
            }
        }

    } catch (Exception e) {}
}



    public void approveRequest(DefaultTableModel model) {
        model.setRowCount(0);

            HttpURLConnection conn = createPostConnection(
                "get_approvals.php",
                "client_fetch_token_request=;;mslaundryshop2025;;&approved=yes"
            );

        JSONObject response = parseJsonResponse(conn);

    if (response != null && response.getBoolean("response")) {
        JSONArray employees = response.getJSONArray("data");

        for (int i = 0; i < employees.length(); i++) {
            JSONObject emp = employees.getJSONObject(i);
            String firstname = emp.getString("firstname");
            String lastname = emp.getString("lastname");
            String email = emp.getString("email");
            String branch = emp.getString("branch");
            String gender = emp.getString("gender");
            String status = emp.getString("status");

           SwingUtilities.invokeLater(() -> {
                model.addRow(new Object[] {
                    firstname, lastname, email, branch, gender, status
                });
                model.fireTableDataChanged();
           });
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
