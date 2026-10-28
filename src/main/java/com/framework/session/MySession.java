package com.framework.session;

import java.util.HashMap;
import java.util.Map;

public class MySession {
    private Map<String, Object> data = new HashMap<>();
    private boolean isDirty = false;
    

    public Object get(String key) { 
        return data.get(key); 
    }
    public void set(String key, Object value) { 
        data.put(key, value); 
        isDirty = true;
    }
    public void remove(String key) { 
        data.remove(key); 
        isDirty = true;
    }
    public Map<String, Object> getData() { 
        return data; 
    }
    public boolean isDirty() {
        return isDirty;
    }
    public boolean resetDirty(){
        return isDirty = false;
    }
    
}