import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
/**
  * This board is an array of squares, but only allows you to access with the Cords class.
  * It also manages a panel with all of the buttons in the right place, with no distance between them.
  * This board also keeps a refrance to all of the Ships in it, to make checking the ships easier.
  * Streams (jdk8 feature) are used because there are very nice features like map((it) -> f(it)) and filter((it) -> it.equals("hi");
  * 
  * The forEach(Consumer<Square> consumer) method is made to iterate over all of the squares quickly in an external method.
  * This is the same for the forButton(Consumer<Button> consumer) method which just iterates over the JButton objects.
  * 
  */
class Board{
    private static final int SIZE = Main.SIZE;

    private final List<List<Square>> graph;
    final JPanel panel;

    private final List<Ship> ships;
    
    final String id = UUID.randomUUID().toString();

    Board(){
        graph = new ArrayList<>();
        panel = new JPanel();
        ships = new ArrayList<>();
        GridLayout layout = new GridLayout(SIZE,SIZE);
        layout.setHgap(0);
        layout.setVgap(0);
        panel.setLayout(layout);
        panel.setSize(Main.HEIGHT,Main.WIDTH);
        for(int r = 0;r < SIZE;r++){
            List<Square> row = new ArrayList<>();
            for(int c = 0;c < SIZE;c++){
                Square t = new Square(r,c);
                panel.add(t.button);
                row.add(t);
            }
            graph.add(row);
        }
    }

    List<Square> squares(){
        return graph.stream().flatMap(Collection::stream).collect(toList());
    }

    void forEach(Consumer<Square> consumer){
        graph.forEach((list) -> list.forEach(consumer));
     }
    void forButton(Consumer<JButton> consumer) { forEach((i) -> consumer.accept(i.button)); }
    
    // Cords are used as a wrapper over int r and int c to make code cleaner and avoid errors
    Square get(Cords cords){
        return graph.get(cords.r).get(cords.c);
    }

    void addShip(Ship ship){
        ships.add(ship);
    }

    List<Ship> getShips() {
        return ships;
    }
}
