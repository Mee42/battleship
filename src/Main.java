import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
/*
 * Author: Carson Graham
 * Date: 2019-5-29
 * Notes:
 * There is aprox. 600 lines in this project.
 * 
 * Everything in this class is static. Everything
 * THere are no Instances of main
 * There is nothing static stored in Main
 * Everything in contained within the main method and branches off into different methods.
 * THe main method properly disposes of JFrames and other objects, so when the Main method exits there is no more memory being
 * Used by it. This lets me call the main method FROM the main method, and not have any bad issues. It is stateless, so when the method
 * restarts the state is repleaced.
 *  
 * 
 */
public class Main {
  static final int SIZE = 10;// This is the size of the board, in buttons. Square it to get the total count of buttons
  static final int HEIGHT = 400;// this is the height of each board
  static final int WIDTH = 400;// width, see HEIGHT
  private static final List<Integer> shipSizes = Arrays.asList(5, 4, 3, 3, 2);//THis is the size of each ship, in order.
  static final int MAX_PLACE_ATTEMPTS = 1000;//THis is a generic value to be used as an upper limit in brute-forcing alogrithms
                                             //  such as placing the enemy ships or the ComputerMode.RANDOM
                                      

  public static void main(String[] args) {
    Board user = new Board();//this is the board that the user's buttons go on
    user.forButton((button) -> button.setBackground(Color.WHITE));//set all buttons to white

    user.panel.setSize(WIDTH, HEIGHT);//shouldn't be needed - TODO check to see if this is in the Board constructor
    JFrame frame = new JFrame();//create the master frame
    checkKeyPress();
    frame.setLayout(new GridLayout(1,1));
//    frame.add(message, BorderLayout.NORTH);


    JPanel panels = new JPanel();

    panels.setLayout(new GridLayout(1,2));
    panels.add(user.panel,BorderLayout.WEST);
    
    Board enemy = generateEnemyBoard();//generate the enemy board with ships
    panels.add(enemy.panel,BorderLayout.EAST);//and add it to the frame


    frame.add(panels,BorderLayout.SOUTH);

    frame.setSize(WIDTH * 2, HEIGHT);//* 2 because there are two windows
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//should be DISPOSE_ON_CLOSE, but I already dispose.
    frame.setTitle("Battleship");//NEEDED FOR i3 SUPPORT
    frame.setVisible(true);


    enemy.forButton(button -> button.setBackground(Color.WHITE));//oh and set all the enemy buttons to white
    
    /*
    In this part of the program I use classes like AtomicReference<T>, AtomicInteger, AtomicBoolean etc.
    Because there are many different threads accessing these variables, like keypress events and click events and such,
    There needs to be thread safety.
    AtomicRefrence<T> gives thread-save methods for accessing and mutating T.
    
    
    */
    AtomicReference<Rotation> rotation =
        new AtomicReference<>(Rotation.UP); //ROtation is an enum - better code!
    // THis is the index of in the shipSize array.  
    AtomicInteger index = new AtomicInteger(0);
    // This is a dynamic producer - it will rerun every time it's called
    // it lets be call inShipPlacement.get() instead of index.get() < shipSizes.size() every single time.
    Producer<Boolean> inShipPlacement = () -> index.get() < shipSizes.size();
    
    // when the  `r` key is pressed, we need the place the mouse is hovering. It's set here every time.
    AtomicReference<Cords> lastCords = new AtomicReference<>(new Cords(0, 0));

    user.forEach(
        item -> {
          addOnHover(
              item.button,
                  () -> {
                if (inShipPlacement.get()) {
                  // WILL RUN ON HOVER IF IN SHIP PLACEMENT MODE
                  redraw(new Ship(item.pos, rotation.get(), shipSizes.get(index.get())), user);
                  lastCords.set(item.pos);
                }
              },
                  () -> {
                if (inShipPlacement.get()) {
                  // WILL RUN ON DEHOVER IF IN SHIP PLACEMENT MODE
                  redraw(new Ship(item.pos, rotation.get(), shipSizes.get(index.get())), user);
                  lastCords.set(item.pos);
                }
              });
          item.button.addActionListener(
              (x) -> {
                // ON CLICK IF IN SHIP PLACEMENT MODE
                if (inShipPlacement.get()) {
                  Ship on = new Ship(item.pos, rotation.get(), shipSizes.get(index.get()));
                  if (on.getSquares().stream().allMatch(Cords::isOnBoard)) {
                    // if all of them are on the board
                    List<Square> squares =
                        on.getSquares().stream().map(user::get).collect(toList());
                    if (squares.stream().noneMatch(Square::isShip)) {
                      // if none are ships or off the board, apply
                      squares.forEach(it -> it.setShip(on).setColor(Color.BLUE));
                      index.getAndIncrement();
                      user.addShip(on);
                    }
                  }
                }
              });
        });

    // add listener to change rotation
    addOnKeyPress(
        82,
        () -> {
          if (inShipPlacement.get()) {
            rotation.getAndUpdate(Rotation::next);//THREAD-SAFE method to get the rotation, change it to the next one, and reassign it.
            System.out.println("Rotation mode: " + rotation.get());
            redraw(new Ship(lastCords.get(), rotation.get(), shipSizes.get(index.get())), user);//use the last cords to redraw the screen.
          }
        });
    //noinspection StatementWithEmptyBody
    while (inShipPlacement.get()) {
      // block the main thread till everything is done.
    }

    System.out.println("User has placed ships");


    ComputerMode aiMode = getAIMode();




      AtomicBoolean playing = new AtomicBoolean(true);

      AtomicReference<Cords> last = new AtomicReference<>(new Cords(0,0));
      
      enemy.forEach(item -> {
          /** Redraw the enemy on hover - the square you hover over will be a shade darker then normal for tactile feedback*/
          final Runnable redrawEnemy = () -> { redrawEnemy(item.pos, enemy); last.set(item.pos); };
          addOnHoverConditional(item.button, redrawEnemy, redrawEnemy, playing::get);

          item.button.addActionListener((a) -> {
              if(playing.get()){
                  //if we're still playing, run the thing
                  if(item.hasBeenClicked()){
                      System.out.println("Nope! - can't click there");
                  }else{
                      item.click();//click the square. This toggles an internal boolean accessed by hasBeenCLicked()
                      playEnemy(user,aiMode);//runs the enemy after the user plays
                      redrawUser(user);//redraws the user, as the enemy just changed the board
                      redrawEnemy(last.get(),enemy);//redraw the enemy for consistency
                  }
              }
          });
      });
      System.out.println("User ships:" + Arrays.toString(user.getShips().toArray()));
      System.out.println("Enemy ships:" + Arrays.toString(user.getShips().toArray()));

      while(   !user.getShips().stream().allMatch(Ship::isSunk) &&
              !enemy.getShips().stream().allMatch(Ship::isSunk)) {
        try { Thread.sleep(100); }catch(Exception ignored){}
        //wait until either all of the user's ships can been sunk or all of the enemies ships have been sunk
      }
      playing.set(false);
      System.out.println("DONE");
     
      try { Thread.sleep(2000); }catch(Exception ignored){}

      frame.dispose();//close the frame and release the memory

      //start the youWin frame.
      JFrame youWin = new JFrame();

      youWin.setSize(100, 100);
      youWin.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      youWin.setTitle("Battleship");//i3 support
      youWin.setLayout(new FlowLayout());
      youWin.add(new JTextArea(user.getShips().stream().allMatch(Ship::isSunk) ? "You lost!" : "You won!"));

      AtomicBoolean repeat = new AtomicBoolean(false);
      JButton goAgain = new JButton("Retry");
      goAgain.addActionListener(a -> repeat.set(true));
      youWin.add(goAgain);

      JButton exit = new JButton("exit");
      exit.addActionListener(a -> System.exit(0));
      youWin.add(exit);
      youWin.setVisible(true);

      while(!repeat.get()){}//wait till the repeat button is called

      youWin.dispose();//close the youWIn frame

      Main.main(args);//call main again. If the exit button is pressed the entire JVM shuts down and this is not called.
      // see the statless-static part of the class description for why this is okay
  }

  /** This is a method that opens a new JFrame to ask the user what AI mode they want to use */
  private static ComputerMode getAIMode(){
      JDialog frame = new JDialog();
      frame.setSize(500, 500);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.setTitle("Battleship");
      frame.setLayout(new FlowLayout());
      frame.setAlwaysOnTop(true);//i3 compatibility
      AtomicReference<ComputerMode> mode = new AtomicReference<>(null);
      //add all values
      for(ComputerMode value : ComputerMode.values()){
          String lowercase = value.toString().toLowerCase();
          String formatted = lowercase.substring(0,1).toUpperCase() + lowercase.substring(1);
          JButton button = new JButton(formatted);
          button.addActionListener(a -> mode.set(value));
          frame.add(button);
      }
      frame.setVisible(true);
      while(mode.get() == null){
         //the mode will be set when a button is clicked, so wait till it's set before returning.
      }
      frame.dispose();
      return mode.get();
  }

  private static Board generateEnemyBoard() {
    Board enemy = new Board();
    int count = 0;
    for (int size : shipSizes) {//for each of the ships
      while (true) {//try until you can place one.
        int r = (int) (Math.random() * SIZE);//generate random cords
        int c = (int) (Math.random() * SIZE);
        Rotation rotation = Rotation.values()[(int) (Math.random() * Rotation.values().length)];
        System.out.println("Testing " + r + "," + c + " : " + rotation);
        
        Ship attempt = new Ship(new Cords(r, c), rotation, size);
        if (enemy.getShips().stream().noneMatch((i) -> i.overlaps(attempt))
            && attempt.getSquares().stream().allMatch(Cords::isOnBoard)) {
          // if none of them overlap AND all of the squares are on the board
          enemy.addShip(attempt);
          attempt.getSquares().stream().map(enemy::get).forEach(i -> i.setShip(attempt));//set the squares's parent to the ship
          break;
        }
        if (count++ > MAX_PLACE_ATTEMPTS) {// if it goes passed MAX_PLACE_ATTEMPTS, something is probably wrong. Crash.
          throw new RuntimeException("Can't place ships for enemy - try again");
        }
      }
      count = 0;
    }
    return enemy;
  }

  /** 
    * This is for the placing-ships part of the game 
    * It sets where the ship might be in gray as a visual
    * and it sets where the visual overlaps with the other ships in RED
    * because it is invalid to place there.
  */
  private static void redraw(Ship on, Board board) {
    List<Cords> squaresOn = on.getSquares();
    board.forEach(
        square -> {
          if (square.isShip() && squaresOn.contains(square.pos)) {
            square.setColor(Color.RED);
          } else if (square.isShip()) {
            square.setColor(Color.BLUE);
          } else if (squaresOn.contains(square.pos)) {
            square.setColor(Color.GRAY);
          } else {
            square.setColor(Color.WHITE);
          }
        });
  }
    /**
      * THIs redraws the enemy board during the game
      * it slightly darkens squares that the mouse curser is over
      *
      */
    private static void redrawEnemy(Cords hover, Board enemy){
      enemy.forEach(square -> {
          if(square.isShip() && square.hasBeenClicked()){
              //hit!
              square.setColor(square.pos.equals(hover) ? Color.RED.darker() : Color.RED);
          }else if (square.hasBeenClicked()){
              //miss!
              square.setColor(square.pos.equals(hover) ? Color.GREEN.darker() : Color.GREEN);
          }else if(square.pos.equals(hover)){
              square.setColor(Color.GRAY);
          } else {
              // nothing yet
              square.setColor(Color.WHITE);
          }
      });
    }
    /**
      * This redraws the user board during the game
      * BLUE is where the ships are, RED is hits, GREEN is miss
      */
    private static void redrawUser(Board user){
      user.forEach(square -> {
          if(square.hasBeenClicked() && square.isShip()){
              square.setColor(Color.RED);
          }else if(square.isShip()){
              square.setColor(Color.BLUE);
          }else if(square.hasBeenClicked()){
              square.setColor(Color.GREEN);
          }else{
              square.setColor(Color.WHITE);
          }
      });
    }
    
    // just calls the aimode.play, but is in a seperate method in case it needs expanding
    private static void playEnemy(Board user,ComputerMode aiMode){
        aiMode.play(user);
    }

  /** This is a method that will print all key events, so it's easy to see what the keycode of a key is */
  private static void checkKeyPress() {
    addOnKeyPress(e -> System.out.println("Got key event = " + e.getKeyCode()));
  }
  /** THis will run the runner when a keypress with a specific keycode is pressed */
  private static void addOnKeyPress(int keycode, Runnable runner) {
    addOnKeyPress(
        e -> {
          if (e.getKeyCode() == keycode) {
            runner.run();
          }
        });
  }
  /** This is a generic keypresslistener that only accepts KEY_PRESSED events. 
    * Because there is also KEY_RELEASED, this is needed to prevent duplicates
    */
  private static void addOnKeyPress(Consumer<KeyEvent> consumer) {
    KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher(
            e -> {
              if (e.getID() == KeyEvent.KEY_PRESSED) {
                consumer.accept(e);
              }
              return false;
            });
  }
  /**
    * the consumer is run when the mouse hovers over the button.
    * the elsee runnable is run when the mouse unhovers over the button.
    * Sources: stackoverflow.com
    */
  private static void addOnHover(
      JButton button,
      Runnable consumer,
      Runnable elsee) {
    button
        .getModel()
        .addChangeListener(
            evt -> {
              ButtonModel model = (ButtonModel) evt.getSource();
              if (model.isRollover()) {
                consumer.run();
              } else {
                elsee.run();
              }
            });
  }
  
  /**
    * Adds a onHover command, but will only run when the conditional is true */
  private static void addOnHoverConditional(JButton button, Runnable consumer, Runnable elsee, Producer<Boolean> conditional){
      addOnHover(button,() -> {
          if(conditional.get())
              consumer.run();
      }, () -> {
          if(conditional.get())
              consumer.run();
      });
  }
}
