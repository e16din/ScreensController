package com.e16din.sc.processor;

import java.util.ArrayList;

public class ScreenContainer {

    private String name;
    private ArrayList<ControllerContainer> controllers = new ArrayList<>();

    public ScreenContainer(String name, ControllerContainer controller) {
        this.name = name;
        this.controllers.add(controller);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ControllerContainer> getControllers() {
        return controllers;
    }

    public void setControllers(ArrayList<ControllerContainer> controllers) {
        this.controllers = controllers;
    }
}
