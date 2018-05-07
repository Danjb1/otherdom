package com.danjb.otherdom.client;

import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import com.danjb.otherdom.client.render.Shaders;
import com.danjb.otherdom.client.util.GLUtils;

public class Client {

    public static final String RESOURCE_DIR = "src/main/resources/";
    
    private static final int WINDOW_WIDTH  = 1280;
    private static final int WINDOW_HEIGHT = 768;
    private static final String WINDOW_TITLE = "RPG";

    public static boolean debugMode;

    private GLWindow window;
    private Input input;
    private State state;
    
    /**
     * Entry point for the application.
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        Client client = new Client();

        Client.debugMode = true;
        
        try {
            client.init();
            client.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            client.tearDown();
        }
    }

    private void init() throws IOException {

        // Setup an error callback
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        // Initialise GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialise GLFW");
        }

        window = new GLWindow(this, WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE);
        input = window.getInput();

        GLUtils.initGL(WINDOW_WIDTH, WINDOW_HEIGHT);
        Shaders.setupShaders();
        
        state = new GameState(this);
    }

    private void tearDown() {
        window.destroy();

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public void start() {
        
        // Our game loop!
        while (!window.isCloseRequested()) {
            window.pollInput();
            state.processInput(input);
            state.update();
            state.render();
            window.refresh();
        }
    }

    public State getState() {
        return state;
    }
    
    public GLWindow getWindow() {
        return window;
    }
    
}
