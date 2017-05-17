package com.hotmail.janie177.gametest.game.main;

import com.hotmail.janie177.gametest.engine.files.ConfigOption;

import java.io.File;

public class Options
{
	//File path
	public static final String FILE_PATH = System.getProperty("user.home") + File.separator + Options.GAME_NAME + File.separator;
	public static final String ASSETS_PATH = FILE_PATH + File.separator + "assets" + File.separator;
	public static final String MODELS_PATH = ASSETS_PATH + File.separator + "models" + File.separator;
	public static final String TEXTURES_PATH = ASSETS_PATH + File.separator + "textures" + File.separator;
	public static final String SHADERS_PATH = ASSETS_PATH + File.separator + "shaders" + File.separator;

	//The version number of OpenGL to use. 3 is recommended.
	public static final int VERSION_MAJOR = 3;

	//The minor version of OpenGL to use. 3 is recommended.
	public static final int VERSION_MINOR = 3;

	//The center of the screen. Used for mouse positions.
	public static int CENTER_X = (int) ConfigSettings.SCREEN_WIDTH.getValue() / 2;
	public static double CENTER_Y = ConfigSettings.SCREEN_HEIGHT.getValue()  / 2;

	//Mipmapping
	public static final int BLOCK_MIPMAPS = 4;

	//block texture size
	public static final int BLOCK_TEXTURE_SIZE = 64;

	public static int BLOCK_ATLAS_WIDTH = -1;
	public static int BLOCK_ATLAS_HEIGHT = -1;

	//How near to the screen the closest objects are.
	public static float NEAR_PLANE = 0.1f;

	//How far the furthest objects are.
	public static float FAR_PLANE = 1000;

	//The GAME_NAME of the game window.
	public static final String GAME_NAME = "Game Test";

	//The ID of the window. Do not change this ever. It will automatically be assigned a value on start, after which it can be used to refer to the window.
	public static long WINDOW_ID = 0;

	//Wether the game is running or not. Set to false to stop the program.
	public static boolean RUNNING = true;

	//How often the game logic updates.
	public static int TPS = 20;

	//The save interval in ticks. 6000 = 5 minutes if tps = 20.
	public static int SAVE_INTERVAL = 6000;


	/**
	 * Enum containing all options that can be user edited.
	 */
	public enum ConfigSettings
	{
		FOV(new ConfigOption("Field-Of-View", 70, 70)),
		FPS(new ConfigOption("Max-FPS", 300, 300)),
		SCREEN_WIDTH(new ConfigOption("Screen-Width", 600, 600)),
		SCREEN_HEIGHT(new ConfigOption("Screen-Height", 400, 400)),
		FULL_SCREEN(new ConfigOption("Full-Screen-Mode", 1, 1)),
		MOUSE_SENSITIVITY(new ConfigOption("Mouse-Sensitivity", 0.3f, 0.3f)),
		RENDER_DISTANCE(new ConfigOption("render-distance", 3, 3)),
		AA_SAMPLES(new ConfigOption("AntiAliasing-Samples", 4, 4));

		private ConfigSettings(ConfigOption option)
		{
			this.option = option;
		}

		private ConfigOption option;

		public ConfigOption getOption()
		{
			return option;
		}

		public float getDefaultValue()
		{
			return option.getDefaultValue();
		}

		public String getKey()
		{
			return option.getKey();
		}

		public float getValue()
		{
			return option.getValue();
		}

		public void setValue(float value)
		{
			option.setValue(value);
		}
	}
}
