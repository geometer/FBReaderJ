package org.fbreader.bookmodel;

public enum FBTextKind {
	REGULAR(0),
	TITLE(1),
	SECTION_TITLE(2),
	POEM_TITLE(3),
	SUBTITLE(4),
	ANNOTATION(5),
	EPIGRAPH(6),
	STANZA(7),
	VERSE(8),
	PREFORMATTED(9),
	IMAGE(10),
	END_OF_SECTION(11),
	CITE(12),
	AUTHOR(13),
	DATE(14),
	INTERNAL_HYPERLINK(15),
	FOOTNOTE(16),
	EMPHASIS(17),
	STRONG(18),
	SUB(19),
	SUP(20),
	CODE(21),
	STRIKETHROUGH(22),
	CONTENTS_TABLE_ENTRY(23),
	LIBRARY_AUTHOR_ENTRY(24),
	LIBRARY_BOOK_ENTRY(25),
	RECENT_BOOK_LIST(26),
	ITALIC(27),
	BOLD(28),
	DEFINITION(29),
	DEFINITION_DESCRIPTION(30),
	H1(31),
	H2(32),
	H3(33),
	H4(34),
	H5(35),
	H6(36),
	EXTERNAL_HYPERLINK(37);

	// We use Index to be sure
	// these numbers are compatible
	// with the C++ version
	public final int Index;

	FBTextKind(int index) {
		Index = index;
	}
};
