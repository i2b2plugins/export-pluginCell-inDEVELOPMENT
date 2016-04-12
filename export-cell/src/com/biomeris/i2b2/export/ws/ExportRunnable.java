/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.ws;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biomeris.i2b2.export.engine.ExcelCreator;
import com.biomeris.i2b2.export.engine.ExportCellException;
import com.biomeris.i2b2.export.engine.ExportConstants;
import com.biomeris.i2b2.export.engine.i2b2comm.I2b2DBCommunicator;
import com.biomeris.i2b2.export.engine.io.misc.Concept;
import com.biomeris.i2b2.export.engine.io.misc.PatientSheetElements;
import com.biomeris.i2b2.export.engine.misc.ConceptAggregate;
import com.biomeris.i2b2.export.engine.session.WExport;
import com.biomeris.i2b2.export.engine.session.WObservation;
import com.biomeris.i2b2.export.engine.session.WPatientInfo;
import com.biomeris.i2b2.export.engine.session.WSession;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public class ExportRunnable implements Runnable {
	private I2b2DBCommunicator i2b2Communicator;

	private WSession session;
	private WExport export;

	private Properties exportCellProperties;

	private static Log log = LogFactory.getLog(ExportRunnable.class);

	ExportRunnable(WSession session, WExport export) {
		super();
		this.export = export;
		this.session = session;

		InputStream is_i2b2 = ExportRunnable.class.getClassLoader().getResourceAsStream("conf/exportcell.properties");
		exportCellProperties = new Properties();
		try {
			exportCellProperties.load(is_i2b2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			innerRun();
		} catch (SQLException | I2B2Exception | ExportCellException | IOException e) {
			export.setStatus(ExportConstants.STATUS_ERROR);
			export.setException(e);
			log.debug(e);
		} catch (Exception e) {
			export.setStatus(ExportConstants.STATUS_ERROR);
			export.setException(new Exception("General server exception(" + e.getClass()+")"));
			log.debug(e);
			e.printStackTrace();
		}
	}

	private void innerRun() throws SQLException, I2B2Exception, ExportCellException, IOException, Exception {
		log.debug("Export " + export.getId() + " started");

		export.setStatus(ExportConstants.STATUS_WORKING);

		// create i2b2 communicator
		i2b2Communicator = new I2b2DBCommunicator(session.getNetwork().getDomain(), session.getNetwork().getProject(), export, exportCellProperties);

		// init i2b2 communicator
		i2b2Communicator.init();

		// create and set up excelCreator
		int rowAccessWindowSize = Integer.parseInt(exportCellProperties.getProperty("exportcell.excel.winsize"));
		ExcelCreator excelCreator = new ExcelCreator(rowAccessWindowSize);
		excelCreator.createFileAndWorkbook(export.getExportDir(), export.getName());

		int dbBlockRead = Integer.parseInt(exportCellProperties.getProperty("exportcell.dbaccess.block"));

		switch (export.getExportParams().getExportType()) {
		case ExportConstants.ALL_OBSERVATIONS:
			excelCreator.addNewSheet("Observations");

			// write header
			List<String> headerElementsAllObs = observationHeader();
			excelCreator.writeHeader(headerElementsAllObs);

			// for each concept
			for (Concept concept : export.getConcepts()) {
				int count = i2b2Communicator.countObservationsQuery(concept);
				if (count > Integer.parseInt(exportCellProperties.getProperty("exportcell.dbaccess.max_obs"))) {
					export.addProblem("Concept \"" + concept.getName() + "\" exceeds observations max number ("+exportCellProperties.getProperty("exportcell.dbaccess.max_obs")+"), it won't be included in the export.");
					continue;
				}

				i2b2Communicator.nameTheChildren(concept);

				i2b2Communicator.startObservationQuery(concept);

				List<WObservation> observations = i2b2Communicator.getAllObservations(dbBlockRead);
				while (observations.size() > 0) {
					excelCreator.writeObservations(concept, observations, export.getExportParams().isMaskPatientIds());
					observations = i2b2Communicator.getAllObservations(dbBlockRead);
				}

				i2b2Communicator.stopQuery();
			}
			break;
		case ExportConstants.PATIENT_AGGREGATE:
			excelCreator.addNewSheet("Aggregates");

			// write header
			List<String> headerElementsPAggr = patientAggregateHeader(export.getConcepts());
			excelCreator.writeHeader(headerElementsPAggr);

			Map<String, List<ConceptAggregate>> patientAggregates = new HashMap<>();

			try {
				List<String> patientIds = i2b2Communicator.getPatientIds();

				for (int i = 0; i < export.getConcepts().size(); i++) {
					Concept concept = export.getConcepts().get(i);

					int count = i2b2Communicator.countObservationsQuery(concept);
					if (count > Integer.parseInt(exportCellProperties.getProperty("exportcell.dbaccess.max_obs"))) {
						export.addProblem("Concept \"" + concept.getName() + "\" exceeds observations max number ("+exportCellProperties.getProperty("exportcell.dbaccess.max_obs")+"), it won't be included in the export.");
						continue;
					}

					i2b2Communicator.nameTheChildren(concept);

					i2b2Communicator.startObservationQuery(concept);
					Map<String, ConceptAggregate> conceptAggregates = i2b2Communicator.getPatientAggregates();

					for (String pId : patientIds) {
						if (!patientAggregates.containsKey(pId)) {
							patientAggregates.put(pId, new ArrayList<ConceptAggregate>());
						}

						if (conceptAggregates.containsKey(pId)) {
							patientAggregates.get(pId).add(conceptAggregates.get(pId));
						} else {
							patientAggregates.get(pId).add(null);
						}
					}

					i2b2Communicator.stopQuery();
				}

				excelCreator.writeAggregates(patientAggregates, export.getConcepts().size(), export.getExportParams().isMaskPatientIds());

			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			break;
		default:
			throw new ExportCellException("Unsupported export type ("+export.getExportParams().getExportType()+")");
		}

		// patient info sheet
		if (export.getExportParams().isPatientSheet()) {
			excelCreator.addNewSheet("Patients");

			// write header
			List<String> headerElementsPInfo = patientInfoHeader(export.getExportParams().getPatientSheetElements());
			excelCreator.writeHeader(headerElementsPInfo);

			List<WPatientInfo> patientInfoList = i2b2Communicator.getPatientinfo();
			excelCreator.writePatientInfo(patientInfoList, export.getExportParams().getPatientSheetElements(), export.getExportParams().isMaskPatientIds());
		}

		// export info sheet
		excelCreator.addNewSheet("Info", false);
		excelCreator.writeExportInfo(export);

		// write excel file
		excelCreator.writeWorkbookToFile(export.getExportParams().isZip());
		export.setExportFile(excelCreator.getOutputFile());
		export.setStatus(ExportConstants.STATUS_DONE);
		
		session.updateLastAccess();

		// last: release resources
		i2b2Communicator.closeConnections();
	}

	private List<String> patientInfoHeader(PatientSheetElements patientSheetElements) {
		List<String> output = new ArrayList<>();

		output.add("Patient Id");

		if (patientSheetElements == null || patientSheetElements.isVitalStatus()) {
			output.add("Vital status");
		}
		if (patientSheetElements == null || patientSheetElements.isBirthDate()) {
			output.add("Birth date");
			output.add("Age");
		}
		if (patientSheetElements == null || patientSheetElements.isDeathDate()) {
			output.add("Death date");
		}
		if (patientSheetElements == null || patientSheetElements.isSex()) {
			output.add("Sex");
		}
		if (patientSheetElements == null || patientSheetElements.isLanguage()) {
			output.add("Language");
		}
		if (patientSheetElements == null || patientSheetElements.isRace()) {
			output.add("Race");
		}
		if (patientSheetElements == null || patientSheetElements.isMaritalStatus()) {
			output.add("Marital status");
		}
		if (patientSheetElements == null || patientSheetElements.isReligion()) {
			output.add("Religion");
		}
		if (patientSheetElements == null || patientSheetElements.isZipCode()) {
			output.add("Zip code");
		}
		if (patientSheetElements == null || patientSheetElements.isIncome()) {
			output.add("Income");
		}

		return output;
	}

	private List<String> observationHeader() {
		List<String> output = new ArrayList<>();

		// 0
		output.add("Patient Id");
		// 1
		output.add("Encounter Num");
		// 2
		output.add("Start date");
		// 3
		output.add("End date");
		// 4
		output.add("Instance num");
		// 5
		output.add("Concept");
		// 6
		output.add("Type");
		// 7
		output.add("Leaf code");
		// 8
		output.add("Leaf name");
		// 9
		output.add("Modifier");
		// 10
		output.add("Modifier code");
		// 11
		output.add("Modifier name");
		// 12
		output.add("Operator");
		// 13
		output.add("Value");
		// 14
		output.add("Unit");

		return output;
	}

	private List<String> patientAggregateHeader(List<Concept> concepts) {
		List<String> output = new ArrayList<>();

		// 0
		output.add("Patient Id");

		for (Concept concept : concepts) {
			String name = concept.getName();

			// (n*5)+1
			output.add(name + " (count)");

			// (n*5)+2
			output.add(name + " (mean)");

			// (n*5)+3
			output.add(name + " (sd)");

			// (n*5)+4
			output.add(name + " (median)");

			// (n*5)+5
			output.add(name + " (mode)");
		}

		return output;
	}

}
