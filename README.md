# httpserver

## **Overview**

This is a Java code for a single-threaded HTTP server. The server listens for incoming connections using the ServerSocket class, and accepts client requests using clientSocket.accept(). When a request is received, the method processClientRequest is called to handle the request. The request is read using a BufferedReader from the clientSocket's input stream, and the response is written using a PrintWriter and BufferedOutputStream to the clientSocket's output stream.

The method parseRequestLine is used to parse the request line from the incoming request and determine the HTTP method (GET or HEAD) and the requested file. If the method is not GET or HEAD, an HTTP response is sent indicating that the method is not supported. If the method is GET or HEAD, the server will attempt to read the requested file. If the file is found, it is returned to the client with an HTTP 200 OK response. If the file is not found, a 404 Not Found response is sent to the client.

Logging is performed using the SLF4J library. The log level is set using the LoggerFactory.getLogger method. The code logs warnings and errors related to processing client requests and closing streams.
    

use this dependency for logger
<dependencies>
<dependency>
<groupId>org.slf4j</groupId>
<artifactId>slf4j-simple</artifactId>
<version>2.0.0-alpha5</version>
</dependency>
</dependencies>
Step 1 : Run the Main class
step 2 : open the web browser
step 3 : http://localhost:8060/punchline.html

