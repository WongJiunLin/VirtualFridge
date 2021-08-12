package com.example.testingfyp;

import java.util.Date;

public class Item {
    String itemName, itemStoredDate, itemExpirationDate;
    String itemImgUri;

    public Item() {
    }

    public Item(String itemName, String itemStoredDate, String itemExpirationDate, String itemImgUri) {
        this.itemName = itemName;
        this.itemStoredDate = itemStoredDate;
        this.itemExpirationDate = itemExpirationDate;
        this.itemImgUri = itemImgUri;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemStoredDate() {
        return itemStoredDate;
    }

    public void setItemStoredDate(String itemStoredDate) {
        this.itemStoredDate = itemStoredDate;
    }

    public String getItemExpirationDate() {
        return itemExpirationDate;
    }

    public void setItemExpirationDate(String itemExpirationDate) {
        this.itemExpirationDate = itemExpirationDate;
    }

    public String getItemImgUri() {
        return itemImgUri;
    }

    public void setItemImgUri(String itemImgUri) {
        this.itemImgUri = itemImgUri;
    }
}
