//first ai just moves around the map

public class Move implements Ai{
	
	private int[] position; //starting at 0,0
	
	private int moves;
	private Direction currentDirection; //starting from north
	
	private GlobalMap map;
	
	private int lastMove;
	

	public Move(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		currentDirection = Direction.NORTH;
		map = new GlobalMap();
	}
	public void updatePosition(char view[][]){
		if (moves == 0){
			//add starting view to map
			//map.movePlayer(new int[2]);
			map.addStartingView(view);
		}else{
			//update the map
			updatePositionFromLastMove(view);		
		}
		map.printMap();
	}
	public char makeMove(){
	  	moves++;
	  	//updatePosition();
	  	return 'f';
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
	
}
