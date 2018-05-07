package biz.netcentric;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PersonServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	   }

	   public void doGet(HttpServletRequest request, HttpServletResponse response)
	      throws ServletException, IOException {
		   
		   try {
			   String path = request.getServletPath();
			   
			   URL resource = getServletContext().getResource(path);
			   String rstr;
			   if(resource == null) {
				   System.out.println("Resource was null");
				   rstr = "<h1>" + "No such document found" + "</h1>";
			   } else {
				   File file = new File(resource.getPath());
				   rstr = PersonJSoupParser.parse(file, request);
				   if(rstr.isEmpty()) {
					   rstr = "<h1>" + "Error is afoot" + "</h1>";
				   }
			   }
			   
		      // Set response content type
		      response.setContentType("text/html");
		      //write the new html
		      PrintWriter out = response.getWriter();
		      out.println(rstr);
		   } catch(Exception ex) {
			   ex.printStackTrace();
		   }
	   }

	   public void destroy() {
	      // do nothing.
	   }
}
