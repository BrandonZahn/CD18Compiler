import java.util.ArrayList;

// Student no: c3186200
//     Course: COMP3290

public class Symbol {

	private String value;
	private String type;
	private int line;
	private int column;
	private int base; // The base pointer used when calculating offsets
	private int offset; // the word offset of this variable in memory
	private ArrayList<Symbol> attributes;
	private ArrayList<Symbol> references;

	public Symbol() {
		value = null;
		type = null;
		line = -1;
		column = -1;
		base = 0;
		offset = 0;
		attributes = new ArrayList<Symbol>();
		references = new ArrayList<Symbol>();
	}

	public Symbol(Token token) {
		value = null;
		type = null;
		line = token.Line();
		column = token.Column();
		base = 0;
		offset = 0;
		attributes = new ArrayList<Symbol>();
		references = new ArrayList<Symbol>();
	}

	public void SetValue(String value) {
		this.value = value;
	}

	public String Value() {
		return value;
	}

	public void SetType(String type) {
		this.type = type;
	}

	public String Type() {
		return type;
	}

	public int Line() {
		return line;
	}

	public int Column() {
		return column;
	}

	public int Base() {
		return base;
	}

	public void SetBase(int base) {
		this.base = base;
	}

	public void SetOffset(int address) {
		this.offset = address;
	}

	public int Offset() {
		return offset;
	}

	public void SetAttributes(ArrayList<Symbol> attributes) {
		this.attributes = attributes;
	}

	public void AddAttribute(Symbol attribute) {
		attributes.add(attribute);
	}

	public ArrayList<Symbol> Attributes() {
		return attributes;
	}

	public void SetReferences(ArrayList<Symbol> references) {
		this.references = references;
	}

	public void AddReference(Symbol reference) {
		references.add(reference);
	}

	public ArrayList<Symbol> References() {
		return references;
	}
}