package com.larack.calcwords;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author larack
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {

		testCalcWords();
		// test();
	}

	public static void testCalcWords() throws IOException {

		String fromFilePath = "/Users/larack/Downloads/tt/app.wxss";
		String resultFilePath = "result.txt";

		long start = System.currentTimeMillis();
		WordsManager dft = new WordsManager(fromFilePath, resultFilePath, WordsManager.PARTEN_WXSS_STYLE); // 文件，线程数，文件分割大小
		dft.calc();
		long end = System.currentTimeMillis();

		System.out.println("统计字符花费时间：" + (end - start) / 1000.0 + "秒");

	}

	public static void test() {

		// Pattern
		// pattern=Pattern.compile("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*$");
		String str = "button::after { .loading {.globalBottomSubmitBtnHover {   color: #979797;";
		// 分组且创建反向引用
		String reg = "\\.[a-zA-Z']+\\s+\\{";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			System.out.println(matcher.group());
			// System.out.println(matcher.group(1));
		}
	}
}
