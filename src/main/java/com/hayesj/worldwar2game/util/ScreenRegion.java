package com.hayesj.worldwar2game.util;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Divides a region of the screen into typical components
 * (0,0) is defined as the bottom left corner.
 *
 * Created by Jacob Hayes on 6/28/2017.
 *
 * @author Jacob Hayes
 */
public class ScreenRegion {

	/**
	 * Created by Jacob Hayes on 6/26/2017.
	 *
	 * @author Jacob Hayes
	 */
	public enum Region { LEFT, RIGHT, TOP, BOTTOM, CENTER
	}

	private Vector2f origin = new Vector2f(0.0f, 0.0f);
	private Vector2f size;
	private Vector3f top;
	private Vector3f bottom;
	private Vector3f left;
	private Vector3f right;
	private Vector3f center;

	/**
	 * For square regions, where all regions are of equal dimension and fuzzy range
	 * @param width the side-length of the region
	 * @param region the (width, height) of all components
	 * @param fuzzy the fuzzy range of all components
	 */
	public ScreenRegion(float width, Vector2f region, float fuzzy) {
		this.size = new Vector2f(width, width);
		this.top = new Vector3f(region.getX(), region.getY(), fuzzy);
		this.bottom = this.top.clone();
		this.left = new Vector3f(region.getY(), region.getX(), fuzzy);
		this.right = this.left.clone();
		this.center = new Vector3f((size.getX() - 2*region.getX()), (size.getY() - 2*region.getY()), 0.0f);
	}

	/**
	 * For rectangular regions, where all components have equal fuzzy range and dimensions are equal for parallel components
	 * @param size the (width, height) of the region
	 * @param horzRegion the (width, height) of the top and bottom components
	 * @param vertRegion the (width, height) of the left and right components
	 * @param fuzzy the fuzzy range of all components
	 */
	public ScreenRegion(Vector2f size, Vector2f horzRegion, Vector2f vertRegion, float fuzzy) {
		this.size = size;
		this.top = new Vector3f(horzRegion.getX(), horzRegion.getY(), fuzzy);
		this.bottom = this.top.clone();
		this.left = new Vector3f(vertRegion.getX(), vertRegion.getY(), fuzzy);
		this.right = this.left.clone();
		this.center = new Vector3f((size.getX() - 2*vertRegion.getX()), (size.getY() - 2*horzRegion.getY()), 0.0f);
	}

	/**
	 * For rectangular regions, where components may be non-symmetrical and fuzzy range is equal for parallel components
	 * @param size the (width, height) of the region
	 * @param topRegion the (width, height) of the top component
	 * @param bottomRegion the (width, height) of the bottom component
	 * @param leftRegion the (width, height) of the left component
	 * @param rightRegion the (width, height) of the right component
	 * @param horzFuzz the fuzzy range of the top and bottom components
	 * @param vertFuzz the fuzzy range of the left and right components
	 */
	public ScreenRegion(Vector2f size, Vector2f topRegion, Vector2f bottomRegion, Vector2f leftRegion, Vector2f rightRegion, float horzFuzz, float vertFuzz) {
		this.size = size;
		this.top = new Vector3f(topRegion.getX(), topRegion.getY(), horzFuzz);
		this.bottom = new Vector3f(bottomRegion.getX(), bottomRegion.getY(), horzFuzz);
		this.left = new Vector3f(leftRegion.getX(), leftRegion.getY(), vertFuzz);
		this.right = new Vector3f(rightRegion.getX(), rightRegion.getY(), vertFuzz);
		this.center = new Vector3f(size.getX() - (leftRegion.getX()+rightRegion.getX()), size.getY() - (topRegion.getY()+bottomRegion.getY()), 0.0f);
	}

	/**
	 * For rectangular regions, where components may be completely irregular
	 * @param size the (width, height) of the region
	 * @param topRegion the (width, height, fuzzy-range) of the top component
	 * @param bottomRegion the (width, height, fuzzy-range) of the bottom component
	 * @param leftRegion the (width, height, fuzzy-range) of the left component
	 * @param rightRegion the (width, height, fuzzy-range) of the right component
	 */
	public ScreenRegion(Vector2f size, Vector3f topRegion, Vector3f bottomRegion, Vector3f leftRegion, Vector3f rightRegion) {
		this.size = size;
		this.top = topRegion;
		this.bottom = bottomRegion;
		this.left = leftRegion;
		this.right = rightRegion;
		this.center = new Vector3f(size.getX() - (leftRegion.getX()+rightRegion.getX()), size.getY() - (topRegion.getY()+bottomRegion.getY()), 0.0f);
	}

	public EnumSet<Region> regionsContaining(Vector2f pt) {
		EnumSet<Region> ret = EnumSet.noneOf(Region.class);

		if (pt.getY() < bottom.getY()+bottom.getZ()) { ret.add(Region.BOTTOM); }
		else if (pt.getY() > size.getY() - (top.getY()+top.getZ()) && pt.getY() < size.getY()) { ret.add(Region.TOP); }

		if (pt.getX() < left.getX()+left.getZ()) { ret.add(Region.LEFT); }
		else if (pt.getX() > size.getX() - (right.getX()+right.getZ()) && pt.getX() < size.getX()) { ret.add(Region.RIGHT); }

		if (ret.isEmpty()) { ret.add(Region.CENTER); }

		return ret;
	}

	public EnumMap<Region, Vector2f[]> getRegions(EnumSet<Region> regions) {
		EnumMap<Region, Vector2f[]> ret = new EnumMap<>(Region.class);

		for (Region rgn : regions) {
			Vector2f[] tmp = new Vector2f[2];
			Vector2f other = new Vector2f();
			Vector2f orgn = new Vector2f();
			float x = 0.0f, y = 0.0f;
			switch (rgn) {
				case TOP:
					orgn = origin.add(new Vector2f(0.0f, left.getY() - top.getY()));
					x = top.getX();
					y = top.getY();
					break;
				case BOTTOM:
					orgn = origin;
					x = bottom.getX();
					y = bottom.getY();
					break;
				case LEFT:
					orgn = origin;
					x = left.getX();
					y = left.getY();
					break;
				case RIGHT:
					orgn = origin.add(new Vector2f(bottom.getX() - right.getX(), 0.0f));
					x = right.getX();
					y = right.getY();
					break;
				case CENTER:
					orgn = origin.add(new Vector2f(left.getX(), bottom.getY()));
					x = center.getX();
					y = center.getY();
					break;
			}

			other.setX(x).setY(y);
			tmp[0] = orgn;
			tmp[1] = orgn.add(other);
			ret.put(rgn, tmp);
		}

		return ret;
	}

	public EnumMap<Region, Float> getFuzzyAreas(EnumSet<Region> regions) {
		EnumMap<Region, Float> ret = new EnumMap<>(Region.class);

		for (Region rgn : regions) {
			float fuzz = 0.0f;
			switch (rgn) {
				case TOP:
					fuzz = top.getZ();
					break;
				case BOTTOM:
					fuzz = bottom.getZ();
					break;
				case LEFT:
					fuzz = left.getZ();
					break;
				case RIGHT:
					fuzz = right.getZ();
					break;
				case CENTER:
					fuzz = center.getZ();
					break;
			}

			ret.put(rgn, fuzz);
		}

		return ret;
	}

	public Vector2f getOrigin() { return origin.clone(); }
	public Vector2f getSize() { return size.clone(); }

	public void setOrigin(Vector2f origin) { this.origin = origin; }
}
