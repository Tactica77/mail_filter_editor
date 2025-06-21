package jp.d77.java.mail_filter_editor.Datas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;

public class BlockedDatas {
//08:12:16<>198.163.193.63<>198.163.192.0/20<>code_550<>NL<>Sagawa-exp.lostari60@gzpp.com<>ml@d77.jp<>RIPE<>
    private WebConfig   m_config;
    private HashMap<String,BlockData>   m_datas;
    private HashMap<String,ArrayList<BlockData>>  m_filedatas;
    private HashMap<String, Integer>    m_score_i;  // IP
    private HashMap<String, Integer>    m_score_o;  // ORG
    private HashMap<String, Integer>    m_score_r;  // Range
    
    public BlockedDatas( WebConfig cfg ){
        this.m_config = cfg;
    }

    public void init(){
        this.m_datas = new HashMap<String,BlockData>();
        this.m_filedatas = new HashMap<String,ArrayList<BlockData>>();
        this.m_score_i = new HashMap<String, Integer>();
        this.m_score_o = new HashMap<String, Integer>();
        this.m_score_r = new HashMap<String, Integer>();
    }

    public HashMap<String,BlockData> getDatas(){
        return this.m_datas;
    }

    public boolean save( LocalDate target_date ){
        String YM = ToolDate.Fromat( target_date, "uuuuMM" );
        String YMD = ToolDate.Fromat( target_date, "uuuuMMdd" );
        String filename = this.m_config.getDataFilePath() + "/log/" + YM + "/block_ip_" + YMD + ".log";
        File f_filename = new File( filename );
        Debugger.LogPrint( "file=" + filename );

        // すでにファイルが存在していればバックアップを作成
        File f_bkfile = new File( filename + ".bak" );
        try {
            if ( f_filename.exists()) {
                Files.copy( f_filename.toPath(), f_bkfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Debugger.LogPrint( "backup " + f_bkfile );
            }
        } catch ( Exception e) {
            e.printStackTrace();
            this.m_config.alertError.addStringBr( "ファイルのバックアップに失敗しました:" + filename + " -> " + f_bkfile + " e=" + e.getMessage() );
            return false;
        }

        if ( this.m_filedatas.containsKey( YMD ) == false ) {
            this.m_config.alertError.addStringBr( "保存するデータが存在しません。" );
            return false;
        }

        ArrayList<BlockData> bds = this.m_filedatas.get(YMD);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for ( BlockData bd: bds ){
                writer.write( bd.getSaveLine() );
                writer.newLine();  // 改行
            }
            this.m_config.alertInfo.addStringBr( "ファイルを書き出しました:" + filename );
        } catch (IOException e) {
            this.m_config.alertError.addStringBr( "ファイルの書き出しに失敗しました:" + filename + " e=" + e.getMessage() );
            e.printStackTrace();
        }

        return true;
    }

    public boolean load( LocalDate target_date ){
        String YM = ToolDate.Fromat( target_date, "uuuuMM" );
        String YMD = ToolDate.Fromat( target_date, "uuuuMMdd" );

        String filename = this.m_config.getDataFilePath() + "/log/" + YM + "/block_ip_" + YMD + ".log";
        Debugger.LogPrint( "file=" + filename );
        try (BufferedReader br = new BufferedReader(new FileReader( filename ))) {
            String line;

            while ((line = br.readLine()) != null) {
                // 行を「<>」で分割
                String[] columns = line.split("<>");
                if ( columns.length < 2 ) continue;

                // 日時を作成
                LocalDateTime dt = this.cnvDateTime( target_date, columns[0] );
                if ( dt == null ) continue;

                BlockData d;
                if ( !this.m_filedatas.containsKey( YMD ) ) {
                    this.m_filedatas.put( YMD, new ArrayList<BlockData>() );
                }
                d = new BlockData();
                this.m_filedatas.get( YMD ).add(d);

                // 0: date time
                d.m_datetime.add(dt);

                // 1: ip
                d.m_ip = columns[1];
                d.countup();

                // 2: range
                if ( columns.length >= 3 ) d.m_range = columns[2];

                // 3: codes
                if ( columns.length >= 4 ) d.m_error_codes.add(columns[3]);

                // 4: cc
                if ( columns.length >= 5 ) d.m_country_code = columns[4];

                // 5: from
                if ( columns.length >= 6 ) d.m_from = columns[5];
                
                // 6: to
                if ( columns.length >= 7 ) d.m_to = columns[6];

                // 7: org
                if ( columns.length >= 8 ) d.m_org = columns[7];
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * スコアを作成
     */
    public void createScore(){
        this.m_datas = new HashMap<String,BlockData>();

        for ( String YMD: this.m_filedatas.keySet() ){
            Debugger.LogPrint( YMD );
            for ( BlockData bd_from: this.m_filedatas.get(YMD) ){
                if ( bd_from.getIp().isEmpty() ) continue;
                String idx = YMD + "_" + bd_from.getIp().get();

                BlockData bd_to;
                if ( this.m_datas.containsKey( idx ) ) {
                    bd_to = this.m_datas.get( idx );
                }else{
                    bd_to = new BlockData();
                    this.m_datas.put( idx, bd_to );
                }

                bd_to.m_datetime = bd_from.m_datetime;
                bd_to.m_ip = bd_from.m_ip;
                this.countScore( "IP", bd_to.m_ip );
                bd_to.m_range = bd_from.m_range;
                this.countScore( "CIDR", bd_to.m_range );
                for( String s: bd_from.m_error_codes ){
                    bd_to.m_error_codes.add( s );
                }
                bd_to.m_country_code = bd_from.m_country_code;
                bd_to.m_address_from_to.add( bd_from.m_from + "->" + bd_from.m_to );
                bd_to.m_org = bd_from.m_org;
                this.countScore( "ORG", bd_to.m_org );
                bd_to.countup();
            }
        }
    }

    /**
     * 日と時を結合してLocalDateTimeを返す
     * @param d
     * @param t
     * @return
     */
    public LocalDateTime cnvDateTime( LocalDate d, String t ){
        try {
            LocalTime at = LocalTime.parse( t );
            return LocalDateTime.of( d, at );
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public String getScore( String ip, String cidr, String org ){
        String ret = "";
        //if ( ip.equals("") || ip.equals("-") || !this.m_score_i.containsKey(ip) ) ret += "cnt:0";
        //else ret += "cnt:" + this.m_score_i.get(ip);

        if ( org.equals("") || org.equals("-") || !this.m_score_o.containsKey(org) ) ret += "O:0";
        else ret += "O:" + this.m_score_o.get(org);

        if ( cidr.equals("") || cidr.equals("-") || !this.m_score_r.containsKey(cidr) ) ret += " R:0";
        else ret += " R:" + this.m_score_r.get(cidr);
        return ret;
    }

    public Integer getCountRange( String key ){
        return this.m_score_r.get(key);
    }

    public Integer getCountOrg( String key ){
        return this.m_score_o.get(key);
    }

    private void countScore( String type, String key ){
        if ( type.equals( "IP" ) ){
            if ( ! this.m_score_i.containsKey(key) ) this.m_score_i.put( key, 0 );
            this.m_score_i.put( key, this.m_score_i.get( key ) + 1 );

        }else if ( type.equals( "CIDR" ) ){
            if ( ! this.m_score_r.containsKey(key) ) this.m_score_r.put( key, 0 );
            this.m_score_r.put( key, this.m_score_r.get( key ) + 1 );

        }else if ( type.equals( "ORG" ) ){
            if ( ! this.m_score_o.containsKey(key) ) this.m_score_o.put( key, 0 );
            this.m_score_o.put( key, this.m_score_o.get( key ) + 1 );
        }
    }
}
