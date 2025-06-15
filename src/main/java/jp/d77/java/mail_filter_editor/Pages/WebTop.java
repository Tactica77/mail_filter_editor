package jp.d77.java.mail_filter_editor.Pages;

import java.time.LocalDate;

import jp.d77.java.mail_filter_editor.BasicIO.BSOpts;
import jp.d77.java.mail_filter_editor.BasicIO.BSSForm;
import jp.d77.java.mail_filter_editor.Datas.BlockData;
import jp.d77.java.mail_filter_editor.Datas.BlockedDatas;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;

public class WebTop extends AbstractWebPage implements InterfaceWebPage{
    private BlockedDatas m_datas;

    public WebTop(WebConfig cfg) {
        super(cfg);
    }

    /**
     * 1:init
     */
    @Override
    public void init() {
        this.m_config.setPageTitle( this.m_config.getPageTitle() + " - Top" );
    }

    /**
     * 2:load
     */
    @Override
    public void load() {
        LocalDate endDate =
            ToolDate.YMD2LocalDate(
                this.m_config.getMethod( "edit_date" ).orElse( 
                    ToolDate.Fromat( LocalDate.now(), "uuuu-MM-dd")
                )
             ).orElse( LocalDate.now() );
        
        LocalDate startDate = endDate.plusDays( Integer.parseInt( this.m_config.getMethod( "edit_days" ).orElse("7") ) * (-1) );
        this.m_datas = new BlockedDatas( this.m_config );

        boolean result = false;
        this.m_datas.init();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            if ( this.m_datas.load( date ) ) result = true;
        }
        this.m_datas.createData();

        if ( ! result ){
            this.m_datas = null;
        }
        this.m_config.alertInfo.addStringBr( startDate + "から" + endDate + "まで表示します。" );
        this.m_config.alertInfo.addStringBr( "(W)...whois検索" );
        this.m_config.alertInfo.addStringBr( "(S)...subnet一覧表示" );
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

        this.displayFormTop();
        this.displayForm();
        this.dispTable();
        this.displayFormBtm();
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

    private void displayForm(){
        BSSForm f = BSSForm.newForm();
        // 日付
        f.divRowTop();

        f.divTop( 2, "text-right")
            .formLabel( BSOpts.init("label", "日付設定") )
        .divBtm(2);

        f.divTop(2)
            .formInput(
                BSOpts.init()
                .type( "date")
                .name( "edit_date")
                .value( this.m_config.getMethod("edit_date").orElse( ToolDate.Fromat( LocalDate.now(), "uuuu-MM-dd") ))
            )
        .divBtm(2);

        f.divTop(2, "text-right")
            .formLabel( BSOpts.init("label", "遡る日数") );
        f.divBtm(2);

        f.divTop(2)
            .formInput(
                BSOpts.init()
                .type( "number")
                .name( "edit_days")
                .value( this.m_config.getMethod("edit_days").orElse( "7" ))
            )
        .divBtm(2);

        f.divTop(4)
        .divBtm(4);

        f.divRowBtm();

        // Submit行
        f.divRowTop();

        f.divTop(2)
            .formSubmit(
                BSOpts.init("name", "submit_list" )
                .label("表示")
                .value("DISP")
                )
        .divBtm(2);

        f.divTop(10);
        f.divBtm(10);

        f.divRowBtm();
        this.m_html.addString( f.toString() );
    }

    private void dispTable(){
        if ( this.m_datas == null ) return;

        BSSForm f = BSSForm.newForm();
        f.tableTop(
            new BSOpts()
                .id( "mfe-table")
                .fclass("table table-bordered table-striped")
                .border("1")
            );

        // Table Header
        f.tableHeadTop();
        f.tableRowTop();
        f.tableTh( "Blocked" );
        f.tableTh( "CC" );
        f.tableTh( "Range" );
        f.tableTh( "IP" );
        f.tableTh( "Code" );
        f.tableTh( "Date" );
        f.tableTh( "Cnt" );
        f.tableTh( "Score" );
        f.tableTh( "From->To" );
        f.tableTh( "Org" );
        f.tableRowBtm();
        f.tableHeadBtm();

        f.tableBodyTop();
        String link;

        String col1day = ToolDate.Fromat( LocalDate.now(), "MM-dd" );
        String col2day = ToolDate.Fromat( LocalDate.now().plusDays( -1 ), "MM-dd" );
        String col3day = ToolDate.Fromat( LocalDate.now().plusDays( -2 ), "MM-dd" );

        for ( String idx: this.m_datas.getDatas().keySet() ){
            BlockData bd = this.m_datas.getDatas().get( idx );
            f.tableRowTop();

            // Blocked
            f.tableTd( "-" );

            // CC
            f.tableTd( bd.getCc().orElse("-") );

            // Range
            f.tableTd( bd.getRange().orElse("-") );

            // IP
            link = "";
            if ( bd.getIp().isPresent() ){
                // whois link
                link = "<A Href=\"/whois?ip=" + bd.getIp().get() + "\" target=\"_blank\">(W)</A>"
                + "<A Href=\"/subnets?ip=" + bd.getIp().get() + "\" target=\"_blank\">(S)</A>";
            }
            f.tableTdHtml( bd.getIp().orElse("-") + link );

            // Code
            f.tableTd( String.join(",", bd.getErrorCodes() ) );

            // Date
            if ( bd.getDate().orElse( "-" ).equals( col1day ) ){
                f.tableTd( bd.getDate().orElse( "-" ), " style=\"background-color: #ffff00;\"" );
            }else if ( bd.getDate().orElse( "-" ).equals( col2day ) ){
                f.tableTd( bd.getDate().orElse( "-" ), " style=\"background-color: #ffff88;\"" );
            }else if ( bd.getDate().orElse( "-" ).equals( col3day ) ){
                f.tableTd( bd.getDate().orElse( "-" ), " style=\"background-color: #ffffcc;\"" );
            }else{
                f.tableTd( bd.getDate().orElse( "-" ) );
            }

            // Cnt
            f.tableTd( bd.getCount() + "" );

            // Score
            f.tableTd( this.m_datas.getScore( bd.getIp().get(), bd.getRange().orElse("-"), bd.getOrg().orElse("-") ) );

            // From To
            f.tableTd( String.join(" ", bd.getFromTo() ) );

            // Org
            f.tableTd( bd.getOrg().orElse("-") );

            f.tableRowBtm();
        }
        f.tableBodyBtm();
        f.tableBtm();
        this.m_html.addString( f.toString() );
    }
}
