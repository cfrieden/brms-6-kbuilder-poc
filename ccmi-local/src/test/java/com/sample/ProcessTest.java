package com.sample;

import org.junit.Before;
import org.junit.Test;

public class ProcessTest {
	
	private ProcessMain process;
	
	@Before
	public void setup(){
		process = new ProcessMain();
	}
	
	@Test
	public void shouldRunProcess(){
		process.main();
	}

}
