package com.framework.listener;

import com.framework.annotation.Controller;
import com.framework.util.Util;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String packageName = servletContext.getInitParameter("package");

        // Nom de la variable de session qui indique que l'utilisateur est connecté
        servletContext.setAttribute("authVar", servletContext.getInitParameter("auth-variable"));

        // Nom de la variable de session qui contient le rôle de l'utilisateur
        servletContext.setAttribute("roleVar", servletContext.getInitParameter("role-variable"));

        try {
            // Scan des URL mappings
            Util.getMappings(packageName, servletContext);
            servletContext.log("ApplicationListener : scan terminé pour le package " + packageName);

            // Récupérer le contexte Spring (démarré avant par ContextLoaderListener)
            ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            if (ctx == null) {
                servletContext.log("ApplicationListener : contexte Spring introuvable, controllers non instanciés");
                return;
            }

            // Une seule instance par controller, stockée dans le ServletContext
            Map<String, Object> controllerInstances = new HashMap<>();
            for (Class<?> clazz : Util.getAllClasses(packageName)) {
                if (Util.hasAnnotation(clazz, Controller.class)) {
                    String className = clazz.getSimpleName();
                    String beanName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
                    try {
                        controllerInstances.put(beanName, ctx.getBean(beanName));
                    } catch (Exception e) {
                        servletContext.log("ApplicationListener : bean non trouvé dans Spring : " + beanName);
                        // Ignorer si pas dans Spring ? Ou gérer différemment ?
                    }
                }
            }
            servletContext.setAttribute("controllerInstances", controllerInstances);
            
            String message = "ApplicationListener : " + controllerInstances.size() + " controller(s) instancié(s).";
            servletContext.log(message);
            System.out.println(message);

        } catch (Exception e) {
            servletContext.log("ApplicationListener : erreur lors du scan : " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("ApplicationListener : arrêt de l'application");
    }
}