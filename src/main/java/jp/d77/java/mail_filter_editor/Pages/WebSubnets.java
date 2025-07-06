package jp.d77.java.mail_filter_editor.Pages;

import jp.d77.java.mail_filter_editor.BasicIO.ToolNet;
import jp.d77.java.mail_filter_editor.BasicIO.ToolWhois;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;

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
        this.m_html.addString(BSSForm.getTableHeader( "mfe" ));
    }

    /**
     * 6:displayNavbar
     */
    @Override
    public void displayNavbar() {
        Debugger.TracePrint();
        SharedWebLib.Navbar( this.m_config, this.m_html );    }

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
        String a[] = this.getConfig().getMethod("ip").get().split( "/");
        String ip = a[0];
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
            String cidr = "-";
            String cc = "-";
            String org = "-";
            String rir = "-";
            ToolWhois.WhoisData wd = ToolWhois.get( r[0], true ).orElse(null);
            if ( wd != null ){
                if ( wd.getCidr().isPresent() ) cidr = wd.getCidr().get().get(0);
                cc = wd.getCc().orElse( "-" );
                org = wd.getOrg().orElse( "-" );
                rir = wd.getServer().orElse( "-" );
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
