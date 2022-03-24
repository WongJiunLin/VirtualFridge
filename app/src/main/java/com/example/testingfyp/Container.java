package com.example.testingfyp;

public class Container {
    String containerName, containerType, containerCreatedDate, createdBy;

    public Container() {
    }

    public Container(String containerName, String containerType, String containerCreatedDate, String createdBy) {
        this.containerName = containerName;
        this.containerType = containerType;
        this.containerCreatedDate = containerCreatedDate;
        this.createdBy = createdBy;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getContainerCreatedDate() {
        return containerCreatedDate;
    }

    public void setContainerCreatedDate(String containerCreatedDate) {
        this.containerCreatedDate = containerCreatedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
