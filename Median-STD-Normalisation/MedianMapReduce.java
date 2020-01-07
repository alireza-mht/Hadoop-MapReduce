package Median;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;



public class MedianMapReduce {


    public static class calculateMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

        Text t1 = new Text();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            int col = 0;
            String[] colvalue = value.toString().split(",");
            Configuration config = context.getConfiguration();
            String attributename = config.get("attributename");

            if (attributename.equals("cpu")) {
                col = 0;
            } else if (attributename.equals("networkin")) {
                col = 1;
            } else if (attributename.equals("networkout")) {
                col = 2;
            } else if (attributename.equals("memory")) {
                col = 3;
            } else if (attributename.equals("target")) {
                col = 4;
            }
            t1.set(String.valueOf(col));
            context.write(t1, new DoubleWritable(Double.parseDouble(colvalue[col])));
        }
    }


    public static class calculateReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {

            double min = Integer.MAX_VALUE, max = 0;
            List<Double> list = new ArrayList<Double>();
            List<Double> temp = new ArrayList<Double>();
            Iterator<DoubleWritable> iterator = values.iterator(); //Iterating
            double sum = 0;
            double count = 0;

            while (iterator.hasNext()) {

                double value = iterator.next().get();
                count = count + 1;
                sum = sum + value;
                list.add(value);
                if(value<min){
                	min = value;
				}else if (value>max){
                	max=value;
				}
            }
			temp = list;

            Collections.sort(list);

            int length = list.size();
            double median = 0;

            if (length % 2 == 0) {
                double medianSum = list.get((length / 2) - 1) + list.get(length / 2);
                median = medianSum / 2;
            } else {
                median = list.get(length / 2);
            }

            double mean = sum / count;
            double sumOfSquares = 0;

            for (double doubleWritable : list) {
                sumOfSquares += (doubleWritable - mean) * (doubleWritable - mean);
            }

            context.write(new Text("Key:" + key + "   Median:   "), new DoubleWritable(median));
            context.write(new Text("Key:" + key + "   Standard Deviation:   "), new DoubleWritable((double) Math.sqrt(sumOfSquares / (count - 1))));
			for (double doubleWritable : temp) {
				context.write(new Text("Key:" + key + "   Normalized Sample:   "), new DoubleWritable((double) (doubleWritable - min) / (max - min)));
			}
        }
    }


    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.set("attributename", args[2]);

        Job job = Job.getInstance(conf, "Find Minimum and Maximum");

        job.setJarByClass(MedianMapReduce.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        job.setMapperClass(calculateMapper.class);
        job.setReducerClass(calculateReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
