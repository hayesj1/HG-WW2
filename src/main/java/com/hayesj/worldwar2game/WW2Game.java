package com.hayesj.worldwar2game;

import com.hayesj.worldwar2game.control.LandNavControl;
import com.hayesj.worldwar2game.input.UserAction;
import com.hayesj.worldwar2game.util.Constants;
import com.hayesj.worldwar2game.util.Directions;
import com.hayesj.worldwar2game.util.ScreenRegion;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.StripBox;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Jacob Hayes on 9/14/2016.
 *
 * @author Jacob Hayes
 */

public class WW2Game extends SimpleApplication implements AnimEventListener {
	private float camSpeed = Constants.MIN_CAM_SPEED;

	private Node collidables;
	private Node selectables;
	private Node wireFrames;

	private List<Spatial> selected = new ArrayList<>(10);

	private BulletAppState bulletAppState;
	private Spatial sceneModel;
	private Spatial player;
	private BitmapText cursorPos;
	private Spatial originCube;

	private ColorRGBA playerColor = ColorRGBA.Blue;

	private UserAction mvWest = UserAction.MOVE_WEST, mvEast = UserAction.MOVE_EAST;
	private UserAction mvNorth = UserAction.MOVE_NORTH, mvSouth = UserAction.MOVE_SOUTH;
	private UserAction msWest = UserAction.MOUSE_WEST, msEast = UserAction.MOUSE_EAST;
	private UserAction msNorth = UserAction.MOUSE_NORTH, msSouth = UserAction.MOUSE_SOUTH;
	private UserAction zoomIn = UserAction.ZOOM_IN, zoomOut = UserAction.ZOOM_OUT;
	private UserAction select = UserAction.SELECT, execute = UserAction.EXECUTE;

	private boolean debugMode = false;
	private CombinedListener combinedListener = new CombinedListener();

	private Geometry markR;
	private Geometry markL;
	private ScreenRegion screen;

	@Override
	public void simpleInitApp() {
		this.cursorPos = new BitmapText(guiFont);
		this.screen = new ScreenRegion(new Vector2f(settings.getWidth(), settings.getHeight()), new Vector2f(settings.getWidth(), 10.0f ), new Vector2f(10.0f, settings.getHeight()), 0.0f);

		FlyCamAppState state = this.stateManager.getState(FlyCamAppState.class);
		this.stateManager.detach(state);

		initKeys();
		initCamera();
		this.initSceneGraph();
		initAnimation();

		context.setTitle("HG: World War II");
		super.setDisplayStatView(debugMode);
		super.setDisplayFps(debugMode);
	}

	private void initKeys() {
		// Camera Movement input-mappings
		inputManager.addMapping(mvWest.name(), new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping(mvEast.name(), new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping(mvNorth.name(), new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping(mvSouth.name(), new KeyTrigger(KeyInput.KEY_S));

		inputManager.addMapping(msWest.name(), new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping(msEast.name(), new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping(msNorth.name(), new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		inputManager.addMapping(msSouth.name(), new MouseAxisTrigger(MouseInput.AXIS_Y, true));

		// Camera Look input-mappings
		inputManager.addMapping(zoomIn.name(), new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addMapping(zoomOut.name(), new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

		// Other input-mappings
		inputManager.addMapping(select.name(), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping(execute.name(), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

		inputManager.addListener(combinedListener, mvWest.name(), mvEast.name(), mvNorth.name(), mvSouth.name());
		inputManager.addListener(combinedListener, msWest.name(), msEast.name(), msNorth.name(), msSouth.name());

		inputManager.addListener(combinedListener, zoomIn.name(), zoomOut.name(), select.name(), execute.name());
	}
	private void initCamera() {
		cam.setLocation(new Vector3f(0.0f, 20.0f, -20.0f));
		cam.setRotation(Constants.isometricRotation);
	}
	private void initSceneGraph() {
		this.collidables = new Node("Collidables");
		this.selectables = new Node("Selectables");
		this.wireFrames = new Node("Wireframes");

		initWorld();
		initLight();
		initPhysics();
		this.initDebug();

		player.addControl(new LandNavControl());

		selectables.attachChild(player);

		collidables.attachChild(sceneModel);
		collidables.attachChild(selectables);

		rootNode.attachChild(originCube);
		rootNode.attachChild(collidables);
		rootNode.attachChild(wireFrames);
	}
	private void initAnimation() {
		AnimControl control = player.getControl(AnimControl.class);
		AnimChannel channel = control.createChannel();

		control.addListener(this);
		channel.setAnim("Idle1");
	}

	private void initWorld() {
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		// We load the scene from the zip file and adjust its size.
		assetManager.registerLocator("./assets/", FileLocator.class);
		sceneModel = assetManager.loadModel("Scenes/town/main.j3o");
		sceneModel.setLocalTranslation(0.0f, 0.0f, 0.0f);
		sceneModel.setLocalScale(1.0f);

		player = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
		player.scale(0.02f);
		player.rotate(Quaternion.ZERO);
		player.setLocalTranslation(Vector3f.ZERO);
	}
	private void initLight() {
		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		rootNode.addLight(dl);
	}
	private void initPhysics() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		bulletAppState.setDebugEnabled(debugMode);

		// We set up collision detection for the scene by creating a
		// compound collision shape and a static RigidBodyControl with mass zero.
		CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
		RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(landscape);

		float playerMass = 0.0f;
		BetterCharacterControl playerControl = new BetterCharacterControl(3.0f, 6.0f, playerMass);
		playerControl.setPhysicsDamping(0.7f);
		player.addControl(playerControl);

		bulletAppState.getPhysicsSpace().add(landscape);
		bulletAppState.getPhysicsSpace().add(playerControl);
	}
	private void initDebug() {
		initMarks();

		originCube = initCube(1.0f, "OriginCube", null);
		originCube.setLocalTranslation(0.0f, -0.4f, 0.0f);

		this.cursorPos.setText("X: 0.0\nY: 0.0");
		this.cursorPos.setSize(16.0f);
		this.cursorPos.setColor(ColorRGBA.DarkGray);
		this.cursorPos.setBox(new Rectangle(0, this.settings.getHeight()/2.0f, 192.0f, 16.0f));
		guiNode.attachChild(cursorPos);
	}

	private void initMarks() {
		Sphere sphere = new Sphere(30, 30, 0.2f);
		markR = new Geometry("MarkR", sphere);
		markL = new Geometry("MarkL", sphere);

		Material matR = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Material matL = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		matR.setColor("Color", ColorRGBA.Red);
		matL.setColor("Color", ColorRGBA.Blue);

		markR.setMaterial(matR);
		markL.setMaterial(matL);

		markL.setLocalScale(75.0f);
	}

	private Spatial initCube(float length, String name, Material mat) {
		StripBox box = new StripBox(length/2, length/2, length/2);
		Geometry cube = new Geometry(name, box);
		Material cubeMat = (mat != null) ? mat : new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		cubeMat.setColor("Color", ColorRGBA.Orange);
		cube.setMaterial(cubeMat);

		return cube;
	}

	/**
	 * This is the main event loop.
	 */
	@Override
	public void simpleUpdate(float tpf) {
		Vector2f cursPos = this.inputManager.getCursorPosition();
		this.cursorPos.setText(String.format("X: %f\nY: %f", cursPos.getX(), cursPos.getY()));

		EnumSet<ScreenRegion.Region> edges = Directions.isCursorOnEdge(inputManager, screen);
		float value = camSpeed*(tpf*2);

		if (mvWest.isActive() || (msWest.isActive() && edges.contains(ScreenRegion.Region.LEFT))) { moveCamera(value, true, false); }
		if (mvEast.isActive() || (msEast.isActive() && edges.contains(ScreenRegion.Region.RIGHT))) { moveCamera(value, true, true); }
		if (mvNorth.isActive() || (msNorth.isActive() && edges.contains(ScreenRegion.Region.TOP))) { moveCamera(value, false, false); }
		if (mvSouth.isActive() || (msSouth.isActive() && edges.contains(ScreenRegion.Region.BOTTOM))) { moveCamera(value, false, true); }

		for (Spatial child : wireFrames.getChildren()) {
			if (child instanceof Geometry) {
				Geometry highlight = (Geometry) child;
				String linkedTo = highlight.getUserData("link");
				Spatial link = selectables.getChild(linkedTo);
				if (link != null && selected.contains(link)) {
					highlight.setLocalTranslation(link.getLocalTranslation());
				} else {
					wireFrames.detachChildNamed(highlight.getName());
				}
			}
		}
	}

	private void moveCamera(float value, boolean sideways, boolean negative) {
		Vector3f temp = new Vector3f(0.0f, 0.0f, 0.0f);
		Vector3f pos = cam.getLocation();
		float newCamSpeed = FastMath.abs(value);

		if (Float.compare(newCamSpeed, Constants.MIN_CAM_SPEED) == -1) { newCamSpeed = Constants.MIN_CAM_SPEED; }
		else if (Float.compare(newCamSpeed, Constants.MAX_CAM_SPEED) == 1) { newCamSpeed = Constants.MAX_CAM_SPEED; }

		if (sideways) { temp.setX(newCamSpeed); }
		else { temp.setZ(newCamSpeed); }

		if (negative) {	temp.negateLocal(); }

		camSpeed = newCamSpeed;
		cam.setLocation(pos.add(temp));
	}

	private void pickSelected() {
		CollisionResults candidates = new CollisionResults();
		Vector2f click = inputManager.getCursorPosition();
		Vector3f origin = cam.getWorldCoordinates(click, 0.0f);
		Vector3f direction = cam.getWorldCoordinates(click, 0.3f);
		direction.subtractLocal(origin).normalizeLocal();
		Ray ray = new Ray(origin, direction);

		selectables.collideWith(ray, candidates);

		if (candidates.size() > 0) {
			CollisionResult closest = candidates.getClosestCollision();
			Geometry geo = closest.getGeometry();
			Node node = geo.getParent();
			Geometry highlight = getHighlight(node, playerColor);
			selected.add(node);
			wireFrames.attachChild(highlight);
		} else {
			selected.clear();
			wireFrames.detachAllChildren();
		}

		if (debugMode) { //DEBUG CODE
			if (!guiNode.hasChild(markL)) { guiNode.attachChild(markL); }
			markL.setLocalTranslation(click.getX(), click.getY(), 0.0f);
		}
	}
	private void execute() {
		//TODO add execution of orders for selected geometry
		//if (selected.size() == 0) { return; }

		Vector2f click2D = inputManager.getCursorPosition();
		Vector3f click3D = cam.getWorldCoordinates(click2D, 0.0f);
		Vector3f clickDir = cam.getWorldCoordinates(click2D, 0.3f);
		clickDir.subtractLocal(click3D).normalizeLocal();
		Ray ray = new Ray(click3D, clickDir);
		CollisionResults candidates = new CollisionResults();

		sceneModel.collideWith(ray, candidates);
		CollisionResult collision = candidates.getClosestCollision();
		Vector3f contact = collision.getContactPoint();

		for (Spatial sel : selected) {
			Vector3f selectedPos = sel.getLocalTranslation();
			Vector3f newPos = contact.subtract(selectedPos);

			newPos.normalizeLocal();
			newPos.setY(0.0f);

			if (sel.equals(player)) {
				BetterCharacterControl ctrl = player.getControl(BetterCharacterControl.class);
				ctrl.setWalkDirection(newPos);
				ctrl.setViewDirection(contact);
				ctrl.warp(contact);
			} else {
				sel.move(newPos);
			}

			//if (sel.getControl(ManualLandControl.class) != null) {
				// Land Movement
			//} else if (sel.getControl(ManualSeaControl.class) != null) {
				// Sea Movement
			//} else if (sel.getControl(ManualAirControl.class) != null) {
				// Air Movement
			//}
		}

		if (debugMode) { //DEBUG CODE
			if (!rootNode.hasChild(markR)) { rootNode.attachChild(markR); }
			markR.setLocalTranslation(contact);
		}

		candidates.clear();

		Geometry collidedWith;
		selectables.collideWith(ray, candidates);

		if (candidates.size() > 0) {
			collision = candidates.getClosestCollision();
			collidedWith = collision.getGeometry();
			contact = collision.getContactPoint();

			if (selectables.hasChild(collidedWith)) {
				//TODO add interactions
			}
		}

		candidates.clear();

	}

	private Geometry getHighlight(Spatial original, ColorRGBA color){
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.getAdditionalRenderState().setLineWidth(3.0f);
		mat.setColor("Color", color);

		BoundingVolume bound = original.getWorldBound().clone();
		WireBox mesh = new WireBox();
		mesh.setBound(bound);

		Geometry g = new Geometry(original.getName()+"_highlight", mesh);
		g.setMaterial(mat);
		g.setLocalScale(new Vector3f(1.5f, 0.025f, 1.5f));
		g.move(original.getLocalTranslation());
		g.setUserData("link", original.getName());

		return g;
	}

	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		switch (animName) {
			case "Walk":
				channel.setAnim("Idle1", 0.50f);
				channel.setLoopMode(LoopMode.DontLoop);
				channel.setSpeed(2f);
				break;
			case "Idle1":
				channel.setAnim("Idle2", 0.50f);
				channel.setLoopMode(LoopMode.Cycle);
				channel.setSpeed(2f);
				break;
			case "Idle2":
				channel.setAnim("Idle3", 0.50f);
				channel.setLoopMode(LoopMode.Cycle);
				channel.setSpeed(2f);
				break;
			case "Idle3":
				channel.setAnim("Idle1", 0.25f);
				channel.setLoopMode(LoopMode.Cycle);
				channel.setSpeed(2f);
				break;
		}
	}
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// unused
	}

	public static void main(String[] args) {
		WW2Game app = new WW2Game();
		app.start();
	}

	private boolean inDebugMode() { return debugMode; }
	private void setDebugModeEnabled(boolean debugMode) {
		this.debugMode = debugMode;
		bulletAppState.setDebugEnabled(debugMode);
	}

	/**
	 * Created by Jacob Hayes on 5/16/2017.
	 *
	 * @author Jacob Hayes
	 */
	public class CombinedListener implements ActionListener, AnalogListener {
		@Override
		public void onAction(String binding, boolean isPressed, float tpf) {
			if (binding.equals(mvWest.name())) {
				mvWest.setActive(isPressed);
			} else if (binding.equals(mvEast.name())) {
				mvEast.setActive(isPressed);
			} else if (binding.equals(mvNorth.name())) {
				mvNorth.setActive(isPressed);
			} else if (binding.equals(mvSouth.name())) {
				mvSouth.setActive(isPressed);
			} else if (binding.equals(select.name()) && !isPressed) {
				pickSelected();
			} else if (binding.equals(execute.name()) && !isPressed) {
				execute();
			}
		}

		@Override
		public void onAnalog(String binding, float value, float tpf) {
			EnumSet<ScreenRegion.Region> edges = Directions.isCursorOnEdge(inputManager, screen);

			msWest.setActive(edges.contains(ScreenRegion.Region.LEFT));
			msEast.setActive(edges.contains(ScreenRegion.Region.RIGHT));
			msNorth.setActive(edges.contains(ScreenRegion.Region.TOP));
			msSouth.setActive(edges.contains(ScreenRegion.Region.BOTTOM));
		}
	}
}

