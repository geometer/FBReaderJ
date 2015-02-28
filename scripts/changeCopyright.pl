#!/usr/bin/perl

use Fcntl;

if ($#ARGV != 0) {
	print "usage:\n  $0 <source_file>\n";
	exit(0);
}

my $source_file = @ARGV[0];

my $min_year = 2043;

my $index = 0;
my $copyright_notice_end_line = 0;

open(SOURCE_FILE, "$source_file") || die "Cannot open file $source_file\n";
while (<SOURCE_FILE>) {
	if ($index == 0) {
		/^\/\*/ || die "File $source_file doesn't contain copyright notice\n";
	} elsif (/\*\// && $copyright_notice_end_line == 0) {
		$min_year < 2043 || die "File $source_file doesn't contain copyright year information\n";
		$copyright_notice_end_line = $index + 1;
	}
	if (/Copyright \(C\)/) {
		my $year = $_;
		$year =~ s/.+Copyright \(C\) ([^ -]+).+\n/\1/; 
		if ($year < $min_year) {
			$min_year = $year;
		}
	}
	++$index;
}
$copyright_notice_end_line > 0 || die "File $source_file doesn't contain copyright notice\n";
$years = ($min_year == 2015) ? 2015 : "$min_year-2015";

open(TMP_FILE, ">TMP");

open(COPYRIGHT_FILE, "./scripts/copyright");
while (<COPYRIGHT_FILE>) {
	s/YEARS/$years/;
	print TMP_FILE $_;
}
close(COPYRIGHT_FILE);

$index = 0;
seek(SOURCE_FILE, 0, SEEK_SET);
while (<SOURCE_FILE>) {
	if (++$index > $copyright_notice_end_line) {
		print TMP_FILE $_;
	}
}
close(SOURCE_FILE);
close(TMP_FILE);

rename("TMP", "$source_file");
