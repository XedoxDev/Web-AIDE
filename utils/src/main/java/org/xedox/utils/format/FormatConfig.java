package org.xedox.utils.format;


public class FormatConfig {

    private static FormatConfig instance;
    private int indentSize = 4;
    private boolean useTab = false;

    private FormatConfig() {}

    public static FormatConfig getInstance() {
        if (instance == null) {
            instance = new FormatConfig();
        }
        return instance;
    }

    public int getIndentSize() {
        return this.indentSize;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    public boolean getUseTab() {
        return this.useTab;
    }

    public void setUseTab(boolean useTab) {
        this.useTab = useTab;
    }
}