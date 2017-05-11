import java.util.Comparator;


public class GameStateComparator implements Comparator<GameState> {

	@Override
	public int compare(GameState gs1, GameState gs2) {
		return gs1.getFValue() - gs2.getFValue();
	}
	
}
