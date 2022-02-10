package com.example.testingfyp;

public class User {
    String username, email, profileImgUri;

    public User() {
    }

    public User(String username, String email, String profileImgUri) {
        this.username = username;
        this.email = email;
        this.profileImgUri = profileImgUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImgUri() {
        return profileImgUri;
    }

    public void setProfileImgUri(String profileImgUri) {
        this.profileImgUri = profileImgUri;
    }
}
