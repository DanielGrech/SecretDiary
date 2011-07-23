package com.DGSD.SecretDiary;

public class Utils {

	public static int getPasswordRating(String password) {
		if (password == null || password.length() < 5) {
			return Password.WEAK;
		}

		int passwordStrength = 0;    

		// minimal pw length of 6
		if (password.length() > 5) {
			passwordStrength++;
		}

		// lower and upper case
		if (password.toLowerCase()!= password) {
			passwordStrength += 2;
		} 

		// good pw length of 8+
		if (password.length() > 7) {
			passwordStrength += 2;
		} 

		int numDigits = getNumberDigits(password);

		// contains digits and non-digits
		if (numDigits > 0 && numDigits != password.length()) {
			passwordStrength += 3;
		} 

		if(passwordStrength >= 6 ) {
			return Password.STRONG;
		} else if(passwordStrength >= 4) {
			return Password.OK;
		} else {
			return Password.WEAK;
		}
	}

	public static int getNumberDigits(String inString){
		if (isEmpty(inString)) {
			return 0;
		}
		
		int numDigits= 0;
		
		for (int i = 0, size = inString.length(); i < size; i++) {
			if (Character.isDigit(inString.charAt(i))) {
				numDigits++; 
			}
		}
		return numDigits; 
	}

	public static boolean isEmpty(String inString) {
		return inString == null || inString.length() == 0;
	}
	
	public static class Password {
		public static final int WEAK = 0;
		public static final int OK = 1;
		public static final int STRONG = 2;
	}

}
