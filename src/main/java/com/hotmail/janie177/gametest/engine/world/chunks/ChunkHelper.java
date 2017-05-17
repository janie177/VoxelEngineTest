package com.hotmail.janie177.gametest.engine.world.chunks;

public class ChunkHelper
{
	/**
	 * Return the file name for this chunk.
	 * @param x The X coordinate of the chunk.
	 * @param z The Y coordinate of the chunk.
	 * @return
	 */
	public static String getChunkFileName(int x, int z)
	{
		return x + "_" + z + ".dat";
	}

	/**
	 * Return if a subChunk is only air blocks.
	 * This is called when saving a chunk, to make sure no useless data is saved.
	 * @param subChunk The subchunk to check.
	 * @return True if there's just air in this subchunk.
	 */
	public static boolean subChunkIsEmpty(byte[][][][] subChunk)
	{
		if(subChunk == null) return true;
		for(int x = 0; x < 16; x++)
		{
			for(int y = 0; y < 16; y++)
			{
				for(int z = 0; z < 16; z++)
				{
					if(subChunk[x][y][z][0] != -128) return false;
				}
			}
		}
		return true;
	}

	/**
	 * Fill a new subchunk with air only blocks.
	 * @return A new subchunk filled with air.
	 */
	public static byte[][][][] createNewSubChunk()
	{
		byte[][][][] airSubChunk = new byte[16][16][16][4];
		for(int x = 0; x < 16; x++)
		{
			for(int y = 0; y < 16; y++)
			{
				for(int z = 0; z < 16; z++)
				{
					//Fill ID with the id for air.
					airSubChunk[x][y][z][0] = -128;
					//Set the block to be transparent(Since air is always transparent.
					airSubChunk[x][y][z][2] = 1;
				}
			}
		}
		return airSubChunk;
	}

	/**
	 * Get the location of a block in a chunk from it's real world coordinate.
	 * @param coordinate The coordinate.
	 * @return The position of a coordinate in a subchunk.
	 */
	public static int calculateLocalPosition(float coordinate)
	{
		//If the coordinate is smaller than 0, the floor of the number needs to be taken since -0.0 still is positive and -0.0001 is still the 15th block of the -1st chunk.
		if(coordinate < 0)
		{
			return Math.floorMod((int) Math.floor(coordinate), 16);
		}
		//Positive, so just return as usual.
		return Math.floorMod((int) coordinate, 16);
	}

	/**
	 * Calculate a chunks position in the world from a block's coordinate.
	 * @param coordinate The X or Z coordinate to convert to the chunks coordinate.
	 * @return The coordinate of the chunk on the X or Z axis.
	 */
	public static int calculateChunkPosition(float coordinate)
	{
		//Coordinate is bigger than or equal to 0. This means its a positive chunk without an offset.
		if(coordinate >= 0)
		{
			return (int) Math.floor(coordinate) / 16;
		}
		//Coordinate is smaller than 0. Offset below 0.
		else
		{
			//Because the way dividing ints works, minus coordinates require to have one subtracted.
			return ((int) (Math.floor(coordinate + 1)) / 16) - 1;
		}
	}

	/**
	 * Calculate the coordinates of a chunk in the world. Input is the ChunkX or ChunkZ.
	 * @param chunkCoordinate
	 * @return
	 */
	public static float calculateWorldPosition(int chunkCoordinate)
	{
		 return chunkCoordinate * 16;
	}
}
