package src.server.java;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class ServerURL {
    private String baseURL;

    public ServerURL() {
        try {
            InetAddress localIp = InetAddress.getLocalHost();
            String ip = localIp.getHostAddress();
            this.baseURL = "http://" + ip + "/mrscleansystem/src/server/php/";
        } catch (Exception e) {
            this.baseURL = "http://localhost/mrscleansystem/src/server/php/";
        }
    }

    public String getFullURL(String path) {
        return baseURL + path;
    }

    public boolean pingServer() {
        try {
            URL url = new URL(getFullURL("ping.php"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
}




    public HttpURLConnection createPostConnection(String path, String postData) {
        try {
            URL url = new URL(getFullURL(path));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postData.length()));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes());
                os.flush();
            }

            return conn;
        } catch (Exception e) {
            return null;
        }
    }

    public HttpURLConnection createGetConnection(String path) {
        try {
            URL url = new URL(getFullURL(path));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            return conn;
        } catch (Exception e) {
            return null;
        }
    }
}
