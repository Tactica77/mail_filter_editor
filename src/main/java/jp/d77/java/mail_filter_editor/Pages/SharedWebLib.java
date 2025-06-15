package jp.d77.java.mail_filter_editor.Pages;

import jp.d77.java.mail_filter_editor.BasicIO.BSOpts;
import jp.d77.java.mail_filter_editor.BasicIO.BSS;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.HtmlString;

public class SharedWebLib {
    public static void Navbar( WebConfig cfg, HtmlString html ){
        Debugger.TracePrint();
        
        html.addString( BSS.getNavbarHeader( cfg.getPageTitle() ) );
        html.addString( BSS.getNavbarLinkItem( BSOpts.init().title("TOP").href("/") ) );

        // Menu
        html.addString( BSS.getNavbarLinkItem( BSOpts.init().title("Block Editor").href("/block_editor") ) );
        html.addString( BSS.getNavbarLinkItem( BSOpts.init().title("Mail Log").href("/mail_log") ) );

        html.addString( BSS.getNavbarFooter() );
    }
}
