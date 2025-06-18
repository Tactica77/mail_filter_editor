package jp.d77.java.mail_filter_editor.Pages;

import jp.d77.java.mail_filter_editor.BasicIO.BSOpts;
import jp.d77.java.mail_filter_editor.BasicIO.BSSForm;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.Datas.BlackList;
import jp.d77.java.mail_filter_editor.Datas.BlackListData;

public class WebBlockEditor extends AbstractWebPage implements InterfaceWebPage{
    private BlackList   m_black_list;

    public WebBlockEditor(WebConfig cfg) {
        super(cfg);
    }

    /**
     * 1:init
     */
    @Override
    public void init() {
        this.m_config.setPageTitle( this.m_config.getPageTitle() + " - BlockEditor" );
    }

    /**
     * 2:load
     */
    @Override
    public void load() {
        this.m_black_list = new BlackList( this.m_config );
        if ( this.m_black_list.load() == false ){
            this.m_black_list = null;
        }
    }

    /**
     * 3:post_save_reload
     */
    @Override
    public void post_save_reload() {
        if ( this.m_config.getMethod( "submit_disable" ).isPresent() ){
            for ( String v: this.m_config.getMethodLists( "edit_select_cidr" ) ){
                this.m_black_list.disable( v );
            }
            if ( this.m_config.getMethod( "edit_select_cidr" ).isPresent() ){
                this.m_black_list.disable( this.m_config.getMethod( "edit_select_cidr" ).get() );
            }
        }else if ( this.m_config.getMethod( "submit_enable" ).isPresent() ){
            for ( String v: this.m_config.getMethodLists( "edit_select_cidr" ) ){
                this.m_black_list.enable( v );
            }
            if ( this.m_config.getMethod( "edit_select_cidr" ).isPresent() ){
                this.m_black_list.enable( this.m_config.getMethod( "edit_select_cidr" ).get() );
            }
        }else if ( this.m_config.getMethod( "submit_delete" ).isPresent() ){
            for ( String v: this.m_config.getMethodLists( "edit_select_cidr" ) ){
                this.m_black_list.remove( v );
            }
            if ( this.m_config.getMethod( "edit_select_cidr" ).isPresent() ){
                this.m_black_list.remove( this.m_config.getMethod( "edit_select_cidr" ).get() );
            }
        }
        this.m_black_list.save();
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
        this.dispTable();
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

    private void dispTable(){
        if ( this.m_black_list == null ) return;
                BSSForm f = BSSForm.newForm();
        f.formTop( this.m_config.Uri, false);
        f.tableTop(
            new BSOpts()
                .id( "mfe-table")
                .fclass("table table-bordered table-striped")
                .border("1")
            );
        
        f.divRowTop();

        f.divTop(2 );
        f.formSubmit( BSOpts.init( "name", "submit_disable").value( "DISABLE" ).label( "DISABLE" ) );
        f.divBtm(2 );

        f.divTop(2 );
        f.formSubmit( BSOpts.init( "name", "submit_enable").value( "ENABLE" ).label( "ENABLE" ) );
        f.divBtm(2 );

        f.divTop(2 );
        f.formSubmit( BSOpts.init( "name", "submit_delete").value( "DELETE" ).label( "DELETE" ) );
        f.divBtm(2 );

        f.divTop(6 );
        f.divBtm(6 );
        f.divRowBtm();

        f.tableHeadTop();
        f.tableRowTh( "CMD", "YMD", "CC", "CIDR", "Organization");
        f.tableHeadBtm();
     
        f.tableBodyTop();

        for ( BlackListData bld: this.m_black_list.getDatas() ){
            String opt = "";
            if ( ! bld.isEnable() ) opt = " style=\"background-color: #CCCCCC;\"";

            f.tableRowTop();

            // Cmd
            f.tableTdHtml( 
                BSSForm.newForm().formInput(
                        BSOpts
                        .init( "type", "checkbox")
                        .set("name", "edit_select_cidr" )
                        .set("value", bld.getCidr())
                ).toString()
                , opt );

            // YMD
            f.tableTd( bld.getAddDate().orElse( "-" ), opt );

            // Cc
            f.tableTd( bld.getCountryCode().orElse( "-" ), opt );

            // CIDR
            f.tableTd( bld.getCidr(), opt );

            // Org
            f.tableTd( bld.getOrg().orElse( "-" ), opt );

            f.tableRowBtm();
        }

        f.tableBodyBtm();
        f.tableBtm();
        f.formBtm();

        this.m_html.addString( f.toString() );
    }
}
