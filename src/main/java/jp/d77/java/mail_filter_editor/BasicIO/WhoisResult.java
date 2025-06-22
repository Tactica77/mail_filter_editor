package jp.d77.java.mail_filter_editor.BasicIO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class WhoisResult {
    public class WhoisData {
        public String m_whois_server = "";
        public HashMap<String,Boolean> m_whois_history = new HashMap<String,Boolean>();
        public LinkedHashMap<String,ArrayList<String>> m_results = new LinkedHashMap<String,ArrayList<String>>();
        public void init(){
            this.m_results = new LinkedHashMap<String,ArrayList<String>>();
        }
    }

    private String m_ipAddress = "";
    private String m_whois_server = "";
    private String m_whois_string = "";
    private String m_error = null;
    private WhoisData m_datas = new WhoisData();
    private WhoisResult m_child = null;
    private int m_level = 0;

    /**
     * 戻り値true=子がいる
     * @return
     */
    public boolean isParents(){
        if ( this.m_child == null ) return true;
        return false;
    }

    public String getIp(){ return this.m_ipAddress; }
    public String getWhoisResult(){ return this.m_whois_string; }
    public String getServer(){ return this.m_datas.m_whois_server; }
    public String getThisServer(){ return this.m_whois_server; }
    public Optional<WhoisResult> getChild(){ return Optional.ofNullable( this.m_child ); }
    public Optional<String> getError(){  return Optional.ofNullable( this.m_error ); }
    public LinkedHashMap<String,ArrayList<String>> getResult(){ return this.m_datas.m_results; }

    public boolean requestWhois( String ip ){
        return this.requestWhois( 0, ip, "whois.iana.org", this.m_datas );
    }

    public boolean requestWhois( int level, String ip, String server, WhoisData datas ){
        this.m_level = level + 1;
        this.m_ipAddress = ip;
        this.m_whois_server = server;
        this.m_datas = datas;
        this.m_datas.init();
        Debugger.LogPrint( "level=" + this.m_level + " ip=" + this.m_ipAddress + " server=" + this.m_whois_server );
    
        try (Socket socket = new Socket(this.m_whois_server, 43);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String line;
            String child_whois_server = null;
            out.println( this.m_ipAddress );
            while ((line = in.readLine()) != null) {
                this.addResult( line );
                if ( child_whois_server == null ) child_whois_server = this.extractReferral(line).orElse( null );
            }

            // 再帰呼び出し制限
            if ( this.m_level >= 5 ) return true;
            if ( child_whois_server != null && ! this.m_datas.m_whois_history.containsKey( child_whois_server ) ){
                this.m_datas.m_whois_history.put( child_whois_server, true );
                // whoisサーバを変えて再検索
                this.m_child = new WhoisResult();
                this.m_child.requestWhois( this.m_level, this.m_ipAddress, child_whois_server, this.m_datas );
            }

            return true;
        } catch (Exception e) {
            this.setError(e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 別のWhoisサーバを要求されてるかチェック
     * @param line
     * @return
     */
    private Optional<String> extractReferral(String line) {
        line = line.trim().toLowerCase();
        if (line.toLowerCase().startsWith("refer:")) {
            return Optional.ofNullable( line.split(":")[1].trim() );
        }

        if (line.startsWith("whois server:") || line.startsWith("referralserver:")) {
            return Optional.ofNullable ( line.replaceAll(".*whois://", "")
                        .replaceAll("whois server:", "")
                        .replaceAll("referralserver:", "")
                        .trim() );
        }
        return Optional.empty();
    }

    public void addResult( String s ){
        this.m_whois_string += s + "\n";

        // 空やコメントに対しては何もしない
        if ( s.isBlank() || s.startsWith("#") || s.startsWith("%") ) return;

        if ( ! s.contains(":") ) return;
        String[] parts = s.split(":", 2);
        String key = parts[0].trim();
        String value = parts[1].trim();

        if ( ! this.m_datas.m_results.containsKey( key ) ) this.m_datas.m_results.put( key, new ArrayList<String>() );
        this.m_datas.m_results.get(key).add(value);
        String sp_key;
        if ( key.toLowerCase().equals("inetnum")
            || key.toLowerCase().equals("netrange")
            || key.toLowerCase().equals("ip-network")
            || key.toLowerCase().equals("[network number]")
            || key.toLowerCase().equals("cidr")
            || key.toLowerCase().equals("route")
            ){
            sp_key = "sp_cidr";
            if ( ! this.m_datas.m_results.containsKey( sp_key ) ) this.m_datas.m_results.put( sp_key, new ArrayList<String>() );
            List<String> cidr = ToolNet.convertToCIDR(value);
            this.m_datas.m_results.get(sp_key).add( String.join(",", cidr) );

        }else if ( key.toLowerCase().equals("country") ){
            sp_key = "sp_country";
            if ( ! this.m_datas.m_results.containsKey( sp_key ) ) this.m_datas.m_results.put( sp_key, new ArrayList<String>() );
            this.m_datas.m_results.get(sp_key).add( value );

        }else if ( key.toLowerCase().equals("orgname")
            || key.toLowerCase().equals("org-name")
            || key.toLowerCase().contains("organization")
            ){
            sp_key = "sp_organization";
            if ( ! this.m_datas.m_results.containsKey( sp_key ) ) this.m_datas.m_results.put( sp_key, new ArrayList<String>() );
            this.m_datas.m_results.get(sp_key).add(value);

        }else if ( key.toLowerCase().equals("descr")
            || key.toLowerCase().equals("[name]")
            ){
            sp_key = "sp_organization2";
            if ( ! this.m_datas.m_results.containsKey( sp_key ) ) this.m_datas.m_results.put( sp_key, new ArrayList<String>() );
            this.m_datas.m_results.get(sp_key).add(value);

        }else if ( key.toLowerCase().equals("role")
            || key.toLowerCase().equals("owner")
            || key.toLowerCase().equals("netname")
            ){
            sp_key = "sp_organization3";
            if ( ! this.m_datas.m_results.containsKey( sp_key ) ) this.m_datas.m_results.put( sp_key, new ArrayList<String>() );
            this.m_datas.m_results.get(sp_key).add(value);
        }
    }

    public void dump(){
        for( String key: this.m_datas.m_results.keySet() ){
            Debugger.LogPrint( "key=" + key );
            for( String value: this.m_datas.m_results.get(key) ){
                Debugger.LogPrint("\tval=" + value );
            }
        }
    }

    public void setError( Exception e ){
        this.m_error = e.getMessage();
    }
}
