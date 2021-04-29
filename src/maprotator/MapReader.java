package maprotator;

public class MapReader
{
	public static void main(String[] args)
	{
		RMMap map = new RMMap("C:\\Users\\claym\\OneDrive\\Documents\\projects\\RPG Maker 2000 workspace\\Rotating Map\\Map0003.lmu");
		map.rotateClockwise();
		map.saveMap("Map0011.lmu");
		map.rotateClockwise();
		map.saveMap("Map0012.lmu");
		map.rotateClockwise();
		map.saveMap("Map0013.lmu");
	}
	
	
}
