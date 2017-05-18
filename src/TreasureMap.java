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
	
	private LinkedList<Integer[]> treasures; //position of the treasures
	
	private LinkedList<Integer[]> keys; //position of the keys
	
	private LinkedList<Integer[]> doors; //position of the doors
	
	private LinkedList<Integer[]> dynamite; //position of the dynamite
	
	private LinkedList<Integer[]> axes; //position of the axes
	
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
		
		
		treasures = new LinkedList<Integer[]>();
		keys = new LinkedList<Integer[]>();
		doors = new LinkedList<Integer[]>();
		trees = new LinkedList<Integer[]>();
	}
	
	
	public void addStartingView(char view[][]){
		assert(view.length > 0);
		assert(view[0].length > 0);
		for(int j = 0; j < view.length; j++){
			for(int i = 0; i < view[0].length; i++){
				
				map[j+dimensions[1]][i+dimensions[0]] = view[j][i];
				if(i == 2 && j == 2) map[j+dimensions[1]][i+dimensions[0]] = '^';
				int x = i - viewOffset; int y = j - viewOffset;
				Integer[] p = {x,y};
				if(view[j][i] == '$'){
					treasures.add(p);
				}else if(view[j][i] == 'k'){
					keys.add(p);
				}
			}
		}
		dimensions[2] = view.length;
		dimensions[3] = view[0].length;
	}
	public void updateMap(char view[], Direction d, int[] pos){
		int cx = centre+pos[0];
		int cy = centre+pos[1];
		switch(d){
		case NORTH:
			if(cy-viewOffset < dimensions[1]){
				dimensions[1] -= 1;
				dimensions[3] += 1;
			}
			for(int i = 0; i < view.length; i++){
				if (map[cy-viewOffset][cx+i-viewOffset] != 'u') continue;
				map[cy-viewOffset][cx+i-viewOffset] = view[i];
				int x = pos[0]+i-viewOffset; int y = pos[1]-viewOffset;
				Integer[] p = {x,y};
				if(view[i] == '$'){
					treasures.add(p);
				}else if(view[i] == 'k'){
					keys.add(p);
				}
			}
			//System.out.println("north");
			break;
		case EAST:
			if(cx+viewOffset >= dimensions[0]+dimensions[2]){
				dimensions[2] += 1;
			}
			for(int i = 0; i < view.length; i++){
				if (map[cy-viewOffset+i][cx+viewOffset] != 'u') continue;
				map[cy-viewOffset+i][cx+viewOffset] = view[i];
				int x = pos[0]+viewOffset; int y = pos[1]+i-viewOffset;
				Integer[] p = {x,y};
				if(view[i] == '$'){
					treasures.add(p);
				}else if(view[i] == 'k'){
					keys.add(p);
				}
			}
			//System.out.println("east");
			break;
		case SOUTH:
			if(cy+viewOffset >= dimensions[1]+dimensions[3]){
				dimensions[3] += 1;
			}
			for(int i = 0; i < view.length; i++){
				if (map[cy+viewOffset][cx+i-viewOffset] != 'u') continue;
				map[cy+viewOffset][cx+i-viewOffset] = view[view.length-1-i];
				int x = pos[0]+i-viewOffset; int y = pos[1]+viewOffset;
				Integer[] p = {x,y};
				if(view[i] == '$'){
					treasures.add(p);
				}else if(view[i] == 'k'){
					keys.add(p);
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
				if (map[cy+i-viewOffset][cx-viewOffset] != 'u') continue;
				map[cy+i-viewOffset][cx-viewOffset] = view[view.length-1-i];
				int x = pos[0]-viewOffset; int y = pos[1]+i-viewOffset;
				Integer[] p = {x,y};
				if(view[i] == '$'){
					treasures.add(p);
				}else if(view[i] == 'k'){
					keys.add(p);
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
	
	public boolean hasTreasure(int x, int y){
		return map[y+centre][x+centre]=='$';
	}
	
	public LinkedList<Integer[]> getTreasures(){
		return treasures;
	}
	
	public LinkedList<Integer[]> getKeys(){
		return keys;
	}
	
	public boolean isBlockedAt(int x, int y){
		char c = map[y+centre][x+centre];
		return c == '*' || c == 'T' || c == '~' || c == '-' || c == 'u';
	}
	
	public boolean isBlockedAt(int x, int y, boolean hasBoat){
		char c = map[y+centre][x+centre];
		return c == '*' || c == '.' || c == 'T' || (hasBoat && c == '~');
	}
}
