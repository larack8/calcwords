package com.larack.calcwords;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 * 
 * @author larack
 *
 */
public class WordsManager {

	/**
	 * 要处理的文件
	 */
	private String fromFilePath = null;

	/**
	 * 保存結果的文件
	 */
	private String resultFilePath;

	/**
	 * 要處理的文件格式
	 */
	private String fromFileFormat = null;

	/**
	 * 线程数
	 */
	private int threadNum;

	/**
	 * 分片线程表
	 */
	private Vector<CalcWordsThread> listCalcWordsThreads = null;
	private Vector<Thread> listThread = null;

	/**
	 * 文件分割大小
	 */
	private long splitSize;

	/**
	 * 当前处理的文件位置
	 */
	private long currentPos;

	private String searchParten = PARTEN_WORDS;

	private String showParten = null;

	/**
	 * 数字正则（数字开头，数字结尾）
	 */
	public static final String PARTEN_NUM = "[^0-9']+";

	/**
	 * 单词正则（前后空格,加上短横杠）
	 */
	public static final String PARTEN_WORDS = "[^a-zA-Z']+";

	/**
	 * WXSS样式正则
	 */
	public static final String PARTEN_WXSS_STYLE = "\\.[a-zA-Z\\-']+\\s+\\{";

	/**
	 * 匹配中文字符的正则表达式
	 */
	public static final String PARTEN_LANGUAGE_CH = "[\\u4e00-\\u9fa5]";

	/**
	 * 匹配双字节字符(包括汉字在内) 应用：计算字符串的长度（一个双字节字符长度计2，ASCII字符计1）
	 */
	public static final String PARTEN_DOUBLE_BYTE_CHAR = "[^\\x00-\\xff]";

	/**
	 * 匹配空行的正则表达式
	 */
	public static final String PARTEN_NULL_LINE = "\n[\\s| ]*\r";

	/**
	 * 匹配HTML标记的正则表达式
	 */
	public static final String PARTEN_HTML = "/<(.*)>.*<\\/\\1>|<(.*) \\/>/";

	/**
	 * 匹配首尾空格的正则表达式
	 */
	public static final String PARTEN_SPACE_START_END = "(^\\s*)|(\\s*$)";

	/**
	 * 匹配帐号是否合法(字母开头，允许5-16字节，允许字母数字下划线)
	 */
	public static final String PARTEN_ACCOUNT = "^[a-zA-Z][a-zA-Z0-9_]{4,15}$";

	/**
	 * 匹配国内电话号码 正确格式为：“XXXX-XXXXXXX”，“XXXX-XXXXXXXX”，“XXX-XXXXXXX”，
	 */
	public static final String PARTEN_TELEPHONE = "(\\d3,4\\d3,4|\\d{3,4}-|\\s)?\\d{8}";

	/**
	 * 验证身份证号（15位或18位数字）
	 */
	public static final String PARTEN_ID_CARD = "^d{15}|d{}18$";

	/**
	 * 匹配腾讯QQ号
	 */
	public static final String PARTEN_QQ = "^[1-9]*[1-9][0-9]*$";

	public static final String PARTEN_NUM_NOT_NEGATIVE_INT = "^\\d+$";// 非负整数（正整数 + 0）

	public static final String PARTEN_NUM_POSITIVE_INT = "^[0-9]*[1-9][0-9]*$"; // 正整数

	public static final String PARTEN_NUM_NOT_POSITIVE_INT = "^((-\\d+)|(0+))$"; // 非正整数（负整数 + 0）

	public static final String PARTEN_NUM_NEGATIVE_INT = "^-[0-9]*[1-9][0-9]*$"; // 负整数

	public static final String PARTEN_NUM_INT = "^-?\\d+$";// 整数

	public static final String PARTEN_NUM_NOT_NEGATIVE_FLOAT = "^\\d+(\\.\\d+)?$"; // 非负浮点数（正浮点数 + 0）

	public static final String PARTEN_NUM_POSITIVE_FLOAT = "^(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*))$"; // 正浮点数

	public static final String PARTEN_NUM_NOT_POSITIVE_FLOAT = "^((-\\d+(\\.\\d+)?)|(0+(\\.0+)?))$"; // 非正浮点数（负浮点数 + 0）

	public static final String PARTEN_NUM_NEGATIVE_FLOAT = "^(-(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*)))$"; // 负浮点数

	public static final String PARTEN_NUM_FLOAT = "^(-?\\d+)(\\.\\d+)?$"; // 浮点数

	public static final String PARTEN_LETTER = "^[A-Za-z]+$"; // 由26个英文字母组成的字符串

	public static final String PARTEN_LETTER_UPPER = "^[A-Z]+$"; // 由26个英文字母的大写组成的字符串

	public static final String PARTEN_LETTER_LOWER = "^[a-z]+$"; // 由26个英文字母的小写组成的字符串

	public static final String PARTEN_NICKNAME = "^[A-Za-z0-9]+$"; // 由数字和26个英文字母组成的字符串

	public static final String PARTEN_USERNAME = "^\\w+$"; // 由数字、26个英文字母或者下划线组成的字符串

	public static final String PARTEN_EMAIL = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$";// email地址

	public static final String PARTEN_URL = "^[a-zA-z]+://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(\\?\\S*)?$";// url

	public static final String PARTEN_Y_M_D = "/^(d{2}|d{4})-((0([1-9]{1}))|(1[1|2]))-(([0-2]([1-9]{1}))|(3[0|1]))$/"; // 年-月-日

	public static final String PARTEN_M_D_Y = "/^((0([1-9]{1}))|(1[1|2]))/(([0-2]([1-9]{1}))|(3[0|1]))/(d{2}|d{4})$/"; // 月/日/年

	public static final String PARTEN_EMAIL_ADDRESS = "^([w-.]+)@(([[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.)|(([w-]+.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(]?)$"; // Emil

	public static final String PARTEN_PHONE = "(d+-)?(d{4}-?d{7}|d{3}-?d{8}|^d{7,8})(-d+)?"; // 电话号码

	public static final String PARTEN_IP_ADDRESS = "^(d{1,2}|1dd|2[0-4]d|25[0-5]).(d{1,2}|1dd|2[0-4]d|25[0-5]).(d{1,2}|1dd|2[0-4]d|25[0-5]).(d{1,2}|1dd|2[0-4]d|25[0-5])$"; // IP地址

	/**
	 * 
	 * @param fromFilePath
	 * @param resultPath
	 * @param searchParten
	 */
	public WordsManager(String fromFilePath, String resultPath, String searchParten) {
		this(fromFilePath, null, resultPath, searchParten, null);
	}

	/**
	 * 
	 * @param fromFilePath
	 * @param fromFileFormat
	 * @param resultPath
	 * @param searchParten
	 * @param showParten
	 */
	public WordsManager(String fromFilePath, String fromFileFormat, String resultPath, String searchParten,
			String showParten) {
		this(fromFilePath, fromFileFormat, resultPath, searchParten, showParten, 1000, 1024 * 1024 * 100);
	}

	/**
	 * 
	 * @param fromFilePath   读取文件路径
	 * @param fromFileFormat 要查的文件格式
	 * @param resultPath     结果保存路径
	 * @param searchParten   搜索正则表达式
	 * @param showParten     显示结果正则表达式
	 * @param threadNum      线程数
	 * @param splitSize      文件分割大小
	 */
	public WordsManager(String fromFilePath, String fromFileFormat, String resultFilePath, String searchParten,
			String showParten, int threadNum, long splitSize) {
// 确定线程数最小是1个
		if (threadNum < 1)
			threadNum = 1;
// 确定线程数最大是10个，防止内存不够用
		if (threadNum > 1000)
			threadNum = 1000;
// 分割最小为1M大小文件
		if (splitSize < 1 * 1024 * 1024)
			splitSize = 1 * 1024 * 1024;
// 分割最大为10M大小文件
		if (splitSize > 1024 * 1024 * 1000)
			splitSize = 1024 * 1024 * 1000;

		this.fromFilePath = fromFilePath;
		this.resultFilePath = resultFilePath;
		this.fromFileFormat = fromFileFormat;
		this.searchParten = searchParten;
		this.showParten = showParten;
		this.threadNum = threadNum;
		this.splitSize = splitSize;
		this.currentPos = 0;
		this.listCalcWordsThreads = new Vector<CalcWordsThread>();
		this.listThread = new Vector<Thread>();

		System.out.println(">>> 1.初始化: fromFileFormat=" + fromFileFormat + ", searchParten=" + searchParten
				+ ", showParten=" + showParten + ", threadNum=" + threadNum + ", splitSize=" + splitSize);

		File fileText = new File(this.resultFilePath);
		if (fileText.exists()) {
			fileText.delete();
		}
	}

	public void calc() throws IOException {
		File files = new File(fromFilePath);
		if (!files.exists() || !files.canRead()) {
			return;
		}
		calc(files);
		saveResult();
	}

	private void calc(File file) throws IOException {
		if (null == file) {
			return;
		}
		if (file.isFile()) {
			doFile(file);
		} else {
			File[] fs = file.listFiles();
			for (File f : fs) {
				if (f.isDirectory()) {
					calc(f);
				}
				String fp = f.getAbsolutePath();
				if (null != fromFileFormat) {
					if (fp.endsWith(fromFileFormat) && f.isFile()) {
						doFile(f);
					}
				} else if (f.isFile()) {
					doFile(f);
				}
			}
		}
	}

	/**
	 * 分片处理
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void doFile(File file) throws IOException {
		System.out.println(">>> 2.正在统计单词:" + file.getAbsolutePath());
		currentPos = 0;
		while (currentPos < file.length()) {
			for (int num = 0; num < threadNum; num++) {
				if (currentPos < file.length()) {
					CalcWordsThread calcWordsThread = null;

					if (currentPos + splitSize < file.length()) {
						RandomAccessFile raf = new RandomAccessFile(file, "r");
						raf.seek(currentPos + splitSize);

						int offset = 0;

						while (true) {
							char ch = (char) raf.read();

// 是否到文件末尾，到了跳出
							if (-1 == ch) {
								currentPos = 0;
								break;
							}

							if (PARTEN_WORDS.equals(searchParten)) {
// 是否是字母和'，都不是跳出（防止单词被截断）
								if (false == Character.isLetter(ch) && '\'' != ch) {
									break;
								}
							}
							offset++;
						}

						calcWordsThread = new CalcWordsThread(file, currentPos, splitSize + offset, searchParten,
								showParten);
						currentPos += splitSize + offset;

						raf.close();
					} else {
						calcWordsThread = new CalcWordsThread(file, currentPos, file.length() - currentPos,
								searchParten, showParten);
						currentPos = file.length();
					}

					Thread thread = new Thread(calcWordsThread);
					System.out.println("** CalcWordsThread is " + thread.getId() + ", file path is "
							+ file.getAbsolutePath() + ", currentPos=" + currentPos);
					thread.start();
					listCalcWordsThreads.add(calcWordsThread);
					listThread.add(thread);
				}
			}

// 判断线程是否执行完成
			while (true) {
				boolean threadsDone = true;

				for (int loop = 0; loop < listThread.size(); loop++) {
					if (listThread.get(loop).getState() != Thread.State.TERMINATED) {
						threadsDone = false;
						break;
					}
				}

				if (true == threadsDone)
					break;
			}
		}

	}

	/**
	 * 保存结果
	 */
	private void saveResult() {

		System.out.println(">>> 3.正在保存结果到文件:" + (new File(resultFilePath).getAbsolutePath()));
// 当分别统计的线程结束后，开始统计总数目的线程
		new Thread(() -> {
// 使用TreeMap保证结果有序（按首字母排序）
			TreeMap<String, Integer> tMap = new TreeMap<String, Integer>();

			for (int loop = 0; loop < listCalcWordsThreads.size(); loop++) {
				Map<String, Integer> hMap = listCalcWordsThreads.get(loop).getResultMap();

				Set<String> keys = hMap.keySet();
				Iterator<String> iterator = keys.iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();

					if (key.equals(""))
						continue;
					if (tMap.get(key) == null) {
						tMap.put(key, hMap.get(key));
					} else {
						tMap.put(key, tMap.get(key) + hMap.get(key));
					}
				}
			}

			for (int loop = 0; loop < listThread.size(); loop++) {
				listThread.get(loop).interrupt();
			}

			Set<String> keys = tMap.keySet();
			Iterator<String> iterator = keys.iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				String calcResult = "样式:" + key + " 出现次数:" + tMap.get(key) + "\n";
				System.out.print(calcResult);
				TextToFile(resultFilePath, calcResult);
			}
			return;
		}).start();
	}

	/**
	 * 结果写入文件
	 * 
	 * @param strFilename
	 * @param strBuffer
	 */
	public static void TextToFile(final String strFilename, final String strBuffer) {
		try {
// 创建文件对象
			File fileText = new File(strFilename);
// 向文件写入对象写入信息
			FileWriter fileWriter = new FileWriter(fileText, true);
// 写文件
			fileWriter.write(strBuffer);
// 关闭
			fileWriter.close();
		} catch (IOException e) {
//
			e.printStackTrace();
		}
	}
}