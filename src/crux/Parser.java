package crux;

import java.io.IOException;

import crux.Token.Kind;

public class Parser
{
	private Scanner scanner;
	private Token current;

	private ErrorReport error;

	private SymbolTable symbolTable;

	public Parser(Scanner scanner)
	{
		this.scanner = scanner;
		this.error = new ErrorReport();

		initSymbolTable();
	}

	public void parse()
	{
		try
		{
			program();
		}
		catch (QuitParseException e)
		{
			error.reportSyntaxError();
		}
	}

	// program := declaration-list EOF
	private void program()
	{
		nextToken();
		declarationList();
		expect(Token.Kind.EOF);
	}

	// declaration-list := { declaration }
	private void declarationList()
	{
		while (have(NonTerminal.DECLARATION_LIST))
		{
			declaration();
		}
	}

	// declaration := variable-declaration | array-declaration |
	// function-definition
	private void declaration()
	{
		if (have(NonTerminal.VARIABLE_DECLARATION))
		{
			variableDeclaration();
		}
		else if (have(NonTerminal.FUNCTION_DEFINITION))
		{
			functionDefinition();
		}
		else if (have(NonTerminal.ARRAY_DECLARATION))
		{
			arrayDeclaration();
		}
	}

	// variable-declaration := "var" IDENTIFIER ":" type ";"
	private void variableDeclaration()
	{
		if (accept(NonTerminal.VARIABLE_DECLARATION))
		{
			tryDeclareSymbol(current);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.COLON);
			type();
			expect(Token.Kind.SEMICOLON);
		}
	}

	// function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type
	// statement-block
	private void functionDefinition()
	{
		if (accept(NonTerminal.FUNCTION_DEFINITION))
		{
			tryDeclareSymbol(current);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.OPEN_PAREN);
			enterScope();
			parameterList();
			expect(Token.Kind.CLOSE_PAREN);
			expect(Token.Kind.COLON);
			type();
			statementBlock();
			exitScope();
		}
	}

	// array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "["
	// INTEGER "]" } ";"
	private void arrayDeclaration()
	{
		if (accept(NonTerminal.ARRAY_DECLARATION))
		{
			tryDeclareSymbol(current);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.COLON);
			type();
			expect(Token.Kind.OPEN_BRACKET);
			expect(Token.Kind.INTEGER);
			expect(Token.Kind.CLOSE_BRACKET);
			while (accept(Token.Kind.OPEN_BRACKET))
			{
				expect(Token.Kind.INTEGER);
				expect(Token.Kind.CLOSE_BRACKET);
			}
			expect(Token.Kind.SEMICOLON);
		}
	}

	// statement-block := "{" statement-list "}"
	private void statementBlock()
	{
		expect(Token.Kind.OPEN_BRACE);
		statementList();
		expect(Token.Kind.CLOSE_BRACE);
	}

	// statement-list := { statement }
	private void statementList()
	{
		while (have(NonTerminal.STATEMENT))
		{
			statement();
		}
	}

	// statement := variable-declaration | call-statement | assignment-statement
	// | if-statement | while-statement | return-statement
	private void statement()
	{
		if (have(NonTerminal.VARIABLE_DECLARATION))
		{
			variableDeclaration();
		}
		else if (have(NonTerminal.CALL_STATEMENT))
		{
			callStatement();
		}
		else if (have(NonTerminal.ASSIGNMENT_STATEMENT))
		{
			assignmentStatement();
		}
		else if (have(NonTerminal.IF_STATEMENT))
		{
			ifStatement();
		}
		else if (have(NonTerminal.WHILE_STATEMENT))
		{
			whileStatement();
		}
		else if (have(NonTerminal.RETURN_STATEMENT))
		{
			returnStatement();
		}
	}

	// call-statement := call-expression ";"
	private void callStatement()
	{
		callExpression();
		expect(Token.Kind.SEMICOLON);
	}

	// call-expression := "::" IDENTIFIER "(" expression-list ")"
	private void callExpression()
	{
		if (accept(NonTerminal.CALL_EXPRESSION))
		{
			tryResolveSymbol(current);
			expect(Token.Kind.IDENTIFIER);
			expect(Token.Kind.OPEN_PAREN);
			expressionList();
			expect(Token.Kind.CLOSE_PAREN);
		}
	}

	// assignment-statement := "let" designator "=" expression0 ";"
	private void assignmentStatement()
	{
		if (accept(NonTerminal.ASSIGNMENT_STATEMENT))
		{
			designator();
			expect(Token.Kind.ASSIGN);
			expression0();
			expect(Token.Kind.SEMICOLON);
		}
	}

	// designator := IDENTIFIER { "[" expression0 "]" }
	private void designator()
	{
		if (have(NonTerminal.DESIGNATOR))
		{
			tryResolveSymbol(current);
			nextToken();
			while (accept(Token.Kind.OPEN_BRACKET))
			{
				expression0();
				expect(Token.Kind.CLOSE_BRACKET);
			}
		}
	}

	// if-statement := "if" expression0 statement-block [ "else" statement-block
	// ]
	private boolean ifStatement()
	{
		if (accept(NonTerminal.IF_STATEMENT))
		{
			enterScope();
			expression0();
			statementBlock();
			if (accept(Token.Kind.ELSE))
			{
				statementBlock();
			}
			exitScope();
		}
		return false;
	}

	// while-statement := "while" expression0 statement-block
	private void whileStatement()
	{
		if (accept(NonTerminal.WHILE_STATEMENT))
		{
			enterScope();
			expression0();
			statementBlock();
			exitScope();
		}
	}

	// return-statement := "return" expression0 ";"
	private void returnStatement()
	{
		if (accept(NonTerminal.RETURN_STATEMENT))
		{
			expression0();
			expect(Token.Kind.SEMICOLON);
		}
	}

	// expression-list := [ expression0 { "," expression0 } ]
	private void expressionList()
	{
		if (have(NonTerminal.EXPRESSION0))
		{
			do
			{
				expression0();
			} while (accept(Token.Kind.COMMA));
		}
	}

	// expression0 := expression1 [ op0 expression1 ]
	private void expression0()
	{
		expression1();
		if (accept(NonTerminal.OP0))
		{
			expression1();
		}
	}

	// expression1 := expression2 { op1 expression2 }
	private void expression1()
	{
		expression2();
		while (accept(NonTerminal.OP1))
		{
			expression2();
		}
	}

	// expression2 := expression3 { op2 expression3 }
	private void expression2()
	{
		expression3();
		while (accept(NonTerminal.OP2))
		{
			expression3();
		}
	}

	// expression3 := "not" expression3 | "(" expression0 ")" | designator |
	// call-expression | literal
	private void expression3()
	{
		if (have(NonTerminal.EXPRESSION3))
		{
			if (accept(Token.Kind.NOT))
			{
				expression3();
			}
			else if (accept(Token.Kind.OPEN_PAREN))
			{
				expression0();
				expect(Token.Kind.CLOSE_PAREN);
			}
			else if (have(NonTerminal.DESIGNATOR))
			{
				designator();
			}
			else if (have(NonTerminal.CALL_EXPRESSION))
			{
				callExpression();
			}
			else if (have(NonTerminal.LITERAL))
			{
				literal();
			}
		}
	}

	// literal := INTEGER | FLOAT | TRUE | FALSE
	private void literal()
	{
		if (accept(NonTerminal.LITERAL))
		{

		}
	}

	// parameter-list := [ parameter { "," parameter } ]
	private void parameterList()
	{
		do
		{
			if (have(NonTerminal.PARAMETER))
			{
				parameter();
			}
		} while (accept(Token.Kind.COMMA));
	}

	// parameter := IDENTIFIER ":" type
	private void parameter()
	{
		if (have(NonTerminal.PARAMETER))
		{
			tryDeclareSymbol(current);
			nextToken();
			expect(Token.Kind.COLON);
			type();
		}
	}

	// type := IDENTIFIER
	private void type()
	{
		expect(Token.Kind.IDENTIFIER);
	}

	private boolean have(NonTerminal nonterminal)
	{
		return nonterminal.firstSet().contains(current.kind);
	}

	private boolean have(Kind kind)
	{
		return current.isToken(kind);
	}

	private boolean accept(NonTerminal nonterminal)
	{
		if (have(nonterminal))
		{
			nextToken();
			return true;
		}
		return false;
	}

	private boolean accept(Kind kind)
	{
		if (have(kind))
		{
			nextToken();
			return true;
		}
		return false;
	}

	private boolean expect(Kind kind)
	{
		if (accept(kind))
		{
			return true;
		}
		String msg = error.reportSyntaxError(kind);
		throw new QuitParseException(msg);
	}

	private boolean expect(NonTerminal nonterminal)
	{
		if (accept(nonterminal))
		{
			return true;
		}
		String msg = error.reportSyntaxError(nonterminal);
		throw new QuitParseException(msg);
	}

	private void nextToken()
	{
		try
		{
			current = scanner.next();
		}
		catch (IOException e)
		{

		}
	}

	public boolean hasError()
	{
		return error.hasError();
	}

	public String errorReport()
	{
		return error.toString();
	}

	private void initSymbolTable()
	{
		symbolTable = new SymbolTable();
		symbolTable.setParent(null);
		symbolTable.setDepth(0);

		symbolTable.insert("readInt");
		symbolTable.insert("readFloat");
		symbolTable.insert("printBool");
		symbolTable.insert("printInt");
		symbolTable.insert("printFloat");
		symbolTable.insert("println");
	}

	private void enterScope()
	{
		SymbolTable table = new SymbolTable();
		table.setDepth(symbolTable.getDepth() + 1);
		table.setParent(symbolTable);
		symbolTable = table;
	}

	private void exitScope()
	{
		symbolTable = symbolTable.getParent();
	}

	private Symbol tryResolveSymbol(Token ident)
	{
		assert (ident.isToken(Token.Kind.IDENTIFIER));
		String name = ident.lexeme;
		try
		{
			return symbolTable.lookup(name);
		}
		catch (SymbolNotFoundError e)
		{
			String message = reportResolveSymbolError(name, ident.lineNumber, ident.charPosition);
			return new ErrorSymbol(message);
		}
	}

	private String reportResolveSymbolError(String name, int lineNum, int charPos)
	{
		return error.reportResolveSymbolError(lineNum, charPos, name);
	}

	private Symbol tryDeclareSymbol(Token ident)
	{
		assert (ident.isToken(Token.Kind.IDENTIFIER));
		String name = ident.lexeme;
		try
		{
			return symbolTable.insert(name);
		}
		catch (RedeclarationError re)
		{
			String message = reportDeclareSymbolError(name, ident.lineNumber, ident.charPosition);
			return new ErrorSymbol(message);
		}
	}

	private String reportDeclareSymbolError(String name, int lineNum, int charPos)
	{
		return error.reportDeclareSymbolError(lineNum, charPos, name);
	}
	
	private class QuitParseException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public QuitParseException(String msg)
		{
			super(msg);
		}
	}

	private class ErrorReport
	{
		private StringBuffer buffer = new StringBuffer();
		private final String expectedResponse = "SyntaxError(%d,%d)[Expected %s but got %s.]";
		private final String tokenResponse = "SyntaxError(%d,%d)[Expected a token from %s but got %s.]";
		private final String syntaxResponse = "SyntaxError(%d,%d)[Could not complete parsing.]";

		public boolean hasError()
		{
			return buffer.length() != 0;
		}

		public String toString()
		{
			return buffer.toString();
		}

		private void reportSyntaxError()
		{
			String message = String.format(syntaxResponse, current.lineNumber, current.charPosition);
			buffer.append(message);
		}

		private String reportSyntaxError(NonTerminal nonTerminal)
		{
			String message = String.format(tokenResponse, current.lineNumber, current.charPosition, nonTerminal.name(), current.kind);
			buffer.append(message + "\n");
			return message;
		}

		private String reportSyntaxError(Token.Kind kind)
		{
			String message = String.format(expectedResponse, current.lineNumber, current.charPosition, kind, current.kind);
			buffer.append(message + "\n");
			return message;
		}

		private String reportResolveSymbolError(int lineNumber, int charPosition, String name)
		{
			String message = "ResolveSymbolError(" + lineNumber + "," + charPosition + ")[Could not find " + name + ".]";
			buffer.append(message + "\n");
			buffer.append(symbolTable.toString() + "\n");
			return message;
		}

		private String reportDeclareSymbolError(int lineNumber, int charPosition, String name)
		{
			String message = "DeclareSymbolError(" + lineNumber + "," + charPosition + ")[" + name + " already exists.]";
			buffer.append(message + "\n");
			buffer.append(symbolTable.toString() + "\n");
			return message;
		}
	}
}
