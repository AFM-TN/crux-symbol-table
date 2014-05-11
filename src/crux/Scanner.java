package crux;

import java.io.IOException;
import java.io.Reader;


public class Scanner {

	private int lineNumber;
	private int charPosition;
	private int nextChar;

	private Reader reader;

	private static final int EOF = -1;
	private static final int TAB = 9;
	private static final int NL = 10;
	private static final int ENTER = 13;
	private static final int WS = 32;

	public Scanner(Reader reader) {
		this.lineNumber = 1;
		this.charPosition = -1;
		this.nextChar = 0;

		this.reader = reader;

		try {
			read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int read() throws IOException {
		int result = nextChar;

		while (result == NL || result == ENTER) {
			++lineNumber;
			charPosition = 0;
			result = reader.read();
		}

		nextChar = reader.read();
		++charPosition;
		return result;
	}

	public Token next() throws IOException {
		String lexeme = "";

		int value = 0;
		int count = 0;

		do {
			value = read();

			if (value == EOF) {
				return Token.generate(Token.Kind.EOF, lineNumber, charPosition);
			}

			if (value == WS || value == TAB) {
				continue;
			}

			lexeme += (char) value;

			if ((lexeme + (char) nextChar).equals("//")) {
				while (!(nextChar == NL || nextChar == EOF || nextChar == ENTER)) {
					read();
				}

				lexeme = "";
				continue;
			}

			if (!Token.isToken(lexeme + (char) nextChar)) {
				return Token.generate(lexeme, lineNumber, charPosition - count);
			}

			++count;
		} while (true);
	}

	public void close() throws IOException {
		reader.close();
	}
}
