import CountClass.MapperClass;
import CountClass.ReducerClass;
import CountWordClass.MapperWordClass;
import CountWordClass.ReducerWordClass;
import Prior.MapperPrior;
import Prior.MapperProbabilityPrior;
import Prior.ReducerPrior;
import Likelihood.MapperProbability;
import Test.MapperTest;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class NaiveBayes {
    public static void main(String[] args) throws Exception {

        Path countWordClass = new Path("countWordClass");
        Path countClass = new Path("countClass");
        Path prior = new Path("prior");
        Path subprior = new Path("subprior");
        Path likelihood = new Path("likelihood");

        Configuration conf = new Configuration();
        conf.set("allClasses", args[2]);
        conf.set("countWordClass", countWordClass.toString());
        conf.set("countClass", countClass.toString());
        conf.set("prior", prior.toString());
        conf.set("subprior", subprior.toString());
        conf.set("likelihood", likelihood.toString());
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

        Job job5 = Job.getInstance(conf, "Test");
        job5.setJarByClass(NaiveBayes.class);
        job5.setMapperClass(MapperTest.class);

        job5.setOutputKeyClass(Text.class);
        job5.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job5, new Path(args[1]));
        FileOutputFormat.setOutputPath(job5, new Path(args[3]));

        System.exit(job5.waitForCompletion(true) ? 0 : 1);
    }
}
