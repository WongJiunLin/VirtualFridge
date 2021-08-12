package com.example.testingfyp;

import java.util.List;

public class Freezer {
    List<Storage> storages;
    String freezerId, freezerType;

    public Freezer() {
    }

    public Freezer(List<Storage> storages, String freezerId, String freezerType) {
        this.storages = storages;
        this.freezerId = freezerId;
        this.freezerType = freezerType;
    }

    public List<Storage> getStorages() {
        return storages;
    }

    public void setStorages(List<Storage> storages) {
        this.storages = storages;
    }

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public String getFreezerType() {
        return freezerType;
    }

    public void setFreezerType(String freezerType) {
        this.freezerType = freezerType;
    }
}
