package com.hotmail.janie177.gametest.engine.rendering;

import com.hotmail.janie177.gametest.engine.managers.RenderManager;
import com.hotmail.janie177.gametest.engine.managers.TextureManager;

import com.hotmail.janie177.gametest.engine.models.TexturedModel;
import com.hotmail.janie177.gametest.engine.shaders.DefaultShader;

import com.hotmail.janie177.gametest.engine.shaders.TerrainShader;
import com.hotmail.janie177.gametest.engine.utils.MathUtil;
import com.hotmail.janie177.gametest.engine.world.chunks.ChunkMesh;
import com.hotmail.janie177.gametest.engine.world.chunks.MeshType;
import com.hotmail.janie177.gametest.game.entities.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

public class ChunkRenderer
{
	private TerrainShader shader;

	public ChunkRenderer(TerrainShader shader)
	{
		this.shader = shader;
	}


	private void bindTextures()
	{
		shader.loadShineVariables(1, 0);
		//Activate texture mode.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		//Supply the texture ID. This was supplied to OpenGL before, and will be aquired by ID since the same ID is in the model class. OpenGL will
		//then know which texture has to be drawn.
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TextureManager.getTexture("textures").getId());
	}


	public void renderMeshes(Set<ChunkMesh> meshes)
	{
		if(meshes.size() < 1)return;

		//Bind the texture sheet
		bindTextures();

		//Enable culling for the solid meshes.
		RenderManager.setCulling(true);

		for(ChunkMesh mesh : meshes)
		{
			if(mesh.isGenerating() || mesh.getVAOId(MeshType.SOLID) == -1)
			{
				continue;
			}

			//Set the vertex array ID to that of the mesh
			GL30.glBindVertexArray(mesh.getVAOId(MeshType.SOLID));
			//Enable array 0 1 2
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);

			//Create the transformation matrix which places the chunk correctly relative to the player location.
			Matrix4f matrix = MathUtil.createTransformationMatrix(new Vector3f(mesh.getWorldX(), 0, mesh.getWorldZ()), 0, 0, 0, 1);
			shader.loadTransformationMatrix(matrix);
			//Draw the blocks.
			GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(MeshType.SOLID), GL11.GL_UNSIGNED_INT, 0);
		}

		//Disable culling for all the transparent meshes.
		RenderManager.setCulling(false);

		//Reflect a little more for transparent objects
		shader.loadShineVariables(10, 1);

		for(ChunkMesh mesh : meshes)
		{
			if(mesh.isGenerating() || mesh.getVAOId(MeshType.TRANSPARENT) == -1) continue;

			//Set the vertex array ID to that of the mesh.
			GL30.glBindVertexArray(mesh.getVAOId(MeshType.TRANSPARENT));
			//Enable array 0 1 2
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);

			//Create the transformation matrix which places the chunk correctly relative to the player location.
			Matrix4f matrix = MathUtil.createTransformationMatrix(new Vector3f(mesh.getWorldX(), 0, mesh.getWorldZ()), 0, 0, 0, 1);
			shader.loadTransformationMatrix(matrix);
			//Draw the blocks.
			GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(MeshType.TRANSPARENT), GL11.GL_UNSIGNED_INT, 0);
		}

		//Unbind
		unbind();
	}

	private void unbind()
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
