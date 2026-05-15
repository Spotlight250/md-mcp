package com.moneydance.modules.features.mcpserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

/**
 * A dashboard for managing the MCP Bridge.
 */
public class McpSettingsWindow extends JFrame {

    private final BridgeManager bridgeManager = new BridgeManager();
    private final JTextArea jsonArea;

    public McpSettingsWindow() {
        setTitle("AI Agent Bridge (MCP) Dashboard");
        setSize(550, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel header = new JLabel("MCP Server Configuration");
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(header, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        
        String descText = "<html><body style='width: 400px;'>" +
                "To connect an AI agent (like Claude Desktop, Cursor, or Goose) to Moneydance, " +
                "copy the configuration block below into your agent's MCP settings.</body></html>";
        contentPanel.add(new JLabel(descText), BorderLayout.NORTH);

        jsonArea = new JTextArea();
        jsonArea.setEditable(false);
        jsonArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        jsonArea.setBackground(new Color(240, 240, 240));
        jsonArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        contentPanel.add(new JScrollPane(jsonArea), BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        // Footer / Actions
        JPanel footerPanel = new JPanel(new BorderLayout());
        
        JButton copyBtn = new JButton("Copy Configuration");
        copyBtn.setPreferredSize(new Dimension(250, 40));
        copyBtn.addActionListener(e -> copyToClipboard());
        
        footerPanel.add(copyBtn, BorderLayout.CENTER);
        
        JLabel helpLabel = new JLabel("<html>Node.js: You need Node.js (version 18 or higher) to run the bridge proxy. " +
                "Download it from <font color='blue'><u>nodejs.org</u></font></html>");
        helpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI("https://nodejs.org/"));
                } catch (Exception ex) {
                    // Ignore or log
                }
            }
        });
        helpLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        footerPanel.add(helpLabel, BorderLayout.SOUTH);
        
        panel.add(footerPanel, BorderLayout.SOUTH);

        add(panel);
        
        refreshData();
    }

    private void refreshData() {
        try {
            String path = bridgeManager.deployBridge();
            String json = bridgeManager.getClaudeConfigJson(path);
            jsonArea.setText(json);
        } catch (IOException e) {
            jsonArea.setText("Error deploying bridge: " + e.getMessage());
        }
    }

    private void copyToClipboard() {
        StringSelection selection = new StringSelection(jsonArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(this, 
                "Configuration copied!\n\nPaste this into your AI agent's config file and restart the agent.", 
                "Copied", JOptionPane.INFORMATION_MESSAGE);
    }
}
