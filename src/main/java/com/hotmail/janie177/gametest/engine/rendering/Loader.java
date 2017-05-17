package com.hotmail.janie177.gametest.engine.rendering;

import com.hotmail.janie177.gametest.engine.managers.TextureManager;
import com.hotmail.janie177.gametest.engine.models.Model;
import com.hotmail.janie177.gametest.engine.models.ModelData;
import com.hotmail.janie177.gametest.engine.utils.PNGDecoder;
import com.hotmail.janie177.gametest.engine.world.blocks.BlockType;
import com.hotmail.janie177.gametest.game.main.Options;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.GL_RGB16UI;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL42.glTexStorage3D;

public class Loader
{
	/**
	 * Create a model from a given set of points.
	 * @param data The modelData taken using the ObjUtil.
	 * @return A new model which can be rendered using the Renderer class.
	 *
	 * WARNING Do not use thos method as it will not allow you to find the model in the ModelManager again.
	 * Only use this in special occasions, such as when a basic model is needed to fit multiple TexturedModels.
	 */
	public Model loadModel(ModelData data)
	{
		//Create a VertexArray in memory, and get its ID.
		int id = createVAO();
		Model m = new Model(id, data.getIndices().length);

		//Store the indices (which order to connect the positions in) in a buffer. Then supply it to OpenGL.
		m.addVBOID(bindIndicesBuffer(data.getIndices()));
		m.addVBOID(storeInVBO(0, 3, data.getVertices())); //3 because there is an x, y and z coordinate.
		m.addVBOID(storeInVBO(1, 2, data.getTextureCoords())); //2 because there's 2 points the 2D texture will bind to. xyz,xyz
		m.addVBOID(storeInVBO(2, 3, data.getNormals())); //Normals which determine which way a point on a texture is facing.
		unbind();

		return new Model(id, data.getIndices().length);
	}

	/**
	 * Load textures using PNGDecoder. Then store the received texture in OpenGL.
	 * @param fileName
	 * @return
	 */
	public int loadTexture(String fileName)
	{
		int id;
		try
		{

			//Import the texture
			FileInputStream in = new FileInputStream(Options.TEXTURES_PATH + fileName + ".png");
			PNGDecoder decoder = new PNGDecoder(in);

			ByteBuffer buf = ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
			decoder.decode(buf, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
			buf.flip();

			//Generate a texture ID.
			id=glGenTextures();
			//Set this ID to a 2D texture in OpenGL.
			glBindTexture(GL_TEXTURE_2D, id);

			//Set some parameters for the image. ENABLE THIS WHEN NOT USING MIPMAPPING MIPMAP MIP MAP
			//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

			//Register the texture buffer as RGB 2D image. Supply the dimensions too.
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

			//Mipmapping
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4F);

			//Close the input stream since it's no longer needed.
			in.close();

		} catch (IOException e)
		{
			System.out.println("Error while loading texture.");
			e.printStackTrace();
			System.exit(1);
			return 0;
		}
		return id;
	}

	public int loadBlockTextureArray()
	{
		int id;
		try
		{
			//Import the texture
			FileInputStream in = new FileInputStream(Options.TEXTURES_PATH + "textures.png");
			PNGDecoder decoder = new PNGDecoder(in);

			glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

			//Generate a texture ID.
			id=glGenTextures();
			//Set this ID to a 2D texture in OpenGL.
			glBindTexture(GL_TEXTURE_2D_ARRAY, id);

			int xBlocks = decoder.getWidth() / Options.BLOCK_TEXTURE_SIZE, zBlocks = decoder.getHeight() / Options.BLOCK_TEXTURE_SIZE;

			//Amount of block faces on the block texture sheet.
			int layers = xBlocks * zBlocks;

			Options.BLOCK_ATLAS_HEIGHT = zBlocks;
			Options.BLOCK_ATLAS_WIDTH = xBlocks;

			//Tell how many layers there will be and how much mipmapping.
			glTexStorage3D(GL_TEXTURE_2D_ARRAY, Options.BLOCK_MIPMAPS, GL_RGBA8, Options.BLOCK_TEXTURE_SIZE, Options.BLOCK_TEXTURE_SIZE, layers);


			//Put all the sub images in individual buffers.
			ByteBuffer buf = ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
			decoder.decode(buf, 4 * decoder.getWidth(), PNGDecoder.Format.RGBA);
			buf.flip();

			//Tell OpenGL that one row is 512 long, so that it will interleave the textures correctly.
			glPixelStorei(GL_UNPACK_ROW_LENGTH, decoder.getWidth());

			int currentLayer = 0;
			//Add all the textures from the atlas.
			for(int z = 0; z < zBlocks; z++)
			{
				for(int x = 0; x < xBlocks; x++)
				{
					/*
					int offsetX = x * Options.BLOCK_TEXTURE_SIZE, offsetZ = offsetX + z * Options.BLOCK_TEXTURE_SIZE;
					glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, offsetX, offsetZ, currentLayer, Options.BLOCK_TEXTURE_SIZE, Options.BLOCK_TEXTURE_SIZE, layers, GL_RGBA, GL_UNSIGNED_BYTE, buf);
					currentLayer++;
					*/
					//int bufOffset = (x + z * xBlocks) * Options.BLOCK_TEXTURE_SIZE * 4;
					int subImageSize = Options.BLOCK_TEXTURE_SIZE * Options.BLOCK_TEXTURE_SIZE * 4;
					int bufOffset = (z * (subImageSize * xBlocks)) + (x * Options.BLOCK_TEXTURE_SIZE * 4);
					glTexSubImage3D(GL_TEXTURE_2D_ARRAY,        // texture
							0,                          // level (target)
							0,                          // xoffset (target)
							0,                          // yoffset (target)
							currentLayer,               // zoffset (target)
							Options.BLOCK_TEXTURE_SIZE, // width (target/source)
							Options.BLOCK_TEXTURE_SIZE, // height (target/source)
							1,                          // depth (target/source)
							GL_RGBA,                    // format (source)
							GL_UNSIGNED_BYTE,           // type (source)
							(ByteBuffer) buf.position(bufOffset));   // data (source)
					currentLayer++;
				}
			}

			//Mipmapping
			GL30.glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
			GL11.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);


			//Row length back to 0
			glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);

			//Close the input stream since it's no longer needed.
			in.close();


		} catch (IOException e)
		{
			System.out.println("Error while loading texture array for blocks.");
			e.printStackTrace();
			System.exit(1);
			return -1;
		}

		return id;
	}

	/**
	 * Create a new VAO.
	 * @return The ID of the new VAO.
	 */
	private int createVAO()
	{
		//Generate a vertexArray in memory. Return the ID given to that adress.
		int id = GL30.glGenVertexArrays();

		//Bind the vertexArray with this ID.
		GL30.glBindVertexArray(id);

		//Return the ID.
		return id;
	}

	/**
	 * Store data in a VBO.
	 * @param index The index in the VAO to store them in.
	 * @param coordinateSize The size.
	 * @param data The data to store.
	 */
	private int storeInVBO(int index, int coordinateSize, float[] data)
	{
		//Create a new VBO. This is automatically added to the currently selected VAO.
		int vboID = GL15.glGenBuffers();

		//Set this ID to bind an array buffer.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);

		//Create a floatbuffer from the data.
		FloatBuffer buffer = storeInFloatBuffer(data);

		//Put the buffer in the VBO. Static so it cannot be edited anymore.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		//Create a pointer in this VAO (with ID = index) that points to the data that was just added. The coordinateSize is the amount of
		//elements in this array.
		GL20.glVertexAttribPointer(index, coordinateSize, GL11.GL_FLOAT, false, 0, 0);

		//No need to unbind the array buffer.
		return vboID;
	}

	/**
	 * Store a float array in a buffer.
	 * @param data The floats to convert.
	 * @return A new float buffer.
	 */
	private FloatBuffer storeInFloatBuffer(float[] data)
	{
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	/**
	 * Bind an array of ints (indices) to OpenGL in the currently selected VAO.
	 * @param indices The data to bind.
	 */
	private int bindIndicesBuffer(int[] indices)
	{
		//Create a VBO for the Indices
		int vboID = GL15.glGenBuffers();

		//Bind the Vertex Buffer Object with the ID.
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);

		//Create a buffer from the indices array. Indices contains info for in which order to connect the dots of the triangles in the positions array.
		IntBuffer buffer = storeInIntBuffer(indices);

		//Tell OpenGL that this buffer is stored as element array, and that it's a static draw (never will be changed).
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		//Do not unbind the elementarraybuffer here. It will stop the rendering from working. Also it's really not necessary.
		return vboID;
	}

	/**
	 * Store integers in a buffer
	 * @param data The integers.
	 * @return A buffer that has been flipped.
	 */
	private IntBuffer storeInIntBuffer(int[] data)
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		//Flip so the front is first.
		buffer.flip();
		return buffer;
	}

	/**
	 * Unbind the VAO after it has been created.
	 */
	private void unbind()
	{
		GL30.glBindVertexArray(0);
	}
}
