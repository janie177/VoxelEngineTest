package com.hotmail.janie177.gametest.engine.utils;

import com.hotmail.janie177.gametest.engine.world.chunks.ChunkHelper;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ViewFrustum {
	private static final float frustum[][] = new float[6][4];

	public static void update(Matrix4f viewMatrix, Matrix4f projectionMatrix)
	{
		Matrix4f frustumMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

		// Right
		frustum[0][0] = frustumMatrix.m03() - frustumMatrix.m00();
		frustum[0][1] = frustumMatrix.m13() - frustumMatrix.m10();
		frustum[0][2] = frustumMatrix.m23() - frustumMatrix.m20();
		frustum[0][3] = frustumMatrix.m33() - frustumMatrix.m30();

		// Left
		frustum[1][0] = frustumMatrix.m03() + frustumMatrix.m00();
		frustum[1][1] = frustumMatrix.m13() + frustumMatrix.m10();
		frustum[1][2] = frustumMatrix.m23() + frustumMatrix.m20();
		frustum[1][3] = frustumMatrix.m33() + frustumMatrix.m30();

		// Bottom
		frustum[2][0] = frustumMatrix.m03() + frustumMatrix.m01();
		frustum[2][1] = frustumMatrix.m13() + frustumMatrix.m11();
		frustum[2][2] = frustumMatrix.m23() + frustumMatrix.m21();
		frustum[2][3] = frustumMatrix.m33() + frustumMatrix.m31();

		// Top
		frustum[3][0] = frustumMatrix.m03() - frustumMatrix.m01();
		frustum[3][1] = frustumMatrix.m13() - frustumMatrix.m11();
		frustum[3][2] = frustumMatrix.m23() - frustumMatrix.m21();
		frustum[3][3] = frustumMatrix.m33() - frustumMatrix.m31();

		// Far
		frustum[4][0] = frustumMatrix.m03() - frustumMatrix.m02();
		frustum[4][1] = frustumMatrix.m13() - frustumMatrix.m12();
		frustum[4][2] = frustumMatrix.m23() - frustumMatrix.m22();
		frustum[4][3] = frustumMatrix.m33() - frustumMatrix.m32();

		// Near
		frustum[5][0] = frustumMatrix.m03() + frustumMatrix.m02();
		frustum[5][1] = frustumMatrix.m13() + frustumMatrix.m12();
		frustum[5][2] = frustumMatrix.m23() + frustumMatrix.m22();
		frustum[5][3] = frustumMatrix.m33() + frustumMatrix.m32();

		// Normalize
		for (int i = 0; i < 6; i++) {
			float t = (float) Math.sqrt(frustum[i][0] * frustum[i][0] + frustum[i][1] * frustum[i][1] + frustum[i][2] * frustum[i][2]);
			for (int j = 0; j < 4; j++) {
				frustum[i][j] /= t;
			}
		}
	}

	public static boolean chunkIsVisible(int cx, int cz)
	{
		float x = ChunkHelper.calculateWorldPosition(cx);
		float z = ChunkHelper.calculateWorldPosition(cz);

		return boxInFrustum(x, 0, z, 512, 16);
	}

	//Returns true if the given point is inside the view frustum
	public static boolean pointIsVisible(float x, float y, float z)
	{
		for (int i = 0; i < 6; i++) {
			if (frustum[i][0] * x + frustum[i][1] * y + frustum[i][2] * z + frustum[i][3] <= 0.0F) {
				return false;
			}
		}
		return true;
	}

	//Returns true if the given cube is inside the view frustum
	public static boolean boxInFrustum(float x, float y, float z, float height, float width)
	{
		float maxX = x + width;
		float maxZ = z + width;
		float maxY = y + height;
		
		// check box outside/inside of frustum
		for( int i=0; i<6; i++ )
		{
			int out = 0;
			Vector4f frustumV = new Vector4f(frustum[i][0], frustum[i][1], frustum[i][2], frustum[i][3]);
			
			out += ((frustumV.dot(new Vector4f(x, y, z, 1.0f) ) < 0.0 )?1:0);
			out += ((frustumV.dot(new Vector4f(maxX, y, z, 1.0f) ) < 0.0 )?1:0);
			out += ((frustumV.dot(new Vector4f(x, maxY, z, 1.0f) ) < 0.0 )?1:0);
			out += ((frustumV.dot(new Vector4f(maxX, maxY, z, 1.0f) ) < 0.0 )?1:0);
			out += ((frustumV.dot(new Vector4f(x, y, maxZ, 1.0f) ) < 0.0 )?1:0);
			out += ((frustumV.dot(new Vector4f(maxX, y, maxZ, 1.0f) ) < 0.0 )?1:0);
			out += ((frustumV.dot(new Vector4f(x, maxY, maxZ, 1.0f) ) < 0.0 )?1:0);
			out += ((frustumV.dot(new Vector4f(maxX, maxY, maxZ, 1.0f) ) < 0.0 )?1:0);
			if( out==8 ) return false;
		}
		return true;
	}

}
