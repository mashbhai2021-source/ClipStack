package com.clipstack;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ClipboardManager {

    // Reads from OS Clipboard with Defensive Checks
    public static String readSystemClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard == null) return null;

            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
                System.out.println("[DEBUG] Ignored clipboard data: Image payload detected.");
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
                System.out.println("[DEBUG] Ignored clipboard data: File/Folder payload detected.");
            }
        } catch (IllegalStateException e) {
            System.err.println("[WARNING] OS Clipboard temporarily locked. Retrying next cycle...");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to read clipboard data: " + e.getMessage());
        }
        return null;
    }

    // Writes to OS Clipboard
    public static void writeToSystemClipboard(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to inject text back into the system clipboard.");
        }
    }
}