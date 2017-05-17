package com.hotmail.janie177.gametest.engine.managers;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.models.Model;
import com.hotmail.janie177.gametest.engine.models.ModelData;
import com.hotmail.janie177.gametest.engine.models.TexturedModel;
import com.hotmail.janie177.gametest.engine.textures.Texture;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ModelManager
{
	private static ConcurrentMap<String, Model> rawModels = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, TexturedModel> texturedModels = new ConcurrentHashMap<>();

	/**
	 * See if a textured model is loaded.
	 * @param modelName The name of the textured model.
	 * @return True if the textured model is loaded in memory.
	 */
	public static boolean isTexturedModelLoaded(String modelName)
	{
		return texturedModels.containsKey(modelName);
	}

	/**
	 * Load a model from obj file and add a texture to it.
	 * @param modelName The name of the model to save it as.
	 * @param model The raw model to use.
	 * @param texture The texture used with this model.
	 */
	public static void loadTexturedModel(String modelName, Model model, Texture texture)
	{
		//Bundle the basic model together with a texture.
		TexturedModel m = new TexturedModel(model, texture);
		texturedModels.put(modelName, m);
	}



	/** RAW MODELS **/

	/**
	 * Load a raw model using modeldata.
	 * @param name The name of the model.
	 * @param data The data, which is retrieved from an .Obj file using the ObjectLoader class.
	 */
	public static void loadRawModel(String name, ModelData data)
	{
		//Load a new basic model.
		Model raw = TestGame.loader.loadModel(data);

		//Store the new basic model with its id.
		rawModels.put(name, raw);
	}

	/**
	 * See if a model is loaded in memory.
	 * @param name The name of the model.
	 * @return True if the model already exists.
	 */
	public static boolean isRawModelLoaded(String name)
	{
		return rawModels.containsKey(name);
	}

	/**
	 * Get a model by name.
	 * @param name The name of the model.
	 * @return The model object with the given name.
	 */
	public static Model getRawModel(String name)
	{
		return rawModels.get(name);
	}

	/**
	 * Remove a raw model from memory.
	 * This will alos remove all texturedModels which use this model. Never use this if a model is being used still.
	 * @param name The name of the model to remove.
	 */
	public static void removeRawModel(String name)
	{
		if(isRawModelLoaded(name))
		{
			//Get the model.
			Model m = rawModels.get(name);

			//Delete VBOs
			m.getVboIDs().stream().forEach(GL15::glDeleteBuffers);
			//Delete VAO
			GL30.glDeleteVertexArrays(m.getId());

			//Delete all textured models that use this basic model.
			texturedModels.values().stream().filter(tm -> tm.getModel().getId() == m.getId()).forEach(tm -> texturedModels.values().remove(tm));

			//Remove from the list.
			rawModels.remove(name);
		}
	}

	/** END OF RAW MODELS **/



	/**
	 * Get a textured model with the given name.
	 * @param name The name of the model.
	 * @return The TexturedModel with the given name.
	 */
	public static TexturedModel getTexturedModel(String name)
	{
		return texturedModels.get(name);
	}

	/**
	 * Remove a textured model.
	 * @param modelName The name of the model to remove.
	 * @return True if the model was loaded and removed.
	 */
	public static boolean removeTexturedModel(String modelName)
	{
		if(isTexturedModelLoaded(modelName))
		{
			texturedModels.remove(modelName);
			return true;
		}
		return false;
	}

	/**
	 * Get all currently loaded models.
	 * @return A collection of strings representing models.
	 */
	public static Collection<String> getAllTexturedModels()
	{
		return texturedModels.keySet();
	}

	/**
	 * Delete all models from memory, and remove all refferences to them so they will be garbage collected.
	 */
	public static void deleteAllModels()
	{
		//Delete all raw models.
		rawModels.keySet().stream().forEach(s ->
		{
			Model model = rawModels.get(s);
			model.getVboIDs().stream().forEach(GL15::glDeleteBuffers);
			GL30.glDeleteVertexArrays(model.getId());

		});

		//Clear the texturedModels.
		texturedModels.clear();

		//Clear the list of rawModels.
		rawModels.clear();
	}
}
