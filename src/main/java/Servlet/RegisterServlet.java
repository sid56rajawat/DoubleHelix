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

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet(name="RegistrationServlet",urlPatterns="/register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getServletContext().getRequestDispatcher("/JSP/register.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public static String getSHA(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String userName, password, email;
		userName = request.getParameter("username");
		password = request.getParameter("password");
		email = request.getParameter("email");
		
		
		if(password.length()<8) {
			request.setAttribute("error", "Password should be minimum 8 characters");
		}
		else {
			try {
				// System.out.println(userName+", "+password+", "+email);
				String hashedPassword = getSHA(password);
				User.add(userName,hashedPassword,email);
			} catch (NoSuchAlgorithmException | ClassNotFoundException | SQLException e) {
				request.setAttribute("error", e.toString());
			}
			request.setAttribute("success", "User has been registered");
		}
		doGet(request, response);
	}

}
