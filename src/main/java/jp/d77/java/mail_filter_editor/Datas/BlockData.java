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

}
