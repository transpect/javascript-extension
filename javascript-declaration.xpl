<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io"
  type="tr:javascript"
  version="1.0">
  
  <p:documentation>
    An extension step for XML Calabash to run JavaScript and NodeJS in XProc 
  </p:documentation>
  
  <p:input port="source" sequence="true">
    <p:documentation>
      You can pass a script to this step as input. You have to add 
      a c:data wrapper element. If you want to run an external 
      script, you have to declare this port as empty with p:empty.
    </p:documentation>
  </p:input>

  <p:output port="result" sequence="true">
    <p:documentation>
      Provides the standard output of the script.
    </p:documentation>
  </p:output>
  
  <p:option name="href" required="false">
    <p:documentation>
      Path to an external script.
    </p:documentation>
  </p:option>
  
</p:declare-step>