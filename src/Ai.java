
public interface Ai {
	static final int mapSize = 80;
	
	public char makeMove();

	public void updatePosition(char[][] view);
}
