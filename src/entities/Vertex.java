package entities;

import java.util.Objects;

public class Vertex {
    private int row;
    private int column;

    public Vertex(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return row == vertex.row &&
                column == vertex.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
