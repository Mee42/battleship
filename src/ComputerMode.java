
import java.util.*;

import static java.util.stream.Collectors.toList;

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
        //major diagonal
        for(int index = 0;index<Main.SIZE;index++){
            Square square = board.get(new Cords(index,index));
            if(!square.hasBeenClicked()){
                square.click();return;
            }
        }
        //minor diagonal
        for(int index = 1;index<Main.SIZE;index++){
            Square square = board.get(new Cords(index - 1,Main.SIZE - 1 - index));
            if(!square.hasBeenClicked()){
                square.click();return;
            }
        }

        //checkerboards
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
                .collect(toList())){
                  if(!square.hasBeenClicked()){
                    square.click();
                    return;
                  }
                }
        }
    }),

    DREADNOUGHT(board -> {




        List<Square> clicked = board.squares()
                .stream()
                .filter(Square::hasBeenClicked)
                .collect(toList());

        List<Square> unsunkShipSquares = clicked
                .stream()
                .filter(Square::isShip)
//                .filter(square -> !square.getShip().isSunk())
                .collect(toList());

        if(clicked.isEmpty()){
            System.out.println("RANDOM - 0");
            SMART_CHECKERS.play(board);
            return;
        }
        List<Integer> inverse = clicked
                .stream()
                .filter(Square::isShip)
                .map(Square::getShip)
                .filter(Ship::isSunk)
                .map(Ship::getLength)
                .distinct()
                .collect(toList());

        List<Integer> shipSizes = new ArrayList<>(Main.shipSizes);
        for(int notThisSize : inverse){
            if(shipSizes.contains(notThisSize)){
                // sketch sintax to use the correct remove method
                // T = Integer
                // - remove(int i)
                // - remove(T t)
                shipSizes.remove((Integer) notThisSize);//ONLY REMOVE ONE, THERE ARE DUPLICATES
            }
        }

        if(shipSizes.isEmpty()){
            System.out.println("RANDOM - 2");
            SMART_CHECKERS.play(board);
            return;
        }

        //generate all possible ships
        List<Square> allowedCords = unsunkShipSquares.stream()
            .flatMap(square -> Arrays.stream(Rotation.values()).flatMap(rot -> shipSizes.stream().map(size -> new Ship(square.pos,rot,size)))//generate all posible
            .filter(ship -> ship.getSquares().stream().allMatch(Cords::isOnBoard)))//all on the board
            .filter(ship -> ship.getSquares().stream().map(board::get).anyMatch((it) -> !it.hasBeenClicked()))//at least one unclicked
            .filter(ship -> ship.getSquares().stream().map(board::get).noneMatch(it -> it.hasBeenClicked() && !it.isShip()))
            .peek(System.out::println)
            .flatMap(ship -> ship.getSquares().stream())
            .map(board::get)
            .filter(square -> !square.hasBeenClicked())
            .collect(toList());

        System.out.print("AllowedCords: ");
        for(Square s : allowedCords){
            System.out.print("[" + s.r() + "," + s.c() + "]");
        }
        System.out.println();
        List<Square> cordsSortedByFrequency = allowedCords
                .stream()
                .sorted(Comparator.comparing(a -> allowedCords.stream().filter(b -> b.equals(a)).count()))
                .peek(a -> System.out.println(a.pos + ":" + allowedCords.stream().filter(b -> b.equals(a)).count()))
                .collect(toList());

        //pick the first one
        if(cordsSortedByFrequency.isEmpty()){
            SMART_CHECKERS.play(board);
            System.out.println("RANDOM - 3");
            return;
        }
        cordsSortedByFrequency.get(cordsSortedByFrequency.size() - 1).click();
        System.out.println("DREADNOUGHT!");
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
