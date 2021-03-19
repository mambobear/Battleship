package battleship;

// import java.security.PublicKey;
// import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Game game = new Game();
        game.init();
        game.play();
    }
}

class Game {

    Scanner scanner = new Scanner(System.in);

    Battlefield battlefield = new Battlefield();


    public void init() {
        ShipClass[] shipsClasses = new ShipClass[5];
        shipsClasses[0] = new ShipClass("Aircraft Carrier", 5);
        shipsClasses[1] = new ShipClass("Battleship", 4);
        shipsClasses[2] = new ShipClass("Submarine", 3);
        shipsClasses[3] = new ShipClass("Cruiser", 3);
        shipsClasses[4] = new ShipClass("Destroyer", 2);

        Scanner scanner = new Scanner(System.in);

        System.out.println(battlefield.asString(true));

        for (int i = 0; i < 5; i++) {
            ShipClass shipClass = shipsClasses[i];
            String prompt = String.format("\nEnter the coordinates the %s (%d cells)\n",
                    shipClass.getType(), shipClass.getLength());
            while (true) {
                System.out.println(prompt);
                String headStr = scanner.next();
                String tailStr = scanner.next();
                try {
                    Ship ship = new Ship(headStr, tailStr, shipClass);
                    battlefield.addShip(ship);
                    System.out.println();
                    System.out.println(battlefield.asString(false));
                    break;
                } catch (IllegalArgumentException e) {
                    prompt = "\n" + e.getMessage();
                }
            }
        }
    }

    public void play() {
        System.out.println("\nThe game starts!\n");
        System.out.println(battlefield.asString(true));

        System.out.println("\nTake a shot!\n");
        String str;
        // TODO: move convert method to Battlefield
        Coordinate shot = null;
        while (true) {
            str = scanner.next();
            try {
                shot = Ship.convertToFieldCoordinates(str);
                boolean hit = battlefield.shoot(shot);
                System.out.println(battlefield.asString(true));
                System.out.println();
                if (hit) {
                    System.out.println("You hit a ship!\n");
                } else {
                    System.out.println("You missed!\n");
                }
                System.out.println(battlefield.asString(false));
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Error! You entered the wrong coordinates! Try again:\n");
            }
        }
    }
}

class BattlefieldCell {
    enum CellStatus {
        UNKNOWN, SHIP, HIT, MISS
    }

    private Ship ship;

    private CellStatus status = CellStatus.UNKNOWN;

    public boolean isBorder() {
        return border;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    private boolean border = false;

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
        this.status = CellStatus.SHIP;
    }

    public CellStatus getStatus() {
        return status;
    }

    public void setStatus(CellStatus status) {
        this.status = status;
    }

    public String toString() {
        String result = "";
        switch (status) {
            case UNKNOWN:
                return "~";
            case SHIP:
                return "O";
            case HIT:
                return "X";
            case MISS:
                return "M";
            default:
                return "";
        }
    }
}

class Battlefield {

    private final int SIZE = 10;
    private final BattlefieldCell[][] field = new BattlefieldCell[SIZE][SIZE];
    //private final ArrayList<Ship> ships = new ArrayList<>();

    Battlefield() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                field[row][col] = new BattlefieldCell();
            }
        }
    }

    void addShip(Ship ship) {
        // TODO: check for duplicates
        //ships.add(ship);
        place(ship);
    }

    private void place(Ship ship) {

        Coordinate[] shipCoords = ship.getCoordinates();

        if (!canPlaceShip(shipCoords)) {
            throw new IllegalArgumentException("Error! You placed it too close to another one. Try again:\n");
        }

        for (Coordinate coord : shipCoords) {
            int row = coord.getRow();
            int col = coord.getCol();
            field[row][col].setShip(ship);
        }

        setShipBorders(shipCoords);
    }

    private void setShipBorders(Coordinate[] shipCoords) {
        for (Coordinate coord : shipCoords) {
            Coordinate[] neighbors = coord.getNeighbors();
            for (Coordinate neighbor : neighbors) {
                int row = neighbor.getRow();
                int col = neighbor.getCol();
                if (0 <= row && row < SIZE && 0 <= col && col < SIZE) {
                    BattlefieldCell cell = field[row][col];
                    if (cell.getStatus() != BattlefieldCell.CellStatus.SHIP) {
                        cell.setBorder(true);
                    }
                }
            }
        }
    }

    private boolean canPlaceShip(Coordinate[] shipCoords) {
        for (Coordinate coord : shipCoords) {
            int row = coord.getRow();
            int col = coord.getCol();
            if (field[row][col].isBorder()) {
                return false;
            }
        }
        return true;
    }

    public String asString(boolean hidden) {
        StringBuilder stringBuilder = new StringBuilder();

        // build header
        stringBuilder.append("  ");
        for (int i = 1; i <= SIZE; i++) {
            stringBuilder.append(i);
            if (i != SIZE) {
                stringBuilder.append(" ");
            } else {
                stringBuilder.append('\n');
            }
        }

        // main part
        for (int row = 0; row < SIZE; row++) {
            stringBuilder.append((char) ('A' + row));
            stringBuilder.append(" ");
            for (int col = 0; col < SIZE; col++) {
                char ch = ' ';
                BattlefieldCell cell = field[row][col];
                if (hidden && cell.getStatus() == BattlefieldCell.CellStatus.SHIP) {
                    stringBuilder.append('~');
                } else {
                    stringBuilder.append(field[row][col]);
                }
                if (col != SIZE - 1) {
                    stringBuilder.append(' ');
                }
            }
            if (row != SIZE - 1) {
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }

    public boolean shoot(Coordinate shot) {
        BattlefieldCell cell = field[shot.getRow()][shot.getCol()];
        if (cell.getStatus() == BattlefieldCell.CellStatus.SHIP) {
            cell.setStatus(BattlefieldCell.CellStatus.HIT);
            return true;
        } else {
            cell.setStatus(BattlefieldCell.CellStatus.MISS);
            return false;
        }
    }
}

class Ship {

    enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    private final int SIZE = 10;

    // private final String type;

    private Coordinate head;
    private final int length;
    private final Orientation orientation;

    Ship(String start, String finish, ShipClass shipClass) {
        head = convertToFieldCoordinates(start);
        Coordinate tail = convertToFieldCoordinates(finish);

        if (!isValid(head, tail)) {
            throw new IllegalArgumentException("Error! Wrong ship location! Try again:\n");
        }

        if (!(head.getRow() <= tail.getRow() && head.getCol() <= tail.getCol())) {
            Coordinate temp = head;
            head = tail;
            tail = temp;
        }

        // Set orientation
        orientation = head.getRow() == tail.getRow() ? Orientation.HORIZONTAL : Orientation.VERTICAL;

        // Set length
        if (orientation == Orientation.HORIZONTAL) {
            this.length = tail.getCol() - head.getCol() + 1;
        } else {
            this.length = tail.getRow() - head.getRow() + 1;
        }

        if (this.length != shipClass.getLength()) {
            throw new IllegalArgumentException(String.format("Error! Wrong length of the %s! Try again:\n",
                    shipClass.getType()));
        }

        if (length == 0 || length > SIZE) {
            throw new IllegalArgumentException("Invalid ship length");
        }
    }

    public Coordinate getHead() {
        return head;
    }

    public int getLength() {
        return length;
    }

    public Coordinate[] getCoordinates() {
        Coordinate[] coords = new Coordinate[length];
        int row = head.getRow();
        int col = head.getCol();
        if (orientation == Orientation.HORIZONTAL) {
            for (int i = 0; i < length; i++) {
                coords[i] = new Coordinate(row, col + i);
            }
        } else {
            for (int i = 0; i < length; i++) {
                coords[i] = new Coordinate(row + i, col);
            }
        }
        return coords;
    }

    static boolean isValid(Coordinate head, Coordinate tail) {
        return (head.getRow() == tail.getRow() || head.getCol() == tail.getCol());
    }

    static Coordinate convertToFieldCoordinates(String str) {

        if (str.length() < 2) {
            throw new IllegalArgumentException("Invalid ship coordinates");
        }

        char rowChar = str.charAt(0);
        if (rowChar < 'A' || rowChar > 'J') {
            throw new IllegalArgumentException("Invalid ship coordinates");
        }

        int row = rowChar - 'A';

        int col = Integer.parseInt(str.substring(1));
        if (col < 1 || col > 10) {
            throw new IllegalArgumentException("Invalid ship coordinates");
        }
        col -= 1;

        return new Coordinate(row, col);
    }

    public String toString() {
        return String.format("head = (%d, %d), length = %d\n", head.getRow(), head.getCol(), length);
    }
}

class Coordinate {

    private final int row;
    private final int col;

    Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    Coordinate[] getNeighbors() {
        Coordinate[] neighbors = new Coordinate[4];
        neighbors[0] = new Coordinate(row, col - 1);
        neighbors[1] = new Coordinate(row - 1, col);
        neighbors[2] = new Coordinate(row, col + 1);
        neighbors[3] = new Coordinate(row + 1, col);
        return neighbors;
    }
}

class ShipClass {


    private final String type;
    private final int length;

    ShipClass(String type, int length) {
        this.type = type;
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }
}