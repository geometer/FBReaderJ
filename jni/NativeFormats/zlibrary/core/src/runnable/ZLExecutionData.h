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

#ifndef __ZLEXECUTIONDATA_H__
#define __ZLEXECUTIONDATA_H__

#include <string>
#include <vector>
#include <set>

#include <shared_ptr.h>

#include <ZLTypeId.h>
#include <ZLUserData.h>

class ZLExecutionData : public ZLUserDataHolder, public ZLObjectWithRTTI {

public:
	typedef std::vector<shared_ptr<ZLExecutionData> > Vector;

public:
	static const ZLTypeId TYPE_ID;

public:
	class Runner {

	protected:
		Runner();
		virtual ~Runner();

	public:
		virtual std::string perform(const Vector &dataVector) const = 0;
		std::string perform(shared_ptr<ZLExecutionData> data) const;
	};

public:
	class Listener {

	protected:
		Listener();

	public:
		virtual ~Listener();

		void cancelProcess();
		virtual void showPercent(int ready, int full) = 0;

	private:
		ZLExecutionData *myProcess;

	friend class ZLExecutionData;
	};

public:
	static std::string perform(shared_ptr<ZLExecutionData> data);
	static std::string perform(const Vector &dataVector);

private:
	static std::set<Runner*> ourRunners;

protected:
	ZLExecutionData();

public:
	virtual ~ZLExecutionData();

	void setListener(shared_ptr<Listener> listener);
	virtual void onCancel();
	void setPercent(int ready, int full);

private:
	shared_ptr<Listener> myListener;
};

#endif /* __ZLEXECUTIONDATA_H__ */
