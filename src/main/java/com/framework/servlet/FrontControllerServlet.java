package com.framework.servlet;

import com.framework.util.Mapping;
import com.framework.util.UrlMethod;
import com.framework.annotation.Auth;
import com.framework.annotation.WebApi;
import com.framework.model.ModelAndView;
import com.framework.upload.MapFile;
import com.framework.session.MySession;

import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Collections;
import java.util.Enumeration;

import com.google.gson.Gson;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@MultipartConfig
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

                Parameter[] parameters = method.getParameters();
                Object[] arguments = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    String paramName = parameters[i].getName();
                    String paramValue = request.getParameter(paramName);
                    Class<?> paramType = parameters[i].getType();

                    if (paramType == MySession.class) {
                        // Créer MySession et la remplir depuis HttpSession
                        MySession mySession = new MySession();
                        HttpSession httpSession = request.getSession();
                        System.out.println("[MySession] Chargée depuis HttpSession - isDirty = " + mySession.isDirty());
                        for (String name : Collections.list(httpSession.getAttributeNames())) {
                            mySession.set(name, httpSession.getAttribute(name));
                        }
                        mySession.resetDirty();
                        arguments[i] = mySession;

                    } else if (Map.class.isAssignableFrom(paramType)) {
                        // Fichiers uploadés
                        Map<String, MapFile> fichiers = new java.util.HashMap<>();
                        try {
                            for (Part part : request.getParts()) {
                                String fileName = part.getSubmittedFileName();
                                if (fileName != null && !fileName.isEmpty()) {
                                    byte[] binaire = part.getInputStream().readAllBytes();
                                    fichiers.put(part.getName(), new MapFile(fileName, binaire));
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        arguments[i] = fichiers;

                    } else if (paramValue == null && !isSimpleType(paramType)) {
                        // Objet complexe (S7 bis)
                        Object obj = paramType.getDeclaredConstructor().newInstance();
                        for (Field field : paramType.getDeclaredFields()) {
                            field.setAccessible(true);
                            String fieldValue = request.getParameter(field.getName());
                            if (fieldValue != null) {
                                field.set(obj, fieldValue);
                            }
                        }
                        arguments[i] = obj;

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

                // verif authentification
                if (mapping.getMethode().isAnnotationPresent(Auth.class)) {
                    String authVar = (String) getServletContext().getAttribute("authVar");
                    String redirectVar = (String) getServletContext().getAttribute("redirectVar");
                    HttpSession session = request.getSession(false);
                    if (session == null || session.getAttribute(authVar) == null) {
                        response.sendRedirect(request.getContextPath() + redirectVar);
                        return;
                    }
                }
                
                Object result = method.invoke(instance, arguments);

                // Après invoke : mettre à jour HttpSession depuis MySession
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].getType() == MySession.class) {
                        MySession mySession = (MySession) arguments[i];
                        HttpSession httpSession = request.getSession();
                        if (mySession.isDirty()) {
                            System.out.println("[MySession] Mise à jour HttpSession...");
                            for (Map.Entry<String, Object> entry : mySession.getData().entrySet()) {
                                httpSession.setAttribute(entry.getKey(), entry.getValue());
                            }
                        } else {
                            System.out.println("[MySession] Aucune mise à jour HttpSession (pas de modification)");
                        }
                    }
                }

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

    private boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == boolean.class || type == Boolean.class;
    }
}