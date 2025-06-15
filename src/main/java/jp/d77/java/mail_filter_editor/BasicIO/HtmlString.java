package jp.d77.java.mail_filter_editor.BasicIO;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Optional;

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

    //******************************************************************************
    // 標準画面出力用の変換関数
    //******************************************************************************
    /**
     * 数値をカンマ区切りの文字列に変換する
     * @param f 変換対象の数値
     * @return カンマ区切りの文字列
     */
    public static String FromatedNum(int f){
        return HtmlString.FromatedNum( Float.valueOf(f) );
    }

    public static String FromatedNum(Float value) {
        if (value == null) return "-";

        if (value == value.longValue()) {
            // 小数がゼロ：整数形式
            DecimalFormat intFormat = new DecimalFormat("#,##0");
            return intFormat.format(value);
        } else {
            // 小数あり：小数第2位まで
            DecimalFormat floatFormat = new DecimalFormat("#,##0.00");
            return floatFormat.format(value);
        }
    }

    /**
     * 文字列が数字のみで構成されているかを判定する
     * @param str 判定対象の文字列
     * @return 数字のみならtrue、そうでなければfalse
     */
    public static boolean isNumeric(String str) {
        return str.matches("\\d+");  // 数字のみ（0-9）
    }

    public static String FromatedNumNoCnm(Float value) {
        if (value == null) return "-";

        if (value == value.longValue()) {
            // 小数がゼロ：整数形式
            DecimalFormat intFormat = new DecimalFormat("0");
            return intFormat.format(value);
        } else {
            // 小数あり：小数第2位まで
            DecimalFormat floatFormat = new DecimalFormat("0.00");
            return floatFormat.format(value);
        }
    }

    //******************************************************************************
    // Stringから保存用形式へ変換
    //******************************************************************************

    /**
     * 文字列からFloat
     * @param str 変換失敗はempty
     * @return
     */
    public static Optional<Float> Str2Float(String str){
        if ( str == null ) return Optional.empty();
        if ( str.equals("") ) return Optional.empty();
        if ( str.equals("-") ) return Optional.empty();
        if ( str.equals("--") ) return Optional.empty();
        
        // 半角に変換（全角の「－」や「．」に備える場合は追加対応も）
        String normalized = str.trim();

        // 数字・符号・小数点・カンマ以外を除去（通貨記号、単位など）
        //String cleaned = normalized.replaceAll("[^\\d+\\-.,]", "");
        String cleaned = normalized.replaceAll("[^\\d+\\-Ee.,]", "");

        // カンマを除去
        cleaned = cleaned.replace(",", "");

        try {
            return Optional.ofNullable( Float.parseFloat(cleaned) );
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static String h( int level, String s ){
        return "<H" + level + ">" + s + "</H" + level + ">\n";
    }
}
