package jp.d77.java.mail_filter_editor.BasicIO;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import jp.d77.java.mail_filter_editor.MailFilterEditorApplication;
import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.BasicIO.ProgramProp;
import jp.d77.java.tools.HtmlIO.HtmlString;

public class WebConfig {
    private HashMap<String,String> Methods;
    private HashMap<String,ArrayList<String>> MethodLists;

    public String ProgramName = "";
    public String ProgramVersion = "";
    public String Uri = "";

    private String m_sPageTitle = "Mail Filter Editor";

    public HtmlString addHeader = HtmlString.init();
    public HtmlString alertBottomInfo = HtmlString.init();
    public HtmlString alertDebug = HtmlString.init();
    public HtmlString alertInfo = HtmlString.init();
    public HtmlString alertError = HtmlString.init();
    private boolean m_bDebug = false;
   
    // コンストラクタ
    public WebConfig( String Uri ){
        this.Methods = new HashMap<String,String>();
        this.MethodLists = new HashMap<String,ArrayList<String>>();
        this.Uri = Uri;
        this.ProgramName = ProgramProp.getProgram();
        this.ProgramVersion = ProgramProp.getVersion();
    }

    //******************************************************************************
    // プロパティ
    //******************************************************************************
    public WebConfig setPageTitle( String sPageTitle ){  this.m_sPageTitle = sPageTitle;return this;}
    //public AssetsAdmConfig setJName( String JName ){    this.m_sJName = JName;  return this;}
    public String getPageTitle() { return this.m_sPageTitle; }

    public String getDataFilePath(){
        if ( MailFilterEditorApplication.getFilePath().isEmpty() ){
            return FileSystems.getDefault().getPath("").toAbsolutePath().toString() + "/../filter2/";
        }
        return MailFilterEditorApplication.getFilePath().get();
    }

    // Method追加
    public void addMethod( String name, String value ){
        if ( value == null ){
        }else if ( value.isEmpty() || value.isBlank() ){
        }else{
            Debugger.InfoPrint( "name=" + name + " value=" + value);
            this.Methods.put( name, value );
            this.alertBottomInfo.addStringBr("addMethod name=" + name + " value=" + value);
        }
    }

    public void addMethodLists( String name, String[] values ){
        if ( values == null ){
        }else if ( values.length <= 0 ){
        }else{
            this.MethodLists.put(name, new ArrayList<String>());
            for ( String v: values ){
                Debugger.InfoPrint( "name=" + name + " value=" + v);
                this.MethodLists.get(name).add(v);
                this.alertBottomInfo.addStringBr("addMethodLists name=" + name + " value=" + v);
            }
        }
    }

    // Method取得
    public Optional<String> getMethod( String name ){
        if ( ! this.Methods.containsKey( name ) ) return Optional.empty();
        return Optional.ofNullable( this.Methods.get(name) );
    }

    public String[] getMethodLists( String name ){
        if ( ! this.MethodLists.containsKey( name ) ) return new String[0];
        return this.MethodLists.get(name).toArray( new String[0] );
    }

    // Method空チェック
    public boolean MethodIsEmpty( String name ){
        return this.Methods.containsKey(name);
    }

    // メソッドKey一覧取得
    public Set<String> enumMethod(){
        return this.Methods.keySet();
    }

    // debug
    public void onDebug(){this.m_bDebug = true;}
    public void offDebug(){this.m_bDebug = false;}
    public boolean isDebug(){return this.m_bDebug;}

    public HtmlString getAddHeader(){
        return this.addHeader;
    }
}
