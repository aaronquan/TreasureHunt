//an extension of the move ai. Finds path to a goal. Only moves

import java.util.*;

public class PathFinder implements Ai {
	
	//private static final int mOffset = 80;
	
	private int[] position; //starting at 0,0
	
	private int moves;
	
	private Direction currentDirection; //starting from north
	
	private TreasureMap map;
	
	private int lastMove;
	
	private int[] goal;
	private String commandBuffer;
	
	public PathFinder(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new TreasureMap();
		
		goal = new int[2];
		commandBuffer = "";
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
			currentDirection = currentDirection.turnLeft();
			map.changePlayerDirection(position, currentDirection);
		}
		if(lastMove == 'r'){
			currentDirection = currentDirection.turnRight();
			map.changePlayerDirection(position, currentDirection);
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
		int ms = map.getMapSize();
		boolean[][] visited = new boolean[ms][ms];
		/*
		for (int i = 0; i < ms; i++) {
			for(int j = 0; j < ms; j++){
				visited[i][j] = false;
			}
		}*/
		
		
		Comparator<GameState> gsc = new GameStateComparator();
		
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState init = new GameState(position[0], position[1], currentDirection);
		states.add(init);
		
		while(!states.isEmpty()){
			GameState currentState = states.poll();
			
			GameState[] toEvaluate = currentState.generateNeighbours();
			for(int i = 0; i < toEvaluate.length; i++){
				
			}
			
			if(currentState.checkGoal(goal)){
				commandBuffer = currentState.getMoves();
				return true;
			}
			
			
		}
		
		return false;
	}
}
