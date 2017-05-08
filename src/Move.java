//first ai just moves around the map

public class Move implements Ai{
	
	private int[] position; //starting at 0,0
	
	private int moves;
	private Direction currentDirection;
	private char[][] globalMap;
	

	public Move(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		
		globalMap = new char[mapSize][mapSize];
		for(int i = 0; i < mapSize; i++){
			for(int j = 0; j < mapSize; j++){
				globalMap[i][j] = 'u'; //for unknown
			}
		}
	}
	public void updatePosition(char view[][]){
		if (moves == 0){
			//add starting view to map and initialise direction
			currentDirection = Direction.getDirection(view[2][2]);
			int offset = mapSize/2 - 2;
			for(int i = 0; i < view.length; i++){
				for(int j = 0; j < view.length; j++){
					globalMap[i+offset][j+offset] = view[i][j];
					if(i == 2 && j == 2) globalMap[i+offset][j+offset] = ' ';
				}
			}
		}else{
			//update the map
			
		}
	}
	public char makeMove(){
	  	moves++;
	  	return 'f';
  	}
}
