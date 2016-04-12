/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */

//run export
function runExport() {
    //build json
    var json = new Object();
    //sessionId
    json.sessionId = i2b2.Export.Workspace.sessionId;
    //name
    var nameInput = $j("#ex_nameInput");
    if (nameInput.val().length == 0) {
        json.name = nameInput.attr("placeholder");
    } else {
        json.name = nameInput.val();
    }
    //newPassword
    var password = i2b2.h.getPass();
    var passwdReplaced = password.replace(/</g, "\&lt;");
    passwdReplaced = passwdReplaced.replace(/>/g, "\&gt;");
    json.newPassword = passwdReplaced;
    //patientSetId
    json.patientSetId = i2b2.Export.Workspace.pSet_SDX.origData.PRS_id;
    //patientSetName
    // json.patientSetName = i2b2.Export.Workspace.pSet_SDX.parent.parent.origData.name;
    json.patientSetName = i2b2.Export.Workspace.pSet_SDX.origData.title;
    //exportParams
    json.exportParams = new Object();
    //exportType
    var cval = $j("input[name=ex_type]:checked").val();
    if (cval === "obs") {
        json.exportParams.exportType = "OBS";
    } else {
        json.exportParams.exportType = "PAT";
    }
    //startDate
    if ($j("#ex_afterInput").val()) {
        json.exportParams.startDate = new Object();
        var sDate = $j("#ex_afterInput").datepicker("getDate");
        json.exportParams.startDate.year = sDate.getFullYear();
        json.exportParams.startDate.month = sDate.getMonth();
        json.exportParams.startDate.dayOfMonth = sDate.getDate();
    }
    //endDate
    if ($j("#ex_beforeInput").val()) {
        json.exportParams.endDate = new Object();
        var eDate = $j("#ex_beforeInput").datepicker("getDate");
        json.exportParams.endDate.year = eDate.getFullYear();
        json.exportParams.endDate.month = eDate.getMonth();
        json.exportParams.endDate.dayOfMonth = eDate.getDate();
    }
    //maskPatientIds
    if ($j("#ex_maskCheck:checkbox:checked").length > 0) {
        json.exportParams.maskPatientIds = true;
    } else {
        json.exportParams.maskPatientIds = false;
    }
    //zip
    if ($j("#ex_zipCheck:checkbox:checked").length > 0) {
        json.exportParams.zip = true;
    } else {
        json.exportParams.zip = false;
    }
    //patientSheet
    if ($j("#ex_detailsCheck:checkbox:checked").length > 0) {
        json.exportParams.patientSheet = true;

        //patientSheetElements
        json.exportParams.patientSheetElements = new Object();
        if ($j("#ex_bdc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.birthDate = true;
        } else {
            json.exportParams.patientSheetElements.birthDate = false;
        }
        if ($j("#ex_ddc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.deathDate = true;
        } else {
            json.exportParams.patientSheetElements.deathDate = false;
        }
        if ($j("#ex_lc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.language = true;
        } else {
            json.exportParams.patientSheetElements.language = false;
        }
        if ($j("#ex_zcc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.zipCode = true;
        } else {
            json.exportParams.patientSheetElements.zipCode = false;
        }
        if ($j("#ex_msc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.maritalStatus = true;
        } else {
            json.exportParams.patientSheetElements.maritalStatus = false;
        }
        if ($j("#ex_sc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.sex = true;
        } else {
            json.exportParams.patientSheetElements.sex = false;
        }
        if ($j("#ex_rc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.race = true;
        } else {
            json.exportParams.patientSheetElements.race = false;
        }
        if ($j("#ex_vsc:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.vitalStatus = true;
        } else {
            json.exportParams.patientSheetElements.vitalStatus = false;
        }
        if ($j("#ex_ic:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.income = true;
        } else {
            json.exportParams.patientSheetElements.income = false;
        }
        if ($j("#ex_rec:checkbox:checked").length > 0) {
            json.exportParams.patientSheetElements.religion = true;
        } else {
            json.exportParams.patientSheetElements.religion = false;
        }
    } else {
        json.exportParams.patientSheet = false;
    }
    //concepts
    json.concepts = new Array();
    for (var i = 0; i < i2b2.Export.Workspace.concept_SDX.length; i++) {
        var cSDX = i2b2.Export.Workspace.concept_SDX[i];
        var ioCon = new Object();
        if (cSDX.origData.isModifier) {
            //modifier
            //itemKey
            ioCon.itemKey = cSDX.origData.parent.key;
            //hlevel
            ioCon.hlevel = cSDX.origData.parent.level;
            //type
            if (cSDX.origData.parent.hasChildren.indexOf("L") > -1) {
                ioCon.type = "L";
            } else {
                ioCon.type = "F";
            }
            //name
            ioCon.name = cSDX.origData.parent.name;
            //tableName
            ioCon.tableName = cSDX.origData.parent.table_name;
            //columnName
            ioCon.columnName = cSDX.origData.parent.column_name;
            //dimCode
            ioCon.dimCode = cSDX.origData.parent.dim_code;
            ioCon.modifier = new Object();
            //modifierKey
            ioCon.modifier.modifierKey = cSDX.origData.key;
            //name
            ioCon.modifier.name = cSDX.origData.name;
            //basecode
            ioCon.modifier.basecode = cSDX.origData.basecode;
            //dimCode
            ioCon.modifier.dimCode = cSDX.origData.dim_code;
            //tableName
            ioCon.modifier.tableName = cSDX.origData.table_name;
            //columnName
            ioCon.modifier.columnName = cSDX.origData.column_name;
        } else {
            //concept
            //itemKey
            ioCon.itemKey = cSDX.origData.key;
            //hlevel
            ioCon.hlevel = cSDX.origData.level;
            //type
            if (cSDX.origData.hasChildren.indexOf("L") > -1) {
                ioCon.type = "L";
            } else {
                ioCon.type = "F";
            }
            //name
            ioCon.name = cSDX.origData.name;
            //tableName
            ioCon.tableName = cSDX.origData.table_name;
            //columnName
            ioCon.columnName = cSDX.origData.column_name;
            //dimCode
            ioCon.dimCode = cSDX.origData.dim_code;
        }
        json.concepts.push(ioCon);
    }

    //send export request (empty callback)
    var scb_export = new i2b2_scopedCallback();
    scb_export.scope = this;
    scb_export.callback = exportCallback;

    var reqStr = JSON.stringify(json);
    i2b2.EXPORTCELL.ajax.export("Plugin:Export", {
        request_string : reqStr
    }, scb_export);
}

function runExportList() {
    if (!i2b2.Export.Workspace.exportListBlock) {
        i2b2.Export.Workspace.exportListBlock = true;

        var scb_exportList = new i2b2_scopedCallback();
        scb_exportList.scope = this;
        scb_exportList.callback = exportListCallback;

        var password = i2b2.h.getPass();
        var passwdReplaced = password.replace(/</g, "\&lt;");
        passwdReplaced = passwdReplaced.replace(/>/g, "\&gt;");
        var reqStr = JSON.stringify({
            "sessionId" : i2b2.Export.Workspace.sessionId,
            "newPassword" : passwdReplaced
        });

        i2b2.EXPORTCELL.ajax.exportList("Plugin:Export", {
            request_string : reqStr
        }, scb_exportList);
    }
}

//Callback ExportList
function exportListCallback(data) {
    var json = jsonFromResponse(data);

    for (var i = 0; i < json.exports.length; i++) {
        var ex = json.exports[i];

        var ex_index = i2b2.Export.Workspace.exportIds.indexOf(ex.id);

        if (ex_index < 0) {
            ex.drawn = false;
            ex.hidden = false;
            ex.date = new Date();
            i2b2.Export.Workspace.exportIds.push(ex.id);
            i2b2.Export.Workspace.exports.push(ex);
        } else {
            //update status, file name, error and problems
            i2b2.Export.Workspace.exports[ex_index].status = ex.status;
            if ( typeof ex.exceptionMessage != "undefined") {
                i2b2.Export.Workspace.exports[ex_index].exceptionMessage = ex.exceptionMessage;
            }
            if ( typeof ex.fileName != "undefined") {
                i2b2.Export.Workspace.exports[ex_index].fileName = ex.fileName;
            }
            if ( typeof ex.problems != "undefined") {
                i2b2.Export.Workspace.exports[ex_index].problems = ex.problems;
            }
        }
    }

    manageDownloads();

    i2b2.Export.Workspace.exportListBlock = false;
}

//Callback Export
function exportCallback(data) {
    //clear forms
    //clearAll();

    //increase export_prog and update name placeholder
    i2b2.Export.Workspace.export_prog++;
    var nameInput = $j("#ex_nameInput");
    var dStamp = dateStamp();
    nameInput.attr("placeholder", "Export_" + dStamp + "_" + i2b2.Export.Workspace.export_prog);
    nameInput.val("");
    manageInputColor('ex_nameInput', 'ex_nameLab');

    //change tab
    tabChange(1);

    //send export list request
    runExportList();
}

//Callback TestSession
function testSessionCallback(data) {
    var json = jsonFromResponse(data);
    if (json.valid) {
        //update GUI
        restoreConceptsAndPSet();
        restoreDownloads();
        tabChange(i2b2.Export.Workspace.activeTab);
    } else {
        //alert
        alert("Your session has expired. Session max inactive time is " + json.lifeSpan + "minutes.");

        //new workspace and session
        createWorkspace();
    }
}

//Callback OpenNewSession
function openNewSessionCallback(data) {
    var json = jsonFromResponse(data);
    i2b2.Export.Workspace.sessionId = json.session.id;
}

function jsonFromResponse(data) {
    //parse della risposta
    // check for errors
    if (data.error) {
        alert('The results from the server could not be understood. Wrong Data Input. Press F12 for more information.');
        console.error("Bad Results from Cell Communicator: ", results);
    }
    var text = data.msgResponse;
    var startTag = '<observation_blob>';
    var startIndex = text.indexOf(startTag) + startTag.length;
    var endTag = '</observation_blob>';
    var endIndex = text.indexOf(endTag);
    var jsonString = text.substring(startIndex, endIndex);
    var json = JSON.parse(jsonString);

    return json;
}

