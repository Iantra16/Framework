package com.framework.model;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String viewName;
    private Map<String, Object> model;

    public ModelAndView(){
        this.model = new HashMap<>();
    }
    
    public ModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }

    public void addObject(String key, Object value) {
        this.model.put(key, value);
    }
    
}
