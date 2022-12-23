package com.ocean.myapp;

public class DataPart {
    private String fileName;
    //private byte[] content;
    //private String type;

    public DataPart() {
    }

    public DataPart(String name, byte[] data) {
        fileName = name;
        //content = data;
    }

    public DataPart(String name, byte[] data, String mimeType) {
        fileName = name;
        //content = data;
        // type = mimeType;
    }

    public DataPart(String fileName) {
        this.fileName = fileName;
    }

    public DataPart(String fileName, String type) {
        this.fileName = fileName;
        //this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

/*    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }*/

    @Override
    public String toString() {
        return "DataPart{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}

