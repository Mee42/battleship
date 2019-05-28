import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Main {
    static final int SIZE = 10;
    private static final int h = 400;
    private static final int w = 400;
    private static final List<Integer> shipSizes = Arrays.asList(5,4,3,3,2);


    public static void main(String[] args) {

        Board<UserShipsSquares> user = new Board<>(UserShipsSquares::new);
        user.forButton((button) -> button.setBackground(Color.WHITE));

        user.panel.setSize(w,h);
        JFrame frame = new JFrame();
        frame.add(user.panel);
        frame.setSize(w,h);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Battleship");
        frame.setVisible(true);

        //start the ship positioning
        //loop through the ships

        int rotation = 0;//0 is up, 1 is right, 2 is down, 3 is right
        AtomicInteger index = new AtomicInteger(0);
        Producer<Boolean> inShipPlacement = () -> index.get() < shipSizes.size();
        //TODO
        // This is a fucking mess. Replace all of this with a Ship class, which is generated from r,c,rot,index.
        // have a Ship#overlaps(Ship other) and Ship#isOverEdge()
        // and Ship#forEachButton and Ship#forEachItem
        // squares should have up references to the ship they belong to or null
        // replace the isShip with a get method which checks the parent ship to null
    user.forEach(
        item -> {
          addOnHover(
              item.button,
              button -> {
                if (inShipPlacement.get()) {

                  // if it's still running the ship selection
                  List<Cords> cords = shipLocations(item.r, item.c, rotation, index.get());

                  user.forEach(
                      around -> {
                        if (cords.stream().anyMatch((x) -> x.r == around.r && x.c == around.c)) {
                          if (around.isShip) {
                            around.button.setBackground(Color.RED);
                          } else {
                            around.button.setBackground(Color.GRAY);
                          }
                        }
                      });
                }
              },
              button -> {
                if (inShipPlacement.get()) {
                  // if it's still running the ship selection
                  List<Cords> cords = shipLocations(item.r, item.c, rotation, index.get());
                  user.forEach(
                      around -> {
                        if (cords.stream().anyMatch((x) -> x.r == around.r && x.c == around.c)) {
                          if (around.isShip) {
                            around.button.setBackground(Color.BLUE);
                          } else {
                            around.button.setBackground(Color.WHITE);
                          }
                        }
                      });
                }
                // deselect
              });
          item.button.addActionListener(
              (x) -> {
                  //test to see if any of the shadowed squares are ships
                  try{
                      boolean anyOfShadowedAreShips = shipLocations(item.r,item.c,rotation,index.get()).stream()
                          .map((cords) -> user.get(cords.r,cords.c))
                          .anyMatch((it) -> it.isShip);
                      if (inShipPlacement.get() && !item.isShip && !anyOfShadowedAreShips) {
                          shipLocations(item.r, item.c, rotation, index.get())
                                  .forEach(
                                          cords -> {
                                              UserShipsSquares i = user.get(cords.r, cords.c);
                                              i.isShip = true;
                                              i.button.setBackground(Color.BLUE);
                                          });
                          item.isShip = true;
                          item.button.setBackground(Color.BLUE);
                          index.getAndIncrement();
                      }
                    }catch(IndexOutOfBoundsException e){
                      //this is thrown if it's out of bounds. the ship can not be placed
                  }

              });
        });
        //noinspection StatementWithEmptyBody
        while(inShipPlacement.get()){
            //block the main thread till everything is done
        }

    }


    static class Rotations{
        static final int UP = 0;
        static final int RIGHT = 1;
        static final int DOWN = 2;
        static final int LEFT = 3;
    }

    private static List<Cords> shipLocations(
            int r,
            int c,
            int rotation,
            int index){
        int shipSize = shipSizes.get(index);
        List<Cords> list = new ArrayList<>();
        int i = 0;
        while(i < shipSize){
            int newR = r;
            int newC = c;
            if(rotation == Rotations.UP){
                newR+= i;
            }
            if(rotation == Rotations.DOWN){
                newR-= i;
            }
            if(rotation == Rotations.RIGHT){
                newC += i;
            }
            if(rotation == Rotations.LEFT){
                newC -= i;
            }
            list.add(new Cords(newR,newC));
            ++i;
        }
        return list;
    }

    interface UnsafeRunnable {void run() throws Throwable;}
    /** print all exceptions but continue anyway */
    private static void unsafe(UnsafeRunnable runner){
        try{
            runner.run();
        }catch(Throwable e){
            e.printStackTrace();
        }
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



