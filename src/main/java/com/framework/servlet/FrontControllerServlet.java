package com.framework.servlet;

import com.framework.util.Mapping;
import com.framework.util.UrlMethod;
import com.framework.annotation.WebApi;
import com.framework.model.ModelAndView;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import com.google.gson.Gson;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class FrontControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = getUrl(request);
        String httpMethod = request.getMethod();
        UrlMethod urlMethod = new UrlMethod(url, httpMethod);

        Map<UrlMethod, Mapping> urlMappings = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("urlMappings");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (urlMappings.containsKey(urlMethod)) {
            Mapping mapping = urlMappings.get(urlMethod);

            try {
                ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
                String className = mapping.getClasse().getSimpleName();
                String beanName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
                Object instance = ctx.getBean(beanName);

                Method method = mapping.getMethode();
                // Object result = method.invoke(instance);

                Parameter[] parameters = method.getParameters();
                Object[] arguments = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    String paramName = parameters[i].getName();
                    String paramValue = request.getParameter(paramName);

                    Class<?> paramType = parameters[i].getType();

                    if (paramValue == null) {
                        arguments[i] = null;
                    } else if (paramType == String.class) {
                        arguments[i] = paramValue;
                    } else if (paramType == int.class || paramType == Integer.class) {
                        arguments[i] = Integer.parseInt(paramValue);
                    } else if (paramType == long.class || paramType == Long.class) {
                        arguments[i] = Long.parseLong(paramValue);
                    } else if (paramType == double.class || paramType == Double.class) {
                        arguments[i] = Double.parseDouble(paramValue);
                    } else if (paramType == boolean.class || paramType == Boolean.class) {
                        arguments[i] = Boolean.parseBoolean(paramValue);
                    } else {
                        arguments[i] = paramValue;
                    }
                }

                Object result = method.invoke(instance, arguments);

                if (mapping.getMethode().isAnnotationPresent(WebApi.class)) {
                    response.setContentType("application/json");
                    Gson gson = new Gson();
                    out.println(gson.toJson(result));

                } else if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;

                    for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    String prefix = getServletContext().getInitParameter("view-prefix");
                    String suffix = getServletContext().getInitParameter("view-suffix");
                    String viewPath = prefix + mv.getViewName() + suffix;

                    request.getRequestDispatcher(viewPath).forward(request, response);

                } else {
                    // Pas de ModelAndView, affichage simple
                    out.println("<html><body>");
                    out.println("<p>" + url + " [" + httpMethod + "] -> " + mapping.toString() + "</p>");
                    out.println("</body></html>");
                }

            } catch (Exception e) {
                System.err.println("Erreur invocation : " + e.getMessage());
                e.printStackTrace();
                out.println("<html><body><h3>Erreur lors de l'invocation</h3></body></html>");
            }

        } else {
            out.println("<html><body>");
            out.println("<h3>Erreur : URL non enregistrée : " + url + " [" + httpMethod + "]</h3>");
            out.println("<p>URLs disponibles :</p><ul>");
            for (UrlMethod u : urlMappings.keySet()) {
                out.println("<li>" + u.toString() + "</li>");
            }
            out.println("</ul></body></html>");
        }
    }

    protected String getUrl(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}