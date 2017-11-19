package Abide.corey.project;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Functions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import Abide.corey.project.model.PostCodeSpend;

public class Utility {

	private static final int TRANSACTION__PCT = 1;
	private static final int TRANSACTION__SURGERY_ID = 2;
	private static final int TRANSACTION_BNF_CODE = 3;
	private static final int TRANSACTION_BNF_NAME= 4;
	private static final int TRANSACTION_QUANTITY= 5;
	private static final int TRANSACTION_PRICE = 7;

	private static final int SURGERY_ID = 1;
	private static final int SURGERY_NAME = 2;
	private static final int SURGERY_STREET= 4;
	private static final int SURGERY_REGION = 6;
	private static final int SURGERY_POSTCODE = 7;


	private static	Map<String, Integer> countMap = new HashMap<String,Integer>();

	/**
	 * Finds all prescriptions with a given name, and finds the average cost
	 * @param filePath
	 * @param perscriptionName
	 * @return String
	 * @throws IOException
	 */
	public String perscriptionAverageCost(String filePath,String perscriptionName) throws IOException{
		Double doubleCost = 0.00;
		int found=0;
		String[] row;

		FileReader reader = new FileReader(filePath);
		CsvParser parser = generateParser();

		parser.beginParsing(reader);

		while ((row = parser.parseNext()) != null) {
			if (row[TRANSACTION_BNF_NAME].equalsIgnoreCase(perscriptionName)){
				doubleCost += Double.valueOf(row[TRANSACTION_PRICE]);
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

	/**
	 * Finds the number of surgeries that match the region name
	 * @param filePath
	 * @param regionName
	 * @return Integer
	 * @throws IOException
	 */
	public int regionSurgeryCount(String filePath,String regionName) throws IOException{
		int found=0;
		String[] row;

		FileReader reader = new FileReader(filePath);
		CsvParser parser = generateParser();

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

	/**
	 * Finds the actual spend of all post codes and returns the top post codes up to {@link Integer} topParam
	 * 
	 * @param filePathSurgery
	 * @param filePathTransaction
	 * @param topParam
	 * @return
	 * @throws IOException
	 */
	public  String costPerPostcodeCSV(String filePathSurgery,String filePathTransaction, String topParam) throws IOException{
		String[] surgeryRow;
		String[] transactionRow;
		Map<String, Double> transactionMap = new HashMap<String,Double>();
		Multimap<String, String> surgeryMap = ArrayListMultimap.create();
		List<PostCodeSpend> resultList = new ArrayList<>();

		FileReader readerTransaction = new FileReader(filePathTransaction);
		CsvParser parserTransaction = generateParser();

		FileReader readerSurgery = new FileReader(filePathSurgery);
		CsvParser parserSurgery = generateParser();

		parserTransaction.beginParsing(readerTransaction);

		while ((transactionRow = parserTransaction.parseNext()) != null) {
			try{
				if (transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) == null){
					transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], Double.valueOf(transactionRow[TRANSACTION_PRICE]));
				} else {
					transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) + (Double.valueOf(transactionRow[TRANSACTION_PRICE])));
				}

			} catch (NumberFormatException  e){//to avoid parsing strings such as column names}
			}
		}

		parserSurgery.beginParsing(readerSurgery);
		while ((surgeryRow = parserSurgery.parseNext()) != null) {
			surgeryMap.put(surgeryRow[SURGERY_POSTCODE], surgeryRow[SURGERY_ID]);
		}

		for(String transactionSurgeryId : transactionMap.keySet()){
			for(Entry<String, String>  surgeryM : surgeryMap.entries()){
				if(surgeryM.getValue().contains(transactionSurgeryId)){
					PostCodeSpend pcs = new PostCodeSpend(surgeryM.getKey(), BigDecimal.valueOf(transactionMap.get(transactionSurgeryId)).setScale(2, RoundingMode.HALF_UP));
					resultList.add(pcs);	
					break;
				}
			}
		}

		resultList = sumDuplicatePostCodes(resultList);

		Collections.sort(resultList,new Comparator<PostCodeSpend>(){
			@Override
			public int compare(PostCodeSpend o1, PostCodeSpend o2) {
				return o2.getSpend().compareTo(o1.getSpend());
			}
		});

		StringBuilder sb = new StringBuilder();
		for (int i=0; i < Integer.valueOf(topParam) && i < resultList.size(); i++){
			sb.append(resultList.get(i).getPostcode() + " : £");
			sb.append(resultList.get(i).getSpend().toString() + "\n");
		}

		return sb.toString();

	}

	/**
	 * Finds the total amount prescriptions given for a surgery, and compares this to the average amount of prescriptions
	 * given within their Primary Care Trust 
	 * 
	 * @param filePathTransaction
	 * @param surgeryId
	 * @return String
	 * @throws FileNotFoundException
	 */
	public String perscriptionsGiven(String filePathTransaction, String surgeryId) throws FileNotFoundException {	
		FileReader readerTransaction = new FileReader(filePathTransaction);
		CsvParser parserTransaction = generateParser();
		parserTransaction.beginParsing(readerTransaction);
		String[] transactionRow;

		int surgeryCount=0;
		int pctCount = 0;
		List<String> surgeriesInPct = new ArrayList<String>();
		surgeriesInPct.add(surgeryId);

		String PCT = findPCT(surgeryId,filePathTransaction);
		while ((transactionRow = parserTransaction.parseNext()) != null) {
			if(transactionRow[TRANSACTION__SURGERY_ID].contains(surgeryId)){
				surgeryCount++;
			}
			if(transactionRow[TRANSACTION__PCT].contains(PCT)){
				Boolean idInList = false;
				for (int i= 0; i < surgeriesInPct.size();i++){
					if (surgeriesInPct.get(i).contains(transactionRow[TRANSACTION__SURGERY_ID])){
						idInList = true;
						break;
					}
				}
				if (idInList== false){
					surgeriesInPct.add(transactionRow[TRANSACTION__SURGERY_ID]);
				}
			}
			pctCount++;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Amount of perscriptions given in surgery : " + surgeryCount + "\n");
		sb.append("Average amount of perscriptions given within PCT of '" + PCT +"' : " + pctCount / surgeriesInPct.size());


		return sb.toString();
	}

	/**
	 * For each region found, finds the average price per prescription and compares this to the national average 
	 * 
	 * @param filePathSurgery
	 * @param filePathTransaction
	 * @param productCode
	 * @return
	 * @throws IOException
	 */
	public  String forEachRegionCSV(String filePathSurgery,String filePathTransaction, String productCode) throws IOException{

		Multimap<String, String> surgeryMap = populateSurgeryMap(filePathSurgery);

		Map<String, Double> transactionMap = populateTranscationMap(filePathTransaction,productCode);

		Map<String, Integer> regionTotalCountMap = assignSurgeryIdsToRegion(surgeryMap);

		Map<String, BigDecimal> regionTotalCostMap = sumTotalCostsOfSurgeriesWithinRegions(transactionMap,surgeryMap);

		Map<String, BigDecimal> averagedRegionPerscriptionMap = divideRegionTotalByCount(regionTotalCostMap,regionTotalCountMap);

		Map<String, BigDecimal> averagedSortedRegionPerscriptionMap  = ImmutableSortedMap.copyOf(averagedRegionPerscriptionMap, Ordering.natural().onResultOf(Functions.forMap(averagedRegionPerscriptionMap)));

		BigDecimal nationalAverageCostOfPerscription = generateNationalAverage(regionTotalCostMap,regionTotalCountMap);

		return generateResultsForRegion(averagedSortedRegionPerscriptionMap,nationalAverageCostOfPerscription);
	}


	/**
	 * Finds the primary care trust associated with a surgeryId
	 * 
	 * @param surgeryId
	 * @param filePathTransaction
	 * @return String 
	 * @throws FileNotFoundException
	 */
	private String findPCT(String surgeryId, String filePathTransaction) throws FileNotFoundException {
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader readerTransactiton = new FileReader(filePathTransaction);
		CsvParser parserTransaction = new CsvParser(settings);
		parserTransaction.beginParsing(readerTransactiton);
		String[] transactionRow;

		String PCT= null;

		while ((transactionRow = parserTransaction.parseNext()) != null) {
			if(transactionRow[TRANSACTION__SURGERY_ID].contains(surgeryId)){
				PCT = transactionRow[TRANSACTION__PCT];
				break;
			}
		}
		return PCT;
	}

	/**
	 * Finds duplicate postcodes and sums the values found
	 * 
	 * @param resultList
	 * @return List of {@link PostCodeSpend}
	 */
	private List<PostCodeSpend> sumDuplicatePostCodes(List<PostCodeSpend> resultList) {

		for(int i =0; i < resultList.size();i++){
			for(int j = i + 1; j < resultList.size();j++){
				if (resultList.get(i).getPostcode() == resultList.get(j).getPostcode()){
					PostCodeSpend pcs = new PostCodeSpend(resultList.get(i).getPostcode(), resultList.get(i).getSpend().add(resultList.get(j).getSpend()));
					resultList.remove(i);
					resultList.remove(j-1);
					resultList.add(pcs);
				}
			}

		}
		return resultList;
	}

	/**
	 * Reads values from a CSV and populates a map with the Surgery Region as key and Surgery ID as value 
	 * 
	 * @param filePathSurgery
	 * @return
	 * @throws FileNotFoundException
	 */
	private  Multimap<String, String> populateSurgeryMap(String filePathSurgery) throws FileNotFoundException {
		Multimap<String, String> surgeryMap = ArrayListMultimap.create();
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader readerSurgery = new FileReader(filePathSurgery);
		CsvParser parserSurgery = new CsvParser(settings);
		parserSurgery.beginParsing(readerSurgery);

		String[] surgeryRow;

		//Parse CSV into a map of surgeries with the region as key and id as value
		while ((surgeryRow = parserSurgery.parseNext()) != null) {
			surgeryMap.put(surgeryRow[SURGERY_REGION], surgeryRow[SURGERY_ID]);
		}

		return surgeryMap;

	}

	/**
	 * Divides the regional total cost of all prescriptions to the amount of prescriptions prescribed
	 * @param regionTotalCostMap
	 * @param regionTotalPerscriptionMap
	 * @return
	 */
	private  BigDecimal generateNationalAverage(Map<String, BigDecimal> regionTotalCostMap,
			Map<String, Integer> regionTotalPerscriptionMap) {

		int totalAmountofPerscriptions = 0;
		BigDecimal totalCostofPerscriptions = new BigDecimal(0);

		for (String regionName : regionTotalPerscriptionMap.keySet()){
			totalAmountofPerscriptions += regionTotalPerscriptionMap.get(regionName);
		}

		for(String regionName : regionTotalCostMap.keySet()){
			totalCostofPerscriptions =  regionTotalCostMap.get(regionName).add(totalCostofPerscriptions);
		}


		return totalCostofPerscriptions.divide(BigDecimal.valueOf(totalAmountofPerscriptions),2,RoundingMode.HALF_UP);
	}

	/**
	 * Finds surgeries that are within a region, and sums the accumulated cost for each surgery Id within a region
	 * @param transactionMap
	 * @param surgeryMap
	 * @return
	 */
	private  Map<String, BigDecimal> sumTotalCostsOfSurgeriesWithinRegions(Map<String, Double> transactionMap,
			Multimap<String, String> surgeryMap) {
		Map<String, BigDecimal> surgeryTotalCostMap = new HashMap<String,BigDecimal>();

		//Create a new map where each surgeries total actual persecription cost is added to the regions total
		for (String transactionSurgeryId : transactionMap.keySet()){
			for(Entry<String, String>  surgeryMapEntry : surgeryMap.entries()){
				if(surgeryMapEntry.getValue().contains(transactionSurgeryId)){
					if(surgeryTotalCostMap.get(surgeryMapEntry.getKey()) == null){
						surgeryTotalCostMap.put(surgeryMapEntry.getKey(),  BigDecimal.valueOf(transactionMap.get(transactionSurgeryId)).setScale(2, RoundingMode.HALF_UP));
					}else {
						surgeryTotalCostMap.put(surgeryMapEntry.getKey(),
								surgeryTotalCostMap.get(surgeryMapEntry.getKey())
								.add(BigDecimal.valueOf(transactionMap.get(transactionSurgeryId))
										.setScale(2, RoundingMode.HALF_UP)));
					}
				}
			}
		}

		return surgeryTotalCostMap;
	}

	/**
	 * Counts the amount of surgeries 
	 * @param surgeryMap
	 * @return
	 */
	private  Map<String, Integer> assignSurgeryIdsToRegion(Multimap<String, String> surgeryMap) {
		Map<String, Integer> countRegion = new HashMap<String,Integer>();

		//Create a new map using the count and surgery map. Summing the amount of perscriptions for each surgery associated within a region. To create a map with a region and the amount 
		//of perscriptions that region has had
		for (String summedPerscriptionId : countMap.keySet()){
			for(Entry<String, String>  surgeryMapEntry : surgeryMap.entries()){
				if(surgeryMapEntry.getValue().contains(summedPerscriptionId)){
					if(countRegion.get(surgeryMapEntry.getKey()) ==null){
						countRegion.put(surgeryMapEntry.getKey(),  countMap.get(summedPerscriptionId));
					}else {
						countRegion.put(surgeryMapEntry.getKey(),
								countRegion.get(surgeryMapEntry.getKey())+ countMap.get(summedPerscriptionId));
					}
				}
			}
		}
		return countRegion;
	}

	/**
	 * Divides a region summed total of prescription costs to the actual amount of prescriptions, to create an average 
	 * 
	 * @param resultSurgeryMap
	 * @param countRegion
	 * @return
	 */
	private  Map<String, BigDecimal> divideRegionTotalByCount(Map<String, BigDecimal> resultSurgeryMap,
			Map<String, Integer> countRegion) {

		Map<String, BigDecimal> averagedRegionPerscriptionMap = new HashMap<String,BigDecimal>();

		//Create a map of the average cost per prescription in each region. Using the amount of prescriptions per region to divide against the summed total of all prescription costs in a region 
		for(Entry<String, BigDecimal>  entryFromResultSurgeryTotal : resultSurgeryMap.entrySet()){
			if (entryFromResultSurgeryTotal.getValue() != null && entryFromResultSurgeryTotal.getClass() != null){
				for (String regionFromSummedPCount : countRegion.keySet()){
					if (regionFromSummedPCount != null && entryFromResultSurgeryTotal.getKey() != null){
						if (regionFromSummedPCount.contains(entryFromResultSurgeryTotal.getKey())){
							BigDecimal divisor = BigDecimal.valueOf(countRegion.get(regionFromSummedPCount));
							averagedRegionPerscriptionMap.put(entryFromResultSurgeryTotal.getKey(), 
									resultSurgeryMap.get(entryFromResultSurgeryTotal.getKey()).divide(divisor, 2, RoundingMode.HALF_UP));
						}
					}
				}
			}
		}
		return averagedRegionPerscriptionMap;
	}

	/**
	 * Creates a string of the results from the regional average perscription cost and national perscription cost
	 * 
	 * @param averagedRegionPerscriptionMap
	 * @param nationalAverageMap
	 * @return
	 */
	private  String generateResultsForRegion(Map<String, BigDecimal> averagedRegionPerscriptionMap, BigDecimal nationalAverageMap) {
		StringBuilder sb = new StringBuilder();

		for (Entry<String,BigDecimal> entry : averagedRegionPerscriptionMap.entrySet()){
			sb.append(entry.getKey() + " : £");
			sb.append(entry.getValue());
			sb.append(" Which is ");
			sb.append(entry.getValue().divide(nationalAverageMap,2,RoundingMode.HALF_UP));
			sb.append("x");
			sb.append(" the national average");
			sb.append("\n");
		}

		sb.append("National average cost of a perscription £" + nationalAverageMap);

		return sb.toString();
	}

	/**
	 * Finds transactions with a matching product name to one give
	 * placing in a new map with a key of the surgery ID and value of the price for the found transaction 
	 * 
	 * @param filePathTransaction
	 * @param productCode
	 * @return
	 * @throws FileNotFoundException
	 */
	private  Map<String, Double> populateTranscationMap(String filePathTransaction, String productCode) throws FileNotFoundException {

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		FileReader readerTransactiton = new FileReader(filePathTransaction);
		CsvParser parserTransaction = new CsvParser(settings);
		parserTransaction.beginParsing(readerTransactiton);

		String[] transactionRow;
		HashMap<String, Double> transactionMap = new HashMap<String,Double>();

		//parse CSV into a map of transcations with key of surgery ID and value of the sum of all actual transaction price within that surgeries
		//also creating countMap where the surgeryId is the key and the amount of transactions associated with that surgery
		while ((transactionRow = parserTransaction.parseNext()) != null) {
			if(transactionRow[TRANSACTION_BNF_NAME].contains(productCode)){
				if (transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) == null && countMap.get(transactionRow[TRANSACTION__SURGERY_ID]) == null){
					transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], Double.valueOf(transactionRow[TRANSACTION_PRICE]));
					countMap.put(transactionRow[TRANSACTION__SURGERY_ID], 1);
				}
				else {
					transactionMap.put(transactionRow[TRANSACTION__SURGERY_ID], transactionMap.get(transactionRow[TRANSACTION__SURGERY_ID]) + (Double.valueOf(transactionRow[TRANSACTION_PRICE])));
					countMap.put(transactionRow[TRANSACTION__SURGERY_ID], countMap.get(transactionRow[TRANSACTION__SURGERY_ID]) + 1);
				}
			}
		}
		return transactionMap;
	}

	/**
	 * Creates a generic CSV parser with a line seperator of \n
	 * @return CSVParser
	 * @throws FileNotFoundException
	 */
	private CsvParser generateParser() throws FileNotFoundException {
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		CsvParser parser = new CsvParser(settings);		

		return parser;
	}
}
