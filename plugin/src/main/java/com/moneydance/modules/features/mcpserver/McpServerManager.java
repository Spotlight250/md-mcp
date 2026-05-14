package com.moneydance.modules.features.mcpserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Manages the lifecycle of an embedded HTTP server that speaks the
 * MCP (Model Context Protocol) JSON-RPC protocol.
 *
 * Uses JDK's built-in com.sun.net.httpserver.HttpServer — zero external
 * dependencies. The server binds to 127.0.0.1 only (localhost).
 *
 * MCP protocol subset implemented:
 *   - initialize (handshake)
 *   - tools/list (discover available tools)
 *   - tools/call (execute a tool)
 *   - ping (keepalive)
 */
public class McpServerManager {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 38867;

    private final Main extension;
    private HttpServer httpServer;
    private ExecutorService executor;
    private volatile boolean running = false;

    // MCP protocol handler
    private final McpProtocolHandler protocolHandler;

    public McpServerManager(Main extension) {
        this.extension = extension;
        this.protocolHandler = new McpProtocolHandler(extension);
    }

    /**
     * Start the HTTP server on localhost. 
     * Runs in a background thread so it doesn't block the Moneydance EDT.
     */
    public synchronized void start() {
        if (running) {
            Main.log("MCP server is already running on " + HOST + ":" + PORT);
            return;
        }

        try {
            executor = Executors.newFixedThreadPool(2);
            httpServer = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
            httpServer.createContext("/mcp", this::handleMcpRequest);
            httpServer.createContext("/health", this::handleHealthCheck);
            httpServer.setExecutor(executor);
            httpServer.start();
            running = true;
            Main.log("MCP server started on http://" + HOST + ":" + PORT + "/mcp");
        } catch (IOException e) {
            Main.log("Failed to start MCP server: " + e.getMessage());
            e.printStackTrace(System.err);
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

        Main.log("Stopping MCP server...");
        cleanup();
        Main.log("MCP server stopped.");
    }

    public boolean isRunning() {
        return running;
    }

    private void cleanup() {
        running = false;
        if (httpServer != null) {
            httpServer.stop(1); // 1 second grace period
            httpServer = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Handle MCP JSON-RPC requests on POST /mcp
     */
    private void handleMcpRequest(HttpExchange exchange) throws IOException {
        // CORS headers for local development
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
            return;
        }

        // Read request body
        String requestBody;
        try (InputStream is = exchange.getRequestBody()) {
            requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        Main.log("Request: " + requestBody);

        // Delegate to protocol handler
        String responseBody = protocolHandler.handleRequest(requestBody);

        Main.log("Response: " + responseBody);

        sendResponse(exchange, 200, responseBody);
    }

    /**
     * Simple health check endpoint.
     */
    private void handleHealthCheck(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, "{\"status\": \"ok\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        exchange.close();
    }
}
