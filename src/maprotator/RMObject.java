package maprotator;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * RMObject.java
 * 
 * An instantiable but still non-specific RPG Maker object, defined (like its parent) by a header and a set of data items.
 * This is the parent of all other non-abstract RM object classes, and certain children (RMEvent and RMEventPage) can be
 * instantiated in this form and later transformed into their own specific subclasses.
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public class RMObject extends RMBaseObject
{	
	/**
	 * Extracts a new RMObject by copying a header and data items out of a
	 * source array of bytes, starting at a given index.
	 * 
	 * @param source		Byte array from which to copy the object
	 * @param startingIndex	Index from which to start copying
	 */
	public RMObject(byte[] source, int startingIndex)
	{
		parse(source, startingIndex);
		this.name = "unnamed";
	}
	
	/**
	 * Conversion constructor. Creates a new RMObject by copying references to header, DataItems, and name from
	 * a target RMObject, and then reparenting the DataItems. This is the constructor called by subclasses to 
	 * "cast" or reinterpret generic RMObjects as members of those specific subclasses. The copied object
	 * should be discarded after.
	 * 
	 * @param copyTarget	The RMObject to copy
	 */
	protected RMObject(RMObject copyTarget)
	{
		this.header = copyTarget.header;
		this.dataItems = copyTarget.dataItems;
		this.name = copyTarget.name;
		
		Iterator<Entry<Integer, DataItem>> itr = iterator();
		while (itr.hasNext())
			itr.next().getValue().setParent(this);
	}
	
	/**
	 *	Instantiates the object without defining any of its fields. Called by RMMap as it needs a superconstructor
	 *  to invoke, but doesn't actually need that superconstructor to do anything. 
	 */
	protected RMObject()
	{
		
	}
	
	/**
	 *	Overrides same method in RMBaseObject.
	 *	Parses header using the assumption that the header is a VLQ containing the object's ID w/r/t some external list.
	 *	If this isn't true for a subclass, that subclass can define its own parseHeader method (which RMMap does).
	 */
	@Override
	protected void parseHeader(byte[] source, int startingIndex)
	{
		header = VLQ.extractVlq(source, startingIndex).getData();
	}
		
	/**
	 * Extracts DataItems from source array starting at the given index.
	 */
	@Override
	protected void parseBody(byte[] source, int startingIndex)
	{
		dataItems = new TreeMap<Integer, DataItem>();
		
		while (source[startingIndex] != 0)
		{
			DataItem result = new DataItem(source, startingIndex, this);
			startingIndex += result.itemSize();
			dataItems.put(result.getID(), result);
			
//			printHexData(result.content);
		}
	}

	/**
	 * Initializes item labels for this object. Since RMObject represents a generic object agnostic of its intended
	 * use, item labels cannot be generated, and so this method doesn't need to do anything. 
	 */
	@Override
	protected void initializeItemLabels() 
	{
		itemLabels = new TreeMap<Integer, String>();
	}

}
