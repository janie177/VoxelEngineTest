package com.hotmail.janie177.gametest.engine.world.chunks;

import com.hotmail.janie177.gametest.engine.world.blocks.Block;
import com.hotmail.janie177.gametest.engine.world.blocks.BlockFace;
import com.hotmail.janie177.gametest.game.main.Options;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

public class ChunkMesh
{
	private int vaoids[] = new int[]{-1, -1};
	private int vboids[][] = new int[][]{{-1, -1},{-1, -1}};
	private int vertexCounts[] = new int[2];
	private Chunk chunk;

	private boolean generating = false;

	//The size of a float.
	private final static int floatSize = Float.SIZE / Byte.SIZE;

	//The size in bytes of each "section"
	private final static int vertexSize = 3 * floatSize;
	private final static int normalSize  = 3 * floatSize;
	private final static int textureSize = 3 * floatSize;

	//The amount of bytes between each position and its corresponding texture data + normals.
	private final static int stride = vertexSize + normalSize + textureSize;

	//The offsets for each element
	private final static long offsetVertices = 0;
	private final static long offsetTexture = 3 * floatSize;
	private final static long offsetNormal = 6 * floatSize;

	public ChunkMesh(Chunk chunk)
	{
		this.chunk = chunk;
	}

	public void refreshMesh()
	{
		//Do not refresh the mesh as long as the chunk is still being updated. Only do it once it's done calculating visibility.
		if(chunk.isUpdating())
		{
			return;
		}

		//Return if this mesh is already being generated
		if(generating) return;

		generating = true;

		int[] counts = chunk.getFaceCount();

		//Destroy the old mesh.
		destroy();

		//Make sure that theres actually blocks of the given type in the chunk.
		if(counts[0] != 0) create(MeshType.SOLID, counts[0]);
		if(counts[1] != 0) create(MeshType.TRANSPARENT, counts[1]);

		//Set this chunk to no longer need a new mesh
		chunk.setNeedsNewMesh(false);

		//Set this chunk to no longer generating.
		generating = false;
	}

	final static float[][] textureCorners = new float[][]{{0, 0},{1, 1},{0, 1},{1, 0}};

	public void create(MeshType type, int faceCount)
	{
		//Create the VAO
		vaoids[type.ordinal()] = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoids[type.ordinal()]);

		//Calculate the sizes of the buffers.
		//facecount is the total amount of faces. Each face has 4 points, consisting out of 3 floats each.
		//Every triangle needs 3 indices, and there is 2 triangles. Though 2 of the points overlap meaning that there's only 4 indices per face.
		int indicesSize = faceCount * 6;
		//The interleaved buffer needs to contain all data.
		//Every face has 4 vertices. Each vertices' total data is the size of stride.
		//Therefore the total amount of floats needed is 4 vertices * stride * faces
		int interleavedSize = stride * faceCount;

		//Set the vertex count
		vertexCounts[type.ordinal()] = indicesSize;

		//Generate all buffers
		//Generate the int buffer.
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesSize);
		//Create the buffer for the data
		FloatBuffer interleavedBuffer = BufferUtils.createFloatBuffer(interleavedSize);

		//A counter to keep track of the indices.
		int counter = 0;

		//Fill the buffers with the correct data.
		//The loop which gets all the block faces.
		for(Block b : chunk.getVisibleBlocks())
		{
			if((b.isTransparent() && type != MeshType.TRANSPARENT) || (!b.isTransparent() && type != MeshType.SOLID)) continue;

			for(BlockFace face : BlockFace.values())
			{
				if(b.isBlockFaceVisible(face))
				{
					//Loop through the vertices
					for(int count = 0; count <= 9; count+=3)
					{
						//Add the X Y Z coordinates of the vertex location.
						interleavedBuffer.put(face.getVertices()[count] + b.getLocalX());
						interleavedBuffer.put(face.getVertices()[count + 1] + b.getY());
						interleavedBuffer.put(face.getVertices()[count + 2] + b.getLocalZ());

						//Add the texture coordinate for the vertex location
						interleavedBuffer.put(textureCorners[face.getUvs()[count / 3]]);

						interleavedBuffer.put(b.getType().getTextureLayerIndex(face));

						//Add the X Y Z of the vertex's normal, which is the same for every vertex on a single face.
						interleavedBuffer.put(face.getOffSetX());
						interleavedBuffer.put(face.getOffSetY());
						interleavedBuffer.put(face.getOffSetZ());
					}

					//Add the indices of the added data.

					for(int i = 0; i < 6; i++)
					{
						indicesBuffer.put(counter + face.getIndices()[i]);
					}

					//Update the counter.
					counter+=4;
				}
			}
		}

		//Finally, flip the buffers.
		indicesBuffer.flip();
		interleavedBuffer.flip();

		//Bind the indices buffer.
		vboids[type.ordinal()][0] = GL15.glGenBuffers();
		//Bind the Vertex Buffer Object with the ID.
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboids[type.ordinal()][0]);
		//Bind the indices buffer
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

		//Generate the buffer location in openGL
		vboids[type.ordinal()][1] = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboids[type.ordinal()][1]);
		//Bind the interleaved buffer.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, interleavedBuffer, GL15.GL_STATIC_DRAW);

		// Enable the vertex attribute locations
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		//Bind attribute pointers using the offsets.
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, offsetVertices);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, offsetTexture);
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, stride, offsetNormal);

		GL30.glBindVertexArray(0);
	}

	public boolean isGenerating()
	{
		return generating;
	}

	public int getVAOId(MeshType type)
	{
		return vaoids[type.ordinal()];
	}

	public int getVertexCount(MeshType type)
	{
		return vertexCounts[type.ordinal()];
	}

	public float getWorldX()
	{
		return ChunkHelper.calculateWorldPosition(chunk.getChunkX());
	}

	public float getWorldZ()
	{
		return ChunkHelper.calculateWorldPosition(chunk.getChunkZ());
	}

	public void destroy()
	{
		int id;
		for(MeshType type : MeshType.values())
		{
			if((id = vaoids[type.ordinal()]) != -1)
			{
				//Delete VBO's
				for(int vbo : vboids[type.ordinal()])
				{
					if(vbo != -1)
					{
						GL15.glDeleteBuffers(vbo);
					}
				}
				//Delete VAO
				GL30.glDeleteVertexArrays(id);
			}
			vboids[type.ordinal()] = new int[]{-1, -1};
			vaoids[type.ordinal()] = -1;
			vertexCounts[type.ordinal()] = 0;
		}
	}
}
