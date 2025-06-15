package jp.d77.java.mail_filter_editor.BasicIO;

public class BSS {
    public enum ALERT {
        PRIMATY    ("alert-primary"),
        SECONDARY  ("alert-secondary"),
        SUCCESS    ("alert-success"),
        DANGER     ("alert-danger"),
        WARNING    ("alert-warning"),
        INFO       ("alert-info"),
        LIGHT      ("alert-light"),
        DARK       ("alert-dark"),

        ERROR      ("alert-danger"),
        DEBUG      ("alert-warning");

        private final String label;
        ALERT(String label) { this.label = label; }
        public String toString() { return label; }
    }

    // -----------------------------------------------------------------------------
    // Infomation
    // -----------------------------------------------------------------------------
    /**
     * BSS Alert
     * @param type ALRTY_***
     * @param val String
     * @return output strings
     */
    public static String alert( ALERT type, String val ){
        if ( val == null ){ return ""; }
        if ( val.isEmpty() || val.isBlank() ){ return ""; }
    
        String res = "<DIV class=\"alert " + type.toString() + "\" role=\"alert\">" + "\n";
        res = res + val + "\n";
        res = res + "</DIV>   <!-- alert -->" + "\n";
        return res;
    }

    // -----------------------------------------------------------------------------
    // Header / Footer
    // -----------------------------------------------------------------------------

    /**
     * HEADER表示
     */
    public static String getHeader( WebConfig cfg ){
//        Debugger.TracePrint(this);
        //HtmlString header = HtmlString.init();

        return HtmlString.init()
            .addStringCr( "<HTML lang=\"ja\">" )
            .addStringCr( "<HEAD>" )
            .addStringCr( "<META http-equiv=\"Content-type\" content=\"text/html; charset=UTF-8\">" )
            .addStringCr( "<META name=\"viewport\" content=\"width=device-width, initial-scale=1\">" )
            .addStringCr( "<LINK href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM\" crossorigin=\"anonymous\">" )
            .addStringCr( "<LINK href=\"https://cdn.datatables.net/t/bs-3.3.6/jqc-1.12.0,dt-1.10.11/datatables.min.css\" rel=\"stylesheet\">" )
            .addStringCr( "<LINK href=\"https://cdn.datatables.net/fixedheader/3.2.1/css/fixedHeader.bootstrap.min.css\" rel=\"stylesheet\">" )
            .addStringCr( "<SCRIPT src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz\" crossorigin=\"anonymous\"></SCRIPT>" )
            .addStringCr( "<SCRIPT src=\"https://cdn.datatables.net/t/bs-3.3.6/jqc-1.12.0,dt-1.10.11/datatables.min.js\"></SCRIPT>" )
            .addStringCr( "<SCRIPT src=\"https://cdn.datatables.net/fixedheader/3.2.1/js/dataTables.fixedHeader.min.js\" type=\"text/javascript\"></SCRIPT>" )
            .addStringCr( cfg.getAddHeader().toString() )
            .addStringCr( "<STYLE>" )
            .addStringCr( "body { padding-top: 70px; }" )
            .addStringCr( "</STYLE>" )
            .addStringCr( "<TITLE>" + HtmlString.HtmlEscape( cfg.ProgramName ) + "</TITLE>" )
            .addStringCr( "</HEAD>" )
            .addStringCr( "<BODY>" )
            .toString() + "\n";
    }

    /**
     * FOOTER表示
     */
    public static String getFooter( WebConfig cfg ) {
        return HtmlString.init()
    	    .addStringCr( "<footer class=\"footer\">" )
	        .addStringCr( HtmlString.HtmlEscape( cfg.ProgramName ) + " " + HtmlString.HtmlEscape( cfg.ProgramVersion ) )
	        .addStringCr( "</footer>" )
	        .addStringCr( "</BODY>" )
	        .addStringCr( "</HTML>" )
            .toString();
    }

    // -----------------------------------------------------------------------------
    // Navbar
    // -----------------------------------------------------------------------------
    public static String getNavbarHeader( String page_title ){
        return HtmlString.init()
            .addStringCr( "<NAV class=\"navbar navbar-expand-lg bg-body-tertiary navbar-fixed-top fh-fixedHeader\">")
            .addStringCr(1,"<BUTTON class=\"navbar-toggler\" type=\"button\" data-bs-toggle=\"collapse\" data-bs-target=\"#navbarSupportedContent\" aria-controls=\"navbarSupportedContent\" aria-expanded=\"false\" aria-label=\"Toggle navigation\">")
            .addStringCr(2,"<SPAN class=\"navbar-toggler-icon\"></SPAN>")
            .addStringCr(1,"</BUTTON>")
            .addStringCr(1,"<A class=\"navbar-brand\" href=\"#\">" + HtmlString.HtmlEscape( page_title ) + "</A>")
            .addStringCr(1,"<DIV class=\"collapse navbar-collapse\" id=\"navbarSupportedContent\">")
            .addStringCr(2,"<UL class=\"navbar-nav me-auto mb-2 mb-lg-0\">")
            .toString();
    }

    public static String getNavbarFooter(){
        return HtmlString.init()
            .addStringCr(2,"</UL>   <!-- navbar-nav -->")
            .addStringCr(1,"</DIV>   <!-- navbar-collapse -->")
            .addStringCr("</NAV>   <!-- navbar -->")
            .toString();
    }

    public static String getNavbarLinkItem( BSOpts bsdata ){
        String sText;

        if ( !bsdata.contains("title") ) return "";
        if ( !bsdata.contains("class") ) bsdata.fclass("nav-link");

        sText = bsdata.get("title").orElse("-");
        if ( bsdata.contains("href") ) {
            sText = "<A " + bsdata.autoget("class") + bsdata.autoget("href") + bsdata.autoget("target") + "\">" + HtmlString.HtmlEscape(sText) + "</A>";
        }

        if ( bsdata.get("class").get().equals("nav-link") ){
            return HtmlString.sp(3) + "<LI class=\"nav-item\">" + sText + "</LI>";
        }else{
            return HtmlString.sp(3) + "<LI>" + sText + "</LI>";
        }
    }

    public static String NavbarHR(){
        return HtmlString.sp(3) + "<HR class=\"dropdown-divider\">\n";
    }

    public static String NavbarDropDown( String sToggle, String... items ){
        String item = "";
        for (int i = 0; i < items.length ; i++){
            item += "    " + items[i];
            //item += HtmlString.sps(5, items[i] );
        }
        return HtmlString.init()
            .addStringCr(3,"<LI class=\"nav-item dropdown\">")
            .addStringCr(4,"<A class=\"nav-link dropdown-toggle\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\" aria-expanded=\"false\">"
            + HtmlString.HtmlEscape( sToggle ) + "</A>")
            .addStringCr(4,"<UL class=\"dropdown-menu\">")
            .addStringCr(item)
            .addStringCr(4,"</UL>")
            .addStringCr(3,"</LI>")
            .toString();
    }
}
