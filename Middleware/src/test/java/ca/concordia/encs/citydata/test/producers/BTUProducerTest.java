package ca.concordia.encs.citydata.test.producers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;


import ca.concordia.encs.citydata.producers.BTUProducer;



/**
 * BTUProducer Tests
 *
 * @author @author Peter Yefi, Vinicius Mioto, Tahereh Bijani,  Mohamed Jendoubi
 * @since 2026-05-27
 * @author: Minette Z. Fixed the test by changing the imports, and using the right assert (assertEquals)
 * @date: 2026-05-29
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
		assertEquals(btuProducer.getFilePath(), stringFilePath);
	}
	
	@Test
	void testThatResultSetMatchesFileContent() {
		btuProducer.fetch();
		
		String [] rowOne = btuProducer.getResult().getFirst().split(",");
		String [] lastRow = btuProducer.getResult().getLast().split(",");
		
		assertEquals(btuProducer.getResult().size(), 30);
		assertEquals(rowOne.length, 8);
		assertEquals(rowOne[0], "2024-10-01 04:50:00+00:00");
		assertEquals(lastRow[7], "20.518442");
		
	}

}
