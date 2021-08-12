package com.example.testingfyp;

import java.util.List;

public class Drawer {
    String drawerID, drawerType;
    List<Storage> storages;

    public Drawer() {
    }

    public Drawer(String drawerID, String drawerType, List<Storage> storages) {
        this.drawerID = drawerID;
        this.drawerType = drawerType;
        this.storages = storages;
    }

    public String getDrawerID() {
        return drawerID;
    }

    public void setDrawerID(String drawerID) {
        this.drawerID = drawerID;
    }

    public String getDrawerType() {
        return drawerType;
    }

    public void setDrawerType(String drawerType) {
        this.drawerType = drawerType;
    }

    public List<Storage> getStorages() {
        return storages;
    }

    public void setStorages(List<Storage> storages) {
        this.storages = storages;
    }
}
