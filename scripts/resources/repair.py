#!/usr/bin/python

import sys
from xml.dom import minidom

def getFirstElementFrom(node):
	while node is not None and node.nodeType != minidom.Node.ELEMENT_NODE:
		node = node.nextSibling
	return node

def processNodes(base, custom):
	baseChild = getFirstElementFrom(base.firstChild)
	customChild = getFirstElementFrom(custom.firstChild)
	while baseChild is not None:
		if baseChild.nodeName == 'node':
			name = baseChild.getAttribute('name')
			if name != '' and (customChild is None or name != customChild.getAttribute('name')):
				newNode = custom.ownerDocument.createElement('node')
				newNode.setAttribute('name', name)
				value = baseChild.getAttribute('value')
				if value != '':
					newNode.setAttribute('value', value)
					newNode.setAttribute('toBeTranslated', 'true')
				customChild = custom.insertBefore(newNode, customChild)
		if customChild is not None:
			processNodes(baseChild, customChild)
			customChild = getFirstElementFrom(customChild.nextSibling)
		baseChild = getFirstElementFrom(baseChild.nextSibling)

if len(sys.argv) != 3:
	print 'Usage: %s <en.xml> <custom.xml>' % sys.argv[0]
	exit(1)

baseModel = minidom.parse(sys.argv[1])
customModel = minidom.parse(sys.argv[2])

processNodes(baseModel, customModel)
print customModel.toxml('UTF-8')
