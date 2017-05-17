package com.hotmail.janie177.gametest.engine.files;

public class ConfigOption
{
	private String key;
	private float value;
	private float defaultValue;

	public ConfigOption(String key, float value, float defaultValue)
	{
		this.value = value;
		this.key = key;
		this.defaultValue = defaultValue;
	}

	public String getKey()
	{
		return key;
	}

	public float getValue()
	{
		return value;
	}

	public void setValue(float value)
	{
		this.value = value;
	}

	public float getDefaultValue()
	{
		return defaultValue;
	}
}
