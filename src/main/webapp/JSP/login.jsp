<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Login</title>
</head>
<body>
	<style><%@include file="../CSS/register_style.css"%></style>
	<p class="error">${error }</p>
	<p class="success">${success }</p>
    <div class="container">
        <h2>User Login</h2>
        <form action="http://localhost:9009/DoubleHelix/login" method="post">
            <label for="username">Username:</label>
            <input type="text" id="username" name="username" required>
            
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Login</button>
        </form>
        <p>Don't have an account? <a href="http://localhost:9009/DoubleHelix/register">Register here</a>.</p>
    </div>
    
    <script>
	    // Function to hide the messages after a delay
	    function hideMessages() {
	        document.querySelectorAll('.error, .success').forEach(function(message) {
	            setTimeout(function() {
	                message.innerHTML = ''; // Clear the content
	                message.style.marginTop = '-50px'; // Move above the container
	                message.style.opacity = 0; // Make transparent
	            }, 6000); // 3000 milliseconds (3 seconds) delay
	        });
	    }
	
	    // Call the function when the page is loaded
	    window.onload = hideMessages;
	</script>
</body>
</html>