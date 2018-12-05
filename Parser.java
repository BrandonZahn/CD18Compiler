// Student no: c3186200
//     Course: COMP3290

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

// Semantic checks:
// 1 - Variable Names (arrays and variables) must be declared before they are used.
// Variable status is updated in declarations() and decl(), checks are in iden() and varorcall().
//
// 2 - Array size must be known at compile time.
// Done in type().
//
// 3 - Strong typing must exist for real variables, real arrays, boolean expressions,
// and arithmetic operations.
// Done in expr().
//
// 4 - Valid assignment operations.
// Done in asgnstat()
//
// 5 - Actual parameters in a procedure or function call must match the type
// of their respective formal parameter in the procedure definition.
// Done in elist()
//
// 6 - The number of actual parameters in a procedure call must be equal to the number
// of formal parameters in the procedure definition.
// Done in elist()
//
// 7 - A function must have at least one return statement.
// Done in func()
//
// 8 - Variable names must be unique at their particular block level (scoping).
// Done in the symbol table
public class Parser {

	private static final int UNDECLARED_VAR = 1, UNDEF_ARR_SIZE = 2, STRONG_TYPE = 3,
	ASSIGNMENT = 4, FUNC_PARAM_TYPE = 5, FUNC_CALL_PARAMS = 6, FUNC_RETURN = 7, VAR_ALREADY_USED = 8;
	
	private static final int
	TEOF  =  0,
	TCD18 =  1,	TCONS =  2,	TTYPS =  3,	TIS   =  4,	TARRS =  5,	TMAIN =  6,
	TBEGN =  7,	TEND  =  8,	TARAY =  9,	TOF   = 10,	TFUNC = 11,	TVOID = 12,
	TCNST = 13,	TINTG = 14,	TREAL = 15,	TBOOL = 16,	TFOR  = 17,	TREPT = 18,
	TUNTL = 19,	TIFTH = 20,	TELSE = 21,	TINPT = 22,	TPRIN = 23,	TPRLN = 24,
	TRETN = 25,	TNOT  = 26,	TAND  = 27,	TOR   = 28,	TXOR  = 29,	TTRUE = 30,
	TFALS = 31,
	TCOMA = 32,	TLBRK = 33,	TRBRK = 34,	TLPAR = 35,	TRPAR = 36,
	TEQUL = 37,	TPLUS = 38,	TMINS = 39,	TSTAR = 40,	TDIVD = 41,	TPERC = 42,
	TCART = 43,	TLESS = 44,	TGRTR = 45,	TCOLN = 46,	TLEQL = 47,	TGEQL = 48,
	TNEQL = 49,	TEQEQ = 50,	TPLEQ = 51,	TMNEQ = 52,	TSTEQ = 53,	TDVEQ = 54,
	TPCEQ = 55,	TSEMI = 56,	TDOT  = 57,
	TIDEN = 58,	TILIT = 59,	TFLIT = 60,	TSTRG = 61,	TUNDF = 62;

	private static final String TPRINT[] = {
		"TEOF  ",
		"TCD18 ",	"TCONS ",	"TTYPS ",	"TIS   ",	"TARRS ",	"TMAIN ",
		"TBEGN ",	"TEND  ",	"TARAY ",	"TOF   ",	"TFUNC ",	"TVOID ",
		"TCNST ",	"TINTG ",	"TREAL ",	"TBOOL ",	"TFOR  ",	"TREPT ",
		"TUNTL ",	"TIFTH ",	"TELSE ",	"TINPT ",	"TPRIN ",	"TPRLN ",
		"TRETN ",	"TNOT  ",	"TAND  ",	"TOR   ",	"TXOR  ",	"TTRUE ",
		"TFALS ",	"TCOMA ",	"TLBRK ",	"TRBRK ",	"TLPAR ",	"TRPAR ",
		"TEQUL ",	"TPLUS ",	"TMINS ",	"TSTAR ",	"TDIVD ",	"TPERC ",
		"TCART ",	"TLESS ",	"TGRTR ",	"TCOLN ",	"TLEQL ",	"TGEQL ",
		"TNEQL ",	"TEQEQ ",	"TPLEQ ",	"TMNEQ ",	"TSTEQ ",	"TDVEQ ",
		"TPCEQ ",	"TSEMI ",	"TDOT  ",
		"TIDEN ",	"TILIT ",	"TFLIT ",	"TSTRG ",	"TUNDF "
	};

	private static final int NUNDEF = 0,
	NPROG = 1,	 NGLOB = 2,		NILIST = 3,		NINIT = 4,	NFUNCS = 5,
	NMAIN = 6,	 NSDLST = 7,	NTYPEL = 8,		NRTYPE = 9,	NATYPE = 10,
	NFLIST = 11, NSDECL = 12,	NALIST = 13,	NARRD = 14,	NFUND = 15,
	NPLIST = 16, NSIMP = 17,	NARRP = 18,		NARRC = 19,	NDLIST = 20,
	NSTATS = 21, NFOR = 22,		NREPT = 23,		NASGNS = 24,	NIFTH = 25,
	NIFTE = 26,	 NASGN = 27,	NPLEQ = 28,		NMNEQ = 29,		NSTEQ = 30,
	NDVEQ = 31,	 NINPUT = 32,	NPRINT = 33,	NPRLN = 34,		NCALL = 35,
	NRETN = 36,	 NVLIST = 37,	NSIMV = 38,		NARRV = 39,		NEXPL = 40,
	NBOOL = 41,	 NNOT = 42,		NAND = 43,		NOR = 44,	 	 	NXOR = 45,
	NEQL = 46,	 NNEQ = 47,		NGRT = 48,		NLSS = 49,    NLEQ = 50,
	NADD = 51,	 NSUB = 52,		NMUL = 53,		NDIV = 54,	NMOD = 55,
	NPOW = 56,	 NILIT = 57,	NFLIT = 58,		NTRUE = 59,	NFALS = 60,
	NFCALL = 61, NPRLST = 62,	NSTRG = 63,		NGEQ = 64;

	private static final String PRINTNODE[] = {
		"NUNDEF ",
		"NPROG  ",	"NGLOB  ",	"NILIST ",	"NINIT  ",	"NFUNCS ",
		"NMAIN  ",	"NSDLST ",	"NTYPEL ",	"NRTYPE ",	"NATYPE ",
		"NFLIST ",	"NSDECL ",	"NALIST ",	"NARRD  ",	"NFUND  ",
		"NPLIST ",	"NSIMP  ",	"NARRP  ",	"NARRC  ",	"NDLIST ",
		"NSTATS ",	"NFOR   ",	"NREPT  ",	"NASGNS ",	"NIFTH  ",
		"NIFTE  ",	"NASGN  ",	"NPLEQ  ",	"NMNEQ  ",	"NSTEQ  ",
		"NDVEQ  ",	"NINPUT ",	"NPRINT ",	"NPRLN  ",	"NCALL  ",
		"NRETN  ",	"NVLIST ",	"NSIMV  ",	"NARRV  ",	"NEXPL  ",
		"NBOOL  ",	"NNOT   ",	"NAND   ",	"NOR    ",	"NXOR   ",
		"NEQL   ",	"NNEQ   ",	"NGRT   ",	"NLSS   ",	"NLEQ   ",
		"NADD   ",	"NSUB   ",	"NMUL   ",	"NDIV   ",	"NMOD   ",
		"NPOW   ",	"NILIT  ",	"NFLIT  ",	"NTRUE  ",	"NFALS  ",
		"NFCALL ",	"NPRLST ",	"NSTRG  ",	"NGEQ   "
	};

	private SymbolTable symbolTable;
	private OutputController outputController;
	private ArrayList<Token> tokenStream;
	private String scope;
	private int outputLineLength;
	private TreeNode syntaxTree;
	private Token next;
	private Stack<String> funcCallParams;
	private Queue<Symbol> symbols;
	private int index;
	private Stack<Integer> funcCallAddCount;

	public Parser(ArrayList<Token> tokenStream, OutputController outputController, SymbolTable symbolTable) {
		syntaxTree = null;
		this.tokenStream = tokenStream;
		this.outputController = outputController;
		this.symbolTable = symbolTable;
		index = 0;
		next = tokenStream.get(0);
		scope = "";
		funcCallParams = new Stack<String>();

		symbols = new LinkedList<Symbol>();
		funcCallAddCount = new Stack<Integer>();
	}

	public TreeNode BuildSyntaxTree() {
		syntaxTree = program();
		return syntaxTree;
	}

	private TreeNode program() {
		TreeNode progNode = new TreeNode(NPROG);
		if (nextIs(TCD18)) {
			progNode.SetSymbolType(next);

			advance();
			if (nextIs(TIDEN)) {
				progNode.SetSymbolValue(next);
				
				advance();
				progNode.SetLeft(globals());
				progNode.SetMiddle(funcs());
				progNode.SetRight(mainbody());
				return progNode;
			}
			else
				return invalid(progNode);
		}
		else
			return invalid(progNode);
	}

	private TreeNode globals() {
		TreeNode globNode = null;
		if (nextIs(TCONS) || nextIs(TTYPS) || nextIs(TARRS)) {
			globNode = new TreeNode(NGLOB);
			scope = "@globals";

			if (nextIs(TCONS)) {
				advance();
				if (nextIs(TIDEN))
					globNode.SetLeft(initlist());
				else
					return invalid(globNode);

				if (nextIs(TTYPS)) {
					advance();
					if (nextIs(TIDEN))
						globNode.SetMiddle(typelist());
					else
						return invalid(globNode);
				}

				if (nextIs(TARRS)) {
					advance();
					if (nextIs(TIDEN))
						globNode.SetRight(arrdecls());
					else
						return invalid(globNode);
				}
			}
			else if (nextIs(TTYPS)) {
				advance();
				if (nextIs(TIDEN))
					globNode.SetMiddle(typelist());
				else
					return invalid(globNode);

				if (nextIs(TARRS)) {
					advance();
					if (nextIs(TIDEN))
						globNode.SetRight(arrdecls());
					else
						return invalid(globNode);
				}
			}
			else {
				advance();
				if (nextIs(TIDEN))
					globNode.SetRight(arrdecls());
				else
					return invalid(globNode);
			}
		}
		return globNode;
	}

	private TreeNode funcs() {
		TreeNode funcsNode = null;
		if (nextIs(TFUNC)) {
			funcsNode = new TreeNode(NFUNCS, func());

			if (nextIs(TMAIN))
				return funcsNode;
			else if (nextIs(TFUNC)) {
				funcsNode.SetRight(funcs());
				return funcsNode;
			}
			else
				return funcsNode;
		}
		else
			return funcsNode;
	}

	private TreeNode func() {
		TreeNode funcNode = new TreeNode(NFUND);
		if (nextIs(TFUNC)) {
			advance();
			if (nextIs(TIDEN)) {
				funcNode.SetSymbolValue(next);
				// Add a new scope to the ST. Also, create a funcSymbol.
				if (!symbolTable.ScopeExists(funcNode.SymbolValue())) {
					symbolTable.AddScope(funcNode.SymbolValue());
					scope = funcNode.SymbolValue();
					symbols.clear();
					Symbol s = new Symbol(next);
					s.SetValue(next.Value());
					s.SetType("@func");
					symbols.add(s);
					funcNode.SetSymbol(s);
				}
				else {
					semanticError(funcNode, VAR_ALREADY_USED);
					return funcNode;
				}

				advance();
				if (nextIs(TLPAR)) {
					advance();
					funcNode.SetLeft(plist());

					if (nextIs(TRPAR)) {
						advance();
						if (nextIs(TCOLN)) {
							advance();
							
							Token returnType = rtype();
							String returnTypeString = "";
							if (returnType != null) {
								if (returnType.Type() == TREAL)
									returnTypeString = TPRINT[TFLIT];
								else if (returnType.Type() == TINTG)
									returnTypeString = TPRINT[TILIT];
								else if (returnType.Type() == TBOOL)
									returnTypeString = TPRINT[TBOOL];
								else
									returnTypeString = TPRINT[returnType.Type()];

								funcNode.SetSymbolType(returnType);
							}
							else
								return invalid(funcNode);
							
							Symbol funcSymbol = symbols.poll();
							for (Symbol attribute : symbols) {
								attribute.SetBase(2);
								funcSymbol.AddAttribute(attribute);
							}
							symbols.clear();
							symbolTable.AddFuncSymbol(scope, funcSymbol);
							
							funcNode.SetMiddle(locals());

							if (nextIs(TBEGN)) {
								advance();
								funcNode.SetRight(stats());

								if (nextIs(TEND)) {
									if (symbolTable.FuncReturnExists(scope)) {
										String returnedType = symbolTable.FuncReturns(scope);
										if (returnedType.equals(returnTypeString))
											advance();
										else
											semanticError(funcNode, FUNC_RETURN);
									}
									else
										semanticError(funcNode, FUNC_RETURN);
									
									return funcNode;
								}
								else
									return invalid(funcNode);
							}
							else
								return invalid(funcNode);
						}
						else
							return invalid(funcNode);
					}
					else
						return invalid(funcNode);
				}
				else
					return invalid(funcNode);
			}
			else
				return invalid(funcNode);
		}
		else
			return invalid(funcNode);
	}

	private TreeNode plist() {
		TreeNode plistNode = null;
		if (!nextIs(TRPAR))
			plistNode = params();
		return plistNode;
	}

	private TreeNode params() {
		TreeNode left = param();
		
		if (nextIs(TRPAR))
			return left;
		else if (nextIs(TCOMA)) {
			TreeNode paramsNode = new TreeNode(NPLIST, left);
			advance();
			paramsNode.SetRight(params());
			return paramsNode;
		}
		else
			return left;
	}

	private TreeNode param() {
		TreeNode idNode = new TreeNode(NUNDEF);
		if (nextIs(TCNST)) {
			advance();
			return new TreeNode(NARRC, arrdecl());
		}
		else if (nextIs(TIDEN)) {
			idNode.SetSymbolValue(next);
			if (symbolTable.SymbolExists(scope, next.Value())) {
				semanticError(idNode, VAR_ALREADY_USED);
				return idNode;
			}

			Symbol s = new Symbol(next);
			idNode.SetSymbol(s);
			s.SetValue(next.Value());
			
			advance();
			if (nextIs(TCOLN)) {
				advance();

				if (nextIs(TINTG)) {
					idNode.SetValue(NSDECL);
					idNode.SetSymbolType(TPRINT[TILIT]);
					s.SetType(TPRINT[TILIT]);
					symbols.add(s);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();

					return new TreeNode(NSIMP, idNode);
				}
				else if (nextIs(TREAL)) {
					idNode.SetValue(NSDECL);
					idNode.SetSymbolType(TPRINT[TFLIT]);
					s.SetType(TPRINT[TFLIT]);
					symbols.add(s);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();

					return new TreeNode(NSIMP, idNode);
				}
				else if (nextIs(TBOOL)) {
					idNode.SetValue(NSDECL);
					idNode.SetSymbolType(TPRINT[TBOOL]);
					s.SetType(TPRINT[TBOOL]);
					symbols.add(s);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();

					return new TreeNode(NSIMP, idNode);
				}
				else if (nextIs(TIDEN)) {
					idNode.SetValue(NARRD);
					idNode.SetSymbolType(next);
					s.SetType(next.Value());
					symbols.add(s);
					if (symbolTable.TypeExists(s.Type())) {
						symbolTable.AddSymbol(scope, s.Value(), s);
						advance();
					}
					else
						semanticError(idNode, UNDECLARED_VAR);

					return new TreeNode(NARRP, idNode);
				}
				else
					return invalid(idNode);
			}
			else
				return invalid(idNode);
		}
		else
			return invalid(idNode);
	}

	private Token rtype() {
		Token token = null;
		if (nextIs(TINTG) || nextIs(TREAL) || nextIs(TBOOL) || nextIs(TVOID)) {
			token = next;
			advance();
		}
		return token;
	}

	private TreeNode locals() {
		TreeNode localNode = null;
		
		if (nextIs(TBEGN))
			return localNode;
		else if (nextIs(TIDEN)) {
			return decl();
		}
		else
			return localNode;
	}

	private TreeNode decl() {
		TreeNode left = declarations();
		
		if (nextIs(TBEGN))
			return left;
		else if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NDLIST, left, decl());
		}
		else
			return invalid(left);
	}

	private TreeNode declarations() {
		TreeNode declarationNode = new TreeNode(NSDECL);
		if (nextIs(TIDEN)) {
			declarationNode.SetSymbolValue(next);
			if (symbolTable.SymbolExists(scope, next.Value())) {
				semanticError(declarationNode, VAR_ALREADY_USED);
				return declarationNode;
			}

			Symbol s = new Symbol(next);
			declarationNode.SetSymbol(s);
			s.SetValue(next.Value());

			advance();
			if (nextIs(TCOLN)) {
				advance();
				if (nextIs(TINTG)) {
					declarationNode.SetSymbolType(TPRINT[TILIT]);
					s.SetType(TPRINT[TILIT]);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();
					return declarationNode;
				}
				else if (nextIs(TREAL)) {
					declarationNode.SetSymbolType(TPRINT[TFLIT]);
					s.SetType(TPRINT[TFLIT]);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();
					return declarationNode;
				}
				else if (nextIs(TBOOL)) {
					declarationNode.SetSymbolType(TPRINT[TBOOL]);
					s.SetType(TPRINT[TBOOL]);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();
					return declarationNode;
				}
				else if (nextIs(TIDEN)) {
					declarationNode.SetValue(NARRD);
					declarationNode.SetSymbolType(next);
					
					if (symbolTable.TypeExists(next.Value())) {
						s.SetType(next.Value());
						declarationNode.SetSymbolType(s.Type());
						symbols.add(s);
						symbolTable.AddSymbol(scope, s.Value(), s);
						advance();
					}
					else
						semanticError(declarationNode, UNDECLARED_VAR);
					
					return declarationNode;
				}
				else
					return invalid(declarationNode);
			}
			else
				return invalid(declarationNode);
		}
		else
			return invalid(declarationNode);
	}

	private TreeNode stats() {
		TreeNode left = null;
		if (nextIs(TFOR) || nextIs(TIFTH))
			left = strstat();
		else {
			left = stat();
			
			if (nextIs(TSEMI))
				advance();
			else
				invalid(left);
		}

		if (nextIs(TEND) || nextIs(TUNTL) || nextIs(TELSE) || nextIs(TEOF))
			return left;
		else
			return new TreeNode(NSTATS, left, stats());
	}

	private TreeNode strstat() {
		if (nextIs(TFOR))
			return forstat();
		else
			return ifstat();
	}
	
	private TreeNode forstat() {
		TreeNode forstatNode = new TreeNode(NFOR);

		if (nextIs(TFOR)) {
			advance();

			if (nextIs(TLPAR)) {
				advance();

				if (nextIs(TIDEN))
					forstatNode.SetLeft(alist());
				
				if (nextIs(TSEMI)) {
					advance();
					
					forstatNode.SetMiddle(bool());
					if (!boolType(forstatNode.Middle())) {
						semanticError(forstatNode, STRONG_TYPE);
						return forstatNode;
					}
					if (nextIs(TRPAR)) {
						advance();

						forstatNode.SetRight(stats());

						if (nextIs(TEND)) {
							advance();

							return forstatNode;
						}
						else
							return invalid(forstatNode);
					}
					else
						return invalid(forstatNode);
				}
				else
					return invalid(forstatNode);
			}
			else
				return invalid(forstatNode);
		}
		else
			return invalid(forstatNode);
	}

	private TreeNode ifstat() {
		TreeNode ifstatNode = new TreeNode(NIFTH);
		if (nextIs(TIFTH)) {
			advance();

			if (nextIs(TLPAR)) {
				advance();

				ifstatNode.SetLeft(bool());
				if (!boolType(ifstatNode.Left())) {
					if (!expressionType(ifstatNode.Left()).equals(TPRINT[TBOOL])) {
						semanticError(ifstatNode, STRONG_TYPE);
						return ifstatNode;
					}
				}
				if (nextIs(TRPAR)) {
					advance();

					TreeNode statsNode = stats();

					if (nextIs(TEND)) {
						advance();
						ifstatNode.SetValue(NIFTH);
						ifstatNode.SetRight(statsNode);

						return ifstatNode;
					}
					else if (nextIs(TELSE)) {
						advance();
						ifstatNode.SetValue(NIFTE);
						ifstatNode.SetMiddle(statsNode);
						ifstatNode.SetRight(stats());

						if (nextIs(TEND)) {
							advance();

							return ifstatNode;
						}
						else
							return invalid(ifstatNode);
					}
					else
						return invalid(ifstatNode);
				}
				else
					return invalid(ifstatNode);
			}
			else
				return invalid(ifstatNode);
		}
		else
			return invalid(ifstatNode);
	}

	private TreeNode stat() {
		if (nextIs(TREPT))
			return repstat();
		else if (nextIs(TIDEN))
			return asgnstatorcallstat();
		else if (nextIs(TINPT) || nextIs(TPRIN) || nextIs(TPRLN))
			return iostat();
		else
			return returnstat();
	}

	private TreeNode repstat() {
		TreeNode repstatNode = new TreeNode(NREPT);
		if (nextIs(TREPT)) {
			advance();


			if (nextIs(TLPAR)) {
				advance();

				if (nextIs(TIDEN))
					repstatNode.SetLeft(alist());

				if (nextIs(TRPAR)) {
					advance();

					repstatNode.SetMiddle(stats());

					if (nextIs(TUNTL)) {
						advance();

						repstatNode.SetRight(bool());
						if (!boolType(repstatNode.Right())) {
							if (!expressionType(repstatNode.Right()).equals(TPRINT[TBOOL])) {
								semanticError(repstatNode, STRONG_TYPE);
							}
						}
						return repstatNode;
					}
					else
						return invalid(repstatNode);
				}
				else
					return invalid(repstatNode);
			}
			else
				return invalid(repstatNode);
		}
		else
			return invalid(repstatNode);
	}

	private TreeNode asgnstatorcallstat() {
		TreeNode varorstatNode = new TreeNode(NASGN);
		if (nextIs(TIDEN)) {
			if (!symbolTable.SymbolExists(scope, next.Value()) && !symbolTable.ScopeExists(next.Value())) {
				semanticError(varorstatNode, UNDECLARED_VAR);
				return varorstatNode;
			}
			TreeNode varorcallNode = varorcall();
			varorstatNode.SetLeft(varorcallNode);

			if (varorcallNode.Value() == NCALL)
				return varorcallNode;
			else {
				if (nextIs(TEQUL) || nextIs(TPLEQ) || nextIs(TMNEQ) ||
					nextIs(TSTEQ) || nextIs(TDVEQ))
				{
					int nodeValue = NUNDEF;
					if (nextIs(TEQUL))
						nodeValue = NASGN;
					else if (nextIs(TPLEQ))
						nodeValue = NPLEQ;
					else if (nextIs(TMNEQ))
						nodeValue = NMNEQ;
					else if (nextIs(TSTEQ))
						nodeValue = NSTEQ;
					else
						nodeValue = NDVEQ;

					advance();
					varorstatNode.SetValue(nodeValue);
					varorstatNode.SetRight(bool());
					
					String lhsType = expressionType(varorcallNode);
					String rhsType = TPRINT[TBOOL];
					if (!boolType(varorstatNode.Right())) {
						rhsType = expressionType(varorstatNode.Right());
					}
					
					if (!lhsType.equals(rhsType)) {
						semanticError(varorstatNode, ASSIGNMENT);
						return varorstatNode;
					}
					return varorstatNode;
				}
				else
					return invalid(varorstatNode);
			}
			
		}
		else
			return invalid(new TreeNode(NASGN));
	}

	// Implements semantic check for declared variables
	// Point 1. Adds a reference of a variable to the STMap
	private TreeNode varorcall() {
		TreeNode varorcallNode = new TreeNode(NSIMV);
		if (nextIs(TIDEN)) {
			varorcallNode.SetSymbolValue(next);
			if (!symbolTable.SymbolExists(scope, next.Value()) && !symbolTable.ScopeExists(next.Value())) {
				semanticError(varorcallNode, UNDECLARED_VAR);
				return varorcallNode;
			}
			Symbol reference = new Symbol(next);
			varorcallNode.SetSymbol(reference);
			reference.SetValue(next.Value());

			if (!symbolTable.ScopeExists(next.Value()))
				varorcallNode.SetSymbolType(symbolTable.RetrieveSymbol(scope, next.Value()).Type());

			if (symbolTable.TypeExists(varorcallNode.SymbolType()))
				varorcallNode.SetValue(NARRV);

			advance();
			if (nextIs(TLPAR)) {
				varorcallNode.SetValue(NCALL);
				advance();

				if (!nextIs(TRPAR)){
					funcCallAddCount.add(funcCallParams.size());
					varorcallNode.SetLeft(elist());
					int validity = validFuncCall(reference.Value());
					if (validity == -1) {
						semanticError(varorcallNode, FUNC_CALL_PARAMS);
						return varorcallNode;
					}
					else if (validity == -2) {
						semanticError(varorcallNode, FUNC_PARAM_TYPE);
						return varorcallNode;
					}
				}
				
				if (nextIs(TRPAR)) {
					advance();
					return varorcallNode;
				}
				else
					return invalid(varorcallNode);
			}
			else if (nextIs(TLBRK)) {
				varorcallNode.SetValue(NARRV);
	
				advance();
				varorcallNode.SetLeft(expr());
				if (!validArrIndex(varorcallNode)) {
					semanticError(varorcallNode, UNDEF_ARR_SIZE);
					return varorcallNode;
				}

				if (nextIs(TRBRK)) {
					advance();
	
					if (nextIs(TDOT)) {
						advance();
	
						if (nextIs(TIDEN)) {
							if (symbolTable.TypeMemberExists(varorcallNode.SymbolType(), next.Value())) {
								TreeNode member = new TreeNode(NSIMV);
								member.SetSymbolValue(next);
								member.SetSymbolType(symbolTable.StructMemberType(varorcallNode.SymbolType(), next.Value()));
								Symbol memberOffset = new Symbol();
								memberOffset.SetValue("@" + symbolTable.TypeMemberIndex(varorcallNode.SymbolType(), next.Value()));
								member.SetSymbol(memberOffset);
								varorcallNode.SetRight(member);
								
								advance();
							}
							else
								semanticError(varorcallNode, UNDECLARED_VAR);
							
							return varorcallNode;
						}
						else
							return invalid(varorcallNode);
					}
					else
						return invalid(varorcallNode);
				}
				else
					return invalid(varorcallNode);
			}
			else
				return varorcallNode;
		}
		else
			return invalid(varorcallNode);
	}

	private TreeNode alist() {
		TreeNode left = asgnstatorcallstat();
		
		if (nextIs(TSEMI) || nextIs(TRPAR))
			return left;
		else if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NASGNS, left, alist());
		}
		return left;
	}

	private TreeNode iostat() {
		if (nextIs(TINPT)) {
			advance();
			return new TreeNode(NINPUT, vlist());
		}
		else if (nextIs(TPRIN)) {
			advance();
			return new TreeNode(NPRINT, prlist());
		}
		else if (nextIs(TPRLN)) {
			advance();
			return new TreeNode(NPRLN, prlist());
		}
		else
			return invalid(new TreeNode(NINPUT));
	}

	private TreeNode prlist() {
		TreeNode left = printitem();
		
		if (nextIs(TSEMI))
			return left;
		else if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NPRLST, left, prlist());
		}
		return left;
	}

	private TreeNode printitem() {
		if (nextIs(TSTRG)) {
			TreeNode strgNode = new TreeNode(NSTRG);
			strgNode.SetSymbolValue(next);
			strgNode.SetSymbolType(next);
			Symbol s = new Symbol(next);
			strgNode.SetSymbol(s);
			s.SetValue(next.Value());
			s.SetType(TPRINT[TSTRG]);
			symbolTable.AddGlobalSymbol(s.Value(), s);
			symbolTable.AddConstant(s.Value(), s);
			advance();
			return strgNode;
		}
		else {
			TreeNode exprPNode = expr();
			if (expressionType(exprPNode).equals("@INV"))
				semanticError(exprPNode, STRONG_TYPE);

			return exprPNode;
		}
	}

	private TreeNode returnstat() {
		if (nextIs(TRETN)) {
			advance();

			if (nextIs(TSEMI)) {
				if (!scope.equals("@main")) {
					symbolTable.SetFuncReturns(scope, TPRINT[TVOID]);
				}
				return new TreeNode(NRETN);
			}
			else {
				TreeNode returnExprNode = new TreeNode(NRETN, expr());
				String returnType = expressionType(returnExprNode);
				if (returnType.equals("@INV"))
					semanticError(returnExprNode, STRONG_TYPE);
				else
					symbolTable.SetFuncReturns(scope, returnType);
				return returnExprNode;
			}
		}
		else
			return invalid(new TreeNode(NRETN));
	}

	private TreeNode vlist() {
		TreeNode left = varorcall();
		
		if (nextIs(TSEMI))
			return left;
		else if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NVLIST, left, vlist());
		}
		return left;
	}

	private TreeNode mainbody() {
		TreeNode mainNode = new TreeNode(NMAIN);
		if (nextIs(TMAIN)) {
			scope = "@main";
			advance();

			mainNode.SetLeft(slist());

			if (nextIs(TBEGN)) {
				advance();

				mainNode.SetRight(stats());

				if (nextIs(TEND)) {
					advance();

					if (nextIs(TCD18)) {
						advance();

						if (nextIs(TIDEN)) {
							mainNode.SetSymbolValue(next);
							advance();

							if (nextIs(TEOF)) {
								return mainNode;
							}
							else
								return invalid(mainNode);
						}
						else
							return invalid(mainNode);
					}
					else
						return invalid(mainNode);
				}
				else
					return invalid(mainNode);
			}
			else
				return invalid(mainNode);
		}
		else
			return invalid(mainNode);
	}

	private TreeNode slist() {
		TreeNode left = sdecl();
		
		if (nextIs(TBEGN))
			return left;
		else if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NSDLST, left, slist());
		}
		return left;
	}

	private TreeNode initlist() {
		TreeNode left = init();
		
		if (nextIs(TTYPS) || nextIs(TARRS) || nextIs(TFUNC) || nextIs(TMAIN))
			return left;
		else if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NILIST, left, initlist());
		}
		return left;
	}
	
	private TreeNode init() {
		TreeNode initNode = new TreeNode(NINIT);
		if (nextIs(TIDEN)) {
			initNode.SetSymbolValue(next);
			if (symbolTable.SymbolExists(scope, next.Value())) {
				semanticError(initNode, VAR_ALREADY_USED);
				return initNode;
			}

			Symbol s = new Symbol(next);
			initNode.SetSymbol(s);
			s.SetValue(next.Value());

			advance();
			if (nextIs(TEQUL)) {
				advance();
				initNode.SetLeft(expr());
				String type = expressionType(initNode);
				if (type.equals("@INV")) {
					semanticError(initNode, STRONG_TYPE);
					return initNode;
				}
				foldExpression(initNode.Left());
				s.SetType(type);
			}
			else
				return invalid(initNode);
			
			initNode.SetSymbolType(s.Type());
			s.AddAttribute(initNode.Left().Symbol());
			symbolTable.AddSymbol(scope, s.Value(), s);
			symbolTable.AddConstant(s.Value(), s);
			return initNode;
		}
		else
			return invalid(initNode);
	}

	private TreeNode typelist() {
		TreeNode left = type();

		if (nextIs(TARRS) || nextIs(TFUNC) || nextIs(TMAIN))
			return left;
		else if (nextIs(TIDEN))
			return new TreeNode(NTYPEL, left, typelist());
		return left;
	}

	private TreeNode type() {
		TreeNode typeNode = new TreeNode(NATYPE);
		if (nextIs(TIDEN)) {
			typeNode.SetSymbolValue(next);
			if (symbolTable.StructExists(next.Value()) || symbolTable.TypeExists(next.Value())) {
				semanticError(typeNode, VAR_ALREADY_USED);
				return typeNode;
			}

			Symbol s = new Symbol(next);
			typeNode.SetSymbol(s);
			s.SetValue(next.Value());

			advance();
			if (nextIs(TIS)) {
				advance();
				if (nextIs(TARAY)) {
					typeNode.SetValue(NATYPE);
					advance();
					if (nextIs(TLBRK)) {
						advance();
						typeNode.SetLeft(expr());
						if (!validArrIndex(typeNode)) {
							semanticError(typeNode, UNDEF_ARR_SIZE);
							return typeNode;
						}
						foldExpression(typeNode.Left());

						if (nextIs(TRBRK)) {
							advance();
							if (nextIs(TOF)) {
								advance();
								if (nextIs(TIDEN)) {
									if (symbolTable.StructExists(next.Value())) {
										typeNode.SetSymbolType(next.Value());
										s.SetType(next.Value());
										if (!symbolTable.ConstantExists(typeNode.Left().Symbol().Value()))
											s.AddAttribute(typeNode.Left().Symbol());
										else
											s.AddAttribute(symbolTable.RetrieveSymbol(scope, typeNode.Left().Symbol().Value()));

										symbolTable.AddType(s.Value(), s);

										int typeSize = symbolTable.TypeSize(s.Value());
										Symbol newSize = new Symbol();
										newSize.SetValue("@" + typeSize);
										newSize.SetType(TPRINT[TILIT]);
										symbolTable.RetrieveSymbol(scope, s.Value()).AddAttribute(newSize);
										if (!symbolTable.SymbolExists(scope, newSize.Value()))
											symbolTable.AddSymbol(scope, newSize.Value(), newSize);
										
										if (typeSize > 32767)
											symbolTable.AddConstant(newSize.Value(), newSize);

										advance();
									}
									else
										semanticError(typeNode, UNDECLARED_VAR);
									return typeNode;
								}
								else
									return invalid(typeNode);
							}
							else
								return invalid(typeNode);
						}
						else
							return invalid(typeNode);
					}
					else
						return invalid(typeNode);
				}
				else {
					typeNode.SetValue(NRTYPE);
					symbols.clear();
					typeNode.SetLeft(fields());
					typeNode.SetSymbolType(typeNode.SymbolValue());

					int offset = 0;
					for (Symbol member : symbols) {
						member.SetOffset(offset);
						s.AddAttribute(member);
						offset++;
					}
					symbols.clear();
					symbolTable.AddStruct(s.Value(), s);

					if (nextIs(TEND)) {
						advance();
						return typeNode;
					}
					else
						return invalid(typeNode);
				}
			}
			else
				return invalid(typeNode);
		}
		else return invalid(typeNode);
	}

	private TreeNode fields() {
		TreeNode left = sdecl();
		
		if (nextIs(TARRS) || nextIs(TFUNC) || nextIs(TMAIN))
			return left;

		if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NFLIST, left, fields());
		}
		return left;
	}

	private TreeNode sdecl() {
		TreeNode sdeclNode = new TreeNode(NSDECL);
		if (nextIs(TIDEN)) {
			sdeclNode.SetSymbolValue(next);
			if (symbolTable.SymbolExists(scope, next.Value())) {
				semanticError(sdeclNode, VAR_ALREADY_USED);
				return sdeclNode;
			}

			Symbol s = new Symbol(next);
			sdeclNode.SetSymbol(s);
			s.SetValue(next.Value());

			advance();
			if (nextIs(TCOLN)) {
				advance();
				if (nextIs(TINTG)) {
					sdeclNode.SetSymbolType(TPRINT[TILIT]);
					s.SetType(TPRINT[TILIT]);
					symbols.add(s);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();
					return sdeclNode;
				}
				else if (nextIs(TREAL)) {
					sdeclNode.SetSymbolType(TPRINT[TFLIT]);
					s.SetType(TPRINT[TFLIT]);
					symbols.add(s);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();
					return sdeclNode;
				}
				else if (nextIs(TBOOL)) {
					sdeclNode.SetSymbolType(TPRINT[TBOOL]);
					s.SetType(TPRINT[TBOOL]);
					symbols.add(s);
					symbolTable.AddSymbol(scope, s.Value(), s);
					advance();
					return sdeclNode;
				}
				else
					return invalid(sdeclNode);
			}
			else
				return invalid(sdeclNode);
		}
		else
			return invalid(sdeclNode);
	}

	private TreeNode arrdecls() {
		TreeNode left = arrdecl();
		
		if (nextIs(TARRS) || nextIs(TFUNC) || nextIs(TMAIN))
			return left;

		if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NALIST, left, arrdecls());
		}
		return left;
	}

	private TreeNode arrdecl() {
		TreeNode adeclNode = new TreeNode(NARRD);
		if (nextIs(TIDEN)) {
			adeclNode.SetSymbolValue(next);
			if (symbolTable.SymbolExists(scope, next.Value())) {
				semanticError(adeclNode, VAR_ALREADY_USED);
				return adeclNode;
			}

			Symbol s = new Symbol(next);
			adeclNode.SetSymbol(s);
			s.SetValue(next.Value());

			advance();
			if (nextIs(TCOLN)) {
				advance();
				if (nextIs(TIDEN)) {
					if (symbolTable.TypeExists(next.Value())) {
						s.SetType(next.Value());
						adeclNode.SetSymbolType(s.Type());
						symbols.add(s);
						symbolTable.AddSymbol(scope, s.Value(), s);
						advance();
					}
					else
						semanticError(adeclNode, UNDECLARED_VAR);
					return adeclNode;
				}
				else
					return invalid(adeclNode);
			}
			else
				return invalid(adeclNode);
		}
		else
			return invalid(adeclNode);
	}

	private TreeNode bool() {
		return booltail(rel());
	}

	private TreeNode booltail(TreeNode left) {
		if (nextIs(TAND) || nextIs(TOR) || nextIs(TXOR)) {
			int nodeValue;
			if (nextIs(TAND))
				nodeValue = NAND;
			else if (nextIs(TOR))
				nodeValue = NOR;
			else
				nodeValue = NXOR;
			
			TreeNode logopNode = new TreeNode(nodeValue);
			logopNode.SetLeft(left);
			TreeNode boolNode = new TreeNode(NBOOL, logopNode);

			advance();
			logopNode.SetRight(rel());

			return booltail(boolNode);
		}
		else
			return left;
	}

	private TreeNode rel() {
		TreeNode relNode = new TreeNode(NUNDEF);

		if (nextIs(TNOT)) {
			advance();
			relNode.SetValue(NNOT);
			relNode.SetLeft(rel());
			return relNode;
		}
		else {
			TreeNode exprNode = expr();
			String lhsType = expressionType(exprNode);
			if (lhsType.equals("@INV"))
				semanticError(exprNode, STRONG_TYPE);

			if (nextIs(TEQEQ) || nextIs(TNEQL) || nextIs(TGRTR) ||
				nextIs(TGEQL) || nextIs(TLESS) || nextIs(TLEQL))
			{
				int nodeValue = NUNDEF;
				if (nextIs(TEQEQ))
					nodeValue = NEQL;
				else if (nextIs(TNEQL))
					nodeValue = NNEQ;
				else if (nextIs(TGRTR))
					nodeValue = NGRT;
				else if (nextIs(TGEQL))
					nodeValue = NGEQ;
				else if (nextIs(TLESS))
					nodeValue = NLSS;
				else
					nodeValue = NLEQ;

				relNode.SetValue(nodeValue);
				advance();
				relNode.SetLeft(exprNode);
				relNode.SetRight(expr());
				String rhsType = expressionType(relNode.Right());
				if (rhsType.equals("@INV") || !lhsType.equals(rhsType))
					semanticError(relNode, STRONG_TYPE);

				return relNode;
			}
			else
				return exprNode;
		}
	}

	private TreeNode expr() {
		TreeNode exprNode = exprtail(term());
		return exprNode;
	}

	private TreeNode exprtail(TreeNode left) {
		if (nextIs(TPLUS) || nextIs(TMINS)) {
			int nodeValue;
			if (nextIs(TPLUS))
				nodeValue = NADD;
			else
				nodeValue = NSUB;

			TreeNode exprNode = new TreeNode(nodeValue);
			exprNode.SetLeft(left);

			advance();
			exprNode.SetRight(term());
			return exprtail(exprNode);
		}
		else
			return left;
	}

	private TreeNode term() {
		return termtail(fact());
	}

	private TreeNode termtail(TreeNode left) {
		if (nextIs(TSTAR) || nextIs(TDIVD) || nextIs(TPERC)) {
			int nodeValue;
			if (nextIs(TSTAR))
				nodeValue = NMUL;
			else if (nextIs(TDIVD))
				nodeValue = NDIV;
			else
				nodeValue = NMOD;
			
			TreeNode termNode = new TreeNode(nodeValue);
			termNode.SetLeft(left);

			advance();
			termNode.SetRight(fact());

			return termtail(termNode);
		}
		else
			return left;
	}

	private TreeNode fact() {
		return facttail(exponent());
	}

	private TreeNode facttail(TreeNode left) {
		if (nextIs(TCART)) {
			TreeNode factNode = new TreeNode(NPOW);
			factNode.SetLeft(left);

			advance();
			factNode.SetRight(exponent());

			if (factNode.Right().SymbolType() != TPRINT[TILIT])
				semanticError(factNode, STRONG_TYPE);

			return facttail(factNode);
		}
		else
			return left;
	}

	private TreeNode exponent() {
		if (nextIs(TLPAR)) {
			advance();
			TreeNode boolNode = bool();
			if (nextIs(TRPAR)) {
				advance();
				return boolNode;
			}
			else
				return invalid(boolNode);
		}
		else if (nextIs(TTRUE)) {
			TreeNode trueLit = new TreeNode(NTRUE);
			trueLit.SetSymbolType(TPRINT[TBOOL]);
			trueLit.SetSymbolValue("@true");
			
			Symbol reference = new Symbol(next);
			trueLit.SetSymbol(reference);
			reference.SetValue("@true");
			reference.SetType(TPRINT[TBOOL]);
			if (symbolTable.SymbolExists(scope, "@true"))
				symbolTable.RetrieveSymbol(scope, "@true").AddReference(reference);
			else
				symbolTable.AddGlobalSymbol("@true", reference);
			advance();
			return trueLit;
		}
		else if (nextIs(TFALS)) {
			TreeNode falseLit = new TreeNode(NFALS);
			falseLit.SetSymbolType(TPRINT[TBOOL]);
			falseLit.SetSymbolValue("@false");
			
			Symbol reference = new Symbol(next);
			falseLit.SetSymbol(reference);
			reference.SetValue("@false");
			reference.SetType(TPRINT[TBOOL]);
			if (symbolTable.SymbolExists(scope, "@false"))
				symbolTable.RetrieveSymbol(scope, "@false").AddReference(reference);
			else
				symbolTable.AddGlobalSymbol("@false", reference);
			advance();
			return falseLit;
		}
		else if (nextIs(TILIT))
			return intlit();
		else if (nextIs(TFLIT))
			return reallit();
		else if (nextIs(TIDEN)) {
			return iden();
		}
		else
			return invalid(new TreeNode(NILIT));
	}

	private TreeNode intlit() {
		TreeNode ilitNode = new TreeNode(NILIT);
		ilitNode.SetSymbolValue("@" + next.Value());
		ilitNode.SetSymbolType(TPRINT[TILIT]);
		Symbol reference = new Symbol(next);
		ilitNode.SetSymbol(reference);
		reference.SetValue("@" + next.Value());
		reference.SetType(TPRINT[TILIT]);
		if (symbolTable.SymbolExists(scope, reference.Value()))
			symbolTable.RetrieveSymbol(scope, reference.Value()).AddReference(reference);
		else
			symbolTable.AddGlobalSymbol(reference.Value(), reference);

		if ((Integer.parseInt(next.Value()) > 65535) && !symbolTable.ConstantExists(reference.Value()))
			symbolTable.AddConstant(reference.Value(), reference);
		advance();
		return ilitNode;
	}

	private TreeNode reallit() {
		TreeNode realNode = new TreeNode(NFLIT);
		realNode.SetSymbolValue("@" + next.Value());
		realNode.SetSymbolType(TPRINT[TFLIT]);
		Symbol reference = new Symbol(next);
		realNode.SetSymbol(reference);
		reference.SetValue("@" + next.Value());
		reference.SetType(TPRINT[TFLIT]);
		if (symbolTable.SymbolExists(scope, reference.Value()))
			symbolTable.RetrieveSymbol(scope, reference.Value()).AddReference(reference);
		else
			symbolTable.AddGlobalSymbol(reference.Value(), reference);

		if (!symbolTable.ConstantExists(reference.Value()))
			symbolTable.AddConstant(reference.Value(), reference);
		advance();
		return realNode;
	}
	
	// Implements semantic check for declared variables
	// Point 1
	private TreeNode iden() {
		TreeNode idenNode = new TreeNode(NSIMV);
		if (!symbolTable.SymbolExists(scope, next.Value()) && !symbolTable.ScopeExists(next.Value())) {
			semanticError(idenNode, UNDECLARED_VAR);
			return idenNode;
		}

		if (symbolTable.IsScope(next.Value()))
			idenNode.SetSymbolType(symbolTable.FuncReturns(next.Value()));
		else
			idenNode.SetSymbolType(symbolTable.RetrieveSymbol(scope, next.Value()).Type());

		idenNode.SetSymbolValue(next.Value());
		Symbol reference = new Symbol(next);
		idenNode.SetSymbol(reference);
		reference.SetValue(next.Value());
		reference.SetType(idenNode.SymbolType());

		if (symbolTable.TypeExists(reference.Type()))
			idenNode.SetValue(NARRV);

		if (!symbolTable.TypeExists(reference.Value()) && !symbolTable.IsScope(next.Value()))
			symbolTable.AddReference(scope, reference.Value(), reference);

		advance();
		if (nextIs(TLPAR)) {
			idenNode.SetValue(NFCALL);

			advance();
			if (nextIs(TRPAR)) {
				advance();
				return idenNode;
			}
			else {
				funcCallAddCount.add(funcCallParams.size());
				idenNode.SetLeft(elist());
				int validity = validFuncCall(reference.Value());
				if (validity == -1) {
					semanticError(idenNode, FUNC_CALL_PARAMS);
					return idenNode;
				}
				else if (validity == -2) {
					semanticError(idenNode, FUNC_PARAM_TYPE);
					return idenNode;
				}
				if (nextIs(TRPAR)) {
					advance();
					return idenNode;
				}
				else
					return invalid(idenNode);
			}
		}
		else if (nextIs(TLBRK)) {
			idenNode.SetValue(NARRV);

			advance();
			idenNode.SetLeft(expr());
			if (!validArrIndex(idenNode)) {
				semanticError(idenNode, UNDEF_ARR_SIZE);
				return idenNode;
			}
			if (nextIs(TRBRK)) {
				advance();

				if (nextIs(TDOT)) {
					advance();

					if (nextIs(TIDEN)) {
						if (symbolTable.TypeMemberExists(idenNode.SymbolType(), next.Value())) {
							TreeNode member = new TreeNode(NSIMV);
							member.SetSymbolValue(next);
							member.SetSymbolType(symbolTable.StructMemberType(idenNode.SymbolType(), next.Value()));
							Symbol memberOffset = new Symbol();
							memberOffset.SetValue("@" + symbolTable.TypeMemberIndex(idenNode.SymbolType(), next.Value()));
							member.SetSymbol(memberOffset);
							idenNode.SetRight(member);
							advance();
						}
						else
							semanticError(idenNode, UNDECLARED_VAR);
						return idenNode;
					}
					else
						return invalid(idenNode);
				}
				else
					return invalid(idenNode);
			}
			else
				return invalid(idenNode);
		}
		else
			return idenNode;
	}

	private TreeNode elist() {
		TreeNode left = bool();
		if (boolType(left))
			funcCallParams.add(TPRINT[TBOOL]);
		else
			funcCallParams.add(expressionType(left));

		if (nextIs(TRPAR))
			return left;
		else if (nextIs(TCOMA)) {
			advance();
			return new TreeNode(NEXPL, left, elist());
		}
		return left;
	}

	private int validFuncCall(String scope) {
		Symbol funcDef = symbolTable.RetrieveSymbol(scope, scope);
		ArrayList<Symbol> funcParamDef = funcDef.Attributes();
		int addedParams = funcCallParams.size() - funcCallAddCount.pop();
		if (addedParams != funcParamDef.size()) {
			return -1;
		}
		else {
			if (funcParamDef.size() > 0) {
				for (int i = funcParamDef.size() - 1; i >= 0; i--) {
					String paramType = funcCallParams.pop();
					addedParams--;
					if (!funcParamDef.get(i).Type().equals(paramType)) {
						for (int j = 0; j < addedParams; j++) {
							funcCallParams.pop();
						}
						return -2;
					}
				}
				return 0;
			}
			else {
				return 0;
			}
		}
	}

	private boolean boolType(TreeNode boolNode) {
		Set<Boolean> boolierPresent = new HashSet<Boolean>();

		searchForBoolifiers(boolNode, boolierPresent);
		
		if (boolierPresent.size() > 1)
			return true;
		else 
			return (boolierPresent.iterator().next() == true);
	}

	// Returns the TreeNode that references the highest point of folding
	// that has happened. If exprNode is returned, it means the whole 
	// expression branch was folded.
	private void foldExpression(TreeNode exprNode) {
		int foldCount = 0;
		while (exprNode.Value() != NILIT && exprNode.Value() != NSIMV) {
			traverseExpressionForFolding(exprNode);
			foldCount++;
		}

		if (foldCount > 0 && !symbolTable.SymbolExists(scope, exprNode.Symbol().Value()))
			symbolTable.AddGlobalSymbol(exprNode.Symbol().Value(), exprNode.Symbol());
	}

	private void traverseExpressionForFolding(TreeNode node) {
		if (node.Value() >= 51 && node.Value() <= 56 && (node.Left().Value() < 51 || node.Left().Value() > 56) &&
			(node.Right().Value() < 51 || node.Right().Value() > 56)) {
			
			int lhsValue;
			if (node.Left().Value() == NSIMV)
				lhsValue = Integer.parseInt(symbolTable.RetrieveSymbol(scope, node.Left().SymbolValue()).Attributes().get(0).Value().substring(1));
			else
				lhsValue = Integer.parseInt(node.Left().SymbolValue().substring(1));
			
			int rhsValue;
			if (node.Right().Value() == NSIMV)
				rhsValue = Integer.parseInt(symbolTable.RetrieveSymbol(scope, node.Right().SymbolValue()).Attributes().get(0).Value().substring(1));
			else
				rhsValue = Integer.parseInt(node.Right().SymbolValue().substring(1));
			
			int newValue = 0;

			if (node.Value() == NADD)
				newValue = lhsValue + rhsValue;
			else if (node.Value() == NSUB)
				newValue = lhsValue - rhsValue;
			else if (node.Value() == NMUL)
				newValue = lhsValue * rhsValue;
			else if (node.Value() == NDIV)
				newValue = lhsValue / rhsValue;
			else if (node.Value() == NMOD)
				newValue = lhsValue % rhsValue;
			else
				newValue = (int) Math.pow((double)lhsValue, (double)rhsValue);

			node.SetValue(NILIT);
			node.SetLeft(null);
			node.SetMiddle(null);
			node.SetRight(null);
			Symbol s = new Symbol();
			s.SetValue("@" + Integer.toString(newValue));
			s.SetType(TPRINT[TILIT]);
			node.SetSymbol(s);
			node.SetSymbolValue(s.Value());
			node.SetSymbolType(s.Type());
		}
		else {
			if (node.Left() != null && node.Left().Value() >= 51 && node.Left().Value() <= 56)
				traverseExpressionForFolding(node.Left());
			if (node.Right() != null && node.Right().Value() >= 51 && node.Right().Value() <= 56)
				traverseExpressionForFolding(node.Right());
		}
	}
	
	private void searchForBoolifiers(TreeNode node, Set<Boolean> boolierPresent) {
		int v = node.Value();
		if (v == NBOOL || v == NNOT || v == NEQL || v == NNEQ ||
			v == NGRT  || v == NLEQ || v == NLSS || v == NGEQ) {
			boolierPresent.add(true);
		}
		else
			boolierPresent.add(false);

		if (node.Left() != null)
			searchForBoolifiers(node.Left(), boolierPresent);
		else if (node.Middle() != null)
			searchForBoolifiers(node.Middle(), boolierPresent);
		else if (node.Right() != null)
			searchForBoolifiers(node.Right(), boolierPresent);
	}

	private void advance() {
		if (index != (tokenStream.size() - 1)) {
			next = tokenStream.get(++index);
		}
	}

	private boolean nextIs(int type) {
		return (next.Type() == type);
	}

	private String expressionType(TreeNode exprNode) {
		Set<String> types = new HashSet<String>();

		traverseTreeForTypes(exprNode, types);

		if (types.size() > 1 || types.size() == 0)
			return "@INV";
		else
			return types.iterator().next();
	}

	private boolean validArrIndex(TreeNode arrvNode) {
		Set<String> types = new HashSet<String>();

		traverseTreeForTypes(arrvNode.Left(), types);

		if (types.isEmpty() || types.size() > 1 || !types.iterator().next().equals(TPRINT[TILIT]))
			return false;
		else
			return true;
	}

	private void traverseTreeForTypes(TreeNode node, Set<String> types) {
		if (node.Value() == NARRV && node.Right() != null)
			types.add(node.Right().SymbolType());
		else if (node.Value() == NFCALL)
			types.add(symbolTable.FuncReturns(node.SymbolValue()));
		else if (node.SymbolType() != null) {
			types.add(node.SymbolType());
		}
		else {
			if (node.Value() != NARRV && node.Value() != NCALL && node.Value() != NFCALL && node.Left() != null)
				traverseTreeForTypes(node.Left(), types);
			if (node.Middle() != null)
				traverseTreeForTypes(node.Middle(), types);
			if (node.Value() != NARRV && node.Right() != null)
				traverseTreeForTypes(node.Right(), types);
		}
	}

	private TreeNode invalid(TreeNode errorNode) {
		if (errorNode != null) {
			outputController.ListInvalidNode(errorNode.Value(), next);
			while (!nextIs(TSEMI) && !nextIs(TEOF) && !nextIs(TEND)) {
				advance();
			}
			errorNode.SetValue(NUNDEF);
			if (!symbols.isEmpty()) {
				symbols.clear();
			}
			return errorNode;
		}
		else {
			outputController.ListInvalidNode(NUNDEF, next);
			while (!nextIs(TSEMI) && !nextIs(TEOF) && !nextIs(TEND)) {
				advance();
			}
			if (!symbols.isEmpty()) {
				symbols.clear();
			}
			return new TreeNode(NUNDEF);
		}

	}

	private TreeNode semanticError(TreeNode errorNode, int type) {
		if (errorNode != null) {
			outputController.ListSemanticError(errorNode.Value(), type, next);
			while (!nextIs(TSEMI) && !nextIs(TEOF) && !nextIs(TEND)) {
				advance();
			}
			errorNode.SetValue(NUNDEF);
			if (!symbols.isEmpty()) {
				symbols.clear();
			}
			return errorNode;
		}
		else {
			outputController.ListSemanticError(NUNDEF, type, next);
			while (!nextIs(TSEMI) && !nextIs(TEOF) && !nextIs(TEND)) {
				advance();
			}
			if (!symbols.isEmpty()) {
				symbols.clear();
			}
			return new TreeNode(NUNDEF);
		}
	}

	public void PrintSyntaxTree(PrintStream out) {
		outputLineLength = 0;
		printTree(out, syntaxTree);
	}

	private void printTree(PrintStream out, TreeNode node) {
		String nodeString = "";
		nodeString += PRINTNODE[node.Value()] + " ";
		
		if (node.SymbolValue() != null)
			nodeString += node.SymbolValue();

		//if (node.SymbolType() != null) {
		//	nodeString += node.SymbolType() + " ";

		if (nodeString.length() > 8) {
			int remainder = 8 - (nodeString.length() % 8);
			for (int i = 0; i < remainder; i++) {
				nodeString += " ";
			}
		}

		out.print(nodeString);
		outputLineLength += nodeString.length();
		if (outputLineLength >= 78) {
			out.print("\n");
			outputLineLength = 0;
		}

		if (node.Left() != null)
			printTree(out, node.Left());
		if (node.Middle() != null)
			printTree(out, node.Middle());
		if (node.Right() != null)
			printTree(out, node.Right());
	}
}