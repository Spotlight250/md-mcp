/**
 * Test client for the Moneydance MCP server plugin.
 * 
 * Simulates the MCP protocol handshake and tool calls using plain HTTP.
 * Uses Node.js built-in fetch (Node 18+).
 * 
 * Usage: node src/test-client.mjs [url]
 *   Default URL: http://127.0.0.1:38867/mcp
 */

const SERVER_URL = process.argv[2] || 'http://127.0.0.1:38867/mcp';
let requestId = 0;

async function jsonRpcRequest(method, params = {}) {
    const id = ++requestId;
    const body = JSON.stringify({
        jsonrpc: '2.0',
        id,
        method,
        params,
    });

    console.log(`\n→ [${method}] (id=${id})`);
    console.log(`  Request: ${body}`);

    try {
        const response = await fetch(SERVER_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body,
        });

        const text = await response.text();
        console.log(`  Status: ${response.status}`);
        
        if (text) {
            const json = JSON.parse(text);
            console.log(`  Response: ${JSON.stringify(json, null, 2)}`);
            return json;
        }
        return null;
    } catch (error) {
        console.error(`  ✗ Error: ${error.message}`);
        throw error;
    }
}

async function jsonRpcNotification(method, params = {}) {
    const body = JSON.stringify({
        jsonrpc: '2.0',
        method,
        params,
    });

    console.log(`\n→ [${method}] (notification — no id)`);

    try {
        const response = await fetch(SERVER_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body,
        });
        const text = await response.text();
        console.log(`  Status: ${response.status} (${text || 'empty body — correct for notification'})`);
    } catch (error) {
        console.error(`  ✗ Error: ${error.message}`);
    }
}

async function main() {
    console.log('==============================================');
    console.log('  Moneydance MCP Server — End-to-End Test');
    console.log('==============================================');
    console.log(`Server URL: ${SERVER_URL}\n`);

    // --- Step 1: Health check ---
    console.log('--- Step 1: Health Check ---');
    try {
        const healthRes = await fetch(SERVER_URL.replace('/mcp', '/health'));
        const health = await healthRes.json();
        console.log(`  Health: ${JSON.stringify(health)}`);
        if (health.status !== 'ok') {
            console.error('  ✗ Health check failed');
            process.exit(1);
        }
        console.log('  ✓ Server is running');
    } catch (error) {
        console.error(`  ✗ Cannot reach server at ${SERVER_URL}`);
        console.error(`    Is Moneydance running with the MCP Server plugin installed?`);
        console.error(`    Error: ${error.message}`);
        process.exit(1);
    }

    // --- Step 2: Initialize (MCP handshake) ---
    console.log('\n--- Step 2: MCP Initialize ---');
    const initResult = await jsonRpcRequest('initialize', {
        protocolVersion: '2024-11-05',
        capabilities: {},
        clientInfo: {
            name: 'md-mcp-test-client',
            version: '1.0.0',
        },
    });

    if (initResult?.result?.protocolVersion) {
        console.log(`  ✓ Protocol version: ${initResult.result.protocolVersion}`);
        console.log(`  ✓ Server: ${initResult.result.serverInfo?.name} v${initResult.result.serverInfo?.version}`);
    } else {
        console.error('  ✗ Initialize failed — unexpected response');
        process.exit(1);
    }

    // --- Step 3: Send initialized notification ---
    console.log('\n--- Step 3: Initialized Notification ---');
    await jsonRpcNotification('notifications/initialized');
    console.log('  ✓ Sent');

    // --- Step 4: List tools ---
    console.log('\n--- Step 4: List Tools ---');
    const toolsResult = await jsonRpcRequest('tools/list');

    if (toolsResult?.result?.tools) {
        const tools = toolsResult.result.tools;
        console.log(`  ✓ Found ${tools.length} tool(s):`);
        for (const tool of tools) {
            console.log(`    - ${tool.name}: ${tool.description}`);
        }
    } else {
        console.error('  ✗ tools/list failed — unexpected response');
        process.exit(1);
    }

    // --- Step 5: Call the ping tool ---
    console.log('\n--- Step 5: Call "ping" Tool ---');
    const pingResult = await jsonRpcRequest('tools/call', {
        name: 'ping',
        arguments: {},
    });

    if (pingResult?.result?.content) {
        const content = pingResult.result.content[0];
        console.log(`  ✓ Tool response type: ${content.type}`);
        
        if (content.type === 'text') {
            const data = JSON.parse(content.text);
            console.log(`  ✓ Status: ${data.status}`);
            console.log(`  ✓ Moneydance build: ${data.moneydance_build}`);
            console.log(`  ✓ Data file: ${data.data_file}`);
            console.log(`  ✓ Server version: ${data.server_version}`);
        }
    } else {
        console.error('  ✗ tools/call failed — unexpected response');
        process.exit(1);
    }

    // --- Step 6: Ping (keepalive) ---
    console.log('\n--- Step 6: Ping (keepalive) ---');
    const keepalive = await jsonRpcRequest('ping');
    if (keepalive?.result) {
        console.log('  ✓ Pong received');
    }

    // --- Summary ---
    console.log('\n==============================================');
    console.log('  ✓ ALL TESTS PASSED — Full stack is working!');
    console.log('==============================================\n');
}

main().catch((error) => {
    console.error('\nFatal error:', error.message);
    process.exit(1);
});
