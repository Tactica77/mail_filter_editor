package jp.d77.java.mail_filter_editor.Datas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNums;
import jp.d77.java.mail_filter_editor.BasicIO.ToolWhois;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.Datas.MailLogData.LogLine;

public class MailLogList {
    public class IpCnt{
        private String m_ip = null;
        private int m_cnt = 0;
        private LinkedHashMap<String,Integer> m_dailycnt = new LinkedHashMap<String,Integer>();
        private String m_cc = null;
        private String m_cidr = null;
        private String m_org = null;
        private boolean m_whois_loaded = false;

        public void setIp( String YMD, String ip ){
            this.m_ip = ip;
            this.m_cnt++;
            if ( YMD == null ) return;
            if ( ! this.m_dailycnt.containsKey( YMD ) ) this.m_dailycnt.put( YMD, 0 );
            this.m_dailycnt.put( YMD, this.m_dailycnt.get( YMD ) + 1 );
        }

        public Optional<String> getCc(){ return Optional.ofNullable( this.m_cc ); }
        public Optional<String> getCidr(){ return Optional.ofNullable( this.m_cidr ); }
        public Optional<String> getOrg(){ return Optional.ofNullable( this.m_org ); }
        public boolean isWhoisLoaded() { return this.m_whois_loaded; }
        public int getCount() { return this.m_cnt; }
        public LinkedHashMap<String,Integer> getDailyCount() { return this.m_dailycnt; }

        public void setWhois(){
            ToolWhois.WhoisData wd = ToolWhois.get( this.m_ip, false).orElse( null );
            if ( wd == null ) return;
            if ( wd.getCidr().isPresent() ) this.m_cidr = wd.getCidr().get().get(0);
            if ( wd.getCc().isPresent() ) this.m_cc = wd.getCc().get();
            if ( wd.getOrg().isPresent() ) this.m_org = wd.getOrg().get();
            this.m_whois_loaded = true;
        }
    }

    private WebConfig   m_config;
    private LinkedHashMap<String,LinkedHashMap<Integer,MailLogData>>    m_datas;    /* YMD,no,MailLogData */
    private HashMap<String, IpCnt> m_IpCnt;
    
    public MailLogList( WebConfig cfg ){
        this.m_config = cfg;
        this.init();
    }

    public void init(){
        this.m_datas = new LinkedHashMap<String,LinkedHashMap<Integer,MailLogData>>();
        this.m_IpCnt = new HashMap<String, IpCnt>();
    }

    /***********************************************************************************************/
    // getter
    /***********************************************************************************************/
    public String[] getYmdList( String YMD ){
        String[] r = this.m_datas.keySet().toArray( new String[0] );
        Arrays.sort( r );
        return r;
    }

    public HashMap<Integer,MailLogData> getData( String YMD ){
        return this.m_datas.get( YMD );
    }

    public HashMap<String, IpCnt> getIpCnt(){
        return this.m_IpCnt;
    }
    /***********************************************************************************************/
    // loader
    /***********************************************************************************************/
    public boolean load( LocalDate target_date ){
        return this.load( target_date, -1 );
    }

    public boolean load( LocalDate target_date, Integer idx ){
        String YM = ToolDate.Fromat( target_date, "uuuuMM" ).orElse( null );
        String YMD = ToolDate.Fromat( target_date, "uuuuMMdd" ).orElse( null );
        if ( YM == null || YMD == null ) return false;

        String filename = this.m_config.getDataFilePath() + "/session_logs/" + YM + "/session_logs_" + YMD + ".txt";
        int cnt = 0;
        try (BufferedReader br = new BufferedReader(new FileReader( filename ))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] columns = line.split("\t");
                if ( columns.length < 2 ) continue;
                int id;
                id = ToolNums.Str2Int( columns[0] ).orElse( -1 );
                if ( id <= 0 ) continue;

                if ( idx > 0 ) {
                    if ( idx != id ) continue;
                }

                this.setData( id, YMD, target_date, columns[1] );
                cnt ++;
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Debugger.LogPrint( "file=" + filename + " lines=" + cnt );
        return true;
    }
    
    /***********************************************************************************************/
    // setter
    /***********************************************************************************************/
    private void setIp( String YMD, String ip ){
        if ( ! this.m_IpCnt.containsKey( ip ) ){
            this.m_IpCnt.put( ip, new IpCnt() );
        }
        this.m_IpCnt.get( ip ).setIp( YMD, ip );
    }

    public boolean setData( int id, String YMD, LocalDate target_date, String line ){
        if ( id == 13 ){
            Debugger.TracePrint();
        }
        if ( this.m_datas.containsKey( YMD ) == false ){
            this.m_datas.put( YMD, new LinkedHashMap<Integer,MailLogData>() );
        }

        if ( this.m_datas.get( YMD ).containsKey( id ) == false ){
            this.m_datas.get( YMD ).put( id, new MailLogData() );
        }

        MailLogData mld = this.m_datas.get( YMD ).get( id );

        mld.setId(id);

        // 正規表現で抽出
        Pattern pattern = Pattern.compile( "^(\\w{3}\\s+\\d{1,2}) (\\d{2}:\\d{2}:\\d{2}) (\\S+) ([^\\[]+)\\[(\\d+)\\]:\\s?(.*)$" );
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            LogLine ll = mld.newLogLine();

            // Date
            mld.setDate( target_date );

            // Time
            ll.m_time = ToolDate.Str2LocalTime( matcher.group(2) ).orElse( null );
            if ( ll.m_time == null ) return false;
            if ( mld.getTimeStart().isEmpty() || ll.m_time.isBefore( mld.getTimeStart().get() ) ){
                mld.setTimeStart( ll.m_time );
            }
            if ( mld.getTimeEnd().isEmpty() || ll.m_time.isBefore( mld.getTimeEnd().get() ) ){
                mld.setTimeEnd( ll.m_time );
            }

            // ID
            //String id = matcher.group(3);           // 4bd7d26bbaa9

            // program name
            ll.m_prog = matcher.group(4);      // postfix/smtpd
            
            // pid
            //String pid = matcher.group(5);          // 20648

            /// other
            ll.m_log = matcher.group(6);      // connect from ...

            mld.getLogs().add(ll);

            this.set_spamd( mld, ll );
            this.set_postfix_smtpd( mld, ll );
            this.set_postfix_smtp( mld, ll );
            this.set_any( mld, ll );
            this.set_dovecot( mld, ll );
            this.set_opendkim( mld, ll );
            this.set_amavis( mld, ll );
        } else {
            return false;
        }
        return true;
    }

    /**
     * ログ解析(spamd)
     * @param ll
     */
    private void set_spamd( MailLogData mld, LogLine ll ){
        if ( ! ll.m_prog.equals("spamd") ) return;
        Pattern pattern;
        Matcher matcher;

        // result:
        // if ( $data =~ /^spamd: result: ([^\s]+) ([^\s]+) ([^\s]+) (.+)$/ ){
        pattern = Pattern.compile( "^spamd: result: (\\S+) (\\S+) (\\S+) (.+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            if ( matcher.group(1).equals( "Y" ) ){
                mld.setError( "SPAM:" + matcher.group(2) );
            }
            mld.addValues( "spamd", "result/score", matcher.group(1) + "/" + matcher.group(2)  );
            for( String s: matcher.group(4).split(",") ){
                mld.addValues( "spamd", "data", s );
            }
        }

        // required_score:
		//if ( $data =~ /,required_score=([\d\.]+),/ ){
			//$self->{session_data}->{$sid}->{spamd}->{required_score} = $1;
        pattern = Pattern.compile( ",required_score=(.+)," );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "spamd", "required_score", matcher.group(1)  );
        }

        // mid
        // if ( $data =~ /,mid=<([^>]+)>,/ ){
        pattern = Pattern.compile( ",mid=<([^>]+)>," );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "spamd", "mid", matcher.group(1)  );
        }
    }

    /**
     * ログ解析(postfix/smtpd)
     * @param ll
     */
    private void set_postfix_smtpd( MailLogData mld, LogLine ll ){
        if ( ! ll.m_prog.equals("postfix/smtpd") ) return;
        Pattern pattern;
        Matcher matcher;

        // disconnect
        // if ( $data =~ /^disconnect from (.*)\[([0-9\.]+)\] (.+)$/ ){
        pattern = Pattern.compile( "^disconnect from (.*)\\[([0-9\\.]+)\\] (.+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "postfix/smtpd", "disconnect", matcher.group(1) + "[" + matcher.group(2) + "] " + matcher.group(3)  );
            return;
        }

        // connect
        // }elsif ( $data =~ /^connect from (.*)\[([0-9\.]+)\]/ ){
        pattern = Pattern.compile( "^connect from (.*)\\[([0-9\\.]+)\\]" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "postfix/smtpd", "connect_host", matcher.group(1) + "[" + matcher.group(2) + "]"  );
            mld.setIp( matcher.group(2) );
            this.setIp( ToolDate.Fromat( mld.getDate().orElse(null), "uuuuMMdd" ).orElse(null), matcher.group(2) );
            return;
        }

        // after
        // }elsif ( $data =~ /^(.+) after (.+) from (.*)\[([0-9\.]+)\]/ ){
        pattern = Pattern.compile( "^(.+) after (.+) from (.*)\\[([0-9\\.]+)\\]" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "postfix/smtpd", "error", matcher.group(1) + " after " + matcher.group(2) );
            mld.setError( matcher.group(1) + " after " + matcher.group(2) );
            return;
        }

        // NOQUEUE
        // }elsif ( $data =~ /NOQUEUE: reject: (.+) from (.*)\[([0-9\.]+)\]: (\d+) ([\d\.]+) <(.*)\[([0-9\.]+)\]>: (.+);/ ){

        pattern = Pattern.compile( "^NOQUEUE: reject: (.+) from (.*)\\[([0-9\\.]+)\\]: (\\d+) ([\\d\\.]+) <(.*)>: (.+);(.+)" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "postfix/smtpd", "reject_host", matcher.group(1) + "[" + matcher.group(2) + "]"  );
            mld.addValues( "postfix/smtpd", "reject_error", matcher.group(4) + " " + matcher.group(5) + "<" + matcher.group(6) + ">:" + matcher.group(7) );
            mld.setError( matcher.group(4) + " " + matcher.group(5) + "<" + matcher.group(6) + ">:" + matcher.group(7) );
            return;
        }

        this.setFromToMid( mld, ll.m_log );

    }

    /**
     * ログ解析(postfix/smtp)
     * @param ll
     */
    private void set_postfix_smtp( MailLogData mld, LogLine ll ){
        if ( ! ll.m_prog.equals("postfix/smtp") ) return;
        Pattern pattern;
        Matcher matcher;

        // D0C24403D281: to=<delta@d77.jp>
        // , relay=127.0.0.1[127.0.0.1]:10024
        // , delay=0.36
        // , delays=0.07/0.02/0.01/0.26
        // , dsn=2.0.0
        // , status=sent (250 2.0.0 from MTA(smtp:[127.0.0.1]:10023): 250 2.0.0 Ok: queued as 19ACB4030B9C)
        pattern = Pattern.compile( "(.+)status=sent \\((\\d+) ([\\d\\.]+) from (.+): (\\d+) ([\\d\\.]+) Ok:(.+)" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.getResult().add( matcher.group(2) + " " + matcher.group(3) + " Ok" );
        }

        pattern = Pattern.compile( "(.+)status=(.+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "postfix/smtp", "status", matcher.group(2)  );
        }
        this.setFromToMid( mld, ll.m_log );
    }

    /**
     * ログ解析(any)
     * @param ll
     */
    private void set_any( MailLogData mld, LogLine ll ){
        if ( ll.m_prog.equals("postfix/qmgr")
            || ll.m_prog.equals("postfix/cleanup")
            || ll.m_prog.equals("postfix/pickup")
            || ll.m_prog.equals("postfix/local")
            || ll.m_prog.equals("postfix/pipe")
            ){
            this.setFromToMid( mld, ll.m_log );
        }
    }

    /**
     * ログ解析(dovecot)
     * @param ll
     */
    private void set_dovecot( MailLogData mld, LogLine ll ){
        if ( ! ll.m_prog.equals("dovecot") ) return;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "(.+) msgid=<([^>]+)>," );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "dovecot", "mid", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+)stored mail into mailbox \\'(.+)\\'" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "dovecot", "into_mailbox", matcher.group(2)  );
            mld.setResult( "Into: " + matcher.group(2) );
        }

        pattern = Pattern.compile( "(.+)saved mail to (.+)" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "dovecot", "into_mailbox", matcher.group(2)  );
            mld.setResult( "Into: " + matcher.group(2) );
        }

        pattern = Pattern.compile( "(.+)lda\\((.+)\\)" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "dovecot", "into_local", matcher.group(2) );
            mld.setResult( "Into: " + matcher.group(2) );
        }
    }

    /**
     * ログ解析(OpenDKIM)
     * @param ll
     */
    private void set_opendkim( MailLogData mld, LogLine ll ){
        if ( ! ll.m_prog.equals("opendkim") ) return;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "^([0-9A-Z]+): DKIM-Signature field added (.+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            mld.addValues( "opendkim", "data", "DKIM-Signature field added " + matcher.group(2)  );
        }else{
            pattern = Pattern.compile( "^([0-9A-Z]+): (.+)$" );
            matcher = pattern.matcher( ll.m_log );
            if (matcher.matches()) {
                mld.addValues( "opendkim", "data", matcher.group(2)  );
                //mld.setError( matcher.group(2) );
            }
        }
    }

    /**
     * ログ解析(amavis)
     * @param ll
     */
    private void set_amavis( MailLogData mld, LogLine ll ){
        if ( ! ll.m_prog.equals("amavis") ) return;
        Pattern pattern;
        Matcher matcher;

        //(07005-07) Blocked SPAM {DiscardedInbound,Quarantined}
        // , [127.0.0.1] [189.90.221.169] <Ana.y_kacchin88@e-hon.ne.jp> -> <red-fox@d77.jp>
        // , Message-ID: <1349204783.822857.1749927736012@vm188>, mail_id: RipUOouNK7S4, Hits: 53.339, size: 13755, 3822 ms
        pattern = Pattern.compile( "^\\(([^\\)]+)\\) (.+) \\{([^\\}]+)\\}, (.+)" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            if ( ! matcher.group(2).equals( "Passed CLEAN" ) ){
                mld.setError( matcher.group(2) + " " + matcher.group(3) );
            }
            mld.addValues( "amavis", "status", matcher.group(2) );
            mld.addValues( "amavis", "status_sub", matcher.group(3) );
        }
    }

    /**
     * ログ解析(From/To/Mid)
     * @param ll
     */
    private void setFromToMid( MailLogData mld, String log ){
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "(.+) from=<([^>]+)>" );
        matcher = pattern.matcher( log );
        if (matcher.matches()) {
            mld.addValues( "address", "from", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+) to=<([^>]+)>" );
        matcher = pattern.matcher( log );
        if (matcher.matches()) {
            mld.addValues( "address", "to", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+) message-id=<([^>]+)>" );
        matcher = pattern.matcher( log );
        if (matcher.matches()) {
            mld.addValues( "mid", "-", matcher.group(2)  );
        }
    }
}
