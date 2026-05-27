package ca.concordia.encs.citydata.test.producers;

import org.junit.jupiter.api.Test;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;


import ca.concordia.encs.citydata.producers.BTUProducer;



/**
 * BTUProducer Tests
 *
 * @author @author Peter Yefi, Vinicius Mioto, Tahereh Bijani,  Mohamed Jendoubi
 * @since 2026-05-27
 */
public class BTUProducerTest {
	
	private BTUProducer btuProducer = null;
	private final String stringFilePath = "./src/test/resources/sample_btu.csv";
	
	@BeforeEach
	void setUp() {
		btuProducer = new BTUProducer(stringFilePath);
	}
	
	@Test
	void testThatFilePathMatch() {
		//Arrange and act
		btuProducer.fetch();
		Assert.assertEquals(btuProducer.getFilePath(), stringFilePath);
	}
	
	@Test
	void testThatResultSetMatchesFileContent() {
		btuProducer.fetch();
		
		String [] rowOne = btuProducer.getResult().getFirst().split(",");
		String [] lastRow = btuProducer.getResult().getLast().split(",");
		
		Assert.assertEquals(btuProducer.getResult().size(), 30);
		Assert.assertEquals(rowOne.length, 8);
		Assert.assertEquals(rowOne[0], "2024-10-01 04:50:00+00:00");
		Assert.assertEquals(lastRow[7], "20.518442");
		
	}

}
