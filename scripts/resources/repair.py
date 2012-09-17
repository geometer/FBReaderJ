#!/usr/bin/python

import sys
from xml.dom import minidom

def processNodes(base, custom):
	customChild = custom.firstChild
	for baseChild in base.childNodes:
		if baseChild.nodeType != minidom.Node.ELEMENT_NODE:
			continue
		while customChild is not None and customChild.nodeType != minidom.Node.ELEMENT_NODE:
			customChild = customChild.nextSibling
		if baseChild.nodeName == 'node':
			name = baseChild.getAttribute('name')
			if name == '':
				continue
			if customChild is None or name != customChild.getAttribute('name'):
				newNode = custom.ownerDocument.createElement('node')
				newNode.setAttribute('name', name);
				value = baseChild.getAttribute('value')
				if value != '':
					newNode.setAttribute('value', value);
					newNode.setAttribute('toBeTranslated', 'true');
				customChild = custom.insertBefore(newNode, customChild)
		processNodes(baseChild, customChild)
		customChild = customChild.nextSibling

if len(sys.argv) != 3:
	print 'Usage: %s <en.xml> <custom.xml>' % sys.argv[0]
	exit(1)

base_model = minidom.parse(sys.argv[1])
custom_model = minidom.parse(sys.argv[2])

processNodes(base_model, custom_model)
print custom_model.toxml('UTF-8')
