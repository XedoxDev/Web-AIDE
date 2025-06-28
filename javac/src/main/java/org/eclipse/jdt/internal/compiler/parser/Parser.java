package org.eclipse.jdt.internal.compiler.parser;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ReadManager;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CombinedBinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompactConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EitherOrMultiPattern;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.ExportsStatement;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FakeDefaultLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.GuardedPattern;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.ImplicitTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntersectionCastTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.ast.ModuleStatement;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.OpensStatement;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.PackageVisibilityStatement;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Pattern;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.ProvidesStatement;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Receiver;
import org.eclipse.jdt.internal.compiler.ast.RecordComponent;
import org.eclipse.jdt.internal.compiler.ast.RecordPattern;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.ast.RequiresStatement;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchExpression;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.TextBlock;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypePattern;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.UnionTypeReference;
import org.eclipse.jdt.internal.compiler.ast.UsesStatement;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.ast.YieldStatement;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.diagnose.DiagnoseParser;
import org.eclipse.jdt.internal.compiler.parser.diagnose.RangeUtil;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.Util;

public class Parser
        implements TerminalTokens, ParserBasicInformation, ConflictedParser, OperatorIds, TypeIds {
    protected static final int THIS_CALL = 3;
    protected static final int SUPER_CALL = 2;
    public static final char[] FALL_THROUGH_TAG = "$FALL-THROUGH$".toCharArray();
    public static final char[] CASES_OMITTED_TAG = "$CASES-OMITTED$".toCharArray();
    public static char[] asb = null;
    public static char[] asr = null;
    protected static final int AstStackIncrement = 100;
    public static char[] base_action = null;
    public static final int BracketKinds = 3;
    public static short[] check_table = null;
    public static final int CurlyBracket = 2;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_AUTOMATON = false;
    private static final String EOF_TOKEN = "$eof";
    private static final String ERROR_TOKEN = "$error";
    protected static final int ExpressionStackIncrement = 100;
    protected static final int GenericsStackIncrement = 10;
    private static final String FILEPREFIX = "parser";
    public static char[] in_symb = null;
    private static final String INVALID_CHARACTER = "Invalid Character";
    public static char[] lhs = null;
    public static String[] name = null;
    public static char[] nasb = null;
    public static char[] nasr = null;
    public static char[] non_terminal_index = null;
    private static final String READABLE_NAMES_FILE = "readableNames";
    public static String[] readableName = null;
    public static byte[] rhs = null;
    public static int[] reverse_index = null;
    public static char[] recovery_templates_index = null;
    public static char[] recovery_templates = null;
    public static char[] statements_recovery_filter = null;
    public static long[] rules_compliance = null;
    public static final int RoundBracket = 0;
    public static char[] scope_la = null;
    public static char[] scope_lhs = null;
    public static char[] scope_prefix = null;
    public static char[] scope_rhs = null;
    public static char[] scope_state = null;
    public static char[] scope_state_set = null;
    public static char[] scope_suffix = null;
    public static final int SquareBracket = 1;
    protected static final int StackIncrement = 255;
    public static char[] term_action = null;
    public static char[] term_check = null;
    public static char[] terminal_index = null;
    private static final String UNEXPECTED_EOF = "Unexpected End Of File";
    public static boolean VERBOSE_RECOVERY = false;
    protected static final int HALT = 0;
    protected static final int RESTART = 1;
    protected static final int RESUME = 2;
    private static final short TYPE_CLASS = 1;
    public Scanner scanner;
    public int currentToken;
    protected int astLengthPtr;
    protected int[] astLengthStack;
    protected int astPtr;
    protected ASTNode[] astStack = new ASTNode[100];
    public CompilationUnitDeclaration compilationUnit;
    protected RecoveredElement currentElement;
    protected boolean diet = false;
    protected int dietInt = 0;
    protected int endPosition;
    protected int endStatementPosition;
    protected int expressionLengthPtr;
    protected int[] expressionLengthStack;
    protected int expressionPtr;
    protected Expression[] expressionStack = new Expression[100];
    protected int rBracketPosition;
    public int firstToken;
    protected int typeAnnotationPtr;
    protected int typeAnnotationLengthPtr;
    protected Annotation[] typeAnnotationStack = new Annotation[100];
    protected int[] typeAnnotationLengthStack;
    protected static final int TypeAnnotationStackIncrement = 100;
    protected int genericsIdentifiersLengthPtr;
    protected int[] genericsIdentifiersLengthStack = new int[10];
    protected int genericsLengthPtr;
    protected int[] genericsLengthStack = new int[10];
    protected int genericsPtr;
    protected ASTNode[] genericsStack = new ASTNode[10];
    protected boolean hasError;
    protected boolean hasReportedError;
    protected int identifierLengthPtr;
    protected int[] identifierLengthStack;
    protected long[] identifierPositionStack;
    protected int identifierPtr;
    protected char[][] identifierStack;
    protected boolean ignoreNextOpeningBrace;
    protected boolean ignoreNextClosingBrace;
    protected int intPtr;
    protected int[] intStack;
    public int lastAct;
    protected int lastCheckPoint;
    protected int lastErrorEndPosition;
    protected int lastErrorEndPositionBeforeRecovery = -1;
    protected int lastIgnoredToken;
    protected int nextIgnoredToken;
    protected int listLength;
    protected int listTypeParameterLength;
    protected int lParenPos;
    protected int rParenPos;
    protected int modifiers;
    protected int modifiersSourceStart;
    protected int annotationAsModifierSourceStart = -1;
    protected int colonColonStart = -1;
    protected int[] nestedMethod;
    protected int forStartPosition = 0;
    protected int nestedType;
    protected int dimensions;
    protected int switchNestingLevel;
    ASTNode[] noAstNodes = new ASTNode[100];
    Expression[] noExpressions = new Expression[100];
    protected boolean optimizeStringLiterals = true;
    protected CompilerOptions options;
    protected ProblemReporter problemReporter;
    protected int rBraceStart;
    protected int rBraceEnd;
    protected int rBraceSuccessorStart;
    protected int realBlockPtr;
    protected int[] realBlockStack;
    protected int recoveredStaticInitializerStart;
    public ReferenceContext referenceContext;
    public boolean reportOnlyOneSyntaxError = false;
    public boolean reportSyntaxErrorIsRequired = true;
    protected boolean restartRecovery;
    protected boolean annotationRecoveryActivated = true;
    protected int lastPosistion;
    public boolean methodRecoveryActivated = false;
    protected boolean statementRecoveryActivated = false;
    protected TypeDeclaration[] recoveredTypes;
    protected int recoveredTypePtr;
    protected int nextTypeStart;
    protected TypeDeclaration pendingRecoveredType;
    public RecoveryScanner recoveryScanner;
    protected int[] stack = new int[255];
    protected int stateStackTop;
    protected int synchronizedBlockSourceStart;
    protected int[] variablesCounter;
    protected boolean checkExternalizeStrings;
    protected boolean recordStringLiterals;
    public Javadoc javadoc;
    public JavadocParser javadocParser;
    protected int lastJavadocEnd;
    public ReadManager readManager;
    protected int valueLambdaNestDepth = -1;
    private int[] stateStackLengthStack = new int[0];
    protected boolean parsingJava8Plus;
    protected boolean parsingJava9Plus;
    protected boolean parsingJava14Plus;
    protected boolean parsingJava15Plus;
    protected boolean parsingJava17Plus;
    protected boolean parsingJava18Plus;
    protected boolean parsingJava21Plus;
    protected boolean parsingJava22Plus;
    protected boolean previewEnabled;
    protected boolean parsingJava11Plus;
    protected int unstackedAct = 17648;
    private boolean haltOnSyntaxError = false;
    private boolean tolerateDefaultClassMethods = false;
    private boolean processingLambdaParameterList = false;
    private boolean expectTypeAnnotation = false;
    private boolean reparsingFunctionalExpression = false;
    private Map<TypeDeclaration, Integer[]> recordNestedMethodLevels;
    protected boolean parsingRecordComponents = false;
    // $FF: synthetic field
    private static volatile int[]
            $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$CaseLabelKind;
    // $FF: synthetic field
    private static volatile int[]
            $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$LocalTypeKind;

    static {
        try {
            initTables();
        } catch (IOException var1) {
            throw new ExceptionInInitializerError(var1.getMessage());
        }
    }

    public static int asi(int state) {
        return asb[original_state(state)];
    }

    public static final short base_check(int i) {
        return check_table[i - 927];
    }

    private static final void buildFile(String filename, List listToDump) {
        try {
            Throwable var2 = null;
            Object var3 = null;

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

                try {
                    Iterator var6 = listToDump.iterator();

                    while (var6.hasNext()) {
                        Object o = var6.next();
                        writer.write(String.valueOf(o));
                    }

                    writer.flush();
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            } catch (Throwable var14) {

                throw var14;
            }
        } catch (IOException var15) {
        }

        System.out.println(filename + " creation complete");
    }

    private static void buildFileForCompliance(String file, int length, String[] tokens) {
        byte[] result = new byte[length * 8];

        for (int i = 0; i < tokens.length; i += 3) {
            if ("2".equals(tokens[i])) {
                int index = Integer.parseInt(tokens[i + 1]);
                String token = tokens[i + 2].trim();
                long compliance = 0L;
                if ("1.4".equals(token)) {
                    compliance = 3145728L;
                } else if ("1.5".equals(token)) {
                    compliance = 3211264L;
                } else if ("1.6".equals(token)) {
                    compliance = 3276800L;
                } else if ("1.7".equals(token)) {
                    compliance = 3342336L;
                } else if ("1.8".equals(token)) {
                    compliance = 3407872L;
                } else if ("9".equals(token)) {
                    compliance = 3473408L;
                } else if ("10".equals(token)) {
                    compliance = 3538944L;
                } else if ("11".equals(token)) {
                    compliance = 3604480L;
                } else if ("12".equals(token)) {
                    compliance = 3670016L;
                } else if ("13".equals(token)) {
                    compliance = 3735552L;
                } else if ("14".equals(token)) {
                    compliance = 3801088L;
                } else if ("15".equals(token)) {
                    compliance = 3866624L;
                } else if ("16".equals(token)) {
                    compliance = 3932160L;
                } else if ("17".equals(token)) {
                    compliance = 3997696L;
                } else if ("18".equals(token)) {
                    compliance = 4063232L;
                } else if ("19".equals(token)) {
                    compliance = 4128768L;
                } else if ("20".equals(token)) {
                    compliance = 4194304L;
                } else if ("21".equals(token)) {
                    compliance = 4259840L;
                } else if ("recovery".equals(token)) {
                    compliance = Long.MAX_VALUE;
                }

                int j = index * 8;
                result[j] = (byte) ((int) (compliance >>> 56));
                result[j + 1] = (byte) ((int) (compliance >>> 48));
                result[j + 2] = (byte) ((int) (compliance >>> 40));
                result[j + 3] = (byte) ((int) (compliance >>> 32));
                result[j + 4] = (byte) ((int) (compliance >>> 24));
                result[j + 5] = (byte) ((int) (compliance >>> 16));
                result[j + 6] = (byte) ((int) (compliance >>> 8));
                result[j + 7] = (byte) ((int) compliance);
            }
        }

        buildFileForTable(file, result);
    }

    private static final String[] buildFileForName(String filename, String contents) {
        String[] result = new String[contents.length()];
        result[0] = null;
        int resultCount = 1;
        StringBuilder buffer = new StringBuilder();
        int start = contents.indexOf("name[]");
        start = contents.indexOf(34, start);
        int end = contents.indexOf("};", start);
        contents = contents.substring(start, end);
        boolean addLineSeparator = false;
        int tokenStart = -1;
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < contents.length(); ++i) {
            char c = contents.charAt(i);
            if (c == '"') {
                if (tokenStart == -1) {
                    tokenStart = i + 1;
                } else {
                    if (addLineSeparator) {
                        buffer.append('\n');
                        result[resultCount++] = currentToken.toString();
                        currentToken = new StringBuilder();
                    }

                    String token = contents.substring(tokenStart, i);
                    if (token.equals("$error")) {
                        token = "Invalid Character";
                    } else if (token.equals("$eof")) {
                        token = "Unexpected End Of File";
                    }

                    buffer.append(token);
                    currentToken.append(token);
                    addLineSeparator = true;
                    tokenStart = -1;
                }
            }

            if (tokenStart == -1 && c == '+') {
                addLineSeparator = false;
            }
        }

        if (currentToken.length() > 0) {
            result[resultCount++] = currentToken.toString();
        }

        buildFileForTable(filename, buffer.toString().toCharArray());
        System.arraycopy(result, 0, result = new String[resultCount], 0, resultCount);
        return result;
    }

    private static void buildFileForReadableName(
            String file,
            char[] newLhs,
            char[] newNonTerminalIndex,
            String[] newName,
            String[] tokens) {
        ArrayList entries = new ArrayList();
        boolean[] alreadyAdded = new boolean[newName.length];

        int i;
        for (i = 0; i < tokens.length; i += 3) {
            if ("1".equals(tokens[i])) {
                int index = newNonTerminalIndex[newLhs[Integer.parseInt(tokens[i + 1])]];
                StringBuilder buffer = new StringBuilder();
                if (!alreadyAdded[index]) {
                    alreadyAdded[index] = true;
                    buffer.append(newName[index]);
                    buffer.append('=');
                    buffer.append(tokens[i + 2].trim());
                    buffer.append('\n');
                    entries.add(String.valueOf(buffer));
                }
            }
        }

        for (i = 1; !"Invalid Character".equals(newName[i]); ++i) {}

        ++i;

        for (; i < alreadyAdded.length; ++i) {
            if (!alreadyAdded[i]) {
                System.out.println(newName[i] + " has no readable name");
            }
        }

        Collections.sort(entries);
        buildFile(file, entries);
    }

    private static final void buildFileForTable(String filename, byte[] bytes) {
        try {
            Throwable var2 = null;
            Object var3 = null;

            try {
                FileOutputStream stream = new FileOutputStream(filename);

                try {
                    stream.write(bytes);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            } catch (Throwable var12) {
                if (var2 == null) {
                    var2 = var12;
                } else if (var2 != var12) {
                    var2.addSuppressed(var12);
                }

                throw var12;
            }
        } catch (IOException var13) {
        }

        System.out.println(filename + " creation complete");
    }

    private static final void buildFileForTable(String filename, char[] chars) {
        byte[] bytes = new byte[chars.length * 2];

        for (int i = 0; i < chars.length; ++i) {
            bytes[2 * i] = (byte) (chars[i] >>> 8);
            bytes[2 * i + 1] = (byte) (chars[i] & 255);
        }

        try {
            Throwable var15 = null;
            Object var4 = null;

            try {
                FileOutputStream stream = new FileOutputStream(filename);

                try {
                    stream.write(bytes);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            } catch (Throwable var13) {
                if (var15 == null) {
                    var15 = var13;
                } else if (var15 != var13) {
                    var15.addSuppressed(var13);
                }

                throw var13;
            }
        } catch (IOException var14) {
        }

        System.out.println(filename + " creation complete");
    }

    private static final byte[] buildFileOfByteFor(String filename, String tag, String[] tokens) {
        int var3 = 0;

        while (!tokens[var3++].equals(tag)) {}

        byte[] bytes = new byte[tokens.length];

        int ic;
        String token;
        int c;
        for (ic = 0; !(token = tokens[var3++]).equals("}"); bytes[ic++] = (byte) c) {
            c = Integer.parseInt(token);
        }

        System.arraycopy(bytes, 0, bytes = new byte[ic], 0, ic);
        buildFileForTable(filename, bytes);
        return bytes;
    }

    private static final char[] buildFileOfIntFor(String filename, String tag, String[] tokens) {
        int var3 = 0;

        while (!tokens[var3++].equals(tag)) {}

        char[] chars = new char[tokens.length];

        int ic;
        String token;
        int c;
        for (ic = 0; !(token = tokens[var3++]).equals("}"); chars[ic++] = (char) c) {
            c = Integer.parseInt(token);
        }

        System.arraycopy(chars, 0, chars = new char[ic], 0, ic);
        buildFileForTable(filename, chars);
        return chars;
    }

    private static final void buildFileOfShortFor(String filename, String tag, String[] tokens) {
        int var3 = 0;

        while (!tokens[var3++].equals(tag)) {}

        char[] chars = new char[tokens.length];

        int ic;
        String token;
        int c;
        for (ic = 0; !(token = tokens[var3++]).equals("}"); chars[ic++] = (char) (c + '耀')) {
            c = Integer.parseInt(token);
        }

        System.arraycopy(chars, 0, chars = new char[ic], 0, ic);
        buildFileForTable(filename, chars);
    }

    private static void buildFilesForRecoveryTemplates(
            String indexFilename,
            String templatesFilename,
            char[] newTerminalIndex,
            char[] newNonTerminalIndex,
            String[] newName,
            char[] newLhs,
            String[] tokens) {
        int[] newReverse = computeReverseTable(newTerminalIndex, newNonTerminalIndex, newName);
        char[] newRecoveyTemplatesIndex = new char[newNonTerminalIndex.length];
        char[] newRecoveyTemplates = new char[newNonTerminalIndex.length];
        int newRecoveyTemplatesPtr = 0;

        for (int i = 0; i < tokens.length; i += 3) {
            if ("3".equals(tokens[i])) {
                int length = newRecoveyTemplates.length;
                if (length == newRecoveyTemplatesPtr + 1) {
                    System.arraycopy(
                            newRecoveyTemplates,
                            0,
                            newRecoveyTemplates = new char[length * 2],
                            0,
                            length);
                }

                newRecoveyTemplates[newRecoveyTemplatesPtr++] = 0;
                int index = newLhs[Integer.parseInt(tokens[i + 1])];
                newRecoveyTemplatesIndex[index] = (char) newRecoveyTemplatesPtr;
                String token = tokens[i + 2].trim();
                StringTokenizer st = new StringTokenizer(token, " ");
                String[] terminalNames = new String[st.countTokens()];

                for (int var17 = 0; st.hasMoreTokens(); terminalNames[var17++] = st.nextToken()) {}

                String[] var21 = terminalNames;
                int var20 = terminalNames.length;

                for (int var19 = 0; var19 < var20; ++var19) {
                    String terminalName = var21[var19];
                    int symbol = getSymbol(terminalName, newName, newReverse);
                    if (symbol > -1) {
                        length = newRecoveyTemplates.length;
                        if (length == newRecoveyTemplatesPtr + 1) {
                            System.arraycopy(
                                    newRecoveyTemplates,
                                    0,
                                    newRecoveyTemplates = new char[length * 2],
                                    0,
                                    length);
                        }

                        newRecoveyTemplates[newRecoveyTemplatesPtr++] = (char) symbol;
                    }
                }
            }
        }

        newRecoveyTemplates[newRecoveyTemplatesPtr++] = 0;
        System.arraycopy(
                newRecoveyTemplates,
                0,
                newRecoveyTemplates = new char[newRecoveyTemplatesPtr],
                0,
                newRecoveyTemplatesPtr);
        buildFileForTable(indexFilename, newRecoveyTemplatesIndex);
        buildFileForTable(templatesFilename, newRecoveyTemplates);
    }

    private static void buildFilesForStatementsRecoveryFilter(
            String filename, char[] newNonTerminalIndex, char[] newLhs, String[] tokens) {
        char[] newStatementsRecoveryFilter = new char[newNonTerminalIndex.length];

        for (int i = 0; i < tokens.length; i += 3) {
            if ("4".equals(tokens[i])) {
                int index = newLhs[Integer.parseInt(tokens[i + 1])];
                newStatementsRecoveryFilter[index] = 1;
            }
        }

        buildFileForTable(filename, newStatementsRecoveryFilter);
    }

    public static final void buildFilesFromLPG(String dataFilename, String dataFilename2) {
        char[] contents = CharOperation.NO_CHAR;

        try {
            contents = Util.getFileCharContent(new File(dataFilename), (String) null);
        } catch (IOException var15) {
            System.out.println(Messages.parser_incorrectPath);
            return;
        }

        StringTokenizer st = new StringTokenizer(new String(contents), " \t\n\r[]={,;");
        String[] tokens = new String[st.countTokens()];

        int var5;
        for (var5 = 0; st.hasMoreTokens(); tokens[var5++] = st.nextToken()) {}

        String prefix = "parser";
        int i = 0;
        i++;
        char[] newLhs = buildFileOfIntFor("parser" + i + ".rsc", "lhs", tokens);
        ++i;
        buildFileOfShortFor("parser" + i + ".rsc", "check_table", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "asb", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "asr", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "nasb", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "nasr", tokens);
        ++i;
        char[] newTerminalIndex =
                buildFileOfIntFor("parser" + i + ".rsc", "terminal_index", tokens);
        ++i;
        char[] newNonTerminalIndex =
                buildFileOfIntFor("parser" + i + ".rsc", "non_terminal_index", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "term_action", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "scope_prefix", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "scope_suffix", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "scope_lhs", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "scope_state_set", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "scope_rhs", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "scope_state", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "in_symb", tokens);
        ++i;
        byte[] newRhs = buildFileOfByteFor("parser" + i + ".rsc", "rhs", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "term_check", tokens);
        ++i;
        buildFileOfIntFor("parser" + i + ".rsc", "scope_la", tokens);
        ++i;
        String[] newName = buildFileForName("parser" + i + ".rsc", new String(contents));
        contents = CharOperation.NO_CHAR;

        try {
            contents = Util.getFileCharContent(new File(dataFilename2), (String) null);
        } catch (IOException var14) {
            System.out.println(Messages.parser_incorrectPath);
            return;
        }

        st = new StringTokenizer(new String(contents), "\t\n\r#");
        tokens = new String[st.countTokens()];

        for (var5 = 0; st.hasMoreTokens(); tokens[var5++] = st.nextToken()) {}

        ++i;
        buildFileForCompliance("parser" + i + ".rsc", newRhs.length, tokens);
        buildFileForReadableName(
                "readableNames.props", newLhs, newNonTerminalIndex, newName, tokens);
        ++i;
        String var10000 = "parser" + i + ".rsc";
        ++i;
        buildFilesForRecoveryTemplates(
                var10000,
                "parser" + i + ".rsc",
                newTerminalIndex,
                newNonTerminalIndex,
                newName,
                newLhs,
                tokens);
        ++i;
        buildFilesForStatementsRecoveryFilter(
                "parser" + i + ".rsc", newNonTerminalIndex, newLhs, tokens);
        System.out.println(Messages.parser_moveFiles);
    }

    protected static int[] computeReverseTable(
            char[] newTerminalIndex, char[] newNonTerminalIndex, String[] newName) {
        int[] newReverseTable = new int[newName.length];

        label36:
        for (int j = 0; j < newName.length; ++j) {
            int k;
            for (k = 0; k < newTerminalIndex.length; ++k) {
                if (newTerminalIndex[k] == j) {
                    newReverseTable[j] = k;
                    continue label36;
                }
            }

            for (k = 0; k < newNonTerminalIndex.length; ++k) {
                if (newNonTerminalIndex[k] == j) {
                    newReverseTable[j] = -k;
                    break;
                }
            }
        }

        return newReverseTable;
    }

    private static int getSymbol(String terminalName, String[] newName, int[] newReverse) {
        for (int j = 0; j < newName.length; ++j) {
            if (terminalName.equals(newName[j])) {
                return newReverse[j];
            }
        }

        return -1;
    }

    public static int in_symbol(int state) {
        return in_symb[original_state(state)];
    }

    public static final void initTables() throws IOException {
        String prefix = "parser";
        int i = 0;
        i++;
        lhs = readTable("parser" + i + ".rsc");
        ++i;
        char[] chars = readTable("parser" + i + ".rsc");
        check_table = new short[chars.length];

        for (int c = chars.length; c-- > 0; check_table[c] = (short) (chars[c] - '耀')) {}

        ++i;
        asb = readTable("parser" + i + ".rsc");
        ++i;
        asr = readTable("parser" + i + ".rsc");
        ++i;
        nasb = readTable("parser" + i + ".rsc");
        ++i;
        nasr = readTable("parser" + i + ".rsc");
        ++i;
        terminal_index = readTable("parser" + i + ".rsc");
        ++i;
        non_terminal_index = readTable("parser" + i + ".rsc");
        ++i;
        term_action = readTable("parser" + i + ".rsc");
        ++i;
        scope_prefix = readTable("parser" + i + ".rsc");
        ++i;
        scope_suffix = readTable("parser" + i + ".rsc");
        ++i;
        scope_lhs = readTable("parser" + i + ".rsc");
        ++i;
        scope_state_set = readTable("parser" + i + ".rsc");
        ++i;
        scope_rhs = readTable("parser" + i + ".rsc");
        ++i;
        scope_state = readTable("parser" + i + ".rsc");
        ++i;
        in_symb = readTable("parser" + i + ".rsc");
        ++i;
        rhs = readByteTable("parser" + i + ".rsc");
        ++i;
        term_check = readTable("parser" + i + ".rsc");
        ++i;
        scope_la = readTable("parser" + i + ".rsc");
        ++i;
        name = readNameTable("parser" + i + ".rsc");
        ++i;
        rules_compliance = readLongTable("parser" + i + ".rsc");
        readableName = readReadableNameTable("readableNames.props");
        reverse_index = computeReverseTable(terminal_index, non_terminal_index, name);
        ++i;
        recovery_templates_index = readTable("parser" + i + ".rsc");
        ++i;
        recovery_templates = readTable("parser" + i + ".rsc");
        ++i;
        statements_recovery_filter = readTable("parser" + i + ".rsc");
        base_action = lhs;
    }

    public static int nasi(int state) {
        return nasb[original_state(state)];
    }

    public static int ntAction(int state, int sym) {
        return base_action[state + sym];
    }

    protected static int original_state(int state) {
        return -base_check(state);
    }

    protected static byte[] readByteTable(String filename) throws IOException {
        byte[] bytes = null;
        Object var3 = null;

        try {
            InputStream stream = Parser.class.getResourceAsStream(filename);

            try {
                if (stream == null) {
                    throw new IOException(
                            Messages.bind(Messages.parser_missingFile, (Object) filename));
                }

                bytes = Util.getInputStreamAsByteArray(new BufferedInputStream(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            return bytes;
        } catch (Throwable var10) {
            throw var10;
        }
    }

    protected static long[] readLongTable(String filename) throws IOException {
        byte[] bytes = null;
        Throwable var2 = null;
        Object var3 = null;

        try {
            InputStream stream = Parser.class.getResourceAsStream(filename);

            try {
                if (stream == null) {
                    throw new IOException(
                            Messages.bind(Messages.parser_missingFile, (Object) filename));
                }

                bytes = Util.getInputStreamAsByteArray(new BufferedInputStream(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (Throwable var11) {

            throw var11;
        }

        int length = bytes.length;
        if (length % 8 != 0) {
            throw new IOException(Messages.bind(Messages.parser_corruptedFile, (Object) filename));
        } else {
            long[] longs = new long[length / 8];
            int i = 0;
            int var5 = 0;

            do {
                longs[var5++] =
                        ((long) (bytes[i++] & 255) << 56)
                                + ((long) (bytes[i++] & 255) << 48)
                                + ((long) (bytes[i++] & 255) << 40)
                                + ((long) (bytes[i++] & 255) << 32)
                                + ((long) (bytes[i++] & 255) << 24)
                                + ((long) (bytes[i++] & 255) << 16)
                                + ((long) (bytes[i++] & 255) << 8)
                                + (long) (bytes[i++] & 255);
            } while (i != length);

            return longs;
        }
    }

    protected static String[] readNameTable(String filename) throws IOException {
        char[] contents = readTable(filename);
        char[][] nameAsChar = CharOperation.splitOn('\n', contents);
        String[] result = new String[nameAsChar.length + 1];
        result[0] = null;

        for (int i = 0; i < nameAsChar.length; ++i) {
            result[i + 1] = new String(nameAsChar[i]);
        }

        return result;
    }

    protected static String[] readReadableNameTable(String filename) {
        String[] result = new String[name.length];
        Properties props = new Properties();

        String n;
        try {
            Throwable var3 = null;
            n = null;

            try {
                InputStream is = Parser.class.getResourceAsStream(filename);

                try {
                    props.load(is);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (Throwable var13) {

                throw var13;
            }
        } catch (IOException var14) {
            result = name;
            return result;
        }

        System.arraycopy(name, 0, result, 0, 139);

        for (int i = 138; i < name.length; ++i) {
            n = props.getProperty(name[i]);
            if (n != null && n.length() > 0) {
                result[i] = n;
            } else {
                result[i] = name[i];
            }
        }

        return result;
    }

    protected static char[] readTable(String filename) throws IOException {
        byte[] bytes = null;
        Throwable var2 = null;
        Object var3 = null;

        try {
            InputStream stream = Parser.class.getResourceAsStream(filename);

            try {
                if (stream == null) {
                    throw new IOException(
                            Messages.bind(Messages.parser_missingFile, (Object) filename));
                }

                bytes = Util.getInputStreamAsByteArray(new BufferedInputStream(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (Throwable var11) {
            if (var2 == null) {
                var2 = var11;
            } else if (var2 != var11) {
                var2.addSuppressed(var11);
            }

            throw var11;
        }

        int length = bytes.length;
        if ((length & 1) != 0) {
            throw new IOException(Messages.bind(Messages.parser_corruptedFile, (Object) filename));
        } else {
            char[] chars = new char[length / 2];
            int i = 0;
            int var5 = 0;

            do {
                chars[var5++] = (char) (((bytes[i++] & 255) << 8) + (bytes[i++] & 255));
            } while (i != length);

            return chars;
        }
    }

    public static int tAction(int state, int sym) {
        return term_action[
                term_check[base_action[state] + sym] == sym
                        ? base_action[state] + sym
                        : base_action[state]];
    }

    protected int actFromTokenOrSynthetic(int previousAct) {
        return tAction(previousAct, this.currentToken);
    }

    public Parser() {}

    public Parser(ProblemReporter problemReporter, boolean optimizeStringLiterals) {
        this.problemReporter = problemReporter;
        this.options = problemReporter.options;
        this.optimizeStringLiterals = optimizeStringLiterals;
        this.initializeScanner();
        this.parsingJava8Plus = this.options.sourceLevel >= 3407872L;
        this.parsingJava9Plus = this.options.sourceLevel >= 3473408L;
        this.parsingJava11Plus = this.options.sourceLevel >= 3604480L;
        this.parsingJava14Plus = this.options.sourceLevel >= 3801088L;
        this.parsingJava15Plus = this.options.sourceLevel >= 3866624L;
        this.parsingJava17Plus = this.options.sourceLevel >= 3997696L;
        this.parsingJava18Plus = this.options.sourceLevel >= 4063232L;
        this.parsingJava21Plus = this.options.sourceLevel >= 4259840L;
        this.parsingJava22Plus = this.options.sourceLevel >= 4325376L;
        this.previewEnabled =
                (this.options.sourceLevel >= ClassFileConstants.JDK11)
                        && this.options.enablePreviewFeatures;
        this.astLengthStack = new int[50];
        this.expressionLengthStack = new int[30];
        this.typeAnnotationLengthStack = new int[30];
        this.intStack = new int[50];
        this.identifierStack = new char[30][];
        this.identifierLengthStack = new int[30];
        this.nestedMethod = new int[30];
        this.realBlockStack = new int[30];
        this.identifierPositionStack = new long[30];
        this.variablesCounter = new int[30];
        this.recordNestedMethodLevels = new HashMap();
        this.javadocParser = this.createJavadocParser();
    }

    protected void annotationRecoveryCheckPoint(int start, int end) {
        if (this.lastCheckPoint < end) {
            this.lastCheckPoint = end + 1;
        }
    }

    public void arrayInitializer(int length) {
        ArrayInitializer ai = new ArrayInitializer();
        if (length != 0) {
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    ai.expressions = new Expression[length],
                    0,
                    length);
        }

        this.pushOnExpressionStack(ai);
        ai.sourceEnd = this.endStatementPosition;
        ai.sourceStart = this.intStack[this.intPtr--];
    }

    protected void blockReal() {
        int var10002 = this.realBlockStack[this.realBlockPtr]++;
    }

    public RecoveredElement buildInitialRecoveryState() {
        this.lastCheckPoint = 0;
        this.lastErrorEndPositionBeforeRecovery = this.scanner.currentPosition;
        RecoveredElement element = null;
        if (this.referenceContext instanceof CompilationUnitDeclaration) {
            element = new RecoveredUnit(this.compilationUnit, 0, this);
            this.compilationUnit.currentPackage = null;
            this.compilationUnit.imports = null;
            this.compilationUnit.types = null;
            this.currentToken = 0;
            this.listLength = 0;
            this.listTypeParameterLength = 0;
            this.endPosition = 0;
            this.endStatementPosition = 0;
            return element;
        } else {
            ReferenceContext var3;
            if ((var3 = this.referenceContext) instanceof AbstractMethodDeclaration) {
                AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) var3;
                element = new RecoveredMethod(methodDeclaration, (RecoveredElement) null, 0, this);
                this.lastCheckPoint = methodDeclaration.bodyStart;
                if (this.statementRecoveryActivated) {
                    element = ((RecoveredElement) element).add((Block) (new Block(0)), 0);
                }
            } else {
                ReferenceContext var5;
                if ((var5 = this.referenceContext) instanceof TypeDeclaration) {
                    TypeDeclaration type = (TypeDeclaration) var5;
                    FieldDeclaration[] fieldDeclarations = type.fields;
                    int length = fieldDeclarations == null ? 0 : fieldDeclarations.length;

                    for (int i = 0; i < length; ++i) {
                        FieldDeclaration field = fieldDeclarations[i];
                        if (field != null
                                && field.getKind() == 2
                                && ((Initializer) field).block != null
                                && field.declarationSourceStart <= this.scanner.initialPosition
                                && this.scanner.initialPosition <= field.declarationSourceEnd
                                && this.scanner.eofPosition <= field.declarationSourceEnd + 1) {
                            element =
                                    new RecoveredInitializer(
                                            field, (RecoveredElement) null, 1, this);
                            this.lastCheckPoint = field.declarationSourceStart;
                            break;
                        }
                    }
                }
            }

            if (element == null) {
                return (RecoveredElement) element;
            } else {
                for (int i = 0; i <= this.astPtr; ++i) {
                    ASTNode node = this.astStack[i];
                    if (node instanceof AbstractMethodDeclaration) {
                        AbstractMethodDeclaration method = (AbstractMethodDeclaration) node;
                        if (method.declarationSourceEnd == 0) {
                            element =
                                    ((RecoveredElement) element)
                                            .add((AbstractMethodDeclaration) method, 0);
                            this.lastCheckPoint = method.bodyStart;
                        } else {
                            element =
                                    ((RecoveredElement) element)
                                            .add((AbstractMethodDeclaration) method, 0);
                            this.lastCheckPoint = method.declarationSourceEnd + 1;
                        }
                    } else if (node instanceof Initializer) {
                        Initializer initializer = (Initializer) node;
                        if (initializer.block != null) {
                            if (initializer.declarationSourceEnd == 0) {
                                element =
                                        ((RecoveredElement) element)
                                                .add((FieldDeclaration) initializer, 1);
                                this.lastCheckPoint = initializer.sourceStart;
                            } else {
                                element =
                                        ((RecoveredElement) element)
                                                .add((FieldDeclaration) initializer, 0);
                                this.lastCheckPoint = initializer.declarationSourceEnd + 1;
                            }
                        }
                    } else if (node instanceof FieldDeclaration) {
                        FieldDeclaration field = (FieldDeclaration) node;
                        if (field.declarationSourceEnd == 0) {
                            element = ((RecoveredElement) element).add((FieldDeclaration) field, 0);
                            if (field.initialization == null) {
                                this.lastCheckPoint = field.sourceEnd + 1;
                            } else {
                                this.lastCheckPoint = field.initialization.sourceEnd + 1;
                            }
                        } else {
                            element = ((RecoveredElement) element).add((FieldDeclaration) field, 0);
                            this.lastCheckPoint = field.declarationSourceEnd + 1;
                        }
                    } else if (node instanceof TypeDeclaration) {
                        TypeDeclaration type = (TypeDeclaration) node;
                        if ((type.modifiers & 16384) == 0) {
                            if (type.declarationSourceEnd == 0) {
                                element =
                                        ((RecoveredElement) element).add((TypeDeclaration) type, 0);
                                this.lastCheckPoint = type.bodyStart;
                            } else {
                                element =
                                        ((RecoveredElement) element).add((TypeDeclaration) type, 0);
                                this.lastCheckPoint = type.declarationSourceEnd + 1;
                            }
                        }
                    } else {
                        if (node instanceof ImportReference) {
                            ImportReference importRef = (ImportReference) node;
                            element =
                                    ((RecoveredElement) element)
                                            .add((ImportReference) importRef, 0);
                            this.lastCheckPoint = importRef.declarationSourceEnd + 1;
                        }

                        if (this.statementRecoveryActivated) {
                            if (node instanceof Block) {
                                Block block = (Block) node;
                                element = ((RecoveredElement) element).add((Block) block, 0);
                                this.lastCheckPoint = block.sourceEnd + 1;
                            } else if (node instanceof LocalDeclaration) {
                                LocalDeclaration statement = (LocalDeclaration) node;
                                element =
                                        ((RecoveredElement) element)
                                                .add((LocalDeclaration) statement, 0);
                                this.lastCheckPoint = statement.sourceEnd + 1;
                            } else {
                                if (node instanceof Expression) {
                                    Expression statement = (Expression) node;
                                    if (statement.isTrulyExpression()) {
                                        if (node instanceof Assignment
                                                || node instanceof PrefixExpression
                                                || node instanceof PostfixExpression
                                                || node instanceof MessageSend
                                                || node instanceof AllocationExpression) {
                                            element =
                                                    ((RecoveredElement) element)
                                                            .add((Statement) statement, 0);
                                            if (statement.statementEnd != -1) {
                                                this.lastCheckPoint = statement.statementEnd + 1;
                                            } else {
                                                this.lastCheckPoint = statement.sourceEnd + 1;
                                            }
                                        }
                                        continue;
                                    }
                                }

                                if (node instanceof Statement) {
                                    Statement statement = (Statement) node;
                                    element =
                                            ((RecoveredElement) element)
                                                    .add((Statement) statement, 0);
                                    this.lastCheckPoint = statement.sourceEnd + 1;
                                }
                            }
                        }
                    }
                }

                if (this.statementRecoveryActivated
                        && this.pendingRecoveredType != null
                        && this.scanner.startPosition - 1
                                <= this.pendingRecoveredType.declarationSourceEnd) {
                    element =
                            ((RecoveredElement) element)
                                    .add((TypeDeclaration) this.pendingRecoveredType, 0);
                    this.lastCheckPoint = this.pendingRecoveredType.declarationSourceEnd + 1;
                    this.pendingRecoveredType = null;
                }

                return (RecoveredElement) element;
            }
        }
    }

    protected void checkAndSetModifiers(int flag) {
        if (flag == 2048 && this.parsingJava17Plus) {
            this.problemReporter()
                    .StrictfpNotRequired(
                            this.scanner.startPosition, this.scanner.currentPosition - 1);
        }

        if ((this.modifiers & flag) != 0) {
            this.modifiers |= 4194304;
        }

        this.modifiers |= flag;
        if (this.modifiersSourceStart < 0) {
            this.modifiersSourceStart = this.scanner.startPosition;
        }

        if (this.currentElement != null) {
            this.currentElement.addModifier(flag, this.modifiersSourceStart);
        }
    }

    public void checkComment() {
        if ((!this.diet || this.dietInt != 0) && this.scanner.commentPtr >= 0) {
            this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        }

        int lastComment = this.scanner.commentPtr;
        int lastCommentStart;
        if (this.modifiersSourceStart >= 0
                && this.modifiersSourceStart > this.annotationAsModifierSourceStart) {
            while (lastComment >= 0) {
                lastCommentStart = this.scanner.commentStarts[lastComment];
                if (lastCommentStart < 0) {
                    lastCommentStart = -lastCommentStart;
                }

                if (lastCommentStart <= this.modifiersSourceStart) {
                    break;
                }

                --lastComment;
            }
        }

        if (lastComment >= 0) {
            lastCommentStart = this.scanner.commentStarts[0];
            if (lastCommentStart < 0) {
                lastCommentStart = -lastCommentStart;
            }

            if (this.forStartPosition != 0 || this.forStartPosition < lastCommentStart) {
                this.modifiersSourceStart = lastCommentStart;
            }

            while (lastComment >= 0 && this.scanner.commentStops[lastComment] < 0) {
                --lastComment;
            }

            if (lastComment >= 0 && this.javadocParser != null) {
                int commentEnd = this.scanner.commentStops[lastComment] - 1;
                if (!this.javadocParser.shouldReportProblems) {
                    this.javadocParser.reportProblems = false;
                } else {
                    this.javadocParser.reportProblems =
                            this.currentElement == null || commentEnd > this.lastJavadocEnd;
                }

                if (this.javadocParser.checkDeprecation(lastComment)) {
                    this.checkAndSetModifiers(1048576);
                }

                this.javadoc = this.javadocParser.docComment;
                if (this.currentElement == null) {
                    this.lastJavadocEnd = commentEnd;
                }
            }
        }
    }

    protected void checkNonNLSAfterBodyEnd(int declarationEnd) {
        if (this.scanner.currentPosition - 1 <= declarationEnd) {
            this.scanner.eofPosition =
                    declarationEnd < Integer.MAX_VALUE ? declarationEnd + 1 : declarationEnd;

            try {
                while (this.scanner.getNextToken() != 39) {}
            } catch (InvalidInputException var3) {
            }
        }
    }

    protected void classInstanceCreation(boolean isQualified) {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) == 1
                && this.astStack[this.astPtr] == null) {
            --this.astPtr;
            AllocationExpression alloc = this.newAllocationExpression(isQualified);
            alloc.sourceEnd = this.endPosition;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                this.expressionPtr -= length;
                System.arraycopy(
                        this.expressionStack,
                        this.expressionPtr + 1,
                        alloc.arguments = new Expression[length],
                        0,
                        length);
            }

            alloc.type = this.getTypeReference(0);
            this.checkForDiamond(alloc.type);
            alloc.sourceStart = this.intStack[this.intPtr--];
            this.pushOnExpressionStack(alloc);
        } else {
            this.dispatchDeclarationInto(length);
            TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration) this.astStack[this.astPtr];
            anonymousTypeDeclaration.declarationSourceEnd = this.endStatementPosition;
            anonymousTypeDeclaration.addClinit();
            anonymousTypeDeclaration.bodyEnd = this.endStatementPosition;
            if (anonymousTypeDeclaration.allocation != null) {
                anonymousTypeDeclaration.allocation.sourceEnd = this.endStatementPosition;
                this.checkForDiamond(anonymousTypeDeclaration.allocation.type);
            }

            if (length == 0
                    && !this.containsComment(
                            anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
                anonymousTypeDeclaration.bits |= 8;
            }

            --this.astPtr;
            --this.astLengthPtr;
        }
    }

    protected AllocationExpression newAllocationExpression(boolean isQualified) {
        Object alloc;
        if (isQualified) {
            alloc = new QualifiedAllocationExpression();
        } else {
            alloc = new AllocationExpression();
        }

        return (AllocationExpression) alloc;
    }

    protected void checkForDiamond(TypeReference allocType) {
        if (allocType instanceof ParameterizedSingleTypeReference) {
            ParameterizedSingleTypeReference type = (ParameterizedSingleTypeReference) allocType;
            if (type.typeArguments == TypeReference.NO_TYPE_ARGUMENTS) {
                if (this.options.sourceLevel < 3342336L) {
                    this.problemReporter().diamondNotBelow17(allocType);
                }

                if (this.options.sourceLevel > 3145728L) {
                    type.bits |= 524288;
                }
            }
        } else if (allocType instanceof ParameterizedQualifiedTypeReference) {
            ParameterizedQualifiedTypeReference type =
                    (ParameterizedQualifiedTypeReference) allocType;
            if (type.typeArguments[type.typeArguments.length - 1]
                    == TypeReference.NO_TYPE_ARGUMENTS) {
                if (this.options.sourceLevel < 3342336L) {
                    this.problemReporter()
                            .diamondNotBelow17(allocType, type.typeArguments.length - 1);
                }

                if (this.options.sourceLevel > 3145728L) {
                    type.bits |= 524288;
                }
            }
        }
    }

    protected ParameterizedQualifiedTypeReference computeQualifiedGenericsFromRightSide(
            TypeReference rightSide, int dim, Annotation[][] annotationsOnDimensions) {
        int nameSize = this.identifierLengthStack[this.identifierLengthPtr];
        int tokensSize = nameSize;
        if (rightSide instanceof ParameterizedSingleTypeReference) {
            tokensSize = nameSize + 1;
        } else if (rightSide instanceof SingleTypeReference) {
            tokensSize = nameSize + 1;
        } else if (rightSide instanceof QualifiedTypeReference) {
            tokensSize = nameSize + ((QualifiedTypeReference) rightSide).tokens.length;
        }

        TypeReference[][] typeArguments = new TypeReference[tokensSize][];
        char[][] tokens = new char[tokensSize][];
        long[] positions = new long[tokensSize];
        Annotation[][] typeAnnotations = null;
        if (rightSide instanceof ParameterizedSingleTypeReference) {
            ParameterizedSingleTypeReference singleParameterizedTypeReference =
                    (ParameterizedSingleTypeReference) rightSide;
            tokens[nameSize] = singleParameterizedTypeReference.token;
            positions[nameSize] =
                    ((long) singleParameterizedTypeReference.sourceStart << 32)
                            + (long) singleParameterizedTypeReference.sourceEnd;
            typeArguments[nameSize] = singleParameterizedTypeReference.typeArguments;
            if (singleParameterizedTypeReference.annotations != null) {
                typeAnnotations = new Annotation[tokensSize][];
                typeAnnotations[nameSize] = singleParameterizedTypeReference.annotations[0];
            }
        } else if (rightSide instanceof SingleTypeReference) {
            SingleTypeReference singleTypeReference = (SingleTypeReference) rightSide;
            tokens[nameSize] = singleTypeReference.token;
            positions[nameSize] =
                    ((long) singleTypeReference.sourceStart << 32)
                            + (long) singleTypeReference.sourceEnd;
            if (singleTypeReference.annotations != null) {
                typeAnnotations = new Annotation[tokensSize][];
                typeAnnotations[nameSize] = singleTypeReference.annotations[0];
            }
        } else {
            char[][] rightSideTokens;
            long[] rightSidePositions;
            Annotation[][] rightSideAnnotations;
            if (rightSide instanceof ParameterizedQualifiedTypeReference) {
                ParameterizedQualifiedTypeReference parameterizedTypeReference =
                        (ParameterizedQualifiedTypeReference) rightSide;
                TypeReference[][] rightSideTypeArguments = parameterizedTypeReference.typeArguments;
                System.arraycopy(
                        rightSideTypeArguments,
                        0,
                        typeArguments,
                        nameSize,
                        rightSideTypeArguments.length);
                rightSideTokens = parameterizedTypeReference.tokens;
                System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
                rightSidePositions = parameterizedTypeReference.sourcePositions;
                System.arraycopy(
                        rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
                rightSideAnnotations = parameterizedTypeReference.annotations;
                if (rightSideAnnotations != null) {
                    typeAnnotations = new Annotation[tokensSize][];
                    System.arraycopy(
                            rightSideAnnotations,
                            0,
                            typeAnnotations,
                            nameSize,
                            rightSideAnnotations.length);
                }
            } else if (rightSide instanceof QualifiedTypeReference) {
                QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) rightSide;
                rightSideTokens = qualifiedTypeReference.tokens;
                System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
                rightSidePositions = qualifiedTypeReference.sourcePositions;
                System.arraycopy(
                        rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
                rightSideAnnotations = qualifiedTypeReference.annotations;
                if (rightSideAnnotations != null) {
                    typeAnnotations = new Annotation[tokensSize][];
                    System.arraycopy(
                            rightSideAnnotations,
                            0,
                            typeAnnotations,
                            nameSize,
                            rightSideAnnotations.length);
                }
            }
        }

        int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        TypeReference[] currentTypeArguments = new TypeReference[currentTypeArgumentsLength];
        this.genericsPtr -= currentTypeArgumentsLength;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                currentTypeArguments,
                0,
                currentTypeArgumentsLength);
        if (nameSize == 1) {
            tokens[0] = this.identifierStack[this.identifierPtr];
            positions[0] = this.identifierPositionStack[this.identifierPtr--];
            typeArguments[0] = currentTypeArguments;
        } else {
            this.identifierPtr -= nameSize;
            System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, nameSize);
            System.arraycopy(
                    this.identifierPositionStack, this.identifierPtr + 1, positions, 0, nameSize);
            typeArguments[nameSize - 1] = currentTypeArguments;
        }

        --this.identifierLengthPtr;

        ParameterizedQualifiedTypeReference typeRef;
        for (typeRef =
                        new ParameterizedQualifiedTypeReference(
                                tokens, typeArguments, dim, annotationsOnDimensions, positions);
                nameSize > 0;
                --nameSize) {
            int length;
            if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
                if (typeAnnotations == null) {
                    typeAnnotations = new Annotation[tokensSize][];
                }

                System.arraycopy(
                        this.typeAnnotationStack,
                        (this.typeAnnotationPtr -= length) + 1,
                        typeAnnotations[nameSize - 1] = new Annotation[length],
                        0,
                        length);
                if (nameSize == 1) {
                    typeRef.sourceStart = typeAnnotations[0][0].sourceStart;
                }
            }
        }

        if ((typeRef.annotations = typeAnnotations) != null) {
            typeRef.bits |= 1048576;
        }

        return typeRef;
    }

    protected void concatExpressionLists() {
        int var10002 = this.expressionLengthStack[--this.expressionLengthPtr]++;
    }

    protected void concatGenericsLists() {
        int[] var10000 = this.genericsLengthStack;
        int var10001 = this.genericsLengthPtr - 1;
        var10000[var10001] += this.genericsLengthStack[this.genericsLengthPtr--];
    }

    protected void concatNodeLists() {
        int[] var10000 = this.astLengthStack;
        int var10001 = this.astLengthPtr - 1;
        var10000[var10001] += this.astLengthStack[this.astLengthPtr--];
    }

    protected void consumeAdditionalBound() {
        this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
    }

    protected void consumeAdditionalBound1() {}

    protected void consumeAdditionalBoundList() {
        this.concatGenericsLists();
    }

    protected void consumeAdditionalBoundList1() {
        this.concatGenericsLists();
    }

    protected boolean isIndirectlyInsideLambdaExpression() {
        return false;
    }

    protected void consumeAllocationHeader() {
        if (this.currentElement != null) {
            if (this.currentToken == 63) {
                TypeDeclaration anonymousType =
                        new TypeDeclaration(this.compilationUnit.compilationResult);
                anonymousType.name = CharOperation.NO_CHAR;
                anonymousType.bits |= 768;
                anonymousType.sourceStart = this.intStack[this.intPtr--];
                anonymousType.declarationSourceStart = anonymousType.sourceStart;
                anonymousType.sourceEnd = this.rParenPos;
                QualifiedAllocationExpression alloc =
                        new QualifiedAllocationExpression(anonymousType);
                alloc.type = this.getTypeReference(0);
                alloc.sourceStart = anonymousType.sourceStart;
                alloc.sourceEnd = anonymousType.sourceEnd;
                this.lastCheckPoint = anonymousType.bodyStart = this.scanner.currentPosition;
                this.currentElement = this.currentElement.add((TypeDeclaration) anonymousType, 0);
                this.lastIgnoredToken = -1;
                if (this.isIndirectlyInsideLambdaExpression()) {
                    this.ignoreNextOpeningBrace = true;
                } else {
                    this.currentToken = 0;
                }

            } else {
                this.lastCheckPoint = this.scanner.startPosition;
                this.restartRecovery = true;
            }
        }
    }

    protected void consumeAnnotationAsModifier() {
        Expression expression = this.expressionStack[this.expressionPtr];
        int sourceStart = expression.sourceStart;
        if (this.modifiersSourceStart < 0) {
            this.modifiersSourceStart = sourceStart;
            this.annotationAsModifierSourceStart = sourceStart;
        }
    }

    protected void consumeAnnotationName() {
        if (this.currentElement != null && !this.expectTypeAnnotation) {
            int start = this.intStack[this.intPtr];
            int end = (int) (this.identifierPositionStack[this.identifierPtr] & 4294967295L);
            this.annotationRecoveryCheckPoint(start, end);
            if (this.annotationRecoveryActivated) {
                this.currentElement =
                        this.currentElement.addAnnotationName(
                                this.identifierPtr, this.identifierLengthPtr, start, 0);
            }
        }

        this.recordStringLiterals = false;
        this.expectTypeAnnotation = false;
    }

    protected void consumeAnnotationTypeDeclaration() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.dispatchDeclarationInto(length);
        }

        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        typeDecl.checkConstructors(this);
        if (this.scanner.containsAssertKeyword) {
            typeDecl.bits |= 1;
        }

        typeDecl.addClinit();
        typeDecl.bodyEnd = this.endStatementPosition;
        if (length == 0 && !this.containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
            typeDecl.bits |= 8;
        }

        typeDecl.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeAnnotationTypeDeclarationHeader() {
        TypeDeclaration annotationTypeDeclaration = (TypeDeclaration) this.astStack[this.astPtr];
        if (this.currentToken == 63) {
            annotationTypeDeclaration.bodyStart = this.scanner.currentPosition;
        }

        if (this.currentElement != null) {
            this.restartRecovery = true;
        }

        this.scanner.commentPtr = -1;
    }

    protected void consumeAnnotationTypeDeclarationHeaderName() {
        TypeDeclaration annotationTypeDeclaration =
                new TypeDeclaration(this.compilationUnit.compilationResult);
        if (this.nestedMethod[this.nestedType] == 0) {
            if (this.nestedType != 0) {
                annotationTypeDeclaration.bits |= 1024;
            }
        } else {
            annotationTypeDeclaration.bits |= 256;
            this.markEnclosingMemberWithLocalType();
            this.blockReal();
        }

        long pos = this.identifierPositionStack[this.identifierPtr];
        annotationTypeDeclaration.sourceEnd = (int) pos;
        annotationTypeDeclaration.sourceStart = (int) (pos >>> 32);
        annotationTypeDeclaration.name = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        --this.intPtr;
        --this.intPtr;
        annotationTypeDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
        annotationTypeDeclaration.modifiers = this.intStack[this.intPtr--] | 8192 | 512;
        int length;
        if (annotationTypeDeclaration.modifiersSourceStart >= 0) {
            annotationTypeDeclaration.declarationSourceStart =
                    annotationTypeDeclaration.modifiersSourceStart;
            --this.intPtr;
        } else {
            length = this.intStack[this.intPtr--];
            annotationTypeDeclaration.declarationSourceStart = length;
        }

        if ((annotationTypeDeclaration.bits & 1024) == 0
                && (annotationTypeDeclaration.bits & 256) == 0
                && this.compilationUnit != null
                && !CharOperation.equals(
                        annotationTypeDeclaration.name, this.compilationUnit.getMainTypeName())) {
            annotationTypeDeclaration.bits |= 4096;
        }

        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    annotationTypeDeclaration.annotations = new Annotation[length],
                    0,
                    length);
        }

        annotationTypeDeclaration.bodyStart = annotationTypeDeclaration.sourceEnd + 1;
        annotationTypeDeclaration.javadoc = this.javadoc;
        this.javadoc = null;
        this.pushOnAstStack(annotationTypeDeclaration);
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter().invalidUsageOfAnnotationDeclarations(annotationTypeDeclaration);
        }

        if (this.currentElement != null) {
            this.lastCheckPoint = annotationTypeDeclaration.bodyStart;
            this.currentElement =
                    this.currentElement.add((TypeDeclaration) annotationTypeDeclaration, 0);
            this.lastIgnoredToken = -1;
        }
    }

    protected void consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters() {
        TypeDeclaration annotationTypeDeclaration =
                new TypeDeclaration(this.compilationUnit.compilationResult);
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                annotationTypeDeclaration.typeParameters = new TypeParameter[length],
                0,
                length);
        this.problemReporter()
                .invalidUsageOfTypeParametersForAnnotationDeclaration(annotationTypeDeclaration);
        annotationTypeDeclaration.bodyStart =
                annotationTypeDeclaration.typeParameters[length - 1].declarationSourceEnd + 1;
        this.listTypeParameterLength = 0;
        if (this.nestedMethod[this.nestedType] == 0) {
            if (this.nestedType != 0) {
                annotationTypeDeclaration.bits |= 1024;
            }
        } else {
            annotationTypeDeclaration.bits |= 256;
            this.markEnclosingMemberWithLocalType();
            this.blockReal();
        }

        long pos = this.identifierPositionStack[this.identifierPtr];
        annotationTypeDeclaration.sourceEnd = (int) pos;
        annotationTypeDeclaration.sourceStart = (int) (pos >>> 32);
        annotationTypeDeclaration.name = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        --this.intPtr;
        --this.intPtr;
        annotationTypeDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
        annotationTypeDeclaration.modifiers = this.intStack[this.intPtr--] | 8192 | 512;
        if (annotationTypeDeclaration.modifiersSourceStart >= 0) {
            annotationTypeDeclaration.declarationSourceStart =
                    annotationTypeDeclaration.modifiersSourceStart;
            --this.intPtr;
        } else {
            int atPosition = this.intStack[this.intPtr--];
            annotationTypeDeclaration.declarationSourceStart = atPosition;
        }

        if ((annotationTypeDeclaration.bits & 1024) == 0
                && (annotationTypeDeclaration.bits & 256) == 0
                && this.compilationUnit != null
                && !CharOperation.equals(
                        annotationTypeDeclaration.name, this.compilationUnit.getMainTypeName())) {
            annotationTypeDeclaration.bits |= 4096;
        }

        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    annotationTypeDeclaration.annotations = new Annotation[length],
                    0,
                    length);
        }

        annotationTypeDeclaration.javadoc = this.javadoc;
        this.javadoc = null;
        this.pushOnAstStack(annotationTypeDeclaration);
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter().invalidUsageOfAnnotationDeclarations(annotationTypeDeclaration);
        }

        if (this.currentElement != null) {
            this.lastCheckPoint = annotationTypeDeclaration.bodyStart;
            this.currentElement =
                    this.currentElement.add((TypeDeclaration) annotationTypeDeclaration, 0);
            this.lastIgnoredToken = -1;
        }
    }

    protected void consumeAnnotationTypeMemberDeclaration() {
        MethodDeclaration annotationTypeMemberDeclaration =
                (MethodDeclaration) this.astStack[this.astPtr];
        annotationTypeMemberDeclaration.modifiers |= 16777216;
        int declarationEndPosition = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        annotationTypeMemberDeclaration.bodyStart = this.endStatementPosition;
        annotationTypeMemberDeclaration.bodyEnd = declarationEndPosition;
        annotationTypeMemberDeclaration.declarationSourceEnd = declarationEndPosition;
    }

    protected void consumeAnnotationTypeMemberDeclarations() {
        this.concatNodeLists();
    }

    protected void consumeAnnotationTypeMemberDeclarationsopt() {
        --this.nestedType;
    }

    protected void consumeArgumentList() {
        this.concatExpressionLists();
    }

    protected void consumeArguments() {
        this.pushOnIntStack(this.rParenPos);
    }

    protected void consumeArrayAccess(boolean unspecifiedReference) {
        Expression exp;
        if (unspecifiedReference) {
            exp =
                    this.expressionStack[this.expressionPtr] =
                            new ArrayReference(
                                    this.getUnspecifiedReferenceOptimized(),
                                    this.expressionStack[this.expressionPtr]);
        } else {
            --this.expressionPtr;
            --this.expressionLengthPtr;
            exp =
                    this.expressionStack[this.expressionPtr] =
                            new ArrayReference(
                                    this.expressionStack[this.expressionPtr],
                                    this.expressionStack[this.expressionPtr + 1]);
        }

        exp.sourceEnd = this.endStatementPosition;
    }

    protected void consumeArrayCreationExpressionWithInitializer() {
        ArrayAllocationExpression arrayAllocation = new ArrayAllocationExpression();
        --this.expressionLengthPtr;
        arrayAllocation.initializer = (ArrayInitializer) this.expressionStack[this.expressionPtr--];
        int length = this.expressionLengthStack[this.expressionLengthPtr--];
        this.expressionPtr -= length;
        System.arraycopy(
                this.expressionStack,
                this.expressionPtr + 1,
                arrayAllocation.dimensions = new Expression[length],
                0,
                length);
        Annotation[][] annotationsOnDimensions = this.getAnnotationsOnDimensions(length);
        arrayAllocation.annotationsOnDimensions = annotationsOnDimensions;
        arrayAllocation.type = this.getTypeReference(0);
        TypeReference var10000 = arrayAllocation.type;
        var10000.bits |= 1073741824;
        if (annotationsOnDimensions != null) {
            arrayAllocation.bits |= 1048576;
            var10000 = arrayAllocation.type;
            var10000.bits |= 1048576;
        }

        arrayAllocation.sourceStart = this.intStack[this.intPtr--];
        if (arrayAllocation.initializer == null) {
            arrayAllocation.sourceEnd = this.endStatementPosition;
        } else {
            arrayAllocation.sourceEnd = arrayAllocation.initializer.sourceEnd;
        }

        this.pushOnExpressionStack(arrayAllocation);
    }

    protected void consumeArrayCreationExpressionWithoutInitializer() {
        ArrayAllocationExpression arrayAllocation = new ArrayAllocationExpression();
        int length = this.expressionLengthStack[this.expressionLengthPtr--];
        this.expressionPtr -= length;
        System.arraycopy(
                this.expressionStack,
                this.expressionPtr + 1,
                arrayAllocation.dimensions = new Expression[length],
                0,
                length);
        Annotation[][] annotationsOnDimensions = this.getAnnotationsOnDimensions(length);
        arrayAllocation.annotationsOnDimensions = annotationsOnDimensions;
        arrayAllocation.type = this.getTypeReference(0);
        TypeReference var10000 = arrayAllocation.type;
        var10000.bits |= 1073741824;
        if (annotationsOnDimensions != null) {
            arrayAllocation.bits |= 1048576;
            var10000 = arrayAllocation.type;
            var10000.bits |= 1048576;
        }

        arrayAllocation.sourceStart = this.intStack[this.intPtr--];
        if (arrayAllocation.initializer == null) {
            arrayAllocation.sourceEnd = this.endStatementPosition;
        } else {
            arrayAllocation.sourceEnd = arrayAllocation.initializer.sourceEnd;
        }

        this.pushOnExpressionStack(arrayAllocation);
    }

    protected void consumeArrayCreationHeader() {}

    protected void consumeArrayInitializer() {
        this.arrayInitializer(this.expressionLengthStack[this.expressionLengthPtr--]);
    }

    protected void consumeArrayTypeWithTypeArgumentsName() {
        int[] var10000 = this.genericsIdentifiersLengthStack;
        int var10001 = this.genericsIdentifiersLengthPtr;
        var10000[var10001] += this.identifierLengthStack[this.identifierLengthPtr];
        this.pushOnGenericsLengthStack(0);
    }

    protected void consumeAssertStatement() {
        this.expressionLengthPtr -= 2;
        this.pushOnAstStack(
                new AssertStatement(
                        this.expressionStack[this.expressionPtr--],
                        this.expressionStack[this.expressionPtr--],
                        this.intStack[this.intPtr--]));
    }

    protected void consumeAssignment() {
        int op = this.intStack[this.intPtr--];
        --this.expressionPtr;
        --this.expressionLengthPtr;
        Expression expression = this.expressionStack[this.expressionPtr + 1];
        this.expressionStack[this.expressionPtr] =
                (Expression)
                        (op != 21
                                ? new CompoundAssignment(
                                        this.expressionStack[this.expressionPtr],
                                        expression,
                                        op,
                                        expression.sourceEnd)
                                : new Assignment(
                                        this.expressionStack[this.expressionPtr],
                                        expression,
                                        expression.sourceEnd));
        if (this.pendingRecoveredType != null) {
            if (this.pendingRecoveredType.allocation != null
                    && this.scanner.startPosition - 1
                            <= this.pendingRecoveredType.declarationSourceEnd) {
                this.expressionStack[this.expressionPtr] = this.pendingRecoveredType.allocation;
                this.pendingRecoveredType = null;
                return;
            }

            this.pendingRecoveredType = null;
        }
    }

    protected void consumeAssignmentOperator(int pos) {
        this.pushOnIntStack(pos);
    }

    protected void consumeBinaryExpression(int op) {
        --this.expressionPtr;
        --this.expressionLengthPtr;
        Expression expr1 = this.expressionStack[this.expressionPtr];
        Expression expr2 = this.expressionStack[this.expressionPtr + 1];
        switch (op) {
            case 0:
                this.expressionStack[this.expressionPtr] = new AND_AND_Expression(expr1, expr2, op);
                break;
            case 1:
                this.expressionStack[this.expressionPtr] = new OR_OR_Expression(expr1, expr2, op);
                break;
            case 4:
            case 15:
                --this.intPtr;
                this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
                break;
            case 14:
                StringLiteral string1;
                CombinedBinaryExpression expr;
                if (this.optimizeStringLiterals) {
                    if (expr1 instanceof StringLiteral) {
                        string1 = (StringLiteral) expr1;
                        if ((expr1.bits & 534773760) >> 21 == 0) {
                            if (expr2 instanceof CharLiteral) {
                                CharLiteral charLiteral = (CharLiteral) expr2;
                                this.expressionStack[this.expressionPtr] =
                                        string1.extendWith(charLiteral);
                            } else if (expr2 instanceof StringLiteral) {
                                StringLiteral string2 = (StringLiteral) expr2;
                                this.expressionStack[this.expressionPtr] =
                                        string1.extendWith(string2);
                            } else {
                                this.expressionStack[this.expressionPtr] =
                                        new BinaryExpression(expr1, expr2, 14);
                            }
                        } else {
                            this.expressionStack[this.expressionPtr] =
                                    new BinaryExpression(expr1, expr2, 14);
                        }
                    } else if (expr1 instanceof CombinedBinaryExpression) {
                        expr = (CombinedBinaryExpression) expr1;
                        if (expr.arity < expr.arityMax) {
                            expr.left = new BinaryExpression(expr);
                            ++expr.arity;
                        } else {
                            expr.left = new CombinedBinaryExpression(expr);
                            expr.arity = 0;
                            expr.tuneArityMax();
                        }

                        expr.right = expr2;
                        expr.sourceEnd = expr2.sourceEnd;
                        this.expressionStack[this.expressionPtr] = expr;
                    } else if (expr1 instanceof BinaryExpression
                            && (expr1.bits & 7936) >> 8 == 14) {
                        this.expressionStack[this.expressionPtr] =
                                new CombinedBinaryExpression(expr1, expr2, 14, 1);
                    } else {
                        this.expressionStack[this.expressionPtr] =
                                new BinaryExpression(expr1, expr2, 14);
                    }
                } else if (expr1 instanceof StringLiteral) {
                    string1 = (StringLiteral) expr1;
                    if (expr2 instanceof StringLiteral && (expr1.bits & 534773760) >> 21 == 0) {
                        this.expressionStack[this.expressionPtr] =
                                string1.extendsWith((StringLiteral) expr2);
                    } else {
                        this.expressionStack[this.expressionPtr] =
                                new BinaryExpression(expr1, expr2, 14);
                    }
                } else if (expr1 instanceof CombinedBinaryExpression) {
                    expr = (CombinedBinaryExpression) expr1;
                    if (expr.arity < expr.arityMax) {
                        expr.left = new BinaryExpression(expr);
                        expr.bits &= -534773761;
                        ++expr.arity;
                    } else {
                        expr.left = new CombinedBinaryExpression(expr);
                        expr.bits &= -534773761;
                        expr.arity = 0;
                        expr.tuneArityMax();
                    }

                    expr.right = expr2;
                    expr.sourceEnd = expr2.sourceEnd;
                    this.expressionStack[this.expressionPtr] = expr;
                } else if (expr1 instanceof BinaryExpression && (expr1.bits & 7936) >> 8 == 14) {
                    this.expressionStack[this.expressionPtr] =
                            new CombinedBinaryExpression(expr1, expr2, 14, 1);
                } else {
                    this.expressionStack[this.expressionPtr] =
                            new BinaryExpression(expr1, expr2, 14);
                }
                break;
            default:
                this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
        }
    }

    protected void consumeBinaryExpressionWithName(int op) {
        this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
        --this.expressionPtr;
        --this.expressionLengthPtr;
        Expression expr1 = this.expressionStack[this.expressionPtr + 1];
        Expression expr2 = this.expressionStack[this.expressionPtr];
        switch (op) {
            case 0:
                this.expressionStack[this.expressionPtr] = new AND_AND_Expression(expr1, expr2, op);
                break;
            case 1:
                this.expressionStack[this.expressionPtr] = new OR_OR_Expression(expr1, expr2, op);
                break;
            case 4:
            case 15:
                --this.intPtr;
                this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
                break;
            case 14:
                StringLiteral string1;
                if (this.optimizeStringLiterals) {
                    if (expr1 instanceof StringLiteral) {
                        string1 = (StringLiteral) expr1;
                        if ((expr1.bits & 534773760) >> 21 == 0) {
                            if (expr2 instanceof CharLiteral) {
                                CharLiteral char2 = (CharLiteral) expr2;
                                this.expressionStack[this.expressionPtr] =
                                        string1.extendWith(char2);
                            } else if (expr2 instanceof StringLiteral) {
                                StringLiteral string2 = (StringLiteral) expr2;
                                this.expressionStack[this.expressionPtr] =
                                        string1.extendWith(string2);
                            } else {
                                this.expressionStack[this.expressionPtr] =
                                        new BinaryExpression(expr1, expr2, 14);
                            }
                            break;
                        }
                    }

                    this.expressionStack[this.expressionPtr] =
                            new BinaryExpression(expr1, expr2, 14);
                } else if (expr1 instanceof StringLiteral) {
                    string1 = (StringLiteral) expr1;
                    if (expr2 instanceof StringLiteral) {
                        StringLiteral string2 = (StringLiteral) expr2;
                        if ((expr1.bits & 534773760) >> 21 == 0) {
                            this.expressionStack[this.expressionPtr] = string1.extendsWith(string2);
                            break;
                        }
                    }

                    this.expressionStack[this.expressionPtr] =
                            new BinaryExpression(expr1, expr2, op);
                } else {
                    this.expressionStack[this.expressionPtr] =
                            new BinaryExpression(expr1, expr2, op);
                }
                break;
            default:
                this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
        }
    }

    protected void consumeBlock() {
        int statementsLength = this.astLengthStack[this.astLengthPtr--];
        Block block;
        if (statementsLength == 0) {
            block = new Block(0);
            block.sourceStart = this.intStack[this.intPtr--];
            block.sourceEnd = this.endStatementPosition;
            if (!this.containsComment(block.sourceStart, block.sourceEnd)) {
                block.bits |= 8;
            }

            --this.realBlockPtr;
        } else {
            block = new Block(this.realBlockStack[this.realBlockPtr--]);
            this.astPtr -= statementsLength;
            System.arraycopy(
                    this.astStack,
                    this.astPtr + 1,
                    block.statements = new Statement[statementsLength],
                    0,
                    statementsLength);
            block.sourceStart = this.intStack[this.intPtr--];
            block.sourceEnd = this.endStatementPosition;
        }

        if (this.currentElement instanceof RecoveredBlock
                && this.currentElement.getLastStart() == block.sourceStart) {
            this.currentElement.updateSourceEndIfNecessary(block.sourceEnd);
        }

        this.pushOnAstStack(block);
    }

    protected void consumeBlockStatement() {}

    protected void consumeBlockStatements() {
        this.concatNodeLists();
    }

    protected void consumeCastExpressionLL1() {
        --this.expressionPtr;
        CastExpression cast;
        Expression exp;
        this.expressionStack[this.expressionPtr] =
                cast =
                        new CastExpression(
                                exp = this.expressionStack[this.expressionPtr + 1],
                                (TypeReference) this.expressionStack[this.expressionPtr]);
        --this.expressionLengthPtr;
        this.updateSourcePosition(cast);
        cast.sourceEnd = exp.sourceEnd;
    }

    public IntersectionCastTypeReference createIntersectionCastTypeReference(
            TypeReference[] typeReferences) {
        if (this.options.sourceLevel < 3407872L) {
            this.problemReporter().intersectionCastNotBelow18(typeReferences);
        }

        return new IntersectionCastTypeReference(typeReferences);
    }

    protected void consumeCastExpressionLL1WithBounds() {
        Expression exp = this.expressionStack[this.expressionPtr--];
        --this.expressionLengthPtr;
        int length;
        TypeReference[] bounds =
                new TypeReference[length = this.expressionLengthStack[this.expressionLengthPtr]];
        System.arraycopy(this.expressionStack, this.expressionPtr -= length - 1, bounds, 0, length);
        CastExpression cast;
        this.expressionStack[this.expressionPtr] =
                cast = new CastExpression(exp, this.createIntersectionCastTypeReference(bounds));
        this.expressionLengthStack[this.expressionLengthPtr] = 1;
        this.updateSourcePosition(cast);
        cast.sourceEnd = exp.sourceEnd;
    }

    protected void consumeCastExpressionWithGenericsArray() {
        TypeReference[] bounds = null;
        int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        if (additionalBoundsLength > 0) {
            bounds = new TypeReference[additionalBoundsLength + 1];
            this.genericsPtr -= additionalBoundsLength;
            System.arraycopy(
                    this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
        }

        int end = this.intStack[this.intPtr--];
        int dim = this.intStack[this.intPtr--];
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        Object castType;
        if (additionalBoundsLength > 0) {
            bounds[0] = this.getTypeReference(dim);
            castType = this.createIntersectionCastTypeReference(bounds);
        } else {
            castType = this.getTypeReference(dim);
        }

        Expression exp;
        CastExpression cast;
        this.expressionStack[this.expressionPtr] =
                cast =
                        new CastExpression(
                                exp = this.expressionStack[this.expressionPtr],
                                (TypeReference) castType);
        --this.intPtr;
        ((TypeReference) castType).sourceEnd = end - 1;
        ((TypeReference) castType).sourceStart =
                (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
        cast.sourceEnd = exp.sourceEnd;
    }

    protected void consumeCastExpressionWithNameArray() {
        int end = this.intStack[this.intPtr--];
        TypeReference[] bounds = null;
        int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        if (additionalBoundsLength > 0) {
            bounds = new TypeReference[additionalBoundsLength + 1];
            this.genericsPtr -= additionalBoundsLength;
            System.arraycopy(
                    this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
        }

        this.pushOnGenericsLengthStack(0);
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        Object castType;
        if (additionalBoundsLength > 0) {
            bounds[0] = this.getTypeReference(this.intStack[this.intPtr--]);
            castType = this.createIntersectionCastTypeReference(bounds);
        } else {
            castType = this.getTypeReference(this.intStack[this.intPtr--]);
        }

        Expression exp;
        CastExpression cast;
        this.expressionStack[this.expressionPtr] =
                cast =
                        new CastExpression(
                                exp = this.expressionStack[this.expressionPtr],
                                (TypeReference) castType);
        ((TypeReference) castType).sourceEnd = end - 1;
        ((TypeReference) castType).sourceStart =
                (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
        cast.sourceEnd = exp.sourceEnd;
    }

    protected void consumeCastExpressionWithPrimitiveType() {
        TypeReference[] bounds = null;
        int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        if (additionalBoundsLength > 0) {
            bounds = new TypeReference[additionalBoundsLength + 1];
            this.genericsPtr -= additionalBoundsLength;
            System.arraycopy(
                    this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
        }

        int end = this.intStack[this.intPtr--];
        Object castType;
        if (additionalBoundsLength > 0) {
            bounds[0] = this.getTypeReference(this.intStack[this.intPtr--]);
            castType = this.createIntersectionCastTypeReference(bounds);
        } else {
            castType = this.getTypeReference(this.intStack[this.intPtr--]);
        }

        Expression exp;
        CastExpression cast;
        this.expressionStack[this.expressionPtr] =
                cast =
                        new CastExpression(
                                exp = this.expressionStack[this.expressionPtr],
                                (TypeReference) castType);
        ((TypeReference) castType).sourceEnd = end - 1;
        ((TypeReference) castType).sourceStart =
                (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
        cast.sourceEnd = exp.sourceEnd;
    }

    protected void consumeCastExpressionWithQualifiedGenericsArray() {
        TypeReference[] bounds = null;
        int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        if (additionalBoundsLength > 0) {
            bounds = new TypeReference[additionalBoundsLength + 1];
            this.genericsPtr -= additionalBoundsLength;
            System.arraycopy(
                    this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
        }

        int end = this.intStack[this.intPtr--];
        int dim = this.intStack[this.intPtr--];
        Annotation[][] annotationsOnDimensions =
                dim == 0 ? null : this.getAnnotationsOnDimensions(dim);
        TypeReference rightSide = this.getTypeReference(0);
        TypeReference castType =
                this.computeQualifiedGenericsFromRightSide(rightSide, dim, annotationsOnDimensions);
        if (additionalBoundsLength > 0) {
            bounds[0] = (TypeReference) castType;
            castType = this.createIntersectionCastTypeReference(bounds);
        }

        --this.intPtr;
        Expression exp;
        CastExpression cast;
        this.expressionStack[this.expressionPtr] =
                cast =
                        new CastExpression(
                                exp = this.expressionStack[this.expressionPtr],
                                (TypeReference) castType);
        ((TypeReference) castType).sourceEnd = end - 1;
        ((TypeReference) castType).sourceStart =
                (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
        cast.sourceEnd = exp.sourceEnd;
    }

    protected void consumeCatches() {
        this.optimizedConcatNodeLists();
    }

    protected void consumeCatchFormalParameter() {
        --this.identifierLengthPtr;
        char[] identifierName = this.identifierStack[this.identifierPtr];
        long namePositions = this.identifierPositionStack[this.identifierPtr--];
        int extendedDimensions = this.intStack[this.intPtr--];
        TypeReference type = (TypeReference) this.astStack[this.astPtr--];
        if (extendedDimensions > 0) {
            type =
                    this.augmentTypeWithAdditionalDimensions(
                            type, extendedDimensions, (Annotation[][]) null, false);
            type.sourceEnd = this.endPosition;
            if (type instanceof UnionTypeReference) {
                this.problemReporter().illegalArrayOfUnionType(identifierName, type);
            }
        }

        --this.astLengthPtr;
        int modifierPositions = this.intStack[this.intPtr--];
        --this.intPtr;
        Argument arg =
                new Argument(
                        identifierName,
                        namePositions,
                        type,
                        this.intStack[this.intPtr + 1] & -1048577);
        arg.bits &= -5;
        arg.declarationSourceStart = modifierPositions;
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    arg.annotations = new Annotation[length],
                    0,
                    length);
        }

        this.pushOnAstStack(arg);
        ++this.listLength;
    }

    protected void consumeCatchHeader() {
        if (this.currentElement != null) {
            if (!(this.currentElement instanceof RecoveredBlock)) {
                if (!(this.currentElement instanceof RecoveredMethod)) {
                    return;
                }

                RecoveredMethod rMethod = (RecoveredMethod) this.currentElement;
                if (rMethod.methodBody != null || rMethod.bracketBalance <= 0) {
                    return;
                }
            }

            Argument arg = (Argument) this.astStack[this.astPtr--];
            LocalDeclaration localDeclaration =
                    new LocalDeclaration(arg.name, arg.sourceStart, arg.sourceEnd);
            localDeclaration.type = arg.type;
            localDeclaration.declarationSourceStart = arg.declarationSourceStart;
            localDeclaration.declarationSourceEnd = arg.declarationSourceEnd;
            this.currentElement = this.currentElement.add((LocalDeclaration) localDeclaration, 0);
            this.lastCheckPoint = this.scanner.startPosition;
            this.restartRecovery = true;
            this.lastIgnoredToken = -1;
        }
    }

    protected void consumeCatchType() {
        int length = this.astLengthStack[this.astLengthPtr--];
        if (length != 1) {
            TypeReference[] typeReferences;
            System.arraycopy(
                    this.astStack,
                    (this.astPtr -= length) + 1,
                    typeReferences = new TypeReference[length],
                    0,
                    length);
            UnionTypeReference typeReference = new UnionTypeReference(typeReferences);
            this.pushOnAstStack(typeReference);
            if (this.options.sourceLevel < 3342336L) {
                this.problemReporter().multiCatchNotBelow17(typeReference);
            }
        } else {
            this.pushOnAstLengthStack(1);
        }
    }

    protected void consumeClassBodyDeclaration() {
        int var10002 = this.nestedMethod[this.nestedType]--;
        Block block = (Block) this.astStack[this.astPtr--];
        --this.astLengthPtr;
        if (this.diet) {
            block.bits &= -9;
        }

        Initializer initializer = (Initializer) this.astStack[this.astPtr];
        initializer.declarationSourceStart = initializer.sourceStart = block.sourceStart;
        initializer.block = block;
        --this.intPtr;
        initializer.bodyStart = this.intStack[this.intPtr--];
        --this.realBlockPtr;
        int javadocCommentStart = this.intStack[this.intPtr--];
        if (javadocCommentStart != -1) {
            initializer.declarationSourceStart = javadocCommentStart;
            initializer.javadoc = this.javadoc;
            this.javadoc = null;
        }

        initializer.bodyEnd = this.endPosition;
        initializer.sourceEnd = this.endStatementPosition;
        initializer.declarationSourceEnd =
                this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeClassBodyDeclarations() {
        this.concatNodeLists();
    }

    protected void consumeClassBodyDeclarationsopt() {
        --this.nestedType;
    }

    protected void consumeClassBodyopt() {
        this.pushOnAstStack((ASTNode) null);
        this.endPosition = this.rParenPos;
    }

    protected void consumeClassDeclaration() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.dispatchDeclarationInto(length);
        }

        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        boolean hasConstructor = typeDecl.checkConstructors(this);
        if (!hasConstructor) {
            switch (TypeDeclaration.kind(typeDecl.modifiers)) {
                case 1:
                case 3:
                    boolean insideFieldInitializer = false;
                    if (this.diet) {
                        for (int i = this.nestedType; i > 0; --i) {
                            if (this.variablesCounter[i] > 0) {
                                insideFieldInitializer = true;
                                break;
                            }
                        }
                    }

                    typeDecl.createDefaultConstructor(
                            !this.diet || this.dietInt != 0 || insideFieldInitializer, true);
                case 2:
            }
        }

        if (this.scanner.containsAssertKeyword) {
            typeDecl.bits |= 1;
        }

        typeDecl.addClinit();
        typeDecl.bodyEnd = this.endStatementPosition;
        if (length == 0 && !this.containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
            typeDecl.bits |= 8;
        }

        typeDecl.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeClassHeader() {
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        if (this.currentToken == 63) {
            typeDecl.bodyStart = this.scanner.currentPosition;
        }

        if (this.currentElement != null) {
            this.restartRecovery = true;
        }

        this.scanner.commentPtr = -1;
    }

    protected void consumeClassHeaderExtends() {
        TypeReference superClass = this.getTypeReference(0);
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        typeDecl.bits |= superClass.bits & 1048576;
        typeDecl.superclass = superClass;
        typeDecl.bodyStart = typeDecl.superclass.sourceEnd + 1;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
        }
    }

    protected void consumeClassHeaderImplements() {
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        System.arraycopy(
                this.astStack,
                this.astPtr + 1,
                typeDecl.superInterfaces = new TypeReference[length],
                0,
                length);
        TypeReference[] superinterfaces = typeDecl.superInterfaces;
        TypeReference[] var7 = superinterfaces;
        int var6 = superinterfaces.length;

        for (int var5 = 0; var5 < var6; ++var5) {
            TypeReference superinterface = var7[var5];
            typeDecl.bits |= superinterface.bits & 1048576;
        }

        typeDecl.bodyStart = typeDecl.superInterfaces[length - 1].sourceEnd + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
        }
    }

    private void consumeClassOrRecordHeaderName1(boolean isRecord) {
        TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
        if (this.nestedMethod[this.nestedType] == 0) {
            if (this.nestedType != 0) {
                typeDecl.bits |= 1024;
            }
        } else {
            typeDecl.bits |= 256;
            this.markEnclosingMemberWithLocalType();
            this.blockReal();
        }

        long pos = this.identifierPositionStack[this.identifierPtr];
        typeDecl.sourceEnd = (int) pos;
        typeDecl.sourceStart = (int) (pos >>> 32);
        typeDecl.name = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        typeDecl.declarationSourceStart = this.intStack[this.intPtr--];
        if (isRecord) {
            typeDecl.restrictedIdentifierStart = typeDecl.declarationSourceStart;
        }

        --this.intPtr;
        typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
        typeDecl.modifiers = this.intStack[this.intPtr--];
        if (typeDecl.modifiersSourceStart >= 0) {
            typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
        }

        if ((typeDecl.bits & 1024) == 0
                && (typeDecl.bits & 256) == 0
                && this.compilationUnit != null
                && !CharOperation.equals(typeDecl.name, this.compilationUnit.getMainTypeName())) {
            typeDecl.bits |= 4096;
        }

        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    typeDecl.annotations = new Annotation[length],
                    0,
                    length);
        }

        typeDecl.bodyStart = typeDecl.sourceEnd + 1;
        if (isRecord) {
            typeDecl.modifiers |= 16777216;
        }

        this.pushOnAstStack(typeDecl);
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
            this.currentElement = this.currentElement.add((TypeDeclaration) typeDecl, 0);
            this.lastIgnoredToken = -1;
        }

        typeDecl.javadoc = this.javadoc;
        this.javadoc = null;
    }

    protected void consumeClassHeaderName1() {
        this.consumeClassOrRecordHeaderName1(false);
    }

    protected void consumeClassInstanceCreationExpression() {
        this.classInstanceCreation(false);
        this.consumeInvocationExpression();
    }

    protected void consumeClassInstanceCreationExpressionName() {
        this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
    }

    protected void consumeClassInstanceCreationExpressionQualified() {
        this.classInstanceCreation(true);
        QualifiedAllocationExpression qae =
                (QualifiedAllocationExpression) this.expressionStack[this.expressionPtr];
        if (qae.anonymousType == null) {
            --this.expressionLengthPtr;
            --this.expressionPtr;
            qae.enclosingInstance = this.expressionStack[this.expressionPtr];
            this.expressionStack[this.expressionPtr] = qae;
        }

        qae.sourceStart = qae.enclosingInstance.sourceStart;
        this.consumeInvocationExpression();
    }

    protected void consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) == 1
                && this.astStack[this.astPtr] == null) {
            --this.astPtr;
            QualifiedAllocationExpression alloc = new QualifiedAllocationExpression();
            alloc.sourceEnd = this.endPosition;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                this.expressionPtr -= length;
                System.arraycopy(
                        this.expressionStack,
                        this.expressionPtr + 1,
                        alloc.arguments = new Expression[length],
                        0,
                        length);
            }

            alloc.type = this.getTypeReference(0);
            this.checkForDiamond(alloc.type);
            length = this.genericsLengthStack[this.genericsLengthPtr--];
            this.genericsPtr -= length;
            System.arraycopy(
                    this.genericsStack,
                    this.genericsPtr + 1,
                    alloc.typeArguments = new TypeReference[length],
                    0,
                    length);
            --this.intPtr;
            alloc.sourceStart = this.intStack[this.intPtr--];
            this.pushOnExpressionStack(alloc);
        } else {
            this.dispatchDeclarationInto(length);
            TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration) this.astStack[this.astPtr];
            anonymousTypeDeclaration.declarationSourceEnd = this.endStatementPosition;
            anonymousTypeDeclaration.bodyEnd = this.endStatementPosition;
            if (length == 0
                    && !this.containsComment(
                            anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
                anonymousTypeDeclaration.bits |= 8;
            }

            --this.astPtr;
            --this.astLengthPtr;
            QualifiedAllocationExpression allocationExpression =
                    anonymousTypeDeclaration.allocation;
            if (allocationExpression != null) {
                allocationExpression.sourceEnd = this.endStatementPosition;
                length = this.genericsLengthStack[this.genericsLengthPtr--];
                this.genericsPtr -= length;
                System.arraycopy(
                        this.genericsStack,
                        this.genericsPtr + 1,
                        allocationExpression.typeArguments = new TypeReference[length],
                        0,
                        length);
                allocationExpression.sourceStart = this.intStack[this.intPtr--];
                this.checkForDiamond(allocationExpression.type);
            }
        }

        QualifiedAllocationExpression qae =
                (QualifiedAllocationExpression) this.expressionStack[this.expressionPtr];
        if (qae.anonymousType == null) {
            --this.expressionLengthPtr;
            --this.expressionPtr;
            qae.enclosingInstance = this.expressionStack[this.expressionPtr];
            this.expressionStack[this.expressionPtr] = qae;
        }

        qae.sourceStart = qae.enclosingInstance.sourceStart;
        this.consumeInvocationExpression();
    }

    protected void consumeClassInstanceCreationExpressionWithTypeArguments() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) == 1
                && this.astStack[this.astPtr] == null) {
            --this.astPtr;
            AllocationExpression alloc = new AllocationExpression();
            alloc.sourceEnd = this.endPosition;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                this.expressionPtr -= length;
                System.arraycopy(
                        this.expressionStack,
                        this.expressionPtr + 1,
                        alloc.arguments = new Expression[length],
                        0,
                        length);
            }

            alloc.type = this.getTypeReference(0);
            this.checkForDiamond(alloc.type);
            length = this.genericsLengthStack[this.genericsLengthPtr--];
            this.genericsPtr -= length;
            System.arraycopy(
                    this.genericsStack,
                    this.genericsPtr + 1,
                    alloc.typeArguments = new TypeReference[length],
                    0,
                    length);
            --this.intPtr;
            alloc.sourceStart = this.intStack[this.intPtr--];
            this.pushOnExpressionStack(alloc);
        } else {
            this.dispatchDeclarationInto(length);
            TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration) this.astStack[this.astPtr];
            anonymousTypeDeclaration.declarationSourceEnd = this.endStatementPosition;
            anonymousTypeDeclaration.bodyEnd = this.endStatementPosition;
            if (length == 0
                    && !this.containsComment(
                            anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
                anonymousTypeDeclaration.bits |= 8;
            }

            --this.astPtr;
            --this.astLengthPtr;
            QualifiedAllocationExpression allocationExpression =
                    anonymousTypeDeclaration.allocation;
            if (allocationExpression != null) {
                allocationExpression.sourceEnd = this.endStatementPosition;
                length = this.genericsLengthStack[this.genericsLengthPtr--];
                this.genericsPtr -= length;
                System.arraycopy(
                        this.genericsStack,
                        this.genericsPtr + 1,
                        allocationExpression.typeArguments = new TypeReference[length],
                        0,
                        length);
                allocationExpression.sourceStart = this.intStack[this.intPtr--];
                this.checkForDiamond(allocationExpression.type);
            }
        }

        this.consumeInvocationExpression();
    }

    protected void consumeClassOrInterface() {
        int[] var10000 = this.genericsIdentifiersLengthStack;
        int var10001 = this.genericsIdentifiersLengthPtr;
        var10000[var10001] += this.identifierLengthStack[this.identifierLengthPtr];
        this.pushOnGenericsLengthStack(0);
    }

    protected void consumeClassOrInterfaceName() {
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
    }

    protected void consumeClassTypeElt() {
        this.pushOnAstStack(this.getTypeReference(0));
        ++this.listLength;
    }

    protected void consumeClassTypeList() {
        this.optimizedConcatNodeLists();
    }

    protected void consumeCompilationUnit() {}

    protected void consumeConditionalExpression(int op) {
        this.intPtr -= 2;
        this.expressionPtr -= 2;
        this.expressionLengthPtr -= 2;
        this.expressionStack[this.expressionPtr] =
                new ConditionalExpression(
                        this.expressionStack[this.expressionPtr],
                        this.expressionStack[this.expressionPtr + 1],
                        this.expressionStack[this.expressionPtr + 2]);
    }

    protected void consumeConditionalExpressionWithName(int op) {
        this.intPtr -= 2;
        this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
        this.expressionPtr -= 2;
        this.expressionLengthPtr -= 2;
        this.expressionStack[this.expressionPtr] =
                new ConditionalExpression(
                        this.expressionStack[this.expressionPtr + 2],
                        this.expressionStack[this.expressionPtr],
                        this.expressionStack[this.expressionPtr + 1]);
    }

    protected void consumeConstructorBlockStatements() {
        this.concatNodeLists();
    }

    protected void consumeConstructorBody() {
        int var10002 = this.nestedMethod[this.nestedType]--;
    }

    protected void consumeConstructorDeclaration() {
        --this.intPtr;
        --this.intPtr;
        --this.realBlockPtr;
        ExplicitConstructorCall constructorCall = null;
        Statement[] statements = null;
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.astPtr -= length;
            if (!this.options.ignoreMethodBodies) {
                ASTNode var5;
                if ((var5 = this.astStack[this.astPtr + 1]) instanceof ExplicitConstructorCall) {
                    ExplicitConstructorCall explicitCall = (ExplicitConstructorCall) var5;
                    System.arraycopy(
                            this.astStack,
                            this.astPtr + 2,
                            statements = new Statement[length - 1],
                            0,
                            length - 1);
                    constructorCall = explicitCall;
                } else {
                    System.arraycopy(
                            this.astStack,
                            this.astPtr + 1,
                            statements = new Statement[length],
                            0,
                            length);
                    constructorCall = SuperReference.implicitSuperConstructorCall();
                }
            }
        } else {
            boolean insideFieldInitializer = false;
            if (this.diet) {
                for (int i = this.nestedType; i > 0; --i) {
                    if (this.variablesCounter[i] > 0) {
                        insideFieldInitializer = true;
                        break;
                    }
                }
            }

            if (!this.options.ignoreMethodBodies && (!this.diet || insideFieldInitializer)) {
                constructorCall = SuperReference.implicitSuperConstructorCall();
            }
        }

        ConstructorDeclaration cd = (ConstructorDeclaration) this.astStack[this.astPtr];
        cd.constructorCall = constructorCall;
        cd.statements = statements;
        if (constructorCall != null && cd.constructorCall.sourceEnd == 0) {
            cd.constructorCall.sourceEnd = cd.sourceEnd;
            cd.constructorCall.sourceStart = cd.sourceStart;
        }

        if ((!this.diet || this.dietInt != 0)
                && statements == null
                && (constructorCall == null || constructorCall.isImplicitSuper())
                && !this.containsComment(cd.bodyStart, this.endPosition)) {
            cd.bits |= 8;
        }

        cd.bodyEnd = this.endPosition;
        cd.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeConstructorHeader() {
        AbstractMethodDeclaration method = (AbstractMethodDeclaration) this.astStack[this.astPtr];
        if (this.currentToken == 63) {
            method.bodyStart = this.scanner.currentPosition;
        }

        if (this.currentElement != null) {
            if (this.currentToken == 26) {
                method.modifiers |= 16777216;
                method.declarationSourceEnd = this.scanner.currentPosition - 1;
                method.bodyEnd = this.scanner.currentPosition - 1;
                if (this.currentElement.parseTree() == method
                        && this.currentElement.parent != null) {
                    this.currentElement = this.currentElement.parent;
                }
            }

            this.restartRecovery = true;
        }
    }

    protected void consumeConstructorHeaderName(boolean isCompact) {
        if (this.currentElement != null && this.lastIgnoredToken == 38) {
            this.lastCheckPoint = this.scanner.startPosition;
            this.restartRecovery = true;
        } else {
            ConstructorDeclaration cd =
                    isCompact
                            ? new CompactConstructorDeclaration(
                                    this.compilationUnit.compilationResult)
                            : new ConstructorDeclaration(this.compilationUnit.compilationResult);
            ((ConstructorDeclaration) cd).selector = this.identifierStack[this.identifierPtr];
            long selectorSource = this.identifierPositionStack[this.identifierPtr--];
            --this.identifierLengthPtr;
            ((ConstructorDeclaration) cd).declarationSourceStart = this.intStack[this.intPtr--];
            ((ConstructorDeclaration) cd).modifiers = this.intStack[this.intPtr--];
            if (isCompact) {
                ((ConstructorDeclaration) cd).modifiers |= 8388608;
            }

            int length;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                System.arraycopy(
                        this.expressionStack,
                        (this.expressionPtr -= length) + 1,
                        ((ConstructorDeclaration) cd).annotations = new Annotation[length],
                        0,
                        length);
            }

            ((ConstructorDeclaration) cd).javadoc = this.javadoc;
            this.javadoc = null;
            ((ConstructorDeclaration) cd).sourceStart = (int) (selectorSource >>> 32);
            this.pushOnAstStack((ASTNode) cd);
            ((ConstructorDeclaration) cd).sourceEnd =
                    isCompact
                            ? ((ConstructorDeclaration) cd).sourceStart
                                    + ((ConstructorDeclaration) cd).selector.length
                                    - 1
                            : this.lParenPos;
            ((ConstructorDeclaration) cd).bodyStart =
                    isCompact
                            ? ((ConstructorDeclaration) cd).sourceStart
                                    + ((ConstructorDeclaration) cd).selector.length
                            : this.lParenPos + 1;
            this.listLength = 0;
            if (this.currentElement != null) {
                this.lastCheckPoint = ((ConstructorDeclaration) cd).bodyStart;
                if (this.currentElement instanceof RecoveredType && this.lastIgnoredToken != 1
                        || ((ConstructorDeclaration) cd).modifiers != 0) {
                    this.currentElement =
                            this.currentElement.add((AbstractMethodDeclaration) cd, 0);
                    this.lastIgnoredToken = -1;
                }
            }
        }
    }

    protected void consumeConstructorHeaderNameWithTypeParameters() {
        if (this.currentElement != null && this.lastIgnoredToken == 38) {
            this.lastCheckPoint = this.scanner.startPosition;
            this.restartRecovery = true;
        } else {
            ConstructorDeclaration cd =
                    new ConstructorDeclaration(this.compilationUnit.compilationResult);
            this.helperConstructorHeaderNameWithTypeParameters(cd);
        }
    }

    private void helperConstructorHeaderNameWithTypeParameters(ConstructorDeclaration cd) {
        cd.selector = this.identifierStack[this.identifierPtr];
        long selectorSource = this.identifierPositionStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                cd.typeParameters = new TypeParameter[length],
                0,
                length);
        cd.declarationSourceStart = this.intStack[this.intPtr--];
        cd.modifiers = this.intStack[this.intPtr--];
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    cd.annotations = new Annotation[length],
                    0,
                    length);
        }

        cd.javadoc = this.javadoc;
        this.javadoc = null;
        cd.sourceStart = (int) (selectorSource >>> 32);
        this.pushOnAstStack(cd);
        cd.sourceEnd = this.lParenPos;
        cd.bodyStart = this.lParenPos + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = cd.bodyStart;
            if (this.currentElement instanceof RecoveredType && this.lastIgnoredToken != 1
                    || cd.modifiers != 0) {
                this.currentElement = this.currentElement.add((AbstractMethodDeclaration) cd, 0);
                this.lastIgnoredToken = -1;
            }
        }
    }

    protected void consumeCreateInitializer() {
        this.pushOnAstStack(new Initializer((Block) null, 0));
    }

    protected void consumeDefaultLabel() {
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumeDefaultModifiers() {
        this.checkComment();
        this.pushOnIntStack(this.modifiers);
        this.pushOnIntStack(
                this.modifiersSourceStart >= 0
                        ? this.modifiersSourceStart
                        : this.scanner.startPosition);
        this.resetModifiers();
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumeDiet() {
        this.checkComment();
        this.pushOnIntStack(this.modifiersSourceStart);
        this.resetModifiers();
        this.jumpOverMethodBody();
    }

    protected void consumeDims() {
        this.pushOnIntStack(this.dimensions);
        this.dimensions = 0;
    }

    protected void consumeDimWithOrWithOutExpr() {
        this.pushOnExpressionStack((Expression) null);
        if (this.currentElement != null && this.currentToken == 63) {
            this.ignoreNextOpeningBrace = true;
            ++this.currentElement.bracketBalance;
        }
    }

    protected void consumeDimWithOrWithOutExprs() {
        this.concatExpressionLists();
    }

    protected void consumeUnionType() {
        this.pushOnAstStack(this.getTypeReference(this.intStack[this.intPtr--]));
        this.optimizedConcatNodeLists();
    }

    protected void consumeUnionTypeAsClassType() {
        this.pushOnAstStack(this.getTypeReference(this.intStack[this.intPtr--]));
    }

    protected void consumeEmptyAnnotationTypeMemberDeclarationsopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeEmptyArgumentListopt() {
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumeEmptyArguments() {
        FieldDeclaration fieldDeclaration = (FieldDeclaration) this.astStack[this.astPtr];
        this.pushOnIntStack(fieldDeclaration.sourceEnd);
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumeEmptyArrayInitializer() {
        this.arrayInitializer(0);
    }

    protected void consumeEmptyArrayInitializeropt() {
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumeEmptyBlockStatementsopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeEmptyCatchesopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeEmptyClassBodyDeclarationsopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeEmptyDimsopt() {
        this.pushOnIntStack(0);
    }

    protected void consumeUnnamedVariable() {
        this.pushOnIntStack(0);
    }

    protected void consumeEmptyEnumDeclarations() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeEmptyExpression() {
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumeEmptyForInitopt() {
        this.pushOnAstLengthStack(0);
        this.forStartPosition = 0;
    }

    protected void consumeEmptyForUpdateopt() {
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumeEmptyInterfaceMemberDeclarationsopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeEmptyInternalCompilationUnit() {
        if (this.compilationUnit.isPackageInfo()) {
            this.compilationUnit.types = new TypeDeclaration[1];
            this.compilationUnit.createPackageInfoType();
        }
    }

    protected void consumeEmptyMemberValueArrayInitializer() {
        this.arrayInitializer(0);
    }

    protected void consumeEmptyMemberValuePairsopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeEmptyMethodHeaderDefaultValue() {
        AbstractMethodDeclaration method = (AbstractMethodDeclaration) this.astStack[this.astPtr];
        if (method.isAnnotationMethod()) {
            this.pushOnExpressionStackLengthStack(0);
        }

        this.recordStringLiterals = true;
    }

    protected void consumeEmptyStatement() {
        char[] source = this.scanner.source;
        if (source[this.endStatementPosition] == ';') {
            this.pushOnAstStack(
                    new EmptyStatement(this.endStatementPosition, this.endStatementPosition));
        } else {
            if (source.length > 5) {
                int pos;
                for (pos = this.endStatementPosition - 4; source[pos] == 'u'; --pos) {}

                int c1;
                int c2;
                int c3;
                int c4;
                if (source[pos] == '\\'
                        && (c1 =
                                        ScannerHelper.getHexadecimalValue(
                                                source[this.endStatementPosition - 3]))
                                <= 15
                        && c1 >= 0
                        && (c2 =
                                        ScannerHelper.getHexadecimalValue(
                                                source[this.endStatementPosition - 2]))
                                <= 15
                        && c2 >= 0
                        && (c3 =
                                        ScannerHelper.getHexadecimalValue(
                                                source[this.endStatementPosition - 1]))
                                <= 15
                        && c3 >= 0
                        && (c4 =
                                        ScannerHelper.getHexadecimalValue(
                                                source[this.endStatementPosition]))
                                <= 15
                        && c4 >= 0
                        && (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4) == ';') {
                    this.pushOnAstStack(new EmptyStatement(pos, this.endStatementPosition));
                    return;
                }
            }

            this.pushOnAstStack(
                    new EmptyStatement(this.endPosition + 1, this.endStatementPosition));
        }
    }

    protected void consumeEmptyTypeDeclaration() {
        this.pushOnAstLengthStack(0);
        if (!this.statementRecoveryActivated) {
            this.problemReporter()
                    .superfluousSemicolon(this.endPosition + 1, this.endStatementPosition);
        }

        this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeEnhancedForStatement() {
        --this.astLengthPtr;
        Statement statement = (Statement) this.astStack[this.astPtr--];
        ForeachStatement foreachStatement = (ForeachStatement) this.astStack[this.astPtr];
        foreachStatement.action = statement;
        if (statement instanceof EmptyStatement) {
            statement.bits |= 1;
        }

        foreachStatement.sourceEnd = this.endStatementPosition;
    }

    protected void consumeEnhancedForStatementHeader() {
        ForeachStatement statement = (ForeachStatement) this.astStack[this.astPtr];
        --this.expressionLengthPtr;
        Expression collection = this.expressionStack[this.expressionPtr--];
        statement.collection = collection;
        statement.elementVariable.declarationSourceEnd = collection.sourceEnd;
        statement.elementVariable.declarationEnd = collection.sourceEnd;
        statement.sourceEnd = this.rParenPos;
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter()
                    .invalidUsageOfForeachStatements(statement.elementVariable, collection);
        }
    }

    protected void consumeEnhancedForStatementHeaderInit(boolean hasModifiers) {
        char[] identifierName = this.identifierStack[this.identifierPtr];
        long namePosition = this.identifierPositionStack[this.identifierPtr];
        LocalDeclaration localDeclaration =
                this.createLocalDeclaration(
                        identifierName, (int) (namePosition >>> 32), (int) namePosition);
        localDeclaration.declarationSourceEnd = localDeclaration.declarationEnd;
        localDeclaration.bits |= 16;
        int extraDims = this.intStack[this.intPtr--];
        Annotation[][] annotationsOnExtendedDimensions =
                extraDims == 0 ? null : this.getAnnotationsOnDimensions(extraDims);
        --this.identifierPtr;
        --this.identifierLengthPtr;
        int declarationSourceStart = 0;
        int modifiersValue = 0;
        if (hasModifiers) {
            declarationSourceStart = this.intStack[this.intPtr--];
            modifiersValue = this.intStack[this.intPtr--];
        } else {
            this.intPtr -= 2;
        }

        TypeReference type = this.getTypeReference(this.intStack[this.intPtr--]);
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    localDeclaration.annotations = new Annotation[length],
                    0,
                    length);
            localDeclaration.bits |= 1048576;
        }

        if (extraDims != 0) {
            type =
                    this.augmentTypeWithAdditionalDimensions(
                            type, extraDims, annotationsOnExtendedDimensions, false);
        }

        if (hasModifiers) {
            localDeclaration.declarationSourceStart = declarationSourceStart;
            localDeclaration.modifiers = modifiersValue;
        } else {
            localDeclaration.declarationSourceStart = type.sourceStart;
        }

        localDeclaration.type = type;
        localDeclaration.bits |= type.bits & 1048576;
        ForeachStatement iteratorForStatement =
                new ForeachStatement(localDeclaration, this.intStack[this.intPtr--]);
        this.pushOnAstStack(iteratorForStatement);
        iteratorForStatement.sourceEnd = localDeclaration.declarationSourceEnd;
        this.forStartPosition = 0;
    }

    protected void consumeEnterAnonymousClassBody(boolean qualified) {
        TypeReference typeReference = this.getTypeReference(0);
        TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult);
        anonymousType.name = CharOperation.NO_CHAR;
        anonymousType.bits |= 768;
        anonymousType.bits |= typeReference.bits & 1048576;
        QualifiedAllocationExpression alloc = new QualifiedAllocationExpression(anonymousType);
        this.markEnclosingMemberWithLocalType();
        this.pushOnAstStack(anonymousType);
        alloc.sourceEnd = this.rParenPos;
        int argumentLength;
        if ((argumentLength = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            this.expressionPtr -= argumentLength;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    alloc.arguments = new Expression[argumentLength],
                    0,
                    argumentLength);
        }

        if (qualified) {
            --this.expressionLengthPtr;
            alloc.enclosingInstance = this.expressionStack[this.expressionPtr--];
        }

        alloc.type = typeReference;
        anonymousType.sourceEnd = alloc.sourceEnd;
        anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
        alloc.sourceStart = this.intStack[this.intPtr--];
        this.pushOnExpressionStack(alloc);
        anonymousType.bodyStart = this.scanner.currentPosition;
        this.listLength = 0;
        this.scanner.commentPtr = -1;
        if (this.currentElement != null) {
            this.lastCheckPoint = anonymousType.bodyStart;
            this.currentElement = this.currentElement.add((TypeDeclaration) anonymousType, 0);
            if (!(this.currentElement instanceof RecoveredAnnotation)) {
                if (this.isIndirectlyInsideLambdaExpression()) {
                    this.ignoreNextOpeningBrace = true;
                } else {
                    this.currentToken = 0;
                }
            } else {
                this.ignoreNextOpeningBrace = true;
                ++this.currentElement.bracketBalance;
            }

            this.lastIgnoredToken = -1;
        }

        this.checkForDiamond(typeReference);
    }

    protected void consumeEnterCompilationUnit() {}

    protected void consumeEnterMemberValue() {
        RecoveredElement var2;
        if ((var2 = this.currentElement) instanceof RecoveredAnnotation) {
            RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var2;
            recoveredAnnotation.hasPendingMemberValueName = true;
        }
    }

    protected void consumeEnterMemberValueArrayInitializer() {
        if (this.currentElement != null) {
            this.ignoreNextOpeningBrace = true;
            ++this.currentElement.bracketBalance;
        }
    }

    private boolean isAFieldDeclarationInRecord() {
        if (this.options.sourceLevel < 3932160L) {
            return false;
        } else {
            int recordIndex = -1;
            Integer[] nestingTypeAndMethod = null;

            int i;
            for (i = this.astPtr; i >= 0; --i) {
                ASTNode var5;
                if ((var5 = this.astStack[i]) instanceof TypeDeclaration) {
                    TypeDeclaration node = (TypeDeclaration) var5;
                    if (node.isRecord()) {
                        nestingTypeAndMethod = (Integer[]) this.recordNestedMethodLevels.get(node);
                        if (nestingTypeAndMethod != null) {
                            if (nestingTypeAndMethod[0] == this.nestedType
                                    && nestingTypeAndMethod[1]
                                            == this.nestedMethod[this.nestedType]) {
                                recordIndex = i;
                                break;
                            }

                            return false;
                        }
                    }
                }
            }

            if (recordIndex < 0) {
                return false;
            } else {
                for (i = recordIndex + 1; i <= this.astPtr; ++i) {
                    ASTNode node = this.astStack[i];
                    if (node instanceof TypeDeclaration) {
                        if (node.sourceEnd < 0) {
                            return false;
                        }
                    } else if (node instanceof AbstractMethodDeclaration) {
                        if (this.nestedType != nestingTypeAndMethod[0]
                                || this.nestedMethod[this.nestedType] != nestingTypeAndMethod[1]) {
                            return false;
                        }
                    } else if (!(node instanceof FieldDeclaration)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    protected void consumeEnterVariable() {
        char[] identifierName = this.identifierStack[this.identifierPtr];
        long namePosition = this.identifierPositionStack[this.identifierPtr];
        int extendedDimensions = this.intStack[this.intPtr--];
        Annotation[][] annotationsOnExtendedDimensions =
                extendedDimensions == 0
                        ? null
                        : this.getAnnotationsOnDimensions(extendedDimensions);
        boolean isLocalDeclaration =
                this.nestedMethod[this.nestedType] != 0 && !this.isAFieldDeclarationInRecord();
        Object declaration;
        if (isLocalDeclaration) {
            declaration =
                    this.createLocalDeclaration(
                            identifierName, (int) (namePosition >>> 32), (int) namePosition);
        } else {
            declaration =
                    this.createFieldDeclaration(
                            identifierName, (int) (namePosition >>> 32), (int) namePosition);
        }

        --this.identifierPtr;
        --this.identifierLengthPtr;
        int variableIndex = this.variablesCounter[this.nestedType];
        TypeReference type;
        if (variableIndex == 0) {
            int length;
            if (isLocalDeclaration) {
                ((AbstractVariableDeclaration) declaration).declarationSourceStart =
                        this.intStack[this.intPtr--];
                ((AbstractVariableDeclaration) declaration).modifiers =
                        this.intStack[this.intPtr--];
                if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                    System.arraycopy(
                            this.expressionStack,
                            (this.expressionPtr -= length) + 1,
                            ((AbstractVariableDeclaration) declaration).annotations =
                                    new Annotation[length],
                            0,
                            length);
                }

                type = this.getTypeReference(this.intStack[this.intPtr--]);
                if (((AbstractVariableDeclaration) declaration).declarationSourceStart == -1) {
                    ((AbstractVariableDeclaration) declaration).declarationSourceStart =
                            type.sourceStart;
                }

                this.pushOnAstStack(type);
            } else {
                type = this.getTypeReference(this.intStack[this.intPtr--]);
                this.pushOnAstStack(type);
                ((AbstractVariableDeclaration) declaration).declarationSourceStart =
                        this.intStack[this.intPtr--];
                ((AbstractVariableDeclaration) declaration).modifiers =
                        this.intStack[this.intPtr--];
                if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                    System.arraycopy(
                            this.expressionStack,
                            (this.expressionPtr -= length) + 1,
                            ((AbstractVariableDeclaration) declaration).annotations =
                                    new Annotation[length],
                            0,
                            length);
                }

                FieldDeclaration fieldDeclaration = (FieldDeclaration) declaration;
                fieldDeclaration.javadoc = this.javadoc;
            }

            this.javadoc = null;
        } else {
            type = (TypeReference) this.astStack[this.astPtr - variableIndex];
            AbstractVariableDeclaration previousVariable =
                    (AbstractVariableDeclaration) this.astStack[this.astPtr];
            ((AbstractVariableDeclaration) declaration).declarationSourceStart =
                    previousVariable.declarationSourceStart;
            ((AbstractVariableDeclaration) declaration).modifiers = previousVariable.modifiers;
            Annotation[] annotations = previousVariable.annotations;
            if (annotations != null) {
                int annotationsLength = annotations.length;
                System.arraycopy(
                        annotations,
                        0,
                        ((AbstractVariableDeclaration) declaration).annotations =
                                new Annotation[annotationsLength],
                        0,
                        annotationsLength);
            }

            ((AbstractVariableDeclaration) declaration).bits |= 4194304;
        }

        ((AbstractVariableDeclaration) declaration).type =
                extendedDimensions == 0
                        ? type
                        : this.augmentTypeWithAdditionalDimensions(
                                type, extendedDimensions, annotationsOnExtendedDimensions, false);
        ((AbstractVariableDeclaration) declaration).bits |= type.bits & 1048576;
        int var10002 = this.variablesCounter[this.nestedType]++;
        this.pushOnAstStack((ASTNode) declaration);
        if (this.currentElement != null) {
            if (!(this.currentElement instanceof RecoveredType)
                    && (this.currentToken == 1
                            || Util.getLineNumber(
                                            ((AbstractVariableDeclaration) declaration)
                                                    .type
                                                    .sourceStart,
                                            this.scanner.lineEnds,
                                            0,
                                            this.scanner.linePtr)
                                    != Util.getLineNumber(
                                            (int) (namePosition >>> 32),
                                            this.scanner.lineEnds,
                                            0,
                                            this.scanner.linePtr))) {
                this.lastCheckPoint = (int) (namePosition >>> 32);
                this.restartRecovery = true;
                return;
            }

            if (isLocalDeclaration) {
                LocalDeclaration localDecl = (LocalDeclaration) this.astStack[this.astPtr];
                this.lastCheckPoint = localDecl.sourceEnd + 1;
                this.currentElement = this.currentElement.add((LocalDeclaration) localDecl, 0);
            } else {
                FieldDeclaration fieldDecl = (FieldDeclaration) this.astStack[this.astPtr];
                this.lastCheckPoint = fieldDecl.sourceEnd + 1;
                this.currentElement = this.currentElement.add((FieldDeclaration) fieldDecl, 0);
            }

            this.lastIgnoredToken = -1;
        }
    }

    protected void consumeEnumBodyNoConstants() {}

    protected void consumeEnumBodyWithConstants() {
        this.concatNodeLists();
    }

    protected void consumeEnumConstantHeader() {
        FieldDeclaration enumConstant = (FieldDeclaration) this.astStack[this.astPtr];
        boolean foundOpeningBrace = this.currentToken == 63;
        TypeDeclaration anonymousType;
        int length;
        if (foundOpeningBrace) {
            anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult);
            anonymousType.name = CharOperation.NO_CHAR;
            anonymousType.bits |= 768;
            length = this.scanner.startPosition;
            anonymousType.declarationSourceStart = length;
            anonymousType.sourceStart = length;
            anonymousType.sourceEnd = length;
            anonymousType.modifiers = 0;
            anonymousType.bodyStart = this.scanner.currentPosition;
            this.markEnclosingMemberWithLocalType();
            this.consumeNestedType();
            int var10002 = this.variablesCounter[this.nestedType]++;
            this.pushOnAstStack(anonymousType);
            QualifiedAllocationExpression allocationExpression =
                    new QualifiedAllocationExpression(anonymousType);
            allocationExpression.enumConstant = enumConstant;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                this.expressionPtr -= length;
                System.arraycopy(
                        this.expressionStack,
                        this.expressionPtr + 1,
                        allocationExpression.arguments = new Expression[length],
                        0,
                        length);
            }

            enumConstant.initialization = allocationExpression;
        } else {
            AllocationExpression allocationExpression = new AllocationExpression();
            allocationExpression.enumConstant = enumConstant;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
                this.expressionPtr -= length;
                System.arraycopy(
                        this.expressionStack,
                        this.expressionPtr + 1,
                        allocationExpression.arguments = new Expression[length],
                        0,
                        length);
            }

            enumConstant.initialization = allocationExpression;
        }

        enumConstant.initialization.sourceStart = enumConstant.declarationSourceStart;
        if (this.currentElement != null) {
            if (foundOpeningBrace) {
                anonymousType = (TypeDeclaration) this.astStack[this.astPtr];
                this.currentElement = this.currentElement.add((TypeDeclaration) anonymousType, 0);
                this.lastCheckPoint = anonymousType.bodyStart;
                this.lastIgnoredToken = -1;
                if (this.isIndirectlyInsideLambdaExpression()) {
                    this.ignoreNextOpeningBrace = true;
                } else {
                    this.currentToken = 0;
                }
            } else {
                if (this.currentToken == 26) {
                    RecoveredType currentType = this.currentRecoveryType();
                    if (currentType != null) {
                        currentType.insideEnumConstantPart = false;
                    }
                }

                this.lastCheckPoint = this.scanner.startPosition;
                this.lastIgnoredToken = -1;
                this.restartRecovery = true;
            }
        }
    }

    protected void consumeEnumConstantHeaderName() {
        if (this.currentElement != null) {
            label33:
            {
                label27:
                {
                    if (!(this.currentElement instanceof RecoveredType)) {
                        RecoveredElement var2;
                        if (!((var2 = this.currentElement) instanceof RecoveredField)) {
                            break label27;
                        }

                        RecoveredField recoveredField = (RecoveredField) var2;
                        if (recoveredField.fieldDeclaration.type != null) {
                            break label27;
                        }
                    }

                    if (this.lastIgnoredToken != 1) {
                        break label33;
                    }
                }

                this.lastCheckPoint = this.scanner.startPosition;
                this.restartRecovery = true;
                return;
            }
        }

        long namePosition = this.identifierPositionStack[this.identifierPtr];
        char[] constantName = this.identifierStack[this.identifierPtr];
        int sourceEnd = (int) namePosition;
        FieldDeclaration enumConstant =
                this.createFieldDeclaration(constantName, (int) (namePosition >>> 32), sourceEnd);
        --this.identifierPtr;
        --this.identifierLengthPtr;
        enumConstant.modifiersSourceStart = this.intStack[this.intPtr--];
        enumConstant.modifiers = this.intStack[this.intPtr--];
        enumConstant.declarationSourceStart = enumConstant.modifiersSourceStart;
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    enumConstant.annotations = new Annotation[length],
                    0,
                    length);
            enumConstant.bits |= 1048576;
        }

        this.pushOnAstStack(enumConstant);
        if (this.currentElement != null) {
            this.lastCheckPoint = enumConstant.sourceEnd + 1;
            this.currentElement = this.currentElement.add((FieldDeclaration) enumConstant, 0);
        }

        enumConstant.javadoc = this.javadoc;
        this.javadoc = null;
    }

    protected void consumeEnumConstantNoClassBody() {
        int endOfEnumConstant = this.intStack[this.intPtr--];
        FieldDeclaration fieldDeclaration = (FieldDeclaration) this.astStack[this.astPtr];
        fieldDeclaration.declarationEnd = endOfEnumConstant;
        fieldDeclaration.declarationSourceEnd = endOfEnumConstant;
        ASTNode initialization = fieldDeclaration.initialization;
        if (initialization != null) {
            initialization.sourceEnd = endOfEnumConstant;
        }
    }

    protected void consumeEnumConstants() {
        this.concatNodeLists();
    }

    protected void consumeEnumConstantWithClassBody() {
        this.dispatchDeclarationInto(this.astLengthStack[this.astLengthPtr--]);
        TypeDeclaration anonymousType = (TypeDeclaration) this.astStack[this.astPtr--];
        --this.astLengthPtr;
        anonymousType.addClinit();
        anonymousType.bodyEnd = this.endPosition;
        anonymousType.declarationSourceEnd =
                this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        FieldDeclaration fieldDeclaration = (FieldDeclaration) this.astStack[this.astPtr];
        fieldDeclaration.declarationEnd = this.endStatementPosition;
        int declarationSourceEnd = anonymousType.declarationSourceEnd;
        fieldDeclaration.declarationSourceEnd = declarationSourceEnd;
        --this.intPtr;
        this.variablesCounter[this.nestedType] = 0;
        --this.nestedType;
        ASTNode initialization = fieldDeclaration.initialization;
        if (initialization != null) {
            initialization.sourceEnd = declarationSourceEnd;
        }
    }

    protected void consumeEnumDeclaration() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.dispatchDeclarationIntoEnumDeclaration(length);
        }

        TypeDeclaration enumDeclaration = (TypeDeclaration) this.astStack[this.astPtr];
        boolean hasConstructor = enumDeclaration.checkConstructors(this);
        if (!hasConstructor) {
            boolean insideFieldInitializer = false;
            if (this.diet) {
                for (int i = this.nestedType; i > 0; --i) {
                    if (this.variablesCounter[i] > 0) {
                        insideFieldInitializer = true;
                        break;
                    }
                }
            }

            enumDeclaration.createDefaultConstructor(!this.diet || insideFieldInitializer, true);
        }

        if (this.scanner.containsAssertKeyword) {
            enumDeclaration.bits |= 1;
        }

        enumDeclaration.addClinit();
        enumDeclaration.bodyEnd = this.endStatementPosition;
        if (length == 0
                && !this.containsComment(enumDeclaration.bodyStart, enumDeclaration.bodyEnd)) {
            enumDeclaration.bits |= 8;
        }

        enumDeclaration.declarationSourceEnd =
                this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeEnumDeclarations() {}

    protected void consumeEnumHeader() {
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        if (this.currentToken == 63) {
            typeDecl.bodyStart = this.scanner.currentPosition;
        }

        if (this.currentElement != null) {
            this.restartRecovery = true;
        }

        this.scanner.commentPtr = -1;
    }

    protected void consumeEnumHeaderName() {
        TypeDeclaration enumDeclaration =
                new TypeDeclaration(this.compilationUnit.compilationResult);
        if (this.nestedMethod[this.nestedType] == 0) {
            if (this.nestedType != 0) {
                enumDeclaration.bits |= 1024;
            }
        } else {
            this.markEnclosingMemberWithLocalType();
            this.blockReal();
        }

        long pos = this.identifierPositionStack[this.identifierPtr];
        enumDeclaration.sourceEnd = (int) pos;
        enumDeclaration.sourceStart = (int) (pos >>> 32);
        enumDeclaration.name = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        enumDeclaration.declarationSourceStart = this.intStack[this.intPtr--];
        --this.intPtr;
        enumDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
        enumDeclaration.modifiers = this.intStack[this.intPtr--] | 16384;
        if (enumDeclaration.modifiersSourceStart >= 0) {
            enumDeclaration.declarationSourceStart = enumDeclaration.modifiersSourceStart;
        }

        if ((enumDeclaration.bits & 1024) == 0
                && (enumDeclaration.bits & 256) == 0
                && this.compilationUnit != null
                && !CharOperation.equals(
                        enumDeclaration.name, this.compilationUnit.getMainTypeName())) {
            enumDeclaration.bits |= 4096;
        }

        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    enumDeclaration.annotations = new Annotation[length],
                    0,
                    length);
        }

        enumDeclaration.bodyStart = enumDeclaration.sourceEnd + 1;
        this.pushOnAstStack(enumDeclaration);
        this.listLength = 0;
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter().invalidUsageOfEnumDeclarations(enumDeclaration);
        }

        if (this.currentElement != null) {
            this.lastCheckPoint = enumDeclaration.bodyStart;
            this.currentElement = this.currentElement.add((TypeDeclaration) enumDeclaration, 0);
            this.lastIgnoredToken = -1;
        }

        enumDeclaration.javadoc = this.javadoc;
        this.javadoc = null;
    }

    protected void consumeEnumHeaderNameWithTypeParameters() {
        TypeDeclaration enumDeclaration =
                new TypeDeclaration(this.compilationUnit.compilationResult);
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                enumDeclaration.typeParameters = new TypeParameter[length],
                0,
                length);
        this.problemReporter().invalidUsageOfTypeParametersForEnumDeclaration(enumDeclaration);
        enumDeclaration.bodyStart =
                enumDeclaration.typeParameters[length - 1].declarationSourceEnd + 1;
        this.listTypeParameterLength = 0;
        if (this.nestedMethod[this.nestedType] == 0) {
            if (this.nestedType != 0) {
                enumDeclaration.bits |= 1024;
            }
        } else {
            this.blockReal();
        }

        long pos = this.identifierPositionStack[this.identifierPtr];
        enumDeclaration.sourceEnd = (int) pos;
        enumDeclaration.sourceStart = (int) (pos >>> 32);
        enumDeclaration.name = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        enumDeclaration.declarationSourceStart = this.intStack[this.intPtr--];
        --this.intPtr;
        enumDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
        enumDeclaration.modifiers = this.intStack[this.intPtr--] | 16384;
        if (enumDeclaration.modifiersSourceStart >= 0) {
            enumDeclaration.declarationSourceStart = enumDeclaration.modifiersSourceStart;
        }

        if ((enumDeclaration.bits & 1024) == 0
                && (enumDeclaration.bits & 256) == 0
                && this.compilationUnit != null
                && !CharOperation.equals(
                        enumDeclaration.name, this.compilationUnit.getMainTypeName())) {
            enumDeclaration.bits |= 4096;
        }

        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    enumDeclaration.annotations = new Annotation[length],
                    0,
                    length);
        }

        enumDeclaration.bodyStart = enumDeclaration.sourceEnd + 1;
        this.pushOnAstStack(enumDeclaration);
        this.listLength = 0;
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter().invalidUsageOfEnumDeclarations(enumDeclaration);
        }

        if (this.currentElement != null) {
            this.lastCheckPoint = enumDeclaration.bodyStart;
            this.currentElement = this.currentElement.add((TypeDeclaration) enumDeclaration, 0);
            this.lastIgnoredToken = -1;
        }

        enumDeclaration.javadoc = this.javadoc;
        this.javadoc = null;
    }

    protected void consumeEqualityExpression(int op) {
        --this.expressionPtr;
        --this.expressionLengthPtr;
        this.expressionStack[this.expressionPtr] =
                new EqualExpression(
                        this.expressionStack[this.expressionPtr],
                        this.expressionStack[this.expressionPtr + 1],
                        op);
    }

    protected void consumeEqualityExpressionWithName(int op) {
        this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
        --this.expressionPtr;
        --this.expressionLengthPtr;
        this.expressionStack[this.expressionPtr] =
                new EqualExpression(
                        this.expressionStack[this.expressionPtr + 1],
                        this.expressionStack[this.expressionPtr],
                        op);
    }

    protected void consumeExitMemberValue() {
        RecoveredElement var2;
        if ((var2 = this.currentElement) instanceof RecoveredAnnotation) {
            RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var2;
            recoveredAnnotation.hasPendingMemberValueName = false;
            recoveredAnnotation.memberValuPairEqualEnd = -1;
        }
    }

    protected void consumeExitTryBlock() {
        if (this.currentElement != null) {
            this.restartRecovery = true;
        }
    }

    protected void consumeExitVariableWithInitialization() {
        --this.expressionLengthPtr;
        AbstractVariableDeclaration variableDecl =
                (AbstractVariableDeclaration) this.astStack[this.astPtr];
        variableDecl.initialization = this.expressionStack[this.expressionPtr--];
        variableDecl.declarationSourceEnd = variableDecl.initialization.sourceEnd;
        variableDecl.declarationEnd = variableDecl.initialization.sourceEnd;
        this.recoveryExitFromVariable();
    }

    protected void consumeExitVariableWithoutInitialization() {
        AbstractVariableDeclaration variableDecl =
                (AbstractVariableDeclaration) this.astStack[this.astPtr];
        variableDecl.declarationSourceEnd = variableDecl.declarationEnd;
        if (this.currentElement instanceof RecoveredField
                && this.endStatementPosition > variableDecl.sourceEnd) {
            this.currentElement.updateSourceEndIfNecessary(this.endStatementPosition);
        }

        this.recoveryExitFromVariable();
    }

    protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
        int startPosition = this.intStack[this.intPtr--];
        ExplicitConstructorCall ecc = new ExplicitConstructorCall(recFlag);
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    ecc.arguments = new Expression[length],
                    0,
                    length);
        }

        switch (flag) {
            case 0:
                ecc.sourceStart = startPosition;
                break;
            case 1:
                --this.expressionLengthPtr;
                ecc.sourceStart =
                        (ecc.qualification = this.expressionStack[this.expressionPtr--])
                                .sourceStart;
                break;
            case 2:
                ecc.sourceStart =
                        (ecc.qualification = this.getUnspecifiedReferenceOptimized()).sourceStart;
        }

        this.pushOnAstStack(ecc);
        ecc.sourceEnd = this.endStatementPosition;
    }

    protected void consumeExplicitConstructorInvocationWithTypeArguments(int flag, int recFlag) {
        int startPosition = this.intStack[this.intPtr--];
        ExplicitConstructorCall ecc = new ExplicitConstructorCall(recFlag);
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    ecc.arguments = new Expression[length],
                    0,
                    length);
        }

        length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                ecc.typeArguments = new TypeReference[length],
                0,
                length);
        ecc.typeArgumentsSourceStart = this.intStack[this.intPtr--];
        switch (flag) {
            case 0:
                ecc.sourceStart = startPosition;
                break;
            case 1:
                --this.expressionLengthPtr;
                ecc.sourceStart =
                        (ecc.qualification = this.expressionStack[this.expressionPtr--])
                                .sourceStart;
                break;
            case 2:
                ecc.sourceStart =
                        (ecc.qualification = this.getUnspecifiedReferenceOptimized()).sourceStart;
        }

        this.pushOnAstStack(ecc);
        ecc.sourceEnd = this.endStatementPosition;
    }

    protected void consumeExpressionStatement() {
        --this.expressionLengthPtr;
        Expression expression = this.expressionStack[this.expressionPtr--];
        expression.statementEnd = this.endStatementPosition;
        expression.bits |= 1048576;
        this.pushOnAstStack(expression);
    }

    protected void consumeFieldAccess(boolean isSuperAccess) {
        FieldReference fr =
                new FieldReference(
                        this.identifierStack[this.identifierPtr],
                        this.identifierPositionStack[this.identifierPtr--]);
        --this.identifierLengthPtr;
        if (isSuperAccess) {
            fr.sourceStart = this.intStack[this.intPtr--];
            fr.receiver = new SuperReference(fr.sourceStart, this.endPosition);
            this.pushOnExpressionStack(fr);
        } else {
            fr.receiver = this.expressionStack[this.expressionPtr];
            fr.sourceStart = fr.receiver.sourceStart;
            this.expressionStack[this.expressionPtr] = fr;
        }
    }

    protected void consumeFieldDeclaration() {
        int variableDeclaratorsCounter = this.astLengthStack[this.astLengthPtr];

        int endPos;
        for (endPos = variableDeclaratorsCounter - 1; endPos >= 0; --endPos) {
            FieldDeclaration fieldDeclaration =
                    (FieldDeclaration) this.astStack[this.astPtr - endPos];
            fieldDeclaration.declarationSourceEnd = this.endStatementPosition;
            fieldDeclaration.declarationEnd = this.endStatementPosition;
        }

        this.updateSourceDeclarationParts(variableDeclaratorsCounter);
        endPos = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        int i;
        if (endPos != this.endStatementPosition) {
            for (i = 0; i < variableDeclaratorsCounter; ++i) {
                FieldDeclaration fieldDeclaration =
                        (FieldDeclaration) this.astStack[this.astPtr - i];
                fieldDeclaration.declarationSourceEnd = endPos;
            }
        }

        i = this.astPtr - this.variablesCounter[this.nestedType] + 1;
        System.arraycopy(this.astStack, i, this.astStack, i - 1, variableDeclaratorsCounter);
        --this.astPtr;
        this.astLengthStack[--this.astLengthPtr] = variableDeclaratorsCounter;
        if (this.currentElement != null) {
            this.lastCheckPoint = endPos + 1;
            if (this.currentElement.parent != null
                    && this.currentElement instanceof RecoveredField
                    && !(this.currentElement instanceof RecoveredInitializer)) {
                this.currentElement = this.currentElement.parent;
            }

            this.restartRecovery = true;
        }

        this.variablesCounter[this.nestedType] = 0;
    }

    protected void consumeForceNoDiet() {
        ++this.dietInt;
    }

    protected void consumeForInit() {
        this.pushOnAstLengthStack(-1);
        this.forStartPosition = 0;
    }

    protected void consumeSingleVariableDeclarator(boolean isVarArgs) {
        NameReference qualifyingNameReference = null;
        boolean isReceiver = this.intStack[this.intPtr--] == 0;
        if (isReceiver) {
            qualifyingNameReference = (NameReference) this.expressionStack[this.expressionPtr--];
            --this.expressionLengthPtr;
        }

        --this.identifierLengthPtr;
        char[] identifierName = this.identifierStack[this.identifierPtr];
        long namePositions = this.identifierPositionStack[this.identifierPtr--];
        int extendedDimensions = this.intStack[this.intPtr--];
        Annotation[][] annotationsOnExtendedDimensions =
                extendedDimensions == 0
                        ? null
                        : this.getAnnotationsOnDimensions(extendedDimensions);
        Annotation[] varArgsAnnotations = null;
        int endOfEllipsis = 0;
        int length;
        if (isVarArgs) {
            endOfEllipsis = this.intStack[this.intPtr--];
            if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
                System.arraycopy(
                        this.typeAnnotationStack,
                        (this.typeAnnotationPtr -= length) + 1,
                        varArgsAnnotations = new Annotation[length],
                        0,
                        length);
            }
        }

        int firstDimensions = this.intStack[this.intPtr--];
        TypeReference type = this.getTypeReference(firstDimensions);
        if (isVarArgs || extendedDimensions != 0) {
            if (isVarArgs) {
                type =
                        this.augmentTypeWithAdditionalDimensions(
                                type,
                                1,
                                varArgsAnnotations != null
                                        ? new Annotation[][] {varArgsAnnotations}
                                        : null,
                                true);
            }

            if (extendedDimensions != 0) {
                type =
                        this.augmentTypeWithAdditionalDimensions(
                                type, extendedDimensions, annotationsOnExtendedDimensions, false);
            }

            type.sourceEnd =
                    type.isParameterizedTypeReference()
                            ? this.endStatementPosition
                            : this.endPosition;
        }

        if (isVarArgs) {
            if (extendedDimensions == 0) {
                type.sourceEnd = endOfEllipsis;
            }

            type.bits |= 16384;
        }

        int modifierPositions = this.intStack[this.intPtr--];
        Object singleVariable;
        if (this.parsingRecordComponents) {
            singleVariable =
                    this.createComponent(
                            identifierName,
                            namePositions,
                            type,
                            this.intStack[this.intPtr--] & -1048577,
                            modifierPositions);
        } else if (isReceiver) {
            singleVariable =
                    new Receiver(
                            identifierName,
                            namePositions,
                            type,
                            qualifyingNameReference,
                            this.intStack[this.intPtr--] & -1048577);
        } else {
            singleVariable =
                    new Argument(
                            identifierName,
                            namePositions,
                            type,
                            this.intStack[this.intPtr--] & -1048577);
        }

        ((AbstractVariableDeclaration) singleVariable).declarationSourceStart = modifierPositions;
        ((AbstractVariableDeclaration) singleVariable).bits |= type.bits & 1048576;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    ((AbstractVariableDeclaration) singleVariable).annotations =
                            new Annotation[length],
                    0,
                    length);
            ((AbstractVariableDeclaration) singleVariable).bits |= 1048576;
            RecoveredType currentRecoveryType = this.currentRecoveryType();
            if (currentRecoveryType != null) {
                currentRecoveryType.annotationsConsumed(
                        ((AbstractVariableDeclaration) singleVariable).annotations);
            }
        }

        this.pushOnAstStack((ASTNode) singleVariable);
        ++this.listLength;
        if (isVarArgs) {
            if (!this.statementRecoveryActivated && extendedDimensions > 0) {
                this.problemReporter()
                        .illegalExtendedDimensions((AbstractVariableDeclaration) singleVariable);
            }
        } else if (this.parsingRecordComponents
                && !this.statementRecoveryActivated
                && extendedDimensions > 0) {
            this.problemReporter()
                    .recordIllegalExtendedDimensionsForRecordComponent(
                            (AbstractVariableDeclaration) singleVariable);
        }
    }

    protected Annotation[][] getAnnotationsOnDimensions(int dimensionsCount) {
        Annotation[][] dimensionsAnnotations = null;
        if (dimensionsCount > 0) {
            for (int i = 0; i < dimensionsCount; ++i) {
                Annotation[] annotations = null;
                int length;
                if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--])
                        != 0) {
                    System.arraycopy(
                            this.typeAnnotationStack,
                            (this.typeAnnotationPtr -= length) + 1,
                            annotations = new Annotation[length],
                            0,
                            length);
                    if (dimensionsAnnotations == null) {
                        dimensionsAnnotations = new Annotation[dimensionsCount][];
                    }

                    dimensionsAnnotations[dimensionsCount - i - 1] = annotations;
                }
            }
        }

        return dimensionsAnnotations;
    }

    protected void consumeSingleVariableDeclaratorList() {
        this.optimizedConcatNodeLists();
    }

    protected void consumeFormalParameterListopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeGenericType() {}

    protected void consumeGenericTypeArrayType() {}

    protected void consumeGenericTypeNameArrayType() {}

    protected void consumeGenericTypeWithDiamond() {
        this.pushOnGenericsLengthStack(-1);
        this.concatGenericsLists();
        --this.intPtr;
    }

    protected void consumeImportDeclaration() {
        ImportReference impt = (ImportReference) this.astStack[this.astPtr];
        impt.declarationEnd = this.endStatementPosition;
        impt.declarationSourceEnd = this.flushCommentsDefinedPriorTo(impt.declarationSourceEnd);
        if (this.currentElement != null) {
            this.lastCheckPoint = impt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ImportReference) impt, 0);
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeImportDeclarations() {
        this.optimizedConcatNodeLists();
    }

    protected void consumeInsideCastExpression() {}

    protected void consumeInsideCastExpressionLL1() {
        this.pushOnGenericsLengthStack(0);
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnExpressionStack(this.getTypeReference(0));
    }

    protected void consumeInsideCastExpressionLL1WithBounds() {
        int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        TypeReference[] bounds = new TypeReference[additionalBoundsLength + 1];
        this.genericsPtr -= additionalBoundsLength;
        System.arraycopy(
                this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
        this.pushOnGenericsLengthStack(0);
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        bounds[0] = this.getTypeReference(0);

        for (int i = 0; i <= additionalBoundsLength; ++i) {
            this.pushOnExpressionStack(bounds[i]);
            if (i > 0) {
                int var10002 = this.expressionLengthStack[--this.expressionLengthPtr]++;
            }
        }
    }

    protected void consumeInsideCastExpressionWithQualifiedGenerics() {}

    protected void consumeInstanceOfExpression() {
        int length = this.astLengthStack[this.astLengthPtr--];
        Object exp;
        if (length > 0) {
            Pattern pattern = (Pattern) this.astStack[this.astPtr--];
            exp = this.consumePatternInsideInstanceof(pattern);
        } else {
            TypeReference typeRef = (TypeReference) this.expressionStack[this.expressionPtr--];
            --this.expressionLengthPtr;
            this.expressionStack[this.expressionPtr] =
                    (Expression)
                            (exp =
                                    new InstanceOfExpression(
                                            this.expressionStack[this.expressionPtr], typeRef));
            int anyModifiersourceStart = this.intStack[this.intPtr--];
            int anyModifiers = this.intStack[this.intPtr--];
            if (anyModifiers != 0) {
                this.problemReporter().illegalModifiers(anyModifiersourceStart, typeRef.sourceEnd);
            }
        }

        if (((Expression) exp).sourceEnd == 0) {
            ((Expression) exp).sourceEnd = this.scanner.startPosition - 1;
        }
    }

    protected Expression consumePatternInsideInstanceof(Pattern pattern) {
        Expression exp =
                this.expressionStack[this.expressionPtr] =
                        new InstanceOfExpression(this.expressionStack[this.expressionPtr], pattern);
        return exp;
    }

    protected void consumeTypeReferenceWithModifiersAndAnnotations() {
        Annotation[] typeAnnotations = null;
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    typeAnnotations = new Annotation[length],
                    0,
                    length);
        }

        TypeReference ref = this.getTypeReference(this.intStack[this.intPtr--]);
        if (typeAnnotations != null) {
            int levels = ref.getAnnotatableLevels();
            if (ref.annotations == null) {
                ref.annotations = new Annotation[levels][];
            }

            ref.annotations[0] = typeAnnotations;
            ref.sourceStart = ref.annotations[0][0].sourceStart;
            ref.bits |= 1048576;
        }

        this.pushOnExpressionStack(ref);
    }

    protected void consumeInstanceOfClassic() {
        this.consumeTypeReferenceWithModifiersAndAnnotations();
        this.pushOnAstLengthStack(0);
    }

    protected void consumeInstanceofPattern() {
        if (this.realBlockPtr != -1) {
            this.blockReal();
        }
    }

    protected void consumeInstanceOfExpressionWithName() {
        int length = this.astLengthStack[this.astLengthPtr--];
        Object exp;
        if (length != 0) {
            Pattern pattern = (Pattern) this.astStack[this.astPtr--];
            this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
            exp = this.consumePatternInsideInstanceof(pattern);
        } else {
            TypeReference typeRef = (TypeReference) this.expressionStack[this.expressionPtr--];
            --this.expressionLengthPtr;
            this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
            this.expressionStack[this.expressionPtr] =
                    (Expression)
                            (exp =
                                    new InstanceOfExpression(
                                            this.expressionStack[this.expressionPtr], typeRef));
            --this.intPtr;
            --this.intPtr;
        }

        if (((Expression) exp).sourceEnd == 0) {
            ((Expression) exp).sourceEnd = this.scanner.startPosition - 1;
        }
    }

    protected void consumeInterfaceDeclaration() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.dispatchDeclarationInto(length);
        }

        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        typeDecl.checkConstructors(this);
        FieldDeclaration[] fields = typeDecl.fields;
        int fieldCount = fields == null ? 0 : fields.length;

        for (int i = 0; i < fieldCount; ++i) {
            FieldDeclaration field = fields[i];
            if (field instanceof Initializer) {
                this.problemReporter().interfaceCannotHaveInitializers(typeDecl.name, field);
            }
        }

        if (this.scanner.containsAssertKeyword) {
            typeDecl.bits |= 1;
        }

        typeDecl.addClinit();
        typeDecl.bodyEnd = this.endStatementPosition;
        if (length == 0 && !this.containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
            typeDecl.bits |= 8;
        }

        typeDecl.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeInterfaceHeader() {
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        if (this.currentToken == 63) {
            typeDecl.bodyStart = this.scanner.currentPosition;
        }

        if (this.currentElement != null) {
            this.restartRecovery = true;
        }

        this.scanner.commentPtr = -1;
    }

    protected void consumeInterfaceHeaderExtends() {
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        System.arraycopy(
                this.astStack,
                this.astPtr + 1,
                typeDecl.superInterfaces = new TypeReference[length],
                0,
                length);
        TypeReference[] superinterfaces = typeDecl.superInterfaces;
        TypeReference[] var7 = superinterfaces;
        int var6 = superinterfaces.length;

        for (int var5 = 0; var5 < var6; ++var5) {
            TypeReference superinterface = var7[var5];
            typeDecl.bits |= superinterface.bits & 1048576;
        }

        typeDecl.bodyStart = typeDecl.superInterfaces[length - 1].sourceEnd + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
        }
    }

    protected void consumeInterfaceHeaderName1() {
        TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
        if (this.nestedMethod[this.nestedType] == 0) {
            if (this.nestedType != 0) {
                typeDecl.bits |= 1024;
            }
        } else {
            typeDecl.bits |= 256;
            this.markEnclosingMemberWithLocalType();
            this.blockReal();
        }

        long pos = this.identifierPositionStack[this.identifierPtr];
        typeDecl.sourceEnd = (int) pos;
        typeDecl.sourceStart = (int) (pos >>> 32);
        typeDecl.name = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        typeDecl.declarationSourceStart = this.intStack[this.intPtr--];
        --this.intPtr;
        typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
        typeDecl.modifiers = this.intStack[this.intPtr--] | 512;
        if (typeDecl.modifiersSourceStart >= 0) {
            typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
        }

        if ((typeDecl.bits & 1024) == 0
                && (typeDecl.bits & 256) == 0
                && this.compilationUnit != null
                && !CharOperation.equals(typeDecl.name, this.compilationUnit.getMainTypeName())) {
            typeDecl.bits |= 4096;
        }

        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    typeDecl.annotations = new Annotation[length],
                    0,
                    length);
        }

        typeDecl.bodyStart = typeDecl.sourceEnd + 1;
        this.pushOnAstStack(typeDecl);
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
            this.currentElement = this.currentElement.add((TypeDeclaration) typeDecl, 0);
            this.lastIgnoredToken = -1;
        }

        typeDecl.javadoc = this.javadoc;
        this.javadoc = null;
    }

    protected void consumePermittedTypes() {
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        typeDecl.restrictedIdentifierStart = this.intStack[this.intPtr--];
        System.arraycopy(
                this.astStack,
                this.astPtr + 1,
                typeDecl.permittedTypes = new TypeReference[length],
                0,
                length);
        TypeReference[] var6;
        int var5 = (var6 = typeDecl.permittedTypes).length;

        for (int var4 = 0; var4 < var5; ++var4) {
            TypeReference typeReference = var6[var4];
            this.rejectIllegalTypeAnnotations(typeReference);
        }

        typeDecl.bodyStart = typeDecl.permittedTypes[length - 1].sourceEnd + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
        }
    }

    protected void consumeInterfaceMemberDeclarations() {
        this.concatNodeLists();
    }

    protected void consumeInterfaceMemberDeclarationsopt() {
        --this.nestedType;
    }

    protected void consumeInterfaceType() {
        this.pushOnAstStack(this.getTypeReference(0));
        ++this.listLength;
    }

    protected void consumeInterfaceTypeList() {
        this.optimizedConcatNodeLists();
    }

    protected void consumeInternalCompilationUnit() {
        if (this.compilationUnit.isPackageInfo()) {
            this.compilationUnit.types = new TypeDeclaration[1];
            this.compilationUnit.createPackageInfoType();
        }
    }

    protected void consumeImplicitlyDeclaredClassBodyDeclarations() {
        this.concatNodeLists();
    }

    protected void consumeInternalCompilationUnitWithPotentialImplicitlyDeclaredClass() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            LinkedList<AbstractMethodDeclaration> methods = new LinkedList();
            LinkedList<FieldDeclaration> fields = new LinkedList();
            LinkedList<TypeDeclaration> types = new LinkedList();
            int sourceStart = Integer.MAX_VALUE;

            for (int i = length - 1; i >= 0; --i) {
                ASTNode astNode = this.astStack[this.astPtr--];
                if (astNode instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) astNode;
                    if (method.declarationSourceStart < sourceStart) {
                        sourceStart = method.declarationSourceStart;
                    }

                    methods.addFirst(method);
                } else if (astNode instanceof TypeDeclaration) {
                    TypeDeclaration type = (TypeDeclaration) astNode;
                    if (type.declarationSourceStart < sourceStart) {
                        sourceStart = type.declarationSourceStart;
                    }

                    types.addFirst(type);
                } else if (astNode instanceof FieldDeclaration) {
                    FieldDeclaration field = (FieldDeclaration) astNode;
                    if (field.declarationSourceStart < sourceStart) {
                        sourceStart = field.declarationSourceStart;
                    }

                    fields.addFirst(field);
                }
            }

            if (methods.isEmpty() && fields.isEmpty()) {
                if (types.size() > 0) {
                    this.compilationUnit.types =
                            (TypeDeclaration[])
                                    types.toArray(
                                            (var0) -> {
                                                return new TypeDeclaration[var0];
                                            });
                }
            } else {
                this.problemReporter()
                        .validateJavaFeatureSupport(
                                JavaFeature.IMPLICIT_CLASSES_AND_INSTANCE_MAIN_METHODS, 0, 0);
                ImplicitTypeDeclaration implicitClass =
                        new ImplicitTypeDeclaration(this.compilationUnit.compilationResult);
                implicitClass.methods =
                        (AbstractMethodDeclaration[])
                                methods.toArray(
                                        (var0) -> {
                                            return new AbstractMethodDeclaration[var0];
                                        });
                implicitClass.createDefaultConstructor(false, true);
                implicitClass.fields =
                        (FieldDeclaration[])
                                fields.toArray(
                                        (var0) -> {
                                            return new FieldDeclaration[var0];
                                        });
                implicitClass.memberTypes =
                        (TypeDeclaration[])
                                types.toArray(
                                        (var0) -> {
                                            return new TypeDeclaration[var0];
                                        });
                implicitClass.declarationSourceStart = sourceStart;
                implicitClass.declarationSourceEnd = this.scanner.eofPosition - 1;
                implicitClass.bodyStart = sourceStart;
                implicitClass.bodyEnd = this.scanner.eofPosition - 1;
                implicitClass.sourceStart = sourceStart;
                implicitClass.sourceEnd = this.scanner.eofPosition - 1;
                types.forEach(
                        (typex) -> {
                            typex.enclosingType = implicitClass;
                        });
                this.compilationUnit.types = new TypeDeclaration[] {implicitClass};
                implicitClass.addClinit();
            }
        }
    }

    protected void consumeInternalCompilationUnitWithTypes() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            if (this.compilationUnit.isPackageInfo()) {
                this.compilationUnit.types = new TypeDeclaration[length + 1];
                this.astPtr -= length;
                System.arraycopy(
                        this.astStack, this.astPtr + 1, this.compilationUnit.types, 1, length);
                this.compilationUnit.createPackageInfoType();
            } else {
                this.compilationUnit.types = new TypeDeclaration[length];
                this.astPtr -= length;
                System.arraycopy(
                        this.astStack, this.astPtr + 1, this.compilationUnit.types, 0, length);
            }
        }
    }

    protected void consumeInvalidAnnotationTypeDeclaration() {
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        if (!this.statementRecoveryActivated) {
            this.problemReporter().illegalLocalTypeDeclaration(typeDecl);
        }

        --this.astPtr;
        this.pushOnAstLengthStack(-1);
        this.concatNodeLists();
    }

    protected void consumeInvalidConstructorDeclaration() {
        ConstructorDeclaration cd = (ConstructorDeclaration) this.astStack[this.astPtr];
        cd.bodyEnd = this.endPosition;
        cd.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        cd.modifiers |= 16777216;
    }

    protected void consumeInvalidConstructorDeclaration(boolean hasBody) {
        if (hasBody) {
            --this.intPtr;
        }

        if (hasBody) {
            --this.realBlockPtr;
        }

        int length;
        if (hasBody && (length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.astPtr -= length;
        }

        ConstructorDeclaration constructorDeclaration =
                (ConstructorDeclaration) this.astStack[this.astPtr];
        constructorDeclaration.bodyEnd = this.endStatementPosition;
        constructorDeclaration.declarationSourceEnd =
                this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        if (!hasBody) {
            constructorDeclaration.modifiers |= 16777216;
        }
    }

    protected void consumeInvalidEnumDeclaration() {
        if (this.options.sourceLevel < 3932160L) {
            TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
            if (!this.statementRecoveryActivated) {
                this.problemReporter().illegalLocalTypeDeclaration(typeDecl);
            }

            --this.astPtr;
            this.pushOnAstLengthStack(-1);
            this.concatNodeLists();
        }
    }

    protected void consumeInvalidInterfaceDeclaration() {
        if (this.options.sourceLevel < 3932160L) {
            TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
            if (!this.statementRecoveryActivated) {
                this.problemReporter().illegalLocalTypeDeclaration(typeDecl);
            }

            --this.astPtr;
            this.pushOnAstLengthStack(-1);
            this.concatNodeLists();
        }
    }

    protected void consumeInterfaceMethodDeclaration(boolean hasSemicolonBody) {
        int explicitDeclarations = 0;
        Statement[] statements = null;
        if (!hasSemicolonBody) {
            --this.intPtr;
            --this.intPtr;
            explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
            int length;
            if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
                if (this.options.ignoreMethodBodies) {
                    this.astPtr -= length;
                } else {
                    System.arraycopy(
                            this.astStack,
                            (this.astPtr -= length) + 1,
                            statements = new Statement[length],
                            0,
                            length);
                }
            }
        }

        MethodDeclaration md = (MethodDeclaration) this.astStack[this.astPtr];
        md.statements = statements;
        md.explicitDeclarations = explicitDeclarations;
        md.bodyEnd = this.endPosition;
        md.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        boolean isDefault = (md.modifiers & 65536) != 0;
        boolean isStatic = (md.modifiers & 8) != 0;
        boolean isPrivate = (md.modifiers & 2) != 0;
        boolean bodyAllowed = this.parsingJava9Plus && isPrivate || isDefault || isStatic;
        if (this.parsingJava8Plus) {
            if (bodyAllowed && hasSemicolonBody) {
                md.modifiers |= 16777216;
            }
        } else {
            if (isDefault) {
                this.problemReporter().defaultMethodsNotBelow18(md);
            }

            if (isStatic) {
                this.problemReporter().staticInterfaceMethodsNotBelow18(md);
            }
        }

        if (!bodyAllowed && !this.statementRecoveryActivated && !hasSemicolonBody) {
            this.problemReporter().abstractMethodNeedingNoBody(md);
        }
    }

    protected void consumeLabel() {}

    protected void consumeLeftParen() {
        this.pushOnIntStack(this.lParenPos);
    }

    protected void consumeLocalVariableDeclaration() {
        int variableDeclaratorsCounter = this.astLengthStack[this.astLengthPtr];
        int startIndex = this.astPtr - this.variablesCounter[this.nestedType] + 1;
        System.arraycopy(
                this.astStack,
                startIndex,
                this.astStack,
                startIndex - 1,
                variableDeclaratorsCounter);
        --this.astPtr;
        this.astLengthStack[--this.astLengthPtr] = variableDeclaratorsCounter;
        this.variablesCounter[this.nestedType] = 0;
        this.forStartPosition = 0;
    }

    protected void consumeLocalVariableDeclarationStatement() {
        int variableDeclaratorsCounter = this.astLengthStack[this.astLengthPtr];
        if (variableDeclaratorsCounter == 1) {
            LocalDeclaration localDeclaration = (LocalDeclaration) this.astStack[this.astPtr];
            if (localDeclaration.isRecoveredFromLoneIdentifier()) {
                Object left;
                if (localDeclaration.type instanceof QualifiedTypeReference) {
                    QualifiedTypeReference qtr = (QualifiedTypeReference) localDeclaration.type;
                    left = new QualifiedNameReference(qtr.tokens, qtr.sourcePositions, 0, 0);
                } else {
                    left = new SingleNameReference(localDeclaration.type.getLastToken(), 0L);
                }

                ((Expression) left).sourceStart = localDeclaration.type.sourceStart;
                ((Expression) left).sourceEnd = localDeclaration.type.sourceEnd;
                Expression right = new SingleNameReference(localDeclaration.name, 0L);
                right.sourceStart = localDeclaration.sourceStart;
                right.sourceEnd = localDeclaration.sourceEnd;
                Assignment assignment = new Assignment((Expression) left, right, 0);
                int end = this.endStatementPosition;
                int var10001;
                if (end == localDeclaration.sourceEnd) {
                    ++end;
                    var10001 = end;
                } else {
                    var10001 = end;
                }

                assignment.sourceEnd = var10001;
                assignment.statementEnd = end;
                this.astStack[this.astPtr] = assignment;
                if (this.recoveryScanner != null) {
                    RecoveryScannerData data = this.recoveryScanner.getData();

                    int position;
                    for (position = data.insertedTokensPtr;
                            position > 0
                                    && data.insertedTokensPosition[position]
                                            == data.insertedTokensPosition[position - 1];
                            --position) {}

                    if (position >= 0) {
                        this.recoveryScanner.insertTokenAhead(78, position);
                    }
                }

                if (this.currentElement != null) {
                    this.lastCheckPoint = assignment.sourceEnd + 1;
                    this.currentElement = this.currentElement.add((Statement) assignment, 0);
                }

                return;
            }
        }

        int var10002 = this.realBlockStack[this.realBlockPtr]++;

        for (int i = variableDeclaratorsCounter - 1; i >= 0; --i) {
            LocalDeclaration localDeclaration = (LocalDeclaration) this.astStack[this.astPtr - i];
            localDeclaration.declarationSourceEnd = this.endStatementPosition;
            localDeclaration.declarationEnd = this.endStatementPosition;
        }
    }

    protected void consumeMarkerAnnotation(boolean isTypeAnnotation) {
        MarkerAnnotation markerAnnotation = null;
        int oldIndex = this.identifierPtr;
        TypeReference typeReference = this.getAnnotationType();
        markerAnnotation = new MarkerAnnotation(typeReference, this.intStack[this.intPtr--]);
        markerAnnotation.declarationSourceEnd = markerAnnotation.sourceEnd;
        if (isTypeAnnotation) {
            this.pushOnTypeAnnotationStack(markerAnnotation);
        } else {
            this.pushOnExpressionStack(markerAnnotation);
        }

        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter().invalidUsageOfAnnotation(markerAnnotation);
        }

        this.recordStringLiterals = true;
        RecoveredElement var6;
        if ((var6 = this.currentElement) instanceof RecoveredAnnotation) {
            RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var6;
            this.currentElement = recoveredAnnotation.addAnnotation(markerAnnotation, oldIndex);
        }
    }

    protected void consumeMemberValueArrayInitializer() {
        this.arrayInitializer(this.expressionLengthStack[this.expressionLengthPtr--]);
    }

    protected void consumeMemberValueAsName() {
        this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
    }

    protected void consumeMemberValuePair() {
        char[] simpleName = this.identifierStack[this.identifierPtr];
        long position = this.identifierPositionStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int end = (int) position;
        int start = (int) (position >>> 32);
        Expression value = this.expressionStack[this.expressionPtr--];
        --this.expressionLengthPtr;
        MemberValuePair memberValuePair = new MemberValuePair(simpleName, start, end, value);
        this.pushOnAstStack(memberValuePair);
        RecoveredElement var9;
        if ((var9 = this.currentElement) instanceof RecoveredAnnotation) {
            RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var9;
            recoveredAnnotation.setKind(1);
        }
    }

    protected void consumeMemberValuePairs() {
        this.concatNodeLists();
    }

    protected void consumeMemberValues() {
        this.concatExpressionLists();
    }

    protected void consumeMethodBody() {
        int var10002 = this.nestedMethod[this.nestedType]--;
    }

    protected void consumeMethodDeclaration(boolean isNotAbstract, boolean isDefaultMethod) {
        if (isNotAbstract) {
            --this.intPtr;
            --this.intPtr;
        }

        int explicitDeclarations = 0;
        Statement[] statements = null;
        if (isNotAbstract) {
            explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
            int length;
            if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
                if (this.options.ignoreMethodBodies) {
                    this.astPtr -= length;
                } else {
                    System.arraycopy(
                            this.astStack,
                            (this.astPtr -= length) + 1,
                            statements = new Statement[length],
                            0,
                            length);
                }
            }
        }

        MethodDeclaration md = (MethodDeclaration) this.astStack[this.astPtr];
        md.statements = statements;
        md.explicitDeclarations = explicitDeclarations;
        if (!isNotAbstract) {
            md.modifiers |= 16777216;
        } else if ((!this.diet || this.dietInt != 0)
                && statements == null
                && !this.containsComment(md.bodyStart, this.endPosition)) {
            md.bits |= 8;
        }

        md.bodyEnd = this.endPosition;
        md.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        if (isDefaultMethod && !this.tolerateDefaultClassMethods) {
            if (this.options.sourceLevel >= 3407872L) {
                this.problemReporter()
                        .defaultModifierIllegallySpecified(md.sourceStart, md.sourceEnd);
            } else {
                this.problemReporter().illegalModifierForMethod(md);
            }
        }
    }

    protected void consumeMethodHeader() {
        AbstractMethodDeclaration method = (AbstractMethodDeclaration) this.astStack[this.astPtr];
        if (this.currentToken == 63) {
            method.bodyStart = this.scanner.currentPosition;
        }

        if (this.currentElement != null) {
            if (this.currentToken == 26) {
                method.modifiers |= 16777216;
                method.declarationSourceEnd = this.scanner.currentPosition - 1;
                method.bodyEnd = this.scanner.currentPosition - 1;
                if (this.currentElement.parseTree() == method
                        && this.currentElement.parent != null) {
                    this.currentElement = this.currentElement.parent;
                }
            } else {
                RecoveredElement var3;
                if (this.currentToken == 63
                        && (var3 = this.currentElement) instanceof RecoveredMethod) {
                    RecoveredMethod recoveredMethod = (RecoveredMethod) var3;
                    if (recoveredMethod.methodDeclaration != method) {
                        this.ignoreNextOpeningBrace = true;
                        ++this.currentElement.bracketBalance;
                    }
                }
            }

            this.restartRecovery = true;
        }
    }

    protected void consumeMethodHeaderDefaultValue() {
        MethodDeclaration md = (MethodDeclaration) this.astStack[this.astPtr];
        int length = this.expressionLengthStack[this.expressionLengthPtr--];
        if (length == 1) {
            --this.intPtr;
            --this.intPtr;
            if (md.isAnnotationMethod()) {
                ((AnnotationMethodDeclaration) md).defaultValue =
                        this.expressionStack[this.expressionPtr];
                md.modifiers |= 131072;
            }

            --this.expressionPtr;
            this.recordStringLiterals = true;
        }

        if (this.currentElement != null && md.isAnnotationMethod()) {
            this.currentElement.updateSourceEndIfNecessary(
                    ((AnnotationMethodDeclaration) md).defaultValue.sourceEnd);
        }
    }

    protected void consumeMethodHeaderExtendedDims() {
        MethodDeclaration md = (MethodDeclaration) this.astStack[this.astPtr];
        int extendedDimensions = this.intStack[this.intPtr--];
        if (md.isAnnotationMethod()) {
            ((AnnotationMethodDeclaration) md).extendedDimensions = extendedDimensions;
        }

        if (extendedDimensions != 0) {
            md.sourceEnd = this.endPosition;
            md.returnType =
                    this.augmentTypeWithAdditionalDimensions(
                            md.returnType,
                            extendedDimensions,
                            this.getAnnotationsOnDimensions(extendedDimensions),
                            false);
            md.bits |= md.returnType.bits & 1048576;
            if (this.currentToken == 63) {
                md.bodyStart = this.endPosition + 1;
            }

            if (this.currentElement != null) {
                this.lastCheckPoint = md.bodyStart;
            }
        }
    }

    protected void consumeMethodHeaderName(boolean isAnnotationMethod) {
        MethodDeclaration md = null;
        if (isAnnotationMethod) {
            md = new AnnotationMethodDeclaration(this.compilationUnit.compilationResult);
            this.recordStringLiterals = false;
        } else {
            md = new MethodDeclaration(this.compilationUnit.compilationResult);
        }

        ((MethodDeclaration) md).selector = this.identifierStack[this.identifierPtr];
        long selectorSource = this.identifierPositionStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        ((MethodDeclaration) md).returnType = this.getTypeReference(this.intStack[this.intPtr--]);
        ((MethodDeclaration) md).bits |= ((MethodDeclaration) md).returnType.bits & 1048576;
        ((MethodDeclaration) md).declarationSourceStart = this.intStack[this.intPtr--];
        ((MethodDeclaration) md).modifiers = this.intStack[this.intPtr--];
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    ((MethodDeclaration) md).annotations = new Annotation[length],
                    0,
                    length);
        }

        ((MethodDeclaration) md).javadoc = this.javadoc;
        this.javadoc = null;
        ((MethodDeclaration) md).sourceStart = (int) (selectorSource >>> 32);
        this.pushOnAstStack((ASTNode) md);
        ((MethodDeclaration) md).sourceEnd = this.lParenPos;
        ((MethodDeclaration) md).bodyStart = this.lParenPos + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            if (!(this.currentElement instanceof RecoveredType)
                    && Util.getLineNumber(
                                    ((MethodDeclaration) md).returnType.sourceStart,
                                    this.scanner.lineEnds,
                                    0,
                                    this.scanner.linePtr)
                            != Util.getLineNumber(
                                    ((MethodDeclaration) md).sourceStart,
                                    this.scanner.lineEnds,
                                    0,
                                    this.scanner.linePtr)) {
                this.lastCheckPoint = ((MethodDeclaration) md).sourceStart;
                this.restartRecovery = true;
            } else {
                this.lastCheckPoint = ((MethodDeclaration) md).bodyStart;
                this.currentElement = this.currentElement.add((AbstractMethodDeclaration) md, 0);
                this.lastIgnoredToken = -1;
            }
        }
    }

    protected void consumeMethodHeaderNameWithTypeParameters(boolean isAnnotationMethod) {
        MethodDeclaration md = null;
        if (isAnnotationMethod) {
            md = new AnnotationMethodDeclaration(this.compilationUnit.compilationResult);
            this.recordStringLiterals = false;
        } else {
            md = new MethodDeclaration(this.compilationUnit.compilationResult);
        }

        ((MethodDeclaration) md).selector = this.identifierStack[this.identifierPtr];
        long selectorSource = this.identifierPositionStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        TypeReference returnType = this.getTypeReference(this.intStack[this.intPtr--]);
        if (isAnnotationMethod) {
            this.rejectIllegalLeadingTypeAnnotations(returnType);
        }

        ((MethodDeclaration) md).returnType = returnType;
        ((MethodDeclaration) md).bits |= returnType.bits & 1048576;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                ((MethodDeclaration) md).typeParameters = new TypeParameter[length],
                0,
                length);
        ((MethodDeclaration) md).declarationSourceStart = this.intStack[this.intPtr--];
        ((MethodDeclaration) md).modifiers = this.intStack[this.intPtr--];
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    ((MethodDeclaration) md).annotations = new Annotation[length],
                    0,
                    length);
        }

        ((MethodDeclaration) md).javadoc = this.javadoc;
        this.javadoc = null;
        ((MethodDeclaration) md).sourceStart = (int) (selectorSource >>> 32);
        this.pushOnAstStack((ASTNode) md);
        ((MethodDeclaration) md).sourceEnd = this.lParenPos;
        ((MethodDeclaration) md).bodyStart = this.lParenPos + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            boolean isType;
            if (!(isType = this.currentElement instanceof RecoveredType)
                    && Util.getLineNumber(
                                    ((MethodDeclaration) md).returnType.sourceStart,
                                    this.scanner.lineEnds,
                                    0,
                                    this.scanner.linePtr)
                            != Util.getLineNumber(
                                    ((MethodDeclaration) md).sourceStart,
                                    this.scanner.lineEnds,
                                    0,
                                    this.scanner.linePtr)) {
                this.lastCheckPoint = ((MethodDeclaration) md).sourceStart;
                this.restartRecovery = true;
            } else {
                if (isType) {
                    ((RecoveredType) this.currentElement).pendingTypeParameters = null;
                }

                this.lastCheckPoint = ((MethodDeclaration) md).bodyStart;
                this.currentElement = this.currentElement.add((AbstractMethodDeclaration) md, 0);
                this.lastIgnoredToken = -1;
            }
        }
    }

    protected void consumeMethodHeaderRightParen() {
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        AbstractMethodDeclaration md = (AbstractMethodDeclaration) this.astStack[this.astPtr];
        md.sourceEnd = this.rParenPos;
        if (length != 0) {
            Argument arg = (Argument) this.astStack[this.astPtr + 1];
            int annotationSourceStart;
            if (arg.isReceiver()) {
                md.receiver = (Receiver) arg;
                if (length > 1) {
                    System.arraycopy(
                            this.astStack,
                            this.astPtr + 2,
                            md.arguments = new Argument[length - 1],
                            0,
                            length - 1);
                }

                Annotation[] annotations = arg.annotations;
                if (annotations != null && annotations.length > 0) {
                    TypeReference type = arg.type;
                    if (type.annotations == null) {
                        type.bits |= 1048576;
                        type.annotations = new Annotation[type.getAnnotatableLevels()][];
                        md.bits |= 1048576;
                    }

                    type.annotations[0] = annotations;
                    annotationSourceStart = annotations[0].sourceStart;
                    if (type.sourceStart > annotationSourceStart) {
                        type.sourceStart = annotationSourceStart;
                    }

                    arg.annotations = null;
                }

                md.bits |= arg.type.bits & 1048576;
            } else {
                System.arraycopy(
                        this.astStack,
                        this.astPtr + 1,
                        md.arguments = new Argument[length],
                        0,
                        length);
                Argument[] var7;
                annotationSourceStart = (var7 = md.arguments).length;

                for (int var9 = 0; var9 < annotationSourceStart; ++var9) {
                    Argument argument = var7[var9];
                    if ((argument.bits & 1048576) != 0) {
                        md.bits |= 1048576;
                        break;
                    }
                }
            }
        }

        md.bodyStart = this.rParenPos + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = md.bodyStart;
            if (this.currentElement.parseTree() == md) {
                return;
            }

            if (md.isConstructor()
                    && (length != 0 || this.currentToken == 63 || this.currentToken == 120)) {
                this.currentElement = this.currentElement.add((AbstractMethodDeclaration) md, 0);
                this.lastIgnoredToken = -1;
            }
        }
    }

    protected void consumeMethodHeaderThrowsClause() {
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        AbstractMethodDeclaration md = (AbstractMethodDeclaration) this.astStack[this.astPtr];
        System.arraycopy(
                this.astStack,
                this.astPtr + 1,
                md.thrownExceptions = new TypeReference[length],
                0,
                length);
        md.sourceEnd = md.thrownExceptions[length - 1].sourceEnd;
        md.bodyStart = md.thrownExceptions[length - 1].sourceEnd + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = md.bodyStart;
        }
    }

    protected void consumeInvocationExpression() {}

    protected void consumeMethodInvocationName() {
        MessageSend m = this.newMessageSend();
        m.sourceEnd = this.rParenPos;
        m.sourceStart =
                (int)
                        ((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr])
                                >>> 32);
        m.selector = this.identifierStack[this.identifierPtr--];
        if (this.identifierLengthStack[this.identifierLengthPtr] == 1) {
            m.receiver = ThisReference.implicitThis();
            --this.identifierLengthPtr;
        } else {
            int var10002 = this.identifierLengthStack[this.identifierLengthPtr]--;
            m.receiver = this.getUnspecifiedReference();
            m.sourceStart = m.receiver.sourceStart;
        }

        int length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--];
        if (length != 0) {
            Annotation[] typeAnnotations;
            System.arraycopy(
                    this.typeAnnotationStack,
                    (this.typeAnnotationPtr -= length) + 1,
                    typeAnnotations = new Annotation[length],
                    0,
                    length);
            this.problemReporter()
                    .misplacedTypeAnnotations(
                            typeAnnotations[0], typeAnnotations[typeAnnotations.length - 1]);
        }

        this.pushOnExpressionStack(m);
        this.consumeInvocationExpression();
    }

    protected void consumeMethodInvocationNameWithTypeArguments() {
        MessageSend m = this.newMessageSendWithTypeArguments();
        m.sourceEnd = this.rParenPos;
        m.sourceStart =
                (int)
                        ((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr])
                                >>> 32);
        m.selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                m.typeArguments = new TypeReference[length],
                0,
                length);
        --this.intPtr;
        m.receiver = this.getUnspecifiedReference();
        m.sourceStart = m.receiver.sourceStart;
        this.pushOnExpressionStack(m);
        this.consumeInvocationExpression();
    }

    protected void consumeMethodInvocationPrimary() {
        MessageSend m = this.newMessageSend();
        m.sourceStart =
                (int)
                        ((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr])
                                >>> 32);
        m.selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        m.receiver = this.expressionStack[this.expressionPtr];
        m.sourceStart = m.receiver.sourceStart;
        m.sourceEnd = this.rParenPos;
        this.expressionStack[this.expressionPtr] = m;
        this.consumeInvocationExpression();
    }

    protected void consumeMethodInvocationPrimaryWithTypeArguments() {
        MessageSend m = this.newMessageSendWithTypeArguments();
        m.sourceStart =
                (int)
                        ((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr])
                                >>> 32);
        m.selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                m.typeArguments = new TypeReference[length],
                0,
                length);
        --this.intPtr;
        m.receiver = this.expressionStack[this.expressionPtr];
        m.sourceStart = m.receiver.sourceStart;
        m.sourceEnd = this.rParenPos;
        this.expressionStack[this.expressionPtr] = m;
        this.consumeInvocationExpression();
    }

    protected void consumeMethodInvocationSuper() {
        MessageSend m = this.newMessageSend();
        m.sourceStart = this.intStack[this.intPtr--];
        m.sourceEnd = this.rParenPos;
        m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr];
        m.selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        m.receiver = new SuperReference(m.sourceStart, this.endPosition);
        this.pushOnExpressionStack(m);
        this.consumeInvocationExpression();
    }

    protected void consumeMethodInvocationSuperWithTypeArguments() {
        MessageSend m = this.newMessageSendWithTypeArguments();
        --this.intPtr;
        m.sourceEnd = this.rParenPos;
        m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr];
        m.selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                m.typeArguments = new TypeReference[length],
                0,
                length);
        m.sourceStart = this.intStack[this.intPtr--];
        m.receiver = new SuperReference(m.sourceStart, this.endPosition);
        this.pushOnExpressionStack(m);
        this.consumeInvocationExpression();
    }

    protected void consumeModifiers() {
        int savedModifiersSourceStart = this.modifiersSourceStart;
        this.checkComment();
        this.pushOnIntStack(this.modifiers);
        if (this.modifiersSourceStart >= savedModifiersSourceStart) {
            this.modifiersSourceStart = savedModifiersSourceStart;
        }

        this.pushOnIntStack(this.modifiersSourceStart);
        this.resetModifiers();
    }

    protected void consumeModifiers2() {
        int[] var10000 = this.expressionLengthStack;
        int var10001 = this.expressionLengthPtr - 1;
        var10000[var10001] += this.expressionLengthStack[this.expressionLengthPtr--];
    }

    protected void consumeMultipleResources() {
        this.concatNodeLists();
    }

    protected void consumeTypeAnnotation() {
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3407872L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            Annotation annotation = this.typeAnnotationStack[this.typeAnnotationPtr];
            this.problemReporter().invalidUsageOfTypeAnnotations(annotation);
        }

        this.dimensions = this.intStack[this.intPtr--];
    }

    protected void consumeOneMoreTypeAnnotation() {
        int var10002 = this.typeAnnotationLengthStack[--this.typeAnnotationLengthPtr]++;
    }

    protected void consumeNameArrayType() {
        this.pushOnGenericsLengthStack(0);
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
    }

    protected void consumeNestedMethod() {
        this.jumpOverMethodBody();
        int var10002 = this.nestedMethod[this.nestedType]++;
        this.pushOnIntStack(this.scanner.currentPosition);
        this.consumeOpenBlock();
    }

    protected void consumeNestedType() {
        int length = this.nestedMethod.length;
        if (++this.nestedType >= length) {
            System.arraycopy(
                    this.nestedMethod, 0, this.nestedMethod = new int[length + 30], 0, length);
            System.arraycopy(
                    this.variablesCounter,
                    0,
                    this.variablesCounter = new int[length + 30],
                    0,
                    length);
        }

        this.nestedMethod[this.nestedType] = 0;
        this.variablesCounter[this.nestedType] = 0;
    }

    protected void consumeNormalAnnotation(boolean isTypeAnnotation) {
        NormalAnnotation normalAnnotation = null;
        int oldIndex = this.identifierPtr;
        TypeReference typeReference = this.getAnnotationType();
        normalAnnotation = new NormalAnnotation(typeReference, this.intStack[this.intPtr--]);
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            System.arraycopy(
                    this.astStack,
                    (this.astPtr -= length) + 1,
                    normalAnnotation.memberValuePairs = new MemberValuePair[length],
                    0,
                    length);
        }

        normalAnnotation.declarationSourceEnd = this.rParenPos;
        if (isTypeAnnotation) {
            this.pushOnTypeAnnotationStack(normalAnnotation);
        } else {
            this.pushOnExpressionStack(normalAnnotation);
        }

        if (this.currentElement != null) {
            this.annotationRecoveryCheckPoint(
                    normalAnnotation.sourceStart, normalAnnotation.declarationSourceEnd);
            RecoveredElement var7;
            if ((var7 = this.currentElement) instanceof RecoveredAnnotation) {
                RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var7;
                this.currentElement = recoveredAnnotation.addAnnotation(normalAnnotation, oldIndex);
            }
        }

        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter().invalidUsageOfAnnotation(normalAnnotation);
        }

        this.recordStringLiterals = true;
    }

    protected void consumeOneDimLoop(boolean isAnnotated) {
        ++this.dimensions;
        if (!isAnnotated) {
            this.pushOnTypeAnnotationLengthStack(0);
        }
    }

    protected void consumeOnlySynchronized() {
        this.pushOnIntStack(this.synchronizedBlockSourceStart);
        this.resetModifiers();
        --this.expressionLengthPtr;
    }

    protected void consumeOnlyTypeArguments() {
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            int length = this.genericsLengthStack[this.genericsLengthPtr];
            this.problemReporter()
                    .invalidUsageOfTypeArguments(
                            (TypeReference) this.genericsStack[this.genericsPtr - length + 1],
                            (TypeReference) this.genericsStack[this.genericsPtr]);
        }
    }

    protected void consumeOnlyTypeArgumentsForCastExpression() {}

    protected void consumeOpenBlock() {
        this.pushOnIntStack(this.scanner.startPosition);
        int stackLength = this.realBlockStack.length;
        if (++this.realBlockPtr >= stackLength) {
            System.arraycopy(
                    this.realBlockStack,
                    0,
                    this.realBlockStack = new int[stackLength + 255],
                    0,
                    stackLength);
        }

        this.realBlockStack[this.realBlockPtr] = 0;
    }

    protected void consumePackageComment() {
        if (this.options.sourceLevel >= 3211264L) {
            this.checkComment();
            this.resetModifiers();
        }
    }

    protected void consumeInternalCompilationUnitWithModuleDeclaration() {
        this.compilationUnit.moduleDeclaration = (ModuleDeclaration) this.astStack[this.astPtr--];
        this.astLengthStack[this.astLengthPtr--] = 0;
    }

    protected void consumeRequiresStatement() {
        RequiresStatement req = (RequiresStatement) this.astStack[this.astPtr];
        req.declarationEnd = req.declarationSourceEnd = this.endStatementPosition;
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = req.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ModuleStatement) req, 0);
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeSingleRequiresModuleName() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
        System.arraycopy(
                this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
        ModuleReference impt;
        RequiresStatement req =
                new RequiresStatement(impt = new ModuleReference(tokens, positions));
        if (this.currentToken == 26) {
            req.declarationSourceEnd = impt.sourceEnd + 1;
        } else {
            req.declarationSourceEnd = impt.sourceEnd;
        }

        req.declarationEnd = req.declarationSourceEnd;
        req.modifiersSourceStart = this.intStack[this.intPtr--];
        req.modifiers |= this.intStack[this.intPtr--];
        req.sourceStart = req.declarationSourceStart = this.intStack[this.intPtr--];
        req.sourceEnd = impt.sourceEnd;
        this.pushOnAstStack(req);
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = req.declarationSourceEnd;
        }
    }

    protected void consumeExportsStatement() {
        ExportsStatement expt = (ExportsStatement) this.astStack[this.astPtr];
        expt.declarationSourceEnd = this.endStatementPosition;
        expt.declarationEnd = expt.declarationSourceEnd;
        if (this.currentElement instanceof RecoveredPackageVisibilityStatement) {
            this.lastCheckPoint = expt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.parent;
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeExportsHeader() {
        ImportReference impt = (ImportReference) this.astStack[this.astPtr];
        impt.bits |= 262144;
        ExportsStatement expt = new ExportsStatement(impt);
        expt.declarationSourceStart = this.intStack[this.intPtr--];
        expt.sourceStart = expt.declarationSourceStart;
        expt.sourceEnd = impt.sourceEnd;
        if (this.currentToken == 26) {
            expt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            expt.declarationSourceEnd = expt.sourceEnd;
        }

        expt.declarationEnd = expt.declarationSourceEnd;
        this.astStack[this.astPtr] = expt;
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = expt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ModuleStatement) expt, 0);
        }
    }

    protected void consumeOpensHeader() {
        ImportReference impt = (ImportReference) this.astStack[this.astPtr];
        impt.bits |= 262144;
        OpensStatement stmt = new OpensStatement(impt);
        stmt.declarationSourceStart = this.intStack[this.intPtr--];
        stmt.sourceStart = stmt.declarationSourceStart;
        stmt.sourceEnd = impt.sourceEnd;
        if (this.currentToken == 26) {
            stmt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            stmt.declarationSourceEnd = stmt.sourceEnd;
        }

        stmt.declarationEnd = stmt.declarationSourceEnd;
        this.astStack[this.astPtr] = stmt;
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = stmt.declarationSourceEnd + 1;
            this.lastCheckPoint = stmt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ModuleStatement) stmt, 0);
        }
    }

    protected void consumeOpensStatement() {
        OpensStatement expt = (OpensStatement) this.astStack[this.astPtr];
        expt.declarationSourceEnd = this.endStatementPosition;
        expt.declarationEnd = expt.declarationSourceEnd;
        if (this.currentElement instanceof RecoveredPackageVisibilityStatement) {
            this.lastCheckPoint = expt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.parent;
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeSingleTargetModuleName() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
        System.arraycopy(
                this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
        ModuleReference reference;
        this.pushOnAstStack(reference = new ModuleReference(tokens, positions));
        if (this.currentElement != null) {
            this.lastCheckPoint = reference.sourceEnd + 1;
        }
    }

    protected void consumeTargetModuleList() {
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        PackageVisibilityStatement node = (PackageVisibilityStatement) this.astStack[this.astPtr];
        if (length > 0) {
            System.arraycopy(
                    this.astStack,
                    this.astPtr + 1,
                    node.targets = new ModuleReference[length],
                    0,
                    length);
            node.sourceEnd = node.targets[length - 1].sourceEnd;
            if (this.currentToken == 26) {
                node.declarationSourceEnd = node.sourceEnd + 1;
            } else {
                node.declarationSourceEnd = node.sourceEnd;
            }
        }

        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = node.sourceEnd;
        }
    }

    protected void consumeTargetModuleNameList() {
        ++this.listLength;
        this.optimizedConcatNodeLists();
    }

    protected void consumeSinglePkgName() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
        System.arraycopy(
                this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
        ImportReference impt;
        this.pushOnAstStack(impt = new ImportReference(tokens, positions, false, 0));
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = impt.sourceEnd + 1;
        }
    }

    protected void consumeUsesStatement() {
        UsesStatement stmt = (UsesStatement) this.astStack[this.astPtr];
        stmt.declarationEnd = stmt.declarationSourceEnd = this.endStatementPosition;
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = stmt.declarationSourceEnd;
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeUsesHeader() {
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
        TypeReference siName = this.getTypeReference(0);
        if (siName.annotations != null) {
            for (int j = 0; j < siName.annotations.length; ++j) {
                Annotation[] qualifierAnnot = siName.annotations[j];
                if (qualifierAnnot != null && qualifierAnnot.length > 0) {
                    this.problemReporter()
                            .misplacedTypeAnnotations(
                                    qualifierAnnot[0], qualifierAnnot[qualifierAnnot.length - 1]);
                    siName.annotations[j] = null;
                }
            }
        }

        UsesStatement stmt = new UsesStatement(siName);
        if (this.currentToken == 26) {
            stmt.declarationSourceEnd = siName.sourceEnd + 1;
        } else {
            stmt.declarationSourceEnd = siName.sourceEnd;
        }

        stmt.declarationEnd = stmt.declarationSourceEnd;
        stmt.sourceStart = stmt.declarationSourceStart = this.intStack[this.intPtr--];
        stmt.sourceEnd = siName.sourceEnd;
        this.pushOnAstStack(stmt);
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = stmt.sourceEnd + 1;
            this.currentElement = this.currentElement.add((ModuleStatement) stmt, 0);
        }
    }

    protected void consumeProvidesInterface() {
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
        TypeReference siName = this.getTypeReference(0);
        if (siName.annotations != null) {
            for (int j = 0; j < siName.annotations.length; ++j) {
                Annotation[] qualifierAnnot = siName.annotations[j];
                if (qualifierAnnot != null && qualifierAnnot.length > 0) {
                    this.problemReporter()
                            .misplacedTypeAnnotations(
                                    qualifierAnnot[0], qualifierAnnot[qualifierAnnot.length - 1]);
                    siName.annotations[j] = null;
                }
            }
        }

        ProvidesStatement ref = new ProvidesStatement();
        ref.serviceInterface = siName;
        this.pushOnAstStack(ref);
        ref.declarationSourceStart = this.intStack[this.intPtr--];
        ref.sourceStart = ref.declarationSourceStart;
        ref.sourceEnd = siName.sourceEnd;
        ref.declarationSourceEnd = ref.sourceEnd;
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = siName.sourceEnd + 1;
            this.currentElement = this.currentElement.add((ModuleStatement) ref, 0);
            this.lastIgnoredToken = -1;
        }
    }

    protected void consumeSingleServiceImplName() {
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
        TypeReference siName = this.getTypeReference(0);
        if (siName.annotations != null) {
            for (int j = 0; j < siName.annotations.length; ++j) {
                Annotation[] qualifierAnnot = siName.annotations[j];
                if (qualifierAnnot != null && qualifierAnnot.length > 0) {
                    this.problemReporter()
                            .misplacedTypeAnnotations(
                                    qualifierAnnot[0], qualifierAnnot[qualifierAnnot.length - 1]);
                    siName.annotations[j] = null;
                }
            }
        }

        this.pushOnAstStack(siName);
        if (this.currentElement instanceof RecoveredModule) {
            this.lastCheckPoint = siName.sourceEnd + 1;
        }
    }

    protected void consumeServiceImplNameList() {
        ++this.listLength;
        this.optimizedConcatNodeLists();
    }

    protected void consumeProvidesStatement() {
        ProvidesStatement ref = (ProvidesStatement) this.astStack[this.astPtr];
        ref.declarationEnd = ref.declarationSourceEnd = this.endStatementPosition;
        if (this.currentElement instanceof RecoveredProvidesStatement) {
            this.lastIgnoredToken = -1;
            this.currentElement = this.currentElement.parent;
            this.restartRecovery = true;
        }
    }

    protected void consumeWithClause() {
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        ProvidesStatement service = (ProvidesStatement) this.astStack[this.astPtr];
        System.arraycopy(
                this.astStack,
                this.astPtr + 1,
                service.implementations = new TypeReference[length],
                0,
                length);
        service.sourceEnd = service.implementations[length - 1].sourceEnd;
        if (this.currentToken == 26) {
            service.declarationSourceEnd = service.sourceEnd + 1;
        } else {
            service.declarationSourceEnd = service.sourceEnd;
        }

        this.listLength = 0;
        if (this.currentElement instanceof RecoveredProvidesStatement) {
            this.lastCheckPoint = service.declarationSourceEnd;
        }
    }

    protected void consumeEmptyModuleStatementsOpt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeModuleStatements() {
        this.concatNodeLists();
    }

    protected void consumeModuleModifiers() {
        this.checkComment();
        int[] var10000 = this.intStack;
        int var10001 = this.intPtr - 1;
        var10000[var10001] |= this.modifiers;
        this.resetModifiers();
        var10000 = this.expressionLengthStack;
        var10001 = this.expressionLengthPtr - 1;
        var10000[var10001] += this.expressionLengthStack[this.expressionLengthPtr--];
    }

    protected void consumeModuleHeader() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, ++this.identifierPtr, tokens, 0, length);
        System.arraycopy(this.identifierPositionStack, this.identifierPtr--, positions, 0, length);
        ModuleDeclaration typeDecl =
                new ModuleDeclaration(this.compilationUnit.compilationResult, tokens, positions);
        typeDecl.declarationSourceStart = this.intStack[this.intPtr--];
        typeDecl.bodyStart = typeDecl.sourceEnd + 1;
        typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
        typeDecl.modifiers = this.intStack[this.intPtr--];
        if (typeDecl.modifiersSourceStart >= 0) {
            typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
        }

        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    typeDecl.annotations = new Annotation[length],
                    0,
                    length);
        }

        this.pushOnAstStack(typeDecl);
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
            this.currentElement = this.currentElement.add((ModuleDeclaration) typeDecl, 0);
            this.lastIgnoredToken = -1;
        }
    }

    protected void consumeModuleDeclaration() {
        this.compilationUnit.javadoc = this.javadoc;
        this.javadoc = null;
        int length = this.astLengthStack[this.astLengthPtr--];
        int[] flag = new int[length + 1];
        int size1 = 0;
        int size2 = 0;
        int size3 = 0;
        int size4 = 0;
        int size5 = 0;
        if (length != 0) {
            for (int i = length - 1; i >= 0; --i) {
                ASTNode astNode = this.astStack[this.astPtr--];
                if (astNode instanceof RequiresStatement) {
                    flag[i] = 1;
                    ++size1;
                } else if (astNode instanceof ExportsStatement) {
                    flag[i] = 2;
                    ++size2;
                } else if (astNode instanceof UsesStatement) {
                    flag[i] = 3;
                    ++size3;
                } else if (astNode instanceof ProvidesStatement) {
                    flag[i] = 4;
                    ++size4;
                } else if (astNode instanceof OpensStatement) {
                    flag[i] = 5;
                    ++size5;
                }
            }
        }

        ModuleDeclaration modul = (ModuleDeclaration) this.astStack[this.astPtr];
        modul.requiresCount = size1;
        modul.exportsCount = size2;
        modul.usesCount = size3;
        modul.servicesCount = size4;
        modul.opensCount = size5;
        modul.requires = new RequiresStatement[size1];
        modul.exports = new ExportsStatement[size2];
        modul.uses = new UsesStatement[size3];
        modul.services = new ProvidesStatement[size4];
        modul.opens = new OpensStatement[size5];
        size5 = 0;
        size4 = 0;
        size3 = 0;
        size2 = 0;
        size1 = 0;
        int flagI = flag[0];
        int start = 0;

        for (int end = 0; end <= length; ++end) {
            if (flagI != flag[end]) {
                int length2;
                switch (flagI) {
                    case 1:
                        size1 += length2 = end - start;
                        System.arraycopy(
                                this.astStack,
                                this.astPtr + start + 1,
                                modul.requires,
                                size1 - length2,
                                length2);
                        break;
                    case 2:
                        size2 += length2 = end - start;
                        System.arraycopy(
                                this.astStack,
                                this.astPtr + start + 1,
                                modul.exports,
                                size2 - length2,
                                length2);
                        break;
                    case 3:
                        size3 += length2 = end - start;
                        System.arraycopy(
                                this.astStack,
                                this.astPtr + start + 1,
                                modul.uses,
                                size3 - length2,
                                length2);
                        break;
                    case 4:
                        size4 += length2 = end - start;
                        System.arraycopy(
                                this.astStack,
                                this.astPtr + start + 1,
                                modul.services,
                                size4 - length2,
                                length2);
                        break;
                    case 5:
                        size5 += length2 = end - start;
                        System.arraycopy(
                                this.astStack,
                                this.astPtr + start + 1,
                                modul.opens,
                                size5 - length2,
                                length2);
                }

                start = end;
                flagI = flag[end];
            }
        }

        modul.bodyEnd = this.endStatementPosition;
        modul.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumePackageDeclaration() {
        ImportReference impt = this.compilationUnit.currentPackage;
        this.compilationUnit.javadoc = this.javadoc;
        this.javadoc = null;
        impt.declarationEnd = this.endStatementPosition;
        impt.declarationSourceEnd = this.flushCommentsDefinedPriorTo(impt.declarationSourceEnd);
        if (this.firstToken == 29) {
            this.unstackedAct = 17647;
        }
    }

    protected void consumePackageDeclarationName() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, ++this.identifierPtr, tokens, 0, length);
        System.arraycopy(this.identifierPositionStack, this.identifierPtr--, positions, 0, length);
        ImportReference impt = new ImportReference(tokens, positions, false, 0);
        this.compilationUnit.currentPackage = impt;
        if (this.currentToken == 26) {
            impt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            impt.declarationSourceEnd = impt.sourceEnd;
        }

        impt.declarationEnd = impt.declarationSourceEnd;
        impt.declarationSourceStart = this.intStack[this.intPtr--];
        if (this.javadoc != null) {
            impt.declarationSourceStart = this.javadoc.sourceStart;
        }

        if (this.currentElement != null) {
            this.lastCheckPoint = impt.declarationSourceEnd + 1;
            this.restartRecovery = true;
        }
    }

    protected void consumePackageDeclarationNameWithModifiers() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, ++this.identifierPtr, tokens, 0, length);
        System.arraycopy(this.identifierPositionStack, this.identifierPtr--, positions, 0, length);
        int packageModifiersSourceStart = this.intStack[this.intPtr--];
        int packageModifiers = this.intStack[this.intPtr--];
        ImportReference impt = new ImportReference(tokens, positions, false, packageModifiers);
        this.compilationUnit.currentPackage = impt;
        int packageModifiersSourceEnd;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    impt.annotations = new Annotation[length],
                    0,
                    length);
            impt.declarationSourceStart = packageModifiersSourceStart;
            packageModifiersSourceEnd = this.intStack[this.intPtr--] - 2;
        } else {
            impt.declarationSourceStart = this.intStack[this.intPtr--];
            packageModifiersSourceEnd = impt.declarationSourceStart - 2;
            if (this.javadoc != null) {
                impt.declarationSourceStart = this.javadoc.sourceStart;
            }
        }

        if ((packageModifiers & -1048577) != 0) {
            this.problemReporter()
                    .illegalModifiers(packageModifiersSourceStart, packageModifiersSourceEnd);
        }

        if (this.currentToken == 26) {
            impt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            impt.declarationSourceEnd = impt.sourceEnd;
        }

        impt.declarationEnd = impt.declarationSourceEnd;
        if (this.currentElement != null) {
            this.lastCheckPoint = impt.declarationSourceEnd + 1;
            this.restartRecovery = true;
        }
    }

    protected void consumePostfixExpression() {
        this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
    }

    protected void consumePrimaryNoNewArray() {
        Expression parenthesizedExpression = this.expressionStack[this.expressionPtr];
        this.updateSourcePosition(parenthesizedExpression);
        int numberOfParenthesis = (parenthesizedExpression.bits & 534773760) >> 21;
        parenthesizedExpression.bits &= -534773761;
        parenthesizedExpression.bits |= numberOfParenthesis + 1 << 21;
    }

    protected void consumePrimaryNoNewArrayArrayType() {
        --this.intPtr;
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
        ClassLiteralAccess cla;
        this.pushOnExpressionStack(
                cla =
                        new ClassLiteralAccess(
                                this.intStack[this.intPtr--],
                                this.getTypeReference(this.intStack[this.intPtr--])));
        this.rejectIllegalTypeAnnotations(cla.type);
    }

    protected void consumePrimaryNoNewArrayName() {
        --this.intPtr;
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
        TypeReference typeReference = this.getTypeReference(0);
        this.rejectIllegalTypeAnnotations(typeReference);
        this.pushOnExpressionStack(
                new ClassLiteralAccess(this.intStack[this.intPtr--], typeReference));
    }

    protected void rejectIllegalLeadingTypeAnnotations(TypeReference typeReference) {
        Annotation[][] annotations = typeReference.annotations;
        if (annotations != null && annotations[0] != null) {
            this.problemReporter()
                    .misplacedTypeAnnotations(
                            annotations[0][0], annotations[0][annotations[0].length - 1]);
            annotations[0] = null;
        }
    }

    private void rejectIllegalTypeAnnotations(TypeReference typeReference) {
        Annotation[][] annotations = typeReference.annotations;
        int i = 0;

        Annotation[] misplacedAnnotations;
        int length;
        for (length = annotations == null ? 0 : annotations.length; i < length; ++i) {
            misplacedAnnotations = annotations[i];
            if (misplacedAnnotations != null) {
                this.problemReporter()
                        .misplacedTypeAnnotations(
                                misplacedAnnotations[0],
                                misplacedAnnotations[misplacedAnnotations.length - 1]);
            }
        }

        annotations = typeReference.getAnnotationsOnDimensions(true);
        i = 0;

        for (length = annotations == null ? 0 : annotations.length; i < length; ++i) {
            misplacedAnnotations = annotations[i];
            if (misplacedAnnotations != null) {
                this.problemReporter()
                        .misplacedTypeAnnotations(
                                misplacedAnnotations[0],
                                misplacedAnnotations[misplacedAnnotations.length - 1]);
            }
        }

        typeReference.annotations = null;
        typeReference.setAnnotationsOnDimensions((Annotation[][]) null);
        typeReference.bits &= -1048577;
    }

    protected void consumeQualifiedSuperReceiver() {
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
        TypeReference typeReference = this.getTypeReference(0);
        this.rejectIllegalTypeAnnotations(typeReference);
        this.pushOnExpressionStack(
                new QualifiedSuperReference(
                        typeReference, this.intStack[this.intPtr--], this.endPosition));
    }

    protected void consumePrimaryNoNewArrayNameThis() {
        this.pushOnGenericsIdentifiersLengthStack(
                this.identifierLengthStack[this.identifierLengthPtr]);
        this.pushOnGenericsLengthStack(0);
        TypeReference typeReference = this.getTypeReference(0);
        this.rejectIllegalTypeAnnotations(typeReference);
        this.pushOnExpressionStack(
                new QualifiedThisReference(
                        typeReference, this.intStack[this.intPtr--], this.endPosition));
    }

    protected void consumePrimaryNoNewArrayPrimitiveArrayType() {
        --this.intPtr;
        ClassLiteralAccess cla;
        this.pushOnExpressionStack(
                cla =
                        new ClassLiteralAccess(
                                this.intStack[this.intPtr--],
                                this.getTypeReference(this.intStack[this.intPtr--])));
        this.rejectIllegalTypeAnnotations(cla.type);
    }

    protected void consumePrimaryNoNewArrayPrimitiveType() {
        --this.intPtr;
        ClassLiteralAccess cla;
        this.pushOnExpressionStack(
                cla =
                        new ClassLiteralAccess(
                                this.intStack[this.intPtr--], this.getTypeReference(0)));
        this.rejectIllegalTypeAnnotations(cla.type);
    }

    protected void consumePrimaryNoNewArrayThis() {
        this.pushOnExpressionStack(
                new ThisReference(this.intStack[this.intPtr--], this.endPosition));
    }

    protected void consumePrimaryNoNewArrayWithName() {
        this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
        Expression parenthesizedExpression = this.expressionStack[this.expressionPtr];
        this.updateSourcePosition(parenthesizedExpression);
        int numberOfParenthesis = (parenthesizedExpression.bits & 534773760) >> 21;
        parenthesizedExpression.bits &= -534773761;
        parenthesizedExpression.bits |= numberOfParenthesis + 1 << 21;
    }

    protected void consumePrimitiveArrayType() {}

    protected void consumePrimitiveType() {
        this.pushOnIntStack(0);
    }

    protected void consumePushLeftBrace() {
        this.pushOnIntStack(this.endPosition);
    }

    protected void consumePushModifiers() {
        this.pushOnIntStack(this.modifiers);
        this.pushOnIntStack(this.modifiersSourceStart);
        this.resetModifiers();
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumePushCombineModifiers() {
        --this.intPtr;
        int newModifiers = this.intStack[this.intPtr--] | 65536;
        this.intPtr -= 2;
        if ((this.intStack[this.intPtr - 1] & newModifiers) != 0) {
            newModifiers |= 4194304;
        }

        int[] var10000 = this.intStack;
        int var10001 = this.intPtr - 1;
        var10000[var10001] |= newModifiers;
        var10000 = this.expressionLengthStack;
        var10001 = this.expressionLengthPtr - 1;
        var10000[var10001] += this.expressionLengthStack[this.expressionLengthPtr--];
        if (this.currentElement != null) {
            this.currentElement.addModifier(newModifiers, this.intStack[this.intPtr]);
        }
    }

    protected void consumePushModifiersForHeader() {
        this.checkComment();
        this.pushOnIntStack(this.modifiers);
        this.pushOnIntStack(this.modifiersSourceStart);
        this.resetModifiers();
        this.pushOnExpressionStackLengthStack(0);
    }

    protected void consumePushPosition() {
        this.pushOnIntStack(this.endPosition);
    }

    protected void consumePushRealModifiers() {
        this.checkComment();
        this.pushOnIntStack(this.modifiers);
        this.pushOnIntStack(this.modifiersSourceStart);
        this.resetModifiers();
    }

    protected void consumeQualifiedName(boolean qualifiedNameIsAnnotated) {
        int var10002 = this.identifierLengthStack[--this.identifierLengthPtr]++;
        if (!qualifiedNameIsAnnotated) {
            this.pushOnTypeAnnotationLengthStack(0);
        }
    }

    protected void consumeUnannotatableQualifiedName() {
        int var10002 = this.identifierLengthStack[--this.identifierLengthPtr]++;
    }

    protected void consumeRecoveryMethodHeaderName() {
        boolean isAnnotationMethod = false;
        RecoveredElement var3;
        if ((var3 = this.currentElement) instanceof RecoveredType) {
            RecoveredType recoveredType = (RecoveredType) var3;
            isAnnotationMethod = (recoveredType.typeDeclaration.modifiers & 8192) != 0;
        } else {
            RecoveredType recoveredType = this.currentElement.enclosingType();
            if (recoveredType != null) {
                isAnnotationMethod = (recoveredType.typeDeclaration.modifiers & 8192) != 0;
            }
        }

        this.consumeMethodHeaderName(isAnnotationMethod);
    }

    protected void consumeRecoveryMethodHeaderNameWithTypeParameters() {
        boolean isAnnotationMethod = false;
        RecoveredElement var3;
        if ((var3 = this.currentElement) instanceof RecoveredType) {
            RecoveredType recoveredType = (RecoveredType) var3;
            isAnnotationMethod = (recoveredType.typeDeclaration.modifiers & 8192) != 0;
        } else {
            RecoveredType recoveredType = this.currentElement.enclosingType();
            if (recoveredType != null) {
                isAnnotationMethod = (recoveredType.typeDeclaration.modifiers & 8192) != 0;
            }
        }

        this.consumeMethodHeaderNameWithTypeParameters(isAnnotationMethod);
    }

    protected void consumeReduceImports() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.astPtr -= length;
            System.arraycopy(
                    this.astStack,
                    this.astPtr + 1,
                    this.compilationUnit.imports = new ImportReference[length],
                    0,
                    length);
        }
    }

    protected void consumeReferenceType() {
        this.pushOnIntStack(0);
    }

    protected void consumeReferenceType1() {
        this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
    }

    protected void consumeReferenceType2() {
        this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
    }

    protected void consumeReferenceType3() {
        this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
    }

    protected void consumeResourceAsLocalVariable() {
        NameReference ref = this.getUnspecifiedReference(true);
        this.pushOnAstStack(ref);
    }

    protected void consumeResourceAsThis() {
        Reference ref = new ThisReference(this.intStack[this.intPtr--], this.endPosition);
        this.pushOnAstStack(ref);
    }

    protected void consumeResourceAsFieldAccess() {
        FieldReference ref = (FieldReference) this.expressionStack[this.expressionPtr--];
        this.pushOnAstStack(ref);
    }

    protected void consumeResourceAsLocalVariableDeclaration() {
        this.consumeLocalVariableDeclaration();
    }

    protected void consumeResourceSpecification() {}

    protected void consumeResourceOptionalTrailingSemiColon(boolean punctuated) {
        Statement statement = (Statement) this.astStack[this.astPtr];
        if (punctuated && statement instanceof LocalDeclaration) {
            LocalDeclaration declaration = (LocalDeclaration) statement;
            declaration.declarationSourceEnd = this.endStatementPosition;
        }
    }

    protected void consumeRestoreDiet() {
        --this.dietInt;
    }

    protected void consumeRightParen() {
        this.pushOnIntStack(this.rParenPos);
    }

    protected void consumeNonTypeUseName() {
        for (int i = this.identifierLengthStack[this.identifierLengthPtr];
                i > 0 && this.typeAnnotationLengthPtr >= 0;
                --i) {
            int length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--];
            if (length != 0) {
                Annotation[] typeAnnotations;
                System.arraycopy(
                        this.typeAnnotationStack,
                        (this.typeAnnotationPtr -= length) + 1,
                        typeAnnotations = new Annotation[length],
                        0,
                        length);
                this.problemReporter()
                        .misplacedTypeAnnotations(
                                typeAnnotations[0], typeAnnotations[typeAnnotations.length - 1]);
            }
        }
    }

    protected void consumeZeroTypeAnnotations() {
        this.pushOnTypeAnnotationLengthStack(0);
        RecoveredElement var2;
        if ((var2 = this.currentElement) instanceof RecoveredAnnotation) {
            RecoveredAnnotation ann = (RecoveredAnnotation) var2;
            if (ann.parent instanceof RecoveredMethod) {
                RecoveredMethod meth = (RecoveredMethod) ann.parent;
                if (!meth.foundOpeningBrace && this.currentToken == 24) {
                    meth.incompleteParameterAnnotationSeen = true;
                }
            }

            if (this.identifierPtr > ann.identifierPtr) {
                ann.hasPendingMemberValueName = true;
                ann.errorToken = this.currentToken;
            }
        }
    }

    protected void consumeRule(int act) {
        switch (act) {
            case 40:
                this.consumePrimitiveType();
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 67:
            case 69:
            case 70:
            case 71:
            case 72:
            case 77:
            case 78:
            case 79:
            case 106:
            case 108:
            case 109:
            case 111:
            case 112:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 123:
            case 125:
            case 126:
            case 129:
            case 132:
            case 142:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 167:
            case 168:
            case 170:
            case 172:
            case 178:
            case 179:
            case 180:
            case 181:
            case 186:
            case 187:
            case 189:
            case 190:
            case 191:
            case 192:
            case 194:
            case 195:
            case 196:
            case 197:
            case 198:
            case 199:
            case 200:
            case 201:
            case 202:
            case 203:
            case 204:
            case 205:
            case 206:
            case 211:
            case 215:
            case 218:
            case 219:
            case 221:
            case 222:
            case 223:
            case 224:
            case 225:
            case 231:
            case 232:
            case 233:
            case 234:
            case 235:
            case 236:
            case 237:
            case 239:
            case 240:
            case 242:
            case 244:
            case 245:
            case 251:
            case 253:
            case 254:
            case 255:
            case 275:
            case 284:
            case 308:
            case 311:
            case 312:
            case 315:
            case 321:
            case 322:
            case 323:
            case 324:
            case 325:
            case 326:
            case 327:
            case 328:
            case 329:
            case 330:
            case 339:
            case 340:
            case 342:
            case 343:
            case 346:
            case 347:
            case 352:
            case 353:
            case 355:
            case 356:
            case 358:
            case 363:
            case 369:
            case 370:
            case 371:
            case 372:
            case 373:
            case 374:
            case 375:
            case 385:
            case 386:
            case 387:
            case 388:
            case 389:
            case 390:
            case 391:
            case 392:
            case 393:
            case 394:
            case 395:
            case 396:
            case 397:
            case 398:
            case 399:
            case 400:
            case 401:
            case 402:
            case 403:
            case 404:
            case 405:
            case 406:
            case 407:
            case 408:
            case 409:
            case 410:
            case 411:
            case 417:
            case 418:
            case 419:
            case 420:
            case 421:
            case 422:
            case 423:
            case 424:
            case 434:
            case 435:
            case 437:
            case 439:
            case 444:
            case 446:
            case 447:
            case 452:
            case 458:
            case 467:
            case 468:
            case 469:
            case 496:
            case 498:
            case 501:
            case 504:
            case 505:
            case 506:
            case 507:
            case 511:
            case 512:
            case 519:
            case 520:
            case 521:
            case 522:
            case 523:
            case 533:
            case 540:
            case 541:
            case 542:
            case 543:
            case 544:
            case 548:
            case 549:
            case 561:
            case 564:
            case 566:
            case 574:
            case 576:
            case 579:
            case 580:
            case 597:
            case 599:
            case 600:
            case 604:
            case 605:
            case 608:
            case 611:
            case 614:
            case 622:
            case 623:
            case 624:
            case 630:
            case 634:
            case 637:
            case 641:
            case 646:
            case 649:
            case 651:
            case 653:
            case 655:
            case 657:
            case 659:
            case 661:
            case 662:
            case 664:
            case 679:
            case 680:
            case 682:
            case 683:
            case 684:
            case 685:
            case 691:
            case 693:
            case 695:
            case 696:
            case 697:
            case 699:
            case 700:
            case 701:
            case 702:
            case 703:
            case 709:
            case 711:
            case 712:
            case 713:
            case 715:
            case 724:
            case 732:
            case 735:
            case 745:
            case 750:
            case 752:
            case 755:
            case 756:
            case 757:
            case 760:
            case 762:
            case 763:
            case 766:
            case 768:
            case 769:
            case 789:
            case 791:
            case 794:
            case 797:
            case 802:
            case 805:
            case 806:
            case 807:
            case 808:
            case 809:
            case 812:
            case 813:
            case 816:
            case 817:
            case 824:
            case 829:
            case 836:
            case 845:
            case 848:
            case 853:
            case 856:
            case 859:
            case 862:
            case 865:
            case 868:
            case 871:
            case 872:
            case 873:
            case 880:
            case 883:
            case 891:
            case 892:
            case 893:
            case 894:
            case 895:
            case 896:
            case 897:
            case 901:
            case 902:
            case 907:
            case 909:
            case 910:
            case 916:
            default:
                break;
            case 54:
                this.consumeReferenceType();
                break;
            case 58:
                this.consumeClassOrInterfaceName();
                break;
            case 59:
                this.consumeClassOrInterface();
                break;
            case 60:
                this.consumeGenericType();
                break;
            case 61:
                this.consumeGenericTypeWithDiamond();
                break;
            case 62:
                this.consumeArrayTypeWithTypeArgumentsName();
                break;
            case 63:
                this.consumePrimitiveArrayType();
                break;
            case 64:
                this.consumeNameArrayType();
                break;
            case 65:
                this.consumeGenericTypeNameArrayType();
                break;
            case 66:
                this.consumeGenericTypeArrayType();
                break;
            case 68:
                this.consumeZeroTypeAnnotations();
                break;
            case 73:
                this.consumeUnannotatableQualifiedName();
                break;
            case 74:
                this.consumeQualifiedName(false);
                break;
            case 75:
                this.consumeQualifiedName(true);
                break;
            case 76:
                this.consumeZeroTypeAnnotations();
                break;
            case 80:
                this.consumeOneMoreTypeAnnotation();
                break;
            case 81:
                this.consumeTypeAnnotation();
                break;
            case 82:
                this.consumeTypeAnnotation();
                break;
            case 83:
                this.consumeTypeAnnotation();
                break;
            case 84:
                this.consumeAnnotationName();
                break;
            case 85:
                this.consumeNormalAnnotation(true);
                break;
            case 86:
                this.consumeMarkerAnnotation(true);
                break;
            case 87:
                this.consumeSingleMemberAnnotation(true);
                break;
            case 88:
                this.consumeNonTypeUseName();
                break;
            case 89:
                this.consumeZeroTypeAnnotations();
                break;
            case 90:
                this.consumeExplicitThisParameter(false);
                break;
            case 91:
                this.consumeExplicitThisParameter(true);
                break;
            case 92:
                this.consumeVariableDeclaratorIdParameter();
                break;
            case 93:
                this.consumeCompilationUnit();
                break;
            case 94:
                this.consumeInternalCompilationUnit();
                break;
            case 95:
                this.consumeInternalCompilationUnit();
                break;
            case 96:
                this.consumeInternalCompilationUnitWithTypes();
                break;
            case 97:
                this.consumeInternalCompilationUnitWithTypes();
                break;
            case 98:
                this.consumeInternalCompilationUnit();
                break;
            case 99:
                this.consumeEmptyInternalCompilationUnit();
                break;
            case 100:
                this.consumeInternalCompilationUnitWithModuleDeclaration();
                break;
            case 101:
                this.consumeInternalCompilationUnitWithModuleDeclaration();
                break;
            case 102:
                this.consumeModuleDeclaration();
                break;
            case 103:
                this.consumeInternalCompilationUnitWithPotentialImplicitlyDeclaredClass();
                break;
            case 104:
                this.consumeInternalCompilationUnitWithPotentialImplicitlyDeclaredClass();
                break;
            case 105:
                this.consumeModuleHeader();
                break;
            case 107:
                this.consumeModuleModifiers();
                break;
            case 110:
                this.consumeEmptyModuleStatementsOpt();
                break;
            case 113:
                this.consumeModuleStatements();
                break;
            case 119:
                this.consumeRequiresStatement();
                break;
            case 120:
                this.consumeSingleRequiresModuleName();
                break;
            case 121:
                this.consumeModifiers();
                break;
            case 122:
                this.consumeDefaultModifiers();
                break;
            case 124:
                this.consumeModifiers2();
                break;
            case 127:
                this.consumeExportsStatement();
                break;
            case 128:
                this.consumeExportsHeader();
                break;
            case 130:
                this.consumeTargetModuleList();
                break;
            case 131:
                this.consumeSingleTargetModuleName();
                break;
            case 133:
                this.consumeTargetModuleNameList();
                break;
            case 134:
                this.consumeSinglePkgName();
                break;
            case 135:
                this.consumeOpensStatement();
                break;
            case 136:
                this.consumeOpensHeader();
                break;
            case 137:
                this.consumeUsesStatement();
                break;
            case 138:
                this.consumeUsesHeader();
                break;
            case 139:
                this.consumeProvidesStatement();
                break;
            case 140:
                this.consumeProvidesInterface();
                break;
            case 141:
                this.consumeSingleServiceImplName();
                break;
            case 143:
                this.consumeServiceImplNameList();
                break;
            case 144:
                this.consumeWithClause();
                break;
            case 145:
                this.consumeReduceImports();
                break;
            case 146:
                this.consumeEnterCompilationUnit();
                break;
            case 169:
                this.consumeCatchHeader();
                break;
            case 171:
                this.consumeImportDeclarations();
                break;
            case 173:
                this.consumeTypeDeclarations();
                break;
            case 174:
                this.consumePackageDeclaration();
                break;
            case 175:
                this.consumePackageDeclarationNameWithModifiers();
                break;
            case 176:
                this.consumePackageDeclarationName();
                break;
            case 177:
                this.consumePackageComment();
                break;
            case 182:
                this.consumeImportDeclaration();
                break;
            case 183:
                this.consumeSingleTypeImportDeclarationName();
                break;
            case 184:
                this.consumeImportDeclaration();
                break;
            case 185:
                this.consumeTypeImportOnDemandDeclarationName();
                break;
            case 188:
                this.consumeEmptyTypeDeclaration();
                break;
            case 193:
                this.consumeModifiers2();
                break;
            case 207:
                this.consumeAnnotationAsModifier();
                break;
            case 208:
                this.consumeClassDeclaration();
                break;
            case 209:
                this.consumeClassHeader();
                break;
            case 210:
                this.consumeTypeHeaderNameWithTypeParameters();
                break;
            case 212:
                this.consumeClassHeaderName1();
                break;
            case 213:
                this.consumeClassHeaderExtends();
                break;
            case 214:
                this.consumeClassHeaderImplements();
                break;
            case 216:
                this.consumeInterfaceTypeList();
                break;
            case 217:
                this.consumeInterfaceType();
                break;
            case 220:
                this.consumeClassBodyDeclarations();
                break;
            case 226:
                this.consumeImplicitlyDeclaredClassBodyDeclarations();
                break;
            case 227:
                this.consumeClassBodyDeclaration();
                break;
            case 228:
                this.consumeDiet();
                break;
            case 229:
                this.consumeClassBodyDeclaration();
                break;
            case 230:
                this.consumeCreateInitializer();
                break;
            case 238:
                this.consumeEmptyTypeDeclaration();
                break;
            case 241:
                this.consumeFieldDeclaration();
                break;
            case 243:
                this.consumeVariableDeclarators();
                break;
            case 246:
                this.consumeEnterVariable();
                break;
            case 247:
                this.consumeExitVariableWithInitialization();
                break;
            case 248:
                this.consumeExitVariableWithoutInitialization();
                break;
            case 249:
                this.consumeForceNoDiet();
                break;
            case 250:
                this.consumeRestoreDiet();
                break;
            case 252:
                this.consumeUnnamedVariable();
                break;
            case 256:
                this.consumeMethodDeclaration(true, false);
                break;
            case 257:
                this.consumeMethodDeclaration(true, true);
                break;
            case 258:
                this.consumeMethodDeclaration(false, false);
                break;
            case 259:
                this.consumeMethodHeader();
                break;
            case 260:
                this.consumeMethodHeader();
                break;
            case 261:
                this.consumeMethodHeaderNameWithTypeParameters(false);
                break;
            case 262:
                this.consumeMethodHeaderName(false);
                break;
            case 263:
                this.consumeMethodHeaderNameWithTypeParameters(false);
                break;
            case 264:
                this.consumeMethodHeaderName(false);
                break;
            case 265:
                this.consumePushCombineModifiers();
                break;
            case 266:
                this.consumeMethodHeaderRightParen();
                break;
            case 267:
                this.consumeMethodHeaderExtendedDims();
                break;
            case 268:
                this.consumeMethodHeaderThrowsClause();
                break;
            case 269:
                this.consumeConstructorHeader();
                break;
            case 270:
                this.consumeConstructorHeaderNameWithTypeParameters();
                break;
            case 271:
                this.consumeConstructorHeaderName(false);
                break;
            case 272:
                this.consumeConstructorDeclaration();
                break;
            case 273:
                this.consumeConstructorHeader();
                break;
            case 274:
                this.consumeConstructorHeaderName(true);
                break;
            case 276:
                this.consumeSingleVariableDeclaratorList();
                break;
            case 277:
                this.consumeSingleVariableDeclarator(false);
                break;
            case 278:
                this.consumeSingleVariableDeclarator(true);
                break;
            case 279:
                this.consumeSingleVariableDeclarator(true);
                break;
            case 280:
                this.consumeCatchFormalParameter();
                break;
            case 281:
                this.consumeCatchType();
                break;
            case 282:
                this.consumeUnionTypeAsClassType();
                break;
            case 283:
                this.consumeUnionType();
                break;
            case 285:
                this.consumeClassTypeList();
                break;
            case 286:
                this.consumeClassTypeElt();
                break;
            case 287:
                this.consumeMethodBody();
                break;
            case 288:
                this.consumeNestedMethod();
                break;
            case 289:
                this.consumeStaticInitializer();
                break;
            case 290:
                this.consumeStaticOnly();
                break;
            case 291:
                this.consumeConstructorDeclaration();
                break;
            case 292:
                this.consumeInvalidConstructorDeclaration();
                break;
            case 293:
                this.consumeExplicitConstructorInvocation(0, 3);
                break;
            case 294:
                this.consumeExplicitConstructorInvocationWithTypeArguments(0, 3);
                break;
            case 295:
                this.consumeExplicitConstructorInvocation(0, 2);
                break;
            case 296:
                this.consumeExplicitConstructorInvocationWithTypeArguments(0, 2);
                break;
            case 297:
                this.consumeExplicitConstructorInvocation(1, 2);
                break;
            case 298:
                this.consumeExplicitConstructorInvocationWithTypeArguments(1, 2);
                break;
            case 299:
                this.consumeExplicitConstructorInvocation(2, 2);
                break;
            case 300:
                this.consumeExplicitConstructorInvocationWithTypeArguments(2, 2);
                break;
            case 301:
                this.consumeExplicitConstructorInvocation(1, 3);
                break;
            case 302:
                this.consumeExplicitConstructorInvocationWithTypeArguments(1, 3);
                break;
            case 303:
                this.consumeExplicitConstructorInvocation(2, 3);
                break;
            case 304:
                this.consumeExplicitConstructorInvocationWithTypeArguments(2, 3);
                break;
            case 305:
                this.consumeInterfaceDeclaration();
                break;
            case 306:
                this.consumeInterfaceHeader();
                break;
            case 307:
                this.consumeTypeHeaderNameWithTypeParameters();
                break;
            case 309:
                this.consumeInterfaceHeaderName1();
                break;
            case 310:
                this.consumeInterfaceHeaderExtends();
                break;
            case 313:
                this.consumeInterfaceMemberDeclarations();
                break;
            case 314:
                this.consumeEmptyTypeDeclaration();
                break;
            case 316:
                this.consumeInterfaceMethodDeclaration(false);
                break;
            case 317:
                this.consumeInterfaceMethodDeclaration(false);
                break;
            case 318:
                this.consumeInterfaceMethodDeclaration(true);
                break;
            case 319:
                this.consumeInvalidConstructorDeclaration(true);
                break;
            case 320:
                this.consumeInvalidConstructorDeclaration(false);
                break;
            case 331:
                this.consumeRecordDeclaration();
                break;
            case 332:
                this.consumeRecordHeaderPart();
                break;
            case 333:
                this.consumeRecordHeaderNameWithTypeParameters();
                break;
            case 334:
                this.consumeRecordHeaderName();
                break;
            case 335:
                this.consumeRecordHeaderName1();
                break;
            case 336:
                this.consumeRecordComponentHeaderRightParen();
                break;
            case 337:
                this.consumeRecordHeader();
                break;
            case 338:
                this.consumeRecordComponentsopt();
                break;
            case 341:
                this.consumeInstanceOfExpression();
                break;
            case 344:
                this.consumeInstanceOfClassic();
                break;
            case 345:
                this.consumeInstanceofPattern();
                break;
            case 348:
                this.consumeTypePattern();
                break;
            case 349:
                this.consumeTypePattern();
                break;
            case 350:
                this.consumeRecordPattern();
                break;
            case 351:
                this.consumePatternListopt();
                break;
            case 354:
                this.consumePatternList();
                break;
            case 357:
                this.consumeUnnamedPattern();
                break;
            case 359:
                this.consumePushLeftBrace();
                break;
            case 360:
                this.consumeEmptyArrayInitializer();
                break;
            case 361:
                this.consumeArrayInitializer();
                break;
            case 362:
                this.consumeArrayInitializer();
                break;
            case 364:
                this.consumeVariableInitializers();
                break;
            case 365:
                this.consumeBlock();
                break;
            case 366:
                this.consumeOpenBlock();
                break;
            case 367:
                this.consumeBlockStatement();
                break;
            case 368:
                this.consumeBlockStatements();
                break;
            case 376:
                this.consumeInvalidInterfaceDeclaration();
                break;
            case 377:
                this.consumeInvalidAnnotationTypeDeclaration();
                break;
            case 378:
                this.consumeInvalidEnumDeclaration();
                break;
            case 379:
                this.consumeLocalVariableDeclarationStatement();
                break;
            case 380:
                this.consumeLocalVariableDeclaration();
                break;
            case 381:
                this.consumeLocalVariableDeclaration();
                break;
            case 382:
                this.consumePushModifiers();
                break;
            case 383:
                this.consumePushModifiersForHeader();
                break;
            case 384:
                this.consumePushRealModifiers();
                break;
            case 412:
                this.consumeEmptyStatement();
                break;
            case 413:
                this.consumeStatementLabel();
                break;
            case 414:
                this.consumeStatementLabel();
                break;
            case 415:
                this.consumeLabel();
                break;
            case 416:
                this.consumeExpressionStatement();
                break;
            case 425:
                this.consumePostExpressionInSwitch(true);
                break;
            case 426:
                this.consumePostExpressionInSwitch(false);
                break;
            case 427:
                this.consumePostExpressionInIf();
                break;
            case 428:
                this.consumePostExpressionInWhile();
                break;
            case 429:
                this.consumeStatementIfNoElse();
                break;
            case 430:
                this.consumeStatementIfWithElse();
                break;
            case 431:
                this.consumeStatementIfWithElse();
                break;
            case 432:
                this.consumeSwitchStatementOrExpression(true);
                break;
            case 433:
                this.consumeSwitchBlock(false);
                break;
            case 436:
                this.consumeSwitchBlock(true);
                break;
            case 438:
                this.consumeSwitchBlockStatements();
                break;
            case 440:
                this.consumeSwitchBlockStatement();
                break;
            case 441:
                this.consumeSwitchLabels(false, false);
                break;
            case 442:
                this.consumeSwitchLabels(true, false);
                break;
            case 443:
                this.consumeSwitchLabels(false, true);
                break;
            case 445:
                this.consumeDefaultLabel();
                break;
            case 448:
                this.consumeSwitchStatementOrExpression(false);
                break;
            case 449:
                this.consumeSwitchRule(Parser.SwitchRuleKind.EXPRESSION);
                break;
            case 450:
                this.consumeSwitchRule(Parser.SwitchRuleKind.BLOCK);
                break;
            case 451:
                this.consumeSwitchRule(Parser.SwitchRuleKind.THROW);
                break;
            case 453:
                this.consumeCaseLabelElements();
                break;
            case 454:
                this.consumeCaseLabelElement(Parser.CaseLabelKind.CASE_EXPRESSION);
                break;
            case 455:
                this.consumeCaseLabelElement(Parser.CaseLabelKind.CASE_DEFAULT);
                break;
            case 456:
                this.consumeCaseLabelElement(Parser.CaseLabelKind.CASE_PATTERN);
                break;
            case 457:
                this.consumeCaseLabelElement(Parser.CaseLabelKind.CASE_PATTERN);
                break;
            case 459:
                this.consumeGuard();
                break;
            case 460:
                this.consumeStatementYield();
                break;
            case 461:
                this.consumeStatementWhile();
                break;
            case 462:
                this.consumeStatementWhile();
                break;
            case 463:
                this.consumeStatementDo();
                break;
            case 464:
                this.consumeStatementFor();
                break;
            case 465:
                this.consumeStatementFor();
                break;
            case 466:
                this.consumeForInit();
                break;
            case 470:
                this.consumeStatementExpressionList();
                break;
            case 471:
                this.consumeSimpleAssertStatement();
                break;
            case 472:
                this.consumeAssertStatement();
                break;
            case 473:
                this.consumeStatementBreak();
                break;
            case 474:
                this.consumeStatementBreakWithLabel();
                break;
            case 475:
                this.consumeStatementContinue();
                break;
            case 476:
                this.consumeStatementContinueWithLabel();
                break;
            case 477:
                this.consumeStatementReturn();
                break;
            case 478:
                this.consumeStatementThrow();
                break;
            case 479:
                this.consumeStatementSynchronized();
                break;
            case 480:
                this.consumeOnlySynchronized();
                break;
            case 481:
                this.consumeStatementTry(false, false);
                break;
            case 482:
                this.consumeStatementTry(true, false);
                break;
            case 483:
                this.consumeStatementTry(false, true);
                break;
            case 484:
                this.consumeStatementTry(true, true);
                break;
            case 485:
                this.consumeResourceSpecification();
                break;
            case 486:
                this.consumeResourceOptionalTrailingSemiColon(false);
                break;
            case 487:
                this.consumeResourceOptionalTrailingSemiColon(true);
                break;
            case 488:
                this.consumeSingleResource();
                break;
            case 489:
                this.consumeMultipleResources();
                break;
            case 490:
                this.consumeResourceOptionalTrailingSemiColon(true);
                break;
            case 491:
                this.consumeResourceAsLocalVariableDeclaration();
                break;
            case 492:
                this.consumeResourceAsLocalVariableDeclaration();
                break;
            case 493:
                this.consumeResourceAsLocalVariable();
                break;
            case 494:
                this.consumeResourceAsThis();
                break;
            case 495:
                this.consumeResourceAsFieldAccess();
                break;
            case 497:
                this.consumeExitTryBlock();
                break;
            case 499:
                this.consumeCatches();
                break;
            case 500:
                this.consumeStatementCatch();
                break;
            case 502:
                this.consumeLeftParen();
                break;
            case 503:
                this.consumeRightParen();
                break;
            case 508:
                this.consumePrimaryNoNewArrayThis();
                break;
            case 509:
                this.consumePrimaryNoNewArray();
                break;
            case 510:
                this.consumePrimaryNoNewArrayWithName();
                break;
            case 513:
                this.consumePrimaryNoNewArrayNameThis();
                break;
            case 514:
                this.consumeQualifiedSuperReceiver();
                break;
            case 515:
                this.consumePrimaryNoNewArrayName();
                break;
            case 516:
                this.consumePrimaryNoNewArrayArrayType();
                break;
            case 517:
                this.consumePrimaryNoNewArrayPrimitiveArrayType();
                break;
            case 518:
                this.consumePrimaryNoNewArrayPrimitiveType();
                break;
            case 524:
                this.consumeReferenceExpressionTypeArgumentsAndTrunk(false);
                break;
            case 525:
                this.consumeReferenceExpressionTypeArgumentsAndTrunk(true);
                break;
            case 526:
                this.consumeReferenceExpressionTypeForm(true);
                break;
            case 527:
                this.consumeReferenceExpressionTypeForm(false);
                break;
            case 528:
                this.consumeReferenceExpressionGenericTypeForm();
                break;
            case 529:
                this.consumeReferenceExpressionPrimaryForm();
                break;
            case 530:
                this.consumeReferenceExpressionPrimaryForm();
                break;
            case 531:
                this.consumeReferenceExpressionSuperForm();
                break;
            case 532:
                this.consumeEmptyTypeArguments();
                break;
            case 534:
                this.consumeIdentifierOrNew(false);
                break;
            case 535:
                this.consumeIdentifierOrNew(true);
                break;
            case 536:
                this.consumeLambdaExpression();
                break;
            case 537:
                this.consumeNestedLambda();
                break;
            case 538:
                this.consumeTypeElidedLambdaParameter(false);
                break;
            case 539:
                this.consumeTypeElidedLambdaParameter(false);
                break;
            case 545:
                this.consumeSingleVariableDeclaratorList();
                break;
            case 546:
                this.consumeTypeElidedLambdaParameter(true);
                break;
            case 547:
                this.consumeBracketedTypeElidedUnderscoreLambdaParameter();
                break;
            case 550:
                this.consumeElidedLeftBraceAndReturn();
                break;
            case 551:
                this.consumeAllocationHeader();
                break;
            case 552:
                this.consumeClassInstanceCreationExpressionWithTypeArguments();
                break;
            case 553:
                this.consumeClassInstanceCreationExpression();
                break;
            case 554:
                this.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
                break;
            case 555:
                this.consumeClassInstanceCreationExpressionQualified();
                break;
            case 556:
                this.consumeClassInstanceCreationExpressionQualified();
                break;
            case 557:
                this.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
                break;
            case 558:
                this.consumeEnterInstanceCreationArgumentList();
                break;
            case 559:
                this.consumeClassInstanceCreationExpressionName();
                break;
            case 560:
                this.consumeClassBodyopt();
                break;
            case 562:
                this.consumeEnterAnonymousClassBody(false);
                break;
            case 563:
                this.consumeClassBodyopt();
                break;
            case 565:
                this.consumeEnterAnonymousClassBody(true);
                break;
            case 567:
                this.consumeArgumentList();
                break;
            case 568:
                this.consumeArrayCreationHeader();
                break;
            case 569:
                this.consumeArrayCreationHeader();
                break;
            case 570:
                this.consumeArrayCreationExpressionWithoutInitializer();
                break;
            case 571:
                this.consumeArrayCreationExpressionWithInitializer();
                break;
            case 572:
                this.consumeArrayCreationExpressionWithoutInitializer();
                break;
            case 573:
                this.consumeArrayCreationExpressionWithInitializer();
                break;
            case 575:
                this.consumeDimWithOrWithOutExprs();
                break;
            case 577:
                this.consumeDimWithOrWithOutExpr();
                break;
            case 578:
                this.consumeDims();
                break;
            case 581:
                this.consumeOneDimLoop(false);
                break;
            case 582:
                this.consumeOneDimLoop(true);
                break;
            case 583:
                this.consumeFieldAccess(false);
                break;
            case 584:
                this.consumeFieldAccess(true);
                break;
            case 585:
                this.consumeFieldAccess(false);
                break;
            case 586:
                this.consumeMethodInvocationName();
                break;
            case 587:
                this.consumeMethodInvocationNameWithTypeArguments();
                break;
            case 588:
                this.consumeMethodInvocationPrimaryWithTypeArguments();
                break;
            case 589:
                this.consumeMethodInvocationPrimary();
                break;
            case 590:
                this.consumeMethodInvocationPrimary();
                break;
            case 591:
                this.consumeMethodInvocationPrimaryWithTypeArguments();
                break;
            case 592:
                this.consumeMethodInvocationSuperWithTypeArguments();
                break;
            case 593:
                this.consumeMethodInvocationSuper();
                break;
            case 594:
                this.consumeArrayAccess(true);
                break;
            case 595:
                this.consumeArrayAccess(false);
                break;
            case 596:
                this.consumeArrayAccess(false);
                break;
            case 598:
                this.consumePostfixExpression();
                break;
            case 601:
                this.consumeUnaryExpression(14, true);
                break;
            case 602:
                this.consumeUnaryExpression(13, true);
                break;
            case 603:
                this.consumePushPosition();
                break;
            case 606:
                this.consumeUnaryExpression(14);
                break;
            case 607:
                this.consumeUnaryExpression(13);
                break;
            case 609:
                this.consumeUnaryExpression(14, false);
                break;
            case 610:
                this.consumeUnaryExpression(13, false);
                break;
            case 612:
                this.consumeUnaryExpression(12);
                break;
            case 613:
                this.consumeUnaryExpression(11);
                break;
            case 615:
                this.consumeCastExpressionWithPrimitiveType();
                break;
            case 616:
                this.consumeCastExpressionWithGenericsArray();
                break;
            case 617:
                this.consumeCastExpressionWithQualifiedGenericsArray();
                break;
            case 618:
                this.consumeCastExpressionLL1();
                break;
            case 619:
                this.consumeCastExpressionLL1WithBounds();
                break;
            case 620:
                this.consumeCastExpressionWithNameArray();
                break;
            case 621:
                this.consumeZeroAdditionalBounds();
                break;
            case 625:
                this.consumeOnlyTypeArgumentsForCastExpression();
                break;
            case 626:
                this.consumeInsideCastExpression();
                break;
            case 627:
                this.consumeInsideCastExpressionLL1();
                break;
            case 628:
                this.consumeInsideCastExpressionLL1WithBounds();
                break;
            case 629:
                this.consumeInsideCastExpressionWithQualifiedGenerics();
                break;
            case 631:
                this.consumeBinaryExpression(15);
                break;
            case 632:
                this.consumeBinaryExpression(9);
                break;
            case 633:
                this.consumeBinaryExpression(16);
                break;
            case 635:
                this.consumeBinaryExpression(14);
                break;
            case 636:
                this.consumeBinaryExpression(13);
                break;
            case 638:
                this.consumeBinaryExpression(10);
                break;
            case 639:
                this.consumeBinaryExpression(17);
                break;
            case 640:
                this.consumeBinaryExpression(19);
                break;
            case 642:
                this.consumeBinaryExpression(4);
                break;
            case 643:
                this.consumeBinaryExpression(6);
                break;
            case 644:
                this.consumeBinaryExpression(5);
                break;
            case 645:
                this.consumeBinaryExpression(7);
                break;
            case 647:
                this.consumeEqualityExpression(18);
                break;
            case 648:
                this.consumeEqualityExpression(20);
                break;
            case 650:
                this.consumeBinaryExpression(2);
                break;
            case 652:
                this.consumeBinaryExpression(8);
                break;
            case 654:
                this.consumeBinaryExpression(3);
                break;
            case 656:
                this.consumeBinaryExpression(0);
                break;
            case 658:
                this.consumeBinaryExpression(1);
                break;
            case 660:
                this.consumeConditionalExpression(22);
                break;
            case 663:
                this.consumeAssignment();
                break;
            case 665:
                this.ignoreExpressionAssignment();
                break;
            case 666:
                this.consumeAssignmentOperator(21);
                break;
            case 667:
                this.consumeAssignmentOperator(15);
                break;
            case 668:
                this.consumeAssignmentOperator(9);
                break;
            case 669:
                this.consumeAssignmentOperator(16);
                break;
            case 670:
                this.consumeAssignmentOperator(14);
                break;
            case 671:
                this.consumeAssignmentOperator(13);
                break;
            case 672:
                this.consumeAssignmentOperator(10);
                break;
            case 673:
                this.consumeAssignmentOperator(17);
                break;
            case 674:
                this.consumeAssignmentOperator(19);
                break;
            case 675:
                this.consumeAssignmentOperator(2);
                break;
            case 676:
                this.consumeAssignmentOperator(8);
                break;
            case 677:
                this.consumeAssignmentOperator(3);
                break;
            case 678:
                this.consumeExpression();
                break;
            case 681:
                this.consumeEmptyExpression();
                break;
            case 686:
                this.consumeEmptyClassBodyDeclarationsopt();
                break;
            case 687:
                this.consumeClassBodyDeclarationsopt();
                break;
            case 688:
                this.consumeDefaultModifiers();
                break;
            case 689:
                this.consumeModifiers();
                break;
            case 690:
                this.consumeEmptyBlockStatementsopt();
                break;
            case 692:
                this.consumeEmptyDimsopt();
                break;
            case 694:
                this.consumeEmptyArgumentListopt();
                break;
            case 698:
                this.consumeFormalParameterListopt();
                break;
            case 704:
                this.consumePermittedTypes();
                break;
            case 705:
                this.consumeEmptyInterfaceMemberDeclarationsopt();
                break;
            case 706:
                this.consumeInterfaceMemberDeclarationsopt();
                break;
            case 707:
                this.consumeNestedType();
                break;
            case 708:
                this.consumeEmptyForInitopt();
                break;
            case 710:
                this.consumeEmptyForUpdateopt();
                break;
            case 714:
                this.consumeEmptyCatchesopt();
                break;
            case 716:
                this.consumeEnumDeclaration();
                break;
            case 717:
                this.consumeEnumHeader();
                break;
            case 718:
                this.consumeEnumHeaderName();
                break;
            case 719:
                this.consumeEnumHeaderNameWithTypeParameters();
                break;
            case 720:
                this.consumeEnumBodyNoConstants();
                break;
            case 721:
                this.consumeEnumBodyNoConstants();
                break;
            case 722:
                this.consumeEnumBodyWithConstants();
                break;
            case 723:
                this.consumeEnumBodyWithConstants();
                break;
            case 725:
                this.consumeEnumConstants();
                break;
            case 726:
                this.consumeEnumConstantHeaderName();
                break;
            case 727:
                this.consumeEnumConstantHeader();
                break;
            case 728:
                this.consumeEnumConstantWithClassBody();
                break;
            case 729:
                this.consumeEnumConstantNoClassBody();
                break;
            case 730:
                this.consumeArguments();
                break;
            case 731:
                this.consumeEmptyArguments();
                break;
            case 733:
                this.consumeEnumDeclarations();
                break;
            case 734:
                this.consumeEmptyEnumDeclarations();
                break;
            case 736:
                this.consumeEnhancedForStatement();
                break;
            case 737:
                this.consumeEnhancedForStatement();
                break;
            case 738:
                this.consumeEnhancedForStatementHeaderInit(false);
                break;
            case 739:
                this.consumeEnhancedForStatementHeaderInit(true);
                break;
            case 740:
                this.consumeEnhancedForStatementHeader();
                break;
            case 741:
                this.consumeImportDeclaration();
                break;
            case 742:
                this.consumeSingleStaticImportDeclarationName();
                break;
            case 743:
                this.consumeImportDeclaration();
                break;
            case 744:
                this.consumeStaticImportOnDemandDeclarationName();
                break;
            case 746:
                this.consumeImportDeclaration();
                break;
            case 747:
                this.consumeSingleModuleImportDeclarationName();
                break;
            case 748:
                this.consumeTypeArguments();
                break;
            case 749:
                this.consumeOnlyTypeArguments();
                break;
            case 751:
                this.consumeTypeArgumentList1();
                break;
            case 753:
                this.consumeTypeArgumentList();
                break;
            case 754:
                this.consumeTypeArgument();
                break;
            case 758:
                this.consumeReferenceType1();
                break;
            case 759:
                this.consumeTypeArgumentReferenceType1();
                break;
            case 761:
                this.consumeTypeArgumentList2();
                break;
            case 764:
                this.consumeReferenceType2();
                break;
            case 765:
                this.consumeTypeArgumentReferenceType2();
                break;
            case 767:
                this.consumeTypeArgumentList3();
                break;
            case 770:
                this.consumeReferenceType3();
                break;
            case 771:
                this.consumeWildcard();
                break;
            case 772:
                this.consumeWildcardWithBounds();
                break;
            case 773:
                this.consumeWildcardBoundsExtends();
                break;
            case 774:
                this.consumeWildcardBoundsSuper();
                break;
            case 775:
                this.consumeWildcard1();
                break;
            case 776:
                this.consumeWildcard1WithBounds();
                break;
            case 777:
                this.consumeWildcardBounds1Extends();
                break;
            case 778:
                this.consumeWildcardBounds1Super();
                break;
            case 779:
                this.consumeWildcard2();
                break;
            case 780:
                this.consumeWildcard2WithBounds();
                break;
            case 781:
                this.consumeWildcardBounds2Extends();
                break;
            case 782:
                this.consumeWildcardBounds2Super();
                break;
            case 783:
                this.consumeWildcard3();
                break;
            case 784:
                this.consumeWildcard3WithBounds();
                break;
            case 785:
                this.consumeWildcardBounds3Extends();
                break;
            case 786:
                this.consumeWildcardBounds3Super();
                break;
            case 787:
                this.consumeTypeParameterHeader();
                break;
            case 788:
                this.consumeTypeParameters();
                break;
            case 790:
                this.consumeTypeParameterList();
                break;
            case 792:
                this.consumeTypeParameterWithExtends();
                break;
            case 793:
                this.consumeTypeParameterWithExtendsAndBounds();
                break;
            case 795:
                this.consumeAdditionalBoundList();
                break;
            case 796:
                this.consumeAdditionalBound();
                break;
            case 798:
                this.consumeTypeParameterList1();
                break;
            case 799:
                this.consumeTypeParameter1();
                break;
            case 800:
                this.consumeTypeParameter1WithExtends();
                break;
            case 801:
                this.consumeTypeParameter1WithExtendsAndBounds();
                break;
            case 803:
                this.consumeAdditionalBoundList1();
                break;
            case 804:
                this.consumeAdditionalBound1();
                break;
            case 810:
                this.consumeUnaryExpression(14);
                break;
            case 811:
                this.consumeUnaryExpression(13);
                break;
            case 814:
                this.consumeUnaryExpression(12);
                break;
            case 815:
                this.consumeUnaryExpression(11);
                break;
            case 818:
                this.consumeBinaryExpression(15);
                break;
            case 819:
                this.consumeBinaryExpressionWithName(15);
                break;
            case 820:
                this.consumeBinaryExpression(9);
                break;
            case 821:
                this.consumeBinaryExpressionWithName(9);
                break;
            case 822:
                this.consumeBinaryExpression(16);
                break;
            case 823:
                this.consumeBinaryExpressionWithName(16);
                break;
            case 825:
                this.consumeBinaryExpression(14);
                break;
            case 826:
                this.consumeBinaryExpressionWithName(14);
                break;
            case 827:
                this.consumeBinaryExpression(13);
                break;
            case 828:
                this.consumeBinaryExpressionWithName(13);
                break;
            case 830:
                this.consumeBinaryExpression(10);
                break;
            case 831:
                this.consumeBinaryExpressionWithName(10);
                break;
            case 832:
                this.consumeBinaryExpression(17);
                break;
            case 833:
                this.consumeBinaryExpressionWithName(17);
                break;
            case 834:
                this.consumeBinaryExpression(19);
                break;
            case 835:
                this.consumeBinaryExpressionWithName(19);
                break;
            case 837:
                this.consumeBinaryExpression(4);
                break;
            case 838:
                this.consumeBinaryExpressionWithName(4);
                break;
            case 839:
                this.consumeBinaryExpression(6);
                break;
            case 840:
                this.consumeBinaryExpressionWithName(6);
                break;
            case 841:
                this.consumeBinaryExpression(5);
                break;
            case 842:
                this.consumeBinaryExpressionWithName(5);
                break;
            case 843:
                this.consumeBinaryExpression(7);
                break;
            case 844:
                this.consumeBinaryExpressionWithName(7);
                break;
            case 846:
                this.consumeInstanceOfExpressionWithName();
                break;
            case 847:
                this.consumeInstanceOfExpression();
                break;
            case 849:
                this.consumeEqualityExpression(18);
                break;
            case 850:
                this.consumeEqualityExpressionWithName(18);
                break;
            case 851:
                this.consumeEqualityExpression(20);
                break;
            case 852:
                this.consumeEqualityExpressionWithName(20);
                break;
            case 854:
                this.consumeBinaryExpression(2);
                break;
            case 855:
                this.consumeBinaryExpressionWithName(2);
                break;
            case 857:
                this.consumeBinaryExpression(8);
                break;
            case 858:
                this.consumeBinaryExpressionWithName(8);
                break;
            case 860:
                this.consumeBinaryExpression(3);
                break;
            case 861:
                this.consumeBinaryExpressionWithName(3);
                break;
            case 863:
                this.consumeBinaryExpression(0);
                break;
            case 864:
                this.consumeBinaryExpressionWithName(0);
                break;
            case 866:
                this.consumeBinaryExpression(1);
                break;
            case 867:
                this.consumeBinaryExpressionWithName(1);
                break;
            case 869:
                this.consumeConditionalExpression(22);
                break;
            case 870:
                this.consumeConditionalExpressionWithName(22);
                break;
            case 874:
                this.consumeAnnotationTypeDeclarationHeaderName();
                break;
            case 875:
                this.consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters();
                break;
            case 876:
                this.consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters();
                break;
            case 877:
                this.consumeAnnotationTypeDeclarationHeaderName();
                break;
            case 878:
                this.consumeAnnotationTypeDeclarationHeader();
                break;
            case 879:
                this.consumeAnnotationTypeDeclaration();
                break;
            case 881:
                this.consumeEmptyAnnotationTypeMemberDeclarationsopt();
                break;
            case 882:
                this.consumeAnnotationTypeMemberDeclarationsopt();
                break;
            case 884:
                this.consumeAnnotationTypeMemberDeclarations();
                break;
            case 885:
                this.consumeMethodHeaderNameWithTypeParameters(true);
                break;
            case 886:
                this.consumeMethodHeaderName(true);
                break;
            case 887:
                this.consumeEmptyMethodHeaderDefaultValue();
                break;
            case 888:
                this.consumeMethodHeaderDefaultValue();
                break;
            case 889:
                this.consumeMethodHeader();
                break;
            case 890:
                this.consumeAnnotationTypeMemberDeclaration();
                break;
            case 898:
                this.consumeAnnotationName();
                break;
            case 899:
                this.consumeNormalAnnotation(false);
                break;
            case 900:
                this.consumeEmptyMemberValuePairsopt();
                break;
            case 903:
                this.consumeMemberValuePairs();
                break;
            case 904:
                this.consumeMemberValuePair();
                break;
            case 905:
                this.consumeEnterMemberValue();
                break;
            case 906:
                this.consumeExitMemberValue();
                break;
            case 908:
                this.consumeMemberValueAsName();
                break;
            case 911:
                this.consumeMemberValueArrayInitializer();
                break;
            case 912:
                this.consumeMemberValueArrayInitializer();
                break;
            case 913:
                this.consumeEmptyMemberValueArrayInitializer();
                break;
            case 914:
                this.consumeEmptyMemberValueArrayInitializer();
                break;
            case 915:
                this.consumeEnterMemberValueArrayInitializer();
                break;
            case 917:
                this.consumeMemberValues();
                break;
            case 918:
                this.consumeMarkerAnnotation(false);
                break;
            case 919:
                this.consumeSingleMemberAnnotationMemberValue();
                break;
            case 920:
                this.consumeSingleMemberAnnotation(false);
                break;
            case 921:
                this.consumeRecoveryMethodHeaderNameWithTypeParameters();
                break;
            case 922:
                this.consumeRecoveryMethodHeaderName();
                break;
            case 923:
                this.consumeRecoveryMethodHeaderNameWithTypeParameters();
                break;
            case 924:
                this.consumeRecoveryMethodHeaderName();
                break;
            case 925:
                this.consumeMethodHeader();
                break;
            case 926:
                this.consumeMethodHeader();
        }
    }

    protected void consumePostExpressionInIf() {}

    protected void consumePostExpressionInSwitch(boolean statSwitch) {}

    protected void consumePostExpressionInWhile() {}

    protected void consumeVariableDeclaratorIdParameter() {
        this.pushOnIntStack(1);
    }

    protected void consumeExplicitThisParameter(boolean isQualified) {
        NameReference qualifyingNameReference = null;
        if (isQualified) {
            qualifyingNameReference = this.getUnspecifiedReference(false);
        }

        this.pushOnExpressionStack(qualifyingNameReference);
        int thisStart = this.intStack[this.intPtr--];
        this.pushIdentifier(ConstantPool.This, ((long) thisStart << 32) + (long) (thisStart + 3));
        this.pushOnIntStack(0);
        this.pushOnIntStack(0);
    }

    protected boolean isAssistParser() {
        return false;
    }

    protected void consumeNestedLambda() {
        this.consumeNestedType();
        int var10002 = this.nestedMethod[this.nestedType]++;
        LambdaExpression lambda =
                new LambdaExpression(this.compilationUnit.compilationResult, this.isAssistParser());
        this.pushOnAstStack(lambda);
        this.processingLambdaParameterList = true;
    }

    protected void consumeLambdaHeader() {
        int arrowPosition = this.scanner.currentPosition - 1;
        Argument[] arguments = null;
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        if (length != 0) {
            System.arraycopy(
                    this.astStack, this.astPtr + 1, arguments = new Argument[length], 0, length);
        }

        for (int i = 0; i < length; ++i) {
            Argument argument = arguments[i];
            if (argument.isReceiver()) {
                this.problemReporter().illegalThis(argument);
            }

            if (this.parsingJava8Plus
                    && !JavaFeature.UNNAMMED_PATTERNS_AND_VARS.isSupported(this.options)
                    && argument.name.length == 1
                    && argument.name[0] == '_') {
                if (this.parsingJava22Plus) {
                    this.problemReporter()
                            .validateJavaFeatureSupport(
                                    JavaFeature.UNNAMMED_PATTERNS_AND_VARS,
                                    argument.sourceStart,
                                    argument.sourceEnd);
                } else {
                    this.problemReporter()
                            .illegalUseOfUnderscoreAsAnIdentifier(
                                    argument.sourceStart, argument.sourceEnd, true, false);
                }
            }
        }

        LambdaExpression lexp = (LambdaExpression) this.astStack[this.astPtr];
        lexp.setArguments(arguments);
        lexp.setArrowPosition(arrowPosition);
        lexp.sourceEnd = this.intStack[this.intPtr--];
        lexp.sourceStart = this.intStack[this.intPtr--];
        lexp.hasParentheses = this.scanner.getSource()[lexp.sourceStart] == '(';
        this.listLength -= arguments == null ? 0 : arguments.length;
        this.processingLambdaParameterList = false;
        if (this.currentElement != null) {
            this.lastCheckPoint = arrowPosition + 1;
            ++this.currentElement.lambdaNestLevel;
        }
    }

    private void setArgumentsTypeVar(LambdaExpression lexp) {
        Argument[] args = lexp.arguments;
        if (this.parsingJava11Plus && args != null && args.length != 0) {
            boolean isVar = false;
            boolean mixReported = false;
            int i = 0;

            for (int l = args.length; i < l; ++i) {
                Argument arg = args[i];
                TypeReference type = arg.type;
                char[][] typeName = type != null ? type.getTypeName() : null;
                boolean prev = isVar;
                isVar =
                        typeName != null
                                && typeName.length == 1
                                && CharOperation.equals(typeName[0], TypeConstants.VAR);
                lexp.argumentsTypeVar |= isVar;
                if (i > 0 && prev != isVar && !mixReported) {
                    this.problemReporter()
                            .varCannotBeMixedWithNonVarParams(isVar ? arg : args[i - 1]);
                    mixReported = true;
                }

                if (isVar && (type.dimensions() > 0 || type.extraDimensions() > 0)) {
                    this.problemReporter().varLocalCannotBeArray(arg);
                }
            }

        } else {
            lexp.argumentsTypeVar = false;
        }
    }

    protected void consumeLambdaExpression() {
        --this.nestedType;
        --this.astLengthPtr;
        Statement body = (Statement) this.astStack[this.astPtr--];
        if (body instanceof Block && this.options.ignoreMethodBodies) {
            Statement oldBody = body;
            body = new Block(0);
            ((Statement) body).sourceStart = ((Statement) oldBody).sourceStart;
            ((Statement) body).sourceEnd = ((Statement) oldBody).sourceEnd;
        }

        LambdaExpression lexp = (LambdaExpression) this.astStack[this.astPtr--];
        --this.astLengthPtr;
        lexp.setBody((Statement) body);
        lexp.sourceEnd = ((Statement) body).sourceEnd;
        if (body instanceof Expression) {
            Expression expression = (Expression) body;
            if (expression.isTrulyExpression()) {
                expression.statementEnd = ((Statement) body).sourceEnd;
            }
        }

        if (!this.parsingJava8Plus) {
            this.problemReporter().lambdaExpressionsNotBelow18(lexp);
        }

        this.setArgumentsTypeVar(lexp);
        this.pushOnExpressionStack(lexp);
        if (this.currentElement != null) {
            this.lastCheckPoint = ((Statement) body).sourceEnd + 1;
            --this.currentElement.lambdaNestLevel;
        }

        this.referenceContext.compilationResult().hasFunctionalTypes = true;
        this.markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind.LAMBDA);
        this.stashTextualRepresentation(lexp);
    }

    private void stashTextualRepresentation(FunctionalExpression fnExp) {
        int length = fnExp.sourceEnd - fnExp.sourceStart + 1;
        System.arraycopy(
                this.scanner.getSource(),
                fnExp.sourceStart,
                fnExp.text = new char[length],
                0,
                length);
    }

    protected Argument typeElidedArgument() {
        --this.identifierLengthPtr;
        char[] identifierName = this.identifierStack[this.identifierPtr];
        long namePositions = this.identifierPositionStack[this.identifierPtr--];
        Argument arg = new Argument(identifierName, namePositions, (TypeReference) null, 0, true);
        arg.declarationSourceStart = (int) (namePositions >>> 32);
        return arg;
    }

    protected void consumeTypeElidedLambdaParameter(boolean parenthesized) {
        int modifier = 0;
        int annotationLength = 0;
        int modifiersStart = 0;
        if (parenthesized) {
            modifiersStart = this.intStack[this.intPtr--];
            modifier = this.intStack[this.intPtr--];
            annotationLength = this.expressionLengthStack[this.expressionLengthPtr--];
            this.expressionPtr -= annotationLength;
        }

        Argument arg = this.typeElidedArgument();
        if (modifier != 0 || annotationLength != 0) {
            this.problemReporter().illegalModifiersForElidedType(arg);
            arg.declarationSourceStart = modifiersStart;
        }

        if (!parenthesized) {
            this.pushOnIntStack(arg.declarationSourceStart);
            this.pushOnIntStack(arg.declarationSourceEnd);
        }

        this.pushOnAstStack(arg);
        ++this.listLength;
    }

    protected void consumeBracketedTypeElidedUnderscoreLambdaParameter() {
        this.consumeDefaultModifiers();
        this.consumeTypeElidedLambdaParameter(true);
    }

    protected void consumeElidedLeftBraceAndReturn() {
        int stackLength = this.stateStackLengthStack.length;
        if (++this.valueLambdaNestDepth >= stackLength) {
            System.arraycopy(
                    this.stateStackLengthStack,
                    0,
                    this.stateStackLengthStack = new int[stackLength + 4],
                    0,
                    stackLength);
        }

        this.stateStackLengthStack[this.valueLambdaNestDepth] = this.stateStackTop;
    }

    protected void consumeExpression() {
        if (this.valueLambdaNestDepth >= 0
                && this.stateStackLengthStack[this.valueLambdaNestDepth]
                        == this.stateStackTop - 1) {
            --this.valueLambdaNestDepth;
            this.scanner.ungetToken(this.currentToken);
            this.currentToken = 73;
            Expression exp = this.expressionStack[this.expressionPtr--];
            --this.expressionLengthPtr;
            this.pushOnAstStack(exp);
        }
    }

    protected void consumeIdentifierOrNew(boolean newForm) {
        if (newForm) {
            int newStart = this.intStack[this.intPtr--];
            this.pushIdentifier(ConstantPool.Init, ((long) newStart << 32) + (long) (newStart + 2));
        }
    }

    protected void consumeEmptyTypeArguments() {
        this.pushOnGenericsLengthStack(0);
    }

    public ReferenceExpression newReferenceExpression() {
        return new ReferenceExpression(this.scanner);
    }

    protected void consumeReferenceExpressionTypeForm(boolean isPrimitive) {
        ReferenceExpression referenceExpression = this.newReferenceExpression();
        TypeReference[] typeArguments = null;
        int sourceEnd = (int) this.identifierPositionStack[this.identifierPtr];
        referenceExpression.nameSourceStart =
                (int) (this.identifierPositionStack[this.identifierPtr] >>> 32);
        char[] selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        if (length > 0) {
            this.genericsPtr -= length;
            System.arraycopy(
                    this.genericsStack,
                    this.genericsPtr + 1,
                    typeArguments = new TypeReference[length],
                    0,
                    length);
            --this.intPtr;
        }

        int dimension = this.intStack[this.intPtr--];
        boolean typeAnnotatedName = false;
        int i = this.identifierLengthStack[this.identifierLengthPtr];

        for (int j = 0; i > 0 && this.typeAnnotationLengthPtr >= 0; ++j) {
            length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr - j];
            if (length != 0) {
                typeAnnotatedName = true;
                break;
            }

            --i;
        }

        if (dimension <= 0 && !typeAnnotatedName) {
            referenceExpression.initialize(
                    this.compilationUnit.compilationResult,
                    this.getUnspecifiedReference(),
                    typeArguments,
                    selector,
                    sourceEnd);
        } else {
            if (!isPrimitive) {
                this.pushOnGenericsLengthStack(0);
                this.pushOnGenericsIdentifiersLengthStack(
                        this.identifierLengthStack[this.identifierLengthPtr]);
            }

            referenceExpression.initialize(
                    this.compilationUnit.compilationResult,
                    this.getTypeReference(dimension),
                    typeArguments,
                    selector,
                    sourceEnd);
        }

        if (CharOperation.equals(selector, TypeConstants.INIT)
                && referenceExpression.lhs instanceof NameReference) {
            Expression var10000 = referenceExpression.lhs;
            var10000.bits &= -4;
        }

        this.consumeReferenceExpression(referenceExpression);
    }

    protected void consumeReferenceExpressionPrimaryForm() {
        ReferenceExpression referenceExpression = this.newReferenceExpression();
        TypeReference[] typeArguments = null;
        int sourceEnd = (int) this.identifierPositionStack[this.identifierPtr];
        referenceExpression.nameSourceStart =
                (int) (this.identifierPositionStack[this.identifierPtr] >>> 32);
        char[] selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        if (length > 0) {
            this.genericsPtr -= length;
            System.arraycopy(
                    this.genericsStack,
                    this.genericsPtr + 1,
                    typeArguments = new TypeReference[length],
                    0,
                    length);
            --this.intPtr;
        }

        Expression primary = this.expressionStack[this.expressionPtr--];
        --this.expressionLengthPtr;
        referenceExpression.initialize(
                this.compilationUnit.compilationResult,
                primary,
                typeArguments,
                selector,
                sourceEnd);
        this.consumeReferenceExpression(referenceExpression);
    }

    protected void consumeReferenceExpressionSuperForm() {
        ReferenceExpression referenceExpression = this.newReferenceExpression();
        TypeReference[] typeArguments = null;
        int sourceEnd = (int) this.identifierPositionStack[this.identifierPtr];
        referenceExpression.nameSourceStart =
                (int) (this.identifierPositionStack[this.identifierPtr] >>> 32);
        char[] selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        if (length > 0) {
            this.genericsPtr -= length;
            System.arraycopy(
                    this.genericsStack,
                    this.genericsPtr + 1,
                    typeArguments = new TypeReference[length],
                    0,
                    length);
            --this.intPtr;
        }

        SuperReference superReference =
                new SuperReference(this.intStack[this.intPtr--], this.endPosition);
        referenceExpression.initialize(
                this.compilationUnit.compilationResult,
                superReference,
                typeArguments,
                selector,
                sourceEnd);
        this.consumeReferenceExpression(referenceExpression);
    }

    protected void consumeReferenceExpression(ReferenceExpression referenceExpression) {
        this.pushOnExpressionStack(referenceExpression);
        if (!this.parsingJava8Plus) {
            this.problemReporter().referenceExpressionsNotBelow18(referenceExpression);
        }

        this.stashTextualRepresentation(referenceExpression);
        this.referenceContext.compilationResult().hasFunctionalTypes = true;
        this.markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind.METHOD_REFERENCE);
    }

    protected void consumeReferenceExpressionTypeArgumentsAndTrunk(boolean qualified) {
        this.pushOnIntStack(qualified ? 1 : 0);
        this.pushOnIntStack(this.scanner.startPosition - 1);
    }

    protected void consumeReferenceExpressionGenericTypeForm() {
        ReferenceExpression referenceExpression = this.newReferenceExpression();
        TypeReference[] typeArguments = null;
        int sourceEnd = (int) this.identifierPositionStack[this.identifierPtr];
        referenceExpression.nameSourceStart =
                (int) (this.identifierPositionStack[this.identifierPtr] >>> 32);
        char[] selector = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        if (length > 0) {
            this.genericsPtr -= length;
            System.arraycopy(
                    this.genericsStack,
                    this.genericsPtr + 1,
                    typeArguments = new TypeReference[length],
                    0,
                    length);
            --this.intPtr;
        }

        int typeSourceEnd = this.intStack[this.intPtr--];
        boolean qualified = this.intStack[this.intPtr--] != 0;
        int dims = this.intStack[this.intPtr--];
        Object type;
        if (qualified) {
            Annotation[][] annotationsOnDimensions =
                    dims == 0 ? null : this.getAnnotationsOnDimensions(dims);
            TypeReference rightSide = this.getTypeReference(0);
            type =
                    this.computeQualifiedGenericsFromRightSide(
                            rightSide, dims, annotationsOnDimensions);
        } else {
            this.pushOnGenericsIdentifiersLengthStack(
                    this.identifierLengthStack[this.identifierLengthPtr]);
            type = this.getTypeReference(dims);
        }

        --this.intPtr;
        ((TypeReference) type).sourceEnd = typeSourceEnd;
        referenceExpression.initialize(
                this.compilationUnit.compilationResult,
                (Expression) type,
                typeArguments,
                selector,
                sourceEnd);
        this.consumeReferenceExpression(referenceExpression);
    }

    protected void consumeEnterInstanceCreationArgumentList() {}

    protected void consumeSimpleAssertStatement() {
        --this.expressionLengthPtr;
        this.pushOnAstStack(
                new AssertStatement(
                        this.expressionStack[this.expressionPtr--], this.intStack[this.intPtr--]));
    }

    protected void consumeSingleMemberAnnotation(boolean isTypeAnnotation) {
        SingleMemberAnnotation singleMemberAnnotation = null;
        int oldIndex = this.identifierPtr;
        TypeReference typeReference = this.getAnnotationType();
        singleMemberAnnotation =
                new SingleMemberAnnotation(typeReference, this.intStack[this.intPtr--]);
        singleMemberAnnotation.memberValue = this.expressionStack[this.expressionPtr--];
        --this.expressionLengthPtr;
        singleMemberAnnotation.declarationSourceEnd = this.rParenPos;
        if (isTypeAnnotation) {
            this.pushOnTypeAnnotationStack(singleMemberAnnotation);
        } else {
            this.pushOnExpressionStack(singleMemberAnnotation);
        }

        if (this.currentElement != null) {
            this.annotationRecoveryCheckPoint(
                    singleMemberAnnotation.sourceStart,
                    singleMemberAnnotation.declarationSourceEnd);
            RecoveredElement var6;
            if ((var6 = this.currentElement) instanceof RecoveredAnnotation) {
                RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var6;
                this.currentElement =
                        recoveredAnnotation.addAnnotation(singleMemberAnnotation, oldIndex);
            }
        }

        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            this.problemReporter().invalidUsageOfAnnotation(singleMemberAnnotation);
        }

        this.recordStringLiterals = true;
    }

    protected void consumeSingleMemberAnnotationMemberValue() {
        RecoveredElement var2;
        if (this.currentElement != null
                && (var2 = this.currentElement) instanceof RecoveredAnnotation) {
            RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var2;
            recoveredAnnotation.setKind(2);
        }
    }

    protected void consumeSingleResource() {}

    protected void consumeSingleStaticImportDeclarationName() {
        this.consumeSingleModifierImportDeclarationName(8);
    }

    protected void consumeSingleModuleImportDeclarationName() {
        this.consumeSingleModifierImportDeclarationName(32768);
    }

    protected void consumeSingleModifierImportDeclarationName(int modifier) {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
        System.arraycopy(
                this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
        ImportReference impt;
        this.pushOnAstStack(impt = new ImportReference(tokens, positions, false, modifier));
        this.modifiers = 0;
        if (modifier == 32768) {
            impt.modifiersSourceStart = this.intStack[this.intPtr--];
        } else {
            impt.modifiersSourceStart = this.modifiersSourceStart;
        }

        this.modifiersSourceStart = -1;
        if (this.currentToken == 26) {
            impt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            impt.declarationSourceEnd = impt.sourceEnd;
        }

        impt.declarationEnd = impt.declarationSourceEnd;
        impt.declarationSourceStart = this.intStack[this.intPtr--];
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            impt.modifiers = 0;
            this.problemReporter().invalidUsageOfStaticImports(impt);
        }

        if (modifier == 32768) {
            impt.bits |= 131072;
        }

        if (this.currentElement != null) {
            this.lastCheckPoint = impt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ImportReference) impt, 0);
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeSingleTypeImportDeclarationName() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
        System.arraycopy(
                this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
        ImportReference impt;
        this.pushOnAstStack(impt = new ImportReference(tokens, positions, false, 0));
        if (this.currentToken == 26) {
            impt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            impt.declarationSourceEnd = impt.sourceEnd;
        }

        impt.declarationEnd = impt.declarationSourceEnd;
        impt.declarationSourceStart = this.intStack[this.intPtr--];
        if (this.currentElement != null) {
            this.lastCheckPoint = impt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ImportReference) impt, 0);
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeStatementBreak() {
        this.pushOnAstStack(
                new BreakStatement(
                        (char[]) null, this.intStack[this.intPtr--], this.endStatementPosition));
        if (this.pendingRecoveredType != null) {
            if (this.pendingRecoveredType.allocation == null
                    && this.endPosition <= this.pendingRecoveredType.declarationSourceEnd) {
                this.astStack[this.astPtr] = this.pendingRecoveredType;
                this.pendingRecoveredType = null;
                return;
            }

            this.pendingRecoveredType = null;
        }
    }

    protected void consumeStatementBreakWithLabel() {
        this.pushOnAstStack(
                new BreakStatement(
                        this.identifierStack[this.identifierPtr--],
                        this.intStack[this.intPtr--],
                        this.endStatementPosition));
        --this.identifierLengthPtr;
    }

    protected void consumeStatementYield() {
        if (this.expressionLengthStack[this.expressionLengthPtr--] != 0) {
            Expression expr = this.expressionStack[this.expressionPtr--];
            YieldStatement yieldStatement =
                    new YieldStatement(
                            expr, false, this.intStack[this.intPtr--], this.endStatementPosition);
            this.pushOnAstStack(yieldStatement);
        }
    }

    protected void consumeStatementCatch() {
        --this.astLengthPtr;
        this.listLength = 0;
    }

    protected void consumeStatementContinue() {
        this.pushOnAstStack(
                new ContinueStatement(
                        (char[]) null, this.intStack[this.intPtr--], this.endStatementPosition));
    }

    protected void consumeStatementContinueWithLabel() {
        this.pushOnAstStack(
                new ContinueStatement(
                        this.identifierStack[this.identifierPtr--],
                        this.intStack[this.intPtr--],
                        this.endStatementPosition));
        --this.identifierLengthPtr;
    }

    protected void consumeStatementDo() {
        --this.intPtr;
        Statement statement = (Statement) this.astStack[this.astPtr];
        --this.expressionLengthPtr;
        this.astStack[this.astPtr] =
                new DoStatement(
                        this.expressionStack[this.expressionPtr--],
                        statement,
                        this.intStack[this.intPtr--],
                        this.endStatementPosition);
    }

    protected void consumeStatementExpressionList() {
        this.concatExpressionLists();
    }

    protected void consumeStatementFor() {
        Expression cond = null;
        boolean scope = true;
        --this.astLengthPtr;
        Statement statement = (Statement) this.astStack[this.astPtr--];
        int length;
        Statement[] updates;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) == 0) {
            updates = null;
        } else {
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    updates = new Statement[length],
                    0,
                    length);
        }

        if (this.expressionLengthStack[this.expressionLengthPtr--] != 0) {
            cond = this.expressionStack[this.expressionPtr--];
        }

        Statement[] inits;
        if ((length = this.astLengthStack[this.astLengthPtr--]) == 0) {
            inits = null;
            scope = false;
        } else if (length == -1) {
            scope = false;
            length = this.expressionLengthStack[this.expressionLengthPtr--];
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    inits = new Statement[length],
                    0,
                    length);
        } else {
            this.astPtr -= length;
            System.arraycopy(
                    this.astStack, this.astPtr + 1, inits = new Statement[length], 0, length);
        }

        this.pushOnAstStack(
                new ForStatement(
                        inits,
                        cond,
                        updates,
                        statement,
                        scope,
                        this.intStack[this.intPtr--],
                        this.endStatementPosition));
    }

    protected void consumeStatementIfNoElse() {
        --this.expressionLengthPtr;
        Statement thenStatement = (Statement) this.astStack[this.astPtr];
        this.astStack[this.astPtr] =
                new IfStatement(
                        this.expressionStack[this.expressionPtr--],
                        thenStatement,
                        this.intStack[this.intPtr--],
                        this.endStatementPosition);
    }

    protected void consumeStatementIfWithElse() {
        --this.expressionLengthPtr;
        --this.astLengthPtr;
        this.astStack[--this.astPtr] =
                new IfStatement(
                        this.expressionStack[this.expressionPtr--],
                        (Statement) this.astStack[this.astPtr],
                        (Statement) this.astStack[this.astPtr + 1],
                        this.intStack[this.intPtr--],
                        this.endStatementPosition);
    }

    protected void consumeStatementLabel() {
        Statement statement = (Statement) this.astStack[this.astPtr];
        this.astStack[this.astPtr] =
                new LabeledStatement(
                        this.identifierStack[this.identifierPtr],
                        statement,
                        this.identifierPositionStack[this.identifierPtr--],
                        this.endStatementPosition);
        --this.identifierLengthPtr;
    }

    protected void consumeStatementReturn() {
        if (this.expressionLengthStack[this.expressionLengthPtr--] != 0) {
            this.pushOnAstStack(
                    new ReturnStatement(
                            this.expressionStack[this.expressionPtr--],
                            this.intStack[this.intPtr--],
                            this.endStatementPosition));
        } else {
            this.pushOnAstStack(
                    new ReturnStatement(
                            (Expression) null,
                            this.intStack[this.intPtr--],
                            this.endStatementPosition));
        }
    }

    protected void consumeSwitchStatementOrExpression(boolean isStmt) {
        --this.nestedType;
        --this.switchNestingLevel;
        SwitchStatement switchStatement = isStmt ? new SwitchStatement() : new SwitchExpression();
        --this.expressionLengthPtr;
        ((SwitchStatement) switchStatement).expression = this.expressionStack[this.expressionPtr--];
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.astPtr -= length;
            System.arraycopy(
                    this.astStack,
                    this.astPtr + 1,
                    ((SwitchStatement) switchStatement).statements = new Statement[length],
                    0,
                    length);
        }

        ((SwitchStatement) switchStatement).explicitDeclarations =
                this.realBlockStack[this.realBlockPtr--];
        ((SwitchStatement) switchStatement).blockStart = this.intStack[this.intPtr--];
        ((SwitchStatement) switchStatement).sourceStart = this.intStack[this.intPtr--];
        ((SwitchStatement) switchStatement).sourceEnd = this.endStatementPosition;
        if (length == 0
                && !this.containsComment(
                        ((SwitchStatement) switchStatement).blockStart,
                        ((SwitchStatement) switchStatement).sourceEnd)) {
            ((SwitchStatement) switchStatement).bits |= 8;
        }

        if (isStmt) {
            this.pushOnAstStack((ASTNode) switchStatement);
        } else {
            if (!this.parsingJava14Plus) {
                this.problemReporter().switchExpressionsNotSupported((ASTNode) switchStatement);
            }

            this.pushOnExpressionStack((Expression) switchStatement);
        }
    }

    protected void consumeStatementSynchronized() {
        if (this.astLengthStack[this.astLengthPtr] == 0) {
            this.astLengthStack[this.astLengthPtr] = 1;
            --this.expressionLengthPtr;
            this.astStack[++this.astPtr] =
                    new SynchronizedStatement(
                            this.expressionStack[this.expressionPtr--],
                            (Block) null,
                            this.intStack[this.intPtr--],
                            this.endStatementPosition);
        } else {
            --this.expressionLengthPtr;
            this.astStack[this.astPtr] =
                    new SynchronizedStatement(
                            this.expressionStack[this.expressionPtr--],
                            (Block) this.astStack[this.astPtr],
                            this.intStack[this.intPtr--],
                            this.endStatementPosition);
        }

        this.modifiers = 0;
        this.modifiersSourceStart = -1;
    }

    protected void consumeStatementThrow() {
        --this.expressionLengthPtr;
        this.pushOnAstStack(
                new ThrowStatement(
                        this.expressionStack[this.expressionPtr--],
                        this.intStack[this.intPtr--],
                        this.endStatementPosition));
    }

    protected void consumeStatementTry(boolean withFinally, boolean hasResources) {
        TryStatement tryStmt = new TryStatement();
        if (withFinally) {
            --this.astLengthPtr;
            tryStmt.finallyBlock = (Block) this.astStack[this.astPtr--];
        }

        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            if (length == 1) {
                tryStmt.catchBlocks = new Block[] {(Block) this.astStack[this.astPtr--]};
                tryStmt.catchArguments = new Argument[] {(Argument) this.astStack[this.astPtr--]};
            } else {
                Block[] bks = tryStmt.catchBlocks = new Block[length];

                for (Argument[] args = tryStmt.catchArguments = new Argument[length];
                        length-- > 0;
                        args[length] = (Argument) this.astStack[this.astPtr--]) {
                    bks[length] = (Block) this.astStack[this.astPtr--];
                }
            }
        }

        --this.astLengthPtr;
        tryStmt.tryBlock = (Block) this.astStack[this.astPtr--];
        if (hasResources) {
            length = this.astLengthStack[this.astLengthPtr--];
            Statement[] stmts = new Statement[length];
            System.arraycopy(this.astStack, (this.astPtr -= length) + 1, stmts, 0, length);
            tryStmt.resources = stmts;
            if (this.options.sourceLevel < 3342336L) {
                this.problemReporter().autoManagedResourcesNotBelow17(stmts);
            }

            if (this.options.sourceLevel < 3473408L) {
                Statement[] var9 = stmts;
                int var8 = stmts.length;

                for (int var7 = 0; var7 < var8; ++var7) {
                    Statement stmt = var9[var7];
                    if (stmt instanceof FieldReference || stmt instanceof NameReference) {
                        this.problemReporter()
                                .autoManagedVariableResourcesNotBelow9((Expression) stmt);
                    }
                }
            }
        }

        tryStmt.sourceEnd = this.endStatementPosition;
        tryStmt.sourceStart = this.intStack[this.intPtr--];
        this.pushOnAstStack(tryStmt);
    }

    protected void consumeStatementWhile() {
        --this.expressionLengthPtr;
        Statement statement = (Statement) this.astStack[this.astPtr];
        this.astStack[this.astPtr] =
                new WhileStatement(
                        this.expressionStack[this.expressionPtr--],
                        statement,
                        this.intStack[this.intPtr--],
                        this.endStatementPosition);
    }

    protected void consumeStaticImportOnDemandDeclarationName() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
        System.arraycopy(
                this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
        ImportReference impt;
        this.pushOnAstStack(impt = new ImportReference(tokens, positions, true, 8));
        impt.trailingStarPosition = this.intStack[this.intPtr--];
        this.modifiers = 0;
        impt.modifiersSourceStart = this.modifiersSourceStart;
        this.modifiersSourceStart = -1;
        if (this.currentToken == 26) {
            impt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            impt.declarationSourceEnd = impt.sourceEnd;
        }

        impt.declarationEnd = impt.declarationSourceEnd;
        impt.declarationSourceStart = this.intStack[this.intPtr--];
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            impt.modifiers = 0;
            this.problemReporter().invalidUsageOfStaticImports(impt);
        }

        if (this.currentElement != null) {
            this.lastCheckPoint = impt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ImportReference) impt, 0);
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeStaticInitializer() {
        Block block = (Block) this.astStack[this.astPtr];
        if (this.diet) {
            block.bits &= -9;
        }

        Initializer initializer = new Initializer(block, 8);
        this.astStack[this.astPtr] = initializer;
        initializer.sourceEnd = this.endStatementPosition;
        initializer.declarationSourceEnd =
                this.flushCommentsDefinedPriorTo(this.endStatementPosition);
        int var10002 = this.nestedMethod[this.nestedType]--;
        initializer.declarationSourceStart = this.intStack[this.intPtr--];
        initializer.bodyStart = this.intStack[this.intPtr--];
        initializer.bodyEnd = this.endPosition;
        initializer.javadoc = this.javadoc;
        this.javadoc = null;
        if (this.currentElement != null) {
            this.lastCheckPoint = initializer.declarationSourceEnd;
            this.currentElement = this.currentElement.add((FieldDeclaration) initializer, 0);
            this.lastIgnoredToken = -1;
        }
    }

    protected void consumeStaticOnly() {
        int savedModifiersSourceStart = this.modifiersSourceStart;
        this.checkComment();
        if (this.modifiersSourceStart >= savedModifiersSourceStart) {
            this.modifiersSourceStart = savedModifiersSourceStart;
        }

        this.pushOnIntStack(this.scanner.currentPosition);
        this.pushOnIntStack(
                this.modifiersSourceStart >= 0
                        ? this.modifiersSourceStart
                        : this.scanner.startPosition);
        this.jumpOverMethodBody();
        int var10002 = this.nestedMethod[this.nestedType]++;
        this.resetModifiers();
        --this.expressionLengthPtr;
        if (this.currentElement != null) {
            this.recoveredStaticInitializerStart = this.intStack[this.intPtr];
        }
    }

    private void consumeTextBlock() {
        this.problemReporter()
                .validateJavaFeatureSupport(
                        JavaFeature.TEXT_BLOCKS,
                        this.scanner.startPosition,
                        this.scanner.currentPosition - 1);
        char[] allchars = this.scanner.getCurrentTextBlock();
        TextBlock textBlock =
                this.createTextBlock(
                        allchars, this.scanner.startPosition, this.scanner.currentPosition - 1);
        this.pushOnExpressionStack(textBlock);
    }

    private TextBlock createTextBlock(char[] allchars, int start, int end) {
        TextBlock textBlock;
        if (this.recordStringLiterals
                && !this.reparsingFunctionalExpression
                && this.checkExternalizeStrings
                && this.lastPosistion < this.scanner.currentPosition
                && !this.statementRecoveryActivated) {
            textBlock =
                    TextBlock.createTextBlock(
                            allchars,
                            start,
                            end,
                            Util.getLineNumber(
                                    this.scanner.startPosition,
                                    this.scanner.lineEnds,
                                    0,
                                    this.scanner.linePtr),
                            Util.getLineNumber(
                                    this.scanner.currentPosition - 1,
                                    this.scanner.lineEnds,
                                    0,
                                    this.scanner.linePtr));
            this.compilationUnit.recordStringLiteral(textBlock, this.currentElement != null);
        } else {
            textBlock = TextBlock.createTextBlock(allchars, start, end, 0, 0);
        }

        return textBlock;
    }

    protected void consumeSwitchBlock(boolean hasContents) {
        if (hasContents) {
            this.concatNodeLists();
        } else {
            this.pushOnAstLengthStack(0);
        }
    }

    protected void consumeSwitchBlockStatement() {
        this.concatNodeLists();
    }

    protected void consumeSwitchBlockStatements() {
        this.concatNodeLists();
    }

    protected void consumeSwitchLabels(boolean shouldConcat, boolean isSwitchRule) {
        Expression[] labelExpressions = null;
        int sourceEnd;
        int sourceStart;
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) > 0) {
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    labelExpressions = new Expression[length],
                    0,
                    length);
            sourceStart = this.intStack[this.intPtr--];
            sourceEnd = labelExpressions[length - 1].sourceEnd;
        } else {
            sourceEnd = this.intStack[this.intPtr--];
            sourceStart = this.intStack[this.intPtr--];
            labelExpressions = Expression.NO_EXPRESSIONS;
        }

        CaseStatement caseStatement = new CaseStatement(labelExpressions, sourceStart, sourceEnd);
        caseStatement.isSwitchRule = isSwitchRule;
        if (labelExpressions.length > 1 && !this.parsingJava14Plus) {
            this.problemReporter().multiConstantCaseLabelsNotSupported(caseStatement);
        }

        if (isSwitchRule && !this.parsingJava14Plus) {
            this.problemReporter().arrowInCaseStatementsNotSupported(caseStatement);
        }

        if (this.hasLeadingTagComment(FALL_THROUGH_TAG, caseStatement.sourceStart)) {
            caseStatement.bits |= 536870912;
        }

        if (labelExpressions.length == 0
                && this.hasLeadingTagComment(CASES_OMITTED_TAG, caseStatement.sourceStart)) {
            caseStatement.bits |= 1073741824;
        }

        this.pushOnAstStack(caseStatement);
        if (shouldConcat) {
            this.optimizedConcatNodeLists();
        }
    }

    protected void consumeSwitchRule(Parser.SwitchRuleKind kind) {
        if (kind == Parser.SwitchRuleKind.EXPRESSION) {
            this.consumeExpressionStatement();
            Expression expr = (Expression) this.astStack[this.astPtr];
            expr.bits &= -1048577;
            YieldStatement yieldStatement =
                    new YieldStatement(expr, true, expr.sourceStart, this.endStatementPosition);
            this.astStack[this.astPtr] = yieldStatement;
        } else if (kind == Parser.SwitchRuleKind.BLOCK) {
            Block block = (Block) this.astStack[this.astPtr];
            block.bits |= 1;
        }

        this.concatNodeLists();
    }

    protected void consumeCaseLabelElement(Parser.CaseLabelKind kind) {
        Expression pattern = null;
        switch ($SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$CaseLabelKind()[
                kind.ordinal()]) {
            case 1:
                if (!((pattern = this.expressionStack[this.expressionPtr])
                        instanceof NullLiteral)) {
                    pattern = null;
                }
                break;
            case 2:
                int end = this.intStack[this.intPtr--];
                int start = this.intStack[this.intPtr--];
                this.pushOnExpressionStack(
                        (Expression) (pattern = new FakeDefaultLiteral(start, end)));
                break;
            case 3:
                --this.astLengthPtr;
                pattern = (Pattern) this.astStack[this.astPtr--];
                this.pushOnExpressionStack((Expression) pattern);
        }

        if (pattern != null) {
            this.problemReporter()
                    .validateJavaFeatureSupport(
                            JavaFeature.PATTERN_MATCHING_IN_SWITCH,
                            ((Expression) pattern).sourceStart,
                            ((Expression) pattern).sourceEnd);
        }
    }

    protected void consumeCaseLabelElements() {
        this.concatExpressionLists();
        boolean thisLabelIsPattern = this.expressionStack[this.expressionPtr] instanceof Pattern;
        boolean lastLabelIsPattern =
                this.expressionStack[this.expressionPtr - 1] instanceof Pattern;
        if (thisLabelIsPattern != lastLabelIsPattern) {
            this.problemReporter()
                    .illegalCaseConstantCombination(this.expressionStack[this.expressionPtr]);
        }

        if (thisLabelIsPattern && lastLabelIsPattern) {
            Pattern lastPattern = (Pattern) this.expressionStack[this.expressionPtr - 1];
            Pattern thisPattern = (Pattern) this.expressionStack[this.expressionPtr];
            if (lastPattern instanceof GuardedPattern) {
                GuardedPattern gp = (GuardedPattern) lastPattern;
                this.problemReporter()
                        .parseErrorMisplacedConstruct(gp.whenSourceStart, gp.sourceEnd);
            }

            Pattern[] patterns =
                    (Pattern[])
                            Stream.concat(
                                            Arrays.stream(lastPattern.getAlternatives()),
                                            Arrays.stream(thisPattern.getAlternatives()))
                                    .toArray(
                                            (var0) -> {
                                                return new Pattern[var0];
                                            });
            Pattern combinedPattern = new EitherOrMultiPattern(patterns);
            if (thisPattern instanceof GuardedPattern) {
                GuardedPattern gp = (GuardedPattern) thisPattern;
                combinedPattern = new GuardedPattern((Pattern) combinedPattern, gp.condition);
                ((GuardedPattern) combinedPattern).whenSourceStart = gp.whenSourceStart;
            }

            this.expressionStack[--this.expressionPtr] = (Expression) combinedPattern;
            int var10002 = this.expressionLengthStack[this.expressionLengthPtr]--;
        }
    }

    protected void consumeToken(int type) {
        switch (type) {
            case 2:
            case 3:
                this.endPosition = this.scanner.startPosition;
                this.endStatementPosition = this.scanner.currentPosition - 1;
            case 6:
            case 9:
            case 10:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 25:
            case 28:
            case 30:
            case 31:
            case 32:
            case 39:
            case 66:
            case 69:
            case 73:
            case 74:
            case 89:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 101:
            case 102:
            case 103:
            case 107:
            case 117:
            case 119:
            case 120:
            case 130:
            default:
                break;
            case 7:
                this.colonColonStart = this.scanner.currentPosition - 2;
                break;
            case 8:
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                break;
            case 11:
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 22:
            case 34:
                this.pushIdentifier();
                long positions;
                if (this.scanner.useAssertAsAnIndentifier
                        && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
                    positions = this.identifierPositionStack[this.identifierPtr];
                    if (!this.statementRecoveryActivated) {
                        this.problemReporter()
                                .useAssertAsAnIdentifier((int) (positions >>> 32), (int) positions);
                    }
                }

                if (this.scanner.useEnumAsAnIndentifier
                        && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
                    positions = this.identifierPositionStack[this.identifierPtr];
                    if (!this.statementRecoveryActivated) {
                        this.problemReporter()
                                .useEnumAsAnIdentifier((int) (positions >>> 32), (int) positions);
                    }
                }
                break;
            case 23:
                this.lParenPos = this.scanner.startPosition;
                break;
            case 24:
                this.rParenPos = this.scanner.currentPosition - 1;
                break;
            case 26:
            case 33:
                this.endStatementPosition = this.scanner.currentPosition - 1;
                this.endPosition = this.scanner.startPosition - 1;
                break;
            case 27:
                this.expectTypeAnnotation = true;
                this.pushOnIntStack(this.dimensions);
                this.dimensions = 0;
            case 37:
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 29:
                this.pushOnIntStack(this.scanner.startPosition);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                break;
            case 35:
            case 36:
                this.endPosition = this.scanner.currentPosition - 1;
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 38:
                this.resetModifiers();
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 40:
                if (this.isParsingModuleDeclaration()) {
                    this.checkAndSetModifiers(64);
                } else {
                    this.checkAndSetModifiers(8);
                }

                this.pushOnExpressionStackLengthStack(0);
                break;
            case 41:
                this.synchronizedBlockSourceStart = this.scanner.startPosition;
                this.checkAndSetModifiers(32);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 42:
                this.checkAndSetModifiers(268435456);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 43:
                this.checkAndSetModifiers(1024);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 44:
                this.checkAndSetModifiers(16);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 45:
                this.checkAndSetModifiers(256);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 46:
                this.checkAndSetModifiers(67108864);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 47:
                this.checkAndSetModifiers(2);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 48:
                this.checkAndSetModifiers(4);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 49:
                this.checkAndSetModifiers(1);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 50:
                this.checkAndSetModifiers(2048);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 51:
                this.checkAndSetModifiers(128);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 52:
                this.checkAndSetModifiers(64);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 53:
                this.pushOnExpressionStack(
                        new FalseLiteral(
                                this.scanner.startPosition, this.scanner.currentPosition - 1));
                break;
            case 54:
                this.pushOnExpressionStack(
                        new NullLiteral(
                                this.scanner.startPosition, this.scanner.currentPosition - 1));
                break;
            case 55:
                this.pushOnExpressionStack(
                        new TrueLiteral(
                                this.scanner.startPosition, this.scanner.currentPosition - 1));
                break;
            case 56:
                this.pushOnExpressionStack(
                        IntLiteral.buildIntLiteral(
                                this.scanner.getCurrentTokenSource(),
                                this.scanner.startPosition,
                                this.scanner.currentPosition - 1));
                break;
            case 57:
                this.pushOnExpressionStack(
                        LongLiteral.buildLongLiteral(
                                this.scanner.getCurrentTokenSource(),
                                this.scanner.startPosition,
                                this.scanner.currentPosition - 1));
                break;
            case 58:
                this.pushOnExpressionStack(
                        new FloatLiteral(
                                this.scanner.getCurrentTokenSource(),
                                this.scanner.startPosition,
                                this.scanner.currentPosition - 1));
                break;
            case 59:
                this.pushOnExpressionStack(
                        new DoubleLiteral(
                                this.scanner.getCurrentTokenSource(),
                                this.scanner.startPosition,
                                this.scanner.currentPosition - 1));
                break;
            case 60:
                this.pushOnExpressionStack(
                        new CharLiteral(
                                this.scanner.getCurrentTokenSource(),
                                this.scanner.startPosition,
                                this.scanner.currentPosition - 1));
                break;
            case 61:
                StringLiteral stringLiteral;
                if (this.recordStringLiterals
                        && !this.reparsingFunctionalExpression
                        && this.checkExternalizeStrings
                        && this.lastPosistion < this.scanner.currentPosition
                        && !this.statementRecoveryActivated) {
                    stringLiteral =
                            this.createStringLiteral(
                                    this.scanner.getCurrentTokenSourceString(),
                                    this.scanner.startPosition,
                                    this.scanner.currentPosition - 1,
                                    Util.getLineNumber(
                                            this.scanner.startPosition,
                                            this.scanner.lineEnds,
                                            0,
                                            this.scanner.linePtr));
                    this.compilationUnit.recordStringLiteral(
                            stringLiteral, this.currentElement != null);
                } else {
                    stringLiteral =
                            this.createStringLiteral(
                                    this.scanner.getCurrentTokenSourceString(),
                                    this.scanner.startPosition,
                                    this.scanner.currentPosition - 1,
                                    0);
                }

                this.pushOnExpressionStack(stringLiteral);
                break;
            case 62:
                this.consumeTextBlock();
                break;
            case 63:
                this.endStatementPosition = this.scanner.currentPosition - 1;
            case 4:
            case 5:
            case 67:
            case 68:
                this.endPosition = this.scanner.startPosition;
                break;
            case 64:
                this.flushCommentsDefinedPriorTo(this.scanner.currentPosition);
                break;
            case 65:
                this.consumeNestedType();
                ++this.switchNestingLevel;
                int var10002 = this.nestedMethod[this.nestedType]++;
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 70:
                this.rBracketPosition = this.scanner.startPosition;
                this.endPosition = this.scanner.startPosition;
                this.endStatementPosition = this.scanner.currentPosition - 1;
                break;
            case 71:
            case 76:
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 72:
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 75:
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 77:
                this.pushOnIntStack(this.scanner.startPosition);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                break;
            case 78:
                RecoveredElement var4;
                if ((var4 = this.currentElement) instanceof RecoveredAnnotation) {
                    RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation) var4;
                    if (recoveredAnnotation.memberValuPairEqualEnd == -1) {
                        recoveredAnnotation.memberValuPairEqualEnd =
                                this.scanner.currentPosition - 1;
                    }
                }
                break;
            case 85:
                this.forStartPosition = this.scanner.startPosition;
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 86:
            case 87:
            case 90:
            case 91:
            case 111:
            case 116:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 88:
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 104:
                this.pushIdentifier(-5);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 105:
                this.pushIdentifier(-3);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 106:
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 108:
                this.pushIdentifier(-2);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 109:
                this.pushIdentifier(-8);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 110:
                this.pushIdentifier(-9);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 112:
                this.pushIdentifier(-10);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 113:
                this.pushIdentifier(-7);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 114:
                this.pushIdentifier(-4);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 115:
                this.pushIdentifier(-6);
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 118:
                this.consumeLambdaHeader();
                break;
            case 121:
                this.checkAndSetModifiers(32);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 127:
                this.checkAndSetModifiers(32);
                this.pushOnExpressionStackLengthStack(0);
                break;
            case 128:
                this.pushOnIntStack(this.scanner.currentPosition - 1);
                break;
            case 129:
                this.pushOnIntStack(this.scanner.startPosition);
                break;
            case 131:
                this.pushOnIntStack(this.scanner.startPosition);
        }
    }

    protected void consumeTypeArgument() {
        this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
    }

    protected void consumeTypeArgumentList() {
        this.concatGenericsLists();
    }

    protected void consumeTypeArgumentList1() {
        this.concatGenericsLists();
    }

    protected void consumeTypeArgumentList2() {
        this.concatGenericsLists();
    }

    protected void consumeTypeArgumentList3() {
        this.concatGenericsLists();
    }

    protected void consumeTypeArgumentReferenceType1() {
        this.concatGenericsLists();
        this.pushOnGenericsStack(this.getTypeReference(0));
        --this.intPtr;
    }

    protected void consumeTypeArgumentReferenceType2() {
        this.concatGenericsLists();
        this.pushOnGenericsStack(this.getTypeReference(0));
        --this.intPtr;
    }

    protected void consumeTypeArguments() {
        this.concatGenericsLists();
        --this.intPtr;
        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            int length = this.genericsLengthStack[this.genericsLengthPtr];
            this.problemReporter()
                    .invalidUsageOfTypeArguments(
                            (TypeReference) this.genericsStack[this.genericsPtr - length + 1],
                            (TypeReference) this.genericsStack[this.genericsPtr]);
        }
    }

    protected void consumeTypeDeclarations() {
        this.concatNodeLists();
    }

    protected void consumeTypeHeaderNameWithTypeParameters() {
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        int length = this.genericsLengthStack[this.genericsLengthPtr--];
        this.genericsPtr -= length;
        System.arraycopy(
                this.genericsStack,
                this.genericsPtr + 1,
                typeDecl.typeParameters = new TypeParameter[length],
                0,
                length);
        typeDecl.bodyStart = typeDecl.typeParameters[length - 1].declarationSourceEnd + 1;
        this.listTypeParameterLength = 0;
        if (this.currentElement != null) {
            RecoveredElement var4;
            if ((var4 = this.currentElement) instanceof RecoveredType) {
                RecoveredType recoveredType = (RecoveredType) var4;
                recoveredType.pendingTypeParameters = null;
                this.lastCheckPoint = typeDecl.bodyStart;
            } else {
                this.lastCheckPoint = typeDecl.bodyStart;
                this.currentElement = this.currentElement.add((TypeDeclaration) typeDecl, 0);
                this.lastIgnoredToken = -1;
            }
        }
    }

    protected void consumeTypeImportOnDemandDeclarationName() {
        int length;
        char[][] tokens =
                new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
        this.identifierPtr -= length;
        long[] positions = new long[length];
        System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
        System.arraycopy(
                this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
        ImportReference impt;
        this.pushOnAstStack(impt = new ImportReference(tokens, positions, true, 0));
        impt.trailingStarPosition = this.intStack[this.intPtr--];
        if (this.currentToken == 26) {
            impt.declarationSourceEnd = this.scanner.currentPosition - 1;
        } else {
            impt.declarationSourceEnd = impt.sourceEnd;
        }

        impt.declarationEnd = impt.declarationSourceEnd;
        impt.declarationSourceStart = this.intStack[this.intPtr--];
        if (this.currentElement != null) {
            this.lastCheckPoint = impt.declarationSourceEnd + 1;
            this.currentElement = this.currentElement.add((ImportReference) impt, 0);
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
        }
    }

    protected void consumeTypeParameter1() {}

    protected void consumeTypeParameter1WithExtends() {
        TypeReference superType = (TypeReference) this.genericsStack[this.genericsPtr--];
        --this.genericsLengthPtr;
        TypeParameter typeParameter = (TypeParameter) this.genericsStack[this.genericsPtr];
        typeParameter.declarationSourceEnd = superType.sourceEnd;
        typeParameter.type = superType;
        typeParameter.bits |= superType.bits & 1048576;
        this.genericsStack[this.genericsPtr] = typeParameter;
    }

    protected void consumeTypeParameter1WithExtendsAndBounds() {
        int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        TypeReference[] bounds = new TypeReference[additionalBoundsLength];
        this.genericsPtr -= additionalBoundsLength;
        System.arraycopy(
                this.genericsStack, this.genericsPtr + 1, bounds, 0, additionalBoundsLength);
        TypeReference superType = this.getTypeReference(this.intStack[this.intPtr--]);
        TypeParameter typeParameter = (TypeParameter) this.genericsStack[this.genericsPtr];
        typeParameter.declarationSourceEnd = bounds[additionalBoundsLength - 1].sourceEnd;
        typeParameter.type = superType;
        typeParameter.bits |= superType.bits & 1048576;
        typeParameter.bounds = bounds;
        TypeReference[] var8 = bounds;
        int var7 = bounds.length;

        for (int var6 = 0; var6 < var7; ++var6) {
            TypeReference bound2 = var8[var6];
            typeParameter.bits |= bound2.bits & 1048576;
        }
    }

    protected void consumeTypeParameterHeader() {
        TypeParameter typeParameter = new TypeParameter();
        int length;
        if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
            System.arraycopy(
                    this.typeAnnotationStack,
                    (this.typeAnnotationPtr -= length) + 1,
                    typeParameter.annotations = new Annotation[length],
                    0,
                    length);
            typeParameter.bits |= 1048576;
        }

        long pos = this.identifierPositionStack[this.identifierPtr];
        int end = (int) pos;
        typeParameter.declarationSourceEnd = end;
        typeParameter.sourceEnd = end;
        int start = (int) (pos >>> 32);
        typeParameter.declarationSourceStart = start;
        typeParameter.sourceStart = start;
        typeParameter.name = this.identifierStack[this.identifierPtr--];
        --this.identifierLengthPtr;
        this.pushOnGenericsStack(typeParameter);
        ++this.listTypeParameterLength;
    }

    protected void consumeTypeParameterList() {
        this.concatGenericsLists();
    }

    protected void consumeTypeParameterList1() {
        this.concatGenericsLists();
    }

    protected void consumeTypeParameters() {
        int startPos = this.intStack[this.intPtr--];
        RecoveredElement var3;
        int length;
        if ((var3 = this.currentElement) instanceof RecoveredType) {
            RecoveredType recoveredType = (RecoveredType) var3;
            length = this.genericsLengthStack[this.genericsLengthPtr];
            TypeParameter[] typeParameters = new TypeParameter[length];
            System.arraycopy(
                    this.genericsStack, this.genericsPtr - length + 1, typeParameters, 0, length);
            recoveredType.add(typeParameters, startPos);
        }

        if (!this.statementRecoveryActivated
                && this.options.sourceLevel < 3211264L
                && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
            length = this.genericsLengthStack[this.genericsLengthPtr];
            this.problemReporter()
                    .invalidUsageOfTypeParameters(
                            (TypeParameter) this.genericsStack[this.genericsPtr - length + 1],
                            (TypeParameter) this.genericsStack[this.genericsPtr]);
        }
    }

    protected void consumeTypeParameterWithExtends() {
        TypeReference superType = this.getTypeReference(this.intStack[this.intPtr--]);
        TypeParameter typeParameter = (TypeParameter) this.genericsStack[this.genericsPtr];
        typeParameter.declarationSourceEnd = superType.sourceEnd;
        typeParameter.type = superType;
        typeParameter.bits |= superType.bits & 1048576;
    }

    protected void consumeTypeParameterWithExtendsAndBounds() {
        int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
        TypeReference[] bounds = new TypeReference[additionalBoundsLength];
        this.genericsPtr -= additionalBoundsLength;
        System.arraycopy(
                this.genericsStack, this.genericsPtr + 1, bounds, 0, additionalBoundsLength);
        TypeReference superType = this.getTypeReference(this.intStack[this.intPtr--]);
        TypeParameter typeParameter = (TypeParameter) this.genericsStack[this.genericsPtr];
        typeParameter.type = superType;
        typeParameter.bits |= superType.bits & 1048576;
        typeParameter.bounds = bounds;
        typeParameter.declarationSourceEnd = bounds[additionalBoundsLength - 1].sourceEnd;
        TypeReference[] var8 = bounds;
        int var7 = bounds.length;

        for (int var6 = 0; var6 < var7; ++var6) {
            TypeReference bound2 = var8[var6];
            typeParameter.bits |= bound2.bits & 1048576;
        }
    }

    protected void consumeGuard() {
        --this.astLengthPtr;
        Pattern pattern = (Pattern) this.astStack[this.astPtr--];
        Expression expr = this.expressionStack[this.expressionPtr--];
        --this.expressionLengthPtr;
        GuardedPattern gPattern = new GuardedPattern(pattern, expr);
        gPattern.whenSourceStart = this.intStack[this.intPtr--];
        this.pushOnAstStack(gPattern);
    }

    protected void consumeTypePattern() {
        char[] identifierName = this.identifierStack[this.identifierPtr];
        long namePosition = this.identifierPositionStack[this.identifierPtr];
        LocalDeclaration local =
                this.createLocalDeclaration(
                        identifierName, (int) (namePosition >>> 32), (int) namePosition);
        local.declarationSourceEnd = local.declarationEnd;
        --this.identifierPtr;
        --this.identifierLengthPtr;
        this.consumeTypeReferenceWithModifiersAndAnnotations();
        TypeReference type = (TypeReference) this.expressionStack[this.expressionPtr--];
        --this.expressionLengthPtr;
        local.annotations =
                type.annotations != null && type.annotations.length > 0
                        ? type.annotations[0]
                        : null;
        type.annotations = null;
        local.type = type;
        TypePattern aTypePattern = TypePattern.createTypePattern(local);
        aTypePattern.sourceStart = this.intStack[this.intPtr--];
        local.modifiers = this.intStack[this.intPtr--];
        local.declarationSourceStart = type.sourceStart;
        aTypePattern.sourceEnd = local.sourceEnd;
        this.problemReporter()
                .validateJavaFeatureSupport(
                        JavaFeature.PATTERN_MATCHING_IN_INSTANCEOF,
                        type.sourceStart,
                        local.declarationEnd);
        this.pushOnAstStack(aTypePattern);
    }

    protected void consumeUnnamedPattern() {
        char[] identifierName = this.identifierStack[this.identifierPtr];
        long namePosition = this.identifierPositionStack[this.identifierPtr];
        LocalDeclaration local =
                this.createLocalDeclaration(
                        identifierName, (int) (namePosition >>> 32), (int) namePosition);
        local.declarationSourceEnd = local.declarationEnd;
        local.declarationSourceStart = (int) (namePosition >>> 32);
        --this.identifierPtr;
        --this.identifierLengthPtr;
        TypePattern aUnnamedPattern = TypePattern.createTypePattern(local);
        aUnnamedPattern.sourceStart = local.sourceStart;
        aUnnamedPattern.sourceEnd = local.sourceEnd;
        this.pushOnAstStack(aUnnamedPattern);
    }

    protected void consumeRecordPattern() {
        Annotation[] typeAnnotations = null;
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(
                    this.expressionStack,
                    (this.expressionPtr -= length) + 1,
                    typeAnnotations = new Annotation[length],
                    0,
                    length);
        }

        int sourceEnd = this.intStack[this.intPtr--];
        --this.intPtr;
        int dimension = this.intStack[this.intPtr--];
        int modifierStart = this.intStack[this.intPtr--];
        int modifier = this.intStack[this.intPtr--];
        TypeReference type = this.getTypeReference(0);
        if (typeAnnotations != null) {
            int levels = type.getAnnotatableLevels();
            if (type.annotations == null) {
                type.annotations = new Annotation[levels][];
            }

            type.annotations[0] = typeAnnotations;
            type.sourceStart = type.annotations[0][0].sourceStart;
            type.bits |= 1048576;
        }

        RecordPattern recPattern = new RecordPattern(type, type.sourceStart, sourceEnd);
        length = this.astLengthPtr == -1 ? 0 : this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        if (length != 0) {
            Pattern[] patterns = new Pattern[length];
            System.arraycopy(this.astStack, this.astPtr + 1, patterns, 0, length);
            recPattern.patterns = patterns;

            Pattern pattern;
            for (int i = 0; i < length; pattern.index = i++) {
                pattern = patterns[i];
                pattern.setEnclosingPattern(recPattern);
            }
        } else {
            recPattern.patterns = ASTNode.NO_TYPE_PATTERNS;
        }

        if (dimension != 0) {
            this.problemReporter().dimensionsIllegalOnRecordPattern(type.sourceStart, sourceEnd);
        }

        if (modifier != 0) {
            this.problemReporter().illegalModifiers(modifierStart, type.sourceStart - 2);
        }

        this.checkForDiamond(recPattern.type);
        this.problemReporter()
                .validateJavaFeatureSupport(
                        JavaFeature.RECORD_PATTERNS, type.sourceStart, sourceEnd);
        this.pushOnAstStack(recPattern);
    }

    protected void consumePatternList() {
        this.optimizedConcatNodeLists();
    }

    protected void consumePatternListopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void consumeZeroAdditionalBounds() {
        if (this.currentToken == 24) {
            this.pushOnGenericsLengthStack(0);
        }
    }

    protected void consumeUnaryExpression(int op) {
        Expression exp = this.expressionStack[this.expressionPtr];
        Object r;
        if (op == 13) {
            if (exp instanceof IntLiteral) {
                IntLiteral intLiteral = (IntLiteral) exp;
                IntLiteral convertToMinValue = intLiteral.convertToMinValue();
                if (convertToMinValue == intLiteral) {
                    r = new UnaryExpression(exp, op);
                } else {
                    r = convertToMinValue;
                }
            } else if (exp instanceof LongLiteral) {
                LongLiteral longLiteral = (LongLiteral) exp;
                LongLiteral convertToMinValue = longLiteral.convertToMinValue();
                if (convertToMinValue == longLiteral) {
                    r = new UnaryExpression(exp, op);
                } else {
                    r = convertToMinValue;
                }
            } else {
                r = new UnaryExpression(exp, op);
            }
        } else {
            r = new UnaryExpression(exp, op);
        }

        ((Expression) r).sourceStart = this.intStack[this.intPtr--];
        ((Expression) r).sourceEnd = exp.sourceEnd;
        this.expressionStack[this.expressionPtr] = (Expression) r;
    }

    protected void consumeUnaryExpression(int op, boolean post) {
        Expression leftHandSide = this.expressionStack[this.expressionPtr];
        if (leftHandSide instanceof Reference) {
            if (post) {
                this.expressionStack[this.expressionPtr] =
                        new PostfixExpression(
                                leftHandSide, IntLiteral.One, op, this.endStatementPosition);
            } else {
                this.expressionStack[this.expressionPtr] =
                        new PrefixExpression(
                                leftHandSide, IntLiteral.One, op, this.intStack[this.intPtr--]);
            }
        } else {
            if (!post) {
                --this.intPtr;
            }

            if (!this.statementRecoveryActivated) {
                this.problemReporter().invalidUnaryExpression(leftHandSide);
            }
        }
    }

    protected void consumeVariableDeclarators() {
        this.optimizedConcatNodeLists();
    }

    protected void consumeVariableInitializers() {
        this.concatExpressionLists();
    }

    protected void consumeWildcard() {
        Wildcard wildcard = new Wildcard(0);
        wildcard.sourceEnd = this.intStack[this.intPtr--];
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.pushOnGenericsStack(wildcard);
    }

    protected void consumeWildcard1() {
        Wildcard wildcard = new Wildcard(0);
        wildcard.sourceEnd = this.intStack[this.intPtr--];
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.pushOnGenericsStack(wildcard);
    }

    protected void consumeWildcard1WithBounds() {}

    protected void consumeWildcard2() {
        Wildcard wildcard = new Wildcard(0);
        wildcard.sourceEnd = this.intStack[this.intPtr--];
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.pushOnGenericsStack(wildcard);
    }

    protected void consumeWildcard2WithBounds() {}

    protected void consumeWildcard3() {
        Wildcard wildcard = new Wildcard(0);
        wildcard.sourceEnd = this.intStack[this.intPtr--];
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.pushOnGenericsStack(wildcard);
    }

    protected void consumeWildcard3WithBounds() {}

    protected void consumeWildcardBounds1Extends() {
        Wildcard wildcard = new Wildcard(1);
        wildcard.bound = (TypeReference) this.genericsStack[this.genericsPtr];
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.genericsStack[this.genericsPtr] = wildcard;
    }

    protected void consumeWildcardBounds1Super() {
        Wildcard wildcard = new Wildcard(2);
        wildcard.bound = (TypeReference) this.genericsStack[this.genericsPtr];
        --this.intPtr;
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.genericsStack[this.genericsPtr] = wildcard;
    }

    protected void consumeWildcardBounds2Extends() {
        Wildcard wildcard = new Wildcard(1);
        wildcard.bound = (TypeReference) this.genericsStack[this.genericsPtr];
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.genericsStack[this.genericsPtr] = wildcard;
    }

    protected void consumeWildcardBounds2Super() {
        Wildcard wildcard = new Wildcard(2);
        wildcard.bound = (TypeReference) this.genericsStack[this.genericsPtr];
        --this.intPtr;
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.genericsStack[this.genericsPtr] = wildcard;
    }

    protected void consumeWildcardBounds3Extends() {
        Wildcard wildcard = new Wildcard(1);
        wildcard.bound = (TypeReference) this.genericsStack[this.genericsPtr];
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.genericsStack[this.genericsPtr] = wildcard;
    }

    protected void consumeWildcardBounds3Super() {
        Wildcard wildcard = new Wildcard(2);
        wildcard.bound = (TypeReference) this.genericsStack[this.genericsPtr];
        --this.intPtr;
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.genericsStack[this.genericsPtr] = wildcard;
    }

    protected void consumeWildcardBoundsExtends() {
        Wildcard wildcard = new Wildcard(1);
        wildcard.bound = this.getTypeReference(this.intStack[this.intPtr--]);
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.pushOnGenericsStack(wildcard);
    }

    protected void consumeWildcardBoundsSuper() {
        Wildcard wildcard = new Wildcard(2);
        wildcard.bound = this.getTypeReference(this.intStack[this.intPtr--]);
        --this.intPtr;
        wildcard.sourceEnd = wildcard.bound.sourceEnd;
        --this.intPtr;
        wildcard.sourceStart = this.intStack[this.intPtr--];
        this.annotateTypeReference(wildcard);
        this.pushOnGenericsStack(wildcard);
    }

    protected void consumeWildcardWithBounds() {}

    protected void consumeRecordDeclaration() {
        int length;
        if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.dispatchDeclarationIntoRecordDeclaration(length);
        }

        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        this.recordNestedMethodLevels.remove(typeDecl);
        this.problemReporter()
                .validateJavaFeatureSupport(
                        JavaFeature.RECORDS, typeDecl.sourceStart, typeDecl.sourceEnd);
        typeDecl.createDefaultConstructor(!this.diet || this.dietInt != 0, true);
        ConstructorDeclaration cd = typeDecl.getConstructor(this);
        if (cd instanceof CompactConstructorDeclaration
                || (typeDecl.recordComponents == null || typeDecl.recordComponents.length == 0)
                        && (cd.arguments == null || cd.arguments.length == 0)) {
            cd.bits |= 512;
        }

        if (this.scanner.containsAssertKeyword) {
            typeDecl.bits |= 1;
        }

        typeDecl.addClinit();
        typeDecl.bodyEnd = this.endStatementPosition;
        if (length == 0 && !this.containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
            typeDecl.bits |= 8;
        }

        char[][] sources = TypeConstants.JAVA_LANG_RECORD;
        long[] poss = new long[sources.length];
        Arrays.fill(poss, 0L);
        TypeReference superClass = new QualifiedTypeReference(sources, poss);
        typeDecl.superclass = superClass;
        typeDecl.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
    }

    protected void consumeRecordHeaderPart() {
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];

        assert typeDecl.isRecord();
    }

    protected void consumeRecordHeaderNameWithTypeParameters() {
        this.consumeTypeHeaderNameWithTypeParameters();
        this.parsingRecordComponents = true;
    }

    protected void consumeRecordHeaderName() {
        this.parsingRecordComponents = true;
    }

    protected void consumeRecordHeaderName1() {
        this.consumeClassOrRecordHeaderName1(true);
    }

    protected void consumeRecordComponentHeaderRightParen() {
        this.parsingRecordComponents = false;
        int length = this.astLengthStack[this.astLengthPtr--];
        this.astPtr -= length;
        TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
        int nestedMethodLevel = this.nestedMethod[this.nestedType];
        this.recordNestedMethodLevels.put(
                typeDecl, new Integer[] {this.nestedType, nestedMethodLevel});
        this.astStack[this.astPtr] = typeDecl;
        if (length != 0) {
            RecordComponent[] recComps = new RecordComponent[length];
            System.arraycopy(this.astStack, this.astPtr + 1, recComps, 0, length);
            typeDecl.recordComponents = recComps;
            this.convertToFields(typeDecl, recComps);
        } else {
            typeDecl.recordComponents = ASTNode.NO_RECORD_COMPONENTS;
        }

        typeDecl.bodyStart = this.rParenPos + 1;
        this.listLength = 0;
        if (this.currentElement != null) {
            this.lastCheckPoint = typeDecl.bodyStart;
            if (this.currentElement.parseTree() == typeDecl) {
                return;
            }
        }

        this.resetModifiers();
    }

    private void convertToFields(TypeDeclaration typeDecl, RecordComponent[] recComps) {
        int length = recComps.length;
        FieldDeclaration[] fields = new FieldDeclaration[length];
        int nFields = 0;
        Set<String> argsSet = new HashSet();
        int i = 0;

        for (int max = recComps.length; i < max; ++i) {
            RecordComponent recComp = recComps[i];
            String argName = new String(recComp.name);
            if (TypeDeclaration.disallowedComponentNames.contains(argName)) {
                this.problemReporter().recordIllegalComponentNameInRecord(recComp, typeDecl);
            } else if (!argsSet.contains(argName)) {
                if (recComp.type.getLastToken() == TypeConstants.VOID) {
                    this.problemReporter().recordComponentCannotBeVoid(recComp);
                } else {
                    if (recComp.isVarArgs() && i < max - 1) {
                        this.problemReporter().recordIllegalVararg(recComp, typeDecl);
                    }

                    argsSet.add(argName);
                    FieldDeclaration f =
                            fields[nFields++] =
                                    this.createFieldDeclaration(
                                            recComp.name, recComp.sourceStart, recComp.sourceEnd);
                    f.bits = recComp.bits;
                    f.declarationSourceStart = recComp.declarationSourceStart;
                    f.declarationEnd = recComp.declarationEnd;
                    f.declarationSourceEnd = recComp.declarationSourceEnd;
                    f.endPart1Position = recComp.sourceEnd;
                    f.endPart2Position = recComp.declarationSourceEnd;
                    f.modifiers = 18;
                    f.isARecordComponent = true;
                    f.modifiers |= 18;
                    f.modifiers |= 16777216;
                    f.modifiersSourceStart = recComp.modifiersSourceStart;
                    f.sourceStart = recComp.sourceStart;
                    f.sourceEnd = recComp.sourceEnd;
                    f.type = recComp.type;
                    if ((recComp.bits & 1048576) != 0) {
                        f.bits |= 1048576;
                    }
                }
            }
        }

        if (nFields < fields.length) {
            FieldDeclaration[] tmp = new FieldDeclaration[nFields];
            System.arraycopy(fields, 0, tmp, 0, nFields);
            fields = tmp;
        }

        typeDecl.fields = fields;
        typeDecl.nRecordComponents = fields.length;
    }

    protected void consumeRecordHeader() {}

    protected void consumeRecordComponentsopt() {
        this.pushOnAstLengthStack(0);
    }

    protected void dispatchDeclarationIntoRecordDeclaration(int length) {
        if (length != 0) {
            int[] flag = new int[length + 1];
            int nFields = 0;
            int size2 = 0;
            int size3 = 0;
            boolean hasAbstractMethods = false;

            for (int i = length - 1; i >= 0; --i) {
                ASTNode astNode = this.astStack[this.astPtr--];
                if (astNode instanceof AbstractMethodDeclaration) {
                    AbstractMethodDeclaration methodDeclaration =
                            (AbstractMethodDeclaration) astNode;
                    flag[i] = 2;
                    ++size2;
                    if (methodDeclaration.isAbstract()) {
                        hasAbstractMethods = true;
                    }
                } else if (astNode instanceof TypeDeclaration) {
                    flag[i] = 3;
                    ++size3;
                } else {
                    flag[i] = 1;
                    ++nFields;
                }
            }

            TypeDeclaration recordDecl = (TypeDeclaration) this.astStack[this.astPtr];
            int nCreatedFields = recordDecl.fields != null ? recordDecl.fields.length : 0;
            if (nFields != 0) {
                FieldDeclaration[] tmp =
                        new FieldDeclaration
                                [(recordDecl.fields != null ? recordDecl.fields.length : 0)
                                        + nFields];
                if (recordDecl.fields != null) {
                    System.arraycopy(recordDecl.fields, 0, tmp, 0, recordDecl.fields.length);
                }

                recordDecl.fields = tmp;
            }

            if (size2 != 0) {
                recordDecl.methods = new AbstractMethodDeclaration[size2];
                if (hasAbstractMethods) {
                    recordDecl.bits |= 2048;
                }
            }

            if (size3 != 0) {
                recordDecl.memberTypes = new TypeDeclaration[size3];
            }

            nFields = nCreatedFields;
            size3 = 0;
            size2 = 0;
            int flagI = flag[0];
            int start = 0;

            int end;
            for (end = 0; end <= length; ++end) {
                if (flagI != flag[end]) {
                    int length2;
                    switch (flagI) {
                        case 1:
                            nFields += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    recordDecl.fields,
                                    nFields - length2,
                                    length2);
                            break;
                        case 2:
                            size2 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    recordDecl.methods,
                                    size2 - length2,
                                    length2);
                            break;
                        case 3:
                            size3 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    recordDecl.memberTypes,
                                    size3 - length2,
                                    length2);
                    }

                    start = end;
                    flagI = flag[end];
                }
            }

            this.checkForRecordMemberErrors(recordDecl, nCreatedFields);
            if (recordDecl.memberTypes != null) {
                for (end = recordDecl.memberTypes.length - 1; end >= 0; --end) {
                    recordDecl.memberTypes[end].enclosingType = recordDecl;
                }
            }
        }
    }

    private void checkForRecordMemberErrors(TypeDeclaration typeDecl, int nCreatedFields) {
        if (typeDecl.fields != null) {
            for (int i = nCreatedFields; i < typeDecl.fields.length; ++i) {
                FieldDeclaration f = typeDecl.fields[i];
                if (f != null && !f.isStatic()) {
                    if (f instanceof Initializer) {
                        Initializer initializer = (Initializer) f;
                        this.problemReporter().recordInstanceInitializerBlockInRecord(initializer);
                    } else {
                        this.problemReporter().recordNonStaticFieldDeclarationInRecord(f);
                    }
                }
            }

            if (typeDecl.methods != null) {
                AbstractMethodDeclaration[] var6;
                int var9 = (var6 = typeDecl.methods).length;

                for (int var8 = 0; var8 < var9; ++var8) {
                    AbstractMethodDeclaration method = var6[var8];
                    if ((method.modifiers & 256) != 0) {
                        this.problemReporter().recordIllegalNativeModifierInRecord(method);
                    }
                }
            }
        }
    }

    public boolean containsComment(int sourceStart, int sourceEnd) {
        for (int iComment = this.scanner.commentPtr; iComment >= 0; --iComment) {
            int commentStart = this.scanner.commentStarts[iComment];
            if (commentStart < 0) {
                commentStart = -commentStart;
            }

            if (commentStart >= sourceStart && commentStart <= sourceEnd) {
                return true;
            }
        }

        return false;
    }

    public MethodDeclaration convertToMethodDeclaration(
            ConstructorDeclaration c, CompilationResult compilationResult) {
        MethodDeclaration m = new MethodDeclaration(compilationResult);
        m.typeParameters = c.typeParameters;
        m.sourceStart = c.sourceStart;
        m.sourceEnd = c.sourceEnd;
        m.bodyStart = c.bodyStart;
        m.bodyEnd = c.bodyEnd;
        m.declarationSourceEnd = c.declarationSourceEnd;
        m.declarationSourceStart = c.declarationSourceStart;
        m.selector = c.selector;
        m.statements = c.statements;
        m.modifiers = c.modifiers;
        m.annotations = c.annotations;
        m.arguments = c.arguments;
        m.thrownExceptions = c.thrownExceptions;
        m.explicitDeclarations = c.explicitDeclarations;
        m.returnType = null;
        m.javadoc = c.javadoc;
        m.bits = c.bits;
        return m;
    }

    protected TypeReference augmentTypeWithAdditionalDimensions(
            TypeReference typeReference,
            int additionalDimensions,
            Annotation[][] additionalAnnotations,
            boolean isVarargs) {
        return typeReference.augmentTypeWithAdditionalDimensions(
                additionalDimensions, additionalAnnotations, isVarargs);
    }

    protected FieldDeclaration createFieldDeclaration(
            char[] fieldDeclarationName, int sourceStart, int sourceEnd) {
        return new FieldDeclaration(fieldDeclarationName, sourceStart, sourceEnd);
    }

    protected RecordComponent createComponent(
            char[] identifierName,
            long namePositions,
            TypeReference type,
            int modifier,
            int declStart) {
        return new RecordComponent(identifierName, namePositions, type, modifier);
    }

    protected JavadocParser createJavadocParser() {
        return new JavadocParser(this);
    }

    protected LocalDeclaration createLocalDeclaration(
            char[] localDeclarationName, int sourceStart, int sourceEnd) {
        return new LocalDeclaration(localDeclarationName, sourceStart, sourceEnd);
    }

    protected StringLiteral createStringLiteral(char[] token, int start, int end, int lineNumber) {
        return new StringLiteral(token, start, end, lineNumber);
    }

    protected RecoveredType currentRecoveryType() {
        if (this.currentElement != null) {
            RecoveredElement var2;
            if ((var2 = this.currentElement) instanceof RecoveredType) {
                RecoveredType recoveredType = (RecoveredType) var2;
                return recoveredType;
            } else {
                return this.currentElement.enclosingType();
            }
        } else {
            return null;
        }
    }

    public CompilationUnitDeclaration dietParse(
            ICompilationUnit sourceUnit, CompilationResult compilationResult) {
        boolean old = this.diet;
        int oldInt = this.dietInt;

        CompilationUnitDeclaration parsedUnit;
        try {
            this.dietInt = 0;
            this.diet = true;
            parsedUnit = this.parse(sourceUnit, compilationResult);
        } finally {
            this.diet = old;
            this.dietInt = oldInt;
        }

        return parsedUnit;
    }

    protected void dispatchDeclarationInto(int length) {
        if (length != 0) {
            int[] flag = new int[length + 1];
            int size1 = 0;
            int size2 = 0;
            int size3 = 0;
            boolean hasAbstractMethods = false;

            for (int i = length - 1; i >= 0; --i) {
                ASTNode astNode = this.astStack[this.astPtr--];
                if (astNode instanceof AbstractMethodDeclaration) {
                    AbstractMethodDeclaration method = (AbstractMethodDeclaration) astNode;
                    flag[i] = 2;
                    ++size2;
                    if (method.isAbstract()) {
                        hasAbstractMethods = true;
                    }
                } else if (astNode instanceof TypeDeclaration) {
                    flag[i] = 3;
                    ++size3;
                } else {
                    flag[i] = 1;
                    ++size1;
                }
            }

            TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
            if (size1 != 0) {
                typeDecl.fields = new FieldDeclaration[size1];
            }

            if (size2 != 0) {
                typeDecl.methods = new AbstractMethodDeclaration[size2];
                if (hasAbstractMethods) {
                    typeDecl.bits |= 2048;
                }
            }

            if (size3 != 0) {
                typeDecl.memberTypes = new TypeDeclaration[size3];
            }

            size3 = 0;
            size2 = 0;
            size1 = 0;
            int flagI = flag[0];
            int start = 0;

            int end;
            for (end = 0; end <= length; ++end) {
                if (flagI != flag[end]) {
                    int length2;
                    switch (flagI) {
                        case 1:
                            size1 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    typeDecl.fields,
                                    size1 - length2,
                                    length2);
                            break;
                        case 2:
                            size2 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    typeDecl.methods,
                                    size2 - length2,
                                    length2);
                            break;
                        case 3:
                            size3 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    typeDecl.memberTypes,
                                    size3 - length2,
                                    length2);
                    }

                    start = end;
                    flagI = flag[end];
                }
            }

            if (typeDecl.memberTypes != null) {
                for (end = typeDecl.memberTypes.length - 1; end >= 0; --end) {
                    TypeDeclaration memberType = typeDecl.memberTypes[end];
                    memberType.enclosingType = typeDecl;
                }
            }
        }
    }

    protected void dispatchDeclarationIntoEnumDeclaration(int length) {
        if (length != 0) {
            int[] flag = new int[length + 1];
            int size1 = 0;
            int size2 = 0;
            int size3 = 0;
            TypeDeclaration enumDeclaration = (TypeDeclaration) this.astStack[this.astPtr - length];
            boolean hasAbstractMethods = false;
            int enumConstantsCounter = 0;

            int flagI;
            for (flagI = length - 1; flagI >= 0; --flagI) {
                ASTNode astNode = this.astStack[this.astPtr--];
                if (astNode instanceof AbstractMethodDeclaration) {
                    AbstractMethodDeclaration method = (AbstractMethodDeclaration) astNode;
                    flag[flagI] = 2;
                    ++size2;
                    if (method.isAbstract()) {
                        hasAbstractMethods = true;
                    }
                } else if (astNode instanceof TypeDeclaration) {
                    flag[flagI] = 3;
                    ++size3;
                } else if (astNode instanceof FieldDeclaration) {
                    FieldDeclaration field = (FieldDeclaration) astNode;
                    flag[flagI] = 1;
                    ++size1;
                    if (field.getKind() == 3) {
                        ++enumConstantsCounter;
                    }
                }
            }

            if (size1 != 0) {
                enumDeclaration.fields = new FieldDeclaration[size1];
            }

            if (size2 != 0) {
                enumDeclaration.methods = new AbstractMethodDeclaration[size2];
                if (hasAbstractMethods) {
                    enumDeclaration.bits |= 2048;
                }
            }

            if (size3 != 0) {
                enumDeclaration.memberTypes = new TypeDeclaration[size3];
            }

            size3 = 0;
            size2 = 0;
            size1 = 0;
            flagI = flag[0];
            int start = 0;

            int end;
            for (end = 0; end <= length; ++end) {
                if (flagI != flag[end]) {
                    int length2;
                    switch (flagI) {
                        case 1:
                            size1 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    enumDeclaration.fields,
                                    size1 - length2,
                                    length2);
                            break;
                        case 2:
                            size2 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    enumDeclaration.methods,
                                    size2 - length2,
                                    length2);
                            break;
                        case 3:
                            size3 += length2 = end - start;
                            System.arraycopy(
                                    this.astStack,
                                    this.astPtr + start + 1,
                                    enumDeclaration.memberTypes,
                                    size3 - length2,
                                    length2);
                    }

                    start = end;
                    flagI = flag[end];
                }
            }

            if (enumDeclaration.memberTypes != null) {
                for (end = enumDeclaration.memberTypes.length - 1; end >= 0; --end) {
                    enumDeclaration.memberTypes[end].enclosingType = enumDeclaration;
                }
            }

            enumDeclaration.enumConstantsCounter = enumConstantsCounter;
        }
    }

    protected CompilationUnitDeclaration endParse(int act) {
        this.lastAct = act;
        if (this.statementRecoveryActivated) {
            RecoveredElement recoveredElement = this.buildInitialRecoveryState();
            if (recoveredElement != null) {
                recoveredElement.topElement().updateParseTree();
            }

            if (this.hasError) {
                this.resetStacks();
            }
        } else if (this.currentElement != null) {
            if (VERBOSE_RECOVERY) {
                System.out.print(Messages.parser_syntaxRecovery);
                System.out.println("--------------------------");
                System.out.println(this.compilationUnit);
                System.out.println("----------------------------------");
            }

            this.currentElement.topElement().updateParseTree();
        } else if (this.diet && VERBOSE_RECOVERY) {
            System.out.print(Messages.parser_regularParse);
            System.out.println("--------------------------");
            System.out.println(this.compilationUnit);
            System.out.println("----------------------------------");
        }

        this.persistLineSeparatorPositions();

        for (int i = 0; i < this.scanner.foundTaskCount; ++i) {
            if (!this.statementRecoveryActivated) {
                this.problemReporter()
                        .task(
                                new String(this.scanner.foundTaskTags[i]),
                                new String(this.scanner.foundTaskMessages[i]),
                                this.scanner.foundTaskPriorities[i] == null
                                        ? null
                                        : new String(this.scanner.foundTaskPriorities[i]),
                                this.scanner.foundTaskPositions[i][0],
                                this.scanner.foundTaskPositions[i][1]);
            }
        }

        this.javadoc = null;
        return this.compilationUnit;
    }

    public int flushCommentsDefinedPriorTo(int position) {
        int lastCommentIndex = this.scanner.commentPtr;
        if (lastCommentIndex < 0) {
            return position;
        } else {
            int index = lastCommentIndex;

            int validCount;
            int immediateCommentEnd;
            for (validCount = 0; index >= 0; ++validCount) {
                immediateCommentEnd = this.scanner.commentStops[index];
                if (immediateCommentEnd < 0) {
                    immediateCommentEnd = -immediateCommentEnd;
                }

                if (immediateCommentEnd <= position) {
                    break;
                }

                --index;
            }

            if (validCount > 0) {
                immediateCommentEnd = -this.scanner.commentStops[index + 1];
                if (immediateCommentEnd > 0) {
                    --immediateCommentEnd;
                    if (Util.getLineNumber(position, this.scanner.lineEnds, 0, this.scanner.linePtr)
                            == Util.getLineNumber(
                                    immediateCommentEnd,
                                    this.scanner.lineEnds,
                                    0,
                                    this.scanner.linePtr)) {
                        position = immediateCommentEnd;
                        --validCount;
                        ++index;
                    }
                }
            }

            if (index < 0) {
                return position;
            } else {
                switch (validCount) {
                    case 0:
                        break;
                    case 1:
                        this.scanner.copyCommentInfo(0, index + 1);
                        break;
                    case 2:
                        this.scanner.copyCommentInfo(0, index + 1);
                        this.scanner.copyCommentInfo(1, index + 2);
                        break;
                    default:
                        this.scanner.copyAllCommentInfo(index + 1, 0, validCount);
                }

                this.scanner.commentPtr = validCount - 1;
                return position;
            }
        }
    }

    protected TypeReference getAnnotationType() {
        int length = this.identifierLengthStack[this.identifierLengthPtr--];
        if (length == 1) {
            return new SingleTypeReference(
                    this.identifierStack[this.identifierPtr],
                    this.identifierPositionStack[this.identifierPtr--]);
        } else {
            char[][] tokens = new char[length][];
            this.identifierPtr -= length;
            long[] positions = new long[length];
            System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
            System.arraycopy(
                    this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
            return new QualifiedTypeReference(tokens, positions);
        }
    }

    public int getFirstToken() {
        return this.firstToken;
    }

    public int[] getJavaDocPositions() {
        int javadocCount = 0;
        int max = this.scanner.commentPtr;

        for (int i = 0; i <= max; ++i) {
            if (this.scanner.commentStarts[i] >= 0 && this.scanner.commentStops[i] > 0) {
                ++javadocCount;
            }
        }

        if (javadocCount == 0) {
            return null;
        } else {
            int[] positions = new int[2 * javadocCount];
            int index = 0;

            for (int i = 0; i <= max; ++i) {
                int commentStart = this.scanner.commentStarts[i];
                if (commentStart >= 0) {
                    int commentStop = this.scanner.commentStops[i];
                    if (commentStop > 0) {
                        positions[index++] = commentStart;
                        positions[index++] = commentStop - 1;
                    }
                }
            }

            return positions;
        }
    }

    public void getMethodBodies(CompilationUnitDeclaration unit) {
        if (unit != null) {
            if (unit.ignoreMethodBodies) {
                unit.ignoreFurtherInvestigation = true;
            } else if ((unit.bits & 16) == 0) {
                int[] oldLineEnds = this.scanner.lineEnds;
                int oldLinePtr = this.scanner.linePtr;
                CompilationResult compilationResult = unit.compilationResult;
                char[] contents =
                        this.readManager != null
                                ? this.readManager.getContents(compilationResult.compilationUnit)
                                : compilationResult.getContents();
                this.scanner.setSource(contents, compilationResult);
                if (this.javadocParser != null && this.javadocParser.checkDocComment) {
                    this.javadocParser.scanner.setSource(contents);
                }

                if (unit.types != null) {
                    TypeDeclaration[] var9;
                    int var8 = (var9 = unit.types).length;

                    for (int var7 = 0; var7 < var8; ++var7) {
                        TypeDeclaration type = var9[var7];
                        type.parseMethods(this, unit);
                    }
                }

                unit.bits |= 16;
                this.scanner.lineEnds = oldLineEnds;
                this.scanner.linePtr = oldLinePtr;
            }
        }
    }

    protected char getNextCharacter(char[] comment, int[] index) {
        int var10004 = index[0];
        int var10001 = index[0];
        index[0] = var10004 + 1;
        char nextCharacter = comment[var10001];
        switch (nextCharacter) {
            case '\\':
                for (int var10002 = index[0]++; comment[index[0]] == 'u'; var10002 = index[0]++) {}

                var10004 = index[0];
                var10001 = index[0];
                index[0] = var10004 + 1;
                int c1;
                if ((c1 = ScannerHelper.getHexadecimalValue(comment[var10001])) <= 15 && c1 >= 0) {
                    var10004 = index[0];
                    var10001 = index[0];
                    index[0] = var10004 + 1;
                    int c2;
                    if ((c2 = ScannerHelper.getHexadecimalValue(comment[var10001])) <= 15
                            && c2 >= 0) {
                        var10004 = index[0];
                        var10001 = index[0];
                        index[0] = var10004 + 1;
                        int c3;
                        if ((c3 = ScannerHelper.getHexadecimalValue(comment[var10001])) <= 15
                                && c3 >= 0) {
                            var10004 = index[0];
                            var10001 = index[0];
                            index[0] = var10004 + 1;
                            int c4;
                            if ((c4 = ScannerHelper.getHexadecimalValue(comment[var10001])) <= 15
                                    && c4 >= 0) {
                                nextCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
                            }
                        }
                    }
                }
            default:
                return nextCharacter;
        }
    }

    protected Expression getTypeReference(Expression exp) {
        exp.bits &= -8;
        exp.bits |= 4;
        return exp;
    }

    protected void annotateTypeReference(Wildcard ref) {
        int length;
        if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
            if (ref.annotations == null) {
                ref.annotations = new Annotation[ref.getAnnotatableLevels()][];
            }

            System.arraycopy(
                    this.typeAnnotationStack,
                    (this.typeAnnotationPtr -= length) + 1,
                    ref.annotations[0] = new Annotation[length],
                    0,
                    length);
            if (ref.sourceStart > ref.annotations[0][0].sourceStart) {
                ref.sourceStart = ref.annotations[0][0].sourceStart;
            }

            ref.bits |= 1048576;
        }

        if (ref.bound != null) {
            ref.bits |= ref.bound.bits & 1048576;
        }
    }

    protected TypeReference getTypeReference(int dim) {
        Annotation[][] annotationsOnDimensions = null;
        int length = this.identifierLengthStack[this.identifierLengthPtr--];
        Object ref;
        int levels;
        if (length < 0) {
            if (dim > 0) {
                annotationsOnDimensions = this.getAnnotationsOnDimensions(dim);
            }

            ref = TypeReference.baseTypeReference(-length, dim, annotationsOnDimensions);
            ((TypeReference) ref).sourceStart = this.intStack[this.intPtr--];
            if (dim == 0) {
                ((TypeReference) ref).sourceEnd = this.intStack[this.intPtr--];
            } else {
                --this.intPtr;
                ((TypeReference) ref).sourceEnd = this.rBracketPosition;
            }
        } else {
            levels = this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr--];
            if (length == levels && this.genericsLengthStack[this.genericsLengthPtr] == 0) {
                if (length == 1) {
                    --this.genericsLengthPtr;
                    if (dim == 0) {
                        ref =
                                new SingleTypeReference(
                                        this.identifierStack[this.identifierPtr],
                                        this.identifierPositionStack[this.identifierPtr--]);
                    } else {
                        annotationsOnDimensions = this.getAnnotationsOnDimensions(dim);
                        ref =
                                new ArrayTypeReference(
                                        this.identifierStack[this.identifierPtr],
                                        dim,
                                        annotationsOnDimensions,
                                        this.identifierPositionStack[this.identifierPtr--]);
                        ((TypeReference) ref).sourceEnd = this.endPosition;
                        if (annotationsOnDimensions != null) {
                            ((TypeReference) ref).bits |= 1048576;
                        }
                    }
                } else {
                    --this.genericsLengthPtr;
                    char[][] tokens = new char[length][];
                    this.identifierPtr -= length;
                    long[] positions = new long[length];
                    System.arraycopy(
                            this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
                    System.arraycopy(
                            this.identifierPositionStack,
                            this.identifierPtr + 1,
                            positions,
                            0,
                            length);
                    if (dim == 0) {
                        ref = new QualifiedTypeReference(tokens, positions);
                    } else {
                        annotationsOnDimensions = this.getAnnotationsOnDimensions(dim);
                        ref =
                                new ArrayQualifiedTypeReference(
                                        tokens, dim, annotationsOnDimensions, positions);
                        ((TypeReference) ref).sourceEnd = this.endPosition;
                        if (annotationsOnDimensions != null) {
                            ((TypeReference) ref).bits |= 1048576;
                        }
                    }
                }
            } else {
                ref = this.getTypeReferenceForGenericType(dim, length, levels);
            }
        }

        levels = ((TypeReference) ref).getAnnotatableLevels();

        for (int i = levels - 1; i >= 0; --i) {
            if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
                if (((TypeReference) ref).annotations == null) {
                    ((TypeReference) ref).annotations = new Annotation[levels][];
                }

                System.arraycopy(
                        this.typeAnnotationStack,
                        (this.typeAnnotationPtr -= length) + 1,
                        ((TypeReference) ref).annotations[i] = new Annotation[length],
                        0,
                        length);
                if (i == 0) {
                    ((TypeReference) ref).sourceStart =
                            ((TypeReference) ref).annotations[0][0].sourceStart;
                }

                ((TypeReference) ref).bits |= 1048576;
            }
        }

        return (TypeReference) ref;
    }

    protected TypeReference getTypeReferenceForGenericType(
            int dim, int identifierLength, int numberOfIdentifiers) {
        Annotation[][] annotationsOnDimensions =
                dim == 0 ? null : this.getAnnotationsOnDimensions(dim);
        if (identifierLength == 1 && numberOfIdentifiers == 1) {
            int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
            TypeReference[] typeArguments = null;
            if (currentTypeArgumentsLength < 0) {
                typeArguments = TypeReference.NO_TYPE_ARGUMENTS;
            } else {
                typeArguments = new TypeReference[currentTypeArgumentsLength];
                this.genericsPtr -= currentTypeArgumentsLength;
                System.arraycopy(
                        this.genericsStack,
                        this.genericsPtr + 1,
                        typeArguments,
                        0,
                        currentTypeArgumentsLength);
            }

            ParameterizedSingleTypeReference parameterizedSingleTypeReference =
                    new ParameterizedSingleTypeReference(
                            this.identifierStack[this.identifierPtr],
                            typeArguments,
                            dim,
                            annotationsOnDimensions,
                            this.identifierPositionStack[this.identifierPtr--]);
            if (dim != 0) {
                parameterizedSingleTypeReference.sourceEnd = this.endStatementPosition;
            }

            return parameterizedSingleTypeReference;
        } else {
            TypeReference[][] typeArguments = new TypeReference[numberOfIdentifiers][];
            char[][] tokens = new char[numberOfIdentifiers][];
            long[] positions = new long[numberOfIdentifiers];
            int index = numberOfIdentifiers;
            int currentIdentifiersLength = identifierLength;

            while (index > 0) {
                int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
                if (currentTypeArgumentsLength > 0) {
                    this.genericsPtr -= currentTypeArgumentsLength;
                    System.arraycopy(
                            this.genericsStack,
                            this.genericsPtr + 1,
                            typeArguments[index - 1] =
                                    new TypeReference[currentTypeArgumentsLength],
                            0,
                            currentTypeArgumentsLength);
                } else if (currentTypeArgumentsLength < 0) {
                    typeArguments[index - 1] = TypeReference.NO_TYPE_ARGUMENTS;
                }

                switch (currentIdentifiersLength) {
                    case 1:
                        tokens[index - 1] = this.identifierStack[this.identifierPtr];
                        positions[index - 1] = this.identifierPositionStack[this.identifierPtr--];
                        break;
                    default:
                        this.identifierPtr -= currentIdentifiersLength;
                        System.arraycopy(
                                this.identifierStack,
                                this.identifierPtr + 1,
                                tokens,
                                index - currentIdentifiersLength,
                                currentIdentifiersLength);
                        System.arraycopy(
                                this.identifierPositionStack,
                                this.identifierPtr + 1,
                                positions,
                                index - currentIdentifiersLength,
                                currentIdentifiersLength);
                }

                index -= currentIdentifiersLength;
                if (index > 0) {
                    currentIdentifiersLength =
                            this.identifierLengthStack[this.identifierLengthPtr--];
                }
            }

            ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference =
                    new ParameterizedQualifiedTypeReference(
                            tokens, typeArguments, dim, annotationsOnDimensions, positions);
            if (dim != 0) {
                parameterizedQualifiedTypeReference.sourceEnd = this.endStatementPosition;
            }

            return parameterizedQualifiedTypeReference;
        }
    }

    protected NameReference getUnspecifiedReference() {
        return this.getUnspecifiedReference(true);
    }

    protected NameReference getUnspecifiedReference(boolean rejectTypeAnnotations) {
        if (rejectTypeAnnotations) {
            this.consumeNonTypeUseName();
        }

        int length;
        Object ref;
        if ((length = this.identifierLengthStack[this.identifierLengthPtr--]) == 1) {
            ref =
                    new SingleNameReference(
                            this.identifierStack[this.identifierPtr],
                            this.identifierPositionStack[this.identifierPtr--]);
        } else {
            char[][] tokens = new char[length][];
            this.identifierPtr -= length;
            System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
            long[] positions = new long[length];
            System.arraycopy(
                    this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
            ref =
                    new QualifiedNameReference(
                            tokens,
                            positions,
                            (int) (this.identifierPositionStack[this.identifierPtr + 1] >> 32),
                            (int) this.identifierPositionStack[this.identifierPtr + length]);
        }

        return (NameReference) ref;
    }

    protected NameReference getUnspecifiedReferenceOptimized() {
        this.consumeNonTypeUseName();
        int length;
        if ((length = this.identifierLengthStack[this.identifierLengthPtr--]) == 1) {
            NameReference ref =
                    new SingleNameReference(
                            this.identifierStack[this.identifierPtr],
                            this.identifierPositionStack[this.identifierPtr--]);
            ref.bits &= -8;
            ref.bits |= 3;
            return ref;
        } else {
            char[][] tokens = new char[length][];
            this.identifierPtr -= length;
            System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
            long[] positions = new long[length];
            System.arraycopy(
                    this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
            NameReference ref =
                    new QualifiedNameReference(
                            tokens,
                            positions,
                            (int) (this.identifierPositionStack[this.identifierPtr + 1] >> 32),
                            (int) this.identifierPositionStack[this.identifierPtr + length]);
            ref.bits &= -8;
            ref.bits |= 3;
            return ref;
        }
    }

    public void goForBlockStatementsopt() {
        this.firstToken = 68;
        this.scanner.recordLineSeparator = false;
    }

    public void goForBlockStatementsOrCatchHeader() {
        this.firstToken = 8;
        this.scanner.recordLineSeparator = false;
    }

    public void goForClassBodyDeclarations() {
        this.firstToken = 21;
        this.scanner.recordLineSeparator = true;
    }

    public void goForCompilationUnit() {
        this.firstToken = 2;
        this.scanner.foundTaskCount = 0;
        this.scanner.recordLineSeparator = true;
    }

    public void goForExpression(boolean recordLineSeparator) {
        this.firstToken = 9;
        this.scanner.recordLineSeparator = recordLineSeparator;
    }

    public void goForFieldDeclaration() {
        this.firstToken = 30;
        this.scanner.recordLineSeparator = true;
    }

    public void goForGenericMethodDeclaration() {
        this.firstToken = 10;
        this.scanner.recordLineSeparator = true;
    }

    public void goForHeaders() {
        RecoveredType currentType = this.currentRecoveryType();
        if (currentType != null && currentType.insideEnumConstantPart) {
            this.firstToken = 67;
        } else {
            this.firstToken = 16;
        }

        this.scanner.recordLineSeparator = true;
        this.scanner.scanContext = null;
    }

    public void goForImportDeclaration() {
        this.firstToken = 31;
        this.scanner.recordLineSeparator = true;
    }

    public void goForInitializer() {
        this.firstToken = 14;
        this.scanner.recordLineSeparator = false;
    }

    public void goForMemberValue() {
        this.firstToken = 31;
        this.scanner.recordLineSeparator = true;
    }

    public void goForMethodBody() {
        this.firstToken = 3;
        this.scanner.recordLineSeparator = false;
    }

    public void goForPackageDeclaration() {
        this.goForPackageDeclaration(true);
    }

    public void goForPackageDeclaration(boolean recordLineSeparators) {
        this.firstToken = 29;
        this.scanner.recordLineSeparator = recordLineSeparators;
    }

    public void goForTypeDeclaration() {
        this.firstToken = 4;
        this.scanner.recordLineSeparator = true;
    }

    public boolean hasLeadingTagComment(char[] commentPrefixTag, int rangeEnd) {
        int iComment = this.scanner.commentPtr;
        if (iComment < 0) {
            return false;
        } else {
            int iStatement = this.astLengthPtr;
            if (iStatement >= 0 && this.astLengthStack[iStatement] > 1) {
                ASTNode lastNode = this.astStack[this.astPtr];

                label67:
                for (int rangeStart = lastNode.sourceEnd; iComment >= 0; --iComment) {
                    int commentStart = this.scanner.commentStarts[iComment];
                    if (commentStart < 0) {
                        commentStart = -commentStart;
                    }

                    if (commentStart < rangeStart) {
                        return false;
                    }

                    if (commentStart <= rangeEnd) {
                        char[] source = this.scanner.source;

                        int charPos;
                        for (charPos = commentStart + 2; charPos < rangeEnd; ++charPos) {
                            char c = source[charPos];
                            if (c >= 128
                                    || (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & 256) == 0) {
                                break;
                            }
                        }

                        int iTag = 0;

                        for (int length = commentPrefixTag.length; iTag < length; ++charPos) {
                            if (charPos >= rangeEnd || source[charPos] != commentPrefixTag[iTag]) {
                                if (iTag == 0) {
                                    return false;
                                }
                                continue label67;
                            }

                            ++iTag;
                        }

                        return true;
                    }
                }

                return false;
            } else {
                return false;
            }
        }
    }

    protected void ignoreNextClosingBrace() {
        this.ignoreNextClosingBrace = true;
    }

    protected void ignoreExpressionAssignment() {
        --this.intPtr;
        ArrayInitializer arrayInitializer =
                (ArrayInitializer) this.expressionStack[this.expressionPtr--];
        --this.expressionLengthPtr;
        if (!this.statementRecoveryActivated) {
            this.problemReporter()
                    .arrayConstantsOnlyInArrayInitializers(
                            arrayInitializer.sourceStart, arrayInitializer.sourceEnd);
        }
    }

    public void initialize() {
        this.initialize(false);
    }

    public void initialize(boolean parsingCompilationUnit) {
        this.javadoc = null;
        this.astPtr = -1;
        this.astLengthPtr = -1;
        this.expressionPtr = -1;
        this.expressionLengthPtr = -1;
        this.typeAnnotationLengthPtr = -1;
        this.typeAnnotationPtr = -1;
        this.identifierPtr = -1;
        this.identifierLengthPtr = -1;
        this.intPtr = -1;
        this.nestedMethod[this.nestedType = 0] = 0;
        this.switchNestingLevel = 0;
        this.variablesCounter[this.nestedType] = 0;
        this.dimensions = 0;
        this.realBlockPtr = -1;
        this.compilationUnit = null;
        this.referenceContext = null;
        this.endStatementPosition = 0;
        this.valueLambdaNestDepth = -1;
        int astLength = this.astStack.length;
        if (this.noAstNodes.length < astLength) {
            this.noAstNodes = new ASTNode[astLength];
        }

        System.arraycopy(this.noAstNodes, 0, this.astStack, 0, astLength);
        int expressionLength = this.expressionStack.length;
        if (this.noExpressions.length < expressionLength) {
            this.noExpressions = new Expression[expressionLength];
        }

        System.arraycopy(this.noExpressions, 0, this.expressionStack, 0, expressionLength);
        this.scanner.commentPtr = -1;
        this.scanner.foundTaskCount = 0;
        this.scanner.eofPosition = Integer.MAX_VALUE;
        this.recordStringLiterals = true;
        boolean checkNLS = this.options.getSeverity(256) != 256;
        this.checkExternalizeStrings = checkNLS;
        this.scanner.checkNonExternalizedStringLiterals = parsingCompilationUnit && checkNLS;
        this.scanner.checkUninternedIdentityComparison =
                parsingCompilationUnit && this.options.complainOnUninternedIdentityComparison;
        this.scanner.lastPosition = -1;
        this.resetModifiers();
        this.lastCheckPoint = -1;
        this.currentElement = null;
        this.restartRecovery = false;
        this.hasReportedError = false;
        this.recoveredStaticInitializerStart = 0;
        this.lastIgnoredToken = -1;
        this.lastErrorEndPosition = -1;
        this.lastErrorEndPositionBeforeRecovery = -1;
        this.lastJavadocEnd = -1;
        this.listLength = 0;
        this.listTypeParameterLength = 0;
        this.lastPosistion = -1;
        this.rBraceStart = 0;
        this.rBraceEnd = 0;
        this.rBraceSuccessorStart = 0;
        this.rBracketPosition = 0;
        this.genericsIdentifiersLengthPtr = -1;
        this.genericsLengthPtr = -1;
        this.genericsPtr = -1;
    }

    public void initializeScanner() {
        this.scanner =
                new Scanner(
                        false,
                        false,
                        false,
                        this.options.sourceLevel,
                        this.options.complianceLevel,
                        this.options.taskTags,
                        this.options.taskPriorities,
                        this.options.isTaskCaseSensitive,
                        this.options.enablePreviewFeatures);
    }

    public void jumpOverMethodBody() {
        if (this.diet && this.dietInt == 0) {
            this.scanner.diet = true;
        }
    }

    private void jumpOverType() {
        if (this.recoveredTypes != null
                && this.nextTypeStart > -1
                && this.nextTypeStart < this.scanner.currentPosition) {
            TypeDeclaration typeDeclaration = this.recoveredTypes[this.recoveredTypePtr];
            boolean isAnonymous = typeDeclaration.allocation != null;
            this.scanner.startPosition = typeDeclaration.declarationSourceEnd + 1;
            this.scanner.currentPosition = typeDeclaration.declarationSourceEnd + 1;
            this.scanner.diet = false;
            if (!isAnonymous) {
                ((RecoveryScanner) this.scanner).setPendingTokens(new int[] {26, 82});
            } else {
                ((RecoveryScanner) this.scanner).setPendingTokens(new int[] {22, 78, 22});
            }

            this.pendingRecoveredType = typeDeclaration;

            try {
                this.currentToken = this.scanner.getNextToken();
            } catch (InvalidInputException var4) {
            }

            if (++this.recoveredTypePtr < this.recoveredTypes.length) {
                TypeDeclaration nextTypeDeclaration = this.recoveredTypes[this.recoveredTypePtr];
                this.nextTypeStart =
                        nextTypeDeclaration.allocation == null
                                ? nextTypeDeclaration.declarationSourceStart
                                : nextTypeDeclaration.allocation.sourceStart;
            } else {
                this.nextTypeStart = Integer.MAX_VALUE;
            }
        }
    }

    protected void markEnclosingMemberWithLocalType() {
        if (this.currentElement == null) {
            this.markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind.LOCAL);
        }
    }

    protected void markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind context) {
        int i = this.astPtr;

        ASTNode node;
        while (true) {
            if (i < 0) {
                if (this.referenceContext instanceof AbstractMethodDeclaration
                        || this.referenceContext instanceof TypeDeclaration) {
                    node = (ASTNode) this.referenceContext;
                    switch ($SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$LocalTypeKind()[
                            context.ordinal()]) {
                        case 2:
                            node.bits |= 2097152;
                            break;
                        case 3:
                            node.bits |= 2097152;
                        case 1:
                            node.bits |= 2;
                    }
                }

                return;
            }

            node = this.astStack[i];
            if (node instanceof AbstractMethodDeclaration || node instanceof FieldDeclaration) {
                break;
            }

            if (node instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) node;
                if (type.declarationSourceEnd == 0) {
                    break;
                }
            }

            --i;
        }

        switch ($SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$LocalTypeKind()[
                context.ordinal()]) {
            case 2:
                node.bits |= 2097152;
                break;
            case 3:
                node.bits |= 2097152;
            case 1:
                node.bits |= 2;
        }
    }

    protected boolean moveRecoveryCheckpoint() {
        int pos = this.lastCheckPoint;
        this.scanner.startPosition = pos;
        this.scanner.currentPosition = pos;
        this.scanner.diet = false;
        if (this.restartRecovery) {
            this.lastIgnoredToken = -1;
            this.scanner.insideRecovery = true;
            return true;
        } else {
            this.lastIgnoredToken = this.nextIgnoredToken;
            this.nextIgnoredToken = -1;

            do {
                try {
                    this.scanner.resetLookBack();
                    this.nextIgnoredToken = this.scanner.getNextNotFakedToken();
                } catch (InvalidInputException var6) {
                    pos = this.scanner.currentPosition;
                } finally {
                    this.scanner.resetLookBack();
                }
            } while (this.nextIgnoredToken < 0);

            if (this.nextIgnoredToken == 39 && this.currentToken == 39) {
                return false;
            } else if (this.lastCheckPoint == this.scanner.currentPosition) {
                return false;
            } else {
                this.lastCheckPoint = this.scanner.currentPosition;
                this.scanner.startPosition = pos;
                this.scanner.currentPosition = pos;
                this.scanner.commentPtr = -1;
                this.scanner.foundTaskCount = 0;
                return true;
            }
        }
    }

    protected MessageSend newMessageSend() {
        MessageSend m = new MessageSend();
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    m.arguments = new Expression[length],
                    0,
                    length);
        }

        return m;
    }

    protected MessageSend newMessageSendWithTypeArguments() {
        MessageSend m = new MessageSend();
        int length;
        if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            this.expressionPtr -= length;
            System.arraycopy(
                    this.expressionStack,
                    this.expressionPtr + 1,
                    m.arguments = new Expression[length],
                    0,
                    length);
        }

        return m;
    }

    protected void optimizedConcatNodeLists() {
        int var10002 = this.astLengthStack[--this.astLengthPtr]++;
    }

    public boolean atConflictScenario(int token) {
        if (this.unstackedAct == 17648) {
            return false;
        } else {
            if (token != 37) {
                token = token == 23 ? 64 : 89;
            }

            return this.automatonWillShift(token, this.unstackedAct);
        }
    }

    protected void parse() {
        boolean isDietParse = this.diet;
        int oldFirstToken = this.getFirstToken();
        this.hasError = false;
        this.hasReportedError = false;
        int act = 1015;
        this.unstackedAct = 17648;
        this.stateStackTop = -1;
        this.currentToken = this.getFirstToken();

        try {
            this.scanner.setActiveParser(this);

            label278:
            while (true) {
                int stackLength = this.stack.length;
                if (++this.stateStackTop >= stackLength) {
                    System.arraycopy(
                            this.stack, 0, this.stack = new int[stackLength + 255], 0, stackLength);
                }

                this.stack[this.stateStackTop] = act;
                this.unstackedAct = act = this.actFromTokenOrSynthetic(act);
                if (act == 17648 || this.restartRecovery()) {
                    int errorPos = this.scanner.currentPosition - 1;
                    if (!this.hasReportedError && act == 17648) {
                        this.hasError = true;
                    }

                    int previousToken = this.currentToken;
                    switch (this.resumeOnSyntaxError()) {
                        case 0:
                            act = 17648;
                            break label278;
                        case 1:
                            if (act == 17648 && previousToken != 0) {
                                this.lastErrorEndPosition = errorPos;
                            }

                            act = 1015;
                            this.stateStackTop = -1;
                            this.currentToken = this.getFirstToken();
                            continue;
                        case 2:
                            if (act == 17648) {
                                act = this.stack[this.stateStackTop--];
                                continue;
                            }
                    }
                }

                if (act <= 926) {
                    --this.stateStackTop;
                } else {
                    boolean oldValue;
                    if (act <= 17648) {
                        if (act >= 17647) {
                            break;
                        }

                        this.consumeToken(this.currentToken);
                        if (this.currentElement != null) {
                            oldValue = this.recordStringLiterals;
                            this.recordStringLiterals = false;
                            this.recoveryTokenCheck();
                            this.recordStringLiterals = oldValue;
                        }

                        try {
                            this.currentToken = this.fetchNextToken();
                        } catch (InvalidInputException var12) {
                            if (!this.hasReportedError) {
                                this.problemReporter().scannerError(this, var12.getMessage());
                                this.hasReportedError = true;
                            }

                            this.lastCheckPoint = this.scanner.currentPosition;
                            this.currentToken = 0;
                            this.restartRecovery = true;
                        }

                        if (this.statementRecoveryActivated) {
                            this.jumpOverType();
                        }
                        continue;
                    }

                    this.consumeToken(this.currentToken);
                    if (this.currentElement != null) {
                        oldValue = this.recordStringLiterals;
                        this.recordStringLiterals = false;
                        this.recoveryTokenCheck();
                        this.recordStringLiterals = oldValue;
                    }

                    try {
                        this.currentToken = this.fetchNextToken();
                    } catch (InvalidInputException var11) {
                        if (!this.hasReportedError) {
                            this.problemReporter().scannerError(this, var11.getMessage());
                            this.hasReportedError = true;
                        }

                        this.lastCheckPoint = this.scanner.currentPosition;
                        this.currentToken = 0;
                        this.restartRecovery = true;
                    }

                    if (this.statementRecoveryActivated) {
                        this.jumpOverType();
                    }

                    act -= 17648;
                    this.unstackedAct = act;
                }

                while (true) {
                    this.stateStackTop -= rhs[act] - 1;
                    this.unstackedAct = ntAction(this.stack[this.stateStackTop], lhs[act]);
                    this.consumeRule(act);
                    act = this.unstackedAct;
                    if (act == 17647) {
                        break label278;
                    }

                    if (act > 926) {
                        break;
                    }
                }
            }
        } finally {
            this.unstackedAct = 17648;
            this.scanner.setActiveParser((ConflictedParser) null);
        }

        this.endParse(act);
        NLSTag[] tags = this.scanner.getNLSTags();
        if (tags != null) {
            this.compilationUnit.nlsTags = tags;
        }

        this.scanner.checkNonExternalizedStringLiterals = false;
        if (this.scanner.checkUninternedIdentityComparison) {
            this.compilationUnit.validIdentityComparisonLines =
                    this.scanner.getIdentityComparisonLines();
            this.scanner.checkUninternedIdentityComparison = false;
        }

        if (this.reportSyntaxErrorIsRequired && this.hasError && !this.statementRecoveryActivated) {
            if (!this.options.performStatementsRecovery) {
                this.reportSyntaxErrors(isDietParse, oldFirstToken);
            } else {
                RecoveryScannerData data =
                        this.referenceContext.compilationResult().recoveryScannerData;
                if (this.recoveryScanner == null) {
                    this.recoveryScanner = new RecoveryScanner(this.scanner, data);
                } else {
                    this.recoveryScanner.setData(data);
                }

                this.recoveryScanner.setSource(this.scanner.source);
                this.recoveryScanner.lineEnds = this.scanner.lineEnds;
                this.recoveryScanner.linePtr = this.scanner.linePtr;
                this.reportSyntaxErrors(isDietParse, oldFirstToken);
                if (data == null) {
                    this.referenceContext.compilationResult().recoveryScannerData =
                            this.recoveryScanner.getData();
                }

                if (this.methodRecoveryActivated && this.options.performStatementsRecovery) {
                    this.methodRecoveryActivated = false;
                    this.recoverStatements();
                    this.methodRecoveryActivated = true;
                    this.lastAct = 17648;
                }
            }
        }

        this.problemReporter.referenceContext = null;
    }

    protected boolean restartRecovery() {
        return this.restartRecovery;
    }

    protected int fetchNextToken() throws InvalidInputException {
        return this.scanner.getNextToken();
    }

    public void parse(
            ConstructorDeclaration cd,
            CompilationUnitDeclaration unit,
            boolean recordLineSeparator) {
        boolean oldMethodRecoveryActivated = this.methodRecoveryActivated;
        if (this.options.performMethodsFullRecovery) {
            this.methodRecoveryActivated = true;
            this.ignoreNextOpeningBrace = true;
        }

        this.initialize();
        this.goForBlockStatementsopt();
        if (recordLineSeparator) {
            this.scanner.recordLineSeparator = true;
        }

        int var10002 = this.nestedMethod[this.nestedType]++;
        this.pushOnRealBlockStack(0);
        this.referenceContext = cd;
        this.compilationUnit = unit;
        this.scanner.resetTo(cd.bodyStart, cd.bodyEnd);

        try {
            this.parse();
        } catch (AbortCompilation var10) {
            this.lastAct = 17648;
        } finally {
            var10002 = this.nestedMethod[this.nestedType]--;
            if (this.options.performStatementsRecovery) {
                this.methodRecoveryActivated = oldMethodRecoveryActivated;
            }
        }

        this.checkNonNLSAfterBodyEnd(cd.declarationSourceEnd);
        if (this.lastAct == 17648) {
            cd.bits |= 524288;
            this.initialize();
        } else {
            cd.explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
            int length;
            ExplicitConstructorCall explicitConstructorCall;
            if (this.astLengthPtr > -1
                    && (length = this.astLengthStack[this.astLengthPtr--]) != 0) {
                this.astPtr -= length;
                if (!this.options.ignoreMethodBodies) {
                    ASTNode var7;
                    if ((var7 = this.astStack[this.astPtr + 1])
                            instanceof ExplicitConstructorCall) {
                        explicitConstructorCall = (ExplicitConstructorCall) var7;
                        System.arraycopy(
                                this.astStack,
                                this.astPtr + 2,
                                cd.statements = new Statement[length - 1],
                                0,
                                length - 1);
                        cd.constructorCall = explicitConstructorCall;
                    } else {
                        System.arraycopy(
                                this.astStack,
                                this.astPtr + 1,
                                cd.statements = new Statement[length],
                                0,
                                length);
                        cd.constructorCall = SuperReference.implicitSuperConstructorCall();
                    }
                }
            } else {
                if (!this.options.ignoreMethodBodies) {
                    cd.constructorCall = SuperReference.implicitSuperConstructorCall();
                }

                if (!this.containsComment(cd.bodyStart, cd.bodyEnd)) {
                    cd.bits |= 8;
                }
            }

            explicitConstructorCall = cd.constructorCall;
            if (explicitConstructorCall != null && explicitConstructorCall.sourceEnd == 0) {
                explicitConstructorCall.sourceEnd = cd.sourceEnd;
                explicitConstructorCall.sourceStart = cd.sourceStart;
            }
        }
    }

    public void parse(
            FieldDeclaration field,
            TypeDeclaration type,
            CompilationUnitDeclaration unit,
            char[] initializationSource) {
        this.initialize();
        this.goForExpression(true);
        int var10002 = this.nestedMethod[this.nestedType]++;
        this.referenceContext = type;
        this.compilationUnit = unit;
        this.scanner.setSource(initializationSource);
        this.scanner.resetTo(0, initializationSource.length - 1);

        try {
            this.parse();
        } catch (AbortCompilation var9) {
            this.lastAct = 17648;
        } finally {
            var10002 = this.nestedMethod[this.nestedType]--;
        }

        if (this.lastAct == 17648) {
            field.bits |= 524288;
        } else {
            field.initialization = this.expressionStack[this.expressionPtr];
            if ((type.bits & 2) != 0) {
                field.bits |= 2;
            }
        }
    }

    public CompilationUnitDeclaration parse(
            ICompilationUnit sourceUnit, CompilationResult compilationResult) {
        return this.parse(sourceUnit, compilationResult, -1, -1);
    }

    public CompilationUnitDeclaration parse(
            ICompilationUnit sourceUnit, CompilationResult compilationResult, int start, int end) {
        CompilationUnitDeclaration unit;
        try {
            this.initialize(true);
            this.goForCompilationUnit();
            this.referenceContext =
                    this.compilationUnit =
                            new CompilationUnitDeclaration(
                                    this.problemReporter, compilationResult, 0);
            ReferenceContext problemReporterContext = this.problemReporter.referenceContext;
            this.problemReporter.referenceContext = this.referenceContext;

            // Упрощённая проверка версии без использования Runtime.Version
            if (this.problemReporter != null
                    && this.options != null
                    && this.options.requestedSourceVersion != null
                    && !this.options.requestedSourceVersion.isBlank()) {
                try {
                    String requestedVersion = this.options.requestedSourceVersion;
                    String latestVersion = CompilerOptions.getLatestVersion();
                    // Простое сравнение строк (если формат версии гарантированно "X.Y.Z")
                    if (requestedVersion.compareTo(latestVersion) > 0) {
                        this.problemReporter.tooRecentJavaVersion(requestedVersion, latestVersion);
                    }
                } catch (Exception var14) {
                    this.problemReporter.abortDueToInternalError(var14.getMessage());
                }
            }

            this.problemReporter.referenceContext = problemReporterContext;

            char[] contents;
            try {
                contents =
                        this.readManager != null
                                ? this.readManager.getContents(sourceUnit)
                                : sourceUnit.getContents();
            } catch (AbortCompilationUnit var13) {
                this.problemReporter()
                        .cannotReadSource(this.compilationUnit, var13, this.options.verbose);
                contents = CharOperation.NO_CHAR;
            }

            compilationResult.cacheContents(contents);
            this.scanner.setSource(contents);
            this.compilationUnit.sourceEnd = this.scanner.source.length - 1;
            if (end != -1) {
                this.scanner.resetTo(start, end);
            }

            if (this.javadocParser != null && this.javadocParser.checkDocComment) {
                this.javadocParser.scanner.setSource(contents);
                if (end != -1) {
                    this.javadocParser.scanner.resetTo(start, end);
                }
            }

            this.parse();
        } finally {
            unit = this.compilationUnit;
            this.compilationUnit = null;
            if (!this.diet) {
                unit.bits |= 16;
            }
        }

        return unit;
    }

    public void parse(
            Initializer initializer, TypeDeclaration type, CompilationUnitDeclaration unit) {
        boolean oldMethodRecoveryActivated = this.methodRecoveryActivated;
        if (this.options.performMethodsFullRecovery) {
            this.methodRecoveryActivated = true;
        }

        this.initialize();
        this.goForBlockStatementsopt();
        int var10002 = this.nestedMethod[this.nestedType]++;
        this.pushOnRealBlockStack(0);
        this.referenceContext = type;
        this.compilationUnit = unit;
        this.scanner.resetTo(initializer.bodyStart, initializer.bodyEnd);

        try {
            this.parse();
        } catch (AbortCompilation var9) {
            this.lastAct = 17648;
        } finally {
            var10002 = this.nestedMethod[this.nestedType]--;
            if (this.options.performStatementsRecovery) {
                this.methodRecoveryActivated = oldMethodRecoveryActivated;
            }
        }

        this.checkNonNLSAfterBodyEnd(initializer.declarationSourceEnd);
        if (this.lastAct == 17648) {
            initializer.bits |= 524288;
        } else {
            initializer.block.explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
            int length;
            if (this.astLengthPtr > -1 && (length = this.astLengthStack[this.astLengthPtr--]) > 0) {
                System.arraycopy(
                        this.astStack,
                        (this.astPtr -= length) + 1,
                        initializer.block.statements = new Statement[length],
                        0,
                        length);
            } else if (!this.containsComment(
                    initializer.block.sourceStart, initializer.block.sourceEnd)) {
                Block var10000 = initializer.block;
                var10000.bits |= 8;
            }

            if ((type.bits & 2) != 0) {
                initializer.bits |= 2;
            }
        }
    }

    public void parse(MethodDeclaration md, CompilationUnitDeclaration unit) {
        if (!md.isAbstract()) {
            if (!md.isNative()) {
                if ((md.modifiers & 16777216) == 0) {
                    boolean oldMethodRecoveryActivated = this.methodRecoveryActivated;
                    if (this.options.performMethodsFullRecovery) {
                        this.ignoreNextOpeningBrace = true;
                        this.methodRecoveryActivated = true;
                        this.rParenPos = md.sourceEnd;
                    }

                    this.initialize();
                    this.goForBlockStatementsopt();
                    int var10002 = this.nestedMethod[this.nestedType]++;
                    this.pushOnRealBlockStack(0);
                    this.referenceContext = md;
                    this.compilationUnit = unit;
                    this.scanner.resetTo(md.bodyStart, md.bodyEnd);

                    try {
                        this.parse();
                    } catch (AbortCompilation var8) {
                        this.lastAct = 17648;
                    } finally {
                        var10002 = this.nestedMethod[this.nestedType]--;
                        if (this.options.performStatementsRecovery) {
                            this.methodRecoveryActivated = oldMethodRecoveryActivated;
                        }
                    }

                    this.checkNonNLSAfterBodyEnd(md.declarationSourceEnd);
                    if (this.lastAct == 17648) {
                        md.bits |= 524288;
                    } else {
                        md.explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
                        int length;
                        if (this.astLengthPtr > -1
                                && (length = this.astLengthStack[this.astLengthPtr--]) != 0) {
                            if (this.options.ignoreMethodBodies) {
                                this.astPtr -= length;
                            } else {
                                System.arraycopy(
                                        this.astStack,
                                        (this.astPtr -= length) + 1,
                                        md.statements = new Statement[length],
                                        0,
                                        length);
                            }
                        } else if (!this.containsComment(md.bodyStart, md.bodyEnd)) {
                            md.bits |= 8;
                        }
                    }
                }
            }
        }
    }

    public ASTNode[] parseClassBodyDeclarations(
            char[] source, int offset, int length, CompilationUnitDeclaration unit) {
        this.initialize();
        this.goForClassBodyDeclarations();
        return this.parseBodyDeclarations(source, offset, length, unit, (short) 1);
    }

    private ASTNode[] parseBodyDeclarations(
            char[] source,
            int offset,
            int length,
            CompilationUnitDeclaration unit,
            short classRecordType) {
        boolean oldDiet = this.diet;
        int oldInt = this.dietInt;
        boolean oldTolerateDefaultClassMethods = this.tolerateDefaultClassMethods;
        this.scanner.setSource(source);
        this.scanner.resetTo(offset, offset + length - 1);
        if (this.javadocParser != null && this.javadocParser.checkDocComment) {
            this.javadocParser.scanner.setSource(source);
            this.javadocParser.scanner.resetTo(offset, offset + length - 1);
        }

        this.nestedType = 1;
        TypeDeclaration referenceContextTypeDeclaration =
                new TypeDeclaration(unit.compilationResult);
        referenceContextTypeDeclaration.name = Util.EMPTY_STRING.toCharArray();
        referenceContextTypeDeclaration.fields = new FieldDeclaration[0];
        this.compilationUnit = unit;
        unit.types = new TypeDeclaration[1];
        unit.types[0] = referenceContextTypeDeclaration;
        this.referenceContext = unit;

        try {
            this.diet = true;
            this.dietInt = 0;
            this.tolerateDefaultClassMethods = this.parsingJava8Plus;
            this.parse();
        } catch (AbortCompilation var24) {
            this.lastAct = 17648;
        } finally {
            this.diet = oldDiet;
            this.dietInt = oldInt;
            this.tolerateDefaultClassMethods = oldTolerateDefaultClassMethods;
        }

        ASTNode[] result = null;
        if (this.lastAct == 17648) {
            if (!this.options.performMethodsFullRecovery
                    && !this.options.performStatementsRecovery) {
                return null;
            }

            final List bodyDeclarations = new ArrayList();
            unit.ignoreFurtherInvestigation = false;
            final Predicate<MethodDeclaration> methodPred =
                    classRecordType == 1
                            ? (mD) -> {
                                return !mD.isDefaultConstructor();
                            }
                            : (mD) -> {
                                return (mD.bits & 1024) == 0;
                            };
            final Consumer<FieldDeclaration> fieldAction =
                    classRecordType == 1
                            ? (fD) -> {
                                bodyDeclarations.add(fD);
                            }
                            : (fD) -> {
                                if ((fD.bits & 1024) == 0) {
                                    bodyDeclarations.add(fD);
                                }
                            };
            ASTVisitor visitor =
                    new ASTVisitor() {
                        public boolean visit(
                                MethodDeclaration methodDeclaration, ClassScope scope) {
                            if (methodPred.test(methodDeclaration)) {
                                bodyDeclarations.add(methodDeclaration);
                            }

                            return false;
                        }

                        public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
                            fieldAction.accept(fieldDeclaration);
                            return false;
                        }

                        public boolean visit(
                                TypeDeclaration memberTypeDeclaration, ClassScope scope) {
                            bodyDeclarations.add(memberTypeDeclaration);
                            return false;
                        }
                    };
            unit.traverse(visitor, unit.scope);
            unit.ignoreFurtherInvestigation = true;
            result = (ASTNode[]) bodyDeclarations.toArray(new ASTNode[bodyDeclarations.size()]);
        } else {
            int astLength;
            if (this.astLengthPtr > -1
                    && (astLength = this.astLengthStack[this.astLengthPtr--]) != 0) {
                result = new ASTNode[astLength];
                this.astPtr -= astLength;
                System.arraycopy(this.astStack, this.astPtr + 1, result, 0, astLength);
            } else {
                result = new ASTNode[0];
            }
        }

        boolean containsInitializers = false;
        TypeDeclaration typeDeclaration = null;
        ASTNode[] var16 = result;
        int var15 = result.length;

        for (int var31 = 0; var31 < var15; ++var31) {
            ASTNode node = var16[var31];
            if (node instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) node;
                type.parseMethods(this, unit);
            } else if (node instanceof AbstractMethodDeclaration) {
                AbstractMethodDeclaration method = (AbstractMethodDeclaration) node;
                method.parseStatements(this, unit);
            } else if (node instanceof FieldDeclaration) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
                switch (fieldDeclaration.getKind()) {
                    case 2:
                        containsInitializers = true;
                        if (typeDeclaration == null) {
                            typeDeclaration = referenceContextTypeDeclaration;
                        }

                        if (typeDeclaration.fields == null) {
                            typeDeclaration.fields = new FieldDeclaration[1];
                            typeDeclaration.fields[0] = fieldDeclaration;
                        } else {
                            int length2 = typeDeclaration.fields.length;
                            FieldDeclaration[] temp = new FieldDeclaration[length2 + 1];
                            System.arraycopy(typeDeclaration.fields, 0, temp, 0, length2);
                            temp[length2] = fieldDeclaration;
                            typeDeclaration.fields = temp;
                        }
                }
            }

            if ((node.bits & 524288) != 0
                    && !this.options.performMethodsFullRecovery
                    && !this.options.performStatementsRecovery) {
                return null;
            }
        }

        if (containsInitializers) {
            FieldDeclaration[] fieldDeclarations = typeDeclaration.fields;
            FieldDeclaration[] var34 = fieldDeclarations;
            int var33 = fieldDeclarations.length;

            for (var15 = 0; var15 < var33; ++var15) {
                FieldDeclaration fieldDeclaration = var34[var15];
                Initializer initializer = (Initializer) fieldDeclaration;
                initializer.parseStatements(this, typeDeclaration, unit);
                if ((initializer.bits & 524288) != 0
                        && !this.options.performMethodsFullRecovery
                        && !this.options.performStatementsRecovery) {
                    return null;
                }
            }
        }

        return result;
    }

    public Expression parseLambdaExpression(
            char[] source,
            int offset,
            int length,
            CompilationUnitDeclaration unit,
            boolean recordLineSeparators) {
        this.haltOnSyntaxError = true;
        this.reparsingFunctionalExpression = true;
        return this.parseExpression(source, offset, length, unit, recordLineSeparators);
    }

    public char[][] parsePackageDeclaration(char[] source, CompilationResult result) {
        this.initialize();
        this.goForPackageDeclaration(false);
        this.referenceContext =
                this.compilationUnit =
                        new CompilationUnitDeclaration(
                                this.problemReporter(), result, source.length);
        this.scanner.setSource(source);

        try {
            this.parse();
        } catch (AbortCompilation var4) {
            this.lastAct = 17648;
        }

        if (this.lastAct == 17648) {
            return null;
        } else {
            return this.compilationUnit.currentPackage == null
                    ? null
                    : this.compilationUnit.currentPackage.getImportName();
        }
    }

    public Expression parseReferenceExpression(
            char[] source,
            int offset,
            int length,
            CompilationUnitDeclaration unit,
            boolean recordLineSeparators) {
        this.reparsingFunctionalExpression = true;
        return this.parseExpression(source, offset, length, unit, recordLineSeparators);
    }

    public Expression parseExpression(
            char[] source,
            int offset,
            int length,
            CompilationUnitDeclaration unit,
            boolean recordLineSeparators) {
        this.initialize();
        this.goForExpression(recordLineSeparators);
        int var10002 = this.nestedMethod[this.nestedType]++;
        this.referenceContext = unit;
        this.compilationUnit = unit;
        this.scanner.setSource(source);
        this.scanner.resetTo(offset, offset + length - 1);

        try {
            this.parse();
        } catch (AbortCompilation var10) {
            this.lastAct = 17648;
        } finally {
            var10002 = this.nestedMethod[this.nestedType]--;
        }

        return this.lastAct == 17648 ? null : this.expressionStack[this.expressionPtr];
    }

    public Expression parseMemberValue(
            char[] source, int offset, int length, CompilationUnitDeclaration unit) {
        this.initialize();
        this.goForMemberValue();
        int var10002 = this.nestedMethod[this.nestedType]++;
        this.referenceContext = unit;
        this.compilationUnit = unit;
        this.scanner.setSource(source);
        this.scanner.resetTo(offset, offset + length - 1);

        try {
            this.parse();
        } catch (AbortCompilation var9) {
            this.lastAct = 17648;
        } finally {
            var10002 = this.nestedMethod[this.nestedType]--;
        }

        return this.lastAct == 17648 ? null : this.expressionStack[this.expressionPtr];
    }

    public void parseStatements(
            ReferenceContext rc,
            int start,
            int end,
            TypeDeclaration[] types,
            CompilationUnitDeclaration unit) {
        boolean oldStatementRecoveryEnabled = this.statementRecoveryActivated;
        this.statementRecoveryActivated = true;
        this.initialize();
        this.goForBlockStatementsopt();
        int var10002 = this.nestedMethod[this.nestedType]++;
        this.pushOnRealBlockStack(0);
        this.pushOnAstLengthStack(0);
        this.referenceContext = rc;
        this.compilationUnit = unit;
        this.pendingRecoveredType = null;
        if (types != null && types.length > 0) {
            this.recoveredTypes = types;
            this.recoveredTypePtr = 0;
            this.nextTypeStart =
                    this.recoveredTypes[0].allocation == null
                            ? this.recoveredTypes[0].declarationSourceStart
                            : this.recoveredTypes[0].allocation.sourceStart;
        } else {
            this.recoveredTypes = null;
            this.recoveredTypePtr = -1;
            this.nextTypeStart = -1;
        }

        this.scanner.resetTo(start, end);
        this.lastCheckPoint = this.scanner.initialPosition;
        this.stateStackTop = -1;

        try {
            this.parse();
        } catch (AbortCompilation var11) {
            this.lastAct = 17648;
        } finally {
            var10002 = this.nestedMethod[this.nestedType]--;
            this.recoveredTypes = null;
            this.statementRecoveryActivated = oldStatementRecoveryEnabled;
        }

        this.checkNonNLSAfterBodyEnd(end);
    }

    public void persistLineSeparatorPositions() {
        if (this.scanner.recordLineSeparator) {
            this.compilationUnit.compilationResult.lineSeparatorPositions =
                    this.scanner.getLineEnds();
        }
    }

    protected void prepareForBlockStatements() {
        this.nestedMethod[this.nestedType = 0] = 1;
        this.variablesCounter[this.nestedType] = 0;
        this.realBlockStack[this.realBlockPtr = 1] = 0;
        this.switchNestingLevel = 0;
    }

    public ProblemReporter problemReporter() {
        if (this.scanner.recordLineSeparator) {
            this.compilationUnit.compilationResult.lineSeparatorPositions =
                    this.scanner.getLineEnds();
        }

        this.problemReporter.referenceContext = this.referenceContext;
        return this.problemReporter;
    }

    protected void pushIdentifier(char[] identifier, long position) {
        int stackLength = this.identifierStack.length;
        if (++this.identifierPtr >= stackLength) {
            System.arraycopy(
                    this.identifierStack,
                    0,
                    this.identifierStack = new char[stackLength + 20][],
                    0,
                    stackLength);
            System.arraycopy(
                    this.identifierPositionStack,
                    0,
                    this.identifierPositionStack = new long[stackLength + 20],
                    0,
                    stackLength);
        }

        this.identifierStack[this.identifierPtr] = identifier;
        this.identifierPositionStack[this.identifierPtr] = position;
        stackLength = this.identifierLengthStack.length;
        if (++this.identifierLengthPtr >= stackLength) {
            System.arraycopy(
                    this.identifierLengthStack,
                    0,
                    this.identifierLengthStack = new int[stackLength + 10],
                    0,
                    stackLength);
        }

        this.identifierLengthStack[this.identifierLengthPtr] = 1;
        if (this.parsingJava8Plus
                && !JavaFeature.UNNAMMED_PATTERNS_AND_VARS.isSupported(this.options)
                && identifier.length == 1
                && identifier[0] == '_'
                && !this.processingLambdaParameterList) {
            if (this.parsingJava22Plus) {
                this.problemReporter()
                        .validateJavaFeatureSupport(
                                JavaFeature.UNNAMMED_PATTERNS_AND_VARS,
                                (int) (position >>> 32),
                                (int) position);
            } else {
                this.problemReporter()
                        .illegalUseOfUnderscoreAsAnIdentifier(
                                (int) (position >>> 32),
                                (int) position,
                                this.parsingJava9Plus,
                                false);
            }
        }
    }

    protected void pushIdentifier() {
        this.pushIdentifier(
                this.scanner.getCurrentIdentifierSource(),
                ((long) this.scanner.startPosition << 32)
                        + (long) (this.scanner.currentPosition - 1));
    }

    protected void pushIdentifier(int flag) {
        int stackLength = this.identifierLengthStack.length;
        if (++this.identifierLengthPtr >= stackLength) {
            System.arraycopy(
                    this.identifierLengthStack,
                    0,
                    this.identifierLengthStack = new int[stackLength + 10],
                    0,
                    stackLength);
        }

        this.identifierLengthStack[this.identifierLengthPtr] = flag;
    }

    protected void pushOnAstLengthStack(int pos) {
        int stackLength = this.astLengthStack.length;
        if (++this.astLengthPtr >= stackLength) {
            System.arraycopy(
                    this.astLengthStack,
                    0,
                    this.astLengthStack = new int[stackLength + 255],
                    0,
                    stackLength);
        }

        this.astLengthStack[this.astLengthPtr] = pos;
    }

    protected void pushOnAstStack(ASTNode node) {
        int stackLength = this.astStack.length;
        if (++this.astPtr >= stackLength) {
            System.arraycopy(
                    this.astStack,
                    0,
                    this.astStack = new ASTNode[stackLength + 100],
                    0,
                    stackLength);
            this.astPtr = stackLength;
        }

        this.astStack[this.astPtr] = node;
        stackLength = this.astLengthStack.length;
        if (++this.astLengthPtr >= stackLength) {
            System.arraycopy(
                    this.astLengthStack,
                    0,
                    this.astLengthStack = new int[stackLength + 100],
                    0,
                    stackLength);
        }

        this.astLengthStack[this.astLengthPtr] = 1;
    }

    protected void pushOnTypeAnnotationStack(Annotation annotation) {
        int stackLength = this.typeAnnotationStack.length;
        if (++this.typeAnnotationPtr >= stackLength) {
            System.arraycopy(
                    this.typeAnnotationStack,
                    0,
                    this.typeAnnotationStack = new Annotation[stackLength + 100],
                    0,
                    stackLength);
        }

        this.typeAnnotationStack[this.typeAnnotationPtr] = annotation;
        stackLength = this.typeAnnotationLengthStack.length;
        if (++this.typeAnnotationLengthPtr >= stackLength) {
            System.arraycopy(
                    this.typeAnnotationLengthStack,
                    0,
                    this.typeAnnotationLengthStack = new int[stackLength + 100],
                    0,
                    stackLength);
        }

        this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr] = 1;
    }

    protected void pushOnTypeAnnotationLengthStack(int pos) {
        int stackLength = this.typeAnnotationLengthStack.length;
        if (++this.typeAnnotationLengthPtr >= stackLength) {
            System.arraycopy(
                    this.typeAnnotationLengthStack,
                    0,
                    this.typeAnnotationLengthStack = new int[stackLength + 100],
                    0,
                    stackLength);
        }

        this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr] = pos;
    }

    protected void pushOnExpressionStack(Expression expr) {
        int stackLength = this.expressionStack.length;
        if (++this.expressionPtr >= stackLength) {
            System.arraycopy(
                    this.expressionStack,
                    0,
                    this.expressionStack = new Expression[stackLength + 100],
                    0,
                    stackLength);
        }

        this.expressionStack[this.expressionPtr] = expr;
        stackLength = this.expressionLengthStack.length;
        if (++this.expressionLengthPtr >= stackLength) {
            System.arraycopy(
                    this.expressionLengthStack,
                    0,
                    this.expressionLengthStack = new int[stackLength + 100],
                    0,
                    stackLength);
        }

        this.expressionLengthStack[this.expressionLengthPtr] = 1;
    }

    protected void pushOnExpressionStackLengthStack(int pos) {
        int stackLength = this.expressionLengthStack.length;
        if (++this.expressionLengthPtr >= stackLength) {
            System.arraycopy(
                    this.expressionLengthStack,
                    0,
                    this.expressionLengthStack = new int[stackLength + 255],
                    0,
                    stackLength);
        }

        this.expressionLengthStack[this.expressionLengthPtr] = pos;
    }

    protected void pushOnGenericsIdentifiersLengthStack(int pos) {
        int stackLength = this.genericsIdentifiersLengthStack.length;
        if (++this.genericsIdentifiersLengthPtr >= stackLength) {
            System.arraycopy(
                    this.genericsIdentifiersLengthStack,
                    0,
                    this.genericsIdentifiersLengthStack = new int[stackLength + 10],
                    0,
                    stackLength);
        }

        this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr] = pos;
    }

    protected void pushOnGenericsLengthStack(int pos) {
        int stackLength = this.genericsLengthStack.length;
        if (++this.genericsLengthPtr >= stackLength) {
            System.arraycopy(
                    this.genericsLengthStack,
                    0,
                    this.genericsLengthStack = new int[stackLength + 10],
                    0,
                    stackLength);
        }

        this.genericsLengthStack[this.genericsLengthPtr] = pos;
    }

    protected void pushOnGenericsStack(ASTNode node) {
        int stackLength = this.genericsStack.length;
        if (++this.genericsPtr >= stackLength) {
            System.arraycopy(
                    this.genericsStack,
                    0,
                    this.genericsStack = new ASTNode[stackLength + 10],
                    0,
                    stackLength);
        }

        this.genericsStack[this.genericsPtr] = node;
        stackLength = this.genericsLengthStack.length;
        if (++this.genericsLengthPtr >= stackLength) {
            System.arraycopy(
                    this.genericsLengthStack,
                    0,
                    this.genericsLengthStack = new int[stackLength + 10],
                    0,
                    stackLength);
        }

        this.genericsLengthStack[this.genericsLengthPtr] = 1;
    }

    protected void pushOnIntStack(int pos) {
        int stackLength = this.intStack.length;
        if (++this.intPtr >= stackLength) {
            System.arraycopy(
                    this.intStack, 0, this.intStack = new int[stackLength + 255], 0, stackLength);
        }

        this.intStack[this.intPtr] = pos;
    }

    protected void pushOnRealBlockStack(int i) {
        int stackLength = this.realBlockStack.length;
        if (++this.realBlockPtr >= stackLength) {
            System.arraycopy(
                    this.realBlockStack,
                    0,
                    this.realBlockStack = new int[stackLength + 255],
                    0,
                    stackLength);
        }

        this.realBlockStack[this.realBlockPtr] = i;
    }

    protected void recoverStatements() {
        class MethodVisitor extends ASTVisitor {
            public ASTVisitor typeVisitor;
            TypeDeclaration enclosingType;
            TypeDeclaration[] types = new TypeDeclaration[0];
            int typePtr = -1;

            public void endVisit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
                this.endVisitMethod(constructorDeclaration, scope);
            }

            public void endVisit(Initializer initializer, MethodScope scope) {
                if (initializer.block != null) {
                    TypeDeclaration[] foundTypes = null;
                    int length = 0;
                    if (this.typePtr > -1) {
                        length = this.typePtr + 1;
                        foundTypes = new TypeDeclaration[length];
                        System.arraycopy(this.types, 0, foundTypes, 0, length);
                    }

                    ReferenceContext oldContext = Parser.this.referenceContext;
                    Parser.this.recoveryScanner.resetTo(initializer.bodyStart, initializer.bodyEnd);
                    Scanner oldScanner = Parser.this.scanner;
                    Parser.this.scanner = Parser.this.recoveryScanner;
                    Parser.this.parseStatements(
                            this.enclosingType,
                            initializer.bodyStart,
                            initializer.bodyEnd,
                            foundTypes,
                            Parser.this.compilationUnit);
                    Parser.this.scanner = oldScanner;
                    Parser.this.referenceContext = oldContext;

                    for (int i = 0; i < length; ++i) {
                        foundTypes[i].traverse(this.typeVisitor, (BlockScope) scope);
                    }
                }
            }

            public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
                this.endVisitMethod(methodDeclaration, scope);
            }

            private void endVisitMethod(
                    AbstractMethodDeclaration methodDeclaration, ClassScope scope) {
                TypeDeclaration[] foundTypes = null;
                int length = 0;
                if (this.typePtr > -1) {
                    length = this.typePtr + 1;
                    foundTypes = new TypeDeclaration[length];
                    System.arraycopy(this.types, 0, foundTypes, 0, length);
                }

                ReferenceContext oldContext = Parser.this.referenceContext;
                Parser.this.recoveryScanner.resetTo(
                        methodDeclaration.bodyStart, methodDeclaration.bodyEnd);
                Scanner oldScanner = Parser.this.scanner;
                Parser.this.scanner = Parser.this.recoveryScanner;
                Parser.this.parseStatements(
                        methodDeclaration,
                        methodDeclaration.bodyStart,
                        methodDeclaration.bodyEnd,
                        foundTypes,
                        Parser.this.compilationUnit);
                Parser.this.scanner = oldScanner;
                Parser.this.referenceContext = oldContext;

                for (int i = 0; i < length; ++i) {
                    foundTypes[i].traverse(this.typeVisitor, scope);
                }
            }

            public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
                this.typePtr = -1;
                return true;
            }

            public boolean visit(Initializer initializer, MethodScope scope) {
                this.typePtr = -1;
                return initializer.block != null;
            }

            public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
                this.typePtr = -1;
                return true;
            }

            private boolean visit(TypeDeclaration typeDeclaration) {
                if (this.types.length <= ++this.typePtr) {
                    int length = this.typePtr;
                    System.arraycopy(
                            this.types,
                            0,
                            this.types = new TypeDeclaration[length * 2 + 1],
                            0,
                            length);
                }

                this.types[this.typePtr] = typeDeclaration;
                return false;
            }

            public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
                return this.visit(typeDeclaration);
            }

            public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
                return this.visit(typeDeclaration);
            }
        }

        MethodVisitor methodVisitor = new MethodVisitor();

        class TypeVisitor extends ASTVisitor {
            public MethodVisitor methodVisitor;
            TypeDeclaration[] types = new TypeDeclaration[0];
            int typePtr = -1;

            public void endVisit(TypeDeclaration typeDeclaration, BlockScope scope) {
                this.endVisitType();
            }

            public void endVisit(TypeDeclaration typeDeclaration, ClassScope scope) {
                this.endVisitType();
            }

            private void endVisitType() {
                --this.typePtr;
            }

            public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
                if (constructorDeclaration.isDefaultConstructor()) {
                    return false;
                } else {
                    constructorDeclaration.traverse(this.methodVisitor, scope);
                    return false;
                }
            }

            public boolean visit(Initializer initializer, MethodScope scope) {
                if (initializer.block == null) {
                    return false;
                } else {
                    this.methodVisitor.enclosingType = this.types[this.typePtr];
                    initializer.traverse(this.methodVisitor, scope);
                    return false;
                }
            }

            public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
                methodDeclaration.traverse(this.methodVisitor, scope);
                return false;
            }

            private boolean visit(TypeDeclaration typeDeclaration) {
                if (this.types.length <= ++this.typePtr) {
                    int length = this.typePtr;
                    System.arraycopy(
                            this.types,
                            0,
                            this.types = new TypeDeclaration[length * 2 + 1],
                            0,
                            length);
                }

                this.types[this.typePtr] = typeDeclaration;
                return true;
            }

            public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
                return this.visit(typeDeclaration);
            }

            public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
                return this.visit(typeDeclaration);
            }
        }

        TypeVisitor typeVisitor = new TypeVisitor();
        methodVisitor.typeVisitor = typeVisitor;
        typeVisitor.methodVisitor = methodVisitor;
        ReferenceContext var4;
        if ((var4 = this.referenceContext) instanceof AbstractMethodDeclaration) {
            AbstractMethodDeclaration method = (AbstractMethodDeclaration) var4;
            method.traverse(methodVisitor, (ClassScope) null);
        } else {
            ReferenceContext var6;
            if ((var6 = this.referenceContext) instanceof TypeDeclaration) {
                TypeDeclaration typeContext = (TypeDeclaration) var6;
                int length = typeContext.fields.length;
                int i = 0;

                while (i < length) {
                    FieldDeclaration fieldDeclaration = typeContext.fields[i];
                    switch (fieldDeclaration.getKind()) {
                        case 2:
                            Initializer initializer = (Initializer) fieldDeclaration;
                            if (initializer.block != null) {
                                methodVisitor.enclosingType = typeContext;
                                initializer.traverse(methodVisitor, (MethodScope) null);
                            }
                        default:
                            ++i;
                    }
                }
            }
        }
    }

    public void recoveryExitFromVariable() {
        if (this.currentElement != null && this.currentElement.parent != null) {
            RecoveredElement var2;
            if ((var2 = this.currentElement) instanceof RecoveredLocalVariable) {
                RecoveredLocalVariable recoveredLocalVariable = (RecoveredLocalVariable) var2;
                int end = recoveredLocalVariable.localDeclaration.sourceEnd;
                this.currentElement.updateSourceEndIfNecessary(end);
                this.currentElement = this.currentElement.parent;
            } else {
                RecoveredElement var4;
                if ((var4 = this.currentElement) instanceof RecoveredField) {
                    RecoveredField recoveredField = (RecoveredField) var4;
                    if (!(this.currentElement instanceof RecoveredInitializer)
                            && this.currentElement.bracketBalance <= 0) {
                        int end = recoveredField.fieldDeclaration.sourceEnd;
                        this.currentElement.updateSourceEndIfNecessary(end);
                        this.currentElement = this.currentElement.parent;
                    }
                }
            }
        }
    }

    public void recoveryTokenCheck() {
        RecoveredElement newElement;
        switch (this.currentToken) {
            case 26:
                this.endStatementPosition = this.scanner.currentPosition - 1;
                this.endPosition = this.scanner.startPosition - 1;
                RecoveredType currentType = this.currentRecoveryType();
                if (currentType != null) {
                    currentType.insideEnumConstantPart = false;
                }
            default:
                if (this.rBraceEnd > this.rBraceSuccessorStart
                        && this.scanner.currentPosition != this.scanner.startPosition) {
                    this.rBraceSuccessorStart = this.scanner.startPosition;
                }
                break;
            case 33:
                if (this.ignoreNextClosingBrace) {
                    this.ignoreNextClosingBrace = false;
                } else {
                    this.rBraceStart = this.scanner.startPosition - 1;
                    this.rBraceEnd = this.scanner.currentPosition - 1;
                    this.endPosition = this.flushCommentsDefinedPriorTo(this.rBraceEnd);
                    newElement =
                            this.currentElement.updateOnClosingBrace(
                                    this.scanner.startPosition, this.rBraceEnd);
                    this.lastCheckPoint = this.scanner.currentPosition;
                    if (newElement != this.currentElement) {
                        this.currentElement = newElement;
                    }
                }
                break;
            case 61:
                if (this.recordStringLiterals
                        && this.checkExternalizeStrings
                        && this.lastPosistion < this.scanner.currentPosition
                        && !this.statementRecoveryActivated) {
                    StringLiteral stringLiteral =
                            this.createStringLiteral(
                                    this.scanner.getCurrentTokenSourceString(),
                                    this.scanner.startPosition,
                                    this.scanner.currentPosition - 1,
                                    Util.getLineNumber(
                                            this.scanner.startPosition,
                                            this.scanner.lineEnds,
                                            0,
                                            this.scanner.linePtr));
                    this.compilationUnit.recordStringLiteral(
                            stringLiteral, this.currentElement != null);
                }
                break;
            case 63:
                newElement = null;
                if (!this.ignoreNextOpeningBrace) {
                    newElement =
                            this.currentElement.updateOnOpeningBrace(
                                    this.scanner.startPosition - 1,
                                    this.scanner.currentPosition - 1);
                }

                this.lastCheckPoint = this.scanner.currentPosition;
                if (newElement != null) {
                    this.restartRecovery = true;
                    this.currentElement = newElement;
                }
        }

        this.ignoreNextOpeningBrace = false;
    }

    protected void reportSyntaxErrors(boolean isDietParse, int oldFirstToken) {
        ReferenceContext var4;
        if ((var4 = this.referenceContext) instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) var4;
            if ((methodDeclaration.bits & 32) != 0) {
                return;
            }
        }

        this.compilationUnit.compilationResult.lineSeparatorPositions = this.scanner.getLineEnds();
        this.scanner.recordLineSeparator = false;
        int start = this.scanner.initialPosition;
        int end =
                this.scanner.eofPosition == Integer.MAX_VALUE
                        ? this.scanner.eofPosition
                        : this.scanner.eofPosition - 1;
        if (isDietParse) {
            TypeDeclaration[] types = this.compilationUnit.types;
            int[][] intervalToSkip = RangeUtil.computeDietRange(types);
            DiagnoseParser diagnoseParser =
                    new DiagnoseParser(
                            this,
                            oldFirstToken,
                            start,
                            end,
                            intervalToSkip[0],
                            intervalToSkip[1],
                            intervalToSkip[2],
                            this.options);
            diagnoseParser.diagnoseParse(false);
            this.reportSyntaxErrorsForSkippedMethod(types);
            this.scanner.resetTo(start, end);
        } else {
            DiagnoseParser diagnoseParser =
                    new DiagnoseParser(this, oldFirstToken, start, end, this.options);
            diagnoseParser.diagnoseParse(this.options.performStatementsRecovery);
        }
    }

    private void reportSyntaxErrorsForSkippedMethod(TypeDeclaration[] types) {
        if (types != null) {
            TypeDeclaration[] var5 = types;
            int var4 = types.length;

            for (int var3 = 0; var3 < var4; ++var3) {
                TypeDeclaration type = var5[var3];
                TypeDeclaration[] memberTypes = type.memberTypes;
                if (memberTypes != null) {
                    this.reportSyntaxErrorsForSkippedMethod(memberTypes);
                }

                AbstractMethodDeclaration[] methods = type.methods;
                int length;
                int j;
                if (methods != null) {
                    AbstractMethodDeclaration[] var11 = methods;
                    j = methods.length;

                    for (length = 0; length < j; ++length) {
                        AbstractMethodDeclaration method = var11[length];
                        if ((method.bits & 32) != 0) {
                            DiagnoseParser diagnoseParser;
                            if (method.isAnnotationMethod()) {
                                diagnoseParser =
                                        new DiagnoseParser(
                                                this,
                                                29,
                                                method.declarationSourceStart,
                                                method.declarationSourceEnd,
                                                this.options);
                                diagnoseParser.diagnoseParse(
                                        this.options.performStatementsRecovery);
                            } else {
                                diagnoseParser =
                                        new DiagnoseParser(
                                                this,
                                                10,
                                                method.declarationSourceStart,
                                                method.declarationSourceEnd,
                                                this.options);
                                diagnoseParser.diagnoseParse(
                                        this.options.performStatementsRecovery);
                            }
                        }
                    }
                }

                FieldDeclaration[] fields = type.fields;
                if (fields != null) {
                    length = fields.length;

                    for (j = 0; j < length; ++j) {
                        FieldDeclaration var16;
                        if ((var16 = fields[j]) instanceof Initializer) {
                            Initializer initializer = (Initializer) var16;
                            if ((initializer.bits & 32) != 0) {
                                DiagnoseParser diagnoseParser =
                                        new DiagnoseParser(
                                                this,
                                                14,
                                                initializer.declarationSourceStart,
                                                initializer.declarationSourceEnd,
                                                this.options);
                                diagnoseParser.diagnoseParse(
                                        this.options.performStatementsRecovery);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void resetModifiers() {
        this.modifiers = 0;
        this.modifiersSourceStart = -1;
        this.annotationAsModifierSourceStart = -1;
        this.scanner.commentPtr = -1;
    }

    protected void resetStacks() {
        this.astPtr = -1;
        this.astLengthPtr = -1;
        this.expressionPtr = -1;
        this.expressionLengthPtr = -1;
        this.typeAnnotationLengthPtr = -1;
        this.typeAnnotationPtr = -1;
        this.identifierPtr = -1;
        this.identifierLengthPtr = -1;
        this.intPtr = -1;
        this.nestedMethod[this.nestedType = 0] = 0;
        this.variablesCounter[this.nestedType] = 0;
        this.switchNestingLevel = 0;
        this.dimensions = 0;
        this.realBlockStack[this.realBlockPtr = 0] = 0;
        this.recoveredStaticInitializerStart = 0;
        this.listLength = 0;
        this.listTypeParameterLength = 0;
        this.genericsIdentifiersLengthPtr = -1;
        this.genericsLengthPtr = -1;
        this.genericsPtr = -1;
        this.valueLambdaNestDepth = -1;
        this.recordNestedMethodLevels = new HashMap();
    }

    protected int resumeAfterRecovery() {
        if (!this.methodRecoveryActivated && !this.statementRecoveryActivated) {
            this.resetStacks();
            this.resetModifiers();
            if (!this.moveRecoveryCheckpoint()) {
                return 0;
            } else if (this.referenceContext instanceof CompilationUnitDeclaration) {
                this.goForHeaders();
                this.diet = true;
                this.dietInt = 0;
                return 1;
            } else {
                return 0;
            }
        } else if (!this.statementRecoveryActivated) {
            this.resetStacks();
            this.resetModifiers();
            if (!this.moveRecoveryCheckpoint()) {
                return 0;
            } else {
                this.goForHeaders();
                return 1;
            }
        } else {
            return 0;
        }
    }

    protected int resumeOnSyntaxError() {
        if (this.haltOnSyntaxError) {
            return 0;
        } else {
            if (this.currentElement == null) {
                this.javadoc = null;
                if (this.statementRecoveryActivated) {
                    return 0;
                }

                this.currentElement = this.buildInitialRecoveryState();
            }

            if (this.currentElement == null) {
                return 0;
            } else {
                if (this.restartRecovery) {
                    this.restartRecovery = false;
                }

                this.updateRecoveryState();
                if (this.getFirstToken() == 21
                        && this.referenceContext instanceof CompilationUnitDeclaration) {
                    TypeDeclaration typeDeclaration =
                            new TypeDeclaration(this.referenceContext.compilationResult());
                    typeDeclaration.name = Util.EMPTY_STRING.toCharArray();
                    this.currentElement =
                            this.currentElement.add((TypeDeclaration) typeDeclaration, 0);
                }

                if (this.lastPosistion < this.scanner.currentPosition) {
                    this.lastPosistion = this.scanner.currentPosition;
                    this.scanner.lastPosition = this.scanner.currentPosition;
                }

                return this.resumeAfterRecovery();
            }
        }
    }

    public void setMethodsFullRecovery(boolean enabled) {
        this.options.performMethodsFullRecovery = enabled;
    }

    public void setStatementsRecovery(boolean enabled) {
        if (enabled) {
            this.options.performMethodsFullRecovery = true;
        }

        this.options.performStatementsRecovery = enabled;
    }

    public String toString() {
        String s = "lastCheckpoint : int = " + String.valueOf(this.lastCheckPoint) + "\n";
        s = s + "identifierStack : char[" + (this.identifierPtr + 1) + "][] = {";

        int i;
        for (i = 0; i <= this.identifierPtr; ++i) {
            s = s + "\"" + String.valueOf(this.identifierStack[i]) + "\",";
        }

        s = s + "}\n";
        s = s + "identifierLengthStack : int[" + (this.identifierLengthPtr + 1) + "] = {";

        for (i = 0; i <= this.identifierLengthPtr; ++i) {
            s = s + this.identifierLengthStack[i] + ",";
        }

        s = s + "}\n";
        s = s + "astLengthStack : int[" + (this.astLengthPtr + 1) + "] = {";

        for (i = 0; i <= this.astLengthPtr; ++i) {
            s = s + this.astLengthStack[i] + ",";
        }

        s = s + "}\n";
        s = s + "astPtr : int = " + String.valueOf(this.astPtr) + "\n";
        s = s + "intStack : int[" + (this.intPtr + 1) + "] = {";

        for (i = 0; i <= this.intPtr; ++i) {
            s = s + this.intStack[i] + ",";
        }

        s = s + "}\n";
        s = s + "expressionLengthStack : int[" + (this.expressionLengthPtr + 1) + "] = {";

        for (i = 0; i <= this.expressionLengthPtr; ++i) {
            s = s + this.expressionLengthStack[i] + ",";
        }

        s = s + "}\n";
        s = s + "expressionPtr : int = " + String.valueOf(this.expressionPtr) + "\n";
        s =
                s
                        + "genericsIdentifiersLengthStack : int["
                        + (this.genericsIdentifiersLengthPtr + 1)
                        + "] = {";

        for (i = 0; i <= this.genericsIdentifiersLengthPtr; ++i) {
            s = s + this.genericsIdentifiersLengthStack[i] + ",";
        }

        s = s + "}\n";
        s = s + "genericsLengthStack : int[" + (this.genericsLengthPtr + 1) + "] = {";

        for (i = 0; i <= this.genericsLengthPtr; ++i) {
            s = s + this.genericsLengthStack[i] + ",";
        }

        s = s + "}\n";
        s = s + "genericsPtr : int = " + String.valueOf(this.genericsPtr) + "\n";
        s = s + "\n\n\n----------------Scanner--------------\n" + this.scanner.toString();
        return s;
    }

    protected void updateRecoveryState() {
        this.currentElement.updateFromParserState();
        this.recoveryTokenCheck();
    }

    protected void updateSourceDeclarationParts(int variableDeclaratorsCounter) {
        int endTypeDeclarationPosition =
                -1 + this.astStack[this.astPtr - variableDeclaratorsCounter + 1].sourceStart;

        FieldDeclaration field;
        for (int i = 0; i < variableDeclaratorsCounter - 1; ++i) {
            field = (FieldDeclaration) this.astStack[this.astPtr - i - 1];
            field.endPart1Position = endTypeDeclarationPosition;
            field.endPart2Position = -1 + this.astStack[this.astPtr - i].sourceStart;
        }

        (field = (FieldDeclaration) this.astStack[this.astPtr]).endPart1Position =
                endTypeDeclarationPosition;
        field.endPart2Position = field.declarationSourceEnd;
    }

    protected void updateSourcePosition(Expression exp) {
        exp.sourceEnd = this.intStack[this.intPtr--];
        exp.sourceStart = this.intStack[this.intPtr--];
        if (exp instanceof FunctionalExpression) {
            FunctionalExpression functionalExp = (FunctionalExpression) exp;
            this.stashTextualRepresentation(functionalExp);
        }
    }

    public void copyState(Parser from) {
        this.stateStackTop = from.stateStackTop;
        this.unstackedAct = from.unstackedAct;
        this.identifierPtr = from.identifierPtr;
        this.identifierLengthPtr = from.identifierLengthPtr;
        this.astPtr = from.astPtr;
        this.astLengthPtr = from.astLengthPtr;
        this.expressionPtr = from.expressionPtr;
        this.expressionLengthPtr = from.expressionLengthPtr;
        this.genericsPtr = from.genericsPtr;
        this.genericsLengthPtr = from.genericsLengthPtr;
        this.genericsIdentifiersLengthPtr = from.genericsIdentifiersLengthPtr;
        this.typeAnnotationPtr = from.typeAnnotationPtr;
        this.typeAnnotationLengthPtr = from.typeAnnotationLengthPtr;
        this.intPtr = from.intPtr;
        this.nestedType = from.nestedType;
        this.switchNestingLevel = from.switchNestingLevel;
        this.realBlockPtr = from.realBlockPtr;
        this.valueLambdaNestDepth = from.valueLambdaNestDepth;
        int length;
        System.arraycopy(
                from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
        System.arraycopy(
                from.identifierStack,
                0,
                this.identifierStack = new char[length = from.identifierStack.length][],
                0,
                length);
        System.arraycopy(
                from.identifierLengthStack,
                0,
                this.identifierLengthStack = new int[length = from.identifierLengthStack.length],
                0,
                length);
        System.arraycopy(
                from.identifierPositionStack,
                0,
                this.identifierPositionStack =
                        new long[length = from.identifierPositionStack.length],
                0,
                length);
        System.arraycopy(
                from.astStack,
                0,
                this.astStack = new ASTNode[length = from.astStack.length],
                0,
                length);
        System.arraycopy(
                from.astLengthStack,
                0,
                this.astLengthStack = new int[length = from.astLengthStack.length],
                0,
                length);
        System.arraycopy(
                from.expressionStack,
                0,
                this.expressionStack = new Expression[length = from.expressionStack.length],
                0,
                length);
        System.arraycopy(
                from.expressionLengthStack,
                0,
                this.expressionLengthStack = new int[length = from.expressionLengthStack.length],
                0,
                length);
        System.arraycopy(
                from.genericsStack,
                0,
                this.genericsStack = new ASTNode[length = from.genericsStack.length],
                0,
                length);
        System.arraycopy(
                from.genericsLengthStack,
                0,
                this.genericsLengthStack = new int[length = from.genericsLengthStack.length],
                0,
                length);
        System.arraycopy(
                from.genericsIdentifiersLengthStack,
                0,
                this.genericsIdentifiersLengthStack =
                        new int[length = from.genericsIdentifiersLengthStack.length],
                0,
                length);
        System.arraycopy(
                from.typeAnnotationStack,
                0,
                this.typeAnnotationStack = new Annotation[length = from.typeAnnotationStack.length],
                0,
                length);
        System.arraycopy(
                from.typeAnnotationLengthStack,
                0,
                this.typeAnnotationLengthStack =
                        new int[length = from.typeAnnotationLengthStack.length],
                0,
                length);
        System.arraycopy(
                from.intStack,
                0,
                this.intStack = new int[length = from.intStack.length],
                0,
                length);
        System.arraycopy(
                from.nestedMethod,
                0,
                this.nestedMethod = new int[length = from.nestedMethod.length],
                0,
                length);
        System.arraycopy(
                from.realBlockStack,
                0,
                this.realBlockStack = new int[length = from.realBlockStack.length],
                0,
                length);
        System.arraycopy(
                from.stateStackLengthStack,
                0,
                this.stateStackLengthStack = new int[length = from.stateStackLengthStack.length],
                0,
                length);
        System.arraycopy(
                from.variablesCounter,
                0,
                this.variablesCounter = new int[length = from.variablesCounter.length],
                0,
                length);
        System.arraycopy(
                from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
        System.arraycopy(
                from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
        System.arraycopy(
                from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
        this.listLength = from.listLength;
        this.listTypeParameterLength = from.listTypeParameterLength;
        this.dimensions = from.dimensions;
        this.recoveredStaticInitializerStart = from.recoveredStaticInitializerStart;
    }

    public int automatonState() {
        return this.stack[this.stateStackTop];
    }

    public boolean automatonWillShift(int token, int lastAction) {
        if (lastAction == 17648) {
            return false;
        } else {
            int stackTop = this.stateStackTop;
            int stackTopState = this.stack[stackTop];
            int highWaterMark = stackTop;
            if (lastAction <= 926) {
                --stackTop;
                lastAction += 17648;
            }

            while (true) {
                if (lastAction > 17648) {
                    lastAction -= 17648;

                    do {
                        stackTop -= rhs[lastAction] - 1;
                        if (stackTop < highWaterMark) {
                            highWaterMark = stackTop;
                            stackTopState = this.stack[stackTop];
                        }

                        lastAction = ntAction(stackTopState, lhs[lastAction]);
                    } while (lastAction <= 926);
                }

                ++stackTop;
                highWaterMark = stackTop;
                stackTopState = lastAction;
                lastAction = tAction(lastAction, token);
                if (lastAction > 926) {
                    if (lastAction != 17648) {
                        return true;
                    }

                    return false;
                }

                --stackTop;
                lastAction += 17648;
            }
        }
    }

    public boolean automatonWillShift(int token) {
        return this.automatonWillShift(token, this.unstackedAct);
    }

    public boolean isParsingJava14() {
        return this.parsingJava14Plus;
    }

    public boolean isParsingModuleDeclaration() {
        return this.parsingJava9Plus
                && this.compilationUnit != null
                && this.compilationUnit.isModuleInfo();
    }

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$CaseLabelKind() {
        int[] var10000 =
                $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$CaseLabelKind;
        if (var10000 != null) {
            return var10000;
        } else {
            int[] var0 = new int[Parser.CaseLabelKind.values().length];

            try {
                var0[Parser.CaseLabelKind.CASE_DEFAULT.ordinal()] = 2;
            } catch (NoSuchFieldError var3) {
            }

            try {
                var0[Parser.CaseLabelKind.CASE_EXPRESSION.ordinal()] = 1;
            } catch (NoSuchFieldError var2) {
            }

            try {
                var0[Parser.CaseLabelKind.CASE_PATTERN.ordinal()] = 3;
            } catch (NoSuchFieldError var1) {
            }

            $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$CaseLabelKind = var0;
            return var0;
        }
    }

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$LocalTypeKind() {
        int[] var10000 =
                $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$LocalTypeKind;
        if (var10000 != null) {
            return var10000;
        } else {
            int[] var0 = new int[Parser.LocalTypeKind.values().length];

            try {
                var0[Parser.LocalTypeKind.LAMBDA.ordinal()] = 3;
            } catch (NoSuchFieldError var3) {
            }

            try {
                var0[Parser.LocalTypeKind.LOCAL.ordinal()] = 1;
            } catch (NoSuchFieldError var2) {
            }

            try {
                var0[Parser.LocalTypeKind.METHOD_REFERENCE.ordinal()] = 2;
            } catch (NoSuchFieldError var1) {
            }

            $SWITCH_TABLE$org$eclipse$jdt$internal$compiler$parser$Parser$LocalTypeKind = var0;
            return var0;
        }
    }

    protected static enum CaseLabelKind {
        CASE_EXPRESSION,
        CASE_DEFAULT,
        CASE_PATTERN;
    }

    private static enum LocalTypeKind {
        LOCAL,
        METHOD_REFERENCE,
        LAMBDA;
    }

    protected static enum SwitchRuleKind {
        EXPRESSION,
        BLOCK,
        THROW;
    }
}
