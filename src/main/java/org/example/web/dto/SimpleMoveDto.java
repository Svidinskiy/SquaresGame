package org.example.web.dto;

public class SimpleMoveDto {
    private int x;
    private int y;
    private String color;   // Цвет хода (b или w)
    private String message; // Статус игры или ошибки

    public SimpleMoveDto(int x, int y, String color, String message) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.message = message;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
