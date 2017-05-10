
public enum Direction {
	EAST, NORTH, WEST, SOUTH;
	
	public Direction turnLeft(){
		return values()[(this.ordinal()+1) % values().length];
	}
	public Direction turnRight(){
		return values()[(this.ordinal()+values().length-1) % values().length];
	}
	
	public char getDirectionalChar(){
		char c = '^';
		switch(this){
		case NORTH:
			c = '^';
			break;
		case EAST:
			c = '>';
			break;
		case SOUTH:
			c = 'v';
			break;
		case WEST:
			c = '<';
			break;
		}
		return c;
	}
	
	public int[] getVector1(){
		int[] v = new int[2];
		switch(this){
		case NORTH:
			v[0] = 0; v[1] = -1;
			break;
		case EAST:
			v[0] = 1; v[1] = 0;
			break;
		case SOUTH:
			v[0] = 0; v[1] = 1;
			break;
		case WEST:
			v[0] = -1; v[1] = 0;
			break;
		}
		return v;
	}
	
	public Direction copy(){
		return values()[this.ordinal()];
	}
	
}
