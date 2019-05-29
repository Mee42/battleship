import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Ship {
  private final Cords pos;
  private final Rotation rotation;
  private final int length;

  private List<Cords> hits;

  public Ship(Cords pos, Rotation rotation, int length) {
    this.pos = pos;
    this.rotation = rotation;
    this.length = length;
    hits =  new ArrayList<>();
  }

  public Cords getPos() {
    return pos;
  }

  public Rotation getRotation() {
    return rotation;
  }

  public int getLength() {
    return length;
  }

  boolean overlaps(Ship other){
      List<Cords> mine = getSquares();
      return other.getSquares()
              .stream()
              .anyMatch(mine::contains);
  }

  List<Cords> overlappingSquares(Ship other){
      List<Cords> mine = getSquares();
      return other.getSquares()
              .stream()
              .filter(mine::contains)
              .collect(Collectors.toList());
  }

  void hit(Cords cords){
      System.out.println("Hit! " + cords);
      if(!getSquares().contains(cords)){
          throw new RuntimeException("Can not hit ship where it does not exist");
      }
      if(hits.contains(cords)){
          throw new RuntimeException("Can not hit ship where it has already been hit");
      }
      hits.add(cords);
  }

    public List<Cords> getHits() {
        return hits;
    }
    boolean isSunk(){

      return hits.size() == length;//when the size of the array is equal the the size of the ship there are no more left to hit
    }

    List<Cords> getSquares(){
      List<Cords> squares = new ArrayList<>();
      int index = 0;
      while(index < length){
          int newR = pos.r;
          int newC = pos.c;
          switch(rotation){
              case UP:
                  newR -= index;
                  break;

              case DOWN:
                  newR += index;
                  break;
              case LEFT:
                  newC -= index;
                  break;
              case RIGHT:
                  newC += index;
                  break;
          }
          squares.add(new Cords(newR,newC));
          index++;
      }
      return squares;
  }

    @Override
    public String toString() {
        return "Ship{" +
                "pos=" + pos +
                ", rotation=" + rotation +
                ", length=" + length +
                ", hits=" + hits +
                '}';
    }
}
