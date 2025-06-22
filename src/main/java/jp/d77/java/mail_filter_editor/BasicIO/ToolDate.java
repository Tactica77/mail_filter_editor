package jp.d77.java.mail_filter_editor.BasicIO;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 日時変換関連ライブラリ
 * @since 1.0.0 2025.06.15
 */
public class ToolDate {
    /**
     * LocalDateTimeを指定の形式に変換する。変換できない場合は「-」を返す。
     * @param input 変換対象の文字列（YYYYMM形式）
     * @param pattern 結果パターン。uuuuMMdd、HH:mm:ssなど
     * @return 変換後の文字列。変換できない場合は「-」
     * @since 2025.06.16
     */
    public static Optional<String> Fromat(LocalDateTime input, String pattern ){
        if (input == null) return Optional.empty();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( pattern );
            return Optional.ofNullable( input.format(formatter) );
        }catch (IllegalArgumentException | DateTimeException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * LocalDateを指定の形式に変換する。
     * @param input 変換対象の文字列（YYYYMM形式）
     * @param pattern 結果パターン。uuuuMMdd、HH:mm:ssなど
     * @return 変換後の文字列。
     * @since 2025.06.15
     */
    public static Optional<String> Fromat(LocalDate input, String pattern ){
        if (input == null) return Optional.empty();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( pattern );
            return Optional.ofNullable( input.format(formatter) );
        }catch (IllegalArgumentException | DateTimeException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * LocalTimeを指定の形式に変換する。変換できない場合は「-」を返す。
     * @param input 変換対象の文字列（YYYYMM形式）
     * @param pattern 結果パターン。uuuuMMdd、HH:mm:ss.SSSなど
     * @return 変換後の文字列。変換できない場合は「-」
     * @since 2025.06.15
     */
    public static Optional<String> Fromat(LocalTime input, String pattern ){
        if (input == null) return Optional.empty();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( pattern );
            return Optional.ofNullable( input.format(formatter) );
        }catch (IllegalArgumentException | DateTimeException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * 文字列からLocalTimeへ変換。
     * @param timeStr
     * @return
     */
    public static Optional<LocalTime> Str2LocalTime(String timeStr) {
        try {
            LocalTime time = LocalTime.parse(timeStr);
            return Optional.ofNullable( time );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 文字列からLocalDateへ変換。yyyy/MM/dd、yyyy年M月d日、yyyy-MM-dd -> yyyy-MM-dd
     * 日が無い場合は01日を補完する
     * @param originalDate
     * @return
     */
    public static Optional<LocalDate> YMD2LocalDate(String dateStr) {
        if ( dateStr == null ) return Optional.empty();
        if ( dateStr.equals("") ) return Optional.empty();

        // 年月だけの形式（補完用）
        if (dateStr.matches("\\d{4}-\\d{2}")) {           // 例: 2024-04
            dateStr += "-01";
        } else if (dateStr.matches("\\d{6}")) {           // 例: 202404
            dateStr += "01";                              // → 20240401
        } else if (dateStr.matches("\\d{4}年\\d{2}月")) { // 例: 2024年04月
            dateStr += "01日";                            // → 2024年04月01日
        }

        // 対応フォーマット
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT));
        formatters.add(DateTimeFormatter.ofPattern("uuuu/M/d").withResolverStyle(ResolverStyle.STRICT));
        formatters.add(DateTimeFormatter.ofPattern("uuuu年M月d日").withResolverStyle(ResolverStyle.STRICT));

        formatters.add(DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT));
        formatters.add(DateTimeFormatter.ofPattern("uuuu/MM/dd").withResolverStyle(ResolverStyle.STRICT));
        formatters.add(DateTimeFormatter.ofPattern("uuuu年MM月dd日").withResolverStyle(ResolverStyle.STRICT));
        formatters.add(DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return Optional.ofNullable( LocalDate.parse(dateStr, formatter) );
            } catch (DateTimeParseException e) {
                // 無視して次へ
            }
        }

        // 全形式失敗
        return Optional.empty();
    }

    /**
     * 年度を返す
     * @param newDate
     * @return 2025など
     */
    public static Integer FYear( LocalDate newDate ){
        // 年度を取得
        int year = newDate.getYear();
        int month = newDate.getMonthValue();
        
        // 年度の計算：4月より前なら前の年が年度
        return (month < 4) ? (year - 1) : year;
    }

    /**
     * 月初を返す
     * @param input
     * @return 2025-06-01など
     */
    public static LocalDate firstOfMonth( LocalDate input ){
        return input.withDayOfMonth(1);
    }

    /**
     * 月末を返す
     * @param input
     * @return 2025-06-30など
     */
    public static LocalDate lastOfMonth( LocalDate input ){
        return input.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * LocalDate型からCalendar型へ変換
     * @param ymd
     * @return
     */
    public static Calendar LocalDate2Calendar( LocalDate ymd ){
        // LocalDate → Date
        Date date = Date.from( ymd.atStartOfDay(ZoneId.systemDefault()).toInstant() );

        // Date → Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);    
        return calendar;    
    }

}
