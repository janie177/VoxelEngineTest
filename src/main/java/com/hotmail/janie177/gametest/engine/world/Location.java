package com.hotmail.janie177.gametest.engine.world;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.world.chunks.Chunk;
import com.hotmail.janie177.gametest.engine.world.chunks.ChunkHelper;
import org.joml.Vector3f;

public class Location extends Vector3f
{
	private World world;

	/**
	 * Constructor to create a new location.
	 * @param x The x coordinate. (horizontal)
	 * @param y The y coordinate. (vertical)
	 * @param z The z coordinate. (horizontal)
	 * @param world The world the location is in. (universal Kappa)
	 */
	public Location(float x, float y, float z, World world)
	{
		super(x, y, z);
		this.world = world;
	}

	/**
	 * Get the world this location points to.
	 */
	public World getWorld()
	{
		return world;
	}

	/**
	 * Set this location to another location.
	 * @param l The location to change it to.
	 */
	public void changeToLocation(Location l)
	{
		this.x = l.x;
		this.y = l.y;
		this.z = l.z;
		this.world = l.world;
	}

	/**
	 * Check if this location is currently loaded.
	 * @return
	 */
	public boolean isLoaded()
	{
		return world != null && TestGame.worldManager.worldIsLoaded(world.getName()) && world.isChunkLoaded((int)x/16, (int)z/16);
	}

	/**
	 * Return a new copy of this location.
	 * @return
	 */
	@Override
	public Location clone()
	{
		return new Location(x, y, z, world);
	}

	@Override
	public Location add(float x, float y, float z)
	{
		this.x +=x;
		this.y +=y;
		this.z +=z;
		return this;
	}

	public Chunk getChunk()
	{
		return world.getChunk(ChunkHelper.calculateChunkPosition(x), ChunkHelper.calculateChunkPosition(z));
	}

	public int getChunkX()
	{
		return ChunkHelper.calculateChunkPosition(x);
	}

	public int getChunkZ()
	{
		return ChunkHelper.calculateChunkPosition(z);
	}

	public boolean isWorldLoaded()
	{
		return world != null && world.isLoaded();
	}
}
