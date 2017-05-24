import java.util.*;


public class SmartHunter implements Ai {
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
	private boolean onWater;
	private int numDynamite;
	
	//searching
	private Section discovered;
	private LinkedList<Integer[]> backtracker;
	private boolean backing;
	
	
	public SmartHunter(){
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
		onWater = false;
		numDynamite = 0;
		
		discovered = new Section();
		backtracker = new LinkedList<Integer[]>();
		backing = false;
	}
	
	private void update(char view[][]){
		if(moves == 0){
			map.addStartingView(view);
			discovered.setTrue(0, 0);
		}else{
			//discovered.printSection(map);
			updateUsingLastMove(view);
		}
		map.printMap();
		SectionManager sm = map.getSectionManager();
		int i = 0;
		for(Section land: sm.getLandSections()){
			System.out.println(i);
			land.printSection(map);
			i++;
		}
		i = 0;
		for(Section water: sm.getWaterSections()){
			System.out.println(i);
			water.printSection(map);
			i++;
		}
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
		}else if(lastMove == 'u' && hasKey){
			if(map.isCharAtPosition(cv[0], cv[1], '-')){
				map.removeDoors(cv);
				map.setCharAt(cv[0], cv[1], ' ');
				map.addToLand(cv);
			}
		}else if(lastMove == 'c' && hasAxe){
			if(map.isCharAtPosition(cv[0], cv[1], 'T')){
				map.setCharAt(cv[0], cv[1], ' ');
				map.addToLand(cv);
				hasRaft = true;
			}
		}else if(lastMove == 'b' && numDynamite > 0){
			if(map.isCharAtPosition(cv[0], cv[1], '*')){
				map.setCharAt(cv[0], cv[1], ' ');
				map.addToLand(cv);
				numDynamite--;
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
		SectionManager sm = map.getSectionManager();
		Section currentSection = sm.getSection(position[0], position[1], onWater);
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
				if(getCommands(goal)) return true;
			}
		}
		if(hasKey && !map.getDoors().isEmpty()){
			for(Integer[] k: map.getDoors()){
				goal[0] = k[0]; goal[1] = k[1];
				if(getCommands(goal)) return true;
			}
		}
		if(!hasAxe && !map.getAxes().isEmpty()){
			for(Integer[] a: map.getAxes()){
				goal[0] = a[0]; goal[1] = a[1];
				if(getCommands(goal)) return true;
			}
		}

		
		if(discovered.isEqual(currentSection)){
			System.out.println("can't explore more!");
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
		Section visited = new Section();
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState init = new GameState(position[0], position[1], currentDirection, hasRaft, onWater, 0);
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
		Direction df = gs.getDirection();
		Direction dl = df.turnLeft();
		Direction dr = df.turnRight();
		Direction db = df.turnLeft().turnLeft();
		GameState f = commandHelper(gs, df, "");
		if(f != null) neighbours.add(f);
		GameState l = commandHelper(gs, dl, "l");
		if(l != null) neighbours.add(l);
		GameState r = commandHelper(gs, dr, "r");
		if(r != null) neighbours.add(r);
		GameState b = commandHelper(gs, db, "ll");
		if(b != null) neighbours.add(b);
		return neighbours;
	}
	
	private GameState commandHelper(GameState gs, Direction d, String move){
		String soFar = gs.getMoves();
		int[] cp = gs.getPosition();
		int[] v = d.getVector1();
		int[] cb = {cp[0]+v[0], cp[1]+v[1]};
		
		boolean gsRaft = gs.hasRaft();
		boolean gsWater = gs.onWater();
		int gsDynamite = gs.numDynamite();
		
		int gsForward1 = gs.getFMoves()+1;
		
		if(!map.isBlockedAt(cb[0], cb[1])){
			return new GameState(cb[0], cb[1], d, gsRaft, gsWater, gsDynamite, soFar+move+"f", gsForward1);
		}else if(map.isCharAtPosition(cb[0], cb[1], '-') && hasKey){
			return new GameState(cb[0], cb[1], d, gsRaft, gsWater, gsDynamite, soFar+move+"uf", gsForward1);
		}else if(map.isCharAtPosition(cb[0], cb[1], 'T') && hasAxe){
			if(gsWater){
				return new GameState(cb[0], cb[1], d, false, gsWater, gsDynamite, soFar+move+"cf", gsForward1);
			}else{
				return new GameState(cb[0], cb[1], d, true, gsWater, gsDynamite, soFar+move+"cf", gsForward1);
			}
		}else if(map.isCharAtPosition(cb[0], cb[1], '*') && gsDynamite > 0){
			return new GameState(cb[0], cb[1], d, gsRaft, gsWater, gsDynamite-1, soFar+move+"bf", gsForward1);
		}else if(map.isCharAtPosition(cb[0], cb[1], '~')){
			if(!gsWater && gsRaft){
				return new GameState(cb[0], cb[1], d, false, true, gsDynamite, soFar+move+"f", gsForward1);
			}else if(gsWater){
				return new GameState(cb[0], cb[1], d, gsRaft, false, gsDynamite, soFar+move+"f", gsForward1);
			}
		}
		
		return null;
		
	}
}


