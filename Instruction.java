import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Instruction {

	private int opcode;
	private Symbol operand;

	public Instruction(int opcode) {
		this.opcode = opcode;
		operand = null;
	}

	public Instruction(int opcode, Symbol operand) {
		this.opcode = opcode;
		this.operand = operand;
	}

	public int Opcode() {
		return opcode;
	}

	public Symbol Operand() {
		return operand;
	}
}