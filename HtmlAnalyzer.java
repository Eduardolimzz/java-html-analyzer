import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HtmlAnalyzer {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("URL connection error");
            return;
        }

        try {
            String html = HttpFetcher.fetch(args[0]);
        } catch (Exception e) {
            System.out.println("URL connection error");
        }
    }

    static class HttpFetcher {

        static String fetch(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new Exception("Non-OK HTTP response");
            }

            StringBuilder content = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            return content.toString();
        }
    }
}