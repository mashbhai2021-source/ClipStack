package com.clipstack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ClipboardHistory {
    private static final int MAX_CAPACITY = 10;


    private final LinkedList<String> history;

    public ClipboardHistory() {
        this.history = new LinkedList<>();
    }


    public void addSnippet(String snippet) {
        if (snippet == null || snippet.trim().isEmpty()) {
            return; // Ignore null or empty copies
        }


        if (history.size() >= MAX_CAPACITY) {
            history.removeFirst();
        }


        history.addLast(snippet);
    }


    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getSize() {

        return history.size();
    }
}