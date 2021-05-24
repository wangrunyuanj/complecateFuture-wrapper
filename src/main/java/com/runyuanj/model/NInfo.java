package com.runyuanj.model;

public class NInfo {

    private String name;

    public NInfo() {
        this.name = "N";
    }

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }
}
