package com.xz.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;
import com.xz.io.DecryptInputDetachEncInfo;

public class EditorFrame extends JFrame {
	private String md5password;

	private JTabbedPane area = new JTabbedPane();

	private JCheckBox jCheckBox = new JCheckBox();
	private JComboBox<String> encType = new JComboBox<String>();
	private JComboBox<Integer> offset = new JComboBox<Integer>();

	private JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));

	private class SaveButtonTabComponent extends ButtonTabComponent{

		public SaveButtonTabComponent(JTabbedPane pane, String title) {
			super(pane, title);
		}

		@Override
		public int beforeCloseTab() {
			JScrollPane scrollPane = (JScrollPane) area.getSelectedComponent();
			TabTextArea tabTextArea = (TabTextArea) scrollPane.getViewport().getView();

			int rep = JOptionPane.NO_OPTION;
			if (tabTextArea.isContentChanged()){

				rep = JOptionPane.showConfirmDialog(EditorFrame.this, "save changes?");

				if (rep == JOptionPane.YES_OPTION) {
					try {
						saveFile(null, 0);
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
			TabTextArea tabTextArea = new TabTextArea(area);
			JScrollPane scroll = new JScrollPane(tabTextArea);
			String tabTitle = "Tab #" + (area.getTabCount() + 1);
			area.addTab(tabTitle, scroll);

			area.setTabComponentAt(area.getTabCount()-1, new SaveButtonTabComponent(area, tabTitle));
			area.setSelectedIndex(area.getTabCount()-1);
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

				TabTextArea tabTextArea = new TabTextArea(area);
				JScrollPane scroll = new JScrollPane(tabTextArea);

				area.addTab(fileBaseName, scroll);
				area.setTabComponentAt(area.getTabCount()-1, new SaveButtonTabComponent(area, fileBaseName));
				area.setSelectedIndex(area.getTabCount()-1);

				try {
					readInFile(filepath, tabTextArea);
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
				} catch (EncryptException ex) {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
				}

			}
		}
	}

	Action Open = new OpenAction();

	Action Save = new AbstractAction("Save", new ImageIcon("icons/save.gif")) {
		public void actionPerformed(ActionEvent e) {
			try {
				saveFile(null, 0);
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
				saveFileAs(null, 0);
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

		TabTextArea tabTextArea = new TabTextArea(area);
		JScrollPane scroll = new JScrollPane(tabTextArea);
		area.addTab("Tab #1", scroll);
		area.setTabComponentAt(0, new SaveButtonTabComponent(area, "Tab #1"));

		add(area,BorderLayout.CENTER);
		
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

		tool.addSeparator();
		tool.add(new JLabel("    Encrypt When Save"));
		tool.add(jCheckBox);

		tool.add(new JLabel("    Encrypt Type"));
		encType.addItem("BitReverseEncrypt");
		encType.addItem("BitComplementEncrypt");
		encType.setMaximumSize(new Dimension(200, 30));
		tool.add(encType);

		JLabel offsetLabel = new JLabel("    Offset");
		offsetLabel.setToolTipText("offset is only required for BitComplementEncrypt");
		tool.add(offsetLabel);
		for (int i=1; i<33; i++)
			offset.addItem(i);
		offset.setMaximumSize(new Dimension(100, 30));
		tool.add(offset);
		
//		Save.setEnabled(false);
//		SaveAs.setEnabled(false);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		
		setTitle("Untitled");
		
		Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (screen.getWidth() - this.getWidth()) / 2, (int) (screen.getHeight() - this.getHeight()) / 2);
        
		setVisible(true);
	}
	
	private void readInFile(String fileName, TabTextArea tabTextArea) throws IOException, EncryptException {
		tabTextArea.loadContent(fileName, md5password);

		setTitle(fileName);
	}
	
	private void saveFile(String encType, int offset) throws IOException, EncryptException {
		JScrollPane scrollPane = (JScrollPane) area.getSelectedComponent();
		TabTextArea tabTextArea = (TabTextArea) scrollPane.getViewport().getView();

		if(!tabTextArea.getFileName().equals("Untitled"))
			tabTextArea.saveContent(encType, offset, md5password);
		else {
			if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
				tabTextArea.setFileName(dialog.getSelectedFile().getAbsolutePath());
				tabTextArea.saveContent(encType, offset, md5password);
			}
		}
	}

	private void saveFileAs(String encType, int offset) throws IOException, EncryptException {
		if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
			JScrollPane scrollPane = (JScrollPane) area.getSelectedComponent();
			TabTextArea tabTextArea = (TabTextArea) scrollPane.getViewport().getView();
			tabTextArea.setFileName(dialog.getSelectedFile().getAbsolutePath());
			tabTextArea.saveContent(encType, offset, md5password);

			setTitle(tabTextArea.getFileName());
		}
	}
}
