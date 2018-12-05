// Student no: c3186200
//     Course: COMP3290

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	private Map<String, Symbol> globals;
	private Map<String, Symbol> main;
	private Map<String, Map<String, Symbol>> funcs;
	private Map<String, String> funcsReturn;
	private Map<String, Symbol> structs;
	private Map<String, Symbol> types;
	private Map<String, Symbol> constants;
	
	public SymbolTable() {
		globals = new HashMap<String, Symbol>();
		main = new HashMap<String, Symbol>();
		funcs = new HashMap<String, Map<String, Symbol>>();
		funcsReturn = new HashMap<String, String>();
		structs = new HashMap<String, Symbol>();
		types = new HashMap<String, Symbol>();
		constants = new HashMap<String, Symbol>();
	}

	public void AddConstant(String constantId, Symbol constant) {
		constants.put(constantId, constant);
	}

	public boolean ConstantExists(String key) {
		return constants.containsKey(key);
	}

	public Collection<Symbol> GetConstants() {
		return constants.values();
	}

	public boolean FuncReturnExists(String scope) {
		return (funcsReturn.get(scope) != null);
	}

	public void SetFuncReturns(String scope, String type) {
		funcsReturn.put(scope, type);
	}

	public String FuncReturns(String scope) {
		return funcsReturn.get(scope);
	}

	public void AddStruct(String structId, Symbol struct) {
		structs.put(structId, struct);
	}

	public boolean IsScope(String value) {
		return funcs.containsKey(value);
	}

	public boolean StructExists(String structId) {
		return structs.containsKey(structId);
	}

	public void AddType(String typeId, Symbol typeArr) {
		types.put(typeId, typeArr);
		globals.put(typeId, typeArr);
	}

	public boolean TypeExists(String typeId) {
		return types.containsKey(typeId);
	}

	public void RemoveSymbol(String scope, String key) {
		if (scope.equals("@globals"))
			globals.remove(key);
		else if (scope.equals("@main"))
			main.remove(key);
		else
			funcs.get(scope).remove(key);
	}

	public int TypeSize(String typeId) {
		Symbol type = types.get(typeId);
		Symbol struct = structs.get(type.Type());

		if (type.Attributes().get(0).Value().contains("@"))
			return (struct.Attributes().size() * Integer.parseInt(type.Attributes().get(0).Value().substring(1)));
		else
			return (struct.Attributes().size() * Integer.parseInt(type.Attributes().get(0).Attributes().get(0).Value().substring(1)));
	}

	public int StructSize(String typeId) {
		Symbol type = types.get(typeId);
		Symbol struct = structs.get(type.Type());
		return (struct.Attributes().size());
	}

	public boolean TypeMemberExists(String typeId, String structMember) {
		Symbol type = types.get(typeId);
		Symbol struct = structs.get(type.Type());
		boolean exists = false;
		for (Symbol member : struct.Attributes()) {
			if (structMember.equals(member.Value())) {
				exists = true;
				break;
			}
		}
		return exists;
	}

	public int TypeMemberIndex(String typeId, String structMember) {
		Symbol type = types.get(typeId);
		Symbol struct = structs.get(type.Type());
		int index = 0;
		for (Symbol member : struct.Attributes()) {
			if (structMember.equals(member.Value()))
				break;
			index++;
		}
		return index;
	}

	public String StructMemberType(String typeId, String structMember) {
		Symbol type = types.get(typeId);
		Symbol struct = structs.get(type.Type());
		if (type != null && struct != null) {
			for (Symbol member : struct.Attributes()) {
				if (structMember.equals(member.Value()))
					return member.Type();
			}
		}
		return null;
	}

	public boolean StructMemberExists(String structId, String structMember) {
		if (structs.get(structId) != null && structs.get(structId).Attributes() != null) {
			for (Symbol member : structs.get(structId).Attributes()) {
				if (member != null && structMember.equals(member.Value()))
					return true;
			}
		}
		return false;
	}

	public boolean SymbolExists(String scope, String key) {
		if (scope.equals("@globals"))
			return globals.containsKey(key);
		else if (scope.equals("@main")) {
			if (globals.containsKey(key))
				return true;
			else
				return main.containsKey(key);
		}
		else {
			if (globals.containsKey(key))
				return true;
			else
				return funcs.get(scope).containsKey(key);
		}
	}

	public Symbol RetrieveSymbol(String scope, String key) {
		if (scope.equals("@globals"))
			return globals.get(key);
		else if (scope.equals("@main")) {
			if (main.containsKey(key))
				return main.get(key);
			else
				return globals.get(key);
		}
		else {
			if (funcs.get(scope).containsKey(key))
				return funcs.get(scope).get(key);
			else
				return globals.get(key);
		}
	}

	public void AddGlobalSymbol(String key, Symbol symbol) {
		globals.put(key, symbol);
	}

	public void AddSymbol(String scope, String key, Symbol symbol) {
		if (scope.equals("@globals"))
			globals.put(key, symbol);
		else if (scope.equals("@main"))
			main.put(key, symbol);
		else
			funcs.get(scope).put(key, symbol);
	}

	public boolean ScopeExists(String key) {
		return funcs.containsKey(key);
	}

	public void AddScope(String scope) {
		funcs.put(scope, new HashMap<String, Symbol>());
	}

	public void AddFuncSymbol(String scope, Symbol funcDefinition) {
		funcs.get(scope).put(scope, funcDefinition);
	}

	public void AddReference(String scope, String key, Symbol reference) {
		if (scope.equals("@globals") || types.get(key) != null)
			globals.get(key).AddReference(reference);
		else if (scope.equals("@main")) {
			if (main.get(key) == null)
				globals.get(key).AddReference(reference);
			else
				main.get(key).AddReference(reference);
		}
		else if (funcs.get(scope).get(key) != null)
			funcs.get(scope).get(key).AddReference(reference);
		else
			globals.get(key).AddReference(reference);
	}
}