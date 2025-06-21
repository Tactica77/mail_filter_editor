package jp.d77.java.mail_filter_editor.Datas;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;

public class MailLogData {
    public class LogLine {
        public LocalTime   m_time;
        public String m_prog;
        public String m_log;
    }
    private Integer     m_id;
    private LocalDate   m_date;
    private LocalTime   m_time_start;
    private LocalTime   m_time_end;
    private String      m_ip;
    private ArrayList<LogLine>   m_log;
    private String      m_addr_from;
    private ArrayList<String>   m_addr_to;
    private ArrayList<String>   m_result;
    private HashMap<String, LinkedHashMap<String,String>> m_values;

    public MailLogData(){
        this.m_log = new ArrayList<LogLine>();
        this.m_values = new HashMap<String, LinkedHashMap<String,String>>();
        this.m_addr_to = new ArrayList<String>();
        this.m_result = new ArrayList<String>();
    }

    public int getId(){
        if ( this.m_id == null ) return 999999;
        return this.m_id;
    }

    public Optional<LocalDate> getDate(){ return Optional.ofNullable( this.m_date ); }
    public Optional<LocalTime> getTimeStart(){ return Optional.ofNullable( this.m_time_start ); }
    public Optional<LocalTime> getTimeEnd(){ return Optional.ofNullable( this.m_time_end ); }
    public Optional<String> getIp(){ return Optional.ofNullable( this.m_ip ); }
    public Optional<String> getFrom(){ return Optional.ofNullable( this.m_addr_from ); }
    public ArrayList<String> getTo(){ return this.m_addr_to; }
    public ArrayList<String> getResult(){ return this.m_result; }
    public String getTimeRange(){
        String ret = "";

        if ( this.m_time_start == null ){
        }else{
            ret += this.m_time_start;
        }
        ret += " - ";

        if ( this.m_time_end == null ){
        }else{
            ret += this.m_time_end;
        }
        return ret;
    }
    public long getSec(){
        if ( this.m_time_start == null ) return 0L;
        if ( this.m_time_end == null ) return 0L;
        return Duration.between( this.m_time_start, this.m_time_end ).getSeconds();
    }
    public ArrayList<LogLine> getLogs(){
        return this.m_log;
    }

    public HashMap<String, LinkedHashMap<String,String>> getValues(){
        return this.m_values;
    }

    public boolean set( int id, LocalDate date, String line ){
        this.m_id = id;
        // 正規表現で抽出
        Pattern pattern = Pattern.compile(
            "^(\\w{3} \\d{1,2}) (\\d{2}:\\d{2}:\\d{2}) (\\S+) ([^\\[]+)\\[(\\d+)\\]:\\s?(.*)$"
        );

        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            LogLine ll = new LogLine();

            // Date
            this.m_date = date;

            // Time
            ll.m_time = ToolDate.Str2LocalTime( matcher.group(2) ).orElse( null );
            if ( ll.m_time == null ) return false;
            if ( this.m_time_start == null || ll.m_time.isBefore( this.m_time_start ) ){
                this.m_time_start = ll.m_time;
            }
            if ( this.m_time_end == null || ll.m_time.isBefore( this.m_time_end ) ){
                this.m_time_end = ll.m_time;
            }

            // ID
            //String id = matcher.group(3);           // 4bd7d26bbaa9

            // program name
            ll.m_prog = matcher.group(4);      // postfix/smtpd
            
            // pid
            //String pid = matcher.group(5);          // 20648

            /// other
            ll.m_log = matcher.group(6);      // connect from ...

            this.m_log.add(ll);

            this.set_spamd( ll );
            this.set_postfix_smtpd( ll );
            this.set_postfix_smtp( ll );
            this.set_any( ll );
            this.set_dovecot( ll );
            this.set_opendkim( ll );
            this.set_amavis( ll );
        } else {
            return false;
        }
        return true;
    }

    private void addValues( String prog, String name, String value ){
        if ( this.m_values.containsKey(prog) == false ){
            this.m_values.put( prog,new LinkedHashMap<String,String>() );
        }

        if ( this.m_values.get(prog).containsKey( name ) == true ){
            this.m_values.get(prog).put( name, 
                this.m_values.get(prog).get( name ) + "\n" + value );
        }else{
            this.m_values.get(prog).put( name, value );
        }
    }

    private void set_spamd( LogLine ll ){
        if ( ! ll.m_prog.equals("spamd") ) return;
        Pattern pattern;
        Matcher matcher;

        // result:
        // if ( $data =~ /^spamd: result: ([^\s]+) ([^\s]+) ([^\s]+) (.+)$/ ){
        pattern = Pattern.compile( "^spamd: result: (\\S+) (\\S+) (\\S+) (.+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "spamd", "result/score", matcher.group(1) + "/" + matcher.group(2)  );
            for( String s: matcher.group(4).split(",") ){
                this.addValues( "spamd", "data", s );
            }
        }

        // required_score:
		//if ( $data =~ /,required_score=([\d\.]+),/ ){
			//$self->{session_data}->{$sid}->{spamd}->{required_score} = $1;
        pattern = Pattern.compile( ",required_score=(.+)," );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "spamd", "required_score", matcher.group(1)  );
        }

        // mid
        // if ( $data =~ /,mid=<([^>]+)>,/ ){
        pattern = Pattern.compile( ",mid=<([^>]+)>," );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "spamd", "mid", matcher.group(1)  );
        }
    }

    private void set_postfix_smtpd( LogLine ll ){
        if ( ! ll.m_prog.equals("postfix/smtpd") ) return;
        Pattern pattern;
        Matcher matcher;

        // disconnect
        // if ( $data =~ /^disconnect from (.*)\[([0-9\.]+)\] (.+)$/ ){
        pattern = Pattern.compile( "^disconnect from (.*)\\[([0-9\\.]+)\\] (.+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "postfix/smtpd", "disconnect", matcher.group(1) + "[" + matcher.group(2) + "] " + matcher.group(3)  );
            return;
        }

        // connect
        // }elsif ( $data =~ /^connect from (.*)\[([0-9\.]+)\]/ ){
        pattern = Pattern.compile( "^connect from (.*)\\[([0-9\\.]+)\\]" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "postfix/smtpd", "connect_host", matcher.group(1) + "[" + matcher.group(2) + "]"  );
            return;
        }

        // after
        // }elsif ( $data =~ /^(.+) after (.+) from (.*)\[([0-9\.]+)\]/ ){
        pattern = Pattern.compile( "^(.+) after (.+) from (.*)\\[([0-9\\.]+)\\]" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "postfix/smtpd", "error", matcher.group(1) + " after " + matcher.group(2) );
            return;
        }

        // NOQUEUE
        // }elsif ( $data =~ /NOQUEUE: reject: (.+) from (.*)\[([0-9\.]+)\]: (\d+) ([\d\.]+) <(.*)\[([0-9\.]+)\]>: (.+);/ ){
        pattern = Pattern.compile( "(.+)OQUEUE: reject: (.+) from (.*)\\[([0-9\\.]+)\\]: (\\d+) ([\\d\\.]+) <(.*)\\[([0-9\\.]+)\\]>: (.+);" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "postfix/smtpd", "reject_host", matcher.group(1)  );
            this.addValues( "postfix/smtpd", "reject_error", matcher.group(4) + " " + matcher.group(5) );
            return;
        }

        this.setFromToMid( ll.m_log );

    }

    private void set_postfix_smtp( LogLine ll ){
        if ( ! ll.m_prog.equals("postfix/smtp") ) return;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "(.+)status=(.+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "postfix/smtp", "status", matcher.group(2)  );
            return;
        }
        this.setFromToMid( ll.m_log );
    }

    private void set_any( LogLine ll ){
        if ( ll.m_prog.equals("postfix/qmgr")
            || ll.m_prog.equals("postfix/cleanup")
            || ll.m_prog.equals("postfix/pickup")
            || ll.m_prog.equals("postfix/local")
            || ll.m_prog.equals("postfix/pipe")
            ){
            this.setFromToMid( ll.m_log );
        }
    }

    private void set_dovecot( LogLine ll ){
        if ( ! ll.m_prog.equals("dovecot") ) return;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "(.+) msgid=<([^>]+)>," );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "dovecot", "mid", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+)stored mail into mailbox \\'(.+)\\'" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "dovecot", "into_mailbox", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+)saved mail to (.+)" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "dovecot", "into_mailbox", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+)lda\\((.+)\\)" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "dovecot", "into_local", matcher.group(2) );
        }
    }

    private void set_opendkim( LogLine ll ){
        if ( ! ll.m_prog.equals("opendkim") ) return;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "^([0-9A-Z]+): ([\\d\\.]+)$" );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "dovecot", "data", matcher.group(2)  );
        }
    }

    private void set_amavis( LogLine ll ){
        if ( ! ll.m_prog.equals("amavis") ) return;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "^\\(([^\\)]+)\\) (.+) \\{([^\\}]+)\\}," );
        matcher = pattern.matcher( ll.m_log );
        if (matcher.matches()) {
            this.addValues( "amavis", "status", matcher.group(2)  );
            this.addValues( "amavis", "status_sub", matcher.group(3)  );
        }
    }

    private void setFromToMid( String log ){
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile( "(.+) from=<([^>]+)>" );
        matcher = pattern.matcher( log );
        if (matcher.matches()) {
            this.addValues( "address", "from", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+) to=<([^>]+)>" );
        matcher = pattern.matcher( log );
        if (matcher.matches()) {
            this.addValues( "address", "to", matcher.group(2)  );
        }

        pattern = Pattern.compile( "(.+) message-id=<([^>]+)>" );
        matcher = pattern.matcher( log );
        if (matcher.matches()) {
            this.addValues( "mid", "-", matcher.group(2)  );
        }
    }
}
