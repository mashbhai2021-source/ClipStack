# ClipStack
ClipStack – Desktop Clipboard History Manager

Description

A background utility that monitors the operating system's clipboard. Every time a user copies
text (Ctrl+C), the application grabs it and keeps a rolling history of the last 10 copied snippets.
Users can view the history list and instantly select an older snippet to re-copy it to their current
clipboard.

Tools / Resources

• Language: Java (JDK 8+)
• Libraries: java.awt.datatransfer (Clipboard API), java.util.LinkedList (In-memory
storage)
• IDE: Any standard Java IDE

Steps

1. Gain access to the operating system's clipboard using Java's Toolkit API.
2. Implement a background thread or a simple timer loop that checks the clipboard every 1
second for new text content.
3. If a new string is detected, push it into a fixed-size LinkedList data structure (e.g., max 10
items). If the list is full, pop the oldest item out.
4. Provide a console menu or system tray menu allowing users to view the list and choose
an item to place back into the active system clipboard.

Architecture

Event-Polling / In-Memory Architecture:
OS Clipboard to Java Polling Thread to Fixed-Size Memory Stack (RAM) to User UI Panel.
Outcome
An ultra-handy daily desktop tool that saves time by extending clipboard memory, running
entirely inside the computer's temporary RAM.
