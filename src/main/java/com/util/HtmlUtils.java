package com.util;

public class HtmlUtils {
    
    /**
     * Converts HTML special characters to their entity equivalents.
     * This forces the browser to render the text literally instead of executing it.
     */
    public static String escape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}