/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.log;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Level;

import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.icu.text.MessageFormat;

public class ITRegistryJULHandler extends Handler{

	private Log log;
	private RS rs;

	@Override
	public void close() {
		log.close();
	}

	@Override
	public void flush() {
		//do nothing
	}

	@Override
	public void publish(LogRecord record) {
		
		rs = RS.getServer();
		if(rs!=null){
			log = rs.getLog();
		}
		
		if(log!=null){	
			Level level = record.getLevel();
			String msg = record.getMessage();
			Object[] parameters = record.getParameters();
			
			if(parameters !=null && parameters.length!=0){
				msg = MessageFormat.format(msg, parameters);
			}
			
			if(Level.WARNING.equals(level)){
				log.logwarn(msg);
			}else if(Level.INFO.equals(level)){
				log.loginfo(msg);
			}else if(Level.FINE.equals(level) 
						|| Level.FINEST.equals(level)
						|| Level.FINER.equals(level)){
				log.logfine(msg);
			}else if(Level.SEVERE.equals(level)){
				log.logerror(msg);
			} else if(Level.CONFIG.equals(level)){
				log.logdebug(msg);
			}	
		}//if log	
	}
}
