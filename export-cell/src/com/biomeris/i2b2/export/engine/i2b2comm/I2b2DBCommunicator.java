/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine.i2b2comm;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biomeris.i2b2.export.engine.ExportCellException;
import com.biomeris.i2b2.export.engine.ExportConstants;
import com.biomeris.i2b2.export.engine.io.misc.Concept;
import com.biomeris.i2b2.export.engine.io.misc.Modifier;
import com.biomeris.i2b2.export.engine.misc.ConceptAggregate;
import com.biomeris.i2b2.export.engine.misc.ObservationAggregator;
import com.biomeris.i2b2.export.engine.session.WExport;
import com.biomeris.i2b2.export.engine.session.WObservation;
import com.biomeris.i2b2.export.engine.session.WPatientInfo;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;

public class I2b2DBCommunicator {
	private String domain, project;
	private WExport export;

	private Properties cellProperties;

	private Connection demodataConnection;
	private String crc_dbDataSource;
	private String crc_dbFullSchema;
	private String crc_dbServerType;

	private List<String> patientIds;
	private String patientSetQuery;

	private boolean queryPresent;
	private PreparedStatement actualStatement;
	private ResultSet actualResultSet;
	private Concept actualConcept;

	private static Log log = LogFactory.getLog(I2b2DBCommunicator.class);

	public I2b2DBCommunicator(String domain, String project, WExport export, Properties exportCellProperties) {
		super();
		this.domain = domain;
		this.project = project;
		this.export = export;
		this.cellProperties = exportCellProperties;
	}

	public void init() throws SQLException, I2B2Exception, ExportCellException {
		if (demodataConnection == null) {
			openDemodataConnection();
		}

		if (patientIds == null) {
			retrievePatientIds(export.getPatSetId());
		}
	}

	public List<String> getPatientIds() {
		return patientIds;
	}

	public List<WPatientInfo> getPatientinfo() throws SQLException {
		List<WPatientInfo> output = new ArrayList<>();

		String patientInfoQuery = "SELECT * FROM " + crc_dbFullSchema + ".patient_dimension WHERE patient_num IN (" + patientSetQuery + ")";

		try (Statement pInfoStmt = demodataConnection.createStatement(); ResultSet pInfoRS = pInfoStmt.executeQuery(patientInfoQuery)) {
			while (pInfoRS.next()) {
				WPatientInfo wpi = new WPatientInfo();

				wpi.setPatientId(pInfoRS.getString("patient_num"));
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isVitalStatus()) {
					wpi.setVitalStatus(pInfoRS.getString("vital_status_cd"));
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isBirthDate()) {
					Date bDate = pInfoRS.getDate("birth_date");
					if (bDate != null) {
						Calendar birthDate = Calendar.getInstance();
						birthDate.setTime(bDate);
						wpi.setDateOfBirth(birthDate);
					}
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isDeathDate()) {
					Date dDate = pInfoRS.getDate("death_date");
					if (dDate != null) {
						Calendar deathDate = Calendar.getInstance();
						deathDate.setTime(dDate);
						wpi.setDateOfDeath(deathDate);
					}
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isSex()) {
					wpi.setSex(pInfoRS.getString("sex_cd"));
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isLanguage()) {
					wpi.setLanguage(pInfoRS.getString("language_cd"));
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isRace()) {
					wpi.setRace(pInfoRS.getString("race_cd"));
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isMaritalStatus()) {
					wpi.setMaritalStatus(pInfoRS.getString("marital_status_cd"));
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isReligion()) {
					wpi.setReligion(pInfoRS.getString("religion_cd"));
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isZipCode()) {
					wpi.setZipCode(pInfoRS.getString("zip_cd"));
				}
				if (export.getExportParams().getPatientSheetElements() == null || export.getExportParams().getPatientSheetElements().isIncome()) {
					wpi.setIncome(pInfoRS.getString("income_cd"));
				}

				output.add(wpi);
			}
		}

		return output;
	}

	public void nameTheChildren(Concept concept) throws SQLException {
		String conceptChildrenNamesQuery = "SELECT concept_cd, name_char FROM " + crc_dbFullSchema + "." + concept.getTableName() + " WHERE " + concept.getColumnName() + " LIKE '" + escape4SQL(concept.getDimCode(), crc_dbServerType) + "%'";

		try (Statement chNamesStmt = demodataConnection.createStatement(); ResultSet chNamesRS = chNamesStmt.executeQuery(conceptChildrenNamesQuery)) {
			while (chNamesRS.next()) {
				String conCd = chNamesRS.getString("concept_cd");
				String name = chNamesRS.getString("name_char");
				concept.getChildrenMap().put(conCd, name);
			}
		}

		if (concept.getModifier() != null) {
			Modifier modifier = concept.getModifier();
			String modifierChildrenNamesQuery = "SELECT modifier_cd, modifier_path FROM " + crc_dbFullSchema + "." + modifier.getTableName() + " WHERE " + modifier.getColumnName() + " LIKE '" + escape4SQL(modifier.getDimCode(), crc_dbServerType) + "%'";

			log.debug(modifierChildrenNamesQuery);
			
			try (Statement chNamesStmt = demodataConnection.createStatement(); ResultSet chNamesRS = chNamesStmt.executeQuery(modifierChildrenNamesQuery)) {
				while (chNamesRS.next()) {
					String modCd = chNamesRS.getString("modifier_cd");
					String name = chNamesRS.getString("modifier_path");
					modifier.getChildrenMap().put(modCd, name);
				}
			}
		}
	}

	public int countObservationsQuery(Concept concept) throws SQLException, ExportCellException {
		String conceptCdQuery = conceptCdQuery(concept);
		String modifierCdQuery = modifierCdQuery(concept.getModifier());

		String countQuery = "SELECT count(*) c FROM " + crc_dbFullSchema + ".observation_fact WHERE patient_num IN (" + patientSetQuery + ") AND concept_cd IN (" + conceptCdQuery + ")";
		if (modifierCdQuery != null) {
			countQuery += " AND modifier_cd IN(" + modifierCdQuery + ")";
		}

		int startDateParam = 0;
		if (export.getExportParams().getStartDate() != null) {
			countQuery += " AND start_date > ? ";
			startDateParam = 1;
		}

		int endDateParam = 0;
		if (export.getExportParams().getEndDate() != null) {
			countQuery += " AND start_date < ? ";
			endDateParam = startDateParam + 1;
		}

		PreparedStatement countStmt = null;
		ResultSet countRS = null;
		try {
			countStmt = demodataConnection.prepareStatement(countQuery);

			if (startDateParam > 0) {
				Timestamp start = new Timestamp(export.getExportParams().getStartDate().getTimeInMillis());
				countStmt.setTimestamp(startDateParam, start);
			}

			if (endDateParam > 0) {
				Timestamp end = new Timestamp(export.getExportParams().getEndDate().getTimeInMillis());
				countStmt.setTimestamp(endDateParam, end);
			}
			
			log.debug(countStmt);

			countRS = countStmt.executeQuery();

			if (countRS.next()) {
				int count = countRS.getInt("c");
				return count;
			} else{
				throw new ExportCellException("Something went wrong with count query");
			}
		}  finally {
			if (countRS != null) {
				countRS.close();
			}
			if (countStmt != null) {
				countStmt.close();
			}
		}
	}

	public void startObservationQuery(Concept concept) throws SQLException, ExportCellException {
		if (queryPresent) {
			throw new ExportCellException("Another query is already running");
		}

		String conceptCdQuery = conceptCdQuery(concept);
		String modifierCdQuery = modifierCdQuery(concept.getModifier());

		String observationQuery = "SELECT * FROM " + crc_dbFullSchema + ".observation_fact WHERE patient_num IN (" + patientSetQuery + ") AND concept_cd IN (" + conceptCdQuery + ")";
		if (modifierCdQuery != null) {
			observationQuery += " AND modifier_cd IN(" + modifierCdQuery + ")";
		}

		int startDateParam = 0;
		if (export.getExportParams().getStartDate() != null) {
			observationQuery += " AND start_date > ? ";
			startDateParam = 1;
		}

		int endDateParam = 0;
		if (export.getExportParams().getEndDate() != null) {
			observationQuery += " AND start_date < ? ";
			endDateParam = startDateParam + 1;
		}

		observationQuery += " order by patient_num";

		actualStatement = demodataConnection.prepareStatement(observationQuery);

		if (startDateParam > 0) {
			Timestamp start = new Timestamp(export.getExportParams().getStartDate().getTimeInMillis());
			actualStatement.setTimestamp(startDateParam, start);
		}

		if (endDateParam > 0) {
			Timestamp end = new Timestamp(export.getExportParams().getEndDate().getTimeInMillis());
			actualStatement.setTimestamp(endDateParam, end);
		}
		
		log.debug(actualStatement);

		actualResultSet = actualStatement.executeQuery();
		actualConcept = concept;
		queryPresent = true;
	}

	public List<WObservation> getAllObservations() throws SQLException, ExportCellException {
		return getAllObservations(-1);
	}

	public List<WObservation> getAllObservations(int length) throws SQLException, ExportCellException {
		List<WObservation> output = new ArrayList<>();

		if (!queryPresent) {
			throw new ExportCellException("You're trying to read from a null ResultSet");
		}

		if (length < 0) {
			while (actualResultSet.next()) {
				// read db and create observation
				WObservation wObservation = getObservationFromDb();

				// add observation to output
				output.add(wObservation);
			}
		} else {
			for (int i = 0; i < length; i++) {
				if (actualResultSet.next()) {
					// read db and create observation
					WObservation wObservation = getObservationFromDb();

					// add observation to output
					output.add(wObservation);
				}
			}
		}

		return output;
	}

	public Map<String, ConceptAggregate> getPatientAggregates() throws SQLException, ExportCellException {
		Map<String, ConceptAggregate> output = new HashMap<>();

		if (!queryPresent) {
			throw new ExportCellException("You're trying to read from a null ResultSet");
		}

		ObservationAggregator observationAggregator = new ObservationAggregator();

		while (actualResultSet.next()) {
			WObservation wObservation = getObservationFromDb();

			if (wObservation.getPatientId().equals(observationAggregator.getPatientId())) {
				observationAggregator.addObservation(wObservation, actualConcept.getChildrenMap().get(wObservation.getConceptCd()));
			} else {
				if (observationAggregator.hasData()) {
					ConceptAggregate ca = observationAggregator.makeAggregate();
					output.put(ca.getPatientId(), ca);
				}
				observationAggregator.clearAndSetUp(actualConcept, wObservation.getPatientId());
				observationAggregator.addObservation(wObservation, actualConcept.getChildrenMap().get(wObservation.getConceptCd()));
			}
		}

		if (observationAggregator.hasData()) {
			ConceptAggregate ca = observationAggregator.makeAggregate();
			output.put(ca.getPatientId(), ca);
			observationAggregator.clearAndSetUp(null, null);
		}

		return output;
	}

	public void stopQuery() throws SQLException, ExportCellException {
		if (!queryPresent) {
			throw new ExportCellException("You're trying to close null Statement and ResultSet");
		}

		actualResultSet.close();
		actualStatement.close();
		actualResultSet = null;
		actualStatement = null;
		actualConcept = null;
		queryPresent = false;
	}

	public void closeConnections() throws SQLException {
		if (demodataConnection != null) {
			demodataConnection.close();
		}
	}

	private WObservation getObservationFromDb() throws SQLException {
		// read db and create observation
		WObservation wObservation = new WObservation();

		wObservation.setPatientId(actualResultSet.getString("patient_num"));
		wObservation.setConceptCd(actualResultSet.getString("concept_cd"));
		wObservation.setInstanceNum(actualResultSet.getString("instance_num"));
		wObservation.setModifierCd(actualResultSet.getString("modifier_cd"));
		wObservation.setNvalNum(actualResultSet.getDouble("nval_num"));
		wObservation.setTvalChar(actualResultSet.getString("tval_char"));
		wObservation.setUnitsCd(actualResultSet.getString("units_cd"));

		Calendar startDate = Calendar.getInstance();
		startDate.setTime(actualResultSet.getDate("start_date"));
		wObservation.setStartDate(startDate);

		Date eDate = actualResultSet.getDate("end_date");
		if (eDate != null) {
			Calendar endDate = Calendar.getInstance();
			endDate.setTime(actualResultSet.getDate("end_date"));
			wObservation.setEndDate(endDate);
		}

		String valueTypeCd = actualResultSet.getString("valtype_cd");
		if (valueTypeCd == null || valueTypeCd.length() == 0) {
			valueTypeCd = ExportConstants.VALTYPE_NULL;
		}
		wObservation.setValueTypeCd(valueTypeCd);

		if (valueTypeCd.equals(ExportConstants.VALTYPE_BLOB)) {
			wObservation.setTvalChar(ExportConstants.VALTYPE_BLOB);
		}
		if (valueTypeCd.equals(ExportConstants.VALTYPE_AT)) {
			wObservation.setTvalChar(ExportConstants.VALTYPE_AT);
		}

		wObservation.setEncounterId(actualResultSet.getString("encounter_num"));

		return wObservation;
	}

	private String conceptCdQuery(Concept concept) {
		String out = "SELECT concept_cd FROM " + crc_dbFullSchema + "." + concept.getTableName() + " WHERE " + concept.getColumnName() + " LIKE '" + escape4SQL(concept.getDimCode(), crc_dbServerType) + "%' group by concept_cd";
		
		log.debug(out);
		
		return out;
	}

	private String modifierCdQuery(Modifier modifier) {
		if (modifier == null) {
			return null;
		}
		String out = "SELECT modifier_cd FROM " + crc_dbFullSchema + "." + modifier.getTableName() + " WHERE " + modifier.getColumnName() + " LIKE '" + escape4SQL(modifier.getDimCode(), crc_dbServerType) + "%' group by modifier_cd";
		
		log.debug(out);
		
		return out;
	}

	private void openDemodataConnection() throws SQLException, I2B2Exception, ExportCellException {
		String dbType = cellProperties.getProperty("exportcell.dbaccess.hive.db.type");
		String dbName = cellProperties.getProperty("exportcell.dbaccess.hive.db.name");
		String dbHost = cellProperties.getProperty("exportcell.dbaccess.hive.db.host");
		String dbPort = cellProperties.getProperty("exportcell.dbaccess.hive.db.port");
		String dbSid = cellProperties.getProperty("exportcell.dbaccess.hive.db.sid");
		String dbUsername = cellProperties.getProperty("exportcell.dbaccess.hive.db.username");
		String dbPassword = cellProperties.getProperty("exportcell.dbaccess.hive.db.password");

		switch (dbType) {
		case ExportConstants.POSTGRESQL_DB:
			try (Connection connection = DriverManager.getConnection("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName, dbUsername, dbPassword);) {
				String queryCrc = "SELECT c_db_datasource, c_db_fullschema, c_db_servertype FROM crc_db_lookup WHERE c_domain_id = '" + domain + "' AND c_project_path = '/" + project + "/'";

				log.debug(queryCrc);

				try (Statement crcStmt = connection.createStatement(); ResultSet crcRS = crcStmt.executeQuery(queryCrc);) {
					if (crcRS.next()) {
						crc_dbDataSource = crcRS.getString("c_db_datasource");
						crc_dbFullSchema = crcRS.getString("c_db_fullschema");
						crc_dbServerType = crcRS.getString("c_db_servertype");
					}
				}
			}
			break;
		case ExportConstants.ORACLE_DB:
			try (Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbSid, dbUsername, dbPassword);) {
				String queryCrc = "SELECT c_db_datasource, c_db_fullschema, c_db_servertype FROM crc_db_lookup WHERE c_domain_id = '" + domain + "' AND c_project_path = '/" + project + "/'";

				log.debug(queryCrc);

				try (Statement changeSchemaStmt = connection.createStatement();) {
					log.debug("ALTER SESSION SET CURRENT_SCHEMA = i2b2hive");
					changeSchemaStmt.executeUpdate("ALTER SESSION SET CURRENT_SCHEMA = i2b2hive");
				}

				try (Statement crcStmt = connection.createStatement(); ResultSet crcRS = crcStmt.executeQuery(queryCrc);) {
					if (crcRS.next()) {
						crc_dbDataSource = crcRS.getString("c_db_datasource");
						crc_dbFullSchema = crcRS.getString("c_db_fullschema");
						crc_dbServerType = crcRS.getString("c_db_servertype");
					}
				}
			}
			break;
		default:
			throw new ExportCellException("DB not supported, only POSTGRESQL and ORACLE");
		}

		ServiceLocator sl = ServiceLocator.getInstance();
		DataSource ds = (DataSource) sl.getAppServerDataSource(crc_dbDataSource);
		demodataConnection = ds.getConnection();
	}

	private void retrievePatientIds(String pSetId) throws SQLException {
		patientIds = new ArrayList<>();

		String patientsQuery = "SELECT patient_num FROM " + crc_dbFullSchema + ".qt_patient_set_collection WHERE result_instance_id = " + pSetId + " ORDER BY patient_num";
		patientSetQuery = patientsQuery;

		log.debug(patientsQuery);

		try (Statement patQueryStmt = demodataConnection.createStatement(); ResultSet patQueryRS = patQueryStmt.executeQuery(patientsQuery)) {
			while (patQueryRS.next()) {
				patientIds.add(patQueryRS.getString("patient_num"));
			}
		}

		Collections.sort(patientIds);
	}

	private String escape4SQL(String orig, String dbType) {
		String returnString = orig;
		switch (dbType) {
		case ExportConstants.POSTGRESQL_DB:
			returnString = orig.replaceAll("\\\\", "\\\\\\\\");
			returnString = returnString.replaceAll("\\(", "\\\\(");
			returnString = returnString.replaceAll("\\)", "\\\\)");
			break;
		case ExportConstants.ORACLE_DB:
			break;
		default:
			break;
		}
		return returnString;
	}
}
