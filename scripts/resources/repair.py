#!/usr/bin/python

import sys
from xml.dom import minidom

def first_after(node):
    while node is not None and node.nodeType != minidom.Node.ELEMENT_NODE:
        node = node.nextSibling
    return node

def process_nodes(base, custom):
    base_child = first_after(base.firstChild)
    custom_child = first_after(custom.firstChild)
    while base_child is not None:
        if base_child.nodeName == 'node':
            name = base_child.getAttribute('name')
            if name != '' and (custom_child is None or name != custom_child.getAttribute('name')):
                new_node = custom.ownerDocument.createElement('node')
                new_node.setAttribute('name', name)
                value = base_child.getAttribute('value')
                if value != '':
                    new_node.setAttribute('value', value)
                    new_node.setAttribute('toBeTranslated', 'true')
                custom_child = custom.insertBefore(new_node, custom_child)
            elif custom_child:
                if base_child.getAttribute('value') and not custom_child.getAttribute('value'):
                    custom_child.setAttribute('value', base_child.getAttribute('value'))
                    custom_child.setAttribute('toBeTranslated', 'true')
                elif custom_child.getAttribute('toBeTranslated') == 'true':
                    custom_child.setAttribute('value', base_child.getAttribute('value'))
        if custom_child is not None:
            process_nodes(base_child, custom_child)
            custom_child = first_after(custom_child.nextSibling)
        base_child = first_after(base_child.nextSibling)

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print 'Usage: %s <en.xml> <custom.xml>' % sys.argv[0]
        exit(1)

    base_model = minidom.parse(sys.argv[1])
    custom_model = minidom.parse(sys.argv[2])
    process_nodes(base_model, custom_model)
    print custom_model.toxml('UTF-8')
