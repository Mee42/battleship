import java.util.Objects;

class Cords {
    final int r;
    final int c;

    Cords(int r, int c) {
        this.r = r;
        this.c = c;
    }

    boolean isOnBoard(){
        return r >= 0 &&
                c >= 0 &&
                r < Main.SIZE &&
                c < Main.SIZE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cords cords = (Cords) o;
        return r == cords.r &&
                c == cords.c;
    }

    @Override
    public String toString() {
        return "Cords{" +
                "r=" + r +
                ", c=" + c +
                '}';
    }
}
