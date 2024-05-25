package Test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Objects;

public class MapperEvaluate extends Mapper<Object, Text, Text, IntWritable> {
    private String[] Classes = new String[0];
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        String allClasses = conf.get("allClasses").trim();
        Classes = allClasses.split(",");
    }
    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] parts = line.split("\t");
        // Text, actual value, probability
        String actual_value = parts[1];
        String proba = parts[2];

        String[] pClasses = proba.split(",");
        String bestValue = "";
        double bestp = -1e9;
        for (String pClass: pClasses){
            String[] pC = pClass.split(":");
            String C = pC[0];
            double p = Double.parseDouble(pC[1]);
            if (p > bestp){
                bestp = p;
                bestValue = C;
            }
        }

        if (Objects.equals(actual_value, Classes[0])){
            if (Objects.equals(actual_value, bestValue)){
                context.write(new Text("tp"), new IntWritable(1));
            } else{
                context.write(new Text("fn"), new IntWritable(1));
            }
        } else{
            if (Objects.equals(actual_value, bestValue)){
                context.write(new Text("tn"), new IntWritable(1));
            } else{
                context.write(new Text("fp"), new IntWritable(1));
            }
        }

    }
}
