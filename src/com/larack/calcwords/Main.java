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

		String fromFilePath = "/Users/larack/Downloads/top100";
		String resultFilePath = "/Users/larack/Downloads/result.txt";

		calcCustom(fromFilePath, resultFilePath);

//		calcLanguageCh(fromFilePath, resultFilePath);// 统计汉字
//		calcLetters(fromFilePath, resultFilePath);// 统计英文单词
//		calcWxssStyle(fromFilePath, resultFilePath);// 统计微信小程序源码WWXSS样式
//		calcWxssProperty(fromFilePath, resultFilePath);// 统计微信小程序源码WXSS属性
//		calcHtml(fromFilePath, resultFilePath);// 统计网页html内容
//		calcCellPhone(fromFilePath, resultFilePath);// 统计手机号码
//		calcIpAddress(fromFilePath, resultFilePath);// 统计IP地址
		// 更多用法请参照calcBase函数,然后参照PartenUtils.java修正则表达式
	}

	/**
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcCustom(String fromFilePath, String resultFilePath) throws IOException {
		String fromFileFormat = null;
		String custom = "navigationStyle";
		WordsManager wm = new WordsManager(fromFilePath, fromFileFormat, resultFilePath, custom, null);
		wm.calc();
	}

	/**
	 * 统计基本用法
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @param searchParten
	 * @param showParten
	 * @throws IOException
	 */
	public static void calcBase(String fromFilePath, String resultFilePath, String searchParten, String showParten)
			throws IOException {
		String fromFileFormat = null;
		WordsManager wm = new WordsManager(fromFilePath, fromFileFormat, resultFilePath, searchParten, showParten);
		wm.calc();
	}

	/**
	 * 统计汉字
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcLanguageCh(String fromFilePath, String resultFilePath) throws IOException {
		calcBase(fromFilePath, resultFilePath, PartenUtils.PARTEN_LANGUAGE_CH, null);
	}

	/**
	 * 统计英文单词
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcLetters(String fromFilePath, String resultFilePath) throws IOException {
		calcBase(fromFilePath, resultFilePath, PartenUtils.PARTEN_WORDS, PartenUtils.PARTEN_WORDS);
	}

	/**
	 * 统计微信小程序源码WXSS属性
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcWxssProperty(String fromFilePath, String resultFilePath) throws IOException {
		String fromFileFormat = ".wxss";
		WordsManager wm = new WordsManager(fromFilePath, fromFileFormat, resultFilePath,
				PartenUtils.PARTEN_WXSS_PROPERTY, PartenUtils.PARTEN_WORDS);
		wm.calc();
	}

	/**
	 * 统计微信小程序源码WWXSS样式
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcWxssStyle(String fromFilePath, String resultFilePath) throws IOException {
		String fromFileFormat = ".wxss";
		WordsManager wm = new WordsManager(fromFilePath, fromFileFormat, resultFilePath, PartenUtils.PARTEN_WXSS_STYLE,
				null);
		wm.calc();
	}

	/**
	 * 统计网页html内容
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcHtml(String fromFilePath, String resultFilePath) throws IOException {
		calcBase(fromFilePath, resultFilePath, PartenUtils.PARTEN_HTML, null);
	}

	/**
	 * 统计手机号码
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcCellPhone(String fromFilePath, String resultFilePath) throws IOException {
		calcBase(fromFilePath, resultFilePath, PartenUtils.PARTEN_CEL_PHONE, null);
	}

	/**
	 * 统计IP地址
	 * 
	 * @param fromFilePath
	 * @param resultFilePath
	 * @throws IOException
	 */
	public static void calcIpAddress(String fromFilePath, String resultFilePath) throws IOException {
		calcBase(fromFilePath, resultFilePath, PartenUtils.PARTEN_IP_ADDRESS, null);
	}
}
