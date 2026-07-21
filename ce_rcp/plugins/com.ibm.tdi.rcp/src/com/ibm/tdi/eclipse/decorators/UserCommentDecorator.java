/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.decorators;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Activator;

public class UserCommentDecorator extends PropertyStoreDecorator {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Image decorateImage(Image image, Object element) {
		if(element instanceof BaseConfiguration && image != null) {
			String comment = ((BaseConfiguration) element).getUserComment();
			if(comment != null && comment.length() > 0) {
				return new Overlay(image).createImage();
			}
		}
		return null;
	}

	public String decorateText(String text, Object element) {
		return null;
	}

	private static class Overlay extends CompositeImageDescriptor {

		private Image baseImage;

		public Overlay(Image image) {
			baseImage = image;
		}

		@Override
		protected void drawCompositeImage(int width, int height) {
			drawImage(baseImage.getImageData(), 0, 0);
			Image img = Activator.getImage("UserComment");
			drawImage(img.getImageData(), width - img.getBounds().width, 0);
		}

		@Override
		protected Point getSize() {
			return new Point(baseImage.getBounds().width, baseImage.getBounds().height);
		}
		
	}
}
