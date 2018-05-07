package com.danjb.otherdom.client;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

/**
 * Class representing an OpenGL-capable window.
 * 
 * @author Dan Bryce
 */
public class GLWindow {

    /**
     * Window handle.
     */
    private long window;
    
    /**
     * Window size.
     */
    private int width, height;
    
    /**
     * Window aspect ratio.
     */
    private float aspectRatio;
    
    /**
     * Class to which input events are passed.
     */
    private Input input = new Input();
    
    /**
     * Map of (GLFW -> Input) key codes for the keys we are interested in.
     */
    private Map<Integer, Integer> listeningKeys = new HashMap<>();
    
    public GLWindow(Client client, int width, int height, String title) {
        
        this.width = width;
        this.height = height;

        aspectRatio = (float) width / height;
        
        listeningKeys.put(GLFW.GLFW_MOUSE_BUTTON_LEFT, Input.KEY_MOUSE_LEFT);
        listeningKeys.put(GLFW.GLFW_KEY_LEFT, Input.KEY_LEFT);
        listeningKeys.put(GLFW.GLFW_KEY_RIGHT, Input.KEY_RIGHT);
        
        // Configure our window
        GLFW.glfwDefaultWindowHints();
        
        // Request forward-compatible 3.2 OpenGL context with only core functionality
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        if (Client.debugMode){
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }

        // Create the window
        window = GLFW.glfwCreateWindow(
                width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Listen for key events
        GLFW.glfwSetKeyCallback(window,
                GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
                    gotKeyEvent(key, action);
                }
        ));

        // Listen for the mouse position
        GLFW.glfwSetCursorPosCallback(window,
                GLFWCursorPosCallback.create((window, xPos, yPos) -> {
                    int mouseX = (int) Math.floor(xPos);
                    int mouseY = (int) Math.floor(yPos);
                    input.setMousePos(mouseX, mouseY);
                }
        ));

        // Listen for mouse events
        GLFW.glfwSetMouseButtonCallback(window,
                GLFWMouseButtonCallback.create((window, button, action, mods) -> {
                    gotKeyEvent(button, action);
                }
        ));

        // Centre our window
        GLFWVidMode vidmode =
                    GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);
        
        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        // Make the window visible
        GLFW.glfwShowWindow(window);
    }

    /**
     * Updates the Input according to the given keypress / key release.
     * 
     * @param key
     * @param action
     */
    private void gotKeyEvent(int key, int action) {
        
        // ESC = close window
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE){
            GLFW.glfwSetWindowShouldClose(window, true);
            return;
        }
        
        if (!listeningKeys.containsKey(key)){
            return;
        }
        
        int inputKeyCode = listeningKeys.get(key);
        
        if (action == GLFW.GLFW_PRESS){
            input.keyPressed(inputKeyCode);
        } else if (action == GLFW.GLFW_RELEASE){
            input.keyReleased(inputKeyCode);
        }
    }
    
    public void destroy() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
    }

    public boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void pollInput() {
        input.prepareForNextFrame();
        
        // Poll for window events; invokes callbacks
        GLFW.glfwPollEvents();
    }

    public void refresh() {
        // Must be called every frame after rendering
        GLFW.glfwSwapBuffers(window);
    }

    public Input getInput() {
        return input;
    }
    
}
