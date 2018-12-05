// Student no: c3186200
//     Course: COMP3290

public class TreeNode {

	public static final int NUNDEF = 0,
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

	public static final String PRINTNODE[] = {
		"NUNDEF",
		"NPROG",	"NGLOB",	"NILIST",	"NINIT",	"NFUNCS",
		"NMAIN",	"NSDLST",	"NTYPEL",	"NRTYPE",	"NATYPE",
		"NFLIST",	"NSDECL",	"NALIST",	"NARRD",	"NFUND",
		"NPLIST",	"NSIMP",	"NARRP",	"NARRC",	"NDLIST",
		"NSTATS",	"NFOR",		"NREPT",	"NASGNS",	"NIFTH",
		"NIFTE",	"NASGN",	"NPLEQ",	"NMNEQ",	"NSTEQ",
		"NDVEQ",	"NINPUT",	"NPRINT",	"NPRLN",	"NCALL",
		"NRETN",	"NVLIST",	"NSIMV",	"NARRV",	"NEXPL",
		"NBOOL",	"NNOT",		"NAND",		"NOR",		"NXOR",
		"NEQL",		"NNEQ",		"NGRT",		"NLSS",		"NLEQ",
		"NADD",		"NSUB",		"NMUL",		"NDIV",		"NMOD",
		"NPOW",		"NILIT",	"NFLIT",	"NTRUE",	"NFALS",
		"NFCALL",	"NPRLST",	"NSTRG",	"NGEQ"
	};

	private String symbolValue;
	private String symbolType;
	private Symbol symbol;
	private int value;
	private TreeNode left;
	private TreeNode middle;
	private TreeNode right;

	public TreeNode(int value) {
		this.value = value;
		left = null;
		middle = null;
		right = null;
		symbolValue = null;
		symbolType = null;
		symbol = null;
	}

	public TreeNode(int value, String symbolValue) {
		this(value);
		this.symbolValue = symbolValue;
	}

	public TreeNode(int value, TreeNode left) {
		this(value);
		this.left = left;
	}
	public TreeNode(int value, TreeNode left, TreeNode right) {
		this(value);
		this.left = left;
		this.right = right;
	}

	public TreeNode(int value, TreeNode left, TreeNode middle, TreeNode right) {
		this(value, left, right);
		this.middle = middle;
	}

	public int Value() {
		return value;
	}

	public TreeNode Left() {
		return left;
	}

	public TreeNode Middle() {
		return middle;
	}

	public TreeNode Right() {
		return right;
	}

	public String SymbolValue() {
		return symbolValue;
	}

	public String SymbolType() {
		return symbolType;
	}

	public Symbol Symbol() {
		return symbol;
	}

	public void SetSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	public void SetValue(int value) {
		this.value = value;
	}

	public void SetLeft(TreeNode node) {
		this.left = node;
	}

	public void SetMiddle(TreeNode node) {
		this.middle = node;
	}

	public void SetRight(TreeNode node) {
		this.right = node;
	}

	public void SetSymbolValue(String symbolValue) {
		this.symbolValue = symbolValue;
	}

	public void SetSymbolValue(Token token) {
		this.symbolValue = token.Value();
	}

	public void SetSymbolType(int symbolType) {
		this.symbolType = Token.TPRINT[symbolType];
	}

	public void SetSymbolType(Token token) {
		this.symbolType = Token.TPRINT[token.Type()];
	}

	public void SetSymbolType(String symbolType) {
		this.symbolType = symbolType;
	}
}