package com.hotmail.janie177.gametest.game.input;

import com.hotmail.janie177.gametest.engine.main.MouseListener;
import com.hotmail.janie177.gametest.engine.main.TestGame;
import com.hotmail.janie177.gametest.game.main.Options;

public class Mouse extends MouseListener
{
	public void onMouseUpdate(double xMoved, double yMoved)
	{
		float mouseSensitivity = Options.ConfigSettings.MOUSE_SENSITIVITY.getValue();
		TestGame.player.addYaw(-( (((float)xMoved) / 5 * mouseSensitivity)));
		TestGame.player.addPitch(-( (((float)yMoved) / 5 * mouseSensitivity)));
	}

	@Override
	protected void onMouseClick(int button, int action, int mods) {

	}
}
