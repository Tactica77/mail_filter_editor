package jp.d77.java.mail_filter_editor.BasicIO;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProgramProp {
    private static String getProp( String name ){
        Properties props = new Properties();
        try (InputStream in = ProgramProp.class.getResourceAsStream("/version.properties")) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return props.getProperty(name);
    }

    public static String getProgram(){
        return ProgramProp.getProp( "program" );
    }

    public static String getVersion(){
        return ProgramProp.getProp( "version" )
            + " " + ProgramProp.getProp( "releaseDate" );
    }
}
