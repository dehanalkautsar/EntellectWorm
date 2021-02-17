package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;
    private MyPlayer myPlayer;
    private int round;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
        this.myPlayer = gameState.myPlayer;
        this.round = gameState.currentRound;
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    public Command run() {

        Worm enemyWorm = getFirstWormInRange();
        Position enemyWormBomb = getFirstWormInRangeToBomb(5); //assume : range of banana and snowball is th same

        Worm[] listOfPlayerWorms = gameState.myPlayer.worms;
        int id = 0;

        if (currentWorm.freeze_round > 0 && myPlayer.count_token > 0) {
            int i;
            for (i = 0; i<listOfPlayerWorms.length; i++) {
                if (listOfPlayerWorms[i].freeze_round == 0 && listOfPlayerWorms[i].health > 0) {
                    id = listOfPlayerWorms[i].id;
                    break;
                }
            }
            if (id != 0) {

                //resolve direction for new worm

                List<List<Cell>> directionLinesSelect = new ArrayList<>();
                boolean friendlyFire = false;
                for (Direction direction : Direction.values()) {
                    List<Cell> directionLine = new ArrayList<>();
                    for (int directionMultiplier = 1; directionMultiplier <= 4 && !friendlyFire; directionMultiplier++) {

                        int coordinateX = listOfPlayerWorms[i].position.x + (directionMultiplier * direction.x);
                        int coordinateY = listOfPlayerWorms[i].position.y + (directionMultiplier * direction.y);

                        if (!isValidCoordinate(coordinateX, coordinateY)) {
                            break;
                        }

                        if (euclideanDistance(listOfPlayerWorms[i].position.x, listOfPlayerWorms[i].position.y, coordinateX, coordinateY) > 4) {
                            break;
                        }

                        Worm[] listOfPlayerWormsA = gameState.myPlayer.worms;
                        for (int j = 0; j < listOfPlayerWormsA.length; j++) {
                            if (coordinateX == listOfPlayerWormsA[j].position.x && coordinateY == listOfPlayerWormsA[j].position.y) {
                                friendlyFire = true;
                                break;
                            }
                        }
                        if (friendlyFire) {
                            break;
                        }

                        Cell cell = gameState.map[coordinateY][coordinateX];
                        if (cell.type != CellType.AIR) {
                            break;
                        }

                        directionLine.add(cell);
                    }
                    directionLinesSelect.add(directionLine);
                }

                Set<String> cellsSelect = directionLinesSelect
                        .stream()
                        .flatMap(Collection::stream)
                        .map(cell -> String.format("%d_%d", cell.x, cell.y))
                        .collect(Collectors.toSet());

                Worm enemyWormSelectToShoot = null;
                for (Worm enemyWormSelect : opponent.worms) {
                    String enemyPosition = String.format("%d_%d", enemyWormSelect.position.x, enemyWormSelect.position.y);
                    if (cellsSelect.contains(enemyPosition) && enemyWormSelect.health > 0) {
                        enemyWormSelectToShoot = enemyWormSelect;
                    }
                }

                //end of prototype

                if (id == 1) {
                    if (enemyWormSelectToShoot != null) {
                        Direction direction = resolveDirection(listOfPlayerWorms[i].position, enemyWormSelectToShoot.position);
                        return new SelectCommand(id,"shoot "+direction.name());
                    }

                    if (listOfPlayerWorms[i].position.x >= 15 && listOfPlayerWorms[i].position.x <= 18 && listOfPlayerWorms[i].position.y >= 15 && listOfPlayerWorms[i].position.y <= 18) {
                        return new DoNothingCommand();
                    }

                    if ((listOfPlayerWorms[i].position.x == 14 && listOfPlayerWorms[i].position.y == 14) || (listOfPlayerWorms[i].position.x == 17 && listOfPlayerWorms[i].position.y == 19)) {
                        return new DoNothingCommand();
                    }

                    List<Cell> surroundingBlocks = getSurroundingCells(listOfPlayerWorms[i].position.x, listOfPlayerWorms[i].position.y);
                    int cellIdx = 2; //random.nextInt(surroundingBlocks.size());
                    if (listOfPlayerWorms[i].position.y > 17) {
                        cellIdx = 5;
                    }

                    Cell block = surroundingBlocks.get(cellIdx);
                    if (block.type == CellType.AIR) {
                        String X = String.valueOf(block.x);
                        String Y = String.valueOf(block.y);
                        return new SelectCommand(id,"move "+X+" "+Y);
                    } else if (block.type == CellType.DIRT) {
                        String X = String.valueOf(block.x);
                        String Y = String.valueOf(block.y);
                        return new SelectCommand(id,"dig "+X+" "+Y);
                    }
                }

                if (id == 2) {
                    if (enemyWormSelectToShoot != null) {
                        Direction direction = resolveDirection(listOfPlayerWorms[i].position, enemyWormSelectToShoot.position);
                        return new SelectCommand(id,"shoot "+direction.name());
                    }

                    if (listOfPlayerWorms[i].position.x >= 15 && listOfPlayerWorms[i].position.x <= 18 && listOfPlayerWorms[i].position.y >= 15 && listOfPlayerWorms[i].position.y <= 18) {
                        return new DoNothingCommand();
                    }

                    if ((listOfPlayerWorms[i].position.x == 14 && listOfPlayerWorms[i].position.y == 18) || (listOfPlayerWorms[i].position.x == 18 && listOfPlayerWorms[i].position.y == 14)) {
                        return new DoNothingCommand();
                    }

                    List<Cell> surroundingBlocks = getSurroundingCells(listOfPlayerWorms[i].position.x, listOfPlayerWorms[i].position.y);
                    int cellIdx = 0; //random.nextInt(surroundingBlocks.size());
                    if (listOfPlayerWorms[i].position.y < 16) {
                        cellIdx = 7;
                    }

                    Cell block = surroundingBlocks.get(cellIdx);
                    if (block.type == CellType.AIR) {
                        String X = String.valueOf(block.x);
                        String Y = String.valueOf(block.y);
                        return new SelectCommand(id,"move "+X+" "+Y);
                    } else if (block.type == CellType.DIRT) {
                        String X = String.valueOf(block.x);
                        String Y = String.valueOf(block.y);
                        return new SelectCommand(id,"dig "+X+" "+Y);
                    }
                }

                if (id == 3) {
                    if (enemyWormSelectToShoot != null) {
                        Direction direction = resolveDirection(listOfPlayerWorms[i].position, enemyWormSelectToShoot.position);
                        return new SelectCommand(id,"shoot "+direction.name());
                    }

                    if (listOfPlayerWorms[i].position.x >= 15 && listOfPlayerWorms[i].position.x <= 18 && listOfPlayerWorms[i].position.y >= 15 && listOfPlayerWorms[i].position.y <= 18) {
                        return new DoNothingCommand();
                    }

                    List<Cell> surroundingBlocks = getSurroundingCells(listOfPlayerWorms[i].position.x, listOfPlayerWorms[i].position.y);
                    int cellIdx = 6; //random.nextInt(surroundingBlocks.size());
                    if (listOfPlayerWorms[i].position.x > 17) {
                        cellIdx = 1;
                    }

                    Cell block = surroundingBlocks.get(cellIdx);
                    if (block.type == CellType.AIR) {
                        String X = String.valueOf(block.x);
                        String Y = String.valueOf(block.y);
                        return new SelectCommand(id,"move "+X+" "+Y);
                    } else if (block.type == CellType.DIRT) {
                        String X = String.valueOf(block.x);
                        String Y = String.valueOf(block.y);
                        return new SelectCommand(id,"dig "+X+" "+Y);
                    }
                }
            }
            else { return new DoNothingCommand();}
        }

        if (currentWorm.id == 1) {
            if (enemyWorm != null) {
                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                return new ShootCommand(direction);
            }

            else if (round<10) return new DoNothingCommand();

            if (currentWorm.position.x >= 15 && currentWorm.position.x <= 18 && currentWorm.position.y >= 15 && currentWorm.position.y <= 18) {
                return new DoNothingCommand();
            }

            if ((currentWorm.position.x == 14 && currentWorm.position.y == 14) || (currentWorm.position.x == 17 && currentWorm.position.y == 19)) {
                return new DoNothingCommand();
            }

            List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
            int cellIdx = 2; //random.nextInt(surroundingBlocks.size());
            if (currentWorm.position.y > 17) {
                cellIdx = 5;
            }


            Cell block = surroundingBlocks.get(cellIdx);
            if (block.type == CellType.AIR) {
                return new MoveCommand(block.x, block.y);
            } else if (block.type == CellType.DIRT) {
                return new DigCommand(block.x, block.y);
            }
        }


        if (currentWorm.id == 2) {
            if ((enemyWormBomb.x != currentWorm.position.x || enemyWormBomb.y != currentWorm.position.y) && getCurrentWorm(gameState).bananaBombs.count > 0)  {
                return new BananaBombCommand(enemyWormBomb.x, enemyWormBomb.y);
            }

            if (enemyWorm != null) {
                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                return new ShootCommand(direction);
            }

            else if (7<round && round<60) return new DoNothingCommand();

            else if ((currentWorm.position.x == 14 && currentWorm.position.y == 18) || (currentWorm.position.x == 18 && currentWorm.position.y == 14)) {
                return new DoNothingCommand();
            }

            List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
            int cellIdx = 0; //random.nextInt(surroundingBlocks.size());
            if (currentWorm.position.y < 16) {
                cellIdx = 7;
            }

            Cell block = surroundingBlocks.get(cellIdx);
            if (block.type == CellType.AIR) {
                return new MoveCommand(block.x, block.y);
            } else if (block.type == CellType.DIRT) {
                return new DigCommand(block.x, block.y);
            }
        }

        if (currentWorm.id == 3) {
            if ((enemyWormBomb.x != currentWorm.position.x || enemyWormBomb.y != currentWorm.position.y) &&  getCurrentWorm(gameState).snowballs.count > 0) {
                for (Worm enemyFreeze : opponent.worms) {
                    if (enemyFreeze.position.x == enemyWormBomb.x && enemyFreeze.position.y == enemyWormBomb.y) {
                        if (enemyFreeze.freeze_round == 0) {
                            return new SnowballCommand(enemyWormBomb.x, enemyWormBomb.y);
                        }
                        else {
                            break;
                        }
                    }
                }

                //return new SnowballCommand(enemyWormBomb.x, enemyWormBomb.y);
            }

            if (enemyWorm != null) {
                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                return new ShootCommand(direction);
            }

            else if (7<round && round<75) return new DoNothingCommand();

            else if (currentWorm.position.x >= 16 && currentWorm.position.x <= 17 && currentWorm.position.y >= 15 && currentWorm.position.y <= 18) {
                return new DoNothingCommand();
            }

            List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
            int cellIdx = 6; //random.nextInt(surroundingBlocks.size());
            if (currentWorm.position.x > 17) {
                cellIdx = 1;
            }

            Cell block = surroundingBlocks.get(cellIdx);
            if (block.type == CellType.AIR) {
                return new MoveCommand(block.x, block.y);
            } else if (block.type == CellType.DIRT) {
                return new DigCommand(block.x, block.y);
            }
        }

        /*List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        int cellIdx = random.nextInt(surroundingBlocks.size());

        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }*/

        return new DoNothingCommand();
    }

    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition) && enemyWorm.health > 0) {
                return enemyWorm;
            }
        }

        return null;
    }

    private Position getFirstWormInRangeToBomb(int range) {
        int coordinateX = currentWorm.position.x;
        int coordinateY = currentWorm.position.y;

        for (int i = coordinateX - 5; i <= coordinateX + 5; i++) {
            for (int j = coordinateY - 5; j <= coordinateY + 5; j++) {
                if (i == coordinateX && j == coordinateY) {
                    continue;
                }
                if (!isValidCoordinate(i,j)) {
                    continue;
                }
                if (euclideanDistance(coordinateX,coordinateY,i,j) > range) {
                    continue;
                }
                for (Worm enemyWorm : opponent.worms) {
                    if (i == enemyWorm.position.x && j == enemyWorm.position.y && enemyWorm.health>0) {
                        return enemyWorm.position;
                    }
                }
            }
        }
        return currentWorm.position;
    }

    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        boolean friendlyFire = false;
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range && !friendlyFire; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Worm[] listOfPlayerWorms = gameState.myPlayer.worms;
                for (int i = 0; i < listOfPlayerWorms.length; i++) {
                    if (coordinateX == listOfPlayerWorms[i].position.x && coordinateY == listOfPlayerWorms[i].position.y
                        && listOfPlayerWorms[i].health>0) {
                        friendlyFire = true;
                        break;
                    }
                }
                if (friendlyFire) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if ((i != x || j != y) && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }
}
