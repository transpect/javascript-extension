# javascript-extension

An extension step for XML Calabash to run JavaScript and NodeJS in XProc

## Description

This step runs JavaScript code either by reading it from the input port or from a reference to an external file.

```xml
<tr:javascript>
  <p:with-option name="href" select="'test.js'"/>
</tr:javascript>

```

```xml
<tr:javascript>
  <p:inline>
    <c:data>
      print('hello world')
    </c:data>
  </p:inline>
</tr:javascript>

```

The step implements the JavaScript engines [Rhino](https://github.com/mozilla/rhino) from Mozilla and [Nashorn](https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/) from Oracle and the [trireme](https://github.com/apigee/trireme) library from Apigee for running node.js scripts inside the JVM.

## Requirements

* Java 1.8

## XML Calabash Configuration

* add the path to `JavaScriptExtension.java` and the jar files in the `lib directory` to your Java classpath
* edit your XProc config and add the class file

```xml
<xproc-config xmlns="http://xmlcalabash.com/ns/configuration" 
  xmlns:tr="http://transpect.io">

  <implementation type="tr:javascript" class-name="JavaScriptExtension"/>
    
</xproc-config>

```

### Limitations

NodeJS code is just executed with Trireme and Rhino. Trireme doesn't support the newer JavaScript engine Nashorn. Please also note that there are differences between the JavaScript implementation of Rhino and NodeJS. There is a detailed [list of supported features](http://mozilla.github.io/rhino/compat/engines.html) from Mozilla.

