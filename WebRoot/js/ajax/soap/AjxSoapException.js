/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at: http://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15 
 * have been added to cover use of software over a computer network and provide for limited attribution 
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B. 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * See the License for the specific language governing rights and limitations under the License. 
 * The Original Code is Zimbra Open Source Web Client. 
 * The Initial Developer of the Original Code is Zimbra, Inc. 
 * All portions of the code are Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc. All Rights Reserved. 
 * ***** END LICENSE BLOCK *****
 */

/**
 * Creates a SOAP exception.
 * @class
 * 
 * 
 * @param {string} 		[msg]		the human readable message
 * @param {constant}		code		the exception code
 * @param {string} 		[method] 	the name of the method throwing the exception
 * @param {string} 		[detail]		any additional detail
 * 
 * @extends		AjxException
 * 
 * @private
 */
AjxSoapException = function(msg, code, method, detail) {
	AjxException.call(this, msg, code, method, detail);
}

AjxSoapException.prototype.toString = 
function() {
	return "AjxSoapException";
}

AjxSoapException.prototype = new AjxException;
AjxSoapException.prototype.constructor = AjxSoapException;

/**
 * Defines an "internal error" exception.
 */
AjxSoapException.INTERNAL_ERROR 	= "INTERNAL_ERROR";
/**
 * Defines an "invalid PDU" exception.
 */
AjxSoapException.INVALID_PDU 		= "INVALID_PDU";
/**
 * Defines an "element exists" exception.
 */
AjxSoapException.ELEMENT_EXISTS 	= "ELEMENT_EXISTS";
