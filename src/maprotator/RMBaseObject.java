package maprotator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * RMBaseObject.java
 * 
 * Abstract class underlying the other objects found in RM2K's .lmu files (including the object representing the map itself).
 * Defines the essential features of RPGMaker objects while leaving room for its children to define functionality specific
 * to their own structure.
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public abstract class RMBaseObject
{
	byte[] header;									// Header information of object
	protected TreeMap<Integer, DataItem> dataItems;	// Array of DataItems that define the object
	protected TreeMap<Integer, String> itemLabels;	// Names the purpose or function associated with each DataItem ID
	protected String name;							// Identifying description of the object itself
	
	/**
	 * When defined, extracts a header from the given byte array, starting at the given index, according
	 * to the child class's expectations for a header; and saves the result to the header array.
	 * 
	 * @param source		Source array to extract header from 
	 * @param startingIndex	Index of start of header
	 */
	protected abstract void parseHeader(byte[] source, int startingIndex);
	
	/**
	 * When defined, extracts data items from a given byte array, starting at the given index, according
	 * to the child class's expectations for data item storage.
	 * 
	 * @param source		Source array to extract data items from
	 * @param startingIndex	Index of start of data item list
	 */
	protected abstract void parseBody(byte[] source, int startingIndex);
	
	/**
	 * When defined, initializes item labels in accordance with the child class's own definitions. 
	 */
	protected abstract void initializeItemLabels();

	/**
	 * Validates that this object has parsed its source data correctly by checking this object's DataItems
	 * against a given list of IDs.
	 * Returns true if all the given IDs can be located within this object's DataItem tree.  
	 * 
	 * @param essentialIDs	An array of IDs to look for in the DataItem tree
	 * @return	True if all given IDs are present in the DataItem tree, false otherwise
	 */
	protected boolean validateData(int[] essentialIDs)
	{
		if (essentialIDs == null)
			return true;
		
		boolean[] idFound = new boolean[essentialIDs.length];
		
		// Whenever an essential ID is found, mark the corresponding index in idFound true
		Iterator<Entry<Integer, DataItem>> itr = dataItems.entrySet().iterator();
		while (itr.hasNext())
		{
			Integer key = itr.next().getKey();
			
			for (int i = 0; i < essentialIDs.length; i++)
				if (key == essentialIDs[i])
				{
					idFound[i] = true;
					i = essentialIDs.length;
				}
		}
		
		// Confirm all essential values have been found
		boolean passedValidation = true;
		for (int i = 0; i < idFound.length; i++)
		{
			passedValidation = passedValidation && idFound[i];
			if (idFound[i] == false)
				System.err.println("Failed to locate essential data item with ID " + String.format("0x%02X.", essentialIDs[i]));
		}
		
		return passedValidation;
	}
	
	/**
	 * Parse the header and body together. All children of RMBaseObject should do this in the same way, so the two steps
	 * are packaged together here.
	 * 
	 * @param source		Source array to extract header from
	 * @param startingIndex	Index of start of header list
	 */
	protected void parse(byte[] source, int startingIndex)
	{
		// Parse input data into individual items
		parseHeader(source, startingIndex);
		parseBody(source, startingIndex + header.length);
	}
	
	/**
	 * Prints all of the object's hex data. Includes both header and data, which is printed in ascending order of ID,
	 * and prints labels along with each item to identify them.
	 */
	protected void printAllData()
	{
		// Initialize item labels if not already labelled
		initializeItemLabels();
		
		// Print object name
		System.out.println(name);
		
		// Print header
		System.out.println("Header");
		RMTools.printHexData(header);
		
		// Print data items
		Iterator<Entry<Integer, DataItem>> itr = dataItems.entrySet().iterator();
		while (itr.hasNext())
		{
			Entry<Integer, DataItem> next = itr.next();
			
			String label = itemLabels.get(next.getKey());
			if (label == null)
				label = "Unknown";
			
			System.out.println(String.format("[0x%02X] ", next.getKey()) + label); 
			RMTools.printHexData(next.getValue().byteArray());
		}
	}
	
	/**
	 * @return The name of the current object
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets a name for the object to help identify it.
	 * 
	 * @param name	New name for the object
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return Total size of the hypothetical byte array corresponding to the object, including header, all data items, and end-of-object byte
	 */
	public int size()
	{
		int size = 1 + header.length;
		Iterator<Entry<Integer, DataItem>> itr = dataItems.entrySet().iterator();
		while (itr.hasNext())
			size += itr.next().getValue().itemSize();
		
		return size;
	}
	
	/**
	 * @return	The number of data items in this object
	 */
	public int itemCount()
	{
		return dataItems.size();
	}
	
	/**
	 * Shorthand alias for the iterator of this object's DataItem map
	 * @return	The iterator for this object's list of DataItems
	 */
	public Iterator<Entry<Integer, DataItem>> iterator()
	{
		return dataItems.entrySet().iterator();
	}
	
	/**
	 * @return	A byte array representation of this object
	 */
	public byte[] toArray()
	{
		byte[] result = new byte[size()];
		
		// Write each item to output array
		// Header
		int index = 0;
		RMTools.copyData(header, 0, result, 0, header.length);
		index += header.length;
		
		// Data items
		Iterator<Entry<Integer, DataItem>> itr = dataItems.entrySet().iterator();
		while (itr.hasNext())
		{
			DataItem nextItem = itr.next().getValue();
			RMTools.copyData(nextItem.byteArray(), 0, result, index, nextItem.itemSize());
			index += nextItem.itemSize();
		}
		
		return result;
	}
}
