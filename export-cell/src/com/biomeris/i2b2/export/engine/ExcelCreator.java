/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.biomeris.i2b2.export.engine.io.misc.Concept;
import com.biomeris.i2b2.export.engine.io.misc.PatientSheetElements;
import com.biomeris.i2b2.export.engine.misc.ConceptAggregate;
import com.biomeris.i2b2.export.engine.session.WExport;
import com.biomeris.i2b2.export.engine.session.WObservation;
import com.biomeris.i2b2.export.engine.session.WPatientInfo;

public class ExcelCreator {
	private Integer rowAccessWindowSize;

	private File excelFile;
	private File zipFile;
	private File outputFile;
	private SXSSFWorkbook workbook;
	private CellStyle dateStyle;
	private CellStyle headerStyle;
	private CellStyle leftSeparatorStyle;

	private Sheet currentSheet;
	private int currentSheetRow = 0;

	private int nextMaskedId = 1;
	private Map<String, Integer> maskedPatientMaps = new HashMap<>();

	private final String EXTENSION = ".xlsx";
	private final String ZIP_EXTENSION = ".zip";

	public ExcelCreator(Integer rowAccessWindowSize) {
		if (rowAccessWindowSize != null && rowAccessWindowSize > 0) {
			this.rowAccessWindowSize = rowAccessWindowSize;
		} else {
			this.rowAccessWindowSize = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;
		}
	}

	public ExcelCreator() {
		this(null);
	}

	public void createFileAndWorkbook(File exportDir, String exportName) {
		if (excelFile == null && zipFile == null && workbook == null) {
			excelFile = new File(exportDir, exportName + EXTENSION);
			zipFile = new File(exportDir, exportName + EXTENSION + ZIP_EXTENSION);
			
			workbook = new SXSSFWorkbook(rowAccessWindowSize);

			dateStyle = workbook.createCellStyle();
			dateStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("m/d/yy h:mm"));

			headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor((short) 1);
			headerStyle.setFillBackgroundColor((short) 41);
			headerStyle.setFillPattern((short) 17);
			headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

			leftSeparatorStyle = workbook.createCellStyle();
			leftSeparatorStyle.setBorderLeft(CellStyle.BORDER_THIN);
		}
	}

	public void addNewSheet(String name) {
		addNewSheet(name, true);
	}

	public void addNewSheet(String name, boolean freeze) {
		currentSheet = workbook.createSheet(name);
		currentSheetRow = 0;

		for (int i = 0; i < 14; i++) {
			currentSheet.setColumnWidth(i, 4000);
		}

		if (freeze) {
			currentSheet.createFreezePane(0, 1);
		}
	}

	public void colorTest() {
		for (short k = (short) 0; k < 3; k++) {
			currentSheet = workbook.createSheet("ct " + k);
			currentSheetRow = 0;

			for (short i = (short) 0; i < 82; i++) {
				Row headerRow_XLSX = currentSheet.createRow(currentSheetRow++);

				Cell c2 = headerRow_XLSX.createCell(0);
				c2.setCellValue("" + i);

				for (short j = (short) 0; j < 19; j++) {
					CellStyle colorStyle = workbook.createCellStyle();
					colorStyle.setFillForegroundColor(k);
					colorStyle.setFillBackgroundColor(i);
					colorStyle.setFillPattern(j);
					Cell c1 = headerRow_XLSX.createCell(j + 1);
					c1.setCellStyle(colorStyle);
					c1.setCellValue("" + j);
				}
			}
		}

	}

	public void writeHeader(List<String> headerElements) {
		Row headerRow_XLSX = currentSheet.createRow(currentSheetRow++);
		headerRow_XLSX.setHeight((short) 350);

		for (int i = 0; i < headerElements.size(); i++) {
			Cell c = headerRow_XLSX.createCell(i);
			c.setCellStyle(headerStyle);
			c.setCellValue(headerElements.get(i));
		}
	}

	public void writeObservations(Concept concept, List<WObservation> observations) {
		writeObservations(concept, observations, false);
	}

	public void writeObservations(Concept concept, List<WObservation> observations, boolean maskPatientids) {
		for (WObservation obs : observations) {
			Row row = currentSheet.createRow(currentSheetRow++);

			// 0
			if (maskPatientids) {
				String patientId = obs.getPatientId();
				Integer maskedId;
				if (maskedPatientMaps.containsKey(patientId)) {
					maskedId = maskedPatientMaps.get(patientId);
				} else {
					maskedId = nextMaskedId;
					maskedPatientMaps.put(patientId, nextMaskedId++);
				}
				row.createCell(0).setCellValue(maskedId);
			} else {
				row.createCell(0).setCellValue(obs.getPatientId());
			}

			// 1
			row.createCell(1).setCellValue(obs.getEncounterId());

			// 2
			Cell cell2 = row.createCell(2);
			cell2.setCellStyle(dateStyle);
			cell2.setCellValue(obs.getStartDate().getTime());

			// 3
			if (obs.getEndDate() != null) {
				Cell cell3 = row.createCell(3);
				cell3.setCellStyle(dateStyle);
				cell3.setCellValue(obs.getEndDate().getTime());
			}

			// 4
			row.createCell(4).setCellValue(obs.getInstanceNum());

			// 5
			row.createCell(5).setCellValue(concept.getName());

			// 6
			if (concept.getType().equals(ExportConstants.LEAF)) {
				row.createCell(6).setCellValue(obs.getValueTypeCd());
			} else {
				row.createCell(6).setCellValue(ExportConstants.FOLDER);
			}

			// 7
			row.createCell(7).setCellValue(obs.getConceptCd());

			// 8
			row.createCell(8).setCellValue(concept.getChildrenMap().get(obs.getConceptCd()));

			if (concept.getModifier() != null) {
				// 9
				row.createCell(9).setCellValue(concept.getModifier().getName());

				// 10
				row.createCell(10).setCellValue(obs.getModifierCd());

				// 11
				row.createCell(11).setCellValue(concept.getModifier().getChildrenMap().get(obs.getModifierCd()));
			}

			// 12
			// 13
			switch (obs.getValueTypeCd()) {
			case ExportConstants.VALTYPE_N:
				row.createCell(12).setCellValue(operator(obs.getTvalChar()));
				row.createCell(13).setCellValue(obs.getNvalNum());
				break;
			default:
				row.createCell(13).setCellValue(obs.getTvalChar());
				break;
			}

			// 14
			row.createCell(14).setCellValue(obs.getUnitsCd());
		}
	}

	public void writeAggregates(Map<String, List<ConceptAggregate>> patientAggregate, int conceptNumber) {
		writeAggregates(patientAggregate, conceptNumber, false);
	}

	public void writeAggregates(Map<String, List<ConceptAggregate>> patientAggregate, int conceptNumber, boolean maskPatientids) {
		Set<Integer> usedColumns = new HashSet<>();

		for (String patientId : patientAggregate.keySet()) {
			Row row = currentSheet.createRow(currentSheetRow++);

			// 0
			if (maskPatientids) {
				Integer maskedId;
				if (maskedPatientMaps.containsKey(patientId)) {
					maskedId = maskedPatientMaps.get(patientId);
				} else {
					maskedId = nextMaskedId;
					maskedPatientMaps.put(patientId, nextMaskedId++);
				}
				row.createCell(0).setCellValue(maskedId);
			} else {
				row.createCell(0).setCellValue(patientId);
			}
			usedColumns.add(0);

			for (int i = 0; i < patientAggregate.get(patientId).size(); i++) {
				ConceptAggregate ca = patientAggregate.get(patientId).get(i);

				if (ca == null) {
					row.createCell((i * 5) + 1).setCellStyle(leftSeparatorStyle);
					row.createCell((i * 5) + 2);
					row.createCell((i * 5) + 3);
					row.createCell((i * 5) + 4);
					row.createCell((i * 5) + 5);
					continue;
				}

				// (i*5)+1
				Cell countCell = row.createCell((i * 5) + 1);
				countCell.setCellStyle(leftSeparatorStyle);
				if (ca.getCount() != null) {
					countCell.setCellValue(ca.getCount());
					usedColumns.add((i * 5) + 1);
				}
				// (i*5)+2
				if (ca.getMean() != null) {
					row.createCell((i * 5) + 2).setCellValue(ca.getMean());
					usedColumns.add((i * 5) + 2);
				}
				// (i*5)+3
				if (ca.getSd() != null) {
					row.createCell((i * 5) + 3).setCellValue(ca.getSd());
					usedColumns.add((i * 5) + 3);
				}
				// (i*5)+4
				if (ca.getMedian() != null) {
					row.createCell((i * 5) + 4).setCellValue(ca.getMedian());
					usedColumns.add((i * 5) + 4);
				}
				// (i*5)+5
				if (ca.getMode() != null) {
					row.createCell((i * 5) + 5).setCellValue(ca.getMode());
					usedColumns.add((i * 5) + 5);
				}
			}

		}

		int colNum = (conceptNumber * 5) + 1;
		for (int i = 0; i < colNum; i++) {
			if (!usedColumns.contains(i)) {
				currentSheet.setColumnHidden(i, true);
			}
		}
	}

	public void writePatientInfo(List<WPatientInfo> patientInfos, PatientSheetElements pse) {
		writePatientInfo(patientInfos, pse, false);
	}

	public void writePatientInfo(List<WPatientInfo> patientInfos, PatientSheetElements pse, boolean maskPatientids) {
		for (WPatientInfo pInfo : patientInfos) {
			Row row = currentSheet.createRow(currentSheetRow++);

			int colIndex = 0;

			if (maskPatientids) {
				String patientId = pInfo.getPatientId();
				Integer maskedId;
				if (maskedPatientMaps.containsKey(patientId)) {
					maskedId = maskedPatientMaps.get(patientId);
				} else {
					maskedId = nextMaskedId;
					maskedPatientMaps.put(patientId, nextMaskedId++);
				}
				row.createCell(colIndex++).setCellValue(maskedId);
			} else {
				row.createCell(colIndex++).setCellValue(pInfo.getPatientId());
			}

			if (pse == null || pse.isVitalStatus()) {
				row.createCell(colIndex++).setCellValue(pInfo.getVitalStatus());
			}

			if (pse == null || pse.isBirthDate()) {
				if (pInfo.getDateOfBirth() != null) {
					Cell cell = row.createCell(colIndex++);
					cell.setCellStyle(dateStyle);
					cell.setCellValue(pInfo.getDateOfBirth().getTime());

					row.createCell(colIndex++).setCellValue(getAge(pInfo.getDateOfBirth()));
				} else {
					colIndex = colIndex + 2;
				}
			}

			if (pse == null || pse.isDeathDate()) {
				if (pInfo.getDateOfDeath() != null) {
					Cell cell = row.createCell(colIndex++);
					cell.setCellStyle(dateStyle);
					cell.setCellValue(pInfo.getDateOfDeath().getTime());
				} else {
					colIndex++;
				}
			}
			if (pse == null || pse.isSex()) {
				if (pInfo.getSex() != null) {
					row.createCell(colIndex++).setCellValue(pInfo.getSex());
				} else {
					colIndex++;
				}
			}
			if (pse == null || pse.isLanguage()) {
				if (pInfo.getLanguage() != null) {
					row.createCell(colIndex++).setCellValue(pInfo.getLanguage());
				} else {
					colIndex++;
				}
			}
			if (pse == null || pse.isRace()) {
				if (pInfo.getRace() != null) {
					row.createCell(colIndex++).setCellValue(pInfo.getRace());
				} else {
					colIndex++;
				}
			}
			if (pse == null || pse.isMaritalStatus()) {
				if (pInfo.getMaritalStatus() != null) {
					row.createCell(colIndex++).setCellValue(pInfo.getMaritalStatus());
				} else {
					colIndex++;
				}

			}
			if (pse == null || pse.isReligion()) {
				if (pInfo.getReligion() != null) {
					row.createCell(colIndex++).setCellValue(pInfo.getReligion());
				} else {
					colIndex++;
				}

			}
			if (pse == null || pse.isZipCode()) {
				if (pInfo.getZipCode() != null) {
					row.createCell(colIndex++).setCellValue(pInfo.getZipCode());
				} else {
					colIndex++;
				}

			}
			if (pse == null || pse.isIncome()) {
				if (pInfo.getIncome() != null) {
					row.createCell(colIndex++).setCellValue(pInfo.getIncome());
				} else {
					colIndex++;
				}

			}
		}
	}

	public void writeExportInfo(WExport export) {
		// User
		Row rowUser = currentSheet.createRow(currentSheetRow++);
		rowUser.createCell(0).setCellValue("User");
		rowUser.createCell(1).setCellValue(export.getUsername());

		// Export date
		Row rowDate = currentSheet.createRow(currentSheetRow++);
		rowDate.createCell(0).setCellValue("Export date");
		Cell todayCell = rowDate.createCell(1);
		todayCell.setCellStyle(dateStyle);
		todayCell.setCellValue(new Date());

		// Export type
		Row rowET = currentSheet.createRow(currentSheetRow++);
		rowET.createCell(0).setCellValue("Export type");
		rowET.createCell(1).setCellValue(export.getExportParams().getExportType());

		// Concepts and modifiers
		int conceptNum = 1;
		for (Concept c : export.getConcepts()) {
			Row rowC1 = currentSheet.createRow(currentSheetRow++);
			rowC1.createCell(0).setCellValue("Concept " + conceptNum + " (name)");
			rowC1.createCell(1).setCellValue(c.getName());
			Row rowC2 = currentSheet.createRow(currentSheetRow++);
			rowC2.createCell(0).setCellValue("Concept " + conceptNum + " (key)");
			rowC2.createCell(1).setCellValue(c.getItemKey());
			if (c.getModifier() != null) {
				Row rowM1 = currentSheet.createRow(currentSheetRow++);
				rowM1.createCell(0).setCellValue("Concept " + conceptNum + " (modifier name)");
				rowM1.createCell(1).setCellValue(c.getModifier().getName());
				Row rowM2 = currentSheet.createRow(currentSheetRow++);
				rowM2.createCell(0).setCellValue("Concept " + conceptNum + " (modifier key)");
				rowM2.createCell(1).setCellValue(c.getModifier().getModifierKey());
			}
			conceptNum++;
		}

		// Filter date ( min start date)
		if (export.getExportParams().getStartDate() != null) {
			Row rowSD = currentSheet.createRow(currentSheetRow++);
			rowSD.createCell(0).setCellValue("Min start date");
			Cell sdCell = rowSD.createCell(1);
			sdCell.setCellStyle(dateStyle);
			sdCell.setCellValue(export.getExportParams().getStartDate().getTime());
		}

		// Filter date (max start date)
		if (export.getExportParams().getEndDate() != null) {
			Row rowED = currentSheet.createRow(currentSheetRow++);
			rowED.createCell(0).setCellValue("Max start date");
			Cell sdCell = rowED.createCell(1);
			sdCell.setCellStyle(dateStyle);
			sdCell.setCellValue(export.getExportParams().getEndDate().getTime());
		}
	}

	public String writeWorkbookToFile() throws IOException {
		return writeWorkbookToFile(false);
	}

	public String writeWorkbookToFile(boolean zip) throws IOException {
		String output;

		FileOutputStream fileOut = new FileOutputStream(excelFile);
		workbook.write(fileOut);
		fileOut.close();

		workbook.dispose();
		workbook.close();
		workbook = null;

		if (zip) {
			zip();
			deleteOriginal();
			outputFile = zipFile;

			output = zipFile.getName();
		} else {
			outputFile = excelFile;

			output = excelFile.getName();
		}

		return output;
	}

	private int getAge(Calendar dateOfBirth) {
		long dob = dateOfBirth.getTimeInMillis();
		long now = Calendar.getInstance().getTimeInMillis();
		long diff = now - dob;
		long millisPerYear = (long) (1000 * 60 * 60 * 24 * 365.25);
		int age = (int) (diff / millisPerYear);
		return age;
	}

	private String operator(String operatorDb) {
		switch (operatorDb) {
		case "E":
			return "=";
		case "L":
			return "<";
		case "G":
			return ">";
		default:
			return "";
		}
	}

	private void deleteOriginal() {
		excelFile.delete();
	}

	private void zip() throws IOException {
		FileOutputStream zipFileOut = new FileOutputStream(zipFile);
		ZipOutputStream zipOutputStream = new ZipOutputStream(zipFileOut);

		ZipEntry zipEntry = new ZipEntry(excelFile.getName());
		zipOutputStream.putNextEntry(zipEntry);

		FileInputStream fileInputStream = new FileInputStream(excelFile);
		byte[] buf = new byte[1024];
		int bytesRead;

		while ((bytesRead = fileInputStream.read(buf)) > 0) {
			zipOutputStream.write(buf, 0, bytesRead);
		}

		fileInputStream.close();
		zipOutputStream.closeEntry();
		zipOutputStream.close();
		zipFileOut.close();
	}

	public File getOutputFile() {
		return outputFile;
	}
}
