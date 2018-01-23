/*
 *  CompletionAction.scala
 *  (ScalaInterpreterPane)
 *
 *  Copyright (c) 2010-2013 Hanns Holger Rutz. All rights reserved.
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


import javax.swing.text.{BadLocationException, JTextComponent}
import tools.nsc.interpreter.PresentationCompilerCompleter
import java.awt.Dialog.ModalityType
import javax.swing.event.{DocumentListener, DocumentEvent}
import java.awt.Point
import javax.swing.{GroupLayout, JList, JScrollPane, JTextField, JDialog, SwingUtilities}
import java.awt.event.{ActionEvent, MouseEvent, MouseAdapter, KeyEvent, KeyAdapter}
import jsyntaxpane.actions.gui.EscapeListener
import jsyntaxpane.util.{StringUtils, SwingUtils}
import jsyntaxpane.SyntaxDocument
import jsyntaxpane.actions.DefaultSyntaxAction

object CompletionAction {
  private final val escapeChars = ";(= \t\n\r"
  private final val defPrefix   = "def "

  // keep the location in line, of the start of the text for completion
  var locInLineStartCompletion = 0
  
  private class Dialog(target: JTextComponent)
    extends JDialog(SwingUtilities.getWindowAncestor(target), ModalityType.APPLICATION_MODAL) with EscapeListener {
    dlg =>

    private var items     = List.empty[String]
    private var succeed   = (_: Option[String]) => ()

    private val ggText    = new JTextField
    private val ggScroll  = new JScrollPane
    private val ggList    = new JList[String]

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
    setResizable(false)
    setUndecorated(true)

    ggText.setBorder(null)
    ggText.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = dlg.keyPressed(e)
    })

    ggList.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = dlg.mouseClicked(e)
    })
    ggList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
    ggList.setFocusable(false)

    ggScroll.setViewportView(ggList)
    private val lay = new GroupLayout(getContentPane)
    getContentPane.setLayout(lay)
    lay.setHorizontalGroup(lay.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addComponent(ggText  , javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MaxValue)
      .addComponent(ggScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MaxValue))
    lay.setVerticalGroup(lay.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(lay.createSequentialGroup
        .addComponent(ggText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0)
        .addComponent(ggScroll, GroupLayout.DEFAULT_SIZE, 111, Short.MaxValue)
      )
    )
    pack()
    ggText.getDocument.addDocumentListener(new DocumentListener {
         // refilter the complerion
      def insertUpdate (e: DocumentEvent): Unit = refilterList()
      def removeUpdate (e: DocumentEvent): Unit = refilterList()
      def changedUpdate(e: DocumentEvent): Unit = refilterList()
    })
    ggText.setFocusTraversalKeysEnabled(false)
    SwingUtils.addEscapeListener(this)

    def show(abbrev: String, items: List[String])(succeed: Option[String] => Unit): Unit = try {
      this.items    = items
      this.succeed  = succeed

      val window    = SwingUtilities.getWindowAncestor(target)
      val rt        = target.modelToView(target.getSelectionStart)   // coordinates of the selection start
      
      val loc       = new Point(rt.x, rt.y)
      
      setLocationRelativeTo(window)
      val loc1      = SwingUtilities.convertPoint(target, loc, window)
      SwingUtilities.convertPointToScreen(loc1, window)
      setLocation(loc1)
    } catch {
      case ex: BadLocationException => // ignore for now
    } finally {
      val font = target.getFont
      ggText.setFont(font)
      ggList.setFont(font)
      doLayout()
      ggText.setText(abbrev)
      refilterList()
      setVisible(true)
    }

    private def refilterList(): Unit = {
      val prefix    = ggText.getText
      val selected  = ggList.getSelectedValue
      val filtered  = items.filter(StringUtils.camelCaseMatch(_, prefix))
      var jvec = new java.util.Vector[String]
      for (x<-filtered) 
          jvec.add(x)
      
      ggList.setListData(jvec)
      if (selected != null && filtered.contains(selected)) {
        ggList.setSelectedValue(selected, true)
      } else {
        ggList.setSelectedIndex(0)
      }
    }

    private def finish(result: Option[String]): Unit = {
      // target.replaceSelection(result)
      succeed(result)
      setVisible(false)
    }

    private def keyPressed(e: KeyEvent): Unit = {
      val i  = ggList.getSelectedIndex
      val ch = e.getKeyChar
      e.getKeyCode match {
        case KeyEvent.VK_ESCAPE => finish(None)

        case KeyEvent.VK_DOWN if i < ggList.getModel.getSize - 1 =>
          val i1 = i + 1
          ggList.setSelectedIndex(i1)
          ggList.ensureIndexIsVisible(i1)

        case KeyEvent.VK_UP if i > 0 =>
          val i1 = i - 1
          ggList.setSelectedIndex(i1)
          ggList.ensureIndexIsVisible(i1)

        case _ if escapeChars.indexOf(ch) >= 0 =>
          val result0 = if (ggList.getSelectedIndex >= 0) {
            ggList.getSelectedValue.toString
          } else {
            ggText.getText
          }
          val result = if (ch == '\n') result0 else {
            result0 + (if (ch == '\t') ' ' else ch)
          }
          finish(Some(result))

        case _ =>
      }
    }

    private def mouseClicked(e: MouseEvent): Unit = {
      if (e.getClickCount == 2) {
        val selected: String = ggList.getSelectedValue.toString
        target.replaceSelection(selected)
        setVisible(false)
      }
    }

    def escapePressed(): Unit = setVisible(false)
  }
}
class CompletionAction(completer: PresentationCompilerCompleter) extends DefaultSyntaxAction("COMPLETION") {
  import CompletionAction.defPrefix
  
  private var dlg: CompletionAction.Dialog = null

  override def actionPerformed(target: JTextComponent, sdoc: SyntaxDocument, dot: Int, e: ActionEvent): Unit = {
    
    // cw: the text from the start of line up to caret position
    // start: offset of line start in document
    var (cw, start) = {
      val sel = target.getSelectedText
      if (sel != null) {
        // if the user has selected text complete with that text
        (sel, target.getSelectionStart)
      } else {
        val line  = sdoc.getLineAt(dot)
        val start = sdoc.getLineStartOffset(dot)

        (line.substring(0, dot - start), start)
      }
    }
    
    // extract text for completion, cw is sthe string from the start of line to the 
    // location where the user tries to complete
    var origcw = cw.toString   // keep a copy cw
 //   \s means a whitespace character: [ \t\n\x0B\f\r]
    var result = cw.split("\\s")
    cw = result(result.length-1)  // the text for completion

// index of the last token in line is point of the completion start    
   CompletionAction.locInLineStartCompletion = origcw.indexOf(cw)

    var cwlen = cw.length
    val m     = completer.complete(cw, cwlen)
    val cand  = m.candidates
    if (cand.isEmpty) return

    val off = start + CompletionAction.locInLineStartCompletion+m.cursor

    val hasDef = cand.exists(_.startsWith(defPrefix))

    val more1 @ (head :: tail) = if (!hasDef) cand else cand.map {
      case x if x.startsWith(defPrefix) => x.substring(4)  // cheesy way of handling the 'def'
      case x => x
    }

    val common  = if (!hasDef) 0 else {
      val comh0   = head.indexOf('[')
      val comh1   = if (comh0 >= 0) comh0 else head.length
      val comh2   = head.indexOf('(')
      val comh3   = if (comh2 >= 0) math.min(comh1, comh2) else comh1
      val comh4   = head.indexOf(':')
      val comh    = if (comh4 >= 0) math.min(comh4, comh3) else comh3
      
      (comh /: tail) { (len, s1) =>
        val m1 = math.min(len, s1.length)
        var m2 = 0
        while (m2 < m1 && s1.charAt(m2) == head.charAt(m2)) m2 += 1
        m2
      }
    }

    target.select(off - common, start + CompletionAction.locInLineStartCompletion+cwlen)

    def perform(replc: String): Unit = {
      val replc1 = removeTypes(replc, 0)
      val p0 = target.getSelectionStart+CompletionAction.locInLineStartCompletion
      target.replaceSelection(replc1)
      val i = replc1.indexOf('(') + 1
      if (i > 0) {
        val j = replc1.indexOf(',', i)
        val k = replc1.indexOf(')', i)
        val m = math.min(j, k)
        target.select(p0 + i, p0 + m)
      }
    }

    
    more1 match {
      case one :: Nil => perform(one)   // the match performs directly the completion
                                                    // replacement without popping a dialog

      case more =>
        if (dlg == null) dlg = new CompletionAction.Dialog(target)

        dlg.show(cw.substring(m.cursor), more1) {
          case Some(result) =>
            
            perform(result)
          case _ => // aborted
// orig:             target.setSelectionStart(target.getSelectionEnd)

            target.setSelectionStart(target.getSelectionEnd+CompletionAction.locInLineStartCompletion)
        }
    }
  }

  private def removeTypes(s: String, i0: Int): String = {
    val i   = s.indexOf('[', i0)
    val j   = s.indexOf('(', i0)
    if (i >= 0 && i < j) {      // type parameter
      val k  = s.indexOf(']', i + 1)
      removeTypes(s.substring(0, i) + s.substring(k + 1), i0)
    } else if (j >= 0) {
      val k = s.indexOf(')', j + 1)
      val m = s.indexOf(':', j + 1)
      if (m >= 0 && m < k) {
        val n     = s.indexOf(',', m + 1)
        val pre   = s.substring(0, m)
        val post  = s.substring(if (n >= 0 && n < k) n else k)
        removeTypes(pre + post, i0)
      } else if (m == k + 1) {
        s.substring(0, m)
      } else if (k >= 0 /* && k < s.length - 1 && s.charAt(k + 1) == '(' */ ) {  // multiple argument lists
        removeTypes(s, k + 1)
      } else {
        s
      }
    } else {
      val m = s.indexOf(':', i0)
      if (m >= 0) s.substring(0, m) else s
    }
  }
}