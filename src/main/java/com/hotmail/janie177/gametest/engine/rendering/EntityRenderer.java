package com.hotmail.janie177.gametest.engine.rendering;

import com.hotmail.janie177.gametest.engine.managers.RenderManager;
import com.hotmail.janie177.gametest.engine.models.Model;
import com.hotmail.janie177.gametest.engine.models.TexturedModel;
import com.hotmail.janie177.gametest.engine.shaders.DefaultShader;
import com.hotmail.janie177.gametest.engine.textures.Texture;
import com.hotmail.janie177.gametest.engine.utils.MathUtil;
import com.hotmail.janie177.gametest.game.entities.Entity;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class EntityRenderer
{
	private DefaultShader shader;

	public EntityRenderer(DefaultShader shader)
	{
		this.shader = shader;
	}

	private void bindModel(TexturedModel model, boolean transparent)
	{
		//Get the model from the entity. This is stored in the TexturedModel class in the RenderingEntity class.
		Model raw = model.getModel();
		//Set the vertex array ID to that of the model. Make OpenGL select that since it was loaded before.
		GL30.glBindVertexArray(raw.getId());
		//Enable array 0 and 1. 0 contains data for the triangle points. 1 contains texture data.
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		//Load reflection light
		Texture texture = model.getTexture();

		//If the texture is transparent, do not disable the rendering of the back of textures.
		RenderManager.setCulling(!transparent);

		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());

		//Activate texture mode.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		//Supply the texture ID. This was supplied to OpenGL before, and will be aquired by ID since the same ID is in the model class. OpenGL will
		//then know which texture has to be drawn.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
	}


	public void renderEntities(List<Entity> entities)
	{
		if(entities.size() < 1)return;

		TexturedModel model = entities.get(0).getRenderingEntity().getModel();

		//Bind the model. Set to transparent if the texture is transparent.
		bindModel(model, model.getTexture().isTransparent());

		int vertexCount = model.getModel().getVertexCount();
		for(Entity ent : entities)
		{
			//Create the transformation matrix which determines how an entity looks.
			Matrix4f matrix = MathUtil.createTransformationMatrix(ent.getLocation(), ent.getRotX(), ent.getRotY(), ent.getRotZ(), ent.getScale());
			shader.loadTransformationMatrix(matrix);
			//Draw triangles from the entity.
			GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
		}
		//Unbind the model
		unbindModel();
	}

	private void unbindModel()
	{
		RenderManager.setCulling(true);
		//Disable array 0 and 1 after drawing is done.
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		//Set vertexarray to 0 (default) to stop anything from happening after drawing is done.
		GL30.glBindVertexArray(0);
	}
}
