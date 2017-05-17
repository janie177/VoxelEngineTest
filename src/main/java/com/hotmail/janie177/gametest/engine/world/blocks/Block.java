package com.hotmail.janie177.gametest.engine.world.blocks;

import com.hotmail.janie177.gametest.engine.world.Location;
import com.hotmail.janie177.gametest.engine.world.World;
import com.hotmail.janie177.gametest.engine.world.chunks.Chunk;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Block
{
	private byte[] blockData;

	private Location location;

	private World world;

	private BlockType type;

	private byte localY;
	private byte localZ;
	private byte localX;

	private UUID chunkUUID;

	private Chunk chunk;

	public Block(byte[] blockData, float x, float y, float z, Chunk chunk, int localX, int localY, int localZ, World world)
	{
		this.blockData = blockData;
		this.chunk = chunk;
		this.chunkUUID = chunk.getUuid();
		this.localX = (byte) localX;
		this.localY = (byte) localY;
		this.localZ = (byte) localZ;
		this.location = new Location(x, y, z, world);
		this.world = world;
		this.type = BlockType.fromID(getRealID());
	}

	public boolean isBlockFaceVisible(BlockFace face)
	{
		return ((blockData[3] >> face.ordinal()) & 1) == 1;
	}

	public void setBlockFaceVisible(BlockFace face)
	{
		blockData[3] = (byte)(blockData[3] | (1 << face.ordinal()));
	}

	public void setBlockFaceInvisible(BlockFace face)
	{
		blockData[3] = (byte) (blockData[3] & ~(1 << face.ordinal()));
	}

	private void setAllBlockFacesInvisible()
	{
		blockData[3] = 0;
	}

	private void setAllFacesVisible()
	{
		blockData[3] = 63;
	}

	public void calculateVisibilityForFace(BlockFace face, Block neighbour)
	{
		//Special blocks with custom models are always visible.
		if(type.isSpecial())
		{
			blockData[3] = 63;
			chunk.addToSpecialBlocksList(this);
			chunk.removeFromVisibleBlockList(this);
			return;
		}
		//Remove the block from the special blocks list just in case.
		else
		{
			chunk.removeFromSpecialBlocksList(this);
		}

		//Set the block to be invisible if its an air block. Always.
		if (isAir())
		{
			blockData[3] = 0;
			chunk.removeFromVisibleBlockList(this);
			return;
		}

		//Neighbour is null, so the block face should be visible just in case. Can always be changed afterward.
		if(neighbour == null)
		{
			setBlockFaceVisible(face);
		}

		//Special neighbour so block is visible.
		else if(neighbour.isSpecial())
		{
			setBlockFaceVisible(face);
		}

		//Check if the current face faces air. If so, it will always be visible.
		else if (neighbour.isAir()) {
			//Current face is next to air. Set face to visible.
			setBlockFaceVisible(face);
		}
		//Check if the block is next to a transparent block and if the block itself is not transparent.
		else if (neighbour.isTransparent() && blockData[2] == 0)
		{
			setBlockFaceVisible(face);
		}
		//Face is invisible. Set it to not render.
		else
		{
			setBlockFaceInvisible(face);
		}

		//At least one side was set visible. This means the block will be added to the list of rendered blocks.
		if(blockData[3] != 0)
		{
			chunk.addToVisibleBlockList(this);
		}
		//No sides are visible. The block has to be removed from the block list.
		else
		{
			//Set all sides invisible.
			blockData[3] = 0;
			chunk.removeFromVisibleBlockList(this);
		}
	}

	/**
	 * Calculate if this block should be visible.
	 */
	public void calculateInitialVisibility()
	{
		//Special blocks with custom models are always visible.
		if(type.isSpecial())
		{
			blockData[3] = 63;
			//chunk.addToSpecialBlocksList(this);
			//chunk.removeFromVisibleBlockList(this);
			return;
		}
		//Remove the block from the special blocks list just in case.
		//else
		//{
		//	chunk.removeFromSpecialBlocksList(this);
		//}

		//Set the block to be invisible if its an air block. Always.
		if (isAir())
		{
			blockData[3] = 0;
			//chunk.removeFromVisibleBlockList(this);
			return;
		}

		for(BlockFace face : BlockFace.values())
		{
			Block neighbour = getNeighbouringBlock(face);

			//Neighbour is null, so the block face should be visible just in case. Can always be changed afterward.
			if(neighbour == null)
			{
				setBlockFaceVisible(face);
			}

			//Special neighbour so block is visible.
			else if(neighbour.isSpecial())
			{
				setBlockFaceVisible(face);
			}

			//Check if the current face faces air. If so, it will always be visible.
			else if(neighbour.getByteID() == -128)
			{
				//Current face is next to air. Set face to visible.
				setBlockFaceVisible(face);
			}
			//Check if the block is next to a transparent block and if the block itself is not transparent.
			else if(neighbour.isTransparent() && blockData[2] == 0)
			{
				setBlockFaceVisible(face);
			}
			//Face is invisible. Set it to not render.
			else
			{
				setBlockFaceInvisible(face);
			}
		}

		/*
		//At least one side was set visible. This means the block will be added to the list of rendered blocks.
		if(blockData[3] != 0)
		{
			//chunk.addToVisibleBlockList(this);
		}
		//No sides are visible. The block has to be removed from the block list.
		else
		{
			//Set all sides invisible.
			blockData[3] = 0;
			//chunk.removeFromVisibleBlockList(this);
		}
		*/
	}

	/**
	 * Calculate if this block and the blocks next to it should be visible.
	 */
	public void calculateVisibilityIncludingNeighbours()
	{
		//Special blocks with custom models are always visible.
		if(type.isSpecial())
		{
			blockData[3] = 63;
			chunk.addToSpecialBlocksList(this);
			chunk.removeFromVisibleBlockList(this);

			//Make all neighbouring faces visible.
			for(BlockFace face : BlockFace.values())
			{
				Block n = getNeighbouringBlock(face);
				if(n != null)
				{
					n.calculateVisibilityForFace(face.getOpposite(), this);
				}
			}

			return;
		}
		//Remove the block from the special blocks list just in case.
		else
		{
			chunk.removeFromSpecialBlocksList(this);
		}

		//Set the block to be invisible if its an air block. Always.
		if (isAir())
		{
			blockData[3] = 0;
			chunk.removeFromVisibleBlockList(this);

			//Make all neighbouring faces visible.
			for(BlockFace face : BlockFace.values())
			{
				Block n = getNeighbouringBlock(face);
				if(n != null)
				{
					n.calculateVisibilityForFace(face.getOpposite(), this);
				}
			}
			return;
		}

		for(BlockFace face : BlockFace.values())
		{
			Block neighbour = getNeighbouringBlock(face);

			//Neighbour is null, so the block face should be visible just in case. Can always be changed afterward.
			if(neighbour == null)
			{
				setBlockFaceVisible(face);
			}

			//Special neighbour so block is visible.
			else if(neighbour.isSpecial())
			{
				setBlockFaceVisible(face);
			}

			//Check if the current face faces air. If so, it will always be visible.
			else if(neighbour.getByteID() == -128)
			{
				//Current face is next to air. Set face to visible.
				setBlockFaceVisible(face);
			}
			//Check if the block is next to a transparent block and if the block itself is not transparent.
			else if(neighbour.isTransparent() && blockData[2] == 0)
			{
				setBlockFaceVisible(face);
				neighbour.calculateVisibilityForFace(face.getOpposite(), this);
			}
			//Check if the neighbour is solid and this block is transparent(making the neighbour visible)
			else if(!neighbour.isTransparent() && blockData[2] == 1)
			{
				setBlockFaceVisible(face);
				neighbour.calculateVisibilityForFace(face.getOpposite(), this);
			}
			//Both blocks are transparent.
			else if(neighbour.isTransparent() && blockData[2] == 1)
			{
				//Transparent blocks are facing eachother.
				setBlockFaceInvisible(face);
				//Update neighbouring block just in case.
				neighbour.setBlockFaceInvisible(face.getOpposite());
			}
			//Face is invisible. Set it to not render.
			else
			{
				setBlockFaceInvisible(face);
				//Update neighbouring block just in case.
				neighbour.calculateVisibilityForFace(face.getOpposite(), this);
			}
		}

		//At least one side was set visible. This means the block will be added to the list of rendered blocks.
		if(blockData[3] != 0)
		{
			chunk.addToVisibleBlockList(this);
		}
		//No sides are visible. The block has to be removed from the block list.
		else
		{
			//Set all sides invisible.
			blockData[3] = 0;
			chunk.removeFromVisibleBlockList(this);
		}
	}

	/**
	 * Get the neighbouring block with the given BlockFace.
	 * @param face The BlockFace to get.
	 * @return The neighbouring block at the given face. Returns null in case the other block was not loaded.
	 */
	public Block getNeighbouringBlock(BlockFace face)
	{
		return world.getBlockAt(location.x + face.getOffSetX(), location.y + face.getOffSetY(), location.z + face.getOffSetZ());
	}


	/** SETTERS **/

	/**
	 * Set the ID for this block, which automatically calculates the visibility and transparency.
	 * @param id The new ID, taken from BlockType.
	 */
	public void setId(int id)
	{
		List<Chunk> alteredChunks = getWorld().getChunksInRegion(getX(), getZ(), getX(), getZ(), true, false);

		blockData[0] = (byte)(id - 128);
		blockData[2] = (byte) (BlockType.fromID(id).isTransparent() ? 1 : 0);

		type = BlockType.fromID(id);

		//Calculate all bordering blocks and this blocks new visibility.
		calculateVisibilityIncludingNeighbours();

		//Remove the block from the listed blocks.
		if(id == 0)
		{
			chunk.removeFromBlocksList(this);
		}

		//Set all chunks altered back to not updating status, and make them regenerate their mesh.
		for(Chunk c : alteredChunks)
		{
			c.setUpdating(false);
			c.setNeedsNewMesh(true);
		}
	}

	/**
	 * Set the datavalue for this block.
	 * @param data The new data value for this block.
	 */
	public void setDataValue(byte data)
	{
		List<Chunk> alteredChunks = getWorld().getChunksInRegion(getX(), getZ(), getX(), getZ(), true, false);

		blockData[1] = data;
		calculateVisibilityIncludingNeighbours();

		//Set all chunks altered back to not updating status, and make them regenerate their mesh.
		for(Chunk c : alteredChunks)
		{
			c.setUpdating(false);
			c.setNeedsNewMesh(true);
		}
	}

	/**
	 * Get the ID of the chunk this block is located in.
	 * @return The unique ID of the chunk this block is in.
	 */
	public UUID getChunkUUID() {
		return chunkUUID;
	}

	/**
	 * See if a block is shine through.
	 * @return True if you can see through the block.
	 */
	public boolean isTransparent()
	{
		return blockData[2] == 1;
	}

	/**
	 * Get the real ID of the block in int form.
	 * @return The ID of the block as defined in the BlockType enum.
	 */
	public int getRealID()
	{
		return blockData[0] + 128;
	}

	/**
	 * Get the ID of this block as byte, which is 128 lower than the "real" id.
	 * @return The block type ID - 128.
	 */
	public byte getByteID()
	{
		return blockData[0];
	}

	/**
	 * See if this block is visible, if so, it should render. If not, it's covered by other blocks.
	 * @return If the current block should be visible to the player and render.
	 */
	public boolean isVisible()
	{
		return blockData[3] != 0 && !isAir();
	}

	/**
	 * See if this block is air.
	 * @return
	 */
	public boolean isAir()
	{
		return blockData[0] == -128;
	}




	/**
	 * Getting all coordinates --------------------------------------------
	 */



	/**
	 * Get the X  coordinate of this block in the world.
	 * @return
	 */
	public float getX()
	{
		return location.x;
	}

	/**
	 * Get the Y coordinate of this block in the world.
	 * @return
	 */
	public float getY()
	{
		return location.y;
	}

	/**
	 * Get the Z coordinate of this block in the world.
	 * @return
	 */
	public float getZ()
	{
		return location.z;
	}

	/**
	 * Get the local X of this block in its subchunk.
	 * @return The local X of this block.
	 */
	public byte getLocalX()
	{
		return localX;
	}

	/**
	 * Get the local Z of this block within its subchunk.
	 * @return The local Z of this block.
	 */
	public byte getLocalZ()
	{
		return localZ;
	}

	/**
	 * Get the local Y of this block within its subschunk.
	 * @return The local y of the block.
	 */
	public byte getLocalY()
	{
		return localY;
	}

	/**
	 * Return the location of this block.
	 * @return The block location.
	 */
	public Location getLocation()
	{
		return location;
	}

	/**
	 * Get the instance of the world this block is located in.
	 * @return The world this block belongs to.
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * remove all refferences to other objects so garbage collection can properly work.
	 */
	public void nullify()
	{
		//Make sure this block wont be marked for rendering.
		chunk.removeFromVisibleBlockList(this);

		//Set all data to null.
		this.chunk = null;
		this.world = null;
	}

	public byte getVisibilityByte()
	{
		return blockData[3];
	}

	public BlockType getType() {
		return type;
	}

	public boolean isSpecial() {
		return type.isSpecial();
	}

	public int getVisibleFacesCount()
	{
		int count = 0;
		for(BlockFace face : BlockFace.values())
		{
			if(isBlockFaceVisible(face))count++;
		}
		return count;
	}
}
