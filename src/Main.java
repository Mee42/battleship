import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class Main {
    public static final int SIZE = 10;
    private static final int h = 400;
    private static final int w = 400;
    private static final List<Integer> shipSizes = Arrays.asList(5,4,3,3,2);


    public static void main(String[] args) {

        Board user = new Board();
        user.forButton((button) -> button.setBackground(Color.WHITE));

        user.panel.setSize(w,h);
        JFrame frame = new JFrame();
        checkKeyPress();
        frame.add(user.panel);
        frame.setSize(w,h);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Battleship");
        frame.setVisible(true);



        //start the ship positioning
        //loop through the ships

        AtomicReference<Rotation> rotation = new AtomicReference<>(Rotation.UP);//0 is up, 1 is right, 2 is down, 3 is right
        AtomicInteger index = new AtomicInteger(0);
        Producer<Boolean> inShipPlacement = () -> index.get() < shipSizes.size();
        AtomicReference<Cords> lastCords = new AtomicReference<>(new Cords(0,0));

    user.forEach(
        item -> {
          addOnHover(
              item.button,
              button -> {
                if (inShipPlacement.get()) {
                  // ON HOVER
                  redraw(new Ship(item.pos, rotation.get(), shipSizes.get(index.get())),user);
                  lastCords.set(item.pos);
                }
              },
              button -> {
                if (inShipPlacement.get()) {
                    // ON UNHOVER
                    redraw(new Ship(item.pos, rotation.get(), shipSizes.get(index.get())),user);
                    lastCords.set(item.pos);
                }
              });
          item.button.addActionListener(
              (x) -> {
                // ON CLICK
                if (inShipPlacement.get()) {
                  Ship on = new Ship(item.pos, rotation.get(), shipSizes.get(index.get()));
                  if (on.getSquares().stream().allMatch(Cords::isOnBoard)) {
                    //if all of them are on the board
                    List<Square> squares =
                        on.getSquares().stream().map(user::get).collect(toList());
                    if (squares.stream().noneMatch(Square::isShip)) {
                      // if none are ships or off the board, apply
                      squares.forEach(it -> it.setShip(on).setColor(Color.BLUE));
                      index.getAndIncrement();
                      user.addShip(on);
                    }
                  }
                }
              });
        });

    // add listener to change rotation
    addOnKeyPress(
        82,
        () -> {
          if (inShipPlacement.get()) {
              rotation.getAndUpdate(Rotation::next);
              System.out.println("Rotation mode: " + rotation.get());
              redraw(new Ship(lastCords.get(),rotation.get(),shipSizes.get(index.get())),user);
          }
        });
        //noinspection StatementWithEmptyBody
        while(inShipPlacement.get()){
            //block the main thread till everything is done
        }
        System.out.println("DONE");



        private
    }

    private static void redraw(Ship on,Board board){
        List<Cords> squaresOn = on.getSquares();
        board.forEach(square -> {
            if(square.isShip() && squaresOn.contains(square.pos)){
                square.setColor(Color.RED);
            }else if(square.isShip()){
                square.setColor(Color.BLUE);
            }else if(squaresOn.contains(square.pos)){
                square.setColor(Color.GRAY);
            }else{
                square.setColor(Color.WHITE);
            }
        });
    }

    private static void checkKeyPress(){
        addOnKeyPress(e -> System.out.println("Got key event = " + e.getKeyCode()));

    }
    private static void addOnKeyPress(int keycode, Runnable runner){
        addOnKeyPress(e -> {
                    if(e.getKeyCode() == keycode){
                        runner.run();
                    }
                    });
    }
    private static void addOnKeyPress(Consumer<KeyEvent> consumer){
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if(e.getID() == KeyEvent.KEY_PRESSED){
                        consumer.accept(e);
                    }
                    return false;
                });
    }
    private static void addOnHover(JButton button,
                                   Consumer<JButton> consumer,
                                   Consumer<JButton> elsee){
        button.getModel().addChangeListener(evt -> {
            ButtonModel model = (ButtonModel)evt.getSource();
            if(model.isRollover()){
                consumer.accept(button);
            }else{
                elsee.accept(button);
            }
        });
    }
}



