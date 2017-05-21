//denotes a joined part of the map
public class Section {
	private static final int size = TreasureMap.getMapSize();
	private boolean[][] section;
	private int halfSize;
	public Section(){
		section = new boolean[size][size];
		halfSize = size/2;
	}
	
	public void setValue(int x, int y, boolean b){
		section[x+halfSize][y+halfSize] = b;
	}
	public void setTrue(int x, int y){
		section[x+halfSize][y+halfSize] = true;
	}
	public boolean getValue(int x, int y){
		return section[x+halfSize][y+halfSize];
	}
	
	//returns equality
	public boolean isEqual(Section s){
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(getValue(i,j) != s.getValue(i, j)) return false;
			}
		}
		return true;
	}
	
	//returns whether s is a subset of this
	public boolean isSubset(Section s){
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(s.getValue(i, j)){
					if(getValue(i,j)) return false;
				}
			}
		}
		return true;
	}
	
	//adds section into this
	public void add(Section s){
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(s.getValue(i, j)) setTrue(i,j);
			}
		}
	}
	
	//checks whether square is next to section
	public boolean isNextTo(int x, int y){
		if(getValue(x+1,y)) return true;
		else if(getValue(x-1,y)) return true;
		else if(getValue(x,y+1)) return true;
		else if(getValue(x,y-1)) return true;
		return false;
	}
}
