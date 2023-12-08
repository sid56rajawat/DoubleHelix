package Servlet;

import model.DNACryptography;
import model.File;
import model.User;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet implementation class FileUploadServlet
 */
@WebServlet(name="FileUploadServlet",urlPatterns="/upload")
@MultipartConfig(maxFileSize = 16177215) // upload file's size up to 16MB

public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.sendRedirect("http://localhost:9009/DoubleHelix/home");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		InputStream inputStream = null; // input stream of the upload file
		String fileName;
		String userName = request.getParameter("username");
		
		Part filePart = request.getPart("file");
		
		if(filePart != null) {
			fileName = extractFileName(filePart);
			inputStream = filePart.getInputStream();
			try {
				InputStream DNAEncryptedFile = DNACryptography.encode(inputStream,User.getPassword(userName));
				File.upload(fileName, DNAEncryptedFile, User.find(userName));
				request.setAttribute("message", "File uploaded successfully");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				request.setAttribute("message", e);
			}
		}
	
		RequestDispatcher reqd = request.getRequestDispatcher("home");
		reqd.forward(request, response);
	}

}
