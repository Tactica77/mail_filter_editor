package jp.d77.java.mail_filter_editor.Pages;

import java.time.LocalDate;

import jp.d77.java.mail_filter_editor.BasicIO.ToolNums;
import jp.d77.java.mail_filter_editor.BasicIO.WebConfig;
import jp.d77.java.mail_filter_editor.Datas.BlockHistry;

public class CliUpdateBlockData extends AbstractWebPage implements InterfaceWebPage {
    private BlockHistry    m_datas;

    public CliUpdateBlockData(WebConfig cfg) {
        super(cfg);
    }

    /**
     * 1:init
     */
    @Override
    public void init() {
        this.m_config.setPageTitle( this.m_config.getPageTitle() + " - UpdateBlockData" );
    }

    /**
     * 2:load
     */
    @Override
    public void load() {
        String mode = this.getConfig().getMethod("mode").orElse( "0" );
        int day = -1;
        if ( ToolNums.isNumeric(mode) ) day = ToolNums.Str2Int(mode).orElse(-1);
        if ( day < 0 ){
            this.getConfig().alertError.addStringBr( "modeの指定が異常です" );
            return;
        }

        // 表示範囲始
        LocalDate target_date = LocalDate.now().plusDays( day * (-1) );

        // 初期化～読み込み
        this.m_datas = new BlockHistry( this.m_config );
        this.m_datas.init();

        if ( this.m_datas.load( target_date ) ) {
            this.m_config.alertInfo.addStringBr( target_date + "を更新しました。" );    
        }
    }

    /**
     * 3:post_save_reload
     */
    @Override
    public void post_save_reload() {
    }

    /**
     * 4:proc
     */
    @Override
    public void proc() {
    }

    /**
     * 5:displayHeader
     */
    @Override
    public void displayHeader(){
        super.displayHeader();
    }

    /**
     * 6:displayNavbar
     */
    @Override
    public void displayNavbar() {
    }

    /**
     * 7:displayInfo
     */
    @Override
    public void displayInfo(){
        super.displayInfo();
    }

    /**
     * 8:displayBody
     */
    @Override
    public void displayBody(){
        super.displayBody();
    }

    /**
     * 9:displayBottomInfo
     */
    @Override
    public void displayBottomInfo(){
        super.displayBottomInfo();
    }

    /**
     * 10:displayFooter
     */
    @Override
    public void displayFooter(){
        super.displayFooter();
    }
}
