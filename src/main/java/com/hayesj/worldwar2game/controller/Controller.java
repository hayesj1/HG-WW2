package com.hayesj.worldwar2game.controller;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;

/**
 * Created by Jacob Hayes on 9/14/2016.
 *
 * @author Jacob Hayes
 */

public class Controller extends SimpleApplication implements AnimEventListener {

	private Spatial sceneModel;
	private BulletAppState bulletAppState;
	private RigidBodyControl landscape;
	private Spatial player;
	private BetterCharacterControl playerControl;
	private CameraControl cameraControl;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, forward = false, back = false;
	private float look_left = 0.0f, look_right = 0.0f, look_up = 0.0f, look_down = 0.0f;

	//Temporary vectors used on each frame.
	//They here to avoid instanciating new vectors on each frame
	private Vector3f camDir = new Vector3f();
	private Vector3f newCamDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();
	private Vector3f camUp = new Vector3f();
	private Vector3f camLookDir = new Vector3f(camDir);
	private AnimChannel channel;
	private AnimControl control;

	public static void main(String[] args) {
		Controller app = new Controller();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		/**
		 * Set up Physics
		 */
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		//bulletAppState.getPhysicsSpace().enableDebug(assetManager);

		// We re-use the flyby camera for rotation, while positioning is handled by physics
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
		flyCam.setMoveSpeed(100);
		setUpKeys();
		setUpLight();

		// We load the scene from the zip file and adjust its size.
		assetManager.registerLocator("./assets/", FileLocator.class);
		sceneModel = assetManager.loadModel("Scenes/town/main.j3o");
		sceneModel.setLocalScale(2f);

		// We set up collision detection for the scene by creating a
		// compound collision shape and a static RigidBodyControl with mass zero.
		CollisionShape sceneShape =
				CollisionShapeFactory.createMeshShape(sceneModel);
		landscape = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(landscape);

		int playerMass = 1;
		player = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
		player.scale(0.5f);
		//player.rotate(0.0f, -3.0f, 0.0f);
		player.setLocalTranslation(0.0f, 0.0f, -2.0f);

		cameraControl = new CameraControl(cam);
		cameraControl.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
		cameraControl.getCamera().setLocation(player.getLocalTranslation().add(0.0f, 5.0f, -10.0f));
		cameraControl.getCamera().lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);

		playerControl = new BetterCharacterControl(3.0f, 6f, 1);
		playerControl.setPhysicsDamping(0.5f);
		//playerControl.setGravity(new Vector3f(Vector3f.UNIT_Y).mult(-1f*playerMass));

		player.addControl(playerControl);
		player.addControl(cameraControl);
		rootNode.attachChild(player);

		// We attach the scene and the player to the rootnode and the physics space,
		// to make them appear in the game world.
		rootNode.attachChild(sceneModel);
		bulletAppState.getPhysicsSpace().add(sceneModel);
		bulletAppState.getPhysicsSpace().add(playerControl);

		control = player.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();

		channel.setAnim("Idle1");
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

	/**
	 * We over-write some navigational key mappings here, so we can add
	 * physics-controlled walking and jumping:
	 */
	private void setUpKeys() {
		// Movement input-mappings
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));

		inputManager.addListener(analogListener, "Left");
		inputManager.addListener(analogListener, "Right");
		inputManager.addListener(analogListener, "Forward");
		inputManager.addListener(analogListener, "Back");
		inputManager.addListener(actionListener, "Jump");

		// Camera Look input-mappings
		inputManager.addMapping("Look_Left", new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping("Look_Right", new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping("Look_Up", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		inputManager.addMapping("Look_Down", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
		inputManager.addMapping("Zoom", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));

		inputManager.addListener(analogListener, "Look_Left");
		inputManager.addListener(analogListener, "Look_Right");
		inputManager.addListener(analogListener, "Look_Up");
		inputManager.addListener(analogListener, "Look_Down");
		inputManager.addListener(analogListener, "Zoom");
	}

	/**
	 * These are custom actions triggered by key presses. We do not walk
	 * yet, we just keep track of the direction the user pressed.
	 */
	private ActionListener actionListener = (binding, isPressed, tpf) -> {
		if (binding.equals("Jump")) {
			if (isPressed) {
				playerControl.jump();
				if (!channel.getAnimationName().equals("Jump")) {
					channel.setAnim("Jump", 0.50f);
					channel.setLoopMode(LoopMode.DontLoop);
				}
			}
		}
		if (isLateralAction(binding) && isPressed) {
			if (!channel.getAnimationName().equals("Walk")) {
				channel.setAnim("Walk", 0.50f);
				channel.setLoopMode(LoopMode.Loop);
			}
		}
	};

	private AnalogListener analogListener = (binding, value, tpf) -> {
		if (binding.equals("Left")) {
			left = isPressed;
		} else if (binding.equals("Right")) {
			right = isPressed;
		} else if (binding.equals("Forward")) {
			forward = isPressed;
		} else if (binding.equals("Back")) {
			back = isPressed;
		} else if (binding.equals("Look_Left")) {
			look_left += value*tpf;
		} else if (binding.equals("Look_Right")) {
			look_right += value*tpf;
		} else if (binding.equals("Look_Up")) {
			look_up += value*tpf;
		} else if (binding.equals("Look_Down")) {
			look_down += value*tpf;
		}
	};

	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Walk")) {
			channel.setAnim("Idle2", 0.50f);
			channel.setLoopMode(LoopMode.Cycle);
			channel.setSpeed(1f);
		} else if (animName.equals("Jump")) {
			channel.setAnim("Idle1", 0.50f);
			channel.setLoopMode(LoopMode.Cycle);
			channel.setSpeed(1f);
		}
	}

	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// unused
	}


	/**
	 * This is the main event loop. We check in which
	 * direction the player is walking by interpreting the camera direction
	 * forward (camDir) and to the side (camLeft). We also make sure
	 * here that the camera moves with player.
	 */
	@Override
	public void simpleUpdate(float tpf) {
		camDir.set(cameraControl.getCamera().getDirection());
		newCamDir.set(camDir);
		camLeft.set(cameraControl.getCamera().getLeft());
		camUp.set(cameraControl.getCamera().getUp());
		walkDirection.set(0, 0, 0);
		if (left) {
			walkDirection.addLocal(camLeft);
		}
		if (right) {
			walkDirection.addLocal(camLeft.negate());
		}
		if (forward) {
			walkDirection.addLocal(camDir);
		}
		if (back) {
			walkDirection.addLocal(camDir.negate());
		}

		playerControl.setWalkDirection(walkDirection.multLocal(2.0f));

		camLookDir.addLocal(camLeft.mult(look_left));
		camLookDir.addLocal(camLeft.mult(look_right));
		camLookDir.addLocal(camUp.mult(look_up));
		camLookDir.addLocal(camUp.mult(look_down));

		cameraControl.getCamera().lookAt(camLookDir,camUp);
		//cam.setLocation(player.getLocalTranslation().add(0.0f, 15.0f, 0.0f));
	}

	private boolean isLateralAction(String binding) {
		return (binding.equals("Forward") || binding.equals("Back") || binding.equals("Left") || binding.equals("Right"));
	}


}

