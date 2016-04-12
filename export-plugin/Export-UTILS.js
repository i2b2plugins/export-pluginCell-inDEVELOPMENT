/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */

//Clone js object
function cl0ne(obj) {
    if (null == obj || "object" != typeof obj)
        return obj;
    // var copy = obj.constructor();
    var copy = new Object();
    for (var attr in obj) {
        if (obj.hasOwnProperty(attr))
            copy[attr] = obj[attr];
    }
    return copy;
}

//Date stamp
function dateStamp() {
    var date = new Date();
    var month = date.getMonth() + 1;
    if (month < 10) {
        month = "0" + month;
    }
    var day = date.getDate();
    if (day < 10) {
        day = "0" + day;
    }
    var out = "" + date.getFullYear() + month + day;
    return out;
}

//Date format
function dateFormat(date) {
    var month = date.month + 1;
    if (month < 10) {
        month = "0" + month;
    }
    var day = date.dayOfMonth;
    if (day < 10) {
        day = "0" + day;
    }
    var out = month + "/" + day + "/" + date.year;
    return out;
}