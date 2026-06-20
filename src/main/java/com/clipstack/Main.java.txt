package com.clipstack;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting ClipStack Test...");
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {

                String copiedText = (String) clipboard.getData(DataFlavor.stringFlavor);

                System.out.println("Success! The clipboard currently holds:");
                System.out.println("--------------------------------------------------");
                System.out.println(copiedText);
                System.out.println("--------------------------------------------------");
            } else {
                System.out.println("There is no text on the clipboard right now.");
            }

        } catch (Exception e) {

            System.out.println("Oops! Something went wrong: " + e.getMessage());
        }
    }
}