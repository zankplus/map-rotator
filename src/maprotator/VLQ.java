package maprotator;

/**
 * VLQ.java
 * Defines a variable-length quantity, i.e., a compressed representation of an integer.
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public class VLQ
{
	private byte[] data;	// The series of bytes comprising this VLQ.
	
	/**
	 * Generates a VLQ from an array of bytes.
	 * No input validation is performed, so be careful.
	 * 
	 * @param input		The array of bytes to interpret as a VLQ representation 
	 */
	public VLQ(byte[] input)
	{
		data = input;
	}
	
	/**
	 * Constructs a VLQ representing the given integer.
	 * 
	 * @param input	An integer to be stored as VLQ
	 */
	public VLQ(int input)
	{
		data = parseInteger(input);
	}
	
	/**
	 * @return	The integer value of this VLQ
	 */
	public int toInteger()
	{
		return arrayToInt(data);
	}
	
	/**
	 * @return	Returns the number of bytes this VLQ uses to represent its integer value.
	 */
	public int size()
	{
		return data.length;
	}
	
	/**
	 * Prints the integer value of this VLQ.
	 */
	public String toString()
	{
		return toInteger() + "";
	}
	
	/**
	 * @return	The byte array representing this VLQ
	 */
	public byte[] getData()
	{
		return data;
	}
	
	/**
	 * Converts an integer into VLQ format and returns the result.
	 * 
	 * @param input	The integer to be converted
	 * @return	The VLQ representation of the given integer
	 */
	public static byte[] parseInteger(int input)
	{
		// The length of the VLQ is one byte for every 7 bits needed to represent the integer
		int bits = countSignificantBits(input);
		byte[] result = new byte[(int) Math.ceil(bits / 7.0)];
		
		// The rightmost byte holds the 7 least significant bits, the second rightmost holds the next 7, and so on...
		for (int i = 0; i < result.length; i++)
		{
			byte currentByte = (byte) ((input >> (i * 7)) & 0x7F);
			result[result.length - i - 1] = currentByte;
		}
		
		// If a byte does not represent the least significant set of bits, the first bit of the byte is switched to 1
		// to indicate that the next byte is part of it too
		for (int i = 0; i < result.length - 1; i++)
			result[i] = (byte) (result[i] | 0x80);
		
		return result;
	}
		
	/**
	 * Interprets a given byte array as a VLQ and returns its integer value.
	 * The VLQ is assumed to comprise the entire array, having already been extracted from the raw data.
	 * 
	 * @param input	The byte array to convert
	 * @return	The integer value of the given array
	 */
	public static int arrayToInt(byte[] input)
	{
		int result = 0;
		int index = 0;
		
		while (index <= input.length && (input[index] & 0xFF) >> 7 == 1)
		{
			result = result << 7;
			result |= input[index] & 0x7F;
			index++;
		}
		
		if (index <= input.length)
			result = (result << 7) | input[index];
		
		return result;
	}
	
	/**
	 * Extracts a VLQ byte array from a larger byte array, starting at the given index.
	 * 
	 * @param source		The array from which to extract a VLQ
	 * @param startingIndex	The index in the source array at which to start reading the VLQ 
	 * @return	A VLQ object representing the extracted value
	 */
	public static VLQ extractVlq(byte[] source, int startingIndex)
	{
		int length = 1;
		while ((source[startingIndex + length - 1] & 0xFF) >> 7 == 1)
			length++;
		
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++)
			result[i] = source[startingIndex + i];
		
		return new VLQ(result);
	}

	/**
	 * Returns the number of significant bits in the binary representation of a given integer
	 * 
	 * @param input	The integer whose significant bits are to be counted
	 * @return The number of significant bits in this integer
	 */
	private static int countSignificantBits(int input)
	{
		int result = 0;
		while (input > 0)
		{
			input = input >> 1;
			result++;
		}
		
		return Math.max(result, 1);
	}
}
