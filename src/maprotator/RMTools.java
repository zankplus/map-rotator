package maprotator;

/**
 * RMTools.java
 * A couple useful methods that are called by other classes, but which don't belong squarely to the province
 * of any one of them.
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public class RMTools 
{
	/**
	 * Copies data from one byte array to another.
	 * 
	 * @param sourceArray		The array to copy data from
	 * @param sourceIndex		The index in the source array from which to start copying
	 * @param destinationArray	The array to copy data to
	 * @param destinationIndex	The index in the destination array at which to start writing
	 * @param length			The number of bytes to copy from the source array to the destination
	 */
	public static void copyData(byte[] sourceArray, int sourceIndex, byte[] destinationArray, int destinationIndex, int length)
	{
		for (int i = 0; i < length; i++)
		{
			destinationArray[destinationIndex + i] = sourceArray[sourceIndex + i];
		}
	}

	/**
	 * Formats and prints an array of bytes for easier viewing.
	 * 
	 * @param data	The data to be printed
	 */
	public static void printHexData(byte[] data)
	{
		int rowLength = 32;
		
		for (int row = 0; row <= Math.ceil(1.0 * data.length / rowLength); row++)
		{
			for (int column = 0; column < rowLength && (column + row * rowLength < data.length); column++)
			{
				if (column % 8 == 0 && column > 0)
					System.out.print("| ");
				
				System.out.print(String.format("%02X ", data[row * rowLength + column]) + " ");
			}
			System.out.println();
		}
	}
}

