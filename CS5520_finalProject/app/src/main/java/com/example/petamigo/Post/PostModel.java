package com.example.petamigo.Post;

public class PostModel {
    private String postWord;
    private String postLoc;
    private String postImg;
    private String postUser;
    private String postId;

    public PostModel(String postWord, String postLoc, String postImg, String postUser) {
        this.postWord = postWord;
        this.postLoc = postLoc;
        this.postImg = postImg;
        this.postUser = postUser;
    }

    public PostModel() {
    }

    public String getPostWord() {
        return postWord;
    }

    public String getPostLoc() {
        return postLoc;
    }

    public String getPostImg() {
        return postImg;
    }

    public void setPostWord(String postWord) {
        this.postWord = postWord;
    }

    public void setPostLoc(String postLoc) {
        this.postLoc = postLoc;
    }

    public void setPostImg(String postImg) {
        this.postImg = postImg;
    }

    public String getPostUser() {
        return postUser;
    }

    public void setPostUser(String postUser) {
        this.postUser = postUser;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
