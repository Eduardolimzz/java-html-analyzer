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

    static class HttpFetcher {
        static String fetch(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setInstanceFollowRedirects(true);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new Exception("Non-OK HTTP response: " + status);
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

                // Tag de abertura
                if (isOpeningTag(line)) {
                    String tagName = extractTagName(line);
                    stack.push(tagName);
                }
                // Tag de fechamento
                else if (isClosingTag(line)) {
                    String tagName = extractTagName(line);

                    if (stack.isEmpty()) {
                        throw new MalformedHtmlException();
                    }

                    String expectedTag = stack.pop();
                    if (!expectedTag.equalsIgnoreCase(tagName)) {
                        throw new MalformedHtmlException();
                    }
                }
                // Texto
                else {
                    int currentDepth = stack.size();
                    if (currentDepth > maxDepth) {
                        maxDepth = currentDepth;
                        deepestText = line;
                    }
                }
            }

            // Verificar se todas as tags foram fechadas
            if (!stack.isEmpty()) {
                throw new MalformedHtmlException();
            }

            return deepestText != null ? deepestText : "";
        }

        private static boolean isOpeningTag(String line) {
            return line.startsWith("<") &&
                    line.endsWith(">") &&
                    !line.startsWith("</") &&
                    !line.startsWith("<!") &&
                    !line.startsWith("<?");
        }

        private static boolean isClosingTag(String line) {
            return line.startsWith("</") && line.endsWith(">");
        }

        private static String extractTagName(String line) {
            String content;

            if (line.startsWith("</")) {
                content = line.substring(2, line.length() - 1);
            } else {
                content = line.substring(1, line.length() - 1);
            }

            return content.trim();
        }
    }

    static class MalformedHtmlException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}