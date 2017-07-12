package com.hayesj.worldwar2game.input;

/**
 * Created by Jacob Hayes on 5/16/2017.
 *
 * @author Jacob Hayes
 */
public enum UserAction {
	MOVE_WEST(false),
	MOVE_EAST(false),
	MOVE_NORTH(false),
	MOVE_SOUTH(false),

	MOUSE_WEST(false),
	MOUSE_EAST(false),
	MOUSE_NORTH(false),
	MOUSE_SOUTH(false),

	ZOOM_IN(false),
	ZOOM_OUT(false),

	SELECT(false),
	EXECUTE(false);

	private boolean isActive;

	public boolean isActive() { return isActive; }
	public void setActive(boolean isActive) { this.isActive = isActive; }

	UserAction(boolean isActive) {
		this.isActive = isActive;
	}
}
