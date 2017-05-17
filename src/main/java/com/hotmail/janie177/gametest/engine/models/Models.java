package com.hotmail.janie177.gametest.engine.models;

import com.hotmail.janie177.gametest.engine.managers.ModelManager;
import com.hotmail.janie177.gametest.engine.managers.TextureManager;
import com.hotmail.janie177.gametest.engine.models.TexturedModel;
import com.hotmail.janie177.gametest.engine.utils.OBJectLoader;
import com.hotmail.janie177.gametest.game.main.Options;

public enum Models
{
	MARKET_STALL("stall", "stall");

	private String fileName;
	private String textureName;
	private float reflect = 0;
	private boolean transparentTexture = false;
	private float shineDampening = 1;

	private Models(String fileName, String textureName, boolean transparentTexture, float shineDampening, float reflect)
	{
		this.fileName = fileName;
		this.textureName = textureName;
		this.reflect = reflect;
		this.shineDampening = shineDampening;
		this.transparentTexture = transparentTexture;
	}

	private Models(String fileName, String textureName)
	{
		this.fileName = fileName;
		this.textureName = textureName;
	}

	public void load()
	{
		//Check if a texture was loaded.
		if(!TextureManager.isLoaded(textureName))
		{
			//Load the texture.
			TextureManager.loadTexture(textureName, transparentTexture, shineDampening, reflect);
		}

		//Check if a raw model was loaded.
		if(!ModelManager.isRawModelLoaded(fileName))
		{
			//Load the raw model and name it the same as its file.
			ModelManager.loadRawModel(fileName, OBJectLoader.loadOBJ(fileName));
		}

		//Check if this model was already loaded, if not create it now.
		if(!ModelManager.isTexturedModelLoaded(name()))
		{
			//Create a new TexturedModel using the loaded model and textures. Save it as the enum entry name.
			ModelManager.loadTexturedModel(name(), ModelManager.getRawModel(fileName), TextureManager.getTexture(textureName));
		}
	}

	public TexturedModel getTexturedModel()
	{
		return ModelManager.getTexturedModel(name());
	}
}
