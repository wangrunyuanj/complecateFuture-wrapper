package com.runyuanj.model;

public class FInfo {

    private String name;

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }

    public FInfo() {
        this.name = "F";
    }
}
