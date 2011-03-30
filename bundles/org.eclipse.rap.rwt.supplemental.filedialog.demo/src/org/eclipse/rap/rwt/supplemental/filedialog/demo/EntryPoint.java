/******************************************************************************
 * Copyright (c) 2011 Texas Center for Applied Technology
 * Texas Engineering Experiment Station
 * The Texas A&M University System
 * All Rights Reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Austin Riddle (Texas Center for Applied Technology) - 
 *                   initial demo implementation
 * 
 *****************************************************************************/
package org.eclipse.rap.rwt.supplemental.filedialog.demo;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class EntryPoint implements IEntryPoint{


    public int createUI(){
        Display display = PlatformUI.createDisplay();
        
        final Shell mainShell = new Shell(display, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        mainShell.setLayout(new FillLayout());
        mainShell.setText("FileDialog Demo");
        
        createContent(mainShell);
        
        mainShell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e){
                mainShell.dispose();
            }
        });
        
        mainShell.setMaximized(true);
        mainShell.open();
        while( !mainShell.isDisposed() ) {
          if( !display.readAndDispatch() ) {
            display.sleep();
          }
        }
        
        return 0;
    }
    
    public Composite createContent (Composite parent) {
      Composite content = new Composite(parent, SWT.BORDER);
      content.setLayout(new GridLayout(2,false));
      Group resultsGroup = new Group(content, SWT.NONE);
      resultsGroup.setLayout(new FillLayout());
      resultsGroup.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
      resultsGroup.setText("File Upload Results");
      final List statsList = new List(resultsGroup, SWT.MULTI | SWT.V_SCROLL);
      
      Composite btnComp = new Composite(content, SWT.NONE);
      GridDataFactory factory = GridDataFactory.fillDefaults();
      btnComp.setLayoutData(factory.create());
      btnComp.setLayout(new GridLayout(1,true));
      final Button autoBtn = new Button(btnComp, SWT.CHECK);
      autoBtn.setText("Auto upload");
      autoBtn.setToolTipText("Auto upload files upon selection");
      autoBtn.setLayoutData(factory.create());
      
      Button addBtn = new Button(btnComp, SWT.PUSH);
      addBtn.setText("Add Single File");
      addBtn.setToolTipText("Launches file dialog for single file selection.");
      addBtn.setLayoutData(factory.create());
      addBtn.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(), SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
          fd.setAutoUpload(autoBtn.getSelection());
          fd.setText("Upload Single File");
          fd.setFilterPath("C:/");
          String[] filterExt = { "*.txt", "*.doc", "*.rtf", "*.*" };
          fd.setFilterExtensions(filterExt);
          String[] filterNames = { "Text Files", "Word Document", "Rich Text Format", "All Files" };
          fd.setFilterNames(filterNames);
          String selected = fd.open();
          if (selected != null) {
            statsList.add(selected);
          }
        }
      });
      Button addMultiBtn = new Button(btnComp, SWT.PUSH);
      addMultiBtn.setText("Add Multiple Files");
      addMultiBtn.setToolTipText("Launches file dialog for multiple file selection.");
      addMultiBtn.setLayoutData(factory.create());
      addMultiBtn.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(), SWT.SHELL_TRIM | SWT.MULTI | SWT.APPLICATION_MODAL);
          fd.setAutoUpload(autoBtn.getSelection());
          fd.setText("Upload Multiple Files");
          fd.setFilterPath("C:/");
          String[] filterExt = { "*.txt", "*.doc", "*.rtf", "*.*" };
          fd.setFilterExtensions(filterExt);
          String[] filterNames = { "Text Files", "Word Document", "Rich Text Format", "All Files" };
          fd.setFilterNames(filterNames);
          String selected = fd.open();
          if (selected != null && selected.length() > 0) {
            String[] fileNames = fd.getFileNames();
            for (int i = 0; i < fileNames.length; i++) {
              statsList.add(fileNames[i]);
            }
          }
        }
      });
      
      Button clearBtn = new Button(btnComp, SWT.PUSH);
      clearBtn.setText("Clear");
      clearBtn.setToolTipText("Clears the results list");
      clearBtn.setLayoutData(factory.create());
      clearBtn.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          statsList.removeAll();
        }
      });
      
      return content;
    }
    
}
