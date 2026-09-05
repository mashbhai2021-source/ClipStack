package com.clipstack;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static String lastText = "";
    private static final ClipboardHistory historyManager = new ClipboardHistory();

    public static void main(String[] args) {

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String currentText = getSystemClipboardText();
                if (currentText != null && !currentText.trim().isEmpty() && !currentText.equals(lastText)) {
                    lastText = currentText;
                    historyManager.addSnippet(currentText);
                }
            } catch (Exception e) {
                // Ignore background errors
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);


        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== ClipStack Console UI ===");

        while (running) {
            System.out.println("\n[1] View Clipboard History");
            System.out.println("[2] Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayAndSelectHistory(scanner);
                    break;
                case "2":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }


        System.out.println("Shutting down ClipStack...");
        scheduler.shutdownNow();
        scanner.close();
        System.exit(0);
    }

    private static void displayAndSelectHistory(Scanner scanner) {
        List<String> history = historyManager.getHistory();
        if (history.isEmpty()) {
            System.out.println("\nHistory is empty. Go copy some text!");
            return;
        }

        System.out.println("\n--- Last 10 Copied Items ---");
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i));
        }
        System.out.println("0. Cancel");
        System.out.print("Pick a number to re-copy (0 to cancel): ");

        try {
            int selection = Integer.parseInt(scanner.nextLine());
            if (selection > 0 && selection <= history.size()) {
                String selectedText = history.get(selection - 1);
                setSystemClipboardText(selectedText);
                lastText = selectedText; // Prevent polling loop from instantly re-adding it
                System.out.println("\n>> SUCCESS: Copied back to clipboard!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
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

        } catch (IllegalStateException e) {
            System.err.println("\n[ERROR] The OS clipboard is currently unavailable or locked by another application.");
        } catch (Exception e) {
            System.err.println("\n[ERROR] Failed to inject text back into the system clipboard.");
        }
    }

}