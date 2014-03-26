package com.horsehour.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.horsehour.datum.DataManager;
import com.horsehour.datum.DataSet;
import com.horsehour.datum.SampleSet;
import com.horsehour.datum.norm.Normalizer;
import com.horsehour.datum.norm.SumNormalizer;
import com.horsehour.filter.L2RLineParser;
import com.horsehour.filter.LineParserFilter;
import com.horsehour.math.MathLib;
import com.horsehour.metric.MAP;
import com.horsehour.metric.Metric;
import com.horsehour.metric.NDCG;
import com.horsehour.model.EnsembleModel;
import com.horsehour.model.Model;
import com.horsehour.ranker.trainer.DEARank;
import com.horsehour.ranker.trainer.RankTrainer;
import com.horsehour.util.FileManager;
import com.horsehour.util.Messenger;
import com.horsehour.util.TickClock;

/**
 * <p> Test Code for Evaluation of the ranking algorithm.
 * @author Chunheng Jiang
 * @version 3.0
 * @since 20131219
 */
public class Test_Eval {
	public RankTrainer ranker;

	public String predictFile;
	public String evalFile;

	public String trainFile;
	public String valiFile;
	public String testFile;

	public String workspace;

	public boolean storePredict = false;
	public boolean preprocess = false;
	public boolean normalize = false;//��׼������

	public Normalizer normalizer = new SumNormalizer();

	public DataSet trainset;
	public DataSet valiset;
	public DataSet testset;

	public Metric[] testMetrics;
	public Metric trainMetric;

	public Messenger msg;
	public int kcv;//k-cross-validation

	public Test_Eval(){
		int m = 11;
		testMetrics = new Metric[m];
		testMetrics[10] = new MAP();

		for(int k = 0; k < 10; k++)
			testMetrics[k] = new NDCG(k + 1);

		msg = new Messenger();
	}

	/**
	 * ����ѵ������֤�Ͳ������ݼ�
	 */
	public void loadDataSet(){
		LineParserFilter lineParser = new L2RLineParser();

		if(!trainFile.isEmpty())
			trainset = DataManager.loadDataSet(trainFile, lineParser);
		if(!valiFile.isEmpty())
			valiset = DataManager.loadDataSet(valiFile, lineParser);
		if(!testFile.isEmpty())
			testset = DataManager.loadDataSet(testFile, lineParser);

		if(trainset != null)
			if(preprocess)
				DataManager.preprocess(trainset);

		if(normalize){
			if(trainset != null)
				normalizer.normalize(trainset);

			if(valiset != null)
				normalizer.normalize(valiset);

			if(testset != null)
				normalizer.normalize(testset);
		}
	}

	/**
	 * �������ݼ�
	 * @param file
	 * @param normalize
	 */
	public DataSet loadDataSet(String file, boolean normalize){
		DataSet dataset;
		dataset = DataManager.loadDataSet(file, new L2RLineParser());

		if(normalize)
			normalizer.normalize(dataset);

		return dataset;
	}

	/**
	 * set up the experiments with kcv-cv
	 */
	public void setup(){
		ranker.trainMetric = trainMetric;

		evalFile = workspace + ".eval";

		trainFile = workspace + "train.txt";
		valiFile = workspace + "vali.txt";
		testFile = workspace + "test.txt"; 

		//file for prediction
		predictFile = workspace + ".score";
		//file for trained model
		ranker.modelFile = workspace + ".model";

		if(msg.get("oriented").equalsIgnoreCase("O"))
			msg.set("candidateFile", workspace + "OLPDEA.weak");
		else
			msg.set("candidateFile", workspace + "ILPDEA.weak");

		loadDataSet();
		ranker.trainset = trainset;
		ranker.valiset = valiset;
		ranker.msg = msg;

		conduct();
	}

	/**
	 * start up the ranking
	 * @param evalFile
	 */
	public void conduct(){
		ranker.train();

		double[][] predict = ranker.bestModel.predict(testset);
		if(storePredict && predictFile != null)
			store(predict, predictFile);			

		eval(predict, testset, testMetrics, evalFile);
	}

	/**
	 * make prediction using the model that trained by given trainer
	 * @param trainer
	 * @param modelFile
	 * @param dataFile
	 * @param output
	 */
	public void predict(RankTrainer trainer, String modelFile, String dataFile, String output){
		Model model = trainer.loadModel(modelFile);
		DataSet dataset = loadDataSet(dataFile, normalize);
		int m = dataset.size();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < m; i++){
			double[] predict = model.predict(dataset.getSampleSet(i));
			int n = predict.length;
			for(int j = 0; j < n; j++)
				sb.append(predict[j] + "\r\n");
		}

		FileManager.writeFile(output, sb.toString());
	}

	/**
	 * make prediction using given model
	 * @param modelFile
	 * @param dataFile
	 * @param output
	 */
	public void predict(String modelFile, String dataFile, String output){
		Model model = ranker.loadModel(modelFile);
		DataSet dataset = loadDataSet(dataFile, normalize);
		int m = dataset.size();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < m; i++){
			double[] predict = model.predict(dataset.getSampleSet(i));
			int n = predict.length;
			for(int j = 0; j < n; j++)
				sb.append(predict[j] + "\r\n");
		}

		FileManager.writeFile(output, sb.toString());
	}


	/**
	 * make prediction on given data set use given model
	 * @param model
	 * @param dataFile
	 * @param output
	 */
	public void predict(Model model, String dataFile, String output){
		DataSet dataset = loadDataSet(dataFile, normalize);
		int m = dataset.size();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < m; i++){
			double[] predict = model.predict(dataset.getSampleSet(i));
			int n = predict.length;
			for(int j = 0; j < n; j++)
				sb.append(predict[j] + "\r\n");
		}

		FileManager.writeFile(output, sb.toString());
	}

	/**
	 * make prediction on given data set use given model
	 * @param model
	 * @param dataset
	 * @param output
	 */
	public void predict(Model model, DataSet dataset, String output){
		int m = dataset.size();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < m; i++){
			double[] predict = model.predict(dataset.getSampleSet(i));
			int n = predict.length;
			for(int j = 0; j < n; j++)
				sb.append(predict[j] + "\r\n");
		}

		FileManager.writeFile(output, sb.toString());
	}

	/**
	 * evaluate the accuracy of predictions
	 * @param ranker
	 * @param predictFile
	 * @param dataFile
	 * @param metrics
	 * @param output
	 */
	public void eval(String predictFile, String dataFile, Metric[] metrics, String output)
	{
		List<String> predictLines = new ArrayList<String>(); 
		FileManager.readLines(predictFile, predictLines);

		DataSet dataset = loadDataSet(dataFile, normalize);
		int k = metrics.length;
		double[] perf = new double[k];

		int count = 0;
		int m = dataset.size();
		for(int i = 0; i < m; i++){
			List<Double> predict = new ArrayList<Double>();
			SampleSet sampleset = dataset.getSampleSet(i);
			int n = sampleset.size();
			for(int j = 0; j < n; j++){
				predict.add(Double.parseDouble(predictLines.get(count)));
				count++;
			}

			for(int j = 0; j < k; j++)
				perf[j] += metrics[j].measure(sampleset.getLabelList(), predict);
		}

		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < k; i++)
			sb.append(perf[i]/m + "\t");
		sb.append("\r\n");

		FileManager.writeFile(output, sb.toString());
	}

	/**
	 * evaluate the accuracy of predictions
	 * @param ranker
	 * @param predict
	 * @param dataFile
	 * @param metrics
	 * @param output
	 */
	public void eval(double[][] predict, DataSet dataset, Metric[] metrics, String output)
	{
		int k = metrics.length;
		double[] perf = new double[k];

		int m = dataset.size();
		for(int i = 0; i < m; i++){
			SampleSet sampleset = dataset.getSampleSet(i);
			for(int j = 0; j < k; j++)
				perf[j] += metrics[j].measure(sampleset.getLabels(), predict[i]);
		}

		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < k; i++)
			sb.append(perf[i]/m + "\t");
		sb.append("\r\n");

		FileManager.writeFile(output, sb.toString());
	}

	/**
	 * store the prediction results
	 * @param predict
	 * @param predictFile
	 */
	public void store(double[][] predict, String predictFile){
		StringBuffer sb = new StringBuffer();
		int size = predict.length;
		for(int i = 0; i < size; i++){
			int n = predict[i].length;

			for(int j = 0; j < n; j++)
				sb.append(predict[i][j] + "\r\n");
		}
		FileManager.writeFile(predictFile, sb.toString());
	}

	/**
	 * calculate the mean value of the evaluation results
	 * @param dir
	 * @param kcv k-cross-validation
	 */
	public void meanEval(String dir, int kcv){
		File[] files = FileManager.getFileList(dir);
		int len = files.length;
		for(int i = 0; i < len; i++){
			String name = files[i].getName();

			if(name.endsWith("eval")){
				name = name.substring(0, name.length() - 4);
				meanEval(dir + name + "eval", dir + name + "mean", kcv);
			}
		}
	}

	/**
	 * calculate the mean value of the evaluation results
	 * @param src
	 * @param dest
	 * @param kcv k-cross-validation
	 */
	public void meanEval(String src, String dest, int kcv){
		List<double[]> evalLine = DataManager.loadDatum(src, "\t");
		int m = evalLine.size();
		int n = m / kcv;//number of groups

		int dim = evalLine.get(0).length;
		for(int i = 0; i < n; i++){
			double[] meanEval = new double[dim];
			for(int j = 0; j < kcv; j++)
				meanEval = MathLib.add(meanEval, evalLine.get(5 * i + j));

			meanEval = MathLib.scale(meanEval, 0.2);

			StringBuffer sb = new StringBuffer();
			for(int j = 0; j < dim; j++)
				sb.append(meanEval[j] + "\t");
			sb.append("\r\n");

			FileManager.writeFile(dest, sb.toString());
		}
	}

	/**
	 * calculate the mean value of the evaluation results
	 * @param trainer
	 * @param src
	 */
	public void meanEval(RankTrainer trainer, String src){
		int dim = testMetrics.length;
		trainer.trainMetric = trainMetric;
		double[] meanEval = new double[dim];
		String evalFile = src + trainer.name();
		List<double[]> evalLine;

		for(int i = 1; i <= kcv; i++){
			evalLine = DataManager.loadDatum(evalFile + "-Fold" + i + ".eval", "\t");
			meanEval = MathLib.add(meanEval, evalLine.get(0));
		}
		meanEval = MathLib.scale(meanEval, (double) 1 / kcv);

		StringBuffer sb = new StringBuffer();
		for(int j = 0; j < dim; j++)
			sb.append(meanEval[j] + "\t");
		sb.append("\r\n");

		int idx = trainer.name().indexOf(".");
		evalFile = src + trainer.name().substring(0, idx);
		FileManager.writeFile(evalFile + ".mean", sb.toString());
	}

	/**
	 * select the learned ensemble model based on the given validation set, 
	 * and test it on the test set
	 * @param ens
	 * @param valiset
	 * @param metric
	 * @param evalFile
	 * @return the index of the best subensemble model
	 */
	public int selectModel(Model ens, DataSet valiset, DataSet testset, 
			Metric metric, String evalFile)
	{
		int sz = ((EnsembleModel) ens).size();
		int m = valiset.size();
		double[][] prediction = new double[m][];

		int bestId = -1;
		double best = 0;
		for(int i = 0; i < sz; i++){
			Model weak = ((EnsembleModel) ens).getModel(i);
			double alpha = ((EnsembleModel) ens).getWeight(i);

			double perf = 0;
			SampleSet sampleset;
			for(int j = 0; j < m; j++){
				sampleset = valiset.getSampleSet(j);
				if(prediction[j] == null)
					prediction[j] = new double[sampleset.size()];

				prediction[j] = MathLib.linearCombinate(prediction[j], 1.0,
						weak.predict(sampleset), alpha);

				perf += metric.measure(sampleset.getLabels(), prediction[j]);
			}

			perf /= m;

			if(perf > best){
				best = perf;
				bestId = i;
			}
		}

		Model model = ((EnsembleModel) ens).getSubEnsemble(0, bestId);
		prediction = model.predict(testset);
		eval(prediction, testset, testMetrics, evalFile);

		return bestId;
	}

	public static void main(String[] args) {
		TickClock.beginTick();

		String workspace = "";

		Test_Eval eval = new Test_Eval();
		
		eval.workspace = workspace;
		eval.storePredict = false;
		eval.normalize = false;
		eval.trainMetric = new NDCG(1);

		int niter = 200;
		eval.msg.setNumOfIter(niter);
		eval.msg.set("oriented", "O");

		eval.ranker = new DEARank();

		eval.setup();

		TickClock.stopTick();
	}
}