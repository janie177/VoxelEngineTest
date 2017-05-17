package com.hotmail.janie177.gametest.engine.files;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.game.main.Options;

import java.io.*;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileHandler
{
	private static String path = System.getProperty("user.home") + File.separator + Options.GAME_NAME + File.separator;
	private static File configFile = new File(path + "settings" + File.separator + "settings.conf");
	private static String assetsLocation = path + "assets" + File.separator;
	private static File textures = new File(assetsLocation + "textures" + File.separator);
	private static File shaders = new File(assetsLocation + "shaders" + File.separator);
	private static File models = new File(assetsLocation + "models" + File.separator);

	public static void createFiles()
	{
		//Create new Settingsfile if it doesnt exist yet.
		if(!configFile.exists())
		{
			configFile.getParentFile().mkdirs();

			try {
				configFile.createNewFile();

				FileWriter writer = new FileWriter(configFile, true);
				Properties properties = new Properties();

				//Fill in the file with all config values.
				for(Options.ConfigSettings setting : Options.ConfigSettings.values())
				{
					properties.setProperty(setting.getKey(), String.valueOf(setting.getDefaultValue()));
				}

				properties.store(writer, Options.GAME_NAME + " User Settings.");

				writer.close();

			} catch (Exception e) {
				System.out.println("Error while creating Settings file.");
				e.printStackTrace();
			}
		}

		File assets = new File(assetsLocation);
		//Create assets
		if(!assets.exists())
		{
			try {
				assets.mkdir();
			} catch (Exception e)
			{
				System.out.println("Error while creating assets folder.");
				e.printStackTrace();
			}
		}

		//Create shaders, textures and unpack them
		if(!textures.exists())
		{
			try {
				textures.mkdir();
			} catch (Exception e)
			{
				System.out.println("Error while creating Textures folder.");
				e.printStackTrace();
			}
		}

		if(!shaders.exists())
		{
			try {
				shaders.mkdir();
			} catch (Exception e)
			{
				System.out.println("Error while creating Textures folder.");
				e.printStackTrace();
			}
		}

		if(!models.exists())
		{
			try {
				models.mkdir();
			} catch (Exception e)
			{
				System.out.println("Error while creating Textures folder.");
				e.printStackTrace();
			}
		}

		unpackAssets();
	}

	private static void fromResourceToFile(String pathIn, String pathOut)
	{

		try
		{
			InputStream in;
			OutputStream out;

			File newFile = new File(pathOut);

			/** Do not overwrite old textures when they already exist. This speeds up the process and allows texture/shader/model editing. **/
			if(!newFile.exists())
			{
				System.out.println("Unpacking file " + pathIn + "...");
				in = TestGame.class.getClass().getResourceAsStream("/" + pathIn);
				out = new FileOutputStream(pathOut);
				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = in.read(bytes)) != -1)
				{
					out.write(bytes, 0, read);
				}

				out.flush();

				in.close();
				out.close();
			}



		} catch (IOException io)
		{
			System.out.println("An error occurred when creating a file from an input stream.");
			io.printStackTrace();
		}
	}

	private static void unpackAssets()
	{
		final File jarFile = new File(TestGame.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		if(jarFile.isFile()) {
			try {
				final JarFile jar = new JarFile(jarFile);
				final Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements())
				{
					final String name = entries.nextElement().getName();
					if (name.matches(".*\\.obj"))
					{
						fromResourceToFile(name, models.getPath() + File.separator + Paths.get(name).getFileName());
					}
					else if (name.matches(".*\\.png"))
					{
						fromResourceToFile(name, textures.getPath() + File.separator + Paths.get(name).getFileName());
					}
					else if(name.matches(".*\\.txt"))
					{
						fromResourceToFile(name, shaders.getPath() + File.separator + Paths.get(name).getFileName());
					}
				}
				jar.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public static void saveSetting(Options.ConfigSettings setting)
	{
		try
		{
			FileWriter writer = new FileWriter(configFile, true);
			Properties properties = new Properties();

			properties.setProperty(setting.getKey(), String.valueOf(setting.getValue()));

			properties.store(writer, Options.GAME_NAME + " User Settings.");

			writer.close();
		} catch (Exception e)
		{
			System.out.println("An error occured when trying to save user settings.");
			e.printStackTrace();
		}
	}

	public static void loadSettings()
	{
		File file = new File(path + "settings" + File.separator + "settings.conf");
		try {
			FileReader reader = new FileReader(file);
			Properties properties = new Properties();
			properties.load(reader);

			//Load all config settings. Default to their default value if not found.
			for(Options.ConfigSettings setting : Options.ConfigSettings.values())
			{
				setting.setValue(Float.parseFloat(properties.getProperty(setting.getKey(), String.valueOf(setting.getDefaultValue()))));
			}

			reader.close();
		} catch (Exception e)
		{
			System.out.println("Error while reading from Settings file.");
			e.printStackTrace();
		}
	}
}
