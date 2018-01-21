package com.e16din.sc.processor;

public class ControllerContainer {

    private String name;
    private boolean startOnce;

    public ControllerContainer(String name, boolean startOnce) {
        this.name = name;
        this.startOnce = startOnce;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStartOnce() {
        return startOnce;
    }

    public void setStartOnce(boolean startOnce) {
        this.startOnce = startOnce;
    }
}
