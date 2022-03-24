package com.example.testingfyp;

import java.util.List;

public class Freezer {
    String freezerName, freezerType;

    public Freezer() {
    }

    public Freezer(List<Storage> storages, String freezerId, String freezerType) {
        this.freezerName = freezerId;
        this.freezerType = freezerType;
    }

    public String getFreezerId() {
        return freezerName;
    }

    public void setFreezerId(String freezerName) {
        this.freezerName = freezerName;
    }

    public String getFreezerType() {
        return freezerType;
    }

    public void setFreezerType(String freezerType) {
        this.freezerType = freezerType;
    }
}
