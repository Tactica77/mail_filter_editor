package jp.d77.java.mail_filter_editor.BasicIO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class WhoisResult {
    private String m_ipAddress = "";
    private String m_iana_result = "";
    private String m_whois_result = "";
    private String m_whoisRedirectServer = null;
    private String m_error = null;
    private LinkedHashMap<String,ArrayList<String>> m_results = new LinkedHashMap<String,ArrayList<String>>();

    public void setIp( String ip ){
        this.m_ipAddress = ip;
    }

    public void addIanaResult( String s ){
        this.m_iana_result += s + "\n";
    }

    public void addWhoisResult( String s ){
        this.m_whois_result += s + "\n";

        // 空やコメントに対しては何もしない
        if ( s.isBlank() || s.startsWith("#") || s.startsWith("%") ) return;

        if ( ! s.contains(":") ) return;
        String[] parts = s.split(":", 2);
        String key = parts[0].trim();
        String value = parts[1].trim();

        if ( ! this.m_results.containsKey( key ) ) this.m_results.put( key, new ArrayList<String>() );
        this.m_results.get(key).add(value);
        String sp_key;
        if ( key.toLowerCase().equals("inetnum")
            || key.toLowerCase().equals("netrange")
            || key.toLowerCase().equals("ip-network")
            || key.toLowerCase().equals("[network number]")
            || key.toLowerCase().equals("cidr")
            || key.toLowerCase().equals("route")
            ){
            sp_key = "sp_cidr";
            if ( ! this.m_results.containsKey( sp_key ) ) this.m_results.put( sp_key, new ArrayList<String>() );
            List<String> cidr = ToolNet.convertToCIDR(value);
            this.m_results.get(sp_key).add( String.join(",", cidr) );

        }else if ( key.toLowerCase().equals("country") ){
            sp_key = "sp_country";
            if ( ! this.m_results.containsKey( sp_key ) ) this.m_results.put( sp_key, new ArrayList<String>() );
            this.m_results.get(sp_key).add( value );

        }else if ( key.toLowerCase().equals("orgname")
            || key.toLowerCase().equals("org-name")
            || key.toLowerCase().contains("organization")
            ){
            sp_key = "sp_organization";
            if ( ! this.m_results.containsKey( sp_key ) ) this.m_results.put( sp_key, new ArrayList<String>() );
            this.m_results.get(sp_key).add(value);

        }else if ( key.toLowerCase().equals("descr")
            || key.toLowerCase().equals("[name]")
            ){
            sp_key = "sp_organization2";
            if ( ! this.m_results.containsKey( sp_key ) ) this.m_results.put( sp_key, new ArrayList<String>() );
            this.m_results.get(sp_key).add(value);

        }else if ( key.toLowerCase().equals("role")
            || key.toLowerCase().equals("owner")
            || key.toLowerCase().equals("netname")
            ){
            sp_key = "sp_organization3";
            if ( ! this.m_results.containsKey( sp_key ) ) this.m_results.put( sp_key, new ArrayList<String>() );
            this.m_results.get(sp_key).add(value);
        }
        //Debugger.LogPrint( "key=" + key + " val=" + value );
    }

    public void dump(){
        for( String key: this.m_results.keySet() ){
            Debugger.LogPrint( "key=" + key );
            for( String value: this.m_results.get(key) ){
                Debugger.LogPrint("\tval=" + value );
            }
        }
    }

    public void setRIR( String ip ){
        this.m_whoisRedirectServer = ip;
    }

    public void setError( Exception e ){
        this.m_error = e.getMessage();
    }

    public String getIp(){  return this.m_ipAddress; }
    public String getIanaResult(){  return this.m_iana_result; }
    public String getWhoisResult(){  return this.m_whois_result; }
    public Optional<String> getRIR(){  return Optional.ofNullable( this.m_whoisRedirectServer ); }
    public Optional<String> getError(){  return Optional.ofNullable( this.m_error ); }
    public LinkedHashMap<String,ArrayList<String>> getResult(){ return this.m_results; }
}
