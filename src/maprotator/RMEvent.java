package maprotator;

import java.util.TreeMap;

/**
 * RMEvent.java
 * 
 * Represents an Event on an RPG Maker 2000 map.
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public class RMEvent extends RMObject
{
	private static int[] essentialIDs = new int[] { 0x05 };	// Only essential item for an RM event is the page data
	private RMEventPage[] pages;							// List of constituent pages
	
	/**
	 * Reinterprets an existing RMObject as an RMEvent. The original should be discarded afterward.
	 * 
	 * @param original	The RMObject to be copied/reinterpreted.
	 */
	public RMEvent(RMObject original)
	{
		super(original);
		name = "Event " + VLQ.arrayToInt(header);
		
		validateData(essentialIDs);
		parsePages();
		
	}
	
	/**
	 * Parses the content of the page data DataItem (0x05) into separate RMEventPage items and stores them in an array.
	 */
	private void parsePages()
	{
		RMObject[] pageObjects= dataItems.get(0x05).getDataAsArray();
		pages = new RMEventPage[pageObjects.length];
		
		for (int i = 0; i < pages.length; i++)
		{
			pages[i] = new RMEventPage(pageObjects[i]);
			pages[i].setName(name + " / Page " + (i + 1));
		}
	}

	/**
	 * Updates RMEventPage items after changes to ensure consistency across all representations.
	 */
	public void updatePages()
	{
		// Remove old page data from items list
		dataItems.remove(0x05);
		dataItems.put(0x05, new DataItem(0x05, pages, this));
	}

	/**
	 * Sets the names to be associated with each DataItem ID.
	 */
	protected void initializeItemLabels()
	{
		if (itemLabels == null)
		{
			itemLabels = new TreeMap<Integer, String>();
			itemLabels.put(0x01, "Event name");
			itemLabels.put(0x02, "X position");
			itemLabels.put(0x03, "Y position");
			itemLabels.put(0x05, "Page data");
		}
	}
	
	/**
	 * @return	This event's starting X-position
	 */
	public int getXPos()
	{
		if (dataItems.containsKey(0x02))
			return dataItems.get(0x02).getDataAsInteger();
		else
			return 0;
	}
	
	/**
	 * @return	This event's starting Y-position
	 */
	public int getYPos()
	{
		if (dataItems.containsKey(0x03))
			return dataItems.get(0x03).getDataAsInteger();
		else
			return 0;
	}

	/**
	 * @param width	This event's new starting X-position
	 */
	public void setXPos(int width)
	{
		dataItems.remove(0x02);
		
		if (width > 0)
			dataItems.put(0x02, new DataItem(0x02, width, this));
	}
	
	/**
	 * @param width	This event's new starting Y-position
	 */
	public void setYPos(int height)
	{
		dataItems.remove(0x03);
		
		if (height > 0)
			dataItems.put(0x03, new DataItem(0x03, height, this));
	}
	
	/**
	 * Updates the event's facing according to the given rotation. Because facing is set on a per page basis, this method
	 * just instructs each RMEventPage to rotate individually.
	 * 
	 * @param rotation	The amount by which to rotate the event (clockwise)
	 */
	public void rotate(Rotation rotation)
	{
		for (RMEventPage page : pages)
			page.rotate(rotation);
	}
}