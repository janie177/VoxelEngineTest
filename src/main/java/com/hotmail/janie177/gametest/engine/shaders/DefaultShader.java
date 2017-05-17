package com.hotmail.janie177.gametest.engine.shaders;

import com.hotmail.janie177.gametest.engine.entities.Camera;
import com.hotmail.janie177.gametest.engine.entities.Light;
import com.hotmail.janie177.gametest.engine.utils.MathUtil;
import com.hotmail.janie177.gametest.game.main.Options;
import org.joml.Matrix4f;

public class DefaultShader extends Shader {

	private static final String vertexFile = Options.SHADERS_PATH + "defaultVertexShader.txt";
	private static final String fragmentFile = Options.SHADERS_PATH + "defaultFragmentShader.txt";
	private int transformationMatrixLocation;
	private int projectionMatrixLocation;
	private int viewMatrixLocation;
	private int lightPositionLocation;
	private int lightColorLocation;
	private int reflectivityLocation;
	private int shineDamperLocation;

	public DefaultShader() {
		super(vertexFile, fragmentFile);
	}

	@Override
	protected void getAllUniformLocations()
	{
		transformationMatrixLocation = super.getUniformLocation("transformationMatrix");
		projectionMatrixLocation = super.getUniformLocation("projectionMatrix");
		viewMatrixLocation = super.getUniformLocation("viewMatrix");
		lightColorLocation = super.getUniformLocation("lightColour");
		lightPositionLocation = super.getUniformLocation("lightPosition");
		shineDamperLocation = super.getUniformLocation("shineDamper");
		reflectivityLocation = super.getUniformLocation("reflectivity");
	}

	public void loadTransformationMatrix(Matrix4f matrix)
	{
		super.loadMatrix(transformationMatrixLocation, matrix);
	}

	public void loadLight(Light light)
	{
		super.loadVector(lightPositionLocation, light.getLocation());
		super.loadVector(lightColorLocation, light.getColour());
	}

	public void loadviewMatrix(Matrix4f viewMatrix)
	{
		super.loadMatrix(viewMatrixLocation, viewMatrix);
	}

	public void loadShineVariables(float dampening, float reflectivity)
	{
		super.loadFloat(shineDamperLocation, dampening);
		super.loadFloat(reflectivityLocation, reflectivity);
	}

	public void loadProjectionMatrix(Matrix4f matrix)
	{
		super.loadMatrix(projectionMatrixLocation, matrix);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
		super.bindAttribute(2, "normal");
	}
}
