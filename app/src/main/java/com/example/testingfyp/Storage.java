package com.example.testingfyp;

import java.util.List;

public class Storage {
    List<Item> items;
    String storageId, storagePosition;

    public Storage() {
    }

    public Storage(List<Item> items, String storageId, String storagePosition) {
        this.items = items;
        this.storageId = storageId;
        this.storagePosition = storagePosition;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getStoragePosition() {
        return storagePosition;
    }

    public void setStoragePosition(String storagePosition) {
        this.storagePosition = storagePosition;
    }
}
