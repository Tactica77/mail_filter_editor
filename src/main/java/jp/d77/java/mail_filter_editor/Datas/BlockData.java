package jp.d77.java.mail_filter_editor.Datas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;

public class BlockData {
    public ArrayList<LocalDateTime> m_datetime = new ArrayList<LocalDateTime>();
    public String    m_ip;
    public String    m_range;
    public ArrayList<String>    m_error_codes = new ArrayList<String>();
    public String    m_country_code;
    public ArrayList<String>    m_address_from_to = new ArrayList<String>();
    public String    m_from;
    public String    m_to;
    public String    m_org;
    public Integer   m_count = 0;

    public void countup(){
        this.m_count++;
    }

    public Optional<ArrayList<LocalDateTime>> getDateTime(){
        return Optional.ofNullable( this.m_datetime );
    }

    public Optional<String> getDate(){
        if ( this.m_datetime.size() <= 0 ) Optional.empty();
        return Optional.ofNullable( ToolDate.Fromat( this.m_datetime.get(0).toLocalDate(), "MM-dd" ) );
    }

    public Optional<String> getIp(){
        return Optional.ofNullable( this.m_ip );
    }

    public Optional<String> getRange(){
        return Optional.ofNullable( this.m_range );
    }

    public String[] getErrorCodes(){
        String[] array = this.m_error_codes.toArray(new String[0]);
        return array;
    }

    public Optional<String> getCc(){
        return Optional.ofNullable( this.m_country_code );
    }

    public String[] getFromTo(){
        String[] array = this.m_address_from_to.toArray(new String[0]);
        return array;
    }

    public Optional<String> getOrg(){
        return Optional.ofNullable( this.m_org );
    }

    public int getCount(){
        return this.m_count;
    }

    /**
     * 保存用１行データへ変換する
     * @return
     */
    public String getSaveLine(){
        String ret = "";

        // 0: date time
        if ( this.m_datetime == null ) ret += "";
        else ret += ToolDate.Fromat( this.m_datetime.get(0), "HH:mm:ss");
        ret += "<>";

        // 1: ip
        if ( this.m_ip == null ) ret += "";
        else ret += m_ip;
        ret += "<>";

        // 2: range
        if ( this.m_range == null ) ret += "";
        else ret += m_range;
        ret += "<>";

        // 3: codes
        if ( this.m_error_codes == null ) ret += "";
        else if ( this.m_error_codes.size() <= 0 ) ret += "";
        else ret += m_error_codes.get(0);
        ret += "<>";

        // 4: cc
        if ( this.m_country_code == null ) ret += "";
        else ret += m_country_code;
        ret += "<>";

        // 5: from
        if ( this.m_from == null ) ret += "";
        else ret += m_from;
        ret += "<>";

        // 6: to
        if ( this.m_to == null ) ret += "";
        else ret += m_to;
        ret += "<>";

        // 7: org
        if ( this.m_org == null ) ret += "";
        else ret += m_org;

        return ret;
    }
}
