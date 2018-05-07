package biz.netcentric;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class PersonServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	      // Do required initialization
	   }

	   public void doGet(HttpServletRequest request, HttpServletResponse response)
	      throws ServletException, IOException {
	      
		   
		  /* System.out.println(request.getPathInfo());
		   System.out.println(request.getPathTranslated());
		   System.out.println(request.getContextPath());
		   System.out.println(request.getServletPath());
		   */
		   try {
			   String path = request.getServletPath();
//			   System.out.println("path = " + path);
			   
			   URL resource = getServletContext().getResource(path);
			   String rstr;
			   if(resource == null) {
				   System.out.println("resource was null");
				   rstr = "<h1>" + "Ana banana" + "</h1>";
			   } else {
//				   System.out.println(resource.getPath());
				   File file = new File(resource.getPath());
				   HtmlTraverser.traverse(file);
				   /*rstr = PersonJSoupParser.parse(file, request);
				   if(rstr.isEmpty()) {
					   rstr = "<h1>" + "Error is afoot" + "</h1>";
				   }*/
			   }
			   
			  
//			   getServletContext().
			   //for each <script> element execute its body as javascript
			   //special element, replace the $values with real values
			   //return the new html
			   
		      // Set response content type
		      response.setContentType("text/html");
	
		      Map<String, String[]> params = request.getParameterMap();
		      if(params.get("id") != null) {
		    	  String val = params.get("id")[0];
		    	  Person p = Person.lookup(val);
		      }
		      
		      // Actual logic goes here.
		      PrintWriter out = response.getWriter();
//		      out.println(rstr);
		      out.println("<h1>" + "Error is afoot" + "</h1>");
		   } catch(Exception ex) {
			   ex.printStackTrace();
		   }
	   }

	   public void destroy() {
	      // do nothing.
	   }
}
