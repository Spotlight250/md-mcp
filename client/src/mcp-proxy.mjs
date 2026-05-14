import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";

import { ListToolsRequestSchema, CallToolRequestSchema } from "@modelcontextprotocol/sdk/types.js";

const MONEYDANCE_URL = process.argv[2] || 'http://127.0.0.1:38867/mcp';

/**
 * A proxy server that translates stdio MCP requests from an AI Agent
 * into HTTP JSON-RPC requests for the Moneydance plugin.
 */
class MoneydanceProxyServer {
    constructor() {
        this.server = new Server(
            { name: "moneydance-proxy", version: "1.0.0" },
            { capabilities: { tools: {} } }
        );

        // Forward ListToolsRequest
        this.server.setRequestHandler(ListToolsRequestSchema, async () => {
            const result = await this.forwardRequest('tools/list', {});
            return result;
        });

        // Forward CallToolRequest
        this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
            const result = await this.forwardRequest('tools/call', {
                name: request.params.name,
                arguments: request.params.arguments
            });
            return result;
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
            
            const data = await response.json();
            
            if (data.error) {
                throw new Error(data.error.message);
            }
            return data.result;
        } catch (err) {
            return {
                content: [{ type: "text", text: `Proxy Error: ${err.message}` }],
                isError: true
            };
        }
    }

    async run() {
        const transport = new StdioServerTransport();
        await this.server.connect(transport);
    }
}

const server = new MoneydanceProxyServer();
server.run().catch(console.error);
