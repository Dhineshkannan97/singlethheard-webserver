package com.server;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.StringTokenizer;

public class Server {
    static org.slf4j.Logger logger = LoggerFactory.getLogger(Server.class);

    public void getConnections(ServerSocket serverSocket) throws IOException {
        while (true) {
            Socket clientSocket;

            Instant startTime = Instant.now();
            try {
                clientSocket = serverSocket.accept();
                Date date = new Date();
                SimpleDateFormat DateFor = new SimpleDateFormat("MM/dd/yyyy");
                String stringDate = DateFor.format(date);
                logger.info("\u001B[33m" + "client send request");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("punchline.html");
                InputStreamReader isReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer htmlLine = new StringBuffer();
                String str;
                while ((str = reader.readLine()) != null) {
                    htmlLine.append(str);
                }
                String browserLine;
                browserLine = in.readLine();
                if (browserLine != null) {
                    logger.info(browserLine);
                    StringTokenizer parse = new StringTokenizer(browserLine);
                    String method = parse.nextToken().toUpperCase();
//                    System.out.println(method);
                    String fileRequest = parse.nextToken().toLowerCase();
//                    System.out.println(fileRequest);
                    logger.info(String.valueOf(htmlLine));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                    if (fileRequest.contains("punchline.html") || fileRequest.equals(null) || fileRequest.equals("/")) {
                        out.print("HTTP/1.1 200 OK\n");
                        out.print("Content-Length: " + htmlLine.length() + "\n");
                        out.print("Content-Type: text/html; charset=utf-8\n");
                        out.print("Date: " + stringDate + "\n");
                        out.print("\n");
                        out.print(htmlLine);
                    } else {
                        out.println("HTTP/1.1 404 File Not Found");
                        out.print("Content-Length: " + htmlLine.length() + "\n");
                        out.print("Content-Type: text/html; charset=utf-8\n");
                        out.print("Date: " + stringDate + "\n");
                        out.print("\n"); // blank line between headers and content
                        out.print(htmlLine);
                    }
                    out.flush();
//                clientSocket.close();
                    Instant endTime = Instant.now();
                    Duration actualDelay = Duration.between(startTime, endTime);
                    logger.info(" actual delay: " + actualDelay.toMillis() + " milliseconds.");
                } else {
                    logger.error("browserLine is null");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}

