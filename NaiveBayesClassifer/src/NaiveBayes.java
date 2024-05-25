import CountClass.MapperClass;
import CountClass.ReducerClass;
import CountWordClass.MapperWordClass;
import CountWordClass.ReducerWordClass;
import Prior.MapperPrior;
import Prior.MapperProbabilityPrior;
import Prior.ReducerPrior;
import Likelihood.MapperProbability;
import Test.MapperEvaluate;
import Test.MapperTest;
import Test.ReducerEvaluate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NaiveBayes {
    public static void main(String[] args) throws Exception {

        /*
        args[0]: file train
        args[1]: file test
        args[2]: list of classes
        args[3]: output
        args[4]: optional: true or false, if true, The system will be put into inference state
        * */
        if (args.length < 4) {
            throw new IllegalArgumentException("The output must be: <train> <test> <list of classes> <output> <optional true or false,default = true>");
        }
        boolean isEvaluate = true;
        if(args.length == 5){
            if (Objects.equals(args[4], "true")){
                isEvaluate = true;
            }
            else if (Objects.equals(args[4], "false")){
                isEvaluate = false;
            } else{
                throw new IllegalArgumentException("The optional not be legal");
            }
        }

        Path countWordClass = new Path("countWordClass");
        Path countClass = new Path("countClass");
        Path prior = new Path("prior");
        Path subprior = new Path("subprior");
        Path likelihood = new Path("likelihood");
        Path evaluate = new Path("evaluate");

        Configuration conf = new Configuration();
        conf.set("allClasses", args[2]);
        conf.set("countWordClass", countWordClass.toString());
        conf.set("countClass", countClass.toString());
        conf.set("prior", prior.toString());
        conf.set("subprior", subprior.toString());
        conf.set("likelihood", likelihood.toString());
        conf.set("evaluate", evaluate.toString());
        conf.setInt("NSentences", 0);

        FileSystem fs = FileSystem.get(conf);
        if(fs.exists(countWordClass))
            fs.delete(countWordClass, true);
        if(fs.exists(countClass))
            fs.delete(countClass, true);
        if(fs.exists(prior))
            fs.delete(prior, true);
        if(fs.exists(subprior))
            fs.delete(subprior, true);
        if(fs.exists(likelihood))
            fs.delete(likelihood, true);
        if(fs.exists(evaluate))
            fs.delete(evaluate, true);

        Job job = Job.getInstance(conf, "Count WordClass");

        job.setJarByClass(NaiveBayes.class);
        job.setMapperClass(MapperWordClass.class);
        job.setReducerClass(ReducerWordClass.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, countWordClass);

        job.waitForCompletion(true);

        /*job2*/

        Job job2 = Job.getInstance(conf, "Count Class");

        job2.setJarByClass(NaiveBayes.class);
        job2.setMapperClass(MapperClass.class);
        job2.setReducerClass(ReducerClass.class);

        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job2, countWordClass);
        FileOutputFormat.setOutputPath(job2, countClass);

        job2.waitForCompletion(true);

        /*job3*/
        Job job3 = Job.getInstance(conf, "Compute Probability");

        job3.setJarByClass(NaiveBayes.class);
        job3.setMapperClass(MapperProbability.class);

        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job3, countWordClass);
        FileOutputFormat.setOutputPath(job3, likelihood);

        job3.waitForCompletion(true);

        /*job4*/
        Job job4 = Job.getInstance(conf, "Prior");

        job4.setJarByClass(NaiveBayes.class);
        job4.setMapperClass(MapperPrior.class);
        job4.setReducerClass(ReducerPrior.class);

        job4.setOutputKeyClass(Text.class);
        job4.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job4, new Path(args[0]));
        FileOutputFormat.setOutputPath(job4, subprior);

        job4.waitForCompletion(true);

        Job subJob4 = Job.getInstance(conf, "Sub prior");
        subJob4.setJarByClass(NaiveBayes.class);
        subJob4.setMapperClass(MapperProbabilityPrior.class);

        subJob4.setOutputKeyClass(Text.class);
        subJob4.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(subJob4, subprior);
        FileOutputFormat.setOutputPath(subJob4, prior);

        subJob4.waitForCompletion(true);

        if(fs.exists(subprior))
            fs.delete(subprior, true);

        /*job5*/

        Job job5 = Job.getInstance(conf, "Test");
        job5.setJarByClass(NaiveBayes.class);
        job5.setMapperClass(MapperTest.class);

        job5.setOutputKeyClass(Text.class);
        job5.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job5, new Path(args[1]));
        FileOutputFormat.setOutputPath(job5, new Path(args[3]));

        job5.waitForCompletion(true);

        if (isEvaluate) {
            Job job6 = Job.getInstance(conf, "Evaluate");

            job6.setJarByClass(NaiveBayes.class);
            job6.setMapperClass(MapperEvaluate.class);
            job6.setReducerClass(ReducerEvaluate.class);

            job6.setOutputKeyClass(Text.class);
            job6.setOutputValueClass(IntWritable.class);

            FileInputFormat.addInputPath(job6, new Path(args[3]));
            FileOutputFormat.setOutputPath(job6, evaluate);

            job6.waitForCompletion(true);

            Map<String, Integer> confusion_matrix = new HashMap<>();
            FileStatus[] fileStatuses = fs.listStatus(evaluate);
            for (FileStatus status : fileStatuses) {
                Path filePath = status.getPath();
                if (status.isFile()) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(filePath)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] kv = line.split("\t");
                            confusion_matrix.put(kv[0], Integer.parseInt(kv[1]));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            DecimalFormat decimalFormat1 = new DecimalFormat("#.#");
            DecimalFormat decimalFormat3 = new DecimalFormat("#.###");

            System.out.println("CONFUSION MATRIX");
            System.out.println("TP:" + confusion_matrix.get("tp") + "\t" + "FP:" + confusion_matrix.get("fp"));
            System.out.println("FN:" + confusion_matrix.get("fn") + "\t" + "TN:" + confusion_matrix.get("tn"));

            int total = confusion_matrix.get("tp") +
                    confusion_matrix.get("fp") +
                    confusion_matrix.get("tn") +
                    confusion_matrix.get("fn");
            double accuracy = (double) (confusion_matrix.get("tp") + confusion_matrix.get("tn")) / total;
            double recall = (double) confusion_matrix.get("tp") / (confusion_matrix.get("tp") + confusion_matrix.get("fn"));
            double precision = (double) confusion_matrix.get("tp") / (confusion_matrix.get("tp") + confusion_matrix.get("fp"));
            double f1_score = 2 * precision * recall / (precision + recall);

            System.out.println("ACCURACY:" + "\t" + decimalFormat1.format(accuracy * 100) + "%");
            System.out.println("RECALL:" + "\t" + decimalFormat3.format(recall));
            System.out.println("PRECISION:" + "\t" + decimalFormat3.format(precision));
            System.out.println("F1 SCORE:" + "\t" + decimalFormat3.format(f1_score));
        }
//        System.exit(job6.waitForCompletion(true) ? 0 : 1);
    }
}
