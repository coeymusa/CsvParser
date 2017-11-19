package Abide.corey.project;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import junit.framework.Assert;

public class UtilityTest {

	Utility underTest = new Utility();

	@Test
	public void shouldCountTheSurgeriesWithinAGivenReion() throws IOException{
		int expected = 7;
		String region = "CLEVELAND";

		int result = underTest.regionSurgeryCount("src/test/resources/RegionSurgeryCountTest.csv", region);

		Assert.assertEquals(expected, result);
	}

	@Test(expected = IOException.class)
	public void reionSurgeryCountShouldThrowCannotFindFileException() throws IOException{
		underTest.regionSurgeryCount("RandomPath", "MIDDLESBROUGH");
	}

	@Test
	public void shouldCountTheAverageCostOfPerscriptionsWithMatchingName() throws IOException{
		String found= "5";
		String averageCost = "£1.10";
		String expected = String.format("Amount of perscriptions found: %s\nAverage actual cost of all Omeprazole: %s", found,averageCost);

		String result = underTest.perscriptionAverageCost("src/test/resources/PerscriptionTotalCostTest.csv", "Omeprazole");

		Assert.assertEquals(expected, result);
	}

	@Test(expected = IOException.class)
	public void perscriptionTotalCostCountShouldThrowCannotFindFileException() throws IOException{
		underTest.perscriptionAverageCost("RANDOMPATH", "Omeprazole");
	}

	@Test
	public void shouldCalculateTheTopSpendingPostCodes() throws IOException{
		String expected = "TS14 7DJ : £7.70\nTS18 2AW : £4.40\n";
		
		String result = underTest.costPerPostcodeCSV("src/test/resources/PostcodeSurgeryTest.csv", "src/test/resources/PostcodeTranscationTest.csv", "2");
		
		Assert.assertEquals(expected,result);
	}
	
	@Test
	public void shouldReturnAllPostcodesIfRequestedTopAmountExceedsAmountOfValidPostcodes() throws IOException{
		String expected = "TS14 7DJ : £7.70\nTS18 2AW : £4.40\nTS26 8DB : £3.30\nTS18 2AT : £1.10\n";
		
		String result = underTest.costPerPostcodeCSV("src/test/resources/PostcodeSurgeryTest.csv", "src/test/resources/PostcodeTranscationTest.csv", "1000");
		
		Assert.assertEquals(expected,result);
	}
	
	@Test(expected = IOException.class)
	public void postcodeSpendMethodShouldThrowCannotFindFileExceptionOnInvalidTransactionCSV() throws IOException{
		underTest.costPerPostcodeCSV("src/test/resources/PostcodeSurgeryTest.csv", "Random", "5");
	}
	
	@Test(expected = IOException.class)
	public void postcodeSpendMethodShouldThrowCannotFindFileExceptionOnInvalidSurgeryCSV() throws IOException{
		underTest.costPerPostcodeCSV("Random", "src/test/resources/PostcodeTranscationTest.csv", "5");
	}
	
	@Test
	public void shouldReturnAveragePriceOfGivenPerscriptionInEachReggion() throws IOException{
		String expected = "SUNDERLAND : £1.10 Which is 0.22x the national average\n"
				+ "CARDIFF : £1.60 Which is 0.32x the national average\n"
				+ "LONDON : £5.20 Which is 1.03x the national average\n"
				+ "CLEVELAND : £7.80 Which is 1.55x the national average\n"
				+ "NEWCASTLE : £10.10 Which is 2.01x the national average\n"
				+ "National average cost of a perscription £5.03";
		
		String result = underTest.forEachRegionCSV("src/test/resources/RegionSurgeryTest.csv", "src/test/resources/RegionTranscationTest.csv", "Pantoprazole");
		
		Assert.assertEquals(expected,result);
	}
	
	@Test
	public void shouldFindAmountOfPerscriptionsForSurgeryComparedToPCTAverage() throws FileNotFoundException{
		String expected= "Amount of perscriptions given in surgery : 3\n"
				+ "Average amount of perscriptions given within PCT of '5D7' : 4";
		
		 String result = underTest.perscriptionsGiven("src/test/resources/PCTQuantityTest.csv", "A86003");
		 
		 Assert.assertEquals(expected, result);

	}

}
