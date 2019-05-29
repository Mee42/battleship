
import javax.swing.*;
import java.awt.*;

/**
 * THIs is a simple class that wraps a square. It contains it's position, it's JButton, if it's been clicked, and it's ship, if it has one
 * 
 * It has two constructors for easy constructing, however it's only every constructed in the Board constructor so it's probably useless.
 * Once it's clicked, it can't be unclicked. THIs is, again, a way for the code to crash if it's incorrectly handling clicks.
 * 
 *
 * This returns itself on set methods to make chaning them easier.
 *  For example, new Square(0,0).setCOlor(COlor.BLUE).setsetShip(shiP);
 *
 */
class Square{
    final Cords pos;
    final JButton button;
    private boolean hasBeenClicked;
    //NULLABLE
    private Ship parent = null;

    Square(Cords cords){
        this.pos = cords;
        button = new JButton();
    }
    Square(int r,int c){
        this(new Cords(r,c));
    }

    int r() {
        return pos.r;
    }
    int c(){
        return pos.c;
    }

    boolean isShip(){ return parent != null; }
    Square setShip(Ship ship){
        this.parent = ship;
        return this;
    }
    /** NULLABLE */
    Ship getShip(){ return parent; }

    Square setColor(Color color){
        button.setBackground(color);
        return this;
    }

    boolean hasBeenClicked() {
        return hasBeenClicked;
    }

    void click(){
        if(hasBeenClicked)
            throw new RuntimeException("Can not click square that has already been clicked");
        if(isShip())
            parent.hit(this.pos);
        hasBeenClicked = true;
    }
}
