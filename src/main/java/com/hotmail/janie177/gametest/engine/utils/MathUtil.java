package com.hotmail.janie177.gametest.engine.utils;

import com.hotmail.janie177.gametest.engine.entities.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MathUtil
{
	/**
	 * Create a matrix4f containing data about rotation and scale.
	 * @param location The vector to translate.
	 * @param rx The x rotation.
	 * @param ry The y rotation.
	 * @param rz The z rotation.
	 * @param scale The scale. 1.0 being normal size.
	 * @return  A new matrix4f that contains all the information supplied in compact format.
	 */
	public static Matrix4f createTransformationMatrix(Vector3f location, float rx, float ry, float rz, float scale)
	{
		//Create a new matrix that can store 16 floats.
		Matrix4f matrix = new Matrix4f();
		//Set the matrix identity. This resets the matrix to its defaults. Important!
		matrix.identity();
		//Store the location in the matrix
		matrix.translate(location);
		//Store the rotation axes in the matrix.
		matrix.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0));
		matrix.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0));
		matrix.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1));
		//Store the scale in the matrix.
		matrix.scale(new Vector3f(scale, scale, scale), matrix);
		return matrix;
	}

	//Pretty much the same thing as above this.
	public static Matrix4f createViewMatrix(Camera camera)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.identity();
		matrix.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1,0,0));
		matrix.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0,1,0));
		matrix.rotate((float) Math.toRadians(camera.getRoll()), new Vector3f(0,0,1));
		Vector3f cameraPosition = camera.getLocation();
		//Negative since the world has to move the other way of the player.
		Vector3f negativePosition = new Vector3f(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
		matrix.translate(negativePosition);
		return matrix;
	}

}
