package com.sample;

import org.junit.runner.RunWith;

import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@Cucumber.Options( format={"json:target/tagCucumber.json", "html:target/cucumber/dev"}, tags = {"@wip","~@not_implemented"}, glue= {"com.sample"}, features= {"src/test/resources"})
public class CukesRunner {

}
