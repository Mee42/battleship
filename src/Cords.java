import java.util.Objects;

/**
  * This is a wrapper class around (int r, int c) to avoid errors.
  * It allows List<Cords> to be made, as List<int,int> is not a thing.
  * It also defines a boolean isOnBoard() method that makes sure that the given cordinites is on the board
  * This moves that repeated functionality to one place. 
  */
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
    public int hashCode() {
        return Objects.hash(r, c);
    }

    @Override
    public String toString() {
        return "Cords{" +
                "r=" + r +
                ", c=" + c +
                '}';
    }
}
