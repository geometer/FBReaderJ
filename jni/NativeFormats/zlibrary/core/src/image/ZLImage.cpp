/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

#include <vector>

#include <ZLExecutionData.h>

#include "ZLImage.h"

#include "ZLImageManager.h"

shared_ptr<ZLExecutionData> ZLImage::synchronizationData() const {
	return 0;
}

bool ZLSingleImage::good() const {
	return !ZLImageManager::Instance().imageData(*this).isNull();
}

bool ZLMultiImage::good() const {
	unsigned int rows = this->rows();
	unsigned int columns = this->columns();
	if (rows == 0 || columns == 0) {
		return false;
	}
	std::vector<int> widths;
	widths.reserve(columns);
	std::vector<int> heights;
	heights.reserve(rows);
	for (unsigned int i = 0; i < rows; ++i) {
		for (unsigned int j = 0; j < columns; ++j) {
			shared_ptr<const ZLImage> subImage = this->subImage(i, j);
			if (subImage.isNull()) {
				return false;
			}
			shared_ptr<ZLImageData> data = ZLImageManager::Instance().imageData(*subImage);
			if (data.isNull()) {
				return false;
			}
			int w = data->width();
			if (i == 0) {
				widths.push_back(w);
			} else if (w != widths[j]) {
				return false;
			}
			int h = data->height();
			if (j == 0) {
				heights.push_back(h);
			} else if (h != heights[i]) {
				return false;
			}
		}
	}
	return true;
}
