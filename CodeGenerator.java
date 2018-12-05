// Student no: c3186200
//     Course: COMP3290

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class CodeGenerator {

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

	private static final int
	IHALT  =  0, INOOP  =  1, ITRAP  =  2, IZERO  =  3, IFALSE =  4, ITRUE  =  5,
	ITYPE  =  7, IITYPE =  8, IFTYPE =  9, IADD   = 11, ISUB   = 12, IMUL   = 13,
	IDIV   = 14, IREM   = 15, IPOW   = 16, ICHS   = 17, IABS   = 18, IGT    = 21, IGE  = 22,
	ILT    = 23, ILE    = 24, IEQ    = 25, INE    = 26, IAND   = 31, IOR    = 32, IXOR = 33,
	INOT   = 34, IBT    = 35, IBF    = 36, IBR    = 37, IL     = 40, ILB    = 41, ILH  = 42,
	IST    = 43, ISTEP  = 51, IALLOC = 52, IARRAY = 53, IINDEX = 54, ISIZE  = 55,
	IDUP   = 56, IREADF = 60, IREADI = 61, IVALPR = 62, ISTRPR = 63, ICHRPR = 64,
	INEWLN = 65, ISPACE = 66, IRVAL  = 70, IRETN  = 71, IJS2   = 72,
	ILVB0  = 80, ILVB1  = 81, ILVB2  = 82, ILAB0  = 90, ILAB1  = 91, ILAB2  = 92;
	
	private SymbolTable symbolTable;
	private TreeNode ASTRoot;
	private String fileName;
	private PrintWriter mod;

	private ArrayList<Instruction> instructions;

	// contains all the int in the read-only. To calculate the operand address:
	// B0 + instructions.size + Symbol offset
	private ArrayList<Symbol> intConstants;

	// contains all the reals in all the program. To calculate the operand address:
	// B0 + instructions.size + intConstants.size + Symbol offset
	private ArrayList<Symbol> realConstants;

	// contains all the strings in the read-only. To calculate the operand address:
	// B0 + instructions.size + intConstants.size + realConstants.size + Symbol offset
	private ArrayList<Symbol> stringConstants;
	private int stringConstSize;
	private String scope; // The current scope for variables
	private int cgpc; // Code-generated Program Counter
	private int sp; // Stack pointer for b1
	private int b2p;
	private int allocounter;
	private String stdOutString;

	public CodeGenerator(String fileName, TreeNode ASTRoot, SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
		this.ASTRoot = ASTRoot;
		this.fileName = fileName;
		
		instructions = new ArrayList<Instruction>();
		intConstants = new ArrayList<Symbol>();
		realConstants = new ArrayList<Symbol>();
		stringConstants = new ArrayList<Symbol>();
		stringConstSize = 0;
		scope = "@globals";
		cgpc = 0; // this will serve as the offset for variables on the stack, IN BYTES
		sp = -1;
		b2p = sp;
		allocounter = 0;
		stdOutString = "";
	}

	public void Generate() {
		createInstructionList();
		createModFile();
	}

	private void createInstructionList() {
		// Fill the list of instructions from the symbol table
		constants();

		// Process the global arrays if present
		if (ASTRoot.Left() != null && ASTRoot.Left().Right() != null)
			alist(ASTRoot.Left().Right());

		// Add the first instruction to jump to main execution point
		Symbol branchToMainAddr = new Symbol();
		branchToMainAddr.SetType("NODE");
		Instruction branchToMain = new Instruction(ILAB0, branchToMainAddr);
		addInstruction(branchToMain);
		addInstruction(new Instruction(IBR));
		
		// Process functions
		if (ASTRoot.Middle() != null)
			funcs(ASTRoot.Middle());
		
		branchToMainAddr.SetBase(0);
		branchToMainAddr.SetOffset(cgpc);
		// Process main function
		if (ASTRoot.Right() != null)
			main(ASTRoot.Right());

		addInstruction(new Instruction(IHALT));
	}

	// Retrieves the collection of constant symbols from the symbol Table
	// and sorts them into the type list to write to mod file
	// No need to set the type tag in instruct memory
	private void constants() {
		for (Symbol constant : symbolTable.GetConstants()) {
			if (constant.Type().equals("TILIT ")) {
				constant.SetBase(0);
				constant.SetOffset(intConstants.size() * 8);
				intConstants.add(constant);
			}
			else if (constant.Type().equals("TFLIT ")) {
				constant.SetBase(0);
				constant.SetOffset(realConstants.size() * 8);
				realConstants.add(constant);
			}
			else {
				constant.SetBase(0);
				constant.SetOffset(stringConstSize);
				stringConstants.add(constant);
				constant.SetValue(constant.Value() + "\0");
				stringConstSize += constant.Value().length();
			}
		}
	}

	private void funcs(TreeNode nfuncs) {
		TreeNode nfund = nfuncs.Left();
		scope = nfund.SymbolValue();
		symbolTable.RetrieveSymbol(scope, scope).SetBase(0);
		symbolTable.RetrieveSymbol(scope, scope).SetOffset(cgpc);

		allocounter = 0;
		b2p = 1;
		if (nfund.Middle() != null)
			dlist(nfund.Middle());

		if (nfund.Right() != null)
			stats(nfund.Right());

		if (nfuncs.Right() != null)
			funcs(nfuncs.Right());
	}

	private void alist(TreeNode node) {
		if (node.Value() == NALIST) {
			alist(node.Left());
			if (node.Right().Value() == NALIST)
				alist(node.Right());
			else if (node.Right().Value() == NARRD)
				arrdecl(node.Right());
		}
		else if (node.Value() == NARRD)
			arrdecl(node);
	}

	private void dlist(TreeNode node) {
		if (node.Value() == NDLIST) {
			dlist(node.Left());
			if (node.Right().Value() == NDLIST)
				dlist(node.Right());
			else if (node.Right().Value() == NSDECL) {
				sdecl(node.Right());
				Symbol s = new Symbol();
				s.SetValue("@" + "1");
				addInstruction(new Instruction(ILB, s));
				addInstruction(new Instruction(IALLOC));
			}
			else if (node.Right().Value() == NARRD)
				arrdecl(node.Right());
		}
		else if (node.Value() == NSDECL) {
			sdecl(node);
			Symbol s = new Symbol();
			s.SetValue("@" + "1");
			addInstruction(new Instruction(ILB, s));
			addInstruction(new Instruction(IALLOC));
		}
		else if (node.Value() == NARRD)
			arrdecl(node);
	}

	private void sdecl(TreeNode nsdecl) {
		int base;
		if (symbolTable.IsScope(scope)) {
			base = 2;
			b2p++;
			symbolTable.RetrieveSymbol(scope, nsdecl.SymbolValue()).SetBase(base);
			symbolTable.RetrieveSymbol(scope, nsdecl.SymbolValue()).SetOffset((b2p) * 8);
		}
		else {
			base = 1;
			sp++;
			symbolTable.RetrieveSymbol(scope, nsdecl.SymbolValue()).SetBase(base);
			symbolTable.RetrieveSymbol(scope, nsdecl.SymbolValue()).SetOffset((sp) * 8);
		}

	}

	private void arrdecl(TreeNode narrd) {
		int base;
		if (symbolTable.IsScope(scope)) {
			base = 2;
			addInstruction(new Instruction(ISTEP));
			symbolTable.RetrieveSymbol(scope, narrd.SymbolValue()).SetBase(base);
			symbolTable.RetrieveSymbol(scope, narrd.SymbolValue()).SetOffset((b2p) * 8);
			addInstruction(new Instruction(ILAB0 + base, symbolTable.RetrieveSymbol(scope, 
				narrd.SymbolValue())));
			Symbol arrSize = symbolTable.RetrieveSymbol(scope, narrd.SymbolType()).Attributes().get(1);
			if (!symbolTable.ConstantExists(arrSize.Value())) {
				if (Integer.parseInt(arrSize.Value().substring(1)) <= 255)
					addInstruction(new Instruction(ILB, arrSize));
				else
					addInstruction(new Instruction(ILH, arrSize));
			}
			else
				addInstruction(new Instruction(ILVB0 + base, arrSize));
	
			addInstruction(new Instruction(IARRAY));
			b2p += Integer.parseInt(arrSize.Value().substring(1));
		}
		else {
			base = 1;
			addInstruction(new Instruction(ISTEP));
			symbolTable.RetrieveSymbol(scope, narrd.SymbolValue()).SetBase(base);
			symbolTable.RetrieveSymbol(scope, narrd.SymbolValue()).SetOffset((sp) * 8);
			addInstruction(new Instruction(ILAB0 + base, symbolTable.RetrieveSymbol(scope, 
				narrd.SymbolValue())));
			Symbol arrSize = symbolTable.RetrieveSymbol(scope, narrd.SymbolType()).Attributes().get(1);
			if (!symbolTable.ConstantExists(arrSize.Value())) {
				if (Integer.parseInt(arrSize.Value().substring(1)) <= 255)
					addInstruction(new Instruction(ILB, arrSize));
				else
					addInstruction(new Instruction(ILH, arrSize));
			}
			else
				addInstruction(new Instruction(ILVB0 + base, arrSize));
	
			addInstruction(new Instruction(IARRAY));
			sp += Integer.parseInt(arrSize.Value().substring(1));
		}
	}

	private void main(TreeNode nmain) {
		scope = "@main";

		allocounter = 0;
		mainlocals(nmain.Left());
		Symbol s = new Symbol();
		s.SetValue("@" + allocounter);
		addInstruction(new Instruction(ILB, s));
		addInstruction(new Instruction(IALLOC));

		stats(nmain.Right());
	}

	private void mainlocals(TreeNode nmainlocal) {
		if (nmainlocal.Value() == NSDECL) {
			sdecl(nmainlocal);
			allocounter++;
		}
	
		if (nmainlocal.Left() != null)
			mainlocals(nmainlocal.Left());
		if (nmainlocal.Right() != null)
			mainlocals(nmainlocal.Right());
	}

	private void stats(TreeNode node) {
		if (node.Value() == NSTATS) {
			stats(node.Left());
			if (node.Right().Value() == NSTATS)
				stats(node.Right());
			else if (node.Right().Value() >= NASGN && node.Right().Value() <= NDVEQ)
				nasgn(node.Right());
			else if (node.Right().Value() == NREPT)
				nrept(node.Right());
			else if (node.Right().Value() == NFOR)
				nfor(node.Right());
			else if (node.Right().Value() == NIFTH)
				nifth(node.Right());
			else if (node.Right().Value() == NIFTE)
				nifte(node.Right());
			else if (node.Right().Value() == NINPUT)
				ninput(node.Right().Left());
			else if (node.Right().Value() == NPRINT)
				nprint(node.Right().Left());
			else if (node.Right().Value() == NPRLN) {
				nprln(node.Right().Left());
				addInstruction(new Instruction(INEWLN));
			}
			else if (node.Right().Value() == NCALL) {
				if (scope.equals("@main"))
					nvfcall(node.Right());
				else
					nvffcall(node.Right());
			}
			else if (node.Right().Value() == NRETN)
				nretn(node.Right());
		}
		else if (node.Value() >= NASGN && node.Value() <= NDVEQ)
			nasgn(node);
		else if (node.Value() == NREPT)
			nrept(node);
		else if (node.Value() == NFOR)
			nfor(node);
		else if (node.Value() == NIFTH)
			nifth(node);
		else if (node.Value() == NIFTE)
			nifte(node);
		else if (node.Value() == NINPUT)
			ninput(node.Left());
		else if (node.Value() == NPRINT)
			nprint(node.Left());
		else if (node.Value() == NPRLN) {
			nprln(node.Left());
			addInstruction(new Instruction(INEWLN));
		}
		else if (node.Value() == NCALL) {
			if (scope.equals("@main"))
				nvfcall(node);
			else
				nvffcall(node);
		}
		else if (node.Value() == NRETN)
			nretn(node);
	}

	private void nrept(TreeNode node) {
		asgnlist(node.Left());
		
		int jumpHere = cgpc;
		stats(node.Middle());
		Symbol jumper = new Symbol();
		jumper.SetType("NODE");
		jumper.SetOffset(jumpHere);
		addInstruction(new Instruction(ILAB0, jumper));

		bool(node.Right());
		addInstruction(new Instruction(IBF));
	}

	private void asgnlist(TreeNode node) {
		if (node.Value() == NASGNS) {
			asgnlist(node.Left());
			if (node.Right().Value() == NASGNS)
				asgnlist(node.Right());
			else if (node.Right().Value() >= NASGN && node.Right().Value() <= NDVEQ)
				nasgn(node.Right());
		}
		else if (node.Value() >= NASGN && node.Value() <= NDVEQ)
			nasgn(node);
	}

	private void nfor(TreeNode node) {
		asgnlist(node.Left());

		Symbol skipper = new Symbol();
		skipper.SetType("NODE");
		int returnHere = cgpc;
		addInstruction(new Instruction(ILAB0, skipper));
		bool(node.Middle());
		addInstruction(new Instruction(IBF));
		stats(node.Right());
		Symbol returner = new Symbol();
		returner.SetType("NODE");
		returner.SetOffset(returnHere);
		addInstruction(new Instruction(ILAB0, returner));
		addInstruction(new Instruction(IBR));
		skipper.SetOffset(cgpc);
	}

	private void nifth(TreeNode node) {
		Symbol elser = new Symbol();
		elser.SetType("NODE");
		addInstruction(new Instruction(ILAB0, elser));
		bool(node.Left());
		addInstruction(new Instruction(IBF));
		stats(node.Right());
		elser.SetOffset(cgpc);
	}

	private void nifte(TreeNode node) {
		Symbol thenner = new Symbol();
		Symbol elser = new Symbol();
		thenner.SetType("NODE");
		addInstruction(new Instruction(ILAB0, elser));
		bool(node.Left());
		addInstruction(new Instruction(IBF));
		stats(node.Middle());
		elser.SetType("NODE");
		addInstruction(new Instruction(ILAB0, thenner));
		addInstruction(new Instruction(IBR));
		elser.SetOffset(cgpc);
		stats(node.Right());
		thenner.SetOffset(cgpc);
	}

	private void ninput(TreeNode node) {
		if (node.Value() == NVLIST) {
			ninput(node.Left());
			if (node.Right().Value() == NVLIST)
				ninput(node.Right());
			else if (node.Right().Value() == NSIMV) {
				addressAccess(node.Right());
				if (node.Right().SymbolType().equals("TILIT "))
					addInstruction(new Instruction(IREADI));
				else
					addInstruction(new Instruction(IREADF));
				addInstruction(new Instruction(IST));
			}
			else if (node.Right().Value() == NARRV) {
				arrayAddressAccess(node.Right());
				if (node.Right().Right().SymbolType().equals("TILIT "))
					addInstruction(new Instruction(IREADI));
				else
					addInstruction(new Instruction(IREADF));
				addInstruction(new Instruction(IST));
			}
		}
		else if (node.Value() == NSIMV) {
			addressAccess(node);
			if (node.SymbolType().equals("TILIT "))
				addInstruction(new Instruction(IREADI));
			else
				addInstruction(new Instruction(IREADF));
			addInstruction(new Instruction(IST));
		}
		else if (node.Value() == NARRV) {
			arrayAddressAccess(node);
			if (node.Right().SymbolType().equals("TILIT "))
				addInstruction(new Instruction(IREADI));
			else
				addInstruction(new Instruction(IREADF));
			addInstruction(new Instruction(IST));
		}
	}

	private void nprint(TreeNode node) {
		if (node.Value() == NPRLST) {
			nprint(node.Left());
			if (node.Right().Value() == NPRLST)
				nprint(node.Right());
			else if (node.Right().Value() == NSTRG) {
				addInstruction(new Instruction(ILAB0, node.Right().Symbol()));
				addInstruction(new Instruction(ISTRPR));
			}
			else {
				expr(node.Right());
				addInstruction(new Instruction(IVALPR));
			}
		}
		else if (node.Value() == NSTRG) {
			addInstruction(new Instruction(ILAB0, node.Symbol()));
			addInstruction(new Instruction(ISTRPR));
		}
		else {
			expr(node);
			addInstruction(new Instruction(IVALPR));
		}
	}

	private void nprln(TreeNode node) {
		if (node.Value() == NPRLST) {
			nprln(node.Left());
			if (node.Right().Value() == NPRLST)
				nprln(node.Right());
			else if (node.Right().Value() == NSTRG) {
				addInstruction(new Instruction(ILAB0, node.Right().Symbol()));
				addInstruction(new Instruction(ISTRPR));
			}
			else {
				expr(node.Right());
				addInstruction(new Instruction(IVALPR));
			}
		}
		else if (node.Value() == NSTRG) {
			addInstruction(new Instruction(ILAB0, node.Symbol()));
			addInstruction(new Instruction(ISTRPR));
		}
		else {
			expr(node);
			addInstruction(new Instruction(IVALPR));
		}
	}

	private void nvffcall(TreeNode node) {
		int targetB2p = b2p;

		String prevScope = scope;
		scope = node.SymbolValue();
		Symbol functionDefinition = symbolTable.RetrieveSymbol(scope, scope);
		ArrayList<Symbol> params = functionDefinition.Attributes();
		ArrayList<TreeNode> exprNodes = new ArrayList<TreeNode>();
		if (node.Left() != null) {
			elist(node.Left(), exprNodes);
			Collections.reverse(exprNodes);
			Collections.reverse(params);
			int offset = params.size() * -1;
			
			int iterator = 0;
			for (Symbol param : params) {
				// Needs to reference by address, ie just load the array descrpitor to this 
				// location. Your are not creating a new array. That has already been done.
				//Just load the array descriptoe!!!!!
				if (exprNodes.get(iterator).Value() == NARRV) {
					Symbol reference = symbolTable.RetrieveSymbol(prevScope, exprNodes.get(iterator).SymbolValue());
					symbolTable.RetrieveSymbol(scope, param.Value()).SetBase(2);
					symbolTable.RetrieveSymbol(scope, param.Value()).SetOffset(offset * 8);
					addInstruction(new Instruction(ILVB0 + reference.Base(), reference));
				}
				else {
					param.SetBase(2);
					param.SetOffset((offset) * 8);
					addInstruction(new Instruction(ISTEP));
					Symbol paramAddr = new Symbol();
					paramAddr.SetBase(2);
					paramAddr.SetOffset((b2p) * 8);
					addInstruction(new Instruction(ILAB0 + paramAddr.Base(), paramAddr));
					scope = prevScope;
					expr(exprNodes.get(iterator));
					scope = node.SymbolValue();
					addInstruction(new Instruction(IST));
				}
				offset++;
				iterator++;
			}

			Collections.reverse(exprNodes);
			Collections.reverse(params);
		}

		Symbol paramCount = new Symbol();
		paramCount.SetValue("@" + params.size());
		addInstruction(new Instruction(ISTEP));
		paramCount.SetBase(2);
		paramCount.SetOffset((b2p) * 8);
		addInstruction(new Instruction(ILAB0 + paramCount.Base(), paramCount));
		addInstruction(new Instruction(ILB, paramCount));
		addInstruction(new Instruction(IST));
		addInstruction(new Instruction(ILAB0, functionDefinition));
		addInstruction(new Instruction(IJS2));

		b2p = targetB2p;
		scope = prevScope;
	}

	private void nvfcall(TreeNode node) {
		int targetSp = sp;

		String prevScope = scope;
		scope = node.SymbolValue();
		Symbol functionDefinition = symbolTable.RetrieveSymbol(scope, scope);
		ArrayList<Symbol> params = functionDefinition.Attributes();
		ArrayList<TreeNode> exprNodes = new ArrayList<TreeNode>();
		if (node.Left() != null) {
			elist(node.Left(), exprNodes);
			Collections.reverse(exprNodes);
			Collections.reverse(params);
			int offset = params.size() * -1;
			
			int iterator = 0;
			for (Symbol param : params) {
				if (exprNodes.get(iterator).Value() == NARRV) {
					Symbol reference = symbolTable.RetrieveSymbol(prevScope, exprNodes.get(iterator).SymbolValue());
					symbolTable.RetrieveSymbol(scope, param.Value()).SetBase(2);
					symbolTable.RetrieveSymbol(scope, param.Value()).SetOffset(offset * 8);
					addInstruction(new Instruction(ILVB0 + reference.Base(), reference));
					sp++;
				}
				else {
					param.SetBase(2);
					param.SetOffset((offset) * 8);
					addInstruction(new Instruction(ISTEP));
					sp++;
					Symbol paramAddr = new Symbol();
					paramAddr.SetBase(1);
					paramAddr.SetOffset((sp) * 8);
					addInstruction(new Instruction(ILAB0 + paramAddr.Base(), paramAddr));
					sp++;
					scope = prevScope;
					expr(exprNodes.get(iterator));
					scope = node.SymbolValue();
					addInstruction(new Instruction(IST));
					sp -= 2;
				}
				offset++;
				iterator++;
			}

			Collections.reverse(exprNodes);
			Collections.reverse(params);
		}

		Symbol paramCount = new Symbol();
		paramCount.SetValue("@" + params.size());
		addInstruction(new Instruction(ISTEP));
		sp++;
		paramCount.SetBase(1);
		paramCount.SetOffset((sp) * 8);
		addInstruction(new Instruction(ILAB0 + paramCount.Base(), paramCount));
		addInstruction(new Instruction(ILB, paramCount));
		addInstruction(new Instruction(IST));
		addInstruction(new Instruction(ILAB0, functionDefinition));
		addInstruction(new Instruction(IJS2));

		sp = targetSp;
		scope = prevScope;
	}

	private void nffcall(TreeNode node) {
		addInstruction(new Instruction(ISTEP));
		
		int targetB2p = b2p;

		String prevScope = scope;
		scope = node.SymbolValue();
		Symbol functionDefinition = symbolTable.RetrieveSymbol(scope, scope);
		ArrayList<Symbol> params = functionDefinition.Attributes();
		ArrayList<TreeNode> exprNodes = new ArrayList<TreeNode>();
		if (node.Left() != null) {
			elist(node.Left(), exprNodes);
			Collections.reverse(exprNodes);
			Collections.reverse(params);
			int offset = params.size() * -1;
			
			int iterator = 0;
			for (Symbol param : params) {
				if (exprNodes.get(iterator).Value() == NARRV) {
					Symbol reference = symbolTable.RetrieveSymbol(prevScope, exprNodes.get(iterator).SymbolValue());
					symbolTable.RetrieveSymbol(scope, param.Value()).SetBase(2);
					symbolTable.RetrieveSymbol(scope, param.Value()).SetOffset(offset * 8);
					addInstruction(new Instruction(ILVB0 + reference.Base(), reference));
				}
				else {
					param.SetBase(2);
					param.SetOffset((offset) * 8);
					addInstruction(new Instruction(ISTEP));
					Symbol paramAddr = new Symbol();
					paramAddr.SetBase(2);
					paramAddr.SetOffset((b2p) * 8);
					addInstruction(new Instruction(ILAB0 + paramAddr.Base(), paramAddr));
					scope = prevScope;
					expr(exprNodes.get(iterator));
					scope = node.SymbolValue();
					addInstruction(new Instruction(IST));
				}
				offset++;
				iterator++;
			}

			Collections.reverse(exprNodes);
			Collections.reverse(params);
		}

		Symbol paramCount = new Symbol();
		paramCount.SetValue("@" + params.size());
		addInstruction(new Instruction(ISTEP));
		paramCount.SetBase(2);
		paramCount.SetOffset((b2p) * 8);
		addInstruction(new Instruction(ILAB0 + paramCount.Base(), paramCount));
		addInstruction(new Instruction(ILB, paramCount));
		addInstruction(new Instruction(IST));
		addInstruction(new Instruction(ILAB0, functionDefinition));
		addInstruction(new Instruction(IJS2));

		b2p = targetB2p;
		scope = prevScope;
	}

	private void nfcall(TreeNode node) {
		addInstruction(new Instruction(ISTEP));
		
		int targetSp = sp;

		String prevScope = scope;
		scope = node.SymbolValue();
		Symbol functionDefinition = symbolTable.RetrieveSymbol(scope, scope);
		ArrayList<Symbol> params = functionDefinition.Attributes();
		ArrayList<TreeNode> exprNodes = new ArrayList<TreeNode>();
		if (node.Left() != null) {
			elist(node.Left(), exprNodes);
			Collections.reverse(exprNodes);
			Collections.reverse(params);
			int offset = params.size() * -1;
			
			int iterator = 0;
			for (Symbol param : params) {
				if (exprNodes.get(iterator).Value() == NARRV) {
					Symbol reference = symbolTable.RetrieveSymbol(prevScope, exprNodes.get(iterator).SymbolValue());
					symbolTable.RetrieveSymbol(scope, param.Value()).SetBase(2);
					symbolTable.RetrieveSymbol(scope, param.Value()).SetOffset(offset * 8);
					addInstruction(new Instruction(ILVB0 + reference.Base(), reference));
					sp++;
				}
				else {
					param.SetBase(2);
					param.SetOffset((offset) * 8);
					addInstruction(new Instruction(ISTEP));
					sp++;
					Symbol paramAddr = new Symbol();
					paramAddr.SetBase(1);
					paramAddr.SetOffset((sp) * 8);
					addInstruction(new Instruction(ILAB0 + paramAddr.Base(), paramAddr));
					sp++;
					scope = prevScope;
					expr(exprNodes.get(iterator));
					scope = node.SymbolValue();
					addInstruction(new Instruction(IST));
					sp -= 2;
				}
				offset++;
				iterator++;
			}

			Collections.reverse(exprNodes);
			Collections.reverse(params);
		}

		Symbol paramCount = new Symbol();
		paramCount.SetValue("@" + params.size());
		addInstruction(new Instruction(ISTEP));
		sp++;
		paramCount.SetBase(1);
		paramCount.SetOffset((sp) * 8);
		addInstruction(new Instruction(ILAB0 + paramCount.Base(), paramCount));
		addInstruction(new Instruction(ILB, paramCount));
		addInstruction(new Instruction(IST));
		addInstruction(new Instruction(ILAB0, functionDefinition));
		addInstruction(new Instruction(IJS2));

		sp = targetSp;
		scope = prevScope;
	}

	private void elist(TreeNode node, ArrayList<TreeNode> exprNodes) {
		if (node.Value() == NEXPL) {
			elist(node.Left(), exprNodes);
			if (node.Right().Value() == NEXPL)
				elist(node.Right(), exprNodes);
			else
				exprNodes.add(node.Right());
		}
		else
			exprNodes.add(node);
	}

	private void nretn(TreeNode node) {
		if (node.Left() != null) {
			expr(node.Left());
			addInstruction(new Instruction(IRVAL));
		}
		addInstruction(new Instruction(IRETN));
	}

	private void addressAccess(TreeNode node) {
		Symbol ref = symbolTable.RetrieveSymbol(scope, node.Symbol().Value());
		addInstruction(new Instruction(ILAB0 + ref.Base(), ref));
	}
	
	private void nasgn(TreeNode node) { // left var, right is bool
		int targetStack;
		if (scope.equals("@main"))
			targetStack = sp;
		else
			targetStack = b2p;

		if (node.Left().Value() == NARRV && node.Left().Left() == null &&
			node.Right().Value() == NARRV && node.Right().Left() == null) {
			Symbol arrDescLHS = symbolTable.RetrieveSymbol(scope, node.Left().SymbolValue());
			Symbol arrDescRHS = symbolTable.RetrieveSymbol(scope, node.Right().SymbolValue());

			symbolTable.RemoveSymbol(scope, arrDescLHS.Value());
			Symbol newArrAddr = new Symbol();
			newArrAddr.SetValue(arrDescLHS.Value());
			newArrAddr.SetType(arrDescLHS.Type());
			newArrAddr.SetBase(arrDescRHS.Base());
			newArrAddr.SetOffset(arrDescRHS.Offset());
			newArrAddr.SetAttributes(arrDescLHS.Attributes());
			symbolTable.AddSymbol(scope, newArrAddr.Value(), newArrAddr);
			node.Left().SetSymbol(newArrAddr);
		}
		else {
			if (node.Left().Value() == NARRV)
				arrayAddressAccess(node.Left());
			else
				addressAccess(node.Left());

			if (node.Value() >= NPLEQ && node.Value() <= NDVEQ) {
				addInstruction(new Instruction(IDUP));
				addInstruction(new Instruction(IL));
			}

			bool(node.Right());

			if (node.Value() == NPLEQ)
				addInstruction(new Instruction(IADD));
			else if (node.Value() == NMNEQ)
				addInstruction(new Instruction(ISUB));
			else if (node.Value() == NSTEQ)
				addInstruction(new Instruction(IMUL));
			else if (node.Value() == NDVEQ)
				addInstruction(new Instruction(IDIV));

			addInstruction(new Instruction(IST));
		}

		if (scope.equals("@main"))
			sp = targetStack;
		else
			b2p = targetStack;
	}
	
	private void bool(TreeNode node) {
		if (node.Value() == NBOOL)
			logop(node.Left());
		else
			rel(node);
	}

	private void logop(TreeNode node) {
		int value = node.Value();
		if (value == NAND || value == NOR || value == NXOR) {
			bool(node.Left());
			bool(node.Right());

			if (value == NAND)
				addInstruction(new Instruction(IAND));
			else if (value == NOR)
				addInstruction(new Instruction(IOR));
			else if (value == NXOR)
				addInstruction(new Instruction(IXOR));

		}
		else
			rel(node);
	}

	private void rel(TreeNode node) {
		int value = node.Value();
		if (value == NNOT) {
			bool(node.Left());
			addInstruction(new Instruction(INOT));
		}
		else if (value == NEQL || value == NNEQ || value == NGRT ||
			value == NGEQ || value == NLSS || value == NLEQ) {
			bool(node.Left());
			bool(node.Right());
			addInstruction(new Instruction(ISUB));

			if (value == NEQL)
				addInstruction(new Instruction(IEQ));
			else if (value == NNEQ)
				addInstruction(new Instruction(INE));
			else if (value == NGRT)
				addInstruction(new Instruction(IGT));
			else if (value == NGEQ)
				addInstruction(new Instruction(IGE));
			else if (value == NLSS)
				addInstruction(new Instruction(ILT));
			else
				addInstruction(new Instruction(ILE));
		}
		else
			expr(node);
	}

	private void expr(TreeNode node) {
		if (node.Value() == NADD || node.Value() == NSUB) {
			bool(node.Left());
			bool(node.Right());
	
			int instruction;
			if (node.Value() == NADD)
				instruction = IADD;
			else
				instruction = ISUB;
			addInstruction(new Instruction(instruction));
		}
		else
			term(node);
	}

	private void term(TreeNode node) {
		if (node.Value() == NMUL || node.Value() == NDIV || node.Value() == NMOD) {
			bool(node.Left());
			bool(node.Right());
	
			int instruction;
			if (node.Value() == NMUL)
				instruction = IMUL;
			else if (node.Value() == NDIV)
				instruction = IDIV;
			else
				instruction = IREM;
			addInstruction(new Instruction(instruction));
		}
		else
			fact(node);
	}

	private void fact(TreeNode node) {
		if (node.Value() == NPOW) {
			bool(node.Left());	
			bool(node.Right());

			addInstruction(new Instruction(IPOW));
		}
		else
			exponent(node);
	}

	private void exponent(TreeNode node) {
		if (scope.equals("@main") && node.Value() == NFCALL)
			nfcall(node);
		else if (node.Value() == NFCALL)
			nffcall(node);
		else if (node.SymbolType().equals("TILIT "))
			intlit(node);
		else if (node.SymbolType().equals("TFLIT "))
			flit(node);
		else if (node.SymbolType().equals("TBOOL "))
			boollit(node);

		else
			arrayAccess(node);
	}

	private void intlit(TreeNode node) {
		Symbol ref = symbolTable.RetrieveSymbol(scope, node.Symbol().Value());
		if (!symbolTable.ConstantExists(ref.Value()) && ref.Value().charAt(0) == '@') {
			if (Integer.parseInt(ref.Value().substring(1)) <= 127)
				addInstruction(new Instruction(ILB, ref));
			else
				addInstruction(new Instruction(ILH, ref));
		}
		else
			addInstruction(new Instruction(ILVB0 + ref.Base(), ref));
	}

	private void flit(TreeNode node) {
		Symbol ref = symbolTable.RetrieveSymbol(scope, node.Symbol().Value());
		addInstruction(new Instruction(ILVB0 + ref.Base(), ref));
	}

	private void boollit(TreeNode node) {
		if (node.Value() == NTRUE)
			addInstruction(new Instruction(ITRUE));
		else if (node.Value() == NFALS)
			addInstruction(new Instruction(IFALSE));
		else {
			Symbol ref = symbolTable.RetrieveSymbol(scope, node.Symbol().Value());
			addInstruction(new Instruction(ILVB0 + ref.Base(), ref));
		}
		
	}

	private void arrayAccess(TreeNode node) {
		Symbol arrDesc = symbolTable.RetrieveSymbol(scope, node.SymbolValue());
		
		if (node.Left() != null) {
			addInstruction(new Instruction(ILVB0 + arrDesc.Base(), arrDesc));
			expr(node.Left());
			Symbol structSize = new Symbol();
			structSize.SetValue("@" + symbolTable.StructSize(arrDesc.Type()));
			addInstruction(new Instruction(ILB, structSize));

			addInstruction(new Instruction(IMUL));
			addInstruction(new Instruction(ILB, node.Right().Symbol()));
			addInstruction(new Instruction(IADD));
			addInstruction(new Instruction(IINDEX));
			addInstruction(new Instruction(IL));
		}
	}

	private void arrayAddressAccess(TreeNode node) {
		Symbol arrDesc = symbolTable.RetrieveSymbol(scope, node.SymbolValue());
		if (node.Left() != null) {
			addInstruction(new Instruction(ILVB0 + arrDesc.Base(), arrDesc));
			expr(node.Left());
			Symbol structSize = new Symbol();
			structSize.SetValue("@" + symbolTable.StructSize(arrDesc.Type()));
			addInstruction(new Instruction(ILB, structSize));
			addInstruction(new Instruction(IMUL));
			addInstruction(new Instruction(ILB, node.Right().Symbol()));
			addInstruction(new Instruction(IADD));
			addInstruction(new Instruction(IINDEX));
		}
	}

	private void addInstruction(Instruction instruction) {
		instructions.add(instruction);
		
		int opcode = instruction.Opcode();
		if (opcode == ILB)
			cgpc += 2;
		else if (opcode == ILH)
			cgpc += 3;
		else if (opcode >= ILVB0)
			cgpc += 5;
		else
			cgpc++;

		if (scope.equals("@main") || scope.equals("@globals")) {
			if (opcode >= IZERO && opcode <= ITRUE)
				sp++;
			else if (opcode >= IADD && opcode <= IPOW)
				sp--;
			else if (opcode >= IAND && opcode <= IXOR)
				sp--;
			else if (opcode >= IBT && opcode <= IBF || opcode == IARRAY)
				sp -= 2;
			else if (opcode == IBR || opcode == IALLOC)
				sp--;
			else if (opcode >= ILB && opcode <= ILH)
				sp++;
			else if (opcode == IST)
				sp -= 2;
			else if (opcode == ISTEP)
				sp++;
			else if (opcode == IINDEX)
				sp--;
			else if (opcode >= IDUP && opcode <= IREADI)
				sp++;
			else if (opcode >= IVALPR && opcode <= ICHRPR)
				sp--;
			else if (opcode == IRVAL)
				sp--;
			else if (opcode >= ILVB0)
				sp++;
		}
		else {
			if (opcode >= IZERO && opcode <= ITRUE)
				b2p++;
			else if (opcode >= IADD && opcode <= IPOW)
				b2p--;
			else if (opcode >= IAND && opcode <= IXOR)
				b2p--;
			else if (opcode >= IBT && opcode <= IBF || opcode == IARRAY)
				b2p -= 2;
			else if (opcode == IBR || opcode == IALLOC)
				b2p--;
			else if (opcode >= ILB && opcode <= ILH)
				b2p++;
			else if (opcode == IST)
				b2p -= 2;
			else if (opcode == ISTEP)
				b2p++;
			else if (opcode == IINDEX)
				b2p--;
			else if (opcode >= IDUP && opcode <= IREADI)
				b2p++;
			else if (opcode >= IVALPR && opcode <= ICHRPR)
				b2p--;
			else if (opcode == IRVAL)
				b2p--;
			else if (opcode >= ILVB0)
				b2p++;
		}
	}

	public String outputString() {
		return stdOutString;
	}

	private void createModFile() {
		try {
			mod = new PrintWriter(new FileOutputStream(fileName + ".mod"), true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int instructionWords = cgpc / 8;
		int fillerBytes = 8 - cgpc % 8;
		int stringWords = stringConstSize / 8;
		int stringFillerBytes = 8 - stringConstSize % 8;
		
		if (cgpc % 8 == 0)
			fillerBytes = 0;

		if (stringConstSize % 8 == 0)
			stringFillerBytes = 0;

		if (fillerBytes != 0)
			instructionWords++;

		mod.println(instructionWords); // Number of words assigned for INSTR type
		stdOutString += instructionWords + "\n";

		if (stringFillerBytes != 0)
			stringWords++;

		// Readjust the memory locations in rom for constants
		for (Symbol s : intConstants) {
			s.SetOffset(s.Offset() + instructionWords * 8);
		}

		for (Symbol s : realConstants) {
			s.SetOffset(s.Offset() + instructionWords * 8 + intConstants.size() * 8);
		}

		for (Symbol s : stringConstants) {
			s.SetOffset(s.Offset() + instructionWords * 8 + intConstants.size() * 8 +
			realConstants.size() * 8);
		}

		int lineLength = 0;
		for (Instruction i : instructions) {
			int opcode = i.Opcode();
			if (opcode == ILB) { // Operands are of type "IMMEDIATE" and use the Symbol Value with the "@"
				mod.print(ILB + "\t");
				stdOutString += ILB + "\t";
				lineLength++;
				if (lineLength % 8 == 0) {
					mod.println();
					stdOutString += "\n";
					
					lineLength = 0;
				}
				mod.print(Integer.parseInt(i.Operand().Value().substring(1)) + "\t");
				stdOutString += Integer.parseInt(i.Operand().Value().substring(1)) + "\t";
				lineLength++;
				if (lineLength % 8 == 0) {
					mod.println();
					stdOutString += "\n";
					lineLength = 0;
				}
			}
			else if (opcode == ILH) { // Operands are of type "IMMEDIATE" and use the Symbol Value with the "@"
				int val = Integer.parseInt(i.Operand().Value().substring(1));
				int[] bytes = new int[] {val / 256, val % 256};
				
				mod.print(ILH + "\t");
				stdOutString += ILH + "\t";
				lineLength++;
				if (lineLength % 8 == 0) {
					mod.println();
					stdOutString += "\n";
					lineLength = 0;
				}
				for (int b : bytes) {
					mod.print(b + "\t");
					stdOutString += b + "\t";
					lineLength++;
					if (lineLength % 8 == 0) {
						mod.println();
						stdOutString += "\n";
						lineLength = 0;
					}
				}
			}
			else if (opcode >= ILVB0) { // Operands are of type "ADDR"
				int val = Integer.parseInt(Integer.toString(i.Operand().Offset()));
				int[] bytes = new int[] {
					val / (256 * 256 * 256),
					val / (256 * 256),
					val / 256,
					val % 256
				};
				
				mod.print(opcode + "\t");
				stdOutString += opcode + "\t";
				lineLength++;
				if (lineLength % 8 == 0) {
					mod.println();
					stdOutString += "\n";
					lineLength = 0;
				}

				for (int b : bytes) {
					mod.print(b + "\t");
					stdOutString += b + "\t";
					lineLength++;
					if (lineLength % 8 == 0) {
						mod.println();
						stdOutString += "\n";
						lineLength = 0;
					}
				}
			}
			else {
				mod.print(opcode + "\t");
				stdOutString += opcode + "\t";
				lineLength++;
				if (lineLength % 8 == 0) {
					mod.println();
					stdOutString += "\n";
					lineLength = 0;
				}
			}
		}

		for (int i = 0; i < fillerBytes; i++) {
			byte nuller = 0;
			mod.print(nuller + "\t");
			stdOutString += nuller + "\t";
			lineLength++;
			if (lineLength % 8 == 0) {
				mod.println();
				stdOutString += "\n";
				lineLength = 0;
			}
		}


		mod.println(intConstants.size());
		stdOutString += intConstants.size() + "\n";
		for (Symbol s : intConstants) {
			if (s.Value().contains("@")) {
				mod.println(s.Value().substring(1));
				stdOutString += s.Value().substring(1) + "\n";
			}
			else {
				mod.println(s.Attributes().get(0).Value().substring(1));
				stdOutString += s.Attributes().get(0).Value().substring(1) + "\n";
			}
		}

		mod.println(realConstants.size());
		stdOutString += realConstants.size() + "\n";
		for (Symbol s : realConstants) {
			if (s.Value().contains("@")) {
				mod.println(s.Value().substring(1));
				stdOutString += s.Value().substring(1) + "\n";
			}
			else {
				mod.println(s.Attributes().get(0).Value().substring(1));
				stdOutString += s.Attributes().get(0).Value().substring(1) + "\n";
			}
		}
		
		mod.println(stringWords);
		stdOutString += stringWords + "\n";
		for (Symbol s : stringConstants) {
			byte[] bytes = s.Value().getBytes();
			for (byte b : bytes) {
				mod.print(b + "\t");
				stdOutString += b + "\t";
				lineLength++;
				if (lineLength % 8 == 0) {
					mod.println();
					stdOutString += "\n";
					lineLength = 0;
				}
			}
		}

		for (int i = 0; i < stringFillerBytes; i++) {
			int nuller = 0;
			mod.print(nuller + "\t");
			stdOutString += nuller + "\t";
		}

		mod.close();
	}
}