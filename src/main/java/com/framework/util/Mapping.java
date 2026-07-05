package com.framework.util;

import java.lang.reflect.Method;

public class Mapping {
    private Class<?> classe;
    private Method methode;

    public Mapping() {}

    public Mapping(Class<?> classe, Method methode) {
        this.classe = classe;
        this.methode = methode;
    }

    public Class<?> getClasse() { return classe; }
    public Method getMethode() { return methode; }

    public void setClasse(Class<?> classe) { this.classe = classe; }
    public void setMethode(Method methode) { this.methode = methode; }

    @Override
    public String toString() {
        return "classe " + classe.getName() + " method " + methode.getName();
    }
}