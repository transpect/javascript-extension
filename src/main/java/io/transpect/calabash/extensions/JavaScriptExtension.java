package io.transpect.calabash.extensions;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileSystems;

import java.util.concurrent.ExecutionException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import javax.xml.transform.stream.StreamSource;

import org.mozilla.javascript.JavaScriptException;

import io.apigee.trireme.core.NodeEnvironment;
import io.apigee.trireme.core.NodeScript;
import io.apigee.trireme.core.NodeException;
import io.apigee.trireme.core.ScriptStatus;
import io.apigee.trireme.core.Sandbox;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.Configuration;

import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

public class JavaScriptExtension extends DefaultStep
{
  private ReadablePipe source = null;
  private WritablePipe result = null;
    
  public JavaScriptExtension(XProcRuntime runtime, XAtomicStep step) {
      super(runtime,step);
  }
  @Override
  public void setInput(String port, ReadablePipe pipe) {
      source = pipe;
  }
  @Override
  public void setOutput(String port, WritablePipe pipe) {
      result = pipe;
  }
  @Override
  public void reset() {
      source.resetReader();
      result.resetWriter();
  }
  @Override
  public void run() throws SaxonApiException
    {      
        super.run();
        RuntimeValue href = getOption(new QName("href"));
        String jscode = getJSCode(source, href, runtime);
        String output;
        try{
            output = runJS(jscode);
            result.write(createXMLOutput(output, true, runtime));
        }catch(Exception e){
            try{
                output = runNodeJS(jscode);
                result.write(createXMLOutput(output, true, runtime));
            }catch (Exception ex){
                System.out.println(ex.getMessage());
                result.write(createXMLOutput(ex.getMessage(), false, runtime));
            }
        }
    }
    private static String getJSCode(ReadablePipe source, RuntimeValue href, XProcRuntime runtime) throws SaxonApiException {
        if(href != null){
            String jscode = getJSCodeFromFile(href);
            return jscode;
        }else{
            String jscode = getJSCodeFromSource(source, runtime);
            return jscode;
        }
    }
    private static String getJSCodeFromFile(RuntimeValue href){
        String jscode = "";
        Path path = FileSystems.getDefault().getPath(href.getString()).toAbsolutePath();
        try{
            jscode = new String(Files.readAllBytes(path));
        }catch(Exception e) {
            System.out.println("Failed to load file: " + path);
            e.printStackTrace(System.out);
        }
        return jscode;
    }
    private static String getJSCodeFromSource(ReadablePipe source, XProcRuntime runtime) throws SaxonApiException{
        XdmNode doc = source.read();
        Processor proc = runtime.getProcessor();
        Configuration config = proc.getUnderlyingConfiguration();
        XPathCompiler compiler = proc.newXPathCompiler();
        compiler.declareNamespace("c", "http://www.w3.org/ns/xproc-step");
        XdmValue xdm = compiler.evaluate("/c:data/text()", doc);
        String jscode = xdm.toString();
        return jscode;
    }
    private static String runJS(String jscode) throws ScriptException{
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        engine.getContext().setWriter(pw);
        engine.eval(jscode);
        String output = sw.getBuffer().toString().trim();
        return output;
    }
    public static String runNodeJS(String jscode) throws NodeException, IOException, InterruptedException, JavaScriptException {
        ByteArrayOutputStream scriptOutput = new ByteArrayOutputStream();
        NodeEnvironment env = new NodeEnvironment();
        Sandbox sb = new Sandbox()
            .setStdout(scriptOutput)
            .setStderr(scriptOutput);
        NodeScript script = env.createScript("thread.js", jscode, null);
        try {
            script.setSandbox(sb);
            try {
                script.execute().get();
            } catch (ExecutionException ee) {
                if (ee.getCause() instanceof JavaScriptException) {
                    throw (JavaScriptException)ee.getCause();
                } else {
                    throw new NodeException(ee.getCause());
                }
            }
            String output = new String(scriptOutput.toByteArray()).trim();
            return output;
        } finally {
            script.close();
        }
    }
    private static XdmNode XMLParseText(String text, XProcRuntime runtime) throws SaxonApiException{
        Processor proc = runtime.getProcessor();
        DocumentBuilder builder = proc.newDocumentBuilder();
        StreamSource ssource = new StreamSource(new StringReader(text));
        XdmNode xdm = builder.build(ssource);
        return xdm;
    }
    private XdmNode createXMLOutput(String output, Boolean success, XProcRuntime runtime){
      TreeWriter tree = new TreeWriter(runtime);
      tree.startDocument(step.getNode().getBaseURI());
      QName qname = success ? XProcConstants.c_result : XProcConstants.c_error;
      tree.addStartElement(qname);
      try{
          XdmNode xdm = XMLParseText(output, runtime);
          tree.addSubtree(xdm);
      }catch(Exception e){
          tree.addText(output);
      }
      tree.addEndElement();
      tree.endDocument();
      XdmNode result = tree.getResult();
      return result;
    }
}
