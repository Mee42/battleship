import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * This returns itself on set methods to make chaning them easier
 */
class Square{
    final Cords pos;
    final JButton button;
    private boolean hasBeenClicked;
    @Nullable private Ship parent = null;

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
    @Nullable Ship getShip(){ return parent; }

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
