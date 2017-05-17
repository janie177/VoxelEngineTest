package com.hotmail.janie177.gametest.engine.world.chunks;

import com.hotmail.janie177.gametest.engine.world.World;
import org.joml.Vector2i;

public class ChunkUnloadingRunnable implements Runnable
{
	private Thread t;
	private World world;
	private Chunk c;
	private ChunkLoader loader;

	public ChunkUnloadingRunnable(Chunk c, World world, ChunkLoader loader)
	{
		this.world = world;
		this.c = c;
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
		c.unload();

		//Remove this thread after its done executing.
		loader.removeThread(t);
	}
}
