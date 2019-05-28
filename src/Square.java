import javax.swing.*;

abstract class Square{
    final int r;
    final int c;
    final JButton button;
    Square(int r,int c){
        this.r = r;
        this.c = c;
        button = new JButton();
    }
}
