class UserShipsSquares extends Square{
    boolean isShip;
    boolean hasBeenSearched;
    UserShipsSquares(int r,int c){
        super(r,c);
        isShip = false;
        hasBeenSearched = false;
    }
}
