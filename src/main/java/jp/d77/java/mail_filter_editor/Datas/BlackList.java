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
import java.util.ArrayList;

import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;

public class BlackList {
    private WebConfig   m_config;
    private ArrayList<BlackListData>    m_datas;

    public BlackList( WebConfig cfg ){
        this.m_config = cfg;
        this.init();
    }

    public void init(){
        this.m_datas = new ArrayList<BlackListData>();
    }

    public ArrayList<BlackListData> getDatas(){
        return this.m_datas;
    }

    public boolean save(){
        String filename = this.m_config.getDataFilePath() + "/block_list_black.txt";
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
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for ( BlackListData bld: this.m_datas ){
                writer.write( bld.getLine() );
                writer.newLine();  // 改行
            }
            this.m_config.alertInfo.addStringBr( "ファイルを書き出しました:" + filename );
        } catch (IOException e) {
            this.m_config.alertError.addStringBr( "ファイルの書き出しに失敗しました:" + filename + " e=" + e.getMessage() );
            e.printStackTrace();
        }

        return true;
    }

    public boolean load(){
        this.init();
        String filename = this.m_config.getDataFilePath() + "/block_list_black.txt";
        Debugger.LogPrint( "file=" + filename );

        try (BufferedReader br = new BufferedReader(new FileReader( filename ))) {
            String line;

            while ((line = br.readLine()) != null) {
                BlackListData bld = new BlackListData();
                if ( bld.set( line ) ){
                    this.m_datas.add(bld);
                }
            }
        } catch (FileNotFoundException e) {
            this.m_config.alertError.addStringBr( "ブラックリストが見つかりませんでした:" + filename + " e=" + e.getMessage() );
        } catch (IOException e) {
            e.printStackTrace();
            this.m_config.alertError.addStringBr( "ブラックリストの読み込みに失敗しました:" + filename + " e=" + e.getMessage() );
            return false;
        }

        return true;
    }
}
