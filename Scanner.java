// Student no: c3186200
//     Course: COMP3290

import java.util.ArrayList;

public class Scanner {

	private ArrayList<ArrayList<Integer>> programClassification;
	private ArrayList<Integer> programLineLengths;
	private OutputController outputController;
	private ArrayList<String> program;
	private boolean started = false;
	private String lexemeClass = "";
	private int currentColumn = 0;
	private int currentLine = 0;
	private int lexemeStart = 0;
	private int lexemeLine = 0;

	public Scanner (ArrayList<String> program, OutputController outputController) {
		programClassification = new ArrayList<ArrayList<Integer>>();
		programLineLengths = new ArrayList<Integer>();
		this.outputController = outputController;
		this.program = program;

		// Reads in the progam into the member variable ArrayList
		for (String line : program) {
			ArrayList<Integer> lineClassification = new ArrayList<Integer>(classifyLine(line));
			programClassification.add(lineClassification);
			programLineLengths.add(line.length());
		}
	}

	public Token NextToken() {
		String lexeme = findLexeme();
		int tokenType = classifyLexeme(lexeme);
		if (lexemeClass == "specialType" || tokenType == 0)
			lexeme = "";

		Token token = new Token(tokenType, lexemeLine + 1, lexemeStart + 1, lexeme);
		
		if (token.Type() == Token.TUNDF)
			outputController.ListInvalidToken(token);

		return token;
	}

	private String findLexeme() {
		// Advance the cursor one step. If it is one step beyond the end of the line,
		// advance on to the next line of the program.
		int lineLength = programLineLengths.get(currentLine);
		if (currentColumn >= (lineLength - 1)) {
			currentLine++;
			currentColumn = 0;
			if (Eof())
				return "EOF";
			else
				lineLength = programLineLengths.get(currentLine);
		}
		else if (started)
			currentColumn++;
		else
			started = true;

		if (lineLength == 0)
			return findLexeme();

		ArrayList<Integer> lineClassification = 
			programClassification.get(currentLine);
		String line = program.get(currentLine);

		int i = currentColumn;
		lexemeStart = currentColumn;
		lexemeLine = currentLine;

		// Check for comments and whitespace. 
		// Increment the current column and search again
		if (lineClassification.get(i) == 6 || lineClassification.get(i) == 4)
			return findLexeme();
		// Check for String constants
		else if (lineClassification.get(i) == 10) {
			int charClass = 5;
			i++;
			while (charClass == 5 && i < (lineLength - 1)) {
				i++;
				charClass = lineClassification.get(i);
			}
			
			int classLength = i - currentColumn - 2;
			currentColumn = i;
			lexemeClass = "stringType";

			if (classLength == 2)
				return "";
			else
				return line.substring(lexemeStart + 1, i);
		}
		// Check for special characters
		else if (lineClassification.get(i) == 3) {
			if (i + 1 < lineLength && line.charAt(i + 1) == '=') {
				char charAtI = line.charAt(i);
				if (charAtI == '<' || charAtI == '>' || charAtI == '!' ||
						charAtI == '=' || charAtI == '+' || charAtI == '-' ||
						charAtI == '*' || charAtI == '/' || charAtI == '%') {
						currentColumn++;
						lexemeClass = "specialType";
						return line.substring(i, i + 2);
				}
			}
			lexemeClass = "specialType";
			return line.substring(i, i + 1);
		}
		// Check for identifiers
		else if (lineClassification.get(i) == 2) {
			int j = i + 1;

			while (j < lineLength) {
				if (lineClassification.get(j) == 2 || lineClassification.get(j) == 1)
					j++;
				else
					break;
			}
			int lexemeLength = j - i;
			if (lexemeLength != 1)
				currentColumn = i + lexemeLength - 1;

			lexemeClass = "identifierType";
			return line.substring(lexemeStart, i + lexemeLength);
		}
		// Check for numerals
		else if (lineClassification.get(i) == 1) {
			int j = i + 1;

			int dotSpot = 0;
			boolean isReal = false;
			boolean isBroked = false;
			while (j < lineLength) {
				if (lineClassification.get(j) == 1 || (lineClassification.get(j) == 3 &&
					line.charAt(j) == '.')) {
					if (line.charAt(j) == '.') {
						dotSpot = j;
						int k = j + 1;
						while (k < lineLength) {
							if (lineClassification.get(k) == 1)
								k++;
							else
								break;
						}
						if (k - j == 1) {
							isBroked = true;
							break;
						}
						else
							isReal = true;
					}
					j++;
				}
				else
					break;
			}
			
			int lexemeLength;
			if (isBroked)
				lexemeLength = dotSpot - i;
			else
				lexemeLength = j - i;
			
			if (lexemeLength != 1)
				currentColumn = i + lexemeLength - 1;

			if (isReal)
				lexemeClass = "realType";
			else
				lexemeClass = "integerType";

			return line.substring(lexemeStart, i + lexemeLength);
		}
		// Undefined character (class 0)
		else {
			int j = i + 1;
			while (j < lineLength) {
				if (lineClassification.get(j) == 0)
					j++;
				else
					break;
			}
			int lexemeLength = j - i;
			if (lexemeLength != 1)
				currentColumn = i + lexemeLength - 1;

			lexemeClass = "invalidType";
			return line.substring(lexemeStart, i + lexemeLength);
		}
	}

	private int classifyLexeme(String lexeme) {
		if			(lexeme == "EOF" && Eof()) return 0;
		else if (lexeme.equals(",")				 )	return 32;
		else if (lexeme.equals("[")				 )	return 33;
		else if (lexeme.equals("]")				 )	return 34;
		else if (lexeme.equals("(")				 )	return 35;
		else if (lexeme.equals(")")				 )	return 36;
		else if (lexeme.equals("=")				 )	return 37;
		else if (lexeme.equals("+")				 )	return 38;
		else if (lexeme.equals("-")				 )	return 39;
		else if (lexeme.equals("*")				 )	return 40;
		else if (lexeme.equals("/")				 )	return 41;
		else if (lexeme.equals("%")				 )	return 42;
		else if (lexeme.equals("^")				 )	return 43;
		else if (lexeme.equals("<")				 )	return 44;
		else if (lexeme.equals(">")				 )	return 45;
		else if (lexeme.equals(":")				 )	return 46;
		else if (lexeme.equals("<=")			 )	return 47;
		else if (lexeme.equals(">=")			 )	return 48;
		else if (lexeme.equals("!=")			 )	return 49;
		else if (lexeme.equals("==")			 )	return 50;
		else if (lexeme.equals("+=")			 )	return 51;
		else if (lexeme.equals("-=")			 )	return 52;
		else if (lexeme.equals("*=")			 )	return 53;
		else if (lexeme.equals("/=")			 )	return 54;
		else if (lexeme.equals("%=")			 )	return 55;
		else if (lexeme.equals(";")				 )	return 56;
		else if (lexeme.equals(".")				 )	return 57;
		else if (lexemeClass == "identifierType") 	return 58;
		else if (lexemeClass == "integerType")			return 59;
		else if (lexemeClass == "realType")				return 60;
		else if (lexemeClass == "stringType")			return 61;
		else																	return 62;
	}

	private ArrayList<Integer> classifyLine(String line) {
		ArrayList<Integer> charClass = new ArrayList<Integer>();
		boolean commentExists = line.contains("/--");
		int commentIndex = line.indexOf("/--");
		boolean quotationExists = line.contains("\"");
		boolean stringConstExists = false;
		boolean stringNotClosed = false;
		int invalidStringStart = 0;

		ArrayList<Integer> quotationCount = new ArrayList<Integer>();
		// Check if multiple of two quotation marks exist in the line for a valid String constants
		if (quotationExists) {

			for (int i = 0; i < line.length(); i++) {
				char charAtI = line.charAt(i);
				if (!(commentExists && i >= commentIndex) && charAtI == '\"')
					quotationCount.add(i);
			}
			
			if (quotationCount.size() % 2 != 0) {
				stringNotClosed = true;
				invalidStringStart = quotationCount.get(quotationCount.size() - 1);
			}
			
			if (quotationCount.size() >= 2)
				stringConstExists = true;
			}

		int quotationIterator = 0;
		boolean finishedStrings = false;
		for (int i = 0; i < line.length(); i++) {
			char charAtI = line.charAt(i);
			// Comment class
			if (commentExists && i >= commentIndex)
				charClass.add(6);
			// String constant class
			else if (stringConstExists && !finishedStrings && i >= quotationCount.get(quotationIterator) && i <= quotationCount.get(quotationIterator + 1)) {
				if (i == quotationCount.get(quotationIterator) || i == quotationCount.get(quotationIterator + 1))
					charClass.add(10);
				else
					charClass.add(5);
				
				if (i == quotationCount.get(quotationIterator + 1) && quotationCount.size() > (quotationIterator + 3))
					quotationIterator += 2;
				else if (i == quotationCount.get(quotationIterator + 1) && quotationCount.size() == (quotationIterator + 2))
					finishedStrings = true;
			}
			// String is not closed properly, invalidate
			else if (stringNotClosed && i >= invalidStringStart)
				charClass.add(0);
			// Whitespace class
			else if (charAtI == ' ' || charAtI == '\t' ||
				charAtI == '\r' || charAtI == '\n')
				charClass.add(4);
			// Special character class
			else if (charAtI == ';' || charAtI == '[' || charAtI == ']' ||
				charAtI == ',' || charAtI == '(' || charAtI == ')' || charAtI == '.' ||
				charAtI == '=' || charAtI == '+' || charAtI == '-' || charAtI == ':' ||
				charAtI == '*' || charAtI == '/' || charAtI == '%' || 
				(charAtI == '!' && line.charAt(i + 1) == '=') ||
				charAtI == '^' || charAtI == '<' || charAtI == '>')
				charClass.add(3);
			// Character is alphabetical
			else if (Character.isLetter(charAtI))
				charClass.add(2);
			// Character is numerical
			else if (Character.isDigit(charAtI))
				charClass.add(1);
			// Character class
			else
				charClass.add(0);
		}
		return charClass;
	}

	// checks if the program has reached the end of file
	public boolean Eof() {
		if (currentLine >= program.size())
			return true;
		else
			return false;
	}
}