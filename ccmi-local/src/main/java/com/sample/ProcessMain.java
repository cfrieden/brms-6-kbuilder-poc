package com.sample;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.io.Resource;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;

public class ProcessMain {
	
	private static String drlString = "";
	
	public static void main(String[] args) {
		KieServices ks = KieServices.Factory.get();
		
		drlStringInit();
		createKieFileSystemAndBuild(ks);
		
		// Add commands to ksession
		KieContainer kContainer = ks.getKieClasspathContainer();
		StatelessKieSession ksession = kContainer.newStatelessKieSession("GeneratedSession");
		
		List<Object> facts = new ArrayList<Object>();
		facts.add(true);
		List<Command<?>> commands = new ArrayList<Command<?>>();
		KieCommands commandFactory = ks.getCommands();
		commands.add(commandFactory.newInsertElements(facts));
		commands.add(commandFactory.newStartProcess("sample.process"));
		commands.add(commandFactory.newFireAllRules());
		
		ksession.addEventListener(new DefaultAgendaEventListener() {
		    public void afterMatchFired(AfterMatchFiredEvent event) {
		    	System.out.println(event.getMatch().getRule().getName());
		    }
		});
		
		// Fire rules
		ExecutionResults results = ksession.execute(commandFactory.newBatchExecution(commands));
		System.out.println("results :" + results);
	}
	
	private static String createKieFileSystemAndBuild(KieServices ks){
		// Create new KieFileSystem and add drl String
		String time = Long.toString(System.currentTimeMillis());
		KieFileSystem kFile = ks.newKieFileSystem();
		Resource kResource = ks.getResources().newReaderResource(new StringReader(drlString));
		kResource.setTargetPath("/tempDrl.drl");
		ReleaseId rid = ks.newReleaseId("com.rhc", "combined-kjar", "0.0.1-SNAPSHOT");
		kFile.generateAndWritePomXML(rid);
		kFile.write(kResource);  

		// Create KieModuleModel and add generated kmodule.xml
		KieModule kOldModule =ks.getRepository().getKieModule(ks.newReleaseId("com.rhc","ccmi-knowledge","0.0.1-SNAPSHOT") );
		
		KieModuleModel kModuleModel = ks.newKieModuleModel();
		KieBaseModel kBaseModel = kModuleModel.newKieBaseModel("genkbase")
		        .setDefault(false)
		        .addInclude("addkbase");
		KieSessionModel kSessionModel = kBaseModel.newKieSessionModel("GeneratedSession")
		        .setDefault(false)
		        .setType(KieSessionModel.KieSessionType.STATELESS);

		kFile.writeKModuleXML(kModuleModel.toXML());
		

        // Build builder
		KieBuilder kBuilder = ks.newKieBuilder(kFile);
		kBuilder.setDependencies(kOldModule);
		kBuilder.buildAll(); // where does this go to?
		//and do what with the kie builder?
		
		if (kBuilder.getResults().hasMessages(Message.Level.ERROR)) {
			for (Message message : kBuilder.getResults().getMessages(Message.Level.ERROR)) {
				System.out.println(message.toString());
			}
		}
		return time;
	}
	
	private static void drlStringInit() {
		drlString = "nice.";
	}
	
}