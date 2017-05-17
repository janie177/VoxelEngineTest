package com.hotmail.janie177.gametest.engine.main;

import com.hotmail.janie177.gametest.game.main.Options;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

public abstract class KeyBoardListener
{

	private long intervalBetweenUpdates = 0, lastUpdate = 0;

	/**
	 * Create a new Keyboard Listener.
	 * @param intervalBetweenUpdates The interval between updating.
	 */
	public KeyBoardListener(long intervalBetweenUpdates)
	{
		this.intervalBetweenUpdates = intervalBetweenUpdates;
		init(Options.WINDOW_ID);
	}

	//Callback stored in case of errors to prevent garbage disposal.
	private static GLFWKeyCallback keyCallback;

	//Init the keyboard listener. Is called when a window is created.
	private void init(long window)
	{
		glfwSetKeyCallback(window, (keyCallback = new GLFWKeyCallback() {

			@Override
			public void invoke(long window, int key, int scancode, int action, int mods)
			{
				onSingleKeyPress(window, key, scancode, action, mods);
			}

		}));
	}

	/**
	 * Check if a key is pressed in real time.
	 * @param key
	 * @return
	 */
	public boolean isKeyPressed(int key)
	{
		return GLFW.glfwGetKey(Options.WINDOW_ID, key) != 0;
	}
	public void updateAllKeys()
	{
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastUpdate >= intervalBetweenUpdates)
		{
			updateKeys();
			lastUpdate = currentTime;
		}
	}

	protected abstract void updateKeys();

	public abstract void onSingleKeyPress(long window, int key, int scancode, int action, int mods);
}
