/*
 * Copyright (c) 2015 Biomeris s.r.l.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v2.1
 * which accompanies this distribution.
 *
 * Contributors:
 *     Matteo Gabetta
 */

function main() {
    // avoid use of prototype JSON function
    if (window.Prototype) {
        delete Object.prototype.toJSON;
        delete Array.prototype.toJSON;
        delete Hash.prototype.toJSON;
        delete String.prototype.toJSON;
    }

    //create workspace
    manageWorkspace();

    //Date pickers
    var curYear = new Date().getFullYear();
    var maxYear = curYear + 2;
    var range = "1990:" + maxYear;
    $j("#ex_afterInput").datepicker({
        changeMonth : true,
        changeYear : true,
        yearRange : range
    });
    $j("#ex_beforeInput").datepicker({
        changeMonth : true,
        changeYear : true,
        yearRange : range
    });

    //Name input
    var nameInput = $j("#ex_nameInput");
    var dStamp = dateStamp();
    nameInput.attr("placeholder", "Export_" + dStamp + "_" + i2b2.Export.Workspace.export_prog);

    //Patient set drop area
    delete i2b2.sdx.Master._sysData["ex_patientDropDiv"];
    var op_trgt = {
        dropTarget : true
    };
    i2b2.sdx.Master.AttachType("ex_patientDropDiv", "PRS", op_trgt);
    i2b2.sdx.Master.setHandlerCustom("ex_patientDropDiv", "PRS", "DropHandler", dropPSet);

    //Concept drop area
    delete i2b2.sdx.Master._sysData["ex_conceptDropDiv"];
    var op_trgt = {
        dropTarget : true
    };
    i2b2.sdx.Master.AttachType("ex_conceptDropDiv", "CONCPT", op_trgt);
    i2b2.sdx.Master.setHandlerCustom("ex_conceptDropDiv", "CONCPT", "DropHandler", dropConcept);

    //Call exportList peroiodically
    setInterval(runExportListPeriod, 3000);
}

function runExportListPeriod() {
    if (i2b2.Export.Workspace.activeTab == 1 && i2b2.Export.Workspace.anyWorking && !i2b2.Export.Workspace.exportListBlock) {
        runExportList();
    }
}

//Manage workspace
function manageWorkspace() {
    if ( typeof i2b2.Export.Workspace == "undefined") {
        createWorkspace();
    } else {
        //Test session id
        var scb_testSession = new i2b2_scopedCallback();
        scb_testSession.scope = this;
        scb_testSession.callback = testSessionCallback;

        var reqStr = JSON.stringify({
            "sessionId" : i2b2.Export.Workspace.sessionId
        });

        i2b2.EXPORTCELL.ajax.testSession("Plugin:Export", {
            request_string : reqStr
        }, scb_testSession);
    }
}

//Create new workspace
function createWorkspace() {
    i2b2.Export.Workspace = new Object();
    i2b2.Export.Workspace.pSet_SDX = null;
    i2b2.Export.Workspace.concept_SDX = null;
    i2b2.Export.Workspace.concept_prog = 0;
    i2b2.Export.Workspace.export_prog = 1;
    i2b2.Export.Workspace.exports = new Array();
    i2b2.Export.Workspace.exportIds = new Array();
    i2b2.Export.Workspace.activeTab = 0;
    i2b2.Export.Workspace.anyWorking = false;
    i2b2.Export.Workspace.exportListBlock = false;

    //Get session id
    var scb_openNewSession = new i2b2_scopedCallback();
    scb_openNewSession.scope = this;
    scb_openNewSession.callback = openNewSessionCallback;

    buildNetwork();
    var reqStr = JSON.stringify({
        "network" : i2b2.Export.Workspace.network
    });

    i2b2.EXPORTCELL.ajax.openNewSession("Plugin:Export", {
        request_string : reqStr
    }, scb_openNewSession);

    //Get download EP
    if ( typeof i2b2.EXPORTCELL.cfg.cellParams.DownloadEP != "undefined") {
        i2b2.Export.Workspace.downloadEP = i2b2.EXPORTCELL.cfg.cellParams.DownloadEP;
    } else {
        //TODO alert
    }
}

//Manage downloads
function manageDownloads() {
    //drawn:true -> update
    //drawn:false -> draw

    i2b2.Export.Workspace.anyWorking = false;

    for (var i = 0; i < i2b2.Export.Workspace.exports.length; i++) {
        var ex = i2b2.Export.Workspace.exports[i];

        var rowId = "ex_down_" + i;

        if (!ex.hidden) {
            if (ex.drawn) {
                //get row
                var row = $j("#" + rowId);

                //manage status
                manageStatus(row, ex);
            } else {
                var sepId = "ex_sep_" + i;
                var dContainer = $j("#ex_downloadCont");
                var rowModel = $j("#ex_downloadProto");
                var sepModel = $j("#ex_sepProto");
                var cloneRow = rowModel.clone(true);
                cloneRow.attr("id", rowId);
                var cloneSep = sepModel.clone(false);
                cloneSep.attr("id", sepId);

                //name
                cloneRow.find(".ex_dName").text(ex.name);
                //date
                var mins = ex.date.getMinutes();
                if (mins < 9) {
                    mins = "0" + mins;
                }
                var secs = ex.date.getSeconds();
                if (secs < 9) {
                    secs = "0" + secs;
                }
                var dateStr = ex.date.toDateString() + " " + ex.date.getHours() + ":" + mins + ":" + secs;
                cloneRow.find(".ex_dDate").text(dateStr);

                //info
                var infoButton = cloneRow.find(".ex_dInfo");
                var infoId = "ex_downinfo_" + i;
                infoButton.attr("id", infoId);
                var detDiv = cloneRow.find(".ex_dDetails");
                var detId = "ex_downDetails_" + i;
                detDiv.attr("id", detId);
                infoButton.attr("onclick", "showInfo('" + ex.id + "','" + detId + "','" + infoId + "');");
                infoButton.attr("onmouseover", "colorInfo('" + infoId + "',true);");
                infoButton.attr("onmouseout", "colorInfo('" + infoId + "',false);");

                //delete(hide)
                var delButton = cloneRow.find(".ex_dDel");
                var delId = "ex_downDel_" + i;
                delButton.attr("id", delId);
                delButton.attr("onclick", "hideDownload('" + ex.id + "','" + rowId + "','" + sepId + "');");
                delButton.attr("onmouseover", "colorDelete('" + delId + "',true);");
                delButton.attr("onmouseout", "colorDelete('" + delId + "',false);");

                //details
                //patient set
                cloneRow.find(".ex_det_pset").text(ex.patientSetName);
                //concepts
                var conceptContainer = cloneRow.find(".ex_det_conceptContainer");
                for (var j = 0; j < ex.concepts.length; j++) {
                    var concept = ex.concepts[j];
                    var conceptName = concept.name;
                    if ( typeof concept.modifier != "undefined") {
                        conceptName = conceptName + " (" + concept.modifier.name + ")";
                    }
                    if (j == 0) {
                        cloneRow.find(".ex_det_con1").text(conceptName);
                    } else {
                        var conRow = cloneRow.find(".ex_det_con2row");
                        var conRow_clone = conRow.clone(true);
                        conRow_clone.removeClass("ex_det_con2row");
                        conRow_clone.find(".ex_det_con2").text(conceptName);
                        conRow_clone.show();
                        conceptContainer.append(conRow_clone);
                    }
                }
                //type
                if (ex.exportParams.exportType === "OBS") {
                    cloneRow.find(".ex_det_type").text("Observations");
                }
                if (ex.exportParams.exportType === "PAT") {
                    cloneRow.find(".ex_det_type").text("Patient aggregate");
                }
                //dates
                if ( typeof ex.exportParams.startDate == "undefined") {
                    cloneRow.find(".ex_det_startLab").hide();
                    cloneRow.find(".ex_det_startVal").hide();
                } else {
                    cloneRow.find(".ex_det_startVal").text(dateFormat(ex.exportParams.startDate));
                }
                if ( typeof ex.exportParams.endDate == "undefined") {
                    cloneRow.find(".ex_det_endLab").hide();
                    cloneRow.find(".ex_det_endVal").hide();
                } else {
                    cloneRow.find(".ex_det_endVal").text(dateFormat(ex.exportParams.endDate));
                }
                if ( typeof ex.exportParams.endDate == "undefined" && typeof ex.exportParams.startDate == "undefined") {
                    cloneRow.find(".ex_det_dates").hide();
                }
                //mask ids
                if (ex.exportParams.maskPatientIds) {
                    cloneRow.find(".ex_det_mask").text("YES");
                } else {
                    cloneRow.find(".ex_det_mask").text("NO");
                }
                //patient sheet
                if (ex.exportParams.patientSheet) {
                    cloneRow.find(".ex_det_ps").text("YES");

                    var pInfoStr = "(";
                    if (ex.exportParams.patientSheetElements.birthDate) {
                        pInfoStr += "Birth date, ";
                    }
                    if (ex.exportParams.patientSheetElements.deathDate) {
                        pInfoStr += "Death date, ";
                    }
                    if (ex.exportParams.patientSheetElements.sex) {
                        pInfoStr += "Sex, ";
                    }
                    if (ex.exportParams.patientSheetElements.race) {
                        pInfoStr += "Race, ";
                    }
                    if (ex.exportParams.patientSheetElements.vitalStatus) {
                        pInfoStr += "Vital status, ";
                    }
                    if (ex.exportParams.patientSheetElements.language) {
                        pInfoStr += "Language, ";
                    }
                    if (ex.exportParams.patientSheetElements.zipCode) {
                        pInfoStr += "Zip code, ";
                    }
                    if (ex.exportParams.patientSheetElements.income) {
                        pInfoStr += "Income, ";
                    }
                    if (ex.exportParams.patientSheetElements.religion) {
                        pInfoStr += "Religion, ";
                    }
                    if (ex.exportParams.patientSheetElements.maritalStatus) {
                        pInfoStr += "Marital status, ";
                    }
                    if (pInfoStr.length > 1) {
                        pInfoStr = pInfoStr.substring(0, pInfoStr.length - 2);
                    }
                    pInfoStr += ")";
                    cloneRow.find(".ex_det_pInfo").text(pInfoStr);

                } else {
                    cloneRow.find(".ex_det_ps").text("NO");
                    cloneRow.find(".ex_det_pInifoDiv").hide();
                }
                
                cloneRow.show();
                cloneSep.show();
                dContainer.append(cloneSep);
                dContainer.append(cloneRow);
                ex.drawn = true;

                //manage status
                manageStatus(cloneRow, ex);
            }
        }

    }
}

function showInfo(exId, detailsId, infoButtId) {
    var exIndex = i2b2.Export.Workspace.exportIds.indexOf(exId);
    var ex = i2b2.Export.Workspace.exports[exIndex];

    if ( typeof ex.details == "undefined") {
        ex.details = false;
    }

    var but = $j("#" + infoButtId);
    if (ex.details) {
        but.attr("onmouseout", "colorInfo('" + infoButtId + "',false);");
        $j("#" + detailsId).hide();
        ex.details = false;
    } else {
        but.attr("src", "js-i2b2/cells/plugins/community/Export/assets/images/info.png");
        but.attr("onmouseout", "");
        $j("#" + detailsId).show();
        ex.details = true;
    }

}

function hideDownload(exId, cloneRowId, cloneSepId) {
    var exIndex = i2b2.Export.Workspace.exportIds.indexOf(exId);
    var ex = i2b2.Export.Workspace.exports[exIndex];
    ex.hidden = true;

    $j("#" + cloneRowId).hide();
    $j("#" + cloneSepId).hide();
}

function manageStatus(cloneRow, ex) {
    if (ex.status === "WORKING") {

        i2b2.Export.Workspace.anyWorking = true;

        cloneRow.find(".ex_dAlert").hide();
        cloneRow.find(".ex_dDown").hide();
        cloneRow.find(".ex_dWait").show();

        //details
        cloneRow.find(".ex_det_status").text("RUNNING");
        cloneRow.find(".ex_det_status").removeClass("w3-text-green");
        cloneRow.find(".ex_det_status").removeClass("w3-text-red");
        cloneRow.find(".ex_det_status").addClass("w3-text-yellow");
    } else if (ex.status === "DONE") {
        cloneRow.find(".ex_dAlert").hide();
        cloneRow.find(".ex_dDown").show();
        cloneRow.find(".ex_dWait").hide();

        //details
        cloneRow.find(".ex_det_status").text("DONE");
        cloneRow.find(".ex_det_status").removeClass("w3-text-yellow");
        cloneRow.find(".ex_det_status").removeClass("w3-text-red");
        cloneRow.find(".ex_det_status").addClass("w3-text-green");
        cloneRow.find(".ex_det_file").text(ex.fileName);

        //problems
        if ( typeof ex.problems != "undefined") {
            var proStr = "";
            for (var j = 0; j < ex.problems.length; j++) {
                if (j > 0) {
                    proStr += "  ";
                }
                proStr += ex.problems[j];
            }
            cloneRow.find(".ex_det_problems").text(proStr);
            cloneRow.find(".ex_det_probDiv").show();
            
            cloneRow.find(".ex_dName").addClass("w3-text-red");
        } else {
            cloneRow.find(".ex_det_probDiv").hide();
        }

        //set up download button
        var downAddr = i2b2.Export.Workspace.downloadEP + i2b2.Export.Workspace.sessionId + "/" + ex.id + "/" + ex.fileName;
        cloneRow.find(".ex_dDown").attr("href", downAddr);
    } else if (ex.status === "ERROR") {
        cloneRow.find(".ex_dAlert").show();
        cloneRow.find(".ex_dDown").hide();
        cloneRow.find(".ex_dWait").hide();

        //details
        cloneRow.find(".ex_det_status").text("ERROR");
        cloneRow.find(".ex_det_status").removeClass("w3-text-green");
        cloneRow.find(".ex_det_status").removeClass("w3-text-yellow");
        cloneRow.find(".ex_det_status").addClass("w3-text-red");
        cloneRow.find(".ex_det_errorDiv").show();
        cloneRow.find(".ex_det_errorType").text(ex.exceptionMessage);
    }
}

//Create network object
function buildNetwork() {
    i2b2.Export.Workspace.network = new Object();
    i2b2.Export.Workspace.network.proxyAddress = location.href;
    i2b2.Export.Workspace.network.pmServiceAddress = i2b2.PM.cfg.cellURL;
    i2b2.Export.Workspace.network.username = i2b2.h.getUser();

    var password = i2b2.h.getPass();
    var passwdReplaced = password.replace(/</g, "\&lt;");
    passwdReplaced = passwdReplaced.replace(/>/g, "\&gt;");
    i2b2.Export.Workspace.network.password = passwdReplaced;

    i2b2.Export.Workspace.network.domain = i2b2.h.getDomain();
    i2b2.Export.Workspace.network.project = i2b2.h.getProject();

    if ( typeof i2b2.EXPORTCELL.cfg.cellParams.staticProxyAddress != "undefined") {
        i2b2.Export.Workspace.network.staticProxyAddress = i2b2.EXPORTCELL.cfg.cellParams.staticProxyAddress;
    }
}

//Drop concept
function dropConcept(sdxData, DroppedOnId) {
    addConcept(sdxData[0]);
    fillConceptDropArea();
}

//Add concept
function addConcept(sdxData) {
    if (i2b2.Export.Workspace.concept_SDX == null) {
        i2b2.Export.Workspace.concept_SDX = new Array();
    }
    var divId = "ex_conc_" + i2b2.Export.Workspace.concept_prog;

    var cloneSDX = cl0ne(sdxData);

    cloneSDX.divId = divId;
    cloneSDX.prog = i2b2.Export.Workspace.concept_prog;

    i2b2.Export.Workspace.concept_SDX.push(cloneSDX);

    var conceptContainer = $j("#ex_conceptSet");
    var conceptRowProto = $j("#ex_conc_proto");
    var cloneRow = conceptRowProto.clone(true);
    cloneRow.attr("id", divId);
    cloneRow.show();

    var conceptLabel = cloneRow.find(".ex_conceptname");
    if (cloneSDX.origData.isModifier) {
        var conceptName = cloneSDX.origData.parent.name;
        var modifierName = cloneSDX.origData.name;
        conceptLabel.text(conceptName + " (" + modifierName + ")");
    } else {
        var conceptName = cloneSDX.origData.name;
        conceptLabel.text(conceptName);
    }

    var deleteButton = cloneRow.find(".ex_delconcept");
    var delId = "delConc_" + i2b2.Export.Workspace.concept_prog;
    deleteButton.attr("id", delId);
    deleteButton.on("click", function() {
        removeConcept(divId);
    });
    deleteButton.on("mouseover", function() {
        colorDelete(delId, true);
    });
    deleteButton.on("mouseout", function() {
        colorDelete(delId, false);
    });

    conceptContainer.append(cloneRow);
    i2b2.Export.Workspace.concept_prog++;

    checkready();
}

//Remove concept
function removeConcept(divId) {
    if (i2b2.Export.Workspace.concept_SDX == null) {
        return;
    } else {
        for ( i = 0; i < i2b2.Export.Workspace.concept_SDX.length; i++) {
            if (i2b2.Export.Workspace.concept_SDX[i].divId === divId) {
                i2b2.Export.Workspace.concept_SDX.splice(i, 1);
                var concDiv = $j("#" + divId);
                concDiv.remove();
            }
        }
    }

    if (i2b2.Export.Workspace.concept_SDX.length == 0) {
        i2b2.Export.Workspace.concept_SDX = null;
    }

    fillConceptDropArea();

    checkready();
}

//Restore concepts and patient set
function restoreConceptsAndPSet() {
    //Concepts
    if (i2b2.Export.Workspace.concept_SDX != null) {
        var conceptContainer = $j("#ex_conceptSet");
        var conceptRowProto = $j("#ex_conc_proto");
        for (var i = 0; i < i2b2.Export.Workspace.concept_SDX.length; i++) {
            var sdxElement = i2b2.Export.Workspace.concept_SDX[i];

            var cloneRow = conceptRowProto.clone(true);
            cloneRow.attr("id", sdxElement.divId);
            cloneRow.show();

            var conceptLabel = cloneRow.find(".ex_conceptname");
            if (sdxElement.origData.isModifier) {
                var conceptName = sdxElement.origData.parent.name;
                var modifierName = sdxElement.origData.name;
                conceptLabel.text(conceptName + " (" + modifierName + ")");
            } else {
                var conceptName = sdxElement.origData.name;
                conceptLabel.text(conceptName);
            }

            var deleteButton = cloneRow.find(".ex_delconcept");
            var delId = "delConc_" + sdxElement.prog;
            deleteButton.attr("id", delId);
            deleteButton.attr("onclick", "removeConcept('" + sdxElement.divId + "');");
            deleteButton.attr("onmouseover", "colorDelete('" + delId + "',true);");
            deleteButton.attr("onmouseout", "colorDelete('" + delId + "',false);");

            conceptContainer.append(cloneRow);
        }
        fillConceptDropArea();
    }

    //Patient set
    fillPatDropArea();

    checkready();
}

function restoreDownloads() {
    for (var i = 0; i < i2b2.Export.Workspace.exports.length; i++) {
        var ex = i2b2.Export.Workspace.exports[i];
        ex.drawn = false;
        ex.details = false;
    }
    manageDownloads();
}

//Drop patient set
function dropPSet(sdxData, DroppedOnId) {
    i2b2.Export.Workspace.pSet_SDX = sdxData[0];
    fillPatDropArea();

    checkready();
}

//Fill concept drop area
function fillConceptDropArea() {
    var noConc = $j("#ex_conceptUnset");
    var conc = $j("#ex_conceptSet");
    if (i2b2.Export.Workspace.concept_SDX == null) {
        noConc.show();
        conc.hide();
    } else {
        noConc.hide();
        conc.show();
    }
}

//Fill patient drop area
function fillPatDropArea() {
    var noPatSet = $j("#ex_patSetUnset");
    var patSet = $j("#ex_patSetSet");
    if (i2b2.Export.Workspace.pSet_SDX == null) {
        patSet.hide();
        noPatSet.show();
    } else {
        var pSetName = $j("#ex_psetNameLab");
        var pSetNum = $j("#ex_psetNumLab");
        // pSetName.text(i2b2.Export.Workspace.pSet_SDX.parent.parent.origData.name);
        pSetName.text(i2b2.Export.Workspace.pSet_SDX.origData.title);
        pSetNum.text(i2b2.Export.Workspace.pSet_SDX.origData.size + " patient(s)");

        patSet.show();
        noPatSet.hide();
    }
}

//remove patient set
function delPatset() {
    i2b2.Export.Workspace.pSet_SDX = null;
    fillPatDropArea();

    checkready();
}

//check if ready to start new export
function checkready() {
    var exportButton = $j("#ex_exportButton");
    if (i2b2.Export.Workspace.pSet_SDX != null && i2b2.Export.Workspace.concept_SDX != null) {
        //ready
        exportButton.removeClass("w3-disabled");
    } else {
        //not ready
        exportButton.addClass("w3-disabled");
    }
}

//Tab buttons
function tabChange(tabNum) {
    //0 newExport
    //1 downloads

    var newExport_Div = $j("#ex_newExport_div");
    var download_Div = $j("#ex_download_div");
    var newExport_Tab = $j("#ex_newExport_tab");
    var download_Tab = $j("#ex_download_tab");

    if (tabNum == 0) {
        newExport_Div.show();
        newExport_Tab.removeClass("w3-white");
        newExport_Tab.addClass("w3-blue");
        download_Div.hide();
        download_Tab.removeClass("w3-blue");
        download_Tab.addClass("w3-white");

        i2b2.Export.Workspace.activeTab = 0;
    }
    if (tabNum == 1) {
        newExport_Div.hide();
        newExport_Tab.removeClass("w3-blue");
        newExport_Tab.addClass("w3-white");
        download_Div.show();
        download_Tab.removeClass("w3-white");
        download_Tab.addClass("w3-blue");

        i2b2.Export.Workspace.activeTab = 1;
    }
}

//Change Yes/No label after checkbox
function checkBoxChange(labelId) {
    var cb_label = $j("#" + labelId);
    if (cb_label.text() === "Yes") {
        cb_label.text("No");
    } else {
        cb_label.text("Yes");
    }
}

//Open/close patient details
function switchDetails(divId) {
    var detailDiv = $j("#" + divId);
    if (detailDiv.css("display") === "block") {
        detailDiv.hide();
    } else {
        detailDiv.show();
    }
}

//Color delete button
function colorDelete(imgId, over) {
    var img = $j("#" + imgId);
    if (over) {
        img.attr("src", "js-i2b2/cells/plugins/community/Export/assets/images/delete.png");
    } else {
        img.attr("src", "js-i2b2/cells/plugins/community/Export/assets/images/delete-lock.png");
    }
}

//Color info button
function colorInfo(imgId, over) {
    var img = $j("#" + imgId);
    if (over) {
        img.attr("src", "js-i2b2/cells/plugins/community/Export/assets/images/info.png");
    } else {
        img.attr("src", "js-i2b2/cells/plugins/community/Export/assets/images/info-lock.png");
    }
}

//Show/hide delete button
function showHideDelete(inputId, imgId) {
    var input = $j("#" + inputId);
    var img = $j("#" + imgId);
    if (input.val()) {
        img.show();
    } else {
        img.hide();
    }
}

//Manage name input
function manageInputColor(inputId, labelId) {
    var input = $j("#" + inputId);
    var label = $j("#" + labelId);
    if (input.val()) {
        label.addClass("w3-text-teal");
    } else {
        label.removeClass("w3-text-teal");
    }
}

//Clear input
function clearInput(inputId) {
    $j("#" + inputId).val("");
}

//Clear all new export form
function clearAll() {
    delPatset();

    if (i2b2.Export.Workspace.concept_SDX != null) {
        var howManyConc = i2b2.Export.Workspace.concept_SDX.length;
        for (var i = 0; i < howManyConc; i++) {
            removeConcept(i2b2.Export.Workspace.concept_SDX[0].divId);
        }
    }
    i2b2.Export.Workspace.concept_SDX = null;
    fillConceptDropArea();

    clearInput("ex_nameInput");
    manageInputColor("ex_nameInput", "ex_nameLab");
    $j("#ex_typeRadioObs").prop('checked', true);
    clearInput('ex_afterInput');
    showHideDelete('ex_afterInput', 'ex_delAfterImg');
    manageInputColor('ex_afterInput', 'ex_afterLab');
    clearInput('ex_beforeInput');
    showHideDelete('ex_beforeInput', 'ex_delBeforeImg');
    manageInputColor('ex_beforeInput', 'ex_beforeLab');
    $j("#ex_maskCheck").prop('checked', false);
    $j("#ex_maskCheckLab").text("No");
    $j("#ex_zipCheck").prop('checked', false);
    $j("#ex_zipCheckLab").text("No");
    $j("#ex_detailsCheck").prop('checked', false);
    $j("#ex_detailsCheckLab").text("No");

    $j("#ex_bdc").prop('checked', false);
    $j("#ex_ddc").prop('checked', false);
    $j("#ex_lc").prop('checked', false);
    $j("#ex_zcc").prop('checked', false);
    $j("#ex_msc").prop('checked', false);
    $j("#ex_sc").prop('checked', false);
    $j("#ex_rc").prop('checked', false);
    $j("#ex_vsc").prop('checked', false);
    $j("#ex_ic").prop('checked', false);
    $j("#ex_rec").prop('checked', false);

    $j("#ex_detailsDiv").hide();
}
