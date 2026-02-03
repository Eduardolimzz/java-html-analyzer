import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class HtmlAnalyzer {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("URL connection error");
            return;
        }

        try {
            String html = HttpFetcher.fetch(args[0]);
            String result = HtmlParser.parse(html);
            System.out.println(result);
        } catch (MalformedHtmlException e) {
            System.out.println("malformed HTML");
        } catch (Exception e) {
            System.out.println("URL connection error");
        }
    }

    //HTTP

    static class HttpFetcher {

        static String fetch(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception();
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

    //PARSER

    static class HtmlParser {

        static String parse(String html) throws MalformedHtmlException {
            Stack<String> stack = new Stack<>();

            int maxDepth = -1;
            String deepestText = null;

            String[] lines = html.split("\n");

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (isOpeningTag(line)) {
                    String tag = extractTagName(line);
                    stack.push(tag);

                } else if (isClosingTag(line)) {
                    String tag = extractTagName(line);

                    if (stack.isEmpty() || !stack.pop().equals(tag)) {
                        throw new MalformedHtmlException();
                    }

                } else {
                    int depth = stack.size();
                    if (depth > maxDepth) {
                        maxDepth = depth;
                        deepestText = line;
                    }
                }
            }

            if (!stack.isEmpty()) {
                throw new MalformedHtmlException();
            }

            return deepestText == null ? "" : deepestText;
        }

        static boolean isOpeningTag(String line) {
            return line.startsWith("<")
                    && line.endsWith(">")
                    && !line.startsWith("</");
        }

        static boolean isClosingTag(String line) {
            return line.startsWith("</")
                    && line.endsWith(">");
        }

        static String extractTagName(String line) {
            if (line.startsWith("</")) {
                return line.substring(2, line.length() - 1);
            }
            return line.substring(1, line.length() - 1);
        }
    }

    //EXCEPTION

    static class MalformedHtmlException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
