package com.api.bugzapper.configuration;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class PushNotificationOptions {
    public static final String REST_API_KEY = "ZGM2ZDgxOWYtOTQzMS00MWZlLWJmOWEtM2E1NTdjYmM4NWU3";
    public static final String APP_ID = "f443c82b-9a63-4822-a1c0-8c7db5d55b30";

    // Callers (phase/project/task/report/apply flows) invoke this per-recipient in a loop on the
    // request thread. It has no Spring bean to hang @Async off (static utility, called from many
    // services), so the actual OneSignal HTTP call is offloaded onto this pool instead — the
    // request no longer waits on OneSignal's response, and a hung/slow OneSignal call can't stall
    // an unrelated HTTP request thread.
    private static final ScheduledExecutorService NOTIFY_POOL = Executors.newScheduledThreadPool(4);

    public static void sendMessageToUser(String message, Integer userId, String title) {
        NOTIFY_POOL.execute(() -> sendMessageToUserBlocking(message, userId, title));
    }

    private static String mountResponseRequest(HttpURLConnection con, int httpResponse) throws IOException {
        String jsonResponse;
        if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            scanner.close();
        } else {
            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            scanner.close();
        }
        return jsonResponse;
    }

    private static void sendMessageToUserBlocking(String message, Integer userId, String title) {
        HttpURLConnection con = null;
        try {
            String jsonResponse;

            URL url = new URL("https://onesignal.com/api/v1/notifications");
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(5000);
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", "Basic " + REST_API_KEY);
            con.setRequestMethod("POST");

            String strJsonBody = "{"
                    + "\"app_id\": \"" + APP_ID + "\","
                    + "\"include_aliases\": { "
                    + "\"external_id\": [\"u" + userId + "\"]},"
                    + "\"target_channel\": \"push\","
                    + "\"data\": {\"foo\": \"bar\"},"
                    + "\"contents\": {\"en\": \"" + message + "\"},"
                    + "\"headings\": {\"en\": \"" + title + "\"}"
                    + "}";


            System.out.println("strJsonBody:\n" + strJsonBody);

            byte[] sendBytes = strJsonBody.getBytes("UTF-8");
            con.setFixedLengthStreamingMode(sendBytes.length);

            OutputStream outputStream = con.getOutputStream();
            outputStream.write(sendBytes);

            int httpResponse = con.getResponseCode();
            System.out.println("httpResponse: " + httpResponse);

            jsonResponse = mountResponseRequest(con, httpResponse);
            System.out.println("jsonResponse:\n" + jsonResponse);

        } catch (Throwable t) {
            System.err.println("Error sending notification to OneSignal");
            t.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
