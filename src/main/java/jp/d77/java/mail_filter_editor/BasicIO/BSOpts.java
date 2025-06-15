package jp.d77.java.mail_filter_editor.BasicIO;

import java.util.HashMap;
import java.util.Optional;

public class BSOpts {
    private HashMap<String,String> datas;

    static public BSOpts init(){
        return new BSOpts();
    }
    public static BSOpts init( String name, String value ){
        return new BSOpts().set(name, value);
    }

    public BSOpts(){
        this.datas = new HashMap<String,String>();
    }

    public BSOpts set( String name, String value ){
        this.datas.put(name, value );
        return this;
    }

    public boolean contains( String name ){
        return this.datas.containsKey(name);
    }

    public Optional<String> get( String name ){
        if ( this.datas.containsKey(name) ) return Optional.ofNullable( this.datas.get(name) );
        return Optional.empty();
    }

    public BSOpts id( String value ){    return this.set("id", value);}
    public BSOpts title( String value ){    return this.set("title", value);}
    public BSOpts href( String value ){     return this.set("href", value);}
    public BSOpts target( String value ){   return this.set("target", value);}
    public BSOpts fclass( String value ){   return this.set("class", value);}
    public BSOpts border( String value ){    return this.set("title", value);}
    public BSOpts width( String value ){    return this.set("title", value);}
    public BSOpts name( String value ){    return this.set("name", value);}
    public BSOpts label( String value ){    return this.set("label", value);}
    public BSOpts type( String value ){    return this.set("type", value);}
    public BSOpts value( String value ){    return this.set("value", value);}

    public String autogetopt( String name ){
        if ( ! this.contains(name) ) return "";
        return " " + name + "=\"" + HtmlString.HtmlEscape( this.datas.get(name) ) + "\"";
    }

    public String autoget( String... names ){
        String ret = "";
        for (String name: names ){
            ret += this.autogetopt(name);
        }
        return ret;
    }
}
