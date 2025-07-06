package jp.d77.java.mail_filter_editor.Pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import jp.d77.java.mail_filter_editor.BasicIO.ToolWhois;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;

public class WebWhois extends AbstractWebPage implements InterfaceWebPage{
    //private WhoisResult m_whois;
    public WebWhois(WebConfig cfg) {
        super(cfg);
    }

    /**
     * 1:init
     */
    @Override
    public void init() {
        this.m_config.setPageTitle( this.m_config.getPageTitle() + " - Whois" );
    }

    /**
     * 2:load
     */
    @Override
    public void load() {
        if ( this.getConfig().getMethod("ip").isEmpty() ) return;

        String server = ToolWhois.requestWhois( this.getConfig().getMethod("ip").get() ).orElse( null );
        if ( server == null ){
            this.m_config.alertError.addStringBr( "whois error" );
            return;
        }
        /*
        this.m_whois = ToolNet.getWhois( this.getConfig().getMethod("ip").get() ).orElse( null );
        if ( this.m_whois == null ) {
            this.m_config.alertError.addStringBr( "whois error" );
            return;
        }
        if ( this.m_whois.getError().isPresent() ) this.m_config.alertError.addStringBr( "whois error:" + this.m_whois.getError().get() );
         */
    }

    /**
     * 3:post_save_reload
     */
    @Override
    public void post_save_reload() {
    }

    /**
     * 4:proc
     */
    @Override
    public void proc() {
    }

    /**
     * 5:displayHeader
     */
    @Override
    public void displayHeader(){
        super.displayHeader();
        this.m_html.addString(BSSForm.getTableHeader( "mfe" ));
        this.m_html.addString(
            "<STYLE>"
            + ".scroll-cell {\n"
            + "  max-height: 300px;\n"
            + "  overflow-y: auto;\n"
            + "  display: block;\n"
            + "}\n"
            + "</STYLE>"
            );
    }

    /**
     * 6:displayNavbar
     */
    @Override
    public void displayNavbar() {
        Debugger.TracePrint();
        SharedWebLib.Navbar( this.m_config, this.m_html );
    }

    /**
     * 7:displayInfo
     */
    @Override
    public void displayInfo(){
        super.displayInfo();
    }

    /**
     * 8:displayBody
     */
    @Override
    public void displayBody(){
        super.displayBody();

        // ヘッダー
        if ( this.getConfig().getMethod("ip").isEmpty() ) {
            this.m_html.addStringCr( HtmlString.h( 1, "ip=null") );
            return;
        }else{
            this.m_html.addStringCr( HtmlString.h( 1, "ip=" + this.getConfig().getMethod("ip").get() ) );
        }
        //if ( this.m_whois == null ) return;

        LinkedHashMap<String,ToolWhois.WhoisData> wd = new LinkedHashMap<String,ToolWhois.WhoisData>();
        String server = "whois.iana.org";
        while ( true ) {
            if ( wd.containsKey( server ) ) break;
            Debugger.InfoPrint( "ip=" + this.getConfig().getMethod("ip").get() + " server=" + server );
            ToolWhois.WhoisData w = ToolWhois.getWhoisCache().get( this.getConfig().getMethod("ip").get() ).get( server );
            wd.put( server, w );
            if ( w.getChildServer().isEmpty() ) break;
            server = w.getChildServer().get();
        }

        List<String> keys = new ArrayList<>(wd.keySet());
        Collections.reverse(keys);

        for ( String key : keys ) {

            BSSForm f = BSSForm.newForm();
            // Results
            f.tableTop("whois_table");
            f.tableBodyTop();
            if ( wd.get(key).getServer().isPresent() ){
                f.tableRowTop();
                f.tableTh( "WhoisServer" );
                f.tableTd( String.join(",", wd.get(key).getServer().get() ) );
                f.tableRowBtm();
            }
            if ( wd.get(key).getChildServer().isPresent() ){
                f.tableRowTop();
                f.tableTh( "ReferredServer" );
                f.tableTd( String.join(",", wd.get(key).getChildServer().get() ) );
                f.tableRowBtm();
            }
            if ( wd.get(key).getCidr().isPresent() ){
                f.tableRowTop();
                f.tableTh( "CIDR" );
                f.tableTdHtml( String.join(",", SharedWebLib.linkIpBasic( wd.get(key).getCidr().get().toArray( new String[0] ) ) ) );
                f.tableRowBtm();
            }
            if ( wd.get(key).getCc().isPresent() ){
                f.tableRowTop();
                f.tableTh( "Country" );
                f.tableTd( String.join(",", wd.get(key).getCc().get() ) );
                f.tableRowBtm();
            }
            if ( wd.get(key).getOrg().isPresent() ){
                f.tableRowTop();
                f.tableTh( "Organization" );
                f.tableTd( String.join(",", wd.get(key).getOrg().get() ) );
                f.tableRowBtm();
            }
            f.tableBodyBtm();
            f.tableBtm();

            f.tableTop("whois_table");
            f.tableBodyTop();
            f.tableRowTop();
            f.tableTdHtml( "<DIV class=\"scroll-cell\">\n" + HtmlString.HtmlEscapeBr( wd.get(key).getQueryResult() ) + "\n</DIV>\n" );
            f.tableRowBtm();
            f.tableBodyBtm();
            f.tableBtm();

            this.m_html.addString( f.toString() );
        }
    }

    /**
     * 9:displayBottomInfo
     */
    @Override
    public void displayBottomInfo(){
        super.displayBottomInfo();
    }

    /**
     * 10:displayFooter
     */
    @Override
    public void displayFooter(){
        super.displayFooter();
    }
}
