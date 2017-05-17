package com.hotmail.janie177.gametest.engine.world.chunks;
import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.utils.ViewFrustum;
import com.hotmail.janie177.gametest.engine.world.World;
import com.hotmail.janie177.gametest.engine.world.blocks.Block;
import com.hotmail.janie177.gametest.engine.world.blocks.BlockFace;
import com.hotmail.janie177.gametest.engine.world.saving.WorldLoader;
import com.hotmail.janie177.gametest.game.main.Options;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.hotmail.janie177.gametest.engine.world.chunks.ChunkHelper.calculateLocalPosition;

public class Chunk
{
	//X Y and Z locations
	//Chunks are 16x16 and 512 height.
	//Every location in a chunk represents a block. Each block has an ID and a data value.
	//To get a specific block, use data[x][z][y][0,1,2] where 0 is ID, 1 is data value.

	//0 - ID. This is stored -128 so this is added on top of it afterwards.
	//1 - The Data Value. Contains extra info about a block which will be used by the renderer.
	//2 - Transparency. Can you see through this block? 0 for solid, 1 for transparent.
	//3 - IsVisible. Is this block being rendered? 0 for invisible, not 0 for visible.
	//To allow for more ID's, all ID's will be -128 so that the - part of the byte is used as well
	//Every chunk exists out of 32 16x16x16 subChunks. If a subchunk only contains air, it will be null.
	private byte[][][][][] data = new byte[32][][][][];

	//All blocks listed by their unique coordinate name.
	private Set<Block> blocks;
	private Set<Block> visibleBlocks;
	private Set<Block> specialBlocks;



	//Location in the world.
	private int x;
	private int z;
	private float worldX;
	private boolean neverUnload = false;
	private float worldZ;
	private World world;
	private UUID uuid;

	private boolean needsNewMesh = true;

	private long lastQueueTime = 0;

	private boolean isLoading = true;
	private boolean isUnloading = false;
	private boolean isUpdating = false;

	private ChunkMesh chunkMesh;

	/**
	 * Create a new chunk at coordinate x and z.
	 * @param x The world coordinate X.
	 * @param z The world coordinate Y.
	 */
	public Chunk(int x, int z, World world)
	{
		this.uuid = UUID.randomUUID();
		this.x = x;
		this.z = z;
		this.worldX = ChunkHelper.calculateWorldPosition(x);
		this.worldZ = ChunkHelper.calculateWorldPosition(z);
		this.world = world;

		chunkMesh = new ChunkMesh(this);
	}

	/**
	 * Set if this chun requires a new mesh before being rendered.
	 * @param needsNewMesh
	 */
	public void setNeedsNewMesh(boolean needsNewMesh)
	{
		this.needsNewMesh = needsNewMesh;
	}

	public boolean needsNewMesh()
	{
		return needsNewMesh;
	}

	/**
	 * Add a block to the map of blocks that will be rendered.
	 * @param b The block to add which will then be visible.
	 */
	public void addToVisibleBlockList(Block b)
	{
		visibleBlocks.add(b);
	}

	/**
	 * Add a block to the list of special blocks that are always visible.
	 * @param b
	 */
	public void addToSpecialBlocksList(Block b)
	{
		specialBlocks.add(b);
	}

	/**
	 * Remove a block from the list of special blocks.
	 * @param b
	 */
	public void removeFromSpecialBlocksList(Block b)
	{
		if(specialBlocks.contains(b))
		{
			specialBlocks.remove(b);
		}
	}

	/**
	 * Remove a block from the map of blocks that will be rendered.
	 * @param b The block to remove which will no longer be visible.
	 */
	public void removeFromVisibleBlockList(Block b)
	{
		if(visibleBlocks.contains(b)) visibleBlocks.remove(b);
	}

	public void removeFromBlocksList(Block b)
	{
		if(blocks.contains(b))blocks.remove(b);
	}


	/**
	 * This will calculate the visibility for all blocks in the chunk.
	 */
	public void calculateVisibility()
	{
		setUpdating(true);

		//Calculate the visibility for all the blocks. This does NOT add them to the lists yet, since those need to be created now.
		blocks.stream().forEach(Block::calculateInitialVisibility);

		setNeedsNewMesh(true);
		setUpdating(false);
	}

	public void initializeVisibilitySets()
	{
		//The size counters.
		int specialSize = 0;
		int visibleSize = 0;

		//Count the amount of each category of visible block.
		for(Block b : blocks)
		{
			if(b.isSpecial()) specialSize++;
			else if(b.isVisible()) visibleSize++;
		}

		//Create the sets with their correct initial block count.
		visibleBlocks = ConcurrentHashMap.newKeySet(visibleSize);
		specialBlocks = ConcurrentHashMap.newKeySet(specialSize);

		//Fill the sets, which will already have the correct amount of space. This reduces the amount of disposed arrays to 0.
		for(Block b : blocks)
		{
			if(b.isSpecial())specialBlocks.add(b);
			else if(b.isVisible()) visibleBlocks.add(b);
		}
	}

	/**
	 * Get the chunks X coordinate, which equals the world coordinate/16, -1 if negative.
	 * @return The location of the chunk X.
	 */
	public int getChunkX()
	{
		return x;
	}

	/**
	 * Get the chunks Y coordinate, which equals the world coordinate/16 -1 if negative.
	 * @return the location of the chunk Y.
	 */
	public int getChunkZ()
	{
		return z;
	}


	/**
	 * Get the path this chunks data is stored to.
	 * @return
	 */
	public String getPath()
	{
		return Options.FILE_PATH + File.separator + "saves" + File.separator + world.getName() + File.separator + "chunks" + File.separator + x + "_" + z + File.separator;
	}

	/**
	 * Get the file name for this chunk.
	 * @return A string that should be used to load and save this chunk.
	 */
	public String getFileName()
	{
		return x + "_" + z + ".dat";
	}


	/**
	 * Load data from a chunk file into the array.
	 * @param data The data to load.
	 */
	public void loadData(byte[][][][][] data)
	{
		this.data = data;

		//After all visible blocks are calculated, add the blocks to the blocks list. Does not create for air blocks.
		fillBlocks();
	}

	/**
	 * Get a neighbouring chunk.
	 * @param face The face of the neighbour.
	 * @return The chunk at the given face, or null if unloaded.
	 */
	public Chunk getBorderingChunk(ChunkFace face)
	{
		return world.getChunk(getChunkX() + face.getOffsetX(), getChunkZ() + face.getOffsetZ());
	}

	/**
	 * Make block faces visible that now face a null since the chunk was unloaded.
	 */
	public void calculateNeighbourBorderOnUnload()
	{
		for(ChunkFace face : ChunkFace.values())
		{
			Chunk neighbour = getBorderingChunk(face);
			if(neighbour != null && !neighbour.isUnloading() && !neighbour.isLoading)
			{
				neighbour.setUpdating(true);

				if(face == ChunkFace.WEST)
				{
					for(Block b : neighbour.getBlocks())
					{
						if(b.getLocalX() == 15)
						{
							b.calculateVisibilityForFace(BlockFace.EAST, null);
						}
					}
				}
				else if(face == ChunkFace.EAST)
				{
					for(Block b : neighbour.getBlocks())
					{
						if(b.getLocalX() == 0)
						{
							b.calculateVisibilityForFace(BlockFace.WEST, null);
						}
					}
				}
				else if(face == ChunkFace.SOUTH)
				{
					for(Block b : neighbour.getBlocks())
					{
						if(b.getLocalZ() == 15)
						{
							b.calculateVisibilityForFace(BlockFace.NORTH, null);
						}
					}
				}
				else if(face == ChunkFace.NORTH)
				{
					for(Block b : neighbour.getBlocks())
					{
						if(b.getLocalZ() == 0)
						{
							b.calculateVisibilityForFace(BlockFace.SOUTH, null);
						}
					}
				}

				neighbour.setNeedsNewMesh(true);
				neighbour.setUpdating(false);
			}
		}
	}

	/**
	 * Calculate the visibility for all blocks neighbouring another chunk.
	 */
	public void calculateBorderBlockVisibility()
	{
		List<Chunk> alteredChunks = getWorld().getChunksInRegion(worldX, worldZ, worldX + 16, worldZ + 16, true, true);

		for(Block b : blocks)
		{
			//WEST -1 x
			if(b.getLocalX() == 0)
			{
				Block neighbour = b.getNeighbouringBlock(BlockFace.WEST);
				b.calculateVisibilityForFace(BlockFace.WEST, neighbour);
				if(neighbour != null)
				{
					neighbour.calculateVisibilityForFace(BlockFace.EAST, b);
				}
			}
			//South -1 z
			if(b.getLocalZ() == 0)
			{
				Block neighbour = b.getNeighbouringBlock(BlockFace.SOUTH);
				b.calculateVisibilityForFace(BlockFace.SOUTH, neighbour);
				if(neighbour != null)
				{
					neighbour.calculateVisibilityForFace(BlockFace.NORTH, b);
				}
			}
			//EAST +1 x
			if(b.getLocalX() == 15)
			{
				Block neighbour = b.getNeighbouringBlock(BlockFace.EAST);
				b.calculateVisibilityForFace(BlockFace.EAST, neighbour);
				if(neighbour != null)
				{
					neighbour.calculateVisibilityForFace(BlockFace.WEST, b);
				}
			}
			//North +1 z
			if(b.getLocalZ() == 15)
			{
				Block neighbour = b.getNeighbouringBlock(BlockFace.NORTH);
				b.calculateVisibilityForFace(BlockFace.NORTH, neighbour);
				if(neighbour != null)
				{
					neighbour.calculateVisibilityForFace(BlockFace.SOUTH, b);
				}
			}
		}

		//Set all chunks altered back to not updating status, and make them regenerate their mesh.
		for(Chunk c : alteredChunks)
		{
			c.setUpdating(false);
			c.setNeedsNewMesh(true);
		}
	}

	/**
	 * Get all data from this chunk.
	 * @return The data in this chunk.
	 */
	public byte[][][][][] getData()
	{
		return data;
	}


	/**
	 * Get a block by its world coordinates.
	 * Make sure this block is inside this chunk.
	 *
	 * @param x The X coordinate in the world.
	 * @param y The Y coordinate in the world.
	 * @param z The Z coordinate in the world.
	 * @return The block at the given location. Will create a new air block if it does not exist.
	 */
	public Block getBlock(float x, float y, float z)
	{
		if(specialBlocks != null)
		{
			//The block exists and it not air, returning the actual block object.
			Optional<Block> block = specialBlocks.stream().filter(b -> b.getX() == x && b.getY() == y && b.getZ() == z).findFirst();
			if(block.isPresent())
			{
				return block.get();
			}
		}

		//The block does not exist at this point, so a new dummy will be created.

		int subChunk = (int)y/16;

		//The subChunk was only air blocks, so it will be created so it can be altered.
		if(data[subChunk] == null)
		{
			data[subChunk] = ChunkHelper.createNewSubChunk();
		}

		//Return a newly created block which can then be altered, automatically affecting the given blocks data.
		return createBlock(subChunk, calculateLocalPosition(x), calculateLocalPosition(y),calculateLocalPosition(z));

	}

	/**
	 * Create a block object from the relative coordinates.
	 * @param x The x coordinate relative to the chunk. (0-15).
	 * @param y The y coordinate relative to the chunk. (0-15).
	 * @param z The z coordinate relative to the chunk. (0-15).
	 * @return A new Block object.
	 */
	public Block createBlock(int subChunk, int x, int y, int z)
	{
		//Get the data for the given block. y/16 equals the subchunk and y%16 equals the y position within that subchunk.
		byte[] ids = data[subChunk][x][y][z];
		//Create a new instance of block, which contains a refference to the block data. Also the coordinates are the world coordinates.
		return new Block(ids, worldX + x, (subChunk * 16 + y), worldZ + z, this, x, y, z, world);
	}

	/**
	 * Create block objects and store them.
	 */
	public void fillBlocks()
	{

		byte[][][][] subChunk;


		//Create an array with enough space for exactly one subchunk (16x16x16). This is then filled up every time for every subchunk.
		//It may not always reach its max value, because air blocks are not added.
		//Once its done looping, index 0 to <index_of_blocks_added> are added to the blocks set.
		//Normally, blocks would be added one by one. This would result in an array being throws away every single time, to make space for a new block.
		//With this system, that's not the case. Instead there's just this one array which is thrown away at the end, and an array for each subchunk.
		//Every subchunk will cause one array to become redundant because the size is not known beforehand, but it lowers the amount to 33 from tens of thousands.
		Block[] tempBlockHolder = new Block[4096];
		int tempIndex = 0;

		//Create the new blocks set, which will then fill up to the exact number defined. This saves about a billion new array allocations.
		blocks = ConcurrentHashMap.newKeySet();

		for(int s = 0; s < 32; s++)
		{
			if((subChunk = data[s]) != null)
			{
				for(int x = 0; x < 16; x++)
				{
					for(int y = 0; y < 16; y++)
					{
						for(int z = 0; z < 16; z++)
						{
							byte[] blockData = subChunk[x][y][z];

							//Exclude air blocks.
							if(blockData[0] != -128)
							{
								Block b = createBlock(s, x, y, z);
								//Save the new block temporarily.
								tempBlockHolder[tempIndex] = b;
								tempIndex++;
							}
						}
					}
				}
				blocks.addAll(Arrays.asList(Arrays.copyOfRange(tempBlockHolder, 0, tempIndex)));
				tempIndex = 0;
			}
		}

		//Set the array to null so garbage collection can do its thing.
		tempBlockHolder = null;
	}

	/**
	 * Returns wether this chunk should be rendered for the player.
	 * @return True if the player can see the chunk.
	 */
	public boolean isChunkVisible() {
		return (Math.abs(TestGame.player.getLocation().getChunkX() - getChunkX()) < 2 && Math.abs(TestGame.player.getLocation().getChunkZ() - getChunkZ()) < 2) || ViewFrustum.chunkIsVisible(getChunkX(), getChunkZ());
	}

	/**
	 * Get all blocks in this chunk.
	 * @return All blocks in this chunk that are not air.
	 */
	public Set<Block> getBlocks()
	{
		return blocks;
	}

	/**
	 * Return all blocks that are marked visible.
	 * @return All blocks that are visible.
	 */
	public Collection<Block> getVisibleBlocks()
	{
		return visibleBlocks;
	}

	/**
	 * Get all special blocks in this chunk.
	 * @return A collection of blocks, which only contains special blocks.
	 */
	public Collection<Block> getSpecialBlocks()
	{
		return specialBlocks;
	}

	/**
	 * Save this chunk to a file.
	 */
	public void saveChunk(boolean saveEntities)
	{
		//Don't save if the chunk is still loading.
		if(isLoading) return;

		//Save the chunk to file, and save entities too if this was called on an unload.
		WorldLoader.saveChunk(this, saveEntities);
	}

	/**
	 * Unload the chunk if it's not marked for eternal loading.
	 * @return Returns if the chunk was unloaded.
	 */
	public boolean unload()
	{
		//Do not unload if the chunk was marked as eternal.
		if(neverUnload) return false;

		//Do not unload this chunk if it's being edited by another process.
		if(isUpdating()) return false;

		//Mark this chunk as unloading, so that the world save task and quitting the game wont cause errors.
		setUnloading(true);

		//If the chunk has not been marked eternal
		removeAirOnlySubChunks();

		//Save the chunk.
		saveChunk(true);

		//No longer list the chunk as loaded.
		world.removeChunkFromLoadedChunks(this);

		//Nullify refferences to the chunk
		blocks.stream().forEach(Block::nullify);

		//Clear all maps.
		blocks.clear();
		visibleBlocks.clear();
		specialBlocks.clear();

		//Clear data
		data = null;

		//Fill in the edges of blocks that now face an unloaded chunk.
		calculateNeighbourBorderOnUnload();

		return true;
	}

	/**
	 * Replace all empty subchunks with null to save memory.
	 */
	public void removeAirOnlySubChunks()
	{
		if(data == null) return;

		//Remove redundant air-only subchunks.
		byte subId = 0;
		for(byte[][][][] subChunk : data)
		{
			//Check if this subChunk is empty
			if(ChunkHelper.subChunkIsEmpty(subChunk))
			{
				//Delete the data of this chunk since it has nothing in it.
				data[subId] = null;
			}
			//Increment the subId counter.
			subId++;
		}
	}

	/**
	 * See if this chunk has been marked for never unloading.
	 * @return
	 */
	public boolean neverUnload() {
		return neverUnload;
	}

	/**
	 * Set this chunk to never unload.
	 * @param neverUnload Wether to never unload, or normally unload this chunk when it's not being used.
	 */
	public void setNeverUnload(boolean neverUnload)
	{
		this.neverUnload = neverUnload;
	}

	/**
	 * Refference to the world this chunk belongs to.
	 * @return
	 */
	public World getWorld()
	{
		return world;
	}

	/**
	 * Get the UUID for this chunk. This is stored in block render data to keep track of chunk unloading.
	 * @return
	 */
	public UUID getUuid()
	{
		return uuid;
	}

	/**
	 * See if the chunk is loading, and should not be edited by any processes.
	 * @return True if the chunk is still loading.
	 */
	public boolean isLoading()
	{
		return isLoading;
	}

	/**
	 *
	 * @param loading
	 */
	public void setLoading(boolean loading)
	{
		this.isLoading = loading;
	}

	/**
	 * See if this chunk is unloading.
	 * @return If this chunk has been marked as unloading.
	 */
	public boolean isUnloading()
	{
		return isUnloading;
	}

	/**
	 * Mark this chunk as unloading, or mark it as no longer unloading.
	 * @param unloading To unload or not to unload.
	 */
	public void setUnloading(boolean unloading)
	{
		isUnloading = unloading;
	}


	/**
	 * Get when this chunk was last queued for loading and unloading.
	 * @return The time when this chunk was last queued in long format.
	 */
	public long getLastQueueTime()
	{
		return lastQueueTime;
	}

	/**
	 * See if this chunk is currently being updated and accessed by another process.
	 * @return True if this chunk is currently being modified.
	 */
	public synchronized boolean isUpdating()
	{
		return isUpdating;
	}

	/**
	 * Set this chunk to updating or not, to stop other processed from accessing it.
	 * @param updating
	 */
	public synchronized void setUpdating(boolean updating)
	{
		this.isUpdating = updating;
	}

	/**
	 * Set the last time this chunk was queued.
	 */
	public void setLastQueueTime()
	{
		this.lastQueueTime = System.currentTimeMillis();
	}

	public ChunkMesh getChunkMesh()
	{
		return chunkMesh;
	}

	/**
	 * Get the amount of vertex points
	 * @return The amount of floats needed to represent this chunk.
	 */
	public int[] getFaceCount()
	{
		int[] counts = new int[2];
		for(Block b : visibleBlocks)
		{
			if(b.isTransparent())
			{
				counts[1]+=(b.getVisibleFacesCount());
			}
			else
			{
				counts[0]+=(b.getVisibleFacesCount());
			}
		}
		return counts;
	}



	public Optional<Block> getOptionalBlock(int worldX, int worldY, int worldZ, Set<Block> setToSearch)
	{
		return setToSearch.stream().filter(b -> b.getX() == worldX && b.getY() == worldY && b.getZ() == worldZ).findFirst();
	}
}
