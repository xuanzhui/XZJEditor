package com.xz.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

import com.xz.encrypt.EncryptException;

public class EditorFrame extends JFrame {
	private String md5password;

	private JTabbedPane area = new JTabbedPane();



	private JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));

	private class SaveButtonTabComponent extends ButtonTabComponent{

		public SaveButtonTabComponent(JTabbedPane pane, String title) {
			super(pane, title);
		}

		@Override
		public int beforeCloseTab() {
			TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getSelectedComponent();

			int rep = JOptionPane.NO_OPTION;
			if (tabEncryptTextArea.isContentChanged() || tabEncryptTextArea.isEncTypeChanged()){

				rep = JOptionPane.showConfirmDialog(EditorFrame.this, "save changes?");

				if (rep == JOptionPane.YES_OPTION) {
					try {
						saveFile(false);
					} catch (IOException ex) {
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
					} catch (EncryptException ex) {
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

			return rep;
		}
	}

	private class NewAction extends AbstractAction {
		
		public NewAction(){
			super("New", new ImageIcon("icons/new.png"));
			this.putValue(SHORT_DESCRIPTION, "New Tab");
		}
		
		public void actionPerformed(ActionEvent e) {
			TabEncryptTextArea tabEncryptTextArea = new TabEncryptTextArea(area);
			String tabTitle = "Tab #" + (area.getTabCount() + 1);
			area.addTab(tabTitle, tabEncryptTextArea);

			area.setTabComponentAt(area.getTabCount() - 1, new SaveButtonTabComponent(area, tabTitle));
			area.setSelectedIndex(area.getTabCount() - 1);

			tabEncryptTextArea.getjTextArea().requestFocus();
		}
	}
	
	Action New = new NewAction();

	private class OpenAction extends AbstractAction {
		public OpenAction(){
			super("Open", new ImageIcon("icons/open.gif"));
			this.putValue(SHORT_DESCRIPTION, "Open File");
		}

		public void actionPerformed(ActionEvent e) {
			if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filepath = dialog.getSelectedFile().getAbsolutePath();
				String fileBaseName = new File(filepath).getName();

				TabEncryptTextArea tabEncryptTextArea = new TabEncryptTextArea(area);

				area.addTab(fileBaseName, tabEncryptTextArea);
				area.setTabComponentAt(area.getTabCount()-1, new SaveButtonTabComponent(area, fileBaseName));
				area.setSelectedIndex(area.getTabCount()-1);

				try {
					readInFile(filepath, tabEncryptTextArea);
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
				} catch (EncryptException ex) {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
				}

				tabEncryptTextArea.getjTextArea().requestFocus();
			}
		}
	}

	Action Open = new OpenAction();

	Action Save = new AbstractAction("Save", new ImageIcon("icons/save.gif")) {
		public void actionPerformed(ActionEvent e) {
			try {
				saveFile(true);
			} catch(IOException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			} catch (EncryptException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	
	Action SaveAs = new AbstractAction("Save as...") {
		
		public void actionPerformed(ActionEvent e) {
			try {
				saveFileAs(true);
			} catch(IOException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			} catch (EncryptException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	};
	
	Action Quit = new AbstractAction("Quit") {
		public void actionPerformed(ActionEvent e) {
			//saveOld();
			System.exit(0);
		}
	};

	public EditorFrame(String md5password) {
		this.md5password = md5password;

		TabEncryptTextArea tabEncryptTextArea = new TabEncryptTextArea(area);
		area.addTab("Tab #1", tabEncryptTextArea);
		area.setTabComponentAt(0, new SaveButtonTabComponent(area, "Tab #1"));

		add(area, BorderLayout.CENTER);
		
		JMenuBar JMB = new JMenuBar();
		setJMenuBar(JMB);
		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMB.add(file); JMB.add(edit);

		file.add(New);file.add(Open);
		Save.putValue(AbstractAction.SHORT_DESCRIPTION, "Save File");
		file.add(Save);
		file.add(Quit);file.add(SaveAs);
		file.addSeparator();
		
		for(int i=0; i<4; i++)
			file.getItem(i).setIcon(null);

		/*
		ActionMap m = area.getActionMap();
		Action Cut = m.get(DefaultEditorKit.cutAction);
		Action Copy = m.get(DefaultEditorKit.copyAction);
		Action Paste = m.get(DefaultEditorKit.pasteAction);
		*/

		Action Cut = new DefaultEditorKit.CutAction();
		Cut.putValue(AbstractAction.SHORT_DESCRIPTION, "Cut");
		Action Copy = new DefaultEditorKit.CopyAction();
		Copy.putValue(AbstractAction.SHORT_DESCRIPTION, "Copy");
		Action Paste = new DefaultEditorKit.PasteAction();
		Paste.putValue(AbstractAction.SHORT_DESCRIPTION, "Paste");

		edit.add(Cut);edit.add(Copy);edit.add(Paste);

		edit.getItem(0).setText("Cut");
		edit.getItem(1).setText("Copy");
		edit.getItem(2).setText("Paste");
		
		JToolBar tool = new JToolBar();
		add(tool, BorderLayout.NORTH);
		
		tool.add(New);tool.add(Open);tool.add(Save);
		tool.addSeparator();
		
		JButton cut = tool.add(Cut), cop = tool.add(Copy),pas = tool.add(Paste);
		
		cut.setText(null); cut.setIcon(new ImageIcon("icons/cut.gif"));
		cop.setText(null); cop.setIcon(new ImageIcon("icons/copy.gif"));
		pas.setText(null); pas.setIcon(new ImageIcon("icons/paste.gif"));

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//everytime a tab is closed, tab total count will decrease 1
				while (area.getTabCount() > 0) {
					area.setSelectedIndex(0);
					((ButtonTabComponent) area.getTabComponentAt(0)).closeTab();
				}
				//System.exit(0);
			}
		});
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		
		setTitle("Untitled");
		
		Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (screen.getWidth() - this.getWidth()) / 2, (int) (screen.getHeight() - this.getHeight()) / 2);
        
		setVisible(true);

		//show UI then focus
		tabEncryptTextArea.getjTextArea().requestFocus();
	}

	private void readInFile(String fileName, TabEncryptTextArea tabEncryptTextArea) throws IOException, EncryptException {
		tabEncryptTextArea.loadContent(fileName, md5password);

		setTitle(fileName);
	}
	
	private void saveFile(boolean echoTitle) throws IOException, EncryptException {
		TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getSelectedComponent();

		if(!tabEncryptTextArea.getFileName().equals("Untitled"))
			tabEncryptTextArea.saveContent(md5password, echoTitle);
		else {
			if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
				tabEncryptTextArea.setFileName(dialog.getSelectedFile().getAbsolutePath());
				tabEncryptTextArea.saveContent(md5password, echoTitle);
			}
		}
	}

	private void saveFileAs(boolean echoTitle) throws IOException, EncryptException {
		if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
			TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getSelectedComponent();
			tabEncryptTextArea.setFileName(dialog.getSelectedFile().getAbsolutePath());
			tabEncryptTextArea.saveContent(md5password, echoTitle);

			setTitle(tabEncryptTextArea.getFileName());
		}
	}
}
