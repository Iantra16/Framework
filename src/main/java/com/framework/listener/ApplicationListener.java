package com.framework.listener;

import com.framework.util.Util;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String packageName = servletContext.getInitParameter("package");

        try {
            Util.getMappings(packageName, servletContext);
            servletContext.log("ApplicationListener : scan terminé pour le package " + packageName);
        } catch (Exception e) {
            servletContext.log("ApplicationListener : erreur lors du scan : " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("ApplicationListener : arrêt de l'application");
    }
}