package com.runyuanj.model;

public class CInfo {

    private String name;

    public CInfo() {
        this.name = "C";
    }

    public CInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }
}
