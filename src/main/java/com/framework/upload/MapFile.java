package com.framework.upload;

public class MapFile {
    private String fileName;
    private String extension;
    private byte[] binaire;

    public MapFile(String fileName, byte[] binaire) {
        this.fileName = fileName;
        this.binaire = binaire;
        this.extension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1)
                : "";
    }

    public String getFileName() { return fileName; }
    public String getExtension() { return extension; }
    public byte[] getBinaire() { return binaire; }

    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setExtension(String extension) { this.extension = extension; }
    public void setBinaire(byte[] binaire) { this.binaire = binaire; }
}