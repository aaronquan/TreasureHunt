import java.util.LinkedList;


public class VSectionManager {
	private LinkedList<VSection> landSections;
	private LinkedList<VSection> waterSections;
	//private int currentSection; //index of the section the player is in
	public VSectionManager(){
		landSections = new LinkedList<VSection>();
		waterSections = new LinkedList<VSection>();
	}
	public void addLand(Integer[] p, int[] dim, char c){
		if(landSections.isEmpty()){
			VSection n = new VSection(dim);
			n.setTrue(p[0], p[1], c);
			landSections.add(n);
		}else{
			combineSections(p[0], p[1], landSections, dim, c);
		}
		
	}
	public void addWater(Integer[] p, int[] dim, char c){
		if(waterSections.isEmpty()){
			VSection n = new VSection(dim);
			n.setTrue(p[0], p[1], c);
			waterSections.add(n);
		}else{
			combineSections(p[0], p[1], waterSections, dim, c);
		}
	}
	public LinkedList<VSection> getLandSections(){
		return landSections;
	}
	public LinkedList<VSection> getWaterSections(){
		return waterSections;
	}
	
	//combine sections on coordinates
	public void combineSections(int x, int y, LinkedList<VSection> sections, int[] dim, char c){
		boolean first = false;
		VSection toAddTo = null;
		LinkedList<Integer> toDelete = new LinkedList<Integer>();
		Integer i = 0;
		for(VSection s: sections){
			//System.out.println(landSections);
			if(!first){
				if(s.isNextTo(x, y)){
					s.setValue(x, y, c);
					first = true;
					toAddTo = s;
				}
			}else{
				if(s.isNextTo(x, y)){
					toAddTo.add(s); //should never be null
					toDelete.addFirst(i);
				}
			}
			i += 1;
		}
		
		if(toAddTo == null){
			VSection n = new VSection(dim);
			n.setTrue(x, y, c);
			sections.add(n);
		}else{
			for(Integer ind: toDelete){
				sections.remove((int)ind);
			}
		}
	}
	
	public VSection getSection(int x, int y, boolean onWater){
		if(!onWater){
			for(VSection s: landSections){
				if(s.getValue(x, y)) return s;
			}
		}else{
			for(VSection s: waterSections){
				if(s.getValue(x, y)) return s;
			}
		}
		return null;
	}
	public void setAllDimensions(int[] dim){
		for(VSection s: landSections){
			s.setDimensions(dim);
		}
		for(VSection s: waterSections){
			s.setDimensions(dim);
		}
	}
}
