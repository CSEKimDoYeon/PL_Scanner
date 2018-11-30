import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Scanner { // Scanner Ŭ���� ����.
	public enum TokenType {
		ID(3), INT(2); // �������� �����Ͽ� ID�� 3, INT�� 2�� ����

		private final int finalState; 

		TokenType(int finalState) { // �ش� ��ū Ÿ���� INT���� ID���� �Ǻ�.
			this.finalState = finalState;
		}
	}

	public static class Token {
		public final TokenType type;
		public final String lexme;

		Token(TokenType type, String lexme) { // Token ������ ����.
			this.type = type;
			this.lexme = lexme;
		}

		@Override
		public String toString() {
			return String.format("[%s: %s]", type.toString(), lexme);
			
		}
	}

	private int transM[][]; // mDFA�� ������ ������ �ִ� 2���� �迭.
	private String source; // main ���� �޾ƿ� String source.
	private StringTokenizer st; // String�� Token ������ �߶�� �ϱ� ������ StringTokenizer�� ����Ѵ�.

	public Scanner(String source) {
		this.transM = new int[4][128]; // ���´� 0~4���� �����ϰ� ���� ���� ������ ū ���� 128�� �����Ѵ�.
		this.source = source == null ? "" : source; // source == null �� true��� ����, �ƴ϶�� this.source = source
													 
		initTM(); // mDFA���� �̵��ؾ� �ϴ� entry�� index�� �Ǻ��ϴ� �Լ� initTM ����.
	}

	private void initTM() { // �̵��ϴ� entry�� �����ϴ� �޼ҵ�.
		
		for (int a = 0; a < 4; a++) { 
			for (int b = 0; b < 128; b++) { 
				switch (a) { // a�� �������� �Ͽ� �� 4���� ���̽��� ������.
				case 0: // ���� index�� �׻� 0���� �����Ѵ�.
					if (Character.isAlphabetic(b)) { // isAlphabetic �Լ��� ����Ͽ� �ش� Value�� ���ĺ����� ����.
						transM[a][b] = 3; // ���� ���ĺ��̸� entry�� 3���� ����.
					} else if (Character.isDigit(b)) { // isDigit �Լ��� ����Ͽ� �ش� Value�� �������� ����.
						transM[a][b] = 2; // ���� ���ڶ�� entry�� 2�� ����.
					} else if (b == '-') { 
						transM[a][b] = 1; // b�� '-' �̶�� entry�� 1�� ����.
					} else { // ������ ���� entry �� -1�� ����.
						transM[a][b] = -1;
					}
					break;
				case 1: 
					if (Character.isAlphabetic(b)) { // isAlphabetic �Լ��� ����Ͽ� �ش� Value�� ���ĺ����� ����.
						transM[a][b] = 3; // ���� ���ĺ��̸� entry�� 3���� ����.
					} else if (Character.isDigit(b)) { // isDigit �Լ��� ����Ͽ� �ش� Value�� �������� ����.
						transM[a][b] = 2; // ���� ���ڶ�� entry�� 2�� ����.
					} else if (b == '-') { 
						transM[a][b] = 1; // b�� '-' �̶�� entry�� 1�� ����.
					} else { // ������ ���� entry �� -1�� ����.
						transM[a][b] = -1;
					}
					break;
				case 2: 
					if (Character.isAlphabetic(b)) { // isAlphabetic �Լ��� ����Ͽ� �ش� Value�� ���ĺ����� ����.
						transM[a][b] = 3; // ���� ���ĺ��̸� entry�� 3���� ����.
					} else if (Character.isDigit(b)) { // isDigit �Լ��� ����Ͽ� �ش� Value�� �������� ����.
						transM[a][b] = 2; // ���� ���ڶ�� entry�� 2�� ����.
					} else if (b == '-') { 
						transM[a][b] = 1; // b�� '-' �̶�� entry�� 1�� ����.
					} else { // ������ ���� entry �� -1�� ����.
						transM[a][b] = -1;
					}
					break;
				case 3: 
					if (Character.isAlphabetic(b)) { // isAlphabetic �Լ��� ����Ͽ� �ش� Value�� ���ĺ����� ����.
						transM[a][b] = 3; // ���� ���ĺ��̸� entry�� 3���� ����.
					} else if (Character.isDigit(b)) { // isDigit �Լ��� ����Ͽ� �ش� Value�� �������� ����.
						transM[a][b] = 2; // ���� ���ڶ�� entry�� 2�� ����.
					} else if (b == '-') { 
						transM[a][b] = 1; // b�� '-' �̶�� entry�� 1�� ����.
					} else { // ������ ���� entry �� -1�� ����.
						transM[a][b] = -1;
					}
					break;
				}
			}
		}

		// transM[4][128] = { {...}, {...}, {...}, {...} };
		// values of entries: -1, 0, 1, 2, 3 : next state
		// TransM[0]['0'] = 2, ..., TransM[0]['9'] = 2,
		// TransM[0]['-'] = 1,
		// TransM[0]['a'] = 3, ..., TransM[0]['z'] = 3,
		// TransM[1]['0'] = 2, ..., TransM[1]['9'] = 2,
		// TransM[2]['0'] = 2, ..., TransM[1]['9'] = 2,
		// TransM[3]['A'] = 3, ..., TransM[3]['Z'] = 3,
		// TransM[3]['a'] = 3, ..., TransM[3]['z'] = 3,
		// TransM[3]['0'] = 3, ..., TransM[3]['9'] = 3,
		// ...
		// The values of the other entries are all -1.
	}

	private Token nextToken() {
		int stateOld = 0, stateNew; // �ʱ� ���´� 0���� �ʱ�ȭ�Ѵ�.
		if (!st.hasMoreTokens()) // hasMoreTokens �޼ҵ带 Ȱ���Ͽ� ���� ��ū�� �ִ��� ���θ� Ȯ���Ѵ�.
			return null;

		String temp = st.nextToken(); // ���� �����ϴ� ��ū�� temp�� �����Ѵ�.

		Token result = null;
		for (int i = 0; i < temp.length(); i++) {
			stateNew = transM[stateOld][temp.charAt(i)];
			// ���ڿ��� ���ڸ� �ϳ��� �����ͼ� ���� �Ǻ�
			if (stateNew == -1) {
				// �Էµ� ������ ���°� reject �̹Ƿ� �����޼��� ����� return��
				System.out.println(String.format("acceptState error %s\n", temp));
				return null;
			}

			stateOld = stateNew;
			// Old state�� New state�� �ʱ�ȭ�Ѵ�.
		}
		for (TokenType t : TokenType.values()) {
			if (t.finalState == stateOld) { 
				result = new Token(t, temp); 
				break;
			}
		}
		return result; // result�� �ʱ�ȭ.
	}

	public List<Token> tokenize() { 
		List<Token> tokenArray = new ArrayList<Token>(); // Token �ڷ����� ArrayList�� �����Ѵ�.
		st = new StringTokenizer(source, " ");
		
		while (st.hasMoreTokens()) { 
			tokenArray.add(nextToken()); // ���� ��ū�� ArrayList�� �����Ѵ�.
		}
		return tokenArray; // ArrayList�� return �Ѵ�.

	}

	public static void main(String[] args) {
		// txt file to String
		// ä��ÿ�
		
		FileReader fr;
		try {
			fr = new FileReader("C:\\Users\\KimDoYeon\\Desktop\\as02.txt");
			BufferedReader br = new BufferedReader(fr); // BufferReader�� ���� fr�� �д´�.
			String source = br.readLine(); // ���� line�� �о source ������ �����Ѵ�.
			Scanner s = new Scanner(source); 
			List<Token> tokenArray = s.tokenize(); // source�� �о ArrayList�� �����.

			for (int i = 0; i < tokenArray.size(); i++) {
				System.out.println(tokenArray.get(i));
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// print
		// ä��ÿ�

		// Java Stream�� �̿��Ͽ� ä��ÿ�(������)
		/*
		 * Scanner sc = new Scanner(); sc.initTM() List<Toke> tokens =
		 * Files.lines(Paths.get("as02.txt")) .map(line-> line.split(" ")) // �ش� �κк���
		 * �ۼ��Ͻÿ�.
		 */

	}
}