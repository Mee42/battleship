import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

class Board{
    private static final int SIZE = Main.SIZE;

    private final List<List<Square>> graph;
    final JPanel panel;

    private final List<Ship> ships;

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
