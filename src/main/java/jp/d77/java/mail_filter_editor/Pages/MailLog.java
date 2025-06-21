package jp.d77.java.mail_filter_editor.Pages;

import java.time.LocalDate;

import jp.d77.java.mail_filter_editor.BasicIO.BSOpts;
import jp.d77.java.mail_filter_editor.BasicIO.BSSForm;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNums;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.Datas.MailLogData;
import jp.d77.java.mail_filter_editor.Datas.MailLogData.LogLine;
import jp.d77.java.mail_filter_editor.Datas.MailLogs;

public class MailLog extends AbstractWebPage implements InterfaceWebPage{
    private MailLogs m_datas;
    private String m_detail_ymd;
    private String m_detail_idx;

    public MailLog(WebConfig cfg) {
        super(cfg);
    }

    /**
     * 1:init
     */
    @Override
    public void init() {
        this.m_config.setPageTitle( this.m_config.getPageTitle() + " - MailLog" );
    }

    /**
     * 2:load
     */
    @Override
    public void load() {
        if ( this.m_config.getMethod( "mode" ).orElse( "").equals( "detail") ){
            this.m_detail_ymd = this.m_config.getMethod( "edit_ymd" ).orElse( "");
            LocalDate ymd = ToolDate.YMD2LocalDate( this.m_detail_ymd ).orElse( null );
            this.m_detail_idx = this.m_config.getMethod( "edit_idx" ).orElse( "");
            if ( ymd == null ) return;

            this.m_datas = new MailLogs( this.m_config );
            this.m_datas.load( ymd, ToolNums.Str2Int( this.m_detail_idx ).orElse( -1 ) );

            this.m_config.alertInfo.addStringBr( ymd + "のid=" + this.m_detail_idx + "の詳細を表示します。" );

        }else{
            // 表示範囲終
            LocalDate endDate =
                ToolDate.YMD2LocalDate(
                    this.m_config.getMethod( "edit_date" ).orElse( 
                        ToolDate.Fromat( LocalDate.now(), "uuuu-MM-dd")
                    )
                ).orElse( LocalDate.now() );
            
            // 表示範囲始
            LocalDate startDate = endDate.plusDays( Integer.parseInt( this.m_config.getMethod( "edit_days" ).orElse("7") ) * (-1) );

            // 初期化～読み込み
            this.m_datas = new MailLogs( this.m_config );
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
            if ( ! result ) {
                this.m_datas = null;    // 読み込み失敗
                return;
            }

            this.m_config.alertInfo.addStringBr( startDate + "から" + endDate + "まで表示します。" );
        }
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
        if ( this.m_config.getMethod( "mode" ).orElse( "").equals( "detail") ){
            this.dispDetailTable();
        }else{
            this.dispTable();
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

    private void dispDetailTable(){
        if ( this.m_datas == null ) return;
        if ( this.m_detail_ymd == null ) return;
        if ( this.m_detail_idx == null ) return;

        if ( this.m_datas.getData( this.m_detail_ymd ).containsKey( ToolNums.Str2Int( this.m_detail_idx ).orElse( -1 ) ) == false ) return;
        MailLogData md = this.m_datas.getData( this.m_detail_ymd ).get( ToolNums.Str2Int( this.m_detail_idx ).orElse( -1 ) );

        BSSForm f = BSSForm.newForm();
        f.tableTop(
            new BSOpts()
                .id( "mfe1-table")
                .fclass("table table-bordered table-striped")
                .border("1")
            );

        // Table Header
        f.tableHeadTop();
        f.tableRowTh( "Prog", "Name", "Value" );
        f.tableHeadBtm();

        f.tableBodyTop();

        for ( String prog: md.getValues().keySet() ){
            for ( String name: md.getValues().get( prog ).keySet() ){
                f.tableRowTop();

                // Prog
                f.tableTd( prog );

                // name
                f.tableTd( name );

                // value
                f.tableTd( md.getValues().get( prog ).get( name ), "style=\"max-height: 4.5em; overflow-y: auto; line-height: 1.5em; display: block;\"" );

                f.tableRowBtm();
            }
        }

        f.tableBodyBtm();
        f.tableBtm();

        f.tableTop(
            new BSOpts()
                .id( "mfe-table")
                .fclass("table table-bordered table-striped")
                .border("1")
            );

            // Table Header
        f.tableHeadTop();
        f.tableRowTh( "Time", "Prog", "Log" );
        f.tableHeadBtm();

        f.tableBodyTop();

        for ( LogLine ll: md.getLogs() ){
            f.tableRowTop();

            // Time
            f.tableTd( ll.m_time + "" );

            // prog
            f.tableTd( ll.m_prog );

            // Log
            f.tableTd( ll.m_log );
            
            f.tableRowBtm();
        }
        f.tableBodyBtm();
        f.tableBtm();
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
        f.tableRowTh( "DateTime", "SID", "SEC", "Logs", "IP", "From", "To", "Result" );
        f.tableHeadBtm();

        f.tableBodyTop();

        for ( String YMD: this.m_datas.getYmdList(null) ){
            for ( int idx: this.m_datas.getData( YMD ).keySet() ){
                MailLogData md = this.m_datas.getData( YMD ).get( idx );
                LocalDate date = ToolDate.YMD2LocalDate(YMD).get();

                f.tableRowTop();

                // Date
                //f.tableTd( YMD );

                // Time
                f.tableTd( date + " " + md.getTimeRange() );

                // SID
                f.tableTdHtml( "<A Href=\"" + this.m_config.Uri + "?mode=detail&edit_ymd=" + YMD + "&edit_idx=" + idx + "\" target=\"_blank\">" + idx + "</A>" );


                // Sec
                f.tableTd( md.getSec() + "s" );

                // Logs
                f.tableTd( md.getLogs().size() + "" );

                // IP
                f.tableTd( md.getIp().orElse( "-" ) );

                // From
                f.tableTd( md.getFrom().orElse( "-" ) );

                // To
                f.tableTdHtml( String.join( "\n", md.getTo() ) );

                // Result
                f.tableTdHtml( String.join( "\n", md.getResult() ) );

                f.tableRowBtm();
            }
        }
        f.tableBodyBtm();
        f.tableBtm();
        this.m_html.addString( f.toString() );
    }
}
