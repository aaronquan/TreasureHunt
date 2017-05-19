import java.util.*;

//better version of pathfinder
//can find a goal for lists of possible positions
public class MultiGoal implements Ai{
	private int[] position; //starting at 0,0
	private int moves;
	private Direction currentDirection; //starting from north
	private TreasureMap map;
	
	private String commandBuffer;
	private char lastMove;
	
	private boolean hasTreasure;
	private boolean hasKey;
	
	//searching
	private boolean[][] discovered;
	private LinkedList<Integer[]> backtracker;
	private boolean backing;
	
	
	public MultiGoal(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new TreasureMap();
		commandBuffer = "";
		hasTreasure = false;
		hasKey = false;
		
		int ms = map.getMapSize();
		discovered = new boolean[ms][ms];
		backtracker = new LinkedList<Integer[]>();
		backing = false;
	}
	
	private void update(char view[][]){
		if(moves == 0){
			map.addStartingView(view);
			int hs = map.getMapSize()/2;
			discovered[hs][hs] = true;
		}else{
			int hs = map.getMapSize()/2;
			discovered[position[0]+hs][position[1]+hs] = true;
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
			if(map.isCharAtPosition(position[0]+v[0], position[1]+v[1], '$')){
				hasTreasure = true;
				System.out.println("has the treasure: "+map.getCharAt(position[0]+v[0], position[1]+v[1]));
			}else if(map.isCharAtPosition(position[0]+v[0], position[1]+v[1], 'k')){
				hasKey = true;
			}
			if(!map.isBlockedAt(position[0]+v[0], position[1]+v[1])){
				if(!backing){
					Integer bt[] = {position[0], position[1]};
					backtracker.add(bt);
				}
				map.movePlayer(position, currentDirection);
				position[0]+=v[0]; position[1]+=v[1];
				map.updateMap(view[0], currentDirection, position);	
			}
		}
	}
	
	public char makeMove(char[][] view) {
		char move = 'f';
		update(view);
		if(!commandBuffer.isEmpty()){
			move = commandBuffer.charAt(0);
			commandBuffer = commandBuffer.substring(1);
		}else{
			//sets goal then gets commands for command buffer
			if(evaluateMove()){
				move = commandBuffer.charAt(0);
				commandBuffer = commandBuffer.substring(1);
			}
		}
		
		moves++;
		lastMove = move;
		return move;
	}
	private boolean evaluateMove(){
		backing = false;
		int[] goal = {0,0};
		if(hasTreasure){
			goal[0] = 0; goal[1] = 0;
			if(getCommands(goal)) return true;
		}
		for(Integer[] c: map.getTreasures()){
			goal[0] = c[0]; goal[1] = c[1];
			if(getCommands(goal)) return true;
		}
		
		//exploring
		int hs = map.getMapSize()/2;
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState current = new GameState(position[0], position[1], currentDirection);
		GameState[] neighbours = current.generateNeighbours();
		for(GameState gs: neighbours){
			int[] pos = gs.getPosition();
			if(!map.isBlockedAt(pos[0], pos[1]) && !discovered[pos[0]+hs][pos[1]+hs]){
				int h = map.getNumUnknowns(pos, gs.getDirection());
				gs.setHeuristic(h);
				states.add(gs);
			}
		}
		if(!states.isEmpty()){
			GameState next = states.poll();
			commandBuffer = next.getMoves();
			int[] pos = next.getPosition();
			discovered[pos[0]+hs][pos[1]+hs] = true;
			return true;
		}else if(!backtracker.isEmpty()){
			Integer[] back = backtracker.removeLast();
			goal[0] = back[0]; goal[1] = back[1];
			if (backing = getCommands(goal)) return true;
		}
		return false;
	}
	
	//use goal and find commands to reach the goal
	//false if no commands are gotten
	private boolean getCommands(int[] goal){
		int ms = map.getMapSize();
		int hs = ms/2;
		boolean[][] visited = new boolean[ms][ms]; //should all be false
		
		Comparator<GameState> gsc = new GameStateComparator(false);
		
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
}
