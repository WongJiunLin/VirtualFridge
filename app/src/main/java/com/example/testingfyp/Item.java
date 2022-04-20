package com.example.testingfyp;

import java.util.Date;

public class Item {
    String itemName, itemStoredDate, itemExpirationDate, itemPosition, placedBy, placedAt;
    String itemImgUri;
    int days, daysPlaced, itemQuantity;

    public Item() {
    }

    public Item(String itemName, String itemStoredDate, String itemExpirationDate, String itemPosition, String placedBy, String placedAt, String itemImgUri, int days, int daysPlaced, int itemQuantity) {
        this.itemName = itemName;
        this.itemStoredDate = itemStoredDate;
        this.itemExpirationDate = itemExpirationDate;
        this.itemPosition = itemPosition;
        this.placedBy = placedBy;
        this.itemImgUri = itemImgUri;
        this.days = days;
        this.daysPlaced = daysPlaced;
        this.itemQuantity = itemQuantity;
        this.placedAt = placedAt;
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

    public String getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(String itemPosition) {
        this.itemPosition = itemPosition;
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

    public int getDaysPlaced() {
        return daysPlaced;
    }

    public void setDaysPlaced(int daysPlaced) {
        this.daysPlaced = daysPlaced;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(String placedAt) {
        this.placedAt = placedAt;
    }
}
