package gsn.http.ac;

import gsn.Main;
import gsn.http.WebConstants;


import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Behnaz Bostanipour
 * Date: Apr 27, 2010
 * Time: 2:24:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyAdminManagementServlet extends HttpServlet
{
    /****************************************** Servlet Methods*******************************************/
    /******************************************************************************************************/

    public void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException
    {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        // Get the session
        HttpSession session = req.getSession();

        User user = (User) session.getAttribute("user");
        if (user == null)
       {
           this.redirectToLogin(req,res);
       }
       else
       {
           if(user.isAdmin()== false)
           {
               res.sendError( WebConstants.ACCESS_DENIED , "Access denied." );
           }
           else
           {
               this.checkSessionScheme(req,res);
               this.printHeader(out);
               this.printLayoutMastHead(out, user );
               this.printLayoutContent(out);
               this.printAdminManagementLinks(out);
               this.printLayoutFooter(out);
           }

       }
    }
    public void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException
    {
       this.doGet(req,res);
    }


    
    /****************************************** HTML Printing Methods*******************************************/
    /***********************************************************************************************************/


    private void printHeader(PrintWriter out)
	{
        out.println("<HTML>");
        out.println("<HEAD>");
		out.println("<TITLE>Admin Only</TITLE>");
        out.println(" <link rel=\"stylesheet\" media=\"screen\" type=\"text/css\" href=\"/style/acstyle.css\"/>");
        //printStyle(out);
        out.println("</HEAD>");
        out.println("<body>");
        out.println("<div id=\"container\">");
        out.println("<div class=box>");

	}
    private void printLayoutMastHead(PrintWriter out, User user)
    {
        out.println("<div id=\"masthead\">");
        out.println("<h1><a id=\"gsn-name\" style=\"\" href=\"/\">" + Main.getContainerConfig( ).getWebName( ) + "</a></h1>");

        out.println("</div>");
        out.println("<div id=\"navigation\">");
        out.println("<div id=\"menu\">");
        this.printLinks(out);
        out.println("</div>");
        out.println("<div id=\"logintext\">");
        this.printUserName(out, user);
        out.println("</div>");
        out.println("</div>");
    }
    private void printLayoutContent(PrintWriter out)
    {
        out.println("<div id=\"content\">");
    }
    private void printLayoutFooter(PrintWriter out)
    {
        out.println("</div>");
        out.println("<div class=\"separator\">");
        out.println("<div id=\"footer\">");
        out.println("<table width=\"100%\"><tr>");
        out.println("<td style=\"width:50%;color:#444444;font-size:12px;line-height:1.4em;\"><b>A Project of <a href=\"http://www.ethz.ch\" target=\"_blank\">ETH Zurich</a>, <a href=\"http://www.unibas.ch\" target=\"_blank\">Uni Basel</a> and <a href=\"http://www.uzh.ch\" target=\"_blank\">Uni Zurich</a></b></td>");
        out.println("<td style=\"text-align:right;width:50%;font-size:9px;color:#666666;\">Powered by <a href=\"http://gsn.sourceforge.net/\">GSN</a>,  Distributed Information Systems Lab, EPFL 2006</td>");
		out.println("</tr></table>");
        out.println("</div>");//footer
        out.println("</div>");//separator
        out.println("</div>");//box
        out.println("</div>");//container
        out.println("</body>");
        out.println("</html>");
    }

    private void printLinks(PrintWriter out)
    {
        out.println("<li><a href=\"/\">Home</a></li>");
        out.println("<li><a href=/gsn/MyAccessRightsManagementServlet>access rights</a></li>");
        out.println("<li class=\"selected\"><a href=/gsn/MyAdminManagementServlet>admin</a></li>");

    }
    private void printUserName(PrintWriter out, User user)
    {
        //String username=user.getUserName();
        out.println("<li><a href=\"/gsn/MyLogoutHandlerServlet\">logout</a></li>");
        out.println("<li><div id=\"logintextprime\">logged in as : "+user.getUserName()+"</div></li>");
    }

    public void printAdminManagementLinks(PrintWriter out)
    {
        out.println("<p>Welcome ! you have the following options:</p>");
        out.println("<ul class=linklistul >");
        out.println("<LI class=linklistli><a href=\"/gsn/MyDisplayACTablesContentServlet\">Check access right tables content</a></LI>");
        out.println("<LI class=linklistli><a href=\"/gsn/MyCreateGroupServlet\">Create new groups</a></LI>");
        out.println("<LI class=linklistli><a href=\"/gsn/MyGroupManagementServlet\">Delete or change group structure</a></LI>");
        out.println("<LI class=linklistli><a href=/gsn/MyUserUpdateWaitingListServlet>Check and validate user updates </a></LI>");
        out.println("<LI class=linklistli><a href=\"/gsn/MyUserCandidateWaitingListServlet\">Check and validate new sign up requests </a></LI>");
        out.println("<LI class=linklistli><a href=\"/gsn/MyDataSourceCandidateWaitingListServlet\">Check and validate new virtual sensors registration requests </a></LI>");
        out.println("</ul>");
    }


    /****************************************** Client Session related Methods*******************************************/
    /********************************************************************************************************************/

   
    private void checkSessionScheme(HttpServletRequest req, HttpServletResponse res)throws IOException
    {

         if(req.getScheme().equals("https")== true)
        {
            if((req.getSession().getAttribute("scheme")==null))
            {
                req.getSession().setAttribute("scheme","https");
            }
        }
         else if(req.getScheme().equals("http")== true )
        {
             if((req.getSession().getAttribute("scheme")==null))
            {
                req.getSession().setAttribute("scheme","http");
            }
            res.sendRedirect("https://"+req.getServerName()+":"+ Main.getContainerConfig().getSSLPort()+"/gsn/MyAdminManagementServlet");

        }
    }
    private void redirectToLogin(HttpServletRequest req, HttpServletResponse res)throws IOException
    {
        req.getSession().setAttribute("login.target", HttpUtils.getRequestURL(req).toString());
        res.sendRedirect("/gsn/MyLoginHandlerServlet");
    }



}
