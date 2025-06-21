package jp.d77.java.mail_filter_editor.Pages;

import java.util.ArrayList;

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

    /**
     * BlockEdirot画面へのリンク
     * @param cidr
     * @param cc
     * @param org
     * @return
     */
    public static String linkBlockEditor( String cidr, String cc, String org ){
        ArrayList<String> res = new ArrayList<String>();
        if ( cc != null && ! cc.isEmpty() ) res.add( "edit_new_cc=" + HtmlString.HtmlEscape( cc ) );
        if ( cidr != null && ! cidr.isEmpty() ) res.add( "edit_new_cidr=" + HtmlString.HtmlEscape( cidr ) );
        if ( org != null && ! org.isEmpty() ) res.add( "edit_new_org=" + HtmlString.HtmlEscape( org ) );
        return "<A Href=\"/block_editor?" + String.join("&", res) + "\" target=\"blank\">" + HtmlString.HtmlEscape( cidr ) + "</A>"
            + SharedWebLib.linkWhois( HtmlString.UriEscape( cidr ) )
            + SharedWebLib.linkSubnets( HtmlString.UriEscape( cidr ) );
    }

    public static String[] linkIpBasic( String... ips ){
        ArrayList<String> ret = new ArrayList<String>();
        for( String ip: ips ){
            ret.add( HtmlString.HtmlEscape( ip ) + SharedWebLib.linkWhois( ip ) + SharedWebLib.linkSubnets( ip ) );
        }
        return ret.toArray( new String[0] );
    }

    /**
     * whois検索画面へのハイパーリンクHTMLを取得。表示は(W)
     * @param ip
     * @return
     */
    public static String linkWhois( String ip ){
        return "<A Href=\"/whois?ip=" + ip + "\" target=\"_blank\">(W)</A>";
    }

    /**
     * サブネット検索画面へのハイパーリンクHTMLを取得。表示は(S)
     * @param ip
     * @return
     */
    public static String linkSubnets( String ip ){
        return "<A Href=\"/subnets?ip=" + ip + "\" target=\"_blank\">(S)</A>";
    }
}
