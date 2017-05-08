//first ai just moves around the map

public class Onlymove implements Ai{
	
	private int[] position; //starting at 0,0
	
	private int moves;
	private int direction;
	//private char[][] globalMap;

	public Onlymove(){
		position = new int[2];
		position[0] = 0; position[1] = 0;
		moves = 0;
		direction = 0;
		
	}
	public char makeMove(){
	  	moves++;
	  	return 'f';
  	}
}
