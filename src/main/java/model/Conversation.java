package model;

import java.util.Timer;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class Conversation {
    private String facebook_id;
    private String name;
    private boolean auto;
    String currentState;

    public Conversation(String facebook_id, String name, boolean auto, String currentState) {
        this.facebook_id = facebook_id;
        this.name = name;
        this.auto = auto;
        this.currentState = currentState;
    }

    public String getFacebook_id() {
        return facebook_id;
    }

    public void setFacebook_id(String facebook_id) {
        this.facebook_id = facebook_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
}
