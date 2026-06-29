package com.framework.util;

public class UrlMethod {
    private String url;
    private String method;

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public void setUrl(String url) { this.url = url; }
    public void setMethod(String method) { this.method = method; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UrlMethod)) return false;
        UrlMethod other = (UrlMethod) obj;
        return this.url.equals(other.url) && this.method.equals(other.method);
    }

    @Override
    public int hashCode() {
        return (url + method).hashCode();
    }

    @Override
    public String toString() {
        return method + " " + url;
    }
}