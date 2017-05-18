import java.util.Comparator;


public class GameStateComparator implements Comparator<GameState> {
	private boolean useStringLength;
	public GameStateComparator(boolean usl){
		useStringLength = usl;
	}
	@Override
	public int compare(GameState gs1, GameState gs2) {
		if(useStringLength){
			return gs1.getFValue() - gs2.getFValue();
		}else{
			return gs1.getHValue() - gs2.getHValue();
		}
	}
	
}
