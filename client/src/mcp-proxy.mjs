import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";

import { ListToolsRequestSchema, CallToolRequestSchema, ListResourcesRequestSchema, ReadResourceRequestSchema } from "@modelcontextprotocol/sdk/types.js";

const MONEYDANCE_URL = process.argv[2] || 'http://127.0.0.1:38867/mcp';

/**
 * A proxy server that translates stdio MCP requests from an AI Agent
 * into HTTP JSON-RPC requests for the Moneydance plugin.
 */
class MoneydanceProxyServer {
    constructor() {
        this.server = new Server(
            { name: "moneydance-proxy", version: "1.0.0" },
            { capabilities: { tools: {}, resources: {} } }
        );

        // Forward ListToolsRequest
        this.server.setRequestHandler(ListToolsRequestSchema, async () => {
            return await this.forwardRequest('tools/list', {});
        });

        // Forward CallToolRequest
        this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
            return await this.forwardRequest('tools/call', {
                name: request.params.name,
                arguments: request.params.arguments
            });
        });

        // Forward ListResourcesRequest
        this.server.setRequestHandler(ListResourcesRequestSchema, async () => {
            return await this.forwardRequest('resources/list', {});
        });

        // Forward ReadResourceRequest
        this.server.setRequestHandler(ReadResourceRequestSchema, async (request) => {
            return await this.forwardRequest('resources/read', {
                uri: request.params.uri
            });
        });
    }

    async forwardRequest(method, params) {
        try {
            const response = await fetch(MONEYDANCE_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    jsonrpc: '2.0',
                    id: Date.now(),
                    method,
                    params
                })
            });
            
            if (!response.ok) {
                if (response.status === 413) {
                    throw new Error("Payload too large (limit is 1MB)");
                }
                throw new Error(`HTTP Error ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            if (data.error) {
                throw new Error(data.error.message);
            }
            return data.result;
        } catch (err) {
            console.error(`[Proxy Error] ${err.message}`);
            if (err.cause && err.cause.code === 'ECONNREFUSED') {
                throw new Error("Moneydance MCP Server is not running. Please open Moneydance and ensure the extension is loaded.");
            }
            throw err;
        }
    }

    async run() {
        const transport = new StdioServerTransport();
        await this.server.connect(transport);
    }
}

const server = new MoneydanceProxyServer();
server.run().catch(console.error);
