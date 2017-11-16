package Abide.corey.project;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import Abide.corey.project.model.PostCodeSpend;
import Abide.corey.project.model.Surgery;
import Abide.corey.project.model.SurgeryTransaction;
import Abide.corey.project.model.Transaction;

public class Utility {

	private static final int TRANSACTION__SURGERY_ID = 2;
	private static final int TRANSACTION_BNF_CODE = 3;
	private static final int TRANSACTION_BNF_NAME= 4;
	private static final int TRANSACTION_QUANTITY= 5;
	private static final int TRANSACTION_PRICE = 6;

	private static final int SURGERY_ID = 1;
	private static final int SURGERY_NAME = 2;
	private static final int SURGERY_STREET= 4;
	private static final int SURGERY_REGION = 6;
	private static final int SURGERY_POSTCODE = 7;

	public static String perscriptionCostreadCSV(String filePath,String perscriptionName) throws IOException{
		Double doubleCost = 0.00;
		int found=0;
		String[] row;

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader reader = new FileReader(filePath);
		CsvParser parser = new CsvParser(settings);

		parser.beginParsing(reader);

		while ((row = parser.parseNext()) != null) {
			if (row[TRANSACTION_BNF_NAME].equalsIgnoreCase(perscriptionName)){
				doubleCost += Double.valueOf(row[6]);
				found++;
			}
		}
		parser.stopParsing();

		BigDecimal totalCost = BigDecimal.valueOf(doubleCost);
		BigDecimal divsor = BigDecimal.valueOf(found);
		StringBuilder sb = new StringBuilder();
		sb.append("Amount of perscriptions found: " + found + "\n"); 
		sb.append("Average actual cost of all " + perscriptionName + ": £" + totalCost.divide(divsor, 2,  RoundingMode.HALF_UP));

		return sb.toString();
	}

	public static int practicesCountCSV(String filePath,String regionName) throws IOException{
		int found=0;
		String[] row;

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader reader = new FileReader(filePath);
		CsvParser parser = new CsvParser(settings);

		parser.beginParsing(reader);

		while ((row = parser.parseNext()) != null) {
			if(row[SURGERY_REGION] != null){
				if (StringUtils.containsIgnoreCase(row[SURGERY_REGION],regionName)){
					found++;
				}
			}
		}
		parser.stopParsing();

		return found;
	}

	public static String costPerPostcodeCSV(String filePathSurgery,String filePathTransaction, int topParam) throws IOException{
		String[] surgeryRow;
		String[] transactionRow;
		Map<String, Double> transactionMap = new HashMap<String,Double>();
		Multimap<String, String> surgeryMap = ArrayListMultimap.create();
		List<PostCodeSpend> resultList = new ArrayList<>();

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader readerTransactiton = new FileReader(filePathTransaction);
		CsvParser parserTransaction = new CsvParser(settings);
		parserTransaction.beginParsing(readerTransactiton);

		FileReader readerSurgery = new FileReader(filePathSurgery);
		CsvParser parserSurgery = new CsvParser(settings);
		parserSurgery.beginParsing(readerSurgery);

		while ((transactionRow = parserTransaction.parseNext()) != null) {
			try{
				if (transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) == null){
					transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], Double.valueOf(transactionRow[TRANSACTION_PRICE]));
				} else {
					transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) + (Double.valueOf(transactionRow[TRANSACTION_PRICE])));
				}

			} catch (NumberFormatException  e){}
		}

		while ((surgeryRow = parserSurgery.parseNext()) != null) {
			surgeryMap.put(surgeryRow[SURGERY_POSTCODE], surgeryRow[SURGERY_ID]);
		}

		for(String transactionSurgeryId : transactionMap.keySet()){
			for(Entry<String, String>  surgeryM : surgeryMap.entries()){
				if(surgeryM.getValue().contains(transactionSurgeryId)){
					PostCodeSpend pcs = new PostCodeSpend(surgeryM.getKey(), BigDecimal.valueOf(transactionMap.get(transactionSurgeryId)).setScale(2, RoundingMode.HALF_UP));
					resultList.add(pcs);				
				}
			}
		}

		Collections.sort(resultList,new Comparator<PostCodeSpend>(){
			@Override
			public int compare(PostCodeSpend o1, PostCodeSpend o2) {
				return o2.getSpend().compareTo(o1.getSpend());
			}
		});

		StringBuilder sb = new StringBuilder();
		for (int i=0; i < topParam; i++){
			sb.append(resultList.get(i).getPostcode() + " : £");
			sb.append(resultList.get(i).getSpend().toString() + "\n");
		}
		return sb.toString();

	}



	public static Object averageCostVsNational(List<Transaction> transcations,  List<Surgery> surgeries, String productCode) {
		//create a list of transactions of given product name - assigned to surgeries 
		List<SurgeryTransaction> surgeryTransactions = regionProductCost(transcations,surgeries,productCode);

		//Create unique set of regions from list
		Set<String> regions = createRegions(surgeryTransactions);

		//aggregate transactions to unique region
		List<RegionSpent> regionSpentList = matchTransactionToRegion(surgeryTransactions,regions);

		double nationalAverageSpent = 0;
		int nationalAveragePerscriptions = 0;
		for(int index=0; index < regionSpentList.size();index++){
			nationalAveragePerscriptions += regionSpentList.get(index).getAmountOfPerscriptions();
			nationalAverageSpent += regionSpentList.get(index).getSpend();
		}

		//build readable string of results
		StringBuilder sb = new StringBuilder();
		for(int index=0; index < regionSpentList.size();index++){
			BigDecimal avg = BigDecimal.valueOf(regionSpentList.get(index).getSpend() / regionSpentList.get(index).getAmountOfPerscriptions()).setScale(2, RoundingMode.HALF_UP);
			sb.append(regionSpentList.get(index).getName() + ":" +  "£"+  avg);
			sb.append("\n");
		}

		BigDecimal avg = BigDecimal.valueOf(nationalAverageSpent / nationalAveragePerscriptions).setScale(2, RoundingMode.HALF_UP);
		sb.append("National average cost per person: £" + avg);


		return sb.toString();
	}

	public static String forEachRegionCSV(String filePathSurgery,String filePathTransaction, String productCode) throws IOException{
		String[] surgeryRow;
		String[] transactionRow;

		//Multimap<String, Double> transactionMap = ArrayListMultimap.create();
		Map<String, Double> transactionMap = new HashMap<String,Double>();
		Map<String, Integer> countMap = new HashMap<String,Integer>();
		Multimap<String, String> surgeryMap = ArrayListMultimap.create();
		Map<String, BigDecimal> resultMap = new HashMap<String,BigDecimal>();


		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader readerTransactiton = new FileReader(filePathTransaction);
		CsvParser parserTransaction = new CsvParser(settings);
		parserTransaction.beginParsing(readerTransactiton);

		FileReader readerSurgery = new FileReader(filePathSurgery);
		CsvParser parserSurgery = new CsvParser(settings);
		parserSurgery.beginParsing(readerSurgery);

		while ((transactionRow = parserTransaction.parseNext()) != null) {
			try{
				if (transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) == null && countMap.get(transactionRow[TRANSACTION__SURGERY_ID]) == null){
					if(transactionRow[TRANSACTION_BNF_CODE].contains(productCode)){
						transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], Double.valueOf(transactionRow[TRANSACTION_PRICE]));
						countMap.put(transactionRow[TRANSACTION__SURGERY_ID], 1);
					}
				} else {
					if(transactionRow[TRANSACTION_BNF_CODE].contains(productCode)){
						transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) + (Double.valueOf(transactionRow[TRANSACTION_PRICE])));
						countMap.put(transactionRow[TRANSACTION__SURGERY_ID], countMap.get(transactionRow[TRANSACTION__SURGERY_ID] +1));
					}
				}

			} catch (NumberFormatException  e){}
		}

		while ((surgeryRow = parserSurgery.parseNext()) != null) {
			surgeryMap.put(surgeryRow[SURGERY_REGION], surgeryRow[SURGERY_ID]);
		}

		for (String transactionSurgeryId : transactionMap.keySet()){
			for(Entry<String, String>  surgeryM : surgeryMap.entries()){
				if(surgeryM.getValue().contains(transactionSurgeryId)){
					if(resultMap.get(surgeryM.getKey()) ==null){
						resultMap.put(surgeryM.getKey(),  BigDecimal.valueOf(transactionMap.get(transactionSurgeryId)).setScale(2, RoundingMode.HALF_UP));
					}else {
						resultMap.put(surgeryM.getKey(),
							resultMap.get(surgeryM.getKey())
										.add(BigDecimal.valueOf(transactionMap.get(transactionSurgeryId))
												.setScale(2, RoundingMode.HALF_UP)));
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		for (Entry<String,BigDecimal> entry : resultMap.entrySet()){
			sb.append(entry.getKey() + " : ");
			sb.append(entry.getValue() + "\n");
		} 

		return sb.toString();
	}

}
