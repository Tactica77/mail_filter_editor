package jp.d77.java.mail_filter_editor.BasicIO;

import java.nio.charset.StandardCharsets;

import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriUtils;

/**
 * Web画面へ出力する様々な文字列を、一時的に蓄える為のクラス
 */
public class HtmlString {
    private String ResultString = "";

    /**
     * init func
     * @return
     */
    static public HtmlString init(){
        return new HtmlString();
    }

    /**
     * init func add to string
     * @param add_string
     * @return
     */
    static public HtmlString init( String add_string ){
        return new HtmlString( add_string );
    }

    /**
     * Constructor
     */
    public HtmlString() {
    }

    /**
     * Constructor add to string
     * @param add_string
     */
    public HtmlString( String add_string ) {
        this();
        this.ResultString += add_string;
    }
    
    /**
     * result string
     */
    public String toString(){
        return this.ResultString;
    }

    /**
     * ResultStringが空かを確認する
     * @return true=空
     */
    public boolean isEmpty(){
        if ( this.ResultString.equals("") ){
            return true;
        }
        return false;
    }

    /**
     * 
     * @param indent 文字列の前に付与するインデント。インデントは
     * @param add_string
     * @return
     */
    public HtmlString addString( int indent, String add_string ){
        return this.addString( HtmlString.sp(indent) + add_string );
    }

    public HtmlString addStringCr( int indent, String add_string ){
        return this.addStringCr( HtmlString.sp(indent) + add_string );
    }

    public HtmlString addStringBr( int indent, String add_string ){
        return this.addStringBr( HtmlString.sp(indent) + add_string );
    }

    public HtmlString addString( String add_string ){
        this.ResultString += add_string;
        return this;
    }

    public HtmlString addStringCr( String add_string ){
        if ( this.ResultString.equals("") ){
            this.ResultString = add_string;
        }else{
            this.ResultString += this.cr() + add_string;
        }
        return this;
    }

    public HtmlString addStringBr( String add_string ){
        if ( this.ResultString.equals("") ){
            this.ResultString = add_string;
        }else{
            this.ResultString += "<BR>" + this.cr() + add_string;
        }
        return this;
    }

    public String cr(){
        return "\n";
    }

    //https://qiita.com/rubytomato@github/items/4eff1ae1bcb6af1c8732

    static public String HtmlEscape( String s ){
        return HtmlUtils.htmlEscape( s );
    }

    static public String HtmlEscapeBr( String s ){
        String res = "";
        for( String v: s.split("\n") ){
            res += HtmlUtils.htmlEscape( v ) + "<BR>\n";
        }
        return res;
    }

    static public String HtmlUnescape( String s ){
        return HtmlUtils.htmlUnescape( s );
    }

    static public String UriEscape( String s ){
        return UriUtils.encode( s,StandardCharsets.UTF_8.name() );
    }

    static public String UriUnescape( String s ){
        return UriUtils.decode( s,StandardCharsets.UTF_8.name() );
    }

    public static String sp(int r){
        String res = "";
        for ( int i = 0; i < r; i++ ){
            res += "  ";
        }
        return res;
    }    

    public static String h( int level, String s ){
        return "<H" + level + ">" + s + "</H" + level + ">\n";
    }
}
