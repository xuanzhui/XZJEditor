package com.xz.frame;

import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;
import com.xz.io.DecryptInputDetachEncInfo;
import com.xz.io.EncryptOutputAttachEncInfo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Created by xuanzhui on 15/5/11.
 */
public class TabEncryptTextArea extends JPanel {

    private String fileName = "Untitled";
    private String fileBaseName = "Untitled";
    private String md5password;

    private boolean contentChanged = false;
    private boolean encTypeChanged = false;

    private JTabbedPane tabbedPane;
    private JTextArea jTextArea;
    private JCheckBox jCheckBox = new JCheckBox();
    private JComboBox<String> encType = new JComboBox<String>();
    private JComboBox<Integer> offset = new JComboBox<Integer>();

    private JTextField jKeyword = new JTextField(12);
    private JButton findBtn = new JButton("Find");
    private JButton findPrevBtn = new JButton("Find Previous");
    private JButton findAllBtn = new JButton("Find All");

    private JTextField jReplaceKW = new JTextField(12);
    private JButton replaceBtn = new JButton("Replace");
    private JButton replaceAllBtn = new JButton("Replace All");

    JToolBar findReplaceTool = new JToolBar();
    private boolean findToolAvailable = false;
    private int pos=0;

    public TabEncryptTextArea(final JTabbedPane tabbedPane, String md5password){
        super();

        this.tabbedPane = tabbedPane;
        this.md5password = md5password;

        this.setLayout(new BorderLayout());

        jTextArea = new JTextArea(20,120);

        this.add(new JScrollPane(jTextArea), BorderLayout.CENTER);

        JToolBar tool = new JToolBar();
        this.add(tool, BorderLayout.NORTH);

        tool.addSeparator();
        tool.add(new JLabel("    Encrypt When Save"));
        tool.add(jCheckBox);

        jCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                encTypeChanged = true;
            }
        });

        tool.add(new JLabel("    Encrypt Type"));
        encType.addItem("BitReverseEncrypt");
        encType.addItem("BitComplementEncrypt");
        encType.setMaximumSize(new Dimension(200, 30));
        encType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    encTypeChanged = true;
            }
        });
        tool.add(encType);

        JLabel offsetLabel = new JLabel("    Offset");
        offsetLabel.setToolTipText("offset is only required for BitComplementEncrypt");
        tool.add(offsetLabel);
        for (int i=1; i<33; i++)
            offset.addItem(i);
        offset.setMaximumSize(new Dimension(100, 30));
        offset.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    encTypeChanged = true;
            }
        });
        tool.add(offset);

        jTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        jTextArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()){
                    if (e.getKeyCode() == KeyEvent.VK_F) {
                        toggleSearchReplaceTool();
                        return;
                    } else if (e.getKeyCode() == KeyEvent.VK_S) {
                        try {
                            saveContent(true);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (EncryptException e1) {
                            e1.printStackTrace();
                        }
                        return;
                    }
                }

                if (!contentChanged) {
                    contentChanged = true;

                    //tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), "* "+fileName);
                    ((ButtonTabComponent) tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())).
                            changeTabLabelTitle("* " + fileBaseName);
                }
            }
        });

        jKeyword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    findKeyword(false);
                }
            }
        });

        findBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                findKeyword(false);
            }
        });

        findPrevBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                findKeyword(true);
            }
        });

        findAllBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String keyword = jKeyword.getText();
                if (keyword.length() == 0 || keyword.equals(" "))
                    return;

                String text = jTextArea.getText();
                pos = text.indexOf(keyword, 0);
                jTextArea.getHighlighter().removeAllHighlights();

                if (pos == -1){
                    pos = 0;
                    JOptionPane.showMessageDialog(null, "can not find " + keyword);
                    return;
                }

                int firstpos = pos;
                int kwlength = keyword.length();

                //highlight
                while (pos != -1) {
                    try {
                        jTextArea.getHighlighter().addHighlight(pos, pos + kwlength,
                                new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE));
                        pos = text.indexOf(keyword, pos + kwlength);
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                //scroll to keyword
                try {
                    Rectangle rectangle = jTextArea.modelToView(firstpos);
                    jTextArea.scrollRectToVisible(rectangle);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }

                pos = 0;

                /*
                if (pos>text.length())
                    pos = 0;
                    */
            }
        });

        replaceBtn.addMouseListener(new MouseAdapter() {
            /*
            @Override
            public void mouseClicked(MouseEvent e) {
                String keyword = jKeyword.getText();
                if (keyword.length() == 0)
                    return;

                String text = jTextArea.getText();
                pos = text.indexOf(keyword, 0);
                if (pos == -1) {
                    pos = 0;
                    JOptionPane.showMessageDialog(null, "can not find " + keyword);
                    return;
                }

//                Rectangle rectangle = null;
//                try {
//                    rectangle = jTextArea.modelToView(pos);
//                } catch (BadLocationException e1) {
//                    e1.printStackTrace();
//                }

                jTextArea.setText(text.replaceFirst(keyword, jReplaceKW.getText()));
                //jTextArea.revalidate();

                //scroll to first keyword occurrence
                Rectangle rectangle = null;
                try {
                    rectangle = jTextArea.modelToView(pos);
                    jTextArea.scrollRectToVisible(rectangle);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }


            }*/

            @Override
            public void mousePressed(MouseEvent e) {
                String keyword = jKeyword.getText();
                if (keyword.length() == 0)
                    return;

                String text = jTextArea.getText();
                pos = text.indexOf(keyword, 0);
                if (pos == -1) {
                    pos = 0;
                    JOptionPane.showMessageDialog(null, "can not find " + keyword);
                    return;
                }

                jTextArea.setText(text.replaceFirst(keyword, jReplaceKW.getText()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //scroll to first keyword occurrence
                try {
                    Rectangle rectangle = jTextArea.modelToView(pos);
                    jTextArea.scrollRectToVisible(rectangle);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });

        replaceAllBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String keyword = jKeyword.getText();
                if (keyword.length() == 0)
                    return;

                String text = jTextArea.getText();
                pos = text.indexOf(keyword);
                if (pos == -1) {
                    pos = 0;
                    JOptionPane.showMessageDialog(null, "can not find " + keyword);
                    return;
                }

                jTextArea.setText(text.replace(keyword, jReplaceKW.getText()));
            }
        });

        findReplaceTool.add(jKeyword);
        findReplaceTool.add(findBtn);
        findReplaceTool.add(findPrevBtn);
        findReplaceTool.add(findAllBtn);
        findReplaceTool.addSeparator();
        findReplaceTool.add(jReplaceKW);
        findReplaceTool.add(replaceBtn);
        findReplaceTool.add(replaceAllBtn);

    }

    private void findKeyword(boolean reverse) {
        String keyword = jKeyword.getText();
        if (keyword.length() == 0 || keyword.equals(" "))
            return;

        String text = jTextArea.getText();

        if (reverse)
            pos = text.lastIndexOf(keyword, pos);
        else
            pos = text.indexOf(keyword, pos);

        if (pos == -1) {
            //could be end or begin of text, re-search
            if (reverse)
                pos = text.lastIndexOf(keyword, text.length());
            else
                pos = text.indexOf(keyword, 0);

            //still can not find, then pop up not found info
            if (pos == -1) {
                pos = 0;
                jTextArea.getHighlighter().removeAllHighlights();
                JOptionPane.showMessageDialog(null, "can not find " + keyword);
                return;
            }
        }
        int kwlength = keyword.length();
        //highlight
        try {
            jTextArea.getHighlighter().removeAllHighlights();
            jTextArea.getHighlighter().addHighlight(pos, pos + kwlength,
                    new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE));
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        //scroll to keyword
        try {
            Rectangle rectangle = jTextArea.modelToView(pos);
            jTextArea.scrollRectToVisible(rectangle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        if (reverse)
            pos = pos - kwlength;
        else
            pos = pos + kwlength;

        /*
        if (pos>text.length())
            pos = 0;
            */
    }

    public void toggleSearchReplaceTool(){
        if (findToolAvailable)
            this.remove(findReplaceTool);
        else
            this.add(findReplaceTool, BorderLayout.SOUTH);

        findToolAvailable = !findToolAvailable;

        //validate and repaint
        this.revalidate();

        if (findToolAvailable)
            jKeyword.requestFocus();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        fileBaseName = new File(fileName).getName();
    }

    public boolean isContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    public boolean isEncTypeChanged() {
        return this.encTypeChanged;
    }

    public JTextArea getjTextArea(){
        return this.jTextArea;
    }

    public String saveContent(boolean echoTitle) throws IOException, EncryptException {

        if (!contentChanged && !encTypeChanged)
            return fileName;

        if (fileName.equals("Untitled")) {
            JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));
            if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                setFileName(dialog.getSelectedFile().getAbsolutePath());
            } else {
                return fileName;
            }
        }

        String encSType = null;
        Integer ioffset = 0;

        if (jCheckBox.isSelected()){
            encSType = (String) encType.getSelectedItem();

            if (encSType != null){
                if (encSType.equals("BitReverseEncrypt")){
                    encSType = Encrypt.ENC_BR;
                } else if (encSType.equals("BitComplementEncrypt")){
                    encSType = Encrypt.ENC_BC;
                    ioffset = (Integer) offset.getSelectedItem();
                }
            }
        }

        if (encSType == null){
            FileWriter w = new FileWriter(fileName);
            jTextArea.write(w);
            w.close();
        } else {
            EncryptOutputAttachEncInfo eoei = new EncryptOutputAttachEncInfo(fileName, md5password, encSType, ioffset);
            fileName = eoei.encryptFromString(jTextArea.getText(), "UTF-8");
        }

        contentChanged = false;

        if (echoTitle)
            ((ButtonTabComponent)tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())).
                    changeTabLabelTitle("  " + fileBaseName);

        return fileName;
    }

    public void loadContent(String fileName) throws IOException, EncryptException {
        if (fileName.endsWith(Encrypt.ENC_FILE_SUFF)){
            DecryptInputDetachEncInfo dide =
                    new DecryptInputDetachEncInfo(fileName, md5password);
            this.setFileName(fileName);
            jTextArea.setText(dide.decryptToString());

            if (dide.getEncType().equals(Encrypt.ENC_BR)) {
                jCheckBox.setSelected(true);
                encType.setSelectedItem("BitReverseEncrypt");
            } else if (dide.getEncType().equals(Encrypt.ENC_BC)){
                jCheckBox.setSelected(true);
                encType.setSelectedItem("BitComplementEncrypt");
                offset.setSelectedItem(dide.getOffset());
            }
        }
        else {
            FileReader r = new FileReader(fileName);
            this.setFileName(fileName);
            jTextArea.read(r, null);
            r.close();
        }

        this.encTypeChanged = false;
    }
}
