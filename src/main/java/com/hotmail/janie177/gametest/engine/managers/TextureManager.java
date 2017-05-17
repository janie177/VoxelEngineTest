package com.hotmail.janie177.gametest.engine.managers;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.textures.Texture;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TextureManager
{
	private static ConcurrentMap<String, Texture> textures = new ConcurrentHashMap<>();
	private static ConcurrentMap<Integer, String> texturesIDs = new ConcurrentHashMap<>();

	public static boolean isLoaded(int textureID)
	{
		return texturesIDs.containsKey(textureID) && textures.containsKey(texturesIDs.get(textureID));
	}

	public static boolean isLoaded(String texture)
	{
		return textures.containsKey(texture);
	}

	public static void loadTexture(String texure)
	{
		Texture t = new Texture(TestGame.loader.loadTexture(texure));
		textures.put(texure, t);
		texturesIDs.put(t.getId(), texure);
	}

	public static void loadBlockTextures()
	{
		Texture t = new Texture(TestGame.loader.loadBlockTextureArray());
		textures.put("textures", t);
		texturesIDs.put(t.getId(), "textures");
	}

	public static void loadTexture(String texure, boolean transparent, float shineDampening, float reflect)
	{
		Texture t = new Texture(TestGame.loader.loadTexture(texure));
		t.setTransparent(transparent);
		t.setShineDampening(shineDampening);
		t.setReflectivity(reflect);
		textures.put(texure, t);
		texturesIDs.put(t.getId(), texure);
	}

	public static Texture getTexture(int id)
	{
		return textures.get(texturesIDs.get(id));
	}

	public static Texture getTexture(String name)
	{
		return textures.get(name);
	}

	public static boolean deleteTexture(String textureName)
	{
		if(isLoaded(textureName))
		{
			Texture t = textures.get(textureName);
			GL11.glDeleteTextures(t.getId());
			if(texturesIDs.containsKey(t.getId()))
			{
				texturesIDs.remove(t.getId());
			}
			return true;
		}
		return false;
	}

	public static void deleteAllTextures()
	{
		textures.keySet().stream().forEach(s ->
		{
			Texture t = textures.get(s);
			GL11.glDeleteTextures(t.getId());
			textures.remove(s);
		});
		texturesIDs.keySet().forEach(texturesIDs::remove);
	}
}
