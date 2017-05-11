import java.util.*;


public class PathFinder implements Ai {
	
	//private static final int mOffset = 80;
	
	private int[] position; //starting at 0,0
	
	private int moves;
	
	private Direction currentDirection; //starting from north
	
	private TreasureMap map;
	
	private int lastMove;
	
	private int[] goal;
	private LinkedList<Character> commandBuffer;
	
	public PathFinder(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new TreasureMap();
		
		goal = new int[2];
		commandBuffer = new LinkedList<Character>();
	}
	public char makeMove(char[][] view) {
		char move = 'f';
		update(view);
		
		
		
		moves++;
		lastMove = move;
		return move;
	}
	
	private void update(char view[][]){
		if(moves == 0){
			map.addStartingView(view);
		}else{
			updateUsingLastMove(view);
		}
	}
	private void updateUsingLastMove(char[][] view){
		if(lastMove == 'l'){
			map.changePlayerDirection(position, currentDirection);
			currentDirection = currentDirection.turnLeft();
		}
		if(lastMove == 'r'){
			map.changePlayerDirection(position, currentDirection);
			currentDirection = currentDirection.turnRight();
		}
		if(lastMove == 'f'){
			int[] v = currentDirection.getVector1();
			//test for wall in front
			if(map.getCharAt(position[0]+v[0], position[1]+v[1]) != '*'){
				map.movePlayer(position, currentDirection);
				addToPosition(v);
				map.updateMap(view[0], currentDirection, position);	
			}
		}
	}
	private void addToPosition(int[] v){
		position[0] += v[0]; position[1] += v[1];
	}
	
	//TO DO
	private void getGoal(){
		goal[0] = 2; goal[1] = 2;
	}
	
	//TO DO
	//use goal and find commands to reach the goal
	//false if no commands are gotten
	private boolean getCommands(){
		return false;
	}
}
