<%@page import="model.File"%>
<%@page import="model.User"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Home</title>
</head>
<body>
	<% String username = (String)session.getAttribute("username"); %>
	<div style="display: flex; background:rgba(0,0,0,0); border: none; box-shadow: 0 0 0 0; padding:0; flex: space-between;">
		<h1 style="color: #007bff;font-family: 'Arial', sans-serif;font-size: larger;">Welcome <% out.println(username); %></h1>
		<button onclick="" style="display: inline-block; margin-left:auto; height: auto;"><a style="color:white;" href="http://localhost:9009/DoubleHelix/login">logout</a></button>
	</div>
	
	<style><%@include file="../CSS/home_style.css"%></style>
	
	
	<div>
        <h2>File Upload</h2>
        <form action="http://localhost:9009/DoubleHelix/upload" method="post" enctype="multipart/form-data">
            <label for="file">Select File:</label>
            <input type="file" id="file" name="file" required>
            <button type="submit">Upload</button>
            <input type="hidden" id="username" name="username" value="${username}">
        </form>
    </div>

    <div>
        <h2>Uploaded Files</h2>
        <ul>
            <%-- Iterate over the list of file names and create download buttons --%>
            <%
			    String[] fileNames = File.getUserFileNames(User.find(username));
			
			    for (String fileName : fileNames) {
			%>
			        <li>
			            <span><%= fileName %></span>
			            <a href="http://localhost:9009/DoubleHelix/download?fileName=<%= fileName %>&amp;userName=<%= username %>">
			                <button>Download</button>
			            </a>
			        </li>
			<%
			    }
			%>

        </ul>
    </div>
    
    <p class="message">${message }</p>
    <script>
	    // Function to hide the messages after a delay
	    function hideMessages() {
	        document.querySelectorAll('.message').forEach(function(message) {
	            setTimeout(function() {
	                message.innerHTML = ''; // Clear the content
	                message.style.marginTop = '-50px'; // Move above the container
	                message.style.opacity = 0; // Make transparent
	            }, 3000); // 3000 milliseconds (3 seconds) delay
	        });
	    }
	
	    // Call the function when the page is loaded
	    window.onload = hideMessages;
	</script>
</body>
</html>