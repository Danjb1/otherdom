package com.danjb.otherdom.client.scene;

import java.awt.Toolkit;

import org.joml.Vector3f;

import com.danjb.otherdom.client.GLWindow;
import com.danjb.otherdom.client.scene.World.CompassDirection;

/**
 * Class representing the camera through which the world is viewed.
 * 
 * A camera typically has the following notions:
 *  - Pitch: Top-to-bottom orientation of the camera (x-axis).
 *  - Yaw: Left-to-right orientation of the camera (y-axis).
 *  - Roll: Tilt of the camera (z-axis).
 * 
 * Fortunately, we don't need to concern ourselves with these angles; instead
 * we can rely on the lookAt() function provided by JOML, which requires only
 * the camera position, target position and up vector.
 * 
 * @author Dan Bryce
 */
public class Camera {

    /**
     * Estimated distance between the viewer's eyes and the screen.
     * The unit used here in cm, but it doesn't actually matter, as long as it's
     * consistent with screenHeight.
     */
    private static final float DISTANCE_TO_SCREEN = 60.0f;
    
    /**
     * Estimated height of the user's screen.
     * The unit used here in cm, but it doesn't actually matter, as long as it's
     * consistent with distanceToScreen.
     */
    private static final float SCREEN_HEIGHT = 30.0f;

    /**
     * Near clipping distance, in metres.
     * Anything closer to the camera than this will not be drawn.
     */
    public static final float Z_NEAR = 0.01f;

    /**
     * Far clipping distance, in metres.
     * Anything further from the camera than this will not be drawn.
     */
    public static final float Z_FAR = 1000.0f;

    /**
     * Height of the camera, in metres.
     */
    private static final float CAMERA_HEIGHT =
            10.0f * TerrainSection.TILE_WIDTH;

    /**
     * The camera's default distance from the player, in metres.
     * 
     * This was chosen (through trial and error) such that the player is drawn
     * at a (more or less) pixel-perfect ratio.
     */
    private static final float DEFAULT_ORBIT_RADIUS = 33.75f;

    /**
     * Rotation per frame while a key is held down, in degrees.
     * 
     * 1 degree per frame = 60 degrees per second.
     */
    public static final float ROT_SPEED = 2.0f;

    /**
     * Position of the camera, in metres.
     */
    private Vector3f position = new Vector3f();

    /**
     * Position of the target point, in metres.
     */
    private Vector3f target = new Vector3f();

    /**
     * "Up" vector that defines which way is up.
     */
    private Vector3f up = new Vector3f(World.Y_AXIS);

    /**
     * Vertical field-of-view, in radians.
     */
    private float fovY;
    
    /**
     * Radius of the camera's orbit around the target, in metres.
     */
    private float orbitRadius = DEFAULT_ORBIT_RADIUS;
    
    /**
     * The current facing of the camera, in degrees.
     * 
     *   0 = north
     *  90 = east
     * 180 = south
     * 270 = west
     */
    private float angle = 0;

    /**
     * Current facing, rounded to the nearest compass direction.
     */
    private CompassDirection compassAngle = CompassDirection.NORTH;
    
    public Camera(GLWindow window) {
        
        /*
         * Calculate the optimal FOV based on the screen resolution. The idea is
         * that the window on the screen should be like a literal window into 
         * the game, so the viewer's eyeline follows the lines of the camera's 
         * frustum. See the diagram here:
         * http://stackoverflow.com/questions/16571981/gluperspective-parameters-what-do-they-mean
         */
        int screenHeightPixels = 
                Toolkit.getDefaultToolkit().getScreenSize().height;
        float realWorldHeightPerPixel = SCREEN_HEIGHT / screenHeightPixels;
        float realWorldWindowHeight = 
                window.getHeight() * realWorldHeightPerPixel;
        // Use trigonometry to calculate the FOV based on the window height
        fovY = (float) Math.atan(realWorldWindowHeight / DISTANCE_TO_SCREEN);

        // For now, our target is the centre of the terrain
        target.x = TerrainSection.WIDTH / 2;
        target.y = 0;
        target.z = TerrainSection.WIDTH / 2;

        // Position the Camera the desired distance away
        position.y = CAMERA_HEIGHT;
        recalculateOrbitPosition();
    }

    public void modAngle(float val){
        angle = (angle + val) % 360;
        if (angle < 0){
            angle = 360 + angle;
        }
        recalculateCompassAngle();
        recalculateOrbitPosition();
    }
    
    private void recalculateCompassAngle() {
        /*
         * NORTH covers the segment between -22.5 (337.5) and 22.5 degrees.
         * Therefore we start by considering the segment between 0 and 22.5
         * degrees, and later we consider the segment between 337.5 and 360
         * degrees.
         */
        int angleCovered = World.COMPASS_ANGLE_INCREMENT / 2;
        for (int i = 0; i < World.NUM_COMPASS_DIRECTIONS; i++){
            if (angle < angleCovered){
                compassAngle = CompassDirection.values()[i];
                return;
            }
            angleCovered += World.COMPASS_ANGLE_INCREMENT;
        }
        // If we still haven't found our segment, the angle must be between
        // 337.5 and 360 degrees.
        compassAngle = CompassDirection.NORTH;
    }

    /**
     * Recalculate the camera's position based on its angle.
     * This uses the parametric equation of a circle to keep the camera the same
     * distance from the target.
     */
    private void recalculateOrbitPosition() {
        float angleRad = (float) Math.toRadians(angle);
        position.x = (float) (target.x - orbitRadius * Math.cos(angleRad));
        position.z = (float) (target.z - orbitRadius * Math.sin(angleRad));
    }

    public Vector3f getPos(){
        return position;
    }
    
    public Vector3f getTarget() {
        return target;
    }

    public Vector3f getUpVector() {
        return up;
    }

    public float getFovY() {
        return fovY;
    }
    
    public float getAngle() {
        return angle;
    }

    public CompassDirection getCompassAngle() {
        return compassAngle;
    }
    
}
