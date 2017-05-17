package com.hotmail.janie177.gametest.engine.managers;

import com.hotmail.janie177.gametest.engine.world.chunks.Chunk;
import com.hotmail.janie177.gametest.engine.world.World;
import com.hotmail.janie177.gametest.engine.world.saving.WorldLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WorldManager
{
	//All world instances
	private ConcurrentMap<String, World> worlds = new ConcurrentHashMap<>();
	private List<String> indexes = new ArrayList<>();

	/**
	 * Load a world from file.
	 * @param name The name of the world (same as the folder it is kept in).
	 */
	public boolean loadWorld(String name)
	{
		World world = WorldLoader.loadWorld(name);
		if(world != null)
		{
			worlds.put(name.toLowerCase(), world);
			indexes.add(name.toLowerCase());
			return true;
		}
		return false;
	}

	/**
	 * See if a world is loaded.
	 * @param name The name of the world.
	 * @return True if the given worldname is loaded.
	 */
	public boolean worldIsLoaded(String name)
	{
		return worlds.containsKey(name.toLowerCase());
	}

	/**
	 * Get a world from the world manager.
	 * @param name The name of the world to get.
	 * @return An instance of the world.
	 */
	public World getWorld(String name)
	{
		return worlds.get(name.toLowerCase());
	}

	/**
	 * Get a world by the given index.
	 * @param index The index.
	 * @return The world corresponding to the index.
	 */
	public World getWorld(int index)
	{
		return worlds.get(indexes.get(index));
	}

	/**
	 * Return all currently loaded worlds.
	 * @return The worlds that are loaded.
	 */
	public Collection<World> getWorlds()
	{
		return worlds.values();
	}

	/**
	 * Unload a world.
	 * @param name The name of the world to unload.
	 * @return True if the world was unloaded, false if it could not be found or if it was the last loaded world, since there has to be one loaded at least.
	 */
	public boolean unloadWorld(String name)
	{
		if(worlds.containsKey(name.toLowerCase()))
		{
			World world = worlds.get(name.toLowerCase());

			//Unload all chunks, saving them in the process.
			for(Chunk chunk : world.getLoadedChunks())
			{
				//Make sure all chunks can be unloaded
				chunk.setNeverUnload(false);
				//Unload the chunk, which in turn saves it, saves entities and then removes it from the loaded chunks.
				chunk.unload();
			}

			//Save the world. At this point all chunks are unloaded so it wont save chunks twice. It will however save the player and other data.
			world.saveWorld(true);

			worlds.remove(name.toLowerCase());
			indexes.remove(name.toLowerCase());
			return true;
		}
		return false;
	}

	/**
	 * Unload a world.
	 * @param world The world to unload.
	 * @return True if the world was unloaded, false if it could not be found or if it was the last loaded world, since there has to be one loaded at least.
	 */
	public boolean unloadWorld(World world)
	{
		if(worlds.containsValue(world))
		{
			//Stop queuing chunks for the world.
			world.getChunkLoader().stopQueueing();

			//Unload all chunks, saving them in the process.
			for(Chunk chunk : world.getLoadedChunks())
			{
				//Make sure all chunks can be unloaded
				chunk.setNeverUnload(false);

				//Unload the chunk, which in turn saves it, saves entities and then removes it from the loaded chunks.
				//Make sure the chunk was not already unloading.
				if(!chunk.isUnloading()) chunk.unload();
			}

			//Save the world. At this point all chunks are unloaded so it wont save chunks twice. It will however save the player and other data.
			world.saveWorld(true);

			worlds.remove(world.getName());
			indexes.remove(world.getName());
			return true;
		}
		return false;
	}

	/**
	 * Unload all worlds.
	 */
	public void unloadAllWorlds()
	{
		worlds.values().stream().forEach(this::unloadWorld);
	}
}
