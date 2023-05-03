package com.nuix.javaenginesimple.examples;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.nuix.javaenginesimple.EngineWrapper;
import com.nuix.javaenginesimple.LicenseFilter;
import com.nuix.enginebaseline.NuixDiagnostics;

import nuix.Case;
import nuix.Utilities;

/***
 * An example demonstrating creation of a new Nuix simple case.
 * 
 * See BasicInitializationExample for more details regarding the basic initialization steps being taken.
 * @author Jason Wells
 *
 */
public class CreateSimpleCaseExample {
	private static Logger logger = null;

	public static void main(String[] args) throws Exception {
		// Specify a custom location for our log files
		String logDirectory = String.format("%s/%s",System.getProperty("nuix.logDir"),DateTime.now().toString("YYYYMMDD_HHmmss"));

		// Create an instance of engine wrapper, which will do the work of getting the Nuix bits initialized.
		// Engine wrapper will need to know what directory you engine release resides.
		EngineWrapper wrapper = new EngineWrapper(System.getProperty("nuix.engineDir"), logDirectory);

		// Relying on log4j2 initializations in EngineWrapper creation, so we wait until after that to fetch our logger
		logger = LogManager.getLogger(CreateSimpleCaseExample.class);
		
		LicenseFilter licenseFilter = wrapper.getLicenseFilter();
		licenseFilter.setMinWorkers(4);
		licenseFilter.addRequiredFeature("CASE_CREATION");
		
		String licenseUserName = System.getProperty("License.UserName");
		String licensePassword = System.getProperty("License.Password");
		
		if(licenseUserName != null && !licenseUserName.trim().isEmpty()) {
			logger.info(String.format("License username was provided via argument -DLicense.UserName: %s",licenseUserName));
		}
		
		if(licensePassword != null && !licensePassword.trim().isEmpty()) {
			logger.info("License password was provided via argument -DLicense.Password");
		}
		
		try {
			wrapper.trustAllCertificates();
			wrapper.withCloudLicense(licenseUserName, licensePassword, new Consumer<Utilities>() {
				public void accept(Utilities utilities) {
					File caseDirectory = new File("D:\\Cases\\MyNuixCase");
					
					// Specify additional settings to use when creating case
					Map<String,Object> caseSettings = new HashMap<String,Object>();
					caseSettings.put("compound", false);
					caseSettings.put("name","My Nuix Simple Case");
					caseSettings.put("description", "A Nuix case created using the Java Engine API");
					caseSettings.put("investigator", "Investigator Name Here");
					
					Case nuixCase = null;
					
					try {
						logger.info(String.format("Creating case: %s",caseDirectory.toString()));
						nuixCase = utilities.getCaseFactory().create(caseDirectory,caseSettings);
						logger.info("Case created");
						
						// *** Do things with the case here ***
						
						// Note that nuixCase is closed in finally block below
					} catch (IOException exc) {
						logger.error(String.format("Error while creating case: %s",caseDirectory.toString()),exc);
					} finally {
						// Make sure we close the case
						if(nuixCase != null) {
							logger.info(String.format("Closing case: %s",caseDirectory.toString()));
							nuixCase.close();
						}
					}
				}
			});
			
		} catch (Exception e) {
			logger.error("Unhandled exception",e);
			NuixDiagnostics.saveDiagnosticsToDirectory("C:\\EngineDiagnostics");
		} finally {
			wrapper.close();
		}
	}
}

