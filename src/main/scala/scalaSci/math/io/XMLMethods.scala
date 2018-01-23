
package scalaSci.math.io

object  XMLMethods  {

   def  GlobalConfToXML = {
     var node = 
    <ScalaLabConf>
       <buildDate>  {scalaExec.Interpreter.GlobalValues.scalalabBuildVersion} </buildDate>
      </ScalaLabConf>
      
  }
  
  def xsave(xname: String, v: Array[Double]): Unit = 
    xsave(v, xname)
  
  // save the double array v to the XML file named xname
   def  xsave( v: Array[Double],  xname: String): Unit = {
        var svalues = ""
        for (k<-0 until v.length)
          svalues += "\n"+v(k)
        svalues += "\n"
        
          
var node =
   <ArrayDouble>{svalues}</ArrayDouble>
  
    var xmlName = 
      if (xname.endsWith(".xml") == false)
        xname+".xml"
      else xname
      
    scala.xml.XML.save(xmlName, node)
    
  }
  
  // loads a double array saved in xml file
  def xload_d(xmlFile: String): Array[Double] = {
    var xmlName = 
      if (xmlFile.endsWith(".xml") == false)
        xmlFile +".xml"
    else xmlFile
    
    var loadNode = xml.XML.loadFile(xmlName)
    loadNode match {
      case <ArrayDouble>{contents}</ArrayDouble>   => 
        var strContents = contents.toString
        var tok = new java.util.StringTokenizer(strContents, "\n")    
        var lst = new scala.collection.mutable.ArrayBuffer[Double](1)
        while (tok.hasMoreTokens()) {
          var toknext = tok.nextToken
          lst.append(java.lang.Double.parseDouble(toknext))
        }
        lst.toArray
      
      case _ => new Array[Double](1)
        
    }
  }
  /*
   var x = vrand(4).getv
   xsave(x, "x")
   var dd = xload_d("x")
   
   */
  def  xsave( v: Array[Array[Double]],  name: String) = {
        var svalues = ""
        var Nrows = v.length
        var Ncols = v(0).length
        for (k<-0 until v.length)
          
          svalues += "\n"+v(k)
          
var node =
  <ArrayDoubleDouble>
   <name> {name}  </name>
   <values> {svalues} </values>
   </ArrayDoubleDouble>
 
    
    var xmlName = 
      if (name.endsWith(".xml") == false)
        name+".xml"
      else name
      
    scala.xml.XML.save(xmlName, node)
    node
  }
    
    
  }

