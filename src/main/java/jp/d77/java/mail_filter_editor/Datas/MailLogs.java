package jp.d77.java.mail_filter_editor.Datas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNums;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;

public class MailLogs {
    private WebConfig   m_config;
    private LinkedHashMap<String,LinkedHashMap<Integer,MailLogData>>    m_datas;

    public MailLogs( WebConfig cfg ){
        this.m_config = cfg;
        this.init();
    }

    public String[] getYmdList( String YMD ){
        String[] r = this.m_datas.keySet().toArray( new String[0] );
        Arrays.sort( r );
        return r;
    }

    public HashMap<Integer,MailLogData> getData( String YMD ){
        return this.m_datas.get( YMD );
    }

    public void init(){
        this.m_datas = new LinkedHashMap<String,LinkedHashMap<Integer,MailLogData>>();
    }

    public boolean load( LocalDate target_date ){
        return this.load( target_date, -1 );
    }

    public boolean load( LocalDate target_date, Integer idx ){
        String YM = ToolDate.Fromat( target_date, "uuuuMM" );
        String YMD = ToolDate.Fromat( target_date, "uuuuMMdd" );

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
                

                if ( this.m_datas.containsKey( YMD ) == false ){
                    this.m_datas.put( YMD, new LinkedHashMap<Integer,MailLogData>() );
                }

                if ( this.m_datas.get( YMD ).containsKey( id ) == false ){
                    this.m_datas.get( YMD ).put( id, new MailLogData() );
                }

                this.m_datas.get( YMD ).get( id ).set( id, target_date, columns[1] );
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
    
}
