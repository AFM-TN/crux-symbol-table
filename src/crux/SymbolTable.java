package crux;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable
{
	private SymbolTable parent;
	private Map<String, Symbol> table;
	private int depth = 0;

	public SymbolTable()
	{
		parent = null;
		table = new LinkedHashMap<String, Symbol>();
	}

	public SymbolTable getParent()
	{
		return parent;
	}

	public void setParent(SymbolTable parent)
	{
		this.parent = parent;
	}

	public Symbol lookup(String name) throws SymbolNotFoundError
	{
		if (table.containsKey(name))
		{
			return table.get(name);
		}

		if (parent != null)
		{
			return parent.lookup(name);
		}

		throw new SymbolNotFoundError(name);
	}

	public Symbol insert(String name) throws RedeclarationError
	{
		if (table.containsKey(name))
		{
			throw new RedeclarationError(new Symbol(name));
		}
		Symbol symbol = new Symbol(name);
		table.put(name, symbol);
		return symbol;
	}

	public void setDepth(int depth)
	{
		this.depth = depth;
	}

	public int getDepth()
	{
		return depth;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if (parent != null)
		{
			sb.append(parent.toString());
		}

		String indent = new String();
		for (int i = 0; i < depth; i++)
		{
			indent += "  ";
		}

		for (Symbol s : table.values())
		{
			sb.append(indent + s.toString() + "\n");
		}
		return sb.toString();
	}

}

class SymbolNotFoundError extends Error
{
	private static final long serialVersionUID = 1L;
	private String name;

	SymbolNotFoundError(String name)
	{
		this.name = name;
	}

	public String name()
	{
		return name;
	}
}

class RedeclarationError extends Error
{
	private static final long serialVersionUID = 1L;

	public RedeclarationError(Symbol sym)
	{
		super("Symbol " + sym + " being redeclared.");
	}
}
