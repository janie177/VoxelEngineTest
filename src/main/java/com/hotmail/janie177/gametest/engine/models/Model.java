package com.hotmail.janie177.gametest.engine.models;

import java.util.ArrayList;
import java.util.List;

public class Model
{
	private int id;
	private int vertexCount;
	private List<Integer> vBOs;

	public Model(int id, int vertexCount)
	{
		this.id = id;
		this.vertexCount = vertexCount;
		vBOs = new ArrayList<>();
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public int getId() {
		return id;
	}

	public void addVBOID(int id)
	{
		vBOs.add(id);
	}

	public List<Integer> getVboIDs()
	{
		return vBOs;
	}
}
