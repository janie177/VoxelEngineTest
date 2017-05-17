package com.hotmail.janie177.gametest.engine.shaders;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

public abstract class Shader {

	private int shaderID;
	private int vertexShaderID;
	private int fragmentShaderID;
	private static FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

	public Shader(String vertexFile,String fragmentFile)
	{
		vertexShaderID = loadShader(vertexFile,GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile,GL20.GL_FRAGMENT_SHADER);
		shaderID = GL20.glCreateProgram();
		GL20.glAttachShader(shaderID, vertexShaderID);
		GL20.glAttachShader(shaderID, fragmentShaderID);
		bindAttributes();
		GL20.glLinkProgram(shaderID);
		GL20.glValidateProgram(shaderID);
		getAllUniformLocations();
	}

	protected int getUniformLocation(String uniform)
	{
		return GL20.glGetUniformLocation(shaderID, uniform);
	}

	protected abstract void getAllUniformLocations();

	protected void loadFloat(int location, float value)
	{
		GL20.glUniform1f(location, value);
	}

	protected void loadVector(int location, Vector3f vector)
	{
		GL20.glUniform3f(location, vector.x, vector.y, vector.z);
	}

	protected void loadBoolean(int location, boolean bool)
	{
		GL20.glUniform1f(location, bool ? 1 : 0);
	}

	protected void loadMatrix(int location, Matrix4f matrix)
	{
		//Store the matrix data in OpenGL format in the buffer.
		matrix.get(buffer);
		//DO NOT FLIP THE BUFFER HERE. The get method stores it  as is.
		//Supply the matrix that was just loaded to OpenGL format to OpenGL.
		GL20.glUniformMatrix4fv(location, false, buffer);
	}

	public void start(){
		GL20.glUseProgram(shaderID);
	}

	public void stop(){
		GL20.glUseProgram(0);
	}

	public void cleanMemory(){
		stop();
		GL20.glDetachShader(shaderID, vertexShaderID);
		GL20.glDetachShader(shaderID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(shaderID);
	}

	protected abstract void bindAttributes();

	protected void bindAttribute(int attribute, String variableName){
		GL20.glBindAttribLocation(shaderID, attribute, variableName);
	}

	private static int loadShader(String file, int type)
	{
		StringBuilder shaderSource = new StringBuilder();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine())!=null){
				shaderSource.append(line).append("//\n");
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS )== GL11.GL_FALSE){
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("An error occurred while compiling a shader.");
			System.exit(-1);
		}
		return shaderID;
	}

}
