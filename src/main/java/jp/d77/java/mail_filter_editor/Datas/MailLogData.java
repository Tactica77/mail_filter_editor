package jp.d77.java.mail_filter_editor.Datas;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

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
    private ArrayList<String>   m_result;
    private ArrayList<String>   m_error;
    private HashMap<String, LinkedHashMap<String,String>> m_values;

    public MailLogData(){
        this.m_log = new ArrayList<LogLine>();
        this.m_values = new HashMap<String, LinkedHashMap<String,String>>();
        this.m_result = new ArrayList<String>();
        this.m_error = new ArrayList<String>();
    }

    /***********************************************************************************************/
    // getter
    /***********************************************************************************************/
    public LogLine newLogLine(){
        return new LogLine();
    }
    public int getId(){
        if ( this.m_id == null ) return 999999;
        return this.m_id;
    }
    public Optional<LocalDate> getDate(){ return Optional.ofNullable( this.m_date ); }
    public Optional<LocalTime> getTimeStart(){ return Optional.ofNullable( this.m_time_start ); }
    public Optional<LocalTime> getTimeEnd(){ return Optional.ofNullable( this.m_time_end ); }
    public Optional<String> getIp(){ return Optional.ofNullable( this.m_ip ); }
    public ArrayList<String> getError(){ return this.m_error; }
    public ArrayList<String> getResult(){ return this.m_result; }
    public Optional<String> getFrom(){
        if ( this.m_values.containsKey("address") == true && this.m_values.get("address").containsKey( "from" ) == true ){
            return Optional.ofNullable( this.m_values.get("address").get( "from" ) );
        }
        return Optional.empty();
    }
    public Optional<String> getTo(){
        if ( this.m_values.containsKey("address") == true && this.m_values.get("address").containsKey( "from" ) == true ){
            return Optional.ofNullable( this.m_values.get("address").get( "from" ) );
        }
        return Optional.empty();
    }
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

    /***********************************************************************************************/
    // setter
    /***********************************************************************************************/
    public void setId( int i ){ this.m_id = i; }
    public void setDate( LocalDate d ){ this.m_date = d; }
    public void setTimeStart( LocalTime t ){ this.m_time_start = t; }
    public void setTimeEnd( LocalTime t ){ this.m_time_end = t; }
    public void setIp( String ip ){ if ( ! ip.equals("127.0.0.1") ) { this.m_ip = ip;} }
    public void setError( String error ){ this.m_error.add( error ); }
    public void setResult( String res ){ this.m_result.add( res ); }

    /**
     * 解析データを格納
     * @param prog
     * @param name
     * @param value
     */
    public void addValues( String prog, String name, String value ){
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
}
