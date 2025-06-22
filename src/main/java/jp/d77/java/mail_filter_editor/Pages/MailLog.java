package jp.d77.java.mail_filter_editor.Pages;

import java.time.LocalDate;
import java.util.Collection;
import java.util.TreeMap;

import jp.d77.java.mail_filter_editor.BasicIO.BSOpts;
import jp.d77.java.mail_filter_editor.BasicIO.BSSForm;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.HtmlGraph;
import jp.d77.java.mail_filter_editor.BasicIO.HtmlString;
import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNet;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNums;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.BasicIO.HtmlGraph.GRAPH_TYPE;
import jp.d77.java.mail_filter_editor.Datas.IptablesLog;
import jp.d77.java.mail_filter_editor.Datas.MailLogData;
import jp.d77.java.mail_filter_editor.Datas.MailLogData.LogLine;
import jp.d77.java.mail_filter_editor.Datas.MailLogList;
import jp.d77.java.mail_filter_editor.Datas.IptablesLog.IptablesLogData;

public class MailLog extends AbstractWebPage implements InterfaceWebPage{
    private MailLogList m_log_list;
    private String      m_detail_ymd;
    private String      m_detail_idx;
    private IptablesLog m_iptables_log;

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
            // 詳細モード
            this.m_detail_ymd = this.m_config.getMethod( "edit_ymd" ).orElse( "");
            LocalDate ymd = ToolDate.YMD2LocalDate( this.m_detail_ymd ).orElse( null );
            this.m_detail_idx = this.m_config.getMethod( "edit_idx" ).orElse( "");
            if ( ymd == null ) return;

            this.m_log_list = new MailLogList( this.m_config );
            this.m_log_list.load( ymd, ToolNums.Str2Int( this.m_detail_idx ).orElse( -1 ) );

            this.m_config.alertInfo.addStringBr( ymd + "のid=" + this.m_detail_idx + "の詳細を表示します。" );

        }else{
            // 表示範囲終
            LocalDate endDate =
                ToolDate.YMD2LocalDate(
                    this.m_config.getMethod( "edit_date" ).orElse( 
                        ToolDate.Fromat( LocalDate.now(), "uuuu-MM-dd").orElse("")
                    )
                ).orElse( LocalDate.now() );
            
            // 表示範囲始
            LocalDate startDate = endDate.plusDays( Integer.parseInt( this.m_config.getMethod( "edit_days" ).orElse("7") ) * (-1) );

            // 初期化～読み込み
            this.m_log_list = new MailLogList( this.m_config );
            boolean result = false;
            this.m_log_list.init();

            LocalDate date = startDate;
            int cnt = 0;
            while ( true ) {
                if ( this.m_log_list.load( date ) ) result = true;
                if ( ! date.isBefore(endDate) ) break;
                date = date.plusDays(1);
                if ( cnt >= 30 ) break;
                cnt++;
            }
            if ( ! result ) {
                this.m_log_list = null;    // 読み込み失敗
                return;
            }
            if ( this.m_config.getMethod( "mode" ).orElse( "").equals( "ipcount") ){
                // iptables読み込み
                this.m_iptables_log = new IptablesLog( this.m_config );
                if ( this.m_iptables_log.load() == false ){
                    this.m_iptables_log = null;
                }
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
        }else if ( this.m_config.getMethod( "mode" ).orElse( "").equals( "ipcount") ){
            this.m_html.addStringCr( HtmlGraph.getHeaderScript() );
            this.dispIpList();
        }else{
            this.displayForm();
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
        if ( this.m_log_list == null ) return;
        if ( this.m_detail_ymd == null ) return;
        if ( this.m_detail_idx == null ) return;

        if ( this.m_log_list.getData( this.m_detail_ymd ).containsKey( ToolNums.Str2Int( this.m_detail_idx ).orElse( -1 ) ) == false ) return;
        MailLogData md = this.m_log_list.getData( this.m_detail_ymd ).get( ToolNums.Str2Int( this.m_detail_idx ).orElse( -1 ) );

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
            f.tableTd( ToolDate.YMD2LocalDate( this.m_detail_ymd ).get() + " " + ll.m_time, "nowrap" );

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
                .value( this.m_config.getMethod("edit_date").orElse( ToolDate.Fromat( LocalDate.now(), "uuuu-MM-dd").orElse("") ))
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

    private void dispIpList(){
        if ( this.m_log_list == null ) return;
        TreeMap<Integer,String> rank = new TreeMap<Integer,String>();
        HtmlGraph graph = new HtmlGraph().setGraphTitle( "IP COUNT" );

        for( String ip: this.m_log_list.getIpCnt().keySet() ){
            if ( ip.equals("127.0.0.1") ) continue;

            if ( this.m_log_list.getIpCnt().get( ip ).getCount() > 3 ){
                graph.getDbf().setProp( ip, "stack_1", GRAPH_TYPE.BAR );
                for ( String YMD: this.m_log_list.getIpCnt().get( ip ).getDailyCount().keySet() ){
                    graph.getDbf().set( YMD, ip, Float.parseFloat( this.m_log_list.getIpCnt().get( ip ).getDailyCount().get( YMD ) + "" ) );
                }
            }

            if ( this.m_log_list.getIpCnt().get( ip ).isWhoisLoaded() ) continue;
            rank.put( this.m_log_list.getIpCnt().get( ip ).getCount(), ip );
        }

        Collection<String> valuesDesc = rank.descendingMap().values();
        int cnt = 3;
        for (String ip : valuesDesc) {
            this.m_log_list.getIpCnt().get( ip ).setWhois();
            cnt --;
            if ( cnt <= 0 ) break;
        }        
        BSSForm f = BSSForm.newForm();

        f.divRowTop();
        f.addString( graph.draw_graph( "mfe" ) );
        f.divRowBtm();

        f.tableTop(
            new BSOpts()
                .id( "mfe-table")
                .fclass("table table-bordered table-striped")
                .border("1")
            );

        // Table Header
        f.tableHeadTop();
        f.tableRowTh( "IP", "Blocked", "Cnt", "CC", "Range", "Organization" );
        f.tableHeadBtm();

        f.tableBodyTop();
        for( String ip: this.m_log_list.getIpCnt().keySet() ){
            //if ( this.m_log_list.getIpCnt().get( ip ).getCount() <= 1 ) continue;
            if ( ip.equals("127.0.0.1") ) continue;

            f.tableRowTop();
            String cc   = this.m_log_list.getIpCnt().get( ip ).getCc().orElse("-");
            String cidr = this.m_log_list.getIpCnt().get( ip ).getCidr().orElse("-");
            String org  = this.m_log_list.getIpCnt().get( ip ).getOrg().orElse("-");

            // IP
            f.tableTdHtml( SharedWebLib.linkBlockEditor(ip, cc, org) );

            // Blocked
            if ( this.m_iptables_log != null ){
                String a[] = ip.split(".");
                int class_a = 0;
                if ( a.length > 0 ) class_a = ToolNums.Str2Int( a[0] ).orElse( 0 );
                boolean m = false;
                for( IptablesLogData ild: this.m_iptables_log.getDatas() ){
                    if ( ild.getCidr().isEmpty() ) continue;
                    if ( ild.getClassA() != class_a ) continue;
                    if ( ToolNet.isWithinCIDR( ip, ild.getCidr().get() ).orElse("").equals( ip )  ){
                        f.tableTd( ild.getCidr().get() );
                        m = true;
                        break;
                    }
                }
                if ( ! m ) f.tableTd( "-" );
            }else{
                f.tableTd( "-" );
            }

            // Count
            f.tableTd( this.m_log_list.getIpCnt().get( ip ).getCount() + "" );

            // CC
            f.tableTd( cc );

            // Range
            f.tableTdHtml( SharedWebLib.linkBlockEditor(cidr, cc, org) );

            // Organization
            f.tableTd( org );

            f.tableRowBtm();
        }
        f.tableBodyBtm();

        f.tableBtm();
        this.m_html.addString( f.toString() );
    }

    private void dispTable(){
        if ( this.m_log_list == null ) return;

        BSSForm f = BSSForm.newForm();
        
        f.tableTop(
            new BSOpts()
                .id( "mfe-table")
                .fclass("table table-bordered table-striped")
                .border("1")
            );

        // Table Header
        f.tableHeadTop();
        f.tableRowTh( "DateTime", "SID", "SEC", "Logs", "IP", "Result", "From", "To" );
        f.tableHeadBtm();

        f.tableBodyTop();

        for ( String YMD: this.m_log_list.getYmdList(null) ){
            for ( int idx: this.m_log_list.getData( YMD ).keySet() ){
                MailLogData md = this.m_log_list.getData( YMD ).get( idx );
                LocalDate date = ToolDate.YMD2LocalDate(YMD).get();

                f.tableRowTop();

                // Date
                //f.tableTd( YMD );

                // Time
                f.tableTd( date + " " + md.getTimeRange(), "nowrap" );

                // SID
                f.tableTdHtml( "<A Href=\"" + this.m_config.Uri + "?mode=detail&edit_ymd=" + YMD + "&edit_idx=" + idx + "\" target=\"_blank\">" + idx + "</A>" );

                // Sec
                f.tableTd( md.getSec() + "s" );

                // Logs
                f.tableTd( md.getLogs().size() + "" );

                // IP
                f.tableTdHtml( String.join( "\n", SharedWebLib.linkIpBasic( md.getIp().orElse( "-" ) ) ) , "nowrap" );

                // Result
                String v = "";
                for ( String s: md.getResult() ){
                    v += HtmlString.HtmlEscape(s) + "<BR>\n";
                }
                for ( String s: md.getError() ){
                    v += "<Font color=\"RED\"><B>" + HtmlString.HtmlEscape(s) + "</B></Font><BR>\n";
                }
                f.tableTdHtml( v );

                // From
                f.tableTd( md.getFrom().orElse( "-" ) );

                // To
                f.tableTd( md.getTo().orElse( "-" ) );

                f.tableRowBtm();
            }
        }
        f.tableBodyBtm();
        f.tableBtm();
        this.m_html.addString( f.toString() );
    }
}
