package com.hotmail.janie177.gametest.engine.world.blocks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum BlockFace
{
	UP(0,1,0, new float[]{-0.5F, +0.5F, -0.5F, -0.5F, +0.5F, +0.5F, +0.5F, +0.5F, -0.5F, +0.5F, +0.5F, +0.5F}, new int[]{0, 3, 2, 0, 1, 3}, new int[]{2,0,1,3}),
	DOWN(0,-1,0, new float[]{-0.5F, -0.5F, -0.5F, -0.5F, -0.5F, +0.5F, +0.5F, -0.5F, -0.5F, +0.5F, -0.5F, +0.5F}, new int[]{0, 3, 1, 0, 2, 3}, new int[]{0,2,3,1}),
	WEST(-1,0,0, new float[]{-0.5F, -0.5F, -0.5F, -0.5F, -0.5F, +0.5F, -0.5F, +0.5F, -0.5F, -0.5F, +0.5F, +0.5F}, new int[]{0, 3, 2, 0, 1, 3}, new int[]{0,3,2,1}),
	EAST(1,0,0, new float[]{+0.5F, -0.5F, -0.5F, +0.5F, -0.5F, +0.5F, +0.5F, +0.5F, -0.5F, +0.5F, +0.5F, +0.5F}, new int[]{0, 3, 1, 0, 2, 3}, new int[]{3,0,1,2}),
	NORTH(0,0,1, new float[]{-0.5F, -0.5F, +0.5F, -0.5F, +0.5F, +0.5F, +0.5F, -0.5F, +0.5F, +0.5F, +0.5F, +0.5F}, new int[]{0, 3, 1, 0, 2, 3}, new int[]{3,1,0,2}),
	SOUTH(0,0,-1, new float[]{-0.5F, -0.5F, -0.5F, -0.5F, +0.5F, -0.5F, +0.5F, -0.5F, -0.5F, +0.5F, +0.5F, -0.5F}, new int[]{0, 3, 2, 0, 1, 3}, new int[]{0,2,3,1});

	/** BACKUP
	UP(0,1,0, new float[]{-0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F}, new int[]{}),
	DOWN(0,-1,0, new float[]{-0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F}, new int[]{}),
	WEST(-1,0,0, new float[]{-0.5F, -0.5F, 0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F}, new int[]{}),
	EAST(1,0,0, new float[]{0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F}, new int[]{}),
	NORTH(0,0,1, new float[]{0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F}, new int[]{}),
	SOUTH(0,0,-1, new float[]{-0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F}, new int[]{});
	*/
	private int offSetX;
	private int offSetY;
	private int offSetZ;
	private float[] vertices;
	private int[] indices;
	private int[] uvs;

	private static ConcurrentMap<BlockFace, BlockFace> opposites = new ConcurrentHashMap<>();

	static {
		opposites.put(UP, DOWN);
		opposites.put(DOWN, UP);
		opposites.put(WEST, EAST);
		opposites.put(EAST, WEST);
		opposites.put(NORTH, SOUTH);
		opposites.put(SOUTH,NORTH);
	}

	BlockFace(int offsetX, int offsetY, int offsetZ, float[] vertices, int[] indices, int[] uvs)
	{
		this.offSetX = offsetX;
		this.offSetY = offsetY;
		this.offSetZ = offsetZ;
		this.vertices = vertices;
		this.indices = indices;
		this.uvs = uvs;
	}

	public BlockFace getOpposite()
	{
		return opposites.get(this);
	}

	public int[] getIndices()
	{
		return indices;
	}

	public int getOffSetX() {
		return offSetX;
	}

	public int getOffSetY() {
		return offSetY;
	}

	public int getOffSetZ() {
		return offSetZ;
	}

	public float[] getVertices()
	{
		return vertices;
	}

	public int[] getUvs()
	{
		return uvs;
	}
}
