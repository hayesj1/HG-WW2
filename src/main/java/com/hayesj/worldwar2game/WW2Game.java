package com.hayesj.worldwar2game;

import com.hayesj.worldwar2game.input.UserAction;
import com.hayesj.worldwar2game.util.Constants;
import com.hayesj.worldwar2game.util.Directions;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
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
import com.jme3.scene.shape.StripBox;

/**
 * Created by Jacob Hayes on 9/14/2016.
 *
 * @author Jacob Hayes
 */

public class WW2Game extends SimpleApplication implements AnimEventListener {
	private float camSpeed = Constants.BASE_CAM_SPEED;

	private Node collidables = new Node("Collidables");
	private Node selectables = new Node("Selectables");
	private Node wireFrames = new Node("WireFrames");

	private BulletAppState bulletAppState;
	private Spatial sceneModel;
	private Spatial player;
	private BetterCharacterControl playerControl;
	private Spatial originCube;

	private Spatial selected;

	private UserAction mvWest = UserAction.MOVE_WEST, mvEast = UserAction.MOVE_EAST;
	private UserAction mvNorth = UserAction.MOVE_NORTH, mvSouth = UserAction.MOVE_SOUTH;
	private UserAction zoomIn = UserAction.ZOOM_IN, zoomOut = UserAction.ZOOM_OUT;
	private UserAction select = UserAction.SELECT, execute = UserAction.EXECUTE;

	private boolean debugMode = false;
	private CombinedListener combinedListener = new CombinedListener();

	@Override
	public void simpleInitApp() {
		context.setTitle("HG: World War II");
		flyCam.setEnabled(false);

		setUpKeys();
		setUpCamera();
		setUpSceneGraph();
		setUpAnimation();
	}

	/**
	 * This is the main event loop.
	 */
	@Override
	public void simpleUpdate(float tpf) {
		Directions.DisplayEdge edge = Directions.CursorOnEdge(inputManager, settings.getWidth(), settings.getHeight());

		if (edge != null) {
			switch(edge) {
				case LEFT:
					moveCamera(Constants.BASE_CAM_SPEED * tpf, true);
					break;
				case RIGHT:
					moveCamera(-(Constants.BASE_CAM_SPEED * tpf), true);
					break;
				case TOP:
					moveCamera(Constants.BASE_CAM_SPEED * tpf, false);
					break;
				case BOTTOM:
					moveCamera(-(Constants.BASE_CAM_SPEED * tpf), false);
					break;
				default:
					break;
			}
		}
	}

	private void setUpKeys() {
		// Camera Movement input-mappings
		inputManager.addMapping(mvWest.name(), new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping(mvEast.name(), new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping(mvNorth.name(), new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping(mvSouth.name(), new KeyTrigger(KeyInput.KEY_S));

		// Camera Look input-mappings
		inputManager.addMapping(zoomIn.name(), new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addMapping(zoomOut.name(), new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

		// Other input-mappings
		inputManager.addMapping(select.name(), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping(execute.name(), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));


		inputManager.addListener(combinedListener, mvWest.name(), mvEast.name(), mvNorth.name(), mvSouth.name());
		inputManager.addListener(combinedListener, zoomIn.name(), zoomOut.name(), select.name(), execute.name());
	}
	private void setUpCamera() {
		cam.setLocation(new Vector3f(0.0f, 20.0f, -20.0f));
		cam.setAxes(Vector3f.UNIT_X, Vector3f.UNIT_Y, Vector3f.UNIT_Z);
		cam.setRotation(Constants.isometricRotation.clone());

		//TODO implement boundaries on Camera movement
		/*flyCam.setMotionAllowedListener((position, velocity) -> {
			if (velocity.length() > CAM_SPEED * 10.0f) {
				velocity.multLocal(10.0f / velocity.length());
			}
			Vector3f future = position.add(velocity);
			if (future.getX() > 100.0f){
				position.setX(100.0f);
			} else if (future.getX() < -100.0f) {
				position.setX(-100.0f);
			}
			if (future.getZ() > 100.0f){
				position.setZ(100.0f);
			} else if (future.getZ() < -100.0f) {
				position.setZ(-100.0f);
			}
		});*/
	}
	private void setUpAnimation() {
		AnimControl control = player.getControl(AnimControl.class);
		AnimChannel channel = control.createChannel();

		control.addListener(this);
		channel.setAnim("Idle1");
	}
	private void setUpSceneGraph() {
		setUpWorld();
		setUpDebug();
		setUpLight();
		setUpPhysics();

		collidables.attachChild(selectables);
		collidables.attachChild(wireFrames);

		rootNode.attachChild(collidables);

		collidables.attachChild(sceneModel);

		selectables.attachChild(player);

		rootNode.attachChild(originCube);
	}
	private void setUpWorld() {
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		// We load the scene from the zip file and adjust its size.
		assetManager.registerLocator("./assets/", FileLocator.class);
		sceneModel = assetManager.loadModel("Scenes/town/main.j3o");
		sceneModel.setLocalTranslation(0.0f, -0.1f, 0.0f);
		sceneModel.setLocalScale(0.5f);

		player = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
		player.scale(0.009f);
		player.rotate(Quaternion.ZERO);
		player.setLocalTranslation(Vector3f.ZERO);
	}
	private void setUpDebug() {
		StripBox box = new StripBox(0.5f, 0.5f, 0.5f);
		originCube = new Geometry("testCube", box);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

		mat.setColor("Color", ColorRGBA.Orange);
		originCube.setMaterial(mat);
		originCube.setLocalTranslation(0.0f, -0.4f, 0.0f);
	}
	private void setUpLight() {
		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		rootNode.addLight(dl);
	}
	private void setUpPhysics() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);

		bulletAppState.setDebugEnabled(debugMode);

		// We set up collision detection for the scene by creating a
		// compound collision shape and a static RigidBodyControl with mass zero.
		CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
		RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(landscape);

		float playerMass = 0.0f;
		playerControl = new BetterCharacterControl(3.0f, 6.0f, playerMass);
		playerControl.setPhysicsDamping(0.7f);

		player.addControl(playerControl);

		bulletAppState.getPhysicsSpace().add(sceneModel);
		bulletAppState.getPhysicsSpace().add(playerControl);
	}

	private void moveCamera(float value, boolean sideways) {
		Vector3f vel = new Vector3f();
		Vector3f pos = cam.getLocation().clone();

		camSpeed *= value;
		if (FastMath.abs(camSpeed) < Constants.BASE_CAM_SPEED) { camSpeed = FastMath.sign(value)*Constants.BASE_CAM_SPEED; }
		else if (FastMath.abs(camSpeed) > Constants.MAX_CAM_SPEED) { camSpeed = FastMath.sign(value)*Constants.MAX_CAM_SPEED; }

		if (sideways) { cam.getLeft(vel); }
		else {
			cam.getLeft().cross(cam.getUp(), vel);
			Vector3f proj = vel.project(Vector3f.UNIT_Z);
			proj.mult((vel.length() / proj.length()), vel);
		}

		vel.multLocal(camSpeed);
		pos.addLocal(vel);
		cam.setLocation(pos);
	}
	private void pickSelected() {
		CollisionResults candidates = new CollisionResults();
		Vector2f click2D = inputManager.getCursorPosition().clone();
		Vector3f click3D = cam.getWorldCoordinates(click2D, 0.0f);
		Vector3f clickDir = cam.getWorldCoordinates(click2D, 1.0f).subtractLocal(click3D).normalizeLocal();

		Ray ray = new Ray(click3D, clickDir);

		selectables.collideWith(ray, candidates);

		if (candidates.size() > 0) {
			CollisionResult selected = candidates.getClosestCollision();
			attachSelectionHighlight(selected.getGeometry(), ColorRGBA.Gray);

			this.selected = selected.getGeometry();
		}
	}

	private void attachSelectionHighlight(Geometry original, ColorRGBA color){
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);

		WireBox mesh = new WireBox();
		mesh.fromBoundingBox( (BoundingBox) original.getModelBound() );

		Geometry g = new Geometry(original.getName()+"_wireframe", mesh);
		//g.setName(original.getName()+"_wireframe");
		g.setMaterial(mat);
		g.scale(0.35f);//.scale(1.05f);
		g.rotate(original.getWorldRotation());
		wireFrames.attachChild(g);
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

	public boolean inDebugMode() { return debugMode; }
	public void setDebugModeEnabled(boolean debugMode) {
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
				//TODO add execution of orders for selected geometry
				return;
			}
		}

		@Override
		public void onAnalog(String binding, float value, float tpf) {
			if (binding.equals(mvWest.name())) {
				moveCamera(value, true);
			} else if (binding.equals(mvEast.name())) {
				moveCamera(-value, true);
			} else if (binding.equals(mvNorth.name())) {
				moveCamera(value, false);
			} else if (binding.equals(mvSouth.name())) {
				moveCamera(-value, false);
			}
		}
	}
}

