package com.xaka.visuals;

public final class Feature {
    private final String name;
    private final Category category;
    private boolean enabled;

    public Feature(String name, Category category, boolean enabled) {
        this.name = name;
        this.category = category;
        this.enabled = enabled;
    }

    public String name() { return name; }
    public Category category() { return category; }
    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public enum Category {
        RENDER("Render"), HUD("HUD"), PLAYER("Player"), MISC("Misc");

        private final String title;
        Category(String title) { this.title = title; }
        public String title() { return title; }
    }
}
