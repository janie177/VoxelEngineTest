package com.hotmail.janie177.gametest.engine.entities;

import com.hotmail.janie177.gametest.engine.models.TexturedModel;
import com.hotmail.janie177.gametest.engine.world.Location;

public class RenderingEntity extends AbstractRenderingEntity
{
	private TexturedModel model;
	private float rotX, rotY, rotZ;
	private float scale;

	public RenderingEntity(TexturedModel model, Location location, float rotX, float rotY, float rotZ, float scale)
	{
		super(location);
		this.model = model;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}

	public TexturedModel getModel() {
		return model;
	}

	public void setModel(TexturedModel model) {
		this.model = model;
	}

	public float getRotY() {
		return rotY;
	}

	public void setRotY(float rotY) {
		this.rotY = rotY;
	}

	public float getRotZ() {
		return rotZ;
	}

	public void setRotZ(float rotZ) {
		this.rotZ = rotZ;
	}

	public float getRotX() {
		return rotX;
	}

	public void setRotX(float rotX) {
		this.rotX = rotX;
	}

	public float getScale() {
		return scale;
	}

	public void increaseRotation(float dx, float dy, float dz)
	{
		this.rotX += dx;
		this.rotY += dy;
		this.rotZ += dz;

	}

	public void setScale(float scale) {
		this.scale = scale;
	}
}
