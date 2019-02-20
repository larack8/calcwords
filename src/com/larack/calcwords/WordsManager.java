package com.larack.calcwords;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	 * 默认线程个数
	 */
	public static final int DEFAULT_THREAD_NUM = 5 * 1000;

	/**
	 * 默认文件分块大小 100M
	 */
	public static final long DEFAULT_SPLIT_SIZE = 1024 * 1024 * 1000;

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
	 * 分片线程表
	 */
	private Vector<CalcWordsThread> listCalcWordsThreads = null;
	private Vector<Thread> listThread = null;

	/**
	 * 线程数
	 */
	private int threadNum;

	/**
	 * 文件分割大小
	 */
	private long splitSize;

	/**
	 * 统计过的文件数
	 */
	private long totalCalcFileCount = 0;

	/**
	 * 当前处理的文件位置
	 */
	private long currentPos;

	private String searchParten = PartenUtils.PARTEN_WORDS;

	private String showParten = null;

	private long calcStartTime;

	/**
	 * 按照value进行降序排序
	 */
	private Comparator<Map.Entry<String, Integer>> valueDownComparator = new Comparator<Map.Entry<String, Integer>>() {
		@Override
		public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
			// TODO Auto-generated method stub
			return o2.getValue() - o1.getValue();
		}
	};

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
	 * @param fromFileFormat 可以为空,表示任何格式文件
	 * @param resultPath
	 * @param searchParten
	 * @param showParten     可以为空,表示按搜索到的原始结果输出
	 */
	public WordsManager(String fromFilePath, String fromFileFormat, String resultPath, String searchParten,
			String showParten) {
		this(fromFilePath, fromFileFormat, resultPath, searchParten, showParten, DEFAULT_THREAD_NUM,
				DEFAULT_SPLIT_SIZE);
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
		// 确定线程数最大是10000个，防止内存不够用
		if (threadNum > 10000)
			threadNum = 10000;
		// 分割最小为1M大小文件
		if (splitSize < 1 * 1024 * 1024)
			splitSize = 1 * 1024 * 1024;
		// 分割最大为10M大小文件
		if (splitSize > 1024 * 1024 * 1000)
			splitSize = 1024 * 1024 * 1000;

		this.totalCalcFileCount = 0;
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
	}

	public void calc() throws IOException {
		calcStartTime = System.currentTimeMillis();
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
		totalCalcFileCount++;
		System.out.println(">>> 2.正在统计文件 " + totalCalcFileCount + ": " + file.getAbsolutePath());
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

							if (PartenUtils.PARTEN_WORDS.equals(searchParten)) {
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
//					System.out.println("** CalcWordsThread is " + thread.getId() + ", file path is "
//							+ file.getAbsolutePath() + ", currentPos=" + currentPos);
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
		System.out.println(">>> 3.正在处理结果");

		// 当分别统计的线程结束后，开始统计总数目的线程
		new Thread(() -> {

			TreeMap<String, Integer> tMap = new TreeMap<String, Integer>();

			// 使用TreeMap保证结果有序（按首字母排序）
			System.out.println("# 1. 正在按首字母排序... ");
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

			if (tMap.size() <= 0) {
				System.out.println("** warning ** 总共查询了 " + totalCalcFileCount + " 个文件, 没有匹配到任何数据，程序退出 !!! ");
				return;
			}

			// 使用TreeMap保证结果有序（然后再按查找到的次数递减排序）
			// map转换成list进行排序
			System.out.println("# 2. 正在按统计次数排序... ");
			List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(tMap.entrySet());
			Collections.sort(list, valueDownComparator);

			System.out.println("# 3. 正在保存到文件... ");

			saveResultKVtogether(list);
			saveResultKVindependent(list);

			System.out.println("## done !! ");
			long end = System.currentTimeMillis();
			System.out.println("@@ 总结: 总共查询了 " + totalCalcFileCount + " 个文件, 创建 " + listCalcWordsThreads.size()
					+ " 个线程, 匹配到 " + tMap.size() + " 个单词, 耗时 " + (end - calcStartTime) / 1000.0 + " 秒!");
			return;
		}).start();
	}

	public void saveResultKVindependent(List<Map.Entry<String, Integer>> list) {
		String keyFilePath = resultFilePath + "_" + "key.txt";
		String valueFilePath = resultFilePath + "_" + "value.txt";
		File keyFile = new File(keyFilePath);
		if (keyFile.exists()) {
			keyFile.delete();
		}
		File valueFile = new File(valueFilePath);
		if (valueFile.exists()) {
			valueFile.delete();
		}
		System.out.println(
				"## 正在分别保存Key和Value到文件中, Key:" + keyFile.getAbsolutePath() + "; Value:" + valueFile.getAbsolutePath());
		for (Map.Entry<String, Integer> entry : list) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			saveResultToFile(keyFilePath, key + "\n");
			saveResultToFile(valueFilePath, String.valueOf(value) + "\n");
		}
	}

	public void saveResultKVtogether(List<Map.Entry<String, Integer>> list) {
		File fileText = new File(this.resultFilePath);
		if (fileText.exists()) {
			fileText.delete();
		}
		System.out.println("## 正在保存结果到文件中 " + fileText.getAbsolutePath());
		for (Map.Entry<String, Integer> entry : list) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			String calcResult = "样式:" + key + " 出现次数:" + value + "\n";
//			System.out.print(calcResult);
			saveResultToFile(resultFilePath, calcResult);
		}
	}

	/**
	 * 结果写入文件
	 * 
	 * @param strFilename
	 * @param strBuffer
	 */
	public static void saveResultToFile(final String strFilename, final String strBuffer) {
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
			System.out.print("IOException when saveResultToFile " + strFilename);
			e.printStackTrace();
		}
	}
}