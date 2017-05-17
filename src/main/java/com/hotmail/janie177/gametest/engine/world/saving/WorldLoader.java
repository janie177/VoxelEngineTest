package com.hotmail.janie177.gametest.engine.world.saving;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.utils.FileUtil;
import com.hotmail.janie177.gametest.engine.world.chunks.Chunk;
import com.hotmail.janie177.gametest.engine.world.World;
import com.hotmail.janie177.gametest.engine.world.generation.WorldGenerator;
import com.hotmail.janie177.gametest.game.entities.Entity;
import com.hotmail.janie177.gametest.game.player.PlayerData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WorldLoader
{
	/** - - - LOADING - - - **/

	public static World loadWorld(String name)
	{
		World world = new World(name, 0, 0);

		File worldDataFile = FileUtil.getOrCreateFile(world.getWorldPath(), "world.dat");
		try
		{
			//Load the world data and apply it to the world.
			WorldData data = (WorldData) FileUtil.loadObjectFromFile(worldDataFile);
			world.setWorldData(data);
		}
		catch (Exception e)
		{
			System.out.println("Error while loading world data.");
			e.printStackTrace();
		}

		File playerDataFile = FileUtil.getOrCreateFile(world.getWorldPath(), "player.dat");
		try
		{
			//Set all the players data to the player.
			PlayerData data = (PlayerData) FileUtil.loadObjectFromFile(playerDataFile);
			TestGame.player.setPlayerData(data);
		}
		catch (Exception e)
		{
			System.out.println("Error while loading player data.");
			e.printStackTrace();
		}

		return world;
	}

	public static Chunk loadChunk(World world, int x, int z)
	{
		//Create the chunk
		Chunk chunk = new Chunk(x, z, world);

		chunk.setLastQueueTime();

		//Add chunk to the list of loaded chunks
		world.addChunkToLoadedChunks(chunk);

		boolean generated = false;

		//Create an empty data array.
		byte data[][][][][];

		//The save file for this specific chunk.
		File chunkFile = new File(chunk.getPath() + chunk.getFileName());

		//Load data from file
		if(chunkFile.exists())
		{
			try
			{
				//Load the data
				data = (byte[][][][][]) FileUtil.loadObjectFromFile(chunkFile);

				//Load the chunk data into the chunk.
				chunk.loadData(data);
			}
			catch (Exception e)
			{
				System.out.println("Could not read data from chunk " + x + "," + z + ".");
				System.out.println("Creating backup of corrupted chunk data.");
				e.printStackTrace();

				//Could not read file, creating new chunks using the world generator. Also back up the corrupted data.

				//Make a copy of the old file.
				File corruptedChunk = new File(chunk.getPath() + "corrupted_" + chunk.getFileName());
				try
				{
					FileUtil.copyFile(chunkFile, corruptedChunk);
				} catch (Exception i)
				{
					System.out.println("Could not copy corrupted chunk file.");
					i.printStackTrace();
				}

				//Load the data from the world generator.
				WorldGenerator.createChunk(chunk);
				generated = true;
			}
		}
		//Call the world generator.
		else
		{
			WorldGenerator.createChunk(chunk);
			generated = true;
		}

		//Load entities into the chunk.
		loadEntities(chunk);

		//If the chunk was newly generated, calculate all visible blocks.
		if(generated)
		{
			chunk.calculateVisibility();
		}

		//Initialize the sets that contain all visible blocks. This has to be done after block visibility is calculated (Which is only needed at generation).
		chunk.initializeVisibilitySets();

		//Calculate which border blocks to render in the chunk.
		chunk.calculateBorderBlockVisibility();

		chunk.setLoading(false);

		return chunk;
	}

	private static void loadEntities(Chunk chunk)
	{
		//Get the entities file.
		File entitiesFile = FileUtil.getOrCreateFile(chunk.getPath(), "entities.dat");
		try
		{
			//TODO This doesnt work rn.
			//Load the entities from the file into the world.
		//	List<Entity> entities = (List<Entity>) FileUtil.loadObjectFromFile(entitiesFile);
		//	chunk.getWorld().loadEntities(entities);
		}
		catch (Exception e)
		{
			System.out.println("Error while trying to load entities in a chunk.");
			e.printStackTrace();
		}
	}




	/** - - - SAVING - - - **/



	public static void saveWorld(World world, boolean saveEntities)
	{
		File playerDataFile = FileUtil.getOrCreateFile(world.getWorldPath(), "player.dat");
		try
		{
			FileUtil.saveObjectToFile(playerDataFile, TestGame.player.getPlayerData(), false);
		}
		catch (Exception e)
		{
			System.out.println("Error while saving player file.");
			e.printStackTrace();
		}
		File worldDataFile = FileUtil.getOrCreateFile(world.getWorldPath(), "world.dat");
		try
		{
			FileUtil.saveObjectToFile(worldDataFile, world.getWorldData(), false);
		}
		catch (Exception e)
		{
			System.out.println("Error while saving world file.");
			e.printStackTrace();
		}


		//Get all chunks and save them, unless they were already unloading (and thus saving).
		world.getLoadedChunks().stream().filter(c -> !c.isUnloading() && !c.isLoading()).forEach(c -> c.saveChunk(saveEntities));
	}

	/**
	 * Save a chunk to file.
	 * @param chunk The chunk to save.
	 */
	public static void saveChunk(Chunk chunk, boolean saveEntities)
	{
		String fileName = chunk.getFileName();
		String chunkPath = chunk.getPath();

		//Create and get the chunk file.
		File chunkFile = FileUtil.getOrCreateFile(chunkPath, fileName);

		try
		{
			//False makes it overwrite all old data.
			FileUtil.saveObjectToFile(chunkFile, chunk.getData(), false);
		}
		catch (Exception e)
		{
			System.out.println("Error when trying to save a chunk to file.");
			e.printStackTrace();
		}

		//Check if entities should be saved. Prevents duplication when the world is saved but the chunk does not unload.
		if(saveEntities) saveEntities(chunk);
	}

	/**
	 * Save a Entities in a chunk to file. Removes them from the world in the process.
	 * @param chunk The chunk to get the entities from.
	 */
	public static void saveEntities(Chunk chunk)
	{
		List<Entity> entities = new ArrayList<>();

		//Save entities that were inside the
		chunk.getWorld().getEntities().stream().filter(e -> e.getLocation().getChunk() != null && e.getLocation().getChunk().getFileName().equals(chunk.getFileName())).forEach(e ->
		{
			//Save all entities inside the unloaded chunk.
			entities.add(e);
			//Remove the saved entities from memory.
			chunk.getWorld().removeEntity(e.getUuid());
		});

		//Create the entities file.
		File entitiesFile = FileUtil.getOrCreateFile(chunk.getPath(), "entities.dat");

		try
		{
			//False means do not append; overwrite all old data.
			FileUtil.saveObjectToFile(entitiesFile, entities, false);
		}
		catch (Exception e)
		{
			System.out.println("Error while trying to save entities in a chunk.");
			e.printStackTrace();
		}
	}
}
