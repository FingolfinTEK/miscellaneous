package com.fingy.gui;

import javax.swing.JTextArea;

public class AppendableJTextArea extends JTextArea {

    private static final long serialVersionUID = 1L;

    public synchronized void appendLine(final String lineText) {
        String text = getText();
        text += lineText;
        text += "\n";
        setText(text);
    }

}
