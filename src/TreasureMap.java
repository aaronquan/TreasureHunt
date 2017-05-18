//The map for the treasure game using relative coordinates to the starting position
//Used to abstract the map generation from the ai
//Holds key information for objects of the map and their whereabouts

import java.util.*;
public class TreasureMap {
	private static final int mapSize = 180;
	
	private static final int centre = mapSize/2;
	
	private static final int viewOffset = 2;
	private static final int viewWidth = 5;
	//centre of the map is (0, 0)
	
	private char map[][]; //takes [y][x] coordinates
	private int[] dimensions; //specifies the active parts of the map rectangle
	//[topleft-x, topleft-y, width, height]
	
	private int[] treasure; //position of the treasure
	private boolean treasureFound;
	
	private LinkedList<Integer[]> trees; //TO DO
	
	
	public TreasureMap(){
		map = new char[mapSize][mapSize];
		for(int i = 0; i < mapSize; i++){
			for(int j = 0; j < mapSize; j++){
				map[i][j] = 'u'; //for unknown
			}
		}
		dimensions = new int[4];
		dimensions[0] = centre - viewOffset;
		dimensions[1] = centre - viewOffset;
		dimensions[2] = 0;
		dimensions[3] = 0;
		
		treasure = new int[2];
		treasureFound = false;
		
		trees = new LinkedList<Integer[]>();
	}
	
	
	public void addStartingView(char view[][]){
		assert(view.length > 0);
		assert(view[0].length > 0);
		for(int j = 0; j < view.length; j++){
			for(int i = 0; i < view[0].length; i++){
				
				map[j+dimensions[1]][i+dimensions[0]] = view[j][i];
				if(i == 2 && j == 2) map[j+dimensions[1]][i+dimensions[0]] = '^';
				if(view[j][i] == '$'){
					treasureFound = true;
					treasure[0] = i - viewOffset; treasure[1] = j - viewOffset;
				}
			}
		}
		dimensions[2] = view.length;
		dimensions[3] = view[0].length;
	}
	public void updateMap(char view[], Direction d, int[] pos){
		int cx = centre+pos[0];
		int cy = centre+pos[1];
		//int offset = 2;
		switch(d){
		case NORTH:
			if(cy-viewOffset < dimensions[1]){
				dimensions[1] -= 1;
				dimensions[3] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy-viewOffset][cx+i-viewOffset] = view[i];
				if(view[i] == '$'){
					treasureFound = true;
					treasure[0] = pos[0]+i-viewOffset; treasure[1] = pos[1]-viewOffset;
				}
			}
			//System.out.println("north");
			break;
		case EAST:
			if(cx+viewOffset >= dimensions[0]+dimensions[2]){
				dimensions[2] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy-viewOffset+i][cx+viewOffset] = view[i];
				if(view[i] == '$'){
					treasureFound = true;
					treasure[0] = pos[0]+viewOffset; treasure[1] = pos[1]+i-viewOffset;
				}
			}
			//System.out.println("east");
			break;
		case SOUTH:
			if(cy+viewOffset >= dimensions[1]+dimensions[3]){
				dimensions[3] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy+viewOffset][cx+i-viewOffset] = view[view.length-1-i];
				if(view[i] == '$'){
					treasureFound = true;
					treasure[0] = pos[0]+i-viewOffset; treasure[1] = pos[1]+viewOffset;
				}
			}
			//System.out.println("south");
			break;
		case WEST:
			if(cx-viewOffset < dimensions[0]){
				dimensions[0] -= 1;
				dimensions[2] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy+i-viewOffset][cx-viewOffset] = view[view.length-1-i];
				if(view[i] == '$'){
					treasureFound = true;
					treasure[0] = pos[0]-viewOffset; treasure[1] = pos[1]+i-viewOffset;
				}
			}
			//System.out.println("west");
			break;
		}
	}
	public char getCharAt(int x, int y){
		return map[y+centre][x+centre];
	}
	public char[][] getMap(){
		return map;
	}
	public int getMapSize(){
		return mapSize;
	}
	
	public void printMap(){
		System.out.println("Global Map");
		System.out.print("+");
		for(int k = 0; k < dimensions[2]; k++){
			System.out.print('-');
		}
		System.out.println("+");
		
		for(int j = dimensions[1]; j < dimensions[1]+dimensions[3]; j++){	
			System.out.print("|");
			for(int i = dimensions[0]; i < dimensions[0]+dimensions[2]; i++){
					System.out.print(map[j][i]);
			}
			System.out.println("|");
		}
		
		System.out.print("+");
		for(int k = 0; k < dimensions[2]; k++){
			System.out.print('-');
		}
		System.out.println("+");
	}
	
	public int getNumUnknowns(int[] pos, Direction d){
		int n = 0;
		switch(d){
		case NORTH:
			for(int i = -viewOffset; i <= viewOffset; i++){
				if(map[pos[1]-viewOffset][pos[0]+i] == 'u') n++;
			}
			break;
		case EAST:
			for(int i = -viewOffset; i <= viewOffset; i++){
				if(map[pos[1]+i][pos[0]+viewOffset] == 'u') n++;
			}
			break;
		case SOUTH:
			for(int i = -viewOffset; i <= viewOffset; i++){
				if(map[pos[1]+viewOffset][pos[0]+i] == 'u') n++;
			}
			break;
		case WEST:
			for(int i = -viewOffset; i <= viewOffset; i++){
				if(map[pos[1]+i][pos[0]-viewOffset] == 'u') n++;
			}
			break;
		}
		return n;
	}
	
	public void movePlayer(int[] pos, Direction d){
		int[] v = d.getVector1();
		char c = d.getDirectionalChar();
		map[centre+pos[1]][centre+pos[0]] = ' ';
		map[centre+pos[1]+v[1]][centre+pos[0]+v[0]] = c;
	}
	public void movePlayer(int[] pos){
		map[centre+pos[1]][centre+pos[0]] = '^';
	}
	public void changePlayerDirection(int[] pos, Direction d){
		map[centre+pos[1]][centre+pos[0]] = d.getDirectionalChar();
	}
	
	//map coordinates to relative coordinates
	private int[] toRelativeCoordinates(int[] tmc){
		int[] r = {tmc[0]-centre, tmc[1]-centre};
		return r;
	}
	
	//may not be valid if treasureFound is false
	//unhandled for multiple treasures
	public int[] getTreasurePosition(){
		int[] t = new int[2];
		if(treasureFound){
			t[0] = treasure[0]; t[1] = treasure[1];
		}else{
			t[0] = 0; t[1] = 0;
		}
		return t;
	}
	/*
	public boolean hasTreasure(int x, int y){
		return treasure[0] == x && treasure[1] == y;
	}*/
	public boolean hasTreasure(int x, int y){
		return map[y+centre][x+centre] == '$';
	}
	
	public boolean isTreasureFound(){
		return treasureFound;
	}
	
	public boolean isBlockedAt(int x, int y){
		char c = map[y+centre][x+centre];
		return c == '*' || c == 'T' || c == '~' || c == 'u';
	}
	
	public boolean isBlockedAt(int x, int y, boolean hasBoat){
		char c = map[y+centre][x+centre];
		return c == '*' || c == '.' || c == 'T' || (hasBoat && c == '~');
	}
}
