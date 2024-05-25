package Test;

import Preprocess.Preprocessing;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapperTest extends Mapper<Object, Text, Text, Text> {
    private final Map<String, String> likelihood = new HashMap<>();
    private final Map<String, String> prior = new HashMap<>();
    private final Map<String, String> countClass = new HashMap<>();
    private String[] Classes = new String[0];
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();

        Path getPrior = new Path(conf.get("prior"));
        Path getLikelihood = new Path(conf.get("likelihood"));
        Path getCountClass = new Path(conf.get("countClass"));

        FileSystem fs = FileSystem.get(conf);

        FileStatus[] fileStatusesPrior = fs.listStatus(getPrior);
        for (FileStatus status : fileStatusesPrior) {
            Path filePath = status.getPath();
            if (status.isFile()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(filePath)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] kv = line.split("\t");
                        prior.put(kv[0], kv[1]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        FileStatus[] fileStatusesLikelihood = fs.listStatus(getLikelihood);
        for (FileStatus status : fileStatusesLikelihood) {
            Path filePath = status.getPath();
            if (status.isFile()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(filePath)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] kv = line.split("\t");
                        likelihood.put(kv[0], kv[1]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        FileStatus[] fileStatusesCountClass = fs.listStatus(getCountClass);
        for (FileStatus status : fileStatusesCountClass) {
            Path filePath = status.getPath();
            if (status.isFile()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(filePath)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] kv = line.split("\t");
                        countClass.put(kv[0], kv[1]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String allClasses = conf.get("allClasses").trim();
        Classes = allClasses.split(",");
    }

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String data = value.toString().trim();
        String[] parts = data.split("\t");

        String line = parts[0];
        String target = "";
        if(parts.length == 2){
            target = parts[1];
        }

        Preprocessing tokLowLem = new Preprocessing();
        List<String> tokens = tokLowLem.lemmatize(line);

        double probability = -1e9;
        String c_hat = Classes[0];
        Map<String, Double> class_probability = new HashMap<>();
        for (String c: Classes){
            double sumLogLikelihood = 0;
            double logPrior;
            double p;

            for (String token: tokens){
                if (likelihood.containsKey(token+","+c)) {
                    String[] xy = likelihood.get(token + "," + c).split("/");
                    double x = Double.parseDouble(xy[0]);
                    double y = Double.parseDouble(xy[1]);
                    sumLogLikelihood += Math.log(x / y);
                }else{
                    String countClass_c = countClass.get(c);
                    double y = Double.parseDouble(countClass_c);
                    sumLogLikelihood += Math.log(1 / y);
                }
            }

            String[] xy = prior.get(c).split("/");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            logPrior = Math.log(x / y);

            p = logPrior + sumLogLikelihood;
            class_probability.put(c,p);

            if(p > probability){
                c_hat = c;
                probability = p;
            }
        }
//        probability = Math.exp(probability);
        double z = 0;
        for (String c: class_probability.keySet()){
            z += Math.exp(class_probability.get(c));
        }
        for (String c: class_probability.keySet()){
            class_probability.put(c, Math.exp(class_probability.get(c)) / z);
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Double> entry : class_probability.entrySet()) {
            sb.append(entry.getKey())
                    .append(":")
                    .append(entry.getValue())
                    .append(",");
        }

        if (!class_probability.isEmpty()) {
            sb.setLength(sb.length() - 1); // Xóa dấu phẩy
        }

        String mapString = sb.toString();

        mapString = !Objects.equals(target, "") ? target + "\t" + mapString : mapString;

        Text newKey = new Text(line);
        Text newValue = new Text(mapString);
        context.write(newKey, newValue);

    }
}
