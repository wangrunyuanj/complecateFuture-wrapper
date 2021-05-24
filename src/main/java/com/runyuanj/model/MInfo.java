package com.runyuanj.model;

public class MInfo {

    private String name;

    public MInfo() {
        this.name = "M";
    }

    public MInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }
}
