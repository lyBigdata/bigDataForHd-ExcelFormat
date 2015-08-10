package cn.hadoop.liuyu.project;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/*
 * 自定义ExcelInputFormat类实现FileInputFormat接口。
 */
public class ExcelInputFormat extends FileInputFormat<LongWritable,Text>{
	@Override
	public RecordReader<LongWritable, Text> createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException, InterruptedException {
		//这里默认是系统实现的的RecordReader，按行读取，下面我们自定义这个类ExcelRecordReader。
		return new ExcelRecordReader();
	}
	
	/**
	 * Reads excel spread sheet , where keys are offset in file and value is the row
	 * containing all column as a string.
	 */
	/*
	 * 自定义ExcelRecordReader类，实现RecordReader接口。
	 */
	public class ExcelRecordReader extends RecordReader<LongWritable, Text> {
		private LongWritable key;//声明 key 类型
		private Text value;//声明 value类型
		private InputStream is;
		private String[] strArrayofLines;

		@Override
		public void initialize(InputSplit genericSplit, TaskAttemptContext context)
				throws IOException, InterruptedException {

			FileSplit split = (FileSplit) genericSplit;
			Configuration job = context.getConfiguration();
			final Path file = split.getPath();

			FileSystem fs = file.getFileSystem(job);
			FSDataInputStream fileIn = fs.open(split.getPath());

			is = fileIn;
			String line = new ExcelParser().parseExcelData(is);//调用解析excel方法，返回解析结果。
			this.strArrayofLines = line.split("\n");//将返回数据解析为数组strArrayofLines
		}

		/*
		 * nextKeyValue()方法，设置读取的 key和value。这里key设置为行的偏移量，
		 * value为数组strArrayofLines里面的值。(non-Javadoc)
		 * @see org.apache.hadoop.mapreduce.RecordReader#nextKeyValue()
		 */
		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			if (key == null) {
				key = new LongWritable(0);
				value = new Text(strArrayofLines[0]);
			} else {
				if (key.get() < (this.strArrayofLines.length - 1)) {
					long pos = (int) key.get();
					key.set(pos + 1);
					value.set(this.strArrayofLines[(int) (pos + 1)]);
					pos++;
				} else {
					return false;
				}
			}

			if (key == null || value == null) {
				return false;
			} else {
				return true;
			}

		}

		@Override
		public LongWritable getCurrentKey() throws IOException,
				InterruptedException {		
			return key;
		}

		@Override
		public Text getCurrentValue() throws IOException, InterruptedException {		
			return value;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			return 0;
		}

		@Override
		public void close() throws IOException {
			if (is != null) {
				is.close();
			}
		}
	}
}
