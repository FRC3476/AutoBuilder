package me.varun.autobuilder.events.textchange;

import me.varun.autobuilder.gui.elements.TextBox;

public interface TextChangeListener {
    /**
     * Fired when text in a textbox is changed
     * @param text new text
     * @param textBox textbox that called this
     */
    void onTextChange(String text, TextBox textBox);
}