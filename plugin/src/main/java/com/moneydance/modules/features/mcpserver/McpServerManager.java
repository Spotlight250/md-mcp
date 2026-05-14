package com.moneydance.modules.features.mcpserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.moneydance.modules.features.mcpserver.tools.*;
import com.moneydance.modules.features.mcpserver.tools.ToolRegistry;

/**
 * Manages the lifecycle of an embedded HTTP server that speaks the
 * MCP (Model Context Protocol) JSON-RPC protocol.
 *
 * Uses raw java.net.ServerSocket to avoid any dependencies on com.sun.net.httpserver
 * which is often stripped from bundled JREs like Moneydance's.
 * Binds to 127.0.0.1 only (localhost).
 */
public class McpServerManager {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 38867;
    private static final int MAX_CONTENT_LENGTH = 1024 * 1024; // 1MB limit

    private final Main extension;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private volatile boolean running = false;

    // MCP protocol handler
    private final McpProtocolHandler protocolHandler;
    private final ToolRegistry toolRegistry;
    private final com.moneydance.modules.features.mcpserver.resources.ResourceRegistry resourceRegistry;

    public McpServerManager(Main extension) {
        this.extension = extension;
        this.toolRegistry = new ToolRegistry();
        this.resourceRegistry = new com.moneydance.modules.features.mcpserver.resources.ResourceRegistry();
        
        // Register core tools
        register(new PingTool("0.1.0"));
        register(new GetAccountsTool());
        register(new GetCategoriesTool());
        register(new GetNetWorthTool());
        register(new GetTransactionsTool());
        
        // Register Phase 2 tools
        register(new GetInvestmentsTool());
        register(new GetSecurityPricesTool());
        register(new GetSecurityPerformanceTool());
        register(new GetCurrenciesTool());

        // Register Resources
        registerResource(new com.moneydance.modules.features.mcpserver.resources.AccountHierarchyResource());
        registerResource(new com.moneydance.modules.features.mcpserver.resources.SecurityMasterResource());
        registerResource(new com.moneydance.modules.features.mcpserver.resources.CategoryListResource());
        
        this.protocolHandler = new McpProtocolHandler(extension, toolRegistry, resourceRegistry);
    }

    private void register(com.moneydance.modules.features.mcpserver.tools.McpTool tool) {
        McpLogger.log("Registering tool: " + tool.getName());
        this.toolRegistry.registerTool(tool);
    }

    private void registerResource(com.moneydance.modules.features.mcpserver.resources.McpResource resource) {
        McpLogger.log("Registering resource: " + resource.getName());
        this.resourceRegistry.registerResource(resource);
    }

    /**
     * Start the HTTP server on localhost.
     */
    public synchronized void start() {
        if (running) {
            McpLogger.log("MCP server is already running on " + HOST + ":" + PORT);
            return;
        }

        try {
            serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(HOST));
            executor = Executors.newFixedThreadPool(4);
            running = true;

            // Start the accept loop in a new thread
            new Thread(this::acceptLoop, "McpServer-Accept").start();

            McpLogger.log("MCP server started on http://" + HOST + ":" + PORT + "/mcp");
        } catch (Exception e) {
            McpLogger.log("Failed to start MCP server: " + e.getMessage());
            cleanup();
        }
    }

    /**
     * Stop the HTTP server gracefully.
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }
        McpLogger.log("Stopping MCP server...");
        cleanup();
        McpLogger.log("MCP server stopped.");
    }

    public boolean isRunning() {
        return running;
    }

    private void cleanup() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            // Ignore
        }
        serverSocket = null;

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            } catch (Exception e) {
                if (running) {
                    McpLogger.log("Socket accept error: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             InputStream is = socket.getInputStream();
             OutputStream os = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            // 1. Read Request Line
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            
            String method = parts[0];
            String path = parts[1];

            // 2. Read Headers
            int contentLength = 0;
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    try {
                        contentLength = Integer.parseInt(line.substring(15).trim());
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Check Content-Length limit
            if (contentLength > MAX_CONTENT_LENGTH) {
                McpLogger.log("Payload too large: " + contentLength);
                sendResponse(os, 413, "Payload Too Large", "application/json", "{\"error\": \"Payload too large. Max " + MAX_CONTENT_LENGTH + " bytes.\"}");
                return;
            }

            // 3. CORS Preflight
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendResponse(os, 204, "No Content", "application/json", "");
                return;
            }

            // 4. Routing
            if ("/health".equals(path)) {
                sendResponse(os, 200, "OK", "application/json", "{\"status\": \"ok\"}");
                return;
            }

            if (!"/mcp".equals(path)) {
                sendResponse(os, 404, "Not Found", "application/json", "{\"error\": \"Not Found\"}");
                return;
            }

            if (!"POST".equalsIgnoreCase(method)) {
                sendResponse(os, 405, "Method Not Allowed", "application/json", "{\"error\": \"Method not allowed. Use POST.\"}");
                return;
            }

            // 5. Read Body
            char[] bodyChars = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int r = reader.read(bodyChars, read, contentLength - read);
                if (r == -1) break;
                read += r;
            }
            String requestBody = new String(bodyChars);

            McpLogger.log("Request: " + requestBody);

            // 6. Handle Protocol
            String responseBody = protocolHandler.handleRequest(requestBody);

            McpLogger.log("Response: " + responseBody);

            // 7. Send Response
            sendResponse(os, 200, "OK", "application/json", responseBody);

        } catch (Exception e) {
            McpLogger.log("Error handling client request: " + e.getMessage());
        }
    }

    private void sendResponse(OutputStream os, int statusCode, String statusText, String contentType, String body) throws Exception {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        
        String headers = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Content-Type\r\n" +
                "Connection: close\r\n\r\n";
                
        os.write(headers.getBytes(StandardCharsets.UTF_8));
        os.write(bodyBytes);
        os.flush();
    }
}
