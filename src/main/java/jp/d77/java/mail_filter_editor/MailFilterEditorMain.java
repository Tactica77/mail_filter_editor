package jp.d77.java.mail_filter_editor;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.BasicIO.Debugger;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNums;
import jp.d77.java.mail_filter_editor.Pages.AbstractWebPage;
import jp.d77.java.mail_filter_editor.Pages.MailLog;
import jp.d77.java.mail_filter_editor.Pages.WebBlockEditor;
import jp.d77.java.mail_filter_editor.Pages.WebSubnets;
import jp.d77.java.mail_filter_editor.Pages.WebTop;
import jp.d77.java.mail_filter_editor.Pages.WebWhois;

//https://qiita.com/zumax/items/8effa97f338dd0224b22

@RestController
public class MailFilterEditorMain {
    @RequestMapping("/")  // ルートへこのメソッドをマップする
    public String mail_filter_editor( HttpServletRequest request ) {
        Debugger.startTimer();
        Debugger.LogPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new WebTop( new WebConfig( "/" ) );

        // Modeを取得
        web.getConfig().addMethod("mode", WebUtils.findParameterValue(request, "mode") );
        Map<String, Object> params;

        // フォーム投稿を取得(edit_から始まる項目を取得)
        params = WebUtils.getParametersStartingWith(request, "edit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                web.getConfig().addMethod("edit_" + e.getKey(), e.getValue().toString() );
            }
        }

        // フォーム投稿を取得(submit_から始まる項目を取得)
        params = WebUtils.getParametersStartingWith(request, "submit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                web.getConfig().addMethod("submit_" + e.getKey(), e.getValue().toString() );
            }
        }

        return this.procWeb( web );
    }

    @RequestMapping("/block_editor")  // ルートへこのメソッドをマップする
    public String BlockEditor( HttpServletRequest request ) {
        Debugger.startTimer();
        Debugger.LogPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new WebBlockEditor( new WebConfig( "/block_editor" ) );

        // Modeを取得
        web.getConfig().addMethod("mode", WebUtils.findParameterValue(request, "mode") );
        Map<String, Object> params;

        // フォーム投稿を取得(edit_から始まる項目を取得)
        params = WebUtils.getParametersStartingWith(request, "edit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                if ( e.getValue() instanceof String[] ){
                    // 配列の場合
                    web.getConfig().addMethodLists("edit_" + e.getKey(), (String[])e.getValue() );
                }else{
                    web.getConfig().addMethod("edit_" + e.getKey(), e.getValue().toString() );
                }
            }
        }

        // フォーム投稿を取得(submit_から始まる項目を取得)
        params = WebUtils.getParametersStartingWith(request, "submit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                web.getConfig().addMethod("submit_" + e.getKey(), e.getValue().toString() );
            }
        }

        return this.procWeb( web );
    }

    @RequestMapping("/whois")  // ルートへこのメソッドをマップする
    public String whois( HttpServletRequest request ) {
        Debugger.startTimer();
        Debugger.LogPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new WebWhois( new WebConfig( "/whois" ) );

        // Modeを取得
        web.getConfig().addMethod("ip", WebUtils.findParameterValue(request, "ip") );

        return this.procWeb( web );
    }

    @RequestMapping("/subnets")  // ルートへこのメソッドをマップする
    public String subnets( HttpServletRequest request ) {
        Debugger.startTimer();
        Debugger.LogPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new WebSubnets( new WebConfig( "/subnets" ) );

        // Modeを取得
        web.getConfig().addMethod("ip", WebUtils.findParameterValue(request, "ip") );

        return this.procWeb( web );
    }

    @RequestMapping("/mail_log")  // ルートへこのメソッドをマップする
    public String MailLog( HttpServletRequest request ) {
        Debugger.startTimer();
        Debugger.LogPrint( "------ START ------" );

        // 表示用クラスの設定
        AbstractWebPage web = new MailLog( new WebConfig( "/mail_log" ) );

        // Modeを取得
        web.getConfig().addMethod("mode", WebUtils.findParameterValue(request, "mode") );
        Map<String, Object> params;

        // フォーム投稿を取得(edit_から始まる項目を取得)
        params = WebUtils.getParametersStartingWith(request, "edit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                web.getConfig().addMethod("edit_" + e.getKey(), e.getValue().toString() );
            }
        }

        // フォーム投稿を取得(submit_から始まる項目を取得)
        params = WebUtils.getParametersStartingWith(request, "submit_");
        if (!params.isEmpty()) {
            for (Entry<String, Object> e : params.entrySet()) {
                web.getConfig().addMethod("submit_" + e.getKey(), e.getValue().toString() );
            }
        }

        return this.procWeb( web );
    }

    private String procWeb( AbstractWebPage Web ){
        Debugger.TracePrint();
        Web.init();
        Web.load();
        Web.post_save_reload();
        Web.proc();
        Web.displayHeader();
        Web.displayNavbar();
        Web.displayInfo();
        Web.displayBody();
        Web.displayBottomInfo();
        Web.displayFooter();
       Debugger.LogPrint( "------ Done bytes="  + ToolNums.FromatedNum( Web.toString().length() ) + " ------" );
        return Web.toString();
    }
}
