package jp.d77.java.mail_filter_editor.Datas;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.d77.java.mail_filter_editor.BasicIO.Debugger;

public class JsonIO {
    public enum DATA_TYPE {
        TEXT
        ,YMD
        ,NUM
        ,NULL
    }
    public class JsonProp{
        private DATA_TYPE   m_type = DATA_TYPE.NULL;
        private String      m_sLabel;
        public JsonProp( String sLabel, DATA_TYPE type){
            this.m_sLabel = sLabel;
            this.m_type = type;
        }
        public JsonProp( DATA_TYPE type ){
            this.m_type = type;
        }
        public DATA_TYPE getType(){return this.m_type;}
        public JsonProp setLabel( String label ){
            this.m_sLabel = label;
            return this;
        }
        public Optional<String> getLabel(){
            return Optional.ofNullable( this.m_sLabel );
        }
    }

    private LinkedHashMap<String,JsonProp>    m_props;          // プロパティ(key, prop)
    private TreeMap<Integer,HashMap<String,JsonData>> m_datas;  // データ(idx, Key, Value)
    private String m_sDbId;             // DBID
    private String m_sDbName;           // DB名

    /**
     * コンストラクタ
     * @param dbid
     */
    public JsonIO( String dbid ){
        this.m_sDbId = dbid;
        this.setDbName(dbid);
        this.m_props = new LinkedHashMap<String,JsonProp>();
        this.initDatas();
    }

    public JsonIO( String dbid, String name ){
        this.m_sDbId = dbid;
        this.setDbName(name);
        this.m_props = new LinkedHashMap<String,JsonProp>();
        this.initDatas();
    }

    public JsonIO setDbName( String name ){
        this.m_sDbName = name;
        return this;
    }

    public void setProp( String key, DATA_TYPE dt ){
        if ( ! this.m_props.containsKey( key ) ){
            this.m_props.put( key, new JsonProp( dt ));
        }
    }
    public void setLabel( String key, String label ){
        if ( ! this.m_props.containsKey( key ) ) return;
        this.m_props.get( key ).setLabel( label );
    }

    public Optional<String> getLabel( String key ){
        if ( ! this.m_props.containsKey( key ) ) return Optional.empty();
        return this.m_props.get( key ).getLabel();
    }

    public String getId(){  return this.m_sDbId;    }
    public String getName(){    return this.m_sDbName;}

    public TreeMap<Integer,HashMap<String,JsonData>> getDatas(){
        return this.m_datas;
    }

    public int newLine(){
        if ( this.m_datas == null || this.m_datas.isEmpty() ) return 0;
        return this.m_datas.lastKey() + 1;
    }

    private boolean setData( int line_id, String key ){
        if ( ! this.m_props.containsKey(key) ){
            Debugger.LogPrint( "ERROR: Undefined key: " + key );
            return false;
        }
        if ( this.m_datas == null ) this.initDatas();
        if ( ! this.m_datas.containsKey( line_id ) ) this.m_datas.put( line_id, new HashMap<String,JsonData>() );
        if ( ! this.m_datas.get( line_id ).containsKey(key) ) this.m_datas.get(line_id).put(key, new JsonData( this.m_props.get(key) ) );
        return true;
    }

    public void set( int line_id, String key, String value ){
        if ( ! this.setData(line_id, key) ) return;
        this.m_datas.get(line_id).get(key).setText(value);
    }

    public void set( int line_id, String key, Float value ){
        if ( ! this.setData(line_id, key) ) return;
        this.m_datas.get(line_id).get(key).setNum(value);
    }

    public void set( int line_id, String key, LocalDate value ){
        if ( ! this.setData(line_id, key) ) return;
        this.m_datas.get(line_id).get(key).setYMD(value);
    }

    public void initDatas(){
        this.m_datas = new TreeMap<Integer,HashMap<String,JsonData>>();
    }

    /**
     * ファイル読み込み
     * @return
     */
    public boolean load( File file ){
        Debugger.TracePrint();
        this.initDatas();

        int lc = 0;
        try {
            // Open & Load File
            ObjectMapper mapper = new ObjectMapper();

            // JSONを Map<String, Map<String, String>> に読み込む
            Map<String, Map<String, String>> data = mapper.readValue(
                file,
                new TypeReference<>() {}
            );

            for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                String sid = entry.getKey();
                int id = Integer.parseInt( sid );
                Map<String, String> record = entry.getValue();
                this.m_datas.put( id, new HashMap<String,JsonData>() );

                for ( String key: this.m_props.keySet() ){
                    JsonData jd = new JsonData( this.m_props.get(key) );
                    jd.set( record.getOrDefault( key, null) );
                    this.m_datas.get(id).put(key, jd);
                }
                lc++;
            }
            Debugger.LogPrint( "Loaded file=" + file + " lc=" + lc );
        }catch(Exception e){
            Debugger.LogPrint( "Loaded Error file=" + file + " lc=" + lc );
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * ファイル書き込み
     * @return
     */
    public boolean save( File file ){
        Debugger.TracePrint();

        TreeMap<Integer,HashMap<String,String>> save_datas = new TreeMap<Integer,HashMap<String,String>>();
        for( Integer line_id: this.m_datas.keySet() ){
            for ( String key: this.m_props.keySet() ){
                if ( ! save_datas.containsKey( line_id ) ) save_datas.put( line_id, new HashMap<String,String>() );
                if ( this.m_datas.get(line_id).get(key).getString().isEmpty() ) continue;
                
                save_datas.get( line_id ).put( key, this.m_datas.get(line_id).get(key).getString().get() );
            }
        }

        try {
            // すでにファイルが存在していればバックアップを作成
            if (file.exists()) {
                File bkfile = new File( file.toPath() + ".bak" );
                Files.copy(file.toPath(), bkfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Debugger.LogPrint( "backup dbf=" + bkfile );
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // 整形出力
            objectMapper.writeValue( file , save_datas );
            Debugger.LogPrint( "saved: " + file );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Debugger.LogPrint( "saved: " + file );
            return false;
        }
    }
}
