package jp.d77.java.mail_filter_editor.Pages;

public interface InterfaceWebPage {
    // 1:init
    public abstract void init();

    // 2:load
    public abstract void load();

    // 3:post_save_reload
    public abstract void post_save_reload();

    // 4 proc
    public abstract void proc();

    // 5:displayHeader
    public void displayHeader();
    
    // 6:displayNavbar
    public abstract void displayNavbar();
    
    // 7:displayInfo
    public void displayInfo();

    // 8:displayBody
    public void displayBody();

    // 9:displayBottomInfo
    public void displayBottomInfo();

    // 10:displayFooter
    public void displayFooter();
}
