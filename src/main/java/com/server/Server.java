package com.server;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class Server {
    private int maxConnections = 100;
    private int connectionsProcessed = 0;
    static org.slf4j.Logger logger = LoggerFactory.getLogger(Server.class);
    boolean verbose = true;
    static final File WEB_ROOT = new File("C:\\Users\\Dhinesh Kannan\\Documents\\Streams\\singlethheard-webserver\\src\\main\\resources");
    static final String DEFAULT_FILE = "punchline.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    Socket clientSocket;

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
//        String requestedFile = null;
        try {
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = new PrintWriter(clientSocket.getOutputStream());
            dataOutStream = new BufferedOutputStream(clientSocket.getOutputStream());
            String[] request = parseAndReadLines(inStream);
            String method = request[0];
            String requestedFile = request[1];
            if (!method.equals("GET") && !method.equals("HEAD")) {
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                byte[] fileData = readFileData(file, fileLength);
                // we send HTTP Headers with data to client
                outStream.println("HTTP/1.1 501 Not Implemented");
                outStream.println("Server: Java HTTP Server from S : 1.0");
                outStream.println("Date: " + new Date());
                outStream.println("Content-type: " + contentMimeType);
                outStream.println("Content-length: " + fileLength);
                outStream.println(); // blank line between headers and content, very important !
                outStream.flush(); // flush character output stream buffer
                // file
                dataOutStream.write(fileData, 0, fileLength);
                dataOutStream.flush();

            } else {
                // GET or HEAD method
                if (requestedFile.endsWith("/")) {
                    requestedFile += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, requestedFile);
                int fileLength = (int) file.length();
                String content = getContentType(requestedFile);

                if (method.equals("GET")) { // GET method so we return content
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP Headers
                    outStream.println("HTTP/1.1 200 OK");
                    outStream.println("Server: Java HTTP Server from Dhinesh : 1.0");
                    outStream.println("Date: " + new Date());
                    outStream.println("Content-type: " + content);
                    outStream.println("Content-length: " + fileLength);
                    outStream.println(); // blank line between headers and content, very important !
                    outStream.flush(); // flush character output stream buffer
                    dataOutStream.write(fileData, 0, fileLength);
                    dataOutStream.flush();
                }

                if (verbose) {
                    System.out.println("File " + requestedFile + " of type " + content + " returned");
                }

            }
        } catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(outStream, dataOutStream);
            } catch (IOException ioe) {
                logger.warn("Error with file not found exception : " + ioe.getMessage());
                logger.info("File not found");
            }

        } catch (
                IOException ioe) {
            logger.warn("Server error : " + ioe);
        } finally {
            try {
                inStream.close();
                outStream.close();
                dataOutStream.close();
//               we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

//        }
            Instant endTime = Instant.now();
            Duration actualDelay = Duration.between(startTime, endTime);
            logger.info(" actual delay: " + actualDelay.toMillis() + " milliseconds.");
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    private String[] parseRequestLine(String requestLine) {
        StringTokenizer parse = new StringTokenizer(requestLine);
        String method = parse.nextToken().toUpperCase();
        String requestedFile = parse.nextToken().toLowerCase();
        return new String[]{method, requestedFile};
    }

    private String[] parseAndReadLines(BufferedReader inStream) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = inStream.readLine()) != null) {
            lines.add(line);
            if (line.isEmpty()) {
                break;
            }
        }
        String[] request = parseRequestLine(lines.get(0));
        String method = request[0];
        String requestedFile = request[1];

        return new String[]{method, requestedFile};
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from dhinesh : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }
}


