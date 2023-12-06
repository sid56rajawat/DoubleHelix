package Servlet;

import model.User;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet(name="LoginServlet",urlPatterns="/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getServletContext().getRequestDispatcher("/JSP/login.jsp").forward(request, response);
	}

	public static String getSHA(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
		return new String(bytes, StandardCharsets.UTF_8);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String userName, password;
		userName = request.getParameter("username");
		password = request.getParameter("password");
		// System.out.println(userName + ", " + password);
		boolean isValidUser = false;
		
		if(password.length()<8) {
			request.setAttribute("error", "Password should be minimum 8 characters");
		}
		else {
			try {
				String hashedPassword = getSHA(password);
				// System.out.println(userName+", "+password+", "+email);
				if(User.isValid(userName,hashedPassword)) {
					// System.out.println("Valid username and password");
					isValidUser = true;
				}
				else {
					request.setAttribute("error", "Username or password invalid");
				}
			} catch (ClassNotFoundException | SQLException | NoSuchAlgorithmException e) {
				request.setAttribute("error", e.toString());
			}
		}
		if(isValidUser) { 
			HttpSession session = request.getSession();
			session.setAttribute("username", userName);
			session.setAttribute("password", password);
			response.sendRedirect("http://localhost:9009/DoubleHelix/home");
		}
		else {doGet(request, response);}
	}

}
