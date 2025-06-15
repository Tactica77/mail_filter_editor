package jp.d77.java.mail_filter_editor.Pages;

import jp.d77.java.mail_filter_editor.BasicIO.BSSForm;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.HtmlString;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNet;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.BasicIO.WhoisResult;

public class WebWhois extends AbstractWebPage implements InterfaceWebPage{
    private WhoisResult m_whois;
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
        this.m_whois = ToolNet.getWhois( this.getConfig().getMethod("ip").get() ).orElse( null );
        if ( this.m_whois == null ) this.m_config.alertError.addStringBr( "whois error" );
        if ( this.m_whois.getError().isPresent() ) this.m_config.alertError.addStringBr( "whois error:" + this.m_whois.getError().get() );
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

        if ( this.getConfig().getMethod("ip").isEmpty() ) {
            this.m_html.addStringCr( HtmlString.h( 1, "ip=null") );
            return;
        }else{
            this.m_html.addStringCr( HtmlString.h( 1, "ip=" + this.getConfig().getMethod("ip").get() ) );
        }
        if ( this.m_whois == null ) return;
        BSSForm f = BSSForm.newForm();
        f.tableTop("whois_table");
        f.tableBodyTop();
        if ( this.m_whois.getResult().containsKey( "sp_cidr" ) ){
            f.tableRowTop();
            f.tableTh( "CIDR" );
            f.tableTd( String.join(",", this.m_whois.getResult().get( "sp_cidr" ) ) );
            f.tableRowBtm();
        }
        if ( this.m_whois.getResult().containsKey( "sp_country" ) ){
            f.tableRowTop();
            f.tableTh( "Country" );
            f.tableTd( String.join(",", this.m_whois.getResult().get( "sp_country" ) ) );
            f.tableRowBtm();
        }
        if ( this.m_whois.getResult().containsKey( "sp_organization" ) ){
            f.tableRowTop();
            f.tableTh( "Organization" );
            f.tableTd( String.join(",", this.m_whois.getResult().get( "sp_organization" ) ) );
            f.tableRowBtm();
        }
        f.tableBodyBtm();
        f.tableBtm();
        this.m_html.addString( f.toString() );

        f = BSSForm.newForm();
        f.tableTop("whois_table");

        f.tableHeadTop();
        f.tableRowTh("Whois Result");
        f.tableHeadBtm();

        f.tableBodyTop();
        f.tableRowTd( this.m_whois.getWhoisResult() );
        f.tableBodyBtm();

        f.tableHeadTop();
        f.tableRowTh("IANA Result");
        f.tableHeadBtm();

        f.tableBodyTop();
        f.tableRowTd( this.m_whois.getIanaResult() );
        f.tableBodyBtm();

        f.tableBtm();
        this.m_html.addString( f.toString() );
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
