package com.sample;

import junit.framework.Assert;
import cucumber.api.java.en.When;

public class CukesSteps {
	
	ProcessMain processMain = new ProcessMain();
	
	@When("^I run main$")
	public void i_run_main(){
		processMain.main();
		Assert.assertTrue(true);
	}

}
