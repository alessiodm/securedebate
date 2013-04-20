SecureDebate
============

__SecureDebate__ is a simple *academic project* for showing how to create a secure multi-chat client-server application with PKI mutual authentication.

## Give It a Try

It is very simple to run it locally using Maven:

 - Start the server: `mvn exec:exec -Dserver`
 - Start one or more clients: `mvn exec:exec -Dclient`
 

## Architecture

Please, remember that it is not intended to be an hyper-fancied and very-well architected distributed application :) so do not start a real-life distributed application moving from the strucure of SecureDebate.

Anyway, __SecureDebate__ implements a simple communication protocol and a well-defined classical client-server architecture.

### Communication Protocol

![Protocol](https://raw.github.com/alessiodm/securedebate/master/docs/protocol.jpg)

### Implementation

![Implementation](https://raw.github.com/alessiodm/securedebate/master/docs/comm.jpg)


## LICENSE - "MIT License"

Copyright (c) 2013 Alessio Della Motta, http://alessiodellamotta.com

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
