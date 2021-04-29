package maprotator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

/**
 * RMMap.java
 * 
 * Represents an RPGMaker 2000 map as an object (which constitutes the contents of a .lmu file).
 * 
 * @author Sraëka-Lillian (Clayton Cooper)
 * @version 04-29-21
 */
public class RMMap extends RMObject
{
	private String folderPath;		// Path of folder in which the loaded map is found
	public RMEvent[] events;		// List of RMEvent objects parsed from the event layer DataItem
	public static int rowLength;	// Number of hexes to display per row when printing data
	public static int[] essentialIDs = { 0x0B, 0x47, 0x48, 0x51, 0x5B };	// List of IDs that must be defined for a map
	public static int[][] tileRotationTable = { {0x00, 0x00, 0x00, 0x00},	// Used to define angular relationships between different tile rotation offsets
											    {0x01, 0x02, 0x04, 0x08},
											    {0x02, 0x04, 0x08, 0x01},
											    {0x03, 0x06, 0x0C, 0x09},
											    {0x04, 0x08, 0x01, 0x02},
											    {0x05, 0x0A, 0x05, 0x0A},
											    {0x06, 0x0C, 0x09, 0x03},
											    {0x07, 0x0E, 0x0D, 0x0B},
											    {0x08, 0x01, 0x02, 0x04},
											    {0x09, 0x03, 0x06, 0x0C},
											    {0x0A, 0x05, 0x0A, 0x05},
											    {0x0B, 0x07, 0x0E, 0x0D},
											    {0x0C, 0x09, 0x03, 0x06},
											    {0x0D, 0x0B, 0x07, 0x0E},
											    {0x0E, 0x0D, 0x0B, 0x07},
											    {0x0F, 0x0F, 0x0F, 0x0F},
											    {0x10, 0x14, 0x18, 0x1C},
											    {0x11, 0x15, 0x19, 0x1D},
											    {0x12, 0x16, 0x1A, 0x1E},
											    {0x13, 0x17, 0x1B, 0x1F},
											    {0x14, 0x18, 0x1C, 0x10},
											    {0x15, 0x19, 0x1D, 0x11},
											    {0x16, 0x1A, 0x1E, 0x12},
											    {0x17, 0x1B, 0x1F, 0x13},
											    {0x18, 0x1C, 0x10, 0x14},
											    {0x19, 0x1D, 0x11, 0x16},
											    {0x1A, 0x1E, 0x12, 0x15},
											    {0x1B, 0x1F, 0x13, 0x17},
											    {0x1C, 0x10, 0x14, 0x18},
											    {0x1D, 0x11, 0x15, 0x1A},
											    {0x1E, 0x12, 0x16, 0x19},
											    {0x1F, 0x13, 0x17, 0x1B},
											    {0x20, 0x21, 0x20, 0x21},
											    {0x21, 0x20, 0x21, 0x20},
											    {0x22, 0x24, 0x26, 0x28},
											    {0x23, 0x25, 0x27, 0x29},
											    {0x24, 0x26, 0x28, 0x22},
											    {0x25, 0x27, 0x29, 0x23},
											    {0x26, 0x28, 0x22, 0x24},
											    {0x27, 0x29, 0x23, 0x25},
											    {0x28, 0x22, 0x24, 0x26},
											    {0x29, 0x23, 0x25, 0x27},
											    {0x2A, 0x2D, 0x2C, 0x2B},
											    {0x2B, 0x2A, 0x2D, 0x2C},
											    {0x2C, 0x2B, 0x2A, 0x2D},
											    {0x2D, 0x2C, 0x2B, 0x2A},
											    {0x2E, 0x2E, 0x2E, 0x2E} };
	
	public static int[][] subtileRotationTable = {{0x0000, 0x0000, 0x0000, 0x0000},	// Defines angular relationships between different subtile offsets
												  {0x0032, 0x0064, 0x0190, 0x00C8},
												  {0x0064, 0x0190, 0x00C8, 0x0032},
												  {0x0190, 0x00C8, 0x0032, 0x0064},
												  {0x00C8, 0x0032, 0x0064, 0x0190},
												  {0x0096, 0x01F4, 0x0258, 0x00FA},
												  {0x01F4, 0x0258, 0x00FA, 0x0096},
												  {0x0258, 0x00FA, 0x0096, 0x01F4},
												  {0x00FA, 0x0096, 0x01F4, 0x0258},
												  {0x02EE, 0x02EE, 0x02EE, 0x02EE}};
	
	// Used for identifying the tile ID offsets of entries in the lower layer tile array
	public static int[] offsetMilestones = {0x0000, 0x03E8, 0x07D0, 0x0BB8, 0x0BEA, 0x0C1C, 0x0FA0, 0x0FD2, 0x1004, 0x1036, 0x1068,
											0x109A, 0x10CC, 0x10FE, 0x1130, 0x1162, 0x1194, 0x11C6};
	
	// Used for identifying the subtile ID offsets of water tiles in the lower layer tile array
	public static int[] subtileMilestones = { 0x0032, 0x0064, 0x0096, 0x00C8, 0x00FA, 0x0190, 0x01F4, 0x0258, 0x02EE };
	
	/**
	 * Attempts to load the specified .lmu map file and parse its header and data items into a new RMMap object.
	 * 
	 * @param path	Absolute path to file to be loaded
	 */
	public RMMap (String path)
	{
		super();
		
		// Initialize item labels, if they are not already initialized
		initializeItemLabels();
		// Extract the folder path
		
		int nameStartIndex = path.substring(0, path.length() - 1).lastIndexOf("\\") + 1;
		folderPath = path.substring(0, nameStartIndex);
		name = path.substring(nameStartIndex);
				
		byte[] inputData;
		try 
		{
			// Read input data
			inputData = Files.readAllBytes(Paths.get(path));

			// Parse object
			parse(inputData, 0);

			// Validate data items
			boolean passedValidation = validateData();
			
			// Display data items
			if (passedValidation)
			{
				// Parse event layer
				parseEventLayer();
				
				// Print map contents
				printAllData();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	protected void parseHeader(byte[] inputData, int startingIndex)
	{
		int headerSize = VLQ.extractVlq(inputData, startingIndex).toInteger();
		header = new byte[1 + headerSize];
		RMTools.copyData(inputData, startingIndex, header, 0, header.length);
	}

	/**
	 * Validates that this object has parsed its source data correctly by checking this object's DataItems
	 * against a given list of IDs.
	 * 
	 * @return	true if every required ID is found 
	 */
	protected boolean validateData()
	{
		return super.validateData(essentialIDs);
	}

	/**
	 * Parses the content of the event layer DataItem (0x51) into separate RMEvent items and stores them in an array.
	 */
	protected void parseEventLayer()
	{
		DataItem eventLayer = dataItems.get(0x51);
		RMObject[] uncast = eventLayer.getDataAsArray();
		events = new RMEvent[uncast.length];
		
		for (int i = 0; i < events.length; i++)
		{
			events[i] = new RMEvent(uncast[i]);
//			events[i].printAllData();
		}
	}

	/**
	 * Updates RMEvent items after changes to ensure consistency across all representations
	 */
	protected void updateEvents()
	{
		// Update pages for each event
		for (RMEvent event : events)
			event.updatePages();
			
		// Replace old pages item in data
		dataItems.remove(0x51);
		dataItems.put(0x051, new DataItem(0x051, events, this));
	}

	/**
	 * Sets the names to be associated with each DataItem ID.
	 */
	protected void initializeItemLabels()
	{
		if (itemLabels == null)
		{
			itemLabels = new TreeMap<Integer, String>();
			itemLabels.put(0x00, "Header");
			itemLabels.put(0x01, "Chipset");
			itemLabels.put(0x02, "Map width");
			itemLabels.put(0x03, "Map height");
			itemLabels.put(0x0B, "Scroll type");
			itemLabels.put(0x1F, "Parallax background enabled");
			itemLabels.put(0x20, "Parallax background selection");
			itemLabels.put(0x21, "Parallax background horizontal loop");
			itemLabels.put(0x22, "Parallax background vertical loop");
			itemLabels.put(0x23, "Parallax background horizontal scroll enabled");
			itemLabels.put(0x24, "Parallax background horizontal scroll speed");
			itemLabels.put(0x25, "Parallax background vertical scroll enabled");
			itemLabels.put(0x26, "Parallax background vertical scroll speed");
			itemLabels.put(0x47, "Lower layer data");
			itemLabels.put(0x48, "Upper layer data");
			itemLabels.put(0x51, "Event layer data");
			itemLabels.put(0x5B, "Save count");
		}
	}

	/**
	 * @return	The width of this map (in tiles)
	 */
	public int getMapWidth()
	{
		if (dataItems.containsKey(0x02))
			return dataItems.get(0x02).getDataAsInteger();
		else
			return 20;
	}
	
	/**
	 * @return	The height of this map (in tiles)
	 */
	public int getMapHeight()
	{
		if (dataItems.containsKey(0x03))
			return dataItems.get(0x03).getDataAsInteger();
		else
			return 15;
	}

	/**
	 * @param width	The new width for this map (in tiles)
	 */
	protected void setMapWidth(int width)
	{
		dataItems.remove(0x02);
		
		if (width > 20)
			dataItems.put(0x02, new DataItem(0x02, width, this));
	}
	
	/**
	 * @param height	The new height for this map (in tiles)
	 */
	protected void setMapHeight(int height)
	{
		dataItems.remove(0x03);
		
		if (height > 15)
			dataItems.put(0x03, new DataItem(0x03, height, this));
	}
	
	/**
	 * Resizes the map to the specified dimensions.
	 * 
	 * @param width		New width of map
	 * @param height	New height of map
	 */
	public void resize(int width, int height)
	{
		setMapWidth(width);
		setMapHeight(height);
	}

	/**
	 * 	Rotates all the tiles and events in this map 90 degrees clockwise.
	 */
	public void rotateClockwise() { rotateMap(Rotation.ROT_90); }
	
	/**
	 * Rotates all the tiles and events in this map 180 degrees.
	 */
	public void rotate180() { rotateMap(Rotation.ROT_180); }
	
	/**
	 * Rotates all the tiles and events in this map 270 degrees clockwise (90 degrees counterclockwise)
	 */
	public void rotateCounterclockwise() { rotateMap(Rotation.ROT_270); }
	
	/**
	 * Rotates all the tiles and events in this map according to the given rotation angle
	 * 
	 * @param rotation	The amount to rotate the map (clockwise)
	 */
	protected void rotateMap(Rotation rotation)
	{
		DataItem lowerLayer = dataItems.remove(0x47);
		DataItem upperLayer = dataItems.remove(0x48);
		byte[] baseLowerTiles = lowerLayer.getData();
		byte[] baseUpperTiles = upperLayer.getData();
		
		byte[] newLowerTiles = new byte[baseLowerTiles.length];
		byte[] newUpperTiles = new byte[baseUpperTiles.length];
		
		int width = getMapWidth();
		int height = getMapHeight();

		// Set new dimensions
		if (rotation == Rotation.ROT_90 || rotation == Rotation.ROT_270)
		{
			int newWidth = getMapHeight();
			int newHeight = getMapWidth();
			setMapHeight(newHeight);
			setMapWidth(newWidth);
		}
		
		int target = baseLowerTiles.length / 2;
		int lastMilestone = offsetMilestones[offsetMilestones.length - 1];

		// Rotate tile layers
		for (int i = 0; i < target; i++)
		{
			// Obtain destination array
			int dest;
			switch (rotation)
			{
				case ROT_90:
					dest = height * (i + 1) - (i / width) * (width * height + 1) - 1;
					break;
				case ROT_180:
					dest = width * height - 1 - i;
					break;
				case ROT_270:
					dest = height * (width - 1 - i) + (i / width) * (height * width + 1);
					break;
				default:
					dest = i;
					break;
			}
			
			// Extract data
			int data = (baseLowerTiles[2 * i] & 0xFF) + ((baseLowerTiles[2 * i + 1] & 0xFF) << 8);
			
			// Get tile's ID offset
			int tileIDOffset = 0;
			
			for (int j = 1; j < offsetMilestones.length; j++)
				if (offsetMilestones[j] <= data)
					tileIDOffset = offsetMilestones[j];
				else
					j = offsetMilestones.length;
			
			// Rotate subtile, if applicable (i.e, for water tiles)
			int oldSubtileOffset = data - tileIDOffset - (data % 50);
			int newSubtileOffset = oldSubtileOffset;
			
			if (tileIDOffset < 3000)
			{
				boolean foundOffset = false;
				
				for (int j = 0; j < subtileRotationTable.length && foundOffset == false; j++)
					if (oldSubtileOffset == subtileRotationTable[j][0])
					{
						newSubtileOffset = subtileRotationTable[j][rotation.ordinal()];
						foundOffset = true;
					}
				
				if (!foundOffset)
				{
					System.err.println("Unknown subtile offset found at tile " + i + ": " + tileIDOffset + " + " + oldSubtileOffset + 
										String.format("(%02X %02X)", baseLowerTiles[2 * i], baseLowerTiles[2 * i + 1]));
				}
			}
			else if (tileIDOffset == 2000)
			{
				oldSubtileOffset = 0;
				newSubtileOffset = 0;
			}

			// Write new data
			int newData;
			if (tileIDOffset == lastMilestone)
			{
				// If offset equals the last milestone, this is a non-tiling chip, and we don't need to rotate it
				newData = data;
			}
			else
			{
				// Otherwise, we must rotate the tile by changing its rotation offset, i.e., the remainder after the tile ID offset
				int rotationOffset = data - tileIDOffset - oldSubtileOffset;
				if (rotationOffset >= tileRotationTable.length)
				{
					System.err.println("Unknown rotation offset found at tile " + i + ": " + tileIDOffset + " + " + oldSubtileOffset + " + " 
										+ rotationOffset + String.format("(%02X %02X)", baseLowerTiles[2 * i], baseLowerTiles[2 * i + 1]));
					newData = tileIDOffset + newSubtileOffset;	
				}
				else
				{
					newData = tileIDOffset + newSubtileOffset + tileRotationTable[rotationOffset][rotation.ordinal()];
				}
				
				
			}

			newLowerTiles[2 * dest] = (byte) (0xFF & newData);
			newLowerTiles[2 * dest + 1] = (byte) (0xFF & (newData >> 8));
			
			newUpperTiles[2 * dest] = baseUpperTiles[2 * i];
			newUpperTiles[2 * dest + 1] = baseUpperTiles[2 * i + 1];
		}
		
		// Rotate event layer
		for (int i = 0; i < events.length; i++)
		{
			int eventX, eventY;
			
			switch (rotation)
			{
				case ROT_90:
					eventX = height - 1 - events[i].getYPos();
					eventY = events[i].getXPos();
					break;
				
				case ROT_180:
					eventX = width - 1 - events[i].getXPos();
					eventY = height - 1 - events[i].getYPos();
					break;
				
				case ROT_270:
					eventX = events[i].getYPos();
					eventY = width - 1 - events[i].getXPos();
					break;
				
				default:
					eventX = events[i].getXPos();
					eventY = events[i].getYPos();
					break;
			}
			
			events[i].setXPos(eventX);
			events[i].setYPos(eventY);
			
			// Rotate facing
			events[i].rotate(rotation);
		}
		
		// Add new tile layers back to dataItems and update events list
		dataItems.put(0x47, new DataItem(0x47, newLowerTiles, this));
		dataItems.put(0x48, new DataItem(0x48, newUpperTiles, this));
		updateEvents();
	}
	
	/**
	 * Writes the current map to a file in the current folder.
	 * 
	 * @param mapName	The file name for the newly saved map
	 */
	public void saveMap(String mapName)
	{
		// Update events
		updateEvents();
		
		// Create empty array of size equal to all the data
		byte[] output = super.toArray();
		
		// Save map
		String destination = folderPath + mapName;
		
		try
		{
			Files.write(Paths.get(destination), output);
			System.out.println("Saved file to " + destination);
		} catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Failed to write file to " + destination);
		}
	}
}


