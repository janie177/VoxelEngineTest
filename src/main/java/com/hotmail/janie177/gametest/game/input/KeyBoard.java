package com.hotmail.janie177.gametest.game.input;

import com.hotmail.janie177.gametest.engine.main.KeyBoardListener;
import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.world.blocks.Block;
import com.hotmail.janie177.gametest.engine.world.blocks.BlockFace;
import com.hotmail.janie177.gametest.game.main.Options;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class KeyBoard extends KeyBoardListener 
{
	/**
	 * Create a new Keyboard Listener.
	 *
	 * @param intervalBetweenUpdates The interval between updating.
	 */
	public KeyBoard(long intervalBetweenUpdates) {
		super(intervalBetweenUpdates);
	}

	@Override
	public void updateKeys() 
	{
		if(isKeyPressed(GLFW_KEY_A))
		{
			TestGame.player.moveLeft();
		}
		else if(isKeyPressed(GLFW_KEY_D))
		{
			TestGame.player.moveRight();
		}

		if(isKeyPressed(GLFW_KEY_W))
		{
			TestGame.player.moveForward();
		}
		else if(isKeyPressed(GLFW_KEY_S))
		{
			TestGame.player.moveBackward();
		}
		if(isKeyPressed(GLFW_KEY_SPACE))
		{
			TestGame.player.moveUp();
		}
		else if(isKeyPressed(GLFW_KEY_LEFT_CONTROL))
		{
			TestGame.player.moveDown();
		}

		//Key camera movement.
		if(isKeyPressed(GLFW_KEY_RIGHT))
		{
			TestGame.player.addYaw(0.02F);
		}
		if(isKeyPressed(GLFW_KEY_LEFT))
		{
			TestGame.player.addYaw(-0.02F);
		}
		if(isKeyPressed(GLFW_KEY_UP))
		{
			TestGame.player.addPitch(-0.02F);
		}
		if(isKeyPressed(GLFW_KEY_DOWN))
		{
			TestGame.player.addPitch(0.02F);
		}
	}

	public void onSingleKeyPress(long window, int key, int scancode, int action, int mods)
	{
		if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
		{
			//Stop the program.
			Options.RUNNING = false;
		}
		else if(key == GLFW_KEY_G && action == GLFW_RELEASE)
		{
			System.out.println("Position: " + TestGame.player.getLocation().x + " " + TestGame.player.getLocation().y + " " + TestGame.player.getLocation().z);
		}
		else if(key == GLFW_KEY_1 && action == GLFW_RELEASE)
		{
			TestGame.player.getLocation().getWorld().getBlockAt(TestGame.player.getLocation().x, TestGame.player.getLocation().y, TestGame.player.getLocation().z).setId(1);
		}
		else if(key == GLFW_KEY_2 && action == GLFW_RELEASE)
		{
			TestGame.player.getLocation().getWorld().getBlockAt(TestGame.player.getLocation().x, TestGame.player.getLocation().y, TestGame.player.getLocation().z).setId(2);
		}
		else if(key == GLFW_KEY_3 && action == GLFW_RELEASE)
		{
			TestGame.player.getLocation().getWorld().getBlockAt(TestGame.player.getLocation().x, TestGame.player.getLocation().y, TestGame.player.getLocation().z).setId(3);
		}
		else if(key == GLFW_KEY_4 && action == GLFW_RELEASE)
		{
			TestGame.player.getLocation().getWorld().getBlockAt(TestGame.player.getLocation().x, TestGame.player.getLocation().y, TestGame.player.getLocation().z).setId(4);
		}
		else if(key == GLFW_KEY_0 && action == GLFW_RELEASE)
		{
			TestGame.player.getLocation().getWorld().getBlockAt(TestGame.player.getLocation().x, TestGame.player.getLocation().y, TestGame.player.getLocation().z).setId(0);
		}
	}
}
