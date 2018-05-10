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
	 * Servlet that responds to requests for a HTML and returns the processed HTML to the client
	 */
	private static final long serialVersionUID = 1L;

	private static final String DOCUMENT_NOT_FOUND = "No such document found";
	private static final String SERVER_ERROR = "Error is afoot";
	
	public void init() throws ServletException {
		//nothing to init
	}

   public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
	   
	   try {
		   String path = request.getServletPath();
		   
		   URL resource = getServletContext().getResource(path);
		   String rstr;
		   if(resource == null) {
			   System.out.println("Resource was null");
			   rstr = "<h1>" + DOCUMENT_NOT_FOUND + "</h1>";
		   } else {
			   File file = new File(resource.getPath());
			   //the initial parser state should contain request parameter
			   MyHtmlParser parser = new MyHtmlParser().withInitialBinding("request", request).withResource(file);				   
			   rstr = parser.parse();
			   //in case of error, parse method returns empty string
			   if(rstr.isEmpty()) {
				   rstr = "<h1>" + SERVER_ERROR + "</h1>";
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
