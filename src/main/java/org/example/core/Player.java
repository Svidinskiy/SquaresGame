package org.example.core;

// Класс игрока
public class Player {
    private final String type;
    private final char color;

    public Player(String type, char color) {
        String lowerType = type.toLowerCase();
        if (!lowerType.equals("user") && !lowerType.equals("comp")) {
            throw new IllegalArgumentException("Invalid player type: must be 'user' or 'comp'");
        }
        this.type = lowerType;

        char upperColor = Character.toUpperCase(color);
        if (upperColor != 'W' && upperColor != 'B') {
            throw new IllegalArgumentException("Invalid color: must be 'W' or 'B'");
        }
        this.color = upperColor;

    }

    public String getType() {
        return type;
    }

    public char getColor() {
        return color;
    }

    public boolean isComputer() {
        return type.equals("comp");
    }
}