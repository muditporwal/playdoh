package com.lsoftware.playdoh.objects;

public class ObjectWithEnum {

    private AnEnum anEnum;

    public ObjectWithEnum() {
    }

    public void setAnEnum(AnEnum anEnum) {
        this.anEnum = anEnum;
    }

    public enum AnEnum {
        X, Y
    }
}