import java.util.LinkedList;

//denotes a joined part of the map
public class Section {
	private static final int size = TreasureMap.getMapSize();
	private static final int halfSize = size/2;
	//private static final int viewOffset = TreasureMap.getViewOffset();
	private boolean[][] section;
	private boolean allFalse;
	
	private LinkedList<Section> adjacentSections;
	
	private int[] dimensions; //for optimisations later if required
	//[topleft-x, topleft-y, width, height]
	
	public Section(int[] dim){
		section = new boolean[size][size];
		allFalse = true;
		adjacentSections = new LinkedList<Section>();
		dimensions = new int[4];
		dimensions[0] = dim[0];
		dimensions[1] = dim[1];
		dimensions[2] = dim[2];
		dimensions[3] = dim[3];
	}
	public Section(){
		section = new boolean[size][size];
		allFalse = true;
		adjacentSections = new LinkedList<Section>();
		dimensions = new int[4];
	}
	
	//avoid using this for is b is true
	//not fully complete
	public void setValue(int x, int y, boolean b){
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
	
	public boolean setTrue(int x, int y){
		if(allFalse){
			section[y+halfSize][x+halfSize] = true;
			allFalse = false;
			//dimensions[0] = x+halfSize; dimensions[1] = y+halfSize; dimensions[2] = 1; dimensions[3] = 1;
		}
		if(isNextTo(x,y)){
			section[y+halfSize][x+halfSize] = true;
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
		return section[y+halfSize][x+halfSize];
	}
	
	
	//returns equality
	public boolean isEqual(Section s){
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
	public boolean isSubset(Section s){
		int[] dim = s.getDimensions();
		for(int j = dim[1]-halfSize; j < dim[1]+dim[3]-halfSize; j++){
			for(int i = dim[0]-halfSize; i < dim[0]+dim[2]-halfSize; i++){
				if(s.getValue(i, j)){
					if(!getValue(i,j)) return false;
				}
			}
		}
		return true;
	}
	
	public boolean isOverlapping(Section s){
		return false;
	}
	
	//adds section into this
	public void add(Section s){
		int[] dim = s.getDimensions();
		for(int j = dim[1]-halfSize; j < dim[1]+dim[3]-halfSize; j++){
			for(int i = dim[0]-halfSize; i < dim[0]+dim[2]-halfSize; i++){
				if(s.getValue(i, j)) setValue(i,j,true);
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
	public boolean allNextTo(int x, int y){
		return getValue(x+1,y) && getValue(x-1,y) && getValue(x,y+1) && getValue(x,y-1);
	}
	public boolean sectionNextTo(Section s){
		for(int j = dimensions[1]-halfSize; j < dimensions[1]+dimensions[3]-halfSize; j++){
			for(int i = dimensions[0]-halfSize; i < dimensions[0]+dimensions[2]-halfSize; i++){
				if(getValue(i, j)){
					if(s.isNextTo(i, j)) return true;
				}
			}
		}
		return false;
	}
	
	public boolean addAdjacent(Section s){
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
	
	public void printSection(TreasureMap m){
		int[] dim = m.getDimensions();
		for(int j = dim[1]-halfSize; j < dim[1]+dim[3]-halfSize; j++){
			for(int i = dim[0]-halfSize; i < dim[0]+dim[2]-halfSize; i++){
				if(getValue(i,j)){
					System.out.print('T');
				}else{
					System.out.print('F');
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
	
	//the positions of the edges of the section
	public LinkedList<Integer[]> getOutline(){
		LinkedList<Integer[]> outline = new LinkedList<Integer[]>();
		for(int j = dimensions[1]-halfSize; j < dimensions[1]+dimensions[3]-halfSize; j++){
			for(int i = dimensions[0]-halfSize; i < dimensions[0]+dimensions[2]-halfSize; i++){
				if(getValue(i, j)){
					if(!allNextTo(i, j)){
						Integer[] pos = {i,j};
						outline.add(pos);
					}
				}
			}
		}
		return outline;
	}
	public LinkedList<Integer[]> getPositions(){
		LinkedList<Integer[]> positions = new LinkedList<Integer[]>();
		for(int j = dimensions[1]-halfSize; j < dimensions[1]+dimensions[3]-halfSize; j++){
			for(int i = dimensions[0]-halfSize; i < dimensions[0]+dimensions[2]-halfSize; i++){
				if(getValue(i, j)){
					Integer[] pos = {i,j};
					positions.add(pos);
				}
			}
		}
		return positions;
	}
	public Section copy(){
		Section copy = new Section();
		for(int j = dimensions[1]-halfSize; j < dimensions[1]+dimensions[3]-halfSize; j++){
			for(int i = dimensions[0]-halfSize; i < dimensions[0]+dimensions[2]-halfSize; i++){
				copy.setValue(i, j, getValue(i,j));
			}
		}
		copy.setDimensions(dimensions);
		return copy;
	}
}
