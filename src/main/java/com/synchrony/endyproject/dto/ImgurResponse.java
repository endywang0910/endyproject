package com.synchrony.endyproject.dto;

public class ImgurResponse {

    public Data data;
    public boolean success;
    public int status;

    public static class Data {
        public String id;
        public String link;
        // other fields you might need
    }
}

