/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tehnicomsolution.pushtest;

import static com.tehnicomsolution.pushtest.SmackCcsClient.createJsonMessage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jivesoftware.smack.XMPPException;

/**
 *
 * @author pedja
 */
public class PushServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        /*try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet PushServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet PushServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }*/
        
        final String userName = "16420697747" + "@gcm.googleapis.com";
        final String password = "AIzaSyDkGPO-ddP7jkhwECyzXpF0xoIluhQfUzY";

        SmackCcsClient ccsClient = new SmackCcsClient(response);

        try
        {
            ccsClient.connect(userName, password);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
        
        if(ccsClient.getConnection().isConnected())
        {
            if(!ccsClient.getConnection().isAuthenticated())
            {
                throw new IllegalArgumentException("client not authenticated");
            }
        }
        else
        {
            throw new IllegalArgumentException("client no connected");
        }

        // Send a sample hello downstream message to a device.
        String toRegId = "APA91bHAe-isv5HXhoxuCjHUESwgZmMD5yqdgBrJPgt3NuQ1jsX00yA-_puR8KKEryXvLUBDzeQORxc6TlRP44qmmxngmE3Bs575DIeU2Xyecuzx9qBJoJP_n-6Lih9tUUVN2jnCk9PYMZY_gqkTVGj9UJLqccTVUNa_RdKuJki0kq3ZfX6XOLU";
        String messageId = ccsClient.getRandomMessageId();
        Map<String, String> payload = new HashMap<String, String>();
        payload.put("Hello", "World");
        payload.put("CCS", "Dummy Message");
        payload.put("EmbeddedMessageId", messageId);
        String collapseKey = "sample";
        Long timeToLive = 10000L;
        Boolean delayWhileIdle = true;
        ccsClient.send(createJsonMessage(toRegId, messageId, payload, collapseKey,
                timeToLive, delayWhileIdle));
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
