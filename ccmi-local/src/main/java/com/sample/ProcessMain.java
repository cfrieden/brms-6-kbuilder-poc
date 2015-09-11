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
	
	private static String drlString = "package com.rhc.demo;\n"
			+ "import java.math.BigDecimal;\n"
			+ "import org.joda.time.DateTime;\n"
			+ "rule \"Goodbye World\"\n"+
"ruleflow-group \"Assemble\"\n"+
"no-loop\n"+
"when\n"+
"	Object()\n"+
"then\n"+
"	System.out.println(\"GoodBye World!\");\n"+
"end";
	
	private static ReleaseId rid;
	
	public void main() {
		long time = System.currentTimeMillis();
		System.out.println(0);
		KieServices ks = KieServices.Factory.get();
		
		System.out.println(System.currentTimeMillis()-time);
		createKieFileSystemAndBuild(ks);
		long buildTime = System.currentTimeMillis()-time;
		System.out.println("Took" +Long.toString(System.currentTimeMillis()-time) + " ms to build all");
		
		// Add commands to ksession
		KieContainer kContainer = ks.newKieContainer(rid);

		System.out.println(ks.getRepository().getKieModule(rid));
		//ks.getKieClasspathContainer().updateToVersion(rid);
		//System.out.printks.newKieContainer(rid);
		StatelessKieSession ksession = kContainer.newStatelessKieSession("GeneratedSession");
		
		List<Object> facts = new ArrayList<Object>();
		facts.add(true);
		List<Command<?>> commands = new ArrayList<Command<?>>();
		KieCommands commandFactory = ks.getCommands();
		commands.add(commandFactory.newInsertElements(facts));
		commands.add(commandFactory.newStartProcess("sample.process"));
		commands.add(commandFactory.newFireAllRules());
		
		// Fire rules
		ExecutionResults results = ksession.execute(commandFactory.newBatchExecution(commands));
		System.out.println("results :" + results);
		
		String timeSpent = Long.toString(buildTime);
		System.out.println("time took to build:"+timeSpent);
		
	}
	
	private static String createKieFileSystemAndBuild(KieServices ks){
		// Create new KieFileSystem and add drl String
		String time = Long.toString(System.currentTimeMillis());
		KieFileSystem kFile = ks.newKieFileSystem();
		Resource kResource = ks.getResources().newReaderResource(new StringReader(drlString));
		kResource.setTargetPath("/tempDrl.drl");
		rid = ks.newReleaseId("com.rhc", "combined-kjar", "0.0.2-SNAPSHOT");
		kFile.generateAndWritePomXML(rid);
		kFile.write(kResource);  

		// Create KieModuleModel and add generated kmodule.xml
		KieModule kOldModule =ks.getRepository().getKieModule(ks.newReleaseId("com.rhc","ccmi-knowledge","0.0.1-SNAPSHOT") );
		KieModule kOldModule2 =ks.getRepository().getKieModule(ks.newReleaseId("com.rhc","ccmi-knowledge2","0.0.1-SNAPSHOT") );
		KieModule kOldModule3 =ks.getRepository().getKieModule(ks.newReleaseId("com.rhc","ccmi-knowledge3","0.0.1-SNAPSHOT") ); 
		
		KieModuleModel kModuleModel = ks.newKieModuleModel();
		KieBaseModel kBaseModel = kModuleModel.newKieBaseModel("genkbase")
		        .setDefault(false)
		        .addInclude("addkbase").addInclude("addkbase2").addInclude("addkbase3");
			kBaseModel.newKieSessionModel("GeneratedSession")
		        .setDefault(false)
		        .setType(KieSessionModel.KieSessionType.STATELESS);

		kFile.writeKModuleXML(kModuleModel.toXML());
		

        // Build builder
		KieBuilder kBuilder = ks.newKieBuilder(kFile);
		kBuilder.setDependencies(kOldModule, kOldModule2, kOldModule3);
		kBuilder.buildAll(); // where does this go to?
		//and do what with the kie builder?
		
		if (kBuilder.getResults().hasMessages(Message.Level.ERROR)) {
			for (Message message : kBuilder.getResults().getMessages(Message.Level.ERROR)) {
				System.out.println("blarg" +message.toString());
			}
		}
		return time;
	}
	
}