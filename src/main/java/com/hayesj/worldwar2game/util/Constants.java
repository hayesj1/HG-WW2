package com.hayesj.worldwar2game.util;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;



/**
 * Created by Jacob Hayes on 5/12/2017.
 *
 * @author Jacob Hayes
 */
public interface Constants {
	float BASE_MOVE_SPEED = 0.25f;
	float BASE_CAM_SPEED = 0.25f;
	float MAX_MOVE_SPEED = BASE_MOVE_SPEED * 10.0f;
	float MAX_CAM_SPEED = BASE_CAM_SPEED * 10.0f;

	Quaternion isometricRotation = new Quaternion(new float[] {FastMath.QUARTER_PI, 0.0f, 0.0f});
}
