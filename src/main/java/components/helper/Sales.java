package src.main.java.components.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONArray;
import org.json.JSONObject;

import src.server.java.ServerURL;

public class Sales extends ServerURL {
    private String lastTime = "0000-00-00 00:00:00";

public void loadSalesChartData(DefaultCategoryDataset dataset) {
    String params = "action=get&client_fetch_token_request=;;mslaundryshop2025;;"
                  + "&since=" + lastTime;

    HttpURLConnection conn = createPostConnection("get_sales.php", params);
    JSONArray arr = parseJsonArrayResponse(conn);

    dataset.clear();

    for (int i = 0; i < arr.length(); i++) {
        JSONObject obj = arr.getJSONObject(i);
        String time        = obj.getString("time");
        String monthName   = obj.getString("month_name");
        int year           = obj.getInt("year");
        String branch      = obj.getString("branch");
        double totalSales  = obj.getDouble("total_sales");

        String columnKey = monthName + " " + year;
        dataset.setValue(totalSales, branch, columnKey);


        if (time.compareTo(lastTime) > 0) {
            lastTime = time;
        }
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
