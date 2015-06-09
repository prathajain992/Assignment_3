import java.io.IOException;
import java.util.Map;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author c0653123
 */
 
 
public class ProductServlet  extends HttpServlet{
    private  Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.err.println("JDBC Driver Not Found: " + ex.getMessage());
        }

        try {
            String jdbc = "jdbc:mysql://ipro.lambton.on.ca/inventory";
            conn = DriverManager.getConnection(jdbc, "products", "products");
        } catch (SQLException ex) {
            System.err.println("Failed to Connect: " + ex.getMessage());
        }
        return conn;
    }

    
   private String getResults(String query, String... arr) {
     String result=new String();
     try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= arr.length; i++) {
                pstmt.setString(i, arr[i - 1]);
            }
            ResultSet s = pstmt.executeQuery();
            JSONArray productArr = new JSONArray();
            while (s.next()) {
                Map productMap = new LinkedHashMap();
                productMap.put("productID", s.getInt("productID"));
                productMap.put("name", s.getString("name"));
                productMap.put("description", s.getString("description"));
                productMap.put("quantity", s.getInt("quantity"));
                productArr.add(productMap);
            }
            result = productArr.toString();
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result.replace("},", "},\n");
       
     }    
   
   
    @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try (PrintWriter out = response.getWriter()) {
            if (!request.getParameterNames().hasMoreElements()) {
                out.println(getResults("SELECT * FROM products"));
            } else {
                int productID = Integer.parseInt(request.getParameter("productID"));
                out.println(getResults("SELECT * FROM products WHERE productID = ?", String.valueOf(productID)));
            }
        } catch (IOException ex) {
            response.setStatus(500);
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
  
    @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Set<String> setKey = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (setKey.contains("name") && setKey.contains("description") && setKey.contains("quantity")) {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `products`(`productID`, `name`, `description`, `quantity`) "
                        + "VALUES (null, '" + request.getParameter("name") + "', '"
                        + request.getParameter("description") + "', "
                        + request.getParameter("quantity") + ");"
                );
                try {
                    pstmt.executeUpdate();
                    request.getParameter("productID");
                    doGet(request, response);
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Data inserted Error while retrieving data.");
                }
            } else {
                out.println("Error: Not enough data to input");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
 
    @Override
   protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Set<String> setKey = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (setKey.contains("productID") && setKey.contains("name") && setKey.contains("description") && setKey.contains("quantity")) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE `products` SET `name`='"
                        + request.getParameter("name") + "',`description`='"
                        + request.getParameter("description")
                        + "',`quantity`=" + request.getParameter("quantity")
                        + " WHERE `productID`=" + request.getParameter("productID"));
                try {
                    pstmt.executeUpdate();
                    doGet(request, response); 
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error putting values.");
                }
            } else {
                out.println("Error: Not enough data to update");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
   
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> setKey = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (setKey.contains("productID")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `products` WHERE `productID`=" + request.getParameter("productID"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error in deleting the product.");
                }
            } else {
                out.println("Error: data to delete");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

