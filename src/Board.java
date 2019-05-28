import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class Board<T extends Square>{
    private static final int SIZE = Main.SIZE;

    private final List<List<T>> graph;
    final JPanel panel;



    Board(SquareCreator<T> creator){
        graph = new ArrayList<>();
        panel = new JPanel();
        GridLayout layout = new GridLayout(SIZE,SIZE);
        layout.setHgap(0);
        layout.setVgap(0);
        panel.setLayout(layout);

        for(int r = 0;r < SIZE;r++){
            List<T> row = new ArrayList<>();
            for(int c = 0;c < SIZE;c++){
                T t = creator.create(r,c);
                panel.add(t.button);
                row.add(t);
            }
            graph.add(row);
        }
    }
    void forEach(Consumer<T> consumer){
        graph.forEach((list) -> list.forEach(consumer));
     }
    void forButton(Consumer<JButton> consumer) { forEach((i) -> consumer.accept(i.button)); }
    T get(int r, int c){
        return graph.get(r).get(c);
    }
}
