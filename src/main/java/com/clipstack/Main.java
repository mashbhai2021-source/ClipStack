package com.clipstack;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static String lastText = "";
    private static final ClipboardHistory historyManager = new ClipboardHistory();

    // GUI Components
    private static DefaultListModel<String> listModel;
    private static JFrame frame;

    public static void main(String[] args) {
        // 1. Initialize the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> createAndShowGUI());

        // 2. Start the Background Polling Engine
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String currentText = getSystemClipboardText();
                if (currentText != null && !currentText.trim().isEmpty() && !currentText.equals(lastText)) {
                    lastText = currentText;
                    historyManager.addSnippet(currentText);
                    updateGUI(); // Refresh the visual list when new text is found
                }
            } catch (Exception e) {
                // Ignore background polling errors
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("ClipStack - History Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Create the scrollable list to display history
        listModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(listModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Last 10 Copied Items"));

        // Create the copy button (Week 6 Logic)
        JButton copyButton = new JButton("Copy Selected to Clipboard");
        copyButton.addActionListener(e -> {
            String selectedText = historyList.getSelectedValue();
            if (selectedText != null) {
                setSystemClipboardText(selectedText);
                lastText = selectedText; // Prevent polling loop from instantly catching this
                JOptionPane.showMessageDialog(frame, "Successfully copied to OS clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an item first.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Add components to window
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(copyButton, BorderLayout.SOUTH);

        // Center on screen and display
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Safely updates the Java Swing UI from the background polling thread
    private static void updateGUI() {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (String snippet : historyManager.getHistory()) {
                listModel.addElement(snippet);
            }
        });
    }

    // Reads from OS Clipboard
    private static String getSystemClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {}
        return null;
    }

    // Writes to OS Clipboard
    private static void setSystemClipboardText(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        } catch (Exception e) {
            System.err.println("Failed to inject text back into the system clipboard.");
        }
    }
}