class EnemyShips extends Square {
    boolean isShip;
    boolean hasBeenSearched;
    EnemyShips(int r,int c){
        super(r,c);
        isShip = false;
        hasBeenSearched = false;
    }
}
