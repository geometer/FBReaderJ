/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

#include <ZLExecutionData.h>

const ZLTypeId ZLExecutionData::TYPE_ID(ZLObjectWithRTTI::TYPE_ID);

std::set<ZLExecutionData::Runner*> ZLExecutionData::ourRunners;

std::string ZLExecutionData::perform(shared_ptr<ZLExecutionData> data) {
	Vector dataVector;
	dataVector.push_back(data);
	return perform(dataVector);
}

std::string ZLExecutionData::perform(const Vector &dataVector) {
	std::string result;
	for (std::set<Runner*>::const_iterator it = ourRunners.begin(); it != ourRunners.end(); ++it) {
		std::string part = (*it)->perform(dataVector);
		if (!part.empty()) {
			if (!result.empty()) {
				result += '\n';
			}
			result += part;
		}
	}
	return result;
}

ZLExecutionData::ZLExecutionData() {
}

ZLExecutionData::~ZLExecutionData() {
}

void ZLExecutionData::setListener(shared_ptr<Listener> listener) {
	if (!myListener.isNull()) {
		myListener->myProcess = 0;
	}
	myListener = listener;
	if (!myListener.isNull()) {
		myListener->myProcess = this;
	}
}

void ZLExecutionData::setPercent(int ready, int full) {
	if (!myListener.isNull()) {
		myListener->showPercent(ready, full);
	}
}

void ZLExecutionData::onCancel() {
}

ZLExecutionData::Runner::Runner() {
	ourRunners.insert(this);
}

ZLExecutionData::Runner::~Runner() {
	ourRunners.erase(this);
}

std::string ZLExecutionData::Runner::perform(shared_ptr<ZLExecutionData> data) const {
	Vector dataVector;
	dataVector.push_back(data);
	return perform(dataVector);
}

ZLExecutionData::Listener::Listener() {
}

ZLExecutionData::Listener::~Listener() {
}

void ZLExecutionData::Listener::cancelProcess() {
	if (myProcess != 0) {
		myProcess->onCancel();
	}
}
