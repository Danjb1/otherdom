package com.danjb.otherdom.client;

/**
 * Class responsible for keeping track of input.
 * 
 * @author Dan Bryce
 */
public class Input {

    private static final int NUM_KEYS = 3;

    public static final int KEY_MOUSE_LEFT = 0;
    public static final int KEY_LEFT       = 1;
    public static final int KEY_RIGHT      = 2;
    
    private boolean[] keysDown = new boolean[NUM_KEYS];
    private boolean[] keysDownLastFrame = new boolean[NUM_KEYS];

    private int mouseX, mouseY;
    
    public void prepareForNextFrame(){
        
        // The current input is now last frame's input
        System.arraycopy(keysDown, 0, keysDownLastFrame, 0, NUM_KEYS);
    }
    
    public void keyPressed(int key){
        keysDown[key] = true;
    }
    
    public void keyReleased(int key){
        keysDown[key] = false;
    }
    
    public boolean isKeyDown(int key){
        return keysDown[key];
    }
    
    public boolean wasKeyPressed(int key){
        return keysDown[key] && !keysDownLastFrame[key];
    }

    public boolean wasKeyReleased(int key){
        return !keysDown[key] && keysDownLastFrame[key];
    }
    
    public int getMouseX() {
        return mouseX;
    }
    
    public int getMouseY() {
        return mouseY;
    }
    
    public void setMousePos(int mouseX, int mouseY){
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
    
}
