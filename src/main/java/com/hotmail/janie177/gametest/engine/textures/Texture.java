package com.hotmail.janie177.gametest.engine.textures;

public class Texture
{
	private int id;
	private float shineDamper = 1;
	private float reflectivity = 0;
	private boolean isTransparent = false;

	public Texture(int id)
	{
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public void setShineDampening(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	public boolean isTransparent() {
		return isTransparent;
	}

	public void setTransparent(boolean transparent) {
		isTransparent = transparent;
	}
}
