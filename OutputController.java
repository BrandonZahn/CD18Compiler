// Student no: c3186200
//     Course: COMP3290

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class OutputController {

	private static final int UNDECLARED_VAR = 1, UNDEF_ARR_SIZE = 2, STRONG_TYPE = 3,
	ASSIGNMENT = 4, FUNC_PARAM_TYPE = 5, FUNC_CALL_PARAMS = 6, FUNC_RETURN = 7, VAR_ALREADY_USED = 8;

	private ArrayList<String> errorLines;
	private ArrayList<String> program;
	private PrintWriter listing;
	private int errorCount;

	public OutputController(ArrayList<String> program) {
		this.program = program;
		try {
			listing = new PrintWriter(new FileOutputStream("listing.lst"), true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		errorCount = 0;
		errorLines = new ArrayList<String>();

		int lineCount = 1;
		String programOut = "";
		for (String line : program) {
			programOut += lineCount + ": " + line;
			lineCount++;
		}
		listing.println(programOut);
		listing.close();
	}

	public int ErrorCount() {
		return errorCount;
	}

	public void CloseStream() {
		listing.close();
	}

	public void ListInvalidToken(Token badToken) {
		String error = "\n\nError at line " + badToken.Line() + ": column " + badToken.Column() + ";\n";
		String programLine = program.get(badToken.Line() - 1);
		error += programLine;
		String indicator = "\n";
		String terminalIndicator = "\n";
		for (int i = 0; i < badToken.Column() - 1; i++) {
			if (programLine.charAt(i) == '\t') {
				terminalIndicator += "        ";
				indicator += "    ";
			}
			else {
				terminalIndicator += " ";
				indicator += " ";
			}
		}
		terminalIndicator += "^\n";
		indicator += "^\n";

		listing.println(error + indicator +
			"\nTUNDF:\nLexical Error! Invalid character(s) " + badToken.ToString() + "\n");
		errorLines.add(error + terminalIndicator +
			"\nTUNDF:\nLexical Error! Invalid character(s) " + badToken.ToString() + "\n");
		errorCount++;
		listing.close();
	}

	public void ListInvalidNode(int nodeType, Token badToken) {
		String error = "\n\nError at line " + badToken.Line() + ": column " + badToken.Column() + ";\n";
		String programLine = program.get(badToken.Line() - 1);
		error += programLine;
		String indicator = "\n";
		String terminalIndicator = "\n";
		for (int i = 0; i < badToken.Column() - 1; i++) {
			if (programLine.charAt(i) == '\t') {
				terminalIndicator += "        ";
				indicator += "    ";
			}
			else {
				terminalIndicator += " ";
				indicator += " ";
			}
		}
		terminalIndicator += "^\n";
		indicator += "^\n";

		listing.println(error + indicator +
			"\nNUNDEF:\nSyntactical Error! Invalid node " + TreeNode.PRINTNODE[nodeType] +
			"\n" + "when parsing token " + badToken.ToString() + "\n");
		errorLines.add(error + terminalIndicator +
		"\nNUNDEF:\nSyntactical Error! Invalid node " + TreeNode.PRINTNODE[nodeType] +
		"\n" + "when parsing token " + badToken.ToString()+ "\n\n");


		errorCount++;
		listing.close();
	}

	public void ListSemanticError(int nodeType, int type, Token badToken) {
		String error = "\nError at line " + badToken.Line() + ": column " + badToken.Column() + ";\n";
		String programLine = program.get(badToken.Line() - 1);
		error += programLine;
		String indicator = "\n";
		String terminalIndicator = "\n";
		for (int i = 0; i < badToken.Column() - 1; i++) {
			if (programLine.charAt(i) == '\t') {
				terminalIndicator += "        ";
				indicator += "    ";
			}
			else {
				terminalIndicator += " ";
				indicator += " ";
			}
		}
		terminalIndicator += "^\n";
		indicator += "^\n";

		listing.println(error + indicator +
			"\nNUNDEF:\nSemantical Error! Invalid node " + TreeNode.PRINTNODE[nodeType] +
			"\n" + "when parsing token " + badToken.ToString());
		errorLines.add(error + terminalIndicator +
		"\nNUNDEF:\nSemantical Error! Invalid node " + TreeNode.PRINTNODE[nodeType] +
		"\n" + "when parsing token " + badToken.ToString() + "\n");
		
		switch (type) {
			case UNDECLARED_VAR:
				listing.println("Variable not declared");
				errorLines.add("Variable not declared");
				break;
			case UNDEF_ARR_SIZE:
				listing.println("Undefined array size");
				errorLines.add("Undefined array size");
				break;
			case STRONG_TYPE:
				listing.println("Strong typed error, type mismatch in expression");
				errorLines.add("Strong typed error, type mismatch in expression");
				break;
			case ASSIGNMENT:
				listing.println("Incorrect types when assigning");
				errorLines.add("Incorrect types when assigning");
				break;
			case FUNC_PARAM_TYPE:
				listing.println("Incorrect parameter types in function call");
				errorLines.add("Incorrect parameter types in function call");
				break;
			case FUNC_CALL_PARAMS:
				listing.println("Incorrect number of parameters in function call");
				errorLines.add("Incorrect number of parameters in function call");
				break;
			case FUNC_RETURN:
				listing.println("Function does not have a valid return statement");
				errorLines.add("Function does not have a valid return statement");
				break;
			case VAR_ALREADY_USED:
				listing.println("Variable already declared in this scope");
				errorLines.add("Variable already declared in this scope");
				break;
			default:
				break;
		}
		errorCount++;
		listing.close();
	}

	public String ErrorLines() {
		String errorString = "";
		for (String line : errorLines) {
			errorString += line;
		}
		return errorString;
	}
}