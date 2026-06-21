package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import com.framework.util.Util;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet {

    private List<String> listControllers = new ArrayList<>();

     @Override
    public void init() throws ServletException {
        // Lit le package depuis web.xml
        String packageName = getServletConfig().getInitParameter("package");
        try {
            listControllers = Util.getControllers(packageName);
        } catch (Exception e) {
            throw new ServletException("Erreur scan controllers : " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = getUrl(request);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<p>" + url + "</p>");
        for (String ctrl : listControllers) {
            out.println("<li>" + ctrl + "</li>");
        }
        out.println("</body></html>");
    }

    protected String getUrl(HttpServletRequest request) {
        String url = request.getRequestURI().substring(request.getContextPath().length());
        return url;
    }
}
