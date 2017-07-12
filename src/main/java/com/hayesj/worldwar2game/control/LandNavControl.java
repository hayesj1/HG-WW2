package com.hayesj.worldwar2game.control;

import com.hayesj.worldwar2game.world.Tile;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

import java.util.ArrayList;

/**
 * Created by Jacob Hayes on 6/7/2017.
 *
 * @author Jacob Hayes
 */
public class LandNavControl extends AbstractControl {

	protected ArrayList<Tile> openList = new ArrayList<>();

	@Override
	protected void controlUpdate(float tpf) {
		//spatial.setLocalTranslation();
		/* // A* Path finding
		openList.add(originalSquare); // start by adding the original position to the open list
		do {
			currentSquare = openList.getSquareWithLowestFScore(); // Get the square with the lowest F score

			closedList.add(currentSquare); // add the current square to the closed list
			openList.remove(currentSquare); // remove it to the open list

			if (closedList.contains(destinationSquare)) { // if we added the destination to the closed list, we've found a path
				// PATH FOUND
				break; // break the loop
			}

			adjacentSquares = currentSquare.walkableAdjacentSquares(); // Retrieve all its walkable adjacent squares

			for (aSquare : adjacentSquares) {

				if (closedList.contains(aSquare)) { // if this adjacent square is already in the closed list ignore it
					continue; // Go to the next adjacent square
				}

				if (!openList.contains(aSquare)) { // if its not in the open list
					// compute its score, set the parent
					openList.add(aSquare]; // and add it to the open list

				} else { // if its already in the open list
					// test if using the current G score makes the aSquare F score lower, if yes update the parent because it means its a better path

				}
			}

		} while(!openList.isEmpty()); // Continue until there is no more available square in the open list (which means there is no path)
		*/
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {}
}
