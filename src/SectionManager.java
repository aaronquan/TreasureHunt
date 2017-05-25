import java.util.LinkedList;


public class SectionManager {
	private LinkedList<Section> landSections;
	private LinkedList<Section> waterSections;
	//private int currentSection; //index of the section the player is in
	public SectionManager(){
		landSections = new LinkedList<Section>();
		waterSections = new LinkedList<Section>();
	}
	public void addLand(Integer[] p){
		if(landSections.isEmpty()){
			Section n = new Section();
			n.setTrue(p[0], p[1]);
			landSections.add(n);
		}else{
			combineSections(p[0], p[1], landSections);
		}
		
	}
	public void addWater(Integer[] p){
		if(waterSections.isEmpty()){
			Section n = new Section();
			n.setTrue(p[0], p[1]);
			waterSections.add(n);
		}else{
			combineSections(p[0], p[1], waterSections);
		}
	}
	public LinkedList<Section> getLandSections(){
		return landSections;
	}
	public LinkedList<Section> getWaterSections(){
		return waterSections;
	}
	
	//combine sections on coordinates
	public void combineSections(int x, int y, LinkedList<Section> sections){
		boolean first = false;
		Section toAddTo = null;
		LinkedList<Integer> toDelete = new LinkedList<Integer>();
		Integer i = 0;
		for(Section s: sections){
			if(!first){
				if(s.setTrue(x, y)){
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
			Section n = new Section();
			n.setTrue(x, y);
			sections.add(n);
		}else{
			for(Integer ind: toDelete){
				System.out.println("hello?");
				sections.remove((int)ind);
			}
		}
	}
	
	public Section getSection(int x, int y, boolean onWater){
		if(!onWater){
			for(Section s: landSections){
				if(s.getValue(x, y)) return s;
			}
		}else{
			for(Section s: waterSections){
				if(s.getValue(x, y)) return s;
			}
		}
		return null;
	}
	public void setAllDimensions(int[] dim){
		for(Section s: landSections){
			s.setDimensions(dim);
		}
		for(Section s: waterSections){
			s.setDimensions(dim);
		}
	}
}
