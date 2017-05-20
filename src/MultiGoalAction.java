import java.util.*;

//better version of pathfinder
//can find a goal for lists of possible positions
public class MultiGoalAction implements Ai{
	private int[] position; //starting at 0,0
	private int moves;
	private Direction currentDirection; //starting from north
	private TreasureMap map;
	
	private String commandBuffer;
	private char lastMove;
	
	private boolean hasTreasure;
	private boolean hasKey;
	private boolean hasAxe;
	private boolean hasRaft;
	
	//searching
	private Section discovered;
	private LinkedList<Integer[]> backtracker;
	private boolean backing;
	
	
	public MultiGoalAction(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new TreasureMap();
		commandBuffer = "";
		
		hasTreasure = false;
		hasKey = false;
		hasAxe = false;
		hasRaft = false;
		
		discovered = new Section(map.getMapSize());
		backtracker = new LinkedList<Integer[]>();
		backing = false;
	}
	
	private void update(char view[][]){
		if(moves == 0){
			map.addStartingView(view);
			discovered.setTrue(0, 0);
		}else{
			updateUsingLastMove(view);
		}
		map.printMap();
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
				discovered.setTrue(cv[0], cv[1]);
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
				hasRaft = true;
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
		for(Integer[] m: map.getTreasures()){
			goal[0] = m[0]; goal[1] = m[1];
			if(getCommands(goal)) return true;
		}
		if(!hasKey && !map.getKeys().isEmpty()){
			for(Integer[] k: map.getKeys()){
				goal[0] = k[0]; goal[1] = k[1];
			}
		}
		if(!hasAxe && !map.getAxes().isEmpty()){
			for(Integer[] a: map.getAxes()){
				goal[0] = a[0]; goal[1] = a[1];
			}
		}
		
		//exploring
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState current = new GameState(position[0], position[1], currentDirection);
		GameState[] neighbours = current.generateNeighbours();
		for(GameState gs: neighbours){
			int[] pos = gs.getPosition();
			if(!map.isBlockedAt(pos[0], pos[1]) && !discovered.getValue(pos[0], pos[1])){
				int h = map.getNumUnknowns(pos, gs.getDirection());
				gs.setHeuristic(h);
				states.add(gs);
			}
		}
		if(!states.isEmpty()){
			GameState next = states.poll();
			commandBuffer = next.getMoves();
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
		Section visited = new Section(map.getMapSize());
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState init = new GameState(position[0], position[1], currentDirection);
		visited.setTrue(position[0], position[1]);
		states.add(init);
		while(!states.isEmpty()){
			GameState currentState = states.poll();
			List<GameState> gs = getNeighbouringGameStates(currentState);
			for(GameState next: gs){
				if(next.checkGoal(goal)){
					commandBuffer = next.getMoves();
					System.out.println(commandBuffer);
					return true;
				}
				int[] pos = next.getPosition();
				if(!visited.getValue(pos[0], pos[1])){
					next.calculateHeuristic(goal);
					states.add(next);
					visited.setTrue(pos[0], pos[1]);
				}
			}
		}
		return false;
	}
	private List<GameState> getNeighbouringGameStates(GameState gs){
		List<GameState> neighbours = new LinkedList<GameState>();
		int[] cp = gs.getPosition();
		Direction df = gs.getDirection();
		String moves = gs.getMoves();
		int[] vf = df.getVector1();
		int[] cvf = {cp[0]+vf[0], cp[1]+vf[1]};
		if(!map.isWallOrWaterAt(cvf[0], cvf[1])){
			neighbours.add(new GameState(cvf[0], cvf[1], df, moves+"f"));
		}else if(map.isCharAtPosition(cvf[0], cvf[1], '-') && hasKey){
			neighbours.add(new GameState(cvf[0], cvf[1], df, moves+"uf"));
		}else if(map.isCharAtPosition(cvf[0], cvf[1], 'T') && hasAxe){
			neighbours.add(new GameState(cvf[0], cvf[1], df, moves+"cf"));
		}
		
		Direction dl = df.turnLeft();
		int[] vl = dl.getVector1();
		int[] cvl = {cp[0]+vl[0], cp[1]+vl[1]};
		if(!map.isWallOrWaterAt(cvl[0], cvl[1])){
			neighbours.add(new GameState(cvl[0], cvl[1], dl, moves+"lf"));
		}else if(map.isCharAtPosition(cvl[0], cvl[1], '-') && hasKey){
			neighbours.add(new GameState(cvl[0], cvl[1], dl, moves+"luf"));
		}else if(map.isCharAtPosition(cvl[0], cvl[1], 'T') && hasAxe){
			neighbours.add(new GameState(cvl[0], cvl[1], dl, moves+"lcf"));
		}
		
		Direction dr = df.turnRight();
		int[] vr = dr.getVector1();
		int[] cvr = {cp[0]+vr[0], cp[1]+vr[1]};
		if(!map.isWallOrWaterAt(cvr[0], cvr[1])){
			neighbours.add(new GameState(cvr[0], cvr[1], dr, moves+"rf"));
		}else if(map.isCharAtPosition(cvr[0], cvr[1], '-') && hasKey){
			neighbours.add(new GameState(cvr[0], cvr[1], dr, moves+"ruf"));
		}else if(map.isCharAtPosition(cvr[0], cvr[1], 'T') && hasAxe){
			neighbours.add(new GameState(cvr[0], cvr[1], dr, moves+"rcf"));
		}
		
		Direction db = df.turnLeft().turnLeft();
		int[] vb = db.getVector1();
		int[] cvb = {cp[0]+vb[0], cp[1]+vb[1]};
		if(!map.isWallOrWaterAt(cvb[0], cvb[1])){
			neighbours.add(new GameState(cvb[0], cvb[1], db, moves+"llf"));
		}else if(map.isCharAtPosition(cvb[0], cvb[1], '-') && hasKey){
			neighbours.add(new GameState(cvb[0], cvb[1], db, moves+"lluf"));
		}else if(map.isCharAtPosition(cvb[0], cvb[1], 'T') && hasAxe){
			neighbours.add(new GameState(cvb[0], cvb[1], db, moves+"llcf"));
		}
		return neighbours;
	}
}
