from os import path, system

def visit(arg, dirname, names):
	print dirname
	for n in names:
		fn = path.join(dirname, n)
		if not path.isdir(fn):
			system('cp ' + fn + ' res/drawable-hdpi')

path.walk('icons', visit, 0)
