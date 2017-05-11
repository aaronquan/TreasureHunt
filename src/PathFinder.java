//an extension of the move ai. Finds path to a goal. Only moves

import java.util.*;

public class PathFinder implements Ai {
	
	//private static final int mOffset = 80;
	
	private int[] position; //starting at 0,0
	
	private int moves;
	
	private Direction currentDirection; //starting from north
	
	private TreasureMap map;
	
	private int lastMove;
	
	private boolean goalSet;
	private int[] goal;
	private String commandBuffer;
	
	public PathFinder(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new TreasureMap();
		
		goal = new int[2];
		goalSet = true;
		getGoal();
		commandBuffer = "";
		
		
	}
	public char makeMove(char[][] view) {
		char move = 'f';
		update(view);
		if(!commandBuffer.isEmpty()){
			move = commandBuffer.charAt(0);
			commandBuffer = commandBuffer.substring(1);
		}
		else if(goalSet){
			if(getCommands()){
				if(!commandBuffer.isEmpty()){
					move = commandBuffer.charAt(0);
					commandBuffer = commandBuffer.substring(1);
				}
			}
		}
		
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
		if(position[0] == goal[0] && position[1] == goal[1]){
			
		}
	}
	
	//use goal and find commands to reach the goal
	//false if no commands are gotten
	private boolean getCommands(){
		int ms = map.getMapSize();
		int hs = ms/2;
		boolean[][] visited = new boolean[ms][ms];
		/*
		for (int i = 0; i < ms; i++) {
			for(int j = 0; j < ms; j++){
				visited[i][j] = false;
			}
		}*/
		visited[position[0]+hs][position[1]+hs] = true;
		
		Comparator<GameState> gsc = new GameStateComparator();
		
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState init = new GameState(position[0], position[1], currentDirection);
		states.add(init);
		
		while(!states.isEmpty()){
			GameState currentState = states.poll();
			
			GameState[] toEvaluate = currentState.generateNeighbours();
			for(int i = 0; i < toEvaluate.length; i++){
				if(toEvaluate[i].checkGoal(goal)){
					commandBuffer = currentState.getMoves();
					goalSet = false;
					System.out.println(commandBuffer);
					return true;
				}
				int[] pos = toEvaluate[i].getPosition();
				if(notBlocked(pos[0], pos[1])){
					if(!visited[pos[0]+hs][pos[1]+hs]){
						toEvaluate[i].calculateHeuristic(goal);
						states.add(toEvaluate[i]);
						visited[pos[0]+hs][pos[1]+hs] = true;
					}
				}
			}
		}
		
		return false;
	}
	private boolean notBlocked(int x, int y) {
		char c = map.getCharAt(x, y);
		return (c != '~' && c != '*');
	}
}
