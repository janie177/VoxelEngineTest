package com.hotmail.janie177.gametest.engine.world.blocks;

import com.hotmail.janie177.gametest.engine.managers.ModelManager;
import com.hotmail.janie177.gametest.engine.managers.TextureManager;
import com.hotmail.janie177.gametest.engine.models.TexturedModel;
import com.hotmail.janie177.gametest.engine.textures.Texture;
import com.hotmail.janie177.gametest.engine.utils.OBJectLoader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum BlockType
{
	AIR(0, false, true, -1),
	STONE(1, true, false, 0),
	GRASS(2, true, false, 16),
	DIRT(3, true, false, 32),
	GLASS(4, true, true, 48);


	private static ConcurrentMap<Integer, BlockType> indices = new ConcurrentHashMap<>();


	private int id;
	private boolean solid;
	private boolean transparent = false;

	private int textureLayerIndex = -1;

	private String textureName = null;
	private String modelName = null;


	private String modelRefference = name() + "_BLOCK";



	static
	{
		for(BlockType type : values())
		{
			indices.put(type.getId(), type);
		}
	}

	BlockType(int id, boolean solid, boolean transparent, int textureLayerIndex)
	{
		this.id = id;
		this.solid = solid;
		this.transparent = transparent;

		this.textureLayerIndex = textureLayerIndex;
	}

	BlockType(int id, boolean solid, boolean transparent, String modelName, String textureName)
	{
		this.id = id;
		this.solid = solid;
		this.transparent = transparent;

		this.modelName = modelName;
		this.textureName = textureName;
	}

	/**
	 * Get the ID of the block.
	 * @return
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Return if entities clip with this block or not.
	 * @return
	 */
	public boolean isSolid()
	{
		return solid;
	}

	/**
	 * Load the modelName for this block type.
	 */
	public void load()
	{
		//Only load blocks that have a custom texture.
		if(textureName == null || modelName == null)
		{
			return;
		}

		//Check if the texture for this object was loaded. If not load it.
		if(!TextureManager.isLoaded(textureName))
		{
			TextureManager.loadTexture(textureName, transparent, 1, 0);
		}

		//Check if the raw modelName (.obj file) was loaded already. If not, load it to memory.
		if(!ModelManager.isRawModelLoaded(modelName))
		{
			ModelManager.loadRawModel(modelName, OBJectLoader.loadOBJ(modelName));
		}

		//Check if a TexturedModel was created for this block. If not, load it.
		if(!ModelManager.isTexturedModelLoaded(modelRefference))
		{
			//Create a new TexturedModel which uses the raw modelName 'modelName' as modelName. It uses the texture stored as 'textureName' as texture.
			ModelManager.loadTexturedModel(modelRefference, ModelManager.getRawModel(modelName), TextureManager.getTexture(textureName));
		}

	}

	/**
	 * Get the blocktype from an ID.
	 * @param id The ID to get the blocktype for.
	 * @return AIR if ID does not exist, otherwise the corresponding blocktype.
	 */
	public static BlockType fromID(int id)
	{
		if(indices.containsKey(id))
		{
			return indices.get(id);
		}
		return AIR;
	}

	/**
	 * Get the texture used by the modelName for this block.
	 * @return the texture used.
	 */
	public Texture getTexture()
	{
		return TextureManager.getTexture(textureName);
	}

	/**
	 * Return the Texturedmodel used by this block.
	 * @return The Texturedmodel used.
	 */
	public TexturedModel getTexturedModel()
	{
		return ModelManager.getTexturedModel(modelRefference);
	}

	/**
	 * Check if this block is special, and uses a custom model and texture.
	 * @return True if the block uses a special model and texture.
	 */
	public boolean isSpecial()
	{
		return textureName != null;
	}

	/**
	 * Check if this block is transparent.
	 * @return True if this blocks texture is transparent.
	 */
	public boolean isTransparent()
	{
		return transparent;
	}

	/**
	 * Get the texture coordinates for the given block.
	 * @return A float array of texture coordinates.
	 */
	public int getTextureLayerIndex(BlockFace face)
	{
		return textureLayerIndex;
	}
}
