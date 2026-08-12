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

        ClipboardHistory historyManager = new ClipboardHistory();

        System.out.println("=== ClipStack History Manager Started ===");
        System.out.println("Monitoring clipboard. Maximum history size is capped at 10.");
        System.out.println("Go copy some text to see the rolling cache in action!");


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                String currentText = getSystemClipboardText();


                if (currentText != null && !currentText.trim().isEmpty() && !currentText.equals(lastText)) {
                    lastText = currentText;


                    historyManager.addSnippet(currentText);


                    System.out.println("\n[NEW CLIPBOARD EVENT DETECTED]");
                    System.out.println("Just Copied: " + currentText);
                    System.out.println("Current Stack Size: " + historyManager.getSize() + "/10");
                    System.out.println("Stack Contents (Oldest to Newest):");

                    for (String snippet : historyManager.getHistory()) {
                        System.out.println(" -> " + snippet);
                    }
                }
            } catch (Exception e) {

            }
        }, 0, 1000, TimeUnit.MILLISECONDS);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down gracefully...");
            scheduler.shutdownNow();
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