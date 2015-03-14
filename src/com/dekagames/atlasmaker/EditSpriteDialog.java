package com.dekagames.atlasmaker;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 18.08.13
 * Time: 23:54
 * To change this template use File | Settings | File Templates.
 */
public class EditSpriteDialog extends JDialog implements ActionListener {

    private     SpriteNode      spriteNode;         // локальная копия - с ней будем работать
    private     SpriteNode      origSprite;         // оригинальная копия - ее перезапишем, если нажмем кнопку Ок
    private     boolean         bCreateNew;         // создаем новый спрайт или редактируем старый
    private	    int             spriteIndex;

    private     JSplitPane      splitPane;
    private     JButton         btnOk, btnCancel;   // стандартные кнопки диалога
    private     JPanel          leftPanel;          // панель с настройками спрайта и кадра
    private     JPanel          rightPanel;         // панель с изображением кадра
    private     JPanel          leftToolbarPanel;   // тулбар в виде панели над левой панелью
    private     JPanel          rightToolbarPanel;  // то же над правой панелью
    private     JButton         btnAdd, btnDel, btnUp, btnDown;     // кнопки левого тулбара
    private     JTextField      textSpriteName;     // поле с именем спрайта
    private     DefaultListModel    listModel;
    private     JList               listFrames;         // список кадров
    private     JPanel          bottomPanel;        // нижняя панель с настройками кадра
    private     JCheckBox       outlineCheck;       // будет дополнительная обводка крайним пикселем.
    private     JCheckBox       cropCheck;          // чекбокс кропа - обрезки пустого места у спрайта
    private     JSpinner        borderSpin;         // спиннер прозрачной границы вокруг спрайта
    private     JFileChooser    imageFileChooser;   // диалог открытия файлов изображений
    private     JSpinner        pivotXSpin, pivotYSpin; // спиннеры горячей точки
    private     DrawFramePanel  drawFramePanel;     // своя панель для изображения кадра спрайта
    private     JScrollPane     frameScroll;        // скролл панель для картинки



    public EditSpriteDialog(Frame parent, int index){
        super(parent, true);        // модальное окно
        setSize(900,600);
        setLocation(100,80);
        setTitle("Edit sprite frames");
        initControls();

        spriteIndex = index;
        if (index <0) {       // создаем новый спрайт
            bCreateNew = true;
            String name = "sprite-"+MainApplication.mainWindow.spriteTree.getCount();
            spriteNode = new SpriteNode(name);
            textSpriteName.setText(name);           // напишем имя в строке редактирования
        }
        else {                      // редактируем старый
            origSprite = MainApplication.mainWindow.spriteTree.getNodeAt(index);
            bCreateNew = false;
            spriteNode = new SpriteNode(origSprite);
            textSpriteName.setText(spriteNode.toString());
        }
        // выставим настройки на форме
        outlineCheck.setSelected(spriteNode.isOutlineWithBorder);
        cropCheck.setSelected(spriteNode.isCropped);
        borderSpin.setValue((int)spriteNode.transparentBorder);
        updateList();
//        setVisible(true);
    }

    private void initControls(){
        setLayout(new BorderLayout());
        // стандартные кнопки (как в книжке портянкина)
        btnOk = new JButton("Ok");          btnOk.addActionListener(this);
        btnCancel = new JButton("Cancel");  btnCancel.addActionListener(this);

        JPanel grid = new JPanel(new GridLayout(1,2,5,0));
        grid.add(btnOk);
        grid.add(btnCancel);
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        flow.add(grid);
        add(flow, BorderLayout.SOUTH);

        // создадим левую панель
        leftPanel = new JPanel(new BorderLayout());
        leftToolbarPanel = new JPanel(new MigLayout("wrap 4"));
        // поле с именем спрайта
        textSpriteName = new JTextField(5000);
        // кнопки тулбара левой панели
        btnAdd = new JButton(new ImageIcon(getClass().getResource("media/PictureAdd.png")));
        btnAdd.addActionListener(this);
        btnAdd.setToolTipText("Add frame");

        btnDel = new JButton(new ImageIcon(getClass().getResource("media/PictureDel.png")));
        btnDel.addActionListener(this);
        btnDel.setToolTipText("Delete frame");

        btnUp = new JButton(new ImageIcon(getClass().getResource("media/Up.png")));
        btnUp.addActionListener(this);
        btnUp.setToolTipText("Move frame up");

        btnDown = new JButton(new ImageIcon(getClass().getResource("media/Down.png")));
        btnDown.addActionListener(this);
        btnDown.setToolTipText("Move frame down");

        leftToolbarPanel.add(new JLabel("Sprite name:"), "span 4");
        leftToolbarPanel.add(textSpriteName, " span 4");

        leftToolbarPanel.add(btnAdd);
        leftToolbarPanel.add(btnDel);
        leftToolbarPanel.add(btnUp);
        leftToolbarPanel.add(btnDown, "wrap");

        leftPanel.add(leftToolbarPanel,BorderLayout.NORTH);
        // список кадров
        listModel = new DefaultListModel();
        listFrames = new JList(listModel);
        // при клике по кадру он рисуется в панели
        listFrames.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                redraw_panel();
            }
        });

        leftPanel.add(new JScrollPane(listFrames),BorderLayout.CENTER);
        // нижняя панель
        bottomPanel = new JPanel(new MigLayout());
        outlineCheck = new JCheckBox("outline with border pixels");
        cropCheck = new JCheckBox("crop sprite frames");
        borderSpin = new JSpinner(new SpinnerNumberModel(0,0,100,1));
        bottomPanel.add(outlineCheck, "wrap");
        bottomPanel.add(cropCheck,"wrap");
        bottomPanel.add(new JLabel("Leave transparent border (pixels):"));
        bottomPanel.add(borderSpin);
        leftPanel.add(bottomPanel,BorderLayout.SOUTH);

        // правая панель
        rightPanel = new JPanel(new BorderLayout());
        rightToolbarPanel = new JPanel(new MigLayout());

        // спиннер Х координаты горячей точки и реакция на его изменение
        pivotXSpin = new JSpinner(new SpinnerNumberModel(0,-10000,10000,1));
        pivotXSpin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selind = listFrames.getSelectedIndex();
                if (selind>=0){
                    FrameNode node = (FrameNode)spriteNode.getChildAt(selind);
                    node.xPivot = (Integer)pivotXSpin.getValue();
                    redraw_panel();
                }
            }
        });

        // спиннер Y координаты горячей точки и реакция на его изменение
        pivotYSpin = new JSpinner(new SpinnerNumberModel(0,-10000,10000,1));
        pivotYSpin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selind = listFrames.getSelectedIndex();
                if (selind>=0){
                    FrameNode node = (FrameNode)spriteNode.getChildAt(selind);
                    node.yPivot = (Integer)pivotYSpin.getValue();
                    redraw_panel();
                }
            }
        });

        rightToolbarPanel.add(new JLabel("Pivot coordinates:"));
        rightToolbarPanel.add(pivotXSpin);
        rightToolbarPanel.add(new JLabel(" X "));
        rightToolbarPanel.add(pivotYSpin);

        // панель для прорисовки кадра
        drawFramePanel = new DrawFramePanel();
        drawFramePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selind = listFrames.getSelectedIndex();
                if (selind>=0){
                    BufferedImage img = spriteNode.getChildAt(selind).image;
                    if (img == null) return;

                    int x = e.getX();
                    int y = e.getY();
                    if (x<img.getWidth() && y<img.getHeight()){
                        FrameNode node = (FrameNode)spriteNode.getChildAt(selind);
                        node.xPivot = x;
                        node.yPivot = y;
                        redraw_panel();
                    }
                }
            }
        });


        frameScroll = new JScrollPane(drawFramePanel);
        frameScroll.setAutoscrolls(true);

        rightPanel.add(rightToolbarPanel, BorderLayout.NORTH);
        rightPanel.add(frameScroll, BorderLayout.CENTER);

        splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane,BorderLayout.CENTER);

        // прочие элементы
        imageFileChooser = new JFileChooser();
        imageFileChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));
        imageFileChooser.setMultiSelectionEnabled(true);
    }

    public final void updateList(){
        FrameNode frameNode;
        int index = listFrames.getSelectedIndex();
        // очистим список
        listModel.clear();
        // добавим все папки в список
        for (int i=0; i<spriteNode.getChildCount(); i++) {
            frameNode = (FrameNode)spriteNode.getChildAt(i);
            listModel.addElement(frameNode.toString());
        }
        // установим прежнее выделение
        listFrames.setSelectedIndex(index);
        // перерисуем панель с картинкой
        redraw_panel();
    }

    // добавляем картинку (кадр) к спрайту
    private void cmdAddPicture(){
        // запустим диалог выбора файлов
        imageFileChooser.setCurrentDirectory(MainWindow.CURRENT_DIR);
        int returnVal = imageFileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            MainWindow.CURRENT_DIR = imageFileChooser.getCurrentDirectory();
            File[] file = imageFileChooser.getSelectedFiles();
            // добавим файлы в Folder
            for (int i=0; i<file.length; i++){
                spriteNode.addElement(new FrameNode(file[i].getPath()));
            }
            updateList();
        }
    }

    // удаляем кадр из спрайта
    private void cmdDelPicture(){
        int selind = listFrames.getSelectedIndex();
        if (selind>=0){
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,"Do you really want to delete frame?", "Confirm delete", JOptionPane.YES_NO_OPTION)) {
                spriteNode.removeElement(selind);
                updateList();
            }
        }
    }

    // двигаем картинку вверх по списку
    private void cmdUpPicture(){
        int selind = listFrames.getSelectedIndex();
        listFrames.setSelectedIndex(spriteNode.moveElementUp(selind));
        updateList();
    }


    // двигаем картинку вниз по списку
    private void cmdDownPicture(){
        int selind = listFrames.getSelectedIndex();
        listFrames.setSelectedIndex(spriteNode.moveElementDown(selind));
        updateList();
    }


    // нажата кнопка ok
    private void cmdOkButton(){
        // новое имя
        spriteNode.setName(textSpriteName.getText());
        // настройки обводки пограничным пикселем
        if (outlineCheck.getSelectedObjects() == null)
            spriteNode.isOutlineWithBorder = false;
        else
            spriteNode.isOutlineWithBorder = true;

        // настройки кропа
        if (cropCheck.getSelectedObjects() == null)	    spriteNode.isCropped = false;
        else
            spriteNode.isCropped = true;
        // настройки границы
        spriteNode.transparentBorder = (Integer)borderSpin.getValue();

        // сохраним в дереве или перезапишем
        if (bCreateNew)     MainApplication.mainWindow.spriteTree.addSprite(spriteNode);
        else		        MainApplication.mainWindow.spriteTree.replaceNode(spriteIndex, spriteNode);

        MainApplication.mainWindow.updateList();
        this.setVisible(false);
        MainWindow.NEED_TO_SAVE = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnAdd)  cmdAddPicture();
        if (obj == btnDel)  cmdDelPicture();
        if (obj == btnUp)   cmdUpPicture();
        if (obj == btnDown) cmdDownPicture();

        if (obj == btnCancel) setVisible(false);
        if (obj == btnOk)   cmdOkButton();
    }

    // перерисовка панели выбранным кадром
    private void redraw_panel(){
        int selind = listFrames.getSelectedIndex();
        if (selind>=0){
            FrameNode frame = (FrameNode)spriteNode.getChildAt(selind);
            drawFramePanel.bufImage = frame.image;
            drawFramePanel.xPivot = frame.xPivot;
            drawFramePanel.yPivot = frame.yPivot;

            pivotXSpin.setValue(drawFramePanel.xPivot);
            pivotYSpin.setValue(drawFramePanel.yPivot);

            drawFramePanel.repaint();
            frameScroll.getViewport().revalidate();
        }
    }
}

