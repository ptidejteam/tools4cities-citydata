package ca.concordia.encs.citydata.test.producers;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.concordia.encs.citydata.producers.BTUProducer;

public class FCUProducerTest {
	
	private BTUProducer fcuProducer = null;
	private final String stringFilePath = "./src/test/resources/sample_fcu.csv";
	
	@BeforeEach
	void setUp() {
		fcuProducer = new BTUProducer(stringFilePath);
	}
	
	@Test
	void testThatFilePathMatch() {
		//Arrange and act
		fcuProducer.fetch();
		Assert.assertEquals(fcuProducer.getFilePath(), stringFilePath);
	}
	
	@Test
	void testThatResultSetMatchesFileContent() {
		fcuProducer.fetch();
		
		String [] rowOne = fcuProducer.getResult().getFirst().split(",");
		String [] lastRow = fcuProducer.getResult().getLast().split(",");
		
		
		Assert.assertEquals(fcuProducer.getResult().size(), 19);
		Assert.assertEquals(rowOne.length, 13);
		Assert.assertEquals(rowOne[0], "2022-03-02 02:15:00-05:00");
		Assert.assertEquals(lastRow[12], "-0.6838173");
		
	}
}
