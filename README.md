DNPakToolUI
========
[![GitHub release](https://img.shields.io/github/release/vincentzhang96/DNPakToolUI.svg?style=flat-square)](https://github.com/vincentzhang96/DNPakToolUI/releases/latest)
[![Travis](https://img.shields.io/travis/vincentzhang96/DNPakToolUI.svg?style=flat-square)](https://travis-ci.org/vincentzhang96/DNPakToolUI)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://github.com/vincentzhang96/DNPakToolUI#license)

A GUI application for browsing Eyedentity/Dragon Nest resource pak files, as a UI front for  
[DNPakTool](https://github.com/vincentzhang96/DNPakTool).

##Download
You may download the latest stable release [here](https://github.com/vincentzhang96/DNPakToolUI/releases/latest).

##To Do
- [ ] Add a throbber when exporting file(s)
- [ ] Implement Search
- [ ] Infrastructure for caching decompressed files to a temp directory
- [ ] Infrastructure for in-tool viewing of files
- [ ] Thumbnail view for directories when selected?
- [ ] Internationalize

##Building
This project uses Maven. Simply build the JAR by running `mvn package`.

You may need to include DNPakTool into your local Maven repository by running:
```
mvn install:install-file -Dfile=DNPakTool-1.0.1.jar -DgroupId=co.phoenixlab.dn -DartifactId=DNPakTool -Dversion=1.0.1 -Dpackaging=jar
```

##Dependencies
- [DNPakTool](https://github.com/vincentzhang96/DNPakTool)
- Java 8u31

##Contributing
Simply submit a pull request and I'll review it for inclusion.

##License

The MIT License (MIT)

Copyright (c) 2015 Vincent Zhang/PhoenixLAB

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
