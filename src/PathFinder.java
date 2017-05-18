//an extension of the move ai. Finds path to a goal. Only moves

import java.util.*;

public class PathFinder implements Ai {
	
	//private static final int mOffset = 80;
	
	private int[] position; //starting at 0,0
	
	private int moves;
	
	private Direction currentDirection; //starting from north
	
	private TreasureMap map;
	
	private boolean hasTreasure;
	
	private int lastMove;
	
	//private boolean goalSet;
	private int[] goal;
	private String commandBuffer;
	
	private boolean[][] discovered;
	//private LinkedList<GameState> dfsStates;
	private LinkedList<Integer[]> backtracker;
	
	public PathFinder(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new TreasureMap();
		
		goal = new int[2];
		
		commandBuffer = "";
		
		hasTreasure = false;
		
		int ms = map.getMapSize();
		discovered = new boolean[ms][ms];
		//dfsStates = new LinkedList<GameState>();
		backtracker = new LinkedList<Integer[]>();
	}
	public char makeMove(char[][] view) {
		char move = 'f';
		update(view);
		
		
		if(!commandBuffer.isEmpty()){
			move = commandBuffer.charAt(0);
			commandBuffer = commandBuffer.substring(1);
		}
		else{
			getGoal(true);
			while(!getCommands()){
				getGoal(false);
			}
			//should never be empty
			if(!commandBuffer.isEmpty()){
				move = commandBuffer.charAt(0);
				commandBuffer = commandBuffer.substring(1);
			}
		}
		
		moves++;
		lastMove = move;
		return move;
	}
	
	private void update(char view[][]){
		if(moves == 0){
			map.addStartingView(view);
			int hs = map.getMapSize()/2;
			discovered[hs][hs] = true;
		}else{
			updateUsingLastMove(view);
		}
		map.printMap();
	}
	private void updateUsingLastMove(char[][] view){
		if(lastMove == 'l'){
			currentDirection = currentDirection.turnLeft();
			map.changePlayerDirection(position, currentDirection);
		}
		else if(lastMove == 'r'){
			currentDirection = currentDirection.turnRight();
			map.changePlayerDirection(position, currentDirection);
		}
		else if(lastMove == 'f'){
			int[] v = currentDirection.getVector1();
			//test for treasure
			if(map.hasTreasure(position[0]+v[0], position[1]+v[1])){
				
				hasTreasure = true;
				System.out.println("has the treasure: "+map.getCharAt(position[0]+v[0], position[1]+v[1]));
			};			
			
			//test for wall in front
			if(!map.isBlockedAt(position[0]+v[0], position[1]+v[1])){
				int hs = map.getMapSize()/2;
				discovered[position[0]+v[0]+hs][position[1]+v[1]+hs] = true;
				map.movePlayer(position, currentDirection);
				addToPosition(v);
				map.updateMap(view[0], currentDirection, position);	
			}
		}
	}
	private void addToPosition(int[] v){
		position[0] += v[0]; position[1] += v[1];
	}
	
	private void getGoal(boolean treasurePath){
		LinkedList<Integer[]> tre = map.getTreasures();
		if(hasTreasure){
			goal[0] = 0; goal[1] = 0;
		}
		else if(!tre.isEmpty() && treasurePath){
			Integer[] first = tre.getFirst();
			System.out.println("found");
			goal[0] = first[0]; goal[1] = first[1];
		}else{
			goal[0] = 0; goal[1] = 0; //default
			
			//discover more of the map
			
			//move in a valid direction using dfs
			GameState current = new GameState(position[0], position[1], currentDirection);
			GameState[] neighbours = current.generateNeighbours();
			boolean found = false;
			//can optimise by choosing best neighbour (based of information gained)
			//this loop only chooses first in the array i.e forward -> left -> right -> back (is priority order)
			for(int i = 0; i < neighbours.length; i++){
				int hs = map.getMapSize()/2;
				int[] newPos = neighbours[i].getPosition();
				if(!map.isBlockedAt(newPos[0], newPos[1]) && !discovered[newPos[0]+hs][newPos[1]+hs]){
					goal[0] = newPos[0]; goal[1] = newPos[1];
					found = true;
					break;
				}
			}
			if(!found && !backtracker.isEmpty()){
				Integer[] prev = backtracker.removeLast();
				goal[0] = prev[0]; goal[1] = prev[1];
			}else{
				Integer bt[] = {position[0], position[1]};
				backtracker.add(bt);
			}
		}
	}
	
	//use goal and find commands to reach the goal
	//false if no commands are gotten
	private boolean getCommands(){
		int ms = map.getMapSize();
		int hs = ms/2;
		boolean[][] visited = new boolean[ms][ms]; //should all be false
		
		Comparator<GameState> gsc = new GameStateComparator(true);
		
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState init = new GameState(position[0], position[1], currentDirection);
		visited[position[0]+hs][position[1]+hs] = true;
		
		states.add(init);
		
		while(!states.isEmpty()){
			GameState currentState = states.poll();
			
			GameState[] toEvaluate = currentState.generateNeighbours();
			for(int i = 0; i < toEvaluate.length; i++){
				if(toEvaluate[i].checkGoal(goal)){
					commandBuffer = toEvaluate[i].getMoves();
					System.out.println(commandBuffer);
					return true;
				}
				int[] pos = toEvaluate[i].getPosition();
				//System.out.println(pos[0]);
				if(!map.isBlockedAt(pos[0], pos[1])){
					
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
