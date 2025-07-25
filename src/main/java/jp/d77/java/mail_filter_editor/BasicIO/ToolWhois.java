package jp.d77.java.mail_filter_editor.BasicIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ToolNums;
import jp.d77.java.tools.Datas.JsonIO;
import jp.d77.java.tools.Datas.JsonIO.DATA_TYPE;

public class ToolWhois {
    /**
     * Whois結果格納
     */
    public static class WhoisData {
        // whois結果そのまま
        private String m_query_result = "";

        // whois取得日
        private LocalDate m_query_date;

        // 解析データ
        private LinkedHashMap<String,ArrayList<String>> m_results = null;

        public void setDate( LocalDate date ){
            this.m_query_date = date;
        }

        public LocalDate getDate(){
            return this.m_query_date;
        }

        /**
         * 有効期限切れか?
         * @return true=期限切れ
         */
        public boolean expired(){
            if ( this.m_query_date == null || this.m_query_date.getDayOfMonth() < 30 ){
                return false;
            }
            return true;
        }

        /**
         * 解析済みか？
         * @return true=解析済み
         */
        public boolean isParsed(){
            if ( this.m_results == null ) return false;
            return true;
        }

        /**
         * 結果を1行ずつ結合
         * @param s
         */
        public void addQueryResult( String s ){
            this.m_query_result += s + "\n";
        }
        /**
         * 結果を格納
         * @param s
         */
        public void setQueryResult( String s ){
            this.m_query_result = s;
        }

        /**
         * 結果格納
         * @param key
         * @param value
         */
        public void addResult( String key, String value ){
            if ( this.m_results == null ) this.m_results = new LinkedHashMap<String,ArrayList<String>>();
            if ( ! this.m_results.containsKey(key) ) this.m_results.put(key, new ArrayList<String>() );

            for ( String v: this.m_results.get(key) ){
                if ( v.equals(value) ) return;  // 同じ値がすでにある
            }
            this.m_results.get(key).add(value);
        }

        /**
         * Key/Value値を取得する
         * @param key
         * @return
         */
        public Optional<ArrayList<String>> getResult( String key ){
            if ( this.m_results == null ) return Optional.empty();
            if ( ! this.m_results.containsKey(key) ) return Optional.empty();
            return Optional.ofNullable( this.m_results.get(key) );
        }

        public Optional<String> getResultOne( String key ){
            if ( this.getResult(key).isEmpty() ) return Optional.empty();
            if ( this.getResult(key).get().size() <= 0 ) return Optional.empty();
            return Optional.ofNullable( this.getResult(key).get().get(0) );
        }

        /**
         * CIDR取得
         * @return
         */
        public Optional<ArrayList<String>> getCidr(){
            if ( this.getResult( "sp_cidr").isEmpty() ) return Optional.empty();
            if ( this.getResult( "sp_cidr").get().size() <= 0 ) return Optional.empty();
            TreeMap<String,Boolean> res = new TreeMap<String,Boolean>();
            for ( String v: this.getResult( "sp_cidr").get()){
                for ( String v2: v.split(",") ){
                    res.put(v2, true);
                }
            }
            return Optional.ofNullable( new ArrayList<>(res.keySet()) );
        }
        /**
         * CC取得
         * @return
         */
        public Optional<String> getCc(){
            if ( this.getResult( "sp_country").isEmpty() ) return Optional.empty();
            if ( this.getResult( "sp_country").get().size() <= 0 ) return Optional.empty();
            return Optional.ofNullable( this.getResult( "sp_country" ).get().get(0) );
        }
        /**
         * Organization取得
         * @return
         */
        public Optional<String> getOrg(){
            if ( ! this.getResult( "sp_organization").isEmpty() && this.getResult( "sp_organization").get().size() > 0 ) 
                return Optional.ofNullable( this.getResult( "sp_organization" ).get().get(0) );
            if ( ! this.getResult( "sp_organization2").isEmpty() && this.getResult( "sp_organization2").get().size() > 0 ) 
                return Optional.ofNullable( this.getResult( "sp_organization2" ).get().get(0) );
            if ( ! this.getResult( "sp_organization3").isEmpty() && this.getResult( "sp_organization3").get().size() > 0 ) 
                return Optional.ofNullable( this.getResult( "sp_organization3" ).get().get(0) );
            return Optional.empty();
        }

        /**
         * 参照先サーバを取得
         * @return
         */
        public Optional<String> getChildServer(){
            if ( this.getResult( "sp_child_server").isEmpty() ) return Optional.empty();
            if ( this.getResult( "sp_child_server").get().size() <= 0 ) return Optional.empty();
            return Optional.ofNullable( this.getResult( "sp_child_server").get().get(0) );
        }

        public Optional<String> getServer(){
            if ( this.getResult( "sp_whois_server").isEmpty() ) return Optional.empty();
            if ( this.getResult( "sp_whois_server").get().size() <= 0 ) return Optional.empty();
            return Optional.ofNullable( this.getResult( "sp_whois_server").get().get(0) );
        }

        public String getQueryResult(){
            return this.m_query_result;
        }
    }

    // Whoisキャッシュ
    private static HashMap<String, HashMap<String, WhoisData>> m_whois_result;  // ip, server, WhoisData

    // Whoisキャッシュファイル名
    private static String m_file = null;
    
    // キャッシュのsaveが必要か?
    private static boolean m_must_save = false;

    /**
     * Whoisデータを取得する
     * @param ip
     * @param exec_query: true=キャッシュに無い場合はネットから取得する
     * @return
     */
    public static Optional<WhoisData> get( String ip, boolean exec_query ){
        String server = ToolWhois.requestWhois(ip).orElse( null );
        if ( server == null ) return Optional.empty();
        if ( ! ToolWhois.m_whois_result.containsKey( ip ) )return Optional.empty();
        if ( ! ToolWhois.m_whois_result.get( ip ).containsKey(server) ) return Optional.empty();
        return Optional.ofNullable( ToolWhois.m_whois_result.get( ip ).get( server ) );
    }

    public static boolean catched( String ip ){
        if ( ! ToolWhois.m_whois_result.containsKey( ip ) ) return false;
        return true;
    }

    public static Optional<String> requestWhois( String ip ){
        return ToolWhois.requestWhois( 0, ip, "whois.iana.org", true );
    }

    public static Optional<String> requestWhois( String ip, boolean exec_query ){
        return ToolWhois.requestWhois( 0, ip, "whois.iana.org", exec_query );
    }

    public static Optional<String> requestWhois( int level, String ip, String server, boolean exec_query ){
        boolean need_query = false;

        if ( ToolWhois.m_whois_result == null ){
            // キャッシュテーブル作成
            ToolWhois.m_whois_result = new HashMap<String, HashMap<String, WhoisData>>();
            ToolWhois.load();
        }

        if ( ! ToolWhois.m_whois_result.containsKey( ip ) ){
            // IP未登録
            ToolWhois.m_whois_result.put( ip, new HashMap<String, WhoisData>() );
            Debugger.InfoPrint( "not regist ip=" + ip );
        }
        if ( ! ToolWhois.m_whois_result.get( ip ).containsKey( server ) ){
            // サーバ未登録
            ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() );
            need_query = true;
            Debugger.InfoPrint( "not regist ip=" + ip + " server=" + server );
        }
        if ( ToolWhois.m_whois_result.get( ip ).get( server ).expired() ){
            // キャッシュ有効期限切れ
            Debugger.InfoPrint( "cache expired ip=" + ip + " server=" + server );
            need_query = true;
        }
        
        if ( need_query ){
            if ( ! exec_query ){
                // クエリー禁止なのでエラー
                return Optional.empty();
            }

            boolean bres;
            if ( server.endsWith( ":4321" ) ){
                ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                bres = ToolWhois.queryRWhois( ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
            }else{
                ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                bres = ToolWhois.queryWhois( "", ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
                if ( bres == false ){
                    ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                    bres = ToolWhois.queryWhois( "n ", ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
                }
                if ( bres == false ){
                    ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                    bres = ToolWhois.queryWhois( "n + ", ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
                }
            }
            if ( !bres ){
                String new_ip = ToolWhois.changeCheckIP( ToolWhois.m_whois_result.get( ip ).get(server) ).orElse( null );
                if ( new_ip == null ) return Optional.empty();
                if ( new_ip.equals( ip ) ) return Optional.empty();
                Debugger.InfoPrint( "change check IP: " + ip + " -> " + new_ip );

                if ( server.endsWith( ":4321" ) ){
                    ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                    bres = ToolWhois.queryRWhois( new_ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
                }else{
                    ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                    bres = ToolWhois.queryWhois( "", new_ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
                    if ( bres == false ){
                        ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                        bres = ToolWhois.queryWhois( "n ", new_ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
                    }
                    if ( bres == false ){
                        ToolWhois.m_whois_result.get( ip ).put(server, new WhoisData() ); // データ初期化
                        bres = ToolWhois.queryWhois( "n + ", new_ip, server, ToolWhois.m_whois_result.get( ip ).get(server) );
                    }
                }
            }

            if ( !bres ){
                return Optional.empty();
            }
        }else{
            if ( ToolWhois.checkWhoisResult( ToolWhois.m_whois_result.get( ip ).get( server ), server ) ){
                // OK
            }
        }

        ToolWhois.m_whois_result.get( ip ).get( server ).addResult( "sp_whois_server", server);

        if ( ToolWhois.m_whois_result.get( ip ).get( server ).getChildServer().isEmpty() || level >= 5 ) {
            // child serverが設定されている
            Debugger.InfoPrint( "level=" + level + " ip=" + ip + " server=" + server + " child_server=empty" + " cache_read=" + (!need_query) );
            if ( ToolWhois.m_must_save ) ToolWhois.save();
            return Optional.ofNullable( server );
        }
        Debugger.InfoPrint( "level=" + level + " ip=" + ip + " server=" + server + " child_server=" + server + " cache_read=" + (!need_query) );
        return ToolWhois.requestWhois( level + 1, ip, ToolWhois.m_whois_result.get( ip ).get( server ).getChildServer().get(), exec_query );
    }

    /**
     * whoisデータを格納しつつ、取得できているがを判定する
     * @param wd
     * @param server
     * @return
     */
    private static boolean checkWhoisResult( WhoisData wd, String server ){
        if ( server.endsWith( ":4321" ) ){
            // 解析
            for ( String line: wd.getQueryResult().split( "\n" ) ){
                ToolWhois.addResultRWhois( wd, line);
            }
        }else{
            // 解析
            for ( String line: wd.getQueryResult().split( "\n" ) ){
                ToolWhois.addResultWhois( wd, line);
            }
        }

        if ( wd.getChildServer().isPresent() ) return true;
        int ok_cnt = 0;
        if ( wd.getCidr().isPresent() ) ok_cnt ++;
        if ( wd.getCc().isPresent() ) ok_cnt ++;
        if ( wd.getOrg().isPresent() ) ok_cnt ++;
        if ( ok_cnt <= 1 ) return false;

        return true;
    }

    private static boolean queryWhois( String request_head, String ip, String server, WhoisData wd ){
        Debugger.InfoPrint( "request_head=" + request_head + " ip=" + ip + " server=" + server );
        String request_ip = ip;
        if ( request_ip.contains( "/" ) ){
            // Cider指定
            String[] w = request_ip.split( "\\/" );
            if ( w.length >= 1 ) request_ip = w[0];
        }

        String res = "";

        try (Socket socket = new Socket( server, 43);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String line;
            out.println( request_head + request_ip );
            while ((line = in.readLine()) != null) {
                res += line + "\n";
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }

        wd.setQueryResult( res );
        wd.setDate( LocalDate.now() );

        if ( ToolWhois.checkWhoisResult( wd, server ) ){
            // OK
            ToolWhois.m_must_save = true;
            return true;
        }

        return false;
    }

    private static boolean queryRWhois( String ip, String server, WhoisData wd ){
        Debugger.InfoPrint( "ip=" + ip + " server=" + server );
        String request_ip = ip;
        if ( request_ip.contains( "/" ) ){
            // Cider指定
            String[] w = request_ip.split( "\\/" );
            if ( w.length >= 1 ) request_ip = w[0];
        }

        String res = "";

        // Server:Port check
        String w[] = server.split(":");
        if ( w.length < 2 ) return false;
        String server_name = w[0];
        int port = 0;
        if ( ToolNums.isNumeric(w[1]) ){
            port = ToolNums.Str2Int( w[1] ).orElse( 0 );
            if ( port != 4321 ) return false;
        }else{
            return false;
        }

        String query = "" + request_ip + "\r\n";
        try (Socket socket = new Socket(server_name, port)) {
            // リクエスト送信
            OutputStream out = socket.getOutputStream();
            out.write(query.getBytes());
            out.flush();

            // 結果受信
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                res += line + "\n";
                //Debugger.InfoPrint( line );
            }

        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }

        wd.setQueryResult( res );
        wd.setDate( LocalDate.now() );

        if ( ToolWhois.checkWhoisResult( wd, server ) ){
            // OK
            ToolWhois.m_must_save = true;
            return true;
        }

        return false;
    }

    /**
     * 指定のIPで取得できなかった場合に代替えで確認できるIPがあるか調べる
     * @param wd
     * @return
     */
    private static Optional<String> changeCheckIP( WhoisData wd ){
        for ( String line: wd.getQueryResult().split( "\n" ) ){
            if ( line.isBlank() || line.startsWith("#") || line.startsWith("%") ) continue;
            Pattern pattern = Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{1,3}){3})\\s*-\\s*(\\d{1,3}(?:\\.\\d{1,3}){3})\\b");
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                String ip1 = matcher.group(1);
                return Optional.ofNullable( ip1 );
            }
        }
        return Optional.empty();
    }

    private static void addResultWhois( WhoisData wd, String line ){
        // 空やコメントに対しては何もしない
        if ( line.isBlank() || line.startsWith("#") || line.startsWith("%") ) return;

        // デフォルトのKey/Value
        if ( ! line.contains(":") ) return;
        String[] parts = line.split(":", 2);
        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        wd.addResult( key, value );
        if ( key.equals("inetnum")
            || key.equals("netrange")
            || key.equals("ip-network")
            || key.equals("[network number]")
            || key.equals("cidr")
            || key.equals("route")
            ){
            List<String> cidr = ToolNet.convertToCIDR(value);
            wd.addResult( "sp_cidr", String.join(",", cidr));
            //Debugger.LogPrint( "...sp_cidr=" + String.join(",", cidr) );

        }else if ( key.toLowerCase().equals("country") ){
            wd.addResult( "sp_country", value);
            //Debugger.LogPrint( "...sp_country=" + value );

        }else if ( key.toLowerCase().equals("orgname")
            || key.equals("org-name")
            ){
            wd.addResult( "sp_organization", value);
            //Debugger.LogPrint( "...sp_organization=" + value );

        }else if ( key.equals("descr")
            || key.contains("organization")
            || key.contains("organisation")
            || key.equals("[name]")
            ){
            wd.addResult( "sp_organization2", value);
            //Debugger.LogPrint( "...sp_organization2=" + value );

        }else if ( key.equals("role")
            || key.equals("org")
            || key.equals("owner")
            || key.equals("netname")
            ){
            wd.addResult( "sp_organization3", value);
            //Debugger.LogPrint( "...sp_organization3=" + value );
        }

        // 別のWhoisサーバを要求されてるかチェック
        if ( key.equals("refer") ) {
            wd.addResult( "sp_child_server", line.split(":")[1].trim());
        }else if ( key.contains( "resourcelink" ) && value.toLowerCase().startsWith( "whois." ) ){
            wd.addResult( "sp_child_server", value);
            return;
        }else if ( key.equals("whois server") || key.equals("referralserver")) {

            String v = value.replaceAll(".*whois://", "").trim();
            if ( v == null ) return;
            wd.addResult( "sp_child_server", v);
        }
    }

    private static void addResultRWhois( WhoisData wd, String line ){
        // 空やコメントに対しては何もしない
        if ( line.isBlank() || line.startsWith("#") || line.startsWith("%") ) return;

        if ( ! line.contains(":") ) return;
        String[] parts = line.split(":", 3);
        if ( parts.length < 3 ) return;
        String key = parts[1].trim().toLowerCase();
        String value = parts[2].trim();

        wd.addResult( key, value );
        if ( key.equals("ip-network")
            ){
            List<String> cidr = ToolNet.convertToCIDR(value);
            wd.addResult( "sp_cidr", String.join(",", cidr));

        }else if ( key.toLowerCase().equals("country-code") ){
            wd.addResult( "sp_country", value);

        }else if ( key.toLowerCase().equals("org-name;i")
            ){
            wd.addResult( "sp_organization", value);
        }
    }

    //******************************************************************************
    // キャッシュファイル操作
    //******************************************************************************

    /**
     * キャッシュファイル名を設定
     * @param file
     */
    public static void setCacheFile( String file ){
        ToolWhois.m_file = file;
    }

    /**
     * キャッシュの全データを取得する
     * @return
     */
    public static HashMap<String, HashMap<String, WhoisData>> getWhoisCache(){
        return ToolWhois.m_whois_result;
    }

    /**
     * キャッシュ読み込み
     */
    private static void load(){
        ToolWhois.m_must_save = false;
        if ( ToolWhois.m_file == null ) return;
        JsonIO j = new JsonIO("whois");
        j.setProp( "ip", DATA_TYPE.TEXT );
        j.setProp( "server", DATA_TYPE.TEXT );
        j.setProp( "result", DATA_TYPE.TEXT );
        j.setProp( "date", DATA_TYPE.YMD );
        if ( ! j.load( new File( ToolWhois.m_file ) ) ) return;

        for ( Integer line_id: j.getDatas().keySet() ){
            String ip = null;
            String server = null;
            String result = null;
            LocalDate date = null;

            if ( j.getDatas().get( line_id ).containsKey("ip" ) ){
                ip = j.getDatas().get( line_id ).get("ip" ).getText().orElse( null );
                if ( ip == null ) continue;
            }else continue;

            if ( j.getDatas().get( line_id ).containsKey("server" ) ){
                server = j.getDatas().get( line_id ).get("server" ).getText().orElse( null );
                if ( server == null ) continue;
            }else continue;

            if ( j.getDatas().get( line_id ).containsKey("result" ) ){
                result = j.getDatas().get( line_id ).get("result" ).getText().orElse( null );
                if ( result == null ) continue;
            }else continue;

            if ( j.getDatas().get( line_id ).containsKey("date" ) ){
                date = j.getDatas().get( line_id ).get("date" ).getYMD().orElse( null );
                if ( date == null ) continue;
            }else continue;

            if ( ! ToolWhois.m_whois_result.containsKey( ip ) ) ToolWhois.m_whois_result.put( ip, new HashMap<String, WhoisData>() );
            if ( ! ToolWhois.m_whois_result.get( ip ).containsKey( server ) ) ToolWhois.m_whois_result.get( ip ).put( server, new ToolWhois.WhoisData() );
            ToolWhois.m_whois_result.get( ip ).get( server).setQueryResult( result );
            ToolWhois.m_whois_result.get( ip ).get( server).setDate(date);
        }
    }

    /**
     * キャッシュ保存
     */
    private static void save(){
        if ( ToolWhois.m_file == null ) return;
        if ( ToolWhois.m_whois_result == null ) return;
        Debugger.TracePrint();

        JsonIO j = new JsonIO("whois");
        j.setProp( "ip", DATA_TYPE.TEXT );
        j.setProp( "server", DATA_TYPE.TEXT );
        j.setProp( "result", DATA_TYPE.TEXT );
        j.setProp( "date", DATA_TYPE.YMD );
        
        for ( String ip: ToolWhois.m_whois_result.keySet() ){
            for ( String server: ToolWhois.m_whois_result.get(ip).keySet() ){
                if ( ToolWhois.m_whois_result.get( ip ).get( server ).getQueryResult() == null ) continue;
                if ( ToolWhois.m_whois_result.get( ip ).get( server ).getDate() == null ) continue;
                int line_id = j.newLine();
                j.set( line_id, "ip", ip);
                j.set( line_id, "server", server);
                j.set( line_id, "result", ToolWhois.m_whois_result.get( ip ).get( server ).getQueryResult() );
                j.set( line_id, "date", ToolWhois.m_whois_result.get( ip ).get( server ).getDate() );
            }
        }
        j.save( new File( ToolWhois.m_file ) );
        ToolWhois.m_must_save = false;
    }
}
