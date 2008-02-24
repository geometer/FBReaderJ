#!/usr/bin/python

import os;
import shutil;

raw_res_dir = "res/raw"
xml_res_dir = "res/xml"
data_dir_common = "../../data"
data_dir_android = "data"

print os.getcwd()

def clean_res_dir(dir):
	if os.path.exists(dir):
		for file in os.listdir(dir):
			os.remove(dir + os.sep + file)
		os.rmdir(dir)
	os.mkdir(dir)

def process_data_dir(prefix, dir):
	for file in os.listdir(dir):
		full_file_name = dir + os.sep + file
		if os.path.isfile(full_file_name):
			#if (file.endswith(".xml")):
			#	shutil.copyfile(full_file_name, xml_res_dir + os.sep + prefix + file.lower())
			shutil.copyfile(full_file_name, (raw_res_dir + os.sep + prefix + file).lower().replace('.', '_').replace('-', '_'))
		elif (file != ".svn"):
			process_data_dir(prefix + file + "__", full_file_name)

clean_res_dir(raw_res_dir)
#clean_res_dir(xml_res_dir)
process_data_dir("data__", data_dir_common)
process_data_dir("data__", data_dir_android)
