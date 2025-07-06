package jp.d77.java.mail_filter_editor.Pages;

import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.tools.BasicIO.Debugger;
import jp.d77.java.tools.HtmlIO.BSOpts;
import jp.d77.java.tools.HtmlIO.BSS;
import jp.d77.java.tools.HtmlIO.BSSForm;
import jp.d77.java.tools.HtmlIO.HtmlString;

public abstract class AbstractWebPage{
    protected WebConfig     m_config;
    protected HtmlString    m_html;

    // コンストラクタ
    public AbstractWebPage( WebConfig cfg ){
        Debugger.TracePrint();
        this.m_config = cfg;
        this.m_html = new HtmlString();
    }

    // 1:init
    public abstract void init();

    // 2:load
    public abstract void load();

    // 3:post_save_reload
    public abstract void post_save_reload();

    // 4 proc
    public abstract void proc();

    // 5:displayHeader
    public void displayHeader(){
        Debugger.TracePrint();
        this.m_html.addString( BSS.getHeader( this.m_config.ProgramName, this.m_config.getAddHeader().toString() ) );
    }
    
    // 6:displayNavbar
    public abstract void displayNavbar();
    
    // 7:displayInfo
    public void displayInfo(){
        Debugger.TracePrint();
        this.m_html.addStringCr( "<DIV class=\"container-fluid\">")
            .addStringCr("<DIV class=\"row\">" );

            // Debug
        if ( ! this.m_config.alertDebug.isEmpty() && this.m_config.isDebug() ) {
            this.m_html.addString(
                BSSForm.alert( BSSForm.ALERT.DEBUG, this.m_config.alertDebug.toString() )
            );
        }

        // Error
        if ( ! this.m_config.alertError.isEmpty() ) {
            this.m_html.addString(
                BSSForm.alert( BSSForm.ALERT.ERROR, this.m_config.alertError.toString() )
            );
        }
        
        // Info
        if ( ! this.m_config.alertInfo.isEmpty() ) {
            this.m_html.addString(
                BSSForm.alert( BSSForm.ALERT.INFO, this.m_config.alertInfo.toString() )
            );
        }
    }

    // 8:displayBody
    public void displayBody(){
        Debugger.TracePrint();
        String title = this.m_config.getPageTitle();
        if ( this.m_config.getPageTitle().equals( "" ) ) title += this.m_config.ProgramName;
        m_html.addString(
            HtmlString.h(1, title )
        );
    }

    // 9:displayBottomInfo
    public void displayBottomInfo(){
        Debugger.TracePrint();
        this.m_html.addString(
            BSSForm.alert( BSSForm.ALERT.INFO, this.m_config.alertBottomInfo.toString() )
        );
        this.m_html.addStringCr( "</DIV>    <!-- container-fluid -->")
            .addStringCr("</DIV>    <!-- row -->" );
    }

    // 10:displayFooter
    public void displayFooter(){
        Debugger.TracePrint();
        this.m_html.addString( BSS.getFooter( this.m_config.ProgramName + " " + this.m_config.ProgramVersion ) );
    }

    public String toString(){
        return this.m_html.toString();
    }

    public HtmlString getHtml(){
        return this.m_html;
    }

    public WebConfig getConfig(){
        return this.m_config;
    }

    /**
     * Form Top
     * @param addforms
     */
    protected void displayFormTop(String... addforms ){
        Debugger.TracePrint();

        // Form Top
        this.m_html.addString(
            BSSForm.newForm()
                .formTop( this.m_config.Uri, false )
                .formInputHidden(
                    BSOpts.init("name", "mode" )
                    .set("value", this.m_config.getMethod("mode").orElse("-") )
                ).toString()
        );
        for( String add: addforms ){
            this.m_html.addString( add );
        }
    }

    /**
     * Form Bottom
     * @param html
     */
    protected void displayFormBtm(){
        Debugger.TracePrint();
        this.m_html.addString( BSSForm.newForm().formBtm().toString() );
    }

}
