import java.util.LinkedList;

//denotes a joined part of the map
public class VSection {
	private static final int size = TreasureMap.getMapSize();
	private static final int halfSize = size/2;
	//private static final int viewOffset = TreasureMap.getViewOffset();
	private char[][] section;
	private boolean allFalse;
	
	private LinkedList<VSection> adjacentSections;
	
	private int[] dimensions; //for optimisations later if required
	//[topleft-x, topleft-y, width, height]
	
	public VSection(int[] dim){
		section = new char[size][size];
		allFalse = true;
		adjacentSections = new LinkedList<VSection>();
		dimensions = new int[4];
		dimensions[0] = dim[0];
		dimensions[1] = dim[1];
		dimensions[2] = dim[2];
		dimensions[3] = dim[3];
	}
	public VSection(){
		section = new char[size][size];
		allFalse = true;
		adjacentSections = new LinkedList<VSection>();
		dimensions = new int[4];
	}
	
	//avoid using this for is b is true
	//not fully complete
	public void setValue(int x, int y, char b){
		/*
		if(x+halfSize < dimensions[0]){
			dimensions[0] -= 1;
			dimensions[2] += 1;
		}else if(y+halfSize < dimensions[1]){
			dimensions[1] -= 1;
			dimensions[3] += 1;
		}else if(x+halfSize >= dimensions[0]+dimensions[2]){
			dimensions[2] += 1;
		}else if(y+halfSize >= dimensions[1]+dimensions[3]){
			dimensions[3] += 1;
		}*/
		section[y+halfSize][x+halfSize] = b;
	}
	
	public boolean setTrue(int x, int y, char c){
		if(allFalse){
			section[y+halfSize][x+halfSize] = c;
			allFalse = false;
			//dimensions[0] = x+halfSize; dimensions[1] = y+halfSize; dimensions[2] = 1; dimensions[3] = 1;
		}
		if(isNextTo(x,y)){
			section[y+halfSize][x+halfSize] = c;
			/*
			if(x+halfSize < dimensions[0]){
				dimensions[0] -= 1;
				dimensions[2] += 1;
			}else if(y+halfSize < dimensions[1]){
				dimensions[1] -= 1;
				dimensions[3] += 1;
			}else if(x+halfSize >= dimensions[0]+dimensions[2]){
				dimensions[2] += 1;
			}else if(y+halfSize >= dimensions[1]+dimensions[3]){
				dimensions[3] += 1;
			}*/
			return true;
		}
		return false;
	}
	public boolean getValue(int x, int y){
		char c = section[y+halfSize][x+halfSize];
		return c == ' ' || c == 'a' || c == 'k' || c == 'd' || c == '$' || c == '^' || c == '~';
	}
	
	public char getCharAt(int x, int y) {
		return section[y+halfSize][x+halfSize];
	}
	
	//returns equality
	public boolean isEqual(VSection s){
		int[] dim = s.getDimensions();
		if(dimensions[0] != dim[0] || dimensions[1] != dim[1] || 
		   dimensions[2] != dim[2] || dimensions[3] != dim[3]){
			return false;
		}
		for(int j = dimensions[1]-halfSize; j < dimensions[1]+dimensions[3]-halfSize; j++){
			for(int i = dimensions[0]-halfSize; i < dimensions[0]+dimensions[2]-halfSize; i++){
				if(getValue(i,j) != s.getValue(i, j)) return false;
			}
		}
		return true;
	}
	
	//returns whether s is a subset of this
	public boolean isSubset(VSection s){
		int[] dim = s.getDimensions();
		for(int j = dim[1]-halfSize; j < dim[1]+dim[3]-halfSize; j++){
			for(int i = dim[0]-halfSize; i < dim[0]+dim[2]-halfSize; i++){
				if(s.getValue(i, j)){
					if(getValue(i,j)) return false;
				}
			}
		}
		return true;
	}
	
	public boolean isOverlapping(VSection s){
		return false;
	}
	
	//adds section into this
	public void add(VSection s){
		int[] dim = s.getDimensions();
		for(int j = dim[1]-halfSize; j < dim[1]+dim[3]-halfSize; j++){
			for(int i = dim[0]-halfSize; i < dim[0]+dim[2]-halfSize; i++){
				if(s.getValue(i, j)) {
					setValue(i,j,s.getCharAt(i,j));
				}
				//System.out.println(s.getValue(i, j));
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
	public boolean sectionNextTo(VSection s){
		for(int j = dimensions[1]-halfSize; j < dimensions[1]+dimensions[3]-halfSize; j++){
			for(int i = dimensions[0]-halfSize; i < dimensions[0]+dimensions[2]-halfSize; i++){
				if(getValue(i, j)){
					if(s.isNextTo(i, j)) return true;
				}
			}
		}
		return false;
	}
	
	public boolean addAdjacent(VSection s){
		for(int j = dimensions[1]-halfSize; j < dimensions[1]+dimensions[3]-halfSize; j++){
			for(int i = dimensions[0]-halfSize; i < dimensions[0]+dimensions[2]-halfSize; i++){
				if(s.getValue(i, j)){
					if(isNextTo(i, j)){
						adjacentSections.add(s);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void printSection(VTreasureMap m){
		int[] dim = m.getDimensions();
		for(int j = dim[1]-halfSize; j < dim[1]+dim[3]-halfSize; j++){
			for(int i = dim[0]-halfSize; i < dim[0]+dim[2]-halfSize; i++){
				if(getValue(i,j)){
					System.out.print(getCharAt(i, j));
				}else{
					System.out.print('.');
				}
			}
			System.out.println();
		}
	}
	
	public int[] getDimensions(){
		return dimensions;
	}
	public void setDimensions(int[] dim){
		for(int i = 0; i < dim.length; i++){
			dimensions[i] = dim[i];
		}
		//dimensions = dim;
	}
}
