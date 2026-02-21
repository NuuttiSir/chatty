import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

public class Main {

    private static StringBuilder msgs = new StringBuilder();
    private static final int port = 6969;
    private static AtomicInteger serverHits = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new handler1());
        server.createContext("/msg", new handler2());
        server.createContext("/registration", new handlerRegisteration());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    static class handler1 implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                    <html>

                    <body>
                    	<h1>Welcome</h1>
                    	<p>This site has been visited %d times!</p>
                    </body>

                    </html>
                    	""".formatted(serverHits.getAndIncrement());
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class handler2 implements HttpHandler {
        private static final int msgMaxLen = 100;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getRequestHeaders();
            String msg = "";
            String contentType = "";
            String response = "";
            int responseCode = 404;

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                } else {
                    response = "No content type";

                }

                if (contentType.equalsIgnoreCase("text/plain")) {
                    BufferedReader bfReader = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                    String text = bfReader.lines().collect(Collectors.joining("\n"));
                    if (text == null || text.length() > msgMaxLen) {
                        response = "No text given or was too long";
                        byte[] bytes = response.getBytes("UTF-8");

                        exchange.sendResponseHeaders(responseCode, bytes.length);
                        OutputStream outputStream = exchange.getResponseBody();
                        outputStream.write(response.getBytes());

                        outputStream.flush();
                        outputStream.close();
                    } else {
                        msgs.append(text);
                        msgs.append("\n");
                        byte[] bytes = response.getBytes("UTF-8");

                        exchange.sendResponseHeaders(responseCode, bytes.length);
                        OutputStream outputStream = exchange.getResponseBody();
                        outputStream.write(response.getBytes());

                        outputStream.flush();
                        outputStream.close();
                    }
                }
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                String responseString = msgs.toString();
                String msgSiteHTML = """
                        <html>

                        <body>
                        	<h1>MESSAGES</h1>
                        	<pre>%s</pre>
                        </body>

                        </html>
                            """.formatted(responseString);
                byte[] bytes = msgSiteHTML.getBytes("UTF-8");

                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(msgSiteHTML.getBytes());

                outputStream.flush();
                outputStream.close();

            } else {
                return;
            }
        }
    }

    public static class handlerRegisteration implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("IN REG");
            Headers headers = exchange.getRequestHeaders();
            String contentType = "";
            String response = "";
            int responseCode = 404;

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                }

                if (contentType.equalsIgnoreCase("text/plain")) {
                    BufferedReader bfReader = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                    String[] usernamePassword = bfReader.lines().collect(Collectors.joining("\n")).split(":");

                    String username = usernamePassword[0];
                    String password = usernamePassword[1];

                    response = "Username + " + username + " password " + password;
                    byte[] bytes = response.getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(response.getBytes());

                    outputStream.flush();
                    outputStream.close();
                }
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                String regSiteHTML = """
                        <html>

                        <body>
                        	<h1>REG</h1>
                        	<pre>Cant GET in REG dummy</pre>
                        </body>

                        </html>
                            """;
                byte[] bytes = regSiteHTML.getBytes("UTF-8");

                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(regSiteHTML.getBytes());

                outputStream.flush();
                outputStream.close();
            } else {
                return;
            }
        }
    }
}