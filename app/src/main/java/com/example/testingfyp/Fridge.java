package com.example.testingfyp;

import java.util.Date;
import java.util.List;

public class Fridge {
    String fridgeName;
    String fridgeCreatedDate;

    public Fridge() {
    }

    public Fridge(String fridgeName, String fridgeCreatedDate) {
        this.fridgeName = fridgeName;
        this.fridgeCreatedDate = fridgeCreatedDate;
    }

    public String getFridgeName() {
        return fridgeName;
    }

    public void setFridgeName(String fridgeName) {
        this.fridgeName = fridgeName;
    }

    public String getFridgeCreatedDate() {
        return fridgeCreatedDate;
    }

    public void setFridgeCreatedDate(String fridgeCreatedDate) {
        this.fridgeCreatedDate = fridgeCreatedDate;
    }
}
