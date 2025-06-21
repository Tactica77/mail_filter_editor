package jp.d77.java.mail_filter_editor.BasicIO;

import java.text.DecimalFormat;
import java.util.Optional;

public class ToolNums {
    /**
     * 数値をカンマ区切りの文字列に変換する
     * @param f 変換対象の数値
     * @return カンマ区切りの文字列
     */
    public static String FromatedNum(int f){
        return ToolNums.FromatedNum( Float.valueOf(f) );
    }

    /**
     * 文字列が数字のみで構成されているかを判定する
     * @param str 判定対象の文字列
     * @return 数字のみならtrue、そうでなければfalse
     */
    public static boolean isNumeric(String str) {
        return str.matches("\\d+");  // 数字のみ（0-9）
    }

    /**
     * Float型を文字列型へ変換する(カンマ区切り)
     * @param value
     * @return
     */
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
     * Float型を文字列型へ変換する(カンマ区切りではない)
     * @param value
     * @return
     */
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

    /**
     * 文字列からInt
     * @param str 変換失敗はempty
     * @return
     */
    public static Optional<Integer> Str2Int(String str){
        if ( str == null ) return Optional.empty();
        if ( str.equals("") ) return Optional.empty();
        if ( str.equals("-") ) return Optional.empty();
        if ( str.equals("--") ) return Optional.empty();
        
        // 半角に変換（全角の「－」や「．」に備える場合は追加対応も）
        String normalized = str.trim();

        // 数字・符号・カンマ以外を除去（通貨記号、単位など）
        String cleaned = normalized.replaceAll("[^\\d+\\-Ee,]", "");

        // カンマを除去
        cleaned = cleaned.replace(",", "");

        try {
            return Optional.ofNullable( Integer.parseInt(cleaned) );
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
