package com.server;

import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Server {
    static final String DEFAULT_FILE = "punchline.html";
    static org.slf4j.Logger logger = LoggerFactory.getLogger(Server.class);
    boolean verbose = true;
    Socket clientSocket;
    private int maxConnections = 100;
    private int connectionsProcessed = 0;

    public void getConnections(ServerSocket serverSocket) throws IOException {
        while (true) {
            if (connectionsProcessed >= maxConnections) {
                break;
            }
            clientSocket = serverSocket.accept();
            processClientRequest(clientSocket);
            connectionsProcessed++;
        }
    }

    private void processClientRequest(Socket clientSocket) throws IOException {
        Instant startTime = Instant.now();
        BufferedReader inStream = null;
        PrintWriter outStream = null;
        BufferedOutputStream dataOutStream = null;
        inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outStream = new PrintWriter(clientSocket.getOutputStream());
        dataOutStream = new BufferedOutputStream(clientSocket.getOutputStream());
        if (inStream != null) {
            Map<String, String> request = parseAndReadLines(inStream);
            String method = request.get("method");
            String requestedFile = request.get("requestedFile");
            if (!method.equals("GET") && !method.equals("HEAD")) {
                String contentMimeType = "text/html";
                // we send HTTP Headers with data to client
                outStream.println("HTTP/1.1 501 Not Implemented");
                outStream.println("Server: Java HTTP Server from S : 1.0");
                outStream.println("Date: " + new Date());
                outStream.println("Content-type: " + contentMimeType);
                outStream.println(); // blank line between headers and content, very important !
                outStream.flush(); // flush character output stream buffer
                // file
                dataOutStream.write(readHtmlFile("not_supported.html"));
                dataOutStream.flush();

            } else {
                try {
                    // GET or HEAD method
                    if (requestedFile.endsWith("/")) {
                        requestedFile += DEFAULT_FILE;
                    }
                    if (method.equals("GET") && (requestedFile.equals("/punchline.html"))) { // GET method so we return content

                        // send HTTP Headers
                        outStream.println("HTTP/1.1 200 OK");
                        outStream.println("Server: Java HTTP Server from Dhinesh : 1.0");
                        outStream.println("Date: " + new Date());
                        outStream.println(); // blank line between headers and content, very important !
                        outStream.flush(); // flush character output stream buffer
                        dataOutStream.write(readHtmlFile("punchline.html"));
                        dataOutStream.flush();
                    } else {
                        fileNotFound(outStream, dataOutStream);
                    }
                    if (verbose) {
//                            System.out.println("File " + requestedFile + " of type " + " returned");
                        logger.info("File " + requestedFile + " of type " + " returned");
                    }
                } catch (FileNotFoundException fnfe) {
                    try {
                        fileNotFound(outStream, dataOutStream);
                    } catch (IOException ioe) {
//                            System.out.println("Error with file not found exception : " + ioe.getMessage());
                        logger.warn("Error with file not found exception : " + ioe.getMessage());
                        logger.info("File not found");
                    }
                } finally {
                    try {
                        inStream.close();
                        outStream.close();
                        dataOutStream.close();
//                clientSocket.close();//               we close socket connection

                    } catch (Exception e) {
                        System.err.println("Error closing stream : " + e.getMessage());
                    }
                }
            }
//         System.out.println(" actual delay: " + actualDelay.toMillis() + " milliseconds.");
        }
        Instant endTime = Instant.now();
        Duration actualDelay = Duration.between(startTime, endTime);

        logger.info(" actual delay: " + actualDelay.toMillis() + " milliseconds.");
    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        else return "text/plain";
    }

    private String[] parseRequestLine(String requestLine) {
        StringTokenizer parse = new StringTokenizer(requestLine);
        String method = parse.nextToken().toUpperCase();
        String requestedFile = parse.nextToken().toLowerCase();
        return new String[]{method, requestedFile};
    }

    private Map<String, String> parseAndReadLines(BufferedReader inStream) throws IOException {
        Map<String, String> result = new HashMap<>();
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = inStream.readLine()) != null) {
            lines.add(line);
            if (line.isEmpty()) {
                break;
            }

        }
        System.out.println(lines.get(0));
        String[] request = parseRequestLine(lines.get(0));
        String method = request[0];
        String requestedFile = request[1];
        result.put("method", method);
        result.put("requestedFile", requestedFile);
        return result;
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut) throws IOException {
        String content = "text/html";
        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from dhinesh : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
//        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content
        out.flush();
        dataOut.write(readHtmlFile("404.html"));
        dataOut.flush();
    }

    private byte[] readHtmlFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        byte[] htmlData = outputStream.toByteArray();
        outputStream.close();
        inputStream.close();
        return htmlData;
    }

}


