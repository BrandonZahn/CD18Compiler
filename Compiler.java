// Student no: c3186200
//     Course: COMP3290

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Compiler {
	private SymbolTable symbolTable;
	private OutputController outputController;
	private ArrayList<Token> tokenStream;
	private ArrayList<String> program;
	private Scanner scanner;
	private Parser parser;
	private TreeNode ASTRoot;
	private CodeGenerator codeGenerator;
	private String modFileName;

	public Compiler(String inputFileName) {
		String content = "";
		try {
			FileReader file = new FileReader(inputFileName);
			modFileName = inputFileName.substring(0, inputFileName.indexOf('.'));
			int i;
			while ((i = file.read()) != -1) {
				content += (char)i;
			}
			file.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Reads in the program from content String
		program = new ArrayList<String>();
		int programLength = content.length();
		int i = 0;
		do {
			int lineCol = content.indexOf("\n", i);
			if (lineCol == -1) {
				program.add(content.substring(i, programLength));
				i = programLength;
			}
			else {
				program.add(content.substring(i, lineCol));
				i = lineCol + 1;
			}
		} while (i < programLength);
	}

	public void CloseOutputController() {
		outputController.CloseStream();
	}

	public boolean ScanProgram() {
		tokenStream = new ArrayList<Token>();
		outputController = new OutputController(program);
		scanner = new Scanner(program, outputController);
		
		while (!scanner.Eof() && outputController.ErrorCount() == 0) {
			tokenStream.add(scanner.NextToken());
		}

		return (outputController.ErrorCount() == 0);
	}

	public boolean ParseProgram() {
		symbolTable = new SymbolTable();
		parser = new Parser(tokenStream, outputController, symbolTable);
		ASTRoot = parser.BuildSyntaxTree();
		return (outputController.ErrorCount() == 0);
	}

	public void ConfirmNoErrors() {
		System.out.println(modFileName + " compiled successfully");
	}

	public void GenerateCode() {
		codeGenerator = new CodeGenerator(modFileName, ASTRoot, symbolTable);
		codeGenerator.Generate();
	}

	public void PrintTokenStream() {
		int outputLineLength = 0;
		for (int i = 0; i < tokenStream.size(); i++) {
			Token token = tokenStream.get(i);
			String outputToken = token.ToString() + " ";
			if (outputToken.length() > 7) {
				int remainder = 7 - (outputToken.length() % 7);
				for (int j = 0; j < remainder; j++) {
					outputToken += " ";
				}
			}

			System.out.print(outputToken);
			
			outputLineLength += outputToken.length();
			if (outputLineLength >= 60) {
				System.out.print("\n");
				outputLineLength = 0;
			}
		}
		System.out.print("\n");
	}

	public void PrintSyntaxTree() {
		parser.PrintSyntaxTree(System.out);
	}

	public void PrintMachineCode() {
		System.out.println(codeGenerator.outputString());
	}

	public void PrintErrors() {
		System.out.println("\n" + outputController.ErrorLines() + "\n");
		System.out.println("Found " + outputController.ErrorCount() + " errors");
	}
}