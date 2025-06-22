package jp.d77.java.mail_filter_editor.Pages;

import java.time.LocalDate;

import jp.d77.java.mail_filter_editor.BasicIO.BSOpts;
import jp.d77.java.mail_filter_editor.BasicIO.BSSForm;
import jp.d77.java.mail_filter_editor.Datas.BlockData;
import jp.d77.java.mail_filter_editor.Datas.BlockedDatas;
import jp.d77.java.mail_filter_editor.Datas.IptablesLog;
import jp.d77.java.mail_filter_editor.Datas.IptablesLog.IptablesLogData;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNet;

public class WebTop extends AbstractWebPage implements InterfaceWebPage{
    private BlockedDatas    m_datas;
    private IptablesLog     m_iptables_log;

    public WebTop(WebConfig cfg) {
        super(cfg);
    }

    /**
     * 1:init
     */
    @Override
    public void init() {
        this.m_config.setPageTitle( this.m_config.getPageTitle() + " - Top" );
        this.m_config.alertInfo.addStringBr( "(W)...whois検索" );
        this.m_config.alertInfo.addStringBr( "(S)...subnet一覧表示" );
        this.m_config.alertInfo.addStringBr( "450...Sender address rejected: Domain not found" );
        this.m_config.alertInfo.addStringBr( "550...User unknown" );
    }

    /**
     * 2:load
     */
    @Override
    public void load() {
        // 表示範囲終
        LocalDate endDate =
            ToolDate.YMD2LocalDate(
                this.m_config.getMethod( "edit_date" ).orElse( 
                    ToolDate.Fromat( LocalDate.now(), "uuuu-MM-dd").orElse( "-")
                )
             ).orElse( LocalDate.now() );
        
        // 表示範囲始
        LocalDate startDate = endDate.plusDays( Integer.parseInt( this.m_config.getMethod( "edit_days" ).orElse("7") ) * (-1) );

        // 初期化～読み込み
        this.m_datas = new BlockedDatas( this.m_config );
        boolean result = false;
        this.m_datas.init();

        LocalDate date = startDate;
        int cnt = 0;
        while ( true ) {
            if ( this.m_datas.load( date ) ) result = true;
            if ( ! date.isBefore(endDate) ) break;
            date = date.plusDays(1);
            if ( cnt >= 30 ) break;
            cnt++;
        }
        if ( ! result ) this.m_datas = null;    // 読み込み失敗
        else{
            // スコア計算
            this.m_datas.createScore();
        }

        if ( this.m_datas != null ){
            // iptables読み込み
            this.m_iptables_log = new IptablesLog( this.m_config );
            if ( this.m_iptables_log.load() == false ){
                this.m_iptables_log = null;
            }
        }

        // block状態確認
        for ( String idx: this.m_datas.getDatas().keySet() ){
            BlockData bd = this.m_datas.getDatas().get( idx );
            if ( bd.getIp().isEmpty() ) continue;
            int a = this.m_iptables_log.ClassA( bd.getIp().get() );

            for ( IptablesLogData ipt: this.m_iptables_log.getDatas() ){
                if ( a != ipt.getClassA() ) continue;
                if ( ToolNet.isWithinCIDR( ipt.getCidr().orElse("-") , bd.getIp().get() ).orElse("-").equals( ipt.getCidr().orElse("-") ) ){
                    bd.m_blocked.add( ipt.getCidr().orElse( "-" ) + "<BR>" + ipt.getCode().orElse("-") );
                }
            }
        }
        this.m_config.alertInfo.addStringBr( startDate + "から" + endDate + "まで表示します。" );
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
                .value( this.m_config.getMethod("edit_date").orElse( ToolDate.Fromat( LocalDate.now(), "uuuu-MM-dd").orElse("-") ))
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
        f.tableTh( "C" );
        f.tableTh( "R" );
        f.tableTh( "O" );
        //f.tableTh( "Score" );
        f.tableTh( "From->To" );
        f.tableTh( "Org" );
        f.tableRowBtm();
        f.tableHeadBtm();

        f.tableBodyTop();

        String col1day = ToolDate.Fromat( LocalDate.now(), "uuuuMMdd" ).orElse( "-" );
        String col2day = ToolDate.Fromat( LocalDate.now().plusDays( -1 ), "uuuuMMdd" ).orElse( "-" );
        String col3day = ToolDate.Fromat( LocalDate.now().plusDays( -2 ), "uuuuMMdd" ).orElse( "-" );

        for ( String idx: this.m_datas.getDatas().keySet() ){
            BlockData bd = this.m_datas.getDatas().get( idx );
            String add_opt = "";

            if ( bd.matchBlockCondition( "black_list" ) ){
                add_opt = "style=\"background-color: #cccccc;\"";
            }

            f.tableRowTop();

            // Blocked
            if ( bd.m_blocked.size() <= 0 ){
                f.tableTd( "-", add_opt );
            }else{
                f.tableTdHtml( String.join("<BR>", bd.m_blocked ), add_opt );
            }

            // CC
            f.tableTd( bd.getCc().orElse("-"), add_opt );

            // Range
            f.tableTdHtml( SharedWebLib.linkBlockEditor( bd.getRange().orElse("-"), bd.getCc().orElse(""), bd.getOrg().orElse("") ), add_opt );

            // IP
            f.tableTdHtml( SharedWebLib.linkBlockEditor( bd.getIp().orElse(""), bd.getCc().orElse(""), bd.getOrg().orElse("") ), "nowrap", add_opt );

            // Code
            f.tableTdHtml( String.join("<BR>", bd.getErrorCodes() ), add_opt );

            // Date
            if ( bd.getDate().orElse( "-" ).equals( col1day ) ){
                f.tableTd( bd.getDate().orElse( "-" ), "style=\"background-color: #ffff00;\"" );
            }else if ( bd.getDate().orElse( "-" ).equals( col2day ) ){
                f.tableTd( bd.getDate().orElse( "-" ), "style=\"background-color: #ffff88;\"" );
            }else if ( bd.getDate().orElse( "-" ).equals( col3day ) ){
                f.tableTd( bd.getDate().orElse( "-" ), "style=\"background-color: #ffffcc;\"" );
            }else{
                f.tableTd( bd.getDate().orElse( "-" ), add_opt );
            }

            // C
            f.tableTd( bd.getCount() + "", add_opt );

            // R
            f.tableTd( this.m_datas.getCountRange( bd.getRange().orElse("-") ) + "", add_opt );

            // R
            f.tableTd( this.m_datas.getCountOrg( bd.getOrg().orElse("-") ) + "", add_opt );

            // Score
//            f.tableTd( this.m_datas.getScore( bd.getIp().get(), bd.getRange().orElse("-"), bd.getOrg().orElse("-") ) );

            // From To
            f.tableTd( String.join(" ", bd.getFromTo() ), add_opt );

            // Org
            f.tableTd( bd.getOrg().orElse("-"), add_opt );

            f.tableRowBtm();
        }
        f.tableBodyBtm();
        f.tableBtm();
        this.m_html.addString( f.toString() );
    }
}
