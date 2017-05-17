package com.hotmail.janie177.gametest.engine.models;

public class ModelData
{
	private float[] vertices;
	private float[] textureCoords;
	private float[] normals;
	private int[] indices;
	private float furthestPoint;

	public ModelData(float[] vertices, float[] textureCoords, float[] normals, int[] indices, float furthestPoint) {
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		this.furthestPoint = furthestPoint;
	}

	//The vertex points. X Y Z coordinates.
	public float[] getVertices() {
		return vertices;
	}

	//The Texture coordinates. X Y coordinates.
	public float[] getTextureCoords() {
		return textureCoords;
	}

	//Normals X Y Z
	public float[] getNormals() {
		return normals;
	}

	//Get the order in which to connect the vertices. 1 2 3, 1 2 3, 3 2 1 etc
	public int[] getIndices() {
		return indices;
	}

	public float getFurthestPoint() {
		return furthestPoint;
	}
}
