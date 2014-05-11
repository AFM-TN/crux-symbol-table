package crux;

public class Symbol
{
	private static final String response = "Symbol(%s)";
	private String name;
	
	public Symbol(String name)
	{
		this.name = name;
	}
	
	public String name()
	{
		return name;
	}
	
	public String toString()
	{
		return String.format(response, name);
	}
	
	public static Symbol newError(String message)
	{
		return new ErrorSymbol(message);
	}
}

class ErrorSymbol extends Symbol
{
	public ErrorSymbol(String message)
	{
		super(message);
	}
}
