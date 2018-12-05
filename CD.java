// Student no: c3186200
//     Course: COMP3290

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;


public class CD {
	public static void main(String args[]) {
		Compiler compiler = new Compiler(args[0]);
		// Scan the input file
		if (compiler.ScanProgram()) {
			// Parse the token stream if no errors
			if (compiler.ParseProgram()) {
				// Generate the code
				compiler.CloseOutputController();
				compiler.ConfirmNoErrors();
				compiler.GenerateCode();
				compiler.PrintMachineCode();
			}
			else
				compiler.PrintErrors();
		}
		else
			compiler.PrintErrors();
	}
}