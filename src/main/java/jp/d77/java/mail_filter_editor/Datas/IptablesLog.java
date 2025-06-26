package jp.d77.java.mail_filter_editor.Datas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;

public class IptablesLog {
    public class IptablesLogData {
        private int m_classA;
        private String m_cidr;
        private String m_code;
        public int getClassA(){ return m_classA; }
        public Optional<String> getCidr(){ return Optional.ofNullable( this.m_cidr ); }
        public Optional<String> getCode(){
            if ( this.m_code == null ) return Optional.empty();
            if ( this.m_code.startsWith( "," ) ) return Optional.ofNullable( this.m_code.substring(1) );
            return Optional.ofNullable( this.m_code );
        }
    }

    private WebConfig   m_config;
    private ArrayList<IptablesLogData>  m_datas;

    public IptablesLog( WebConfig cfg ){
        this.m_config = cfg;
        this.init();
    }

    public void init(){
        this.m_datas = new ArrayList<IptablesLogData>();
    }

    public ArrayList<IptablesLogData> getDatas(){
        return this.m_datas;
    }

    public boolean load(){
        this.init();
        String filename = this.m_config.getDataFilePath() + "/make_iptables.log";
        Debugger.LogPrint( "file=" + filename );

        try (BufferedReader br = new BufferedReader(new FileReader( filename ))) {
            String line;

            int ok = 0;
            int ng = 0;
            while ((line = br.readLine()) != null) {
                String[] items = line.split(" ");
                if ( items.length < 21 ) {
                    ng++;
                    continue;
                }else{
                    ok++;
                }
                IptablesLogData ild = new IptablesLogData();
                this.m_datas.add(ild);
                ild.m_cidr = items[12];
                ild.m_code = items[20];
                ild.m_classA = this.ClassA( ild.m_cidr );
            }
            Debugger.LogPrint( "loaded ok=" + ok + " ng=" + ng);

        } catch (FileNotFoundException e) {
            this.m_config.alertError.addStringBr( "IpTablesログが見つかりませんでした:" + filename + " e=" + e.getMessage() );
        } catch (IOException e) {
            e.printStackTrace();
            this.m_config.alertError.addStringBr( "IpTablesログの読み込みに失敗しました:" + filename + " e=" + e.getMessage() );
            return false;
        }

        return true;
    }

    public int ClassA( String cidr ){
        try {
            String[] a = cidr.split("\\.");
            return Integer.parseInt( a[0] );
        } catch ( Exception e ){
            return -1;
        }

    }
}
