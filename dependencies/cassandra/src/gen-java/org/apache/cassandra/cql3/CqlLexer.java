// $ANTLR 3.2 Sep 23, 2009 12:02:23 /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g 2012-05-24 10:52:13

    package org.apache.cassandra.cql3;

    import org.apache.cassandra.thrift.InvalidRequestException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CqlLexer extends Lexer {
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
    public static final int K_TRUNCATE=45;
    public static final int COMMENT=83;
    public static final int T__97=97;
    public static final int K_ORDER=12;
    public static final int T__96=96;
    public static final int T__95=95;
    public static final int D=66;
    public static final int E=54;
    public static final int F=58;
    public static final int G=72;
    public static final int K_TYPE=42;
    public static final int K_KEYSPACE=31;
    public static final int K_COUNT=6;
    public static final int A=64;
    public static final int B=74;
    public static final int C=56;
    public static final int L=55;
    public static final int M=61;
    public static final int N=65;
    public static final int O=60;
    public static final int H=63;
    public static final int I=69;
    public static final int J=77;
    public static final int K_UPDATE=24;
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
    public static final int COMPIDENT=51;
    public static final int K_WITH=32;
    public static final int K_IN=52;
    public static final int K_FROM=7;
    public static final int K_COLUMNFAMILY=33;
    public static final int K_DROP=44;

        List<Token> tokens = new ArrayList<Token>();

        public void emit(Token token)
        {
            state.token = token;
            tokens.add(token);
        }

        public Token nextToken()
        {
            super.nextToken();
            if (tokens.size() == 0)
                return Token.EOF_TOKEN;
            return tokens.remove(0);
        }

        private List<String> recognitionErrors = new ArrayList<String>();

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


    // delegates
    // delegators

    public CqlLexer() {;} 
    public CqlLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CqlLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g"; }

    // $ANTLR start "T__85"
    public final void mT__85() throws RecognitionException {
        try {
            int _type = T__85;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:50:7: ( ';' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:50:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__85"

    // $ANTLR start "T__86"
    public final void mT__86() throws RecognitionException {
        try {
            int _type = T__86;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:51:7: ( '(' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:51:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__86"

    // $ANTLR start "T__87"
    public final void mT__87() throws RecognitionException {
        try {
            int _type = T__87;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:52:7: ( ')' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:52:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__87"

    // $ANTLR start "T__88"
    public final void mT__88() throws RecognitionException {
        try {
            int _type = T__88;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:53:7: ( '\\*' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:53:9: '\\*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__88"

    // $ANTLR start "T__89"
    public final void mT__89() throws RecognitionException {
        try {
            int _type = T__89;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:54:7: ( ',' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:54:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__89"

    // $ANTLR start "T__90"
    public final void mT__90() throws RecognitionException {
        try {
            int _type = T__90;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:55:7: ( '=' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:55:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__90"

    // $ANTLR start "T__91"
    public final void mT__91() throws RecognitionException {
        try {
            int _type = T__91;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:56:7: ( '.' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:56:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__91"

    // $ANTLR start "T__92"
    public final void mT__92() throws RecognitionException {
        try {
            int _type = T__92;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:57:7: ( '+' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:57:9: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__92"

    // $ANTLR start "T__93"
    public final void mT__93() throws RecognitionException {
        try {
            int _type = T__93;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:58:7: ( '-' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:58:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__93"

    // $ANTLR start "T__94"
    public final void mT__94() throws RecognitionException {
        try {
            int _type = T__94;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:59:7: ( '<' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:59:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__94"

    // $ANTLR start "T__95"
    public final void mT__95() throws RecognitionException {
        try {
            int _type = T__95;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:60:7: ( '<=' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:60:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__95"

    // $ANTLR start "T__96"
    public final void mT__96() throws RecognitionException {
        try {
            int _type = T__96;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:61:7: ( '>=' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:61:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__96"

    // $ANTLR start "T__97"
    public final void mT__97() throws RecognitionException {
        try {
            int _type = T__97;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:62:7: ( '>' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:62:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__97"

    // $ANTLR start "K_SELECT"
    public final void mK_SELECT() throws RecognitionException {
        try {
            int _type = K_SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:489:9: ( S E L E C T )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:489:16: S E L E C T
            {
            mS(); 
            mE(); 
            mL(); 
            mE(); 
            mC(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_SELECT"

    // $ANTLR start "K_FROM"
    public final void mK_FROM() throws RecognitionException {
        try {
            int _type = K_FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:490:7: ( F R O M )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:490:16: F R O M
            {
            mF(); 
            mR(); 
            mO(); 
            mM(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_FROM"

    // $ANTLR start "K_WHERE"
    public final void mK_WHERE() throws RecognitionException {
        try {
            int _type = K_WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:491:8: ( W H E R E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:491:16: W H E R E
            {
            mW(); 
            mH(); 
            mE(); 
            mR(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_WHERE"

    // $ANTLR start "K_AND"
    public final void mK_AND() throws RecognitionException {
        try {
            int _type = K_AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:492:6: ( A N D )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:492:16: A N D
            {
            mA(); 
            mN(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_AND"

    // $ANTLR start "K_KEY"
    public final void mK_KEY() throws RecognitionException {
        try {
            int _type = K_KEY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:493:6: ( K E Y )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:493:16: K E Y
            {
            mK(); 
            mE(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_KEY"

    // $ANTLR start "K_INSERT"
    public final void mK_INSERT() throws RecognitionException {
        try {
            int _type = K_INSERT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:494:9: ( I N S E R T )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:494:16: I N S E R T
            {
            mI(); 
            mN(); 
            mS(); 
            mE(); 
            mR(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INSERT"

    // $ANTLR start "K_UPDATE"
    public final void mK_UPDATE() throws RecognitionException {
        try {
            int _type = K_UPDATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:495:9: ( U P D A T E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:495:16: U P D A T E
            {
            mU(); 
            mP(); 
            mD(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_UPDATE"

    // $ANTLR start "K_WITH"
    public final void mK_WITH() throws RecognitionException {
        try {
            int _type = K_WITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:496:7: ( W I T H )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:496:16: W I T H
            {
            mW(); 
            mI(); 
            mT(); 
            mH(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_WITH"

    // $ANTLR start "K_LIMIT"
    public final void mK_LIMIT() throws RecognitionException {
        try {
            int _type = K_LIMIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:497:8: ( L I M I T )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:497:16: L I M I T
            {
            mL(); 
            mI(); 
            mM(); 
            mI(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_LIMIT"

    // $ANTLR start "K_USING"
    public final void mK_USING() throws RecognitionException {
        try {
            int _type = K_USING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:8: ( U S I N G )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:16: U S I N G
            {
            mU(); 
            mS(); 
            mI(); 
            mN(); 
            mG(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_USING"

    // $ANTLR start "K_CONSISTENCY"
    public final void mK_CONSISTENCY() throws RecognitionException {
        try {
            int _type = K_CONSISTENCY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:499:14: ( C O N S I S T E N C Y )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:499:16: C O N S I S T E N C Y
            {
            mC(); 
            mO(); 
            mN(); 
            mS(); 
            mI(); 
            mS(); 
            mT(); 
            mE(); 
            mN(); 
            mC(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_CONSISTENCY"

    // $ANTLR start "K_LEVEL"
    public final void mK_LEVEL() throws RecognitionException {
        try {
            int _type = K_LEVEL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:500:8: ( ( O N E | Q U O R U M | A L L | A N Y | L O C A L '_' Q U O R U M | E A C H '_' Q U O R U M | T W O | T H R E E ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:500:16: ( O N E | Q U O R U M | A L L | A N Y | L O C A L '_' Q U O R U M | E A C H '_' Q U O R U M | T W O | T H R E E )
            {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:500:16: ( O N E | Q U O R U M | A L L | A N Y | L O C A L '_' Q U O R U M | E A C H '_' Q U O R U M | T W O | T H R E E )
            int alt1=8;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:500:18: O N E
                    {
                    mO(); 
                    mN(); 
                    mE(); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:501:18: Q U O R U M
                    {
                    mQ(); 
                    mU(); 
                    mO(); 
                    mR(); 
                    mU(); 
                    mM(); 

                    }
                    break;
                case 3 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:502:18: A L L
                    {
                    mA(); 
                    mL(); 
                    mL(); 

                    }
                    break;
                case 4 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:503:18: A N Y
                    {
                    mA(); 
                    mN(); 
                    mY(); 

                    }
                    break;
                case 5 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:504:18: L O C A L '_' Q U O R U M
                    {
                    mL(); 
                    mO(); 
                    mC(); 
                    mA(); 
                    mL(); 
                    match('_'); 
                    mQ(); 
                    mU(); 
                    mO(); 
                    mR(); 
                    mU(); 
                    mM(); 

                    }
                    break;
                case 6 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:505:18: E A C H '_' Q U O R U M
                    {
                    mE(); 
                    mA(); 
                    mC(); 
                    mH(); 
                    match('_'); 
                    mQ(); 
                    mU(); 
                    mO(); 
                    mR(); 
                    mU(); 
                    mM(); 

                    }
                    break;
                case 7 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:506:18: T W O
                    {
                    mT(); 
                    mW(); 
                    mO(); 

                    }
                    break;
                case 8 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:507:18: T H R E E
                    {
                    mT(); 
                    mH(); 
                    mR(); 
                    mE(); 
                    mE(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_LEVEL"

    // $ANTLR start "K_USE"
    public final void mK_USE() throws RecognitionException {
        try {
            int _type = K_USE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:6: ( U S E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:16: U S E
            {
            mU(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_USE"

    // $ANTLR start "K_COUNT"
    public final void mK_COUNT() throws RecognitionException {
        try {
            int _type = K_COUNT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:511:8: ( C O U N T )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:511:16: C O U N T
            {
            mC(); 
            mO(); 
            mU(); 
            mN(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_COUNT"

    // $ANTLR start "K_SET"
    public final void mK_SET() throws RecognitionException {
        try {
            int _type = K_SET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:512:6: ( S E T )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:512:16: S E T
            {
            mS(); 
            mE(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_SET"

    // $ANTLR start "K_BEGIN"
    public final void mK_BEGIN() throws RecognitionException {
        try {
            int _type = K_BEGIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:513:8: ( B E G I N )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:513:16: B E G I N
            {
            mB(); 
            mE(); 
            mG(); 
            mI(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BEGIN"

    // $ANTLR start "K_APPLY"
    public final void mK_APPLY() throws RecognitionException {
        try {
            int _type = K_APPLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:514:8: ( A P P L Y )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:514:16: A P P L Y
            {
            mA(); 
            mP(); 
            mP(); 
            mL(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_APPLY"

    // $ANTLR start "K_BATCH"
    public final void mK_BATCH() throws RecognitionException {
        try {
            int _type = K_BATCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:515:8: ( B A T C H )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:515:16: B A T C H
            {
            mB(); 
            mA(); 
            mT(); 
            mC(); 
            mH(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BATCH"

    // $ANTLR start "K_TRUNCATE"
    public final void mK_TRUNCATE() throws RecognitionException {
        try {
            int _type = K_TRUNCATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:516:11: ( T R U N C A T E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:516:16: T R U N C A T E
            {
            mT(); 
            mR(); 
            mU(); 
            mN(); 
            mC(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TRUNCATE"

    // $ANTLR start "K_DELETE"
    public final void mK_DELETE() throws RecognitionException {
        try {
            int _type = K_DELETE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:517:9: ( D E L E T E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:517:16: D E L E T E
            {
            mD(); 
            mE(); 
            mL(); 
            mE(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DELETE"

    // $ANTLR start "K_IN"
    public final void mK_IN() throws RecognitionException {
        try {
            int _type = K_IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:518:5: ( I N )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:518:16: I N
            {
            mI(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_IN"

    // $ANTLR start "K_CREATE"
    public final void mK_CREATE() throws RecognitionException {
        try {
            int _type = K_CREATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:519:9: ( C R E A T E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:519:16: C R E A T E
            {
            mC(); 
            mR(); 
            mE(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_CREATE"

    // $ANTLR start "K_KEYSPACE"
    public final void mK_KEYSPACE() throws RecognitionException {
        try {
            int _type = K_KEYSPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:11: ( ( K E Y S P A C E | S C H E M A ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:16: ( K E Y S P A C E | S C H E M A )
            {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:16: ( K E Y S P A C E | S C H E M A )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='K'||LA2_0=='k') ) {
                alt2=1;
            }
            else if ( (LA2_0=='S'||LA2_0=='s') ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:18: K E Y S P A C E
                    {
                    mK(); 
                    mE(); 
                    mY(); 
                    mS(); 
                    mP(); 
                    mA(); 
                    mC(); 
                    mE(); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:521:20: S C H E M A
                    {
                    mS(); 
                    mC(); 
                    mH(); 
                    mE(); 
                    mM(); 
                    mA(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_KEYSPACE"

    // $ANTLR start "K_COLUMNFAMILY"
    public final void mK_COLUMNFAMILY() throws RecognitionException {
        try {
            int _type = K_COLUMNFAMILY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:522:15: ( ( C O L U M N F A M I L Y | T A B L E ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:522:16: ( C O L U M N F A M I L Y | T A B L E )
            {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:522:16: ( C O L U M N F A M I L Y | T A B L E )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='C'||LA3_0=='c') ) {
                alt3=1;
            }
            else if ( (LA3_0=='T'||LA3_0=='t') ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:522:18: C O L U M N F A M I L Y
                    {
                    mC(); 
                    mO(); 
                    mL(); 
                    mU(); 
                    mM(); 
                    mN(); 
                    mF(); 
                    mA(); 
                    mM(); 
                    mI(); 
                    mL(); 
                    mY(); 

                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:523:20: T A B L E
                    {
                    mT(); 
                    mA(); 
                    mB(); 
                    mL(); 
                    mE(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_COLUMNFAMILY"

    // $ANTLR start "K_INDEX"
    public final void mK_INDEX() throws RecognitionException {
        try {
            int _type = K_INDEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:524:8: ( I N D E X )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:524:16: I N D E X
            {
            mI(); 
            mN(); 
            mD(); 
            mE(); 
            mX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INDEX"

    // $ANTLR start "K_ON"
    public final void mK_ON() throws RecognitionException {
        try {
            int _type = K_ON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:525:5: ( O N )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:525:16: O N
            {
            mO(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ON"

    // $ANTLR start "K_DROP"
    public final void mK_DROP() throws RecognitionException {
        try {
            int _type = K_DROP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:526:7: ( D R O P )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:526:16: D R O P
            {
            mD(); 
            mR(); 
            mO(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DROP"

    // $ANTLR start "K_PRIMARY"
    public final void mK_PRIMARY() throws RecognitionException {
        try {
            int _type = K_PRIMARY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:527:10: ( P R I M A R Y )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:527:16: P R I M A R Y
            {
            mP(); 
            mR(); 
            mI(); 
            mM(); 
            mA(); 
            mR(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_PRIMARY"

    // $ANTLR start "K_INTO"
    public final void mK_INTO() throws RecognitionException {
        try {
            int _type = K_INTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:528:7: ( I N T O )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:528:16: I N T O
            {
            mI(); 
            mN(); 
            mT(); 
            mO(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_INTO"

    // $ANTLR start "K_VALUES"
    public final void mK_VALUES() throws RecognitionException {
        try {
            int _type = K_VALUES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:529:9: ( V A L U E S )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:529:16: V A L U E S
            {
            mV(); 
            mA(); 
            mL(); 
            mU(); 
            mE(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_VALUES"

    // $ANTLR start "K_TIMESTAMP"
    public final void mK_TIMESTAMP() throws RecognitionException {
        try {
            int _type = K_TIMESTAMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:530:12: ( T I M E S T A M P )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:530:16: T I M E S T A M P
            {
            mT(); 
            mI(); 
            mM(); 
            mE(); 
            mS(); 
            mT(); 
            mA(); 
            mM(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TIMESTAMP"

    // $ANTLR start "K_TTL"
    public final void mK_TTL() throws RecognitionException {
        try {
            int _type = K_TTL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:531:6: ( T T L )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:531:16: T T L
            {
            mT(); 
            mT(); 
            mL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TTL"

    // $ANTLR start "K_ALTER"
    public final void mK_ALTER() throws RecognitionException {
        try {
            int _type = K_ALTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:532:8: ( A L T E R )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:532:16: A L T E R
            {
            mA(); 
            mL(); 
            mT(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ALTER"

    // $ANTLR start "K_ADD"
    public final void mK_ADD() throws RecognitionException {
        try {
            int _type = K_ADD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:533:6: ( A D D )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:533:16: A D D
            {
            mA(); 
            mD(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ADD"

    // $ANTLR start "K_TYPE"
    public final void mK_TYPE() throws RecognitionException {
        try {
            int _type = K_TYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:534:7: ( T Y P E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:534:16: T Y P E
            {
            mT(); 
            mY(); 
            mP(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_TYPE"

    // $ANTLR start "K_COMPACT"
    public final void mK_COMPACT() throws RecognitionException {
        try {
            int _type = K_COMPACT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:535:10: ( C O M P A C T )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:535:16: C O M P A C T
            {
            mC(); 
            mO(); 
            mM(); 
            mP(); 
            mA(); 
            mC(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_COMPACT"

    // $ANTLR start "K_STORAGE"
    public final void mK_STORAGE() throws RecognitionException {
        try {
            int _type = K_STORAGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:536:10: ( S T O R A G E )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:536:16: S T O R A G E
            {
            mS(); 
            mT(); 
            mO(); 
            mR(); 
            mA(); 
            mG(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_STORAGE"

    // $ANTLR start "K_ORDER"
    public final void mK_ORDER() throws RecognitionException {
        try {
            int _type = K_ORDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:537:8: ( O R D E R )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:537:16: O R D E R
            {
            mO(); 
            mR(); 
            mD(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ORDER"

    // $ANTLR start "K_BY"
    public final void mK_BY() throws RecognitionException {
        try {
            int _type = K_BY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:538:5: ( B Y )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:538:16: B Y
            {
            mB(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_BY"

    // $ANTLR start "K_ASC"
    public final void mK_ASC() throws RecognitionException {
        try {
            int _type = K_ASC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:539:6: ( A S C )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:539:16: A S C
            {
            mA(); 
            mS(); 
            mC(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_ASC"

    // $ANTLR start "K_DESC"
    public final void mK_DESC() throws RecognitionException {
        try {
            int _type = K_DESC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:540:7: ( D E S C )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:540:16: D E S C
            {
            mD(); 
            mE(); 
            mS(); 
            mC(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "K_DESC"

    // $ANTLR start "A"
    public final void mA() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:543:11: ( ( 'a' | 'A' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:543:13: ( 'a' | 'A' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "A"

    // $ANTLR start "B"
    public final void mB() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:544:11: ( ( 'b' | 'B' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:544:13: ( 'b' | 'B' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "B"

    // $ANTLR start "C"
    public final void mC() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:545:11: ( ( 'c' | 'C' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:545:13: ( 'c' | 'C' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "C"

    // $ANTLR start "D"
    public final void mD() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:546:11: ( ( 'd' | 'D' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:546:13: ( 'd' | 'D' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "D"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:547:11: ( ( 'e' | 'E' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:547:13: ( 'e' | 'E' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "E"

    // $ANTLR start "F"
    public final void mF() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:548:11: ( ( 'f' | 'F' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:548:13: ( 'f' | 'F' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "F"

    // $ANTLR start "G"
    public final void mG() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:549:11: ( ( 'g' | 'G' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:549:13: ( 'g' | 'G' )
            {
            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "G"

    // $ANTLR start "H"
    public final void mH() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:550:11: ( ( 'h' | 'H' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:550:13: ( 'h' | 'H' )
            {
            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "H"

    // $ANTLR start "I"
    public final void mI() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:551:11: ( ( 'i' | 'I' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:551:13: ( 'i' | 'I' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "I"

    // $ANTLR start "J"
    public final void mJ() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:552:11: ( ( 'j' | 'J' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:552:13: ( 'j' | 'J' )
            {
            if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "J"

    // $ANTLR start "K"
    public final void mK() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:553:11: ( ( 'k' | 'K' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:553:13: ( 'k' | 'K' )
            {
            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "K"

    // $ANTLR start "L"
    public final void mL() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:554:11: ( ( 'l' | 'L' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:554:13: ( 'l' | 'L' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "L"

    // $ANTLR start "M"
    public final void mM() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:555:11: ( ( 'm' | 'M' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:555:13: ( 'm' | 'M' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "M"

    // $ANTLR start "N"
    public final void mN() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:556:11: ( ( 'n' | 'N' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:556:13: ( 'n' | 'N' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "N"

    // $ANTLR start "O"
    public final void mO() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:557:11: ( ( 'o' | 'O' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:557:13: ( 'o' | 'O' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "O"

    // $ANTLR start "P"
    public final void mP() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:558:11: ( ( 'p' | 'P' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:558:13: ( 'p' | 'P' )
            {
            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "P"

    // $ANTLR start "Q"
    public final void mQ() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:559:11: ( ( 'q' | 'Q' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:559:13: ( 'q' | 'Q' )
            {
            if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Q"

    // $ANTLR start "R"
    public final void mR() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:11: ( ( 'r' | 'R' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:13: ( 'r' | 'R' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "R"

    // $ANTLR start "S"
    public final void mS() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:561:11: ( ( 's' | 'S' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:561:13: ( 's' | 'S' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "S"

    // $ANTLR start "T"
    public final void mT() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:562:11: ( ( 't' | 'T' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:562:13: ( 't' | 'T' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "T"

    // $ANTLR start "U"
    public final void mU() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:563:11: ( ( 'u' | 'U' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:563:13: ( 'u' | 'U' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "U"

    // $ANTLR start "V"
    public final void mV() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:564:11: ( ( 'v' | 'V' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:564:13: ( 'v' | 'V' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "V"

    // $ANTLR start "W"
    public final void mW() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:565:11: ( ( 'w' | 'W' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:565:13: ( 'w' | 'W' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "W"

    // $ANTLR start "X"
    public final void mX() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:566:11: ( ( 'x' | 'X' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:566:13: ( 'x' | 'X' )
            {
            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "X"

    // $ANTLR start "Y"
    public final void mY() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:567:11: ( ( 'y' | 'Y' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:567:13: ( 'y' | 'Y' )
            {
            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Y"

    // $ANTLR start "Z"
    public final void mZ() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:568:11: ( ( 'z' | 'Z' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:568:13: ( 'z' | 'Z' )
            {
            if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Z"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            int c;

             StringBuilder b = new StringBuilder(); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:5: ( '\\'' (c=~ ( '\\'' ) | '\\'' '\\'' )* '\\'' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:7: '\\'' (c=~ ( '\\'' ) | '\\'' '\\'' )* '\\''
            {
            match('\''); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:12: (c=~ ( '\\'' ) | '\\'' '\\'' )*
            loop4:
            do {
                int alt4=3;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='\'') ) {
                    int LA4_1 = input.LA(2);

                    if ( (LA4_1=='\'') ) {
                        alt4=2;
                    }


                }
                else if ( ((LA4_0>='\u0000' && LA4_0<='&')||(LA4_0>='(' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:13: c=~ ( '\\'' )
            	    {
            	    c= input.LA(1);
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}

            	     b.appendCodePoint(c);

            	    }
            	    break;
            	case 2 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:50: '\\'' '\\''
            	    {
            	    match('\''); 
            	    match('\''); 
            	     b.appendCodePoint('\''); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
             setText(b.toString());     }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "QUOTED_NAME"
    public final void mQUOTED_NAME() throws RecognitionException {
        try {
            int _type = QUOTED_NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            int c;

             StringBuilder b = new StringBuilder(); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:579:5: ( '\\\"' (c=~ ( '\\\"' ) | '\\\"' '\\\"' )* '\\\"' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:579:7: '\\\"' (c=~ ( '\\\"' ) | '\\\"' '\\\"' )* '\\\"'
            {
            match('\"'); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:579:12: (c=~ ( '\\\"' ) | '\\\"' '\\\"' )*
            loop5:
            do {
                int alt5=3;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\"') ) {
                    int LA5_1 = input.LA(2);

                    if ( (LA5_1=='\"') ) {
                        alt5=2;
                    }


                }
                else if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:579:13: c=~ ( '\\\"' )
            	    {
            	    c= input.LA(1);
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}

            	     b.appendCodePoint(c); 

            	    }
            	    break;
            	case 2 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:579:51: '\\\"' '\\\"'
            	    {
            	    match('\"'); 
            	    match('\"'); 
            	     b.appendCodePoint('\"'); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
             setText(b.toString());     }
        finally {
        }
    }
    // $ANTLR end "QUOTED_NAME"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:583:5: ( '0' .. '9' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:583:7: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:587:5: ( ( 'A' .. 'Z' | 'a' .. 'z' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:587:7: ( 'A' .. 'Z' | 'a' .. 'z' )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "HEX"
    public final void mHEX() throws RecognitionException {
        try {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:591:5: ( ( 'A' .. 'F' | 'a' .. 'f' | '0' .. '9' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:591:7: ( 'A' .. 'F' | 'a' .. 'f' | '0' .. '9' )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEX"

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:595:5: ( ( '-' )? ( DIGIT )+ )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:595:7: ( '-' )? ( DIGIT )+
            {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:595:7: ( '-' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='-') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:595:7: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:595:12: ( DIGIT )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='0' && LA7_0<='9')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:595:12: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "QMARK"
    public final void mQMARK() throws RecognitionException {
        try {
            int _type = QMARK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:599:5: ( '?' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:599:7: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QMARK"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:607:5: ( INTEGER '.' INTEGER )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:607:7: INTEGER '.' INTEGER
            {
            mINTEGER(); 
            match('.'); 
            mINTEGER(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "IDENT"
    public final void mIDENT() throws RecognitionException {
        try {
            int _type = IDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:611:5: ( LETTER ( LETTER | DIGIT | '_' )* )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:611:7: LETTER ( LETTER | DIGIT | '_' )*
            {
            mLETTER(); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:611:14: ( LETTER | DIGIT | '_' )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='0' && LA8_0<='9')||(LA8_0>='A' && LA8_0<='Z')||LA8_0=='_'||(LA8_0>='a' && LA8_0<='z')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENT"

    // $ANTLR start "COMPIDENT"
    public final void mCOMPIDENT() throws RecognitionException {
        try {
            int _type = COMPIDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:615:5: ( IDENT ( ':' ( IDENT | INTEGER ) )+ )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:615:7: IDENT ( ':' ( IDENT | INTEGER ) )+
            {
            mIDENT(); 
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:615:13: ( ':' ( IDENT | INTEGER ) )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==':') ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:615:15: ':' ( IDENT | INTEGER )
            	    {
            	    match(':'); 
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:615:19: ( IDENT | INTEGER )
            	    int alt9=2;
            	    int LA9_0 = input.LA(1);

            	    if ( ((LA9_0>='A' && LA9_0<='Z')||(LA9_0>='a' && LA9_0<='z')) ) {
            	        alt9=1;
            	    }
            	    else if ( (LA9_0=='-'||(LA9_0>='0' && LA9_0<='9')) ) {
            	        alt9=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 9, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt9) {
            	        case 1 :
            	            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:615:20: IDENT
            	            {
            	            mIDENT(); 

            	            }
            	            break;
            	        case 2 :
            	            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:615:28: INTEGER
            	            {
            	            mINTEGER(); 

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPIDENT"

    // $ANTLR start "UUID"
    public final void mUUID() throws RecognitionException {
        try {
            int _type = UUID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:619:5: ( HEX HEX HEX HEX HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:619:7: HEX HEX HEX HEX HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX
            {
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            match('-'); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 
            mHEX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UUID"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:627:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:627:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:627:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='\t' && LA11_0<='\n')||LA11_0=='\r'||LA11_0==' ') ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


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

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:631:5: ( ( '--' | '//' ) ( . )* ( '\\n' | '\\r' ) )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:631:7: ( '--' | '//' ) ( . )* ( '\\n' | '\\r' )
            {
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:631:7: ( '--' | '//' )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='-') ) {
                alt12=1;
            }
            else if ( (LA12_0=='/') ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:631:8: '--'
                    {
                    match("--"); 


                    }
                    break;
                case 2 :
                    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:631:15: '//'
                    {
                    match("//"); 


                    }
                    break;

            }

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:631:21: ( . )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0=='\n'||LA13_0=='\r') ) {
                    alt13=2;
                }
                else if ( ((LA13_0>='\u0000' && LA13_0<='\t')||(LA13_0>='\u000B' && LA13_0<='\f')||(LA13_0>='\u000E' && LA13_0<='\uFFFF')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:631:21: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "MULTILINE_COMMENT"
    public final void mMULTILINE_COMMENT() throws RecognitionException {
        try {
            int _type = MULTILINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:5: ( '/*' ( . )* '*/' )
            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:7: '/*' ( . )* '*/'
            {
            match("/*"); 

            // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:12: ( . )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='*') ) {
                    int LA14_1 = input.LA(2);

                    if ( (LA14_1=='/') ) {
                        alt14=2;
                    }
                    else if ( ((LA14_1>='\u0000' && LA14_1<='.')||(LA14_1>='0' && LA14_1<='\uFFFF')) ) {
                        alt14=1;
                    }


                }
                else if ( ((LA14_0>='\u0000' && LA14_0<=')')||(LA14_0>='+' && LA14_0<='\uFFFF')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:12: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            match("*/"); 

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULTILINE_COMMENT"

    public void mTokens() throws RecognitionException {
        // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:8: ( T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | K_SELECT | K_FROM | K_WHERE | K_AND | K_KEY | K_INSERT | K_UPDATE | K_WITH | K_LIMIT | K_USING | K_CONSISTENCY | K_LEVEL | K_USE | K_COUNT | K_SET | K_BEGIN | K_APPLY | K_BATCH | K_TRUNCATE | K_DELETE | K_IN | K_CREATE | K_KEYSPACE | K_COLUMNFAMILY | K_INDEX | K_ON | K_DROP | K_PRIMARY | K_INTO | K_VALUES | K_TIMESTAMP | K_TTL | K_ALTER | K_ADD | K_TYPE | K_COMPACT | K_STORAGE | K_ORDER | K_BY | K_ASC | K_DESC | STRING_LITERAL | QUOTED_NAME | INTEGER | QMARK | FLOAT | IDENT | COMPIDENT | UUID | WS | COMMENT | MULTILINE_COMMENT )
        int alt15=65;
        alt15 = dfa15.predict(input);
        switch (alt15) {
            case 1 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:10: T__85
                {
                mT__85(); 

                }
                break;
            case 2 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:16: T__86
                {
                mT__86(); 

                }
                break;
            case 3 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:22: T__87
                {
                mT__87(); 

                }
                break;
            case 4 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:28: T__88
                {
                mT__88(); 

                }
                break;
            case 5 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:34: T__89
                {
                mT__89(); 

                }
                break;
            case 6 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:40: T__90
                {
                mT__90(); 

                }
                break;
            case 7 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:46: T__91
                {
                mT__91(); 

                }
                break;
            case 8 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:52: T__92
                {
                mT__92(); 

                }
                break;
            case 9 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:58: T__93
                {
                mT__93(); 

                }
                break;
            case 10 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:64: T__94
                {
                mT__94(); 

                }
                break;
            case 11 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:70: T__95
                {
                mT__95(); 

                }
                break;
            case 12 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:76: T__96
                {
                mT__96(); 

                }
                break;
            case 13 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:82: T__97
                {
                mT__97(); 

                }
                break;
            case 14 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:88: K_SELECT
                {
                mK_SELECT(); 

                }
                break;
            case 15 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:97: K_FROM
                {
                mK_FROM(); 

                }
                break;
            case 16 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:104: K_WHERE
                {
                mK_WHERE(); 

                }
                break;
            case 17 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:112: K_AND
                {
                mK_AND(); 

                }
                break;
            case 18 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:118: K_KEY
                {
                mK_KEY(); 

                }
                break;
            case 19 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:124: K_INSERT
                {
                mK_INSERT(); 

                }
                break;
            case 20 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:133: K_UPDATE
                {
                mK_UPDATE(); 

                }
                break;
            case 21 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:142: K_WITH
                {
                mK_WITH(); 

                }
                break;
            case 22 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:149: K_LIMIT
                {
                mK_LIMIT(); 

                }
                break;
            case 23 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:157: K_USING
                {
                mK_USING(); 

                }
                break;
            case 24 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:165: K_CONSISTENCY
                {
                mK_CONSISTENCY(); 

                }
                break;
            case 25 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:179: K_LEVEL
                {
                mK_LEVEL(); 

                }
                break;
            case 26 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:187: K_USE
                {
                mK_USE(); 

                }
                break;
            case 27 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:193: K_COUNT
                {
                mK_COUNT(); 

                }
                break;
            case 28 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:201: K_SET
                {
                mK_SET(); 

                }
                break;
            case 29 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:207: K_BEGIN
                {
                mK_BEGIN(); 

                }
                break;
            case 30 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:215: K_APPLY
                {
                mK_APPLY(); 

                }
                break;
            case 31 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:223: K_BATCH
                {
                mK_BATCH(); 

                }
                break;
            case 32 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:231: K_TRUNCATE
                {
                mK_TRUNCATE(); 

                }
                break;
            case 33 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:242: K_DELETE
                {
                mK_DELETE(); 

                }
                break;
            case 34 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:251: K_IN
                {
                mK_IN(); 

                }
                break;
            case 35 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:256: K_CREATE
                {
                mK_CREATE(); 

                }
                break;
            case 36 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:265: K_KEYSPACE
                {
                mK_KEYSPACE(); 

                }
                break;
            case 37 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:276: K_COLUMNFAMILY
                {
                mK_COLUMNFAMILY(); 

                }
                break;
            case 38 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:291: K_INDEX
                {
                mK_INDEX(); 

                }
                break;
            case 39 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:299: K_ON
                {
                mK_ON(); 

                }
                break;
            case 40 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:304: K_DROP
                {
                mK_DROP(); 

                }
                break;
            case 41 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:311: K_PRIMARY
                {
                mK_PRIMARY(); 

                }
                break;
            case 42 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:321: K_INTO
                {
                mK_INTO(); 

                }
                break;
            case 43 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:328: K_VALUES
                {
                mK_VALUES(); 

                }
                break;
            case 44 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:337: K_TIMESTAMP
                {
                mK_TIMESTAMP(); 

                }
                break;
            case 45 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:349: K_TTL
                {
                mK_TTL(); 

                }
                break;
            case 46 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:355: K_ALTER
                {
                mK_ALTER(); 

                }
                break;
            case 47 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:363: K_ADD
                {
                mK_ADD(); 

                }
                break;
            case 48 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:369: K_TYPE
                {
                mK_TYPE(); 

                }
                break;
            case 49 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:376: K_COMPACT
                {
                mK_COMPACT(); 

                }
                break;
            case 50 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:386: K_STORAGE
                {
                mK_STORAGE(); 

                }
                break;
            case 51 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:396: K_ORDER
                {
                mK_ORDER(); 

                }
                break;
            case 52 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:404: K_BY
                {
                mK_BY(); 

                }
                break;
            case 53 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:409: K_ASC
                {
                mK_ASC(); 

                }
                break;
            case 54 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:415: K_DESC
                {
                mK_DESC(); 

                }
                break;
            case 55 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:422: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 56 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:437: QUOTED_NAME
                {
                mQUOTED_NAME(); 

                }
                break;
            case 57 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:449: INTEGER
                {
                mINTEGER(); 

                }
                break;
            case 58 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:457: QMARK
                {
                mQMARK(); 

                }
                break;
            case 59 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:463: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 60 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:469: IDENT
                {
                mIDENT(); 

                }
                break;
            case 61 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:475: COMPIDENT
                {
                mCOMPIDENT(); 

                }
                break;
            case 62 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:485: UUID
                {
                mUUID(); 

                }
                break;
            case 63 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:490: WS
                {
                mWS(); 

                }
                break;
            case 64 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:493: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 65 :
                // /media/dev/carbon/trunk-new/graphite/dependencies/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1:501: MULTILINE_COMMENT
                {
                mMULTILINE_COMMENT(); 

                }
                break;

        }

    }


    protected DFA1 dfa1 = new DFA1(this);
    protected DFA15 dfa15 = new DFA15(this);
    static final String DFA1_eotS =
        "\13\uffff";
    static final String DFA1_eofS =
        "\13\uffff";
    static final String DFA1_minS =
        "\1\101\2\uffff\1\114\2\uffff\1\110\4\uffff";
    static final String DFA1_maxS =
        "\1\164\2\uffff\1\156\2\uffff\1\167\4\uffff";
    static final String DFA1_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\5\1\6\1\uffff\1\4\1\3\1\7\1\10";
    static final String DFA1_specialS =
        "\13\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\3\3\uffff\1\5\6\uffff\1\4\2\uffff\1\1\1\uffff\1\2\2\uffff"+
            "\1\6\14\uffff\1\3\3\uffff\1\5\6\uffff\1\4\2\uffff\1\1\1\uffff"+
            "\1\2\2\uffff\1\6",
            "",
            "",
            "\1\10\1\uffff\1\7\35\uffff\1\10\1\uffff\1\7",
            "",
            "",
            "\1\12\16\uffff\1\11\20\uffff\1\12\16\uffff\1\11",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
    static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
    static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
    static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
    static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
    static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
    static final short[][] DFA1_transition;

    static {
        int numStates = DFA1_transitionS.length;
        DFA1_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
        }
    }

    class DFA1 extends DFA {

        public DFA1(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 1;
            this.eot = DFA1_eot;
            this.eof = DFA1_eof;
            this.min = DFA1_min;
            this.max = DFA1_max;
            this.accept = DFA1_accept;
            this.special = DFA1_special;
            this.transition = DFA1_transition;
        }
        public String getDescription() {
            return "500:16: ( O N E | Q U O R U M | A L L | A N Y | L O C A L '_' Q U O R U M | E A C H '_' Q U O R U M | T W O | T H R E E )";
        }
    }
    static final String DFA15_eotS =
        "\11\uffff\1\46\1\50\1\52\21\53\2\uffff\1\124\1\uffff\1\53\3\uffff"+
        "\1\124\6\uffff\1\53\1\uffff\15\53\1\151\7\53\1\170\12\53\1\u0084"+
        "\5\53\1\uffff\1\124\3\uffff\1\53\1\u008d\6\53\1\u0094\2\53\1\u0097"+
        "\1\u0094\1\u0098\1\u0099\1\u009a\1\uffff\4\53\1\u00a0\11\53\1\uffff"+
        "\1\u0094\6\53\1\u00b0\1\53\1\u0094\1\53\1\uffff\6\53\1\124\1\53"+
        "\1\uffff\2\53\1\u00bd\1\53\1\u00bf\1\53\1\uffff\2\53\4\uffff\1\53"+
        "\1\u00c4\3\53\1\uffff\14\53\1\u00d4\2\53\1\uffff\3\53\1\u00da\1"+
        "\53\1\u00dc\2\53\1\124\3\53\1\uffff\1\53\1\uffff\1\u00e4\1\u00e5"+
        "\1\u00e6\1\53\1\uffff\1\53\1\u00e9\1\u00ea\2\53\1\u00ed\3\53\1\u00f1"+
        "\1\53\1\u00f3\3\53\1\uffff\1\u0094\1\53\1\u00f8\1\u00f9\1\u00fa"+
        "\1\uffff\1\53\1\uffff\2\53\1\124\1\u00ff\1\53\1\u0101\1\53\3\uffff"+
        "\1\53\1\u0104\2\uffff\1\u0105\1\53\1\uffff\3\53\1\uffff\1\u010a"+
        "\1\uffff\1\u0094\3\53\3\uffff\1\u010e\1\53\1\u0110\1\124\1\uffff"+
        "\1\u0112\1\uffff\2\53\2\uffff\3\53\1\u0118\1\uffff\3\53\1\uffff"+
        "\1\u011c\1\uffff\1\124\1\uffff\1\53\1\u0101\3\53\1\uffff\2\53\1"+
        "\u0123\1\uffff\1\124\4\53\1\u0128\1\uffff\4\53\1\uffff\2\53\1\u012f"+
        "\2\u0094\1\u00f8\1\uffff";
    static final String DFA15_eofS =
        "\u0130\uffff";
    static final String DFA15_minS =
        "\1\11\10\uffff\1\55\2\75\21\60\2\uffff\1\56\1\uffff\1\60\1\uffff"+
        "\1\52\1\uffff\1\56\6\uffff\1\60\1\uffff\46\60\1\uffff\1\56\3\uffff"+
        "\20\60\1\uffff\16\60\1\uffff\13\60\1\uffff\6\60\1\56\1\60\1\uffff"+
        "\6\60\1\uffff\2\60\4\uffff\5\60\1\uffff\17\60\1\uffff\10\60\1\56"+
        "\3\60\1\uffff\1\60\1\uffff\4\60\1\uffff\17\60\1\uffff\5\60\1\uffff"+
        "\1\60\1\uffff\2\60\1\56\4\60\3\uffff\2\60\2\uffff\2\60\1\uffff\3"+
        "\60\1\uffff\1\60\1\uffff\4\60\3\uffff\3\60\1\56\1\uffff\1\60\1\uffff"+
        "\2\60\2\uffff\4\60\1\uffff\3\60\1\uffff\1\60\1\uffff\1\56\1\uffff"+
        "\1\55\4\60\1\uffff\3\60\1\uffff\1\55\5\60\1\uffff\4\60\1\uffff\6"+
        "\60\1\uffff";
    static final String DFA15_maxS =
        "\1\172\10\uffff\1\71\2\75\21\172\2\uffff\1\146\1\uffff\1\172\1\uffff"+
        "\1\57\1\uffff\1\71\6\uffff\1\172\1\uffff\46\172\1\uffff\1\146\3"+
        "\uffff\20\172\1\uffff\16\172\1\uffff\13\172\1\uffff\6\172\1\146"+
        "\1\172\1\uffff\6\172\1\uffff\2\172\4\uffff\5\172\1\uffff\17\172"+
        "\1\uffff\10\172\1\146\3\172\1\uffff\1\172\1\uffff\4\172\1\uffff"+
        "\17\172\1\uffff\5\172\1\uffff\1\172\1\uffff\2\172\1\146\4\172\3"+
        "\uffff\2\172\2\uffff\2\172\1\uffff\3\172\1\uffff\1\172\1\uffff\4"+
        "\172\3\uffff\3\172\1\146\1\uffff\1\172\1\uffff\2\172\2\uffff\4\172"+
        "\1\uffff\3\172\1\uffff\1\172\1\uffff\1\146\1\uffff\5\172\1\uffff"+
        "\3\172\1\uffff\1\71\5\172\1\uffff\4\172\1\uffff\6\172\1\uffff";
    static final String DFA15_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\24\uffff\1\67\1\70\1\uffff"+
        "\1\72\1\uffff\1\77\1\uffff\1\100\1\uffff\1\11\1\13\1\12\1\14\1\15"+
        "\1\74\1\uffff\1\75\46\uffff\1\71\1\uffff\1\73\1\76\1\101\20\uffff"+
        "\1\42\16\uffff\1\47\13\uffff\1\64\10\uffff\1\34\6\uffff\1\31\2\uffff"+
        "\1\57\1\21\1\65\1\22\5\uffff\1\32\17\uffff\1\55\14\uffff\1\17\1"+
        "\uffff\1\25\4\uffff\1\52\17\uffff\1\60\5\uffff\1\66\1\uffff\1\50"+
        "\7\uffff\1\20\1\56\1\36\2\uffff\1\46\1\27\2\uffff\1\26\3\uffff\1"+
        "\33\1\uffff\1\63\4\uffff\1\45\1\37\1\35\4\uffff\1\16\1\uffff\1\44"+
        "\2\uffff\1\23\1\24\4\uffff\1\43\3\uffff\1\41\1\uffff\1\53\1\uffff"+
        "\1\62\5\uffff\1\61\3\uffff\1\51\6\uffff\1\40\4\uffff\1\54\6\uffff"+
        "\1\30";
    static final String DFA15_specialS =
        "\u0130\uffff}>";
    static final String[] DFA15_transitionS = {
            "\2\42\2\uffff\1\42\22\uffff\1\42\1\uffff\1\36\4\uffff\1\35\1"+
            "\2\1\3\1\4\1\10\1\5\1\11\1\7\1\43\12\37\1\uffff\1\1\1\12\1\6"+
            "\1\13\1\40\1\uffff\1\17\1\31\1\24\1\32\1\27\1\15\2\41\1\21\1"+
            "\41\1\20\1\23\2\41\1\25\1\33\1\26\1\41\1\14\1\30\1\22\1\34\1"+
            "\16\3\41\6\uffff\1\17\1\31\1\24\1\32\1\27\1\15\2\41\1\21\1\41"+
            "\1\20\1\23\2\41\1\25\1\33\1\26\1\41\1\14\1\30\1\22\1\34\1\16"+
            "\3\41",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\44\2\uffff\12\45",
            "\1\47",
            "\1\51",
            "\12\60\1\55\6\uffff\2\60\1\57\1\60\1\54\16\60\1\56\6\60\4\uffff"+
            "\1\60\1\uffff\2\60\1\57\1\60\1\54\16\60\1\56\6\60",
            "\12\62\1\55\6\uffff\6\62\13\60\1\61\10\60\4\uffff\1\60\1\uffff"+
            "\6\62\13\60\1\61\10\60",
            "\12\60\1\55\6\uffff\7\60\1\64\1\63\21\60\4\uffff\1\60\1\uffff"+
            "\7\60\1\64\1\63\21\60",
            "\12\62\1\55\6\uffff\3\62\1\67\2\62\5\60\1\65\1\60\1\70\1\60"+
            "\1\66\2\60\1\71\7\60\4\uffff\1\60\1\uffff\3\62\1\67\2\62\5\60"+
            "\1\65\1\60\1\70\1\60\1\66\2\60\1\71\7\60",
            "\12\60\1\55\6\uffff\4\60\1\72\25\60\4\uffff\1\60\1\uffff\4"+
            "\60\1\72\25\60",
            "\12\60\1\55\6\uffff\15\60\1\73\14\60\4\uffff\1\60\1\uffff\15"+
            "\60\1\73\14\60",
            "\12\60\1\55\6\uffff\17\60\1\75\2\60\1\74\7\60\4\uffff\1\60"+
            "\1\uffff\17\60\1\75\2\60\1\74\7\60",
            "\12\60\1\55\6\uffff\10\60\1\77\5\60\1\76\13\60\4\uffff\1\60"+
            "\1\uffff\10\60\1\77\5\60\1\76\13\60",
            "\12\62\1\55\6\uffff\6\62\10\60\1\100\2\60\1\101\10\60\4\uffff"+
            "\1\60\1\uffff\6\62\10\60\1\100\2\60\1\101\10\60",
            "\12\60\1\55\6\uffff\15\60\1\103\3\60\1\102\10\60\4\uffff\1"+
            "\60\1\uffff\15\60\1\103\3\60\1\102\10\60",
            "\12\60\1\55\6\uffff\24\60\1\104\5\60\4\uffff\1\60\1\uffff\24"+
            "\60\1\104\5\60",
            "\12\62\1\55\6\uffff\1\105\5\62\24\60\4\uffff\1\60\1\uffff\1"+
            "\105\5\62\24\60",
            "\12\60\1\55\6\uffff\1\113\6\60\1\110\1\106\10\60\1\111\1\60"+
            "\1\112\2\60\1\114\1\60\1\107\1\60\4\uffff\1\60\1\uffff\1\113"+
            "\6\60\1\110\1\106\10\60\1\111\1\60\1\112\2\60\1\114\1\60\1\107"+
            "\1\60",
            "\12\62\1\55\6\uffff\1\115\3\62\1\117\1\62\22\60\1\116\1\60"+
            "\4\uffff\1\60\1\uffff\1\115\3\62\1\117\1\62\22\60\1\116\1\60",
            "\12\62\1\55\6\uffff\4\62\1\120\1\62\13\60\1\121\10\60\4\uffff"+
            "\1\60\1\uffff\4\62\1\120\1\62\13\60\1\121\10\60",
            "\12\60\1\55\6\uffff\21\60\1\122\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\122\10\60",
            "\12\60\1\55\6\uffff\1\123\31\60\4\uffff\1\60\1\uffff\1\123"+
            "\31\60",
            "",
            "",
            "\1\126\1\uffff\12\125\7\uffff\6\127\32\uffff\6\127",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\130\4\uffff\1\44",
            "",
            "\1\126\1\uffff\12\45",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\60\1\55\6\uffff\13\60\1\131\7\60\1\132\6\60\4\uffff\1\60"+
            "\1\uffff\13\60\1\131\7\60\1\132\6\60",
            "",
            "\12\60\1\55\6\uffff\16\60\1\133\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\133\13\60",
            "\12\60\1\55\6\uffff\7\60\1\134\22\60\4\uffff\1\60\1\uffff\7"+
            "\60\1\134\22\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\16\60\1\135\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\135\13\60",
            "\12\136\1\55\6\uffff\6\136\24\60\4\uffff\1\60\1\uffff\6\136"+
            "\24\60",
            "\12\60\1\55\6\uffff\23\60\1\137\6\60\4\uffff\1\60\1\uffff\23"+
            "\60\1\137\6\60",
            "\12\60\1\55\6\uffff\4\60\1\140\25\60\4\uffff\1\60\1\uffff\4"+
            "\60\1\140\25\60",
            "\12\60\1\55\6\uffff\13\60\1\141\7\60\1\142\6\60\4\uffff\1\60"+
            "\1\uffff\13\60\1\141\7\60\1\142\6\60",
            "\12\60\1\55\6\uffff\17\60\1\143\12\60\4\uffff\1\60\1\uffff"+
            "\17\60\1\143\12\60",
            "\12\136\1\55\6\uffff\3\136\1\144\2\136\24\60\4\uffff\1\60\1"+
            "\uffff\3\136\1\144\2\136\24\60",
            "\12\60\1\55\6\uffff\3\60\1\146\24\60\1\145\1\60\4\uffff\1\60"+
            "\1\uffff\3\60\1\146\24\60\1\145\1\60",
            "\12\60\1\55\6\uffff\2\60\1\147\27\60\4\uffff\1\60\1\uffff\2"+
            "\60\1\147\27\60",
            "\12\60\1\55\6\uffff\30\60\1\150\1\60\4\uffff\1\60\1\uffff\30"+
            "\60\1\150\1\60",
            "\12\60\1\55\6\uffff\3\60\1\154\16\60\1\153\1\152\6\60\4\uffff"+
            "\1\60\1\uffff\3\60\1\154\16\60\1\153\1\152\6\60",
            "\12\60\1\55\6\uffff\4\60\1\156\3\60\1\155\21\60\4\uffff\1\60"+
            "\1\uffff\4\60\1\156\3\60\1\155\21\60",
            "\12\60\1\55\6\uffff\3\60\1\157\26\60\4\uffff\1\60\1\uffff\3"+
            "\60\1\157\26\60",
            "\12\60\1\55\6\uffff\2\60\1\160\27\60\4\uffff\1\60\1\uffff\2"+
            "\60\1\160\27\60",
            "\12\60\1\55\6\uffff\14\60\1\161\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\161\15\60",
            "\12\60\1\55\6\uffff\13\60\1\162\1\164\1\163\6\60\1\165\5\60"+
            "\4\uffff\1\60\1\uffff\13\60\1\162\1\164\1\163\6\60\1\165\5\60",
            "\12\60\1\55\6\uffff\4\60\1\166\25\60\4\uffff\1\60\1\uffff\4"+
            "\60\1\166\25\60",
            "\12\60\1\55\6\uffff\3\60\1\167\26\60\4\uffff\1\60\1\uffff\3"+
            "\60\1\167\26\60",
            "\12\60\1\55\6\uffff\4\60\1\171\25\60\4\uffff\1\60\1\uffff\4"+
            "\60\1\171\25\60",
            "\12\60\1\55\6\uffff\16\60\1\172\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\172\13\60",
            "\12\136\1\55\6\uffff\2\136\1\173\3\136\24\60\4\uffff\1\60\1"+
            "\uffff\2\136\1\173\3\136\24\60",
            "\12\60\1\55\6\uffff\14\60\1\174\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\174\15\60",
            "\12\60\1\55\6\uffff\17\60\1\175\12\60\4\uffff\1\60\1\uffff"+
            "\17\60\1\175\12\60",
            "\12\60\1\55\6\uffff\21\60\1\176\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\176\10\60",
            "\12\60\1\55\6\uffff\24\60\1\177\5\60\4\uffff\1\60\1\uffff\24"+
            "\60\1\177\5\60",
            "\12\60\1\55\6\uffff\13\60\1\u0080\16\60\4\uffff\1\60\1\uffff"+
            "\13\60\1\u0080\16\60",
            "\12\60\1\55\6\uffff\1\60\1\u0081\30\60\4\uffff\1\60\1\uffff"+
            "\1\60\1\u0081\30\60",
            "\12\60\1\55\6\uffff\16\60\1\u0082\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\u0082\13\60",
            "\12\136\1\55\6\uffff\6\136\15\60\1\u0083\6\60\4\uffff\1\60"+
            "\1\uffff\6\136\15\60\1\u0083\6\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\136\1\55\6\uffff\6\136\1\u0085\23\60\4\uffff\1\60\1\uffff"+
            "\6\136\1\u0085\23\60",
            "\12\136\1\55\6\uffff\6\136\5\60\1\u0087\6\60\1\u0086\7\60\4"+
            "\uffff\1\60\1\uffff\6\136\5\60\1\u0087\6\60\1\u0086\7\60",
            "\12\60\1\55\6\uffff\16\60\1\u0088\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\u0088\13\60",
            "\12\60\1\55\6\uffff\10\60\1\u0089\21\60\4\uffff\1\60\1\uffff"+
            "\10\60\1\u0089\21\60",
            "\12\60\1\55\6\uffff\13\60\1\u008a\16\60\4\uffff\1\60\1\uffff"+
            "\13\60\1\u008a\16\60",
            "",
            "\1\126\1\uffff\12\u008b\7\uffff\6\127\32\uffff\6\127",
            "",
            "",
            "",
            "\12\60\1\55\6\uffff\4\60\1\u008c\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u008c\25\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\21\60\1\u008e\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u008e\10\60",
            "\12\60\1\55\6\uffff\4\60\1\u008f\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u008f\25\60",
            "\12\60\1\55\6\uffff\14\60\1\u0090\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u0090\15\60",
            "\12\u0091\1\55\6\uffff\6\u0091\24\60\4\uffff\1\60\1\uffff\6"+
            "\u0091\24\60",
            "\12\60\1\55\6\uffff\7\60\1\u0092\22\60\4\uffff\1\60\1\uffff"+
            "\7\60\1\u0092\22\60",
            "\12\60\1\55\6\uffff\21\60\1\u0093\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u0093\10\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\4\60\1\u0095\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u0095\25\60",
            "\12\60\1\55\6\uffff\13\60\1\u0096\16\60\4\uffff\1\60\1\uffff"+
            "\13\60\1\u0096\16\60",
            "\12\u0091\1\55\6\uffff\6\u0091\24\60\4\uffff\1\60\1\uffff\6"+
            "\u0091\24\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\22\60\1\u009b\7\60\4\uffff\1\60\1\uffff"+
            "\22\60\1\u009b\7\60",
            "",
            "\12\60\1\55\6\uffff\16\60\1\u009c\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\u009c\13\60",
            "\12\60\1\55\6\uffff\4\60\1\u009d\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u009d\25\60",
            "\12\60\1\55\6\uffff\4\60\1\u009e\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u009e\25\60",
            "\12\60\1\55\6\uffff\15\60\1\u009f\14\60\4\uffff\1\60\1\uffff"+
            "\15\60\1\u009f\14\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\1\u00a1\31\60\4\uffff\1\60\1\uffff\1\u00a1"+
            "\31\60",
            "\12\60\1\55\6\uffff\1\u00a2\31\60\4\uffff\1\60\1\uffff\1\u00a2"+
            "\31\60",
            "\12\60\1\55\6\uffff\10\60\1\u00a3\21\60\4\uffff\1\60\1\uffff"+
            "\10\60\1\u00a3\21\60",
            "\12\60\1\55\6\uffff\24\60\1\u00a4\5\60\4\uffff\1\60\1\uffff"+
            "\24\60\1\u00a4\5\60",
            "\12\60\1\55\6\uffff\22\60\1\u00a5\7\60\4\uffff\1\60\1\uffff"+
            "\22\60\1\u00a5\7\60",
            "\12\60\1\55\6\uffff\17\60\1\u00a6\12\60\4\uffff\1\60\1\uffff"+
            "\17\60\1\u00a6\12\60",
            "\12\60\1\55\6\uffff\15\60\1\u00a7\14\60\4\uffff\1\60\1\uffff"+
            "\15\60\1\u00a7\14\60",
            "\12\60\1\55\6\uffff\1\u00a8\31\60\4\uffff\1\60\1\uffff\1\u00a8"+
            "\31\60",
            "\12\60\1\55\6\uffff\4\60\1\u00a9\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00a9\25\60",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\21\60\1\u00aa\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u00aa\10\60",
            "\12\u0091\1\55\6\uffff\6\u0091\1\60\1\u00ab\22\60\4\uffff\1"+
            "\60\1\uffff\6\u0091\1\60\1\u00ab\22\60",
            "\12\60\1\55\6\uffff\4\60\1\u00ac\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00ac\25\60",
            "\12\60\1\55\6\uffff\4\60\1\u00ad\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00ad\25\60",
            "\12\60\1\55\6\uffff\4\60\1\u00ae\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00ae\25\60",
            "\12\60\1\55\6\uffff\15\60\1\u00af\14\60\4\uffff\1\60\1\uffff"+
            "\15\60\1\u00af\14\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\13\60\1\u00b1\16\60\4\uffff\1\60\1\uffff"+
            "\13\60\1\u00b1\16\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\2\60\1\u00b2\27\60\4\uffff\1\60\1\uffff"+
            "\2\60\1\u00b2\27\60",
            "",
            "\12\60\1\55\6\uffff\10\60\1\u00b3\21\60\4\uffff\1\60\1\uffff"+
            "\10\60\1\u00b3\21\60",
            "\12\60\1\55\6\uffff\2\60\1\u00b4\27\60\4\uffff\1\60\1\uffff"+
            "\2\60\1\u00b4\27\60",
            "\12\60\1\55\6\uffff\4\60\1\u00b5\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00b5\25\60",
            "\12\60\1\55\6\uffff\17\60\1\u00b6\12\60\4\uffff\1\60\1\uffff"+
            "\17\60\1\u00b6\12\60",
            "\12\60\1\55\6\uffff\14\60\1\u00b7\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u00b7\15\60",
            "\12\60\1\55\6\uffff\24\60\1\u00b8\5\60\4\uffff\1\60\1\uffff"+
            "\24\60\1\u00b8\5\60",
            "\1\126\1\uffff\12\u00b9\7\uffff\6\127\32\uffff\6\127",
            "\12\60\1\55\6\uffff\2\60\1\u00ba\27\60\4\uffff\1\60\1\uffff"+
            "\2\60\1\u00ba\27\60",
            "",
            "\12\60\1\55\6\uffff\1\u00bb\31\60\4\uffff\1\60\1\uffff\1\u00bb"+
            "\31\60",
            "\12\60\1\55\6\uffff\14\60\1\u00bc\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u00bc\15\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\u00be\1\55\6\uffff\6\u00be\24\60\4\uffff\1\60\1\uffff\6"+
            "\u00be\24\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\4\60\1\u00c0\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00c0\25\60",
            "",
            "\12\60\1\55\6\uffff\21\60\1\u00c1\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u00c1\10\60",
            "\12\60\1\55\6\uffff\30\60\1\u00c2\1\60\4\uffff\1\60\1\uffff"+
            "\30\60\1\u00c2\1\60",
            "",
            "",
            "",
            "",
            "\12\60\1\55\6\uffff\17\60\1\u00c3\12\60\4\uffff\1\60\1\uffff"+
            "\17\60\1\u00c3\12\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\21\60\1\u00c5\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u00c5\10\60",
            "\12\60\1\55\6\uffff\27\60\1\u00c6\2\60\4\uffff\1\60\1\uffff"+
            "\27\60\1\u00c6\2\60",
            "\12\60\1\55\6\uffff\6\60\1\u00c7\23\60\4\uffff\1\60\1\uffff"+
            "\6\60\1\u00c7\23\60",
            "",
            "\12\60\1\55\6\uffff\23\60\1\u00c8\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00c8\6\60",
            "\12\60\1\55\6\uffff\13\60\1\u00c9\16\60\4\uffff\1\60\1\uffff"+
            "\13\60\1\u00c9\16\60",
            "\12\60\1\55\6\uffff\23\60\1\u00ca\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00ca\6\60",
            "\12\60\1\55\6\uffff\14\60\1\u00cb\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u00cb\15\60",
            "\12\60\1\55\6\uffff\10\60\1\u00cc\21\60\4\uffff\1\60\1\uffff"+
            "\10\60\1\u00cc\21\60",
            "\12\60\1\55\6\uffff\1\u00cd\31\60\4\uffff\1\60\1\uffff\1\u00cd"+
            "\31\60",
            "\12\60\1\55\6\uffff\23\60\1\u00ce\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00ce\6\60",
            "\12\60\1\55\6\uffff\23\60\1\u00cf\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00cf\6\60",
            "\12\60\1\55\6\uffff\21\60\1\u00d0\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u00d0\10\60",
            "\12\60\1\55\6\uffff\24\60\1\u00d1\5\60\4\uffff\1\60\1\uffff"+
            "\24\60\1\u00d1\5\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\u00d2\1\uffff\32\60",
            "\12\60\1\55\6\uffff\22\60\1\u00d3\7\60\4\uffff\1\60\1\uffff"+
            "\22\60\1\u00d3\7\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\4\60\1\u00d5\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00d5\25\60",
            "\12\60\1\55\6\uffff\2\60\1\u00d6\27\60\4\uffff\1\60\1\uffff"+
            "\2\60\1\u00d6\27\60",
            "",
            "\12\60\1\55\6\uffff\4\60\1\u00d7\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00d7\25\60",
            "\12\60\1\55\6\uffff\7\60\1\u00d8\22\60\4\uffff\1\60\1\uffff"+
            "\7\60\1\u00d8\22\60",
            "\12\60\1\55\6\uffff\15\60\1\u00d9\14\60\4\uffff\1\60\1\uffff"+
            "\15\60\1\u00d9\14\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\23\60\1\u00db\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00db\6\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\1\u00dd\31\60\4\uffff\1\60\1\uffff\1\u00dd"+
            "\31\60",
            "\12\60\1\55\6\uffff\4\60\1\u00de\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00de\25\60",
            "\1\126\1\uffff\12\u00df\7\uffff\6\127\32\uffff\6\127",
            "\12\60\1\55\6\uffff\23\60\1\u00e0\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00e0\6\60",
            "\12\60\1\55\6\uffff\6\60\1\u00e1\23\60\4\uffff\1\60\1\uffff"+
            "\6\60\1\u00e1\23\60",
            "\12\60\1\55\6\uffff\1\u00e2\31\60\4\uffff\1\60\1\uffff\1\u00e2"+
            "\31\60",
            "",
            "\12\u00e3\1\55\6\uffff\6\u00e3\24\60\4\uffff\1\60\1\uffff\6"+
            "\u00e3\24\60",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\1\u00e7\31\60\4\uffff\1\60\1\uffff\1\u00e7"+
            "\31\60",
            "",
            "\12\60\1\55\6\uffff\23\60\1\u00e8\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00e8\6\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\4\60\1\u00eb\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00eb\25\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\u00ec\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\15\60\1\u00ee\14\60\4\uffff\1\60\1\uffff"+
            "\15\60\1\u00ee\14\60",
            "\12\60\1\55\6\uffff\22\60\1\u00ef\7\60\4\uffff\1\60\1\uffff"+
            "\22\60\1\u00ef\7\60",
            "\12\60\1\55\6\uffff\2\60\1\u00f0\27\60\4\uffff\1\60\1\uffff"+
            "\2\60\1\u00f0\27\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\4\60\1\u00f2\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00f2\25\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\14\60\1\u00f4\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u00f4\15\60",
            "\12\60\1\55\6\uffff\20\60\1\u00f5\11\60\4\uffff\1\60\1\uffff"+
            "\20\60\1\u00f5\11\60",
            "\12\60\1\55\6\uffff\23\60\1\u00f6\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u00f6\6\60",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\1\u00f7\31\60\4\uffff\1\60\1\uffff\1\u00f7"+
            "\31\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\60\1\55\6\uffff\4\60\1\u00fb\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u00fb\25\60",
            "",
            "\12\60\1\55\6\uffff\21\60\1\u00fc\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u00fc\10\60",
            "\12\60\1\55\6\uffff\22\60\1\u00fd\7\60\4\uffff\1\60\1\uffff"+
            "\22\60\1\u00fd\7\60",
            "\1\126\1\uffff\12\u00fe\7\uffff\6\127\32\uffff\6\127",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\4\60\1\u0100\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u0100\25\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\u0102\1\55\6\uffff\6\u0102\24\60\4\uffff\1\60\1\uffff\6"+
            "\u0102\24\60",
            "",
            "",
            "",
            "\12\60\1\55\6\uffff\2\60\1\u0103\27\60\4\uffff\1\60\1\uffff"+
            "\2\60\1\u0103\27\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\20\60\1\u0106\11\60\4\uffff\1\60\1\uffff"+
            "\20\60\1\u0106\11\60",
            "",
            "\12\60\1\55\6\uffff\5\60\1\u0107\24\60\4\uffff\1\60\1\uffff"+
            "\5\60\1\u0107\24\60",
            "\12\60\1\55\6\uffff\23\60\1\u0108\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u0108\6\60",
            "\12\60\1\55\6\uffff\23\60\1\u0109\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u0109\6\60",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\24\60\1\u010b\5\60\4\uffff\1\60\1\uffff"+
            "\24\60\1\u010b\5\60",
            "\12\60\1\55\6\uffff\1\u010c\31\60\4\uffff\1\60\1\uffff\1\u010c"+
            "\31\60",
            "\12\60\1\55\6\uffff\23\60\1\u010d\6\60\4\uffff\1\60\1\uffff"+
            "\23\60\1\u010d\6\60",
            "",
            "",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\30\60\1\u010f\1\60\4\uffff\1\60\1\uffff"+
            "\30\60\1\u010f\1\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\126\1\uffff\12\u0111\7\uffff\6\127\32\uffff\6\127",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\u0113\1\55\6\uffff\6\u0113\24\60\4\uffff\1\60\1\uffff\6"+
            "\u0113\24\60",
            "\12\60\1\55\6\uffff\4\60\1\u0114\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u0114\25\60",
            "",
            "",
            "\12\60\1\55\6\uffff\24\60\1\u0115\5\60\4\uffff\1\60\1\uffff"+
            "\24\60\1\u0115\5\60",
            "\12\60\1\55\6\uffff\1\u0116\31\60\4\uffff\1\60\1\uffff\1\u0116"+
            "\31\60",
            "\12\60\1\55\6\uffff\4\60\1\u0117\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u0117\25\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\60\1\55\6\uffff\16\60\1\u0119\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\u0119\13\60",
            "\12\60\1\55\6\uffff\14\60\1\u011a\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u011a\15\60",
            "\12\60\1\55\6\uffff\4\60\1\u011b\25\60\4\uffff\1\60\1\uffff"+
            "\4\60\1\u011b\25\60",
            "",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\126\1\uffff\12\u011d\7\uffff\6\127\32\uffff\6\127",
            "",
            "\1\127\2\uffff\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff"+
            "\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\16\60\1\u011e\13\60\4\uffff\1\60\1\uffff"+
            "\16\60\1\u011e\13\60",
            "\12\60\1\55\6\uffff\14\60\1\u011f\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u011f\15\60",
            "\12\60\1\55\6\uffff\15\60\1\u0120\14\60\4\uffff\1\60\1\uffff"+
            "\15\60\1\u0120\14\60",
            "",
            "\12\60\1\55\6\uffff\21\60\1\u0121\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u0121\10\60",
            "\12\60\1\55\6\uffff\17\60\1\u0122\12\60\4\uffff\1\60\1\uffff"+
            "\17\60\1\u0122\12\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\127\1\126\1\uffff\12\45",
            "\12\60\1\55\6\uffff\21\60\1\u0124\10\60\4\uffff\1\60\1\uffff"+
            "\21\60\1\u0124\10\60",
            "\12\60\1\55\6\uffff\10\60\1\u0125\21\60\4\uffff\1\60\1\uffff"+
            "\10\60\1\u0125\21\60",
            "\12\60\1\55\6\uffff\2\60\1\u0126\27\60\4\uffff\1\60\1\uffff"+
            "\2\60\1\u0126\27\60",
            "\12\60\1\55\6\uffff\24\60\1\u0127\5\60\4\uffff\1\60\1\uffff"+
            "\24\60\1\u0127\5\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\60\1\55\6\uffff\24\60\1\u0129\5\60\4\uffff\1\60\1\uffff"+
            "\24\60\1\u0129\5\60",
            "\12\60\1\55\6\uffff\13\60\1\u012a\16\60\4\uffff\1\60\1\uffff"+
            "\13\60\1\u012a\16\60",
            "\12\60\1\55\6\uffff\30\60\1\u012b\1\60\4\uffff\1\60\1\uffff"+
            "\30\60\1\u012b\1\60",
            "\12\60\1\55\6\uffff\14\60\1\u012c\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u012c\15\60",
            "",
            "\12\60\1\55\6\uffff\14\60\1\u012d\15\60\4\uffff\1\60\1\uffff"+
            "\14\60\1\u012d\15\60",
            "\12\60\1\55\6\uffff\30\60\1\u012e\1\60\4\uffff\1\60\1\uffff"+
            "\30\60\1\u012e\1\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\1\55\6\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            ""
    };

    static final short[] DFA15_eot = DFA.unpackEncodedString(DFA15_eotS);
    static final short[] DFA15_eof = DFA.unpackEncodedString(DFA15_eofS);
    static final char[] DFA15_min = DFA.unpackEncodedStringToUnsignedChars(DFA15_minS);
    static final char[] DFA15_max = DFA.unpackEncodedStringToUnsignedChars(DFA15_maxS);
    static final short[] DFA15_accept = DFA.unpackEncodedString(DFA15_acceptS);
    static final short[] DFA15_special = DFA.unpackEncodedString(DFA15_specialS);
    static final short[][] DFA15_transition;

    static {
        int numStates = DFA15_transitionS.length;
        DFA15_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA15_transition[i] = DFA.unpackEncodedString(DFA15_transitionS[i]);
        }
    }

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = DFA15_eot;
            this.eof = DFA15_eof;
            this.min = DFA15_min;
            this.max = DFA15_max;
            this.accept = DFA15_accept;
            this.special = DFA15_special;
            this.transition = DFA15_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | K_SELECT | K_FROM | K_WHERE | K_AND | K_KEY | K_INSERT | K_UPDATE | K_WITH | K_LIMIT | K_USING | K_CONSISTENCY | K_LEVEL | K_USE | K_COUNT | K_SET | K_BEGIN | K_APPLY | K_BATCH | K_TRUNCATE | K_DELETE | K_IN | K_CREATE | K_KEYSPACE | K_COLUMNFAMILY | K_INDEX | K_ON | K_DROP | K_PRIMARY | K_INTO | K_VALUES | K_TIMESTAMP | K_TTL | K_ALTER | K_ADD | K_TYPE | K_COMPACT | K_STORAGE | K_ORDER | K_BY | K_ASC | K_DESC | STRING_LITERAL | QUOTED_NAME | INTEGER | QMARK | FLOAT | IDENT | COMPIDENT | UUID | WS | COMMENT | MULTILINE_COMMENT );";
        }
    }
 

}