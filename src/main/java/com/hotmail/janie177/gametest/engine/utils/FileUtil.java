package com.hotmail.janie177.gametest.engine.utils;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileUtil
{

	/**
	 * Create a file if it does not exist yet.
	 * @param file The file to create.
	 * @return A file that was either already there, or newly created.
	 */
	public static File createFile(File file)
	{
		if(!file.exists())
		{
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (Exception e)
			{
				System.out.println("Unable to create file " + file.getName() + "!");
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * Get a file at path + name. If the file does not exist, it will be created.
	 * @param path The path of the file.
	 * @param name The name of the file.
	 * @return The new file, or old file if it already exists.
	 */
	public static File getOrCreateFile(String path, String name)
	{
		File file = new File(path + File.separator + name);
		createFile(file);
		return file;
	}

	/**
	 * Copy a file to another file.
	 * @param srcFile The source file.
	 * @param destFile The destination file.
	 * @throws IOException
	 */
	public static void copyFile(File srcFile, File destFile) throws IOException
	{
		createFile(srcFile);
		createFile(destFile);

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new RandomAccessFile(srcFile,"rw").getChannel();
			destination = new RandomAccessFile(destFile,"rw").getChannel();

			long position = 0;
			long count = source.size();

			source.transferTo(position, count, destination);
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * Save an object to a file.
	 * The object has to implement Serializable.
	 * @param file The file to save to.
	 * @param object The object to save.
	 * @return True if the object was stored, false if something went wrong.
	 */
	public static boolean saveObjectToFile(File file, Object object, boolean append) throws IOException
	{
		//Overwrite old data.
		FileOutputStream out = new FileOutputStream(file, append);
		ObjectOutputStream oOut = new ObjectOutputStream(out);
		//Write the chunk data.
		oOut.writeObject(object);
		oOut.close();
		out.close();
		return true;
	}

	/**
	 * Read an object from a file. Returns the object or null if an error occurred.
	 * @param file The file to read from.
	 * @return The read object. Make sure to cast it to the type it was before serialization.
	 */
	public static Object loadObjectFromFile(File file) throws IOException, ClassNotFoundException
	{
		FileInputStream in = new FileInputStream(file);
		ObjectInputStream oIn = new ObjectInputStream(in);
		Object object = oIn.readObject();
		in.close();
		oIn.close();
		return object;
	}
}
