// $ANTLR 3.2 Sep 23, 2009 12:02:23 /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g 2012-05-24 10:52:12

    package org.apache.cassandra.cql3;

    import java.util.Map;
    import java.util.HashMap;
    import java.util.Collections;
    import java.util.List;
    import java.util.ArrayList;

    import org.apache.cassandra.cql3.statements.*;
    import org.apache.cassandra.utils.Pair;
    import org.apache.cassandra.thrift.ConsistencyLevel;
    import org.apache.cassandra.thrift.InvalidRequestException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CqlParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "K_USE", "K_SELECT", "K_COUNT", "K_FROM", "K_USING", "K_CONSISTENCY", "K_LEVEL", "K_WHERE", "K_ORDER", "K_BY", "K_ASC", "K_DESC", "K_LIMIT", "INTEGER", "K_AND", "K_INSERT", "K_INTO", "K_VALUES", "K_TIMESTAMP", "K_TTL", "K_UPDATE", "K_SET", "K_DELETE", "K_BEGIN", "K_BATCH", "K_APPLY", "K_CREATE", "K_KEYSPACE", "K_WITH", "K_COLUMNFAMILY", "K_PRIMARY", "K_KEY", "K_COMPACT", "K_STORAGE", "K_INDEX", "IDENT", "K_ON", "K_ALTER", "K_TYPE", "K_ADD", "K_DROP", "K_TRUNCATE", "UUID", "QUOTED_NAME", "STRING_LITERAL", "FLOAT", "QMARK", "COMPIDENT", "K_IN", "S", "E", "L", "C", "T", "F", "R", "O", "M", "W", "H", "A", "N", "D", "K", "Y", "I", "U", "P", "G", "Q", "B", "X", "V", "J", "Z", "DIGIT", "LETTER", "HEX", "WS", "COMMENT", "MULTILINE_COMMENT", "';'", "'('", "')'", "'\\*'", "','", "'='", "'.'", "'+'", "'-'", "'<'", "'<='", "'>='", "'>'"
    };
    public static final int LETTER=80;
    public static final int K_CREATE=30;
    public static final int EOF=-1;
    public static final int K_PRIMARY=34;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__91=91;
    public static final int K_VALUES=21;
    public static final int T__92=92;
    public static final int K_USE=4;
    public static final int STRING_LITERAL=48;
    public static final int T__90=90;
    public static final int K_ON=40;
    public static final int K_USING=8;
    public static final int K_ADD=43;
    public static final int K_ASC=14;
    public static final int K_KEY=35;
    public static final int COMMENT=83;
    public static final int K_TRUNCATE=45;
    public static final int K_ORDER=12;
    public static final int T__97=97;
    public static final int T__96=96;
    public static final int T__95=95;
    public static final int D=66;
    public static final int E=54;
    public static final int F=58;
    public static final int G=72;
    public static final int K_COUNT=6;
    public static final int K_KEYSPACE=31;
    public static final int K_TYPE=42;
    public static final int A=64;
    public static final int B=74;
    public static final int C=56;
    public static final int L=55;
    public static final int M=61;
    public static final int N=65;
    public static final int O=60;
    public static final int H=63;
    public static final int I=69;
    public static final int K_UPDATE=24;
    public static final int J=77;
    public static final int K=67;
    public static final int U=70;
    public static final int T=57;
    public static final int W=62;
    public static final int V=76;
    public static final int Q=73;
    public static final int K_COMPACT=36;
    public static final int P=71;
    public static final int S=53;
    public static final int R=59;
    public static final int T__85=85;
    public static final int T__87=87;
    public static final int K_TTL=23;
    public static final int T__86=86;
    public static final int T__89=89;
    public static final int Y=68;
    public static final int T__88=88;
    public static final int X=75;
    public static final int Z=78;
    public static final int K_INDEX=38;
    public static final int K_INSERT=19;
    public static final int WS=82;
    public static final int K_APPLY=29;
    public static final int K_STORAGE=37;
    public static final int K_TIMESTAMP=22;
    public static final int K_AND=18;
    public static final int K_DESC=15;
    public static final int QMARK=50;
    public static final int K_LEVEL=10;
    public static final int K_BATCH=28;
    public static final int UUID=46;
    public static final int K_DELETE=26;
    public static final int K_BY=13;
    public static final int FLOAT=49;
    public static final int K_SELECT=5;
    public static final int K_LIMIT=16;
    public static final int K_ALTER=41;
    public static final int K_SET=25;
    public static final int K_WHERE=11;
    public static final int QUOTED_NAME=47;
    public static final int MULTILINE_COMMENT=84;
    public static final int HEX=81;
    public static final int K_INTO=20;
    public static final int IDENT=39;
    public static final int DIGIT=79;
    public static final int K_BEGIN=27;
    public static final int INTEGER=17;
    public static final int K_CONSISTENCY=9;
    public static final int K_WITH=32;
    public static final int COMPIDENT=51;
    public static final int K_IN=52;
    public static final int K_FROM=7;
    public static final int K_COLUMNFAMILY=33;
    public static final int K_DROP=44;

    // delegates
    // delegators


        public CqlParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CqlParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return CqlParser.tokenNames; }
    public String getGrammarFileName() { return "/media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g"; }


        private List<String> recognitionErrors = new ArrayList<String>();
        private int currentBindMarkerIdx = -1;

        public void displayRecognitionError(String[] tokenNames, RecognitionException e)
        {
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, tokenNames);
            recognitionErrors.add(hdr + " " + msg);
        }

        public List<String> getRecognitionErrors()
        {
            return recognitionErrors;
        }

        public void throwLastRecognitionError() throws InvalidRequestException
        {
            if (recognitionErrors.size() > 0)
                throw new InvalidRequestException(recognitionErrors.get((recognitionErrors.size()-1)));
        }

        // used by UPDATE of the counter columns to validate if '-' was supplied by user
        public void validateMinusSupplied(Object op, final Term value, IntStream stream) throws MissingTokenException
        {
            if (op == null && (value.isBindMarker() || Long.parseLong(value.getText()) > 0))
                throw new MissingTokenException(102, stream, value);
        }




    // $ANTLR start "query"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:116:1: query returns [ParsedStatement stmnt] : st= cqlStatement ( ';' )* EOF ;
    public final ParsedStatement query() throws RecognitionException {
        ParsedStatement stmnt = null;

        ParsedStatement st = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:119:5: (st= cqlStatement ( ';' )* EOF )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:119:7: st= cqlStatement ( ';' )* EOF
            {
            pushFollow(FOLLOW_cqlStatement_in_query72);
            st=cqlStatement();

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:119:23: ( ';' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==85) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:119:24: ';'
            	    {
            	    match(input,85,FOLLOW_85_in_query75); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_query79); 
             stmnt = st; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmnt;
    }
    // $ANTLR end "query"


    // $ANTLR start "cqlStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:122:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement | st2= insertStatement | st3= updateStatement | st4= batchStatement | st5= deleteStatement | st6= useStatement | st7= truncateStatement | st8= createKeyspaceStatement | st9= createColumnFamilyStatement | st10= createIndexStatement | st11= dropKeyspaceStatement | st12= dropColumnFamilyStatement | st13= dropIndexStatement | st14= alterTableStatement );
    public final ParsedStatement cqlStatement() throws RecognitionException {
        ParsedStatement stmt = null;

        SelectStatement.RawStatement st1 = null;

        UpdateStatement st2 = null;

        UpdateStatement st3 = null;

        BatchStatement st4 = null;

        DeleteStatement st5 = null;

        UseStatement st6 = null;

        TruncateStatement st7 = null;

        CreateKeyspaceStatement st8 = null;

        CreateColumnFamilyStatement.RawStatement st9 = null;

        CreateIndexStatement st10 = null;

        DropKeyspaceStatement st11 = null;

        DropColumnFamilyStatement st12 = null;

        DropIndexStatement st13 = null;

        AlterTableStatement st14 = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:124:5: (st1= selectStatement | st2= insertStatement | st3= updateStatement | st4= batchStatement | st5= deleteStatement | st6= useStatement | st7= truncateStatement | st8= createKeyspaceStatement | st9= createColumnFamilyStatement | st10= createIndexStatement | st11= dropKeyspaceStatement | st12= dropColumnFamilyStatement | st13= dropIndexStatement | st14= alterTableStatement )
            int alt2=14;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:124:7: st1= selectStatement
                    {
                    pushFollow(FOLLOW_selectStatement_in_cqlStatement113);
                    st1=selectStatement();

                    state._fsp--;

                     stmt = st1; 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:125:7: st2= insertStatement
                    {
                    pushFollow(FOLLOW_insertStatement_in_cqlStatement138);
                    st2=insertStatement();

                    state._fsp--;

                     stmt = st2; 

                    }
                    break;
                case 3 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:126:7: st3= updateStatement
                    {
                    pushFollow(FOLLOW_updateStatement_in_cqlStatement163);
                    st3=updateStatement();

                    state._fsp--;

                     stmt = st3; 

                    }
                    break;
                case 4 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:127:7: st4= batchStatement
                    {
                    pushFollow(FOLLOW_batchStatement_in_cqlStatement188);
                    st4=batchStatement();

                    state._fsp--;

                     stmt = st4; 

                    }
                    break;
                case 5 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:128:7: st5= deleteStatement
                    {
                    pushFollow(FOLLOW_deleteStatement_in_cqlStatement214);
                    st5=deleteStatement();

                    state._fsp--;

                     stmt = st5; 

                    }
                    break;
                case 6 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:129:7: st6= useStatement
                    {
                    pushFollow(FOLLOW_useStatement_in_cqlStatement239);
                    st6=useStatement();

                    state._fsp--;

                     stmt = st6; 

                    }
                    break;
                case 7 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:130:7: st7= truncateStatement
                    {
                    pushFollow(FOLLOW_truncateStatement_in_cqlStatement267);
                    st7=truncateStatement();

                    state._fsp--;

                     stmt = st7; 

                    }
                    break;
                case 8 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:131:7: st8= createKeyspaceStatement
                    {
                    pushFollow(FOLLOW_createKeyspaceStatement_in_cqlStatement290);
                    st8=createKeyspaceStatement();

                    state._fsp--;

                     stmt = st8; 

                    }
                    break;
                case 9 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:132:7: st9= createColumnFamilyStatement
                    {
                    pushFollow(FOLLOW_createColumnFamilyStatement_in_cqlStatement307);
                    st9=createColumnFamilyStatement();

                    state._fsp--;

                     stmt = st9; 

                    }
                    break;
                case 10 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:133:7: st10= createIndexStatement
                    {
                    pushFollow(FOLLOW_createIndexStatement_in_cqlStatement319);
                    st10=createIndexStatement();

                    state._fsp--;

                     stmt = st10; 

                    }
                    break;
                case 11 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:134:7: st11= dropKeyspaceStatement
                    {
                    pushFollow(FOLLOW_dropKeyspaceStatement_in_cqlStatement338);
                    st11=dropKeyspaceStatement();

                    state._fsp--;

                     stmt = st11; 

                    }
                    break;
                case 12 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:135:7: st12= dropColumnFamilyStatement
                    {
                    pushFollow(FOLLOW_dropColumnFamilyStatement_in_cqlStatement356);
                    st12=dropColumnFamilyStatement();

                    state._fsp--;

                     stmt = st12; 

                    }
                    break;
                case 13 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:136:7: st13= dropIndexStatement
                    {
                    pushFollow(FOLLOW_dropIndexStatement_in_cqlStatement370);
                    st13=dropIndexStatement();

                    state._fsp--;

                     stmt = st13; 

                    }
                    break;
                case 14 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:137:7: st14= alterTableStatement
                    {
                    pushFollow(FOLLOW_alterTableStatement_in_cqlStatement391);
                    st14=alterTableStatement();

                    state._fsp--;

                     stmt = st14; 

                    }
                    break;

            }
             if (stmt != null) stmt.setBoundTerms(currentBindMarkerIdx + 1); 
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "cqlStatement"


    // $ANTLR start "useStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:143:1: useStatement returns [UseStatement stmt] : K_USE ks= keyspaceName ;
    public final UseStatement useStatement() throws RecognitionException {
        UseStatement stmt = null;

        String ks = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:144:5: ( K_USE ks= keyspaceName )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:144:7: K_USE ks= keyspaceName
            {
            match(input,K_USE,FOLLOW_K_USE_in_useStatement424); 
            pushFollow(FOLLOW_keyspaceName_in_useStatement428);
            ks=keyspaceName();

            state._fsp--;

             stmt = new UseStatement(ks); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "useStatement"


    // $ANTLR start "selectStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:147:1: selectStatement returns [SelectStatement.RawStatement expr] : K_SELECT (sclause= selectClause | ( K_COUNT '(' sclause= selectClause ')' ) ) K_FROM cf= columnFamilyName ( K_USING K_CONSISTENCY K_LEVEL )? ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY c= cident ( K_ASC | K_DESC )? )? ( K_LIMIT rows= INTEGER )? ;
    public final SelectStatement.RawStatement selectStatement() throws RecognitionException {
        SelectStatement.RawStatement expr = null;

        Token rows=null;
        Token K_LEVEL1=null;
        List<ColumnIdentifier> sclause = null;

        CFName cf = null;

        List<Relation> wclause = null;

        ColumnIdentifier c = null;



                boolean isCount = false;
                ConsistencyLevel cLevel = ConsistencyLevel.ONE;
                int limit = 10000;
                boolean reversed = false;
                ColumnIdentifier orderBy = null;
            
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:162:5: ( K_SELECT (sclause= selectClause | ( K_COUNT '(' sclause= selectClause ')' ) ) K_FROM cf= columnFamilyName ( K_USING K_CONSISTENCY K_LEVEL )? ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY c= cident ( K_ASC | K_DESC )? )? ( K_LIMIT rows= INTEGER )? )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:162:7: K_SELECT (sclause= selectClause | ( K_COUNT '(' sclause= selectClause ')' ) ) K_FROM cf= columnFamilyName ( K_USING K_CONSISTENCY K_LEVEL )? ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY c= cident ( K_ASC | K_DESC )? )? ( K_LIMIT rows= INTEGER )?
            {
            match(input,K_SELECT,FOLLOW_K_SELECT_in_selectStatement462); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:162:16: (sclause= selectClause | ( K_COUNT '(' sclause= selectClause ')' ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==INTEGER||LA3_0==IDENT||(LA3_0>=UUID && LA3_0<=QUOTED_NAME)||LA3_0==88) ) {
                alt3=1;
            }
            else if ( (LA3_0==K_COUNT) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:162:18: sclause= selectClause
                    {
                    pushFollow(FOLLOW_selectClause_in_selectStatement468);
                    sclause=selectClause();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:162:41: ( K_COUNT '(' sclause= selectClause ')' )
                    {
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:162:41: ( K_COUNT '(' sclause= selectClause ')' )
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:162:42: K_COUNT '(' sclause= selectClause ')'
                    {
                    match(input,K_COUNT,FOLLOW_K_COUNT_in_selectStatement473); 
                    match(input,86,FOLLOW_86_in_selectStatement475); 
                    pushFollow(FOLLOW_selectClause_in_selectStatement479);
                    sclause=selectClause();

                    state._fsp--;

                    match(input,87,FOLLOW_87_in_selectStatement481); 
                     isCount = true; 

                    }


                    }
                    break;

            }

            match(input,K_FROM,FOLLOW_K_FROM_in_selectStatement494); 
            pushFollow(FOLLOW_columnFamilyName_in_selectStatement498);
            cf=columnFamilyName();

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:164:7: ( K_USING K_CONSISTENCY K_LEVEL )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==K_USING) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:164:9: K_USING K_CONSISTENCY K_LEVEL
                    {
                    match(input,K_USING,FOLLOW_K_USING_in_selectStatement508); 
                    match(input,K_CONSISTENCY,FOLLOW_K_CONSISTENCY_in_selectStatement510); 
                    K_LEVEL1=(Token)match(input,K_LEVEL,FOLLOW_K_LEVEL_in_selectStatement512); 
                     cLevel = ConsistencyLevel.valueOf((K_LEVEL1!=null?K_LEVEL1.getText():null).toUpperCase()); 

                    }
                    break;

            }

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:165:7: ( K_WHERE wclause= whereClause )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==K_WHERE) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:165:9: K_WHERE wclause= whereClause
                    {
                    match(input,K_WHERE,FOLLOW_K_WHERE_in_selectStatement527); 
                    pushFollow(FOLLOW_whereClause_in_selectStatement531);
                    wclause=whereClause();

                    state._fsp--;


                    }
                    break;

            }

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:166:7: ( K_ORDER K_BY c= cident ( K_ASC | K_DESC )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==K_ORDER) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:166:9: K_ORDER K_BY c= cident ( K_ASC | K_DESC )?
                    {
                    match(input,K_ORDER,FOLLOW_K_ORDER_in_selectStatement544); 
                    match(input,K_BY,FOLLOW_K_BY_in_selectStatement546); 
                    pushFollow(FOLLOW_cident_in_selectStatement550);
                    c=cident();

                    state._fsp--;

                     orderBy = c; 
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:166:48: ( K_ASC | K_DESC )?
                    int alt6=3;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==K_ASC) ) {
                        alt6=1;
                    }
                    else if ( (LA6_0==K_DESC) ) {
                        alt6=2;
                    }
                    switch (alt6) {
                        case 1 :
                            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:166:49: K_ASC
                            {
                            match(input,K_ASC,FOLLOW_K_ASC_in_selectStatement555); 

                            }
                            break;
                        case 2 :
                            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:166:57: K_DESC
                            {
                            match(input,K_DESC,FOLLOW_K_DESC_in_selectStatement559); 
                             reversed = true; 

                            }
                            break;

                    }


                    }
                    break;

            }

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:167:7: ( K_LIMIT rows= INTEGER )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==K_LIMIT) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:167:9: K_LIMIT rows= INTEGER
                    {
                    match(input,K_LIMIT,FOLLOW_K_LIMIT_in_selectStatement576); 
                    rows=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_selectStatement580); 
                     limit = Integer.parseInt((rows!=null?rows.getText():null)); 

                    }
                    break;

            }


                      SelectStatement.Parameters params = new SelectStatement.Parameters(cLevel,
                                                                                         limit,
                                                                                         orderBy,
                                                                                         reversed,
                                                                                         isCount);
                      expr = new SelectStatement.RawStatement(cf, params, sclause, wclause);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "selectStatement"


    // $ANTLR start "selectClause"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:178:1: selectClause returns [List<ColumnIdentifier> expr] : (ids= cidentList | '\\*' );
    public final List<ColumnIdentifier> selectClause() throws RecognitionException {
        List<ColumnIdentifier> expr = null;

        List<ColumnIdentifier> ids = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:179:5: (ids= cidentList | '\\*' )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==INTEGER||LA9_0==IDENT||(LA9_0>=UUID && LA9_0<=QUOTED_NAME)) ) {
                alt9=1;
            }
            else if ( (LA9_0==88) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:179:7: ids= cidentList
                    {
                    pushFollow(FOLLOW_cidentList_in_selectClause616);
                    ids=cidentList();

                    state._fsp--;

                     expr = ids; 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:180:7: '\\*'
                    {
                    match(input,88,FOLLOW_88_in_selectClause626); 
                     expr = Collections.<ColumnIdentifier>emptyList();

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "selectClause"


    // $ANTLR start "whereClause"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:183:1: whereClause returns [List<Relation> clause] : first= relation ( K_AND next= relation )* ;
    public final List<Relation> whereClause() throws RecognitionException {
        List<Relation> clause = null;

        Relation first = null;

        Relation next = null;


         clause = new ArrayList<Relation>(); 
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:185:5: (first= relation ( K_AND next= relation )* )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:185:7: first= relation ( K_AND next= relation )*
            {
            pushFollow(FOLLOW_relation_in_whereClause669);
            first=relation();

            state._fsp--;

             clause.add(first); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:185:46: ( K_AND next= relation )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==K_AND) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:185:47: K_AND next= relation
            	    {
            	    match(input,K_AND,FOLLOW_K_AND_in_whereClause674); 
            	    pushFollow(FOLLOW_relation_in_whereClause678);
            	    next=relation();

            	    state._fsp--;

            	     clause.add(next); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return clause;
    }
    // $ANTLR end "whereClause"


    // $ANTLR start "insertStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:188:1: insertStatement returns [UpdateStatement expr] : K_INSERT K_INTO cf= columnFamilyName '(' c1= cident ( ',' cn= cident )+ ')' K_VALUES '(' v1= term ( ',' vn= term )+ ')' ( usingClause[attrs] )? ;
    public final UpdateStatement insertStatement() throws RecognitionException {
        UpdateStatement expr = null;

        CFName cf = null;

        ColumnIdentifier c1 = null;

        ColumnIdentifier cn = null;

        Term v1 = null;

        Term vn = null;



                Attributes attrs = new Attributes();
                List<ColumnIdentifier> columnNames  = new ArrayList<ColumnIdentifier>();
                List<Term> columnValues = new ArrayList<Term>();
            
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:201:5: ( K_INSERT K_INTO cf= columnFamilyName '(' c1= cident ( ',' cn= cident )+ ')' K_VALUES '(' v1= term ( ',' vn= term )+ ')' ( usingClause[attrs] )? )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:201:7: K_INSERT K_INTO cf= columnFamilyName '(' c1= cident ( ',' cn= cident )+ ')' K_VALUES '(' v1= term ( ',' vn= term )+ ')' ( usingClause[attrs] )?
            {
            match(input,K_INSERT,FOLLOW_K_INSERT_in_insertStatement714); 
            match(input,K_INTO,FOLLOW_K_INTO_in_insertStatement716); 
            pushFollow(FOLLOW_columnFamilyName_in_insertStatement720);
            cf=columnFamilyName();

            state._fsp--;

            match(input,86,FOLLOW_86_in_insertStatement732); 
            pushFollow(FOLLOW_cident_in_insertStatement736);
            c1=cident();

            state._fsp--;

             columnNames.add(c1); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:202:51: ( ',' cn= cident )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==89) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:202:53: ',' cn= cident
            	    {
            	    match(input,89,FOLLOW_89_in_insertStatement743); 
            	    pushFollow(FOLLOW_cident_in_insertStatement747);
            	    cn=cident();

            	    state._fsp--;

            	     columnNames.add(cn); 

            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);

            match(input,87,FOLLOW_87_in_insertStatement754); 
            match(input,K_VALUES,FOLLOW_K_VALUES_in_insertStatement764); 
            match(input,86,FOLLOW_86_in_insertStatement776); 
            pushFollow(FOLLOW_term_in_insertStatement780);
            v1=term();

            state._fsp--;

             columnValues.add(v1); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:204:49: ( ',' vn= term )+
            int cnt12=0;
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==89) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:204:51: ',' vn= term
            	    {
            	    match(input,89,FOLLOW_89_in_insertStatement786); 
            	    pushFollow(FOLLOW_term_in_insertStatement790);
            	    vn=term();

            	    state._fsp--;

            	     columnValues.add(vn); 

            	    }
            	    break;

            	default :
            	    if ( cnt12 >= 1 ) break loop12;
                        EarlyExitException eee =
                            new EarlyExitException(12, input);
                        throw eee;
                }
                cnt12++;
            } while (true);

            match(input,87,FOLLOW_87_in_insertStatement797); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:205:9: ( usingClause[attrs] )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==K_USING) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:205:11: usingClause[attrs]
                    {
                    pushFollow(FOLLOW_usingClause_in_insertStatement809);
                    usingClause(attrs);

                    state._fsp--;


                    }
                    break;

            }


                      expr = new UpdateStatement(cf, columnNames, columnValues, attrs);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "insertStatement"


    // $ANTLR start "usingClause"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:211:1: usingClause[Attributes attrs] : K_USING usingClauseObjective[attrs] ( ( K_AND )? usingClauseObjective[attrs] )* ;
    public final void usingClause(Attributes attrs) throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:212:5: ( K_USING usingClauseObjective[attrs] ( ( K_AND )? usingClauseObjective[attrs] )* )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:212:7: K_USING usingClauseObjective[attrs] ( ( K_AND )? usingClauseObjective[attrs] )*
            {
            match(input,K_USING,FOLLOW_K_USING_in_usingClause839); 
            pushFollow(FOLLOW_usingClauseObjective_in_usingClause841);
            usingClauseObjective(attrs);

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:212:43: ( ( K_AND )? usingClauseObjective[attrs] )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==K_CONSISTENCY||LA15_0==K_AND||(LA15_0>=K_TIMESTAMP && LA15_0<=K_TTL)) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:212:45: ( K_AND )? usingClauseObjective[attrs]
            	    {
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:212:45: ( K_AND )?
            	    int alt14=2;
            	    int LA14_0 = input.LA(1);

            	    if ( (LA14_0==K_AND) ) {
            	        alt14=1;
            	    }
            	    switch (alt14) {
            	        case 1 :
            	            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:212:45: K_AND
            	            {
            	            match(input,K_AND,FOLLOW_K_AND_in_usingClause846); 

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_usingClauseObjective_in_usingClause849);
            	    usingClauseObjective(attrs);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "usingClause"


    // $ANTLR start "usingClauseDelete"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:215:1: usingClauseDelete[Attributes attrs] : K_USING usingClauseDeleteObjective[attrs] ( ( K_AND )? usingClauseDeleteObjective[attrs] )* ;
    public final void usingClauseDelete(Attributes attrs) throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:216:5: ( K_USING usingClauseDeleteObjective[attrs] ( ( K_AND )? usingClauseDeleteObjective[attrs] )* )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:216:7: K_USING usingClauseDeleteObjective[attrs] ( ( K_AND )? usingClauseDeleteObjective[attrs] )*
            {
            match(input,K_USING,FOLLOW_K_USING_in_usingClauseDelete871); 
            pushFollow(FOLLOW_usingClauseDeleteObjective_in_usingClauseDelete873);
            usingClauseDeleteObjective(attrs);

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:216:49: ( ( K_AND )? usingClauseDeleteObjective[attrs] )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==K_CONSISTENCY||LA17_0==K_AND||LA17_0==K_TIMESTAMP) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:216:51: ( K_AND )? usingClauseDeleteObjective[attrs]
            	    {
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:216:51: ( K_AND )?
            	    int alt16=2;
            	    int LA16_0 = input.LA(1);

            	    if ( (LA16_0==K_AND) ) {
            	        alt16=1;
            	    }
            	    switch (alt16) {
            	        case 1 :
            	            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:216:51: K_AND
            	            {
            	            match(input,K_AND,FOLLOW_K_AND_in_usingClauseDelete878); 

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_usingClauseDeleteObjective_in_usingClauseDelete881);
            	    usingClauseDeleteObjective(attrs);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "usingClauseDelete"


    // $ANTLR start "usingClauseDeleteObjective"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:219:1: usingClauseDeleteObjective[Attributes attrs] : ( K_CONSISTENCY K_LEVEL | K_TIMESTAMP ts= INTEGER );
    public final void usingClauseDeleteObjective(Attributes attrs) throws RecognitionException {
        Token ts=null;
        Token K_LEVEL2=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:220:5: ( K_CONSISTENCY K_LEVEL | K_TIMESTAMP ts= INTEGER )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==K_CONSISTENCY) ) {
                alt18=1;
            }
            else if ( (LA18_0==K_TIMESTAMP) ) {
                alt18=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:220:7: K_CONSISTENCY K_LEVEL
                    {
                    match(input,K_CONSISTENCY,FOLLOW_K_CONSISTENCY_in_usingClauseDeleteObjective903); 
                    K_LEVEL2=(Token)match(input,K_LEVEL,FOLLOW_K_LEVEL_in_usingClauseDeleteObjective905); 
                     attrs.cLevel = ConsistencyLevel.valueOf((K_LEVEL2!=null?K_LEVEL2.getText():null).toUpperCase()); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:221:7: K_TIMESTAMP ts= INTEGER
                    {
                    match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_usingClauseDeleteObjective916); 
                    ts=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_usingClauseDeleteObjective920); 
                     attrs.timestamp = Long.valueOf((ts!=null?ts.getText():null)); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "usingClauseDeleteObjective"


    // $ANTLR start "usingClauseObjective"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:224:1: usingClauseObjective[Attributes attrs] : ( usingClauseDeleteObjective[attrs] | K_TTL t= INTEGER );
    public final void usingClauseObjective(Attributes attrs) throws RecognitionException {
        Token t=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:225:5: ( usingClauseDeleteObjective[attrs] | K_TTL t= INTEGER )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==K_CONSISTENCY||LA19_0==K_TIMESTAMP) ) {
                alt19=1;
            }
            else if ( (LA19_0==K_TTL) ) {
                alt19=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:225:7: usingClauseDeleteObjective[attrs]
                    {
                    pushFollow(FOLLOW_usingClauseDeleteObjective_in_usingClauseObjective940);
                    usingClauseDeleteObjective(attrs);

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:226:7: K_TTL t= INTEGER
                    {
                    match(input,K_TTL,FOLLOW_K_TTL_in_usingClauseObjective949); 
                    t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_usingClauseObjective953); 
                     attrs.timeToLive = Integer.valueOf((t!=null?t.getText():null)); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "usingClauseObjective"


    // $ANTLR start "updateStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:229:1: updateStatement returns [UpdateStatement expr] : K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET termPairWithOperation[columns] ( ',' termPairWithOperation[columns] )* K_WHERE wclause= whereClause ;
    public final UpdateStatement updateStatement() throws RecognitionException {
        UpdateStatement expr = null;

        CFName cf = null;

        List<Relation> wclause = null;



                Attributes attrs = new Attributes();
                Map<ColumnIdentifier, Operation> columns = new HashMap<ColumnIdentifier, Operation>();
            
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:5: ( K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET termPairWithOperation[columns] ( ',' termPairWithOperation[columns] )* K_WHERE wclause= whereClause )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:7: K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET termPairWithOperation[columns] ( ',' termPairWithOperation[columns] )* K_WHERE wclause= whereClause
            {
            match(input,K_UPDATE,FOLLOW_K_UPDATE_in_updateStatement987); 
            pushFollow(FOLLOW_columnFamilyName_in_updateStatement991);
            cf=columnFamilyName();

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:241:7: ( usingClause[attrs] )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==K_USING) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:241:9: usingClause[attrs]
                    {
                    pushFollow(FOLLOW_usingClause_in_updateStatement1001);
                    usingClause(attrs);

                    state._fsp--;


                    }
                    break;

            }

            match(input,K_SET,FOLLOW_K_SET_in_updateStatement1013); 
            pushFollow(FOLLOW_termPairWithOperation_in_updateStatement1015);
            termPairWithOperation(columns);

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:242:44: ( ',' termPairWithOperation[columns] )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==89) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:242:45: ',' termPairWithOperation[columns]
            	    {
            	    match(input,89,FOLLOW_89_in_updateStatement1019); 
            	    pushFollow(FOLLOW_termPairWithOperation_in_updateStatement1021);
            	    termPairWithOperation(columns);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            match(input,K_WHERE,FOLLOW_K_WHERE_in_updateStatement1032); 
            pushFollow(FOLLOW_whereClause_in_updateStatement1036);
            wclause=whereClause();

            state._fsp--;


                      return new UpdateStatement(cf, columns, wclause, attrs);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "updateStatement"


    // $ANTLR start "deleteStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:249:1: deleteStatement returns [DeleteStatement expr] : K_DELETE (ids= cidentList )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ;
    public final DeleteStatement deleteStatement() throws RecognitionException {
        DeleteStatement expr = null;

        List<ColumnIdentifier> ids = null;

        CFName cf = null;

        List<Relation> wclause = null;



                Attributes attrs = new Attributes();
                List<ColumnIdentifier> columnsList = Collections.emptyList();
            
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:5: ( K_DELETE (ids= cidentList )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:7: K_DELETE (ids= cidentList )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause
            {
            match(input,K_DELETE,FOLLOW_K_DELETE_in_deleteStatement1076); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:16: (ids= cidentList )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==INTEGER||LA22_0==IDENT||(LA22_0>=UUID && LA22_0<=QUOTED_NAME)) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:18: ids= cidentList
                    {
                    pushFollow(FOLLOW_cidentList_in_deleteStatement1082);
                    ids=cidentList();

                    state._fsp--;

                     columnsList = ids; 

                    }
                    break;

            }

            match(input,K_FROM,FOLLOW_K_FROM_in_deleteStatement1095); 
            pushFollow(FOLLOW_columnFamilyName_in_deleteStatement1099);
            cf=columnFamilyName();

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:262:7: ( usingClauseDelete[attrs] )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==K_USING) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:262:9: usingClauseDelete[attrs]
                    {
                    pushFollow(FOLLOW_usingClauseDelete_in_deleteStatement1109);
                    usingClauseDelete(attrs);

                    state._fsp--;


                    }
                    break;

            }

            match(input,K_WHERE,FOLLOW_K_WHERE_in_deleteStatement1121); 
            pushFollow(FOLLOW_whereClause_in_deleteStatement1125);
            wclause=whereClause();

            state._fsp--;


                      return new DeleteStatement(cf, columnsList, wclause, attrs);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "deleteStatement"


    // $ANTLR start "batchStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:270:1: batchStatement returns [BatchStatement expr] : K_BEGIN K_BATCH ( usingClause[attrs] )? s1= batchStatementObjective ( ';' )? (sN= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH ;
    public final BatchStatement batchStatement() throws RecognitionException {
        BatchStatement expr = null;

        ModificationStatement s1 = null;

        ModificationStatement sN = null;



                Attributes attrs = new Attributes();
                List<ModificationStatement> statements = new ArrayList<ModificationStatement>();
            
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:299:5: ( K_BEGIN K_BATCH ( usingClause[attrs] )? s1= batchStatementObjective ( ';' )? (sN= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:299:7: K_BEGIN K_BATCH ( usingClause[attrs] )? s1= batchStatementObjective ( ';' )? (sN= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH
            {
            match(input,K_BEGIN,FOLLOW_K_BEGIN_in_batchStatement1166); 
            match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement1168); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:299:23: ( usingClause[attrs] )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==K_USING) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:299:25: usingClause[attrs]
                    {
                    pushFollow(FOLLOW_usingClause_in_batchStatement1172);
                    usingClause(attrs);

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_batchStatementObjective_in_batchStatement1190);
            s1=batchStatementObjective();

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:38: ( ';' )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==85) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:38: ';'
                    {
                    match(input,85,FOLLOW_85_in_batchStatement1192); 

                    }
                    break;

            }

             statements.add(s1); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:67: (sN= batchStatementObjective ( ';' )? )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==K_INSERT||LA27_0==K_UPDATE||LA27_0==K_DELETE) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:69: sN= batchStatementObjective ( ';' )?
            	    {
            	    pushFollow(FOLLOW_batchStatementObjective_in_batchStatement1201);
            	    sN=batchStatementObjective();

            	    state._fsp--;

            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:96: ( ';' )?
            	    int alt26=2;
            	    int LA26_0 = input.LA(1);

            	    if ( (LA26_0==85) ) {
            	        alt26=1;
            	    }
            	    switch (alt26) {
            	        case 1 :
            	            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:96: ';'
            	            {
            	            match(input,85,FOLLOW_85_in_batchStatement1203); 

            	            }
            	            break;

            	    }

            	     statements.add(sN); 

            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);

            match(input,K_APPLY,FOLLOW_K_APPLY_in_batchStatement1217); 
            match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement1219); 

                      return new BatchStatement(statements, attrs);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "batchStatement"


    // $ANTLR start "batchStatementObjective"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:307:1: batchStatementObjective returns [ModificationStatement statement] : (i= insertStatement | u= updateStatement | d= deleteStatement );
    public final ModificationStatement batchStatementObjective() throws RecognitionException {
        ModificationStatement statement = null;

        UpdateStatement i = null;

        UpdateStatement u = null;

        DeleteStatement d = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:308:5: (i= insertStatement | u= updateStatement | d= deleteStatement )
            int alt28=3;
            switch ( input.LA(1) ) {
            case K_INSERT:
                {
                alt28=1;
                }
                break;
            case K_UPDATE:
                {
                alt28=2;
                }
                break;
            case K_DELETE:
                {
                alt28=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }

            switch (alt28) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:308:7: i= insertStatement
                    {
                    pushFollow(FOLLOW_insertStatement_in_batchStatementObjective1250);
                    i=insertStatement();

                    state._fsp--;

                     statement = i; 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:309:7: u= updateStatement
                    {
                    pushFollow(FOLLOW_updateStatement_in_batchStatementObjective1263);
                    u=updateStatement();

                    state._fsp--;

                     statement = u; 

                    }
                    break;
                case 3 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:310:7: d= deleteStatement
                    {
                    pushFollow(FOLLOW_deleteStatement_in_batchStatementObjective1276);
                    d=deleteStatement();

                    state._fsp--;

                     statement = d; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return statement;
    }
    // $ANTLR end "batchStatementObjective"


    // $ANTLR start "createKeyspaceStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:313:1: createKeyspaceStatement returns [CreateKeyspaceStatement expr] : K_CREATE K_KEYSPACE ks= keyspaceName K_WITH props= properties ;
    public final CreateKeyspaceStatement createKeyspaceStatement() throws RecognitionException {
        CreateKeyspaceStatement expr = null;

        String ks = null;

        Map<String, String> props = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:317:5: ( K_CREATE K_KEYSPACE ks= keyspaceName K_WITH props= properties )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:317:7: K_CREATE K_KEYSPACE ks= keyspaceName K_WITH props= properties
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createKeyspaceStatement1302); 
            match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_createKeyspaceStatement1304); 
            pushFollow(FOLLOW_keyspaceName_in_createKeyspaceStatement1308);
            ks=keyspaceName();

            state._fsp--;

            match(input,K_WITH,FOLLOW_K_WITH_in_createKeyspaceStatement1316); 
            pushFollow(FOLLOW_properties_in_createKeyspaceStatement1320);
            props=properties();

            state._fsp--;

             expr = new CreateKeyspaceStatement(ks, props); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createKeyspaceStatement"


    // $ANTLR start "createColumnFamilyStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:321:1: createColumnFamilyStatement returns [CreateColumnFamilyStatement.RawStatement expr] : K_CREATE K_COLUMNFAMILY cf= columnFamilyName cfamDefinition[expr] ;
    public final CreateColumnFamilyStatement.RawStatement createColumnFamilyStatement() throws RecognitionException {
        CreateColumnFamilyStatement.RawStatement expr = null;

        CFName cf = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:329:5: ( K_CREATE K_COLUMNFAMILY cf= columnFamilyName cfamDefinition[expr] )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:329:7: K_CREATE K_COLUMNFAMILY cf= columnFamilyName cfamDefinition[expr]
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createColumnFamilyStatement1345); 
            match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_createColumnFamilyStatement1347); 
            pushFollow(FOLLOW_columnFamilyName_in_createColumnFamilyStatement1351);
            cf=columnFamilyName();

            state._fsp--;

             expr = new CreateColumnFamilyStatement.RawStatement(cf); 
            pushFollow(FOLLOW_cfamDefinition_in_createColumnFamilyStatement1361);
            cfamDefinition(expr);

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createColumnFamilyStatement"


    // $ANTLR start "cfamDefinition"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:333:1: cfamDefinition[CreateColumnFamilyStatement.RawStatement expr] : '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )? ;
    public final void cfamDefinition(CreateColumnFamilyStatement.RawStatement expr) throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:5: ( '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )? )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:7: '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )?
            {
            match(input,86,FOLLOW_86_in_cfamDefinition1380); 
            pushFollow(FOLLOW_cfamColumns_in_cfamDefinition1382);
            cfamColumns(expr);

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:29: ( ',' ( cfamColumns[expr] )? )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==89) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:31: ',' ( cfamColumns[expr] )?
            	    {
            	    match(input,89,FOLLOW_89_in_cfamDefinition1387); 
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:35: ( cfamColumns[expr] )?
            	    int alt29=2;
            	    int LA29_0 = input.LA(1);

            	    if ( (LA29_0==INTEGER||LA29_0==K_PRIMARY||LA29_0==IDENT||(LA29_0>=UUID && LA29_0<=QUOTED_NAME)) ) {
            	        alt29=1;
            	    }
            	    switch (alt29) {
            	        case 1 :
            	            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:35: cfamColumns[expr]
            	            {
            	            pushFollow(FOLLOW_cfamColumns_in_cfamDefinition1389);
            	            cfamColumns(expr);

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);

            match(input,87,FOLLOW_87_in_cfamDefinition1396); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:335:7: ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==K_WITH) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:335:9: K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )*
                    {
                    match(input,K_WITH,FOLLOW_K_WITH_in_cfamDefinition1406); 
                    pushFollow(FOLLOW_cfamProperty_in_cfamDefinition1408);
                    cfamProperty(expr);

                    state._fsp--;

                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:335:35: ( K_AND cfamProperty[expr] )*
                    loop31:
                    do {
                        int alt31=2;
                        int LA31_0 = input.LA(1);

                        if ( (LA31_0==K_AND) ) {
                            alt31=1;
                        }


                        switch (alt31) {
                    	case 1 :
                    	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:335:37: K_AND cfamProperty[expr]
                    	    {
                    	    match(input,K_AND,FOLLOW_K_AND_in_cfamDefinition1413); 
                    	    pushFollow(FOLLOW_cfamProperty_in_cfamDefinition1415);
                    	    cfamProperty(expr);

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfamDefinition"


    // $ANTLR start "cfamColumns"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:338:1: cfamColumns[CreateColumnFamilyStatement.RawStatement expr] : (k= cident v= comparatorType ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' k= cident ( ',' c= cident )* ')' );
    public final void cfamColumns(CreateColumnFamilyStatement.RawStatement expr) throws RecognitionException {
        ColumnIdentifier k = null;

        String v = null;

        ColumnIdentifier c = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:339:5: (k= cident v= comparatorType ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' k= cident ( ',' c= cident )* ')' )
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==INTEGER||LA35_0==IDENT||(LA35_0>=UUID && LA35_0<=QUOTED_NAME)) ) {
                alt35=1;
            }
            else if ( (LA35_0==K_PRIMARY) ) {
                alt35=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }
            switch (alt35) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:339:7: k= cident v= comparatorType ( K_PRIMARY K_KEY )?
                    {
                    pushFollow(FOLLOW_cident_in_cfamColumns1441);
                    k=cident();

                    state._fsp--;

                    pushFollow(FOLLOW_comparatorType_in_cfamColumns1445);
                    v=comparatorType();

                    state._fsp--;

                     expr.addDefinition(k, v); 
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:339:64: ( K_PRIMARY K_KEY )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==K_PRIMARY) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:339:65: K_PRIMARY K_KEY
                            {
                            match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns1450); 
                            match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns1452); 
                             expr.setKeyAlias(k); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:340:7: K_PRIMARY K_KEY '(' k= cident ( ',' c= cident )* ')'
                    {
                    match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns1464); 
                    match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns1466); 
                    match(input,86,FOLLOW_86_in_cfamColumns1468); 
                    pushFollow(FOLLOW_cident_in_cfamColumns1472);
                    k=cident();

                    state._fsp--;

                     expr.setKeyAlias(k); 
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:340:62: ( ',' c= cident )*
                    loop34:
                    do {
                        int alt34=2;
                        int LA34_0 = input.LA(1);

                        if ( (LA34_0==89) ) {
                            alt34=1;
                        }


                        switch (alt34) {
                    	case 1 :
                    	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:340:63: ',' c= cident
                    	    {
                    	    match(input,89,FOLLOW_89_in_cfamColumns1477); 
                    	    pushFollow(FOLLOW_cident_in_cfamColumns1481);
                    	    c=cident();

                    	    state._fsp--;

                    	     expr.addColumnAlias(c); 

                    	    }
                    	    break;

                    	default :
                    	    break loop34;
                        }
                    } while (true);

                    match(input,87,FOLLOW_87_in_cfamColumns1488); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfamColumns"


    // $ANTLR start "cfamProperty"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:343:1: cfamProperty[CreateColumnFamilyStatement.RawStatement expr] : (k= property '=' v= propertyValue | K_COMPACT K_STORAGE );
    public final void cfamProperty(CreateColumnFamilyStatement.RawStatement expr) throws RecognitionException {
        String k = null;

        String v = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:344:5: (k= property '=' v= propertyValue | K_COMPACT K_STORAGE )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==IDENT||LA36_0==COMPIDENT) ) {
                alt36=1;
            }
            else if ( (LA36_0==K_COMPACT) ) {
                alt36=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:344:7: k= property '=' v= propertyValue
                    {
                    pushFollow(FOLLOW_property_in_cfamProperty1508);
                    k=property();

                    state._fsp--;

                    match(input,90,FOLLOW_90_in_cfamProperty1510); 
                    pushFollow(FOLLOW_propertyValue_in_cfamProperty1514);
                    v=propertyValue();

                    state._fsp--;

                     expr.addProperty(k, v); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:345:7: K_COMPACT K_STORAGE
                    {
                    match(input,K_COMPACT,FOLLOW_K_COMPACT_in_cfamProperty1524); 
                    match(input,K_STORAGE,FOLLOW_K_STORAGE_in_cfamProperty1526); 
                     expr.setCompactStorage(); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfamProperty"


    // $ANTLR start "createIndexStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:348:1: createIndexStatement returns [CreateIndexStatement expr] : K_CREATE K_INDEX (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' ;
    public final CreateIndexStatement createIndexStatement() throws RecognitionException {
        CreateIndexStatement expr = null;

        Token idxName=null;
        CFName cf = null;

        ColumnIdentifier id = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:5: ( K_CREATE K_INDEX (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:7: K_CREATE K_INDEX (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')'
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createIndexStatement1551); 
            match(input,K_INDEX,FOLLOW_K_INDEX_in_createIndexStatement1553); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:24: (idxName= IDENT )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==IDENT) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:25: idxName= IDENT
                    {
                    idxName=(Token)match(input,IDENT,FOLLOW_IDENT_in_createIndexStatement1558); 

                    }
                    break;

            }

            match(input,K_ON,FOLLOW_K_ON_in_createIndexStatement1562); 
            pushFollow(FOLLOW_columnFamilyName_in_createIndexStatement1566);
            cf=columnFamilyName();

            state._fsp--;

            match(input,86,FOLLOW_86_in_createIndexStatement1568); 
            pushFollow(FOLLOW_cident_in_createIndexStatement1572);
            id=cident();

            state._fsp--;

            match(input,87,FOLLOW_87_in_createIndexStatement1574); 
             expr = new CreateIndexStatement(cf, (idxName!=null?idxName.getText():null), id); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createIndexStatement"


    // $ANTLR start "alterTableStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:356:1: alterTableStatement returns [AlterTableStatement expr] : K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH props= properties ) ;
    public final AlterTableStatement alterTableStatement() throws RecognitionException {
        AlterTableStatement expr = null;

        CFName cf = null;

        ColumnIdentifier id = null;

        String v = null;

        Map<String, String> props = null;



                AlterTableStatement.Type type = null;
                props = new HashMap<String, String>();
            
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:367:5: ( K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH props= properties ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:367:7: K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH props= properties )
            {
            match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement1614); 
            match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_alterTableStatement1616); 
            pushFollow(FOLLOW_columnFamilyName_in_alterTableStatement1620);
            cf=columnFamilyName();

            state._fsp--;

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:368:11: ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH props= properties )
            int alt38=4;
            switch ( input.LA(1) ) {
            case K_ALTER:
                {
                alt38=1;
                }
                break;
            case K_ADD:
                {
                alt38=2;
                }
                break;
            case K_DROP:
                {
                alt38=3;
                }
                break;
            case K_WITH:
                {
                alt38=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }

            switch (alt38) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:368:13: K_ALTER id= cident K_TYPE v= comparatorType
                    {
                    match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement1634); 
                    pushFollow(FOLLOW_cident_in_alterTableStatement1638);
                    id=cident();

                    state._fsp--;

                    match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTableStatement1640); 
                    pushFollow(FOLLOW_comparatorType_in_alterTableStatement1644);
                    v=comparatorType();

                    state._fsp--;

                     type = AlterTableStatement.Type.ALTER; 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:369:13: K_ADD id= cident v= comparatorType
                    {
                    match(input,K_ADD,FOLLOW_K_ADD_in_alterTableStatement1660); 
                    pushFollow(FOLLOW_cident_in_alterTableStatement1666);
                    id=cident();

                    state._fsp--;

                    pushFollow(FOLLOW_comparatorType_in_alterTableStatement1670);
                    v=comparatorType();

                    state._fsp--;

                     type = AlterTableStatement.Type.ADD; 

                    }
                    break;
                case 3 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:370:13: K_DROP id= cident
                    {
                    match(input,K_DROP,FOLLOW_K_DROP_in_alterTableStatement1693); 
                    pushFollow(FOLLOW_cident_in_alterTableStatement1698);
                    id=cident();

                    state._fsp--;

                     type = AlterTableStatement.Type.DROP; 

                    }
                    break;
                case 4 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:371:13: K_WITH props= properties
                    {
                    match(input,K_WITH,FOLLOW_K_WITH_in_alterTableStatement1738); 
                    pushFollow(FOLLOW_properties_in_alterTableStatement1743);
                    props=properties();

                    state._fsp--;

                     type = AlterTableStatement.Type.OPTS; 

                    }
                    break;

            }


                    expr = new AlterTableStatement(cf, type, id, v, props);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "alterTableStatement"


    // $ANTLR start "dropKeyspaceStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:378:1: dropKeyspaceStatement returns [DropKeyspaceStatement ksp] : K_DROP K_KEYSPACE ks= keyspaceName ;
    public final DropKeyspaceStatement dropKeyspaceStatement() throws RecognitionException {
        DropKeyspaceStatement ksp = null;

        String ks = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:382:5: ( K_DROP K_KEYSPACE ks= keyspaceName )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:382:7: K_DROP K_KEYSPACE ks= keyspaceName
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropKeyspaceStatement1803); 
            match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement1805); 
            pushFollow(FOLLOW_keyspaceName_in_dropKeyspaceStatement1809);
            ks=keyspaceName();

            state._fsp--;

             ksp = new DropKeyspaceStatement(ks); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ksp;
    }
    // $ANTLR end "dropKeyspaceStatement"


    // $ANTLR start "dropColumnFamilyStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:385:1: dropColumnFamilyStatement returns [DropColumnFamilyStatement stmt] : K_DROP K_COLUMNFAMILY cf= columnFamilyName ;
    public final DropColumnFamilyStatement dropColumnFamilyStatement() throws RecognitionException {
        DropColumnFamilyStatement stmt = null;

        CFName cf = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:389:5: ( K_DROP K_COLUMNFAMILY cf= columnFamilyName )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:389:7: K_DROP K_COLUMNFAMILY cf= columnFamilyName
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropColumnFamilyStatement1834); 
            match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_dropColumnFamilyStatement1836); 
            pushFollow(FOLLOW_columnFamilyName_in_dropColumnFamilyStatement1840);
            cf=columnFamilyName();

            state._fsp--;

             stmt = new DropColumnFamilyStatement(cf); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "dropColumnFamilyStatement"


    // $ANTLR start "dropIndexStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:392:1: dropIndexStatement returns [DropIndexStatement expr] : K_DROP K_INDEX index= IDENT ;
    public final DropIndexStatement dropIndexStatement() throws RecognitionException {
        DropIndexStatement expr = null;

        Token index=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:396:5: ( K_DROP K_INDEX index= IDENT )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:397:7: K_DROP K_INDEX index= IDENT
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropIndexStatement1871); 
            match(input,K_INDEX,FOLLOW_K_INDEX_in_dropIndexStatement1873); 
            index=(Token)match(input,IDENT,FOLLOW_IDENT_in_dropIndexStatement1877); 
             expr = new DropIndexStatement((index!=null?index.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "dropIndexStatement"


    // $ANTLR start "truncateStatement"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:401:1: truncateStatement returns [TruncateStatement stmt] : K_TRUNCATE cf= columnFamilyName ;
    public final TruncateStatement truncateStatement() throws RecognitionException {
        TruncateStatement stmt = null;

        CFName cf = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:405:5: ( K_TRUNCATE cf= columnFamilyName )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:405:7: K_TRUNCATE cf= columnFamilyName
            {
            match(input,K_TRUNCATE,FOLLOW_K_TRUNCATE_in_truncateStatement1908); 
            pushFollow(FOLLOW_columnFamilyName_in_truncateStatement1912);
            cf=columnFamilyName();

            state._fsp--;

             stmt = new TruncateStatement(cf); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "truncateStatement"


    // $ANTLR start "cident"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:409:1: cident returns [ColumnIdentifier id] : (t= ( IDENT | UUID | INTEGER ) | t= QUOTED_NAME );
    public final ColumnIdentifier cident() throws RecognitionException {
        ColumnIdentifier id = null;

        Token t=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:413:5: (t= ( IDENT | UUID | INTEGER ) | t= QUOTED_NAME )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==INTEGER||LA39_0==IDENT||LA39_0==UUID) ) {
                alt39=1;
            }
            else if ( (LA39_0==QUOTED_NAME) ) {
                alt39=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:413:7: t= ( IDENT | UUID | INTEGER )
                    {
                    t=(Token)input.LT(1);
                    if ( input.LA(1)==INTEGER||input.LA(1)==IDENT||input.LA(1)==UUID ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     id = new ColumnIdentifier((t!=null?t.getText():null), false); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:414:7: t= QUOTED_NAME
                    {
                    t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cident1966); 
                     id = new ColumnIdentifier((t!=null?t.getText():null), true); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return id;
    }
    // $ANTLR end "cident"


    // $ANTLR start "keyspaceName"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:418:1: keyspaceName returns [String id] : cfOrKsName[name, true] ;
    public final String keyspaceName() throws RecognitionException {
        String id = null;

         CFName name = new CFName(); 
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:420:5: ( cfOrKsName[name, true] )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:420:7: cfOrKsName[name, true]
            {
            pushFollow(FOLLOW_cfOrKsName_in_keyspaceName2014);
            cfOrKsName(name, true);

            state._fsp--;

             id = name.getKeyspace(); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return id;
    }
    // $ANTLR end "keyspaceName"


    // $ANTLR start "columnFamilyName"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:423:1: columnFamilyName returns [CFName name] : ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false] ;
    public final CFName columnFamilyName() throws RecognitionException {
        CFName name = null;

         name = new CFName(); 
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:425:5: ( ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false] )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:425:7: ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false]
            {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:425:7: ( cfOrKsName[name, true] '.' )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==IDENT) ) {
                int LA40_1 = input.LA(2);

                if ( (LA40_1==91) ) {
                    alt40=1;
                }
            }
            else if ( (LA40_0==QUOTED_NAME) ) {
                int LA40_2 = input.LA(2);

                if ( (LA40_2==91) ) {
                    alt40=1;
                }
            }
            switch (alt40) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:425:8: cfOrKsName[name, true] '.'
                    {
                    pushFollow(FOLLOW_cfOrKsName_in_columnFamilyName2048);
                    cfOrKsName(name, true);

                    state._fsp--;

                    match(input,91,FOLLOW_91_in_columnFamilyName2051); 

                    }
                    break;

            }

            pushFollow(FOLLOW_cfOrKsName_in_columnFamilyName2055);
            cfOrKsName(name, false);

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return name;
    }
    // $ANTLR end "columnFamilyName"


    // $ANTLR start "cfOrKsName"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:428:1: cfOrKsName[CFName name, boolean isKs] : (t= IDENT | t= QUOTED_NAME );
    public final void cfOrKsName(CFName name, boolean isKs) throws RecognitionException {
        Token t=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:429:5: (t= IDENT | t= QUOTED_NAME )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==IDENT) ) {
                alt41=1;
            }
            else if ( (LA41_0==QUOTED_NAME) ) {
                alt41=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:429:7: t= IDENT
                    {
                    t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cfOrKsName2076); 
                     if (isKs) name.setKeyspace((t!=null?t.getText():null), false); else name.setColumnFamily((t!=null?t.getText():null), false); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:430:7: t= QUOTED_NAME
                    {
                    t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cfOrKsName2095); 
                     if (isKs) name.setKeyspace((t!=null?t.getText():null), true); else name.setColumnFamily((t!=null?t.getText():null), true); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfOrKsName"


    // $ANTLR start "cidentList"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:433:1: cidentList returns [List<ColumnIdentifier> items] : t1= cident ( ',' tN= cident )* ;
    public final List<ColumnIdentifier> cidentList() throws RecognitionException {
        List<ColumnIdentifier> items = null;

        ColumnIdentifier t1 = null;

        ColumnIdentifier tN = null;


         items = new ArrayList<ColumnIdentifier>(); 
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:435:5: (t1= cident ( ',' tN= cident )* )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:435:8: t1= cident ( ',' tN= cident )*
            {
            pushFollow(FOLLOW_cident_in_cidentList2130);
            t1=cident();

            state._fsp--;

             items.add(t1); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:435:38: ( ',' tN= cident )*
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==89) ) {
                    alt42=1;
                }


                switch (alt42) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:435:39: ',' tN= cident
            	    {
            	    match(input,89,FOLLOW_89_in_cidentList2135); 
            	    pushFollow(FOLLOW_cident_in_cidentList2139);
            	    tN=cident();

            	    state._fsp--;

            	     items.add(tN); 

            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return items;
    }
    // $ANTLR end "cidentList"


    // $ANTLR start "term"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:439:1: term returns [Term term] : (t= ( STRING_LITERAL | UUID | IDENT | INTEGER | FLOAT ) | t= QMARK );
    public final Term term() throws RecognitionException {
        Term term = null;

        Token t=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:440:5: (t= ( STRING_LITERAL | UUID | IDENT | INTEGER | FLOAT ) | t= QMARK )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==INTEGER||LA43_0==IDENT||LA43_0==UUID||(LA43_0>=STRING_LITERAL && LA43_0<=FLOAT)) ) {
                alt43=1;
            }
            else if ( (LA43_0==QMARK) ) {
                alt43=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:440:7: t= ( STRING_LITERAL | UUID | IDENT | INTEGER | FLOAT )
                    {
                    t=(Token)input.LT(1);
                    if ( input.LA(1)==INTEGER||input.LA(1)==IDENT||input.LA(1)==UUID||(input.LA(1)>=STRING_LITERAL && input.LA(1)<=FLOAT) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     term = new Term((t!=null?t.getText():null), (t!=null?t.getType():0)); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:441:7: t= QMARK
                    {
                    t=(Token)match(input,QMARK,FOLLOW_QMARK_in_term2198); 
                     term = new Term((t!=null?t.getText():null), (t!=null?t.getType():0), ++currentBindMarkerIdx); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return term;
    }
    // $ANTLR end "term"


    // $ANTLR start "intTerm"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:444:1: intTerm returns [Term integer] : (t= INTEGER | t= QMARK );
    public final Term intTerm() throws RecognitionException {
        Term integer = null;

        Token t=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:5: (t= INTEGER | t= QMARK )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==INTEGER) ) {
                alt44=1;
            }
            else if ( (LA44_0==QMARK) ) {
                alt44=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:7: t= INTEGER
                    {
                    t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_intTerm2268); 
                     integer = new Term((t!=null?t.getText():null), (t!=null?t.getType():0)); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:446:7: t= QMARK
                    {
                    t=(Token)match(input,QMARK,FOLLOW_QMARK_in_intTerm2280); 
                     integer = new Term((t!=null?t.getText():null), (t!=null?t.getType():0), ++currentBindMarkerIdx); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return integer;
    }
    // $ANTLR end "intTerm"


    // $ANTLR start "termPairWithOperation"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:449:1: termPairWithOperation[Map<ColumnIdentifier, Operation> columns] : key= cident '=' (value= term | c= cident ( '+' v= intTerm | (op= '-' )? v= intTerm ) ) ;
    public final void termPairWithOperation(Map<ColumnIdentifier, Operation> columns) throws RecognitionException {
        Token op=null;
        ColumnIdentifier key = null;

        Term value = null;

        ColumnIdentifier c = null;

        Term v = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:450:5: (key= cident '=' (value= term | c= cident ( '+' v= intTerm | (op= '-' )? v= intTerm ) ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:450:7: key= cident '=' (value= term | c= cident ( '+' v= intTerm | (op= '-' )? v= intTerm ) )
            {
            pushFollow(FOLLOW_cident_in_termPairWithOperation2304);
            key=cident();

            state._fsp--;

            match(input,90,FOLLOW_90_in_termPairWithOperation2306); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:451:9: (value= term | c= cident ( '+' v= intTerm | (op= '-' )? v= intTerm ) )
            int alt47=2;
            switch ( input.LA(1) ) {
            case INTEGER:
            case IDENT:
            case UUID:
                {
                int LA47_1 = input.LA(2);

                if ( (LA47_1==INTEGER||LA47_1==QMARK||(LA47_1>=92 && LA47_1<=93)) ) {
                    alt47=2;
                }
                else if ( (LA47_1==K_WHERE||LA47_1==89) ) {
                    alt47=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 47, 1, input);

                    throw nvae;
                }
                }
                break;
            case STRING_LITERAL:
            case FLOAT:
            case QMARK:
                {
                alt47=1;
                }
                break;
            case QUOTED_NAME:
                {
                alt47=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }

            switch (alt47) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:451:11: value= term
                    {
                    pushFollow(FOLLOW_term_in_termPairWithOperation2320);
                    value=term();

                    state._fsp--;

                     columns.put(key, new Operation(value)); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:452:11: c= cident ( '+' v= intTerm | (op= '-' )? v= intTerm )
                    {
                    pushFollow(FOLLOW_cident_in_termPairWithOperation2336);
                    c=cident();

                    state._fsp--;

                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:452:20: ( '+' v= intTerm | (op= '-' )? v= intTerm )
                    int alt46=2;
                    int LA46_0 = input.LA(1);

                    if ( (LA46_0==92) ) {
                        alt46=1;
                    }
                    else if ( (LA46_0==INTEGER||LA46_0==QMARK||LA46_0==93) ) {
                        alt46=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 46, 0, input);

                        throw nvae;
                    }
                    switch (alt46) {
                        case 1 :
                            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:452:22: '+' v= intTerm
                            {
                            match(input,92,FOLLOW_92_in_termPairWithOperation2340); 
                            pushFollow(FOLLOW_intTerm_in_termPairWithOperation2348);
                            v=intTerm();

                            state._fsp--;

                             columns.put(key, new Operation(c, Operation.Type.PLUS, v)); 

                            }
                            break;
                        case 2 :
                            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:453:22: (op= '-' )? v= intTerm
                            {
                            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:453:24: (op= '-' )?
                            int alt45=2;
                            int LA45_0 = input.LA(1);

                            if ( (LA45_0==93) ) {
                                alt45=1;
                            }
                            switch (alt45) {
                                case 1 :
                                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:453:24: op= '-'
                                    {
                                    op=(Token)match(input,93,FOLLOW_93_in_termPairWithOperation2375); 

                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_intTerm_in_termPairWithOperation2380);
                            v=intTerm();

                            state._fsp--;


                                                   validateMinusSupplied(op, v, input);
                                                   if (op == null)
                                                       v = new Term(-(Long.valueOf(v.getText())), v.getType());
                                                   columns.put(key, new Operation(c, Operation.Type.MINUS, v));
                                                 

                            }
                            break;

                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "termPairWithOperation"


    // $ANTLR start "property"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:464:1: property returns [String str] : p= ( COMPIDENT | IDENT ) ;
    public final String property() throws RecognitionException {
        String str = null;

        Token p=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:465:5: (p= ( COMPIDENT | IDENT ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:465:7: p= ( COMPIDENT | IDENT )
            {
            p=(Token)input.LT(1);
            if ( input.LA(1)==IDENT||input.LA(1)==COMPIDENT ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

             str = (p!=null?p.getText():null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "property"


    // $ANTLR start "propertyValue"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:468:1: propertyValue returns [String str] : v= ( STRING_LITERAL | IDENT | INTEGER | FLOAT ) ;
    public final String propertyValue() throws RecognitionException {
        String str = null;

        Token v=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:469:5: (v= ( STRING_LITERAL | IDENT | INTEGER | FLOAT ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:469:7: v= ( STRING_LITERAL | IDENT | INTEGER | FLOAT )
            {
            v=(Token)input.LT(1);
            if ( input.LA(1)==INTEGER||input.LA(1)==IDENT||(input.LA(1)>=STRING_LITERAL && input.LA(1)<=FLOAT) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

             str = (v!=null?v.getText():null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "propertyValue"


    // $ANTLR start "properties"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:472:1: properties returns [Map<String, String> props] : k1= property '=' v1= propertyValue ( K_AND kn= property '=' vn= propertyValue )* ;
    public final Map<String, String> properties() throws RecognitionException {
        Map<String, String> props = null;

        String k1 = null;

        String v1 = null;

        String kn = null;

        String vn = null;


         props = new HashMap<String, String>(); 
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:474:5: (k1= property '=' v1= propertyValue ( K_AND kn= property '=' vn= propertyValue )* )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:474:7: k1= property '=' v1= propertyValue ( K_AND kn= property '=' vn= propertyValue )*
            {
            pushFollow(FOLLOW_property_in_properties2536);
            k1=property();

            state._fsp--;

            match(input,90,FOLLOW_90_in_properties2538); 
            pushFollow(FOLLOW_propertyValue_in_properties2542);
            v1=propertyValue();

            state._fsp--;

             props.put(k1, v1); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:474:64: ( K_AND kn= property '=' vn= propertyValue )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==K_AND) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:474:65: K_AND kn= property '=' vn= propertyValue
            	    {
            	    match(input,K_AND,FOLLOW_K_AND_in_properties2547); 
            	    pushFollow(FOLLOW_property_in_properties2551);
            	    kn=property();

            	    state._fsp--;

            	    match(input,90,FOLLOW_90_in_properties2553); 
            	    pushFollow(FOLLOW_propertyValue_in_properties2557);
            	    vn=propertyValue();

            	    state._fsp--;

            	     props.put(kn, vn); 

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return props;
    }
    // $ANTLR end "properties"


    // $ANTLR start "relation"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:1: relation returns [Relation rel] : (name= cident type= ( '=' | '<' | '<=' | '>=' | '>' ) t= term | name= cident K_IN '(' f1= term ( ',' fN= term )* ')' );
    public final Relation relation() throws RecognitionException {
        Relation rel = null;

        Token type=null;
        ColumnIdentifier name = null;

        Term t = null;

        Term f1 = null;

        Term fN = null;


        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:478:5: (name= cident type= ( '=' | '<' | '<=' | '>=' | '>' ) t= term | name= cident K_IN '(' f1= term ( ',' fN= term )* ')' )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==INTEGER||LA50_0==IDENT||LA50_0==UUID) ) {
                int LA50_1 = input.LA(2);

                if ( (LA50_1==K_IN) ) {
                    alt50=2;
                }
                else if ( (LA50_1==90||(LA50_1>=94 && LA50_1<=97)) ) {
                    alt50=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 50, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA50_0==QUOTED_NAME) ) {
                int LA50_2 = input.LA(2);

                if ( (LA50_2==K_IN) ) {
                    alt50=2;
                }
                else if ( (LA50_2==90||(LA50_2>=94 && LA50_2<=97)) ) {
                    alt50=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 50, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:478:7: name= cident type= ( '=' | '<' | '<=' | '>=' | '>' ) t= term
                    {
                    pushFollow(FOLLOW_cident_in_relation2585);
                    name=cident();

                    state._fsp--;

                    type=(Token)input.LT(1);
                    if ( input.LA(1)==90||(input.LA(1)>=94 && input.LA(1)<=97) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_term_in_relation2611);
                    t=term();

                    state._fsp--;

                     rel = new Relation(name, (type!=null?type.getText():null), t); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:479:7: name= cident K_IN '(' f1= term ( ',' fN= term )* ')'
                    {
                    pushFollow(FOLLOW_cident_in_relation2623);
                    name=cident();

                    state._fsp--;

                    match(input,K_IN,FOLLOW_K_IN_in_relation2625); 
                     rel = Relation.createInRelation(name); 
                    match(input,86,FOLLOW_86_in_relation2635); 
                    pushFollow(FOLLOW_term_in_relation2639);
                    f1=term();

                    state._fsp--;

                     rel.addInValue(f1); 
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:480:44: ( ',' fN= term )*
                    loop49:
                    do {
                        int alt49=2;
                        int LA49_0 = input.LA(1);

                        if ( (LA49_0==89) ) {
                            alt49=1;
                        }


                        switch (alt49) {
                    	case 1 :
                    	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:480:45: ',' fN= term
                    	    {
                    	    match(input,89,FOLLOW_89_in_relation2644); 
                    	    pushFollow(FOLLOW_term_in_relation2648);
                    	    fN=term();

                    	    state._fsp--;

                    	     rel.addInValue(fN); 

                    	    }
                    	    break;

                    	default :
                    	    break loop49;
                        }
                    } while (true);

                    match(input,87,FOLLOW_87_in_relation2655); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return rel;
    }
    // $ANTLR end "relation"


    // $ANTLR start "comparatorType"
    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:483:1: comparatorType returns [String str] : c= ( IDENT | STRING_LITERAL | K_TIMESTAMP ) ;
    public final String comparatorType() throws RecognitionException {
        String str = null;

        Token c=null;

        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:484:5: (c= ( IDENT | STRING_LITERAL | K_TIMESTAMP ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:484:7: c= ( IDENT | STRING_LITERAL | K_TIMESTAMP )
            {
            c=(Token)input.LT(1);
            if ( input.LA(1)==K_TIMESTAMP||input.LA(1)==IDENT||input.LA(1)==STRING_LITERAL ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

             str = (c!=null?c.getText():null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "comparatorType"

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    static final String DFA2_eotS =
        "\21\uffff";
    static final String DFA2_eofS =
        "\21\uffff";
    static final String DFA2_minS =
        "\1\4\7\uffff\2\37\7\uffff";
    static final String DFA2_maxS =
        "\1\55\7\uffff\2\46\7\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\2\uffff\1\16\1\10\1\11\1\12"+
        "\1\13\1\14\1\15";
    static final String DFA2_specialS =
        "\21\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\6\1\1\15\uffff\1\2\4\uffff\1\3\1\uffff\1\5\1\4\2\uffff\1"+
            "\10\12\uffff\1\12\2\uffff\1\11\1\7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\13\1\uffff\1\14\4\uffff\1\15",
            "\1\16\1\uffff\1\17\4\uffff\1\20",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "122:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement | st2= insertStatement | st3= updateStatement | st4= batchStatement | st5= deleteStatement | st6= useStatement | st7= truncateStatement | st8= createKeyspaceStatement | st9= createColumnFamilyStatement | st10= createIndexStatement | st11= dropKeyspaceStatement | st12= dropColumnFamilyStatement | st13= dropIndexStatement | st14= alterTableStatement );";
        }
    }
 

    public static final BitSet FOLLOW_cqlStatement_in_query72 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_85_in_query75 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_EOF_in_query79 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectStatement_in_cqlStatement113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_insertStatement_in_cqlStatement138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateStatement_in_cqlStatement163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_batchStatement_in_cqlStatement188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteStatement_in_cqlStatement214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_useStatement_in_cqlStatement239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_truncateStatement_in_cqlStatement267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createKeyspaceStatement_in_cqlStatement290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createColumnFamilyStatement_in_cqlStatement307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createIndexStatement_in_cqlStatement319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropKeyspaceStatement_in_cqlStatement338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropColumnFamilyStatement_in_cqlStatement356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropIndexStatement_in_cqlStatement370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alterTableStatement_in_cqlStatement391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_USE_in_useStatement424 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_keyspaceName_in_useStatement428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_SELECT_in_selectStatement462 = new BitSet(new long[]{0x0000C08000020040L,0x0000000001000000L});
    public static final BitSet FOLLOW_selectClause_in_selectStatement468 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_K_COUNT_in_selectStatement473 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_selectStatement475 = new BitSet(new long[]{0x0000C08000020000L,0x0000000001000000L});
    public static final BitSet FOLLOW_selectClause_in_selectStatement479 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_selectStatement481 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_K_FROM_in_selectStatement494 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_selectStatement498 = new BitSet(new long[]{0x0000000000011902L});
    public static final BitSet FOLLOW_K_USING_in_selectStatement508 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_K_CONSISTENCY_in_selectStatement510 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_K_LEVEL_in_selectStatement512 = new BitSet(new long[]{0x0000000000011802L});
    public static final BitSet FOLLOW_K_WHERE_in_selectStatement527 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_whereClause_in_selectStatement531 = new BitSet(new long[]{0x0000000000011002L});
    public static final BitSet FOLLOW_K_ORDER_in_selectStatement544 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_K_BY_in_selectStatement546 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_selectStatement550 = new BitSet(new long[]{0x000000000001C002L});
    public static final BitSet FOLLOW_K_ASC_in_selectStatement555 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_K_DESC_in_selectStatement559 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_K_LIMIT_in_selectStatement576 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_INTEGER_in_selectStatement580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cidentList_in_selectClause616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_88_in_selectClause626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_in_whereClause669 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_K_AND_in_whereClause674 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_relation_in_whereClause678 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_K_INSERT_in_insertStatement714 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_K_INTO_in_insertStatement716 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_insertStatement720 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_insertStatement732 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_insertStatement736 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_insertStatement743 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_insertStatement747 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_87_in_insertStatement754 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_K_VALUES_in_insertStatement764 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_insertStatement776 = new BitSet(new long[]{0x0007408000020000L});
    public static final BitSet FOLLOW_term_in_insertStatement780 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_insertStatement786 = new BitSet(new long[]{0x0007408000020000L});
    public static final BitSet FOLLOW_term_in_insertStatement790 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_87_in_insertStatement797 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_usingClause_in_insertStatement809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_USING_in_usingClause839 = new BitSet(new long[]{0x0000000000C00200L});
    public static final BitSet FOLLOW_usingClauseObjective_in_usingClause841 = new BitSet(new long[]{0x0000000000C40202L});
    public static final BitSet FOLLOW_K_AND_in_usingClause846 = new BitSet(new long[]{0x0000000000C00200L});
    public static final BitSet FOLLOW_usingClauseObjective_in_usingClause849 = new BitSet(new long[]{0x0000000000C40202L});
    public static final BitSet FOLLOW_K_USING_in_usingClauseDelete871 = new BitSet(new long[]{0x0000000000400200L});
    public static final BitSet FOLLOW_usingClauseDeleteObjective_in_usingClauseDelete873 = new BitSet(new long[]{0x0000000000440202L});
    public static final BitSet FOLLOW_K_AND_in_usingClauseDelete878 = new BitSet(new long[]{0x0000000000400200L});
    public static final BitSet FOLLOW_usingClauseDeleteObjective_in_usingClauseDelete881 = new BitSet(new long[]{0x0000000000440202L});
    public static final BitSet FOLLOW_K_CONSISTENCY_in_usingClauseDeleteObjective903 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_K_LEVEL_in_usingClauseDeleteObjective905 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TIMESTAMP_in_usingClauseDeleteObjective916 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_INTEGER_in_usingClauseDeleteObjective920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_usingClauseDeleteObjective_in_usingClauseObjective940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TTL_in_usingClauseObjective949 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_INTEGER_in_usingClauseObjective953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_UPDATE_in_updateStatement987 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_updateStatement991 = new BitSet(new long[]{0x0000000002000100L});
    public static final BitSet FOLLOW_usingClause_in_updateStatement1001 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_K_SET_in_updateStatement1013 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_termPairWithOperation_in_updateStatement1015 = new BitSet(new long[]{0x0000000000000800L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_updateStatement1019 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_termPairWithOperation_in_updateStatement1021 = new BitSet(new long[]{0x0000000000000800L,0x0000000002000000L});
    public static final BitSet FOLLOW_K_WHERE_in_updateStatement1032 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_whereClause_in_updateStatement1036 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DELETE_in_deleteStatement1076 = new BitSet(new long[]{0x0000C08000020080L});
    public static final BitSet FOLLOW_cidentList_in_deleteStatement1082 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_K_FROM_in_deleteStatement1095 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_deleteStatement1099 = new BitSet(new long[]{0x0000000000000900L});
    public static final BitSet FOLLOW_usingClauseDelete_in_deleteStatement1109 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_K_WHERE_in_deleteStatement1121 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_whereClause_in_deleteStatement1125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BEGIN_in_batchStatement1166 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_K_BATCH_in_batchStatement1168 = new BitSet(new long[]{0x0000000005080100L});
    public static final BitSet FOLLOW_usingClause_in_batchStatement1172 = new BitSet(new long[]{0x0000000005080100L});
    public static final BitSet FOLLOW_batchStatementObjective_in_batchStatement1190 = new BitSet(new long[]{0x0000000025080100L,0x0000000000200000L});
    public static final BitSet FOLLOW_85_in_batchStatement1192 = new BitSet(new long[]{0x0000000025080100L});
    public static final BitSet FOLLOW_batchStatementObjective_in_batchStatement1201 = new BitSet(new long[]{0x0000000025080100L,0x0000000000200000L});
    public static final BitSet FOLLOW_85_in_batchStatement1203 = new BitSet(new long[]{0x0000000025080100L});
    public static final BitSet FOLLOW_K_APPLY_in_batchStatement1217 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_K_BATCH_in_batchStatement1219 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_insertStatement_in_batchStatementObjective1250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateStatement_in_batchStatementObjective1263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteStatement_in_batchStatementObjective1276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createKeyspaceStatement1302 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_K_KEYSPACE_in_createKeyspaceStatement1304 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_keyspaceName_in_createKeyspaceStatement1308 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_K_WITH_in_createKeyspaceStatement1316 = new BitSet(new long[]{0x0008008000000000L});
    public static final BitSet FOLLOW_properties_in_createKeyspaceStatement1320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createColumnFamilyStatement1345 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_K_COLUMNFAMILY_in_createColumnFamilyStatement1347 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_createColumnFamilyStatement1351 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_cfamDefinition_in_createColumnFamilyStatement1361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_cfamDefinition1380 = new BitSet(new long[]{0x0000C08400020000L});
    public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition1382 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_89_in_cfamDefinition1387 = new BitSet(new long[]{0x0000C08400020000L,0x0000000002800000L});
    public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition1389 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_87_in_cfamDefinition1396 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_K_WITH_in_cfamDefinition1406 = new BitSet(new long[]{0x0008009000000000L});
    public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition1408 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_K_AND_in_cfamDefinition1413 = new BitSet(new long[]{0x0008009000000000L});
    public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition1415 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_cident_in_cfamColumns1441 = new BitSet(new long[]{0x0001008000400000L});
    public static final BitSet FOLLOW_comparatorType_in_cfamColumns1445 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns1450 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_K_KEY_in_cfamColumns1452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns1464 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_K_KEY_in_cfamColumns1466 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_cfamColumns1468 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_cfamColumns1472 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_89_in_cfamColumns1477 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_cfamColumns1481 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_87_in_cfamColumns1488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_cfamProperty1508 = new BitSet(new long[]{0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_90_in_cfamProperty1510 = new BitSet(new long[]{0x0003008000020000L});
    public static final BitSet FOLLOW_propertyValue_in_cfamProperty1514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_COMPACT_in_cfamProperty1524 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_K_STORAGE_in_cfamProperty1526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createIndexStatement1551 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_K_INDEX_in_createIndexStatement1553 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_IDENT_in_createIndexStatement1558 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_K_ON_in_createIndexStatement1562 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_createIndexStatement1566 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_createIndexStatement1568 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_createIndexStatement1572 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_createIndexStatement1574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement1614 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_K_COLUMNFAMILY_in_alterTableStatement1616 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_alterTableStatement1620 = new BitSet(new long[]{0x00001A0100000000L});
    public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement1634 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement1638 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_K_TYPE_in_alterTableStatement1640 = new BitSet(new long[]{0x0001008000400000L});
    public static final BitSet FOLLOW_comparatorType_in_alterTableStatement1644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ADD_in_alterTableStatement1660 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement1666 = new BitSet(new long[]{0x0001008000400000L});
    public static final BitSet FOLLOW_comparatorType_in_alterTableStatement1670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_alterTableStatement1693 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement1698 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_WITH_in_alterTableStatement1738 = new BitSet(new long[]{0x0008008000000000L});
    public static final BitSet FOLLOW_properties_in_alterTableStatement1743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_dropKeyspaceStatement1803 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement1805 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_keyspaceName_in_dropKeyspaceStatement1809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_dropColumnFamilyStatement1834 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_K_COLUMNFAMILY_in_dropColumnFamilyStatement1836 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_dropColumnFamilyStatement1840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_dropIndexStatement1871 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_K_INDEX_in_dropIndexStatement1873 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_IDENT_in_dropIndexStatement1877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TRUNCATE_in_truncateStatement1908 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_columnFamilyName_in_truncateStatement1912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cident1942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_NAME_in_cident1966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cfOrKsName_in_keyspaceName2014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cfOrKsName_in_columnFamilyName2048 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_91_in_columnFamilyName2051 = new BitSet(new long[]{0x0000808000000000L});
    public static final BitSet FOLLOW_cfOrKsName_in_columnFamilyName2055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cfOrKsName2076 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_NAME_in_cfOrKsName2095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_cidentList2130 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_cidentList2135 = new BitSet(new long[]{0x0000C08000020000L});
    public static final BitSet FOLLOW_cident_in_cidentList2139 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_set_in_term2167 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QMARK_in_term2198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_intTerm2268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QMARK_in_intTerm2280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_termPairWithOperation2304 = new BitSet(new long[]{0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_90_in_termPairWithOperation2306 = new BitSet(new long[]{0x0007C08000020000L});
    public static final BitSet FOLLOW_term_in_termPairWithOperation2320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_termPairWithOperation2336 = new BitSet(new long[]{0x0004000000020000L,0x0000000030000000L});
    public static final BitSet FOLLOW_92_in_termPairWithOperation2340 = new BitSet(new long[]{0x0004000000020000L,0x0000000030000000L});
    public static final BitSet FOLLOW_intTerm_in_termPairWithOperation2348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_termPairWithOperation2375 = new BitSet(new long[]{0x0004000000020000L,0x0000000030000000L});
    public static final BitSet FOLLOW_intTerm_in_termPairWithOperation2380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property2458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_propertyValue2489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_properties2536 = new BitSet(new long[]{0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_90_in_properties2538 = new BitSet(new long[]{0x0003008000020000L});
    public static final BitSet FOLLOW_propertyValue_in_properties2542 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_K_AND_in_properties2547 = new BitSet(new long[]{0x0008008000000000L});
    public static final BitSet FOLLOW_property_in_properties2551 = new BitSet(new long[]{0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_90_in_properties2553 = new BitSet(new long[]{0x0003008000020000L});
    public static final BitSet FOLLOW_propertyValue_in_properties2557 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_cident_in_relation2585 = new BitSet(new long[]{0x0000000000000000L,0x00000003C4000000L});
    public static final BitSet FOLLOW_set_in_relation2589 = new BitSet(new long[]{0x0007408000020000L});
    public static final BitSet FOLLOW_term_in_relation2611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_relation2623 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_K_IN_in_relation2625 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_relation2635 = new BitSet(new long[]{0x0007408000020000L});
    public static final BitSet FOLLOW_term_in_relation2639 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_89_in_relation2644 = new BitSet(new long[]{0x0007408000020000L});
    public static final BitSet FOLLOW_term_in_relation2648 = new BitSet(new long[]{0x0000000000000000L,0x0000000002800000L});
    public static final BitSet FOLLOW_87_in_relation2655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_comparatorType2678 = new BitSet(new long[]{0x0000000000000002L});

}