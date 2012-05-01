// $ANTLR 3.2 Sep 23, 2009 12:02:23 /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g 2012-04-26 23:16:18

package org.apache.cassandra.cli;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CliParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NODE_CONNECT", "NODE_DESCRIBE", "NODE_DESCRIBE_CLUSTER", "NODE_USE_TABLE", "NODE_EXIT", "NODE_HELP", "NODE_NO_OP", "NODE_SHOW_CLUSTER_NAME", "NODE_SHOW_VERSION", "NODE_SHOW_KEYSPACES", "NODE_SHOW_SCHEMA", "NODE_THRIFT_GET", "NODE_THRIFT_GET_WITH_CONDITIONS", "NODE_THRIFT_SET", "NODE_THRIFT_COUNT", "NODE_THRIFT_DEL", "NODE_THRIFT_INCR", "NODE_THRIFT_DECR", "NODE_ADD_COLUMN_FAMILY", "NODE_ADD_KEYSPACE", "NODE_DEL_KEYSPACE", "NODE_DEL_COLUMN_FAMILY", "NODE_UPDATE_KEYSPACE", "NODE_UPDATE_COLUMN_FAMILY", "NODE_LIST", "NODE_TRUNCATE", "NODE_ASSUME", "NODE_CONSISTENCY_LEVEL", "NODE_DROP_INDEX", "NODE_COLUMN_ACCESS", "NODE_ID_LIST", "NODE_NEW_CF_ACCESS", "NODE_NEW_KEYSPACE_ACCESS", "CONVERT_TO_TYPE", "FUNCTION_CALL", "CONDITION", "CONDITIONS", "ARRAY", "HASH", "PAIR", "NODE_LIMIT", "NODE_KEY_RANGE", "SEMICOLON", "CONNECT", "HELP", "USE", "DESCRIBE", "EXIT", "QUIT", "SHOW", "KEYSPACES", "SCHEMA", "API_VERSION", "CREATE", "KEYSPACE", "UPDATE", "COLUMN", "FAMILY", "DROP", "INDEX", "GET", "SET", "INCR", "DECR", "DEL", "COUNT", "LIST", "TRUNCATE", "ASSUME", "CONSISTENCYLEVEL", "IntegerPositiveLiteral", "Identifier", "StringLiteral", "WITH", "TTL", "BY", "ON", "AND", "IntegerNegativeLiteral", "DoubleLiteral", "IP_ADDRESS", "CONFIG", "FILE", "LIMIT", "Letter", "Digit", "Alnum", "SingleStringCharacter", "EscapeSequence", "CharacterEscapeSequence", "HexEscapeSequence", "UnicodeEscapeSequence", "SingleEscapeCharacter", "NonEscapeCharacter", "EscapeCharacter", "DecimalDigit", "HexDigit", "WS", "COMMENT", "'/'", "'CLUSTER'", "'CLUSTER NAME'", "'?'", "'AS'", "'WHERE'", "'='", "'>'", "'<'", "'>='", "'<='", "'.'", "'['", "','", "']'", "'{'", "'}'", "':'", "'('", "')'"
    };
    public static final int NODE_DESCRIBE=5;
    public static final int NODE_THRIFT_GET_WITH_CONDITIONS=16;
    public static final int TTL=78;
    public static final int NODE_SHOW_KEYSPACES=13;
    public static final int CONDITION=39;
    public static final int COUNT=69;
    public static final int DecimalDigit=99;
    public static final int EOF=-1;
    public static final int Identifier=75;
    public static final int NODE_UPDATE_COLUMN_FAMILY=27;
    public static final int SingleStringCharacter=91;
    public static final int NODE_USE_TABLE=7;
    public static final int NODE_DEL_KEYSPACE=24;
    public static final int CREATE=57;
    public static final int NODE_CONNECT=4;
    public static final int CONNECT=47;
    public static final int INCR=66;
    public static final int SingleEscapeCharacter=96;
    public static final int FAMILY=61;
    public static final int GET=64;
    public static final int COMMENT=102;
    public static final int SHOW=53;
    public static final int ARRAY=41;
    public static final int NODE_ADD_KEYSPACE=23;
    public static final int EXIT=51;
    public static final int NODE_THRIFT_DEL=19;
    public static final int IntegerNegativeLiteral=82;
    public static final int ON=80;
    public static final int NODE_DROP_INDEX=32;
    public static final int SEMICOLON=46;
    public static final int KEYSPACES=54;
    public static final int CONDITIONS=40;
    public static final int FILE=86;
    public static final int NODE_LIMIT=44;
    public static final int LIST=70;
    public static final int NODE_DESCRIBE_CLUSTER=6;
    public static final int IP_ADDRESS=84;
    public static final int NODE_THRIFT_SET=17;
    public static final int NODE_NO_OP=10;
    public static final int NODE_ID_LIST=34;
    public static final int WS=101;
    public static final int ASSUME=72;
    public static final int NODE_THRIFT_COUNT=18;
    public static final int DESCRIBE=50;
    public static final int Alnum=90;
    public static final int CharacterEscapeSequence=93;
    public static final int NODE_SHOW_CLUSTER_NAME=11;
    public static final int USE=49;
    public static final int NODE_THRIFT_DECR=21;
    public static final int FUNCTION_CALL=38;
    public static final int EscapeSequence=92;
    public static final int Letter=88;
    public static final int DoubleLiteral=83;
    public static final int HELP=48;
    public static final int HexEscapeSequence=94;
    public static final int NODE_EXIT=8;
    public static final int LIMIT=87;
    public static final int T__118=118;
    public static final int T__119=119;
    public static final int DEL=68;
    public static final int T__116=116;
    public static final int T__117=117;
    public static final int T__114=114;
    public static final int NODE_SHOW_SCHEMA=14;
    public static final int T__115=115;
    public static final int NODE_LIST=28;
    public static final int UPDATE=59;
    public static final int T__122=122;
    public static final int NODE_UPDATE_KEYSPACE=26;
    public static final int T__121=121;
    public static final int T__120=120;
    public static final int AND=81;
    public static final int NODE_NEW_CF_ACCESS=35;
    public static final int CONSISTENCYLEVEL=73;
    public static final int HexDigit=100;
    public static final int QUIT=52;
    public static final int NODE_TRUNCATE=29;
    public static final int INDEX=63;
    public static final int NODE_SHOW_VERSION=12;
    public static final int T__107=107;
    public static final int T__108=108;
    public static final int NODE_NEW_KEYSPACE_ACCESS=36;
    public static final int T__109=109;
    public static final int T__103=103;
    public static final int T__104=104;
    public static final int TRUNCATE=71;
    public static final int T__105=105;
    public static final int T__106=106;
    public static final int COLUMN=60;
    public static final int T__111=111;
    public static final int T__110=110;
    public static final int T__113=113;
    public static final int EscapeCharacter=98;
    public static final int T__112=112;
    public static final int PAIR=43;
    public static final int NODE_CONSISTENCY_LEVEL=31;
    public static final int WITH=77;
    public static final int BY=79;
    public static final int UnicodeEscapeSequence=95;
    public static final int HASH=42;
    public static final int SET=65;
    public static final int Digit=89;
    public static final int API_VERSION=56;
    public static final int NODE_ASSUME=30;
    public static final int CONVERT_TO_TYPE=37;
    public static final int NODE_THRIFT_GET=15;
    public static final int NODE_DEL_COLUMN_FAMILY=25;
    public static final int NODE_KEY_RANGE=45;
    public static final int KEYSPACE=58;
    public static final int StringLiteral=76;
    public static final int NODE_HELP=9;
    public static final int CONFIG=85;
    public static final int IntegerPositiveLiteral=74;
    public static final int SCHEMA=55;
    public static final int DROP=62;
    public static final int NonEscapeCharacter=97;
    public static final int DECR=67;
    public static final int NODE_ADD_COLUMN_FAMILY=22;
    public static final int NODE_THRIFT_INCR=20;
    public static final int NODE_COLUMN_ACCESS=33;

    // delegates
    // delegators


        public CliParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CliParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return CliParser.tokenNames; }
    public String getGrammarFileName() { return "/opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g"; }


        public void reportError(RecognitionException e) 
        {
            String errorMessage;

            if (e instanceof NoViableAltException)
            {
                errorMessage = "Command not found: `" + this.input + "`. Type 'help;' or '?' for help.";
            }
            else
            {
                errorMessage = "Syntax error at position " + e.charPositionInLine + ": " + this.getErrorMessage(e, this.getTokenNames());
            }

            throw new RuntimeException(errorMessage);
        }


    public static class root_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "root"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:140:1: root : statement ( SEMICOLON )? EOF -> statement ;
    public final CliParser.root_return root() throws RecognitionException {
        CliParser.root_return retval = new CliParser.root_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMICOLON2=null;
        Token EOF3=null;
        CliParser.statement_return statement1 = null;


        CommonTree SEMICOLON2_tree=null;
        CommonTree EOF3_tree=null;
        RewriteRuleTokenStream stream_SEMICOLON=new RewriteRuleTokenStream(adaptor,"token SEMICOLON");
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:140:5: ( statement ( SEMICOLON )? EOF -> statement )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:140:7: statement ( SEMICOLON )? EOF
            {
            pushFollow(FOLLOW_statement_in_root421);
            statement1=statement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_statement.add(statement1.getTree());
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:140:17: ( SEMICOLON )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==SEMICOLON) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:0:0: SEMICOLON
                    {
                    SEMICOLON2=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_root423); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMICOLON.add(SEMICOLON2);


                    }
                    break;

            }

            EOF3=(Token)match(input,EOF,FOLLOW_EOF_in_root426); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EOF.add(EOF3);



            // AST REWRITE
            // elements: statement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 140:32: -> statement
            {
                adaptor.addChild(root_0, stream_statement.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "root"

    public static class statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:142:1: statement : ( connectStatement | exitStatement | countStatement | describeTable | describeCluster | addKeyspace | addColumnFamily | updateKeyspace | updateColumnFamily | delColumnFamily | delKeyspace | useKeyspace | delStatement | getStatement | helpStatement | setStatement | incrStatement | showStatement | listStatement | truncateStatement | assumeStatement | consistencyLevelStatement | dropIndex | -> ^( NODE_NO_OP ) );
    public final CliParser.statement_return statement() throws RecognitionException {
        CliParser.statement_return retval = new CliParser.statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.connectStatement_return connectStatement4 = null;

        CliParser.exitStatement_return exitStatement5 = null;

        CliParser.countStatement_return countStatement6 = null;

        CliParser.describeTable_return describeTable7 = null;

        CliParser.describeCluster_return describeCluster8 = null;

        CliParser.addKeyspace_return addKeyspace9 = null;

        CliParser.addColumnFamily_return addColumnFamily10 = null;

        CliParser.updateKeyspace_return updateKeyspace11 = null;

        CliParser.updateColumnFamily_return updateColumnFamily12 = null;

        CliParser.delColumnFamily_return delColumnFamily13 = null;

        CliParser.delKeyspace_return delKeyspace14 = null;

        CliParser.useKeyspace_return useKeyspace15 = null;

        CliParser.delStatement_return delStatement16 = null;

        CliParser.getStatement_return getStatement17 = null;

        CliParser.helpStatement_return helpStatement18 = null;

        CliParser.setStatement_return setStatement19 = null;

        CliParser.incrStatement_return incrStatement20 = null;

        CliParser.showStatement_return showStatement21 = null;

        CliParser.listStatement_return listStatement22 = null;

        CliParser.truncateStatement_return truncateStatement23 = null;

        CliParser.assumeStatement_return assumeStatement24 = null;

        CliParser.consistencyLevelStatement_return consistencyLevelStatement25 = null;

        CliParser.dropIndex_return dropIndex26 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:143:5: ( connectStatement | exitStatement | countStatement | describeTable | describeCluster | addKeyspace | addColumnFamily | updateKeyspace | updateColumnFamily | delColumnFamily | delKeyspace | useKeyspace | delStatement | getStatement | helpStatement | setStatement | incrStatement | showStatement | listStatement | truncateStatement | assumeStatement | consistencyLevelStatement | dropIndex | -> ^( NODE_NO_OP ) )
            int alt2=24;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:143:7: connectStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_connectStatement_in_statement442);
                    connectStatement4=connectStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, connectStatement4.getTree());

                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:144:7: exitStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_exitStatement_in_statement450);
                    exitStatement5=exitStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, exitStatement5.getTree());

                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:145:7: countStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_countStatement_in_statement458);
                    countStatement6=countStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, countStatement6.getTree());

                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:146:7: describeTable
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_describeTable_in_statement466);
                    describeTable7=describeTable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, describeTable7.getTree());

                    }
                    break;
                case 5 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:147:7: describeCluster
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_describeCluster_in_statement474);
                    describeCluster8=describeCluster();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, describeCluster8.getTree());

                    }
                    break;
                case 6 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:148:7: addKeyspace
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_addKeyspace_in_statement482);
                    addKeyspace9=addKeyspace();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, addKeyspace9.getTree());

                    }
                    break;
                case 7 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:149:7: addColumnFamily
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_addColumnFamily_in_statement490);
                    addColumnFamily10=addColumnFamily();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, addColumnFamily10.getTree());

                    }
                    break;
                case 8 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:150:7: updateKeyspace
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_updateKeyspace_in_statement498);
                    updateKeyspace11=updateKeyspace();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, updateKeyspace11.getTree());

                    }
                    break;
                case 9 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:151:7: updateColumnFamily
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_updateColumnFamily_in_statement506);
                    updateColumnFamily12=updateColumnFamily();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, updateColumnFamily12.getTree());

                    }
                    break;
                case 10 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:152:7: delColumnFamily
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_delColumnFamily_in_statement514);
                    delColumnFamily13=delColumnFamily();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, delColumnFamily13.getTree());

                    }
                    break;
                case 11 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:153:7: delKeyspace
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_delKeyspace_in_statement522);
                    delKeyspace14=delKeyspace();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, delKeyspace14.getTree());

                    }
                    break;
                case 12 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:154:7: useKeyspace
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_useKeyspace_in_statement530);
                    useKeyspace15=useKeyspace();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, useKeyspace15.getTree());

                    }
                    break;
                case 13 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:155:7: delStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_delStatement_in_statement538);
                    delStatement16=delStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, delStatement16.getTree());

                    }
                    break;
                case 14 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:156:7: getStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_getStatement_in_statement546);
                    getStatement17=getStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, getStatement17.getTree());

                    }
                    break;
                case 15 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:157:7: helpStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_helpStatement_in_statement554);
                    helpStatement18=helpStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, helpStatement18.getTree());

                    }
                    break;
                case 16 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:158:7: setStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_setStatement_in_statement562);
                    setStatement19=setStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, setStatement19.getTree());

                    }
                    break;
                case 17 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:159:7: incrStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_incrStatement_in_statement570);
                    incrStatement20=incrStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, incrStatement20.getTree());

                    }
                    break;
                case 18 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:160:7: showStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_showStatement_in_statement578);
                    showStatement21=showStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, showStatement21.getTree());

                    }
                    break;
                case 19 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:161:7: listStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_listStatement_in_statement586);
                    listStatement22=listStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listStatement22.getTree());

                    }
                    break;
                case 20 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:162:7: truncateStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_truncateStatement_in_statement594);
                    truncateStatement23=truncateStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, truncateStatement23.getTree());

                    }
                    break;
                case 21 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:163:7: assumeStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assumeStatement_in_statement602);
                    assumeStatement24=assumeStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assumeStatement24.getTree());

                    }
                    break;
                case 22 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:164:7: consistencyLevelStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_consistencyLevelStatement_in_statement610);
                    consistencyLevelStatement25=consistencyLevelStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, consistencyLevelStatement25.getTree());

                    }
                    break;
                case 23 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:165:7: dropIndex
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_dropIndex_in_statement618);
                    dropIndex26=dropIndex();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dropIndex26.getTree());

                    }
                    break;
                case 24 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:166:7: 
                    {

                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 166:7: -> ^( NODE_NO_OP )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:166:10: ^( NODE_NO_OP )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_NO_OP, "NODE_NO_OP"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statement"

    public static class connectStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "connectStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:169:1: connectStatement : ( CONNECT host '/' port ( username password )? -> ^( NODE_CONNECT host port ( username password )? ) | CONNECT ip_address '/' port ( username password )? -> ^( NODE_CONNECT ip_address port ( username password )? ) );
    public final CliParser.connectStatement_return connectStatement() throws RecognitionException {
        CliParser.connectStatement_return retval = new CliParser.connectStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CONNECT27=null;
        Token char_literal29=null;
        Token CONNECT33=null;
        Token char_literal35=null;
        CliParser.host_return host28 = null;

        CliParser.port_return port30 = null;

        CliParser.username_return username31 = null;

        CliParser.password_return password32 = null;

        CliParser.ip_address_return ip_address34 = null;

        CliParser.port_return port36 = null;

        CliParser.username_return username37 = null;

        CliParser.password_return password38 = null;


        CommonTree CONNECT27_tree=null;
        CommonTree char_literal29_tree=null;
        CommonTree CONNECT33_tree=null;
        CommonTree char_literal35_tree=null;
        RewriteRuleTokenStream stream_CONNECT=new RewriteRuleTokenStream(adaptor,"token CONNECT");
        RewriteRuleTokenStream stream_103=new RewriteRuleTokenStream(adaptor,"token 103");
        RewriteRuleSubtreeStream stream_port=new RewriteRuleSubtreeStream(adaptor,"rule port");
        RewriteRuleSubtreeStream stream_ip_address=new RewriteRuleSubtreeStream(adaptor,"rule ip_address");
        RewriteRuleSubtreeStream stream_username=new RewriteRuleSubtreeStream(adaptor,"rule username");
        RewriteRuleSubtreeStream stream_host=new RewriteRuleSubtreeStream(adaptor,"rule host");
        RewriteRuleSubtreeStream stream_password=new RewriteRuleSubtreeStream(adaptor,"rule password");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:170:5: ( CONNECT host '/' port ( username password )? -> ^( NODE_CONNECT host port ( username password )? ) | CONNECT ip_address '/' port ( username password )? -> ^( NODE_CONNECT ip_address port ( username password )? ) )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==CONNECT) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==Identifier) ) {
                    alt5=1;
                }
                else if ( (LA5_1==IP_ADDRESS) ) {
                    alt5=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:170:7: CONNECT host '/' port ( username password )?
                    {
                    CONNECT27=(Token)match(input,CONNECT,FOLLOW_CONNECT_in_connectStatement647); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CONNECT.add(CONNECT27);

                    pushFollow(FOLLOW_host_in_connectStatement649);
                    host28=host();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_host.add(host28.getTree());
                    char_literal29=(Token)match(input,103,FOLLOW_103_in_connectStatement651); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_103.add(char_literal29);

                    pushFollow(FOLLOW_port_in_connectStatement653);
                    port30=port();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_port.add(port30.getTree());
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:170:29: ( username password )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==Identifier) ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:170:30: username password
                            {
                            pushFollow(FOLLOW_username_in_connectStatement656);
                            username31=username();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_username.add(username31.getTree());
                            pushFollow(FOLLOW_password_in_connectStatement658);
                            password32=password();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_password.add(password32.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: host, port, username, password
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 171:9: -> ^( NODE_CONNECT host port ( username password )? )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:171:12: ^( NODE_CONNECT host port ( username password )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_CONNECT, "NODE_CONNECT"), root_1);

                        adaptor.addChild(root_1, stream_host.nextTree());
                        adaptor.addChild(root_1, stream_port.nextTree());
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:171:37: ( username password )?
                        if ( stream_username.hasNext()||stream_password.hasNext() ) {
                            adaptor.addChild(root_1, stream_username.nextTree());
                            adaptor.addChild(root_1, stream_password.nextTree());

                        }
                        stream_username.reset();
                        stream_password.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:172:7: CONNECT ip_address '/' port ( username password )?
                    {
                    CONNECT33=(Token)match(input,CONNECT,FOLLOW_CONNECT_in_connectStatement693); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CONNECT.add(CONNECT33);

                    pushFollow(FOLLOW_ip_address_in_connectStatement695);
                    ip_address34=ip_address();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ip_address.add(ip_address34.getTree());
                    char_literal35=(Token)match(input,103,FOLLOW_103_in_connectStatement697); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_103.add(char_literal35);

                    pushFollow(FOLLOW_port_in_connectStatement699);
                    port36=port();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_port.add(port36.getTree());
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:172:35: ( username password )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==Identifier) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:172:36: username password
                            {
                            pushFollow(FOLLOW_username_in_connectStatement702);
                            username37=username();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_username.add(username37.getTree());
                            pushFollow(FOLLOW_password_in_connectStatement704);
                            password38=password();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_password.add(password38.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: password, port, username, ip_address
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 173:9: -> ^( NODE_CONNECT ip_address port ( username password )? )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:173:12: ^( NODE_CONNECT ip_address port ( username password )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_CONNECT, "NODE_CONNECT"), root_1);

                        adaptor.addChild(root_1, stream_ip_address.nextTree());
                        adaptor.addChild(root_1, stream_port.nextTree());
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:173:43: ( username password )?
                        if ( stream_password.hasNext()||stream_username.hasNext() ) {
                            adaptor.addChild(root_1, stream_username.nextTree());
                            adaptor.addChild(root_1, stream_password.nextTree());

                        }
                        stream_password.reset();
                        stream_username.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "connectStatement"

    public static class helpStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "helpStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:176:1: helpStatement : ( HELP HELP -> ^( NODE_HELP NODE_HELP ) | HELP CONNECT -> ^( NODE_HELP NODE_CONNECT ) | HELP USE -> ^( NODE_HELP NODE_USE_TABLE ) | HELP DESCRIBE -> ^( NODE_HELP NODE_DESCRIBE ) | HELP DESCRIBE 'CLUSTER' -> ^( NODE_HELP NODE_DESCRIBE_CLUSTER ) | HELP EXIT -> ^( NODE_HELP NODE_EXIT ) | HELP QUIT -> ^( NODE_HELP NODE_EXIT ) | HELP SHOW 'CLUSTER NAME' -> ^( NODE_HELP NODE_SHOW_CLUSTER_NAME ) | HELP SHOW KEYSPACES -> ^( NODE_HELP NODE_SHOW_KEYSPACES ) | HELP SHOW SCHEMA -> ^( NODE_HELP NODE_SHOW_SCHEMA ) | HELP SHOW API_VERSION -> ^( NODE_HELP NODE_SHOW_VERSION ) | HELP CREATE KEYSPACE -> ^( NODE_HELP NODE_ADD_KEYSPACE ) | HELP UPDATE KEYSPACE -> ^( NODE_HELP NODE_UPDATE_KEYSPACE ) | HELP CREATE COLUMN FAMILY -> ^( NODE_HELP NODE_ADD_COLUMN_FAMILY ) | HELP UPDATE COLUMN FAMILY -> ^( NODE_HELP NODE_UPDATE_COLUMN_FAMILY ) | HELP DROP KEYSPACE -> ^( NODE_HELP NODE_DEL_KEYSPACE ) | HELP DROP COLUMN FAMILY -> ^( NODE_HELP NODE_DEL_COLUMN_FAMILY ) | HELP DROP INDEX -> ^( NODE_HELP NODE_DROP_INDEX ) | HELP GET -> ^( NODE_HELP NODE_THRIFT_GET ) | HELP SET -> ^( NODE_HELP NODE_THRIFT_SET ) | HELP INCR -> ^( NODE_HELP NODE_THRIFT_INCR ) | HELP DECR -> ^( NODE_HELP NODE_THRIFT_DECR ) | HELP DEL -> ^( NODE_HELP NODE_THRIFT_DEL ) | HELP COUNT -> ^( NODE_HELP NODE_THRIFT_COUNT ) | HELP LIST -> ^( NODE_HELP NODE_LIST ) | HELP TRUNCATE -> ^( NODE_HELP NODE_TRUNCATE ) | HELP ASSUME -> ^( NODE_HELP NODE_ASSUME ) | HELP CONSISTENCYLEVEL -> ^( NODE_HELP NODE_CONSISTENCY_LEVEL ) | HELP -> ^( NODE_HELP ) | '?' -> ^( NODE_HELP ) );
    public final CliParser.helpStatement_return helpStatement() throws RecognitionException {
        CliParser.helpStatement_return retval = new CliParser.helpStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token HELP39=null;
        Token HELP40=null;
        Token HELP41=null;
        Token CONNECT42=null;
        Token HELP43=null;
        Token USE44=null;
        Token HELP45=null;
        Token DESCRIBE46=null;
        Token HELP47=null;
        Token DESCRIBE48=null;
        Token string_literal49=null;
        Token HELP50=null;
        Token EXIT51=null;
        Token HELP52=null;
        Token QUIT53=null;
        Token HELP54=null;
        Token SHOW55=null;
        Token string_literal56=null;
        Token HELP57=null;
        Token SHOW58=null;
        Token KEYSPACES59=null;
        Token HELP60=null;
        Token SHOW61=null;
        Token SCHEMA62=null;
        Token HELP63=null;
        Token SHOW64=null;
        Token API_VERSION65=null;
        Token HELP66=null;
        Token CREATE67=null;
        Token KEYSPACE68=null;
        Token HELP69=null;
        Token UPDATE70=null;
        Token KEYSPACE71=null;
        Token HELP72=null;
        Token CREATE73=null;
        Token COLUMN74=null;
        Token FAMILY75=null;
        Token HELP76=null;
        Token UPDATE77=null;
        Token COLUMN78=null;
        Token FAMILY79=null;
        Token HELP80=null;
        Token DROP81=null;
        Token KEYSPACE82=null;
        Token HELP83=null;
        Token DROP84=null;
        Token COLUMN85=null;
        Token FAMILY86=null;
        Token HELP87=null;
        Token DROP88=null;
        Token INDEX89=null;
        Token HELP90=null;
        Token GET91=null;
        Token HELP92=null;
        Token SET93=null;
        Token HELP94=null;
        Token INCR95=null;
        Token HELP96=null;
        Token DECR97=null;
        Token HELP98=null;
        Token DEL99=null;
        Token HELP100=null;
        Token COUNT101=null;
        Token HELP102=null;
        Token LIST103=null;
        Token HELP104=null;
        Token TRUNCATE105=null;
        Token HELP106=null;
        Token ASSUME107=null;
        Token HELP108=null;
        Token CONSISTENCYLEVEL109=null;
        Token HELP110=null;
        Token char_literal111=null;

        CommonTree HELP39_tree=null;
        CommonTree HELP40_tree=null;
        CommonTree HELP41_tree=null;
        CommonTree CONNECT42_tree=null;
        CommonTree HELP43_tree=null;
        CommonTree USE44_tree=null;
        CommonTree HELP45_tree=null;
        CommonTree DESCRIBE46_tree=null;
        CommonTree HELP47_tree=null;
        CommonTree DESCRIBE48_tree=null;
        CommonTree string_literal49_tree=null;
        CommonTree HELP50_tree=null;
        CommonTree EXIT51_tree=null;
        CommonTree HELP52_tree=null;
        CommonTree QUIT53_tree=null;
        CommonTree HELP54_tree=null;
        CommonTree SHOW55_tree=null;
        CommonTree string_literal56_tree=null;
        CommonTree HELP57_tree=null;
        CommonTree SHOW58_tree=null;
        CommonTree KEYSPACES59_tree=null;
        CommonTree HELP60_tree=null;
        CommonTree SHOW61_tree=null;
        CommonTree SCHEMA62_tree=null;
        CommonTree HELP63_tree=null;
        CommonTree SHOW64_tree=null;
        CommonTree API_VERSION65_tree=null;
        CommonTree HELP66_tree=null;
        CommonTree CREATE67_tree=null;
        CommonTree KEYSPACE68_tree=null;
        CommonTree HELP69_tree=null;
        CommonTree UPDATE70_tree=null;
        CommonTree KEYSPACE71_tree=null;
        CommonTree HELP72_tree=null;
        CommonTree CREATE73_tree=null;
        CommonTree COLUMN74_tree=null;
        CommonTree FAMILY75_tree=null;
        CommonTree HELP76_tree=null;
        CommonTree UPDATE77_tree=null;
        CommonTree COLUMN78_tree=null;
        CommonTree FAMILY79_tree=null;
        CommonTree HELP80_tree=null;
        CommonTree DROP81_tree=null;
        CommonTree KEYSPACE82_tree=null;
        CommonTree HELP83_tree=null;
        CommonTree DROP84_tree=null;
        CommonTree COLUMN85_tree=null;
        CommonTree FAMILY86_tree=null;
        CommonTree HELP87_tree=null;
        CommonTree DROP88_tree=null;
        CommonTree INDEX89_tree=null;
        CommonTree HELP90_tree=null;
        CommonTree GET91_tree=null;
        CommonTree HELP92_tree=null;
        CommonTree SET93_tree=null;
        CommonTree HELP94_tree=null;
        CommonTree INCR95_tree=null;
        CommonTree HELP96_tree=null;
        CommonTree DECR97_tree=null;
        CommonTree HELP98_tree=null;
        CommonTree DEL99_tree=null;
        CommonTree HELP100_tree=null;
        CommonTree COUNT101_tree=null;
        CommonTree HELP102_tree=null;
        CommonTree LIST103_tree=null;
        CommonTree HELP104_tree=null;
        CommonTree TRUNCATE105_tree=null;
        CommonTree HELP106_tree=null;
        CommonTree ASSUME107_tree=null;
        CommonTree HELP108_tree=null;
        CommonTree CONSISTENCYLEVEL109_tree=null;
        CommonTree HELP110_tree=null;
        CommonTree char_literal111_tree=null;
        RewriteRuleTokenStream stream_EXIT=new RewriteRuleTokenStream(adaptor,"token EXIT");
        RewriteRuleTokenStream stream_HELP=new RewriteRuleTokenStream(adaptor,"token HELP");
        RewriteRuleTokenStream stream_DEL=new RewriteRuleTokenStream(adaptor,"token DEL");
        RewriteRuleTokenStream stream_UPDATE=new RewriteRuleTokenStream(adaptor,"token UPDATE");
        RewriteRuleTokenStream stream_SET=new RewriteRuleTokenStream(adaptor,"token SET");
        RewriteRuleTokenStream stream_COUNT=new RewriteRuleTokenStream(adaptor,"token COUNT");
        RewriteRuleTokenStream stream_KEYSPACES=new RewriteRuleTokenStream(adaptor,"token KEYSPACES");
        RewriteRuleTokenStream stream_106=new RewriteRuleTokenStream(adaptor,"token 106");
        RewriteRuleTokenStream stream_API_VERSION=new RewriteRuleTokenStream(adaptor,"token API_VERSION");
        RewriteRuleTokenStream stream_CONSISTENCYLEVEL=new RewriteRuleTokenStream(adaptor,"token CONSISTENCYLEVEL");
        RewriteRuleTokenStream stream_LIST=new RewriteRuleTokenStream(adaptor,"token LIST");
        RewriteRuleTokenStream stream_105=new RewriteRuleTokenStream(adaptor,"token 105");
        RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
        RewriteRuleTokenStream stream_QUIT=new RewriteRuleTokenStream(adaptor,"token QUIT");
        RewriteRuleTokenStream stream_KEYSPACE=new RewriteRuleTokenStream(adaptor,"token KEYSPACE");
        RewriteRuleTokenStream stream_INDEX=new RewriteRuleTokenStream(adaptor,"token INDEX");
        RewriteRuleTokenStream stream_CREATE=new RewriteRuleTokenStream(adaptor,"token CREATE");
        RewriteRuleTokenStream stream_SCHEMA=new RewriteRuleTokenStream(adaptor,"token SCHEMA");
        RewriteRuleTokenStream stream_CONNECT=new RewriteRuleTokenStream(adaptor,"token CONNECT");
        RewriteRuleTokenStream stream_DROP=new RewriteRuleTokenStream(adaptor,"token DROP");
        RewriteRuleTokenStream stream_INCR=new RewriteRuleTokenStream(adaptor,"token INCR");
        RewriteRuleTokenStream stream_ASSUME=new RewriteRuleTokenStream(adaptor,"token ASSUME");
        RewriteRuleTokenStream stream_TRUNCATE=new RewriteRuleTokenStream(adaptor,"token TRUNCATE");
        RewriteRuleTokenStream stream_DESCRIBE=new RewriteRuleTokenStream(adaptor,"token DESCRIBE");
        RewriteRuleTokenStream stream_COLUMN=new RewriteRuleTokenStream(adaptor,"token COLUMN");
        RewriteRuleTokenStream stream_FAMILY=new RewriteRuleTokenStream(adaptor,"token FAMILY");
        RewriteRuleTokenStream stream_DECR=new RewriteRuleTokenStream(adaptor,"token DECR");
        RewriteRuleTokenStream stream_GET=new RewriteRuleTokenStream(adaptor,"token GET");
        RewriteRuleTokenStream stream_USE=new RewriteRuleTokenStream(adaptor,"token USE");
        RewriteRuleTokenStream stream_SHOW=new RewriteRuleTokenStream(adaptor,"token SHOW");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:177:5: ( HELP HELP -> ^( NODE_HELP NODE_HELP ) | HELP CONNECT -> ^( NODE_HELP NODE_CONNECT ) | HELP USE -> ^( NODE_HELP NODE_USE_TABLE ) | HELP DESCRIBE -> ^( NODE_HELP NODE_DESCRIBE ) | HELP DESCRIBE 'CLUSTER' -> ^( NODE_HELP NODE_DESCRIBE_CLUSTER ) | HELP EXIT -> ^( NODE_HELP NODE_EXIT ) | HELP QUIT -> ^( NODE_HELP NODE_EXIT ) | HELP SHOW 'CLUSTER NAME' -> ^( NODE_HELP NODE_SHOW_CLUSTER_NAME ) | HELP SHOW KEYSPACES -> ^( NODE_HELP NODE_SHOW_KEYSPACES ) | HELP SHOW SCHEMA -> ^( NODE_HELP NODE_SHOW_SCHEMA ) | HELP SHOW API_VERSION -> ^( NODE_HELP NODE_SHOW_VERSION ) | HELP CREATE KEYSPACE -> ^( NODE_HELP NODE_ADD_KEYSPACE ) | HELP UPDATE KEYSPACE -> ^( NODE_HELP NODE_UPDATE_KEYSPACE ) | HELP CREATE COLUMN FAMILY -> ^( NODE_HELP NODE_ADD_COLUMN_FAMILY ) | HELP UPDATE COLUMN FAMILY -> ^( NODE_HELP NODE_UPDATE_COLUMN_FAMILY ) | HELP DROP KEYSPACE -> ^( NODE_HELP NODE_DEL_KEYSPACE ) | HELP DROP COLUMN FAMILY -> ^( NODE_HELP NODE_DEL_COLUMN_FAMILY ) | HELP DROP INDEX -> ^( NODE_HELP NODE_DROP_INDEX ) | HELP GET -> ^( NODE_HELP NODE_THRIFT_GET ) | HELP SET -> ^( NODE_HELP NODE_THRIFT_SET ) | HELP INCR -> ^( NODE_HELP NODE_THRIFT_INCR ) | HELP DECR -> ^( NODE_HELP NODE_THRIFT_DECR ) | HELP DEL -> ^( NODE_HELP NODE_THRIFT_DEL ) | HELP COUNT -> ^( NODE_HELP NODE_THRIFT_COUNT ) | HELP LIST -> ^( NODE_HELP NODE_LIST ) | HELP TRUNCATE -> ^( NODE_HELP NODE_TRUNCATE ) | HELP ASSUME -> ^( NODE_HELP NODE_ASSUME ) | HELP CONSISTENCYLEVEL -> ^( NODE_HELP NODE_CONSISTENCY_LEVEL ) | HELP -> ^( NODE_HELP ) | '?' -> ^( NODE_HELP ) )
            int alt6=30;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:177:7: HELP HELP
                    {
                    HELP39=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement748); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP39);

                    HELP40=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement750); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP40);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 178:9: -> ^( NODE_HELP NODE_HELP )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:178:12: ^( NODE_HELP NODE_HELP )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:179:7: HELP CONNECT
                    {
                    HELP41=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement775); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP41);

                    CONNECT42=(Token)match(input,CONNECT,FOLLOW_CONNECT_in_helpStatement777); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CONNECT.add(CONNECT42);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 180:9: -> ^( NODE_HELP NODE_CONNECT )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:180:12: ^( NODE_HELP NODE_CONNECT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_CONNECT, "NODE_CONNECT"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:181:7: HELP USE
                    {
                    HELP43=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement802); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP43);

                    USE44=(Token)match(input,USE,FOLLOW_USE_in_helpStatement804); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_USE.add(USE44);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 182:9: -> ^( NODE_HELP NODE_USE_TABLE )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:182:12: ^( NODE_HELP NODE_USE_TABLE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_USE_TABLE, "NODE_USE_TABLE"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:183:7: HELP DESCRIBE
                    {
                    HELP45=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement829); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP45);

                    DESCRIBE46=(Token)match(input,DESCRIBE,FOLLOW_DESCRIBE_in_helpStatement831); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DESCRIBE.add(DESCRIBE46);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 184:9: -> ^( NODE_HELP NODE_DESCRIBE )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:184:12: ^( NODE_HELP NODE_DESCRIBE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_DESCRIBE, "NODE_DESCRIBE"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:185:7: HELP DESCRIBE 'CLUSTER'
                    {
                    HELP47=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement855); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP47);

                    DESCRIBE48=(Token)match(input,DESCRIBE,FOLLOW_DESCRIBE_in_helpStatement857); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DESCRIBE.add(DESCRIBE48);

                    string_literal49=(Token)match(input,104,FOLLOW_104_in_helpStatement859); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_104.add(string_literal49);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 186:9: -> ^( NODE_HELP NODE_DESCRIBE_CLUSTER )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:186:12: ^( NODE_HELP NODE_DESCRIBE_CLUSTER )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_DESCRIBE_CLUSTER, "NODE_DESCRIBE_CLUSTER"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:187:7: HELP EXIT
                    {
                    HELP50=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement883); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP50);

                    EXIT51=(Token)match(input,EXIT,FOLLOW_EXIT_in_helpStatement885); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EXIT.add(EXIT51);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 188:9: -> ^( NODE_HELP NODE_EXIT )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:188:12: ^( NODE_HELP NODE_EXIT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_EXIT, "NODE_EXIT"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:189:7: HELP QUIT
                    {
                    HELP52=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement910); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP52);

                    QUIT53=(Token)match(input,QUIT,FOLLOW_QUIT_in_helpStatement912); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUIT.add(QUIT53);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 190:9: -> ^( NODE_HELP NODE_EXIT )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:190:12: ^( NODE_HELP NODE_EXIT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_EXIT, "NODE_EXIT"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:191:7: HELP SHOW 'CLUSTER NAME'
                    {
                    HELP54=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement937); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP54);

                    SHOW55=(Token)match(input,SHOW,FOLLOW_SHOW_in_helpStatement939); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SHOW.add(SHOW55);

                    string_literal56=(Token)match(input,105,FOLLOW_105_in_helpStatement941); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_105.add(string_literal56);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 192:9: -> ^( NODE_HELP NODE_SHOW_CLUSTER_NAME )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:192:12: ^( NODE_HELP NODE_SHOW_CLUSTER_NAME )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_SHOW_CLUSTER_NAME, "NODE_SHOW_CLUSTER_NAME"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:193:7: HELP SHOW KEYSPACES
                    {
                    HELP57=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement965); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP57);

                    SHOW58=(Token)match(input,SHOW,FOLLOW_SHOW_in_helpStatement967); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SHOW.add(SHOW58);

                    KEYSPACES59=(Token)match(input,KEYSPACES,FOLLOW_KEYSPACES_in_helpStatement969); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEYSPACES.add(KEYSPACES59);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 194:9: -> ^( NODE_HELP NODE_SHOW_KEYSPACES )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:194:12: ^( NODE_HELP NODE_SHOW_KEYSPACES )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_SHOW_KEYSPACES, "NODE_SHOW_KEYSPACES"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:195:7: HELP SHOW SCHEMA
                    {
                    HELP60=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement994); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP60);

                    SHOW61=(Token)match(input,SHOW,FOLLOW_SHOW_in_helpStatement996); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SHOW.add(SHOW61);

                    SCHEMA62=(Token)match(input,SCHEMA,FOLLOW_SCHEMA_in_helpStatement998); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SCHEMA.add(SCHEMA62);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 196:13: -> ^( NODE_HELP NODE_SHOW_SCHEMA )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:196:16: ^( NODE_HELP NODE_SHOW_SCHEMA )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_SHOW_SCHEMA, "NODE_SHOW_SCHEMA"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 11 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:197:7: HELP SHOW API_VERSION
                    {
                    HELP63=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1026); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP63);

                    SHOW64=(Token)match(input,SHOW,FOLLOW_SHOW_in_helpStatement1028); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SHOW.add(SHOW64);

                    API_VERSION65=(Token)match(input,API_VERSION,FOLLOW_API_VERSION_in_helpStatement1030); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_API_VERSION.add(API_VERSION65);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 198:9: -> ^( NODE_HELP NODE_SHOW_VERSION )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:198:12: ^( NODE_HELP NODE_SHOW_VERSION )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_SHOW_VERSION, "NODE_SHOW_VERSION"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 12 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:199:7: HELP CREATE KEYSPACE
                    {
                    HELP66=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1054); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP66);

                    CREATE67=(Token)match(input,CREATE,FOLLOW_CREATE_in_helpStatement1056); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CREATE.add(CREATE67);

                    KEYSPACE68=(Token)match(input,KEYSPACE,FOLLOW_KEYSPACE_in_helpStatement1058); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEYSPACE.add(KEYSPACE68);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 200:9: -> ^( NODE_HELP NODE_ADD_KEYSPACE )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:200:12: ^( NODE_HELP NODE_ADD_KEYSPACE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_ADD_KEYSPACE, "NODE_ADD_KEYSPACE"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 13 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:201:7: HELP UPDATE KEYSPACE
                    {
                    HELP69=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1083); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP69);

                    UPDATE70=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_helpStatement1085); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_UPDATE.add(UPDATE70);

                    KEYSPACE71=(Token)match(input,KEYSPACE,FOLLOW_KEYSPACE_in_helpStatement1087); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEYSPACE.add(KEYSPACE71);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 202:9: -> ^( NODE_HELP NODE_UPDATE_KEYSPACE )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:202:12: ^( NODE_HELP NODE_UPDATE_KEYSPACE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_UPDATE_KEYSPACE, "NODE_UPDATE_KEYSPACE"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 14 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:203:7: HELP CREATE COLUMN FAMILY
                    {
                    HELP72=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1111); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP72);

                    CREATE73=(Token)match(input,CREATE,FOLLOW_CREATE_in_helpStatement1113); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CREATE.add(CREATE73);

                    COLUMN74=(Token)match(input,COLUMN,FOLLOW_COLUMN_in_helpStatement1115); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLUMN.add(COLUMN74);

                    FAMILY75=(Token)match(input,FAMILY,FOLLOW_FAMILY_in_helpStatement1117); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FAMILY.add(FAMILY75);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 204:9: -> ^( NODE_HELP NODE_ADD_COLUMN_FAMILY )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:204:12: ^( NODE_HELP NODE_ADD_COLUMN_FAMILY )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_ADD_COLUMN_FAMILY, "NODE_ADD_COLUMN_FAMILY"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 15 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:205:7: HELP UPDATE COLUMN FAMILY
                    {
                    HELP76=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1142); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP76);

                    UPDATE77=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_helpStatement1144); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_UPDATE.add(UPDATE77);

                    COLUMN78=(Token)match(input,COLUMN,FOLLOW_COLUMN_in_helpStatement1146); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLUMN.add(COLUMN78);

                    FAMILY79=(Token)match(input,FAMILY,FOLLOW_FAMILY_in_helpStatement1148); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FAMILY.add(FAMILY79);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 206:9: -> ^( NODE_HELP NODE_UPDATE_COLUMN_FAMILY )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:206:12: ^( NODE_HELP NODE_UPDATE_COLUMN_FAMILY )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_UPDATE_COLUMN_FAMILY, "NODE_UPDATE_COLUMN_FAMILY"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 16 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:207:7: HELP DROP KEYSPACE
                    {
                    HELP80=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1172); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP80);

                    DROP81=(Token)match(input,DROP,FOLLOW_DROP_in_helpStatement1174); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DROP.add(DROP81);

                    KEYSPACE82=(Token)match(input,KEYSPACE,FOLLOW_KEYSPACE_in_helpStatement1176); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEYSPACE.add(KEYSPACE82);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 208:9: -> ^( NODE_HELP NODE_DEL_KEYSPACE )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:208:12: ^( NODE_HELP NODE_DEL_KEYSPACE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_DEL_KEYSPACE, "NODE_DEL_KEYSPACE"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 17 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:209:7: HELP DROP COLUMN FAMILY
                    {
                    HELP83=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1201); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP83);

                    DROP84=(Token)match(input,DROP,FOLLOW_DROP_in_helpStatement1203); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DROP.add(DROP84);

                    COLUMN85=(Token)match(input,COLUMN,FOLLOW_COLUMN_in_helpStatement1205); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLUMN.add(COLUMN85);

                    FAMILY86=(Token)match(input,FAMILY,FOLLOW_FAMILY_in_helpStatement1207); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FAMILY.add(FAMILY86);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 210:9: -> ^( NODE_HELP NODE_DEL_COLUMN_FAMILY )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:210:12: ^( NODE_HELP NODE_DEL_COLUMN_FAMILY )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_DEL_COLUMN_FAMILY, "NODE_DEL_COLUMN_FAMILY"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 18 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:211:7: HELP DROP INDEX
                    {
                    HELP87=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1232); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP87);

                    DROP88=(Token)match(input,DROP,FOLLOW_DROP_in_helpStatement1234); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DROP.add(DROP88);

                    INDEX89=(Token)match(input,INDEX,FOLLOW_INDEX_in_helpStatement1236); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INDEX.add(INDEX89);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 212:9: -> ^( NODE_HELP NODE_DROP_INDEX )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:212:12: ^( NODE_HELP NODE_DROP_INDEX )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_DROP_INDEX, "NODE_DROP_INDEX"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 19 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:213:7: HELP GET
                    {
                    HELP90=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1260); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP90);

                    GET91=(Token)match(input,GET,FOLLOW_GET_in_helpStatement1262); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_GET.add(GET91);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 214:9: -> ^( NODE_HELP NODE_THRIFT_GET )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:214:12: ^( NODE_HELP NODE_THRIFT_GET )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_THRIFT_GET, "NODE_THRIFT_GET"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 20 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:215:7: HELP SET
                    {
                    HELP92=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1287); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP92);

                    SET93=(Token)match(input,SET,FOLLOW_SET_in_helpStatement1289); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET93);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 216:9: -> ^( NODE_HELP NODE_THRIFT_SET )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:216:12: ^( NODE_HELP NODE_THRIFT_SET )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_THRIFT_SET, "NODE_THRIFT_SET"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 21 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:217:7: HELP INCR
                    {
                    HELP94=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1314); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP94);

                    INCR95=(Token)match(input,INCR,FOLLOW_INCR_in_helpStatement1316); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INCR.add(INCR95);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 218:9: -> ^( NODE_HELP NODE_THRIFT_INCR )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:218:12: ^( NODE_HELP NODE_THRIFT_INCR )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_THRIFT_INCR, "NODE_THRIFT_INCR"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 22 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:219:7: HELP DECR
                    {
                    HELP96=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1340); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP96);

                    DECR97=(Token)match(input,DECR,FOLLOW_DECR_in_helpStatement1342); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DECR.add(DECR97);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 220:9: -> ^( NODE_HELP NODE_THRIFT_DECR )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:220:12: ^( NODE_HELP NODE_THRIFT_DECR )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_THRIFT_DECR, "NODE_THRIFT_DECR"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 23 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:221:7: HELP DEL
                    {
                    HELP98=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1366); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP98);

                    DEL99=(Token)match(input,DEL,FOLLOW_DEL_in_helpStatement1368); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DEL.add(DEL99);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 222:9: -> ^( NODE_HELP NODE_THRIFT_DEL )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:222:12: ^( NODE_HELP NODE_THRIFT_DEL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_THRIFT_DEL, "NODE_THRIFT_DEL"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 24 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:223:7: HELP COUNT
                    {
                    HELP100=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1393); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP100);

                    COUNT101=(Token)match(input,COUNT,FOLLOW_COUNT_in_helpStatement1395); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COUNT.add(COUNT101);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 224:9: -> ^( NODE_HELP NODE_THRIFT_COUNT )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:224:12: ^( NODE_HELP NODE_THRIFT_COUNT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_THRIFT_COUNT, "NODE_THRIFT_COUNT"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 25 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:225:7: HELP LIST
                    {
                    HELP102=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1420); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP102);

                    LIST103=(Token)match(input,LIST,FOLLOW_LIST_in_helpStatement1422); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LIST.add(LIST103);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 226:9: -> ^( NODE_HELP NODE_LIST )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:226:12: ^( NODE_HELP NODE_LIST )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_LIST, "NODE_LIST"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 26 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:227:7: HELP TRUNCATE
                    {
                    HELP104=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1447); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP104);

                    TRUNCATE105=(Token)match(input,TRUNCATE,FOLLOW_TRUNCATE_in_helpStatement1449); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUNCATE.add(TRUNCATE105);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 228:9: -> ^( NODE_HELP NODE_TRUNCATE )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:228:12: ^( NODE_HELP NODE_TRUNCATE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_TRUNCATE, "NODE_TRUNCATE"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 27 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:229:7: HELP ASSUME
                    {
                    HELP106=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1473); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP106);

                    ASSUME107=(Token)match(input,ASSUME,FOLLOW_ASSUME_in_helpStatement1475); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ASSUME.add(ASSUME107);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 230:9: -> ^( NODE_HELP NODE_ASSUME )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:230:12: ^( NODE_HELP NODE_ASSUME )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_ASSUME, "NODE_ASSUME"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 28 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:231:7: HELP CONSISTENCYLEVEL
                    {
                    HELP108=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1499); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP108);

                    CONSISTENCYLEVEL109=(Token)match(input,CONSISTENCYLEVEL,FOLLOW_CONSISTENCYLEVEL_in_helpStatement1501); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CONSISTENCYLEVEL.add(CONSISTENCYLEVEL109);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 232:9: -> ^( NODE_HELP NODE_CONSISTENCY_LEVEL )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:232:12: ^( NODE_HELP NODE_CONSISTENCY_LEVEL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NODE_CONSISTENCY_LEVEL, "NODE_CONSISTENCY_LEVEL"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 29 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:233:7: HELP
                    {
                    HELP110=(Token)match(input,HELP,FOLLOW_HELP_in_helpStatement1525); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HELP.add(HELP110);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 234:9: -> ^( NODE_HELP )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:234:12: ^( NODE_HELP )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 30 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:235:7: '?'
                    {
                    char_literal111=(Token)match(input,106,FOLLOW_106_in_helpStatement1548); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_106.add(char_literal111);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 236:9: -> ^( NODE_HELP )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:236:12: ^( NODE_HELP )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_HELP, "NODE_HELP"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "helpStatement"

    public static class exitStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exitStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:239:1: exitStatement : ( QUIT -> ^( NODE_EXIT ) | EXIT -> ^( NODE_EXIT ) );
    public final CliParser.exitStatement_return exitStatement() throws RecognitionException {
        CliParser.exitStatement_return retval = new CliParser.exitStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUIT112=null;
        Token EXIT113=null;

        CommonTree QUIT112_tree=null;
        CommonTree EXIT113_tree=null;
        RewriteRuleTokenStream stream_EXIT=new RewriteRuleTokenStream(adaptor,"token EXIT");
        RewriteRuleTokenStream stream_QUIT=new RewriteRuleTokenStream(adaptor,"token QUIT");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:240:5: ( QUIT -> ^( NODE_EXIT ) | EXIT -> ^( NODE_EXIT ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==QUIT) ) {
                alt7=1;
            }
            else if ( (LA7_0==EXIT) ) {
                alt7=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:240:7: QUIT
                    {
                    QUIT112=(Token)match(input,QUIT,FOLLOW_QUIT_in_exitStatement1583); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUIT.add(QUIT112);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 240:12: -> ^( NODE_EXIT )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:240:15: ^( NODE_EXIT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_EXIT, "NODE_EXIT"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:241:7: EXIT
                    {
                    EXIT113=(Token)match(input,EXIT,FOLLOW_EXIT_in_exitStatement1597); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EXIT.add(EXIT113);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 241:12: -> ^( NODE_EXIT )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:241:15: ^( NODE_EXIT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_EXIT, "NODE_EXIT"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exitStatement"

    public static class getStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "getStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:244:1: getStatement : ( GET columnFamilyExpr ( 'AS' typeIdentifier )? ( 'LIMIT' limit= IntegerPositiveLiteral )? -> ^( NODE_THRIFT_GET columnFamilyExpr ( ^( CONVERT_TO_TYPE typeIdentifier ) )? ( ^( NODE_LIMIT $limit) )? ) | GET columnFamily 'WHERE' getCondition ( 'AND' getCondition )* ( 'LIMIT' limit= IntegerPositiveLiteral )? -> ^( NODE_THRIFT_GET_WITH_CONDITIONS columnFamily ^( CONDITIONS ( getCondition )+ ) ( ^( NODE_LIMIT $limit) )? ) );
    public final CliParser.getStatement_return getStatement() throws RecognitionException {
        CliParser.getStatement_return retval = new CliParser.getStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token limit=null;
        Token GET114=null;
        Token string_literal116=null;
        Token string_literal118=null;
        Token GET119=null;
        Token string_literal121=null;
        Token string_literal123=null;
        Token string_literal125=null;
        CliParser.columnFamilyExpr_return columnFamilyExpr115 = null;

        CliParser.typeIdentifier_return typeIdentifier117 = null;

        CliParser.columnFamily_return columnFamily120 = null;

        CliParser.getCondition_return getCondition122 = null;

        CliParser.getCondition_return getCondition124 = null;


        CommonTree limit_tree=null;
        CommonTree GET114_tree=null;
        CommonTree string_literal116_tree=null;
        CommonTree string_literal118_tree=null;
        CommonTree GET119_tree=null;
        CommonTree string_literal121_tree=null;
        CommonTree string_literal123_tree=null;
        CommonTree string_literal125_tree=null;
        RewriteRuleTokenStream stream_IntegerPositiveLiteral=new RewriteRuleTokenStream(adaptor,"token IntegerPositiveLiteral");
        RewriteRuleTokenStream stream_GET=new RewriteRuleTokenStream(adaptor,"token GET");
        RewriteRuleTokenStream stream_108=new RewriteRuleTokenStream(adaptor,"token 108");
        RewriteRuleTokenStream stream_107=new RewriteRuleTokenStream(adaptor,"token 107");
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleTokenStream stream_LIMIT=new RewriteRuleTokenStream(adaptor,"token LIMIT");
        RewriteRuleSubtreeStream stream_typeIdentifier=new RewriteRuleSubtreeStream(adaptor,"rule typeIdentifier");
        RewriteRuleSubtreeStream stream_getCondition=new RewriteRuleSubtreeStream(adaptor,"rule getCondition");
        RewriteRuleSubtreeStream stream_columnFamilyExpr=new RewriteRuleSubtreeStream(adaptor,"rule columnFamilyExpr");
        RewriteRuleSubtreeStream stream_columnFamily=new RewriteRuleSubtreeStream(adaptor,"rule columnFamily");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:245:5: ( GET columnFamilyExpr ( 'AS' typeIdentifier )? ( 'LIMIT' limit= IntegerPositiveLiteral )? -> ^( NODE_THRIFT_GET columnFamilyExpr ( ^( CONVERT_TO_TYPE typeIdentifier ) )? ( ^( NODE_LIMIT $limit) )? ) | GET columnFamily 'WHERE' getCondition ( 'AND' getCondition )* ( 'LIMIT' limit= IntegerPositiveLiteral )? -> ^( NODE_THRIFT_GET_WITH_CONDITIONS columnFamily ^( CONDITIONS ( getCondition )+ ) ( ^( NODE_LIMIT $limit) )? ) )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==GET) ) {
                int LA12_1 = input.LA(2);

                if ( ((LA12_1>=IntegerPositiveLiteral && LA12_1<=StringLiteral)||LA12_1==IntegerNegativeLiteral) ) {
                    int LA12_2 = input.LA(3);

                    if ( (LA12_2==115) ) {
                        alt12=1;
                    }
                    else if ( (LA12_2==108) ) {
                        alt12=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 2, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:245:7: GET columnFamilyExpr ( 'AS' typeIdentifier )? ( 'LIMIT' limit= IntegerPositiveLiteral )?
                    {
                    GET114=(Token)match(input,GET,FOLLOW_GET_in_getStatement1620); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_GET.add(GET114);

                    pushFollow(FOLLOW_columnFamilyExpr_in_getStatement1622);
                    columnFamilyExpr115=columnFamilyExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnFamilyExpr.add(columnFamilyExpr115.getTree());
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:245:28: ( 'AS' typeIdentifier )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==107) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:245:29: 'AS' typeIdentifier
                            {
                            string_literal116=(Token)match(input,107,FOLLOW_107_in_getStatement1625); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_107.add(string_literal116);

                            pushFollow(FOLLOW_typeIdentifier_in_getStatement1627);
                            typeIdentifier117=typeIdentifier();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_typeIdentifier.add(typeIdentifier117.getTree());

                            }
                            break;

                    }

                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:245:51: ( 'LIMIT' limit= IntegerPositiveLiteral )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==LIMIT) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:245:52: 'LIMIT' limit= IntegerPositiveLiteral
                            {
                            string_literal118=(Token)match(input,LIMIT,FOLLOW_LIMIT_in_getStatement1632); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_LIMIT.add(string_literal118);

                            limit=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_getStatement1636); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_IntegerPositiveLiteral.add(limit);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: limit, columnFamilyExpr, typeIdentifier
                    // token labels: limit
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_limit=new RewriteRuleTokenStream(adaptor,"token limit",limit);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 246:9: -> ^( NODE_THRIFT_GET columnFamilyExpr ( ^( CONVERT_TO_TYPE typeIdentifier ) )? ( ^( NODE_LIMIT $limit) )? )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:246:12: ^( NODE_THRIFT_GET columnFamilyExpr ( ^( CONVERT_TO_TYPE typeIdentifier ) )? ( ^( NODE_LIMIT $limit) )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_THRIFT_GET, "NODE_THRIFT_GET"), root_1);

                        adaptor.addChild(root_1, stream_columnFamilyExpr.nextTree());
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:246:47: ( ^( CONVERT_TO_TYPE typeIdentifier ) )?
                        if ( stream_typeIdentifier.hasNext() ) {
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:246:49: ^( CONVERT_TO_TYPE typeIdentifier )
                            {
                            CommonTree root_2 = (CommonTree)adaptor.nil();
                            root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONVERT_TO_TYPE, "CONVERT_TO_TYPE"), root_2);

                            adaptor.addChild(root_2, stream_typeIdentifier.nextTree());

                            adaptor.addChild(root_1, root_2);
                            }

                        }
                        stream_typeIdentifier.reset();
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:246:86: ( ^( NODE_LIMIT $limit) )?
                        if ( stream_limit.hasNext() ) {
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:246:86: ^( NODE_LIMIT $limit)
                            {
                            CommonTree root_2 = (CommonTree)adaptor.nil();
                            root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_LIMIT, "NODE_LIMIT"), root_2);

                            adaptor.addChild(root_2, stream_limit.nextNode());

                            adaptor.addChild(root_1, root_2);
                            }

                        }
                        stream_limit.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:247:7: GET columnFamily 'WHERE' getCondition ( 'AND' getCondition )* ( 'LIMIT' limit= IntegerPositiveLiteral )?
                    {
                    GET119=(Token)match(input,GET,FOLLOW_GET_in_getStatement1681); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_GET.add(GET119);

                    pushFollow(FOLLOW_columnFamily_in_getStatement1683);
                    columnFamily120=columnFamily();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnFamily.add(columnFamily120.getTree());
                    string_literal121=(Token)match(input,108,FOLLOW_108_in_getStatement1685); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_108.add(string_literal121);

                    pushFollow(FOLLOW_getCondition_in_getStatement1687);
                    getCondition122=getCondition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_getCondition.add(getCondition122.getTree());
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:247:45: ( 'AND' getCondition )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0==AND) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:247:46: 'AND' getCondition
                    	    {
                    	    string_literal123=(Token)match(input,AND,FOLLOW_AND_in_getStatement1690); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_AND.add(string_literal123);

                    	    pushFollow(FOLLOW_getCondition_in_getStatement1692);
                    	    getCondition124=getCondition();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_getCondition.add(getCondition124.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);

                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:247:67: ( 'LIMIT' limit= IntegerPositiveLiteral )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==LIMIT) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:247:68: 'LIMIT' limit= IntegerPositiveLiteral
                            {
                            string_literal125=(Token)match(input,LIMIT,FOLLOW_LIMIT_in_getStatement1697); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_LIMIT.add(string_literal125);

                            limit=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_getStatement1701); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_IntegerPositiveLiteral.add(limit);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: getCondition, limit, columnFamily
                    // token labels: limit
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_limit=new RewriteRuleTokenStream(adaptor,"token limit",limit);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 248:9: -> ^( NODE_THRIFT_GET_WITH_CONDITIONS columnFamily ^( CONDITIONS ( getCondition )+ ) ( ^( NODE_LIMIT $limit) )? )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:248:12: ^( NODE_THRIFT_GET_WITH_CONDITIONS columnFamily ^( CONDITIONS ( getCondition )+ ) ( ^( NODE_LIMIT $limit) )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_THRIFT_GET_WITH_CONDITIONS, "NODE_THRIFT_GET_WITH_CONDITIONS"), root_1);

                        adaptor.addChild(root_1, stream_columnFamily.nextTree());
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:248:59: ^( CONDITIONS ( getCondition )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDITIONS, "CONDITIONS"), root_2);

                        if ( !(stream_getCondition.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_getCondition.hasNext() ) {
                            adaptor.addChild(root_2, stream_getCondition.nextTree());

                        }
                        stream_getCondition.reset();

                        adaptor.addChild(root_1, root_2);
                        }
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:248:87: ( ^( NODE_LIMIT $limit) )?
                        if ( stream_limit.hasNext() ) {
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:248:87: ^( NODE_LIMIT $limit)
                            {
                            CommonTree root_2 = (CommonTree)adaptor.nil();
                            root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_LIMIT, "NODE_LIMIT"), root_2);

                            adaptor.addChild(root_2, stream_limit.nextNode());

                            adaptor.addChild(root_1, root_2);
                            }

                        }
                        stream_limit.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "getStatement"

    public static class getCondition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "getCondition"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:251:1: getCondition : columnOrSuperColumn operator value -> ^( CONDITION operator columnOrSuperColumn value ) ;
    public final CliParser.getCondition_return getCondition() throws RecognitionException {
        CliParser.getCondition_return retval = new CliParser.getCondition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.columnOrSuperColumn_return columnOrSuperColumn126 = null;

        CliParser.operator_return operator127 = null;

        CliParser.value_return value128 = null;


        RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
        RewriteRuleSubtreeStream stream_columnOrSuperColumn=new RewriteRuleSubtreeStream(adaptor,"rule columnOrSuperColumn");
        RewriteRuleSubtreeStream stream_operator=new RewriteRuleSubtreeStream(adaptor,"rule operator");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:252:5: ( columnOrSuperColumn operator value -> ^( CONDITION operator columnOrSuperColumn value ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:252:7: columnOrSuperColumn operator value
            {
            pushFollow(FOLLOW_columnOrSuperColumn_in_getCondition1752);
            columnOrSuperColumn126=columnOrSuperColumn();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnOrSuperColumn.add(columnOrSuperColumn126.getTree());
            pushFollow(FOLLOW_operator_in_getCondition1754);
            operator127=operator();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_operator.add(operator127.getTree());
            pushFollow(FOLLOW_value_in_getCondition1756);
            value128=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_value.add(value128.getTree());


            // AST REWRITE
            // elements: operator, value, columnOrSuperColumn
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 253:9: -> ^( CONDITION operator columnOrSuperColumn value )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:253:12: ^( CONDITION operator columnOrSuperColumn value )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDITION, "CONDITION"), root_1);

                adaptor.addChild(root_1, stream_operator.nextTree());
                adaptor.addChild(root_1, stream_columnOrSuperColumn.nextTree());
                adaptor.addChild(root_1, stream_value.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "getCondition"

    public static class operator_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "operator"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:256:1: operator : ( '=' | '>' | '<' | '>=' | '<=' );
    public final CliParser.operator_return operator() throws RecognitionException {
        CliParser.operator_return retval = new CliParser.operator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set129=null;

        CommonTree set129_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:257:5: ( '=' | '>' | '<' | '>=' | '<=' )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set129=(Token)input.LT(1);
            if ( (input.LA(1)>=109 && input.LA(1)<=113) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set129));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "operator"

    public static class typeIdentifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "typeIdentifier"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:260:1: typeIdentifier : ( Identifier | StringLiteral | IntegerPositiveLiteral );
    public final CliParser.typeIdentifier_return typeIdentifier() throws RecognitionException {
        CliParser.typeIdentifier_return retval = new CliParser.typeIdentifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set130=null;

        CommonTree set130_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:261:5: ( Identifier | StringLiteral | IntegerPositiveLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set130=(Token)input.LT(1);
            if ( (input.LA(1)>=IntegerPositiveLiteral && input.LA(1)<=StringLiteral) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set130));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "typeIdentifier"

    public static class setStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "setStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:264:1: setStatement : SET columnFamilyExpr '=' objectValue= value ( WITH TTL '=' ttlValue= IntegerPositiveLiteral )? -> ^( NODE_THRIFT_SET columnFamilyExpr $objectValue ( $ttlValue)? ) ;
    public final CliParser.setStatement_return setStatement() throws RecognitionException {
        CliParser.setStatement_return retval = new CliParser.setStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ttlValue=null;
        Token SET131=null;
        Token char_literal133=null;
        Token WITH134=null;
        Token TTL135=null;
        Token char_literal136=null;
        CliParser.value_return objectValue = null;

        CliParser.columnFamilyExpr_return columnFamilyExpr132 = null;


        CommonTree ttlValue_tree=null;
        CommonTree SET131_tree=null;
        CommonTree char_literal133_tree=null;
        CommonTree WITH134_tree=null;
        CommonTree TTL135_tree=null;
        CommonTree char_literal136_tree=null;
        RewriteRuleTokenStream stream_IntegerPositiveLiteral=new RewriteRuleTokenStream(adaptor,"token IntegerPositiveLiteral");
        RewriteRuleTokenStream stream_SET=new RewriteRuleTokenStream(adaptor,"token SET");
        RewriteRuleTokenStream stream_109=new RewriteRuleTokenStream(adaptor,"token 109");
        RewriteRuleTokenStream stream_WITH=new RewriteRuleTokenStream(adaptor,"token WITH");
        RewriteRuleTokenStream stream_TTL=new RewriteRuleTokenStream(adaptor,"token TTL");
        RewriteRuleSubtreeStream stream_columnFamilyExpr=new RewriteRuleSubtreeStream(adaptor,"rule columnFamilyExpr");
        RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:265:5: ( SET columnFamilyExpr '=' objectValue= value ( WITH TTL '=' ttlValue= IntegerPositiveLiteral )? -> ^( NODE_THRIFT_SET columnFamilyExpr $objectValue ( $ttlValue)? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:265:7: SET columnFamilyExpr '=' objectValue= value ( WITH TTL '=' ttlValue= IntegerPositiveLiteral )?
            {
            SET131=(Token)match(input,SET,FOLLOW_SET_in_setStatement1852); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SET.add(SET131);

            pushFollow(FOLLOW_columnFamilyExpr_in_setStatement1854);
            columnFamilyExpr132=columnFamilyExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamilyExpr.add(columnFamilyExpr132.getTree());
            char_literal133=(Token)match(input,109,FOLLOW_109_in_setStatement1856); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_109.add(char_literal133);

            pushFollow(FOLLOW_value_in_setStatement1860);
            objectValue=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_value.add(objectValue.getTree());
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:265:50: ( WITH TTL '=' ttlValue= IntegerPositiveLiteral )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==WITH) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:265:51: WITH TTL '=' ttlValue= IntegerPositiveLiteral
                    {
                    WITH134=(Token)match(input,WITH,FOLLOW_WITH_in_setStatement1863); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_WITH.add(WITH134);

                    TTL135=(Token)match(input,TTL,FOLLOW_TTL_in_setStatement1865); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TTL.add(TTL135);

                    char_literal136=(Token)match(input,109,FOLLOW_109_in_setStatement1867); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_109.add(char_literal136);

                    ttlValue=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_setStatement1871); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IntegerPositiveLiteral.add(ttlValue);


                    }
                    break;

            }



            // AST REWRITE
            // elements: ttlValue, objectValue, columnFamilyExpr
            // token labels: ttlValue
            // rule labels: retval, objectValue
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleTokenStream stream_ttlValue=new RewriteRuleTokenStream(adaptor,"token ttlValue",ttlValue);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_objectValue=new RewriteRuleSubtreeStream(adaptor,"rule objectValue",objectValue!=null?objectValue.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 266:9: -> ^( NODE_THRIFT_SET columnFamilyExpr $objectValue ( $ttlValue)? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:266:12: ^( NODE_THRIFT_SET columnFamilyExpr $objectValue ( $ttlValue)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_THRIFT_SET, "NODE_THRIFT_SET"), root_1);

                adaptor.addChild(root_1, stream_columnFamilyExpr.nextTree());
                adaptor.addChild(root_1, stream_objectValue.nextTree());
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:266:60: ( $ttlValue)?
                if ( stream_ttlValue.hasNext() ) {
                    adaptor.addChild(root_1, stream_ttlValue.nextNode());

                }
                stream_ttlValue.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "setStatement"

    public static class incrStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "incrStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:269:1: incrStatement : ( INCR columnFamilyExpr ( BY byValue= incrementValue )? -> ^( NODE_THRIFT_INCR columnFamilyExpr ( $byValue)? ) | DECR columnFamilyExpr ( BY byValue= incrementValue )? -> ^( NODE_THRIFT_DECR columnFamilyExpr ( $byValue)? ) );
    public final CliParser.incrStatement_return incrStatement() throws RecognitionException {
        CliParser.incrStatement_return retval = new CliParser.incrStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token INCR137=null;
        Token BY139=null;
        Token DECR140=null;
        Token BY142=null;
        CliParser.incrementValue_return byValue = null;

        CliParser.columnFamilyExpr_return columnFamilyExpr138 = null;

        CliParser.columnFamilyExpr_return columnFamilyExpr141 = null;


        CommonTree INCR137_tree=null;
        CommonTree BY139_tree=null;
        CommonTree DECR140_tree=null;
        CommonTree BY142_tree=null;
        RewriteRuleTokenStream stream_DECR=new RewriteRuleTokenStream(adaptor,"token DECR");
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleTokenStream stream_INCR=new RewriteRuleTokenStream(adaptor,"token INCR");
        RewriteRuleSubtreeStream stream_columnFamilyExpr=new RewriteRuleSubtreeStream(adaptor,"rule columnFamilyExpr");
        RewriteRuleSubtreeStream stream_incrementValue=new RewriteRuleSubtreeStream(adaptor,"rule incrementValue");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:270:5: ( INCR columnFamilyExpr ( BY byValue= incrementValue )? -> ^( NODE_THRIFT_INCR columnFamilyExpr ( $byValue)? ) | DECR columnFamilyExpr ( BY byValue= incrementValue )? -> ^( NODE_THRIFT_DECR columnFamilyExpr ( $byValue)? ) )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==INCR) ) {
                alt16=1;
            }
            else if ( (LA16_0==DECR) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:270:7: INCR columnFamilyExpr ( BY byValue= incrementValue )?
                    {
                    INCR137=(Token)match(input,INCR,FOLLOW_INCR_in_incrStatement1917); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INCR.add(INCR137);

                    pushFollow(FOLLOW_columnFamilyExpr_in_incrStatement1919);
                    columnFamilyExpr138=columnFamilyExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnFamilyExpr.add(columnFamilyExpr138.getTree());
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:270:29: ( BY byValue= incrementValue )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==BY) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:270:30: BY byValue= incrementValue
                            {
                            BY139=(Token)match(input,BY,FOLLOW_BY_in_incrStatement1922); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_BY.add(BY139);

                            pushFollow(FOLLOW_incrementValue_in_incrStatement1926);
                            byValue=incrementValue();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_incrementValue.add(byValue.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: byValue, columnFamilyExpr
                    // token labels: 
                    // rule labels: retval, byValue
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_byValue=new RewriteRuleSubtreeStream(adaptor,"rule byValue",byValue!=null?byValue.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 271:9: -> ^( NODE_THRIFT_INCR columnFamilyExpr ( $byValue)? )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:271:12: ^( NODE_THRIFT_INCR columnFamilyExpr ( $byValue)? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_THRIFT_INCR, "NODE_THRIFT_INCR"), root_1);

                        adaptor.addChild(root_1, stream_columnFamilyExpr.nextTree());
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:271:48: ( $byValue)?
                        if ( stream_byValue.hasNext() ) {
                            adaptor.addChild(root_1, stream_byValue.nextTree());

                        }
                        stream_byValue.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:272:7: DECR columnFamilyExpr ( BY byValue= incrementValue )?
                    {
                    DECR140=(Token)match(input,DECR,FOLLOW_DECR_in_incrStatement1960); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DECR.add(DECR140);

                    pushFollow(FOLLOW_columnFamilyExpr_in_incrStatement1962);
                    columnFamilyExpr141=columnFamilyExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnFamilyExpr.add(columnFamilyExpr141.getTree());
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:272:29: ( BY byValue= incrementValue )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==BY) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:272:30: BY byValue= incrementValue
                            {
                            BY142=(Token)match(input,BY,FOLLOW_BY_in_incrStatement1965); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_BY.add(BY142);

                            pushFollow(FOLLOW_incrementValue_in_incrStatement1969);
                            byValue=incrementValue();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_incrementValue.add(byValue.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: columnFamilyExpr, byValue
                    // token labels: 
                    // rule labels: retval, byValue
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_byValue=new RewriteRuleSubtreeStream(adaptor,"rule byValue",byValue!=null?byValue.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 273:9: -> ^( NODE_THRIFT_DECR columnFamilyExpr ( $byValue)? )
                    {
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:273:12: ^( NODE_THRIFT_DECR columnFamilyExpr ( $byValue)? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_THRIFT_DECR, "NODE_THRIFT_DECR"), root_1);

                        adaptor.addChild(root_1, stream_columnFamilyExpr.nextTree());
                        // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:273:48: ( $byValue)?
                        if ( stream_byValue.hasNext() ) {
                            adaptor.addChild(root_1, stream_byValue.nextTree());

                        }
                        stream_byValue.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "incrStatement"

    public static class countStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "countStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:276:1: countStatement : COUNT columnFamilyExpr -> ^( NODE_THRIFT_COUNT columnFamilyExpr ) ;
    public final CliParser.countStatement_return countStatement() throws RecognitionException {
        CliParser.countStatement_return retval = new CliParser.countStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COUNT143=null;
        CliParser.columnFamilyExpr_return columnFamilyExpr144 = null;


        CommonTree COUNT143_tree=null;
        RewriteRuleTokenStream stream_COUNT=new RewriteRuleTokenStream(adaptor,"token COUNT");
        RewriteRuleSubtreeStream stream_columnFamilyExpr=new RewriteRuleSubtreeStream(adaptor,"rule columnFamilyExpr");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:277:5: ( COUNT columnFamilyExpr -> ^( NODE_THRIFT_COUNT columnFamilyExpr ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:277:7: COUNT columnFamilyExpr
            {
            COUNT143=(Token)match(input,COUNT,FOLLOW_COUNT_in_countStatement2012); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COUNT.add(COUNT143);

            pushFollow(FOLLOW_columnFamilyExpr_in_countStatement2014);
            columnFamilyExpr144=columnFamilyExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamilyExpr.add(columnFamilyExpr144.getTree());


            // AST REWRITE
            // elements: columnFamilyExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 278:9: -> ^( NODE_THRIFT_COUNT columnFamilyExpr )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:278:12: ^( NODE_THRIFT_COUNT columnFamilyExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_THRIFT_COUNT, "NODE_THRIFT_COUNT"), root_1);

                adaptor.addChild(root_1, stream_columnFamilyExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "countStatement"

    public static class delStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "delStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:281:1: delStatement : DEL columnFamilyExpr -> ^( NODE_THRIFT_DEL columnFamilyExpr ) ;
    public final CliParser.delStatement_return delStatement() throws RecognitionException {
        CliParser.delStatement_return retval = new CliParser.delStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DEL145=null;
        CliParser.columnFamilyExpr_return columnFamilyExpr146 = null;


        CommonTree DEL145_tree=null;
        RewriteRuleTokenStream stream_DEL=new RewriteRuleTokenStream(adaptor,"token DEL");
        RewriteRuleSubtreeStream stream_columnFamilyExpr=new RewriteRuleSubtreeStream(adaptor,"rule columnFamilyExpr");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:282:5: ( DEL columnFamilyExpr -> ^( NODE_THRIFT_DEL columnFamilyExpr ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:282:7: DEL columnFamilyExpr
            {
            DEL145=(Token)match(input,DEL,FOLLOW_DEL_in_delStatement2048); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DEL.add(DEL145);

            pushFollow(FOLLOW_columnFamilyExpr_in_delStatement2050);
            columnFamilyExpr146=columnFamilyExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamilyExpr.add(columnFamilyExpr146.getTree());


            // AST REWRITE
            // elements: columnFamilyExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 283:9: -> ^( NODE_THRIFT_DEL columnFamilyExpr )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:283:12: ^( NODE_THRIFT_DEL columnFamilyExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_THRIFT_DEL, "NODE_THRIFT_DEL"), root_1);

                adaptor.addChild(root_1, stream_columnFamilyExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "delStatement"

    public static class showStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "showStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:286:1: showStatement : ( showClusterName | showVersion | showKeyspaces | showSchema );
    public final CliParser.showStatement_return showStatement() throws RecognitionException {
        CliParser.showStatement_return retval = new CliParser.showStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.showClusterName_return showClusterName147 = null;

        CliParser.showVersion_return showVersion148 = null;

        CliParser.showKeyspaces_return showKeyspaces149 = null;

        CliParser.showSchema_return showSchema150 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:287:5: ( showClusterName | showVersion | showKeyspaces | showSchema )
            int alt17=4;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==SHOW) ) {
                switch ( input.LA(2) ) {
                case 105:
                    {
                    alt17=1;
                    }
                    break;
                case API_VERSION:
                    {
                    alt17=2;
                    }
                    break;
                case KEYSPACES:
                    {
                    alt17=3;
                    }
                    break;
                case SCHEMA:
                    {
                    alt17=4;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

                    throw nvae;
                }

            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:287:7: showClusterName
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_showClusterName_in_showStatement2084);
                    showClusterName147=showClusterName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, showClusterName147.getTree());

                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:288:7: showVersion
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_showVersion_in_showStatement2092);
                    showVersion148=showVersion();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, showVersion148.getTree());

                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:289:7: showKeyspaces
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_showKeyspaces_in_showStatement2100);
                    showKeyspaces149=showKeyspaces();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, showKeyspaces149.getTree());

                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:290:7: showSchema
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_showSchema_in_showStatement2108);
                    showSchema150=showSchema();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, showSchema150.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "showStatement"

    public static class listStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:293:1: listStatement : LIST columnFamily ( keyRangeExpr )? ( 'LIMIT' limit= IntegerPositiveLiteral )? -> ^( NODE_LIST columnFamily ( keyRangeExpr )? ( ^( NODE_LIMIT $limit) )? ) ;
    public final CliParser.listStatement_return listStatement() throws RecognitionException {
        CliParser.listStatement_return retval = new CliParser.listStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token limit=null;
        Token LIST151=null;
        Token string_literal154=null;
        CliParser.columnFamily_return columnFamily152 = null;

        CliParser.keyRangeExpr_return keyRangeExpr153 = null;


        CommonTree limit_tree=null;
        CommonTree LIST151_tree=null;
        CommonTree string_literal154_tree=null;
        RewriteRuleTokenStream stream_IntegerPositiveLiteral=new RewriteRuleTokenStream(adaptor,"token IntegerPositiveLiteral");
        RewriteRuleTokenStream stream_LIST=new RewriteRuleTokenStream(adaptor,"token LIST");
        RewriteRuleTokenStream stream_LIMIT=new RewriteRuleTokenStream(adaptor,"token LIMIT");
        RewriteRuleSubtreeStream stream_columnFamily=new RewriteRuleSubtreeStream(adaptor,"rule columnFamily");
        RewriteRuleSubtreeStream stream_keyRangeExpr=new RewriteRuleSubtreeStream(adaptor,"rule keyRangeExpr");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:294:5: ( LIST columnFamily ( keyRangeExpr )? ( 'LIMIT' limit= IntegerPositiveLiteral )? -> ^( NODE_LIST columnFamily ( keyRangeExpr )? ( ^( NODE_LIMIT $limit) )? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:294:7: LIST columnFamily ( keyRangeExpr )? ( 'LIMIT' limit= IntegerPositiveLiteral )?
            {
            LIST151=(Token)match(input,LIST,FOLLOW_LIST_in_listStatement2125); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LIST.add(LIST151);

            pushFollow(FOLLOW_columnFamily_in_listStatement2127);
            columnFamily152=columnFamily();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamily.add(columnFamily152.getTree());
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:294:25: ( keyRangeExpr )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==115) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:0:0: keyRangeExpr
                    {
                    pushFollow(FOLLOW_keyRangeExpr_in_listStatement2129);
                    keyRangeExpr153=keyRangeExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyRangeExpr.add(keyRangeExpr153.getTree());

                    }
                    break;

            }

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:294:39: ( 'LIMIT' limit= IntegerPositiveLiteral )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==LIMIT) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:294:40: 'LIMIT' limit= IntegerPositiveLiteral
                    {
                    string_literal154=(Token)match(input,LIMIT,FOLLOW_LIMIT_in_listStatement2133); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LIMIT.add(string_literal154);

                    limit=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_listStatement2137); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IntegerPositiveLiteral.add(limit);


                    }
                    break;

            }



            // AST REWRITE
            // elements: columnFamily, limit, keyRangeExpr
            // token labels: limit
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleTokenStream stream_limit=new RewriteRuleTokenStream(adaptor,"token limit",limit);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 295:9: -> ^( NODE_LIST columnFamily ( keyRangeExpr )? ( ^( NODE_LIMIT $limit) )? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:295:12: ^( NODE_LIST columnFamily ( keyRangeExpr )? ( ^( NODE_LIMIT $limit) )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_LIST, "NODE_LIST"), root_1);

                adaptor.addChild(root_1, stream_columnFamily.nextTree());
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:295:37: ( keyRangeExpr )?
                if ( stream_keyRangeExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_keyRangeExpr.nextTree());

                }
                stream_keyRangeExpr.reset();
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:295:51: ( ^( NODE_LIMIT $limit) )?
                if ( stream_limit.hasNext() ) {
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:295:51: ^( NODE_LIMIT $limit)
                    {
                    CommonTree root_2 = (CommonTree)adaptor.nil();
                    root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_LIMIT, "NODE_LIMIT"), root_2);

                    adaptor.addChild(root_2, stream_limit.nextNode());

                    adaptor.addChild(root_1, root_2);
                    }

                }
                stream_limit.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listStatement"

    public static class truncateStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "truncateStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:298:1: truncateStatement : TRUNCATE columnFamily -> ^( NODE_TRUNCATE columnFamily ) ;
    public final CliParser.truncateStatement_return truncateStatement() throws RecognitionException {
        CliParser.truncateStatement_return retval = new CliParser.truncateStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TRUNCATE155=null;
        CliParser.columnFamily_return columnFamily156 = null;


        CommonTree TRUNCATE155_tree=null;
        RewriteRuleTokenStream stream_TRUNCATE=new RewriteRuleTokenStream(adaptor,"token TRUNCATE");
        RewriteRuleSubtreeStream stream_columnFamily=new RewriteRuleSubtreeStream(adaptor,"rule columnFamily");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:299:5: ( TRUNCATE columnFamily -> ^( NODE_TRUNCATE columnFamily ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:299:7: TRUNCATE columnFamily
            {
            TRUNCATE155=(Token)match(input,TRUNCATE,FOLLOW_TRUNCATE_in_truncateStatement2183); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TRUNCATE.add(TRUNCATE155);

            pushFollow(FOLLOW_columnFamily_in_truncateStatement2185);
            columnFamily156=columnFamily();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamily.add(columnFamily156.getTree());


            // AST REWRITE
            // elements: columnFamily
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 300:9: -> ^( NODE_TRUNCATE columnFamily )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:300:12: ^( NODE_TRUNCATE columnFamily )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_TRUNCATE, "NODE_TRUNCATE"), root_1);

                adaptor.addChild(root_1, stream_columnFamily.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "truncateStatement"

    public static class assumeStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assumeStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:303:1: assumeStatement : ASSUME columnFamily assumptionElement= Identifier 'AS' entityName -> ^( NODE_ASSUME columnFamily $assumptionElement entityName ) ;
    public final CliParser.assumeStatement_return assumeStatement() throws RecognitionException {
        CliParser.assumeStatement_return retval = new CliParser.assumeStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token assumptionElement=null;
        Token ASSUME157=null;
        Token string_literal159=null;
        CliParser.columnFamily_return columnFamily158 = null;

        CliParser.entityName_return entityName160 = null;


        CommonTree assumptionElement_tree=null;
        CommonTree ASSUME157_tree=null;
        CommonTree string_literal159_tree=null;
        RewriteRuleTokenStream stream_107=new RewriteRuleTokenStream(adaptor,"token 107");
        RewriteRuleTokenStream stream_ASSUME=new RewriteRuleTokenStream(adaptor,"token ASSUME");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleSubtreeStream stream_columnFamily=new RewriteRuleSubtreeStream(adaptor,"rule columnFamily");
        RewriteRuleSubtreeStream stream_entityName=new RewriteRuleSubtreeStream(adaptor,"rule entityName");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:304:5: ( ASSUME columnFamily assumptionElement= Identifier 'AS' entityName -> ^( NODE_ASSUME columnFamily $assumptionElement entityName ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:304:7: ASSUME columnFamily assumptionElement= Identifier 'AS' entityName
            {
            ASSUME157=(Token)match(input,ASSUME,FOLLOW_ASSUME_in_assumeStatement2218); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ASSUME.add(ASSUME157);

            pushFollow(FOLLOW_columnFamily_in_assumeStatement2220);
            columnFamily158=columnFamily();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamily.add(columnFamily158.getTree());
            assumptionElement=(Token)match(input,Identifier,FOLLOW_Identifier_in_assumeStatement2224); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_Identifier.add(assumptionElement);

            string_literal159=(Token)match(input,107,FOLLOW_107_in_assumeStatement2226); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_107.add(string_literal159);

            pushFollow(FOLLOW_entityName_in_assumeStatement2228);
            entityName160=entityName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_entityName.add(entityName160.getTree());


            // AST REWRITE
            // elements: entityName, assumptionElement, columnFamily
            // token labels: assumptionElement
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleTokenStream stream_assumptionElement=new RewriteRuleTokenStream(adaptor,"token assumptionElement",assumptionElement);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 305:9: -> ^( NODE_ASSUME columnFamily $assumptionElement entityName )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:305:12: ^( NODE_ASSUME columnFamily $assumptionElement entityName )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_ASSUME, "NODE_ASSUME"), root_1);

                adaptor.addChild(root_1, stream_columnFamily.nextTree());
                adaptor.addChild(root_1, stream_assumptionElement.nextNode());
                adaptor.addChild(root_1, stream_entityName.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assumeStatement"

    public static class consistencyLevelStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "consistencyLevelStatement"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:308:1: consistencyLevelStatement : CONSISTENCYLEVEL 'AS' defaultType= Identifier -> ^( NODE_CONSISTENCY_LEVEL $defaultType) ;
    public final CliParser.consistencyLevelStatement_return consistencyLevelStatement() throws RecognitionException {
        CliParser.consistencyLevelStatement_return retval = new CliParser.consistencyLevelStatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token defaultType=null;
        Token CONSISTENCYLEVEL161=null;
        Token string_literal162=null;

        CommonTree defaultType_tree=null;
        CommonTree CONSISTENCYLEVEL161_tree=null;
        CommonTree string_literal162_tree=null;
        RewriteRuleTokenStream stream_107=new RewriteRuleTokenStream(adaptor,"token 107");
        RewriteRuleTokenStream stream_CONSISTENCYLEVEL=new RewriteRuleTokenStream(adaptor,"token CONSISTENCYLEVEL");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:309:5: ( CONSISTENCYLEVEL 'AS' defaultType= Identifier -> ^( NODE_CONSISTENCY_LEVEL $defaultType) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:309:7: CONSISTENCYLEVEL 'AS' defaultType= Identifier
            {
            CONSISTENCYLEVEL161=(Token)match(input,CONSISTENCYLEVEL,FOLLOW_CONSISTENCYLEVEL_in_consistencyLevelStatement2266); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONSISTENCYLEVEL.add(CONSISTENCYLEVEL161);

            string_literal162=(Token)match(input,107,FOLLOW_107_in_consistencyLevelStatement2268); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_107.add(string_literal162);

            defaultType=(Token)match(input,Identifier,FOLLOW_Identifier_in_consistencyLevelStatement2272); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_Identifier.add(defaultType);



            // AST REWRITE
            // elements: defaultType
            // token labels: defaultType
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleTokenStream stream_defaultType=new RewriteRuleTokenStream(adaptor,"token defaultType",defaultType);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 310:9: -> ^( NODE_CONSISTENCY_LEVEL $defaultType)
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:310:12: ^( NODE_CONSISTENCY_LEVEL $defaultType)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_CONSISTENCY_LEVEL, "NODE_CONSISTENCY_LEVEL"), root_1);

                adaptor.addChild(root_1, stream_defaultType.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "consistencyLevelStatement"

    public static class showClusterName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "showClusterName"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:313:1: showClusterName : SHOW 'CLUSTER NAME' -> ^( NODE_SHOW_CLUSTER_NAME ) ;
    public final CliParser.showClusterName_return showClusterName() throws RecognitionException {
        CliParser.showClusterName_return retval = new CliParser.showClusterName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SHOW163=null;
        Token string_literal164=null;

        CommonTree SHOW163_tree=null;
        CommonTree string_literal164_tree=null;
        RewriteRuleTokenStream stream_105=new RewriteRuleTokenStream(adaptor,"token 105");
        RewriteRuleTokenStream stream_SHOW=new RewriteRuleTokenStream(adaptor,"token SHOW");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:314:5: ( SHOW 'CLUSTER NAME' -> ^( NODE_SHOW_CLUSTER_NAME ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:314:7: SHOW 'CLUSTER NAME'
            {
            SHOW163=(Token)match(input,SHOW,FOLLOW_SHOW_in_showClusterName2306); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SHOW.add(SHOW163);

            string_literal164=(Token)match(input,105,FOLLOW_105_in_showClusterName2308); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_105.add(string_literal164);



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 315:9: -> ^( NODE_SHOW_CLUSTER_NAME )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:315:12: ^( NODE_SHOW_CLUSTER_NAME )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_SHOW_CLUSTER_NAME, "NODE_SHOW_CLUSTER_NAME"), root_1);

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "showClusterName"

    public static class addKeyspace_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "addKeyspace"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:318:1: addKeyspace : CREATE KEYSPACE keyValuePairExpr -> ^( NODE_ADD_KEYSPACE keyValuePairExpr ) ;
    public final CliParser.addKeyspace_return addKeyspace() throws RecognitionException {
        CliParser.addKeyspace_return retval = new CliParser.addKeyspace_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CREATE165=null;
        Token KEYSPACE166=null;
        CliParser.keyValuePairExpr_return keyValuePairExpr167 = null;


        CommonTree CREATE165_tree=null;
        CommonTree KEYSPACE166_tree=null;
        RewriteRuleTokenStream stream_CREATE=new RewriteRuleTokenStream(adaptor,"token CREATE");
        RewriteRuleTokenStream stream_KEYSPACE=new RewriteRuleTokenStream(adaptor,"token KEYSPACE");
        RewriteRuleSubtreeStream stream_keyValuePairExpr=new RewriteRuleSubtreeStream(adaptor,"rule keyValuePairExpr");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:319:5: ( CREATE KEYSPACE keyValuePairExpr -> ^( NODE_ADD_KEYSPACE keyValuePairExpr ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:319:7: CREATE KEYSPACE keyValuePairExpr
            {
            CREATE165=(Token)match(input,CREATE,FOLLOW_CREATE_in_addKeyspace2339); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CREATE.add(CREATE165);

            KEYSPACE166=(Token)match(input,KEYSPACE,FOLLOW_KEYSPACE_in_addKeyspace2341); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_KEYSPACE.add(KEYSPACE166);

            pushFollow(FOLLOW_keyValuePairExpr_in_addKeyspace2343);
            keyValuePairExpr167=keyValuePairExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyValuePairExpr.add(keyValuePairExpr167.getTree());


            // AST REWRITE
            // elements: keyValuePairExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 320:9: -> ^( NODE_ADD_KEYSPACE keyValuePairExpr )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:320:12: ^( NODE_ADD_KEYSPACE keyValuePairExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_ADD_KEYSPACE, "NODE_ADD_KEYSPACE"), root_1);

                adaptor.addChild(root_1, stream_keyValuePairExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "addKeyspace"

    public static class addColumnFamily_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "addColumnFamily"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:323:1: addColumnFamily : CREATE COLUMN FAMILY keyValuePairExpr -> ^( NODE_ADD_COLUMN_FAMILY keyValuePairExpr ) ;
    public final CliParser.addColumnFamily_return addColumnFamily() throws RecognitionException {
        CliParser.addColumnFamily_return retval = new CliParser.addColumnFamily_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CREATE168=null;
        Token COLUMN169=null;
        Token FAMILY170=null;
        CliParser.keyValuePairExpr_return keyValuePairExpr171 = null;


        CommonTree CREATE168_tree=null;
        CommonTree COLUMN169_tree=null;
        CommonTree FAMILY170_tree=null;
        RewriteRuleTokenStream stream_FAMILY=new RewriteRuleTokenStream(adaptor,"token FAMILY");
        RewriteRuleTokenStream stream_CREATE=new RewriteRuleTokenStream(adaptor,"token CREATE");
        RewriteRuleTokenStream stream_COLUMN=new RewriteRuleTokenStream(adaptor,"token COLUMN");
        RewriteRuleSubtreeStream stream_keyValuePairExpr=new RewriteRuleSubtreeStream(adaptor,"rule keyValuePairExpr");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:324:5: ( CREATE COLUMN FAMILY keyValuePairExpr -> ^( NODE_ADD_COLUMN_FAMILY keyValuePairExpr ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:324:7: CREATE COLUMN FAMILY keyValuePairExpr
            {
            CREATE168=(Token)match(input,CREATE,FOLLOW_CREATE_in_addColumnFamily2377); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CREATE.add(CREATE168);

            COLUMN169=(Token)match(input,COLUMN,FOLLOW_COLUMN_in_addColumnFamily2379); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLUMN.add(COLUMN169);

            FAMILY170=(Token)match(input,FAMILY,FOLLOW_FAMILY_in_addColumnFamily2381); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FAMILY.add(FAMILY170);

            pushFollow(FOLLOW_keyValuePairExpr_in_addColumnFamily2383);
            keyValuePairExpr171=keyValuePairExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyValuePairExpr.add(keyValuePairExpr171.getTree());


            // AST REWRITE
            // elements: keyValuePairExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 325:9: -> ^( NODE_ADD_COLUMN_FAMILY keyValuePairExpr )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:325:12: ^( NODE_ADD_COLUMN_FAMILY keyValuePairExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_ADD_COLUMN_FAMILY, "NODE_ADD_COLUMN_FAMILY"), root_1);

                adaptor.addChild(root_1, stream_keyValuePairExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "addColumnFamily"

    public static class updateKeyspace_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "updateKeyspace"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:328:1: updateKeyspace : UPDATE KEYSPACE keyValuePairExpr -> ^( NODE_UPDATE_KEYSPACE keyValuePairExpr ) ;
    public final CliParser.updateKeyspace_return updateKeyspace() throws RecognitionException {
        CliParser.updateKeyspace_return retval = new CliParser.updateKeyspace_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token UPDATE172=null;
        Token KEYSPACE173=null;
        CliParser.keyValuePairExpr_return keyValuePairExpr174 = null;


        CommonTree UPDATE172_tree=null;
        CommonTree KEYSPACE173_tree=null;
        RewriteRuleTokenStream stream_UPDATE=new RewriteRuleTokenStream(adaptor,"token UPDATE");
        RewriteRuleTokenStream stream_KEYSPACE=new RewriteRuleTokenStream(adaptor,"token KEYSPACE");
        RewriteRuleSubtreeStream stream_keyValuePairExpr=new RewriteRuleSubtreeStream(adaptor,"rule keyValuePairExpr");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:329:5: ( UPDATE KEYSPACE keyValuePairExpr -> ^( NODE_UPDATE_KEYSPACE keyValuePairExpr ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:329:7: UPDATE KEYSPACE keyValuePairExpr
            {
            UPDATE172=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_updateKeyspace2417); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_UPDATE.add(UPDATE172);

            KEYSPACE173=(Token)match(input,KEYSPACE,FOLLOW_KEYSPACE_in_updateKeyspace2419); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_KEYSPACE.add(KEYSPACE173);

            pushFollow(FOLLOW_keyValuePairExpr_in_updateKeyspace2421);
            keyValuePairExpr174=keyValuePairExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyValuePairExpr.add(keyValuePairExpr174.getTree());


            // AST REWRITE
            // elements: keyValuePairExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 330:9: -> ^( NODE_UPDATE_KEYSPACE keyValuePairExpr )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:330:12: ^( NODE_UPDATE_KEYSPACE keyValuePairExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_UPDATE_KEYSPACE, "NODE_UPDATE_KEYSPACE"), root_1);

                adaptor.addChild(root_1, stream_keyValuePairExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "updateKeyspace"

    public static class updateColumnFamily_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "updateColumnFamily"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:333:1: updateColumnFamily : UPDATE COLUMN FAMILY keyValuePairExpr -> ^( NODE_UPDATE_COLUMN_FAMILY keyValuePairExpr ) ;
    public final CliParser.updateColumnFamily_return updateColumnFamily() throws RecognitionException {
        CliParser.updateColumnFamily_return retval = new CliParser.updateColumnFamily_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token UPDATE175=null;
        Token COLUMN176=null;
        Token FAMILY177=null;
        CliParser.keyValuePairExpr_return keyValuePairExpr178 = null;


        CommonTree UPDATE175_tree=null;
        CommonTree COLUMN176_tree=null;
        CommonTree FAMILY177_tree=null;
        RewriteRuleTokenStream stream_FAMILY=new RewriteRuleTokenStream(adaptor,"token FAMILY");
        RewriteRuleTokenStream stream_UPDATE=new RewriteRuleTokenStream(adaptor,"token UPDATE");
        RewriteRuleTokenStream stream_COLUMN=new RewriteRuleTokenStream(adaptor,"token COLUMN");
        RewriteRuleSubtreeStream stream_keyValuePairExpr=new RewriteRuleSubtreeStream(adaptor,"rule keyValuePairExpr");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:334:5: ( UPDATE COLUMN FAMILY keyValuePairExpr -> ^( NODE_UPDATE_COLUMN_FAMILY keyValuePairExpr ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:334:7: UPDATE COLUMN FAMILY keyValuePairExpr
            {
            UPDATE175=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_updateColumnFamily2454); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_UPDATE.add(UPDATE175);

            COLUMN176=(Token)match(input,COLUMN,FOLLOW_COLUMN_in_updateColumnFamily2456); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLUMN.add(COLUMN176);

            FAMILY177=(Token)match(input,FAMILY,FOLLOW_FAMILY_in_updateColumnFamily2458); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FAMILY.add(FAMILY177);

            pushFollow(FOLLOW_keyValuePairExpr_in_updateColumnFamily2460);
            keyValuePairExpr178=keyValuePairExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyValuePairExpr.add(keyValuePairExpr178.getTree());


            // AST REWRITE
            // elements: keyValuePairExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 335:9: -> ^( NODE_UPDATE_COLUMN_FAMILY keyValuePairExpr )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:335:12: ^( NODE_UPDATE_COLUMN_FAMILY keyValuePairExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_UPDATE_COLUMN_FAMILY, "NODE_UPDATE_COLUMN_FAMILY"), root_1);

                adaptor.addChild(root_1, stream_keyValuePairExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "updateColumnFamily"

    public static class delKeyspace_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "delKeyspace"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:338:1: delKeyspace : DROP KEYSPACE keyspace -> ^( NODE_DEL_KEYSPACE keyspace ) ;
    public final CliParser.delKeyspace_return delKeyspace() throws RecognitionException {
        CliParser.delKeyspace_return retval = new CliParser.delKeyspace_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DROP179=null;
        Token KEYSPACE180=null;
        CliParser.keyspace_return keyspace181 = null;


        CommonTree DROP179_tree=null;
        CommonTree KEYSPACE180_tree=null;
        RewriteRuleTokenStream stream_DROP=new RewriteRuleTokenStream(adaptor,"token DROP");
        RewriteRuleTokenStream stream_KEYSPACE=new RewriteRuleTokenStream(adaptor,"token KEYSPACE");
        RewriteRuleSubtreeStream stream_keyspace=new RewriteRuleSubtreeStream(adaptor,"rule keyspace");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:339:5: ( DROP KEYSPACE keyspace -> ^( NODE_DEL_KEYSPACE keyspace ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:339:7: DROP KEYSPACE keyspace
            {
            DROP179=(Token)match(input,DROP,FOLLOW_DROP_in_delKeyspace2493); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DROP.add(DROP179);

            KEYSPACE180=(Token)match(input,KEYSPACE,FOLLOW_KEYSPACE_in_delKeyspace2495); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_KEYSPACE.add(KEYSPACE180);

            pushFollow(FOLLOW_keyspace_in_delKeyspace2497);
            keyspace181=keyspace();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyspace.add(keyspace181.getTree());


            // AST REWRITE
            // elements: keyspace
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 340:9: -> ^( NODE_DEL_KEYSPACE keyspace )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:340:12: ^( NODE_DEL_KEYSPACE keyspace )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_DEL_KEYSPACE, "NODE_DEL_KEYSPACE"), root_1);

                adaptor.addChild(root_1, stream_keyspace.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "delKeyspace"

    public static class delColumnFamily_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "delColumnFamily"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:343:1: delColumnFamily : DROP COLUMN FAMILY columnFamily -> ^( NODE_DEL_COLUMN_FAMILY columnFamily ) ;
    public final CliParser.delColumnFamily_return delColumnFamily() throws RecognitionException {
        CliParser.delColumnFamily_return retval = new CliParser.delColumnFamily_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DROP182=null;
        Token COLUMN183=null;
        Token FAMILY184=null;
        CliParser.columnFamily_return columnFamily185 = null;


        CommonTree DROP182_tree=null;
        CommonTree COLUMN183_tree=null;
        CommonTree FAMILY184_tree=null;
        RewriteRuleTokenStream stream_FAMILY=new RewriteRuleTokenStream(adaptor,"token FAMILY");
        RewriteRuleTokenStream stream_DROP=new RewriteRuleTokenStream(adaptor,"token DROP");
        RewriteRuleTokenStream stream_COLUMN=new RewriteRuleTokenStream(adaptor,"token COLUMN");
        RewriteRuleSubtreeStream stream_columnFamily=new RewriteRuleSubtreeStream(adaptor,"rule columnFamily");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:344:5: ( DROP COLUMN FAMILY columnFamily -> ^( NODE_DEL_COLUMN_FAMILY columnFamily ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:344:7: DROP COLUMN FAMILY columnFamily
            {
            DROP182=(Token)match(input,DROP,FOLLOW_DROP_in_delColumnFamily2531); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DROP.add(DROP182);

            COLUMN183=(Token)match(input,COLUMN,FOLLOW_COLUMN_in_delColumnFamily2533); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLUMN.add(COLUMN183);

            FAMILY184=(Token)match(input,FAMILY,FOLLOW_FAMILY_in_delColumnFamily2535); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FAMILY.add(FAMILY184);

            pushFollow(FOLLOW_columnFamily_in_delColumnFamily2537);
            columnFamily185=columnFamily();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamily.add(columnFamily185.getTree());


            // AST REWRITE
            // elements: columnFamily
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 345:9: -> ^( NODE_DEL_COLUMN_FAMILY columnFamily )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:345:12: ^( NODE_DEL_COLUMN_FAMILY columnFamily )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_DEL_COLUMN_FAMILY, "NODE_DEL_COLUMN_FAMILY"), root_1);

                adaptor.addChild(root_1, stream_columnFamily.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "delColumnFamily"

    public static class dropIndex_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dropIndex"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:348:1: dropIndex : DROP INDEX ON columnFamily '.' columnName -> ^( NODE_DROP_INDEX columnFamily columnName ) ;
    public final CliParser.dropIndex_return dropIndex() throws RecognitionException {
        CliParser.dropIndex_return retval = new CliParser.dropIndex_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DROP186=null;
        Token INDEX187=null;
        Token ON188=null;
        Token char_literal190=null;
        CliParser.columnFamily_return columnFamily189 = null;

        CliParser.columnName_return columnName191 = null;


        CommonTree DROP186_tree=null;
        CommonTree INDEX187_tree=null;
        CommonTree ON188_tree=null;
        CommonTree char_literal190_tree=null;
        RewriteRuleTokenStream stream_INDEX=new RewriteRuleTokenStream(adaptor,"token INDEX");
        RewriteRuleTokenStream stream_ON=new RewriteRuleTokenStream(adaptor,"token ON");
        RewriteRuleTokenStream stream_114=new RewriteRuleTokenStream(adaptor,"token 114");
        RewriteRuleTokenStream stream_DROP=new RewriteRuleTokenStream(adaptor,"token DROP");
        RewriteRuleSubtreeStream stream_columnFamily=new RewriteRuleSubtreeStream(adaptor,"rule columnFamily");
        RewriteRuleSubtreeStream stream_columnName=new RewriteRuleSubtreeStream(adaptor,"rule columnName");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:349:5: ( DROP INDEX ON columnFamily '.' columnName -> ^( NODE_DROP_INDEX columnFamily columnName ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:349:7: DROP INDEX ON columnFamily '.' columnName
            {
            DROP186=(Token)match(input,DROP,FOLLOW_DROP_in_dropIndex2571); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DROP.add(DROP186);

            INDEX187=(Token)match(input,INDEX,FOLLOW_INDEX_in_dropIndex2573); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_INDEX.add(INDEX187);

            ON188=(Token)match(input,ON,FOLLOW_ON_in_dropIndex2575); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ON.add(ON188);

            pushFollow(FOLLOW_columnFamily_in_dropIndex2577);
            columnFamily189=columnFamily();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamily.add(columnFamily189.getTree());
            char_literal190=(Token)match(input,114,FOLLOW_114_in_dropIndex2579); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_114.add(char_literal190);

            pushFollow(FOLLOW_columnName_in_dropIndex2581);
            columnName191=columnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnName.add(columnName191.getTree());


            // AST REWRITE
            // elements: columnName, columnFamily
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 350:9: -> ^( NODE_DROP_INDEX columnFamily columnName )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:350:12: ^( NODE_DROP_INDEX columnFamily columnName )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_DROP_INDEX, "NODE_DROP_INDEX"), root_1);

                adaptor.addChild(root_1, stream_columnFamily.nextTree());
                adaptor.addChild(root_1, stream_columnName.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "dropIndex"

    public static class showVersion_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "showVersion"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:353:1: showVersion : SHOW API_VERSION -> ^( NODE_SHOW_VERSION ) ;
    public final CliParser.showVersion_return showVersion() throws RecognitionException {
        CliParser.showVersion_return retval = new CliParser.showVersion_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SHOW192=null;
        Token API_VERSION193=null;

        CommonTree SHOW192_tree=null;
        CommonTree API_VERSION193_tree=null;
        RewriteRuleTokenStream stream_API_VERSION=new RewriteRuleTokenStream(adaptor,"token API_VERSION");
        RewriteRuleTokenStream stream_SHOW=new RewriteRuleTokenStream(adaptor,"token SHOW");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:354:5: ( SHOW API_VERSION -> ^( NODE_SHOW_VERSION ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:354:7: SHOW API_VERSION
            {
            SHOW192=(Token)match(input,SHOW,FOLLOW_SHOW_in_showVersion2616); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SHOW.add(SHOW192);

            API_VERSION193=(Token)match(input,API_VERSION,FOLLOW_API_VERSION_in_showVersion2618); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_API_VERSION.add(API_VERSION193);



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 355:9: -> ^( NODE_SHOW_VERSION )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:355:12: ^( NODE_SHOW_VERSION )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_SHOW_VERSION, "NODE_SHOW_VERSION"), root_1);

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "showVersion"

    public static class showKeyspaces_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "showKeyspaces"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:358:1: showKeyspaces : SHOW KEYSPACES -> ^( NODE_SHOW_KEYSPACES ) ;
    public final CliParser.showKeyspaces_return showKeyspaces() throws RecognitionException {
        CliParser.showKeyspaces_return retval = new CliParser.showKeyspaces_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SHOW194=null;
        Token KEYSPACES195=null;

        CommonTree SHOW194_tree=null;
        CommonTree KEYSPACES195_tree=null;
        RewriteRuleTokenStream stream_KEYSPACES=new RewriteRuleTokenStream(adaptor,"token KEYSPACES");
        RewriteRuleTokenStream stream_SHOW=new RewriteRuleTokenStream(adaptor,"token SHOW");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:359:5: ( SHOW KEYSPACES -> ^( NODE_SHOW_KEYSPACES ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:359:7: SHOW KEYSPACES
            {
            SHOW194=(Token)match(input,SHOW,FOLLOW_SHOW_in_showKeyspaces2649); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SHOW.add(SHOW194);

            KEYSPACES195=(Token)match(input,KEYSPACES,FOLLOW_KEYSPACES_in_showKeyspaces2651); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_KEYSPACES.add(KEYSPACES195);



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 360:9: -> ^( NODE_SHOW_KEYSPACES )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:360:12: ^( NODE_SHOW_KEYSPACES )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_SHOW_KEYSPACES, "NODE_SHOW_KEYSPACES"), root_1);

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "showKeyspaces"

    public static class showSchema_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "showSchema"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:363:1: showSchema : SHOW SCHEMA ( keyspace )? -> ^( NODE_SHOW_SCHEMA ( keyspace )? ) ;
    public final CliParser.showSchema_return showSchema() throws RecognitionException {
        CliParser.showSchema_return retval = new CliParser.showSchema_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SHOW196=null;
        Token SCHEMA197=null;
        CliParser.keyspace_return keyspace198 = null;


        CommonTree SHOW196_tree=null;
        CommonTree SCHEMA197_tree=null;
        RewriteRuleTokenStream stream_SCHEMA=new RewriteRuleTokenStream(adaptor,"token SCHEMA");
        RewriteRuleTokenStream stream_SHOW=new RewriteRuleTokenStream(adaptor,"token SHOW");
        RewriteRuleSubtreeStream stream_keyspace=new RewriteRuleSubtreeStream(adaptor,"rule keyspace");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:364:5: ( SHOW SCHEMA ( keyspace )? -> ^( NODE_SHOW_SCHEMA ( keyspace )? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:364:7: SHOW SCHEMA ( keyspace )?
            {
            SHOW196=(Token)match(input,SHOW,FOLLOW_SHOW_in_showSchema2683); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SHOW.add(SHOW196);

            SCHEMA197=(Token)match(input,SCHEMA,FOLLOW_SCHEMA_in_showSchema2685); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SCHEMA.add(SCHEMA197);

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:364:19: ( keyspace )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( ((LA20_0>=IntegerPositiveLiteral && LA20_0<=StringLiteral)||LA20_0==IntegerNegativeLiteral) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:364:20: keyspace
                    {
                    pushFollow(FOLLOW_keyspace_in_showSchema2688);
                    keyspace198=keyspace();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyspace.add(keyspace198.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: keyspace
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 365:9: -> ^( NODE_SHOW_SCHEMA ( keyspace )? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:365:12: ^( NODE_SHOW_SCHEMA ( keyspace )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_SHOW_SCHEMA, "NODE_SHOW_SCHEMA"), root_1);

                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:365:31: ( keyspace )?
                if ( stream_keyspace.hasNext() ) {
                    adaptor.addChild(root_1, stream_keyspace.nextTree());

                }
                stream_keyspace.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "showSchema"

    public static class describeTable_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "describeTable"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:368:1: describeTable : DESCRIBE ( keyspace )? -> ^( NODE_DESCRIBE ( keyspace )? ) ;
    public final CliParser.describeTable_return describeTable() throws RecognitionException {
        CliParser.describeTable_return retval = new CliParser.describeTable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DESCRIBE199=null;
        CliParser.keyspace_return keyspace200 = null;


        CommonTree DESCRIBE199_tree=null;
        RewriteRuleTokenStream stream_DESCRIBE=new RewriteRuleTokenStream(adaptor,"token DESCRIBE");
        RewriteRuleSubtreeStream stream_keyspace=new RewriteRuleSubtreeStream(adaptor,"rule keyspace");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:369:5: ( DESCRIBE ( keyspace )? -> ^( NODE_DESCRIBE ( keyspace )? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:369:7: DESCRIBE ( keyspace )?
            {
            DESCRIBE199=(Token)match(input,DESCRIBE,FOLLOW_DESCRIBE_in_describeTable2726); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DESCRIBE.add(DESCRIBE199);

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:369:16: ( keyspace )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( ((LA21_0>=IntegerPositiveLiteral && LA21_0<=StringLiteral)||LA21_0==IntegerNegativeLiteral) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:369:17: keyspace
                    {
                    pushFollow(FOLLOW_keyspace_in_describeTable2729);
                    keyspace200=keyspace();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyspace.add(keyspace200.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: keyspace
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 370:9: -> ^( NODE_DESCRIBE ( keyspace )? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:370:12: ^( NODE_DESCRIBE ( keyspace )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_DESCRIBE, "NODE_DESCRIBE"), root_1);

                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:370:28: ( keyspace )?
                if ( stream_keyspace.hasNext() ) {
                    adaptor.addChild(root_1, stream_keyspace.nextTree());

                }
                stream_keyspace.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "describeTable"

    public static class describeCluster_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "describeCluster"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:373:1: describeCluster : DESCRIBE 'CLUSTER' -> ^( NODE_DESCRIBE_CLUSTER ) ;
    public final CliParser.describeCluster_return describeCluster() throws RecognitionException {
        CliParser.describeCluster_return retval = new CliParser.describeCluster_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DESCRIBE201=null;
        Token string_literal202=null;

        CommonTree DESCRIBE201_tree=null;
        CommonTree string_literal202_tree=null;
        RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
        RewriteRuleTokenStream stream_DESCRIBE=new RewriteRuleTokenStream(adaptor,"token DESCRIBE");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:374:5: ( DESCRIBE 'CLUSTER' -> ^( NODE_DESCRIBE_CLUSTER ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:374:7: DESCRIBE 'CLUSTER'
            {
            DESCRIBE201=(Token)match(input,DESCRIBE,FOLLOW_DESCRIBE_in_describeCluster2771); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DESCRIBE.add(DESCRIBE201);

            string_literal202=(Token)match(input,104,FOLLOW_104_in_describeCluster2773); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_104.add(string_literal202);



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 375:9: -> ^( NODE_DESCRIBE_CLUSTER )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:375:12: ^( NODE_DESCRIBE_CLUSTER )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_DESCRIBE_CLUSTER, "NODE_DESCRIBE_CLUSTER"), root_1);

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "describeCluster"

    public static class useKeyspace_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "useKeyspace"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:378:1: useKeyspace : USE keyspace ( username )? ( password )? -> ^( NODE_USE_TABLE keyspace ( username )? ( password )? ) ;
    public final CliParser.useKeyspace_return useKeyspace() throws RecognitionException {
        CliParser.useKeyspace_return retval = new CliParser.useKeyspace_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token USE203=null;
        CliParser.keyspace_return keyspace204 = null;

        CliParser.username_return username205 = null;

        CliParser.password_return password206 = null;


        CommonTree USE203_tree=null;
        RewriteRuleTokenStream stream_USE=new RewriteRuleTokenStream(adaptor,"token USE");
        RewriteRuleSubtreeStream stream_username=new RewriteRuleSubtreeStream(adaptor,"rule username");
        RewriteRuleSubtreeStream stream_keyspace=new RewriteRuleSubtreeStream(adaptor,"rule keyspace");
        RewriteRuleSubtreeStream stream_password=new RewriteRuleSubtreeStream(adaptor,"rule password");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:379:5: ( USE keyspace ( username )? ( password )? -> ^( NODE_USE_TABLE keyspace ( username )? ( password )? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:379:7: USE keyspace ( username )? ( password )?
            {
            USE203=(Token)match(input,USE,FOLLOW_USE_in_useKeyspace2804); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_USE.add(USE203);

            pushFollow(FOLLOW_keyspace_in_useKeyspace2806);
            keyspace204=keyspace();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyspace.add(keyspace204.getTree());
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:379:20: ( username )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==Identifier) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:379:22: username
                    {
                    pushFollow(FOLLOW_username_in_useKeyspace2810);
                    username205=username();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_username.add(username205.getTree());

                    }
                    break;

            }

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:379:34: ( password )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==StringLiteral) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:379:36: password
                    {
                    pushFollow(FOLLOW_password_in_useKeyspace2817);
                    password206=password();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_password.add(password206.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: keyspace, password, username
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 380:9: -> ^( NODE_USE_TABLE keyspace ( username )? ( password )? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:380:12: ^( NODE_USE_TABLE keyspace ( username )? ( password )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_USE_TABLE, "NODE_USE_TABLE"), root_1);

                adaptor.addChild(root_1, stream_keyspace.nextTree());
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:380:38: ( username )?
                if ( stream_username.hasNext() ) {
                    adaptor.addChild(root_1, stream_username.nextTree());

                }
                stream_username.reset();
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:380:52: ( password )?
                if ( stream_password.hasNext() ) {
                    adaptor.addChild(root_1, stream_password.nextTree());

                }
                stream_password.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "useKeyspace"

    public static class keyValuePairExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyValuePairExpr"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:384:1: keyValuePairExpr : entityName ( ( AND | WITH ) keyValuePair )* -> ^( NODE_NEW_KEYSPACE_ACCESS entityName ( keyValuePair )* ) ;
    public final CliParser.keyValuePairExpr_return keyValuePairExpr() throws RecognitionException {
        CliParser.keyValuePairExpr_return retval = new CliParser.keyValuePairExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND208=null;
        Token WITH209=null;
        CliParser.entityName_return entityName207 = null;

        CliParser.keyValuePair_return keyValuePair210 = null;


        CommonTree AND208_tree=null;
        CommonTree WITH209_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleTokenStream stream_WITH=new RewriteRuleTokenStream(adaptor,"token WITH");
        RewriteRuleSubtreeStream stream_keyValuePair=new RewriteRuleSubtreeStream(adaptor,"rule keyValuePair");
        RewriteRuleSubtreeStream stream_entityName=new RewriteRuleSubtreeStream(adaptor,"rule entityName");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:385:5: ( entityName ( ( AND | WITH ) keyValuePair )* -> ^( NODE_NEW_KEYSPACE_ACCESS entityName ( keyValuePair )* ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:385:7: entityName ( ( AND | WITH ) keyValuePair )*
            {
            pushFollow(FOLLOW_entityName_in_keyValuePairExpr2869);
            entityName207=entityName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_entityName.add(entityName207.getTree());
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:385:18: ( ( AND | WITH ) keyValuePair )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==WITH||LA25_0==AND) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:385:20: ( AND | WITH ) keyValuePair
            	    {
            	    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:385:20: ( AND | WITH )
            	    int alt24=2;
            	    int LA24_0 = input.LA(1);

            	    if ( (LA24_0==AND) ) {
            	        alt24=1;
            	    }
            	    else if ( (LA24_0==WITH) ) {
            	        alt24=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 24, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt24) {
            	        case 1 :
            	            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:385:21: AND
            	            {
            	            AND208=(Token)match(input,AND,FOLLOW_AND_in_keyValuePairExpr2874); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_AND.add(AND208);


            	            }
            	            break;
            	        case 2 :
            	            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:385:27: WITH
            	            {
            	            WITH209=(Token)match(input,WITH,FOLLOW_WITH_in_keyValuePairExpr2878); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_WITH.add(WITH209);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_keyValuePair_in_keyValuePairExpr2881);
            	    keyValuePair210=keyValuePair();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_keyValuePair.add(keyValuePair210.getTree());

            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);



            // AST REWRITE
            // elements: entityName, keyValuePair
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 386:9: -> ^( NODE_NEW_KEYSPACE_ACCESS entityName ( keyValuePair )* )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:386:12: ^( NODE_NEW_KEYSPACE_ACCESS entityName ( keyValuePair )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_NEW_KEYSPACE_ACCESS, "NODE_NEW_KEYSPACE_ACCESS"), root_1);

                adaptor.addChild(root_1, stream_entityName.nextTree());
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:386:50: ( keyValuePair )*
                while ( stream_keyValuePair.hasNext() ) {
                    adaptor.addChild(root_1, stream_keyValuePair.nextTree());

                }
                stream_keyValuePair.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyValuePairExpr"

    public static class keyValuePair_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyValuePair"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:389:1: keyValuePair : attr_name '=' attrValue -> attr_name attrValue ;
    public final CliParser.keyValuePair_return keyValuePair() throws RecognitionException {
        CliParser.keyValuePair_return retval = new CliParser.keyValuePair_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal212=null;
        CliParser.attr_name_return attr_name211 = null;

        CliParser.attrValue_return attrValue213 = null;


        CommonTree char_literal212_tree=null;
        RewriteRuleTokenStream stream_109=new RewriteRuleTokenStream(adaptor,"token 109");
        RewriteRuleSubtreeStream stream_attr_name=new RewriteRuleSubtreeStream(adaptor,"rule attr_name");
        RewriteRuleSubtreeStream stream_attrValue=new RewriteRuleSubtreeStream(adaptor,"rule attrValue");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:390:5: ( attr_name '=' attrValue -> attr_name attrValue )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:390:7: attr_name '=' attrValue
            {
            pushFollow(FOLLOW_attr_name_in_keyValuePair2938);
            attr_name211=attr_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_attr_name.add(attr_name211.getTree());
            char_literal212=(Token)match(input,109,FOLLOW_109_in_keyValuePair2940); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_109.add(char_literal212);

            pushFollow(FOLLOW_attrValue_in_keyValuePair2942);
            attrValue213=attrValue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_attrValue.add(attrValue213.getTree());


            // AST REWRITE
            // elements: attr_name, attrValue
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 391:9: -> attr_name attrValue
            {
                adaptor.addChild(root_0, stream_attr_name.nextTree());
                adaptor.addChild(root_0, stream_attrValue.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyValuePair"

    public static class attrValue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrValue"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:394:1: attrValue : ( arrayConstruct | hashConstruct | attrValueString | attrValueInt | attrValueDouble );
    public final CliParser.attrValue_return attrValue() throws RecognitionException {
        CliParser.attrValue_return retval = new CliParser.attrValue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.arrayConstruct_return arrayConstruct214 = null;

        CliParser.hashConstruct_return hashConstruct215 = null;

        CliParser.attrValueString_return attrValueString216 = null;

        CliParser.attrValueInt_return attrValueInt217 = null;

        CliParser.attrValueDouble_return attrValueDouble218 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:395:5: ( arrayConstruct | hashConstruct | attrValueString | attrValueInt | attrValueDouble )
            int alt26=5;
            switch ( input.LA(1) ) {
            case 115:
                {
                alt26=1;
                }
                break;
            case 118:
                {
                alt26=2;
                }
                break;
            case Identifier:
            case StringLiteral:
                {
                alt26=3;
                }
                break;
            case IntegerPositiveLiteral:
            case IntegerNegativeLiteral:
                {
                alt26=4;
                }
                break;
            case DoubleLiteral:
                {
                alt26=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:395:7: arrayConstruct
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_arrayConstruct_in_attrValue2974);
                    arrayConstruct214=arrayConstruct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayConstruct214.getTree());

                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:396:7: hashConstruct
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_hashConstruct_in_attrValue2982);
                    hashConstruct215=hashConstruct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, hashConstruct215.getTree());

                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:397:7: attrValueString
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_attrValueString_in_attrValue2990);
                    attrValueString216=attrValueString();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, attrValueString216.getTree());

                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:398:7: attrValueInt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_attrValueInt_in_attrValue2998);
                    attrValueInt217=attrValueInt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, attrValueInt217.getTree());

                    }
                    break;
                case 5 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:399:7: attrValueDouble
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_attrValueDouble_in_attrValue3006);
                    attrValueDouble218=attrValueDouble();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, attrValueDouble218.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attrValue"

    public static class arrayConstruct_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arrayConstruct"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:403:1: arrayConstruct : '[' ( hashConstruct ( ',' )? )* ']' -> ^( ARRAY ( hashConstruct )* ) ;
    public final CliParser.arrayConstruct_return arrayConstruct() throws RecognitionException {
        CliParser.arrayConstruct_return retval = new CliParser.arrayConstruct_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal219=null;
        Token char_literal221=null;
        Token char_literal222=null;
        CliParser.hashConstruct_return hashConstruct220 = null;


        CommonTree char_literal219_tree=null;
        CommonTree char_literal221_tree=null;
        CommonTree char_literal222_tree=null;
        RewriteRuleTokenStream stream_116=new RewriteRuleTokenStream(adaptor,"token 116");
        RewriteRuleTokenStream stream_117=new RewriteRuleTokenStream(adaptor,"token 117");
        RewriteRuleTokenStream stream_115=new RewriteRuleTokenStream(adaptor,"token 115");
        RewriteRuleSubtreeStream stream_hashConstruct=new RewriteRuleSubtreeStream(adaptor,"rule hashConstruct");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:404:5: ( '[' ( hashConstruct ( ',' )? )* ']' -> ^( ARRAY ( hashConstruct )* ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:404:7: '[' ( hashConstruct ( ',' )? )* ']'
            {
            char_literal219=(Token)match(input,115,FOLLOW_115_in_arrayConstruct3025); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_115.add(char_literal219);

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:404:11: ( hashConstruct ( ',' )? )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==118) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:404:12: hashConstruct ( ',' )?
            	    {
            	    pushFollow(FOLLOW_hashConstruct_in_arrayConstruct3028);
            	    hashConstruct220=hashConstruct();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_hashConstruct.add(hashConstruct220.getTree());
            	    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:404:26: ( ',' )?
            	    int alt27=2;
            	    int LA27_0 = input.LA(1);

            	    if ( (LA27_0==116) ) {
            	        alt27=1;
            	    }
            	    switch (alt27) {
            	        case 1 :
            	            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:0:0: ','
            	            {
            	            char_literal221=(Token)match(input,116,FOLLOW_116_in_arrayConstruct3030); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_116.add(char_literal221);


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);

            char_literal222=(Token)match(input,117,FOLLOW_117_in_arrayConstruct3035); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_117.add(char_literal222);



            // AST REWRITE
            // elements: hashConstruct
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 405:9: -> ^( ARRAY ( hashConstruct )* )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:405:12: ^( ARRAY ( hashConstruct )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARRAY, "ARRAY"), root_1);

                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:405:20: ( hashConstruct )*
                while ( stream_hashConstruct.hasNext() ) {
                    adaptor.addChild(root_1, stream_hashConstruct.nextTree());

                }
                stream_hashConstruct.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arrayConstruct"

    public static class hashConstruct_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "hashConstruct"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:408:1: hashConstruct : '{' hashElementPair ( ',' hashElementPair )* '}' -> ^( HASH ( hashElementPair )+ ) ;
    public final CliParser.hashConstruct_return hashConstruct() throws RecognitionException {
        CliParser.hashConstruct_return retval = new CliParser.hashConstruct_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal223=null;
        Token char_literal225=null;
        Token char_literal227=null;
        CliParser.hashElementPair_return hashElementPair224 = null;

        CliParser.hashElementPair_return hashElementPair226 = null;


        CommonTree char_literal223_tree=null;
        CommonTree char_literal225_tree=null;
        CommonTree char_literal227_tree=null;
        RewriteRuleTokenStream stream_116=new RewriteRuleTokenStream(adaptor,"token 116");
        RewriteRuleTokenStream stream_118=new RewriteRuleTokenStream(adaptor,"token 118");
        RewriteRuleTokenStream stream_119=new RewriteRuleTokenStream(adaptor,"token 119");
        RewriteRuleSubtreeStream stream_hashElementPair=new RewriteRuleSubtreeStream(adaptor,"rule hashElementPair");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:409:5: ( '{' hashElementPair ( ',' hashElementPair )* '}' -> ^( HASH ( hashElementPair )+ ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:409:7: '{' hashElementPair ( ',' hashElementPair )* '}'
            {
            char_literal223=(Token)match(input,118,FOLLOW_118_in_hashConstruct3073); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_118.add(char_literal223);

            pushFollow(FOLLOW_hashElementPair_in_hashConstruct3075);
            hashElementPair224=hashElementPair();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_hashElementPair.add(hashElementPair224.getTree());
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:409:27: ( ',' hashElementPair )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==116) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:409:28: ',' hashElementPair
            	    {
            	    char_literal225=(Token)match(input,116,FOLLOW_116_in_hashConstruct3078); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_116.add(char_literal225);

            	    pushFollow(FOLLOW_hashElementPair_in_hashConstruct3080);
            	    hashElementPair226=hashElementPair();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_hashElementPair.add(hashElementPair226.getTree());

            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);

            char_literal227=(Token)match(input,119,FOLLOW_119_in_hashConstruct3084); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_119.add(char_literal227);



            // AST REWRITE
            // elements: hashElementPair
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 410:9: -> ^( HASH ( hashElementPair )+ )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:410:12: ^( HASH ( hashElementPair )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(HASH, "HASH"), root_1);

                if ( !(stream_hashElementPair.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_hashElementPair.hasNext() ) {
                    adaptor.addChild(root_1, stream_hashElementPair.nextTree());

                }
                stream_hashElementPair.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "hashConstruct"

    public static class hashElementPair_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "hashElementPair"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:413:1: hashElementPair : rowKey ':' rowValue -> ^( PAIR rowKey rowValue ) ;
    public final CliParser.hashElementPair_return hashElementPair() throws RecognitionException {
        CliParser.hashElementPair_return retval = new CliParser.hashElementPair_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal229=null;
        CliParser.rowKey_return rowKey228 = null;

        CliParser.rowValue_return rowValue230 = null;


        CommonTree char_literal229_tree=null;
        RewriteRuleTokenStream stream_120=new RewriteRuleTokenStream(adaptor,"token 120");
        RewriteRuleSubtreeStream stream_rowKey=new RewriteRuleSubtreeStream(adaptor,"rule rowKey");
        RewriteRuleSubtreeStream stream_rowValue=new RewriteRuleSubtreeStream(adaptor,"rule rowValue");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:414:5: ( rowKey ':' rowValue -> ^( PAIR rowKey rowValue ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:414:7: rowKey ':' rowValue
            {
            pushFollow(FOLLOW_rowKey_in_hashElementPair3120);
            rowKey228=rowKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rowKey.add(rowKey228.getTree());
            char_literal229=(Token)match(input,120,FOLLOW_120_in_hashElementPair3122); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_120.add(char_literal229);

            pushFollow(FOLLOW_rowValue_in_hashElementPair3124);
            rowValue230=rowValue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rowValue.add(rowValue230.getTree());


            // AST REWRITE
            // elements: rowKey, rowValue
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 415:9: -> ^( PAIR rowKey rowValue )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:415:12: ^( PAIR rowKey rowValue )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PAIR, "PAIR"), root_1);

                adaptor.addChild(root_1, stream_rowKey.nextTree());
                adaptor.addChild(root_1, stream_rowValue.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "hashElementPair"

    public static class columnFamilyExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnFamilyExpr"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:418:1: columnFamilyExpr : columnFamily '[' rowKey ']' ( '[' column= columnOrSuperColumn ']' ( '[' super_column= columnOrSuperColumn ']' )? )? -> ^( NODE_COLUMN_ACCESS columnFamily rowKey ( $column ( $super_column)? )? ) ;
    public final CliParser.columnFamilyExpr_return columnFamilyExpr() throws RecognitionException {
        CliParser.columnFamilyExpr_return retval = new CliParser.columnFamilyExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal232=null;
        Token char_literal234=null;
        Token char_literal235=null;
        Token char_literal236=null;
        Token char_literal237=null;
        Token char_literal238=null;
        CliParser.columnOrSuperColumn_return column = null;

        CliParser.columnOrSuperColumn_return super_column = null;

        CliParser.columnFamily_return columnFamily231 = null;

        CliParser.rowKey_return rowKey233 = null;


        CommonTree char_literal232_tree=null;
        CommonTree char_literal234_tree=null;
        CommonTree char_literal235_tree=null;
        CommonTree char_literal236_tree=null;
        CommonTree char_literal237_tree=null;
        CommonTree char_literal238_tree=null;
        RewriteRuleTokenStream stream_117=new RewriteRuleTokenStream(adaptor,"token 117");
        RewriteRuleTokenStream stream_115=new RewriteRuleTokenStream(adaptor,"token 115");
        RewriteRuleSubtreeStream stream_columnFamily=new RewriteRuleSubtreeStream(adaptor,"rule columnFamily");
        RewriteRuleSubtreeStream stream_rowKey=new RewriteRuleSubtreeStream(adaptor,"rule rowKey");
        RewriteRuleSubtreeStream stream_columnOrSuperColumn=new RewriteRuleSubtreeStream(adaptor,"rule columnOrSuperColumn");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:419:5: ( columnFamily '[' rowKey ']' ( '[' column= columnOrSuperColumn ']' ( '[' super_column= columnOrSuperColumn ']' )? )? -> ^( NODE_COLUMN_ACCESS columnFamily rowKey ( $column ( $super_column)? )? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:419:7: columnFamily '[' rowKey ']' ( '[' column= columnOrSuperColumn ']' ( '[' super_column= columnOrSuperColumn ']' )? )?
            {
            pushFollow(FOLLOW_columnFamily_in_columnFamilyExpr3159);
            columnFamily231=columnFamily();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnFamily.add(columnFamily231.getTree());
            char_literal232=(Token)match(input,115,FOLLOW_115_in_columnFamilyExpr3161); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_115.add(char_literal232);

            pushFollow(FOLLOW_rowKey_in_columnFamilyExpr3163);
            rowKey233=rowKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rowKey.add(rowKey233.getTree());
            char_literal234=(Token)match(input,117,FOLLOW_117_in_columnFamilyExpr3165); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_117.add(char_literal234);

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:420:9: ( '[' column= columnOrSuperColumn ']' ( '[' super_column= columnOrSuperColumn ']' )? )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==115) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:420:11: '[' column= columnOrSuperColumn ']' ( '[' super_column= columnOrSuperColumn ']' )?
                    {
                    char_literal235=(Token)match(input,115,FOLLOW_115_in_columnFamilyExpr3178); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_115.add(char_literal235);

                    pushFollow(FOLLOW_columnOrSuperColumn_in_columnFamilyExpr3182);
                    column=columnOrSuperColumn();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnOrSuperColumn.add(column.getTree());
                    char_literal236=(Token)match(input,117,FOLLOW_117_in_columnFamilyExpr3184); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_117.add(char_literal236);

                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:421:13: ( '[' super_column= columnOrSuperColumn ']' )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==115) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:421:14: '[' super_column= columnOrSuperColumn ']'
                            {
                            char_literal237=(Token)match(input,115,FOLLOW_115_in_columnFamilyExpr3200); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_115.add(char_literal237);

                            pushFollow(FOLLOW_columnOrSuperColumn_in_columnFamilyExpr3204);
                            super_column=columnOrSuperColumn();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnOrSuperColumn.add(super_column.getTree());
                            char_literal238=(Token)match(input,117,FOLLOW_117_in_columnFamilyExpr3206); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_117.add(char_literal238);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: rowKey, column, columnFamily, super_column
            // token labels: 
            // rule labels: retval, column, super_column
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_column=new RewriteRuleSubtreeStream(adaptor,"rule column",column!=null?column.tree:null);
            RewriteRuleSubtreeStream stream_super_column=new RewriteRuleSubtreeStream(adaptor,"rule super_column",super_column!=null?super_column.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 423:7: -> ^( NODE_COLUMN_ACCESS columnFamily rowKey ( $column ( $super_column)? )? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:423:10: ^( NODE_COLUMN_ACCESS columnFamily rowKey ( $column ( $super_column)? )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_COLUMN_ACCESS, "NODE_COLUMN_ACCESS"), root_1);

                adaptor.addChild(root_1, stream_columnFamily.nextTree());
                adaptor.addChild(root_1, stream_rowKey.nextTree());
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:423:51: ( $column ( $super_column)? )?
                if ( stream_column.hasNext()||stream_super_column.hasNext() ) {
                    adaptor.addChild(root_1, stream_column.nextTree());
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:423:60: ( $super_column)?
                    if ( stream_super_column.hasNext() ) {
                        adaptor.addChild(root_1, stream_super_column.nextTree());

                    }
                    stream_super_column.reset();

                }
                stream_column.reset();
                stream_super_column.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnFamilyExpr"

    public static class keyRangeExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyRangeExpr"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:426:1: keyRangeExpr : '[' ( (startKey= entityName )? ':' (endKey= entityName )? )? ']' -> ^( NODE_KEY_RANGE ( $startKey)? ( $endKey)? ) ;
    public final CliParser.keyRangeExpr_return keyRangeExpr() throws RecognitionException {
        CliParser.keyRangeExpr_return retval = new CliParser.keyRangeExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal239=null;
        Token char_literal240=null;
        Token char_literal241=null;
        CliParser.entityName_return startKey = null;

        CliParser.entityName_return endKey = null;


        CommonTree char_literal239_tree=null;
        CommonTree char_literal240_tree=null;
        CommonTree char_literal241_tree=null;
        RewriteRuleTokenStream stream_117=new RewriteRuleTokenStream(adaptor,"token 117");
        RewriteRuleTokenStream stream_115=new RewriteRuleTokenStream(adaptor,"token 115");
        RewriteRuleTokenStream stream_120=new RewriteRuleTokenStream(adaptor,"token 120");
        RewriteRuleSubtreeStream stream_entityName=new RewriteRuleSubtreeStream(adaptor,"rule entityName");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:427:5: ( '[' ( (startKey= entityName )? ':' (endKey= entityName )? )? ']' -> ^( NODE_KEY_RANGE ( $startKey)? ( $endKey)? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:427:10: '[' ( (startKey= entityName )? ':' (endKey= entityName )? )? ']'
            {
            char_literal239=(Token)match(input,115,FOLLOW_115_in_keyRangeExpr3269); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_115.add(char_literal239);

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:427:14: ( (startKey= entityName )? ':' (endKey= entityName )? )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( ((LA34_0>=IntegerPositiveLiteral && LA34_0<=StringLiteral)||LA34_0==IntegerNegativeLiteral||LA34_0==120) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:427:16: (startKey= entityName )? ':' (endKey= entityName )?
                    {
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:427:24: (startKey= entityName )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( ((LA32_0>=IntegerPositiveLiteral && LA32_0<=StringLiteral)||LA32_0==IntegerNegativeLiteral) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:0:0: startKey= entityName
                            {
                            pushFollow(FOLLOW_entityName_in_keyRangeExpr3275);
                            startKey=entityName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_entityName.add(startKey.getTree());

                            }
                            break;

                    }

                    char_literal240=(Token)match(input,120,FOLLOW_120_in_keyRangeExpr3278); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_120.add(char_literal240);

                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:427:47: (endKey= entityName )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( ((LA33_0>=IntegerPositiveLiteral && LA33_0<=StringLiteral)||LA33_0==IntegerNegativeLiteral) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:0:0: endKey= entityName
                            {
                            pushFollow(FOLLOW_entityName_in_keyRangeExpr3282);
                            endKey=entityName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_entityName.add(endKey.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }

            char_literal241=(Token)match(input,117,FOLLOW_117_in_keyRangeExpr3288); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_117.add(char_literal241);



            // AST REWRITE
            // elements: startKey, endKey
            // token labels: 
            // rule labels: endKey, retval, startKey
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_endKey=new RewriteRuleSubtreeStream(adaptor,"rule endKey",endKey!=null?endKey.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_startKey=new RewriteRuleSubtreeStream(adaptor,"rule startKey",startKey!=null?startKey.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 428:7: -> ^( NODE_KEY_RANGE ( $startKey)? ( $endKey)? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:428:10: ^( NODE_KEY_RANGE ( $startKey)? ( $endKey)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_KEY_RANGE, "NODE_KEY_RANGE"), root_1);

                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:428:27: ( $startKey)?
                if ( stream_startKey.hasNext() ) {
                    adaptor.addChild(root_1, stream_startKey.nextTree());

                }
                stream_startKey.reset();
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:428:38: ( $endKey)?
                if ( stream_endKey.hasNext() ) {
                    adaptor.addChild(root_1, stream_endKey.nextTree());

                }
                stream_endKey.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyRangeExpr"

    public static class columnName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnName"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:431:1: columnName : entityName ;
    public final CliParser.columnName_return columnName() throws RecognitionException {
        CliParser.columnName_return retval = new CliParser.columnName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.entityName_return entityName242 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:432:2: ( entityName )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:432:4: entityName
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_entityName_in_columnName3322);
            entityName242=entityName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, entityName242.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnName"

    public static class attr_name_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attr_name"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:435:1: attr_name : Identifier ;
    public final CliParser.attr_name_return attr_name() throws RecognitionException {
        CliParser.attr_name_return retval = new CliParser.attr_name_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier243=null;

        CommonTree Identifier243_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:436:5: ( Identifier )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:436:7: Identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            Identifier243=(Token)match(input,Identifier,FOLLOW_Identifier_in_attr_name3336); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            Identifier243_tree = (CommonTree)adaptor.create(Identifier243);
            adaptor.addChild(root_0, Identifier243_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attr_name"

    public static class attrValueString_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrValueString"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:439:1: attrValueString : ( Identifier | StringLiteral ) ;
    public final CliParser.attrValueString_return attrValueString() throws RecognitionException {
        CliParser.attrValueString_return retval = new CliParser.attrValueString_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set244=null;

        CommonTree set244_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:440:5: ( ( Identifier | StringLiteral ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:440:7: ( Identifier | StringLiteral )
            {
            root_0 = (CommonTree)adaptor.nil();

            set244=(Token)input.LT(1);
            if ( (input.LA(1)>=Identifier && input.LA(1)<=StringLiteral) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set244));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attrValueString"

    public static class attrValueInt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrValueInt"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:443:1: attrValueInt : ( IntegerPositiveLiteral | IntegerNegativeLiteral );
    public final CliParser.attrValueInt_return attrValueInt() throws RecognitionException {
        CliParser.attrValueInt_return retval = new CliParser.attrValueInt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set245=null;

        CommonTree set245_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:444:5: ( IntegerPositiveLiteral | IntegerNegativeLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set245=(Token)input.LT(1);
            if ( input.LA(1)==IntegerPositiveLiteral||input.LA(1)==IntegerNegativeLiteral ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set245));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attrValueInt"

    public static class attrValueDouble_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrValueDouble"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:448:1: attrValueDouble : DoubleLiteral ;
    public final CliParser.attrValueDouble_return attrValueDouble() throws RecognitionException {
        CliParser.attrValueDouble_return retval = new CliParser.attrValueDouble_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DoubleLiteral246=null;

        CommonTree DoubleLiteral246_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:449:5: ( DoubleLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:449:7: DoubleLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            DoubleLiteral246=(Token)match(input,DoubleLiteral,FOLLOW_DoubleLiteral_in_attrValueDouble3405); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DoubleLiteral246_tree = (CommonTree)adaptor.create(DoubleLiteral246);
            adaptor.addChild(root_0, DoubleLiteral246_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attrValueDouble"

    public static class keyspace_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyspace"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:452:1: keyspace : entityName ;
    public final CliParser.keyspace_return keyspace() throws RecognitionException {
        CliParser.keyspace_return retval = new CliParser.keyspace_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.entityName_return entityName247 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:453:2: ( entityName )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:453:4: entityName
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_entityName_in_keyspace3421);
            entityName247=entityName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, entityName247.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyspace"

    public static class replica_placement_strategy_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "replica_placement_strategy"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:456:1: replica_placement_strategy : StringLiteral ;
    public final CliParser.replica_placement_strategy_return replica_placement_strategy() throws RecognitionException {
        CliParser.replica_placement_strategy_return retval = new CliParser.replica_placement_strategy_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token StringLiteral248=null;

        CommonTree StringLiteral248_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:457:5: ( StringLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:457:7: StringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            StringLiteral248=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_replica_placement_strategy3435); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            StringLiteral248_tree = (CommonTree)adaptor.create(StringLiteral248);
            adaptor.addChild(root_0, StringLiteral248_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "replica_placement_strategy"

    public static class keyspaceNewName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyspaceNewName"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:460:1: keyspaceNewName : entityName ;
    public final CliParser.keyspaceNewName_return keyspaceNewName() throws RecognitionException {
        CliParser.keyspaceNewName_return retval = new CliParser.keyspaceNewName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.entityName_return entityName249 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:461:2: ( entityName )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:461:4: entityName
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_entityName_in_keyspaceNewName3449);
            entityName249=entityName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, entityName249.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyspaceNewName"

    public static class comparator_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparator"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:464:1: comparator : StringLiteral ;
    public final CliParser.comparator_return comparator() throws RecognitionException {
        CliParser.comparator_return retval = new CliParser.comparator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token StringLiteral250=null;

        CommonTree StringLiteral250_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:465:5: ( StringLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:465:7: StringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            StringLiteral250=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_comparator3463); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            StringLiteral250_tree = (CommonTree)adaptor.create(StringLiteral250);
            adaptor.addChild(root_0, StringLiteral250_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comparator"

    public static class command_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "command"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:468:1: command : Identifier ;
    public final CliParser.command_return command() throws RecognitionException {
        CliParser.command_return retval = new CliParser.command_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier251=null;

        CommonTree Identifier251_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:468:9: ( Identifier )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:468:11: Identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            Identifier251=(Token)match(input,Identifier,FOLLOW_Identifier_in_command3482); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            Identifier251_tree = (CommonTree)adaptor.create(Identifier251);
            adaptor.addChild(root_0, Identifier251_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "command"

    public static class newColumnFamily_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "newColumnFamily"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:471:1: newColumnFamily : entityName ;
    public final CliParser.newColumnFamily_return newColumnFamily() throws RecognitionException {
        CliParser.newColumnFamily_return retval = new CliParser.newColumnFamily_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.entityName_return entityName252 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:472:2: ( entityName )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:472:4: entityName
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_entityName_in_newColumnFamily3496);
            entityName252=entityName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, entityName252.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "newColumnFamily"

    public static class username_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "username"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:475:1: username : Identifier ;
    public final CliParser.username_return username() throws RecognitionException {
        CliParser.username_return retval = new CliParser.username_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier253=null;

        CommonTree Identifier253_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:475:9: ( Identifier )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:475:11: Identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            Identifier253=(Token)match(input,Identifier,FOLLOW_Identifier_in_username3505); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            Identifier253_tree = (CommonTree)adaptor.create(Identifier253);
            adaptor.addChild(root_0, Identifier253_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "username"

    public static class password_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "password"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:478:1: password : StringLiteral ;
    public final CliParser.password_return password() throws RecognitionException {
        CliParser.password_return retval = new CliParser.password_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token StringLiteral254=null;

        CommonTree StringLiteral254_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:478:9: ( StringLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:478:11: StringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            StringLiteral254=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_password3517); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            StringLiteral254_tree = (CommonTree)adaptor.create(StringLiteral254);
            adaptor.addChild(root_0, StringLiteral254_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "password"

    public static class columnFamily_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnFamily"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:481:1: columnFamily : entityName ;
    public final CliParser.columnFamily_return columnFamily() throws RecognitionException {
        CliParser.columnFamily_return retval = new CliParser.columnFamily_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.entityName_return entityName255 = null;



        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:482:3: ( entityName )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:482:5: entityName
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_entityName_in_columnFamily3532);
            entityName255=entityName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, entityName255.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnFamily"

    public static class entityName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "entityName"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:485:1: entityName : ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral ) ;
    public final CliParser.entityName_return entityName() throws RecognitionException {
        CliParser.entityName_return retval = new CliParser.entityName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set256=null;

        CommonTree set256_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:486:3: ( ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:486:5: ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral )
            {
            root_0 = (CommonTree)adaptor.nil();

            set256=(Token)input.LT(1);
            if ( (input.LA(1)>=IntegerPositiveLiteral && input.LA(1)<=StringLiteral)||input.LA(1)==IntegerNegativeLiteral ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set256));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "entityName"

    public static class rowKey_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rowKey"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:489:1: rowKey : ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall ) ;
    public final CliParser.rowKey_return rowKey() throws RecognitionException {
        CliParser.rowKey_return retval = new CliParser.rowKey_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier257=null;
        Token StringLiteral258=null;
        Token IntegerPositiveLiteral259=null;
        Token IntegerNegativeLiteral260=null;
        CliParser.functionCall_return functionCall261 = null;


        CommonTree Identifier257_tree=null;
        CommonTree StringLiteral258_tree=null;
        CommonTree IntegerPositiveLiteral259_tree=null;
        CommonTree IntegerNegativeLiteral260_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:5: ( ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:8: ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:8: ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall )
            int alt35=5;
            switch ( input.LA(1) ) {
            case Identifier:
                {
                int LA35_1 = input.LA(2);

                if ( (LA35_1==121) ) {
                    alt35=5;
                }
                else if ( (LA35_1==117||LA35_1==120) ) {
                    alt35=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 35, 1, input);

                    throw nvae;
                }
                }
                break;
            case StringLiteral:
                {
                alt35=2;
                }
                break;
            case IntegerPositiveLiteral:
                {
                alt35=3;
                }
                break;
            case IntegerNegativeLiteral:
                {
                alt35=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }

            switch (alt35) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:9: Identifier
                    {
                    Identifier257=(Token)match(input,Identifier,FOLLOW_Identifier_in_rowKey3577); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier257_tree = (CommonTree)adaptor.create(Identifier257);
                    adaptor.addChild(root_0, Identifier257_tree);
                    }

                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:22: StringLiteral
                    {
                    StringLiteral258=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_rowKey3581); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    StringLiteral258_tree = (CommonTree)adaptor.create(StringLiteral258);
                    adaptor.addChild(root_0, StringLiteral258_tree);
                    }

                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:38: IntegerPositiveLiteral
                    {
                    IntegerPositiveLiteral259=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_rowKey3585); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerPositiveLiteral259_tree = (CommonTree)adaptor.create(IntegerPositiveLiteral259);
                    adaptor.addChild(root_0, IntegerPositiveLiteral259_tree);
                    }

                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:63: IntegerNegativeLiteral
                    {
                    IntegerNegativeLiteral260=(Token)match(input,IntegerNegativeLiteral,FOLLOW_IntegerNegativeLiteral_in_rowKey3589); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerNegativeLiteral260_tree = (CommonTree)adaptor.create(IntegerNegativeLiteral260);
                    adaptor.addChild(root_0, IntegerNegativeLiteral260_tree);
                    }

                    }
                    break;
                case 5 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:490:88: functionCall
                    {
                    pushFollow(FOLLOW_functionCall_in_rowKey3593);
                    functionCall261=functionCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, functionCall261.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rowKey"

    public static class rowValue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rowValue"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:493:1: rowValue : ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall | hashConstruct ) ;
    public final CliParser.rowValue_return rowValue() throws RecognitionException {
        CliParser.rowValue_return retval = new CliParser.rowValue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier262=null;
        Token StringLiteral263=null;
        Token IntegerPositiveLiteral264=null;
        Token IntegerNegativeLiteral265=null;
        CliParser.functionCall_return functionCall266 = null;

        CliParser.hashConstruct_return hashConstruct267 = null;


        CommonTree Identifier262_tree=null;
        CommonTree StringLiteral263_tree=null;
        CommonTree IntegerPositiveLiteral264_tree=null;
        CommonTree IntegerNegativeLiteral265_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:5: ( ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall | hashConstruct ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:8: ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall | hashConstruct )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:8: ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral | functionCall | hashConstruct )
            int alt36=6;
            switch ( input.LA(1) ) {
            case Identifier:
                {
                int LA36_1 = input.LA(2);

                if ( (LA36_1==121) ) {
                    alt36=5;
                }
                else if ( (LA36_1==EOF||LA36_1==116||LA36_1==119) ) {
                    alt36=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 36, 1, input);

                    throw nvae;
                }
                }
                break;
            case StringLiteral:
                {
                alt36=2;
                }
                break;
            case IntegerPositiveLiteral:
                {
                alt36=3;
                }
                break;
            case IntegerNegativeLiteral:
                {
                alt36=4;
                }
                break;
            case 118:
                {
                alt36=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }

            switch (alt36) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:9: Identifier
                    {
                    Identifier262=(Token)match(input,Identifier,FOLLOW_Identifier_in_rowValue3615); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier262_tree = (CommonTree)adaptor.create(Identifier262);
                    adaptor.addChild(root_0, Identifier262_tree);
                    }

                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:22: StringLiteral
                    {
                    StringLiteral263=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_rowValue3619); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    StringLiteral263_tree = (CommonTree)adaptor.create(StringLiteral263);
                    adaptor.addChild(root_0, StringLiteral263_tree);
                    }

                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:38: IntegerPositiveLiteral
                    {
                    IntegerPositiveLiteral264=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_rowValue3623); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerPositiveLiteral264_tree = (CommonTree)adaptor.create(IntegerPositiveLiteral264);
                    adaptor.addChild(root_0, IntegerPositiveLiteral264_tree);
                    }

                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:63: IntegerNegativeLiteral
                    {
                    IntegerNegativeLiteral265=(Token)match(input,IntegerNegativeLiteral,FOLLOW_IntegerNegativeLiteral_in_rowValue3627); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerNegativeLiteral265_tree = (CommonTree)adaptor.create(IntegerNegativeLiteral265);
                    adaptor.addChild(root_0, IntegerNegativeLiteral265_tree);
                    }

                    }
                    break;
                case 5 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:88: functionCall
                    {
                    pushFollow(FOLLOW_functionCall_in_rowValue3631);
                    functionCall266=functionCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, functionCall266.getTree());

                    }
                    break;
                case 6 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:494:103: hashConstruct
                    {
                    pushFollow(FOLLOW_hashConstruct_in_rowValue3635);
                    hashConstruct267=hashConstruct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, hashConstruct267.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rowValue"

    public static class value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:497:1: value : ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall ) ;
    public final CliParser.value_return value() throws RecognitionException {
        CliParser.value_return retval = new CliParser.value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier268=null;
        Token IntegerPositiveLiteral269=null;
        Token IntegerNegativeLiteral270=null;
        Token StringLiteral271=null;
        CliParser.functionCall_return functionCall272 = null;


        CommonTree Identifier268_tree=null;
        CommonTree IntegerPositiveLiteral269_tree=null;
        CommonTree IntegerNegativeLiteral270_tree=null;
        CommonTree StringLiteral271_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:5: ( ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:7: ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:7: ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall )
            int alt37=5;
            switch ( input.LA(1) ) {
            case Identifier:
                {
                int LA37_1 = input.LA(2);

                if ( (LA37_1==121) ) {
                    alt37=5;
                }
                else if ( (LA37_1==EOF||LA37_1==SEMICOLON||LA37_1==WITH||LA37_1==AND||LA37_1==LIMIT) ) {
                    alt37=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 37, 1, input);

                    throw nvae;
                }
                }
                break;
            case IntegerPositiveLiteral:
                {
                alt37=2;
                }
                break;
            case IntegerNegativeLiteral:
                {
                alt37=3;
                }
                break;
            case StringLiteral:
                {
                alt37=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }

            switch (alt37) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:8: Identifier
                    {
                    Identifier268=(Token)match(input,Identifier,FOLLOW_Identifier_in_value3657); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier268_tree = (CommonTree)adaptor.create(Identifier268);
                    adaptor.addChild(root_0, Identifier268_tree);
                    }

                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:21: IntegerPositiveLiteral
                    {
                    IntegerPositiveLiteral269=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_value3661); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerPositiveLiteral269_tree = (CommonTree)adaptor.create(IntegerPositiveLiteral269);
                    adaptor.addChild(root_0, IntegerPositiveLiteral269_tree);
                    }

                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:46: IntegerNegativeLiteral
                    {
                    IntegerNegativeLiteral270=(Token)match(input,IntegerNegativeLiteral,FOLLOW_IntegerNegativeLiteral_in_value3665); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerNegativeLiteral270_tree = (CommonTree)adaptor.create(IntegerNegativeLiteral270);
                    adaptor.addChild(root_0, IntegerNegativeLiteral270_tree);
                    }

                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:71: StringLiteral
                    {
                    StringLiteral271=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_value3669); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    StringLiteral271_tree = (CommonTree)adaptor.create(StringLiteral271);
                    adaptor.addChild(root_0, StringLiteral271_tree);
                    }

                    }
                    break;
                case 5 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:498:87: functionCall
                    {
                    pushFollow(FOLLOW_functionCall_in_value3673);
                    functionCall272=functionCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, functionCall272.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "value"

    public static class functionCall_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "functionCall"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:501:1: functionCall : functionName= Identifier '(' ( functionArgument )? ')' -> ^( FUNCTION_CALL $functionName ( functionArgument )? ) ;
    public final CliParser.functionCall_return functionCall() throws RecognitionException {
        CliParser.functionCall_return retval = new CliParser.functionCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token functionName=null;
        Token char_literal273=null;
        Token char_literal275=null;
        CliParser.functionArgument_return functionArgument274 = null;


        CommonTree functionName_tree=null;
        CommonTree char_literal273_tree=null;
        CommonTree char_literal275_tree=null;
        RewriteRuleTokenStream stream_121=new RewriteRuleTokenStream(adaptor,"token 121");
        RewriteRuleTokenStream stream_122=new RewriteRuleTokenStream(adaptor,"token 122");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleSubtreeStream stream_functionArgument=new RewriteRuleSubtreeStream(adaptor,"rule functionArgument");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:502:5: (functionName= Identifier '(' ( functionArgument )? ')' -> ^( FUNCTION_CALL $functionName ( functionArgument )? ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:502:7: functionName= Identifier '(' ( functionArgument )? ')'
            {
            functionName=(Token)match(input,Identifier,FOLLOW_Identifier_in_functionCall3694); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_Identifier.add(functionName);

            char_literal273=(Token)match(input,121,FOLLOW_121_in_functionCall3696); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_121.add(char_literal273);

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:502:35: ( functionArgument )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( ((LA38_0>=IntegerPositiveLiteral && LA38_0<=StringLiteral)||LA38_0==IntegerNegativeLiteral) ) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:0:0: functionArgument
                    {
                    pushFollow(FOLLOW_functionArgument_in_functionCall3698);
                    functionArgument274=functionArgument();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_functionArgument.add(functionArgument274.getTree());

                    }
                    break;

            }

            char_literal275=(Token)match(input,122,FOLLOW_122_in_functionCall3701); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_122.add(char_literal275);



            // AST REWRITE
            // elements: functionName, functionArgument
            // token labels: functionName
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleTokenStream stream_functionName=new RewriteRuleTokenStream(adaptor,"token functionName",functionName);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 503:9: -> ^( FUNCTION_CALL $functionName ( functionArgument )? )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:503:12: ^( FUNCTION_CALL $functionName ( functionArgument )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FUNCTION_CALL, "FUNCTION_CALL"), root_1);

                adaptor.addChild(root_1, stream_functionName.nextNode());
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:503:42: ( functionArgument )?
                if ( stream_functionArgument.hasNext() ) {
                    adaptor.addChild(root_1, stream_functionArgument.nextTree());

                }
                stream_functionArgument.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "functionCall"

    public static class functionArgument_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "functionArgument"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:506:1: functionArgument : ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral );
    public final CliParser.functionArgument_return functionArgument() throws RecognitionException {
        CliParser.functionArgument_return retval = new CliParser.functionArgument_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set276=null;

        CommonTree set276_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:507:5: ( Identifier | StringLiteral | IntegerPositiveLiteral | IntegerNegativeLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set276=(Token)input.LT(1);
            if ( (input.LA(1)>=IntegerPositiveLiteral && input.LA(1)<=StringLiteral)||input.LA(1)==IntegerNegativeLiteral ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set276));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "functionArgument"

    public static class columnOrSuperColumn_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnOrSuperColumn"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:510:1: columnOrSuperColumn : ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall ) ;
    public final CliParser.columnOrSuperColumn_return columnOrSuperColumn() throws RecognitionException {
        CliParser.columnOrSuperColumn_return retval = new CliParser.columnOrSuperColumn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier277=null;
        Token IntegerPositiveLiteral278=null;
        Token IntegerNegativeLiteral279=null;
        Token StringLiteral280=null;
        CliParser.functionCall_return functionCall281 = null;


        CommonTree Identifier277_tree=null;
        CommonTree IntegerPositiveLiteral278_tree=null;
        CommonTree IntegerNegativeLiteral279_tree=null;
        CommonTree StringLiteral280_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:5: ( ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:7: ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:7: ( Identifier | IntegerPositiveLiteral | IntegerNegativeLiteral | StringLiteral | functionCall )
            int alt39=5;
            switch ( input.LA(1) ) {
            case Identifier:
                {
                int LA39_1 = input.LA(2);

                if ( (LA39_1==121) ) {
                    alt39=5;
                }
                else if ( ((LA39_1>=109 && LA39_1<=113)||LA39_1==117) ) {
                    alt39=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 39, 1, input);

                    throw nvae;
                }
                }
                break;
            case IntegerPositiveLiteral:
                {
                alt39=2;
                }
                break;
            case IntegerNegativeLiteral:
                {
                alt39=3;
                }
                break;
            case StringLiteral:
                {
                alt39=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }

            switch (alt39) {
                case 1 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:8: Identifier
                    {
                    Identifier277=(Token)match(input,Identifier,FOLLOW_Identifier_in_columnOrSuperColumn3769); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier277_tree = (CommonTree)adaptor.create(Identifier277);
                    adaptor.addChild(root_0, Identifier277_tree);
                    }

                    }
                    break;
                case 2 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:21: IntegerPositiveLiteral
                    {
                    IntegerPositiveLiteral278=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_columnOrSuperColumn3773); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerPositiveLiteral278_tree = (CommonTree)adaptor.create(IntegerPositiveLiteral278);
                    adaptor.addChild(root_0, IntegerPositiveLiteral278_tree);
                    }

                    }
                    break;
                case 3 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:46: IntegerNegativeLiteral
                    {
                    IntegerNegativeLiteral279=(Token)match(input,IntegerNegativeLiteral,FOLLOW_IntegerNegativeLiteral_in_columnOrSuperColumn3777); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IntegerNegativeLiteral279_tree = (CommonTree)adaptor.create(IntegerNegativeLiteral279);
                    adaptor.addChild(root_0, IntegerNegativeLiteral279_tree);
                    }

                    }
                    break;
                case 4 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:71: StringLiteral
                    {
                    StringLiteral280=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_columnOrSuperColumn3781); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    StringLiteral280_tree = (CommonTree)adaptor.create(StringLiteral280);
                    adaptor.addChild(root_0, StringLiteral280_tree);
                    }

                    }
                    break;
                case 5 :
                    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:511:87: functionCall
                    {
                    pushFollow(FOLLOW_functionCall_in_columnOrSuperColumn3785);
                    functionCall281=functionCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, functionCall281.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnOrSuperColumn"

    public static class host_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "host"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:514:1: host : host_name -> ^( NODE_ID_LIST host_name ) ;
    public final CliParser.host_return host() throws RecognitionException {
        CliParser.host_return retval = new CliParser.host_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CliParser.host_name_return host_name282 = null;


        RewriteRuleSubtreeStream stream_host_name=new RewriteRuleSubtreeStream(adaptor,"rule host_name");
        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:515:5: ( host_name -> ^( NODE_ID_LIST host_name ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:515:7: host_name
            {
            pushFollow(FOLLOW_host_name_in_host3807);
            host_name282=host_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_host_name.add(host_name282.getTree());


            // AST REWRITE
            // elements: host_name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 516:9: -> ^( NODE_ID_LIST host_name )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:516:12: ^( NODE_ID_LIST host_name )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_ID_LIST, "NODE_ID_LIST"), root_1);

                adaptor.addChild(root_1, stream_host_name.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "host"

    public static class host_name_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "host_name"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:519:1: host_name : Identifier ( '.' Identifier )* ;
    public final CliParser.host_name_return host_name() throws RecognitionException {
        CliParser.host_name_return retval = new CliParser.host_name_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier283=null;
        Token char_literal284=null;
        Token Identifier285=null;

        CommonTree Identifier283_tree=null;
        CommonTree char_literal284_tree=null;
        CommonTree Identifier285_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:520:5: ( Identifier ( '.' Identifier )* )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:520:7: Identifier ( '.' Identifier )*
            {
            root_0 = (CommonTree)adaptor.nil();

            Identifier283=(Token)match(input,Identifier,FOLLOW_Identifier_in_host_name3840); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            Identifier283_tree = (CommonTree)adaptor.create(Identifier283);
            adaptor.addChild(root_0, Identifier283_tree);
            }
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:520:18: ( '.' Identifier )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==114) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:520:19: '.' Identifier
            	    {
            	    char_literal284=(Token)match(input,114,FOLLOW_114_in_host_name3843); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal284_tree = (CommonTree)adaptor.create(char_literal284);
            	    adaptor.addChild(root_0, char_literal284_tree);
            	    }
            	    Identifier285=(Token)match(input,Identifier,FOLLOW_Identifier_in_host_name3845); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    Identifier285_tree = (CommonTree)adaptor.create(Identifier285);
            	    adaptor.addChild(root_0, Identifier285_tree);
            	    }

            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "host_name"

    public static class ip_address_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ip_address"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:523:1: ip_address : IP_ADDRESS -> ^( NODE_ID_LIST IP_ADDRESS ) ;
    public final CliParser.ip_address_return ip_address() throws RecognitionException {
        CliParser.ip_address_return retval = new CliParser.ip_address_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IP_ADDRESS286=null;

        CommonTree IP_ADDRESS286_tree=null;
        RewriteRuleTokenStream stream_IP_ADDRESS=new RewriteRuleTokenStream(adaptor,"token IP_ADDRESS");

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:524:5: ( IP_ADDRESS -> ^( NODE_ID_LIST IP_ADDRESS ) )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:524:7: IP_ADDRESS
            {
            IP_ADDRESS286=(Token)match(input,IP_ADDRESS,FOLLOW_IP_ADDRESS_in_ip_address3868); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IP_ADDRESS.add(IP_ADDRESS286);



            // AST REWRITE
            // elements: IP_ADDRESS
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 525:9: -> ^( NODE_ID_LIST IP_ADDRESS )
            {
                // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:525:12: ^( NODE_ID_LIST IP_ADDRESS )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NODE_ID_LIST, "NODE_ID_LIST"), root_1);

                adaptor.addChild(root_1, stream_IP_ADDRESS.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ip_address"

    public static class port_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "port"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:529:1: port : IntegerPositiveLiteral ;
    public final CliParser.port_return port() throws RecognitionException {
        CliParser.port_return retval = new CliParser.port_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IntegerPositiveLiteral287=null;

        CommonTree IntegerPositiveLiteral287_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:530:5: ( IntegerPositiveLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:530:7: IntegerPositiveLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            IntegerPositiveLiteral287=(Token)match(input,IntegerPositiveLiteral,FOLLOW_IntegerPositiveLiteral_in_port3907); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IntegerPositiveLiteral287_tree = (CommonTree)adaptor.create(IntegerPositiveLiteral287);
            adaptor.addChild(root_0, IntegerPositiveLiteral287_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "port"

    public static class incrementValue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "incrementValue"
    // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:533:1: incrementValue : ( IntegerPositiveLiteral | IntegerNegativeLiteral );
    public final CliParser.incrementValue_return incrementValue() throws RecognitionException {
        CliParser.incrementValue_return retval = new CliParser.incrementValue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set288=null;

        CommonTree set288_tree=null;

        try {
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:534:5: ( IntegerPositiveLiteral | IntegerNegativeLiteral )
            // /opt/projects/carbon/trunkKernelOrbitPlatform/carbon/platform/trunk/dependencies/cassandra/src/java/org/apache/cassandra/cli/Cli.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set288=(Token)input.LT(1);
            if ( input.LA(1)==IntegerPositiveLiteral||input.LA(1)==IntegerNegativeLiteral ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set288));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "incrementValue"

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA2_eotS =
        "\35\uffff";
    static final String DFA2_eofS =
        "\1\23\3\uffff\1\25\30\uffff";
    static final String DFA2_minS =
        "\1\56\3\uffff\1\56\3\72\25\uffff";
    static final String DFA2_maxS =
        "\1\152\3\uffff\1\150\2\74\1\77\25\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\1\1\2\1\3\4\uffff\1\14\1\15\1\16\1\17\1\20\1\21\1\22"+
        "\1\23\1\24\1\25\1\26\1\30\1\5\1\4\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\27";
    static final String DFA2_specialS =
        "\35\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\23\1\1\1\13\1\10\1\4\2\2\1\16\3\uffff\1\5\1\uffff\1\6\2\uffff"+
            "\1\7\1\uffff\1\12\1\14\2\15\1\11\1\3\1\17\1\20\1\21\1\22\40"+
            "\uffff\1\13",
            "",
            "",
            "",
            "\1\25\33\uffff\3\25\5\uffff\1\25\25\uffff\1\24",
            "\1\26\1\uffff\1\27",
            "\1\30\1\uffff\1\31",
            "\1\33\1\uffff\1\32\2\uffff\1\34",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
            return "142:1: statement : ( connectStatement | exitStatement | countStatement | describeTable | describeCluster | addKeyspace | addColumnFamily | updateKeyspace | updateColumnFamily | delColumnFamily | delKeyspace | useKeyspace | delStatement | getStatement | helpStatement | setStatement | incrStatement | showStatement | listStatement | truncateStatement | assumeStatement | consistencyLevelStatement | dropIndex | -> ^( NODE_NO_OP ) );";
        }
    }
    static final String DFA6_eotS =
        "\45\uffff";
    static final String DFA6_eofS =
        "\1\uffff\1\27\4\uffff\1\31\36\uffff";
    static final String DFA6_minS =
        "\1\60\1\56\4\uffff\1\56\2\uffff\1\66\3\72\30\uffff";
    static final String DFA6_maxS =
        "\1\152\1\111\4\uffff\1\150\2\uffff\1\151\2\74\1\77\30\uffff";
    static final String DFA6_acceptS =
        "\2\uffff\1\36\1\1\1\2\1\3\1\uffff\1\6\1\7\4\uffff\1\23\1\24\1\25"+
        "\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\5\1\4\1\10\1\11\1\12"+
        "\1\13\1\14\1\16\1\15\1\17\1\20\1\21\1\22";
    static final String DFA6_specialS =
        "\45\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\1\71\uffff\1\2",
            "\1\27\1\4\1\3\1\5\1\6\1\7\1\10\1\11\3\uffff\1\12\1\uffff\1"+
            "\13\2\uffff\1\14\1\uffff\1\15\1\16\1\17\1\20\1\21\1\22\1\23"+
            "\1\24\1\25\1\26",
            "",
            "",
            "",
            "",
            "\1\31\71\uffff\1\30",
            "",
            "",
            "\1\33\1\34\1\35\60\uffff\1\32",
            "\1\36\1\uffff\1\37",
            "\1\40\1\uffff\1\41",
            "\1\42\1\uffff\1\43\2\uffff\1\44",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "176:1: helpStatement : ( HELP HELP -> ^( NODE_HELP NODE_HELP ) | HELP CONNECT -> ^( NODE_HELP NODE_CONNECT ) | HELP USE -> ^( NODE_HELP NODE_USE_TABLE ) | HELP DESCRIBE -> ^( NODE_HELP NODE_DESCRIBE ) | HELP DESCRIBE 'CLUSTER' -> ^( NODE_HELP NODE_DESCRIBE_CLUSTER ) | HELP EXIT -> ^( NODE_HELP NODE_EXIT ) | HELP QUIT -> ^( NODE_HELP NODE_EXIT ) | HELP SHOW 'CLUSTER NAME' -> ^( NODE_HELP NODE_SHOW_CLUSTER_NAME ) | HELP SHOW KEYSPACES -> ^( NODE_HELP NODE_SHOW_KEYSPACES ) | HELP SHOW SCHEMA -> ^( NODE_HELP NODE_SHOW_SCHEMA ) | HELP SHOW API_VERSION -> ^( NODE_HELP NODE_SHOW_VERSION ) | HELP CREATE KEYSPACE -> ^( NODE_HELP NODE_ADD_KEYSPACE ) | HELP UPDATE KEYSPACE -> ^( NODE_HELP NODE_UPDATE_KEYSPACE ) | HELP CREATE COLUMN FAMILY -> ^( NODE_HELP NODE_ADD_COLUMN_FAMILY ) | HELP UPDATE COLUMN FAMILY -> ^( NODE_HELP NODE_UPDATE_COLUMN_FAMILY ) | HELP DROP KEYSPACE -> ^( NODE_HELP NODE_DEL_KEYSPACE ) | HELP DROP COLUMN FAMILY -> ^( NODE_HELP NODE_DEL_COLUMN_FAMILY ) | HELP DROP INDEX -> ^( NODE_HELP NODE_DROP_INDEX ) | HELP GET -> ^( NODE_HELP NODE_THRIFT_GET ) | HELP SET -> ^( NODE_HELP NODE_THRIFT_SET ) | HELP INCR -> ^( NODE_HELP NODE_THRIFT_INCR ) | HELP DECR -> ^( NODE_HELP NODE_THRIFT_DECR ) | HELP DEL -> ^( NODE_HELP NODE_THRIFT_DEL ) | HELP COUNT -> ^( NODE_HELP NODE_THRIFT_COUNT ) | HELP LIST -> ^( NODE_HELP NODE_LIST ) | HELP TRUNCATE -> ^( NODE_HELP NODE_TRUNCATE ) | HELP ASSUME -> ^( NODE_HELP NODE_ASSUME ) | HELP CONSISTENCYLEVEL -> ^( NODE_HELP NODE_CONSISTENCY_LEVEL ) | HELP -> ^( NODE_HELP ) | '?' -> ^( NODE_HELP ) );";
        }
    }
 

    public static final BitSet FOLLOW_statement_in_root421 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_root423 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_root426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_connectStatement_in_statement442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exitStatement_in_statement450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_countStatement_in_statement458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_describeTable_in_statement466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_describeCluster_in_statement474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_addKeyspace_in_statement482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_addColumnFamily_in_statement490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateKeyspace_in_statement498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateColumnFamily_in_statement506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delColumnFamily_in_statement514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delKeyspace_in_statement522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_useKeyspace_in_statement530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delStatement_in_statement538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_getStatement_in_statement546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_helpStatement_in_statement554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setStatement_in_statement562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_incrStatement_in_statement570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_showStatement_in_statement578 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listStatement_in_statement586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_truncateStatement_in_statement594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assumeStatement_in_statement602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_consistencyLevelStatement_in_statement610 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropIndex_in_statement618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONNECT_in_connectStatement647 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_host_in_connectStatement649 = new BitSet(new long[]{0x0000000000000000L,0x0000008000000000L});
    public static final BitSet FOLLOW_103_in_connectStatement651 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_port_in_connectStatement653 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L});
    public static final BitSet FOLLOW_username_in_connectStatement656 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_password_in_connectStatement658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONNECT_in_connectStatement693 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_ip_address_in_connectStatement695 = new BitSet(new long[]{0x0000000000000000L,0x0000008000000000L});
    public static final BitSet FOLLOW_103_in_connectStatement697 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_port_in_connectStatement699 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L});
    public static final BitSet FOLLOW_username_in_connectStatement702 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_password_in_connectStatement704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement748 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_HELP_in_helpStatement750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement775 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_CONNECT_in_helpStatement777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement802 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_USE_in_helpStatement804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement829 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_DESCRIBE_in_helpStatement831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement855 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_DESCRIBE_in_helpStatement857 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
    public static final BitSet FOLLOW_104_in_helpStatement859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement883 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_EXIT_in_helpStatement885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement910 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_QUIT_in_helpStatement912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement937 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SHOW_in_helpStatement939 = new BitSet(new long[]{0x0000000000000000L,0x0000020000000000L});
    public static final BitSet FOLLOW_105_in_helpStatement941 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement965 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SHOW_in_helpStatement967 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_KEYSPACES_in_helpStatement969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement994 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SHOW_in_helpStatement996 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_SCHEMA_in_helpStatement998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1026 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_SHOW_in_helpStatement1028 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_API_VERSION_in_helpStatement1030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1054 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_CREATE_in_helpStatement1056 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_KEYSPACE_in_helpStatement1058 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1083 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_UPDATE_in_helpStatement1085 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_KEYSPACE_in_helpStatement1087 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1111 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_CREATE_in_helpStatement1113 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_COLUMN_in_helpStatement1115 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_FAMILY_in_helpStatement1117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1142 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_UPDATE_in_helpStatement1144 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_COLUMN_in_helpStatement1146 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_FAMILY_in_helpStatement1148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1172 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_DROP_in_helpStatement1174 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_KEYSPACE_in_helpStatement1176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1201 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_DROP_in_helpStatement1203 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_COLUMN_in_helpStatement1205 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_FAMILY_in_helpStatement1207 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1232 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_DROP_in_helpStatement1234 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_INDEX_in_helpStatement1236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1260 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_GET_in_helpStatement1262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1287 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_helpStatement1289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1314 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_INCR_in_helpStatement1316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1340 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_DECR_in_helpStatement1342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1366 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_DEL_in_helpStatement1368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1393 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_COUNT_in_helpStatement1395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1420 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_LIST_in_helpStatement1422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1447 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_TRUNCATE_in_helpStatement1449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1473 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_ASSUME_in_helpStatement1475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1499 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CONSISTENCYLEVEL_in_helpStatement1501 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HELP_in_helpStatement1525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_106_in_helpStatement1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUIT_in_exitStatement1583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXIT_in_exitStatement1597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GET_in_getStatement1620 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamilyExpr_in_getStatement1622 = new BitSet(new long[]{0x0000000000000002L,0x0000080000800000L});
    public static final BitSet FOLLOW_107_in_getStatement1625 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001C00L});
    public static final BitSet FOLLOW_typeIdentifier_in_getStatement1627 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_LIMIT_in_getStatement1632 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_getStatement1636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GET_in_getStatement1681 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamily_in_getStatement1683 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_108_in_getStatement1685 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_getCondition_in_getStatement1687 = new BitSet(new long[]{0x0000000000000002L,0x0000000000820000L});
    public static final BitSet FOLLOW_AND_in_getStatement1690 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_getCondition_in_getStatement1692 = new BitSet(new long[]{0x0000000000000002L,0x0000000000820000L});
    public static final BitSet FOLLOW_LIMIT_in_getStatement1697 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_getStatement1701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnOrSuperColumn_in_getCondition1752 = new BitSet(new long[]{0x0000000000000000L,0x0003E00000000000L});
    public static final BitSet FOLLOW_operator_in_getCondition1754 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_value_in_getCondition1756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_operator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_typeIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_setStatement1852 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamilyExpr_in_setStatement1854 = new BitSet(new long[]{0x0000000000000000L,0x0000200000000000L});
    public static final BitSet FOLLOW_109_in_setStatement1856 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_value_in_setStatement1860 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_WITH_in_setStatement1863 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_TTL_in_setStatement1865 = new BitSet(new long[]{0x0000000000000000L,0x0000200000000000L});
    public static final BitSet FOLLOW_109_in_setStatement1867 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_setStatement1871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INCR_in_incrStatement1917 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamilyExpr_in_incrStatement1919 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_BY_in_incrStatement1922 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040400L});
    public static final BitSet FOLLOW_incrementValue_in_incrStatement1926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECR_in_incrStatement1960 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamilyExpr_in_incrStatement1962 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_BY_in_incrStatement1965 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040400L});
    public static final BitSet FOLLOW_incrementValue_in_incrStatement1969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_countStatement2012 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamilyExpr_in_countStatement2014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEL_in_delStatement2048 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamilyExpr_in_delStatement2050 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_showClusterName_in_showStatement2084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_showVersion_in_showStatement2092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_showKeyspaces_in_showStatement2100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_showSchema_in_showStatement2108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIST_in_listStatement2125 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamily_in_listStatement2127 = new BitSet(new long[]{0x0000000000000002L,0x0008000000800000L});
    public static final BitSet FOLLOW_keyRangeExpr_in_listStatement2129 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_LIMIT_in_listStatement2133 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_listStatement2137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUNCATE_in_truncateStatement2183 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamily_in_truncateStatement2185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSUME_in_assumeStatement2218 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamily_in_assumeStatement2220 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_Identifier_in_assumeStatement2224 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_107_in_assumeStatement2226 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_entityName_in_assumeStatement2228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONSISTENCYLEVEL_in_consistencyLevelStatement2266 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_107_in_consistencyLevelStatement2268 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_Identifier_in_consistencyLevelStatement2272 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SHOW_in_showClusterName2306 = new BitSet(new long[]{0x0000000000000000L,0x0000020000000000L});
    public static final BitSet FOLLOW_105_in_showClusterName2308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_addKeyspace2339 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_KEYSPACE_in_addKeyspace2341 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyValuePairExpr_in_addKeyspace2343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_addColumnFamily2377 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_COLUMN_in_addColumnFamily2379 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_FAMILY_in_addColumnFamily2381 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyValuePairExpr_in_addColumnFamily2383 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateKeyspace2417 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_KEYSPACE_in_updateKeyspace2419 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyValuePairExpr_in_updateKeyspace2421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateColumnFamily2454 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_COLUMN_in_updateColumnFamily2456 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_FAMILY_in_updateColumnFamily2458 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyValuePairExpr_in_updateColumnFamily2460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_delKeyspace2493 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_KEYSPACE_in_delKeyspace2495 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyspace_in_delKeyspace2497 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_delColumnFamily2531 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_COLUMN_in_delColumnFamily2533 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_FAMILY_in_delColumnFamily2535 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamily_in_delColumnFamily2537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_dropIndex2571 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_INDEX_in_dropIndex2573 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_ON_in_dropIndex2575 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnFamily_in_dropIndex2577 = new BitSet(new long[]{0x0000000000000000L,0x0004000000000000L});
    public static final BitSet FOLLOW_114_in_dropIndex2579 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnName_in_dropIndex2581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SHOW_in_showVersion2616 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_API_VERSION_in_showVersion2618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SHOW_in_showKeyspaces2649 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_KEYSPACES_in_showKeyspaces2651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SHOW_in_showSchema2683 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_SCHEMA_in_showSchema2685 = new BitSet(new long[]{0x0000000000000002L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyspace_in_showSchema2688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESCRIBE_in_describeTable2726 = new BitSet(new long[]{0x0000000000000002L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyspace_in_describeTable2729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESCRIBE_in_describeCluster2771 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
    public static final BitSet FOLLOW_104_in_describeCluster2773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USE_in_useKeyspace2804 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_keyspace_in_useKeyspace2806 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001800L});
    public static final BitSet FOLLOW_username_in_useKeyspace2810 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_password_in_useKeyspace2817 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_entityName_in_keyValuePairExpr2869 = new BitSet(new long[]{0x0000000000000002L,0x0000000000022000L});
    public static final BitSet FOLLOW_AND_in_keyValuePairExpr2874 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_WITH_in_keyValuePairExpr2878 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_keyValuePair_in_keyValuePairExpr2881 = new BitSet(new long[]{0x0000000000000002L,0x0000000000022000L});
    public static final BitSet FOLLOW_attr_name_in_keyValuePair2938 = new BitSet(new long[]{0x0000000000000000L,0x0000200000000000L});
    public static final BitSet FOLLOW_109_in_keyValuePair2940 = new BitSet(new long[]{0x0000000000000000L,0x00480000000C1C00L});
    public static final BitSet FOLLOW_attrValue_in_keyValuePair2942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayConstruct_in_attrValue2974 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_hashConstruct_in_attrValue2982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrValueString_in_attrValue2990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrValueInt_in_attrValue2998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrValueDouble_in_attrValue3006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_115_in_arrayConstruct3025 = new BitSet(new long[]{0x0000000000000000L,0x0060000000000000L});
    public static final BitSet FOLLOW_hashConstruct_in_arrayConstruct3028 = new BitSet(new long[]{0x0000000000000000L,0x0070000000000000L});
    public static final BitSet FOLLOW_116_in_arrayConstruct3030 = new BitSet(new long[]{0x0000000000000000L,0x0060000000000000L});
    public static final BitSet FOLLOW_117_in_arrayConstruct3035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_118_in_hashConstruct3073 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_hashElementPair_in_hashConstruct3075 = new BitSet(new long[]{0x0000000000000000L,0x0090000000000000L});
    public static final BitSet FOLLOW_116_in_hashConstruct3078 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_hashElementPair_in_hashConstruct3080 = new BitSet(new long[]{0x0000000000000000L,0x0090000000000000L});
    public static final BitSet FOLLOW_119_in_hashConstruct3084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rowKey_in_hashElementPair3120 = new BitSet(new long[]{0x0000000000000000L,0x0100000000000000L});
    public static final BitSet FOLLOW_120_in_hashElementPair3122 = new BitSet(new long[]{0x0000000000000000L,0x0040000000041C00L});
    public static final BitSet FOLLOW_rowValue_in_hashElementPair3124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnFamily_in_columnFamilyExpr3159 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000000L});
    public static final BitSet FOLLOW_115_in_columnFamilyExpr3161 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_rowKey_in_columnFamilyExpr3163 = new BitSet(new long[]{0x0000000000000000L,0x0020000000000000L});
    public static final BitSet FOLLOW_117_in_columnFamilyExpr3165 = new BitSet(new long[]{0x0000000000000002L,0x0008000000000000L});
    public static final BitSet FOLLOW_115_in_columnFamilyExpr3178 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnOrSuperColumn_in_columnFamilyExpr3182 = new BitSet(new long[]{0x0000000000000000L,0x0020000000000000L});
    public static final BitSet FOLLOW_117_in_columnFamilyExpr3184 = new BitSet(new long[]{0x0000000000000002L,0x0008000000000000L});
    public static final BitSet FOLLOW_115_in_columnFamilyExpr3200 = new BitSet(new long[]{0x0000000000000000L,0x0000000000041C00L});
    public static final BitSet FOLLOW_columnOrSuperColumn_in_columnFamilyExpr3204 = new BitSet(new long[]{0x0000000000000000L,0x0020000000000000L});
    public static final BitSet FOLLOW_117_in_columnFamilyExpr3206 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_115_in_keyRangeExpr3269 = new BitSet(new long[]{0x0000000000000000L,0x0120000000041C00L});
    public static final BitSet FOLLOW_entityName_in_keyRangeExpr3275 = new BitSet(new long[]{0x0000000000000000L,0x0100000000000000L});
    public static final BitSet FOLLOW_120_in_keyRangeExpr3278 = new BitSet(new long[]{0x0000000000000000L,0x0020000000041C00L});
    public static final BitSet FOLLOW_entityName_in_keyRangeExpr3282 = new BitSet(new long[]{0x0000000000000000L,0x0020000000000000L});
    public static final BitSet FOLLOW_117_in_keyRangeExpr3288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_entityName_in_columnName3322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_attr_name3336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attrValueString3353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attrValueInt0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DoubleLiteral_in_attrValueDouble3405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_entityName_in_keyspace3421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_replica_placement_strategy3435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_entityName_in_keyspaceNewName3449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_comparator3463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_command3482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_entityName_in_newColumnFamily3496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_username3505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_password3517 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_entityName_in_columnFamily3532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_entityName3545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_rowKey3577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_rowKey3581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_rowKey3585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerNegativeLiteral_in_rowKey3589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionCall_in_rowKey3593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_rowValue3615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_rowValue3619 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_rowValue3623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerNegativeLiteral_in_rowValue3627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionCall_in_rowValue3631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_hashConstruct_in_rowValue3635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_value3657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_value3661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerNegativeLiteral_in_value3665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_value3669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionCall_in_value3673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_functionCall3694 = new BitSet(new long[]{0x0000000000000000L,0x0200000000000000L});
    public static final BitSet FOLLOW_121_in_functionCall3696 = new BitSet(new long[]{0x0000000000000000L,0x0400000000041C00L});
    public static final BitSet FOLLOW_functionArgument_in_functionCall3698 = new BitSet(new long[]{0x0000000000000000L,0x0400000000000000L});
    public static final BitSet FOLLOW_122_in_functionCall3701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_functionArgument0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_columnOrSuperColumn3769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_columnOrSuperColumn3773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerNegativeLiteral_in_columnOrSuperColumn3777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_columnOrSuperColumn3781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionCall_in_columnOrSuperColumn3785 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_host_name_in_host3807 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_host_name3840 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_114_in_host_name3843 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_Identifier_in_host_name3845 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_IP_ADDRESS_in_ip_address3868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IntegerPositiveLiteral_in_port3907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_incrementValue0 = new BitSet(new long[]{0x0000000000000002L});

}