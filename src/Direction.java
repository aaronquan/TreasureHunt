
public enum Direction {
	EAST, NORTH, WEST, SOUTH;
	
	public static Direction getDirection(char c){
		switch (c){
		case '^':
			return NORTH;
		case 'v':
			return SOUTH;
		case '>':
			return EAST;
		case '<':
			return WEST;
		default:
			return NORTH;
		}
	}
	public Direction turnLeft(){
		return values()[(ordinal()+1)%values().length];
	}
	public Direction turnRight(){
		return values()[(ordinal()-1)%values().length];
	}
	
}
