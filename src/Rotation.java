/**
 * THis stores the next refrances as a string to avoid compile time errors
 */
public enum Rotation {
    UP("RIGHT"),
    RIGHT("DOWN"),
    DOWN("LEFT"),
    LEFT("UP");


    private String next;

    Rotation(String next) {
        this.next = next;
    }
    public Rotation next(){
        return Rotation.valueOf(next);
    }
}
