package CountWordClass;

import Preprocess.Preprocessing;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


import java.io.IOException;

import java.util.List;
import java.util.Objects;

public class MapperWordClass extends Mapper<Object, Text, Text, IntWritable>{
    private final Text docClassKey = new Text();
    private final IntWritable countValue = new IntWritable();
    private String[] Classes = new String[0];
    private final Preprocessing tokLowLem = new Preprocessing();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException{
        Configuration conf = context.getConfiguration();
        String allClasses = conf.get("allClasses").trim();
        Classes = allClasses.split(",");
    }
    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        String line = value.toString().trim();
        String[] parts = line.split("\t");
        if (parts.length == 2) {
            String sentence = parts[0];
            String docClass = parts[1];

            List<String> tokens = tokLowLem.lemmatize(sentence);

            for (String token : tokens) {
                for(String Class: Classes){
                    if (Objects.equals(Class, docClass)){
                        countValue.set(1);
                    }else{
                        countValue.set(0);
                    }
                    docClassKey.set(token + "," + Class);
                    context.write(docClassKey, countValue);
                }

            }
        }
    }
}
