package com.hotmail.janie177.gametest.engine.world.chunks;

import com.hotmail.janie177.gametest.engine.world.World;
import org.joml.Vector2i;

public class ChunkLoadingRunnable implements Runnable
{
	private Thread t;
	private World world;
	private Vector2i coordinates;
	private ChunkLoader loader;

	public ChunkLoadingRunnable(Vector2i coordinates, World world, ChunkLoader loader)
	{
		this.world = world;
		this.coordinates = coordinates;
		this.loader = loader;
	}

	protected void setThread(Thread t)
	{
		this.t = t;
	}

	@Override
	public void run()
	{
		//Load the chunk.
		Chunk loaded = world.loadChunk(coordinates.x, coordinates.y);
		loaded.setNeedsNewMesh(true);

		//Remove this thread after it's done executing.
		loader.removeThread(t);
	}
}
