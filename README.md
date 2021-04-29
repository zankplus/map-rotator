# RPG Maker 2000 Map Rotator

![maprotation](https://user-images.githubusercontent.com/12615965/116481900-1ea75700-a852-11eb-945d-cfd51e86ec9e.png)

## Program information

Uploaded here is the code I use to create rotated versions of existing maps in RPG Maker 2000. I wrote this code to let me rotate maps in RPG Maker 2000 for a project (still forthcoming at the time of writing) that would implement "chunky" map rotation in the style of _Brandish_ (1991). I made it with a focus on rotating _world_ maps, so the bulk of the effort has gone into making sure the autotiles at the top of each chipset connection properly after rotation; I have not, for instance, done the work to ensure that multi-tile or three-dimensional objects like walls and cliffs survive the rotation process (in fact, I guarantee they will be butchered). 

In light of the fairly narrow use case, and the fact that I haven't made a user-friendly executable interfact for it, I'm sharing this code primarily for instructive purposes. Although prior work exists that operates on map data (like [EasyRPG's lmu2png tool](https://github.com/EasyRPG/Tools), which I used to take the pictures up above), few resources exist online for learning how to understand the structure of RM2K map files and edit their contents. I share my code as well as my notes for this purpose.


### Notes on usage

In lieu of making a dedicated executable, I mainly use this program by editing the commands I want into the main method. That usually looks something like this:

```
public static void main(String[] args)
{
      RMMap map = new RMMap("C:\\RPG Maker 2000 workspace\\Rotating Map\\Map0010.lmu");
      map.rotateClockwise();
//	map.rotateCounterclockwise();
//	map.rotate180();
      map.saveMap("Map0011.lmu");		
}
```
Fuller explanation of these methods can be found in the files themselves, but shouldn't be necessary if all you want to do is rotate some maps. Note that simply creating a new map file in a project folder will not make RPG Maker recognize it. For the maps you save to be readable by RPG Maker, they must overwrite an existing map file. Be sure to create dummy maps to overwrite for this purpose (and be careful not to overwrite anything you care about!).

## Notes on hex editing RPG Maker 2000 map files

What follows is a summary of what I've learned about RPG Maker 2000's map files and the hex data that constitutes them. As I have only explored and documented the workings of RPGMaker map data as far as is necessary for the purposes of my own goal of rotating maps, please bear in mind that the information I've collected is incomplete and imperfect. I present the work in its present state in hopes of helping anyone interested in RM2K map hacking to orient themselves and sparing them the ugly work of reconstructing the map structure from scratch on their own.

I discovered after writing most of this that more complete information about particular object structures is available on [EasyRPG Wiki](https://wiki.easyrpg.org/). Where their tables are more thorough than my own, I link to specific pages on the Wiki for reference.

### A note on language

Understand that this is my first experience with hex editing, and as I mostly conducted this venture through trial and error without much awareness of outside sources, I had to come up with my own names for things. I don't know if there's standard terminology for the things I describe, or if the data structures I describe are commonplace in the hex editing world and don't need my redundant (or inaccurate!) explanations. My aim is to present what I learned as clearly and honestly as possible; I apologize if my choice of language interferes wtih that goal.

### Object structure

An RPGMaker map file compromises a single "object" data structure representing the map. Because the map both is an object and contains other objects within it, it's worth describing the structure of objects before examining the specifics of the map object.

An object comprises a list of what I call "data items", structures that package data meaningful to RM2K with metadata for identifying and quantifying the data. This list is prefixed by a header and suffixed by a single `00` byte marking the object's end. Data items are a bit more complex than the other two parts, so I'll briefly describe the header and end byte before explaining data items.

For the map, the header consists of 11 bytes: the first, 0A, describes the length of the string that comprises the rest of the header, `LcfMapUnit`. To the best of my knowledge there is no way to change this header, so for all intents and purposes a map's header is always exactly 11 bytes long. For other objects, like events and event pages, the header represents the numerical ID of that object in the list that contains them; this takes the form of a variable length quality (explained below), so it's usually just one or two bytes. The end-of-object byte `00` occurs after the last data item in an object and signals that there are no more data items to read in the file. It works the same way in every type of object. 

Returning to data items: a data item consists of the following values:
- An ID integer, identifying the data item's content so that the program reading it knows how to parse it and what to do with it.
- A content size integer, measuring in byte the length of the data item's content is, so that the program reading it know where the data ends (there's no end-of-object byte or equivalent for a data item).
- The data (content) itself. This can be a single number (like map width), a string (like a parallax background file name), or an object (like an event), an array of numbers (like tile data), or an array of objects (like event layer data). 

These are stored in continuous bytes in the file. For example, the the data item that stores the map's width has the ID `0x02`, so for a map 100 tiles wide, the data item will appear like this in the hex editor:

`02 01 64`

To read this, RPG Maker starts by reading the ID byte, which tells it what the data means. Then it reads the data size; in this example it's only one byte long, but it can be longer (see the section of variable length quantities below). The content size tells RM how many more bytes to read. For the tile layer data, the content might be thousands or tens of thousands of bytes long. In the present case the size value is just `0x01`, so RM identifies the next 1 byte as content. Note that none of these values tell you what the internal structure of the content is or how it should be read; that varies from item to item, and the program reading the data needs to know the item is in order to parse its data correctly. The different structures I've identified will be described below. Note that the content size integer is a variable length quantity, so while it's often 1 byte long, it *can* be longer if the data contained in the item is very large. I suspect the ID integer is a VLQ as well, but since I haven't seen an object use an ID higher than `0x51`, its size is functionally fixed at 1 byte long.

Data items make up the bulk of the map (and indeed of any object). Note that they're always stored in ascending order, at least by RPG Maker. I haven't tested whether RPG Maker will read the map correctly if *I* save items out of order, but it seems possible.


### Variable Length Quantities

Except where otherwise noted, integers are stored as variable length quantities (VLQs). My own explanation follows, but I'm mostly paraphrasing Wikipedia. See https://en.wikipedia.org/wiki/Variable-length_quantity for more details.

VLQs represent integers using a variable number of bytes, determined by the number of significant bits in the binary representation. This format allows for the full range of numbers (up to 4 bytes, I think) to be utilized while storing small positive numbers in 1-3. The tradeoff is that larger numbers (and negative numbers) can use up to 5 bytes, so VLQS provide the most compression benefit when you have a large amount of small positive numbers with only a handful of large positives and/or negatives mixed in. Fortunately, this is how RPG Maker 2000 map files are.

A VLQ can store up to 7 bits of data per byte. These are the *last* 7 bits of each byte. The *first* bit tells you whether this is the last byte of the integer. If it's a 1, keep reading: the next byte is part of the VLQ, too. If it's a 0, you've reached the end and needn't read any more bytes. To extract the integer value from a VLQ, just put together the last 7 bits of each digit (it's big-endian, so the leftmost bits are the most significant). For example, if your VLQ is A2 B9 57, your bytes look like this:

```
Hex	Binary		Length bit	Content bits	Combined and padded to 4 bytes			
A0	1010 0010	1	 	010 0010	1000 1001 1100 1100 0111
B9	1011 0011	1		011 1001
47	0100 0111	0		100 0111
```

So the sequence A2 B9 57 represents the value `10001001110011000111`, or 564,423. RM2K reads these as 4 byte signed integers, which means that the largest possible value, `8F FF FF FF 7F`, which represents `11111111 11111111 11111111 11111111`, is read as -1. That's a bit unwieldy, but it doesn't come up too often. The only place I've seen negative numbers in a map file is in the parallax background auto scroll speed setting.


### Reading different items

So far I've described how to extract data from RM2K maps, but not how to read that data itself. The data item's ID tells RPG Maker what that data *is*, but not how to interpret it. That information isn't encoded in the object or in the map file but in RPG Maker itself. That means it's up to us to figure out what each ID means and how to read it through trial and error. The table below, which is surely incomplete, describes what each ID I've encountered in the map object represents and how its content is structured. (For more information, see the [LcfMapUnit page on EasyRPG Wiki](https://wiki.easyrpg.org/development/data-structure-reference/lcfmapunit)).

```
ID      Data type       Purpose
0x01	Integer 	Chipset
0x02	Integer 	Map width
0x03	Integer 	Map height
0x0B	Integer 	Scroll type (i.e., how the map loops into itself)
0x1F	Boolean 	Parallax background enabled
0x20	String		Parallax background name (doesn't seem to include filename extension)
0x21	Boolean 	Parallax background horizontal loop enabled
0x22	Boolean 	Parallax background vertical loop enabled
0x23	Boolean 	Parallax background horizontal scroll enabled
0x24	Integer		Parallax background horizontal scroll speed
0x25	Boolean 	Parallax background vertical scroll enabled
0x26	Integer		Parallax background vertical scroll speed
0x47	Integer array	Lower layer tile data
0x48	Integer array	Upper layer tile data
0x51	Object list	Event layer data
0x5B	Integer		Number of times the map has been saved (starts at 02)
```

Note that some of these values may be absent. Notably, map width and height are only stored in the data if their values differ from the default 20 and 15 (respectively). If RM2K reads a map file and these values are absent, it assumes these default values. Similarly, if the map is saved with the first tileset listed in the database, the value of `0x01` is implicit and the item is omitted. Note also that some map information is not stored in the map file at all. The map name and the encounters, BGM, battle background, teleport, escape, and save sections of the map properties window are stored in RPG_RT.lmt per [EasyRPG Wiki](https://wiki.easyrpg.org/development/data-structure-reference/lcfmaptree).

Integer values are read as VLQs. Booleans are read the same way as integers, but (as is standard) only take the values 0 (false) and true (1). I haven't played with strings at all but they appear to be a series of ASCII characters with no extra metadata attached. For example, the data item `20 07 43 6F 73 6D 7F 73 31`, representing the map's chosen parallax background graphic, has the ID `20` and size `07`, with the next 7 values representing the text `Cosmos1` (the name of the graphic, minus its filename extension) in ASCII. That's the whole data item.

Integer arrays will be explained more fully in the "Reading Lower Layer Data" section.

Object list are used for storing event data on the map, or page data in events. They begin with a VLQ describing the number of objects in the list, then proceeds to list objects in their entirety in order of their IDs (which constitutes their header). RPG Maker stops reading an object list once it has read the indicated number of objects; there is no end-of-object byte for lists (although individual objects have them, which can be misleading).


### Reading lower layer map data

This is the most complicated part of the file. Pour yourself a glass of water and settle in.

Lower layer tile data is a list of 2-byte integers stored in little-endian format (that is, the the least significant bits come first, so the number 1000 or `0x03E8` is stored as E8 03). The list's length is equal to the total number of tiles on the map, times two since each value is two bytes long. So for a 20x15 map, the content of the lower layer tile data item should be 20 x 15 x 2 or 600 bytes long, and the content size value should read [84 58] accordingly. Each 2-byte integer tells RPG Maker what tile to draw at the corresponding space. They're stored from left to right, then top to bottom, so the first two bytes represent the tile at map location (0, 0), the second is (1, 0), and so on. 

This is where things start to get tricky. To understand the way tiles are stored in memory, we should first acknowledge that there are two types of tiles: those that autotile (i.e, transform to show smooth connections to their neighbors), and those that don't. In the tile selection window of RPG Maker 2000's map editor, the first three tiles of the first row, and the entirety of the second and third rows, are all autotiling tiles (or just 'autotiles'). Everything after that does not automatically tile. Autotiles have more values associated with them, because they can take on more shapes depending on which tiles are next to them. Non-autotiling tiles only have one value associated with them, because they don't change depending on their neighbors. An non-autotiling tile always looks the same.

(Also worth mentioning is that the first row of tiles are all animated. The animated tiles store a little extra information, but since they're all saved in the first frame of animation, we can ignore that. At any rate, since it hasn't been relevant to my own project of map rotation, I don't have any information about how the animations are stored.)

Each 2-byte integer stores up to 3 pieces of information: a tile ID offset, a subtile offset, and a connections offset. All tiles have a tile ID offset; autotiling tiles include a connections offset, and water tiles, which have even more complex connections than other autotiles, employ a subtile offset. The integer stored in the data item represents the sum of all three of these. (For tiles that don't have a subtile offset or a connections offset, treat these values as 0.)

First, let's talk about tile ID offset. Each distinct tile in the tile selection window has its own unique tile offset. These are unevenly spaced because, as noted previously, autotiling tiles can take more distinct values than non-autotiling ones. Here's how the tile IDs correspond to tiles:

```
Offset	Hex	Location in selection window	Appearance in 'Basis' chipset
0	0x00	Row 1, column 1			Water tile 1 (Shallow water/grassland coast)
1000	0x03E8	Row 1, column 2			Water tile 2 (Shallow water/snowfield coast)
2000	0x07D0	Row 1, column 3			Deep water tile (Deep water)
3000	0x0BB8	Row 1, column 4			Animated tile 1 (Waterfall
3050	0x0BEA	Row 1, column 5			Animated tile 2 (Whirlpool
3100	0x0C1C	Row 1, column 6			Animated tile 3 (Ice block
4000	0x0FA0	Row 2, column 1			Autotile 1 (Grassland)
4050	0x0F2D	Row 2, column 2			Autotile 2 (Grassier grassland)
4100	0x1004	Row 2, column 3			Autotile 3 (Forest)
4150	0x1036	Row 2, column 4			Autotile 4 (Mountain)
4200	0x1068	Row 2, column 5			Autotile 5 (Wasteland)
4250	0x109A	Row 2, column 6			Autotile 6 (Desert)
4300	0x10CC	Row 3, column 1			Autotile 7 (Swamp)
4350	0x10FE	Row 3, column 2			Autotile 8 (Snowfield)
4400	0x1130	Row 3, column 3			Autotile 9 (Snowy forest)
4450	0x1162	Row 3, column 4			Autotile 10 (Snowy mountain)
4500	0x1194	Row 3, column 5			Autotile 11 (Purple void)
4550	0x11C6	Row 3, column 6			Autotile 12 (Black void)
5000	0x1388	Row 4, column 1			Simple tile 1 (Red floor)
5001	0x1389	Row 4, column 2			Simple tile 2 (Green floor)
...
5143	0x1417	Row 27, column 6		Simple tile 144 (last non-tiling tile)
```

So far so good. 

The autotiles (including the water tiles) utilize a "connections offset" that facilitates autotiling behavior. The connections offset tells RPG Maker which directions adjacent tiles of the same identity can be found in, so that the map drawer can choose a tile that depicts seamless connections with tiles in those directions. Although the chipset file only shows 12 tiles for each (non-water) autotile, RM2K can depict more than 12 different connections, splicing together different tiles from the chipset file to product more distinct connections. RPGMaker considers connections to exist in binary (either there is an adjacent tile of the same identity in this direction, or there isn't), and examines the 4 cardinally adjacent tiles as well as the 4 diagonally adjacent ones to determine where to draw these connections. From this you might expect there to be a total of 256 (2^8) possible connections, but in practice only 46 are considered. This is because a diagonal connection is only considered if an orthogonal connection exists on both sides of it. As an example, a forest tile with no adjacent forest tiles will show an isolated forest - no connections. With a forest tile to the north of it, it will show one connection (to the north). With forest tiles in the north and northeast positions, it will still only show show the one northerly connection, because an orthogonal connection only exists on one side of the diagonal one. But with forest tiles in the north, northeast, and east positions, it will show connections in all three corresponding directions. The tile displayed in this case is distinct from the tile that's displayed when there are forests only in the north and east positions. The visual difference is subtle, but noticeable - and important for my own project of rotating maps.

Each valid connections offset corresponds to a set of directional connections (and, it could be said, an inverse set of blocked connections). There is not, as far as I know, a mathematical correspondance between the offset and the connection set it represents (although the regularity of the first 16 values can make it look that way). The table below details which connection set corresponds to each offset.

```
        Connections blocked		Connections allowed
ID	MAJOR		MINOR		MAJOR		MINOR
00	None		None		S E N W 	SW SE NE NW 
01	None		         NW	S E N W 	SW SE NE 
02	None		      NE	S E N W 	SW SE    NW 
03	None		      NE NW	S E N W 	SW SE 
04	None		   SE		S E N W 	SW    NE NW 
05	None		   SE    NW	S E N W 	SW    NE 
06	None		   SE NE	S E N W 	SW       NW 
07	None		   SE NE NW	S E N W 	SW 
08	None		SW		S E N W 	   SE NE NW 
09	None		SW       NW	S E N W 	   SE NE 
0A	None		SW    NE	S E N W 	   SE    NW 
0B	None		SW    NE NW	S E N W 	   SE 
0C	None		SW SE		S E N W 	      NE NW 
0D	None		SW SE    NW	S E N W 	      NE 
0E	None		SW SE NE	S E N W 	         NW 
0F	None		SW SE NE NW	S E N W 	None
10	      W		SW       NW	S E N 		   SE NE 
11	      W		SW    NE NW	S E N 		   SE 
12	      W		SW SE    NW	S E N 		      NE 
13	      W		SW SE NE NW	S E N 		None
14	    N		      NE NW	S E   W 	SW SE 
15	    N		   SE NE NW	S E   W 	SW 
16	    N		SW    NE NW	S E   W 	   SE 
17	    N		SW SE NE NW	S E   W 	None
18	  E		   SE NE	S   N W 	SW       NW 
19	  E		   SE NE NW	S   N W 	SW 
1A	  E		SW SE NE	S   N W 	         NW 
1B	  E		SW SE NE NW	S   N W 	None
1C	S		SW SE		  E N W 	      NE NW 
1D	S		SW SE    NW	  E N W 	      NE 
1E	S		SW SE NE	  E N W 	         NW 
1F	S		SW SE NE NW	  E N W 	None
20	  E   W		SW SE NE NW	S   N 		None
21	S   N		SW SE NE NW	  E   W 	None
22	    N W		SW    NE NW	S E 	   	   SE
23	    N W		SW SE NE NW	S E 		None
24	  E N		   SE SE NW	S     W 	SW
25	  E N		SW SE NE NW	S     W 	None	
26	S E		SW SE NE	    N W 	         NW
27	S E		SW SE NE NW	    N W 	None
28	S     W		SW SE    NW	  E N 		      NE
29	S     W		SW SE NE NW	  E N 		None
2A	  E N W		SW SE NE NW	S 		None
2B	S   N W		SW SE NE NW	  E 		None
2C	S E   W		SW SE NE NW	    N 		None
2D	S E N		SW SE NE NW	      W 	None
2E	None		None		None		None
```

Lastly, the subtile offset is used by water tiles to indicate a second layer of connections. The first two tiles in the selection window represent shallow water, and these behave a little differently than the third, which represents deep water. The autotiling of shallow water is blocked in two different ways: by deep water, and by land (any non-animated tile). (With other shallow water, or with the animated tiles, shallow water appears to choose connections as though they shared a tile identity.) Connections are drawn wherever tiling is not blocked. For shallow water tiles, the connection offset describes connections blocked by land; the job of the subtile offset is to describe connections blocked by deep water. Because these offsets are spaced `0x32` (50) values apart like tile ID offsets for autotiles, I imagine these offsets represent new "base" tiles formed from shallow and deep water tiles, which have their terrestrial connections added on top of them - and this I call them subtiles.

Unlike the primary connections described by the connection offsets, these secondary connections only occur in cardinal directions. Moreover, where connections occur on opposite sides, the base tile (subtile offset 0) is used as though there were four connections. This means there are only a handful of possible subtile offsets - one for 0 connections, 4 for single connections, 4 for non-opposite double connections, and one for all other cases.

```
ID	Decimal	Blocked		Connected
0	0	None		W N E S
0032	50	W N		    E S
0064	100	  N E 		W     S
0096	150	W N E		      S
00C8	200	W     S		  N E
00FA	250	W N   S		    E
0190	400	    E S		W N
01F4	500	  N E S		W
0825	600	W N   S		    E
02EE	750	W N E S		None
```

It is possible that other subtile IDs exist, but I have yet to find a way to generate them for shallow water tiles.

Deep water tiles appear to work similarly, but I'm less clear on the particulars of their mechanics or how their data is expressed visually. What I do know is that the deep water tile uses the connections offset to display coastlines in the same way as shallow water, and that for the purposes of my own project of map rotation, assuming it uses the subtile offset in the same way has not resulted in any obvious visual glitches (though I have while making this assumption encountered a couple of unfamiliar subtile offsets for deep water that I haven't investigated yet).


### Reading upper layer map data

The upper layer stores its tile data in the same format as the lower layer; it too uses 2-byte integers to represent tile data and, predictably, stores that data in an array of the same length as the lower layer's array. Fortunately, upper layers tile representation is much simpler. They don't autotile or animate, so each one can only take one value. They begin at `0x2710` (10000) and proceed one at a time up to `0x279F` (10143), representing the last tile in the chipset. 


### Reading event layer map data

The basic structure of the event layer data is simple: it's an array of objects as described under the _Reading different items_ heading. As events are objects, they aren't wholly different in structure from the map; the main differences are that the header of an event just contains the event's ID (the same Event ID displayed at the top of RM2K's event editor window) and that IDs of its data items mean different things than the IDs of the map's data items. These IDs are summarized below (with a little more detail available on [EasyRPG Wiki](https://wiki.easyrpg.org/development/data-structure-reference/lcfmapunit/map-event)):


```
ID      Data type       Purpose
01	String		Item 
02	Integer		X-position
03	Integer		Y-position
05	Object list     Page data
```

This picture is complicated in practice by the fact that most of the event's content is located in its page data. This should be unsurprising for most RM2K users: most information about an event is set on a per page basis. It just means that a lot of essential event data, like what the event does or what it looks like, is buried one layer deeper than simple identifying information. Page data exists as a list inside the event object in much the same way that event data exists as a list inside of the map object. I've summarized my own findings below, but a more detailed information is available on [this page at EasyRPG Wiki](https://wiki.easyrpg.org/development/data-structure-reference/lcfmapunit/map-event/event-page).

```
ID      Data type       Purpose
02	Object	        Conditions (not yet documented)
15	String	        Charset name
16	Integer	        Position within charset (0-7)
17	Integer	        Default facing (up = 0, right = 1, down = 2, left = 3)
19	Unknown	        Unknown (19-33 generally stuff in the bottom left part of the event editor)
1F	Unknown	        Unknown
20	Unknown	        Unknown
21	Unknown	        Unknown
22	Unknown	        Unknown
23	Unknown	        Unknown
24	Unknown	        Unknown
29	Unknown	        Unknown
33	Unknown	        Unknown
34	Unknown	        Event script
```

## Contact
Again, feel free to contact me by email at _claymcooper_ at _gmail dot _com_ if you have any questions about my code or my notes or about hex editing RM2K maps. You can also reach me on discord at _lizard_ (number sign) _6588_. My brain usually fills up with stuff pretty fast, so if you're contacting me more than a couple months after publishing this (it's the end of April, 2021 as I write this) I might not have all the answers, but I'll still be happy to give it a shot.
