package maprotator;

import java.util.TreeMap;

/**
 * RMEventPage.java
 * 
 * Represents a page in an Event on an RPGMaker 2000 map.
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public class RMEventPage extends RMObject
{
	private static int[] essentialIDs = new int[] { 0x02, 0x17, 0x33, 0x34 }; // List of IDs that must be defined for a map
	
	/**
	 * Reinterprets an existing RMObject as an RMEventPage. The original should be discarded afterward.
	 * 
	 * @param original	The RMObject to be copied/reinterpreted.
	 */
	public RMEventPage(RMObject original)
	{
		super(original);
		validateData(essentialIDs);
	}
	
	/**
	 * Sets the names to be associated with each DataItem ID.
	 */
	protected void initializeItemLabels()
	{
		if (itemLabels == null)
		{
			itemLabels = new TreeMap<Integer, String>();
			itemLabels.put(0x02, "Conditions");
			itemLabels.put(0x15, "Charset name");
			itemLabels.put(0x16, "Charset selection index");
			itemLabels.put(0x17, "Facing");
			itemLabels.put(0x19, "Undetermined");
			itemLabels.put(0x1F, "Undetermined");
			itemLabels.put(0x20, "Undetermined");
			itemLabels.put(0x21, "Undetermined");
			itemLabels.put(0x23, "Undetermined");
			itemLabels.put(0x24, "Undetermined");
			itemLabels.put(0x29, "Undetermined");
			itemLabels.put(0x33, "Undetermined");
			itemLabels.put(0x34, "Script data");
		}
	}
	
	/**
	 * Updates the event's facing according to the given rotation.
	 * 
	 * @param rotation	The amount by which to rotate the event (clockwise)
	 */
	public void rotate(Rotation rotation)
	{
		DataItem currentRotation = dataItems.get(0x17);
		int newRotation = ((currentRotation.getDataAsInteger() + rotation.ordinal()) % 4);
		dataItems.put(0x17, new DataItem(0x17, newRotation, this));
	}
}
