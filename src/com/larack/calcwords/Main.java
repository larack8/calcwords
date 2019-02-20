package com.larack.calcwords;

import java.io.IOException;

/**
 * 
 * @author larack
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {

		testCalcWords();
	}

	public static void testCalcWords() throws IOException {

		String fromFilePath = "/Users/larack/Downloads/tt";

//		String fromFilePath = "/Users/larack/Downloads/top100_2";
		String resultFilePath = "result.txt";
		String fromFileFormat = ".wxss";

		WordsManager wm = new WordsManager(fromFilePath, fromFileFormat, resultFilePath,
				PartenUtils.PARTEN_WXSS_PROPERTY, null);
		wm.calc();

	}

}
