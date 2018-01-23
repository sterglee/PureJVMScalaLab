/*
 * This code is adapted for ScalaLab from a related work of 
 *  Hanns Holger Rutz in his ScalaInterpreterPane.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package  scalainterpreter


import java.awt.event.ActionEvent
import scalaExec.Interpreter.GlobalValues
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import scalaExec.gui.AutoCompletionFrame
import scala.tools.nsc.interpreter.Parsed
import javax.swing.JList

class ScalaCompletionAction() extends javax.swing.AbstractAction() {
   
// the completion implemented requires a selection text to be specified
      def actionPerformed(e: ActionEvent) {
  
  val  target = GlobalValues.editorPane   // the editor for which the completion works
  var cstr = target.getSelectedText  //  get the selection text
  
  val endCommandChar = ';'   // character denoting the end of the previous command
  
  if (cstr == null)   
  {   // no text is selected, use the whole buffer
    cstr = target.getText        // the whole buffer text
   
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
                     
    
  }  // no text is selected: use the whole buffer
  
                    // create a completion object                    
            var completer = new  scala.tools.nsc.interpreter.PresentationCompilerCompleter(scalaExec.Interpreter.GlobalValues.globalInterpreter);
                     
                    var scalaResultsList = completer.complete(cstr, 0).candidates
                   var   matchedSize = scalaResultsList.length
               if (matchedSize > 0) {  // top level elements exist, edit them
                     val    dmatches =  new Array[Object](matchedSize)
                     val    complResults = scalaResultsList.iterator
                     var    k=0
                     while (complResults.hasNext)  {   // collect the matched results
                         var  currentCompletion =  complResults.next().asInstanceOf[String]
                         dmatches(k) = currentCompletion
                         k += 1
                  }
                var topLevelResultsList = new JList(dmatches)
                
        
           GlobalValues.autoCompletionFrame = new AutoCompletionFrame("Scala Completer results");
           GlobalValues.autoCompletionFrame.displayMatches(topLevelResultsList);
                        
                          
            }
            
            
      }
}

