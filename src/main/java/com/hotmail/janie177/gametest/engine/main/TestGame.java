package com.hotmail.janie177.gametest.engine.main;

import com.hotmail.janie177.gametest.engine.display.DisplayManager;
import com.hotmail.janie177.gametest.engine.files.FileHandler;
import com.hotmail.janie177.gametest.engine.managers.ModelManager;
import com.hotmail.janie177.gametest.engine.managers.RenderManager;
import com.hotmail.janie177.gametest.engine.managers.TextureManager;
import com.hotmail.janie177.gametest.engine.managers.WorldManager;
import com.hotmail.janie177.gametest.engine.rendering.Loader;
import com.hotmail.janie177.gametest.engine.world.Location;
import com.hotmail.janie177.gametest.engine.world.blocks.BlockType;
import com.hotmail.janie177.gametest.game.input.KeyBoard;
import com.hotmail.janie177.gametest.game.input.Mouse;
import com.hotmail.janie177.gametest.game.main.GameMain;
import com.hotmail.janie177.gametest.game.main.Options;
import com.hotmail.janie177.gametest.engine.models.Models;
import com.hotmail.janie177.gametest.game.player.Player;

import static com.hotmail.janie177.gametest.game.main.Options.RUNNING;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;


public class TestGame
{
	//The model loader.
	public static Loader loader;

	//The player from which the player can see the world.
	public static Player player;

	//The keyboard Listener
	public static KeyBoardListener keyboard;

	//The mouse listener
	public static MouseListener mouse;

	//The world manager instance
	public static WorldManager worldManager;

	//The main render manager.
	public static RenderManager renderManager;

	//Local variables
	private static long timeBetweenFrames, lastTime, timeBetweenTicks, lastTick, tick = 0;

	//Start everything
	public static void main(String[] args)
	{
		//Load user settings. Create dir if does not exist.
		FileHandler.createFiles();
		FileHandler.loadSettings();

		//Create the window. Set size and all that. Gives WindowID a value, so has to go first.
		DisplayManager.create();

		//Make a keyboard instance. Use milliseconds in which to wait between keyboard updates.
		keyboard = new KeyBoard(1);

		//Make a mouse instance
		mouse = new Mouse();

		//Make an instance of the loader. This loads rendering.
		loader = new Loader();

		//World manager
		worldManager = new WorldManager();
		worldManager.loadWorld("world");

		//Create an instance of the isTransparent manager.
		renderManager = new RenderManager();

		//Create a new player
		player = new Player(new Location(0, 20, 0, worldManager.getWorld(0)));

		//The time between frames.
		lastTime = System.currentTimeMillis();
		timeBetweenFrames = Options.ConfigSettings.FPS.getValue() > 0 ? (1000/(int) Options.ConfigSettings.FPS.getValue()) : 0;

		//The Ticks calculations
		lastTick = System.currentTimeMillis();
		timeBetweenTicks = 1000/Options.TPS;

		//Load the default texture sheet to memory, used for all normal blocks.
		TextureManager.loadBlockTextures();

		//Load all block models + their textures to memory, given that they are special with a custom model and texture.
		for(BlockType b : BlockType.values())
		{
			if(b.isSpecial()) b.load();
		}

		//Load all models in the array to memory in OpenGL. If there is going to be A LOT of models this could be done more efficiently.
		for(Models m : Models.values())
		{
			m.load();
		}

		//Start the game loop
		gameLoop();
	}

	//TODO REMOVE
	private static long lastCheck = 0;

	private static void gameLoop()
	{
		int saveInterval = Options.SAVE_INTERVAL;

		//Set the FPS counter time.
		debug_fps_start_time = System.currentTimeMillis();

		//TestGame game loop.
		while(!glfwWindowShouldClose(Options.WINDOW_ID) && RUNNING)
		{

			//TODO REMOVE
			long difference = System.currentTimeMillis() - lastCheck;
			if(difference > 40) System.out.println("Something hogged the main thread for more than 0.04 seconds: " + difference + "!!!!!!!!!!!!!!!!!!!");
			lastCheck = System.currentTimeMillis();




			//Update the keyboard Outside the FPS cap. Otherwise it wont update at the same rate on slower PC's.
			keyboard.updateAllKeys();

			//Update the game logic, limited to TPS. Everything inside here happens once a tick.
			if(System.currentTimeMillis() - lastTick >= timeBetweenTicks)
			{
				//Update chunk queueing, loading and unloading.
				player.getWorld().getChunkLoader().executeChunkQueue();

				//Update all game logic.
				GameMain.mainGameLoop(tick++);

				//Save the world every x minutes.
				if(tick % saveInterval == 0)
				{
					worldManager.getWorlds().stream().forEach(w -> w.saveWorld(false));
				}

				lastTick = System.currentTimeMillis();
			}

			//FPS cap. Set in options.
			if(System.currentTimeMillis() - lastTime >= timeBetweenFrames)
			{
				//Render
				renderManager.renderAll(player);

				//Update the display.
				DisplayManager.update();
				//Set the last time the frame was updates for FPS caps.
				lastTime = System.currentTimeMillis();

				//A frame was rendered, add to the FPS counter. Then check if a second has passed, and display the FPS in the console.
				debug_fps++;
				if(System.currentTimeMillis() - debug_fps_start_time > 1000)
				{
					debug_fps_start_time = System.currentTimeMillis();
					System.out.println("FPS: " + debug_fps);
					debug_fps = 0;
				}
			}
		}

		//Save the worlds and all chunks.
		worldManager.unloadAllWorlds();

		//Clean memory after rendering
		renderManager.disableAll();

		//Close the window.
		DisplayManager.close();

		//Clean up memory by removing loaded VAO's and VBO's
		ModelManager.deleteAllModels();

		//Remove all textures from memory
		TextureManager.deleteAllTextures();
	}

	private static long debug_fps_start_time = 0;
	private static long debug_fps = 0;
}
