package com.hotmail.janie177.gametest.engine.world.chunks;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.world.World;
import com.hotmail.janie177.gametest.game.main.Options;
import org.joml.Vector2i;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.hotmail.janie177.gametest.engine.world.chunks.ChunkHelper.calculateChunkPosition;

public class ChunkLoader
{
	//The queues for unloading chunks.
	private ConcurrentMap<Vector2i, Double> chunkLoadQueue = new ConcurrentHashMap<>();
	private ConcurrentMap<Chunk, Double> chunkUnloadQueue = new ConcurrentHashMap<>();
	private ConcurrentMap<String, Long> lastUnloadTimes = new ConcurrentHashMap<>();

	private boolean queue = true;

	private World world;

	private int chunksPerSecond = 5;
	private int chunkQueueInterval = Options.TPS * 5;

	private int chunkLoadInterval = Options.TPS / chunksPerSecond;
	private long tick = 0;

	private int maxThreads = 8;

	//List of threads that are loading async. This is used to stop loading when the queueing is stopped.
	private static Set<Thread> threads = ConcurrentHashMap.newKeySet();


	//Constructor
	public ChunkLoader(World world)
	{
		this.world = world;
	}



	private synchronized void unloadNextFurthestChunk()
	{
		if(chunkUnloadQueue.isEmpty()) return;

		double furthest = 0;
		Chunk toUnload = null;
		for(Chunk c : chunkUnloadQueue.keySet())
		{
			double d = chunkUnloadQueue.get(c);
			if(d > furthest)
			{
				furthest = d;
				toUnload = c;
			}
		}

		//If no chunk was found OR the found chunk is currently updating, do not do anything. This mainly needs to happen because when a chunk next to it loads,
		//It will start calculating the border blocks' visibility. If the chunk starts unloading while that happens, it will throw a NPE.
		//The calculating visibility of border blocks will set that chunk to updating, so if it has started calculating the chunk wont unload until it's finished doing that.
		if(toUnload == null || toUnload.isUpdating()) return;

		toUnload.setLastQueueTime();
		unloadChunk(toUnload);
		chunkUnloadQueue.remove(toUnload);
	}

	private synchronized void loadClosestChunk()
	{
		if(chunkLoadQueue.isEmpty()) return;

		double closest = 9000;
		Vector2i toLoad = null;

		for(Vector2i v : chunkLoadQueue.keySet())
		{
			double d = chunkLoadQueue.get(v);

			if(d < closest)
			{
				toLoad = v;
				closest = d;
			}
		}
		if(toLoad == null) return;

		loadChunk(toLoad);

		chunkLoadQueue.remove(toLoad);
	}

	protected synchronized void removeThread(Thread t)
	{
		//Remove the thread if it is in the set.
		if(threads.contains(t)) threads.remove(t);
	}


	private void loadChunk(Vector2i coordinates)
	{
		ChunkLoadingRunnable r = new ChunkLoadingRunnable(coordinates, world, this);
		final Thread t = new Thread(r);
		r.setThread(t);
		threads.add(t);
		t.start();
	}

	private void unloadChunk(Chunk chunk)
	{
		lastUnloadTimes.put(chunk.getFileName(), System.currentTimeMillis());
		ChunkUnloadingRunnable r = new ChunkUnloadingRunnable(chunk, world, this);
		Thread t = new Thread(r);
		r.setThread(t);
		threads.add(t);
		t.start();
	}


	/**
	 * Load chunks from the given player location
	 */
	public void executeChunkQueue()
	{
		//Check if queueing is still enabled.
		if(!queue) return;

		//Make sure that there's not too many concurrent threads.
		if(threads.size() >= maxThreads)
		{
			return;
		}

		if (tick % chunkQueueInterval == 0)
		{
			queueChunksForLoading();
			queueChunksForUnloading();
		}
		tick++;

		if (tick % chunkLoadInterval == 0)
		{
			unloadNextFurthestChunk();
			loadClosestChunk();
		}
	}

	private void queueChunksForLoading()
	{
		//Empty the old queue.
		chunkLoadQueue.clear();

		int chunkX = calculateChunkPosition(TestGame.player.getLocation().x);
		int chunkZ = calculateChunkPosition(TestGame.player.getLocation().z);

		int renderDistance = (int) Options.ConfigSettings.RENDER_DISTANCE.getValue();

		for(int x = chunkX - renderDistance; x <= chunkX + renderDistance; x++)
		{
			for(int z = chunkZ - renderDistance; z <= chunkZ + renderDistance; z++)
			{
				//For every chunk within the render distance, check if it's loaded. If not, load it.

				//The file name of the chunk, to check against last load times.
				String fileName = ChunkHelper.getChunkFileName(x, z);

				//If the chunk is not loaded (or loading) and it has not been unloaded in the past 5 seconds, queue it.
				if(!world.isChunkLoaded(x, z) && !(System.currentTimeMillis() - lastUnloadTimes.getOrDefault(fileName, 0L) < 5000))
				{
					double distance = Math.sqrt(Math.pow(x - chunkX, 2)+Math.pow(z - chunkZ, 2));
					chunkLoadQueue.put(new Vector2i(x, z), distance);
				}
			}
		}
	}

	/**
	 * Unload chunks that are not near the player anymore.
	 */
	private void queueChunksForUnloading()
	{
		//Clear old queue
		chunkUnloadQueue.clear();

		//Get the players chunk location.
		int centerX = calculateChunkPosition(TestGame.player.getLocation().x);
		int centerZ = calculateChunkPosition(TestGame.player.getLocation().z);

		int renderDistance = (int) Options.ConfigSettings.RENDER_DISTANCE.getValue();
		double distance;

		for(Chunk chunk : world.getLoadedChunks())
		{
			//Chunk is still loading, don't unload it, or chunk is still unloading and was thus already queued. Also don't queue it if the chunk is less that 5 seconds old.
			if(chunk.isLoading() || chunk.isUnloading() || (System.currentTimeMillis() - chunk.getLastQueueTime() < 5000)) continue;

			//Do not waste any calculation on chunks that wont unload anyways.
			if(chunk.neverUnload()) continue;

			//Calculate the distance between the two chunks.
			distance = Math.sqrt(Math.pow(chunk.getChunkX() - centerX, 2)+Math.pow(chunk.getChunkZ() - centerZ, 2));

			//Compare to render distance
			if(distance > renderDistance + 1.5)
			{
				//Add the chunk to be unloaded.
				chunkUnloadQueue.put(chunk, distance);
			}
		}

	}

	/**
	 * Directly stop the queueing of chunks, and clear the existing queue.
	 */
	public void stopQueueing()
	{
		queue = false;
		chunkLoadQueue.clear();
		chunkUnloadQueue.clear();
	}

	/**
	 * Start queueing chunks again. Note that the "executeQueue" method still has to be called in a loop for this to work.
	 */
	public void restartQueueing()
	{
		//Start the queueing again.
		queue = true;
	}



}
