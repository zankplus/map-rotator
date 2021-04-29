package maprotator;

/**
 * DataItem.java
 * 
 * Packages data stored in the data array of an RMObject with metadata 
 * that identifies it and defines its length. 
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public class DataItem
{
	private int id;				// Denotes data item's identity and purpose within parent object
	private int dataSize;		// Size in bytes of data
	private int dataIndex;		// Index at which data begins
	private byte[] content;		// Representation of complete data item (inc. metadata) as an array of bytes
	private RMObject parent;	// Reference to parent object
	
	/**
	 * Creates a new DataItem by extracting an ID, size integer, and data from a source byte array, 
	 * beginning at the given index and copying over the bytes according to the extracted size integer.
	 * 
	 * @param source	Source byte array to copy data from
	 * @param index		Index of the source array to begin reading from
	 * @param parent	Reference to the parent object to which this items belongs
	 */
	public DataItem(byte[] source, int index, RMObject parent)
	{
		VLQ contentLength = VLQ.extractVlq(source, index + 1);
		dataSize = contentLength.toInteger();
		dataIndex = 1 + contentLength.size();
		content = new byte[1 + contentLength.size() + dataSize];
		RMTools.copyData(source, index, content, 0, content.length);
		id = content[0];
		
		this.parent = parent;
	}
	
	/**
	 * Creates a new DataItem from a given ID and data in the form of a byte array.
	 * 
	 * @param id		The ID to associate with this data
	 * @param data		The data to be stored in this item
	 * @param parent	Reference to the parent object to which this item belongs
	 */
	public DataItem(int id, byte[] data, RMObject parent)
	{
		this.id = id;
		this.parent = parent;
		
		VLQ sizeVLQ = new VLQ(data.length);
		dataSize = data.length;
		dataIndex = 1 + sizeVLQ.size();
		content = new byte[1 + sizeVLQ.size() + dataSize];
		
		content[0] = (byte) id;
		RMTools.copyData(sizeVLQ.getData(), 0, content, 1, sizeVLQ.size());
		RMTools.copyData(data, 0, content, dataIndex, data.length);
		
	}
	
	/**
	 * Creates a new DataItem from a given ID and data in the form of a VLQ.
	 *  
	 * @param id		The ID to associate with this data
	 * @param data		The data to be stored in this item
	 * @param parent	Reference to the parent object to which this item belongs
	 */
	public DataItem(int id, VLQ vlq, RMObject parent)
	{
		this(id, vlq.getData(), parent);
	}
	
	/**
	 * Creates a new DataItem from a given ID and data in the form of an integer.
	 *  
	 * @param id		The ID to associate with this data
	 * @param data		The data to be stored in this item
	 * @param parent	Reference to the parent object to which this item belongs
	 */
	public DataItem(int id, int data, RMObject parent)
	{
		this(id, new VLQ(data), parent);
	}
	
	/**
	 * Creates a new DataItem from a given ID and data in the form of an array of
	 * RMObjects.
	 *  
	 * @param id		The ID to associate with this data
	 * @param array		The data to be stored in this item
	 * @param parent	Reference to the parent object to which this item belongs
	 */
	public DataItem(int id, RMObject[] array, RMObject parent)
	{
		// Set ID
		this.id = id;
		
		// Determine cumulative size of all items in object array
		int dataSize = 0;
		for (RMObject object : array)
			dataSize += object.size();
		
		// Calculate VLQ size to determine total size of DataItem's byte array
		VLQ arrayLengthVLQ = new VLQ(array.length);
		dataSize += arrayLengthVLQ.size();
		VLQ sizeVLQ = new VLQ(dataSize);
		
		
		dataIndex = 1 + sizeVLQ.size();
		content = new byte[dataIndex + dataSize];
		
		// Write object data to content array
		content[0] = (byte) id;
		RMTools.copyData(sizeVLQ.getData(), 0, content, 1, sizeVLQ.size());
		RMTools.copyData(arrayLengthVLQ.getData(), 0, content, dataIndex, arrayLengthVLQ.size());
		
		// Array items
		int index = dataIndex + arrayLengthVLQ.size();
		for (int i = 0; i < array.length; i++)
		{
			byte[] objectData = array[i].toArray();
			RMTools.copyData(objectData, 0, content, index, objectData.length);
			index += objectData.length;
		}
		
		this.parent = parent;
	}

	/**
	 * @return	Length of content array (includes data and metadata)
	 */
	public int itemSize()
	{
		return content.length;
	}
	
	/**
	 * @return	The byte array representing the entirety of this data item
	 */
	public byte[] byteArray()
	{
		return content;
	}

	/**
	 * @return	ID of DataItem in its parent object (as integer)
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * @return	Length of the portion of the content array representing the item's data
	 */
	public int getDataSize()
	{
		return dataSize; 
	}
	
	/**
	 * @return	A copy of the portion of the content array representing the item's data, as a byte array
	 */
	public byte[] getData()
	{
		byte[] result = new byte[getDataSize()];
		for (int i = 0; i < result.length; i++)
			result[i] = content[dataIndex + i];
		
		return result;
	}
	
	/**
	 * @return	The integer value of the item's data (parsed as a VLQ)
	 */
	public int getDataAsInteger()
	{
		VLQ result = VLQ.extractVlq(content, dataIndex);
		return result.toInteger();
	}
	
	/**
	 * @return The item's data parsed as a string
	 */
	public String getDataAsString()
	{
		char[] result = new char[getDataSize()];
		for (int i = 0; i < result.length; i++)
			result[i] = (char) content[dataIndex + i];
		
		return new String(getData());
	}
	
	/**
	 * @return The array of RMObjects constituting this item's data - assuming it actually represents an array of objects.
	 */
	public RMObject[] getDataAsArray()
	{
		VLQ arrayLengthVLQ = VLQ.extractVlq(content, dataIndex); 
		int arrayLength = arrayLengthVLQ.toInteger();
		
		int index = dataIndex + arrayLengthVLQ.size();
		RMObject[] objects = new RMObject[arrayLength];
		
//		RMObject.printHexData(content);
		
		for (int i = 0; i < arrayLength; i++)
		{
			
			objects[i] = new RMObject(content, index);
			index += objects[i].size();
		}
		
		if (index == content.length)
		{
			System.out.println("Successfully parsed " + arrayLength + " objects from " + parent.getName() + "/Item 0x"  + String.format("%02X", id));
		}
		else
		{
			System.err.println("Failed to parse " + arrayLength + " objects from " + parent.getName() + "/Item 0x"  + String.format("%02X", id));
			System.err.println("Index: " + index + " / Content length: " + content.length);
			System.exit(1);
		}
		
		return objects;
	}
	
	/**
	 * Updates this DataItem's reference to its parent.
	 * @param parent
	 */
	public void setParent(RMObject parent)
	{
		this.parent = parent;
	}
	
	
}
