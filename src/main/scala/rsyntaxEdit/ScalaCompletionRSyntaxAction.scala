
package rsyntaxEdit



import java.awt.event.ActionEvent
import javax.swing.text.JTextComponent
import tools.nsc.interpreter.PresentationCompilerCompleter
import collection.JavaConversions

class ScalaCompletionRSyntaxAction( completer: PresentationCompilerCompleter )   {

   def performCompletion() = {
  
    
  val  target = scalaExec.Interpreter.GlobalValues.globalRSyntaxEditorPane  // the editor for which the completion works
  
      val (cw, start) = {  
         val sel = target.getSelectedText
         if( sel != null ) {      // selected text does exist
            (sel, target.getSelectionStart)
         } else {  //  selected text does not exist

     val endCommandChar = ';'   // character denoting the end of the previous command
     
          // no text is selected, use the whole buffer
    var cstr = target.getText        // the whole buffer text
// get the string up to the current position
      val  currentPos = target.getCaretPosition
      cstr = cstr.substring(0, currentPos)
                              // get the starting point of the current command start                    
  var  lastNewLineCommandStartIdx = cstr.lastIndexOf('\n')+1
  if (lastNewLineCommandStartIdx == -1)
    lastNewLineCommandStartIdx = 0
  
  var posAutoCompletion = lastNewLineCommandStartIdx  //  indexes the start of autocompletion
  var  frozeneditorPaneContents = target.getText   // a copy of the buffer
                         
  // check for multiple commands per line       
  val previousSemicolonCommandStartIdx = cstr.lastIndexOf(endCommandChar)+1
  if (previousSemicolonCommandStartIdx > lastNewLineCommandStartIdx)  {  // multiple commands per line separated by ';''
    posAutoCompletion = previousSemicolonCommandStartIdx  // update index after ';'
    }
    
      //// update start of autocompletion to be after that characters
    val   toksToCheckForLastIndex = "= +*-"
    val   toksCnt = toksToCheckForLastIndex.length
    for ( k <- 0 until toksCnt)  {
      var tokIndex = cstr.lastIndexOf(toksToCheckForLastIndex.charAt(k))+1  // index of token  (in order to match the text after it)
      if (tokIndex > posAutoCompletion)  {
        posAutoCompletion = tokIndex
                 }
        }
                   
        // take the final text to use for code completion
         cstr = cstr.substring(posAutoCompletion, cstr.length()).trim()
     // val line    = sdoc.getLineAt( dot )
            
        //    (line.substring( 0, dot - start ), start)
          (cstr, posAutoCompletion)  
        
         }
      }

      val cwlen = cw.length()
      val m = completer.complete( cw, cwlen )
      if( m.candidates.isEmpty )   ""
      else {

      val off = start + m.cursor
      target.select( off, start + cwlen )

    m.candidates match {
         case one :: Nil =>
            target.replaceSelection( one )
         case more =>
          scalaExec.Interpreter.GlobalValues.scalaResultsForCompletion = new java.util.ArrayList()     
          more foreach ( scalaExec.Interpreter.GlobalValues.scalaResultsForCompletion.add(_))    // add the elements for completion
          rsyntaxEdit.GCompletionProvider.createScalaCompletionProvider()
          }
      m.candidates.toString
   }
   }
   
  } 
  