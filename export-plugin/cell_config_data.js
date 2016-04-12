/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */

// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
    files:[
        "Export_ctrlr.js",
        "Export.js",
        "Export-IO.js",
        "Export-UTILS.js",
        "assets/json2.js",
        "assets/jMetro/js/jquery-ui-1.9.2.min.js"
    ],
    css:[
        "w3-mod.css"
    ],
    config: {
        // additional configuration variables that are set by the system
        short_name: "Export",
        name: "Export",
        description: "Export",
        category: ["plugin"],
        plugin: {
            isolateHtml: false,  // this means do not use an IFRAME
            isolateComm: false,  // this means to expect the plugin to use AJAX communications provided by the framework
            standardTabs: true, // this means the plugin uses standard tabs at top
            html: {
                source: 'injected_screens.html',
                mainDivId: 'Export-mainDiv'
            }
        },
        icons: {
            size32x32: "Export_icon_32x32.png"
        }
    }
}
