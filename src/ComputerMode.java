
import java.util.Arrays;
import java.util.List;

enum ComputerMode {
    ITERATIVE(board -> {
        for(int r = 0;r<Main.SIZE;r++){
            for(int c = 0;c<Main.SIZE;c++){
                Square square = board.get(new Cords(r, c));
                if (!square.hasBeenClicked()) {
                    square.click();
                    return;
                }
            }
        }
    }),

    RANDOM(board -> {
        int i = 0;
        while(i++ < Main.MAX_PLACE_ATTEMPTS){
            int r = (int)(Math.random() * Main.SIZE);
            int c = (int)(Math.random() * Main.SIZE);
            Square square = board.get(new Cords(r,c));
            if (!square.hasBeenClicked()) {
                square.click();
                return;
            }
        }
        throw new RuntimeException("hit MAX_PLACE_ATTEMPTS when trying to play as random");
    }),

    CHECKERS(board -> {
       for(int i = 0;i<=1;i++){
         for(int r = 0;r<Main.SIZE;r++){
           for(int c = (r + i) % 2;c<Main.SIZE;c+=2){
             Square square = board.get(new Cords(r,c));
             if(!square.hasBeenClicked()){
               square.click();return;
             }
           }
         }
       }
    })

    ;



    interface Runner{ void play(Board board); }
    private final Runner runner;
    ComputerMode(Runner runner) {
        this.runner = runner;
    }

    void play(Board board){
        runner.play(board);
    }
}
