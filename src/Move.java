import java.util.*;

//first ai just moves around the map

public class Move implements Ai{
	
	private int[] position; //starting at 0,0
	
	private int moves;
	private Direction currentDirection; //starting from north
	
	private GlobalMap map;
	
	private int lastMove;
	
	
	//private boolean[][] visited;
	private List<Integer[]> toVisit;
	
	private List<Character> nextCommands;

	public Move(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new GlobalMap();
		
		/*
		int s = map.getMapSize();
		visited = new boolean[s][s];
		for(int i = 0; i < s; i++){
			for(int j = 0; j < s; j++){
				visited[i][j] = false;
			}
		}
		*/
		toVisit = new LinkedList<Integer[]>();
		Integer[] i = {0,0};
		toVisit.add(i);
		nextCommands = new LinkedList<Character>();
	}
	public void updatePosition(char view[][]){
		if (moves == 0){
			//add starting view to map
			//map.movePlayer(new int[2]);
			map.addStartingView(view);
			
			Integer[] cp = toVisit.remove(0);
			addDirectionsToVisit(cp);
			
		}else{
			//update the map
			updatePositionFromLastMove(view);		
		}
		map.printMap();
	}
	public char makeMove(){
		char move = 'f';
		if (!nextCommands.isEmpty()){
			move = nextCommands.remove(0);
		}
		else if (!toVisit.isEmpty()){
			Integer[] cp = toVisit.remove(toVisit.size()-1);
			addDirectionsToVisit(cp);
			//visited[cp[0]][cp[1]] = true;
			
			nextCommands = findSteps(cp);
			move = nextCommands.remove(0);
		}
	  	moves++;
	  	return move;
  	}
	public void lastMove(int c){
		lastMove = c;
	}
	
	private void addToPosition(int x, int y){
		//int[] v = currentDirection.getVector1();
		position[0] += x;
		position[1] += y;
	}
	
	private void updatePositionFromLastMove(char[][] view){
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
				addToPosition(v[0], v[1]);
				map.updateMap(view[0], currentDirection, position);	
			}
		}
	}
	
	//adds new goal directions
	private void addDirectionsToVisit(Integer[] cp){
		for(int i = 0; i < Direction.values().length; i++){
			int[] vecn = Direction.values()[i].getVector1();
			Integer[] combined = {cp[0]+vecn[0], cp[1]+vecn[1]};
			char n = map.getCharAt(combined[0], combined[1]);
			if(n != '*' && n != '~'){
				toVisit.add(combined);
			}
		}
	}
	
	//finds a sequence of steps to new direction
	//currently only for max 1 forward steps to the goal 
	private List<Character> findSteps(Integer[] goal){
		List<Character> steps = new LinkedList<Character>();
		Direction dl = currentDirection.copy().turnLeft();
		Direction dr = currentDirection.copy().turnRight();
		Direction db = currentDirection.copy().turnLeft().turnLeft();
		
		System.out.println("Goal: "+String.valueOf(goal[0])+" ,"+String.valueOf(goal[1]));
		
		//forward
		int[] vf = currentDirection.getVector1();
		int[] vl = dl.getVector1();
		int[] vr = dr.getVector1();
		int[] vb = db.getVector1();
		
		//System.out.println("vl: "+String.valueOf(position[0]+vf[0])+" ,"+String.valueOf(position[1]+vf[1]));
		if(position[0]+vf[0] == goal[0] && position[1]+vf[1] == goal[1]){
			steps.add('f');
		}
		else if(position[0]+vl[0] == goal[0] && position[1]+vl[1] == goal[1]){
			steps.add('l');
			steps.add('f');
		}
		else if(position[0]+vr[0] == goal[0] && position[1]+vr[1] == goal[1]){
			steps.add('r');
			steps.add('f');
		}
		else if(position[0]+vb[0] == goal[0] && position[1]+vb[1] == goal[1]){
			steps.add('l');
			steps.add('l');
			steps.add('f');
		}
		return steps;
	}
	
	
	public int nMoves(){
		return moves;
	}
}
