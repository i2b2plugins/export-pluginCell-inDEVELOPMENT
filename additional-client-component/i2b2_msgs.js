/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */


// create the communicator Object
i2b2.EXPORTCELL.ajax = i2b2.hive.communicatorFactory("EXPORTCELL");
i2b2.EXPORTCELL.cfg.msgs = {};
i2b2.EXPORTCELL.cfg.parsers = {};

// ================================================================================================== //
i2b2.EXPORTCELL.cfg.msgs.exportCell_Request = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\r'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/"\r'+
'  xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/"\r'+
'  xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/"\r'+
'  xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/"\r'+
'  xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/"\r'+
'  xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\r'+
'  <request_header>\n'+
'      <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'  </request_header>\n'+
'  <message_body>\n'+
'  <ns2:patient_data>\n'+
'      <ns2:observation_set>\n'+
'      <observation>\n'+
'          <observation_blob>{{{request_string}}}</observation_blob>\n'+
'      </observation>\n'+
'      </ns2:observation_set>\n'+
'  </ns2:patient_data>\n'+
'  </message_body>\n'+
'  {{{proxy_info}}}\n'+
'</ns6:request>';

i2b2.EXPORTCELL.cfg.parsers.exportCell_Request = function() {
    if (!this.error) {
        this.model = [];
    } else {
        this.model = false;
        console.error("[readApprovedEntries] Could not parse() data!");
    }
    return this;
}

i2b2.EXPORTCELL.ajax._addFunctionCall("testSession",
                                i2b2.EXPORTCELL.cfg.cellURL + 'testSession',
                                i2b2.EXPORTCELL.cfg.msgs.exportCell_Request,
                                ["request_string"],
                                i2b2.EXPORTCELL.cfg.parsers.exportCell_Request);
                                
i2b2.EXPORTCELL.ajax._addFunctionCall("openNewSession",
                                i2b2.EXPORTCELL.cfg.cellURL + 'openNewSession',
                                i2b2.EXPORTCELL.cfg.msgs.exportCell_Request,
                                ["request_string"],
                                i2b2.EXPORTCELL.cfg.parsers.exportCell_Request);
                                
i2b2.EXPORTCELL.ajax._addFunctionCall("export",
                                i2b2.EXPORTCELL.cfg.cellURL + 'export',
                                i2b2.EXPORTCELL.cfg.msgs.exportCell_Request,
                                ["request_string"],
                                i2b2.EXPORTCELL.cfg.parsers.exportCell_Request);
                                
i2b2.EXPORTCELL.ajax._addFunctionCall("exportList",
                                i2b2.EXPORTCELL.cfg.cellURL + 'exportList',
                                i2b2.EXPORTCELL.cfg.msgs.exportCell_Request,
                                ["request_string"],
                                i2b2.EXPORTCELL.cfg.parsers.exportCell_Request);

