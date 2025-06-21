package jp.d77.java.mail_filter_editor.Pages;

import jp.d77.java.mail_filter_editor.BasicIO.BSSForm;
import jp.d77.java.mail_filter_editor.BasicIO.HtmlString;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNet;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.BasicIO.WhoisResult;

public class WebSubnets extends AbstractWebPage implements InterfaceWebPage{

    public WebSubnets(WebConfig cfg) {
        super(cfg);
    }
    /**
     * 1:init
     */
    @Override
    public void init() {
        this.m_config.setPageTitle( this.m_config.getPageTitle() + " - Subnets" );
    }

    /**
     * 2:load
     */
    @Override
    public void load() {
        if ( this.getConfig().getMethod("ip").isEmpty() ) return;
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
        String ip = this.getConfig().getMethod("ip").get();
        BSSForm f = BSSForm.newForm();
        f.tableTop("subnets_table");

        f.tableHeadTop();
        f.tableRowTh("CIDR", "MASK", "START IP", "END IP", "WHOIS RANGE", "CC", "ORG NAME", "RIR");
        f.tableHeadBtm();

        f.tableBodyTop();
        for( int i = 1; i <= 32; i++ ){
            f.tableRowTop();
            // CIDR
            f.tableTd("/" + i );

            // MASK
            f.tableTd( ToolNet.Cidr2MaskString(i) );

            // START IP
            String[] r = ToolNet.calculateRange( ip, i );
            f.tableTd( r[0] );

            // END IP
            f.tableTd( r[1] );

            // WHOIS RANGE
            WhoisResult whois = ToolNet.getWhois( r[0] ).orElse( null );
            String cidr = "-";
            String cc = "-";
            String org = "-";
            String rir = "-";
            if ( whois != null ){
                if ( whois.getResult().containsKey("sp_cidr") ) cidr = whois.getResult().get("sp_cidr").get(0);
                if ( whois.getResult().containsKey("sp_country") ) cc = whois.getResult().get("sp_country").get(0);
                if ( whois.getResult().containsKey("sp_organization") ) org = whois.getResult().get("sp_organization").get(0);
                else if ( whois.getResult().containsKey("sp_organization2") ) org = whois.getResult().get("sp_organization2").get(0);
                else if ( whois.getResult().containsKey("sp_organization3") ) org = whois.getResult().get("sp_organization3").get(0);
                rir = whois.getRIR().orElse("-");
            }

            f.tableTdHtml( SharedWebLib.linkBlockEditor(cidr, cc, org) );

            // CC
            f.tableTd( cc );

            // ORG NAME
            f.tableTd( org );

            // RIR
            f.tableTd( rir );

            f.tableRowBtm();
        }
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
