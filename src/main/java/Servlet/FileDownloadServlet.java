package Servlet;

import model.DNACryptography;
import model.User;
import model.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FileDownloadServlet
 */
@WebServlet(name="FileDownloadServlet",urlPatterns="/download")
public class FileDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileDownloadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String fileName = request.getParameter("fileName");
		String userName = request.getParameter("userName");
		// System.out.println(fileName+userName);
		InputStream fileContent = null;
		try {
			fileContent = File.download(fileName,User.find(userName));
			fileContent = DNACryptography.decode(fileContent, User.getPassword(userName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        
        // Stream the file content to the response
        byte[] buffer = new byte[4096];
        int bytesRead;
        OutputStream outStream = response.getOutputStream();
        while ((bytesRead = fileContent.read(buffer)) != -1) {
        	// System.out.println(bytesRead);
        	outStream.write(buffer, 0, bytesRead);
        }

        fileContent.close();
        outStream.close();
		// response.sendRedirect("http://localhost:9009/DoubleHelix/home");
        request.getRequestDispatcher("/JSP/home.jsp");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
