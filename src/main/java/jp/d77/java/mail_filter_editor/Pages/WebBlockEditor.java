package jp.d77.java.mail_filter_editor.Pages;

import java.time.LocalDate;

import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.Datas.BlackList;
import jp.d77.java.mail_filter_editor.Datas.BlackListData;
import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ToolDate;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;

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
        }else if ( this.m_config.getMethod( "submit_new_add" ).isPresent() ){
            String ymd, cc, cidr, org;
            if ( this.m_config.getMethod( "edit_new_ymd" ).isPresent() ) ymd = this.m_config.getMethod( "edit_new_ymd" ).get();
            else ymd = ToolDate.Fromat( LocalDate.now() , "uuuuMMdd" ).orElse( "-" );
            if ( this.m_config.getMethod( "edit_new_cc" ).isPresent() ) cc = this.m_config.getMethod( "edit_new_cc" ).get();
            else cc = "";
            if ( this.m_config.getMethod( "edit_new_cidr" ).isPresent() ) cidr = this.m_config.getMethod( "edit_new_cidr" ).get();
            else cidr = "";
            if ( this.m_config.getMethod( "edit_new_org" ).isPresent() ) org = this.m_config.getMethod( "edit_new_org" ).get();
            else org = "";
            if ( this.m_black_list.findBlackListData(cidr).isPresent() ){
                // 登録済み
                this.m_config.alertError.addStringBr( "登録済みです:" + cidr );
            }else{
                BlackListData bld = this.m_black_list.getNewData();
                bld.set( cidr, ymd, cc, org );
                this.m_black_list.setUpdate();
                this.m_config.alertInfo.addStringBr( "登録しました:" + cidr );
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

        // Top
        f.formTop( this.m_config.Uri, false);
        
        // Command buttons
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

        // New Form
        f.divRowTop();
        f.divTop(2 );   f.formLabel( BSOpts.init( "label", "YYYYMMDD" ) );  f.divBtm(2 );
        f.divTop(2 );   f.formLabel( BSOpts.init( "label", "CC" ) );  f.divBtm(2 );
        f.divTop(2 );   f.formLabel( BSOpts.init( "label", "CIDR" ) );  f.divBtm(2 );
        f.divTop(2 );   f.formLabel( BSOpts.init( "label", "Organization" ) );  f.divBtm(2 );
        f.divTop(4 );   f.formLabel( BSOpts.init( "label", "ADD" ) );  f.divBtm(4 );
        f.divRowBtm();

        String ymd, cc, cidr, org;
        if ( this.m_config.getMethod( "edit_new_ymd" ).isPresent() ) ymd = this.m_config.getMethod( "edit_new_ymd" ).get();
        else ymd = ToolDate.Fromat( LocalDate.now() , "uuuuMMdd" ).orElse( "-" );
        if ( this.m_config.getMethod( "edit_new_cc" ).isPresent() ) cc = this.m_config.getMethod( "edit_new_cc" ).get();
        else cc = "";
        if ( this.m_config.getMethod( "edit_new_cidr" ).isPresent() ) cidr = this.m_config.getMethod( "edit_new_cidr" ).get();
        else cidr = "";
        if ( this.m_config.getMethod( "edit_new_org" ).isPresent() ) org = this.m_config.getMethod( "edit_new_org" ).get();
        else org = "";

        f.divRowTop();

        // YYYYMMDD
        f.divTop(2 );
        f.formInput(
            BSOpts.init( "type", "text" )
                .set( "name", "edit_new_ymd")
                .value( ymd )
            );
        f.divBtm(2 );

        // CC
        f.divTop(2 );
        f.formInput(
            BSOpts.init( "type", "text" )
                .set( "name", "edit_new_cc")
                .set( "value", cc)
            );
        f.divBtm(2 );

        // CIDR
        f.divTop(2 );
        f.formInput(
            BSOpts.init( "type", "text" )
                .set( "name", "edit_new_cidr")
                .set( "value", cidr)
            );
        f.divBtm(2 );
        
        // Organization
        f.divTop(2 );
        f.formInput(
            BSOpts.init( "type", "text" )
                .set( "name", "edit_new_org")
                .set( "value", org)
            );
        f.divBtm(2 );

        // ADD button
        f.divTop(4 );
        f.formSubmit(
            BSOpts.init( "name", "submit_new_add" )
                .set( "value", "submit_new_add")
                .set("label", "ADD")
            );
        f.divBtm(4 );
        f.divRowBtm();

        f.tableTop(
            new BSOpts()
                .id( "mfe-table")
                .fclass("table table-bordered table-striped")
                .border("1")
            );

        // Table header
        f.tableHeadTop();
        f.tableRowTh( "CMD", "YMD", "CC", "CIDR", "Organization", "Parent");
        f.tableHeadBtm();
     
        // Table body
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
            f.tableTdHtml( HtmlString.HtmlEscape( bld.getCidr() ) + SharedWebLib.linkWhois( bld.getCidr() ) + SharedWebLib.linkSubnets( bld.getCidr() ), opt );

            // Org
            f.tableTd( bld.getOrg().orElse( "-" ), opt );

            // Parent
            f.tableTd( bld.getDuplicateCidr().orElse( "-" ), opt );

            f.tableRowBtm();
        }

        f.tableBodyBtm();
        f.tableBtm();
        f.formBtm();

        this.m_html.addString( f.toString() );
    }
}
