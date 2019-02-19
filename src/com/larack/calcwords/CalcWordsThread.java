package com.larack.calcwords;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author larack
 *
 */
public class CalcWordsThread implements Runnable {
	private FileChannel fileChannel = null;
	private FileLock lock = null;
	private MappedByteBuffer mbBuf = null;
	private Map<String, Integer> hashMap = null;

	@SuppressWarnings("resource")
	public CalcWordsThread(File file, long start, long size) // 文件，起始位置，映射文件大小
	{
		try {
			// 得到当前文件的通道
			fileChannel = new RandomAccessFile(file, "rw").getChannel();
			// 锁定当前文件的部分
			lock = fileChannel.lock(start, size, false);
			// 对当前文件片段建立内存映射，如果文件过大需要切割成多个片段
			mbBuf = fileChannel.map(FileChannel.MapMode.READ_ONLY, start, size);
			// 创建HashMap实例存放处理结果
			hashMap = new HashMap<String, Integer>();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 重写run()方法
	@Override
	public void run() {
		String str = Charset.forName("UTF-8").decode(mbBuf).toString();
		str = str.toLowerCase();
		String[] strArray = str.split("[^a-zA-Z']+");

		for (int i = 0; i < strArray.length; i++) {

			if (strArray[i].equals(""))
				continue;

			if (hashMap.get(strArray[i]) == null) {
				hashMap.put(strArray[i], 1);
			} else {
				hashMap.put(strArray[i], hashMap.get(strArray[i]) + 1);
			}
		}

		try {
			// 释放文件锁
			lock.release();
			// 关闭文件通道
			fileChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	// 获取当前线程的执行结果
	public Map<String, Integer> getResultMap() {
		return hashMap;
	}
}