/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import com.ibm.tdi.eclipse.Messages;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator {

	public final static String TDI_MARKER_TYPE_ATTRIBUTE = "tdi.problem.type"; //$NON-NLS-1$
	
	public final static String TDI_MARKER_MISSING_PROPERTY = "tdi.missing.property"; //$NON-NLS-1$
	
	public IMarkerResolution[] getResolutions(IMarker marker) {
		try {
			if(TDI_MARKER_MISSING_PROPERTY.equals(marker.getAttribute(TDI_MARKER_TYPE_ATTRIBUTE)))
				return new IMarkerResolution[]{new AddPropertyGenerator(marker)};
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static class AddPropertyGenerator implements IMarkerResolution {
		private IMarker marker;

		public AddPropertyGenerator(IMarker marker) {
			this.marker = marker;
			
		}

		public String getLabel() {
			try {
				return Messages.getString("outline.label.0") + ": " + marker.getAttribute(TDI_MARKER_MISSING_PROPERTY);
			} catch (Exception e) {
				return e.toString();
			}
		}

		public void run(IMarker marker) {
		}
		
	}
}
