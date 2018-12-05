// Student no: c3186200
//     Course: COMP3290

public class Token {

	public static final int
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

	public static final String TPRINT[] = {
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

	private int type;
	private int line;
	private int column;
	private String value;

	public Token(int type, int line, int column, String value) {
		this.type = type;
		this.line = line;
		this.column = column;
		this.value = value;

		if (type == TIDEN) {
			int val = checkKeywords(value);
			if (val > 0) {
				this.type = val;
				this.value = "";
			}
		}
	}

	public int Type() {
		return type;
	}

	public int Line() {
		return line;
	}

	public int Column() {
		return column;
	}

	public String Value() {
		return value;
	}

	public String ToString() {
		String str = TPRINT[type];
		
		if (value == "")
			return str;
		else if (type != TUNDF)
			str += " " + value;
		else {
			str += " ";
			for (int i = 0; i < value.length(); i++) {
				char ch = value.charAt(i);
				int j = (int)ch;
				if (j <= 31 || j >= 127)
					str += "\\" + j;
				else
					str += ch;
			}
		}
		return str;
	}

	private int checkKeywords(String s) {
		s = s.toLowerCase();
		if ( s.equals("cd18")      )	return TCD18;
		if ( s.equals("constants") )	return TCONS;
		if ( s.equals("types")     )	return TTYPS;
		if ( s.equals("is")        )	return TIS;
		if ( s.equals("arrays")    )	return TARRS;

		if ( s.equals("main")      )	return TMAIN;
		if ( s.equals("begin")     )	return TBEGN;
		if ( s.equals("end")       )	return TEND;
		if ( s.equals("array")     )	return TARAY;
		if ( s.equals("of")        )	return TOF;
		if ( s.equals("func")      )	return TFUNC;
		if ( s.equals("void")      )	return TVOID;
		if ( s.equals("const")     )	return TCNST;

		if ( s.equals("integer")   )	return TINTG;
		if ( s.equals("real")      )	return TREAL;
		if ( s.equals("boolean")   )	return TBOOL;

		if ( s.equals("for")       )	return TFOR;
		if ( s.equals("repeat")    )	return TREPT;
		if ( s.equals("until")     )	return TUNTL;
		if ( s.equals("if")        )	return TIFTH;
		if ( s.equals("else")      )	return TELSE;

		if ( s.equals("input")     )	return TINPT;
		if ( s.equals("print")     )	return TPRIN;
		if ( s.equals("printline") )	return TPRLN;
		if ( s.equals("return")    )	return TRETN;

		if ( s.equals("and")       )	return TAND;
		if ( s.equals("or")        )	return TOR;
		if ( s.equals("xor")       )	return TXOR;
		if ( s.equals("not")       )	return TNOT;
		if ( s.equals("true")      )	return TTRUE;
		if ( s.equals("false")     )	return TFALS;

		return -1;
	}
}