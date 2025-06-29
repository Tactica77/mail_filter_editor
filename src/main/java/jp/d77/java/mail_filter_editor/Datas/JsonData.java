package jp.d77.java.mail_filter_editor.Datas;

import java.time.LocalDate;
import java.util.Optional;

import jp.d77.java.mail_filter_editor.BasicIO.ToolDate;
import jp.d77.java.mail_filter_editor.BasicIO.ToolNums;
import jp.d77.java.mail_filter_editor.Datas.JsonIO.JsonProp;

public class JsonData {
    private String      m_sData;
    private LocalDate   m_dData;
    private Float       m_fData;
    private boolean     m_empty = true;
    private String      m_sError = null;
    private JsonProp    m_Prop = null;

    public JsonData( JsonProp prop ){
        this.m_Prop = prop;
        this.m_sError = null;
        this.m_empty = true;
    }

    public Optional<String> getError(){
        return Optional.ofNullable( this.m_sError );
    }

    //******************************************************************************
    // Setter
    //******************************************************************************
    public JsonData set( String sData ){
        this.m_sError = null;
        this.m_empty = true;
        switch ( this.m_Prop.getType() ){
            case TEXT:
                this.setText(sData);
                return this;
            case NUM:
                this.setNum(sData);
                return this;
            case YMD:
                this.setYMD(sData);
                return this;
            default:
                this.m_sError = "undefine format: " + sData;
                return this;
        }
    }

    private boolean checkEmpty( String sData ){
        if ( sData == null || sData.isEmpty() || sData.equals( "" ) || sData.equals("-") || sData.equals("--") ){
            this.m_sError = "data empty: " + sData;
            this.m_empty = true;
            return true;
        }
        return false;
    }
    
    // Normal Set
    public JsonData setText( String sData ){
        if ( this.checkEmpty( sData ) ) return this;
        this.m_sData = sData;
        this.m_empty = false;
        return this;
    }

    public JsonData setNum( Float fData ){
        if ( fData == null ){
            this.m_sError = "data empty: " + fData;
            this.m_empty = true;
            return this;
        }
        this.m_fData = fData;
        this.m_empty = false;
        return this;
    }

    // String to Convert Set
    public JsonData setNum( String sData ){
        if ( this.checkEmpty( sData ) ) return this;

        Float f = ToolNums.Str2Float(sData).orElse( null );
        if ( f == null ){
            this.m_sError = "invalid num format: " + sData;
            this.m_empty = true;
            return this;
        }
        this.m_fData = f;
        this.m_empty = false;
        return this;
    }

    public JsonData setYMD( LocalDate dData ){
        if ( dData == null ){
            this.m_sError = "data empty: " + dData;
            this.m_empty = true;
            return this;
        }
        this.m_dData = dData;
        this.m_empty = false;
        return this;
    }

    public JsonData setYMD( String sData ){
        if ( this.checkEmpty( sData ) ) return this;
        LocalDate ld = ToolDate.YMD2LocalDate( sData ).orElse( null );
        if ( ld == null ){
            this.m_sError = "invalid YMD date format: " + sData;
            this.m_empty = true;
            return this;
        }
        return this.setYMD( ld );
    }
    //******************************************************************************
    // Getter
    //******************************************************************************

    public boolean isEmpty(){
        return this.m_empty;
    }

    public Optional<String> getString(){
        switch ( this.m_Prop.getType() ){
            case TEXT:
                return this.getText();
            case NUM:
                Float f = this.getNum().orElse( null );
                if ( f == null || f.isNaN() || f.isInfinite() ) return Optional.empty();
                String s = new java.math.BigDecimal( f.toString() ).toPlainString();
                return Optional.ofNullable( s );
            case YMD:
                LocalDate ld = this.getYMD().orElse( null );
                return Optional.ofNullable( ld.toString() );
            default:
                return Optional.empty();
        }       
    }

    public Optional<String> getText(){
        if ( this.isEmpty() ) Optional.empty();
        return Optional.ofNullable( this.m_sData );
    }

    public Optional<LocalDate> getYMD(){
        if ( this.isEmpty() ) Optional.empty();
        return Optional.ofNullable( this.m_dData );
    }

    public Optional<Float> getNum(){
        if ( this.isEmpty() ) Optional.empty();
        return Optional.ofNullable( this.m_fData );
    }
}