package com.framework.session;

import java.util.HashMap;
import java.util.Map;

public class MySession {
    private Map<String, Object> data = new HashMap<>();
    
    public Object get(String key) { 
        return data.get(key); 
    }
    public void set(String key, Object value) { 
        data.put(key, value); 
    }
    public void remove(String key) { 
        data.remove(key); 
    }
    public Map<String, Object> getData() { 
        return data; 
    }
}