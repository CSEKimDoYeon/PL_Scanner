import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Scanner { // Scanner 클래스 정의.
	public enum TokenType {
		ID(3), INT(2); // 열거형을 선언하여 ID는 3, INT는 2로 정의

		private final int finalState; 

		TokenType(int finalState) { // 해당 토큰 타입이 INT인지 ID인지 판별.
			this.finalState = finalState;
		}
	}

	public static class Token {
		public final TokenType type;
		public final String lexme;

		Token(TokenType type, String lexme) { // Token 생성자 정의.
			this.type = type;
			this.lexme = lexme;
		}

		@Override
		public String toString() {
			return String.format("[%s: %s]", type.toString(), lexme);
			
		}
	}

	private int transM[][]; // mDFA의 정보를 가지고 있는 2차원 배열.
	private String source; // main 에서 받아온 String source.
	private StringTokenizer st; // String을 Token 단위로 잘라야 하기 때문에 StringTokenizer를 사용한다.

	public Scanner(String source) {
		this.transM = new int[4][128]; // 상태는 0~4까지 존재하고 이후 열은 적당히 큰 수인 128로 정의한다.
		this.source = source == null ? "" : source; // source == null 이 true라면 공백, 아니라면 this.source = source
													 
		initTM(); // mDFA에서 이동해야 하는 entry의 index를 판별하는 함수 initTM 실행.
	}

	private void initTM() { // 이동하는 entry를 결정하는 메소드.
		
		for (int a = 0; a < 4; a++) { 
			for (int b = 0; b < 128; b++) { 
				switch (a) { // a를 기준으로 하여 총 4개의 케이스를 나눈다.
				case 0: // 시작 index는 항상 0부터 시작한다.
					if (Character.isAlphabetic(b)) { // isAlphabetic 함수를 사용하여 해당 Value가 알파벳인지 검증.
						transM[a][b] = 3; // 만약 알파벳이면 entry를 3으로 설정.
					} else if (Character.isDigit(b)) { // isDigit 함수를 사용하여 해당 Value가 숫자인지 검증.
						transM[a][b] = 2; // 만약 숫자라면 entry를 2로 설정.
					} else if (b == '-') { 
						transM[a][b] = 1; // b가 '-' 이라면 entry를 1로 설정.
					} else { // 나머지 경우는 entry 를 -1로 설정.
						transM[a][b] = -1;
					}
					break;
				case 1: 
					if (Character.isAlphabetic(b)) { // isAlphabetic 함수를 사용하여 해당 Value가 알파벳인지 검증.
						transM[a][b] = 3; // 만약 알파벳이면 entry를 3으로 설정.
					} else if (Character.isDigit(b)) { // isDigit 함수를 사용하여 해당 Value가 숫자인지 검증.
						transM[a][b] = 2; // 만약 숫자라면 entry를 2로 설정.
					} else if (b == '-') { 
						transM[a][b] = 1; // b가 '-' 이라면 entry를 1로 설정.
					} else { // 나머지 경우는 entry 를 -1로 설정.
						transM[a][b] = -1;
					}
					break;
				case 2: 
					if (Character.isAlphabetic(b)) { // isAlphabetic 함수를 사용하여 해당 Value가 알파벳인지 검증.
						transM[a][b] = 3; // 만약 알파벳이면 entry를 3으로 설정.
					} else if (Character.isDigit(b)) { // isDigit 함수를 사용하여 해당 Value가 숫자인지 검증.
						transM[a][b] = 2; // 만약 숫자라면 entry를 2로 설정.
					} else if (b == '-') { 
						transM[a][b] = 1; // b가 '-' 이라면 entry를 1로 설정.
					} else { // 나머지 경우는 entry 를 -1로 설정.
						transM[a][b] = -1;
					}
					break;
				case 3: 
					if (Character.isAlphabetic(b)) { // isAlphabetic 함수를 사용하여 해당 Value가 알파벳인지 검증.
						transM[a][b] = 3; // 만약 알파벳이면 entry를 3으로 설정.
					} else if (Character.isDigit(b)) { // isDigit 함수를 사용하여 해당 Value가 숫자인지 검증.
						transM[a][b] = 2; // 만약 숫자라면 entry를 2로 설정.
					} else if (b == '-') { 
						transM[a][b] = 1; // b가 '-' 이라면 entry를 1로 설정.
					} else { // 나머지 경우는 entry 를 -1로 설정.
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
		int stateOld = 0, stateNew; // 초기 상태는 0으로 초기화한다.
		if (!st.hasMoreTokens()) // hasMoreTokens 메소드를 활용하여 다음 토큰이 있는지 여부를 확인한다.
			return null;

		String temp = st.nextToken(); // 다음 존재하는 토큰을 temp에 저장한다.

		Token result = null;
		for (int i = 0; i < temp.length(); i++) {
			stateNew = transM[stateOld][temp.charAt(i)];
			// 문자열의 문자를 하나씩 가져와서 상태 판별
			if (stateNew == -1) {
				// 입력된 문자의 상태가 reject 이므로 에러메세지 출력후 return함
				System.out.println(String.format("acceptState error %s\n", temp));
				return null;
			}

			stateOld = stateNew;
			// Old state를 New state로 초기화한다.
		}
		for (TokenType t : TokenType.values()) {
			if (t.finalState == stateOld) { 
				result = new Token(t, temp); 
				break;
			}
		}
		return result; // result를 초기화.
	}

	public List<Token> tokenize() { 
		List<Token> tokenArray = new ArrayList<Token>(); // Token 자료형의 ArrayList를 생성한다.
		st = new StringTokenizer(source, " ");
		
		while (st.hasMoreTokens()) { 
			tokenArray.add(nextToken()); // 다음 토큰을 ArrayList에 저장한다.
		}
		return tokenArray; // ArrayList를 return 한다.

	}

	public static void main(String[] args) {
		// txt file to String
		// 채우시오
		
		FileReader fr;
		try {
			fr = new FileReader("C:\\Users\\KimDoYeon\\Desktop\\as02.txt");
			BufferedReader br = new BufferedReader(fr); // BufferReader를 통해 fr를 읽는다.
			String source = br.readLine(); // 다음 line을 읽어서 source 변수에 저장한다.
			Scanner s = new Scanner(source); 
			List<Token> tokenArray = s.tokenize(); // source를 읽어서 ArrayList를 만든다.

			for (int i = 0; i < tokenArray.size(); i++) {
				System.out.println(tokenArray.get(i));
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// print
		// 채우시오

		// Java Stream을 이용하여 채우시오(가산점)
		/*
		 * Scanner sc = new Scanner(); sc.initTM() List<Toke> tokens =
		 * Files.lines(Paths.get("as02.txt")) .map(line-> line.split(" ")) // 해당 부분부터
		 * 작성하시오.
		 */

	}
}