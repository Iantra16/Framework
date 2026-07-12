package com.framework.servlet;

import com.framework.util.Mapping;
import com.framework.util.UrlMethod;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Map;
import com.framework.model.ModelAndView;

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

        // Récupération de la map depuis le ServletContext (remplie par le Listener au
        // démarrage)
        Map<UrlMethod, Mapping> urlMappings = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("urlMappings");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (urlMappings.containsKey(urlMethod)) {
            Mapping mapping = urlMappings.get(urlMethod);

            try {
                Object instance = mapping.getClasse().getDeclaredConstructor().newInstance();
                Method method = mapping.getMethode();
                Object val = method.invoke(instance);

                if (val instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) val;

                    if (mv.getModel() != null) {
                        for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    String prefix = getServletContext().getInitParameter("view-prefix");
                    String suffix = getServletContext().getInitParameter("view-suffix");
                    String viewPath = prefix + mv.getViewName() + suffix;

                    request.getRequestDispatcher(viewPath).forward(request, response);
                }

                out.println("<html><body>");
                out.println("<p>" + url + " [" + httpMethod + "] -> " + mapping.toString() + "</p>");
                out.println("</body></html>");

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