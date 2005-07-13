Notes on documentation structure and versioning
------------------------------------------------

This folder contains "xdocs" - raw unformatted files for
website and product documentation.

To create HTML documentation, do:
	ant docs

To create docs for the website, do:
	ant -Ddocs.project=project-website.xml
	
When creating new documentation, please note that we've adopted a few
conventions for product documentation (User's Guide, Developer's Guide, etc).

(1) The website always shows the released version of the docs, while
the nightly snapshort has the latest.

(2) To make this work, before generating the website copy the latest
released product docs (in the subdirectory docs) and rename it
"docs14" (for version 1.4)

(3) All internal links to product documenation should be directly to 
the "docs" directory.

(4) Update the file .htaccess to make any link to "docs" link to
"docs14" instead.

Consequently, on a file system a link to docs/user-guide.html will
point to the latest file, while on the webserver it will be forwarded
to docs14/user-guide.html.

Finally, note that (for compatibility with old links) links to all old 
product doc URLs still work (rewritten with .htaccess to go to the new
folder).

