package com.clipstack;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static String lastText = "";
    private static final ClipboardHistory historyManager = new ClipboardHistory();

    // UI Components
    private static DefaultListModel<String> listModel;
    private static JFrame frame;
    private static PopupMenu trayMenu;
    private static TrayIcon trayIcon;

    public static void main(String[] args) {
        // 1. Initialize the GUI and System Tray on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
            setupSystemTray();
        });

        // 2. Start the Background Polling Engine
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String currentText = getSystemClipboardText();
                if (currentText != null && !currentText.trim().isEmpty() && !currentText.equals(lastText)) {
                    lastText = currentText;
                    historyManager.addSnippet(currentText);
                    updateUI(); // Refresh both the Swing Window and the System Tray Menu
                }
            } catch (Exception e) {
                // Ignore background polling errors
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("ClipStack - History Manager");
        // Hide the window instead of exiting when the user clicks 'X'
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(listModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Last 10 Copied Items"));

        JButton copyButton = new JButton("Copy Selected to Clipboard");
        copyButton.addActionListener(e -> {
            String selectedText = historyList.getSelectedValue();
            if (selectedText != null) {
                setSystemClipboardText(selectedText);
                lastText = selectedText;
                JOptionPane.showMessageDialog(frame, "Successfully copied to OS clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an item first.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(copyButton, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported on this OS.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        trayMenu = new PopupMenu();
        Image icon = createDynamicIcon();

        trayIcon = new TrayIcon(icon, "ClipStack", trayMenu);
        trayIcon.setImageAutoSize(true);

        // Double-clicking the tray icon brings the Swing window back up
        trayIcon.addActionListener(e -> {
            frame.setVisible(true);
            frame.setState(Frame.NORMAL);
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        }

        updateUI();
    }

    private static void updateUI() {
        SwingUtilities.invokeLater(() -> {
            // A. Update the Swing GUI List
            listModel.clear();
            List<String> history = historyManager.getHistory();
            for (String snippet : history) {
                listModel.addElement(snippet);
            }

            // B. Update the System Tray Menu
            if (trayMenu != null) {
                trayMenu.removeAll();

                MenuItem openApp = new MenuItem("Open ClipStack UI");
                openApp.addActionListener(e -> {
                    frame.setVisible(true);
                    frame.setState(Frame.NORMAL);
                });
                trayMenu.add(openApp);
                trayMenu.addSeparator();

                if (history.isEmpty()) {
                    MenuItem emptyItem = new MenuItem("(Empty - Copy text to start)");
                    emptyItem.setEnabled(false);
                    trayMenu.add(emptyItem);
                } else {
                    for (int i = 0; i < history.size(); i++) {
                        String text = history.get(i);
                        String displayText = (i + 1) + ". " + (text.length() > 40 ? text.substring(0, 37) + "..." : text);
                        MenuItem item = new MenuItem(displayText);

                        item.addActionListener(e -> {
                            setSystemClipboardText(text);
                            lastText = text;
                            trayIcon.displayMessage("ClipStack", "Copied to clipboard!", TrayIcon.MessageType.INFO);
                        });
                        trayMenu.add(item);
                    }
                }

                trayMenu.addSeparator();
                MenuItem exitItem = new MenuItem("Exit ClipStack");
                exitItem.addActionListener(e -> System.exit(0));
                trayMenu.add(exitItem);
            }
        });
    }

    // Creates a visual tray icon programmatically so you don't need external image files
    private static Image createDynamicIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(41, 128, 185)); // Blue
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("C", 3, 13);
        g.dispose();
        return image;
    }

    private static String getSystemClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {}
        return null;
    }

    private static void setSystemClipboardText(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        } catch (Exception e) {}
    }
}