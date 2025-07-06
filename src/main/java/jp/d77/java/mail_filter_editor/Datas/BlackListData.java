package jp.d77.java.mail_filter_editor.Datas;

import java.util.Optional;

import jp.d77.java.mail_filter_editor.BasicIO.ToolNet;
import jp.d77.java.tools.BasicIO.Debugger;

public class BlackListData {
    private String  m_cidr;
    private String  m_add_date;
    private String  m_country_code;
    private String  m_org;
    private boolean m_enable;
    private String  m_line;
    private String  m_duplicate_cidr;

    /**
     * ファイルから読み込んだ行データを格納する
     * @param line
     * @return false = データとして無効
     */
    public boolean set( String line ){
        this.m_line = line;
        // タブ・スペースの混合を区切りにして4分割だけ行う（limit=5）
        String[] parts = line.split("[ \t]", 5);

        // 0: cidr
        if (parts.length < 1) return false;
        if ( parts[0].startsWith("#") ){
            this.m_enable = false;
            this.m_cidr = parts[0].substring(1);
        }else{
            this.m_enable = true;
            this.m_cidr = parts[0];
        }

        // 1: #
        // 2: add date
        if (parts.length < 3) return true;
        this.m_add_date = parts[2];

        // 3: country_code
        if (parts.length < 4) return true;
        this.m_country_code = parts[3];

        // 4: org
        if (parts.length < 5) return true;
        this.m_org = parts[4];
        return true;
    }

    public boolean isEnable(){  return this.m_enable;   }
    public String getLine(){    return this.m_line; }
    public String getCidr(){    return this.m_cidr; }
    public Optional<String> getAddDate(){    return Optional.ofNullable( this.m_add_date ); }
    public Optional<String> getCountryCode(){    return Optional.ofNullable( this.m_country_code ); }
    public Optional<String> getOrg(){    return Optional.ofNullable( this.m_org ); }

    public void set( String cidr, String add_date, String country_code, String org ){
        this.m_enable = true;
        this.m_cidr = cidr;
        this.m_add_date = add_date;
        this.m_country_code = country_code;
        this.m_org = org;
        this.setLine();
    }

    public void enabled( boolean b ){
        this.m_enable = b;
        this.setLine();
    }

    private void setLine(){
        if ( this.m_enable ) this.m_line = "";
        else this.m_line = "#";

        if ( this.m_cidr == null ) {
            this.m_line = "";
            return;
        }else if ( this.m_cidr.isEmpty() ) {
            this.m_line = "";
            return;
        }else{
            this.m_line += this.m_cidr;
        }
        this.m_line += "\t# ";
        if ( this.m_add_date != null ) this.m_line += this.m_add_date;
        this.m_line += " ";
        if ( this.m_country_code != null ) this.m_line += this.m_country_code;
        this.m_line += " ";
        if ( this.m_org != null ) this.m_line += this.m_org;
    }

    public Optional<String> getDuplicateCidr(){
        return Optional.ofNullable( this.m_duplicate_cidr );
    }

    public void checkDuplicateCidr( String cidr ){
        if ( ToolNet.isWithinCIDR( cidr, this.m_cidr ).orElse("").equals( cidr ) ){
            this.m_duplicate_cidr = cidr;
            Debugger.InfoPrint( this.m_cidr + " is contained in " + cidr );
        }
    }

}
