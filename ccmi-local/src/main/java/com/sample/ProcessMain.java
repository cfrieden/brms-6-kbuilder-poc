package com.sample;

import java.io.StringReader;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.test.JBPMHelper;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.helper.FluentKieModuleDeploymentHelper;
import org.kie.api.builder.helper.KieModuleDeploymentHelper;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;

public class ProcessMain {
	
	private static String drlString = "";

	public static void main(String[] args) {
		KieServices ks = KieServices.Factory.get();

		createKieFileSystemAndBuild(ks);
		createKjarAndDeployToMaven("kjarGroupId", "kjarArtifactId");
		
		KieContainer kContainer = ks.getKieClasspathContainer();
		KieBase kbase = kContainer.getKieBase("addkbase");
		
		RuntimeManager manager = createRuntimeManager(kbase);
		RuntimeEngine engine = manager.getRuntimeEngine(null);
		KieSession ksession = engine.getKieSession();
		TaskService taskService = engine.getTaskService();

		ksession.startProcess("sample.process");

		// let john execute Task 1
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
		TaskSummary task = list.get(0);
		System.out.println("John is executing task " + task.getName());
		taskService.start(task.getId(), "john");
		taskService.complete(task.getId(), "john", null);

		// let mary execute Task 2
		list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
		task = list.get(0);
		System.out.println("Mary is executing task " + task.getName());
		taskService.start(task.getId(), "mary");
		taskService.complete(task.getId(), "mary", null);

		manager.disposeRuntimeEngine(engine);
		System.exit(0);
	}

	private static RuntimeManager createRuntimeManager(KieBase kbase) {
		JBPMHelper.startH2Server();
		JBPMHelper.setupDataSource();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
		RuntimeEnvironmentBuilder builder = RuntimeEnvironmentBuilder.Factory.get()
			.newDefaultBuilder().entityManagerFactory(emf)
			.knowledgeBase(kbase);
		return RuntimeManagerFactory.Factory.get()
			.newSingletonRuntimeManager(builder.get(), "com.sample:example:1.0");
	}
	
	private static String createKieFileSystemAndBuild(KieServices ks){
		// Create new KieFileSystem and add drl String
		String time = Long.toString(System.currentTimeMillis());
		KieFileSystem kFile = ks.newKieFileSystem();
		Resource kResource = ks.getResources().newReaderResource(new StringReader(drlString));
		kResource.setTargetPath("/tempDrl.drl");
		kFile.write(kResource);  

        // Build builder
		KieBuilder kBuilder = ks.newKieBuilder(kFile);
		kBuilder.buildAll();
		
		if (kBuilder.getResults().hasMessages(Message.Level.ERROR)) {
			for (Message message : kBuilder.getResults().getMessages(Message.Level.ERROR)) {
				System.out.println(message.toString());
			}
		}
		return time;
	}
	
	private static String createKjarAndDeployToMaven(String kjarGroupId, String kjarArtifactId) {
		// Create ModuleDeploymentHelper
		String time = Long.toString(System.currentTimeMillis());
		FluentKieModuleDeploymentHelper helper = KieModuleDeploymentHelper.newFluentInstance();

		// Create KModule
		KieModuleModel kModuleModel = helper.getKieModuleModel();
        KieBaseModel kBaseModel = kModuleModel.newKieBaseModel( "genkbase" )
        		.setDefault(false);
        KieSessionModel kSessionModel = kBaseModel.newKieSessionModel( "GeneratedSession" )
        		.setType(KieSessionModel.KieSessionType.STATELESS)
        		.setDefault(false);
        
        // Create KJAR and deploy to maven
		helper.setGroupId(kjarGroupId)
			.setArtifactId(kjarArtifactId)
			.setVersion(time);

		helper.createKieJarAndDeployToMaven();

		return time;

	}

}