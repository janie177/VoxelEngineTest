package com.hotmail.janie177.gametest.engine.entities;

import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.engine.world.Location;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera extends AbstractRenderingEntity
{
	private Vector3f velocity = new Vector3f(0, 0, 0);
	private float speed = 0.015f;
	private float pitch = 0;
	private float yaw = 0;
	private float roll = 0;

	public Camera(Location location)
	{
		super(location);
	}

	/**
	 * Move the player at a certain speed, and take the pitch/yaw into account for the direction.
	 * @param speed The speed to move at.
	 * @param yawOffset The offset. Is used for strafing (+ and - 90 degrees).
	 */
	public void moveLocationNaturally(float speed, float yawOffset, boolean moveHeight)
	{
		float yawR = (float) Math.toRadians(yaw + yawOffset - 90), pitchR = (float) Math.toRadians(pitch);

		velocity.x = (float)(Math.cos(yawR) * Math.cos(pitchR));
		velocity.z = (float)(Math.sin(yawR) * Math.cos(pitchR));
		if(moveHeight) velocity.y = -(float) (Math.sin(pitchR));
		else velocity.y = 0;

		velocity.normalize();
		velocity.mul(speed);
		getLocation().add(velocity);


		//MOVE THE SUN
		TestGame.player.getWorld().getSun().setLocation(getLocation().clone().add(100,100,100));

	}

	public void moveUp()
	{
		getLocation().y += speed;
	}
	public void moveDown()
	{
		getLocation().y -= speed;
	}
	public void moveRight()
	{
		moveLocationNaturally(speed, 90, false);
	}
	public void moveLeft()
	{
		moveLocationNaturally(speed, -90, false);
	}
	public void moveForward()
	{
		moveLocationNaturally(speed, 0, true);
	}
	public void moveBackward()
	{
		moveLocationNaturally(-speed, 0, true);
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch % 360;
	}

	public void addPitch(float added)
	{
		setPitch(added + pitch);
	}

	public void addYaw(float added)
	{
		setYaw(added + yaw);
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw)
	{
		this.yaw = yaw % 360;
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}
}
