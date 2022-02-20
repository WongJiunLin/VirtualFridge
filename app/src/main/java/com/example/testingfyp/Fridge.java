package com.example.testingfyp;

import java.util.Date;
import java.util.List;

public class Fridge {
    String fridgeName;
    String fridgeCreatedDate;
    String createdBy;

    public Fridge() {
    }

    public Fridge(String fridgeName, String fridgeCreatedDate, String createdBy) {
        this.fridgeName = fridgeName;
        this.fridgeCreatedDate = fridgeCreatedDate;
        this.createdBy = createdBy;
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

    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}

