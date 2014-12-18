#!/usr/bin/python

import os, re, shutil, sys, time, zipfile

def collect_languages(proto_dir):
    return [os.path.splitext(name)[0] for name in os.listdir(proto_dir) if name.endswith('.html')]

def find_title(path):
    pattern = re.compile(r'<h2>(.*)<\/h2>')
    with open(path, 'r') as stream:
        for line in stream:
            match = pattern.match(line)
            if match:
                return match.group(1)
    return None

def string_from_template(template_path, data):
    result = ''
    pattern = re.compile(r'(.*)\$([^$]*)\$(.*\n)')
    with open(template_path, 'r') as istream:
        for line in istream:
            match = pattern.match(line)
            if match:
                result += match.group(1) + data[match.group(2)] + match.group(3)
            else:
                result += line
    return result

def generate_epub(proto_dir, html_dir, epub, lang):
    html_file = os.path.join(html_dir, lang + '.html')
    title = find_title(html_file)
    if not title:
        raise Exception('Title not found in %s' % htmlfile)

    data = {
        'TITLE': title,
        'LANG':  lang,
        'ID':    'fbreader:intro:%s:%d' % (lang, time.time())
    }

    with zipfile.ZipFile(epub, 'w', zipfile.ZIP_DEFLATED) as zip_file:
        zip_file.writestr('mimetype', 'application/epub+zip', zipfile.ZIP_STORED)
        zip_file.write(os.path.join(proto_dir, 'container.xml'), arcname='META-INF/container.xml')
        zip_file.write(os.path.join(proto_dir, 'style.css'), arcname='style.css')
        zip_file.writestr('main.html', string_from_template(html_file, data))
        zip_file.writestr('content.opf', string_from_template(os.path.join(proto_dir, 'content.opf'), data))
        zip_file.write(os.path.join(proto_dir, 'fbreader.png'), arcname='fbreader.png')

if __name__ == '__main__':
    if len(sys.argv) != 4:
        exit('Usage: %s <proto_dir> <html_dir> <output_dir>' % sys.argv[0])

    proto_dir = sys.argv[1]
    html_dir = sys.argv[2]
    output_dir = sys.argv[3]

    os.makedirs(output_dir)
    for lang in collect_languages(html_dir):
        epub = os.path.join(output_dir, 'intro-' + lang + '.epub')
        if os.path.exists(epub):
            os.remove(epub)
        print 'Generating intro for language %s...' % lang
        generate_epub(proto_dir, html_dir, epub, lang)
