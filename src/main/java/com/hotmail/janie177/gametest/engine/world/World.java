package com.hotmail.janie177.gametest.engine.world;

import com.hotmail.janie177.gametest.engine.entities.Light;
import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.world.blocks.Block;
import com.hotmail.janie177.gametest.engine.world.chunks.Chunk;
import com.hotmail.janie177.gametest.engine.world.chunks.ChunkHelper;
import com.hotmail.janie177.gametest.engine.world.chunks.ChunkLoader;
import com.hotmail.janie177.gametest.engine.world.saving.WorldData;
import com.hotmail.janie177.gametest.engine.world.saving.WorldLoader;
import com.hotmail.janie177.gametest.game.entities.Entity;
import com.hotmail.janie177.gametest.game.entities.EntityType;
import com.hotmail.janie177.gametest.game.main.Options;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.hotmail.janie177.gametest.engine.world.chunks.ChunkHelper.calculateChunkPosition;

public class World
{
	//The level seed which is used to generate terrain.
	private long seed;
	//The name of the world.
	private String name;

	//The time of the world
	private long time;
	//The natural light
	private Light sun;


	//A list of all chunks that are currently loaded.
	private ConcurrentMap<UUID, Chunk> loadedChunks = new ConcurrentHashMap<>();

	//A map containing all entities in this world that are of a specific type.
	private ConcurrentMap<EntityType, Set<UUID>> entityTypes = new ConcurrentHashMap<>();
	//A map containing the actual entities, which can be obtained with their UUID.
	private ConcurrentMap<UUID, Entity> entities = new ConcurrentHashMap<>();

	//The chunk loader which loads and queues chunks async.
	private ChunkLoader chunkLoader;

	/**
	 * Create a new instance of World.
	 * @param name The name of the world.
	 * @param seed The seed of the world.
	 */
	public World(String name, long seed, long time)
	{
		this.chunkLoader = new ChunkLoader(this);
		this.name = name.toLowerCase();
		this.seed = seed;
		this.time = time;
		this.sun =  new Light(new Location(0, 50, 0, this));

		//Load all entity types and put empty lists in.
		for(EntityType t : EntityType.values())
		{
			entityTypes.put(t, ConcurrentHashMap.newKeySet());
		}
	}


	/** BLOCK DATA **/


	/**
	 * Get a block at a given coordinate.
	 * @param x The world X coordinate.
	 * @param y The world Y coordinate.
	 * @param z The world Z coordinate.
	 * @return The block at the given location, or null if unloaded.
	 */
	public Block getBlockAt(float x, float y, float z)
	{
		//There's no blocks at at Y larger than 511 or below 0.
		if(y < 0 || y > 511) return null;

		int chunkX = calculateChunkPosition(x);
		int chunkZ = calculateChunkPosition(z);

		Chunk c = getChunk(chunkX, chunkZ);

		if(c != null)
		{
			return c.getBlock(x, y, z);
		}
		return null;
	}


	/** END OF BLOCK DATA **/




	public WorldData getWorldData()
	{
		return new WorldData(name, seed, time);
	}

	public void setWorldData(WorldData data)
	{
		this.seed = data.getSeed();
		this.name = data.getName();
		this.time = data.getTime();
	}

	/**
	 * Check if a chunk is loaded at chunk coordinates X and Z.
	 * @param x The X coordinate. Equals world coordinate / 16 -1 if negative.
	 * @param z The Z coordinate. Equals world coordinate / 16 -1 if negative.
	 * @return The index of the chunk if it exists. Else it returns -1.
	 */
	public boolean isChunkLoaded(int x, int z)
	{
		return loadedChunks.values().stream().filter(c -> c.getChunkX() == x && c.getChunkZ() == z).findFirst().isPresent();
	}

	/**
	 * Get a chunk by coordinates. If the chunk does not exist, returns null.
	 * @param x The x coordinate. Equals worldX / 16 -1 if negative.
	 * @param z The z coordinate. Ewuals worldZ / 16 -1 if negative.
	 * @return Return the chunk with the given coordinates or null if the chunk was not loaded.
	 */
	public Chunk getChunk(int x, int z)
	{
		Optional<Chunk> c = loadedChunks.values().stream().filter(ch -> ch.getChunkX() == x && ch.getChunkZ() == z).findFirst();
		if(c.isPresent())
		{
			return c.get();
		}
		return null;
	}

	/**
	 * Get a chunk by its UUID.
	 * @param uuid The UUID of the chunk to get.
	 * @return The Chunk corresponding to the given UUID, or null if it was not loaded or existing.
	 */
	public Chunk getChunk(UUID uuid)
	{
		return loadedChunks.getOrDefault(uuid, null);
	}

	/**
	 * Check whether the world is currently loaded in the world manager.
	 * @return
	 */
	public boolean isLoaded()
	{
		return TestGame.worldManager.worldIsLoaded(name);
	}

	/**
	 * Load a chunk at chunk coordinates x and z.
	 * @param x The x coordinate. Equals worldX / 16 -1 if negative.
	 * @param z The z coordinate. Equals worldZ / 16 -1 if negative.
	 * @return True if the chunk was loaded. False if it was already loaded or something went wrong.
	 */
	public Chunk loadChunk(int x, int z)
	{
		return WorldLoader.loadChunk(this, x, z);
	}

	/**
	 * Return a list containing all currently loaded chunks.
	 * @return A list of all currently loaded chunks.
	 */
	public Collection<Chunk> getLoadedChunks()
	{
		return loadedChunks.values();
	}


	/** ------ RENDERING_ENTITY STUFF ------ **/

	/**
	 * Create a new entity in this world.
	 * @param type The type of entity.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The newly created entity.
	 */
	public Entity createEntity(EntityType type, float x, float y, float z)
	{
		//Create the new location
		Location location = new Location(x, y, z, this);

		//Create a new instance of type.
		Entity entity = type.createInstance(location);

		if(entity == null)
		{
			return null;
		}

		//Add the entity to the list of entities.
		entities.put(entity.getUuid(), entity);

		//Add the entity UUID to the list of UUID's of that type for rendering purposes.
		Set<UUID> list = entityTypes.get(type);
		list.add(entity.getUuid());
		entityTypes.put(type, list);

		return entity;
	}

	protected void loadEntities(List<Entity> newEntities)
	{
		for(Entity ent : newEntities)
		{
			entities.put(ent.getUuid(), ent);
			Set<UUID> list = entityTypes.get(ent.getType());
			list.add(ent.getUuid());
			entityTypes.put(ent.getType(), list);
		}
	}

	/**
	 * Get all UUIDs of all entities of a certain type.
	 * @param type The type of entity.
	 * @return A list of UUIDs, representing every entity in the world of the specified type.
	 */
	public Set<UUID> getEntities(EntityType type)
	{
		return entityTypes.get(type);
	}

	/**
	 * Get all entities currently in this world.
	 * @return
	 */
	public Collection<Entity> getEntities()
	{
		return entities.values();
	}

	/**
	 * Get all entity types that are currently in this world.
	 * @return
	 */
	public Collection<EntityType> getEntityTypes()
	{
		return entityTypes.keySet();
	}

	/**
	 * Get an entity by it's unique ID.
	 * @param uuid The uuid of the entity.
	 * @return The entity belonging to the UUID. Always call entityExists(uuid) before this.
	 */
	public Entity getEntity(UUID uuid)
	{
		return entities.get(uuid);
	}

	/**
	 * Check if an entity exists.
	 * @param uuid The UUID of the entity.
	 * @return True if the entity exists and is loaded.
	 */
	public boolean entityExists(UUID uuid)
	{
		return entities.containsKey(uuid);
	}

	/**
	 * Remove an entity from the world.
	 * @param uuid THe UUID of the entity to remove.
	 * @return True if the entity was removed. False if it didn't exist.
	 */
	public boolean removeEntity(UUID uuid)
	{
		if(entities.containsKey(uuid))
		{
			Entity entity = entities.get(uuid);
			//Remove from the types list.
			entityTypes.get(entity.getType()).remove(entity.getUuid());
			//Remove from the entities list.
			entities.remove(uuid);
			return true;
		}
		//RenderingEntity was not loaded.
		return false;
	}

	public String getWorldPath()
	{
		return Options.FILE_PATH + File.separator + "saves" + File.separator + getName().toLowerCase();
	}

	/**
	 * Save the world to file.
	 */
	public void saveWorld(boolean saveEntities)
	{
		WorldLoader.saveWorld(this, saveEntities);
	}

	/**
	 * Get the seed for the world. Used for world generation.
	 * @return The long seed of the world.
	 */
	public long getSeed() {
		return seed;
	}

	/**
	 * Get the name of this world
	 * @return The world name in string format.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the time of the world.
	 * @return The current time.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Set the time of the world.
	 * @param time The time to set it to.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Get the sunlight source.
	 * @return The lightsource of the world.
	 */
	public Light getSun() {
		return sun;
	}

	/**
	 * Set the lightsource of the world.
	 * @param sun The new lightsource.
	 */
	public void setSun(Light sun) {
		this.sun = sun;
	}

	/**
	 * Add a chunk to the list of chunks that will be calculated and rendered.
	 * @param chunk The chunk to add.
	 */
	public void addChunkToLoadedChunks(Chunk chunk)
	{
		loadedChunks.put(chunk.getUuid(), chunk);
	}

	/**
	 * Remove a chunk from the list of loaded chunks. This will stop it from rendering or updating.
	 * @param chunk The chunk to remove.
	 */
	public void removeChunkFromLoadedChunks(Chunk chunk)
	{
		if(loadedChunks.containsKey(chunk.getUuid()))
		{
			loadedChunks.remove(chunk.getUuid());
		}
	}

	/**
	 * Get the instance of the chunk loader.
	 * @return The instance of the chunk loader.
	 */
	public ChunkLoader getChunkLoader()
	{
		return chunkLoader;
	}




	//Methods for editing chunks, small and large edits.

	/**
	 * Get a list of chunks inside a region, given that they are loaded at that time.
	 * If a chunk is not loaded, or unloading, it will not be put in the list.
	 * NOTE: This also returns bordering chunks if a block is on the edge
	 * @param x1 The x coordinate of the first block.
	 * @param z1 The z coordinate of the first block.
	 * @param x2 The x coordinate of the second block.
	 * @param z2 The z coordinate of the second block.
	 * @param mark True if all chunks should be marked as updating to prevent unloading. Make sure that they are unmarked afterward if true.
	 * @return A list of chunks that can then be edited.
	 */
	public List<Chunk> getChunksInRegion(float x1, float z1, float x2, float z2, boolean mark, boolean includeLoading)
	{
		List<Chunk> chunks = new ArrayList<>();
		int chunkX1, chunkX2, chunkZ1, chunkZ2;
		
		//Make sure that chunkX1 is always the smallest coordinate. This way the loop will always correctly calculate chunks inside the region.
		if(x1 <= x2)
		{
			chunkX1 = ChunkHelper.calculateChunkPosition(x1 - 1);
			chunkX2 = ChunkHelper.calculateChunkPosition(x2 + 1);
		}
		else
		{
			chunkX1 = ChunkHelper.calculateChunkPosition(x2 - 1);
			chunkX2 = ChunkHelper.calculateChunkPosition(x1 + 1);
		}

		//Make sure that chunkz1 is always the smallest coordinate.
		if(z1 <= z2)
		{
			chunkZ1 = ChunkHelper.calculateChunkPosition(z1 - 1);
			chunkZ2 = ChunkHelper.calculateChunkPosition(z2 + 1);
		}
		else
		{
			chunkZ1 = ChunkHelper.calculateChunkPosition(z2 - 1);
			chunkZ2 = ChunkHelper.calculateChunkPosition(z1 + 1);
		}

		for(int x = chunkX1; x <= chunkX2; x++)
		{
			for(int z = chunkZ1; z <= chunkZ2; z++)
			{
				//X and Z represent every chunk inside the given region, or chunks just outside it that are still possibly affected by a block change.
				Chunk c = getChunk(x, z);
				//Make sure that the chuink is not already unloading or unloaded.
				if(c != null && !c.isUnloading() && (!c.isLoading() || (c.isLoading() && includeLoading)))
				{
					if(mark)c.setUpdating(true);
					chunks.add(c);
				}
			}
		}
		return chunks;
	}
}
