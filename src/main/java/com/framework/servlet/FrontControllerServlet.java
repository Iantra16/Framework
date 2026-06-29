package com.framework.servlet;

import com.framework.util.Mapping;
import com.framework.util.Util;
import com.framework.util.UrlMethod;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;

public class FrontControllerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private HashMap<UrlMethod, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        String packageName = getServletConfig().getInitParameter("package");
        try {
            urlMappings = Util.getMappings(packageName);
        } catch (Exception e) {
            throw new ServletException("Erreur scan controllers : " + e.getMessage(), e);
        }
    }

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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = getUrl(request);
        String httpMethod = request.getMethod();
        UrlMethod urlMethod = new UrlMethod(url, httpMethod);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (urlMappings.containsKey(urlMethod)) {
            Mapping mapping = urlMappings.get(urlMethod);
            try {
                // Instanciation du controller
                Class<?> clazz = Class.forName(mapping.getClassName());
                Object instance = clazz.getDeclaredConstructor().newInstance();

                // Invocation de la méthode
                Method method = clazz.getMethod(mapping.getMethodName());
                method.invoke(instance);

                out.println("<html><body>");
                out.println("<p>" + url + " [" + httpMethod + "] -> " + mapping.toString() + "</p>");
                out.println("</body></html>");

            } catch (Exception e) {
                throw new ServletException("Erreur invocation : " + e.getMessage(), e);
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