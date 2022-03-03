package com.example.testingfyp;

import java.util.Date;

public class Item {
    String itemName, itemStoredDate, itemExpirationDate, placedBy;
    String itemImgUri;
    int days;

    public Item() {
    }

    public Item(String itemName, String itemStoredDate, String itemExpirationDate, String placedBy, String itemImgUri, int days) {
        this.itemName = itemName;
        this.itemStoredDate = itemStoredDate;
        this.itemExpirationDate = itemExpirationDate;
        this.placedBy = placedBy;
        this.itemImgUri = itemImgUri;
        this.days = days;
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

    public String getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(String placedBy) {
        this.placedBy = placedBy;
    }

    public String getItemImgUri() {
        return itemImgUri;
    }

    public void setItemImgUri(String itemImgUri) {
        this.itemImgUri = itemImgUri;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
