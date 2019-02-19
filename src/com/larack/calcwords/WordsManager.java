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

	// 要处理的文件
	private String fromFilePath = null;

	private String resultFilePath;

	private String fromFileFormat = null;

	// 线程数
	private int threadNum;

	// 线程表
	private Vector<CalcWordsThread> listCalcWordsThreads = null;
	private Vector<Thread> listThread = null;

	// 文件分割大小
	private long splitSize;

	// 当前处理的文件位置
	private long currentPos;

	private String searchParten = PARTEN_WORDS;

	private String showParten = null;

	/**
	 * 英文字符（不管前后空格,加上短横杠）
	 */
	public static final String PARTEN_LETTER = "[a-zA-Z\\-]+";

	/**
	 * 单词正则（前后空格,加上短横杠）
	 */
	public static final String PARTEN_WORDS = "[^a-zA-Z']+";

	/**
	 * WXSS样式正则
	 */
	public static final String PARTEN_WXSS_STYLE = "\\.[a-zA-Z\\-']+\\s+\\{";

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