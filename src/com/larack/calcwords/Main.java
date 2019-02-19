package com.larack.calcwords;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

/**
 * 
 * @author larack
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {

		String fromFilePath = "/Users/larack/Downloads/tt/app.wxss";
		String resultFilePath = "result.txt";

		File file = new File(fromFilePath);

		long start = System.currentTimeMillis();
		WordsManager dft = new WordsManager(file, resultFilePath, WordsManager.FUNC_CALC_WXSS_STAYLE); // 文件，线程数，文件分割大小
		dft.doFile();
		long end = System.currentTimeMillis();

		System.out.println("统计字符花费时间：" + (end - start) / 1000.0 + "秒");

	}
}
