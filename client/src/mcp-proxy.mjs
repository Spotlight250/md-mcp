#!/usr/bin/env node

/**
 * Moneydance MCP Bridge Proxy (Zero-Dependency)
 * 
 * This script bridges MCP stdio (Standard Input/Output) used by AI agents
 * to the HTTP JSON-RPC server running inside Moneydance.
 * 
 * Requirements: Node.js v18.0.0 or higher
 * Dependencies: None
 */

import { createInterface } from 'readline';

const MONEYDANCE_URL = process.argv[2] || 'http://127.0.0.1:38867/mcp';

// Set up readline to read from stdin (AI agent -> Proxy)
const rl = createInterface({
    input: process.stdin,
    terminal: false
});

// Process each line (MCP message)
rl.on('line', async (line) => {
    if (!line.trim()) return;

    let request;
    try {
        request = JSON.parse(line);
    } catch (e) {
        return; // Ignore malformed JSON
    }

    try {
        // Forward to Moneydance
        const response = await fetch(MONEYDANCE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: line,
            signal: AbortSignal.timeout(60000) // Increase timeout for complex calculations
        });

        const responseText = await response.text();
        if (responseText) {
            process.stdout.write(responseText + '\n');
        }

    } catch (err) {
        // GHOST MODE: If Moneydance is closed, pretend we are a successful but empty server
        if (request.method === 'initialize') {
            const ghostInit = {
                jsonrpc: "2.0",
                id: request.id,
                result: {
                    protocolVersion: "2024-11-05",
                    capabilities: { tools: {}, resources: {} },
                    serverInfo: { name: "moneydance-mcp (offline)", version: "0.1.0" }
                }
            };
            process.stdout.write(JSON.stringify(ghostInit) + '\n');
            console.error("[Moneydance Bridge] Moneydance is offline. Entering Ghost Mode.");
        } else if (request.method === 'tools/list' || request.method === 'resources/list') {
            const ghostList = {
                jsonrpc: "2.0",
                id: request.id,
                result: { tools: [], resources: [] }
            };
            process.stdout.write(JSON.stringify(ghostList) + '\n');
        } else {
            // For actual tool calls, we should report that Moneydance is needed
            sendError(line, -32001, "Moneydance is not running. Please open Moneydance to use this tool.");
            console.error(`[Moneydance Bridge Error] ${err.message}`);
        }
    }
});

/**
 * Sends a valid JSON-RPC 2.0 error response back to the agent
 */
function sendError(originalLine, code, message) {
    try {
        const request = JSON.parse(originalLine);
        if (request.id !== undefined) {
            const errorResponse = {
                jsonrpc: "2.0",
                id: request.id,
                error: {
                    code: code,
                    message: message
                }
            };
            process.stdout.write(JSON.stringify(errorResponse) + '\n');
        }
    } catch (e) {
        // If we can't parse the request, we can't send an ID-matched error
    }
}

// Log startup info to stderr
console.error(`[Moneydance Bridge] Proxy started. Forwarding to ${MONEYDANCE_URL}`);
console.error(`[Moneydance Bridge] Node version: ${process.version}`);
