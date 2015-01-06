#noinst_PROGRAMS = filter_dup
include_HEADERS = linebreak.h linebreakdef.h
lib_LTLIBRARIES = liblinebreak.la

liblinebreak_la_LDFLAGS = -no-undefined -version-info 2:0
liblinebreak_la_SOURCES = \
	linebreak.c \
	linebreakdata.c \
	linebreakdef.c

EXTRA_DIST = \
	LineBreak1.sed \
	LineBreak2.sed \
	linebreakdata1.tmpl \
	linebreakdata2.tmpl \
	linebreakdata3.tmpl \
	LICENCE \
	Doxyfile \
	Makefile.gcc \
	Makefile.msvc \
	doc

distclean-local:
	rm -f LineBreak.txt filter_dup${EXEEXT}

doc:
	cd ${top_srcdir} && doxygen

LineBreak.txt:
	wget http://unicode.org/Public/UNIDATA/LineBreak.txt

linebreakdata: ${builddir}/filter_dup LineBreak.txt
	sed -n -f ${top_srcdir}/LineBreak1.sed LineBreak.txt > tmp.txt
	sed -f ${top_srcdir}/LineBreak2.sed tmp.txt | ${builddir}/filter_dup > tmp.c
	head -2 LineBreak.txt > tmp.txt
	cat ${top_srcdir}/linebreakdata1.tmpl tmp.txt ${top_srcdir}/linebreakdata2.tmpl tmp.c ${top_srcdir}/linebreakdata3.tmpl > ${top_srcdir}/linebreakdata.c
	rm tmp.txt tmp.c
