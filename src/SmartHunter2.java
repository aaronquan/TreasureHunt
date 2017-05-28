import java.util.*;
/*
Briefly describe how your program works, including any algorithms and data structures employed, and explain any design decisions you made along the way.

How It Works
The AI works with three steps, updating the data structures, finding goal positions and finding the moves to get to the goal.
In the update phase, the ai uses its previous move as well as the data found from view to update the map and its gamestate (position, direction etc.).
Finding the goal (evaluateMove function) is the decision making step of the ai. Here we evaluate the position.
Final step is getting the sequence of moves to reach the goal to store in the commandbuffer (getCommands function).

Data Structures
GameState - a hypothetical state of the game
TreasureMap - holds the map of the world in a 160x160 array and the objects in the map
Section - denotes a part of the map (main decision making tool). 
e.g. if the treasure is in a neighbouring section blocked by a door and we have a key we can get the treasure!

Design Decisions

The code uses an ai interface for the Agent AI which has been refined over many times to our final copy. We started with an only moving ai and slowly started adding extra logic to each ai version (other ai's not included in submission).
 We first start of with the global map class called TreasureMap. It populates a 160x160 array as we explore around with the agent, identifying the items around the map and placing on it accordingly. We start off with the agent staring at
north, and use the Direction.java class in order to help with positioning the Agent. In order to make decisions on the map, we "sectioned" off the map, in order to help the ai
decide on what to do. A section denotes an area of connected land and water. So a section will always contain the current
path we've discovered so far, and all other disjointed land/water sections within the global map. We have a gamestate which records the gamestate for each foreseeable move, 
i.e going north/east/west/south. Our AI works by first seeing if there exist something we can grab within our section, if so it will path straight to it, otherwise it would
create a state for each direction and will go towards the most unknowns in that state (the heuristic). For doors, we unlock as there is no consequences (i.e no losing keys) however
with going on water and using dynamite, it was more harder to implement as we lose the item if we don't choose the right thing (its not perfectly implemented).

Reflection
Actually quite disappointed with the end result. Found it difficult to evaluate decisions such as finding the best wall to blow up with the data we had.
Thought that the initial design of the ai was good. Got quite messy afterwards. Not sure if the sections class is really helpful or easy to code with.
Seemed like a logical decision at the time but turn out too difficult to use properly (goal was to improve performance). Performance in the end was never the issue.
Thus should have done more searching. 
Could have created a graph of sections, but was worried of the difficulty maintaining the data structures, as maintaining sections was already iffy.
 
*/

public class SmartHunter2 implements Ai {
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
	private Section beenTo;
	private LinkedList<Integer[]> backtracker;
	private boolean backing;
	
	//private LinkedList<Integer[]> futureGoals;
	
	
	public SmartHunter2(){
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
		
		beenTo = new Section(map.getDimensions()); //all the positions we have already been
		backtracker = new LinkedList<Integer[]>();
		backing = false;
	}
	
	private void update(char view[][]){
		if(moves == 0){
			map.addStartingView(view);
			beenTo.setTrue(0, 0);
		}else{
			//discovered.printSection(map);
			
			updateUsingLastMove(view);
		}
		beenTo.setDimensions(map.getDimensions());
	}
	
	private void updateUsingLastMove(char[][] view){
		int[] v = currentDirection.getVector1();
		int[] cv = {position[0]+v[0], position[1]+v[1]};
		if(lastMove == 'l'){
			currentDirection = currentDirection.turnLeft();
			map.changePlayerDirection(position, currentDirection);
		}else if(lastMove == 'r'){
			currentDirection = currentDirection.turnRight();
			map.changePlayerDirection(position, currentDirection);
		}else if(lastMove == 'f'){
			//test for treasure
			if(map.isCharAtPosition(cv[0], cv[1], '$')){
				hasTreasure = true;
				//System.out.println("has the treasure: "+map.getCharAt(cv[0], cv[1]));
			}else if(map.isCharAtPosition(cv[0], cv[1], 'k')){
				hasKey = true;
				
			}else if(map.isCharAtPosition(cv[0], cv[1], 'a')){
				hasAxe = true;
			}else if(map.isCharAtPosition(cv[0], cv[1], 'd')){
				numDynamite++;
			}
			if(!map.isBlockedAt(cv[0], cv[1]) || map.isCharAtPosition(cv[0], cv[1], '~')){
				//dont have to do the case where hasRaft is false and water since game is lost
				if(map.isCharAtPosition(cv[0], cv[1], '~')){
					//System.out.println("water!!!");
					map.movePlayer(position, currentDirection, onWater);
					onWater = true;
					if(hasRaft) hasRaft = false;
				}else{
					map.movePlayer(position, currentDirection, onWater);
					onWater = false;
				}
				beenTo.setTrue(cv[0], cv[1]);
				if(!backing){
					Integer bt[] = {position[0], position[1]};
					backtracker.add(bt);
				}
				//map.movePlayer(position, currentDirection);
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
				map.removeTrees(cv);
				map.setCharAt(cv[0], cv[1], ' ');
				map.addToLand(cv);
				if(!onWater) hasRaft = true;
			}
		}else if(lastMove == 'b' && numDynamite > 0){
			if(map.isCharAtPosition(cv[0], cv[1], '*')){
				map.removeDynamite(cv);
				map.setCharAt(cv[0], cv[1], ' ');
				map.addToLand(cv);
				numDynamite--;
			}else if(map.isCharAtPosition(cv[0], cv[1], 'T')){
				map.removeTrees(cv);
				map.setCharAt(cv[0], cv[1], ' ');
				map.addToLand(cv);
				numDynamite--;
			}
		}
	}
	
	//Agent calls this!
	public char makeMove(char[][] view) {
		char move = 'f'; //default move
		update(view);
		if(!commandBuffer.isEmpty()){
			//use a predetermined move
			move = commandBuffer.charAt(0);
			commandBuffer = commandBuffer.substring(1);
		}else{
			//sets goal then gets commands for the command buffer
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
		Section homeSection = sm.getSection(0, 0, false);
		LinkedList<Section> l = reachableLandSections(currentSection);
		LinkedList<Section> w = reachableWaterSections(currentSection);
		if(hasTreasure){
			goal[0] = 0; goal[1] = 0;
			if(getCommands(goal)) return true;
		}
		for(Integer[] m: map.getTreasures()){
			if(currentSection.isEqual(homeSection)){
				for(Section s: l){
					//s.printSection(map);
					if(s.getValue(m[0], m[1])){
						goal[0] = m[0]; goal[1] = m[1];
						if(getCommands(goal)) return true;
					}
				}
			}else{
				if(onWater){
					for(Section s: l){
						boolean hasTree = false;
						for(Integer[] trees: map.getTrees()){
							if(s.isNextTo(trees[0], trees[1])){
								hasTree = true; break;
							}
						}
						if(s.getValue(m[0], m[1]) && hasTree){
							goal[0] = m[0]; goal[1] = m[1];
							if(getCommands(goal)) return true;
						}
					}
				}else if(hasRaft){
					for(Section s: l){
						if(s.getValue(m[0], m[1])){
							goal[0] = m[0]; goal[1] = m[1];
							if(getCommands(goal)) return true;
						}
					}
				}
			}
			//System.out.println("can reach treasure?");
		}
		if(!hasKey && !map.getKeys().isEmpty()){
			for(Integer[] k: map.getKeys()){
				if(currentSection.getValue(k[0], k[1])){
					goal[0] = k[0]; goal[1] = k[1];
					if(getCommands(goal)) return true;
				}
			}
		}
		if(!onWater && hasKey && !map.getDoors().isEmpty()){
			for(Integer[] d: map.getDoors()){
				if(currentSection.isNextTo(d[0], d[1])){
					goal[0] = d[0]; goal[1] = d[1];
					if(getCommands(goal)) return true;
				}
			}
		}
		if(!hasAxe && !map.getAxes().isEmpty()){
			for(Integer[] a: map.getAxes()){
				if(currentSection.getValue(a[0], a[1])){
					goal[0] = a[0]; goal[1] = a[1];
					if(getCommands(goal)) return true;
				}
			}
		}
		
		//if there is nothing left to explore in our current section
		if(beenTo.isSubset(currentSection)){
			//can cut down trees or blast walls or use raft to explore other sections
			if(!onWater){
				//on land
				if(hasRaft){
					for(Section s: w){
						if(beenTo.isSubset(s)) continue;
						Integer[] getGoal = getClosestFromCurrentPosition(s);
						goal[0] = getGoal[0]; goal[1] = getGoal[1];
						if(getCommands(goal)) return true;
					}
				}else{
					for(Section s: l){
						//s.printSection(map);
						if(beenTo.isSubset(s)) continue;
						Integer[] getGoal = getClosestFromCurrentPosition(s);
						goal[0] = getGoal[0]; goal[1] = getGoal[1];
						if(getCommands(goal)) return true;
					}
				}
			}else{
				//on water
				Section sec = bestValueSectionFromWater(l);
				if(sec != null){
					Integer[] getGoal = getClosestFromCurrentPosition(sec);
					goal[0] = getGoal[0]; goal[1] = getGoal[1];
					if(getCommandsMove(goal)) return true;
				}else{
					for(Section s: l){
						//s.printSection(map);
						if(beenTo.isSubset(s)) continue;
						boolean hasTree = false;
						for(Integer[] tree: map.getTrees()){
							if(s.isNextTo(tree[0], tree[1])){
								hasTree = true; 
								break;
							}
						}
						if(!hasTree) continue;
						Integer[] getGoal = getClosestFromCurrentPosition(s);
						goal[0] = getGoal[0]; goal[1] = getGoal[1];
						if(getCommands(goal)) return true;
					}
				}
			}
		}
		if(!onWater && !hasRaft && hasAxe && !map.getTrees().isEmpty()){
			for(Integer[] t: map.getTrees()){
				if(currentSection.isNextTo(t[0], t[1])){
					goal[0] = t[0]; goal[1] = t[1];
					if(getCommands(goal)) return true;
				}
			}
		}
		
		//exploring
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState current = new GameState(position[0], position[1], currentDirection);
		GameState[] neighbours = current.generateNeighbours();
		for(GameState gs: neighbours){
			int[] pos = gs.getPosition();
			if(!onWater){
				if(!map.isBlockedAt(pos[0], pos[1]) && !beenTo.getValue(pos[0], pos[1])){
					int h = map.getNumUnknowns(pos, gs.getDirection());
					gs.setHeuristic(-h);
					states.add(gs);
				}
			}else{
				if(map.isCharAtPosition(pos[0], pos[1], '~') && !beenTo.getValue(pos[0], pos[1])){
					int h = map.getNumUnknowns(pos, gs.getDirection());
					gs.setHeuristic(-h);
					states.add(gs);
				}
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
		
		//find goal out of all positions in the current section which gives the best information
		
		
		return false;
	}
	
	//use goal and find commands to reach the goal
	//false if no commands are gotten
	//a* search
	private boolean getCommands(int[] goal){
		Section visited = new Section(map.getDimensions());
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState init = new GameState(position[0], position[1], currentDirection, hasRaft, onWater, numDynamite);
		visited.setTrue(position[0], position[1]);
		states.add(init);
		while(!states.isEmpty()){
			GameState currentState = states.poll();
			List<GameState> gs = getNeighbouringGameStates(currentState);
			for(GameState next: gs){
				if(next.checkGoal(goal)){
					commandBuffer = next.getMoves();
					//System.out.println(commandBuffer);
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
	//modified a* search (can only move)
	private boolean getCommandsMove(int[] goal){
		Section visited = new Section(map.getDimensions());
		Comparator<GameState> gsc = new GameStateComparator(false);
		PriorityQueue<GameState> states = new PriorityQueue<GameState>(gsc);
		GameState init = new GameState(position[0], position[1], currentDirection, hasRaft, onWater, numDynamite);
		visited.setTrue(position[0], position[1]);
		states.add(init);
		while(!states.isEmpty()){
			GameState currentState = states.poll();
			List<GameState> gs = getNeighbouringGameStatesMove(currentState);
			for(GameState next: gs){
				if(next.checkGoal(goal)){
					commandBuffer = next.getMoves();
					//System.out.println(commandBuffer);
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
	private List<GameState> getNeighbouringGameStatesMove(GameState gs){
		List<GameState> neighbours = new LinkedList<GameState>();
		Direction df = gs.getDirection();
		Direction dl = df.turnLeft();
		Direction dr = df.turnRight();
		Direction db = df.turnLeft().turnLeft();
		GameState f = commandHelperMove(gs, df, "");
		if(f != null) neighbours.add(f);
		GameState l = commandHelperMove(gs, dl, "l");
		if(l != null) neighbours.add(l);
		GameState r = commandHelperMove(gs, dr, "r");
		if(r != null) neighbours.add(r);
		GameState b = commandHelperMove(gs, db, "ll");
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
		//if(gsForward1 > 10) return null;
		
		if(!map.isBlockedAt(cb[0], cb[1])){
			return new GameState(cb[0], cb[1], d, gsRaft, false, gsDynamite, soFar+move+"f", gsForward1);
		}else if(map.isCharAtPosition(cb[0], cb[1], '-') && hasKey){
			return new GameState(cb[0], cb[1], d, gsRaft, false, gsDynamite, soFar+move+"uf", gsForward1);
		}else if(map.isCharAtPosition(cb[0], cb[1], 'T') && hasAxe){
			if(gsWater){
				return new GameState(cb[0], cb[1], d, false, false, gsDynamite, soFar+move+"cf", gsForward1);
			}else{
				return new GameState(cb[0], cb[1], d, true, false, gsDynamite, soFar+move+"cf", gsForward1);
			}
		}else if(map.isCharAtPosition(cb[0], cb[1], 'T') && gsDynamite > 0){
			return new GameState(cb[0], cb[1], d, gsRaft, false, gsDynamite-1, soFar+move+"bf", gsForward1+10);
		}else if(map.isCharAtPosition(cb[0], cb[1], '*') && gsDynamite > 0){
			return new GameState(cb[0], cb[1], d, gsRaft, false, gsDynamite-1, soFar+move+"bf", gsForward1);
		}else if(map.isCharAtPosition(cb[0], cb[1], '~')){
			if(!gsWater && gsRaft){
				return new GameState(cb[0], cb[1], d, false, true, gsDynamite, soFar+move+"f", gsForward1);
			}else if(gsWater){
				return new GameState(cb[0], cb[1], d, false, true, gsDynamite, soFar+move+"f", gsForward1);
			}
		}
		
		return null;
		
	}
	private GameState commandHelperMove(GameState gs, Direction d, String move){
		String soFar = gs.getMoves();
		int[] cp = gs.getPosition();
		int[] v = d.getVector1();
		int[] cb = {cp[0]+v[0], cp[1]+v[1]};
		
		boolean gsRaft = gs.hasRaft();
		boolean gsWater = gs.onWater();
		int gsDynamite = gs.numDynamite();
		
		int gsForward1 = gs.getFMoves()+1;
		//if(gsForward1 > 10) return null;
		if(!map.isBlockedAt(cb[0], cb[1])){
			return new GameState(cb[0], cb[1], d, gsRaft, false, gsDynamite, soFar+move+"f", gsForward1);
		}
		else if(map.isCharAtPosition(cb[0], cb[1], '~')){
			if(!gsWater && gsRaft){
				return new GameState(cb[0], cb[1], d, false, true, gsDynamite, soFar+move+"f", gsForward1);
			}else if(gsWater){
				return new GameState(cb[0], cb[1], d, false, true, gsDynamite, soFar+move+"f", gsForward1);
			}
		}
		return null;
	}
	
	//gets neighbouring sections
	private LinkedList<Section> reachableLandSections(Section sec){
		SectionManager sm = map.getSectionManager();
		//Section currentSection = sm.getSection(position[0], position[1], onWater);
		
		LinkedList<Section> reachable = new LinkedList<Section>();
		LinkedList<Section> landSections = sm.getLandSections();
		
		for(Section s: landSections){
			/*
			if(onWater){
				if(currentSection.sectionNextTo(s)){
					reachable.add(s);
				}
			}else{
			*/
			if(s.isEqual(sec)) continue;
			//s.printSection(map);
			Section examined = s.copy();
			LinkedList<GameState> states = new LinkedList<GameState>();
			LinkedList<Integer[]> squares = s.getOutline();
			boolean found = false;
			for(Integer[] sq: squares){
				//System.out.println(sq[0]+" "+sq[1]);
				states.add(new GameState(sq[0], sq[1], currentDirection, hasRaft, onWater, numDynamite));
			}
			while(!states.isEmpty() && !found){
				GameState curr = states.removeFirst();
				int[] cp = curr.getPosition();
				Integer[] u = {cp[0], cp[1]-1}; Integer[] d = {cp[0], cp[1]+1};
				Integer[] l = {cp[0]-1, cp[1]}; Integer[] r = {cp[0]+1, cp[1]};
				LinkedList<Integer[]> directions = new LinkedList<Integer[]>();
				directions.add(u); directions.add(d); directions.add(r); directions.add(l);
				int dyn = curr.numDynamite();
				for(Integer[] pos: directions){
					if (examined.getValue(pos[0], pos[1])) continue;
					examined.setTrue(pos[0], pos[1]);
					if(sec.getValue(pos[0], pos[1])) {
						reachable.add(s);
						found = true;
					}
					if(map.isCharAtPosition(pos[0], pos[1], 'T') && hasAxe){
						states.addFirst(new GameState(pos[0], pos[1], currentDirection, hasRaft, onWater, dyn));
					}else if(map.isCharAtPosition(pos[0], pos[1], 'T') && dyn > 0){
						states.addFirst(new GameState(pos[0], pos[1], currentDirection, hasRaft, onWater, dyn-1));
					}else if(map.isCharAtPosition(pos[0], pos[1], '-') && hasKey){
						states.addFirst(new GameState(pos[0], pos[1], currentDirection, hasRaft, onWater, dyn));
					}else if(map.isCharAtPosition(pos[0], pos[1], '*') && dyn > 0){
						states.addFirst(new GameState(pos[0], pos[1], currentDirection, hasRaft, onWater, dyn-1));
					}
				}
				//}
			}
		}
		
		return reachable;
	}
	private LinkedList<Section> reachableWaterSections(Section sec){
		LinkedList<Section> reachable = new LinkedList<Section>();
		SectionManager sm = map.getSectionManager();
		//Section currentSection = sm.getSection(position[0], position[1], onWater);
		LinkedList<Section> waterSections = sm.getWaterSections();
		if(hasRaft){
			for(Section s: waterSections){
				if(sec.sectionNextTo(s)) reachable.add(s);
			}
		}
		
		return reachable;
	}
	private Integer[] getClosestFromCurrentPosition(Section s){
		LinkedList<Integer[]> outline = s.getOutline();
		int min = 80*80; int index = 0; int minIndex = -1;
		for(Integer[] pos: outline){
			if(Math.abs(pos[0]-position[0])+Math.abs(pos[1]-position[1])<min){
				min = Math.abs(pos[0]-position[0])+Math.abs(pos[1]-position[1]);
				minIndex = index; 
			}
			index++;
		}
		if(minIndex == -1) return null;
		return outline.get(minIndex);
		
	}
	
	private Section bestValueSectionFromWater(LinkedList<Section> ls){
		//SectionManager sm = map.getSectionManager();
		//Section currentSection = sm.getSection(position[0], position[1], onWater);
		boolean needKey = false;
		//boolean needDynamite = false;
		for(Section s: ls){
			for(Integer[] d: map.getDoors()){
				if(s.isNextTo(d[0], d[1])) needKey = true;
			}
		}
		int max = 0;
		Section ret = null;
		for(Section s: ls){
			int val = 1;
			boolean hasTrees = false;
			if(beenTo.isSubset(s)) continue;
			for(Integer[] t: map.getTrees()){
				if(s.isNextTo(t[0], t[1])) hasTrees = true;
			}
			if(!hasTrees) continue;
			for(Integer[] d: map.getDynamite()){
				if(s.getValue(d[0], d[1])) val++;
			}
			if(needKey && !hasKey){
				for(Integer[] k: map.getKeys()){
					if(s.getValue(k[0], k[1])) val+=100;
				}
			}
			if(hasKey){
				for(Integer[] d: map.getDoors()){
					if(s.getValue(d[0], d[1])) val+=2;
				}
			}
			if(val > max){
				max = val;
				ret = s;
			}
		}
		return ret;
	}
}
