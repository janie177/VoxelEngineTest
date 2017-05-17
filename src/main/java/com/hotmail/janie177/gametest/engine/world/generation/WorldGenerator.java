package com.hotmail.janie177.gametest.engine.world.generation;

import com.hotmail.janie177.gametest.engine.world.blocks.BlockFace;
import com.hotmail.janie177.gametest.engine.world.chunks.Chunk;
import com.hotmail.janie177.gametest.engine.world.chunks.ChunkHelper;

public class WorldGenerator
{
	public static void createChunk(Chunk chunk)
	{
		//Allocate new memory for the data. Later air-only parts will be set to null.
		byte[][][][][] data = new byte[32][][][][];

		//Fill it with air.
		for(int i = 0; i < 32; i++)
		{
			data[i] = ChunkHelper.createNewSubChunk();
		}

		float worldX = ChunkHelper.calculateWorldPosition(chunk.getChunkX());
		float worldZ = ChunkHelper.calculateWorldPosition(chunk.getChunkZ());



		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				int maxY = (calculateHeight((worldX + x) * 3));

				for(int y = 0; y < maxY; y++)
				{
					byte id = (byte)(y < 59 ? 2 : (y > 74 ? 2 : (y > 30 ? 1 : 3)));
					data[y / 16][x][y%16][z][0] =  (byte)(id - 128);
					data[y / 16][x][y%16][z][2] = 0;
				}
			}
		}

		



		//Counter for the sub chunk id.
		int subId = 0;

		//Set all air-only regions to null
		for(byte[][][][] subChunk : data)
		{
			//Check if this subChunk was not null already and if it is now null
			if(ChunkHelper.subChunkIsEmpty(subChunk))
			{
				//Delete the data of this chunk since it has nothing in it.
				data[subId] = null;
			}
			//Increment the subId counter.
			subId++;
		}

		//Load the newly created data into the chunk
		chunk.loadData(data);

		//Save the chunk
		chunk.saveChunk(false);
	}

	private static int calculateHeight(float x)
	{
		float top     = 50f;
		float yScale  = 20f;
		float yBase   = top + yScale;
		return (int)( yBase - Math.sin( Math.toRadians(x) ) * yScale );
	}
}
