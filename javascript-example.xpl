<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  version="1.0">

  <p:input port="source">
    <p:empty/>
  </p:input>
          
  <p:output port="result"/>

  <p:option name="href" select="'test/test.js'"/>
  
  <p:import href="javascript-declaration.xpl"/>
  
  <tr:javascript>
    <p:with-option name="href" select="$href"/>
  </tr:javascript>
  
</p:declare-step>