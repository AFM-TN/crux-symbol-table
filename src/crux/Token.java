package crux;

public class Token {

	protected static enum Kind {
		ADD("+"), AND("and"), ARRAY("array"), ASSIGN("="), CALL("::"), CLOSE_BRACE(
				"}"), CLOSE_BRACKET("]"), CLOSE_PAREN(")"), COLON(":"), COMMA(
				","), DIV("/"), ELSE("else"), EOF(), EQUAL("=="), ERROR(), FALSE(
				"false"), FLOAT(), FUNC("func"), GREATER_EQUAL(">="), GREATER_THAN(
				">"), IDENTIFIER(), IF("if"), INTEGER(), LESS_THAN("<"), LESSER_EQUAL(
				"<="), LET("let"), MUL("*"), NOT("not"), NOT_EQUAL("!="), OPEN_BRACE(
				"{"), OPEN_BRACKET("["), OPEN_PAREN("("), OR("or"), RETURN(
				"return"), SEMICOLON(";"), SUB("-"), TRUE("true"), VAR("var"), WHILE(
				"while");

		private String lexeme;

		private Kind() {
			this.lexeme = "";
		}

		private Kind(String lexeme) {
			this.lexeme = lexeme;
		}
	}

	protected Kind kind;
	protected int lineNumber;
	protected int charPosition;
	protected String lexeme;

	private static final String responseNoValue = "%s(lineNum:%d, charPos:%d)";
	private static final String responseValue = "%s(%s)(lineNum:%d, charPos:%d)";

	private static final String error = "%s(Unexpected character: %s)(lineNum:%d, charPos:%d)";

	private Token(Kind kind, String lexeme, int lineNumber, int charPosition) {
		this.kind = kind;
		this.lexeme = lexeme;
		this.lineNumber = lineNumber;
		this.charPosition = charPosition;
	}

	private Token(Kind kind, int lineNumber, int charPosition) {
		this(kind, kind.lexeme, lineNumber, charPosition);
	}

	private static Kind findKind(String lexeme) {
		for (Kind kind : Kind.values()) {
			if (kind.lexeme.equals(lexeme)) {
				return kind;
			}
		}

		return null;
	}

	private static boolean isInteger(String lexeme) {
		try {
			return Integer.signum(Integer.parseInt(lexeme)) != -1;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isFloat(String lexeme) {
		// Double.parseDouble and Float.parseFloat does not work for this

		char firstChar = lexeme.charAt(0);
		if (firstChar == '.') {
			return false;
		}

		if (!lexeme.contains(".")) {
			return false;
		}

		int count = 0;
		char[] lexemeArray = lexeme.toCharArray();
		for (char c : lexemeArray) {
			if (c == '.') {
				if (++count > 1) {
					return false;
				}
			} else {
				if (!Character.isDigit(c)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isIdentifier(String lexeme) {
		char firstChar = lexeme.charAt(0);

		if (!(Character.isLetter(firstChar) || firstChar == '_')) {
			return false;
		}
		
		char[] lexemeArray = lexeme.toCharArray();
		for (char c : lexemeArray) {
			if (!(Character.isDigit(c) || Character.isLetter(c) || c == '_')) {
				return false;
			}
		}
		return true;
	}

	public static Token generate(Kind kind, int lineNumber, int charPosition) {
		return new Token(kind, lineNumber, charPosition);
	}

	public static Token generate(String lexeme, int lineNumber, int charPosition) {
		Kind kind = findKind(lexeme);

		if (kind != null) {
			return new Token(kind, lineNumber, charPosition);
		}

		if (isInteger(lexeme)) {
			return new Token(Kind.INTEGER, lexeme, lineNumber, charPosition);
		}

		if (isFloat(lexeme)) {
			return new Token(Kind.FLOAT, lexeme, lineNumber, charPosition);
		}

		if (isIdentifier(lexeme)) {
			return new Token(Kind.IDENTIFIER, lexeme, lineNumber, charPosition);
		}
		return new Token(Kind.ERROR, lexeme, lineNumber, charPosition);
	}

	public String toString() {
		if (kind.equals(Kind.ERROR)) {
			return String.format(error, kind, lexeme, lineNumber, charPosition);
		}

		if (kind.equals(Kind.INTEGER) || kind.equals(Kind.FLOAT)
				|| kind.equals(Kind.IDENTIFIER)) {
			return String.format(responseValue, kind, lexeme, lineNumber,
					charPosition);
		}

		return String.format(responseNoValue, kind, lineNumber, charPosition);
	}

	public boolean isToken (Kind kind) {
		return this.kind.equals(kind);
	}
	
	public static boolean isToken(String lexeme) {
		for (Kind kind : Kind.values()) {
			if (kind.lexeme.startsWith(lexeme)) {
				return true;
			}
		}

		if (isInteger(lexeme)) {
			return true;
		}

		if (isFloat(lexeme)) {
			return true;
		}

		if (isIdentifier(lexeme)) {
			return true;
		}

		return false;
	}
}
