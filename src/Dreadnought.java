import java.util.*;

class Dreadnought{
    private static Map<String,Dreadnought> games = new HashMap<>();
    public static Dreadnought getInstanceForBoard(Board board){
        if(games.containsKey(board.id)){
            return games.get(board.id);
        }
        games.put(board.id,new Dreadnought(board));
        return games.get(board.id);
    }
    
    private final String id;
    private Dreadnought(Board board){
        this.id = board.id;
    }
    
    private final List<Ship> possibleShips = new ArrayList<>();
    
    public void play(Board board){
      updatePossibleShips(possibleShips);
      if(possibleShips.isEmpty()){
          doRandomPick(board);
      }else{
          doTargetedPick(board);
      }
    }
    private void updatePossibleShips(Board board){
      possibleShips.clear();
      
    }
    
    private void doRandomPick(Board board){
      while(true){
          int r = (int)(Math.random() * Main.SIZE);
          int c = (int)(Math.random() * Main.SIZE);
          Square square = board.get(new Cords(r,c));
          if(!square.hasBeenClicked()){
               square.click();return;
          }
      }
    }
    
    private void doTargetedPick(Board board){
    
    }

}