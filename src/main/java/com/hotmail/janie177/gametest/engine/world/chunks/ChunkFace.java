package com.hotmail.janie177.gametest.engine.world.chunks;

public enum ChunkFace
{
	NORTH(0,1),
	EAST(1,0),
	SOUTH(0,-1),
	WEST(-1,0);

	private int offsetX;
	private int offsetZ;

	private ChunkFace(int offsetX, int offsetZ)
	{
		this.offsetX = offsetX;
		this.offsetZ = offsetZ;
	}


	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetZ()
	{
		return offsetZ;
	}
}
