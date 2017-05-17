package com.hotmail.janie177.gametest.engine.display;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.game.main.Options;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glViewport;

public class DisplayManager
{
	//This is here in case of errors. Otherwise it will be garbage collected.
	private static GLFWFramebufferSizeCallback framebufferSizeCallback;

	/**
	 * Create a new OpenGL window. This is called once at the start of the program.
	 */
	public static void create()
	{
		//Init glfw
		glfwInit();

		//Set all parameters for the OpenGL window. This was done with the Display class in previous versions of lwjgl. Now it uses GLFW.

		//OpenGL
		glfwWindowHint(GLFW_SAMPLES, (int) Options.ConfigSettings.AA_SAMPLES.getValue());
		//Allow resizing
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		//Version
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, Options.VERSION_MAJOR);
		//Minor version
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, Options.VERSION_MINOR);
		//Profile
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		//Compatibility with newer versions
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

		//The ID of the monitor used
		long monitor = 0;

		//If full screen is enabled in Options, load the game full screen
		if(Options.ConfigSettings.FULL_SCREEN.getValue() != 0)
		{
			monitor = glfwGetPrimaryMonitor();
			GLFWVidMode vidMode = glfwGetVideoMode(monitor);
			Options.ConfigSettings.SCREEN_WIDTH.setValue( vidMode.width());
			Options.ConfigSettings.SCREEN_HEIGHT.setValue(vidMode.height());
		}


		//Set the window ID to the created OpenGL window. This window contains all the action.
		Options.WINDOW_ID = glfwCreateWindow((int) Options.ConfigSettings.SCREEN_WIDTH.getValue(), (int) Options.ConfigSettings.SCREEN_HEIGHT.getValue(), Options.GAME_NAME, monitor, 0);

		//If the window was not created, send an error and stop the program.
		if(Options.WINDOW_ID == 0) {
			throw new RuntimeException("Could not create window.");
		}

		//Hide the cursor.
		glfwSetInputMode(Options.WINDOW_ID, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

		//Set the context for the window.
		glfwMakeContextCurrent(Options.WINDOW_ID);
		//Create capabilities.
		GL.createCapabilities();
		//Show the window. This is false by default.
		glfwShowWindow(Options.WINDOW_ID);

		//Set resize listener
		glfwSetFramebufferSizeCallback(Options.WINDOW_ID, (framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				onResize(width, height);
			}
		}));

		IntBuffer bufferWidth = BufferUtils.createIntBuffer(1), bufferHeight = BufferUtils.createIntBuffer(1);
		glfwGetFramebufferSize(Options.WINDOW_ID, bufferWidth, bufferHeight);
		onResize(bufferWidth.get(), bufferHeight.get());
	}

	/**
	 * Updates the screen. This runs every frame in the loop in TestGame.
	 */
	public static void update()
	{
		glfwPollEvents();
		glfwSwapBuffers(Options.WINDOW_ID);
	}

	/**
	 * Called when the screen is resized.
	 * @param width The new width.
	 * @param height The new height.
	 */
	public static void onResize(int width, int height)
	{
		//Update DIMENSIONS for other classes to use.
		Options.ConfigSettings.SCREEN_HEIGHT.setValue(height);
		Options.ConfigSettings.SCREEN_WIDTH.setValue(width);

		//Update the center of the screen
		Options.CENTER_X = (int) Options.ConfigSettings.SCREEN_WIDTH.getValue() / 2;
		Options.CENTER_Y = (int) Options.ConfigSettings.SCREEN_HEIGHT.getValue() / 2;

		//Update the projection matrix to its new size, given that the game has been loaded already.
		if(TestGame.renderManager != null)
		{
			TestGame.renderManager.createProjectionMatrix();
		}

		//Set the screen size.
		glViewport(0, 0, width, height);
	}

	/**
	 * Close the window, ending the program.
	 */
	public static void close()
	{
		//Close the window.
		glfwDestroyWindow(Options.WINDOW_ID);

		//Get rid of glfw
		glfwTerminate();
	}
}
