
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
/**
  * This is the class that defines the different AI modes the user can select.
  * Adding a constant here wil automaticly add the button to the selection menu, 
  * though the selection menu may need to be resized.
  */
enum ComputerMode {
   //loop through and find the next non-clicked square and click it.
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
   // randomly select a square on the board. Hope it doesn't go over MAX_PLACE_ATTEMPTS
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
    //go in a checkerboard pattern to increase coverage
    //ironicly,, this is actualy much less efficent then ITERATIVE,
    // requiring (SIZE^2)/2 squares to be clicked before ships can start being sunk
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
    }),
    
    //Checkerboards, but it will be smarter when it encounters a ship
    // and it will complete the ship
    SMART_CHECKERS(board -> {
        for(int r = 0;r<Main.SIZE;r++){
          for(int c = 0;c<Main.SIZE;c++){
            Square square = board.get(new Cords(r,c));
            if(square.hasBeenClicked()){
               if(square.isShip()){
                 Cords[] offsets = { new Cords(r+1,c),new Cords(r-1,c),new Cords(r,c+1), new Cords(r,c-1) };
                 for(Cords around : offsets){
                   if(!around.isOnBoard()){
                     continue;//if it's not on the board it's not an option
                   }
                   Square squareAround = board.get(around);
                   if(!squareAround.hasBeenClicked()){
                      squareAround.click();
                      return;
                   }
                 }
               }
            }
          }
        }
        ComputerMode.CHECKERS.play(board);//just run the checkers mode if there are no exposed hits.
    }),
    
    HACKED(board -> {
        for(Ship ship : board.getShips()){
            for(Square square : ship.getSquares()
                .stream()
                .map(board::get)
                .collect(Collectors.toList())){
                  if(!square.hasBeenClicked()){
                    square.click();
                    return;
                  }
                }
        }
    }),
    
    DREADNOUGHT(board -> {
      Dreadnought dread = Dreadnought.getInstanceForBoard(board);
      dread.play(board);
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
