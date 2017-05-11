
public class TreasureMap {
	private static final int mapSize = 180;
	
	private static final int centre = mapSize/2;
	//centre of the map is (0, 0)
	
	private char map[][]; //takes [y][x] coordinates
	private int[] dimensions; //specifies the active parts of the map rectangle
	//[topleft-x, topleft-y, width, height]
	
	
	public TreasureMap(){
		map = new char[mapSize][mapSize];
		for(int i = 0; i < mapSize; i++){
			for(int j = 0; j < mapSize; j++){
				map[i][j] = 'u'; //for unknown
			}
		}
		dimensions = new int[4];
		dimensions[0] = centre - 2;
		dimensions[1] = centre - 2;
		dimensions[2] = 0;
		dimensions[3] = 0;
	}
	
	
	public void addStartingView(char view[][]){
		assert(view.length > 0);
		assert(view[0].length > 0);
		for(int j = 0; j < view.length; j++){
			for(int i = 0; i < view[0].length; i++){
				map[j+dimensions[1]][i+dimensions[0]] = view[j][i];
				if(i == 2 && j == 2) map[j+dimensions[1]][i+dimensions[0]] = '^';
			}
		}
		dimensions[2] = view.length;
		dimensions[3] = view[0].length;
	}
	public void updateMap(char view[], Direction d, int[] pos){
		int cx = centre+pos[0];
		int cy = centre+pos[1];
		int offset = 2;
		switch(d){
		case NORTH:
			if(cy-offset < dimensions[1]){
				dimensions[1] -= 1;
				dimensions[3] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy-offset][cx-offset+i] = view[i];
			}
			System.out.println("north");
			break;
		case EAST:
			if(cx+offset >= dimensions[0]+dimensions[2]){
				dimensions[2] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy-offset+i][cx+offset] = view[i];
			}
			System.out.println("east");
			break;
		case SOUTH:
			if(cy+offset >= dimensions[1]+dimensions[3]){
				dimensions[3] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy+offset][cx-offset+i] = view[view.length-1-i];
			}
			System.out.println("south");
			break;
		case WEST:
			if(cx-offset < dimensions[0]){
				dimensions[0] -= 1;
				dimensions[2] += 1;
			}
			for(int i = 0; i < view.length; i++){
				map[cy-offset+i][cx-offset] = view[view.length-1-i];
			}
			System.out.println("west");
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
}
