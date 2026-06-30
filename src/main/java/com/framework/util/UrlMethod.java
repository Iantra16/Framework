package com.framework.util;

import java.util.Objects;

public class UrlMethod {
    private String url;
    private String method;

    public UrlMethod() {}

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof UrlMethod))
            return false;
        UrlMethod other = (UrlMethod) obj;
        if (this.url.equals(other.url) && this.method.equals(other.method)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }

    @Override
    public String toString() {
        return method + " " + url;
    }
}