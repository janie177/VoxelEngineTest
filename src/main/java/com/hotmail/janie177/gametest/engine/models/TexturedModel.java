package com.hotmail.janie177.gametest.engine.models;

import com.hotmail.janie177.gametest.engine.textures.Texture;

public class TexturedModel {

	private Model model;
	private Texture texture;

	public TexturedModel(Model model, Texture texture)
	{
		this.model = model;
		this.texture = texture;
	}

	public Texture getTexture() {
		return texture;
	}

	public Model getModel() {
		return model;
	}
}
