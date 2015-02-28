/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __PDBPLUGIN_H__
#define __PDBPLUGIN_H__

#include <shared_ptr.h>

#include "../FormatPlugin.h"

class PdbPlugin : public FormatPlugin {

public:
	static std::string fileType(const ZLFile &file);

protected:
	PdbPlugin();

public:
	virtual ~PdbPlugin();
};

/*
class PluckerPlugin : public PdbPlugin {

public:
	bool providesMetainfo() const;
	bool acceptsFile(const ZLFile &file) const;
	bool readMetainfo(Book &book) const;
	bool readModel(BookModel &model) const;
};
*/

class SimplePdbPlugin : public PdbPlugin {

public:
	bool readMetainfo(Book &book) const;
	//bool readModel(BookModel &model) const;

protected:
	virtual shared_ptr<ZLInputStream> createStream(const ZLFile &file) const = 0;
	virtual void readDocumentInternal(const ZLFile &file, BookModel &model, const class PlainTextFormat &format, const std::string &encoding, ZLInputStream &stream) const;
};

class PalmDocLikePlugin : public SimplePdbPlugin {

public:
	bool providesMetainfo() const;
	const std::string &tryOpen(const ZLFile &file) const;

protected:
	shared_ptr<ZLInputStream> createStream(const ZLFile &file) const;
};

class PalmDocPlugin : public PalmDocLikePlugin {

public:
	//bool acceptsFile(const ZLFile &file) const;

	void readDocumentInternal(const ZLFile &file, BookModel &model, const class PlainTextFormat &format, const std::string &encoding, ZLInputStream &stream) const;

//private:
	//FormatInfoPage *createInfoPage(ZLOptionsDialog &dialog, const ZLFile &file);
};

class MobipocketPlugin : public PalmDocLikePlugin {

private:
	//bool acceptsFile(const ZLFile &file) const;
	const std::string supportedFileType() const;
	//virtual FormatInfoPage *createInfoPage(ZLOptionsDialog &dialog, const ZLFile &file);

	//virtual const std::string &tryOpen(const ZLFile &file) const;
	std::vector<shared_ptr<FileEncryptionInfo> > readEncryptionInfos(Book &book) const;
	bool readUids(Book &book) const;
	bool readLanguageAndEncoding(Book &book) const;
	bool readMetainfo(Book &book) const;
	bool readModel(BookModel &model) const;

	void readDocumentInternal(const ZLFile &file, BookModel &model, const class PlainTextFormat &format, const std::string &encoding, ZLInputStream &stream) const;
	shared_ptr<const ZLImage> coverImage(const ZLFile &file) const;
	std::string readAnnotation(const ZLFile &file) const;
};

/*
class EReaderPlugin : public SimplePdbPlugin {

public:
	bool providesMetainfo() const;
	bool acceptsFile(const ZLFile &file) const;
	bool readMetainfo(Book &book) const;
	const std::string &tryOpen(const ZLFile &file) const;

	void readDocumentInternal(const ZLFile &file, BookModel &model, const class PlainTextFormat &format, const std::string &encoding, ZLInputStream &stream) const;
protected:
	shared_ptr<ZLInputStream> createStream(const ZLFile &file) const;
};

class ZTXTPlugin : public SimplePdbPlugin {

public:
	bool providesMetainfo() const;
	bool acceptsFile(const ZLFile &file) const;

protected:
	shared_ptr<ZLInputStream> createStream(const ZLFile &file) const;

private:
	FormatInfoPage *createInfoPage(ZLOptionsDialog &dialog, const ZLFile &file);
};
*/

inline PdbPlugin::PdbPlugin() {}

#endif /* __PDBPLUGIN_H__ */
