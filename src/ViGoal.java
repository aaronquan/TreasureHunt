import java.util.*;

//better version of pathfinder
//can find a goal for lists of possible positions
public class ViGoal implements Ai{
	private int[] position; //starting at 0,0
	private int moves;
	private Direction currentDirection; //starting from north
	private TreasureMap map;
	
	private String commandBuffer;
	private char lastMove;
	
	private boolean hasTreasure;
	private boolean hasKey;
	private boolean hasAxe;
	
	//searching
	private boolean[][] discovered;
	private LinkedList<Integer[]> backtracker;
	private boolean backing;
	
	
	public ViGoal(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new TreasureMap();
		commandBuffer = "";
		hasTreasure = false;
		hasKey = false;
		hasAxe = false;
		
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
		//map.printMap();
	}
	
	private void updateUsingLastMove(char[][] view){
		int[] v = currentDirection.getVector1();
		int[] cv = {position[0]+v[0], position[1]+v[1]};
		if(lastMove == 'l'){
			currentDirection = currentDirection.turnLeft();
			map.changePlayerDirection(position, currentDirection);
		}
		else if(lastMove == 'r'){
			currentDirection = currentDirection.turnRight();
			map.changePlayerDirection(position, currentDirection);
		}
		else if(lastMove == 'f'){
			//test for treasure
			if(map.isCharAtPosition(cv[0], cv[1], '$')){
				hasTreasure = true;
				System.out.println("has the treasure: "+map.getCharAt(cv[0], cv[1]));
			}else if(map.isCharAtPosition(cv[0], cv[1], 'k')){
				hasKey = true;
			}else if(map.isCharAtPosition(cv[0], cv[1], 'a')){
				hasAxe = true;
			}
			if(!map.isBlockedAt(cv[0], cv[1])){
				if(!backing){
					Integer bt[] = {position[0], position[1]};
					backtracker.add(bt);
				}
				map.movePlayer(position, currentDirection);
				position[0]+=v[0]; position[1]+=v[1];
				map.updateMap(view[0], currentDirection, position);	
			}
		}else if(lastMove == 'u'){
			if(map.isCharAtPosition(cv[0], cv[1], '-')){
				map.setCharAt(cv[0], cv[1], ' ');
			}
		}else if(lastMove == 'c'){
			if(map.isCharAtPosition(cv[0], cv[1], 'T')){
				map.setCharAt(cv[0], cv[1], ' ');
			}
		}
	}
	
	public char makeMove(char[][] view) {
		char move = 'f';
		update(view);
		//move based on commandbuffer
		if(!commandBuffer.isEmpty()){
			move = commandBuffer.charAt(0);
			commandBuffer = commandBuffer.substring(1);
		}else{
			//sets goal then gets commands from command buffer
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
		//Has treasure, returning to goal
		if(hasTreasure){
			goal[0] = 0; goal[1] = 0;
			if(getCommands(goal)) return true;
		}
		//doesnt have treasure, will try to get to treasure if found on map
		//will keep overwite goal, until last found treasure, then update goals.
		for(Integer[] m: map.getTreasures()){
			goal[0] = m[0]; goal[1] = m[1];
			System.out.println("asdasd, x = " + m[0] + " y = " + m[1] + "\nmy currposition x = "+ position[0] +" y = " +position[1]);
			if(getCommands(goal)) return true;
		}
		//same as above, but for keys
		if(!hasKey && !map.getKeys().isEmpty()){
			for(Integer[] k: map.getKeys()){
				goal[0] = k[0]; goal[1] = k[1];
			}
			if(getCommands(goal)) return true;
		}
		//same as above, but for axes, but will overwrite keys goal.
		if(!hasAxe && !map.getAxes().isEmpty()){
			for(Integer[] a: map.getAxes()){
				goal[0] = a[0]; goal[1] = a[1];
			}
		}
		
		//exploring
		int hs = map.getMapSize()/2;
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState current = new GameState(position[0], position[1], currentDirection);
		GameState[] neighbours = current.generateNeighbours();

		for(GameState gs: neighbours){
			int[] pos = gs.getPosition();
			if(isUnblockable(pos[0], pos[1]) && !discovered[pos[0]+hs][pos[1]+hs]){

				int h = map.getNumUnknowns(pos, gs.getDirection());

				System.out.println("h = " + h);

				gs.setHeuristic(h);
				states.add(gs);
				goal = pos;
				if(getCommands(goal))
					return true;
			}
		}
		if(!states.isEmpty()){
			GameState next = states.poll();
			commandBuffer = next.getMoves();
			int[] pos = next.getPosition();
			discovered[pos[0]+hs][pos[1]+hs] = true;
			return true;
		}

		if(!backtracker.isEmpty()){
			Integer[] back = backtracker.removeLast();
			goal[0] = back[0]; goal[1] = back[1];
			if (backing = getCommands(goal)) return true;
		}

		return false;
	}
	
	boolean isUnblockable(int x, int y) {
		System.out.println("x = " + x + " y = " + y);
		if(map.isCharAtPosition(x,y, '-') && hasKey) {
			System.out.println("dude I am here git me out");
			return true;
		}
		return (!map.isBlockedAt(x, y));
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
			int[] cp = currentState.getPosition();
			
			GameState[] gs = new GameState[4];
			
			Direction df = currentState.getDirection();
			gs[0] = commandHelper(cp[0], cp[1], df, currentState, "");
			//gamestate to add to turn left
			Direction dl = df.turnLeft();
			gs[1] = commandHelper(cp[0], cp[1], dl, currentState, "l");
			//gamestate to add to turn right
			Direction dr = df.turnRight();
			gs[2] = commandHelper(cp[0], cp[1], dr, currentState, "r");
			//gamestate to add to do a 180 (turn left twice)
			Direction db = df.turnLeft().turnLeft();
			gs[3] = commandHelper(cp[0], cp[1], db, currentState, "ll");

			for(int i = 0; i < gs.length; i++){
				if(gs[i] == null) continue;
				if(gs[i].checkGoal(goal)){
					commandBuffer = gs[i].getMoves();
					System.out.println(commandBuffer);
					return true;
				}
				int[] pos = gs[i].getPosition();
				//System.out.println(pos[0]);
				if(!visited[pos[0]+hs][pos[1]+hs]){
					gs[i].calculateHeuristic(goal);
					states.add(gs[i]);
					visited[pos[0]+hs][pos[1]+hs] = true;
				}
			}
		}
		return false;
	}

	GameState commandHelper(int x, int y, Direction d, GameState currState, String move) {
		int[] v = d.getVector1();
		int[] cvf = {x+v[0], y+v[1]};
		if(map.isCharAtPosition(cvf[0], cvf[1], '-') && hasKey){
			return new GameState(cvf[0], cvf[1], d, currState.getMoves()+move+"uf");
		} else if(!map.isBlockedAt(cvf[0] , cvf[1])){
			return new GameState(cvf[0] , cvf[1], d, currState.getMoves()+move+"f");
		}

		return null;
	}
	
}
