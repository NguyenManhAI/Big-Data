package Prior;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class MapperPrior extends Mapper<Object, Text, Text, IntWritable> {
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        String line = value.toString().trim();

        String[] parts = line.split("\t");

        context.write(new Text(parts[1]), new IntWritable(1));
    }
}
