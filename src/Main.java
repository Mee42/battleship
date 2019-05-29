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

public class Main {
  static final int SIZE = 10;
  static final int HEIGHT = 400;
  static final int WIDTH = 400;
  private static final List<Integer> shipSizes = Arrays.asList(5, 4, 3, 3, 2);
  static final int MAX_PLACE_ATTEMPTS = 1000;

  public static void main(String[] args) {
    Board user = new Board();
    user.forButton((button) -> button.setBackground(Color.WHITE));

    user.panel.setSize(WIDTH, HEIGHT);
    JFrame frame = new JFrame();
    checkKeyPress();
    frame.setLayout(new GridLayout(1,1));
//    frame.add(message, BorderLayout.NORTH);


    JPanel panels = new JPanel();

    panels.setLayout(new GridLayout(1,2));
    panels.add(user.panel,BorderLayout.WEST);
    Board enemy = generateEnemyBoard();
    panels.add(enemy.panel,BorderLayout.EAST);


    frame.add(panels,BorderLayout.SOUTH);

    frame.setSize(WIDTH * 2, HEIGHT);//50 for the message
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setTitle("Battleship");
    frame.setVisible(true);


    enemy.forButton(button -> button.setBackground(Color.WHITE));
    // start the ship positioning
    // loop through the ships

    AtomicReference<Rotation> rotation =
        new AtomicReference<>(Rotation.UP); // 0 is up, 1 is right, 2 is down, 3 is right
    AtomicInteger index = new AtomicInteger(0);
    Producer<Boolean> inShipPlacement = () -> index.get() < shipSizes.size();
    AtomicReference<Cords> lastCords = new AtomicReference<>(new Cords(0, 0));

    user.forEach(
        item -> {
          addOnHover(
              item.button,
                  () -> {
                if (inShipPlacement.get()) {
                  // ON HOVER
                  redraw(new Ship(item.pos, rotation.get(), shipSizes.get(index.get())), user);
                  lastCords.set(item.pos);
                }
              },
                  () -> {
                if (inShipPlacement.get()) {
                  // ON UNHOVER
                  redraw(new Ship(item.pos, rotation.get(), shipSizes.get(index.get())), user);
                  lastCords.set(item.pos);
                }
              });
          item.button.addActionListener(
              (x) -> {
                // ON CLICK
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
            rotation.getAndUpdate(Rotation::next);
            System.out.println("Rotation mode: " + rotation.get());
            redraw(new Ship(lastCords.get(), rotation.get(), shipSizes.get(index.get())), user);
          }
        });
    //noinspection StatementWithEmptyBody
    while (inShipPlacement.get()) {
      // block the main thread till everything is done
    }

    System.out.println("User has placed ships");


    ComputerMode aiMode = getAIMode();




      AtomicBoolean playing = new AtomicBoolean(true);

      AtomicReference<Cords> last = new AtomicReference<>(new Cords(0,0));
      enemy.forEach(item -> {
          final Runnable redrawEnemy = () -> { redrawEnemy(item.pos, enemy); last.set(item.pos); };
          addOnHoverConditional(item.button, redrawEnemy, redrawEnemy, playing::get);

          item.button.addActionListener((a) -> {
              if(playing.get()){
                  //if we're still playing, run the thing
                  if(item.hasBeenClicked()){
                      System.out.println("Nope!");
                  }else{
                      item.click();
                      playEnemy(user,aiMode);
                      redrawUser(user);
                      redrawEnemy(last.get(),enemy);
                  }
              }
          });
      });
      System.out.println("User ships:" + Arrays.toString(user.getShips().toArray()));
      System.out.println("Enemy ships:" + Arrays.toString(user.getShips().toArray()));

      while(   !user.getShips().stream().allMatch(Ship::isSunk) &&
              !enemy.getShips().stream().allMatch(Ship::isSunk)) {
//          System.out.println("user.getShips().stream().allMatch(Ship::isSunk)  :  " + user.getShips().stream().allMatch(Ship::isSunk));
//          System.out.println("enemy.getShips().stream().allMatch(Ship::isSunk) :  " + enemy.getShips().stream().allMatch(Ship::isSunk));
        try { Thread.sleep(100); }catch(Exception ignored){}

//          System.out.println("User ships:" + Arrays.toString(user.getShips().toArray()));
//          System.out.println("Enemy ships:" + Arrays.toString(user.getShips().toArray()));

      }

      frame.dispose();

      JFrame youWin = new JFrame();

      youWin.setSize(100, 100);
      youWin.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      youWin.setTitle("Battleship");
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

      //noinspection StatementWithEmptyBody
      while(!repeat.get()){}

      youWin.dispose();

      Main.main(args);
  }

  private static ComputerMode getAIMode(){
      JDialog frame = new JDialog();
      frame.setSize(100, 100);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.setTitle("Battleship");
      frame.setLayout(new FlowLayout());
      frame.setAlwaysOnTop(true);//i3 compatibility
      AtomicReference<ComputerMode> mode = new AtomicReference<>(null);
      for(ComputerMode value : ComputerMode.values()){
          String lowercase = value.toString().toLowerCase();
          String formatted = lowercase.substring(0,1).toUpperCase() + lowercase.substring(1);
          JButton button = new JButton(formatted);
          button.addActionListener(a -> mode.set(value));
          frame.add(button);
      }
      frame.setVisible(true);
      while(mode.get() == null){ }
      frame.dispose();
      return mode.get();
  }

  private static Board generateEnemyBoard() {
    Board enemy = new Board();
    int count = 0;
    for (int size : shipSizes) {
      while (true) {
        int r = (int) (Math.random() * SIZE);
        int c = (int) (Math.random() * SIZE);
        Rotation rotation = Rotation.values()[(int) (Math.random() * Rotation.values().length)];
        System.out.println("Testing " + r + "," + c + " : " + rotation);
        Ship attempt = new Ship(new Cords(r, c), rotation, size);
        if (enemy.getShips().stream().noneMatch((i) -> i.overlaps(attempt))
            && attempt.getSquares().stream().allMatch(Cords::isOnBoard)) {
          // if none of them overlap AND all of them are on the board
          enemy.addShip(attempt);
          attempt.getSquares().stream().map(enemy::get).forEach(i -> i.setShip(attempt));
          break;
        }
        if (count++ > MAX_PLACE_ATTEMPTS) {
          throw new RuntimeException("Can't place ships for enemy - try again");
        }
      }
      count = 0;
    }
    return enemy;
  }

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
    private static void playEnemy(Board user,ComputerMode aiMode){
        aiMode.play(user);
    }

  private static void checkKeyPress() {
    addOnKeyPress(e -> System.out.println("Got key event = " + e.getKeyCode()));
  }

  private static void addOnKeyPress(int keycode, Runnable runner) {
    addOnKeyPress(
        e -> {
          if (e.getKeyCode() == keycode) {
            runner.run();
          }
        });
  }

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

  private static void addOnHover(
      JButton button,Runnable consumer,Runnable elsee) {
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
