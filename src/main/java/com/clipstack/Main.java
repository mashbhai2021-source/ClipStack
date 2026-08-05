package com.clipstack;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static String lastText = "";

    public static void main(String[] args) {
        System.out.println("=== ClipStack Polling Engine Started ===");
        System.out.println("Monitoring system clipboard every 1000ms... (Press Ctrl+C to stop)");


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


        scheduler.scheduleAtFixedRate(() -> {
            try {
                String currentText = getSystemClipboardText();


                if (currentText != null && !currentText.equals(lastText)) {
                    lastText = currentText;
                    System.out.println("[DETECTED CLIPBOARD CHANGE] -> " + currentText);
                }
            } catch (Exception e) {
                System.err.println("Error reading clipboard: " + e.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down background thread gracefully...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            System.out.println("ClipStack stopped.");
        }));
    }


    private static String getSystemClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {

        }
        return null;
    }
}