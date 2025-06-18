package jp.d77.java.mail_filter_editor.BasicIO;

import java.util.HashMap;

public class BSSForm {
    private enum FTYPE {
        TABLE_TOP
        ,TABLE_TH
        ,TABLE_TR
        ,TABLE_TD
        ,FORM_TOP
        ,FORM_INPUT
    };
    private String  topStr = "";
    private HashMap<FTYPE,BSOpts>  backup_opts = new HashMap<FTYPE,BSOpts>();
    private static Integer indent_count = 0;

    public BSSForm(){}
    public static BSSForm newForm(){
        return new BSSForm();
    }

    public static String sp() {
        //BSSForm.indent_count += add_count;
        return "\t".repeat( BSSForm.indent_count + 1 );
    }
    public static String spinc() {
        String ret = BSSForm.sp();
        BSSForm.indent_count += 1;
        return ret;
    }
    public static String spdec() {
        BSSForm.indent_count -= 1;
        String ret = BSSForm.sp();
        if ( BSSForm.indent_count < 0 ) BSSForm.indent_count = 0;
        return ret;
    }

    public String toString(){
        return this.topStr;
    }
    public BSSForm addString( String s ){
        this.topStr += BSSForm.sp() + s;
        return this;
    }
    public BSSForm addStringCr( String s ){
        this.topStr += BSSForm.sp() + s + "\n";
        return this;
    }
    public BSSForm addStringBr( String s ){
        this.topStr += BSSForm.sp() + s + "<BR>\n";
        return this;
    }

    // -----------------------------------------------------------------------------
    // R O W S
    // -----------------------------------------------------------------------------

    private BSSForm divsTop( String sclass ){
        this.topStr += BSSForm.spinc() + "<DIV class=\"" + sclass + "\">\n";
        return this;
    }

    private BSSForm divsBtm( String sclass ){
        this.topStr += BSSForm.spdec() + "</DIV>  <!-- " + sclass + " -->\n";
        return this;
    }

    public BSSForm divBorderTop( int grid12 ){
        return this.divsTop( "border col-" + grid12 );
    }

    public BSSForm divBorderBtm( int grid12 ){
        return this.divsTop( "border col-" + grid12 );
    }

    public BSSForm divRowTop(){
        return this.divsTop( "row mb-3" );
    }
    public BSSForm divRowBtm(){
        return this.divsBtm( "row mb-3" );
    }

    public BSSForm divTop( int grid12, String... add_classes ){
        String classes = "col-" + grid12;
        if ( add_classes.length > 0 ) classes += " " + String.join(" ", add_classes);
        return this.divsTop( classes );
    }

    public BSSForm divBtm( int grid12, String... add_classes ){
        String classes = "col-" + grid12;
        if ( add_classes.length > 0 ) classes += " " + String.join(" ", add_classes);
        return this.divsBtm( classes );
    }

    // -----------------------------------------------------------------------------
    // F O R M S
    // -----------------------------------------------------------------------------
    public BSSForm formTop( String action, Boolean multi ){
	    if ( multi ){
		    this.topStr += BSSForm.spinc() + "<FORM action=\"" + action + "\" method=\"POST\" ENCTYPE=\"multipart/form-data\">" + "\n";
	    }else{
		    this.topStr += BSSForm.spinc() + "<FORM action=\"" + action + "\" method=\"POST\">" + "\n";
	    }
        return this;
    }

    public BSSForm formBtm() {
	    this.topStr += BSSForm.spdec() + "</FORM>\n";
        return this;
    }

    public BSSForm formLabel( BSOpts opts ){
        if ( opts == null && this.backup_opts.containsKey( FTYPE.FORM_INPUT )) opts = this.backup_opts.get( FTYPE.FORM_INPUT );
        if ( opts == null ) return this;

        String label = opts.get( "label" ).orElse( "" );
        String name  = opts.get( "name" ).orElse( "" );
        this.topStr += BSSForm.sp() + "<LABEL for=\"" + HtmlString.HtmlEscape(name) + "\" class=\"col-form-label\">" + HtmlString.HtmlEscape(label) + "</LABEL>" + "\n";
        this.backup_opts.put( FTYPE.FORM_INPUT, opts);
        return this;
    }

    public BSSForm formInput( BSOpts opts ){
        if ( opts == null && this.backup_opts.containsKey( FTYPE.FORM_INPUT )) opts = this.backup_opts.get( FTYPE.FORM_INPUT );
        if ( opts == null ) return this;

        String type  = opts.get( "type" ).orElse( "input" );
        String name  = opts.get( "name" ).orElse( "" );
        String value = opts.get( "value" ).orElse( "" );
        if ( value.equals( "" ) ){
            value = opts.get( "default_value" ).orElse( "" );
        }
        
        //this.topStr += BSSForm.sp() + "<INPUT type=\"" + type + "\" name=\"" + HtmlString.HtmlEscape(name) + "\" class=\"form-control\" id=\"" + HtmlString.HtmlEscape(name) + "\" value=\"" + HtmlString.HtmlEscape(value) + "\">" + "\n";
        this.topStr += BSSForm.sp() + "<INPUT type=\"" + type + "\" name=\"" + HtmlString.HtmlEscape(name) + "\" value=\"" + HtmlString.HtmlEscape(value) + "\">" + "\n";
        this.backup_opts.put( FTYPE.FORM_INPUT, opts);
        return this;
    }

    public BSSForm formInputHidden( BSOpts opts ) {
        if ( opts == null && this.backup_opts.containsKey( FTYPE.FORM_INPUT )) opts = this.backup_opts.get( FTYPE.FORM_INPUT );
        if ( opts == null ) return this;

        opts.set( "type", "hidden" );
        opts.set( "id", opts.get("name").orElse("-") );
    
        this.topStr += BSSForm.sp() + "<INPUT " + opts.autoget("type", "name", "id", "value" ) + " class=\"form-control\">\n";
        this.backup_opts.put( FTYPE.FORM_INPUT, opts);
        return this;
    }

    public BSSForm formSubmit( BSOpts opts ){
        if ( opts == null && this.backup_opts.containsKey( FTYPE.FORM_INPUT )) opts = this.backup_opts.get( FTYPE.FORM_INPUT );
        if ( opts == null ) return this;

        opts.set( "type", "submit" );
        this.topStr += BSSForm.sp() + "<BUTTON " + opts.autoget("type", "name", "value") + " class=\"btn btn-primary\">" + HtmlString.HtmlEscape( opts.get("label").orElse("-") ) + "</BUTTON>\n";
        this.backup_opts.put( FTYPE.FORM_INPUT, opts);
        return this;
    }

    // -----------------------------------------------------------------------------
    // T A B L E S
    // -----------------------------------------------------------------------------
    public static String getTableHeader( String id ){
        String h;
        h = "<SCRIPT>\n"
            + "jQuery(function($){\n"
            + BSSForm.sp() + "$(\"#" + HtmlString.HtmlEscape(id) + "-table\").DataTable({\n"
            + BSSForm.sp() + "searching: true,\n"
            + BSSForm.sp() + "fixedHeader: true,\n"
            + BSSForm.sp() + "ordering: true,\n"
            + BSSForm.sp() + "info: true,\n"
            + BSSForm.sp() + "paging: false,\n"
//            + BSSForm.sp() + "lengthMenu: [[20,40,80,100,-1],[20,40,80,100,'ALL']],\n"
//            + BSSForm.sp() + "pagingType: 'full_numbers',\n"
//            + BSSForm.sp() + "pageLength: 20\n"
            + BSSForm.sp() + "});\n"
            + "});\n"
            + "</SCRIPT>\n";
        return h;
    }

    public BSSForm tableTop( String table_id ){
        return this.tableTop(
            new BSOpts()
                    .id(table_id)
                    .fclass("table table-bordered table-striped")
                    .border("1")
        );
    }

    public BSSForm tableTop( BSOpts opts ){
        this.topStr += BSSForm.spinc() + "<TABLE" + opts.autoget( "id","class","border","width" ) + ">\n";
        return this;
    }
    public BSSForm tableBtm(){
        this.topStr += BSSForm.spdec() + "</TABLE>\n";
        return this;
    }

    public BSSForm tableHeadTop(){
        this.topStr +=  BSSForm.spinc() + "<THEAD>\n";
        return this;
    }

    public BSSForm tableHeadBtm(){
        this.topStr +=  BSSForm.spdec() + "</THEAD>\n";
        return this;
    }

    public BSSForm tableBodyTop(){
        this.topStr += BSSForm.spinc() + "<TBODY>\n";
        return this;
    }

    public BSSForm tableBodyBtm(){
        this.topStr += BSSForm.spdec() + "</TBODY>\n";
        return this;
    }

    public BSSForm tableRowTh( String... ss ){
        BSSForm f = this.tableRowTop();
        for( String s: ss ){
            f.tableTh( s );
        }
        f.tableRowBtm();
        return this;
    }

    public BSSForm tableRowTd( String... ss ){
        BSSForm f = this.tableRowTop();
        for( String s: ss ){
            f.tableTd( s );
        }
        f.tableRowBtm();
        return this;
    }

    public BSSForm tableRowTop(){
        this.topStr += BSSForm.spinc() + "<TR>\n";
        return this;
    }

    public BSSForm tableRowBtm(){
        this.topStr += BSSForm.spdec() + "</TR>\n";
        return this;
    }

    public BSSForm tableTh( String s ){
        String ds = "";
        for( String ss: s.split("\n")){
            if ( ds.equals("") ){
                ds += HtmlString.HtmlEscape(ss);
            }else{
                ds += "<BR>\n" + HtmlString.HtmlEscape(ss);
            }
        }
        this.topStr += BSSForm.sp() + "<TH>" + ds + "</TH>\n";
        //this.topStr += BSSForm.sp() + "<TH>" + HtmlString.HtmlEscape(s) + "</TH>\n";
        return this;
    }
    public BSSForm tableTd( String s, String... opts ){
        String ds = "";
        for( String ss: s.split("\n")){
            if ( ds.equals("") ){
                ds += HtmlString.HtmlEscape(ss);
            }else{
                ds += "<BR>\n" + HtmlString.HtmlEscape(ss);
            }
        }
        this.topStr += BSSForm.sp() + "<TD" + String.join(" ", opts) + ">" + ds + "</TD>\n";
        return this;
    }

    public BSSForm tableTdHtml( String s, String... opts ){
        this.topStr += BSSForm.sp() + "<TD" + String.join(" ", opts) + ">" + s + "</TD>\n";
        return this;
    }
}
