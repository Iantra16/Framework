package com.framework.servlet;

import com.framework.util.Mapping;
import com.framework.util.Util;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.HashMap;

public class FrontControllerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private HashMap<String, Mapping> urlMappings = new HashMap<>();

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
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (urlMappings.containsKey(url)) {
            Mapping mapping = urlMappings.get(url);
            out.println("<html><body>");
            out.println("<p>" + url + " -> " + mapping.toString() + "</p>");
            out.println("</body></html>");
        } else {
            out.println("<html><body>");
            out.println("<h3>Erreur : URL non enregistrée : " + url + "</h3>");
            out.println("<p>URLs disponibles :</p><ul>");
            for (String u : urlMappings.keySet()) {
                out.println("<li>" + u + "</li>");
            }
            out.println("</ul></body></html>");
        }
    }

    protected String getUrl(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}