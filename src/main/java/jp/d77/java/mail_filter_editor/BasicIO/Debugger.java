package jp.d77.java.mail_filter_editor.BasicIO;

public class Debugger {
    private static Long m_start = null;
    private static Long m_savetime = null;

    private static Long m_start_ns = null;

    /**
     * 特定の文字列をログとしてコンソール出力する
     * @param s
     */
    public static void LogPrint( String s ){
        String sClass = Thread.currentThread().getStackTrace()[2].getClassName();
        String sMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
        String sClasses[] = sClass.split("\\.");
        if ( sMethod.equals( "<init>" ) ) { sMethod = "CONSTRUCTOR"; }
        System.out.println( Debugger.elapsedTimer() + "ms --> " + sClasses[sClasses.length - 1] + "> " + sMethod + "> " + s );
    }

    /**
     * このメソッドを書いたメソッドの名前などをログとしてコンソール出力
     */
    public static void TracePrint(){
        String sClass = Thread.currentThread().getStackTrace()[2].getClassName();
        String sMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
        String sClasses[] = sClass.split("\\.");
        if ( sMethod.equals( "<init>" ) ) { sMethod = "CONSTRUCTOR"; }
        
        System.out.println( Debugger.elapsedTimer() + "ms Trace: " + sClasses[sClasses.length - 1] + "> " + sMethod );
    }

    /**
     * カウンタータイマー開始。以後、ログに開始からの経過時間表示が自動付与される
     */
    public static void startTimer(){
        Debugger.m_start = System.currentTimeMillis();
        Debugger.m_start_ns = System.nanoTime();
    }

    /**
     * ログに表示するための経過時間(ms)を返す
     * @return
     */
    private static Long elapsedTimer(){
        if ( Debugger.m_start == null ) Debugger.startTimer();

        long end = System.currentTimeMillis();
        return end - Debugger.m_start;
    }

    public static void CountPerSecondLogPrint( String s ){
        if ( Debugger.m_savetime == null ) Debugger.m_savetime = System.currentTimeMillis();
        if ( Debugger.m_savetime + 1000 > System.currentTimeMillis() ) return;
        Debugger.LogPrint(s);
        Debugger.m_savetime = System.currentTimeMillis();
    }

    /**
     * 特定の文字列をログとしてコンソール出力するが表示される時間がns
     * @param s
     */
    public static void NanoTimerLogPrint( String s ){
        Long time_ns = System.nanoTime() - Debugger.m_start_ns;
        Debugger.LogPrint(time_ns + " ns: " + s);
    }

}
