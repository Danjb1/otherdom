package com.danjb.otherdom.client;

public abstract class State {

    protected Client client;
    
    public State(Client client) {
        this.client = client;
    }
    
    public abstract void processInput(Input input);
    
    public abstract void update();

    public abstract void render();

}
