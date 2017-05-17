package com.hotmail.janie177.gametest.engine.managers;

import com.hotmail.janie177.gametest.engine.entities.Camera;
import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.rendering.BlockRenderer;
import com.hotmail.janie177.gametest.engine.rendering.ChunkRenderer;
import com.hotmail.janie177.gametest.engine.rendering.EntityRenderer;
import com.hotmail.janie177.gametest.engine.shaders.DefaultShader;
import com.hotmail.janie177.gametest.engine.shaders.TerrainShader;
import com.hotmail.janie177.gametest.engine.utils.MathUtil;
import com.hotmail.janie177.gametest.engine.utils.ViewFrustum;
import com.hotmail.janie177.gametest.engine.world.blocks.Block;
import com.hotmail.janie177.gametest.engine.world.World;
import com.hotmail.janie177.gametest.engine.world.blocks.BlockType;
import com.hotmail.janie177.gametest.engine.world.chunks.ChunkMesh;
import com.hotmail.janie177.gametest.game.entities.Entity;
import com.hotmail.janie177.gametest.game.entities.EntityType;
import com.hotmail.janie177.gametest.game.main.Options;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.hotmail.janie177.gametest.game.main.Options.FAR_PLANE;
import static com.hotmail.janie177.gametest.game.main.Options.NEAR_PLANE;

public class RenderManager
{
	private DefaultShader shader = new DefaultShader();
	private TerrainShader terrainShader = new TerrainShader();
	private ChunkRenderer chunkRenderer = new ChunkRenderer(terrainShader);
	private BlockRenderer blockRenderer = new BlockRenderer(shader);
	private EntityRenderer entityRenderer = new EntityRenderer(shader);
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;

	//All models to update.
	private ConcurrentMap<EntityType, Set<Entity>> entities = new ConcurrentHashMap<>();
	private ConcurrentMap<BlockType, Set<Block>> specialBlocks = new ConcurrentHashMap<>();
	private Set<ChunkMesh> chunkMeshes = ConcurrentHashMap.newKeySet();


	public RenderManager()
	{
		initSets();
		createProjectionMatrix();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
		terrainShader.start();
		terrainShader.loadProjectionMatrix(projectionMatrix);
		terrainShader.stop();
		setCulling(true);
	}

	/** ------ **/
	/** RENDER **/
	/** ------ **/

	/**
	 * Render everything that needs to be rendered.
	 */
	public void renderAll(Camera camera)
	{
		//Calculate the view matrix.
		viewMatrix = MathUtil.createViewMatrix(camera);

		//Calculate the view frustum every frame.
		ViewFrustum.update(viewMatrix, projectionMatrix);

		//Prepare all entities to be rendered.
		prepareForRender();

		//Set the background to gray, delete old model data layers.
		prepareBackGround();

		//Start the shader
		shader.start();
		terrainShader.start();

		//Load the light.
		shader.loadLight(TestGame.player.getWorld().getSun());
		terrainShader.loadLight(TestGame.player.getWorld().getSun());

		//Load the view Matrix (Camera position)
		shader.loadviewMatrix(viewMatrix);
		terrainShader.loadviewMatrix(viewMatrix);

		//Render all special blocks
		for(BlockType type : specialBlocks.keySet())
		{
			blockRenderer.renderBlocks(new ArrayList<>(specialBlocks.get(type)), type);
		}

		//Render all entities.
		for(EntityType type : entities.keySet())
		{
			entityRenderer.renderEntities(new ArrayList<>(entities.get(type)));
		}

		//Render all chunk meshes.
		chunkRenderer.renderMeshes(chunkMeshes);

		//Stop the shader
		shader.stop();
		terrainShader.stop();

		//Clear all entities for the next cycle.
		clearMaps();
	}

	/**
	 * Remove things from memory.
	 */
	public void disableAll()
	{
		entities.clear();
		specialBlocks.clear();
		shader.cleanMemory();
	}


	/** ------ **/
	/** RENDER **/
	/** ------ **/


	private void prepareForRender()
	{
		World world = TestGame.player.getWorld();

		//Load all entities into the map.
		for(EntityType type : world.getEntityTypes())
		{
			for(UUID uuid : world.getEntities(type))
			{
				Entity ent = world.getEntity(uuid);
				entities.get(type).add(ent);
			}
		}

		world.getLoadedChunks().stream().filter(c -> c.isChunkVisible() && !c.isLoading() && !c.isUnloading()).forEach(c ->
		{
			c.getSpecialBlocks().stream().forEach(b -> specialBlocks.get(b.getType()).add(b));
			if(c.needsNewMesh())
			{
				c.getChunkMesh().refreshMesh();
			}
			if(!c.getChunkMesh().isGenerating()) chunkMeshes.add(c.getChunkMesh());
		});

	}


	private void clearMaps()
	{
		entities.values().stream().forEach(Set::clear);
		specialBlocks.values().stream().forEach(Set::clear);
		chunkMeshes.clear();
	}

	private void initSets()
	{
		//Clear the sets
		entities.clear();
		specialBlocks.clear();

		for(BlockType type : BlockType.values())
		{
			if(type.isSpecial()) specialBlocks.put(type, ConcurrentHashMap.newKeySet());
		}

		for(EntityType type : EntityType.values())
		{
			entities.put(type, ConcurrentHashMap.newKeySet());
		}
	}

	public void createProjectionMatrix()
	{
		float aspectRatio = Options.ConfigSettings.SCREEN_WIDTH.getValue() / Options.ConfigSettings.SCREEN_HEIGHT.getValue();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(Options.ConfigSettings.FOV.getValue() / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00(x_scale);
		projectionMatrix.m11(y_scale);
		projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustum_length));
		projectionMatrix.m23(-1);
		projectionMatrix.m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustum_length));
		projectionMatrix.m33(0);
	}


	/**
	 * Set culling to true or false. If true, the back of blocks will not render.
	 */
	public static void setCulling(boolean culling)
	{
		if(culling)
		{
			//Not rendering invisible sides
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);

			//Disable blending UNTESTED
			GL11.glDisable(GL11.GL_BLEND);
		}
		else
		{
			GL11.glDisable(GL11.GL_CULL_FACE);

			//Blend with background textures UNTESTED
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

	}

	/**
	 * Clear the background, enable depth test.
	 */
	public static void prepareBackGround()
	{
		//Enable rendering depth. (prevents the back of models being rendered).
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		//Clear the background
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		//Set background to gray
		GL11.glClearColor(0.2F,0.76F,0.9F,1);
	}
}
