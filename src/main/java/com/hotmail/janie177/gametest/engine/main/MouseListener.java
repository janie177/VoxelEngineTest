package com.hotmail.janie177.gametest.engine.main;

import com.hotmail.janie177.gametest.game.main.Options;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import static org.lwjgl.glfw.GLFW.*;

public abstract class MouseListener
{
	public MouseListener()
	{
		init(Options.WINDOW_ID);
	}

	//For debugging to prevent garbage collection
	private static GLFWCursorPosCallback mouseCallback;
	private static GLFWMouseButtonCallback mouseClickCallback;

	private void init(long window)
	{
		glfwSetCursorPosCallback(window, (mouseCallback = new GLFWCursorPosCallback() {

			@Override
			public void invoke(long window, double xpos, double ypos)
			{
				onMouseUpdate(Options.CENTER_X - xpos, Options.CENTER_Y - ypos);
				glfwSetCursorPos(Options.WINDOW_ID, Options.CENTER_X, Options.CENTER_Y);
			}
		}));

		glfwSetMouseButtonCallback(window, (mouseClickCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods)
			{
				onMouseClick(button, action, mods);
			}
		}));
	}


	/**
	 * This is called when the mouse moves.
	 * @param xMoved How much the X changed.
	 * @param yMoved How much the Y changed.
	 */
	protected abstract void onMouseUpdate(double xMoved, double yMoved);

	/**
	 * Listen for mouse clicks.
	 * @param button The mousebutton that was clicked.
	 * @param action The action.
	 * @param mods The modifications.
	 */
	protected abstract void onMouseClick(int button, int action, int mods);

}
